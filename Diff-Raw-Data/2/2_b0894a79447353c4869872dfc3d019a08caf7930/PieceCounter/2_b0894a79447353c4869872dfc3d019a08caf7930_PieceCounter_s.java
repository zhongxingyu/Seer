 import java.io.*;
 import java.util.*;
 
 public class PieceCounter
 {
 	public PieceCounter()
 	{
 
 	}
 
 	public int countTotalPieces(int[][] board, int rows, int columns, int player)
 	{
 		int pieceCounter = 0;
 		for(int i = 0; i < rows; i++)
 		{
 			for(int j = 0; j < columns; j++)
 			{
 				if(board[i][j] == player)
 					pieceCounter++;
 			}
 		}
 		return pieceCounter;
 	}
 
 	public int rowCounter(int[][] board, int cols, int row, int player)
 	{
 		int pieceCounter = 0;
 		for(int i = 0; i < cols; i++)
 		{
 			if(board[row][i] == player)
 				pieceCounter++;
 		}
 		return pieceCounter;
 	}
 
 	public int colCounter(int[][] board, int rows, int col, int player)
 	{
		pieceCounter = 0;
 		for(int i = 0; i < rows; i++)
 		{
 			if(board[i][col] == player)
 				pieceCounter++;
 		} 
 		return pieceCounter;
 	}
 
 	public static void main(String args[])
 	{
 		PieceCounter tester = new PieceCounter();
 
 		if(args.length != 3) 
 		{
 		    System.out.println("Usage: java FeatureCounter [num rows] [num cols] [player num]\n");
 		    System.exit(-1);
 		}
 		int num_rows = Integer.parseInt(args[0]);
 		int num_cols = Integer.parseInt(args[1]);
 		int player_num = Integer.parseInt(args[2]);
 
 		int board[][] = new int[num_rows][num_cols];
 		
 		// create an example board
 		for(int i = 0; i < num_rows; i++)
 		{
 			for(int j = 0; j < num_cols; j++)
 			{
 				board[i][j] = player_num;
 			}
 		}
 		
 		int total = tester.countTotalPieces(board, num_rows, num_cols, player_num);
 		System.out.println(total);
 	}
 }
