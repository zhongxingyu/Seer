 
 package models.user;
 
 import models.dbentities.UserModel;
 import play.mvc.Content;
 import play.mvc.Result;
 
 /**
  * The default abstract class for User.
  * @author Sander Demeester
 **/
 
 public abstract class User{

 	public UserModel data;
 
     /**
 	 * @param data
 	 */
 	public User(UserModel data) {
 		super();
 		this.data = data;
 	}
 
 	/**
      * Returns info about this user as a String.
      * @return Userinfo.
      */
 	
 	
 	
     public String getUserInfo() {
         return null;
     }
 
     public UserModel getData() {
 		return data;
 	}
 
 	public void setData(UserModel data) {
 		this.data = data;
 	}
 
 	/**
      * Reset the password of this user. Is delegated to the
      * AuthenticationManager.
      */
     public void resetPassword(){
 
     }
 
     /**
      * Logs out the user. Is delegated to the AuthenticationManager.
      */
     public void logout(){
 
     }
     
     /*
      * Returns the landing page. Is to be implemented by the child classes
      *@return Landing Page
      */
     public abstract Content getLandingPage();
     
     /*
      * Returns the userID
      * @return userID
      */
     public UserID getID(){
     	//TODO
     	return null;
     }
     
     /*
      * Returns the statistics page
      * @return Statistics Page
      */
     public abstract Result showStatistics();
     
     /*
      * Returns the personal info page
      * @return Personal Info Page
      */
     public Result showPersonalInformation(){
     	return null;
     }
     
 
 
 }
