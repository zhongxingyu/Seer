 package ufit.profilecreation;
 
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import ufit.global.MyApp;
 import ufit.namespace.R;
 import android.widget.Toast;
 
 public class WeeklyPlannerActivity extends Activity  implements OnClickListener{
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.weeklyplanner);
         initialiseButtons();
         
        int thisWeek = new GregorianCalendar().get(Calendar.WEEK_OF_YEAR);
         TextView thisWeek2 = (TextView) findViewById(R.id.weekly_thisweek);
		thisWeek2.setText("Week " + Integer.toString(thisWeek));
  
     }
     public void initialiseButtons() {
         Button exercisesSunday = (Button) findViewById(R.id.sunday);
         Button exercisesMonday = (Button) findViewById(R.id.monday);
         Button exercisesTuesday = (Button) findViewById(R.id.tuesday);
         Button exercisesWednesday = (Button) findViewById(R.id.wednesday);
         Button exercisesThursday = (Button) findViewById(R.id.thursday);
         Button exercisesFriday = (Button) findViewById(R.id.friday);
         Button exercisesSaturday = (Button) findViewById(R.id.saturday);
 
         // button goes to home screen
         final Button button0 = (Button) findViewById(R.id.button0);
         button0.setOnClickListener(this);
         
         exercisesSunday.setOnClickListener(this);
         exercisesMonday.setOnClickListener(this);
         exercisesTuesday.setOnClickListener(this);
         exercisesWednesday.setOnClickListener(this);
         exercisesThursday.setOnClickListener(this);
         exercisesFriday.setOnClickListener(this);
         exercisesSaturday.setOnClickListener(this);
 
     }    
     //@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.sunday){
 			Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);	        
 	    }else if(v.getId() == R.id.monday) {
 	    	Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);			
 		} else if (v.getId() == R.id.tuesday) {
 			Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);			
 		} else if(v.getId() == R.id.wednesday) {
 			Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);
 		} else if (v.getId() == R.id.thursday) {
 			Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);
 		} else if (v.getId() == R.id.friday) {
 			Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);
 		} else if (v.getId() == R.id.saturday) {
 			Intent intent = new Intent(this,SkillSelection.class);
 			this.startActivity(intent);
 		} else if (v.getId() == R.id.button0){
 			Intent intent = new Intent (this, HomeScreen.class);
 			startActivity(intent);
 		}
     }
 	
 }
