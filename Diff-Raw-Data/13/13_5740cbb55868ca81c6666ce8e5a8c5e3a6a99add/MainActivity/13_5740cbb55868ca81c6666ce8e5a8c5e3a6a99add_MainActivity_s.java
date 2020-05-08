 package com.itoxygen.publicsafety;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 
 public class MainActivity extends Activity {
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public List<String> item = null; 
 	public List<String> path = null;
 	public List<String> history = new ArrayList<String>(); //list of previously clicked on file paths 
 	public Spinner historySpinner;
 	LinearLayout layout;
 	public String root;
 	public ImageButton switchView;
 	public Button sortAlpha;
 	boolean isSorted;
 
 	//screen
 	int width,height;
 	Display display;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_main_tile);
 
 		//screen
 		display = getWindowManager().getDefaultDisplay();
 		
 		width = display.getWidth();
 		height = display.getHeight();
 		
 		
 		historySpinner = (Spinner)findViewById(R.id.historySpinner);
 		sortAlpha = (Button)findViewById(R.id.sortAlpha);
 		switchView = (ImageButton)findViewById(R.id.switchView);
 
 		root = Environment.getExternalStorageDirectory().getPath(); //gets the root path of SD card
 		history.add("Clear History"); //adds a "button" to clear history
 		loadSharedPrefs();
 		if(root != null) { getDir(root); } 
 		checkSort();
 
 		//switches from tile view to list view
 		switchView.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				Intent intent = new Intent(getApplicationContext(), MainActivityList.class);
 				finish();
 				startActivity(intent);
 			}	
 		});
 
 		//switches between ascending and descending sort
 		sortAlpha.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				if(isSorted) {
 					isSorted = false; //global boolean
 				}
 				else {
 					isSorted = true; //global boolean
 				}
 				saveSharedPrefs("Alpha");
 				checkSort();
 			}	
 		});
 
 		historySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int pos, long arg3) {
 				//getDir(historySpinner.getItemAtPosition(pos).toString());
 				if(historySpinner.getItemAtPosition(pos).toString().equals("Clear History")) {
 					history.clear(); //if clear history "button" is pressed, clear the spinner
 					history.add("Clear History"); //re-adds the "button"
 				}
 			}
 			public void onNothingSelected(AdapterView<?> arg0) { }
 		});
 
 	}
 
 	/**
 	 * Does the ascending and descending sorting and
 	 * changes the color of the button/text
 	 */
 	protected void checkSort() {
 
 		if(history.size() <= 1) {
 			getDir(root);
 		}
 		else {
 			getDir(history.get(history.size()-1).toString());
 		}
 
 		if (isSorted) {
 			//Alpha Sort
 			Collections.sort(item, String.CASE_INSENSITIVE_ORDER); //sorts the filenames
 			Collections.sort(path, String.CASE_INSENSITIVE_ORDER); //sorts the spinner
 			sortAlpha.setText("A > Z");
 			sortAlpha.setBackgroundColor(Color.BLACK);
 			sortAlpha.setTextColor(Color.YELLOW);
 		}
 		else {	
 			//Reverse Alpha Sort
 			Collections.sort(item, Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER)); //sorts the filenames
 			Collections.sort(path, Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER)); //sorts the spinner
 			sortAlpha.setBackgroundColor(Color.YELLOW);
 			sortAlpha.setText("Z > A");
 			sortAlpha.setTextColor(Color.BLACK);
 		}
 
 		populate(); //puts buttons on screen
 	}
 
 	/**
 	 * Saves SharedPreferences
 	 * @param name - name of the key in sharedPrefs
 	 */
 	public void saveSharedPrefs(String name) {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean(name, isSorted);
 		editor.commit();
 	}
 
 	/**
 	 * Loads from SharedPreferences
 	 */
 	public void loadSharedPrefs() {
 		SharedPreferences loadPrefs = getSharedPreferences(PREFS_NAME, 0);
 		isSorted = loadPrefs.getBoolean("Alpha", true);
 	}
 
 
 	/**
 	 * Method that gets the directory of the given path
 	 * @param dirPath - string of the specified path
 	 */
 	private void getDir(String dirPath) {
 		item = new ArrayList<String>();
 		path = new ArrayList<String>();
 		File f = new File(dirPath);
 		File[] files = f.listFiles();
 
 		if(!dirPath.equals(root)) {
 			item.add(root); //adds the root to the file directory
 			path.add(root); //adds the root to the history spinner
 			item.add("../"); //adds an "up" button to go up one folder
 			path.add(f.getParent()); 	
 		}
 
 		for(int i=0; i < files.length; i++) { //iterate thru the files
 			File file = files[i];
 			if(!file.isHidden() && file.canRead()) { 
 				path.add(file.getPath()); 
 				if(file.isDirectory()) {
 					item.add(file.getName() + "/"); //if the item is a folder
 				} else {
 					item.add(file.getName()); //if the item is a file
 				}
 			}	
 		}//end for loop
 
 	}//end of get Dir method
 
 	public void goToParent(View v) {
 		File file = new File(path.get(v.getId()));
 		if (file.isDirectory()) {
 			if(file.canRead()){
 				getDir(path.get(v.getId()));
 				if(isSorted) {
 					Collections.sort(item, String.CASE_INSENSITIVE_ORDER);
 					Collections.sort(path, String.CASE_INSENSITIVE_ORDER);
 				}
 				populate();
 			}
 			else{ 
 				new AlertDialog.Builder(MainActivity.this)
 				.setIcon(R.drawable.ic_launcher)
 				.setTitle("[" + file.getName() + "] folder can't be read!")
 				.setPositiveButton("OK", null).show(); 
 			}	
 		} else {
 			//this should not happen
 		}
 	}
 
 	/**
 	 * Helper method that sets up all the buttons
 	 * Creates 5 rows
 	 */
 	private void populate() {
 
 		layout = (LinearLayout) findViewById(R.id.lin);
 		layout.removeAllViews();
 		
 		int rotation = display.getRotation();
 		int columNum = 0;
 		
 		
 		
		 
 		if(rotation == 0 || rotation == 180){
 			columNum = 4;
 		}else{
			columNum = 8;
 		}
 		
 		int rowNum = item.size()/columNum;
 		
 		if(item.size()%columNum != 0)
 			rowNum++;
 		
 		
 		Log.e(root, "Item Size: "+item.size()+"");
		Log.e(root, "columNum: "+columNum+"");
		Log.e(root, "rowNum: "+rowNum+"");
 
 
 		for(int i = 0; i < rowNum; i++){		
 			LinearLayout imgRow = new LinearLayout(getApplicationContext());
 			imgRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			LinearLayout textRow = new LinearLayout(getApplicationContext());
 			imgRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			for(int j = 0; j < columNum; j++){
 				
 				if(j+(i*columNum) < item.size()){
 					
 					ImageButton imgBtn = new ImageButton(this);
 					
 					
 					imgBtn.setImageResource(R.drawable.psafety_folder);
 					
 					
 					imgBtn.setBackgroundDrawable(null);
 					imgBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 					imgBtn.setMaxWidth(width/columNum);
 					imgBtn.setMaxHeight(width/columNum);
 					imgBtn.setId(j + (i * columNum));
 					imgBtn.setOnClickListener(new ClickListener());
 
 					Button btn = new Button(this);
 					btn.setBackgroundDrawable(null);
 					btn.setText(item.get(j+(i*columNum)));
 					btn.setTextSize(14);
 					btn.setGravity(0);
 					btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 					btn.setWidth(width/columNum);
 					btn.setId(j + (i * columNum));
 					btn.setOnClickListener(new ClickListener());
 					
 					imgRow.addView(imgBtn);
 					textRow.addView(btn);
 					
 				}
 				
 			}
 			
 			layout.addView(imgRow);
 			layout.addView(textRow);
 		}
 
 		ArrayAdapter<String> historyList =
 				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, history);
 		historySpinner.setAdapter(historyList); //sets the history arraylist to populate the spinner
 		historySpinner.setSelection(history.size()-1); //sets the spinner to display the history correctly
 
 	}
 
 
 	/**
 	 * Class that is called when a button from the commandDialog is pressed
 	 */
 	class ClickListener implements OnClickListener {
 		public void onClick(View v) {
 			//Log.v("Click Occurs", "This is a click");
 			File file = new File(path.get(v.getId()));
 			history.add(path.get(v.getId()));
 			if (file.isDirectory()) {
 				if(file.canRead()){
 					getDir(path.get(v.getId()));
 					if(isSorted) {
 						Collections.sort(item, String.CASE_INSENSITIVE_ORDER);
 						Collections.sort(path, String.CASE_INSENSITIVE_ORDER);
 					}
 					populate();
 				}
 				else{ 
 					new AlertDialog.Builder(MainActivity.this)
 					.setIcon(R.drawable.ic_launcher)
 					.setTitle("[" + file.getName() + "] folder can't be read!")
 					.setPositiveButton("OK", null).show(); 
 				}	
 			}
 			else { 
 				//check file type
 				Shared.openPdf(file, MainActivity.this);
 				/*new AlertDialog.Builder(MainActivity.this)
 				.setIcon(R.drawable.ic_launcher)
 				.setTitle("[" + file.getName() + "]")
 				.setPositiveButton("OK", null).show();*/
 			}
 		}
 	}
 
 
 }
 
