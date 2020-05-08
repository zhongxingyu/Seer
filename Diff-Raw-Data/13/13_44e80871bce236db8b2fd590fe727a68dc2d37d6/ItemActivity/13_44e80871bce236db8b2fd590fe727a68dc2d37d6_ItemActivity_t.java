 package com.teamswag.course_organizer;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ItemActivity extends ListActivity implements
 		OnItemLongClickListener {
 
 	ArrayList<String> itemList = new ArrayList<String>();
 	private ArrayAdapter<String> aa;
 	private DatabaseHelper db;
 	private Cursor cursor;
 	private ListView lv;
 	private TextView yearPath;
 	private TextView semesterPath;
 	private TextView coursePath;
 	private TextView criteriaPath;
 	private String yearName;
 	private String yearId;
 	private String semesterName;
 	private String semesterId;
 	private String courseName;
 	private String courseId;
 	private String criteriaName;
 	private String criteriaId;
 	private TextView selection;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_item);
 
 		yearPath = (TextView) findViewById(R.id.tv_itemyear);
 		semesterPath = (TextView) findViewById(R.id.tv_itemsemester);
 		coursePath = (TextView) findViewById(R.id.tv_itemcourse);
 		criteriaPath = (TextView) findViewById(R.id.tv_itemcriteria);
 
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			yearName = bundle.getString(YearTable.COLUMN_NAME);
 			yearId = bundle.getString(CourseTable.COLUMN_YEAR_ID);
 			semesterName = bundle.getString(SemesterTable.COLUMN_NAME);
 			semesterId = bundle.getString(CourseTable.COLUMN_SEMESTER_ID);
 			courseName = bundle.getString(CourseTable.COLUMN_NAME);
 			courseId = bundle.getString(CriteriaTable.COLUMN_COURSE_ID);
 			criteriaName = bundle.getString(CriteriaTable.COLUMN_NAME);
 			criteriaId = bundle.getString(ItemTable.COLUMN_CRITERIA_ID);
 
 		}
 		yearPath.setText(yearName);
 		semesterPath.setText(" ->" + semesterName);
 		coursePath.setText(" ->" + courseName);
 		criteriaPath.setText(" ->" + criteriaName);
 
 		db = new DatabaseHelper(this);
 		populateList();
 
		aa = new ItemAdapter();
 
 
		setListAdapter(aa);
 
 		lv = getListView();
 		lv.setOnItemLongClickListener(this);
 
 	}
 
	class ItemAdapter extends ArrayAdapter<String> {
		ItemAdapter() {
 			super(ItemActivity.this, R.layout.row, R.id.tv_leftvalue, itemList);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = super.getView(position, convertView, parent);
 			TextView right = (TextView) row.findViewById(R.id.tv_rightvalue);
 			right.setText("TEST GRADE");
 			return row;
 		}
 	}
 
 	public void returnToYear(View v) {
 		Intent path = new Intent(ItemActivity.this, YearActivity.class);
 		startActivity(path);
 	}
 
 	public void returnToSemester(View v) {
 		Intent path = new Intent(ItemActivity.this, SemesterActivity.class);
 		path.putExtra(YearTable.COLUMN_NAME, yearName);
 		path.putExtra(SemesterTable.COLUMN_YEAR_ID, yearId);
 		startActivity(path);
 	}
 
 	public void returnToCourse(View v) {
 		Intent path = new Intent(ItemActivity.this, CourseActivity.class);
 		path.putExtra(YearTable.COLUMN_NAME, yearName);
 		path.putExtra(CourseTable.COLUMN_YEAR_ID, yearId);
 		path.putExtra(SemesterTable.COLUMN_NAME, semesterName);
 		path.putExtra(CourseTable.COLUMN_SEMESTER_ID, semesterId);
 		startActivity(path);
 	}
 
 	public void returnToCriteria(View v) {
 		Intent path = new Intent(ItemActivity.this, CriteriaActivity.class);
 		path.putExtra(YearTable.COLUMN_NAME, yearName);
 		path.putExtra(CourseTable.COLUMN_YEAR_ID, yearId);
 		path.putExtra(SemesterTable.COLUMN_NAME, semesterName);
 		path.putExtra(CourseTable.COLUMN_SEMESTER_ID, semesterId);
 		path.putExtra(CourseTable.COLUMN_NAME, courseName);
 		path.putExtra(CriteriaTable.COLUMN_COURSE_ID, courseId);
 		startActivity(path);
 	}
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view,
 			int position, long id) {
 
 		final String name = itemList.get(position);
 
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Confirm Delete");
 		b.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				ItemTable.delete(name, criteriaId, db);
 				populateList();
 				aa.notifyDataSetChanged();
 			}
 		});
 		b.setNegativeButton("CANCEL", null);
 		b.create().show();
 
 		return true;
 	}
 
 	public void plusItem(View view) {
 		LayoutInflater inflater = LayoutInflater.from(this);
 		View inputLayout = inflater.inflate(R.layout.activity_inputitems, null);
 
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle("Add an Item");
 		b.setView(inputLayout);
 		final EditText itemName = (EditText) inputLayout
 				.findViewById(R.id.et_itemname);
 		final EditText itemScore = (EditText) inputLayout
 				.findViewById(R.id.et_itemscore);
 		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String name = itemName.getText().toString();
 				Double score = Double.parseDouble(itemScore.getText()
 						.toString());
 				ItemTable.add(name, criteriaId, score, db);
 				populateList();
 				aa.notifyDataSetChanged();
 			}
 		});
 		b.setNegativeButton("CANCEL", null);
 		b.create().show();
 
 	}
 
 	private void populateList() {
 		cursor = db.getReadableDatabase().rawQuery(
 				"SELECT " + ItemTable.COLUMN_NAME + " FROM " + ItemTable.NAME
 						+ " WHERE " + ItemTable.COLUMN_CRITERIA_ID + "=\'"
 						+ criteriaId + "\' ORDER BY " + ItemTable.COLUMN_NAME
 						+ " ASC", null);
 
 		itemList.clear();
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			itemList.add(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		cursor.close();
 
 	}
 
 }
