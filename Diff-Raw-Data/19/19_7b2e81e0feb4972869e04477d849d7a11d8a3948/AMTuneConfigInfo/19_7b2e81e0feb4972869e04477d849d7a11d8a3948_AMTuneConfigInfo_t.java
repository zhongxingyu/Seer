 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
  *
  * The contents of this file are subject to the terms
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
 * $Id: AMTuneConfigInfo.java,v 1.2 2008-07-10 12:43:47 kanduls Exp $
  */
 
 package com.sun.identity.tune.config;
 
 import com.sun.identity.tune.base.WebContainerConfigInfoBase;
 import com.sun.identity.tune.common.MessageWriter;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.common.AMTuneLogger;
 import com.sun.identity.tune.constants.DSConstants;
 import com.sun.identity.tune.constants.FAMConstants;
 import com.sun.identity.tune.constants.AMTuneConstants;
 import com.sun.identity.tune.constants.WebContainerConstants;
 import com.sun.identity.tune.util.AMTuneUtil;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 
 /**
  * This class contains tuning parameter and there values.
  */
 public class AMTuneConfigInfo implements AMTuneConstants, DSConstants,
 FAMConstants, WebContainerConstants {
     
     private String confFileName;
     private AMTuneLogger pLogger;
     private MessageWriter mWriter;
     private boolean isReviewMode;
     private boolean tuneOS;
     private boolean tuneWebContainer;
     private boolean tuneDS;
     private boolean tuneFAM;
     private boolean isJVM64BitAvailable;
     private String osType;
     private String osPlatform;
     private String hostName;
     private String webContainer;
     private String famAdmLocation;
     private String famConfigDir;
     private String famServerUrl;
     private String famAdmUser;
     private String defaultOrgPeopleContainer;
     private int famTunePctMemoryToUse;
     private int famTunePerThreadStackSizeInKB;
     private int famTunePerThreadStackSizeInKB64Bit;
     private boolean famTuneDontTouchSessionParameters;
     private int famTuneSessionMaxSessionTimeInMts;
     private int famTuneSessionMaxIdleTimeInMts;
     private int famTuneSessionMaxCachingTimeInMts;
     private double famTuneMemMaxHeapSizeRatio;
     private double famTuneMemMinHeapSizeRatio;
     private int famTuneMinMemoryToUseInMB;
     private int famTuneMaxMemoryToUseInMB;
     private int famTuneMaxMemoryToUseInMBDefault;
     private String famTuneMemMaxHeapSizeRatioExp;
     private String famAdminPassword;
     private List realms;
     private String perlBinDir;
     private int gcThreads;
     private int acceptorThreads;
     private int numNotificationQueue;
     private int numNotificationThreads;
     private int numSMLdapThreads;
     private int numLdapAuthThreads;
     private int numRQThrottle;
     private int numOfMaxThreadPool;
     private int numCpus;
     private int memAvail;
     private int memToUse;
     private int maxHeapSize;
     private int minHeapSize;
     private int maxNewSize;
     private int maxPermSize;
     private int cacheSize;
     private int sdkCacheSize;
     private int numSDKCacheEntries;
     private int sessionCacheSize;
     private int numSessions;
     private double amTuneMaxNoThreads;
     private double amTuneMaxNoThreads64Bit;
     private int maxThreads;
     private WebContainerConfigInfoBase webConfigInfo = null;
     private ResourceBundle confBundle;
     private DSConfigInfo dsConfigInfo;
     private DSConfigInfo smConfigInfo;
     private boolean isUMSMDSSame;
     private boolean tuneUMOnly;
     
     /**
      * Constructs the instance of AMTuneConfigInfo
      * 
      * @param confFileName Configuration File name.  This file will be 
      * searched in the classpath.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public AMTuneConfigInfo(String confFileName)
     throws AMTuneException {
         this.confFileName = confFileName;
         pLogger = AMTuneLogger.getLoggerInst();
         mWriter = MessageWriter.getInstance();
         initialize();
     }
     
     /**
      * Initializes the configuration information.
      * 
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void initialize() 
     throws AMTuneException {
         try {
             confBundle = ResourceBundle.getBundle(confFileName);
             setReviewMode(confBundle.getString(AMTUNE_MODE));
             setLogType(confBundle.getString(AMTUNE_LOG_LEVEL));
             setTuneOS(confBundle.getString(AMTUNE_TUNE_OS));
             setTuneWebContainer(
                     confBundle.getString(AMTUNE_TUNE_WEB_CONTAINER));
             setWebContainer(confBundle.getString(WEB_CONTAINER));
             if (isTuneWebContainer() || isTuneFAM()) {
                 if (getWebContainer().equals(WS7_CONTAINER)) {
                     webConfigInfo = new WS7ContainerConfigInfo(confBundle);
                 } else if (getWebContainer().equals(AS91_CONTAINER)) {
                     webConfigInfo = new AS9ContainerConfigInfo(confBundle);
                 }
             }
             if (webConfigInfo != null) {
                 isJVM64BitAvailable = webConfigInfo.isJVM64Bit();
             } else {
                 isJVM64BitAvailable = false;
             }
             setTuneDS(confBundle.getString(AMTUNE_TUNE_DS));
             setTuneFAM(confBundle.getString(AMTUNE_TUNE_IDENTITY));
             if (isTuneFAM()) {
                 setFamAdminPassword(confBundle.getString(FAMADM_PASSWORD));
                 setFAMServerUrl(confBundle.getString(FAMSERVER_URL));
                 setFAMAdmUser(confBundle.getString(FAMADM_USER));
                 setRealms(confBundle.getString(REALM_NAME));
                setFAMAdmLocation(confBundle.getString(FAMADM_LOCATION));
                setFAMConfigDir(confBundle.getString(FAM_CONFIG_DIR));
             }
             setFAMTuneMinMemoryToUseInMB(
                     confBundle.getString(AMTUNE_MIN_MEMORY_TO_USE_IN_MB));
             setFAMTuneMaxMemoryToUseInMBDefault(
                     confBundle.getString(
                     AMTUNE_MAX_MEMORY_TO_USE_IN_MB_DEFAULT));
             setFAMTuneMaxMemoryToUseInMB();
             setFAMTunePerThreadStackSizeInKB(confBundle.getString(
                     AMTUNE_PER_THREAD_STACK_SIZE_IN_KB));
             setFAMTunePerThreadStackSizeInKB64Bit(confBundle.getString(
                     AMTUNE_PER_THREAD_STACK_SIZE_IN_KB_64_BIT));
             setFAMTuneDontTouchSessionParameters(confBundle.getString(
                     AMTUNE_DONT_TOUCH_SESSION_PARAMETERS));
             setFAMTunePctMemoryToUse(
                     confBundle.getString(AMTUNE_PCT_MEMORY_TO_USE));
             setFAMTuneSessionMaxSessionTimeInMts(
                     confBundle.getString(
                     AMTUNE_SESSION_MAX_SESSION_TIME_IN_MTS));
             setFAMTuneSessionMaxIdleTimeInMts(
                     confBundle.getString(AMTUNE_SESSION_MAX_IDLE_TIME_IN_MTS));
             setFAMTuneSessionMaxCachingTimeInMts(
                     confBundle.getString(
                     AMTUNE_SESSION_MAX_CACHING_TIME_IN_MTS));
             setFAMTuneMemMaxHeapSizeRatio(confBundle.getString(
                     AMTUNE_MEM_MAX_HEAP_SIZE_RATIO));
             setFAMTuneMemMinHeapSizeRatio(confBundle.getString(
                     AMTUNE_MEM_MIN_HEAP_SIZE_RATIO));
             setDefaultOrgPeopleContainer(
                     confBundle.getString(DEFAULT_ORG_PEOPLE_CONTAINER));
             if (isTuneDS()) {
                 setUMSMDSSame(confBundle.getString(IS_UM_SM_DATASTORE_SAME));
                 setTuneUMOnly(confBundle.getString(TUNE_UM_ONLY));
                 dsConfigInfo = new DSConfigInfo(confBundle, false);
                 if (!isUMSMDSSame()) {
                     smConfigInfo = new DSConfigInfo(confBundle, true);
                 }
             } 
             calculateTuneParams();
         } catch (Exception ex) {
             pLogger.logException("initialize", ex);
             throw new AMTuneException("Couldn't initialize " +
                     "configuration data");
         }
     }
     
     /**
      * This method parses realm string and converts to list of realms to be 
      * tuned.
      * @param realmNames
      */
     private void setRealms(String realmNames) 
     throws AMTuneException {
         if (realmNames != null && realmNames.trim().length() > 0) {
             if (realmNames.indexOf("|") != -1) {
             realms = AMTuneUtil.getTokensList(realmNames, "|"); 
             } else {
                 realms = new ArrayList();
                 realms.add(realmNames);
             }
         } else {
            mWriter.writelnLocaleMsg("pt-inval-config");
             AMTuneUtil.printErrorMsg(REALM_NAME);
             pLogger.log(Level.SEVERE, "setRealms",
                     "Error setting Realms. " +
                     "Please check the value for the property " +
                     REALM_NAME);
             throw new AMTuneException("Invalid value for " + REALM_NAME);
         }
     }
     
     /**
      * Returns list of realms to be tuned.
      * @return
      */
     public List getRealms() {
         return realms;
     }
     
     private void setReviewMode(String reviewMode) {
         if (reviewMode != null && 
                 reviewMode.trim().equalsIgnoreCase("CHANGE")) {
             isReviewMode = false;
         } else {
             isReviewMode = true;
         }
         pLogger.log(Level.INFO, "setReviewMode", "Review mode is set to : " + 
                 isReviewMode);
     }
     
     /**
      * Return ture if only review mode.
      * @return
      */
     public boolean isReviewMode() {
         return isReviewMode;
     }
     
     /**
      * Set logging Type
      * @param logType
      */
     private void setLogType(String logType) {
         if (logType != null && logType.trim().equals("NONE")) {
             MessageWriter.setWriteToFile(true);
             MessageWriter.setWriteToTerm(false);
         } else if (logType != null && logType.trim().equals("TERM")) {
             MessageWriter.setWriteToFile(false);
             MessageWriter.setWriteToTerm(true);
         } else if (logType != null && logType.trim().equals("FILE")) {
             MessageWriter.setWriteToFile(true);
             MessageWriter.setWriteToTerm(true);
         }
     }
     
     private void setTuneOS(String value) {
         if (value != null && value.equalsIgnoreCase("true")) {
             tuneOS = true;
         } else {
             tuneOS = false;
             pLogger.log(Level.INFO, "setTuneOS", "OS will not be tuned.");
         }
     }
     
     public boolean isTuneOS() {
         return tuneOS;
     }
     
     private void setTuneWebContainer(String value) {
         if (value != null && value.equalsIgnoreCase("true")) {
             tuneWebContainer = true;
         } else {
             tuneWebContainer = false;
             pLogger.log(Level.INFO, "setTuneWebContainer", "Web container " +
                     "will not be tuned.");
         }
     }
     
     public boolean isTuneWebContainer() {
         return tuneWebContainer;
     }
     
     private void setTuneDS(String value) {
         if (value != null && value.equals("true")) {
             tuneDS = true;
         } else {
             tuneDS = false;
             pLogger.log(Level.INFO, "setTuneDS", "Directory Server will not " +
                     "be tuned.");
         }
     }
     
     public boolean isTuneDS() {
         return tuneDS;
     }
     
     private void setTuneFAM(String value) {
         if (value != null && value.equals("true")) {
             tuneFAM = true;
         } else {
             tuneFAM = false;
             pLogger.log(Level.INFO, "setTuneFAM", "Federated AccessManager " +
                     "will not be tuned.");
         }
     }
     
     public boolean isTuneFAM() {
         return tuneFAM;
     }
     
     private void setOSType() {
         osType = System.getProperty("os.name");
     }
     
     public String getOSType() {
         return osType;
     }
     
     private void setOSPlatform() {
         osPlatform = System.getProperty("os.arch");
     }
     
     public String getOSPlatform() {
         return osPlatform;
     }
     
     private void setHostName() {
         hostName = AMTuneUtil.getHostName();
     }
     
     public String getHostName() {
         return hostName;
     }
     
     /**
      * Set Web container type.
      * @param webContainer webContainer type.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setWebContainer(String webContainer) 
     throws AMTuneException {
         if (webContainer != null && webContainer.trim().length() > 0 &&
                 AMTuneUtil.isSupportedWebContainer(webContainer.trim())) {
             this.webContainer = webContainer.trim();
         } else {
             pLogger.log(Level.SEVERE, "setWebContainer",
                     "Unsupported web container.  Please check the value for " +
                     WEB_CONTAINER);
             mWriter.writelnLocaleMsg("pt-webcon-not-supported");
             AMTuneUtil.printErrorMsg(WEB_CONTAINER);
             throw new AMTuneException("Unsupported Web Container");
         }
     }
     
     public String getWebContainer() {
         return webContainer;
     }
     
     /**
      * Set Federated AccessManager admin tools location.
      * @param famAdmLocation Directory were famadm tool is present.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMAdmLocation(String famAdmLocation) 
     throws AMTuneException {
         if (famAdmLocation != null &&
                 famAdmLocation.trim().length() > 0) {
             File famDir = new File(famAdmLocation);
             if (famDir.isDirectory()) {
                 this.famAdmLocation = famAdmLocation.trim();
             } else {
                 mWriter.write(famAdmLocation + " ");
                 mWriter.writeLocaleMsg("pt-not-valid-dir");
                 pLogger.log(Level.SEVERE, "setFAMAdmLocation",
                         "FAM Admin tools location is not valid Directory. " +
                         "Please check the value for the property " +
                         FAMADM_LOCATION);
                 throw new AMTuneException("Invalid FAM admin tools location");
             }
         } else {
            mWriter.writelnLocaleMsg("pt-inval-config");
             AMTuneUtil.printErrorMsg(FAMADM_LOCATION);
             pLogger.log(Level.SEVERE, "setFAMAdmLocation",
                     "Error setting FAM Admin Location. " +
                     "Please check the value for the property " +
                     FAMADM_LOCATION);
             throw new AMTuneException("Invalid value for " + FAMADM_LOCATION);
         }
     }
     
     public String getFAMAdmLocation() {
         return famAdmLocation;
     }
     
     /**
      * Sets FAM configuration directory location.
      * @param famConfigDir FAM configuration directory location.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMConfigDir(String famConfigDir) 
     throws AMTuneException {
         if (famConfigDir != null &&
                 famConfigDir.trim().length() > 0) {
             File famDir = new File(famConfigDir);
             if (famDir.isDirectory()) {
                 this.famConfigDir = famConfigDir.trim();
             } else {
                 mWriter.write(famConfigDir + " ");
                 mWriter.writeLocaleMsg("pt-not-valid-dir");
                
                 pLogger.log(Level.SEVERE, "setFAMAdmLocation",
                         "FAM config is not valid Directory. " +
                         "Please check the value for the property " +
                         FAM_CONFIG_DIR);
                 throw new AMTuneException("Invalid FAM install location");
             }
         } else {
            mWriter.writelnLocaleMsg("pt-inval-config");
             AMTuneUtil.printErrorMsg(FAM_CONFIG_DIR);
             pLogger.log(Level.SEVERE, "setFAMAdmLocation",
                     "Error setting FAM config Location. " +
                     "Please check the value for the property " +
                     FAM_CONFIG_DIR);
             throw new AMTuneException("Invalid value for " + FAM_CONFIG_DIR);
         }
     }
     
     public String getFAMConfigDir() {
         return famConfigDir;
     }
     
     /**
      * Set FAM server URL
      * @param famServerUrl FAM server url.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMServerUrl(String famServerUrl) 
     throws AMTuneException {
         if (famServerUrl != null && famServerUrl.trim().length() > 0) {
             this.famServerUrl = famServerUrl;
         } else {
             mWriter.writelnLocaleMsg("pt-fam-server-url-not-found");
             AMTuneUtil.printErrorMsg(FAMSERVER_URL);
             pLogger.log(Level.SEVERE, "setFAMServerUrl", 
                     "Error setting FAM Server URL. " +
                     "Please check the value for the property " + 
                     FAMSERVER_URL);
             throw new AMTuneException("Invalid value for " + FAMSERVER_URL);
         }
     }
     
     public String getFAMServerUrl() {
         return famServerUrl;
     }
     
     /**
      * Set FAM Administrator User.
      * @param famAdmUser Administrator user name.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMAdmUser(String famAdmUser) 
     throws AMTuneException {
         if (famAdmUser != null && famAdmUser.trim().length() > 0) {
             this.famAdmUser = famAdmUser;
         } else {
             mWriter.writelnLocaleMsg("pt-fam-admin-user-not-found");
             AMTuneUtil.printErrorMsg(FAMADM_USER);
             pLogger.log(Level.SEVERE, "setFAMServerUrl", 
                     "Error setting FAM Admin User. " +
                     "Please check the value for the property " + 
                     FAMADM_USER);
             throw new AMTuneException("Invalid value for " + FAMADM_USER);
         }
     }
     
     public String getFAMAdmUser() {
         return famAdmUser;
     }
     
     /**
      * Set default organization people container.
      * @param defaultOrgPeopleContainer
      */
     private void setDefaultOrgPeopleContainer(
             String defaultOrgPeopleContainer) {
         this.defaultOrgPeopleContainer = defaultOrgPeopleContainer;
     }
     
     public String getDefaultOrgPeopleContainer() {
         return defaultOrgPeopleContainer;
     }
     
     /**
      * Percentage memory to Use.
      * @param famTunePctMemoryToUse
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void setFAMTunePctMemoryToUse(String famTunePctMemoryToUse) 
     throws AMTuneException {
         try {
             this.famTunePctMemoryToUse = 
                     Integer.parseInt(famTunePctMemoryToUse.trim());
             if (this.famTunePctMemoryToUse > 100 ) {
                 pLogger.log(Level.WARNING, "setFAMTunePctMemoryToUse", 
                     AMTUNE_PCT_MEMORY_TO_USE + " value is > 100 so using " +
                     "default value 100.");
                 this.famTunePctMemoryToUse = 100;
             } else if (this.famTunePctMemoryToUse < 0) {
                 pLogger.log(Level.WARNING, "setFAMTunePctMemoryToUse", 
                     AMTUNE_PCT_MEMORY_TO_USE + " value is < 0 so using " +
                     "default value 0.");
                 this.famTunePctMemoryToUse = 0;
             }
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_PCT_MEMORY_TO_USE);
             pLogger.log(Level.SEVERE, "setFAMTunePctMemoryToUse", 
                     "Error setting % memory to use, make sure the value " +
                     "for "+ AMTUNE_PCT_MEMORY_TO_USE +" is valid Integer.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     private int getFAMTunePctMemoryToUse() {
         return famTunePctMemoryToUse;
     }
     
     /**
      * Per Thread Stack size in Kilo bytes.
      * @param famTunePerThreadStackSizeInKB
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTunePerThreadStackSizeInKB(
             String famTunePerThreadStackSizeInKB)
     throws AMTuneException {
         try {
             this.famTunePerThreadStackSizeInKB =
                     Integer.parseInt(famTunePerThreadStackSizeInKB.trim());
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_PER_THREAD_STACK_SIZE_IN_KB);
             pLogger.log(Level.SEVERE, "setFAMTunePerThreadStackSizeInKB",
                     "Error parsing value, make sure the value for " +
                     AMTUNE_PER_THREAD_STACK_SIZE_IN_KB + " is valid Integer. ");
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     public int getFAMTunePerThreadStackSizeInKB() {
         return famTunePerThreadStackSizeInKB;
     }
     
     /**
      * Per Thread Stack Size in kilo bytes for 64 JVM.
      * @param famTunePerThreadStackSizeInKB64Bit
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTunePerThreadStackSizeInKB64Bit(
             String famTunePerThreadStackSizeInKB64Bit)
     throws AMTuneException {
         try {
             this.famTunePerThreadStackSizeInKB64Bit =
                     Integer.parseInt(famTunePerThreadStackSizeInKB64Bit.trim());
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_PER_THREAD_STACK_SIZE_IN_KB_64_BIT);
             pLogger.log(Level.SEVERE, "setFAMTunePerThreadStackSizeInKB64Bit",
                     "Error setting value, make sure the value " +
                     "for " + AMTUNE_PER_THREAD_STACK_SIZE_IN_KB_64_BIT +
                     " is valid Integer. ");
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     public int getFAMTunePerThreadStackSizeInKB64Bit() {
         return famTunePerThreadStackSizeInKB64Bit;
     }
     
     /**
      * Set value for session parameters.
      * @param amTuneDontTouchSessionParameters
      */
     private void setFAMTuneDontTouchSessionParameters(
             String amTuneDontTouchSessionParameters) {
             this.famTuneDontTouchSessionParameters = 
             Boolean.parseBoolean(amTuneDontTouchSessionParameters);
     }
     
     public boolean getFAMTuneDontTouchSessionParameters() {
         return famTuneDontTouchSessionParameters;
     }
     
     /**
      * Set Max session time in minutes.
      * @param famTuneSessionMaxSessionTimeInMts
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneSessionMaxSessionTimeInMts(
             String famTuneSessionMaxSessionTimeInMts) 
     throws AMTuneException {
         try {
             if (!getFAMTuneDontTouchSessionParameters() && 
                     famTuneSessionMaxSessionTimeInMts == null) {
                 this.famTuneSessionMaxSessionTimeInMts = 60;
             } else {
                 this.famTuneSessionMaxSessionTimeInMts = 
                         Integer.parseInt(
                         famTuneSessionMaxSessionTimeInMts.trim());
             }
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_SESSION_MAX_SESSION_TIME_IN_MTS);
             pLogger.log(Level.SEVERE, "setFAMTuneSessionMaxSessionTimeInMts",
                     "Error setting value, make sure the value for " +
                     AMTUNE_SESSION_MAX_SESSION_TIME_IN_MTS +
                     " is valid Integer.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     public int getFAMTuneSessionMaxSessionTimeInMts() {
         return famTuneSessionMaxSessionTimeInMts;
     }
     
     /**
      * Set Session Max ideal time in Minutes
      * @param famTuneSessionMaxIdleTimeInMts
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneSessionMaxIdleTimeInMts(
             String famTuneSessionMaxIdleTimeInMts) 
     throws AMTuneException {
         try {
             if (!getFAMTuneDontTouchSessionParameters() &&
                     famTuneSessionMaxIdleTimeInMts == null) {
                 this.famTuneSessionMaxIdleTimeInMts = 10;
             } else {
                 this.famTuneSessionMaxIdleTimeInMts =
                         Integer.parseInt(famTuneSessionMaxIdleTimeInMts.trim());
             }
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_SESSION_MAX_IDLE_TIME_IN_MTS);
             pLogger.log(Level.SEVERE, "setFAMTuneSessionMaxIdleTimeInMts",
                     "Error setting value, make sure the value for " +
                     AMTUNE_SESSION_MAX_IDLE_TIME_IN_MTS + " is valid Integer.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     public int getFAMTuneSessionMaxIdleTimeInMts() {
         return famTuneSessionMaxIdleTimeInMts;
     }
     
     /**
      * Set session max caching time in minutes
      * @param famTuneSessionMaxCachingTimeInMts
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneSessionMaxCachingTimeInMts(
             String famTuneSessionMaxCachingTimeInMts) 
     throws AMTuneException {
         try {
             if (!getFAMTuneDontTouchSessionParameters() &&
                     famTuneSessionMaxCachingTimeInMts == null) {
                 this.famTuneSessionMaxCachingTimeInMts = 2;
             } else {
                 this.famTuneSessionMaxCachingTimeInMts =
                         Integer.parseInt(
                         famTuneSessionMaxCachingTimeInMts.trim());
             }
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_SESSION_MAX_CACHING_TIME_IN_MTS);
             pLogger.log(Level.SEVERE, "setFAMTuneSessionMaxCachingTimeInMts",
                     "Error setting value, make sure the value for " +
                     AMTUNE_SESSION_MAX_CACHING_TIME_IN_MTS +
                     " is valid Integer.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     public int getFAMTuneSessionMaxCachingTimeInMts() {
         return famTuneSessionMaxCachingTimeInMts;
     }
     
     /**
      * Set Maximum heap size ratio.
      * @param famTuneMemMaxHeapSizeRatio
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneMemMaxHeapSizeRatio(
             String famTuneMemMaxHeapSizeRatio) 
     throws AMTuneException {
         try {
             this.famTuneMemMaxHeapSizeRatio = 
                     AMTuneUtil.evaluteDivExp(
                     famTuneMemMaxHeapSizeRatio.trim());
             this.famTuneMemMaxHeapSizeRatioExp = famTuneMemMaxHeapSizeRatio;
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_MEM_MAX_HEAP_SIZE_RATIO);
             pLogger.log(Level.SEVERE, "setFAMTuneMemMaxHeapSizeRatio",
                     "Error setting vlaue, make sure the value for " +
                     AMTUNE_MEM_MAX_HEAP_SIZE_RATIO + " is valid expression.");
             throw new AMTuneException(ex.getMessage());
         }
     }
             
     public double getFAMTuneMemMaxHeapSizeRatio() {
         return famTuneMemMaxHeapSizeRatio;
     }
     
     public String getFAMTuneMemMaxHeapSizeRatioExp() {
         return famTuneMemMaxHeapSizeRatioExp;
     }
     
     /**
      * Set Minimum heap size ratio.
      * @param famTuneMemMinHeapSizeRatio
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneMemMinHeapSizeRatio(
             String famTuneMemMinHeapSizeRatio) 
     throws AMTuneException {
         try {
             this.famTuneMemMinHeapSizeRatio = 
                     AMTuneUtil.evaluteDivExp(famTuneMemMinHeapSizeRatio);
         } catch (Exception ex) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_MEM_MIN_HEAP_SIZE_RATIO);
             pLogger.log(Level.SEVERE, "setFAMTuneMemMinHeapSizeRatio",
                     "Error setting value, make sure the value for " +
                     AMTUNE_MEM_MIN_HEAP_SIZE_RATIO + " is valid expresseion.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     public double getFAMTuneMemMinHeapSizeRation() {
         return famTuneMemMinHeapSizeRatio;
     }
     /**
      * Set Minimum memory to use in MB
      * @param famTuneMinMemoryToUseInMB
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneMinMemoryToUseInMB(String famTuneMinMemoryToUseInMB) 
     throws AMTuneException {
         try {
             this.famTuneMinMemoryToUseInMB = 
                     Integer.parseInt(famTuneMinMemoryToUseInMB);
         } catch (Exception exp) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_MIN_MEMORY_TO_USE_IN_MB);
             pLogger.log(Level.SEVERE, "setFAMTuneMinMemoryToUseInMB",
                     "Error setting value, make sure the value for " +
                     AMTUNE_MIN_MEMORY_TO_USE_IN_MB + " is valid integer.");
             throw new AMTuneException(exp.getMessage());
         }
     }
     
     private void setFAMTuneMaxMemoryToUseInMB() 
     throws Exception {
         if (AMTuneUtil.isLinux() || AMTuneUtil.isSunOs()) {
             if (getWebContainer().equals(WS7_CONTAINER)) {
                 if (AMTuneUtil.isLinux()) {
                     setFAMTuneMaxMemoryToUseInMB(
                             confBundle.getString(
                             AMTUNE_MAX_MEMORY_TO_USE_IN_MB_X86));
                 } else {
                     setFAMTuneMaxMemoryToUseInMB(
                             confBundle.getString(
                             AMTUNE_MAX_MEMORY_TO_USE_IN_MB_SOLARIS));
                 }
             } else {
                 if (AMTuneUtil.getOSPlatform().indexOf("sparc") == -1) {
                     setFAMTuneMaxMemoryToUseInMB(
                             confBundle.getString(
                             AMTUNE_MAX_MEMORY_TO_USE_IN_MB_X86));
                 } else {
                     setFAMTuneMaxMemoryToUseInMB(
                             confBundle.getString(
                             AMTUNE_MAX_MEMORY_TO_USE_IN_MB_SOLARIS));
                 }
             }
         } else if (AMTuneUtil.isWindows2003()) {
             setFAMTuneMaxMemoryToUseInMB(                   
                     Integer.toString(getFAMTuneMaxMemoryToUseInMBDefault()));
         }
     }
     
     public int getFAMTuneMinMemoryToUseInMB() {
         return famTuneMinMemoryToUseInMB;
     }
     
     /**
      * Set Maximum memory to user in MB
      * @param famTuneMaxMemoryToUseInMB
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneMaxMemoryToUseInMB (
             String famTuneMaxMemoryToUseInMB) 
     throws AMTuneException {
         try {
             this.famTuneMaxMemoryToUseInMB = 
                     Integer.parseInt(famTuneMaxMemoryToUseInMB);
         } catch (Exception exp) {
             pLogger.log(Level.SEVERE, "setFAMTuneMaxMemoryToUseInMb",
                     "Error setting value, is valid integer.");
             throw new AMTuneException(exp.getMessage());
         }
     }
     
     public int getFAMTuneMaxMemoryToUseInMB() {
         return famTuneMaxMemoryToUseInMB;
     }
     
     /**
      * Set Default maximum memory to be used.
      * @param famTuneMaxMemoryToUseInMBDefault
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMTuneMaxMemoryToUseInMBDefault 
             (String famTuneMaxMemoryToUseInMBDefault) 
     throws AMTuneException {
         try {
             this.famTuneMaxMemoryToUseInMBDefault = 
                     Integer.parseInt(famTuneMaxMemoryToUseInMBDefault);
         } catch (Exception exp) {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(AMTUNE_MAX_MEMORY_TO_USE_IN_MB_DEFAULT);
             pLogger.log(Level.SEVERE, "setFAMTuneMaxMemoryToUseInMbDefault",
                     "Error setting value, make sure the value for " +
                     AMTUNE_MAX_MEMORY_TO_USE_IN_MB_DEFAULT +
                     " is valid integer.");
             throw new AMTuneException(exp.getMessage());
         }
     }
     
     public int getFAMTuneMaxMemoryToUseInMBDefault () {
         return famTuneMaxMemoryToUseInMBDefault;
     }
     
     /**
      * Set FAM admin Password.
      * @param famAdminPassword
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFamAdminPassword(String famAdminPassword) 
     throws AMTuneException {
         if (famAdminPassword != null && famAdminPassword.trim().length() > 0) {
             this.famAdminPassword = famAdminPassword.trim();
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(FAMADM_PASSWORD);
             pLogger.log(Level.SEVERE, "setFamAdminPassword",
                     "Error setting FAM Administrator Password. " +
                     "Please check the value for the property " +
                     FAMADM_PASSWORD);
             throw new AMTuneException("Invalid value for " + FAMADM_PASSWORD);
         }
     }
     
     public String getFamAdminPassword() {
         return famAdminPassword;
     }
     
     
     
     /**
      * Set Perl bin Directory.
      * @param perlBinDir
      */
     private void setPerlBinDir(String perlBinDir) {
         this.perlBinDir = perlBinDir;
     }
     
     public String getPerlBinDir() {
         return perlBinDir;
     }
     
     
     /**
      * This method calculates required tuning parameters based on the 
      * system memory available.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void calculateTuneParams() 
     throws AMTuneException {
         try {
             mWriter.writeln(LINE_SEP);
             mWriter.write("FAM tune ");
             mWriter.writelnLocaleMsg("pt-conf-info");
             mWriter.writeln(LINE_SEP);
             if (isReviewMode) {
                 mWriter.writelnLocaleMsg("pt-review-msg");
             } else {
                 mWriter.writelnLocaleMsg("pt-change-msg");
             }
             mWriter.writeLocaleMsg("pt-os-msg");
             mWriter.writeln(tuneOS + " ");
             mWriter.writeLocaleMsg("pt-fam-msg");
             mWriter.writeln(tuneFAM + " ");
             mWriter.writeLocaleMsg("pt-ds-msg");
             mWriter.writeln(tuneDS + " ");
             mWriter.writeLocaleMsg("pt-web-msg");
             mWriter.writeln(tuneWebContainer + " ");
             if (webContainer.equals(WS7_CONTAINER) || 
                     webContainer.equals(WS61_CONTAINER)) {
                 if (isJVM64BitAvailable) {
                     mWriter.writelnLocaleMsg("pt-ws-64-msg");
                 } else {
                     mWriter.writelnLocaleMsg("pt-ws-32-msg");
                 }
             }
             mWriter.writeln(LINE_SEP);
             mWriter.writelnLocaleMsg("pt-conf-detecting");
             mWriter.writeln(LINE_SEP);
             numCpus = Integer.parseInt(AMTuneUtil.getNumberOfCPUS());
             mWriter.writeLocaleMsg("pt-no-cpu");
             mWriter.writeln(numCpus + " ");
             gcThreads = numCpus;
             acceptorThreads = numCpus;
             mWriter.writeLocaleMsg("pt-ws-acceptor-msg");
             mWriter.writeln(acceptorThreads + " ");
             memAvail = Integer.parseInt(AMTuneUtil.getSystemMemory());
             mWriter.writeLocaleMsg("pt-mem-avail-msg");
             mWriter.writeln(memAvail + " ");
             //if (!webContainer.equals(WS7_CONTAINER)) {
             //    setFAMTuneMaxMemoryToUseInMB(Integer.toString(
             //           getFAMTuneMaxMemoryToUseInMBDefault()));
             //}
             memToUse = (int) (memAvail * getFAMTunePctMemoryToUse() / 100);
             if ((memToUse > famTuneMaxMemoryToUseInMB) && 
                     !isJVM64BitAvailable) {
                 memToUse = famTuneMaxMemoryToUseInMB;
             }
             mWriter.writeLocaleMsg("pt-mem-to-use-msg");
             mWriter.writeln(memToUse + " ");
             if (memToUse == 0) {
                 mWriter.writeLocaleMsg("pt-unable-mem-req");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Error computing memory " +
                         "requirements");
             }
 
             if (memToUse >= getFAMTuneMinMemoryToUseInMB()) {
                 mWriter.writelnLocaleMsg("pt-enough-mem");
             } else {
                 mWriter.writelnLocaleMsg("pt-no-enough-mem");
                 throw new AMTuneException("Not Enough memory.");
             }
             mWriter.writeln(LINE_SEP);
             mWriter.writelnLocaleMsg("pt-conf-calc-tune-params");
             mWriter.writeln(LINE_SEP);
             maxHeapSize = 
                     (int)((double) memToUse * getFAMTuneMemMaxHeapSizeRatio());
             mWriter.writeLocaleMsg("pt-max-heap-size-msg");
             mWriter.writeln(maxHeapSize + " ");
             minHeapSize = maxHeapSize;
             mWriter.writeLocaleMsg("pt-min-heap-size-msg");
             mWriter.writeln(minHeapSize + " ");
 
             maxNewSize = (int) ((double) maxHeapSize * AMTUNE_MEM_MAX_NEW_SIZE);
             mWriter.writeLocaleMsg("pt-max-new-size-msg");
             mWriter.writeln(maxNewSize + " ");
 
             if (getWebContainer().equals(WS61_CONTAINER)) {
                 maxPermSize =
                         (int) ((double) maxHeapSize * AMTUNE_MEM_MAX_PERM_SIZE);
                 mWriter.writeLocaleMsg("pt-max-perm-size-msg");
                 mWriter.writeln(maxPermSize + " ");
             }
             cacheSize = (int) ((double) maxHeapSize * AMTUNE_MEM_CACHES_SIZE);
             mWriter.writeLocaleMsg("pt-cache-size-msg"); 
             mWriter.writeln(cacheSize + " ");
 
             sdkCacheSize =
                     (int) ((double) cacheSize * AMTUNE_MEM_SDK_CACHE_SIZE);
             mWriter.writeLocaleMsg("pt-sdk-cache-size-msg");
             mWriter.writeln(sdkCacheSize + " ");
             numSDKCacheEntries = (int) ((double) sdkCacheSize * 1024.0 /
                     AMTUNE_AVG_PER_ENTRY_CACHE_SIZE_IN_KB);
             mWriter.writeLocaleMsg("pt-no-sdk-cache-ent-msg");
             mWriter.writeln(numSDKCacheEntries + " ");
             sessionCacheSize =
                     (int) ((double) cacheSize * AMTUNE_MEM_SESSION_CACHE_SIZE);
             mWriter.writeLocaleMsg("pt-session-cache-size-msg");
             mWriter.writeln(sessionCacheSize + " ");
             numSessions = (int) ((double) sessionCacheSize * 1024.0 /
                     AMTUNE_AVG_PER_SESSION_SIZE_IN_KB);
             mWriter.writeLocaleMsg("pt-no-session-cache-ent-msg");
             mWriter.writeln(numSessions + " ");
             //AMTUNE_MAX_NUM_THREADS="$AMTUNE_MEM_THREADS_SIZE*
             //(1024/$AMTUNE_PER_THREAD_STACK_SIZE_IN_KB)"
             amTuneMaxNoThreads = (AMTUNE_MEM_THREADS_SIZE *
                     (1024.0 / (double) getFAMTunePerThreadStackSizeInKB()));
             amTuneMaxNoThreads64Bit = (AMTUNE_MEM_THREADS_SIZE *
                     (1024.0 / 
                     (double) getFAMTunePerThreadStackSizeInKB64Bit()));
             maxThreads = 0;
             if (isJVM64BitAvailable) {
                 maxThreads = 
                         (int)(amTuneMaxNoThreads64Bit * (double) maxHeapSize);
             } else {
                 maxThreads = (int)(amTuneMaxNoThreads * (double) maxHeapSize);
             }
             mWriter.writeLocaleMsg("pt-max-java-threads-msg"); 
             mWriter.writeln(maxThreads + " ");
             numRQThrottle =
                     (int) ((double) maxThreads * AMTUNE_WS_RQTHROTTLE_THREADS);
             numOfMaxThreadPool = numRQThrottle;
             if (getWebContainer().equals(WS61_CONTAINER)) {
                 mWriter.writeLocaleMsg("pt-rq-thro-msg");
                 mWriter.writeln(numRQThrottle + " ");
             } else {
                 mWriter.writeLocaleMsg("pt-max-thread-pool-msg");
                 mWriter.writeln(numOfMaxThreadPool + " ");
             }
 
             numLdapAuthThreads =
                     (int) ((double) maxThreads * AMTUNE_IS_AUTH_LDAP_THREADS);
             mWriter.writeLocaleMsg("pt-ldap-auth-threads-msg");
             mWriter.writeln(numLdapAuthThreads + " ");
 
             numSMLdapThreads =
                     (int) ((double) maxThreads * AMTUNE_IS_SM_LDAP_THREADS);
             mWriter.writeLocaleMsg("pt-sm-ldap-threads-msg");
             mWriter.writeln(numSMLdapThreads + " ");
 
             numNotificationThreads = numCpus * 3;
             mWriter.writeLocaleMsg("pt-notification-threads-msg");
             mWriter.writeln(numNotificationThreads + " ");
             numNotificationQueue =
                     (int) (AMTUNE_NOTIFICATION_QUEUE_CALC_FACTOR *
                     (double) numSessions);
             numNotificationQueue = (numNotificationQueue / (10 ^ 1)) * (10 ^ 1);
             mWriter.writeLocaleMsg("pt-notification-queue-size-msg");
             mWriter.writeln(numNotificationQueue + " ");
             mWriter.writeln(PARA_SEP);
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "calculateTuneParams",
                     "Error while caliculating tuning parameters.");
             pLogger.logException("calculateTuneParams", ex);
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     /**
      * Return number of acceptor threads.
      * @return Acceptor threads.
      */
     public int getAcceptorThreads() {
         return acceptorThreads;
     }
     
     /**
      * Return maximum number of threads.
      * @return number of threads.
      */
     public double getAmTuneMaxNoThreads() {
         return amTuneMaxNoThreads;
     }
     
     /**
      * Return max number of threads in 64 bit machine
      * @return number of threads.
      */
     public double getAmTuneMaxNoThreads64Bit() {
         return amTuneMaxNoThreads64Bit;
     }
     
     /**
      * Return cache size.
      * @return cache size.
      */
     public int getCacheSize() {
         return cacheSize;
     }
     
     /**
      * Return number of Garbage collection threads.
      * @return Number of GC threads.
      */
     public int getGcThreads() {
         return gcThreads;
     }
     
     /**
      * Return Maximum heap size that can be set.
      * @return maximum heap size.
      */
     public int getMaxHeapSize() {
         return maxHeapSize;
     }
     
     /**
      * Return max new size.
      * @return max new size.
      */
     public int getMaxNewSize() {
         return maxNewSize;
     }
     
     /**
      * Return max Perm Size.
      * @return max Perm Size.
      */
     public int getMaxPermSize() {
         return maxPermSize;
     }
     
     /**
      * Return Max Threads.
      * @return Maximum no of threads.
      */
     public int getMaxThreads() {
         return maxThreads;
     }
     
     /**
      * Return available memory.
      * @return available memory.
      */
     public int getMemAvail() {
         return memAvail;
     }
     
     /**
      * Return Memory to be used.
      * @return Memory to be used.
      */
     public int getMemToUse() {
         return memToUse;
     }
     
     /**
      * Return Minimum heap size.
      * @return Heap size.
      */
     public int getMinHeapSize() {
         return minHeapSize;
     }
     
     /**
      * Return number of CPU's in the system.
      * @return number of cpu's
      */
     public int getNumCpus() {
         return numCpus;
     }
     
     /**
      * Return number of LDAP Auth threads to be used.
      * @return number of LDAP auth threads.
      */
     public int getNumLdapAuthThreads() {
         return numLdapAuthThreads;
     }
     
     /**
      * Return Notification queue size.
      * @return Notification queue size.
      */
     public int getNumNotificationQueue() {
         return numNotificationQueue;
     }
     
     /**
      * Return number of Notification threads to be used.
      * @return Number of Notification threads
      */
     public int getNumNotificationThreads() {
         return numNotificationThreads;
     }
     
     /**
      * Return number of MaxThreads Pool
      * @return Number of MaxThreads Pool
      */
     public int getNumOfMaxThreadPool() {
         return numOfMaxThreadPool;
     }
     
     /**
      * Return RQThrottle
      * @return RQThrottle
      */
     public int getNumRQThrottle() {
         return numRQThrottle;
     }
     
     /**
      * Return number of SDK cache entries.
      * @return Number of SDK cache entries.
      */
     public int getNumSDKCacheEntries() {
         return numSDKCacheEntries;
     }
     
     /**
      * Return number of SMLDAP threads to be used.
      * @return Number of SMLDAP thread to be used.
      */
     public int getNumSMLdapThreads() {
         return numSMLdapThreads;
     }
     
     /**
      * Return number of sessions.
      * @return Number of sessions.
      */
     public int getNumSessions() {
         return numSessions;
     }
     
     /**
      * Return SDK cache size.
      * @return SDK cache size.
      */
     public int getSdkCacheSize() {
         return sdkCacheSize;
     }
     
     /**
      * Return Session cache size
      * @return Session cache size
      */
     public int getSessionCacheSize() {
         return sessionCacheSize;
     }
     
     
     /**
      * Return web container configuration object.
      * @return
      */
     public WebContainerConfigInfoBase getWSConfigInfo() {
         return webConfigInfo;
     }
     
     /**
      * Set to true if datastore for UM and SM are same.
      */
     private void setUMSMDSSame(String isUMSMDSSame) 
     throws AMTuneException {
         if (isUMSMDSSame != null && isUMSMDSSame.trim().length() > 0) {
             this.isUMSMDSSame = Boolean.parseBoolean(isUMSMDSSame);
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(IS_UM_SM_DATASTORE_SAME);
             pLogger.log(Level.SEVERE, "setUMSMDSSame", 
                     "Please check the value for the property " + 
                     IS_UM_SM_DATASTORE_SAME);
             throw new AMTuneException("Invalid value for " + 
                     IS_UM_SM_DATASTORE_SAME);
         }
     }
     
     public boolean isUMSMDSSame() {
         return isUMSMDSSame;
     }
     
     /**
      * Set to true if only UM need to be tuned.
      */
     
     private void setTuneUMOnly(String tuneUMOnly) 
     throws AMTuneException {
         if (tuneUMOnly != null && tuneUMOnly.trim().length() > 0) {
             this.tuneUMOnly = Boolean.parseBoolean(tuneUMOnly);
         } else {
             mWriter.writeLocaleMsg("pt-inval-val-msg");
             AMTuneUtil.printErrorMsg(TUNE_UM_ONLY);
             pLogger.log(Level.SEVERE, "setTuneUMOnly", 
                     "Please check the value for the property " + 
                     TUNE_UM_ONLY);
             throw new AMTuneException("Invalid value for " + 
                     TUNE_UM_ONLY);
         }
     }
     
     public boolean isUMOnlyTune() {
         return tuneUMOnly;
     }
     
     public DSConfigInfo getDSConfigInfo() {
         return dsConfigInfo;
     }
     public DSConfigInfo getSMConfigInfo() {
         return smConfigInfo;
     }
     
 }
