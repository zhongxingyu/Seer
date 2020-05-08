 // BridgeDb,
 // An abstraction layer for identifier mapping services, both local and online.
 //
 // Copyright 2006-2009  BridgeDb developers
 // Copyright 2012-2013  Christian Y. A. Brenninkmeijer
 // Copyright 2012-2013  OpenPhacts
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package uk.ac.manchester.cs.openphacts.ims.loader.transative;
 
 import java.io.File;
 import org.bridgedb.sql.SQLUriMapper;
 import org.bridgedb.sql.TestSqlFactory;
 import org.bridgedb.statistics.MappingSetInfo;
 import org.bridgedb.utils.BridgeDBException;
 import org.bridgedb.utils.ConfigReader;
 import org.bridgedb.utils.Reporter;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 import uk.ac.manchester.cs.openphacts.ims.loader.Loader;
 import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfFactory;
 import uk.ac.manchester.cs.datadesc.validator.rdftools.RdfReader;
 import uk.ac.manchester.cs.datadesc.validator.rdftools.VoidValidatorException;
 
 /**
  *
  * @author Christian
  */
 public class TransativeTestBase  {
     
     static SQLUriMapper uriListener;
     static RdfReader reader;
     static Loader instance;
 
     @BeforeClass
     public static void setUpClass() throws BridgeDBException, VoidValidatorException {
         ConfigReader.useTest();
         TestSqlFactory.checkSQLAccess();
         uriListener = SQLUriMapper.createNew();
        instance = new Loader();
         reader = RdfFactory.getTestFilebase();
     }
     
     protected void loadFile(String fileName) throws BridgeDBException, VoidValidatorException{
         File file = new File(fileName);
         loadFile(file);
     }
     
     protected void loadFile(File file) throws BridgeDBException, VoidValidatorException{
         Reporter.println("parsing " + file.getAbsolutePath());
         int mappingSetId = instance.load(file);
         MappingSetInfo mapping = uriListener.getMappingSetInfo(mappingSetId);
         int numberOfLinks = mapping.getNumberOfLinks();
         assertThat(numberOfLinks, greaterThanOrEqualTo(3));      
     }
     
 }
