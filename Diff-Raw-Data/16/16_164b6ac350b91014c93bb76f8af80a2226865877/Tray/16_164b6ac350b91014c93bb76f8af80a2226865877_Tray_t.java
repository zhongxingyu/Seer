 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ircclient.gui;
 
 import ircclient.gui.windows.IRCWindow;
 
 import java.awt.AWTException;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 /**
  *
  * @author fc
  */
 public class Tray {
 
     private IRCWindow window;
     private SystemTray tray;
     private TrayIcon icon;
 
     public Tray(IRCWindow window) {
         this.window = window;
 
         if (SystemTray.isSupported()) {
             tray = SystemTray.getSystemTray();
 
         //   Image image = Toolkit.getDefaultToolkit().createImage("./ChopNix/res/icon.gif");
 
            Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/home/home.png"));
            
             MouseListener mouseListener = new MouseListener() {
 
                 @Override
                 public void mouseClicked(MouseEvent e) {
                 }
 
                 @Override
                 public void mouseEntered(MouseEvent e) {
                     getWindow().setVisible(true);
                     getWindow().setExtendedState(JFrame.NORMAL);
                 }
 
                 @Override
                 public void mouseExited(MouseEvent e) {
                 }
 
                 @Override
                 public void mousePressed(MouseEvent e) {
                 }
 
                 @Override
                 public void mouseReleased(MouseEvent e) {
                 }
             };
 
             PopupMenu popup = new PopupMenu();
             MenuItem restoreItem = new MenuItem("Restore");
             MenuItem awayItem = new MenuItem("Away");
             MenuItem exitItem = new MenuItem("Exit");
 
             restoreItem.addActionListener(restoreListener);
             awayItem.addActionListener(awayListener);
             exitItem.addActionListener(exitListener);
 
             popup.add(restoreItem);
             popup.add(awayItem);
             popup.addSeparator();
             popup.add(exitItem);
 
 
             ActionListener actionListener = new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     getWindow().setVisible(true);
                     getWindow().setExtendedState(JFrame.NORMAL);
                 }
             };
 
             icon = new TrayIcon(image, getWindow().getTitle(), popup);
             icon.setImageAutoSize(true);
             icon.addActionListener(actionListener);
             icon.addMouseListener(mouseListener);
 
 
             try {
                 tray.add(icon);
             } catch (AWTException e) {
                 System.err.println("TrayIcon could not be added.");
             }
             icon.setImage(image);
 
         } else {
             System.out.println("no tray");
 
         }
     }
     ActionListener restoreListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             getWindow().setVisible(true);
             getWindow().setExtendedState(JFrame.NORMAL);
 
         }
     };
     ActionListener awayListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             try {
                 for (ServerPanel sp : getWindow().getServerList()) {
                     sp.away("away");
                 }
             } catch (IOException ioe) {
             }
         }
     };
     ActionListener exitListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             getWindow().exit();
         }
     };
 
     public boolean isTray() {
         return (window != null) && (tray != null) ? true : false;
     }
 
     public void message(String title, String message) {
         icon.displayMessage(title, message, TrayIcon.MessageType.INFO);
     }
 
     public IRCWindow getWindow() {
         return window;
     }
 
     public TrayIcon getTrayIcon() {
         return icon;
     }
 }
