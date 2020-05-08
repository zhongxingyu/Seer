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
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 
 public class InfotecActivity extends Activity implements OnClickListener, Serializable {
 	private Button search;
 	private ArrayList<School> schools = new ArrayList<School>();
 	private ArrayList<String> var = new ArrayList<String>();
 	private String[] vars = new String[5];
 	private String[] majors;
 	private Spinner spinnersport;
 	private Spinner spinnertuition;
 	private Spinner spinnerregion;
 	private Spinner spinnerTU;
 	private Spinner spinnerSetting;
 	private EditText mMajorView;
 	private EditText mHouseView;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 //		WHEN SENDING TO COLLEGE PAGE, BUNDLE THIS WAY!
 //		Bundle bundle = new Bundle();
 //		bundle.putParcelable("school", new School("Millard West","Liberal Arts", "Urban", "100", "200","300", "50"));
 //		i.putExtras(bundle);
 //		startActivity(i);
 		
 		readCsvValues();
 
 		mMajorView = (EditText) findViewById(R.id.major2);
 
 		search = (Button) findViewById(R.id.search);
 		search.setOnClickListener(this);
 
 		spinnertuition = (Spinner) findViewById(R.id.spinnerTuition);
 		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
 				this, R.array.tuition_array,
 				android.R.layout.simple_spinner_item);
 		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnertuition.setAdapter(adapter1);
 		spinnertuition
 				.setOnItemSelectedListener(new MyOnItemSelectedListener());
 
 		spinnerregion = (Spinner) findViewById(R.id.spinnerRegion);
 		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
 				this, R.array.region_array,
 				android.R.layout.simple_spinner_item);
 		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerregion.setAdapter(adapter2);
 		spinnerregion.setOnItemSelectedListener(new MyOnItemSelectedListener());
 		
 		spinnerTU = (Spinner) findViewById(R.id.spinnerPop);
 		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
 				this, R.array.pop_array,
 				android.R.layout.simple_spinner_item);
 		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerTU.setAdapter(adapter3);
 		spinnerTU.setOnItemSelectedListener(new MyOnItemSelectedListener());
 		
 		spinnerSetting = (Spinner) findViewById(R.id.spinnerSetting);
 		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
 				this, R.array.setting_array,
 				android.R.layout.simple_spinner_item);
 		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerSetting.setAdapter(adapter4);
 		spinnerSetting.setOnItemSelectedListener(new MyOnItemSelectedListener());
 
 	}
 
 	private void readCsvValues() {
 		BufferedReader file;
 		AssetManager assetManager = getAssets();
 
 		try {
 			file = new BufferedReader(new InputStreamReader(
					assetManager.open("sample.csv")));
 			String line;
 			//file.readLine();
 			while ((line = file.readLine()) != null) {
 				String[] lineParts = line.split(",");
 				String schoolName = lineParts[0];
 				String state = lineParts[1];
 				String setting = lineParts[2];
 				String totalUndergrads = lineParts[3];
 				String tuitionInState = lineParts[4];
 				String majorsString = lineParts[5];
 				majors = majorsString.split("\\|");
 				//String tuitionOutOfState = lineParts[5];
 				//String roomAndBoardCost = lineParts[6];
 
 				School s = new School(schoolName, setting,	state,
 						totalUndergrads, tuitionInState, /*tuitionOutOfState*/majors
 						/*roomAndBoardCost*/);
 				
 				schools.add(s);
 			}
 			file.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 
 		}
   
 	}
 
 	public class MyOnItemSelectedListener implements OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 		//	Toast.makeText(parent.getContext(),
 			//		parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG)
 				//	.show();
 		}
 
 		public void onNothingSelected(AdapterView parent) {
 			// Do nothing.
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.search: {
 			var.clear();
 			var.add(mMajorView.getText().toString());
 			var.add(spinnerTU.getSelectedItem().toString());
 			var.add(spinnertuition.getSelectedItem().toString());
 			var.add(spinnerregion.getSelectedItem().toString());
 			var.add(spinnerSetting.getSelectedItem().toString());
 
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
 
 	public ArrayList<String> getVars() {
 		return var;
 
 	}
 }
