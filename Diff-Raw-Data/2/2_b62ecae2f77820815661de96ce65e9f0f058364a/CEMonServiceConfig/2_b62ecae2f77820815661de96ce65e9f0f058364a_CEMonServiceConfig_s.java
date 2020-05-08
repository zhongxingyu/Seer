 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. 
  * See http://www.eu-egee.org/partners/ for details on the copyright
  * holders.  
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *     http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package org.glite.ce.monitor.configuration;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 import org.glite.ce.commonj.configuration.CommonConfigException;
 import org.glite.ce.commonj.configuration.CommonServiceConfig;
 import org.glite.ce.commonj.configuration.xppm.ConfigurationListener;
 import org.glite.ce.monitorapij.resource.Resource;
 
 public class CEMonServiceConfig
     extends CommonServiceConfig {
 
     private static Logger logger = Logger.getLogger(CEMonServiceConfig.class.getName());
 
     protected static CEMonServiceConfig serviceConfiguration = null;
 
     public CEMonServiceConfig() throws CommonConfigException {
         super();
     }
 
     protected String getSysPropertyName() {
         return "cemonitor.configuration.path";
     }
 
     public ArrayList<Resource> getResources(String category) {
         ArrayList<Resource> result = new ArrayList<Resource>();
 
         Class<?> resClass = getClassForCategory(category);
         if (confManager != null && resClass != null) {
             Object[] tmpArray = confManager.getConfigurationElements(resClass);
 
             for (Object obj : tmpArray) {
                 logger.debug("Found resource " + ((Resource) obj).getId());
                 result.add((Resource) obj);
             }
         }
 
         return result;
 
     }
 
     public void registerListener(ConfigurationListener lsnr) {
         confManager.addListener(lsnr);
     }
 
     public static CEMonServiceConfig getConfiguration() {
         if (serviceConfiguration == null) {
 
             synchronized (CEMonServiceConfig.class) {
 
                 if (serviceConfiguration == null) {
 
                     serviceConfiguration = (CEMonServiceConfig) CommonServiceConfig.getConfiguration();
 
                 }
 
             }
         }
 
        return null;
     }
 
     public static Class<?> getClassForCategory(String category) {
         if (category == "action") {
             return org.glite.ce.monitorapij.resource.types.Action.class;
         }
         if (category == "queryprocessor") {
             return org.glite.ce.monitorapij.queryprocessor.QueryProcessor.class;
         }
         if (category == "subscription") {
             return org.glite.ce.monitorapij.resource.types.SubscriptionPersistent.class;
         }
         if (category == "sensor") {
             return org.glite.ce.monitorapij.sensor.Sensor.class;
         }
         return null;
     }
 
 }
