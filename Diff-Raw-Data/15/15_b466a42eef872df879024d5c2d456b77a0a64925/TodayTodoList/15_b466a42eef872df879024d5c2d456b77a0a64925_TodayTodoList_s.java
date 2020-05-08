 package com.twocities.pomodoro;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.ActionMode;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.twocities.pomodoro.Utils.TimeUtils;
 import com.twocities.pomodoro.provider.TaskConstract;
 import com.twocities.pomodoro.widget.ActionableToastBar;
 
 public class TodayTodoList extends TodoListFragment {
 	private EditText mQuickStart;
 	private ActionableToastBar mUndoBar;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup containder,
 			Bundle savedInstaceState) {
 		return inflater.inflate(R.layout.fragment_today, containder, false);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		setupViews(view);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		clearQuickAddBar();
 	}
 
 	private void setupViews(View view) {
 		mUndoBar = (ActionableToastBar) view.findViewById(R.id.undo_bar);
 		mQuickStart = (EditText) view.findViewById(R.id.quick_add_pomodoro);
 
 		mQuickStart.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_DONE) {
 					addTask(v.getText().toString());
 					// startClock(v.getText().toString());
 					return true;
 				}
 				return false;
 			}
 		});
 		Button pickerButton = (Button) view.findViewById(R.id.task_picker);
 		pickerButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getActivity(), "task picker", Toast.LENGTH_SHORT)
 						.show();
 				if (getActivity() instanceof HomeActivity) {
 					HomeActivity activity = (HomeActivity) getActivity();
 					FutureTodoList fragment = new FutureTodoList();
 					Bundle args = new Bundle();
 					args.putBoolean(FutureTodoList.EXTRA_SELECTED_MODE, true);
 					fragment.setArguments(args);
 					activity.switchContent(fragment, true);
 				}
 			}
 		});
 	}
 
 	private void addTask(String title) {
 		if (TextUtils.isEmpty(title)) {
 			return;
 		}
 		ContentValues values = new ContentValues();
 		long createTime = TimeUtils.getTimeNow();
 		values.put(TaskConstract.Columns.TITLE, title);
 		values.put(TaskConstract.Columns.REMINDER_TIME, createTime);
 		values.put(TaskConstract.Columns.CREATE_TIME, createTime);
 		values.put(TaskConstract.Columns.FLAG_DONE, 0);
 		values.put(TaskConstract.Columns.FLAG_DEL, 0);
 		values.put(TaskConstract.Columns.FLAG_EMERGENCY, 0);
 		getActivity().getContentResolver().insert(TaskConstract.CONTENT_URI,
 				values);
 		clearQuickAddBar();
 	}
 
 	private void clearQuickAddBar() {
 		InputMethodManager imm = (InputMethodManager) getActivity()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(mQuickStart.getWindowToken(), 0);
 		mQuickStart.clearFocus();
 		mQuickStart.setText("");
 	}
 
 	@Override
 	protected String getSelection() {
 		return " ( " + TaskConstract.Columns.REMINDER_TIME + " >= " + "?"
 				+ " AND " + TaskConstract.Columns.REMINDER_TIME + " <= " + "?"
 				+ " AND " + TaskConstract.Columns.FLAG_DEL + " != " + "?"
 				+ " AND " + TaskConstract.Columns.FLAG_DONE + " != " + "?"
 				+ " ) ";
 	}
 
 	@Override
 	protected String[] getSelectionArgs() {
 		String[] selectionArgs = new String[4];
 		String[] timeSelection = TimeUtils.rangeOfToday();
 		selectionArgs[0] = timeSelection[0];
 		selectionArgs[1] = timeSelection[1];
 		selectionArgs[2] = String.valueOf(1);
 		selectionArgs[3] = String.valueOf(1);
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
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 		super.onDestroyActionMode(mode);
 		
 	}
 }
