 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package superkidsapplication.events;
 
 import com.ece.superkids.users.entities.User;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 
 /**
  *
  * @author baris
  */
 public class Session implements ActionListener {
 
     User currentUser = null;
 
     private Session() {
         System.out.println("Session created");
     }
 
     public void actionPerformed(ActionEvent e) {
         JButton button = (JButton) e.getSource();
         if (isUserLoggedIn() == false) {
             button.setVisible(false);
         }
         if (isUserLoggedIn() == true) {
             button.setVisible(false);
         }
     }
 
     private static class SessionHolder {
 
         public static final Session aSession = new Session();
     }
 
     public static Session aSession() {
 
         return Session.SessionHolder.aSession;
     }
 
     public boolean login(User user) {
         if (currentUser == null) {
             currentUser = user;
             System.out.println("User logged in: " + user.getName());
             return true;
         } else {
             System.out.println("There is a user already logged in.");
             return false;
         }
     }
 
     public boolean logout() {
         if (currentUser != null) {
             String tmpUser = currentUser.getName();
             currentUser = null;
             System.out.println("User logged out: " + tmpUser);
             return true;
         } else {
             System.out.println("This user is not logged in yet");
             return false;
         }
     }
 
     public boolean isUserLoggedIn() {
         if (currentUser == null) {
             return false;
         } else {
             return true;
         }
     }
 
     public boolean isUserLoggedIn(User user) {
        if (currentUser!=null&&currentUser.getName().equals(user.getName())){
             return true;
         } else {
             return false;
         }
     }
 
     public User getLoggedInUser() {
         return currentUser;
     }
 }
