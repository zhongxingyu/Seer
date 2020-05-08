 package mobile.team4.game;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class Rampart extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(new Game(this));
     }
 }
