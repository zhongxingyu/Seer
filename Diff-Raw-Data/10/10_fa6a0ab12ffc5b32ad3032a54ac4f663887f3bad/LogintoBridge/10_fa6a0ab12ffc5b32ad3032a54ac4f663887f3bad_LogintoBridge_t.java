 /*
  * GUARD the Bridge
  * Copyright (C) 2012  Matthew Finkel <Matthew.Finkel@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package edu.uconn.guarddogs.guardthebridge;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.net.ssl.SSLProtocolException;
 import javax.net.ssl.SSLSocket;
 
 import com.google.protobuf.TextFormat;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import edu.uconn.guarddogs.guardthebridge.Communication.Request;
 import edu.uconn.guarddogs.guardthebridge.Communication.Response;
 
 public class LogintoBridge extends ListActivity {
 	private static final String TAG = "LIB-GTBLOG";
 	private EditText mNetIdText;
     private EditText mAuthText;
     private String mCarNum;
     private CarsGtBDbAdapter mDbHelper;
     private static final int CarNum_SELECT=0;
     private LogintoBridge self;
     private ProgressDialog mProgBar = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		this.self = this;
 		
 		//Re-establish connections to databases
 		mDbHelper = new CarsGtBDbAdapter(this);
         
         setContentView(R.layout.cars);
         setTitle(R.string.app_name);
 
         TextView carnumtext = (TextView) findViewById(R.id.carnum);
         carnumtext.setClickable(true);//Sets the text to be clickable
         
         carnumtext.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
             	Intent i = new Intent(self, CarNumList.class);
             	startActivityForResult(i, CarNum_SELECT);
             }
 
         });
 	}
 	
 	 protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 	        super.onActivityResult(requestCode, resultCode, intent);
 	        Log.v(TAG, "On Return");
 	        
 	        setContentView(R.layout.main);
 	        setTitle(R.string.app_name);
 	        
 	        TextView carnumtext = (TextView) findViewById(R.id.carnum);
 	        carnumtext.setClickable(true);//Sets the text to be clickable
 	        
 	        mAuthText = (EditText) findViewById(R.id.authkey);
 	        mNetIdText = (EditText) findViewById(R.id.netid);
 	        
 	        Button loginButton = (Button) findViewById(R.id.login);
 	        
 	        
 	        loginButton.setOnClickListener(new View.OnClickListener() {
 
 	            public void onClick(View view) {
 	            	new AuthTask().execute();
 	            }
 
 	        });
	        mDbHelper.open();
 	        mCarNum = Integer.toString(mDbHelper.getCar());
	        mDbHelper.close();
 	        Log.v(TAG, "Ret Car Number: " + mCarNum);
 	        TextView showcarnum = (TextView)findViewById(R.id.showcarnum);
 	        showcarnum.setText("You Selected Car Number: " + mCarNum);
 	        
 	        carnumtext.setOnClickListener(new View.OnClickListener() {
 
 	            public void onClick(View view) {
 	            	Intent i = new Intent(self, CarNumList.class);
 	            	startActivityForResult(i, CarNum_SELECT);
 	            }
 
 	        });
 	    }
 	 
 	 private class AuthTask extends AsyncTask<Void, Integer, Integer>
 	 {
		 static final int INCREMENT_PROGRESS = 20;
 		 protected void onPreExecute()
 		 {
 			 mProgBar = new ProgressDialog(self);
 			 mProgBar.setCancelable(true);
 			 mProgBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			 mProgBar.setMessage("Establishing Connection with server...");
 			 mProgBar.show();
 		 }
 		 
 		 @Override
 		 protected Integer doInBackground(Void... params)
 		 {
 			 int nRetVal = sendAuthCheck(mNetIdText, mAuthText, mCarNum);
 			 return nRetVal;
 		 }
  
 		 public int sendAuthCheck(EditText netid, EditText authcode, String carnum)
 		 {
 			 Request aPBReq;
 			 Response aPBRes;
 			 GtBSSLSocketFactoryWrapper aSSLSF = new GtBSSLSocketFactoryWrapper();
 			 
 			 publishProgress(INCREMENT_PROGRESS);
 			 SSLSocket aSock = aSSLSF.getSSLSocket();
 			 if(aSock.isClosed())
 			 {
 				 aSock = aSSLSF.createSSLSocket(self);
 			 }
 			 if (aSock.getSession() == null)
 			 {
 				 aSSLSF = aSSLSF.getNewSSLSFW(self);
 				 aSock = aSSLSF.getSSLSocket();
 			 }
 			 
 			 publishProgress(INCREMENT_PROGRESS);
 			 try {
 				 OutputStream aOS = null;
 				 aOS = aSock.getOutputStream();
 				 aPBReq = Request.newBuilder().
 						 setNReqId(2).
 						 setSReqType("AUTH").
 						 addSParams(netid.getText().toString()).
 						 addSParams(authcode.getText().toString()).
 						 addSParams(carnum).
 						 build();
 				 Log.v(TAG, "Number of Params: " + aPBReq.getSParamsCount());
 				 Log.v(TAG, "Nightly Key: " + authcode.getText().toString());
 				 Log.v(TAG, "NetID: " + netid.getText().toString());
 				 Log.v(TAG, "Car Number: " + carnum);
 				 Log.v(TAG, "Serialized Size: " + aPBReq.getSerializedSize());
 				 Log.v(TAG, "Sending Buf:");
 				 Log.v(TAG, TextFormat.shortDebugString(aPBReq));
 				 try
 				 {
 					 aOS.write(aPBReq.getSerializedSize());
 				 } catch (SSLProtocolException e)
 				 {
 					 Log.e(TAG, "SSLProtoclException Caught. On-write to Output Stream");
 					 aSSLSF.forceReHandshake(self);
 					 aSock = aSSLSF.getSSLSocket();
 					 aOS = aSock.getOutputStream();
 					 try
 					 {
 						 aOS.write(aPBReq.getSerializedSize());
 					 } catch (SSLProtocolException ex)
 					 {
 						 aSSLSF = aSSLSF.getNewSSLSFW(self);
 						 aSock = aSSLSF.getSSLSocket();
 						 aOS = aSock.getOutputStream();
 						 aOS.write(aPBReq.getSerializedSize());
 					 }
 				 }
 				 publishProgress(INCREMENT_PROGRESS);
 				 aPBReq.writeTo(aOS);
 				 aOS.close();
 				 InputStream aIS = aSock.getInputStream();
 				 byte[] vbuf = new byte[14];
 				 aIS.read(vbuf);
 	
 				 aSock.close();
 			 	aPBRes = Response.parseFrom(vbuf);
 			 	publishProgress(INCREMENT_PROGRESS);
 			 	aIS.close();
 			 	return aPBRes.getNRespId();
 			 } catch (IOException e){
 				 Log.e(TAG, "Connection to server could not be established.");
 				 return -2;
 			 }
 		 }
 	 
 		 protected void onPostExecute(Integer res)
 		 {
 			 publishProgress(INCREMENT_PROGRESS);
 			 if(res != 0)
 			 {
 				 dealwitherrors(res);
 			 }
 			 else{
				 
 				 mProgBar.dismiss();
 				 Intent i = new Intent (self, GuardtheBridge.class);
 				 startActivity(i);
 			 }
 		 }
 	 	
 		 protected void onProgressUpdate(Integer... progress)
 		 {
 			 int nTotalProgress = mProgBar.getProgress() + progress[0];
 			 switch (nTotalProgress)
 			 {
 			 case 0:
 			 case 20:
 				 mProgBar.setMessage("Establishing Connection with server...");
 				 break;
 			 case 40:
 				 mProgBar.setMessage("Connection Established, Sending credentials...");
 				 break;
 			 case 60:
 				 mProgBar.setMessage("Receiving response...");
 				 break;
 			 case 80:
 				 mProgBar.setMessage("Reading response...");
 				 break;
 			 case 100:
 				 mProgBar.setMessage("Done!");
 				 break;
 			 }		
 			 mProgBar.setProgress(nTotalProgress);
 		 }
 		 
 		 public void dealwitherrors(int retval)
 		 {
 			 mProgBar.dismiss();
 			 switch (retval)
 			 {
 			 case -1:
 				 getDialog("I'm sorry, but please retype the authentication code." 
 						 + "The one you entered could not be verified");
 				 break;
 			 case -2:
 				 getDialog("I'm sorry, but please retype your NetID." 
 						 + "The one you entered could not be verified");
 				 break;
 			 case -3:
 				 
 			 case -4:
 				 
 			 default:
 				 getDialog("I'm sorry, but an unknown error occurred. " +
 						 "Please call dispatch/the supervisor if this persists.");
 				 break;
 			 }
 		 }
 
 		 private void getDialog(String msg){
 			 AlertDialog.Builder builder = new AlertDialog.Builder(self);
 			 builder.setMessage(msg);
 			 builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				 public void onClick(DialogInterface dialog, int id) {
 					 return;
 				 }
 			 });
 			 builder.show();
 		 }
 	 }
 }
