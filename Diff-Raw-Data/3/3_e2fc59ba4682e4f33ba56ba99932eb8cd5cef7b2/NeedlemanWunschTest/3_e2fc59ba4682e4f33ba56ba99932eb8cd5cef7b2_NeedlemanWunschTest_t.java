 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.recursive.WikipediaAlignmentMatrix1;
 import de.bioinformatikmuenchen.pg4.alignment.recursive.ZeroOneAlignmentMatrix;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.distance.QUASARDistanceMatrixFactory;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import static junit.framework.Assert.assertEquals;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author tobias
  */
 public class NeedlemanWunschTest {
 
     public NeedlemanWunschTest() {
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
 
 //   
     /**
      * Test of align method, of class NeedlemanWunsch.
      */
     @Test
     public void testAlignAvatar() {
         NeedlemanWunsch instance = new NeedlemanWunsch(AlignmentMode.FREESHIFT, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, new ZeroOneAlignmentMatrix(), new ConstantGapCost(0));
         Sequence seq1Obj = new Sequence("GAATTCAGTTA");
         Sequence seq2Obj = new Sequence("GGATCGA");
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         //System.out.println(instance.printMatrix());
 //        SequencePairAlignment spa = result.getFirstAlignment();
         //System.out.println("##### \n" + spa.queryAlignment + "\n" + spa.matchLine + "\n" + spa.targetAlignment);
         assertEquals(6.0, result.getScore(), 0.0000000001);
         assertEquals(6.0, CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.FREESHIFT, result.getFirstAlignment(), new ZeroOneAlignmentMatrix(), new ConstantGapCost(0)), 0.0000000001);
         //System.out.println("Q: " + result.getFirstAlignment().getQueryAlignment());
         //System.out.println("T: " + result.getFirstAlignment().getTargetAlignment());
 //        assertEquals("G-AATTCAGTTA", currentAlignment.getSequence());
     }
 
     /**
      * Test of align method, of class NeedlemanWunsch.
      */
     @Test
     public void testAlignZeroOneMatrix() {
         NeedlemanWunsch w = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, new ZeroOneAlignmentMatrix(), new ConstantGapCost(0));
         AlignmentResult result = w.align(new Sequence("ACGA"), new Sequence("TCCG"));
         //System.out.println("Score: " + result.getScore());
         assertEquals(2, result.getScore(), 0.00000001);
 
         System.out.println(result.getFirstAlignment().getQueryAlignment());
         System.out.println(result.getFirstAlignment().getTargetAlignment());
         //assertEquals("G-AATTCAGTTA", currentAlignment.getSequence());
         assertEquals(2.0, CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.GLOBAL, result.getFirstAlignment(), new ZeroOneAlignmentMatrix(), new ConstantGapCost(0)), 0.0000000001);
     }
 
     /**
      * Test of align method, of class RecursiveNWAlignmentProcessor. Just
      * something with high match rate.
      *
      * Manually verified (Uli)
      */
     @Test
     public void testAlignSimple() {
         Sequence seq1Obj = new Sequence("GAATT");
         Sequence seq2Obj = new Sequence("GGATC");
         IDistanceMatrix matrix = new WikipediaAlignmentMatrix1();
         IGapCost gapCost = new ConstantGapCost(-5);
         NeedlemanWunsch instance = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(24.0, result.getScore(), 0.0000000001);
         assertEquals(24.0, CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.GLOBAL, result.getFirstAlignment(), new ZeroOneAlignmentMatrix(), new ConstantGapCost(0)), 0.0000000001);
         //System.out.println("##### \n" + spa.queryAlignment + "\n" + spa.matchLine + "\n" + spa.targetAlignment);
     }
 
     /**
      * It should match on the left border
      */
     @Test
     public void testAlignLeftBorder() {
         Sequence seq1Obj = new Sequence("GAATT");
         Sequence seq2Obj = new Sequence("GAATTGGATC");
         IDistanceMatrix matrix = new WikipediaAlignmentMatrix1();
         IGapCost gapCost = new ConstantGapCost(-5);
         NeedlemanWunsch instance = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         System.out.println("X");
         System.out.println(result.getFirstAlignment().getQueryAlignment());
         System.out.println(result.getFirstAlignment().getTargetAlignment());
         System.out.println("X");
        assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.GLOBAL, result.getFirstAlignment(), new ZeroOneAlignmentMatrix(), new ConstantGapCost(0)), 0.0000000001);
         //System.out.println("##### \n" + spa.queryAlignment + "\n" + spa.matchLine + "\n" + spa.targetAlignment);
     }
 //    @Test
 //    public void testAlignSimple() {
 //        Sequence seq1Obj = new Sequence("GAATT");
 //        Sequence seq2Obj = new Sequence("GGATC");
 //        IDistanceMatrix matrix = new WikipediaAlignmentMatrix1();
 //        IGapCost gapCost = new ConstantGapCost(-5);
 //        NeedlemanWunsch instance = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
 //        AlignmentResult result = instance.align(seq1Obj, seq2Obj);
 //        assertEquals(24.0, result.getScore(), 0.0000000001);
 //    }
 //    @Test
 //    public void test Stuff() {
 //
 //        //SmithWaterman w = new SmithWaterman(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, new ZeroOneAlignmentMatrix(), new ConstantGapCost(0));
 //        NeedlemanWunsch w = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, new ZeroOneAlignmentMatrix(), new ConstantGapCost(0));
 //        AlignmentResult result = w.align(new Sequence("GAATTCAGTTA"), new Sequence("GGATCGA"));
 //        System.out.println("Score: " + result.getScore());
 //        assertEquals(6, result.getScore(), 0.00000001);
 //        System.out.println("spa size: " + result.getAlignments().size());
 //        SequencePairAlignment spa = result.getFirstAlignment();
 //        System.out.println("##aligned sequence: " + spa.queryAlignment + "\n" + spa.matchLine + "\n" + spa.targetAlignment);
 //        assertEquals("G-AATTCAGTTA", spa.queryAlignment);
 //    }
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is Gotoh or SW
      */
 //    @Test
 //    public void testAlignReal1() throws IOException {
 //        IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat"));
 //        Sequence seq1Obj = new Sequence("GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK");
 //        Sequence seq2Obj = new Sequence("MEEAKQKVVDFLNSKSKSKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG");
 //        IGapCost gapCost = new ConstantGapCost(-5);
 //        NeedlemanWunsch instance = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
 //        AlignmentResult result = instance.align(seq1Obj, seq2Obj);
 //        assertEquals(4.90, result.getScore(), 0.00000001);
 //    }
 //    @Test
 //    public void testAlignRealMatrix() throws IOException {
 //        NeedlemanWunsch nw = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, QUASARDistanceMatrixFactory.factorize("/matrices/blosum80.mat"), null);        
 //    }
 }
