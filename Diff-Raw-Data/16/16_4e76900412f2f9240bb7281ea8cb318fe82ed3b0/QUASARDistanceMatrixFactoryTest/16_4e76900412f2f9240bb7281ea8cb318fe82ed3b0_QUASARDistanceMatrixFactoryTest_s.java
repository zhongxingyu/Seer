 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.common.distance;
 
 import java.io.InputStreamReader;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import junit.framework.TestCase;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author koehleru
  */
 public class QUASARDistanceMatrixFactoryTest extends TestCase {
 
     public QUASARDistanceMatrixFactoryTest() {
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
      * Test of factorize method, of class QUASARDistanceMatrixFactory.
      */
     @Test
     public void testFactorize() throws Exception {
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(
                 new InputStreamReader(
                 QUASARDistanceMatrixFactoryTest.class.getResourceAsStream("/de/bioinformatikmuenchen/pg4/common/distance/blosum80.mat")));
         assertEquals(7.0, matrix.distance('A', 'A'), 0.0000001);
         assertEquals(-3.0, matrix.distance('A', 'R'), 0.0000001);
         assertEquals(-3.0, matrix.distance('R', 'A'), 0.0000001);
         assertEquals(9, matrix.distance('R', 'R'), 0.0000001);
         assertEquals(-1.0, matrix.distance('C', 'A'), 0.0000001);
         assertEquals(-1.0, matrix.distance('A', 'C'), 0.0000001);
         //Check if there's a value for every pair
         String rowIndex = "ARNDCQEGHILKMFPSTWYV";
         String colIndex = "ARNDCQEGHILKMFPSTWYV";
         //Check if the matrix has data for all character pairs
         for (int y = 0; y < colIndex.length(); y++) {
             for (int x = 0; x < rowIndex.length(); x++) {
                 char A = rowIndex.charAt(x);
                 char B = colIndex.charAt(y);
                 assertTrue("No data for " + A + " " + B + ": " + matrix.distance(A, B), matrix.distance(A, B) >= Double.NEGATIVE_INFINITY);
                 assertTrue("No data for " + B + " " + A + ": " + matrix.distance(B, A), matrix.distance(B, A) >= Double.NEGATIVE_INFINITY);
             }
         }
     }
 
     @Test
     public void testDayhoff() throws Exception {
         IDistanceMatrix matrix = QUASARDistanceMatrixFactory.factorize(
                 new InputStreamReader(
                 QUASARDistanceMatrixFactoryTest.class.getResourceAsStream("/de/bioinformatikmuenchen/pg4/common/distance/dayhoff.mat")));
         //Check if there's a value for every pair
         String rowIndex = "ARNDCQEGHILKMFPSTWYV";
         String colIndex = "ARNDCQEGHILKMFPSTWYV";
         assertEquals(-1.60, matrix.distance('A', 'R'), 0.0000000001);
         assertEquals(-1.60, matrix.distance('R', 'A'), 0.0000000001);
         assertEquals(1.8, matrix.distance('A', 'A'), 0.0000000001);
         //Check if the matrix has data for all character pairs
         for (int y = 0; y < colIndex.length(); y++) {
             for (int x = 0; x < rowIndex.length(); x++) {
                 char A = rowIndex.charAt(x);
                 char B = colIndex.charAt(y);
                 assertTrue("No data for " + A + " " + B + ": " + matrix.distance(A, B), matrix.distance(A, B) >= Double.NEGATIVE_INFINITY);
                 assertTrue("No data for " + B + " " + A + ": " + matrix.distance(B, A), matrix.distance(B, A) >= Double.NEGATIVE_INFINITY);
             }
         }
     }
 }
