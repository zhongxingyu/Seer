 package com.secondhand.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.secondhand.model.entity.Enemy;
 import com.secondhand.model.entity.GameSettings;
 import com.secondhand.model.entity.Planet;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.IPhysicsEntity;
 import com.secondhand.model.physics.IPhysicsObject;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PlanetType;
 import com.secondhand.model.resource.SoundType;
 
 public class PlayerTest extends TestCase {
 	final float FORCE = 60;
 
 	class PhysicsEntity implements IPhysicsEntity {
 		
 		public Vector2 transform;
 
 		public int state;
 
 		public boolean test1Pass;
 		public boolean test2Pass;
 
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
 		public void applyImpulse(Vector2 impulse, Vector2 impulsePosition) {
 
 			if (state == 1) {
 				if (impulse.almostEquals(new Vector2(3 * FORCE * -1, 0))
 						&& impulsePosition.almostEquals(new Vector2(50, 0))) {
 					this.test1Pass = true;
 				}
 			}
 
 			if (state == 2) {
 				if (impulse.almostEquals(new Vector2(3 * FORCE, 0))
 						&& impulsePosition.almostEquals(new Vector2(-50, 0))) {
 					this.test2Pass = true;
 				}
 			}
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
 			transform = position;
 			
 		}
 
 		@Override
 		public void stopMovment() {
 		}
 
 		@Override
 		public boolean isStraightLine(IPhysicsObject entity,
 				IPhysicsObject enemy) {
 			return false;
 		}
 
 		@Override
 		public float getVelocity() {
 			return 0;
 		}
 	}
 
 	class Listener implements PropertyChangeListener {
 		String name;
 		Object type;
 
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			name = event.getPropertyName();
 			type = event.getNewValue();
 
 		}
 
 		public String getString() {
 			return name;
 		}
 
 		public Object getType() {
 			return type;
 		}
 	}
 
 	public void testConstructor() {
 
		GameSettings.getInstance().setMirroredMovement(false);
 		Player player = new Player(new Vector2(), 10, 3, // lives
 				100, // starting score
 				200); // maxsize
 
 		assertEquals(200, player.getMaxSize());
 		assertEquals(false, player.isMirroredMovement());
 		assertEquals(1, player.getSpeedMultiplier(), 0.0001f);
 		assertEquals(1, player.getScoreMultiplier(), 0.0001f);
 		assertEquals(3, player.getLives());
 		assertEquals(100, player.getScore());
 		assertEquals(false, player.canEatInedibles());
 	}
 
 	public void testReactToTouch() {
 
		GameSettings.getInstance().setMirroredMovement(false);
 		Player player = new Player(new Vector2(), 10, 3, // lives
 				100, // starting score
 				200); // maxsize
 		PhysicsEntity physics = new PhysicsEntity();
 		player.setPhysics(physics);
 		player.setSpeedMultiplier(3);
 
 		// with speed mutlipliers:
 
 		physics.state = 1;
 		physics.test1Pass = false;
 
 		player.reactToTouch(new Vector2(50, 0));
 
 		assertTrue(physics.test1Pass);
 
 		// now with mirrored movement
 		physics.state = 2;
 		physics.test2Pass = false;
 		player.setMirroredMovement(true);
 
 		player.reactToTouch(new Vector2(50, 0));
 
 		assertTrue(physics.test2Pass);
 
 		// now test mirrored movement gamesetting.
 		player.setMirroredMovement(false);
 
		GameSettings.getInstance().setMirroredMovement(true);
 
 		// with speed mutlipliers:
 
 		physics.state = 2;
 		physics.test2Pass = false;
 
 		player.reactToTouch(new Vector2(50, 0));
 
 		assertTrue(physics.test2Pass);
 
 		// now with mirrored movement
 		physics.state = 1;
 		physics.test1Pass = false;
 		player.setMirroredMovement(true);
 
 		player.reactToTouch(new Vector2(50, 0));
 
 		assertTrue(physics.test1Pass);
 
 	}
 
 	public void testWasEaten() {
 		class List implements PropertyChangeListener {
 						
 						public boolean playerKilled;
 						public String name;
 						public boolean moved;
 						public SoundType type;
 			
 						@Override
 						public void propertyChange(PropertyChangeEvent event) {
 							name = event.getPropertyName();
 							if(name.equals(Player.SOUND)) {
 								final SoundType newV = (SoundType)event.getNewValue();
 								
 								if(newV == SoundType.PLAYER_KILLED_SOUND)
 									playerKilled = true;
 							}
 							
 							if(name.equals(Player.RANDOMLY_REPOSITION_PLAYER)) {
 								moved = true;
 							}
 							
 							
 							
 						}
 						public String getString() {
 							return name;
 						}
 
 						public SoundType getType() {
 							return type;
 						}
 					}
 
 		Player player = new Player(new Vector2(), 10, 1, // lives
 				100, // starting score
 				200); // maxsize
 
 		List list = new List();
 		player.addListener(list);
 
 		Enemy enemy = new Enemy(new Vector2(10, 10), 100);
 
 		enemy.eatEntity(player);
 
 		assertEquals(0, player.getLives());
 		assertTrue(list.playerKilled);
 		assertTrue(list.moved);
 		assertTrue(player.lostAllLives());
 
 		player.gainLife();
 
 		assertEquals(1, player.getLives());
 		assertFalse(player.lostAllLives());
 		assertEquals(Player.INCREASE_LIFE, list.getString());
 	}
 
 	public void testEntityWasToBig() {
 
 		Listener listener = new Listener();
 		Player player = new Player(new Vector2(), 1, 3, // lives
 				1, // starting score
 				200); // maxsize
 		player.addListener(listener);
 		Planet planet = new Planet(new Vector2(50, 50), 100, PlanetType.DRUGS);
 
 		player.eatEntity(planet);
 
 		assertEquals(SoundType.OBSTACLE_COLLISION_SOUND, listener.getType());
 		assertEquals(Player.SOUND, listener.getString());
 
 	}
 
 	public void testEating() {
 
 		Listener listener = new Listener();
 
 		Player player = new Player(new Vector2(), 101, 3, // lives
 				1, // starting score
 				200); // maxsize
 
 		player.addListener(listener);
 		Planet planet = new Planet(new Vector2(50, 50), 100, PlanetType.DRUGS);
 		planet.setPhysics(new PhysicsEntity());
 		player.eatEntity(planet);
 		assertEquals(SoundType.GROW_SOUND, listener.getType());
 		assertEquals(Player.SOUND, listener.getString());
 	}
 
 	public void testRGB(){
 	Listener listener = new Listener();
 		
 		Player player = new Player(new Vector2(), 101, 3, // lives
 				1, // starting score
 				200); // maxsize
 		
 		player.addListener(listener);
 		float[] rgb = player.getRGB();
 		
 		assertEquals(rgb[0], 1f);
 		assertEquals(rgb[1], 1f);
 		assertEquals(rgb[2], 1f);
 		
 		player.setRGB(new float[]{0,0,0});
 		
 		rgb = player.getRGB();
 		
 		assertEquals(rgb[0], 0f);
 		assertEquals(rgb[1], 0f);
 		assertEquals(rgb[2], 0f);
 		
 		assertEquals(Player.COLOR, listener.getString());
 		rgb = (float[]) listener.getType();
 		
 		assertEquals(rgb[0], 0f);
 		assertEquals(rgb[1], 0f);
 		assertEquals(rgb[2], 0f);
 		
 		
 	}
 	
 	public void testMoveToNeddedposition(){
 		Player player = new Player(new Vector2(), 101, 3, // lives
 				1, // starting score
 				200); // maxsize
 		PhysicsEntity p = new PhysicsEntity();
 		player.setPhysics(p);
 		Vector2 v = new Vector2(25,20);
 		player.setNeedsToMovePosition(v);
 		player.moveToNeededPositionIfNecessary();
 		
 		assertEquals(25f, p.transform.x);
 		assertEquals(20f, p.transform.y);
 		
 	}
 }
