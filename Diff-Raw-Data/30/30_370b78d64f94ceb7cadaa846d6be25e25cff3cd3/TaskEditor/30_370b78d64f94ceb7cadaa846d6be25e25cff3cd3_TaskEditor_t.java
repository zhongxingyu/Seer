/* TaskEditor.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import javax.swing.JTextArea;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 /**
  * Class that implements an editor component for editing tasks in Task Mistress.
  * @author anonpds <anonpds@gmail.com>
  */
 @SuppressWarnings("serial")
 public class TaskEditor extends JTextArea implements DocumentListener {
 	/** Tells whether the editor has changed since it was intialised. */
 	private boolean changed;
 
 	/** Default constructor. */
 	public TaskEditor() {
 		/* enable word wrapping */
 		this.setWrapStyleWord(true);
 		this.setLineWrap(true);
 		
 		/* disable editing by default */
 		this.close(null);
 		
 		/* add listener for text changes */
 		this.getDocument().addDocumentListener(this);
 		
 		/* no changes */
 		this.changed = false;
 	}
 
 	/**
 	 * Tells if the editor is open or closed.
 	 * @return true is the editor is open, false if closed
 	 */
 	public boolean isOpen() {
 		return this.isEditable();
 	}
 
 	/**
 	 * Tells whether the editor has changed since initialisation.
 	 * @return true if the editor has changed, false if not
 	 */
 	public boolean hasChanged() {
 		return this.changed;
 	}
 	
 	/**
 	 * Opens the editor by setting the initial text and making the text editable.
 	 * @param text the initial text of the editor
 	 */
 	public void open(String text) {
 		super.setText(text);
 		this.setEditable(true);
 		this.changed = false;
 	}
 	
 	/**
 	 * Closes the editor by making it non-editable and optionally setting a new text for it.
 	 * @param text
 	 */
 	public void close(String text) {
 		if (text != null) this.setText(text);
 		this.setEditable(false);
 		this.changed = false;
 	}
 
 	/**
 	 * Listens to text changes in the editor.
 	 * @param e the change event
 	 */
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		this.changed = true;
 	}
 
 	/**
 	 * Listens to text inserts in the editor.
 	 * @param e the change event
 	 */
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		this.changed = true;
 	}
 
 	/**
 	 * Listens to text removals in the editor.
 	 * @param e the change event
 	 */
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		this.changed = true;
 	}
 }
