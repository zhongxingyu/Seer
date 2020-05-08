 package com.jmuindi.cuprint;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ipaulpro.afilechooser.utils.FileUtils;
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 
 public class PrintActivity extends Activity  implements PrintCallBack {
 	// Request Code for On Activity Result 
 	private static final int ACTIVITY_REQUEST_CODE = 6387;	
 	public static final String TAG = "PrintActivity";
 	public static final String SUCCESS_STATUS = "Job Sent Successfully";
 	public static final String ERROR_STATUS = "Job Failed to be sent";
 	public static final String ACTIVITY_STATE = "PrintActivityState"; 
 	private static final boolean DEBUG = false; 
 	private Dialog progressDialog = null;
 
 	public HashMap<String, ArrayList<String>> printMap = null;
 	public File file = null; 
 	
 	
 	public void d(String msg) {
 		if (DEBUG)
 			Log.d(TAG,msg);
 	}
 	
 	public void e(String msg) {
 		if (DEBUG)
 			Log.e(TAG, msg);
 	}
 	
 	public void v(String msg) {
 		if (DEBUG)
 			Log.v(TAG, msg);
 	}
 	public void w(String msg) {
 		if (DEBUG)
 			Log.w(TAG, msg);
 	}
 	
 	
 	/**
 	 * Show Short Toast Message. 
 	 * @param msg
 	 */
 	public void sm(String msg) {
 		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Show Long Toast.
 	 * @param msg
 	 */
 	public void sml(String msg) {
 		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
 	}
 	
 		
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		
 	}
 	
 	private boolean isFirstRun() {
 		SharedPreferences sp = this.getPreferences(MODE_PRIVATE); 		
 		boolean doneFirstRun = sp.getBoolean("firstruncompleted", false);
 		if (doneFirstRun) {
 			return false;
 		} else {
 			return true;
 		}		
 	}
 	
 	private void doFirstRun() {		
 		showHelpDialog();  
 		SharedPreferences sp = this.getPreferences(MODE_PRIVATE); 
 		Editor ed = sp.edit();
 		ed.putBoolean("firstruncompleted", true);
 		ed.commit(); 
 	}
 	
 	
 	
 	private void saveCurrentActivityState() {
 		PrintActivityState activityState = getCurrentState();
 		String stateData = Util.saveObjectToBase64(activityState);
 		SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
 		Editor ed = sp.edit();		
 		ed.putString(ACTIVITY_STATE, stateData);		
 		boolean success = ed.commit(); 
 		if (!success) {
 			e("Failed to persist/save current activity state");
 		} else {
 			v("Saving current activity state completed successfully");
 		}
 	}
 			
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);					
 		setContentView(R.layout.activity_print);
 		initBuildingSpinner();
 		initProgressDialog();		
 		
 		// Restore Persisted Activity State 
 		restoreActivityState(); 
 		
 		if (savedInstanceState == null) { // This is a brand new Run.			
 			hideStatusBar(); 
 		}
 		
 		// Show Help First on First Run. 
 		if (isFirstRun()) {
 			doFirstRun(); 
 		}
 		
 		// Get intent, action and MIME type
 	    Intent intent = getIntent();
 	    String action = intent.getAction();
 	    String type = intent.getType();	    
 	    
 	    if (Intent.ACTION_VIEW.equals(action) && type != null) {
 	    	// Handle Intent and get the data as a file. 
 	    	Uri uri = intent.getData();
 	    	try {
 				// Create a file instance from the URI
 				File file = FileUtils.getFile(uri);	
 				d("Is Uri Null? " + String.valueOf(uri == null));
 				setFileToPrint(file, uri.getLastPathSegment());
 			} catch (Exception e) {
 				e("File Loading error when " +
 				  "preloading it from an intent" + e);
 				sm("Autoloading Failed, Please Manually Select File to Print");
 			}
 	    }
 	}
 
 	private void restoreActivityState() {
 		SharedPreferences sp = this.getPreferences(MODE_PRIVATE); 
 		String data = sp.getString(ACTIVITY_STATE, null);
 		if (data != null) {
 			d("About to Restore Activity State");
 			Object obj = Util.getObjectFromBase64(data);
 			PrintActivityState activityState = (PrintActivityState) obj; 
 			loadSavedState(activityState);
 		} else {
 			d("Did not restore activity state");
 		}
 	}
 	
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		
 		int id = item.getItemId(); 
 		switch (id) {
 		case R.id.menuItemHelp: {
 			createHelpDialog().show();
 			return true;
 		}
 		default:{
 			return false;
 		}
 		}
 	}
 	
 	private void showHelpDialog() {
 		createHelpDialog().show();
 	}
 	
 	private AlertDialog createHelpDialog() {		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.help_dialog_title);
         builder.setMessage(R.string.help_message);        
         builder.setPositiveButton(android.R.string.ok, null);         		
         return builder.create();						
 	}
 	
 	
 	private PrintActivityState getCurrentState() {
 		String building = getSelectedBuilding(); 
 		String printer = getSelectedPrinter(); 
 		String filename = getCurrentFilename();
 		String statusMessage= getCurrentStatusMsg();
 		PrinterOptions options = getCurrentPrinterOptions();
 		boolean statusVisible = isStatusBarVisible(); 
 		
 		PrintActivityState pas = new PrintActivityState();		
 		pas.setBuilding(building);
 		pas.setPrinter(printer);
 		pas.setPrintOptions(options);
 		pas.setFilename(filename);
 		pas.setFile(this.file);
 		pas.setStatusmessage(statusMessage);
 		pas.setStatusbarvisible(statusVisible);
 		
 		return pas; 
 	}
 	
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {		
 		super.onSaveInstanceState(outState);		
 		d("Attempting to persist current activity state ...");
 		saveCurrentActivityState();
 	}
 	
 	private boolean isStatusBarVisible() {
 		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutStatusBar);
 		return ll.getVisibility() == View.VISIBLE ;
 	}
 	
 	private String getCurrentStatusMsg() {
 		TextView tv = (TextView) findViewById(R.id.TextViewStatusMsg);
 		return tv.getText().toString();
 	}
 	
 	
 	private int getPrinterIndex(String printer) {
 		return getItemIndexInSpinner(R.id.spinnerPrinter, printer);
 	}
 	
 	private int getBuildingIndex(String building) {
 		return getItemIndexInSpinner(R.id.spinnerBuilding, building);
 	}
 	/**
 	 * Returns index in which given item is in the spinner. 
 	 * If item is not found, -1 is returned. 
 	 * @param item - item to look for.
 	 * @param spinnerId - Resource Id of the Spinner 
 	 * @return 
 	 */
 	private int getItemIndexInSpinner(int spinnerId, String item) {
 		Spinner spinner = (Spinner) findViewById(spinnerId); 
 		int count = spinner.getAdapter().getCount();
 		for (int i = 0; i < count; ++i) {
 			String s = (String) spinner.getItemAtPosition(i); 
 			if (s.equals(item)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	private void loadPrinter(String building, String printer) {
 		
 		Spinner sPrinter = (Spinner) findViewById(R.id.spinnerPrinter); 
 		Spinner sBuilding = (Spinner) findViewById(R.id.spinnerBuilding);
 		
 		int buildingIndex = getBuildingIndex(building);
 		v("buidling Index == " + buildingIndex);
 		int printerIndex = getPrinterIndex(printer);
 		
 		sPrinter.setSelection(printerIndex); 
 		sBuilding.setSelection(buildingIndex);
 	}
 	
 	private void loadPrinterOptions(boolean doubleSided, boolean collate, 
 								    int copies) {
 		CheckBox chkDoubleSided = (CheckBox) findViewById(R.id.checkBoxDoubleSided);
 		CheckBox chkCollate = (CheckBox) findViewById(R.id.checkBoxCollate); 
 		EditText etCopies = (EditText) findViewById(R.id.editTextCopies);		
 		
 		chkDoubleSided.setChecked(doubleSided);
 		chkCollate.setChecked(collate); 
 		etCopies.setText(String.valueOf(copies));
 	}
 	
 	private void loadFilename(String filename) {
 		TextView tv = (TextView) findViewById(R.id.textViewFilename); 
 		tv.setText(filename); 
 	}
 	
 	private void loadStatusMessage(String statusMessage, boolean statusVisible) {
 		if (statusMessage != null) {
 			if (statusMessage.toLowerCase().contains("success")) {
 				updateStatusBar(R.drawable.success_checkmark, SUCCESS_STATUS);				
 			} else { // error
 				// Note: this also catches the first state app is in on startup 
 				// when it's empty. 
 				updateStatusBar(R.drawable.failure_checkmark, ERROR_STATUS);
 			}
 			if (statusVisible) {
 				showStatusBar();
 			} else {
 				hideStatusBar(); 
 			}
 		} else {
 			hideStatusBar();
 		}
 	}
 	
 	
 	
 	private void loadSavedState(PrintActivityState pas) {
 		if (pas != null) {
 			String building = pas.getBuilding(); 
 			String printer = pas.getPrinter();  
 			String filename = pas.getFilename();
 			boolean collate = pas.isCollate();
 			boolean doubleSided = pas.isDoubleSided(); 
 			boolean statusBarVisible = pas.isStatusbarvisible();
 			int copies = pas.getCopies(); 
 			File file = pas.getFile(); 
 			String statusMessage = pas.getStatusmessage();
 			
 			d("building = " + building);
 			d("printer = " + printer);
 			d("filename = " + filename);
 			d("collate ?  " + collate);
 			d("doulbeseid= " + doubleSided);
 			d("copies = " + copies);
 			
 			
 			this.file = file;
 			loadPrinter(building, printer); 
 			loadPrinterOptions(doubleSided, collate, copies); 
 			loadFilename(filename);
 			loadStatusMessage(statusMessage, statusBarVisible);			
 			enablePrintButton(); 
 		}
 	}
 
 	private void disablePrintButton() {		
 		Button btn = (Button) findViewById(R.id.btnPrint);
 		if (btn.isEnabled()) {
 			btn.setEnabled(false);
 		}
 
 	}
 	
 	/**
 	 * Enables Print we have a file set. 
 	 */
 	private void enablePrintButton() {
 		if (havePrintableFile()) {
 			Button btn = (Button)  findViewById(R.id.btnPrint); 
 			if (!btn.isEnabled()) {
 				btn.setEnabled(true);
 			}
 		} else {
 			d("Not enabling Print Button because File is" +
 			  " either null or non-exitent");
 		}
 	}
 	
 	private void onClickImageButtonMinus(ImageButton ib) {
 		EditText et = (EditText) findViewById(R.id.editTextCopies);
 		int current = Integer.parseInt(et.getText().toString());
 		if (current > 1) {
 			String newValue = String.valueOf(current - 1);
 			et.setText(newValue);				
 		}
 	}
 	
 	private void onClickImageButtonPlus(ImageButton ib) {
 		EditText et = (EditText) findViewById(R.id.editTextCopies);
 		int current = Integer.parseInt(et.getText().toString());
 		String newValue = String.valueOf(current + 1); 
 		et.setText(newValue); 
 	}
 		
 	
 	private boolean havePrintableFile() {
 		if (this.file == null) {
 			return false;
 		} else if (!this.file.exists()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private void setFileToPrint(File f, String uriName) {
 		if (f == null) {
 			w("Setting null file to Print");
 		} else if (!f.exists()) {
 			w("Given non-existent file to Print - ignoring...");
 		} else {
 			v("Setting this file for print: " + f.getAbsolutePath());
 			this.file = f; 
 			
 			// Update selected file in UI 
 			String fname = "";
 			if (uriName.trim().length() > 0) {
 				fname = uriName;
 				d("Using URI Name: " + uriName);
 			} else if (f.getName().trim().length() > 0) {
 				d("Using FName: " + f.getName());
 				fname = f.getName(); 
 			} else {
 				e("File name not available");
 			}
 			d("File name was finally set to " + fname);
 			TextView tv = (TextView) findViewById(R.id.textViewFilename);
 			tv.setText(fname); 
 			
 			// Enable Print Button
 			enablePrintButton();
 		}
 	}
 	
 	private void initBuildingSpinner() {
 		
 		// Initialize the Print Map if Needed
 		if (printMap == null) {
 			printMap = Printers.getPrintersList(this);			
 		}
 		
 		Spinner spinner = (Spinner) findViewById(R.id.spinnerBuilding);
 		ArrayList<String> buildings = new ArrayList<String>(20);
 		for (String building : printMap.keySet()) {
 			buildings.add(building);
 		}
 		Collections.sort(buildings);
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 			android.R.layout.simple_spinner_item, buildings);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 
 		
 		
 		// Attach OnItem Selected Listener so that we update the printers Spinner 
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				String building = (String) parent.getItemAtPosition(pos);
 				updatePrinterSpinner(building);
 				
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {				
 				// Do nothing
 			}			
 		});
 		
 		// Initialize Printer Spinner. 
 		updatePrinterSpinner(buildings.get(0));
 	}
 			
 	private void showFileBrowser() {
 		// Use the GET_CONTENT intent from the utility class
 		Intent target = FileUtils.createGetContentIntent();				
 		// Create the chooser Intent
 		Intent intent = Intent.createChooser(
 				target, "Select File to Print");
 		
 		
 		try {
 			startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
 		} catch (ActivityNotFoundException e) {
 			// The reason for the existence of aFileChooser
 		}				
 	}
 	
 	private void updatePrinterSpinner(String building) {		
 		Spinner spinner = (Spinner) findViewById(R.id.spinnerPrinter);
 		ArrayList<String> printers = printMap.get(building);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, printers);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);		
 	}
 	
 	
 	public void onClick(View v) {
 		
 		int id = v.getId(); 
 		switch (id) {
 		case R.id.ImageButtonMinus: {			
 			ImageButton ib = (ImageButton) v;
 			onClickImageButtonMinus(ib);
 			break;
 		}
 		case R.id.ImageButtonPlus: {			
 			ImageButton ib = (ImageButton) v; 
 			onClickImageButtonPlus(ib);
 			break;
 		}
 
 		case R.id.btnBrowse: {
 			showFileBrowser(); 
 			break;
 		}
 		
 		case R.id.btnPrint: {
 			onClickButtonPrint(); 
 		}
 		default:
 			break;
 		}
 		
 	}
 
 	private PrinterOptions getCurrentPrinterOptions() {
 		CheckBox doubleSided = (CheckBox) findViewById(R.id.checkBoxDoubleSided);
 		CheckBox collate = (CheckBox) findViewById(R.id.checkBoxCollate); 
 		EditText copies = (EditText) findViewById(R.id.editTextCopies);		
 		int numCopies = Integer.parseInt(copies.getText().toString());
 		return new PrinterOptions(doubleSided.isChecked(), collate.isChecked(), numCopies); 
 	}
 	
 	private String getSelectedPrinter() {
 		Spinner sPrinter = (Spinner) findViewById(R.id.spinnerPrinter); 
 		String printer  = (String) sPrinter.getSelectedItem();
 		return printer;
 	}
 	
 	private String getSelectedBuilding() {
 		Spinner sBuilding = (Spinner) findViewById(R.id.spinnerBuilding); 
 		String building = (String)  sBuilding.getSelectedItem(); 
 		return building; 
 	}
 	
 	private String getCurrentFilename() { 
 		TextView tv = (TextView) findViewById(R.id.textViewFilename);
 		return tv.getText().toString(); 
 	}
 	
 	private void onClickButtonPrint() {
 
 		if (!Util.isNetworkAvailable(this)) {
 			e("Cannot print because there is not active internet connection");
 			sm("An active Internet connection is required to print. Please ensure " +
 			   "you're connected to a netowrk and try again");
 			return; 
 		}
 		
 		// Get Selected Printer
 		String printer = getSelectedPrinter(); 
 		String building = getSelectedBuilding(); 
 		PrinterOptions options = getCurrentPrinterOptions(); 
 		
 		// show progress dialog 
 		this.progressDialog.show(); 		
 
 		// Send the Printer Job		
 		CUPrint.print(building, printer, options, this.file, this);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, 
 									Intent data) {
 		switch (requestCode) {
 		case ACTIVITY_REQUEST_CODE:	
 			// If the file selection was successful
 			if (resultCode == RESULT_OK) {		
 				if (data != null) {
 					// Get the URI of the selected file
 					final Uri uri = data.getData();
 					try {
 						// Create a file instance from the URI
 						final File file = FileUtils.getFile(uri);
 						String ext = FileUtils.getExtension(uri.getPath());
 						d("**** File Extension == " + ext);
 						if (Util.canPrintExtension(ext)) {
 							setFileToPrint(file, uri.getLastPathSegment());
 						} else { // Cannot Print File. 							
 							sml("Cannot Print Files of Extension:'" + ext +"' "+									
 							    "Please choose a supported document type and " +
 							    "try again");
 							clearSelectedFile();
 						}
 					} catch (Exception e) {
						e("File select error: " + e);
 					}
 				}
 			} 
 			break;
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	public void test() {		
 		initBuildingSpinner();
 		initProgressDialog();
 	}
 	
 	
 	public void asyncHttpTest() {
 		System.out.println("Making ASYNC Request2 ...");		
 		AsyncHttpClient client = new AsyncHttpClient();
 		String url = "http://httpbin.org/get?q=search";
 		client.get(url, new AsyncHttpResponseHandler() {							
 			@Override
 		    public void onSuccess(String response) {		        
 		    	System.out.println(response);
 		    }
 		});					
 	}
 	public void asyncHttpPrintTest() {
 		CUPrint.loadTestFile(this);
 		CUPrint.testPrint();
 	}
 	
 	
 	public void loadPrinters() {
 		Printers.getPrintersList(this);
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.print, menu);
 		return true;
 	}
 
 	private void initProgressDialog() {
 		if (this.progressDialog == null) {
 			ProgressDialog dialog = new ProgressDialog(this);			
 			dialog.setMessage("Sending Print Job ...");
 			dialog.setIndeterminate(true);
 			dialog.setCancelable(false);
 			this.progressDialog = dialog;
 		}
 	}
 
 	private void updateStatusBar(int imageId, String status) {
 		ImageView iv = (ImageView) findViewById(R.id.imageViewStatusIcon);
 		TextView tv = (TextView) findViewById(R.id.TextViewStatusMsg);
 		
 		// Update Image 
 		iv.setImageResource(imageId);
 		
 		// update status message
 		String time = Util.getCurrentTime(); 		
 		String msg = String.format("%s - %s", time, status);
 		tv.setText(msg);
 	}
 	
 	private void printJobSuccess() {
 		int imageId = R.drawable.success_checkmark;
 		String status = SUCCESS_STATUS;
 		updateStatusBar(imageId, status);
 	}
 	
 	private void printJobFailure() {
 		int imageId = R.drawable.failure_checkmark; 
 		String status = ERROR_STATUS;
 		updateStatusBar(imageId, status);
 	}
 			
 	private void resetFilenameUIText() {		
 		TextView tv = (TextView) findViewById(R.id.textViewFilename);
 		String msg = "Select File to Print";
 		tv.setText(msg); 
 	}
 	
 	private void clearSelectedFile() {
 		this.file = null; 
 		disablePrintButton();
 		resetFilenameUIText(); 
 	}
 	
 	private void showStatusBar() {
 		// Enable the status message bar if needed
 		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutStatusBar);
 		if (ll.getVisibility() != View.VISIBLE) {
 			ll.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void hideStatusBar() {
 		// Hide the status message bar if needed
 		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutStatusBar);
 		if (ll.getVisibility() == View.VISIBLE) {
 			ll.setVisibility(View.INVISIBLE);
 		}
 	}
 	
 	@Override
 	public void done(boolean success) {				 				
 		if (success) {
 			printJobSuccess();
 		} else {
 			printJobFailure(); 
 		}
 		this.progressDialog.dismiss(); 
 		showStatusBar(); 
 		
 		
 	}
 
 }
