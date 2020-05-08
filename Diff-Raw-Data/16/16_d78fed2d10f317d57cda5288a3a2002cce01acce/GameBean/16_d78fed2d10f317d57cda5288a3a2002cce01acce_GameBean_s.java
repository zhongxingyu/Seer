 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.examples.tictactoe;
 
 import java.io.Serializable;
 import java.util.Random;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.model.SelectItem;
 
 /**
  * The main managed bean. It stores the playing area, information about players and game logic.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean
 @SessionScoped
 public class GameBean implements Serializable {
 
     // number of image on which the user clicked
     private int imageNumber;
     // number of player who clicked (0 or 1)
     private int player = 0;
     private Player[] players;
     private FieldState[] fieldStates;
     // number of player that won - 9 for no winner
     private int gameFinished = 9;
     // how many clicks were performed
     private int clicks = 0;
     private int games = 0;
     private int ties = 0;
     private Random random;
     private SelectItem[] playersItems = new SelectItem[]{
         new SelectItem(Player.HUMAN_PLAYER, "human"),
         new SelectItem(Player.COMPUTER_PLAYER, "computer")
     };
 
     public GameBean() {
         fieldStates = new FieldState[9];
         random = new Random(9);
         players = new Player[2];
 
         players[0] = new Player("Player 1", Player.HUMAN_PLAYER, Character.X, true);
         players[1] = new Player("Player 2", Player.COMPUTER_PLAYER, Character.O, false);
 
         for (int i = 0; i < 9; i++) {
             fieldStates[i] = FieldState.CLEAR;
         }
     }
 
     public FieldState[] getFieldStates() {
         return fieldStates;
     }
 
     public void setFieldStates(FieldState[] fieldStates) {
         this.fieldStates = fieldStates;
     }
 
     public int getImageNumber() {
         return imageNumber;
     }
 
     public void setImageNumber(int imageNumber) {
         this.imageNumber = imageNumber;
     }
 
     public Player[] getPlayers() {
         return players;
     }
 
     public void setPlayers(Player[] players) {
         this.players = players;
     }
 
     public SelectItem[] getPlayersItems() {
         return playersItems;
     }
 
     public int getGames() {
         return games;
     }
 
     public void setGames(int games) {
         this.games = games;
     }
 
     public int getPlayer() {
         return player;
     }
 
     public void setPlayer(int player) {
         this.player = player;
     }
 
     public int getTies() {
         return ties;
     }
 
     public void setTies(int ties) {
         this.ties = ties;
     }
 
     public String click() {
         if (gameFinished == 0 || gameFinished == 1 || clicks == 9) {
             return null;
         }
 
         // if player click into clear field, perform click, otherwise ignore click
         if (FieldState.CLEAR.equals(fieldStates[imageNumber])) {
             fieldStates[imageNumber] = FieldState.getNormalState(players[player].getCharacter());
 
             checkLine(imageNumber / 3);
             checkColumn(imageNumber % 3);
             if (imageNumber % 2 == 0) {
                 checkDiagonals();
             }
 
             clicks++;
             if (clicks == 9 && gameFinished != 0 && gameFinished != 1) {
                 games++;
                 ties++;
                 return null;
             }
             changePlayer();
         }
 
         if (gameFinished != 0 && gameFinished != 1 && players[player].getType() == Player.COMPUTER_PLAYER && clicks != 9) {
             do {
                 imageNumber = random.nextInt(9);
             } while (!fieldStates[imageNumber].equals(FieldState.CLEAR));
             click();
         }
 
         return null;
     }
 
     public String newGame() {
         for (int i = 0; i < 9; i++) {
             fieldStates[i] = FieldState.CLEAR;
         }
         gameFinished = 9;
         player = (players[0].isFirst() ? 0 : 1);
         clicks = 0;
 
         if (players[player].getType() == Player.COMPUTER_PLAYER) {
             imageNumber = random.nextInt(9);
             click();
         }
 
         return null;
     }
 
    public String selectFirstPlayer() {
        players[0].setFirst(player == 0 ? true : false);
        players[1].setFirst(player == 1 ? true : false);
 
        return newGame();
     }
 
     public String clearStats() {
         games = 0;
         ties = 0;
         players[0].setWins(0);
         players[1].setWins(0);
 
         return null;
     }
 
     /**
      * Checks whether the selected line wins.
      * 
      * @param index
      *            the number of line (0 to 2)
      */
     private void checkLine(int index) {
         if (fieldStates[3 * index].equals(fieldStates[3 * index + 1]) && fieldStates[3 * index + 1].equals(fieldStates[3 * index + 2])) {
             for (int i = 0; i < 3; i++) {
                 fieldStates[3 * index + i] = FieldState.getWinningState(players[player].getCharacter());
             }
             finishGame();
         }
     }
 
     /**
      * Checks whether the selected column wins.
      * 
      * @param index
      *            the number of column (0 to 2)
      */
     private void checkColumn(int index) {
         if (fieldStates[index + 0].equals(fieldStates[index + 3]) && fieldStates[index + 3].equals(fieldStates[index + 6])) {
             for (int i = 0; i < 7; i += 3) {
                 fieldStates[index + i] = FieldState.getWinningState(players[player].getCharacter());
             }
             finishGame();
         }
     }
 
     /**
      * Checks whether diagonals win.
      */
     private void checkDiagonals() {
         Character charr = players[player].getCharacter();
 
         if (fieldStates[0].equals(fieldStates[4]) && fieldStates[4].equals(fieldStates[8]) && !fieldStates[0].equals(FieldState.CLEAR)) {
             fieldStates[0] = FieldState.getWinningState(charr);
             fieldStates[4] = FieldState.getWinningState(charr);
             fieldStates[8] = FieldState.getWinningState(charr);
             finishGame();
         }
 
         if (fieldStates[2].equals(fieldStates[4]) && fieldStates[4].equals(fieldStates[6]) && !fieldStates[2].equals(FieldState.CLEAR)) {
             fieldStates[2] = FieldState.getWinningState(charr);
             fieldStates[4] = FieldState.getWinningState(charr);
             fieldStates[6] = FieldState.getWinningState(charr);
             finishGame();
         }
     }
 
     private void changePlayer() {
         player = player == 0 ? 1 : 0;
     }
 
     private void finishGame() {
         gameFinished = player == 0 ? 0 : 1;
         players[player].setWins(players[player].getWins() + 1);
         games++;
     }
 }
