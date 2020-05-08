 /*
 Adventure App - Allows you to create an Adventure Book, or Download
 	books from other authors.
 Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.uofa.adventure_app.controller;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
 import com.uofa.adventure_app.application.AdventureApplication;
 import com.uofa.adventure_app.model.Fragement;
 import com.uofa.adventure_app.model.Story;
 /**
  * Keeps track of the current story, fragement, and keeps a stack of both
  * as well.
  * @author Chris Pavlicek
  *
  */
 public class StoryController {
 	
 	ArrayList<Story> stories;
 	Story currentStory;
 	Fragement currentFragement;
 	ArrayList<Fragement> previousFragementList;
 	
 	private static String FILENAME = "savedStories.sav";
 	
 	public StoryController () {
 		this.stories = new ArrayList<Story>();
 		this.previousFragementList = new ArrayList<Fragement>();
 	}
 	
 	/**
 	 * @return the currentStory
 	 */
 	public Story currentStory() {
 		return currentStory;
 	}
 
 	/**
 	 * Sets the Current Story we are interested in and sets
 	 * the first fragement as the current fragement. 
 	 * @param currentStory the currentStory to set
 	 */
 	public void setCurrentStory(Story currentStory) {
 		clearPreviousFragementList();
 		this.currentStory = currentStory;
 		setCurrentFragement(currentStory.startFragement());
 	}
 
 	/**
 	 * @return the currentFragement
 	 */
 	public Fragement currentFragement() {
 		return currentFragement;
 	}
 
 	/**
 	 * @param currentFragement the currentFragement to set
 	 */
 	public void setCurrentFragement(Fragement currentFragement) {
 		this.currentFragement = currentFragement;
 	}
 	
 	/**
 	 * returns the previous Fragement List.
 	 * @return ArrayList<Fragement> previousFragementList
 	 */
 	public ArrayList<Fragement> previousFragementList() {
 		return previousFragementList;
 	}
 
 	/**
 	 * Removes the last index in the list, If empty
 	 * nothing happens.
 	 */
 	public void popPreviousFragement() {
 		
 		if(!previousFragementList.isEmpty()) {
 			int lastIndex = previousFragementList.size()-1;
 			previousFragementList.remove(lastIndex);
 		}
 	}
 	
 	/**
 	 * returns the Last Fragement we visited, if none, null is returned
 	 * @return
 	 */
 	public Fragement lastFragement() {
 		if(!previousFragementList.isEmpty()) {
 			int lastIndex = previousFragementList.size()-1;
 			return previousFragementList.get(lastIndex);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Adds A Fragement to our list. This is used for backtracking
 	 * and for editing.
 	 * @param Fragement f
 	 */
 	public void addPreviousFragement(Fragement f) {
 		previousFragementList.add(f);
 	}
 	
 	/**
 	 * Clears the Previous Fragement List.
 	 */
 	private void clearPreviousFragementList() {
 		this.previousFragementList.clear();
 	}
 	
 
 	/**
 	 * adds a story to the list of stories for the browserView.
 	 * @param Story story
 	 */
 
 	public void addStory(Story story) {
 		if(!stories.contains(story)) {
 			this.stories.add(story);
			AdventureApplication.getActivityController().update();
 		}
 	}
 	/**
 	 * Replaces a story in the story list so that we dont end up with duplicates
 	 * @param Story story
 	 */
 	public void replaceStory(Story story) {
 		if(stories.contains(story)) {
 			int index = this.stories.indexOf(story);
 			this.stories.set(index, story);
 		}
 	}
 	
 	public ArrayList<Story> stories() {
 		return this.stories;
 	}
 
 /**
  * Allows you to set the current array of stories.
  * You should probably not call this unless you
  * know what your doing!
  * @param ArrayList<Story> stories
  */
 	public void setStories(ArrayList<Story> stories) {
 		this.stories = stories;
 	}
 	
 	/** 
 	 * Saves the current state of the story controller.
 	 */
 	public void saveStories() {
 		// Use file stream to save our array object.
 		ArrayList<Story> tempArray = new ArrayList<Story>();
 		for(Story s: AdventureApplication.getStoryController().stories()) {
 			if(s.isLocal()) {
 				tempArray.add(s);
 			}
 		}
 		
 				try {
 					FileOutputStream fos =  AdventureApplication.context().openFileOutput(FILENAME, 0);
 					ObjectOutputStream oos = new ObjectOutputStream(fos);
 					oos.writeObject(tempArray);
 					fos.close();
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			tempArray = null;
 	}
 	
 	/**
 	 * Loads stories from the local storage file.
 	 */
 	public void loadStories() {
 		ArrayList<Story> result = new ArrayList<Story>();
 		try {
 			FileInputStream fis = AdventureApplication.context().openFileInput(FILENAME);
 
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			while (true) {
 				result.addAll((ArrayList) ois.readObject());
 			}
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(result);
 		this.setStories(result);
 	}
 	
 }
