 package edu.wctc.java.demo.tictactoe.domain;
 
 import java.awt.Color;
 import java.util.Random;
 
 /**
  * This class is the engine that drives the game logic. Its collaborators
  * are the Tile objects (Xs and 0s) and Rail objects which represent winning
  * combinations. 
  * <P>
  * The GameEngine provides a computer opponent for any "X" player.
  * Currently the human player is always "X" and the GameEngine will only
  * support a computer opponent.
  * 
  * @author   Jim Lombardo, Lead Java Instructor, jlombardo@wctc.edu
  * @version  1.08
  */
 public class GameEngine {
     private static final int ROW1 = 0;
     private static final int ROW2 = 1;
     private static final int ROW3 = 2;
     private static final int COL1 = 3;
     private static final int COL2 = 4;
     private static final int COL3 = 5;
     private static final int DIAGL = 6;
     private static final int DIAGR = 7;
     private static final int R1C1 = 0;
     private static final int R1C2 = 1;
     private static final int R1C3 = 2;
     private static final int R2C1 = 3;
     private static final int R2C2 = 4;
     private static final int R2C3 = 5;
     private static final int R3C1 = 6;
     private static final int R3C2 = 7;
     private static final int R3C3 = 8;
     
     private int xWins = 0;
     private int oWins = 0;
     private int draws = 0;
     
     private Random rand = new Random(System.nanoTime());
     private Tile[] tiles;
     private int tilesPlayed = 0;
     private Rail[] rails;
     private String winningPlayer = "";
     
     /**
      * Constructs a GameEngine with nine (9) supplied Tile objects that are
      * are born unmarked. Possible winning combinations of three (3) tiles are
      * then constructed as Rail objects. This makes checks for wins and draws
      * easy by comparing the state of the Rails.
      * 
      * @param tiles - an array of Tile objects (custom JButton objects) 
      * representing each square space (9 total) on the game board.
      */
    public GameEngine(final Tile[] tiles) {
         this.tiles = tiles;
         tilesPlayed = 0;
         initRails();
     }
     
     // Determines tile combinations that form winning rails.
     private final void initRails() {
         rails = new Rail[8];
         rails[ROW1] = new Rail(
             new Tile[] {
                 tiles[R1C1], tiles[R1C2], tiles[R1C3]
             }
         );
         rails[ROW2] = new Rail(
             new Tile[] {
                 tiles[R2C1], tiles[R2C2], tiles[R2C3]
             }
         );
         rails[ROW3] = new Rail(
             new Tile[] {
                 tiles[R3C1], tiles[R3C2], tiles[R3C3]
             }
         );
         rails[COL1] = new Rail(
             new Tile[] {
                 tiles[R1C1], tiles[R2C1], tiles[R3C1]
             }
         );
         rails[COL2] = new Rail(
             new Tile[] {
                 tiles[R1C2], tiles[R2C2], tiles[R3C2]
             }
         );
         rails[COL3] = new Rail(
             new Tile[] {
                 tiles[R1C3], tiles[R2C3], tiles[R3C3]
             }
         );
         rails[DIAGL] = new Rail(
             new Tile[] {
                 tiles[R1C1], tiles[R2C2], tiles[R3C3]
             }
         );
         rails[DIAGR] = new Rail(
             new Tile[] {
                 tiles[R1C3], tiles[R2C2], tiles[R3C1]
             }
         );
     }
     
     /*
      * Selects a computer move using a simple AI routine. First tries to
      * find a winning move, then if none available, tries to block a winning
      * opportunity by the player "X", and if that is not available, just
      * finds a random, empty tile.
      */
     public final Tile selectComputerMove() {
         Tile tile = null;
         int count = 0;
         String tileName = null;
         boolean continueLoop = true;
         
         // First try to find a winning move
         for (Rail rail : rails) {
             for (Tile t : rail.getTiles()) {
                 if (t.getText().equals("0")) count++;
             }
             if (count == 2) {
                 for (Tile t : rail.getTiles()) {
                     if (t.getText().equals("")) {
                         tileName = t.getName();
                         return t;
                     }
                 }
             }
             count = 0;   
         }
         
         // If none found, try to block "X"
          for (Rail rail : rails) {
             for (Tile t : rail.getTiles()) {
                 if (t.getText().equals("X")) count++;
             }
             if (count == 2) {
                 for (Tile t : rail.getTiles()) {
                     if (t.getText().equals("")) {
                         tileName = t.getName();
                         return t;
                     }
                 }
             }
             count = 0;
         }
          
         // If no blocking move, try to play the center which gives "O"
         // a statistical advantage
         if (!tiles[R2C2].isSelected()) {
             return tiles[R2C2];
         }
        
        
         // If center move is not available, try to find an open corner tile, 
         // which might set up an winning combination
         if (!tiles[R1C1].isSelected()) {
             return tiles[R1C1];
         } else if (!tiles[R3C3].isSelected()) {
             return tiles[R3C3];
         } else if (!tiles[R3C1].isSelected()) {
             return tiles[R3C1];
         } else if (!tiles[R1C3].isSelected()) {
             return tiles[R1C3];
         }
         
         // If none found just pick an empty tile at random
         if(tileName == null) {
             int row = rand.nextInt(3) + 1;
             int col = rand.nextInt(3) + 1;
             tileName = "r" + row + "c" + col;
             for (Tile btn: tiles) {
                 if (btn.getName().equals(tileName)) {
                     tile = btn;
                     break;
                 }
             }
         }
         
         return tile;
     }
     
     // Be sure to call this before checkForWin
     public final boolean checkForDraw() {
         boolean result = false;
         String player = "";
         
         for (Rail rail : rails) {
             if (rail.isWinner()) {
                 return false;
             }
         }
         
         if (tilesPlayed == 9) {
             draws++;
 //            window.getStatusMsg().setText(DRAW_MSG);  
             result = true;
         }
         
         return result;
     }
     
     // Be sure to call this only after checkForDraw
     public final boolean checkForWin() {
         boolean result = false;
         String player = "";
         
         for (Rail rail : rails) {
             if (rail.isWinner()) {
                 result = true;
                 for(Tile tile : rail.getTiles()) {
                     tile.setBackground(Color.GREEN);
                     player = tile.getText();
                 }
                 if (player.equals("X")) {
                     winningPlayer = "X";
                     this.xWins++;
                 } else if (player.equals("0")) {
                     winningPlayer = "0";
                     this.oWins++;
                 }
                 break;
             }
         }
         
         return result;
     }
 
     public final int getTilesPlayed() {
         return tilesPlayed;
     }
 
     public final void incrementTilesPlayed() {
         this.tilesPlayed++;
     }
 
     public final String getWinningPlayer() {
         return winningPlayer;
     }
 
     public final void setWinningPlayer(final String winningPlayer) {
         this.winningPlayer = winningPlayer;
     }
 
     public final int getDraws() {
         return draws;
     }
 
     public final int getoWins() {
         return oWins;
     }
 
     public final int getxWins() {
         return xWins;
     }
 
     public final Tile[] getTiles() {
         return tiles;
     }
 
 }
