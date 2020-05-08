 /*
  * jCleaningSchedule - program for printing house cleaning schedules
  * Copyright (C) 2012  Martin Mareš <mmrmartin[at]gmail[dot]com>
  *
  * This file is part of jCleaningSchedule.
  *
  * jCleaningSchedule is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * jCleaningSchedule is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with jCleaningSchedule.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.martinmares.jcleaningschedule.core;
 
 import cz.martinmares.jcleaningschedule.gui.LicenseAgreementFrame;
 import cz.martinmares.jcleaningschedule.gui.MainFrame;
 import cz.martinmares.jcleaningschedule.gui.SettingsChangeListener;
 import cz.martinmares.jcleaningschedule.gui.SettingsFrame;
 import cz.martinmares.jcleaningschedule.gui.UpdateFrame;
 import java.awt.Color;
 import java.awt.Font;
 import java.io.IOException;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 /**
  * The Main class of jCleaningSchedule.
  * This method handle basic activities after application startup.
  * @author Martin Mareš
  */
 public class Main {
 
     public static final String APP_VERSION = "3.0.0";
     public static final String APP_HOMEPAGE = "https://github.com/mmrmartin/jCleaningSchedule"; 
     //Web data
     public static final String UPDATE_VERSION_URL = "http://goo.gl/GY95t"; 
     public static final String UPDATE_CHANGELOG_URL = "https://raw.github.com/mmrmartin/jCleaningSchedule/master/CHANGELOG"; 
     public static final String UPDATE_WWW = "https://github.com/mmrmartin/jCleaningSchedule/downloads"; 
     public static final String FIRST_RUN_URL = "http://goo.gl/uidjq";
     //Default settings
     public static final Color   DEFAULT_SC_BG_COLOR = Color.YELLOW;
     public static final Color   DEFAULT_SC_TXT_COLOR = Color.BLACK;
     public static final String  DEFAULT_FONT_NAME = Font.SANS_SERIF;
     public static final boolean DEFAULT_AUTO_UPDATES_ENABLED = true;
     private static final ResourceBundle str = ResourceBundle.getBundle("cz/martinmares/jcleaningschedule/resources/lang");
     
    /**
     * The main method of jCleanningSchedule whitch runs the application.
     * @param args the command line arguments
     */
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ex) {
             System.err.printf("I can't change a default look and feel, " +
                     "but don't wory about it");
         }
         JCleaningScheduleData data = null;
         try {
             data = new JCleaningScheduleData();
             showMainFrame(data);
         } catch (DataNotFoundException ex) {
             showLicenseAgreementFrame(data);
         } catch (IOException ex) {
             //TODO - adds info and showLicenseAgreementFrame :-(
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Shows the license agreement frame.
      * This frame is shown if user runs application for the first time.
      * @param data application setting
      * @see cz.martinmares.jcleaningschedule.gui.LicenseAgreementFrame;
      */
     public static void showLicenseAgreementFrame(final JCleaningScheduleData data) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 final LicenseAgreementFrame dialog = new LicenseAgreementFrame(data);
                 dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                     @Override
                     public void windowClosing(java.awt.event.WindowEvent e) {
                         JOptionPane.showMessageDialog(dialog,
                                 str.getString("MAIN_LICENSE_DISAGREEMENT"),
                                 str.getString("DIALOG_INFORMATION"), 
                                 JOptionPane.INFORMATION_MESSAGE);
                         System.exit(2);
                     }
                 });
                 dialog.setVisible(true);
             }
         });
         
     }
     
     /**
      * Check for updates.
      * If if founds updates, it'll show <code>updateFrame</code>.
      * @see cz.martinmares.jcleaningschedule.gui.UpdateFrame
      */    
     public static void checkForUpdates() {
         if(!WebServices.getLastVersion().equals(Main.APP_VERSION)) {
             UpdateFrame updateFrame = new UpdateFrame(null, true);
             updateFrame.setVisible(true);
         }
     }
     
     /**
      * Shows the License Settings Frame.
      * This frame is shown if user runs application for the first time after
      * license agreement window and also check for application updates.
      * @param data application setting
      * @param autoUpdateEnabled True - check for updates before opening <code>
      * SettingsFrame</code>.
      * @see cz.martinmares.jcleaningschedule.gui.SettingsFrame
      */    
    public static void showSettingsFrame(JCleaningScheduleData data, boolean autoUpdateEnabled) {
         if(autoUpdateEnabled) {
             checkForUpdates();
         }
         final SettingsFrame dialog = new SettingsFrame(new javax.swing.JFrame(), true, data);
         dialog.setAutoUpdates(autoUpdateEnabled);
         dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 
             @Override
             public void windowClosing(java.awt.event.WindowEvent e) {
                 JOptionPane.showMessageDialog(dialog,
                         str.getString("MAIN_NO_SETTINGS"),
                         str.getString("DIALOG_ERROR"), JOptionPane.ERROR_MESSAGE);
                 System.exit(2);
             }
         });
         dialog.setSettingsChangeListener(new SettingsChangeListener() {
             @Override
             public void onSettingsChange() {
                showMainFrame(dialog.getJCleaningScheduleData());
             }
         });
         dialog.setVisible(true);
     }
      
     
     /**
      * Shows the MainFrame.
      * This frame is shown after aplication startup or after  
      * <code>SettingsFrame</code>.
      * @param data application setting
      * @see cz.martinmares.jcleaningschedule.gui.MainFrame
      */     
     public static void showMainFrame(JCleaningScheduleData data) {
         MainFrame dialog = new MainFrame(data);
         dialog.setVisible(true);
         if(data.isAutoUpdatesEnabled()) {
             checkForUpdates();
         }
     }
 }
