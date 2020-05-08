 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 /**
  * @author Anthony Glover
  *
  * GUI Class
  * Initial window for the application, provides a log in screen
  * for a driver. Driver uses his driver number as an ID
  */
 
 public class LoginGUI extends Window implements ActionListener
 {
   public Container pane;
   private JLabel idL = new JLabel("Driver Number"),
                  passwordL = new JLabel("Password"), 
                  loginText = new JLabel ("Please enter your details"), 
                  blank = new JLabel("");
   
   private JTextField idTF;
   private JPasswordField passwordTF;
   private JButton loginB, exitB;
   
   public static MainGUI window;
 	
   /**
    * Shows the GUI window and adds the labels and buttons
    */
   public void show(MainGUI _window)
   {
     window = _window;
     pane = window.getContentPane();
     pane.setLayout(new GridLayout(4, 2));
     
     pane.add(idL);
     idTF = new JTextField(10);
     pane.add(idTF);
     
     pane.add(passwordL);
     passwordTF = new JPasswordField(10);
     pane.add(passwordTF);
     
     pane.add(loginText);
     pane.add(blank);
     
     exitB = new JButton("Exit");
     exitB.addActionListener(this);
     pane.add(exitB);
     loginB = new JButton("Login");
     loginB.addActionListener(this);
     pane.add(loginB);
   }
 	
   /**
    * If a button has been pressed, do the appropriate response
    */
   public void actionPerformed(ActionEvent e)
   {
     if(e.getSource() == loginB)
     {
       try
       {
         Driver driver = new Driver(idTF.getText());
 
        if(driver.checkPassword(passwordTF.getText()))
           driver.showWelcome();
         else
           loginText.setText("Incorrect Login\n information");
       }
       catch(Exception ex)
       {
 	loginText.setText("Incorrect Login\n information");
       }
     }
     else if(e.getSource() == exitB)
       System.exit(0);
   }
 }
