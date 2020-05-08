 /**
  * 
  */
 package story.book;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * StoryFragmentListActivity displays all story fragments contained
  * in the story that is currently open.
  * 
 * @author jsurya
  *
  */
 public class StoryFragmentListActivity extends Activity {
 	ActionBar actionBar;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.story_fragment_read_activity);
 		actionBar = getActionBar();
 		actionBar.setTitle(R.string.StoryTitle);
 		
 	}
 }
