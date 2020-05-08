 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BackEnd.ManagerSystem;
 
 import BackEnd.UserSystem.PasswordMismatchError;
 import BackEnd.UserSystem.IllegalCharacterException;
 import BackEnd.UserSystem.Address;
 import BackEnd.UserSystem.PhoneNumber;
 import BackEnd.UserSystem.User;
 import BackEnd.UserSystem.Participant;
 import EMS_Database.DoesNotExistException;
 import EMS_Database.InputUser;
 import EMS_Database.impl.UserData_Table;
 import java.util.ArrayList;
 
 /**
  *
  * @author David Tersoff
  */
 public class UserManager {
 
     private ArrayList<Participant> userList;
     private User selectedUser;
     private User loggedInUser;
     private UserData_Table usersTable;
 
     public UserManager()
             throws DoesNotExistException {
        usersTable = new UserData_Table();
         userList = new ArrayList<Participant>();
         rebuildUserList();
     }
     
     public UserData_Table getUsersTable(){
         return usersTable;
     }
 
     private void rebuildUserList()
             throws DoesNotExistException {
        System.out.println(usersTable.currentUIDList());
         for (Integer userID : usersTable.currentUIDList()) {
             if (usersTable.getParticipant(userID)) {
                 userList.add(rebuildParticipant(userID));
             } else {
                 userList.add((Participant) rebuildUser(userID));
             }
         }
     }
 
     private Participant rebuildParticipant(int userID)
             throws DoesNotExistException {
         Participant participant = new Participant(userID, usersTable.getFirstName(userID), usersTable.getLastName(userID),
                 usersTable.getEmail(userID));
         participant.setPhoneNumber(new PhoneNumber(usersTable.getPhone(userID)));
         participant.setAddress(new Address(usersTable.getStreet(userID), usersTable.getCity(userID),
                 usersTable.getState(userID), usersTable.getZipcode(userID), usersTable.getCountry(userID)));
         return participant;
     }
 
     private User rebuildUser(int userID)
             throws DoesNotExistException {
         User user = new User(userID, usersTable.getFirstName(userID), usersTable.getLastName(userID),
                 usersTable.getEmail(userID), usersTable.getPwd(userID));
 
         user.setAdminPrivilege(usersTable.getLevel(userID) == 1 ? true : false);
         user.setEventCreationPrivilege(usersTable.getEventCreationPrivilege(userID) == 1 ? true : false);
         user.setPhoneNumber(new PhoneNumber(usersTable.getPhone(userID)));
         user.setAddress(new Address(usersTable.getStreet(userID), usersTable.getCity(userID),
                 usersTable.getState(userID), usersTable.getZipcode(userID), usersTable.getCountry(userID)));
 
         return user;
     }
 
     public ArrayList<Participant> getUserList() {
         return userList;
     }
 
     public void setLoggedInUser(User u) {
         loggedInUser = u;
     }
 
     public void addUser(User user){
             userList.add(new User(usersTable.createUser(new InputUser(user)), user));
     }
 
     public void removeUser(User user) throws DoesNotExistException {
             usersTable.removeUser(selectedUser.getUID());
             userList.remove(selectedUser);
     }
 
     public void setSelectedUser(User selectedUser){
         this.selectedUser = selectedUser;
     }
 
     public User getSelectedUser(){
             return selectedUser;
     }
 
     public void editFirstName(String firstName, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasUserPrivilege(loggedInUser, selectedUser)) {
             selectedUser.setFirstName(firstName);
             usersTable.setFirstName(selectedUser.getUID(), firstName);
         }
         //Write to database
     }
 
     public void editLastName(String lastName, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasUserPrivilege(loggedInUser, selectedUser)) {
             selectedUser.setLastName(lastName);
             usersTable.setLastName(selectedUser.getUID(), lastName);
         }
         //Write to database
     }
 
     public void editEmailAddress(String emailAddress, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasUserPrivilege(loggedInUser, selectedUser)) {
             selectedUser.setEmailAddress(emailAddress);
             usersTable.setEmail(selectedUser.getUID(), emailAddress);
         }
         //Write to database
     }
 
     public void editAddress(Address address, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasUserPrivilege(loggedInUser, selectedUser)) {
             selectedUser.setAddress(address);
             usersTable.setAddress(selectedUser.getUID(), address);
         }
     }
 
     public void editPhoneNumber(PhoneNumber phoneNumber, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasUserPrivilege(loggedInUser, selectedUser)) {
             selectedUser.setPhoneNumber(phoneNumber);
             usersTable.setPhone(selectedUser.getUID(), phoneNumber.toString());
         }
         //Write to database
     }
 
     public void editPassword(String password, String passwordMatch, User loggedInUser) throws IllegalCharacterException, PasswordMismatchError, PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasUserPrivilege(loggedInUser, selectedUser)) {
             selectedUser.setPassword(password, passwordMatch);
             usersTable.setPwd(selectedUser.getUID(), password);
         }
     }
 
     public void editAdminPrivilege(boolean adminPrivilege, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasAdminPrivilege(loggedInUser)) {
             selectedUser.setAdminPrivilege(adminPrivilege);
             usersTable.setLevel(selectedUser.getUID(), adminPrivilege == true? 1 : 0);
         }
 
         //Write to database
     }
 
     public void editEventCreationPrivilege(boolean eventCreationPrivilege) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasAdminPrivilege(loggedInUser)) {
             selectedUser.setEventCreationPrivilege(eventCreationPrivilege);
             usersTable.setLevel(selectedUser.getUID(), eventCreationPrivilege == true? 1 : 0);
         }
 
         //Write to database
     }
 }
