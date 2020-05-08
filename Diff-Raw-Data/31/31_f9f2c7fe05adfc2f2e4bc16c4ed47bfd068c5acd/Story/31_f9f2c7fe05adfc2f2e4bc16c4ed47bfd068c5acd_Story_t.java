 /*
 Story Class for CreateYourOwnAdventure App.
 Aggregates functionality and data for a single story.
 
 License GPLv3: GNU GPL Version 3
 <http://gnu.org/licenses/gpl.html>.
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package cmput301.f13t01.createyourownadventure;
 
 import java.io.IOException;
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
 * Story object, basic functional unit for a story.
 * Has title, author, description, and first page attributes.
 * Aggregates History, FragmentList, and ChoiceMap objects.
 * - History tracks reader progress
 * - FragmentList is the container for the Fragments (Pages) of the story
 * - ChoiceMap stores the choices and associates for the Fragments
 * 
 * @author Jesse Chu <jhchu@ualberta.ca>
 */
 
 public class Story implements Serializable {
 
         /* Instance Variables for a Story */
         // Story Attributes
         private String title;
         private String author;
         private String description;
         private Integer firstPage;
         // Aggregated objects for functionality
         private History historyStack;
         private StoryFragmentList fragmentList;
         private ChoiceMap choiceMap;
 
         
         /**
          * Constructor. Creates a new instance of a Story.
          * Created with default values assigned to attributes.
          */
         public Story() {
             this.title = "New Story";
             this.author = "";
             this.description = "A new Story";
             this.firstPage = null;
             this.historyStack = new History();
             this.fragmentList = new StoryFragmentList();
             this.choiceMap = new ChoiceMap();
         }
 
         
         /* Functions that deal with Story attributes */
         /**
          * Getter for story title.
          *
          * @return the title of the story
          */
         public String getTitle() {
             return this.title;
         }
         
         /**
          * Getter for story author.
          *
          * @return the author of the story
          */
         public String getAuthor() {
             return this.author;
         }
         
         /**
          * Getter for the description of the story.
          *
          * @return the description of the story
          */
         public String getDescription() {
             return this.description;
         }
         
         /**
          * Getter for the first fragment of the story.
          *
          * @return the ID of the first fragment of the story
          */
         public Integer getFirstPage() {
             return this.firstPage;
         }
         
         /**
          * Setter for the title of the story.
          *
          * @param title the string to set the title to
          * @return true if successful, false otherwise
          */
         public boolean setTitle(String title) {
             this.title = title;
             return true;
         }
         
         /**
          * Setter for the author of the story.
          *
          * @param author the string to set the author to
          * @return true if successful, false otherwise
          */
         public boolean setAuthor(String author) {
             this.author = author;
             return true;
         }
         
         /**
          * Setter for the description of the story.
          *
          * @param description the string to set the description to
          * @return true if successful, false otherwise
          */
         public boolean setDescription(String description) {
             this.description = description;
             return true;
         }
         
         /**
          * Setter for the first page of the story.
          *
          * @param id the id of the fragment to set as first page
          * @return true if successful, false otherwise
          */
         public boolean setFirstPage(Integer id) {
 	        StoryFragment checkFragment = fragmentList.getFragment(id);
 	        if (checkFragment != null) {
                 this.firstPage = id;
                 return true;
 	        } else {
                 return false;
 	        }
         }
 
         
         /* Functions that deal with the StoryFragmentList */
         /**
          * Fetches a requested StoryFragment by ID from FragmentList
          *
          * @param id the id of the StoryFragment to return
          * @return the requested StoryFragment, null if it doesn't exist
          */
         public StoryFragment getFragment(Integer id) {
             return this.fragmentList.getFragment(id);
         }
         
         /**
          * Returns the ID of a given StoryFragment.
          *
          * @param fragment the StoryFragment to get the ID of
          * @return the ID of the StoryFragment, null if not found
          */
         public Integer getFragmentId(StoryFragment fragment) {
         	return this.fragmentList.getFragmentId(fragment);
         }
         
         /**
          * Returns a StoryFragmentInfo object for the given ID.
          * Used to display StoryFragmentInfo in lists.
          *
          * @param id the ID of the fragment to get the info of
          * @return a StoryFragmentInfo object for that ID
          */
         public StoryFragmentInfo getFragmentInfo(Integer id) {
         	return this.fragmentList.getFragmentInfo(id);
         }
         
     	/**
     	 * Returns an ArrayList of StoryFragmentInfo objects for
     	 * all StoryFragment objects.
     	 * 
     	 * @return an ArrayList of all StoryFragmentInfo
     	 */
         public ArrayList<StoryFragmentInfo> getFragmentInfoList() {
         	return this.fragmentList.getFragmentInfoList();
         }
 
     	/**
     	 * Places a new fragment into the FragmentList.
     	 * A new ID is automatically generated for each fragment.
     	 * 
     	 * @param newFragment The StoryFragment to add to StoryFragmentList
     	 * @return the ID given to the added StoryFragment
     	 */
         public Integer addFragment(StoryFragment newFragment) {
         	return this.fragmentList.addFragment(newFragment);
         }
 
     	/**
     	 * Removes a requested Fragment from the FragmentList.
    	 * If the requested Fragment is the first page of the story,
    	 * the first page of the story is now a Null.
     	 * Returns boolean based on success/failure.
     	 * 
     	 * @param id ID of the Fragment to remove
     	 * @return true if successful, false otherwise
     	 */
         public boolean removeFragment(Integer id) {
        	if (id == this.firstPage) {
        		this.firstPage = null;
        	}
         	return this.fragmentList.removeFragment(id);
         }
         
     	/**
     	 * Updates the Fragment associated with a given ID.
     	 * Fragment ID must have been in use to be valid
     	 * 
     	 * @param id ID of the Fragment in to updated
     	 * @param fragment Fragment to update to
     	 * @returns true is successful, false otherwise
     	 */
     	public boolean updateFragment(Integer id, StoryFragment fragment) {
     		return this.fragmentList.updateFragment(id, fragment);
     	}
 
 
         /* Functions that deal with the ChoiceMap */
     	/**
     	 * Adds a new given Choice to a StoryFragment (by ID)
     	 * 
     	 * @param fragmentId ID of StoryFragment to add Choice to
     	 * @param choice Choice object to add to the ChoiceMap
     	 */
     	public void addChoice(int fragmentId, Choice choice) {
     		this.choiceMap.addChoice(fragmentId, choice);
     	}
     	
     	/**
     	 * Removes a choice from the ChoiceMap, using a given
     	 * StoryFragment ID and index
     	 * 
     	 * @param fragmentId ID of StoryFragment where choice is located
     	 * @param index Index of the choice to remove
     	 * @return Returns true if choice existed, otherwise returns false
     	 */
     	public boolean deleteChoice(int fragmentId, int index) {
     		return this.choiceMap.deleteChoice(fragmentId, index);
     	}
     	
     	/**
     	 * Updates a Choice for a given StoryFragment with a new given
     	 * Choice, using its index
     	 * 
     	 * @param fragmentId ID of StoryFragment where choice is located
     	 * @param index Index of choice to change
     	 * @param choice New choice to put in there
     	 * @return Returns true if choice already existed, false otherwise
     	 */
     	public boolean updateChoice(int fragmentId, int index, Choice choice) {
     		return this.choiceMap.updateChoice(fragmentId, index, choice);
     	}
     	
     	/**
     	 * Used to remove all references to a given StoryFragment ID.
     	 * Should be called when a StoryFragment is deleted.
     	 * 
     	 * @param fragmentId   ID of StoryFragment to remove references to
     	 */
     	public void cleanFragmentReferences(int fragmentId) {
     		this.choiceMap.cleanFragmentReferences(fragmentId);
     	}
     	
     	/**
     	 * Returns an ArrayList of all Choices for a given StoryFragment ID
     	 * Entries are sorted by destination StoryFragment ID
     	 * 
     	 * @param fragmentId ID of StoryFragment to get the choices for
     	 * @return ArrayList of Choice objects for the given StoryFragment ID
     	 */
     	public ArrayList<Choice> getChoices(int fragmentId) {
     		return this.choiceMap.getChoices(fragmentId);
     	}
         
     	
         /* Functions that deal with the History */
     	/**
     	 * Returns the ID of the last-viewed Story Fragment.
     	 * If History is empty, returns null.
     	 * 
     	 * @return Integer ID of last-viewed fragment
     	 */
     	public Integer getMostRecent() {
     		return this.historyStack.getMostRecent();
     	}
     	
     	/**
     	 * Removes the most recent StoryFragment ID from the stack,
     	 * and returns the previous StoryFragment ID.
     	 * When there is no previous ID to return, returns null.
     	 * 
     	 * @return StoryFragmentID of previous StoryFragment
     	 */
     	public Integer goBack() {
     		return this.historyStack.goBack();
     	}
     	
     	/**
     	 * Pushes a StoryFragment ID onto the History Stack.
     	 * Should be called for every new StoryFragment viewed.
     	 * 
     	 * @param fragment_id ID of StoryFragment to be pushed to stack
     	 */
     	public void pushToStack(Integer fragmentId) {
     		this.historyStack.pushToStack(fragmentId);
     	}
         
     	/**
     	 * Clears the History stack.
     	 * Used when a user decides to start a new reading session.
     	 */
     	public void clearHistory() {
     		this.historyStack.clearHistory();
     	}
         
     	
         /* Methods required for Serializable Interface */
     	/**
     	 * Serializable method to write out a Story.
     	 * 
     	 * @param out an ObjectOutputSteam
     	 * @throws IOException
     	 */
         private void writeObject(java.io.ObjectOutputStream out)
                  throws IOException {
                 out.writeObject(this.title);
                 out.writeObject(this.author);
                 out.writeObject(this.description);
                 out.writeObject(this.firstPage);
                 out.writeObject(this.historyStack);
                 out.writeObject(this.fragmentList);
                 out.writeObject(this.choiceMap);
         }
         
         /**
          * Serializable method to read in a Story.
          * 
          * @param in an ObjectInputStream
          * @throws IOException
          * @throws ClassNotFoundException
          */
         private void readObject(java.io.ObjectInputStream in)
          throws IOException, ClassNotFoundException {
                 this.title = (String) in.readObject();
                 this.author = (String) in.readObject();
                 this.description = (String) in.readObject();
                 this.firstPage = (Integer) in.readObject();
                 this.historyStack = (History) in.readObject();
                 this.fragmentList = (StoryFragmentList) in.readObject();
                 this.choiceMap = (ChoiceMap) in.readObject();
         }
         
         private void readObjectNoData()
          throws ObjectStreamException{
         }
 
 }
