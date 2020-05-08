 package com.detourgames.raw.engineTest;
 
 import com.detourgames.raw.AnimationButton;
 import com.detourgames.raw.Camera;
 import com.detourgames.raw.ControllerNone;
 import com.detourgames.raw.PhysicsHUDElement;
 import com.detourgames.raw.SpriteSheet;
 import com.detourgames.raw.StateButton;
 
 public abstract class MenuHUDButton extends MenuHUDElement{
 	
 	public MenuHUDButton(Camera camera, SpriteSheet spriteSheet) {
 		super(new PhysicsHUDElement(camera), new AnimationButton(spriteSheet) , new StateButton(), new ControllerNone());
 		// TODO Auto-generated constructor stub
 	}
 	
 	public void resize(){
 		((PhysicsHUDElement)mPhysics).resize();
 	}
 	
 	public boolean isTouchInside(float x, float y){
 		return ((PhysicsHUDElement)mPhysics).isTouchInside(x, y);
 	}
 
 	
 }
