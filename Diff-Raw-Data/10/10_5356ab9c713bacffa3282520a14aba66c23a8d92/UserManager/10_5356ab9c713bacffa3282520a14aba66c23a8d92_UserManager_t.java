 package org.mdissjava.mdisscore.controller.bll;
 
 import java.util.List;
 
 import org.mdissjava.mdisscore.model.pojo.User;
 
 public interface UserManager {
 	
 	void saveUser(User user);
 	void deleteUser(User user);
 //	void loggedIn(ObjectId id);
 	User getUserById(int id);
 	User getUserByNick(String userNickname);
 	//void modifyAddress(ObjectId id, Address address);
 	//void modifyConfiguration(ObjectId id, Configuration conf);
 	List<User> findFollows(String userNickname, int pageNumber, int maxResults);
 	List<User> findFollowers(String userNickname, int pageNumber, int maxResults);
 	boolean changePassword(User user,String oldPassword ,String newPassword);
 	boolean emailAlreadyExists(String email);
 	boolean nickAlreadyExists(String userNickname);
 	public void addFollow(String userNickname, User follow);
 	public void addFollower(String userNickname, User follower);
 	public void activateUser(int idUser);
 	boolean followsUser(String userNickname, User follow);
 	void deleteFollow (String userNickname, User follow);
 	void deleteFollower(String userNickname, User follower);
 	int getTotalUsers();
 }
 	
