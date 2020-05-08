 package models;
 
 import java.io.Serializable;
 import java.util.Vector;
 
 /** This class provides methods for user management.
  * Every instance of this class contains a reference to the associated set of users
  * and a reference to the current user. 
  */
 public class UserManager implements Serializable{
 
 	private static final long serialVersionUID = 6782682832616034376L;
 	private UserSet users;
     private User currentUser;
 
     /** Constructs a new user manager with an empty user set.
      */
     UserManager() {
         users = new UserSet(this);
     }
 
    /** Create new user with a given username and password.
      * The user is automatically added to the user set.
      * @param username string representing the username.
      * @param rawPassword char array representing the password.
      */
     public void initializeUser(String username, char[] rawPassword) {
     	User user = new User(username, rawPassword);
         DisplayState state = new DisplayState();
         EventHandler events = new EventHandler();
         UserProfile profile = new UserProfile(state, events, user);
         user.setUserProfile(profile);
         this.add(user);
     }
     
     /** Returns current user.
      * @return current user.
      */
     public User getCurrentUser() {
     	return currentUser;
     }
     
     /** Sets a given user as the current user.
      * @param user user to be set as the current user.
      */
     public void setCurrentUser(User user) {
     	currentUser = user;
     }
     
     /** Validates and returns user with a given name and password.
      * @param username string representing the username.
      * @param rawPassword char array representing the password.
      * @return user with the given username and password, null if there is no such user.
      */
     public User validateUser(String username, char[] rawPassword) {
     	User res = getByUsername(username);
         System.out.println(res);
         if(res != null && res.hasPassword(rawPassword))
     		return res;
     	return null;
     }
     
     /** Returns user with a given username.
      * @see UserSet#getByUsername(String)
      */
     public User getByUsername(String username){
     	return users.getByUsername(username);
     }
     
     /** Creates and returns new user set containing all users in the associated user set.
      * @see UserSet#all()
      */
     public UserSet all() {
         return users.all();
     }
 
     /** Returns vector containing names of all users in this set. 
      * @see UserSet#getUsernames()
      */
     public Vector<String> getUsernames() {
     	return users.getUsernames();
     }
     
     /** Adds a given user to the associated user set.
      * @param user the user to be added
      * @return <code>true</code> if the set did not already contain the specified user
      */
     public boolean add(User user) {
         return users.add(user);
     }
     
     /** Removes user with a given username and password.
      * User is first validated with {@link #validateUser(String, char[]) validateUser} method.
      * @param username string representing the username.
      * @param rawPassword char array representing the password.
      * @return <code>true</code> if removing was successful, <code>false</code> if there is no such user.
      */
     public boolean remove(String username, char[] rawPassword) {
     	User user = validateUser(username, rawPassword);
     	if(user == null)
     		return false;
     	if(user == currentUser)
     		currentUser = null;
     	users.remove(user);
     	return true;
     }
     
     /** Returns string representation of this user manager.
      * @return string representation of this user manager.
      */
     public String toString(){
     	String res = "UserManager[";
     	res += "current:" + currentUser + ", ";
     	res += users;
     	res += "]";
     	return res;
     }
 }
