 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 
 import de.aidger.model.Runtime;
 import de.aidger.view.SettingsDialog;
 
 /**
  * Saves the settings in the dialog and closes it.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class SaveSettingsAction extends AbstractAction {
 
     /**
      * Initializes the save settings action.
      */
     public SaveSettingsAction() {
         putValue(Action.NAME, _("Save"));
         putValue(Action.SHORT_DESCRIPTION, _("Save settings"));
     }
 
     /**
      * Saves the settings in the configuration and closes the dialog.
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         SettingsDialog dlg = (SettingsDialog) ((JComponent) e.getSource())
                 .getTopLevelAncestor();
 
         // TODO: Add validation code
         Runtime.getInstance().setOption("name", dlg.getUsername());
         Runtime.getInstance().setOption("pdf-viewer", dlg.getPDFViewer());
 
         Runtime.getInstance().setOption(
                 "language",
                 Runtime.getInstance().getLanguages().get(
                         dlg.getSelectedLanguage()).fst());
 
         Runtime.getInstance().setOption("activities",
                 Integer.toString(dlg.getNumOfActivities()));
         Runtime.getInstance().setOption("auto-open",
                 Boolean.toString(dlg.isOpenSelected()));
         Runtime.getInstance().setOption("auto-save",
                 Boolean.toString(dlg.isSaveSelected()));
 
         dlg.setVisible(false);
        dlg.dispose();
     }
 }
