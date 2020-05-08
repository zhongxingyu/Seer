 package aceofspades;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Player1os <player1os at gmail.com>
  */
 public class Application {
 
     /**
      * @param args the command line arguments
      */
     private static ArrayList<CardSet> CardSets;
     private static ArrayList<Card> Cards;
     
     public static LoadScript lsGame;
 
     public static void main(String[] args) {       
         Cards = new ArrayList<Card>();
         CardSets = new ArrayList<CardSet>();
         
         aceofspades.frames.Frame frame = new aceofspades.frames.Frame();
     }    
     
     public static void createCard(String _value, String _suit, int x, int y, int cardset, int position) {
         Card c = new Card(_value, _suit, x, y, CardSets.get(cardset), position);        
         CardSets.get(cardset).addCard(position, c);
     }
     
     public static int createCardSet(int x, int y, String name) {
         CardSets.add(new CardSet(x, y, name));
         return CardSets.size() - 1;
     }
     
     public static void removeCardSet(int id) {
         CardSets.remove(id);
     }
     
     public static CardSet getCardSet(int id) {
         return CardSets.get(id);
     }
 
     public static int getCardSetSize() {
         return CardSets.size();
     }    
     
 }
