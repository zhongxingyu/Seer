 /**
 * Copyright (c) 2009 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package com.redhat.katello.sam.proxy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Properties;
 
 /**
  * Config
  */
 public class Config {
 
     private static final String CONFIG_FILE = "/etc/sam-proxy/sam-proxy.conf";
     private static final String DEFAULT_CONFIG_RESOURCE = "config/sam-proxy.conf";
     private Properties props;
 
     public Config() {
 
         // Load system properties file, otherwise use the default:
         try {
             InputStream is = null;
 
             File configFile = new File(CONFIG_FILE);
             if (configFile.exists()) {
                 is = new FileInputStream(configFile);
             }
             else {
                 URL url = this.getClass().getClassLoader().getResource(
                     DEFAULT_CONFIG_RESOURCE);
                 is = url.openStream();
             }
             props = new Properties();
 
             props.load(is);
 
             is.close();
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public String getProperty(String key) {
         return props.getProperty(key);
     }
 
 }
