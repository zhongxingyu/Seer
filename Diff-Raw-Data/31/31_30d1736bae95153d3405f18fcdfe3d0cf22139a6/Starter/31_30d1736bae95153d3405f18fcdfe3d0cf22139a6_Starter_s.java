 public class Starter {
 
 	public static void main(String[] args) {
		Game theGame = new Game(new SimPlayer("x",true,new MultithreadedAI()), new SimPlayer("o",false,new MultithreadedAI()));
 		Player winner = theGame.play();
 		if (winner == null) {
 			System.out.println("TIE");
 		} else {
 			System.out.println("WINNER:  " + winner.getXO());
 		}
 	}
 }
