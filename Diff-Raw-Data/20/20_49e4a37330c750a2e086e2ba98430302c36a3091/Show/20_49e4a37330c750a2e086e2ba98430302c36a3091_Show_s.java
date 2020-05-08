 package com.example.voter;
 
import com.example.voter.Launch;
 import com.example.voter.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
 import android.widget.TextView;
 
 public class Show extends Activity{
 	
 	private TextView titleTextView;
 	private TextView descriptionTextView;
	private TextView addidTextView;
 
 	public void onCreate(Bundle saveInstanceState){
 		Bundle savedInstanceState = null;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showvote);
 		
 		Intent intent = getIntent();
 
 		VoteClass new_vote = (VoteClass)intent.getSerializableExtra("data");
 		
 //		Bundle bundle = this.getIntent().getExtras();
 //		final String title= bundle.getString("title");
 //		final String description = bundle.getString("description");
 //		final String addid = bundle.getString("addid");
 //		
 		titleTextView = (TextView)findViewById(R.id.title);
 		descriptionTextView = (TextView)findViewById(R.id.describe);
		addidTextView = (TextView)findViewById(R.id.launcher);

 		titleTextView.setText("Ļǣ"+new_vote.getTopic());
		descriptionTextView.setText(":\n"+new_vote.getDescription());
 
 	}
 	
 }
