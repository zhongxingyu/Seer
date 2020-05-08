 package com.secondhand.model;
 
 import junit.framework.TestCase;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.secondhand.model.entity.Enemy;
 import com.secondhand.model.entity.GameWorld;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.view.physics.MyPhysicsWorld;
 
 public class EnemyTest extends TestCase {
 	
 
 	public void testConstructor() {
		Vector2 vector1 = new Vector2(2f, 4f);
 		float rad = 3.2f;
 
 		Enemy enemy = new Enemy(vector1, rad);
		Vector2 enemyPosition = enemy.getInitialPosition();
 		assertEquals(rad, enemy.getRadius());
		assertEquals(vector1.x, enemyPosition.x);
		assertEquals(vector1.y, enemyPosition.y);
 	}
 /*
 	public void testIsBiggerThan() {
 		com.secondhand.model.physics.Vector2 vector1 = new com.secondhand.model.physics.Vector2(2f, 4f);
 		com.secondhand.model.physics.Vector2 vector2 = new com.secondhand.model.physics.Vector2(2f, 4f);
 		float rad = 3.2f;
 
 		Enemy enemy = new Enemy(vector2, rad, gW);
 
 		Player other = new Player(vector2, rad - 1, gW);
 		assertTrue(enemy.canEat(other));
 		
 		other = new Player(vector2, rad+1, gW);
 		assertFalse(enemy.canEat(other));
 	}*/
 
 }
