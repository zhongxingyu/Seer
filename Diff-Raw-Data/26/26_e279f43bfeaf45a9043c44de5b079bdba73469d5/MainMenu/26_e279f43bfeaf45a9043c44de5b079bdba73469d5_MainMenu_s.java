 package ie.ucd.asteroid;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class MainMenu extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main_menu);
     }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }
     //opens the weekly asteroids activity
     public void weeklyBtn(View v){   	
     	Intent weeklyBtnIntent = new Intent(this, WeeklyAsteroids.class);
     	startActivity(weeklyBtnIntent);
     }
     //opens the doomsday clock
     public void doomsBtn(View v){   	
     	Intent doomsBtnIntent = new Intent(this, DoomsDayCountdown.class);
     	startActivity(doomsBtnIntent);
     }
     //opens the about page
     public void iBtn(View v){   	
     	Intent iBtnIntent = new Intent(this, About.class);
     	startActivity(iBtnIntent);
     }
 }
