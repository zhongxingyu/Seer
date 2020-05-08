 package rsmg.model.object.unit;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.LinkedList;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import rsmg.model.object.bullet.Bullet;
 import rsmg.model.object.item.HealthPack;
 import rsmg.model.object.item.Item;
 
 
 /**
  * Tests the character class.
  * @author Daniel Jonsson
  *
  */
 public class TestPCharacter {
 
 	PCharacter character;
 	
 	@BeforeClass
 	public static void beforeClass() {
 	}
 
 	@AfterClass
 	public static void afterClass() {
 	}
 
 	@Before
 	public void before() {
 		character = new PCharacter(0, 0, new LinkedList<Bullet>());
 	}
 
 	@After
 	public void after() {
 	}
 
 	@Test
 	public void testJump() {
 		character.jump();
 		assertTrue(character.getVelocityY() < 0);
 	}
 
	//@Test
 	public void testMoveLeft() {
 		character.moveLeft();
 		assertTrue(character.getVelocityX() < 0);
 	}
 
	//@Test
 	public void testMoveRight() {
 		character.moveRight();
 		assertTrue(character.getVelocityX() > 0);
 	}
 
 	@Test
 	public void testHasCollidedWith() {
 		
 		PCharacter character = new PCharacter(0, 0, null);
 		PCharacter character2 = new PCharacter(5, 5, null);
 		assertTrue(character.hasCollidedWith(character2));
 		assertTrue(character2.hasCollidedWith(character));
 		
 		// Rotate both right
 		character.setX(5);
 		character2.setX(0);
 		assertTrue(character.hasCollidedWith(character2));
 		assertTrue(character2.hasCollidedWith(character));
 		
 		// Test with items
 		Item item = new HealthPack(0,0);
 		character.setX(0);
 		assertTrue(character.hasCollidedWith(item));
 		item.setY(character.getHeight());
 		assertFalse(character.hasCollidedWith(item));
 		item.setY(character.getHeight()-1);
 		assertTrue(character.hasCollidedWith(item));
 	}
 }
