 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 public class Interpreter
 {
     public static void saveGame(final Sudoku game, final int time, final String filename)
     {
         StringBuffer buffer = new StringBuffer();
         
        buffer.append("time = " + Double.toString(time) + "\n");
         buffer.append("assist = " + Integer.toString(game.getRemainingAssists()) + "\n");
         buffer.append("board = { " + game.getProblem() + " }\n");
         buffer.append("solution = { " + game.getSolution() + " }\n");
         buffer.append("editable = { " + game.getEditable() + "}");
         try{
         PrintWriter writer = new PrintWriter(filename, "UTF-8");
         writer.print(buffer.toString());
         writer.close();
         }catch (Exception ex) {}
     }
     
     public static Sudoku loadGame(String filename)
     {
         int assists = 0;
         int time = 0;
         int[][] board = null;
         int[][] solution = null;
         boolean[][] editable = null;
         
         Scanner scanner;
         StringTokenizer token;
         
         try
         {
         	scanner = new Scanner(new FileReader(filename));
         	
             while (scanner.hasNext())
             {
                 token = new StringTokenizer(scanner.nextLine(), " ");
                 
                 if (token.nextToken().equals("time"))
                 {
                     token.nextToken();
                     time = Integer.parseInt(token.nextToken());
                 }
                 else if (token.nextToken().equals("assist"))
                 {
                     token.nextToken();
                     assists = Integer.parseInt(token.nextToken());
                 }
                 else if (token.nextToken().equals("board"))
                 {
                     board = textToSudoku(token);
                 }
                 else if (token.nextToken().equals("solution"))
                 {
                     solution = textToSudoku(token);
                 }
                 else if (token.nextToken().equals("editable"))
                 {
                     editable = textToEditable(token);
                 }
             }
         }
         catch (Exception ex)
         {
         }
         
         return new Sudoku(board, solution, editable, assists);
     }
     
     private static int[][] textToSudoku(StringTokenizer token)
     {
         int[][] board = new int[9][9];
         
         token.nextToken();
         token.nextToken();
         
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 board[i][j] = Integer.parseInt(token.nextToken());
             }
         }
         
         return board;
     }
     
     private static boolean[][] textToEditable(StringTokenizer token)
     {
         boolean[][] board = new boolean[9][9];
         
         token.nextToken();
         token.nextToken();
         
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 board[i][j] = Boolean.parseBoolean(token.nextToken());
             }
         }
         
         return board;
     }
     
     public static boolean saveProfile(Profile profile)
     {
         return true;
     }
     
     public static Profile loadProfile(String filename)
     {
         return new Profile("");
     }
 }
