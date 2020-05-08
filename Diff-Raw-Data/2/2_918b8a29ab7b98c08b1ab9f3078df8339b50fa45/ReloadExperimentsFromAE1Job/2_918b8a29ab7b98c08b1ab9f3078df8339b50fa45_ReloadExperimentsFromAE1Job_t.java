 package uk.ac.ebi.arrayexpress.jobs;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.quartz.JobListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.ApplicationJob;
 import uk.ac.ebi.arrayexpress.components.*;
 import uk.ac.ebi.arrayexpress.components.Experiments.UpdateSourceInformation;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.db.ArrayXmlDatabaseRetriever;
 import uk.ac.ebi.arrayexpress.utils.db.ExperimentListDatabaseRetriever;
 import uk.ac.ebi.arrayexpress.utils.db.IConnectionSource;
 import uk.ac.ebi.arrayexpress.utils.db.UserXmlDatabaseRetriever;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class ReloadExperimentsFromAE1Job extends ApplicationJob implements JobListener
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private List<Long> exps;
     private IConnectionSource connectionSource;
     private StringBuffer xmlBuffer;
 
     private AtomicInteger numThreadsCompleted;
     private int expsPerThread;
 
     public void doExecute( JobExecutionContext jec ) throws Exception
     {
         String usersXml = null;
         String arrayDesignsXml = null;
         String experimentsXml = null;
 
         // kicks reload of atlas experiments just in case
         ((JobsController) getComponent("JobsController")).executeJob("reload-atlas-info");
 
         try {
             // check preferences and if source location is defined, use that
             String sourceLocation = getPreferences().getString("ae.experiments.ae1.source-location");
             UpdateSourceInformation sourceInformation = null;
             if (!"".equals(sourceLocation)) {
                 logger.info("Reload of experiment data from [{}] requested", sourceLocation);
                 usersXml = getXmlFromFile(new File(sourceLocation, "users.xml"));
                 arrayDesignsXml = getXmlFromFile(new File(sourceLocation, "arrays.xml"));
                 File experimentsSourceFile = new File(sourceLocation, "experiments.xml");
                 experimentsXml = getXmlFromFile(experimentsSourceFile);
                 sourceInformation = new UpdateSourceInformation(Experiments.ExperimentSource.AE1, experimentsSourceFile);
 
             } else {
                 // check if we have available database connection
                 DbConnectionPool dbConnectionPool = (DbConnectionPool) getComponent("DbConnectionPool");
 
                 if (null != dbConnectionPool) {
                     JobDataMap jdm = jec.getMergedJobDataMap();
                     String connNames = jdm.getString("connections");
                     if (null == connNames || 0 == connNames.length()) {
                         connNames = getPreferences().getString("ae.experiments.ae1.db-connections");
                     }
 
                     logger.info("Reload of experiment data from connection(s) [{}] requested", connNames);
 
                     // a special case: if datasource is "rawfile" use a temporary file instead, don't reload users
                     if ("rawfile".equals(connNames)) {
                         experimentsXml = getXmlFromFile(
                                 new File(
                                         System.getProperty("java.io.tmpdir")
                                         , "ae1-raw-experiments.xml"
                                 )
                         );
                     } else {
                         this.connectionSource = dbConnectionPool.getConnectionSource(connNames);
                     }
 
                 }
 
                 if (null != this.connectionSource) {
                     try {
                         usersXml = getUsersXmlFromDb();
                         arrayDesignsXml = getArrayDesignsXmlFromDb();
                         experimentsXml = getExperimentsXmlFromDb();
                         sourceInformation = new UpdateSourceInformation(
                                 Experiments.ExperimentSource.AE1
                                 , this.connectionSource.getName()
                                 , new Date().getTime()
                         );
                     } finally {
                         this.connectionSource.close();
                         this.connectionSource = null;
                     }
 
                 } else {
                     logger.error("No database connection available");
                 }
             }
 
             // now, if we need to export, do it now
             String exportLocation = getPreferences().getString("ae.experiments.ae1.export-location");
             if (!"".equals(exportLocation)) {
                 logger.info("Export of experiment data to [{}] requested", exportLocation);
                 StringTools.stringToFile(usersXml, new File(exportLocation, "users.xml"));
                 StringTools.stringToFile(arrayDesignsXml, new File(exportLocation, "arrays.xml"));
                 StringTools.stringToFile(experimentsXml, new File(exportLocation, "experiments.xml"));
             }
 
             // export to temp directory anyway (only if debug is enabled)
             if (logger.isDebugEnabled() && null != experimentsXml) {
                 StringTools.stringToFile(
                         experimentsXml
                         , new File(
                                 System.getProperty("java.io.tmpdir")
                                 , "ae1-raw-experiments.xml"
                         )
                 );
             }
 
             if (null != usersXml && !"".equals(usersXml)) {
                 updateUsers(usersXml);
             }
 
             if (null != arrayDesignsXml && !"".equals(arrayDesignsXml)) {
                 updateArrayDesigns(arrayDesignsXml);
             }
 
             if (null != experimentsXml && !"".equals(experimentsXml)) {
                 updateExperiments(experimentsXml, sourceInformation);
             }
 
         } catch (Exception x) {
             throw new RuntimeException(x);
         }
     }
 
     private String getXmlFromFile(File xmlFile) throws Exception
     {
         logger.info("Getting XML from file [{}]", xmlFile);
         return StringTools.fileToString(
                 xmlFile
                 , "UTF-8"
         );
     }
 
     private void updateUsers( String xmlString ) throws Exception
     {
         ((Users) getComponent("Users")).update(xmlString, Users.UserSource.AE1);
 
         logger.info("User information reload completed");
 
     }
 
     private void updateExperiments( String xmlString, UpdateSourceInformation sourceInformation ) throws Exception
     {
         ((Experiments) getComponent("Experiments")).update(
                 xmlString
                 , sourceInformation
         );
 
         logger.info("Experiment information reload completed");
 
     }
 
     private void updateArrayDesigns( String xmlString ) throws Exception
     {
         ((ArrayDesigns) getComponent("ArrayDesigns")).update(
                 xmlString
                 , ArrayDesigns.ArrayDesignSource.AE1
         );
 
         logger.info("Platform design information reload completed");
 
     }
 
     private String getUsersXmlFromDb() throws Exception
     {
         return new UserXmlDatabaseRetriever(this.connectionSource).getXml();
     }
 
     private String getArrayDesignsXmlFromDb() throws Exception
     {
         return new ArrayXmlDatabaseRetriever(this.connectionSource).getXml();
     }
 
     private String getExperimentsXmlFromDb() throws Exception
     {
         String experimentsXml = null;
         Long threads = getPreferences().getLong("ae.experiments.ae1.reload.threads");
         if (null != threads) {
             int numThreadsForRetrieval = threads.intValue();
             numThreadsCompleted = new AtomicInteger();
 
             exps = new ExperimentListDatabaseRetriever(connectionSource).getExperimentList();
             Thread.sleep(1);
 
             logger.info("Got [{}] experiments listed in the database, scheduling retrieval", exps.size());
             xmlBuffer = new StringBuffer(20000000);
             xmlBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                     .append("<experiments total=\"").append(exps.size()).append("\">")
                     ;
 
             ((JobsController) getComponent("JobsController")).setJobListener(this);
 
             if (exps.size() > 0) {
                 if (exps.size() <= numThreadsForRetrieval) {
                     numThreadsForRetrieval = 1;
                 }
                 // split list into several pieces
                 expsPerThread = (int) Math.ceil(((double) exps.size()) / ((double) numThreadsForRetrieval));
                 for (int i = 0; i < numThreadsForRetrieval; ++i) {
                     ((JobsController) getComponent("JobsController")).executeJobWithParam("retrieve-xml", "index", String.valueOf(i));
                     Thread.sleep(1);
                 }
 
                 while (numThreadsCompleted.get() < numThreadsForRetrieval) {
                     Thread.sleep(1000);
                 }
 
                 ((JobsController) getComponent("JobsController")).setJobListener(null);
                 xmlBuffer.append("</experiments>");
 
                 experimentsXml = xmlBuffer.toString();
 
                 if (logger.isDebugEnabled()) {
                     StringTools.stringToFile(
                             experimentsXml
                             , new File(
                                     System.getProperty("java.io.tmpdir")
                                     , "ae1-raw-experiments.txt"
                             )
                     );
                 }
 
                 experimentsXml = StringTools.replaceIllegalHTMLCharacters(  // filter out all junk Unicode chars
                         StringTools.unescapeXMLDecimalEntities(             // convert &#dddd; entities to their Unicode values
                                 StringTools.detectDecodeUTF8Sequences(      // attempt to intelligently convert UTF-8 to Unicode
                                         experimentsXml
                                 ).replaceAll("&amp;#(\\d+);", "&#$1;")      // transform &amp;#dddd; -> &#dddd;
                         )
                 );
             }
         }
     return experimentsXml;
     }
 
     // jobListener support
     public String getName()
     {
         return "job-listener";
     }
 
     public void jobToBeExecuted( JobExecutionContext jec )
     {
         if (jec.getJobDetail().getName().equals("retrieve-xml")) {
             JobDataMap jdm = jec.getMergedJobDataMap();
             int index = Integer.parseInt(jdm.getString("index"));
             jdm.put("xmlBuffer", xmlBuffer);
             jdm.put("connectionSource", connectionSource);
             jdm.put("exps", exps.subList(index * expsPerThread, Math.min(((index + 1) * expsPerThread), exps.size())));
         }
     }
 
     public void jobExecutionVetoed( JobExecutionContext jec )
     {
         if (jec.getJobDetail().getName().equals("retrieve-xml")) {
             try {
                 interrupt();
             } catch (Exception x) {
                 logger.error("Caught an exception:", x);
             }
         }
     }
 
     public void jobWasExecuted( JobExecutionContext jec, JobExecutionException jobException )
     {
         if (jec.getJobDetail().getName().equals("retrieve-xml")) {
             JobDataMap jdm = jec.getMergedJobDataMap();
            jdm.remove("xmlBuffer");
             jdm.remove("connectionSource");
             jdm.remove("exps");
 
             numThreadsCompleted.incrementAndGet();
         }
     }
 }
