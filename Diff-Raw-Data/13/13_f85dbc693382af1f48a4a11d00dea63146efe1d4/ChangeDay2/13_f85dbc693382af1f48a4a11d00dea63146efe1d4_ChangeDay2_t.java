 package nl.tuestudent.thermostaat;
 
 import nl.tuestudent.thermostaat.data.DayProgram;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ChangeDay2 extends FragmentActivity {
 	
 	String dayName;
 	DayProgram dayProgram;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_change_day_2);
 		
 		// Get the message from the intent
 	    Intent intent = getIntent();
 	    dayName = intent.getStringExtra(ChangeWeekprogram.EXTRA_MESSAGE);
 	    setTitle(dayName);
 
 	    // Create the text view
 	    //TextView textView = (TextView) findViewById(R.id.t_day_name);
 	    //textView.setText(dayName);
 	    
 	    dayProgram = MainActivity.weekProg.findDayProgram(dayName);
 	    
 	    
 	    ListView view = (ListView) findViewById(android.R.id.list);
 	    String[] names =  {"","","","","","","","","","",};
 	    RowAdapter adapter = new RowAdapter(this, dayProgram.getSwitches(), names );
 	    view.setAdapter(adapter);
 	    
 	    	
 	    	view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				//show add time menu
				Bundle b = new Bundle();
				b.putInt("item_position", position);
				b.putString("day_name", dayName);
 				DialogFragment tempFrag = new PickDayNight();
				tempFrag.setArguments(b);
 				tempFrag.show(getSupportFragmentManager(), "kjljnjkjhjftfuhjvjuyg");
 				
 				//TODO send the time (position of item you clicked on) to the dialog so it can use that as default dialog
 				
 			}
 		});
 	}
 
 }
