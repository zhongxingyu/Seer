 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.TimerTask;
 import java.util.Timer;
 
 import javax.swing.*;
 
 public class TrayMenu implements ActionListener{
                 
         private TrayIcon trayIcon;
         private Image trayImage;
         private SystemTray tray;
         private PopupMenu popMenu;
         private MenuItem listGist;
         private MenuItem searchGist;
         
         private boolean visible;
         private boolean messageVisible;
         private String lastMessage;
 
         public TrayMenu(){
         	
                 visible = false;
                 messageVisible = false;
                 
                 if(SystemTray.isSupported()){
                        trayImage = Toolkit.getDefaultToolkit().getImage("icons\\document-new.png");
                         trayIcon = new TrayIcon(trayImage, "GistPal");
                         tray = SystemTray.getSystemTray();
                         
                         popMenu = new PopupMenu();
                         listGist = new MenuItem("View all gists");
                         searchGist = new MenuItem("Search gist");
                         listGist.addActionListener(this);
                         searchGist.addActionListener(this);
                         popMenu.add(listGist);
                         popMenu.add(searchGist);
                         
                         trayIcon.setPopupMenu(popMenu);
                         trayIcon.setImageAutoSize(true);
                         trayIcon.addActionListener(this);
 
                         try {
                                 tray.add(trayIcon);
                                 visible = true;
                         } 
                         catch (AWTException e) {
                                 System.err.println("Could not add trayicon");
                                 JOptionPane.showMessageDialog(null, "Could not add trayicon");
                         }
                 }
                 else{
                         System.err.println("Systemtray was not found");
                         JOptionPane.showMessageDialog(null,"Systemtray was not found");
                 }
         }
         public void showTray(boolean status){
                 if(status){
                         try {
                                 tray.add(trayIcon);
                                 visible = true;
                         } 
                         catch (AWTException e) {
                                 visible = false;
                                 System.err.println("Could not add trayicon");
                                 JOptionPane.showMessageDialog(null, "Could not add trayicon");
                         }
                 }
                 else{
                         tray.remove(trayIcon);
                         visible = false;
                 }
         }
         public void viewMessage(String message){
                 if(visible){
                         lastMessage = message;
                         trayIcon.displayMessage("GistPal", message,TrayIcon.MessageType.INFO);
                         messageVisible = true;
                         Timer t = new Timer();
                         t.schedule(new DelayTime(),6000);
                 }
         }
         @Override
         public void actionPerformed(ActionEvent e) {
                 if(e.getSource() == listGist){
                 	// TODO      
                 }
                 else if(e.getSource() == searchGist){
                 	// TODO
                 }
                 else if(e.getSource() == trayIcon){
                         if(!messageVisible){
                         	// TODO
                         }
                         else{
                         	// TODO
                         }
                 }       
                 else{
                         System.out.println("Source:" + e.getSource());
                 }
         }
         /**
          * Private class that is used in the timer
          * The only thing it does that I change the boolean messageVisible back to default
          * messageVisible = false
          * after the timer is done which is default 6 seconds
          */
         private class DelayTime extends TimerTask{
                 public void run(){
                         messageVisible = false;
                 }
         }
         public static void main(String[] args){
         	new TrayMenu();
         }
 }
