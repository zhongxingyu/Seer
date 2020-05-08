 package input;
 import game.Game;
 import game.Player;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 
 public class InputHandler implements KeyListener {
     private static final int DEBOUNCE_TIME = 20;
     /*
      * To handle multiple keys pressed at once, we maintain a keyState array of
      * the keys we are interested in. Since the KeyCodes of these keys may be
      * spread from 0 to 65535 we have an array of the keys we are interested in
      * and use the same index in the key state array as in the KEYS_IN_USE array.
      */
     /**
      * These are the keys we will maintain key state for.
      */
     private static final int[] KEYS_IN_USE = {
         KeyEvent.VK_SPACE,
         KeyEvent.VK_UP,
         KeyEvent.VK_DOWN,
         KeyEvent.VK_LEFT,
         KeyEvent.VK_RIGHT,
         KeyEvent.VK_ESCAPE,
         KeyEvent.VK_Q,
         KeyEvent.VK_PAUSE,
         KeyEvent.VK_P,
         KeyEvent.VK_W,
         KeyEvent.VK_S,
         KeyEvent.VK_A,
         KeyEvent.VK_Z,
         KeyEvent.VK_F11,
         KeyEvent.VK_BACK_SLASH,
        KeyEvent.VK_PAGE_UP,
        KeyEvent.VK_PAGE_DOWN,
     };
     /**
      * This is a mask to indicate if each key is disabled when the game is paused
      */
     private static final boolean[] KEY_PAUSE_MASK = {
         true, //KeyEvent.VK_SPACE,
         true, //KeyEvent.VK_UP,
         true, //KeyEvent.VK_DOWN,
         true, //KeyEvent.VK_LEFT,
         true, //KeyEvent.VK_RIGHT,
         false,//KeyEvent.VK_ESCAPE,
         false,//KeyEvent.VK_Q,
         false,//KeyEvent.VK_PAUSE,
         false,//KeyEvent.VK_P,
         true, //KeyEvent.VK_W,
         true, //KeyEvent.VK_S,
        true, //KeyEvent.VK_A,
        true, //KeyEvent.VK_Z,
         true, //KeyEvent.VK_F11,
         true, //KeyEvent.VK_BLACK_SLASH,
        true, //KeyEvent.VK_PAGE_UP
        true, //KeyEvent.VK_PAGE_DOWN
     };
     /**
     * This is a mask to indicate if each key should be debounced
      */
     private static final boolean[] KEY_DEBOUNCE_MASK = {
         false,//KeyEvent.VK_SPACE,
         false,//KeyEvent.VK_UP,
         false,//KeyEvent.VK_DOWN,
         false,//KeyEvent.VK_LEFT,
         false,//KeyEvent.VK_RIGHT,
         false,//KeyEvent.VK_ESCAPE,
         false,//KeyEvent.VK_Q,
         true, //KeyEvent.VK_PAUSE,
         true, //KeyEvent.VK_P,
         true, //KeyEvent.VK_W,
         true, //KeyEvent.VK_S,
         false, // A
         false, // Z
         true, //KeyEvent.VK_F11,
         true, //KeyEvent.VK_BLACK_SLASH,
        false, //KeyEvent.VK_PAGE_UP
        false, //KeyEvent.VK_PAGE_DOWN
     };
 
 
     private boolean[] keyState;
     private int[] keyDebounce;
 
     public InputHandler() {
         keyState = new boolean[KEYS_IN_USE.length];
         keyDebounce = new int[KEYS_IN_USE.length];
        assert(KEY_PAUSE_MASK.length == KEYS_IN_USE.length);
        assert(KEY_DEBOUNCE_MASK.length == KEYS_IN_USE.length);
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         for (int i = 0; i < KEYS_IN_USE.length; i++) {
             if (e.getKeyCode() == KEYS_IN_USE[i]) {
                 keyState[i] = true;
                 return; /* Return as soon as we found a match */
             }
         }
         System.err.println("DEBUG: unhandled key press " + e.getKeyCode());
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         for (int i = 0; i < KEYS_IN_USE.length; i++) {
             if (e.getKeyCode() == KEYS_IN_USE[i]) {
                 keyState[i] = false;
                 return;
             }
         }
         System.err.println("DEBUG: unhandled key release " + e.getKeyCode());
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     public void update() {
         Player player = Game.getPlayer();
 
         // On state transition to player death, clear key state
         if(player != null && player.isAlive() == false) {
             clearKeyState();
         }
 
         // Update our debounce timers
         for (int i = 0; i < keyDebounce.length; i++)
             if (keyDebounce[i] > 0)
                 keyDebounce[i]--;
 
         for (int i = 0; i < KEYS_IN_USE.length; i++) {
             /* Skip keys that are up */
             if (keyState[i] == false)
                 continue;
 
             // Skip keys that are disabled when the game is paused
             if (Game.isPaused() && KEY_PAUSE_MASK[i])
                 continue;
 
             // Respawn player on first key stroke after death
             if (player.isAlive() == false) {
                 player.respawn();
             }
 
             // Skip keys who's debounce timer is running
             if (KEY_DEBOUNCE_MASK[i]) {
                 if (keyDebounce[i] > 0)
                     continue;
                 else
                     keyDebounce[i] = DEBOUNCE_TIME;
             }
 
             switch(KEYS_IN_USE[i]){
                 case(KeyEvent.VK_SPACE):
                     player.input(PlayerInput.SHOOT);
                     break;
                 case(KeyEvent.VK_UP):
                     player.input(PlayerInput.PITCH_UP);
                     break;
                 case(KeyEvent.VK_DOWN):
                     player.input(PlayerInput.PITCH_DOWN);
                     break;
                 case(KeyEvent.VK_LEFT):
                     player.input(PlayerInput.YAW_LEFT);
                     break;
                 case(KeyEvent.VK_RIGHT):
                     player.input(PlayerInput.YAW_RIGHT);
                     break;
                case(KeyEvent.VK_PAGE_UP):
                    player.input(PlayerInput.ROLL_LEFT);
                    break;
                case(KeyEvent.VK_PAGE_DOWN):
                    player.input(PlayerInput.ROLL_RIGHT);
                    break;
                 case(KeyEvent.VK_A):
                     player.input(PlayerInput.FORWARD);
                     break;
                 case(KeyEvent.VK_Z):
                     player.input(PlayerInput.BACK);
                     break;
                 case(KeyEvent.VK_Q):
                 case(KeyEvent.VK_ESCAPE):
                     Game.exit();
                     clearKeyState(); /* Clear key state so we don't find this command in our buffer when we return to the game */
                     break;
                 case(KeyEvent.VK_P): // Fall through
                 case(KeyEvent.VK_PAUSE):
                     Game.togglePause();
                     break;
                 case(KeyEvent.VK_BACK_SLASH):
                     // Same as VK_F11
                 case(KeyEvent.VK_F11): // This is just for DEBUGGING
                     //Bandit.spawn();
                 default:
                     //do nothing
             }
         }
     }
 
     /*
      * Reset key states
      */
     private void clearKeyState() {
         for (int i = 0; i < keyState.length; i++)
             keyState[i] = false;
     }
 }
 
 
 
