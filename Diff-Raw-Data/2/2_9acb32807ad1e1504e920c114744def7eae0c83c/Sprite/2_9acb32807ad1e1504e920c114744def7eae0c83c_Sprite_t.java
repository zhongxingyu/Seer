 package com.detourgames.raw;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public abstract class Sprite implements IFocusable{
 
 	PhysicsComponent mPhysics;
 	StateComponent mState;
 	AnimationComponent mAnimation;
 	ControllerComponent mController = new ControllerComponent();
 	
 	float mCameraOffsetX = 0;
 	float mCameraOffsetY = 0;
 
 	public Sprite(PhysicsComponent pc, AnimationComponent ac, StateComponent sc) {
 		
 		// all sprites will create their own animations in their own constructors.
 		// that means each sprite subclass must have a SpriteSheet param in their constructor.
 		mPhysics = pc;
 		mAnimation = ac;
 		mState = sc;
 		mPhysics.setParentSprite(this);//TODO see if this works or else I need to do this in each subclass
 
 	}
 
 	public void destroy() {
 		// TODO
 	}
 
 	public void draw(SpriteBatch sb, long nanoTime) {
 		//sb.draw(mAnimation.getFrame(nanoTime), mPhysics.getX()+mAnimation.getOffsetX(), mPhysics.getY()+mAnimation.getOffsetY(), mAnimation.getWidth(), mAnimation.getHeight());
		sb.draw(mAnimation.getFrame(nanoTime), mPhysics.getX()+mAnimation.getOffsetX(), mPhysics.getY()+mAnimation.getOffsetY(), mAnimation.getWidth()/2f, mAnimation.getHeight()/2f, mAnimation.getWidth(), mAnimation.getHeight(), 1, 1, (float)Math.toDegrees(mPhysics.mBody.getAngle()));
 	}
 
 	public void update(float deltaTime) {
 		
 		mState.update(mPhysics);
 		mAnimation.update(mState);
 		mController.update(mState, mPhysics);
 		mPhysics.update();
 		
 	}
 
 	public float getX() {
 		return mPhysics.getX();
 	}
 
 	public float getY() {
 		return mPhysics.getY();
 	}
 	
 	@Override
 	public float getCameraOffsetX() {
 		return mCameraOffsetX;
 	}
 
 	@Override
 	public float getCameraOffsetY() {
 		return mCameraOffsetY;
 	}
 
 	/*
 	 * public float getOriginX(){ return mPhysics.getX() + mDrawWidth/2f; }
 	 * 
 	 * public float getOriginY(){ return mPhysics.getY() + mDrawHeight/2f; }
 	 * 
 	 * public Vec2 getProjectileSpawnPoint(){//TODO move to an interface like
 	 * IProjectileParent or something
 	 * mProjectileSpawnPoint.set(mPhysics.getX()+3, mPhysics.getY());//TODO
 	 * change arguments to mPhysics.getProjectileSpawn return
 	 * mProjectileSpawnPoint; }
 	 */
 	
 }
