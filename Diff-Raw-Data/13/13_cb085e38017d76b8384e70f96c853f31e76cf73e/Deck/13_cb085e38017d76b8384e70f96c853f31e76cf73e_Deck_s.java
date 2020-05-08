 package game.play;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Deck {
 
 	private List<Card> deck;
 	
 	public Deck()
 	{
 		this.deck = buildCleanDeck();
 	}
 
 	private List<Card> buildCleanDeck() {
 		List<Card> deck = new ArrayList<Card>(52);
 		for(Suit suit : Suit.allSuits())
 		{
 			for(int denom = 1; denom <= 13; denom++)
 			{
 				deck.add(new Card(denom, suit));
 			}
 		}
 		return deck;
 	}
 	
 	public void shuffle()
 	{
 		Card temp;
 		int j;
 		for(int i=0;i<deck.size();i++)
 		{
 			j = (int)(Math.random()*i);
 			temp = deck.get(i);
 			deck.set(i, deck.get(j));
 			deck.set(j, temp);
 		}
 	}
 	
 	public List<Card> getDeck()
 	{
 		return deck;
 	}
 	
 	public Card peekCard()
 	{
 		return this.peekCard(0);
 	}
 	
 	public Card peekCard(int index)
 	{
 		return deck.get(index);
 	}
 	
 	public Card drawCard()
 	{
 		return this.drawCard(0);
 	}
 	
 	public Card drawCard(int index)
 	{
		return deck.get(index);
 	}
 	
 }
