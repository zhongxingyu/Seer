 package edu.chl.codenameg.model.entity;
 
 import static org.junit.Assert.assertTrue;
 
import java.awt.Point;

 import org.junit.Test;
 
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Vector2D;
 
 public class GoalBlockTest {
 
 	@Test
 	public void testSetPosition() {
 		GoalBlock block = new GoalBlock();
		Point point = new Point(10,20);
 		block.setPosition(point);
 		assertTrue(block.getPosition().equals(point));
 	}
 	@Test
 	public void testSetHitbox(){
 		GoalBlock block = new GoalBlock();
 		Hitbox hb = new Hitbox(1,1);
 		block.setHitbox(hb);
 		assertTrue(block.getHitbox().equals(hb));
 	}
 	@Test
 	public void testGetVector2D(){
 		GoalBlock block = new GoalBlock();
 		Vector2D v2d= new Vector2D(0,0);
 		assertTrue(block.getVector2D().equals(v2d));
 	}
 	@Test
 	public void testIsColliding(){
 		GoalBlock block1 = new GoalBlock();
 		GoalBlock block2 = new GoalBlock();
		Point p = new Point(10,20);
 		block1.setPosition(p);
 		block2.setPosition(p);
 		block1.collide(block2);
 		block2.collide(block1);
 		assertTrue(block1.isColliding() && block2.isColliding());
 	}
 	
 	@Test
 	public void testPlayerWin(){
		Point pos = new Point(0,0);
 		Hitbox hb = new Hitbox(2,2);
 		PlayerCharacter pc = new PlayerCharacter(pos);
 		GoalBlock gb = new GoalBlock(hb, pos);
 		pc.collide(gb);
 		gb.collide(pc);
 		assertTrue(pc.hasWonGame());
 	}
 
 }
