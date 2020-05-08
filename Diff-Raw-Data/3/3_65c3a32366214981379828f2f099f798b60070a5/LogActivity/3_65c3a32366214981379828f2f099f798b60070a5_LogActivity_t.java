 package se761.bestgroup.vsmreceiver;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class LogActivity extends Activity {
 
 	private NfcAdapter mNfcAdapter;
 	private ArrayAdapter<String> listAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_log);
 
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
 		listAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1);
 		ListView lv = (ListView) findViewById(R.id.logListView);
 		lv.setAdapter(listAdapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.log, menu);
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
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		// onResume gets called after this to handle the intent
 		setIntent(intent);
 	}
 
 	void processIntent(Intent intent) {

 		Parcelable[] rawMsgs = intent
 				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		// only one message sent during the beam
 		NdefMessage msg = (NdefMessage) rawMsgs[0];
 		String patient = new String(msg.getRecords()[0].getPayload());
 		String vitalInfo = new String(msg.getRecords()[1].getPayload());
 		listAdapter.add(patient);
 		listAdapter.add(vitalInfo);
 		// send the message somewhere
 		System.out.println("DEBUG: Creating async ");
 		SubmitVitalStats vitalStatsUpload = new SubmitVitalStats();
 		vitalStatsUpload.execute(patient, vitalInfo);
 	}
 
 	private class SubmitVitalStats extends AsyncTask<String, Void, Boolean> {
 
 		private DefaultHttpClient httpclient;

 
 		@Override
 		protected Boolean doInBackground(String... params) {
 
 			// instantiates httpclient to make request
 			httpclient = new DefaultHttpClient();
 
 			// url with the post data
 			System.out.println("DEBUG: Creating post ");
 
 
 			// passes the results to a string builder/entity
 			StringEntity patientSE = null;
 			StringEntity vitalStatsSE = null;
 			String patientString = params[0].toString();
 			String nhi;
 			try {
 				nhi = new JSONObject(patientString).getString("nhi");
 			} catch (JSONException e1) {
 				e1.printStackTrace();
 				return false;
 			}
 			try {
 				patientSE = new StringEntity(patientString);
 				vitalStatsSE = new StringEntity(params[1].toString());
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				return false;
 			}
 			
 			boolean result = true;
 			result = httpPost(patientSE, new HttpPost(
 					"http://vsm.herokuapp.com/patients/"));
 			result = httpPost(vitalStatsSE, new HttpPost(
 					"http://vsm.herokuapp.com/patients/" + nhi + "/vitalinfos/"));
 			
 			System.out.println("DEBUG: async done");
 			return result;
 		}
 		
 		private boolean httpPost(StringEntity se, HttpPost httpost) {
 			// sets the post request as the resulting string
 			httpost.setEntity(se);
 			// sets a request header so the page receving the request
 			// will know what to do with it
 			httpost.setHeader("Accept", "application/json");
 			httpost.setHeader("Content-type", "application/json");
 
 			System.out.println("DEBUG: getting response ");
 			try {
 				HttpResponse response = httpclient.execute(httpost);
 				InputStream content = response.getEntity().getContent();
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						content));
 				String line;
 				StringBuilder sb = new StringBuilder();
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 
 				Log.d("VSM", sb.toString());
 			} catch (ClientProtocolException e) {
 
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 	}
 
 
 }
