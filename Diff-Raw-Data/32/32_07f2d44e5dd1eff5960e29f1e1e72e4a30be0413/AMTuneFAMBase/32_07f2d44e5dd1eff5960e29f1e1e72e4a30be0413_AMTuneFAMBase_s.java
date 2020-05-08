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
 * $Id: AMTuneFAMBase.java,v 1.1 2008-07-02 18:44:02 kanduls Exp $
  */
 
 package com.sun.identity.tune.base;
 
 import com.sun.identity.tune.common.MessageWriter;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.common.AMTuneLogger;
 import com.sun.identity.tune.config.AMTuneConfigInfo;
 import com.sun.identity.tune.intr.TuneFAM;
 import com.sun.identity.tune.util.AMTuneUtil;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 
 /**
  * Base class for tuning Federated Access Manager.
  *
  */
 public abstract class AMTuneFAMBase extends TuneFAM {
     private String famPassFilePath;
     protected String famCmdPath;
     protected AMTuneConfigInfo configInfo;
     protected AMTuneLogger pLogger;
     protected MessageWriter mWriter;
     protected String famadmCommonParamsNoServer;
 
     /**
      * This method initializes the Performance tuning configuration information.
      *
      * @param configInfo Instance of AMTuneConfigInfo class
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void initialize(AMTuneConfigInfo configInfo)
     throws AMTuneException {
         this.configInfo = configInfo;
         famPassFilePath = AMTuneUtil.TMP_DIR + "fampassfile";
         pLogger = AMTuneLogger.getLoggerInst();
         mWriter = MessageWriter.getInstance();
         setFAMAdmCmd();
         famadmCommonParamsNoServer = " --adminid " +
                 configInfo.getFAMAdmUser() + " --password-file " +
                 famPassFilePath;
         writePasswordToFile();
     }
     
     /**
      * Set famadm cmd based on platform.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void setFAMAdmCmd() 
     throws AMTuneException {
         if (AMTuneUtil.isWindows2003()) {
             famCmdPath = configInfo.getFAMAdmLocation() + FILE_SEP +
                     "famadm.bat ";
         } else {
             famCmdPath = configInfo.getFAMAdmLocation() + FILE_SEP +
                     "famadm ";
         }
         File famAdmF = new File(famCmdPath.trim());
         if (!famAdmF.isFile()) {
             mWriter.writelnLocaleMsg("pt-fam-tool-not-found");
             mWriter.writelnLocaleMsg("pt-cannot-proceed");
             mWriter.writeLocaleMsg("pt-conf-parm-cust-msg");
             mWriter.writeln(FAMADM_LOCATION);
         }
     }
     
     /**
      * Writes password to file.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void writePasswordToFile ()
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "writePasswordToFile", "Creating FAM " +
                     "password file.");
             File passFile = new File(famPassFilePath);
             BufferedWriter pOut = new BufferedWriter(new FileWriter(passFile));
             pOut.write(configInfo.getFamAdminPassword());
             pOut.flush();
             pOut.close();
             if (!AMTuneUtil.isWindows2003()) {
                 String chmodCmd = "/bin/chmod 400 " + famPassFilePath;
                 StringBuffer rbuff = new StringBuffer();
                 int ext = AMTuneUtil.executeCommand(chmodCmd, rbuff);
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "writePassWordToFile",
                     "Couldn't write password to file. ");
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     /**
      * Deletes password file.
      */
     protected void deletePasswordFile() {
         if (!AMTuneUtil.isWindows2003()) {
             String chmodCmd = "/bin/chmod 700 " + famPassFilePath;
             StringBuffer rbuff = new StringBuffer();
             int ext = AMTuneUtil.executeCommand(chmodCmd, rbuff);
         }
         File passFile = new File(famPassFilePath);
         passFile.delete();
     }
 
     /**
      * Updates Service config
      * @param args List of properties to be updated.
      */
     protected void updateFAMServiceCfg(List attrs) {
         StringBuffer resultBuffer = new StringBuffer();
         try {
             StringBuffer updateCmd = new StringBuffer(famCmdPath);
             updateCmd.append(UPDATE_SERVER_SUB_CMD);
             updateCmd.append(famadmCommonParamsNoServer);
             updateCmd.append(" ");
             updateCmd.append(FAMADM_SERVER);
             updateCmd.append(" ");
             updateCmd.append(configInfo.getFAMServerUrl());
             updateCmd.append(" ");
             updateCmd.append(ATTR_VALUES_OPT);
             if (!AMTuneUtil.isWindows2003()) {
                 updateCmd.append(" ");
             } else {
                 updateCmd.append(" \"");
             }
             int retVal;
             Iterator itr = attrs.iterator();
             while(itr.hasNext()) {
                 String args = itr.next().toString();
                 String cmd = updateCmd.toString() + args;
                 if (AMTuneUtil.isWindows2003()) {
                     cmd = cmd + "\"";
                 }
                 pLogger.log(Level.FINEST, "updateFAMServiceCfg", 
                         "Executing cmd " + cmd);
                 if (!AMTuneUtil.isWindows2003()) {
                     try {
                         retVal = AMTuneUtil.executeScriptCmd(cmd, resultBuffer);
                     } catch (AMTuneException ex) {
                         retVal = -1;
                     }
                 } else {
                     retVal = AMTuneUtil.executeCommand(cmd, resultBuffer);
                 }
                 if (retVal == -1) {
                     pLogger.log(Level.SEVERE, "updateFAMServiceCfg", 
                             "Error updating fam service config values.");
                     throw new AMTuneException(resultBuffer.toString());
                 }
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "updateFAMServiceCfg", "Error updating " +
                     "FAM Server configuration. " + ex.getMessage());
         }
     }
     
     /**
      * Returns the list of datastores for the realmName
      * @param realmName Name of the realm.
      * @return DataStore names in the form of List.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected List getDataStoreList(String realmName) 
     throws AMTuneException {
         List dataStoreList = new ArrayList();
         try {
             pLogger.log(Level.INFO, "getDataStoreList",
                     "Getting datastore list. ");
             StringBuffer dataStoreListCmd = 
                     new StringBuffer(famCmdPath);
             dataStoreListCmd.append(LIST_DATA_STORES_SUB_CMD);
             dataStoreListCmd.append(" -e ");
             dataStoreListCmd.append(realmName);
             dataStoreListCmd.append(famadmCommonParamsNoServer);
             StringBuffer rBuff = new StringBuffer();
             int extVal = AMTuneUtil.executeCommand(dataStoreListCmd.toString(), 
                     rBuff);
             if (extVal == -1) {
                     throw new AMTuneException("List data store cmd failed. ");
             }
             if (rBuff.indexOf("There were no datastores") != -1) {
                 pLogger.log(Level.SEVERE, "getDataStoreList",
                         "No datastore for the realm:" + realmName);
             } else {
                 String reqStr = rBuff.toString().trim().replace("Datastore:",
                         "");
                 StringTokenizer str = new StringTokenizer(reqStr, "\n");
                 while (str.hasMoreTokens()) {
                    dataStoreList.add(str.nextToken());
                 }
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "getDataStoreList",
                     "Error getting data store list. ");
             throw new AMTuneException(ex.getMessage());
         }
         pLogger.log(Level.FINEST, "getDataStoreList",
                     "Returning datastore list. " + dataStoreList.toString());
         return dataStoreList;
     }
     
     /**
      * This method queries the FAM server and returns the server configuration
      * in the form of Map.
      */
     protected Map getFAMServerConfig() {
         Map famCfgInfo = new HashMap();
         StringBuffer listSerCfgCmd = new StringBuffer(famCmdPath);
         listSerCfgCmd.append(LIST_SERVER_CFG_SUB_CMD);
         listSerCfgCmd.append(" ");
         listSerCfgCmd.append(FAMADM_SERVER);
         listSerCfgCmd.append(" ");
         listSerCfgCmd.append(configInfo.getFAMServerUrl());
         listSerCfgCmd.append(famadmCommonParamsNoServer);
         listSerCfgCmd.append(" -w");
         StringBuffer rBuff = new StringBuffer();
         int extVal = AMTuneUtil.executeCommand(listSerCfgCmd.toString(),
                 rBuff);
         if (extVal != -1) {
             StringTokenizer str =
                     new StringTokenizer(rBuff.toString(), "\n");
             while (str.hasMoreTokens()) {
                 String line = str.nextToken();
                 if (line != null && line.length() > 0) {
                     StringTokenizer lStr = new StringTokenizer(line, "=");
                     lStr.hasMoreTokens();
                     String key = lStr.nextToken();
                     if (lStr.hasMoreTokens()) {
                         String val = lStr.nextToken();
                         famCfgInfo.put(key, val);
                     } else {
                         famCfgInfo.put(key, "");
                     }
                 
                 }
             }
         } else {
             pLogger.log(Level.WARNING, "getFAMServerConfig",
                     "Error while getting server configuration.");
         }
         pLogger.log(Level.FINEST, "getFAMServerConfig",
                     "Returning FAM configuration Map " + famCfgInfo.toString());
         return famCfgInfo;
     }
 }
