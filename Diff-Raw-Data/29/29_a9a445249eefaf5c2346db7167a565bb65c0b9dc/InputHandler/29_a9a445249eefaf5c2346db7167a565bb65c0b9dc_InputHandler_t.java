 package thrust.input;
 
 /**
  * Processes and delegates each keyboard input received.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 public class InputHandler {
   /** An unknown character code. */
   private static final char UNKNOWN_CHAR = '\0';
   /** Press 'h' to display the high score */
   public static final char DISPLAY_HIGH_SCORES = 'h';
   /** Press 'm' to toggle music/sound effects */
   public static final char TOGGLE_MUSIC_OR_EFFECTS = 'm';
   /** Press [space] to start the game */
   public static final char START_GAME = ' ';
   /** Press [Esc] to end the game. */
   public static final char STOP_GAME = (char)27;
   /** Press [Enter] to fire the gun */
   public static final char FIRE_GUN = '\r';
   /** Press 'a' to turn left */
   public static final char TURN_LEFT = 'a';
   /** Press 's' to turn right */
   public static final char TURN_RIGHT = 's';
   /** Press [shift] to use engine */
   public static final char USE_ENGINE = (char) 14;
   /** Press [space] to use shield and/or pick up object. */
   public static final char USE_SHIELD = ' ';
 
   /**
    * @return What are the legal keyboard inputs?
    */
   public /*@ pure @*/ char[] legal_inputs() {
     /* Array of the legal inputs */
     char[] legal_inputs_array = {DISPLAY_HIGH_SCORES, TOGGLE_MUSIC_OR_EFFECTS, START_GAME, STOP_GAME, FIRE_GUN,
                                 TURN_LEFT, TURN_RIGHT, USE_ENGINE, USE_SHIELD};
     //@ assert legal_inputs_array != null;
     return legal_inputs_array;
     
   }
 
   /**
    * @return Is this character a legal keyboard input?
    * @param the_character the character to check.
    */
   /*@ ensures \result <==> (the_character == DISPLAY_HIGH_SCORES) |
     @                      (the_character == TOGGLE_MUSIC_OR_EFFECTS) |
     @                      (the_character == START_GAME) |
     @                      (the_character == STOP_GAME) |
     @                      (the_character == FIRE_GUN) |
     @                      (the_character == TURN_LEFT) |
     @                      (the_character == TURN_RIGHT) |
     @                      (the_character == USE_ENGINE) |
     @                      (the_character == USE_SHIELD);
     @*/
   public /*@ pure @*/ boolean legal_input(char the_character) {
     char[] legal_input_array = legal_inputs();
     // Search through array for the char, if found, return true
    for(int i = 0 ; i<legal_input_array.length; i++){
       if(legal_input_array[i] == the_character){
         return true;
       }
     }
     //If it isn't found, false will be returned
     return false;
   }
 
   /**
    * Process this keyboard input character.
    * @param the_keyboard_input the input character to process.
    */
   //@ requires legal_input(the_keyboard_input);
   public void process(char the_keyboard_input) {
    //@assert legal_input(the_keyboard_input);
     
     if(true/*game has not started*/){
       switch(the_keyboard_input){
         case START_GAME: /* start game */
         case DISPLAY_HIGH_SCORES: /* start game */
       }
     }
     
     else/* Game has started */{
       switch(the_keyboard_input){
         case TOGGLE_MUSIC_OR_EFFECTS: /* change music mode*/
         case STOP_GAME: /* stop game */
         case FIRE_GUN: /* Fire a bullet*/
         case TURN_LEFT: /*Rotate ship left*/
         case TURN_RIGHT: /* Rotate ship right*/
         case USE_ENGINE: /*Accelerate ship */
         case USE_SHIELD: /*Use shield/pick up */
       }
     }
     
    
   }
 }
