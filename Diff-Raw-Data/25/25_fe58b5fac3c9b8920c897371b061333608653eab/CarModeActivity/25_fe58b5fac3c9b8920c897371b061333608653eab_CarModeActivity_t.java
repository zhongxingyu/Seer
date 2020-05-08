 package com.ginkage.carmodetoggle;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 public class CarModeActivity extends Activity {
 	private static final String dock_state = "dockstate";
	
	public void Message(final String msg) {
		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
 
 	public void RunAsRoot(String cmd, int mode) {
 		boolean success = false;
 
 		try {
 			Process p = Runtime.getRuntime().exec("su");
 			DataOutputStream os = new DataOutputStream(p.getOutputStream());		
 			os.writeBytes(cmd + " " + mode + "\n");
 			os.writeBytes("exit\n");	
 			os.flush();
 
 			if (p.waitFor() != 255) {
				Message("Go to " + ((mode == Intent.EXTRA_DOCK_STATE_CAR) ? "car" : "normal") + " mode");
 				success = true;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		if (!success)
			Message("Failed to get root access");
 	}
 
 	private class SwitchState extends AsyncTask<String, Void, Void> {
 		@Override
 		protected Void doInBackground(String... param) {
 			try {
 				String cmd = param[0];
 				IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
 				Intent dockStatus = registerReceiver(null, ifilter);
 				int dockState = (dockStatus == null ?
 					Intent.EXTRA_DOCK_STATE_UNDOCKED :
 					dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1));
 
 				if (dockState == Intent.EXTRA_DOCK_STATE_UNDOCKED)
 					RunAsRoot(cmd, Intent.EXTRA_DOCK_STATE_CAR);
 				else if (dockState == Intent.EXTRA_DOCK_STATE_CAR)
 					RunAsRoot(cmd, Intent.EXTRA_DOCK_STATE_UNDOCKED);
 				else
					Message("Wrong dock mode");
 			}
 			catch (Exception e) {
 				e.printStackTrace();
				Message("Unknown error occured");
 			}
 
 			return null;
 		}
 	}
 
 	private String getDockPath() {
 		File file = getFileStreamPath(dock_state);
 		if (!file.exists()) {
 			try {
 				InputStream ins = getResources().openRawResource(R.raw.dockstate);
 				byte[] buffer = new byte[ins.available()];
 				ins.read(buffer);
 				ins.close();
 
 				FileOutputStream fos = openFileOutput(dock_state, Context.MODE_PRIVATE);
 				fos.write(buffer);
 				fos.close();
 
 				file.setExecutable(true);
 			} catch (IOException e) {
				Toast.makeText(this, "Error storing binary", Toast.LENGTH_SHORT).show();
 				return null;
 			}
 		}
 		return file.getAbsolutePath();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_car_mode);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
 
 		String cmd = getDockPath();
 		if (cmd != null)
 			new SwitchState().execute(cmd);
 
 		finish();
 	}
 }
