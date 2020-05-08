 package jumble;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import java.io.IOException;
 import org.apache.bcel.classfile.JavaClass;
 import java.util.Random;
 
 /**
  * Tests the corresponding class.
  *
  * @author Sean A. Irvine
  * @version $Revision$
  */
 public class MutaterTest extends TestCase {
 
   public MutaterTest(String name) {
     super(name);
   }
 
   public static Test suite() {
     TestSuite suite = new TestSuite(MutaterTest.class);
     return suite;
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 
   public void testCountMutationPointsX0() {
     assertEquals(0, new Mutater(0).countMutationPoints("jumble.X0"));
   }
 
   public void testCountMutationPointsX0I() {
     assertEquals(-1, new Mutater(0).countMutationPoints("jumble.X0I"));
   }
 
   public void testCountMutationPointsX1() {
     assertEquals(1, new Mutater(0).countMutationPoints("jumble.X1"));
   }
 
   public void testCountMutationPointsX2() {
     assertEquals(9, new Mutater(0).countMutationPoints("jumble.X2"));
   }
 
   public void testCountMutationPointsX2r() {
     Mutater m = new Mutater(0);
     m.setMutateReturnValues(true);
     assertEquals(10, m.countMutationPoints("jumble.X2"));
   }
 
   public void testCountMutationPointsX2i() {
     Mutater m = new Mutater(0);
     m.setMutateInlineConstants(true);
     assertEquals(11, m.countMutationPoints("jumble.X2"));
   }
 
   public void testCountMutationPointsX2ir() {
     Mutater m = new Mutater(0);
     m.setMutateInlineConstants(true);
     m.setMutateReturnValues(true);
     assertEquals(12, m.countMutationPoints("jumble.X2"));
   }
 
   
   
   private void testDescriptions(int x, String s) throws IOException {
     Mutater m = new Mutater(x);
     assertEquals(null, m.getModification());
     m.setMutateInlineConstants(true);
     m.setMutateReturnValues(true);
     m.jumbler("jumble.X2");
     assertEquals(m.getModification(), s, m.getModification());
   }
 
   public void testDescriptionsX2() throws IOException {
     testDescriptions(0, "jumble.X2:6: * -> /");
     testDescriptions(1, "jumble.X2:6: / -> *");
     testDescriptions(2, "jumble.X2:6: + -> -");
     testDescriptions(3, "jumble.X2:6: % -> *");
     testDescriptions(4, "jumble.X2:6: / -> *");
     testDescriptions(5, "jumble.X2:6: - -> +");
     testDescriptions(6, "jumble.X2:6: 5 -> -1");
     testDescriptions(7, "jumble.X2:6: >> -> <<");
     testDescriptions(8, "jumble.X2:6: << -> >>");
     testDescriptions(9, "jumble.X2:6: 57 (9) -> 58 (:)");
     testDescriptions(10, "jumble.X2:6: & -> |");
     testDescriptions(11, "jumble.X2:6: changed return value (ireturn)");
     testDescriptions(500, null);
   }
 
   private void testDescriptions3(int x, String s) throws IOException {
     Mutater m = new Mutater(x);
     assertEquals(null, m.getModification());
     m.setMutateInlineConstants(true);
     m.setMutateReturnValues(true);
     m.jumbler("jumble.X3");
     assertEquals(m.getModification(), s, m.getModification());
   }
 
   public void testDescriptionsX3() throws IOException {
    testDescriptions3(0, "jumble.X3:6: * -> /");
     testDescriptions3(1, "jumble.X3:6: * -> /");
    testDescriptions3(2, "jumble.X3:6: + -> -");
    testDescriptions3(3, "jumble.X3:6: - -> +");
    testDescriptions3(4, "jumble.X3:6: changed return value (areturn)");
     testDescriptions3(500, null);
   }
 
   public void testFindsClass() {
     Mutater m = new Mutater(0);
     try {
       assertNotNull(m.jumbler("jumble.X3"));
     } catch (IOException e) {
       fail ("IO problem");
     }
   }
 
   public void testDoesntFindClass() {
     Mutater m = new Mutater(0);
     try {
       m.jumbler("poxweed");
       fail("IO failed to fire");
     } catch (IOException e) {
        // ok
     }
   }
 
   private void testDescriptions4(int x, String s) throws IOException {
     Mutater m = new Mutater(x);
     assertEquals(null, m.getModification());
     m.setMutateInlineConstants(true);
     m.setMutateReturnValues(true);
     m.jumbler("jumble.X4");
     assertEquals(m.getModification(), s, m.getModification());
   }
 
   public void testDescriptionsX4() throws IOException {
     testDescriptions4(0, "jumble.X4:6: * -> /");
     testDescriptions4(1, "jumble.X4:6: / -> *");
     testDescriptions4(2, "jumble.X4:6: + -> -");
     testDescriptions4(3, "jumble.X4:6: % -> *");
     testDescriptions4(4, "jumble.X4:6: / -> *");
     testDescriptions4(5, "jumble.X4:6: - -> +");
     testDescriptions4(6, "jumble.X4:6: 5 -> -1");
     testDescriptions4(7, "jumble.X4:6: >> -> <<");
     testDescriptions4(8, "jumble.X4:6: << -> >>");
     testDescriptions4(9, "jumble.X4:6: & -> |");
     testDescriptions4(10, "jumble.X4:6: changed return value (lreturn)");
     testDescriptions4(500, null);
   }
 
   public void hash(String s, int mp, long h) {
     try {
       Mutater m = new Mutater(mp);
       JavaClass c = m.jumbler(s);
       assertEquals(h, irvineHash(c.getBytes(), 0, c.getBytes().length));
     } catch (Exception e) {
       fail(e.getMessage());
     }
   }
 
 //  public void testX1() {
 //    hash("jumble.X1", 0, -198300596214704957L);
 //    hash("jumble.X1", 1, -99160866112043740L);
 //  }
 //
 //  public void testX2() {
 //    hash("jumble.X2", 0, -7862720645899172273L);
 //    hash("jumble.X2", 1, 5377800701323600766L);
 //    hash("jumble.X2", 2, 4749833603092092233L);
 //    hash("jumble.X2", 3, 6546560926190321894L);
 //    hash("jumble.X2", 4, 588472248583441424L);
 //    hash("jumble.X2", 5, 4006834831399313369L);
 //    hash("jumble.X2", 6, 1706316102534582516L);
 //    hash("jumble.X2", 7, 2821587797992537863L);
 //    hash("jumble.X2", 8, -4327004115041685281L);
 //    hash("jumble.X2", 9, -8984876399724589582L);
 //    hash("jumble.X2", 10, -3393737352001148148L);
 //    hash("jumble.X2", 11, -3393737352001148148L);
 //  }
 //
 //  public void testX3() {
 //    hash("jumble.X3", 0, -6912814337401050070L);
 //    hash("jumble.X3", 1, -6305687574031288397L);
 //    hash("jumble.X3", 2, -7572680762404172073L);
 //    hash("jumble.X3", 3, -45494795387826754L);
 //    hash("jumble.X3", 4, -9013035596486906095L);
 //    hash("jumble.X3", 5, -7020311211546946589L);
 //    hash("jumble.X3", 6, 4686291222113703279L);
 //    hash("jumble.X3", 7, 4686291222113703279L);
 //  }
 //
 //  public void testX4() {
 //    hash("jumble.X4", 0, -3826548509183312234L);
 //    hash("jumble.X4", 1, -573678711190914183L);
 //    hash("jumble.X4", 2, 701134789134704163L);
 //    hash("jumble.X4", 3, -8267376496489928705L);
 //    hash("jumble.X4", 4, 5571030277499787942L);
 //    hash("jumble.X4", 5, -6745642128101340253L);
 //    hash("jumble.X4", 6, 7940965012977604712L);
 //    hash("jumble.X4", 7, -8991507075973028893L);
 //    hash("jumble.X4", 8, 2609809997431547786L);
 //    hash("jumble.X4", 9, 1824724605084526440L);
 //    hash("jumble.X4", 10, 1824724605084526440L);
 //  }
 
   
   public void testGetMutatedMethodName() {
     Mutater m = new Mutater(0);
     assertEquals("add(II)I", m.getMutatedMethodName("experiments.JumblerExperiment"));
 
     m = new Mutater(2);
     assertEquals("add(II)I", m.getMutatedMethodName("experiments.JumblerExperiment"));
     
     m = new Mutater(3);
     assertEquals("multiply(II)I", m.getMutatedMethodName("experiments.JumblerExperiment"));
   
     try {
       m = new Mutater(500);
       m.getMutatedMethodName("experiments.JumblerExperiment");    
       fail();
     } catch (Exception e) {
       ; //ok
     }
   }
   
   public void testGetMethodRelativeMutationPoint() {
     Mutater m = new Mutater(0);
     assertEquals(0, m.getMethodRelativeMutationPoint("experiments.JumblerExperiment"));
 
     m = new Mutater(2);
     assertEquals(2, m.getMethodRelativeMutationPoint("experiments.JumblerExperiment"));
 
     m = new Mutater(3);
     assertEquals(0, m.getMethodRelativeMutationPoint("experiments.JumblerExperiment"));
   
     try {
       m = new Mutater(500);
       m.getMethodRelativeMutationPoint("experiments.JumblerExperiment");    
       fail();
     } catch (Exception e) {
       ; //ok
     }
   }
   
   /** Randomly generated arrays used to compute irvineHash codes */
   private static final long[] HASH_BLOCKS;
   static {
     HASH_BLOCKS = new long[256];
     Random r = new Random(1L); // use same seed for deterministic behavior
     for (int i = 0; i < 256; i++) {
       HASH_BLOCKS[i] = r.nextLong();
     }
   }
 
   /**
    * Returns a 64 bit hash of the given string. This hash function
    * exhibits better statistical behavior than String hashCode() and
    * has speed comparable to CRC32.
    *
    * @param in bytes to checksum
    * @param start first byte
    * @param length length of input
    * @return a hash
    */
   private static long irvineHash(final byte[] in, final int start, final int length) {
     long r = 0L;
     for (int i = 0; i < length; i++) {
       final long sgn = (r & 0x8000000000000000L) >>> 63;
       r <<= 1;
       r |= sgn;
       r ^= HASH_BLOCKS[(in[i + start] + i) & 0xFF];
     }
     return r;
   }
 
 
 }
