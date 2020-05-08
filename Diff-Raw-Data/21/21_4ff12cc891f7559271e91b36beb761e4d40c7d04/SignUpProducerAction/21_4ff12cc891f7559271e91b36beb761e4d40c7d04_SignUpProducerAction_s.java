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
 public class SignUpProducerAction implements SignUpAction{
     
     AdminSession session;
     JDialog signUpDialog;
     
     public SignUpProducerAction(AdminSession session) {
         this.session = session;
     }
 
     public void execute(String name, String email, String login, String password, String repeatPassword) {
         try {
             session.getAdmin().signUpProducer(name, email, login, password, repeatPassword);
             session.buildProducersTable();
             this.signUpDialog.dispose();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(signUpDialog, e.getMessage(), "Tchu Tcha Tcha Store", JOptionPane.ERROR_MESSAGE);
         }
     }
     
     public void setSignUpDialog(JDialog dialog) {
         this.signUpDialog = dialog;
     }
     
 }
