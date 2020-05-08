 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author mike
  */
 public interface SpecialActions {
     
     int Skip(int currentPlayerIndex, ArrayList<Player> players); //Skips the player. player index plus 1
     int Reverse(int currentPlayerIndex, ArrayList<Player> players); //Decrement player index by 1
    void DrawTwo(ArrayList<Player> p, Deck d, int pos); // Player plus 1 and draw two. 
    
 }
