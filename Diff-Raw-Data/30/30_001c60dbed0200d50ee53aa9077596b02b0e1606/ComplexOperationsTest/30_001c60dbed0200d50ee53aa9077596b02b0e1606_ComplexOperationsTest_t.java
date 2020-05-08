 package ru.ssau.karanashev.complex;
 
 import junit.framework.TestCase;
 import org.junit.Test;
 
 import static ru.ssau.karanashev.TestConstants.EPS;
 import static ru.ssau.karanashev.complex.ComplexOperations.exp;
 import static ru.ssau.karanashev.complex.ComplexOperations.mult;
 
 /**
  * User: Mukhamed Karanashev
  * Date: Sep 21, 2010
  * Time: 10:36:54 PM
  */
 public class ComplexOperationsTest extends TestCase {
 
     @Test
     public void testMult() {
         double a = 0, b = 0, c = 0, d = 0;
 
         double[] result = new double[2];
 
         result = mult(a, b, c, d, result);
 
         assertEquals(result[0], 0, EPS);
         assertEquals(result[1], 0, EPS);
 
         a = b = c = d = 1;
         result = mult(a, b, c, d, result);
 
         assertEquals(result[0], 0, EPS);
         assertEquals(result[1], 2, EPS);
 
         a = b = 1;
         c = 2;
         d = -2;
         result = mult(a, b, c, d, result);
 
         assertEquals(result[0], 4, EPS);
         assertEquals(result[1], 0, EPS);
     }
 
     @Test
     public void testExp() {
         double w = 0;
         double[] result = new double[2];

        result = exp(w, null);

        assertEquals(result[0], 1, EPS);
        assertEquals(result[1], 0, EPS);

         result = exp(w, result);
 
         assertEquals(result[0], 1, EPS);
         assertEquals(result[1], 0, EPS);
 
         w = Math.PI / 2;
         result = exp(w, result);
 
         assertEquals(result[0], 0, EPS);
         assertEquals(result[1], 1, EPS);
 
         w = Math.PI;
         result = exp(w, result);
 
         assertEquals(result[0], -1, EPS);
         assertEquals(result[1], 0, EPS);
 
         w = -Math.PI / 2;
         result = exp(w, result);
 
         assertEquals(result[0], 0, EPS);
         assertEquals(result[1], -1, EPS);
     }
 }
