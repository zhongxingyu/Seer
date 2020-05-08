 package groupproject.wargame;
 
 // -------------------------------------------------------------------------
 /**
  * The data representation for a playing card.
  *
  * @author Matthew Bock (mattb93)
  * @author Kevin Olson (kevino93)
  * @author John Sebestin (jseb11)
  * @author Sonika Singh (sonika06)
  * @version Nov 14, 2012
  */
 public class Card
 {
    private String graphic;
 
     private int    value;
 
     private int    suit;
     /**
      * 0 is clubs, 1 is hearts, 2 is diamonds, 3 is spades
      */
 
     private int    type;
 
 
     /**
      * 2 through 10 are normal cards, 11 is joker, 12 is queen, 13 is king, 14
      * is ace
      */
 
     /**
      * Create a new Card object.
      *
      * @param suit
      *            The suit to classify the card under.
      * @param type
      *            The value the card has. Face cards are 11 for joker, 12 for
      *            queen, 13 for king, and 14 for ace.
      */
     public Card(int suit, int type)
     {
         this.suit = suit;
         this.type = type;
 
         if (1 <= type && type <= 9)
         {
             this.value = type;
         }
         if (10 <= type && type <= 13)
         {
             this.value = 10;
         }
         if (type == 14)
         {
             this.value = 11;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Returns the integer representation of this card's suit. 0 is clubs, 1 is
      * hearts, 2 is diamonds, 3 is spades.
      *
      * @return the card's suit value.
      */
     public int suitValue()
     {
         return suit;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Returns the card's suit.
      *
      * @return the card's suit as a string.
      */
     public String suit()
     {
         if (suit == 0)
         {
             return "clubs";
         }
 
         else if (suit == 1)
         {
             return "hearts";
         }
 
         else if (suit == 2)
         {
             return "diamonds";
         }
 
         else
         {
             return "spades";
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * The number of points that the card is worth.
      *
      * @return the card's integer value.
      */
     public int value()
     {
         return this.value;
     }
 
 
     // ----------------------------------------------------------
     /**
      * What kind of card this is.
      *
      * @return the type of card represented as an integer.
      */
     public int type()
     {
         return this.type;
     }
 
 
     // ----------------------------------------------------------
     /**
      * If the card is a face card, it will return the card's face type as a
      * string. Otherwise, an exception will be thrown.
      *
      * @return the card's face value.
      */
     public String faceValue()
     {
         if (type == 11)
         {
             return "jack";
         }
 
         else if (type == 12)
         {
             return "queen";
         }
 
         else if (type == 13)
         {
             return "king";
         }
 
         else if (type == 14)
         {
             return "ace";
         }
 
         else
         {
             return "Not a face card";
         }
     }
 
 
     public String toString()
     {
         String result = "";
 
         if (type < 11)
         {
             result = result + type;
         }
         else
         {
             result = result + faceValue();
         }
 
         result = result + " of ";
         result = result + suit();
 
         return result;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Switches between high and low ace values.
      */
     public void toggleAce()
     {
         if (value == 11)
         {
             value = 1;
         }
 
         else if (value == 1)
         {
             value = 11;
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Returns the name of the image corresponding to this card.
      *
      * @return the name of this card's image.
      */
     public String toGraphic()
     {
         String result = "";
 
         result = suit() + type;
 
         return result;
     }
 }
