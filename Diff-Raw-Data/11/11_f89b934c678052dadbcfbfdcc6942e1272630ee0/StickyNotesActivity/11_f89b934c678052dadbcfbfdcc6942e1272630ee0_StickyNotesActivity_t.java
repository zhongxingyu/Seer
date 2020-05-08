 /*
  * Copyright 2011, The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package android.stickynotes;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiConfiguration.AuthAlgorithm;
 import android.net.wifi.WifiConfiguration.GroupCipher;
 import android.net.wifi.WifiConfiguration.KeyMgmt;
 import android.net.wifi.WifiConfiguration.PairwiseCipher;
 import android.net.wifi.WifiConfiguration.Protocol;
 import android.net.wifi.WifiManager;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.io.IOException;
 import java.util.List;
 
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.Tweet;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class StickyNotesActivity extends Activity {
 	static final int PICK_VENUE_RESULT = 0;
 	
     private static final String TAG = "stickynotes";	//for logging
     private final String MY_USERNAME = "username";
     private SharedPreferences myPrefs;
     
     private boolean mResumed = false;
     private boolean mWriteMode = false;
     NfcAdapter mNfcAdapter;
     EditText mNote;
 	boolean wifiMode;
 	int wifiConfigIndex;
     WifiManager wifi;
 	TextView textStatus;
 	
 	String TwitAccount;
 	String TwitUser;
 	boolean twitterMode;
 
 	String fbID;
 	String fbUser;
 	
     PendingIntent mNfcPendingIntent;
     IntentFilter[] mWriteTagFilters;
     IntentFilter[] mNdefExchangeFilters;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         myPrefs = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
         setContentView(R.layout.mainscreen);
         
         if (mNfcAdapter == null)
         	toast("No NFC support!");
         
         wifiMode = false;
         wifiConfigIndex = -1; // First click of Wifi Button sets this to 0...Yeah poor style but w/e
         wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         
         textStatus = (TextView) findViewById(R.id.text_view);
         
 		textStatus.setText("");
 		textStatus.append("Choose your mode or write a message!");
         mNote = ((EditText) findViewById(R.id.note));
 		
         TwitAccount = ""; //this will set your saved Twitter account to nothing when started
         twitterMode = false;
 	
         fbID = "";
         
         if (mNfcAdapter != null) //device NFC capable
         {
 	        mNote.addTextChangedListener(mTextWatcher);
 	        
 			// Handle all of our received NFC intents in this activity.
 	        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
 	                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 	
 	        // Intent filters for reading a note from a tag or exchanging over p2p.
 	        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
 	        try {
 	            ndefDetected.addDataType("text/plain");
 	        } catch (MalformedMimeTypeException e) { }
 	        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };
 	
 	        // Intent filters for writing to a tag
 	        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
 	        mWriteTagFilters = new IntentFilter[] { tagDetected };
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mResumed = true;
         if (mNfcAdapter != null) { //device NFC capable
 	        // Sticky notes received from Android
 	        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 	            NdefMessage[] messages = getNdefMessages(getIntent());
 	            byte[] payload = messages[0].getRecords()[0].getPayload();
 	            setNoteBody(new String(payload));
 	            setIntent(new Intent()); // Consume this intent.
 	        }
 	        enableNdefExchangeMode();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mResumed = false;
         if (mNfcAdapter != null) { //device NFC capable
         	mNfcAdapter.disableForegroundNdefPush(this);
         }
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         // NDEF exchange mode
         if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
             NdefMessage[] msgs = getNdefMessages(intent);
             promptForContent(msgs[0]);
         }
 
         // Tag writing mode
         if (mWriteMode && !twitterMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) 
         {
             Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             //TODO: Decide button context handling scheme
             //We need some kind of context switch here or inside getNoteAsNdef
             //As it is, what is written in the note gets packed into the NDEF payload. 
             // Ideally, if we're passing facebook/twitter/4square/wifi info across, we dont
             //		want that stuff visible to the user and inside the note. 
             writeTag(getNoteAsNdef(), detectedTag);
         } if (mWriteMode && twitterMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) 
         {
             Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             writeTag(getNoteAsNdef(TwitAccount), detectedTag);
         }
     }
 
     private TextWatcher mTextWatcher = new TextWatcher() {
 
         @Override
         public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 
         }
 
         @Override
         public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 
         }
 
         @Override
         public void afterTextChanged(Editable arg0) {
             if (mResumed) {
                 mNfcAdapter.enableForegroundNdefPush(StickyNotesActivity.this, getNoteAsNdef());
             }
         }
     };
     
     private void openFb(String user)
     {
     	//checks if Facebook application is installed
     	PackageManager pm = getPackageManager();
     	boolean installed = false;
     	try {
     		pm.getPackageInfo("com.facebook.katana", PackageManager.GET_ACTIVITIES);
     		installed = true;
     	} catch (PackageManager.NameNotFoundException e) {
     		 installed = false;
     	}
     	
     	if (installed) {
 	    	//opens user profile in fb application
 	    	Intent intent = new Intent(Intent.ACTION_VIEW);
 	    	intent.setClassName("com.facebook.katana", "com.facebook.katana.ProfileTabHostActivity");
 	    	intent.putExtra("extra_user_id", user);
 	    	this.startActivity(intent);
     	}
     	else {
     		toast("FAILED: The Facebook application must be installed.");
     	}
     }
     
    private void getTweets(String twit)
     {
     	wifi.setWifiEnabled(true);
     	ConfigurationBuilder cb = new ConfigurationBuilder();
     	cb.setDebugEnabled(true)
     	  .setOAuthConsumerKey("TvywVhWx7r7QQev2UGfA4g")
     	  .setOAuthConsumerSecret("Nv22zsyf1VS0vvi6hwAMyvJk9LUtSXwRUB4xwp2gRs");
     	TwitterFactory tf = new TwitterFactory(cb.build());
     	Twitter twitter = tf.getInstance();
         try {
             QueryResult result = twitter.search(new Query(twit));
             List<Tweet> tweets = result.getTweets();
             textStatus.setText("");
            	textStatus.append("Recent tweets about '"+twit+"':\n");
             for (Tweet tweet : tweets) {
                textStatus.append("@" + tweet.getFromUser() + " - " + tweet.getText() +"\n\n");
             }
         } catch (TwitterException te) {
             te.printStackTrace();
            textStatus.append("Failed to search tweets: " + te.getMessage() + " " + twit);
         }
     }
     
    private void promptForContent(final NdefMessage msg) {
         new AlertDialog.Builder(this).setTitle("Replace current content?")
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface arg0, int arg1) {
                     String body = new String(msg.getRecords()[0].getPayload());
                     detectWifi(body);
                     if(body.contains("twituser=") && twitterMode)
                     {
                     	String twitUse = body.substring(9);
                     	getTweets(twitUse);
                     }
                     if(body.contains("fbuser="))
                     {
                     	String fbUse = body.substring(7);
                     	openFb(fbUse);
                     }
                     if(!twitterMode && !body.contains("fbuser="))	
                     	setNoteBody(body);
                 }
             })
             .setNegativeButton("No", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface arg0, int arg1) {
                     
                 }
             }).show();
     }
 
     private void setNoteBody(String body) {
         Editable text = mNote.getText();
         text.clear();
         text.append(body);
     }
 
     private NdefMessage getNoteAsNdef() 
     {
     	byte[] textBytes = mNote.getText().toString().getBytes();
     	if(wifiMode)
     	{
     		String temp = mNote.getText().toString() + "\n" + wifi.getConfiguredNetworks().get(wifiConfigIndex).toString();
     		textBytes = temp.getBytes();
     		//textStatus.setText("Following NDEF created:\n" + temp);
     	}
         NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                 new byte[] {}, textBytes);
         return new NdefMessage(new NdefRecord[] {
             textRecord
         });
     }
      private NdefMessage getNoteAsNdef(String message) {
         byte[] textBytes = message.getBytes();
         NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                 new byte[] {}, textBytes);
         return new NdefMessage(new NdefRecord[] {
             textRecord
         });
     }
 
     NdefMessage[] getNdefMessages(Intent intent) {
         // Parse the intent
         NdefMessage[] msgs = null;
         String action = intent.getAction();
         if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                 || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
             Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
             if (rawMsgs != null) {
                 msgs = new NdefMessage[rawMsgs.length];
                 for (int i = 0; i < rawMsgs.length; i++) {
                     msgs[i] = (NdefMessage) rawMsgs[i];
                 }
             } else {
                 // Unknown tag type
                 byte[] empty = new byte[] {};
                 NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                 NdefMessage msg = new NdefMessage(new NdefRecord[] {
                     record
                 });
                 msgs = new NdefMessage[] {
                     msg
                 };
             }
         } else {
             Log.d(TAG, "Unknown intent.");
             finish();
         }
         return msgs;
     }
 
     @Override
 	public void onStop() {
     	super.onStop();
 	}
     
     private void enableNdefExchangeMode() {
         mNfcAdapter.enableForegroundNdefPush(StickyNotesActivity.this, getNoteAsNdef());
         mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
     }
     private void enableNdefExchangeMode(String message) {
         mNfcAdapter.enableForegroundNdefPush(StickyNotesActivity.this, getNoteAsNdef(message));
         mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
     }
     private void disableNdefExchangeMode() {
         mNfcAdapter.disableForegroundNdefPush(this);
         mNfcAdapter.disableForegroundDispatch(this);
     }
 
     private void enableTagWriteMode() {
         mWriteMode = true;
         IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
         mWriteTagFilters = new IntentFilter[] {
             tagDetected
         };
         mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
     }
 
     private void disableTagWriteMode() {
         mWriteMode = false;
         mNfcAdapter.disableForegroundDispatch(this);
     }
 
     boolean writeTag(NdefMessage message, Tag tag) {
         int size = message.toByteArray().length;
 
         try {
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 ndef.connect();
 
                 if (!ndef.isWritable()) {
                     toast("Tag is read-only.");
                     return false;
                 }
                 if (ndef.getMaxSize() < size) {
                     toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                             + " bytes.");
                     return false;
                 }
 
                 ndef.writeNdefMessage(message);
                 toast("Wrote message to pre-formatted tag.");
                 return true;
             } else {
                 NdefFormatable format = NdefFormatable.get(tag);
                 if (format != null) {
                     try {
                         format.connect();
                         format.format(message);
                         toast("Formatted tag and wrote message");
                         return true;
                     } catch (IOException e) {
                         toast("Failed to format tag.");
                         return false;
                     }
                 } else {
                     toast("Tag doesn't support NDEF.");
                     return false;
                 }
             }
         } catch (Exception e) {
             toast("Failed to write tag");
         }
 
         return false;
     }
 
     void readWepConfig()
     { 
         //WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
         List<WifiConfiguration> item = wifi.getConfiguredNetworks();
         int i = item.size();
         Log.d("WifiPreference", "NO OF CONFIG " + i );
         for(WifiConfiguration config : item)
         {
         textStatus.append( "SSID" + config.SSID + "\n");
         textStatus.append(" PASSWORD" + config.preSharedKey + "\n");
         Log.d("WifiPreference", "ALLOWED ALGORITHMS");
         Log.d("WifiPreference", "LEAP" + config.allowedAuthAlgorithms.get(AuthAlgorithm.LEAP));
         Log.d("WifiPreference", "OPEN" + config.allowedAuthAlgorithms.get(AuthAlgorithm.OPEN));
         Log.d("WifiPreference", "SHARED" + config.allowedAuthAlgorithms.get(AuthAlgorithm.SHARED));
         Log.d("WifiPreference", "GROUP CIPHERS");
         Log.d("WifiPreference", "CCMP" + config.allowedGroupCiphers.get(GroupCipher.CCMP));
         Log.d("WifiPreference", "TKIP" + config.allowedGroupCiphers.get(GroupCipher.TKIP));
         Log.d("<WifiPreference", "WEP104" + config.allowedGroupCiphers.get(GroupCipher.WEP104));
         Log.d("WifiPreference", "WEP40" + config.allowedGroupCiphers.get(GroupCipher.WEP40));
         Log.d("WifiPreference", "KEYMGMT");
         Log.d("WifiPreference", "IEEE8021X" + config.allowedKeyManagement.get(KeyMgmt.IEEE8021X));
         Log.d("WifiPreference", "NONE" + config.allowedKeyManagement.get(KeyMgmt.NONE));
         Log.d("WifiPreference", "WPA_EAP" + config.allowedKeyManagement.get(KeyMgmt.WPA_EAP));
         Log.d("WifiPreference", "WPA_PSK" + config.allowedKeyManagement.get(KeyMgmt.WPA_PSK));
         Log.d("WifiPreference", "PairWiseCipher");
         Log.d("WifiPreference", "CCMP" + config.allowedPairwiseCiphers.get(PairwiseCipher.CCMP));
         Log.d("WifiPreference", "NONE" + config.allowedPairwiseCiphers.get(PairwiseCipher.NONE));
         Log.d("WifiPreference", "TKIP" + config.allowedPairwiseCiphers.get(PairwiseCipher.TKIP));
         Log.d("WifiPreference", "Protocols");
         Log.d("WifiPreference", "RSN" + config.allowedProtocols.get(Protocol.RSN));
         Log.d("WifiPreference", "WPA" + config.allowedProtocols.get(Protocol.WPA));
         Log.d("WifiPreference", "WEP Key Strings");
         String[] wepKeys = config.wepKeys;
         textStatus.append( "   WEP KEY 0" + wepKeys[0] + "\n");
         textStatus.append( "   WEP KEY 1" + wepKeys[1] + "\n");
         textStatus.append( "   WEP KEY 2" + wepKeys[2] + "\n");
         textStatus.append( "   WEP KEY 3" + wepKeys[3] + "\n");
         }
     }
     
     int saveWepConfig(String input)
     {
         WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         WifiConfiguration wc = new WifiConfiguration(); 
         String text = input;
         if(text.indexOf("SSID:") == -1 || text.indexOf("BSSID:") == -1 || text.indexOf("PRIO:") == -1 || text.indexOf("Protocols:") == -1 || text.indexOf("\n AuthAlg") == -1 || text.indexOf("\n Pairwise") == -1 || text.indexOf("\n Group") == -1 )
         {	
         	toast( "SSID: " + (text.indexOf("SSID:") == -1) + "\nBSSID: "+ (text.indexOf("BSSID:") == -1) + "\nprio: " + (text.indexOf("PRIO:") == -1) + "\nproto: " + (text.indexOf("Protocols:") == -1) + "\nauth: "+ (text.indexOf("\n AuthAlg") == -1) + "\npair: "+ (text.indexOf("\n Pairwise") == -1) +"\ngroup: "+ (text.indexOf("\n Group") == -1 ));
         	return 0;
         }
         
         
         String password = text.substring(0, text.indexOf("\n"));
         textStatus.append("pass::"+ password +":");
         text = text.substring(text.indexOf("\n")+1);
         
         String ssid = text.substring(text.indexOf("SSID:") + 6, text.indexOf("BSSID:") - 1);
         wc.SSID = ssid;
         textStatus.append(ssid + "\n");
         String bssid = text.substring(text.indexOf("BSSID:") + 7, text.indexOf("PRIO:") - 1);
        // if(bssid.equals("null"))
         //	wc.BSSID = "";
         //else
         	//wc.BSSID = bssid;
         textStatus.append(bssid + "\n");
         
         int prio = Integer.parseInt(text.substring(text.indexOf("PRIO:") + 6, text.indexOf("\n")));
         wc.priority = prio;
         text = text.substring(text.indexOf("\n")+1);
         textStatus.append("" + prio + "\n");
         String keymgmt = text.substring(0, text.indexOf("Protocols:"));
         if(keymgmt.contains("NONE"))
         	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
         else if(keymgmt.contains("WPA"))
         	wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
         textStatus.append(keymgmt + "\n");
         
         String protocols = text.substring(text.indexOf("Protocols: "), text.indexOf("\n Auth"));
         if(protocols.contains("WPA"))
         	wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         if(protocols.contains("RSN"))
         	wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         textStatus.append(protocols + "\n");
         
         text = text.substring(text.indexOf("\n Auth")+1);
         String authalg = text.substring(0,text.indexOf("\n Pairwise"));
         
         if(authalg.contains("OPEN"))
         	wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
         if(authalg.contains("SHARED"))
         	wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
         
         textStatus.append(authalg + "\n");
         
         text = text.substring(text.indexOf("\n Pairwise")+1);
         String pairwise = text.substring(0,text.indexOf("\n Group"));
         textStatus.append(pairwise + "\n");
         
         if(pairwise.contains("CCMP"))
         	wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         if(pairwise.contains("TKIP"))
             wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         
         text = text.substring(text.indexOf("\n Group")+1);
         String groupcipher = text.substring(0,text.indexOf("\n PSK"));
         textStatus.append(groupcipher + "\n");
         
         if(groupcipher.contains("WEP40"))
         	 wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
         if(groupcipher.contains("WEP104"))
         	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
         if(groupcipher.contains("CCMP"))
         	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
         if(groupcipher.contains("TKIP"))
         	wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
         wc.hiddenSSID = false;
         wc.status = WifiConfiguration.Status.DISABLED;     
 
         wc.wepKeys[0] = password; //This is the WEP Password
         wc.wepTxKeyIndex = 0;
         
         textStatus.append(wc.toString());
         
 
         //WifiManager  wifiManag = (WifiManager) this.getSystemService(WIFI_SERVICE);
         boolean res1 = wifi.setWifiEnabled(true);
         int res = wifi.addNetwork(wc);
         textStatus.append("add Network returned " + res + "enabled: " + res1 + "\n");
         boolean es = wifi.saveConfiguration();
         toast("saveConfiguration returned " + es +"\n");
         boolean b = wifi.enableNetwork(res, true);   
         toast("enableNetwork returned " + b +"\n");  
         return 1; // Success
         
     }
     
     private void toast(String text) {
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
     }
     
     /**implementing buttons*/
 	public void onFacebookClicked(View view)
 	{  //TODO complete
 		wifiMode = false;
 		twitterMode = false;
 		textStatus.setText("Choose your mode or write a message!");
 		setNoteBody("");
 
 		fbUser = myPrefs.getString(MY_USERNAME, "username");
 		
 		final FrameLayout fl = new FrameLayout(this);
 		final EditText input = new EditText(this); 
 		input.setGravity(Gravity.CENTER);
 		fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 		input.setText("");
 		
 		if(fbUser.equals("username"))
         {
 			new AlertDialog.Builder(StickyNotesActivity.this) 
 	        .setView(fl)			 
 	        .setTitle("Please enter facebook id: ")			 
 	        .setPositiveButton("OK", new DialogInterface.OnClickListener(){			 
 	                @Override			 
 	                public void onClick(DialogInterface d, int which) {			 
 	                        d.dismiss();
 	                        fbUser = input.getText().toString();
 	                        
 	                        //store user information
 	       				 	SharedPreferences.Editor prefsEditor = myPrefs.edit();
 	       				 	prefsEditor.putString(MY_USERNAME, fbUser);
 	       				 	prefsEditor.commit();
 	       			     
 	                        fbID = "fbuser=" + fbUser;
 
 	                        disableNdefExchangeMode();
 	                		enableNdefExchangeMode(fbID);
 	                		new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Touch phones to friend")
 	                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
 	                            @Override
 	                            public void onCancel(DialogInterface dialog) {
 	                            	disableNdefExchangeMode();
 	                                enableNdefExchangeMode();
 	                            }
 	                        }).create().show();
 	                }
 	        })
 	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
 	                @Override
 	                public void onClick(DialogInterface d, int which) {
 	                        d.dismiss();
 	                }
 	        }).create().show();
 		}
 		else	//username previously saved
 		{
 			new AlertDialog.Builder(StickyNotesActivity.this) 
 	        .setTitle("Use "+fbUser+"?")			 
 	        .setPositiveButton("Yes", new DialogInterface.OnClickListener(){			 
 	                @Override			 
 	                public void onClick(DialogInterface d, int which) {			 
 	                        d.dismiss();
 	                        disableNdefExchangeMode();
 	                		enableNdefExchangeMode(fbID);
 	                		new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Touch phones to friend")
 	                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
 	                            @Override
 	                            public void onCancel(DialogInterface dialog) {
 	                            	disableNdefExchangeMode();
 	                                enableNdefExchangeMode();
 	                            }
 	                        }).create().show();
 	                }
 	        })
 	        .setNegativeButton("No", new DialogInterface.OnClickListener(){
 	                @Override
 	                public void onClick(DialogInterface d, int which) {
 	                        d.dismiss();
 	                        new AlertDialog.Builder(StickyNotesActivity.this) 
 	            	        .setView(fl)			 
 	            	        .setTitle("Please enter facebook id: ")			 
 	            	        .setPositiveButton("OK", new DialogInterface.OnClickListener(){			 
 	            	                @Override			 
 	            	                public void onClick(DialogInterface d, int which) {			 
 	            	                        d.dismiss();
 	            	                        fbUser = input.getText().toString();
 	            	                        
 	            	                        //store user information
 	            	       				 	SharedPreferences.Editor prefsEditor = myPrefs.edit();
 	            	       				 	prefsEditor.putString(MY_USERNAME, fbUser);
 	            	       				 	prefsEditor.commit();
 	            	       			     
 	            	                        fbID = "fbuser=" + fbUser;
 
 	            	                        disableNdefExchangeMode();
 	            	                		enableNdefExchangeMode(fbID);
 	            	                		new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Touch phones to friend")
 	            	                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
 	            	                            @Override
 	            	                            public void onCancel(DialogInterface dialog) {
 	            	                            	disableNdefExchangeMode();
 	            	                                enableNdefExchangeMode();
 	            	                            }
 	            	                        }).create().show();
 	            	                }
 	            	        })
 	            	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
 	            	                @Override
 	            	                public void onClick(DialogInterface d, int which) {
 	            	                        d.dismiss();
 	            	                }
 	            	        }).create().show();
 	                }
 	        }).create().show();
 		}
 	}  
 	
 	public void onTwitterClicked(View view)
 	{  //TODO complete
 		//toast("twitter");
 		twitterMode = true;
 		wifiMode = false; // This is only temporary until we decide a context handling thingy
 		textStatus.setText("Choose your mode or write a message!");
 		setNoteBody("");
 		
 		if(TwitAccount == "")
 		{
 			//textStatus.setText("Twitter account name not stored!\n");
 			final FrameLayout fl = new FrameLayout(this);
 			final EditText input = new EditText(this); 
 			input.setGravity(Gravity.CENTER);
 			fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 			input.setText("");
 			new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Please choose an option: ")
             .setPositiveButton("Read", new DialogInterface.OnClickListener(){			 
 			                @Override			 
 			                public void onClick(DialogInterface d, int which) {			 
 			                        d.dismiss();
 			                        //TwitUser = input.getText().toString();
 			                        //TwitAccount = "twituser=" + TwitUser;
 			                        //enableNdefExchangeMode(TwitAccount);
 			            			//setNoteBody(TwitAccount);
 			                }
 			        }).setNegativeButton("Write", new DialogInterface.OnClickListener(){
 		                @Override
 		                public void onClick(DialogInterface d, int which) {
 		                        d.dismiss();
 		               // }
 			        //}).create().show();
 			new AlertDialog.Builder(StickyNotesActivity.this) 
 			        .setView(fl)			 
 			        .setTitle("Please enter keyword: ")			 
 			        .setPositiveButton("OK", new DialogInterface.OnClickListener(){			 
 			                @Override			 
 			                public void onClick(DialogInterface d, int which) {			 
 			                        d.dismiss();
 			                        TwitUser = input.getText().toString();
 			                        TwitAccount = "twituser=" + TwitUser;
 			                        d.dismiss();
 			                        disableNdefExchangeMode();
 			                        enableTagWriteMode();
 			                		//enableNdefExchangeMode(TwitAccount);
 			                		new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Touch tag to write")
 			                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
 			                            @Override
 			                            public void onCancel(DialogInterface dialog) {
 			                                disableTagWriteMode();
 			                                enableNdefExchangeMode();
 			                            }
 			                        }).create().show();
 
 			                        
 			                        //enableNdefExchangeMode(TwitAccount);
 			            			//setNoteBody(TwitAccount);
 			                }
 			        })
 			        .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
 			                @Override
 			                public void onClick(DialogInterface d, int which) {
 			                        d.dismiss();
 			                }
 			        }).create().show();
 		                }
 			        }).create().show();
 		}
 		else{
 			//toast(TwitAccount);
 			final FrameLayout fl = new FrameLayout(this);
 			final EditText input = new EditText(this); 
 			input.setGravity(Gravity.CENTER);
 			fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
 			input.setText("");
 			new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Please choose an option: ")
             .setPositiveButton("Read", new DialogInterface.OnClickListener(){			 
 			                @Override			 
 			                public void onClick(DialogInterface d, int which) {			 
 			                        d.dismiss();
 			                        //TwitUser = input.getText().toString();
 			                        //TwitAccount = "twituser=" + TwitUser;
 			                        //enableNdefExchangeMode(TwitAccount);
 			            			//setNoteBody(TwitAccount);
 			                }
 			        }).setNegativeButton("Write", new DialogInterface.OnClickListener(){
 		                @Override
 		                public void onClick(DialogInterface d, int which) {
 		                        d.dismiss();
 		               // }
 			        //}).create().show();
 			new AlertDialog.Builder(StickyNotesActivity.this) 
 			        .setView(fl)			 
 			        .setTitle("Use " + TwitUser + " as keyword?")			 
 			        .setNegativeButton("Change Keyword", new DialogInterface.OnClickListener(){			 
 			                @Override			 
 			                public void onClick(DialogInterface d, int which) {			 
 			                        d.dismiss();
 			                        TwitUser = "";
 			                        //if(TwitUser!="Abhi")
 			                        TwitAccount = "twituser=" + TwitUser;
 			                        enableNdefExchangeMode(TwitAccount);
 				            		//setNoteBody(TwitAccount);
 			                        //else{
 			                        	//TwitAccount = "Abhi sucks";
 			                       //}
 			                }
 			        })
 			        .setPositiveButton("Write to Tag", new DialogInterface.OnClickListener(){
 			                @Override
 			                public void onClick(DialogInterface d, int which) {
 			                        d.dismiss();
 			                        disableNdefExchangeMode();
 			                        enableTagWriteMode();
 			                		//enableNdefExchangeMode(TwitAccount);
 			                		new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Touch tag to write")
 			                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
 			                            @Override
 			                            public void onCancel(DialogInterface dialog) {
 			                                disableTagWriteMode();
 			                                enableNdefExchangeMode();
 			                            }
 			                        }).create().show();
 			            			//setNoteBody(TwitAccount);
 			                }
 			        }).create().show();
 			  }
 	        }).create().show();
 		}
 		 
 	} 
 	
 	private static String convertStreamToString(InputStream is) {
         /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
  
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return sb.toString();
     }
 	
 	public void onFoursquareClicked(View view)
 	{  //TODO complete
 		twitterMode = false;
 		wifiMode = false; // This is only temporary until we decide a context handling thingy
 		textStatus.setText("Choose your mode or write a message!");
 		setNoteBody("");
 		startActivityForResult(new Intent(this, FoursqActivity.class), PICK_VENUE_RESULT);
 
 		
 		
 //		String clientID = "J5PQXMEG2EBIBL0ZCD5NDKWIXAMEGDKIMNGDBBG5FPGJEP55";
 //		String clientSecret = "CH3I40KL4DLDG5DDPV0H0DMURIC3ABZFSAV5GQK5AAMXM143";
 //		//^ This is very secure
 //
 //		HttpClient client = new DefaultHttpClient();
 //		
 //		String url = "https://api.foursquare.com/v2/venues/search?ll=34,-118&client_id=J5PQXMEG2EBIBL0ZCD5NDKWIXAMEGDKIMNGDBBG5FPGJEP55&client_secret=CH3I40KL4DLDG5DDPV0H0DMURIC3ABZFSAV5GQK5AAMXM143&v=20120222&radius=400";
 //		//HttpGet httpGet = new HttpGet(url);
 //		
 //
 //		String returnString = null;
 //		
 //		try {
 //			toast("hello");
 //			InputStream content = null;
 //			HttpResponse response = client.execute(new HttpGet(url));
 //			content = response.getEntity().getContent();
 //
 //				String test = convertStreamToString(content);
 //				try {
 //					JSONObject obj = new JSONObject(test);
 //					JSONObject response1 = obj.getJSONObject("response");
 //					JSONArray venues = response1.getJSONArray("venues");
 //					String[] names = new String[venues.length()];
 //					
 //					for (int i = 0; i < venues.length(); i++)
 //					{
 //						JSONObject temp = venues.getJSONObject(i);
 //						String tempStr = temp.getString("name");
 //						names[i] = tempStr;
 ////						text += tempStr + "\n";
 //					}
 //					
 //					ListView listview = (ListView) findViewById(R.id.mylist);
 //					ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, android.R.id.text1, names);   
 //					listview.setAdapter(adapter);
 //				}
 //				catch(JSONException e)
 //				{
 //					toast(e.toString());
 //				}
 //			
 //
 //		}
 //		catch (IOException e)
 //		{
 //			toast("error," + e);
 //		}
 //		
 //		//MyLocation myLocation = new MyLocation();
 //		//LocationResult locationResult = null;
 //		//myLocation.getLocation(this, locationResult);
 //		
 //
 //		toast("foursquare");
 	} 
 	
 	 protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
     	toast("outside if");
             if (resultCode == RESULT_OK) {
                 // A contact was picked.  Here we will just display it
                 // to the user.
             	String result = data.getStringExtra("fsqstring");
                 toast(result);
                 setNoteBody(result);
             }
         
     }
 	
 	public void onWifiClicked(View view)
 	{  //TODO In Progress
 		wifiMode = true;
 		twitterMode = false;
 		wifiConfigIndex++;
 		List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
         int i = configs.size();
         if(wifiConfigIndex >= i)
         	{wifiConfigIndex = 0;}
         if(i == 0)
         {textStatus.setText("No networks saved. Create one to share!\n");}
         else
         {
         	int count = 0;
         	textStatus.setText("");
        		textStatus.append("Tap again to cycle network to share\n");
        		for(WifiConfiguration config : configs)
         	{
        			char selected = 'O';
         		if(count == wifiConfigIndex)
         		{selected = 'X';}
         		textStatus.append("[" + selected + "]" + config.SSID + "\n" );
         		count++;
         	}
         
        		//textStatus.setText(configs.get(wifiConfigIndex).toString());
        		
        		Editable text = mNote.getText();
        		text.clear();
        		text.append("Type the network password here!");//configs.get(wifiConfigIndex).toString());
         }
         	
 	} 
 	
 	public boolean detectWifi(String body)
 	{
 		if(wifiMode == true && body.contains("SSID:") && body.contains("ID:") && body.contains("BSSID:") && body.contains("PRIO:")&& body.contains("KeyMgmt:") && body.contains("Protocols"))
         {
         	textStatus.setText("");
         	textStatus.append("Network Info Found!\n");
         	saveWepConfig(body);
         	//setNoteBody("Wifi info has been added to your phone!");
         	return true;
         }
 		
 		return false;
 		
 	}
 	
 	public void onWriteClicked(View view)
 	{  
 		// Write to a tag for as long as the dialog is shown.
         disableNdefExchangeMode();
         enableTagWriteMode();
 
         new AlertDialog.Builder(StickyNotesActivity.this).setTitle("Touch tag to write")
                 .setOnCancelListener(new DialogInterface.OnCancelListener() {
                     @Override
                     public void onCancel(DialogInterface dialog) {
                         disableTagWriteMode();
                         enableNdefExchangeMode();
                     }
                 }).create().show();
 	}  
 }
