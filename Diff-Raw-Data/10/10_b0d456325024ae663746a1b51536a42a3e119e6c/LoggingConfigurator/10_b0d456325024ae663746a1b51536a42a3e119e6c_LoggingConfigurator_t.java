 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.tools;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 /**
  * A place to put a static method that looks for a Log4j.xml file
  * and does the configureAndWatch stuff to configure log4j.
  * @author John Walsh
  *
  */
 public class LoggingConfigurator {
     private static boolean loggingConfigured = false;
     private static final String LOGGER_CONFIG = "Log4j.xml";
     
     public static Logger configureLogging(Class callingClass) {
         if (!loggingConfigured) {
             URL configFileURL = callingClass.getClassLoader().getResource(LOGGER_CONFIG);
             if (configFileURL == null){
                 System.err.println("The log4j configuration file \""+LOGGER_CONFIG+
                     "\" was not found on the classpath.");
             }
            DOMConfigurator.configureAndWatch(configFileURL.toString());
             loggingConfigured = true;
             System.out.println("\n\nLogging initial configuration complete according to file "+configFileURL);
         }
         System.out.println("Log4j logger created.");
         return Logger.getLogger(callingClass.getName());
     }
 }
