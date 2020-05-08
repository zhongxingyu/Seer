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
 
 public class SpecialCard extends Card implements SpecialActions
 {
    enum cardValues {REVERSE,SKIP,DRTWO} 
    cardValues sp; 
 
    
    public SpecialCard(cardValues newSp, cardColor newColor)
    {
        sp = newSp; 
        color = newColor; 
    }
    
     /**
      *
      */
     @Override
    public void Print()
    {
        System.out.print(" " + sp + " " + color);
    }
     
     public Card.cardColor GetColor()
     {
         return color; 
     }
     
     public cardValues GetSpecial()
     {
         return sp; 
     }
     
    @Override
     public int Skip(int currentPlayerIndex, ArrayList<Player> players)
     {
         int index = 0; 
         if((currentPlayerIndex-1) < 0)
             index  = (players.size() +2);
         else if(currentPlayerIndex == (players.size()-1) )
             index = 1;
         else
             index = (currentPlayerIndex +2); 
         return index; 
         
     }
     
    @Override
     public int Reverse(int currentPlayerIndex, ArrayList<Player> players)
     {
         int index = 0; 
         
         if((currentPlayerIndex-1) < 0)
             index  = (players.size()-1);
         else if(currentPlayerIndex == (players.size()-1) )
             index = 0;
         else
             index = (currentPlayerIndex -1); 
         return index; 
     }
     
    public void DrawTwo(ArrayList<Player> players, Deck theDeck, int pos)
     {
          //Need a way to handle matching colors as well. 
          for (int i = 0; i < 2; i++) 
          {
            players.get(pos +1).GetCard(theDeck.DrawNext());   
          }
     }
 }
