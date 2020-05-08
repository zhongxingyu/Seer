 package com.placella.socialconnections;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.*;
 import android.webkit.*;
 import android.widget.Button;
 
 public class Activity_LecturerMenu extends Activity {
 	private Activity self = this;
 	public static final int WEB_REQUEST = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_lecturer_menu);
         
         final String token = getIntent().getStringExtra("token");
         Button button;
 
         button = (Button) this.findViewById(R.id.takeAttendanceBtn);
         button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent i = new Intent(self, Activity_TakeAttendance.class);
 				i.putExtra("token", token);
	    		startActivity(i);
 			}	
 		});
         button = (Button) findViewById(R.id.manageAssessmentsBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "manageAssessment", token);
 			}
         });
         button = (Button) findViewById(R.id.twitterBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "twitter", token);
 			}
         });
         button = (Button) findViewById(R.id.viewStudentAttendanceBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "viewStudentAttendance", token);
 			}
         });
         button = (Button) findViewById(R.id.postNotesBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "postNotes", token);
 			}
         });
         button = (Button) findViewById(R.id.logOutBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 			    CookieSyncManager.createInstance(self);
 			    CookieManager.getInstance().removeAllCookie();
 			    finish();
 			}
         });
         button = (Button) findViewById(R.id.manageStudentsBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "manageStudents", token);
 			}
         });
         button = (Button) findViewById(R.id.manageGroupsBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "manageGroups", token);
 			}
         });
         button = (Button) findViewById(R.id.viewExcusesBtn);
         button.setOnClickListener(new Button.OnClickListener () {
 			@Override
 			public void onClick(View arg0) {
 				Activity_Web.launch(self, "viewExcuses", token);
 			}
         });
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) 
     {  
     	if (requestCode == WEB_REQUEST && resultCode == 0) {
     		finish();
     	}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_lecturer_menu, menu);
         return true;
     }
 }
