 package de.electricdynamite.pasty;
 
 /*
  *  Copyright 2012-2013 Philipp Geschke
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 
 import de.electricdynamite.pasty.PastyLoader.PastyResponse;
 
 
 public class AddItemFragment extends SherlockDialogFragment {
 	private static final String TAG = AddItemFragment.class.toString();
 	protected PastyPreferencesProvider prefs;
 	protected Context context;
 	protected EditText mNewItemET;
 	private boolean LOCAL_LOG;
 	
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	@Override
 	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if(PastySharedStatics.LOCAL_LOG == true) this.LOCAL_LOG = true;
 		if(this.context == null) {
 			this.context = getSherlockActivity().getApplication();
 		}
 		if(this.prefs == null) {
 			this.prefs = new PastyPreferencesProvider(getSherlockActivity().getApplication());
 		} else {
 			prefs.reload();
 		}
 		
 	    View v = null;
 	    if (Intent.ACTION_SEND.equals(getSherlockActivity().getIntent().getAction())) {
 	    	// called via share intent
 	        getDialog().setTitle(R.string.dialog_item_add_title_running);
 	    	Bundle extras = getSherlockActivity().getIntent().getExtras();
     		v = inflater.inflate(R.layout.progressbar, container, false);
 	    	if (extras != null) {
 	    		String mItem = extras.getString(Intent.EXTRA_TEXT);
 	        	new ItemAddTask().execute(mItem);
 	    	}
 	    } else {
 			getDialog().setTitle(R.string.dialog_item_add_title);
 	    	v = inflater.inflate(R.layout.add_item, container, false);
 		    Button button = (Button)v.findViewById(R.id.button_add_item_confirm);
 			if(mNewItemET == null) { mNewItemET = (EditText) v.findViewById(R.id.NewItem); }
 			getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
 	        button.setOnClickListener(new OnClickListener() {
 	            public void onClick(View v) {
 					LinearLayout mDialogLayout = (LinearLayout) v.getParent();
 					String mItem = mNewItemET.getText().toString();
 			    	if(mItem != null && mItem.length() == 0) {
 			    		Toast.makeText(context, getString(R.string.empty_item), Toast.LENGTH_LONG).show();
 			    		return;
 			    	}
 			    	ViewGroup container = (ViewGroup) mDialogLayout.getParent();
 			        container.removeAllViews();
 			        View pb = inflater.inflate(R.layout.progressbar, container, false);
 			        container.addView(pb);
 			        getDialog().setTitle(R.string.dialog_item_add_title_running);
 					getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 	            	new ItemAddTask().execute(mItem);
 	            }
 	        });
 	
 			if(prefs.getPasteCurrClip()) {
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSherlockActivity().getSystemService("clipboard");
 					if(clipboard.hasPrimaryClip()) {
 						if(LOCAL_LOG) Log.v(TAG, "onCreateView(): using modern clipboard API");
 						android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
 						if(item != null) mNewItemET.setText(item.coerceToText(context));
 						item = null;
 					}
 					clipboard = null;
 				} else {
 					ClipboardManager clipboard = (ClipboardManager) getSherlockActivity().getSystemService("clipboard");
 					if(clipboard.hasText()) {
 						mNewItemET.setText(clipboard.getText());
 					}
 					clipboard = null;
 				}
 			}
 	    }
         return v;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 	}
 	
 	@Override
 	public void onDismiss(DialogInterface dialog) {
 		// TODO make sure we destroy our view
 	}
 	
 	public interface AddItemFragmentCallbackListener {
         void onAddItemFragmentCallbackSignal(int signal);
     }
 	
 	private class ItemAddTask extends AsyncTask<String, Void, PastyResponse > {
 	    /** The system calls this to perform work in a worker thread and
 	      * delivers it the parameters given to AsyncTask.execute() */
 		@Override
 		protected PastyResponse doInBackground(String... item) {
 			PastyClient client = new PastyClient(prefs.getRESTBaseURL(), true);
 			client.setUsername(prefs.getUsername());
 			client.setPassword(prefs.getPassword());
 			PastyResponse result;
 			try {
 				final String itemId = client.addItem(item[0]);
 				result = new PastyResponse(itemId);
 			} catch (PastyException e) {
 				result = new PastyResponse(e);
 			}
 			return result;
 		}
 	    
 	    /** The system calls this to perform work in the UI thread and delivers
 	      * the result from doInBackground() */
 	    protected void onPostExecute(PastyResponse result) {
 	       if(result.hasException) {
 	    	   handleException(result.getException());
 	       } else {
 	   		final AddItemFragmentCallbackListener activity = (AddItemFragmentCallbackListener) getSherlockActivity();
 	   		Toast.makeText(context, getString(R.string.item_added), Toast.LENGTH_LONG).show();
 	   		prefs.storeLastItem(result.getItemId());
 	   		activity.onAddItemFragmentCallbackSignal(PastySharedStatics.SIGNAL_EXIT);
 	       }
 	    }
 	}
 	
 	 protected void handleException(PastyException mException) {
 	    	TextView mHelpTextBig = (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextBig);
 	    	TextView mHelpTextSmall = (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextSmall);
 	    	switch(mException.errorId) {
  		case PastyException.ERROR_AUTHORIZATION_FAILED:
  			Log.d(TAG, "ERROR_AUTHORIZATION_FAILED EXCEPTION");
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_login_failed_title);
 				mHelpTextSmall.setText(R.string.error_login_failed);
 				return;
 			case PastyException.ERROR_IO_EXCEPTION:
  			Log.d(TAG, "ERROR_IO_EXCEPTION");
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_io_title);
 				mHelpTextSmall.setText(R.string.error_io);
 			case PastyException.ERROR_ILLEGAL_RESPONSE:
 				Log.d(TAG, "ERROR_ILLEGAL_RESPONSE EXCEPTION");
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_badanswer_title);
 				mHelpTextSmall.setText(R.string.error_badanswer);
 				return;
 			case PastyException.ERROR_UNKNOWN:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_unknown_title);
 				mHelpTextSmall.setText(R.string.error_unknown);
 				return;
 			default:
 				break;
 			}

			mHelpTextBig = null;
 	    }
 	
 }
