 package findMyIP;
 
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 
 import java.awt.AWTException;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.IOException;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 public class FindMyIP {
   
     private static final int QUICK_INTERVAL = 5 * 1000;      // 5 seconds.
     private static final int SLOW_INTERVAL  = 5 * 60 * 1000; // 5 minutes.
 
     private static final String name = "FindMyIP";
 
     private String currentIP;
     private String currentLocalIP;
     private TrayIcon trayIcon;
     private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
   
     public FindMyIP() {
         currentIP = IPUtils.getIPAddress();
         currentLocalIP = IPUtils.getLocalIPAddress();
     }
 
     private void displayMessage(String message, String localMessage) {
        trayIcon.displayMessage(name, message + currentIP + "\n" + localMessage + currentLocalIP, TrayIcon.MessageType.INFO);
     }
 
     private void displayUpdateMessage(boolean onlyWhenChanged) {
 
         String message = onlyWhenChanged ? null : "IP not changed: ";
        String localMessage = onlyWhenChanged ? null : "Local IP not changed: ";
 
         String newIP = IPUtils.getIPAddress();
 
         if (!currentIP.equals(newIP)) {
             currentIP = newIP;
             message = "New public IP: ";
         }
 
         newIP = IPUtils.getLocalIPAddress();
 
         if (!currentLocalIP.equals(newIP)) {
             currentLocalIP = newIP;
             localMessage = "New local IP: ";
         }
 
         if (timer != null) {
 
             if (currentIP.equals("<unknown>") || currentLocalIP.equals("<unknown>")) {
                 updateTimer(QUICK_INTERVAL);
             }
             else {
                 updateTimer(SLOW_INTERVAL);
             }
         }
 
         if (message != null && localMessage != null) {
             displayMessage(message, localMessage);
         }
     }
 
     private void copyIPToClipboard() {
 
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                 new StringSelection(currentIP), null);
 
     }
 
     private void copyLocalIPToClipboard() {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                 new StringSelection(currentLocalIP), null);
     }
 
     private void createAndShowGUI() {
 
         if (!SystemTray.isSupported()) {
             System.out.println("SystemTray is not supported");
             System.exit(1);
         }
 
         PopupMenu popupMenu= new PopupMenu();
 
         MenuItem copyItem = new MenuItem("Copy public IP to clipboard");
         MenuItem copyLocalItem = new MenuItem("Copy local IP to clipboard");
         MenuItem updateItem = new MenuItem("Update now");
         MenuItem exitItem = new MenuItem("Exit");
 
         popupMenu.add(copyItem);
         popupMenu.add(copyLocalItem);
         popupMenu.addSeparator();
         popupMenu.add(updateItem);
         popupMenu.add(exitItem);
 
         try {
             trayIcon = new TrayIcon(ImageIO.read(getClass().getResource(("/icon.png"))), name, popupMenu);
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         try {
             SystemTray.getSystemTray().add(trayIcon);
         }
         catch (AWTException e) {
             e.printStackTrace();
         }
 
         trayIcon.setImageAutoSize(true);
         trayIcon.setToolTip("Current IP: " + currentIP + "\n" + "Current Local IP: " + currentLocalIP);
         displayMessage("Current IP: ", "Current Local IP: ");
 
         copyItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 copyIPToClipboard();
             }
         });
 
         copyLocalItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 copyLocalIPToClipboard();
             }
         });
 
         updateItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 displayUpdateMessage(false);
             }
         });
 
         exitItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 SystemTray.getSystemTray().remove(trayIcon);
                 System.exit(0);
             }
         });
     }
 
     private void updateTimer(int intervalTime) {
 
         timer.shutdown();
         timer = Executors.newSingleThreadScheduledExecutor();
         timer.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                 displayUpdateMessage(true);
             }
         }, 0, intervalTime, TimeUnit.SECONDS);
 
     }
 
     public static void main(String[] args) {
 
         final FindMyIP ipFinder = new FindMyIP();
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 ipFinder.createAndShowGUI();
                 ipFinder.updateTimer(SLOW_INTERVAL);
             }
         });
 
     }
 
 }
