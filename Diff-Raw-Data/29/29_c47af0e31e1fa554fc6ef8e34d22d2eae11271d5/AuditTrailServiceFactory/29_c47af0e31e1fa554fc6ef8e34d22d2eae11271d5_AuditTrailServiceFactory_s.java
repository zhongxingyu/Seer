 /*
  * #%L
  * Bitrepository Audit Trail Service
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.audittrails.service;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.bitrepository.protocol.security.BasicMessageAuthenticator;
 import org.bitrepository.protocol.security.BasicMessageSigner;
 import org.bitrepository.protocol.security.BasicOperationAuthorizor;
 import org.bitrepository.protocol.security.BasicSecurityManager;
 import org.bitrepository.protocol.security.MessageAuthenticator;
 import org.bitrepository.protocol.security.MessageSigner;
 import org.bitrepository.protocol.security.OperationAuthorizor;
 import org.bitrepository.protocol.security.PermissionStore;
 import org.bitrepository.protocol.security.SecurityManager;
 
 public class AuditTrailServiceFactory {
     
     private static AuditTrailService auditTrailService;
     private static String configurationDir;
     private static String privateKeyFile;
     private static MessageAuthenticator authenticator;
     private static MessageSigner signer;
     private static OperationAuthorizor authorizer;
     private static PermissionStore permissionStore;
     private static SecurityManager securityManager;
     
     /** The properties file holding implementation specifics for the alarm service. */
     private static final String CONFIGFILE = "audittrails.properties";
     /** Property key to tell where to locate the path and filename to the private key file. */
     private static final String PRIVATE_KEY_FILE = "org.bitrepository.audit-trail-service.privateKeyFile";
     
     /**
      * Private constructor as the class is meant to be used in a static way.
      */
     private AuditTrailServiceFactory() {
         
     }
     
     
     public static void init(String confDir) {
         configurationDir = confDir;
     }
     
     /**
      * Factory method to retrieve AuditTrailService  
      */
     public synchronized static AuditTrailService getAuditTrailService() {
         if(auditTrailService == null) {
            loadProperties();
            permissionStore = new PermissionStore();
            authenticator = new BasicMessageAuthenticator(permissionStore);
            signer = new BasicMessageSigner();
            authorizer = new BasicOperationAuthorizor(permissionStore);
            securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                    authenticator, signer, authorizer, permissionStore);
            auditTrailService = new AuditTrailService();
         }
         
         return auditTrailService;
     }
     
     private static void loadProperties() throws IOException {
         Properties properties = new Properties();
         String propertiesFile = configurationDir + "/" + CONFIGFILE;
         BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
         properties.load(propertiesReader);
         privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
     }
 }
