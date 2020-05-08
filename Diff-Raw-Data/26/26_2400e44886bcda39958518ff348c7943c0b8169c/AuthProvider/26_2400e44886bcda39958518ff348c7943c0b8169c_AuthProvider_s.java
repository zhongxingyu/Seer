 package se.davvs.floorballfighters.authorization;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.web.authentication.WebAuthenticationDetails;
 import org.springframework.stereotype.Component;
 
 @Component
 public class AuthProvider implements AuthenticationProvider {
 
 	@Override
 	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
         String name = authentication.getName();
         WebAuthenticationDetails details = (WebAuthenticationDetails)authentication.getDetails();
         UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
 
         String email = String.valueOf(auth.getPrincipal());
         String password = String.valueOf(auth.getCredentials());

         List<SimpleGrantedAuthority> grantedAuths = new ArrayList<SimpleGrantedAuthority>();
         grantedAuths.add(new SimpleGrantedAuthority("ROLE_ACCESS"));
         UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(name, null, grantedAuths);
         UserDetails userDetails = new UserDetails();
         newAuth.setDetails(userDetails);
 		return newAuth;
 	}
 
 	@Override
 	public boolean supports(Class<?> authentication) {
 		return authentication.equals(UsernamePasswordAuthenticationToken.class);
 	}
 
 }
