 package in.ac.vit.productrating.security;
 
 import in.ac.vit.productrating.model.User;
 import in.ac.vit.productrating.service.UserService;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 public class MongoDbAuthenticationService implements UserDetailsService {
 
 	private UserService userService;
 	private transient Logger log = Logger
 			.getLogger(MongoDbAuthenticationService.class);
 
 	public MongoDbAuthenticationService(UserService userService) {
 		this.userService = userService;
 	}
 
 	@Override
 	public UserDetails loadUserByUsername(String username)
 			throws UsernameNotFoundException {
 		User user = this.userService.findByEmail(username);
 		if (user == null) {
 			this.log.warn("No User exists with username" + username);
 			throw new UsernameNotFoundException(
 					"No such user exists. Username = " + username);
 		}
 		List<GrantedAuthority> authorities = new ArrayList<>(1);
 		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
 		this.log.debug("authentication successfull");
 		return new org.springframework.security.core.userdetails.User(username,
				user.getPassword(), true, true, true, true, authorities);
 	}
 
 	protected UserService getUserService() {
 		return this.userService;
 	}
 }
