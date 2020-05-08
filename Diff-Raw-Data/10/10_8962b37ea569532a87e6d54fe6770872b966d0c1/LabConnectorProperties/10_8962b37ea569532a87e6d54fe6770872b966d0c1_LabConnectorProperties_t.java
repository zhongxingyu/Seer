 /**
  * SAHARA Scheduling Server - LabConnector
  * Properties class to derive the external LabConnector web service location
  * 
  * @license See LICENSE in the top level directory for complete license terms.
  * 
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names
  *    of its contributors may be used to endorse or promote products derived from
  *    this software without specific prior written permission.
  *    
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * @author Herbert Yeung
  * @date 27th May 2010
  */
 package au.edu.labshare.schedserver.labconnector.client;
 
 // Packages needed for reading properties file (without reinventing the wheel)
 import java.util.Properties;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 public class LabConnectorProperties
 {
     /** LabConnector properties configuration file location. */
     //public static String LABCONNECTOR_PROPERTIES_PATH = "resources/labconnector.properties";
     public static String LABCONNECTOR_PORTNUM         = "labconnector_port";
     public static String LABCONNECTOR_HOSTNAME        = "labconnector_remotehost";
     public static String LABCONNECTOR_PATH            = "labconnector_wspath";
 
     /** LabConnector configuration. */
     private Config       labConnectorConfig;
     private Logger       logger;
     private final Properties   labConnectorProperties;
     
     /** Used to store the configuration value*/
     public static String LABCONNECTOR_CONNECTION_STR  = null;
 
     public LabConnectorProperties(BundleContext context)
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
     
 
     public LabConnectorProperties()
     {
         this.labConnectorProperties = new Properties();
         populateDefaults();
     }
 
 
     public String getProperties()
     {
         if(LABCONNECTOR_CONNECTION_STR == null)
         {
             return "http://" + 
                     this.labConnectorProperties.getProperty(LabConnectorProperties.LABCONNECTOR_HOSTNAME) + ":" + 
                     this.labConnectorProperties.getProperty(LabConnectorProperties.LABCONNECTOR_PORTNUM) + 
                     this.labConnectorProperties.getProperty(LabConnectorProperties.LABCONNECTOR_PATH);
         }
         else
             return LABCONNECTOR_CONNECTION_STR;
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
         
         String remotehostProperty = this.labConnectorConfig.getProperty(LabConnectorProperties.LABCONNECTOR_HOSTNAME);
         if (remotehostProperty == null)
         {
             this.logger.warn("LabConnector Remote Host configuration property (labconnector_remotehost) not found," +
                     " using the default (" + this.labConnectorProperties.getProperty(LabConnectorProperties.LABCONNECTOR_HOSTNAME) + ").");
         }
         else
         {
             this.logger.info("LabConnector Remote Host name as " + remotehostProperty + ".");
             //this.labConnectorProperties.setProperty(LabConnectorProperties.LABCONNECTOR_HOSTNAME, remotehostProperty);
             LABCONNECTOR_CONNECTION_STR = "http://" + remotehostProperty + ":"; 
         }
         
         String portNumProperty = this.labConnectorConfig.getProperty(LabConnectorProperties.LABCONNECTOR_PORTNUM);
         if (portNumProperty == null)
         {
             this.logger.warn("LabConnector Remote Port Number configuration property (labconnector_port) not found," +
                     " using the default (" + this.labConnectorProperties.getProperty(LabConnectorProperties.LABCONNECTOR_PORTNUM) + ").");
         }
         else
         {
             this.logger.info("LabConnector Remote Port Number as " + portNumProperty + ".");
             //this.labConnectorProperties.setProperty(LabConnectorProperties.LABCONNECTOR_PORTNUM, portNumProperty);
             LABCONNECTOR_CONNECTION_STR = LABCONNECTOR_CONNECTION_STR + portNumProperty; 
         }
         
         String wsPathProperty = this.labConnectorConfig.getProperty(LabConnectorProperties.LABCONNECTOR_PATH);
         if (wsPathProperty == null)
         {
             this.logger.warn("LabConnector Web Services Path configuration property (labconnector_wspath) not found," +
                     " using the default (" + this.labConnectorProperties.getProperty(LabConnectorProperties.LABCONNECTOR_PATH) + ").");
         }
         else
         {
             this.logger.info("LabConnector Web Services Path configuration property as " + wsPathProperty + ".");
             //this.labConnectorProperties.setProperty(LabConnectorProperties.LABCONNECTOR_PATH, wsPathProperty);
             LABCONNECTOR_CONNECTION_STR = LABCONNECTOR_CONNECTION_STR + wsPathProperty; 
         }
     }
     
     private void populateDefaults()
     {        
         /* LabConnector specific defaults. */
         this.labConnectorProperties.setProperty(LabConnectorProperties.LABCONNECTOR_HOSTNAME, "ilabs-test.eng.uts.edu.au");
         this.labConnectorProperties.setProperty(LabConnectorProperties.LABCONNECTOR_PORTNUM, "7070");
        this.labConnectorProperties.setProperty(LabConnectorProperties.LABCONNECTOR_PATH, "/LabConnector/LabConnector.asmx");
     }
 }
