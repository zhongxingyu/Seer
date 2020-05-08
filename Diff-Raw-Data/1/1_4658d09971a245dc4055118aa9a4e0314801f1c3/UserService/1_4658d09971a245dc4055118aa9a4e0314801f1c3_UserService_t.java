 package scrumter.service;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
 import org.springframework.stereotype.Service;
 
 import scrumter.model.entity.Authority;
 import scrumter.model.entity.User;
 import scrumter.model.repository.AuthorityRepository;
 import scrumter.model.repository.UserRepository;
 
 @Service
 public class UserService {
 
 	private Logger logger = Logger.getLogger(UserService.class);
 
 	@Autowired
 	private MessageDigestPasswordEncoder passwordEncoder;
 
 	@Autowired
 	private UserRepository userRepository;
 
 	@Autowired
 	private AuthorityRepository authorityRepository;
 
 	public void addUser(User user) {
		user.setPassword(encodePassword(user.getPassword()));
 		userRepository.create(user);
 	}
 
 	public void updateUser(User user) {
 		userRepository.update(user);
 	}
 
 	public User findUserById(Long id) {
 		return userRepository.findById(id);
 	}
 
 	public User findUserByEmail(String email) {
 		return userRepository.findByEmail(email);
 	}
 
 	public User findUserByUsernameAndCompany(String username, String company) {
 		return userRepository.findByUsernameAndCompany(username, company);
 	}
 
 	public List<User> findUsersByCompany(String company) {
 		return userRepository.findAllByCompany(company);
 	}
 
 	public void changePassword(User user, String password) {
 		logger.info("Changing password for user: " + user);
 		userRepository.refresh(user);
 		user.setPassword(encodePassword(password));
 		userRepository.update(user);
 	}
 
 	public void changePicture(User user, byte[] picture) {
 		logger.info("Changing picture for user: " + user);
 		userRepository.refresh(user);
 		user.setPicture(picture);
 		userRepository.update(user);
 	}
 
 	public boolean checkPassword(User user, String password) {
 		return user.getPassword().equals(encodePassword(password));
 	}
 
 	public String encodePassword(String password) {
 		return passwordEncoder.encodePassword(password, null);
 	}
 
 	public Authority createAuthority(String name) {
 		Authority authority = new Authority(name);
 		authorityRepository.create(authority);
 		return authority;
 	}
 
 }
