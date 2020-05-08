 package com.caseynbrown.moneymanager;
 
 import static android.provider.BaseColumns._ID;
 import static com.caseynbrown.moneymanager.ConstantsDB.AMOUNT_ENTRY;
 import static com.caseynbrown.moneymanager.ConstantsDB.AMOUNT_PEOPLE;
 import static com.caseynbrown.moneymanager.ConstantsDB.DATE_ENTRY;
 import static com.caseynbrown.moneymanager.ConstantsDB.NAME_PEOPLE;
 import static com.caseynbrown.moneymanager.ConstantsDB.NOTES_ENTRY;
 import static com.caseynbrown.moneymanager.ConstantsDB.TABLE_NAME_ENTRY;
 import static com.caseynbrown.moneymanager.ConstantsDB.TITLE_ENTRY;
 import static com.caseynbrown.moneymanager.ConstantsDB.USER_ENTRY;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // This class is used on the "Quick Balance View" and "Edit Balances" screens.
 public class NewEntryActivity extends Activity{
 
 	// DB People
 	private DBData database;
 	private static String[] FROM = {_ID, NAME_PEOPLE, AMOUNT_PEOPLE};
 	private static String ORDER_BY = NAME_PEOPLE + " DESC";
 	private static int[] TO = {0, R.id.rowName, 0 };
 
 	// Other
 	int numIds;
 	ArrayList<Integer> selectedIds;
 	ArrayList<String> selectedNames;
 	protected static final int PEOPLE_LIST_CREATE = 0;
 	protected static final int OWE_CREATE = 1;
 	TextView whoBox;
 	EditText whereBox, amountBox, notesBox;
 	CheckBox check;
 	String plusminus;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         this.numIds = 0;
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.newentry);
         database = new DBData(this);
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 
         // Get the interactive elements from screen.
         this.whoBox = ((TextView) findViewById(R.id.entry_whoBox));
         this.whereBox = ((EditText) findViewById(R.id.entry_whereBox));
         this.amountBox = ((EditText) findViewById(R.id.entry_amountBox));
         this.notesBox = ((EditText) findViewById(R.id.entry_notesBox));
         this.check = ((CheckBox) findViewById(R.id.entry_divideCheckBox));
 
         // Add click listeners for the buttons
         this.whoBox.setOnClickListener(
         		new EditText.OnClickListener(){
         			@Override public void onClick(View view){
         				Intent listIntent = new Intent(view.getContext(), PeopleListActivity.class);
         				startActivityForResult(listIntent, PEOPLE_LIST_CREATE);
         			}
         		});
 
         this.amountBox.setOnClickListener(
         		new EditText.OnClickListener(){
         			@Override public void onClick(View view){
         				showModal();
         			}
         		});
 
         // Quick Balance
         ((Button) findViewById(R.id.submitButton)).setOnClickListener(
         		new Button.OnClickListener() {
         			@Override
         			public void onClick(View view){
         				System.out.println("clicked submit button");
         				// Check that the proper fields are filled.
         				ArrayList<String> invalidFields = inputValidation();
         				if (invalidFields.size() > 0){
         					// Do a toast
         					makeToastError(invalidFields);
         				} else {
         					try {
         						updateSQL();
         					} catch (Exception e){
         						e.printStackTrace();
         					}
         					finish();
         				}
     				}
         		});
 
         ((TextView) findViewById(R.id.entry_divideText)).setOnClickListener(
         		new TextView.OnClickListener() {
         			@Override
         			public void onClick(View view){
         				toggleCheckbox();
         			}
         		});
 	}
 
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 
 		this.database.close();
 	}
 
 	public void showModal(){
 		ModalAmount d = new ModalAmount(this, new OnReadyListener());
 		d.show();
 	}
 
 	// Get the amount back from the Modal Listener
 	private class OnReadyListener implements ModalAmount.ReadyListener{
 
 		@Override
 		public void ready(boolean negative, String amount) {
 			HelperMethods.updateAmountBox(amountBox, negative, amount);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		CheckBox check = ((CheckBox) findViewById(R.id.entry_divideCheckBox));
 		if (this.numIds > 0){
 
 			// Set the button to display the users
 	        String displayNames = "";
 	        for (int i = 0; i < this.selectedNames.size(); i++){
 	        	String s = this.selectedNames.get(i);
 	        	if (i + 1 == this.selectedNames.size()){
 	        		displayNames = displayNames + s;
 	        	} else {
 	        		displayNames = displayNames + s +"\n";
 	        	}
 	        }
         	this.whoBox.setText(displayNames);
 
 	        // Enable the divide checkbox
         	if (this.numIds > 1){
 	        	check.setEnabled(true);
         	}
         } else {
         	this.whoBox.setText("");
         	check.setEnabled(false);
         }
 	}
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data){
     	super.onActivityResult(requestCode, resultCode, data);
     	switch(requestCode){
     		case(PEOPLE_LIST_CREATE) : {
     			if (resultCode == Activity.RESULT_OK){
     				// Update instance variables
     				this.selectedIds = data.getIntegerArrayListExtra("ids");
     				this.selectedNames = data.getStringArrayListExtra("names");
     				numIds = selectedIds.size();
     			}
 			}
 		}
     }
 
     private void updateSQL(){
 
     	// Extract the data from the input fields.
     	int amount = HelperMethods.dollarToInt(this.amountBox.getText().toString());
     	String title = this.whereBox.getText().toString();
     	String notes = this.notesBox.getText().toString();
 
     	try {
     		// Update for multiple users
     		if (this.selectedIds.size() > 1){
 
     			// Determine the amount for the entry
 	    		int newAmount = 0;
 	    		if (this.check.isChecked()){
 	    			int nonRounded = amount / this.selectedIds.size();
 	    			newAmount = nonRounded;
 	    		} else {
 	    			newAmount = amount;
 	    		}
 
 	    		// Update for each user.
 	    		SQLiteDatabase db = database.getWritableDatabase();
 	    		for (int i = 0; i < this.selectedIds.size(); i++){
 	    			int id = this.selectedIds.get(i);
 	    			addNewEntry(db, id, newAmount, title, notes);
 	    		}
 
 	    	// Update for one user
 	    	} else {
 	    		// Add a new entry for that user
 	    		SQLiteDatabase db = database.getWritableDatabase();
 	    		addNewEntry(db, this.selectedIds.get(0), amount, title, notes);
 
 	    	}
     	} finally {
     		database.close();
     	}
 	}
 
     private void addNewEntry(SQLiteDatabase db, int id, int amount, String title, String notes){
     	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
     	Date dateType = new Date();
     	String date = dateFormat.format(dateType);
 
 		ContentValues values = new ContentValues();
     	values.put(USER_ENTRY, id);
     	values.put(DATE_ENTRY, date);
     	values.put(TITLE_ENTRY, title);
     	values.put(AMOUNT_ENTRY, amount);
     	values.put(NOTES_ENTRY, notes);
     	db.insertOrThrow(TABLE_NAME_ENTRY, null, values);
     }
 
     // Check that there all necessary fields are filled.
     private ArrayList<String> inputValidation(){
     	ArrayList<String> returnString = new ArrayList<String>();
 
     	String whoString = this.whoBox.getText().toString();
     	if (whoString.equals("") || whoString == null){
     		returnString.add("Who");
     	}
 
     	String whereString = this.whereBox.getText().toString();
     	if (whereString.equals("") || whereString == null){
     		returnString.add("Where");
     	}
 
     	String amountString = this.amountBox.getText().toString();
     	if (amountString.equals("") || amountString == null){
     		returnString.add("Amount");
 		}
 
     	return returnString;
    }
 
     private void makeToastError(ArrayList<String> input){
     	String errorMessage = "Please fill in the following fields: ";
     	for (String s: input){
     		errorMessage = errorMessage + s+" ";
     	}
     	Toast t = new Toast(this);
     	TextView error = new TextView(this);
     	error.setText(errorMessage);
     	error.setTextColor(Color.WHITE);
     	error.setBackgroundColor(Color.BLACK);
     	t.setView(error);
     	t.show();
     }
 
     private void toggleCheckbox(){
     	if (this.check.isEnabled()){
 			this.check.toggle();
 		}
     }
 }
