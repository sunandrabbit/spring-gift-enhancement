package gift.service;

import gift.domain.Member;
import gift.domain.Token;
import gift.entity.MemberEntity;
import gift.error.AlreadyExistsException;
import gift.error.ForbiddenException;
import gift.repository.MemberRepository;
import gift.util.JwtUtil;
import gift.util.PasswordUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Token register(Member member) {
        memberRepository.findByEmail(member.getEmail())
            .ifPresent(m -> { throw new AlreadyExistsException("Already Exists Member"); });
        member.setPassword(PasswordUtil.encodePassword(member.getPassword()));
        MemberEntity loginMemberEntity = memberRepository.save(dtoToEntity(member));
        return new Token(jwtUtil.generateToken(entityToDto(loginMemberEntity)));
    }

    @Transactional
    public Token login(Member member) {
        MemberEntity memberEntity = memberRepository.findByEmail(member.getEmail())
            .orElseThrow(() -> new ForbiddenException("Invalid email or Not Exists Member"));
        if (PasswordUtil.matches(member.getPassword(), memberEntity.getPassword())) {
            return new Token(jwtUtil.generateToken(entityToDto(memberEntity)));
        }
        throw new ForbiddenException("Invalid password");
    }

    private Member entityToDto(MemberEntity memberEntity) {
        return new Member(memberEntity.getId(), memberEntity.getEmail(),
            memberEntity.getPassword());
    }

    private MemberEntity dtoToEntity(Member member) {
        return new MemberEntity(member.getEmail(), member.getPassword());
    }

}