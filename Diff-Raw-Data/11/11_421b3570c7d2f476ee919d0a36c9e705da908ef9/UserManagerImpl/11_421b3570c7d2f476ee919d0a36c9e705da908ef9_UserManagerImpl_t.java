 package org.imirsel.nema.service.impl;
 
 import org.imirsel.nema.dao.UserDao;
 import org.imirsel.nema.model.User;
 import org.imirsel.nema.service.UserExistsException;
 import org.imirsel.nema.service.UserManager;
 import org.imirsel.nema.service.UserService;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.dao.DaoAuthenticationProvider;
 import org.springframework.security.providers.dao.SaltSource;
 import org.springframework.security.providers.encoding.PasswordEncoder;
 import org.springframework.security.userdetails.UserDetails;
 import org.springframework.security.userdetails.UsernameNotFoundException;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.dao.DataIntegrityViolationException;
 
 import javax.jws.WebService;
 import javax.persistence.EntityExistsException;
 import javax.jcr.SimpleCredentials;
 import java.util.List;
 
 
 /**
  * Implementation of UserManager interface.
  *
  * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
  */
 @WebService(serviceName = "UserService", endpointInterface = "org.imirsel.nema.service.UserService")
 public class UserManagerImpl extends UniversalManagerImpl implements UserManager, UserService {
 	private UserDao dao;
 	private PasswordEncoder passwordEncoder;
 
 	/**
 	 * Set the Dao for communication with the data layer.
 	 * @param dao the UserDao that communicates with the database
 	 */
 	@Required
 	public void setUserDao(UserDao dao) {
 		this.dao = dao;
 	}
 
 	/**
 	 * Set the PasswordEncoder used to encrypt passwords.
 	 * @param passwordEncoder the PasswordEncoder implementation
 	 */
 	@Required
 	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
 		this.passwordEncoder = passwordEncoder;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public User getUser(String userId) {
 		return dao.get(new Long(userId));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<User> getUsers(User user) {
 		return dao.getUsers();
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public User saveUser(User user) throws UserExistsException {
 
 		if (user.getVersion() == null) {
 			// if new user, lowercase userId
 			user.setUsername(user.getUsername().toLowerCase());
 		}
 
 		// Get and prepare password management-related artifacts
 		boolean passwordChanged = false;
 		if (passwordEncoder != null) {
 			// Check whether we have to encrypt (or re-encrypt) the password
 			if (user.getVersion() == null) {
 				// New user, always encrypt
 				passwordChanged = true;
 			} else {
 				// Existing user, check password in DB
 				String currentPassword = dao.getUserPassword(user.getUsername());
 				if (currentPassword == null) {
 					passwordChanged = true;
 				} else {
 					if (!currentPassword.equals(user.getPassword())) {
 						passwordChanged = true;
 					}
 				}
 			}
 
 			// If password was changed (or new user), encrypt it
 			if (passwordChanged) {
 				user.setPassword(passwordEncoder.encodePassword(user.getPassword(), null));
 			}
 		} else {
 			log.warn("PasswordEncoder not set, skipping password encryption...");
 		}
 
 		try {
 			return dao.saveUser(user);
 		} catch (DataIntegrityViolationException e) {
 			e.printStackTrace();
 			log.warn(e.getMessage());
 			throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
 		} catch (EntityExistsException e) { // needed for JPA
 			e.printStackTrace();
 			log.warn(e.getMessage());
 			throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeUser(String userId) {
 		log.debug("removing user: " + userId);
 		dao.remove(new Long(userId));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param username the login name of the human
 	 * @return User the populated user object
 	 * @throws UsernameNotFoundException thrown when username not found
 	 */
 	public User getUserByUsername(String username) throws UsernameNotFoundException {
 		return (User) dao.loadUserByUsername(username);
 	}
 	
 	
 	/**
 	 * Returns the users credentials. 
 	 * @return
 	 */
	public SimpleCredentials getCurrentUserCredentials(){
 		User user = getCurrentUser();
 		String name=user.getPassword();
 		String password=user.getUsername();
 		return new SimpleCredentials(name,password.toCharArray());
 	}
 
 	/**
 	 * Returns the User object for the user who is currently logged in.
 	 * Returns null if no user is available.
 	 * @return
 	 */
 	public User getCurrentUser() {
 		Object obj= SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		User user = null;
 
 		if(obj==null){
 			System.out.println("HERE.... came to gt cuttrnt user");
 			if(SecurityContextHolder.getContext()==null){
 				System.out.println("SecurityContextHolder.getContext() is null");
 			}else if(SecurityContextHolder.getContext().getAuthentication()==null){
 				System.out.println("SecurityContextHolder.getContext().getAuthentication() is null");
 			}else{
 				System.out.println("SecurityContextHolder.getContext().getAuthentication().getPrincipal() is null");
 			}
 			return null;
 		}if (obj instanceof UserDetails) {
 			System.out.println("Found user detail");
 			String username = ((UserDetails)obj).getUsername();
 			user = getUserByUsername(username);
 		}else{
 			System.out.println("===> obj is: "+ obj);
 			user = getUserByUsername(obj.toString());
 		}                                                                                                                                                              
 
 		return user;                                                                                                                                                           
 	} 
 }
