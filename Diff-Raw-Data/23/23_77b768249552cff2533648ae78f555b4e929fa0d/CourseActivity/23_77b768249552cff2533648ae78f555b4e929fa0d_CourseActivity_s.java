 /**
  * @author Nick Huebner and Mark Redden
  * @version 1.0
  */
 
 package com.selagroup.schedu.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.ScheduApplication;
 import com.selagroup.schedu.Utility;
 import com.selagroup.schedu.model.Course;
 import com.selagroup.schedu.model.Instructor;
 import com.selagroup.schedu.model.Location;
 import com.selagroup.schedu.model.TimePlaceBlock;
 
 /**
  * The Class CourseActivity.
  */
 public class CourseActivity extends Activity {
 	
 	public static final int INSTRUCTOR_EDIT_CODE = 1;
 	
 	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy");
 	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a");
 	
 	private TextView course_course_code;
 	private TextView course_course_name;
 	private TextView course_countDown_label;
 	private TextView course_tv_countDown;
 	private TextView course_tv_date;
 	private TextView course_tv_time;
//	private TextView course_building;
//	private TextView course_room;
 	private TextView course_location;
 	private TextView course_instructor;
 	private TextView course_instructor_location;
 	private ScrollView course_sv_instructor_hours;
 	
 	private Button course_btn_edit_instructor;
 	private Button course_btn_notes;
 	private Button course_btn_assignments;
 	private Button course_btn_exams;
 	
 	private Calendar day;
 	private int duration;
 	private int courseID;
 	private Course thisCourse;
 	private TimePlaceBlock thisBlock;
 	private Location thisLocation;
 	private Instructor thisInstructor;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_course);
 	
 		Intent intent = getIntent();
 		courseID = intent.getIntExtra("courseID", -1);
 		int blockID = intent.getIntExtra("blockID", -1);
 		day = (Calendar) intent.getSerializableExtra("day");
 		
 		thisCourse = ((ScheduApplication) getApplication()).getCourseManager().get(courseID);
 		thisBlock = thisCourse.getScheduleBlock(blockID);
 		duration = (int) (thisBlock.getEndTime().getTimeInMillis() - thisBlock.getStartTime().getTimeInMillis());
 		updateDayWithBlockTime();
 		thisLocation = thisBlock.getLocation();
 		thisInstructor = thisCourse.getInstructor();
 		
 		initWidgets();
 		setValues();
 		initListeners();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == INSTRUCTOR_EDIT_CODE && resultCode == RESULT_OK) {
			//thisInstructor = (Instructor) data.getSerializableExtra("instructor");
 			thisCourse = ((ScheduApplication) getApplication()).getCourseManager().get(courseID);
 			thisInstructor = thisCourse.getInstructor();
 			setValues();
 		}
 	}
 	
 	private void updateDayWithBlockTime() {
 		int year = day.get(Calendar.YEAR);
 		int month = day.get(Calendar.MONTH);
 		int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
 		
 		Calendar start = thisBlock.getStartTime();
 		int hour = start.get(Calendar.HOUR_OF_DAY);
 		int minute = start.get(Calendar.MINUTE);
 		
 		day.set(year, month, dayOfMonth, hour, minute, 0);
 	}
 	
 	private void initWidgets() {
 		// Initialize widget handles
 		course_course_code = (TextView) findViewById(R.id.course_course_code);
 		course_course_name = (TextView) findViewById(R.id.course_course_name);
 		course_tv_date = (TextView) findViewById(R.id.course_tv_date);
 		course_tv_time = (TextView) 		findViewById(R.id.course_tv_time);
 		course_countDown_label = (TextView) findViewById(R.id.course_countDown_label);
 		course_tv_countDown = (TextView) findViewById(R.id.course_tv_countDown);
//		course_building = (TextView) findViewById(R.id.course_building);
//		course_room = (TextView) findViewById(R.id.course_room);
 		course_location = (TextView) findViewById(R.id.course_location);
 		course_instructor = (TextView) findViewById(R.id.course_instructor);
 		course_instructor_location = (TextView) findViewById(R.id.course_instructor_location);
 		course_sv_instructor_hours = (ScrollView) findViewById(R.id.course_sv_instructor_hours);
 		
 		course_btn_edit_instructor = (Button) findViewById(R.id.course_btn_edit_instructor);
 		course_btn_notes = (Button) findViewById(R.id.course_btn_notes);
 		course_btn_assignments = (Button) findViewById(R.id.course_btn_reminders);
 		course_btn_exams = (Button) findViewById(R.id.course_btn_assignments_exams);
 
 	}
 
 	private void setCourseTimer() {
 		long now = System.currentTimeMillis();
 		long difference = day.getTimeInMillis() - now;
 		if (difference <= 0) { // it's now after the start time
 			course_countDown_label.setText(R.string.course_course_ends);
 			// calculate new difference between now and end time
 			difference = day.getTimeInMillis() + duration - now;
 			if (difference <= 0) { // it's now after the end time
 				course_tv_countDown.setText("00 : 00 : 00");
 			}
 		}
 		else { // it's before the start time, set the label
 			course_countDown_label.setText(R.string.course_course_begins);
 		}
 		if (difference > 0) { // before start time, or before end time, do the same thing
 			new CountDownTimer(difference, 1000) {
 
 				public void onTick(long millisUntilFinished) {
 					int days = (int) millisUntilFinished / Utility.MILLIS_PER_DAY;
 					int hours = (int) millisUntilFinished / Utility.MILLIS_PER_HOUR % Utility.HOURS_PER_DAY;
 					int minutes = (int) millisUntilFinished / Utility.MILLIS_PER_MINUTE % Utility.MINUTES_PER_HOUR;
 					int seconds = (int) millisUntilFinished / Utility.MILLIS_PER_SECOND % Utility.SECONDS_PER_MINUTE;
 					String dayText = days > 1 ? days + " days, " : days == 1 ? days + " day, " : "";
 					course_tv_countDown.setText(dayText + String.format("%02d : %02d : %02d", hours, minutes, seconds));
 				}
 
 				public void onFinish() {
 					setCourseTimer();
 				}
 			}.start();
 		}
 	}
 	
 	private void setValues() {
 		// Set values
 		course_course_code.setText(thisCourse.getCourseCode());
 		course_course_name.setText(thisCourse.getCourseName());
 		course_tv_time.setText(thisBlock.toTimeString());
		course_tv_date.setText(DATE_FORMAT.format(thisBlock.getStartTime().getTime()));
		//course
 		setCourseTimer();
 		course_location.setText(thisLocation.getBuilding() + ", " + thisLocation.getRoom());
//		course_building.setText(thisLocation.getBuilding());
//		course_room.setText(thisLocation.getRoom());
 		if (thisInstructor != null) {
 			course_instructor.setText(thisInstructor.getName());
			course_instructor_location.setText(thisInstructor.getLocation().getBuilding()
					+ ", " + thisInstructor.getLocation().getRoom());
 			Utility.populateInstructorHours(course_sv_instructor_hours, thisInstructor.getOfficeBlocks());
 		}
 	}
 	
 	private void initListeners() {
 		course_btn_edit_instructor.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent intent = new Intent(CourseActivity.this, InstructorActivity.class);
 				intent.putExtra("courseID", thisCourse.getID());
 				startActivityForResult(intent, INSTRUCTOR_EDIT_CODE);
 			}
 		});
 		
 		course_btn_notes.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				startActivity(new Intent(CourseActivity.this, NotesActivity.class));
 			}
 		});
 		
 		course_btn_exams.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				startActivity(new Intent(CourseActivity.this, CourseAssignActivity.class));
 			}
 		});
 	}
 }
