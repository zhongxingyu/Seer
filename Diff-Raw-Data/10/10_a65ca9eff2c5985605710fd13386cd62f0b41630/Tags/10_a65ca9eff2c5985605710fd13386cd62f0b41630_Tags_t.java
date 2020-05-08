 package internalformatting;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  * Creates an ArrayList of Strings that will describe certain attributes of various objects in the system.
  * will assist in the search process
  */
 
 /**
  * @author Clayven Anderson
  * @version 2/15/12 for cs48 W12
  */
 
 public class Tags extends ArrayList<String> implements Serializable{
 
 	/**
 	 * creates an empty Arraylist of tags
 	 */
 	
 	//default constructor
 	public Tags() {
 		super(1);// calls superclass's constructor to make and empty ArrayList
 	}
 
 	final private static long serialVersionUID = 1670087L;
   
 	/** Appends a new "tag" to the end of the ArrayList 
 	 * @param newTag New tag to be appended to list
 	 */
 	
 	public void AddTag(String newTag) {
 		this.ensureCapacity(this.size() + 1);
 		this.add(newTag);
 	}
 
 	/** removes a specified tag from the list
 	 * @param BadTag the tag the must be removed
 	 */
 	
 	public void deleteTag(String BadTag) {
		for(int i=0; i<=this.size(); i++ ) {
			if(this.get(i).equals(BadTag) ) {
 				this.remove(i);
 				break;
 			}
 			else {
 				continue;
 			}
 		}
    }
   
 	/** clears all tags*/
 	
 	public void clearTags(){
 		this.clear();
 	}
 	
 	/**
 	 * returns the tag at position i
 	 * @param i i is the index of the desired tag
 	 * @return 
 	 */
 	
 	public String getTag(int i){ // A getter method takes a parameter?
 		return this.get(i);
 	}
 	
 	/** returns all of the tags in the array list as a single string (just for the moment, most likely will change)*/
 	public String returnAllTags() {
 		String AllTags = "";
 		if(this.isEmpty() == true ) {
			AllTags = AllTags + "No tags Available";
 		}
 		else {
			for(int i=0; i<this.size(); i++){
 				AllTags = AllTags + " " + this.get(i); //***TODO format this a bit better***
 			}
 		}
 		return AllTags;
 	}
 
 	public int getSize() {
 		return this.size();
 	}
  
 } // class Tags
