/* Task.java - Part of Task Mistress
  * Written in 2012 by anonymous.
  * 
  * To the extent possible under law, the author(s) have dedicated all copyright and related and neighbouring rights to
  * this software to the public domain worldwide. This software is distributed without any warranty.
  * 
  * Full license at <http://creativecommons.org/publicdomain/zero/1.0/>.
  */
 
 package anonpds.TaskMistress;
 
 import java.awt.BorderLayout;
 import java.text.DateFormat;
 import java.util.Date;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 /**
  * Implements a component that displays task editor and information about the currently edited task.
  * 
  * Currently the view consists of the following arrangement of components, laid out inside the TaskView itself with
  * BorderLayout:
  * 
  * <ul>
  *   <li>statusBar: JLabel component that displays the status of the Task (BorderLayout.NORTH)</li>
  *   <li>editor: TaskEditor component wrapped in JScrollPane; displays the task editor (BorderLayout.CENTER)</li>
  * </ul>
  * 
  * @author anonpds <anonpds@gmail.com>
  */
 @SuppressWarnings("serial")
 public class TaskView extends JPanel implements DocumentListener {
 	/** Label that displays the Task status. */
 	private JLabel statusBar;
 	
 	/** The editor, which may be used to edit tasks. */
 	private TaskEditor editor;
 
 	/** The currently displayed Task. */
 	private Task task;
 
 	/** Indicates whether the editor text has changed since it was loaded from the task. */
 	private boolean dirty;
 
 	/**
 	 * Constructs the TaskView.
 	 */
 	public TaskView() {
 		/* build the user interface */
 		this.setLayout(new BorderLayout());
 		this.statusBar = new JLabel("No task selected.");
 		this.editor = new TaskEditor();
 		this.add(this.statusBar, BorderLayout.NORTH);
 		this.add(new JScrollPane(this.editor), BorderLayout.CENTER);
 		
 		/* set the document listener to the editor to watch for changes */
 		this.editor.getDocument().addDocumentListener(this);
 	}
 
 	/**
 	 * Returns the currently displayed task.
 	 * @return the currently displayed task or null if no task displayed 
 	 */
 	public Object getTask() {
 		return this.task;
 	}
 	
 	/**
 	 * Sets the Task that is displayed in the TaskView.
 	 * @param task the Task to display
 	 */
 	public void setTask(Task task) {
 		if (task == null) {
 			this.task = null;
 			this.statusBar.setText("No task selected.");
 			this.editor.close("");
 		} else {
 			this.task = task;
 			this.editor.open(this.task.getText());
 			this.setDirty(false);
 			this.updateStatus();
 		}
 	}
 
 	/** Updates the status bar text. */
 	public void updateStatus() {
 		/* has the task been changed since last save */
 		String edited = "";
 		if (this.isDirty()) edited = " (changed)";
 
 		/* creation date formatting */
 		Date date = new Date(this.task.getCreationTime());
 		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 		
 		/* set the text */
 		this.statusBar.setText("Created: " + format.format(date) + edited);
 	}
 
 	/** Handles changes in the editor component; sets the dirty flag and updates the status text. */
 	public void editorChanged() {
 		if (!this.isDirty()) {
 			this.setDirty(true);
 			this.updateStatus();
 		}
 	}
 
 	/** Updates the Task text from the editor. */ 
 	public void updateText() {
 		if (this.isDirty())	this.task.setText(this.editor.getText());
 	}
 
 	/**
 	 * Tells whether the task text has been edited since the editor was initialised.
 	 * @return true if the text has changed, false if not
 	 */
 	private boolean isDirty() {
 		return this.dirty; 
 	}
 
 	/**
 	 * Sets the dirty status of the task text.
 	 * @param dirty the new dirty status (true if the text has changed, false if not)
 	 */
 	private void setDirty(boolean dirty) {
 		this.dirty = dirty; 
 	}
 
 	/**
 	 * Handles the event of the text change in the editor.
 	 * @param e the document event
 	 */
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		this.editorChanged();
 	}
 
 	/**
 	 * Handles the event of the text insertion in the editor.
 	 * @param e the document event
 	 */
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		this.editorChanged();
 	}
 
 	/**
 	 * Handles the event of the text removal in the editor.
 	 * @param e the document event
 	 */
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		this.editorChanged();
 	}
 }
