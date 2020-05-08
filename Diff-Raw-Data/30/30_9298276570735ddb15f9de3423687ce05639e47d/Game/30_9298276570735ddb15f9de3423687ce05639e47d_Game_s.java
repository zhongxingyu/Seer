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
 package com.rogue.connectfour.game;
 
 import com.rogue.connectfour.ConnectFour;
 import com.rogue.connectfour.board.FullColumnException;
 import com.rogue.connectfour.player.Player;
 import com.rogue.connectfour.player.PlayerManager;
 import com.rogue.connectfour.player.type.Human;
 
 /**
  * Game class for {@link ConnectFour}. Manages a single game, and returns a
  * winner afterwards
  *
  * @since 1.0.0
  * @author Spencer Alderman
  * @version 1.0.0
  */
 public class Game {
 
     private final ConnectFour project;
     private char winner = ' ';
     private int turn = 0;
 
     /**
      * Constructor for {@link Game}. Sets the {@link ConnectFour} project
      * instance and the maximum number of turns
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @param project The {@link ConnectFour} instance
      * @param maxMoves The maximum number of turns before the game is over
      */
     public Game(ConnectFour project, int maxMoves) {
         this.project = project;
         this.turn = maxMoves;
     }
 
     /**
      * Starts a game
      *
      * @since 1.0.0
      * @version 1.0.0
      *
      * @return The winner of the game
      */
     public char start() {
         while (this.turn != 0) {
             this.project.printBoard();
             PlayerManager pm = this.project.getPlayerManager();
             Player current = pm.getPlayer();
             System.out.println(current.getType() + " player " + current.getIdent().toString() + " moving...");
             boolean valid = false;
             getInput: // Inner loop for getting and verifying input
             while (!valid) {
                 int i = current.nextMove(); // Get the next move
                 if (i == -1) {
                     System.out.println(current.getIdent().toString() + " quits the game");
                    System.exit(0);
                 }
                 boolean win;
                 try {
                     if (i < 0 || i > this.project.getBoard().maxWidth) {
                         throw new FullColumnException();
                     }
                     win = this.project.getBoard().play(current.getIdent(), i);
                     valid = true; // If no FCE, then the move was valid
                 } catch (FullColumnException ex) {
                     if (current instanceof Human) {
                         System.out.println("invalid column: " + i);
                     }
                     continue getInput; // Repeat the getInput loop
                 }
                 if (win) { // Returns the winning char
                     return current.getIdent().toString().toCharArray()[0];
                 }
             }
             this.turn--; // Tick tock, turns decrementing
         }
         return ' '; // If turns run out, no winner is declared
     }
 }
