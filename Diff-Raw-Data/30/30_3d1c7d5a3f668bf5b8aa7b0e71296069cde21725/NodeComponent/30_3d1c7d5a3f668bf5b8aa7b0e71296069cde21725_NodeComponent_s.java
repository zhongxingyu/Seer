 package org.bruno.edithistory;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BorderFactory;
import javax.swing.JPanel;
 import javax.swing.border.Border;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 
 /**
  * This class is a component that goes in the edit history view.
  * The red, green, orange, purple, and blue boxes in the edit history view
  * are NodeComponents.
  */
 public class NodeComponent extends JPanel {
     private static final long serialVersionUID = 1L;
     private UndoController undoController;
     private CompoundEdit edit;
     public static final Border thinBlackBorder = BorderFactory.createLineBorder(Color.black, 1, false);
     public static final Border thickBlackBorder = BorderFactory.createLineBorder(Color.black, 3, false);
     public static final Border thickBlueBorder = BorderFactory.createLineBorder(Color.blue, 3, false);
     public static final int brightness = 235;
     private boolean selectedForCompound;
 
     public NodeComponent(UndoController uc) {
 	undoController = uc;
 	setBorder(thinBlackBorder);
 	setOpaque(true);
 	selectedForCompound = false;
 
 	addMouseListener(new MouseAdapter() {
 		@Override
 		    public void mouseEntered(MouseEvent e) {
 		    NodeComponent.this.mouseEntered();
 		}
 
 		@Override
 		    public void mouseClicked(MouseEvent e) {
 		    NodeComponent.this.mouseClicked(e);
 		}
 	    });
     }
 
     public NodeComponent(CompoundEdit edit, UndoController undoController) {
 	this(undoController);
 	this.edit = edit;
 	setColor();
     }
 
     /**
      * Selects this NodeComponent when the mouse enters it.
      */
     public void mouseEntered() {
 	EditHistoryView view = undoController.getView();
 	if (view.getSelectedNode() != null){
 	    view.getSelectedNode().unselectedBorder();
 	}
 	selectedBorder();
 	view.setSelectedNode(this);
 	undoController.getView().setDocument(getDocument());
     }
     
     /**
      * Double click reverts, right click selects for compression.
      */
     public void mouseClicked(MouseEvent e) {
 	int modifiers = e.getModifiers();
 	if (e.getClickCount() == 1 && (modifiers == 4 || modifiers == 18)) {
 	    changeSelectionForCompound();
 	}
 	else if (e.getClickCount() == 2 && modifiers == 16){
 	    undoController.revert(getEdit());
 	}
     }
     
     @Override
 	public Dimension getPreferredSize() {
 	Dimension d = super.getPreferredSize();
 	return new Dimension((int) d.getWidth(), 50);
     }
 
     @Override
 	public Dimension getMaximumSize() {
 	return new Dimension(5000000, 50);
     }
 
     /**
      * Sets the background color of this node appropriately.
      */
     public void setColor() {
 	if (edit.getComment() != null && !edit.getComment().equals("")){
 	    setBackground(Color.orange);
 	}
 	else if (edit.getType().equals("addition")) {
 	    setBackground(new Color(0, brightness, 0));
 	}
 	else if (edit.getType().equals("deletion")) {
 	    setBackground(new Color(brightness, 0, 0));
 	} 
 	else if (edit.getType().equals("empty")){
 	    setBackground(Color.gray);
 	}
 	if (edit.getIsRevert()){
 	    setBackground(new Color(brightness, 0, brightness));
 	}
 	else if (edit.getIsMask()){
 	    setBackground(new Color(0, 0, brightness));
 	}
     }
 
     /**
      * Adds the appropriate border for a selected node.
      */
     public void selectedBorder()
     {
 	if (!selectedForCompound)
 	    setBorder(thickBlackBorder);
 	else
 	    setBorder(thickBlueBorder);
     }
 
     /**
      * Adds the appropriate border for an unselected node.
      */
     public void unselectedBorder()
     {
 	if (!selectedForCompound)
 	    setBorder(thinBlackBorder);
 	else
 	    setBorder(thickBlueBorder);
     }
 
     public void setComment(String comment)
     {
 	edit.setComment(comment);
 	setColor();
     }
 
     public String getComment()
     {
 	return edit.getComment();
     }
 
     public Document getDocument()
     {
 	return undoController.restoreTo(edit);
     }
 
     public UndoController getUndoController() {
 	return undoController;
     }
 
     public CompoundEdit getEdit()
     {
 	return edit;
     }
 
     public boolean isSelectedForCompound() {
 	return selectedForCompound;
     }
 
     public void selectForCompound() {
 	if (!isSelectedForCompound()) {
 	    selectedForCompound = true;
 	    setBorder(thickBlueBorder);
 	    undoController.selectNodeForCompound(this);
 	}
     }
 
     public void deselectForCompound() {
 	if (isSelectedForCompound()) {
 	    selectedForCompound = false;
 	    undoController.deselectNodeForCompound(this);
 	    setBorder(thinBlackBorder);
 	}
     }
 
     public void changeSelectionForCompound() {
 	if (isSelectedForCompound()) {
 	    deselectForCompound();
 	} 
 	else
 	    selectForCompound();
     }
 
 }
