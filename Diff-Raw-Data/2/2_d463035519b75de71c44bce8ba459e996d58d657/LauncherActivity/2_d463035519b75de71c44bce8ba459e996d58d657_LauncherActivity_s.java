 package ca.idrc.tagin.app;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 import ca.idrc.tagin.lib.TaginManager;
 import ca.idrc.tagin.lib.TaginService;
 
 public class LauncherActivity extends Activity {
 
 	private TaginManager mTaginManager;
 	private Button mRequestButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_launcher);
 		mRequestButton = (Button) findViewById(R.id.requestURN);
 		mTaginManager = new TaginManager(this);
 		registerReceiver(mReceiver, new IntentFilter(TaginService.ACTION_URN_READY));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.launcher, menu);
 		return true;
 	}
 
 	public void onRequestURN(View view) {
 		mRequestButton.setText("Requesting URN...");
 		mTaginManager.requestURN();
 	}
 	
 
 	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(TaginService.ACTION_URN_READY)) {
				String urn = intent.getStringExtra(TaginService.EXTRA_RESULT);
 				if (urn != null) {
 					mRequestButton.setText(urn);
 				} else {
 					mRequestButton.setText("Failed to acquire URN");
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
