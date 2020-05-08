 package edu.duke.cabig.catrip.gui;
 
 import edu.duke.cabig.catrip.gui.util.CommonUtils;
 import edu.duke.cabig.catrip.gui.util.ExceptionThreadGroup;
 import edu.duke.cabig.catrip.gui.util.GUIConstants;
 import edu.duke.cabig.catrip.gui.util.Logger;
 import edu.duke.cabig.catrip.gui.webstart.WebstartConfigurator;
 import edu.duke.cabig.catrip.gui.wizard.WelcomeScreen;
 import java.io.File;
 import java.util.ArrayList;
 import org.apache.commons.logging.Log;
 
 /**
  * Main class of the GUI project. Entry point for the GUI.
  *
  * @author Sanjeev Agarwal .
  */
 public class Main {
     static Log log ;//= Logger.getDefaultLogger();
     private static ArrayList<String> bufferedLogMsgs = new ArrayList<String>(10); 
     
     
     /** Creates a new instance of Main */
     public Main() {
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // This is the entry point to the GUI module.
         // Perform few basic check on the settings of the GUI and launch the Welcome Screen.
         // perform the static initializations also if required.
         // Check:
         // caTRIP_config.xml for Index service and Dorian Urls.
         
         
         // log file setup.
         try{
             File logFile = new File("C:\\caTRIP_logs.txt");
             if (!logFile.exists()){
                 bufferedLogMsgs.add(" Log File doesn't exist.. Creating one.. "); 
                 System.out.println(" Log File doesn't exist.. Creating one.. ");
                 logFile.createNewFile();
             }
         } catch (Exception e) {
             e.printStackTrace();
             bufferedLogMsgs.add(" Error in Creating the Log File: "+ e.getMessage());
             System.out.println(" Error in Creating the Log File: \n"+ CommonUtils.getStringException(e));
         }
         
         
         // Process all arguments here.
         bufferedLogMsgs.add(" reading proeprty: catrip.home.dir ");
         String caTripHomeDir = System.getProperty("catrip.home.dir");
         if (caTripHomeDir != null){
             bufferedLogMsgs.add(" User supplied the new CATRIP_HOME property: "+caTripHomeDir);
             GUIConstants.CATRIP_HOME = System.getProperty("user.home") + File.separator + caTripHomeDir.trim();
             GUIConstants.CATRIP_CONFIG_FILE_LOCATION = GUIConstants.CATRIP_HOME + File.separator + "catrip-config.xml";
             // change that in fqe class also..
             //gov.nih.nci.catrip.fqe.utils.PropertyReader.CATRIP_HOME = GUIConstants.CATRIP_HOME;
             bufferedLogMsgs.add(" CaTRIP configuration Directory location is changed to: "+GUIConstants.CATRIP_HOME);
             System.out.println("CaTRIP configuration Directory location is changed to: "+GUIConstants.CATRIP_HOME);
         }
         
          // TODO -SB- Ideally check if the new caTRIP_HOME is having correct set of config files? or version? if not than overwrite that.
         
        
         
         bufferedLogMsgs.add(" reading proeprty: catrip.config.version ");
         String caTRIP_Version = System.getProperty("catrip.config.version");
         if (caTRIP_Version != null){
             GUIConstants.caTRIPVersion = caTRIP_Version.trim();
             bufferedLogMsgs.add(" User supplied the new CATRIP_VERSION property: "+caTRIP_Version);
         }
         
         
         
         // sanjeev: check if the application is launched via the webstart context.
         String webstartStr = System.getProperty("deployment.user.cachedir");
         if(webstartStr != null){
             bufferedLogMsgs.add(" This is a webstart version of caTRIP");
             System.out.println("This is a webstart version of caTRIP");
             WebstartConfigurator.configure();
         } else {
             bufferedLogMsgs.add(" This is a stand alone version of caTRIP");
             System.out.println("This is a stand alone version of caTRIP");
         }
         
        log = Logger.getDefaultLogger();
         
         // now add the buffered log msgs into log file.
         for (int i = 0; i < bufferedLogMsgs.size(); i++) {
             String msg = bufferedLogMsgs.get(i);
             log.info(msg); 
         }
         
         
         ThreadGroup exceptionThreadGroup = new ExceptionThreadGroup();
         
         java.awt.EventQueue.invokeLater(new Thread(exceptionThreadGroup, "Init thread") {
             public void run() {
                 WelcomeScreen ws= new WelcomeScreen();
                 //ws.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                 ws.setBounds(10,10,550,365);
                 ws.center();
                 ws.setVisible(true);
                 
             }
         });
         
         
         
         
     }
     
 }
