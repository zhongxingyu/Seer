 package org.mdkt.backoffice.server.rpc;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.mdkt.backoffice.client.dto.User;
 import org.mdkt.backoffice.client.rpc.UsersService;
 import org.mdkt.library.security.users.AppRole;
 import org.mdkt.library.security.users.GaeUser;
 import org.mdkt.library.security.users.UserRegistry;
 import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
 
 public class UsersServiceImpl implements UsersService {
 	
 	private final Logger logger = Logger.getLogger(getClass());
 	
 	private UserRegistry userRegistry;
 	
 	public void setUserRegistry(UserRegistry userRegistry) {
 		this.userRegistry = userRegistry;
 	}
 
 	@Override
 	public Boolean deleteUser(String userId) {
 		return null;
 	}
 
 	@Override
 	public ArrayList<User> deleteUsers(ArrayList<String> userIds) {
 		logger.debug("Deleting " + userIds.size() + " users. " + userIds.toString());
 		userRegistry.removeUsers(userIds);
 		return null;
 	}
 
 	@Override
 	public ArrayList<User> getUserDetails() {
 		List<GaeUser> gaeUsers = userRegistry.getAllGaeUsers();
 		ArrayList<User> users =  new ArrayList<User>();
		final String currentUserId = ((GaeUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
 		for (GaeUser gu : gaeUsers) {
 			User u = new User();
 			u.setUserId(gu.getUserId());
 			u.setEmail(gu.getEmail());
 			u.setEnabled(gu.isEnabled());
 			u.setFirstname(gu.getFirstname());
 			u.setLastname(gu.getLastname());
 			u.setLastSignedIn(new Date(gu.getLastSignedIn()));
 			u.setCurrentUser(currentUserId.equals(gu.getUserId()));
 			Set<String> authorities = new HashSet<String>();
 			for (GrantedAuthority role : gu.getAuthorities()) {
 				authorities.add(role.getAuthority());
 			}
 			u.setAuthorities(authorities);
 			users.add(u);
 		}
 		return users;
 	}
 
 	@Override
 	public User getUser(String userId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public User updateUser(User user) {
 		Set<AppRole> authorities = new HashSet<AppRole>();
 		for (String role : user.getAuthorities()) {
 			authorities.add(AppRole.valueOf(role));
 		}
 		GaeUser newUser = new GaeUser(user.getUserId(), user.getEmail(), user.getFirstname(), user.getLastname(), authorities, true);
 		userRegistry.registerUser(newUser);
 		return user;
 	}
 
 	@Override
 	public String getAcceptedUser(String email) {
 		return userRegistry.findAcceptedUser(email);
 	}
 }
