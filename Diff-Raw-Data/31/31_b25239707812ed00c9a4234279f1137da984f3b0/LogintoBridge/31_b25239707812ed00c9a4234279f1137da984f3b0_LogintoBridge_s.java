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
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 
 import javax.net.ssl.SSLProtocolException;
 import javax.net.ssl.SSLSocket;
 
 import com.google.protobuf.InvalidProtocolBufferException;
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
 	private EditText mNetIdDText;
 	private EditText mNetIdRText;
 	private EditText mAuthText;
 	private EditText mCarSeats;
 	private String mCarNum;
 	private CarsGtBDbAdapter mDbHelper;
 	private static final int CarNum_SELECT = 0;
 	private LogintoBridge self;
 	private ProgressDialog mProgBar = null;
 	private String exceptionalMessage = "";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		this.self = this;
 
 		// Re-establish connections to databases
 		mDbHelper = new CarsGtBDbAdapter(this);
 
 		setContentView(R.layout.cars);
 		setTitle(R.string.app_name);
 
 		TextView carnumtext = (TextView) findViewById(R.id.carnum);
 		carnumtext.setClickable(true);// Sets the text to be clickable
 
 		carnumtext.setOnClickListener(new View.OnClickListener()
 		{
 
 			public void onClick(View view)
 			{
 				Intent i = new Intent(self, CarNumList.class);
 				startActivityForResult(i, CarNum_SELECT);
 			}
 
 		});
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		if (mProgBar != null && mProgBar.isShowing())
 			mProgBar.cancel();
 
 		if (mDbHelper != null && !mDbHelper.isClosed())
 			mDbHelper.close();
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent)
 	{
 		super.onActivityResult(requestCode, resultCode, intent);
 		Log.v(TAG, "On Return");
 
 		mDbHelper.open();
 		int nCarNum = mDbHelper.getCar();
 		mCarNum = Integer.toString(nCarNum);
 		mDbHelper.close();
 		/*
 		 * We want mCarNum to be either negative or to store another value in
 		 * the Db if we failed to retrieve the number of cars. We want this
 		 * Activity to return to its initial state if we fail and to proceed to
 		 * the login layout if we're successful.
 		 */
 		if (nCarNum > 0)
 		{
 			Log.v(TAG, "Ret Car Number: " + mCarNum);
 			setContentView(R.layout.main);
 			setTitle(R.string.app_name);
 
 			TextView carnumtext = (TextView) findViewById(R.id.carnum);
 			carnumtext.setClickable(true);// Sets the text to be clickable
 
 			mAuthText = (EditText) findViewById(R.id.authkey);
 			mNetIdDText = (EditText) findViewById(R.id.netidD);
 			mNetIdRText = (EditText) findViewById(R.id.netidR);
 			mCarSeats = (EditText) findViewById(R.id.carseats);
 
 			Button loginButton = (Button) findViewById(R.id.login);
 
 			loginButton.setOnClickListener(new View.OnClickListener()
 			{
 
 				public void onClick(View view)
 				{
 					Log.v(TAG, "CarSeats: '" + mCarSeats.getText().toString()
 							+ "' Regex match: "
 							+ mCarSeats.getText().toString().matches("[0-9]"));
 					if (mCarSeats.getText().toString().matches("[0-9]"))
 						new AuthTask().execute();
 					else {
 						AlertDialog.Builder msgBox = new AlertDialog.Builder(
 								self);
 						msgBox.setMessage("Please make sure you enter"
 								+ " the number of seats in your car."
 								+ " Thank you!");
 						msgBox.setPositiveButton("Ok",
 								new DialogInterface.OnClickListener()
 						{
 									public void onClick(DialogInterface dialog,
 											int id) 
 									{
 									}
 								});
 						msgBox.show();
 					}
 				}
 
 			});
 			TextView showcarnum = (TextView) findViewById(R.id.showcarnum);
 			showcarnum.setText("You Selected Car Number: " + mCarNum);
 
 			carnumtext.setOnClickListener(new View.OnClickListener()
 			{
 
 				public void onClick(View view)
 				{
 					Intent i = new Intent(self, CarNumList.class);
 					startActivityForResult(i, CarNum_SELECT);
 				}
 
 			});
 		} else
 		{
 			setContentView(R.layout.cars);
 			setTitle(R.string.app_name);
 
 			TextView carnumtext = (TextView) findViewById(R.id.carnum);
 			carnumtext.setClickable(true);// Sets the text to be clickable
 
 			carnumtext.setOnClickListener(new View.OnClickListener()
 			{
 
 				public void onClick(View view)
 				{
 					Intent i = new Intent(self, CarNumList.class);
 					startActivityForResult(i, CarNum_SELECT);
 				}
 
 			});
 		}
 	}
 
 	private class AuthTask extends AsyncTask<Void, Integer, Integer>
 	{
 		static final int INCREMENT_PROGRESS = 20;
 		private SSLSocket aSock = null;
 
 		protected void onPreExecute() {
 			mProgBar = new ProgressDialog(self);
 			mProgBar.setCancelable(true);
 			mProgBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgBar.setMessage("Establishing Connection with server...");
 			mProgBar.show();
 		}
 
 		@Override
 		protected Integer doInBackground(Void... params)
 		{
 			int nRetVal = sendAuthCheck(mNetIdDText, mNetIdRText, mAuthText,
 					mCarSeats, mCarNum);
 			return nRetVal;
 		}
 
 		public int sendAuthCheck(EditText netid1, EditText netid2,
 				EditText authcode, EditText carseats, String carnum)
 		{
 			Request aPBReq = null;
 			Response aPBRes = null;
 			GtBSSLSocketFactoryWrapper aSSLSF = null;
 			try {
 				aSSLSF = new GtBSSLSocketFactoryWrapper(self);
 			} catch (UnrecoverableKeyException e1)
 			{
 				exceptionalMessage = "We ran into an unrecoverable key"
 						+ " exception. Please notify the IT Officer. Sorry.";
 				cancel(true);
 			} catch (KeyStoreException e1)
 			{
 				exceptionalMessage = "We couldn't find or open the KeyStore."
 						+ "This is manditory to use this app so please notify "
 						+ "the IT Officer. Sorry.";
 				cancel(true);
 			} catch (NoSuchAlgorithmException e1)
 			{
 				exceptionalMessage = "This tablet doesn't support an "
 						+ "algorithm we need to use. Please notify the "
 						+ "IT Officer so it can be updated. Sorry.";
 				cancel(true);
 			} catch (SignalException e1)
 			{
 				exceptionalMessage = "We appear to have low signal strength."
 						+ " We can't connect right now, sorry.";
 				cancel(true);
 			} catch (GTBSSLSocketException e1)
 			{
 				exceptionalMessage = e1.getMessage();
 				cancel(true);
 			}
 			/* Return error code and handle */
 			if (isCancelled())
 				return -1;
 
 			publishProgress(INCREMENT_PROGRESS);
 			try {
 				aSock = aSSLSF.getSSLSocket();
 			} catch (GTBSSLSocketException e)
 			{
 				exceptionalMessage = "We could not connect to the server! :("
 						+ " Do we currently have 3G service?";
 				cancel(true);
 				/* Return error code and handle */
 				if (isCancelled())
 					return -1;
 			}
 			if (aSock.isClosed())
 			{
 				Log.w(TAG, "Socket IS closed!");
 				try
 				{
 					aSock = aSSLSF.createSSLSocket(self);
 				} catch (UnrecoverableKeyException e1)
 				{
 					exceptionalMessage = "We ran into an unrecoverable key"
 							+ " exception. Please notify the IT Officer. Sorry.";
 					cancel(true);
 				} catch (KeyStoreException e1)
 				{
 					exceptionalMessage = "We couldn't find or open the KeyStore."
 							+ "This is manditory to use this app so please notify "
 							+ "the IT Officer. Sorry.";
 					cancel(true);
 				} catch (NoSuchAlgorithmException e1)
 				{
 					exceptionalMessage = "This tablet doesn't support an "
 							+ "algorithm we need to use. Please notify the "
 							+ "IT Officer so it can be updated. Sorry.";
 					cancel(true);
 				} catch (SignalException e1)
 				{
 					exceptionalMessage = "We appear to have low signal strength. "
 							+ "We can't connect right now, sorry.";
 					cancel(true);
 				} catch (GTBSSLSocketException e1)
 				{
 					exceptionalMessage = e1.getMessage();
 					cancel(true);
 				}
 			}
 
 			/* Return error code and handle */
 			if (isCancelled())
 				return -5;
 
 			if (aSock.isOutputShutdown())
 			{
 				Log.w(TAG, "We just opened the socket but Output Stream"
 						+ " is Shutdown!");
 
 				try {
 					if (aSock != null)
 						aSock.close();
 					aSock = aSSLSF.createSSLSocket(self);
 				} catch (UnrecoverableKeyException e1)
 				{
 					exceptionalMessage = "We ran into an unrecoverable key exception."
 							+ " Please notify the IT Officer. Sorry.";
 					cancel(true);
 				} catch (KeyStoreException e1)
 				{
 					exceptionalMessage = "We couldn't find or open the KeyStore."
 							+ "This is manditory to use this app so"
 							+ " please notify the IT Officer. Sorry.";
 					cancel(true);
 				} catch (NoSuchAlgorithmException e1)
 				{
 					exceptionalMessage = "This tablet doesn't support an algorithm we"
 							+ " need to use. Please notify the "
 							+ "IT Officer so it can be updated. Sorry.";
 					cancel(true);
 				} catch (SignalException e1)
 				{
 					exceptionalMessage = "We appear to have low signal strength. "
 							+ "We can't connect right now, sorry.";
 					cancel(true);
 				} catch (GTBSSLSocketException e)
 				{
 					exceptionalMessage = "We could not connect to the server! :("
 							+ " Do we currently have 3G service?";
 					cancel(true);
 				} catch (IOException e)
 				{
 					exceptionalMessage = "We could not connect to the server! :("
 							+ " Do we currently have 3G service?";
 					cancel(true);
 				}
 				/* Return error code and handle */
 				if(isCancelled())
 					return -5;
 			}
 
 			if (aSSLSF.getSession() != null)
 				Log.v(TAG, "Session is still valid");
 			else
 			{
 				Log.w(TAG, "Session is NO LONGER VALID");
 				try
 				{
 					aSSLSF = new GtBSSLSocketFactoryWrapper(self);
 				} catch (UnrecoverableKeyException e1)
 				{
 					exceptionalMessage = "We ran into an unrecoverable key"
 							+ " exception. Please notify the IT Officer. Sorry.";
 					cancel(true);
 				} catch (KeyStoreException e1)
 				{
 					exceptionalMessage = "We couldn't find or open the KeyStore."
 							+ "This is manditory to use this app so"
 							+ " please notify the IT Officer. Sorry.";
 					cancel(true);
 				} catch (NoSuchAlgorithmException e1)
 				{
 					exceptionalMessage = "This tablet doesn't support an "
 							+ "algorithm we need to use. Please notify the "
 							+ "IT Officer so it can be updated. Sorry.";
 					cancel(true);
 				} catch (SignalException e1)
 				{
 					exceptionalMessage = "We appear to have low signal"
 							+ " strength. We can't connect right now, sorry.";
 					cancel(true);
 				} catch (GTBSSLSocketException e1)
 				{
 					exceptionalMessage = e1.getMessage();
 					cancel(true);
 				}
 
 				/* Return error code and handle */
 				if(isCancelled())
 					return -5;
 
 				try
 				{
 					aSock = aSSLSF.getSSLSocket();
 				} catch (GTBSSLSocketException e)
 				{
 					exceptionalMessage = "We could not connect to the server! :("
 							+ " Do we currently have 3G service?";
 					cancel(true);
 				}
 				/* Return error code and handle */
 				if(isCancelled())
 					return -5;
 			}
 
 			publishProgress(INCREMENT_PROGRESS);
 			try
 			{
 				OutputStream aOS = null;
 				try
 				{
 					aOS = aSock.getOutputStream();
 				} catch (IOException e)
 				{
 					try
 					{
 						if (aSock != null)
 							aSock.close();
 						aSSLSF.forceReHandshake(self);
 						aSock = aSSLSF.getSSLSocket();
 						aOS = aSock.getOutputStream();
 					} catch (UnrecoverableKeyException e1)
 					{
 						exceptionalMessage = "We ran into an unrecoverable key"
 								+ " exception. Please notify the IT Officer. Sorry.";
 						cancel(true);
 					} catch (KeyStoreException e1)
 					{
 						exceptionalMessage = "We couldn't find or open the KeyStore."
 								+ "This is manditory to use this "
 								+ "app so please notify the IT Officer. Sorry.";
 						cancel(true);
 					} catch (NoSuchAlgorithmException e1)
 					{
 						exceptionalMessage = "This tablet doesn't support an algorithm "
 								+ "we need to use. Please notify the "
 								+ "IT Officer so it can be updated. Sorry.";
 						cancel(true);
 					} catch (SignalException e1)
 					{
 						exceptionalMessage = "We appear to have low signal strength. "
 								+ "We can't connect right now, sorry.";
 						cancel(true);
 					} catch (GTBSSLSocketException e1)
 					{
 						exceptionalMessage = e1.getMessage();
 						cancel(true);
 					}
 
 					/* Return error code and handle */
 					if(isCancelled())
 						return -5;
 				}
 
 				aPBReq = Request
 						.newBuilder()
 						.setNReqId(2)
 						.setSReqType("AUTH")
 						.addSParams(netid1.getText().toString())
 						.addSParams(netid2.getText().toString())
 						.addSParams(authcode.getText().toString())
 						.addSParams(carnum)
 						.addNParams(
 								Integer.parseInt(carseats.getText().toString()))
 						.build();
 				Log.v(TAG, "Number of Params: " + aPBReq.getSParamsCount());
 				Log.v(TAG, "Nightly Key: " + authcode.getText().toString());
 				Log.v(TAG, "Driver NetID: " + netid1.getText().toString());
 				Log.v(TAG, "Ride-Along NetID: " + netid2.getText().toString());
 				Log.v(TAG, "Seats in car: " + carseats.getText().toString());
 				Log.v(TAG, "Car Number: " + carnum);
 				Log.v(TAG, "Serialized Size: " + aPBReq.getSerializedSize());
 				Log.v(TAG, "Sending Buf:");
 				Log.v(TAG, TextFormat.shortDebugString(aPBReq));
 				if (aSock.isConnected())
 				{
 					try
 					{
 						aOS.write(aPBReq.getSerializedSize());
 					} catch (SSLProtocolException e)
 					{
 						Log.e(TAG, "SSLProtoclException Caught. On-write to"
 								+ " Output Stream");
 						try {
 							aSSLSF.forceReHandshake(self);
 						} catch (UnrecoverableKeyException e1)
 						{
 							exceptionalMessage = "We ran into an unrecoverable key "
 									+ "exception. "
 									+ "Please notify the IT Officer. Sorry.";
 							cancel(true);
 						} catch (KeyStoreException e1)
 						{
 							exceptionalMessage = "We couldn't find or open the KeyStore."
 									+ "This is manditory to use this app so"
 									+ " please notify the IT Officer. Sorry.";
 							cancel(true);
 						} catch (NoSuchAlgorithmException e1)
 						{
 							exceptionalMessage = "This tablet doesn't support an "
 									+ "algorithm we need to use. Please "
 									+ "notify the IT Officer so it can "
 									+ "be updated. Sorry.";
 							cancel(true);
 						} catch (SignalException e1)
 						{
 							exceptionalMessage = "We appear to have low signal strength. "
 									+ "We can't connect right now, sorry.";
 							cancel(true);
 						} catch (GTBSSLSocketException e1)
 						{
 							exceptionalMessage = e1.getMessage();
 							cancel(true);
 						}
 
 						/* Return error code and handle */
 						if(isCancelled())
 							return -5;
 
 						try
 						{
 							aSock = aSSLSF.getSSLSocket();
 						} catch (GTBSSLSocketException ex) {
 							exceptionalMessage = "We could not connect to the server! :("
 									+ " Do we currently have 3G service?";
 							cancel(true);
 						}
 						aOS = aSock.getOutputStream();
 						try
 						{
 							aOS.write(aPBReq.getSerializedSize());
 						} catch (SSLProtocolException ex) {
 							try {
 								aSSLSF.loadStores();
 								aSSLSF.createConnection();
 
 								aSock = aSSLSF.getSSLSocket();
 								aOS = aSock.getOutputStream();
 								aOS.write(aPBReq.getSerializedSize());
 							} catch (UnrecoverableKeyException e1) {
 								exceptionalMessage = "We ran into an unrecoverable key"
 										+ " exception. "
 										+ "Please notify the IT Officer. Sorry.";
 								cancel(true);
 							} catch (KeyStoreException e1) {
 								exceptionalMessage = "We couldn't find or open the"
 										+ " KeyStore. This is manditory to"
 										+ " use this app so please notify"
 										+ " the IT Officer. Sorry.";
 								cancel(true);
 							} catch (NoSuchAlgorithmException e1) {
 								exceptionalMessage = "This tablet doesn't support an "
 										+ "algorithm we need to use. "
 										+ "Please notify the IT Officer"
 										+ " so it can be updated. Sorry.";
 								cancel(true);
 							} catch (SignalException e1) {
 								exceptionalMessage = "We appear to have low signal"
 										+ " strength. We can't connect"
 										+ " right now, sorry.";
 								cancel(true);
 							} catch (GTBSSLSocketException e1) {
 								exceptionalMessage = e1.getMessage();
 								cancel(true);
 							}
 
 							/* Return error code and handle */
 							if(isCancelled())
 								return -5;
 						}
 					}
 				} else
 				{
 					exceptionalMessage = "We could not connect to the server! :("
 							+ " Do we currently have 3G service?";
 					cancel(true);
 				}
 				/* Return error code and handle */
 				if(isCancelled())
 					return -5;
 
 				publishProgress(INCREMENT_PROGRESS);
 				if (aSock.isConnected())
 					aPBReq.writeTo(aOS);
 				else
 				{
 					Log.v(TAG, "Server-side closed early. Watchdog effect?");
 					exceptionalMessage = "Our connection to the server was"
 							+ "broken! :(" + " Do we still have 3G service?";
 					cancel(true);
 					/* Return error code and handle */
 					return -5;
 				}
 				aOS.close();
 				InputStream aIS = aSock.getInputStream();
 				byte[] vbuf = new byte[14];
 				aIS.read(vbuf);
 				// Server Side is already closed
 				if (aSock != null)
 					aSock.close();
 
 				try
 				{
 					aPBRes = Response.parseFrom(vbuf);
 				} catch (InvalidProtocolBufferException e)
 				{
 					Log.w(TAG, "We received an invalid buf! Failing.");
 					exceptionalMessage = "We asked for the number of cars but"
 							+ " we got garbage as a reply. Try again soon.";
 					cancel(true);
 					/* Return error code and handle */
 					return -1;
 				}
 				publishProgress(INCREMENT_PROGRESS);
 				aIS.close();
 				/*
 				 * TODO Per the comment made on dealwitherrors, we should return
 				 * the Response here.
 				 */
 				return aPBRes.getNRespId();
 			} catch (IOException e)
 			{
 				Log.e(TAG, "Connection to server could not be established.");
 				return -2;
 			}
 		}
 
 		protected void onPostExecute(Integer res) {
 			try {
 				if (aSock != null)
 					aSock.close();
 			} catch (IOException e) {
 			}
 			publishProgress(INCREMENT_PROGRESS);
 			if (res != 0) {
 				dealwitherrors(res);
 			} else 
 			{
 				mProgBar.dismiss();
 				Intent i = new Intent(self, GuardtheBridge.class);
 				startActivity(i);
 			}
 		}
 
 		protected void onProgressUpdate(Integer... progress) {
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
 
 		protected void onCancelled() {
 			try
 			{
 				if (aSock != null)
 					aSock.close();
 			} catch (IOException e) 
 			{
 			}
 			mProgBar.dismiss();
 			if (exceptionalMessage.compareTo("") != 0) {
 				AlertDialog.Builder msgBox = new AlertDialog.Builder(self);
 				msgBox.setMessage(exceptionalMessage + "\n\n Would you"
 						+ " like to continue without a connection to the"
 						+ " server? This will be must more annoying "
 						+ "because you will be asked this question every time"
 						+ " we need to connect to the server.");
 				msgBox.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								mProgBar.dismiss();
 								Intent i = new Intent(self,
 										GuardtheBridge.class);
 								startActivity(i);
 							}
 						});
 				msgBox.setNegativeButton("No",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								new AlertDialog.Builder(self)
 										.setMessage("Sorry for the inconvenience. "
 												+ "GUARD the Bridge is now exiting.");
 								try {
 									Thread.sleep(5000);
 								} catch (InterruptedException e) {
 									finish();
 								}
 								finish();
 							}
 						});
 				msgBox.show();
 			}
 			exceptionalMessage = "";
 		}
 
 		/*
 		 * TODO It may be better if we generate these error messages
 		 * server-side. This would result in a lot more flexibility and not
 		 * being restricted to error codes.
 		 */
 		public void dealwitherrors(int retval)
 		{
 			mProgBar.dismiss();
 			switch (retval) {
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
