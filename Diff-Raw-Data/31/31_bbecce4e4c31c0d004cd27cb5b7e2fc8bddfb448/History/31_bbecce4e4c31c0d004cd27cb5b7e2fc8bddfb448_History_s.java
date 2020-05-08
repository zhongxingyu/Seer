 /*
 History class for CreateYourOwnAdventure.
 Keeps a memory of past read fragments
 
      Copyright  2013 Reginald Miller
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
 
 /**
  * An instance of this class exists when a story is
  * created, keeping track of which fragment
  * was last viewed in read mode by a user.
  * 
  * @author Reginald Miller
  *
  */
 
 public class History implements Serializable {
 
 	private ArrayList<Integer> historyStack;
 	
 	public History() {
 		historyStack = new ArrayList<Integer>();
 	}
 	
 	/**
 	 * Returns the most recently-viewed fragment, or
 	 * null if the stack is empty.
 	 * 
 	 * @return Integer ID of last-viewed fragment.
 	 */
 	public Integer getMostRecent() {
 		
 		int size = historyStack.size();
 				
 		if (size > 0) {
 			int lastIndex = size - 1;
 			return historyStack.get(lastIndex);
 		}
 		//Returns null if stack is empty
 		return null;
 	}
 	
 	/**
 	 * Pops the latest-viewed fragment from the stack
 	 * and returns the id of the next most recently viewed
 	 * fragment.
 	 * If on the first-viewed fragment, returns null.
 	 * If stack is empty, returns null.
 	 * 
 	 * @return Second-last-viewed fragment.
 	 */
 	public Integer goBack() {
 		
 		int size = historyStack.size();
 		
 		if (size > 0) {
 			int lastIndex = size - 1;
 			int lastFragment = historyStack.get(lastIndex);
 			historyStack.remove(lastIndex);
			printHistory();
 			return lastFragment;
 		}
 		//If stack is empty
 		else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Pushes the newest fragment being viewed to the stack, if 
 	 * the most recent fragment is not at the top of the stack.
 	 * 
 	 * @param fragment_id   ID of fragment to be pushed to stack.
 	 */
 	public void pushToStack(Integer fragmentId) {
 		
 		Integer mostRecent = getMostRecent();
 		
 		if (mostRecent != null) {
			if (fragmentId != mostRecent) {
 				historyStack.add(fragmentId); }
 			else {
 				return;
 			}
 		}
 		else {
 			historyStack.add(fragmentId);
 		}
		printHistory();
 		return;
 	}
 	
 	
 	/**
 	 * Clears the stack so that user may start again.
 	 */
 	public void clearHistory() {
 		historyStack.clear();
		printHistory();
 		return;
 	}
 	
 	/**
 	 * Writes the Serializable historyStack object to
 	 * a file.
 	 * 
 	 * @param out   ObjectOutputStream to write with
 	 * @throws IOException
 	 */
 	private void writeObject(java.io.ObjectOutputStream out)
 		     throws IOException {
 		out.writeObject(historyStack);
 		return;
 	}
 	
 	/**
 	 * Reads the Serializable historyStack object from
 	 * a file.
 	 * 
 	 * @param in   ObjectInputStream to read with
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	private void readObject(java.io.ObjectInputStream in)
 		     throws IOException, ClassNotFoundException {
 		historyStack = (ArrayList<Integer>) in.readObject();
 		return;
 	}
 		 
 	private void readObjectNoData() 
 		     throws ObjectStreamException {
 		return;
 	}
 	
	// debug purpose only
	private void printHistory() {
		System.out.println("History Stack:");
		for (int fragId : historyStack) {
			System.out.print(fragId + " ");
		}
		System.out.println("Done printing.");
	}
	
 }
