 package pong.model;
 
 import org.jbox2d.common.Vec2;
 
 import pong.control.MouseInput;
 
 public class MoveableItem extends GameItem{
 
 	public MoveableItem(float xPos, float yPos, float zPos, Type type) {
 		super(xPos, yPos, zPos, type);
 	}
 	
 	//move item according to (mouse) input
 	public void moveItem(float inputX, float inputY){
 		//map x,y coordinates in window to numbers working well with linearVelocity function (using Const.MOUSE_SENSE)
 		float normalizedX = mapRange(0 , Const.SCREEN_WIDTH , -Const.MOUSE_SENSE , Const.MOUSE_SENSE ,  inputX );
 		float normalizedY = mapRange(0 , -Const.SCREEN_HEIGHT , -Const.MOUSE_SENSE , Const.MOUSE_SENSE ,inputY );
 		
 		//implement a deadzone with a radious of MOUSE_DEADZONE
 		if(Math.abs(normalizedX) <= Const.MOUSE_DEADZONE)
 			normalizedX=0;
 		if(Math.abs(normalizedY) <= Const.MOUSE_DEADZONE)
 			normalizedY=0;
 		//apply force to object
		if(body != null){
			body.setLinearVelocity(new Vec2( normalizedX , -normalizedY));
		}
		
 	}
 	
 	//maps a value in one given range to another given range
 	// @param [a1,a2] = range 1 , [b1,b2] = range 2, s = value in range 1 to be mapped to range 2
 	private float mapRange(float a1, float a2, float b1, float b2, float s){
 		return b1 + ((s - a1)*(b2 - b1))/(a2 - a1);
 	}
 }
