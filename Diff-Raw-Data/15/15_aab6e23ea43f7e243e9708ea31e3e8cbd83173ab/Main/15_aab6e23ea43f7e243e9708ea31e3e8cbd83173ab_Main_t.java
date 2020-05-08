 /**
  * Kuebiko - Main.java
  * Copyright 2011 Dave Huffman (daveh303 at yahoo dot com).
  * TODO license info.
  */
 
 package dmh.kuebiko;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.prefs.Preferences;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.google.common.collect.Maps;
 
 import dmh.kuebiko.controller.NoteManager;
 import dmh.kuebiko.model.DaoParameter;
 import dmh.kuebiko.model.NoteDaoFactory;
 import dmh.kuebiko.model.NoteDaoFactory.OfficialDao;
 import dmh.kuebiko.view.NoteFrame;
 
 /**
  * Main class for launching Kuebiko as a Swing application.
  *
  * @author davehuffman
  */
 public class Main {
     private static final Logger log = Logger.getLogger(Main.class);
     
     public static enum Setting {
        DAO_CLASS("IN_MEMORY"), 
        DATA_LOCATION(null), 
        FONT_NAME("Monospaced"),
        FONT_SIZE("12");
        
        final String defaultValue;
        
        private Setting(String defaultValue) {
            this.defaultValue = defaultValue;
        }
     }
     
     private static final EnumMap<Setting, String> SETTINGS = Maps.newEnumMap(Setting.class);
     private static final String SETTINGS_FILE_NAME = "kuebiko.properties";
     
     /**
      * Exception handler for Kuebiko.
      */
     private static class KuebikoUncaughtExceptionHandler 
     implements UncaughtExceptionHandler {
         @Override
         public void uncaughtException(Thread thread, final Throwable exception) {
             if (SwingUtilities.isEventDispatchThread()) {
                 handleException(exception);
             } else {
                 SwingUtilities.invokeLater(
                     new Runnable() {
                         @Override
                         public void run() {
                             handleException(exception);
                         }
                     });
             }
         }
 
         private void handleException(Throwable exception) {
             // TODO log or submit error somewhere where it can be reported.
             log.error("Uncaught exception.", exception);
 
             // TODO replace null with top-most frame or dialog.
             // TODO use special error dialog with reporting mechanism.
             JOptionPane.showMessageDialog(null, "Oops! Something bad happened.");
         }
     }
     
     public static void main(final String[] args) {
         PropertyConfigurator.configure(
                 Main.class.getClassLoader().getResource("log4j.properties"));
         
         Thread.setDefaultUncaughtExceptionHandler(new KuebikoUncaughtExceptionHandler());
         
         try {
             // Special setup to support MacOS X menus.
             System.setProperty("apple.laf.useScreenMenuBar", "true");
             System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Kuebiko");
             
             // Use the default look and feel of the host system.
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         loadSettings();
         
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 /** XXX scaffolding. */
 //                getPrefs().put(Preference.CURRENT_STACK_LOCATION.toString(), "");
 //                getPrefs().put(Preference.CURRENT_STACK_DAO.toString(), InMemoryNoteDao.class.getName());
                 
 //                Map<String, String> daoParams = Maps.newHashMap();
 //                for (DaoParameter daoParam: DaoParameter.values()) {
 //                    
 //                    String key = "kueb." + daoParam.toString();
 //                    String property = System.getProperty(key);
 //                    
 //                    System.out.printf("[CONFIG] Parameter [%s] = [%s].%n", key, property);
 //                    
 //                    daoParams.put(daoParam.toString(), property); 
 //                }
                 
                 Map<String, String> daoParams = Maps.newHashMap();
                 daoParams.put(DaoParameter.CLASS_NAME.toString(), getSetting(Setting.DAO_CLASS)); 
                 daoParams.put(DaoParameter.DIRECTORY.toString(), getSetting(Setting.DATA_LOCATION)); 
                 
                 NoteManager noteMngr;
                 try {
                     noteMngr = new NoteManager(NoteDaoFactory.get(daoParams));
                 } catch (Exception e) {
                     System.err.printf("Invalid parameters [%s].%n", daoParams);
                     System.err.println("Valid DAOs:");
                     for (OfficialDao officialDao: OfficialDao.values()) {
                         System.err.println(officialDao);
                     }
                     throw new RuntimeException(e);
                 }
                 
                 NoteFrame noteFrame = new NoteFrame(noteMngr);
                 noteFrame.setVisible(true);
             }
         });
     }
     
     public static final void loadSettings() {
         // Load the properties file.
         final Properties rawSettings = new Properties();
         try {
             rawSettings.load(new FileInputStream(SETTINGS_FILE_NAME));
         } catch (FileNotFoundException e) {
             log.warn(String.format("Settings file [%s] does not exist.", SETTINGS_FILE_NAME));
         } catch (IOException e) {
             log.error(String.format("Error reading settings file [%s].", SETTINGS_FILE_NAME), e);
             throw new RuntimeException(e);
         }
         
         SETTINGS.clear();
         for (Map.Entry<Object, Object> rawSetting : rawSettings.entrySet()) {
             Setting key;
             try {
                 key = Setting.valueOf(rawSetting.getKey().toString());
             } catch (IllegalArgumentException e) {
                 log.warn(String.format("Setting [%s] is unknown.", rawSetting.getKey()));
                 continue;
             }
             
             SETTINGS.put(key, rawSetting.getValue().toString());
         }
     }
     
     public static String getSetting(Setting setting) {
        return SETTINGS.containsKey(setting) ? 
                SETTINGS.get(setting) : setting.defaultValue;
     }
     
     enum Preference {
         CURRENT_STACK_LOCATION, CURRENT_STACK_DAO
     }
     
     public static Preferences getPrefs() {
         return Preferences.userNodeForPackage(Main.class);
     }
     
     private static int logCount = 0;
     @Deprecated
     public static void log(String format, Object... data) {
         System.err.printf("[%3d] %s%n", logCount++, String.format(format, data));
     }
 }
