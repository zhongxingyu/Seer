 package com.linkedin.localin.ININ;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.linkedin.localin.ININ.ChatActivity.msgHandler;
 
 
 import android.app.Service;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.widget.Toast;
 
 
 public class MessageCenter extends Service {
 
 	public static final String DEBUG_TAG = "orion_database";
 	
 	//records will be stored into local database
 	private static final boolean TODB = true;
 	
 	HashMap<Integer,msgHandler> handlers = new HashMap<Integer,msgHandler>();
 	HashMap<Integer,ArrayList<Msg>> bufferPool = new HashMap<Integer,ArrayList<Msg>>();
 	
 	private msgHandler handler;
 	public void setHandler(Integer userid, msgHandler activityHandler){
 		 handlers.put(userid, activityHandler);
 	}
 	
	public void clearHandler(Integer userid){
		handlers.remove(userid);
	}
	
 	/*=========================== Service Backbone Part ===========================*/
     private final IBinder mBinder = new SampleBinder();
     
 	public class SampleBinder extends Binder{
 		public MessageCenter getService(){
 			return MessageCenter.this;
 		}
 	}	
 	
 	private ArrayList<Msg> messages;
 	public static int my_id;
 	Timer timer = new Timer();
 	
 	
 	public void deliverMessages(){
 		for(Integer userid : bufferPool.keySet()){
 			msgHandler handler = handlers.get(userid);
 			if(handler == null){
 				
 			}
 			else{
 				Message msg = new Message();
 				msg.what = 1;
 				msg.obj = bufferPool.get(userid);
 				handler.sendMessage(msg);
 			}
 		}
 	}
 	
 	private void bufferMessage(Msg message){
 		Integer from = (int) message.getFromUserId(); //52502731
 		ArrayList<Msg> buffer =  bufferPool.get(from);
 		if(buffer == null){
 			buffer = new ArrayList<Msg>();
 			buffer.add(message);
 			bufferPool.put(from, buffer);
 		}
 		else{
 			buffer.add(message);
 		}
 	}
 	
 	
 	@Override 
 	public void onDestroy(){
 		timer.cancel();
 	}
 	
 	TimerTask fetchTask = new TimerTask(){
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			HttpClient client = new DefaultHttpClient();
 		    Log.d("SeanTest", "fetching data to "+my_id);   
 	        HttpGet mget = new HttpGet("http://aaronplex.net/project/localin/retrieve.php?to="+my_id);
 			String messageid = null;
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
 	    			JSONArray jsonArray = new JSONArray(sb.toString());
 	    			
 	    			//DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    			if(jsonArray.length()>0){
 		    			for(int i =0;i<jsonArray.length();i++){
 		    				JSONObject obj = jsonArray.getJSONObject(i);
 		    				
 		    				
 		    				Msg m = new Msg(obj.getLong("id"),obj.getLong("fromUser") ,obj.getLong("toUser"), obj.getString("message"), obj.getString("time"), 0);
 		    				Log.d("SeanMsg", m.getMessage());
 		    				bufferMessage(m);
 		    			}
 		    			deliverMessages();
 //		    			handler.post(new Runnable() {
 //							@Override
 //							public void run() {
 //								//logLastMessage(messages.get(messages.size()-1));
 //								
 //							}
 //						});
 	    			}
 	    			Log.d("info", sb.toString());
 	    			//messageid = sb.toString().trim();
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}	
 		}
     	
     };
 	
 	public int logLastMessage(Msg message){
 		long otherId;
 		if(message.getFromUserId()==my_id){
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
     
 	@Override
 	public boolean onUnbind(Intent intent){	
 		
 //		int modeInt = intent.getIntExtra("mode",0);
 //		MODE mode = MODE.values()[modeInt];
 //		if(mode == MODE.COLLECT){
 //			collect_service_num --;
 //		}
 		
 		//if( mode==MODE.COLLECT || mode == MODE.EXPERIMENT){
 		
 		return super.onUnbind(intent);
 	}
 	
 	
 	
 	@Override 
 	public void onCreate(){
 		timer.schedule(fetchTask, 1000,1000);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return mBinder;
 	}
 }
