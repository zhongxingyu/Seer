 package com.android.interview;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class Interview extends Activity {
     private static final int TAKE_PHOTO = 0;
     private static final int RECORD_AUDIO = 1;
     private static final int RECORD_VIDEO = 2;
 
     private static final int SUBJECT_DASHBOARD_VIEW = 0;
     private static final int SUBJECT_CREATE_VIEW = 1;
     private static final int SUBJECT_LIST_VIEW = 2;
     private static final int SUBJECT_DETAILS_VIEW = 3;
 
     private ViewFlipper flipper;
     private File currentSubjectDirectory = null;
     private File baseDirectory = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dashboard);
 
         this.baseDirectory = getFilesDir();
         this.flipper = (ViewFlipper) findViewById(R.id.subject_views);
 
        // Setup dashboard buttons
         Button subjectCreateButton = (Button) findViewById(R.id.subject_create_button);
         subjectCreateButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 flipper.setDisplayedChild(Interview.SUBJECT_CREATE_VIEW);
             }
         });
 
         Button subjectListButton = (Button) findViewById(R.id.subject_list_button);
         subjectListButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 flipper.setDisplayedChild(Interview.SUBJECT_LIST_VIEW);
             }
         });
 
         // Setup subject creation buttons
         Button subjectCreateSaveButton = (Button) findViewById(R.id.subject_create_save_button);
         subjectCreateSaveButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 EditText newSubjectField = (EditText) findViewById(R.id.subject_create_name);
                 String currentSubjectName = newSubjectField.getText()
                         .toString();
 
                 createSubject(currentSubjectName);
 
                 // Update dropdown list
                 Spinner spinner = (Spinner) findViewById(R.id.subject_list_dropdown);
                 populateSpinner(spinner);
 
                 flipper.setDisplayedChild(Interview.SUBJECT_DETAILS_VIEW);
             }
         });
 
         Button subjectCreateBackButton = (Button) findViewById(R.id.subject_create_back_button);
         subjectCreateBackButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 flipper.setDisplayedChild(Interview.SUBJECT_DASHBOARD_VIEW);
             }
         });
 
         Spinner spinner = (Spinner) findViewById(R.id.subject_list_dropdown);
         populateSpinner(spinner);
         spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parent, View view,
                     int position, long id) {
                 String subjectName = parent.getItemAtPosition(position)
                         .toString();
                 currentSubjectDirectory = new File(baseDirectory
                         .getAbsolutePath() + File.separator + subjectName);
             }
 
             public void onNothingSelected(AdapterView parent) {
                 // Do nothing
             }
         });
 
         Button subjectListViewButton = (Button) findViewById(R.id.subject_list_view_button);
         subjectListViewButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 flipper.setDisplayedChild(Interview.SUBJECT_DETAILS_VIEW);
             }
         });
 
         Button subjectListBackButton = (Button) findViewById(R.id.subject_list_back_button);
         subjectListBackButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 flipper.setDisplayedChild(Interview.SUBJECT_DASHBOARD_VIEW);
             }
         });
 
         // Setup subject view buttons
         Button subjectViewBackButton = (Button) findViewById(R.id.subject_dashboard_button);
         subjectViewBackButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 flipper.setDisplayedChild(Interview.SUBJECT_DASHBOARD_VIEW);
             }
         });
     }
 
     public void populateSpinner(Spinner spinner) {
         // Setup subject listing buttons
         ArrayAdapter<String> subjectListAdapter = new ArrayAdapter<String>(
                 this, android.R.layout.simple_spinner_dropdown_item,
                 this.baseDirectory.list());
         spinner.setAdapter(subjectListAdapter);
     }
 
     public void createSubject(String currentSubjectName) {
         String subjectDirectory = baseDirectory.getAbsolutePath()
                 + File.separator + currentSubjectName;
         currentSubjectDirectory = new File(subjectDirectory);
 
         if (currentSubjectDirectory.exists()) {
             Toast.makeText(getApplicationContext(),
                     "Subject exits. Please pick a different name.",
                     Toast.LENGTH_SHORT).show();
             return;
         }
 
         if (currentSubjectDirectory.mkdirs()) {
             String message = "Subject " + currentSubjectName + " created!";
             Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                     .show();
         } else {
             Toast.makeText(getApplicationContext(),
                     "Error: Could not make subject directory!",
                     Toast.LENGTH_SHORT).show();
         }
     }
 
     public void takePhoto(View view) {
         // TODO: Check for interviewTitle (see above)
         Intent intent = new Intent(this, CameraSurface.class);
         startActivityForResult(intent, TAKE_PHOTO);
     }
 
     public void recordAudio(View view) {
         // TODO: Check for interviewTitle (see above)
         Intent intent = new Intent(this, Audio.class);
         startActivityForResult(intent, RECORD_AUDIO);
     }
 
     public void recordVideo(View view) {
         // TODO: Invoke video recording activity
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras;
         if (intent != null) {
             extras = intent.getExtras();
         }
         // Bundle extras = intent.getExtras();
         switch (requestCode) {
             case TAKE_PHOTO:
                 break;
             case RECORD_AUDIO:
                 break;
             case RECORD_VIDEO:
                 break;
         }
     }
 }
