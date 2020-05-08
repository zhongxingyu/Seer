 package com.example.nfcproject;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.provider.Settings;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 	private NfcAdapter mNfcAdapter;
     private IntentFilter[] mWriteTagFilters;
 	private PendingIntent mNfcPendingIntent;
 	private boolean isWrite = false;
 	private boolean writeProtect = false;
 	private Context context;
 	private EditText ssidField;
 	private TextView ssidText;
 	private EditText passField;
 	private TextView passText;
 	private CheckBox isHiddenButton;
 	private boolean isHidden;
 	private RadioGroup buttonGroup;
 	private RadioButton noneRadio;
 	private boolean isNone;
 	private RadioButton wepRadio;
 	private boolean isWEP;
 	private RadioButton wpaRadio;
 	private boolean isWPA;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		System.out.println("on create");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 		
 		context = getApplicationContext();
 		
 		Intent intent = new Intent(this,
 				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
 						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		
 		mNfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
         
         IntentFilter discovery=new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
         IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);        
         IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
 
         // Intent filters for writing to a tag
         mWriteTagFilters = new IntentFilter[] { ndefDetected, discovery, techDetected };
 		
 		ssidField = (EditText) findViewById(R.id.ssidField);
 		ssidText = (TextView) findViewById(R.id.ssidText);
 		passField = (EditText) findViewById(R.id.passField);
 		passField.setVisibility(View.INVISIBLE);
 		
 		passText = (TextView) findViewById(R.id.passText);
 		passText.setVisibility(View.INVISIBLE);
 		
 		isHiddenButton = (CheckBox) findViewById(R.id.checkBox1);
 		isHiddenButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
 				isHidden = isChecked;
 			}
 		});
 		
 		noneRadio = (RadioButton) findViewById(R.id.none);		
 		wepRadio = (RadioButton) findViewById(R.id.wep);		
 		wpaRadio = (RadioButton) findViewById(R.id.wpa);
 	
 		
 		buttonGroup = (RadioGroup) findViewById(R.id.radioGroup1);
 		buttonGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				isNone = noneRadio.isChecked();
 				isWEP = wepRadio.isChecked();
 				isWPA = wpaRadio.isChecked();
 				
 				if(noneRadio.isChecked()){
 					passField.setVisibility(View.INVISIBLE);
 					passText.setVisibility(View.INVISIBLE);					
 				}
 				if(wepRadio.isChecked()){
 					
 					passField.setVisibility(View.VISIBLE);
 					passText.setVisibility(View.VISIBLE);
 				}
 				if(wpaRadio.isChecked()){
 					passField.setVisibility(View.VISIBLE);
 					passText.setVisibility(View.VISIBLE);
 				}
 			}
 			
 		});
 		
 		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton1);
 		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				isWrite = isChecked;
 				if(isWrite){
 					ssidField.setVisibility(View.VISIBLE);
 					ssidText.setVisibility(View.VISIBLE);
 					passField.setVisibility(View.VISIBLE);
 					passText.setVisibility(View.VISIBLE);
 					buttonGroup.setVisibility(View.VISIBLE);
 					isHiddenButton.setVisibility(View.VISIBLE);
 				}
 				else{
 					ssidField.setVisibility(View.INVISIBLE);
 					ssidText.setVisibility(View.INVISIBLE);
 					passField.setVisibility(View.INVISIBLE);
 					passText.setVisibility(View.INVISIBLE);
 					buttonGroup.setVisibility(View.INVISIBLE);
 					isHiddenButton.setVisibility(View.INVISIBLE);
 				}
 				
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (mNfcAdapter != null) {
 			
 			if (!mNfcAdapter.isEnabled()) {
 				LayoutInflater inflater = getLayoutInflater();
 				View dialoglayout = inflater.inflate(R.layout.activity_main,
 						(ViewGroup) findViewById(R.id.nfc_settings_layout));
 				new AlertDialog.Builder(this)
 						.setView(dialoglayout)
 						.setPositiveButton("Update Settings",
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface arg0,
 											int arg1) {
 										Intent setnfc = new Intent(
 												Settings.ACTION_WIRELESS_SETTINGS);
 										startActivity(setnfc);
 									}
 								})
 						.setOnCancelListener(
 								new DialogInterface.OnCancelListener() {
 									public void onCancel(DialogInterface dialog) {
 										System.out.println("cancel");
 										finish(); // exit application if user
 													// cancels
 									}
 								}).create().show();
 			}
 			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
 					mWriteTagFilters, null);
 		} else {
 			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
 					mWriteTagFilters, null);
 			Toast.makeText(context,
 					"Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (mNfcAdapter != null)
 			mNfcAdapter.disableForegroundDispatch(this);
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 
 		if(isWrite){
 			if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
 	        	// validate that this tag can be written....
 				Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 
 				// check if tag is writable (to the extent that we can
 				if(writableTag(detectedTag)) {
 					//writeTag here
 					WriteResponse wr = writeTag(getTagAsNdef(), detectedTag);
 					String message = (wr.getStatus() == 1? "Success: " : "Failed: ") + wr.getMessage();
 					Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(context,"This tag is not writable",Toast.LENGTH_SHORT).show();
 
 				}	            
 	        }
 		}
 		else{
 			if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
 
 				NdefMessage[] messages = null;
 				Parcelable[] rawMsgs = intent
 						.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 				if (rawMsgs != null) {
 					messages = new NdefMessage[rawMsgs.length];
 					for (int i = 0; i < rawMsgs.length; i++) {
 						messages[i] = (NdefMessage) rawMsgs[i];
 					}
 				}
 				if (messages[0] != null) {
 					String result = "";
 					byte[] payload = messages[0].getRecords()[0].getPayload();
 					// this assumes that we get back am SOH followed by host/code
 					for (int b = 0; b < payload.length; b++) { // skip SOH
 						result += (char) payload[b];
 					}
 
 					//Split result by ":" for "ssid:password"
					String ssid = "";
 					String password = "";
					if(result.contains(":")){
						ssid = result.split(":")[0];

						if(result.split(":").length > 0)
							password = result.split(":")[1];
					}
					else
						ssid = result;
 					
 					WifiConfiguration conf = new WifiConfiguration();
 					conf.SSID = "\"" + ssid + "\"";
 					conf.status = WifiConfiguration.Status.ENABLED;
 
 					
					if(isNone || passField.getText().toString().isEmpty()) {
 						//For open networks
 						conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 						conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 					}
 					else if(isWEP || isWPA) {
 						conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
 						conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
 						conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
 						conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
 						conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
 						conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
 						
 						if(isWEP){		
 							//For WEP networks							
 							conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 							conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 							conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
 							conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
 							conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
 
 							conf.wepKeys[0] = "\"".concat(password).concat("\"");
 							conf.wepTxKeyIndex = 0;
 						}
 						else if(isWPA){
 							//For WPA and WPA2 networks
 							conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
 							conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
 							conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
 
 							conf.preSharedKey = "\"" + password + "\"";
 						}						
 					}
 					
 					
 					//For hidden networks
 					if(isHidden)
 						conf.hiddenSSID = true;
 
 					WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
 					int networkId = wifiManager.addNetwork(conf);
 
 					wifiManager.enableNetwork(networkId, true);
 					wifiManager.reconnect();
 
 					Toast.makeText(context,"Tag Contains " + result, Toast.LENGTH_SHORT).show();
 
 				}
 			}
 
 		}}
 	
 	public WriteResponse writeTag(NdefMessage message, Tag tag) {
         int size = message.toByteArray().length;
         String mess = "";
 
         try {
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 ndef.connect();
 
                 if (!ndef.isWritable()) {
                     return new WriteResponse(0,"Tag is read-only");
 
                 }
                 if (ndef.getMaxSize() < size) {
                     mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                             + " bytes.";
                     return new WriteResponse(0,mess);
                 }
 
                 ndef.writeNdefMessage(message);
                 if(writeProtect)  ndef.makeReadOnly();
                 mess = "Wrote message to pre-formatted tag.";
                 return new WriteResponse(1,mess);
             } else {
                 NdefFormatable format = NdefFormatable.get(tag);
                 if (format != null) {
                     try {
                         format.connect();
                         format.format(message);
                         mess = "Formatted tag and wrote message";
                         return new WriteResponse(1,mess);
                     } catch (IOException e) {
                         mess = "Failed to format tag.";
                         return new WriteResponse(0,mess);
                     }
                 } else {
                     mess = "Tag doesn't support NDEF.";
                     return new WriteResponse(0,mess);
                 }
             }
         } catch (Exception e) {
             mess = "Failed to write tag";
             return new WriteResponse(0,mess);
         }
     }
     
     private class WriteResponse {
     	int status;
     	String message;
     	WriteResponse(int Status, String Message) {
     		this.status = Status;
     		this.message = Message;
     	}
     	public int getStatus() {
     		return status;
     	}
     	public String getMessage() {
     		return message;
     	}
     }
     
 	public static boolean supportedTechs(String[] techs) {
 	    boolean ultralight=false;
 	    boolean nfcA=false;
 	    boolean ndef=false;
 	    for(String tech:techs) {
 	    	if(tech.equals("android.nfc.tech.MifareUltralight")) {
 	    		ultralight=true;
 	    	}else if(tech.equals("android.nfc.tech.NfcA")) { 
 	    		nfcA=true;
 	    	} else if(tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable")) {
 	    		ndef=true;
 	   		
 	    	}
 	    }
         if(ultralight && nfcA && ndef) {
         	return true;
         } else {
         	return false;
         }
 	}
 	
     private boolean writableTag(Tag tag) {
 
         try {
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 ndef.connect();
                 if (!ndef.isWritable()) {
                     Toast.makeText(context,"Tag is read-only.",Toast.LENGTH_SHORT).show();
                     ndef.close(); 
                     return false;
                 }
                 ndef.close();
                 return true;
             } 
         } catch (Exception e) {
             Toast.makeText(context,"Failed to read tag",Toast.LENGTH_SHORT).show();
         }
 
         return false;
     }
     
     private NdefMessage getTagAsNdef() {
    	
		String uniqueId = ssidField.getText().toString();
		
		if(!passField.getText().toString().isEmpty()){
			uniqueId = uniqueId + ":" + passField.getText().toString();
		}
 
 		byte[] SSID = uniqueId.getBytes(Charset.forName("US-ASCII"));
 		byte[] payload = new byte[SSID.length + 1];
 		payload[0] = 0x01;
 
		NdefRecord rtdTextRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
 				NdefRecord.RTD_TEXT, new byte[0], SSID);
 
		return new NdefMessage(new NdefRecord[] { rtdTextRecord });
 
 	}
  
 }
