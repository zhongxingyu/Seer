 package se.chalmers.kangaroo.model.utils;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import se.chalmers.kangaroo.model.utils.Position;
 /**
  * A testing class for testing Position.
  * @author twist3r
  *
  */
 public class PositionTest {
 
 	/**
 	 * Tests getY().
 	 */
 	@Test
 	public void testGetY() {
 		Position p = new Position(1,3);
 		assertTrue(p.getY()==3);
 	}
 	/**
 	 * Tests getX();
 	 */
 	@Test
 	public void testGetX() {
 		Position p = new Position(4,2);
 		assertTrue(p.getX()==4);
 	}
 	/**
 	 * Tests constructors.
 	 */
 	@Test
 	public void testPosition() {
 		Position p = new Position(4,2);
 		Position s = new Position(4,2);
 		assertTrue(p.getX()==4 && s.getX()==4 && p.getY()==2 && s.getY()==2);
 	}
 
 }
