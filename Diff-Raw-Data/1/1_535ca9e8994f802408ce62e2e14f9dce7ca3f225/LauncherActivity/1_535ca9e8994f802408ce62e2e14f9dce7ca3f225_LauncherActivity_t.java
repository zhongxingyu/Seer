 package ca.idrc.tagin.app;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.tagin.Tagin;
 import com.google.api.services.tagin.model.Pattern;
 import com.google.api.services.tagin.model.URN;
 
 public class LauncherActivity extends Activity {
 	
 	private Tagin mTaginService;
 	private Button mRequestButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_launcher);
 		mRequestButton = (Button) findViewById(R.id.requestURN);
 		
 		Tagin.Builder builder = new Tagin.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 		mTaginService = builder.build();
 	}
 	
 	public void onRequestURN(View view) {
 		new RequestURNTask().execute();
 	}
 	
 	private class RequestURNTask extends AsyncTask<Void, Void, URN> {
 		
 		private WifiManager mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
 		
 		@Override
 		protected void onPreExecute() {
 			mRequestButton.setText("Fetching URN...");
 		}
 
 		@Override
 		protected URN doInBackground(Void... params) {
 			URN urn = null;
 			if (mWifiManager.isWifiEnabled()) {
				mWifiManager.startScan();
 				Pattern pattern = new Pattern();
 				for (ScanResult sr : mWifiManager.getScanResults()) {
 					pattern.put(sr.BSSID, sr.frequency, sr.level);
 				}
 				pattern.updateRanks();
 				try {
 					urn = mTaginService.patterns().add(pattern).execute();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return urn;
 		}
 		
 		@Override
 		protected void onPostExecute(URN urn) {
 			if (urn != null)
 				mRequestButton.setText(urn.get("value").toString());
 			else
 				mRequestButton.setText("Could not fetch URN");
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.launcher, menu);
 		return true;
 	}
 
 }
