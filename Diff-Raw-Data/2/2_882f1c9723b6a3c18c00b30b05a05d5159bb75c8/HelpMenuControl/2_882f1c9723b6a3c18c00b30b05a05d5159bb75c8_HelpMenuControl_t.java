 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ticTacToe;
 
 
 /**
  *
  * @author jacksonrkj
  */
 public class HelpMenuControl  {
     
     public HelpMenuControl() {
         
     } 
 
     public void displayBoardHelp() {
         System.out.println();
         this.displayHelpBoarder();             
         System.out.println( 
                 "\tThe game board for Tic-Tac-Toe. It consist of a grid of "
                + "\n\tlocations. Players place their marker on the different locations "
                 + "\n\ton the board in an effort to win the game. The default board is "
                 + "\n\t3 rows by 3 columns.");
         displayHelpBoarder();
     }
     
     
         
     public void displayGameHelp() {
         System.out.println();
         displayHelpBoarder();     
         System.out.println( 
                  "\tThe objective of the game is to be the first player to mark three "
                 + "\n\tsquares vertically, horizontally or diagonally. Each player takes "
                 + "\n\tturns placing their marker in one of the locations on the "
                 + "\n\tboard. The first player to get \"three-in-a-row\" is the winner."
                 ); 
         displayHelpBoarder();
     }
             
     public void displayRealPlayerHelp() {
         System.out.println();
         displayHelpBoarder();     
         System.out.println( 
                 "\tA real player manually takes their turn by placing their mark "
                 + "\n\tin an unused location on the board."
                 ); 
         displayHelpBoarder();
     }
     
                    
     public void displayComputerPlayerHelp() {
         System.out.println();
         displayHelpBoarder();     
         System.out.println( 
                 "\tA computer based player utomatically takes its turn "
                 + "\n\timmediatly after a real player in a single player game."
                 ); 
         displayHelpBoarder();
     }
              
     public void displayLocationHelp() {
         System.out.println();
         displayHelpBoarder();     
         System.out.println( 
                "\tA location on the board where a player can place their marker"
                 ); 
         displayHelpBoarder();
     }
                  
     public void displayMarkerHelp() {
         System.out.println();
         displayHelpBoarder();     
         System.out.println( 
                "\tA symbol that \"marks\" the locations in the board that are occupied "
                 + "by a player. "
                 + "\n\tThe default markers are \"X\" and \"O\"."
                 ); 
         displayHelpBoarder();
     }
     
     
     public void displayHelpBoarder() {       
         System.out.println(
         "\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
     }
     
   
 }
