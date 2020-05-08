 package edu.illinois.CS598rhk;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import edu.illinois.CS598rhk.services.BluetoothService;
 import edu.illinois.CS598rhk.services.CoreTask;
 import edu.illinois.CS598rhk.services.PowerManagement;
 import edu.illinois.CS598rhk.services.SchedulerService;
 import edu.illinois.CS598rhk.services.WifiService;
 
 public class MainActivity extends Activity {
 	
 	private static final String MSG_TAG = "MainACtivity";
 	private static String ICICLE_KEY = "main-activity";
 	private static String PHONEID_KEY = "phoneID";
 	private static String IPADDR_KEY = "ipAddr";
 	private static String STATE_KEY = "state";
 	private CoreTask coretask;
 	
 	public static String NAME_KEY = "name key";
 	public static String ADDRESS_KEY = "address key";
 	
 	private String phoneID;
 	private String ipAddr;
 
 	private EditText phoneIdText;
 	private EditText ipAddrText;
 	
 	private Button startButton;
 	private Button stopButton;
 	
 	private boolean servicesStarted;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		if (savedInstanceState == null) {
 			phoneID = null;
 			ipAddr = null;
 			servicesStarted = false;
 		} else {
 			Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
 			if (map != null) {
 				phoneID = map.getString(PHONEID_KEY);
 				ipAddr = map.getString(IPADDR_KEY);
 				servicesStarted = map.getBoolean(STATE_KEY);
 			}
 		}
 
 		setContentView( R.layout.main );
 		
 		phoneIdText = (EditText) findViewById(R.id.PhoneIdText);
 		ipAddrText = (EditText) findViewById(R.id.IPAddrText);
 		startButton = (Button) findViewById(R.id.StartButton);
 		stopButton = (Button) findViewById(R.id.StopButton);
 		
 		updateButtons(servicesStarted);
 		
 		coretask = new CoreTask();
         coretask.setPath(this.getApplicationContext().getFilesDir().getParent());
         Log.d(MSG_TAG, "Current directory is "
                 + this.getApplicationContext().getFilesDir().getParent());
 
      // Check Homedir, or create it
         this.checkDirs();
 
         // Check for binaries
         boolean filesetOutdated = coretask.filesetOutdated();
         if (binariesExists() == false || filesetOutdated) {
             if (coretask.hasRootPermission()) {
                 installBinaries();
                 // if (filesetOutdated) {
                 // this.openConfigRecoverDialog();
                 // }
             } else {
                 this.openNotRootDialog();
             }
         }
         
 		startButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (canStart()) {
 					updateButtons(true);
 					startServices();
 				}
 				else {
 					displayError();
 				}
 			}
 		});
 		
 		stopButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				updateButtons(false);
 				stopServices();
 			}
 		});
 		
 		if (phoneID != null) {
 			phoneIdText.setText(phoneID);
 		}
 		if (ipAddr != null)  {
 			ipAddrText.setText(ipAddr);
 		}
	
 	}
 	
 	// From MainActivity.java
     private void openNotRootDialog() {
         LayoutInflater li = LayoutInflater.from(this);
         View view = li.inflate(R.layout.norootview, null);
         new AlertDialog.Builder(this).setTitle("Not Root!").setIcon(R.drawable.warning)
                 .setView(view).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         //Log.d(MSG_TAG, "Close pressed");
                         // this.application.finish();
                     }
                 }).setNeutralButton("Override", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         //Log.d(MSG_TAG, "Override pressed");
                         //installBinaries();
                     }
                 }).show();
     }
 	
 	private void displayError() {
 		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
         alertbox.setMessage("Invalid Phone Id and/or IP address.");
         alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface arg0, int arg1) {
                 //Do Nothing
             }
         });
         alertbox.show();
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		Bundle map = new Bundle();
 		map.putString(PHONEID_KEY, phoneID);
 		map.putString(IPADDR_KEY, ipAddr);
 		map.putBoolean(STATE_KEY, servicesStarted);
 		outState.putBundle(ICICLE_KEY, map);
 	}
 
 	
 	private boolean canStart() {
 		return (validIP() && validPhoneId());
 	}
 	
 	private boolean validIP() {
 		return true;
 	}
 	
 	private boolean validPhoneId() {
 		return true;
 	}
 	
 	private void updateButtons(boolean state) {
 		servicesStarted = state;
 		startButton.setEnabled(!servicesStarted);
 		stopButton.setEnabled(servicesStarted);
 	}
 	
 	private void startServices() {
 		startService(new Intent(MainActivity.this, PowerManagement.class));
 		Intent i = new Intent(MainActivity.this, SchedulerService.class);
		i.putExtra(NAME_KEY, phoneIdText.getText().toString());
		i.putExtra(ADDRESS_KEY, ipAddrText.getText().toString());
 		startService(i);
 	}
 	
 	private void stopServices() {
 		stopService(new Intent(MainActivity.this, PowerManagement.class));
 		stopService(new Intent(MainActivity.this, SchedulerService.class));
 		stopService(new Intent(MainActivity.this, WifiService.class));
 		stopService(new Intent(MainActivity.this, BluetoothService.class));
 	}
 	
 	// Binary install
     public boolean binariesExists() {
         File file = new File(this.coretask.DATA_FILE_PATH + "/bin/tether");
         if (file.exists()) {
             return true;
         }
         return false;
     }
     
     public void setIPAddress(String ip) {
     	this.coretask.runRootCommand("/sbin/ifconfig tiwlan0 "+ip+" netmask 255.255.255.0");
     	
     }
 
     public void installBinaries() {
         List<String> filenames = new ArrayList<String>();
         // tether
         this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/netcontrol", edu.illinois.CS598rhk.R.raw.netcontrol);
         filenames.add("netcontrol");
         // dnsmasq
         this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/dnsmasq", edu.illinois.CS598rhk.R.raw.dnsmasq);
         filenames.add("dnsmasq");
         // iwconfig
         this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/iwconfig", edu.illinois.CS598rhk.R.raw.iwconfig);
         filenames.add("iwconfig");
         try {
             this.coretask.chmodBin(filenames);
         } catch (Exception e) {
         	Log.d(MSG_TAG, "Unable to change permission on binary files!");
         }
         // dnsmasq.conf
         this.copyBinary(this.coretask.DATA_FILE_PATH + "/conf/dnsmasq.conf", edu.illinois.CS598rhk.R.raw.dnsmasq_conf);
         // tiwlan.ini
         this.copyBinary(this.coretask.DATA_FILE_PATH + "/conf/tiwlan.ini", edu.illinois.CS598rhk.R.raw.tiwlan_ini);
         Log.d(MSG_TAG, "Binaries and config-files installed!");
     }
 
     private void copyBinary(String filename, int resource) {
         File outFile = new File(filename);
         InputStream is = this.getResources().openRawResource(resource);
         byte buf[] = new byte[1024];
         int len;
         try {
             OutputStream out = new FileOutputStream(outFile);
             while ((len = is.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
             out.close();
             is.close();
         } catch (IOException e) {
         	Log.d(MSG_TAG, "Couldn't install file - " + filename + "!");
         }
     }
 
     private void checkDirs() {
         File dir = new File(this.coretask.DATA_FILE_PATH);
         if (dir.exists() == false) {
         	Log.d(MSG_TAG, "Application data-dir does not exist!");
         } else {
             dir = new File(this.coretask.DATA_FILE_PATH + "/bin");
             if (dir.exists() == false) {
                 if (!dir.mkdir()) {
                 	Log.d(MSG_TAG, "Couldn't create bin-directory!");
                 }
             }
             dir = new File(this.coretask.DATA_FILE_PATH + "/var");
             if (dir.exists() == false) {
                 if (!dir.mkdir()) {
                 	Log.d(MSG_TAG, "Couldn't create var-directory!");
                 }
             }
             dir = new File(this.coretask.DATA_FILE_PATH + "/conf");
             if (dir.exists() == false) {
                 if (!dir.mkdir()) {
                 	Log.d(MSG_TAG, "Couldn't create conf-directory!");
                 }
             }
         }
     }
 }
