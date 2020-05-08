 /*
  *  Copyright 2010 GTRI.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package gov.lexs.jaxb.tests;
 
 import gov.lexs.v4_0.SchemaProvider;
 import java.io.File;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * FIXME Describe this type.
  * <br/><br/>
  * @author brad
  * @date Dec 3, 2010
  */
 public class TestSchemaProvider extends AbstractTest {
     //==========================================================================
     //  Tests
     //==========================================================================
     @Test
     public void testValidationOfKnownValidFile() throws Exception  {
         logger.info("Testing that the schema validates a known valid file...");
 
         logger.debug("Creating schema...");
         Schema schema = SchemaProvider.createLexs40Schema();
         assertNotNull( schema );
 
        File file = new File("./src/test/resources/lexs-4.0-samples/PD-samples/doPublish-all-fields.xml");
         assertTrue( file.exists() );
 
         logger.debug("Calling validator...");
         schema.newValidator().validate(new StreamSource(file));
 
         logger.info("Successfully tested known valid file!");
     }//end testValidationOfKnownValidFile()
 
     @Test
     public void testValidationOfKnownInvalidFile() throws Exception  {
         logger.info("Testing that the schema errors on a known invalid file...");
 
         logger.debug("Creating schema...");
         Schema schema = SchemaProvider.createLexs40Schema();
         assertNotNull( schema );
 
         File file = new File("./src/test/resources/invalid.xml");
         assertTrue( file.exists() );
         try{
             logger.debug("Calling validator...");
             schema.newValidator().validate(new StreamSource(file));
             fail("Successfully validated a known invalid file.");
         }catch(Exception e){
             logger.debug("Successfully errored on: {}", e.toString());
         }
         logger.info("Successfully errored on known invalid file!");
     }//end testValidationOfKnownValidFile()
     
 }/* end class TestSchemaProvider */
