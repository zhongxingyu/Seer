 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
/**
 * 
 */
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics;
 
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import edu.wpi.cs.wpisuitetng.janeway.config.ConfigManager;
 
 /**
  * A list of notes that are added to a requirement.
  * @author Brian Froehlich
  *
  */
 public class NoteList {
 
 	private LinkedList<Note> notes;
 	
 	/**
 	 * Constructs an empty list of notes.
 	 */
 	public NoteList()
 	{
 		this.notes = new LinkedList<Note>();
 	}
 	
 	/**
 	 * Use this function to get a list iterator that you can use to cycle through the elements of the list
 	 * @param index The index of the list that you want the iterator to start on
 	 * @return The iterator containing all the elements of the list
 	 */
 	public ListIterator<Note> getIterator(int index){
 		return this.notes.listIterator(index);
 	}
 	
 	/**
 	 * Getter for the linked list of notes
 	 * @return the linked list of notes
 	 */
 	public LinkedList<Note> getNotes(){
 		return this.notes;
 	}
 	
 	/**
 	 * Allows you to add to the records of transactions
 	 * Always adds to the new note to the end of the list
 	 * @param msg The message in the note to be added
 	 * @return The note that was just added to the notes
 	 */
 	
 	public Note add(String msg){
 		int id = notes.size() + 1;
 		long time = System.currentTimeMillis();
 		String user = ConfigManager.getConfig().getUserName();
 		Note newNote = new Note(id, user, time, msg);
 		notes.add(newNote);
 		return newNote;
 	}
 	
 	/**
 	 * Allows you to get the item at the given index in the list
 	 * @param index The index at which the desired note resides
 	 * @return The note at the index given by the parameter
 	 */
 	public Note getItem(int index){
 		return this.notes.get(index);
 	}
 }
