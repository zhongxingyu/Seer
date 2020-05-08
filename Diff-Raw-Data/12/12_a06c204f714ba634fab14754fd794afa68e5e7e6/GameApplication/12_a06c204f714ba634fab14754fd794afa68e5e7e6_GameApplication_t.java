 package BattleBases;
 
 public class GameApplication {
 
 	/**
 	 * @param args
 	 * @throws InterruptedException 
 	 */
 	public static void main(String[] args) throws InterruptedException {
 		GameLoop game = new GameLoop();
 		// Create player one
 		Player player1;
 		AbstractWarrior[] warriors = new AbstractWarrior[4];
 		warriors[0] = new GruntWarrior();
 		warriors[1] = new GruntWarrior();
 		warriors[2] = new GruntWarrior();
 		warriors[3] = new GruntWarrior();
 		Army army = new Army(warriors);
 		player1 = new Player("Student", army );
 		
 		// Create player two
 		Player player2;
		warriors = new AbstractWarrior[4];
 		warriors[0] = new GoliathWarrior();
 		warriors[1] = new GoliathWarrior();
 		warriors[2] = new GoliathWarrior();
 		warriors[3] = new GoliathWarrior();
 		army = new Army(warriors);
 		player2 = new Player("Dorin", army);
 		
 		game.init(player1, player2);
 		int gameTimer = 0;
 		while (game.isRunning){
 			gameTimer++;
 			if(gameTimer >= Integer.MAX_VALUE){
 				System.out.println("You have played this game for too long, go outside and get some exercise!");
 				game.isRunning=false;
 			}
 			game.update(gameTimer);
 			game.draw();
 			Thread.sleep(100);
 		}
 	}
 
 }
