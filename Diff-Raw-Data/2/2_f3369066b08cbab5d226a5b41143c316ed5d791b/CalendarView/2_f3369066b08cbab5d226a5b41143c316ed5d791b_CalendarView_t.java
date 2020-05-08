 /*
  * Copyright (C) 2011 Chris Gao <chris@exina.net>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Gesture detection: Joaquin Navarro
  * 
  */
 
 package com.exina.android.calendar;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import com.jnsapps.workshiftcalendar.R;
 import com.jnsapps.workshiftcalendar.db.DataBaseHelper;
 import com.jnsapps.workshiftcalendar.db.WorkTableDbAdapter;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.util.MonthDisplayHelper;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class CalendarView extends View {
 	// private static int WEEK_TOP_MARGIN = 64;
 	private static int WEEK_TOP_MARGIN;
 	private static int CELL_WIDTH;
 	private static int CELL_HEIGH;
 	// private static int CELL_MARGIN_TOP = 100;
 	private static int CELL_MARGIN_TOP;
 	// private static int CELL_MARGIN_LEFT = 39;
 	private static int CELL_MARGIN_LEFT;
 	private static float CELL_TEXT_SIZE;
 	private static float SHIFT_SIZE;
 	private static float PREV_SHIFT_SIZE;
 	// private static float TODAY_STROKE_WIDTH = 3f;
 	private static float TODAY_STROKE_WIDTH;
 	// private static float MONTH_YEAR_TEXT_SIZE = 32f;
 	private static float MONTH_YEAR_TEXT_SIZE;
 	// private static float WEEK_TITLE_TEXT_SIZE = 16f;
 	private static float WEEK_TITLE_TEXT_SIZE;
 
 	private Calendar mRightNow = null;
 	private Cell mToday = null;
 	private Cell[][] mCells = new Cell[6][7];
 	private OnCellTouchListener mOnCellTouchListener = null;
 	public MonthDisplayHelper mHelper;
 	private int firstDayOfWeek;
 
 	// Month and week colors
 	private int monthBackgroundColor, monthTextColor, weekBackgroundColor,
 			weekTextColor, todayColor, overtimeColor, dailyNotesColor;
 	private String overtimeAbbreviation = "OT";
 
 	// Gesture detection
 	private GestureDetector gestureDetector;
 
 	// Database
 	private WorkTableDbAdapter mDatabase;
 	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 
 	// Stats
 	private Statistics mStats;
 
 	private Context mContext;
 
 	public interface OnCellTouchListener {
 		public void onTouch(Cell cell);
 
 		public void onLongPress(Cell cell);
 	}
 
 	public CalendarView(Context context) {
 		this(context, null);
 	}
 
 	public CalendarView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		mContext = context;
 		// Database
 		mDatabase = new WorkTableDbAdapter(context);
 
 		mRightNow = Calendar.getInstance();
 		refreshPreferences();
 		initCalendarView();
 
 		// Gesture detection
 		gestureDetector = new GestureDetector(new MyGestureDetector());
 		this.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(final View view, final MotionEvent event) {
 				gestureDetector.onTouchEvent(event);
 				return true;
 			}
 		});
 		// Stats
 		mStats = new Statistics(mContext, this);
 
 	}
 
 	class MyGestureDetector extends SimpleOnGestureListener {
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			if (mOnCellTouchListener != null) {
 				for (Cell[] week : mCells) {
 					for (Cell day : week) {
 						if (day.hitTest((int) e.getX(), (int) e.getY())) {
 							mOnCellTouchListener.onTouch(day);
 						}
 					}
 				}
 			}
 			return true;
 		}
 
 		@Override
 		public void onLongPress(MotionEvent e) {
 			if (mOnCellTouchListener != null) {
 				for (Cell[] week : mCells) {
 					for (Cell day : week) {
 						if (day.hitTest((int) e.getX(), (int) e.getY())) {
 							mOnCellTouchListener.onLongPress(day);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	private void initCalendarView() {
 		Resources res = getResources();
 		CELL_MARGIN_LEFT = 0;
 		CELL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.cell_text_size);
 		SHIFT_SIZE = res.getDimensionPixelSize(R.dimen.shift_text_size);
 		PREV_SHIFT_SIZE = res
 				.getDimensionPixelSize(R.dimen.prev_shift_text_size);
 		TODAY_STROKE_WIDTH = res
 				.getDimensionPixelSize(R.dimen.today_stroke_width);
 		MONTH_YEAR_TEXT_SIZE = res
 				.getDimensionPixelSize(R.dimen.month_year_text_size);
 		WEEK_TOP_MARGIN = (int) res
 				.getDimensionPixelSize(R.dimen.week_top_margin);
 		WEEK_TITLE_TEXT_SIZE = res
 				.getDimensionPixelSize(R.dimen.week_title_text_size);
 		CELL_MARGIN_TOP = (int) res
 				.getDimensionPixelSize(R.dimen.cell_margin_top);
 	}
 
 	public void refreshPreferences() {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(mContext);
 		monthBackgroundColor = prefs.getInt("monthBackgroundColor",
 				MonthYearTitle.DEFAULT_BACKGROUND_COLOR);
 		monthTextColor = prefs.getInt("monthTextColor",
 				MonthYearTitle.DEFAULT_TEXT_COLOR);
 		weekBackgroundColor = prefs.getInt("weekBackgroundColor",
 				WeekTitle.DEFAULT_BACKGROUND_COLOR);
 		weekTextColor = prefs.getInt("weekTextColor",
 				WeekTitle.DEFAULT_TEXT_COLOR);
 		todayColor = prefs.getInt("todayColor", Cell.DEFAULT_TODAY_COLOR);
 		overtimeColor = prefs.getInt("overtimeColor", Cell.DEFAULT_TODAY_COLOR);
 		dailyNotesColor = prefs.getInt("dailyNotesColor",
 				Cell.DEFAULT_DAILY_NOTES_COLOR);
 		overtimeAbbreviation = mContext
 				.getString(R.string.overtime_abbreviation);
 		String firstDayOfWeekString = prefs.getString("firstDayOfWeek", "");
 		try {
 			firstDayOfWeek = Integer.parseInt(firstDayOfWeekString);
 		} catch (Exception e) {
 			firstDayOfWeek = mRightNow.getFirstDayOfWeek();
 		}
 		if (mHelper != null) {
 			int currentYear = mHelper.getYear();
 			int currentMonth = mHelper.getMonth();
 			mHelper = new MonthDisplayHelper(currentYear, currentMonth,
 					firstDayOfWeek);
 		} else {
 			mHelper = new MonthDisplayHelper(mRightNow.get(Calendar.YEAR),
 					mRightNow.get(Calendar.MONTH), firstDayOfWeek);
 		}
 
 	}
 
 	private synchronized void initCells() {
 		class _calendar {
 			public int day;
 			public boolean thisMonth;
 
 			public _calendar(int d, boolean b) {
 				day = d;
 				thisMonth = b;
 			}
 
 			public _calendar(int d) {
 				this(d, false);
 			}
 		}
 		;
 		_calendar[][] tmp = new _calendar[6][7];
 
 		for (int i = 0; i < tmp.length; i++) {
 			int n[] = mHelper.getDigitsForRow(i);
 			for (int d = 0; d < n.length; d++) {
 				if (mHelper.isWithinCurrentMonth(i, d))
 					tmp[i][d] = new _calendar(n[d], true);
 				else
 					tmp[i][d] = new _calendar(n[d]);
 
 			}
 		}
 
 		// Get data from database
 		Calendar startDate = Calendar.getInstance();
 		startDate.set(Calendar.YEAR, getYear());
 		startDate.set(Calendar.MONTH, mHelper.getMonth());
 		startDate.set(Calendar.DAY_OF_MONTH, 1);
 
 		Calendar endDate = Calendar.getInstance();
 		endDate.set(Calendar.YEAR, getYear());
 		endDate.set(Calendar.MONTH, mHelper.getMonth());
 		endDate.set(Calendar.DAY_OF_MONTH, mHelper.getNumberOfDaysInMonth());
 
 		Calendar today = Calendar.getInstance();
 		int thisDay = 0;
 		mToday = null;
 		if (mHelper.getYear() == today.get(Calendar.YEAR)
 				&& mHelper.getMonth() == today.get(Calendar.MONTH)) {
 			thisDay = today.get(Calendar.DAY_OF_MONTH);
 		}
 		int holiday = (8 - firstDayOfWeek) % 7;
 
 		// build cells
 		Rect Bound = new Rect(CELL_MARGIN_LEFT, CELL_MARGIN_TOP, CELL_WIDTH
 				+ CELL_MARGIN_LEFT, CELL_HEIGH + CELL_MARGIN_TOP);
 		for (int week = 0; week < mCells.length; week++) {
 			for (int day = 0; day < mCells[week].length; day++) {
 				if (tmp[week][day].thisMonth) {
 					if (day == holiday)
 						mCells[week][day] = new RedCell(tmp[week][day].day,
 								new Rect(Bound), CELL_TEXT_SIZE, SHIFT_SIZE,
 								PREV_SHIFT_SIZE);
 					else
 						mCells[week][day] = new Cell(tmp[week][day].day,
 								new Rect(Bound), CELL_TEXT_SIZE, SHIFT_SIZE,
 								PREV_SHIFT_SIZE);
 				} else
 					mCells[week][day] = new GrayCell(tmp[week][day].day,
 							new Rect(Bound), CELL_TEXT_SIZE, SHIFT_SIZE,
 							PREV_SHIFT_SIZE);
 
 				Bound.offset(CELL_WIDTH, 0); // move to next column
 
 				// get today
 				if (tmp[week][day].day == thisDay && tmp[week][day].thisMonth) {
 					mToday = mCells[week][day];
 					mToday.setToday(true, todayColor, TODAY_STROKE_WIDTH);
 				}
 			}
 			Bound.offset(0, CELL_HEIGH); // move to next row and first column
 			Bound.left = CELL_MARGIN_LEFT;
 			Bound.right = CELL_MARGIN_LEFT + CELL_WIDTH;
 		}
 		// Load shift data
 		mDatabase = mDatabase.open();
 		Cursor shiftCursor = mDatabase.fetchShifts(startDate, endDate);
 		shiftCursor.moveToFirst();
 		int dateColumnIndex = shiftCursor
 				.getColumnIndex(DataBaseHelper.KEY_DATE);
 		int shiftFinalIndex = shiftCursor
 				.getColumnIndex(DataBaseHelper.KEY_SHIFT_FINAL);
 		int previousShiftIndex = shiftCursor
 				.getColumnIndex(DataBaseHelper.KEY_SHIFT_PREVIOUS);
 		int colorIndex = shiftCursor.getColumnIndex(DataBaseHelper.KEY_COLOR);
 		while (!shiftCursor.isAfterLast()) {
 			String dateString = shiftCursor.getString(dateColumnIndex);
 			String shift = shiftCursor.getString(shiftFinalIndex);
 			String previousShift = shiftCursor.getString(previousShiftIndex);
 			int color = shiftCursor.getInt(colorIndex);
 			Date date;
 			try {
 				date = df.parse(dateString);
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(date);
 				int day = cal.get(Calendar.DAY_OF_MONTH);
 				int row = mHelper.getRowOf(day);
 				int column = mHelper.getColumnOf(day);
 				mCells[row][column].setWorkShift(shift, color);
 				if (previousShift != null) {
 					mCells[row][column].setPreviousWorkShift(previousShift);
 				}
 			} catch (ParseException e) {
 				Log.e("CalendarView", "ParseException");
 			}
 			shiftCursor.moveToNext();
 		}
 		shiftCursor.close();
 		// Overtime
 		Cursor overtimeCursor = mDatabase.fetchOvertime(startDate, endDate);
 		overtimeCursor.moveToFirst();
 		while (!overtimeCursor.isAfterLast()) {
 			String dateString = overtimeCursor.getString(dateColumnIndex);
 			Date date;
 			try {
 				date = df.parse(dateString);
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(date);
 				int day = cal.get(Calendar.DAY_OF_MONTH);
 				int row = mHelper.getRowOf(day);
 				int column = mHelper.getColumnOf(day);
 				mCells[row][column].setOvertime(overtimeAbbreviation,
 						overtimeColor);
 			} catch (ParseException e) {
 				Log.e("CalendarView", "ParseException");
 			}
 			overtimeCursor.moveToNext();
 		}
 		overtimeCursor.close();
 		// Daily notes
 		Cursor dailyNotesCursor = mDatabase.fetchDailyNotes(startDate, endDate);
 		dateColumnIndex = dailyNotesCursor
 				.getColumnIndex(DataBaseHelper.KEY_DATE);
 		dailyNotesCursor.moveToFirst();
 		while (!dailyNotesCursor.isAfterLast()) {
 			String dateString = dailyNotesCursor.getString(dateColumnIndex);
 			Date date;
 			try {
 				date = df.parse(dateString);
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(date);
 				int day = cal.get(Calendar.DAY_OF_MONTH);
 				int row = mHelper.getRowOf(day);
 				int column = mHelper.getColumnOf(day);
 				mCells[row][column].setDailyNote(true, dailyNotesColor);
 			} catch (ParseException e) {
 				Log.e("CalendarView", "ParseException");
 			}
 			dailyNotesCursor.moveToNext();
 		}
 		dailyNotesCursor.close();
 		// End
 		mDatabase.close();
 	}
 
 	@Override
 	public void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		Rect re = new Rect(getLeft(), getTop(), getRight(), getBottom());
 		CELL_MARGIN_LEFT = (right - left - re.width()) / 2;
 		CELL_WIDTH = getWidth() / 7;
 		CELL_HEIGH = (int) (CELL_WIDTH * 0.75);
 		initCells();
 		super.onLayout(changed, left, top, right, bottom);
 	}
 
 	public void setTimeInMillis(long milliseconds) {
 		mRightNow.setTimeInMillis(milliseconds);
 		initCells();
 		this.invalidate();
 	}
 
 	public int getYear() {
 		return mHelper.getYear();
 	}
 
 	public int getMonth() {
 		return mHelper.getMonth();
 	}
 
 	public void nextMonth() {
 		mHelper.nextMonth();
 		mStats = new Statistics(mContext, this);
 		initCells();
 		invalidate();
 		forceLayout();
 	}
 
 	public void previousMonth() {
 		mHelper.previousMonth();
 		mStats = new Statistics(mContext, this);
 		initCells();
 		invalidate();
 		forceLayout();
 	}
 
 	public boolean firstDay(int day) {
 		return day == 1;
 	}
 
 	public boolean lastDay(int day) {
 		return mHelper.getNumberOfDaysInMonth() == day;
 	}
 
 	public void goToday() {
 		Calendar cal = Calendar.getInstance();
 		mHelper = new MonthDisplayHelper(cal.get(Calendar.YEAR),
 				cal.get(Calendar.MONTH));
 		initCells();
 		invalidate();
 	}
 
 	public Calendar getDate() {
 		return mRightNow;
 	}
 
 	public void setOnCellTouchListener(OnCellTouchListener p) {
 		mOnCellTouchListener = p;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// Get size for cells
 		CELL_HEIGH = (int) (CELL_WIDTH * 0.75);
 		// draw background
 		super.onDraw(canvas);
 
 		// draw month year title
 		Rect mytBound = new Rect(0, 0, getWidth(), WEEK_TOP_MARGIN);
 		MonthYearTitle myt = new MonthYearTitle(mytBound, MONTH_YEAR_TEXT_SIZE,
 				monthBackgroundColor, monthTextColor);
 		myt.setTitle(mHelper.getMonth(), mHelper.getYear());
 		myt.draw(canvas);
 		// draw week title
 		Rect weekTitleBound = new Rect(0, WEEK_TOP_MARGIN, getWidth(),
 				CELL_MARGIN_TOP);
 		WeekTitle weekTitle = new WeekTitle(weekTitleBound,
 				WEEK_TITLE_TEXT_SIZE, weekBackgroundColor, weekTextColor,
 				firstDayOfWeek);
 		weekTitle.draw(canvas);
 
 		// draw cells
 		for (Cell[] week : mCells) {
 			for (Cell day : week) {
 				day.draw(canvas);
 			}
 		}
 
 		// re draw today
 		if (mToday != null) {
 			mToday.draw(canvas);
 		}
 		// draw statistics
 		Rect statsBound = new Rect(0, CELL_MARGIN_TOP + CELL_HEIGH * 6,
 				getWidth(), getHeight());
 		mStats.setBounds(statsBound);
 		mStats.draw(canvas);
 	}
 
 	private class GrayCell extends Cell {
 		public GrayCell(int dayOfMon, Rect rect, float s, float shiftSize,
 				float prevShiftSize) {
 			super(dayOfMon, rect, s, shiftSize, prevShiftSize);
 			mPaint.setColor(Color.LTGRAY);
 		}
 	}
 
 	private class RedCell extends Cell {
 		public RedCell(int dayOfMon, Rect rect, float s, float shiftSize,
 				float prevShiftSize) {
 			super(dayOfMon, rect, s, shiftSize, prevShiftSize);
 			mPaint.setColor(0xdddd0000);
 		}
 
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		int cell_width = MeasureSpec.getSize(widthMeasureSpec) / 7;
 		int cell_height = (int) (cell_width * 0.75);
 		int height = CELL_MARGIN_TOP + cell_height * 6
 				+ mStats.getPredictedHeight();
 		super.onMeasure(widthMeasureSpec,
 				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
 	}
 
 	public void setDate(Calendar date) {
 		mHelper = new MonthDisplayHelper(date.get(Calendar.YEAR),
				date.get(Calendar.MONTH), firstDayOfWeek);
 		mStats = new Statistics(mContext, this);
 	}
 
 	@Override
 	public void invalidate() {
 		mStats.getStatistics(this);
 		initCells();
 		super.invalidate();
 	}
 
 }
