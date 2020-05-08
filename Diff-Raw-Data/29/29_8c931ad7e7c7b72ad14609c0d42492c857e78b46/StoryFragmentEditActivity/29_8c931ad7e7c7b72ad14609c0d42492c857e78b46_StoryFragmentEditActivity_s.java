 /**
  * 
  */
 package story.book;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.ActionBar.LayoutParams;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /**
  * StoryFragmentEditActivity is the interface users can make changes
  * to illustrations contained in the story fragment which is currently
  * open. All text illustrations displayed as dynamically editable EditTexts.
  * 
  * @author Jessica Surya
  *
  */
 @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 public class StoryFragmentEditActivity extends FragmentActivity implements StoryView<StoryFragment> {
 	ActionBar actionBar;
 
 	StoryFragment SF;
 	FragmentCreationController FCC;
 	ArrayList<StoryFragment> SFL;
 	ArrayAdapter<StoryFragment> adapter;
 	StoryCreationController SCC;
 	StoryApplication SA;
 
 	ArrayList<Illustration> illustrations;
 	ArrayList<DecisionBranch> decisions;
 	ArrayList<Button> buttons;
 
 	//storyFragment passed as intent from FragmentList
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.reading_fragment);
 		savedInstanceState = getIntent().getExtras();
 		int FID = savedInstanceState.getInt("FID");
 
 		SCC = new StoryCreationController();
 		SFL = new ArrayList<StoryFragment>();
 
 		HashMap<Integer, StoryFragment> map = SCC.getFragments();
 		for (Integer key : map.keySet()){
 			SFL.add(map.get(key));
 		}
 
 		SF = SFL.get(FID);
 
 		FCC = new FragmentCreationController(SF);
 		String title = SF.getFragmentTitle();
 
 		actionBar = getActionBar();
 		actionBar.setTitle(title);
 
 		SF.addView(this);
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		displayFragment();
 	}
 
 	@Override
 	public void onPause(){
 		super.onPause();
 	}
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		displayFragment();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu items for use in the action bar
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.add_illustration_menu, menu);
 
 		inflater.inflate(R.menu.standard_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.text:
 			// TODO: Pass illustration to fragment for editing
 
 			return true;
 
 		case R.id.take_photo:
 
 			return true;
 		case R.id.addGalleryPhoto:
 
 			return true;
 		case R.id.addDecisionBranch:
 
 			return true;
 
 		case R.id.audio:
 
 			return true;
 
 		case R.id.video:
 
 			return true;
 
 		case R.id.record_video:
 
 			return true;
 		case R.id.title_activity_dashboard:
 			Intent intent = new Intent(this, Dashboard.class);
 			startActivity(intent);
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	/**
 	 * formatView() (FOR TEXTVIEWS ONLY) formats illustration textViews in an array list
 	 * by changing:
 	 * 		- text size (20)
 	 * 		- text color (black)
 	 * 		- padding on the left side
 	 * 
 	 * @param v
 	 * 
 	 */
 	private void formatView(ArrayList<View> v) {
 		Iterator<View> viewIterator = v.iterator();
 		EditText x = null;
 		while(viewIterator.hasNext()) {
 			x = (EditText) viewIterator.next();
 			x.setTextSize(20);
 			x.setTextColor(Color.BLACK);
 			x.setPaddingRelative(5, 0, 0, 0);
 		}
 	}
 	/**
 	 * formatButton() creates a button with the corresponding decision branch text
 	 * for each decision branch in an array list of decision branches.
 	 * 
 	 * This returns method an array list of buttons.
 	 * 
 	 * @param db
 	 * @param c
 	 * @return ArrayList<Button>
 	 */
 	private ArrayList<Button> formatButton(ArrayList<DecisionBranch> db, Context c) {
 
 		ArrayList<Button> buttonList = new ArrayList<Button>();
 
 		Iterator<DecisionBranch> dbIterator = db.iterator();
 		DecisionBranch d = null;
 		Button button;
 		while(dbIterator.hasNext()) {
 
 			d = dbIterator.next();
 			button = new Button(c);
 			button.setText(d.getDecisionText());
 			buttonList.add(button);
 		}
 
 		return buttonList;
 	}
 
 	@Override
 	public void update(StoryFragment model) {
 		// TODO Auto-generated method stub
 		//display fragment contents
 		displayFragment();
 	}
 
 	/**
 	 * displayFragments() displays all text illustrations as views 
 	 * and decision branches as buttons by getting them from the containing 
 	 * fragment and formatting them by calling <code>formatView</code>
 	 * and <code>formatButton</code> respectively.
 	 * 
 	 */
 	private void displayFragment() {
 		illustrations = SF.getIllustrations();
 		decisions = SF.getDecisionBranches();
 
 		ArrayList<View> illustrationViews = new ArrayList<View>();
 
 		for (Illustration i : illustrations){
 			illustrationViews.add(((TextIllustration)i).getEditView());
 		}

 		formatView(illustrationViews);
		LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
		for (View t: illustrationViews) {
 			this.addContentView(t, p);
 		}
 
 		buttons = formatButton(decisions, this);
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
 				LayoutParams.WRAP_CONTENT);

		//TODO: Fix the overlap of illustrations
		lp.addRule(RelativeLayout.ALIGN_BOTTOM);
 		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 
 		for (Button dbButton : buttons) {
 			this.addContentView(dbButton, lp);
 		}
 	}
 }
