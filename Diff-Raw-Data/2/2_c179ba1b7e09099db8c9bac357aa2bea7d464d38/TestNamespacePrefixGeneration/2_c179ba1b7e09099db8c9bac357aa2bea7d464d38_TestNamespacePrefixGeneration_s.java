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
 
 import gov.lexs.v4_0.JAXBUtils;
 import java.io.File;
 import java.io.StringWriter;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * FIXME Describe this type.
  * <br/><br/>
  * @author brad
  * @date Dec 3, 2010
  */
 public class TestNamespacePrefixGeneration extends AbstractTest {
     //==========================================================================
     //  Test Contstants
     //==========================================================================
    public static final String TEST_FILE = "./src/test/resources/lexs-4.0-samples/PD-samples/doPublish-all-fields.xml";
     //==========================================================================
     //  Test Helper Methods
     //==========================================================================
 
     //==========================================================================
     //  Tests
     //==========================================================================
     @Test
     public void testNamespacePrefixBindings() throws Exception {
         logger.info("Testing that JAXBUtils loads up 'default' namespace prefix bindings...");
         File testFile = new File(TEST_FILE);
         assertTrue(testFile.exists());
         logger.debug("Unmarshalling file to object...");
         Object object = JAXBUtils.unmarshal(testFile);
 
         StringWriter writer = new StringWriter();
         logger.debug("Marshalling back to a String...");
         JAXBUtils.marshall(object, writer);
         String marshalledXML = writer.toString();
 
         logger.debug("Comparing namespaces...");
         assertTrue( marshalledXML.contains("<ulex:") );
         assertTrue( marshalledXML.contains("<ulexpd:") );
         // assertTrue( marshalledXML.contains("<ulexcodes:") );
         assertTrue( marshalledXML.contains("<ulexlib:") );
         assertTrue( marshalledXML.contains("<j:") );
         assertTrue( marshalledXML.contains("<nc:") );
         assertTrue( marshalledXML.contains("s:id") );
         assertTrue( marshalledXML.contains("<lexs:") );
         assertTrue( marshalledXML.contains("<lexsdigest:") );
         assertTrue( marshalledXML.contains("<em:") );
         assertTrue( marshalledXML.contains("<im:") );
         assertTrue( marshalledXML.contains("<intel:") );
         assertTrue( marshalledXML.contains("<fs:") );
         assertTrue( marshalledXML.contains("<m:") );
         assertTrue( marshalledXML.contains("<scr:") );
         assertTrue( marshalledXML.contains("<wsa:") );
         assertTrue( marshalledXML.contains("<icism-metadata:") );
         assertTrue( marshalledXML.contains("icism:classification") );
 //        assertTrue( marshalledXML.contains("<xsi:") );
 
         assertFalse( marshalledXML.contains("<ns1:") );
 
         logger.info("JAXBUtils successfully output 'default' namespace prefix bindings!");
     }//end testNamespacePrefixBindings()
 
 
 }/* end class TestNamespacePrefixGeneration */
