 public class TestAIEquality {
 	public static void main(String[] args) {
		AIEngine AI1 = new AaronAI();
		AIEngine AI2 = new MultithreadedAI();
 		Player randPlayer1 = new SimPlayer("x", true, new RandomAI()/*the AI doesn't actually matter here*/);
 		Player randPlayer2 = new SimPlayer("o", false, new RandomAI()/*the AI doesn't actually matter here*/);
 
 		for (int i = 0; i<100; i++) {
 			Game g = new Game(randPlayer1, randPlayer2);
 			while (randPlayer1.getAllMoves(g.getGameBoard()).length>0 && randPlayer2.getAllMoves(g.getGameBoard()).length>0 && !g.isDraw()) {
 				// System.out.println("moving player 1");
 				Move[] unrankedMoves = randPlayer1.getAllMoves(g.getGameBoard());
 				Move[] moves1 = AI1.rankBestMove(unrankedMoves, g, randPlayer1, 5);
 				Move[] moves2 = AI2.rankBestMove(unrankedMoves, g, randPlayer1, 5);
 				for (int moveIndex = 0; moveIndex<moves1.length; moveIndex++) {
 					if (!moves1[moveIndex].equals(moves2[moveIndex])) {
 						System.out.println("wrong move!");
 						break;
 					}
 				}
 				g = new Game(g, moves1[0]);
 
 
 				if (randPlayer1.getAllMoves(g.getGameBoard()).length>0 && randPlayer2.getAllMoves(g.getGameBoard()).length>0 && !g.isDraw()) {
 					unrankedMoves = randPlayer2.getAllMoves(g.getGameBoard());
 					moves1 = AI1.rankBestMove(unrankedMoves, g, randPlayer2, 5);
 					moves2 = AI2.rankBestMove(unrankedMoves, g, randPlayer2, 5);
 					for (int moveIndex = 0; moveIndex<moves1.length; moveIndex++) {
 						if (!moves1[moveIndex].equals(moves2[moveIndex])) {
 							System.out.println("wrong move!");
 							break;
 						}
 					}
 					// System.out.println("moving player 2");
 					g = new Game(g, moves1[0]);
 				}
 			}
 			System.out.print("" + i + "th ");
 			System.out.println("Game finished!");
 		}
 	}
 }
