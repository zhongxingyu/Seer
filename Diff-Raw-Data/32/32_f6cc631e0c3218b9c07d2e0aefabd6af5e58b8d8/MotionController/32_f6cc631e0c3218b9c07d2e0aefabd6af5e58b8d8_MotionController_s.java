package gameCharacter;
 
 import app.RPGame;
 
 /**
  * Controls the movement of the game character.
  * Can be scripted, AI-based or human player controlled.
  * 
  * @author jameshong
  *
  */
 public abstract class MotionController {
 
 	RPGame game;
 	GameCharacter character;
 	
 	public MotionController(RPGame game, GameCharacter character){
 		this.game = game;
 		this.character = character;
 	}
 	
 	public abstract void update(long elapsedTime);
 	
 }
