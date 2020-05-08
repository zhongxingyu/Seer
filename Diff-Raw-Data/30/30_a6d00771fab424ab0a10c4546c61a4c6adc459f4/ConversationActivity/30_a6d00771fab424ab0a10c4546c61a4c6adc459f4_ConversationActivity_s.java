 package com.aha.activities;
 
 import java.net.DatagramSocket;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.aha.R;
 import com.aha.models.AppInfo;
 import com.aha.models.Constants;
 import com.aha.models.DataObject;
 import com.aha.models.NetworkInfo;
 import com.aha.services.NetService;
 import com.aha.services.NetService.LocalBinder;
 
 public class ConversationActivity extends Activity implements OnClickListener {
 
 	EditText address, message;
     NetService mService;
     AlertDialog alert;
     boolean mBound = false;
     int orginIP;
     Handler handler;
     
 	Button sendB;
 	EditText et;
 	ListView lv;
 	private ArrayAdapter<String> conversationArray;    
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.conversation);
         
         conversationArray = new ArrayAdapter<String>(this, R.layout.message);
         lv = (ListView)this.findViewById(R.id.ListView); 
         lv.setAdapter(conversationArray);           
         
         et = (EditText)this.findViewById(R.id.EditText);  
         
         sendB = (Button)this.findViewById(R.id.SendB);                 
         sendB.setOnClickListener(this);
     }
     
     private void initConversation() {
     	
     	
     	
     }
         
     public void onClick(View view) {
         	
         switch (view.getId()) {	    	        
             case R.id.SendB:
     	        if (mBound) {
     	        	
     	        	try {
     	    			DataObject dataObject = new DataObject();
     	    			dataObject.setMessage(et.getText().toString());	
     	    			dataObject.setDestinationAddress(orginIP);
     	    			dataObject.setOrginAddress(NetworkInfo.getInstance().getMyIP());
     	    			dataObject.setMessageType(Constants.ALERT); 
     	    			
     	    			NetworkInfo ni = NetworkInfo.getInstance();
     	    			
 						if (ni.conversations.containsKey(orginIP)) {
 							Vector<DataObject> v = ni.conversations
 									.get(orginIP);
 							v.add(dataObject);
 						} else {
 							Vector<DataObject> v = new Vector<DataObject>();
 							v.add(dataObject);
 							ni.conversations.put(new Integer(orginIP), v);
 						}    	    			
        	    			loadList();
 
     	        		mService.sendMessage(dataObject);
     	        		et.setText("");
     	    		} catch (Exception e) {
     	    			e.printStackTrace();
     	    		}	
     	        }  
     	        break;	    	        
         }	            		        	        
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         // Bind to LocalService        
         try
         {
         	orginIP = getIntent().getExtras().getInt("originIP");
         	// bind to service
         	Intent intent = new Intent(this, NetService.class);
 	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	        
 	        // init conversation array
 	        loadList();
 	              
         } catch (Exception e) {
         	
         	e.printStackTrace();
         }
         
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         // The activity has become visible (it is now "resumed").
         Intent intent = new Intent(this, NetService.class);
         getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);	        
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         if (mBound) {
         	getApplicationContext().unbindService(mConnection);
             mBound = false;
         }	        
         // Another activity is taking focus (this activity is about to be "paused").
     }	    
     
     @Override
     protected void onStop() {
         super.onStop();
         // Unbind from the service
         if (mBound) {
         	getApplicationContext().unbindService(mConnection);
             mBound = false;
         }
     }
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
 
         //@Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LocalBinder binder = (LocalBinder) service;
             mService = binder.getService();
             mBound = true;
             
             handler = new Handler() {
                 @Override
                 public void handleMessage(Message msg) {
                     switch (msg.what) {
         	            case 3:
         	            loadList();	     	            
         	            break;
                     }
                 }
             };            
             
             AppInfo ai = AppInfo.getInstance();
             
 	        ai.setConversationContext(ConversationActivity.this);
 	        ai.setConversationHandler(handler);            
 
         }
    
         //@Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };	
 	
     
     
 	
     private synchronized void loadList() 
     {
         conversationArray.clear();
                 
         NetworkInfo ni = NetworkInfo.getInstance();
         
         Vector<DataObject> v = ni.conversations.get(orginIP);
         
         if (v!=null)
         {
 	        for (int i=0; i<v.size(); i++) {
 	        	
 	        	if (v.get(i).getOrginAddress() == ni.getMyIP())
 	        		conversationArray.add("ME: " + v.get(i).getMessage());
 	        	else
	        		conversationArray.add(v.get(i).getOrginAddress() + ": " + v.get(i).getMessage());
 	        }   
         }
     	
     }	
 
 }
 
