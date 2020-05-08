 package thrust.input;
 import java.util.logging.Logger;
 /**
  * Processes and delegates each keyboard input received.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  * @revised 04/04/08 Patrick Nevin: 06754155
  *                   Robert Plunkett: 06038883
  * @revised 20/04/08 Patrick Nevin
 */
 public class InputHandler {
 
   /** Press h to Display High Score.*/
   public static final char DISPLAY_HIGH_SCORES = 'h';
   /** Press m to Toggle Music Or Effect.*/
   public static final char TOGGLE_MUSIC_OR_EFFECTS = 'm';
   /**Press [space] to Start Game.*/
   public static final char START_GAME = ' ';
   /** Press [Esc] to Stop Game.*/
   public static final char STOP_GAME = 27;
   /**Press [Return] to Fire Gun.*/
   public static final char FIRE_GUN = '\r';
   /**Press a to Turn Left.*/
   public static final char TURN_LEFT = 'a';
   /**Press s to Turn Right.*/
   public static final char TURN_RIGHT = 's';
   /**Press shift to Use Engine.*/
   public static final char USE_ENGINE = 16;
   /**Press [Space] to Use Shield.*/
   public static final char USE_SHIELD = ' ';
   /**
    * @return What are the legal keyboard inputs?
    */
   public/*@ pure @*/char[] legal_inputs() {
     //any array of char's
     final char[] legal_inputs = {DISPLAY_HIGH_SCORES, TOGGLE_MUSIC_OR_EFFECTS,
                                  START_GAME, STOP_GAME, FIRE_GUN, TURN_LEFT,
                                  TURN_RIGHT, USE_ENGINE, USE_SHIELD };
     //return the array
     return legal_inputs;
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
   public/*@ pure @*/boolean legal_input(final char the_character) {
 
     final char[] legals = legal_inputs();
     for (int i = 0; i <= legals.length; i++) {
       if (legals[i] == the_character) {
         return true;
       }
     }
     return false;
   }
 
   /**
    * Process this keyboard input character.
    * @param the_keyboard_input the input character to process.
    */
   //@ requires legal_input(the_keyboard_input);
   public void process(final char the_keyboard_input) {
 
     /**
      *Is the game on or off, might have to move this
      *condition somewhere else later,
      */
     boolean game_on = false;
 
     /**
      * Haven't yet decided how to deal with the key board input,
      * so lets log for the time being!
      */
     final Logger my_logger = Logger.getLogger("thrust.input.InputHandler");
     switch (the_keyboard_input) {
       case DISPLAY_HIGH_SCORES:
         my_logger.info("New Command to deal with: DISPLAY_HIGH_SCORES");
         break;
       case TOGGLE_MUSIC_OR_EFFECTS:
         my_logger.info("New Command to deal with: TOGGLE_MUSIC_OR_EFFECTS");
         break;
       case STOP_GAME:
         game_on = false;
         my_logger.info("New Command to deal with: STOP_GAME");
         break;
       case FIRE_GUN:
         my_logger.info("New Command to deal with: FIRE_GUN");
         break;
       case TURN_LEFT:
         my_logger.info("New Command to deal with: TURN_LEFT");
         break;
       case TURN_RIGHT:
         my_logger.info("New Command to deal with: TURN_RIGHT");
         break;
       case USE_ENGINE:
         my_logger.info("New Command to deal with: USE_ENGINE");
         break;
       default:/**do nothing*/
     }
     //char [space] is overloaded and has two associated behaviours
     //if the game is not on, start the game when [space] pressed
     if (the_keyboard_input == ' '/**START_GAME*/ && !game_on) {
       my_logger.info("New Command to deal with: START_GAME");
       game_on = true;
     } //what's the story with this curly... seems alrite...
     //otherwise if the game is not on use shield when [space] pressed
     else if (the_keyboard_input == ' '/**USE_SHIELD*/ && game_on) {
       my_logger.info("New Command to deal with: USE_SHIELD ");
     }
   }
 }
