 package edu.upenn.cis350;
 
 import java.util.List;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.SaveCallback;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ManageGroups extends Activity {
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Parse.initialize(this, Settings.APPLICATION_ID, Settings.CLIENT_ID);
         setContentView(R.layout.manage_groups);
         final Spinner groups = (Spinner)findViewById(R.id.groupSpinner);
         final Spinner systems = (Spinner)findViewById(R.id.systemSpinner);
 
         final ArrayAdapter <CharSequence> adapter =
 			new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("\tSelect a Group");
         ParseQuery query = new ParseQuery("Group");
 		query.orderByAscending("name");	
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> arg0, ParseException arg1) {
 				// TODO Auto-generated method stub
 				if(arg0 != null)
 				for(ParseObject obj : arg0){
 					adapter.add(obj.getString("name"));
 				}
 				groups.setAdapter(adapter);
 			}
 			
 		});
 		
 		groups.setOnItemSelectedListener(new OnItemSelectedListener(){
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
				if(arg0 != null && arg0.getSelectedItemPosition() != 0){
 					//TODO: jump to group's page
					String name = arg0.getSelectedItem().toString();
					Intent i = new Intent(getApplicationContext(), ContactList.class);
					i.putExtra("groupName", name);
					i.putExtra("filter", ListItem.Type.GROUP);
					finish();
					startActivity(i);
 				}
 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				// no-op
 			}
 			
 		});
 		
 	       final ArrayAdapter <CharSequence> adapter2 =
 				new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
 	        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        adapter2.add("\tSelect a System");
 	        ParseQuery query2 = new ParseQuery("System");
 			query2.orderByAscending("name");	
 			query2.findInBackground(new FindCallback() {
 
 				@Override
 				public void done(List<ParseObject> arg0, ParseException arg1) {
 					// TODO Auto-generated method stub
 					if(arg0 != null)
 					for(ParseObject obj : arg0){
 						adapter2.add(obj.getString("name"));
 					}
 					systems.setAdapter(adapter2);
 				}
 				
 			});
 			
 			systems.setOnItemSelectedListener(new OnItemSelectedListener(){
 
 				@Override
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					// TODO Auto-generated method stub
					if(arg0 != null && arg0.getSelectedItemPosition() != 0){
 						//TODO: jump to system's page
						String name = arg0.getSelectedItem().toString();
						Intent i = new Intent(getApplicationContext(), ContactList.class);
						i.putExtra("groupName", name);
						i.putExtra("filter", ListItem.Type.SYSTEM);
						finish();
						startActivity(i);
 					}
 
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 					// TODO Auto-generated method stub
 					// no-op
 				}
 				
 			});
         
     }
         
 	// onClick function of addGroup button
 	public void addGroup(View view){
 		EditText text = (EditText) findViewById(R.id.groupText);
 		if(text != null){
 			final String t = text.getText().toString();
 			if("".equals(t)){
 				final Toast toast = Toast.makeText(this, "Invalid Group Name", Toast.LENGTH_SHORT);
 				toast.show();
 				return;
 			}
 			ParseQuery query = new ParseQuery("Group");
 			query.whereEqualTo("name", t);
 			query.findInBackground(new FindCallback(){
 
 				@Override
 				public void done(List<ParseObject> arg0, ParseException arg1) {
 					// TODO Auto-generated method stub
 					// doesn't exist - add it
 					if(arg0 != null && arg0.size() == 0){
 						ParseObject obj = new ParseObject("Group");
 						obj.put("name", t);
 						obj.saveInBackground(new SaveCallback(){
 
 							@Override
 							public void done(ParseException arg0) {
 								// TODO Auto-generated method stub
 								Intent i = new Intent(getApplicationContext(), ManageGroups.class);
 								finish();
 								startActivity(i);
 							}
 							
 						});
 					}
 					else {
 						final Toast toast = Toast.makeText(getApplicationContext(), "Duplicate Group Name", Toast.LENGTH_SHORT);
 						toast.show();
 					}
 				}
 				
 			});
 		}
 	}
 	
 	// onClick function of addSystem button
 	public void addSystem(View view){
 		EditText text = (EditText) findViewById(R.id.systemText);
 		if(text != null){
 			final String t = text.getText().toString();
 			if("".equals(t)){
 				final Toast toast = Toast.makeText(this, "Invalid Group Name", Toast.LENGTH_SHORT);
 				toast.show();
 				return;
 			}
 			ParseQuery query = new ParseQuery("System");
 			query.whereEqualTo("name", t);
 			query.findInBackground(new FindCallback(){
 
 				@Override
 				public void done(List<ParseObject> arg0, ParseException arg1) {
 					// TODO Auto-generated method stub
 					// doesn't exist - add it
 					if(arg0 != null && arg0.size() == 0){
 						ParseObject obj = new ParseObject("System");
 						obj.put("name", t);
 						obj.saveInBackground(new SaveCallback(){
 
 							@Override
 							public void done(ParseException arg0) {
 								// TODO Auto-generated method stub
 								Intent i = new Intent(getApplicationContext(), ManageGroups.class);
 								finish();
 								startActivity(i);
 							}
 							
 						});
 					}
 					else {
 						final Toast toast = Toast.makeText(getApplicationContext(), "Duplicate System Name", Toast.LENGTH_SHORT);
 						toast.show();
 					}
 				}
 				
 			});
 		}
 	}
 }
