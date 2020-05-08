 package com.detourgames.raw;
 
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class Projectile extends Sprite implements IReusable{
 	
 	public boolean isActive = false;
 	public boolean isReadyForSpawn = false;
 	
 	public static final int TYPE_RAW = 1;
 	public static final int TYPE_ENEMY=2;
 	
 	public static final float WIDTH_RAW = 0.4f;
 	public static final float HEIGHT_RAW = 0.4f;
 	
	public static final float VELOCITY_RAW = 1.0f;
 	
 	public static final Vector2[] VERTS_RAW = new Vector2[]{new Vector2(-0.2f,-0.2f), new Vector2(0.2f,-0.2f), new Vector2(0.2f,0.2f), new Vector2(-0.2f,0.2f)};
 	
 	
 	public Projectile(SpriteSheet spriteSheet){
 		
 		super(new PhysicsProjectile(), new AnimationProjectile(spriteSheet,WIDTH_RAW,HEIGHT_RAW), new StateProjectile());
 		
 	}
 	
 	public void create(World world, float x, float y){
 		//TODO
 		mPhysics.create(world, x, y, VERTS_RAW, true, FixtureType.HERO_PROJECTILE);
 		mPhysics.mBody.setBullet(true);
 	}
 	
 	public void prepareForSpawn(int type, Sprite parent, Vector2 destination){
 		mState.setInitialState();
 		((PhysicsProjectile)mPhysics).setProjectileProperties(type, parent, destination);
 		//TODO finish
 		
 		isReadyForSpawn = true;
 	}
 
 	public void spawn(World world) {
 		if(isReadyForSpawn){
 			isActive = true;
 			isReadyForSpawn = false;
 			mPhysics.mBody.setActive(true);//this is the call that sets them going.
 		}else{
 			System.out.println("call prepareForSpawn() before spawn()");
 		}
 	}
 
 	public void recycle() {
 		isActive = false;
 		isReadyForSpawn = false;
 		mPhysics.mBody.setTransform(-1, -1, 0);
 		mPhysics.mBody.setLinearVelocity(0, 0);
 		mPhysics.mBody.setActive(false);
 		//TODO set to a neutral state that won't interfere with the rest of the game.
 	}
 	
 }
