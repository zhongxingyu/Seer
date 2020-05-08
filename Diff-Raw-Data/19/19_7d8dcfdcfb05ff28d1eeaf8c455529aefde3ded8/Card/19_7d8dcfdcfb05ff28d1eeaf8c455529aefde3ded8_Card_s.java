 import java.util.HashMap;
 import java.util.Hashtable;
 
 /**
  * 
  */
 
 /**
  * @author Khaleeq Ahmad
  *
  */
 public class Card
 {
    static Suit suit;
    static Rank rank;
     
 //    static HashMap<Rank, Integer> ranks = new HashMap<Rank, Integer>(13);
     
 //    Colour colour;
     
     Card(Suit suit, Rank rank)	
     {
 //	fillRanks();
 	
 	this.suit = suit;
 	this.rank = rank;
 	  
     }
     
 //    public static void fillRanks()
 //    {
 //	ranks.put(Rank.ace, 1);
 //	ranks.put(Rank.two, 2);
 //	ranks.put(Rank.three, 3);
 //	ranks.put(Rank.four, 4);
 //	ranks.put(Rank.five, 5);
 //	ranks.put(Rank.six, 6);
 //	ranks.put(Rank.seven, 7);
 //	ranks.put(Rank.eight, 8);
 //	ranks.put(Rank.nine, 9);
 //	ranks.put(Rank.ten, 10);
 //	ranks.put(Rank.jack, 10);
 //	ranks.put(Rank.queen, 10);
 //	ranks.put(Rank.king, 10);
 //    }
     
     public String toString()
     {
 	return rank + " of " + suit;
     }
 
    public static Suit getSuit()
     {
         return suit;
     }
 
     public void setSuit(Suit suit)
     {
         this.suit = suit;
     }
 
    public static Rank getRank()
     {
         return rank;
     }
 
     public void setRank(Rank rank)
     {
         this.rank = rank;
     }
 
 }
