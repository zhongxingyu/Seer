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
 
 import com.bigpupdev.synodroid.protocol.ResponseHandler;
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * The base class of an activity in Synodroid
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public abstract class SynodroidFragment extends Fragment implements ResponseHandler {
 	// The error dialog
 	private AlertDialog errorDialog;
 	// The error dialog listener
 	private DialogInterface.OnClickListener errorDialogListener;
 
 	// A generic Handler which delegate to the activity
 	private Handler handler = new Handler() {
 		// The toast message
 		@Override
 		public void handleMessage(Message msgP) {
			Activity a = SynodroidFragment.this.getActivity();
 			if (a != null){
 				Synodroid app = (Synodroid) a.getApplication();
 				// According to the message
 				switch (msgP.what) {
 				case MSG_OPERATION_PENDING:
 					if (app != null && app.DEBUG) Log.d(Synodroid.DS_TAG,"SynodroidFragment: Received operation pending message.");
 					if (a instanceof HomeActivity){
 						((HomeActivity) a).updateRefreshStatus(true);
 					}
 					else if (a instanceof DetailActivity){
 						((DetailActivity) a).updateRefreshStatus(true);
 					}
 					break;
 				case MSG_TOAST:
 					if (app != null && app.DEBUG) Log.d(Synodroid.DS_TAG,"SynodroidFragment: Received toast message.");
					String text = (String) msgP.obj;
					Toast toast = Toast.makeText(a, text, Toast.LENGTH_SHORT);
					toast.show();
 					break;
 				default:
 					if (app != null && app.DEBUG) Log.d(Synodroid.DS_TAG,"SynodroidFragment: Received default message.");
 					if (a instanceof HomeActivity){
 						((HomeActivity) a).updateRefreshStatus(false);
 					}
 					else if (a instanceof DetailActivity){
 						((DetailActivity) a).updateRefreshStatus(false);
 					}
 					break;
 				}
 				// Delegate to the sub class in case it have something to do
 				SynodroidFragment.this.handleMessage(msgP);
 			}
 		}
 	};
 
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
 
 		createDialogs();
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
 
 	/**
 	 * Create all required dialogs
 	 */
 	private void createDialogs() {
 		// The error dialog
 		Activity a = getActivity();
 		AlertDialog.Builder builder = new AlertDialog.Builder(a);
 		// By default the message is "Error Unknown"
 		builder.setMessage(R.string.err_unknown);
 		builder.setTitle(getString(R.string.connect_error_title)).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 				// If a listener as been defined
 				if (errorDialogListener != null) {
 					errorDialogListener.onClick(dialog, id);
 				}
 			}
 		});
 		errorDialog = builder.create();
 	}
 
 	/**
 	 * Show an error message
 	 * 
 	 * @param msgP
 	 *            The message to display
 	 * @param listenerP
 	 *            A listener which will be called when the user will click on the OK button
 	 */
 	public void showError(String msgP, DialogInterface.OnClickListener listenerP) {
 		try{
 			if (((Synodroid)SynodroidFragment.this.getActivity().getApplication()).DEBUG) Log.d(Synodroid.DS_TAG,"SynodroidFragment: Showing error message.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		try {
 			errorDialog.setMessage(msgP);
 			errorDialogListener = listenerP;
 			errorDialog.show();
 		} catch (Exception e) {
 			// Being unable to show the dialog, maybe the application does not have focus anymore...
 		}
 	}
 
 }
