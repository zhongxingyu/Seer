 package com.habosa.dotdotdash;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.inputmethodservice.InputMethodService;
 import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
 import android.view.View;
 import android.view.inputmethod.InputConnection;
 import android.view.inputmethod.InputMethodManager;
 
 public class MainService extends InputMethodService
 implements OnKeyboardActionListener {
 
     public static int KEY_CODE_MAIN = 1;
     public static long LONG_PRESS_TIME = 250L;
     public static int CONVERT_DELAY_MILLIS = 700;
 
     private InputMethodManager mInputMethodManager;
 
     private MorseKeyboardView mInputView;
     private MorseKeyboard mMorseKeyboard;
 
     private Date mPressStarted;
     private StringBuilder mComposing = new StringBuilder();
 
     public static Map<String, String> decoder = new HashMap<String, String>();
     private static Timer sTimer = new Timer();
 
     @Override
     public void onCreate() {
         super.onCreate();
         mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
         initDecoder();
     }
 
     public void initDecoder() {
         decoder.put(".-", "A");
         decoder.put("-...", "B");
         decoder.put("-.-.", "C");
         decoder.put("-..", "D");
         decoder.put(".", "E");
         decoder.put("..-.", "F");
         decoder.put("--.", "G");
         decoder.put("....", "H");
         decoder.put("..", "I");
         decoder.put(".---", "J");
         decoder.put("-.-", "K");
         decoder.put(".-..", "L");
         decoder.put("--", "M");
         decoder.put("-.", "N");
         decoder.put("---", "O");
         decoder.put(".--.", "P");
         decoder.put("--.-", "Q");
         decoder.put(".-.", "R");
         decoder.put("...", "S");
         decoder.put("-", "T");
         decoder.put("..-", "U");
         decoder.put("...-", "V");
         decoder.put(".--", "W");
         decoder.put("-..-", "X");
         decoder.put("-.--", "Y");
         decoder.put("--..", "Z");
         decoder.put("-----", "0");
         decoder.put(".----", "1");
         decoder.put("..---", "2");
         decoder.put("...--", "3");
         decoder.put("....-", "4");
         decoder.put(".....", "5");
         decoder.put("-....", "6");
         decoder.put("--...", "7");
         decoder.put("---..", "8");
         decoder.put("----.", "9");
     }
 
     @Override
     public void onInitializeInterface() {
         mMorseKeyboard = new MorseKeyboard(this, R.xml.morse);
     }
 
     @Override
     public View onCreateInputView() {
         mInputView = (MorseKeyboardView) getLayoutInflater().inflate(R.layout.morse_keyboard, null);
         mInputView.setOnKeyboardActionListener(this);
         mInputView.setKeyboard(mMorseKeyboard);
         return mInputView;
     }
 
     @Override
     public void onKey(int primaryCode, int[] keyCodes) {
         // TODO(samstern): Do something here
     }
 
     @Override
     public void onPress(int primaryCode) {
         // Mark time
         mPressStarted = new Date();
         // Cancel scheduled tasks
         sTimer.cancel();
     }
 
     @Override
     public void onRelease(int primaryCode) {
         if (primaryCode == 1) {
             Date pressEnded = new Date();
             long diff = pressEnded.getTime() - mPressStarted.getTime();
             if (diff >= LONG_PRESS_TIME) {
                 // Long press
                 mComposing.append('-');
                 sendText("-");
             } else {
                 // Short press
                 mComposing.append('.');
                 sendText(".");
             }
         } else if (primaryCode == 2) {
             sendText(" ");
             mComposing.setLength(0);
         }
        
        // Convert after a small delay
        sTimer = new Timer();
        sTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                convertLastChar();
            }      
        }, CONVERT_DELAY_MILLIS);
     }
 
     /**
      * Take the morse chars, make a letter.
      */
     public void convertLastChar() {
         String entered = mComposing.toString();
         String letter = decoder.get(entered);
         if (letter != null) {
             // Delete the dots and dashes
             deleteText(mComposing.length());
             // Send a letter
             sendText(letter);
             mComposing.setLength(0);
         } else {
             sendText("?");
             mComposing.setLength(0);
         }
     }
 
     /**
      * Send the text to the view.
      */
     public void sendText(String toSend) {
         InputConnection inputConnection = getCurrentInputConnection();
         inputConnection.commitText(toSend, 1);
     }
 
     /**
      * Delete the last 'num' characters
      * @param num
      */
     public void deleteText(int num) {
         InputConnection inputConnection = getCurrentInputConnection();
         inputConnection.deleteSurroundingText(num, 0);
     }
 
     @Override
     public void onText(CharSequence text) {
         // TODO Auto-generated method stub     
     }
 
     @Override
     public void swipeDown() { 
         // TODO(samstern): Hide the noob buttons
     }
 
     @Override
     public void swipeUp() {
         // TODO(samstern): Show the noob buttons
     }
 
     @Override
     public void swipeLeft() { }
 
     @Override
     public void swipeRight() { }
 
 }
