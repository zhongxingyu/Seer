 /*
  * Author: David Duncan
  * stacks.java
  * Purpose: Stacks is a class which will be used in the game for the cards that will either be face down and/or face up.
  *          This class is intended to be used for every pile of cards that DSS uses. 
  */
 public class stacks {
     
     public int top_of_down, top_of_up;
     public int size_of_down, size_of_up;
     public int capac_of_down, capac_of_up;
     public Card [] up;
     public Card [] down;
     
     public stacks() {
         size_of_down = 6;
         size_of_up = 12;
         up = new Card[size_of_up];
         down = new Card[size_of_down];
         top_of_down = 0;
         top_of_up = 0;
         capac_of_down = 0;
         capac_of_up = 0;
         
         for(int i = 0; i < size_of_down; i++)
             down[i] = new Card();
         for(int i = 0; i < size_of_up; i++)
             up[i] = new Card();
     }
     
     public stacks(int size) {
         size_of_down = size;
         size_of_up = size;
         up = new Card[size_of_up];
         down = new Card[size_of_down];
         top_of_down = 0;
         top_of_up = 0;
         capac_of_down = 0;
         capac_of_up = 0;
         
         for(int i = 0; i < size; i++) {
             up[i] = new Card();
             down[i] = new Card();
         }
             
     }
     
     public boolean isDownEmpty() {
         return capac_of_down == 0;
     }
     
     public boolean isUpEmpty() {
         return capac_of_up == 0;
     }
     
     public boolean isUpFull() {
         return capac_of_up == size_of_up;
     }
     
     public boolean isDownFull() {
         return capac_of_down == size_of_down;
     }
     
     public Card pop_down () {
         Card tempc;
         
         if ( isDownEmpty() ) 
             return new Card();
         else {
            tempc = down[top_of_down - 1];
            top_of_down = top_of_down - 1;
            capac_of_down = capac_of_down - 1;
             return tempc;
         }
     }
     
     public Card pop_up () {
         Card tempc;
         if ( isUpEmpty() )
             return new Card();
         else {
            tempc = up[top_of_up - 1];
            top_of_up = top_of_up - 1;
            capac_of_up = capac_of_up - 1;
             return tempc;
         }
     }
     
     public void push_up (Card pushed) {
         if ( !isUpFull() ) { 
            up[top_of_up] = pushed;
            top_of_up = top_of_up + 1;
             capac_of_up++;
         }
     }
     
     public void push_down (Card pushed) {
         if ( !isDownFull() ) {
            down[top_of_down] = pushed;
            top_of_down = top_of_down + 1;
             capac_of_down++;
         }
     }
     
     public Card peek_down () {
         if ( isDownEmpty() )
             return new Card();
         else
             return down[top_of_down];
     }
     
     public Card peek_up () {
         if ( isUpEmpty() )
             return new Card();
         else
             return up[top_of_up];
     }
 }
