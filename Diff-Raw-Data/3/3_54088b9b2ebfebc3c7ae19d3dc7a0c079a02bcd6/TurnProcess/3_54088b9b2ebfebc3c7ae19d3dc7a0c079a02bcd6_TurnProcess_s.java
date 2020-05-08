 /*
  * All code unless credited otherwise is copyright 2012 Charles Sherman, all rights reserved
  */
 package atrophy.combat.mechanics;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.Stack;
 
 import watoydoEngine.designObjects.display.Crowd;
 import watoydoEngine.designObjects.display.TextButton;
 import watoydoEngine.gubbinz.Maths;
 import watoydoEngine.workings.DisplayManager;
 import watoydoEngine.workings.displayActivity.ActivePane;
 import atrophy.combat.CombatInorganicManager;
 import atrophy.combat.CombatMembersManager;
 import atrophy.combat.CombatUiManager;
 import atrophy.combat.CombatVisualManager;
 import atrophy.combat.actions.ActionSuite;
 import atrophy.combat.actions.CombatKeyboardHandler;
 import atrophy.combat.actions.CombatMouseHandler;
 import atrophy.combat.actions.MouseAbilityHandler;
 import atrophy.combat.ai.Ai;
 import atrophy.combat.ai.AiGenerator;
 import atrophy.combat.ai.ThinkingAi;
 import atrophy.combat.display.AiCrowd;
 import atrophy.combat.display.AiManagementSuite;
 import atrophy.combat.display.LineDrawer;
 import atrophy.combat.display.ui.CartographerBox;
 import atrophy.combat.display.ui.FloatingIcons;
 import atrophy.combat.display.ui.MessageBox;
 import atrophy.combat.display.ui.UiUpdaterSuite;
 import atrophy.combat.display.ui.loot.LootBox;
 import atrophy.hardPanes.SplashPane;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class TurnProcess.
  */
 public class TurnProcess {
 	
 	/**
 	 * The turn count.
 	 */
 	private int turnCount;
 	
 	/**
 	 * The shuffled ai.
 	 */
 	private Stack<Ai> shuffledAi;
 	
 	/**
 	 * The turn in progress.
 	 */
 	private boolean turnInProgress;
 
 	private AiCrowd aiCrowd;
 	private CombatMouseHandler combatMouseHandler;
 	private CombatKeyboardHandler combatKeyboardHandler;
 	private CombatUiManager combatUiManager;
 	private MessageBox messageBox;
 	private LootBox lootBox;
 	private CartographerBox cartographerBox;
 	private FloatingIcons floatingIcons;
 	private MouseAbilityHandler mouseAbilityHandler;
 	private CombatVisualManager combatVisualManager;
 	private CombatMembersManager combatMembersManager;
 	private CombatInorganicManager combatInorganicManager;
 	private LineDrawer lineDrawer;
 	
 	/**
 	 * Instantiates a new turn process.
 	 */
 	public TurnProcess(){
 		
 		turnCount = 0;
 		turnInProgress = false;
 		
 	}
 	
 	public void lazyLoad(AiManagementSuite aiManagementSuite, UiUpdaterSuite uiUpdaterSuite, CombatInorganicManager combatInorganicManager, ActionSuite actionSuite){
 		
 		this.aiCrowd = aiManagementSuite.getAiCrowd();
 		this.combatMembersManager = aiManagementSuite.getCombatMembersManager();
 		this.combatInorganicManager = combatInorganicManager;
 		this.lineDrawer = uiUpdaterSuite.getCombatUiManager().getLineSurface();
 		
 		this.combatVisualManager = uiUpdaterSuite.getCombatVisualManager();
 		this.combatUiManager = uiUpdaterSuite.getCombatUiManager();
 		this.messageBox = uiUpdaterSuite.getMessageBox();
 		this.lootBox = uiUpdaterSuite.getLootBox();
 		this.cartographerBox = uiUpdaterSuite.getCartographerBox();
 		this.floatingIcons = uiUpdaterSuite.getFloatingIcons();
 		
 		this.combatMouseHandler = actionSuite.getCombatMouseHandler();
 		this.combatKeyboardHandler = actionSuite.getCombatKeyboardHandler();
 		this.mouseAbilityHandler = actionSuite.getMouseAbilityHandler();
 		
 		shuffledAi = aiCrowd.getShuffledStack();
 		
 	}
 	
 	/**
 	 * End turn.
 	 */
 	public synchronized void endTurn(){
 		if(!turnInProgress){
 			turnInProgress = true;
 			combatMouseHandler.setActive(false);
 			combatKeyboardHandler.setFocus(false);
 			mouseAbilityHandler.cancelAbilitySetting();
 			combatUiManager.getActionsBar().setVisible(false);
 			for(int i = 0; i < aiCrowd.getActorCount(); i++){
 				aiCrowd.getMask(i).setActive(false);
 			}
 		
 			if(!allPlayersWaiting()){
 				
 				if(turnCount > 0 && turnCount%20 == 0){
 					combatUiManager.getLargeEventText().flashText("Turn " + turnCount, Color.white);
 				}
 				
 				turnCount++;
 			}
 			
 			messageBox.setVisible(false);
 			lootBox.setVisible(false);
 			cartographerBox.setVisible(false);
 			
 			combatVisualManager.setTableMasks(false);
 			updateTweens();
 			updateAiBrass();
 			updateAi();
 			lineDrawer.updateAlphas();
 		}
 	}
 	
 	/**
 	 * Update tweens.
 	 */
 	private void updateTweens(){
 		for(int i = 0; i < aiCrowd.getActorCount(); i++){
 			// If images haven't tweened to their target then skip them to their location
 			aiCrowd.getMask(i).updateImage();
 		}
 	}
 	
 	/**
 	 * Update assets.
 	 *
 	 * @param originator the originator
 	 */
 	private void updateAssets(Ai originator){
 		combatInorganicManager.updateAssets(originator);
 	}
 	
 	/**
 	 * Update ai brass.
 	 */
 	private void updateAiBrass(){
 		combatMembersManager.updateCommanders();
 	}
 	
 	/**
 	 * Update ai.
 	 */
 	private void updateAi(){
 		
 		if(shuffledAi.size() == 0)
 			shuffledAi = aiCrowd.getShuffledStack();
 
 		currentAiDone(shuffledAi.peek().isSkippingTurns());
 	}
 	
 	/**
 	 * Current ai done.
 	 *
 	 * @param aiEnd the ai end
 	 */
 	public void currentAiDone(boolean aiEnd){
 		
 		while(shuffledAi.size() > 0 && !allPlayersWaiting()){
 			
 			if(!shuffledAi.peek().isDead()){
 				
 				if(aiEnd && !shuffledAi.peek().isSkippingTurns()){
 					lastAiUpdated();
 					return;
 				}
 				
 				updateAssets(shuffledAi.peek());
 				
 				Ai currentAi = shuffledAi.pop();
 				
 				currentAi.action();
 				aiCrowd.getActorMask(currentAi).updateTween();
//				return;
 			}
 			else{
 				
 				updateAssets(shuffledAi.peek());
 				
 				// remove dead ai from stack
 				shuffledAi.pop();
 			}
 			
 			if(shuffledAi.size() == 0){
 				shuffledAi = aiCrowd.getShuffledStack();
 			}
 			
 			// if the next ai is a player then stop auto cycling
 			if(!shuffledAi.peek().isSkippingTurns()){
 				lastAiUpdated();
 				return;
 			}
 		}
 	
 		if(shuffledAi.size() == 0){
 			shuffledAi = aiCrowd.getShuffledStack();
 			currentAiDone(true);
 		}
 		
 		if(checkGameOver()){
 			lastAiUpdated();
 			endGame();
 			combatUiManager.getLargeEventText().flashText("Game Over, Turn: " + turnCount, Color.red.darker());
 		}
 	}
 	
 	// checks if all player controlled units are skipping turns to stop infinite looping
 	/**
 	 * All players waiting.
 	 *
 	 * @return true, if successful
 	 */
 	private boolean allPlayersWaiting() {
 		for(int i = 0; i < aiCrowd.getActorCount(); i++){
 			if(aiCrowd.getActor(i).getFaction().equals(AiGenerator.PLAYER) && !aiCrowd.getActor(i).isSkippingTurns() && !aiCrowd.getActor(i).isDead()){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Last ai updated.
 	 */
 	private void lastAiUpdated(){
 		
 		if(shuffledAi.size() == 0){
 			shuffledAi = aiCrowd.getShuffledStack();
 		}
 		
 		turnInProgress = false;
 		combatUiManager.getActionsBar().setVisible(true);
 		updateUi();
 		
 		if(checkGameOver()){
 			endGame();
 			combatUiManager.getLargeEventText().flashText("Game Over, Turn: " + turnCount, Color.red.darker());
 		}
 		
 		floatingIcons.updatePending();
 		messageBox.updateMessages();
 		
 		combatMouseHandler.setActive(true);
 		combatKeyboardHandler.setFocus(true);
 		combatUiManager.getActionsBar().setVisible(true);
 		
 		for(int i = 0; i < aiCrowd.getActorCount(); i++){
 			aiCrowd.getMask(i).setActive(true);
 		}
 		lineDrawer.updateAlphas();
 	}
 	
 	/**
 	 * Update ui.
 	 */
 	private void updateUi(){
 		
 		// Update any graphics or settings since all actors have done their actions
 		for(int i = 0; i < aiCrowd.getActorCount(); i++){
 			
 			//Update images to reflect any effects that happened in the turn
 			aiCrowd.getActorMask(aiCrowd.getActor(i)).updateMask();
 			
 			// If a target was killed/went out of room, all ai aiming at the target need to be updated
 			if((aiCrowd.getActor(i).getAction().equals("Aim") || aiCrowd.getActor(i).getAction().equals("Fire")) &&
 					
 			   (aiCrowd.getActor(i).getTargetAi() == null || aiCrowd.getActor(i).getTargetAi().isDead() || 
 			  
 			   aiCrowd.getActor(i).getTargetAi().getLevelBlock() != aiCrowd.getActor(i).getLevelBlock())
 			   
 				){
 				aiCrowd.getActor(i).setAction("");
 				aiCrowd.getActor(i).setTargetAi(null);
 				aiCrowd.getActor(i).setSwing(0);
 			}
 		}
 		
 		combatVisualManager.updateVisibleAi();
 		
 		// If player is at target then remove marker
 		if(combatMembersManager.getCurrentAi() != null &&
 		   Maths.getDistance(combatMembersManager.getCurrentAi().getLocation(), combatMembersManager.getCurrentAi().getMoveLocation()) == 0){
 			combatUiManager.getMoveFlag().setVisible(false);
 		}
 	
 		// Update help text as some items might be displaying old information
 		combatUiManager.updateUi();
 		combatUiManager.getMiniMap().updateAiDrawing();
 		combatUiManager.getFloatingIcons().updateOverlappingIcons();
 		
 		// reorder z in case units died
 		ActivePane.getInstance().getPane().computeZOrder();
 	}
 	
 	/**
 	 * Check game over.
 	 */
 	private boolean checkGameOver(){
 		for(int i = 0; i < aiCrowd.getActorCount(); i++){
 			if(!aiCrowd.getActor(i).isDead() &&
 			   aiCrowd.getActor(i).getFaction().equals("Player")){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * End game.
 	 */
 	private void endGame(){
 		combatVisualManager.revealAll();
 		
 		TextButton newGame = new TextButton(Color.yellow, Color.red) {
 			
 			{
 				this.setText("To Splash Screen");
 				this.setVisible(true);
 			}
 			
 			@Override
 			public boolean mD(Point mousePosition, MouseEvent e) {
 				ActivePane.getInstance().changePane(new Crowd(new SplashPane()));
 				return true;
 			}
 		};
 		
 		newGame.setLocation(DisplayManager.getInstance().getResolution()[0] /2 - 40, DisplayManager.getInstance().getResolution()[1] /2 + 30);
 		ActivePane.getInstance().getPane().addMouseActionItem(newGame);
 		ActivePane.getInstance().getPane().addDisplayItem(newGame);
 		
 //		TextButton newGame = new TextButton("New Game", Color.yellow, Color.red) {
 //			
 //			{
 //				this.setText("New Game");
 //				this.setVisible(true);
 //			}
 //			
 //			@Override
 //			public boolean mD(Point mousePosition, MouseEvent e) {
 //				ActivePane.getInstance().changePane(new Crowd("CurrentPane",false,new CharacterCreatePane()));
 //				return true;
 //			}
 //		};
 //		
 //		newGame.setLocation(DisplayManager.getInstance().getResolution()[0] /2 - 40, DisplayManager.getInstance().getResolution()[1] /2 + 30);
 //		ActivePane.getInstance().getPane().addMouseActionItem(newGame);
 //		ActivePane.getInstance().getPane().addDisplayItem(newGame);
 		
 //		TextButton loadGame = new TextButton("Load", Color.yellow, Color.red) {
 //			
 //			{
 //				this.setText("Load");
 //				this.setVisible(true);
 //			}
 //			
 //			@Override
 //			public boolean mD(Point mousePosition, MouseEvent e) {
 //				
 //				ActivePane.getInstance().setVisible(false);
 //				
 //				JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.home") + "/Atrophy"));
 //				int returnValue = chooser.showOpenDialog(new JFrame());
 //				
 //				if(returnValue == JFileChooser.APPROVE_OPTION){
 //					ActivePane.getInstance().changePane(new Crowd("CurrentPane",false,new GameMenuHardPane()));
 //					SaveFile.loadGame(chooser.getSelectedFile());
 //				}
 //				
 //				ActivePane.getInstance().setVisible(true);
 //				
 //				return true;
 //			}
 //		};
 //		
 //		loadGame.setLocation(DisplayManager.getInstance().getResolution()[0] /2 - 40, DisplayManager.getInstance().getResolution()[1] /2 + 60);
 //		ActivePane.getInstance().getPane().addMouseActionItem(loadGame);
 //		ActivePane.getInstance().getPane().addDisplayItem(loadGame);
 	}
 	
 	/**
 	 * Sets the turn count.
 	 *
 	 * @param turnCount the new turn count
 	 */
 	public void setTurnCount(int turnCount) {
 		this.turnCount = turnCount;
 	}
 
 	/**
 	 * Gets the turn count.
 	 *
 	 * @return the turn count
 	 */
 	public int getTurnCount() {
 		return turnCount;
 	}
 	
 	/**
 	 * Gets the top ai.
 	 *
 	 * @return the top ai
 	 */
 	public Ai getTopAi() {
 		return this.shuffledAi.peek();
 	}
 
 	/**
 	 * Replace ai.
 	 *
 	 * @param thinkingAi the thinking ai
 	 * @param playerAi the player ai
 	 */
 	public void replaceAi(ThinkingAi thinkingAi, Ai playerAi) {
 		for(int i = 0; i < shuffledAi.size(); i++){
 			if(shuffledAi.get(i) == thinkingAi){
 				shuffledAi.set(i, playerAi);
 			}
 		}
 	}
 	
 }
