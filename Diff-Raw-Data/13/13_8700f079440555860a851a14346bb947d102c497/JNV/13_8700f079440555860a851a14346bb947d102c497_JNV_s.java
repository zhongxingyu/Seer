 package org.vinodkd.jnv;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.HashMap;
 import java.util.List;
 
 class JNV{
 	public static void main(String[] args) {
 		JNV jnv = new JNV();
 		Models logicalModels 	= jnv.createModels();
 		// Models viewModels 		= jnv.createViewModels(logicalModels);
 		HashMap<String,Component> ui = jnv.createUI(logicalModels);	// call getInitialState to build ui.
 		// ignoring the urge to overengineer with state machines for now.
 		jnv.addBehaviors(ui,logicalModels);
 		ui.get("window").setVisible(true);
 	}
 
 	public JNV(){}
 
 	public Models createModels(){
 		Notes notes = new Notes();
 		Models models = new Models();
 		models.add("notes",notes);
 		return models;
 	}
 
 	// public Models createViewModels(Models logicalModels){
 	// 	ViewModels models = new ViewModels();
 
 	// 	Model logicalNotes = logicalModels.get("notes");
 	// 	models.add("notetitle", new NoteTitle(logicalNotes));
 	// 	models.add("searchresults", new SearchResults(logicalNotes));
 	// 	models.add("notecontents", new NoteContents(logicalNotes));
 	// }
 
 
 	public HashMap<String,Component> createUI(Models models){
 		HashMap<String,Component> controls = new HashMap<String,Component>();
 
 		JTextField noteName = new JTextField();
 		noteName.setPreferredSize(new Dimension(500,25));
 		controls.put("noteName", noteName);
 
 		// should createUI know about model data? no. kludge for now.
 		@SuppressWarnings("unchecked")
 		HashMap<String,Note> notes = (HashMap<String,Note>)(models.get("notes").getInitialValue());
 		DefaultListModel<String> foundNotesModel = new DefaultListModel<String>();
 		for(String title:notes.keySet()){
 			foundNotesModel.addElement(title);
 		}
 
 		JList<String> foundNotes = new JList<String>(foundNotesModel);
 		foundNotes.setLayoutOrientation(JList.VERTICAL);
 		JScrollPane foundNotesScroller = new JScrollPane(foundNotes);
 		foundNotesScroller.setPreferredSize(new Dimension(500,150));
 		controls.put("foundNotes", foundNotes);
 
 		JTextArea noteContent = new JTextArea();
 		noteContent.setLineWrap(true);
 		noteContent.setWrapStyleWord(true);
 		JScrollPane noteContentScroller = new JScrollPane(noteContent);
 		noteContentScroller.setPreferredSize(new Dimension(500,400));
 		controls.put("noteContent", noteContent);
 
 		Box vbox = Box.createVerticalBox();
 		vbox.add(noteName);
 		vbox.add(foundNotesScroller);
 		vbox.add(noteContentScroller);
 
 		JFrame ui = new JFrame("jNV");
 		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		ui.setPreferredSize(new Dimension(520,600));
 
 		ui.add(vbox);
 
 		ui.pack();
 		controls.put("window", ui);
 		return controls;
 	}
 
 	private boolean SEARCHING = false;
 
 	public void addBehaviors(HashMap<String,Component> ui, final Models models){
 		final JTextField noteName = (JTextField)ui.get("noteName");
 		final JList foundNotes = (JList)ui.get("foundNotes");
 		final JTextArea noteContent = (JTextArea)ui.get("noteContent");
 		final JFrame window = (JFrame)ui.get("window");
 
 		final Notes notes = (Notes) models.get("notes");
 
 		noteName.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				SEARCHING = true;
 				String nName = noteName.getText();
 				List<String> searchResult = notes.search(nName);
 
 				// clear out list's model first regardless of search outcome.
 				@SuppressWarnings("unchecked")
 				DefaultListModel<String> fnModel = (DefaultListModel<String>)foundNotes.getModel();
 				fnModel.removeAllElements();
 				if(searchResult.isEmpty()){
 					noteContent.requestFocus();
 				}
 				else{
 					for(String title:searchResult){
 						fnModel.addElement(title);
 					}
 				}
 				SEARCHING = false;
 			}
 		}
 		);
 
 		foundNotes.addListSelectionListener(new ListSelectionListener(){
 			public void valueChanged(ListSelectionEvent e){
                 // when still in search mode, this event is triggered by elements being added/removed
                 // from the model. the title should not updated then.
                 if(!SEARCHING){
                     // set the note title to the selected value
                     String selectedNote = (String)foundNotes.getSelectedValue();
                     noteName.setText(selectedNote);
	                // now set the content to reflect the selection as well
	                setNoteContent(noteContent, notes, selectedNote);
                 }
 
 			}
 		});
 
 		noteContent.addKeyListener(new KeyAdapter(){
 			// this is from http://stackoverflow.com/a/5043957's 'Use a keylistener' solution
 			public void keyPressed(KeyEvent e){
                 if (e.getKeyCode() == KeyEvent.VK_TAB &&  e.isShiftDown()){
                     e.consume();
                     // fix for issue #6
                     saveIncremental();
                     KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
                 }
 			}
 		});
 		window.addWindowListener( new WindowAdapter(){
 			public void windowClosing(WindowEvent e){
 				saveIncremental();
 			}
 		}
 		);
 	}
 
	private void setNoteContent(JTextArea noteContent, Notes notes,String selectedNote){
         noteContent.selectAll();
        noteContent.replaceSelection(notes.get(selectedNote).getContents());
 
 	}
 	private void saveIncremental(){
 		System.out.println("saved!");
 	}
 }
