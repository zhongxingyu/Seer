 package edithistory;
 
 import java.io.Serializable;
 import javax.swing.UIManager;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.JTextArea;
 import javax.swing.JPanel;
 import java.awt.Component;
 import javax.swing.text.Document;
 import javax.swing.text.BadLocationException;
 import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 
 public class UndoController implements UndoableEditListener, Serializable
 {
     private static final long serialVersionUID = 1L;
     private CompoundEdit lastUndoEdit;
     private CompoundEdit toUndo;
     private CompoundEdit lastDisplayEdit;
     private transient UndoAction undoAction;
     private transient JTextArea textArea;
     private transient EditHistoryView view;
     private transient NodeComponent toCompound1;
     private transient NodeComponent toCompound2;
 
     public UndoController(JTextArea textArea)
     {
 	this.textArea = textArea;
	undoAction = new UndoAction(this);
 	lastUndoEdit = new CompoundEdit();
 	lastDisplayEdit = new CompoundEdit();
 	toUndo = lastUndoEdit;
 	view = new EditHistoryView(this);
 	view.addEdit(lastDisplayEdit);
     }
 
     @Override
 	public void undoableEditHappened(UndoableEditEvent e)
     {
 	addEdit(new MyUndoableEdit(e.getEdit()));
 	undoAction.updateUndoState();
     }
 
     public void addEdit(MyUndoableEdit e)
     {
 	if (!lastUndoEdit.addEdit(e, "undo")){
 	    lastUndoEdit = new CompoundEdit(e, lastUndoEdit);
 	}
 	if (!lastDisplayEdit.addEdit(e, "display")){
 	    lastDisplayEdit = new CompoundEdit(e, lastDisplayEdit);
 	    view.addEdit(lastDisplayEdit);
 	}
 	toUndo = lastUndoEdit;
     }
 
     public boolean canUndo()
     {
 	return (!toUndo.getType().equals("empty"));
     }
 
     public void undo()
     {
 	CompoundEdit currentToUndo = toUndo;
 	toUndo.undo(textArea.getDocument());
 	toUndo = currentToUndo.getParent();
     }
 
     public Document restoreTo(CompoundEdit edit)
     {
 	Document document = textArea.getDocument();
 	RSyntaxDocument restoredDocument = new RSyntaxDocument(SyntaxConstants.SYNTAX_STYLE_JAVA);
 	try {
 	    restoredDocument.insertString(0,document.getText(0, document.getLength()), null);
 	} 
 	catch (BadLocationException e1) {
 	}
 	CompoundEdit e = lastDisplayEdit;
 	while (e != edit){
 	    e.undo(restoredDocument);
 	    e = e.getParent();
 	}
 	return restoredDocument;
     }
 
     public void revert(CompoundEdit edit)
     {
 	int numComponents = view.getNodeComponents().length;
 	Document restored = restoreTo(edit);
 	try{
 	    textArea.replaceRange(restored.getText(0, restored.getLength()), 0, textArea.getDocument().getLength());
 	}
 	catch(BadLocationException ex){}
 	Component[] nodeComponents = view.getNodeComponents();
 	int numNewComponents = nodeComponents.length - numComponents;
 	if (numNewComponents > 1){
 	    view.compress((NodeComponent) nodeComponents[nodeComponents.length - numNewComponents],
 			  (NodeComponent) nodeComponents[nodeComponents.length - 1]);
 	}
     }
 
     public void selectNodeForCompound(NodeComponent node)
     {
 	if (toCompound1 == null)
 	    toCompound1 = node;
 	else if (toCompound2 == null){
 	    toCompound2 = node;
 	    view.compress(toCompound1, toCompound2);
 	    toCompound1.deselectForCompound();
 	    toCompound2.deselectForCompound();
 	    toCompound1 = null;
 	    toCompound2 = null;
 	}
     }
 
     public void deselectNodeForCompound(NodeComponent node)
     {
 	if (toCompound1 == node){
 	    toCompound1 = null;
 	}
     }
 
     /* Getters and Setters */
     public UndoAction getUndoAction()
     {
 	return undoAction;
     }
 
     public String getUndoPresentationName()
     {
 	return UIManager.getString("AbstractUndoaleEdit.undoText");
     }
 
     public EditHistoryView getView()
     {
 	return view;
     }
 
 }
