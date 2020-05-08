 package com.testgame.scene;
 
 import java.util.ArrayList;
 
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.input.touch.TouchEvent;
 
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.scene.SceneManager.SceneType;
 import com.testgame.sprite.GameDialogBox;
 import com.testgame.sprite.HighlightedSquare;
 
 public class GuideScene extends GameScene {
	
	
	private ButtonSprite okayButton;
	private GameDialogBox dialogBox;
 
 	public enum GuidePhase {
 		SELECT_CHARACTER,
 		MOVE_UNIT,
 		ATTACK_UNIT,
 		WIN_GAME;
 	}
 	
 	private GuidePhase currentPhase;
 	
 	@Override
 	public void createScene() {
 		
 		resourcesManager.setMap("guide.tmx");
 		
 		resourcesManager.unitArray = new ArrayList<Integer>();
 		resourcesManager.unitArray.add(1);
 		resourcesManager.unitArray.add(2);
 		resourcesManager.unitArray.add(0);
 		
 		resourcesManager.unitArray2 = new ArrayList<Integer>();
 		resourcesManager.unitArray2.add(1);
 		resourcesManager.unitArray2.add(2);
 		resourcesManager.unitArray2.add(0);
 		
 		resourcesManager.isLocal = true;
 		
		okayButton = new ButtonSprite(0, 0, resourcesManager.continue_region, vbom, new OnClickListener() {

			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				dialogBox.dismiss();
			}});
		
 		super.createScene();
 		
 		currentPhase = GuidePhase.SELECT_CHARACTER;
 		switchToCurrentPhase();
 	}
 
 	private void switchToCurrentPhase() {
 		
 		switch(currentPhase) {
 		case SELECT_CHARACTER:
 			disableAllMoves();
 			disableAllAttacks();
 			
			ButtonSprite[] buttons = {okayButton};
			dialogBox = new GameDialogBox(this.hud, "Welcome to the guide! To view a unit's information, just tap on their picture.", 2,true,  buttons);
 			
 			break;
 		case MOVE_UNIT:
 			enableAllMoves();
 			
 			break;
 		case ATTACK_UNIT:
 			enableAllAttacks();
 			
 			break;
 		case WIN_GAME:
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	private void enableAllAttacks() {
 		for (AUnit u : this.game.getPlayer().getUnits()){
 			u.setCanAttack(true);
 		}
 	}
 
 	private void enableAllMoves() {
 		for (AUnit u : this.game.getPlayer().getUnits()){
 			u.setCanMove(true);
 		}
 	}
 
 	private void disableAllAttacks() {
 		for (AUnit u : this.game.getPlayer().getUnits()){
 			u.setCanAttack(false);
 		}
 	}
 
 	private void disableAllMoves() {
 		for (AUnit u : this.game.getPlayer().getUnits()){
 			u.setCanMove(false);
 		}
 	}
 
 	
 	@Override 
 	public void alertForAttack() {
 		if (currentPhase == GuidePhase.ATTACK_UNIT) {
 			currentPhase = GuidePhase.WIN_GAME;
 			switchToCurrentPhase();
 			
			ButtonSprite[] buttons = {okayButton};
			dialogBox = new GameDialogBox(this.hud, "Fantastic! You've learned all the moves! Keep playing til you win. :)", 2, true, buttons);
 		}
 	}
 	
 	@Override
 	public void squareTouched(HighlightedSquare sq, final TouchEvent pSceneTouchEvent) {
 		super.squareTouched(sq, pSceneTouchEvent);
 		
 		if (currentPhase == GuidePhase.MOVE_UNIT) {
 			currentPhase = GuidePhase.ATTACK_UNIT;
 			switchToCurrentPhase();
 			
			ButtonSprite[] buttons = {okayButton};
			dialogBox = new GameDialogBox(this.hud, "Nice! Next, try attacking by selecting one of your characters and then hitting a red highlighted unit.", 2, true, buttons);
 
 		}
 	}
 	
 	@Override
 	public void setSelectedCharacter(AUnit selectedCharacter) {
 
 		if (currentPhase == GuidePhase.SELECT_CHARACTER) {
 			currentPhase = GuidePhase.MOVE_UNIT;
 			switchToCurrentPhase();
 			
			ButtonSprite[] buttons = {okayButton};
			dialogBox = new GameDialogBox(this.hud, "Great job! To move a character, select a blue unit and then double tap on a blue square to move.", 2, true,  buttons);
 		}
 		
 		super.setSelectedCharacter(selectedCharacter);
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		SceneManager.getInstance().loadTutorialScene(engine);
 	}
 
 	@Override
 	public void onHomeKeyPressed() {
 		super.onHomeKeyPressed();
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_GUIDE;
 	}
 
 	@Override
 	public void disposeScene() {
 		super.disposeScene();
 	}
 
 }
