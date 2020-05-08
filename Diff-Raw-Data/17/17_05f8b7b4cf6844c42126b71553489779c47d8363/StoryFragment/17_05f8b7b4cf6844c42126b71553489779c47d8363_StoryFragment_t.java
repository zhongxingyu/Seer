 /*
 Fragment Class for CreateYourOwnAdventure App.
 Composed of Content objects which implement the Media interface.
     
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
  * StoryFragment object, essentially a single page for a story.
  * Has title and description attributes.
  * Composed of Content and Annotations, which implement the Media interface.
  * Content and Annotations can be one of:
  * Text, Image, Video, Sound
  * 
  * @author Jesse Chu <jhchu@ualberta.ca>
  */
 
 public class StoryFragment implements Serializable {
 	
 	/* Instance Variables for a Fragment */
 	// Attributes for a Fragment
 	private String title;
 	private String description;
 	
 	// Content/Annotation Lists for a Fragment
 	private ArrayList<Media> contentList;
 	private ArrayList<Media> annotationList;
 	
 	/**
 	 * Constructor. Initializes all instance variables.
 	 * Instantiated with default values.
 	 */
 	public StoryFragment() {
 //		this.title = "New Page";
 //		this.description = "A new page";
 		this.contentList = new ArrayList<Media>();
 		this.annotationList = new ArrayList<Media>();
 	}
 	
 	/**
 	 * Getter for Fragment title.
 	 * 
 	 * @return the title of the Fragment
 	 */
 	public String getTitle() {
 		return this.title;
 	}
 	
 	/**
 	 * Getter for Fragment description.
 	 * 
 	 * @return the description of the Fragment
 	 */
 	public String getDescription() {
 		return this.description;
 	}
 	
 	/**
 	 * Getter for the contentList for a Fragment
 	 * 
 	 * @return the contentList of the Fragment
 	 */
 	public ArrayList<Media> getContentList() {
 		return this.contentList;
 	}
 	
 	/**
 	 * Getter for the annotationList for a Fragment
 	 * 
 	 * @return the annotationList of the Fragment
 	 */
 	public ArrayList<Media> getAnnotationList() {
 		return this.annotationList;
 	}
 	
 	/**
 	 * Setter for the title of the Fragment.
 	 * 
 	 * @param title the string to set the title to
 	 * @return true if successful, false otherwise
 	 */
 	public boolean setTitle(String title) {
 		this.title = title;
 		return true;
 	}
 	
 	/**
 	 * Setter for the description of the Fragment.
 	 * 
 	 * @param title the string to set the description to
 	 * @return true if successful, false otherwise
 	 */
 	public boolean setDescription(String description) {
 		this.description = description;
 		return true;
 	}
 	
 	public void displayFragment() {
 		
 	}
 
 	/**
 	 * Adds a given Media object to the contentList
 	 * 
 	 * @param content the Media to add to the Fragment
 	 * @return true if successful, false otherwise
 	 */
 	public boolean addContent(Media content) {
		if (!(content == null)){
			contentList.add(content);
			return true;
		} else {
			return false;
		}
 	}
 
 	/**
 	 * Removes a given Media object from the contentList.
 	 * 
 	 * @param content the Media to remove from the Fragment
 	 * @return true if successful, false otherwise
 	 */
 	public boolean removeContent(Media content) {
 		boolean removal = contentList.remove(content);
 		if (removal) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Clears the contentList.
 	 */
 	public void removeAllContent() {
 		this.contentList = new ArrayList<Media>();
 	}
 	
 	/**
 	 * Adds a given Media object to the annotationList.
 	 * 
 	 * @param annotation the Media to add to the Fragment
 	 * @return true if successful, false otherwise
 	 */
 	public boolean addAnnotation(Media annotation) {
		if (!(annotation == null)) {
			annotationList.add(annotation);
			return true;
		} else {
			return false;
		}
 	}
 	
 	/**
 	 * Removes a given Media object from the annotationList.
 	 * 
 	 * @param annotation the Media to remove from the Fragment
 	 * @return true if successful, false otherwise
 	 */
 	public boolean removeAnnotation(Media annotation) {
 		boolean removal = annotationList.remove(annotation);
 		if (removal) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Clears the annotationList.
 	 */
 	public void removeAllAnnotation() {
 		this.annotationList = new ArrayList<Media>();
 	}
 	
 	/* Methods required for Serializable Interface */
 	/**
 	 * Serializable method to write out a StoryFragment.
 	 * 
 	 * @param out an ObjectOutputSteam
 	 * @throws IOException
 	 */
 	private void writeObject(java.io.ObjectOutputStream out)
 		     throws IOException {
 		out.writeObject(this.title);
 		out.writeObject(this.description);
 		out.writeObject(this.contentList);
 		out.writeObject(this.annotationList);
 	}
 	
     /**
      * Serializable method to read in a StoryFragment.
      * 
      * @param in an ObjectInputStream
      * @throws IOException
      * @throws ClassNotFoundException
      */
 	private void readObject(java.io.ObjectInputStream in)
 	    throws IOException, ClassNotFoundException {
 		this.title = (String) in.readObject();
 		this.description = (String) in.readObject();
 		this.contentList = (ArrayList<Media>) in.readObject();
 		this.annotationList = (ArrayList<Media>) in.readObject();
 	}
 	
 	private void readObjectNoData()
 	    throws ObjectStreamException{
 	}
 	
 }
