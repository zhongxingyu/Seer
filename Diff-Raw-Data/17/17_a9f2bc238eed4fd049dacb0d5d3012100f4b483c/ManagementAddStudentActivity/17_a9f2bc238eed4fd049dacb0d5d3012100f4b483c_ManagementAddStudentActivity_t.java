 package edu.upenn.nettercenter.auni.cookcompetition.sections;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Switch;
 import android.widget.Toast;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.OptionsItem;
 import com.googlecode.androidannotations.annotations.OrmLiteDao;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.j256.ormlite.dao.Dao;
 
import java.sql.SQLException;

 import edu.upenn.nettercenter.auni.cookcompetition.DatabaseHelper;
 import edu.upenn.nettercenter.auni.cookcompetition.R;
 import edu.upenn.nettercenter.auni.cookcompetition.models.Student;
 
 @EActivity(R.layout.activity_management_add_student)
 public class ManagementAddStudentActivity extends Activity {
 
 	@OrmLiteDao(helper = DatabaseHelper.class, model = Student.class)
 	Dao<Student, Long> dao;
 
 	@Extra
 	Long studentId;
 
 	@ViewById
 	EditText studentFirstName;
 	
 	@ViewById
 	EditText studentLastInitial;
 	
 	@ViewById
 	EditText studentNickname;
 	
 	@ViewById
 	ViewGroup studentEditor;
 	
 	@ViewById
 	Switch isActive;
 			
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.activity_management_add_student, menu);
 
 		// Hide "Save and Continue" button in edit mode
 		boolean visible = studentId == null;
 		menu.findItem(R.id.menu_add_student_next).setVisible(visible);
 
 		return true;
 	}
 	
 	@AfterViews
 	void setUpActionBar() {
 		ActionBar actionBar = getActionBar();
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View customActionBarView = inflater.inflate(R.layout.action_bar_add_student, null);
         View doneItem = customActionBarView.findViewById(R.id.menu_add_student_done);
         doneItem.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				done();
 			}
 		});
         
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                 ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                 ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView);        
 	}
 	
 	@AfterViews
 	void setFocus() {
 		studentFirstName.requestFocus();
 	}
 	
 	@AfterViews
 	void loadStudent() {
 		if (studentId == null) return;
 
 		try {
 			Student student = dao.queryForId(studentId);
 			studentFirstName.setText(student.getFirstName());
 			studentLastInitial.setText(student.getLastInitial());
 			studentNickname.setText(student.getNickname());
 			isActive.setChecked(student.isActive());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	void done() {
 		if (isNameEmpty()) {
 			saveCurrentStudent();
 		}
 		finish();
 	}
 	
 	@OptionsItem(R.id.menu_add_student_next)
 	void next() {
 		if (isNameEmpty()) {
 			saveCurrentStudent();
 			clearAllEditText(studentEditor);
 			setFocus();
 		}
 	}
 	
 	private boolean isNameEmpty() {
 		return studentFirstName.getText().toString().trim().length() > 0;
 	}
 
 	@OptionsItem(R.id.menu_add_student_discard)
 	void discard() {
 		if (isNameEmpty()) {
 			finish();
 		} else {
 			new AlertDialog.Builder(this)
 				.setMessage("Discard your changes?")
 			    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			    	@Override
 				    public void onClick(DialogInterface dialog, int which) {
 				        finish();
 				    }
 			    })
 			    .setNegativeButton(android.R.string.cancel, null)
 			    .show();
 		}
 	}
 	
 	private void saveCurrentStudent() {
 		try {
 			Student student;
 			if (studentId == null) {
 				student = new Student();
 			} else {
 				student = dao.queryForId(studentId);
 			}
 			student.setFirstName(studentFirstName.getText().toString().trim());
			student.setLastInitial(studentLastInitial.getText().toString().trim());
 			student.setNickname(studentNickname.getText().toString().trim());
 			student.setActive(isActive.isChecked());	
 			dao.createOrUpdate(student);
 			
 			String message;
 			if (studentId == null) {
 				message = "Student created."; 
 			} else {
 				message = "Student updated.";
 			}
 			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void clearAllEditText(ViewGroup viewGroup) {
 		for (int i = 0; i < viewGroup.getChildCount(); i++) {
 			View view = viewGroup.getChildAt(i);
 			if (view instanceof EditText) {
 				((EditText) view).getText().clear();
 			} else if (view instanceof ViewGroup) {
 				clearAllEditText((ViewGroup) view);
 			}
 		}
 	}	
 }
