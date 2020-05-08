 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package actions;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import sessions.AdminSession;
 import sessions.LoginSession;
 
 /**
  *
  * @author Joao
  */
 public class SignUpAdminAction implements SignUpAction{
     
     AdminSession session;
     JDialog signUpDialog;
     
     public SignUpAdminAction(AdminSession session) {
         this.session = session;
     }
 
     public void execute(String name, String email, String login, String password, String repeatPassword) {
         try {
             session.getAdmin().signUpAdmin(name, email, login, password, repeatPassword);
             this.signUpDialog.dispose();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(signUpDialog, e.getMessage(), "Tchu Tcha Tcha Store", JOptionPane.ERROR_MESSAGE);
         }
     }
     
     public void setSignUpDialog(JDialog dialog) {
         this.signUpDialog = dialog;
     }
     
 }
