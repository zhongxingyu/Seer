 package com.kodingen.cetrin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GameModel extends BaseModel {
     private static final byte FIELD_SIZE = 3;
     public static final int X = 1, O = -1, EMPTY = 0;
 
     private int[][] gameField = new int[FIELD_SIZE][FIELD_SIZE];
     private List<Turn> turns = new ArrayList<Turn>(FIELD_SIZE * FIELD_SIZE);
     private final Player xPlayer;
     private final Player oPlayer;
     private Player currentPlayer;
     private Player winner;
    private int turnsLeft = FIELD_SIZE * FIELD_SIZE;
 
     public GameModel(Player xPlayer, Player oPlayer) {
         this.xPlayer = xPlayer;
         this.oPlayer = oPlayer;
         currentPlayer = xPlayer;
     }
 
     Player getCurrentPlayer() {
         return currentPlayer;
     }
 
     private Player getXPlayer() {
         return xPlayer;
     }
 
     private Player getOPlayer() {
         return oPlayer;
     }
 
     public String makeTurn(int x, int y) {
        assert turnsLeft > 0;
         if (x < 0 || x >= FIELD_SIZE || y < 0 || y >= FIELD_SIZE) {
             return "Entered coordinates out of range. Try again.";
         }
         if (gameField[x][y] != EMPTY) {
             return "Selected position not empty. Try again.";
         }
         gameField[x][y] = getCurrentPlayer().getSymbolCode();
         turns.add(new Turn(x, y));
        turnsLeft--;
         checkWinner();
         currentPlayer = currentPlayer == xPlayer ? oPlayer : xPlayer;
         notifySubscribers();
         if (getWinner() == null) {
             currentPlayer.makeTurn(this); // next player turn
         }
         return null;
     }
 
     int turnsCount() {
         return turns.size();
     }
 
     Turn getLastTurn() {
         return turns.get(turns.size() - 1);
     }
 
     Turn getTurn(int index) {
         if (index < 0 || index >= turns.size()) {
             throw new IllegalArgumentException();
         }
         return turns.get(index);
     }
 
     private void checkWinner() {
         int winSymptom;
         for (int x = 0; x < FIELD_SIZE; x++) {
             winSymptom = 0;
             for (int y = 0; y < FIELD_SIZE; y++) {
                 winSymptom += gameField[x][y];
             }
             if (Math.abs(winSymptom) == FIELD_SIZE) {
                 setWinner(winSymptom);
             }
         }
         for (int y = 0; y < FIELD_SIZE; y++) {
             winSymptom = 0;
             for (int x = 0; x < FIELD_SIZE; x++) {
                 winSymptom += gameField[x][y];
             }
             if (Math.abs(winSymptom) == FIELD_SIZE) {
                 setWinner(winSymptom);
             }
         }
         winSymptom = 0;
         for (int x = 0, y = 0; x < FIELD_SIZE; x++, y++) {
             winSymptom += gameField[x][y];
         }
         if (Math.abs(winSymptom) == FIELD_SIZE) {
             setWinner(winSymptom);
         }
         winSymptom = 0;
         for (int x = 0, y = FIELD_SIZE - 1; x < FIELD_SIZE; x++, y--) {
             winSymptom += gameField[x][y];
         }
         if (Math.abs(winSymptom) == FIELD_SIZE) {
             setWinner(winSymptom);
         }
     }
 
     private void setWinner(int code) {
         if (code == FIELD_SIZE) {
             winner = getXPlayer();
         } else if (code == -FIELD_SIZE) {
             winner = getOPlayer();
         } else {
             throw new IllegalArgumentException();
         }
     }
 
     public Player getWinner() {
         return winner;
     }
 
     public boolean hasMoreTurns() {
        return turnsLeft > 0;
     }
 
     public int getFieldCell(int x, int y) {
         assert x >= 0 && x < FIELD_SIZE && y >=0 && y < FIELD_SIZE;
         return gameField[x][y];
     }
 
     public char getFieldCellChar(int x, int y) {
         int charCode = getFieldCell(x, y);
         switch (charCode) {
             case X: return 'X';
             case O: return 'O';
             default: return ' ';
         }
     }
 
     public int getFieldSize() {
         return FIELD_SIZE;
     }
 
     public void discardLastTwoMoves() {
         if (turnsCount() >= 2) {
            turns.remove(turnsCount() - 1);
            turns.remove(turnsCount() - 1);
         }
     }
 
     static class Turn {
         private int x, y;
 
         Turn(int x, int y) {
             this.x = x;
             this.y = y;
         }
 
         int getX() {
             return x;
         }
 
         int getY() {
             return y;
         }
         boolean isCenterTurn() {
             return x == FIELD_SIZE / 2 && y == FIELD_SIZE / 2;
         }
         boolean isCornerTurn() {
             return (x == 0 && y == 0) ||
                     (x == 0 && y == FIELD_SIZE - 1) ||
                     (x == FIELD_SIZE - 1 && y == 0) ||
                     (x == FIELD_SIZE - 1 && y == FIELD_SIZE - 1);
         }
         boolean isSideTurn() {
             return (x == FIELD_SIZE / 2 && y == 0) ||
                     (x == 0 && y == FIELD_SIZE / 2) ||
                     (x == FIELD_SIZE - 1 && y == FIELD_SIZE / 2) ||
                     (x == FIELD_SIZE / 2 && y == FIELD_SIZE - 1);
         }
     }
 
 }
