 //package AI;
 
 import java.util.Arrays;
 import java.util.StringTokenizer;
 
 public class Jarvis {
 
     private static int num_col;
     private static int num_row;
     private static int last_col;
     private static int total_game_time;
     private static int player1_time ;
     private static int last_move_time;
     
     public static void main(String[] args) {
         
         Util util = new Util();      
         String input = Util.getStandardInput();        
         
         GameTree gameTree = createGameTree(input);
         
         gameTree.GenerateChildren(gameTree.head, 5, true);
         
         Util.Move winningMove = findOptimalMove(gameTree.head);
                 
         //IsWin isWin = new IsWin();                    
         //int score = isWin.winFunction(gameTree.head, last_col);
         
         //printBoard(gameTree.gameBoard);
         
         System.out.println("(" + (winningMove.column + 1) + "," + Util.getKeyByValue(Util.pieceMap, winningMove.gamePiece) + ")");    
     }
     
     private static void printBoard(byte[][] gameBoard){
         // prints the game board (will be rotated 90 degress)
          for (int x = 0; x < num_col; x++) {
              for (int y = 0; y < num_row; y++) {
                  System.out.print(Util.getKeyByValue(Util.pieceMap, gameBoard[x + 3][y + 3]));
              }
              System.out.println();
          }
     }
     
     private static Util.Move findOptimalMove(GameNode gameNodeHead) {
         
         GameNode bestGameBoard = gameNodeHead.children[4];
         double highestScore = 0;
         
         for(GameNode gameNode: gameNodeHead.children){           
             if(gameNode.score > 0){
                 //immediate move to win
                 switch((int)gameNode.score){
                     case 5:
                         System.err.println("Initiating the clean slate protocol.");                        
                         break;
                     case 4:                        
                         System.err.println("Test complete. Preparing to power down and begin diagnostics...");
                         break;
                     case 3:
                         System.err.println("Upgrades are going well, Sir, but it seems when I get to the end of a sentence I say the wrong cranberry.");
                         break;
                     }                
                 return new Util.Move(gameNode);
             }
             if(gameNode.score < 0){
                 //immediate move to prevent a red win
                return new Util.Move(gameNode.column, Util.gamePiece_s);
             } 
             if(highestScore < gameNode.calculateChildrensScore()){
                 highestScore = gameNode.calculateChildrensScore();
                 bestGameBoard = gameNode;
             }
         }
         
         if(highestScore == 0){
             
             byte[][] gameBoard = Util.buildGameBoardFromNode(bestGameBoard);            
             if(gameBoard[bestGameBoard.column + 3][num_row + 3] == Util.gamePiece_s){
                 return new Util.Move(bestGameBoard);
             }else{
                 int col = 0;
                 for(byte[] column: gameBoard){
                     if(column[num_row + 3] == Util.gamePiece_s){
                         return new Util.Move(col,Util.gamePiece_b);
                     }
                     col++;
                 }
             }
         }
         
         return new Util.Move(bestGameBoard);
     }
     
     private static GameTree createGameTree(String input){
         
         StringTokenizer tokenizer = new StringTokenizer(input, ",");
         
         try {
             num_col = Integer.parseInt((String) tokenizer.nextElement());
             num_row = Integer.parseInt((String) tokenizer.nextElement());
             last_col = Integer.parseInt((String) tokenizer.nextElement());
             total_game_time = Integer.parseInt((String) tokenizer.nextElement());
             player1_time = Integer.parseInt((String) tokenizer.nextElement());
             last_move_time = Integer.parseInt((String) tokenizer.nextElement());
             
             Util.gameHeight = num_row;
             Util.gameWidth = num_col;
         } catch (NumberFormatException e) {
             System.err.println("Invalid input: " + e);
             return null;
         }
         
         byte[][] gameBoard = new byte[num_col + 6][num_row + 6];
         for(byte[] column: gameBoard){
             Arrays.fill(column, Util.gamePiece_s);
         }
         
         for (int x = 0; x < num_col; x++) {
             for (int y = 0; y < num_row; y++) {
                 gameBoard[x + 3][y + 3] = Util.pieceMap.get((String) tokenizer.nextElement());
             }
         }
 
         GameTree gameTree = new GameTree(gameBoard);
         return gameTree;
     }
 }
