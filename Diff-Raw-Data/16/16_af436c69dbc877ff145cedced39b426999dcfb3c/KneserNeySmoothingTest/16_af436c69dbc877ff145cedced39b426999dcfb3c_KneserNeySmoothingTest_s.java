 package eu.monnetproject.translation.langmodels.smoothing;
 
 import eu.monnetproject.translation.langmodels.NGram;
 import eu.monnetproject.translation.langmodels.WeightedNGramCountSet;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author john
  */
 public class KneserNeySmoothingTest {
 
     public KneserNeySmoothingTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     private static NGram ng(int... x) {
         return new NGram(x);
     }
     /**
      * Test of ngramScores method, of class KneserNeySmoothing.
      */
     @Test
     public void testNgramScores() {
         System.out.println("ngramScores");
         final LossyCounterWithHistory counter = new LossyCounterWithHistory(2);
         counter.offer(1);
         counter.offer(2);
         counter.offer(3);
         counter.offer(4);
         counter.offer(4);
         counter.offer(3);
         counter.offer(2);
         counter.offer(1);
         counter.offer(1);
         counter.offer(2);
         counter.offer(3);
         counter.offer(3);
         counter.offer(5);
         WeightedNGramCountSet countSet = counter.counts().asWeightedSet();
         KneserNeySmoothing instance = new KneserNeySmoothing(counter.histories(), new int[][] { 
             { 1,1,2,1 },
             { 8,2,0,0 }
         }, 2);
         assertArrayEquals(new double[] { Math.log10(1e-6), Math.log10(1.0/9.0) }, instance.ngramScores(ng(1),countSet), 0.01);
         assertArrayEquals(new double[] { Math.log10(1e-6), Math.log10(1.0/9.0) }, instance.ngramScores(ng(2),countSet),0.01);
         assertArrayEquals(new double[] { Math.log10(1.0/15.0), Math.log10(1.0/3.0) }, instance.ngramScores(ng(3),countSet),0.01);
         assertArrayEquals(new double[] { Math.log10(1.0/5.0), Math.log10(1.0/3.0) }, instance.ngramScores(ng(4),countSet),0.01);
        assertArrayEquals(new double[] { Math.log10(1.0/15.0), Math.log10(1e-6) }, instance.ngramScores(ng(5),countSet),0.01);
         assertArrayEquals(new double[] { Math.log10(1e-6) }, instance.ngramScores(ng(1,2),countSet),0.01);
         assertArrayEquals(new double[] { Math.log10(1.0/12.0) }, instance.ngramScores(ng(3,4),countSet),0.01);
     }
 
 }
