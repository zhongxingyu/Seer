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
 
 public class SemesterActivity extends ListActivity implements
 		OnItemLongClickListener {
 
 	private ArrayList<String> semesterList = new ArrayList<String>();
 	private ArrayAdapter<String> aa;
 	private DatabaseHelper db;
 	private Cursor cursor;
 	private ListView lv;
 	private TextView yearPath;
 	private String yearName;
 	private String yearId;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_semester);
 
 		yearPath = (TextView) findViewById(R.id.tv_yearpath);
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			yearName = bundle.getString(YearTable.COLUMN_NAME);
 			yearId = bundle.getString(YearTable.COLUMN_ID);
 		}
 		yearPath.setText(yearName);
 
 		db = new DatabaseHelper(this);
 		populateList();
 		
 
 		aa = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, semesterList);
 		setListAdapter(aa);
 
 		lv = getListView();
 		lv.setOnItemLongClickListener(this);
 
 	}
 
 	public void returnToYear(View v) {
 		Intent path = new Intent(SemesterActivity.this, YearActivity.class);
 		startActivity(path);
 		finish();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		String semesterName = semesterList.get(position);
 		String semesterId = SemesterTable.getId(semesterName, db);
 
 		Intent intent = new Intent(SemesterActivity.this, CourseActivity.class);
		intent.putExtra(SemesterTable.COLUMN_NAME, semesterName);
 		intent.putExtra(SemesterTable.COLUMN_ID, semesterId);
 		intent.putExtra(YearTable.COLUMN_NAME, yearName);
 		intent.putExtra(YearTable.COLUMN_ID, yearId);
 		startActivity(intent);
 	}
 	
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view,
 			int position, long id) {
 
 		final String name = semesterList.get(position);
 
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Confirm Delete");
 		b.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				SemesterTable.delete(name, db);
 				populateList();
 				aa.notifyDataSetChanged();
 			}
 		});
 		b.setNegativeButton("CANCEL", null);
 		b.create().show();
 
 		return true;
 	}
 
 	public void plusSemester(View view) {
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Add a Semester");
 		final EditText input = new EditText(this);
 		b.setView(input);
 		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				SemesterTable.add(input.getText().toString(), yearId, db);
 				populateList();
 				aa.notifyDataSetChanged();
 			}
 		});
 		b.setNegativeButton("CANCEL", null);
 		b.create().show();
 
 	}
 
 	private void populateList() {
 		cursor = db.getReadableDatabase().rawQuery(
 				"SELECT " + SemesterTable.COLUMN_NAME + " FROM "
 						+ SemesterTable.NAME + " WHERE "
 						+ SemesterTable.COLUMN_YEAR_ID + "=" + yearId
 						+ " ORDER BY " + SemesterTable.COLUMN_NAME + " ASC",
 				null);
 
 		semesterList.clear();
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			semesterList.add(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		cursor.close();
 
 	}
 
 }
