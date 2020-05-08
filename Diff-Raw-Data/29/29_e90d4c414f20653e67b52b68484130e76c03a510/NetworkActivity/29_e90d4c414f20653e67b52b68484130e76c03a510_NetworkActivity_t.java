 package edu.uidaho.engr.seniordesign.umax;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.uidaho.engr.seniordesign.umax.DefaultVariables.OnControlStatusListener;
 import edu.uidaho.engr.seniordesign.umax.NetConnectionFragment.OnNetListClickListener;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 
 public class NetworkActivity extends Activity implements OnNetListClickListener {
 	
 	private CommService commService;
 	private Intent serviceIntent;
 	private NetConnectionFragment ncf;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         ActionBar actionBar = getActionBar();
         
        //actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ad_action_bar_gradient_bak));
         actionBar.setDisplayHomeAsUpEnabled(true);
         
         if (savedInstanceState == null) {
             ncf = new NetConnectionFragment();
             ncf.setArguments(getIntent().getExtras());
             getFragmentManager().beginTransaction().add(android.R.id.content, ncf).commit();
         }
         
         commService = CommService.getInstance();
         
         if (commService == null) {
         	serviceIntent = new Intent(getApplicationContext(), CommService.class);
         	startService(serviceIntent);
         }
         
         commService = CommService.getInstance();
         if (commService == null) {
         	Timer t = new Timer();
         	t.schedule(getCommService, 50);
         } else {
         	fragmentHandler.sendMessage(fragmentHandler.obtainMessage(0));
         }
         
         DefaultVariables.addOnControlStatusListener(new OnControlStatusListener() {
 
 			public void updateView(int what, int arg1, int arg2, Object obj) {
 				viewUpdateHandler.sendMessage(viewUpdateHandler.obtainMessage(what, arg1, arg2, obj));
 			}
         	
         });
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, UMaxRemoteActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
             overridePendingTransition(0, 0);
 			return true;
 		}
 		return false;
 	}
 	
 	private TimerTask getCommService = new TimerTask() {
 		@Override
 		public void run() {
 			commService = CommService.getInstance();
 	        if (commService == null) {
 	        	Timer t = new Timer();
 	        	getCommService.cancel();
 	        	t.schedule(getCommService, 50);
 	        } else {
 	        	fragmentHandler.sendMessage(fragmentHandler.obtainMessage(0));
 	        }
 		}
     };
     
     final Handler fragmentHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		if (msg.what == 0) {
     			DefaultVariables.SetNetConnectionFragment(ncf);
     		}
     	}
     };
 
     public void connect() {
     	final SharedPreferences settings  = getSharedPreferences(UMaxRemoteActivity.PREFS_NAME, 0);
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Enter address");
 		alert.setMessage("Enter address");
 		final EditText input = new EditText(this);
 		String lastConnectedAddress = settings.getString("lastConnectedAddress", "");
 		input.setText(lastConnectedAddress);
 		input.setInputType(0x00000003);
 		alert.setView(input);
 		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString("lastConnectedAddress", input.getText().toString());
 				editor.commit();
 				DefaultVariables.setConnecting();
 				commService.connect(input.getText().toString());
 			}
 		});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				// canceled.
 			}
 		});
 		alert.show();
 		
 	}
 
 	public void disconnect() {
 		commService.disconnect();
 	}
     
     private Handler viewUpdateHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		if (msg.what == 0) {
     			// set enabled
     			if (msg.obj == null)
     				return;
     			setViewEnabled((View) msg.obj, (msg.arg1 == 1 ? true : false));
     		} else if (msg.what == 1) {
     			// set enabled
     			if (msg.obj == null)
     				return;
     			setMenuEnabled((MenuItem) msg.obj, (msg.arg1 == 1 ? true : false));
     		}
     	}
     	public void setViewEnabled(View view, boolean enabled) {
     		try {
     			view.setEnabled(enabled);
     		}
     		catch (Exception e) {
     			// we can't touch this view, so we do nothing
     		}
     	}
     	public void setMenuEnabled(MenuItem view, boolean enabled) {
     		try {
     			view.setEnabled(enabled);
     		}
     		catch (Exception e) {
     			// we can't touch this view, so we do nothing
     		}
     	}
     };
 }
