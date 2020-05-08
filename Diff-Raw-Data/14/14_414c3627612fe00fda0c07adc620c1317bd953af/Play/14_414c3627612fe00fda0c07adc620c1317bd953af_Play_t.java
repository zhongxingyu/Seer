 package model;
 
 import java.util.Collections;
 import java.util.List;
 
 public class Play
 {
     private final int playerID;
     private final List<Card> cards;
 
     public Play(int playerID, List<Card> cards)
     {
         if (cards.isEmpty())
             throw new IllegalStateException("Play must have at least one card");
 
         this.playerID = playerID;
         this.cards = Collections.unmodifiableList(cards);
     }
 
     public int getPlayerID()
     {
         return playerID;
     }
 
    @Override
    public String toString()
    {
        return "Play [playerID=" + playerID + ", cards=" + cards + "]";
    }

     public List<Card> getCards()
     {
         return cards;
     }
 
     public int numCards()
     {
         return cards.size();
     }
 
     public int numPoints()
     {
         int numPoints = 0;
         for (Card card : cards)
             switch (card.value)
             {
                 case FIVE:
                     numPoints += 5;
                     break;
                 case TEN:
                     numPoints += 10;
                     break;
                 case KING:
                     numPoints += 10;
                     break;
                 default:
                     break;
             }
         return numPoints;
     }
 
 }
