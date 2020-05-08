 package com.mofirouz.notifier;
 
 import ch.swingfx.twinkle.NotificationBuilder;
 import ch.swingfx.twinkle.manager.NotificationManagers;
 import ch.swingfx.twinkle.style.AbstractNotificationStyle;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 
 /**
  *
  * @author Mo Firouz
  * @since 2/10/11
  */
 public class SystemNotification {
 
     public final static String VERSION = "0.0.1";
     private final int MAX_MESS_LENGTH = 80;
     private final static String GROWL_NAME = "JavaPushMail";
     private static String NOTIFY_OSD_PATH = "/usr/bin/notify-send";
     private Growl growl;
     private String title, iconPath;
     private String[] message;
     private boolean showFallback;
     private NotificationConfiguration notificationConfiguration;
     private ImageIcon icon = null;
     private TrayIcon trayIcon = null;
     private PopupMenu popup;
     private SystemTray tray;
 
     public SystemNotification() {
         notificationConfiguration = NotificationConfiguration.getDefaultConfiguration();
         initTrayPopup();
 
     }
 
     public SystemNotification(NotificationConfiguration config) {
         notificationConfiguration = config;
         initTrayPopup();
     }
 
     public void setNotifyOSDPath(String path) {
         NOTIFY_OSD_PATH = path;
     }
 
     public void showNotification(boolean showFallback, String title, String[] message) {
         if (title == null || title.isEmpty() || message == null || message.length == 0)
             return;
 
         this.title = title;
         this.message = message;
         this.showFallback = showFallback;
         showTrayIcon();
         showNotification();
     }
 
     public void setIcon(File iconFile) {
         if (iconFile != null) {
             try {
                 iconPath = iconFile.getAbsolutePath().replaceFirst("file:/", "/");
                 icon = new ImageIcon(iconPath);
             } catch (Exception e) {
                 icon = null;
             }
         } else {
             icon = null;
             iconPath = null;
         }
     }
 
     public void setPopupMenu(PopupMenu popup) {
         this.popup = popup;
         initTrayPopup();
     }
 
     private void showTrayIcon() {
         if (icon == null && !SystemTray.isSupported())
             return;
 
         if (tray == null)
             tray = SystemTray.getSystemTray();
 
         if (trayIcon == null) {
             trayIcon = new java.awt.TrayIcon(icon.getImage());
             trayIcon.setToolTip("You have new mail!");
             trayIcon.displayMessage(title, createSingleLineString(message), TrayIcon.MessageType.INFO);
             trayIcon.setImageAutoSize(true);
             trayIcon.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent ae) {
                     tray.remove(trayIcon);
                 }
             });
             trayIcon.addMouseListener(new MouseAdapter() {
 
                 @Override
                 public void mousePressed(MouseEvent me) {
                     super.mousePressed(me);
                     if (me.getButton() == 1) {
                         showNotification();
                     }
                 }
             });
             if (popup == null)
                 initTrayPopup();
             trayIcon.setPopupMenu(popup);
         }
         try {
             if (tray.getTrayIcons().length == 0)
                 tray.add(trayIcon);
         } catch (Exception e) {
         }
     }
 
     private void showNotification() {
         try {
 
             if (showFallback) {
                 fallbackNotification();
             } else {
                 String os = System.getProperty("os.name");
                 if (os.contains("Windows"))
                     fallbackNotification();
                 else if (os.contains("Mac"))
                     notifyGrowl();
                 else if (os.contains("Linux"))
                     notifyOSD();
             }
         } catch (Exception e) {
             fallbackNotification();
         }
     }
 
     private void notifyGrowl() {
         if (growl == null) {
             growl = new Growl("JavaPushMail",
                     new String[]{
                         GROWL_NAME
                     },
                     new String[]{
                         GROWL_NAME
                     });
             growl.init();
             growl.registerApplication();
         }
 
         String content = createMultiLineString(message);
         if (icon == null)
             growl.notify(GROWL_NAME, title, content);
         else
             growl.notifyWithIcon(GROWL_NAME, title, content, "file://" + iconPath);
 
 
     }
 
     private void notifyOSD() throws IOException, InterruptedException {
 
         Process p = Runtime.getRuntime().exec(buildArgs(), null, null);
         p.waitFor();
         if (p.exitValue() != 0)
             fallbackNotification();
     }
 
     private String[] buildArgs() {
         ArrayList<String> args = new ArrayList<String>();
 
         args.add(NOTIFY_OSD_PATH);
         args.add("" + title + "");
         String line = "";
         for (int i = 0; i < message.length; i++) {
             line += message[i];
             if (i != (message.length - 1))
                 line += " \n";
         }
         args.add("" + line + "");
         args.add("-i");
         args.add(iconPath);
 
         String[] ret = new String[args.size()];
         ret = args.toArray(ret);
         return ret;
     }
 
     private void fallbackNotification() {
        String content = createMultiLineString(message);
         if (content.length() > MAX_MESS_LENGTH)
             content = (content.substring(0, MAX_MESS_LENGTH)) + "...";
 
         NotificationBuilder builder = new NotificationBuilder();
         AbstractNotificationStyle style = notificationConfiguration.getStyle();
         style.withWidth(250);
         style.withWindowCornerRadius(20);
         builder = builder.withFadeInAnimation(true).withFadeOutAnimation(true).withTitle(title).withMessage(content).withStyle(style).withPosition(notificationConfiguration.getPosition()).withNotificationManager(NotificationManagers.SEQUENTIAL);
 
         if (icon != null) {
             builder = builder.withIcon(new ImageIcon(icon.getImage(), icon.getDescription()));
         }
 
         builder.showNotification();
     }
 
     private void initTrayPopup() {
         MenuItem hide = new MenuItem("Hide Icon");
         hide.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 tray.remove(trayIcon);
             }
         });
         if (popup == null)
             popup = new PopupMenu();
         popup.add(hide);
     }
 
     public static String createMultiLineString(String[] mess) {
         String content = mess[0];
         for (int i = 1; i < mess.length; i++)
             content += " \n" + mess[i];
 
         return content;
     }
 
     public static String createSingleLineString(String[] mess) {
         String content = mess[0];
         for (int i = 1; i < mess.length; i++)
             content += " " + mess[i];
 
         return content;
     }
 }
