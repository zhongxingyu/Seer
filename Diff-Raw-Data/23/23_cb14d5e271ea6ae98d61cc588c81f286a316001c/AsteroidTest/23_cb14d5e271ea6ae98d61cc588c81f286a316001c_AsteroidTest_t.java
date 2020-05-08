 package model;
 
 import static org.junit.Assert.*;
 
import java.awt.geom.Point2D;

 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AsteroidTest {
 	Asteroid asteroid;
 
 	@Before
 	public void setUp() {
		asteroid = new Asteroid(new Point2D.Double(0, 0), 10, new Point2D.Double(1, 1));
 	}
 
 	@Test
 	public void test_that_an_asteroid_has_a_positive_score() {
 		assertTrue(asteroid.getPoints() >= 0);
 	}
 
 	@Test
 	public void test_that_an_asteroid_has_a_higher_score_if_smaller() {
		Asteroid asteroid2 = new Asteroid(new Point2D.Double(0, 0), 20, new Point2D.Double(1, 1));
 		assertTrue(asteroid.getPoints() > asteroid2.getPoints());
 	}
 
 	@Test
 	public void test_that_an_asteroid_has_a_higher_score_if_faster() {
		Asteroid asteroid2 = new Asteroid(new Point2D.Double(0, 0), 10, new Point2D.Double(5, 2));
 		assertTrue(asteroid.getPoints() < asteroid2.getPoints());
 	}
 
 	@Test
 	public void test_that_a_big_slow_asteroid_has_1_point() {
		Asteroid asteroid = new Asteroid(new Point2D.Double(100, 100), 30, new Point2D.Double(1, 1));
 
 		assertEquals(1, asteroid.getPoints());
 	}
 
 	@Test
 	public void test_that_a_normal_sized_faster_asteroid_has_2_points() {
		Asteroid asteroid = new Asteroid(new Point2D.Double(100, 100), 30, new Point2D.Double(5, 0));
 
 		assertEquals(2, asteroid.getPoints());
 	}
 
 	@Test
 	public void test_that_a_smaller_slow_asteroid_has_2_points() {
		Asteroid asteroid = new Asteroid(new Point2D.Double(100, 100), 20, new Point2D.Double(1, 1));
 
 		assertEquals(2, asteroid.getPoints());
 	}
 
 	@Test
 	public void test_that_a_smaller_faster_asteroid_has_4_points() {
		Asteroid asteroid = new Asteroid(new Point2D.Double(100, 100), 20, new Point2D.Double(5, 5));
 
 		assertEquals(4, asteroid.getPoints());
 	}
 
 }
