 package de.gymbuetz.gsgbapp;
 
import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
public class RepresentationActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_representation);
 
 		String teacher = (String) getIntent().getExtras().get("teacher");
 		String subject = (String) getIntent().getExtras().get("subject");
 		String subject_ori = (String) getIntent().getExtras().get("subject_ori");
 		String room = (String) getIntent().getExtras().get("room");
 		String more = (String) getIntent().getExtras().get("more");
 		String lesson = (String) getIntent().getExtras().get("lesson");
 		String clas = (String) getIntent().getExtras().get("class");
 		String date = (String) getIntent().getExtras().get("date");
 
 		this.setTitle(date + " - " + clas + " - " + lesson + ". " + getResources().getString(R.string.lesson));
 
 		// TextView tv = (TextView)findViewById(R.id.text_lesson);
 		// tv.setText(lesson);
 		TextView tv = (TextView) findViewById(R.id.text_room);
 		tv.setText(room);
 		tv = (TextView) findViewById(R.id.text_teacher);
 		tv.setText(teacher);
 		tv = (TextView) findViewById(R.id.text_subject);
 		tv.setText(subject);
 		tv = (TextView) findViewById(R.id.text_subject_ori);
 		tv.setText(subject_ori);
 		tv = (TextView) findViewById(R.id.text_more);
 		tv.setText(more);
 		// tv = (TextView)findViewById(R.id.text_class);
 		// tv.setText(clas);
 	}
 }
