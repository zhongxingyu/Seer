 package org.iplantc.core.uidiskresource.client.views.dialogs;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.models.CommonModelUtils;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uicommons.client.views.gxt3.dialogs.IPlantDialog;
 import org.iplantc.core.uidiskresource.client.gin.DiskResourceInjector;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView.Presenter;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.TakesValue;
 import com.google.gwt.user.client.ui.HasEnabled;
 import com.google.gwt.user.client.ui.HasValue;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.TextField;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
 import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
 
 /**
  * An <code>IPlantDialog</code> which wraps the standard <code>DiskResourceView</code> for file
  * selection.
  *
  * Users of this class are responsible adding hide handlers to get the selected file. FIXME JDS Needs to
  * support MultiSelect, TakesValue<List<String>>
  *
  * @author jstroot
  *
  */
 public class FileSelectDialog extends IPlantDialog implements TakesValue<List<File>> {
 
     private final DiskResourceView.Presenter presenter;
     private final TextField selectedFileField = new TextField();
     private List<File> selectedFileIds;
 
     public static FileSelectDialog singleSelect(List<DiskResource> diskResourcesToSelect) {
         return new FileSelectDialog(diskResourcesToSelect, true);
     }
 
     public static FileSelectDialog selectParentFolderById(String folderId, boolean singleSelect) {
         return new FileSelectDialog(folderId,singleSelect);
     }
 
     protected FileSelectDialog(String folderId, boolean singleSelect) {
         presenter = DiskResourceInjector.INSTANCE.getDiskResourceViewPresenter();
         init(singleSelect);
 
         HasId folderToSelect = null;
         if(folderId!= null) {
             folderToSelect = CommonModelUtils.createHasIdFromString(folderId);
         }
 
         presenter.go(this, folderToSelect, null);
     }
 
     protected FileSelectDialog(List<DiskResource> diskResourcesToSelect, boolean singleSelect) {
 
         presenter = DiskResourceInjector.INSTANCE.getDiskResourceViewPresenter();
         init(singleSelect);
 
         HasId folderToSelect = null;
         if (diskResourcesToSelect != null && !diskResourcesToSelect.isEmpty()) {
            String parentId = DiskResourceUtil.parseParent(diskResourcesToSelect.get(0).getPath());
             folderToSelect = CommonModelUtils.createHasIdFromString(parentId);
         }
 
         presenter.go(this, folderToSelect, diskResourcesToSelect);
     }
 
     
     public void cleanUp() {
         presenter.cleanUp();
     }
     
     @Override
     public void onHide(){
         cleanUp();
     }
     
     
     private void init(boolean singleSelect) {
 
         // Disable Ok button by default.
         getOkButton().setEnabled(false);
 
         setResizable(true);
         setSize("640", "480");
         setHeadingText(I18N.DISPLAY.selectAFile());
 
         final FieldLabel fl = new FieldLabel(selectedFileField, I18N.DISPLAY.selectedFile());
 
         selectedFileField
                 .addKeyUpHandler(new SelectedFileFieldKeyUpHandler(presenter, selectedFileField));
 
         presenter.getView().setSouthWidget(fl);
         presenter.addFileSelectChangedHandler(new FileSelectionChangedHandler(this, selectedFileField,
                 getOkButton()));
 
         // Tell the presenter to add the view with the north and east widgets hidden.
         DiskResourceView.Presenter.Builder b = presenter.builder().hideNorth().hideEast()
                 .disableFilePreview();
         if (singleSelect) {
             b.singleSelect();
         }
     }
 
     @Override
     public void setValue(List<File> value) {
         this.selectedFileIds = value;
     }
 
     @Override
     public List<File> getValue() {
         return selectedFileIds;
     }
 
     public Set<DiskResource> getDiskResources() {
         return presenter.getSelectedDiskResources();
     }
 
     private final class SelectedFileFieldKeyUpHandler implements KeyUpHandler {
         private final Presenter presenter;
         private final HasValue<String> hasValue;
 
         public SelectedFileFieldKeyUpHandler(final DiskResourceView.Presenter presenter,
                 final HasValue<String> hasValue) {
             this.presenter = presenter;
             this.hasValue = hasValue;
         }
 
         @Override
         public void onKeyUp(KeyUpEvent event) {
             if ((event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
                     || (event.getNativeKeyCode() == KeyCodes.KEY_DELETE)) {
                 presenter.deSelectDiskResources();
                 hasValue.setValue(null);
             } else {
                 event.preventDefault();
             }
 
         }
     }
     
     private final class FileSelectionChangedHandler implements SelectionChangedHandler<DiskResource> {
         private final HasValue<String> textbox;
         private final HasEnabled okButton;
         private final TakesValue<List<File>> dlg;
 
         private FileSelectionChangedHandler(final TakesValue<List<File>> dlg,
                 final HasValue<String> textBox, final HasEnabled okButton) {
             this.textbox = textBox;
             this.okButton = okButton;
             this.dlg = dlg;
         }
 
         @Override
         public void onSelectionChanged(SelectionChangedEvent<DiskResource> event) {
             // Disable the okButton
             okButton.setEnabled(false);
 
             if (event.getSelection() == null) {
                 return;
             }
 
             ArrayList<File> newArrayList = Lists.newArrayList(DiskResourceUtil.extractFiles(event
                     .getSelection()));
             dlg.setValue(newArrayList);
             List<String> idList = DiskResourceUtil.asStringIdList(newArrayList);
             String fileNames = DiskResourceUtil.asCommaSeperatedNameList(idList);
             textbox.setValue(fileNames);
 
             if (!Strings.isNullOrEmpty(fileNames)) {
                 // Enable the okButton
                 okButton.setEnabled(true);
             }
         }
     }
 
 }
