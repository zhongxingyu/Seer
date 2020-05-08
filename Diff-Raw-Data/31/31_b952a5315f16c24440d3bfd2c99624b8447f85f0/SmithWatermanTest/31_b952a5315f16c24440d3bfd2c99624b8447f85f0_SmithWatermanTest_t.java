 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.IDPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.alignment.recursive.MinusOneTwoAlignmentMatrix;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
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
  * @author koehleru
  */
 public class SmithWatermanTest {
 
     public SmithWatermanTest() {
     }
 
     /**
      * Test of align method, of class SmithWaterman. Example from
      * http://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm#Example
      *
      * Matrix is completely verified
      */
     @Test
     public void testMatrix() {
         System.out.println("align");
         Sequence seq1 = new Sequence("ACACACTA");
         Sequence seq2 = new Sequence("AGCACACA");
         SmithWaterman instance = new SmithWaterman(AlignmentMode.LOCAL, AlignmentAlgorithm.SMITH_WATERMAN, new MinusOneTwoAlignmentMatrix(), new ConstantGapCost(-1));
         AlignmentResult result = instance.align(seq1, seq2);
         //Check some other values
         assertArrayEquals(new double[]{0, 2, 1, 0, 2, 1, 2, 1, 2}, instance.getMatrix()[1], 0.000000001);
         assertArrayEquals(new double[]{0, 1, 1, 3, 2, 4, 3, 4, 3}, instance.getMatrix()[2], 0.000000001);
         assertArrayEquals(new double[]{0, 2, 1, 2, 5, 4, 6, 5, 6}, instance.getMatrix()[3], 0.000000001);
         assertArrayEquals(new double[]{0, 1, 1, 3, 4, 7, 6, 8, 7}, instance.getMatrix()[4], 0.000000001);
         assertArrayEquals(new double[]{0, 2, 1, 2, 5, 6, 9, 8, 10}, instance.getMatrix()[5], 0.000000001);
         assertArrayEquals(new double[]{0, 1, 1, 3, 4, 7, 8, 11, 10}, instance.getMatrix()[6], 0.000000001);
         assertArrayEquals(new double[]{0, 0, 0, 2, 3, 6, 7, 10, 10}, instance.getMatrix()[7], 0.000000001);
         assertArrayEquals(new double[]{0, 2, 1, 1, 4, 5, 8, 9, 12}, instance.getMatrix()[8], 0.000000001);
         System.out.println(result.getFirstAlignment().getQueryAlignment());
         System.out.println(result.getFirstAlignment().getTargetAlignment());
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.LOCAL, result.getFirstAlignment(), new MinusOneTwoAlignmentMatrix(), new ConstantGapCost(-1)), 0.0000001);
     }
 
     @Test
     public void testAlignReal1() throws IOException {
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(SmithWatermanTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK");
         Sequence seq2Obj = new Sequence("MEEAKQKVVDFLNSKSKSKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG");
         IGapCost gapCost = new ConstantGapCost(-1);
         //Align global
         SmithWaterman instance = new SmithWaterman(AlignmentMode.LOCAL, AlignmentAlgorithm.SMITH_WATERMAN, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
        System.out.println(result.getFirstAlignment().getQueryAlignment());
        System.out.println(result.getFirstAlignment().getTargetAlignment());
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.LOCAL, result.getFirstAlignment(), matrix, gapCost), 0.00000001);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testAlignReal2() throws IOException {
         //1xauA00  1fltX00
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(SmithWatermanTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("CEVQLNIKRNSKHSAWTGELFKIECPVKYCVHRPNVTWCKHNGTIWVPLEVGPQLYTSWEENRSVPVFVLHFKPIHLSDNGSYSCSTNFNSQVINSHSVTIHV");
         Sequence seq2Obj = new Sequence("GRPFVEMYSEIPEIIHMTEGRELVIPCRVTSPNITVTLKKFPLDTLIPDGKRIIWDSRKGFIISNATYKEIGLLTCEATVNGHLYKTNYLTHRQ");
         IGapCost gapCost = new ConstantGapCost(-1);
         //Align global
         SmithWaterman instance = new SmithWaterman(AlignmentMode.LOCAL, AlignmentAlgorithm.SMITH_WATERMAN, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.LOCAL, result.getFirstAlignment(), matrix, gapCost), 0.00000001);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testAlignReal3() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(SmithWatermanTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("ECLNKPINHQSNLVVPNTVKNTDGSLVTPEIISDDGDYEKPNVKWHLPEFTNEVSFIFYQPVTIGKAKARFHGRVTQP");
         Sequence seq2Obj = new Sequence("EEPQLSCFRKSPLSNVVCEWGPRSTPSLTTKAVLLVRKFQNSPAEDFQEPCQYSQESQKFSCQLAVPEGDSSFYIVSMCVASSVGSKFSKTQTFQGCGI");
         IGapCost gapCost = new ConstantGapCost(-1);
         //Align global
         SmithWaterman instance = new SmithWaterman(AlignmentMode.LOCAL, AlignmentAlgorithm.SMITH_WATERMAN, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.LOCAL, result.getFirstAlignment(), matrix, gapCost), 0.00000001);
     }
 
     @Test
     public void testAlignReal4() throws IOException {
         //1gvmF00 1nukA00
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(SmithWatermanTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("GGIVHSDGSYPKDKFEKINGTWYYFDSSGYMLADRWRKHTDGNWYWFDNSGEMATGWKKIADKWYYFNEEGAMKTGWVKYKDTWYYLDAKEGAMVSNAFIQSADGTGWYYLKPDGTLADRPEFTVEPDGLITV");
         Sequence seq2Obj = new Sequence("EETLMDSTTATAELGWMVHPPSGWEEVSGYDNTIRTYQVCNVFESSQNNWLRTKFIRRRGAHRIHVEMKFSVRDCSSIPSVPGSCKETFNLYYYEADFDLATKTFPNWMENPWVKVDTIAADESFSQVDVMKINTEVRSFGPVSRNGFYLAFQDYGGCMSLIAVRVFY");
         IGapCost gapCost = new ConstantGapCost(-1);
         //Align global
         SmithWaterman instance = new SmithWaterman(AlignmentMode.LOCAL, AlignmentAlgorithm.SMITH_WATERMAN, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.LOCAL, result.getFirstAlignment(), matrix, gapCost), 0.00000001);
     }
 
     @Test
     public void testAlignReal5() throws IOException {
         //1kydA01 1z9sA01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(SmithWatermanTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("GSPGIRLGSSEDNFARFVCKNNGVLFENQLLQIGLKSEFRQNLGRMFIFYGNKTSTQFLNFTPTLICADDLQTNLNLQTKPVDPTVDGGAQVQQVVNIECISDFTEAPVLNIQFRYGGTFQNVSVKLPITLNK");
         Sequence seq2Obj = new Sequence("VTIGESRIIYPLDAAGVMVSVKNTQDYPVLIQSRIYDENKEKPFVVTPPLFRLDAKQQNSLRIAQAGGVFPRDKESLKWLCVKGIPPDVGVFVQFAINNCIKLLVRP");
         IGapCost gapCost = new ConstantGapCost(-1);
         //Align global
         SmithWaterman instance = new SmithWaterman(AlignmentMode.LOCAL, AlignmentAlgorithm.SMITH_WATERMAN, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreNonAffine(AlignmentMode.LOCAL, result.getFirstAlignment(), matrix, gapCost), 0.00000001);
     }
 }
