 package com.mofirouz.javapushmail.app.ui;
 
 import com.mofirouz.javapushmail.JavaPushMailLogger;
 import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.UIManager;
 
 /**
  *
  * @author Mo Firouz 
  * @since 2/10/11
  */
 public class JavaPushMail {
     private JavaPushMailFrame frame;
     private JavaPushMailAccountsManager manager;
     public static String NOTIFICATION_ICON;
     public static File NOTIFICATION_ICON_FILE;
     public static File LOG_FILE;
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
         }
 
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 JavaPushMail jpm = new JavaPushMail();
                jpm.init();
             }
         });
     }
 
     public JavaPushMail() {
         NOTIFICATION_ICON = getTempImagePath(new ImageIcon(getClass().getResource("email48x48.png")).getImage());
         NOTIFICATION_ICON_FILE = new File(NOTIFICATION_ICON);
         LOG_FILE = new File("JavaPushMail.log");
     }
 
     public void init() {
         initLogger();
         initFrame();
         initManager();
         frame.init(manager);
         frame.showMe(!frame.isUsingPerferences());
 
         manager.loadAccounts();
         //manager.readAccounts("credentials.credentials");
 
         // for the initial waiting to connect...
         if (manager.countAccounts() > 0) {
             frame.setWaitingState(true);
         }
     }
 
     private void initManager() {
         manager = new JavaPushMailAccountsManager() {
             @Override
             public void handleError(Exception ex) {
                 frame.onErrorCallback(ex);
             }
 
             @Override
             public void onModelChange() {
                 frame.updateOnModelChange();
             }
 
             @Override
             public void onStateChange() {
                 frame.updateOnStateChange();
             }
         };
     }
 
     private void initFrame() {
         String os = System.getProperty("os.name");
         if (os.contains("Mac")) {
             frame = new JavaPushMailFrameX();
         } else {
             frame = new JavaPushMailFrame();
         }
     }
 
     private void initLogger() {
         JavaPushMailLogger.setWriteFile(LOG_FILE);
         JavaPushMailLogger.setWriteToFile(true);
         JavaPushMailLogger.setLevel(0);
     }
     
     protected static String getTempImagePath(Image icon) {
         if (icon == null) {
             return null;
         }
 
         File iconFile = new File(".");
 
         try {
             iconFile = File.createTempFile("temp", ".png");
             BufferedImage bimg = new BufferedImage(icon.getWidth(null), icon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
             Graphics2D g2d = bimg.createGraphics();
             g2d.drawImage(icon, 0, 0, null);
             g2d.dispose();
             ImageIO.write(bimg, "png", iconFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         iconFile.deleteOnExit();
 
         return iconFile.getAbsolutePath();
     }
 }
