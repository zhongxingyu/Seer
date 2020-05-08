 package code;
 
 import javax.swing.SwingUtilities;
 
 import View.ChessView;;
 
 public class Main {
 
     static int searchDepth = 5;
     static ChessBoard board ;
     static String gameInput = "COMMAND LINE";
     
     
     /*
      * Insertion point for the project
      * 
      * @param args String[] args are the parameters
      */
     public static void main(String[] args) {
 
         if (gameInput.equals("GUI")){
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 ChessView main = new ChessView();
                 main.setVisible(true);
             }
         });
         }
         
         if (gameInput.equals("HTTP")){ 
             boolean playerColor = true;
             board = new ChessBoard();
             if (!playerColor) {
                 try {
                     Searcher.searchLayer1(!playerColor, board, searchDepth);
                 } catch (Exception e) {
                 }
             }
 
             while (true) {
                 try {
                     board.prettyPrint();
                     //CLI.makeMoveForUser(board, playerColor);
                     //TODO: Use the Connection class
                     Searcher.searchLayer1(!playerColor, board, searchDepth);
                 } catch (Exception e) {
                     Searcher.determineWinner(board, playerColor);
                     break;
                 }
             }
         }
         
         if (gameInput.equals("COMMAND LINE")){
             boolean playerColor = true;
             board = new ChessBoard();
             if (!playerColor) {
                 try {
                     Searcher.searchLayer1(!playerColor, board, searchDepth);
                 } catch (Exception e) {
                 }
             }
 
             while (true) {
                 try {
                     board.prettyPrint();
                     CLI.makeMoveForUser(board, playerColor);
                     Searcher.searchLayer1(!playerColor, board, searchDepth);
                } catch (Exception e) {
                    e.printStackTrace();
                     Searcher.determineWinner(board, playerColor);
                     break;
                 }
             }
         }
     }
 }
