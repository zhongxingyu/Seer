 package team.nine;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class Team9Activity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

        setContentView(R.layout.home);
     }
	 public void homeClick(View view) {  
		 Intent home = new Intent("Team9.Login");
			startActivity(home);
	 }
	 public void searchClick(View view) {  
		 Intent home = new Intent("Team9.Home");
			startActivity(home);
	 }
 }
