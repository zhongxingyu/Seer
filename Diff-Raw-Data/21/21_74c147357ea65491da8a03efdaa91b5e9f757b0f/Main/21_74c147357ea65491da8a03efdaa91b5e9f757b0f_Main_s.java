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
                 
                String daoClassName = System.getProperty(
                        PARAM_DAO_CLASS, OfficialDao.IN_MEMORY.toString());
                 Map<String, String> daoParams = Maps.newHashMap();
                 for (DaoParameter daoParam: DaoParameter.values()) {
                    String key = daoParam.toString();
                    daoParams.put(key, System.getProperty("kueb." + key)); 
                 }
                 
                 NoteManager noteMngr;
                 try {
                    noteMngr = new NoteManager(
                            NoteDaoFactory.get(daoClassName, daoParams));
                 } catch (Exception e) {
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
