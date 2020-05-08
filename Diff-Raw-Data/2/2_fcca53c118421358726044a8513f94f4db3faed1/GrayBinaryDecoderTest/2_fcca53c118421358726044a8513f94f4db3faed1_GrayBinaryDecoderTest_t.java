 package core.ga;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Rekin
  */
 public class GrayBinaryDecoderTest {
 
     public GrayBinaryDecoderTest() {
     }
 
     public Addressable addressableFor(final String str) {
         return new Addressable()  {
 
             public boolean get(int address) {
                 return str.charAt(address) == '1';
             }
 
             public void set(int address, boolean b) {
                 throw new UnsupportedOperationException("MOCK");
             }
         };
     }
 
     @Test
     public void testDecode() {
         System.out.println("decode");
         String[] testArray = {
             "0000", "0001", "0011", "0010",
             "0110", "0111", "0101", "0100",
             "1100", "1101", "1111", "1110",
             "1010", "1011", "1001", "1000"};
         GrayBinaryDecoderPlusONE instance = new GrayBinaryDecoderPlusONE();
         for (int i = 0; i < testArray.length; i++) {
             int result = instance.decode(addressableFor(testArray[i]), 0, 4);
            assertEquals(i+1, result);
         }
     }
 }
