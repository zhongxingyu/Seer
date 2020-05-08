 /*
 ChoiceMap class for CreateYourOwnAdventure.
 Allows for access to all choices linking fragments in a story
 
      Copyright  �2013 Reginald Miller
     <Contact: rmiller3@ualberta.ca>
     
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
 
 import java.io.IOException;
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * This class contains all methods associated with selecting
  * adding, editing or removing the choice linkages for a given story.
  * 
  * @author Reginald Miller, October 2013
  *
  */
 
 public class ChoiceMap implements Serializable {
 
 	//Maps Fragment IDs to Fragment ArrayList, to be indexed
 	private HashMap<Integer, ArrayList<Choice>> choiceMapping;
 	
 	public ChoiceMap() {
 		this.choiceMapping = new HashMap<Integer, ArrayList<Choice>>();
 	}
 	
 	/**
 	 * This is called whenever a previously-unlinked choice selects a
 	 * new destination.
 	 * 
 	 * @param fragmentId   ID of fragment where choice is located
 	 * @param choice   Choice object to add to the ChoiceMap
 	 */
 	public void addChoice(Integer fragmentId, Choice choice) {
 		
 		ArrayList<Choice> destinations = choiceMapping.get(fragmentId);
 		
 		if (destinations != null) {
 			destinations.add(choice);
 			choiceMapping.put(fragmentId, destinations);
 		}
 		//This occurs if no choices in the fragment exist yet
 		else {
 			ArrayList<Choice> destination = new ArrayList<Choice>();
 			destination.add(choice);
 			choiceMapping.put(fragmentId, destination);
 		}
 		return;
 	}
 	
 	/**
 	 * Removes a choice from the ChoiceMap when author unlinks a choice
 	 * 
 	 * @param fragmentId   ID of fragment where choice is located
 	 * @param index   Index of the choice to remove
 	 * @return   Returns true if choice existed, otherwise returns false
 	 */
 	public boolean deleteChoice(Integer fragmentId, int index) {
 		
 		ArrayList<Choice> destinations = choiceMapping.get(fragmentId);
 		
 		//Makes sure entry exists in HashMap and that the choice doesn't
 		//index outside of length of destinations list
 		if(destinations != null && index >= 0 && index < destinations.size()) {
 			destinations.remove(index);
 			choiceMapping.put(fragmentId, destinations);
 			return true;
 		}
 		//If tried to remove a choice that doesn't exist, returns false
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Switches the destination of an already-linked choice to a different
 	 * destination fragment.
 	 * 
 	 * @param fragmentId   ID of fragment where choice is located
 	 * @param index   Index of choice to change
 	 * @param choice   New choice to put in there
 	 * @return   Returns true if choice already existed, false otherwise
 	 */
 	public boolean updateChoice(Integer fragmentId, int index, Choice choice) {
 		
 		ArrayList<Choice> destinations = choiceMapping.get(fragmentId);
 		
 		//Insures that the choice already exists, deletes old choice from ChoiceMap
		if(destinations != null && index > 0 && index < destinations.size()) {
 			destinations.set(index, choice);
 			choiceMapping.put(fragmentId, destinations);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Want this to be called when a fragment is deleted. If it doesn't
 	 * exist, then there's nothing to remove. Also removes all choices
 	 * currently linked to it.
 	 * 
 	 * @param fragmentId   ID of fragment to remove from all choices
 	 */
 	public void cleanFragmentReferences(Integer fragmentId) {
 		
 		//Removes all choices the fragment is linked to
 		if (choiceMapping.containsKey(fragmentId)) {
 			choiceMapping.remove(fragmentId);
 		}
 		
 		//Checks for all instances where the fragment was a 
 		//destination and removes them.
 		for (Integer id : choiceMapping.keySet()) {
 			ArrayList<Choice> choiceList = choiceMapping.get(id);
 			int choiceIndex;
 				
 			//Make sure we don't remove entry while iterating
 			//through it.
 			//If multiple choices leading to same fragment, want
 			//to remove all of them.
 			do {
 				choiceIndex = -1;
 				for (Choice choice : choiceList) {
 					if (choice.getDestinationId() == fragmentId) {
 						choiceIndex = choiceList.indexOf(choice);
 						break;
 					}
 				}
 				
 				//If choice found, it is removed
 				if (choiceIndex != -1) {
 					choiceList.remove(choiceIndex);
 				}
 			} while (choiceIndex != -1);
 			
 			choiceMapping.put(id, choiceList);
 		}
 		
 		return;
 	}
 	
 	/**
 	 * Returns an ArrayList of map entries for all possible choices for the fragment.
 	 * Entries are sorted by fragment ID.
 	 * 
 	 * @param fragmentId   ID of fragment to fetch the choices for
 	 * @return   Assorted ArrayList of Integer, String pairs associated with fragment ID and choice text
 	 */
 	public ArrayList<Choice> getChoices(Integer fragmentId) {
 		
 		ArrayList<Choice> destinations = choiceMapping.get(fragmentId);
 		
 		if (destinations == null) {
 			return new ArrayList<Choice>();
 		}
 		else {
 			return destinations;
 		}
 	}
 	
 	/**
 	 * Writes the Serializable choiceMapping HashMap to
 	 * a file.
 	 * 
 	 * @param out   ObjectOutputStream to write with
 	 * @throws IOException
 	 */
 	private void writeObject(java.io.ObjectOutputStream out)
 		     throws IOException {
 		out.writeObject(choiceMapping);
 	}
 	
 	/**
 	 * Reads the Serializable choiceMapping HashMap from
 	 * a file
 	 * 
 	 * @param in   ObjectInputStream to read with
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	private void readObject(java.io.ObjectInputStream in)
 		     throws IOException, ClassNotFoundException {
 		choiceMapping = (HashMap<Integer, ArrayList<Choice>>) in.readObject();
 	}
 		 
 	private void readObjectNoData() 
 		     throws ObjectStreamException {
 	}
 	
 }
