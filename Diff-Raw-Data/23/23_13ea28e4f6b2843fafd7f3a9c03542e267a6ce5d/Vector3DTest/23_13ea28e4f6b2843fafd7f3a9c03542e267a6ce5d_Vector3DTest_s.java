 /**
  * Vector3DTest.java - test for the Vector3D class
  */
 package edu.bu.cs.cs480;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 /**
  * Test for the Vector3D class.
  * 
  * @author Jeffrey Finkelstein <jeffrey.finkelstein@gmail.com>
  * @since Spring 2011
  */
 public class Vector3DTest {
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs680.Vector3D#crossProduct(edu.bu.cs.cs680.Vector3D)}.
    */
   @Test
   public void testCrossProduct() {
     final Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
     final Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
     final Vector3D v3 = new Vector3D(2.0, 0.0, 0.0);
     Vector3D result = v1.crossProduct(v2);
     assertEquals(0.0, result.x(), 0.0);
     assertEquals(0.0, result.y(), 0.0);
     assertEquals(1.0, result.z(), 0.0);
     result = v2.crossProduct(v3);
     assertEquals(0.0, result.x(), 0.0);
     assertEquals(0.0, result.y(), 0.0);
     assertEquals(-2.0, result.z(), 0.0);
   }
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs680.Vector3D#difference(edu.bu.cs.cs680.Vector3D)}.
    */
   @Test
   public void testDifference3D() {
     final Vector3D v1 = new Vector3D(1.0, 0.0, 1.0);
     final Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
     final Vector3D result = v1.difference(v2);
     assertEquals(1.0, result.x(), 0.0);
     assertEquals(-1.0, result.y(), 0.0);
     assertEquals(1.0, result.z(), 0.0);
   }
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs680.Vector3D#equalTo(edu.bu.cs.cs680.Vector3D)}.
    */
   @Test
   public void testEqualTo() {
     final Vector3D v1 = new Vector3D(1, 2, 3);
     final Vector3D v2 = new Vector3D(1, 2, 3);
     final Vector3D v3 = new Vector3D(1, 2, 4);
     assertTrue(v1.equalTo(v1));
     assertTrue(v2.equalTo(v2));
     assertTrue(v3.equalTo(v3));
     assertTrue(v1.equalTo(v2));
     assertTrue(v2.equalTo(v1));
     assertFalse(v1.equalTo(v3));
     assertFalse(v3.equalTo(v1));
     assertFalse(v2.equalTo(v3));
     assertFalse(v3.equalTo(v2));
   }
 
   /**
   * Test method for {@link edu.bu.cs.cs680.Vector3D#norm()}.
    */
   @Test
   public void testNorm() {
     Vector3D v1 = new Vector3D(3.0, 0.0, 4.0);
     assertEquals(5.0, v1.norm(), 0.0);
     v1 = new Vector3D(3.0, 0.0, 0.0);
     assertEquals(3.0, v1.norm(), 0.0);
   }
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs680.Vector3D#Vector3D(double, double, double)}.
    */
   @Test
   public void testVector3DDoubleDoubleDouble() {
     final Vector3D vector = new Vector3D(0.0d, 1.0d, 2.0d);
     assertEquals(0.0, vector.x(), 0.0);
     assertEquals(1.0, vector.y(), 0.0);
     assertEquals(2.0, vector.z(), 0.0);
   }
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs680.Vector3D#Vector3D(float, float, float)}.
    */
   @Test
   public void testVector3DFloatFloatFloat() {
     final Vector3D vector = new Vector3D(0.0f, 1.0f, 2.0f);
     assertEquals(0.0, vector.x(), 0.0);
     assertEquals(1.0, vector.y(), 0.0);
     assertEquals(2.0, vector.z(), 0.0);
   }
 
   /**
   * Test method for {@link edu.bu.cs.cs680.Vector3D#scaledBy(double)}.
    */
   @Test
   public void testScaledBy() {
     final Vector3D v1 = new Vector3D(1.0, 0.0, -1.0);
     final float scale = 10;
     assertEquals(scale, v1.scaledBy(scale).x(), 0.0);
     assertEquals(0.0, v1.scaledBy(scale).y(), 0.0);
     assertEquals(-scale, v1.scaledBy(scale).z(), 0.0);
   }
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs680.Vector3D#sumWith(edu.bu.cs.cs680.Vector3D)}.
    */
   @Test
   public void testSumWith() {
     final Vector3D v1 = new Vector3D(1.0, 0.0, 1.0);
     final Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
     assertEquals(1.0, v1.sumWith(v2).x(), 0.0);
     assertEquals(1.0, v1.sumWith(v2).y(), 0.0);
     assertEquals(1.0, v1.sumWith(v2).z(), 0.0);
   }
 
 }
