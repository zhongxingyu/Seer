 package brown.games.tictac;
 
 import java.io.InputStream;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import brown.games.Evaluation;
 import brown.games.GameMove;
 import brown.games.GameState;
 import brown.games.algos.MiniMaxEvaluation;
 
 /**
  * @author Matt Brown msbcode@gmail.com
  * @date Jun 3, 2010
  */
 public class TTRunner {
 
 	private static final Logger log = LoggerFactory.getLogger(TTRunner.class);
 
 	private TicTacPlayer human;
 
 	private TicTacPlayer computer;
 
 	private Evaluation evaluation;
 
 	private int size;
 
 	public TTRunner(TicTacPlayer human, TicTacPlayer computer, Evaluation evaluation, int size) {
 		this.human = human;
 		this.computer = computer;
 		this.evaluation = evaluation;
 		this.size = size;
 
 	}
 
 	public static void main(String[] args) {
 
 		TicTacPlayer human = new TicTacPlayer(Tile.X);
 		TicTacPlayer computer = new TicTacPlayer(Tile.O);
 
		int ply = 2;
 		int size = 3;
 
 		Evaluation evaluation = new MiniMaxEvaluation(ply);
 
 		TTRunner runner = new TTRunner(human, computer, evaluation, size);
 		runner.runGame(System.in);
 
 	}
 
 	public void runGame(InputStream stream) {
 
 		// 1. ask user for input
 		// 2. make move
 		// 3. draw board
 		// 4. determine computers move, make move
 		// 5. draw board
 		// 6. repeat until won or draw
 
 		TicTacGameState state = new TicTacGameState(size);
 
 		Scanner scanner = new Scanner(System.in);
 		scanner.useDelimiter(System.getProperty("line.separator"));
 
 		while (!state.isDraw() && !state.isWin()) {
 			GameMove move = getNextPlayerMove(scanner, state);
 
 			if (log.isInfoEnabled()) log.info("runGame: player selected move {}", move);
 
 			move.execute(state);
 			state.board.prettyPrint(System.out);
 
 			// check if game just ended
 			if (state.isDraw() || state.isWin()) break;
 
 			GameMove opponentMove = evaluation.bestMove(state, computer, human);
 			System.out.println("The computer has decided where to move: " + opponentMove);
 			opponentMove.execute(state);
 			state.board.prettyPrint(System.out);
 		}
 
 		if (state.isDraw()) {
 			System.out.println("The game was a draw - bummer.");
 		}
 		else if (state.isWinner(human)) {
 			System.out.println("Wow, you beat a computer at tic tac toe - congratulations!");
 		}
 		else {
 			System.out.println("You lost!");
 		}
 	}
 
 	/**
 	 * Returns the player's next move, after asking player to input move. This
 	 * method handles checking if move is valid for the game state.
 	 */
 	private GameMove getNextPlayerMove(Scanner scanner, GameState state) {
 
 		GameMove move = null;
 
 		System.out.print("You are X. Which position would you like to go in (1-9)? ");
 		boolean legal = false;
 		while (!legal) {
 			boolean inputMismatch = false;
 			try {
 				int pos = scanner.nextInt();
 				legal = pos > 0 && pos <= (size * size);
 				if (!legal) inputMismatch = true;
 
 				if (legal) {
 					// decrement pos since board is 0-based
 					pos--;
 					move = new TicTacGameMove(human.getTile(), pos / size, pos % size);
 					if (!move.isValid(state)) {
 						System.out.print("That move is taken. Please enter another: ");
 						legal = false;
 					}
 				}
 			}
 			catch (InputMismatchException e) {
 				inputMismatch = true;
 				// advance past the token that caused the problem
 				scanner.next();
 			}
 
 			if (inputMismatch) System.out.println("Please enter 1 thru 9: ");
 
 		}
 
 		return move;
 	}
 }
