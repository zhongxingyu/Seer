 package com.teamswag.course_organizer;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class CourseActivity extends ListActivity implements
 		OnItemLongClickListener {
 
 	ArrayList<String> courseList = new ArrayList<String>();
 	ArrayAdapter<String> aa;
 	DatabaseHelper db;
 	Cursor cursor;
 	ListView lv;
 	TextView yearPath;
 	TextView semesterPath;
 	String yearName;
 	String yearId;
 	String semesterName;
 	String semesterId;
 	TextView courseTest;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_course);
 
 		yearPath =(TextView) findViewById(R.id.tv_yearpathcourse);
 		semesterPath = (TextView) findViewById(R.id.tv_semesterpath);
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			
			//semesterName = bundle.getString(SemesterTable.COLUMN_NAME);
			semesterName = bundle.getString("ctest");
 			semesterId = bundle.getString(SemesterTable.COLUMN_ID);
 			yearName = bundle.getString(YearTable.COLUMN_NAME);
 			yearId = bundle.getString(YearTable.COLUMN_ID);
 		}
 		yearPath.setText(yearName);
 		semesterPath.setText(semesterName);
 
 		db = new DatabaseHelper(this);
 		populateList();
 
 		aa = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, courseList);
 		setListAdapter(aa);
 
 		lv = getListView();
 		lv.setOnItemLongClickListener(this);
 
 	}
 	
 
 	public void returnToYear(View v) {
 		Intent path = new Intent(CourseActivity.this, YearActivity.class);
 		startActivity(path);
 		finish();
 	}
 
 	public void returnToSemester(View v) {
 		Intent path = new Intent(CourseActivity.this, SemesterActivity.class);
 		path.putExtra(YearTable.COLUMN_NAME, yearName);
 		path.putExtra(YearTable.COLUMN_ID, yearId);
 		startActivity(path);
 		;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		String courseName = courseList.get(position);
 		String courseId = CourseTable.getId(courseName, db);
 
 		Intent criteria = new Intent(CourseActivity.this,
 				CriteriaActivity.class);
 		criteria.putExtra(YearTable.COLUMN_NAME, yearName);
 		criteria.putExtra(YearTable.COLUMN_ID, yearId);
 		criteria.putExtra(SemesterTable.COLUMN_NAME, semesterName);
 		criteria.putExtra(SemesterTable.COLUMN_ID, semesterId);
 		criteria.putExtra(CourseTable.COLUMN_NAME, courseName);
 		criteria.putExtra(CourseTable.COLUMN_ID, courseId);
 		startActivity(criteria);
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view,
 			int position, long id) {
 
 		final String name = courseList.get(position);
 
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Confirm Delete");
 		b.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				CourseTable.delete(name, db);
 				populateList();
 				aa.notifyDataSetChanged();
 			}
 		});
 		b.setNegativeButton("CANCEL", null);
 		b.create().show();
 
 		return true;
 	}
 
 	public void plusCourse(View view) {
 		// Will eventually get to launching the InputCourseActivity
 
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Add a Course");
 		final EditText input = new EditText(this);
 		b.setView(input);
 		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				CourseTable.add(input.getText().toString(), semesterId, yearId,
 						db);
 				populateList();
 				aa.notifyDataSetChanged();
 			}
 		});
 		b.setNegativeButton("CANCEL", null);
 		b.create().show();
 
 	}
 
 	private void populateList() {
 		cursor = db.getReadableDatabase().rawQuery(
 				"SELECT " + CourseTable.COLUMN_NAME + " FROM "
 						+ CourseTable.NAME + " WHERE "
 						+ CourseTable.COLUMN_YEAR_ID + "=" + yearId
 						+ " AND " + CourseTable.COLUMN_SEMESTER_ID + "="
 						+ semesterId + " ORDER BY " + CourseTable.COLUMN_NAME
 						+ " ASC", null);
 
 		courseList.clear();
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			courseList.add(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		cursor.close();
 
 	}
 
 }
