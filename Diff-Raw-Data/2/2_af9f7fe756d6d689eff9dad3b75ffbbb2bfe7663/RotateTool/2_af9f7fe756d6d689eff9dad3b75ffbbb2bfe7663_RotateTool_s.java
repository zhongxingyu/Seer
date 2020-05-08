 package com.funprog.tabletennis;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * A rotation tool that has the ability to rotate the image
  * to match where a user has touched it.
  */
 public class RotateTool extends ControlTool {
 	/**
 	 * Constructs the tool with the image at the position
 	 * @param image The image of the tool
 	 * @param position The position of the tool including
 	 * width and height
 	 */
 	public RotateTool(Texture image, Rectangle position) {
 		super(image, position);
 	}
 	
 	/**
	 * Updates the too based on user input by rotating the image
 	 * so that its top is always facing the touch
 	 * @param x The x value of the touch
 	 * @param y The y value of the touch
 	 */
 	public void updateTouch(float x, float y) {
 		// Calculate the vector from the sprite's center to the touch point
 		Vector2 touch = new Vector2(x, y);
 		touch = touch.sub(sprite.getX() + sprite.getOriginX(),
 				sprite.getY() + sprite.getOriginY());
 		
 		// Normalize the vector
 		touch = touch.scl(1 / touch.len());
 		
 		// Dot the vector with the up vector to begin to calculate the angle
 		// of the image to become
 		float dot = touch.dot(new Vector2(0, 1));
 		
 		// Get the angle using the formula cos(theta) = a dot b where
 		// both both a and b are unit vectors
 		double angle = Math.acos(dot);
 		
 		// Since arccos returns between 0 and pi, figure out to go clockwise
 		// or counter-clockwise based on the x value of the input
 		if (touch.x < 0) {
 			// Go counter-clockwise because negative x is left of top
 			sprite.setRotation(MathUtils.radiansToDegrees * (float) angle);
 		} else {
 			// Go clockwise because positive x is right of top
 			sprite.setRotation(-MathUtils.radiansToDegrees * (float) angle);
 		}
 	}
 	
 	/**
 	 * Rotates the tool to the desired angle
 	 * @param angle The angle to rotate to in radians
 	 */
 	public void setRotation(float angle) {
 		sprite.setRotation(MathUtils.radiansToDegrees * angle);
 	}
 	
 	/**
 	 * Returns the current angle of the tool
 	 * @return The angle in radians
 	 */
 	public float getRotation() {
 		return sprite.getRotation() * MathUtils.degreesToRadians;
 	}
 }
