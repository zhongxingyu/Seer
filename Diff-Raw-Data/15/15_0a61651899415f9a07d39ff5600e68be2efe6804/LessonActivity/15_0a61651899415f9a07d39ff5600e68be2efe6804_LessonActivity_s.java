 package com.schema.bro;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Spinner;
 
 import com.schema.bro.ks.Customizer;
 import com.schema.bro.ks.Lesson;
 import com.schema.bro.ks.Schedule;
 import com.schema.bro.ks.TimePickerWidget;
 
 public class LessonActivity extends Activity implements OnClickListener{
 
 	private int val = 0, ID = -1;
 	private boolean edit = false;
 	private TimePickerWidget startTimePicker, endTimePicker;
 	private Spinner spinny;
 	private TypedArray images;
 	
 
 	@SuppressLint("Recycle")
 	protected void onCreate(Bundle savedInstanceState) {
 		Customizer.setTheme(this);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.lesson_activity);
 		// If API is sufficient 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		// Time pickers
 		startTimePicker = (TimePickerWidget) findViewById(R.id.startTime);
 		startTimePicker.setTitle("Startar");
 		endTimePicker = (TimePickerWidget) findViewById(R.id.endTime);
 		endTimePicker.setTitle("Slutar");
 		
 		// Weekday spinner
 		spinny = (Spinner) findViewById(R.id.weekSpinner);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.weekdays, R.layout.spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinny.setAdapter(adapter);
 		
 		// Load data
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			edit = extras.getBoolean("edit", false);
 			if (edit) {
 					try{
 						Lesson lesson = new Lesson(extras.getString("lesson", "empty"));
 						spinny.setSelection(lesson.getWeekdayValue() - 2);
 						startTimePicker.init(lesson.getStartHour(), lesson.getStartMinute());
 						endTimePicker.init(lesson.getEndHour(), lesson.getEndMinute());
						((EditText) findViewById(R.id.editName)).setText(lesson.getName());
						((EditText) findViewById(R.id.editRoom)).setText(lesson.getRoom());
						((EditText) findViewById(R.id.editTeacher)).setText(lesson.getMaster());
 						val = lesson.getImage();
 						ID = lesson.getID();
 					}catch(Exception ex){}
 					getActionBar().setTitle("Ändra lektion");
 			} else {
 				startTimePicker.init(8, 20);
 				endTimePicker.init(9, 0);
 				spinny.setSelection(extras.getInt("day", 0));
 			}
 		} else {
 			Log.e("LessonActivity:onCreate", "No extras");
 		}
 		
 		// Image Buttons
 		final ImageButton nextBtn = (ImageButton) findViewById(R.id.nextImageButton);
 		final ImageButton prevBtn = (ImageButton) findViewById(R.id.prevImageButton);
 		nextBtn.setOnClickListener(this);
 		prevBtn.setOnClickListener(this);
 		
 		// Image view
 		images = getResources().obtainTypedArray(R.array.imageIDs);
 		((ImageView) findViewById(R.id.lessonImage)).setImageResource(images.getResourceId(val, -1));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.done, menu);
 		if (edit)
 			menu.add("Ta bort").setIcon(R.drawable.ic_action_remove_light)
 					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		return true;
 	}
 
 	public void onClick(final View v) {
 		switch (v.getId()) {
 		case R.id.prevImageButton:
 			val--;
 			if (val < 0)
 				val = images.length() - 1;
 			break;
 		case R.id.nextImageButton:
 			val++;
 			if (val > images.length() - 1)
 				val = 0;
 			break;
 		}
 		((ImageView) findViewById(R.id.lessonImage)).setImageResource(images.getResourceId(val, -1));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getTitle().equals("Ta bort")) {
 			removeLesson();
 			finish();
 			return true;
 		}
 
 		switch (item.getItemId()) {
 		case R.id.done:
 			if (edit)
 				editLesson();
 			else
 				createLesson();
 			finish();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	public void editLesson() {
 		SharedPreferences prefs = getSharedPreferences(Schedule.PREFS_NAME, 0);
 		String data = getInputData(ID);
 		prefs.edit().putString("lesson_" + ID, data).commit();
 	}
 
 	public void createLesson() {
 		SharedPreferences prefs = getSharedPreferences(Schedule.PREFS_NAME, 0);
 		int ID = prefs.getInt("count", 0);
 		String data = getInputData(ID);
 		prefs.edit().putString("lesson_" + ID, data).commit();
 		prefs.edit().putInt("count", ID + 1).commit();
 	}
 
 	public void removeLesson() {
 		SharedPreferences prefs = getSharedPreferences(Schedule.PREFS_NAME, 0);
 		prefs.edit().putString("lesson_" + ID, "empty").commit();
 	}
 
 	public String getInputData(int ID){
 		final EditText edit_name = (EditText) findViewById(R.id.editName);
 		final EditText edit_teacher = (EditText) findViewById(R.id.editTeacher);
 		final EditText edit_room = (EditText) findViewById(R.id.editRoom);
 		
 		String name = edit_name.getText().toString();
 		String teacher = edit_teacher.getText().toString();
 		String room = edit_room.getText().toString();
 		String startTime = startTimePicker.getTime();
 		String endTime = endTimePicker.getTime();
 		String day = spinny.getSelectedItem().toString();
 		
 		if(name == null || name.equals(""))
 			name = "Lektions namn";
 		
 		if(teacher == null || teacher.equals(""))
 			teacher = " ";
 		
 		if(room == null || room.equals(""))
 			room = " ";
 
 		return Lesson.convertToString(day, startTime, endTime, name, room, teacher, val, ID);
 	}
 
 }
