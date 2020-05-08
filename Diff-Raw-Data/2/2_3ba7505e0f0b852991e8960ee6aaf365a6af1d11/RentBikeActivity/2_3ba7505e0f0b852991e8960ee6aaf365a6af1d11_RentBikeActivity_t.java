 package com.twt.xtreme;
 
 import com.google.gson.Gson;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RentBikeActivity extends Activity {
 
 	final static String T = "RentBikeActivity";
 	TextView tStatus;
 	TextView tQuery;
 	String android_id;
 	String slot_id_json;
 	TagData tag;
 	Button bPickupBike;
 	Button bDropoffBike;
 	private static Intent TrackingSvcIntent;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.rentbike);
 		tStatus = (TextView)findViewById(R.id.slot_status);
 		tQuery = (TextView)findViewById(R.id.slot_query);
 		bPickupBike = (Button) findViewById(R.id.btn_pickup_action);
 		bDropoffBike = (Button) findViewById(R.id.btn_dropoff_action);
 		android_id = android.provider.Settings.Secure.getString(getContentResolver(), 
 				android.provider.Settings.Secure.ANDROID_ID);
 		TrackingSvcIntent = new Intent(this, TrackingService.class);
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		refreshView();
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		
 		NdefMessage[] nmsgs = getNdefMessages(getIntent());
 		for (NdefMessage nmsg : nmsgs) {
 			NdefRecord[] nrecs = nmsg.getRecords();
 			for (NdefRecord nrec : nrecs ) {
 				slot_id_json = new String(nrec.getPayload(), 3, (nrec.getPayload().length-3));
 				Log.d(T,"Got tag content: "+ slot_id_json);
 				break;
 			}
 		}
 		Gson g = new Gson();
 		tag = g.fromJson(slot_id_json, TagData.class);
 		tStatus.setText("This is Bikestop:"+
 				"\n  Outlet: "+tag.slot_outlet+"" +
 				"\n  Lot Name: "+ tag.slot_name + 
 				"\n  Lot ID: "+tag.slot_id );
 		
 		if (Util.isRented(getApplicationContext())) {
 			tQuery.setText("Do you wish to return this bike?");
 		} else {
			tQuery.setText("Do you wish to rent this bike?");
 		}
 	}
 	
 	public void doPickupBikeAction(View v) {
 		RentalRecord rec = new RentalRecord();
 		rec.setDeviceId(android_id);
 		rec.setPickup_slot_id(tag.slot_id);
 		int result = HttpUtil.pickupBike(getApplicationContext(), rec);
 		if (result >= HttpResult.STATUS_OK) {
 			Util.setRentalRecordToSharedPref(getApplicationContext(), rec);
 			Util.putSharedPrefStr(getApplicationContext(), "rental_id", ""+result);
 			Toast.makeText(getApplicationContext(), 
 					"Bike picked up successfully at "+tag.slot_outlet+":"+tag.slot_name,
 					Toast.LENGTH_SHORT).show();
 			// tStatus.setText("Bike picked up successfully.");
 			// start location tracking service
 			startService(TrackingSvcIntent);
 			Log.d(T, "Bike picked up successfuly");
 			Intent mainIntent = new Intent(this, LeTourActivity.class);
 			startActivity(mainIntent);
 		} else {
 			tStatus.setText("No bike available at slot:"+rec.getPickup_slot_id());
 			Log.d(T, "No bike available at slot:"+rec.getPickup_slot_id());
 		}
 		refreshView();
 	}
 	
 	public void doDropoffBikeAction(View v) {
 		RentalRecord rec = Util.getRentalRecordFromSharedPref(getApplicationContext());
 		if (rec != null) {
 			rec.setDropoff_slot_id(tag.slot_id);
 			int result = HttpUtil.dropOffBike(getApplicationContext(), rec);
 			if (result >= HttpResult.STATUS_OK) {
 				Util.clearRentalRecordFromSharedPref(getApplicationContext());
 				Util.clearSharedPrefStr(getApplicationContext(), "rental_id");
 				// tStatus.setText("Bike dropped off successfully.");
 				// stop location tracking service
 				Toast.makeText(getApplicationContext(), 
 						"Bike returned successfully at "+tag.slot_outlet+":"+tag.slot_name,
 						Toast.LENGTH_SHORT).show();
 				stopService(TrackingSvcIntent);
 				Log.d(T, "Bike dropped off successfully");
 				Intent mainIntent = new Intent(this, LeTourActivity.class);
 				startActivity(mainIntent);
 			} else {
 				tStatus.setText("Slot " + rec.getDropoff_slot_id()
 						+ " occupied.");
 				Log.d(T, "Slot " + rec.getDropoff_slot_id() + " occupied.");
 			}
 		} else {
 			tStatus.setText("You do not have bike to drop off");
 			Log.d(T, "No bike to drop off");
 		}
 		refreshView();
 	}
 	
 	
 	NdefMessage[] getNdefMessages(Intent intent) {
 	    // Parse the intent
 	    NdefMessage[] msgs = null;
 	    Log.d(T, "NDEF discovered!");
 	    String action = intent.getAction();
 	    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
 	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 	        if (rawMsgs != null) {
 	            msgs = new NdefMessage[rawMsgs.length];
 	            for (int i = 0; i < rawMsgs.length; i++) {
 	                msgs[i] = (NdefMessage) rawMsgs[i];
 	            }
 	        }
 	        else {
 	        // Unknown tag type
 	            byte[] empty = new byte[] {};
 	            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
 	            NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
 	            msgs = new NdefMessage[] {msg};
 	        }
 	    }        
 	    else {
 	        Log.e(T, "Unknown intent " + intent);
 	        finish();
 	    }
 	    return msgs;
 	}
 	
 	private void refreshView() {
 		if (Util.isRented(getApplicationContext())) {
 			bDropoffBike.setVisibility(View.VISIBLE);
 			bPickupBike.setVisibility(View.GONE);
 		} else {
 			bDropoffBike.setVisibility(View.GONE);
 			bPickupBike.setVisibility(View.VISIBLE);
 		}
 	}
 		
 }
