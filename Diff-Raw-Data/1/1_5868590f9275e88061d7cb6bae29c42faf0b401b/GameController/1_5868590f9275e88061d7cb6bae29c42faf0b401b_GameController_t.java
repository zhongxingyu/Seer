 package controller;
 
 import java.awt.event.KeyEvent;
 import java.io.File;
 
 import javax.swing.Timer;
 
 import model.GameModel;
 import model.GameState;
 import model.GameStateFactory;
 import model.elements.Bullet;
 import model.elements.BulletDirection;
 import model.elements.Player;
 import utils.Input;
 import view.MainView;
 import view.render.GameStateRenderer;
 import view.state.GameViewState;
 
 import command.CommandFactory;
 import command.CommandListener;
 
 public class GameController extends AbstractController {
 
 	private Timer timer;
 	private GameStateRenderer renderer;
 	private GameStateFactory factory;
 	
 	private boolean moveInvadersRight = true;
 	
 
 	public GameController(MainView gw, GameModel gm) {
 		super(gw, gm);
 		
 		this.renderer = new GameStateRenderer();
 		this.factory = new GameStateFactory();
 		
 		// TODO dont init this here!
 		GameState gameState = factory.createLevelOne();
 		gameState.setLastUpdateTime(System.currentTimeMillis());
 		gm.setActiveGameState(gameState);
 	}
 	
 	private Timer getTimer() {
 		if (this.timer == null) {
 			this.timer = new Timer(50, new CommandListener(CommandFactory.createUpdateGameStateCommand(gameModel.getActiveGameState())));
 		}
 		
 		return timer;
 	}
 
 	/**
 	 * Sets the enabled state of the game loop, effectively starting and stopping a game session
 	 * @param enabled Whether the loop will run or not
 	 */
 	public void setGameLoopEnabled(boolean enabled) {
 		if (enabled) {
 			this.getTimer().start();
 		} else {
 			this.getTimer().stop();
 		}
 	}
 
 	/**
 	 * @param gameState
 	 *            The {@link GameState} whose values are to be stepped forward
 	 *            in time.
 	 */
 	public void updateGameState(GameState gameState) {
 
 		long currentTime = System.currentTimeMillis();
 		long timeDelta = currentTime - gameState.getLastUpdateTime();
 		
 		// Update Player
 		Player player = gameState.getPlayer();
 		if (Input.getInstance().isKeyDown(KeyEvent.VK_LEFT)) {
 			player.getPosition().x -= this.distance(timeDelta, player.getPlayerMovementSpeed());
 		}
 		if (Input.getInstance().isKeyDown(KeyEvent.VK_RIGHT)) {
 			player.getPosition().x += this.distance(timeDelta, player.getPlayerMovementSpeed());
 		}
 		if (Input.getInstance().isKeyDown(KeyEvent.VK_SPACE)) {
 			if(player.getTimeOfLastShot() - (int) System.currentTimeMillis() < - player.getPlayerShotFrequency()){	//the player can only shoot once per playerShotFrequency
 				player.setTimeOfLastShot((int) System.currentTimeMillis());
 				SoundController.playSound(new File("leftright.wav"),1,75);
 				
 				// TODO : Create the shot
 				Bullet currentShot = new Bullet(BulletDirection.Up);
 				currentShot.getPosition().x = player.getPosition().x + 24;
 				currentShot.getPosition().y = player.getPosition().y;
 				gameState.getShots().add(currentShot);
 			}
 		}
 
 		player.getPosition().x = Math.max(0, player.getPosition().x);
 		player.getPosition().x = Math.min(GameModel.SCREEN_WIDTH - 48, player.getPosition().x);
 		
 		// Move shots upwards
 		moveShots(gameState, timeDelta);
 		
 		//Checks for collisions
 		hasHitInvader(gameState);
 		
 		moveInvaders(gameState, timeDelta);
 
 		// Render the game state
 		GameViewState gameView = (GameViewState) mainView.getContentPane();
 		this.renderer.render(gameView.getDisplay(), gameState);
 		
 		// Update time stamp
 		gameState.setLastUpdateTime(currentTime);
 	}
 
 	/**
 	 * @param timeDelta Time passed since last frame
 	 * @param speed How fast is the object moving
 	 * @return A distance dependant on time, thereby avoiding glitches that make movement unstable
 	 */
 	private long distance(long timeDelta, int speed) {
 		return timeDelta/50 * speed;
 	}
 	
 	/**
 	 * @param gameState
 	 * Moves the bullets upwards
 	 */
 	private void moveShots(GameState gameState, long timeDelta){
 		// TODO : should check for collisions here instead of hasHitInvader-method!
 		int bulletSpeed = 0;
 		if(gameState.getShots().size() > 0){ //instead of 
 			bulletSpeed = gameState.getShots().get(0).getBulletSpeed();
 		}
 		
 		for (int i = 0; i < gameState.getShots().size(); i++) {
 			
 			if(gameState.getShots().get(i).getPosition().y <= 0){ //removes if moves outside JFrame
 				gameState.getShots().remove(i);
 			}else{
 				gameState.getShots().get(i).getPosition().y -= this.distance(timeDelta, bulletSpeed);
 				
 //				gameState.getShots().get(i).getPosition().y -= gameState.getShots().get(i).getBulletSpeed();
 			}
 			
 		}
 	}
 	
 	private void hasHitInvader(GameState gameState){
 		//TODO: er ikke 100% sikker p at den ALDRIG vil give Exception eller vil undlade at tjekke relevante skud
 		//Checks ALL shots against ALL invaders for collisions
 		int noOfInvaders = gameState.getInvaders().size(); //VERY(!!!) important these this stay. Removes risk of outOfBoundsException
 		int noOfShots = gameState.getShots().size();
 		for (int i = 0; i < noOfInvaders; i++) {
 			for (int j = 0; j < noOfShots; j++) {
 				if(gameState.getShots().get(j).getPosition().y < gameState.getInvaders().get(i).getPosition().y + gameState.getInvaders().get(i).getHeight()
			&&		gameState.getShots().get(j).getPosition().y + gameState.getShots().get(j).getHeight() > gameState.getInvaders().get(i).getPosition().y	
 			&&		gameState.getShots().get(j).getPosition().x < gameState.getInvaders().get(i).getPosition().x + gameState.getInvaders().get(i).getWidth()
 			&&		gameState.getShots().get(j).getPosition().x + gameState.getShots().get(j).getWidth() > gameState.getInvaders().get(i).getPosition().x	){
 					gameState.getShots().remove(j);
 					gameState.getInvaders().remove(i);
 					noOfInvaders--;
 					noOfShots--;
 					break;
 				}
 			}
 		}
 	}
 	
 	private void moveInvaders(GameState gameState, long timeDelta){
 		int gameWidthFucked = 500 - 20; //TODO: GameModel.SCREEN_WIDTH is correct, but the final check screws up...
 		int invaderSpeed = 0;
 		int leftmostInvader = gameState.getLeftmostInvader(), rightmostInvader = gameState.getRightmostInvader();
 		
 		if(gameState.getInvaders().size() > 0){ //instead of calling it for each invader. Perhaps one should, if there are different invaders, with different speeds.
 			invaderSpeed = gameState.getInvaders().get(0).getInvaderSpeed();
 		}
 		
 		/*
 		 * Flytter samtlige invaders, men tjekker inden om den aktuelle invader er den mest til venstre/hjre
 		 */
         for (int i = 0; i < gameState.getInvaders().size(); i++) {
         	if(gameState.getInvaders().get(i).getPosition().x + gameState.getInvaders().get(i).getWidth() > rightmostInvader){	//find rightmostInvader
         		rightmostInvader = gameState.getInvaders().get(i).getPosition().x + gameState.getInvaders().get(i).getWidth();
         	}
         	if(gameState.getInvaders().get(i).getPosition().x < leftmostInvader){
         		leftmostInvader = gameState.getInvaders().get(i).getPosition().x;
         	}
         	
         	if(moveInvadersRight){
         		gameState.getInvaders().get(i).getPosition().x +=  this.distance(timeDelta, invaderSpeed);
     		}else{ //move f***ing left :-)
     			gameState.getInvaders().get(i).getPosition().x -=  this.distance(timeDelta, invaderSpeed);
     		}
 		}
         
         /*
          * checks if sides are hit
          */
         if(rightmostInvader > gameWidthFucked){ //make 'em move left and down
     		moveInvadersRight = false;
     		System.out.println("Changing direction, rightmostInvader: "+rightmostInvader+" leftmostInvader: "+leftmostInvader);
     		for (int i = 0; i < gameState.getInvaders().size(); i++) {
     			gameState.getInvaders().get(i).getPosition().y += 8;
 			}
         }else if(leftmostInvader < 20){			//moves right and down
         	moveInvadersRight = true;
         	for (int i = 0; i < gameState.getInvaders().size(); i++) {
     			gameState.getInvaders().get(i).getPosition().y += 8;
 			}
         }
 		
 	}
 	
 	
 }
