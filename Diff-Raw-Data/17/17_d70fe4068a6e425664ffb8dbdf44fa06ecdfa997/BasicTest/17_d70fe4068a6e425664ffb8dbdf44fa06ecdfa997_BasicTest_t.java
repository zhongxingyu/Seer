 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kryptoprojekt.model;
 
 import java.util.LinkedList;
import java.util.Random;
 import junit.framework.TestCase;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Stefan
  */
 public class BasicTest extends TestCase {
 
     public BasicTest(String name) {
         super(name);
     }
 
     public void testGCD() {
         Tuple<Z, LinkedList<Z[]>> gcd = Basic.gcd(new Z(2), new Z(10));
         assertEquals(gcd.first(), new Z(2));
 
         gcd = Basic.gcd(new Z("1278007"), new Z("8910141879815"));
         assertEquals(gcd.first(), new Z(1));
     }
 
     public void testSquareAndMultiply() {
         assertEquals(Basic.squareAndMultiply(new Z(12), new Z(2)).first(), new Z(144));
         assertEquals(Basic.squareAndMultiply(new Z(2000), new Z(0)).first(), new Z(1));
         assertEquals(Basic.squareAndMultiply(new Z(10), new Z(10)).first(), new Z("10000000000"));
         assertEquals(Basic.squareAndMultiply(new Z(7), new Z(7)).first(), new Z(823543));
         assertEquals(Basic.squareAndMultiply(new Z(3), new Z(18)).first(), new Z(387420489));
 
         assertEquals(Basic.squareAndMultiply(new PrimeFieldElement(3, 7), new Z(18)).first(), new PrimeFieldElement(1, 7));
         assertEquals(Basic.squareAndMultiply(new PrimeFieldElement(13, 37), new Z(1000)).first(), new PrimeFieldElement(33, 37));
 
         try {
             Basic.squareAndMultiply(new Z(12), new Z(-1));
             fail();
         } catch (IllegalArgumentException iae) {
         }
     }
 
     public void testSquareAndMultiplyModulo() {
         Z result = new Z(0);
         for (int i = 1; i <= 1000; i++) {
             result = result.add(Basic.squareAndMultiply(new Z(i), new Z(i), new Z(10000000000l)).first());
         }
         assertEquals(result.mod(new Z(10000000000l)), new Z(9110846700l));
 
         try {
             Basic.squareAndMultiply(new Z(12), new Z(-1), new Z(10));
             fail();
         } catch (IllegalArgumentException iae) {
         }
 
         try {
             Basic.squareAndMultiply(new Z(12), new Z(10), new Z(0));
             fail();
         } catch (IllegalArgumentException iae) {
         }
     }
 
     public void testPhi() {
         for (int i = 0; i <= 100; i++) {
             assertEquals(Basic.phi(new Z(900 + i)).first(), new Z(phi900[i]));
         }
         LinkedList<Z> factors = Basic.phi(new Z("1234567812345678")).second();
         int index = 0;
         Z[] equation = {new Z(2), new Z(3), new Z(3), new Z(17), new Z(47), new Z(14593), new Z(5882353)};
         for (Z z : factors) {
             assertEquals(z, equation[index++]);
         }
     }
 
     public void testExtendedGCD() {
        
     }
 
     public void testSqrt() {
         assertTrue(Basic.sqrt(new Z(0)).isZERO());
 
         Z add = new Z(1);
         Z number = new Z(0);
         for (int i = 1; i <= 100; i++) {
             number = number.add(add);
             assertEquals(Basic.sqrt(number), new Z(i));
             add = add.add(new Z(2));
         }
 
         try {
             Basic.sqrt(new Z(-1));
             fail();
         } catch (IllegalArgumentException iae) {
         }
     }
     private int[] phi900 = {
         240, 832, 400, 504, 448, 720, 300, 906, 452, 600, 288, 910, 288, 820, 456, 480,
         456, 780, 288, 918, 352, 612, 460, 840, 240, 720, 462, 612, 448, 928, 240, 756,
         464, 620, 466, 640, 288, 936, 396, 624, 368, 940, 312, 880, 464, 432, 420, 946,
         312, 864, 360, 632, 384, 952, 312, 760, 476, 560, 478, 816, 256, 930, 432, 636,
         480, 768, 264, 966, 440, 576, 384, 970, 324, 828, 486, 480, 480, 976, 324, 880,
         336, 648, 490, 982, 320, 784, 448, 552, 432, 924, 240, 990, 480, 660, 420, 792,
         328, 996, 498, 648, 400};
 }
