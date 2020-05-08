 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.bigpupdev.synodroid.R;
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.data.Task;
 import com.bigpupdev.synodroid.data.TaskFile;
 import com.bigpupdev.synodroid.data.TaskStatus;
 import com.bigpupdev.synodroid.ui.SynodroidFragment;
 import com.bigpupdev.synodroid.utils.ActionModeHelper;
 import com.bigpupdev.synodroid.action.GetFilesAction;
 import com.bigpupdev.synodroid.action.TaskActionMenu;
 import com.bigpupdev.synodroid.adapter.FileActionAdapter;
 import com.bigpupdev.synodroid.adapter.FileDetailAdapter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager.BadTokenException;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 /**
  * This activity displays a task's details
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class DetailFiles extends SynodroidFragment implements OnCheckedChangeListener{
 	FileDetailAdapter fileAdapter;
 	private ListView filesListView;
 		
 	private Activity a;
 
 	public ActionModeHelper mCurrentActionMode;
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// ignore orientation change
 		super.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	public void handleMessage(Message msgP) {
 		((DetailActivity)a).handleMessage(msgP);
 	}
 	
 	public void resetList(){
 		try{
 			if (((Synodroid)((DetailActivity)a).getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DetailFiles: Reseting file list.");
 		}
 		catch (Exception ex){/*DO NOTHING*/}
 		
 		if (fileAdapter != null)
 			fileAdapter.updateFiles(new ArrayList<TaskFile>());
 	}
 	
 	public void updateEmptyValues(String text, boolean showPB){
 		if (filesListView != null){
 			View empty = filesListView.getEmptyView();
 			if (empty != null){
 				ProgressBar pb = (ProgressBar) empty.findViewById(R.id.empty_pb);
 				TextView tv = (TextView) empty.findViewById(R.id.empty_text);
 				if (showPB){
 					pb.setVisibility(View.VISIBLE);
 				}
 				else{
 					pb.setVisibility(View.GONE);
 				}
 				
 				tv.setText(text);
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		a = this.getActivity();
 		Synodroid app = (Synodroid) a.getApplication();
 		try{
 			if (((Synodroid)((DetailActivity)a).getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DetailFiles: Creating file list fragment.");
 		}
 		catch (Exception ex){/*DO NOTHING*/}
 		
 		// Get the details intent
 		Intent intent = a.getIntent();
 		Task task = (Task) intent.getSerializableExtra("com.bigpupdev.synodroid.ds.Details");
 	
 		mCurrentActionMode = ((BaseActivity) getActivity()).getActionModeHelper();
 		
 		View v = inflater.inflate(R.layout.detail_files, null);
 
 		filesListView = (ListView) v.findViewById(android.R.id.list);
		fileAdapter = new FileDetailAdapter(this, task, app.getServer().getDsmVersion());
 		filesListView.setAdapter(fileAdapter);
 		filesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		filesListView.setOnItemClickListener(fileAdapter);
 		filesListView.setOnItemLongClickListener(fileAdapter);
 		View empty = v.findViewById(android.R.id.empty);
 		
 		filesListView.setEmptyView(empty);
 		
 		if (!task.isTorrent && !task.isNZB){
 			updateEmptyValues(getString(R.string.empty_file_list_wrong_type), false);
 		}
 		else if (!task.status.equals(TaskStatus.TASK_DOWNLOADING.name()) && !task.status.equals(TaskStatus.TASK_SEEDING.name())){
 			updateEmptyValues(a.getString(R.string.empty_file_list), false);
 		}
 		else{
 			app.executeAsynchronousAction(this, new GetFilesAction(task), false);
 		}
 		setRetainInstance(true);
 		return v;
 	}
 
 	public void finish() {
 		getActivity().finish();
 	}
 	
 	// List of checkbox and task
 	public List<TaskFile> checked_tasks = new ArrayList<TaskFile>();
 	public List<Integer> checked_tasks_id = new ArrayList<Integer>();
 	
 	public void resetChecked(){
 		try{
 			if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DetailFiles: Resetting check selection.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		checked_tasks = new ArrayList<TaskFile>();
 		checked_tasks_id = new ArrayList<Integer>();
 		fileAdapter.clearTasksSelection();
 	
 	}
 	
 	public void validateChecked(ArrayList<Integer> currentTasks){
 		try{
 			if (((Synodroid)getActivity().getApplication()).DEBUG) Log.v(Synodroid.DS_TAG,"DetailFiles: Validating checked items.");
 		}catch (Exception ex){/*DO NOTHING*/}
 		
 		List<Integer> toDel = new ArrayList<Integer>();
 		
 		for (Integer i : checked_tasks_id) {
 			if (!currentTasks.contains(i)){
 				toDel.add(checked_tasks_id.indexOf(i));
 			}
 		}
 		Collections.sort(toDel, Collections.reverseOrder());
 		
 		for (Integer pos : toDel){
 			try{
 				checked_tasks.remove(pos.intValue());
 			}catch (IndexOutOfBoundsException e){ /*IGNORE*/}
 			try{
 				checked_tasks_id.remove(pos.intValue());
 			}catch (IndexOutOfBoundsException e){ /*IGNORE*/}
 		}
 		
 		if (checked_tasks_id.size() == 0){
 			mCurrentActionMode.stopActionMode();
 		}
 		else{
 			String selected = getActivity().getString(R.string.selected);
 			mCurrentActionMode.setTitle(Integer.toString(checked_tasks_id.size()) +" "+ selected);
 		}
 	}
 	
 	public void onCheckedChanged(CompoundButton button, boolean check) {
 		TaskFile t = (TaskFile)button.getTag();
 		if (check){
 			
 			Intent intent = a.getIntent();
 			Task task = (Task) intent.getSerializableExtra("com.bigpupdev.synodroid.ds.Details");
 		
 			if (checked_tasks_id.contains(t.id)) return;
 			t.selected = true;
 			
 			try{
 				if (((Synodroid)getActivity().getApplication()).DEBUG) Log.d(Synodroid.DS_TAG,"DetailFiles: File id "+t.id+" checked.");
 			}catch (Exception ex){/*DO NOTHING*/}
 			
 			mCurrentActionMode.startActionMode(this, task);
 			checked_tasks.add(t);
 			checked_tasks_id.add(t.id);
 		}
 		else{
 			if (!checked_tasks_id.contains(t.id)) return;
 			t.selected = false;
 			
 			try{
 				if (((Synodroid)getActivity().getApplication()).DEBUG) Log.d(Synodroid.DS_TAG,"DetailFiles: File id "+t.id+" unchecked.");
 			}catch (Exception ex){/*DO NOTHING*/}
 
 			checked_tasks.remove(t);
 			checked_tasks_id.remove(checked_tasks_id.indexOf(t.id));
 			if (checked_tasks_id.size() == 0){
 				if (!mCurrentActionMode.terminating){
 					mCurrentActionMode.stopActionMode();
 				}
 			}
 		}
 		String selected = getActivity().getString(R.string.selected);
 		mCurrentActionMode.setTitle(Integer.toString(checked_tasks_id.size()) +" "+ selected);
 	}
 	
 	/**
 	 * A task as been long clicked by the user
 	 * 
 	 * @param file
 	 */
 	public void onTaskLongClicked(final TaskFile file) {
 		final Activity a = getActivity();
 		Intent intent = a.getIntent();
 		Task task = (Task) intent.getSerializableExtra("com.bigpupdev.synodroid.ds.Details");
 	
 		AlertDialog.Builder builder = new AlertDialog.Builder(a);
 		builder.setTitle(getString(R.string.dialog_title_action));
 		final FileActionAdapter adapter = new FileActionAdapter(a, file, task, fileAdapter.getFileList());
 		if (adapter.getCount() != 0) {
 			builder.setAdapter(adapter, new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					TaskActionMenu taskAction = (TaskActionMenu) adapter.getItem(which);
 					// Only if TaskActionMenu is enabled: it seems that even if the
 					// item is
 					// disable the user can tap it
 					if (taskAction.isEnabled()) {
 						Synodroid app = (Synodroid) a.getApplication();
 						app.executeAction(DetailFiles.this, taskAction.getAction(), true);
 					}
 				}
 			});
 			AlertDialog connectDialog = builder.create();
 			try {
 				connectDialog.show();
 			} catch (BadTokenException e) {
 				// Unable to show dialog probably because intent has been closed. Ignoring...
 			}
 		}
 	}
 
 }
