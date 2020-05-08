 package tomato.math;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import tomato.math.IVec2D;
 import tomato.math.Vec2DPrecise;
 
 public class UTestVec2DPrecise {
 
 
 	@Test
 	public void testGetLength() {
 		IVec2D vector = new Vec2DPrecise(2.5, 3.1);
 		
 		assertTrue(Math.sqrt(2.5*2.5 + 3.1*3.1) == vector.getLength());
 		
 	}
 
 	@Test
 	public void testAddition() {
 		IVec2D a = new Vec2DPrecise(1.1, 2.9);
 		IVec2D b = new Vec2DPrecise(2.1, -4.1);
 		
 		IVec2D added = a.getConnectionVector(b);
 		
 		assertTrue(added.getX() == 1.0);
 		assertTrue(added.getY() == -7.0);	}
 
 	@Test
 	public void testMultiply() {
 
 		IVec2D vector = new Vec2DPrecise(2.5, 3.1);
 		vector = vector.multiply(2.0);
 
 		assertTrue(5.0 == vector.getX());
 		assertTrue(6.2 == vector.getY());
 
 	}
 
 	@Test
 	public void testGetConnectionVector() {
 		IVec2D a = new Vec2DPrecise(1.1, 2.9);
 		IVec2D b = new Vec2DPrecise(2.1, -4.1);
 		IVec2D connection = a.getConnectionVector(b);
 		
 		assertTrue(connection.getX() == 1.0);
 		assertTrue(connection.getY() == -7.0);
 	}
 
 	@Test
 	public void testPointProduct() {
 		IVec2D a = new Vec2DPrecise(1.1, 2.9);
 		IVec2D b = new Vec2DPrecise(2.1, -4.1);
 		// 2.31 - 11,89
 		double expected = -9.58;
		assertTrue("Point product is not correct. Given: " + a.dotProduct(b) + "|Expected: " + expected, Math.abs(a.dotProduct(b) - expected) <= 0.001);
 	}
 
 	@Test
 	public void testSubstract() {
 		IVec2D a = new Vec2DPrecise(1.1, 2.9);
 		IVec2D b = new Vec2DPrecise(2.1, -4.1);
 		
 		IVec2D substract = a.substract(b);
 		
 		assertTrue(substract.getX() == -1.0);
 		assertTrue(substract.getY() == 7.0);
 	}
 
 	@Test
 	public void testGetUnitVector() {
 		IVec2D vector = new Vec2DPrecise(2.5, 3.1);
 		double length = vector.getLength();
 		
		IVec2D unitVector = vector.getNormalized();
 		assertTrue(unitVector.getX() == vector.getX()/length);
 		assertTrue(unitVector.getY() == vector.getY()/length);
 		
 		assertTrue(Math.abs(unitVector.getLength() - 1) <= 0.001);
 
 	}
 
 	@Test
 	public void testToString() {
 		IVec2D vector = new Vec2DPrecise(2.5, 3.1);
 
 		assertEquals("[x = 2.5][y = 3.1][length = " + Math.sqrt(2.5*2.5 + 3.1*3.1) + "]", vector.toString());
 	}
 
 }
