 /*
  * Copyright 2011 Mark Eschbach.
  *
  * $HeadURL$
  * $Id$
  */
 package com.meschbach.xp.vaadin.sticky.model.memory;
 
 import com.meschbach.xp.vaadin.sticky.model.StickyException;
 import com.meschbach.xp.vaadin.sticky.model.StickyNote;
 import com.meschbach.xp.vaadin.sticky.model.StickyQuotaException;
 import com.meschbach.xp.vaadin.sticky.model.StickyUser;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 /**
  *
  * @author "Mark Eschbach" (meschbach@gmail.com)
  */
 public class MemoryStickyUser implements StickyUser, Serializable {
 
     List<StickyNote> notes;
     String name;
     int quota;
 
     public MemoryStickyUser(String userName, int quota) {
 	this.notes = new ArrayList<StickyNote>();
 	this.quota = quota;
 	this.name = userName;
     }
 
 
     public synchronized StickyNote createNote() throws StickyException {
	if(notes.size() + 1 >= quota){
 	    throw new StickyQuotaException();
 	}
 	MemoryStickyNote msn = new MemoryStickyNote();
 	notes.add(msn);
 	return msn;
     }
 
     public List<StickyNote> getNotes() throws StickyException {
 	return Collections.unmodifiableList(notes);
     }
 
     public String getUserName() throws StickyException {
 	return name;
     }
 
     public void removeNote(StickyNote note) throws StickyException {
 	if(!notes.remove(note)) throw new NoSuchElementException();
     }
 }
