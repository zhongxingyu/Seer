 package watsons.wine;
 
 import android.app.Activity;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 
 public class WatsonsWineActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
            
         setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         ImageView main_bg = (ImageView) findViewById(R.id.main_bg);
         main_bg.setOnClickListener(new View.OnClickListener()         
         {
 
             public void onClick(View v) {
             	// Perform action on click
             	Intent myIntent = new Intent(v.getContext(), TabGroupView.class);
                 startActivity(myIntent);
             }
         });
     }
 }
