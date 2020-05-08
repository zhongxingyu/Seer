 package yoloswagswag.swe_app;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.View;
 import android.content.Intent;
 
 public class chillActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.chill_screen);
     }
 
     public void timeChange(View view){
         startActivity(new Intent(this, timeSelector.class));
     }
 }
