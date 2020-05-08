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
 * $Id: AMTune.java,v 1.5 2008-08-12 05:10:55 kanduls Exp $
  */
 
 package com.sun.identity.tune;
 
 import com.sun.identity.tune.common.FileHandler;
 import com.sun.identity.tune.common.MessageWriter;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.common.AMTuneLogger;
 import com.sun.identity.tune.config.AMTuneConfigInfo;
 import com.sun.identity.tune.constants.DSConstants;
 import com.sun.identity.tune.constants.AMTuneConstants;
 import com.sun.identity.tune.constants.WebContainerConstants;
 import com.sun.identity.tune.impl.TuneAS9Container;
 import com.sun.identity.tune.impl.TuneDS5Impl;
 import com.sun.identity.tune.impl.TuneDS6Impl;
 import com.sun.identity.tune.impl.TuneFAM8Impl;
 import com.sun.identity.tune.impl.TuneLinuxOS;
 import com.sun.identity.tune.impl.TuneSolarisOS;
 import com.sun.identity.tune.impl.TuneWS7Container;
 import com.sun.identity.tune.intr.Tuning;
 import com.sun.identity.tune.util.AMTuneUtil;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 
 /**
  *
  * This is a main class which invokes DS, OpenSSO Enterprise and Web container 
  * tuners based on the options set in the amtune-env.properties.
  */
 public class AMTune {
         static AMTuneLogger pLogger = null;
         static MessageWriter mWriter = null;
     public static void main(String args[]) {
         AMTuneConfigInfo confInfo = null;
         try {
             String confFilePath = AMTuneConstants.ENV_FILE_NAME;
             pLogger = AMTuneLogger.getLoggerInst();
             mWriter = MessageWriter.getInstance();
             AMTuneLogger.setLogLevel("FINEST");
             mWriter.writeln(AMTuneConstants.PARA_SEP);
             mWriter.writeln("Debug log file : " + pLogger.getLogFilePath());
             mWriter.writeln("Configuration information file : " + 
                     mWriter.getConfigurationFilePath());
             mWriter.writeln(AMTuneConstants.PARA_SEP);
              //init utils
             AMTuneUtil.initializeUtil();
             if (args.length == 1) {
                 StringTokenizer st = new StringTokenizer(args[0], "=");
                 st.hasMoreTokens();
                 String opt = st.nextToken();
                 if (opt.indexOf(AMTuneConstants.CMD_OPTION2) != -1) {
                     st.hasMoreTokens();
                     AMTuneLogger.setLogLevel(st.nextToken());
                 }
             }
             confInfo = new AMTuneConfigInfo(confFilePath);
             List tunerList = getTuners(confInfo);
             Iterator itr = tunerList.iterator();
             while (itr.hasNext()) {
                 Tuning compTuner = (Tuning)itr.next();
                 compTuner.initialize(confInfo);
                 compTuner.startTuning();
             }
         } catch (Exception ex) {
             if (pLogger != null) {
                 pLogger.log(Level.SEVERE, "main", ex.getMessage());
                 pLogger.logException("main", ex);
             } else {
                 ex.printStackTrace();
             }
             if (mWriter != null) {
                 mWriter.writelnLocaleMsg("pt-error-tuning-msg");
             } else {
                 System.out.println("Error occured while tuning. " +
                         "Check logs for root cause.");
             }
         } finally {
            replacePasswords();
         }
     }
 
     /**
      * Factory method which creates component tuners for tuning.
      * @param confInfo
      * @return List of tuner objects.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private static List getTuners(AMTuneConfigInfo confInfo)
     throws AMTuneException {
         List tunerList = new ArrayList();
         if (confInfo.isTuneOS()) {
             if (AMTuneUtil.isSunOs()) {
                 tunerList.add(new TuneSolarisOS());
             } else if (AMTuneUtil.isLinux()) {
                 tunerList.add(new TuneLinuxOS());
             }  else {
                 throw new AMTuneException("Unsupported OS for tuning.");
             }
         }
         if (confInfo.isTuneDS()) {
             if (confInfo.getDSConfigInfo().isRemoteDS()) {
                 AMTuneUtil.createRemoteDSTuningZipFile(confInfo);
             } else {
                 String dsVersion = confInfo.getDSConfigInfo().getDsVersion();
                 if (AMTuneUtil.isSupportedUMDSVersion(dsVersion)) {
                     if (dsVersion.indexOf(DSConstants.DS5_VERSION) != -1) {
                         tunerList.add(new TuneDS5Impl(false));
                     } else if (dsVersion.indexOf(
                             DSConstants.DS63_VERSION) != -1) {
                         tunerList.add(new TuneDS6Impl(false));
                     }
                 } else {
                     throw new AMTuneException("Invalid UM DS Version: " +
                             dsVersion);
                 }
             }
             if (!confInfo.isUMSMDSSame() && !confInfo.isUMOnlyTune()) {
                 if (confInfo.getSMConfigInfo().isRemoteDS()) {
                     AMTuneUtil.createRemoteDSTuningZipFile(confInfo);
                 } else {
                     String smDSVersion =
                             confInfo.getSMConfigInfo().getDsVersion();
                     if (AMTuneUtil.isSupportedSMDSVersion(smDSVersion)) {
                         if (smDSVersion.indexOf(
                                 DSConstants.DS5_VERSION) != -1) {
                             tunerList.add(new TuneDS5Impl(true));
                         } else if (smDSVersion.indexOf(
                                 DSConstants.DS63_VERSION) != -1) {
                             tunerList.add(new TuneDS6Impl(true));
                         } else {
                             throw new AMTuneException(
                                     "Tuner not available for " + smDSVersion);
                         }
                     } else {
                         throw new AMTuneException("Invalid SM DS Version: " +
                                 smDSVersion);
                     }
                 }
             }
         }
         if (confInfo.isTuneWebContainer()) {
             if (confInfo.getWebContainer().equals(
                     WebContainerConstants.WS7_CONTAINER)) {
                 tunerList.add(new TuneWS7Container());
             } else if (confInfo.getWebContainer().equals(
                     WebContainerConstants.AS91_CONTAINER)) {
                 tunerList.add(new TuneAS9Container());
             } else {
                 throw new AMTuneException("Invalid WebContainer.");
             }
         }
         if (confInfo.isTuneFAM()) {
             tunerList.add(new TuneFAM8Impl());
         }
         return tunerList;
     }
     
    private static void replacePasswords() {
         try {
             String propFile = AMTuneUtil.getCurDir() + 
                     AMTuneConstants.FILE_SEP +
                     AMTuneConstants.ENV_FILE_NAME + ".properties";
             FileHandler fh = new FileHandler(propFile);
             int reqLine =
                    fh.getLineNum(AMTuneConstants.OPENSSOADMIN_PASSWORD + "=");
            fh.replaceLine(reqLine, AMTuneConstants.OPENSSOADMIN_PASSWORD + 
                    "=");
             reqLine = fh.getLineNum(AMTuneConstants.SM_DIRMGR_PASSWORD + "=");
             fh.replaceLine(reqLine, AMTuneConstants.SM_DIRMGR_PASSWORD + "=");
             reqLine =
                     fh.getLineNum(AMTuneConstants.WSADMIN_PASSWORD + "=");
             fh.replaceLine(reqLine, AMTuneConstants.WSADMIN_PASSWORD + "=");
             reqLine = fh.getLineNum(AMTuneConstants.ASADMIN_PASSWORD + "=");
             fh.replaceLine(reqLine, AMTuneConstants.ASADMIN_PASSWORD + "=");
             reqLine = fh.getLineNum("^" + AMTuneConstants.DIRMGR_PASSWORD + 
                     "=");
             fh.replaceLine(reqLine, AMTuneConstants.DIRMGR_PASSWORD + "=");
             fh.close();
         } catch (Exception ex) {
             System.out.println("Error in password replacement " +
                     ex.getMessage());
         }
     }
 }
