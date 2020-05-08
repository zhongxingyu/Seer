 package de.fhb.maus.android.todolist;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import de.fhb.maus.android.todolist.R;
 import de.fhb.maus.android.todolist.database.TodoDatabaseAdapter;
 
 public class TodoEditActivity extends Activity {
 
 	private Spinner mCategory;
 	private CheckBox mCheckBox;
 	private EditText mTitleText, mBodyText;
 	private Button confirmButton;
 	private Long mRowId;
 	private TodoDatabaseAdapter mDbHelper;
 	private Calendar mCalendar;
 	private boolean backButtonOverClicked = false;
 	private TextView mTextViewDate, mTextViewTime;
 	private int mYear, mMonth, mDay, mHour, mMinute;
 	private DateFormat mDateFormat, mTimeFormat;
 	private Cursor todo;
 	static final int DATE_DIALOG_ID = 0, TIME_DIALOG_ID = 1;
 
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		mDbHelper = new TodoDatabaseAdapter(this);
		
 		mDbHelper.open();
 		setContentView(R.layout.todo_edit);
 		mCategory = (Spinner) findViewById(R.id.category);
 		mTitleText = (EditText) findViewById(R.id.summary);
 		mBodyText = (EditText) findViewById(R.id.description);
 		mCheckBox = (CheckBox) findViewById(R.id.checkBox);
 		confirmButton = (Button) findViewById(R.id.button_save);
 		mTextViewDate = (TextView) findViewById(R.id.textViewDate);
 		mTextViewTime = (TextView) findViewById(R.id.textViewTime);
 
 		mDateFormat = new SimpleDateFormat("dd-MM-yyyy");
 		mTimeFormat = new SimpleDateFormat("HH:mm");
 
 		mRowId = null;
 		mCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+01:00"));
 		Bundle extras = getIntent().getExtras();
 		mRowId = (bundle == null) ? null : (Long) bundle
 				.getSerializable(TodoDatabaseAdapter.KEY_ROWID);
 		if (extras != null) {
 			mRowId = extras.getLong(TodoDatabaseAdapter.KEY_ROWID);
 		}
		
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 		mTextViewDate.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 		mTextViewTime.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(TIME_DIALOG_ID);
 			}
 		});
 		populateFields();
 	}
 	private void updateDisplay() {
 		mCalendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
 		mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
 		mTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
 		mTextViewDate
 				.setText(mDateFormat.format(mCalendar.getTime().getTime()));
 		mTextViewTime
 				.setText(mTimeFormat.format(mCalendar.getTime().getTime()));
 
 	}
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			mYear = year;
 			mMonth = month;
 			mDay = day;
 			updateDisplay();
 		}
 	};
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hour, int minute) {
 			mHour = hour;
 			mMinute = minute;
 			updateDisplay();
 		}
 	};
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 			case DATE_DIALOG_ID :
 				return new DatePickerDialog(this, mDateSetListener, mYear,
 						mMonth, mDay);
 			case TIME_DIALOG_ID :
 				return new TimePickerDialog(this, mTimeSetListener, mHour,
 						mMinute, false);
 		}
 		return null;
 	}
 
 	// When showing the Edit Screen for a ToDo
 	private void populateFields() {
 		if (mRowId != null) {
 			todo = mDbHelper.fetchTodo(mRowId);
 			startManagingCursor(todo);
 
 			String category = todo.getString(todo
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_CATEGORY));
 			for (int i = 0; i < mCategory.getCount(); i++) {
 				String s = (String) mCategory.getItemAtPosition(i);
 				Log.e(null, s + " " + category);
 				if (s.equalsIgnoreCase(category)) {
 					mCategory.setSelection(i);
 				}
 			}
 
 			if (todo.getInt(todo
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_DONE)) == 1) {
 				mCheckBox.setChecked(true);
 			} else {
 				mCheckBox.setChecked(false);
 			}
 
 			mCalendar.setTimeInMillis(Long.valueOf(todo.getString(todo
 					.getColumnIndex(TodoDatabaseAdapter.KEY_DATE))));
 			mYear = mCalendar.getTime().getYear() + 1900;
 			mMonth = mCalendar.getTime().getMonth();
 			mDay = mCalendar.getTime().getDate();
 			mHour = mCalendar.getTime().getHours() + 1;
 			mMinute = mCalendar.getTime().getMinutes();
 
 			mTitleText.setText(todo.getString(todo
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_SUMMARY)));
 			mBodyText
 					.setText(todo.getString(todo
 							.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_DESCRIPTION)));
 			updateDisplay();
		}else{
			mYear = mCalendar.get(Calendar.YEAR);
			mMonth = mCalendar.get(Calendar.MONTH);
			mDay = mCalendar.get(Calendar.DATE);
			mHour = mCalendar.get(Calendar.HOUR);
			mMinute = mCalendar.get(Calendar.MINUTE);
			updateDisplay();
 		}
 	}
 	// by resuming
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState();
 		outState.putSerializable(TodoDatabaseAdapter.KEY_ROWID, mRowId);
 	}
 
 	// Just Save the Edit if not the back button was clicked
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (!backButtonOverClicked) {
 			saveState();
 		}
 	}
 
 	// Looking for the pressevent of the back Button
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO
 		// http://developer.android.com/reference/android/app/Activity.html#onBackPressed%28%29
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			Log.d(this.getClass().getName(), "back button pressed");
 			backButtonOverClicked = true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		populateFields();
 	}
 
 	private void saveState() {
 		mCalendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
 		String category = (String) mCategory.getSelectedItem();
 		String summary = mTitleText.getText().toString();
 		String description = mBodyText.getText().toString();
 		boolean done = mCheckBox.isChecked();
 		String date = String.valueOf(mCalendar.getTime().getTime());
 
 		Toast.makeText(
 				this,
 				getResources().getString(R.string.additionalTodo)
 						+ " "
 						+ summary
 						+ " "
 						+ getResources()
 								.getString(R.string.additionalTodoSaved),
 				Toast.LENGTH_LONG).show();
 
 		if (mRowId == null) {
 			long id = mDbHelper.createTodo(date, category, done, summary,
 					description);
 			if (id > 0) {
 				mRowId = id;
 			}
 		} else {
 			mDbHelper.updateTodo(mRowId, date, category, done, summary,
 					description);
 		}
 	}
 }
