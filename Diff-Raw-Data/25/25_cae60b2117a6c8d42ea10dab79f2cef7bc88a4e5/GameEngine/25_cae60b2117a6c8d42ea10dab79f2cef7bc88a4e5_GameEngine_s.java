 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.common.Coordinate;
 import edu.victone.scrabblah.logic.common.Tile;
 import edu.victone.scrabblah.logic.common.Word;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 4:40 PM
  */
 public class GameEngine { //rules, etc
 
     public static boolean isLegalState(GameState gameState) {
        if (Dictionary.isLoaded()) {
             gameState.setStatusMessage("Dictionary not loaded.");
             return false;
         }
 
         //if first turn is a pass or a swap
         if (gameState.getTurn() == 1 && gameState.getGameBoard().getWordList().size() == 0) {
             return true;
         }
 
         if (gameState.getPlayerList() == null) {
             gameState.setStatusMessage("There are no players.");
             return false;
         }
 
         if (!isLegalGameBoard(gameState.getGameBoard())) {
             gameState.setStatusMessage("Illegal tile placement.");
             return false;
         }
 
         //if all of these tests have passed, then we are golden.
         return true;
     }
 
     public static boolean centerCellUnoccupied(GameBoard gameBoard) {
         return gameBoard.getCellAt(GameBoard.CENTER).isEmpty();
     }
 
     public static boolean isLegalGameBoard(GameBoard gameBoard) { //terrible use of repeated code
         //if first word played is a one-letter word
         if (gameBoard.getNumOccupiedCells() == 1) {
             //gameState.setStatusMessage("Single-letter words are not allowed.");
             return false;
         }
 
         if (centerCellUnoccupied(gameBoard)) {
             System.out.println("not legal: center cell unoccupied");
             return false;
         }
 
         //are all letters contiguous?
         if (!areLettersContiguous(gameBoard)) {
             //gameState.setStatusMessage("Invalid tile placement.");
             return false;
         }
 
         //ensure all the words on the board are words in the dictionary
         int n = indexOfBadWord(gameBoard.getWordList());
         if (n != -1) {
             //String sDisplay = gameBoard.getWordList().get(n).toString().substring(0, 1).toUpperCase()
             //        + gameBoard.getWordList().get(n).toString().substring(1);
             //gameState.setStatusMessage(sDisplay + " is not a valid word.");
             return false;
         }
 
         //todo: remove this GameEngine debug code
         System.out.println("words size: " + gameBoard.getWordList().size());
         for (Word word : gameBoard.getWordList()) {
             System.out.println("words from board: " + word);
         }
         return true;
     }
 
     private static boolean areLettersContiguous(GameBoard gameBoard) {
         ArrayList<BoardCell> neighbors;
         BoardCell boardCell;
         Coordinate coord;
         for (int y = 0; y < 15; y++) {
             for (int x = 0; x < 15; x++) {
                 coord = new Coordinate(x, y);
                 boardCell = gameBoard.getCellAt(coord);
                 if (!boardCell.isEmpty()) {
                     neighbors = gameBoard.getCellNeighbors(coord);
                     //all tiles must have at least one tile neighbor
                     if ((neighbors.get(0) == null || neighbors.get(0).isEmpty()) &&
                             (neighbors.get(1) == null || neighbors.get(1).isEmpty()) &&
                             (neighbors.get(2) == null || neighbors.get(2).isEmpty()) &&
                             (neighbors.get(3) == null || neighbors.get(3).isEmpty())) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     public static int indexOfBadWord(ArrayList<Word> words) {
         //returns the index of the first Word in the list that
         //is not in the dictionary,
         //or -1 if all words are in the dictionary
         for (Word w : words) {
             if (!Dictionary.contains(w.getString())) {
                 return words.indexOf(w);
             }
         }
         return -1;
     }
 
     public static int computeScore(GameBoard oldBoard, GameBoard currentBoard) {
         int score = 0;
         if (oldBoard == null) { //first turn case
             oldBoard = new GameBoard();
         }
 
         //get words that are on cb but not ob.
         ArrayList<Word> newWords = currentBoard.getWordList();
         newWords.removeAll(oldBoard.getWordList());
 
         for (Word w : newWords) {
             score += computeWordScore(w, currentBoard);
         }
         return score;
     }
 
     private static int computeWordScore(Word w, GameBoard gameBoard) {
         int wordMultiplier = 1;
         int wordScore = 0;
         int x = w.getHead().getX();
         int y = w.getHead().getY();
 
         char[] charArr = w.getString().toCharArray();
         for (int i = 0; i < charArr.length; i++) {
             int tileMultiplier = 1;
 
             BoardCell boardCell = (w.isHorizontal() ? gameBoard.getCellAt(x + i, y) : gameBoard.getCellAt(x, y + i));
 
             //BoardCell boardCell = gameBoard.getCellAt(new Coordinate(
             //      (w.isHorizontal() ? x + i : x), (w.isHorizontal() ? y : y + i)));
             int multiplier = boardCell.getMultiplier();
 
             if (boardCell.isWordMultiplier()) {
                 wordMultiplier *= multiplier;
             } else {
                 tileMultiplier = multiplier;
             }
             wordScore += (Tile.getValue(charArr[i]) * tileMultiplier);
         }
         return wordScore * wordMultiplier;
     }
 }
