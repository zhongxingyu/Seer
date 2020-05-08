 package ie.dit.dt211.java;
 
 import static org.junit.Assert.*;
 import static java.lang.Math.PI;
 
 import org.junit.Test;
 
 public class ShapeTest {
 
 	@Test public void createsACircle() {
 		assertTrue(new Circle(2.0) instanceof Shape);
 	}
 
 	@Test public void getsAreaOfACircle() {
 		assertEquals(PI * 2.0 * 2.0, new Circle(2.0).area(), 0.000000001);
 	}
 	
 	@Test public void createsASquare() {
 		assertTrue(new Square(2.0) instanceof Shape);
 	}
 	
 	@Test public void getsAreaOfASquare() {
 		assertEquals(4.0, new Square(2.0).area(), 0.000000001);
 	}
 	
 	@Test public void createsACube() {
 		assertTrue(new Cube(2.0) instanceof Shape);
 	}
 	
 	@Test public void getsAreaOfACube() {
 		assertEquals(24.0, new Cube(2.0).area(), 0.000000001);
 	}
 	
 	@Test public void getsVolumeOfACube() {
 		assertEquals(8.0, new Cube(2.0).volume(), 0.000000001);
 	}
 	
 	@Test public void createsASphere() {
 		assertTrue(new Sphere(2.0) instanceof Shape);
 	}
 	
 	@Test public void getsAreaOfASphere() {
		assertEquals(24.0, new Cube(2.0).area(), 0.000000001);
 	}
 	
 	@Test public void getsVolumeOfASphere() {
 		assertEquals(4.0/3.0 * PI * 8.0, new Sphere(2.0).volume(), 0.000000001);
 	}
 }
