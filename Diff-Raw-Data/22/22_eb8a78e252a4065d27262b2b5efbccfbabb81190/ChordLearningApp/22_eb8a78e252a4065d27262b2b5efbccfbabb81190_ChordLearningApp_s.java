 package ro.btanase.chordlearning;
 
 import java.awt.EventQueue;
 import java.io.File;
 
 import javax.management.RuntimeErrorException;
 import javax.swing.UIManager;
 
 import org.apache.log4j.Logger;
 
 import ro.btanase.chordlearning.exceptions.MyUncaughtExceptionHandler;
 import ro.btanase.chordlearning.frames.ApplicationMainWindow;
 import ro.btanase.chordlearning.frames.UserFolderFrame;
 import ro.btanase.chordlearning.services.UserData;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 
 public class ChordLearningApp {
   private static Logger log = Logger.getLogger(ChordLearningApp.class);
   private static Injector injector;
 //  public static final double CONFIG_FILE_CHORDS_VERSION = 1.0;
 //  public static final double CONFIG_FILE_LESSON_VERSION = 1.1;
  public static final String VERSION = "1.1.1";
   public static final String CONFIG_FOLDER = "config";
   
   /**
    * run with parameter "-c" to reconfigure user folder
    * @param args
    */
   public static void main(String[] args) {
 
     MyUncaughtExceptionHandler.registerExceptionHandler();
     
     if (args.length == 1 && args[0].equals("-c")){
       File file = new File(System.getProperty("user.home") + File.separator + UserData.DATA_DIR + File.separator + "application.properties");
       if (file.exists()){
         if (!file.delete()){
           throw new RuntimeException("Unable to delete configuration file: " + file.getAbsolutePath());
         }
       }
     }
     
     EventQueue.invokeLater(new Runnable() {
       
       @Override
       public void run() {
         try {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Throwable e) {
           log.error("unable to set look and feel", e);
           throw new RuntimeException(e);
         }
 
         injector = Guice.createInjector(new ChordLearningModule());
         initUserData();
         ApplicationMainWindow ldf = injector.getInstance(ApplicationMainWindow.class);
         ldf.setLocationRelativeTo(null);
         ldf.setVisible(true);
       }
     });
   }
   
   private static void initUserData(){
     UserData userData = injector.getInstance(UserData.class);
     if (!userData.isUserDirectoryDefined()){
       UserFolderFrame uff = injector.getInstance(UserFolderFrame.class);
       uff.setLocationRelativeTo(null);
       uff.setVisible(true);
     }
     
     userData.upgradeIfNecessary();
   }
   
   public static Injector getInjector() {
     return injector;
   }
   
   
 }
