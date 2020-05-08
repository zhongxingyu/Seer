 package no.hist.gruppe5.pvu;
 
 import static com.badlogic.gdx.Input.Keys.*;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.TimeUtils;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 23/10/13
  * Time: 09:16
  */
 public class Input {
    
    private static long BACK_REPEAT_DELAY = 400L;
    private static long mLastPressBack = 0L;
 
     private long REPEAT_DELAY = 200L;
     private long ACTION_REPEAT_DELAY = 200L;
 
     private long mLastPress = 0L;
     private long mLastPressAction = 0L;
 
     /**
      * Default delay is 200ms.
      */
     public Input() {
         initLastPressed();
     }
 
     /**
      * Use only if you want a custom delay on key input (does not apply to
      * continuous methods).
      * @param navigationDelay Used for up, down, left, right
      * @param actionDelay Used for action
      */
     public Input(long navigationDelay, long actionDelay) {
         this.REPEAT_DELAY = navigationDelay;
         this.ACTION_REPEAT_DELAY = actionDelay;
         initLastPressed();
 
     }
 
     /**
      * This is required to stop the game from accepting input a few ms after the screen
      * is started.
      */
     private void initLastPressed() {
         long now = TimeUtils.millis();
         mLastPress = now;
         mLastPressBack = now;
         mLastPressAction = now;
     }
 
     /**
      * Returns true once every 200ms if ESCAPE is pressed. Use for navigating back.
      */
    public static boolean back() {
         if(isKey(ESCAPE)) {
             if(isBackReady()) {
                 mLastPressBack = TimeUtils.millis();
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns true once every 200ms if SPACE. Use for MENU action. Use
      * continuousAction() if you require continuous input.
      */
     public boolean action() {
         if(continuousAction()) {
             if(isActionReady()) {
                 mLastPressAction = TimeUtils.millis();
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns true once if SPACE is pressed, no delay. Use action() if you want slower input.
      */
     public static boolean continuousAction() {
         return isKey(SPACE);
     }
 
     /**
      * Returns true once every 200ms if E. Use for MENU action. Has no continuous
      * counterpart.
      */
     public boolean alternateAction() {
         if(isKey(E)) {
             if(isActionReady()) {
                 mLastPressBack = TimeUtils.millis();
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for UP. Use continuousUp() for continuous input.
      */
     public boolean up() {
         if(continuousUp()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for DOWN. Use continuousDown() for continuous input.
      */
     public boolean down() {
         if(continuousDown()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for LEFT. Use continuousLeft() for continuous input.
      */
     public boolean left() {
         if(continuousLeft()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for RIGHT. Use continuousRight() for continuous input.
      */
     public boolean right() {
         if(continuousRight()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns if UP is being pressed, no delay.
      */
     public static boolean continuousUp() {
         return isKey(W) || isKey(UP) || isKey(DPAD_UP);
     }
 
     /**
      * Returns if DOWN is being pressed, no delay.
      */
     public static boolean continuousDown() {
         return isKey(S) || isKey(DOWN) || isKey(DPAD_DOWN);
     }
 
     /**
      * Returns if LEFT is being pressed, no delay.
      */
     public static boolean continuousLeft() {
         return isKey(A) || isKey(LEFT) || isKey(DPAD_LEFT);
     }
 
     /**
      * Returns if RIGHT is being pressed, no delay.
      */
     public static boolean continuousRight() {
         return isKey(D) || isKey(RIGHT) || isKey(DPAD_RIGHT);
     }
 
     private boolean keyAndTime() {
         if(isKeyReady()) {
             mLastPress = TimeUtils.millis();
             return true;
         }
         return false;
     }
 
     private static boolean isKey(int key) {
         return Gdx.input.isKeyPressed(key);
     }
 
    private static boolean isBackReady() {
        return ((TimeUtils.millis() - mLastPressBack) > BACK_REPEAT_DELAY);
     }
 
     private boolean isKeyReady() {
         return ((TimeUtils.millis() - mLastPress) > REPEAT_DELAY);
     }
 
     public boolean isActionReady() {
         return ((TimeUtils.millis() - mLastPressAction) > ACTION_REPEAT_DELAY);
     }
 }
