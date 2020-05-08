 /**
  * 
  */
 package ca.ualberta.cs.c301f13t13.gui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.widget.ListView;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 
 /**
  * @author joshuatate
  *
  */
 public class ViewAllChaptersActivity extends Activity {
 
 	private ListView chapters;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.activity_all_chapters);
 		chapters = (ListView) findViewById(R.id.allChaptersView);
 		
 		
 		Bundle bundle = this.getIntent().getExtras();
 		if (bundle.getBoolean("viewing")) {
 			//Just viewing chapters; not selecting for
 			//any purpose
			chapters.setOnClickListener(null);
 		} else {
 			//Selecting a chapter to add as a choice
			chapters.setOnClickListener(new OnClickListener() {
 				@Override
				public void onClick(View v) {
					//Add selected chapter as choice
 				}
 			});
 		}
 	}
 
 }
