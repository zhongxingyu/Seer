 package es.finnapps.piggybank.activities;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcAdapter.CreateNdefMessageCallback;
 import android.nfc.NfcEvent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.inject.Inject;
 
 import es.finnapps.piggybank.R;
 import es.finnapps.piggybank.bankapi.BankApiInterface;
 import es.finnapps.piggybank.model.Piggy;
 import es.finnapps.piggybank.piggyapi.PiggyApiInterface;
 import es.finnapps.piggybank.sharedprefs.PiggyBankPreferences;
 
 public class BrowsePigsActivity extends RoboActivity implements CreateNdefMessageCallback, OnItemSelectedListener,
         OnClickListener {
 
     @InjectView(R.id.gallery)
     private Gallery mGallery;
     @InjectView(R.id.piggy_name_textview)
     private TextView mPiggyNameTextView;
     @InjectView(R.id.coin1)
     private ImageButton mCoin1;
     @InjectView(R.id.coin2)
     private ImageButton mCoin2;
     @InjectView(R.id.coin20)
     private ImageButton mCoin20;
     @InjectView(R.id.coin50)
     private ImageButton mCoin50;
     @InjectView(R.id.coin100)
     private ImageButton mCoin100;
     @InjectView(R.id.coin200)
     private ImageButton mCoin200;
     @InjectView(R.id.progressbar)
     private ProgressBar mProgressBar;
     
     @InjectView(R.id.amount_edit)
     private EditText amountDeposit;
     
     @InjectView(R.id.donate_button)
     private Button deposit;
     
     private NfcAdapter mNfcAdapter;
     private Piggy currentPiggy = null;
 
     @Inject
     private PiggyApiInterface mPiggyApi;
     @Inject
     private BankApiInterface mBankApi;
     @Inject
     private PiggyBankPreferences mPreferences;
     private ImageView mEmptyPiggyView;
     private List<Piggy> mPiggies;
     private List<View> mPiggyViews;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.browse_pigs_activity);
 
         mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter == null) {
             Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
             finish();
             return;
         }
 
         mGallery.setOnItemSelectedListener(this);
 
        mCoin1.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(1);
             }
         });
 
         mCoin2.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(2);
             }
         });
         mCoin20.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(20);
             }
         });
         mCoin50.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(50);
             }
         });
         mCoin100.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(100);
             }
         });
         mCoin200.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(200);
             }
         });
 
         deposit.setOnClickListener(new OnClickListener() {
             
             public void onClick(View v) {
                 deposit(Long.parseLong(amountDeposit.getText().toString()));
             }
         });
 
 
         // Empty piggy, for creating new piggy
         mEmptyPiggyView = new ImageView(BrowsePigsActivity.this);
         mEmptyPiggyView.setImageResource(R.drawable.nuevo_piggy);
         //mEmptyPiggyView.setOnClickListener(BrowsePigsActivity.this);
         mGallery.setOnItemClickListener(new OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> arg0, final View v, int arg2, long arg3) {
                 if (v == mEmptyPiggyView) {
                     
                     Animation scaleAnim = AnimationUtils.loadAnimation(BrowsePigsActivity.this, R.anim.coin_click);
                     scaleAnim.setAnimationListener(new AnimationListener() {
 
                         public void onAnimationStart(Animation animation) {
                             // TODO Auto-generated method stub
 
                         }
 
                         public void onAnimationRepeat(Animation animation) {
                             // TODO Auto-generated method stub
 
                         }
 
                         public void onAnimationEnd(Animation animation) {
                             startActivity(new Intent(BrowsePigsActivity.this, CreatePiggyActivity.class));
                         }
                     });
                     v.startAnimation(scaleAnim);
                     
                 } 
                 else {
                     ((ImageView)v).setImageResource(R.drawable.nuevo_cerdo_nfc_hoover);
                 }
                 
                Toast.makeText(BrowsePigsActivity.this, "hello", Toast.LENGTH_SHORT).show(); 
             }
             
         });
 
         initActivity();
     }
 
     @Override
     protected void onRestart() {
         super.onRestart();
         initActivity();
     }
 
     private void initActivity() {
         new AsyncTask<Void, Void, Void>() {
             protected void onPreExecute() {
                 mProgressBar.setVisibility(View.VISIBLE);
             };
 
             @Override
             protected Void doInBackground(Void... params) {
 
                 mPiggies = new ArrayList<Piggy>();
                 mPiggies.addAll(mPiggyApi.getSharedPiggys(mPreferences.getUserPhone()));
                 mPiggyViews = new ArrayList<View>();
 
                 for (int i = 0; i < mPiggies.size(); i++) {
                     final ImageView view = new ImageView(BrowsePigsActivity.this);
                     view.setImageResource(R.drawable.nuevo_cerdo_nfc);
                     mPiggyViews.add(view);
                 }
                 // Add empty piggy
                 mPiggyViews.add(mEmptyPiggyView);
 
                 return null;
             }
 
             protected void onPostExecute(Void result) {
                 mGallery.setAdapter(new PiggySpinner());
                 mProgressBar.setVisibility(View.INVISIBLE);
             };
 
         }.execute();
 
         mNfcAdapter.setNdefPushMessageCallback(this, this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_browse_pigs_activicity, menu);
         return true;
     }
 
     class PiggySpinner extends BaseAdapter {
         public int getCount() {
             return mPiggyViews.size();
         }
 
         public Object getItem(int position) {
             return mPiggyViews.get(position);
         }
 
         public long getItemId(int position) {
             return mPiggyViews.get(position).getId();
         }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             return mPiggyViews.get(position);
         }
 
     }
 
     public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
         if (view == mEmptyPiggyView) {
             mPiggyNameTextView.setText(getString(R.string.new_piggy));
             return;
         }
         if (position >= mPiggies.size()) {
             Log.e("Browse", "position >= size");
             return;
         }
         currentPiggy = mPiggies.get(position);
         mPiggyNameTextView.setText(mPiggies.get(position).getName());
     }
 
     public void onNothingSelected(AdapterView<?> arg0) {
         // TODO Auto-generated method stub
 
     }
 
     public void onClick(View v) {
         Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.coin_click);
         v.startAnimation(scaleAnim);
 
       
        if (v == mCoin200)
        {
            shareAndRequest("account1234", -1);
        }
 
     }
 
     private void deposit(float amount) {
         final float _amount = amount;
         new AsyncTask<Void, Void, Void>() {
 
             protected void onPreExecute() {
                 mProgressBar.setVisibility(View.VISIBLE);
             };
 
             @Override
             protected Void doInBackground(Void... params) {
                 Piggy piggy = mPiggies.get(mGallery.getSelectedItemPosition());
                 mBankApi.transferFunds(mPreferences.getBaseAccount(), piggy.getNumber(), mPreferences.getToken(),
                         piggy.getName(), mPreferences.getUserPhone(), _amount);
                 return null;
             }
 
             protected void onPostExecute(Void result) {
                 mProgressBar.setVisibility(View.INVISIBLE);
             };
 
         }.execute();
     }
 
     public NdefMessage createNdefMessage(NfcEvent event) {
        StringBuilder sb = new StringBuilder(currentPiggy.getNumber());
        sb.append(":");
        sb.append(currentPiggy.getAmountToShare());
         NdefMessage msg = new NdefMessage(new NdefRecord[] { createMimeRecord(
                 "application/com.doorthing.door",
                (currentPiggy.getNumber()+":"+currentPiggy.getAmountToShare()).getBytes())
//               sb.toString().getBytes())
         });
         return msg;
     }
 
     /**
      * Creates a custom MIME type encapsulated in an NDEF record
      */
     public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
         byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
         NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
         return mimeRecord;
     }
 
     public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
         byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
         Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
         byte[] textBytes = payload.getBytes(utfEncoding);
         int utfBit = encodeInUtf8 ? 0 : (1 << 7);
         char status = (char) (utfBit + langBytes.length);
         byte[] data = new byte[1 + langBytes.length + textBytes.length];
         data[0] = (byte) status;
         System.arraycopy(langBytes, 0, data, 1, langBytes.length);
         System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
         NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
         return record;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         // Check to see that the Activity started due to an Android Beam
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
             if (processIntent(getIntent())) {
                 // create nuevo cerdo
             } else {
                 // fail
             }
         }
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         // onResume gets called after this to handle the intent
         setIntent(intent);
     }
 
     /**
      * Parses the NDEF Message from the intent and prints to the TextView
      */
     public boolean processIntent(Intent intent) {
 
         Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
         // only one message sent during the beam
         NdefMessage msg = (NdefMessage) rawMsgs[0];
         String s =  new String(msg.getRecords()[0].getPayload());
         int limiterIndex = s.indexOf(':');
         if (limiterIndex == -1)
         {
             Log.e("Browse", "No colon in NFC message.");
             return false;
         }
         if (limiterIndex == 0)
         {
             Log.e("Browse", "No phonenumber in NFC message.");
             return false;
         }
         if (limiterIndex == s.length()-1)
         {
             Log.e("Browse", "No amount in NFC message.");
             return false;
         }
         
         String account = s.substring(0, limiterIndex);
         float amount= Float.parseFloat(s.substring(limiterIndex+1, s.length()));
         
         shareAndRequest(account,amount);
        
         
 
         // record 0 contains the MIME type, record 1 is the AAR, if present
         return true;
     }
 
     private void shareAndRequest(String account, float amount) {
         final float _amount = amount;
         final String _account = account;
        new AsyncTask<Void, Void, Void>(){
             
            
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
            }
 
         @Override
         protected Void doInBackground(Void... params) {
             // share piggy
             List<String> shared = new ArrayList<String>();
             shared.add(mPreferences.getUserPhone());
            Piggy piggy = new Piggy(null, _account, 0, null, null, 0, shared, _amount);
            mPiggyApi.sharePiggyWith(piggy);
             return null;
         };
         protected void onPostExecute(Void result) {
                mProgressBar.setVisibility(View.INVISIBLE);
             
                if (_amount <= 0)
                {
                    // No amount
                    Toast.makeText(BrowsePigsActivity.this, BrowsePigsActivity.this.getString(R.string.pig_shared_with_you),
                            Toast.LENGTH_LONG).show();
                }
                else {
                    String formattedString = String.format(BrowsePigsActivity.this.getString(R.string.accept_transfer), _amount); 
                    MyDialogAlert newFragment = MyDialogAlert.newInstance(R.string.attention_nfc,
                           formattedString ,new Runnable() {
                             
                             public void run() {
                                 // Positive action: transaction
                             }
                         },null);
                    newFragment.show(getFragmentManager(), "dialog"); 
                }
                            
         };
        }.execute();
     }
 
 } 
