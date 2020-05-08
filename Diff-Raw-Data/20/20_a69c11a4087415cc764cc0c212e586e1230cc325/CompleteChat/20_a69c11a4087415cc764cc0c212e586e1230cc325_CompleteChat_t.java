 package client;
 
 import java.awt.CardLayout;
 import java.io.PrintWriter;
 
 import javax.swing.*;
 
 import client.gui.*;
 
 public class CompleteChat {
 
     private final LoginWindow login;
     private final MainWindow main;
     private Client c = null;
     
     public CompleteChat() {
         main = new MainWindow();
         login = new LoginWindow(main);
         
     }
     
     public void start() {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 main.pack();
                 main.setLocationRelativeTo(null);
                 main.setVisible(true);
                 c = login.getClient();
                 if (c == null) {
                     System.out.println("closed login window");
                     main.dispose();
                 }
                 else {
                     main.setClient(c);
                    c.start(main);
                 }
                 
                 // Get the output stream from the client here and then
                 // run the processing things and send them to the main
             }
         });
     }
     
     public static void main(final String[] args) {
         CompleteChat CC = new CompleteChat();
         CC.start();
     }
 }
