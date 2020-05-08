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
 * $Id: TuneDS6Impl.java,v 1.3 2008-07-25 05:49:59 kanduls Exp $
  */
 
 package com.sun.identity.tune.impl;
 
 import com.sun.identity.tune.base.AMTuneDSBase;
 import com.sun.identity.tune.common.FileHandler;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.config.AMTuneConfigInfo;
 import com.sun.identity.tune.util.AMTuneUtil;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 
 /**
  * <code>TuneDS6Impl<\code> extends the <code>AMTuneDSBase<\code> and tunes
  * the Directory server 6.0
  *
  */
 public class TuneDS6Impl extends AMTuneDSBase {
     private String dsAdmPath;
     private String dsConf;
     private String dbBackupDir;
     
     public TuneDS6Impl(boolean isSMStore) {
         super(isSMStore);
     }
     
     /**
      * Initializes the configuration information.
      *
      * @param confInfo Configuration information used for computing the tuning
      *   parameters for Directory server.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void initialize(AMTuneConfigInfo configInfo)
     throws AMTuneException {
         super.initialize(configInfo);
        if (AMTuneUtil.isWindows()) {
             dsAdmPath = dsConfInfo.getDSToolsBinDir() + FILE_SEP + "dsadm.exe ";
             dsConf = dsConfInfo.getDSToolsBinDir() + FILE_SEP + "dsconf.exe ";
         } else {
             dsAdmPath = dsConfInfo.getDSToolsBinDir() + FILE_SEP + "dsadm ";
             dsConf = dsConfInfo.getDSToolsBinDir() + FILE_SEP + "dsconf ";
         }
         dbBackupDir = DB_BACKUP_DIR_PREFIX + "-" + AMTuneUtil.getRandomStr();
         checkDSRealVersion();
     }
     
     private void checkDSRealVersion()
     throws AMTuneException {
         String verCmd = dsAdmPath + " --version";
         StringBuffer rBuff = new StringBuffer();
         int extVal = AMTuneUtil.executeCommand(verCmd, rBuff);
         if (extVal == -1) {
             mWriter.writelnLocaleMsg("pt-ds-version-fail-msg");
             mWriter.writelnLocaleMsg("pt-cannot-proceed");
             throw new AMTuneException("Error getting DS version.");
         } else {
             if (rBuff.toString().indexOf(DS63_VERSION) == -1 &&
                 rBuff.indexOf(DS62_VERSION) != -1) {
                 mWriter.writelnLocaleMsg("pt-ds-unsupported-msg");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Unsupported DS version.");
             }
         }
     }
     
     /**
      * This method performs the sequence of operations for tuning
      * Directory server 6.0.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void startTuning()
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "startTuning","Start tuning.");
             mWriter.writeln(CHAPTER_SEP);
             mWriter.writelnLocaleMsg("pt-fam-ds6-tuning");
             if (isSM) {
                 mWriter.writelnLocaleMsg("pt-fam-sm-ds-tuning");
             }
             mWriter.writeln(CHAPTER_SEP);
             mWriter.writelnLocaleMsg("pt-init");
             mWriter.writeln(LINE_SEP);
             computeTuneValues();
             mWriter.writeln(PARA_SEP);
             modifyLDAP();
            if ( ! AMTuneUtil.isWindows()) {
                 //For Windows changing dse is not recommended.
                 tuneUsingDSE();
             }
             if (!isSM) {
                 tuneDSIndex();
             }
             tuneFuture();
            mWriter.writelnLocaleMsg("pt-ds-um-mutliple-msg");
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "startTuning", "Error Tuning DSEE6.0");
             mWriter.writelnLocaleMsg("pt-error-tuning-msg");
             pLogger.logException("startTuning", ex);
         } finally {
             try {
                 releaseCon();
             } catch (Exception ex) {
             //ignore
             }
             deletePasswordFile();
         }
     }
 
     /**
      * This method modify the DB home location in dse.ldif to point to
      * new location.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private List getIndexForDB()
     throws AMTuneException {
         List idxList = new ArrayList();
         try {
             pLogger.log(Level.FINE, "getIndexForDB", "Get existing index.");
             String listIndexCmd = dsConf + "list-indexes --port " +
                     dsConfInfo.getDsPort() + " --unsecured --no-inter " +
                     "--pwd-file " + dsPassFilePath + " " +
                     dsConfInfo.getRootSuffix();
             StringBuffer resultBuffer = new StringBuffer();
             int retVal = AMTuneUtil.executeCommand(listIndexCmd,
                     resultBuffer);
             if (retVal == -1){
                 throw new AMTuneException(resultBuffer.toString());
             }
             StringTokenizer idxTokens =
                     new StringTokenizer(resultBuffer.toString(), " ");
             while(idxTokens.hasMoreTokens()) {
                 idxList.add(idxTokens.nextToken().trim());
             }
             pLogger.log(Level.FINE, "getIndexForDB", "Returning idx list " +
                     idxList.toString());
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "getIndexForDB", "Error finding index");
             throw new AMTuneException(ex.getMessage());
         }
        return idxList;
     }
 
     /**
      * This method finds the attributes that need to be indexed, if tuning mode
      * is set to "CHANGE" it creates the index for the required attributes.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void tuneDSIndex()
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "tuneDSIndex", "Tune DS Index.");
             List existingIdx = getIndexForDB();
             List notIdxList = tuneDSIndex(existingIdx);
             if (configInfo.isReviewMode()) {
                 return;
             }
             if (notIdxList.size() == 0) {
                 mWriter.writelnLocaleMsg("pt-all-idx-exist");
             } else {
                 mWriter.writeLocaleMsg("pt-creating-idx");
                 mWriter.writeln(notIdxList.toString());
                 Iterator idxListItr = notIdxList.iterator();
                 while(idxListItr.hasNext()) {
                     String curAttr = (String)idxListItr.next();
                     mWriter.writeLocaleMsg("pt-create-idx-attr");
                     mWriter.writeln(curAttr);
                     createIndex(curAttr);
                     mWriter.writelnLocaleMsg("pt-done");;
                 }
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "tuneDSIndex", "Error tuning index.");
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     /**
      * Creates index for the required attribute, creates new index dn for the
      * attribute and invokes dsconf create-index.
      *
      * @param attrName Name of the attribute to be indexed.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
 
     private void createIndex(String attrName)
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "createIndex", "Create index for " +
                     attrName);
             String createIndexCmd = dsConf + "create-index --port " +
                     dsConfInfo.getDsPort() + 
                     " --unsecured --no-inter --pwd-file " +
                     dsPassFilePath + " " +dsConfInfo.getRootSuffix() + " " +
                     attrName;
             StringBuffer resultBuffer = new StringBuffer();
             int retVal = AMTuneUtil.executeCommand(createIndexCmd,
                     resultBuffer);
             if (retVal == -1){
                 throw new AMTuneException(resultBuffer.toString());
             }
         } catch (Exception ex) {
             mWriter.writelnLocaleMsg("pt-idx-create-error");
             pLogger.log(Level.SEVERE, "createIndex", "Error creating index " +
                     "for attribute " + attrName);
         }
     }
 
     /**
      * This method modify the DB home location in dse.ldif to point to
      * new location.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void tuneUsingDSE()
     throws AMTuneException {
         try {
             super.tuneUsingDSE();
             pLogger.log(Level.FINE, "tuneUsingDSE", "");
             if (configInfo.isReviewMode()) {
                 return;
             }
             pLogger.log(Level.FINE, "tuneUsingDSE", "Modify dse.ldif");;
             if (curDBHomeLocation.equals(newDBHomeLocation)) {
                 pLogger.log(Level.INFO, "tuneUsingDSE",
                         "Current DB Location is " +
                         "same as recommended value.");
                 return;
             }
             stopDS();
             backUpDS();
             FileHandler dseH = new FileHandler(dseLdifPath);
             int reqLineNo = dseH.lineContains(NSSLAPD_DB_HOME_DIRECTORY + ":");
             dseH.replaceLine(reqLineNo, NSSLAPD_DB_HOME_DIRECTORY + ": " +
                     newDBHomeLocation);
             dseH.close();
             startDS();
         } catch (FileNotFoundException fex) {
             throw new AMTuneException(fex.getMessage());
         } catch (IOException ioe) {
             throw new AMTuneException(ioe.getMessage());
         }
     }
 
     /**
      * This method computes the recommended values for tuning LDAP and apply
      * the modifications to the LDAP.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void modifyLDAP()
     throws AMTuneException{
         pLogger.log(Level.FINE, "modifyLDAP", "Modify LDAP attributes.");
         boolean applyRec = false;
         boolean remAci = false;
         ldapTuningRecommendations();
         if(configInfo.isReviewMode()) {
             return;
         }
         stopDS();
         backUpDS();
         startDS();
         applyRec = applyRecommendations();
         if (applyRec || remAci) {
             stopDS();
             startDS();
         }
     }
 
     /**
      * Takes the backup of the DS 6.0 by invoking dsadm backup.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void  backUpDS()
     throws AMTuneException {
         try {
             String dbBackUpSuccessFile = instanceDir +
                     FILE_SEP + "bak" + FILE_SEP + dbBackupDir +
                     FILE_SEP + "SUCCESS.dontdelete";
             File successFile = new File(dbBackUpSuccessFile);
             if (successFile.isFile()) {
                 pLogger.log(Level.INFO, "backUpDS", "Backup exists");
                 return;
             }
             File bakDir = new File(dsConfInfo.getDsInstanceDir() +
                     FILE_SEP + "bak");
             if (!bakDir.isDirectory()) {
                 bakDir.mkdir();
             }
             StringBuffer resultBuffer = new StringBuffer();
             String db2BakCmd = dsAdmPath + "backup " + instanceDir + " " +
                     bakDir.getAbsolutePath() + FILE_SEP + dbBackupDir;
             pLogger.log(Level.FINE, "backUpDS", "Backing up DS instance." +
                     instanceDir);
             int retVal = AMTuneUtil.executeCommand(db2BakCmd, resultBuffer);
             if (retVal == -1) {
                 mWriter.writelnLocaleMsg("pt-cannot-backup-db");
                 throw new AMTuneException("Data Base Backup failed.");
             }
             pLogger.log(Level.FINE, "backUpDS", "Backing up Done...");
             try {
                 File dseLdif = new File(dseLdifPath);
                 pLogger.log(Level.FINE, "backUpDS", "Backing " + dseLdifPath);
                 File bakDseFile = new File(instanceDir +
                         FILE_SEP + "bak" + FILE_SEP + dbBackupDir +
                         FILE_SEP + "dse.ldif");
                 AMTuneUtil.CopyFile(dseLdif, bakDseFile);
                 pLogger.log(Level.FINE, "backUpDS", "Backing Done..");
             } catch (Exception ex) {
                 throw new AMTuneException("Couldn't bakup dse.ldif. " +
                         ex.getMessage());
             }
             successFile.createNewFile();
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "backUpDS",
                     "Error backing up DS " + ex.getMessage());
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     /**
      * Stops the Directory server using dsamd stop
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void stopDS()
     throws AMTuneException {
         pLogger.log(Level.FINE, "stopDS", "Stopping DS6.");
         StringBuffer resultBuffer = new StringBuffer();
         String stopCmd = dsAdmPath + "stop " + instanceDir;
         int retVal = AMTuneUtil.executeCommand(stopCmd, resultBuffer);
         if (retVal == -1){
             throw new AMTuneException(resultBuffer.toString());
         }
         pLogger.log(Level.FINE, "stopDS", "DS6 Successfully stopped.");
     }
 
     /**
      * Starts the Directory server using dsadm start
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void startDS()
     throws AMTuneException {
         pLogger.log(Level.FINE, "startDS", "Starting DS6.");
         StringBuffer resultBuffer = new StringBuffer();
         String startCmd = dsAdmPath + "start " + instanceDir;
         int retVal = AMTuneUtil.executeCommand(startCmd, resultBuffer);
         if (retVal == -1){
             throw new AMTuneException(resultBuffer.toString());
         }
         pLogger.log(Level.FINE, "startDS", "DS6 Successfully started.");
     }
 }
