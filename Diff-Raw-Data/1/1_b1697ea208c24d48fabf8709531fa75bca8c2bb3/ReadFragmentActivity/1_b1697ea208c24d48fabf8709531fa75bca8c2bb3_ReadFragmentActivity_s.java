 /*
 FragmentList Class for CreateYourOwnAdventure App.
 ReadFragmentActivity, the activity called for reading any story
 fragment. Relies on ReadFragmentView for the display and
 ReadStoryManager to control the content and interaction.
     
     License GPLv3: GNU GPL Version 3
     <http://gnu.org/licenses/gpl.html>.
     
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cmput301.f13t01.createyourownadventure;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * ReadFragmentActivity, the activity called for reading any story fragment.
  * Relies on ReadFragmentView for the display and ReadStoryManager for story
  * access.
  * 
  * @author Eddie Tai <eddie@ualberta.ca>
  */
 public class ReadFragmentActivity extends FragmentActivity {
 
 	// declaration of variables
 	//FragmentManager fragmentManager;
 	ReadStoryManager storyManager;
 	UUID storyId;
 
 	/**
 	 * Called when the activity is first created. Receives intent from main
 	 * activity to create the first fragment.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		if (savedInstanceState == null) {
 			super.onCreate(savedInstanceState);
 			GlobalManager app = (GlobalManager) getApplication();
 
 			setContentView(R.layout.activity_view_fragment);
 
 			// enable the Up button in action bar
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 
 			// intent has the story ID, and story fragment ID to display
 			Intent intent = getIntent();
 			// receive id of story fragment to show
 			storyId = (UUID) intent.getSerializableExtra(getResources()
 					.getString(R.string.story_id));
 
 			// set the story in the story manager
 			app.setStoryManager(storyId);
 			this.storyManager = app.getStoryManager();
 
 			// depending if we are reading from beginning,
 			// fetch the appropriate fragment ID accordingly
 			Integer fragmentId;
 			Boolean fromBeginning = (boolean) intent.getBooleanExtra(
 					getResources().getString(R.string.story_continue), false);
 			if (fromBeginning) {
 				fragmentId = storyManager.getFirstPageId();
				// fragmentId = 0;
 			} else {
 				// show first page if the story has never been read
 				fragmentId = storyManager.getMostRecent();
 				if (fragmentId.equals(null)) {
 					fragmentId = storyManager.getFirstPageId();
 				}
 			}
 
 			// if there is no first page set, exit readmode
 			if (fragmentId.equals(null)) {
 				Toast.makeText(getBaseContext(),
 						"First page has not been set for this story!",
 						Toast.LENGTH_LONG).show();
 				finish();
 			}
 
 			commitFragment(fragmentId);
 		}
 	}
 
 	/**
 	 * places menu in action bar as specified
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.read_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * Determines the resulting action of choosing a particular action in the
 	 * action bar.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.action_return_to_beginning:
 			// Go back to the start of the story, clearing history stack
 			toBeginning();
 			return true;
 		case R.id.action_return_to_previous_page:
 			// Go back to the previous story fragment according to history stack
 			toPrevious();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Go to the beginning (first page) of a story. Apprehend the current page
 	 * to the history stack History is cleared.
 	 */
 	public void toBeginning() {
 
 		// get the fragment id of the story's first page
 		Integer destinationId = storyManager.getFirstPageId();
 		storyManager.clearHistory();
 
 		// read the next story fragment
 		commitFragment(destinationId);
 	}
 
 	/**
 	 * Go back to the previous page dictated by the history stack. Remove the
 	 * current page from the history stack
 	 */
 	public void toPrevious() {
 
 		// go back to previous, adjusting history stack properly
 		Integer destinationId = storyManager.getMostRecent();
 
 		if (destinationId != null) {
 			// read the next story fragment if there is a previous fragment in
 			// the history stack
 			commitFragment(destinationId);
 		} else {
 			// go back to the previous level
 			finish();
 		}
 	}
 
 	/**
 	 * Sets the functionality of the clicklistener for the choice buttons. This
 	 * function fetches the destination ID for a chosen choice, and generates a
 	 * new fragment accordingly.
 	 * 
 	 * @param v
 	 *            the button that is pressed
 	 * @param fragmentId
 	 *            the id of the fragment to be displayed next
 	 */
 	public void onFragmentListClick(View v, Integer fragmentId) {
 		// TODO Auto-generated method stub
 
 		// fetch the destinationId of the next fragment to show
 		int selectedChoice = v.getId() - 1;
 
 		ArrayList<Choice> choiceList = storyManager.getChoices(fragmentId);
 		Choice choice = choiceList.get(selectedChoice);
 		Integer destinationId = choice.getDestinationId();
 
 		// generate new fragment to replace the old one
 		commitFragment(destinationId);
 	}
 
 	/**
 	 * Using a fragment manager, this function creates and commits a new
 	 * ReadFragmentView fragment to be displayed for this view. This replaces
 	 * whatever the old fragment is, thus giving the readers a new view on the
 	 * new content.
 	 * 
 	 * @param storyId
 	 *            the UUID of the story
 	 * @param fragmentId
 	 *            the story fragment to be displayed
 	 */
 	public void commitFragment(Integer fragmentId) {
 		// prepare for the fragment
 		FragmentManager fragmentManager = getFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager
 				.beginTransaction();
 
 		// prepare bundle to pass arguments
 		Bundle bundle = new Bundle();
 		bundle.putInt(getResources().getString(R.string.destination_id),
 				fragmentId);
 
 		// create the fragment and pass the bundle to the fragment
 		ReadFragmentView newFragment = new ReadFragmentView();
 		newFragment.setArguments(bundle);
 		fragmentTransaction.replace(R.id.read_fragment_activity, newFragment);
 		fragmentTransaction.commit();
 	}
 }
