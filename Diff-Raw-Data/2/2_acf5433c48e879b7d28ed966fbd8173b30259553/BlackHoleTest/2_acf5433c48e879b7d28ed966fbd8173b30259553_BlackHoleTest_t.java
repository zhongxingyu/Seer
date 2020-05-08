 package com.secondhand.model;
 
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.entity.BlackHole;
 import com.secondhand.model.entity.Enemy;
 import com.secondhand.model.entity.Obstacle;
 import com.secondhand.model.entity.Planet;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.IPhysicsEntity;
 import com.secondhand.model.physics.IPhysicsObject;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PlanetType;
 import com.secondhand.model.resource.SoundType;
 
 public class BlackHoleTest extends TestCase {
 	
 	
 	public BlackHole getNewBlackHole(Vector2 position, final float radius, final int score) {
 		return new BlackHole(position,radius, score) {
 
 		};
 	}
 	
 	public void testConstructor() {
 		Vector2 position = new Vector2();
 		
 		BlackHole blackHole = getNewBlackHole(position, 10, 0);
 		
 		assertEquals(false, blackHole.canEatInedibles());
 	}	
 	
 	private boolean radiusPropChangeWasCorrect;
 	private boolean propChangeSent;
 
 	private class PropChangeTest implements PropertyChangeListener {
 
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			String name = event.getPropertyName();
 			if(name.equals(BlackHole.RADIUS)) {
 				final float old = (Float)event.getOldValue();
 				final float newV = (Float)event.getNewValue();
 				
 				if((int)old == 10 && (int)newV == 3) 
 					radiusPropChangeWasCorrect = true;
 			}
 			propChangeSent = true;
 		}
 	}
 	
 	public void testSetRadius() {
 
 		Vector2 position = new Vector2();
 		
 		BlackHole blackHole = getNewBlackHole(position, 10, 10);
 
 		// Cannot have a negative radius
 		try {
 			blackHole.setRadius(-1);
 			assertTrue(false);
 		} catch (AssertionError er) {
 			assertTrue(true);
 		}
 		
 		PropertyChangeListener listener = new PropChangeTest();
 		blackHole.addListener(listener);
 		this.propChangeSent = false;
 		radiusPropChangeWasCorrect = false;
 		
 		blackHole.setRadius(3);
 		assertEquals(3, (int)blackHole.getRadius());
 		assertTrue(radiusPropChangeWasCorrect);
 		assertTrue(propChangeSent);
 		
 		// make sure we can also remove listeners.
 		blackHole.removeListener(listener);
 		this.propChangeSent = false;
 		blackHole.setRadius(4);
 		assertFalse(propChangeSent);
 	}
 	
 	public void testScoreWorth() {
 	Vector2 position = new Vector2();
 		
 		BlackHole blackHole = getNewBlackHole(position, 10, 0);
 		
 		assertEquals(3 * 10, blackHole.getScoreValue());
 	}
 	
 	
 	private class ObstacleTestPhysicsEntity implements IPhysicsEntity {
 
 		@Override
 		public float getCenterX() {
 			return 0;
 		}
 
 		@Override
 		public float getCenterY() {
 			return 0;
 		}
 
 		@Override
 		public void deleteBody() {
 		}
 
 		@Override
 		public void applyImpulse(Vector2 impulsePosition, float maxSpeed) {
 		}
 
 		@Override
 		public void applyImpulse(Vector2 impulsePosition, Vector2 impulse) {
 		}
 
 		@Override
 		public void setLinearDamping(float f) {
 		}
 
 		@Override
 		public void detachSelf() {
 		}
 
 		@Override
 		public float computePolygonRadius(List<Vector2> polygon) {
 			return 10;
 		}
 
 		@Override
 		public void setTransform(Vector2 position) {
 		}
 
 		@Override
 		public void stopMovment() {
 		}
 
 		@Override
 		public boolean isStraightLine(IPhysicsObject entity,
 				IPhysicsObject enemy) {
 			return false;
 		}
 	}
 	
 	
 	public void testCanEat() {
 		float rad = 3.2f;
 		Vector2 vector = new Vector2();
 		Enemy enemy = new Enemy(vector, rad);
 
 		Player other = new Player(vector, rad - 1, 3, 0, 0);
 		assertTrue(enemy.canEat(other));
 
 		other = new Player(vector, rad + 1, 3 ,1, 0);
 		assertFalse(enemy.canEat(other));
 		
 		final ArrayList<Vector2> polygon = new ArrayList<Vector2>();
 		
 		final Obstacle obstacle = new Obstacle(new Vector2(), polygon);
 		obstacle.setPhysics(new ObstacleTestPhysicsEntity());
 		
 		assertFalse(enemy.canEat(obstacle));
 
 		enemy.setRadius(11);
 		// radius doesn't matter, as it is inedible 
 		assertFalse(enemy.canEat(obstacle));
 		
 		enemy.setCanEatInedibles(true);
 		enemy.setRadius(3);
 
 		// it's bigger
 		assertFalse(enemy.canEat(obstacle));
 
 		enemy.setRadius(11);
 		assertTrue(enemy.canEat(obstacle));
 		
 	}
 	
 	
 	
 	
 	
 	
 	private class EnemyTestPhysicsEntity implements IPhysicsEntity {
 
 		@Override
 		public float getCenterX() {
 			return 0;
 		}
 
 		@Override
 		public float getCenterY() {
 			return 0;
 		}
 
 		@Override
 		public void deleteBody() {
 		}
 
 		@Override
 		public void applyImpulse(Vector2 impulsePosition, float maxSpeed) {
 		}
 
 		@Override
 		public void applyImpulse(Vector2 impulsePosition, Vector2 impulse) {
 		}
 
 		@Override
 		public void setLinearDamping(float f) {
 		}
 
 		@Override
 		public void detachSelf() {
 			detachSelfCalled = true;
 		}
 
 		@Override
 		public float computePolygonRadius(List<Vector2> polygon) {
 			return 10;
 		}
 
 		@Override
 		public void setTransform(Vector2 position) {
 		}
 
 		@Override
 		public void stopMovment() {
 		}
 
 		@Override
 		public boolean isStraightLine(IPhysicsObject entity,
 				IPhysicsObject enemy) {
 			return false;
 		}
 	}
 	
 	private boolean detachSelfCalled;
 	
 	private boolean onGrowSoundPlayed;
 	private boolean scoreChangeProperlySent;
 	private int newScore;
 	private boolean onTooBigEntity;
 	
 	private class Prop2ChangeTest implements PropertyChangeListener {
 
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			String name = event.getPropertyName();
 			if(name.equals(Player.SOUND)) {
 				final SoundType newV = (SoundType)event.getNewValue();
 				
 				if(newV == SoundType.GROW_SOUND)
 					onGrowSoundPlayed = true;
 			} 
 			
 			if(name.equals(Player.INCREASE_SCORE)) {
 				final int newV = (Integer)event.getNewValue();
 				final int old = (Integer)event.getOldValue();
 				MyDebug.d("newScore: " + newScore);
 				if(old == 10 &&  newV == newScore)
 					scoreChangeProperlySent = true;
 			} 
 			
 			if(name.equals(Player.SOUND)) {
 				final SoundType newV = (SoundType)event.getNewValue();
 				
 				if(newV == SoundType.OBSTACLE_COLLISION_SOUND)
 					onTooBigEntity = true;
 			}
 		}
 	}
 	
 	public void testEatEntity() {	
 
 		Player other = new Player(new Vector2(), 10, 3, 10, 0);
 		other.setScoreMultiplier(2);
 		other.addListener(new Prop2ChangeTest());
 		
 		Enemy enemy = new Enemy(new Vector2(), 9);
 		enemy.setPhysics(new EnemyTestPhysicsEntity());
 
 		this.detachSelfCalled = false;
 		this.onGrowSoundPlayed = false;
 		this.scoreChangeProperlySent = false;
 		
 		newScore = 10 + 2*(enemy.getScoreValue());
 		
 		other.eatEntity(enemy);
 		
 		assertEquals(newScore, other.getScore());
 		assertTrue(this.detachSelfCalled);
 		assertTrue(this.onGrowSoundPlayed);
 		assertTrue(this.scoreChangeProperlySent);
 		
 		//Radius should not change
 		assertEquals(10,other.getRadius(), 0.0001f);
 		
		assertEquals(10 + enemy.getRadius() * Player.GROWTH_FACTOR, other.getRadius() + other.getIncreaseSize(), 0.0001f);
		
 		// now make a planet bigger than the player.
 		this.onTooBigEntity = false;
 		final Planet planet = new Planet(new Vector2(), 300, PlanetType.BLOOD);
 		planet.setPhysics(new EnemyTestPhysicsEntity());
 		other.eatEntity(planet);
 		assertTrue(this.onTooBigEntity);
 		
 		// now create a bigger enemy and ensure that it eats the player instead.
 		Enemy enemyBigger = new Enemy(new Vector2(), 300);
 		enemyBigger.setPhysics(new EnemyTestPhysicsEntity());
 
 		
 		other.eatEntity(enemyBigger);
 		// if the enemy gained enough score, then it worked. 
 		assertEquals(other.getScoreValue(), enemyBigger.getScore());
 	}
 	
 
 }
