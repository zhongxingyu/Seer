 package davmail.tray;
 
 import davmail.Settings;
 import davmail.ui.AboutFrame;
 import davmail.ui.SettingsFrame;
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 import org.apache.log4j.lf5.LF5Appender;
 import org.apache.log4j.lf5.LogLevel;
 import org.apache.log4j.lf5.viewer.LogBrokerMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * Tray icon handler based on SWT
  */
 public class SwtGatewayTray implements DavGatewayTrayInterface {
     protected SwtGatewayTray() {
     }
 
     private static TrayItem trayItem = null;
     private static java.awt.Image awtImage = null;
     private static Image image = null;
     private static Image image2 = null;
     private static Image inactiveImage = null;
     private static Display display;
     private static Shell shell;
     private boolean isActive = true;
 
     public java.awt.Image getFrameIcon() {
         return awtImage;
     }
 
     public void switchIcon() {
         isActive = true;
         display.syncExec(new Runnable() {
             public void run() {
                 if (trayItem.getImage() == image) {
                     trayItem.setImage(image2);
                 } else {
                     trayItem.setImage(image);
                 }
             }
         });
 
     }
 
     public void resetIcon() {
         display.syncExec(new Runnable() {
             public void run() {
                 trayItem.setImage(image);
             }
         });
     }
 
     public void inactiveIcon() {
         isActive = false;
         display.syncExec(new Runnable() {
             public void run() {
                 trayItem.setImage(inactiveImage);
             }
         });
     }
 
     public boolean isActive() {
         return isActive;
     }
 
     public void displayMessage(final String message, final Priority priority) {
         if (trayItem != null) {
             display.asyncExec(new Runnable() {
                 public void run() {
                     int messageType = 0;
                     if (priority == Priority.INFO) {
                         messageType = SWT.ICON_INFORMATION;
                     } else if (priority == Priority.WARN) {
                         messageType = SWT.ICON_WARNING;
                     } else if (priority == Priority.ERROR) {
                         messageType = SWT.ICON_ERROR;
                     }
                     if (messageType == 0) {
                         trayItem.setToolTipText("DavMail gateway \n" + message);
                     } else {
                         final ToolTip toolTip = new ToolTip(shell, SWT.BALLOON | messageType);
                         toolTip.setText("DavMail gateway");
                         toolTip.setMessage(message);
                         trayItem.setToolTip(toolTip);
                         toolTip.setVisible(true);
                     }
                 }
             });
         }
     }
 
     /**
      * Load image with current class loader.
      *
      * @param fileName image resource file name
      * @return image
      */
     public static Image loadSwtImage(String fileName) {
         Image result = null;
         try {
             ClassLoader classloader = DavGatewayTray.class.getClassLoader();
             URL imageUrl = classloader.getResource(fileName);
             result = new Image(display, imageUrl.openStream());
         } catch (IOException e) {
             DavGatewayTray.warn("Unable to load image", e);
         }
         return result;
     }
 
     public void init() {
         // set native look and feel
         try {
             String lafClassName = UIManager.getSystemLookAndFeelClassName();
             // workaround for bug when SWT and AWT both try to access Gtk
             if (lafClassName.indexOf("gtk") > 0) {
                 lafClassName = UIManager.getCrossPlatformLookAndFeelClassName();
             }
             UIManager.setLookAndFeel(lafClassName);
         } catch (Exception e) {
             DavGatewayTray.warn("Unable to set look and feel");
         }
 
         new Thread("SWT") {
             public void run() {
                 display = new Display();
                 shell = new Shell(display);
 
                 final Tray tray = display.getSystemTray();
                 if (tray != null) {
 
                     trayItem = new TrayItem(tray, SWT.NONE);
                     trayItem.setToolTipText("DavMail gateway");
 
                     awtImage = DavGatewayTray.loadImage("tray.png");
                     image = loadSwtImage("tray.png");
                     image2 = loadSwtImage("tray2.png");
                     inactiveImage = loadSwtImage("trayinactive.png");
 
                     trayItem.setImage(image);
 
                     // create a popup menu
                     final Menu popup = new Menu(shell, SWT.POP_UP);
                     trayItem.addListener(SWT.MenuDetect, new Listener() {
                         public void handleEvent(Event event) {
                             display.asyncExec(
                                     new Runnable() {
                                         public void run() {
                                             popup.setVisible(true);
                                         }
                                     });
                         }
                     });
 
                     MenuItem aboutItem = new MenuItem(popup, SWT.PUSH);
                     aboutItem.setText("About...");
                     final AboutFrame aboutFrame = new AboutFrame();
                     aboutItem.addListener(SWT.Selection, new Listener() {
                         public void handleEvent(Event event) {
                             display.asyncExec(
                                     new Runnable() {
                                         public void run() {
                                             aboutFrame.update();
                                             aboutFrame.setVisible(true);
                                         }
                                     });
                         }
                     });
 
                     final SettingsFrame settingsFrame = new SettingsFrame();
                     trayItem.addListener(SWT.DefaultSelection, new Listener() {
                         public void handleEvent(Event event) {
                             display.asyncExec(
                                     new Runnable() {
                                         public void run() {
                                             settingsFrame.reload();
                                             settingsFrame.setVisible(true);
                                             // workaround for focus on first open
                                             settingsFrame.setVisible(true);
                                         }
                                     });
                         }
                     });
 
                     // create menu item for the default action
                     MenuItem defaultItem = new MenuItem(popup, SWT.PUSH);
                     defaultItem.setText("Settings...");
                     defaultItem.addListener(SWT.Selection, new Listener() {
                         public void handleEvent(Event event) {
                             display.asyncExec(
                                     new Runnable() {
                                         public void run() {
                                             settingsFrame.reload();
                                             settingsFrame.setVisible(true);
                                             // workaround for focus on first open
                                             settingsFrame.setVisible(true);
                                         }
                                     });
                         }
                     });
 
                     MenuItem logItem = new MenuItem(popup, SWT.PUSH);
                     logItem.setText("Show logs...");
                     logItem.addListener(SWT.Selection, new Listener() {
                         public void handleEvent(Event event) {
                             display.asyncExec(
                                     new Runnable() {
                                         public void run() {
 
                                             Logger rootLogger = Logger.getRootLogger();
                                             LF5Appender lf5Appender = (LF5Appender) rootLogger.getAppender("LF5Appender");
                                             if (lf5Appender == null) {
                                                 lf5Appender = new LF5Appender(new LogBrokerMonitor(LogLevel.getLog4JLevels()) {
                                                     protected void closeAfterConfirm() {
                                                         hide();
                                                     }
                                                 });
                                                 lf5Appender.setName("LF5Appender");
                                                 rootLogger.addAppender(lf5Appender);
                                             }
                                             lf5Appender.getLogBrokerMonitor().show();
                                         }
                                     });
                         }
                     });
 
                     MenuItem exitItem = new MenuItem(popup, SWT.PUSH);
                     exitItem.setText("Exit");
                     exitItem.addListener(SWT.Selection, new Listener() {
                         public void handleEvent(Event event) {
                             shell.dispose();
 
                             if (image != null) {
                                 image.dispose();
                             }
                             if (image2 != null) {
                                 image2.dispose();
                             }
                             display.dispose();
 
                             //noinspection CallToSystemExit
                             System.exit(0);
                         }
                     });
 
                     // display settings frame on first start
                     if (Settings.isFirstStart()) {
                         settingsFrame.setVisible(true);
                     }
 
                     while (!shell.isDisposed()) {
                         if (!display.readAndDispatch()) {
                             display.sleep();
                         }
                     }
 
                     if (image != null) {
                         image.dispose();
                     }
                     if (image2 != null) {
                         image2.dispose();
                     }
                     display.dispose();
                 }
             }
         }.start();
     }
 
 }
