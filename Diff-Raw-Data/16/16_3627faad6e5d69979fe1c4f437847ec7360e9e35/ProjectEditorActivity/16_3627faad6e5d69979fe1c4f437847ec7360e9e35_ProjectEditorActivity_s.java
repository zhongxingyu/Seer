 /**
  * @author Kolby Canlser
  * @version 1.0.BETA_02
  * 
  *  Creates the Project Editor page
  */
 
 /*
  * TODO Need JavaDocing for this entire class: ProjectEditor
  */
 
 package com.ubundude.timesheet;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 /** 
  * ProjectEditorActivity class
  * 
  * Class that creates a layout and defines logic for creating, editing,
  * and deleting projects in the projects table.
  */
 public class ProjectEditorActivity extends Activity {
 	private SQLiteDatabase db;
 	private TimesheetDatabaseHelper dbHelp = new TimesheetDatabaseHelper(this);
 	private EditText shortCodeEdit, fullNameEdit, rateEdit, descEdit;
 	private int project;
 	private Button saveButton, cancelButton, deleteButton;
 	
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.editor_project);
         
         Bundle extras = getIntent().getExtras();
         if(extras != null) {
         	project = extras.getInt("PROJECT_NAME");
         }
         
         saveButton = (Button)findViewById(R.id.saveProjectButton);
         cancelButton = (Button)findViewById(R.id.cancelProjectButton);
         deleteButton = (Button)findViewById(R.id.projectDeleteButton);
         
         if (project != 1) {
         	loadProject(project);
         } 
         
         saveButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(project != 1) {
 					updateProject(project);
 				} else {
 					insertNewProject(v);
 				}
 				
 			}
 		});
         
         cancelButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				cancelButtonHandler(v);
 			}
 		});
         
         deleteButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				projectDeleteHandler(v, project);
 			}
 		});
 	}
 	
 	/**
 	 * Method called to load project from database when ID is passed by intent
 	 * 
 	 * @param project The ID of the project selected from the spinner
 	 */
 	public void loadProject(int project) {
 		/** Initializes the edit text fields for use */
 		shortCodeEdit = (EditText)findViewById(R.id.shortCodeEditText);
 		fullNameEdit = (EditText)findViewById(R.id.fullNameEditText);
 		rateEdit = (EditText)findViewById(R.id.rateEditText);
 		descEdit = (EditText)findViewById(R.id.descriptionEditText);
 		
 		String selectProject = "select * from projects where _id = " + project;
 		db = dbHelp.getReadableDatabase();
 		Cursor cu = db.rawQuery(selectProject, null);
 		cu.moveToFirst();
 		shortCodeEdit.setText(cu.getString(2));
 		fullNameEdit.setText(cu.getString(1));
 		rateEdit.setText(cu.getString(3));
 		descEdit.setText(cu.getString(4));
 		cu.close();
 		db.close();
 	}
 	
 	public void updateProject(int projectId) {
 		/** Initializes the edit text fields for use */
 		shortCodeEdit = (EditText)findViewById(R.id.shortCodeEditText);
 		fullNameEdit = (EditText)findViewById(R.id.fullNameEditText);
 		rateEdit = (EditText)findViewById(R.id.rateEditText);
 		descEdit = (EditText)findViewById(R.id.descriptionEditText);
 		
 		/** Variables to store form elements into for insertion to database */
 		final String shortCode, fullName, rate, description;
 		
 		/** Get the text from the EditTexts, convert to strings, and store the values */
 		shortCode = shortCodeEdit.getText().toString();
 		fullName = fullNameEdit.getText().toString();
 		rate = rateEdit.getText().toString();
 		description = descEdit.getText().toString();
 		
 		String updateSQL = "update projects " +
 				"set name='" + fullName + "', shortcode='" + shortCode + "', rate='" +
 				rate + "', description='" +
 				description + "' " +
 				"where _id = " + projectId;
 		
 		db = dbHelp.getWritableDatabase();
 		db.execSQL(updateSQL);
 		db.close();
 		
 		finish();
 		
 	}
 	
 	/**
 	 * Method that is called when the Save Button is pressed
 	 * <p>
 	 * Only called if a new project needs to be inserted. 
 	 * Otherwise, project gets updated via updateProject()
 	 */
 	public void insertNewProject(View v) {
 		 /** Initializes the edit text fields for use */
 		shortCodeEdit = (EditText)findViewById(R.id.shortCodeEditText);
 		fullNameEdit = (EditText)findViewById(R.id.fullNameEditText);
 		rateEdit = (EditText)findViewById(R.id.rateEditText);
 		descEdit = (EditText)findViewById(R.id.descriptionEditText);
 		
 		/** Variables to store form elements into for insertion to database */
 		final String shortCode, fullName, rate, description;
 		
 		/** Get the text from the EditTexts, convert to strings, and store the values */
 		shortCode = shortCodeEdit.getText().toString();
 		fullName = fullNameEdit.getText().toString();
 		rate = rateEdit.getText().toString();
 		description = descEdit.getText().toString();
 		
 		if (fullName.equals("") && shortCode.equals("")) {
 			Context context = getApplicationContext();
 			String toastTest = "Project Is Empty";
 			int duration = Toast.LENGTH_LONG;
 			
 			Toast toast = Toast.makeText(context, toastTest, duration);
 			toast.show();
 		} else {
 			String insertString = "insert into projects (name, shortcode, rate, description) " +
 					"values('" + fullName + "', '" + shortCode + "', '" + rate +
 					"', '" + description + "')";
 		
 			db = dbHelp.getWritableDatabase();
 			try {
 				db.execSQL(insertString);
 			} catch (Exception ex) {
 				Log.d("projectSaveFail", ex.getMessage(), ex.fillInStackTrace());
 			}
 			db.close();
 			
 			/** Return to the previous activity */
 			finish();
 		}
 	}
 
 	/**
 	 * Handler for the Cancel Button
 	 * 
 	 * @param v Gets the current View
 	 */
 	public void cancelButtonHandler(View v){
 		finish();
 	}
 	
 	public void projectDeleteHandler(View v, int projectId) {
 		String deleteSQL = "delete from projects where _id = " + projectId;
 		if(projectId != 1) {
 			db = dbHelp.getWritableDatabase();
 			db.execSQL(deleteSQL);
 			db.close();
 			finish();
 		} else {
 			Context context = getApplicationContext();
 			String toastTest = "Cannot Delete Empty Project";
 			int duration = Toast.LENGTH_LONG;
 			
 			Toast toast = Toast.makeText(context, toastTest, duration);
 			toast.show();
 		}
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.editor_menu, menu);
         return true;
     }
 	
 	@Override 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
		case R.id.menu_defaults:
 			Toast.makeText(this, "Defaults button clicked", Toast.LENGTH_SHORT).show();
 			return(true);
 		case R.id.delete_item:
 			View v = null;
 			projectDeleteHandler(v, project);
 			return(true);
 		}
 		return(super.onOptionsItemSelected(item));
 	}
 	
 }
