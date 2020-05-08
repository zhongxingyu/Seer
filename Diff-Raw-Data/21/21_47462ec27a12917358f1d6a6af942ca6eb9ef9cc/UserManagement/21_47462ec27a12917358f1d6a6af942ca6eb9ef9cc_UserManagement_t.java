 package battleship.logic;
 
 import battleship.data.UserFile;
import battleship.objects.User;
 import battleship.objects.DuplicateUsersException;
 import battleship.objects.IncompleteDataException;
 import battleship.objects.NotMatchingPasswordsException;
 import battleship.objects.WrongCredentialsException;
 
 import java.io.IOException;
 import java.util.UUID;
 
 
 /**
  * Create new useers, login existing users and request the currently logged in userManagement.
  *
  * @author Hinrich Kaestner, Tom Ohme
  */
 public class UserManagement {
     private static UserManagement userManagement = null;
    private User oUser;
 
     private UserManagement() {
 
     }
 
     /**
      * This class is a singleton.
      *
      * @return an instance of UserManagement
      */
     public static UserManagement getInstance() {
         if(null == userManagement) {
             userManagement = new UserManagement();
         }
         return userManagement;
     }
 
     /**
      * Checks if the entered data is valid and creates a new userManagement.
      *
      * @param username userManagement name
      * @param password1 password, first entry
      * @param password2 password, second entry
      * @return userManagement ID
      * @throws DuplicateUsersException
      * @throws NotMatchingPasswordsException
      * @throws IncompleteDataException
      * @throws IOException
      */
     public String createNewUser(String username, String password1, String password2) throws DuplicateUsersException, NotMatchingPasswordsException, IncompleteDataException, IOException {
         if (username.equals("") || password1.equals("") || password2.equals("")) {
             throw new IncompleteDataException("Please fill out all fields.");
         }
         UserFile dUserFile = new UserFile();
         if (dUserFile.checkUsernameExists(username)) {
             throw new DuplicateUsersException("This username has already been taken. Please choose another one.");
         }
         if (!password1.equals(password2)) {
             throw new NotMatchingPasswordsException("The passwords do not match. Please try again.");
         }
 
         String uid = UUID.randomUUID().toString();
         oUser = new battleship.objects.User(uid, username, password2);
         dUserFile.store(oUser);
         return uid;
     }
 
     /**
      * Checks if the entered data is valid and logs the userManagement in.
      *
      * @param username userManagement name
      * @param password password
      * @throws WrongCredentialsException
      * @throws IncompleteDataException
      */
     public void login(String username, String password) throws WrongCredentialsException, IncompleteDataException {
         if (username.equals("") || password.equals("")) {
             throw new IncompleteDataException("Please fill out all fields.");
         }
         UserFile dUserFile = new UserFile();
         oUser = dUserFile.get(username, password);
         if(null == oUser) {
            throw new WrongCredentialsException("An user with the given credentials does not exist. Please register if you have not already done so.");
         }
     }
 
     /**
      * Returns the current userManagement.
      *
      * @return current userManagement
      */
     public battleship.objects.User getUser() {
         return oUser;
     }
 }
