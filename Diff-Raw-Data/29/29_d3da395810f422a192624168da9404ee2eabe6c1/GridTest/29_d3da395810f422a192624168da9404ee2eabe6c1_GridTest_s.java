 package edu.wheaton.simulator.test.datastructure;
 
 import static org.junit.Assert.fail;
 
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.AbstractGUIGridObserver;
 import edu.wheaton.simulator.datastructure.AgentAppearance;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Prototype;
 
 public class GridTest {
 
 	private Grid grid;
 
 	@Before
 	public void setUp() {
 		grid = new Grid(20, 30);
 	}
 
 	@Test
 	public void sizeTest() {
 		Assert.assertTrue(grid.getWidth() == 20);
 		Assert.assertTrue(grid.getHeight() == 30);
 	}
 
 	@Test
 	public void positionTest() {
 		// in bounds
 		Assert.assertTrue(grid.emptyPos(0, 0));
 		Assert.assertTrue(grid.emptyPos(10, 10));
 		Assert.assertTrue(grid.emptyPos(19, 29));
 		
 		// out of bounds
 		Assert.assertFalse(grid.emptyPos(-10, -7));
 		Assert.assertFalse(grid.emptyPos(98, 100));
 	}
 
 	@Test
 	public void addAgentTest() {
 		Prototype p = new Prototype("Test");
 		
 		// In bounds
 		Assert.assertTrue(grid.addAgent(p.createAgent(grid), 5, 5)); // ID = 0
 		Assert.assertTrue(grid.addAgent(p.createAgent(grid), 15, 20)); // ID = 1
 		
 		// Out of bounds
 		Assert.assertFalse(grid.addAgent(p.createAgent(grid), -5, -5)); // ID = 2
 		Assert.assertFalse(grid.addAgent(p.createAgent(grid), 30, 30)); // ID = 3
 		
 		Assert.assertEquals(0, IDOfAgentAt(5, 5));
 		Assert.assertEquals(1, IDOfAgentAt(15, 20));
 	}
 
 	/**
 	 * Helper method that provides the integer ID of the Agent at the given x/y
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private int IDOfAgentAt(int x, int y) {
 		return grid.getAgent(x, y).getID().getInt();
 	}
 	
 	@Test
 	public void removeTest() {
 		Prototype p = new Prototype("Test");
 		grid.addAgent(p.createAgent(grid), 5, 5);
 		grid.removeAgent(5, 5);
 		Assert.assertNull(grid.getAgent(5, 5));
 	}
 	
 	@Test
 	public void observerTest() {
 		grid = new Grid(20, 30);
 		
 		Prototype p = new Prototype("Test");
 		
 		// In bounds
 		Assert.assertTrue(grid.addAgent(p.createAgent(grid), 5, 5)); // ID = 0
 		Assert.assertTrue(grid.addAgent(p.createAgent(grid), 15, 20)); // ID = 1
 		
 		// Out of bounds
 		Assert.assertFalse(grid.addAgent(p.createAgent(grid), -5, -5)); // ID = 2
 		Assert.assertFalse(grid.addAgent(p.createAgent(grid), 30, 30)); // ID = 3
 		
 		Assert.assertEquals(5, IDOfAgentAt(5, 5));
 		Assert.assertEquals(6, IDOfAgentAt(15, 20));
 		
 		grid.addObserver(new AbstractGUIGridObserver() {
 
 			@Override
 			public void update(Set<AgentAppearance> agents) {
 				Assert.assertEquals(agents.size(), 2);
 			}
 			
 		});
 		try {
			grid.notifyObservers(false);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 			fail("Problem with notifying observers.");
 		}
 	}
 }
