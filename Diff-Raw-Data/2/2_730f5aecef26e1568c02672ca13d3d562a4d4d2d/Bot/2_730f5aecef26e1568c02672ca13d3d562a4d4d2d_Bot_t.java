 /*
  * Copyright (c) 2013 Elder Research, Inc.
  * All rights reserved. 
  */
 package ai;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import board.Board;
 
 /**
  * This class implements a bot that can play Letterpress.
  * 
  * @author William Proffitt
  * @since Aug 2, 2013
  */
 public class Bot implements Player {
 
 	private Board board;
 
 	public Bot(Board board) {
 		this.board = board;
 	}
 
 	public int[][] provideMove(char[][] letterBoard, char[][] colorBoard,
 			HashSet<String> usedWords, char turn) {
 		
 		return chooseMove(turn);
 	}
 
 	/**
 	 * 
 	 * @param lowerBound
 	 * @param upperBound
 	 * @return Random integer between lowerBound and upperBound inclusive
 	 */
 	public int getRandomIntInRange(int lowerBound, int upperBound) {
 		if (lowerBound > upperBound) {
 			int tempLowerBound = lowerBound;
 			int tempUpperBound = upperBound;
 			lowerBound = tempUpperBound;
 			upperBound = tempLowerBound;
 		}
 
 		upperBound++;
 		int differenceInBounds = upperBound - lowerBound;
 		return (int) (Math.floor(Math.random() * differenceInBounds) + lowerBound);
 	}
 
 	private int count(char[][] board, char target) {
 		int count = 0;
 		for (int i = 0; i < 5; ++i) {
 			for (int j = 0; j < 5; ++j) {
 				if (target == board[i][j]) {
 					++count;
 				}
 			}
 		}
 		return count;
 	}
 
 	private int[][] chooseMove(char color) {
 		System.out.println("[BOT] I am choosing a move.");
 		int max = 0;
 		int[][] bestmove = new int[0][0];
 		char[][] simBoard;
 		for (int[][] move : board.getPlayableWords()) {
 			if (!board.isValidWord(board.whatWordDoesThisPlayMake(move))) {
 				continue;
 			}
 			simBoard = simulateMove(move, color);
 			
 			if (weWin(simBoard, color)) {
 				bestmove = move;
 				break;
 			}
 			
 			int nextWordScore = totalDarkMyColor(simBoard, color);
 			if (nextWordScore > max) {
 				bestmove = move;
 				max = nextWordScore;
 			}
 		}
 		if (bestmove.length == 0) {
 			System.out.println("[BOT] Error: No Playable Moves");
 			try {Thread.sleep(1000);} catch (InterruptedException e) {}
 		}
 		return bestmove;
 	}
 
 	private char[][] simulateMove(int[][] move, char color) {
 		char[][] resultingBoard;
 		resultingBoard = Board.colorTiles(board.getColorBoard(), move, color);
 		return resultingBoard;
 
 	}
 
 	private boolean weWin(char[][] board, char color) {
 		
		int nulls = count(board, ' ');
 		
 		if (nulls == 0) {
 			int reds = count(board, color) + count(board, Character.toUpperCase(color));
 			if (reds > 12) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private int totalDarkMyColor(char[][] targetBoard, char color) {
 		char target = Character.toUpperCase(color);
 		int score = count(targetBoard, target);
 		return score;
 
 	}
 
 }
