 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
  * law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  */
 package com.bigpupdev.synodroid.ui;
 
 import java.util.List;
 
 import com.bigpupdev.synodroid.action.DetailTaskAction;
 import com.bigpupdev.synodroid.action.SynoAction;
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.server.SynoServer;
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 
 /**
  * The base class of an activity in Synodroid
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public abstract class SynodroidFragment extends Fragment implements ResponseHandler {
 	protected List<SynoAction> postOTPActions = null;
 	
 	protected boolean otp_dialog = false;
 
 	// A generic Handler which delegate to the activity
 	private Handler handler = new Handler() {
 		// The toast message
 		@SuppressWarnings("unchecked")
 		@Override
 		public void handleMessage(Message msgP) {
 			final Activity a = SynodroidFragment.this.getActivity();
			final SynoServer server = ((Synodroid) a.getApplication()).getServer();
 			if (a != null){
 				Synodroid app = (Synodroid) a.getApplication();
 				Style msg_style = null;
 				// According to the message
 				switch (msgP.what) {
 				case ResponseHandler.MSG_CONNECT_WITH_ACTION:
 					try{
 						if (((Synodroid)a.getApplication()).DEBUG) Log.w(Synodroid.DS_TAG,"SynodroidFragment: Received connect with action message.");
 					}catch (Exception ex){/*DO NOTHING*/}
 					
 					((BaseActivity)a).showDialogToConnect(true, (List<SynoAction>) msgP.obj, true);
 					break;
 				case ResponseHandler.MSG_ERROR:
 					try{
 						if (((Synodroid)a.getApplication()).DEBUG) Log.w(Synodroid.DS_TAG,"SynodroidFragment: Received error message.");
 					}catch (Exception ex){/*DO NOTHING*/}
 					
 					// Change the title
 					((BaseActivity)a).updateSMServer(null);
 					
 					// Show the error
 					// Save the last error inside the server to surive UI rotation and
 					// pause/resume.
 					if (server != null) {
 						server.setLastError((String) msgP.obj);
 						android.view.View.OnClickListener ocl = new android.view.View.OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								if (server != null) {
 									if (!server.isConnected()) {
 										((BaseActivity) a).showDialogToConnect(false, null, false);
 									}
 								}
 								Crouton.cancelAllCroutons();
 							}
 						};
 						Crouton.makeText(getActivity(), server.getLastError()+ "\n\n" + getText(R.string.click_dismiss), Synodroid.CROUTON_ERROR).setOnClickListener(ocl).show();
 					}
 					break;
 				case ResponseHandler.MSG_OTP_REQUESTED:
 					try{
 						if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"SynodroidFragment: Received OTP Request message.");
 					}catch (Exception ex){/*DO NOTHING*/}
 					
 					postOTPActions = (List<SynoAction>)msgP.obj;
 					// Show the connection dialog
 					try {
 						((BaseActivity)a).showDialog(BaseActivity.OTP_REQUEST_DIALOG_ID);
 					} catch (Exception e) {/* Unable to show dialog probably because intent has been closed. Ignoring...*/}
 					break;
 				case ResponseHandler.MSG_CONNECTED:
 					try{
 						if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"SynodroidFragment: Received connected to server message.");
 					}catch (Exception ex){/*DO NOTHING*/}
 					
 					((BaseActivity)a).updateSMServer(server);
 					
 					break;
 				case ResponseHandler.MSG_CONNECTING:
 					try{
 						if (((Synodroid)a.getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"SynodroidFragment: Received connected to server message.");
 					}catch (Exception ex){/*DO NOTHING*/}
 					
 					((BaseActivity)a).updateSMServer(null);
 					
 					break;
 				case MSG_OPERATION_PENDING:
 					if (app != null && app.DEBUG) Log.v(Synodroid.DS_TAG,"SynodroidFragment: Received operation pending message.");
 					if (a instanceof HomeActivity){
 						((HomeActivity) a).updateRefreshStatus(true);
 					}
 					else if (a instanceof DetailActivity){
 						((DetailActivity) a).updateRefreshStatus(true);
 					}
 					else if (a instanceof SearchActivity){
 						((SearchActivity) a).updateRefreshStatus(true);
 					}
 					else if (a instanceof FileActivity){
 						((FileActivity) a).updateRefreshStatus(true);
 					}
 					else if (a instanceof BrowserActivity){
 						((BrowserActivity) a).updateRefreshStatus(true);
 					}
 					break;
 				case MSG_INFO:
 					if (msg_style == null) msg_style = Synodroid.CROUTON_INFO;
 				case MSG_ALERT:
 					if (msg_style == null) msg_style = Synodroid.CROUTON_ALERT;
 				case MSG_ERR:
 					if (msg_style == null) msg_style = Synodroid.CROUTON_ERROR;
 				case MSG_CONFIRM:
 					if (msg_style == null) msg_style = Synodroid.CROUTON_CONFIRM;
 					if (app != null && app.DEBUG) Log.v(Synodroid.DS_TAG,"SynodroidFragment: Received toast message.");
 					final String text = (String) msgP.obj;
 					Runnable runnable = new Runnable() {
 						public void run() {
 							Crouton.makeText(a, text, Synodroid.CROUTON_CONFIRM).show();
 						}
 						};
 					a.runOnUiThread(runnable);
 					break;
 				default:
 					if (app != null && app.DEBUG) Log.v(Synodroid.DS_TAG,"SynodroidFragment: Received default message.");
 					if (a instanceof HomeActivity){
 						((HomeActivity) a).updateRefreshStatus(false);
 					}
 					else if (a instanceof DetailActivity){
 						((DetailActivity) a).updateRefreshStatus(false);
 					}
 					else if (a instanceof SearchActivity){
 						((SearchActivity) a).updateRefreshStatus(false);
 					}
 					else if (a instanceof FileActivity){
 						((FileActivity) a).updateRefreshStatus(false);
 					}
 					else if (a instanceof BrowserActivity){
 						((BrowserActivity) a).updateRefreshStatus(false);
 					}
 					break;
 				}
 				// Delegate to the sub class in case it have something to do
 				SynodroidFragment.this.handleMessage(msgP);
 			}
 		}
 	};
 
 	public void onResume(){
 		super.onResume();
 		Activity a = getActivity();
 		Synodroid app = (Synodroid) a.getApplication();
 		try{
 			if (app.DEBUG) Log.v(Synodroid.DS_TAG,"DetailMain: Resuming server.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		SynoServer server = app.getServer();
 		if (server != null){
 			server.bindResponseHandler(this);
 		}
 		((BaseActivity) a).updateSMServer(server);
 		
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// ignore orientation change
 		super.onConfigurationChanged(newConfig);
 	}
 
 	/**
 	 * Activity creation
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.bigpupdev.synodroid.common.protocol.ResponseHandler#handleReponse( android .os.Message)
 	 */
 	public final void handleReponse(Message msgP) {
 		handler.sendMessage(msgP);
 	}
 
 	/**
 	 * Handle the message from a none UI thread. It is safe to interact with the UI in this method
 	 */
 	public abstract void handleMessage(Message msgP);
 	
 	@Override
 	public void onDestroy(){
 		Crouton.cancelAllCroutons();
 		super.onDestroy();
 		
 	}
 	
 	public void setAlreadyCanceled(boolean value){
 		((BaseActivity) getActivity()).setAlreadyCanceled(value);
 	}
 	
 	public void showDialogToConnect(boolean autoConnectIfOnlyOneServerP, final List<SynoAction> actionQueueP, final boolean automated){
 		((BaseActivity) getActivity()).showDialogToConnect(autoConnectIfOnlyOneServerP, actionQueueP, automated);
 	}
 	
 	public List<SynoAction> getPostOTPActions(){
 		return postOTPActions;
 	}
 	
 	public void resetPostOTPActions(){
 		postOTPActions = null;
 	}
 	
 	public void setOTPDialog(boolean otp){
 		otp_dialog = otp;
 	}
 }
