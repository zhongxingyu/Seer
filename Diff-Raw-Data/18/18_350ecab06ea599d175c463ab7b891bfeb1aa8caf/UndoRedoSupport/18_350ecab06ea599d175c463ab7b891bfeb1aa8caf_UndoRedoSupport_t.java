 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.tools.gui.jtextcompoonent.undoredo;
 
 import java.awt.Event;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.table.TableModel;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import javax.swing.undo.UndoManager;
 
 /**
  * A class that adds undo/redo support to JTextComponents.
  * 
  * @author srichter
  */
 public class UndoRedoSupport {
 
     private UndoRedoSupport() {
     }
 
     /**
      * Add undo/redo support to the given JTextComponent.
      * 
      * Maps: Undo -> CONTROL-Z
      *       Redo -> CONTROL-Y
      * 
      * @param the JTextComponent to decorate with undo/redo support.
      */
     public static void decorate(JTextComponent c) {
         final Document doc = c.getDocument();
         final UndoManager undo = new UndoManager();
         doc.addUndoableEditListener(undo);
         addCommandsToComponent(c, undo);
     }
     
     /**
      * 
      * DANGER: might not work with sorted Tables!
      * @param c
      * @deprecated
      */
     @Deprecated
     public static void decorate(JTable c) {
         final TableModel mo = c.getModel();
         final UndoableTableModel umo = new UndoableTableModel(mo);
         final UndoManager undo = new UndoManager();
         umo.addUndoableEditListener(undo);
         c.setModel(umo);
         addCommandsToComponent(c, undo);
     }
 
     private static void addCommandsToComponent(JComponent c, UndoManager undo) {
         final InputMap im = c.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         UndoAction ua = new UndoAction(undo);
         final KeyStroke controlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
        im.put(controlZ, "control z");  //NOI18N
        c.getActionMap().put("control z", ua);  //NOI18N
         final RedoAction ra = new RedoAction(undo);
         final KeyStroke controlY = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK);
        im.put(controlY, "control y");  //NOI18N
        c.getActionMap().put("control y", ra);  //NOI18N
     }
 
     public static final void discardAllEdits(JTextComponent c) {
        Action aa = c.getActionMap().get("control z");  //NOI18N
         if (aa != null && aa instanceof ManagerAction) {
             ((ManagerAction) aa).discardAllEdits();
         }
        aa = c.getActionMap().get("control y");  //NOI18N
         if (aa != null && aa instanceof ManagerAction) {
             ((ManagerAction) aa).discardAllEdits();
         }
     }
 
     private static abstract class ManagerAction extends AbstractAction {
 
         public ManagerAction(UndoManager undo) {
             if (undo == null) {
                throw new IllegalArgumentException("UndoManager can not be null for ManagerAction construction!");  //NOI18N
             }
             this.undo = undo;
         }
         private final UndoManager undo;
 
         public void discardAllEdits() {
             getUndo().discardAllEdits();
         }
 
         public UndoManager getUndo() {
             return undo;
         }
     }
 
     /**
      * An Undo action on the given UndoManager.
      * @author srichter
      */
     private static class UndoAction extends ManagerAction {
 
         public UndoAction(UndoManager undo) {
             super(undo);
         }
 
         public void actionPerformed(ActionEvent e) {
             if (getUndo().canUndo()) {
                 getUndo().undo();
             }
         }
     }
 
     /**
      * A Redo action on the given UndoManager.
      * 
      * @author srichter
      */
     private static class RedoAction extends ManagerAction {
 
         public RedoAction(UndoManager undo) {
             super(undo);
         }
 
         public void actionPerformed(ActionEvent e) {
             if (getUndo().canRedo()) {
                 getUndo().redo();
             }
         }
     }
 }
 
