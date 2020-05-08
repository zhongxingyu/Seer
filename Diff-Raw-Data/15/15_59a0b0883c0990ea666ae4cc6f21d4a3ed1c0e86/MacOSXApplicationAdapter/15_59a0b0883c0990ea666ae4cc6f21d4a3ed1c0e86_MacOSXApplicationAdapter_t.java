 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package booknaviger.macworld;
 
 import booknaviger.MainInterface;
 import com.apple.eawt.AboutHandler;
 import com.apple.eawt.AppEvent.AboutEvent;
 import com.apple.eawt.AppEvent.AppReOpenedEvent;
 import com.apple.eawt.AppEvent.QuitEvent;
 import com.apple.eawt.AppReOpenedListener;
 import com.apple.eawt.QuitHandler;
 import com.apple.eawt.QuitResponse;
 import java.awt.SystemTray;
 
 /**
  *
  * @author Inervo
  */
 public class MacOSXApplicationAdapter {
 
 
     /**
      * Constructeur de l'adapteur de l'application pour Mac OS X
      * @param mainInterace instance de BookNavigerApp
      */
    public MacOSXApplicationAdapter(final MainInterface mainInterface) {
         com.apple.eawt.Application.getApplication().setAboutHandler(new AboutHandler() {
 
             /**
              * Handle sur le menu about
              * @param ae Event
              */
             @Override
             public void handleAbout(AboutEvent ae) {
                 //mainInterface.showAboutBox();
             }
         });
         com.apple.eawt.Application.getApplication().setQuitHandler(new QuitHandler() {
 
             /**
              * Handle sur le quit
              * @param ae Event
              */
             @Override
             public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                 MainInterface.getInstance().exit();
             }
         });
         com.apple.eawt.Application.getApplication().addAppEventListener(new AppReOpenedListener() {
 
             /**
              * Handle sur le fait de cliquer sur le logo de l'appli
              * @param ae Event
              */
             @Override
             public void appReOpened(AppReOpenedEvent aroe) {
                 if (MainInterface.getInstance().isDisplayable() && !MainInterface.getInstance().isVisible()) {
                     MainInterface.getInstance().setVisible(true);
                 }
                 if (MainInterface.getInstance().getReadInterface() == null) {
                     return;
                 }
                 if (MainInterface.getInstance().getReadInterface().isDisplayable() && !MainInterface.getInstance().getReadInterface().isVisible()) {
                     MainInterface.getInstance().getReadInterface().setVisible(true);
                     SystemTray sysTray = SystemTray.getSystemTray();
                     sysTray.remove(sysTray.getTrayIcons()[0]);
                 }
             }
         });
        com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(mainInterface, true);
         com.apple.eawt.Application.getApplication().setDockIconImage(new javax.swing.ImageIcon(getClass().getResource(java.util.ResourceBundle.getBundle("booknaviger/resources/Application").getString("appLogoIcon"))).getImage());
     }
     
     public static boolean isMac() {
         return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
     }
     
     public static void setMacInterfaceAndCommands() {
             System.setProperty("apple.laf.useScreenMenuBar", "true");
             System.setProperty("com.apple.mrj.application.apple.menu.about.name", "BookNaviger"); // <--Useless / --> -Xcode:name="BookNaviger"
     }
 }
