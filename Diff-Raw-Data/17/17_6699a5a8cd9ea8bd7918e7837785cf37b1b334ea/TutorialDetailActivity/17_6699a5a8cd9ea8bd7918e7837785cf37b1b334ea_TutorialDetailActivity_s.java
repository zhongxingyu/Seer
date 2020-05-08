 package com.jep.learning;
 
 import com.jep.learning.models.Chapter;
 import com.jep.learning.models.Tutorial;
 import com.jep.learning.models.User;
 import com.jep.learning.services.SessionService;
 import com.jep.learning.services.TutorialService;
 
 import android.os.Bundle;
import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 
 public class TutorialDetailActivity extends CommonActivity {
 
 	private int currentTutorial = 0;
 	private Chapter chapter;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tutorial_detail);
 		
 		String chapterName = getIntent().getStringExtra("chapter");
 		
 		TutorialService service = new TutorialService(getApplicationContext());
 		chapter = service.searchChapter(chapterName);
 		
 		displayTutorial();
 		
 		Button back = (Button) findViewById(R.id.btnBack);
 		Button next = (Button) findViewById(R.id.btnNext);
 		back.setOnClickListener(onBackClickLister);
 		next.setOnClickListener(onNextClickLister);
 			
 	}
 	
 	public void displayTutorial() {
 		Tutorial tutorial = chapter.getTutorials().get(currentTutorial);		
 		WebView tvBody = (WebView) findViewById(R.id.webView1);		
 		tvBody.loadData(tutorial.getBody(), "text/html", null);
 		
 		
 		SessionService session = new SessionService(prefs);
		if(currentTutorial == chapter.getTutorials().size()-1) {
			Log.e("app", "advancing user chapter to " + chapter.getId());
			User user = session.getCurrentUser();
 			user.setCurrentChapter(chapter.getId() + 1);
 			session.saveUserSession(user);
 		}
 	}
 	private OnClickListener onBackClickLister = new OnClickListener() {		
 		@Override
 		public void onClick(View v) {
 			if(currentTutorial-1 < 0) {
 				back();
 				return;
 			}
 			currentTutorial--;			
 			displayTutorial();
 			
 		}
 
 		
 	};
 	private OnClickListener onNextClickLister = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			if(currentTutorial+1 >= chapter.getTutorials().size()) {
 				return;
 			}
 			currentTutorial++;
 			displayTutorial();			
 		}
 	};
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.tutorial_detail, menu);
 		return true;
 	}
 
 }
