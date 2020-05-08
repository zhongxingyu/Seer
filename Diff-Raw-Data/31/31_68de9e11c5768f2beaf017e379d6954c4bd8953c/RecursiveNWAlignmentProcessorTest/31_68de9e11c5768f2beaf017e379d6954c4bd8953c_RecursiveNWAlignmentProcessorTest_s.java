 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment.recursive;
 
 import de.bioinformaikmuenchen.pg4.common.Sequence;
 import de.bioinformaikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.alignment.AlignmentAlgorithm;
 import de.bioinformatikmuenchen.pg4.alignment.AlignmentMode;
 import de.bioinformatikmuenchen.pg4.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import static junit.framework.Assert.assertEquals;
 import junit.framework.TestCase;
 
 /**
  *
  * @author koehleru
  */
 public class RecursiveNWAlignmentProcessorTest extends TestCase {
 
     public RecursiveNWAlignmentProcessorTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     //Wikipedia does not provide the correct alignment
//    /**
//     * Test of align method, of class RecursiveNWAlignmentProcessor.
//     */
//    public void testAlignWikiepdia() {
//        Sequence seq1Obj = new Sequence("AGACTAGTTAC");
//        Sequence seq2Obj = new Sequence("CGAGACGT");
//        IDistanceMatrix matrix = new WikipediaAlignmentMatrix1();
//        IGapCost gapCost = new ConstantGapCost(-5);
//        RecursiveNWAlignmentProcessor instance = new RecursiveNWAlignmentProcessor(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
//        AlignmentResult result = instance.align(seq1Obj, seq2Obj);
//        assertEquals(16.0, result.getScore()); //Assuming this is correct
//
//    }
 
     /**
      * Test of align method, of class RecursiveNWAlignmentProcessor.
      * http://www.avatar.se/molbioinfo2001/dynprog/dynamic.html
      */
     public void testAlignAvatarSE() {
         Sequence seq1Obj = new Sequence("GAATTCAGTTA");
         Sequence seq2Obj = new Sequence("GGATCGA");
         IDistanceMatrix matrix = new ZeroOneAlignmentMatrix();
         IGapCost gapCost = new ConstantGapCost(0);
         RecursiveNWAlignmentProcessor instance = new RecursiveNWAlignmentProcessor(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(6.0, result.getScore());
     }
     
        /**
      * Test of align method, of class RecursiveNWAlignmentProcessor.
     * http://www.avatar.se/molbioinfo2001/dynprog/dynamic.html
      */
     public void testAlignSimple() {
         Sequence seq1Obj = new Sequence("GAATT");
         Sequence seq2Obj = new Sequence("GGATC");
         IDistanceMatrix matrix = new WikipediaAlignmentMatrix1();
         IGapCost gapCost = new ConstantGapCost(-5);
         RecursiveNWAlignmentProcessor instance = new RecursiveNWAlignmentProcessor(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, matrix, gapCost);
         AlignmentResult result = instance.align(seq1Obj, seq2Obj);
         assertEquals(24.0, result.getScore());
 
     }
 }
