 package au.edu.labshare.schedserver.labconnector.service;
 
 //Packages needed for reading properties file (without reinventing the wheel)
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Properties;
 import java.text.SimpleDateFormat;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 public class LabConnectorExperimentStorage
 {
     /** LabConnector properties configuration file location. */
     public static String LABCONNECTOR_STORELOC         = "labconnector_storeloc";
     
     
     /** LabConnector configuration. */
     private Config       labConnectorConfig;
     private Logger       logger;
     private final Properties   labConnectorStorageProperties;
     
     /** Used to store the configuration value*/
    public static String LABCONNECTOR_STORELOC_STR  = null;
 
     public LabConnectorExperimentStorage(BundleContext context)
     {        
         /* 
          * Load the default properties
          */
         this();
         
         /*
          * Log anything that goes wrong        
          */
         this.logger = LoggerActivator.getLogger();
         
         ServiceReference ref = context.getServiceReference(Config.class.getName());
         this.labConnectorConfig = (Config)context.getService(ref);
         
         /*
          * Get the properties from either SchedServer/trunk/conf/schedulingserver.properties file
          * or from default values
          */
         loadConfiguration();
     }
     
     public LabConnectorExperimentStorage()
     {
         this.labConnectorStorageProperties = new Properties();
         populateDefaults();
     }
     
     /**
      * Loads the configuration properties. The following list contains
      * the configuration keys and expected data types:
      * <ul>
      *  <li>labconnector_remotehost - String - The LabConnector URL on the remote end.</li>
      *  <li>labconnector_port - PortNumber.</li>
      *  <li>labconnector_wspath - String - WebService path to the LabConnector hosted service.</li>
      * </ul>
      */
     private void loadConfiguration()
     {
         
         String storageLocProperty = this.labConnectorConfig.getProperty(LabConnectorExperimentStorage.LABCONNECTOR_STORELOC);
         if (storageLocProperty == null)
         {
             this.logger.warn("LabConnector Experiment Storage Location configuration property (labconnector_storeloc) not found," +
                     " using the default (" + this.labConnectorStorageProperties.getProperty(LabConnectorExperimentStorage.LABCONNECTOR_STORELOC_STR) + ").");
         }
         else
         {
             this.logger.info("LabConnector Experiment Storage Location as " + storageLocProperty + ".");
             LABCONNECTOR_STORELOC_STR = storageLocProperty; 
         }
     }
     
     public String getStorageLocation()
     {
         if(LABCONNECTOR_STORELOC_STR == null)
         {
             return this.labConnectorStorageProperties.getProperty(LabConnectorExperimentStorage.LABCONNECTOR_STORELOC_STR);
         }
         else
             return LABCONNECTOR_STORELOC_STR;
     }
     
     private void populateDefaults()
     {        
         /* LabConnector specific defaults. */
         this.labConnectorStorageProperties.setProperty(LabConnectorExperimentStorage.LABCONNECTOR_STORELOC_STR, "/Users/neo/");
     }
     
     public boolean writeExperimentResults(String userID, String experimentResults)
     {
         String filePath = getStorageLocation() + "/" + userID + "/" + "results.txt";
         try
         {
             //Open up filestream to write to 
             final FileOutputStream output = new FileOutputStream(filePath, true);
             final Date date = new Date();
             final SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
             String dateStr = simpleDate.format(date);
             output.write(dateStr.getBytes());
             output.write((new String("\n")).getBytes());
             output.write(experimentResults.getBytes());
             output.flush();
             output.close();
             
             return true;
       
         }
         catch(IOException e)
         {
             System.err.println("Failed to write to file (" + filePath + "). The error is of type" +
                     e.getClass().getCanonicalName() + " with message " + e.getMessage() + ".");
         }
         
         return false;
     }
     
     public String readExperimentResults(String userID, String experimentID)
     {
         String filePath = getStorageLocation() + "/" + userID + "/" + "results.txt";
         try
         {
             //Open up a file + filestream to write to 
             int ch;
             StringBuffer strContent = new StringBuffer("");
            
             final FileInputStream input = new FileInputStream(filePath);
             
             while((ch = input.read()) != -1)
                     strContent.append(ch);
             
             input.close();
             return strContent.toString();
             
         }
         catch(IOException e)
         {
             System.err.println("Failed to read from file (" + filePath + "). The error is of type" +
                     e.getClass().getCanonicalName() + " with message " + e.getMessage() + ".");
         }
         
         return null;
     }
 }
