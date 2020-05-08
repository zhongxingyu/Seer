 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hcatalog.hcatmix.performance;
 
 import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
 import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.hcatalog.hcatmix.*;
 import org.apache.hcatalog.hcatmix.conf.HiveTableSchema;
 import org.apache.hcatalog.hcatmix.conf.TableSchemaXMLParser;
 import org.apache.pig.PigRunner;
 import org.apache.pig.tools.pigstats.JobStats;
 import org.apache.pig.tools.pigstats.OutputStats;
 import org.apache.pig.tools.pigstats.PigProgressNotificationListener;
 import org.apache.thrift.TException;
 
 import org.perf4j.GroupedTimingStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.Assert;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.List;
 
 import org.perf4j.StopWatch;
 
 import static junit.framework.Assert.assertEquals;
 
 public class LoadStoreScriptRunner {
     private static final Logger LOG = LoggerFactory.getLogger(LoadStoreScriptRunner.class);
     private static final String HCATMIX_LOCAL_ROOT = "/tmp/hcatmix";
     private static final String HCATMIX_HDFS_ROOT = "/tmp/hcatmix";
     private static final String PIG_DATA_OUTPUT_DIR = HCATMIX_HDFS_ROOT + "/pigdata";
     private static final String DATAGEN_OUTPUT_DIR = HCATMIX_HDFS_ROOT + "/data";
     private static final String HCATMIX_PIG_SCRIPT_DIR = HCATMIX_LOCAL_ROOT + "/pig_scripts";
 
     private final HCatMixSetup hCatMixSetup;
     private final HCatMixSetupConf hCatMixSetupConf;
     private final String tableName;
     private final String dbName;
     private final int NUM_MAPPERS = 30;
     private final String additionalJars;
     private final int rowCount;
     private final String hcatTableSpecFileName;
     private final GroupedTimingStatistics timedStats = new GroupedTimingStatistics();
     private final HiveTableSchema hiveTableSchema;
 
     public LoadStoreScriptRunner(String hcatTableSpecFile) throws MetaException, IOException, SAXException, ParserConfigurationException,
             NoSuchObjectException, TException, InvalidObjectException {
         this.hcatTableSpecFileName = new File(hcatTableSpecFile).getName();
 
         // Generate data
         hCatMixSetupConf = new HCatMixSetupConf.Builder().confFileName(hcatTableSpecFile)
                 .createTable().generateData().outputDir(DATAGEN_OUTPUT_DIR).generatePigScripts()
                 .pigScriptDir(HCATMIX_PIG_SCRIPT_DIR).pigDataOutputDir(PIG_DATA_OUTPUT_DIR)
                 .numMappers(NUM_MAPPERS).build();
         hCatMixSetup = new HCatMixSetup();
 
         additionalJars = getHCatLibJars();
 
         TableSchemaXMLParser configParser = new TableSchemaXMLParser(hcatTableSpecFile);
         List<HiveTableSchema> multiInstanceList = configParser.getHiveTableList();
         assertEquals("Only one table specification should be present per file", 1, multiInstanceList.size());
         hiveTableSchema = multiInstanceList.get(0);
         tableName = hiveTableSchema.getName();
         dbName = hiveTableSchema.getDatabaseName();
         rowCount = hiveTableSchema.getRowCount();
     }
 
     private String getHCatLibJars() {
         String hcatDir = System.getenv("HCAT_HOME");
         File hcatLibDir = new File(hcatDir + "/lib/");
 
         StringBuffer jars = new StringBuffer();
         String delim = "";
         for (File jarFile : hcatLibDir.listFiles()) {
             jars.append(delim).append(jarFile);
             delim = ":";
         }
         return jars.toString();
     }
 
     public void setUp() throws IOException, TException, NoSuchObjectException, MetaException, SAXException,
             InvalidObjectException, ParserConfigurationException {
         hCatMixSetup.setupFromConf(hCatMixSetupConf);
 
         // Also create one more copy of the table for testing copying from one HCat table to another
         hiveTableSchema.setName(HCatMixUtils.getCopyTableName(tableName));
         try {
            LOG.info("About to create table: " + hiveTableSchema.getName());
             hCatMixSetup.createTable(hiveTableSchema);
             LOG.info("Successfully created table: " + hiveTableSchema.getName());
             // Revert back the name to the original name, so that the calling setUp() again wont give a wrong name
             hiveTableSchema.setName(HCatMixUtils.removeCopyFromTableName(hiveTableSchema.getName()));
         } catch (AlreadyExistsException e) {
             LOG.info("Couldn't create table, " + hiveTableSchema.getName() + ". Already exists ignored and proceeding", e);
         }
 
     }
 
     private void runScript(String scriptName) {
         PigProgressListener listener = new PigProgressListener(rowCount);
 
         String tmpDir = System.getProperty("buildDirectory");
         if( tmpDir == null) {
             if(new File("target/").exists()) {
                 tmpDir = "target";
             } else {
                 tmpDir = "/tmp";
             }
         }
        final String logFileName =  new StringBuilder().append(tmpDir).append("/").append(scriptName).append("-")
                .append(System.currentTimeMillis()/1000).append(".log").toString();
         LOG.info("[" + scriptName + "] log file: " + logFileName);
         String[] args = {"-Dpig.additional.jars=" + additionalJars, "-f", scriptName, "-l", logFileName};
 //        String[] args = {"-Dpig.additional.jars=" + additionalJars, "-f", scriptName};
         PigRunner.run(args, listener);
     }
 
     public void runPigLoadHCatStoreScript() throws IOException {
         LOG.info("Running pig script using pig load/HCat store");
         StopWatch stopWatch = new LoadStoreStopWatch(hcatTableSpecFileName, LoadStoreStopWatch.LoadStoreType.PIG_LOAD_HCAT_STORE);
         runScript(HCatMixUtils.getHCatStoreScriptName(HCATMIX_PIG_SCRIPT_DIR, tableName));
         stopWatch.stop();
         timedStats.addStopWatch(stopWatch);
         LOG.info("Successfully ran pig script: pig load/HCat store");
     }
 
     public void runHCatLoadPigStoreScript() throws IOException {
         LOG.info("Running pig script using HCat load/pig store");
         StopWatch stopWatch = new LoadStoreStopWatch(hcatTableSpecFileName, LoadStoreStopWatch.LoadStoreType.HCAT_LOAD_PIG_STORE);
         runScript(HCatMixUtils.getHCatLoadScriptName(HCATMIX_PIG_SCRIPT_DIR, tableName));
         stopWatch.stop();
         timedStats.addStopWatch(stopWatch);
         LOG.info("Successfully ran pig script: HCat load/pig store");
     }
 
     public void runPigLoadPigStoreScript() throws IOException {
         LOG.info("Running pig script using pig load/pig store");
         StopWatch stopWatch = new LoadStoreStopWatch(hcatTableSpecFileName, LoadStoreStopWatch.LoadStoreType.PIG_LOAD_PIG_STORE);
         runScript(HCatMixUtils.getPigLoadStoreScriptName(HCATMIX_PIG_SCRIPT_DIR, tableName));
         stopWatch.stop();
         timedStats.addStopWatch(stopWatch);
         LOG.info("Successfully ran pig script: pig load/pig store");
     }
 
     public void runHCatLoadHCatStoreScript() throws IOException {
         LOG.info("Running pig script using hcat load/ store");
         StopWatch stopWatch = new LoadStoreStopWatch(hcatTableSpecFileName, LoadStoreStopWatch.LoadStoreType.HCAT_LOAD_HCAT_STORE);
         runScript(HCatMixUtils.getHCatLoadStoreScriptName(HCATMIX_PIG_SCRIPT_DIR, tableName));
         stopWatch.stop();
         timedStats.addStopWatch(stopWatch);
         LOG.info("Successfully ran pig script: pig load/hcat store");
     }
 
     public void deleteHCatTables() throws NoSuchObjectException, MetaException, TException {
         // Delete the HCat Table
         try {
             hCatMixSetup.deleteTable(dbName, tableName);
         } catch (Exception e) {
             LOG.info(MessageFormat.format("Couldn't delete table {0}.{1}. Ignored and proceeding", dbName, tableName), e);
         }
 
         // Delete the copy table
         try {
             hCatMixSetup.deleteTable(dbName, HCatMixUtils.getCopyTableName(tableName));
         } catch (Exception e) {
             LOG.info(MessageFormat.format("Couldn't delete table {0}.{1}. Ignored and proceeding", dbName, tableName), e);
         }
     }
 
     public void deletePigData() {
         // Delete the generated pig data
         final String pigData = HCatMixUtils.getPigOutputLocation(PIG_DATA_OUTPUT_DIR, dbName, tableName);
         LOG.info(MessageFormat.format("About to delete pig output directory: {0}", pigData));
         try {
             HCatMixHDFSUtils.deleteRecursive(pigData);
             LOG.info(MessageFormat.format("Deleted pig output directory: {0}", pigData));
         } catch (IOException e) {
             LOG.error(MessageFormat.format("Could not delete directory: {0}. Ignored proceeding",
                     pigData), e);
         }
     }
 
     /**
      * Delete generated input data
      */
     public void deleteGeneratedDataDir() {
         final String dataDir = HCatMixUtils.getDataLocation(DATAGEN_OUTPUT_DIR, tableName);
         try {
             HCatMixHDFSUtils.deleteRecursive(dataDir);
         } catch (IOException e) {
             LOG.error("Could not delete directory: " + dataDir);
         }
     }
 
     public GroupedTimingStatistics getTimedStats() {
         return timedStats;
     }
 
     public static class PigProgressListener implements PigProgressNotificationListener {
         private final int expectedNumRecords;
 
         public PigProgressListener(int expectedNumRecords) {
             this.expectedNumRecords = expectedNumRecords;
         }
 
         @Override
         public void launchStartedNotification(String scriptId, int numJobsToLaunch) {
            LOG.info(MessageFormat.format("{0}: number of jobs to launch: {1}", scriptId, numJobsToLaunch));
         }
 
         @Override
         public void jobsSubmittedNotification(String scriptId, int numJobsSubmitted) {
             LOG.info(MessageFormat.format("{0}: Number of job submitted: {1}", scriptId, numJobsSubmitted));
         }
 
         @Override
         public void jobStartedNotification(String scriptId, String assignedJobId) {
             LOG.info(MessageFormat.format("{0}: Hadoop job ID: {1}", scriptId, assignedJobId));
         }
 
         @Override
         public void jobFinishedNotification(String scriptId, JobStats jobStats) {
             LOG.info(MessageFormat.format("{0}: Avg map time: {1}", scriptId, jobStats.getAvgMapTime()));
         }
 
         @Override
         public void jobFailedNotification(String scriptId, JobStats jobStats) {
             Assert.fail(MessageFormat.format("{0}: Hadoop job ID: {1} failed", scriptId, jobStats.getJobId()));
         }
 
         @Override
         public void outputCompletedNotification(String scriptId, OutputStats outputStats) {
             assertEquals("Expected number of records were not written",
                     expectedNumRecords, outputStats.getNumberRecords());
         }
 
         @Override
         public void progressUpdatedNotification(String scriptId, int progress) {
            LOG.info(MessageFormat.format("{0}: Progress: {1}", scriptId, progress));
 
         }
 
         @Override
         public void launchCompletedNotification(String scriptId, int numJobsSucceeded) {
             LOG.info(MessageFormat.format("{0}: Launch completed: {1}", scriptId, numJobsSucceeded));
 
         }
     }
 }
