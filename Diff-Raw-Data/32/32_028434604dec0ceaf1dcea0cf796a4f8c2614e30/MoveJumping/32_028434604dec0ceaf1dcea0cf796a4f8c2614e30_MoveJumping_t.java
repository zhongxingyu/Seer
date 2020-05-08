 package com.spotlightcoding.components;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.spotlightcoding.Component;
 import com.spotlightcoding.Entity;
 
 public class MoveJumping extends Component{
 	//------------------------------------------------CONSTANTS
 	private final int JUMP_HEIGHT = 96;
 	//------------------------------------------------PROPERTIES
 	private int robState;
 	private float prevHeight; 
 	private float topHeight;
 	private float speed; 
 	//------------------------------------------------CONSTRUCTOR
 	public MoveJumping(String myId){
 		this.id = myId;
 		this.speed = 0.4f;
 	}
 	//------------------------------------------------GETS/SETS
 	//------------------------------------------------PUBLIC METHODS
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		
 		
 		Input input = gc.getInput();
 		Vector2f position = owner.getPosition();
 		robState = owner.getState();
 					
 		if((input.isKeyDown(Input.KEY_W) && (owner.getState() == Entity.NORMAL))){
 			prevHeight = position.y;
 			topHeight = position.y - JUMP_HEIGHT;
 			owner.setState(Entity.JUMPING);
 		}
 			
 		if(owner.getState() == Entity.JUMPING){
 
 			if(position.y >= topHeight) {
 				position.y -= speed * delta;
				
 				
 			}else if(position.y <= topHeight){
 				owner.setState(Entity.FALLING);
				
 			}
 			
 		}
 			
 		
 		
 		
 		
 	}
 	//------------------------------------------------PRIVATE METHODS
 }
