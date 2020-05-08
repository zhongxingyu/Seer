 package pl.edu.agh.iosr.brokers;
 
 import java.util.Collections;
 
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 public class UserService implements UserDetailsService {
 
 	private UserDao userDao;
 
 	public void setUserDao(UserDao userDao) {
 		this.userDao = userDao;
 	}
 
 	@Override
 	public UserDetails loadUserByUsername(String username)
 			throws UsernameNotFoundException {
 
 		User user = userDao.getUser(username);
 
 		if (user != null) {
 
 			return new org.springframework.security.core.userdetails.User(
 					user.getName(), user.getPassword(), true, true, true, true,
 					Collections.singleton(new SimpleGrantedAuthority(
 							"ROLE_USER")));
 
 		} else {
 			throw new UsernameNotFoundException("No user with username '"
 					+ username + "' found!");
 		}
 	}
 }
