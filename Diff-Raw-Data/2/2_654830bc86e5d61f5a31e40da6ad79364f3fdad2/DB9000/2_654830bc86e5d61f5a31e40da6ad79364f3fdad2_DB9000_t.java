 package com.dlepla.db9000;
 // Debt Buster 9000 Login Main Program
 
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import com.dlepla.db9000.lib.Reference;
 
 public class DB9000 extends JFrame
 {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     
     // Runs the DB9000 program in new Runnable queue
 
     public static void main(String[] args)
     {
 
         EventQueue.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
 
                 new DB9000();
                 
             }
         });
     }
     
    // getting reference to JFram object instance.
     
     JFrame mainWindow = this;
     
 
     public DB9000()
     {
 
         this.setTitle("Debt Blaster 9000");
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setSize(700, 450);
         this.setMinimumSize(new Dimension(715, 482));
         this.setIconImage(Toolkit.getDefaultToolkit().getImage((Reference.X16_ICON_LOCATION.toString())));
         
         LoginPanel loginPanel = new LoginPanel(mainWindow);
         this.getRootPane().setDefaultButton(loginPanel.loginButton);
         
         this.add(loginPanel);
         this.pack();
         this.setLocationRelativeTo(null);
         this.setVisible(true);
         
     }
 }
