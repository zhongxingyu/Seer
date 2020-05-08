 package uk.co.ipodling.newsblurb;
 /* 
  * application originally being developed for university. A reader application
  * for the news reader 'newsblur' which is open source
  * test account for testing
  * user: paptestaccount
  * password: soyoucanseethisworks
  * */
 
 import java.io.IOException;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.Intent;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.content.SharedPreferences;
 
 //log in activity
 //content retrieval
 //populate sidebar
 //display content
 //mark as read when content goes off top screen
 //fling to open/close sidebar
 
 public class MainActivity extends Activity implements Sidebar.Listener{
     private NewsblurbPreferences newsblurbPreferences;
 	private int distance = 100;
 	private int velocity = 150;
 	protected ListView sidebarList;
 	protected Sidebar sidebar;
     protected String[] mStrings = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         newsblurbPreferences = new NewsblurbPreferences(getApplicationContext());
 		requestWindowFeature(Window.FEATURE_NO_TITLE); //kinda not good way to do it if it takes a second to load but gets the job done
 		super.onCreate(savedInstanceState);
 		if(newsblurbPreferences.contains("user") && newsblurbPreferences.contains("pass")){
 			// log in with saved preferences
 		} else {
 			Intent i = new Intent(getApplicationContext(), Login.class);
 			startActivity(i);
 			// intent, move to log in activity
 		} 
 		setContentView(R.layout.activity_main);
 //		findViewById(R.layout.activity_main).setOnTouchListener;
 //		findViewById(R.layout.activity_main).setOnTouchListener(new MyTouchListener());
 		sidebar = (Sidebar) findViewById(R.id.animation_layout);
         sidebar.setListener(this);
         sidebarList   = (ListView) findViewById(R.id.sidebar_list);
         sidebarList.setAdapter(
                 new ArrayAdapter<String>(
                     this, android.R.layout.simple_list_item_multiple_choice
                     , mStrings));
         sidebarList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
     }
 	
 	 public void onClickContentButton(View v) {
 	        sidebar.toggleSidebar();
 	    }
 	 
 	 @Override
 	    public void onBackPressed() {
 	        if (sidebar.isOpening()) {
 	            sidebar.closeSidebar();
 	        } else {
 	            finish();
 	        }
 	    }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public void onSidebarOpened() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onSidebarClosed() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onContentTouchedWhenOpening() {
         sidebar.closeSidebar();
         return true;
 	}
 
 	private final GestureDetector gdt = new GestureDetector(new GestureListener());
 	  
 	  private class GestureListener extends SimpleOnGestureListener {
 	 
 	     @Override
 	     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 	        if(e1.getX() - e2.getX() > distance && Math.abs(velocityX) > velocity) {
 		        sidebar.toggleSidebar();
 	           // Right to left, your code here
 	           return true;
 	        } else if (e2.getX() - e1.getX() > distance && Math.abs(velocityX) >  velocity) {
 		        sidebar.toggleSidebar();
 
 	           // Left to right, your code here
 	           return true;
 	        }
 	        if(e1.getY() - e2.getY() > distance && Math.abs(velocityY) > velocity) {
 	           // Bottom to top, your code here
 	           return true;
 	        } else if (e2.getY() - e1.getY() > distance && Math.abs(velocityY) > velocity) {
 	           // Top to bottom, your code here
 	           return true;
 	        }
 	        return false;
 	     }
 	  }
 	  
 	  private final class MyTouchListener implements OnTouchListener {
 		  @Override
 	        public boolean onTouch(final View view, final MotionEvent event) {
 	           return gdt.onTouchEvent(event);
 	        }
 	     };
 
 }
