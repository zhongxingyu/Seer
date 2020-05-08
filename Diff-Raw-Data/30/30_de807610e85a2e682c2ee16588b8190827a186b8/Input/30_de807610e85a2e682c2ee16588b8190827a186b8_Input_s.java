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
 
     private static final long REPEAT_DELAY = 200L;
     private static final long ACTION_REPEAT_DELAY = 200L;
 
     private static long mLastPress = 0L;
     private static long mLastPressBack = 0L;
     private static long mLastPressAction = 0L;
 
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
     public static boolean action() {
         if(continuousAction()) {
             if(isActionReady()) {
                mLastPressBack = TimeUtils.millis();
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
     public static boolean alternateAction() {
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
     public static boolean up() {
         if(continuousUp()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for DOWN. Use continuousDown() for continuous input.
      */
     public static boolean down() {
         if(continuousDown()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for LEFT. Use continuousLeft() for continuous input.
      */
     public static boolean left() {
         if(continuousLeft()) {
             return keyAndTime();
         }
         return false;
     }
 
     /**
      * Returns true every 200ms for RIGHT. Use continuousRight() for continuous input.
      */
     public static boolean right() {
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
 
     private static boolean keyAndTime() {
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
         return ((TimeUtils.millis() - mLastPressBack) > ACTION_REPEAT_DELAY);
     }
 
     private static boolean isKeyReady() {
         return ((TimeUtils.millis() - mLastPress) > REPEAT_DELAY);
     }
 
     public static boolean isActionReady() {
         return ((TimeUtils.millis() - mLastPressAction) > REPEAT_DELAY);
     }
 }
