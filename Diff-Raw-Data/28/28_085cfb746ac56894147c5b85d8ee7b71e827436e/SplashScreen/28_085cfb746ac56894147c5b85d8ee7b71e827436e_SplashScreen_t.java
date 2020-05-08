 package view.splashscreen;
 
 import controllers.ControllerAuthentication;
 import java.awt.BorderLayout;
 import java.awt.Toolkit;
 import javax.swing.*;
import model.dao.DAO;
 import view.frames.Login;
 import view.frames.MainFrame;
 import view.notifications.Notifications;
 
 /**
  *
  * @author skuarch
  */
 public class SplashScreen extends JFrame {
 
     private JProgressBar progressbar = null;
     private JLabel imageLabel = null;
     private Notifications notifications = null;
 
     //==========================================================================
     public SplashScreen() {
         progressbar = new JProgressBar();
         imageLabel = new JLabel();
         notifications = new Notifications();
         initComponents();
         setLocationRelativeTo(getContentPane());
     } // end SplashScreen2
 
     //==========================================================================
     private void initComponents() {
 
         try {
 
             setIconImage(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/view/images/ssnIcon.png")));
             setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
             setUndecorated(true);
             setTitle("Sispro Sniffer Network");
             getRootPane().setWindowDecorationStyle(JRootPane.NONE);
             setLayout(new BorderLayout());
 
             imageLabel.setIcon(new ImageIcon(SplashScreen.class.getResource("/view/images/splashscreen.png")));
             add(imageLabel, BorderLayout.CENTER);
             progressbar.setIndeterminate(true);
             add(progressbar, BorderLayout.SOUTH);
             progressbar.setString("loading");
 
             pack();
 
         } catch (Exception e) {
             notifications.error("error loading interface", e);
         }
 
     }
 
     //==========================================================================
     public void showSplash() {
         setVisible(true);
         loader();
     }
 
     //==========================================================================
     private void loader() {
 
         try {
 
            new ControllerAuthentication().login("1", "1");
            new DAO().hsql("from Users where user_name = '123'");
             MainFrame.getInstance();
             new Login().setVisible(true);
 
         } catch (Exception e) {
             notifications.error("error loading interface", e);
         } finally {
             setVisible(false);
         }
 
     } // end loader
 } // end class
