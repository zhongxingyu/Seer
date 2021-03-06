 /**
  *  Copyright 2012 Sven Ewald
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.xmlbeam.tests;
 
 import static org.junit.Assert.assertEquals;
 
import java.util.Scanner;

 import java.io.IOException;
 
 import org.junit.Test;
 import org.xmlbeam.DocumentURL;
 import org.xmlbeam.XBProjector;
 
 /**
  * Tests to ensure the function of toString(), equals() and hashCode() for
  * projections.
  * 
  * @author sven
  * 
  */
 public class TestObjectInvoker {
 
     @Test
     public void testToString() throws IOException {
        XMLBeamTestSuite testSuite = new XBProjector().read().fromURLAnnotation(XMLBeamTestSuite.class);
        testSuite.toString();
        String orig = new Scanner(TestObjectInvoker.class.getResourceAsStream(XMLBeamTestSuite.class.getAnnotation(DocumentURL.class).value().substring("resource://".length()))).useDelimiter("\\A").next();
         assertEquals(orig.replaceAll("\\s", ""), testSuite.toString().replaceAll("\\s", ""));
     }
 }
