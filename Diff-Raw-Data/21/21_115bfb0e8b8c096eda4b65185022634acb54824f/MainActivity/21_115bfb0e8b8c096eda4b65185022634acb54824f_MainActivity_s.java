 package edu.kaist.kse631.bmaingret_achin.mycoach;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class MainActivity extends BaseActivity {
 	private View startActivitySpecifics = null;
 	private View activityStartedSpecifics = null;
 	private long activityId = -1;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         /* Hiding the part used when an activity is going on */
         startActivitySpecifics = (View) findViewById(R.id.start_activity_layout);
         activityStartedSpecifics = (View) findViewById(R.id.activity_started_layout);
         activityStartedSpecifics.setVisibility(View.GONE);
         
         Button startActivity = (Button) findViewById(R.id.main_startButton);
         startActivity.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, NewActivityActivity.class);
 				startActivityForResult(intent, C.REQUEST_START_ACTIVITY);			
 			}
 		});
         
         Button historyButton = (Button) findViewById(R.id.main_historyButton);
         historyButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
 				startActivity(intent);	
 			}
 		});
         
         ImageButton stopActivity = (ImageButton) findViewById(R.id.main_stop_button);
         stopActivity.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				activityStartedSpecifics.setVisibility(View.GONE);
 				startActivitySpecifics.setVisibility(View.VISIBLE);
 				Toast toast = Toast.makeText(MainActivity.this, "TODO: Record activity", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		});
         
         Button recordActivity = (Button) findViewById(R.id.main_recordButton);
         recordActivity.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MainActivity.this, ActivityDetailsActivity.class);
 				startActivity(intent);	
 			}
 		});
     }
     
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	  if (requestCode == 1) {
     	     if(resultCode == RESULT_OK){       
     	    	 startActivitySpecifics.setVisibility(View.GONE);
     	    	 activityStartedSpecifics.setVisibility(View.VISIBLE);
     	    	 TextView activityText = (TextView) findViewById(R.id.main_ongoing_activity);
     	    	 String activity = data.getStringExtra("activity");
     	    	 activityText.setText(activityText.getText() + " " + activity);
     	     }
     	     if (resultCode == RESULT_CANCELED) {
     	     }
     	  }
     	}
 }
