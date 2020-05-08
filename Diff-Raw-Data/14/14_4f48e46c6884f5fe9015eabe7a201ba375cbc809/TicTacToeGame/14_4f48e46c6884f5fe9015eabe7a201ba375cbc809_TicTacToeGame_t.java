 import java.util.Scanner;
 
 public class TicTacToeGame
 {
 	private char[][] board;
 	
 	public TicTacToeGame()
 	{
 		board = new char[3][3];
 		for(int i = 0; i < 3; i++)
 		{
 			for(int j = 0; j < 3; j++)
 			{
 				board[i][j] = ' ';
 			}
 		}	
 	}
 	
 	public boolean makeMove(int row, int col, boolean isPlayerX)
 	{
 		if(board[row][col] == ' ')
 		{
 			if(isPlayerX)
 			{
 				board[row][col] = 'X';
 			}	
 			else
 			{
 				board[row][col] = 'O';
 			}	
 			return true;	
 		}
 		else
 			return false;
 	}
 	
 	public void printBoard()
 	{
 		System.out.println(board[0][0] + " | " + board[1][0] + " | " + board[2][0]);
 		System.out.println("- + - + -");
 		System.out.println(board[0][1] + " | " + board[1][1] + " | " + board[2][1]);
 		System.out.println("- + - + -");
 		System.out.println(board[0][2] + " | " + board[1][2] + " | " + board[2][2]);
 		
 		/*for (int i = 0; i < 3; i++)
 		{
 			if (i != 0)
 			{
 				for (int k = 0; k < SIZE * 2 - 1; k++)
 			for (int j = 0; j < 3; j++)
 			{
 				if (j != 0)
 					System.out.print("|");
 			}
 		}*/			
 	}
 	
 	public static void main(String[] args)
 	{
 		String whosPlaying = "X";
 		boolean isXTurn = true;
		int moveRow = 0, moveCol = 0, turnCount = 1;
 		TicTacToeGame game = new TicTacToeGame();
 		
 		Scanner keyboard = new Scanner(System.in);
 		
 		System.out.println("Who will go first?");
 		whosPlaying = keyboard.nextLine();
 		
 		if (whosPlaying.equalsIgnoreCase("X"))
 			isXTurn = true;
 		else
 			isXTurn = false;
 		
		while (turnCount < 10)
 		{
 			System.out.println("Player " + whosPlaying + "'s turn!");
 			System.out.println("Which row?");
 			moveRow = keyboard.nextInt();
 			System.out.println("Which column?");
 			moveCol = keyboard.nextInt();
 			game.makeMove(moveRow, moveCol, isXTurn);
 			
 			if (isXTurn)
 			{
 				isXTurn = false;
 				whosPlaying = "O";
 			}	
 			else if (!isXTurn)
 			{
 				isXTurn = true;
 				whosPlaying = "X";
			}
			
			game.printBoard();
			turnCount++;
 		}	
 	}
 }
