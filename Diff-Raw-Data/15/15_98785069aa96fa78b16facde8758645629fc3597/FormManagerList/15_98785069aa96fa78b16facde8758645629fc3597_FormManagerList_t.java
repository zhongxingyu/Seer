 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.odk.collect.android.activities;
 
 import java.util.ArrayList;
 
 import org.odk.collect.android.R;
 import org.odk.collect.android.application.Collect;
 import org.odk.collect.android.listeners.DeleteFormsListener;
 import org.odk.collect.android.listeners.DiskSyncListener;
 import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
 import org.odk.collect.android.tasks.DeleteFormsTask;
 import org.odk.collect.android.tasks.DiskSyncTask;
 import org.odk.collect.android.utilities.VersionHidingCursorAdapter;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 /**
  * Responsible for displaying and deleting all the valid forms in the forms
  * directory.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  * @author Yaw Anokwa (yanokwa@gmail.com)
  */
 public class FormManagerList extends SherlockListFragment implements DiskSyncListener,
 		DeleteFormsListener {
 	private static String t = "FormManagerList";
 	private static final String SELECTED = "selected";
 	private static final String syncMsgKey = "syncmsgkey";
 
 	private View mView;
 	private AlertDialog mAlertDialog;
 
 	private SimpleCursorAdapter mInstances;
 	private ArrayList<Long> mSelected = new ArrayList<Long>();
 
 	static class BackgroundTasks {
 		DiskSyncTask mDiskSyncTask = null;
 		DeleteFormsTask mDeleteFormsTask = null;
 
 		BackgroundTasks() {
 		};
 	}
 
 	BackgroundTasks mBackgroundTasks; // handed across orientation changes
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
     @Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
     	
 		mView = inflater.inflate(R.layout.form_manage_list, container, false);
 		setRetainInstance(true);
 		
 		return mView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		
 		super.onActivityCreated(savedInstanceState);
 		String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
         Cursor c = getActivity().managedQuery(FormsColumns.CONTENT_URI, null, null, null, sortOrder);
 
 		String[] data = new String[] { FormsColumns.DISPLAY_NAME,
 				FormsColumns.DISPLAY_SUBTEXT, FormsColumns.JR_VERSION };
 		int[] view = new int[] { R.id.text1, R.id.text2, R.id.text3 };
 
 		// render total instance view
 		mInstances = new VersionHidingCursorAdapter(FormsColumns.JR_VERSION, this.getActivity(),
 				R.layout.two_item_multiple_choice, c, data, view);
 		setListAdapter(mInstances);
 		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		getListView().setItemsCanFocus(false);
 
 		if (savedInstanceState != null
 				&& savedInstanceState.containsKey(syncMsgKey)) {
 			TextView tv = (TextView) mView.findViewById(R.id.status_text);
 			tv.setText(savedInstanceState.getString(syncMsgKey));
 		}
 
		mBackgroundTasks = (BackgroundTasks) getActivity().getLastCustomNonConfigurationInstance();
 		if (mBackgroundTasks == null) {
 			mBackgroundTasks = new BackgroundTasks();
 			mBackgroundTasks.mDiskSyncTask = new DiskSyncTask();
 			mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(this);
 			mBackgroundTasks.mDiskSyncTask.execute((Void[]) null);
 		}
 	}
 
 	@Override
 	public void onViewStateRestored(Bundle savedInstanceState) {
 		super.onViewStateRestored(savedInstanceState);
 		if (savedInstanceState!=null)
 		{
 			long[] selectedArray = savedInstanceState.getLongArray(SELECTED);
 			for (int i = 0; i < selectedArray.length; i++) {
 				mSelected.add(selectedArray[i]);
 			}
 		}
 	}
 
 	@Override
 	public void onStart() {
     	super.onStart();
 		Collect.getInstance().getActivityLogger().logOnStart(this.getActivity()); 
     }
     
     @Override
 	public void onStop() {
 		Collect.getInstance().getActivityLogger().logOnStop(this.getActivity()); 
     	super.onStop();
     }
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		long[] selectedArray = new long[mSelected.size()];
 		for (int i = 0; i < mSelected.size(); i++) {
 			selectedArray[i] = mSelected.get(i);
 		}
 		outState.putLongArray(SELECTED, selectedArray);
 		TextView tv = (TextView) mView.findViewById(R.id.status_text);
 		outState.putString(syncMsgKey, tv.getText().toString());
 	}
 
 	@Override
 	public void onResume() {
 		// hook up to receive completion events
 		mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(this);
 		if (mBackgroundTasks.mDeleteFormsTask != null) {
 			mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
 		}
 		super.onResume();
 		// async task may have completed while we were reorienting...
 		if (mBackgroundTasks.mDiskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
 			SyncComplete(mBackgroundTasks.mDiskSyncTask.getStatusMessage());
 		}
 		if (mBackgroundTasks.mDeleteFormsTask != null
 				&& mBackgroundTasks.mDeleteFormsTask.getStatus() == AsyncTask.Status.FINISHED) {
 			deleteComplete(mBackgroundTasks.mDeleteFormsTask.getDeleteCount());
 		}
 	}
 
 	@Override
 	public void onPause() {
 		mBackgroundTasks.mDiskSyncTask.setDiskSyncListener(null);
 		if (mBackgroundTasks.mDeleteFormsTask != null ) {
 			mBackgroundTasks.mDeleteFormsTask.setDeleteListener(null);
 		}
 		if (mAlertDialog != null && mAlertDialog.isShowing()) {
 			mAlertDialog.dismiss();
 		}
 
 		super.onPause();
 	}
 	
 	protected void selectAll() {
 		boolean checkAll = false;
         // if everything is checked, uncheck
         if (mSelected.size() == mInstances.getCount()) {
             checkAll = false;
             mSelected.clear();
         } else {
             // otherwise check everything
             checkAll = true;
             for (int pos = 0; pos < FormManagerList.this.getListView().getCount(); pos++) {
                 Long id = getListAdapter().getItemId(pos);
                 if (!mSelected.contains(id)) {
                     mSelected.add(id);
                 }
             }
         }
         for (int pos = 0; pos < FormManagerList.this.getListView().getCount(); pos++) {
             FormManagerList.this.getListView().setItemChecked(pos, checkAll);
         }
 	}
 	
 	protected void delete() {
 		Collect.getInstance().getActivityLogger().logAction(this, "deleteButton", Integer.toString(mSelected.size()));
 
 		if (mSelected.size() > 0) {
 			createDeleteFormsDialog();
 		} else {
 			Toast.makeText(getActivity().getApplicationContext(),
 					R.string.noselect_error, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * Create the form delete dialog
 	 */
 	private void createDeleteFormsDialog() {
     	Collect.getInstance().getActivityLogger().logAction(this, "createDeleteFormsDialog", "show");
 		mAlertDialog = new AlertDialog.Builder(this.getActivity()).create();
 		mAlertDialog.setTitle(getString(R.string.delete_file));
 		mAlertDialog.setMessage(getString(R.string.delete_confirm,
 				mSelected.size()));
 		DialogInterface.OnClickListener dialogYesNoListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int i) {
 				switch (i) {
 				case DialogInterface.BUTTON1: // delete
 			    	Collect.getInstance().getActivityLogger().logAction(this, "createDeleteFormsDialog", "delete");
 					deleteSelectedForms();
 					break;
 				case DialogInterface.BUTTON2: // do nothing
 			    	Collect.getInstance().getActivityLogger().logAction(this, "createDeleteFormsDialog", "cancel");
 					break;
 				}
 			}
 		};
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setButton(getString(R.string.delete_yes),
 				dialogYesNoListener);
 		mAlertDialog.setButton2(getString(R.string.delete_no),
 				dialogYesNoListener);
 		mAlertDialog.show();
 	}
 
 	/**
 	 * Deletes the selected files.First from the database then from the file
 	 * system
 	 */
 	private void deleteSelectedForms() {
 		// only start if no other task is running
 		if (mBackgroundTasks.mDeleteFormsTask == null) {
 			mBackgroundTasks.mDeleteFormsTask = new DeleteFormsTask();
 			mBackgroundTasks.mDeleteFormsTask
 					.setContentResolver(getActivity().getContentResolver());
 			mBackgroundTasks.mDeleteFormsTask.setDeleteListener(this);
 			mBackgroundTasks.mDeleteFormsTask.execute(mSelected
 					.toArray(new Long[mSelected.size()]));
 		} else {
 			Toast.makeText(this.getActivity(), getString(R.string.file_delete_in_progress),
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		// get row id from db
 		Cursor c = (Cursor) getListAdapter().getItem(position);
 		long k = c.getLong(c.getColumnIndex(BaseColumns._ID));
 
 		// add/remove from selected list
 		if (mSelected.contains(k))
 			mSelected.remove(k);
 		else
 			mSelected.add(k);
 		
 		Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick", Long.toString(k));
 	}
 
 	@Override
 	public void SyncComplete(String result) {
 		Log.i(t, "Disk scan complete");
 		if (mView != null){
 			TextView tv = (TextView) mView.findViewById(R.id.status_text);
 			tv.setText(result);
 		}
 	}
 
 	@Override
 	public void deleteComplete(int deletedForms) {
 		Log.i(t, "Delete forms complete");
         Collect.getInstance().getActivityLogger().logAction(this, "deleteComplete", Integer.toString(deletedForms));
 		if (deletedForms == mSelected.size()) {
 			// all deletes were successful
 			Toast.makeText(getActivity().getApplicationContext(),
 					getString(R.string.file_deleted_ok, deletedForms),
 					Toast.LENGTH_SHORT).show();
 		} else {
 			// had some failures
 			Log.e(t, "Failed to delete " + (mSelected.size() - deletedForms)
 					+ " forms");
 			Toast.makeText(
 					getActivity().getApplicationContext(),
 					getString(R.string.file_deleted_error, mSelected.size()
 							- deletedForms, mSelected.size()),
 					Toast.LENGTH_LONG).show();
 		}
 		mBackgroundTasks.mDeleteFormsTask = null;
 		mSelected.clear();
 		getListView().clearChoices(); // doesn't unset the checkboxes
 		for ( int i = 0 ; i < getListView().getCount() ; ++i ) {
 			getListView().setItemChecked(i, false);
 		}
 	}
 }
