 package org.iplantc.core.uidiskresource.client.views.dialogs;
 
 import org.iplantc.core.uicommons.client.models.UserSettings;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uidiskresource.client.I18N;
 import org.iplantc.core.uidiskresource.client.gin.DiskResourceInjector;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 
 import com.google.common.base.Strings;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.ui.HasEnabled;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.TextField;
 
 public class SaveAsDialog extends IPlantDialog {
 
     private final DiskResourceView.Presenter presenter;
     private final TextField selectedFolderField = new TextField();
     private final TextField fileNameField = new TextField();
    private final Folder selectedFolder = null;
 
     public SaveAsDialog() {
         // Get a reference to the OK button, and disable it by default.
         TextButton okButton = null;
         Widget w = getButtonBar().getItemByItemId(PredefinedButton.OK.name());
         if ((w != null) && (w instanceof TextButton)) {
             okButton = (TextButton)w;
             okButton.setEnabled(false);
         }
 
         setResizable(true);
         setSize("480", "425");
         setHeadingText(I18N.DISPLAY.saveAs());
 
         presenter = DiskResourceInjector.INSTANCE.getDiskResourceViewPresenter();
 
         final FieldLabel fl1 = new FieldLabel(selectedFolderField, I18N.DISPLAY.selectedFolder());
         final FieldLabel fl2 = new FieldLabel(fileNameField, I18N.DISPLAY.fileName());
         fileNameField.addValueChangeHandler(new FileNameValueChangeHandler(okButton, selectedFolderField));
 
         VerticalLayoutContainer vlc = new VerticalLayoutContainer();
         vlc.add(fl1, new VerticalLayoutData(1, -1));
         vlc.add(fl2, new VerticalLayoutData(1, -1));
 
         presenter.getView().setSouthWidget(vlc, 60);
         presenter.addFolderSelectionHandler(new FolderSelectionChangedHandler(selectedFolderField, okButton, fileNameField));
 
         // Tell the presenter to add the view with the north, east, and center widgets hidden.
         // presenter.go(this, false, true, true, true);
         presenter.builder().hideNorth().hideCenter().hideEast().singleSelect().go(this);
 
        // FIXME JDS THE CODE BELOW NEEDS TO BE INTEGRATED INTO DISK Resources classes.

        // // if not refresh and currently nothing was selected and remember path is enabled, the go
        // // back to last back
         UserSettings instance = UserSettings.getInstance();
         String id = instance.getDefaultFileSelectorPath();
         boolean remember = instance.isRememberLastPath();
         if (remember && id != null && !id.isEmpty()) {
             presenter.setSelectedFolderById(id);
         }
 
     }
 
     private final class FileNameValueChangeHandler implements ValueChangeHandler<String> {
         private final HasEnabled okButton;
         private final HasValue<String> selectedFolderField;
 
         public FileNameValueChangeHandler(final HasEnabled okButton, final HasValue<String> selectedFolderField) {
             this.okButton = okButton;
             this.selectedFolderField = selectedFolderField;
         }
 
         @Override
         public void onValueChange(ValueChangeEvent<String> event) {
             okButton.setEnabled(!Strings.isNullOrEmpty(event.getValue()) 
                     && !Strings.isNullOrEmpty(selectedFolderField.getValue()));
         }
     }
 
     private final class FolderSelectionChangedHandler implements SelectionHandler<Folder> {
         private final HasValue<String> textBox;
         private final HasEnabled okButton;
         private final HasValue<String> fileNameTextBox;
 
         private FolderSelectionChangedHandler(final HasValue<String> folderTextBox, final HasEnabled okButton, final HasValue<String> fileNameTextBox) {
             this.textBox = folderTextBox;
             this.okButton = okButton;
             this.fileNameTextBox = fileNameTextBox;
         }
 
         @Override
         public void onSelection(SelectionEvent<Folder> event) {
             if (event.getSelectedItem() == null) {
                 // Disable the okButton
                 okButton.setEnabled(false);
                 return;
             }
            Folder diskResource = event.getSelectedItem();
            textBox.setValue(diskResource.getName());
             // Enable the okButton
             okButton.setEnabled(!Strings.isNullOrEmpty(fileNameTextBox.getValue()));
         }
     }
 
     public String getFileName() {
         return fileNameField.getCurrentValue();
     }
 
     public Folder getSelectedFolder() {
         return selectedFolder;
     }
 
 
 
 }
