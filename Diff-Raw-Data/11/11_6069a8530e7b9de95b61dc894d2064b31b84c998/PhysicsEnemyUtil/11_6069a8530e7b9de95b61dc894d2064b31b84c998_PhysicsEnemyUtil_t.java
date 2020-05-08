 package com.secondhand.view.physics;
 
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.RayCastCallback;
 import com.secondhand.model.Enemy;
 import com.secondhand.model.Entity;
 
 // contains the two raycast classes that enemy uses.
 public class PhysicsEnemyUtil {
 	private boolean straightLine;
 	private final PhysicsWorld physics;
 
 	public PhysicsEnemyUtil(final PhysicsWorld physics) {
 		straightLine = true;
 		this.physics = physics;
 	}
 
 	//true if there is a line where there are no uneatable entities
 	public boolean straightLine(final Entity entity,final Enemy enemy) {
 		straightLine = true;
 		physics.rayCast(new RayCastCallback() {
 
 			@Override
 			public float reportRayFixture(final Fixture fixture,
 					final Vector2 point, final Vector2 normal,
 					final float fraction) {
 
 				if (((Entity) fixture.getBody().getUserData()) == entity) {
 					return 1;
 
 				} else if (enemy.canEat((Entity) fixture.getBody()
 						.getUserData())) {
 					return 1;
 				}
 
 				straightLine = false;
 
 				return 0;
 			}
		}, new Vector2(enemy.getCenterX(),enemy.getCenterY()) , new Vector2(enemy.getCenterX(),enemy.getCenterY()));
 
 		return straightLine;
 	}
 
 	// need to raycast in the direction the enemy is moving
 	// the ray shouldn't be to long: radius + 5-10?
 	// so how to decide the second point for the ray?
 
 	// clarify: a point 5-10 length-units in front of where the enemy is moving.
 	public Vector2 dangerClose(final Enemy enemy) {
		
		// hope this works, otherwise Andreas will have to fix it. 
		final Vector2 v = new Vector2(enemy.getCenterX(), enemy.getCenterY());   //enemy.getPhysics().getBody().getWorldCenter();
		
		
 		//Vector2 v2 = enemy.getBody().getLinearVelocity();
 		physics.rayCast(new RayCastCallback(){
 
 			@Override
 			public float reportRayFixture(final Fixture fixture,final  Vector2 point,
 					final Vector2 normal, final float fraction) {
 				if(fixture.getBody().getUserData() != null){
 					if(enemy.canEat((Entity)fixture.getBody().getUserData())){
 						return 0;
 					}else{
 						enemy.retreat((Entity)fixture.getBody().getUserData());
 					}
 				}
 				return 0;
 			}
 
 		},v, null);// <--- the second point of the ray should go here
 		return null;
 
 	}
 }
