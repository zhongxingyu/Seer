 package csci498.jlamson.lunchlist;
 
 import android.app.Activity;
import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class LunchList extends Activity {
 	Restaurant restaurant = new Restaurant();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lunch_list);
         
        addSurplusRadioButtons(savedInstanceState);
        
         Button save = (Button)findViewById(R.id.save);
         save.setOnClickListener(onSave);
     }

	private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
 			EditText name = (EditText)findViewById(R.id.name);
 			EditText address = (EditText)findViewById(R.id.addr);
 			RadioGroup types = (RadioGroup)findViewById(R.id.types);
 			
 			restaurant.setName(name.getText().toString());
 			restaurant.setAddress(address.getText().toString());
 			
 			switch (types.getCheckedRadioButtonId()) {
 			case R.id.sit_down:
 				restaurant.setType("sit_down");
 				break;
 			case R.id.take_out:
 				restaurant.setType("take_out");
 				break;
 			case R.id.delivery:
 				restaurant.setType("delivery");
 				break;
 			}
 		}
 	};
 
	private void addSurplusRadioButtons(Context context) {
		RadioGroup types = (RadioGroup)findViewById(R.id.types);
		
		int bigNumber = 20;
		for(int i=0; i < bigNumber; i++) {
			RadioButton rb = new RadioButton(context);
			rb.setText("fooblah");
			types.addView(rb);
		}
	}
 }
