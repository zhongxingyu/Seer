 package gogo.gadgeto.car;
 
 import gogo.gadgeto.model.Database;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class LeaveCarGroupActivity extends Activity {
 	
 	private Spinner groupIdSpinner;
 	private Button leaveGroupButton;
 	
 	private Database database;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_leave_car_group);
         
         groupIdSpinner = (Spinner) findViewById(R.id.LeaveGroupIdSpinner);
         leaveGroupButton = (Button) findViewById(R.id.LeaveGroupSendButton);
         
         database = Database.getInstance();
         
         String groupStrings[] = database.getGroups(database.getUsername());
         
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,groupStrings);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         groupIdSpinner.setAdapter(adapter);
         
         
         
//        groupIdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
//					int position, long id) {
//				((TextView)parentView.getChildAt(0)).setTextColor(Color.rgb(0, 0, 0));  				
//			}
//
//			public void onNothingSelected(AdapterView<?> arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_leave_car_group, menu);
         return true;
     }
 
     
 }
