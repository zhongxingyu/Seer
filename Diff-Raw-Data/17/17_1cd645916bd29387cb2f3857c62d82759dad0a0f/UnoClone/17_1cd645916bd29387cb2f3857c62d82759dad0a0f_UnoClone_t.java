 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 /**
  *
  * @author mike
  */
 public class UnoClone {
    /**
     *
     * @param args
     */
     public static void main(String[] args)
     { 
         Player p = new Player(); 
         Player p2 = new Player(); 
         Dealer d = new Dealer(); 
         Deck deck = new Deck(); 
         
         d.Shuffle(deck);
         for (int i = 0; i < 7; i++) {
             p.GetCard(d.Deal(deck));
             p2.GetCard(d.Deal(deck));
         }
         p.ShowHand();
         System.out.println("");
         p2.ShowHand();
         
         System.out.println("Removeing the third card");
         deck.AddDiscard(p2.Discard(2)); 
         System.out.println(p2.TotalCards());
         p2.ShowHand();
         System.out.println(deck.getSize("discard"));
         deck.PrintDeck("discard");
         
     }
 }
