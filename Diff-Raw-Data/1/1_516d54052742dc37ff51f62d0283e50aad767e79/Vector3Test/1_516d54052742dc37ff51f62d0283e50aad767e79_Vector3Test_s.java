 package math;
 
 import static org.junit.Assert.*;
 
 import math.Vector3;
 
 import org.junit.Test;
 
 public class Vector3Test {
     private static final double EPSILON = 1.00E-6;
 
 
     @Test
     public void testVector3() {
         Vector3 v = new Vector3();
         
         assertEquals(0.0f, v.x, EPSILON);
         assertEquals(0.0f, v.y, EPSILON);
         assertEquals(0.0f, v.z, EPSILON);
     }
 
     @Test
     public void testVector3FloatFloatFloat() {
         float x = 1.23f;
         float y = -2.34f;
         float z = 0.23f;
         
         Vector3 v = new Vector3(x, y, z);
         
         assertEquals(x, v.x, EPSILON);
         assertEquals(y, v.y, EPSILON);
         assertEquals(z, v.z, EPSILON);
 
     @Test
     public void testSameDirection(){
         assertTrue(Vector3.UNIT_X.sameDirection(Vector3.UNIT_X));
         assertFalse(Vector3.UNIT_X.sameDirection(Vector3.UNIT_X.negate()));
         assertFalse(Vector3.UNIT_X.sameDirection(Vector3.UNIT_Y));
         assertFalse(Vector3.UNIT_X.sameDirection(Vector3.UNIT_Z));
     }
 
     @Test
     public void testVector3Vector3() {
         Vector3 original = new Vector3(1.23f, -2.34f, 0.23f);
         
         Vector3 copy = new Vector3(original);
 
         assertVector3Equals(original, copy);
     }
 
     @Test
     public void testToString() {
         Vector3 v = new Vector3(1.23f, -2.34f, 0.23f);
         assertEquals("<1.23000, -2.34000, 0.230000>", v.toString());
     }
 
     @Test
     public void testTimesFloat() {
         Vector3 v1 = new Vector3(0.0f, -2.0f, 6.3f);
         assertVector3Equals(new Vector3(0.0f, -4.0f, 12.6f),v1.times(2.0f));
         assertVector3Equals(v1,v1.times(1.0f));
     }
 
     @Test
     public void testPlus() {
         Vector3 v1 = new Vector3(1.0f,1.0f,1.0f);
         Vector3 v1_copy = new Vector3(v1);
         Vector3 v2 = new Vector3( -3.0f, 0.0f, 7.2f);
         Vector3 v2_copy = new Vector3(v2);
         assertVector3Equals(new Vector3(4.0f, 1.0f, -6.2f), v1.minus(v2));
         assertVector3Equals(v1_copy, v1);
         assertVector3Equals(v2_copy, v2);
         
     }
 
     @Test
     public void testPlusEquals() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testNormalize() {
         assertEquals(Vector3.UNIT_X, (new Vector3(5,0,0)).normalize());
         assertEquals(Vector3.UNIT_Y, (new Vector3(0,5,0)).normalize());
         assertEquals(Vector3.UNIT_Z, (new Vector3(0,0,5)).normalize());
         assertEquals(Vector3.ORIGIN, Vector3.ORIGIN.normalize());
     }
 
     @Test
     public void testDotProduct() {
         assertEquals(Vector3.UNIT_X.dotProduct(Vector3.UNIT_X),1.0f,EPSILON);
         assertEquals(Vector3.UNIT_X.dotProduct(Vector3.UNIT_X.negate()),-1.0f,EPSILON);
         assertEquals(Vector3.UNIT_X.dotProduct(Vector3.UNIT_Y),0.0f,EPSILON);
         assertEquals(Vector3.UNIT_X.dotProduct(Vector3.UNIT_Y.negate()),0.0f,EPSILON);
 
         assertEquals(Vector3.UNIT_X.dotProduct(Vector3.UNIT_Z),0.0f,EPSILON);
         assertEquals(Vector3.UNIT_X.dotProduct(Vector3.UNIT_Z.negate()),0.0f,EPSILON);
     }
 
     @Test
     public void testCross() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testProjectionFrom() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testProjectionTo() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testTimesQuaternion() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testTimesFloatArray() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testMagnitude() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testMinus() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testMinusEquals() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testEqualsVector3() {
         assertTrue(Vector3.UNIT_X.equals(Vector3.UNIT_X));
         assertTrue(Vector3.UNIT_X.equals(new Vector3(Vector3.UNIT_X)));
     }
 
     @Test
     public void testMagnitude2() {
         fail("Not yet implemented"); // TODO
     }
 
     @Test
     public void testNegate() {
         fail("Not yet implemented"); // TODO
     }
 
     public static void assertVector3Equals(Vector3 a, Vector3 b) {
         assertVector3Equals(a, b, EPSILON);
     }
 
     
     public static void assertVector3Equals(Vector3 a, Vector3 b, double delta) {
         assertEquals(a.x, b.x, delta);
         assertEquals(a.y, b.y, delta);
         assertEquals(a.z, b.z, delta);
     }
 }
