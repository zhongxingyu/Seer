 package org.clafer.ir;
 
 import java.util.Random;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author jimmy
  */
 public class IrUtilTest {
 
     private final Random rand = new Random();
 
     private int randInt() {
         return rand.nextInt(201) - 100;
     }
 
     private IrDomain randDomain() {
         switch (rand.nextInt(20)) {
             case 0:
             case 1:
             case 2:
             case 3:
             case 4:
             case 5:
             case 6:
             case 7:
             case 8:
                 int[] values = new int[rand.nextInt(100) + 1];
                 for (int i = 0; i < values.length; i++) {
                     values[i] = rand.nextBoolean() ? rand.nextInt(100) : -rand.nextInt(100);
                 }
                 return Irs.enumDomain(values);
             case 9:
             case 10:
             case 11:
             case 12:
             case 13:
             case 14:
             case 15:
             case 16:
             case 17:
                 int low = rand.nextBoolean() ? rand.nextInt(100) : -rand.nextInt(100);
                 int high = low + rand.nextInt(100) + 1;
                 return Irs.boundDomain(low, high);
             default:
                 return Irs.EmptyDomain;
         }
     }
 
     @Test
     public void testIntersects() {
         IrDomain d1 = randDomain();
         IrDomain d2 = randDomain();
        assertEquals(!IrUtil.intersection(d1, d2).isEmpty() || (d1.isEmpty() && d2.isEmpty()),
                IrUtil.intersects(d1, d2));
     }
 
     @Test
     public void testIsSubsetOf() {
         IrDomain d1 = randDomain();
         IrDomain d2 = randDomain();
         assertEquals(IrUtil.intersection(d1, d2).equals(d1), IrUtil.isSubsetOf(d1, d2));
     }
 
     @Test
     public void testAdd() {
         IrDomain d = randDomain();
         int val = randInt();
         IrDomain add = IrUtil.add(d, val);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d.contains(i) || val == i, add.contains(i));
         }
     }
 
     @Test
     public void testRemove() {
         IrDomain d = randDomain();
         int val = randInt();
         IrDomain remove = IrUtil.remove(d, val);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d.contains(i) && val != i, remove.contains(i));
         }
     }
 
     @Test
     public void testBoundLow() {
         IrDomain d = randDomain();
         int low = randInt();
         IrDomain bound = IrUtil.boundLow(d, low);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d.contains(i) && i >= low, bound.contains(i));
         }
     }
 
     @Test
     public void testBoundHigh() {
         IrDomain d = randDomain();
         int high = randInt();
         IrDomain bound = IrUtil.boundHigh(d, high);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d.contains(i) && i <= high, bound.contains(i));
         }
     }
 
     @Test
     public void testDifference() {
         IrDomain d1 = randDomain();
         IrDomain d2 = randDomain();
         IrDomain difference = IrUtil.difference(d1, d2);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d1.contains(i) && !d2.contains(i), difference.contains(i));
         }
     }
 
     @Test
     public void testIntersection() {
         IrDomain d1 = randDomain();
         IrDomain d2 = randDomain();
         IrDomain intersection = IrUtil.intersection(d1, d2);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d1.contains(i) && d2.contains(i), intersection.contains(i));
         }
     }
 
     @Test
     public void testUnion() {
         IrDomain d1 = randDomain();
         IrDomain d2 = randDomain();
         IrDomain union = IrUtil.union(d1, d2);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d1.contains(i) || d2.contains(i), union.contains(i));
         }
     }
 
     @Test
     public void testMinus() {
         IrDomain d = randDomain();
         IrDomain minus = IrUtil.minus(d);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d.contains(i), minus.contains(-i));
         }
     }
 
     @Test
     public void testOffset() {
         IrDomain d = randDomain();
         IrDomain offset = IrUtil.offset(d, 3);
         for (int i = -100; i <= 200; i++) {
             assertEquals(d.contains(i), offset.contains(i + 3));
         }
     }
 }
