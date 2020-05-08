 // App Name: Drunk Friend
 // Author: Austin Diviness
 // Last Updated: 2/12/2013
 
 package com.austindiviness.drunkfriend;
 
 import java.util.ArrayList;
import java.util.Collections;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.ContactsContract;
 import android.provider.Settings;
 import android.telephony.SmsManager;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	public ArrayList<ContactData> data;
 	public LocationListener locationListener;
 	public LocationManager locationManager;
 	public String numberToText;
 	public String contactName;
 	public String message1 = "Hi, this is an automated message being sent to you because I'm drunk and would like to be picked up near the following location:";
 	public ProgressDialog loading;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.contact_list);
 		data = getContacts(); // creates arraylist for contact data
 		ArrayList<String> names = new ArrayList<String>();
 		for (ContactData item: data) {
 			names.add(item.getName());
 		}
		Collections.sort(names);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.textview, names);
 		ListView listView = (ListView) findViewById(R.id.listview1);
 		listView.setAdapter(adapter);
 		// check if GPS is enabled
 		if (!gpsEnabled()) {
 			AlertDialog.Builder noGPSEnabled = new AlertDialog.Builder(MainActivity.this);
 			noGPSEnabled.setTitle("GPS Disabled");
 			noGPSEnabled.setMessage("Please enable GPS in settings menu");
 			noGPSEnabled.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {	
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 				}
 			});
 			noGPSEnabled.setPositiveButton("GPS Settings", new DialogInterface.OnClickListener() {		
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// create intent to send user to GPS settings screen
 					Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 					startActivity(gpsIntent);
 					dialog.dismiss();
 				}
 			});
 			noGPSEnabled.show();
 		}
 		// set click listener
 		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				numberToText = "Invalid";
 				TextView textView = (TextView) view;
 				contactName = textView.getText().toString(); // insert alert dialog here to make sure the user wants to text the user they picked
 				AlertDialog.Builder checkCorrectContact = new AlertDialog.Builder(MainActivity.this);
                 checkCorrectContact.setTitle("Confirm Contact");
                 checkCorrectContact.setMessage("Do you want to send pick up message to " + contactName + "?");
                 checkCorrectContact.setNegativeButton("No", new DialogInterface.OnClickListener() {
                 	@Override
                     public void onClick(DialogInterface dialog, int which) {
                 		// no, close the dialog
                         dialog.dismiss();
                     }
                 });
                 checkCorrectContact.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 	@Override
                     public void onClick(DialogInterface dialog, int which) {
                         // yes, send the message
                 		// get number of selected contact
         				for(ContactData contact: data) {
         					if (contact.getName().equalsIgnoreCase(contactName)) {
         						numberToText = contact.getNumber();
         						break;
         					}
         				}
                         loading = new ProgressDialog(getBaseContext()).show(MainActivity.this, "Gathering GPS Data", "This may take a little while...", true);
         				sendMessage(numberToText);
                     }
                 });
                 checkCorrectContact.show();
 			}
 		});
 	}
 
 
 
 	public ArrayList<ContactData> getContacts() {
 		ArrayList<ContactData> data = new ArrayList<ContactData>(); // array to hold contact data to return to main method
 		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // dunno
		ContentResolver cr = getContentResolver(); // dunno
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null); // never actually used, dunno
		String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}; // creates projection to gather data from name and number columnns in database
		Cursor names = getContentResolver().query(uri, projection, null, null, null); // dunno
		int indexName = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME); // gets column index of name
		int indexNumber = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER); // gets column index of number
 		names.moveToFirst();
 		do {
			String name = names.getString(indexName);
			String number = names.getString(indexNumber);
			data.add(new ContactData(name, number));
 		}
 		while (names.moveToNext());
 		return data;
 	}
 	
 	public boolean gpsEnabled() {
 		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 	}
 	
 	public void sendMessage(String contactNumber) {
 		// get location
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationListener = new MyLocationListener();
 		String locationProvider = LocationManager.GPS_PROVIDER;
 		locationManager.requestLocationUpdates(locationProvider, 2000, 50, locationListener);
 		//Toast.makeText(getBaseContext(), "Gathering GPS information, this will take a little while...", Toast.LENGTH_LONG).show();
 		// pause for 15 seconds, before running rest of code inside handler
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                 if (location == null) {
                     //Toast.makeText(getBaseContext(), "GPS either turned off or cannot contact satellites", Toast.LENGTH_LONG).show();
                     AlertDialog.Builder badSignal = new AlertDialog.Builder(MainActivity.this);
                     loading.dismiss();
                     badSignal.setTitle("No Satellite Coverage");
                     badSignal.setMessage("Insufficient satellite information is available. Perhaps try again outside?");
                     badSignal.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                     	@Override
                     	public void onClick(DialogInterface dialog, int which) {
                     		// bad GPS signal, turn off location manager and exit
                     		locationManager.removeUpdates(locationListener);
                     		locationManager = null;
                     		finish();
                     	}
                     });
                     badSignal.show();
                 }
                 else {
 					String lat = String.valueOf(location.getLatitude());
 					String lon = String.valueOf(location.getLongitude());
 					// correct order to display values is apparently lat, lon, even though it seems pretty ambiguous
 					String message2 = "https://maps.google.com/maps?z=12&t=m&q=" + lat + "," + lon; 
 					SmsManager sms = SmsManager.getDefault();
 					sms.sendTextMessage(numberToText, null, message1, null, null);
 					sms.sendTextMessage(numberToText, null, message2, null, null);
 					AlertDialog.Builder messageSent = new AlertDialog.Builder(MainActivity.this);
 					messageSent.setTitle("Success!");
 					messageSent.setMessage("Successfully sent SMS message to " + contactName + " regarding your situation and present location.");
 					messageSent.setNeutralButton("OK", new DialogInterface.OnClickListener() {						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 			                // message sent, turn off location manager and exit
 			                locationManager.removeUpdates(locationListener);
 			                locationManager = null;
 							finish();	
 						}
 					});
 					loading.dismiss();
 					messageSent.show();
                 }
 			}
 		}, 15000);
 		// end of handler code
 	}
 }
 
 
