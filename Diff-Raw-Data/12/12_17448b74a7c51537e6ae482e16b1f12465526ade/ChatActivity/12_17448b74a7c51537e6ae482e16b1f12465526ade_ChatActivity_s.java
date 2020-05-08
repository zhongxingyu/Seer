 package com.linkedin.localin.ININ;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.linkedin.localin.ININ.MessageCenter.SampleBinder;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ChatActivity extends Activity implements OnClickListener
 {
 	private Contact currentUser;
 	private Contact contact;
 	
 	private TextView titleText;
 	private Button btnSend;
 	private EditText contentText;
 	private ListView chatList;
 	
 	private ArrayList<Msg> messages;
 	private MsgListAdapter adapter;
 	
 	Context mContext;
 	
 	private HttpClient httpclient = new DefaultHttpClient();
 
 	public msgHandler dHandler;
 	
 	public class msgHandler extends Handler{
 
         @Override
         public void handleMessage(Message msg){
 
             switch(msg.what){
             case 1:
             	//Toast.makeText(mContext, "works", Toast.LENGTH_LONG).show();
             	ArrayList<Msg> list = (ArrayList<Msg>)msg.obj;
             	messages.addAll(list);
             	adapter.notifyDataSetChanged();
             //Implement this
           }
       }
 	}   
 	
 	MessageCenter mMessageCenter;
 	boolean mBound = false;
 	
 	private ServiceConnection mConnection = new ServiceConnection(){
     	@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			SampleBinder binder = (SampleBinder) service;
 			mMessageCenter = binder.getService();
 			mMessageCenter.setHandler((int)contact.getMemberId(), dHandler);
 			mBound = true;
 		}
 
     	@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mBound = false;
 		}
     	
     };
 	
 	
     
     AsyncTask <Void,Void,JSONArray> fetchRecent = new AsyncTask<Void,Void,JSONArray>(){
 
 		@Override
 		protected JSONArray doInBackground(Void... params) {
 			HttpClient client = new DefaultHttpClient();
 			JSONArray jsonArray ; 
 	        HttpGet mget = new HttpGet("http://aaronplex.net/project/localin/recent.php?user="+contact.getMemberId());
 			try
 			{
 				
 				HttpResponse response = client.execute(mget);
 				if(response.getStatusLine().getStatusCode() == 200)
 				{
 					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	    			StringBuilder sb = new StringBuilder();
 	    			String line;
 	    			while((line = br.readLine()) != null)
 	    			{
 	    				sb.append(line);
 	    				//Log.d("info", line);
 	    			}
 	    			br.close();
 	    			jsonArray = new JSONArray(sb.toString());
 	    			return jsonArray;
 				}
 			}
 			catch(Exception e){
 				e.printStackTrace();
 				return null;
 			}
 			return null;
 			
 		}
 		
 		@Override
         protected void onPostExecute(JSONArray jsonArray) {
 			try{
 				if(jsonArray!=null){
 					for(int i =jsonArray.length();i>0;i--){
 						JSONObject obj = jsonArray.getJSONObject(i-1);
 						Msg m = new Msg(obj.getLong("id"),obj.getLong("fromUser") ,obj.getLong("toUser"), obj.getString("message"), obj.getString("time"), 0);
 						messages.add(m);
 					}
 					logLastMessage(messages.get(messages.size()-1));
 					adapter.notifyDataSetChanged();
 				}
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
         }
     };
     
    
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat);
         
         contact = (Contact)this.getIntent().getSerializableExtra("contact");
         currentUser = (Contact)this.getIntent().getSerializableExtra("current");
         titleText = (TextView)findViewById(R.id.textView1);
         btnSend = (Button)findViewById(R.id.button1);
         chatList = (ListView) findViewById(R.id.listView1);
         contentText = (EditText) findViewById(R.id.editText1);
         
         titleText.setText("Chat with " + contact.getName());
         
         btnSend.setOnClickListener(this);
         
         messages = new ArrayList<Msg>();
         adapter = new MsgListAdapter(this, messages, currentUser, contact);
         chatList.setAdapter(adapter);
         
         fetchRecent.execute((Void[]) null);  
         dHandler =  new msgHandler();
         
         mContext = this;
         MessageCenter.my_id = (int)currentUser.getMemberId();
         dHandler = new msgHandler();
         Intent intent= new Intent(this, MessageCenter.class);
     	boolean bindsuccess = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
         
     }
    
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_chat, menu);
         return true;
     }
     
     @Override
     public void onRestart(){
         Intent intent= new Intent(this, MessageCenter.class);
     	boolean bindsuccess = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
     	Log.d("message","restart");
     	super.onRestart();
     }
 
     @Override
     public void onStop(){
     	unbindService(mConnection);
     	Log.d("message", "disconnect");
     	super.onStop();
     }
     
 	@Override
 	public void onClick(View v) 
 	{
 		String message = contentText.getText().toString();
 		contentText.setText("");
 		if(message.length() != 0)
 		{
 			HttpPost mpost = new HttpPost("http://aaronplex.net/project/localin/send.php");
 			String messageid = null;
 			try
 			{
 				List<NameValuePair> params = new ArrayList<NameValuePair>();
 				params.add(new BasicNameValuePair("from", "" + currentUser.getMemberId()));
 				params.add(new BasicNameValuePair("to", "" + contact.getMemberId()));
 				params.add(new BasicNameValuePair("message", message));
 				mpost.setEntity(new UrlEncodedFormEntity(params));
 				
 				HttpResponse response = httpclient.execute(mpost);
 				if(response.getStatusLine().getStatusCode() == 200)
 				{
 					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	    			StringBuilder sb = new StringBuilder();
 	    			String line;
 	    			while((line = br.readLine()) != null)
 	    			{
 	    				sb.append(line + "\n");
 	    				//Log.d("info", line);
 	    			}
 	    			br.close();
 	    			Log.d("info", sb.toString());
 	    			messageid = sb.toString().trim();
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			Msg m = new Msg(Long.parseLong(messageid), currentUser.getMemberId(), contact.getMemberId(), message, df.format(new Date()), 0);
 			messages.add(m);
 			logLastMessage(messages.get(messages.size()-1));
 			adapter.notifyDataSetChanged();
 		}
 		
 	}
 	
 	public int delete(int userid){
     	String base = ConversationProvider.RECORD_URI.toString();
     	Uri uri = Uri.parse(base+"/"+userid);
     	ContentResolver contentResolver = this.getContentResolver();
         return contentResolver.delete(uri, null, null);
     }
     
 	public int logLastMessage(Msg message){
 		long otherId;
 		if(message.getFromUserId()==currentUser.getMemberId()){
 			otherId = message.getToUserId();
 		}
 		else{
 			otherId = message.getFromUserId();
 		}
 		return store(otherId,message.getTime(),message.getMessage());
 		
 	}
 	
     public int store(long userid, Date timestamp, String lastSentence){
     	Uri mRecordUri;
     	
     	//step one, store the sensor information and get a row id, uri should just be the normal uri
     	ContentValues values = new ContentValues();
     	
     	values.put(ConversationProvider.OTHERID, userid);
     	values.put(ConversationProvider.LASTSENTENCE, lastSentence);
     	values.put(ConversationProvider.TIMESTAMP, timestamp.getTime());
     	ContentResolver resolver = getContentResolver();
     	mRecordUri = resolver.insert(ConversationProvider.RECORD_URI, values);
     	
     	//should be the id
     	final String row_id = mRecordUri.getLastPathSegment();
     	return Integer.parseInt(row_id);       
     }
     
 public int update(int userid, Timestamp timestamp, String lastSentence){
     	
     	//step one, store the sensor information and get a row id, uri should just be the normal uri
     	ContentValues values = new ContentValues();
     	
     	values.put(ConversationProvider.OTHERID, userid);
     	values.put(ConversationProvider.LASTSENTENCE, lastSentence);
     	values.put(ConversationProvider.TIMESTAMP, timestamp.getTime());
     	ContentResolver resolver = getContentResolver();
     	
     	
     	String base = ConversationProvider.RECORD_URI.toString();
     	Uri uri = Uri.parse(base+"/"+userid);
     	int count = resolver.update(uri, values,null,null);
     	return count;
     }
     
     public void query() throws Throwable{
         ContentResolver contentResolver = this.getContentResolver();
         //Uri uri = Uri.parse("content://com.ljq.provider.personprovider/person");
         Uri uri = ConversationProvider.RECORD_URI;
         Cursor cursor = contentResolver.query(uri, null, null, null, null);
         while(cursor.moveToNext()){
             
             int userid = cursor.getInt(0);
             long timestamp = cursor.getLong(1);
             String lastMsg = cursor.getString(2);
             Log.e("RESULT", userid+" "+timestamp+" "+lastMsg);
         }
         cursor.close();
     }
 }
