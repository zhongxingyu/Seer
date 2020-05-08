 package platformer.core.model.level.impl;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import platformer.core.model.GameObject;
 import platformer.core.model.Level;
 import platformer.core.model.gameobject.impl.LevelTile;
 import platformer.core.model.systems.impl.physics.PhysicsSystem;
 import platformer.core.model.systems.impl.physics.bodies.Square;
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class DummyLevel implements Level {
 
 	private Collection<GameObject> gameObjects;
 
 	public DummyLevel() {
 		gameObjects = new LinkedList<GameObject>();		
 	}
 	
 	public void initialize(PhysicsSystem physicsSystem) {
 		for (int i = 0; i < 200; i++) {
 			Vector3 position = new Vector3(i * 10, 0, 0);
 			Rectangle bounds = new Rectangle(0, 0, 10, 10);
 			Body squareBody = physicsSystem.createBody(Square.class.getName(), position, bounds);
 			gameObjects.add(new LevelTile(position, bounds, squareBody));
 		}
 		
 		//Generate some platforms
		for (int i = 0; i < 20; i++) {
			Vector3 position = new Vector3(MathUtils.random(100, 2000), MathUtils.random(100, 1000), 0);
 			
			for(int j=1; j < 20; j++){
 				Rectangle bounds = new Rectangle(0, 0, 10, 10);
 				position.x = position.x + 10;
 				Body squareBody = physicsSystem.createBody(Square.class.getName(), position, bounds);
 				gameObjects.add(new LevelTile(position, bounds, squareBody));
 			}
 		}
 	}
 
 	@Override
 	public Collection<GameObject> getGameObjects() {
 		return gameObjects;
 	}
 
 	@Override
 	public Vector2 getGravity() {
 		return new Vector2(0, -9.8f); 
 	}
 
 }
