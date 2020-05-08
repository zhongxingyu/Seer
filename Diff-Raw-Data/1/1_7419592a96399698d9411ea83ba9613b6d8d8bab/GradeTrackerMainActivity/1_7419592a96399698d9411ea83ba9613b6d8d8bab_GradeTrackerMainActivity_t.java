 package com.andresjruiz.gradetracker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GradeTrackerMainActivity extends ListActivity {
 	
 	private String[] classNames;
 	private String[] classGrades;
 	private StorageInterface storage;
 	
 	private static final int ADD_NEW_CLASS_REQUEST = 5;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //Remove title
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         //Fill in with the layout
         setContentView(R.layout.main);
         
         //Instantiate the Dabase Interface
         storage = new StorageInterface();
         
         //Get the names of the classes on the database
         classNames = storage.getClassNames();
         //Get the grades for each of the classes from the database
         classGrades = storage.getClassGradesPercent();
         
         //Data mapping
         //Initialize List of Maps, each list item is a row in the list. The map is a hash table
         //of the data name its holding in the key. The name is used to map it to the resource
         //in the xml file.
         ArrayList<Map<String, String>> listData = new ArrayList<Map<String, String>>();
         //Name mapping
         String[] listMappings = {"ClassName", "Grade"};
         //Resource mapping
         int[] resourceMappings = {R.id.class_name, R.id.grade_display };
         
         //Populate list
         for(int i = 0; i < classNames.length; i++){
         	Map<String, String> row = new HashMap<String, String>();
         	
         	row.put("ClassName", classNames[i]);
         	row.put("Grade", classGrades[i]);
         	
         	listData.add(row);
         }
         
         //Fill in the list data
         setListAdapter(new SimpleAdapter(this, listData, R.layout.main_list_item, listMappings, resourceMappings));
         
         
         //Create the listener for the selection of a class
         this.getListView().setOnItemClickListener(new OnItemClickListener(){
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 			        int position, long id) {
 				Intent showClassInfo = new Intent();
 				showClassInfo.setClassName("com.andresjruiz.gradetracker", "com.andresjruiz.gradetracker.ClassActivity");
 				showClassInfo.putExtra("ClassName", ((TextView) view.findViewById(R.id.class_name)).getText());
 				showClassInfo.putExtra("StorageInterface", storage);
 				
 				startActivity(showClassInfo);
 			}
         });
         
         Button button = (Button) findViewById(R.id.addClassButton);
         
         button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent addClass = new Intent();
 				addClass.setClassName("com.andresjruiz.gradetracker", "com.andresjruiz.gradetracker.AddClass_Step1");
 				addClass.putExtra("StorageInterface", storage);
 				
 				startActivity(addClass);
 			}
 		});
     }
     
     @Override
     public void onStop(){
     	super.onStop();
     	
     	//Close and clean up any storage things
     	storage.close();
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent data){
     	if(requestCode == ADD_NEW_CLASS_REQUEST){
     		
     	}
     }
 
 }
