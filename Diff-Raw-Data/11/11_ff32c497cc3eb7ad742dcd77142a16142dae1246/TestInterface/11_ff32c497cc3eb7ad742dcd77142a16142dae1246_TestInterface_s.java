 package game.ui;
 
 import java.util.Stack;
 
 import game.logic.Game;
 import general_utilities.WaitTime;
 
 
 public class TestInterface  extends GameInterface {
 	
 	private GameOptions options;
 	private Stack<Character> heroMoves;
 	
 	
 	public TestInterface(GameOptions options, Stack<Character> heroMoves){
 		this.options = options;
 		this.heroMoves = heroMoves;
 	}
 	
 	public void startGame() {
 		game = new Game(options);
 		mainLoop(heroMoves);
 	}
 
 	private void mainLoop(Stack<Character> heroMoves) {
 
 		boolean goOn = true;
 		char input = ' ';
 
 		while( !heroMoves.empty() && goOn ){
 
 			try {
 				input = heroMoves.pop();
 			}
 			catch(Exception e) {
 				System.err.println("Problem reading user input!");
 			}
 
 			goOn = game.heroTurn(input);
 
 			//GameOutput.clearScreen();
			System.out.println("Heros' turn");
 			GameOutput.printGame(game);
 			WaitTime.wait(250);
 
 			goOn = game.dragonTurn(goOn);
 			//GameOutput.clearScreen();
 			System.out.println("Dragons' turn");
 			GameOutput.printGame(game);
 			WaitTime.wait(250);
 
 			goOn = game.checkState(goOn);
 
 			//GameOutput.clearScreen();
 			GameOutput.printEventQueue(game.getEvents() );
 			GameOutput.printGame(game);
 			WaitTime.wait(250);
 
 		}
 
 	}
 
 }
