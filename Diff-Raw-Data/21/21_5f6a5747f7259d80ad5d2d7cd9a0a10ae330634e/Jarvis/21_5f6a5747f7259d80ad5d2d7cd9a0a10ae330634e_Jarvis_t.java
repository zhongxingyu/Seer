 package AI;
 
 /*
 * non-blocking console input courtesy of http://www.darkcoding.net/software/non-blocking-console-io-is-not-possible/
 */
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.StringTokenizer;
 
 public class Jarvis {
     
     private static String ttyConfig;
 
     private static int num_col;
     private static int num_row;
     private static int last_col;
     private static int total_game_time;
     private static int player1_time ;
     private static int last_move_time;
     
     public static void main(String[] args) {
         
         Util util = new Util();
         
         String input = getStandardInput();        
        
         GameTree gameTree = createGameTree(input);
        gameTree.GenerateChildren(gameTree.head, 5, true);
         
         Move winningMove = findOptimalMove(gameTree.head);
                
        //IsWin isWin = new IsWin();                    
        //int score = isWin.winFunction(gameTree.head, last_col);
         
        System.out.println(winningMove.column + " " + winningMove.gamePiece);    
     }
     
     private static Move findOptimalMove(GameNode gameNodeHead) {
         
         GameNode bestGameBoard = gameNodeHead.children[4];
         int highestScore = 0;
         
         for(GameNode gameNode: gameNodeHead.children){            
             if(highestScore < gameNode.calculateChildrensScore()){
                 highestScore = gameNode.calculateChildrensScore();
                 bestGameBoard = gameNode;
             }
         }
         
         if(highestScore == 0){
             //make sure we return a valid move
         }
         
         return new Move(bestGameBoard); //No win found = PANIC
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
     
     private static String getStandardInput(){
         
         String input = "";
         
         try {
             setTerminalToCBreak();
             char character = (char) System.in.read();
             while (character != ')') {
                 if ( System.in.available() != 0 ) {
                     input += character;
                     character = (char) System.in.read();
                 }
             } // end while
         }
         catch (IOException e) {
             System.err.println("IOException");
         }
         catch (InterruptedException e) {
             System.err.println("InterruptedException");
         }
         finally 
         {
             try {
                 stty( ttyConfig.trim() );
             }
             catch (Exception e) {
                 System.err.println("Exception restoring tty config");
             }
         }
         input = input.substring(1);   
         return input;
     }
 
     private static void setTerminalToCBreak() throws IOException, InterruptedException {
 
         ttyConfig = stty("-g");
 
         // set the console to be character-buffered instead of line-buffered
         stty("-icanon min 1");
 
         // disable character echoing
         stty("-echo");
     }
 
     /**
      *  Execute the stty command with the specified arguments
      *  against the current active terminal.
      */
     private static String stty(final String args)
                     throws IOException, InterruptedException {
         String cmd = "stty " + args + " < /dev/tty";
 
         return exec(new String[] {
                     "sh",
                     "-c",
                     cmd
                 });
     }
 
     /**
      *  Execute the specified command and return the output
      *  (both stdout and stderr).
      */
     private static String exec(final String[] cmd)
                     throws IOException, InterruptedException {
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
 
         Process p = Runtime.getRuntime().exec(cmd);
         int c;
         InputStream in = p.getInputStream();
 
         while ((c = in.read()) != -1) {
             bout.write(c);
         }
 
         in = p.getErrorStream();
 
         while ((c = in.read()) != -1) {
             bout.write(c);
         }
 
         p.waitFor();
 
         String result = new String(bout.toByteArray());
         return result;
     }
     
     public static class Move{
         public Move(GameNode gameNode) {
             column = gameNode.column;
             gamePiece = gameNode.gamePiece;
         }
         int column;
         byte gamePiece;
     }
 }
