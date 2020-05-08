 package csci498.gnanda.lunchList;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
import android.widget.Spinner;
 
 public class LunchList extends Activity {
 	
 	private List<Restaurant> model = new ArrayList<Restaurant>();
 	private ArrayAdapter<Restaurant> adapter = null;
 	private RadioButton a = null;
 	private RadioGroup types = null;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button save = (Button) findViewById(R.id.save);
         save.setOnClickListener(onSave);    
         
         types = (RadioGroup) findViewById(R.id.types);
         
 	    a = new RadioButton(this);
 	    a.setText("testButton");
 	    types.addView(a);
 
         setUpListAdapter();
     }
     
     private void setUpListAdapter() {
		Spinner list = (Spinner) findViewById(R.id.restaurants);
 		adapter = new ArrayAdapter<Restaurant>(this, android.R.layout.simple_list_item_1, model);
 		list.setAdapter(adapter);		
 	}
 
 	private View.OnClickListener onSave = new View.OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			Restaurant r = new Restaurant();
 			
 			EditText name = (EditText) findViewById(R.id.name);
			AutoCompleteTextView address = (AutoCompleteTextView) findViewById(R.id.addr);
 			
 			r.setName(name.getText().toString());
 			r.setAddress(address.getText().toString());
 			
 			addRadioGroupType(r);
 			
 			adapter.add(r);
 		}
 		
 	};
 
 	private void addRadioGroupType(Restaurant r) {
 	    //RadioGroup types = (RadioGroup) findViewById(R.id.types);
 	    
 	    switch (types.getCheckedRadioButtonId()) {
 	      case R.id.sit_down:
 	        r.setType("sit_down");
 	        break;
 	        
 	      case R.id.take_out:
 	        r.setType("take_out");
 	        break;
 	        
 	      case R.id.delivery:
 	        r.setType("delivery");
 	        break;
 	    }
 	}    
 	
 }
