 package fr.umlv.escape.game;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Iterator;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Filter;
 
 import android.content.Context;
 
 import fr.umlv.escape.file.IllegalFormatContentFile;
 import fr.umlv.escape.front.FrontApplication;
import fr.umlv.escape.front.SpriteShip.SpriteType;
import fr.umlv.escape.gesture.Gesture;
import fr.umlv.escape.gesture.GestureDetector;
import fr.umlv.escape.move.PlayerMove;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.ship.ShipFactory;
import fr.umlv.escape.weapon.ShootPlayer;
import fr.umlv.escape.world.Bodys;
 import fr.umlv.escape.world.CollisionMonitor;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**
  * Main class of the Escape game. It initialize and manage all the progress of the game
  */
 public class Game {
 	private Level currentLevel;
 	private int currentNbLv;
 	private int nbLevel;
 	private String[] levelsNames;
 	private static Game TheGame;
 	Player player1;
 	private FrontApplication frontApplication;
 	private CollisionMonitor collisionMonitor;
 	private final EscapeWorld escapeWorld;
 	private GameRoutine gameRoutine;
 	private final static int LIFE_NUMBER = 3;
 	//public volatile boolean isStarted;
 	private boolean isLevelLoaded = false;
 	
 	private Game(){
 		this.escapeWorld = EscapeWorld.getTheWorld();
 	}
 	
 	private class GameRoutine extends Thread {
 		private final long TIME_TO_RESPAWN = 3000;
 
 		Context context;
 		private GameRoutine(Context context){
 			this.context = context;
 		}
 		
 		@Override
 		public void run() {
 			long begin;
 			long elapsedWave;
 			long elapsedStep;
 			boolean isWaveFinished;
 			long lastDeath=0; //TODO gerer mort du joueur
 			
 			//Process all levels
 			while(currentNbLv<nbLevel && !Thread.currentThread().isInterrupted()){
 				System.out.println("CREATING LEVEL");
 				System.out.println(Arrays.toString(levelsNames));
 				if(!isLevelLoaded){
 					try {
 						currentLevel=LevelFactory.getTheLevelFactory().createLevel(context, levelsNames[currentNbLv]);
 						//isStarted = true;
 					} catch (IOException e1) {
 						Thread.currentThread().interrupt();
 					} catch (IllegalFormatContentFile e) {
 						Thread.currentThread().interrupt();
 					}	
 				}
 				/*synchronized (frontApplication.getBattleField()) {
 					frontApplication.getBattleField().notifyAll();
 				}*/
 				System.out.println(currentNbLv);
 				System.out.println(currentLevel);
 				while(currentLevel.launchNextWave() && !Thread.currentThread().isInterrupted()){
 					ArrayList<Ship> shipList;
 					begin=System.currentTimeMillis();
 					elapsedWave=0;
 					
 					isWaveFinished=false;
 					while(!isWaveFinished && !Thread.currentThread().isInterrupted()){
 						//world step every 15ms
 						elapsedStep=System.currentTimeMillis();
 						elapsedWave=elapsedStep-begin;
 						
 						escapeWorld.step();
 						collisionMonitor.performPostStepCollision(); // Process post collision treatment
 						
 						if(lastDeath==0 && (!player1.getShip().isAlive())){
 							if(player1.getLife()==0){
 								//TODO Game Over
 								return;
 							} else {
 								player1.setLife(player1.getLife()-1);
 								player1.ship.setHealth(99);
 								EscapeWorld.getTheWorld().setActive(player1.ship.body, false);
 								lastDeath=elapsedStep;
 							}
 						} else if(elapsedStep - lastDeath > TIME_TO_RESPAWN){
 							EscapeWorld.getTheWorld().setActive(player1.ship.body, true);
 							player1.getShip().setAlive(true);
 							lastDeath = 0;
 						}
 						isWaveFinished = true;
 						shipList=currentLevel.waveList.get(currentLevel.currentWave).shipList;
 						for(int i=0; i<shipList.size();++i){
 							Ship ship=shipList.get(i);
 							
 							if(ship.isAlive()){
 								ship.move();			// Move all ship of the wave
 								if(ship.shoot(ship.getPosXCenter(),ship.getPosYCenter())){
 									ship.fire();		// Make all ship shooting
 								}
 								isWaveFinished=false; // If their still are ship the wave is not finished
 							}
 						}
 						if((currentLevel.getCurrentDelay()!=0) && (elapsedWave>currentLevel.getCurrentDelay())){
 							System.out.println("Delay has expired must launched a wave");
 							isWaveFinished=true;
 						}
 						
 						try{
 							elapsedStep=System.currentTimeMillis()-elapsedStep;
 							if(elapsedStep>15){
 								elapsedStep=15;
 							}
 							Thread.sleep(15-elapsedStep);
 						} catch (InterruptedException e) {
 							Thread.currentThread().interrupt();
 						}
 					}
 					currentLevel.changeWave();
 				}
 				currentNbLv+=1;
 				isLevelLoaded = false;
 			}
 		}
 		
 	}
 
 	/**
 	 * Initialize all that is needed to launch the screen and start playing.
 	 * @throws IOException
 	 * @throws IllegalFormatContentFile
 	 */
 	public void initializeGame(Context context, FrontApplication frontAplication, String[] levelsNames) throws IOException, IllegalFormatContentFile{	
 		this.levelsNames = levelsNames;
 		this.nbLevel = levelsNames.length;
 		//this.isStarted = false;
 		this.currentNbLv=0;
 		this.frontApplication = frontAplication;
 		this.collisionMonitor=new CollisionMonitor(frontApplication.getBattleField());
 		Ship playerShip=ShipFactory.getTheShipFactory().createShip("default_ship_player",FrontApplication.WIDTH/2, FrontApplication.HEIGHT/3*2, 99, "PlayerMove");
 		Game.getTheGame().getFrontApplication().getBattleField().addShip(playerShip);
 		player1=new Player("Marc",playerShip,LIFE_NUMBER); 
 		this.currentLevel = LevelFactory.getTheLevelFactory().createLevel(context, levelsNames[currentNbLv]);
 		this.isLevelLoaded = true;
 	}
 
 	/**
 	 * Start the game. When this method is called, the simulation of the world begin.
 	 * 
 	 * @throws IOException
 	 * @throws IllegalFormatContentFile
 	 */
 	public void startGame(Context context) throws IOException, IllegalFormatContentFile{
 		this.gameRoutine = new GameRoutine(context);
 		this.gameRoutine.start();
 	}
 	
 	public Player getPlayer1() {
 		return player1;
 	}
 
 	/** Get the unique instance of {@link Game}.
 	 * @return The unique instance of {@link Game}
 	 */
 	public static Game getTheGame(){
 		if(Game.TheGame==null){
 			Game.TheGame = new Game();
 		}
 		return Game.TheGame;
 	}
 	
 	public FrontApplication getFrontApplication() {
 		return frontApplication;
 	}
 	
 	public void setFrontApplication(FrontApplication frontApplication) {
 		this.frontApplication = frontApplication;
 	}
 	
 	public void stop(){
 		gameRoutine.interrupt();
 	}
 }
