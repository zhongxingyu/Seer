 package pl.xt.jokii.carserv;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import pl.xt.jokii.db.CarServEntry;
 import pl.xt.jokii.db.CarServProviderMetaData;
 import pl.xt.jokii.db.DbUtils;
 import pl.xt.jokii.db.CarServProviderMetaData.CarServTableMetaData;
 import pl.xt.jokii.reminder.AlarmUtil;
 import pl.xt.jokii.utils.Connectivity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class NewEntry extends Activity{
 	private int selectedType = 0;
 	private long EntryId = 0;
 	private CarServEntry carServEntry = null;
 	private String[] items;
 	private Activity activity = this;
 	private DbUtils dbUtils;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		
 		setContentView(R.layout.add_new_entry);	
 		
 		dbUtils = new DbUtils(getContentResolver());								// Set content resolver for DbUtils class
 		Intent intent = getIntent();     
 		EntryId = intent.getLongExtra(Car_servActivity.EDIT_ENTRY_RES, 0);	
 		
 		if(0 != EntryId)
 		{
 			carServEntry = dbUtils.getEntryFromDB(EntryId);
 		}
 		
 		if(!Connectivity.isOnline(this))	// Check for Internet connection
 		{
 			Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
 		}
 		
 		// SPINNER ***********************************************************************************************
 		//String[] items = new String[] {"Inne", "Olej", "Pyn hamulcowy", "Opony", "Filtr powietrza", "Rozrzd"};
 		items 							= getResources().getStringArray(R.array.entry_types);
 		Spinner spinner 				= (Spinner) findViewById(R.id.spinnerTypeChooser);
 		ArrayAdapter<String> adapter 	= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			
 		spinner.setAdapter(adapter);
 	
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
 				selectedType = pos;
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				selectedType = 0;				
 			}
 		});
 		// *******************************************************************************************************
 		
 		// Set values to edit
 		if(null != carServEntry)
 		{
 			// Type -------------------------------------------------------------------------------------------------
 			spinner.setSelection(carServEntry.getType());
 			
 			// Header -----------------------------------------------------------------------------------------------
 			EditText 		editTextOpis 	= (EditText)findViewById(R.id.editWindowDescription);				 
 			editTextOpis.setText(carServEntry.getHeader()+"");
 			 
 			// Mileage ----------------------------------------------------------------------------------------------
 			EditText 		editTextMileage	= (EditText)findViewById(R.id.editMileage);
 			editTextMileage.setText(carServEntry.getMileage()+"");	
 			 
 			 // Expiration date -------------------------------------------------------------------------------------
 			DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker1);
 			
 			Calendar calRestored = Calendar.getInstance();
 			calRestored.setTimeInMillis(carServEntry.getDate());
 			
 			datePicker.init(calRestored.get(Calendar.YEAR), calRestored.get(Calendar.MONTH), calRestored.get(Calendar.DAY_OF_MONTH), null);			 
 		}
 		
 
 		
 		Button buttonOk = (Button)findViewById(R.id.buttonOK);
 		buttonOk.setOnClickListener(new View.OnClickListener() 
 		{
 			
 			public void onClick(View v) 
 			{
 				
 				 // Header -----------------------------------------------------------------------------------------------
 				 EditText 		editTextOpis 	= (EditText)findViewById(R.id.editWindowDescription);				 
 				 String   		header 			= editTextOpis.getText().toString();
 				 
 				 // Mileage ----------------------------------------------------------------------------------------------
 				 EditText 		editTextMileage	= (EditText)findViewById(R.id.editMileage);
 				 int 			mileage			= 0;
 				 
 				 if(editTextMileage.getText().toString().length() > 0)
 				 {
 					 mileage = Integer.parseInt(editTextMileage.getText().toString());
 				 }
 				 
 				 // DatePicker ********************************************************************************************
 				 DatePicker datePicker 	= (DatePicker) findViewById(R.id.datePicker1);				 
 				 Calendar 	cal 		= Calendar.getInstance();
 				 
 				 cal.set(Calendar.YEAR, 		datePicker.getYear());
 				 cal.set(Calendar.MONTH, 		datePicker.getMonth());
 				 cal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
 				 
 				 long dateStamp = cal.getTimeInMillis();
 				 String dateString = String.format("\n%02d.%02d.%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
 				 
 				 Intent	data 		= new Intent();
 				 Date 	currentDate = new Date();
 				 
 				 if(null != carServEntry)													// Update entry
 				 {
 					 
 					 carServEntry.setType(selectedType);
 					 carServEntry.setHeader(header);
 					 carServEntry.setMileage(mileage);
 					 carServEntry.setDate(dateStamp);
 					 carServEntry.setExpired(currentDate.getTime() > dateStamp);
 	
 					 dbUtils.updateEntryDB(EntryId, carServEntry);
 					 
					 if(Car_servActivity.resultsSet != null){								// resultsSet is null when we go to edition from notification bar when event occur
						 Car_servActivity.resultsSet.updateEntry(EntryId, carServEntry);
					 }
 					 
 					 // Set up Alarm
 					 AlarmUtil.setAlarm(getApplicationContext(), EntryId, header, dateStamp);					 
 	   	    		 
 	   	    		 Toast.makeText(getApplicationContext(), getResources().getString(R.string.alarm_set_on) + dateString, Toast.LENGTH_SHORT).show();					 
 					 
 	   	        	 data.putExtra(Car_servActivity.UPDATE_ITEM_RES, EntryId);
 	   	        	 NewEntry.this.setResult(Activity.RESULT_OK, data);						 
 					 
 					 NewEntry.this.finish();
 				 }
 				 else																		// New entry
 				 {	
 //			 		 ContentValues args = new ContentValues();
 //					 args.put(CarServTableMetaData.SERVICE_HEADER, 	header);
 //					 args.put(CarServTableMetaData.SERVICE_DATE, 	dateStamp);
 //					 args.put(CarServTableMetaData.SERVICE_MILEAGE, mileage);
 //					 args.put(CarServTableMetaData.SERVICE_TYPE, 	selectedType);
 //					 
 //					 getContentResolver().insert(CarServProviderMetaData.CarServTableMetaData.CONTENT_URI, args);
 					 carServEntry = new CarServEntry();
 					 carServEntry.setType(selectedType);
 					 carServEntry.setHeader(header);
 					 carServEntry.setMileage(mileage);
 					 carServEntry.setDate(dateStamp);
 					 carServEntry.setExpired(currentDate.getTime() > dateStamp);
 					 dbUtils.insertEntryDB(carServEntry);	
 					 
 					 
 					 
 					 if(Connectivity.isOnline(activity))	// Check for Internet connection
 					 {
 						 try
 						 {
 							 // ******* send via POST **********
 							 StringBuilder str = new StringBuilder();
 							 str.append("HEADER: " 		+ header 				+ "\n");
 							 str.append("MILEAGE: " 	+ mileage 				+ "\n");
 							 str.append("DATESTAMP: "	+ dateStamp 			+ "\n");
 							 str.append("TYPE: " 		+ items[selectedType] 	+ "\n");
 							 str.append("EXPIRED: " 	+ (currentDate.getTime() > dateStamp) 	+ "\n");
 							 dbUtils.sendViaPOST(str.toString());
 							 // ******* send via POST ********** end
 						 }
 						 catch (Exception e)
 						 {
 							 Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error_update_local)+"", Toast.LENGTH_SHORT).show();
 						 }
 						 
 					 }
 					 else
 					 {
 						 Toast.makeText(getApplicationContext(), getResources().getString(R.string.updated_local_db)+"", Toast.LENGTH_SHORT).show();
 					 }	 
 						 
 		   	    	 Cursor cursor = getContentResolver().query(CarServProviderMetaData.CarServTableMetaData.CONTENT_URI, new String[]{CarServTableMetaData._ID}, null, null, CarServTableMetaData._ID + " DESC"); // kolumna "_id", bez kluzuli WHERE, bez WHERE argumentw, sortowanie DESC
 		   	       	
 		   	    	 if(cursor.moveToFirst())			//Metoda zwraca FALSE jesli cursor jest pusty
 		   	    	 { 		 
 		   	    		 EntryId = cursor.getInt(cursor.getColumnIndex(CarServTableMetaData._ID));
 		   	    		 data.putExtra(Car_servActivity.NEW_ENTRY_RES, EntryId);		   	    		 
 
 		   	    		 // Set up Alarm
 		   	    		 AlarmUtil.setAlarm(getApplicationContext(), EntryId, header, dateStamp);
 
 		   	    		 Toast.makeText(getApplicationContext(), getResources().getString(R.string.alarm_set_on) + dateString, Toast.LENGTH_SHORT).show();
 		   	    		 Toast.makeText(getApplicationContext(), getResources().getString(R.string.updated_local_db)+"", Toast.LENGTH_SHORT).show();
 
 		   	    		 NewEntry.this.setResult(Activity.RESULT_OK, data);
 		   	    	 }
 		   	    	 else
 		   	    	 {		      
 		   	    		 Log.e("ERROR GET CURSOR ID", "cursor pusty");       	 
 		   	    	 }   
 		   	         cursor.close();					   	         
 												                     
 			     	NewEntry.this.finish(); 
 				 }
 							
 			}
 		});
 	}
 }
