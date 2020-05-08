 package nl.vanreijmersdal.jtt;
 
 import java.awt.AWTException;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 import javax.swing.JOptionPane;
 
 /**
  * Java Time Tracker
  */
 public class App {  
     public static void main( String[] args ) throws InterruptedException {
               
         TimeTracker timeTracker = new TimeTracker();
         timeTracker.startTray();
         
     }
 }
 
 class TimeTracker {
     
     boolean taskRunning = false;
     boolean showWarning = false;
     int showWarningTime = 4000;
     long idleSince = new Date().getTime();
     
     Image idleIcon, startIcon, warningIcon;
     TrayIcon trayIcon = null;
     Notification notify = new Notification();
 
     private void startTask() {
         if(taskRunning == false) {
             String task = JOptionPane.showInputDialog(null, "Task description:", "Start new task", JOptionPane.PLAIN_MESSAGE);
             if( task != null) {
                 notify.showNotification("Started task: " + task, true);
                 trayIcon.setImage(startIcon);
                 showWarning = false;
                 taskRunning = true;
             }
         }
     }
     
     public void startTray() throws InterruptedException {
 
         if (SystemTray.isSupported()) {
             
             final SystemTray tray = SystemTray.getSystemTray();
 
             Dimension trayIconSize = tray.getTrayIconSize();
             idleIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon_idle.png"));
             idleIcon = idleIcon.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
             startIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon_working.png"));
             startIcon = startIcon.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
             warningIcon = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon_warning.png"));
             warningIcon = warningIcon.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
 
             trayIcon = new TrayIcon(idleIcon);
             trayIcon.addActionListener(new ActionListener() {
                @Override
                 public void actionPerformed(ActionEvent e) {
                     startTask();
                 }
             });        
             
             PopupMenu popup = new PopupMenu();
             MenuItem start = new MenuItem("Start");
             start.addActionListener(new ActionListener() {
                @Override
                 public void actionPerformed(ActionEvent e) {
                     startTask();
                 }
             });
             popup.add(start);
            
             MenuItem stop = new MenuItem("Stop");
             popup.add(stop);
             stop.addActionListener(new ActionListener() {
                @Override
                 public void actionPerformed(ActionEvent e) {
                     taskRunning = false;
                     idleSince = new Date().getTime();
                     trayIcon.setImage(idleIcon);
                 }
             });
             
             MenuItem exit = new MenuItem("Exit");
             exit.addActionListener(new ActionListener() {
                @Override
                 public void actionPerformed(ActionEvent e) {
                     // TODO Save current state to disk
                     tray.remove(trayIcon);
                     System.exit(0);
                 }
             });
             popup.add(exit);
 
             trayIcon.setToolTip("Time Tracker");
             trayIcon.setPopupMenu(popup);
             
             try {
                 tray.add(trayIcon);
             } catch (AWTException e) {
                 System.out.println("Could not start tray application: " + e);
             }
             
             while(true) {
                 long currentTime = new Date().getTime();
                 if(currentTime - idleSince > showWarningTime && !taskRunning && !showWarning) {
                   notify.showNotification("Not working on a task?", false);
                   showWarning = true;
                   idleSince = new Date().getTime();
                 }
                Thread.sleep(100); // Prevents 100% CPU usage for while(true). TODO replace with wait()/notify() system?               
             }            
        } else {
            System.out.println("Could not start tray application");
         }
     }
 }
