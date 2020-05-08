 
 package models.user;
 
 /**
  * Modeling UserID
  * @author Sander Demeester
  */
 public class UserID {
 
     private String userID;
 
     public UserID(String UserID){
         this.userID = UserID;
     }
 
     /**
      *
      * @return Get the users ID.
      */
    public String getUserID(){
         return userID;
     }
 
     /**
      * Add userID to user.
      * @param UserID
      */
     public void setUserID(String userID){
         this.userID = userID;
     }
 
 }
