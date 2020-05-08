 package edu.fsu.cs.group5socialnetwork;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ProfileOptions extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile_options);
 
 		TextView username = (TextView) findViewById(R.id.usernameTV);
 		username.setText(MainActivity.mUserName.getText());
 
 		ListView mLV = (ListView) findViewById(R.id.listView1);
 
 		// event handler for when you click on a listView item
 		mLV.setOnItemClickListener(new OnItemClickListener(){
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent myIntent;
 				switch( position ) 
 				{
 				case 0:  
 					// High scores
 					/*myIntent = new Intent(getApplicationContext(), Profile.class);
 					myIntent.putExtra("quizType","profileQuiz");
 					startActivity(myIntent);*/
 					myIntent = new Intent(getApplicationContext(), HighScores.class);
 					startActivity(myIntent);
 					break;
 				case 1:  
 					myIntent = new Intent(getApplicationContext(), Profile.class);
 					myIntent.putExtra("quizType","profileQuiz");
 					startActivity(myIntent);
 					break;
 				case 2:
 
 					break;
 				case 3:  
 
 					break;
 				}
 			}}); 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_profile_options, menu);
 		return true;
 	}
 }
