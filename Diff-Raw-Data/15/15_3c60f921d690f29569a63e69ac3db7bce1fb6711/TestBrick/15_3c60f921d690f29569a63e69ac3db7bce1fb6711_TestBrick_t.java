 package model;
 
 import static org.junit.Assert.*;

 
 import org.junit.Before;
 import org.junit.Test;
 
 import model.Brick;
 
 public class TestBrick {
 	
 	private Brick brick;
 
 	@Before
 	public void setUp() throws Exception {
		brick = Brick.getInstance();
 	}
 
 	@Test
 	public void testGetInstance() {
 		assertNotNull(brick);
 	}
 
 	@Test
 	public void testReset() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testTurn() {
 		fail("Not yet implemented");
 	}
 
 }
