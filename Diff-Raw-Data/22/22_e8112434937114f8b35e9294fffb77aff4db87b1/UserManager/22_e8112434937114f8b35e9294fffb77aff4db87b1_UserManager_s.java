 package net.retakethe.policyauction.data.api;
 
 import java.util.List;
 
 import net.retakethe.policyauction.data.api.dao.UserDAO;
 import net.retakethe.policyauction.data.api.dao.UserPendingDAO;
 import net.retakethe.policyauction.data.api.dao.UsernameDAO;
 import net.retakethe.policyauction.data.api.types.UserID;
 import net.retakethe.policyauction.data.api.types.UserRole;
 
 /**
  * @author Mathew Hartley
  */
 public interface UserManager {
 
     public class NoSuchUserException extends Exception {
         private static final long serialVersionUID = 0L;
 
         public NoSuchUserException(UserID userID) {
             super("No user found with id '" + userID.asString() + "'");
         }
     }
 
     UserID makeUserID(String asString);
     
     List<UserRole> getUserRoles(UserID userID) throws NoSuchUserException;
 
     UserDAO getUser(UserID userID) throws NoSuchUserException;
 
     UserPendingDAO getUserPending(String email) throws NoSuchUserException;
     
     UsernameDAO getUsername(String username) throws NoSuchUserException;
 
     UserDAO createUser();
     
     UserDAO activateUser(UserDAO user);
 
     List<UserDAO> getAllUsers();
 
     /**
      * Update a user object (in the cassandra database).
      *
      * @param user the user
      */
     void update(UserDAO user);
 
     /**
      * Persist, the initial step in a user creation process. The user is expected to have stored against them: a userID, email address and activationCode.
      *
      * @param user the user
      */
     void persist(UserDAO user);
 
 	void deleteUser(UserDAO user);
 
 	void deleteUserPending(UserDAO user);
 
 }
