 package edu.ucsb.cs290.touch.to.chat;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.crypto.SecretKey;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import edu.ucsb.cs290.touch.to.chat.crypto.KeyExchange;
 
 public class BeginKeyExchangeActivity extends Activity {
 
     private IntentFilter[] intentFiltersArray;
 	private PendingIntent pendingIntent;
 	private String[][] mTechLists;
 	private NfcAdapter mAdapter;
 	private KeyExchange keyExchange;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_begin_key_exchange);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         pendingIntent = PendingIntent.getActivity(this, 0, 
         		new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
         IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
         try {
             ndef.addDataType("*/*");    
         }
         catch (MalformedMimeTypeException e) {
             throw new RuntimeException("fail", e);
         }
         intentFiltersArray = new IntentFilter[] {ndef, };
         mTechLists = new String[][] { new String[] { Ndef.class.getName() } };
         mAdapter = NfcAdapter.getDefaultAdapter(this);
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, mTechLists);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_begin_key_exchange, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void onPause() {
         super.onPause();
         mAdapter.disableForegroundDispatch(this);
     }
 
     public void onResume() {
         super.onResume();
         mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, mTechLists);
     }
 
     public void onNewIntent(Intent intent) {
         Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
         byte[] b = Ndef.get(tagFromIntent).getCachedNdefMessage().toByteArray();
         SecretKey result;
         try {
         	result = keyExchange.setOtherPublicKey(b, "AES");
         } catch(Exception e)  {
         	Logger.getLogger("touch-to-text").log(Level.SEVERE, "Exception in key exchange!", e);
         	setResult(RESULT_CANCELED, new Intent());
         	return;
         }
         setResult(RESULT_OK, new Intent().putExtra("AES Key", result.getEncoded()));
     }
 }
