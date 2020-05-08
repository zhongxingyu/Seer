 package ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.ImageIcon;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ToolTipManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.StyledEditorKit;
 
 import ui.annotations.EditorActionInfo;
 import ui.newgui.AbstractEditorComponent;
 import ui.newgui.DefaultEditorAction;
 import ui.newgui.EditorAction;
 import ui.newgui.EditorComponent;
 
 import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
 
 public class SourceFile extends AbstractEditorComponent implements
 		EditorComponent {
 
 	static private int num = 0;
 
	private JScrollPane scrollPane;
 
 	private CompoundEditorPane editorPane;
 
 	private String filename;
 
 	// private String title;
 
 	// private LinkedList<DefaultEditorAction> actions;
 	//
 	// private Hashtable myactions;
 
 	private boolean modified;
 
 	private Stack<String> undohistory;
 
 	private Stack<String> redohistory;
 
 	private DocumentListener doclistener;
 
 	public SourceFile() {
 		super("Source");
 		// mypanel = new JPanel();
 		// actions = new LinkedList<DefaultEditorAction>();
 		// myactions = new Hashtable();
 		undohistory = new Stack<String>();
 		redohistory = new Stack<String>();
 
 		undohistory.push("");
 
 		// TODO use an update manager
 		doclistener = new DocumentListener() {
 			public void insertUpdate(DocumentEvent arg0) {
 				if (!SourceFile.this.isModified()) {
 					SourceFile.this.setModified(true);
 					SourceFile.this.setFilename("*" + filename);
 				}
 				// System.out.println("Part of document added.");
 				try {
 					String doctext = arg0.getDocument().getText(0,
 							arg0.getDocument().getLength());
 					// if (doctext.length() - history.peek().length() > 5
 					// || doctext.length() - history.peek().length() < -5) {
 					undohistory.push(doctext);
 					// }
 					setActionStatus("Undo", true);
 					setActionStatus("Redo", false);
 					redohistory.clear();
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			public void removeUpdate(DocumentEvent arg0) {
 				if (!SourceFile.this.isModified()) {
 					SourceFile.this.setModified(true);
 					SourceFile.this.setFilename("*" + filename);
 				}
 				// System.out.println("Part of document removed.");
 				try {
 					undohistory.push(arg0.getDocument().getText(0,
 							arg0.getDocument().getLength()));
 					setActionStatus("Undo", true);
 					setActionStatus("Redo", false);
 					redohistory.clear();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			public void changedUpdate(DocumentEvent arg0) {
 				if (!SourceFile.this.isModified()) {
 					SourceFile.this.setModified(true);
 					SourceFile.this.setFilename("*" + filename);
 				}
 			}
 		};
 
 		this.setLayout(new BorderLayout());
 		// this.title = "Source";
 		this.filename = "newfile" + num + ".ml";
 		num++;
 //		this.scrollPane = new JScrollPane();
 		this.editorPane = new CompoundEditorPane();
 		this.editorPane.setName(filename);
 //		this.scrollPane.setViewportView(editorPane);
 //		this.scrollPane.setName(filename);
 		((MLStyledDocument) editorPane.getEditorPane ().getDocument())
 				.addDocumentListener(doclistener);
 		this.add(editorPane, BorderLayout.CENTER);
 
 		ToolTipManager.sharedInstance().registerComponent(this.editorPane.getEditorPane());
 		// generateActions();
 
 		setActionStatus("Undo", false);
		// setActionStatus("Redo", false);
 	}
 
 	@EditorActionInfo(visible = false, name = "Undo", icon = "icons/undo.gif", accelModifiers = KeyEvent.CTRL_MASK, accelKey = KeyEvent.VK_Z)
 	public void handleUndo() {
 		try {
 			getDocument().removeDocumentListener(doclistener);
 			if (undohistory.peek().equals("")) {
 				setActionStatus("Undo", false);
 				redohistory.push(getDocument().getText(0,
 						getDocument().getLength()));
 				getDocument().remove(0, getDocument().getLength());
 				getDocument().insertString(0, undohistory.peek(), null);
 			} else {
 				if (undohistory.peek().equals(
 						getDocument().getText(0, getDocument().getLength()))) {
 					undohistory.pop();
 					handleUndo();
 					return;
 				} else {
 					redohistory.push(getDocument().getText(0,
 							getDocument().getLength()));
 					getDocument().remove(0, getDocument().getLength());
 					getDocument().insertString(0, undohistory.pop(), null);
 				}
 			}
 			getDocument().addDocumentListener(doclistener);
 			setActionStatus("Redo", true);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@EditorActionInfo(visible = false, name = "Redo", icon = "icons/redo.gif", accelModifiers = KeyEvent.VK_UNDEFINED, accelKey = KeyEvent.VK_UNDEFINED)
 	public void handleRedo() {
 		try {
 			if (redohistory.size() > 0) {
 				getDocument().removeDocumentListener(doclistener);
 				getDocument().remove(0, getDocument().getLength());
 				getDocument().insertString(0, redohistory.pop(), null);
 				getDocument().addDocumentListener(doclistener);
 				undohistory.push(getDocument().getText(0,
 						getDocument().getLength()));
 				setActionStatus("Undo", true);
 				if (redohistory.size() == 0)
 					setActionStatus("Redo", false);
 			} else {
 				setActionStatus("Redo", false);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setName(String name) {
 		this.filename = name;
 //		this.scrollPane.setName(this.filename);
 	}
 
 	public String getFilename() {
 		return this.filename;
 	}
 
	public Component getComponent() {
		return this.scrollPane;
	}
 
 	public MLStyledDocument getDocument() {
 		return (MLStyledDocument) editorPane.getEditorPane ().getDocument();
 	}
 
 	// public List<EditorAction> getActions() {
 	// List<EditorAction> tmp = new LinkedList<EditorAction>();
 	// tmp.addAll(actions);
 	// return tmp;
 	// }
 
 	// public String getTitle() {
 	//
 	// return title;
 	// }
 
 	// public Component getDisplay() {
 	// return getComponent();
 	// }
 
 	public void setFilename(String filename) {
 		String filenameold = this.filename;
 		this.filename = filename;
 		firePropertyChange("filename", filenameold, filename);
 	}
 
 	public boolean isModified() {
 		return modified;
 	}
 
 	public void setModified(boolean modified) {
 		boolean modifiedold = this.modified;
 		this.modified = modified;
 		firePropertyChange("modified", modifiedold, modified);
 	}
 
 	// public void setTitle(String title) {
 	// this.title = title;
 	// }
 
 }
