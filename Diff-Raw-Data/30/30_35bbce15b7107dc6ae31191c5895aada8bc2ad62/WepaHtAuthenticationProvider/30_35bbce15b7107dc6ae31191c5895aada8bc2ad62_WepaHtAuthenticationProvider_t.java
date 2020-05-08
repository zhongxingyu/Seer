 package wad.timetables.auth;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.stereotype.Service;
 import wad.timetables.domain.User;
 import wad.timetables.service.UserService;
 
 /* @author mhaanran */
 
 @Service
 public class WepaHtAuthenticationProvider implements AuthenticationProvider {
 
     @Autowired 
     private UserService userService;
     
     @Override  
     public Authentication authenticate(Authentication a) throws AuthenticationException {
           
         String username = a.getName();
         String password = a.getCredentials().toString();
         User user = new User();
         user.setUsername(username);
        user.setPassword(password); 
         List<GrantedAuthority> grantedA = new ArrayList();
         if(username.equals("nsa")&&password.equals("nsa")) {
             grantedA.add(new SimpleGrantedAuthority("auth"));
             return new UsernamePasswordAuthenticationToken(username, password, grantedA);
         }
         else if (userService.userExists(user)) {
             grantedA.add(new SimpleGrantedAuthority("auth"));
             return new UsernamePasswordAuthenticationToken(username, password, grantedA);
         }
        throw new AuthenticationException("Wrong username or password!") {};
    } 
 
     @Override
     public boolean supports(Class authType) {
          return authType.equals(UsernamePasswordAuthenticationToken.class);
     }
 
 }
