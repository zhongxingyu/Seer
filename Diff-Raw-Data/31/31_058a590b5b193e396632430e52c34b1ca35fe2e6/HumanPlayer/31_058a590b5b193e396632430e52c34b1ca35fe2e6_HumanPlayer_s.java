 package hearts.client.player;
 
 import hearts.server.game.Card;
 import hearts.server.game.Deck;
 import hearts.server.game.Suit;
 import hearts.server.game.Value;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class HumanPlayer extends Player {
 
 	Scanner keyboard;
 	public static void main(String[] args)
 	{
 		ArrayList<Card> a = new ArrayList<Card>();
 		ArrayList<ArrayList<Card>> b = new ArrayList<ArrayList<Card>>();
 		Deck d = new Deck();
 		
 		a.add(d.getDeck().get(0));
 		b.add(a);
 		a.remove(0);
 		
 		System.out.println(("spads".equals(Suit.SPADES)));
 		
 	}
 	
 	public HumanPlayer()
 	{
 		this("");
 		
 	}
 	
 	public HumanPlayer(String name)
 	{
 		super(name);
 		keyboard = new Scanner(System.in);
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 	}
 	public void kill()
 	{
 		keyboard.close();
 	}
 
 	@Override
 	public void playCardPrompt() {
 		System.out.println();
 		System.out.println(printHand());
 		
 		chooseSuit();
 		
 	}
 	
 	private void chooseSuit()
 	{
 		Suit leadSuit = myServer.getGame().getTable().getLeadSuit();
 		boolean chosen = false;
 		
 		if(leadSuit != null)
 		{
 			
 			//If has lead suit skip to choosing suit
 			if(hasSuit(leadSuit))
 			{
 				System.out.println("Lead suit is " + leadSuit.toString());
 				if(leadSuit == Suit.CLUBS)
 					chooseCard(0);
 				if(leadSuit == Suit.DIAMONDS)
 					chooseCard(1);
 				if(leadSuit == Suit.SPADES)
 					chooseCard(2);
 				if(leadSuit == Suit.HEARTS)
 					chooseCard(3);
 				chosen = true;
 			}
 			else
 			{
 				System.out.println("You don't have lead suit!");
 			}
 		}
 		else
 		{
 			//If there is no lead suit
 			System.out.println("You're leading.");
 			if(have2Clubs())
 			{
 				playCard(handBySuit.get(0).get(0));
 				chosen = true;
 				System.out.println("\n***You had the 2 of Clubs. It's been played for you.***\n");
 			}
 		}
 		
 		// If suit hasn't been chosen yet
 		while(!chosen)
 		{
 			System.out.print("Choose your suit: ");
 			String suit = keyboard.next();
 			
 			suit = suit.trim();
 			suit = suit.toLowerCase();
 			
 			if(suit.equals("clubs"))
 			{
 				if(clubs.size() == 0)
 				{
 					System.out.println("You're out of that suit");
 				} else {
 					//choose card
 					chooseCard(0);
 					chosen = true;
 				}
 			} 
 			else if(suit.equals("diamonds"))
 			{
 				if(diamonds.size() == 0)
 				{
 					System.out.println("You're out of that suit");
 				} else {
 					//choose card
 					chooseCard(1);
 					chosen = true;
 				}
 			}
 			else if(suit.equals("spades"))
 			{
 				if(spades.size() == 0)
 				{
 					System.out.println("You're out of that suit");
 				} else {
 					//choose card
 					chooseCard(2);
 					chosen = true;
 				}
 			}
 			else if(suit.equals("hearts"))
 			{
 				
 				//TODO handle hearts not being broken but having only hearts in hand and leading
 				if(myServer.getGame().getRoundsPlayed() == 0)
 				{
 					System.out.println("Can't play points on first round!");
 				}
 				else if(hearts.size() == 0)
 				{
 					System.out.println("You're out of that suit");
 				}
 				else if(!myServer.getGame().getHeartsBroken())
 				{
					System.out.println("Hearts haven't been broken yet!");
 				}
 				else
 				{
 					//choose card
 					chooseCard(3);
 					chosen = true;
 				}
 			}
 			else
 			{
 				System.out.println("That's not a suit!");
 			}
 			System.out.println();
 		}
 	}
 	
 	private void chooseCard(int suitNum)
 	{
 		boolean played = false;
 		while(!played)
 		{
 			if(suitNum == 0)
 				System.out.println(printClubs());
 			else if(suitNum == 1)
 				System.out.println(printDiamonds());
 			else if(suitNum == 2)
 				System.out.println(printSpades());
 			else if(suitNum == 3)
 				System.out.println(printHearts());
 			else
 				System.out.println("Invalid suit num chosen!");
 
 			System.out.print("Choose the index of the card (starting with 1): ");
 
 			int index = 0;
 			if (keyboard.hasNextInt()) {
 				index = keyboard.nextInt();
 			} else {
 				keyboard.next(); //garbage step to process line and move on
 			}
 
 			if(index < 1 || index > handBySuit.get(suitNum).size())
 			{
 				System.out.println("Invalid index\n");
 			}
 			else
 			{
 				// check if trying to play the queen of spades on first round
 				if(suitNum == 2 && handBySuit.get(suitNum).get(index-1).getValue() == Value.QUEEN && myServer.getGame().getRoundsPlayed() == 0)
 				{
 					System.out.println("Can't play points on the first round. Even the Queen!");
 				}
 				else
 				{
 					playCard(handBySuit.get(suitNum).get(index-1));
 					played = true;
 				}
 			}
 		}
 	}
 	
 	
 }
