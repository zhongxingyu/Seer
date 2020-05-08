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
   //todo: eliminate this class by moving the methods to appropriate class files
 
   public static boolean isLegalState(GameState gameState) {
     if (!Dictionary.isLoaded()) {
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
     return gameBoard.getCellAt(Coordinate.CENTER).isEmpty();
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
       if (!Dictionary.contains(w.getWord())) {
         return words.indexOf(w);
       }
     }
     return -1;
   }
 
   public static int computeScore(GameBoard oldBoard, GameBoard newBoard) {
     int score = 0;
     if (oldBoard == null) { //first turn case
       System.out.println("never happens...?");
       oldBoard = new GameBoard();
     }
 
     //get words that are on nb but not ob.
     ArrayList<Word> wordsToScore = newBoard.getWordList();
     System.out.println("word list before removal: " + wordsToScore);
 
     wordsToScore.removeAll(oldBoard.getWordList());
     System.out.println("word list after removal: " + wordsToScore);
 
     for (Word w : wordsToScore) {
       System.out.println("computing the score for " + w);
 
       score += computeWordScore(w, new GameBoard());
     }
 
     if (newBoard.getNumOccupiedCells() - oldBoard.getNumOccupiedCells() == 7) {
       score += 50; //used all your tiles
     }
 
     return score;
   }
 
   public static int computeWordScore(Word w, GameBoard gameBoard) {
     int wordMultiplier = 1;
     int wordScore = 0;
     int x = w.getHead().getX();
     int y = w.getHead().getY();
 
    char[] wordAsArray = w.getWord().toCharArray();
    for (int i = 0; i < wordAsArray.length; i++) {
       int tileMultiplier = 1;
       Coordinate c = w.isHorizontal() ? new Coordinate(x + i, y) : new Coordinate(x, y + i);
       BoardCell boardCell = gameBoard.getCellAt(c);
 
       int multiplier = boardCell.getMultiplier();
 
       if (boardCell.isWordMultiplier()) {
         wordMultiplier *= multiplier;
       } else {
         tileMultiplier = multiplier;
       }
 
      wordScore += (Tile.getValue(wordAsArray[i]) * (boardCell.isLocked() ? 1 : tileMultiplier) );
     }
 
     return wordScore * wordMultiplier;
   }
 }
