 package com.bustiblelemons.tasque.main;
 
 import java.util.ArrayList;
 
 import static com.bustiblelemons.tasque.utilities.Values.TAG;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.bustiblelemons.tasque.R;
 import com.bustiblelemons.tasque.database.Database;
 import com.bustiblelemons.tasque.frontend.Task;
 import com.bustiblelemons.tasque.main.NotesFragment.NotesFragmentListener;
 import com.bustiblelemons.tasque.main.TasqueGroupFragment.TasqueGroupFragmentListener;
 import com.bustiblelemons.tasque.rtm.RTMBackend;
 import com.bustiblelemons.tasque.utilities.Values.FragmentArguments;
 
 public class CompletedTasksFragment extends SherlockFragment implements OnItemClickListener, OnItemLongClickListener,
 		OnTouchListener {
 
 	public static final String FRAGMENT_TAG = "completed";
 	private View view;
 	private ListView listView;
 	private TasqueAdapter adapter;
 	private Context context;
 	private Bundle args;
 	private Cursor data;
 	private String listId;
 	private boolean DELETING_ENABLED;
 	private ActionBar abar;
 
 	public interface CompletedTasksListener {
 		public void onShowCompletedTasksFragment(String listId);
 
 		public void onTaskMarkedActive();
 
 		public void onStartDeletingCompletedTasks();
 
 		public void onStopDeletingCompletedTasks();
 
 		public void onDeleteItems();
 	}
 
 	private NotesFragmentListener showNotesFragment;
 	private TasqueGroupFragmentListener tasqueGroupFragmentListener;
 	private RightSideFragmentPocketListener rightSideFragmentChange;
 	private EditText input;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		rightSideFragmentChange = (RightSideFragmentPocketListener) activity;
 		tasqueGroupFragmentListener = (TasqueGroupFragmentListener) activity;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.context = getActivity().getApplicationContext();
 		this.args = getArguments();
 		this.listId = args.getString(FragmentArguments.ID);
 	}
 
 	// FIXME Menu is changing due to fragments being reinstaintained.
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.fragment_completed_tasks, null);
 		listView = (ListView) view.findViewById(R.id.fragment_completed_tasks_list);
 		this.data = Database.getCompletedTasks(context, listId);
 		adapter = new TasqueAdapter(context, data);
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(this);
 		listView.setOnItemLongClickListener(this);
 		return view;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		setUpHasMenu();
 	}
 
 	public void setUpHasMenu() {
 		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			if (context.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_HIGH) {
 				Log.d(TAG, "High density");
 				setHasOptionsMenu(false);
 				tasqueGroupFragmentListener.setActionBarForInput();
 			}
 		} else {
 			setHasOptionsMenu(true);
 			setActionBar();
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		tasqueGroupFragmentListener.onRefreshCategory();
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		menu.clear();
 		if (DELETING_ENABLED) {
 			inflater.inflate(R.menu.fragment_tasque_completed_delete, menu);
 		} else {
 			inflater.inflate(R.menu.fragment_tasque_completed, menu);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_completed_add_new_task:
 			this.addTask();
 			tasqueGroupFragmentListener.onRefreshCategory();
 		case R.id.menu_completed_delete_start:
 			this.startDeleting();
 			getActivity().supportInvalidateOptionsMenu();
 			return true;
 		case R.id.menu_completed_delete_ok:
 			this.deleteSelected();
 			data = Database.getCompletedTasks(context, listId);
 		case R.id.menu_completed_delete_cancel:
 			this.stopDeleting();
 			getActivity().supportInvalidateOptionsMenu();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	void deleteSelected() {
 		ArrayList<String> tasksToDelete = adapter.getIDsToDelete();
 		Task.delete(context, listId, tasksToDelete);
 	}
 
 	void startDeleting() {
 		abar = getSherlockActivity().getSupportActionBar();
 		abar.setTitle(R.string.fragment_task_group_deleting_title);
 		DELETING_ENABLED = true;
 		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		
 	}
 
 	void stopDeleting() {
 		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		abar = getSherlockActivity().getSupportActionBar();
 		abar.setTitle(context.getString(R.string.fragment_completed_title));
 		DELETING_ENABLED = false;
 		adapter.resetDeletionSelection();
 		this.refreshData();
 	}
 
 	private void addTask() {
		input = (EditText) abar.getCustomView().findViewById(R.id.actionbar_input);
 		String task = input.getText().toString();
 		if (task.length() > 0) {
 			Database.newTask(context, listId, task);
 		}
 		if (!RTMBackend.useRTM(context)) {
 			tasqueGroupFragmentListener.onRefreshCategory(0);
 		}
 	}
 
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		this.addTask();
 		return true;
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (DELETING_ENABLED) {
 			this.stopDeleting();
 			return true;
 		}
 		rightSideFragmentChange.onRemoveRightSideFragment();
 		return false;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if (DELETING_ENABLED) {
 			this.adapter.markForDeletion(arg2);
 		} else {
 			this.adapter.toggle(arg2);
 			String taskId = String.valueOf(adapter.getItemId(arg2));
 			String taskName = adapter.getTaskName(arg2);
 			if (!RTMBackend.useRTM(context)) {
 				tasqueGroupFragmentListener.onRefreshCategory(0);
 				String tasksListId = adapter.getListId(arg2);
 				tasqueGroupFragmentListener.onRefreshCategory(tasksListId);
 			}
 			Task.markActive(context, listId, taskId, taskName);
 			tasqueGroupFragmentListener.onRefreshCategory();
 			this.loadData(listId);
 		}
 		setActionBar();
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 		showNotesFragment.onShowNotesFragment(listId, adapter.getItemStringId(position), adapter.getTaskName(position));
 		return true;
 	}
 
 	public void refreshData() {
 		this.data = Database.getCompletedTasks(context, listId);
 		adapter = new TasqueAdapter(context, data);
 		listView.setAdapter(adapter);
 	}
 
 	public void loadData(String categoryID) {
 		data = Database.getCompletedTasks(context, categoryID);
 		adapter = new TasqueAdapter(context, data);
 		listView.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		return true;
 	}
 
 	public void loadData(Integer position) {
 		this.loadData(String.valueOf(position));
 	}
 
 	public void setActionBar() {
 		abar = getSherlockActivity().getSupportActionBar();
 		abar.setDisplayShowCustomEnabled(false);
 		abar.setDisplayShowTitleEnabled(true);
 		abar.setTitle(context.getString(R.string.fragment_completed_title));
 	}
 }
