 package org.nebulostore.appcore;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 
 import org.junit.Test;
 import org.nebulostore.addressing.AppKey;
 import org.nebulostore.addressing.ObjectId;
 import org.nebulostore.appcore.exceptions.NebuloException;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Simple unit test for NebuloFile.
  */
 public final class NebuloFileTest {
 
   @Test
   public void testWriteMultipleChunks() {
    NebuloFile file = new NebuloFile(new AppKey(BigInteger.ONE), new ObjectId(BigInteger.ONE), 10);
 
     // Do three writes.
     byte[] as = new byte[35];
     Arrays.fill(as, (byte) 'a');
     byte[] bs = new byte[38];
     Arrays.fill(bs, (byte) 'b');
     byte[] cs = new byte[13];
     Arrays.fill(cs, (byte) 'c');
 
     byte[] sum = new byte[53];
     System.arraycopy(as, 0, sum, 0, 35);
     System.arraycopy(bs, 0, sum, 15, 38);
     System.arraycopy(cs, 0, sum, 19, 13);
 
     try {
       file.write(as, 0);
       file.write(bs, 15);
       file.write(cs, 19);
     } catch (NebuloException exception) {
       assertTrue(false);
     }
 
     assertTrue(file.getSize() == 53);
     try {
       byte[] check = file.read(0, file.getSize());
       assertTrue(check.length == 53);
       assertTrue(Arrays.equals(check, sum));
     } catch (NebuloException exception) {
       assertTrue(false);
     }
   }
 }
