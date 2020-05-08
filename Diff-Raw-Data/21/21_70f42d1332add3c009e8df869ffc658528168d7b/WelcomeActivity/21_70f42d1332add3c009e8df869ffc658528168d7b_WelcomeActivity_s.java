 package com.nbos.phonebook;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.CookieManager;
 import android.widget.FilterQueryProvider;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 import com.nbos.phonebook.contentprovider.Provider;
 import com.nbos.phonebook.value.Contact;
 import com.nbos.phonebook.value.Group;
 
 public class WelcomeActivity extends ListActivity {
 	static String tag = "Phonebook";
 	List<Contact> m_contacts = null;
 	List<Contact> m_phoneContacts = null;
 	ProgressDialog m_ProgressDialog = null;
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.group_list_menu, menu);
 	    return true;
 		
 	}
     static int ADD_GROUP = 1, SHOW_GROUP = 2;
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.add_group:
 		     Intent i = new Intent(WelcomeActivity.this, AddGroupActivity.class);
 	         startActivityForResult(i, ADD_GROUP);	
 	     }
 	     return true;
 	  
 	    }
 		
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if((requestCode == ADD_GROUP && resultCode == RESULT_OK)
 		|| (requestCode == SHOW_GROUP && resultCode == RESULT_OK))
 				refreshGroups();
 	}
 
 	
 	private void refreshGroups() {
 		System.out.println("Refresh the groups");
 		m_cursor.requery();
 	}
 	List<Group> m_groups;
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         testContentProvider();
         populateGroups();
         getListView().setTextFilterEnabled(true);
     	String phoneNumber = getPhoneNumber();
     	Log.i(tag, "phone number: "+phoneNumber);
    	Test.getDirtyGroups(this.getApplicationContext());
     	// Test.getGroupList(this.getApplicationContext());
     }
 
 
 	private void testContentProvider() {
         Uri URI = Uri.parse("content://"+Provider.AUTHORITY+"/"+Provider.BookContent.CONTENT_PATH);
         Cursor c = getContentResolver().query(URI, null, null, null, null);
         Log.i(tag, "There are "+c.getCount()+" books");
 	}
 	Cursor m_cursor;
     
     private void populateGroups() {
 	    ContentResolver cr = getContentResolver();
 	    m_cursor = cr.query(ContactsContract.Groups.CONTENT_SUMMARY_URI, null,
 	    	ContactsContract.Groups.DELETED + "=0",	    		
 	    	null, null);
 	  
         String[] fields = new String[] {
                 ContactsContract.Groups.TITLE,
                 ContactsContract.Groups.SUMMARY_COUNT,
                 ContactsContract.Groups.SYNC1
         };
         
         SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.group_entry, m_cursor,
                 fields, new int[] {R.id.groupName, R.id.groupCount, R.id.groupOwner});
         
         adapter.setStringConversionColumn(
                 m_cursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));
         adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
             public Cursor runQuery(CharSequence constraint) {
                 String partialItemName = null;
                 if (constraint != null) {
                     partialItemName = constraint.toString();
                 }
                 m_cursor = getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI, null,
             	        // "DISPLAY_NAME = '" + NAME + "'",
             	    	ContactsContract.Groups.DELETED + "=0"
             	    	+ " and "+ContactsContract.Groups.TITLE+" like '%" + partialItemName+ "%'",	    		
             	    	null, null);
                 Log.i(tag, "partial name is" +partialItemName);
                 
                 return m_cursor;
             }
         });
         
         getListView().setAdapter(adapter);
 	    
 	}
    
     ListView mGroupList;
 
 
     private  Runnable mainUiThread = new Runnable() {
 	    public void run() {
 	    	// createAGroup(getApplicationContext());
 	    	getAccounts();
 
 			
 			/*
 	    	m_phoneContacts = getPhoneContacts();
 	    	TextView note2 = (TextView) findViewById(R.id.note2);
 	    	note2.setText("There are "+m_phoneContacts.size()+" contacts in your phone");
 	    	m_ProgressDialog = ProgressDialog.show(WelcomeActivity.this,    
 	                  "Please wait ...", "Retrieving data ...", true);
 	    	new DownloadContactsTask().execute();
 	    	*/
 	    }
 
 
 		boolean hasAccount(String name, String type) {
 	        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
 	        Log.i(tag, "There are "+accounts.length+" accounts");
 	        for (Account account : accounts) 
 	        	if(account.name == name && account.type == type)
 	        		return true;
 	    	return false;
 	    }
 		
 		private void getAccounts() {
 	        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
 	        Log.i(tag, "There are "+accounts.length+" accounts");
 	        for (Account account : accounts) {
 	        	Log.i(tag, "account name: "+account.name);
 	        	Log.i(tag, "account type: "+account.type);
 	        }
 
 			// TODO Auto-generated method stub
 			
 		}
     };
     
     private class DownloadContactsTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			/*TextView note1 = (TextView) findViewById(R.id.note1);
 			if(m_contacts == null)
 				note1.setText("Could not get contacts from remote");
 			else
 				note1.setText("There are "+m_contacts.size()+" contacts in remote");
 			*/
 			m_ProgressDialog.dismiss();
 			if(m_exception != null)
 			{
 				alert(m_exception);
 				m_exception = null;
 			}
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			getContacts();
 			return null;
 		}
     } 
     String m_exception = null;
     private void getContacts() {
     	String phoneNumber = getPhoneNumber();
     	Log.i(tag, "phone number: "+phoneNumber);
         HttpClient httpClient = getHttpClient();
         HttpContext localContext = new BasicHttpContext();
         HttpGet httpGet = new HttpGet(HTTP_SERVER + "/phonebook/mobile/index?id="+phoneNumber);
         try {
           HttpResponse response = httpClient.execute(httpGet, localContext);
           m_contacts = parseContacts(EntityUtils.toString(response.getEntity()));
           synchronizeContacts();
           Log.i(tag, "There are "+m_phoneContacts.size()+" contacts in the list");
         } catch (Exception e) {
           Log.e(tag, "Exception: "+e);
           m_exception = "Could not sync with server. Error is: "+e.toString(); 
           e.printStackTrace();
         } 
         // new Thread(returnResults).start();
         // runOnUiThread(returnRes);
       }
 
     private void synchronizeContacts() {
 		
 	}
 
 	private void alert(String string) {
     	Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(string);
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	private  Runnable returnResults = new Runnable() {
         public void run() {
             /*if(m_orders != null && m_orders.size() > 0){
                 m_adapter.notifyDataSetChanged();
                 for(int i=0;i<m_orders.size();i++)
                 m_adapter.add(m_orders.get(i));
             }*/
             
             // m_adapter.notifyDataSetChanged();
         }
       };
     
     private String getPhoneNumber() {
 		return ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
 	}
 
 	private List<Contact> parseContacts(String string) throws JSONException {
         ArrayList<Contact> sContacts = new ArrayList<Contact>();
         JSONArray contacts= new JSONArray(string);
         // getWindow().setTitle("Welcome. There are "+contacts.length()+" contacts.");
         for(int i=0; i< contacts.length(); i++)
         {
           JSONObject contact = contacts.getJSONObject(i);
           Contact c = new Contact(contact.getString("name"), contact.getString("number"));
           sContacts.add(c);
         }
         Log.i(tag, "There are "+sContacts.size()+" contacts.");
         return sContacts;//.toArray(new String[sCoupons.size()] );
 	}
 
 	private List<Contact> getPhoneContacts() {
 	    //  Find contact based on name.
 	    ContentResolver cr = getContentResolver();
 	    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
 	        // "DISPLAY_NAME = '" + NAME + "'",
 	    	null, null, null);
 	    Log.i(tag, "There are "+cursor.getCount()+" contacts on the phone");
 	    List<Contact> contacts = new ArrayList<Contact>(); 
 		    while (cursor.moveToNext()) 
 		    {
 		        String contactId =
 		            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
 	        //
 	        //  Get all phone numbers.
 	        //
 	        Cursor phones = cr.query(Phone.CONTENT_URI, null,
 	            Phone.CONTACT_ID + " = " + contactId, null, null);
 	        
 	        while (phones.moveToNext()) {
 	            String number = phones.getString(phones.getColumnIndex(Phone.NUMBER)),
 	            	name = phones.getString(phones.getColumnIndex(Phone.DISPLAY_NAME));
 	            Log.i("", "Name is: "+name+", number is: "+number);
 	            contacts.add(new Contact(name, number));
 	            int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
 	            switch (type) {
 	                case Phone.TYPE_HOME:
 	                    // do something with the Home number here...
 	                    break;
 	                case Phone.TYPE_MOBILE:
 	                    // do something with the Mobile number here...
 	                    break;
 	                case Phone.TYPE_WORK:
 	                    // do something with the Work number here...
 	                    break;
 	                }
 	        }
 	        phones.close();
 	        //
 	        //  Get all email addresses.
 	        //
 	        Cursor emails = cr.query(Email.CONTENT_URI, null,
 	            Email.CONTACT_ID + " = " + contactId, null, null);
 	        while (emails.moveToNext()) {
 	            String email = emails.getString(emails.getColumnIndex(Email.DATA));
 	            int type = emails.getInt(emails.getColumnIndex(Phone.TYPE));
 	            switch (type) {
 	                case Email.TYPE_HOME:
 	                    // do something with the Home email here...
 	                    break;
 	                case Email.TYPE_WORK:
 	                    // do something with the Work email here...
 	                    break;
 	            }
 	        }
 	        emails.close();
 	    }
 	    cursor.close();
 	    return contacts;
 	}	
 	static String HTTP_SERVER = "http://10.9.8.29:8080";
     // static String HTTP_SERVER = "https://ecoupons.upromise.com";
     static HttpClient httpClient;
     
     public static HttpClient getHttpClient() {
         if(httpClient == null)
         {
         	HttpParams httpParameters = new BasicHttpParams();
         	int timeoutConnection = 3000;
         	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
         	// Set the default socket timeout (SO_TIMEOUT) 
         	// in milliseconds which is the timeout for waiting for data.
         	int timeoutSocket = 5000;
         	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
         	
           httpClient = new DefaultHttpClient(httpParameters);
           CookieManager cookieManager = CookieManager.getInstance();
           cookieManager.setAcceptCookie(true); 
         }
         return httpClient;
     }
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		m_cursor.moveToPosition(position);
 		String groupId = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Groups._ID));
 		String groupName = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Groups.TITLE));
 		Log.i(tag, "Group id: "+groupId+", name: "+groupName);
 		Intent i = new Intent(WelcomeActivity.this, GroupActivity.class);
 		i.putExtra("id", groupId);
 		i.putExtra("name", groupName);
         startActivityForResult(i, SHOW_GROUP);	
 		//Toast.makeText(this, "You selected: group id: " + o.getId()+", name: "+o.getName(), Toast.LENGTH_SHORT)
 			//	.show();
 
 	}
 }
