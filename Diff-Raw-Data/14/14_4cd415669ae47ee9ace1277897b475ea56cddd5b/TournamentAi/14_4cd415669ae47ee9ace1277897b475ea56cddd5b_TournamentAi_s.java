 package rps.client.ai;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import rps.client.GameListener;
 import rps.game.Game;
import rps.game.GameImpl;
import rps.game.GameImplFixture;
 import rps.game.data.AttackResult;
 import rps.game.data.Figure;
 import rps.game.data.FigureKind;
 import rps.game.data.Move;
 import rps.game.data.Player;
 
 /**
  * This class contains an advanced AI, that should participate in the
  * tournament.
  */
 public class TournamentAi implements GameListener {
 
 	// time limits
 	private final int maxDurationForMoveInMilliSeconds;
 	private final int maxDurationForAllMovesInMilliSeconds;
 	
 	// game environment
 	private Game game;
 	private Player player = new Player("Gruppe (#105)");
 	
 	// flag for discovered statistic update after drawn attack
 	private boolean lastAttackWasDrawn = false;
 	
	private int movesCounter = 0; //Zahl der ausgeführten Züge der KI
 	
 	// discovered stuff
 	private int discoveredRocks = 0, discoveredPapers = 0, discoveredScissors = 0, discoveredTraps = 0;
 	private ArrayList<Figure> discoveredFigures = new ArrayList<Figure>();
 	
 	// kind choice memory
 	private ArrayList<FigureKind> lastFigureKindChoices = new ArrayList<FigureKind>();
 
 	/**
	 * Create tournamen ai.
 	 * 
 	 * Tournament AI uses the minimax algorithm to provide moves.
 	 * The algorithm uses alpha-beta-pruning for optimization.
 	 * 
 	 * @param maxDurationForMoveInMilliSeconds
 	 * @param maxDurationForAllMovesInMilliSeconds
 	 */
 	public TournamentAi(int maxDurationForMoveInMilliSeconds, int maxDurationForAllMovesInMilliSeconds) {
 		this.maxDurationForMoveInMilliSeconds = maxDurationForMoveInMilliSeconds;
 		this.maxDurationForAllMovesInMilliSeconds = maxDurationForAllMovesInMilliSeconds;
 		// TODO Auto-generated constructor stub
 	}
 	
 	/**
 	 * returns the AI player.
 	 */
 	public Player getPlayer() {
 		return this.player;
 	}
 
 	@Override
 	public void chatMessage(Player sender, String message) throws RemoteException {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void provideInitialAssignment(Game game) throws RemoteException {
 		this.game = game;
 		
 		// random flag and trap position
 		Random random = new Random();
 		int randomFlagPosition = 28 + random.nextInt(7);
 		
 		// Liste mit allen eigenen Figuren erstellen
 		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
 				
 		for(int i =0; i<4; i++) {
 			list.add(FigureKind.PAPER);
 			list.add(FigureKind.ROCK);
 			list.add(FigureKind.SCISSORS);
 		}
 		
 		Collections.shuffle(list); // Liste mischen -> zufällige Anordnung
 		
 		// komplettes Feld
 		FigureKind[] initialAssignment = new FigureKind[42];
 		
 		int i=0;
 		for(int j=28, k=35; j<35; j++, k++) {
 			if(j == randomFlagPosition) {
 				initialAssignment[j] = FigureKind.TRAP;
 				initialAssignment[k] = FigureKind.FLAG;
 				continue;
 			}
 			
 			initialAssignment[j] = list.get(i); // 28 offset for top assignment
 			initialAssignment[k] = list.get(i+1); // 28 offset for top assignment
 			i += 2;
 		}
 		
 		this.game.setInitialAssignment(this.player, initialAssignment);
 	}
 
 	@Override
 	public void provideInitialChoice() throws RemoteException {
 		this.game.setInitialChoice(getPlayer(), BasicAi.randomChoice());
 	}
 
 	@Override
 	public void startGame() throws RemoteException {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void provideNextMove() throws RemoteException {
 		long moveCalculationStartedAt = System.nanoTime();
 
 		//"mini-eröffnungsbuch": 2 first moves für die Ai: zwei figuren in der mitte je 1 feld vor
 		/*if(firstMoves==0) {
 			if(this.game.getField()[32].getKind().isMovable()) {
 				this.game.move(this.player, 32, 25);
 			}
 			else {
 				this.game.move(this.player, 30, 23);
 			}
 			firstMoves++;
 		}
 		else if(firstMoves==1) {
 			if(this.game.getField()[31].getKind().isMovable()) {
 				this.game.move(this.player, 31, 24);
 			}
 			else {
 				this.game.move(this.player, 30, 23);
 			}
 			firstMoves++;
 		}
 		*/
 		//normaler Zug
 		//else {
 			Move move = minimax(this.game.getField(), moveCalculationStartedAt);
 			this.game.move(this.player, move.getFrom(), move.getTo());
 		//}
 	}
 
 	@Override
 	public void figureMoved() throws RemoteException {}
 
 	@Override
 	public void figureAttacked() throws RemoteException {
 		// update statistic about discovered figures
 		updateDiscoveredStatistic(this.game.getLastMove());
 	}
 
 
 	@Override
 	public void provideChoiceAfterFightIsDrawn() throws RemoteException {
 		this.game.setUpdatedKindAfterDraw(player, BasicAi.randomChoice());
 		this.lastAttackWasDrawn = true;
 	}
 	
 
 	@Override
 	public void gameIsLost() throws RemoteException {}
 	
 
 	@Override
 	public void gameIsWon() throws RemoteException {}
 	
 
 	@Override
 	public void gameIsDrawn() throws RemoteException {}
 	
 
 	@Override
 	public String toString() {
 		return "Tournament AI";
 	}
 	
 	
 	/**
 	 * Return the chance that a hidden figure the kind provided.
 	 * 
 	 * @param kind
 	 * @return
 	 */
 	public double getChanceByFigureKind(FigureKind kind) {
 		double result = 0.0;
 		
 		int undiscoveredFiguresCount = 14 - (this.discoveredRocks
 				+ this.discoveredPapers
 				+ this.discoveredScissors
 				+ this.discoveredTraps);
 		
 		// TODO add second counter for simulation discovers
 		if(kind == FigureKind.ROCK) {
 			result = (4.0 - this.discoveredRocks) / undiscoveredFiguresCount;
 		} else if(kind == FigureKind.PAPER) {
 			result = (4.0 - this.discoveredPapers) / undiscoveredFiguresCount;
 		} else if(kind == FigureKind.SCISSORS) {
 			result = (4.0 - this.discoveredScissors) / undiscoveredFiguresCount;
 		} else if(kind == FigureKind.TRAP) {
 			result = (1.0 - this.discoveredTraps) / undiscoveredFiguresCount;
 		} else if(kind == FigureKind.FLAG) {
 			result = 1.0 / undiscoveredFiguresCount;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Get the figure kind that has most chance to be the kind that a hidden figure of the opponent has.
 	 * @return
 	 */
 	public FigureKind getFigureKindByStatistic() {
 		if(this.discoveredPapers >= this.discoveredRocks && this.discoveredPapers >= this.discoveredScissors) {
 			return FigureKind.PAPER;
 		} else if(this.discoveredRocks >= this.discoveredPapers && this.discoveredRocks >= this.discoveredScissors) {
 			return FigureKind.ROCK;
 		} else {
 			return FigureKind.SCISSORS;
 		}
 	}
 
 	/**
 	 * minimax algorithm
 	 * 
 	 * Is called by provideNextMove to determine a good move for the ai.
 	 * 
 	 * @param board
 	 * @return
 	 * @throws RemoteException
 	 */
 	public Move minimax(Figure[] board, long startTime) throws RemoteException {
 		Figure[] newBoard;
 		int score = Integer.MIN_VALUE;
 		Move result = null;
 		
 		board = replaceHiddenFiguresWithPossibleRealFigures(board);
 		
 		for(Move move: BasicAi.getPossibleMoves(board, this.player)) {
 			if(move == null) {
 				continue;
 			} 
 			
 			newBoard = performMove(board, move);
 			int minScore = minValue(newBoard, startTime, Integer.MIN_VALUE, Integer.MAX_VALUE);
 			
 			if(minScore > score) {
 				result = move;
 				score = minScore;
 			}
 		}
 
 		movesCounter++;
 		return result;
 	}
 
 	/**
 	 * Replace hidden figures on a board with possible figures for the player.
 	 * 
 	 * Uses the discovered statistic to replace the hidden figures on a board
 	 * with real figure kinds. This is used for simulation in the minimax algorithm.
 	 * 
 	 * @param board
 	 * @return
 	 * @throws RemoteException
 	 */
 	private Figure[] replaceHiddenFiguresWithPossibleRealFigures(Figure[] board)
 			throws RemoteException {
 		// clone old board
 		Figure[] newBoard = board.clone();
 		
 		// create array with remaining figure kinds for hidden figures
 		ArrayList<FigureKind> hiddenFigures = new ArrayList<FigureKind>();
 		for(int i=this.discoveredRocks; i<4; i++) {
 			hiddenFigures.add(FigureKind.ROCK);
 		}
 		for(int i=this.discoveredPapers; i<4; i++) {
 			hiddenFigures.add(FigureKind.PAPER);
 		}
 		for(int i=this.discoveredScissors; i<4; i++) {
 			hiddenFigures.add(FigureKind.SCISSORS);
 		}
 		if(this.discoveredTraps == 0) {
 			hiddenFigures.add(FigureKind.TRAP);
 		}
 		hiddenFigures.add(FigureKind.FLAG);
 		
 		// shuffle array to get a random allocation
 		Collections.shuffle(hiddenFigures);
 		
 		// replace hidden figures with figure kinds from the shuffled array
 		int j=0;
 		for(int i=0; i<newBoard.length; i++) {
 			if(newBoard[i] != null && newBoard[i].getKind() == FigureKind.HIDDEN) {
 				newBoard[i] = new Figure(hiddenFigures.get(j), getOpponent());
 				j++;
 			}
 		}
 		
 		// return board without hidden figures
 		return newBoard;
 	}
 	
 	/**
 	 * max-Function of the minimax algorithm.
 	 * 
 	 * @param board
 	 * @param depth
 	 * @param alpha
 	 * @param beta
 	 * @return
 	 * @throws RemoteException
 	 */
 	public int maxValue(Figure[] board, long startTime, int alpha, int beta) throws RemoteException {
 		// end-state reached?
 		if(terminalTest(board)) {
 			return utility(board);
 		}
 		// stop recursion
 		else if(System.nanoTime()-startTime >= 750000L * this.maxDurationForMoveInMilliSeconds) {
 			return utility(board);
 		}
 		
 		int v = Integer.MIN_VALUE;
 		
 		for(Move move: BasicAi.getPossibleMoves(board, this.player)) {
 			// skip empty moves (no valid action)
 			if(move == null) {
 				continue;
 			}
 			
 			// update state
 			Figure[] newBoard = performMove(board, move);
 			
 			// evaluate
 			v = Math.max(v, minValue(newBoard, startTime, alpha, beta));
 			if(v >= beta) {
 				return v;
 			} else {
 				alpha = Math.max(alpha, v);
 			}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * min-Function of the minimax algorithm
 	 * 
 	 * @param board
 	 * @param depth
 	 * @param alpha
 	 * @param beta
 	 * @return
 	 * @throws RemoteException
 	 */
 	public int minValue(Figure[] board, long startTime, int alpha, int beta) throws RemoteException {
 		// end-state reached?
 		if(terminalTest(board)) {
 			return utility(board);
 		}
 		// stop recursion
 		else if(System.nanoTime()-startTime >= 750000L * this.maxDurationForMoveInMilliSeconds) {
 			return utility(board);
 		}
 		
 		int v = Integer.MAX_VALUE;
 
 		for(Move move: BasicAi.getPossibleMoves(board, getOpponent())) {
 			// skip empty moves (no valid action)
 			if(move == null) {
 				continue;
 			}
 			
 			// update state
 			Figure[] newBoard = performMove(board, move);
 			
 			// evaluate
 			v = Math.min(v, maxValue(newBoard, startTime, alpha, beta));
 			if(v <= alpha) {
 				return v;
 			} else {
 				beta = Math.min(beta, v);
 			}
 		}
 		
 		return v;
 	}
 	
 	/**
 	 * Return whether game is over.
 	 * 
 	 * @return
 	 * @throws RemoteException 
 	 */
 	public boolean terminalTest(Figure[] board) throws RemoteException {
 		return gameIsDefinitlyDrawn(board) || gameIsDefinitlyWon(board) || gameIsLost(board);
 	}
 	
 	
 	/**
 	 * Determines a score for a given state.
 	 * 
 	 * @return
 	 * @throws RemoteException 
 	 */
 	public int utility(Figure[] board) throws RemoteException {
 		int result = 0;
 
 		// return a static value if the game is over
 		if(gameIsLost(board)) {
 			result = Integer.MIN_VALUE;
 		} else if(gameIsDefinitlyWon(board)) {
 			result = Integer.MAX_VALUE;
 		} else if(gameIsDefinitlyDrawn(board)) {
 			result = 0;
 		}
 		// use a heuristic to score an unfinished game
 		else {
 			
 			for(int i=0; i<board.length; i++) {
 				if(board[i] != null) {
 					if(board[i].belongsTo(getPlayer())) {
 						
 						if(movesCounter < 7) {//andere Bewertung für die ersten 7 züge -> Aufbau
 							//je weiter vorne eine figur steht, desto höher ihre bewertung
 							if(i<7) { // letzte reihe
 								result += 80;
 							}
 							else if(i<14) { // vorletzte Reihe
 								result += 70;
 							}
 							else if(i<21) { // 4. Reihe
 								result += 60;
 							}
 							else if(i<28) { // 3. Reihe
 								result -= Math.abs(i - 24); //entfernung vom mittelfeld wird abgezogen -> Ai startet das Vorziehen über die Mitte
 								result += 50;
 							}
 							else if(i<35) { //2. Reihe
 								result += 30;
 							}
 						}
 						else { //im weiteren spielverlauf (ab 8. Zug)
 							//jede reihe gleichwertig, hauptsache nach vorne
 							for(int a=35; a>i; a-=7) {
 								result++;
 							}							
 						}
 						
 						result += 10000; // +10000 für jede eigene figur auf dem Feld
 					} else {
 						result -= 10000; // -10000 für jede gegnerische figur auf dem Feld
 					}
 				}
 			}			
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Updates a board with a move.
 	 * 
 	 * Used in the minimax algorithm.
 	 * 
 	 * @param board
 	 * @param move
 	 * @return
 	 * @throws RemoteException
 	 */
 	private Figure[] performMove(Figure[] board, Move move) throws RemoteException {
 		Figure[] newBoard = board.clone();
 		
 		int fromIndex = move.getFrom();
 		int toIndex = move.getTo();
 		
 		// normal move
 		if(board[toIndex] == null) {
 			newBoard[toIndex] = board[fromIndex];
 			newBoard[fromIndex] = null;
 		}
 		// attack
 		else {
 			FigureKind fromKind = board[fromIndex].getKind();
 			FigureKind toKind = board[toIndex].getKind();
 			AttackResult attackResult = fromKind.attack(toKind);
 			
 			//this.updateDiscoveredStatistic(move);
 			
 			if(attackResult == AttackResult.WIN_AGAINST_FLAG || attackResult == AttackResult.WIN) {
 				newBoard[toIndex] = board[fromIndex];
 				newBoard[fromIndex] = null;
 			} else if(attackResult == AttackResult.LOOSE) {
 				newBoard[fromIndex] = null;
 			} else if(attackResult == AttackResult.LOOSE_AGAINST_TRAP) {
 				newBoard[fromIndex] = null;
 				newBoard[toIndex] = null;
 			} else if(attackResult == AttackResult.DRAW) {
 				FigureKind aiChoice = this.getFigureKindByStatistic();
 				FigureKind opponentChoice = BasicAi.randomChoice();
 				
 				Figure aiFigure = new Figure(aiChoice, this.player);
 				Figure opponentFigure = new Figure(opponentChoice, getOpponent());
 				
 				if(board[fromIndex].belongsTo(this.player)) {
 					newBoard[fromIndex] = aiFigure;
 					newBoard[toIndex] = opponentFigure;
 				} else {
 					newBoard[fromIndex] = opponentFigure;
 					newBoard[toIndex] = aiFigure;
 				}
 				
 				newBoard = performMove(newBoard, move);
 			}
 		}
 
 		return newBoard;
 	}
 	
 
 	/**
 	 * Returns the opponent player.
 	 * 
 	 * @return
 	 * @throws RemoteException
 	 */
 	private Player getOpponent() throws RemoteException {
 		return this.game.getOpponent(this.player);
 	}
 	
 	/**
 	 * Update statistic about discovered figures.
 	 * 
 	 * Is called when an attack happens. The last move is evaluated.
 	 * If the opponent figure is in the list of discovered figures,
 	 * nothing happens. Otherwise the type of the figure is evaluated
 	 * and the corresponding counter is incremented. Then the figure 
 	 * is added to the discovered list to prevent statistic updates
 	 * on future attacks.
 	 * 
 	 * @throws RemoteException
 	 */
 	private void updateDiscoveredStatistic(Move lastMove) throws RemoteException {
 		// get information about attack from last move
 		Figure[] oldBoard = lastMove.getOldField();
 		int indexFrom = lastMove.getFrom();
 		int indexTo = lastMove.getTo();
 		
 		if(this.lastAttackWasDrawn) {
 			if(oldBoard[indexFrom].belongsTo(getOpponent())) {
 				this.discoveredFigures.add(oldBoard[indexFrom]);
 			}
 			else {
 				this.discoveredFigures.add(oldBoard[indexTo]);
 			}
 			
 			this.lastAttackWasDrawn = false;
 			return;
 		}
 		
 		// figure out which kind the involved opponent figure has
 		Figure discoveredFigure;
 		FigureKind discoveredFigureKind;
 		
 		if(oldBoard[indexFrom].belongsTo(this.player)) {
 			discoveredFigure = oldBoard[indexTo];
 		} else {
 			discoveredFigure = oldBoard[indexFrom];
 		}
 		discoveredFigureKind = discoveredFigure.getKind();
 		
 		// find out whether this figure was already discovered
 		for(int i=0; i<this.discoveredFigures.size(); i++) {
 			// compare the discovered figure with the 
 			// already discovered figures by object address
 			if(discoveredFigure == this.discoveredFigures.get(i)) {
 				// leave search algorithm
 				return;
 			}
 		}
 		
 		this.discoveredFigures.add(discoveredFigure);
 		
 		// increment the type counter
 		if(discoveredFigureKind == FigureKind.ROCK) {
 			this.discoveredRocks += 1;
 		} else if(discoveredFigureKind == FigureKind.PAPER) {
 			this.discoveredPapers += 1;
 		} else if(discoveredFigureKind == FigureKind.SCISSORS) {
 			this.discoveredScissors += 1;
 		} else if(discoveredFigureKind == FigureKind.TRAP) {
 			this.discoveredTraps += 1;
 		}
 	}
 
 	private boolean gameIsLost(Figure[] board) {
 		boolean gameIsLost = true;
 		for(int i=0; i<board.length; i++) {
 			if(board[i] != null && board[i].belongsTo(getPlayer()) && board[i].getKind() == FigureKind.FLAG) {
 				gameIsLost = false;
 			}
 		}
 		return gameIsLost;
 	}
 	
 	private boolean gameIsDefinitlyDrawn(Figure[] board) throws RemoteException {
 		int opponentFigures = 0;
 		
 		for(int i=0; i<board.length; i++) {
 			if(board[i] != null) {
 				if(board[i].belongsTo(getPlayer()) && board[i].getKind().isMovable()) {
 					return false;
 				}
 				
 				if(board[i].belongsTo(getOpponent())) {
 					opponentFigures++;
 				}
 			}
 		}
 		
 		return (opponentFigures==1);
 	}
 	
 	private boolean gameIsDefinitlyWon(Figure[] board) throws RemoteException {
 		for(int i=0; i<board.length; i++) {
 			if(board[i] != null && board[i].belongsTo(getOpponent())) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 }
