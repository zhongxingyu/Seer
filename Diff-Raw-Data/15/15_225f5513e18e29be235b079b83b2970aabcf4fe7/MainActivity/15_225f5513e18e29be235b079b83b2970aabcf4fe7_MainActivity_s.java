 package ca.on.rom.romsearch;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Spinner;
 
 public class MainActivity extends Activity {
 	
 	public static final String EXTRA_MESSAGE = "ca.on.rom.ROMSEARCH.MESSAGE";
 	public static final String EXTRA_SAVEFILE = "ca.on.rom.ROMSEARCH.SAVEFILE";
 	
 	public String[] exhibitArray;
 	public Double[] completionArray;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//GridData.setupTables(getApplicationContext());
 		setContentView(R.layout.activity_main);
 		loadSave();
 //		onCreateSpinner();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 //	public void onCreateSpinner() {
 //		Spinner spinner = (Spinner) findViewById(R.id.exhibit_spinner);
 //		//Create an ArrayAdapter using the string array and a default spinner layout
 //		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 //			//	R.array.exhibit_array, android.R.layout.simple_spinner_dropdown_item);
 //		//Specify the layout to use when the list of choices appears
 //		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 //		String spinnerArray[] = makeSpinnerArray();
 //		SpinnerArrayAdapter adapter = new SpinnerArrayAdapter (this, 
 //				android.R.layout.simple_spinner_dropdown_item, spinnerArray);
 //		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 //		//Apply the adapter to the spinner
 //		spinner.setAdapter(adapter);
 //	}
 //	
 //	public void chooseExhibit(View view) {
 //		Intent intent = new Intent(this, DisplayExhibitActivity.class);
 //		//get name
 //		Spinner spinner = (Spinner) findViewById(R.id.exhibit_spinner);
 //		String exhibit = spinner.getSelectedItem().toString().split(" - ")[0]; //get name only
 //		//get save data
 //		SharedPreferences sharedPref = this.getSharedPreferences(exhibit, Context.MODE_PRIVATE);
 //		String savedata = sharedPref.getString(exhibit, "000000000");
 //		//send name and save data
 //		intent.putExtra(EXTRA_MESSAGE, exhibit);
 //		intent.putExtra(EXTRA_SAVEFILE, savedata);
 //		startActivity(intent);
 //		
 //	}
 	
 	public void loadSave() {
 		ExhibitData eData; //exhibit data structure
 		SharedPreferences sData; //exhibit save data string from sharedpref
 		Double completion; //% completion
 		ArrayList<Double> completionList = new ArrayList<Double>();
 		exhibitArray = getResources().getStringArray(R.array.exhibit_array);
 		//get all completion values
 		for (int i = 0; i < exhibitArray.length; i++) {
 			sData = this.getSharedPreferences(exhibitArray[i], Context.MODE_PRIVATE);
 			eData = new ExhibitData(sData.getString(exhibitArray[i], "000000000"));
 			completion = eData.getCompletion();
 			completionList.add(completion);
 		}
 		Double[] completionArray = new Double[completionList.size()];
 		completionArray = completionList.toArray(completionArray);
 		this.completionArray = completionArray;
 	}
 	
 	public String[] makeSpinnerArray() {
 		String[] spinnerArray = new String[exhibitArray.length];
 		//combine exhibit name and completion for spinnerArray
 		String exhibit;
 		String completion;
 		double val;
 		DecimalFormat df2 = new DecimalFormat("###.##");
 		for (int i = 0; i < exhibitArray.length; i++) {
 			exhibit = exhibitArray[i];
 			val = completionArray[i] * 100;
 			//format double to 2 decimal places
 			val = Double.valueOf(df2.format(val));
 			completion = Double.toString(val);
 			spinnerArray[i] = exhibit + " - " + completion + "%";
 		}
 		return spinnerArray;
 	}
 	
 	public void gotoEgypt(View view) {
 		Intent intent = new Intent(this, DisplayExhibitActivity.class);
 		String exhibit = "Galleries of Africa: Egypt";
		SharedPreferences sharedPref = this.getSharedPreferences(exhibit, Context.MODE_PRIVATE);
		String savedata = sharedPref.getString(exhibit, "000000000");
		intent.putExtra(EXTRA_SAVEFILE, savedata);
 		intent.putExtra(EXTRA_MESSAGE, exhibit);
 		startActivity(intent);
 	}
 
 	public void gotoGreece(View view) {
 		Intent intent = new Intent(this, DisplayExhibitActivity.class);
 		String exhibit = "Gallery of Greece";
		SharedPreferences sharedPref = this.getSharedPreferences(exhibit, Context.MODE_PRIVATE);
		String savedata = sharedPref.getString(exhibit, "000000000");
		intent.putExtra(EXTRA_SAVEFILE, savedata);
 		intent.putExtra(EXTRA_MESSAGE, exhibit);
 		
 		startActivity(intent);
 	}
 }
