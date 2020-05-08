 package com.marthym.oikonomos.server.services;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.mindrot.jbcrypt.BCrypt;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.context.SecurityContextImpl;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.marthym.oikonomos.client.services.AuthenticationService;
 import com.marthym.oikonomos.server.repositories.UserRepository;
 import com.marthym.oikonomos.shared.exceptions.OikonomosException;
 import com.marthym.oikonomos.shared.model.User;
 import com.marthym.oikonomos.shared.model.UserProfile;
 
 @Repository
 @Service("authenticationService")
 public class AuthenticationServiceImpl extends RemoteServiceServlet implements AuthenticationService {
 	private static final long serialVersionUID = 1L;
 	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
 	
 	@Autowired
 	private UserRepository userRepository;
 
 	@Override
 	public User authenticate(String username, String password) throws OikonomosException {
 		if (password == null || password.length() < 8) {
 			throw new OikonomosException("error.message.user.unauthorized", "User not found !");
 		}
 		
 		long count = userRepository.count();
 		if (count == 0) {
 			LOGGER.warn("First user login ! Create administrateur : "+username);
 			String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
			User newUser = new User(username, "", username.split("@")[0], hashPassword);
			newUser.setUserProfile(UserProfile.ADMIN);
			userRepository.saveAndFlush(newUser);
 		}
		User currentUser = userRepository.findByUserEmail(username);
 		
 		if (currentUser == null || !BCrypt.checkpw(password, currentUser.getUserHashPassword())) 
 			throw new OikonomosException("error.message.user.unauthorized", "User not found !");
 		
 		// Create de session if user is valid
 		List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
 		switch (currentUser.getUserProfile()) {
 		case ADMIN:
 			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
 		case USER:
 			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
 			break;
 		default:
 			throw new OikonomosException("error.message.user.unauthorized", "User not found !");
 		}
 
 //		org.springframework.security.core.userdetails.User user = 
 //				new org.springframework.security.core.userdetails.User(
 //						currentUser.getUserEmail(), 
 //						currentUser.getUserHashPassword(), 
 //						true, true, true, true, 
 //						authorities);
 		Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, password, authorities);
 		SecurityContext sc = new SecurityContextImpl();
 		sc.setAuthentication(auth);
 		SecurityContextHolder.setContext(sc);
 		
 		return currentUser;
 	}
 
 	@Override
 	public void logout() {
 		SecurityContextHolder.clearContext();
 	}
 
 }
