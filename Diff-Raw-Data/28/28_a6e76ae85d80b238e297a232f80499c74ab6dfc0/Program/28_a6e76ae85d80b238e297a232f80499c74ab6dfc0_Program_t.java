 package is.marshmallow;
 import java.io.*;
 import java.lang.*;
 import java.util.Scanner;
 public class Program
 {
 	public static void main(String[] args)
 	{
 		String player1 = "";
 		String player2 = "";
 		String lastPlayer = "";
 		System.out.print("Choose a name for player 1: ");
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         try {
             player1 = in.readLine();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         System.out.print("Choose a name for player 2: ");
         try {
             player2 = in.readLine();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         TicTacToe t = new TicTacToe(player1, player2);
 
 		System.out.println(t.getBoard());
 
 		while (!t.isWinner)
 		{
 			lastPlayer = t.currentPlayer;
 			System.out.println("It is " + t.currentPlayer + "'s turn ");
 			System.out.print("Select a number: ");
             try {
                 String number = in.readLine();
                 int value = Integer.parseInt(number);
 
                 t.placeMarker(value, t.currentMarker);
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             System.out.println(t.getBoard());
 			if (t.checkWinner())
 			{
 				System.out.println("Congrats " + lastPlayer);
 			}
 			else if (t.checkDraw())
 			{
 				System.out.println("It is a tie");
 			}
 		}
 
         try {
             in.readLine();
         } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
}
