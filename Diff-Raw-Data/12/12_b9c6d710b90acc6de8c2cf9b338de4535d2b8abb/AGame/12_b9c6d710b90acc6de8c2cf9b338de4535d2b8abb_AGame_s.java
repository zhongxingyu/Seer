 package com.testgame;
 
 import java.util.Random;
 
import android.util.Log;

 import com.testgame.mechanics.map.GameMap;
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.mechanics.unit.Ditz;
 import com.testgame.mechanics.unit.Jock;
 import com.testgame.mechanics.unit.Nerd;
 import com.testgame.player.APlayer;
 import com.testgame.player.ComputerPlayer;
 import com.testgame.resource.ResourcesManager;
 import com.testgame.scene.GameScene;
 
 /**
  * Abstract implementation of the IGame interface.
  * @author Alen Lukic
  *
  */
 public class AGame implements IGame {
 
 	
 	public ResourcesManager resourcesManager;
 	
 	protected GameScene gameScene;
 	
 	/**
 	 * Player 1.
 	 */
 	protected APlayer player;
 	
 	protected int turncount;
 
 	/**
 	 * Player 2.
 	 */
 	private ComputerPlayer compPlayer;
 	
 	private boolean firstTurn;
 	
 	/**
 	 * Signal for the end of a player's turn.
 	 */
 	protected boolean turnOver;
 
 	/**
 	 * The map.
 	 */
 	public GameMap gameMap;
 
 	/**
 	 * Map's x-dimension.
 	 */
 	protected int xDim;
 
 	/**
 	 * Map's y-dimension.
 	 */
 	protected int yDim;
 
 	/**
 	 * Delimiting line between the two players' sides.
 	 */
 	protected int divider;
 
 	/**
 	 * Random number generator.
 	 */
 	protected Random rand;
 
 	/**
 	 * Constructor.
 	 * @param pOne Player one.
 	 * @param pTwo Player two.
 	 * @param xDim The size of the x-dimension of the map.
 	 * @param yDim The size of the y-dimension of the map.
 	 */
 	public AGame(APlayer pOne, ComputerPlayer pTwo, int xDim, int yDim, GameScene game, boolean turn) {
 		this.resourcesManager = ResourcesManager.getInstance();
 		this.setFirstTurn(turn);
 		this.gameScene = game;
 		this.player = pOne;
 		this.setCompPlayer(pTwo);
 		this.gameMap = new GameMap(xDim, yDim);
 		this.xDim = xDim;
 		this.yDim = yDim;
 		this.divider = yDim / 2;
 		this.rand = new Random();
 		this.turnOver = false;
 		turncount = 0;
 		init();
 	}
 
 	/**
 	 * Performs initialization needed to begin the game.
 	 */
 	private void init() {
 		
 		int jocks = resourcesManager.unitArray.get(0);
 		int nerds = resourcesManager.unitArray.get(1);
 		int ditz = resourcesManager.unitArray.get(2);
 		int j = 0;
 		if(isFirstTurn())
 			j = 10;
 		for(int i = 0; i < 10; i++){
 				if(nerds > 0){
 					AUnit unit = new Nerd(gameMap, i, j, gameScene, "blue");
 					unit.init(); 
 					player.addUnit(unit);
 					nerds--;
 				}
 				else if(ditz > 0){
 					AUnit unit = new Ditz(gameMap, i, j, gameScene, "blue");
 					unit.init(); 
 					player.addUnit(unit);
 					ditz--;
 				}
 				else if(jocks > 0){
 					AUnit unit = new Jock(gameMap, i, j, gameScene, "blue");
 					unit.init(); 
 					player.addUnit(unit);
 					jocks--;
 				}
 			}
 	}
 
 	public int getCount(){
 		return turncount;
 	}
 	
 	public void incrementCount(){
 		turncount++;
 	}
 
 	/**
 	 * Ends the game.
 	 */
 	private void endGame(APlayer winner) {
 		// TODO: Send notification to the players
 		this.gameScene.setEndGameText(winner);
 	}
 	
 	public APlayer getPlayer() {
 		return this.player;
 	}
 
 	public ComputerPlayer getCompPlayer() {
 		return compPlayer;
 	}
 
 	public void setCompPlayer(ComputerPlayer compPlayer) {
 		this.compPlayer = compPlayer;
 	}
 
 	public boolean isFirstTurn() {
 		return firstTurn;
 	}
 
 	public void setFirstTurn(boolean firstTurn) {
 		this.firstTurn = firstTurn;
 	}
 
 	public GameScene getGameScene() {
 	
 		return gameScene;
 	}
 	
 	
 
 }
