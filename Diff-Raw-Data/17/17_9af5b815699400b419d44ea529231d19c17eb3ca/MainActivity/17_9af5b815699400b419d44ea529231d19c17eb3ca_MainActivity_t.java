 package com.linkingenius.voodoo;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.linkingenius.voodoo.core.BlackList;
 import com.linkingenius.voodoo.observers.Mailer;
 import com.linkingenius.voodoo.observers.Twitterer;
 import com.linkingenius.voodoo.observers.UserNotifier;
 import com.linkingenius.voodoo.utils.AboutDialogBuilder;
 import com.linkingenius.voodoo.utils.Contact;
 import com.linkingenius.voodoo.utils.Tools;
 import com.linkingenius.voodoo.utils.numberpicker.NumberPickerDialog;
 
 /**
  * Main activity for my blacklist application. Contains the start/stop button 
  * for activating the scanner of incoming calls, and the menu to select the 
  * contacts to filter.
  * 
  * @author Francisco Prez-Sorrosal (fperez)
  *
  */
 public class MainActivity extends Activity implements OnClickListener, OnItemClickListener,  NumberPickerDialog.OnNumberSetListener  {
 
 	private static final String TAG = "MainActivity";
 	
 	public static final String TIMER_PREFS = "timer_preferences";
 	public static final String TIMER_MESSAGE = "timer_message";
	public static final String TIMER_IN_MINUTES = "timer_in_minutes";
 	
 	private static final int PICK_CONTACT = 0;
 
 	private ToggleButton startStopTB;
 	private CheckBox filterAllCB;
 	private ToggleButton emailTB;
 	private ToggleButton twitterTB;
 	private ListView filteredContactsLV;
 	private TextView timer;
 	private ArrayAdapter<Contact> filteredContactsAdapter;
 
 	private IncomingCallScanner incomingCallScanner;
 	
 	private SharedPreferences timerPreferences;
 	
 	private int minutes;
 	
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			incomingCallScanner = ((IncomingCallScanner.LocalBinder) service).getService();
 			TextView timer = (TextView) findViewById(R.id.timerLabel);
 			if(minutes != 0) {
 				Calendar now = Calendar.getInstance();
 		        now.add(Calendar.MINUTE, minutes);
 		        Date date = now.getTime();
 		        TimeZone tz = TimeZone.getDefault();
 		        
 		        DateFormat formatter = DateFormat.getTimeInstance(DateFormat.MEDIUM);
 		        formatter.setTimeZone(tz);
 		        String timeAsString = formatter.format(date);
 		        timer.setText(String.format(getResources().getString(R.string.timer_set_text), timeAsString));
 		        timer.refreshDrawableState();
 				incomingCallScanner.setVoodooTimer(minutes);
 			}
 			Log.d(TAG, "Observers " + incomingCallScanner.nofObservers());
 			startStopTB.setChecked(true);
 			if(incomingCallScanner.isAllCallsFilterEnabled())
 				filterAllCB.setChecked(true);
 			if(incomingCallScanner.containsObserver(Mailer.INSTANCE))
 				emailTB.setChecked(true);
 			if(incomingCallScanner.containsObserver(Twitterer.INSTANCE))
 				twitterTB.setChecked(true);
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			Log.d(TAG, "onServiceDisconnected: Incoming Call Scanner Disconnected");
 			minutes = 0;
 			TextView timer = (TextView) findViewById(R.id.timerLabel);
         		timer.setText(R.string.timer_default_text);
 			clearFilterForAllCallsFromService();
 			clearEmailConnectionFromService();
 			clearTwitterConnectionFromService();
 			startStopTB.setChecked(false);
 			incomingCallScanner = null;
 		}
 	};
 
 	// ------------------------- Lifecycle -------------------------
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.main);
 		
 		AdView adView = (AdView)this.findViewById(R.id.adView);
 	    AdRequest request = new AdRequest();
 	    request.addTestDevice(AdRequest.TEST_EMULATOR);
 	    request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");
 	    adView.loadAd(request);
 	    
 		startStopTB = (ToggleButton) findViewById(R.id.startStopTB);
 		startStopTB.setOnClickListener(this);
 		
 		filterAllCB = (CheckBox) findViewById(R.id.filterAllCB);
 		filterAllCB.setOnClickListener(this);
 
 		emailTB = (ToggleButton) findViewById(R.id.emailTB);
 		emailTB.setOnClickListener(this);
 		
 		twitterTB = (ToggleButton) findViewById(R.id.twitterTB);
 		twitterTB.setOnClickListener(this);
 
 		filteredContactsLV = (ListView) findViewById(R.id.filteredContactsLV);
 		filteredContactsLV.setOnItemClickListener(this);
 		// Load the contact ListView
 		filteredContactsAdapter = new DualLineArrayAdapter(getApplicationContext(),
 				R.layout.contact_list_item, BlackList.INSTANCE.toArrayList(getApplicationContext()));
 		filteredContactsLV.setAdapter(filteredContactsAdapter);
 		filteredContactsAdapter.notifyDataSetChanged();
 		filteredContactsLV.refreshDrawableState();
 		
 		timer = (TextView) findViewById(R.id.timerLabel);
 		timerPreferences = getSharedPreferences(TIMER_PREFS, Activity.MODE_PRIVATE);
 
 		// This binding is required because we have to check if the service was
 		// previously started in a previous invocation. 
 		// For versions > 2.1 change binding for Context.BIND_NOT_FOREGROUND 
 		bindService(new Intent(this, 
 			IncomingCallScanner.class), mConnection, Context.BIND_DEBUG_UNBIND);
 		timer.setText(timerPreferences.getString(TIMER_MESSAGE, getResources().getString(R.string.timer_default_text)));
		minutes = timerPreferences.getInt(TIMER_IN_MINUTES, 0);
 		Log.d(TAG, "onCreate");
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		timerPreferences.edit()
 		.putString(TIMER_MESSAGE, (String) timer.getText())
		.putInt(TIMER_IN_MINUTES, minutes)
 		.commit();
 		// Don't call clearTwitter/MailConnection() on destroying this activity
 		// cause, if enabled, messages must be sent!!!
 		unbindService(mConnection);
 		Log.d(TAG, "onDestroy");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d(TAG, "onPause");
 		BlackList.INSTANCE.stopAutosaving(getApplicationContext());
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume");
 		BlackList.INSTANCE.startAutosaving(getApplicationContext());
 	}
 
 	// ----------------------- END Lifecycle ------------------------
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 			
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.contacts:			
 			Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); 
 			startActivityForResult(intentContact, PICK_CONTACT);
 			break;
 		case R.id.twitter:
 			launchTwitterConfigurationActivity();
 			break;
 		case R.id.timer:
 			NumberPickerDialog dialog = new NumberPickerDialog(this, android.R.style.Theme_Translucent, 60);
             dialog.setTitle(getString(R.string.dialog_picker_title));
             dialog.setOnNumberSetListener(this);
             dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
             dialog.show();
 			break;
 		case R.id.logs:
 			UserNotifier.INSTANCE.showCallScannerNotification(getApplicationContext(),
 					UserNotifier.CallScannerNotification.SHOW_LOG);
 			Intent showLogIntent = new Intent(this, ShowLogActivity.class);
 			startActivity(showLogIntent);
 			break;
 		case R.id.email:
 			launchEmailUserDataActivity();
 			break;
 		case R.id.about:
 			AlertDialog builder;
 			try {
 				builder = AboutDialogBuilder.create(this);
 				builder.show();
 			} catch (NameNotFoundException e) {
 				e.printStackTrace();
 			}
 			break;
 		}
 		return true;
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		switch(requestCode) {
 		
 		case PICK_CONTACT:
 			if(intent != null) { // This is required because the user cannot select any contact
 				Contact contact = Contact
 						.getContactFromAndroid(
 								this, 
 								intent.getData().getLastPathSegment()).build();
 				// Your class variables now have the data, so do something with it
 				String name = contact.getName();
 				if(name !=null & !contact.getPhoneNumbers().isEmpty()) {
 					Contact previousContact = BlackList.INSTANCE.addContact(getApplicationContext(), contact);
 					if(previousContact == null) { // To avoid duplicates
 						filteredContactsAdapter.add(contact);
 						filteredContactsAdapter.notifyDataSetChanged();
 						filteredContactsLV.refreshDrawableState();
 						Log.d(TAG, name + " has been filtered");
 					} else {
 						Toast.makeText(this, R.string.contact_already_filtered, Toast.LENGTH_SHORT).show();
 					}
 				} else {
 					Toast.makeText(this, R.string.missing_contact_data, Toast.LENGTH_SHORT).show();
 				}
 			} else {
 				Toast.makeText(this, R.string.no_contact_selected, Toast.LENGTH_SHORT).show();
 			}
 			break;
 		}
 		
 	}
 
 	@Override
 	public void onClick(View src) {
 		switch (src.getId()) {
 		case R.id.startStopTB:
 			if(startStopTB.isChecked()) {
 				Log.d(TAG, "onClick: starting service");
 				startIncomingCallScannerService();
 			} else {
 				Log.d(TAG, "onClick: stopping service");
 				stopService(new Intent(this, IncomingCallScanner.class));
 			}
 			break;
 		case R.id.filterAllCB:
 			if(filterAllCB.isChecked()) {
 				if(incomingCallScanner != null) {
 					incomingCallScanner.filterAllCalls(true);
 				} else {
 					filterAllCB.setChecked(false);
 				}
 			} else {				
 				if(incomingCallScanner != null)
 					clearFilterForAllCallsFromService();
 			}
 			break;
 		case R.id.twitterTB:
 			if(twitterTB.isChecked()) {
 				if(incomingCallScanner != null) {
 					
 					if(!Tools.isNetworkAvailable(getApplicationContext())) {
 						Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
 						clearTwitterConnectionFromService();
 						return;
 					}					
 					incomingCallScanner.addCallObserver(Twitterer.INSTANCE);
 				} else {
 					twitterTB.setChecked(false);
 				}
 			} else {
 				if(incomingCallScanner != null)
 					clearTwitterConnectionFromService();
 			}
 			break;
 		case R.id.emailTB:
 			if(emailTB.isChecked()) {
 				if(incomingCallScanner != null) {
 					
 					if(!Tools.isNetworkAvailable(getApplicationContext())) {
 						Toast.makeText(this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
 						clearEmailConnectionFromService();
 						return;
 					}
 					incomingCallScanner.addCallObserver(Mailer.INSTANCE);
 				} else {
 					emailTB.setChecked(false);
 				}
 			} else {
 				if(incomingCallScanner != null)
 					clearEmailConnectionFromService();
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 		Log.d(TAG, "onItemClick");
 		final Contact contact = (Contact) filteredContactsLV.getItemAtPosition(position);
 		AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
 		adb.setTitle(getResources().getString(R.string.delete_op));
 		adb.setMessage(String.format(getResources().getString(R.string.remove_user),  contact.getName()));
 		adb.setPositiveButton(getResources().getString(R.string.ok_tag), new AlertDialog.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				BlackList.INSTANCE.removeContact(getApplicationContext(), contact);
 				filteredContactsAdapter.remove(contact);
 				filteredContactsAdapter.notifyDataSetChanged();
 				filteredContactsLV.refreshDrawableState();
 			}});
 		adb.setNegativeButton(getResources().getString(R.string.cancel_tag), null);
 		adb.show();
 	}
 	
 	public void onNumberSet(int minutes) {
         Log.d("NumberPicker", "Number selected: " + minutes);
 		this.minutes = minutes;
         TextView timer = (TextView) findViewById(R.id.timerLabel);
         if(incomingCallScanner == null) {
 	        if(minutes == 0) {
 	        		timer.setText(R.string.timer_default_text);
 	        } else {
 		        timer.setText(String.format(getResources().getString(R.string.timer_set_text), Integer.toString(minutes)));
 	        }
         } else {
         		Toast.makeText(this, getResources().getString(R.string.timer_service_running), Toast.LENGTH_SHORT).show();
         }
     }
 	
 	// ------------------------- Private Methods ----------------------------
 	
 	private void clearFilterForAllCallsFromService() {
 		if(incomingCallScanner != null)
 			incomingCallScanner.filterAllCalls(false);
 		filterAllCB.setChecked(false);
 	}
 	
 	private void startIncomingCallScannerService() {
 		if(incomingCallScanner == null) {
 			// For versions > 2.1 change binding for Context.BIND_NOT_FOREGROUND
     			bindService(new Intent(this, 
     					IncomingCallScanner.class), mConnection, Context.BIND_DEBUG_UNBIND);
 		}
 		startService(new Intent(this, IncomingCallScanner.class));
 	}
 	
 	// ------------------------- Twitter Related Stuff ------------------------
 			
 	private void clearTwitterConnectionFromService() {
 		if(incomingCallScanner != null)
 			incomingCallScanner.removeCallObserver(Twitterer.INSTANCE);
 		twitterTB.setChecked(false);
 	}
 	
 	private  void launchTwitterConfigurationActivity() {
 		Intent i = new Intent(this, TwitterConfigurationActivity.class);
 		startActivity(i);
 	}
 	
 	// ---------------------- Mail related stuff ------------------------------
 	
 	private void launchEmailUserDataActivity() {
 		Intent i = new Intent(this, EmailConfigurationActivity.class);
 		startActivity(i);
 	}
 	
 	private void clearEmailConnectionFromService() {
 		if(incomingCallScanner != null)
 			incomingCallScanner.removeCallObserver(Mailer.INSTANCE);
 		emailTB.setChecked(false);
 	}
 	
 	// -------------------------- Private classes -----------------------------
 	
 	/**
 	 * Allows to present the contents of the Blacklist as well-defined rows
 	 * 
 	 * Francisco Prez-Sorrosal (fperez)
 	 *
 	 */
 	private class DualLineArrayAdapter extends ArrayAdapter<Contact> {
 
 		private ArrayList<Contact> contacts;
 
 	    public DualLineArrayAdapter(Context context, int textViewResourceId, ArrayList<Contact> contacts) {
 	            super(context, textViewResourceId, contacts);
 	            this.contacts = contacts;
 	    }
 
 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	            View v = convertView;
 	            if (v == null) {
 	                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	                v = vi.inflate(R.layout.contact_list_item, null);
 	            }
 	            Contact contact = contacts.get(position);
 	            if (contact != null) {
 	        			ImageView contactPhoto = (ImageView) v.findViewById(R.id.contactPhoto);
 	                TextView contactName = (TextView) v.findViewById(R.id.contactName);
 	                TextView contactPhones = (TextView) v.findViewById(R.id.contactPhones);
 	                
 	                contactPhoto.setImageBitmap(contact.getPhoto());                            
 	                contactName.setText(contact.getName());                            
 	                contactPhones.setText(contact.getPhoneNumbers().toString());
 	            }
 	            return v;
 	    }
 	}
 }
