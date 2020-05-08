 /**
  * AnonCredCheckActivity.java
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
  * Copyright (C) Maarten Everts, TNO, July 2012,
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, July 2012.
  */
 
 package org.irmacard.androidverifier;
 
 import java.util.Collection;
 import java.util.Locale;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.IsoDepCardService;
 
 import org.irmacard.android.util.credentials.AndroidWalker;
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.idemix.spec.IdemixVerifySpecification;
 import org.irmacard.credentials.idemix.util.CredentialInformation;
 import org.irmacard.credentials.idemix.util.VerifyCredentialInformation;
 import org.irmacard.credentials.info.DescriptionStore;
 import org.irmacard.credentials.info.InfoException;
 import org.irmacard.credentials.info.IssuerDescription;
 import org.irmacard.credentials.info.VerificationDescription;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.IsoDep;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 /**
  * Main Activity for the IRMA android verifier application.
  * 
  * @author Maarten Everts, TNO.
  *
  */
 public class AnonCredCheckActivity extends Activity {
 	
 	private NfcAdapter nfcA;
 	private PendingIntent mPendingIntent;
 	private IntentFilter[] mFilters;
 	private String[][] mTechLists;
 	private final String TAG = "AnonCredCheck";
 	private IdemixVerifySpecification idemixVerifySpec;
 	private byte[] lastTagUID;
 	private boolean useFullScreen = true;
 	private CountDownTimer cdt = null;
 	private static final int STATE_WAITING = 0;
 	private static final int STATE_CHECKING = 1;
 	private static final int STATE_RESULT_OK = 2;
 	private static final int STATE_RESULT_MISSING = 3;
 	private static final int STATE_RESULT_WARNING = 4;
 		
 	private int activityState = STATE_WAITING;
 	private String currentVerifier;
 	private String currentVerificationID;
 	private boolean verificationSetup = false;
 	
 	private static final int WAITTIME = 6000; // Time until the status jumps back to STATE_WAITING
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         String prefLang = sharedPref.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");
 
         Configuration config = new Configuration(getResources().getConfiguration());
         config.locale = new Locale(prefLang);
         getResources().updateConfiguration(config,getResources().getDisplayMetrics());
         
         requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
         setContentView(R.layout.main);
         getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.transparentshape));
         
         findViewById(R.id.mainshape).setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
         
         // Prevent the screen from turning off
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         // NFC stuff
         nfcA = NfcAdapter.getDefaultAdapter(getApplicationContext());
         mPendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
         // Setup an intent filter for all TECH based dispatches
         IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
         mFilters = new IntentFilter[] { tech };
 
         // Setup a tech list for all IsoDep cards
         mTechLists = new String[][] { new String[] { IsoDep.class.getName() } };
 
         setState(STATE_WAITING, "");
 
         
     }
 
     
     void setupScreen() {
     	if (useFullScreen) {
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
             getActionBar().hide();
     	} else {
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
             getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             getActionBar().show();
     	}
     }
     
     public void toggleFullscreen(View v) {
     	useFullScreen = !useFullScreen;
     	if (useFullScreen) {
     		v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
     	}
     	setupScreen();
     }
     
     private void setState(int state, String feedback) {
     	Log.i(TAG,"Set state: " + state);
     	activityState = state;
     	int imageResource = 0;
     	int statusTextResource = 0;
     	((AnimatedArrow)findViewById(R.id.animatedArrow)).stopAnimation();
     	switch (activityState) {
     	case STATE_WAITING:
     		imageResource = R.drawable.irma_icon_place_card_520px;
     		statusTextResource = R.string.status_waiting;
     		break;
 		case STATE_CHECKING:
 			((AnimatedArrow)findViewById(R.id.animatedArrow)).startAnimation();
 			imageResource = R.drawable.irma_icon_card_found_520px;
 			statusTextResource = R.string.status_checking;
 			break;
 		case STATE_RESULT_OK:
 			imageResource = R.drawable.irma_icon_ok_520px;
 			statusTextResource = R.string.status_ok;
 			break;
 		case STATE_RESULT_MISSING:
 			imageResource = R.drawable.irma_icon_missing_520px;
 			statusTextResource = R.string.status_missing;
 			break;
 		case STATE_RESULT_WARNING:
 			imageResource = R.drawable.irma_icon_warning_520px;
 			statusTextResource = R.string.status_warning;
 			break;
 		default:
 			break;
 		}
     	
 
     	if (activityState == STATE_RESULT_OK ||
     			activityState == STATE_RESULT_MISSING || 
     			activityState == STATE_RESULT_WARNING) {
         	if (cdt != null) {
         		cdt.cancel();
         	}
         	cdt = new CountDownTimer(WAITTIME, 100) {
 
         	     public void onTick(long millisUntilFinished) {
 
         	     }
 
         	     public void onFinish() {
         	    	 if (activityState != STATE_CHECKING) {
         	    		 setState(STATE_WAITING, "");
         	    	 }
         	     }
         	  }.start();
     	}
     	((TextView)findViewById(R.id.feedbacktext)).setText(feedback);
 		((TextView)findViewById(R.id.statustext)).setText(statusTextResource);
 		((ImageView)findViewById(R.id.statusimage)).setImageResource(imageResource);
     	
     }
     
     /**
      * Checks whether verifier/issuer and verification description are already properly
      * set in the preferences.
      */
     private void checkPreferences() {
     	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
     	String verifier = sharedPref.getString(SettingsActivity.KEY_PREF_VERIFIER, "");
     	String verificationID = sharedPref.getString(SettingsActivity.KEY_PREF_VERIFICATIONDESCRIPTION, "");
 
     	if (verifier.equals("") || verificationID.equals("")) {
     		DescriptionStore ds = null;
     		try {
     			 ds = DescriptionStore.getInstance();
     		} catch (InfoException e) {
     			e.printStackTrace();
     		}
     		
     		// Check whether the selected verifier is still available, if not, select the first one
     		Collection<IssuerDescription> verifiers = ds.getIssuerDescriptions();
     		String selectedVerifier = null;
     		for (IssuerDescription issuerDescription : verifiers) {
 				if (selectedVerifier == null || verifier.equals(issuerDescription.getID())) {
 					selectedVerifier = issuerDescription.getID();
 				}
 			}
     		if (!verifier.equals(selectedVerifier)) {
     			// If not properly set, change/set the preference
     			Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
     			prefEditor.putString(SettingsActivity.KEY_PREF_VERIFIER, selectedVerifier);
     			prefEditor.commit();
     		}
     		
     		// Check whether the selected verificationdescription is still available, if not, select the first one
     		Collection<VerificationDescription> verificationDescriptions = ds.getVerificationDescriptionsForVerifier(selectedVerifier);
     		String selectedVerificationID = null;
     		for (VerificationDescription verificationDescription : verificationDescriptions) {
 				if (selectedVerificationID == null || verificationID.equals(verificationDescription.getVerificationID())) {
 					selectedVerificationID = verificationDescription.getVerificationID();
 				}
 			}
     		if (!verificationID.equals(selectedVerificationID)) {
     			// If not properly set, change/set the preference
     			Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
     			prefEditor.putString(SettingsActivity.KEY_PREF_VERIFICATIONDESCRIPTION, selectedVerificationID);
     			prefEditor.commit();
     		}
     	}
     }
     
     public void setupVerification() {
 
 
         AndroidWalker aw = new AndroidWalker(getResources().getAssets());
         CredentialInformation.setTreeWalker(aw);
         DescriptionStore.setTreeWalker(aw);
         try {
 			DescriptionStore.getInstance();
 		} catch (InfoException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         
     	checkPreferences();
     	
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         String verifier = sharedPref.getString(SettingsActivity.KEY_PREF_VERIFIER, "Albron");
         String verificationID = sharedPref.getString(SettingsActivity.KEY_PREF_VERIFICATIONDESCRIPTION, "studentCardNone");
 
     	currentVerifier = verifier;
     	currentVerificationID = verificationID;
     	Log.i(TAG,"use CurrentVerifier: " + currentVerifier);
     	Log.i(TAG,"use VerificationID: " + currentVerificationID);
     	
         VerifyCredentialInformation vci = null;
 		try {
 			vci = new VerifyCredentialInformation(verifier, verificationID);
 			VerificationDescription vd = DescriptionStore.getInstance().getVerificationDescriptionByName(verifier, verificationID);
 			TextView credInfo = (TextView)findViewById(R.id.credentialinfo);
 			credInfo.setText(vd.getName());
 			ImageView targetLogo = (ImageView)findViewById(R.id.target);
 			targetLogo.setImageBitmap(aw.getVerifierLogo(vd));
 			idemixVerifySpec = vci.getIdemixVerifySpecification();
 		} catch (InfoException e) {
 			e.printStackTrace();
 		}
     	verificationSetup  = true;
     }
     
     public void printInfo() {
     	try {
 			Collection<IssuerDescription> issuers = DescriptionStore.getInstance().getIssuerDescriptions();
 			for (IssuerDescription issuerDescription : issuers) {
 				System.out.println(" * Issuer: " + issuerDescription.getID() + " (" + issuerDescription.getID() + ")");
 				Collection<VerificationDescription> verificationDescriptions = DescriptionStore.getInstance().getVerificationDescriptionsForVerifier(issuerDescription);
 				for (VerificationDescription verificationDescription : verificationDescriptions) {
 					System.out.println("      -> " + verificationDescription.getVerificationID() + " (" + verificationDescription.getName() + ")");
 				}
 			}
 		} catch (InfoException e) {
 			e.printStackTrace();
 		}
     	
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
         	processIntent(getIntent());
         }        
         if (nfcA != null) {
         	nfcA.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
         }
 
         setupVerification();
         
         // Set the fonts, we have to do this like this because the font is supplied
         // with the application.
         Typeface ubuntuFontR=Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-R.ttf");
         ((TextView)findViewById(R.id.statustext)).setTypeface(ubuntuFontR);
         ((TextView)findViewById(R.id.feedbacktext)).setTypeface(ubuntuFontR);
         
         Typeface ubuntuFontM=Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-B.ttf");
         ((TextView)findViewById(R.id.credentialinfo)).setTypeface(ubuntuFontM);
         
         Typeface ubuntuFontRI=Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-RI.ttf");
         ((TextView)findViewById(R.id.feedbacktext)).setTypeface(ubuntuFontRI);
         
         setupScreen();
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	if (nfcA != null) {
     		nfcA.disableForegroundDispatch(this);
     	}
     }
 
     public void processIntent(Intent intent) {
         Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     	IsoDep tag = IsoDep.get(tagFromIntent);
     	
     	if (tag != null && !intent.getBooleanExtra("handled", false)) {
     		lastTagUID = tagFromIntent.getId();
     		// Make sure verification stuff is setup
     		if (!verificationSetup) {
     			setupVerification();
     		}
     		// Make sure we're not already communicating with a card
     		if (activityState != STATE_CHECKING) {
 	    		setState(STATE_CHECKING, "");
 	    		new CheckCardCredentialTask().execute(tag);
     		}
     	}
     	intent.putExtra("handled", true);
     }
     
     @Override
     public void onNewIntent(Intent intent) {
         setIntent(intent);
     }
     
     private void showResult(int resultValue, String feedback) {
     	switch (resultValue) {
 		case Verification.RESULT_VALID:
 			setState(STATE_RESULT_OK, feedback);
 			break;
 		case Verification.RESULT_INVALID:
 			setState(STATE_RESULT_MISSING, feedback);
 			break;
 		case Verification.RESULT_FAILED:
 			setState(STATE_RESULT_MISSING, feedback);
 			break;
 		default:
 			break;
 		}
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_history:
         	Intent historyIntent = new Intent(this, VerificationListActivity.class);
         	startActivity(historyIntent);
         	return true;
         case R.id.menu_settings:
         	Intent settingsIntent = new Intent(this, SettingsActivity.class);
         	startActivity(settingsIntent);
         	return true;
         default:
         	return super.onOptionsItemSelected(item);
         }
     }
     
     
     private class CheckCardCredentialTask extends AsyncTask<IsoDep, Void, Verification> {
 
 		@Override
 		protected Verification doInBackground(IsoDep... arg0) {
 			IsoDep tag = arg0[0];
 			
 			// Make sure time-out is long enough (10 seconds)
 			tag.setTimeout(10000);
 			
 			CardService cs = new IsoDepCardService(tag);
 
 			IdemixCredentials ic = new IdemixCredentials(cs);
 			Attributes attr = null;
 			try {
 				attr = ic.verify(idemixVerifySpec);
 				
 				cs.close();
 				tag.close();
 				
 				if (attr == null) {
 		            Log.i(TAG,"The proof does not verify");
 		            return new Verification(Verification.RESULT_INVALID, lastTagUID, "Proof did not verify.", "");
 		        } else {
 		        	Log.i(TAG,"The proof verified!");
 		        	return checkAttributes(attr);
 		        }				
 			} catch (Exception e) {
 				Log.e(TAG, "Idemix verification threw an Exception!");
 				e.printStackTrace();
 				return new Verification(Verification.RESULT_FAILED, lastTagUID, "Exception message: " + e.getMessage(), "");
 			}
 		}
 		
 		private Verification checkAttributes(Attributes attr) {
 			// Use-case specific code for handling the attributes
			if (currentVerifier.equals("Bar") && currentVerificationID.equals("over18")) {
 				String age = new String(attr.get("over18"));
 				if (age.equalsIgnoreCase("yes")) {
 	        		return new Verification(Verification.RESULT_VALID, lastTagUID, "", "");
 	        	} else {
 	        		return new Verification(Verification.RESULT_INVALID, lastTagUID, "Not over 18", "");		        		
 	        	}
 			} else if (currentVerifier.equals("Stadspas") && currentVerificationID.equals("addressWoonplaats")) {
 				String city = new String(attr.get("city"));
 				return new Verification(Verification.RESULT_VALID, lastTagUID, city, city);
 			} else {
 				return new Verification(Verification.RESULT_VALID, lastTagUID, "", "");
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(Verification verification) {
 	        // Defines an object to contain the new values to insert
 	        ContentValues mNewValues = new ContentValues();
 	        /*
 	         * Sets the values of each column and inserts the word. The arguments to the "put"
 	         * method are "column name" and "value"
 	         */
 	        mNewValues.put(VerificationData.Verifications.COLUMN_NAME_RESULT,verification.getResult());
 	        mNewValues.put(VerificationData.Verifications.COLUMN_NAME_CARDUID, verification.getCardUIDString());
 	        mNewValues.put(VerificationData.Verifications.COLUMN_NAME_INFO,verification.getInfo());
 	        getContentResolver().insert(
 	        		VerificationData.Verifications.CONTENT_URI,
 	        	    mNewValues
 	        	);
 			AnonCredCheckActivity.this.showResult(verification.getResult(), verification.getFeedback());
 		}
     }
 }
