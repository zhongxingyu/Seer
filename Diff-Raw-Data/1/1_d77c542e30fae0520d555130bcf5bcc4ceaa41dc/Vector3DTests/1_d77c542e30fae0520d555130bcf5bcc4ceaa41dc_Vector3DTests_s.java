 package com.smmsp.tests.maths;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.smmsp.math.Angles3D;
 import com.smmsp.math.Vector3D;
 
 public class Vector3DTests {
 
 	@Test
 	public void testAdd() {
 		final Vector3D a = new Vector3D(1, 2, 3);
 		final Vector3D b = new Vector3D(1, 2, 3);
 		
 		final Vector3D exp = new Vector3D(2, 4, 6);
 		assertEquals(exp, a.add(b));
 		assertEquals(exp, b.add(a));
 		
 	}
 
 	@Test
 	public void testSubtract() {
 		final Vector3D a = new Vector3D(1, 2, 3);
 		final Vector3D b = new Vector3D(1, 2, 3);
 		
 		final Vector3D exp = new Vector3D();
 		
 		assertEquals(exp, a.subtract(b));
 		assertEquals(exp, b.subtract(a));
 		
 	}
 
 	@Test
 	public void testInvert() {
 		final Vector3D a = new Vector3D(10, -10, 20);
 		
 		final Vector3D exp = new Vector3D(-10, 10, -20);
 		assertEquals(exp, a.invert());
 	}
 
 	@Test
 	public void testDirectionAnglesRadians() {
 	}
 
 	@Test
 	public void testExample1_1(){
 		final Vector3D A = new Vector3D(1, -4, 8);
 		
 		final double mag = A.magnitude();
 		assertEquals(9, mag, 0.1);
 		
 		final Angles3D thetas = A.directionAnglesDegrees();
 		
 		assertEquals(83.62, thetas.getThetaI(), 0.01);
 		assertEquals(116.4, thetas.getThetaJ(), 0.02);
 		assertEquals(27.27, thetas.getThetaK(), 0.02);
 		
 	}
 	
 	@Test
 	public void testExample1_2(){
 		final Vector3D A = new Vector3D(1, 6, 18);
 		final Vector3D B = new Vector3D(42, -69, 98);
 		
 		final double magA = A.magnitude();
 		final double magB = B.magnitude();
 		
 		assertEquals(19, magA, 0.1);
 		assertEquals(127, magB, 0.1);
 		
 		// Dot product is communicative
 		assertEquals(1392, A.dotProduct(B), 0.1);
 		assertEquals(1392, B.dotProduct(A), 0.1);
 	
 		assertEquals(54.77, A.angleBetweenInDegrees(B), 0.01);
 		assertEquals(54.77, B.angleBetweenInDegrees(A), 0.01);
 		
 		assertEquals(10.96, A.projectOnTo(B), 0.01);
 		assertEquals(73.26, B.projectOnTo(A), 0.01);
 	}
 	
 	@Test
 	public void testExample1_3(){
 		final Vector3D A = new Vector3D(-3, 7, 9);
 		final Vector3D B = new Vector3D(6, -5, 8);
 		
 		final Vector3D AxB = new Vector3D(101, 78, -27);
 		assertEquals(AxB, A.crossProduct(B));
 
 		final Vector3D DxA = new Vector3D(891, -828, 941);
 		assertEquals(DxA, A.crossProduct(B).crossProduct(A));
 		
 	}
 }
