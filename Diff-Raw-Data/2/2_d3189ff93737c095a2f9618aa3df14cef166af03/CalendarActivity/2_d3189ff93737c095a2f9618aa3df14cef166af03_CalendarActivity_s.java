 package com.jnsapps.workshiftcalendar;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Hashtable;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.exina.android.calendar.CalendarView;
 import com.exina.android.calendar.Cell;
 import com.exina.android.calendar.CustomScrollView;
 import com.jnsapps.workshiftcalendar.R;
 import com.jnsapps.workshiftcalendar.db.WorkTableDbAdapter;
 import com.jnsapps.workshiftcalendar.model.Shift;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Joaqun Navarro Salmern
  * 
  */
 public class CalendarActivity extends SherlockActivity implements
 		CalendarView.OnCellTouchListener {
 	static final int PREFERENCES_REQUEST = 0;
 	static final int PATTERN_REQUEST = 1;
 	CalendarView mCalendarView = null;
 
 	private static final int DIALOG_SET_WORK_SHIFT_ID = 0;
 	private static final int DIALOG_CHANGE_WORK_SHIFT_ID = 1;
 	private static final int DIALOG_HELP_ID = 2;
 	private static final int DIALOG_OVERTIME_ID = 3;
 	private static final int DIALOG_DAY_DETAIL = 4;
 	private static final int DIALOG_DAILYNOTES_ID = 5;
 	private static String DEFAULT_SHIFTS = "";
 	private String mCustomShifts;
 	private ArrayList<Shift> mShiftList;
 	private Hashtable<String, Shift> mShiftTable;
 	private CharSequence[] changeItems;
 	private ArrayList<String> dialogItems;
 	private ArrayList<String> currentItems;
 
 	private Cell selectedCell = null;
 
 	private WorkTableDbAdapter mDatabase;
 
 	// ViewPager test
 	private ViewPager awesomePager;
 	private Context cxt;
 	private AwesomePagerAdapter awesomeAdapter;
 	private int focusedPage = 1;
 
 	private AlertDialog helpDialog;
 	private StateHolder mStateHolder;
 
 	private AlertDialog overtimeDialog, dailyNotesDialog;
 
 	private DateFormat df = SimpleDateFormat.getDateInstance();
 
 	private static final String SHARE_PATH = "/WorkShiftCalendar";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Object retained = getLastNonConfigurationInstance();
 		if (retained != null && retained instanceof StateHolder) {
 			mStateHolder = (StateHolder) retained;
 			selectedCell = mStateHolder.mSavedSelectedCell;
 		} else {
 			mStateHolder = new StateHolder();
 		}
 		DEFAULT_SHIFTS = getString(R.string.default_shifts);
 		refreshShiftsPreferences();
 		setContentView(R.layout.main);
 
 		mDatabase = new WorkTableDbAdapter(this);
 
 		// ViewPager
 		cxt = this;
 
 		awesomePager = (ViewPager) findViewById(R.id.awesomepager);
 		awesomePager.setOffscreenPageLimit(3);
 		awesomeAdapter = new AwesomePagerAdapter();
 		awesomePager.setAdapter(awesomeAdapter);
 
 		awesomePager.setCurrentItem(1, false);
 
 		awesomePager
 				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 					@Override
 					public void onPageScrolled(int position,
 							float positionOffset, int positionOffsetPixels) {
 					}
 
 					@Override
 					public void onPageScrollStateChanged(int state) {
 						if (state == ViewPager.SCROLL_STATE_IDLE) {
 							CustomScrollView[] csv = new CustomScrollView[3];
 							CalendarView[] cv = new CalendarView[3];
 							for (int i = 0; i < 3; i++) {
 								csv[i] = (CustomScrollView) awesomePager
 										.getChildAt(i);
 								cv[i] = (CalendarView) csv[i]
 										.findViewById(R.id.calendarview);
 							}
 							if (focusedPage == 0) {
 								for (CalendarView calendarView : cv) {
 									calendarView.previousMonth();
 								}
 
 							} else if (focusedPage == 2) {
 								for (CalendarView calendarView : cv) {
 									calendarView.nextMonth();
 								}
 							}
 
 							// always set to middle page to continue to be able
 							// to scroll up/down
 							awesomePager.setCurrentItem(1, false);
 						}
 					}
 
 					@Override
 					public void onPageSelected(int position) {
 						focusedPage = position;
 					}
 				});
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (helpDialog != null && helpDialog.isShowing()) {
 			helpDialog.dismiss();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshShiftsPreferences();
 		if (mCalendarView != null) {
 			mCalendarView.invalidate();
 		}
 		if (mStateHolder.mIsShowingDialog) {
 			helpDialog.show();
 		} else if (!mStateHolder.mDialogClosed) {
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(getBaseContext());
 			boolean showHelp = prefs.getBoolean("showHelpV140", true);
 			if (showHelp) {
 				showDialog(DIALOG_HELP_ID);
 			}
 		}
 	}
 
 	private void refreshShiftsPreferences() {
 		// Create default shifts if preferences are empty
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		mCustomShifts = prefs.getString("shifts", "");
 		if (mCustomShifts.equals("")) {
 			SharedPreferences.Editor editor1 = prefs.edit();
 			editor1.putString("shifts", DEFAULT_SHIFTS);
 			editor1.commit();
 			mCustomShifts = DEFAULT_SHIFTS;
 		}
 		mShiftList = Shift.parse(mCustomShifts);
 		mShiftTable = new Hashtable<String, Shift>();
 		changeItems = new CharSequence[mShiftList.size()];
 		int i = 0;
 		dialogItems = new ArrayList<String>();
 		String itemRow;
 		for (Shift s : mShiftList) {
 			itemRow = s.getAbbreviation() + " (" + s.getName() + ")";
 			changeItems[i++] = itemRow;
 			dialogItems.add(itemRow);
 			mShiftTable.put(s.getAbbreviation(), s);
 		}
 		dialogItems.add(getString(R.string.overtime_set));
 		dialogItems.add(getString(R.string.daily_notes));
 
 	}
 
 	public void onTouch(Cell cell) {
 		selectedCell = cell;
 		mStateHolder.mSavedSelectedCell = cell;
 
 		if (cell.getOvertime().equals("") && cell.getShift().equals("")
 				&& !cell.existsDailyNote()) {
 			Toast.makeText(getApplicationContext(), R.string.long_press_help,
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		Bundle data = new Bundle();
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, mCalendarView.getYear());
 		c.set(Calendar.MONTH, mCalendarView.getMonth());
 		c.set(Calendar.DAY_OF_MONTH, cell.getDayOfMonth());
 		data.putSerializable("date", df.format(c.getTime()));
 		float totalHours = 0;
 		if (!cell.getOvertime().equals("")) {
 			mDatabase = mDatabase.open();
 			long overtimeMinutes = mDatabase.getOvertime(c);
 			mDatabase.close();
 			data.putString("overtime", Helper.formatInterval(overtimeMinutes));
 			totalHours += overtimeMinutes / 60f;
 		} else {
 			data.putString("overtime", getString(R.string.unknown));
 		}
 		if (cell.existsDailyNote()) {
 			mDatabase = mDatabase.open();
 			String dailyNotes = mDatabase.getDailyNote(c);
 			mDatabase.close();
 			data.putString("dailyNotes", dailyNotes);
 		} else {
 			data.putString("dailyNotes", getString(R.string.unknown));
 		}
 		if (!cell.getShift().equals("")) {
 			Shift s = mShiftTable.get(selectedCell.getShift());
 			if (s == null) {
 				mDatabase = mDatabase.open();
 				s = mDatabase.getShiftObject(c);
 				mDatabase.close();
 				data.putString("shift", selectedCell.getShift());
 			}
 			data.putString("shift", s.getName());
 			totalHours += s.getHours();
 		} else {
 			data.putString("shift", getString(R.string.unknown));
 		}
 		String totalHoursString = Helper.formatInterval(totalHours);
 		data.putString("hours", totalHoursString);
 		showDialog(DIALOG_DAY_DETAIL, data);
 	}
 
 	public void onLongPress(Cell cell) {
 		selectedCell = cell;
 		mStateHolder.mSavedSelectedCell = cell;
 		showDialog(DIALOG_SET_WORK_SHIFT_ID);
 	}
 
 	protected Dialog onCreateDialog(int id, Bundle bundle) {
 		Dialog dialog = null;
 		LayoutInflater inflater = LayoutInflater.from(this);
 		switch (id) {
 		case DIALOG_SET_WORK_SHIFT_ID:
 			// do the work to define the set work shift Dialog
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(R.string.select_workshift);
 			builder.setItems(changeItems, new WorkShiftDialogListener());
 			AlertDialog alert = builder.create();
 			return alert;
 		case DIALOG_CHANGE_WORK_SHIFT_ID:
 			// do the work to define the change work shift Dialog
 			AlertDialog.Builder changeBuilder = new AlertDialog.Builder(this);
 			changeBuilder.setTitle(R.string.select_new_workshift);
 			changeBuilder.setItems(changeItems,
 					new ChangeWorkShiftDialogListener());
 			AlertDialog changeAlert = changeBuilder.create();
 			return changeAlert;
 		case DIALOG_HELP_ID:
 			AlertDialog.Builder helpBuilder;
 			View layout = inflater.inflate(R.layout.dialog_help, null);
 			helpBuilder = new AlertDialog.Builder(this);
 			helpBuilder.setView(layout);
 			helpBuilder.setTitle(getString(R.string.help_dialog_title));
 			helpBuilder.setPositiveButton(
 					getString(R.string.help_dialog_close),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialoginterface,
 								int id) {
 							dialoginterface.cancel();
 							mStateHolder.mDialogClosed = true;
 						}
 					});
 			helpBuilder.setNegativeButton(
 					getString(R.string.help_dialog_close_forever),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialoginterface,
 								int id) {
 							SharedPreferences prefs = PreferenceManager
 									.getDefaultSharedPreferences(getBaseContext());
 							SharedPreferences.Editor editor1 = prefs.edit();
 							editor1.putBoolean("showHelp", false);
 							editor1.putBoolean("showHelpV140", false);
 							editor1.commit();
 							dialoginterface.cancel();
 							mStateHolder.mDialogClosed = true;
 						}
 					});
 			helpDialog = helpBuilder.create();
 			return helpDialog;
 		case DIALOG_OVERTIME_ID:
 			View overtimeLayout = inflater.inflate(R.layout.dialog_overtime,
 					null);
 			AlertDialog.Builder overtimeBuilder = new AlertDialog.Builder(this);
 			overtimeBuilder.setView(overtimeLayout).setTitle(
 					R.string.overtime_set);
 			overtimeBuilder.setPositiveButton(
 					getString(R.string.save_button_text),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialoginterface,
 								int id) {
 							dialoginterface.cancel();
 							setOvertime(overtimeDialog);
 						}
 					});
 			overtimeBuilder.setNegativeButton(
 					getString(R.string.cancel_button_text),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialoginterface,
 								int id) {
 							dialoginterface.cancel();
 						}
 					});
 			overtimeDialog = overtimeBuilder.create();
 			return overtimeDialog;
 		case DIALOG_DAILYNOTES_ID:
 			View dailyNotesLayout = inflater.inflate(
 					R.layout.dialog_dailynotes, null);
 			AlertDialog.Builder dailyNotesBuilder = new AlertDialog.Builder(
 					this);
 			dailyNotesBuilder.setView(dailyNotesLayout).setTitle(
 					R.string.daily_notes);
 			dailyNotesBuilder.setPositiveButton(
 					getString(R.string.save_button_text),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialoginterface,
 								int id) {
 							dialoginterface.cancel();
 							setDailyNotes(dailyNotesDialog);
 						}
 					});
 			dailyNotesBuilder.setNegativeButton(
 					getString(R.string.cancel_button_text),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialoginterface,
 								int id) {
 							dialoginterface.cancel();
 						}
 					});
 			dailyNotesDialog = dailyNotesBuilder.create();
 			return dailyNotesDialog;
 		case DIALOG_DAY_DETAIL:
 			Dialog dayDetailDialog = new Dialog(this);
 			dayDetailDialog.setContentView(R.layout.dialog_day_detail);
 			dayDetailDialog.setCanceledOnTouchOutside(true);
 			return dayDetailDialog;
 		}
 		return dialog;
 	}
 
 	@Override
 	protected void onPrepareDialog(final int id, final Dialog dialog,
 			Bundle bundle) {
 		super.onPrepareDialog(id, dialog);
 		switch (id) {
 		case DIALOG_SET_WORK_SHIFT_ID:
 			currentItems = (ArrayList<String>) dialogItems.clone();
 			ListView dropdown = ((AlertDialog) dialog).getListView();
 			if (selectedCell == null)
 				break;
 			boolean shiftSet = selectedCell.getShift().length() == 0 ? false
 					: true;
 			boolean overtimeSet = selectedCell.getOvertime().length() == 0 ? false
 					: true;
 			boolean dailyNotesSet = selectedCell.existsDailyNote();
 			if (shiftSet) {
 				currentItems.add(0, getString(R.string.change_shift));
 				currentItems.add(getString(R.string.delete));
 			}
 			if (overtimeSet) {
 				currentItems.add(getString(R.string.overtime_delete));
 			}
 			if (dailyNotesSet) {
 				currentItems.add(getString(R.string.daily_notes_delete));
 			}
 			String[] itemArray = new String[currentItems.size()];
 			itemArray = currentItems.toArray(itemArray);
 			dropdown.setAdapter(new ArrayAdapter<CharSequence>(this,
 					android.R.layout.select_dialog_item, itemArray));
 			break;
 		case DIALOG_CHANGE_WORK_SHIFT_ID:
 			ListView dropdown2 = ((AlertDialog) dialog).getListView();
 			dropdown2.setAdapter(new ArrayAdapter<CharSequence>(this,
 					android.R.layout.select_dialog_item, changeItems));
 			break;
 		case DIALOG_DAILYNOTES_ID:
 			EditText dailyNotesET = (EditText) dialog
 					.findViewById(R.id.dailynotes_edit_text);
 			if (bundle != null) {
 				dailyNotesET.setText(bundle.getString("dailyNotes"));
 			} else {
 				dailyNotesET.setText("");
 			}
 			break;
 		case DIALOG_DAY_DETAIL:
 			dialog.setTitle(getString(R.string.detail) + ": "
 					+ bundle.getString("date"));
 			TextView shiftTV = (TextView) dialog
 					.findViewById(R.id.dialog_day_detail_workshift);
 			TextView overtimeTV = (TextView) dialog
 					.findViewById(R.id.dialog_day_detail_overtime);
 			TextView totalHoursTV = (TextView) dialog
 					.findViewById(R.id.dialog_day_detail_total_hours);
 			TextView dailyNotesTV = (TextView) dialog
 					.findViewById(R.id.dialog_day_detail_dailyNotes);
 			shiftTV.setText(bundle.getString("shift"));
 			overtimeTV.setText(bundle.getString("overtime"));
 			totalHoursTV.setText(bundle.getString("hours"));
 			dailyNotesTV.setText(bundle.getString("dailyNotes"));
 			break;
 		}
 
 	}
 
 	private void setOvertime(Dialog dialog) {
 		if (selectedCell == null) {
 			Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 		EditText hoursEditText = (EditText) dialog
 				.findViewById(R.id.overtime_hours_edit_text);
 		EditText minutesEditText = (EditText) dialog
 				.findViewById(R.id.overtime_minutes_edit_text);
 		String hours = hoursEditText.getText().toString();
 		String minutes = minutesEditText.getText().toString();
 
 		if (hours.equals("") && minutes.equals("")) {
 			Toast.makeText(this, R.string.error_form_complete_something,
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, mCalendarView.getYear());
 		c.set(Calendar.MONTH, mCalendarView.getMonth());
 		c.set(Calendar.DAY_OF_MONTH, selectedCell.getDayOfMonth());
 
 		try {
 			hours = hours.equals("") ? "0" : hours;
 			minutes = minutes.equals("") ? "0" : minutes;
 			long hoursFloat = Long.valueOf(hours);
 			long minutesFloat = Long.valueOf(minutes);
 			long overtimeMinutes = hoursFloat * 60 + minutesFloat;
 
 			mDatabase = mDatabase.open();
 			mDatabase.setOvertime(c, overtimeMinutes);
 			mDatabase.close();
 			mCalendarView.invalidate();
 
 			Toast.makeText(this, R.string.overtime_show_tip, Toast.LENGTH_LONG)
 					.show();
 
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 
 	}
 
 	private void setDailyNotes(Dialog dialog) {
 		if (selectedCell == null) {
 			Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 		EditText dailyNotesEditText = (EditText) dialog
 				.findViewById(R.id.dailynotes_edit_text);
 		String dailyNotes = dailyNotesEditText.getText().toString();
 
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, mCalendarView.getYear());
 		c.set(Calendar.MONTH, mCalendarView.getMonth());
 		c.set(Calendar.DAY_OF_MONTH, selectedCell.getDayOfMonth());
 
 		try {
 			mDatabase = mDatabase.open();
 			if (dailyNotes.length() == 0)
 				mDatabase.deleteDailyNotes(c);
 			else
 				mDatabase.setDailyNote(c, dailyNotes);
 			Toast.makeText(this, R.string.daily_notes_saved_toast,
 					Toast.LENGTH_LONG).show();
 			mDatabase.close();
 			mCalendarView.invalidate();
 
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT)
 					.show();
 			return;
 		}
 
 	}
 
 	private class WorkShiftDialogListener implements
 			DialogInterface.OnClickListener {
 
 		@Override
 		public void onClick(DialogInterface dialog, int item) {
 			CharSequence option = currentItems.get(item);
 
 			Toast.makeText(getApplicationContext(), option, Toast.LENGTH_SHORT)
 					.show();
 
 			Calendar c = Calendar.getInstance();
 			c.set(Calendar.YEAR, mCalendarView.getYear());
 			c.set(Calendar.MONTH, mCalendarView.getMonth());
 			c.set(Calendar.DAY_OF_MONTH, selectedCell.getDayOfMonth());
 
 			if (option.equals(getString(R.string.change_shift))) {
 				showDialog(DIALOG_CHANGE_WORK_SHIFT_ID);
 			} else if (option.equals(getString(R.string.delete))) {
 				mDatabase = mDatabase.open();
 				mDatabase.deleteShift(c);
 				mDatabase.close();
 			} else if (option.equals(getString(R.string.overtime_set))) {
 				showDialog(DIALOG_OVERTIME_ID);
 			} else if (option.equals(getString(R.string.overtime_delete))) {
 				mDatabase = mDatabase.open();
 				mDatabase.deleteOvertime(c);
 				mDatabase.close();
 			} else if (option.equals(getString(R.string.daily_notes))) {
 				if (selectedCell.existsDailyNote()) {
 					Bundle data = new Bundle();
 					data.putSerializable("date", df.format(c.getTime()));
 					mDatabase = mDatabase.open();
 					String dailyNotes = mDatabase.getDailyNote(c);
 					mDatabase.close();
 					data.putString("dailyNotes", dailyNotes);
 					showDialog(DIALOG_DAILYNOTES_ID, data);
 				} else {
 					showDialog(DIALOG_DAILYNOTES_ID);
 				}
 			} else if (option.equals(getString(R.string.daily_notes_delete))) {
 				mDatabase = mDatabase.open();
 				mDatabase.deleteDailyNotes(c);
 				mDatabase.close();
 			} else {
 				String abbreviation = currentItems.get(item).split(" \\(")[0];
 				mDatabase = mDatabase.open();
 				Shift shift = mShiftTable.get(abbreviation);
 				mDatabase.setWorkShift(c, abbreviation, shift.getName(),
 						shift.getColor(), shift.getHours());
 				mDatabase.close();
 			}
 			mCalendarView.invalidate();
 		}
 
 	}
 
 	private class ChangeWorkShiftDialogListener implements
 			DialogInterface.OnClickListener {
 
 		@Override
 		public void onClick(DialogInterface dialog, int item) {
 			CharSequence option = changeItems[item];
 
 			Calendar c = Calendar.getInstance();
 			c.set(Calendar.YEAR, mCalendarView.getYear());
 			c.set(Calendar.MONTH, mCalendarView.getMonth());
 			c.set(Calendar.DAY_OF_MONTH, selectedCell.getDayOfMonth());
 
 			String newShiftAbbreviation = option.toString().split(" \\(")[0];
 			mDatabase = mDatabase.open();
 			Shift newShift = mShiftTable.get(newShiftAbbreviation);
 			String previousShift = mDatabase.changeWorkShift(c,
 					newShiftAbbreviation, newShift.getName(),
 					newShift.getColor(), newShift.getHours());
 			mDatabase.close();
 			if (previousShift != null) {
 
 				mCalendarView.invalidate();
 			}
 
 		}
 
 	}
 
 	private class AwesomePagerAdapter extends PagerAdapter {
 
 		private int previousPos = 1;
 		CalendarView calView;
 		Calendar now = Calendar.getInstance();
 
 		private int count = 3;
 
 		@Override
 		public int getCount() {
 			return count;
 		}
 
 		/**
 		 * Create the page for the given position. The adapter is responsible
 		 * for adding the view to the container given here, although it only
 		 * must ensure this is done by the time it returns from
 		 * {@link #finishUpdate()}.
 		 * 
 		 * @param container
 		 *            The containing View in which the page will be shown.
 		 * @param position
 		 *            The page position to be instantiated.
 		 * @return Returns an Object representing the new page. This does not
 		 *         need to be a View, but can be some other container of the
 		 *         page.
 		 */
 		@Override
 		public Object instantiateItem(ViewGroup collection, int position) {
 			CustomScrollView container = (CustomScrollView) LayoutInflater
 					.from(cxt).inflate(R.layout.page, null);
 			LinearLayout wrapper = (LinearLayout) container.getChildAt(0);
 			calView = (CalendarView) wrapper.getChildAt(0);
 			if (previousPos > position) {
 				now.add(Calendar.MONTH, -1);
 				calView.setDate(now);
 			} else if (previousPos < position) {
 				now.add(Calendar.MONTH, position - previousPos);
 				calView.setDate(now);
 			}
 			previousPos = position;
 			((ViewPager) collection).addView(container);
 
 			return container;
 		}
 
 		/**
 		 * Remove a page for the given position. The adapter is responsible for
 		 * removing the view from its container, although it only must ensure
 		 * this is done by the time it returns from {@link #finishUpdate()}.
 		 * 
 		 * @param container
 		 *            The containing View from which the page will be removed.
 		 * @param position
 		 *            The page position to be removed.
 		 * @param object
 		 *            The same object that was returned by
 		 *            {@link #instantiateItem(View, int)}.
 		 */
 		@Override
 		public void destroyItem(View collection, int position, Object view) {
 			((ViewPager) collection).removeView((CustomScrollView) view);
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object) {
 			return view == ((CustomScrollView) object);
 		}
 
 		/**
 		 * Called when the a change in the shown pages has been completed. At
 		 * this point you must ensure that all of the pages have actually been
 		 * added or removed from the container as appropriate.
 		 * 
 		 * @param container
 		 *            The containing View which is displaying this adapter's
 		 *            page views.
 		 */
 		@Override
 		public void finishUpdate(View arg0) {
 			CustomScrollView sv = (CustomScrollView) awesomePager.getChildAt(1);
 			LinearLayout wrapper = (LinearLayout) sv.getChildAt(0);
 			mCalendarView = (CalendarView) wrapper.getChildAt(0);
 			mCalendarView.setOnCellTouchListener(CalendarActivity.this);
 		}
 
 		@Override
 		public void restoreState(Parcelable arg0, ClassLoader arg1) {
 		}
 
 		@Override
 		public Parcelable saveState() {
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View arg0) {
 		}
 
 	}
 
 	protected void initNote() {
 		CalendarView cv = (CalendarView) ((CustomScrollView) awesomePager
				.getChildAt(0)).findViewById(R.id.calendarview);
 		Intent i = new Intent(this, NoteActivity.class);
 		i.putExtra(NoteActivity.EXTRA_MONTH_ID, cv.getMonth());
 		i.putExtra(NoteActivity.EXTRA_YEAR_ID, cv.getYear());
 		startActivity(i);
 	}
 
 	protected void initStatistics() {
 		CalendarView cv = (CalendarView) ((CustomScrollView) awesomePager
 				.getChildAt(0)).findViewById(R.id.calendarview);
 		Intent i = new Intent(this, StatisticsActivity.class);
 		i.putExtra(StatisticsActivity.EXTRA_YEAR_ID, cv.getYear());
 		startActivity(i);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.share_menu_item:
 			share();
 			return true;
 		case R.id.preferences_menu_item:
 			Intent p = new Intent(this, Preferences.class);
 			startActivityForResult(p, PREFERENCES_REQUEST);
 			return true;
 		case R.id.edit_menu_item:
 			Intent i = new Intent(this, ShiftListActivity.class);
 			startActivity(i);
 			return true;
 		case R.id.notes_menu_item:
 			initNote();
 			return true;
 		case R.id.statistics_menu_item:
 			initStatistics();
 			return true;
 		case R.id.help_menu_item:
 			showDialog(DIALOG_HELP_ID);
 			return true;
 		case R.id.backup_menu_item:
 			Intent j = new Intent(this, BackupActivity.class);
 			startActivityForResult(j, PREFERENCES_REQUEST);
 			return true;
 		case R.id.pattern_menu_item:
 			Intent ii = new Intent(this, ShiftPatternActivity.class);
 			startActivityForResult(ii, PATTERN_REQUEST);
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_CANCELED)
 			return;
 		if (requestCode == PREFERENCES_REQUEST) {
 			if (resultCode == Preferences.PREFERENCES_CHANGED) {
 				try {
 					for (int i = 0; i < 3; i++) {
 						CustomScrollView csv = (CustomScrollView) awesomePager
 								.getChildAt(i);
 						CalendarView cv = (CalendarView) csv
 								.findViewById(R.id.calendarview);
 						cv.refreshPreferences();
 						cv.invalidate();
 					}
 				} catch (Exception e) {
 				}
 			}
 		} else if (requestCode == PATTERN_REQUEST) {
 			if (resultCode == ShiftPatternActivity.PATTERN_SET) {
 				try {
 					for (int i = 0; i < 3; i++) {
 						CustomScrollView csv = (CustomScrollView) awesomePager
 								.getChildAt(i);
 						CalendarView cv = (CalendarView) csv
 								.findViewById(R.id.calendarview);
 						cv.invalidate();
 					}
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 
 	private void share() {
 		// Take screeshot
 		CalendarView cv = (CalendarView) ((CustomScrollView) awesomePager
 				.getChildAt(0)).findViewById(R.id.calendarview);
 		String imageName = getString(R.string.screenshot_prefix) + "-"
 				+ cv.getYear() + "-" + (cv.getMonth() + 1);
 		String imagePath = takeScreenshot(imageName);
 		if (imagePath == null)
 			return;
 		Toast.makeText(this, R.string.screenshot_saved, Toast.LENGTH_SHORT)
 				.show();
 		// Share
 		Intent share = new Intent(Intent.ACTION_SEND);
 		share.setType("image/*");
 		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imagePath));
 		startActivity(Intent.createChooser(share,
 				getString(R.string.share_label_menu)));
 	}
 
 	private String takeScreenshot(String imageName) {
 		// image naming and path to include sd card appending name you choose
 		// for file
 		String mParentFolder = Environment.getExternalStorageDirectory()
 				.toString() + "/" + SHARE_PATH;
 		File mParentDir = new File(mParentFolder);
 		mParentDir.mkdirs();
 		String mPath = mParentFolder + "/" + imageName + ".jpg";
 
 		// create bitmap screen capture
 		Bitmap bitmap;
 		View v1 = findViewById(R.id.awesomepager);
 		v1.setDrawingCacheEnabled(true);
 		bitmap = Bitmap.createBitmap(v1.getDrawingCache());
 		v1.setDrawingCacheEnabled(false);
 
 		OutputStream fout = null;
 		File imageFile = new File(mPath);
 
 		try {
 			fout = new FileOutputStream(imageFile);
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 97, fout);
 			fout.flush();
 			fout.close();
 
 		} catch (Exception e) {
 			mPath = null;
 		}
 		return mPath;
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return mStateHolder;
 	}
 
 	private static class StateHolder {
 		boolean mIsShowingDialog = false;
 		boolean mDialogClosed = false;
 		Cell mSavedSelectedCell;
 
 		public StateHolder() {
 		}
 	}
 
 }
