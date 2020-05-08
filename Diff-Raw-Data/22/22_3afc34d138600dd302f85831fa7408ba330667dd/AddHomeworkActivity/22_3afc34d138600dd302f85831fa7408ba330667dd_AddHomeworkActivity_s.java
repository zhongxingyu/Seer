 //http://stackoverflow.com/questions/7200108/android-gettext-from-edittext-field
 
 package edu.mines.caseysoto.schoolschedulercaseysoto;
 
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 
 public class AddHomeworkActivity extends Activity {
 	private final static int DESC_MAX = 140;
 	private View mCourseText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_homework);
 
 		mCourseText = findViewById(R.id.courseNameEnd);
 		//get the message from the intent
 		Intent intent = getIntent();
 		String message = intent.getStringExtra( MainActivity.COURSE_MNAME);
 		((TextView) mCourseText).setText(message);
 	}
 
 	public void submit(View view){
 		EditText nameInput = (EditText) findViewById(R.id.nameInput);
 		EditText descriptionInput = (EditText) findViewById(R.id.descriptionInput);
 		EditText dateInput = (EditText) findViewById(R.id.dateInput);
 
 		String hwName = nameInput.getText().toString();
 		String desc = descriptionInput.getText().toString();
 		String dueDate = dateInput.getText().toString();
 		Intent intent = getIntent();
 		String course = intent.getStringExtra( MainActivity.COURSE_MNAME);
 
 		if(hwName.length() > 0 && desc.length() > 0){
 			if(desc.length() > DESC_MAX) {
 				desc = desc.substring(0, DESC_MAX);
 			}
 		} else {
 			hwName = "Untitled";
 			desc = "None";
 		}
 
 		if(checkDate(dueDate)){
 			Toast toast = Toast.makeText(getApplicationContext(), "Name: " + hwName + " Desc: " + desc + " Due Date: " + dueDate, Toast.LENGTH_LONG);
 			toast.show();
 		} else {
			Toast toast = Toast.makeText(getApplicationContext(), "Incorrect date format. Set to today's date." , Toast.LENGTH_LONG);
 			toast.show();
 			dueDate = "10022013";
 		}
 		
 		insertNewHW(hwName, dueDate, desc, course);
 		
 		finish();
 	}
 
 	private boolean checkDate(String dateInput) {
 		boolean correctInput = false;
 		
 		if(dateInput.length() == 8){
 			dateInput = dateInput.substring(0, 2) + "-" + dateInput.substring(2, 4) + "-" + dateInput.substring(4, 8);
 		}
 		
 		if(dateInput.length() == 10){
 			    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
 			    sdf.setLenient(false);
 			    correctInput = sdf.parse(dateInput, new ParsePosition(0)) != null;
 		} else {
 			Toast toast = Toast.makeText(getApplicationContext(), "Incorrect date format. Set to today's date." , Toast.LENGTH_LONG);
 			toast.show();
 		}
 
 		return correctInput;
 	}
 	
 	public void insertNewHW(String name, String date, String desc, String course){
 		ContentValues values = new ContentValues();
 		values.put( HomeworkTable.COLUMN_NAME, name );
 		values.put( HomeworkTable.COLUMN_DATE, date );
 		values.put( HomeworkTable.COLUMN_DESCRIPTION, desc);
 		values.put( HomeworkTable.COLUMN_COURSE_NAME, course);
 		
 		String[] projection = { HomeworkTable.COLUMN_ID, HomeworkTable.COLUMN_NAME};
 		String[] selection = {name};
 		
 		Uri CourseUri = getContentResolver().insert( SchedulerContentProvider.CONTENT_URI_H, values );
 
 		//checks to see if that course name has already been added
 		Cursor cursor = getContentResolver().query( SchedulerContentProvider.CONTENT_URI_H, projection, "name=?", selection, HomeworkTable.COLUMN_ID + " DESC" );
 		if(cursor.getCount() > 1){
 			cursor.moveToFirst();
 			Uri courseUri = Uri.parse( SchedulerContentProvider.CONTENT_URI_H + "/" +  cursor.getString(cursor.getColumnIndexOrThrow( HomeworkTable.COLUMN_ID )) );
 			getContentResolver().delete(courseUri, null, null);
 			Toast toast = Toast.makeText(getApplicationContext(),"Have already added " + name +"!" , Toast.LENGTH_LONG);
 			toast.show();
 			finish();
 		}
 		cursor.close();
 	}
 }
