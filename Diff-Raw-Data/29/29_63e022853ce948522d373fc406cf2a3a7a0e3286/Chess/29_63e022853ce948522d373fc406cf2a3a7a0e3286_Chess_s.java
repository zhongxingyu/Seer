 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.*;
 
 import exceptions.IllegalMoveException;
 import exceptions.IllegalPromotionException;
 
 
 public class Chess {
   public static void main(String[] argv) {
     Board board = new Board();
     String player_turn = "white";
     String input = "";
     Scanner sc = new Scanner(System.in);
     
     int offered_draw = 0;
     
     System.out.println(board);
     
     while (input != null) {
       // are we in mate?
       King tking = board.matedKing();
       if (tking != null) {
         if (tking.getColor().equals("w")) {
           System.out.println("black wins. gg.");
           break;
         } else {
           System.out.println("white wins. gg.");
           break;
         }
       }
       
       System.out.println("it is "+ player_turn + "\'s turn");
       while(input.equals("")) {
         System.out.print(">  ");
         input = sc.nextLine().trim();
       }
       
       String[] inputTokens = input.split(" ");
       
       // someone resigns
       if (inputTokens[0].equals("resign")) {
         if (player_turn.equals("white")) {
           System.out.println("black wins. gg.");
           break;
         } else {
           System.out.println("white wins. gg.");
           break;
         }
       }
       
       // draw
       if (inputTokens[0].equals("draw?")) {
         if (offered_draw == 0) {
           System.out.println(player_turn + " offered a draw");
           player_turn = nextPlayer(player_turn);
           input = "";
           offered_draw = 2;
           continue;
         } else if (offered_draw == 2) {
           System.out.println("draw game. gg.");
           break;
         }
       }
       
       try {
        if (!board.pieceBelongsToPlayer(inputTokens[0], player_turn)) {
           throw new IllegalMoveException("piece does not belong to " + player_turn);
         }
         
         board.movePiece(inputTokens[0], inputTokens[1]);
         
         if (inputTokens.length == 3) {
           board.checkForUpgrades(inputTokens[2]);
         }
         
         System.out.println(board);
         player_turn = nextPlayer(player_turn);
         
         if (offered_draw == 2) offered_draw = 0; // cancel the draw offer
       } catch (IllegalMoveException e) {
         System.err.println("Error: " + e.getMessage() + ". Try again.\n");
       } catch (IllegalPromotionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
       
       input = "";
     }
   }
   
   private static String nextPlayer(String current_player) {
     return (current_player.equals("black") ? "white" : "black");
   }
   
 }
