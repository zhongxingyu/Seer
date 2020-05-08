 /* Play_Pile.java
  * Purpose: Play_Pile will be the implementation of the seven piles on the playing field for Solitaire that the user will mostly interact with.
  */
 
 public class Play_Pile extends Piles {
     
     public Play_Pile(int size_of_down, int size_of_up, int x_of_up, int y_of_up, int x_of_down, int y_of_down) {
         super(size_of_down, size_of_up, x_of_up, y_of_up, x_of_down, y_of_down);
     }
     
     public Play_Pile(int size_of_up, int x_of_up, int y_of_up, int x_of_down, int y_of_down) {
         super(size_of_up, x_of_up, y_of_up, x_of_down, y_of_down);
     }
     
     /* boolean isValidMove(Card endpoint)
     * 
     * Purpose: isValidMove() is a check to see if the card that the player wants to move
     *          to the one of the seven main piles is a legal move in the game of Solitaire.
     * 
     * Procedure: isValidMove() is given the pile where the player wants to move the card and the card the player is trying to move.
     *            First, it checks to see if the there are no cards in the face-down pile or face up pile of where the user wants to move it and the card being moved is a King.
     *            Next, it checks to see if the colors of the top of the stack's card and the card being moved are different.
     *            Finally, the values are compared. The card being moved has to be lower than the top's card by one.
     */
 
     public boolean isValidMove( Card moved ) {
         if ( !this.isEmpty('u') ) {
            if ( this.isEmpty('d') && this.isEmpty('u') && moved.value == 13)
                return true;
             if ( this.up[this.top_of_up].color != moved.color && this.up[this.top_of_up].value == ( moved.value + 1 ))
                 return true;
         }
         return false;
     }
     
     /* void move_card(Piles endpoint)
      * Purpose: move_card() is the function that will properly move the card(s) from one pile to another if the move is valid.
      * 
      * Procedure: move_card() will recieve a stack of cards that the user wants to move--which can include multiple cards or just one.
      *            isValidMove() will be called to check and make sure it's a valid move.
      */
     
     public boolean move_card( Piles moved ) {
         if ( this.isValidMove( moved.up[0] ) ) {
            
             int size = moved.capac_of_up;
            
            Piles temp = new Piles(size);
            
            for (int i = 0; i < size; i++ ){
                temp.push(moved.pop('u'), 'u');
            }
            
             for (int i = 0; i < size; i++) {
                this.push(temp.pop('u'), 'u');
             }
            
             return true;
         }
         
         else {
             //play sound
             return false;
         }
     }
 }
