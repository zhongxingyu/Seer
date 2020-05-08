 /**
 * @author MpoMp 
* @version 1.4
 * @since 14-12-2011
 * 
 * Tic-Tac-Toe game
 */ 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
  
 /**	Notes and tasks:
 *
 * 	TODO: add table lines display
 * 	TODO: new game prompt (victory/loss message)
 */
 public class TicTac 
 {
  	public static void main(String args[]) throws IOException 
 	{
             InputStreamReader isr = new InputStreamReader(System.in);
             BufferedReader input = new BufferedReader(isr);
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
                             System.out.println("X player: Give a LINE number: ");
 				l = Integer.parseInt(input.readLine());
                                 System.out.println("X player: Give a ROW number: ");
 				r = Integer.parseInt(input.readLine());
 				
 				if ((r < 1 || r > 3) || (l < 1 || l > 3))
 					System.out.println("Out of bounds \n");
 				if (ttt[l-1][r-1].compareToIgnoreCase("-") != 0)
 					System.out.println("Not an empty position \n");
 			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l-1][r-1].compareToIgnoreCase("-") != 0));
 	
                     ttt[l-1][r-1] = x;
 			empt--;
 			
 			display(ttt); //Table display
 			
 			do
 			{
 				System.out.println("O player: Give a LINE number: ");
 				l = Integer.parseInt(input.readLine());
                                 System.out.println("O player: Give a ROW number: ");
 				r = Integer.parseInt(input.readLine());
 				if ((r < 1 || r > 3) || (l < 1 || l > 3))
 					System.out.println("Out of bounds \n");
 				if (ttt[l-1][r-1].compareToIgnoreCase("-") != 0)
 					System.out.println("Not an empty position \n");
 			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l-1][r-1].compareToIgnoreCase("-") != 0));
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
 				System.out.println("X player: Give a LINE number: ");
 				l = Integer.parseInt(input.readLine());
                                 System.out.println("X player: Give a ROW number: ");
 				r = Integer.parseInt(input.readLine());
 				if ((r < 1 || r > 3) || (l < 1 || l > 3))
 					System.out.println("Out of bounds \n");
 				if (ttt[l-1][r-1].compareToIgnoreCase("-") != 0)
 					System.out.println("Not an empty position \n");
 			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l-1][r-1].compareToIgnoreCase("-") != 0));
 			ttt[l-1][r-1] = x;
 			empt--;
 			display(ttt); 			//Table display
 			
 			win = check(ttt, x, l-1, r-1);
 			if (win || empt == 0) break; 	//While X plays first, he will also be the last to play if the game ends up to a draw
 			
 			do
 			{
 			System.out.println("O player: Give a LINE number: ");
 				l = Integer.parseInt(input.readLine());
                                 System.out.println("O player: Give a ROW number: ");
 				r = Integer.parseInt(input.readLine());
 				if ((r < 1 || r > 3) || (l < 1 || l > 3))
 					System.out.println("Out of bounds \n");
 				if (ttt[l-1][r-1].compareToIgnoreCase("-") != 0)
 					System.out.println("Not an empty position \n");
 			} while ((r < 1 || r > 3) || (l < 1 || l > 3) || (ttt[l-1][r-1].compareToIgnoreCase("-") != 0));
 			ttt[l-1][r-1] = o;
 			empt--;			
 			display(ttt); //Table display
 			
 			win = check(ttt, o, l-1, r-1);
 		}while(!win);
 		
 		if (empt == 0 && win == false)
 			System.out.println("\nDraw");
 	}
 	
 	//"check" function checks if the last move is a winning move
 	//variable "lm" holds the last move symbol
 	private static boolean check(String t[][], String lm, int l, int r)
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
 			System.out.println("Player " + lm + " wins!");
 		
 		return ret;		
 	}
 	
 	//displays the table
 	private static void display(String t[][])
 	{
 		System.out.println("\n");
 		for (byte i=0; i<3; i++) 
 		{			
 			System.out.println("\n");
 			for (byte j=0; j<3; j++)
 				System.out.print(" " + t[i][j]);
 		}
 		System.out.println("\n");
 	}
 }	
