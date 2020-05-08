 /*
  * Copyright 2011 Miltiadis Allamanis
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
  
 /**
  * 
  */
 package ac.uk.cam.cl.passgroiApp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.google.nigori.client.DAG;
 import com.google.nigori.client.Node;
 import com.google.nigori.common.Revision;
 
 import uk.ac.cam.cl.passgori.IPasswordStore;
 import uk.ac.cam.cl.passgori.Password;
 import uk.ac.cam.cl.passgori.PasswordStoreException;
 import ac.uk.cam.cl.passgroiApp.PasswordStoreService.PasswordStorageBinder;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * An activity presenting the a password entity.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class PassgoriPresentPasswordsActivity extends Activity {
 
 	public class DeletePasswordThread extends Thread {
 
 		private final String mPasswordId;
 
 		public DeletePasswordThread(final String passwordId) {
 			mPasswordId = passwordId;
 		}
 
 		@Override
 		public void run() {
 			try {
 				if (mPasswordStore.removePassword(mPasswordId)) {
 					runOnUiThread(new PasswordDeleted());
 					// go back!
 				} else {
 					// update GUI
 					runOnUiThread(new FailureNotification(
 							"Password unaccessbile"));
 				}
 			} catch (PasswordStoreException e) {
 				runOnUiThread(new FailureNotification(e.getMessage()));
 
 			}
 		}
 	}
 
 	public class PasswordDeleted implements Runnable {
 
 		@Override
 		public void run() {
 			mLoadingDialog.dismiss();
 			setResult(1); // on finish ask previous activity to refresh
 			finish();
 			Toast.makeText(getApplicationContext(), "Password Deleted",
 					Toast.LENGTH_LONG);
 		}
 
 	}
 
 	private class FailureNotification implements Runnable {
 
 		private final String mMessage;
 
 		public FailureNotification(String message) {
 			mMessage = message;
 		}
 
 		@Override
 		public void run() {
 			if (mLoadingDialog != null)
 				mLoadingDialog.dismiss();
 			Toast.makeText(getApplicationContext(), mMessage, Toast.LENGTH_LONG);
 		}
 
 	}
 
 	private class GetPassword extends Thread {
 		@Override
 		public void run() {
 			try {
 				Password password = mPasswordStore.retrivePassword(getIntent()
 						.getExtras().getString("passwordId"));
 
 				if (password != null) {
 					UpdatePasswordDetails updater = new UpdatePasswordDetails(
 							password);
 					runOnUiThread(updater);
 				} else {
 					// Update GUI about failure!!
 					runOnUiThread(new FailureNotification(
 							"Password Unaccessible"));
 				}
 			} catch (PasswordStoreException e) {
 				runOnUiThread(new FailureNotification(e.getMessage()));
 
 			}
 		}
 	}
 
 	private class UpdatePasswordDetails implements Runnable {
 
 		private final Password mPassword;
 
 		public UpdatePasswordDetails(Password password) {
 			mPassword = password;
 		}
 
 		@Override
 		public void run() {
 			mUsernameField.setText(mPassword.getUsername());
 			mPasswordField.setText(mPassword.getPassword());
 			mNotesField.setText(mPassword.getNotes());
 
 			mUsernameField.setVisibility(View.VISIBLE);
 			mPasswordField.setVisibility(View.VISIBLE);
 			mNotesField.setVisibility(View.VISIBLE);
 			mHistoryList.setVisibility(View.VISIBLE);
 
 			mLoadingDialog.dismiss();
 		}
 
 	}
 
 	/**
 	 * The TextView corresponding to the password's title.
 	 */
 	private TextView mPasswordTitle;
 
 	/**
 	 * The TextView corresponding to the paswords's username.
 	 */
 	private TextView mUsernameField;
 
 	/**
 	 * The TextView corresponding to the password's password.
 	 */
 	private TextView mPasswordField;
 
 	/**
 	 * The TextView corresponding to the password's notes.
 	 */
 	private TextView mNotesField;
 
 	private ExpandableListView mHistoryList;
 
   /**
 	 * Progress Dialog to indicate progress.
 	 */
 	private ProgressDialog mLoadingDialog;
 
 	/**
 	 * The password store.
 	 */
 	private IPasswordStore mPasswordStore;
 
 	/** Defines callbacks for service binding, passed to bindService() */
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
		private boolean mBound;
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			PasswordStorageBinder binder = (PasswordStorageBinder) service;
 
 			try {
 				mPasswordStore = binder.getStore();
				mBound = true;
 
 				// Spawn thread to get password
 				new GetPassword().start();
 			} catch (PasswordStoreException e) {
 				new FailureNotification(e.getMessage()).run();
 			}
 
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
 		}
 	};
 
   @Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.password_present);
 
 		// Assign content
 		mPasswordTitle = (TextView) findViewById(R.id.passwordTitle);
 		mUsernameField = (TextView) findViewById(R.id.usernameField);
 		mPasswordField = (TextView) findViewById(R.id.passwordField);
 		mNotesField = (TextView) findViewById(R.id.notesField);
 		mHistoryList = (ExpandableListView) findViewById(R.id.historyList);
 
 		mLoadingDialog = ProgressDialog.show(this, "",
 				"Loading. Please wait...", true);
 
 		String id = getIntent().getExtras().getString("passwordId");
 		mPasswordTitle.setText(id);
 		mUsernameField.setVisibility(View.INVISIBLE);
 		mPasswordField.setVisibility(View.INVISIBLE);
 		mNotesField.setVisibility(View.INVISIBLE);
 		mHistoryList.setVisibility(View.INVISIBLE);
 		try {
       mHistoryList.setAdapter(new HistoryAdapter(this,id));
     } catch (PasswordStoreException e) {
       runOnUiThread(new FailureNotification("Error getting history: " + e.toString()));
     }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.password_present_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.editPasswordOption:
 			Intent intent = new Intent(this, PassgoriEditPasswordActivity.class);
 			intent.putExtra("passwordId",
 					getIntent().getExtras().getString("passwordId"));
 			startActivityForResult(intent, 0);
 			return true;
 		case R.id.deletePasswordOption:
 			mLoadingDialog = ProgressDialog.show(this, "",
 					"Deleting. Please wait...", true);
 			new DeletePasswordThread(getIntent().getExtras().getString(
 					"passwordId")).start();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		// Close activity to avoid storing the password if someone exits us from
 		// the home button
 		finish();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		// Bind to PasswordStoreService
 		Intent intent = new Intent(this, PasswordStoreService.class);
 
 		if (!getApplicationContext().bindService(intent, mConnection,
 				Context.BIND_AUTO_CREATE)) {
 			// Inform GUI for failure!!
 			mLoadingDialog.dismiss();
 			new FailureNotification("Unable to connect to internal server")
 					.run();
 
 		}
 
 		// Once the service is binded, we will load the list
 	}
 
 	private class HistoryAdapter extends BaseExpandableListAdapter {
 	  protected LayoutInflater inflater;
 	  protected List<Revision> revisions;
     private String id;
     private boolean initialised = false;
     protected Map<Integer,Password> cache = new TreeMap<Integer,Password>();
 
 	  public HistoryAdapter(Context con, String id) throws PasswordStoreException{
 	    inflater = LayoutInflater.from(con);
       revisions = new ArrayList<Revision>();
       this.id = id;
     }
 
 	  private void initialise() {
       if (!initialised && mPasswordStore != null) {
         try {
           DAG<Revision> history = mPasswordStore.getHistory(id);
 
           if (history != null) {
             for (Node<Revision> revision : history) {
               revisions.add(revision.getValue());
             }
           }
         } catch (PasswordStoreException e) {
           runOnUiThread(new FailureNotification("Error while getting history" + e.toString()));
         }
         initialised = true;
       }
 	  }
     @Override
     public Password getChild(int groupPosition, int childPosition) {
       if (groupPosition != 0){
         return null;
       }
       try {
         initialise();
         Password password = cache.get(childPosition);
         if (password == null) {
           password = mPasswordStore.retrivePassword(id, revisions.get(childPosition));
           cache.put(childPosition, password);
         }
         return password;
       } catch (PasswordStoreException e) {
         runOnUiThread(new FailureNotification("Error retrieving old password"));
         return null;
       }
     }
 
     @Override
     public long getChildId(int groupPosition, int childPosition) {
       if (groupPosition != 0){
         return 0;
       }
       return childPosition;
     }
 
     @Override
     public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
         View convertView, ViewGroup parent) {
       if (groupPosition != 0){
         return null;
       }
       Password password = getChild(groupPosition,childPosition);
       if (convertView == null){
         convertView = inflater.inflate(R.layout.history_child, null);
       }
 
       TextView title = (TextView) convertView.findViewById(R.id.passwordTitle);
       title.setText(password.getId());
       TextView username = (TextView) convertView.findViewById(R.id.usernameField);
       username.setText(password.getUsername());
       TextView passwordField = (TextView) convertView.findViewById(R.id.passwordField);
       passwordField.setText(password.getPassword());
       TextView notes = (TextView) convertView.findViewById(R.id.notesField);
       notes.setText(password.getNotes());
       TextView date = (TextView) convertView.findViewById(R.id.historyChildDate);
       date.setText(new Date(password.getGeneratedAt()).toLocaleString());
 
       return convertView;
     }
 
     @Override
     public int getChildrenCount(int groupPosition) {
       if (groupPosition != 0){
         return 0;
       }
       initialise();
       return revisions.size();
     }
 
     @Override
     public List<Revision> getGroup(int groupPosition) {
       if (groupPosition != 0){
         return null;
       }
       initialise();
       return revisions;
     }
 
     @Override
     public int getGroupCount() {
       return 1;
     }
 
     @Override
     public long getGroupId(int groupPosition) {
       return R.layout.history_group;
     }
 
     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
         ViewGroup parent) {
       if (convertView == null){
         convertView = inflater.inflate(R.layout.history_group, null);
       }
       return convertView;
     }
 
     @Override
     public boolean hasStableIds() {
       return true;
     }
 
     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
       if (groupPosition != 0) {
         return false;
       }
       initialise();
       if (childPosition < revisions.size()) {
         return true;
       } else {
         return false;
       }
     }
     
   }
 }
