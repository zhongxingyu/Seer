 package com.garethmurphy.dcutimetable;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 
 
 public class DcuTimetableActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         final EditText classCode = (EditText)findViewById(R.id.editText1);
         final EditText classYear = (EditText)findViewById(R.id.editText2);
         final RadioButton sem1 = (RadioButton)findViewById(R.id.radio0);
         final Button btn = (Button)findViewById(R.id.button1);
         
         btn.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				if (classCode.length() == 0 || classYear.length() == 0) return;
 				
 				String url = "http://www.dcu.ie/timetables/feed.php3?hour=1-28&template=student";
 				
 				// Add the class code to the URL.
 				url += "&prog=" + classCode.getText();
 				
 				// The year of study.
 				url += "&per=" + classYear.getText();
 				
 				if (sem1.isChecked()) {
 					url += "&week1=1&week2=12";
 				} else {
					url += "&week1=13&week2=24";
 				}
 				
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(url));
 				startActivity(i);
 			}
 		});
     }
 }
