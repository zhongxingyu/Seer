 package domainObjects;
 
 import java.util.HashSet;
 
 /**
  *
  * @author Alex
  * @author Oliver  
  * 
  */
 public class User {
     String userName;
    String password;    
     HashSet<UserTypes.UserType> types;
    
    public User(String userName, String password) {
         this.userName = userName;
         this.password = password;
         this.types = new HashSet();
     }
     
     public void addUserType(UserTypes.UserType type) {
         types.add(type);
     }
     
     public void deleteUserType(UserTypes.UserType type) {
         types.remove(type);
     }
     
     public boolean checkUserType(UserTypes.UserType type) {
         return types.contains(type);
     }
     
     public void modifyUserName(String newUserName) {
         this.userName = newUserName;
     }
     
     public void modifyPassword(String newPassword) {
         this.password = newPassword;
     }
     
     public boolean checkPassword(String password) {
         return this.password.equals(password);
     }
     
     public String getUser() {
         return this.userName;
     }
     
     public String getPassword() {
         return this.password;
     }
    /* 
     public UserTypes.UserType[] getUserTypes() {
         return (UserTypes.UserType[]) this.types.toArray();
     }
     */
 }
