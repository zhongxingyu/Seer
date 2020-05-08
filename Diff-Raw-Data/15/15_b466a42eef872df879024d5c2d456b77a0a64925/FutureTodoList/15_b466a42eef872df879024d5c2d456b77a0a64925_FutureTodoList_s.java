 package com.twocities.pomodoro;
 
 import android.app.ActionBar.OnNavigationListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 
 import com.twocities.pomodoro.Utils.TimeUtils;
 import com.twocities.pomodoro.provider.TaskConstract;
 import com.twocities.pomodoro.widget.ActionableToastBar;
 
 public class FutureTodoList extends TodoListFragment implements
 		OnNavigationListener {
 
 	public static final String EXTRA_SELECTED_MODE = "extra_selected_mode";
 	private ActionableToastBar mUndoBar;
 	private boolean mMutilSeletedMode = false;
 	private ActionMode mActionMode;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup containder,
 			Bundle savedInstaceState) {
 		return inflater.inflate(R.layout.fragment_future, containder, false);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		mUndoBar = (ActionableToastBar) view.findViewById(R.id.undo_bar);
 	}
 
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);

 		getActivity().findViewById(R.id.add_task).setOnClickListener(
 				new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						addTask();
 					}
 				});
 		if (getArguments() != null) {
 			mMutilSeletedMode = getArguments().getBoolean(EXTRA_SELECTED_MODE,
 					false);
 		}
 		// handleMultiMode();
 	}
 
 	@SuppressWarnings("unused")
 	private void handleMultiMode() {
 		if (mMutilSeletedMode) {
 			if (mActionMode == null) {
 				mActionMode = getActivity().startActionMode(this);
 			}
 		}
 	}
 
 	private void addTask() {
 		Intent intent = new Intent(getActivity(), TaskEditActivity.class);
 		this.startActivity(intent);
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		return false;
 	}
 
 	@Override
 	protected String getSelection() {
 		return " ( " + TaskConstract.Columns.FLAG_DEL + " != " + "?" + " AND "
 				+ TaskConstract.Columns.FLAG_DONE + " != " + "?" + " AND "
 				+ " ( " + TaskConstract.Columns.REMINDER_TIME + " < " + "?"
 				+ " OR " + TaskConstract.Columns.REMINDER_TIME + " > " + "?"
 				+ " ) " + " ) ";
 	}
 
 	@Override
 	protected String[] getSelectionArgs() {
 		String[] selectionArgs = new String[4];
 		String[] timeSelection = TimeUtils.rangeOfToday();
 		selectionArgs[0] = String.valueOf(1);
 		selectionArgs[1] = String.valueOf(1);
 		selectionArgs[2] = timeSelection[0];
 		selectionArgs[3] = timeSelection[1];
 		return selectionArgs;
 	}
 
 	@Override
 	protected ActionableToastBar getUndoBar() {
 		return this.mUndoBar;
 	}
 
 	@Override
 	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 		MenuInflater inflater = mode.getMenuInflater();
 		inflater.inflate(R.menu.bulk_edit, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
