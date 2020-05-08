 package com.johnjohnbear.easystudy;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class StudyActivity extends Activity {
 	private ArrayList<String> studyContent = new ArrayList<String>();
 	private int current=0;
 	
 	public StudyActivity() {
 		studyContent.add("学");
 		studyContent.add("习");
 		studyContent.add("起");
 		studyContent.add("来");
 		studyContent.add("很");
 		studyContent.add("简");
 		studyContent.add("单");
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.layout_study_activity);
 		
 		updateWhiteBoardContent(studyContent.get(current));
 		
 	}
 	
 	private void updateWhiteBoardContent(String content)
 	{
 		TextView textView=(TextView)findViewById(R.id.whiteboard);
 		textView.setText(content);
 	}
 	
 	public void onClickGotIt(View view) {
 		navigateToNextItem();
 		
 	}
 	
 	public void onClickNoIdea(View view) {
 		navigateToNextItem();
 		
 	}
 
 	private void navigateToNextItem() {
 		current++;
 		if(endOfContent())
 		{
			updateWhiteBoardContent(getString(R.string.accomplish_string));		
 		}
 		else
 		{
 
 			updateWhiteBoardContent(studyContent.get(current));
 		}
 	}
 	
 	
 
 	private boolean endOfContent() {
 		return current >= studyContent.size();
 	}
 	
 
 }
