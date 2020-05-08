 package ca.idrc.tagin.app;
 
 import java.io.IOException;
 import java.util.List;
 
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
 import android.widget.TextView;
 import android.widget.Toast;
 import ca.idrc.tagin.lib.TaginManager;
 import ca.idrc.tagin.lib.TaginService;
 
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.tagin.model.Fingerprint;
 import com.google.api.services.tagin.model.FingerprintCollection;
 
 public class TaginActivity extends Activity {
 	
 	private final String MAX_NEIGHBOURS = "10";
 
 	private TaginManager mTaginManager;
 	
 	private Button mURNRequestButton;
 	private Button mListFingerprintsButton;
 	private Button mFindNeighboursButton;
 	
 	private TextView mURNTextView;
 	private TextView mListFPTextView;
 	private EditText mNeighboursEditText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tagin);
 		mURNRequestButton = (Button) findViewById(R.id.requestURN);
 		mListFingerprintsButton = (Button) findViewById(R.id.listFingerprints);
 		mFindNeighboursButton = (Button) findViewById(R.id.findButton);
 		mURNTextView = (TextView) findViewById(R.id.textView1);
 		mListFPTextView = (TextView) findViewById(R.id.textView2);
 		mNeighboursEditText = (EditText) findViewById(R.id.editText3);
 		
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
 		mURNRequestButton.setText(R.string.requesting_urn);
 		mTaginManager.apiRequest(TaginService.REQUEST_URN);
 	}
 	
 	public void onListFingerprints(View view) {
 		mListFingerprintsButton.setText(R.string.requesting_fp_list);
 		mTaginManager.apiRequest(TaginService.REQUEST_LIST_FINGERPRINTS);
 	}
 	
 	public void onFindNeighbours(View view) {
 		if (mNeighboursEditText.getText().length() < 30) {
 			Toast.makeText(this, R.string.invalid_urn, Toast.LENGTH_SHORT).show();
 		} else {
 			mFindNeighboursButton.setText(R.string.searching_for_neighbours);
 			String urn = mNeighboursEditText.getText().toString();
 			mTaginManager.apiRequest(TaginService.REQUEST_NEIGHBOURS, urn, MAX_NEIGHBOURS);
 		}
 	}
 	
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(TaginService.ACTION_URN_READY)) {
 				String urn = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				handleURNResponse(urn);
 			} else if (intent.getAction().equals(TaginService.ACTION_FINGERPRINTS_READY)) {
 				String str = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				handleFingerprintsResponse(str);
 			} else if (intent.getAction().equals(TaginService.ACTION_NEIGHBOURS_READY)) {
 				String str = intent.getStringExtra(TaginService.EXTRA_QUERY_RESULT);
 				handleNeighboursResponse(str);
 			}
 		}
 	};
 	
 	private void handleFingerprintsResponse(String result) {
 		FingerprintCollection fps = null;
 		if (result != null) {
 			try {
 				fps = new GsonFactory().fromString(result, FingerprintCollection.class);
 			} catch (IOException e) {
				Log.e("tagin!", "Deserialization error: " + e.getMessage());
 			}
 		}
 		
 		if (fps != null) {
 			StringBuffer sb = new StringBuffer();
 			List<Fingerprint> items = fps.getItems();
 			if (items != null) {
 				for (Fingerprint fp : items) {
 					sb.append("ID:     " + fp.getId() + "\nURN:   " + fp.getUrn() + "\n\n");
 				}
 			}
 			mListFPTextView.setText(sb.toString());
 		} else {
 			mListFPTextView.setText(R.string.failed_list_fp);
 		}
 		mListFingerprintsButton.setText(R.string.fp_list);
 	}
 	
 	private void handleURNResponse(String urn) {
 		if (urn != null) {
 			mURNTextView.setText(urn);
 		} else {
 			mURNTextView.setText(R.string.failed_acquire_urn);
 		}
 		mURNRequestButton.setText(R.string.request_urn);
 	}
 	
 	private void handleNeighboursResponse(String result) {
 		if (result != null) {
 			mNeighboursEditText.setText(result);
 		} else {
 			mNeighboursEditText.setText(R.string.could_not_find_neighbours);
 		}
 		mFindNeighboursButton.setText(R.string.find_neighbours);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(mReceiver);
 	}
 
 }
