 /*
 FragmentList Class for CreateYourOwnAdventure App.
 ReadStoryManager is the manager that allows interaction between the user
 and the view. It dictates what content is to be displayed by the view. It
 keeps track of the history stack as well when read navigates to various
 other story fragments.
     
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
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 /**
  * @author Eddie Tai <eddie@ualberta.ca>
  * 
  * ReadStoryManager is the manager that allows interaction between the user
  * and the view. It dictates what content is to be displayed by the view. It
  * keeps track of the history stack as well when read navigates to various
  * other story fragments.
  * 
  */
 public class ReadStoryManager implements OnItemClickListener {
 
 	// declaration of variables
 	ReadFragmentView view ;
 	StoryFragment fragment;
 	Integer fragmentId;
 	Context context;
 	Story story;
 	Integer storyId;
 	ArrayList<Choice> choices;
 	History history;
 	
 
 	public ReadStoryManager(final Integer storyId, final Integer fragmentId,
 			final ReadFragmentView view, final ReadFragmentActivity context) {
 		
 		// setting of variables
 		this.view = view;
 		this.context = context;
 		this.storyId = storyId;
 		this.fragmentId = fragmentId;
 
 		// set the story using storyId through global manager
 		GlobalManager globalmanager = (GlobalManager) ((Activity) context)
 				.getApplication();
 		globalmanager.setStoryManagers(storyId);
 
 		// fetch the fragment from the story level
 		fragment = story.getFragment(fragmentId);
 
 		// get media_list
 		ArrayList<Media> media_list = fragment.getContentList();
 
 		// cycle through the media list
 		for (int i = 0; i < media_list.size(); i++) {
 
 			// get media file
 			Media media = media_list.get(i);
 
 			// get media's type
 			Class media_type = media.getClass();
 			if (media_type == Text.class.getClass()) {
 
 				// get media content's string as s
 				String s = media.toString();
 				view.setTextView(s);
 			}
 
 			// the rest are implemented later for iteration 2
 
 			// else if type is image
 			// if(media_type == Image.class.getClass())
 
 			// else if type is sound
 			// if(media_type == Audio.class.getClass())
 
 			// else if type is video
 			// if(media_type == Video.class.getClass())
 		}
 
 		// from story level with fragment id, get the choice map
 		ChoiceMap choiceMap = story.getChoiceMap();
 
 		// from choice map, use fragment id to get arraylist of choice object
 		choices = choiceMap.getChoices(fragmentId);
 
 		// if there are choices, cycle through them and extract the flavour
 		// texts
 		if (choices != null) {
 			ArrayList<String> flavourText = new ArrayList<String>();
 
 			for (int i = 0; i < choices.size(); i++) {
 				String s = choices.get(i).getFlavourText();
 				flavourText.add(s);
 			}
 
 			// set the view of choices with flavour text
 			view.setChoiceView(flavourText, this);
 		}
 
 	}
 
 	/**
 	 * Go to the beginning (first page) of a story
 	 * Apprehend the current page to the history stack
	 * History is cleared.
 	 */
 	public void toBeginning() {
 
 		// get the fragment id of the story's first page
 		Integer destinationId = story.getFirstPage();
		story.clearHistory();
 
 		// read the next story fragment
 		readNextFragment(storyId, destinationId);
 	}
 
 	/**
 	 * Go back to the previous page dictated by the history stack
 	 * Remove the current page from the history stack
 	 */
 	public void toPrevious() {
 
		// go back to previous, adjusting history stack properly
		Integer destinationId = story.getMostRecent();
 
 		// read the next story fragment
 		readNextFragment(storyId, destinationId);
 	}
 
 	/**
 	 * Used by the global manager to inform the ReadStoryManager of what
 	 * story is to be read
 	 * @param story the story that is to be read
 	 */
 	public void setStory(Story story) {
 		this.story = story;
 	}
 
 	/**
 	 * On selection of a choice in view, direct the user to the next story
 	 * fragment according to the choice map.
 	 */
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		
 		// use position to refer to the choice item from choice map
 		Choice choice = choices.get(position);
 		
 		Integer destinationId = choice.getDestinationId();
 
 		// add to the history stack
 		history.pushToStack(destinationId);
 
 		// read the next story fragment
 		readNextFragment(storyId, destinationId);
 
 	}
 
 	/**
 	 * A function that starts a new activity 
 	 * @param storyId
 	 *            the Id of the story object that the user is reading at the
 	 *            moment
 	 * @param fragmentId
 	 *            the Id of the story fragment that the user is to read next
 	 */
 	public void readNextFragment(Integer storyId, Integer fragmentId) {
 		Intent intent = new Intent(context, ReadFragmentActivity.class);
 		intent.putExtra("storyId", storyId);
 		intent.putExtra("fragmentId", fragmentId);
 		((Activity) context).startActivity(intent);
 	}
 
 }
