 package edithistory;
 
 import java.util.List;
 import java.util.ArrayList;
 import javax.swing.undo.UndoableEdit;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
 
 import com.google.common.collect.Lists;
 
 public class TextEdit extends Edit
 {
     private List<UndoableEdit> edits;
     private boolean undone;
 
     public TextEdit(UndoableEdit e, Edit parent)
     {
 	super(parent);
 	edits = new ArrayList<>();
 	edits.add(e);
 	setType(e.getPresentationName());
 	setSize(Edit.editSize(e));
 	undone = false;
     }
 
     @Override
 	public boolean canUndo()
     {
 	if (!undone())
 	    return true;
 	else
 	    return getParent().canUndo();
     }
 
     @Override
 	public boolean canRedo()
     {
 	return undone();
     }
 
     @Override
 	public boolean undone()
     {
 	return undone;
     }
 
     @Override
 	public void undo(Edit edit)
     {
 	if (!undone()){
 	    for (UndoableEdit e : Lists.reverse(edits)){
 		e.undo();
 	    }
 	    undone = true;
 	    edit.setType(Edit.oppositeType(getType()));
 	    edit.setSize(getSize());
 	    edit.setAffectedEdit(this);
 	}
 	else{
 	    getParent().undo(edit);
 	}
     }
 
     @Override
 	public void redo(Edit edit)
     {
	if (undone() && (!(getParent() instanceof TextEdit) || !getParent().undone())){ //THIS INSTANCEOF IS A PROBLEM! THEY WERE RIGHT
 	    for (UndoableEdit e : edits){
 		e.redo();
 	    }
 	    undone = false;
 	    edit.setType(getType());
 	    edit.setSize(getSize());
 	    edit.setAffectedEdit(this);
 	}
 	else{
 	    getParent().redo(edit);
 	}
     }
  
     @Override
 	public void backInTime()
     {
 	if (!undone()){
 	    for (UndoableEdit e : Lists.reverse(edits)){
 		e.undo();
 	    }
 	    undone = true;
 	}
 	else{
 	    for (UndoableEdit e : edits){
 		e.redo();
 	    }
 	    undone = false;
 	}
     }
   
     @Override
 	public void forwardInTime()
     {
 	backInTime();
     }
 
     @Override
 	public boolean addEdit(UndoableEdit e)
     {
 	String changedText = Edit.changedText(e);
 	int size = Edit.editSize(e);
 	if (size > 1)
 	    return false;
 	else if (getSize() >= 5 && (changedText.equals(" ") ||
 			       changedText.equals("\n") ||
 			       changedText.equals("\t"))){
 	    return false;
 	}
 	else if (!getType().equals(e.getPresentationName()))
 	    return false;
 	else{
 	    edits.add(e);
 	    setSize(getSize() + size);
 	    return true;
 	}
     }
 
     @Override
 	public int getLocation()
     {
 	AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edits.get(0);
 	return event.getOffset();
     }
 	
 }
