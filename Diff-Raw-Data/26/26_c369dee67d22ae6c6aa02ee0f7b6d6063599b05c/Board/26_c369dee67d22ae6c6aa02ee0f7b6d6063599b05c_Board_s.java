 package minesweeper;
 
 import java.util.Random;
 
 import static minesweeper.BoardStatus.*;
 
 /**
  * @author shiera
  */
 
 public class Board {
 
     public static final int BOMB = -1;
     private int size;
     private int bombAmount;
     private int markedSpaces = 0;
     // the board whit -1 for bomb and numbers 0-9 for bombs close
     private int[][] board;
     // the statusBoard whit 0 for uncovered, -1 for marked bomb and 1 for uncovered
     private BoardStatus[][] boardStatus;
 
     public Board(int size, int bombAmount) {
         this.size = size;
         this.bombAmount = bombAmount;
         // TODO bombamount can't be bigger than  size*size/4
         board = new int[size][size];
         boardStatus = new BoardStatus[size][size];
     }
 
 
 
     /**
      *
      * @return the boardStatus in form of int[][] whit 0 for uncovered,
      *          -1 for marked bomb and 1 for uncovered
      */
     public BoardStatus[][] getBoardStatus() {
         return boardStatus;
     }
 
 
     /**
      *
      * @return  the board in form of int[][] whit -1 for bomb and numbers 0-9 for bombs close
      */
     public int[][] getBoard() {
         return board;
     }
 
     public int getMarkedSpaces() {
         return markedSpaces;
     }
 
     /**
      *
      * @param x cordinate for the new status
      * @param y cordinate for the new status
      * @param status the new status -1 for marked bomb, 0 for covered, 1 for uncoverd
      */
     public void setStatusXY(int x, int y, BoardStatus status){
         if (cordOnBoard(x, y)){
            if (boardStatus[y][x] != MARKED && status == MARKED){
                 markedSpaces++;
                 boardStatus[y][x] = status;
             }
             else if (boardStatus[y][x] == MARKED && status == COVERED){
                 markedSpaces--;
                 boardStatus[y][x] = status;
             }
             else if (status == UNCOVERED && boardStatus[y][x] != UNCOVERED){
                 exoandIf0(x, y);
             }
             else{
                boardStatus[y][x] = status;
             }
         }
         else{
             System.out.println("cordinates x = " + x + ", y = " + y + " not on board");
         }
 
 
 
 
 
     }
 
     private void exoandIf0(int x, int y){
          // dont do if already uncovered
          if  (boardStatus[y][x] == UNCOVERED) return;
          boardStatus[y][x] = UNCOVERED;
          if (board[y][x] == 0 ){
              if (cordOnBoard(x-1, y-1)) exoandIf0(x-1, y-1);
              if (cordOnBoard(x  , y-1)) exoandIf0(x  , y-1);
              if (cordOnBoard(x+1, y-1)) exoandIf0(x+1, y-1);
              if (cordOnBoard(x-1, y  )) exoandIf0(x-1, y  );
              if (cordOnBoard(x+1, y  )) exoandIf0(x+1, y  );
              if (cordOnBoard(x-1, y+1)) exoandIf0(x-1, y+1);
              if (cordOnBoard(x  , y+1)) exoandIf0(x  , y+1);
              if (cordOnBoard(x+1, y+1)) exoandIf0(x+1, y+1);
 
          }
     }
 
     /**
      * configs a board... Places bombs and number
      */
     public void configBoard(){
         clearBoard();
         placeBombs();
         placeNumbers();
         coverBoard();
     }
 
     private void coverBoard(){
         for (int y = 0; y <size ; y++) {
             for (int x = 0; x <size ; x++) {
                 boardStatus[y][x] = COVERED;
             }
         }
     }
 
     private void clearBoard(){
         for (int y = 0; y <size ; y++) {
             for (int x = 0; x <size ; x++) {
                 board[y][x] = 0;
             }
         }
     }
     // TODO miten tehdä ettei mahdoton kaikki pommit yhdessä kasassa-tilanne
      /**
      * places the bombs at the board.
      * if the first place it tries to blace the bomb at are occupied
      * it places the bomb at next unoccupied place */
      private void placeBombs(){
         Random randomPlace = new Random();
         for (int i = 0; i <bombAmount ; i++) {
              int x = randomPlace.nextInt(size);
              int y = randomPlace.nextInt(size);
              while (board[y][x] == BOMB){
                  x ++;
                  if (x >= size){
                      x = 0;
                      y ++;
                  }
                  if (y >= size){
                      y = 0;
                  }
              }
             board[y][x] = BOMB;
 
         }
     }
 
     // nopeampi tapa ois ehkä kattoa paikat läpi vain kerran ja jos on pommi laittaa +1 jokaseen...
 
     /**
      *places the numbers on the board
      */
     private void placeNumbers(){
         for (int y = 0; y < size ; y++) {
             for (int x = 0; x <size ; x++) {
                 if (board[y][x] != BOMB){
                     board[y][x] =  bombsClose(x,y);
                 }
             }
         }
     }
 
     /**
      * Checks how many bombs there are close to the given cordinates
      */
     private int bombsClose(int x, int y){
         int bombsClose = 0;
         if (isBombAt(x-1, y-1)) bombsClose ++;
         if (isBombAt(x, y-1))   bombsClose ++;
         if (isBombAt(x+1, y-1)) bombsClose ++;
         if (isBombAt(x-1, y))   bombsClose ++;
         if (isBombAt(x+1, y))   bombsClose ++;
         if (isBombAt(x-1, y+1)) bombsClose ++;
         if (isBombAt(x, y+1))   bombsClose ++;
         if (isBombAt(x+1, y+1)) bombsClose ++;
         return bombsClose;
     }
 
     private boolean cordOnBoard(int x, int y){
         if (y >= 0 && x >= 0 && x < size && y < size) return true;
         return false;
     }
 
     private boolean isBombAt(int x, int y){
         if (cordOnBoard(x,y) && board[y][x] == BOMB){
             return true;
         }
         return false;
     }
 
 
 
 
 
 
     /**
      * Prints the board not hidden
      */
     public void printShownBoard(){
         for (int y = 0; y < size ; y++) {
             for (int x = 0; x < size ; x++) {
                 if (board[y][x] == BOMB)System.out.print("* ");
                 else System.out.print(board[y][x] + " ");
             }
             System.out.println();
         }
     }
 
     public void printBoard(){
         for (int y = 0; y < size ; y++) {
             for (int x = 0; x < size ; x++) {
                 switch (boardStatus[y][x]) {
 
                     case UNCOVERED:
                         if (board[y][x] == BOMB)System.out.print("* ");
                         else System.out.print(board[y][x] + " ");
                         break;
                     case MARKED:
                         System.out.print("x ");
                         break;
                     case COVERED:
                         System.out.print("_ ");
                         break;
                 }
             }
             System.out.println();
         }
     }
 
     public boolean checkBoard(){
         if (markedSpaces < bombAmount){
             return false;
         }
         for (int y = 0; y <size ; y++) {
             for (int x = 0; x <size ; x++) {
                 if (board[y][x] == BOMB && (boardStatus[y][x] != MARKED )){
                     return false;
                 }
             }
         }
         return true;
     }
 
 }
