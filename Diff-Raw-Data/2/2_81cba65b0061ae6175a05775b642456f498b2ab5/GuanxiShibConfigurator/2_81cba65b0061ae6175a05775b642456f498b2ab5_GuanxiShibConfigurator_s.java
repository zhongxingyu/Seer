 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.xwiki.authentication.guanxi;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class GuanxiShibConfigurator {
 
     private static final Log log = LogFactory.getLog(GuanxiShibConfigurator.class);
     
     /**
      *   
      *   Load the config from file 
      */
     public static GuanxiShibConfig getGuanxiShibConfig( ) {
        
        if (log.isDebugEnabled()) {
            log.debug("Loading GuanxiShibConfig using " + 
               GuanxiShibConstants.PROPERTIES_FILE + " for GX-Auth values");
        }
 
        GuanxiShibConfig config = new GuanxiShibConfig();
 
        try { 
            // load the config from file
            InputStream propertiesIn = null; 
            propertiesIn = GuanxiShibAuthenticator.class.getResourceAsStream(
                     GuanxiShibConstants.PROPERTIES_FILE);
 
            Properties configProperties = new Properties();
            configProperties.load(propertiesIn);
 
            // set properties file
            config.setPropertiesFile(configProperties.getProperty(
                 GuanxiShibConstants.PROPERTIES_FILE));
 
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator Properties file set to: " + 
                   config.getPropertiesFile());
            }
 
            // set create users
            config.setCreateUsers(Boolean.valueOf(configProperties.getProperty(
                 GuanxiShibConstants.CREATE_USERS)).booleanValue());
 
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator set to create users: " + 
                   config.isCreateUsers());
            }
 
            // set update info 
            config.setUpdateInfo(Boolean.valueOf(configProperties.getProperty(
                 GuanxiShibConstants.UPDATE_INFO)).booleanValue());
 
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator set to update info: " + 
                  config.isCreateUsers());
            }
 
            // set default groups 
            List defaultGroups = new ArrayList();
 
            String groups = configProperties.getProperty(
                GuanxiShibConstants.DEFAULT_XWIKI_GROUPS);
            
            if (groups != null) {
                 defaultGroups.addAll(StringUtils.
                     toListDelimitedByComma(groups));
                 
                 if (log.isDebugEnabled()) {
                     for (Iterator i = defaultGroups.iterator(); i.hasNext();) {
                         log.debug("Adding group " + i.next().toString() +
                                   " to list of groups");
                     }
                 }
            }
 
            config.setDefaultGroups(defaultGroups);
              
            // set default user space 
            config.setDefaultUserSpace(configProperties.getProperty(
                 GuanxiShibConstants.DEFAULT_XWIKI_USER_SPACE));
 
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator using " + 
                   config.getDefaultUserSpace() + " as user space");
            }
 
            // set userid header 
            config.setHeaderUserid(configProperties.getProperty(
                 GuanxiShibConstants.HEADER_USERID));
          
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator userid header set to " + 
                   config.getHeaderUserid());
            }
 
            // set header mail
            config.setHeaderMail(configProperties.getProperty(
                 GuanxiShibConstants.HEADER_MAIL));
          
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator mail header set to " + 
                   config.getHeaderMail());
            }
 
            // set header fullname
            config.setHeaderFullname(configProperties.getProperty(
                 GuanxiShibConstants.HEADER_FULLNAME));
          
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator fullname header set to " + 
                   config.getHeaderFullname());
            }
 
            // set 
            config.setReplacementChar(configProperties.getProperty(
                 GuanxiShibConstants.REPLACEMENT_CHAR));
          
            if (log.isDebugEnabled()) {
                log.debug(
                   "GuanxiAuthenticator replacement character set to " + 
                   config.getReplacementChar());
            }
            
        } catch (IOException e) {
             log.warn("Unable to read properties from file, defaulting", e);
        }
 
        return config;
 
     } 
 }
