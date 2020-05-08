 package com.zenika.cudf.parser;
 
 import com.zenika.cudf.model.Binary;
 import com.zenika.cudf.model.BinaryId;
 import com.zenika.cudf.model.CUDFDescriptor;
 import com.zenika.cudf.model.Preamble;
 import com.zenika.cudf.model.Request;
 import com.zenika.cudf.parser.mock.MockSerializer;
 import com.zenika.cudf.parser.model.CUDFParsedDescriptor;
 import com.zenika.cudf.parser.model.ParsedBinary;
 import com.zenika.cudf.parser.model.ParsedPreamble;
 import com.zenika.cudf.parser.model.ParsedRequest;
 import org.junit.Test;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Antoine Rouaze <antoine.rouaze@zenika.com>
  */
 public class TestSerializer extends AbstractTestParser {
 
     @Test
     public void testSerializer() throws Exception {
         MockSerializer serializer = new MockSerializer();
 
         CUDFDescriptor descriptor = createDescriptor();
         serializer.serialize(descriptor);
 
         CUDFParsedDescriptor parsedDescriptor = serializer.getParsedDescriptor();
         assertNotNull(parsedDescriptor);
 
         assertParsedPreamble(parsedDescriptor);
         assertParsedBinaries(parsedDescriptor);
         assertParsedRequest(parsedDescriptor);
     }
 
     private void assertParsedPreamble(CUDFParsedDescriptor parsedDescriptor) {
         ParsedPreamble actualParsedPreamble = parsedDescriptor.getPreamble();
         ParsedPreamble expectedParsedPreamble = createExpectedParsedPreamble();
 
         assertParsedPreamble(actualParsedPreamble, expectedParsedPreamble);
     }
 
     private void assertParsedPreamble(ParsedPreamble actualParsedPreamble, ParsedPreamble expectedParsedPreamble) {
         assertEquals(expectedParsedPreamble.getProperties(), actualParsedPreamble.getProperties());
         assertEquals(expectedParsedPreamble.getReqChecksum(), actualParsedPreamble.getReqChecksum());
         assertEquals(expectedParsedPreamble.getStatusChecksum(), actualParsedPreamble.getStatusChecksum());
         assertEquals(expectedParsedPreamble.getUnivChecksum(), actualParsedPreamble.getUnivChecksum());
     }
 
     private void assertParsedRequest(CUDFParsedDescriptor parsedDescriptor) {
         ParsedRequest actualParsedRequest = parsedDescriptor.getRequest();
         ParsedRequest expectedParsedRequest = createExpectedParsedRequest();
 
         assertParsedRequest(actualParsedRequest, expectedParsedRequest);
     }
 
     private void assertParsedRequest(ParsedRequest actualParsedRequest, ParsedRequest expectedParsedRequest) {
         assertEquals(expectedParsedRequest.getInstall(), actualParsedRequest.getInstall());
         assertEquals(expectedParsedRequest.getRemove(), actualParsedRequest.getRemove());
         assertEquals(expectedParsedRequest.getUpdate(), actualParsedRequest.getUpdate());
     }
 
     private void assertParsedBinaries(CUDFParsedDescriptor parsedDescriptor) {
         ParsedBinary expectedParsedBinary1 = createExpectedParsedBinary(binaryId1, "1.0", "jar", false);
         ParsedBinary expectedParsedBinary2 = createExpectedParsedBinary(binaryId2, "1.0.0", "jar", false);
         ParsedBinary expectedParsedBinary3 = createExpectedParsedBinary(binaryId3, "1.2-SNAPSHOT", "jar", true);
 
         ParsedBinary actualParsedBinary1 = findParsedBinaryByBinaryId(binaryId1, parsedDescriptor.getPackages());
         ParsedBinary actualParsedBinary2 = findParsedBinaryByBinaryId(binaryId2, parsedDescriptor.getPackages());
         ParsedBinary actualParsedBinary3 = findParsedBinaryByBinaryId(binaryId3, parsedDescriptor.getPackages());
 
         assertParsedBinary(expectedParsedBinary1, actualParsedBinary1);
         assertParsedBinary(expectedParsedBinary2, actualParsedBinary2);
         assertParsedBinary(expectedParsedBinary3, actualParsedBinary3);
 
         assertTrue(actualParsedBinary1.getDependencies().contains(binaryId2));
         assertTrue(actualParsedBinary1.getDependencies().contains(binaryId3));
     }
 
     private void assertParsedBinary(ParsedBinary expectedParsedBinary, ParsedBinary actualParsedBinary) {
         assertNotNull(actualParsedBinary);
         assertEquals(expectedParsedBinary.getRevision(), actualParsedBinary.getRevision());
         assertEquals(expectedParsedBinary.getType(), actualParsedBinary.getType());
         assertEquals(expectedParsedBinary.isInstalled(), actualParsedBinary.isInstalled());
     }
 
     private ParsedPreamble createExpectedParsedPreamble() {
         ParsedPreamble expectedParsedPreamble = new ParsedPreamble();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("key", "value");
         expectedParsedPreamble.setProperties(properties);
         expectedParsedPreamble.setReqChecksum("req");
         expectedParsedPreamble.setStatusChecksum("status");
         expectedParsedPreamble.setUnivChecksum("univ");
         return expectedParsedPreamble;
     }
 
     private ParsedBinary createExpectedParsedBinary(BinaryId binaryId, String revision, String type, boolean installed) {
         ParsedBinary parsedBinary = new ParsedBinary();
         parsedBinary.setBinaryId(binaryId);
         parsedBinary.setRevision(revision);
         parsedBinary.setInstalled(installed);
         parsedBinary.setType(type);
         return parsedBinary;
     }
 
     private ParsedRequest createExpectedParsedRequest() {
         ParsedRequest expectedParsedRequest = new ParsedRequest();
         expectedParsedRequest.getInstall().add(binaryId1);
         return expectedParsedRequest;
     }
 
     private CUDFDescriptor createDescriptor() {
         CUDFDescriptor descriptor = new CUDFDescriptor();
 
         Preamble preamble = createPreamble();
         Set<Binary> binaries = createBinaries(binaryId1, binaryId2, binaryId3);
         Request request = createRequest(findBinaryByBinaryId(binaryId1, binaries));
 
         descriptor.setPreamble(preamble);
         descriptor.setPackages(binaries);
         descriptor.setRequest(request);
 
         return descriptor;
     }
 
     private Preamble createPreamble() {
         Preamble preamble = new Preamble();
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("key", "value");
         preamble.setProperties(properties);
        preamble.setReqChecksum("reg");
         preamble.setStatusChecksum("status");
         preamble.setUnivChecksum("univ");
         return preamble;
     }
 
     private Set<Binary> createBinaries(BinaryId binaryId1, BinaryId binaryId2, BinaryId binaryId3) {
         Set<Binary> binaries = new HashSet<Binary>();
         Binary binary1 = createBinary(binaryId1, "1.0", "jar", false);
         Binary binary2 = createBinary(binaryId2, "1.0.0", "jar", false);
         Binary binary3 = createBinary(binaryId3, "1.2-SNAPSHOT", "jar", true);
 
         binary1.getDependencies().add(binary2);
         binary1.getDependencies().add(binary3);
 
         binaries.add(binary1);
         binaries.add(binary2);
         binaries.add(binary3);
         return binaries;
     }
 
     private Binary createBinary(BinaryId binaryId, String revision, String type, boolean installed) {
         Binary binary = new Binary(binaryId);
         binary.setInstalled(installed);
         binary.setRevision(revision);
         binary.setType(type);
         return binary;
     }
 
     private Request createRequest(Binary binary1) {
         Request request = new Request();
         request.getInstall().add(binary1);
         return request;
     }
 }
