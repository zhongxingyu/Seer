 package com.secondhand.physics;
 
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.util.MathUtils;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class CustomPhysicsConnector extends PhysicsConnector {
 
 	private final boolean isCircle;
 	
	public CustomPhysicsConnector(final IShape shape, final boolean circle, final Body body, final boolean updatePosition, final boolean updateRotation) {
 		super(shape, body, updatePosition, updateRotation );
 		
 		this.isCircle = circle;
 	}
 	
 	
 	@Override
 	public void onUpdate(final float pSecondsElapsed) {
 		final IShape shape = this.mShape;
 		final Body body = this.mBody;
 
 		if(this.mUpdatePosition) {
 			final Vector2 position = body.getPosition();
 			final float pixelToMeterRatio = this.mPixelToMeterRatio;
 			
 			float x = position.x * pixelToMeterRatio;
 			float y = position.y * pixelToMeterRatio;
 			
 			// the Circle class is positioned by center.
 			if(!isCircle) {
 				x -=  this.mShapeHalfBaseWidth;
 				y -=  this.mShapeHalfBaseHeight;
 				
 			}
 			
 			shape.setPosition(x , y);
 		}
 
 		if(this.mUpdateRotation) {
 			final float angle = body.getAngle();
 			shape.setRotation(MathUtils.radToDeg(angle));
 		}
 	}
 	
 }
