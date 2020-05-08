 package ConwaysGameOfLife;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class ConwaysGameOfLifeTest {
 
 	private ConwaysGameOfLife conwaysGameOfLife;
 	
 	@Before
 	public void setUp() throws Exception {
 		conwaysGameOfLife = new ConwaysGameOfLife(6, 6);
 	}
 	
 	@Test
 	public void testGetStateAtPosition(){
		conwaysGameOfLife.getStateAtPosition(0, 0);
 		assertFalse(conwaysGameOfLife.getStateAtPosition(0, 0));
 	}
 
 	@Test
 	public void testSetStateAtPosition() {
 		conwaysGameOfLife.setStateAtPosition(0, 0, true);
 		assertTrue(conwaysGameOfLife.getStateAtPosition(0, 0));
 	}
 	
 	
 	/**
 	 * Expect the following evolution to occur (6x6 matrix; 'x' == dead, '0' == alive ):
 	 
 	   0 0 x x x x
 	   0 x x x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	   
 	   0 0 x x x x
 	   0 0 x x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	 
 	 
 	 */
 	@Test
 	public void testEvolveSimple(){
 		conwaysGameOfLife.setStateAtPosition(0, 0, true);
 		conwaysGameOfLife.setStateAtPosition(0, 1, true);
 		conwaysGameOfLife.setStateAtPosition(1, 0, true);
 		conwaysGameOfLife.evolve();
 		assertTrue(conwaysGameOfLife.getStateAtPosition(0, 0));
 		assertTrue(conwaysGameOfLife.getStateAtPosition(0, 1));
 		assertTrue(conwaysGameOfLife.getStateAtPosition(1, 0));
 		assertTrue(conwaysGameOfLife.getStateAtPosition(1, 1));
 	}
 	
 	/**
 	 * Expect the following evolution to occur (6x6 matrix; 'x' == dead, '0' == alive ):
 	 
 	   x x x x x x
 	   0 0 0 x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	   
 	   x 0 x x x x
 	   x 0 x x x x
 	   x 0 x x x x
 	   x x x x x x
 	   x x x x x x
 	   x x x x x x
 	 
 	 
 	 */
 	@Test
 	public void testEvolveBlinker(){
 		conwaysGameOfLife.setStateAtPosition(0, 1, true);
 		conwaysGameOfLife.setStateAtPosition(1, 1, true);
 		conwaysGameOfLife.setStateAtPosition(2, 1, true);
 		conwaysGameOfLife.evolve();
 		assertFalse(conwaysGameOfLife.getStateAtPosition(0, 1));
 		assertFalse(conwaysGameOfLife.getStateAtPosition(2, 1));
 		assertTrue(conwaysGameOfLife.getStateAtPosition(1, 0));
 		assertTrue(conwaysGameOfLife.getStateAtPosition(1, 1));
 		assertTrue(conwaysGameOfLife.getStateAtPosition(1, 2));
 	}
 
 }
