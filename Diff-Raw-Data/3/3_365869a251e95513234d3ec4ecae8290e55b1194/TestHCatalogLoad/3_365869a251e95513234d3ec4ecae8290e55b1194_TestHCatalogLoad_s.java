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
 
 package org.apache.hcatalog.hcatmix.load;
 
 import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.hcatalog.hcatmix.HCatMixUtils;
 import org.apache.hcatalog.hcatmix.load.hadoop.ReduceResult;
 import org.apache.hcatalog.hcatmix.load.tasks.HCatLoadTask;
 import org.apache.hcatalog.hcatmix.loadstore.LoadStoreScriptRunner;
 import org.apache.hcatalog.hcatmix.publisher.LoadTestResultsPublisher;
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.*;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedMap;
 
 import static org.testng.Assert.assertNotNull;
 import static org.testng.Assert.fail;
 
 public class TestHCatalogLoad {
     private static final Logger LOG = LoggerFactory.getLogger(TestHCatalogLoad.class);
     private LoadStoreScriptRunner loadStoreScriptRunner;
     private static final String LOAD_TEST_CONF_FILE_ARG_NAME = "loadTestConfFile";
     private static String resultsDir;
 
     @DataProvider(name = "LoadTestConfFiles")
     public Iterator<Object[]> loadTestConfFiles() {
         final String loadTestFiles = System.getProperty(LOAD_TEST_CONF_FILE_ARG_NAME);
         final List<Object[]> loadTestConfFiles = new ArrayList<Object[]>();
 
         if (loadTestFiles == null) {
             ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
             final String loadTestConfDirName = classLoader.getResource("load").getPath();
             LOG.info("Will look in directory:" + loadTestConfDirName + " for load test configuration files");
             File loadTestConfDir = new File(loadTestConfDirName);
 
             assertNotNull(loadTestConfDir.listFiles());
             FilenameFilter propertiesFilter = new FilenameFilter() {
                 @Override
                 public boolean accept(File dir, String name) {
                     return name.endsWith(".properties");
                 }
             };
             for (File file : loadTestConfDir.listFiles(propertiesFilter)) {
                 loadTestConfFiles.add(new Object[]{file.getAbsolutePath()});
             }
         } else {
             LOG.info(MessageFormat.format("Honouring command line option: -D{0}={1}", LOAD_TEST_CONF_FILE_ARG_NAME, loadTestFiles));
             loadTestConfFiles.add(loadTestFiles.split(","));
         }
         return loadTestConfFiles.iterator();
     }
 
     @BeforeClass
     public static void setupResultsDirectory() {
         resultsDir = HCatMixUtils.getTempDirName() + "/results/loadtest/";
         File resultsDirObj = new File(resultsDir);
         if(!resultsDirObj.mkdirs()) {
             fail("Could not setup results directory: " + resultsDir);
         }
         LOG.info("Created results directory: " + resultsDirObj.getAbsolutePath());
 
 //        loadStoreTestResults = new LoadStoreTestResults(resultsDir + "/" + RESULTS_ALL_HTML,
 //                resultsDir + "/" + RESULTS_ALL_JSON);
     }
     @BeforeTest
     public void setUp() throws MetaException, IOException, TException, NoSuchObjectException, SAXException, InvalidObjectException, ParserConfigurationException {
         URL url = Thread.currentThread().getContextClassLoader().getResource(HCatLoadTask.LOAD_TEST_HCAT_SPEC_FILE);
         if(url == null) {
             LOG.error(HCatLoadTask.LOAD_TEST_HCAT_SPEC_FILE + " not found");
             throw new RuntimeException(HCatLoadTask.LOAD_TEST_HCAT_SPEC_FILE + " not found");
         }
 
         String hcatTableSpecFile = url.getPath();
 
         loadStoreScriptRunner = new LoadStoreScriptRunner(hcatTableSpecFile, 1);
         loadStoreScriptRunner.setUp(false);
         loadStoreScriptRunner.runPigLoadHCatStoreScript();
     }
 
     @Test(dataProvider = "LoadTestConfFiles")
     public void doLoadTest(String confFile) throws Exception {
         HadoopLoadGenerator loadGenerator = new HadoopLoadGenerator();
         SortedMap<Long, ReduceResult> results =  loadGenerator.runLoadTest(confFile, null);
        LoadTestResultsPublisher publisher = new LoadTestResultsPublisher(results, resultsDir + "/" + confFile + ".html");
         publisher.publishAll();
     }
 
     @AfterTest
     public void tearDown() throws NoSuchObjectException, MetaException, TException {
         loadStoreScriptRunner.deleteHCatTables();
     }
 }
