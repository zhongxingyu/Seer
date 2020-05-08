 /**
 * @author MpoMp 
 * @version 0.2
 * @since 14-12-2011
 * 
 * Tic-Tac-Toe game
 */ 
 
 import acm.program.*;
  
 /**	Notes and tasks:
 *	-acm package used for parsing and printing
 *
 *	TODO: remove acm dependence, update to ver 1.x
 * 	TODO: check for useful exception handling
 * 	TODO: add table lines display
 */
 public class TicTac extends Program 
 {
  	public void run() 
 	{
 		String ttt[][] = new String[3][3];	//the tic-tac-toe table and variables
 		final String x = "X"; 
 		final String o = "O";
 		boolean win = false;
 		byte empt = 9;				//empty positions left
 		int l, r;				//l, r variables for reading the user input
 		
 		//Table initialized
 		for (byte i=0; i<3; i++)
 			for (byte j=0; j<3; j++)
 				ttt[i][j] = "-";
 		
 		display(ttt); //Table display
 		
 		//Two initial turns for each player
 		//No winning moves available yet
 		for (byte i=1; i<=2; i++)
 		{
 			do
 			{
 				l = readInt("X player: Give a LINE number: ");
 				r = readInt("X player: Give a ROW number: ");
			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l][r].compareToIgnoreCase("-") != 0))
 			ttt[l-1][r-1] = x;
 			empt--;
 			
 			display(ttt); //Table display
 			
 			do
 			{
 				l = readInt("O player: Give a LINE number: ");
 				r = readInt("O player: Give a ROW number: ");
			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l][r].compareToIgnoreCase("-") != 0))
 			ttt[l-1][r-1] = o;
 			empt--;
 			
 			display(ttt); //Table display
 		}
 		
 		//Winning moves available
 		//Checking for victory after each move
 		do
 		{
 			do
 			{
 				l = readInt("X player: Give a LINE number: ");
 				r = readInt("X player: Give a ROW number: ");
			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l][r].compareToIgnoreCase("-") != 0))
 			ttt[l-1][r-1] = x;
 			empt--;
 			display(ttt); 			//Table display
 			
 			win = check(ttt, x, l-1, r-1);
 			if (win || empt == 0) break; 	//While X plays first, he will also be the last to play if the game ends up to a draw
 			
 			do
 			{
 				l = readInt("O player: Give a LINE number: ");
 				r = readInt("O player: Give a ROW number: ");
			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l][r].compareToIgnoreCase("-") != 0))
 			ttt[l-1][r-1] = o;
 			empt--;			
 			display(ttt); //Table display
 			
 			win = check(ttt, o, l-1, r-1);
 		}while(!win);
 		
 		if (empt == 0 && win == false)
 			println("\nDraw");
 	}
 	
 	//"check" function checks if the last move is a winning move
 	//variable "lm" holds the last move symbol
 	private boolean check(String t[][], String lm, int l, int r)
 	{
 		boolean ret = false;
 				
 		//check if the row where the last move was made is complete
 		if ((t[0][r].compareToIgnoreCase(t[1][r]) == 0  && (t[0][r].compareToIgnoreCase(t[2][r]) == 0 )))
 		{
 			ret = true;
 		}
 		//check if the line where the last move was made is complete
 		else if ((t[l][0].compareToIgnoreCase(t[l][1]) == 0  && (t[l][0].compareToIgnoreCase(t[l][2]) == 0 )))
 		{
 			ret = true;
 		}
 		//check the first diagonal
 		else if ((t[0][0].compareToIgnoreCase(t[1][1]) == 0 && (t[0][0].compareToIgnoreCase(t[2][2]) == 0)))
 		{
 			ret = true;
 		}
 		//check the second diagonal
 		else if ((t[2][0].compareToIgnoreCase(t[1][1]) == 0  && (t[2][0].compareToIgnoreCase(t[0][2]) == 0 )))
 		{
 			ret = true;
 		}
 		
 		if(ret) 
 			println("Player " + lm + " wins!");
 		
 		return ret;		
 	}
 	
 	//displays the table
 	private void display(String t[][])
 	{
 		println("\n");
 		for (byte i=0; i<3; i++) 
 		{			
 			println("\n");
 			for (byte j=0; j<3; j++)
 				print(" " + t[i][j]);
 		}
 		println("\n");
 	}
 }	
