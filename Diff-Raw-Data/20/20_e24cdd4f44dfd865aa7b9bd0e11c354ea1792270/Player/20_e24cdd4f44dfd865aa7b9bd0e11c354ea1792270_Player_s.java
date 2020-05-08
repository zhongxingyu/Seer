 /**
  * 
  */
 
 /**
  * @author Khaleeq Ahmad
  * 
  */
 public class Player
 {
     static String name;
     int score;
     static Hand hand = new Hand();
 
     Player(String name)
     {
 	this.name = name;
     }
 
     public String toString()
     {
 	return name + ": " + hand;
     }
 
     public static void pickUp(Deck d, int n)
     {
 	for (int i = 0; i <n; i++)
 	{
	    System.out.println("--------------");
 	    System.out.println(i);
 	    System.out.println("pre D:" + d.size() + " H: " + hand.size());
 	    System.out.println(d.view());
 	    System.out.println(hand.view());
 	    System.out.println(d.peek(0));
 	    Card top = d.draw();
	    hand.add(top);
 	    System.out.println(d.peek(0));
 
 	    
 //	    System.out.println("d"+top+d.size());
 	    System.out.println("post D:"+d.size() + " H: " + hand.size());
 	    System.out.println(d.view());
 	    System.out.println(hand.view());
 	    
 	    System.out.println(top + " has been added to " + name + "'s hand.");
 
 
 	    Rank thisRank = top.rank;
 	    RankMap map = Main.rankmap;
 	    int value = map.getRank(thisRank);
 	    hand.raiseValue(value);
 	    
 	    //if (value > 21) && hand.contains(Ace)
 	    //	value = value - 10;
 	    
 	}
 	
 	viewHand();
     }
 
     public static void viewHand()
     {
 	System.out.println(name + "'s hand: " +hand.view());
     }
     
     public void hit(Deck d)
     {
 	if (hand.value < 21)
 	    pickUp(d, 1);
 	else
 	    lost();
     }
     
 
 
     /**
      * 
      */
     private static void lost()
     {
 	System.out.println(name + "'s hand has reached " + hand.value);
 	System.out.println(name + " has lost the game.");
     }
 
     private static void won()
     {
 	if (hand.value == 21)
 	{
 	    System.out.println(name + "'s hand has reached " + hand.value);
 	}
 	System.out.println(name + " has won the game.");
     }
 
     
 
     public static void main(String[] args)
     {
 	Deck d =new Deck();
 	//d.shuffle();
 
	pickUp(d, 52);
 	System.out.println(hand.view());
 	System.out.println(hand.contains(Rank.ace));
     }
 }
