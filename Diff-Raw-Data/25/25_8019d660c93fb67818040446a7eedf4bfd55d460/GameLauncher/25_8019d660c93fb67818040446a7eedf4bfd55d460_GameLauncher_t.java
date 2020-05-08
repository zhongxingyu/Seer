 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.GameState;
 import edu.mines.csci598.recycler.backend.ModalMouseMotionInputDriver;
 import edu.mines.csci598.recycler.bettyCrocker.Song;
 import edu.mines.csci598.recycler.bettyCrocker.Track;
 import edu.mines.csci598.recycler.frontend.graphics.GameScreen;
 import edu.mines.csci598.recycler.frontend.graphics.InstructionScreen;
 import edu.mines.csci598.recycler.frontend.motion.ConveyorBelt;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 
 import java.awt.*;
 
 /**
  * This class launches 2 instances of GameLogic which represent the left and right games being played.
  * User: jzeimen
  * Date: 11/19/12
  * Time: 8:17 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameLauncher extends GameState {
 	private GameManager gameManager;
 	private GameLogic leftGame, rightGame;
     private GameStatusDisplay leftGameStatusDisplay, rightGameStatusDisplay;
 	private GameScreen gameScreen;
     private boolean gameStarted;
     private long timeInstructionsStarted;
 
     private InstructionScreen instructionScreen;
 
 	public GameLauncher() {
         //Preloading the images will prevent some flickering.
         //TODO: Preload background and correct/incorrect images too
         RecyclableType.preLoadImages();
		gameManager = new GameManager("Recycler",true);
 
 		gameScreen = GameScreen.getInstance();
         leftGameStatusDisplay = new GameStatusDisplay(Side.LEFT);
         rightGameStatusDisplay = new GameStatusDisplay(Side.RIGHT);
 
         gameScreen.addTextSpriteHolder(leftGameStatusDisplay);
         gameScreen.addTextSpriteHolder(rightGameStatusDisplay);
         leftGame = new GameLogic(
                 new RecycleBins(RecycleBins.Side.LEFT),
 				ConveyorBelt.getConveyorBeltPathLeft(),
                 gameManager,
                 leftGameStatusDisplay,
                 false,
                false,
                 false);
 		rightGame = new GameLogic(
                 new RecycleBins(RecycleBins.Side.RIGHT),
                 ConveyorBelt.getConveyorBeltPathRight(),
                 gameManager,
                 rightGameStatusDisplay,
                 GameConstants.SECOND_PLAYER_IS_A_COMPUTER,
                true,
                 GameConstants.DEBUG_COLLISIONS);
         leftGame.addLinkToOtherScreen(rightGame);
         rightGame.addLinkToOtherScreen(leftGame);
 
         instructionScreen = new InstructionScreen();
         gameStarted = false;
         timeInstructionsStarted = System.currentTimeMillis() / 1000;
 	}
 
 	protected void drawThis(Graphics2D g2d) {
         if (gameStarted) {
             gameScreen.paint(g2d, gameManager.getCanvas());
         }
         else {
             instructionScreen.paint(g2d, gameManager.getCanvas());
         }
 	}
 
 	public GameManager getGameManager() {
 		return gameManager;
 	}
 
 	public static void main(String[] args) {
         Song x = new Song();
         x.addTrack(new Track("src/main/resources/Sounds/recyclotron.mp3"));
         x.setLooping(true);
         x.startPlaying();
         GameLauncher gm = new GameLauncher();
 		ModalMouseMotionInputDriver mouse = new ModalMouseMotionInputDriver();
 		gm.getGameManager().installInputDriver(mouse);
 		gm.getGameManager().setState(gm);
 		gm.getGameManager().run();
 		gm.getGameManager().destroy();
 	}
 
 	public GameLauncher updateThis(float time) {
 
         if (gameStarted) {
 		    leftGame.updateThis();
 		    rightGame.updateThis();
         }
         else {
             if ((System.currentTimeMillis() / 1000) > timeInstructionsStarted + 5) {
                 gameStarted = true;
             }
         }
 		return this;
 	}
 
 }
