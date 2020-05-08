 /*
 Adventure App - Allows you to create an Adventure Book, or Download
 	books from other users.
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
 package com.uofa.adventure_app.model;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import com.uofa.adventure_app.application.AdventureApplication;
 /**
  * This ties the rest of the model together. everything is 
  * contained in a "Story".
  * @author Kevin Lafond, Joel Malina, Chris Pavlicek, Ulvi Ibrahimov
  *
  */
 public class Story implements Serializable, Cloneable {
 
 	private String title;
 	private ArrayList<User> users; 
 	private ArrayList<Fragement> fragements;
 	private UUID id;
 	private boolean isLocal;
 	private Fragement startFragement;
 	
 	public Story() {
 		this.users = new ArrayList<User>();
 		this.fragements = new ArrayList<Fragement>();
 		this.id = UUID.randomUUID();
 		this.title = "";
 		this.isLocal = false;
 	}
 	
 	public Story(UUID uniqueId) {
 		this.users = new ArrayList<User>();
 		this.fragements = new ArrayList<Fragement>();
 		this.id = uniqueId;
 		this.title = "";
 	}
 	
 	
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	/**
 	 * @param title the title of the story
 	 */
 	public String title() {
 		return this.title;
 	}
 
 	/**
 	 * @param users the users to set
 	 */
 	public void setUsers(ArrayList<User> users) {
 		this.users = users;
 	}
 /**
  * Adds an User to the Current List of users
  * @param author
  */
 	public void addUser(User user) {
 		
 		if (!this.users().contains(user)){
 			this.users.add(user);
 		}
 	}
 	/**
 	 * returns list of users for a story
 	 * @return
 	 */
 	public ArrayList<User> users() {
 		return this.users;
 	}
 	
 	/**
 	 * @param fragements the fragements to set
 	 */
 	public void setFragements(ArrayList<Fragement> fragements) {
 		this.fragements = fragements;
 	}
 	/**
 	 * adds a fragment to a story
 	 * @param fragement
 	 */
 	public void addFragement(Fragement fragement) {
 		this.fragements.add(fragement);
 	}
 	/**
 	 * Returns a list of fragments for a story
 	 * @return ArrayList<fragment>
 	 */
 	public ArrayList<Fragement> getFragements()
 	{	
 		return this.fragements;	
 	}
 	
 	// shouldn't this be private?!
 	// cause i can set two story id's to be the same otherwise!!
 	public void setId(UUID id) {
 		this.id = id;
 	}
 	/**
 	 * 
 	 * @return UUID
 	 */
 	public UUID id() {
 		return this.id;
 	}
 	
 /**
  * Returns the boolean true if the story is in the local storage
  * otherwise false.
  * @return
  */
 	public boolean isLocal() {
 		return this.isLocal;
 	}
 	
 	/**
 	 * Set if the story is local or not.
 	 * @param value
 	 */
 	public void setIsLocal(boolean value) {
 		this.isLocal = value;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		// TODO Auto-generated method stub
 		if(o.getClass().equals(this.getClass())) {
 			Story story = (Story) o;
 			if (this.id().equals(story.id()))
 				return true;
 			else
 				return false;
 		}
 		
 		if (this == o)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * @return the startFragement
 	 */
 	public Fragement startFragement() {
 		return startFragement;
 	}
 
 	/**
 	 * @param startFragement the startFragement to set
 	 */
 	public void setStartFragement(Fragement startFragement) {
 		this.startFragement = startFragement;
 	} 
 	
 
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return super.toString() + this.title() + this.getFragements() + "\n";
 	}   
 
 	/**
 	 * Always treat de-serialization as a full-blown constructor, by validating
 	 * the final state of the de-serialized object.
 	 */
 	private void readObject(ObjectInputStream aInputStream)
 			throws ClassNotFoundException, IOException {
 		// always perform the default de-serialization first
 		aInputStream.defaultReadObject();
 	}
 
 	/**
 	 * This is the default implementation of writeObject. Customise if
 	 * necessary.
 	 */
 	private void writeObject(ObjectOutputStream aOutputStream)
 			throws IOException {
 		// perform the default serialization for all non-transient, non-static
 		// fields
 		aOutputStream.defaultWriteObject();
 	}
 	/**
 	 * creates a local copy of a story.
 	 * @return Story
 	 */
 	public Story localCopy() {
 		Story s = new Story();
 		s.setTitle(new String("Copy of " + this.title()));
 		for (User user : this.users()) {
 			s.addUser(user);
 		}
 		s.addUser(AdventureApplication.user());
 		
 		//for(User user: s.users()) {
 		//System.out.println(user.uid().toString());
 		//}
 		//
 		s.setIsLocal(true);
 		Map<UUID, UUID> uidMap = new HashMap<UUID,UUID>();
		
 		ArrayList<Fragement> copyFragements = new ArrayList<Fragement>();
 		// Start Fragement should always be set!
 		if(this.startFragement() != null) {
 			Fragement startFragement = this.startFragement().localCopy();
 			s.setStartFragement(startFragement);
 			copyFragements.add(startFragement);
 			uidMap.put(this.startFragement().uid(), startFragement.uid());
 		} else {
 			return null;
 		}
 
 		for (Fragement f : this.getFragements()) 
 		{
 			if(!f.equals(startFragement)) {
 				Fragement localCopy = f.localCopy();
 				uidMap.put(f.uid(), localCopy.uid());
 				copyFragements.add(localCopy);
 			}
 		}
 		
 		for (Fragement f : copyFragements) 
 			{
 			ArrayList<Choice> copyChoices = new ArrayList<Choice>();
 			for (Choice c : f.choices()) {
 					UUID oldUid = uidMap.get(c.getChoiceId());
 					Fragement newFragementChoice = copyFragements.get(copyFragements.indexOf(new Fragement(oldUid)));
 					c.setChoice(newFragementChoice);
 				}
 				f.setChoices(copyChoices);
 			}
 		s.setFragements(copyFragements);
 		
 		return s;
 		
 	}
 	
 	/**
 	 * Returns the fragement with the assosiated UUID,
 	 * if it does not exist null is returned.
 	 * @param UUID id
 	 * @return Fragement
 	 */
 	public Fragement fragementWithId(UUID id) {
 		Fragement compareFrag = new Fragement(id);
 		if(this.fragements.contains(compareFrag)) {
 			int index = this.fragements.indexOf(compareFrag);	
 			return this.fragements.get(index);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Replaces fragment, if it does not exist than it is added
 	 * @param frag
 	 */
 	public void replaceFragement(Fragement frag) {
 		if(this.fragements.contains(frag)) {
 			int index = this.fragements.indexOf(frag);
 			this.fragements.set(index, frag);
 		} else {
 			this.fragements.add(frag);
 		}
 	}
 	
 }
