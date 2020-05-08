 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe.data;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author michael
  */
 public class EvaluationReportTest {
 
     public EvaluationReportTest() {
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
      * Test of save method, of class EvaluationReport.
      */
     @Test
     public void testSave() throws Exception {
         System.out.println("save");
 
         EvaluationReport eval = new EvaluationReport();
         eval.setTrueNegativeCount(2);
         eval.setTruePositiveCount(1);
         eval.setFalseNegativeCount(3);
         eval.setFalsePositiveCount(4);
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         assertTrue(eval.save(out));
         out.close();
 
         String nl = "\n";
         String expectedReport = "TP: 1" + nl
                 + "FP: 4" + nl
                 + "TN: 2" + nl
                 + "FN: 3";
 
         String report = out.toString();
 
        assertTrue(report.startsWith(expectedReport));
     }
 
     /**
      * Test of addPartial method, of class EvaluationReport.
      */
     @Test
     public void testAddPartial() {
         System.out.println("addPartial");
         EvaluationReport partial = new EvaluationReport();
         partial.setTrueNegativeCount(1);
         partial.setTruePositiveCount(2);
         partial.setFalseNegativeCount(3);
         partial.setFalsePositiveCount(4);
 
         EvaluationReport instance = new EvaluationReport();
         instance.addPartial(partial);
 
         assertEquals(partial.getTrueNegativeCount(), instance.getTrueNegativeCount());
         assertEquals(partial.getFalseNegativeCount(), instance.getFalseNegativeCount());
         assertEquals(partial.getTruePositiveCount(), instance.getTruePositiveCount());
         assertEquals(partial.getFalsePositiveCount(), instance.getFalsePositiveCount());
 
         instance.addPartial(partial);
 
         assertEquals(2 * partial.getTrueNegativeCount(), instance.getTrueNegativeCount());
         assertEquals(2 * partial.getFalseNegativeCount(), instance.getFalseNegativeCount());
         assertEquals(2 * partial.getTruePositiveCount(), instance.getTruePositiveCount());
         assertEquals(2 * partial.getFalsePositiveCount(), instance.getFalsePositiveCount());
 
     }
 
     /**
      * Test of recordPrediction method, of class EvaluationReport.
      */
     @Test
     public void testRecordPrediction() {
         System.out.println("recordPrediction");
 
         EvaluationReport instance = new EvaluationReport();
         instance.recordPrediction(true, true);
         assertEquals(1, instance.getTruePositiveCount());
 
         instance.recordPrediction(true, false);
         assertEquals(1, instance.getFalsePositiveCount());
 
         instance.recordPrediction(false, false);
         assertEquals(1, instance.getTrueNegativeCount());
 
         instance.recordPrediction(false, true);
         assertEquals(1, instance.getFalseNegativeCount());
 
     }
 }
