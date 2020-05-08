 package px.spaceInvaders.core;
 
 /**Entry-Point class for the Space Invaders game.
  * @author Michael Stopa */
 public class GameCore {
     
     // ++++ ++++ Enumeration ++++ ++++
     
     /**Enum used to determine the current state of the game.
      * @author Michael Stopa */
     public enum Mode {
         IN_GAME,
         PAUSED,
         MAIN_MENU,
         GAME_OVER
     }
     
     // ++++ ++++ Data ++++ ++++
     
     /**Static reference to the current GameCore singleton */
     public static GameCore instance;
     
     private Mode mode;
     private Frame frame;
     
     /**Entry point method, starts the game by setting the GameCore.instance singleton and
      * creating a Frame object to put the game into. 
      * @param args Command-line Arguments */
     public static void main(String[] args) {
         instance = new GameCore();
     }
     
     // ++++ ++++ Initialization ++++ ++++
     
     /**Private constructor is only called by main() method. */
     private GameCore() {
        mode = Mode.MAIN_MENU;
         frame = new Frame();
         frame.setVisible(true);
         FrameControl.centerFrame(frame);
     }
     
     // ++++ ++++ Accessors ++++ ++++
     
     /**@return The game's current game state mode. */
     public Mode getMode() {
         return mode;
     }
     
     // ++++ ++++ Mutators ++++ ++++
     
     /**@param mode Set the game's current state mode. */
     public void setMode(Mode mode) {
         this.mode = mode;
     }
 }
