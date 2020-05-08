 package main;
 
 public class Main
 {
 
     public static void main(String[] args)
     {
         TicTacToe game = new TicTacToe();
         game.checkWinner();
 
         System.out.println("Welcome to TicTacToe!\n");
 
         game.setMark(5,'X');
         game.newBoard();
 
 
 
         while (!game.isQuitGame())
         {
             System.out.println("\nWhat do you want to do?");
             System.out.println("1. Play a game of TicTacToe");
             System.out.println("2. See rulebook");
             System.out.println("3. Quit\n");
 
             //Get user input
             boolean validInput = false;
             int userInput = -1;
             while (!validInput)
             {
                 userInput = game.GetUserInput();
                 if (userInput == -1 || userInput < 1 || userInput > 3)
                     System.out.println("Please select a number between 1 and 3");
                 else
                     validInput = true;
             }
 
             if (userInput == 1)
             {
                 //Play game
                 System.out.println("\nPlay game!\n");
             }
             else if (userInput == 2)
             {
                 //Print rulebook
                 System.out.println("\nThe board looks like this:\n");
 
                System.out.println("[1] [2] [3]");
                System.out.println("[4] [5] [6]");
                System.out.println("[7] [8] [9]\n");
 
                 System.out.println("Player 1 writes X's, player 2 writes O's.");
                 System.out.println("When it's your turn, press a number from 1 to 9 to mark the corresponding field.");
                 System.out.println("The first one to get three fields in a row wins!\n");
             }
             else if (userInput == 3)
             {
                 //Quit game
                 game.QuitGame();
             }
         }
 
         System.out.println("Have a nice day!");
 	}
 
 
 
 }
