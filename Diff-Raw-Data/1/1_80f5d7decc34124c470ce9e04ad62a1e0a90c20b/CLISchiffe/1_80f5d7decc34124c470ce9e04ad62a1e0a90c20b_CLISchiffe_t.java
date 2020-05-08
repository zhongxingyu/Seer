 package testpackage.interfaces;
 
 import testpackage.shared.ship.AI;
 import testpackage.shared.ship.BadAI;
 import testpackage.shared.ship.Engine;
 import testpackage.shared.ship.Rules;
 import testpackage.shared.ship.exceptions.InvalidInstruction;
 //import testpackage.shared.ship.exceptions.InvalidLevelException;
 import acm.program.ConsoleProgram;
 
 /**
  * command line interface (ACM ConsoleProgram)
  */
 public class CLISchiffe extends ConsoleProgram {
 
 	private static final long serialVersionUID = 1L;
 
 	private Engine engine; 
 	
 	/**
 	 * constructor initializes game engine, which constructs game boards and so on
 	 */
 	public CLISchiffe() {
 		engine = new Engine();
 	}
 	
 	/**
 	 * ACM JTF entry point
 	 */
 	public void run() {
 		boolean shotspership = readBoolean("Choose shooting ships?");
 		boolean moreshots = readBoolean("Extra shot when hitting opponent?");
 		boolean reichweite = false;
 		if (shotspership) {
 			engine.enableShotsPerShip(Rules.shotsPerShipPart);
 			reichweite = readBoolean("Limit ship range?");
 			if (reichweite) engine.enableRange();
 		}
 		engine.setMoreShots(moreshots);
 		
 		int winner = -1;
 		AI computer = new BadAI();
 		computer.setEngine(engine);
 		while (!engine.isFinished()) {
 			if (engine.getState().isPlayerTurn()) {
 				print(engine.getLevelStringForPlayer(0));
 				if (shotspership) print(engine.getAmmoStringForPlayer(0));
 				println("Your turn!");
 				boolean tryAgain;
 				do {
 					tryAgain = false;
 					
 					if (shotspership) {
 						int shootery = readInt("Shooter X Koordinate:");
 						int shooterx = readInt("Shooter Y Koordinate:");
 						try {
 							engine.chooseFiringXY(0, shooterx, shootery);
 							if (reichweite) println(Engine.getTargetString(engine.getTargets(0, shooterx, shootery)));
 						} catch (InvalidInstruction e) {
 							showErrorMessage(e.getMessage());
 							continue;
 						}
 					}
 					
 					int y = readInt("X Koordinate:"); //coordinates are exchanged cause it's confusing
 					int x = readInt("Y Koordinate:"); //if X is downwards and Y is towards right
 
 					try {
 						engine.attack(0, x, y);
 					} catch (InvalidInstruction e) {
 						showErrorMessage(e.getMessage());
 						tryAgain = true; // player gets another try
 					}
 				} while (tryAgain);
 			} else {
 				println("Computer plays!");
 				//pause(1000);
 				computer.playAs(1); // play as player 2 (players are 0 and 1)
 			}
 			
 			winner = engine.checkWin().playernr; // updates also isFinished()
 		}
 		println("Game over");
 		if (winner == -1) {
 			println("Draw");
 		} else {
 			println("Player " + (winner+1) + " won!");
 		}
 
 	}
 	
 	public static void main(String[] args) {
 		new CLISchiffe().start();
 	}
 
 }
