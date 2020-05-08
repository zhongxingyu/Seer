 package distributed.systems.das.units;
 
 import java.io.Serializable;
 
 import distributed.systems.core.LogEntry.Position;
 import distributed.systems.core.SynchronizedSocket;
 import distributed.systems.das.BattleField;
 import distributed.systems.das.GameState;
 
 /**
  * A Player is, as the name implies, a playing 
  * character. It can move in the four wind directions,
  * has a hitpoint range between 10 and 20 
  * and an attack range between 1 and 10.
  * 
  * Every player runs in its own thread, simulating
  * individual behaviour, not unlike a distributed
  * server setup.
  *   
  * @author Pieter Anemaet, Boaz Pat-El
  */
 @SuppressWarnings("serial")
 public class Player extends Unit implements Runnable, Serializable {
 	/* Reaction speed of the player
 	 * This is the time needed for the player to take its next turn.
 	 * Measured in half a seconds x GAME_SPEED.
 	 */
 	protected int timeBetweenTurns;
 	public static final int MIN_TIME_BETWEEN_TURNS = 2;
 	public static final int MAX_TIME_BETWEEN_TURNS = 7;
 	public static final int MIN_HITPOINTS = 10;
 	public static final int MAX_HITPOINTS = 20;
 	public static final int MIN_ATTACKPOINTS = 1;
 	public static final int MAX_ATTACKPOINTS = 10;
 
 	protected Unit[][] map;
 
 	private transient int lastX = -1;
 	private transient int lastY = -1;
 	private transient int attemptsCounter = 0;
 
 	/**
 	 * Create a player, initialize both 
 	 * the hit and the attackpoints. 
 	 * @param bfUrl 
 	 * @param bfPort 
 	 */
 	public Player(int x, int y, String url, int port, String bfUrl, int bfPort) {
 		/* Initialize the hitpoints and attackpoints */
		super(url, port, bfUrl, bfPort, (int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), MIN_ATTACKPOINTS + (int)(Math.random() * MAX_ATTACKPOINTS));
 
 		/* Create a random delay */
 		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;
 
 		if (!spawn(x, y))
 			return; // We could not spawn on the battlefield
 		//setPosition(x, y);
 		
 		
 
 		/* Create a new player thread */
 		//new Thread(this).start();
 		runnerThread = new Thread(this);
 		runnerThread.start();
 	}
 
 	/**
 	 * Roleplay the player. Make the player act once in a while,
 	 * only stopping when the player is actually dead or the 
 	 * program has halted.
 	 * 
 	 * It checks a random direction, if an entity is located there.
 	 * If there is a player, it will try to heal that player if the
 	 * 50% health rule applies. If there is a dragon, it will attack
 	 * and if there is nothing, it will move in that direction. 
 	 */
 	@SuppressWarnings("static-access")
 	public void run() {
 		Direction direction;
 		UnitType adjacentUnitType;
 		int targetX = 0, targetY = 0;
 		
 		this.running = true;
 
 		while(GameState.getRunningState() && this.running) {
 			//System.out.println("LOOP");
 			try {			
 				/* Sleep while the player is considering its next move */
 				//Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));
				//Thread.currentThread().sleep((int)(500));
				
				Thread.currentThread().sleep(500 + (int)(Math.random()*300));
 
 				/* Stop if the player runs out of hitpoints */
 				if (getHitPoints() <= 0)
 					break;
 				
 				
 				Dragon closestDragon = (Dragon)closestUnitOfType(UnitType.dragon);
 				if(attemptsCounter<3) {
 					direction = inDirectionOfUnit(closestDragon);
 				} else {
 					direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];
 				}
 				// Randomly choose one of the four wind directions to move to if there are no units present
 				adjacentUnitType = UnitType.undefined;
 				
 				switch (direction) {
 					case up:
 						if (this.getY().intValue() <= 0)
 							// The player was at the edge of the map, so he can't move north and there are no units there
 							continue;
 						
 						targetX = this.getX();
 						targetY = this.getY() - 1;
 						break;
 					case down:
 						if (this.getY().intValue() >= BattleField.MAP_HEIGHT - 1)
 							// The player was at the edge of the map, so he can't move south and there are no units there
 							continue;
 
 						targetX = this.getX();
 						targetY = this.getY().intValue() + 1;
 						break;
 					case left:
 						if (this.getX().intValue() <= 0)
 							// The player was at the edge of the map, so he can't move west and there are no units there
 							continue;
 
 						targetX = this.getX() - 1;
 						targetY = this.getY();
 						break;
 					case right:
 						if (this.getX().intValue() >= BattleField.MAP_WIDTH - 1)
 							// The player was at the edge of the map, so he can't move east and there are no units there
 							continue;
 
 						targetX = this.getX() + 1;
 						targetY = this.getY();
 						break;
 				}
 
 				// Get what unit lies in the target square
 				adjacentUnitType = this.getType(targetX, targetY);
 				
 				switch (adjacentUnitType) {
 					case undefined:
 						// There is no unit in the square. Move the player to this square
 						if(lastX == this.getX().intValue() && lastY == this.getY().intValue()) {
 							attemptsCounter++;
 						} else {
 							lastX = this.getX();
 							lastY = this.getY();
 							attemptsCounter = 0;
 							
 						}
 						this.moveUnit(targetX, targetY);
 							
 						break;
 					case player:
 						// There is a player in the square, attempt a healing
 						this.healDamage(targetX, targetY, getAttackPoints());
 						break;
 					case dragon:
 						// There is a dragon in the square, attempt a dragon slaying
 						this.dealDamage(targetX, targetY, getAttackPoints());
 						break;
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Exit Unit Loop");
 		//clientSocket.unRegister();
 	}
 	
 	public static void main(String[] args) {
 
 		if(args.length==5) {
 			try {
 				BattleField.generatePlayeres(Integer.parseInt(args[0]), args[1],Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));				
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println("numberOfPlayers url startingPort battlefieldUrl  battlefieldPort");
 				System.exit(1);
 			}
 		} 
 		
 		
 
 		
 	}
 }
