 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.common.Coordinate;
 import edu.victone.scrabblah.logic.common.Tile;
 import edu.victone.scrabblah.logic.common.Word;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 4:00 PM
  */
 public class GameBoard {
     static int[][] cellValues =
             {{4, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 4},
                     {0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0},
                     {0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0},
                     {1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1},
                     {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                     {0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0},
                     {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0},
                     {4, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, 4},
                     {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0},
                     {0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0},
                     {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                     {1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1},
                     {0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0},
                     {0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0},
                     {4, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 4}};
 
     private BoardCell[][] boardCells = new BoardCell[15][15];
     private ArrayList<Word> wordList;
 
     public GameBoard() {
         initBoard();
     }
 
     public GameBoard(GameBoard gb) {
         initBoard();
         for (int i = 0; i < 15; i++) {
             for (int j = 0; j < 15; j++) {
                 if (!boardCells[i][j].isEmpty()) {
                     Coordinate coord = new Coordinate(i, j);
                     boardCells[i][j].setTile(new Tile(gb.getCell(coord).getTile()));
                     if (gb.getCell(coord).isLocked()) {
                         boardCells[i][j].lock();
                     }
                 }
             }
         }
     }
 
     private void initBoard() {
         for (int i = 0; i < 15; i++) {
             for (int j = 0; j < 15; j++) {
                 switch (cellValues[i][j]) {
                     case -1: //star cell
                     case 0:  //regular
                         boardCells[i][j] = new BoardCell(1, false);
                         break;
                     case 1:  //dublet
                         boardCells[i][j] = new BoardCell(2, false);
                         break;
                     case 2:  //triplet
                         boardCells[i][j] = new BoardCell(3, false);
                         break;
                     case 3:  //dubword
                         boardCells[i][j] = new BoardCell(2, true);
                         break;
                     case 4:  //tripword
                         boardCells[i][j] = new BoardCell(3, true);
                         break;
                 }
             }
         }
     }
 
     public BoardCell getCell(Coordinate coord) {
         return boardCells[coord.getY()][coord.getX()];
     }
 
     public ArrayList<BoardCell> getCellNeighbors(Coordinate coord) {
         ArrayList<BoardCell> retVal = new ArrayList<BoardCell>(4);
 
         retVal.add(getCell(coord.getY() != 0 ? new Coordinate(coord.getX(), coord.getY() - 1) : null));
         retVal.add(getCell(coord.getY() != 14 ? new Coordinate(coord.getX(), coord.getY() + 1) : null));
         retVal.add(getCell(coord.getX() != 14 ? new Coordinate(coord.getX() + 1, coord.getY()) : null));
         retVal.add(getCell(coord.getX() != 0 ? new Coordinate(coord.getX() - 1, coord.getY()) : null));
 
         return retVal;
     }
 
     public int getNumOccupiedCells() {
         int ctr = 0;
         for (int i = 0; i < 15; i++)
             for (int j = 0; j < 15; j++)
                 if (!boardCells[i][j].isEmpty())
                     ctr++;
         return ctr;
     }
 
     public void lockOccupiedCells() {
         for (int i = 0; i < 15; i++)
             for (int j = 0; j < 15; j++)
                 getCell(new Coordinate(i, j)).lock();
 
     }
 
     public void setWordList(ArrayList<Word> wordList) {
         this.wordList = wordList;
     }
 
     public ArrayList<Word> getWordList() {
         return wordList;
     }
 
     @Override
     public String toString() {
         String header = "   A   B   C   D   E   F   G   H   I   J   K   L   M   N   O\n";
         String row = " |---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|";
         StringBuilder sb = new StringBuilder(header);
        sb.append(row + "\n");
         for (int i = 0; i < 15; i++) {
             sb.append((char) (i + 65));
             sb.append("|");
             for (int j = 0; j < 15; j++) {
                 BoardCell bc = boardCells[i][j];
                 if (bc.isEmpty()) {
                     switch (bc.getMultiplier()) {
                         case 1:
                             sb.append("   |");
                             break;
                         case 2:
                             sb.append(bc.isWordMultiplier() ? "DW |" : "DL |");
                             break;
                         case 3:
                             sb.append(bc.isWordMultiplier() ? "TW |" : "TL |");
                             break;
                     }
                 } else {
                    sb.append(bc.getTile().toString() + " |");
                 }
             }
             sb.append((char) (i + 65));
            sb.append("\n" + row + "\n");
         }
         sb.append(header);
         return sb.toString();
     }
 }
