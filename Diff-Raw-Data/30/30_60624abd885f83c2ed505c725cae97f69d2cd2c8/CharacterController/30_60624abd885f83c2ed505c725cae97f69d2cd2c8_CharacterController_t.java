 package com.dat255_group3.controller;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.dat255_group3.model.Character;
 import com.dat255_group3.view.CharacterView;
 /**
  * The controller class for the character
  */
 public class CharacterController {
 
 	private WorldController worldController;
 	private Character character;
 	private CharacterView characterView;
 	/**
 	 * The constructor creates a character with the default friction 0
 	 * @param worldController 
 	 * @param camera , the camera used to display everything
 	 */
 	public CharacterController(WorldController worldController, OrthographicCamera camera){
 		this.worldController = worldController;
 		this.character = new Character(0.5);
 		this.characterView = new CharacterView(character, camera);
 	}
 
 	public Character getCharacter() {
 		return character;
 	}
 
 	public CharacterView getCharacterView() {
 		return characterView;
 	}
 	/**
 	 * Jumps the character body if its vertical velocity = 0
 	 */
 	public void tryToJump() {
 		if(this.worldController.getCharBody().getLinearVelocity().y == 0) {
 			jump();
 		}
 	}
 	
 	/**
 	 * Jumps the character body
 	 */
 	public void jump(){
 		float impulse = this.worldController.getCharBody().getMass()*6;
 		worldController.getCharBody().applyLinearImpulse(0.0f, impulse, 
 				worldController.getCharBody().getWorldCenter().x, worldController.getCharBody().getWorldCenter().y, true);
 	}
 }
 
 
