 package com.columbia.ng911;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.zoolu.sip.provider.SipStack;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class NG911Activity extends Activity {
 	/* For SIP */
 	private SipController sipController;
 	private Handler t140Handler;
 	private T140Writer t140writer;
 	private StringBuffer t140IncomingBuffer = new StringBuffer("");
 	private CharSequence t140IncomingCharSeq = t140IncomingBuffer;
 	
 	private static mysip sip ;
 	private static String TAG = NG911Activity.class.getName();
 
 
 	private static final int PHOTO_RESULT = 4433;
 	
 	public static final int IMAGE_RECEIVED_RESULT=39485439;
 	
 	private ArrayAdapter<String> arrayAdapter; 
 	private ListView chatWindowListView;
 
 	/** Called when the activity is first created. */
 	LocationManager locationManager;
 	ConnectivityManager connectivityManager;
 	Location location;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		SipStack.debug_level=0;
 		
 
 		connectivityManager = (ConnectivityManager) this
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		alertIfNoNetwork();
 		
 		/*******************************************
 		 * Real Time Text or Normal Radio Button
 		 *******************************************/
 		RadioButton textTypeButton = (RadioButton)findViewById(R.id.RTP);
 		textTypeButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 				if (arg1)
 					Log.e("RTT_BUTTON", "yes");
 				else
 					Log.e("RTT_BUTTON", "no");
 				sipController.setIsRealTime(arg1);
 			}
 		});
 
 		
 		//initialise array Adapter
 		arrayAdapter= new ArrayAdapter<String>(this,R.layout.adapterunit);
 		chatWindowListView=(ListView)findViewById(R.id.chatListView);
 		chatWindowListView.setAdapter(arrayAdapter);
 		
 		/*******************************************
 		 * 
 		 * Camera button
 		 * 
 		 *******************************************/
 		Button cameraButton = (Button) findViewById(R.id.sendPhotoButton);
 		cameraButton.setOnClickListener(cameraButtonOnClickListener);
 
 		/********************************
 		 * SipController Initialize
 		 *******************************/
//		sipController = new SipController("test", "128.237.250.232", "5060");
 		t140Handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				char c = (char)msg.arg1;
 				t140IncomingBuffer.append(Character.toString(c));
				arrayAdapter.add((String)t140IncomingCharSeq);
				Log.e("T140", Integer.toString(msg.arg1));
 			}
 		};
 		t140writer = new T140Writer(t140Handler);
 		sipController = new SipController("test", "128.59.22.88", "5080", t140writer);
 
 		sip = new mysip(sipController.getSharedSipProvider(),this);
 		
 		Button callButton = (Button) findViewById(R.id.call);
 		callButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				System.out.println("Outgoing call");
 				sipController.call();
 			}
 		});
 
 		Button hangButton = (Button) findViewById(R.id.hang);
 		hangButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				System.out.println("Hang up call");
 				sipController.hangup();
 			}
 		});
 
 		Button sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
 		sendMessageButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 
 				// Intent for starting preview..does not take photos yet.
 
 				if (v.getId() == R.id.sendMessageButton) {
 					TextView tv = (TextView) findViewById(R.id.message);
 					String inputMessage = tv.getText().toString();
 
 					//sipController.send(inputMessage);
 					sip.send(inputMessage);
 					tv.setText("");
 					
 //					chatWindowTextView.append("\n User: "+inputMessage);
 					arrayAdapter.add("\n User: "+inputMessage);
 					
 					
 				}
 			}
 		});
 		
 		/* Jin : I moved these from onStart(), because onStart() called several times after closing camera */ 
 		sendMessageEditText = (EditText) findViewById(R.id.message);
 		sendMessageEditText.setOnKeyListener(rttTextListener);
 		sendMessageEditText.addTextChangedListener(rttTextWatcher);
 	}
 
 	private void alertIfNoNetwork() {
 		NetworkInfo[] networkInfoTrial = connectivityManager
 				.getAllNetworkInfo();
 		boolean isConnected = false;
 		for (int i = 0; i < networkInfoTrial.length; i++) {
 			if (networkInfoTrial[i].isConnected()) {
 				isConnected = true;
 			}
 		}
 		if (isConnected == false) {
 			Toast.makeText(getApplicationContext(), "network is not available",
 					Toast.LENGTH_LONG).show();
 			// AlertDialog connectedToNetwork=new
 			// AlertDialog(getBaseContext(),false,);
 			showAlertDialog("No Network Connectivity");
 			
 		}
 	}
 
 	private void showAlertDialog(String reason){
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				NG911Activity.this);
 		alertDialogBuilder.setCancelable(true);
 		alertDialogBuilder.setTitle(reason);
 		alertDialogBuilder.setMessage("Exiting app, call 911?");
 
 		alertDialogBuilder.setPositiveButton("Call 911",
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 						Intent callIntent = new Intent(Intent.ACTION_CALL);
 						callIntent.setData(Uri.parse("tel:" + 35354));
 						callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 						startActivity(callIntent);
 						// finish();
 					}
 				});
 
 		alertDialogBuilder.setNegativeButton("Exit",
 				new DialogInterface.OnClickListener() {
 
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 						dialog.dismiss();
 						finish();
 					}
 				});
 		// AlertDialog.Builder
 		// alertDialogBuilder=initializeAlertDialog("No Network Connectivity",
 		// "Exiting app..Call 911?");
 		AlertDialog alert = alertDialogBuilder.create();
 		alert.show();
 	}
 
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		if(data!=null){
 			if (!data.getExtras().isEmpty()) {
 				if (requestCode == PHOTO_RESULT) {
 	
 					Bitmap photoResult = (Bitmap) data.getExtras().get("data");
 					ImageView imageView = new ImageView(
 							this.getApplicationContext());
 					imageView.setImageBitmap(photoResult);
 					setContentView(imageView);
 	
 				}
 				if(requestCode==IMAGE_RECEIVED_RESULT){
 	//				byte[] jpegByteArray=(byte[]) data.getExtras().get(CameraCapture.JPEG_STRING);
 					Uri uri=(Uri) data.getExtras().get(CameraCapture.JPEG_STRING);
 					try {
 						InputStream is=getContentResolver().openInputStream(uri);
 						InputStreamReader isr=new InputStreamReader(is);
 						BufferedReader br=new BufferedReader(isr);
 						
 						StringBuilder sb=new StringBuilder();
 						
 						String read= br.readLine();
 						int i=0;
 						while(read!=null){
 							read=br.readLine();
 							sb.append(read);
 							sip.send(read);	
 						}
 						String jpegString=sb.toString();
 						
 						sip.send(jpegString);
 						
 						Log.e(TAG,"jpegString is: "+jpegString);
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					Log.e(TAG,"Received image result from cameraCapture class");
 					
 	//				Bitmap pictureTaken = BitmapFactory.decodeByteArray(jpegByteArray, 0,
 	//				jpegByteArray.length);
 	//				Log.e(TAG+ "onpictureTaken()","bitmap is: "+pictureTaken.toString());
 	//				ImageView imageView = new ImageView(getApplicationContext());
 	//				imageView.setImageBitmap(pictureTaken);
 	//				setContentView(imageView);
 					
 					
 				}
 			}
 		}
 	}
 
 	OnClickListener cameraButtonOnClickListener = new OnClickListener() {
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 
 			Intent intent = new Intent(getBaseContext(),
 					CameraCapture.class);
 			startActivityForResult(intent,IMAGE_RECEIVED_RESULT);
 			
 //			Android Camera API
 //			Intent intent = new Intent(
 //					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 //			startActivityForResult(intent, PHOTO_RESULT);
 
 		}
 	};
 
 	static boolean flagLostSent=false;
 	LocationListener locationListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			/*
 			 * Toast.makeText(getApplicationContext(),"location is"+location.
 			 * toString() , Toast.LENGTH_LONG).show();
 			 */
 //			if(!flagLostSent){
 				LostConnector lostConnector = LostConnector.getInstance();
 				lostConnector.setContext(getApplicationContext());
 				lostConnector.setLocation(location.getLatitude(),
 						location.getLongitude());
 				if (lostConnector.requestSent() == false) {
 					String serverIp=lostConnector.getPSAPD();
 					if(serverIp.equals("")||serverIp==null){
 						showAlertDialog("No PSAP server nearby");
 					}
 				}
 				flagLostSent=true;
 //			}
 		}
 
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 
 		}
 
 	};
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		// locationManager.removeUpdates(locationListener);
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		Log.e(TAG, "onStart()");
 		
 		try {
 			alertIfNoNetwork();
 			/**************************
 			 * 
 			 * Check for location via GPS or network provider
 			 * 
 			 **************************/
 			locationManager = (LocationManager) this
 					.getSystemService(Context.LOCATION_SERVICE);
 			boolean isGPSEnabled = locationManager
 					.isProviderEnabled(LocationManager.GPS_PROVIDER);
 			if (isGPSEnabled) {
 				locationManager
 						.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 								1000, 0, locationListener);
 
 			} else {
 				locationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, 1000, 0,
 						locationListener);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// InputMethodManager
 		// iMM=(InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
 		// iMM.showSoftInput(getCurrentFocus(),0 );
 		super.onStart();
 	}
 
 	EditText sendMessageEditText;
 
 	OnKeyListener rttTextListener = new OnKeyListener() {
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			// TODO Auto-generated method stub
 			Log.e(TAG, "onKey() of rttTextListener called");
 			
 			if (keyCode == KeyEvent.KEYCODE_0) {
 				Log.e(TAG, "caught 0");
 			}
 
 			char keyLabel = event.getDisplayLabel();
 			String keyLabelString = String.valueOf(keyLabel);
 			Log.e(TAG,
 					"onKey() event keyLabel= " + keyLabelString + " keyCode= "
 							+ event.getKeyCode() + " Unicode= "
 							+ event.getUnicodeChar());
 			if (keyCode == 67)
 				sipController.sendRTT((char) 0x08);
 			return false;
 		}
 
 	};
 	TextWatcher rttTextWatcher = new TextWatcher() {
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 			// TODO Auto-generated method stub
 			Log.e(TAG, "onTextChanged() new char sequence is " + s + " start="
 					+ start + " ,before= " + before + ",count= " + count);
 			Log.e(TAG,
 					"new charSequence is ="
 							+ s.subSequence(start, start + count));
 
 			// RTT send
 			if (count > 0)
 				sipController.sendRTT(s.charAt(start));
 
 //			chatWindowTextView.setText(s);
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void afterTextChanged(Editable s) {
 			// TODO Auto-generated method stub
 			Log.e(TAG, "afterTextChanged " + s.toString());
 		}
 	};
 
 	
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 
 		locationManager.removeUpdates(locationListener);
 	}
 }
