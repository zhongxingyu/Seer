 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 
 /**
  * Sudoku is the entry point for the game. It 
  * has the main game loop and some simple menu stuff.
  * 
  * @author Andrew Hood
  * @version 0.1
  */
 public class Sudoku
 {
     public static void main(String[] args)
     {
     	// Make a new board
         Gameboard board = new Gameboard();
         
         // load the Sudoku board 
         board.loadSudoku();
         
         System.out.println("#################################");
         System.out.println("# Sudoku   (c) 2013 Andrew Hood #");
         System.out.println("# 1) Single Player. 2) Solver   #");
         System.out.println("#################################");
         int whichType = 0;
         Scanner input = new Scanner(System.in);
         
         boolean badInput = false;
         do
         {
         	try 
         	{
        		System.out.print("~$ ");
         		whichType = input.nextInt();
         		badInput = false;
         	} catch (InputMismatchException ex) {
         		System.err.println("1 for Single Player, 2 for Solver.");
         		badInput = true;
         		input.nextLine();
         	}
         // keep prompting for 1 or 2 until either is selected. 
         } while(badInput || whichType < 1 || whichType > 2);
         
         // Instantiate a player. 
         Player player = null;
         if(whichType == 1)
         {
         	 player = new NormalPlayer(board);
         } else if(whichType == 2) {
         	player = new ComputerPlayer(board);
         } else {
         	System.exit(1);
         }
         
         // The main game loop
         while(!board.isComplete())
         {
             Move tempMove = player.nextMove();
             if(board.isMoveValid(tempMove))
             {
                 board.placeMove(tempMove);
             } else {
                 System.err.println("The move: " + tempMove + " is invalid.");
             }
         }
         System.out.println("You won!");
     }
     
 }
