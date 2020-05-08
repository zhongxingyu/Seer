 package de.aidger.controller.actions.masterdata;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import de.aidger.view.UI;
 import de.aidger.view.tabs.MasterDataEditorTab;
 import de.aidger.view.tabs.MasterDataViewerTab;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * This action saves the master data and replaces the current tab with the
  * master data viewer tab.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class MasterDataEditorSaveAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public MasterDataEditorSaveAction() {
         putValue(Action.NAME, _("Save"));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         JButton btnSave = (JButton) e.getSource();
         JPanel buttons = (JPanel) btnSave.getParent();
         MasterDataEditorTab tab = (MasterDataEditorTab) buttons.getParent();
 
         if (!tab.isEditMode()) {
             tab.createModel();
         }
 
         tab.setModel();
 
         try {
            if (!tab.getModel().save()) {
                UI.displayError(_("Validation failed."));
            }
         } catch (AdoHiveException e1) {
             UI.displayError(_("Could not save the model to database."));
         }
 
         UI.getInstance().replaceCurrentTab(
                 new MasterDataViewerTab(tab.getType()));
     }
 }
