 package com.game.jetpack;
 
import android.util.Log;

 import com.bag.lib.math.Vector2;
 
 public class Joystick {
 	
 	public Vector2 basePosition;
 	public Vector2 stickPosition;
 	public Vector2 xyDistance;
 	public float size;
 	public float lastAngle;
 	
 	public Joystick(float baseX, float baseY, float jsize){
 		basePosition = new Vector2(baseX,baseY);
 		stickPosition = new Vector2(0,0);
 		xyDistance = new Vector2(0,0);
 		size = jsize;
 	}
 	
 	public void setBasePosition(Vector2 newPos){
 		basePosition.x = newPos.x;
 		basePosition.y = newPos.y;
 		resetStickPosition();
 	}
 	
 	public void resetStickPosition(){
 		stickPosition.x = basePosition.x;
 		stickPosition.y = basePosition.y;	
 	}
 	
 	// Returns true if the touch was in bounds and the stick position was 
 	// correctly changed
 	public boolean moveStick_X(Vector2 touch) {
     	if(touch.x <= basePosition.x + size && touch.x >= basePosition.x - size) {
     		stickPosition.x = touch.x;
     		return true;
     	} 
     	return false;
 	}
 	
 	// Returns true if the touch was in bounds and the stick position was 
 	// correctly changed
 	public boolean moveStick_Y(Vector2 touch) {
     	if(touch.y <= basePosition.y + size && touch.y >= basePosition.y - size) {
     		stickPosition.y = touch.y;
     		return true;
     	} 
     	return false;
 	}
 	
 	// Returns the distance between the stick and the base,
 	// In X and Y (in a vector) separately
 	public Vector2 getStickBaseDistance() {
 		
 		xyDistance.x = (stickPosition.x - basePosition.x);
 		xyDistance.y =  (stickPosition.y - basePosition.y);
 			
 		return xyDistance;
 	}
 	
 	public float getAngle() {
 		//Log.d("posy", "Stick diff Y: " + (stickPosition.y - basePosition.y));
 		//Log.d("posx", "Stick diff X: " + (stickPosition.x - basePosition.x));
 		float val = 0;
 		
 		if(stickPosition.y != basePosition.y && basePosition.x != stickPosition.x)
 			val = (float) Math.atan2(stickPosition.y - basePosition.y, stickPosition.x - basePosition.x) * (180/3.1416f);
 		else
 			val = lastAngle;
 		
 		lastAngle = val;
 		return val;
 	}
 }
