 /**
  * @author Nick Huebner and Mark Redden
  * @version 1.0
  */
 
 package com.selagroup.schedu.activities;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.ScheduApplication;
 import com.selagroup.schedu.adapters.CourseBlockAdapter;
 import com.selagroup.schedu.adapters.CourseBlockAdapter.BlockDeleteListener;
 import com.selagroup.schedu.managers.CourseManager;
 import com.selagroup.schedu.managers.InstructorManager;
 import com.selagroup.schedu.model.Course;
 import com.selagroup.schedu.model.Instructor;
 import com.selagroup.schedu.model.Term;
 import com.selagroup.schedu.model.TimePlaceBlock;
 
 /**
  * The Class AddCourseActivity.
  */
 public class AddCourseActivity extends Activity {
 	private static final int sAddTimeCode = 5;
 
 	// Managers
 	private CourseManager mCourseManager;
 	private InstructorManager mInstructorManager;
 
 	// Views
 	private ListView addcourse_lv_schedule;
 	private EditText addcourse_et_course_code;
 	private EditText addcourse_et_course_name;
 	private AutoCompleteTextView addcourse_et_instructor;
 	private Button addcourse_btn_add_time;
 	private Button addcourse_btn_cancel;
 	private Button addcourse_btn_add;
 	private Button addcourse_btn_delete;
 
 	// Adapters
 	private CourseBlockAdapter mScheduleAdapter;
 
 	// Data
 	private boolean mEditMode = false;
 	private Course mCourseToEdit = null;
 	private Term mCurrentTerm;
 	private List<TimePlaceBlock> mScheduleBlocks = new LinkedList<TimePlaceBlock>();
 	private List<Instructor> mInstructors;
 
 	private AlertDialog.Builder validateDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_addcourse);
 
 		// Get the selected term
 		ScheduApplication myApp = ((ScheduApplication) getApplication());
 		mCurrentTerm = myApp.getCurrentTerm();
 
 		// Get the course manager and the instructor manager
 		mCourseManager = myApp.getCourseManager();
 		mInstructorManager = myApp.getInstructorManager();
 		mInstructors = mInstructorManager.getAll();
 
 		mEditMode = getIntent().getBooleanExtra("edit", false);
 		if (mEditMode) {
 			mCourseToEdit = (Course) getIntent().getSerializableExtra("course");
 			setTitle("Edit Course");
 		}
 
 		initWidgets();
 		initListeners();
 		reset();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case sAddTimeCode:
 			if (resultCode == RESULT_OK) {
 				// Add a block
 				TimePlaceBlock block = (TimePlaceBlock) data.getSerializableExtra("block");
 				mScheduleBlocks.add(block);
 				mScheduleAdapter.notifyDataSetChanged();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Initialize widgets for this activity
 	 */
 	private void initWidgets() {
 		addcourse_lv_schedule = (ListView) findViewById(R.id.addcourse_lv_schedule);
 		addcourse_et_course_code = (EditText) findViewById(R.id.addcourse_et_course_code);
 		addcourse_et_course_name = (EditText) findViewById(R.id.addcourse_et_course_name);
 		addcourse_et_instructor = (AutoCompleteTextView) findViewById(R.id.addcourse_et_instructor);
 		addcourse_btn_add_time = (Button) findViewById(R.id.addcourse_btn_add_time);
 		addcourse_btn_cancel = (Button) findViewById(R.id.addcourse_btn_cancel);
 		addcourse_btn_add = (Button) findViewById(R.id.addcourse_btn_add);
 		addcourse_btn_delete = (Button) findViewById(R.id.addcourse_btn_delete);
 		
 		if (!mEditMode) {
 			addcourse_et_course_code.requestFocus();
 		} else {
 			addcourse_btn_delete.setVisibility(View.VISIBLE);
 		}
 
 		// Set up list view adapter for schedule blocks
 		mScheduleAdapter = new CourseBlockAdapter(this, android.R.layout.simple_list_item_1, mScheduleBlocks, new BlockDeleteListener() {
 			public void onDelete(TimePlaceBlock iBlock) {
 				if (mEditMode) {
 					mCourseToEdit.removeScheduleBlock(iBlock);
 					mScheduleBlocks.remove(iBlock);
 					mScheduleAdapter.notifyDataSetChanged();
 				}
 			}
 		});
 		addcourse_lv_schedule.setAdapter(mScheduleAdapter);
 
 		// Set up adapter to auto complete instructor
 		addcourse_et_instructor.setAdapter(new ArrayAdapter<Instructor>(this, android.R.layout.simple_dropdown_item_1line, mInstructors));
 	
 		validateDialog = new AlertDialog.Builder(AddCourseActivity.this);
 		validateDialog
 			.setMessage(R.string.addcourse_dialog_validate_text)
 			.setPositiveButton(R.string.addcourse_dialog_validate_OK_btn, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 	}
 
 	/**
 	 * Initializes the listeners.
 	 */
 	private void initListeners() {
 		// Set up add time button listener
 		addcourse_btn_add_time.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				startActivityForResult(new Intent(AddCourseActivity.this, AddTimeActivity.class), sAddTimeCode);
 			}
 		});
 
 		// Set up done button listener
 		addcourse_btn_cancel.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				finish();
 			}
 		});
 
 		// Set up "add" button listener
 		addcourse_btn_add.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				if (mEditMode) {
 					if (editCourseHelper()) {
 						finish();
 					}
 					else {
 						validateDialog.show();
 					}
 				}
 				else {
 					// Try to add the course. If successful, reset fields and finish
 					if (addCourseHelper()) {
 						reset();
 						finish();
 					}
 					else {
 						validateDialog.show();
 					}
 				}
 				ScheduApplication app = (ScheduApplication)getApplication();
 				app.getAlarmSystem().scheduleEventsForDay(mCourseManager.getAllForTerm(mCurrentTerm.getID()), Calendar.getInstance(), true);
 			}
 		});
 		
 		if (mEditMode) {
 			addcourse_btn_delete.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					(new AlertDialog.Builder(AddCourseActivity.this))
 						.setTitle(R.string.addcourse_dialog_delete_title)
 						.setMessage(R.string.addcourse_dialog_delete_text)
 						.setNegativeButton(R.string.addcourse_cancel_btn, new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 							}
 						})
 						.setPositiveButton(R.string.addcourse_delete_btn, new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								mCourseManager.delete(mCourseToEdit);
 								finish();
 							}
 						})
 						.show();
 				}
 			});
 		}
 	}
 
 	/**
 	 * Resets the activity's UI and data
 	 */
 	private void reset() {
 		// If editing, populate the data from the course
 		if (mEditMode) {
 			mScheduleBlocks.addAll(mCourseToEdit.getScheduleBlocks());
 			addcourse_et_course_code.setText(mCourseToEdit.getCode());
 			addcourse_et_course_name.setText(mCourseToEdit.getName());
 			Instructor instructor = mCourseToEdit.getInstructor();
 			addcourse_et_instructor.setText(instructor == null ? "" : instructor.toString());
 			addcourse_btn_add.setText("Save");
 		}
 		// If adding, clear data
 		else {
 			mScheduleBlocks.clear();
 			addcourse_et_course_code.setText("");
 			addcourse_et_course_name.setText("");
 			addcourse_et_course_code.requestFocus();
 		}
 		mScheduleAdapter.notifyDataSetChanged();
 	}
 
 	private boolean editCourseHelper() {
 		String code = addcourse_et_course_code.getText().toString();
 		String instructorName = addcourse_et_instructor.getText().toString();
 
 		// Update course if it has a term, a code, and at least one schedule block
 		if (mCurrentTerm != null && !code.equals("") && mScheduleBlocks.size() > 0) {
 			// Update course name/code
 			mCourseToEdit.setCode(code);
 			mCourseToEdit.setName(addcourse_et_course_name.getText().toString());
 
 			// Create and insert a new instructor or find an existing instructor
 			Instructor instructor = new Instructor(-1, instructorName, "", "");
 			if (instructorName.equals("")) {
 				instructor = null;
 			}
 			int index = mInstructors.indexOf(instructor);
 			if (index != -1) {
 				instructor = mInstructors.get(index);
 			}
 			else {
 				mInstructors.add(instructor);
				mInstructorManager.insert(instructor);
 			}
 			mCourseToEdit.setInstructor(instructor);
 			
 			// Add new schedule blocks
 			List<TimePlaceBlock> existingBlocks = mCourseToEdit.getScheduleBlocks();
 			for (TimePlaceBlock block : mScheduleBlocks) {
 				if (!existingBlocks.contains(block)) {
 					mCourseToEdit.addScheduleBlock(block);
 				}
 			}
 			
 			// Database update
 			mCourseManager.update(mCourseToEdit);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	/**
 	 * Helps to add a course given the information populated in this activity
 	 * @return true, if successfully added a course
 	 */
 	private boolean addCourseHelper() {
 		// Get information for the course
 		String code = addcourse_et_course_code.getText().toString();
 		String name = addcourse_et_course_name.getText().toString();
 		String instructorName = addcourse_et_instructor.getText().toString();
 
 		// Create and insert a new instructor or find an existing instructor
 		Instructor instructor = new Instructor(-1, instructorName, "", "");
 		if (instructorName.equals("")) {
 			instructor = null;
 		}
 		else {
 			mInstructors.add(instructor);
 		}
 
 		Course newCourse = new Course(-1, mCurrentTerm, code, name, instructor);
 
 		// Add schedule blocks
 		for (TimePlaceBlock block : mScheduleBlocks) {
 			newCourse.addScheduleBlock(block);
 		}
 
 		// Insert new course if it has a term, a code, and at least one schedule block
 		if (mCurrentTerm != null && !code.equals("") && mScheduleBlocks.size() > 0) {
 			mCourseManager.insert(newCourse);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 }
