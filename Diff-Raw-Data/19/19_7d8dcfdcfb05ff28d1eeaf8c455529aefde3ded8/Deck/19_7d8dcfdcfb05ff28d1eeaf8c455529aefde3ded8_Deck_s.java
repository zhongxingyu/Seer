 import java.util.Collections;
 import java.util.Stack;
 
 /**
  * 
  */
 
 /**
  * @author Khaleeq Ahmad
  * 
  */
 public class Deck //extends CardStack
 {
     static Stack<Card> d = new Stack<Card>();
 
     Deck()
     {
 	if (!d.isEmpty())
 	{
 	    clear();
 	}
 
 	buildDeck();
 
 	shuffle();
 
     }
 
     /**
      * 
      */
     private static void buildDeck()
     {
 
 	for (Suit suit : Suit.values())
 	{
 	    for (Rank rank : Rank.values())
 	    {
 		//System.out.println(suit + " " + rank);
 		add(new Card(suit, rank));
 	    }
 	}
 
	System.out.println(d);
 
     }
 
     public static Card peek()
     {
 	return d.peek();
     }
 
     public static Card draw()
     {
 	return d.pop();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see CardStack#place(Card)
      */
     //    @Override
     public static void add(Card c)
     {
 	d.addElement(c);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see CardStack#shuffle()
      */
     //    @Override
     public static void shuffle()
     {
 	Collections.shuffle(d);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see CardStack#clear()
      */
     // @Override
     public void clear()
     {
 	d.clear();
     }
 
     /**
      * @return
      */
     public int size()
     {
 	// TODO Auto-generated method stub
 	return d.size();
     }
 
     //        public static void main(String[] args)
     //        {
     //    	buildDeck();
     //    	System.out.println("After build" + d.size());
     //    
     //    	Card c = draw();
     //    	System.out.println("After store draw" + d.size());
     //    
     //    	System.out.println(c);
     //    	System.out.println("After print draw" + d.size());
     //    
     //    	add(c);
     //    	System.out.println("After place back" + d.size());
     //    
     //    	System.out.println(peek());
     //    	System.out.println("After peek" + d.size());
     //    
     //    	shuffle();
     //    	System.out.println("Shuffle: " + peek());
     //    	System.out.println("Shuffle size" + d.size());
     //    
     //    	shuffle();
     //    	System.out.println("Shuffle: " + draw());
     //    	System.out.println("Shuffle size" + d.size());
     //        }
 
     public static void main(String[] args)
     {
 	buildDeck();
     }
 
 }
