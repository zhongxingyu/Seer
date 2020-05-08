 /*
  * Copyright (C) 2013 Spencer Alderman
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.rogue.connectfour;
 
 import com.rogue.connectfour.board.Board;
 import com.rogue.connectfour.board.Node;
 import com.rogue.connectfour.board.Piece;
 import com.rogue.connectfour.game.Game;
 import com.rogue.connectfour.player.PlayerManager;
 
 /**
  * Main class for the project
  *
  * @since 1.0.0
  * @author Spencer Alderman
  * @version 1.0.0
  */
 public class ConnectFour {
     
     private final Board board;
     private final PlayerManager manager;
     private final Game game;
     
     /**
      * Game constructor. Initializes variables to be used from a global
      * perspective.
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @param xType Player type for X player
      * @param oType Player type for O player
      * @param rows Number of rows to play the game with
      * @param columns Number of columns to play the game with
      */
     public ConnectFour(String xType, String oType, int rows, int columns) {
         this.board = new Board(this, rows, columns);
         this.manager = new PlayerManager(this, xType, oType);
         this.game = new Game(this, rows * columns);
         this.init();
     }
     
     /**
      * Private void to avoid calls within the main class constructor
      * 
      * @since 1.0.0
      * @version 1.0.0
      */
     private void init() {
         char c = this.game.start();
         if (c == ' ') {
             System.out.println("Its a tie, no one wins");
         } else {
             System.out.println(c + " won " + this.board.getWinningMove());
         }
        this.printBoard();
     }
     
     /**
      * Returns the board module for the project
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return The {@link Board} module
      */
     public Board getBoard() {
         return this.board;
     }
     
     /**
      * Gets the player manager for the project
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return The {@link PlayerManager} module
      */
     public PlayerManager getPlayerManager() {
         return this.manager;
     }
     
     /**
      * Prints the layout of the provided {@link Board}
      * 
      * @since 1.0.0
      * @version 1.0.0
      */
     public synchronized void printBoard() {
         System.out.println();
         final Node<Piece>[][] grid;
         synchronized (grid = this.board.getGrid()) {
             StringBuilder back = new StringBuilder();
             for (int i = 0; i < grid.length; i++) {
                 StringBuilder row = new StringBuilder("|");
                 for (int w = 0; w < grid[i].length; w++) {
                     row.append(grid[i][w].getData().toString()).append("|");
                 }
                 back.append(row).append('\n');
             }
             StringBuilder bottom = new StringBuilder("+");
             for (int i = 0; i < grid[0].length; i++) {
                 bottom.append("-+");
             }
             System.out.println(back.append(bottom.toString()).toString());
         }
         System.out.println();
     }
     
 }
