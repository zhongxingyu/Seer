 package com.receiptspp.shopfrontmockup.android;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
import android.widget.TextView;
 import android.widget.Toast;
 
 import com.receiptspp.shopfrontmockup.R;
 import com.receiptspp.shopfrontmockup.business.Cart;
 import com.receiptspp.shopfrontmockup.business.MockReceipt;
 import com.receiptspp.shopfrontmockup.business.Product;
import com.receiptspp.shopfrontmockup.common.Util;
 
 public class CheckoutActivity extends Activity {
 
 	@SuppressWarnings("unused")
 	private NfcAdapter mNfcAdapter;
 	private Cart cart;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_checkout);
		
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
 		cart = Cart.getInstance();
		
		TextView subTotalView = (TextView) findViewById(R.id.checkoutProductSubtotal);
		subTotalView.setText(Util.generatePriceString(cart.getTotalTransaction()));
 
 		ListView checkoutProductContainer = (ListView) findViewById(R.id.checkoutProductContainer);
 
 		ArrayAdapter<Product> adapter = new CartViewArrayAdapter(this,
 				cart.getProductsInCart());
 
 		checkoutProductContainer.setAdapter(adapter);
 		
 		Button checkoutConfirmButton = (Button) findViewById(R.id.checkoutConfirmButton);
 		
 		checkoutConfirmButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				String userId = cart.getUserId();
 				String text = "";
 				if (userId == null){
 					text = "No user ID Received";
 				} else {
 					text = "User ID is " + userId;
 				}
 				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
 				MockReceipt receipt = new MockReceipt(true);
 				receipt.setName("Trimtex Mock Store");
 				Log.v("Receipt",receipt.toJSON().toString());
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.checkout, menu);
 		return true;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// Check to see that the Activity started due to an Android Beam
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			processIntent(getIntent());
 		}
 	}
 
 	/**
 	 * Get any messages from the intent and upload them to the server using the
 	 * SubmitVitalStats task
 	 * 
 	 * @param intent
 	 */
 	void processIntent(Intent intent) {
 
 		Parcelable[] rawMsgs = intent
 				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		// only one message sent during the beam
 		Toast.makeText(getApplicationContext(), "Got intent",
 				Toast.LENGTH_SHORT).show();
 		NdefMessage msg = (NdefMessage) rawMsgs[0];
 		String json = new String(msg.getRecords()[0].getPayload());
 		String userId = "";
 		long receivedTime = 0;
 		try {
 			JSONObject userData = new JSONObject(json);
 			userId = (String) userData.get("id");
 			receivedTime = (long) userData.getLong("time");
 			Toast.makeText(getApplicationContext(),
 					"Received NDEF from " + userId + " @" + receivedTime,
 					Toast.LENGTH_LONG).show();
 			Cart cart = Cart.getInstance();
 			cart.setUserId(userId);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
