 package main;
 
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 public class TicTacToe
 {
     public boolean quitGame = false;
     public char[][] arr;
 
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
 
     public void setMark(int field, char symbol)
     {
         //X and O are the only characters allowed
 
         if(symbol!='X' && symbol!='O')
         {
             System.out.println("This symbol is not allowed");
             return;
         }
 
         char cfield;
         cfield = Character.forDigit(field,10);   //We want to compare it to field value to see if it's already marked
 
 
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
         //System.out.println(arr[2][(field-1)%3]);
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
         if (arr[i][i]=='X')      //We check the intersecting field [0][0], [1][1], [2]{2]
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
             System.out.println("It's draw hurreYY!, let's play again");
             return 0;
         }
 
 
         System.out.println("The title TicTacToe-Champion is still up for grabs");
         return -1;  //If the game is not over yet
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
            System.out.print("[");
             for(int j=0; j<3; j++)
             {
                System.out.print(" "+arr[i][j]+" ");
             }
            System.out.println("]");
         }
     }
 }
