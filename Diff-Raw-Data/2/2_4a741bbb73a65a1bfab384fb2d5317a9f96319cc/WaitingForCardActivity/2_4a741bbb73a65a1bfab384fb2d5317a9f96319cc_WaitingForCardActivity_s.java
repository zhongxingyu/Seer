 /**
  * WaitingForCardActivity.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, Februari 2013.
  */
 
 package org.irmacard.androidmanagement;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.IsoDepCardService;
 
 import org.irmacard.android.util.credentials.AndroidWalker;
 import org.irmacard.android.util.credentials.CredentialPackage;
 import org.irmacard.android.util.pindialog.EnterPINDialogFragment;
 import org.irmacard.androidmanagement.dialogs.AlertDialogFragment;
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.idemix.util.CredentialInformation;
 import org.irmacard.credentials.info.CredentialDescription;
 import org.irmacard.credentials.info.DescriptionStore;
 import org.irmacard.credentials.info.InfoException;
 import org.irmacard.credentials.util.CardVersion;
 import org.irmacard.credentials.util.log.LogEntry;
 import org.irmacard.idemix.IdemixService;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.IsoDep;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class WaitingForCardActivity extends Activity implements EnterPINDialogFragment.PINDialogListener, AlertDialogFragment.AlertDialogListener {
 	private NfcAdapter nfcA;
 	private PendingIntent mPendingIntent;
 	private IntentFilter[] mFilters;
 	private String[][] mTechLists;
 	private Tag tag;
 	private IsoDep isotag;
 	private int tries = -1;
 	
 	private final String TAG = "WaitingForCard";
 	
 	private static final int STATE_IDLE = 0;
 	private static final int STATE_WAITING_PIN = 1;
 	private static final int STATE_CHECKING = 2;
 	private static final int STATE_DISPLAYING = 3;
 	private static final int STATE_ERROR = 4;
 	private int activityState = STATE_IDLE;
 	
     public static final byte[] DEFAULT_PIN = {0x30, 0x30, 0x30, 0x30};
     public static final byte[] DEFAULT_MASTER_PIN = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
 
     public static final String EXTRA_CREDENTIAL_PACKAGES = "org.irmacard.androidmanagement.credential_packages";
     public static final String EXTRA_LOG_ENTRIES = "org.irmacard.androidmanagement.log_entries";
     public static final String EXTRA_TAG = "org.irmacard.androidmanagement.tag";
     public static final String EXTRA_CARD_PIN = "org.irmacard.androidmanagement.cardpin";
     public static final String EXTRA_CARD_VERSION = "org.irmacard.androidmanagement.cardversion";
 
 	private class CardData {
 		public ArrayList<CredentialPackage> credentials;
 		public ArrayList<LogEntry> logs;
 		public CardVersion cardVersion;
 
 		public Exception e;
 		public int tries = -1;
 
 		public CardData(ArrayList<CredentialPackage> credentials, ArrayList<LogEntry> logs, CardVersion cardVersion) {
 			this.credentials = credentials;
 			this.logs = logs;
 			this.cardVersion = cardVersion;
 
 			this.e = null;
 		}
 
 		public CardData(Exception e){
 			this.e = e;
 		}
 
 		public CardData(int tries) {
 			this.tries = tries;
 		}
 
 		public boolean isSuccesful() {
 			return e == null && tries == -1;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_waiting_for_card);
 		
 		// Make sure all configuration files can be found
 	    AndroidWalker aw = new AndroidWalker(getResources().getAssets());
 	    DescriptionStore.setTreeWalker(aw);
 	    CredentialInformation.setTreeWalker(aw);
 	    
 	    try {
 			DescriptionStore.getInstance();
 		} catch (InfoException e) {
 			Log.e("error", "something went wrong");
 			e.printStackTrace();
 			AlertDialogFragment f = AlertDialogFragment.getInstance("Cannot load Description Store", e.getMessage());
 			f.show(getFragmentManager(), "alert");
 			setState(STATE_ERROR);
 		}
 		
         // NFC stuff
         nfcA = NfcAdapter.getDefaultAdapter(getApplicationContext());
         mPendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
         // Setup an intent filter for all TECH based dispatches
         IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
         mFilters = new IntentFilter[] { tech };
 
         // Setup a tech list for all IsoDep cards
         mTechLists = new String[][] { new String[] { IsoDep.class.getName() } };
         
         // Set initial state
         setState(STATE_IDLE);
         
         if(getIntent() != null) {
         	onNewIntent(getIntent());
         }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.activity_waiting_for_card, menu);
 		return true;
 	}
 	
     @Override
     public void onResume() {
         super.onResume();
         if (nfcA != null) {
         	nfcA.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
         }
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	if (nfcA != null) {
     		nfcA.disableForegroundDispatch(this);
     	}
     }
     
     private void askForPin() {
 		setState(STATE_WAITING_PIN);
 		DialogFragment pinDialog = EnterPINDialogFragment.getInstance(tries);
 		pinDialog.show(getFragmentManager(), "pinentry");
     }
     
     public void setState(int state) {
     	int imageResource = 0;
     	int statusTextResource = 0;
     	
     	Log.i(TAG, "Changing status to " + state);
     	
     	switch(state) {
     	case STATE_CHECKING:
     		Log.i("TAG", "Changing status to Checking");
     		imageResource = R.drawable.irma_icon_card_found_520px;
     		statusTextResource = R.string.status_loading_credentials;
     		break;
     	case STATE_IDLE:
     		Log.i("TAG", "Changing status to IDLE");
     		imageResource = R.drawable.irma_icon_place_card_520px;
     		statusTextResource = R.string.status_waiting_for_card;
     		break;
     	case STATE_WAITING_PIN:
     		Log.i(TAG, "Changing status to WAITING_PIN");
     		imageResource = R.drawable.irma_icon_card_found_520px;
     		statusTextResource = R.string.status_waiting_for_pin;
     		break;
     	case STATE_ERROR:
     		Log.i(TAG, "Changing status to ERROR");
     		imageResource = R.drawable.irma_icon_warning_520px;
     		statusTextResource = R.string.status_error;
     		break;
     	}
     	
     	((TextView) findViewById(R.id.statustext)).setText(statusTextResource);
     	((ImageView) findViewById(R.id.statusimage)).setImageResource(imageResource);
     	
     	activityState = state;
     }
     
     @Override
     public void onNewIntent(Intent intent) {
         Log.i(TAG, "Discovered tag with intent: " + intent);
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
 	        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 	    	isotag = IsoDep.get(tag);
 	    	if (isotag != null) {
 	    		Log.i(TAG,"Found IsoDep tag!");
 	    		
 	    		// Make sure we're not already communicating with a card
 	    		if (activityState == STATE_IDLE) {
 	    			askForPin();
 	    		}
 	    	}
         }
     }
     
     private class LoadCredentialsFromCardTask extends AsyncTask<IsoDep, Void, CardData> {
     	private final String TAG = "LoadingTask";
     	private String pin;
     	private Context context;
 
     	protected LoadCredentialsFromCardTask(Context context, String pin) {
     		this.context = context;
     		this.pin = pin;
     	}
 
 		@Override
 		protected CardData doInBackground(IsoDep... arg0) {
 			ArrayList<CredentialPackage> credentialpks = new ArrayList<CredentialPackage>();
 			ArrayList<LogEntry> logs = new ArrayList<LogEntry>();
 			boolean credentialsRead = false;
 			CardVersion cardVersion = null;
 
 			IsoDep tag = arg0[0];
 			
 			// Make sure time-out is long enough (10 seconds)
 			tag.setTimeout(10000);
 
 			IdemixService is = new IdemixService(new IsoDepCardService(tag));
 			IdemixCredentials ic = new IdemixCredentials(is);
 			
 			try {
 				ic.connect();
 				cardVersion = ic.getCardVersion();
 				tries = is.sendCardPin(pin.getBytes());
 
 				if(tries != -1) {
 					// Pin-code incorrect, returning
 					return new CardData(tries);
 				}
 
 				Log.i(TAG,"Retrieving credentials now"); 
 				List<CredentialDescription> credentials = ic.getCredentials();
 				for(CredentialDescription cd : credentials) {
 					Log.i(TAG, "Found credential: " + cd);
 					Attributes attr = ic.getAttributes(cd);
 					Log.i(TAG, "With attributes: " + attr);
 					credentialpks.add(new CredentialPackage(cd, attr));
 				}
 				credentialsRead = true;
 
 				Log.i(TAG,"Retrieving logs now");
 				for(LogEntry l : ic.getLog()) {
 					logs.add(l);
 				}
 
 				is.close();
 				
 				Log.i(TAG, "All attributes read!");
 			} catch (CardServiceException e) {
 				Log.e(TAG, "CardServiceException caught");
 				e.printStackTrace();
 				if(credentialsRead) {
 					Log.e(TAG, "Continuing as credentials were read");
 					logs = null;
 				} else {
 					return new CardData(e);
 				}
 			} catch (Exception e) {
 				Log.e(TAG, "Reading verification caused exception");
 				e.printStackTrace();
 				return new CardData(e);
 			} finally {
 				try {
 					tag.close();
 				} catch (IOException e) {
 					Log.e(TAG, "Failed to close tag connection");
 					e.printStackTrace();
 				}
 			}
 
 			return new CardData(credentialpks, logs, cardVersion);
 		}
 		
 		@Override
 		protected void onPostExecute(CardData data) {
 			setState(STATE_IDLE);
 			
 			if(data.isSuccesful()) {
 				// Move to CredentialListActivity
 				Log.i(TAG, "Post Excecute, got all data succesfully");
 				Intent intent = new Intent(context, CredentialListActivity.class);
 				intent.putExtra(EXTRA_CREDENTIAL_PACKAGES, data.credentials);
 				intent.putExtra(EXTRA_LOG_ENTRIES, data.logs);
 				intent.putExtra(EXTRA_TAG, tag);
 				intent.putExtra(EXTRA_CARD_PIN, pin);
 				intent.putExtra(EXTRA_CARD_VERSION, data.cardVersion);
 				tries = -1;
 				startActivity(intent);
 			} else {
 				if(tries != -1) {
 					Log.i(TAG, "Pin incorect, " + tries + " left.");
 					if(tries > 0) {
 						askForPin();
 					} else {
 						AlertDialogFragment f = AlertDialogFragment.getInstance("PIN Error", "You have no more pin tries left!");
 						f.show(getFragmentManager(), "alert");
 						tries = -1;
 						setState(STATE_ERROR);
 					}
 				} else {
 					// Notify user of error
 					Log.i(TAG, "General error occurred");
 					AlertDialogFragment f = AlertDialogFragment.getInstance("Card Error", data.e.getMessage());
 					f.show(getFragmentManager(), "alert");
 					setState(STATE_ERROR);
 				}
 			}
 		}
     }
 
 	@Override
 	public void onPINEntry(String pincode) {
		Log.i(TAG, "Pin entered " + pincode);
 		setState(STATE_CHECKING);
 		new LoadCredentialsFromCardTask(this, pincode).execute(isotag);
 	}
 
 	@Override
 	public void onPINCancel() {
 		Log.i(TAG, "Pin entry canceled!");
 		setState(STATE_IDLE);
 	}
 
 	@Override
 	public void onAlertDismiss() {
 		setState(STATE_IDLE);
 	}
 
 }
