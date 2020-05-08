 /**
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or https://www.escidoc.org/license/ESCIDOC.LICENSE .
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  *
  *
  * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.bwelabs.depositor.service;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.InvalidPropertiesFormatException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.help.UnsupportedOperationException;
 
 import org.escidoc.core.client.ingest.exceptions.ConfigurationException;
 import org.escidoc.core.client.ingest.exceptions.IngestException;
 import org.escidoc.core.client.ingest.filesystem.FileIngester;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 
 import de.escidoc.bwelabs.deposit.Configuration;
 import de.escidoc.bwelabs.depositor.error.AlreadyExistException;
 import de.escidoc.bwelabs.depositor.error.AlreadyExpiredException;
 import de.escidoc.bwelabs.depositor.error.ApplicationException;
 import de.escidoc.bwelabs.depositor.error.ConnectionException;
 import de.escidoc.bwelabs.depositor.error.DepositorException;
 import de.escidoc.bwelabs.depositor.error.InfrastructureException;
 import de.escidoc.bwelabs.depositor.utility.Utility;
 import de.escidoc.core.resources.common.properties.PublicStatus;
 
 /**
  * Provides methods which execute requests to a deposit servlet, administers threads.
  * 
  * @author ROF
  * 
  */
 public class SessionManager extends Thread {
 
     private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class.getName());
 
     public static final String PROP_BASEDIR = "depositor.sessionBaseDir";
 
     public static final String PROP_MAX_THREAD_NUMBER = "depositor.maxThreadNumber";
 
     public static final String PROP_PING_INTERVAL = "depositor.pingIntervalSeconds";
 
     public static final String ERR_MAX_THREADS_ =
         "The depositor service is unavalible. The maximal number of threads is reached. Please try later.";
 
     private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
 
     private static final String PATH_FORMAT = "yyyy_MM_dd_HH_mm_ss_SSS";
 
     private File baseDir;
 
     private int maxThreadNumber;
 
     private int threadNumber;
 
     private int pingInterval;
 
     private Map<String, Properties> configurations;
 
     private Map<String, String> configurationDirPathes;
 
     private Map<String, Properties> failedConfigurations;
 
     private Map<String, Properties> expiredSuccessfulConfigurations;
 
     private Map<String, Vector<ItemSession>> sessions;
 
     private Map<String, String> failedExpiredConfDir;
 
     private Vector<String> expiredConfigurationsSinceLastRun;
 
     private Map<String, File> dirsFromLastRunToProcess;
 
     private Vector<String> isCleaning;
 
     private boolean isThreadNeedsToFinish;
 
     private boolean isThreadFinished;
 
     public SessionManager(Properties props) throws DepositorException {
         Preconditions.checkNotNull(props, "props is null: %s", props);
 
         threadNumber = 0;
         int threadNumber = loadConfigurationAndGetThreadNumber(props);
 
         init(new File(props.getProperty(PROP_BASEDIR)), threadNumber);
     }
 
     private int loadConfigurationAndGetThreadNumber(Properties props) throws DepositorException {
         if (props.getProperty(PROP_BASEDIR) == null) {
             String message = "Required property missing: " + PROP_BASEDIR;
             LOG.error(message);
             throw new DepositorException(message);
         }
 
         if (props.getProperty(PROP_MAX_THREAD_NUMBER) == null) {
             String message = "Required property missing: " + PROP_MAX_THREAD_NUMBER;
             LOG.error(message);
             throw new DepositorException(message);
         }
         int threadNumber;
         try {
             threadNumber = Integer.parseInt(props.getProperty(PROP_MAX_THREAD_NUMBER));
         }
         catch (Exception e) {
             String message = "Required property must an integer: " + PROP_MAX_THREAD_NUMBER;
             LOG.error(message);
             throw new DepositorException(message);
         }
 
         if (props.getProperty(PROP_PING_INTERVAL) == null) {
             String message = "Required property missing: " + PROP_PING_INTERVAL;
             LOG.error(message);
             throw new DepositorException(message);
         }
 
         try {
             this.pingInterval = Integer.parseInt(props.getProperty(PROP_PING_INTERVAL));
         }
         catch (Exception e) {
             String message = "Required property must an integer: " + PROP_PING_INTERVAL;
             LOG.error(message);
             throw new DepositorException(message);
         }
         return threadNumber;
     }
 
     private void init(File baseDir, int maxThreadNumber) throws DepositorException {
        this.baseDir = baseDir;
         dirsFromLastRunToProcess = new HashMap<String, File>();
         sessions = new HashMap<String, Vector<ItemSession>>();
         failedExpiredConfDir = new HashMap<String, String>();
         configurations = new HashMap<String, Properties>();
         failedConfigurations = new HashMap<String, Properties>();
         expiredSuccessfulConfigurations = new HashMap<String, Properties>();
         configurationDirPathes = new HashMap<String, String>();
         isCleaning = new Vector<String>();
         expiredConfigurationsSinceLastRun = new Vector<String>();
 
         if (!baseDir.exists()) {
             baseDir.mkdirs();
         }
 
         File[] dirs = baseDir.listFiles();
         if (dirs == null)
             throw new DepositorException("Unable to restore configuration directories within a base directory: "
                 + baseDir.getPath());
 
         if (dirs.length > 0) {
             LOG.info("Restoring configurations from last run...");
 
             for (int i = 0; i < dirs.length; i++) {
                 if (dirs[i].isDirectory()) {
                     String dirName = dirs[i].getName();
                     File configurationFile = new File(dirs[i], Constants.CONFIGURATION_FILE_NAME);
                     if (!configurationFile.exists()) {
                         String message =
                             "Can not restore the configuration from the directory "
                                 + baseDir
                                 + "/"
                                 + dirName
                                 + " on start up of the Depositor: a configuration file does not exist in the directory.";
                         LOG.error(message);
                         continue;
                     }
 
                     FileInputStream fis = null;
                     Properties configProperties = null;
                     String configId = null;
                     try {
                         fis = new FileInputStream(configurationFile);
                         configProperties = new Properties();
 
                         try {
                             configProperties.loadFromXML(fis);
                             configId = configProperties.getProperty(Constants.PROPERTY_CONFIGURATION_ID);
                             if (dirName.startsWith("failed_expired_")) {
                                 failedExpiredConfDir.put(configId, dirName);
                                 continue;
                             }
                             if (isMonitoringTimeOver(configProperties)) {
                                 expiredConfigurationsSinceLastRun.add(configId);
                             }
                             this.configurations.put(configId, configProperties);
                             this.configurationDirPathes.put(configId, dirName);
                         }
                         catch (InvalidPropertiesFormatException e) {
                             String message =
                                 "Can not restore the configuration data from the directory " + baseDir + "/" + dirName
                                     + " on start up of the Depositor:";
                             LOG.error(message + e.getMessage());
                             continue;
                         }
                         catch (IOException e) {
                             String message =
                                 "Can not restore the configuration from the directory " + baseDir + "/" + dirName
                                     + " on start up of the Depositor:";
                             LOG.error(message + e.getMessage());
                             continue;
                         }
 
                     }
                     catch (FileNotFoundException e) {
                         String message =
                             "Can not restore the configuration data from the directory " + baseDir + "/" + dirName
                                 + " on start up of the Depositor:";
                         LOG.error(message + e.getMessage());
                         continue;
                     }
                     // save configuration directory from last run to
                     // process it later
                     dirsFromLastRunToProcess.put(configId, dirs[i]);
                 }
                 else {
                     dirs[i].delete();
                 }
             }
         }
         threadNumber = 0;
         this.maxThreadNumber = maxThreadNumber;
         setName("Session-Reaper");
         start();
     }
 
     /**
      * Method processes not successful content files, remained from a last run of a Depositor service.
      * 
      * @param directoryToProcess
      * @param configId
      */
     void processContentFiles(final File directoryToProcess, final String configId) {
         File[] files = directoryToProcess.listFiles();
         for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
             if (!(files[fileIndex].getName().equals(Constants.CONFIGURATION_FILE_NAME) || files[fileIndex]
                 .getName().startsWith("successful_"))) {
                 try {
                     storeContentToInfrastructure(directoryToProcess, configId, files, fileIndex);
                 }
                 catch (DepositorException e) {
                     // FIXME give a message
                     LOG.error(e.getMessage(), e);
                     addToFailedConfigurations(configId);
                 }
             }
         }
     }
 
     private void storeContentToInfrastructure(final File directoryToProcess, final String configId, File[] files, int j)
         throws DepositorException {
         new ItemSession(this, configurations.get(configId), files[j], directoryToProcess, null).start();
     }
 
     // ////////////////////////////////////////////////////////////////////////
     /**
      * Method increases counter holding the number of currently running threads.
      */
     protected synchronized void increaseThreadsNumber() {
         threadNumber++;
     }
 
     /**
      * Method decreases counter holding the number of currently running threads.
      */
     protected synchronized void decreaseThreadsNumber() {
         threadNumber--;
     }
 
     /**
      * Sessions administrator thread.
      */
     public void run() {
         while (!isThreadNeedsToFinish) {
             processFromLastRun();
             // iterate thru configurations, find expired configurations -->
             // clean sessions, find
             // failed configurations --> try to repair failed configurations
             if (configurations.size() > 0) {
                 synchronized (configurations) {
                     // FIXME containerId is _always_ empty
                     Vector<String> containerIds = new Vector<String>();
 
                     for (String configId : configurations.keySet()) {
                         Properties configuration = configurations.get(configId);
                         if (isReingestNotNeeded(configId, configuration)) {
                             cleanUpExpiredConfiguration(configId);
                         }
                         else {
                             sendPingToCore(containerIds, configuration);
                             reingestFailedIngest(configId, configuration);
                         }
                     }
 
                 }
             }
             int waitedSeconds = 0;
             while (!isThreadNeedsToFinish && (waitedSeconds < pingInterval / 2)) {
                 try {
                     Thread.sleep(1000);
                 }
                 catch (Exception e) {
                     LOG.error("Something wrong: " + e.getMessage());
                 }
                 waitedSeconds++;
             }
         }
 
         isThreadFinished = true;
     }
 
     private boolean isReingestNotNeeded(String configId, Properties configuration) {
         return expiredConfigurationsSinceLastRun.contains(configId) || isMonitoringTimeOver(configuration);
     }
 
     // if the configuration was already expired to restart
     // time of Depositor or if a monitoring time for the
     // configuration is over, clean up sessions for the
     // configuration
     private void cleanUpExpiredConfiguration(String configId) {
         cleanupSessions(configId);
         expiredConfigurationsSinceLastRun.remove(configId);
     }
 
     private void processFromLastRun() {
         // processing content files from last run of a Deposit service,
         // happens only once after restart of the Deposit service
         for (String configId : dirsFromLastRunToProcess.keySet()) {
             File dirToProcess = dirsFromLastRunToProcess.get(configId);
             processContentFiles(dirToProcess, configId);
         }
 
         dirsFromLastRunToProcess = new HashMap<String, File>();
     }
 
     private void reingestFailedIngest(String configId, Properties configuration) {
         // try to store failed content files of failed configurations into infrastructure
         if (failedConfigurations.containsKey(configId)) {
             boolean stillFailed = false;
             Vector<ItemSession> oldSessionsForConfiguration = new Vector<ItemSession>();
             Vector<ItemSession> newSessionsForConfiguration = new Vector<ItemSession>();
             Vector<ItemSession> sessionsForConfiguration = sessions.get(configId);
             synchronized (sessionsForConfiguration) {
                 for (int i = 0; i < sessionsForConfiguration.size(); i++) {
                     // check if a session was repaired in the meantime
                     ItemSession session = sessionsForConfiguration.get(i);
                     if (session.isFinished() && session.isSessionFailed()) {
                         // try try to store failed content files into infrastructure
                         stillFailed = true;
                         try {
                             ItemSession newSession =
                                 new ItemSession(this, configuration, session.get_contentFile(),
                                     session.get_configurationDirectory(), session.get_providedCheckSum());
 
                             newSessionsForConfiguration.add(newSession);
                             oldSessionsForConfiguration.add(session);
                         }
                         catch (DepositorException e) {
                             // FIXME give a message
                             LOG.error(e.getMessage(), e);
                         }
                     }
                 }
                 sessionsForConfiguration.removeAll(oldSessionsForConfiguration);
             }
             for (int i = 0; i < newSessionsForConfiguration.size(); i++) {
                 ItemSession session = newSessionsForConfiguration.get(i);
                 session.start();
             }
             // if all currently finished sessions of the configuration was repaired in a meantime,
             // the configuration is not 'failed' any more
             // and deposit service can accept new content files for the configuration
             if (!stillFailed) {
                 synchronized (failedConfigurations) {
                     failedConfigurations.remove(configId);
                 }
             }
         }
     }
 
     private void sendPingToCore(Vector<String> containerIds, Properties configuration) {
         // monitoring time is not over, ping a container for
         // the configuration
 
         String containerId = configuration.getProperty(Constants.PROPERTY_EXPERIMENT_ID);
         if (!containerIds.contains(containerId)) {
 
             String handle = configuration.getProperty(Constants.PROPERTY_USER_HANDLE);
             // ping alive for the container with the handle
             // try {
             // GetMethod get = EscidocConnector
             // .pingContainer(
             // configuration
             // .getProperty(Constants.PROPERTY_INFRASTRUCTURE_ENDPOINT),
             // containerId, handle);
             // get.releaseConnection();
             // } catch (ApplicationException e) {
             // logger.error("Error while ping container with id "
             // + containerId + e.getMessage());
             // // notify the user and eSyncDemon
             // } catch (InfrastructureException e) {
             // logger.error("Error while ping container with id "
             // + containerId + e.getMessage());
             // // notify the user and eSyncDemon
             // } catch (ConnectionException e) {
             // logger.error("Error while ping container with id "
             // + containerId + e.getMessage());
             // // notify the user and eSyncDemon
             // } catch (Throwable e) {
             // logger.error("Unexpected error while ping container with id "
             // + containerId + e.getMessage());
             // // notify the user and eSyncDemon
             // }
             containerIds.add(containerId);
         }
     }
 
     /**
      * Method checks if the monitoring time of a provided configuration is over.
      * 
      * @param configuration
      * @return
      */
     private static boolean isMonitoringTimeOver(Properties configuration) {
         String monitoringStartTime = configuration.getProperty(Constants.PROPERTY_MONITORING_START_TIME);
         if (monitoringStartTime == null) {
             return false;
         }
         DateTimeZone.setDefault(DateTimeZone.UTC);
 
         DateTime startTime = new DateTime(monitoringStartTime);
         String monitoringDuration = configuration.getProperty(Constants.PROPERTY_TIME_MONITORING_DURATION);
 
         int monitoringDurationMinutes = Integer.parseInt(monitoringDuration);
         DateTime endTime = startTime.plusMinutes(monitoringDurationMinutes);
 
         return endTime.isBeforeNow();
     }
 
     // ////////////////////////////////////////////////////////////////////////
 
     /**
      * Method adds a session to the map of tracked sessions of a configuration with a provided id.
      */
     protected void addSession(ItemSession session, String configurationId) {
         synchronized (sessions) {
             Vector<ItemSession> configurationSessions = sessions.get(configurationId);
             if (configurationSessions == null) {
                 configurationSessions = new Vector<ItemSession>();
 
             }
             configurationSessions.add(session);
             sessions.put(configurationId, configurationSessions);
         }
     }
 
     /**
      * Method adds a configuration to a map with failed configurations.
      * 
      * @param configurationId
      */
     public void addToFailedConfigurations(final String configurationId) {
         synchronized (failedConfigurations) {
             if (!failedConfigurations.containsKey(configurationId)) {
                 failedConfigurations.put(configurationId, configurations.get(configurationId));
             }
         }
     }
 
     // ////////////////////////////////////////////////////////////////////////
     /**
      * Method checks if a limit of threads on Depositor is exceeded and calls a method to check a provided stream with a
      * configuration.
      * 
      * 
      * Method reads a configuration from a provided input stream. It checks if all mandatory configuration properties
      * are present and have valid values. It creates a directory for the configuration inside a base directory, save a
      * configuration file Constants.CONFIGURATION_FILE_NAME into this configuration directory and store a configuration
      * as an item in to an infrastructure with the infrastructure end point contained in the configuration.
      * 
      * 
      */
     public void storeConfiguration(Configuration configProperties) throws ApplicationException, DepositorException,
         ConnectionException, InfrastructureException {
         throw new UnsupportedOperationException("everything moved");
     }
 
     public void ingestConfiguration(Configuration configProperties, File configFile) throws ConfigurationException,
         IngestException {
         FileIngester ingester =
             buildFileIngester(configProperties, configFile,
                 configurationDirPathes.get(configProperties.getProperty(Configuration.PROPERTY_CONFIGURATION_ID)));
 
         LOG.debug("ingesting configuration");
         ingester.setForceCreate(true);
         ingester.ingest();
     }
 
     private FileIngester buildFileIngester(Configuration configProperties, File configFile, String configDirName) {
         LOG.debug("prepare ingesting configuration");
         FileIngester ingester =
             new FileIngester(configProperties.getProperty(Configuration.PROPERTY_INFRASTRUCTURE_ENDPOINT),
                 configProperties.getProperty(Configuration.PROPERTY_USER_HANDLE),
                 configProperties.getProperty(Configuration.PROPERTY_EXPERIMENT_ID));
 
         LOG.debug("should be the same:[");
         LOG.debug(baseDir + "/" + configDirName + "/" + Constants.CONFIGURATION_FILE_NAME);
         LOG.debug(configFile.getPath());
         LOG.debug("]");
 
         ingester.addFile(baseDir + "/" + configDirName + "/" + Constants.CONFIGURATION_FILE_NAME);
         ingester.setItemContentModel(configProperties.getProperty(Configuration.PROPERTY_CONTENT_MODEL_ID));
         // FIXME
         ingester.setContainerContentModel(configProperties.getProperty(Configuration.PROPERTY_CONTENT_MODEL_ID));
         ingester.setContext(configProperties.getProperty(Configuration.PROPERTY_CONTEXT_ID));
         ingester.setContentCategory("ORIGINAL");
         ingester.setInitialLifecycleStatus(PublicStatus.PENDING);
         ingester.setMimeType("text/xml");
         ingester.setValidStatus("valid");
         ingester.setVisibility("visible");
         return ingester;
     }
 
     public void registerConfiguration(Configuration configProperties) {
         synchronized (configurations) {
             configurations.put(configProperties.getProperty(Configuration.PROPERTY_CONFIGURATION_ID), configProperties);
         }
     }
 
     public void checkIfAlreadyExists(String configurationId) throws AlreadyExistException {
         if (configurations.containsKey(configurationId) || expiredSuccessfulConfigurations.containsKey(configurationId)
             || failedExpiredConfDir.containsKey(configurationId)) {
             String message = "Configuration " + configurationId + " already exists.";
             LOG.error(message);
             throw new AlreadyExistException(message);
         }
     }
 
     /**
      * Removes a configuration file and configuration directory from a file system.
      * 
      * @param configFile
      */
     private void deleteConfigFile(File configFile) {
         String confDirectoryName = configFile.getParent();
         configFile.delete();
         File parentDirectory = new File(confDirectoryName);
         if (parentDirectory.exists()) {
             parentDirectory.delete();
         }
     }
 
     /**
      * Method creates a new configuration directory for a provided configuration with a currently time stamp as a name
      * and save a configuration file within it.
      * 
      * @param configuration
      * @param configurationId
      * @return File with a created configuration directory.
      * @throws DepositorException
      */
     public File saveInLocalFileSystem(Properties configuration) throws DepositorException {
 
         LOG.debug("saving configuration in local file system");
         DateTimeZone.setDefault(DateTimeZone.UTC);
         DateTime currentTime = new DateTime();
         DateTimeFormatter fmt = DateTimeFormat.forPattern(PATH_FORMAT);
         String configurationDirectoryName = currentTime.toString(fmt);
         File configurationDirectory = new File(baseDir, configurationDirectoryName);
         configurationDirectory.mkdirs();
         File configurationFile = new File(configurationDirectory, Constants.CONFIGURATION_FILE_NAME);
         FileOutputStream os = null;
         try {
             os = new FileOutputStream(configurationFile);
         }
         catch (FileNotFoundException e) {
             LOG.error(e.getMessage());
             throw new DepositorException(e.getMessage());
         }
         try {
             configuration.storeToXML(os, null);
             os.flush();
             os.close();
             synchronized (configurationDirPathes) {
                 configurationDirPathes.put(configuration.getProperty(Configuration.PROPERTY_CONFIGURATION_ID),
                     configurationDirectoryName);
             }
 
         }
         catch (IOException e) {
             LOG.error(e.getMessage());
             throw new DepositorException(e.getMessage());
         }
         return configurationFile;
     }
 
     /**
      * Method checks if a configuration-session is being cleaned at the moment by another thread. In this case it waits
      * before the other thread finished, reads the result of the clean operation and throws an exception if the clean
      * operation could not be successful performed. If a configuration-session is not being cleaned by another thread,
      * method calls a clean method cleanupSessions() and then removes a configuration from a map with registered
      * configurations.
      * 
      * @param configId
      * @throws ApplicationException
      * @throws DepositorException
      */
     public void deleteConfiguration(final String configId) throws ApplicationException, DepositorException {
 
         if (failedExpiredConfDir.containsKey(configId)) {
             String message =
                 "Can not delete the configuration: depositor could not store some content "
                     + "files for this configuration into the infrastracture.";
             LOG.error(message);
             throw new DepositorException(message);
         }
 
         if (!isCleaning.contains(configId)) {
             synchronized (configurations) {
                 if (!configurations.containsKey(configId)) {
                     String message = "Depositor can not find a configuration with the id " + configId + ".";
                     LOG.error(message);
                     throw new ApplicationException(message);
                 }
                 // the configuration is not deleting at the moment by any
                 // different thread and the configuration is registered,
                 // cleanup session for the configuration
                 cleanupSessions(configId);
                 configurations.remove(configId);
             }
         }
         else {
             // wait until the thread cleaning this configuration is
             // finished
             while (isCleaning.contains(configId)) {
                 try {
                     Thread.sleep(250);
                 }
                 catch (Exception e) {
                 }
             }
         }
     }
 
     /**
      * Method calls a method putMonitoringStartTimeIntoConfigurationIfMissing() to check if a configuration with a
      * provided id contains a property Monitoring Start Time and put it into a configuration if it is missing. Then it
      * reads a provided input stream, checks if a provided check sum is valid and save a content into a configuration
      * directory for a configuration with a provided id. If a check sum is valid, it starts a new thread to store a
      * content as an item into a container in the infrastructure for the configuration.
      * 
      * @param configId
      * @param checkSumValue
      * @param is
      * @param fileName
      * @return true - if a check sum is valid, false - otherwise
      * @throws ApplicationException
      * @throws DepositorException
      */
     public boolean refactorNameOfThisMethod(
         final String configId, final String checkSumValue, final InputStream is, final String fileName)
         throws ApplicationException, DepositorException {
 
         checkPreconditions(configId);
         File configurationDirectory = new File(baseDir, configurationDirPathes.get(configId));
         checkIfExists(configId, configurationDirectory);
         checkFileName(configId, fileName, configurationDirectory);
         putMonitoringStartTimeIntoConfigurationIfMissing(configId);
 
         File content = new File(configurationDirectory, fileName);
         MessageDigest digest = storeFileAndCalculateChecksum(configId, is, content);
         if (isCheckSumEquals(digest, checkSumValue)) {
             ingestFileAsync(configId, checkSumValue, configurationDirectory, content);
             return true;
         }
 
         content.delete();
         return false;
     }
 
     private void ingestFileAsync(
         final String configId, final String checkSumValue, File configurationDirectory, File content)
         throws DepositorException {
         // now, content from the request is stored and validated.
         // create a session and start it. The session computed all additional
         // information and stores the content as component content in an item in
         // the eSciDoc Infrastructure.
         new ItemSession(this, configurations.get(configId), content, configurationDirectory, checkSumValue).start();
     }
 
     private boolean isCheckSumEquals(MessageDigest md, String checkSumValue) {
 
         // compare computed digest with the one send with the request
         byte[] digest = md.digest();
         String checksum = Utility.byteArraytoHexString(digest);
         LOG.debug("Checksums: send[" + checkSumValue + "] file[" + checksum + "]");
 
         return checksum.equals(checkSumValue);
     }
 
     private static void checkIfExists(final String configId, File configurationDirectory) throws DepositorException {
         if (!configurationDirectory.exists()) {
             String message =
                 "Error on Depositor: can not found a directory for the configuration with the id " + configId + ".";
             LOG.error(message);
             throw new DepositorException(message);
         }
     }
 
     private MessageDigest storeFileAndCalculateChecksum(final String configId, final InputStream is, File contentFile)
         throws DepositorException {
         MessageDigest md = getMessageDigest(configId);
         DigestInputStream dis = null;
         try {
             FileOutputStream fos = new FileOutputStream(contentFile);
             dis = new DigestInputStream(is, md);
             byte[] buf = new byte[5000];
             int readByte;
             while ((readByte = dis.read(buf)) > 0) {
                 fos.write(buf, 0, readByte);
             }
         }
         catch (FileNotFoundException e) {
             LOG.error(e.getMessage());
             throw new DepositorException(e.getMessage());
         }
         catch (IOException e) {
             LOG.error(e.getMessage());
             throw new DepositorException(e.getMessage());
         }
         finally {
             if (dis != null) {
                 try {
                     dis.close();
                 }
                 catch (IOException e) {
                     LOG.error(e.getMessage());
                 }
             }
         }
         return md;
     }
 
     private MessageDigest getMessageDigest(final String configId) throws DepositorException {
         MessageDigest md = null;
         try {
             md =
                 MessageDigest.getInstance(configurations.get(configId).getProperty(
                     Constants.PROPERTY_CHECKSUM_ALGORITHM));
         }
         catch (NoSuchAlgorithmException e) {
             LOG.error(e.getMessage());
             throw new DepositorException(e.getMessage());
         }
         return md;
     }
 
     // check if filename is already sent for this configuration
     private static void checkFileName(final String configId, final String fileName, File configurationDirectory)
         throws AlreadyExistException {
         File[] files = configurationDirectory.listFiles();
         for (int i = 0; i < files.length; i++) {
             String name = files[i].getName();
             if (name.endsWith(fileName)) {
                 if (name.equals("successful_" + fileName) || name.equals("failed_" + fileName)
                     || name.equals("successful_failed_" + fileName) || name.equals(fileName)) {
                     String message =
                         "A content file '" + fileName + "' for the configuration with id " + configId
                             + " already exists on Depositor.";
                     LOG.error(message);
                     throw new AlreadyExistException(message);
                 }
             }
         }
     }
 
     private void checkPreconditions(final String configId) throws DepositorException, AlreadyExpiredException,
         ApplicationException {
         if (threadNumber == maxThreadNumber) {
             LOG.error(ERR_MAX_THREADS_);
             throw new DepositorException(ERR_MAX_THREADS_);
         }
 
         if (expiredSuccessfulConfigurations.containsKey(configId)
             || expiredConfigurationsSinceLastRun.contains(configId) || isCleaning.contains(configId)) {
             String message = "A session for the configuration with " + configId + " is expired.";
             LOG.error(message);
             throw new AlreadyExpiredException(message);
         }
 
         if (failedExpiredConfDir.containsKey(configId)) {
             String message =
                 "A configuration with id  "
                     + configId
                     + " is expiered and failed due to an internal failure on a deposit service or on an infrastructure.";
             LOG.error(message);
             throw new DepositorException(message);
         }
 
         if (failedConfigurations.containsKey(configId)) {
             String message =
                 "Error on Depositor: can not temporary accept content files for the configuration with the id "
                     + configId + " due to an internal failure on a deposit service or on an infrastructure.";
             LOG.error(message);
             throw new DepositorException(message);
         }
 
         if (!configurations.containsKey(configId)) {
             String message = "Can not find a configuration with the id " + configId + ".";
             LOG.error(message);
             throw new ApplicationException(message);
         }
     }
 
     /**
      * Method checks if a configuration with a provided id contains a property Monitoring Start Time. It put a time
      * stamp with a current time into a configuration contained in a map with configurations and in a configuration
      * file.
      * 
      * @deprecated Monitoring start time should not be needed in configuration.
      * @param configId
      */
     @Deprecated
     private void putMonitoringStartTimeIntoConfigurationIfMissing(String configId) {
         File configurationDirectory = new File(baseDir, configurationDirPathes.get(configId));
         Properties configuration = null;
         // if a configuration does not contain a calculated monitoring start
         // time,
         // put a calculated monitoring start time in to the configuration and
         // store the
         // configuration into a configuration file
         synchronized (configurations) {
             configuration = configurations.get(configId);
             String monitoringStartTime = configuration.getProperty(Constants.PROPERTY_MONITORING_START_TIME);
             if (monitoringStartTime == null) {
                 DateTimeZone.setDefault(DateTimeZone.UTC);
                 DateTime currentTime = new DateTime();
                 DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
                 String timeStamp = currentTime.toString(fmt);
                 configuration.put(Constants.PROPERTY_MONITORING_START_TIME, timeStamp);
 
                 File configurationFile = new File(configurationDirectory, Constants.CONFIGURATION_FILE_NAME);
                 configurationFile.delete();
                 FileOutputStream os = null;
                 try {
                     os = new FileOutputStream(configurationFile);
                 }
                 catch (FileNotFoundException e) {
                     LOG.error(e.getMessage());
 
                 }
                 try {
                     configuration.storeToXML(os, null);
                     if (os != null) {
                         os.flush();
                         os.close();
                     }
                 }
                 catch (IOException e) {
                     LOG.error(e.getMessage());
 
                 }
             }
         }
     }
 
     // ///////////////////////////////////////////////////////////////////////
 
     public void close() {
         isThreadNeedsToFinish = true;
         while (!isThreadFinished) {
             try {
                 Thread.sleep(250);
             }
             catch (Exception e) {
             }
         }
     }
 
     public void finalize() {
         close();
     }
 
     /**
      * Method puts a provided configuration id into a set with configurations, which are being cleaned at the moment, to
      * prevent another threads from the cleaning of the configuration. It checks if all of the configuration sessions
      * finished successful or a configuration has no sessions and is not contained in a map with a failed
      * configurations. In this case it removes a configuration directory and a configuration file from a file system and
      * put the configuration id into a map with successful finished configurations. Otherwise it calls a method
      * renameConfigDirectoryToFailedExpired() to mark a configuration directory as expired and failed. A fact, that a
      * configuration has no sessions and is contained in a map with failed configurations means that content files of
      * the configuration could not be restored from a file system on restart of Demon service. Finally method removes
      * the configuration id from a set with configurations, which are being cleaned at the moment.
      * 
      * @param configurationId
      */
     public void cleanupSessions(final String configurationId) {
         // add the configuration to a set of configurations, which are being
         // cleaned at the moment
         synchronized (isCleaning) {
             isCleaning.add(configurationId);
         }
         Vector<ItemSession> sessionsForConfiguration = null;
         Properties configuration = configurations.get(configurationId);
         synchronized (sessions) {
             sessionsForConfiguration = sessions.get(configurationId);
             sessions.remove(configurationId);
         }
         if (sessionsForConfiguration != null) {
             boolean configurationFailed = false;
             Iterator<ItemSession> iter = sessionsForConfiguration.iterator();
             File configurationDirectory = null;
             // check if some sessions of the configuration failed
             while (iter.hasNext()) {
                 ItemSession session = iter.next();
                 configurationDirectory = session.get_configurationDirectory();
                 while (!session.isFinished()) {
                     try {
                         Thread.sleep(250);
                     }
                     catch (Exception e) {
                     }
                 }
                 if (!session.isSessionFailed()) {
                     session.deleteContentFile();
                     iter.remove();
                 }
                 else {
                     configurationFailed = true;
                 }
 
             }
             if (configurationFailed) {
                 // some sessions of the configuration failed (some content files
                 // could not be stored into an infrastructure)
                 renameConfigDirectoryToFailedExpired(configurationDirectory, configurationId);
             }
             else {
                 // all sessions of the configuration finished successful (all
                 // content files were stored into an infrastructure)
                 removeSuccessfulConfiguration(configurationDirectory, configurationId, configuration);
             }
         }
         else {
             // There are no sessions for the configuration
             String confDirectoryName = configurationDirPathes.get(configurationId);
             File configurationDirectory = new File(baseDir, confDirectoryName);
             if (configurationDirectory.exists()) {
                 if (failedConfigurations.containsKey(configurationId)) {
                     // content files for the configuration could not be restored
                     // from a file system after restart of a Depositor
                     renameConfigDirectoryToFailedExpired(configurationDirectory, configurationId);
                 }
                 else {
                     removeSuccessfulConfiguration(configurationDirectory, configurationId, configuration);
                 }
             }
         }
         synchronized (failedConfigurations) {
             failedConfigurations.remove(configurationId);
         }
         synchronized (isCleaning) {
             // remove the configuration from a set of configurations, which
             // are being cleaned at the moment
             isCleaning.remove(configurationId);
         }
     }
 
     /**
      * Method removes a provided configuration file and a provided configuration directory from a file system. It puts a
      * provided configuration id and a provided configuration into a map with successful finished configurations.
      * 
      * @param configurationFile
      * @param configurationDirectory
      * @param configurationId
      * @param configuration
      */
     private void removeSuccessfulConfiguration(
         final File configurationDirectory, final String configurationId, final Properties configuration) {
         File[] files = configurationDirectory.listFiles();
         for (int i = 0; i < files.length; i++) {
             files[i].delete();
         }
         configurationDirectory.delete();
         synchronized (expiredSuccessfulConfigurations) {
             expiredSuccessfulConfigurations.put(configurationId, configuration);
         }
     }
 
     /**
      * Method adds a a prefix 'failed_expired_' to a configuration directory name and put the configuration id into a
      * map, containing ids of failed expired configurations and configuration directory names.
      * 
      * @param configurationDirectory
      * @param configurationId
      */
     private void renameConfigDirectoryToFailedExpired(final File configurationDirectory, final String configurationId) {
         File[] files = configurationDirectory.listFiles();
         for (int i = 0; i < files.length; i++) {
             if (files[i].getName().startsWith("successful_")) {
                 files[i].delete();
             }
         }
         String configDirName = configurationDirectory.getName();
         synchronized (failedExpiredConfDir) {
             boolean success = configurationDirectory.renameTo(new File(baseDir, "failed_expired_" + configDirName));
             if (!success) {
                 failedExpiredConfDir.put(configurationId, configDirName);
                 LOG.error("Error while cleaning up sessions for the configuration with id " + configurationId
                     + " : can not rename a configuration directory to 'failed_expired_" + configDirName + "'.");
             }
             else {
                 failedExpiredConfDir.put(configurationId, "failed_expired_" + configDirName);
             }
         }
     }
 
 }
