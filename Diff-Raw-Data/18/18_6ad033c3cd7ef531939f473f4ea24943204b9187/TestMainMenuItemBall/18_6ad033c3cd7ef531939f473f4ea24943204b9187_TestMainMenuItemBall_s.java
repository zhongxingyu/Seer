 package com.karolmajta.stp.test.models;
 
 import com.karolmajta.stp.models.MainMenuItemBall;
 
 import junit.framework.TestCase;
 
 public class TestMainMenuItemBall extends TestCase {
 	public final float FLOAT_EPSILON = 1.0e-6f;
 	public final String LABEL = "Label";
 	public final float X0 = 35.2f;
 	public final float Y0 = 108.3f;
 	public final float R = 22.0f;
 	public final float X1 = -22.11f;
 	public final float Y1 = 14.82f;
 	public final float VX = 2.31f;
 	public final float VY = -8.32f;
 	public final float K = 2.0f;
 	public final float B = 3.0f;
 	public final float M = 10.0f;
 	public final long DT = 200;
 	
 	
 	private MainMenuItemBall b = new MainMenuItemBall
 			(
 					LABEL, X0, Y0, R, X1, Y1, VX, VY, K, B, M
 			);
 	
 	public void testGetBaseDx() {
 		assertEquals(X0-X1, b.getBaseDx(), FLOAT_EPSILON);
 	}
 	
 	public void testGetBaseDy() {
 		assertEquals(Y0-Y1, b.getBaseDy(), FLOAT_EPSILON);
 	}
 
 	/**
 	 * Test that next acceleration is calculated basing on spring-damper
 	 * equation.
 	 */
 	public void testGetNextAX() {
		float expected = -(K/M)*b.getBaseDx() - (B/M)*b.getCurrentVX();
 		float actual = b.getNextAX();
 		assertEquals(expected, actual, FLOAT_EPSILON);
 	}
 	
 	public void testGetNextAY() {
		float expected = -(K/M)*b.getBaseDy() - (B/M)*b.getCurrentVY();
 		float actual = b.getNextAY();
 		assertEquals(expected, actual, FLOAT_EPSILON);
 	}
 
 	/**
 	 * Test that next velocity is calculated basing on Newton's equation.
 	 */
 	public void testGetNextVX() {
 		float expected = b.getCurrentVX()+b.getNextAX()*DT;
 		float actual = b.getNextVX(DT);
 		assertEquals(expected, actual, FLOAT_EPSILON);
 	}
 	
 	public void testGetNextVY() {
 		float expected = b.getCurrentVY()+b.getNextAY()*DT;
 		float actual = b.getNextVY(DT);
 		assertEquals(expected, actual, FLOAT_EPSILON);
 	}
 
 	/**
 	 * Test that next x coordinate is calculated basing on Newton's equation.
 	 */
 	public void testGetNextCurrentX() {
 		float expected = X1+VX*DT;
 		float actual = b.getNextCurrentX(DT);
 		assertEquals(expected, actual, FLOAT_EPSILON);
 	}
 	
 	public void testGetNextCurrentY() {
 		float expected = Y1+VY*DT;
 		float actual = b.getNextCurrentY(DT);
 		assertEquals(expected, actual, FLOAT_EPSILON);
 	}
 	
 	public void testGetDistance() {
 		float dxSquared = (X0-X1)*(X0-X1);
 		float dySquared = (Y0-Y1)*(Y0-Y1);
 		float expectedDistance = (float)Math.sqrt(dxSquared+dySquared);
 		
 		assertEquals(expectedDistance, b.getBasePointDist(), FLOAT_EPSILON);
 	}
 	
 	/**
 	 * Test order of integration equations is fine.
 	 */
 	public void testIntegrate() {
 		float AX = b.getNextAX();
 		float AY = b.getNextAY();
 		float nextVX = VX+AX*DT;
 		float nextVY = VY+AY*DT;
 		float nextX = X1+VX*DT;
 		float nextY = Y1+VY*DT;
 		
 		b.integrate(DT);
 		
 		assertEquals(b.getCurrentVX(), nextVX);
 		assertEquals(b.getCurrentVY(), nextVY);
 		assertEquals(b.getCurrentX(), nextX);
 		assertEquals(b.getCurrentY(), nextY);
 	}
 
 	public void testIntegratesOnTick() {
 		float AX = b.getNextAX();
 		float AY = b.getNextAY();
 		float nextVX = VX+AX*DT;
 		float nextVY = VY+AY*DT;
 		float nextX = X1+VX*DT;
 		float nextY = Y1+VY*DT;
 		
 		b.tick(DT);
 		
 		assertEquals(b.getCurrentVX(), nextVX);
 		assertEquals(b.getCurrentVY(), nextVY);
 		assertEquals(b.getCurrentX(), nextX);
 		assertEquals(b.getCurrentY(), nextY);
 	}
 }
