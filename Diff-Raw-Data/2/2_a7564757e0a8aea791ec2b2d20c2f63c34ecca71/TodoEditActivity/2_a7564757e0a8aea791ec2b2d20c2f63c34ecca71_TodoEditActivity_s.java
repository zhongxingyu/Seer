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
 	private Button mAddButton, mDeleteButton;
 	private Long mRowId;
 	private TodoDatabaseAdapter mDbHelper;
 	private Calendar mCalendar;
 	private TextView mTextViewDate, mTextViewTime;
 	private int mYear, mMonth, mDay, mHour, mMinute;
 	private DateFormat mDateFormat, mTimeFormat;
 	private Cursor mCursor;
 	static final int DATE_DIALOG_ID = 0, TIME_DIALOG_ID = 1;
 
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		mDbHelper = new TodoDatabaseAdapter(this);
 		mDbHelper.open();
 		setContentView(R.layout.todo_edit);
 
		mCategory = (Spinner) findViewById(R.id.category);
 		mTitleText = (EditText) findViewById(R.id.textViewSummary);
 		mBodyText = (EditText) findViewById(R.id.textViewDescription);
 		mCheckBox = (CheckBox) findViewById(R.id.checkBoxDone);
 		mAddButton = (Button) findViewById(R.id.buttonSave);
 		mDeleteButton = (Button) findViewById(R.id.buttonDelete);
 		mTextViewDate = (TextView) findViewById(R.id.textViewDate);
 		mTextViewTime = (TextView) findViewById(R.id.textViewTime);
 		// http://developer.android.com/reference/java/text/SimpleDateFormat.html
 		mDateFormat = new SimpleDateFormat("dd-MM-yyyy");
 		mTimeFormat = new SimpleDateFormat("HH:mm");
 		mCalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+01:00"));
 
 		Bundle extras = getIntent().getExtras();
 		mRowId = null;
 		mRowId = (bundle == null) ? null : (Long) bundle
 				.getSerializable(TodoDatabaseAdapter.KEY_ROWID);
 		if (extras != null) {
 			mRowId = extras.getLong(TodoDatabaseAdapter.KEY_ROWID);
 		}
 
 		mAddButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				saveToDo();
 				
 			}
 		});
 		mDeleteButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				deleteToDo();
 			}
 		});
 		mTextViewDate.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 		mTextViewTime.setOnClickListener(new View.OnClickListener() {
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
 						mMinute, true);
 		}
 		return null;
 	}
 
 	// When showing the Edit Screen for a ToDo
 	private void populateFields() {
 		if (mRowId != null) {
 			mCursor = mDbHelper.fetchTodo(mRowId);
 			startManagingCursor(mCursor);
 
 			String category = mCursor.getString(mCursor
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_CATEGORY));
 			for (int i = 0; i < mCategory.getCount(); i++) {
 				String s = (String) mCategory.getItemAtPosition(i);
 				Log.e(null, s + " " + category);
 				if (s.equalsIgnoreCase(category)) {
 					mCategory.setSelection(i);
 				}
 			}
 
 			if (mCursor.getInt(mCursor
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_DONE)) == 1) {
 				mCheckBox.setChecked(true);
 			} else {
 				mCheckBox.setChecked(false);
 			}
 
 			mCalendar.setTimeInMillis(Long.valueOf(mCursor.getString(mCursor
 					.getColumnIndex(TodoDatabaseAdapter.KEY_DATE))));
 			mYear = mCalendar.getTime().getYear() + 1900;
 			mMonth = mCalendar.getTime().getMonth();
 			mDay = mCalendar.getTime().getDate();
 			mHour = mCalendar.getTime().getHours() + 1;
 			mMinute = mCalendar.getTime().getMinutes();
 
 			mTitleText.setText(mCursor.getString(mCursor
 					.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_SUMMARY)));
 			mBodyText
 					.setText(mCursor.getString(mCursor
 							.getColumnIndexOrThrow(TodoDatabaseAdapter.KEY_DESCRIPTION)));
 			updateDisplay();
 		} else {
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
 		outState.putSerializable(TodoDatabaseAdapter.KEY_ROWID, mRowId);
 	}
 
 	//
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		populateFields();
 	}
 
 	private void saveToDo() {
 		mCalendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
 		String category = (String) mCategory.getSelectedItem();
 		String summary = mTitleText.getText().toString();
 		String description = mBodyText.getText().toString();
 		boolean done = mCheckBox.isChecked();
 		String date = String.valueOf(mCalendar.getTime().getTime());
 
 		Toast.makeText(
 				this,
 				getResources().getString(R.string.additionalTodo) + " "
 						+ summary + " "
 						+ getResources().getString(R.string.additionalTodoSave),
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
 		finish();
 	}
 
 	private void deleteToDo() {
 		if (mRowId != null) {
 			mDbHelper.deleteTodo(mRowId);
 			String summary = mTitleText.getText().toString();
 			Toast.makeText(
 					this,
 					getResources().getString(R.string.additionalTodo)
 							+ " "
 							+ summary
 							+ " "
 							+ getResources().getString(
 									R.string.additionalTodoDelete),
 					Toast.LENGTH_LONG).show();
 			finish();
 		}
 	}
 }
