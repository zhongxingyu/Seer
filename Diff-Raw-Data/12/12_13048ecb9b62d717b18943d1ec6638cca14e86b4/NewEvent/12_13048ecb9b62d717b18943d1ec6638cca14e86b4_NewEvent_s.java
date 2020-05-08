 package com.example.calendar;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Calendar;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class NewEvent extends Activity {
 
 	
 	final Calendar c = Calendar.getInstance();
 	
 	final static int ADD_FILE = 3;
 	final static int ADD_APP = 4;
 	final static int ADD_CONTACT = 1001;// 1001
 	final static int ADD_LINK=5;
 	ArrayList<String> files = new ArrayList<String>();
 	ArrayList<String> apps = new ArrayList<String>();
 	ArrayList<String> contacts = new ArrayList<String>();
 	ArrayList<String> links = new ArrayList<String>();
 	String curLink="";
 	DbHandler db;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_new_event);
 		
 		int cur_year = c.get(Calendar.YEAR);
 		int cur_month = c.get(Calendar.MONTH);
 		int cur_day = c.get(Calendar.DAY_OF_MONTH);
		String cur_date = cur_month + "/" + cur_day + "/" + cur_year;
 		Button date_f = (Button) findViewById(R.id.date_button);
 		date_f.setText(cur_date);
 		
 		db = new DbHandler(this);
 		Intent intent = getIntent();
 		String year = intent.getStringExtra("year");
 		String month = intent.getStringExtra("month");
 		String day = intent.getStringExtra("day");
 		String date = month + "/" + day + "/" + year;
 		if (year != null || month != null || day != null) {
 			Button date_field = (Button) findViewById(R.id.date_button);
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
 				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.file_layout);
 				String f = (String) data.getSerializableExtra("file");
 				files.add(f);
 				int name_loc = f.lastIndexOf("/");
 				String f_name = f.substring(name_loc + 1);
 				RelativeLayout relativeLayout = new RelativeLayout(this);
 
 				TextView name = new TextView(this);
 				name.setText(f_name);
 				RelativeLayout.LayoutParams name_params = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				name_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 				relativeLayout.addView(name, name_params);
 
 				RelativeLayout.LayoutParams btn_params = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 				final Button btn = new Button(this);
 				btn.setText("Delete");
 				btn.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						RelativeLayout r = (RelativeLayout) v.getParent();
 						r.removeAllViews();
 					}
 				});
 				relativeLayout.addView(btn, btn_params);
 				linearLayout.addView(relativeLayout);
 			}
 			break;
 
 		case ADD_CONTACT:
 			if (resultCode == RESULT_OK) {
 				Uri contactUri = data.getData();
 				String[] projection = { Phone.NUMBER, Phone.DISPLAY_NAME };
 				Cursor cursor = getContentResolver().query(contactUri,
 						projection, null, null, null);
 				cursor.moveToFirst();
 
 				int column = cursor.getColumnIndex(Phone.DISPLAY_NAME);
 				String cont_name = cursor.getString(column);
 
 				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.contacts_layout);
 				RelativeLayout relativeLayout = new RelativeLayout(this);
 
 				TextView name = new TextView(this);
 				name.setText(cont_name);
 				RelativeLayout.LayoutParams name_params = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				name_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 				relativeLayout.addView(name, name_params);
 
 				RelativeLayout.LayoutParams btn_params = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 				final Button btn = new Button(this);
 				btn.setText("Delete");
 				btn.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						RelativeLayout r = (RelativeLayout) v.getParent();
 						r.removeAllViews();
 					}
 				});
 				relativeLayout.addView(btn, btn_params);
 				linearLayout.addView(relativeLayout);
 			}
 			break;
 		
 		case ADD_LINK:
 			//handle whatever happens after the activity to make file and filename
 			break;
 		}
 	}
 
 	public void addApp(View view) {
 		// shamelessly stolen from
 		// http://stackoverflow.com/questions/2695746/how-to-get-a-list-of-installed-android-applications-and-pick-one-to-run
 
 		final PackageManager pm = getPackageManager();
 		// get a list of installed apps.
 		List<ApplicationInfo> packages = pm
 				.getInstalledApplications(PackageManager.GET_META_DATA);
 		List<CharSequence> packagenames = new ArrayList<CharSequence>();
 
 		for (ApplicationInfo app : packages) {
 			// if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
 			packagenames.add(app.packageName);
 			/*
 			 * //it's a system app, not interested } else if ((app.flags &
 			 * ApplicationInfo.FLAG_SYSTEM) == 1) { //Discard this one //in this
 			 * case, it should be a user-installed app } else {
 			 * 
 			 * }
 			 */
 
 			// pm.getLaunchIntentForPackage(packageInfo.packageName));
 		}// the getLaunchIntentForPackage returns an intent that you can use
 			// with startActivity()
 			// should print out name of app
 		final CharSequence[] items = packagenames
 				.toArray(new CharSequence[packagenames.size()]);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Options for App choice");
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 
 				String app_name = (String) items[item];
 
 				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.app_layout);
 				apps.add(app_name);
 				RelativeLayout relativeLayout = new RelativeLayout(
 						NewEvent.this);
 
 				TextView name = new TextView(NewEvent.this);
 				int name_loc = app_name.lastIndexOf(".");
 				String a_name = app_name.substring(name_loc + 1);
 				name.setText(a_name);
 				RelativeLayout.LayoutParams name_params = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				name_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 				relativeLayout.addView(name, name_params);
 
 				RelativeLayout.LayoutParams btn_params = new RelativeLayout.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 				btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 				final Button btn = new Button(NewEvent.this);
 				btn.setText("Delete");
 				btn.setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						RelativeLayout r = (RelativeLayout) v.getParent();
 						r.removeAllViews();
 					}
 				});
 				relativeLayout.addView(btn, btn_params);
 				linearLayout.addView(relativeLayout);
 
 			}
 		}).show();
 	}
 
 	public void addFile(View view) {
 		Intent intent = new Intent(this, AndroidExplorer.class);
 		startActivityForResult(intent, ADD_FILE);
 	}
 
 	public void addContact(View view) {
 		Uri t = Uri.parse("content://contacts");
 		Intent pickContactIntent = new Intent(Intent.ACTION_PICK, t);
 		pickContactIntent.setType(Phone.CONTENT_TYPE);
 		startActivityForResult(pickContactIntent, ADD_CONTACT);
 	}
 	
 	public void addLink(View view){
 		curLink="";
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Rename link");
 		alert.setMessage("Change name of link for display");
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			if(input.getText().toString().equals("")){
 				  curLink+= "TBobstaclesAG";
 			  }
 			  else{
 				  curLink+= input.getText().toString();
 				  
 			  }
 			Toast.makeText(getApplicationContext(), 
 					curLink, Toast.LENGTH_LONG).show();
 		  }
 		});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		alert.show();
 		
 		AlertDialog.Builder alert1 = new AlertDialog.Builder(this);
 		alert.setTitle("New Link");
 		alert.setMessage("Insert link:");
 
 		final EditText input1 = new EditText(this);
 		alert.setView(input1);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		  curLink+= input1.getText().toString();
 		  curLink+= "\n";
 		  }
 		});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		alert.show();
 		links.add(curLink);
 		
 		
 		
 	}
 
 	  
 	  
 
 	public void save(View view) {
 		Button date = (Button) findViewById(R.id.date_button);
 		String dateMessage = date.getText().toString();
 		EditText name = (EditText) findViewById(R.id.name_message);
 		String nameMessage = name.getText().toString();
 		EditText location = (EditText) findViewById(R.id.location_message);
 		String locationMessage = location.getText().toString();
 		EditText notes = (EditText) findViewById(R.id.note_message);
 		String notesMessage = notes.getText().toString();
 		CalendarEvent newEvent = new CalendarEvent(dateMessage, nameMessage,
 				locationMessage);
 		db.addEvent(newEvent);
 		Intent i = new Intent();
 		i.putExtra("event", newEvent);
 		i.putStringArrayListExtra("apps", apps);
 		i.putStringArrayListExtra("contacts", contacts);
 		i.putStringArrayListExtra("files", files);
 		i.putExtra("notes", notesMessage);
 		setResult(RESULT_OK, i);
 		finish();
 	}
 
 	public void cancel(View view) {
 		Intent i = new Intent();
 		setResult(RESULT_CANCELED, i);
 		finish();
 	}
 
 	public class DatePickerFragment extends DialogFragment implements
 			DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current date as the default date in the picker
 			final Calendar c = Calendar.getInstance();
 			int year = c.get(Calendar.YEAR);
 			int month = c.get(Calendar.MONTH);
 			int day = c.get(Calendar.DAY_OF_MONTH);
 
 			// Create a new instance of DatePickerDialog and return it
 			return new DatePickerDialog(getActivity(), this, year, month, day);
 		}
 
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			Button date_field = (Button) findViewById(R.id.date_button);
			String date = month + "/" + day + "/" + year;
 			date_field.setText(date);
 		}
 	}
 
 	public void showDatePickerDialog(View v) {
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "datePicker");
 	}
 }
