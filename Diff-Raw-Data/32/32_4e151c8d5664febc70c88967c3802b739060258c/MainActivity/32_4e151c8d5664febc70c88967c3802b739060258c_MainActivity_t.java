 package edu.mines.csci498.bwisdom.lunchlist;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.RadioGroup;
 
 public class MainActivity extends Activity {
 
 	Restaurant r = new Restaurant();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Button save = (Button) findViewById(R.id.save);
 
 		save.setOnClickListener(onSave);
 
 	}
 
 	private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
 			EditText name = (EditText) findViewById(R.id.name);
 			EditText address = (EditText) findViewById(R.id.addr);
 
 			r.setName(name.getText().toString());
 			r.setAddress(address.getText().toString());
			
			RadioGroup types = (RadioGroup) findViewById(R.id.types);
			
			switch(types.getCheckedRadioButtonId()){
				case R.id.sit_down:
					r.setType("sit_down");
					break;
				case R.id.takout:
					r.setType("takeout");
					break;
				case R.id.delivery:
					r.setType("delivery");
					break;
			}	
 		}
 	};
 }
