 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models;
 
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.Note;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.NoteList;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.NotePanel;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * Tests the Note.java, NoteList.java, and NotePanel.java source files
  * @author Rolling Thunder
  *
  */
 public class NoteTest {
 	
 	//===== Note.java tests =====
	/*
 	@Test
 	public void makeANoteTest() {
 		assertNotNull(new Note("Rolling Thunder", 120, "Test note"));
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Test
 	public void makeADepreciatedNoteTest() {
 		assertNotNull(new Note(3, "Rolling Thunder", 120, "Test note"));
 	}
 	
 	// This test does not check the exact Id number due to the auto assignment of Id numbers
 	// causing the tests to fail if ran out of order.
 	@Test
 	public void getIdTest() {
 		Note testNote = new Note("Rolling Thunder", 120, "Test note");
 		assertNotNull(testNote.getId());
 		@SuppressWarnings("deprecation")
 		Note testNote2 = new Note(3, "Rolling Thunder", 120, "Test note");
 		assertNotNull(testNote2.getId());
 	}
 	
 	@Test
 	public void allNoteGettersTest() {
 		Note testNote = new Note("Rolling Thunder", 120, "Test note");
 		assertEquals("Rolling Thunder", testNote.getUser());
 		assertEquals(120, testNote.getTimestamp());
 		assertEquals("Test note", testNote.getMessage());
 	}
 	
 	@Test
 	public void allNoteSettersTest() {
 		Note testNote = new Note("Rolling Thunder", 120, "Test note");
 		testNote.setUser("Thunder Roller");
 		testNote.setTimestamp(360);
 		testNote.setMessage("Changed message");
 		
 		assertEquals("Thunder Roller", testNote.getUser());
 		assertEquals(360, testNote.getTimestamp());
 		assertEquals("Changed message", testNote.getMessage());
 	}
 	
 	//===== NoteList.java test =====
 	
 	@Test
 	public void makeANoteListTest() {
 		assertNotNull(new NoteList());
 	}
 	
 	@Test
 	public void addNoteToNoteListTest() {
 		NoteList list = new NoteList();
 		assertNotNull(list.add("first note added"));
 	}
 	
 	@Test
 	public void getNotesFromNoteListTest() {
 		NoteList list = new NoteList();
 		list.add("first note added");
 		list.add("second note added");
 		list.add("third note added");
 		
 		LinkedList<Note> returnList = list.getNotes();
 		assertEquals("first note added", returnList.get(0).getMessage());
 		assertEquals("second note added", returnList.get(1).getMessage());
 		assertEquals("third note added", returnList.get(2).getMessage());
 	}
 	
 	@Test
 	public void getItemFromNotesListTest() {
 		NoteList list = new NoteList();
 		list.add("first note added");
 		list.add("second note added");
 		list.add("third note added");
 		
 		assertEquals("first note added", list.getItem(0).getMessage());
 		assertEquals("second note added", list.getItem(1).getMessage());
 		assertEquals("third note added", list.getItem(2).getMessage());
 	}
 	
 	//===== NotePanel.java test =====
 	
 	@Test
 	public void makeANotePanelTest() {
 		Note testNote = new Note("Rolling Thunder", 120, "Test note");
 		assertNotNull(new NotePanel(testNote));
 		
 	}
 	
 	@Test
 	public void createNotePanelFromNoteListTest() {
 		NoteList list = new NoteList();
 		list.add("first note added");
 		list.add("second note added");
 		list.add("third note added");
 		assertNotNull(NotePanel.createList(list));
	}*/
 }
