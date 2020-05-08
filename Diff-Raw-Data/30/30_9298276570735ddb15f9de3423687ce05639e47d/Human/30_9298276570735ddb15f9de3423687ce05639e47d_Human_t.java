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
 package com.rogue.connectfour.player.type;
 
 import com.rogue.connectfour.ConnectFour;
 import com.rogue.connectfour.board.Piece;
 import com.rogue.connectfour.player.Player;
 import java.util.Scanner;
 
 /**
  * Human AI for {@link ConnectFour}. Moves are determined by a human player.
  * 
  * @since 1.0.0
  * @author Spencer Alderman
  * @version 1.0.0
  */
 public class Human implements Player {
     
     private final ConnectFour project;
     private final Piece ident;
     
     /**
      * Constructor for {@link Human}. Sets the {@link ConnectFour} instance and 
      * the {@link Piece} to use
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @param project
      * @param ident The {@link Piece} for this {@link Player} to use.
      */
     public Human(ConnectFour project, Piece ident) {
         this.project = project;
         this.ident = ident;
     }
 
     /**
      * Asks for input for a column, and then plays it
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return The next move to play
      */
     @Override
     public int nextMove() {
         Scanner scan = new Scanner(System.in);
        System.out.print("Player " + this.ident.toString() + ": Enter the column to drop your piece (-1 to quit): ");
         return scan.nextInt();
     }
     
     /**
      * Returns "human", or the {@link Player} type.
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return The {@link Player} type
      */
     @Override
     public String getType() {
         return "human";
     }
     
     /**
      * Returns the {@link Piece} in use by this {@link Player}
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return The {@link Piece} in use
      */
     @Override
     public Piece getIdent() {
         return this.ident;
     }
 }
