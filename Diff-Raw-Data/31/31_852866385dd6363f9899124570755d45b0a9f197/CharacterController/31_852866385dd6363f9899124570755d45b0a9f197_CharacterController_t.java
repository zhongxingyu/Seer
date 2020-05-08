 package controller;
 
 
 import model.Character;
 import model.Item;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import view.CharacterView;
 
 
 public class CharacterController {
 	private final InGameController inGameController;
 	private Character character;
 	private final CharacterView characterView;
 	
 	public CharacterController(final InGameController inGameController) throws SlickException {
 		this.inGameController = inGameController;
 		if (this.inGameController.getBlockMapController().getBlockMapView().getStartingPos() == null) {
 			this.character = new Character(400, 100);
 		} else {
 			this.character = new Character(this.inGameController.getBlockMapController().getBlockMapView().getStartingPos().getPosX(),
 					this.inGameController.getBlockMapController().getBlockMapView().getStartingPos().getPosY());
 		}
 		
 		this.characterView = new CharacterView(character);
 	}
 	
 	public Character getCharacter() {
 		return character;
 	}
 
 	public CharacterView getCharacterView() {
 		return characterView;
 	}
 
 	/**
 	 * Checks which key is pressed down in terms of the character's movement 
 	 */
 	public void keyPressedUpdate(GameContainer gc) {
 		final Input input = gc.getInput();		
 		
 		if(input.isKeyDown(Input.KEY_RIGHT)) {
 			inGameController.getWorldController().moveBodyRight();
 			if(characterView.isBlinking()) {
 				characterView.animateBlinkingRight();
 			} else {
 				characterView.animateWalkingRight();
 			}
 		}else if(input.isKeyDown(Input.KEY_LEFT)) {
 			inGameController.getWorldController().moveBodyLeft();
 			if(characterView.isBlinking()) {
 				characterView.animateBlinkingLeft();
 			} else {
 				characterView.animateWalkingLeft();
 			}
 		} else { //if no key is pressed
 			if(characterView.isWalkingLeft() || characterView.isStandingLeft()) {
 				characterView.animateStandingLeft();
 			} else {
 				characterView.animateStandingRight();
 			}
 		} 
 	}
 	
 	/**
 	 * If on ground, then jump.
 	 */
 	public void tryToJumpCharacter() {
 		if(characterView.getCharacterBody().getLinearVelocity().y <= 0.0001 && characterView.getCharacterBody().getLinearVelocity().y >= -0.0001){
 			inGameController.getWorldController().jumpBody();
 		}
 	}
 	/**
 	 * checks if the character is close enough to an item to pick it up.
 	 * @return the item that the character is standing at.
 	 */
 	public Item findItemToPickUp() {
 		for (int i = 0; i < inGameController.getWorldController().getItemViewList().size(); i++) {
 			if ((characterView.getSlickShape().intersects(inGameController.getWorldController().getItemViewList().get(i).getShape()) ||
 					characterView.getSlickShape().contains(inGameController.getWorldController().getItemViewList().get(i).getShape()))
 					&& !inGameController.getWorldController().getItemViewList().get(i).getItem().isDelivered()) {
 				return inGameController.getWorldController().getItemViewList().get(i).getItem();
 			}
 		}
 		return null;
 	}
 }
