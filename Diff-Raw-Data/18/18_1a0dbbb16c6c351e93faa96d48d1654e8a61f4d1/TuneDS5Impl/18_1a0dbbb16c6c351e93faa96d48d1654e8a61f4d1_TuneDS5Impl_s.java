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
 * $Id: TuneDS5Impl.java,v 1.3 2008-07-25 05:48:57 kanduls Exp $
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
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPAttributeSet;
 import netscape.ldap.LDAPEntry;
 
 /**
  * <code>TuneDS5Impl<\code> extends the <code>AMTuneDSBase<\code> and tunes
  * the Directory server 5.2
  *
  */
 public class TuneDS5Impl extends AMTuneDSBase {
     private String db2BakPath;
     private String db2IndexPath;
     private String stopCmdPath;
     private String startCmdPath;
     private String dbBackUpDir;
 
     /**
      * Constructs the instance of this class
      */
     public TuneDS5Impl(boolean isSMStore) {
         super(isSMStore);
     }
 
     /**
      * Initializes the configuration information.
      *
      * @param confInfo Configuration information used for computing the tuning
      *   parameters for Directory server.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void initialize(AMTuneConfigInfo confInfo)
     throws AMTuneException {
         super.initialize(confInfo);
         if (AMTuneUtil.isWindows()) {
             db2BakPath = instanceDir + FILE_SEP + "db2bak.bat ";
             db2IndexPath = instanceDir + FILE_SEP + "db2index.pl ";
             stopCmdPath = instanceDir + FILE_SEP + "stop-slapd.bat";
             startCmdPath = instanceDir + FILE_SEP + "start-slapd.bat";
         } else {
             db2BakPath = instanceDir + FILE_SEP + "db2bak ";
             db2IndexPath = instanceDir + FILE_SEP + "db2index.pl ";
             stopCmdPath = instanceDir + FILE_SEP + "stop-slapd";
             startCmdPath = instanceDir + FILE_SEP + "start-slapd";
         }
         dbBackUpDir = DB_BACKUP_DIR_PREFIX + "-" + AMTuneUtil.getRandomStr();
     }
 
     /**
      * This method performs the sequence of operations for tuning
      * Directory server 5.2.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public void startTuning()
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "startTuning","Start tuning.");
             mWriter.writeln(CHAPTER_SEP);
             mWriter.writelnLocaleMsg("pt-fam-ds-tuning");
             if (isSM) {
                 mWriter.writelnLocaleMsg("pt-fam-sm-ds-tuning");
             }
             mWriter.writeln(CHAPTER_SEP);
             mWriter.writelnLocaleMsg("pt-init");
             mWriter.writeln(LINE_SEP);
             computeTuneValues();
             mWriter.writeln(PARA_SEP);
             modifyLDAP();
             if ( !AMTuneUtil.isWindows()) {
                 //For Windows changing dse with this script is not recommended.
                 tuneUsingDSE();
             }
             if (!isSM) {
                 tuneDSIndex();
             }
             tuneFuture();
             mWriter.writelnLocaleMsg("pt-ds-um-mutliple-msg");
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "startTuning", "Error tuning DS5.2");
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
      * This method modify s the DB home location in dse.ldif to point to
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
      * This method computes the recommended values for tuning LDAP and applys
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
      * This method finds the attributes that need to be indexed, if tuning mode
      * is set to "CHANGE" it creates the index for the required attributes.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void tuneDSIndex()
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "tuneDSIndex", "Tune DS Index.");
             String dn = getDBDN();
             StringTokenizer firstCol = new StringTokenizer(dn, ",");
             String dbName = firstCol.nextToken().replace("cn=", "");
             List existingIdx = getIndexForDB(dbName);
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
             pLogger.log(Level.SEVERE, "tundeDSIndex", "Error tuning DS5 index");
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     /**
      * Creates index for the required attribute, creates new index dn for the
      * attribute and invokes db2index.pl script
      *
      * @param attrName Name of the attribute to be indexed.
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private void createIndex(String attrName)
     throws AMTuneException {
         try {
             pLogger.log(Level.FINE, "createIndex", "Creating index for " +
                     attrName);
             String dbName = getDBName();
             String newDn = "cn=" + attrName + ",cn=index,cn=" + dbName +
                     "," + LDBM_DATABASE_DN;
             String[] objClassArr = {"top", "nsIndex"};
             String[] idxTypes = {"pres", "eq", "sub"};
             LDAPAttribute[] attrs = {
                 new LDAPAttribute("objectClass", objClassArr),
                 new LDAPAttribute("cn", attrName),
                 new LDAPAttribute("nsSystemIndex", idxTypes)
             };
             LDAPAttributeSet attrSet = new LDAPAttributeSet(attrs);
             LDAPEntry newIdxEntry = new LDAPEntry(newDn, attrSet);
             addLDAPEntry(newIdxEntry);
            String perlExePath = configInfo.getPerlBinDir() + FILE_SEP +
                    "perl.exe ";
             File pexe = new File (perlExePath);
             if (!pexe.isFile()) {
                 //Assuming perl is in system path
                perlExePath = "perl.exe ";
             }
             String idxCmd = perlExePath + db2IndexPath + "-D \"" +
                     dsConfInfo.getDirMgrUid() + "\" -j " + dsPassFilePath +
                     " -n " + dbName + " -t " + attrName;
             StringBuffer resultBuffer = new StringBuffer();
             int retVal = AMTuneUtil.executeCommand(idxCmd, resultBuffer);
             if (retVal == -1) {
                 throw new AMTuneException("Creating index command failed.");
             }
         } catch (Exception ex) {
             //just print the message
             mWriter.writelnLocaleMsg("pt-idx-create-error");
             pLogger.log(Level.SEVERE, "createIndex", "Error creating index " +
                     "for attribute " + attrName + " : " + ex.getMessage());
 
         }
     }
 
     /**
      * Takes the backup of the DS 5.2 by invoking db2bak.bat
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void  backUpDS()
     throws AMTuneException {
         try {
             String dbBackUpSuccessFile = instanceDir + FILE_SEP + "bak" +
                     FILE_SEP + dbBackUpDir + FILE_SEP +
                     "SUCCESS.dontdelete";
             File successFile = new File(dbBackUpSuccessFile);
             if (successFile.isFile()) {
                 pLogger.log(Level.INFO, "backUpDS", "Backup exists");
                 return;
             }
             File bakDir = new File(instanceDir + FILE_SEP + "bak");
             if (!bakDir.isDirectory()) {
                 bakDir.mkdir();
             }
             StringBuffer resultBuffer = new StringBuffer();
             String db2BakCmd = db2BakPath +  bakDir.getAbsolutePath() +
                     FILE_SEP + dbBackUpDir;
             pLogger.log(Level.FINE, "backUpDS", "Backing up DS.");
             int retVal = AMTuneUtil.executeCommand(db2BakCmd, resultBuffer);
             if (retVal == -1) {
                 mWriter.writelnLocaleMsg("pt-cannot-backup-db");
                 throw new AMTuneException("Data Base Backup failed.");
             }
             pLogger.log(Level.FINE, "backUpDS", "Backing up Done...");
             try {
                 File dseLdif = new File(dseLdifPath);
                 pLogger.log(Level.FINE, "backUpDS", "Backing " + dseLdifPath);
                 File bakDseFile = new File(instanceDir + FILE_SEP +
                         "bak" + FILE_SEP + dbBackUpDir + FILE_SEP +
                         "dse.ldif");
                 AMTuneUtil.CopyFile(dseLdif, bakDseFile);
                 pLogger.log(Level.FINE, "backUpDS", "Backing Done..");
             } catch (Exception ex) {
                 throw new AMTuneException("Couldn't bakup dse.ldif. " +
                         ex.getMessage());
             }
             successFile.createNewFile();
         } catch (Exception ex) {
             throw new AMTuneException(ex.getMessage());
         }
     }
 
     /**
      * Stops the Directory Server 5.2 using stop-slapd.bat
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void stopDS()
     throws AMTuneException {
         pLogger.log(Level.FINE, "stopDS", "Stopping DS.");
         StringBuffer resultBuffer = new StringBuffer();
         int retVal = AMTuneUtil.executeCommand(stopCmdPath, resultBuffer);
         if (retVal == -1){
             throw new AMTuneException("Error stopping DS 5.");
         }
         pLogger.log(Level.FINE, "stopDS", "DS Successfully stopped.");
     }
 
     /**
      * Stops the Directory Server 5.2 using start-slapd.bat
      *
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     protected void startDS()
     throws AMTuneException {
         pLogger.log(Level.FINE, "startDS", "Starting DS.");
         StringBuffer resultBuffer = new StringBuffer();
         int retVal = AMTuneUtil.executeCommand(startCmdPath, resultBuffer);
         if (retVal == -1){
             throw new AMTuneException("Error starting DS 5.");
         }
         pLogger.log(Level.FINE, "startDS", "DS Successfully started.");
     }
 }
