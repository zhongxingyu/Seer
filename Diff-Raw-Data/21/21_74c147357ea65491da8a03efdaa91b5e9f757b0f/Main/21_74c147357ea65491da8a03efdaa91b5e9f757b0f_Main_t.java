 /**
  * Kuebiko - Main.java
  * Copyright 2011 Dave Huffman (daveh303 at yahoo dot com).
  * TODO license info.
  */
 
 package dmh.kuebiko;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
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
     private static final String PARAM_DAO_CLASS = "kueb.dao"; 
     
     /** Temporary constant for disabling code that is not a part of the 0.1 release. */
     public static final boolean POST_0_1_RELEASE = false;
     
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
             exception.printStackTrace();
 
             // TODO replace null with top-most frame or dialog.
             // TODO use special error dialog with reporting mechanism.
             JOptionPane.showMessageDialog(null, "Oops! Something bad happened.");
         }
     }
     
     public static void main(final String[] args) {
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
         
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 /** XXX scaffolding. */
 //                getPrefs().put(Preference.CURRENT_STACK_LOCATION.toString(), "");
 //                getPrefs().put(Preference.CURRENT_STACK_DAO.toString(), InMemoryNoteDao.class.getName());
                 
                 Map<String, String> daoParams = Maps.newHashMap();
                 for (DaoParameter daoParam: DaoParameter.values()) {
                    String key = "kueb." + daoParam.toString();
                    String property = System.getProperty(key);
                    
                    System.out.printf("[CONFIG] Parameter [%s] = [%s].%n", key, property);
                    
                    daoParams.put(daoParam.toString(), property); 
                 }
                 
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
     
     enum Preference {
         CURRENT_STACK_LOCATION, CURRENT_STACK_DAO
     }
     
     public static Preferences getPrefs() {
         return Preferences.userNodeForPackage(Main.class);
     }
     
     private static int logCount = 0;
     public static void log(String format, Object... data) {
         System.err.printf("[%3d] %s%n", logCount++, String.format(format, data));
     }
 }
