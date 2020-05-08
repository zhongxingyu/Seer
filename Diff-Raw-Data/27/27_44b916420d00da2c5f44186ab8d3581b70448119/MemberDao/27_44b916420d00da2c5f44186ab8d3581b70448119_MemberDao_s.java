 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.repo;
 
 import java.util.List;
 
import org.jboss.as.quickstarts.kitchensink.model.Member;
 
 public interface MemberDao
 {
     public Member findById(Long id);
 
     public Member findByEmail(String email);
 
     public List<Member> findAllOrderedByName();
 
     public void register(Member member);
 }
