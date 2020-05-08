 package edu.pitt.cs1635group3.Activities;
 
 import edu.pitt.cs1635group3.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.util.Log;
 import android.view.View;
 
 public class HomeActivity extends Activity {
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dashboard);
        Log.d("GOT", "TO THIS POINT OK");
         
     }
     public void myLists(View v) {
 
 		Intent intent = new Intent(HomeActivity.this,
 				SociaListActivity.class);
 		startActivityForResult(intent, 0);
 
 	}
 }
