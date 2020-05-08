 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package catchatbb;
 
 import static catchatbb.AddUserBB.PU;
 import catchatmodel.Chat;
 import catchatmodel.ChatFactory;
 import catchatmodel.UserAccount;
 import java.io.Serializable;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Named;
 
 /**
  *
  * @author Nora
  */
 @Named("status")
 @SessionScoped
 public class StatusManager implements Serializable {
     private String username;
     protected static String currentuser;
     private String password;
     private Chat chat;
     
     /**
     * Changes useraccount-status to online and forward to chatroom.xhtml if the useraccount is found in database.
      * 
      * @return navigation strings
      */
     public String login() {   
         chat = ChatFactory.getChat(PU);
         UserAccount user = chat.getByName(username);
         if((user != null) && (password.equals(user.getPassword()))){
             currentuser=username;
             user.setStatus("online");
             chat.update(user);
             return "LOGIN_SUCCESS";
         }
         else
             return "LOGIN_FAIL";
     }
     
     /**
     * Changes useraccount-status to offline and forward to index.xhtml if the useraccount is found in database.
      * 
      * @return navigation strings
      */
     public String logout() {   
         chat = ChatFactory.getChat(PU);
         UserAccount user = chat.getByName(username);
         
         if(user != null){
             user.setStatus("offline");
             chat.update(user);
             return "LOGOUT_SUCCESS";
         }
         else
             return "LOGOUT_FAIL";
     }
     
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 }
