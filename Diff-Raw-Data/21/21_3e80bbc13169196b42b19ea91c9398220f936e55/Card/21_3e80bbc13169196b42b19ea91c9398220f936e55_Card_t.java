 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mas;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author jelmer
  */
 public class Card {
     public enum Type {
         ACE, KING, QUEEN, JACK 
     }
     
     public enum Suit {
         DIAMONDS, CLUBS, HEARTS, SPADES
     }
     
     private Image image;
     
     private Type type;
     
     private Suit suit;
     
     public Card(Type type, Suit suit)
     {
         this.type = type;
         this.suit = suit;
     }
     
     public Type getType()
     {
         return type;
     }
     
     public Suit getSuit()
     {
         return suit;
     }
     
     public final String getCode()
     {
         String typeName = "";
         String suitName = "";
         
         switch (type)
         {
             case ACE:
                 typeName = "A";
                 break;
             case KING:
                 typeName = "K";
                 break;
             case QUEEN:
                 typeName = "Q";
                 break;
             case JACK:
                 typeName = "J";
                 break;
         }
         
         switch (suit)
         {
             case DIAMONDS:
                 suitName = "D";
                 break;
             case CLUBS:
                 suitName = "C";
                 break;
             case HEARTS:
                 suitName = "H";
                 break;
             case SPADES:
                 suitName = "S";
                 break;
         }
         
         return typeName + suitName;
     }
     
     public Image getImage()
     {
        if (image == null) {
            File imageFile = new File("../Images/" + getCode() + ".png");
        
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e) {
                System.err.println(e.toString() + imageFile.toString());
                // do something intelligent.
            }
        }
        
         return image;
     }
     
     static public ArrayList<Card> getDeck()
     {
         ArrayList<Card> deck = new ArrayList<Card>();
         
         for (Type type : Type.values())
             for (Suit suit : Suit.values())
                 deck.add(new Card(type, suit));
         
         return deck;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (getClass() != obj.getClass())
             return false;
         
         final Card other = (Card) obj;
         
         return getType().equals(other.getType())
             && getSuit().equals(other.getSuit());
     }
 
     @Override
     public int hashCode()
     {
         int hash = 3;
         hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
         hash = 89 * hash + (this.suit != null ? this.suit.hashCode() : 0);
         return hash;
     }
     
     @Override
     public String toString()
     {
         return type + " of " + suit;
     }
 }
