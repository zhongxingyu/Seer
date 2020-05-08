 package org.Zeitline.GUI.Action;
 
 import org.Zeitline.Event.ComplexEvent;
 import org.Zeitline.EventTree;
 import org.Zeitline.TimeEventTransferHandler;
 import org.Zeitline.Zeitline;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 /**
 * Created by IntelliJ IDEA.
 * User: Bart
 * Date: 14/02/12
 * Time: 19:30
 * To change this template use File | Settings | File Templates.
 */
 public class PasteAction extends AbstractAction {
 
     private Zeitline zeitline;
 
     public PasteAction(Zeitline zeitline, String text, ImageIcon icon, int mnemonic) {
         super(text, icon);
         this.zeitline = zeitline;
         putValue(MNEMONIC_KEY, new Integer(mnemonic));
         putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, ActionEvent.SHIFT_MASK));
         putValue(SHORT_DESCRIPTION, "Paste");
     } // PasteAction
 
     public void actionPerformed(ActionEvent e) {
 
         if (zeitline.getCutBuffer() == null)
             return;
 
         EventTree currentTree = zeitline.getTimelines().getCurrentTree();
         if (currentTree.getSelectionCount() != 1)
             return;
 
         ComplexEvent targetNode;
         try {
             targetNode = (ComplexEvent) currentTree.getSelectionPath().getLastPathComponent();
         } catch (ClassCastException ce) {
             return;
         }
 
         ((TimeEventTransferHandler) currentTree.getTransferHandler()).performPaste(zeitline.getCutBuffer(), targetNode);
 
        zeitline.setCutBuffer(null);
 
         zeitline.getSaveAction().setEnabled(true);
         zeitline.pasteAction.setEnabled(false);
 
     } // actionPerformed
 
     public boolean pastePossible() {
         return (zeitline.getCutBuffer() != null);
     } // pastePossible
 
 } // class PasteAction
