 package ca.idrc.tagin.app;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import ca.idrc.tagin.lib.TaginManager;
 import ca.idrc.tagin.lib.TaginService;
 
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.tagin.model.Fingerprint;
 import com.google.api.services.tagin.model.FingerprintCollection;
 
 public class LauncherActivity extends Activity {
 
 	private TaginManager mTaginManager;
 	
 	private Button mURNRequestButton;
 	private Button mListFingerprintsButton;
 	private Button mFindNeighboursButton;
 	private EditText mEditText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_launcher);
 		mURNRequestButton = (Button) findViewById(R.id.requestURN);
 		mListFingerprintsButton = (Button) findViewById(R.id.listFingerprints);
 		mFindNeighboursButton = (Button) findViewById(R.id.findButton);
 		mEditText = (EditText) findViewById(R.id.editText);
 		
 		mTaginManager = new TaginManager(this);
 		registerReceiver(mReceiver, new IntentFilter(TaginService.ACTION_URN_READY));
 		registerReceiver(mReceiver, new IntentFilter(TaginService.ACTION_NEIGHBOURS_READY));
 		registerReceiver(mReceiver, new IntentFilter(TaginService.ACTION_FINGERPRINTS_READY));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.launcher, menu);
 		return true;
 	}
 
 	public void onRequestURN(View view) {
 		mURNRequestButton.setText("Requesting URN...");
 		mTaginManager.apiRequest(TaginService.REQUEST_URN);
 	}
 	
 	public void onListFingerprints(View view) {
 		mListFingerprintsButton.setText("Requesting fingerprints list...");
 		mTaginManager.apiRequest(TaginService.REQUEST_LIST_FINGERPRINTS);
 	}
 	
 	public void onFindNeighbours(View view) {
 		if (mEditText.getText().length() != 32) {
 			Toast.makeText(this, "Invalid URN", Toast.LENGTH_SHORT).show();
 		} else {
 			mFindNeighboursButton.setText("Searching for nearby neighbours...");
 			mTaginManager.apiRequest(TaginService.REQUEST_NEIGHBOURS, mEditText.getText().toString());
 		}
 	}
 	
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(TaginService.ACTION_URN_READY)) {
 				String urn = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				if (urn != null) {
 					mURNRequestButton.setText(urn);
 				} else {
 					mURNRequestButton.setText("Failed to acquire URN");
 				}
 			} else if (intent.getAction().equals(TaginService.ACTION_FINGERPRINTS_READY)) {
 				String str = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				FingerprintCollection fps = null;
 				
 				try {
 					fps = new GsonFactory().fromString(str, FingerprintCollection.class);
 				} catch (IOException e) {
 					Log.e("tagin!", "Deserialization error: " + e.getMessage());
 				}
 				
 				if (fps != null) {
					StringBuffer items = new StringBuffer();
					for (Fingerprint fp : fps.getItems()) {
						items.append("ID: " + fp.getId() + "\nURN: " + fp.getUrn() + "\n\n");
 					}
					mListFingerprintsButton.setText(items.toString());
 				} else {
 					mListFingerprintsButton.setText("Failed to list fingerprints");
 				}
 			} else if (intent.getAction().equals(TaginService.ACTION_NEIGHBOURS_READY)) {
 				String str = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				if (str != null) {
 					mFindNeighboursButton.setText(str);
 				} else {
 					mFindNeighboursButton.setText("Could not find neighbours");
 				}
 			}
 		}
 	};
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(mReceiver);
 	}
 
 }
