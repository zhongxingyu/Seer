 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.KeyStroke;
 
 /**
  * Close the currently open dialog.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class AbortAction extends AbstractAction {
 
     /**
      * Initializes the abort action.
      */
     public AbortAction() {
         putValue(Action.NAME, _("Abort"));
         putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                 ActionEvent.CTRL_MASK));
         putValue(Action.SHORT_DESCRIPTION, _("Abort the operation"));
     }
 
     /**
      * Closes the currently open dialog.
      */
     @Override
     public void actionPerformed(ActionEvent e) {
        ((JComponent) e.getSource()).getTopLevelAncestor().setVisible(false);
     }
 }
