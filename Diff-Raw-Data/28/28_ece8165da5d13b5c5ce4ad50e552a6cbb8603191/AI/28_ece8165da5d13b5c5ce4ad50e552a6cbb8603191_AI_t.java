 ﻿package quoridor;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import quoridor.Move.MoveType;
 import util.Pair;
 import util.Two;
 
 
 /**
  * AI is called to generate a move based on game state and a smartness of a move.
  * 
  * <h2>Goals</h2>
  * <ul>
  * <li>Generates random, naive, or pro moves.</li>
  * </ul> 
  * 
  */
 
 public class AI {
 
 	Game game;
 
 	/**
 	 * Constructor for AI. It requireds type Game to be passed in.
 	 * @param game the game AI is required for.
 	 */
 	public AI(Game game){
 		this.game = game;
 	}
 
 	/**
 	 * Creates a move based on the required intelligence of the AI
 	 * @return a Move
 	 */
 	public Move createMove(){
 		Move move = null;
 
 		if (game.myTurn().level().equals("random")) {
 			move = randomMove();
 		} else if (game.myTurn().level().equals("naive")) {
 			move = naiveMove();
 		} else if (game.myTurn().level().equals("pro")) {
 			move = proMove();
 		}
 
 		return move;
 	}
 
 	/**
 	 * Makes a random move out of all the possible moves
 	 * @return a Move
 	 */
 	private Move randomMove() {
 		ArrayList<Move> possibleMoves = findPossibleMoves(game);
 		Random randomGenerator = new Random();
 		Move move = possibleMoves.get(randomGenerator.nextInt(possibleMoves.size()));
 
 		return move;
 	}
 
 	/**
 	 * Makes a naive move based on the current state of the game using a heuristic
 	 * @return
 	 */
 	private Move naiveMove() {
 		ArrayList<Move> possibleMoves = findPossibleMoves(game);
 		Move move = null;
 		int myValue = game.shortestPath(game.myTurn()).size();
 		int playerShortestPath = game.shortestPath(game.players().other(game.myTurn())).size();
 
 		int highestValue = (int) Double.NEGATIVE_INFINITY;
 		int index = 0;
 		Random random = new Random();
 		//Find best move		
 
 		if (myValue >= playerShortestPath - 1 && game.myTurn().wallsLeft() != 0 
 				&& game.moves.size() > random.nextInt(4)) {
 			for (int i = 0; i < possibleMoves.size(); i++) {
 				int value = 0;
 				if (possibleMoves.get(i).direction() == MoveType.HORIZONTAL 
 						|| possibleMoves.get(i).direction() == MoveType.VERTICAL) {
 
 					Game tempGame = createTempGame(game.moves);
 
 					//add wall
 					tempGame.move(possibleMoves.get(i), tempGame.myTurn);
 
 					int tempShortest = tempGame.shortestPath(tempGame.myTurn).size();
 					int myTempShortest = tempGame.shortestPath(tempGame.players.other(tempGame.myTurn)).size();
 					
 					//calculate value of each move
 					value = (tempGame.shortestPath(tempGame.myTurn).size())
 					- (myTempShortest - myValue)
 					- wallDistance(possibleMoves.get(i));
 
 					if (playerShortestPath == tempShortest) {
 						if (myValue == myTempShortest) {
 							value = value * 1/2 - 20;
 						} 
 					}
 
 					if (isAdjacent(possibleMoves.get(i))) {
 						value++;
 					}
 
 					if (value > highestValue) {
 						highestValue = value;
 						index = i;
 					}
 				}
 				move = possibleMoves.get(index);
 			}	
 		} else {
 			move = game.shortestPath(game.myTurn()).get(1);
 			if (!game.isValid(move, game.myTurn())) {
 				move = game.shortestPath(game.myTurn()).get(2);
 				if (!game.isValid(move, game.myTurn())) {
 					//find best move by comparing shortest path 
 					Game tempGame = createTempGame(game.moves);
 					Game tempGameTwo = createTempGame(game.moves);
 
 					Point p = game.myTurn().pawn();
 					Move tempMove = new Move(p.x() + 1, p.y(), MoveType.PAWN);
 					Move tempMoveTwo = new Move(p.x() - 1, p.y(), MoveType.PAWN);
 
 					if (tempGame.isValid(tempMove, tempGame.myTurn())) {
 						move = tempMove;
 						if (tempGame.isValid(tempMoveTwo, tempGame.myTurn())) {
 							tempGame.move(tempMove, game.myTurn());
 							tempGameTwo.move(tempMoveTwo, game.myTurn());
 
 							if (tempGame.shortestPath(tempGame.players().other(tempGame.myTurn())).size() <
 									tempGameTwo.shortestPath(tempGameTwo.players().other(tempGameTwo.myTurn())).size())	{
 								move = tempMove;
 							} else {
 								move = tempMoveTwo;
 							}
 						}
 					} else if (tempGame.isValid(tempMoveTwo, tempGame.myTurn())) {
 						move = tempMoveTwo;
 					}
 				}
 			}
 		}
 
 		return move;
 	}	
 
 	/**
 	 * Makes a move with alpha-beta pruning look ahead based on a heuristic
 	 * @return a Move
 	 */
 	private Move proMove() {
 		Pair<Integer, Move> result = maxValue(game.moves, 0, (int) Double.NEGATIVE_INFINITY, (int) Double.POSITIVE_INFINITY);
 		return result._2();
 	}
 
 	private int desiredDepth = 3;
 
 	/**
 	 * The first part of the alpha-beta pruning 
 	 * @param moves a list of all the moves made so far
 	 * @param currentSearchDepth the current depth of the search
 	 * @param alphaMax the max value of alpha
 	 * @param betaMin the minimum value of beta
 	 * @return a Pair of argument, the alpha value of the move and the move 
 	 */
 	private Pair<Integer, Move> maxValue(LinkedList<Move> moves, int currentSearchDepth, int alphaMax, int betaMin) {
 		Move bestMove = null;
 		int value;
 		if (currentSearchDepth == desiredDepth || isGoalState(moves)) {
 
 			//****************************** Needs implementing ******************************//
 			/**/ 	Random random = new Random();											/**/
 			/**/	ArrayList<Move> m = findPossibleMoves(game);							/**/
 			/**/	return Pair.pair(random.nextInt(1000), m.get(random.nextInt(m.size())));/**/
 			/**/    //return Heuristic(state), any move										/**/
 			//********************************************************************************//
 		}
 
 		Game tempGame = createTempGame(moves);
 		ArrayList<Move> moveList = findPossibleMoves(tempGame);
 
 		bestMove = moveList.get(0);
 
 		for (int i = 0; i < moveList.size(); i++) {
 			Game tempGameTwo = createTempGame(moves);
 			tempGameTwo.move(moveList.get(i), tempGame.myTurn());
 
 			value = minValue(tempGameTwo.moves, currentSearchDepth + 1, alphaMax, betaMin);
 			if (value > alphaMax) {
 				alphaMax = value;
 				bestMove = moveList.get(i);
 			}
 
 			if (alphaMax >= betaMin) {				
 				return Pair.pair(alphaMax, bestMove);
 			}
 		}
 
 		return Pair.pair(alphaMax, bestMove);
 	}
 
 	/**
 	 * The second part of the alpha-beta pruning 
 	 * @param moves a list of all the moves made so far
 	 * @param currentSearchDepth the current depth of the search
 	 * @param alphaMax the max value of alpha
 	 * @param betaMin the minimum value of beta
 	 * @return the minimum value of beta of type int
 	 */
 	private int minValue(LinkedList<Move> moves, int currentSearchDepth, int alphaMax, int betaMin) {
 		Pair<Integer, Move> value = null;
 		if (currentSearchDepth == desiredDepth || isGoalState(moves)) {
 			//********** Needs implementing **********//
 			/**/ //return heuristic(state)			/**/
 			/**/ Random random = new Random();		/**/
 			/**/									/**/
 			/**/ return random.nextInt(1000);		/**/
 			//****************************************//
 		}
 
 		Game tempGame = createTempGame(moves);
 		ArrayList<Move> moveList = findPossibleMoves(tempGame);
 		for (int i = 0; i < moveList.size(); i++) {
 			Game tempGameTwo = createTempGame(moves);
 			tempGameTwo.move(moveList.get(i), tempGame.myTurn());
 
 			value = maxValue(tempGameTwo.moves, currentSearchDepth + 1, alphaMax, betaMin);
 			betaMin = Math.min(value._1, betaMin);
 			if (alphaMax >= betaMin) {
 				return betaMin;
 			}
 		}		
 
 		return betaMin;
 	}
 
 	/**
	 * The heuristic for alpha-beta pruning
	 * @param moves a list of all the moves played so far
	 * @return an int, the heuristic value of a move
	 */
	private int heuristic(LinkedList<Move> moves) {
		int value = 0;
		
		
		return value;
	}
	/**
 	 * Checks if the state of the moves is a winning game state
 	 * @param moves the list of all moves made so far
 	 * @return boolean true if it is a winning state, false if not
 	 */
 	private boolean isGoalState(LinkedList<Move> moves) {
		
		if (!moves.isEmpty()) {
			for (int i = 0; i < 9; i++) {
				if ((moves.getLast().coord().x() == i && moves.getLast().coord().y() == 1) 
						|| (moves.getLast().coord().x() == i && moves.getLast().coord().y() == 9)) {
					return true;
				}
 			}
 		}
		
 		return false;
 	}
 
 	/**
 	 * Checks if a wall placement is adjacent to a player
 	 * @param move the wall placement
 	 * @return true if it is adjacent, false if not
 	 */
 	private boolean isAdjacent(Move move) {
 		Point player = game.players().other(game.myTurn()).pawn();
 		int direction;
 
 		if (game.myTurn().equals(game.players()._1())) {
 			direction = 1;
 		} else {
 			direction = -1;
 		}
 
 		if (direction == -1) {
 			if (move.coord().x() == player.x() && move.coord().y() == player.y() 
 					||	(move.coord().x() == player.x() - 1 && move.coord().y() == player.y()
 							&& move.direction() == MoveType.HORIZONTAL)
 							||	(move.coord().x() == player.x() && move.coord().y() == player.y() - 1 
 									&& move.direction() == MoveType.VERTICAL)
 									||	(move.coord().x() == player.x() + 1 && move.coord().y() == player.y() 
 											&& move.direction() == MoveType.VERTICAL)
 											||	(move.coord().x() == player.x() + 1 && move.coord().y() == player.y() - 1
 													&& move.direction() == MoveType.VERTICAL)) {
 
 				return true;
 			}
 		} else {
 			if ((move.coord().x() == player.x() && move.coord().y == player.y() - 1
 					&& move.direction() == MoveType.VERTICAL)
 					||	(move.coord().x() == player.x() && move.coord().y == player.y()
 							&& move.direction() == MoveType.VERTICAL)
 							||	(move.coord().x() == player.x() + 1 && move.coord().y == player.y() - 1
 									&& move.direction() == MoveType.VERTICAL)
 									||	(move.coord().x() == player.x() + 1 && move.coord().y == player.y()
 											&& move.direction() == MoveType.VERTICAL)
 											||	(move.coord().x() == player.x() - 1 && move.coord().y() == player.y()
 													&& move.direction() == MoveType.HORIZONTAL)
 													||	(move.coord().x() == player.x() && move.coord().y() == player.y() + 1
 															&& move.direction() == MoveType.HORIZONTAL)) {
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Finds the distance of the wall placement to the opponent
 	 * @param move the placement of the wall
 	 * @return the int value of the straight line distance
 	 */
 	private int wallDistance(Move move) {
 		Point otherPlayer = game.players().other(game.myTurn()).pawn();
 
 		return (int) Math.sqrt(Math.pow((otherPlayer.x() - move.coord().x()), 2)
 				+ Math.pow((otherPlayer.y() - move.coord().y()), 2));
 	}
 
 	/**
 	 * Creates a copy of a game with the exact game state
 	 * @param moves the list of all moves made so far
 	 * @return a Game
 	 */
 	private Game createTempGame(LinkedList<Move> moves) {
 		Player tempPl1 = new Human("Player 1");
 		Player tempPl2 = new Human("Player 2");
 		Game tempGame = new Game(Two.two(tempPl1, tempPl2));
 
 		tempGame.initGame(null);
 		for (int j = 0; j < moves.size(); j++) {
 			tempGame.move(moves.get(j), tempGame.myTurn());
 		}
 
 		return tempGame;
 	}
 
 	/**
 	 * Finds all the possible moves of the current player in a game
 	 * @param g the game - type Game
 	 * @return ArrayList of moves
 	 */
 	private ArrayList<Move> findPossibleMoves(Game g) {
 		ArrayList<Move> possibleMoves = new ArrayList<Move>();
 		ArrayList<Move> checkList = new ArrayList<Move>();
 		Point current = g.myTurn.pawn();
 
 		checkList.add(new Move(current.x() + 1, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x() + 2, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() + 1, MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() + 2, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x() - 2, current.y(), MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() - 1, MoveType.PAWN));
 		checkList.add(new Move(current.x(), current.y() - 2, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y() - 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() + 1, current.y() - 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y() + 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() + 1, current.y() + 1, MoveType.PAWN));
 		checkList.add(new Move(current.x() - 1, current.y() + 1, MoveType.PAWN));
 
 		//add walls to checklist
 		if (g.myTurn().wallsLeft() > 0) {
 			for (int i = 1; i < 9; i++) {
 				for (int j = 1; j < 9; j++) {
 					checkList.add(new Move(j, i, MoveType.HORIZONTAL));
 					checkList.add(new Move(j, i, MoveType.VERTICAL));
 				}
 			}
 		}
 
 		for (int i = 0; i < checkList.size(); i++) {
 			if (g.isValid(checkList.get(i), g.myTurn())) {
 				possibleMoves.add(checkList.get(i));
 			}
 		}		
 
 		return possibleMoves;
 	}
 
 }
