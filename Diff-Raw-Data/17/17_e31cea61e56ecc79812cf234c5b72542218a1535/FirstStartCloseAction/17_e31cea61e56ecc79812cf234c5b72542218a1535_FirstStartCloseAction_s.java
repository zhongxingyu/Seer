 package de.aidger.controller.actions;
 
 import de.aidger.view.FirstStartDialog;
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 
 
 /**
  * This action saves the name and closes the FirstStartDialog.
  *
  * @author aidGer Team
  */
 public class FirstStartCloseAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public FirstStartCloseAction() {
         putValue(Action.NAME, _("Close"));
         putValue(Action.SHORT_DESCRIPTION, _("Close the dialog"));
     }
 
     /*
      * (non-Javadoc)
      *
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
         FirstStartDialog dlg = (FirstStartDialog) ((JComponent) e.getSource())
             .getTopLevelAncestor();
 
         if (dlg.getEnteredName().isEmpty()) {
            UI.displayError(_("The name can't be empty"));
         } else {
             Runtime.getInstance().setOption("name", dlg.getEnteredName());
 
             dlg.setVisible(false);
             dlg.dispose();
         }
     }
 
 }
