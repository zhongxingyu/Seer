 package edu.osu.picola.beans;
 
 import edu.osu.picola.dao.CourseDAO;
 import edu.osu.picola.dao.UserDAO;
 import edu.osu.picola.dataobjects.Course;
 import edu.osu.picola.dataobjects.User;
 import java.io.Serializable;
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author akers.79
  */
 @ManagedBean
 @SessionScoped
 public class LoginBean implements Serializable{
     private String username;
     private String password;
     private String firstName;
     private String userImagePath;
     private String Description;
     private String gender;
     private Date updateDate;
     private Date birthday;
     public static User user;
 
     public String loginUser() {
         System.out.println("Username: " + username);
         System.out.println("Password: " + password);
         boolean wasLogged = false;
         user = UserDAO.getUserByLoginInfo(username);
         System.out.println("User Object: " + user);
         // check user exists
         if (user != null) {
             // TODO GET PASSWORD
             // TODO check user password
            //wasLogged = password.equals("password");
            wasLogged = UserDAO.verifyPassword(password, user.getUser_id());
             if (wasLogged) {
                 // update login time
                 user.setLast_login_date(new Timestamp(System.currentTimeMillis()));
                 UserDAO.updateUser(user);
                 
                 switch(user.getRole()){
                     case(1):
                         return "instructor";
                     case(2):
                         return "student";
                     case(3):
                         return "Role3";
                 }
                 
                 return "failure";
             }
         }
         System.out.println("TESTING LOGIN! returned: " + wasLogged);
         return "failure";
     }
     
     public String getUserRole(){
         if(user != null){
             switch(user.getRole()){
                     case(1):
                         return "instructor";
                     case(2):
                         return "student";
                     case(3):
                         return "Role3";
                 }
             
         }
         return "failure";
     }
     
     public String logoutUser() {
         user = null;
         username = "";
         password = "";
         return "success";
     }
     
     public String setupManagement(){
         if(user != null){
             this.userImagePath = user.getImg_path();
             this.Description = user.getProfile_decr();
             this.birthday = user.getBirthday();
             this.firstName = user.getF_name();
             this.updateDate = new Date(user.getLast_update().getTime());
             this.gender = user.getGender();
             return "success";
         }
         return "failure";
     }
     
     public boolean isUserLoggedIn(){
         System.out.println("CURRENT USER: " + user);
         return (user != null);
     }
     
     public String getName() {
         return username;
     }
 
     public void setName(String name) {
         this.username = name;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
     
     
 
     public String getUserImagePath() {
         return userImagePath;
     }
 
     public void setUserImagePath(String userImagePath) {
         this.userImagePath = userImagePath;
     }
 
     public String getDescription() {
         return Description;
     }
 
     public void setDescription(String Description) {
         this.Description = Description;
     }
 
     public Date getBirthday() {
         return birthday;
     }
 
     public void setBirthday(Date birthday) {
         this.birthday = birthday;
     }
 
     public String getGender() {
         return gender;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public Date getUpdateDate() {
         return updateDate;
     }
 
     public void setUpdateDate(Date updateDate) {
         this.updateDate = updateDate;
     }
     
     public int getLoggedInUserId(){
         return user.getUser_id();
     }
     
     public void revertImagePath(ActionEvent ae){
         System.out.println("Reverting Image Path");
         this.userImagePath = user.getImg_path();
     }
     
     public void testImagePath(ActionEvent ae){
         System.out.println("testing Image Path");
         
     }
     
     public void upDateInformation(ActionEvent ae){
         System.out.println("Changing information for User " + user.getF_name());
         System.out.println("Current Info: ");
         System.out.println("    BirthDay: " + user.getBirthday());
         System.out.println("    ImagePath: " + user.getImg_path());
         System.out.println("    Description: " + user.getProfile_decr());
         System.out.println();
         System.out.println("New Info to Set: ");
         System.out.println("    BirthDay: " + this.birthday);
         System.out.println("    ImagePath: " + this.userImagePath);
         System.out.println("    Description: " + this.Description);
         System.out.println();
         user.setBirthday(this.birthday);
         user.setImg_path(this.userImagePath);
         user.setProfile_decr(this.Description);
         user.setLast_update(new Timestamp(System.currentTimeMillis()));
         
         UserDAO.updateUser(user);
         
         this.birthday = user.getBirthday();
         this.userImagePath = user.getImg_path();
         this.Description = user.getProfile_decr();
         
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Success:", "Account Updated"));
     }
 }
