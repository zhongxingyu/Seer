 /**
  * @author Nick Huebner and Mark Redden
  * @version 1.0
  */
 
 package com.selagroup.schedu.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.LinearInterpolator;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.selagroup.schedu.HorizontalPanelSwitcher;
 import com.selagroup.schedu.HorizontalPanelSwitcher.OnDragListener;
 import com.selagroup.schedu.HorizontalPanelSwitcher.OnPanelSwitchListener;
 import com.selagroup.schedu.ObservableScrollView;
 import com.selagroup.schedu.ObservableScrollView.ScrollViewListener;
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.ScheduApplication;
 import com.selagroup.schedu.managers.CourseManager;
 import com.selagroup.schedu.model.Course;
 import com.selagroup.schedu.model.Term;
 import com.selagroup.schedu.model.TimePlaceBlock;
 
 /**
  * The Class CalendarActivity.
  */
 public class CalendarActivity extends Activity {
 	private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEEE, MMM d, yyyy");
 	private static final SimpleDateFormat WEEK_FORMAT = new SimpleDateFormat("MMM d");
 	private static final int DAYS_IN_WEEK = 7;
 	private static final int DAY_VIEW_BUFFER = 15;
 
 	private static final LinearLayout.LayoutParams WEEK_LAYOUT_PARAMS;
 	private static final RelativeLayout.LayoutParams DAY_PARENT_LAYOUT_PARAMS;
 
 	private static final int ANIMATION_TIME = 200;
 	private static final AlphaAnimation FADE_IN_ANIMATION;
 	private static final AlphaAnimation FADE_OUT_ANIMATION;
 
 	// Initialize constants
 	static {
 		WEEK_LAYOUT_PARAMS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 		WEEK_LAYOUT_PARAMS.setMargins(0, 2, 0, 2);
 
 		DAY_PARENT_LAYOUT_PARAMS = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 
 		FADE_IN_ANIMATION = new AlphaAnimation(0, 1);
 		FADE_IN_ANIMATION.setDuration(ANIMATION_TIME);
 		FADE_IN_ANIMATION.setInterpolator(new LinearInterpolator());
 
 		FADE_OUT_ANIMATION = new AlphaAnimation(1, 0);
 		FADE_OUT_ANIMATION.setDuration(ANIMATION_TIME);
 		FADE_OUT_ANIMATION.setInterpolator(new LinearInterpolator());
 	}
 
 	// Widgets
 	private ToggleButton calendar_btn_day;
 	private ToggleButton calendar_btn_week;
 	private TextView calendar_tv_date;
 	private ImageButton calendar_btn_next;
 	private ImageButton calendar_btn_prev;
 
 	// Day widgets
 	private HorizontalPanelSwitcher calendar_switcher_day;
 
 	private View mThisDayView;
 	private RelativeLayout mThisDayParentView;
 	private RelativeLayout mThisDayCourses;
 	private ObservableScrollView mThisDayScroll;
 	private LinkedList<TextView> mThisDayCourseBlocks = new LinkedList<TextView>();
 
 	private View mNextDayView;
 	private RelativeLayout mNextDayParentView;
 	private RelativeLayout mNextDayCourses;
 	private ObservableScrollView mNextDayScroll;
 	private LinkedList<TextView> mNextDayCourseBlocks = new LinkedList<TextView>();
 
 	private View mPrevDayView;
 	private RelativeLayout mPrevDayParentView;
 	private RelativeLayout mPrevDayCourses;
 	private ObservableScrollView mPrevDayScroll;
 	private LinkedList<TextView> mPrevDayCourseBlocks = new LinkedList<TextView>();
 
 	// Week widgets
 	private HorizontalPanelSwitcher calendar_switcher_week;
 
 	private View mThisWeekView;
 	private RelativeLayout mThisWeekParentView;
 	private LinearLayout mThisWeekLayout;
 	private ObservableScrollView mThisWeekScroll;
 	private LinkedList<TextView> mThisWeekCourseBlocks = new LinkedList<TextView>();
 
 	private View mNextWeekView;
 	private RelativeLayout mNextWeekParentView;
 	private LinearLayout mNextWeekLayout;
 	private ObservableScrollView mNextWeekScroll;
 	private LinkedList<TextView> mNextWeekCourseBlocks = new LinkedList<TextView>();
 
 	private View mPrevWeekView;
 	private RelativeLayout mPrevWeekParentView;
 	private LinearLayout mPrevWeekLayout;
 	private ObservableScrollView mPrevWeekScroll;
 	private LinkedList<TextView> mPrevWeekCourseBlocks = new LinkedList<TextView>();
 
 	// Managers
 	private CourseManager mCourseManager;
 
 	// Data
 	private List<Course> mCourses;
 	private Calendar mThisDay;
 	private Calendar mNextDay;
 	private Calendar mPrevDay;
 	private Term mCurrentTerm;
 	private float mDensity;
 	private boolean mScrollLock = false;
 	private boolean mDayMode = true;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calendar);
 
 		ScheduApplication myApp = ((ScheduApplication) getApplication());
 		mCourseManager = myApp.getCourseManager();
 
 		// Set up the correct day to view
 		mThisDay = Calendar.getInstance();
 		mCurrentTerm = myApp.getCurrentTerm();
 		if (mThisDay.after(mCurrentTerm.getEndDate())) {
 			mThisDay = mCurrentTerm.getEndDate();
 		} else if (mThisDay.before(mCurrentTerm.getStartDate())) {
 			mThisDay = mCurrentTerm.getStartDate();
 		}
 		mNextDay = (Calendar) mThisDay.clone();
 		mNextDay.roll(Calendar.DAY_OF_MONTH, true);
 		mPrevDay = (Calendar) mThisDay.clone();
 		mPrevDay.roll(Calendar.DAY_OF_MONTH, false);
 
 		mDensity = getResources().getDisplayMetrics().density;
 
 		// Get all courses for the current term
 		mCourses = mCourseManager.getAllForTerm(mCurrentTerm.getID());
 
 		initWidgets();
 		initListeners();
 		initDayView();
 	}
 
 	/**
 	 * Initializes the widgets.
 	 */
 	private void initWidgets() {
 		initDayWidgets();
 		initWeekWidgets();
 	}
 
 	/**
 	 * Initializes widgets for the day view
 	 */
 	private void initDayWidgets() {
 		calendar_btn_day = (ToggleButton) findViewById(R.id.calendar_btn_day);
 		calendar_btn_day.setChecked(true);
 		calendar_btn_week = (ToggleButton) findViewById(R.id.calendar_btn_week);
 
 		calendar_btn_next = (ImageButton) findViewById(R.id.calendar_btn_next);
 		calendar_btn_prev = (ImageButton) findViewById(R.id.calendar_btn_prev);
 		calendar_tv_date = (TextView) findViewById(R.id.calendar_tv_date);
 
 		// Construct the day calendar view
 		calendar_switcher_day = (HorizontalPanelSwitcher) findViewById(R.id.calendar_switcher_day);
 
 		mThisDayParentView = new RelativeLayout(this);
 		mThisDayParentView.setLayoutParams(DAY_PARENT_LAYOUT_PARAMS);
 
 		mPrevDayParentView = new RelativeLayout(this);
 		mPrevDayParentView.setLayoutParams(DAY_PARENT_LAYOUT_PARAMS);
 
 		mNextDayParentView = new RelativeLayout(this);
 		mNextDayParentView.setLayoutParams(DAY_PARENT_LAYOUT_PARAMS);
 
 		calendar_switcher_day.setCenterLayout(mThisDayParentView);
 		calendar_switcher_day.setLeftLayout(mPrevDayParentView);
 		calendar_switcher_day.setRightLayout(mNextDayParentView);
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 
 		// Initialize this day
 		mThisDayView = inflater.inflate(R.layout.layout_day, mThisDayParentView);
 		mThisDayCourses = (RelativeLayout) mThisDayView.findViewById(R.id.calendar_day_courses);
 		mThisDayScroll = (ObservableScrollView) mThisDayView.findViewById(R.id.calendar_sv_day);
 
 		// Initialize next day
 		mNextDayView = inflater.inflate(R.layout.layout_day, mNextDayParentView);
 		mNextDayCourses = (RelativeLayout) mNextDayView.findViewById(R.id.calendar_day_courses);
 		mNextDayScroll = (ObservableScrollView) mNextDayView.findViewById(R.id.calendar_sv_day);
 
 		// Initialize previous day
 		mPrevDayView = inflater.inflate(R.layout.layout_day, mPrevDayParentView);
 		mPrevDayCourses = (RelativeLayout) mPrevDayView.findViewById(R.id.calendar_day_courses);
 		mPrevDayScroll = (ObservableScrollView) mPrevDayView.findViewById(R.id.calendar_sv_day);
 
 		// Scroll to the current time in the day
 		mThisDayScroll.post(new Runnable() {
 			public void run() {
 				mThisDayScroll.scrollTo(0, (int) (mDensity * (60 * mThisDay.get(Calendar.HOUR_OF_DAY) + mThisDay.get(Calendar.MINUTE))));
 			}
 		});
 	}
 
 	/**
 	 * Initializes widgets for the week view
 	 */
 	private void initWeekWidgets() {
 		calendar_btn_week = (ToggleButton) findViewById(R.id.calendar_btn_week);
 		calendar_btn_week.setChecked(false);
 		calendar_btn_week = (ToggleButton) findViewById(R.id.calendar_btn_week);
 
 		calendar_btn_next = (ImageButton) findViewById(R.id.calendar_btn_next);
 		calendar_btn_prev = (ImageButton) findViewById(R.id.calendar_btn_prev);
 		calendar_tv_date = (TextView) findViewById(R.id.calendar_tv_date);
 
 		// Construct the week calendar view
 		calendar_switcher_week = (HorizontalPanelSwitcher) findViewById(R.id.calendar_switcher_week);
 
 		mThisWeekParentView = new RelativeLayout(this);
 		mThisWeekParentView.setLayoutParams(DAY_PARENT_LAYOUT_PARAMS);
 
 		mPrevWeekParentView = new RelativeLayout(this);
 		mPrevWeekParentView.setLayoutParams(DAY_PARENT_LAYOUT_PARAMS);
 
 		mNextWeekParentView = new RelativeLayout(this);
 		mNextWeekParentView.setLayoutParams(DAY_PARENT_LAYOUT_PARAMS);
 
 		calendar_switcher_week.setCenterLayout(mThisWeekParentView);
 		calendar_switcher_week.setLeftLayout(mPrevWeekParentView);
 		calendar_switcher_week.setRightLayout(mNextWeekParentView);
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 
 		// Initialize this week
 		mThisWeekView = inflater.inflate(R.layout.layout_week, mThisWeekParentView);
 		mThisWeekLayout = (LinearLayout) mThisWeekView.findViewById(R.id.calendar_week_courses);
 		mThisWeekScroll = (ObservableScrollView) mThisWeekView.findViewById(R.id.calendar_sv_week);
 
 		// Initialize next week
 		mNextWeekView = inflater.inflate(R.layout.layout_week, mNextWeekParentView);
 		mNextWeekLayout = (LinearLayout) mNextWeekView.findViewById(R.id.calendar_week_courses);
 		mNextWeekScroll = (ObservableScrollView) mNextWeekView.findViewById(R.id.calendar_sv_week);
 
 		// Initialize previous week
 		mPrevWeekView = inflater.inflate(R.layout.layout_week, mPrevWeekParentView);
 		mPrevWeekLayout = (LinearLayout) mPrevWeekView.findViewById(R.id.calendar_week_courses);
 		mPrevWeekScroll = (ObservableScrollView) mPrevWeekView.findViewById(R.id.calendar_sv_week);
 	}
 
 	/**
 	 * Initializes the listeners.
 	 */
 	private void initListeners() {
 		// Switch between day and week views
 		OnClickListener buttonListener = new OnClickListener() {
 			public void onClick(View v) {
 				switch (v.getId()) {
 				case R.id.calendar_btn_day:
 					mDayMode = true;
 					calendar_btn_day.setChecked(true);
 					calendar_btn_week.setChecked(false);
 					calendar_switcher_day.setVisibility(View.VISIBLE);
					calendar_switcher_week.setVisibility(View.GONE);
					initDayView();
 					break;
 				case R.id.calendar_btn_week:
 					mDayMode = false;
 					calendar_btn_day.setChecked(false);
 					calendar_btn_week.setChecked(true);
 					calendar_switcher_day.setVisibility(View.GONE);
					calendar_switcher_week.setVisibility(View.VISIBLE);
					initWeekView();
 					break;
 				}
 			}
 		};
 		calendar_btn_day.setOnClickListener(buttonListener);
 		calendar_btn_week.setOnClickListener(buttonListener);
 
 		// Move to next day when hitting the next button
 		calendar_btn_next.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (mDayMode) {
 					calendar_switcher_day.switchPanels(true);
 				} else {
 					calendar_switcher_week.switchPanels(true);
 				}
 			}
 		});
 
 		// Move to previous day when hitting the previous button
 		calendar_btn_prev.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (mDayMode) {
 					calendar_switcher_day.switchPanels(false);
 				}
 				else {
 					calendar_switcher_week.switchPanels(false);
 				}
 			}
 		});
 
 		initDayListeners();
 		initWeekListeners();
 	}
 
 	/**
 	 * Initializes listeners associated with widgets in the day view
 	 */
 	private void initDayListeners() {
 		// React to switching the day in the panel switcher
 		calendar_switcher_day.setOnPanelSwitchListener(new OnPanelSwitchListener() {
 			public void switchRight() {
 				mThisDay.add(Calendar.DAY_OF_MONTH, 1);
 				mNextDay.add(Calendar.DAY_OF_MONTH, 1);
 				mPrevDay.add(Calendar.DAY_OF_MONTH, 1);
 
 				initDayView();
 			}
 
 			public void switchLeft() {
 				mThisDay.add(Calendar.DAY_OF_MONTH, -1);
 				mNextDay.add(Calendar.DAY_OF_MONTH, -1);
 				mPrevDay.add(Calendar.DAY_OF_MONTH, -1);
 
 				initDayView();
 			}
 		});
 
 		// Lock the scroll view and disappear the buttons while changing days
 		calendar_switcher_day.setOnDragListener(new OnDragListener() {
 			public void dragStart() {
 				setScrollLock(true);
 			}
 
 			public void dragEnd(boolean isSwitching) {
 				setScrollLock(false);
 				if (isSwitching) {
 					fadeInOut(calendar_tv_date);
 				}
 			}
 		});
 
 		// Intercept touch events while "mScrollLock" is true
 		mThisDayScroll.setOnTouchListener(new ScrollView.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return mScrollLock;
 			}
 		});
 
 		// Synchronize the previous day's and the next day's scroll views with this day's scroll view
 		mThisDayScroll.setOnScrollListener(new ScrollViewListener() {
 			public void onScrollChanged(ObservableScrollView view, int x, int y, int oldx, int oldy) {
 				mNextDayScroll.scrollTo(0, y);
 				mPrevDayScroll.scrollTo(0, y);
 			}
 		});
 	}
 
 	/**
 	 * Initializes listeners associated with widgets in the week view
 	 */
 	private void initWeekListeners() {
 		// React to switching the week in the panel switcher
 		calendar_switcher_week.setOnPanelSwitchListener(new OnPanelSwitchListener() {
 			public void switchRight() {
 				mThisDay.add(Calendar.WEEK_OF_MONTH, 1);
 				mNextDay.add(Calendar.WEEK_OF_MONTH, 1);
 				mPrevDay.add(Calendar.WEEK_OF_MONTH, 1);
 
 				initWeekView();
 			}
 
 			public void switchLeft() {
 				mThisDay.add(Calendar.WEEK_OF_MONTH, -1);
 				mNextDay.add(Calendar.WEEK_OF_MONTH, -1);
 				mPrevDay.add(Calendar.WEEK_OF_MONTH, -1);
 
 				initWeekView();
 			}
 		});
 
 		// Lock the scroll view and disappear the buttons while changing weeks
 		calendar_switcher_week.setOnDragListener(new OnDragListener() {
 			public void dragStart() {
 				setScrollLock(true);
 			}
 
 			public void dragEnd(boolean isSwitching) {
 				setScrollLock(false);
 				if (isSwitching) {
 					fadeInOut(calendar_tv_date);
 				}
 			}
 		});
 
 		// Intercept touch events while "mScrollLock" is true
 		mThisWeekScroll.setOnTouchListener(new ScrollView.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return mScrollLock;
 			}
 		});
 
 		// Synchronize the previous week's and the next week's scroll views with this week's scroll view
 		mThisWeekScroll.setOnScrollListener(new ScrollViewListener() {
 			public void onScrollChanged(ObservableScrollView view, int x, int y, int oldx, int oldy) {
 				mNextWeekScroll.scrollTo(0, y);
 				mPrevWeekScroll.scrollTo(0, y);
 			}
 		});
 	}
 
 	private void initDayView() {
 		// Remove any existing day views
 		mThisDayCourses.removeAllViews();
 		mNextDayCourses.removeAllViews();
 		mPrevDayCourses.removeAllViews();
 
 		calendar_tv_date.setText(DAY_FORMAT.format(mThisDay.getTime()));
 
 		mPrevDayScroll.post(new Runnable() {
 			public void run() {
 				mPrevDayScroll.scrollTo(0, (int) mThisDayScroll.getScrollY());
 			}
 		});
 
 		mNextDayScroll.post(new Runnable() {
 			public void run() {
 				mNextDayScroll.scrollTo(0, (int) mThisDayScroll.getScrollY());
 			}
 		});
 
 		// Add courses for current day
 		for (Course course : mCourses) {
 			List<TimePlaceBlock> blocks = course.getBlocksOnDay(mThisDay.get(Calendar.DAY_OF_WEEK) - 1);
 			for (TimePlaceBlock block : blocks) {
 				addCourseBlockToDay(course, block, mThisDay, mThisDayCourseBlocks, mThisDayCourses);
 			}
 		}
 
 		// Add courses for next day
 		for (Course course : mCourses) {
 			List<TimePlaceBlock> blocks = course.getBlocksOnDay(mNextDay.get(Calendar.DAY_OF_WEEK) - 1);
 			for (TimePlaceBlock block : blocks) {
 				addCourseBlockToDay(course, block, mNextDay, mNextDayCourseBlocks, mNextDayCourses);
 			}
 		}
 
 		// Add courses for previous day
 		for (Course course : mCourses) {
 			List<TimePlaceBlock> blocks = course.getBlocksOnDay(mPrevDay.get(Calendar.DAY_OF_WEEK) - 1);
 			for (TimePlaceBlock block : blocks) {
 				addCourseBlockToDay(course, block, mPrevDay, mPrevDayCourseBlocks, mPrevDayCourses);
 			}
 		}
 	}
 
 	private void initWeekView() {
 		// Remove any existing week views
 		mThisWeekLayout.removeAllViews();
 		mNextWeekLayout.removeAllViews();
 		mPrevWeekLayout.removeAllViews();
 
 		Calendar firstDayOfWeek = (Calendar) mThisDay.clone();
 		firstDayOfWeek.set(Calendar.DAY_OF_WEEK, mThisDay.getFirstDayOfWeek());
 		calendar_tv_date.setText("Week of " + WEEK_FORMAT.format(firstDayOfWeek.getTime()));
 		
 		// Initialize the current week
 		initSingleWeek((Calendar) firstDayOfWeek.clone(), mThisWeekCourseBlocks, mThisWeekLayout);
 		
 		// Initialize the next week
 		firstDayOfWeek.add(Calendar.WEEK_OF_YEAR, 1);
 		initSingleWeek((Calendar) firstDayOfWeek.clone(), mNextWeekCourseBlocks, mNextWeekLayout);
 		
 		// Initialize the previous week
 		firstDayOfWeek.add(Calendar.WEEK_OF_YEAR, 1);
 		initSingleWeek(firstDayOfWeek, mPrevWeekCourseBlocks, mPrevWeekLayout);
 	}
 
 	private void initSingleWeek(Calendar iFirstDay, LinkedList<TextView> iBlocks, LinearLayout iWeekLayout) {
 		// Tree map to store sorted blocks and the associated courses
 		TreeMap<TimePlaceBlock, Course> courseBlocks = new TreeMap<TimePlaceBlock, Course>();
 
 		// Set the day to the first day of the current week
 		TextView weekDayBlock;
 
 		// Add blocks for each day
 		for (int i = 0; i < DAYS_IN_WEEK; ++i) {
 			// Initialize the day header block
 			weekDayBlock = new TextView(this);
 			weekDayBlock.setTextColor(Color.BLACK);
 			weekDayBlock.setBackgroundColor(Color.LTGRAY);
 			weekDayBlock.setText(DAY_FORMAT.format(iFirstDay.getTime()));
 			weekDayBlock.setBackgroundResource(R.drawable.calendar_block);
 			weekDayBlock.setLayoutParams(WEEK_LAYOUT_PARAMS);
 
 			// Add the week's day block to the list and the linear layout
 			iBlocks.add(weekDayBlock);
 			iWeekLayout.addView(weekDayBlock);
 
 			// Add time blocks to tree map for sorting by time of day
 			for (Course course : mCourses) {
 				List<TimePlaceBlock> blocks = course.getBlocksOnDay(i);
 				for (TimePlaceBlock block : blocks) {
 					courseBlocks.put(block, course);
 				}
 			}
 
 			// Add time blocks to the view in chronological order
 			for (Entry<TimePlaceBlock, Course> entry : courseBlocks.entrySet()) {
 				addCourseBlockToWeek(entry.getValue(), entry.getKey(), iFirstDay, iBlocks, iWeekLayout);
 			}
 
 			courseBlocks.clear();
 
 			// Increment the day by 1
 			iFirstDay.add(Calendar.DAY_OF_WEEK, 1);
 		}
 	}
 
 	private void addCourseBlockToDay(Course iCourse, TimePlaceBlock iBlock, Calendar iDay, LinkedList<TextView> iBlockList, RelativeLayout iLayout) {
 
 		// Initialize the block's colors, text, and listeners
 		TextView courseDayBlock = getCourseBlock(iCourse, iBlock);
 
 		// Sets the block height appropriately (1 minute = 1 dp)
 		int blockHeight_dp = (int) (iBlock.getMinutesElapsed() * mDensity + 0.5f);
 
 		// Sets the block distance from the top of the layout
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, blockHeight_dp);
 		params.setMargins(0, (int) ((iBlock.getMinutesAfterMidnight() + DAY_VIEW_BUFFER) * mDensity + 0.5f), 0, 0);
 		courseDayBlock.setLayoutParams(params);
 		courseDayBlock.setTag(iDay.clone());
 
 		// Add the block to the list of blocks and the layout
 		iBlockList.add(courseDayBlock);
 		iLayout.addView(courseDayBlock);
 	}
 
 	private void addCourseBlockToWeek(Course iCourse, TimePlaceBlock iBlock, Calendar iDay, LinkedList<TextView> iCourseBlocks, LinearLayout iWeekLayout) {
 		// Initialize the block's colors, text, and listeners
 		TextView weekDayBlock = getCourseBlock(iCourse, iBlock);
 		weekDayBlock.setTag(iDay.clone());
 
 		weekDayBlock.setLayoutParams(WEEK_LAYOUT_PARAMS);
 
 		// Add the week's day block to the list and the linear layout
 		iCourseBlocks.add(weekDayBlock);
 		iWeekLayout.addView(weekDayBlock);
 	}
 
 	/**
 	 * Gets a widget representing a course's block on the calendar
 	 * @param iCourse The course for this course block
 	 * @param iBlock The data for this block
 	 * @return A text view widget for this course ready to be placed in a day or week view
 	 */
 	private TextView getCourseBlock(Course iCourse, TimePlaceBlock iBlock) {
 		TextView courseBlock = new TextView(this);
 		courseBlock.setTextColor(Color.BLACK);
 		courseBlock.setBackgroundColor(Color.GREEN);
 		courseBlock.setText(iCourse.toString() + "\n" + iBlock.getLocation());
 		courseBlock.setBackgroundResource(R.drawable.course_block_low);
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
 			showCourse.putExtra("day", (Calendar) view.getTag());
 			startActivity(showCourse);
 		}
 	}
 
 	/**
 	 * Sets the scroll lock, which prevents the scroll views from scrolling
 	 * @param iScrollLock
 	 */
 	public void setScrollLock(boolean iScrollLock) {
 		mScrollLock = iScrollLock;
 	}
 
 	/**
 	 * @return True if scrolling is locked
 	 */
 	public boolean getScrollLock() {
 		return mScrollLock;
 	}
 	
 	private void fadeInOut(final View iView) {
 		iView.setEnabled(false);
 		iView.postDelayed(new Runnable() {
 			public void run() {
 				iView.setVisibility(View.INVISIBLE);
 				iView.setEnabled(true);
 				iView.startAnimation(FADE_IN_ANIMATION);
 				iView.setVisibility(View.VISIBLE);
 			}
 		}, ANIMATION_TIME);
 		iView.startAnimation(FADE_OUT_ANIMATION);
 	}
 }
