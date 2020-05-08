 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.config.internal;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.virgo.kernel.serviceability.NonNull;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class ConfigurationAdminConfigurationInfo implements ConfigurationInfo {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final ConfigurationAdmin configurationAdmin;
 
     private final String pid;
 
     public ConfigurationAdminConfigurationInfo(@NonNull ConfigurationAdmin configurationAdmin, String pid) {
         this.configurationAdmin = configurationAdmin;
         this.pid = pid;
     }
 
     public String getPid() {
         return this.pid;
     }
 
     public Map<String, String> getProperties() {
         try {
             Configuration configuration = configurationAdmin.getConfiguration(this.pid, null);
             Map<String, String> properties = new HashMap<String, String>();
 
             @SuppressWarnings("unchecked")
            Dictionary<String, String> dictionary = configuration.getProperties();
 
             if (dictionary != null) {
                 Enumeration<String> keys = dictionary.keys();
                 while (keys.hasMoreElements()) {
                     String key = keys.nextElement();
                    String value = dictionary.get(key);
                    properties.put(key, value);
                 }
             }
             return properties;
         } catch (IOException e) {
             logger.warn("Unable to get configuration for {}", this.pid);
         }
         return null;
     }
 
 }
