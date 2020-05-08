 package com.joshcheek.ttt.cli;
 
 import com.joshcheek.ttt.library.Game;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Scanner;
 
 /**
  * Created by IntelliJ IDEA.
  * User: joshuajcheek
  * Date: 9/8/11
  * Time: 11:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class IOInteractionImpl implements IOInteraction {
 
 
     private Scanner in;
     private PrintStream out;
 
     public IOInteractionImpl(InputStream in, PrintStream out) {
         this.in = new Scanner(in);
         this.out = out;
     }
 
     public void displayResults(Game game) {
         displayBoard(game);
         if(game.isTie())                out.print("The game ended in a tie.");
         else if(game.winner() == 1)     out.print("Player 1 won the game!");
         else if(game.winner() == 2)     out.print("Player 2 won the game!");
     }
 
     public char promptForPlayer(int playerNumber) {
         out.print("Should Player " + playerNumber + " be a human or computer? (h/c) ");
         String line = in.nextLine();
         char toReturn = line.toLowerCase().charAt(0);
         if(toReturn == 'c' || toReturn == 'h')
             return toReturn;
         out.println("Invalid input, expected 'c' or 'h', but got '" + toReturn + "'");
         return promptForPlayer(playerNumber);
     }
 
     public int promptMove(Game game) {
         displayBoard(game);
         out.print("Where would you like to move? (" + availableMoves(game) + ") ");
        int move = Integer.parseInt(in.nextLine());
         if(game.isAvailable(move))
             return move;
         out.println("You can't move there.");
         return promptMove(game);
     }
 
     private void displayBoard(Game game) {
         out.println( "     |     |     ");
         out.println( "  " + markerFor(game, 1) + "  |  " + markerFor(game, 2) + "  |  " + markerFor(game, 3) + "  ");
         out.println( "     |     |     ");
         out.println( "-----|-----|-----");
         out.println( "     |     |     ");
         out.println( "  " + markerFor(game, 4) + "  |  " + markerFor(game, 5) + "  |  " + markerFor(game, 6) + "  ");
         out.println( "     |     |     ");
         out.println( "-----|-----|-----");
         out.println( "     |     |     ");
         out.println( "  " + markerFor(game, 7) + "  |  " + markerFor(game, 8) + "  |  " + markerFor(game, 9) + "  ");
         out.println( "     |     |     ");
     }
 
     private String markerFor(Game game, int position) {
         int player = game.playerAt(position);
         return  player == 1 ? "X" :
                 player == 2 ? "O" :
                               " ";
     }
 
     private String availableMoves(Game game) {
         if(game.availableMoves().length == 1)
             return Integer.toString(game.availableMoves()[0]);
         String result = "";
         int length = game.availableMoves().length;
         for(int i=0; i+1<length; ++i) {
             int move = game.availableMoves()[i];
             result += move + ", ";
         }
         result += "or " + game.availableMoves()[length-1];
         return result;
     }
 }
