 package main;
 
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 public class TicTacToe
 {
     public boolean quitGame = false;
     public char[][] arr;
     public boolean player1=true;
     public boolean player2=true;
 
     public TicTacToe()
     {
         this.quitGame = false;
         arr = new char[3][3];
         char cntr = '1';
 
         for (int i=0; i<3; i++)
         {
              for (int j=0; j<3 ;j++)
              {
                  arr[i][j]=cntr;
                  cntr++;
              }
         }
     }
     public void newBoard()
     {
         char cntr='1';
         for (int i=0; i<3; i++)
         {
             for (int j=0;j<3;j++)
             {
                 arr[i][j]=cntr;
                 cntr++;
             }
         }
     }
 
     public void setMark(int field)
     {
         //X and O are the only characters allowed
         char symbol;
         if (player1)
             symbol='X';
         else
             symbol='O';
 
 
         //We want to compare it to field value to see if it's already marked
         char cfield;
         cfield = Character.forDigit(field,10);
 
 
         //We use modulus to map from field to matrix and check if it has already been marked
 
         if(field>6)
             if(arr[2][(field-1) % 3]==cfield)
                 arr[2][(field-1) % 3]=symbol;
             else
                 System.out.println("It's already marked");
         else if (field>3)
             if(arr[1][(field-1) % 3]==cfield)
                 arr[1][(field-1) % 3]=symbol;
             else
                 System.out.println("It's already marked");
         else
             if(arr[0][(field-1) % 3]==cfield)
                 arr[0][(field-1) % 3]=symbol;
             else
                 System.out.println("It's already marked");
 
     }
     public void switchPlayer(){                 //Might have to switch because both start as TRUE
 
         if(player1)
         {
             player2=true;
             player1=false;
         }
         else
         {
             player1=true;
             player2=false;
         }
     }
 
     public int checkWinner()
     {
         //Returns:
         //1 :  If player 1 won
         //2 : If player 2 won
         //0 : If it's a draw
         //-1 : If no winner yet, game still playing
 
 
         boolean draw=true;
         int winner;
         //Check for 3-in-a-row vertical and horizontal
         for (int i=0;i<3;i++)
         {
         if (arr[i][i]=='X')      //We check the intersecting field [0][0], [1][1], [2][2]
             winner = 1;
         else
             winner = 2;
         if(arr[i][0]==arr[i][1] && arr[i][1]==arr[i][2])
         {
             return winner;
         }
         if(arr[0][i]==arr[1][i] && arr[1][i]==arr[2][i])
         {
             return winner;
         }
 
         }
 
         //We check for 3-in-a-row on the diagonal
         if(arr[0][0]==arr[1][1] && arr[1][1]==arr[2][2])
         {
             if (arr[0][0]=='X')
             return 1;
              else
             return 2;
         }
          if(arr[0][2]==arr[1][1] && arr[1][1]==arr[2][0])
          {
              if (arr[0][0]=='X')
             return 1;
              else
             return 2;
         }
         for (int i=0;i<3;i++)
         {
             for (int j=0;j<3;j++)
             {
                 if(Character.isDigit(arr[i][j]))
                     draw=false;
 
             }
         }
         if(draw)
         {
             return 0;
         }
 
         //The game is not over yet
         return -1;
     }
 
 
     public boolean isQuitGame()
     {
         return quitGame;
     }
 
     public void QuitGame()
     {
         this.quitGame = true;
     }
 
     public int GetUserInput()
     {
         try
         {
             return new Scanner(System.in).nextInt();
         }
         catch (InputMismatchException ex)
         {
             return -1;
         }
     }
 
     public char GetMark(int field)
     {
         field -= 1;  //Switch to 0 based indices
         int i = -1;
         int j = -1;
 
         if (field >= 6)
         {
             //Bottom row
             i = 2;
             j = field % 3;
         }
         else if (field >= 3 && field <=5)
         {
             //Middle row
             i = 1;
             j = field % 3;
         }
         else if (field >= 0 && field <=2)
         {
             //Top row
             i = 0;
             j = field % 3;
         }
 
         return arr[i][j];
     }
 
 
     public void printWinner(int winner)
     {
         if(winner==0){
             System.out.println("DRAW!");
         }
         else if(winner==1){
             System.out.println("Player 1 wins!");
         }
         else if(winner==2){
             System.out.println("Player 2 wins!");
         }
         else
             System.out.println("something went terribly wrong..");
 
     }
 
     public void printBoard()
     {
         for(int i=0; i<3; i++)
         {
             for(int j=0; j<3; j++)
             {
                System.out.print("["+ arr[i][j] + "]");
             }
            System.out.println("");
         }
     }
 
     public void playGame()
     {
         int winner = -1;
 
         newBoard();
 
         while (winner == -1)
         {
             System.out.println();
             printBoard();
 
             System.out.println();
 
             //Get user input
             boolean validInput = false;
             int userInput = -1;
             while (!validInput)
             {
                 userInput = GetUserInput();
                 if (userInput == -1 || userInput < 1 || userInput > 9)
                     System.out.println("Please select a number between 1 and 9");
                 else
                     validInput = true;
             }
 
             setMark(userInput);
             winner = checkWinner();
             switchPlayer();
         }
 
         //Game has ended
         //winner = 0 if draw
         //winner = 1 if player 1 won
         //winner = 2 if player 2 won
 
         System.out.println();
         printWinner(winner);
     }
 
 }
 
 
