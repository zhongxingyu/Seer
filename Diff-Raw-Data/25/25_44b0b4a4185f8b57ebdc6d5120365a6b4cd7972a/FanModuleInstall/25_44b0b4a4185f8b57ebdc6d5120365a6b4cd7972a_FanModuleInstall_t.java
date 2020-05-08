 /*
  * Thibaut Colar Dec 18, 2009
  */
 package net.colar.netbeans.fan;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
import net.colar.netbeans.fan.actions.FanAction;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.indexer.FanIndexerFactory;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import net.jot.logger.JOTLogger;
 import net.jot.persistance.JOTPersistanceManager;
 import net.jot.prefs.JOTPreferences;
 import org.openide.modules.ModuleInstall;
 
 /**
  * Module startup/shutdown hooks.
  * @author thibautc
  */
 public class FanModuleInstall extends ModuleInstall
 {
     // IF a breaking change is made to the prefs files compare to a previous version, bump up the number
 
     public static final String PROP_PREF_FILE_VERSION = "nb.fantom.prefs.version";
     public static final int PREF_VERSION = 2;
 
     /**
      * Startup
      */
     @Override
     public void restored()
     {
         System.out.println("Starting up Fantom plugin.");
         // Initialize special logging as needed
         FanNBLogging.setupLogging();
         File fantomHome = FanUtilities.getFanUserHome();
         File logHome = new File(fantomHome + File.separator + "log" + File.separator);
         logHome.mkdirs();
 
         File prefFile = new File(fantomHome, "jot.prefs");
         try
         {
             if (!prefFile.exists() || isOutdated(prefFile))
             {
                 prefFile = createPrefFiles(fantomHome);
             }
             System.setProperty("jot.prefs", prefFile.getAbsolutePath());
 
             JOTPreferences prefs = JOTPreferences.getInstance();
             // Initializing the Logger
             JOTLogger.init(prefs, logHome.getAbsolutePath(), "jot.log");
             JOTLogger.setPrintToConcole(true);
             // Call DB upgrader
             FanIndexer.upgrade();
             // Initialize the persistance / databases(s).
             JOTPersistanceManager.getInstance().init(prefs);
         } catch (Exception e)
         {
             e.printStackTrace();
         }
 
         //start indexer
         if (FanPlatform.isConfigured())
         {
             // run in a thread, so that startup continues
             new Thread()
             {
 
                 @Override
                 public void run()
                 {
                     try
                     { // Delay it a bit, it looks better if started once netbeans is done starting (popup).
                         sleep(6000);
                     } catch (Exception e)
                     {
                     }
                     FanIndexerFactory.getIndexer().indexAll(false);
                 }
             }.start();
         }
 
         super.restored();
     }
 
     /**
      * Shutdown
      * @return
      */
     @Override
     public boolean closing()
     {
         System.out.println("Shutting down Fantom plugin.");
         try
         {
            FanAction.shutdownTales();
             FanIndexer.shutdown();
             Thread.sleep(250);
             JOTPersistanceManager.getInstance().destroy();
             JOTLogger.destroy();
         } catch (Exception e)
         {
             e.printStackTrace();
         }
         return super.closing();
     }
 
     /**
      * Create the pref file(s) and return the main pref file.
      * @param fantomHome
      * @return
      */
     private File createPrefFiles(File fantomHome) throws IOException
     {
         InputStream is = getClass().getClassLoader().getResourceAsStream("net/colar/netbeans/fan/jot.prefs");
         File prefFile = new File(fantomHome, "jot.prefs");
         InputStream is2 = getClass().getClassLoader().getResourceAsStream("net/colar/netbeans/fan/db.properties");
         copyIsIntoFile(is, prefFile);
         File dbFile = new File(fantomHome, "db.properties");
         copyIsIntoFile(is2, dbFile);
 
         // Set the DB file path in the db props file
         File dbFolder = new File(fantomHome.getAbsolutePath() + File.separator + "db" + File.separator);
         dbFolder.mkdirs();
         FileInputStream fis = new FileInputStream(dbFile);
         Properties props = new Properties();
         props.load(fis);
         fis.close();
         // H2 JDBC
         props.setProperty("db.jdbc.url", "jdbc:h2:file:" + dbFolder.getAbsolutePath() + File.separator + "default;TRACE_LEVEL_FILE=1;LOCK_TIMEOUT=8000");
         FileOutputStream fos = new FileOutputStream(dbFile);
         props.store(fos, "");
         fos.close();
 
         // Set the DB path in the main props file
         FileInputStream fis2 = new FileInputStream(prefFile);
         Properties props2 = new Properties();
         props2.load(fis2);
         fis2.close();
         props2.setProperty("db.fs.root_folder.windows", dbFolder.getAbsolutePath() + File.separator);
         props2.setProperty("db.fs.root_folder.others", dbFolder.getAbsolutePath() + File.separator);
         // VERSION
         props2.setProperty(PROP_PREF_FILE_VERSION, "" + PREF_VERSION);
         FileOutputStream fos2 = new FileOutputStream(prefFile);
         props2.store(fos2, "");
         fos2.close();
 
         return prefFile;
     }
 
     private void copyIsIntoFile(InputStream is, File prefFile) throws IOException
     {
         FileOutputStream out = new FileOutputStream(prefFile);
         byte[] buffer = new byte[10000];
         int read = -1;
         while ((read = is.read(buffer)) != -1)
         {
             out.write(buffer, 0, read);
         }
         out.close();
         is.close();
     }
 
     private boolean isOutdated(File prefFile)
     {
         try
         {
             FileInputStream fis = new FileInputStream(prefFile);
             Properties props = new Properties();
             props.load(fis);
             String version = props.getProperty(PROP_PREF_FILE_VERSION);
             if (version != null && version.compareTo("" + PREF_VERSION) >= 0)
             {
                 return false;
             }
         } catch (Exception e)
         {
         }
         return true;
     }
 }
