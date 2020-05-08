 /**
  * 
  */
 package ca.ualberta.cs.c301f13t13.gui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
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
			chapters.setOnItemClickListener(null);
 		} else {
 			//Selecting a chapter to add as a choice
			chapters.setOnItemClickListener(new OnItemClickListener() {
 				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					
 				}
 			});
 		}
 	}
 
 }
