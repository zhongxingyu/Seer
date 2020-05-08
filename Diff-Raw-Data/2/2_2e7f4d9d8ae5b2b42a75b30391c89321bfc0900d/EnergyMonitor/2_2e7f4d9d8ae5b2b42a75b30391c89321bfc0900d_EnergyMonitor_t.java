 package energymonitor;
 
 import java.awt.AWTException;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 import java.util.Timer;
 
 public class EnergyMonitor {
     static TrayIcon trayIcon = new TrayIcon(ImageDisplay.getTrayImage());
     static class ShowMessageListener implements ActionListener {
 
         TrayIcon trayIcon;
         String title;
         String message;
         TrayIcon.MessageType messageType;
 
         ShowMessageListener(
                 TrayIcon trayIcon,
                 String title,
                 String message,
                 TrayIcon.MessageType messageType) {
             this.trayIcon = trayIcon;
             this.title = title;
             this.message = message;
             this.messageType = messageType;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             trayIcon.displayMessage(title, message, messageType);
         }
     }
 
     public static void setNimbusUI(){
         try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (Exception ex) {
         }
     }
     
     public static void main(String[] args) {
  
         setNimbusUI();
          
         Runnable runner;
         runner = new Runnable() {
             @Override
             public void run() {
                 if (SystemTray.isSupported()) {
                     SystemTray tray = SystemTray.getSystemTray();
                     
                     PopupMenu popup = new PopupMenu();
 
                     //TrayIcon trayIcon = new TrayIcon(ImageDisplay.getTrayImage(), "Energy Monitor", popup);
                    
                     trayIcon.setToolTip("Energy Monitor");
                     trayIcon.setPopupMenu(popup);
                     //menu1
                     MenuItem item = new MenuItem("Infomation");
                     item.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             //tray.remove(trayIcon);
 
                             final JFrame jf = new ClientInfo();
                             jf.setTitle("Energy Monitor");
                             jf.setIconImage(ImageDisplay.getTrayImage());
                             // Get the size of the screen
                             Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                             // Determine the new location of the window
                             int w = jf.getSize().width;
                             int h = jf.getSize().height;
                             int x = (dim.width - w) / 2;
                             int y = (dim.height - h) / 2;
                             // Move the window
                             jf.setLocation(x, y);
                             jf.setResizable(false); //Disable the Resize Button
                             jf.setVisible(true);
 
                         }
                     });
                     popup.add(item);
                     //menu2
                     item = new MenuItem("About Energy Monitor");
                     item.addActionListener(new ShowMessageListener(trayIcon,
                             "Name Title", "name", TrayIcon.MessageType.ERROR));
                     popup.add(item);
                     //menu3
 //                    item = new MenuItem("Close");
 //                    item.addActionListener(new ActionListener() {
 //                        public void actionPerformed(ActionEvent e) {
 //                            tray.remove(trayIcon);
 //                        }
 //                    });
 //                    popup.add(item);
                     
                     Timer timer = new Timer("sendData");
                     TaskProcess t = new TaskProcess();
                     timer.schedule(t, 0, 1000);
                     
                     try {
                         tray.add(trayIcon);
                     } catch (AWTException e) {
                         System.err.println("Can't add to tray");
 
                     }
                     
 
 
                     
                 } else {
                     System.err.println("Tray unavailable");
                 }
 
             }
         };
         EventQueue.invokeLater(runner);
         
 
         
     }
 }
