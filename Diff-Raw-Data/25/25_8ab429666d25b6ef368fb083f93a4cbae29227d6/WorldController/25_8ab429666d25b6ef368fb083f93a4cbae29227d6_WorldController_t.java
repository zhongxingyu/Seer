 package com.me.mygdxgame;
 
 import java.util.Map; 
 import java.util.HashMap;
 
 import org.lwjgl.input.Mouse;
  
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.input.*;
 import com.badlogic.gdx.math.Vector2;
 
 @SuppressWarnings("unused")
 public class WorldController{
 		
 	private World world; 
 	private Ball ball;
 	private WorldRenderer wRend;
 	private direcLogic directionLogic; 
 	private int leftButtonCount = 0;
 	private int rightButtonCount = 0;
 	private float speedChange = 10000.0f;  
 	
 	enum Buttons {
 		LEFT, RIGHT
 	}
 	//hashmap of buttons
 	static Map<Buttons, Boolean> buttons = new HashMap<WorldController.Buttons, Boolean>(); 
 	static {
 		buttons.put(Buttons.LEFT,  false); 
 		buttons.put(Buttons.RIGHT, false);
 	};
 	
 	public WorldController(World world) {
 		this.world = world; 
 		this.ball = world.getBall();
 	}
 	
 	//enable left click (this is for processInput, left click is always enabled)
 	public void leftKeyPressed() { 
 		buttons.get(buttons.put(Buttons.LEFT, true));
 	}
 	//disable
 	public void leftKeyReleased() {
 		buttons.get(buttons.put(Buttons.LEFT, false));
 		leftButtonCount = 1;
 	}
 	
 	// enable right click
 	public void rightKeyPressed() { 
 		buttons.get(buttons.put(Buttons.RIGHT, true));
 	}
 	//disable
 	public void rightKeyReleased() {
 		buttons.get(buttons.put(Buttons.RIGHT, false));
 		rightButtonCount = 1;
 	}
 	
 	//get input and update ball gets power and dir from direcLogic class
 	public void update(float delta, float power, Vector2 dir) {
 		processInput(delta, power, dir); 
 		if (ball.inHole == true){
 			ball.getVelocity().x = 0;
 			ball.getVelocity().y = 0;
		}		
 		ball.update(delta);
 	}
 	
 	//updates the speed and position of the ball when the mouse is clicked
 	private void processInput(float delta, float power, Vector2 dir) {
 				
 		System.out.println(power);
 		System.out.println(dir);
 		if(leftButtonCount == 0 && rightButtonCount == 0){
 			ball.getVelocity().x = 0; 
 			ball.getVelocity().y = 0;
 		}
 		if(leftButtonCount == 1){
 			if(speedChange != 0.0){
 				speedChange -=50f;
 				if(speedChange < 0) speedChange = 0;
				if (ball.bounceX) ball.getVelocity().x = (-(dir.x) * (power*5)) * delta;
				else ball.getVelocity().x = ((dir.x) * (power*5)) * delta;
				if (ball.bounceY) ball.getVelocity().y = (-(dir.y) * (power*5)) * delta;
				else ball.getVelocity().y = ((dir.y) * (power*5)) * delta;
				
			} else { 
 				    //reset speed and button count
					ball.bounceX = false;
					ball.bounceY = false;
 					speedChange = 10000; 
 					leftButtonCount = 0;
			}
 		}
 //		if(rightButtonCount == 1){
 //			if(speedChange > 0.0){
 //				speedChange -=50f;
 //				if(speedChange < 0) speedChange = 0;				
 //
 //				if(!ball.bounceY) ball.getVelocity().y = (speedChange * delta);
 //				else ball.getVelocity().y = (-speedChange * delta);
 //			}
 //			else {
 //				speedChange = 10000; 
 //				rightButtonCount = 0;
 //			}
 //		}
 	}
 }
