 package com.abbyy.cloudocr.fragments;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 
 import com.abbyy.cloudocr.CreateTaskActivity;
 import com.abbyy.cloudocr.R;
 import com.abbyy.cloudocr.SettingsActivity;
 import com.abbyy.cloudocr.ChooseTaskActivity;
 import com.abbyy.cloudocr.TasksManagerService;
 import com.abbyy.cloudocr.database.TasksContract;
 
 /**
  * Child of the abstract tasks fragment to display the active tasks. It will
  * load the active tasks from the database and show an option to create a new
  * task.
  * 
  * @author Denis Lapuente
  * 
  */
 public class ActiveTasksFragment extends TasksFragment {
 
 	/**
 	 * Called upon creation of the fragment instance.
 	 * 
 	 * Aside from inflating the whole view, we add the onClickListener to launch
 	 * a new task on it.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.active_tasks_fragment, container,
 				false);
 		v.findViewById(R.id.create_new_task).setOnClickListener(
 				new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						launchNewTask();
 					}
 				});
 		return v;
 	}
 
 	/**
 	 * After the view is created, the fragment is loaded into the activity and
 	 * this method is called. When called we just load the tasks.
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		getActivity().getSupportLoaderManager().restartLoader(
 				LOADER_ACTIVE_TASKS, null, this);
 	}
 
 	/**
 	 * We load the actions for the action bar / menu.
 	 * 
 	 * The menu for the fragment only contains a refresh option.
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.active_tasks, menu);
 	}
 
 	/**
 	 * Manage the possible action clicks. Currently it is only a refresh option
 	 * which launchs a task to download the status of the tasks
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			downloadTasks(true);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onResume() {
		getLoaderManager().restartLoader(LOADER_COMPLETED_TASKS, null, this);
 		super.onResume();
 	}
 
 	/**
 	 * We set the adapter parameters for the list. The information which is to
 	 * be shown, where is it to be shown.
 	 */
 	@Override
 	void setAdapter() {
 		String[] from = { TasksContract.TasksTable.TASK_ID,
 				TasksContract.TasksTable.ESTIMATED_PROCESSING_TIME };
 		int[] to = { R.id.task_list_item_task_id,
 				R.id.task_list_item_estimated_time };
 
 		mAdapter = new TasksAdapter(getActivity(), R.layout.completed_entry,
 				null, from, to, 0, true);
 
 		setListAdapter(mAdapter);
 
 	}
 
 	/**
 	 * Convenience method for launching a new task. When in portrait, it will
 	 * launch a new activity. When in landscape, it will replace the secondary
 	 * fragment with the choose task fragment.
 	 */
 	private void launchNewTask() {
 		if (isLandscape) {
 			ChooseTaskFragment fragment = new ChooseTaskFragment();
 			FragmentTransaction transaction = getActivity()
 					.getSupportFragmentManager().beginTransaction();
 			transaction.disallowAddToBackStack();
 			transaction.replace(R.id.main_activity_second_fragment, fragment);
 			transaction.commit();
 		} else {
 			Intent intent = new Intent(getActivity(), ChooseTaskActivity.class);
 			intent.putExtra(CreateTaskActivity.EXTRA_PROCESS_MODE,
 					SettingsActivity.PROCESSING_MODE_IMAGE);
 			startActivity(intent);
 		}
 	}
 
 	/**
 	 * Convenience method for removing a task from the list. It calls the
 	 * service to try to cancel the task if possible.
 	 */
 	@Override
 	void removeTask(String taskId) {
 		Intent service = new Intent(getActivity(), TasksManagerService.class);
 		service.putExtra(TasksManagerService.EXTRA_ACTION,
 				TasksManagerService.ACTION_DELETE_ACTIVE_TASK);
 		service.putExtra(TasksManagerService.EXTRA_TASK_ID, taskId);
 		getActivity().startService(service);
 	}
 }
