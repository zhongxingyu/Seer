 package edu.chl.codenameg.model;
 
 import static org.junit.Assert.*;
 
 import java.awt.Point;
 
 import org.junit.Test;
 
import edu.chl.codenameg.model.entity.Block;

 public class WorldTest {
 
 	@Test
 	public void testSetMap() {
 		World world = new World();
 		Block block = new Block();
 		world.add(block);
		assertTrue(world.getEntities().size() == 1);
 	}
 	
 	@Test
 	public void testEntityCollide() {
 		World world = new World();
 		Block block = new Block();
 		Block block2 = new Block();
 		block.setPosition(new Point(10,10));
 		block2.setPosition(new Point(10,10));
 		world.add(block);
 		world.add(block2);
 		world.update();
 		assertTrue(block.isColliding() && block2.isColliding());
 	}	
 
 }
