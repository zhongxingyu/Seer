 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.AffineGapCost;
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
     @Test
     public void fpa() throws IOException {
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("id xy","GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK");
        Sequence seq2Obj = new Sequence("id2 test_zyx","MEEAKQKVVDFLNSKSKSKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         FixedPoint instance = new FixedPoint(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         //AlignmentResult result = instance.align(seq1Obj, seq2Obj);
        instance.makePlot(seq1Obj, seq2Obj, true);
         //assertEquals(4.90, result.getScore(), 0.00000001);
     }
 
     @Test
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
     @Test
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
     @Test
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
         String exp1 = "GPLDVQVTEDAVRRYLTRKPMTTKDLLKKFQTKKTGLSSEQTVNVLAQILKRLNPERKMINDKMHFSLK-";
         String exp2 = "----MEEAKQKVVDFLNSKSK-SKFYFNDFTDLFPDMKQREVKKILTALVNDEVLEYWSSGSTTMYGLKG";
         System.out.println();
         System.out.println("A1Q: " + alignment.queryAlignment);
         System.out.println("E1Q: " + exp1);
         System.out.println("A1T: " + alignment.targetAlignment);
         System.out.println("E1T: " + exp2);
         assertEquals(correctAlignmentLength, alignment.queryAlignment.length());
         assertEquals(correctAlignmentLength, alignment.targetAlignment.length());
         assertEquals(4.900, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.GLOBAL, alignment, matrix, gapCost), 0.00000001);
         assertEquals(exp1, alignment.queryAlignment);
         assertEquals(exp2, alignment.targetAlignment);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testAlignmentResult2() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("TPSMEDYIEQIYMLIEEKGYARVSDIAEALAVHPSSVTKMVQKLDKDEYLIYGLVLTSKGKKIGKR");
         Sequence seq2Obj = new Sequence("MKYNNHDKIRDFIIIEAYMFRFKKKVKPEVDMTIKEFILLTYLFHQQENTLPFKKIVSDLCYKQSDLVQHIKVLVKHSYISKVRSKIDERNTYISISEEQREKIAERVTLFDQIIKQFNLADQSES");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         int correctAlignmentLength = "TP------------------------------SMEDYIEQIYMLIEEKGYARVSDIAEALAVHPSSVTKMVQKLDKDEYL-----------IYGLVLTSKGKKIGKR-------------------".length();
         Gotoh instance = new Gotoh(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         SequencePairAlignment alignment = result.getFirstAlignment();
         assertEquals(-19.2, result.getScore(), 0.00000001);
         String exp1 = "TP------------------------------SMEDYIEQIYMLIEEKGYARVSDIAEALAVHPSSVTKMVQKLDKDEYL-----------IYGLVLTSKGKKIGKR-------------------";
         String exp2 = "MKYNNHDKIRDFIIIEAYMFRFKKKVKPEVDMTIKEFILLTYLFHQQENTLPFKKIVSDLCYKQSDLVQHIKVLVKHSYISKVRSKIDERNTYISISEEQREKIAERVTLFDQIIKQFNLADQSES";
         System.out.println();
         System.out.println("A2Q: " + alignment.queryAlignment);
         System.out.println("E2Q: " + exp1);
         System.out.println("A2T: " + alignment.targetAlignment);
         System.out.println("E2T: " + exp2);
         assertEquals(correctAlignmentLength, alignment.queryAlignment.length());
         assertEquals(correctAlignmentLength, alignment.targetAlignment.length());
         assertEquals(-19.200, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.GLOBAL, alignment, matrix, gapCost), 0.00000001);
         assertEquals(exp1, alignment.queryAlignment);
         assertEquals(exp2, alignment.targetAlignment);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testLocalAlignmentResult() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("ECLNKPINHQSNLVVPNTVKNTDGSLVTPEIISDDGDYEKPNVKWHLPEFTNEVSFIFYQPVTIGKAKARFHGRVTQP");
         Sequence seq2Obj = new Sequence("EEPQLSCFRKSPLSNVVCEWGPRSTPSLTTKAVLLVRKFQNSPAEDFQEPCQYSQESQKFSCQLAVPEGDSSFYIVSMCVASSVGSKFSKTQTFQGCGI");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         Gotoh instance = new Gotoh(AlignmentMode.LOCAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         //System.out.println(instance.printMatrix());
         SequencePairAlignment alignment = result.getFirstAlignment();
         assertEquals(31.800, result.getScore(), 0.00000001);
         String exp1 = "ECLNKPINHQSNLVVPNTVKNTDGSLVTPEIISDDGDYEKPNVKWH-----------------------------------LPEFTNEVSFIFYQPVTIGKAKARFHGRVTQP--------------------------------";
         String exp2 = "----------------------------------------------EEPQLSCFRKSPLSNVVCEWGPRSTPSLTTKAVLLVRKFQNSPAEDFQEPCQYSQESQKFSCQLAVPEGDSSFYIVSMCVASSVGSKFSKTQTFQGCGI";
         System.out.println();
         System.out.println("(L)A1Q: " + alignment.queryAlignment);
         System.out.println("(L)E1Q: " + exp1);
         System.out.println("(L)A1T: " + alignment.targetAlignment);
         System.out.println("(L)E1T: " + exp2);
         assertEquals(31.800, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.LOCAL, alignment, matrix, gapCost), 0.00000001);
         assertEquals(exp1, alignment.queryAlignment);
         assertEquals(exp2, alignment.targetAlignment);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testLocalAlignmentResultMaxInCorner() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("ACACACTA");
         Sequence seq2Obj = new Sequence("AGCACACA");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         Gotoh instance = new Gotoh(AlignmentMode.LOCAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         //System.out.println(instance.printMatrix());
         SequencePairAlignment alignment = result.getFirstAlignment();
         assertEquals(result.getScore(), CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.LOCAL, alignment, matrix, gapCost), 0.00000001);
     }
 
     /**
      * Real world example from sanity.pairs that hasn't worked somewhen -- Not
      * sure if this is for Goto, so doublechekc
      */
     @Test
     public void testFreeshiftAlignmentResult1() throws IOException {
         //1m9sA02  1p9mC01
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
         Sequence seq1Obj = new Sequence("ECLNKPINHQSNLVVPNTVKNTDGSLVTPEIISDDGDYEKPNVKWHLPEFTNEVSFIFYQPVTIGKAKARFHGRVTQP");
         Sequence seq2Obj = new Sequence("EEPQLSCFRKSPLSNVVCEWGPRSTPSLTTKAVLLVRKFQNSPAEDFQEPCQYSQESQKFSCQLAVPEGDSSFYIVSMCVASSVGSKFSKTQTFQGCGI");
         IGapCost gapCost = new AffineGapCost(-12, -1);
         Gotoh instance = new Gotoh(AlignmentMode.FREESHIFT, AlignmentAlgorithm.GOTOH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         System.out.println(instance.printMatrix());
         SequencePairAlignment alignment = result.getFirstAlignment();
         assertEquals(20.900, result.getScore(), 0.00000001);
         String exp1 = "-----ECLNK-PINHQSNLVVPNTVKNTDGSLVTPEIISDDGDYEKPNVKWHLPEFTNEVSFIFYQPVTIGKAKARFHGRVTQP--------------------------------";
         String exp2 = "EEPQLSCFRKSPLSN----VVCEWGPRSTPSLTTKAVL-------------LVRKFQNSPAEDFQEPCQYSQESQKFSCQLAVPEGDSSFYIVSMCVASSVGSKFSKTQTFQGCGI";
         System.out.println();
         System.out.println("(F)A1Q: " + alignment.queryAlignment);
         System.out.println("(F)E1Q: " + exp1);
         System.out.println("(F)A1T: " + alignment.targetAlignment);
         System.out.println("(F)E1T: " + exp2);
         assertEquals(20.9, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.FREESHIFT, new SequencePairAlignment(exp1, exp2), matrix, gapCost), 0.00000001);
         assertEquals(20.9, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.FREESHIFT, alignment, matrix, gapCost), 0.00000001);
         assertEquals(exp1, alignment.queryAlignment);
         assertEquals(exp2, alignment.targetAlignment);
     }
 
 //    /**
 //     * Real world example from sanity.pairs that hasn't worked somewhen -- Not
 //     * sure if this is for Goto, so doublechekc
 //     */
 //    @Test
 //    public void testAlignmentResult3() throws IOException {
 //        //1sqjB02 1iarB02
 //    ERROR: THIS TEST DOES PRODUCE A DIFFERENT ALIGNMENT...
 //        IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(new InputStreamReader(NeedlemanWunschTest.class.getResourceAsStream("/matrices/dayhoff.mat")));
 //        Sequence seq1Obj = new Sequence("GGGYITGIVAHPKTKDLLYARTDIGGAYRWDAGTSKWIPLNDFIEAQDMNIMGTESIALDPNNPDRLYLAQGRYVGDEWAAFYVSEDRGQSFTIYESPFPMGANDMGRNNGERLAVNPFNSNEVWMGTRTEGIWKSSDRAKTWTNVTSIPDAFTNGIGYTSVIFDPERNGTIYASATAPQGMYVTHDGGVSWEPVAGQPSSWLNRTTGAFPDKKPASIAPQPMKVALTPNFLYVTYADYPGPWGVTFGEVWRQNRTSGAWDDITPRVGNSSPAPYNNQTFPAGGFCGLSVDATNPNRLVVITLDRDPGPALDSIYLSTDAGATWKDVTQLSSPSNLEGNWGHPTNAARYKDGTPVPWLDFNNGPQWGGYGAPHGTPGLTKFGWWMSAVLIDPFNPEHLMYGTGATIWATDTLSRVEKDWAPSWYLQIDGIEEKSTAKCANGQKGTHCY");
 //        Sequence seq2Obj = new Sequence("KPRAPGNLTVHDTLLLTWSNPYPPDNYLYNHLTYAVNIWSENDPADFRIYNVTYLEPSLRIAAGISYRARVRAWAQAYNTTWSEWSPSTKW");
 //        IGapCost gapCost = new AffineGapCost(-12, -1);
 //        int correctAlignmentLength = "GGGYITGIVAHPKTKDLLYARTDIGGAYRWDAGTSKWIPLNDFIEAQDMNIMGTESIALDPNNPDRLYLAQGRYVGDEWAAFYVSEDRGQSFTIYESPFPMGANDMGRNNGERLAVNPFNSNEVWMGTRTEGIWKSSDRAKTWTNVTSIPDAFTNGIGYTSVIFDPERNGTIYASATAPQGMYVTHDGGVSWEPVAGQPSSWLNRTTGAFPDKKPASIAPQPMKVALTPNFLYVTYADYPGPWGVTFGEVWRQNRTSGAWDDITPRVGNSSPAPYNNQTFPAGGFCGLSVDATNPNRLVVITLDRDPGPALDSIYLSTDAGATWKDVTQLSSPSNLEGNWGHPTNAARYKDGTPVPWLDFNNGPQWGGYGAPHGTPGLTKFGWWMSAVLIDPFNPEHLMYGTGATIWATDTLSRVEKDWAPS--WYLQIDGIEEKSTAKCANGQKGTHCY".length();
 //        Gotoh instance = new Gotoh(AlignmentMode.GLOBAL, AlignmentAlgorithm.GOTOH, matrix, gapCost);
 //        AlignmentResult result = instance.align(seq1Obj, seq2Obj);
 //        SequencePairAlignment alignment = result.getFirstAlignment();
 //        String exp1 = "GGGYITGIVAHPKTKDLLYARTDIGGAYRWDAGTSKWIPLNDFIEAQDMNIMGTESIALDPNNPDRLYLAQGRYVGDEWAAFYVSEDRGQSFTIYESPFPMGANDMGRNNGERLAVNPFNSNEVWMGTRTEGIWKSSDRAKTWTNVTSIPDAFTNGIGYTSVIFDPERNGTIYASATAPQGMYVTHDGGVSWEPVAGQPSSWLNRTTGAFPDKKPASIAPQPMKVALTPNFLYVTYADYPGPWGVTFGEVWRQNRTSGAWDDITPRVGNSSPAPYNNQTFPAGGFCGLSVDATNPNRLVVITLDRDPGPALDSIYLSTDAGATWKDVTQLSSPSNLEGNWGHPTNAARYKDGTPVPWLDFNNGPQWGGYGAPHGTPGLTKFGWWMSAVLIDPFNPEHLMYGTGATIWATDTLSRVEKDWAPS--WYLQIDGIEEKSTAKCANGQKGTHCY";
 //        String exp2 = "KPRAPGNLTVH---DTLLLT---------WS----------------------------NPYPPDNYLYNHLTYAVNIW-----SENDPADFRIYNVTY----------------------------------------------------------------LEP-------------------------------------------------------SLRIAAG-----ISYRARVRAWAQAYNTTW---------------------------------------------------------------------------------------------------------------------------------------------------------------------SEWSPSTKW-------------------------";
 //        System.out.println();
 //        System.out.println("A3Q: " + alignment.queryAlignment);
 //        System.out.println("E3Q: " + exp1);
 //        System.out.println("A3T: " + alignment.targetAlignment);
 //        System.out.println("E3T: " + exp2);
 //        assertEquals(-259.4, result.getScore(), 0.00000001);
 //        assertEquals(correctAlignmentLength, alignment.queryAlignment.length());
 //        assertEquals(correctAlignmentLength, alignment.targetAlignment.length());
 //        assertEquals(-259.400, CheckScoreCalculator.calculateCheckScoreAffine(AlignmentMode.GLOBAL, alignment, matrix, gapCost), 0.00000001);
 //        assertEquals(exp1, alignment.queryAlignment);
 //        assertEquals(exp2, alignment.targetAlignment);
 //    }
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
