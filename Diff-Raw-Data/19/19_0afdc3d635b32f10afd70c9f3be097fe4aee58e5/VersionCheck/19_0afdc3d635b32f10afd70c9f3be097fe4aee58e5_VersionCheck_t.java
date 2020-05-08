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
 * $Id: VersionCheck.java,v 1.3 2007-08-29 21:18:45 exu Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.tools.bundles;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 public class VersionCheck implements SetupConstants{
     
     /**
      * Check whether the version AM is valid.
      *
      * @param bundle The ResourceBundle contains the prompt message.
      */
     public static int isValid(ResourceBundle bundle) {
         Properties configProperties = new Properties();
         String amExpectedVersion = null;
         String javaExpectedVersion = null;
         InputStream configFile = null;
         javaExpectedVersion = System.getProperty(JAVA_VERSION_EXPECTED);
         amExpectedVersion = System.getProperty(AM_VERSION_EXPECTED);
         try {
             configFile = new FileInputStream(System.getProperty(VERSION_FILE));
             configProperties.load(configFile);
         } catch (IOException ex) {
             //ex.printStackTrace();
             System.out.println(bundle.getString("message.error.amconfig") + " "
                 + System.getProperty(VERSION_FILE));
             return 1;
         } finally {
             try {
                 configFile.close();
             } catch (IOException ignored) {
             }
         }
         if (! ((new File(System.getProperty(XML_CONFIG))).exists())) {
             System.out.println(System.getProperty(XML_CONFIG) + " " +
                 bundle.getString("message.error.serverconfig"));
             return 1;
         }
         String configVersion = configProperties.getProperty(System.getProperty(
             AM_VERSION_CURRENT));
         if (!versionCompatible(System.getProperty(JAVA_VERSION_CURRENT),
             javaExpectedVersion)) {
             System.out.println(bundle.getString("message.error.version.jvm") +
                 " " + javaExpectedVersion + " .");
             return 1;
         }
         if (!versionCompatible(configVersion.trim(), amExpectedVersion)) {
             System.out.println(bundle.getString("message.error.version.am") +
                 " " + amExpectedVersion + " .");
             return 1;
         }
         return 0;
     }
 
     /**
      * Check whether the version String is compatible with expected.
      * 
      * @param currentVersion The string of current version.
      * @param expectedVersion The string of expected version.
      * @return A boolean value to indicate whether the version is compatible.
      */
     protected static boolean versionCompatible(String currentVersion,
         String expectedVersion) {
         if (Character.isDigit(expectedVersion.charAt(expectedVersion.length()
             - 1))) {
             if (!currentVersion.startsWith(expectedVersion)) {
                 return false;
             }
         } else {
             boolean backwardCom = false;
             int compareLength = Math.min(expectedVersion.length() - 1,
                 currentVersion.length());
             if (expectedVersion.endsWith("-")) {
                 backwardCom = true;
             }
             for (int i = 0; i < compareLength; i++) {
                 if (backwardCom) {
                     if (expectedVersion.charAt(i) < currentVersion.charAt(i)) {
                         return false;
                     }
                 } else {
                    if (currentVersion.charAt(i) > expectedVersion.charAt(i)) {
                        break;
                    }
                     if (currentVersion.charAt(i) < expectedVersion.charAt(i)) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 }
