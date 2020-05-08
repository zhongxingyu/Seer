 package apache2tray;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class TrayHandler {
     private Image[]    statusImages = new Image[2];
     private String[]   statusText   = new String[2];
     private TrayIcon   trayicon;
     private SystemTray systemtray;
     /**
      * The state of apache when last calling updateTrayIcon(). Used to 
      * prevent a periodical update of the tray icon with the same image.
      */
     private Boolean    lastState;
     
     /**
      * Adds the status Images to the statusImages array, the status text and gets the system tray.
      */
     public TrayHandler() {
         this.statusImages[0] = Toolkit.getDefaultToolkit().getImage("src/apache2tray/images/active.png");
         this.statusImages[1] = Toolkit.getDefaultToolkit().getImage("src/apache2tray/images/inactive.png");
         
         this.statusText[0] = "Apache is running";
         this.statusText[1] = "Apache is not running";
         
        this.systemtray = SystemTray.getSystemTray();
     }
     
     /**
      * Checks if Apache is running or not and sets the adequate icon and status text. Only used on
      * Application startup.
      * @param state Weather Apache is running (true) or not (false) 
      */
     public void setTrayIcon(Boolean state) {
         if (true == state) {
             this.trayicon = new TrayIcon(this.statusImages[0], this.statusText[0]);
         } else {
             this.trayicon = new TrayIcon(this.statusImages[1], this.statusText[1]);
         }
         
         this.trayicon.setImageAutoSize(true);  
         
         try {
             this.systemtray.add(this.trayicon);
             this.addMenuItems();
         } catch (AWTException e) {
             System.err.println("TrayIcon could not be added.");
             System.out.println("Exiting...");
             System.exit(0);
         }
     }
     
     /**
      * Checks if the state of Apache has changed since the last time this function
      * was called and if so updates the Apache icon & status text. 
      * @param state Weather Apache is running (true) or not (false) 
      */
     public void updateTrayIcon(Boolean state) {
         if (lastState == state) {
             return;
         }
         
         if (true == state) {
             this.trayicon.setImage(this.statusImages[0]);
             this.trayicon.setToolTip(this.statusText[0]);
         } else {
             this.trayicon.setImage(this.statusImages[1]);
             this.trayicon.setToolTip(this.statusText[1]);
         }
         
         this.lastState = state;
     }
     
     /**
      * Adds the menu items (currently only an exit button) and attaches action
      * listeners to them.
      */
     private void addMenuItems() {
         PopupMenu popup      = new PopupMenu();
         MenuItem defaultItem = new MenuItem("Exit");
         
         defaultItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Exiting...");
                 System.exit(0);
             }
         });
         
         popup.add(defaultItem);
         
         this.trayicon.setPopupMenu(popup);
     }
 }
