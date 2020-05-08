 package cytoscape.util;
 
 import cytoscape.Cytoscape;
 
 import javax.swing.undo.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.undo.*;
 
 public class UndoManager {
 
   private  javax.swing.undo.UndoManager undo;
   private  UndoAction undoAction;
   private  RedoAction redoAction;
 
   public UndoManager ( cytoscape.view.CyMenus menus ) {
   
     
     undo = new javax.swing.undo.UndoManager();
     undoAction = new UndoAction();
     redoAction = new RedoAction();
     
     JMenuItem undoItem = new JMenuItem( undoAction );
     JMenuItem redoItem = new JMenuItem( redoAction );
 
     undoItem.setAccelerator(  javax.swing.KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_Z,
                                                                   ActionEvent.CTRL_MASK ) );
     redoItem.setAccelerator(  javax.swing.KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_Y,
                                                                   ActionEvent.CTRL_MASK ) );
 
     menus.getMenuBar().getMenu( "Edit" ).add( undoItem );
     menus.getMenuBar().getMenu( "Edit" ).add( redoItem );
 
     System.out.println( "UndoManager initialized" );
 
   }
 
   public  void addEdit ( UndoableEdit edit ) {
     undo.addEdit( edit );
     undoAction.update();
     redoAction.update();
   }
 
   protected class UndoAction extends AbstractAction {
     public UndoAction() {
 	    super("Undo");
 	    setEnabled(false);
     }
 
     public void actionPerformed(ActionEvent e) {
 	    try {
         undo.undo();
 	    } catch (CannotUndoException ex) {
         //System.out.println("Unable to undo: " + ex);
        //ex.printStackTrace();
 	    }
 	    update();
 	    redoAction.update();
     }
 
     protected void update() {
 	    if(undo.canUndo()) {
         setEnabled(true);
         putValue(Action.NAME, undo.getUndoPresentationName());
 	    }
 	    else {
         setEnabled(false);
         putValue(Action.NAME, "Undo");
 	    }
     }
   }
 
   protected class RedoAction extends AbstractAction {
     public RedoAction() {
 	    super("Redo");
 	    setEnabled(false);
     }
 
     public void actionPerformed(ActionEvent e) {
 	    try {
         undo.redo();
 	    } catch (CannotRedoException ex) {
         //System.out.println("Unable to redo: " + ex);
        //ex.printStackTrace();
 	    }
 	    update();
 	    undoAction.update();
     }
 
     protected void update() {
 
       System.out.println( "REDO: "+undo.canRedo() );
 
 	    if(undo.canRedo()) {
         setEnabled(true);
         putValue(Action.NAME, undo.getRedoPresentationName());
 	    }
 	    else {
         setEnabled(false);
         putValue(Action.NAME, "Redo");
 	    }
     }
   }
   
   
 
 }
