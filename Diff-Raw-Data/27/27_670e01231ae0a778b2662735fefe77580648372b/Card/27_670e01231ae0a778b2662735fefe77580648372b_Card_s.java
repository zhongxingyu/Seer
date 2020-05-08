 public class Card {
 
     public enum Suit {
         HEARTS,
         DIAMONDS,
         CLUBS,
         SPADES
     }
 
 	private int m_value;
 	private Suit m_suit;
 
 	public Card(int value, Suit suit) {
 		m_value = value;
 		m_suit = suit;
 	}
 
     @Override
     public String toString() {
         String retVal;
 
         switch(m_value) {
             case 11:
                 retVal = "J";
                 break;
             case 12:
                 retVal = "Q";
                 break;
             case 13:
                 retVal = "K";
                 break;
            case 0:
                 retVal = "A";
                 break;
             default:
                 retVal = "" + m_value;
                 break;
         }
 
         switch(m_suit) {
             case DIAMONDS:
                 retVal += "\u2666";
                 break;
             case HEARTS:
                 retVal += "\u2665";
                 break;
             case CLUBS:
                 retVal += "\u2663";
                 break;
             case SPADES:
                 retVal += "\u2660";
                 break;
         }
 
         return retVal;
     }
 }
