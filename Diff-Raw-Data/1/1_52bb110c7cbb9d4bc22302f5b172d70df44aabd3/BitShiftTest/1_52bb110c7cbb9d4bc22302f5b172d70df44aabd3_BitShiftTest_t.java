 package number;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * Author: Roy Yang
  * Date: 11/29/12
  */
 public class BitShiftTest {
   @Test
   public void testRShiftLong() {
     BitShift bitShift = new BitShift();
     Assert.assertEquals(8L, bitShift.rShiftLong(128L, 4));
     Assert.assertEquals(7L, bitShift.rShiftLong(112L, 4));
     Assert.assertEquals(6L, bitShift.rShiftLong(96L, 4));
    Assert.assertEquals(6L, bitShift.rShiftLong(97L, 4));
   }
 }
