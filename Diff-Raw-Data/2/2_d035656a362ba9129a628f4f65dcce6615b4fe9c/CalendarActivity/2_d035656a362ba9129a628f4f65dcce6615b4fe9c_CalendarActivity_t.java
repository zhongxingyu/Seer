 /**
  * @author Nick Huebner and Mark Redden
  * @version 1.0
  */
 
 package com.selagroup.schedu.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.selagroup.schedu.MyApplication;
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.managers.CourseManager;
 import com.selagroup.schedu.model.Course;
 import com.selagroup.schedu.model.Term;
 import com.selagroup.schedu.model.TimePlaceBlock;
 
 /**
  * The Class CalendarActivity.
  */
 public class CalendarActivity extends Activity {
 	private static final SimpleDateFormat sDayFormat = new SimpleDateFormat("EEEE, MMM d, yyyy");
 	private static final int sDaysInWeek = 7;
 	private static final int sDayViewBuffer_dp = 15;
 
 	// Widgets
 	private TextView calendar_tv_date;
 
 	private ScrollView calendar_sv_day;
 	private ScrollView calendar_sv_week;
 
 	private ToggleButton calendar_btn_day;
 	private ToggleButton calendar_btn_week;
 
 	private RelativeLayout calendar_day_layout;
 	private LinkedList<TextView> mCourseBlocks = new LinkedList<TextView>();
 
 	private LinearLayout calendar_ll_week;
 	private static LinearLayout.LayoutParams sWeekParams;
 	private ArrayList<TextView> mWeekDayBlocks = new ArrayList<TextView>(7);
 
 	// Managers
 	private CourseManager mCourseManager;
 
 	// Data
 	private List<Course> mCourses;
 	private Calendar mCurrentDay;
 	private Term mCurrentTerm;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calendar);
 
 		MyApplication myApp = ((MyApplication) getApplication());
 		mCourseManager = myApp.getCourseManager();
 
 		// Set up the correct day to view
 		mCurrentDay = Calendar.getInstance();
 		mCurrentTerm = myApp.getCurrentTerm();
 		if (mCurrentDay.after(mCurrentTerm.getEndDate())) {
 			mCurrentDay = mCurrentTerm.getStartDate();
 		}
 
 		// Get all courses for the current term and day
 		mCourses = mCourseManager.getAllForTerm(mCurrentTerm.getID());
 
 		initWidgets();
 		initListeners();
 		initDay();
 		initWeek();
 	}
 
 	/**
 	 * Initializes the widgets.
 	 */
 	private void initWidgets() {
 		calendar_tv_date = (TextView) findViewById(R.id.calendar_tv_date);
 		calendar_tv_date.setText(sDayFormat.format(mCurrentDay.getTime()));
 
 		calendar_btn_day = (ToggleButton) findViewById(R.id.calendar_btn_day);
 		calendar_btn_day.setChecked(true);
 		calendar_btn_week = (ToggleButton) findViewById(R.id.calendar_btn_week);
 
 		calendar_sv_day = (ScrollView) findViewById(R.id.calendar_sv_day);
 		calendar_sv_week = (ScrollView) findViewById(R.id.calendar_sv_week);
 
 		calendar_day_layout = (RelativeLayout) findViewById(R.id.calendar_day_courses);
 		calendar_ll_week = (LinearLayout) findViewById(R.id.calendar_ll_week);
 		
 		// Layout parameters
 		sWeekParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 		sWeekParams.setMargins(0, 2, 0, 2);
 	}
 
 	/**
 	 * Initializes the listeners.
 	 */
 	private void initListeners() {
 		OnClickListener buttonListener = new OnClickListener() {
 			public void onClick(View v) {
 				switch (v.getId()) {
 				case R.id.calendar_btn_day:
 					calendar_tv_date.setText(sDayFormat.format(mCurrentDay.getTime()));
 					calendar_btn_week.setChecked(false);
 					calendar_btn_day.setChecked(true);
 					calendar_sv_week.setVisibility(View.GONE);
 					calendar_sv_day.setVisibility(View.VISIBLE);
 					break;
 				case R.id.calendar_btn_week:
 					Calendar firstDay = (Calendar) mCurrentDay.clone();
 					firstDay.set(Calendar.DAY_OF_WEEK, mCurrentDay.getFirstDayOfWeek());
 					calendar_tv_date.setText("Week of " + sDayFormat.format(firstDay.getTime()));
 					calendar_btn_day.setChecked(false);
 					calendar_btn_week.setChecked(true);
 					calendar_sv_day.setVisibility(View.GONE);
 					calendar_sv_week.setVisibility(View.VISIBLE);
 					break;
 				}
 			}
 		};
 
 		calendar_btn_day.setOnClickListener(buttonListener);
 		calendar_btn_week.setOnClickListener(buttonListener);
 	}
 
 	private void initDay() {
 		// Add courses for current day
 		for (Course course : mCourses) {
 			List<TimePlaceBlock> blocks = course.getBlocksOnDay(mCurrentDay.get(Calendar.DAY_OF_WEEK) - 1);
 			for (TimePlaceBlock block : blocks) {
 				addCourseBlockToDay(course, block);
 			}
 		}
 	}
 
 	private void initWeek() {
 		
 		// Tree map to store sorted blocks and the associated courses
 		TreeMap<TimePlaceBlock, Course> courseBlocks = new TreeMap<TimePlaceBlock, Course>();
 		
 		// Set the day to the first day of the current week
 		TextView weekDayBlock;
 		Calendar day = (Calendar) mCurrentDay.clone();
 		day.set(Calendar.DAY_OF_WEEK, mCurrentDay.getFirstDayOfWeek());
 
 		// Add blocks for each day
 		for (int i = 0; i < sDaysInWeek; ++i) {
 			// Initialize the week's day block
 			weekDayBlock = new TextView(this);
 			weekDayBlock.setTextColor(Color.BLACK);
 			weekDayBlock.setBackgroundColor(Color.LTGRAY);
 			weekDayBlock.setText(sDayFormat.format(day.getTime()));
 			weekDayBlock.setLayoutParams(sWeekParams);
 
 			// Add the week's day block to the list and the linear layout
 			mWeekDayBlocks.add(weekDayBlock);
 			calendar_ll_week.addView(weekDayBlock);
 
 			for (Course course : mCourses) {
 				List<TimePlaceBlock> blocks = course.getBlocksOnDay(i);
 				for (TimePlaceBlock block : blocks) {
 					courseBlocks.put(block, course);
 				}
 			}
 			
 			for (Entry<TimePlaceBlock, Course> entry : courseBlocks.entrySet()) {
 				addCourseBlockToWeek(entry.getValue(), entry.getKey());
 			}
 			
 			courseBlocks.clear();
 
 			// Increment the day by 1
 			day.add(Calendar.DAY_OF_WEEK, 1);
 		}
 	}
 
 	private void addCourseBlockToDay(Course iCourse, TimePlaceBlock iBlock) {
 
 		// Initialize the block's colors, text, and listeners
 		TextView courseDayBlock = getCourseBlock(iCourse, iBlock);
 
 		// Sets the block height appropriately (1 minute = 1 dp)
 		final float scale = getResources().getDisplayMetrics().density;
 		int blockHeight_dp = (int) (iBlock.getMinutesElapsed() * scale + 0.5f);
 
 		// Sets the block distance from the top of the layout
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, blockHeight_dp);
		params.setMargins(0, (int) ((iBlock.getMinutesAfterMidnight() + sDayViewBuffer_dp) * scale + 0.5f), 0, 0);
 		courseDayBlock.setLayoutParams(params);
 
 		// Add the block to the list of blocks and the layout
 		mCourseBlocks.add(courseDayBlock);
 		calendar_day_layout.addView(courseDayBlock);
 	}
 
 	private void addCourseBlockToWeek(Course iCourse, TimePlaceBlock iBlock) {
 		// Initialize the block's colors, text, and listeners
 		TextView weekDayBlock = getCourseBlock(iCourse, iBlock);
 		
 		weekDayBlock.setLayoutParams(sWeekParams);
 		
 		// Add the week's day block to the list and the linear layout
 		mWeekDayBlocks.add(weekDayBlock);
 		calendar_ll_week.addView(weekDayBlock);
 	}
 
 	private TextView getCourseBlock(Course iCourse, TimePlaceBlock iBlock) {
 		TextView courseBlock = new TextView(this);
 		courseBlock.setTextColor(Color.BLACK);
 		courseBlock.setBackgroundColor(Color.GREEN);
 		courseBlock.setText(iCourse.getCourseName() + " (" + iCourse.getCourseCode() + ")\n" + iBlock.getLocation());
 		courseBlock.setClickable(true);
 		courseBlock.setOnClickListener(new CourseClickListener(iCourse.getID(), iBlock.getID()));
 		return courseBlock;
 	}
 
 	private class CourseClickListener implements OnClickListener {
 		private int mCourseID;
 		private int mBlockID;
 
 		public CourseClickListener(int iCourseID, int iBlockID) {
 			mCourseID = iCourseID;
 			mBlockID = iBlockID;
 		}
 
 		public void onClick(View view) {
 			Intent showCourse = new Intent(CalendarActivity.this, CourseActivity.class);
 			showCourse.putExtra("courseID", mCourseID);
 			showCourse.putExtra("blockID", mBlockID);
 			startActivity(showCourse);
 		}
 	}
 }
