 package cz.cvut.fel.bupro.security;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fel.bupro.dao.UserRepository;
 import cz.cvut.fel.bupro.model.User;
 
 @Service
 public class SpringSecurityService implements SecurityService {
 	private final Log log = LogFactory.getLog(getClass());
 
 	@Autowired
 	private UserRepository userRepository;
 
 	private User find(Authentication authentication) {
 		Object o = authentication.getPrincipal();
 		if (o instanceof User) {
			return userRepository.findOne(((User) o).getId());
 		}
 		if (o instanceof UserDetails) {
 			log.trace("Principal type UserDetails");
 			UserDetails userDetails = (UserDetails) o;
 			return userRepository.findByUsername(userDetails.getUsername());
 		}
 		if (o instanceof String) {
 			log.trace("Principal type plain username");
 			return userRepository.findByUsername(String.valueOf(o));
 		}
 		throw new IllegalStateException("Unknown type of principal " + o.getClass());
 	}
 
 	@Transactional
 	public User getCurrentUser() {
 		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		if (authentication == null) {
 			log.info("Authentication information not available!");
 			return null;
 		}
 		User user = find(authentication);
 		if (user == null) {
 			log.info("User instance not found!");
 		}
 		return user;
 	}
 
 	@Transactional
 	public User createUser(User user) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
