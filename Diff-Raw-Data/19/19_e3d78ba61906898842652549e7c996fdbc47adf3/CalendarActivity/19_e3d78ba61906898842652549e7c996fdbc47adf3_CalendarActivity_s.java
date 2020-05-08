 /**
  * @author Nick Huebner and Mark Redden
  * @version 1.0
  */
 
 package com.selagroup.schedu.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.managers.CourseManager;
 import com.selagroup.schedu.model.Course;
 import com.selagroup.schedu.model.Term;
 import com.selagroup.schedu.model.TimePlaceBlock;
 import com.selagroup.schedu.MyApplication;
 
 /**
  * The Class CalendarActivity.
  */
 public class CalendarActivity extends Activity {
 	private static final SimpleDateFormat sDayFormat = new SimpleDateFormat("EEEE MMM d, yyyy");
 
 	// Widgets
 	private TextView calendar_tv_date;
 
 	private ScrollView calendar_sv_day;
 	private ScrollView calendar_sv_week;
 
 	private ToggleButton calendar_btn_day;
 	private ToggleButton calendar_btn_week;
 
 	private RelativeLayout calendar_day_courses;
 	
 	private LinkedList<TextView> mCourseBlocks = new LinkedList<TextView>();
 
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
 		
 		// Add courses for current day
 		for (Course course : mCourses) {
			List<TimePlaceBlock> blocks = course.getBlocksOnDay(mCurrentDay.get(Calendar.DAY_OF_WEEK));
 			for (TimePlaceBlock block : blocks) {
 				addCourseBlockToDay(course, block);
 			}
 		}
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
 
 		calendar_day_courses = (RelativeLayout) findViewById(R.id.calendar_day_courses);
 
 		OnClickListener buttonListener = new OnClickListener() {
 			public void onClick(View v) {
 				switch (v.getId()) {
 				case R.id.calendar_btn_day:
 					calendar_btn_week.setChecked(false);
 					calendar_btn_day.setChecked(true);
 					calendar_sv_week.setVisibility(View.GONE);
 					calendar_sv_day.setVisibility(View.VISIBLE);
 					break;
 				case R.id.calendar_btn_week:
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
 
 	private void addCourseBlockToDay(Course iCourse, TimePlaceBlock iBlock) {
 		TextView courseBlock = new TextView(this);
 		courseBlock.setTextColor(Color.BLACK);
 		courseBlock.setBackgroundColor(Color.GREEN);
 		courseBlock.setText(iCourse.getCourseCode());
 		courseBlock.setClickable(true);
 
		courseBlock.setOnClickListener(new CourseClickListener(iCourse));
 		
 		final float scale = getResources().getDisplayMetrics().density;
 		int blockHeight_dp = (int) (iBlock.getMinutesElapsed() * scale + 0.5f);
 //		int blockHeight_dp = (int) (60 * scale + 0.5f);
 
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, blockHeight_dp);
 		params.setMargins(0, (int) (iBlock.getMinutesAfterMidnight() * scale + 0.5f), 0, 0);
 //		params.setMargins(0, (int) (300 * scale + 0.5f), 0, 0);
 		courseBlock.setLayoutParams(params);
 
 		mCourseBlocks.add(courseBlock);
 		calendar_day_courses.addView(courseBlock);
 	}
 	
 	private class CourseClickListener implements OnClickListener { 
		private Course mCourse;
		public CourseClickListener(Course iCourse) {
			mCourse = iCourse;
 		}
 
 		public void onClick(View view) {
 			Intent showCourse = new Intent(CalendarActivity.this, CourseActivity.class);
			showCourse.putExtra("courseID", mCourse.getID());
			showCourse.putExtra("blockID", -1);
 			startActivity(showCourse);
 		}
 	}
 }
