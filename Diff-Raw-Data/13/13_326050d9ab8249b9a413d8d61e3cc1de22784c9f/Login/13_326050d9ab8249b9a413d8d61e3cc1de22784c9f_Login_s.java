 package ausleihe.view.panel;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import ausleihe.controller.Controller;
 
 
 public class Login extends JPanel{
     private static final long serialVersionUID = 1L;
     private Controller controller;
     
     private JTextArea ta_user;
     private JTextArea ta_pasw;
     private JButton b_login;
     
     public Login(final Controller controller){
         super();
         this.controller = controller;
         
         
        ta_user = new JTextArea("Username");
        ta_pasw = new JTextArea("Password");
         
         ta_user.setPreferredSize(new Dimension(80, 20));
         ta_pasw.setPreferredSize(new Dimension(80, 20));
         
         b_login = new JButton("login");
         b_login.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent arg0) { 
                 if(controller.connect(ta_user.getText(), ta_pasw.getText())){
                     // connected
                 } else {
                     // connection failed
                 }
             }
         });
         
         add(ta_user);
         add(ta_pasw);
         add(b_login);
         
         
     }
 
     public String getUsername(){
         return ta_user.getText();
     }
     
     public String getPassword(){
         return ta_pasw.getText();
     }
     
     public JButton getLoginButton(){
         return b_login;
     }
 }
