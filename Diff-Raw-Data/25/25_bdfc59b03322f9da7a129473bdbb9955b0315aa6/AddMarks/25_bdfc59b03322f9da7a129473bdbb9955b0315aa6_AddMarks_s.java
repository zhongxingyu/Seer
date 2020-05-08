 package com.gpa.calculator;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.gpa.calculator.models.Marks;
 
 public class AddMarks extends Activity implements OnClickListener{
 	
 	TextView courseName;
 	EditText testName, scoredMarks, fromTotal, totalWeight;
 	Button saveMarks;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_marks);
 		
 		//Initialize Variables
 		init();
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null)
 			courseName.setText(extras.getString("CourseName"));
 		else
 			courseName.setText("Course name unavailable");
 		
 		//On Save Marks button click
 		saveMarks.setOnClickListener(this);
 	}
 
 	private void init() {
 		// TODO Auto-generated method stub
 		courseName = (TextView) findViewById(R.id.tvAddCourseMark);
 		testName = (EditText) findViewById(R.id.etTestName);
 		scoredMarks = (EditText) findViewById(R.id.etMarksScored);
 		fromTotal = (EditText) findViewById(R.id.etMarksScoredFrom);
 		totalWeight = (EditText) findViewById(R.id.etTotalWeightage);
 		saveMarks =(Button) findViewById(R.id.bSaveMark);
 	}
 
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
		Marks mark = new Marks();
 		
 		switch (v.getId()) {
 		case R.id.bSaveMark:
 			try{
 				//long result = 0;
			 	
 			 	mark.set_courseId(1);
 			 	mark.set_testName(testName.getText().toString());
 			 	mark.set_ScoredMarks(Float.parseFloat(scoredMarks.getText().toString()));
 			 	mark.set_fromTotal(Float.parseFloat(fromTotal.getText().toString()));
 			 	mark.set_totalWeight(Float.parseFloat(totalWeight.getText().toString()));
 			 	long feed = mark.insertMarks();
 			 	courseName.setText("Response: " + feed);
 			 	
 			 	
 			} catch (Exception e) {
 				// TODO: handle exception
 				courseName.setText(e.toString());
 			}
 			break;
 
 		default:
 			break;
 		}
 	}
 
 }
