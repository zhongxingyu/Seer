 package com.zenika.cudf.parser;
 
 import com.zenika.cudf.model.Binary;
 import com.zenika.cudf.model.CUDFDescriptor;
 import com.zenika.cudf.model.Preamble;
 import com.zenika.cudf.model.Request;
 import com.zenika.cudf.parser.mock.MockDeserializer;
 import org.junit.Test;
 
 import java.util.Set;
 
import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * @author Antoine Rouaze <antoine.rouaze@zenika.com>
  */
 public class TestDeserializer extends AbstractTestParser {
 
     @Test
     public void testDeserializer() throws Exception {
         AbstractDeserializer deserializer = new MockDeserializer(createParsedDescriptor());
         CUDFDescriptor actualDescriptor = deserializer.deserialize();
         CUDFDescriptor expectedDescriptor = createDescriptor();
 
         assertNotNull(actualDescriptor);
 
         assertPreamble(expectedDescriptor.getPreamble(), actualDescriptor.getPreamble());
         assertBinaries(expectedDescriptor.getPackages(), actualDescriptor.getPackages());
         assertRequest(expectedDescriptor.getRequest(), actualDescriptor.getRequest());
     }
 
     private void assertPreamble(Preamble expectedPreamble, Preamble actualPreamble) {
         assertEquals(expectedPreamble.getProperties(), actualPreamble.getProperties());
         assertEquals(expectedPreamble.getReqChecksum(), actualPreamble.getReqChecksum());
         assertEquals(expectedPreamble.getStatusChecksum(), actualPreamble.getStatusChecksum());
         assertEquals(expectedPreamble.getUnivChecksum(), actualPreamble.getUnivChecksum());
     }
 
     private void assertBinaries(Set<Binary> expectedBinaries, Set<Binary> actualBinaries) {
         Binary actualBinary1 = findBinaryByBinaryId(binaryId1, actualBinaries);
         Binary actualBinary2 = findBinaryByBinaryId(binaryId2, actualBinaries);
         Binary actualBinary3 = findBinaryByBinaryId(binaryId3, actualBinaries);
 
         Binary expectedBinary1 = findBinaryByBinaryId(binaryId1, expectedBinaries);
         Binary expectedBinary2 = findBinaryByBinaryId(binaryId2, expectedBinaries);
         Binary expectedBinary3 = findBinaryByBinaryId(binaryId3, expectedBinaries);
 
         assertBinary(expectedBinary1, actualBinary1);
         assertBinary(expectedBinary2, actualBinary2);
         assertBinary(expectedBinary3, actualBinary3);
     }
 
     private void assertBinary(Binary expectedBinary, Binary actualBinary) {
         assertEquals(expectedBinary.getBinaryId(), actualBinary.getBinaryId());
         assertEquals(expectedBinary.getRevision(), actualBinary.getRevision());
         assertEquals(expectedBinary.getType(), actualBinary.getType());
         assertEquals(expectedBinary.isInstalled(), actualBinary.isInstalled());
         assertEquals(expectedBinary.getDependencies(), actualBinary.getDependencies());
     }
 
     private void assertRequest(Request expectedRequest, Request actualRequest) {
         assertEquals(expectedRequest.getInstall(), actualRequest.getInstall());
         assertEquals(expectedRequest.getRemove(), actualRequest.getRemove());
         assertEquals(expectedRequest.getUpdate(), actualRequest.getUpdate());
     }
 }
