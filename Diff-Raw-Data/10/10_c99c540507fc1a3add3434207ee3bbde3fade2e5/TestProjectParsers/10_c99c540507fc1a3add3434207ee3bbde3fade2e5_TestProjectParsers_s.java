 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.gagravarr.tika;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.util.Set;
 
 import org.apache.tika.config.TikaConfig;
 import org.apache.tika.metadata.DublinCore;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.metadata.TikaMetadataKeys;
 import org.apache.tika.mime.MediaType;
 import org.apache.tika.parser.AutoDetectParser;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.parser.Parser;
 import org.apache.tika.sax.BodyContentHandler;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.xml.sax.ContentHandler;
 
 @RunWith(JUnit4.class)
 public class TestProjectParsers {
     public static MediaType MPP_TYPE = MediaType.application("vnd.ms-project");
     public static MediaType MPX_TYPE = MediaType.application("x-project");
     
     @Test
     public void BasicProcessing() throws Exception {
         InputStream mpx = getTestFile("testPROJECT.mpx", null);
         InputStream mpp2003 = getTestFile("testPROJECT2003.mpp", null);
         InputStream mpp2007 = getTestFile("testPROJECT2007.mpp", null);
         
         MPPParser mppParser = new MPPParser();
         MPXParser mpxParser = new MPXParser();
         ContentHandler handler = new BodyContentHandler(); 
         
         // Call Tika on the mpx file
         mpxParser.parse(mpx, handler, new Metadata(), new ParseContext());
         
         // Call Tika on the 2003 file
         mppParser.parse(mpp2003, handler, new Metadata(), new ParseContext());
         
         // Call Tika on the 2007 file
         mppParser.parse(mpp2007, handler, new Metadata(), new ParseContext());
     }
     
     @Test
     public void AutoDetectParsing() throws Exception {
         Set<MediaType> types;
         
         // Check the parsers claims to support the right things
         MPPParser mppParser = new MPPParser();
         types = mppParser.getSupportedTypes(new ParseContext());
         assertTrue("Not found in " + types, types.contains(MPP_TYPE));
         assertFalse("Shouldn't be in " + types, types.contains(MPX_TYPE));
         
         MPXParser mpxParser = new MPXParser();
         types = mpxParser.getSupportedTypes(new ParseContext());
         assertTrue("Not found in " + types, types.contains(MPX_TYPE));
         assertFalse("Shouldn't be in " + types, types.contains(MPP_TYPE));
         
         // Check that the Default Parser claims to support them both
         Parser baseParser = TikaConfig.getDefaultConfig().getParser();
         types = baseParser.getSupportedTypes(new ParseContext());
         assertTrue("Not found in " + types, types.contains(MPP_TYPE));
         assertTrue("Not found in " + types, types.contains(MPX_TYPE));
         Parser autoDetectParser = new AutoDetectParser(baseParser);
 
         
         // Check the parsing works
         Metadata metadata;
         ContentHandler handler = new BodyContentHandler(); 
         
         
         // Ask for a MPP to be processed
         metadata = new Metadata();
         InputStream mpp2003 = getTestFile("testPROJECT2003.mpp", metadata);
         autoDetectParser.parse(mpp2003, handler, metadata, new ParseContext());
 
         // Check it worked
         assertEquals(
                 "The quick brown fox jumps over the lazy dog", 
                 metadata.get(DublinCore.TITLE)
         );
        assertTrue(handler.toString().contains("Fox does his jump"));
        assertTrue(handler.toString().contains("Obtain Dog"));
        assertTrue(handler.toString().contains("from 2011-11-25T08:00:00"));
        assertTrue(handler.toString().contains("to 2011-11-24T17:00:00"));
        assertTrue(handler.toString().contains("taking 1 Day"));
         
         
         // Ask for a MPX to be processed
         metadata = new Metadata();
         handler = new BodyContentHandler();
         InputStream mpx = getTestFile("testPROJECT.mpx", metadata);
         autoDetectParser.parse(mpx, handler, metadata, new ParseContext());
 
         // Check it worked (Note - no metadata for MPX)
         assertTrue(handler.toString().contains("Fox does his jump"));
         assertTrue(handler.toString().contains("Obtain Dog"));
 //        assertContains("from 2011-11-25T08:00:00", handler.toString());
 //        assertContains("to 2011-11-24T17:00:00", handler.toString());
         assertContains("from 2011-11-25T", handler.toString());
         assertContains("to 2011-11-24T", handler.toString());
         assertContains("taking 1 Day", handler.toString());
     }
     
     protected static InputStream getTestFile(String name, Metadata metadata) throws Exception {
         InputStream s = TestProjectParsers.class.getResourceAsStream("/test-files/" + name);
         assertNotNull("Test file not found: " + name, s);
 
         if (metadata != null) {
             metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, name);
         }
         return s;
     }
 
     protected static void assertContains(String needle, String haystack) {
         assertTrue("'" + needle + "' not found in:\n" + haystack, haystack.contains(needle));
     }
 }
