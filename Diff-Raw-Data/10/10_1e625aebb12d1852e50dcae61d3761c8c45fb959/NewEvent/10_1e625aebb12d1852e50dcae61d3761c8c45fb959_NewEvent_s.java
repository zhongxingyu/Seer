 package com.example.calendar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 //Nagle imports for contacts
 import android.database.Cursor;
 import android.net.Uri;
 import android.widget.TextView;
 import android.content.ContentResolver;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.CommonDataKinds.Email;  
 
 public class NewEvent extends Activity {
 
 	final static int ADD_FILE = 3;
 	final static int ADD_APP = 4;
 	final static int ADD_CONTACT = 1001;//1001
 	ArrayList<EventFile> files = new ArrayList<EventFile>();
 	DbHandler db;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_new_event);
 		db = new DbHandler(this);
 		Intent intent = getIntent();
 		String year = intent.getStringExtra("year");
 		String month = intent.getStringExtra("month");
 		String day = intent.getStringExtra("day");
 		String date = month + "/" + day + "/" + year;
 		if (year != null || month != null || day != null) {
 			EditText date_field = (EditText) findViewById(R.id.date_message);
 			date_field.setText(date);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_new_event, menu);
 		return true;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 			case ADD_FILE:
 				if (resultCode == RESULT_OK) {
 					LinearLayout linearLayout = (LinearLayout)findViewById(R.id.file_layout);
 					EventFile f = (EventFile) data.getSerializableExtra("file");
 					files.add(f);
 					RelativeLayout relativeLayout = new RelativeLayout(this);
 					
 					
 					TextView name = new TextView(this);
 					name.setText(f.getName());
 					RelativeLayout.LayoutParams name_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
 					name_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 					relativeLayout.addView(name, name_params);
 					
 					RelativeLayout.LayoutParams btn_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
 					btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 					final Button btn = new Button(this);
 		        	btn.setText("Delete");
 		            btn.setOnClickListener(new View.OnClickListener() {
 		                public void onClick(View v) {
 		                	RelativeLayout r = (RelativeLayout) v.getParent();
 		                	r.removeAllViews();
 		                }
 		            });
 		            relativeLayout.addView(btn,btn_params);
 					linearLayout.addView(relativeLayout);
 				}
 				break;
 			
 			case ADD_APP:
 				if (resultCode == RESULT_OK) {
 					/*LinearLayout linearLayout = (LinearLayout)findViewById(R.id.file_layout);
 					EventFile f = (EventFile) data.getSerializableExtra("file");
 					files.add(f);
 					TextView name = new TextView(this);
 					name.setText(f.getName());
 					linearLayout.addView(name);*/
 				}
 				break;
 				
 			case ADD_CONTACT:
 				if(resultCode == RESULT_OK){
				//do something with contact result
 					String email="";
 					LinearLayout linearLayout = (LinearLayout)findViewById(R.id.contacts_layout);
 					Uri result= data.getData();
 					String id = result.getLastPathSegment();
 					Cursor cursor = getContentResolver().query(  
 					        Email.CONTENT_URI, null,  
 					        Email.CONTACT_ID + "=?",  
 					        new String[]{id}, null); 
 					if (cursor.moveToFirst()) {  
 					    int emailIdx = cursor.getColumnIndex(Email.DATA);  
 					    email = cursor.getString(emailIdx);  
 					} 
					 
 					TextView name= new TextView(this);
 					name.setText(email);
 					if (email.length() == 0) {  
 					    Toast.makeText(this, "No email found for contact.", Toast.LENGTH_LONG).show();  
 					} 					
 				}
 				break;
 		}
 	}
 
 	public void addFile(View view) {
 		Intent intent = new Intent(this, AndroidExplorer.class);
 		startActivityForResult(intent, ADD_FILE);
 	}
 	
 	public void addApp(View view)
 	{
 		//shamelessly stolen from http://stackoverflow.com/questions/2695746/how-to-get-a-list-of-installed-android-applications-and-pick-one-to-run
 		
 		final PackageManager pm = getPackageManager();
 		//get a list of installed apps.
         List<ApplicationInfo> packages = pm
                 .getInstalledApplications(PackageManager.GET_META_DATA);
     	List<CharSequence> packagenames = new ArrayList<CharSequence>();
 
         for (ApplicationInfo app : packages) {
         	//if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                 packagenames.add(app.packageName);
             /*//it's a system app, not interested
             } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                 //Discard this one
             //in this case, it should be a user-installed app
             } else {
                 
             }*/
         	
             //pm.getLaunchIntentForPackage(packageInfo.packageName)); 
         }// the getLaunchIntentForPackage returns an intent that you can use with startActivity()
         //should print out name of app
         final CharSequence [] items = packagenames.toArray(new CharSequence[packagenames.size()]);
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Options for App choice");
         builder.setItems(items, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
             }
         }).show();
         
         
    	 //Attach a button to call this and it will show any app that has a ACTION_MAIN
    	 //So it shows any app
       	/*Intent intent = new Intent (Intent.ACTION_MAIN);
       	//	title is set as "Choose app"
       	String title = "Choose an App";
       	Intent chooser = Intent.createChooser(intent, title);
       	
       	PackageManager packageManager = getPackageManager();
       	List<ResolveInfo> activities = packageManager.queryIntentActivities(chooser, 0);
       	boolean isIntentSafe = activities.size() > 0;
       	  
       	// Start an activity if it's safe
       	if (isIntentSafe) {
       	    startActivity(chooser);
       	}*/
 	}
 
     public void addContact(View view){
     	 Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
     	            Contacts.CONTENT_URI);  
     	 startActivityForResult(contactPickerIntent, ADD_CONTACT);
     }
     
 	public void save(View view) {
 		EditText date = (EditText) findViewById(R.id.date_message);
 		String dateMessage = date.getText().toString();
 		EditText name = (EditText) findViewById(R.id.name_message);
 		String nameMessage = name.getText().toString();
 		EditText location = (EditText) findViewById(R.id.location_message);
 		String locationMessage = location.getText().toString();
 		EditText notes = (EditText) findViewById(R.id.note_message);
 		String notesMessage = notes.getText().toString();
 		CalendarEvent newEvent = new CalendarEvent(dateMessage, nameMessage,
 				locationMessage,files, notesMessage);
 		db.addEvent(newEvent);
 		Intent i = new Intent();
 		i.putExtra("event", newEvent);
 		setResult(RESULT_OK, i);
 		finish();
 	}
 	
 	public void cancel(View view) {
 		Intent i = new Intent();
 		setResult(RESULT_CANCELED, i);
 		finish();
 	}
 }
