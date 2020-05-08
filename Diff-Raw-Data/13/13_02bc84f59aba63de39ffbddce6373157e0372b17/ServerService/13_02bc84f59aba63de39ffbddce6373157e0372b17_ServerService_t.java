 package com.veilingsite.shared;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 import com.veilingsite.shared.domain.Auction;
import com.veilingsite.shared.domain.Bid;
 import com.veilingsite.shared.domain.Category;
 import com.veilingsite.shared.domain.User;
 
 @RemoteServiceRelativePath("myServer")
 public interface ServerService extends RemoteService {
 	
 	/**
 	 * Get a single user based on a user name.
 	 * @param s String User name of the user to be returned.
 	 * @return User returns the User if found, else null.
 	 */
 	public User getUser(String s);
 	
 	/**
 	 * adds User u to the DataStore.
 	 * @param u User The user to be added.
 	 * @return True or False if user is added
 	 */
 	public User addUser(User u);
 	
 	public void removeUser(User u);
 	
 	public void updateUser(User u);
 	public boolean doesUserExist(String userName);
 	
 	public Auction addAuction(Auction a);
 	public void updateAuction(Auction a);
 	public void removeAuction(Auction a);
 	public Auction getAuction(Long id);
 	
 	/**
 	 * Gets an ArrayList of all users currently in the DataStore.
 	 * @return ArrayList<User> ArrayList of users.
 	 */
 	public ArrayList<User> getUserList();
 	public ArrayList<Auction> findAuction(String sw, Category ct, ArrayList<Category> c, String or, String ad);
 	public User loginUser(User u) throws Exception;
 
 	public ArrayList<Auction> getAuctionList(User limitUser, Category limitCat);
 	
 	public Bid addBid(Bid b);
 	
 	public ArrayList<Category> getCategoryList();
 	public ArrayList<Category> getChildrenOfCategory(Category c);
 	public Category addCategory(Category c);
 	public Category getCategory(String s);
 	public void deleteCategory(String s) throws Exception;
 
 	public Map<String, Integer> getDayStatistics();
 
 	public Map<Integer, Integer> getDayOfWeekStatistics();
 }
