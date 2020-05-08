 package com.razh.tiling;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class Actor3D extends Actor {
 	private float mZ;
 	private float mDepth;
 
 	public float getZ() {
 		return mZ;
 	}
 
 	public void setZ(float z) {
 		mZ = z;
 	}
 
 	public Vector2 getPosition2D() {
 		return new Vector2(getX(), getY());
 	}
 
 	public Vector3 getPosition() {
 		return new Vector3(getX(), getY(), getZ());
 	}
 
 	public void setPosition(Vector3 position) {
 		super.setPosition(position.x, position.y);
 		setZ(position.z);
 	}
 
 	@Override
 	public void setPosition(float x, float y) {
 		super.setPosition(x, y);
 	}
 
 	public void setPosition(float x, float y, float z) {
 		super.setPosition(x, y);
 		setZ(z);
 	}
 
 	public void translate(float x, float y, float z) {
 		super.translate(x, y);
 		setZ(getZ() + z);
 	}
 
 	public float getDepth() {
 		return mDepth;
 	}
 
 	public void setDepth(float depth) {
 		mDepth = depth;
 	}
 
 	/**
 	 * @param actor
 	 * @return Normalized vector from this to actor.
 	 */
 	public Vector3 vectorTo(Actor3D actor) {
 		return actor.getPosition()
		     	    .cpy()
 		            .sub(getPosition())
 		            .nor();
 	}
 }
