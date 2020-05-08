 package minesweeper;
 
 import java.util.Scanner;
 
 import static minesweeper.BoardStatus.*;
 
 /**
  * @author shiera
  */
 public class GameLogic {
     private final int BOMB = Board.BOMB;
     private Board board;
     private int size;
     private int bombAmount;
 
     private boolean running;
     private boolean playingRound;
     private Scanner reader = new Scanner(System.in);
 
     public GameLogic(int size, int boardAmount) {
         this.size = size;
         this.bombAmount = boardAmount;
         board = new Board(size, bombAmount);
         running = true;
         playingRound = false;
     }
 
     public int[][] getBoard() {
         return board.getBoard();
     }
 
     public BoardStatus[][] getBoardStatus(){
         return board.getBoardStatus();
     }
 
     public void play(){
         System.out.println("\n\nwelcome to minesweeper\n\n");
         while (running){
             playRound();
             int answ = -1;
             while (answ != 0 && answ != 1){
                 System.out.println("\n\nAgain? 0 = no, 1= yes\n\n");
                 try {
                     answ = Integer.parseInt(reader.nextLine());
                 } catch (NumberFormatException e) {
                     System.out.println("not an option");
                 }
                 if (answ != 0 && answ != 1){
                     System.out.println("not an option");
                 }
             }
             if (answ == 0){
                 running = false;
             }
         }
     }
 
     private void playRound(){
         playingRound = true;
         board.configBoard();
         while (playingRound){
             board.printBoard();
             // TODO game crashes if the input is something else than a number
             System.out.println("give x cordinate (0 - " + (size-1) + "): " );
             int x = 0;
             try {
                 x = Integer.parseInt(reader.nextLine());
             } catch (NumberFormatException e) {
                 System.out.println("not a number");
                 continue;
             }
             if (x < 0 || x >= size ){
                 System.out.println("out of board");
                 continue;
             }
             System.out.println("give y cordinate (0 - " + (size-1) + "): ");
             int y = 0;
             try {
                 y = Integer.parseInt(reader.nextLine());
             } catch (NumberFormatException e) {
                 System.out.println("not a number");
                 continue;
             }
             if (y < 0 || y >= size ){
                 System.out.println("out of board");
                 continue;
             }
             System.out.println("What do you want to do? 0 = mark, 1 = uncover, 2 = unMark ");
             int option = 0;
             try {
                 option = Integer.parseInt(reader.nextLine());
             } catch (NumberFormatException e) {
                 System.out.println("not a number");
                 continue;
             }
             if (option == 1){
                 uncover(x, y);
             }
             else if (option == 0){
                 mark(x, y);
             }
             else if (option == 2){
                 board.setStatusXY(x, y, COVERED);
 
             }
             else{
                 System.out.println("not an option");
             }
         }
     }
 
     private void uncover(int x, int y){
         board.setStatusXY(x, y, UNCOVERED );
         if (board.getBoard()[y][x] == BOMB){
             lost();
         }
     }
 
     private void lost(){
         System.out.println("\n\nARRGGH a BOMB\n\n");
         playingRound = false;
         board.printShownBoard();
     }
 
     private void won(){
        System.out.println("\n\nYay! All Bombs found");
         playingRound = false;
         board.printShownBoard();
     }
 
     private void mark(int x, int y){
         if (board.getMarkedSpaces() >= bombAmount)System.out.println("no marks left unmark something");
         else board.setStatusXY(x, y, MARKED);
 
         if (board.getMarkedSpaces() >= bombAmount){
              checkIfReady();
         }
     }
 
     private void checkIfReady(){
         int ready = -1;
         while (ready != 1 && ready != 0) {
             board.printBoard();
            System.out.println("All marks used are you ready to check?\n 0 = no, 1 = yes");
             try {
                 ready = Integer.parseInt(reader.nextLine());
             } catch (NumberFormatException e) {
                 System.out.println("not a number");
                 continue;
             }
             if (ready != 1 && ready != 0){
                 System.out.println("thats not an option");
             }
         }
         if (ready == 1){
             if (board.checkBoard()) won();
             else lost();
 
 
         }
     }
 
 
     public void changeBoard(int size, int bombAmount){
         board = new Board(size, bombAmount);
     }
 }
