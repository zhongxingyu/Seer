 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package story.book.model;
 
 import java.util.*;
 
 import story.book.view.StoryView;
 
 /**
  * Template model class for the application.
  *  
  * @author 	Alexander Cheung
  * @param 	<V>	Any object implementing the <code>StoryView</code> interface
  * @see		StoryView
  */
 public abstract class StoryModel<V extends StoryView> {
 	
 	transient private Set<V> views;
 	
 	/**
 	 * Default constructor initializes an empty <code>ArrayList</code> of
 	 * <code>V</code> objects.
 	 */
 	public StoryModel () {
 		views = new HashSet<V>();
 	}
 
 	/**
 	 * Add a <code>V</code> object to the list of views.
 	 * @param 	view	the <code>V</code> object to add
 	 */
 	public void addView(V view) {
 		if(views == null) //when IO creates a new story from disk story Model constructor is never called
 			views = new HashSet<V>();
 		views.add(view);
 		
 	}
 
 	/**
 	 * Remove a <code>V</code> object from the list of views.
 	 * @param 	view	the <code>V</code> object to remove
 	 */
 	public void deleteView(V view) {
 		views.remove(view);
 	}
 
 	/**
 	 * Notifies all views that the model has changed.
 	 */
 	public void notifyViews() {
 		for (V view : views) {
 			view.update(this);
 		}
 	}
 }
