 package com.xetten.courses.coursera.crypto1.week3;
 
 import com.google.common.base.Joiner;
 import org.junit.Test;
 
 import java.net.URL;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * @author <a href="mailto:cristian.lungu@tora.com">Cristian Lungu</a>
  */
 public class ProgrammingAssignamentTest {
 
 
     @Test
     public void testVerificationResourceThere() throws Exception {
         URL url = getClass().getClassLoader().getResource("META-INF/week3/verification.mp4");
         assertNotNull(url);
     }
 
     @Test
     public void testChallengeResourceThere() throws Exception {
         URL url = getClass().getClassLoader().getResource("META-INF/week3/challenge.mp4");
         assertNotNull(url);
     }
 
     @Test
     public void testVerification() throws Exception {
         ChainSHA256 chainSHA256 = new ChainSHA256(
                 HashSHA256.newInstance(),
                 new FileReader(
                         getClass().getClassLoader()
                                 .getResource("META-INF/week3/verification.mp4")
                         ,
                         1024
                 )
         );
 
         byte[] expectedHash0 = new byte[]{(byte) 0x03, (byte) 0xc0, (byte) 0x8f, (byte) 0x4e, (byte) 0xe0, (byte) 0xb5, (byte) 0x76, (byte) 0xfe, (byte) 0x31, (byte) 0x93, (byte) 0x38, (byte) 0x13, (byte) 0x9c, (byte) 0x04, (byte) 0x5c, (byte) 0x89, (byte) 0xc3, (byte) 0xe8, (byte) 0xe9, (byte) 0x40, (byte) 0x96, (byte) 0x33, (byte) 0xbe, (byte) 0xa2, (byte) 0x94, (byte) 0x42, (byte) 0xe2, (byte) 0x14, (byte) 0x25, (byte) 0x00, (byte) 0x6e, (byte) 0xa8};
         byte[] actualHash0 = chainSHA256.get(0);
         assertEquals(new String(expectedHash0), new String(actualHash0));
     }
 
     @Test
     public void testGetHexRepresentation() throws Exception {
         String actualValue = getHexRepresentation(new byte[]{(byte) 0x03, (byte) 0xc0, (byte) 0x8f, (byte) 0x4e, (byte) 0xe0, (byte) 0xb5, (byte) 0x76, (byte) 0xfe, (byte) 0x31, (byte) 0x93, (byte) 0x38, (byte) 0x13, (byte) 0x9c, (byte) 0x04, (byte) 0x5c, (byte) 0x89, (byte) 0xc3, (byte) 0xe8, (byte) 0xe9, (byte) 0x40, (byte) 0x96, (byte) 0x33, (byte) 0xbe, (byte) 0xa2, (byte) 0x94, (byte) 0x42, (byte) 0xe2, (byte) 0x14, (byte) 0x25, (byte) 0x00, (byte) 0x6e, (byte) 0xa8});
         String expectedValue = "03c08f4ee0b576fe319338139c045c89c3e8e9409633bea29442e21425006ea8";
         assertEquals(expectedValue, actualValue);
     }
 
     @Test
     public void solveChallange() throws Exception {
         ChainSHA256 chainSHA256 = new ChainSHA256(
                 HashSHA256.newInstance(),
                 new FileReader(
                         getClass().getClassLoader()
                                 .getResource("META-INF/week3/challenge.mp4")
                         ,
                         1024
                 )
         );
 
         byte[] actualHash0 = chainSHA256.get(0);
         String result = getHexRepresentation(actualHash0);
         System.out.println(result);
     }
 
     private String getHexRepresentation(byte[] actualHash0) {
         String result = "";
         for (byte b : actualHash0) {
             result += String.format("%02x", b);
         }
         return result;
     }
 }
