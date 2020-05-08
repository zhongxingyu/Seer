 package com.kuxhausen.projectmeet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.CharArrayBuffer;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class InvitePeopleActivity extends Activity implements OnClickListener {
 
 	Button addPerson;
 	ListView addedPeople;
 	ArrayList<Person> pList;
 	CustomAdapter adapter;
 	
 	
 	Meeting theMeeting;
 
 	Toast mToast;
 	ResultDisplayer mPendingResult;
 
 	class ResultDisplayer implements OnClickListener {
 		String mMsg;
 		String mMimeType;
 
 		ResultDisplayer(String msg, String mimeType) {
 			mMsg = msg;
 			mMimeType = mimeType;
 		}
 
 		public void onClick(View v) {
 			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 			intent.setType(mMimeType);
 			mPendingResult = this;
 			startActivityForResult(intent, 1);
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_invitepeople);
 
 		Log.e("asdf",""+getIntent().getExtras().get("theMeeting").getClass());
 		theMeeting = getIntent().getExtras().getParcelable("theMeeting");
 		
 		
 		// Watch for button clicks.
 		Button addPerson = ((Button) findViewById(R.id.addPersonButton));
 		addPerson.setOnClickListener(new ResultDisplayer("Selected contact",ContactsContract.Contacts.CONTENT_ITEM_TYPE));
 		Button sendInvities = ((Button) findViewById(R.id.launchInvitesButton));
 		sendInvities.setOnClickListener(this);
 		
 		pList =  new ArrayList<Person>();
 				
 		adapter = new CustomAdapter(this,
 				R.layout.quick_contacts, pList);
 		addedPeople = ((ListView) findViewById(R.id.addedPeopleListView));
 		addedPeople.setAdapter(adapter);
 	}
 
 	
 	
 	  public void onUpload() {
 	    
 	    String readTwitterFeed = uploadMeeting();
	    try {
 	      JSONArray jsonArray = new JSONArray(readTwitterFeed);
 	      Log.i("asdf",
 	          "Number of entries " + jsonArray.length());
 	      for (int i = 0; i < jsonArray.length(); i++) {
 	        JSONObject jsonObject = jsonArray.getJSONObject(i);
 	        Log.i("asdf", jsonObject.getString("text"));
 	      }
 	    } catch (Exception e) {
 	      e.printStackTrace();
	    }
 	  }
 
 	  public String uploadMeeting() {
 	    StringBuilder builder = new StringBuilder();
 	    HttpClient client = new DefaultHttpClient();
 	        
 	    
 	    HttpPost httpPost = new HttpPost("http://meetable.io/api/0/meeting/new");
 	    try {
 	      
 	    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 	        nameValuePairs.add(new BasicNameValuePair("data", theMeeting.toJSON(pList).toString()));
 	        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));    	
 	    	
 	    	HttpResponse response = client.execute(httpPost);
 	      StatusLine statusLine = response.getStatusLine();
 	      int statusCode = statusLine.getStatusCode();
 	      if (statusCode == 200) {
 	    	  
 	    	Log.e("asdf",response.toString());  
 	    	  
 	        HttpEntity entity = response.getEntity();
 	        InputStream content = entity.getContent();
 	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 	        String line;
 	        String debugOutput = "";
 	        while ((line = reader.readLine()) != null) {
 	          builder.append(line);
 	          debugOutput += line;
 	        }
 	        Log.e("asdf", debugOutput);
 	      } else {
 	        Log.e("asdf", "Failed to download file");
 	      }
 	    } catch (ClientProtocolException e) {
 	      e.printStackTrace();
 	    } catch (IOException e) {
 	      e.printStackTrace();
 	    }
 	    return builder.toString();
 	  }
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.launchInvitesButton:
 			onUpload();
 			break;
 		case R.id.addPersonButton: new ResultDisplayer("Selected contact", ContactsContract.Contacts.CONTENT_ITEM_TYPE);
 		
 		break;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		if (data != null) {
 			Uri uri = data.getData();
 			if (uri != null) {
 				Cursor c = null;
 				try {
 					//c = getContentResolver().query(uri,
 					//		new String[] { BaseColumns._ID }, null, null, null);
 					
 					c = getContentResolver().query(uri,
 							CONTACTS_SUMMARY_PROJECTION, null, null, null);
 					Log.e("fdsa",""+c.getCount());
 					
 					if (!c.moveToFirst()) 
 						return;
 					System.out.println(c.getInt(SUMMARY_ID_COLUMN_INDEX)); 
 					int myId = (int)c.getInt(SUMMARY_ID_COLUMN_INDEX);
 					Person guy = new Person();
 					guy.name = ""+c.getString(SUMMARY_NAME_COLUMN_INDEX);
 					
 					c = getContentResolver().query(Data.CONTENT_URI,
 					          new String[] {Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
 					          Data.CONTACT_ID + "=?" + " AND "
 					                  + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
 					          new String[] {String.valueOf(myId)}, null);
 					
 										
 					
 					if (!c.moveToFirst()) 
 						return;
 					
 					guy.number = c.getString(1);
 					adapter.add(guy);
 					//pList.add(guy);
 					
 					/*if(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)>0)
 					{
 						
 						Cursor pCur = getContentResolver().query(
 						         ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 						         null, 
 						         ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
 						         new String[]{c.getString(SUMMARY_ID_COLUMN_INDEX)}, null);						
 					}
 					*/
 					
 					
 					/*if (c != null && c.moveToFirst()) {
 						int id = c.getInt(0);
 						if (mToast != null) {
 							mToast.cancel();
 						}
 						String txt = mPendingResult.mMsg + ":\n" + uri
 								+ "\nid: " + id;
 						mToast = Toast.makeText(this, txt, Toast.LENGTH_LONG);
 						mToast.show();
 					}*/
 				} finally {
 					if (c != null) {
 						c.close();
 					}
 				}
 			}
 		}
 	}
 
 	static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
 			Contacts._ID, // 0
 			Contacts.DISPLAY_NAME, // 1
 			Contacts.PHOTO_ID, //2
 			Contacts.HAS_PHONE_NUMBER // 3
 	};
 
 	static final int SUMMARY_ID_COLUMN_INDEX = 0;
 	static final int SUMMARY_NAME_COLUMN_INDEX = 1;
 	static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 2;
 	static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 3;
 
 	
 		public class CustomAdapter extends ArrayAdapter<Person>{
 		    private ArrayList<Person> entries;
 		    private Activity activity;
 		 
 		    public CustomAdapter(Activity a, int textViewResourceId, ArrayList<Person> entries) {
 		        super(a, textViewResourceId, entries);
 		        this.entries = entries;
 		        this.activity = a;
 		    }
 		 
 		    public class ViewHolder{
 		        public TextView item1;
 		        public TextView item2;
 		    }
 		 
 		    @Override
 		    public View getView(int position, View convertView, ViewGroup parent) {
 		        View v = convertView;
 		        ViewHolder holder;
 		        if (v == null) {
 		            LayoutInflater vi =
 		                (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		            v = vi.inflate(R.layout.grid_item, null);
 		            holder = new ViewHolder();
 		            holder.item1 = (TextView) v.findViewById(R.id.big);
 		            holder.item2 = (TextView) v.findViewById(R.id.small);
 		            v.setTag(holder);
 		        }
 		        else
 		            holder=(ViewHolder)v.getTag();
 		 
 		        final Person custom = entries.get(position);
 		        if (custom != null) {
 		            holder.item1.setText(custom.name);
 		            if(theMeeting.preferSMS && custom.number.length()>0)
 		            	holder.item2.setText(custom.number);
 		            else
 		            	holder.item2.setText(custom.email);
 		        }
 		        return v;
 		    }
 		}
 	
 	}
 	final class Person {
 	
 	public String name;
 	public String number;
 	public String email;
 	//public QuickContactBadge photoView;
 }
