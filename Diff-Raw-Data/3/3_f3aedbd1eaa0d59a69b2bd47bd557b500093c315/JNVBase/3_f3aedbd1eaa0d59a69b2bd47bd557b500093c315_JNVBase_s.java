 package org.vinodkd.jnv;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.*;
 import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Date;
 
 abstract class JNVBase{
 
 	public JNVBase(){
 		Models logicalModels 	= createModels();
 		// Models viewModels 		= jnv.createViewModels(logicalModels);
 		HashMap<String,Component> ui = createUI(logicalModels);	// call getInitialState to build ui.
 		// ignoring the urge to overengineer with state machines for now.
 		addBehaviors(ui,logicalModels);
 		ui.get("window").setVisible(true);
 	}
 
 	public Models createModels(){
 		NotesStore store = getStore();
 		store.setDir(System.getProperty("user.dir"));
 		Notes notes = new Notes(store);
 		Models models = new Models();
 		models.add("notes",notes);
 		return models;
 	}
 
 	public abstract NotesStore getStore();
 
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
 
 		NoteListTableModel foundNotesModel = new NoteListTableModel(notes);
 		JTable foundNotes = new JTable(foundNotesModel);
 		foundNotes.setFillsViewportHeight(true);
 		foundNotes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
 
 		JScrollPane foundNotesScroller = new JScrollPane(foundNotes);
 		foundNotesScroller.setPreferredSize(new Dimension(500,150));
 		controls.put("foundNotes", foundNotes);
 
 		JTextArea noteContent = new JTextArea();
 		noteContent.setLineWrap(true);
 		noteContent.setTabSize(4);
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
 	private int DOC_MOD_EVENT_COUNT = 0;
 	private final int EVENT_COUNT_TO_SAVE_AT = 20;
 
 	public void addBehaviors(HashMap<String,Component> ui, final Models models){
 		final JTextField noteName = (JTextField)ui.get("noteName");
 		final JTable foundNotes = (JTable)ui.get("foundNotes");
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
 				DefaultTableModel fnModel = (DefaultTableModel)foundNotes.getModel();
 				fnModel.setRowCount(0);
 				if(searchResult.isEmpty()){
 					noteContent.requestFocus();
 				}
 				else{
 					for(String title:searchResult){
 						fnModel.addRow(new Object[] {title, notes.get(title).getLastModified()});
 					}
 				}
 				SEARCHING = false;
 			}
 		}
 		);
 
 		foundNotes.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 			public void valueChanged(ListSelectionEvent e){
                 // when still in search mode, this event is triggered by elements being added/removed
                 // from the model. the title should not updated then.
                 if(!SEARCHING){
                     // set the note title to the selected value
                     String selectedNote = (String)foundNotes.getValueAt(foundNotes.getSelectedRow(),0);
                     noteName.setText(selectedNote);
                 }
                 // now set the content to reflect the selection as well
                 setNoteContent(noteContent, notes, foundNotes);
 			}
 		});
 
 		foundNotes.addKeyListener(new KeyAdapter(){
 			// this is from http://stackoverflow.com/a/5043957's 'Use a keylistener' solution
 			public void keyPressed(KeyEvent e){
                 if (e.getKeyCode() == KeyEvent.VK_TAB &&  e.isShiftDown()){
                     e.consume();
                     KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
                 }
 			}
 		});
 
 		noteContent.addKeyListener(new KeyAdapter(){
 			// this is from http://stackoverflow.com/a/5043957's 'Use a keylistener' solution
 			public void keyPressed(KeyEvent e){
                 if (e.getKeyCode() == KeyEvent.VK_TAB &&  e.isShiftDown()){
                     e.consume();
                     // fix for issue #6
                     saveIncremental(noteContent,noteName,notes);
                     KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
                 }
 			}
 		});
 
 		noteContent.getDocument().addDocumentListener(new DocumentListener(){
 		    public void insertUpdate(DocumentEvent e) {
 		        saveIfRequired(e);
 		    }
 		    public void removeUpdate(DocumentEvent e) {
 		        saveIfRequired(e);
 		    }
 		    public void changedUpdate(DocumentEvent e) {
 		        //Plain text components do not fire these events
 		    }
 		    //TODO: do both saveIfRequired()s need to be synchronized?
 		    private synchronized void saveIfRequired(DocumentEvent e){
 		        if(DOC_MOD_EVENT_COUNT == EVENT_COUNT_TO_SAVE_AT){
 		            saveIncremental(noteContent,noteName,notes);
 		            DOC_MOD_EVENT_COUNT = 0;
 		        }
 		        else{
 					DOC_MOD_EVENT_COUNT++;
 		        }
 		    }
 
 		});
 
 		window.addWindowListener( new WindowAdapter(){
 			public void windowClosing(WindowEvent e){
 				saveIncremental(noteContent,noteName,notes);
 			}
 		}
 		);
 	}
 
 	private void setNoteContent(JTextArea noteContent, Notes notes,JTable foundNotes){
 		int selectedRow = foundNotes.getSelectedRow();
 		String content = "";
 		if(selectedRow >=0 ){	// selection exists
 			String selectedNoteName = (String)foundNotes.getValueAt(selectedRow,0);
 			Note selectedNote = notes.get(selectedNoteName);
 			content = selectedNote != null ? selectedNote.getContents(): "";
 		}
         noteContent.selectAll();
         noteContent.replaceSelection(content);
         noteContent.setCaretPosition(0);
 
 	}
 	private void saveIncremental(JTextArea noteContent,JTextField noteName, Notes notes){
         Document doc = noteContent.getDocument();
         String title = noteName.getText();
         String text = "";
         try{
         	text = doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
         }catch(BadLocationException ble){
         	System.out.println("text exception:" + ble);
         }
 
         if( !"".equals(title) && !"".equals(text)){
             notes.set(title, text);
         }
         notes.store();
 	}
 }
