 public class Starter {
 
 	public static void main(String[] args) {
		Game theGame = new Game(new SimPlayer("x",true,new AaronAI()), new Human("o",false,new AaronAI()));
 		Player winner = theGame.play();
 		if (winner == null) {
 			System.out.println("TIE");
 		} else {
 			System.out.println("WINNER:  " + winner.getXO());
 		}
 	}
 }
