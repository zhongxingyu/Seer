 package cz.xlinux.spApp;
 
 import cz.xlinux.libAPI.aidl.EntryPoint;
 import cz.xlinux.libAPI.libFce.APIConnection;
 import cz.xlinux.libAPI.libFce.CBOnSvcChange;
 import cz.xlinux.spApp.R;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SPActivity extends Activity implements OnClickListener,
 		CBOnSvcChange {
 
 	private static final String LOG_TAG = "SPActivity";
 	private APIConnection conn;
 	private TextView mTvLog;
 	private boolean isBound;
 	private EntryPoint apiService;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// ---
 		mTvLog = (TextView) findViewById(R.id.tvLog);
 		// mTvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
 
 		Button clr;
 		clr = (Button) findViewById(R.id.btTestCert);
 		clr.setOnClickListener(this);
 		clr = (Button) findViewById(R.id.btTestSvc);
 		clr.setOnClickListener(this);
 		clr = (Button) findViewById(R.id.btTestScan);
 		clr.setOnClickListener(this);
 		// ---
 
 		conn = new APIConnection();
 		
		Intent intent = new Intent("core.API.BindAction");
 		intent.putExtra("version", "1.0");
 		Log.d(LOG_TAG, "intent = " + intent);
 
 		isBound = bindService(intent, conn, Context.BIND_AUTO_CREATE);
 		Log.d(LOG_TAG, "bindService = " + isBound);
 	}
 
 	@Override
 	public void onClick(View v) {
 		mTvLog.setText("...");
 		Log.d(LOG_TAG, "onClick,v=" + v);
 		switch (v.getId()) {
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		try {
 			if (isBound) {
 				unbindService(conn);
 			}
 		} catch (Throwable t) {
 		}
 	}
 
 	@Override
 	public void setService(EntryPoint apiService) {
 		this.apiService = apiService;
 	}
 }
