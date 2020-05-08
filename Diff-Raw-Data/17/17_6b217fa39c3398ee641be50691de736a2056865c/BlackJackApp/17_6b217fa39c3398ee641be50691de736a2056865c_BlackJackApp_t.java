 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class BlackJackApp
 {
 
 	/**
 	 * @param args
 	 */
 	private static You player;
 	private static Opponent opponent;
 	private static Scanner sc;
 	private static Deck deck;
 	private static int rounds;
 	public static void main(String[] args) 
 	{
 		// TODO Auto-generated method stub
 		rounds = 0;
 		deck = new Deck();
 		sc = new Scanner(System.in);
 		player = new You();
 		opponent = new Opponent();
 		
 		//System.out.println(deck.toString());
 		//deck.shuffle();
 		System.out.println("---Alexandre's Casino!---");
 		
 		do
 		{
 			System.out.println("New Round\n");
 			opponent.hand = new ArrayList<Hand>();
 			player.hand = new ArrayList<Hand>();
 			for ( int i = 0; i < 2; i++ )
 			{
 				opponent.deal(deck, 0);
 				player.deal(deck, 0);
 			}
 			
 			while ( true )
 			{
 				for ( int s = 0; s < player.hand.size(); s++)
 				{
 					player.hand.get(s).madeFromSplit = false;
 				}
 				
				if ( opponent.getWillPlay() )
 				{
 					System.out.println("--------dealer----------------------");
 					opponent.Play(deck, rounds);
 				}
 				else
 				{
 					break;
 				}
 				
				if ( player.getWillPlay() )
 				{
 					System.out.println("--------you----------------------");
 					player.Play(deck, sc, rounds);
 				}
 				rounds++;
 			}
 			
 			for ( int c = 0; c < player.hand.size(); c++)
 			{
 				System.out.println( "\n" + player.hand.get(c).toString() );
 				if (!player.hand.get(c).hasSurrendered)
 				{
 					if ((!player.isBust(c) || (opponent.isBust(0) || player.getScore(c) >= opponent.getScore(0))) )
 					{
 						
 						if ( player.getScore(c) == opponent.getScore(0) && player.isBlackJack(c) && !opponent.isBlackJack(0))
 						{
 							System.out.println("This hand was a draw");
 							player.draw(c);
 							
 						}
 						else
 						{
 							System.out.println("you won this hand");
 							player.win(c);
 						}
 					}
 					else
 					{
 						System.out.println("dealer won this hand");
 					}
 				}
 				else
 				{
 					System.out.println("you surrendered this hand");
 				}
 			}
 			
 			rounds = 0;
 		}
 		while ( !player.Lost() );
 	}
 }
