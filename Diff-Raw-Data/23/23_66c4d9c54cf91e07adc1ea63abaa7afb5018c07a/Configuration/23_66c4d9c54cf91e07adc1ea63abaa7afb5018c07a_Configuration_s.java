 /**
  * Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group
  * E184
  *
  * This work was partially supported by the European Commission in terms of the
  * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package at.ac.tuwien.dsg.mela.analysisservice.utils;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Properties;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
  *
  * Class which acts as entry point towards all configuration options that need
  * to be added to the MELA-AnalysisService
  *
  */
 public class Configuration {
 
     private static Properties configuration;
     static Logger logger;
 
     static {
         configuration = new Properties();
         try {
             InputStream is = Configuration.class.getResourceAsStream("/config/Config.properties");
             configuration.load(is);
             PropertyConfigurator.configure(Configuration.class.getResourceAsStream("/config/Log4j.properties"));
             logger = Logger.getLogger("melaAnalysisServiceLogger");
         } catch (Exception ex) {
             Logger.getLogger(Configuration.class.getName()).log(Level.FATAL, null, ex);
         }
 
     }
 
     public static Logger getLogger(Class loggerClass) {
         return Logger.getLogger(loggerClass);
     }
 
    public static int getMonitoringAggregationIntervalInSeconds() {
         if (configuration.containsKey("DATA_COLLECTION_INTERVAL_IN_SECONDS")) {
             return Integer.parseInt(configuration.getProperty("DATA_COLLECTION_INTERVAL_IN_SECONDS"));
         } else {
             return 5; //default 5 seconds
         }
     }
 
    public static int getMonitoringIntervalInSeconds() {
         if (configuration.containsKey("DATA_AGGREGATION_WINDOWS")) {
             return Integer.parseInt(configuration.getProperty("DATA_AGGREGATION_WINDOWS"));
         } else {
             return 2; //default 2 frames
         }
     }
 
     public static Boolean isElasticityAnalysisEnabled() {
         if (configuration.containsKey("ELASTICITY_ANALYSIS_ENABLED")) {
             return Boolean.parseBoolean(configuration.getProperty("ELASTICITY_ANALYSIS_ENABLED").toLowerCase());
         } else {
             return true; //default
         }
     }
 
     public static int getDataServicePort() {
         if (configuration.containsKey("MELA_DATA_SERVICE_PORT")) {
             return Integer.parseInt(configuration.getProperty("MELA_DATA_SERVICE_PORT"));
         } else {
             return 9123; //default 2 frames
         }
     }
 
     public static String getDataServiceIP() {
         if (configuration.containsKey("MELA_DATA_SERVICE_IP")) {
             return configuration.getProperty("MELA_DATA_SERVICE_IP");
         } else {
             return "localhost";
         }
     }
     
     
     public static String getJCatascopiaIP() {
         if (configuration.containsKey("JCATASCOPIA_IP")) {
             return configuration.getProperty("JCATASCOPIA_IP");
         } else {
             return "localhost";
         }
     }
 }
