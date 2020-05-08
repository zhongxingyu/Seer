 package algorithm.cc150.chapter5;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.BitSet;
 
 import org.junit.Test;
 
 import algorithm.TestBase;
 
 /**
 * You are given two 32-bit numbers, N and M, and wo bit operations, i and j.
  * Write a method to insert M into N such that M starts at bit j and ends at bit
  * i. You can assume that the bits j through i have enough space to fit all of
  * M. That is, if M = 10011, you can assume that there are at least 5 bits
  * between j and i. You would not, for example, have j = 3 and i = 2, because M
  * could not fully fit between bit 3 and bit 2.
  * 
  * EXAMPLE Input: N = 10000000000, M = 10011, i = 2, j = 6 Output: N =
  * 1001001100
  */
 public class TestQuestion1 extends TestBase {
 
   private Question1 question;
 
   @Override
   protected void initInstance() {
     question = new Question1();
   }
 
   @Override
   @Test
   public void testPosCase() {
     BitSet b1 = BitSet.valueOf(new long[] { Long.parseLong("10000000000") });
     BitSet b2 = BitSet.valueOf(new long[] { Long.parseLong("10011") });
     BitSet r1 = BitSet.valueOf(new long[] { Long.parseLong("10001001100") });
     assertEquals(r1, question.insert(b1, b2, 2, 6));
 
     BitSet b3 = BitSet.valueOf(new long[] { Long.parseLong("100000000000") });
     BitSet b4 = BitSet.valueOf(new long[] { Long.parseLong("100110") });
     BitSet r2 = BitSet.valueOf(new long[] { Long.parseLong("100010011000") });
     assertEquals(r1, question.insert(b1, b2, 2, 7));
   }
 
   @Override
   @Test
   public void testNegCase() {
     // no neg case need
   }
 
 }
