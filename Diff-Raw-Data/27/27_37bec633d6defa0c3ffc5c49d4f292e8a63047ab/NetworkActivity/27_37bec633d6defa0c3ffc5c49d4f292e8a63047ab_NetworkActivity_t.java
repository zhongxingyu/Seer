 package com.aha.activities;
 
 import java.util.Vector;
 
 import android.app.Activity;
 
 
 
 
 
 import com.aha.R;
 import android.widget.ArrayAdapter;
 
 import com.aha.models.AppInfo;
 import com.aha.models.Constants;
 import com.aha.models.DataObject;
 import com.aha.models.NetworkInfo;
 import com.aha.services.NetService;
 import com.aha.services.NetService.LocalBinder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class NetworkActivity extends Activity implements OnClickListener, OnItemClickListener {
 	
 	
 	Button infoB;
 	Button wifiOnB;
 	Button wifiOffB;
 	Button localB;
 	Button staticB;
 	Button globalB;
 	TextView statusTV;
 	TextView ipTV;
     NetService mService;
     AlertDialog alert;
     boolean mBound = false;
     private ArrayAdapter<Integer> networkArray;
     ListView lv;
     Handler handler;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.network);
         statusTV = (TextView)this.findViewById(R.id.StatusTV);
         ipTV = (TextView)this.findViewById(R.id.IPTV);
         localB = (Button)this.findViewById(R.id.LocalB);
         globalB = (Button)this.findViewById(R.id.GlobalB);
                 
         localB.setOnClickListener(this);
         globalB.setOnClickListener(this);
         
        networkArray = new ArrayAdapter<Integer>(this, R.layout.message);
        lv = (ListView)this.findViewById(R.id.ListView);
        lv.setAdapter(networkArray); 
        lv.setOnItemClickListener(this);
        
        ipTV.setText(Constants.BASE_ADDRESS + NetworkInfo.getInstance().getInitIP());
        
     }
     
     public void onItemClick(AdapterView<?> parent, View view,
             int position, long id) {
     		    	
     	  String orginIP = (String)((TextView) view).getText();
     	  
           Intent intent = new Intent(NetworkActivity.this, ConversationActivity.class);
           intent.putExtra("originIP", orginIP);
           startActivity(intent);           
           
         }    
     
         
     public void onClick(View v) {
         	
         switch (v.getId()) {	    	        
             case R.id.LocalB:
     	        if (mBound) {	    	        	
     	        	try {
 	        	    	Intent intent = new Intent(NetworkActivity.this, SendMessageActivity.class);
     	                startActivity(intent); 
     		        	
     	    		} catch (Exception e) {
     	    			e.printStackTrace();
     	    		}	
     	        } 
     	        break;
             case R.id.GlobalB:
     	        if (mBound) {
     	            String msg = mService.getStatus();
     	            statusTV.setText(msg);
     	        }
     	        break;	    	        
         }	            		        	        
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         // Bind to LocalService
         //System.out.println("Trying to bind to the service");
         
         try
         {
 	        Intent intent = new Intent(this, NetService.class);
 	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	        
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
 	    	            case 2:
 	        	            String ip = (String) msg.obj;
 	        	            if (ip.length() > 0)
	        	            	ipTV.setText(ip);
 	        	            
 	        	            loadList();	     	            
 	        	        break;
                     	case 3:
         	            statusTV.setText((String) msg.obj);	     	            
         	            break;
                     }
                 }
             };            
             
             AppInfo ai = AppInfo.getInstance();
             
 	        ai.setNetworkContext(NetworkActivity.this);
 	        ai.setNetworkHandler(handler);             
         }
 
         //@Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };	
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }  
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.disconnect:
             mService.netOff();
             statusTV.setText("Network is off");
             return true;
         case R.id.connect:
 	        if (mBound) {	    	        	
 	        	try {
 	        		
 	    			final String[] items = { "30minutes", "1hour", "2hours", "3hours" };
 		        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		        	builder.setTitle("How long are you staying?");
 		        	builder.setItems(items, new DialogInterface.OnClickListener() {
 		        	    public void onClick(DialogInterface dialog, int item) { 
 		        	 
 		        	    	mService.startNetwork(item);
 		        	    	
 		        	    }
 		        	});
 		        	alert = builder.create();        
 		        	alert.show();
 		        	
 	    		} catch (Exception e) {
 	    			e.printStackTrace();
 	    		}
 	        }
             return true;            
         default:
             return super.onOptionsItemSelected(item);
         }
     }    	  
     
     private synchronized void loadList() 
     {
         networkArray.clear();
                
         NetworkInfo ni = NetworkInfo.getInstance();
        
         for (int i=0; i<ni.network.size(); i++) {
         	networkArray.add(ni.network.get(i));
         }        	
     	
     }	    
 
 }
