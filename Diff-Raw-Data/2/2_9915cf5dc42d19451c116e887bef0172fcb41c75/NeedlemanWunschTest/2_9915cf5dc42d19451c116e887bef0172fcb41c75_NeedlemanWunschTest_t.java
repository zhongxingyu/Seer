 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformaikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.recursive.ZeroOneAlignmentMatrix;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
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
     public void testAlignZeroOneMatrix() {
        NeedlemanWunsch w = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, new ZeroOneAlignmentMatrix(), new ConstantGapCost(0));
         AlignmentResult result = w.align(new Sequence("ACGA"), new Sequence("TCCG"));
         System.out.println("Score: "+result.getScore());
         assertEquals(6, result.getScore(), 0.00000001);
         //assertEquals("G-AATTCAGTTA", currentAlignment.getSequence());
         
     }
     
     
 //    @Test
 //    public void testAlignRealMatrix() throws IOException {
 //        NeedlemanWunsch nw = new NeedlemanWunsch(AlignmentMode.GLOBAL, AlignmentAlgorithm.NEEDLEMAN_WUNSCH, QUASARDistanceMatrixFactory.factorize("/matrices/blosum80.mat"), null);        
 //    }
 }
