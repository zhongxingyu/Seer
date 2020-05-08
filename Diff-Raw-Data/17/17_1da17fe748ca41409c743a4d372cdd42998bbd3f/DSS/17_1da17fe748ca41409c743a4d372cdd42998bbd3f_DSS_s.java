 /* DSS - Dave's Simple Solitaire by David Duncan
  * Objective: To create a playable version of a simple solitaire card game.
  */
 
 import java.util.Arrays;
 import java.util.Collections;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import static org.lwjgl.opengl.GL11.*;
 
 public class DSS {
     
     //Constants of some of the main compoenents in the game.
     private final int MAIN_SIZE = 7;
     private final int ACE_SIZE = 4;
     private final int DECK_SIZE = 52;
     
     //The main variables I use throughout the program
     private Card [] shuffle = new Card [DECK_SIZE];          // shuffle is for populating a deck of cards and shuffling them in order to pass them out.
     private stacks [] main_stacks = new stacks [MAIN_SIZE]; // main_stacks are the seven main piles of cards the player interacts with.
     private stacks draw_stack;                              // This stack is for the piles of cards where the user draws three from the rest of the deck.
     private stacks [] ace_stacks = new stacks[ACE_SIZE];    // This array is for the piles of aces where the player will place the cards to win.
     
     //Properties of the window
     private final String title = "DSS -- Dave's Simple Solitaire";
     private final int width = 1680;
     private final int height = 1000;
     
     
     public DSS() {
         initialize_game();
     }
     
     public static void main( String[] args ) {
         
         new DSS().gameLoop();
         System.exit(0);
       
    }
 
     /* boolean game_over()
      * Purpose: game_over() checks the 4 ace piles and determines if the game is over.
      * Procedure: All this function does is check to see if each stack's up pile is full since
      *            the isValidAceMove should take care of whether or not the cards in the stack were legal moves.
      */
     public boolean game_over( stacks [] aces ) {
         for(int i = 0; i < ACE_SIZE; ++i) {
             if ( !aces[i].isFull('u') )
                 return false;
         }
         return true;
     }
     
     public void initialize_game() {
         
         int value;
         char suit;
         int count = 0;
         char color;
         
         String sname;
         
         
         //Initializing OpenGL window and starting the canvas.
         try {
             Display.setDisplayMode(new DisplayMode(width, height));
             Display.setTitle(title);
             Display.setInitialBackground(0.0f, 0.9f, 0.0f);
             Display.create();
             Display.setVSyncEnabled(true);
             
                 glEnable(GL_TEXTURE_2D);
 
             glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 
                 glEnable(GL_BLEND);
                 glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
                 glViewport(0, 0, width, height);
             glMatrixMode(GL_MODELVIEW);
 
             glMatrixMode(GL_PROJECTION);
             glLoadIdentity();
             glOrtho(0, width, height, 0, 1, -1);
             glMatrixMode(GL_MODELVIEW);  
            
             
         } catch ( LWJGLException e ) {
             e.printStackTrace();
             System.exit(0);
         }
         
         // This for loop is for populating a deck in order to shuffle it and populate the playing field 
         for( int i = 0; i < DECK_SIZE; ++i ) {
             value = (i % 13) + 1;
             if(  i < 26 ) {
                 color = 'R';
                 if ( i < 13 ) {
                     suit = 'D';
                     sname = "diamond";
                     
                 }
                 else {
                     suit = 'H';
                     sname = "heart";
                 }
             }
             else {
                 color = 'B';
                 if ( i < 39 ) {
                     suit = 'C';
                     sname = "club";
                 }
                 else {
                     suit = 'S';
                     sname = "spade";
                 }
             }
             
             sname = sname.concat(Integer.toString(value)).concat(".png");
             
             shuffle[i] = new Card (value, color, suit, 0, 0, sname);
             shuffle[i].load();
         }
         
         
         // From the deck 'shuffle', I'm converting it to a list to use the library Collections' function shuffle()
         // in order to shuffle the deck before I deal the cards onto the playing field.
         Collections.shuffle( Arrays.asList( shuffle ) );
                 
 
         
         for (int i = 0; i < MAIN_SIZE; i++) {
            main_stacks[i] = new stacks(50 + (i * 280), 350, 50 + (i * 280), 350);
         }
         
         
         /* 
         *  Populating the 7 piles laid out on the playing field.
         *  This loops deals the very first pile with a face up card followed by the rest of the piles with face down.
         *  If this loop works correctly, the first stack should only have a face up card and none under them while the 
         *  last pile should have 6 cards faced down under it with one face up card.
         */
         
         for( int i = 0; i < MAIN_SIZE; i++ ) {
             main_stacks[i].push(shuffle[count], 'u');
             count++;
             for ( int k = i + 1; k < MAIN_SIZE; k++ ) {
                 main_stacks[k].push(shuffle[count], 'd');
                 count++;
             }
         }
         
        for ( int i = 0; i < ACE_SIZE; i++ )
            ace_stacks[i] = new stacks(30, 510 + (i * 280), 0, 0);
         
        draw_stack = new stacks( ( DECK_SIZE - count ), 30, 40, 310, 40);
         
         //This temp stack is to correct the conversion to the draw_stack
         stacks temp = new stacks( DECK_SIZE - count );
         
     }
     
     /* void gameLoop() 
      * Purpose: gameLoop is the function that holds the main game loop and is what will call the other functions to make 
      *          the game run and function properly.
      * Procedure: gameLoop will call frameRendering() to re-draw how the game should look.
      *      
      */
     
     public void gameLoop() {
         while ( !game_over(ace_stacks) ) {
             
             glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
             glMatrixMode(GL_MODELVIEW);
             glLoadIdentity();
             
             frameRendering();
             
             Display.update();
             
             if ( Display.isCloseRequested() ) {
                 Display.destroy();
                 System.exit(0);
             } 
         }
     }
     
     
     public void frameRendering() {
         
         Display.sync(100);
         
         //For background color
         glClearColor(0.0f, 0.5f, 0.0f, 0.0f);
         
         for( int i = 0; i < 7; i++) {
             main_stacks[i].draw();
         }
    
     }
     
 }
