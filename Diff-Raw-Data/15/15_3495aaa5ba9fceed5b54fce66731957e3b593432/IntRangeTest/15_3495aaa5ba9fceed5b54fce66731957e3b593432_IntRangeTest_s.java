 package de.sofd.util;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author olaf
  */
 public class IntRangeTest {
 
     public IntRangeTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testBasics() {
         System.out.println("Basics");
         IntRange ir = new IntRange(30, 50);
         assertEquals(30, ir.getMin());
         assertEquals(50, ir.getMax());
         assertEquals(20, ir.getDelta());
     }
 
     @Test
     public void testIntersection() {
         System.out.println("Intersection");
         IntRange r1 = new IntRange(20, 30);
         assertEquals(new IntRange(26, 30), r1.intersect(new IntRange(26, 40)));
         assertEquals(new IntRange(20, 30), r1.intersect(new IntRange(20, 30)));
         assertEquals(new IntRange(20, 20), r1.intersect(new IntRange(10, 20)));
         assertEquals(new IntRange(30, 30), r1.intersect(new IntRange(30, 40)));
         assertNull(r1.intersect(new IntRange(40, 50)));
         assertNull(r1.intersect(new IntRange(0, 10)));
         assertNull(r1.intersect(new IntRange(0, 19)));
         assertNull(r1.intersect(new IntRange(31, 40)));
     }
 
     @Test
     public void testSubtraction() {
         System.out.println("Subtraction");
         IntRange r1 = new IntRange(20, 30);
         assertArrayEquals(new IntRange[]{new IntRange(20,21), new IntRange(25,30)}, r1.subtract(new IntRange(22,24)));
         assertArrayEquals(new IntRange[]{new IntRange(20,25)}, r1.subtract(new IntRange(26,40)));
         assertArrayEquals(new IntRange[]{new IntRange(24,30)}, r1.subtract(new IntRange(15,23)));
         assertArrayEquals(new IntRange[]{new IntRange(20,30)}, r1.subtract(new IntRange(40,50)));
         assertArrayEquals(new IntRange[]{new IntRange(20,30)}, r1.subtract(new IntRange(0,10)));
         assertArrayEquals(new IntRange[]{new IntRange(20,29)}, r1.subtract(new IntRange(30,40)));
         assertArrayEquals(new IntRange[]{new IntRange(20,30)}, r1.subtract(new IntRange(31,40)));
         assertArrayEquals(new IntRange[]{new IntRange(20,30)}, r1.subtract(new IntRange(32,40)));
         assertArrayEquals(new IntRange[]{new IntRange(21,30)}, r1.subtract(new IntRange(10,20)));
         assertArrayEquals(new IntRange[]{new IntRange(20,30)}, r1.subtract(new IntRange(10,19)));
         assertArrayEquals(new IntRange[]{new IntRange(20,30)}, r1.subtract(new IntRange(10,18)));
         assertArrayEquals(new IntRange[]{}, r1.subtract(new IntRange(10,40)));
         assertArrayEquals(new IntRange[]{}, r1.subtract(new IntRange(20,30)));
         assertArrayEquals(new IntRange[]{new IntRange(30,30)}, r1.subtract(new IntRange(20,29)));
         assertArrayEquals(new IntRange[]{new IntRange(30,30)}, r1.subtract(new IntRange(10,29)));
         assertArrayEquals(new IntRange[]{new IntRange(20,20)}, r1.subtract(new IntRange(21,30)));
         assertArrayEquals(new IntRange[]{new IntRange(20,20)}, r1.subtract(new IntRange(21,40)));
         assertArrayEquals(new IntRange[]{new IntRange(20,20), new IntRange(30,30)}, r1.subtract(new IntRange(21,29)));
     }
 }
