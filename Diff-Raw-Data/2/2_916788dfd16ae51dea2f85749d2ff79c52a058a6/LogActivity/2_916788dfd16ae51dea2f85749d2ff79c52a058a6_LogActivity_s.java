 package com.marakana.android.logclient;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.marakana.android.logcommon.ILogService;
 import com.marakana.android.logcommon.LogMessage;
 
 public class LogActivity extends Activity implements OnClickListener,
 		ServiceConnection {
 	private static final String TAG = "LogActivity";
	private static final int[] LOG_LEVEL = { Log.VERBOSE, Log.DEBUG, Log.DEBUG,
 			Log.WARN, Log.ERROR };
 
 	private Spinner priority;
 
 	private EditText tag;
 
 	private EditText msg;
 
 	private Button button;
 
 	private ILogService service;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		super.setContentView(R.layout.main);
 		this.priority = (Spinner) super.findViewById(R.id.log_priority);
 		this.tag = (EditText) super.findViewById(R.id.log_tag);
 		this.msg = (EditText) super.findViewById(R.id.log_msg);
 		this.button = (Button) super.findViewById(R.id.log_button);
 		this.button.setOnClickListener(this);
 		this.button.setEnabled(false);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.d(TAG, "Binding...");
 		if (!super.bindService(new Intent(ILogService.class.getName()), this,
 				BIND_AUTO_CREATE)) {
 			Log.d(TAG, "Failed to bind");
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.d(TAG, "Unbinding...");
 		super.unbindService(this);
 	}
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		Log.d(TAG, "Connected to " + name);
 		this.service = ILogService.Stub.asInterface(service);
 		this.button.setEnabled(true);
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		Log.d(TAG, "Disconnected from " + name);
 		this.service = null;
 		this.button.setEnabled(false);
 	}
 
 	public void onClick(View v) {
 		int priorityPosition = this.priority.getSelectedItemPosition();
 		if (priorityPosition != AdapterView.INVALID_POSITION) {
 			int priority = LOG_LEVEL[priorityPosition];
 			String tag = this.tag.getText().toString();
 			String msg = this.msg.getText().toString();
 			try {
 				this.service.log(new LogMessage(priority, tag, msg));
 				this.tag.getText().clear();
 				this.msg.getText().clear();
 				Toast.makeText(this, R.string.log_success, Toast.LENGTH_SHORT)
 						.show();
 			} catch (RemoteException e) {
 				Log.d(TAG, "Failed to log", e);
 				Toast.makeText(this, R.string.log_failure, Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 	}
 }
