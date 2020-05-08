 package ufit.profilecreation;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import ufit.namespace.R;
 
 public class ModifyOptionsScreenActivity extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.modifyoptionsscreen);
         initialiseButtons();
         
     }
     public void initialiseButtons() {
         Button mod_ex = (Button) findViewById(R.id.mod_exercises);
         Button mod_go = (Button) findViewById(R.id.mod_goals);
         Button mod_eq = (Button) findViewById(R.id.mod_equip);
         Button button0 = (Button) findViewById(R.id.button0);
         mod_ex.setOnClickListener(this);
         mod_eq.setOnClickListener(this);
         mod_go.setOnClickListener(this);
         button0.setOnClickListener(this);
     }    
     //@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.mod_exercises){
 			Intent intent = new Intent(this,WeeklyPlannerActivity.class);
 			this.startActivity(intent);	        
 		} else if(v.getId() == R.id.mod_goals){
 			Intent intent = new Intent(this,WeeklyPlannerActivity.class);
 			this.startActivity(intent);	        
 		} else if(v.getId() == R.id.mod_equip){
			Intent intent = new Intent(this,MachineSelection.class);
 			this.startActivity(intent);	        
 		} else if(v.getId() == R.id.button0){
 			Intent intent = new Intent(this,HomeScreen.class);
 			this.startActivity(intent);	        
 		}
     }
 }
