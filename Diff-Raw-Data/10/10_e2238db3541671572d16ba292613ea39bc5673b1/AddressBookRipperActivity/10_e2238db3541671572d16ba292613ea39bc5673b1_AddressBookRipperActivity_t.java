 package com.advsofteng.app1;
 
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import com.google.gson.Gson;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.telephony.TelephonyManager;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddressBookRipperActivity extends Activity {
 	
 	private static final String TAG = "Ripper"; 
 	private static final String KEY = "ASE-GROUP2";	/* Key used for SHA-1 encoding */
 	private GatherContactsTask gatherer = null;
 	AddressEntryAdapter adaptor = null;
 	
 	private String countryCode;	/* ISO Country Code to be used for canonicalising MSISDNS */
 	private String ownNumber; /* Users own phone number */
 	private Button sendFriendList;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.contacts);
 		
 		TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
 		countryCode = tm.getSimCountryIso();
 		ownNumber = tm.getLine1Number();
 		
 		gatherer = new GatherContactsTask();
 		gatherer.execute();
 
 		sendFriendList = (Button) this.findViewById(R.id.btnSendFriends);
 		sendFriendList.setText(R.string.friends_loading);
 		sendFriendList.setEnabled(false);
 	
 		sendFriendList.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				uploadContacts(AdvSoftEngApp1Activity.globalAddressBook);
 			}
 		});
 				
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.addressbook, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 		    case R.id.unsubscribe:
 		    	unsubscribe();
 		        return true;
 		    default:
 		        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	private void unsubscribe() {
 		UnsubscribeTask unsubscriber = new UnsubscribeTask();
     	unsubscriber.execute();
 	}
 	
 	////////////////////////////////////////////
 
 	private void uploadContacts(AddressBook a) {
     	UploadContactsTask uploader = new UploadContactsTask();
     	uploader.execute(a);
     }
 	
 	/**
 	 * Turns a given MSISDN into a usable hash to identify the number. Does this by
 	 * rendering the number into a canonical international MSISDN, then putting
 	 * it through SHA-1
 	 * 
 	 * @param s input MSISDN
 	 * @return
 	 */
 	private String hashMsisdn(String s) {
 		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
 		String input = null;
 		PhoneNumber pn = null;
 		
 		try {
 			pn = phoneUtil.parse(s, countryCode.toUpperCase());
 			input = phoneUtil.format(pn,PhoneNumberFormat.E164);
 			return sha1(input, KEY);
 		} catch (Exception e) {
 			// This is either a NumberFormatException or an error finding the crypto stuffs.
 			// Either way ignore, we'll use the number as given if we can't format it
 			return input;
 		}
 	}
 	
 	/**
 	 * Hash function, taken from http://stackoverflow.com/questions/4534370/how-to-hash-string-using-sha-1-with-key
 	 * 
 	 * @param s
 	 * @param keyString
 	 * @return
 	 * @throws UnsupportedEncodingException
 	 * @throws NoSuchAlgorithmException
 	 * @throws InvalidKeyException
 	 */
 
 	private String sha1(String s, String keyString)
 			throws UnsupportedEncodingException, NoSuchAlgorithmException,
 			InvalidKeyException {
 
 		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"),
 				"HmacSHA1");
 		Mac mac = Mac.getInstance("HmacSHA1");
 		mac.init(key);
 
 		byte[] bytes = mac.doFinal(s.getBytes("UTF-8"));
 
 		return new String(Base64.encode(bytes, 0));
 
 	}
 	////////////////////////////////////////////
 	
 	private class UploadContactsTask extends AsyncTask<AddressBook, Integer, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(AddressBook... ab) {
 			Gson gson = new Gson();
 			Log.i(TAG, "Got entries " + ab[0].getEntries().size());
 			
 			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(AdvSoftEngApp1Activity.ENDPOINT+"/addressBook");
 
 			try {
 				HttpEntity ent = new StringEntity(gson.toJson(ab[0]));
 				post.setEntity(ent);
 				HttpResponse response = client.execute(post);
				Log.i(TAG, "post to " + post.getURI() + " done, response="+response.getStatusLine().getStatusCode());
 			} catch (Exception e) {
 				Log.i(TAG, "post threw " + e);
 				e.printStackTrace();
 				return false;
 			}
 			
 			return true;
 		}
 	}
 	
 	void CopyAddressBookEntry(AddressBookEntry originalAddBookEnt, AddressBookEntry copiedAddBookEnt ){
 		
 		copiedAddBookEnt.setHashes(originalAddBookEnt.getHashes());
 		copiedAddBookEnt.setId(originalAddBookEnt.getId());
 		copiedAddBookEnt.setName(originalAddBookEnt.getName());
 		copiedAddBookEnt.setOwner(originalAddBookEnt.getOwner());
 		copiedAddBookEnt.setPermission(originalAddBookEnt.getPermission());
 		
 		return;
 	}
 
 	
 	/**
 	 * Private class to gather contacts from the on-phone address book and use
 	 * them to populate an AddressBook object in the background.
 	 * 
 	 * @author twhume
 	 * 
 	 */
 
 	private class GatherContactsTask extends AsyncTask<Void, Integer, AddressBook> {
 
 		protected void onPostExecute(AddressBook result) {
 			Log.i(TAG, System.currentTimeMillis() + " done, result= " + result);
 			
 			AdvSoftEngApp1Activity.globalAddressBook = result;
 			ListView list = (ListView)findViewById(R.id.friendslist);
 			adaptor = new AddressEntryAdapter();
 			list.setAdapter(adaptor);
 			sendFriendList.setText(R.string.friends_loaded);
 			sendFriendList.setEnabled(true);
 		}
 
 		/**
 		 * Iterate through the address book, creating an AddressBook structure which
 		 * contains a list of AddressBookEntries, which each contains a list of
 		 * hashes
 		 */
 		
 		@Override
 		protected AddressBook doInBackground(Void... params) {
 			Log.i(TAG, System.currentTimeMillis() + " starting");
 
 			String[] columns = new String[] { Phone._ID, Phone.DISPLAY_NAME,
 					Phone.NUMBER, Phone.RAW_CONTACT_ID };
 			Uri contacts = Phone.CONTENT_URI;
 			Cursor managedCursor = managedQuery(contacts, columns,
 					null, // get all rows
 					null, // Selection arguments (none)
 					// Put the results in ascending order by name
 					Phone.DISPLAY_NAME + " ASC");
 
 			AddressBook a = new AddressBook();
 			a.setDeviceId(AdvSoftEngApp1Activity.DEVICE_ID);
 			a.setOwnerHash(hashMsisdn(ownNumber));
 			ArrayList<AddressBookEntry> entries = new ArrayList<AddressBookEntry>();
 			ArrayList<String> hashes = new ArrayList<String>();
 			String lastId = null;
 			a.setEntries(entries);
 
 			try {
 				if (!managedCursor.moveToFirst()) return a;
 				lastId = managedCursor.getString(3);
 				AddressBookEntry abe = new AddressBookEntry();
 				abe.setName(managedCursor.getString(1));
 				
 				do {
 
 					if (!managedCursor.getString(3).equals(lastId)) {
 						Log.d(TAG, "------");
 						abe.setHashes(hashes);
 						entries.add(abe);
 						abe = new AddressBookEntry();
 						abe.setName(managedCursor.getString(1));
 						hashes = new ArrayList<String>();
 					}
 					Log.d(TAG, "adding to hashes:" + hashMsisdn(managedCursor.getString(2)) + " from " + managedCursor.getString(2));
 
 					hashes.add(hashMsisdn(managedCursor.getString(2))); 
 					lastId = managedCursor.getString(3);
 
 				} while (managedCursor.moveToNext());
 				
 				abe.setHashes(hashes);
 				entries.add(abe);
 
 			} finally {
 				managedCursor.close();
 			}
 
 			return a;
 		}
 	}
 	
 	/*
 	 * Private Class that deals with our addressbook UI
 	 * 
 	 */
 	 class AddressEntryAdapter extends ArrayAdapter<AddressBookEntry> {
 		 AddressEntryAdapter() {
 		      super(AddressBookRipperActivity.this, 
 					R.layout.row,  
 					AdvSoftEngApp1Activity.globalAddressBook.getEntries());
 		    }
 		 
 		 
 		 public View getView( int position, View convertView, ViewGroup parent){
 			 
 			 View row=convertView;
 			 AddressHolder holder = null;
 			 final AddressBookEntry currentEntry = ((AddressBookEntry)AdvSoftEngApp1Activity.globalAddressBook.getEntries().get(position));
 
 			 if (row==null) { 
 				 LayoutInflater inflater=getLayoutInflater();
 			     row=inflater.inflate(R.layout.row, parent, false);
 			     holder=new AddressHolder(row);
 			     
 			     holder.friendCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
 			    	 @Override
                      public void onCheckedChanged(CompoundButton buttonView,
                              boolean isChecked) {
 		    		 
 			    		 if(buttonView.isChecked())
 			    			 currentEntry.setPermission(AddressBookEntry.PERM_SHOWN);
 			    		 else
 			    			 currentEntry.setPermission(AddressBookEntry.PERM_HIDDEN);
 			    		 
 			    		 //Log.i(TAG, "Name = " + currentEntry.getName() + " Permission = " + currentEntry.getPermission());
 			    		 
                      }
 
 			     });
 			     
 			     row.setTag(holder);
 				 
 			 }
 			 else{ // access existing row.
 				 holder=(AddressHolder)row.getTag();
 			 }
 			 holder.populateFrom(AdvSoftEngApp1Activity.globalAddressBook.getEntries().get(position));
 			 
 			 return(row);
 		 }
 		 
 	 }
 
 	 // Loads addressBookEntries details into UI arrayAdaptor
 	 static class AddressHolder {
 		    private TextView name=null;
 		    private CheckBox friendCheckBox = null;
 		    private boolean bCheckBox = false;
 		    
 		    AddressHolder(View row) {
 		      name=(TextView)row.findViewById(R.id.row_name);
 		      friendCheckBox = (CheckBox)row.findViewById(R.id.row_checkbox);
 		      
 		    }
 		    
 		    void populateFrom(AddressBookEntry entry) {
 		    	
 		    	if(AddressBookEntry.PERM_SHOWN == entry.getPermission())
 		    		bCheckBox = true;
 		    	else
 		    		bCheckBox = false;
 		    	
 		        name.setText(entry.getName());
 		        friendCheckBox.setChecked(bCheckBox);
 		    
 		       
 		      }
 
 	 }
 	 
 		/**
 		 * Fires off an HTTP request to unsubscribe this app from the service
 		 * 
 		 * @author twhume
 		 *
 		 */
 
 		private class UnsubscribeTask extends AsyncTask<Void, Integer, Void> {
 
 			protected void onPostExecute(Void result) {
 				finish();
 					Toast toast=Toast.makeText(getApplicationContext(), getString(R.string.unsubscribed), 2000);
 					toast.show();
 			}
 
 			/**
 			 * Fire off the "unsubscribe me from this service" HTTP request
 			 */
 			
 			@Override
 			protected Void doInBackground(Void... params) {
 				Log.i(TAG, System.currentTimeMillis() + " starting");
 				
 				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(AdvSoftEngApp1Activity.ENDPOINT + "/unsubscribe/" + AdvSoftEngApp1Activity.DEVICE_ID);
 
 				try {
 					HttpResponse response = client.execute(post);
 					Log.i(TAG, "post to " + post.getURI() + " done, response="+response.getStatusLine().getStatusCode());
 				} catch (Exception e) {
 					Log.i(TAG, "post threw " + e);
 					e.printStackTrace();
 				}
 				
 				return null;
 			}
 		}
 
 }
