 package com.android.locproof.stamp;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StampProtocolActivity extends Activity {
 	/* Debugging */
     private static final String TAG = "StampMain";
     private static final boolean D = true;
     
     /* Roles */
     public static final int SLEEPER 	= 0;
     public static final int WITNESS 	= 1; 
     public static final int PROVER 		= 2;
     
     /* Intent request codes */
     private static final int REQUEST_ENABLE_BT = 1;
     
     public static final String DEVICENAME = "witness0_STAMP";
     
     public static final String SM_TRANSITION = "state_change";
     public static final String NEW_WITNESS = "new_witness";
     public static final String CONNECTION = "connection";
     public static final String MESSAGE = "message";
     
     public static final int UI_M_SMTRANSITION = 0;
     public static final int UI_M_WITNESSLIST = 1;
     public static final int UI_M_CONNECTION = 2;
     public static final int UI_M_RCVDMSG = 3;
     
 	/* UI Layout Views */
     private MyClickListener mCListener;
     private Button btActWitness;
     private Button btReqProof;
     private Button btVldProof;
     private Button btDisable;
     private ScrollView svStatus;
     private TextView tvStatus;
     
     /* */
     private BluetoothAdapter mBluetoothAdapter = null;
     private boolean mBluetoothOn = false;
     private boolean mWitnessOn = false;
     
     private int mRole = SLEEPER;	
     private StampProver mProver = null;
     private StampWitness mWitness = null;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         initUI();
         
         // Get local Bluetooth adapter
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         // If the adapter is null, then Bluetooth is not supported
         if (mBluetoothAdapter == null) {
         	Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
             finish();
             return;
         }
     }
     
     @Override
     public void onStart() {
         super.onStart();
         if(D) Log.e(TAG, "++ ON START ++");
 
         // If BT is not on, request that it be enabled.
         if (!mBluetoothAdapter.isEnabled()) {
         	mBluetoothOn = false;
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         } else {
         	mBluetoothOn = true;
         }
     }
     
     /**
      * Initialize UI elements
      */
     private void initUI(){
     	mCListener = new MyClickListener();
     	btActWitness = (Button) findViewById(R.id.btActWitness);
     	btActWitness.setTag(1);
     	btActWitness.setOnClickListener(mCListener);
     	btReqProof = (Button) findViewById(R.id.btReqProof);
     	btReqProof.setTag(2);
     	btReqProof.setOnClickListener(mCListener);
     	btVldProof = (Button) findViewById(R.id.btVldProof);
     	btVldProof.setTag(3);
     	btVldProof.setOnClickListener(mCListener);
     	btDisable = (Button) findViewById(R.id.btDisable);
     	btDisable.setTag(4);
     	btDisable.setOnClickListener(mCListener);
     	svStatus = (ScrollView) findViewById(R.id.svStatus);
     	tvStatus = (TextView) findViewById(R.id.tvStatus);
     	tvStatus.setText("STAMP\n");
     }
     
     /**
      * Helper function that sets up device
      */
     private synchronized void setDeviceName(){
     	mWitnessOn = false;
 		mBluetoothAdapter.setName(DEVICENAME);
 		Intent discoverable = 
 				new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
 		startActivity(discoverable);
 		mWitnessOn = true;
     }
     
     /**
      * Helper function that makes device undiscoverable
      */
     private synchronized void disableDevice(){
     	Intent discoverable = 
 				new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
 		startActivity(discoverable);
 		mWitnessOn = false;
     }
 	
     /**
      * Button click listener
      */
     private class MyClickListener implements View.OnClickListener{
     	//@Override
     	public void onClick(View v){
     		int tag = (Integer) v.getTag();
     		switch(tag){
 	    		case 1:
 	    			if(D) Log.d(TAG, "Click on witness");
 	    			if(mBluetoothOn == false){
 	    				tvStatus.append("Bluetooth is not available\n");
 	    			}else{
 	    				/* agree to be a witness */
 		    			/* cannot be prover and witness at the same time */
 		    			if((mRole == PROVER)||(mRole == WITNESS)){
 		    				/* do nothing until prover operations finished */
 		    			}else{
 		    				/* initialize witness */
 		    				mRole = WITNESS;
 		    				setDeviceName();
 		    				if(mProver != null){
 		    					mProver.stopSM();
 		    				}
 		    				if(mWitness == null){
 		    					mWitness = new StampWitness(StampProtocolActivity.this, mHandler);
 		    					mWitness.startSM();
 		    				}
 		    			}
 	    			}
 					break;
 				case 2:
 					if(D) Log.d(TAG, "Click on prover");
 					if(mBluetoothOn == false){
 	    				tvStatus.append("Bluetooth is not available\n");
 	    			}else{
 		    			if(mRole == PROVER){
 		    				/* do nothing until prover operations finished */
 		    			}else{
 		    				/* initialize witness */
 		    				mRole = PROVER;
 		    				if(mWitness != null){
 		    					mWitness.stopSM();
 		    				}
 		    				if(mProver == null){
 		    					mProver = new StampProver(StampProtocolActivity.this, mHandler);
 		    					mProver.startSM();
 		    				}
 		    			}
 	    			}
 					break;
 				case 3:
 					
 					break;
 				case 4:
 					if(D) Log.d(TAG, "Click on disable");
 					if(mProver != null){
     					mProver.stopSM();
     					mProver = null;
     				}
 					if(mWitness != null){
     					mWitness.stopSM();
     					mWitness = null;
     				}
 					disableDevice();
 					mRole = SLEEPER;
 					putDebug("MAIN", "Disable");
 					break;
     			default:
     				break;
     		}
     	}
     }
     
     /**
 	 * Helper function that posts debug messages onto control message board
 	 * @param aTag tag of message 
 	 * @param aMsg control message
 	 */
 	private void putDebug(String aTag, String aMsg){
 		if(!aMsg.equals("")){
 			tvStatus.append("["+aTag+"] "+aMsg+"\n");
 			svStatus.post(new Runnable(){
 		        public void run(){
 		        	svStatus.fullScroll(View.FOCUS_DOWN);
 		        }
 		    });
 		}
 	}
 	
     /* The Handler that gets information back from the BluetoothEntities */
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
 	            case UI_M_SMTRANSITION:
 	            	putDebug("STATE", msg.getData().getString(SM_TRANSITION));
 	                break;
 	            case UI_M_WITNESSLIST:
 	            	String witness = msg.getData().getString(NEW_WITNESS);
 	            	String witnesses[];
 	            	if(witness.contains("No witness discovered")){
 	            		putDebug("DISCOVERY", witness);
 	            	}else{
 	            		witnesses = witness.split(";");
 	            		putDebug("DISCOVERY", "Discovered witness: ");
 	            		for(int i = 0; i < witnesses.length; i++){
 	            			putDebug("DISCOVERY", witnesses[i]);
 	            		}
 	            	}
 	                break;
 	            case UI_M_CONNECTION:
 	            	putDebug("CONNECTION", msg.getData().getString(CONNECTION));
 	                break;
 	            case UI_M_RCVDMSG:
 	            	putDebug("MESSAGE", msg.getData().getString(MESSAGE));
 	                break;
 	        }
         }
     };
     
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(D) Log.d(TAG, "onActivityResult " + resultCode);
         switch (requestCode) {
 	        case REQUEST_ENABLE_BT:
 	            // When the request to enable Bluetooth returns
 	            if (resultCode == Activity.RESULT_OK) {
 	                // Bluetooth is now enabled, so set up a chat session
 	            	mBluetoothOn = true;
 	            } else {
 	                // User did not enable Bluetooth or an error occured
 	                Log.d(TAG, "BT not enabled");
 	                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
 	                finish();
 	            }
 	            break;
 	        default:
 	        	break;
 	    }
     }
 }
