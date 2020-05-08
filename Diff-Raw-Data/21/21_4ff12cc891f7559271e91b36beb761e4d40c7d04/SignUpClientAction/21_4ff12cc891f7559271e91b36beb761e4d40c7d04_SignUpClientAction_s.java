 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package actions;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import sessions.LoginSession;
 import sessions.Session;
 import sessions.UserSession;
 import users.Client;
 import users.Users;
 
 /**
  *
  * @author Joao
  */
 public class SignUpClientAction implements SignUpAction{
     
     LoginSession session;
     JDialog signUpDialog;
     
     public SignUpClientAction(LoginSession session) {
         this.session = session;
     }
 
     public void execute(String name, String email, String login, String password, String repeatPassword) {
         try {
             Client client = new Client(name, email, login, password, repeatPassword);
             Users.getInstance().signUp(client); 
             LoginSession.startSession(client);
             ((UserSession) Session.getInstance()).showDialog();
             session.getComponent().dispose();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(signUpDialog, e.getMessage(), "Tchu Tcha Tcha Store", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public void setSignUpDialog(JDialog dialog) {
         this.signUpDialog = dialog;
     }
     
     
 }
