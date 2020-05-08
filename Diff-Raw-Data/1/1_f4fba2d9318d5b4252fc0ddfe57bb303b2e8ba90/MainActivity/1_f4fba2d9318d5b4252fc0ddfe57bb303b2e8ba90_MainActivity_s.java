 package com.example.breezehome;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.widget.Toast;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.net.wifi.SupplicantState;
 import android.nfc.FormatException;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 
 public class MainActivity extends Activity implements 
 		HomeFragment.ActivityListener, 
 		WebServiceFragment.OnUrlListener,
 		AdminFragment.ActivityListener {
 	
 	// NFC
 	private NfcAdapter mNfcAdapter;
 	private IntentFilter tagFilters[];
 	private boolean writeMode;
 	private Tag mytag;
 	
 	// WiF
 	private WifiManager wifi;
 	private WifiInfo wifiInfo;
 	private boolean disconnectOccurred;
 	private String currentSSID;
 	private String breezehomeSSID;
 	private String breezehomePass;
 
 	// Action Bar
 	private ActionBar actionbar;
 
 	// HomeFragment
 	private HomeFragment homeFragment;
 	private ActionBar.Tab homeTab;
 	private BreezehomeService breezehomeService;
 	private ArrayList<BreezehomeService> serviceList;
 	private String helpText = "";
 	
 	// WebServiceFragment
 	private WebServiceFragment webServiceFragment;
 	private ActionBar.Tab webServiceTab;
 	private String selectedUrl = "";
 
 	// AdminFragment
 	private AdminFragment adminFragment;
 	private ActionBar.Tab adminTab;
 	private String tagText = "";
 	
 	///////////////////////////////////////////////////////////////////
 	// Activity Life-cycle events
 	///////////////////////////////////////////////////////////////////
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d("DEBUG", "MainActivity.onCreate");
 
         actionbar = getActionBar();
         actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         
         homeTab = actionbar.newTab().setIcon(R.drawable.home_fragment_icon);
         webServiceTab = actionbar.newTab().setIcon(R.drawable.webservice_fragment_icon);
         adminTab = actionbar.newTab().setIcon(R.drawable.admin_fragment_icon);
         
         homeTab.setTabListener(new TabListener<HomeFragment>(this, "home", HomeFragment.class));
         webServiceTab.setTabListener(new TabListener<WebServiceFragment>(this, "webService", WebServiceFragment.class));
         adminTab.setTabListener(new TabListener<AdminFragment>(this, "admin", AdminFragment.class));
         
         actionbar.addTab(homeTab);
         actionbar.addTab(webServiceTab);
         actionbar.addTab(adminTab);
         
         mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter == null) {
             Log.d("NFC", "NFC Not available");
         }
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         Log.d("DEBUG", "MainActivity.onResume");
         wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         if (wifi.isWifiEnabled() == false) {
         	wifi.setWifiEnabled(true);
         }
         
         if (homeFragment == null) {
         	homeFragment = (HomeFragment)getFragmentManager().findFragmentByTag("home");
        
         }
         if (webServiceFragment == null) {
         	webServiceFragment = (WebServiceFragment)getFragmentManager().findFragmentByTag("webService");
         }
         if (adminFragment == null) {
         	adminFragment = (AdminFragment)getFragmentManager().findFragmentByTag("admin");
         }
 
         Intent intent = new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
         IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
         IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
         ndefDetected.addCategory(Intent.CATEGORY_DEFAULT);
         tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
 		tagFilters = new IntentFilter[] { ndefDetected, tagDetected };
         mNfcAdapter.enableForegroundDispatch(this, pIntent, tagFilters, null);
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	Log.d("DEBUG", "MainActivity.onPause");
     	try {
     		unregisterReceiver(broadcastReceiver);
     	} catch (IllegalArgumentException e) {
     		// This is okay...
     	}
     	mNfcAdapter.disableForegroundDispatch(this);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         Log.d("DEBUG", "MainActivity.onSaveInstanceState");
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
     
     
 	///////////////////////////////////////////////////////////////////
 	// Action Bar events
 	///////////////////////////////////////////////////////////////////
     
     public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
         
     	private Fragment mFragment;
         private final Activity mActivity;
         private final String mTag;
         private final Class<T> mClass;
 
         public TabListener(Activity activity, String tag, Class<T> clz) {
             mActivity = activity;
             mTag = tag;
             mClass = clz;
         }
 
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
             // Check if the fragment is already initialized
             if (mFragment == null) {
                 // If not, instantiate and add it to the activity
                 mFragment = Fragment.instantiate(mActivity, mClass.getName());
                 ft.add(android.R.id.content, mFragment, mTag);
             } else {
                 // If it exists, simply attach it in order to show it
                 ft.attach(mFragment);
             }
         }
 
         public void onTabUnselected(Tab tab, FragmentTransaction ft) {
             if (mFragment != null) {
                 // Detach the fragment, because another one is being attached
                 ft.detach(mFragment);
             }
         }
 
         public void onTabReselected(Tab tab, FragmentTransaction ft) {
             // User selected the already selected tab. Usually do nothing.
 
         }
     }
     
     
 	///////////////////////////////////////////////////////////////////
 	// HomeFragment interface events
 	///////////////////////////////////////////////////////////////////
 
     @Override
 	public void onServiceSelected(String url) {
 		Log.d("DEBUG", url);
 		if (!url.equalsIgnoreCase(this.selectedUrl)) {
 			this.selectedUrl = url;
 			actionbar.selectTab(webServiceTab);
 		}
 	}
     
     @Override
 	public ArrayList<BreezehomeService> getServiceList() {
 		return this.serviceList;
 	}
     
     @Override
 	public void setServiceList(ArrayList<BreezehomeService> serviceList) {
     	this.serviceList = null;
     	this.serviceList = serviceList;
 	}
     
     @Override
 	public String getHelpText() {
 		return this.helpText;
 	}
 
     private void setHomeFragmentHelpText(String helpText) {
     	this.helpText = helpText;
     	homeFragment.setHelpText(helpText);
     }
     
 	///////////////////////////////////////////////////////////////////
 	// WebServiceFragment interface events
 	///////////////////////////////////////////////////////////////////
     
 	@Override
 	public String onGetUrl() {
 		return this.selectedUrl;
 	}
 	
 	@Override
 	public void onSetUrl(String url) {
 		this.selectedUrl = url;
 	}
 	
 	///////////////////////////////////////////////////////////////////
 	// AdminFragment interface events
 	///////////////////////////////////////////////////////////////////
 	
 	@Override
 	public void writeToTag(String tagText) {
 		this.writeMode = true;
 		this.tagText = tagText;
 		
 	}
 	
     
 	///////////////////////////////////////////////////////////////////
 	// NFC Scanner/Writer
 	///////////////////////////////////////////////////////////////////
     
     @Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		String intentAction = intent.getAction().toString();
 		Log.d("NFC", "MainActivity.onNewIntent - " + intentAction);
 		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
 			if (writeMode) {
 				writeTag(intent);
 			} else {
 				readTag(intent);
 			}
 		}
 	}
     
     private void readTag(Intent intent) {
     	Log.d("NFC", "readTag");
 		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		if (rawMsgs != null) {
 			NdefMessage[] messages = new NdefMessage[rawMsgs.length];
 			for (int i = 0; i < rawMsgs.length; i++) {
 				messages[i] = (NdefMessage) rawMsgs[i];
 			}
 			String str = new String(
 					messages[0].getRecords()[0].getPayload());
 			Log.d("NFC",str);
 			String[] nfcInfo = str.split(";");
 			boolean isAdmin = false;
 			if (nfcInfo.length == 5) {
 				Log.d("NFC","Scanned a service tag");
 				if (nfcInfo[4].equalsIgnoreCase("admin")) {
 					isAdmin = true;
 				}
 				setHomeFragmentHelpText("Select a service or scan a new tag");
 				HomeFragment homeFragment = (HomeFragment)getFragmentManager().findFragmentByTag("home");
 				homeFragment.addService(new BreezehomeService(nfcInfo[1], nfcInfo[2], nfcInfo[3], isAdmin, str));
 			} else if (nfcInfo.length == 7) {
 				Log.d("NFC","Scanned a auth/service tag");
 				breezehomeSSID = "\"" + nfcInfo[1] + "\"";
 				breezehomePass = "\"" + nfcInfo[2] + "\"";
 				if (nfcInfo[6].equalsIgnoreCase("admin")) {
 					isAdmin = true;
 				}
 				
 				breezehomeService = new BreezehomeService(nfcInfo[3], nfcInfo[4], nfcInfo[5], isAdmin, str);
 				wifiInfo = wifi.getConnectionInfo();
 		        
 		        if (wifiInfo != null) {
 		        	currentSSID = wifiInfo.getSSID();
 		        }
 				if (currentSSID.replace("\"", "").equalsIgnoreCase(breezehomeSSID.replace("\"", ""))) {
 					setHomeFragmentHelpText("Select a service or scan a new tag");
 				} else {
 					wifiAuth();
 				}
 			} else {
 				Toast.makeText(getApplicationContext(), "Invalid breezehome tag", Toast.LENGTH_LONG).show();
 			}
 			for (int i = 0; i < nfcInfo.length; i++) {
 				Log.d("NFC",nfcInfo[i]);
 			}
 		}
 	}
     
     private void writeTag(Intent intent) {
     	Log.d("NFC", "writeTag");
     	mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     	Log.d("NFC", "tag: " + mytag.toString());
     	try {
 			write(tagText, mytag);
 		} catch (IOException e) {
 			Toast.makeText(getApplicationContext(), "Tag writing was NOT successful!", Toast.LENGTH_LONG ).show();
 		} catch (FormatException e) {
 			Toast.makeText(getApplicationContext(), "Tag writing was NOT successful!", Toast.LENGTH_LONG ).show();
 		}
     	Toast.makeText(getApplicationContext(), "Tag writing successful!", Toast.LENGTH_LONG ).show();
     	writeMode = false;
     }
     
     private void write(String text, Tag tag) throws IOException, FormatException 
 	{
 
 		NdefRecord[] records = { createRecord(text) };
 		NdefMessage  message = new NdefMessage(records);
 		
 		// Get an instance of Ndef for the tag.
 		Ndef ndef = Ndef.get(tag);
 		// Enable I/O
 		ndef.connect();
 		// Write the message
 		ndef.writeNdefMessage(message);
 		// Close the connection
 		ndef.close();
 	}
     
     private NdefRecord createRecord(String text) throws UnsupportedEncodingException 
 	{
 		String lang       = "en";
 		byte[] textBytes  = text.getBytes();
 		byte[] langBytes  = lang.getBytes("US-ASCII");
 		int    langLength = langBytes.length;
 		int    textLength = textBytes.length;
 		byte[] payload    = new byte[1 + langLength + textLength];
 
 		// set status byte (see NDEF spec for actual bits)
 		payload[0] = (byte) langLength;
 
 		// copy langbytes and textbytes into payload
 		System.arraycopy(langBytes, 0, payload, 1,              langLength);
 		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
 
 		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);
 
 		return recordNFC;
 	}
     
     
 	///////////////////////////////////////////////////////////////////
 	// WiFi state events
 	///////////////////////////////////////////////////////////////////
     
  	public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
  	    
  		@Override
  	    public void onReceive(Context context, Intent intent) {
  	    	final String action = intent.getAction();
  	    	Log.d("DEBUG", "onReceive: " + action);
  	    	if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
  	    		SupplicantState stateInfo = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
  	    		Log.d("DEBUG", "onReceive [SUPPLICANT_STATE_CHANGED_ACTION] EXTRA_NEW_STATE = " + stateInfo.name());
  	    		if (stateInfo.name() == SupplicantState.DISCONNECTED.name()) {
  	    			disconnectOccurred = true;
  	    		} else if  (stateInfo.name() == SupplicantState.COMPLETED.name()) {
  	    			if (disconnectOccurred == true) {
  	    				disconnectOccurred = false;
  	    				Log.d("DEBUG", "Connected to access point");
  	    				setHomeFragmentHelpText("Select a service or scan a new tag");
 						homeFragment.addService(breezehomeService);
  	    			}
  	    		}
  	    	} 
  	    }
  	};
 
     private void wifiAuth() {
     	setHomeFragmentHelpText("Connecting ..");
     	WifiConfiguration wifiConf = (WifiConfiguration) new WifiConfiguration();
     	wifiConf.SSID = breezehomeSSID;
     	wifiConf.preSharedKey = breezehomePass;
     	wifiConf.hiddenSSID = false;
     	int netID = wifi.addNetwork(wifiConf);
     	wifi.enableNetwork(netID, true);
     	registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
     }
 
 	
 
 }
