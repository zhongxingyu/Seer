 /* DSS - Dave's Simple Solitaire 
  * Objective: To create a playable version of a simple solitaire card game.
  * 
  */
 
 import java.util.Arrays;
 import java.util.Collections;
 
 public class DSS {
     
     public static final int MAIN_SIZE = 7;
     public static final int ACE_SIZE = 4;
     public static final int DECK_SIZE = 52;
     
     public static void main(String[] args) {
         
         Card [] shuffle = new Card [DECK_SIZE];
         int value;
         char suit;
         char color;
         
         
         // This for loop is for populating a deck in order to populate the playing field 
         for( int i = 0; i < DECK_SIZE; ++i ) {
             value = (i % 13) + 1;
             if(  i < 26 ) {
                 color = 'R';
                 if ( i < 13 ) suit = 'D';
                 else suit = 'H';
             }
             else {
                 color = 'B';
                 if ( i < 39 ) suit = 'C';
                 else suit = 'S';
             }
             shuffle[i] = new Card ( value, color, suit );
         }
         
         
         // From the deck 'shuffle', I'm converting it to a list to use the Collections function shuffle()
         // in order to shuffle the deck before I deal the cards onto the playing field.
         Collections.shuffle( Arrays.asList( shuffle ) );
                 
         // main_stacks are the seven main piles of cards the player interacts with.
         stacks [] main_stacks = new stacks [MAIN_SIZE];
         for (int i = 0; i < MAIN_SIZE; i++) {
             main_stacks[i] = new stacks();
         }
         
         
         /* 
         *  Populating the 7 piles laid out on the playing field.
         *  This loops deals the very first pile with a face up card followed by the rest of the piles with face down.
         *  If this loop works correctly, the first stack should only have a face up card and none under them while the 
         *  last pile should have 6 cards faced down under it with one face up card.
         */
         int count = 0;
         for( int i = 0; i < MAIN_SIZE; i++ ) {
             main_stacks[i].push_up(shuffle[count]);
             count++;
             for ( int k = i + 1; k < MAIN_SIZE; k++ ) {
                 main_stacks[k].push_down(shuffle[count]);
                 count++;
             }
         }
         
         /*
          * This array is for the piles of aces where the player will place the cards to win.
          */
         stacks [] ace_stacks = new stacks[ACE_SIZE];
         for ( int i = 0; i < ACE_SIZE; i++ )
             ace_stacks[i] = new stacks();
         
         //This stack is for the piles of cards where the user draws three from the rest of the deck.
         stacks draw_stack = new stacks(DECK_SIZE - count);
         
         //This temp stack is to correct the conversion to the draw_stack
         stacks temp = new stacks( DECK_SIZE - count );
         
         for(int i = 0; i < ( DECK_SIZE - count); i++ ) {
             temp.push_down(shuffle[i + count]);
         }
         
         for(int i = 0; i < ( DECK_SIZE - count); i++ ) {
             draw_stack.push_down(temp.pop_down());
         }
         
         //Main game loop
         //while( !game_over(ace_stacks) ) {
             
         //}
         
     }
     
     
     /* 
     * boolean isValidMove(Card moved, Card endpoint)
     * 
     * Purpose: isValidMove() is a check to see if the card that the player wants to move
     *          to the one of the seven main piles is a legal move in the game of Solitaire.
     * 
     * Procedure: isValidMove() is given the pile where the player wants to move the card and the card the player is trying to move.
     *            First, it checks to see if the there are no cards in the face-down pile or face up pile of where the user wants to move it and the card being moved is a King.
     *            Next, it checks to see if the colors of the top of the stack's card and the card being moved are different.
     *            Finally, the values are compared. The card being moved has to be lower than the top's card by one.
     */
 
     public boolean isValidMove(Card moved, stacks endpoint) {
         
         if ( endpoint.isDownEmpty() && endpoint.isUpEmpty() && moved.value == 13)
             return true;
         if ( moved.color == endpoint.up[endpoint.top_of_up].color )
             return false;
         if ( moved.value != (endpoint.up[endpoint.top_of_up].value - 1) )
             return false;
         
         return true;
     }
     
     /* 
     * boolean isValidAceMove(Card moved, Card endpoint)
     * Purpose: isValidAceMove() is a check to see if the card that the player wants to move
     *          to one of the Ace piles is a legal move in the game of Solitaire.
     * Procedure: isValidMove() is given the stack where the card will be going to and the card that is being moved itself.
     *            It first checks if the ace pile is empty and if the card being moved is an Ace.
     *            It then checks if the suits match the card and the top card in the pile.
     *            Finally, it checks to see if the value of the card being moved is only one higher than the current card on top.
     */
     
     public boolean isValidAceMove( Card moved, stacks endpoint ) {
         
         if ( endpoint.isUpEmpty() && moved.value == 1 ) 
             return true;
         if ( moved.suit != endpoint.up[endpoint.top_of_up].suit )
             return false;
         if ( moved.value != (endpoint.up[endpoint.top_of_up].value + 1) )
             return false;
         
         return true;
     }
     
     /* boolean game_over()
      * Purpose: game_over() checks the 4 ace piles and determines if the game is over.
      * Procedure: All this function does is check to see if each stack's up pile is full since
      *            the isValidAceMove should take care of whether or not the cards in the stack were legal moves.
      */
     public static boolean game_over( stacks [] aces ) {
         for(int i = 0; i < ACE_SIZE; ++i) {
             if ( !aces[i].isUpFull() )
                 return false;
         }
         return true;
     }
     
     /* void draw_three()
      * Purpse: draw_three() is the function which the draw pile uses to draw the top three cards from the face-down draw pile
      *         and transfers it over to the face up pile.
     * Procdure: This function will just pop three cards off the top of the face-down and push them onto the face-up pile.
      */
     
     public static void draw_three( stacks draw_pile ) {
         if ( draw_pile.isDownEmpty() && draw_pile.isUpEmpty() ) {
             //Play sound to notify user that there are no more cards for either piles.
         }
         
         //This case is for if all the cards are in the face-up pile in which they need to be transferred to the face-down pile.
         else if ( draw_pile.isDownEmpty() ) {
             int check = draw_pile.capac_of_up;
             for( int i = 0; i < check; i++ ) {
                 draw_pile.push_down( draw_pile.pop_up() );
             }
         }
         
         else {
            int check = ( 3 - (draw_pile.capac_of_down % 3));
             for (int i = 0; i < check; i++){
                draw_pile.push_up( draw_pile.pop_down() );
                System.out.println(draw_pile.top_of_up + " " + draw_pile.top_of_down);
             }    
         }
     }
 }
