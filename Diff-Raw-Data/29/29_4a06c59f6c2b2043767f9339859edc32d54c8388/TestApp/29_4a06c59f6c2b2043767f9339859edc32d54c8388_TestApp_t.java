 package cz.vity.freerapid.plugins.services.enterupload;
 
 import cz.vity.freerapid.plugins.dev.PluginDevApplication;
 import cz.vity.freerapid.plugins.webclient.ConnectionSettings;
 import cz.vity.freerapid.plugins.webclient.interfaces.HttpFile;
 import org.jdesktop.application.Application;
 
 import java.net.URL;
 
 /**
  * @author Ladislav Vitasek
  */
 public class TestApp extends PluginDevApplication {
     @Override
     protected void startup() {
         final HttpFile httpFile = getHttpFile(); //creates new test instance of HttpFile
         try {
             //we set file URL
            httpFile.setNewURL(new URL("http://www.enterupload.com/tolq8f2vbzup/favicon.png.html"));
             //the way we connect to the internet
             final ConnectionSettings connectionSettings = new ConnectionSettings();// creates default connection
             //then we tries to download
             final EnteruploadServiceImpl service = new EnteruploadServiceImpl(); //instance of service - of our plugin
             testRun(service, httpFile, connectionSettings);//download file with service and its Runner
             //all output goes to the console
         } catch (Exception e) {//catch possible exception
             e.printStackTrace(); //writes error output - stack trace to console
         }
         this.exit();//exit application
     }
 
     /**
      * Main start method for running this application
      * Called from IDE
      *
      * @param args arguments for application
      */
     public static void main(String[] args) {
         Application.launch(TestApp.class, args);//starts the application - calls startup() internally
     }
 }
