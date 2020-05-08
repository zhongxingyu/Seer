 /*Source of how many cards in the deck: http://www.squidoo.com/how-many-cards-in-a-deck */
 
 package practiceUno; 
 
 import java.util.*; 
 
 /**
  *
  * @author mike
  */
 public class Deck
 {
     enum deckType {DEAL, DISCARD}; 
     
     private final int MAX_CARD = 108; 
     private Stack<Card> deck = new Stack<Card>();  
     private Stack<Card> discardDeck = new Stack<Card>();
     private deckType whichDeck;
     
     /**
      *
      */
     public Deck()
     {   
        CreateNumbers(); 
        CreateSpecial(); 
        CreateWild(); 
     }
     
     public void Shuffle()
     { 
         try
         {
             Collections.shuffle(deck);
         }
         catch(Exception ex)
         {
         }
        
     }
     
     private void AddToDeck(Card c, int numberOfCards)
     {
         for(int i = 0; i < numberOfCards; i++)
             deck.push(c);
     }
      
     private void CreateNumbers()
     {
         for(Card.cardColor color : Card.cardColor.values())
         {
                 AddToDeck( new NumberCard (0, color), 1);
                 for (int i = 1; i < 10; i++) 
                     AddToDeck( new NumberCard (i, color), 2);
         }      
     }
     
     private void CreateSpecial()
     {
         for(SpecialCard.cardValues sp : SpecialCard.cardValues.values())
         {
                 for(Card.cardColor color : Card.cardColor.values())
                 {
                     AddToDeck(new SpecialCard(sp, color), 2);
                 }
         }
     }
     
     private void CreateWild()
     {
         for(WildCard.cardWild wild : WildCard.cardWild.values())
             AddToDeck(new WildCard(wild), 4);
     }
 
     /**
      *
      * @return
      */
     public Card DrawNext()
     {
         Card c; 
         if(!deck.isEmpty())
             return (Card)deck.pop();
         else
             System.out.println("This deck is done. Need to shuffle and replace deck.");
         return null;
 
     }
     
     /**
      *
      * @param c
      */
     public void AddDiscard(Card c, Player play)
     {
        boolean canPlace = false; 
        Card discard = discardDeck.peek();
        
        if(c.getClass().equals(NumberCard.class))
        {
            canPlace = CheckNumber(discard, c); 
        }
        else if(c.getClass().equals(SpecialCard.class))
        {
            canPlace = CheckSpecial(discard, c);  
        }
        else if(c.getClass().equals(WildCard.class))
        {
            canPlace = CheckWild(discard, c, play); 
        }
        else
        {
            System.out.println("No matching cards available");
        }
        if(canPlace)
         discardDeck.push(c);
 
     }
     
     private boolean CheckNumber(Card discard, Card c)
     {
         boolean canPlace = false; 
         if(discard.getClass().equals(NumberCard.class))
         {
                NumberCard pushNum = (NumberCard) c; 
                NumberCard topCard = (NumberCard) discard; 
                
                 System.out.println("On Top: "  + topCard.GetColor() + " " + topCard.GetNumber());
             if(pushNum.GetColor().equals(topCard.GetColor()) )
             {
                 System.out.println("Match on color");
                 canPlace = true;
             }
             else if(pushNum.GetNumber() == topCard.GetNumber())
             {
                 System.out.println("Match in number");
                 canPlace = true; 
             }
          }
          System.out.println("Number Card Played");
          return canPlace; 
     }
     
     private boolean CheckSpecial(Card discard, Card c)
     {
         boolean canPlace = false; 
         
         if(discard.getClass().equals(SpecialCard.class))
         {
             System.out.print("On Top: ");
             discardDeck.peek().Print();
             System.out.println("");
             
             SpecialCard inPlay = (SpecialCard)c; 
             SpecialCard top = (SpecialCard)discard;
             
             if(c.GetColor().equals(discard.GetColor()))
             {
                 canPlace = true; 
             }
             else if(inPlay.GetSpecial().equals(top.GetSpecial()))
             {
                 canPlace = true; 
             }
         }
         System.out.println("Special Card Played.");
         return canPlace; 
     }
     
     private boolean CheckWild(Card discard, Card c, Player thisPlayer)
     {
         boolean canPlace = true; 
         
         WildCard wild = (WildCard)c;
         
         if(wild.GetWild().equals(WildCard.cardWild.WILD))
         {
             wild.Wild(new Scanner(System.in));
         }
         else if(wild.GetWild().equals(WildCard.cardWild.WILDDRFOUR))
         {
           wild.DrawFour(thisPlayer, this, new Scanner(System.in));
         }
         
         
         return canPlace; 
     }
     
     /**
      *
      * @param deckName
      * @return
      */
     public int getSize(String deckName)
     {
        int size = 0; 
        if(deckName.equals("discard"))
        {
            size = discardDeck.size(); 
        }
        else if(deckName.equals("regular"))
        {
            size = deck.size(); 
        }
         return size; 
     }
     
     /**
      *
      */
     public void ShowDiscard()
     {
         discardDeck.peek().Print();
         System.out.println("");
     }
     
     /**
      *
      */
     public void SetUpDiscard()
     {
         discardDeck.add(DrawNext());
     }
 
     
     /**
      *
      */
     public void PrintDeck(String deckName)
     {  
         Stack<Card> tmpDeck = new Stack<Card>(); 
         if(deckName.equals("discard"))
             tmpDeck = discardDeck; 
         else if(deckName.equals("regular"))
             tmpDeck = deck; 
         
         for(Card c : tmpDeck)
         {
             c.Print();
             System.out.println("");
         }
     }
     
     /**
      *
      * @return
      */
     public Stack<Card> GetDeck() 
    {
        return deck; 
    }
    
     
     /**
      *
      * @param special
      * @param p
      */
     public void SideEffect(SpecialCard special, ArrayList<Player> players, int pos)
   {
       //TODO: cShould check for the Wild Side effect as well
       switch(special.GetSpecial())
       {
           case SKIP: special.Skip(pos, players);
               break; 
           case REVERSE: special.Reverse(pos, players);
               break;
          case DRTWO: special.DrawTwo(players.get(pos + 1 ), this);
               break;
       }
   }
    
 }
