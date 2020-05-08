 package ualberta.g12.adventurecreator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.support.v4.app.NavUtils;
 
 public class EditChoiceActivity extends Activity {
 	private int ourFragmentId;
 	private int ourStoryId;
 	private Story ourStory;
 	private List<Fragment> ourFragmentList;
 	private int userPicked;
 	private List<String> possibleChoices;
 	private Fragment linked;
 	private int requestKey;
 	private int storyPos,fragPos ,choicePos;
 	private StoryList sl ;
 	private Fragment fragment;
 	private OfflineIOHelper offlineHelper = new OfflineIOHelper(EditChoiceActivity.this);
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_choice);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		///stufsf;jasdf
 		
 			 Intent editChoiceIntent = getIntent();
 			 storyPos  = (Integer)editChoiceIntent.getSerializableExtra("StoryPos");
 			 fragPos = (Integer)editChoiceIntent.getSerializableExtra("FragmentPos"); 
 			 choicePos = (Integer)editChoiceIntent.getSerializableExtra("ChoicePos"); 
 /*
 			 //load save file
 			        storyList = offlineHelper.loadOfflineStories();
 			        story = storyList.getAllStories().get(storyPos);
 			        fragment = story.getFragments().get(fragPos);
 			        Choice choice fragment.getChoices().get(choicePos);
 		 
 			*/
 		Intent i = getIntent();
 		//Bundle extras = i.getExtras();
 		//ourStoryId = extras.getInt("OurStoryId");
 		//ourFragmentId = extras.getInt("OurFragmentId");
 		Button choiceButton  = (Button) findViewById(R.id.choiceButton);
 		//StoryList sl = AdventureCreatorApplication.getStoryList();
 		sl  = offlineHelper.loadOfflineStories();
 		ourStory = sl.getAllStories().get(storyPos);
 		//Log.d("WHAT IS OUR STORY SIZE?", ourStory.getStoryTitle());
 		ourFragmentList = ourStory.getFragments();
 		fragment = ourFragmentList.get(fragPos);
 		Log.d("HURR DURR", "HEER HERR");
 		Log.d("WHAT AM BEFORE", String.format("%d",ourFragmentList.size() ));
 		choiceButton.setOnClickListener(new OnClickListener() {
 		  @Override
           public void onClick(View v) {
 			  dialogz();
 
 			  
           }
       }
 				
 				
 				);
 		
 	}
 	
 	//http://stackoverflow.com/questions/4473940/android-best-practice-returning-values-from-a-dialog
 	private void dialogz(){
 		  AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		  builder.setTitle("Pick a Fragment to link {NONE} means end");
 		  possibleChoices = getFragmentTitleList(ourFragmentList);
 		  CharSequence[] chars = possibleChoices.toArray(new CharSequence[possibleChoices.size()]);
 		  
 		  builder.setItems(chars, new DialogInterface.OnClickListener() {
 		      public void onClick(DialogInterface dialog, int item) {
 		    	  doneSelecting(item);
 		      }
 		  });
 		  AlertDialog alert = builder.create();
 		  alert.show();
 		  Log.d("Morning", "HI");
 		  
 	}
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		//getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 	
 	
 	
 
 	private void doneSelecting(int result){
		if (result!=possibleChoices.size()){
 			linked = ourFragmentList.get(result);
 		}
 	}
 	
 	private List<String> getFragmentTitleList(List<Fragment> fragList){
 		List<String> temp = new ArrayList<String>();
 		Log.d("PSADF", String.format("%d",fragList.size() ));
 		for(int i = 0; i < fragList.size() ; i++){
 			temp.add( fragList.get(i).getTitle());
 		}
 		temp.add("{NONE}");
 		return temp;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//TODO make own 
 		getMenuInflater().inflate(R.menu.story_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		/*
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 		*/
 		
 		//Fragment frag = null; 
 		// TODO SEARCH FOR FRAGMENT BY FRAGMENT ID?
 		//for (int i = 0; i < ourFragmentList.size(); i++){
 		//	if (ourFragmentList.get(i).getId() == ourFragmentId)
 		//		frag = ourFragmentList.get(i);
 		//}
 		//Log.d("WHAT AM BEFORE", String.format("%d",frag.getChoices().size() ));
 		//Log.d("WHAT?!", frag.getChoices().get(0).getChoiceText());
 		//TODO CHANGE THIS AND REFACTOR ALL CODE IN CLASS
 		FragmentController fc = new FragmentController();
 		EditText myTitleET = (EditText)findViewById(R.id.choiceBody);
 		String Title = myTitleET.getText().toString();
		Choice newChoice = new Choice();
 		// TODO needs to be checked;need controller
		newChoice.setChoiceText(Title);
		newChoice.setLinkedToFragment(linked);
		fc.addChoice(fragment, newChoice);
 		//sl.getAllStories().get(storyPos).getFragments().set(fragPos, fragment);
 		int i = fragment.getChoices().size();
 		Log.d("DID IT GROW?", String.format("%d", i));
 		finish();
 		return true;
 	}
 
 }
