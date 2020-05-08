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
 
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.CredentialsException;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.idemix.spec.IdemixVerifySpecification;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.IsoDepCardService;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.IsoDep;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.utils.StructureStore;
 
 
 /**
  * Main Activity for the IRMA android verifier application.
  * 
  * @author Maarten Everts, TNO.
  *
  */
 public class AnonCredCheckActivity extends Activity {
 
     // 0x0064 is the id of the student credential
 	private static final short CREDID_STUDENT = (short)0x0064;
 	
 	private NfcAdapter nfcA;
 	private PendingIntent mPendingIntent;
 	private IntentFilter[] mFilters;
 	private String[][] mTechLists;
 	private final String TAG = "AnonCredCheck";
 	private IdemixVerifySpecification idemixVerifySpec;
 	private byte[] lastTagUID;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
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
         
         setupIdemix();
     }
     
     public void setupIdemix() {
		StructureStore.getInstance().get("http://www.irmacard.org/org.irmacard.credentials/phase1/RU/sp.xml",
         		getApplicationContext().getResources().openRawResource(R.raw.sp));
 		
		StructureStore.getInstance().get("http://www.irmacard.org/org.irmacard.credentials/phase1/RU/gp.xml",
         		getApplicationContext().getResources().openRawResource(R.raw.gp));
 
        StructureStore.getInstance().get("http://www.irmacard.org/org.irmacard.credentials/phase1/RU/ipk.xml",
         		getApplicationContext().getResources().openRawResource(R.raw.ipk));
 		
        StructureStore.getInstance().get("http://www.irmacard.org/org.irmacard.credentials/phase1/RU/studentCard/structure.xml",
         		getApplicationContext().getResources().openRawResource(R.raw.structure));
 
         ProofSpec spec = (ProofSpec) StructureStore.getInstance().get("specification",
         		getApplicationContext().getResources().openRawResource(R.raw.specification));
         
         idemixVerifySpec = new IdemixVerifySpecification(spec, CREDID_STUDENT);     
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (nfcA != null) {
         	nfcA.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
         }
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
             onNewIntent(getIntent());
         }
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	if (nfcA != null) {
     		nfcA.disableForegroundDispatch(this);
     	}
     }
     
     public void onNewIntent(Intent intent) {
         Log.i(TAG, "Discovered tag with intent: " + intent);
         Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     	IsoDep tag = IsoDep.get(tagFromIntent);
     	if (tag != null) {
     		lastTagUID = tagFromIntent.getId();
     		Log.i(TAG,"Found IsoDep tag!");
     		showProgressDialog();
     		new CheckCardCredentialTask().execute(tag);
     	}
     }
     
     private void showResultDialog(int resultValue) {
     	DialogFragment df = (DialogFragment)getFragmentManager().findFragmentByTag("checkingdialog");
     	if (df != null) {
     		df.dismiss();
     	}
     	DialogFragment newFragment = CheckResultDialogFragment.newInstance(resultValue);
     	newFragment.show(getFragmentManager(), "resultdialog");    	
     }
     
     private void showProgressDialog() {
     	DialogFragment df = (DialogFragment)getFragmentManager().findFragmentByTag("resultdialog");
     	if (df != null) {
     		df.dismiss();
     	}    	
     	DialogFragment newFragment = ProgressDialogFragment.newInstance(R.string.checkcredentialstitle);
     	newFragment.show(getFragmentManager(), "checkingdialog");    	
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
         	Intent intent = new Intent(this, VerificationListActivity.class);
         	startActivity(intent);
         	return true;
         default:
         	return super.onOptionsItemSelected(item);
         }
     }
     
     public static class ProgressDialogFragment extends DialogFragment {
 
         public static ProgressDialogFragment newInstance(int title) {
             ProgressDialogFragment frag = new ProgressDialogFragment();
             Bundle args = new Bundle();
             args.putInt("title", title);
             frag.setArguments(args);
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return new AlertDialog.Builder(getActivity())
                     .setTitle(getArguments().getInt("title"))
                     .setView(new ProgressBar(getActivity().getApplicationContext(), null,
         					android.R.attr.progressBarStyleLarge))
                     .setNegativeButton("Cancel",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int whichButton) {
                             	dialog.dismiss();
                             }
                         }
                     )
                     .create();
         }
     }
 
     public static class CheckResultDialogFragment extends DialogFragment {
 
         public static CheckResultDialogFragment newInstance(int value) {
             CheckResultDialogFragment frag = new CheckResultDialogFragment();
             Bundle args = new Bundle();
             args.putInt("value", value);
             frag.setArguments(args);
             return frag;
         }
 
        
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
         	ImageView iv = new ImageView(getActivity().getApplicationContext());
         	int value = getArguments().getInt("value");
         	int image_resource = R.drawable.orange_questionmark0350;
         	int title_resource = R.string.verificationfailed_title;
 
         	switch (value) {
 			case Verification.RESULT_VALID:
 				image_resource = R.drawable.green_check0350;
 				title_resource = R.string.foundcredential_title;
 				break;
 			case Verification.RESULT_INVALID:
 				image_resource = R.drawable.red_cross0350;
 				title_resource = R.string.nocredential_title;
 				break;
 			}
         	iv.setImageResource(image_resource);
             return new AlertDialog.Builder(getActivity())
                     .setTitle(title_resource)
                     .setView(iv)
                     .setPositiveButton("OK",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int whichButton) {
                             	dialog.dismiss();
                             }
                         }
                     )
                     .create();
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
 				if (attr == null) {
 		            Log.i(TAG,"The proof does not verify");
 		            return new Verification(Verification.RESULT_INVALID, lastTagUID, "Proof did not verify.");
 		        } else {
 		        	Log.i(TAG,"The proof verified!");
 		        	return new Verification(Verification.RESULT_VALID, lastTagUID, "");
 		        }				
 			} catch (CredentialsException e) {
 				Log.e(TAG, "Idemix verification threw an Exception!");
 				e.printStackTrace();
 				return new Verification(Verification.RESULT_FAILED, lastTagUID, "Exception message: " + e.getMessage());
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
 			AnonCredCheckActivity.this.showResultDialog(verification.getResult());
 		}
     }
 }
