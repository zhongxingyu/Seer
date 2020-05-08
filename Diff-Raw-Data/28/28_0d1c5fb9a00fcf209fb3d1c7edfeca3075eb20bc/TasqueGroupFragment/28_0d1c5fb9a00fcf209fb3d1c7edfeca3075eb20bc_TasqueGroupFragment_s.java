 package com.bustiblelemons.tasque.main;
 
 import static com.bustiblelemons.tasque.utilities.Values.TAG;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
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
 import com.bustiblelemons.tasque.main.CategoriesFragment.CategoriesFragmentListener;
 import com.bustiblelemons.tasque.main.CompletedTasksFragment.CompletedTasksListener;
 import com.bustiblelemons.tasque.main.NotesFragment.NotesFragmentListener;
 import com.bustiblelemons.tasque.rtm.RTMBackend;
 import com.bustiblelemons.tasque.rtm.RTMSyncService.OnRTMRefresh;
 import com.bustiblelemons.tasque.settings.SettingsActivity;
 import com.bustiblelemons.tasque.settings.SettingsUtil;
 import com.bustiblelemons.tasque.utilities.Values.FragmentArguments;
 
 public class TasqueGroupFragment extends SherlockFragment implements OnItemLongClickListener, OnItemClickListener,
 		OnTouchListener {
 
 	private boolean DELETING_IN_PROGRESS;
 	private View view;
 	private ListView listView;
 	private TasqueAdapter adapter;
 	private Bundle args;
 	private int categoryID;
 	private String listId;
 	private Cursor data;
 	private Context context;
 	private ActionBar abar;
 
 	private NotesFragmentListener showNotesFragment;
 
 	private CategoriesFragmentListener onShowCategoriesFragment;
 
 	public interface TasqueGroupFragmentListener {
 		public boolean onRefreshCategory();
 
 		public boolean onRefreshCategory(int positionInAdapter);
 
 		public boolean onRefreshCategory(String listId);
 
 		public void setActionBarForInput();
 
 		public void setDefaultCategory(int categoryID);
 	}
 
 	private TasqueGroupFragmentListener tasqueGroupFragmentListener;
 	private OnRTMRefresh rtmRefresh;
 	private CompletedTasksListener completedTasksListener;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		completedTasksListener = (CompletedTasksListener) activity;
 		tasqueGroupFragmentListener = (TasqueGroupFragmentListener) activity;
 		showNotesFragment = (NotesFragmentListener) activity;
 		onShowCategoriesFragment = (CategoriesFragmentListener) activity;
 		rtmRefresh = (OnRTMRefresh) activity;
 	}
 
 	public static TasqueGroupFragment newInstance(Pair<Integer, String> category) {
 		TasqueGroupFragment f = new TasqueGroupFragment();
 		Bundle args = new Bundle();
 		int id = category.first;
 		String name = category.second;
 		Log.d(TAG, "newInstance for " + id + " " + name);
 		args.putString(FragmentArguments.CATEGORY, name);
 		args.putInt(FragmentArguments.ID, id);
 		f.setArguments(args);
 		return f;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = getActivity().getApplicationContext();
 		setHasOptionsMenu(true);
 		abar = getSherlockActivity().getSupportActionBar();
 		setRetainInstance(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		view = inflater.inflate(R.layout.fragment_task_group, null);
 		listView = (ListView) view.findViewById(R.id.fragment_task_group_listview);
 		listView.setOnItemClickListener(this);
 		listView.setOnItemLongClickListener(this);
 		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		args = getArguments();
 		if (args != null) {
 			categoryID = args.getInt(FragmentArguments.ID);
 			listId = String.valueOf(categoryID);
 			// categoryName = args.getString(FragmentArguments.CATEGORY);
 		}
 		this.loadData();
 		context = getActivity().getApplicationContext();
 		adapter = new TasqueAdapter(context, data);
 		listView.setAdapter(adapter);
 		return view;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 	public void loadData() {
 		try {
 			this.data = Database.getTasks(context, listId);
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void refreshData() {
 		this.loadData();
 		this.adapter.notifyDataSetChanged();
 		adapter = new TasqueAdapter(context, data);
 		listView.setAdapter(adapter);
 	}
 
 	public boolean addTask(TextView v) {
 		String taskName = v.getText().toString();
 		if (taskName.length() > 0) {
 			Task.add(context, listId, taskName);
 			v.setText("");
 			this.refreshData();
 			return true;
 		}
 		return false;
 	}
 
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		if (event != null) {
 			return this.addTask(v);
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		menu.clear();
 		if (SettingsUtil.isDefaultCategory(context, categoryID)) {
 			inflater.inflate(R.menu.fragment_tasque_group_default, menu);
 		} else {
 			inflater.inflate(R.menu.fragment_tasque_group, menu);
 		}
 		if (RTMBackend.useRTM(context)) {
 			inflater.inflate(R.menu.rtm_refresh_option, menu);
 		}
 		if (DELETING_IN_PROGRESS) {
 			menu.clear();
 			inflater.inflate(R.menu.fragment_tasque_group_delete_tasks, menu);
 		}
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (DELETING_IN_PROGRESS) {
 			this.disableDeleting();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_add_new_task:
 			this.addTask(Tasque.getActionBarInput());
 			this.refreshData();
 			return true;
 		case R.id.menu_manage_cateogies:
 			onShowCategoriesFragment.onShowCategoriesFragment();
 			return true;
 		case R.id.menu_show_completed:
 			completedTasksListener.onShowCompletedTasksFragment(listId);
 			return true;
 		case R.id.menu_settings:
 			Intent settings = new Intent(context, SettingsActivity.class);
 			startActivity(settings);
 			return true;
 		case R.id.menu_set_default:
 			tasqueGroupFragmentListener.setDefaultCategory(categoryID);
 			getActivity().supportInvalidateOptionsMenu();
 			return true;
 		case R.id.menu_un_set_default:
 			tasqueGroupFragmentListener.setDefaultCategory(0);
 			getActivity().supportInvalidateOptionsMenu();
 			return true;
 		case R.id.menu_rtm_refresh:
 			rtmRefresh.startRTMRefreshService(context, true);
 			return true;
 		case R.id.menu_start_deleting:
 			this.startDeleting();
 			return true;
 		case R.id.menu_delete_tasks_ok:
 			ArrayList<String> tasksToDelete = adapter.getIDsToDelete();
 			Task.delete(context, listId, tasksToDelete);
 		case R.id.menu_delete_tasks_cancel:
 			this.disableDeleting();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void disableDeleting() {
 		this.refreshData();
 		tasqueGroupFragmentListener.setActionBarForInput();
 		DELETING_IN_PROGRESS = false;
 		getActivity().supportInvalidateOptionsMenu();
 		completedTasksListener.onStopDeletingCompletedTasks();
 	}
 
 	private void startDeleting() {
 		abar.setDisplayShowCustomEnabled(false);
 		abar.setDisplayShowTitleEnabled(true);
 		abar.setTitle(R.string.fragment_task_group_deleting_title);
 		DELETING_IN_PROGRESS = true;
 		getActivity().supportInvalidateOptionsMenu();
 		completedTasksListener.onStartDeletingCompletedTasks();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if (DELETING_IN_PROGRESS) {
 			adapter.markForDeletion(arg2);
 		} else {
 			adapter.toggle(arg2);
 			String taskId = String.valueOf(adapter.getItemId(arg2));
 			if (!RTMBackend.useRTM(context)) {
 				if (categoryID > 0) {
 					tasqueGroupFragmentListener.onRefreshCategory(0);
 				}
 			}
 			Task.markDone(context, listId, taskId, adapter.getTaskName(arg2));
 			String tasksListId = adapter.getListId(arg2);
 			tasqueGroupFragmentListener.onRefreshCategory(tasksListId);
 			this.refreshData();
 		}
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
 		showNotesFragment.onShowNotesFragment(listId, adapter.getItemStringId(position), adapter.getTaskName(position));
 		return true;
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		return true;
 	}
 }
