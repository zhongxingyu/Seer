 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.AffineGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.common.distance.QUASARDistanceMatrixFactory;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author koehleru
  */
 public class GotohTest {
 
     public GotohTest() {
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
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
    //@Test
     public void testAlignReal1() throws IOException {
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK");
         Sequence seq2Obj = new Sequence("MEEAKQKVVDFLNSKSKSKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         Gotoh instance = new Gotoh(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(4.90, result.getScore(), 0.00000001);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
    //@Test
     public void testAlignReal2() throws IOException {
         //1xauA00  1fltX00
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("CEVQLNIKRNSKHSAWTGELFKIECPVKYCVHRPNVTWCKHNGTIWVPLEVGPQLYTSWEENRSVPVFVLHFKPIHLSDNGSYSCSTNFNSQVINSHSVTIHV");
         Sequence seq2Obj = new Sequence("GRPFVEMYSEIPEIIHMTEGRELVIPCRVTSPNITVTLKKFPLDTLIPDGKRIIWDSRKGFIISNATYKEIGLLTCEATVNGHLYKTNYLTHRQ");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         Gotoh instance = new Gotoh(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(4.70, result.getScore(), 0.00000001);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
    //@Test
     public void testAlignReal3() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("ECLNKPINHQSNLVVPNTVKNTDGSLVTPEIISDDGDYEKPNVKWHLPEFTNEVSFIFYQPVTIGKAKARFHGRVTQP");
         Sequence seq2Obj = new Sequence("EEPQLSCFRKSPLSNVVCEWGPRSTPSLTTKAVLLVRKFQNSPAEDFQEPCQYSQESQKFSCQLAVPEGDSSFYIVSMCVASSVGSKFSKTQTFQGCGI");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         Gotoh instance = new Gotoh(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(-19.2, result.getScore(), 0.00000001);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testAlignmentResult() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK");
         Sequence seq2Obj = new Sequence("MEEAKQKVVDFLNSKSKSKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         int correctAlignmentLength = "GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK-".length();
         Gotoh instance = new Gotoh(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         SequencePairAlignment alignment = result.getFirstAlignment();
         assertEquals(4.900, result.getScore(), 0.00000001);
         System.out.println("A1: " + alignment.queryAlignment);
         System.out.println("A2: " + alignment.targetAlignment);
         assertEquals(correctAlignmentLength, alignment.queryAlignment.length());
         assertEquals(correctAlignmentLength, alignment.targetAlignment.length());
         assertEquals(4.900, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.GLOBAL, alignment, matrix, gapCost), 0.00000001);
         assertEquals("GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK-", alignment.queryAlignment);
         assertEquals("----MEEAKQKVVDFLNSKSK-SKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG", alignment.targetAlignment);
     }
 
 //    @Test
     public void testAlignHay() throws IOException {
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("SIE");
         Sequence seq2Obj = new Sequence("SAHNE");
         IGapCost gapCost = new AffineGapCost(-21, -1);
         NeedlemanWunsch instance = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(4.90, result.getScore(), 0.00000001);
     }
 }
