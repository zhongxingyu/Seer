 package org.iplantc.de.client.views.panels;
 
 import org.iplantc.core.uiapplications.client.I18N;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.google.gwt.user.client.ui.HTML;
 
 /**
  * 
  * A Folder Selector panel with additional text field to collect new name
  * 
  * @author sriram
  * 
  */
 
 public class SaveAsFolderSelectDialogPanel extends FolderSelectDialogPanel {
 
     private TextField<String> txtNewName;
 
     public SaveAsFolderSelectDialogPanel(Folder folder, String currentFolderId, String tag) {
         super(folder, currentFolderId, tag);
         navigationSelectionChangeListener = new SaveAsFolderSelectionChangeListener();
     }
 
     private class SaveAsFolderSelectionChangeListener implements Listener<BaseEvent> {
         @Override
         public void handleEvent(BaseEvent be) {
             setParentOkButton();
             DiskResource resource = pnlNavigation.getSelectedItem();
             if (resource instanceof Folder && resource != null) {
                 txtResourceName.setValue(resource.getId());
             } else {
                 // disable OK button if a folder is not selected
                 txtResourceName.setValue(""); //$NON-NLS-1$
             }
             updateOkButton();
         }
     }
 
     private void initNameField() {
        txtNewName = new TextField<String>();
        txtNewName.setWidth(300);
        txtNewName.addListener(Events.OnBlur, new Listener<BaseEvent>() {
             @Override
            public void handleEvent(final BaseEvent be) {
                 updateOkButton();
             }
        });
     }
 
     private void updateOkButton() {
         DiskResource resource = pnlNavigation.getSelectedItem();
         if (resource instanceof Folder && resource != null && getNewName() != null
                 && !getNewName().isEmpty()) {
             enableParentOkButton();
         } else {
             disableParentOkButton();
         }
     }
 
     /**
      * Initialize all components used by this widget.
      */
     @Override
     protected void initComponents(String lblStringSelectedResource) {
         super.initComponents(lblStringSelectedResource);
 
         Label lblNewName = new Label(I18N.DISPLAY.name() + ":");
         lblNewName.setWidth(85);
 
         HorizontalPanel pnlNewName = new HorizontalPanel();
 
         initNameField();
         pnlNewName.setSpacing(2);
         pnlNewName.add(lblNewName);
         pnlNewName.add(txtNewName);
 
         container.add(new HTML("<br/>"));
         container.add(pnlNewName);
 
         pnlNavigation.setMaskingParent(container);
     }
 
     /**
      * @param txtNewName the name to set
      */
     public void setNewName(String txtNewName) {
         this.txtNewName.setValue(txtNewName);
     }
 
     /**
      * @return the txtNewName
      */
     public String getNewName() {
         return txtNewName.getValue();
     }
 
 }
