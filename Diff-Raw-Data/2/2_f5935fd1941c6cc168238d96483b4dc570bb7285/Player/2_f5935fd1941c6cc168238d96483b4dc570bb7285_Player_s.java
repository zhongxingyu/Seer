 package nim;
 
 import java.util.Scanner;
 
 public class Player {
 	private String name;
	private static Scanner input;
 	
 	public Player(String name)
 	{
 		this.name = name;
 	}
 	
 	public Board takeTurn(Board board)
 	{
 		int row, x;
 		board.displayBoard();
 		System.out.println("Player " + name + "'s turn! Please choose a row(1, 2, or 3):");
 		while(true)
 		{
 			try
 			{
 				row = input.nextInt();
 				if(row > 3 || row < 1)
 					throw new Exception();
 				
 				if(board.getRow(row) == 0)
 				{
 					System.out.println("That row is already empty! Please try another.");
 					throw new Exception();
 				}
 					
 					
 				break;
 			}
 			catch(Exception e)
 			{
 				System.err.println("Invalid Input");
 				System.out.println("You goofed. Please choose an integer 1, 2, or 3:");
 			}
 		}
 		
 		System.out.println("You have chosen row " + row + ". Now please choose a number to remove between 1 and " + board.getRow(row));
 		while(true)
 		{
 			try
 			{
 				x = input.nextInt();
 				if(x > 0 && x <= board.getRow(row))
 				{
 					board.makeMove(row, x);
 					System.out.println(x + " removed from row " + row + ".");
 				}
 				else
 				{
 					throw new Exception();
 				}
 				
 				break;
 			}
 			catch(Exception e)
 			{
 				System.err.println("Invalid Input");
 				System.out.println("You goofed. Please choose an integer between 1 and " + board.getRow(row));
 			}
 		}
 		return board;
 	}
 }
