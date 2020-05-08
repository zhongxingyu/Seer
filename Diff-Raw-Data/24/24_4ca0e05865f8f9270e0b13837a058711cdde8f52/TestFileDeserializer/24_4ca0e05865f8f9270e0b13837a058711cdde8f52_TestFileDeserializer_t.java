 package com.zenika.cudf.parser;
 
 import com.zenika.cudf.model.BinaryId;
 import com.zenika.cudf.parser.model.CUDFParsedDescriptor;
 import com.zenika.cudf.parser.model.ParsedBinary;
 import com.zenika.cudf.parser.model.ParsedPreamble;
 import com.zenika.cudf.parser.model.ParsedRequest;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Antoine Rouaze <antoine.rouaze@zenika.com>
  */
 public class TestFileDeserializer {
 
     @Test
     public void testFileDeserializer() throws Exception {
        FileDeserializer fileDeserializer = new FileDeserializer(new File("src/test/resources/com/zenika/cudf/resource/test.cudf"));
         CUDFParsedDescriptor parsedDescriptor = fileDeserializer.parseCudf();
 
         ParsedPreamble expectedParsedPreamble = parsedDescriptor.getPreamble();
         assertParsedPreamble(expectedParsedPreamble);
 
         Set<ParsedBinary> expectedParsedBinaries = parsedDescriptor.getPackages();
         assertParsedBinaries(expectedParsedBinaries);
 
         ParsedRequest expectedParsedRequest = parsedDescriptor.getRequest();
         assertParsedRequest(expectedParsedRequest);
 
     }
 
     private void assertParsedPreamble(ParsedPreamble expectedParsedPreamble) {
         Map<String, String> expectedProperties = new HashMap<String, String>();
         expectedProperties.put("number", "string");
         expectedProperties.put("recommends", "vpkgformula = [true!]");
         expectedProperties.put("suggests", "vpkglist = []");
         expectedProperties.put("type", "string = [\"\"]");
         assertEquals(expectedProperties, expectedParsedPreamble.getProperties());
         assertNull(expectedParsedPreamble.getReqChecksum());
         assertNull(expectedParsedPreamble.getStatusChecksum());
         assertNull(expectedParsedPreamble.getUnivChecksum());
     }
 
     private void assertParsedBinaries(Set<ParsedBinary> expectedParsedBinaries) {
         ParsedBinary expectedParsedBinary = null;
         assertEquals(3, expectedParsedBinaries.size());
         for (ParsedBinary parsedBinary : expectedParsedBinaries) {
             if (parsedBinary.getBinaryId().equals(new BinaryId("jar1", "zenika", 1))) {
                 expectedParsedBinary = parsedBinary;
             }
         }
         assertNotNull(expectedParsedBinary);
         assertEquals("1.0", expectedParsedBinary.getRevision());
         assertEquals("jar", expectedParsedBinary.getType());
         Set<BinaryId> expectedParsedBinaryIds = expectedParsedBinary.getDependencies();
         assertEquals(2, expectedParsedBinaryIds.size());
         assertTrue(expectedParsedBinaryIds.contains(new BinaryId("jar2", "zenika", 1)));
         assertTrue(expectedParsedBinaryIds.contains(new BinaryId("jar3", "zenika", 2)));
     }
 
     private void assertParsedRequest(ParsedRequest expectedParsedRequest) {
         assertEquals(1, expectedParsedRequest.getInstall().size());
         assertTrue(expectedParsedRequest.getInstall().contains(new BinaryId("jar1", "zenika", 1)));
     }
 }
