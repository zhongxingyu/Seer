 package com.receiptspp.shopfrontmockup.android;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.NfcA;
 import android.os.AsyncTask;
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
 import com.receiptspp.shopfrontmockup.common.Keys;
 import com.receiptspp.shopfrontmockup.common.Util;
 
 public class CheckoutActivity extends Activity {
 
 	private NfcAdapter mNfcAdapter;
 	private Cart cart;
 	private NfcA mNfc;
 	private PendingIntent pendingIntent;
 	private IntentFilter[] intentFiltersArray;
 	private String[][] techListsArray;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_checkout);
 
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
 		// pending intent to be used for foreground dispatch
 		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
 				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
 		try {
 			// TODO: sort out this mime time.
 			ndef.addDataType("*/*");
 		} catch (MalformedMimeTypeException e) {
 			throw new RuntimeException();
 		}
 		intentFiltersArray = new IntentFilter[] { ndef, };
 		techListsArray = new String[][] { new String[] { NfcA.class.getName() } };
 
 		cart = Cart.getInstance();
 
 		TextView subTotalView = (TextView) findViewById(R.id.checkoutProductSubtotal);
 		subTotalView.setText(Util.generatePriceString(cart
 				.getTotalTransaction()));
 
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
 				if (userId == null) {
 					text = "No user ID Received, can't upload receipt";
 					Toast.makeText(getApplicationContext(), text,
 							Toast.LENGTH_LONG).show();
 				} else {
 					text = "User ID is " + userId;
 					Toast.makeText(getApplicationContext(), text,
 							Toast.LENGTH_LONG).show();
 					MockReceipt receipt = new MockReceipt(true);
 					receipt.setName("Trimtex Mock Store");
 					receipt.setCategory("Sports");
 					SubmitJsonToServer jsonUpload = new SubmitJsonToServer();
 					try {
 						String jsonString = receipt.toJSON().toString();
 						jsonUpload.execute(jsonString);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 					}
 				}
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
 	protected void onPause() {
 		super.onPause();
 		mNfcAdapter.disableForegroundDispatch(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
 				intentFiltersArray, techListsArray);
 		// Check to see that the Activity started due to an Android Beam
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			processIntent(getIntent());
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES) != null) {
			Log.v("nfc","Going to process NDEF intent");
			processIntent(intent);
			return;
		}
 		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag != null){
			Log.v("nfc","Processing tag");
 			processTag(tag);
			return;
 		}
		Log.v("nfc","Doing nothing");
 	}
 
 	/**
 	 * Process a TECH intent, that is an intent from a smart card
 	 * 
 	 * @param intent
 	 */
 	private void processTag(Tag tag) {
 		mNfc = NfcA.get(tag);
 		try {
 			mNfc.connect();
 			Log.d("tag", "connected.");
 			byte[] id = mNfc.getTag().getId();
 			Log.d("tag", "Got id from tag:" + id);
 			// Toast.makeText(this, "Hop: " + Util.getHex(id),
 			// Toast.LENGTH_LONG).show();
 			String cardId = Util.getHex(id);
 			new GetUserFromCard().execute(cardId);
 		} catch (IOException e) {
 			Log.d("tag", "error reading the tag");
 		} finally {
 			if (mNfc != null) {
 				try {
 					mNfc.close();
 				} catch (IOException e) {
 					Log.d("tag", "error closing the tag");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Get any messages from the intent and upload them to the server using the
 	 * SubmitVitalStats task
 	 * 
 	 * @param intent
 	 */
 	private void processIntent(Intent intent) {
 
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
 
 	private class GetUserFromCard extends AsyncTask<String, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			String cardId = params[0];
 
 			HttpClient client = new DefaultHttpClient();
 
 			String url = Keys.receiptsSmartcard + cardId;
 
 			HttpGet get = new HttpGet(url);
 
 			HttpResponse response;
 
 			try {
 				response = client.execute(get);
 				InputStream ins = response.getEntity().getContent();
 				BufferedReader buff = new BufferedReader(new InputStreamReader(
 						ins));
 				StringBuilder sb = new StringBuilder();
 				String line;
 				while ((line = buff.readLine()) != null) {
 					sb.append(line);
 				}
 
 				JSONObject json = new JSONObject(sb.toString());
 				boolean enabled = json.getBoolean(Keys.cardEnabled);
 				if (enabled) {
 					String userId = json.getString(Keys.userId);
 					Cart.getInstance().setUserId(userId);
 					return true;
 				}
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			// Cart.getInstance().setUserId(userId);
 			return false;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 			if (result) {
 				Toast.makeText(getApplicationContext(),
 						"User: " + Cart.getInstance().getUserId(),
 						Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(getBaseContext(), "Card not active.",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 	}
 
 	/**
 	 * Async task for Uploading JSON receipt data to server
 	 * 
 	 * @author Jourdan Harvey
 	 * 
 	 */
 	private class SubmitJsonToServer extends AsyncTask<String, Void, Boolean> {
 
 		private DefaultHttpClient httpclient;
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 
 			// instantiates httpclient to make request
 			httpclient = new DefaultHttpClient();
 
 			// passes the results to a string builder/entity
 			StringEntity jsonSE = null;
 			String jsonString;
 			try {
 				jsonString = params[0].toString();
 			} catch (IndexOutOfBoundsException e) {
 				// incorrect msg
 				return false;
 			}
 
 			try {
 				jsonSE = new StringEntity(jsonString);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				return false;
 			}
 
 			boolean result = true;
 
 			String userId = Cart.getInstance().getUserId();
 
 			String url = Keys.receiptsEndpointStart + userId
 					+ Keys.receiptsEndpointEnd;
 
 			Log.v("checkout", url);
 			Log.v("checkout", jsonString);
 
 			// call httpPost method to post the json to the server
 			result = httpPost(jsonSE, new HttpPost(url));
 
 			return result;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 			if (result) {
 				// Clear cart
 				Cart.getInstance().clear();
 				// Launch main activity
 				Intent intent = new Intent(getApplicationContext(),
 						MainActivity.class);
 				startActivity(intent);
 				finish();
 			}
 		}
 
 		/**
 		 * Take an HttpPost and execute it
 		 * 
 		 * @param se
 		 * @param httppost
 		 * @return
 		 */
 		private boolean httpPost(StringEntity se, HttpPost httppost) {
 			// sets the post request as the resulting string
 			httppost.setEntity(se);
 			// sets a request header so the page receiving the request
 			// will know what to do with it
 			httppost.setHeader("Accept", "application/json");
 			httppost.setHeader("Content-type", "application/json");
 
 			try {
 				// execute, get response, read
 				HttpResponse response = httpclient.execute(httppost);
 				InputStream content = response.getEntity().getContent();
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						content));
 				String line;
 				StringBuilder sb = new StringBuilder();
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 				Log.v("checkout", "Response:" + sb.toString());
 				// failing catches
 			} catch (ClientProtocolException e) {
 
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			// winning
 			return true;
 		}
 
 	}
 
 }
