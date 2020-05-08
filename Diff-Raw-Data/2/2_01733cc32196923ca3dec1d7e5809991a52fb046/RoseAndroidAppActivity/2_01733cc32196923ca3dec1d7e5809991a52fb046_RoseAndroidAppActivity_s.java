 package awesome.app;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class RoseAndroidAppActivity extends Activity implements OnClickListener {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         ((View)findViewById(R.id.schedule_lookup_button)).setOnClickListener(this);
         ((View)findViewById(R.id.student_lookup_button)).setOnClickListener(this);
         ((View)findViewById(R.id.ara_menu_button)).setOnClickListener(this);
         ((View)findViewById(R.id.help_button)).setOnClickListener(this);
         ((View)findViewById(R.id.feedback_button)).setOnClickListener(this);
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	public void onClick(View v) {
 		Intent myIntent = null;
 		switch(v.getId()){
 		case R.id.feedback_button:
 			myIntent = new Intent(this, InAppFeedback.class);
 			startActivity(myIntent);
 			break;
 		case R.id.student_lookup_button:
 			myIntent = new Intent(this, StudentLookupActivity.class);
 			startActivity(myIntent);
 			break;
 		case R.id.ara_menu_button:
 			Intent araIntent = new Intent(this, Ara.class);
 			startActivity(araIntent);
 			break;
 		case R.id.help_button:
			Intent helpIntent = new Intent(this, Ara.class);
 			startActivity(helpIntent);
 			break;
 		case R.id.schedule_lookup_button:
 			myIntent = new Intent(this, ScheduleLookupActivity.class);
 			startActivity(myIntent);
 			break;
 		}
 	}
 }	
