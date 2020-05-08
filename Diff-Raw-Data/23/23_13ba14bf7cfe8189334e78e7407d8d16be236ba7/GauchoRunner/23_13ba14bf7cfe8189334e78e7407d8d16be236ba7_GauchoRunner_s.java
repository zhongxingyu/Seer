 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * The main class that sets up the display and initializes the games states
  */
 public class GauchoRunner extends StateBasedGame {
     /** The state ID for Main menu */
     public static final int MAIN_MENU_STATE_ID = 0;
     /** The state ID for playable game */
     public static final int PLAY_STATE_ID = 1;
     /** The state ID for Instructions menu */
     public static final int INSTRUCTION_STATE_ID = 2;
     /** The state ID for Pause menu */
     public static final int PAUSE_STATE_ID = 3;
     public static final int END_PLAY_STATE = 4;
     /** The container for the main game */
     private static AppGameContainer app;
     ///** The restartFlag to determine if the PlayState will be restarted upon entering */
     //private Boolean restartFlag = false;
 
     /**
      * Creates the states used in the game
      */
     public GauchoRunner() {
        super("Gaucho Runner");
         MenuState menuState = new MenuState(MAIN_MENU_STATE_ID, 300, 180, 131);
         this.addState(menuState);
         PlayState playState = new PlayState(PLAY_STATE_ID);
         this.addState(playState);
         InstructionState instructionState = new InstructionState(INSTRUCTION_STATE_ID);
         this.addState(instructionState);
         PauseState pauseState = new PauseState(PAUSE_STATE_ID, 300, 180, 131, playState);
         this.addState(pauseState);
 //        EndPlayState endPlayState = new EndPlayState(4, 3 );//4 = playstate
 //        this.addState(endPlayState);
         this.enterState(MAIN_MENU_STATE_ID);
     }
 
     /**
      * Displays window
      * @param argv
      */
     public static void main(String[] argv) {
 
         try {
             app = new AppGameContainer(new GauchoRunner());
 
             app.setShowFPS(false);
             app.setVSync(true);
             app.setTargetFrameRate(60);
             app.setDisplayMode(800, 600, false);
             app.start();
         } catch (SlickException e) {
             e.printStackTrace();
         }
     }
 
     @Override
    public void initStatesList(GameContainer gameContainer) throws SlickException {
         //NOTHING FOR NOW - MUST OVERRIDE
     }
 }
