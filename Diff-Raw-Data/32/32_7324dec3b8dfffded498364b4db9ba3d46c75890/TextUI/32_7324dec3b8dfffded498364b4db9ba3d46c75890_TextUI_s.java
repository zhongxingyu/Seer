 package bombgame.tui;
 
 import bombgame.controller.GameHandler;
 import bombgame.controller.ManAI;
 import bombgame.controller.PlayerTUI;
 import bombgame.entities.Bomb;
 import bombgame.entities.Explosion;
 import bombgame.entities.GameObject;
 import bombgame.entities.Man;
 import bombgame.entities.Wall;
 
 /**
  * TextUI brings all components together.
  * @author JeGa, Rookfighter
  *
  */
 public final class TextUI {
 	
 	private GameHandler handler;
 	private PlayerTUI player;
 	private ManAI[] ais;
 	// KI - Maybe List to store ki
 	
 	/**
 	 * Creates a GameHandler with standard Constructor.
 	 */
 	public TextUI() {
 		Man manPlayer = new Man(0, 0); // Man for player
		Man manAi = new Man(8,8);
 		
 		handler = new GameHandler();
 		player = new PlayerTUI(manPlayer);
		ais = new ManAI[1];
 		ais[0] =  new ManAI(manAi, handler);
 		handler.addObject(manPlayer);
 		handler.addObject(manAi);
 		printField();
 	}
 	
 	public TextUI(final GameObject[][] field) {
 		handler = new GameHandler(field);
 	}
 
 	/**
 	 * Updates the KI calculation, movement of Man-objects, placement of Bombs
 	 * and the calculation of Explosions (in this order).
 	 */
 	public boolean update() {
 		//!! look at the order.
 		//scan input false;
 		//Scanner in = new Scanner(System.in);
		
 		
 		for(ManAI ai : ais) {
 			ai.calcNextStep();
 		}
 		player.move();
 		
 		handler.moveAll();
 		handler.updateBombs();
 		handler.placeBombs();
 		handler.updateExplosion();
 		
 		printAllPlayers();
 		printAI();
 		printBombs();
 		printExplosions();
 		printField();
 		return true;
 	}
 	
 	/**
 	 * Prints the current Map on the standard output.
 	 */
 	public void printField() {
 		
 		//print on screen
 		System.out.println(createTUI());
 	}
 	
 	/**
 	 * Creates and returns the String-object displaying the Interface of the Game.
 	 * @return - String-object, that represents User Interface
 	 */
 	protected String createTUI() {
 		
 		StringBuilder sb = new StringBuilder();
 		GameObject[][] field = handler.getField();
 		
 		for(int i = 0; i < field[0].length; i++) {
 			
 			for(int j = 0; j < field.length; j++) {
 				
 				if(field[j][i] instanceof Wall) {
 					
 					sb.append(" # ");
 					
 				} else if(field[j][i] instanceof Man) {
 				
 					sb.append(" M ");
 					
 				} else if(field[j][i] instanceof Bomb) {
 					
 					sb.append(" O ");
 					
 				}  else if(field[j][i] instanceof Explosion) {
 					
 					sb.append(" X ");
 					
 				} else {
 					
 					sb.append(" - ");
 					
 				}
 			}
 			
 			//next line
 			sb.append("\n");
 			
 		}
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * Prints coordinates and direction of all Man-objects (AI and Player)
 	 * on the standard output.
 	 */
 	public void printAllPlayers() {
 		System.out.println("Players:");
 		System.out.println(player);
 		// syso(ki) if more ki -> loop from list
 	}
 	
 	
 	/**
 	 * Prints the location of all bombs in the game.
 	 */
 	public void printBombs() {
 		System.out.println("Bombs:");
 		for (Bomb bomb : handler.getBombs()) {
 			System.out.println(bomb);
 		}
 	}
 	
 	
 	/**
 	 * Prints the location of all explosions in the game.
 	 */
 	public void printExplosions() {
 		System.out.println("Explosions:");
 		System.out.println(handler.explosionListToString());
 	}
 	
 	
 	/**
 	 * Prints the location of all ManAI-objects in the game.
 	 */
 	public void printAI() {
 		System.out.println("AI:");
 		for(ManAI ai : ais) {
 			System.out.println(ai);
 		}
 	}
 	
 	
 	/**
 	 * Returns the GameHandler-object currently used by the TextUI.
 	 * @return - GameHandler-object used by TextUI
 	 */
 	public GameHandler getGameHandler() {
 		return handler;
 	}
 	
 	
 	/**
 	 * Creates a new Player controlling the specified Man-object.
 	 * @param man - Man-object controlled by the new Player
 	 */
 	public void setPlayer(final Man man) {
 		player = new PlayerTUI(man);
 	}
 	
 	
 	/**
 	 * Returns the currently use Player.
 	 * @return - Player-object currently in use
 	 */
 	public PlayerTUI getPlayer() {
 		return player;
 	}
 	
 }
 
 
