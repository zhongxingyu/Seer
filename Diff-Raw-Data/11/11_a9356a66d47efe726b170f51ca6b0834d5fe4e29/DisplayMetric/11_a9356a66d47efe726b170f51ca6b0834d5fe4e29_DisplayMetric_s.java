 package edu.colorado.trackers.HealthMetrics;
 
 import java.util.Calendar;
 
 import edu.colorado.trackers.R;
 import edu.colorado.trackers.SendEmail;
 import edu.colorado.trackers.db.Database;
 import edu.colorado.trackers.db.Deleter;
 import edu.colorado.trackers.db.Inserter;
 import edu.colorado.trackers.db.ResultSet;
 import edu.colorado.trackers.db.Selector;
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.PopupWindow;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class DisplayMetric extends FragmentActivity implements View.OnClickListener 
 {
 	public final static String EXTRA_MESSAGE = "com.example.DisplayMetric.MESSAGE";	
 	
 	private Database db;                                    //db instance
 	private String tableName = "healthMetrics15";           //db table for healthMetric
 	
 	String Titlemessage;     								//title displayed at the top of the page
 	String EmailMessage = null;								//email content
 	PopupWindow popupWindow;
 	
     Calendar cal        =  Calendar.getInstance();          //get today's date
    int      setYear    =  cal.get(Calendar.YEAR);
    int      setMonth   =  cal.get(Calendar.MONTH);
    int      setDay     =  cal.get(Calendar.DAY_OF_MONTH);  
     
     Handler msgHandler = new Handler()   //message parsing - sent by DatePickerFragment
     {    
         public void handleMessage(Message m)
         {
             Bundle b    =   m.getData();                       				   //extract the bundle  
             setDay      =   b.getInt("pickedDay");                             //extract the day 
             setMonth    =   b.getInt("pickedMonth") + 1;                       //extract the month
             setYear     =   b.getInt("pickedYear");                            //extract the year
             Button bt   =   (Button) findViewById(R.id.datePicker);      
             String txt  =   setMonth + "-"+ setDay + "-"+ setYear + " ";       //display the selected date
             bt.setText(txt);
             bt.setWidth(10);
             bt.setBackgroundResource(0);
             bt.setEnabled(false);
             bt = (Button) findViewById(R.id.addEntry);
            	bt.setVisibility(View.VISIBLE);            						//enable addEntry button
         }       
     };  
     
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
     	super.onCreate(savedInstanceState);
         Intent intent  =  getIntent();
         Titlemessage   =  intent.getStringExtra(HealthMetrics.EXTRA_MESSAGE); //get the message sent from HealthMetrics 
         setTitle(Titlemessage);   //display it as the title of the page
         setContentView(R.layout.activity_display_metric);   
         
         db = new Database(this, "healthmetric15.db");
         if (!db.tableExists(tableName)) {
  
         	db.createTable(tableName, "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         			"type TEXT NOT NULL, " +
     				"reading INTEGER NOT NULL, " +
     				"date TEXT NOT NULL " );
         }
 
 		getListItems();            //retrieve the records
 		
 		Button bt   =   (Button) findViewById(R.id.datePicker);      
         bt.setEnabled(false);
 		
 		EditText e = (EditText) findViewById(R.id.ReadingInput);    // add watcher for reading text input
 		e.addTextChangedListener(new LocalTextWatcher());
     }
 
     /* get database values */
     public void getListItems() {
     	int counter = 0;
     	TableLayout table = (TableLayout) findViewById(R.id.TableLayout01); // empty the table contents if any,
     	table.removeAllViews();  
     	Selector selector = db.selector(tableName);
     	selector.addColumns(new String[] { "id", "reading", "date"});
     	selector.where("type = ?", new String[] {Titlemessage}); 
     	int count = selector.execute();
     	System.out.println("Selected (" + count + ") items");
     	ResultSet cursor = selector.getResultSet();
     	
     	if(cursor.getCount() > 0)									//display the db records in table
     	{
     		cursor.moveToLast();
     		EmailMessage = "     Reading              Date   \n";
     		while (!cursor.isBeforeFirst()) 
     		{
     		counter++;
     		Integer reading  = cursor.getInt(1); 
     		String date      = cursor.getString(2);
     		
     		System.out.println("Record:  reading=" + reading + " date =:" + date+":");
     		
     		EmailMessage +=   reading + "         " + date + "\n";     //add to the email content
     		
     		TableRow row     = new TableRow(this);
         	TextView t1 = new TextView(this);       // create a new TextView for reading     
         	t1.setText( " " + reading);           	            
         	t1.setGravity(Gravity.CENTER);
         	row.addView(t1);  		
         	
         	TextView t2 = new TextView(this);       // create a new TextView for date  
         	t2.setText( " "+ date);     
         	t2.setGravity(Gravity.CENTER);
         	row.addView(t2);
 
         	Button delBtn = new Button(this);      // enable delete button
         	delBtn.setId(counter);
         	delBtn.setWidth(1);
         	delBtn.setHeight(1);
         	delBtn.setBackgroundResource(R.drawable.del);
         	delBtn.setOnClickListener(this);
         	
         	TableRow.LayoutParams params1 = new TableRow.LayoutParams();
         	params1.span = 3;
         	params1.leftMargin = 62;
         	row.addView(delBtn, 2, params1);
         	
             table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));            
             cursor.moveToPrevious();
     		}
     		cursor.close();
     	}
     }
     
     /*called on click of delete button each reading*/
     public void onClick(View v) {   //TODO DB
     	TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
     	View row = (View) v.getParent();
     	TextView reading = (TextView) ((TableRow) row).getChildAt(0);
     	TextView date = (TextView) ((TableRow) row).getChildAt(1);
     	Deleter deleter = db.deleter(tableName);
     	String dt = date.getText().toString().trim();	
     	deleter.where("type = ?  and reading = ? and date = ?", new String[] {Titlemessage, reading.getText().toString().trim(), dt}); 
     	deleter.execute();
     	System.out.println("Deleted: reading = " + reading.getText().toString() +":date="+dt+":");
     	table.removeView(row); 
     	getListItems();					//update the table 					      
     }
     
     /** Called when the user clicks the DeletAll button */
     public void deleteAll(View view) 
     {
     	TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
         Deleter deleter = db.deleter(tableName);
         deleter.where("type = ?", new String[] {Titlemessage}); 
         deleter.execute();
         table.removeAllViews();
     }
     
     /** Called when the user clicks the done button */
     public void done(View view) {
     	Intent intent = new Intent(this, HealthMetrics.class);   //go back to HealthMetrics main menu
         String message = "more data";
         intent.putExtra(EXTRA_MESSAGE, message);
         startActivity(intent);
     }
     
     /** Called when the user clicks the mail button */
     @SuppressLint("NewApi")
 	public void sendEmail(View view) {
     	String subject = Titlemessage;
 		System.out.println("Got email: " + EmailMessage);
 		if(EmailMessage == null)		//if no data to send
 		{
 			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
 		    View popupView = layoutInflater.inflate(R.layout.activity_popup, null);  
 		    popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		    popupWindow.showAtLocation(this.findViewById(R.id.TableLayout03), Gravity.CENTER, 0, 0);
 		}
 		else
 		{
 			SendEmail s= new SendEmail(subject, EmailMessage); 
 			s.show(getFragmentManager(), "Email healthMetric");
 		}
     }
     
     /** Called when the user clicks the close button on popup window*/
     public void popupClose(View view) {
     	popupWindow.dismiss();				//close the popup
     }
     
     /** Called when the user clicks the add button */
     public void addEntry(View view) 
     {  
          String value1;
          ContentValues values = new ContentValues();        
          values.put("type", Titlemessage);
      
          EditText e = (EditText) findViewById(R.id.ReadingInput);
          value1 = e.getText().toString();
          
  	 	 e.setText(null);
          values.put("reading", value1);
 
          Button bt = (Button) findViewById(R.id.datePicker);
          value1 = bt.getText().toString().trim();   
          bt.setText(null);
  		 bt.setBackgroundResource(R.drawable.cal);
          values.put("date", value1);
          
          bt = (Button) findViewById(R.id.addEntry);
          bt.setVisibility(View.GONE);
  		 
  	  	Inserter inserter = db.inserter(tableName);					//insert the new reading to database
 		inserter.columnNameValues(values);
 		inserter.execute();
 		getListItems(); 
     }
     
     private boolean validateFields() 			//validate the input fields.
     {
     	try 
 		{
     		EditText e = (EditText) findViewById(R.id.ReadingInput);
     		Integer ei = Integer.parseInt(e.getText().toString());
 	
     		if (ei < 1) 					   //if reading less than = 0.
     		{
     			e.setError("invalid entry");  //set error
     			return false;
     		}
     		else 
     		{
     			e.setError(null);	
     			return true; 
     		}
 		}
     	catch (NumberFormatException e) 
 		{
 				System.out.println("Catch ");
 		}
     	return true;
 	}
     
     private class LocalTextWatcher implements TextWatcher {   //watcher for input text
 
 		public void afterTextChanged(Editable s) 
 		{
 			Button bt   =   (Button) findViewById(R.id.datePicker);   //if the reading is valid, enable date button
 			if(validateFields())
 				bt.setEnabled(true);
 			else
 				bt.setEnabled(false);
 		}
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 		public void onTextChanged(CharSequence s, int start, int before, int count) {}
 	}
     
     /** Called when the user clicks the done button */
     public void back(View view)     
     {
     	Intent intent = new Intent(this, HealthMetrics.class);   //back to HealthMetric menu
        	String message = "more data";
        	intent.putExtra(EXTRA_MESSAGE, message);
        	startActivity(intent);
     }
     
     /* Called when the user clicks on calendar button */
     public void  pickDate(View view) 
     {
         Bundle datePickerBundle = new Bundle();
         datePickerBundle.putInt("pickedDay",   setDay);	
         datePickerBundle.putInt("pickedMonth", setMonth);
         datePickerBundle.putInt("pickedYear",  setYear);
         DatePickerFragment datePicker = new DatePickerFragment(msgHandler);	  //call datePicker to display date picker popup
         datePicker.setArguments(datePickerBundle);
         FragmentManager     fmgr      = getSupportFragmentManager();
         FragmentTransaction ftrn      = fmgr.beginTransaction();
         ftrn.add(datePicker, "date_picker");
         ftrn.commit();    
     }
 }
