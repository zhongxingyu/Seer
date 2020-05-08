 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SetupProduct.java,v 1.1 2007-05-10 17:21:22 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.setup;
 
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.TestConstants;
 import java.util.logging.Level;
 import java.io.FileInputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.PropertyResourceBundle;
 import java.util.Set;
 
 /**
  * This class calls the TestCommon.configureProduct method to configure
  * the product. In case product is already configured, nothing happens.
  */
 public class SetupProduct extends TestCommon {
 
     private String FILE_CLIENT_PROPERTIES = "AMConfig.properties";
     private Map properties = new HashMap();
 
     /**
      * If the system is execution in single server mode, it makes a call to
      * configure the product. If system is execution in multi server mode
      * (samlv2 etc), no product configuration call is made.
      */
     public SetupProduct(String testDir, String serverName1, String serverName2)
     {
         super("SetupProduct");
         if ((serverName2.indexOf("SERVER_NAME2")) != -1) {
            log(logLevel, "SetupProduct", "Initiating setup for " +
                     serverName1);
             initiateProductSetup(testDir, serverName1);
         } else {
            log(logLevel, "SetupProduct", "Multi serever initiation handled" +
                     " in respective modules");
         }
     }
 
      /**
       * Makes the actual call to configure the product. In case configuration
       * fails for some reason, it flags the product setup as failure in the
       * AMConfig.properties file. This prevents execution of all other
       * testcases.
       */ 
     public void initiateProductSetup(String testDir, String serverName) {
         entering("initiateProductSetup", null);
         try {
            PropertyResourceBundle clientDef = new PropertyResourceBundle(
                    new FileInputStream(testDir + fileseparator + "resources" +
                    fileseparator + FILE_CLIENT_PROPERTIES));
 
            log(logLevel, "initiateProductSetup", serverName);
             if (!configureProduct(getConfigurationMap("Configurator-" +
                     serverName))) {
                log(logLevel, "initiateProductSetup", "Product configuration failed.");
                 Set set = new HashSet();
                 set.add((String)TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT);
                 properties = getMapFromResourceBundle("AMConfig", set);
                 properties.put(TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT,
                         "fail");
                 createFileFromMap(properties, "resources" + fileseparator +
                         FILE_CLIENT_PROPERTIES);
             }
         } catch(Exception e) {
             log(Level.SEVERE, "initiateProductSetup", e.getMessage(), null);
             e.printStackTrace();
         }
         exiting("initiateProductSetup");
     }
 
     public static void main(String args[]) {
         try {
             SetupProduct cp = new SetupProduct(args[0], args[1], args[2]);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
