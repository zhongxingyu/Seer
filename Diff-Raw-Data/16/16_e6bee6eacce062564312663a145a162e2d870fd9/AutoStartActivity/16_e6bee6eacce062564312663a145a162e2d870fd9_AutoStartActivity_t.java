 package edu.illinois.CS598rhk;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import edu.illinois.CS598rhk.services.BluetoothService;
 import edu.illinois.CS598rhk.services.CoreTask;
 import edu.illinois.CS598rhk.services.LoggingService;
 import edu.illinois.CS598rhk.services.SchedulerService;
 import edu.illinois.CS598rhk.services.WifiService;
 
 public class AutoStartActivity extends Activity {
 
 	private static final String MSG_TAG = "MainACtivity";
 	private static final String CONFIG_FILENAME = "config";
 	private static String ICICLE_KEY = "main-activity";
 	private static String IPADDR_KEY = "ipAddr";
 	private static String STATE_KEY = "state";
 	private CoreTask coretask;
 
 	public static String ADDRESS_KEY = "address key";
 	public static String BT_ADDRESS_KEY = "bt address key";
 
 	public static String INTENT_TO_UPDATE_UI = "update debug ui";
 
 	private String ipAddr;
 
 	private EditText ipAddrText;
 
 	private Button startButton;
 	private Button stopButton;
 
 	private boolean servicesStarted;
 	private boolean debugViewUp;
 
 	private Timer debugViewUpdateTimer;
 	private TimerTask debugViewUpdateTask;
 	private DebugViewMessageReceiver messageReceiver;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (savedInstanceState == null) {
 			ipAddr = null;
 			servicesStarted = false;
 		} else {
 			Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
 			if (map != null) {
 				ipAddr = map.getString(IPADDR_KEY);
 				servicesStarted = map.getBoolean(STATE_KEY);
 			}
 		}
 
 		setContentView(R.layout.main);
 
 		Intent blueTest = new Intent(
 				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		blueTest.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
 		startActivity(blueTest);
 
 		ipAddrText = (EditText) findViewById(R.id.IPAddrText);
 		startButton = (Button) findViewById(R.id.StartButton);
 		stopButton = (Button) findViewById(R.id.StopButton);
 		
 		updateButtons(servicesStarted);
 
 		Intent blueIntent = new Intent(
 				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		blueIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
 		startActivity(blueIntent);
 
 		coretask = new CoreTask();
 		coretask.setPath(this.getApplicationContext().getFilesDir().getParent());
 		Log.d(MSG_TAG, "Current directory is "
 				+ this.getApplicationContext().getFilesDir().getParent());
 
 		IntentFilter messageFilter = new IntentFilter();
 		messageFilter.addAction(INTENT_TO_UPDATE_UI);
 		messageFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
 		messageReceiver = new DebugViewMessageReceiver();
 		registerReceiver(messageReceiver, messageFilter);
 		debugViewUpdateTimer = new Timer();
 		debugViewUpdateTask = new TimerTask() {
 
 			@Override
 			public void run() {
 				Intent updateDebug = new Intent(INTENT_TO_UPDATE_UI);
 				sendBroadcast(updateDebug);
 			}
 		};
 		debugViewUpdateTimer.scheduleAtFixedRate(debugViewUpdateTask, 500,
 				500);
 
 		// Check Homedir, or create it
 		this.checkDirs();
 
 		// Check for binaries
 		boolean filesetOutdated = coretask.filesetOutdated();
 		if (binariesExists() == false || filesetOutdated) {
 			if (coretask.hasRootPermission()) {
 				installBinaries();
 				// if (filesetOutdated) {
 				// this.openConfigRecoverDialog();
 				// }link
 			} else {
 				this.openNotRootDialog();
 			}
 		}
 
 		startButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (canStart()) {
 					try {
 						saveConfigFile();
 					} catch (IOException e) {
 						
 					}
 					updateButtons(true);
 					startServices();
 				} else {
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
 
 		try 
 		{
 			loagConfigFile();
 		} catch (IOException e) {
 			ipAddr = "192.168.1.2";
 		}
 		
 		ipAddrText.setText(ipAddr);
 				
 		//auto start
 		updateButtons(true);
 		startServices();
 		
 		
 	}
 
 	private class DebugViewMessageReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(INTENT_TO_UPDATE_UI)) {
 				updateDebugView();
 			} else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
 				if(intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0) != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
 				{
 					Intent blueIntent = new Intent(
 							BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 					blueIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
 					startActivity(blueIntent);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.auto_menu, menu);
 		return true;
 	}
 	
 	public void loagConfigFile() throws IOException {
 		byte[] buffer = new byte[1024];
 		FileInputStream fis = openFileInput(CONFIG_FILENAME);
 		fis.read(buffer);
 		fis.close();
 		
 		String input = new String(buffer);
 		ipAddr = input.split("\n")[0];
 		
 	}
 	
 	public void saveConfigFile() throws IOException {
 		FileOutputStream fos = openFileOutput(CONFIG_FILENAME, MODE_WORLD_READABLE);
 		fos.write((ipAddr+"\n").getBytes());
 		fos.close();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.start:
 			if (!servicesStarted)
 				startServices();
				updateButtons(true);
 			return true;
 		case R.id.stop:
 			if (servicesStarted) {
 				updateButtons(false);
 				stopServices();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void updateDebugView() {
 		if (debugViewUp) {
 			if (servicesStarted) {
 
 				((TextView) findViewById(R.id.btmac)).setText("bt address: "
 						+ BluetoothAdapter.getDefaultAdapter().getAddress());
 				((TextView) findViewById(R.id.btname)).setText("name:"
 						+ BluetoothAdapter.getDefaultAdapter().getName());
 				if (BluetoothService.activeNeighbors != null) {
 					((TextView) findViewById(R.id.btneighborcount))
 							.setText("bt active count: "
 									+ BluetoothService.activeNeighbors.size());
 				}
 				if (BluetoothService.potentialNeighbors != null) {
 					((TextView) findViewById(R.id.btneighborcount2))
 							.setText("bt potential count: "
 									+ BluetoothService.potentialNeighbors.size());
 				}
 				((TextView) findViewById(R.id.wifiip)).setText("ip address: "
 						+ WifiService.myIPAddress);
 
 				((TextView) findViewById(R.id.wifineighborcount))
 						.setText("wifi neighbor count: "
 								+ SchedulerService.getNeighborCount());
 
 				if (WifiService.wifiState == WifiService.WIFI_STATE_DISCOVERYING)
 					((TextView) findViewById(R.id.wifion))
 							.setText("wifi state: discoverying");
 				else
 					((TextView) findViewById(R.id.wifion))
 							.setText("wifi state: paused");
 				((TextView) findViewById(R.id.wifiprog))
 						.setText("wifi progress: " + WifiService.timeSlice
 								+ "/25");
 				((TextView) findViewById(R.id.services))
 						.setText("services: started");
 				((TextView) findViewById(R.id.bterrorcount))
 						.setText("bt errocount: " + BluetoothService.errorCount);
 			} else {
 				((TextView) findViewById(R.id.services))
 						.setText("services: stopped");
 			}
 		}
 	}
 
 	// From MainActivity.java
 	private void openNotRootDialog() {
 		LayoutInflater li = LayoutInflater.from(this);
 		View view = li.inflate(R.layout.norootview, null);
 		new AlertDialog.Builder(this)
 				.setTitle("Not Root!")
 				.setIcon(R.drawable.warning)
 				.setView(view)
 				.setNegativeButton("Close",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								// Log.d(MSG_TAG, "Close pressed");
 								// this.application.finish();
 							}
 						})
 				.setNeutralButton("Override",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								// Log.d(MSG_TAG, "Override pressed");
 								// installBinaries();
 							}
 						}).show();
 	}
 
 	private void displayError() {
 		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
 		alertbox.setMessage("Invalid Phone Id and/or IP address.");
 		alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface arg0, int arg1) {
 				// Do Nothing
 			}
 		});
 		alertbox.show();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		Bundle map = new Bundle();
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
 		if (servicesStarted) {
 			debugViewUp = true;
 			setContentView(R.layout.debug);
 		}
 	}
 
 	private void startServices() {
 		startService(new Intent(AutoStartActivity.this, LoggingService.class));
 		Intent i = new Intent(AutoStartActivity.this, SchedulerService.class);
 		i.putExtra(ADDRESS_KEY, ipAddrText.getText().toString());
 		startService(i);
 
 	}
 
 	private void stopServices() {
 		stopService(new Intent(AutoStartActivity.this, LoggingService.class));
 		stopService(new Intent(AutoStartActivity.this, SchedulerService.class));
 		stopService(new Intent(AutoStartActivity.this, WifiService.class));
 		stopService(new Intent(AutoStartActivity.this, BluetoothService.class));
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
 		this.coretask.runRootCommand("/sbin/ifconfig tiwlan0 " + ip
 				+ " netmask 255.255.255.0");
 
 	}
 
 	public void installBinaries() {
 		List<String> filenames = new ArrayList<String>();
 		// tether
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/netcontrol",
 				edu.illinois.CS598rhk.R.raw.netcontrol);
 		filenames.add("netcontrol");
 		// dnsmasq
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/dnsmasq",
 				edu.illinois.CS598rhk.R.raw.dnsmasq);
 		filenames.add("dnsmasq");
 		// iwconfig
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/iwconfig",
 				edu.illinois.CS598rhk.R.raw.iwconfig);
 		filenames.add("iwconfig");
 		// wl1251 modules
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/compat.ko",
 				edu.illinois.CS598rhk.R.raw.compat);
 		filenames.add("compat.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/crc7.ko",
 				edu.illinois.CS598rhk.R.raw.crc7);
 		filenames.add("crc7.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/cfg80211.ko",
 				edu.illinois.CS598rhk.R.raw.cfg80211);
 		filenames.add("cfg80211.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/mac80211.ko",
 				edu.illinois.CS598rhk.R.raw.mac80211);
 		filenames.add("mac80211.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/wl1251.ko",
 				edu.illinois.CS598rhk.R.raw.wl1251);
 		filenames.add("compat.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/wl1251_sdio.ko",
 				edu.illinois.CS598rhk.R.raw.wl1251_sdio);
 		filenames.add("wl1251_sdio.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/msm_wifi.ko",
 				edu.illinois.CS598rhk.R.raw.msm_wifi);
 		filenames.add("msm_wfi.ko");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/msm_wifi.ko",
 				edu.illinois.CS598rhk.R.raw.msm_wifi);
 		filenames.add("msm_wfi.ko");
 		// wifi scripts
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/load.sh",
 				edu.illinois.CS598rhk.R.raw.load);
 		filenames.add("load.sh");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/up.sh",
 				edu.illinois.CS598rhk.R.raw.up);
 		filenames.add("up.sh");
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/bin/down.sh",
 				edu.illinois.CS598rhk.R.raw.down);
 		filenames.add("down.sh");
 
 		try {
 			this.coretask.chmodBin(filenames);
 		} catch (Exception e) {
 			Log.d(MSG_TAG, "Unable to change permission on binary files!");
 		}
 		// dnsmasq.conf
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/conf/dnsmasq.conf",
 				edu.illinois.CS598rhk.R.raw.dnsmasq_conf);
 		// tiwlan.ini
 		this.copyBinary(this.coretask.DATA_FILE_PATH + "/conf/tiwlan.ini",
 				edu.illinois.CS598rhk.R.raw.tiwlan_ini);
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
