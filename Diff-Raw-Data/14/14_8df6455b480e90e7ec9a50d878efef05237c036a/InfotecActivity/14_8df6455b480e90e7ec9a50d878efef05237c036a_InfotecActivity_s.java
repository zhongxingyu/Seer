 package mwhs.ap.bkat.app;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import mwhs.ap.doan.app.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class InfotecActivity extends Activity implements android.view.View.OnClickListener, Serializable {
 	private Button search;
 	private ArrayList<School> schools = new ArrayList<School>();
 	private AdapterView<?> parent1;
 	private ArrayList<String> var = new ArrayList<String>();
 	private String[] vars = new String[6];
 	private Spinner spinnersport;
 	private Spinner spinnertuition;
 	private Spinner spinnerregion;
 	private EditText mMajorView;
 	private EditText mPopView;
 	private EditText mHouseView;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         readCsvValues();
      
         
         mMajorView = (EditText) findViewById(R.id.major2);
         mPopView = (EditText) findViewById(R.id.population2);
         mHouseView = (EditText) findViewById(R.id.housing2);
         
         spinnersport = (Spinner) findViewById(R.id.spinnerSports);
         
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 this, R.array.sports_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
    
         search = (Button) findViewById(R.id.search);
         search.setOnClickListener(this);
 
         spinnersport.setAdapter(adapter);
         spinnersport.setOnItemSelectedListener(new MyOnItemSelectedListener());
         Object s = spinnersport.getSelectedItem();
         System.out.println(s);
         
         spinnertuition = (Spinner) findViewById(R.id.spinnerTuition);
         ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                 this, R.array.tuition_array, android.R.layout.simple_spinner_item);
         adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinnertuition.setAdapter(adapter1);
         spinnertuition.setOnItemSelectedListener(new MyOnItemSelectedListener());
         
         spinnerregion = (Spinner) findViewById(R.id.spinnerRegion);
         ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                 this, R.array.region_array, android.R.layout.simple_spinner_item);
         adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinnerregion.setAdapter(adapter2);
         spinnerregion.setOnItemSelectedListener(new MyOnItemSelectedListener());
         
     }
     
     private void readCsvValues() {
 		BufferedReader file;
 		AssetManager assetManager = getAssets();
 
 		
 		try {
 			file = new BufferedReader(new InputStreamReader(assetManager.open("sample.csv")));
 			String line;
 			file.readLine();
 			while ((line = file.readLine()) != null) {
 				String[] lineParts = line.split(",");
 				String schoolName = lineParts[0];
 				String schoolType = lineParts[1];
 				String setting = lineParts[2];
 				String totalUndergrads = lineParts[3];
 				String tuitionInState = lineParts[4];
 				String tuitionOutOfState = lineParts[5];
 				String roomAndBoardCost = lineParts[6];
 				
 				School s = new School(schoolName, schoolType, setting, totalUndergrads, tuitionInState, tuitionOutOfState, roomAndBoardCost);
 				schools.add(s);
 			}
 			file.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 		
 	}
 
 	public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent,
             View view, int pos, long id) {
          Toast.makeText(parent.getContext(), parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
         }
         
         
 
         public void onNothingSelected(AdapterView parent) {
           // Do nothing.
         }
     }
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.search:{
 			var.clear();
 			var.add(mMajorView.getText().toString());
 			var.add(spinnersport.getSelectedItem().toString());
 			var.add(mPopView.getText().toString());
 			var.add(spinnertuition.getSelectedItem().toString());
 			var.add(mHouseView.getText().toString());
 			var.add(spinnerregion.getSelectedItem().toString());
 			
 			for (int i = 0; i < var.size(); i++) {
 				vars[i] = var.get(i);
 			}
 			
 			Bundle b = new Bundle();
 			b.putStringArray("strings", vars);
 			Bundle b2 = new Bundle();
 			b2.putParcelableArrayList("schools", schools);
 			Intent i = new Intent();
 			i.putExtras(b);
 			i.putExtras(b2);
 			i.setClass(this, SearchResults.class);
 			startActivity(i);
 		}
 		}
 		
 	}
 	
 	public ArrayList<String> getVars(){
 		return var;
 		
 	}
 }
