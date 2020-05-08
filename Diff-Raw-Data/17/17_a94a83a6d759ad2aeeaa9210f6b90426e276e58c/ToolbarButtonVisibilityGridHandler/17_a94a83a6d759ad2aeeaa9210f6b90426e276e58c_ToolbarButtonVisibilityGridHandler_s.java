 package org.iplantc.core.uidiskresource.client.presenters.handlers;
 
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.views.widgets.DiskResourceViewToolbar;
 
 /**
  * A ToolbarButtonVisibilitySelectionHandler for DiskResource selections in the Data Window main grid.
  * 
  * @author psarando
  * 
  */
 public class ToolbarButtonVisibilityGridHandler extends
         ToolbarButtonVisibilitySelectionHandler<DiskResource> {
 
     public ToolbarButtonVisibilityGridHandler(DiskResourceViewToolbar toolbar) {
         super(toolbar);
     }
 
     @Override
     protected void updateToolbar(List<DiskResource> selection) {
         boolean selectionEmpty = selection.isEmpty();
         boolean oneSelected = selection.size() == 1;
         boolean selectionInTrash = selectionInTrash(selection);
         boolean owner = !selectionEmpty && DiskResourceUtil.isOwner(selection);
 
         boolean canDownload = !selectionEmpty;
         boolean canSimpleDownload = canDownload && !DiskResourceUtil.containsFolder(selection);
         boolean canRename = oneSelected && owner && !selectionInTrash;
         boolean canShare = !selectionInTrash && DiskResourceUtil.hasOwner(selection);
        boolean canShareDataLink = canShare
                && !(oneSelected && DiskResourceUtil.containsFolder(selection));
         boolean canEditMetadata = oneSelected && owner && !selectionInTrash;
         boolean canDelete = owner;
         boolean canEdit = canRename || canDelete || canEditMetadata;
 
         toolbar.setDownloadsEnabled(canDownload);
         toolbar.setBulkDownloadButtonEnabled(canDownload);
         toolbar.setSimpleDowloadButtonEnabled(canSimpleDownload);
         toolbar.setRenameButtonEnabled(canRename);
         toolbar.setDeleteButtonEnabled(canDelete);
         toolbar.setShareButtonEnabled(canShare);
         toolbar.setShareMenuItemEnabled(canShare);
         toolbar.setDataLinkMenuItemEnabled(canShareDataLink);
         toolbar.setRestoreMenuItemEnabled(selectionInTrash);
         toolbar.setMetaDatMenuItemEnabled(canEditMetadata);
         toolbar.setEditEnabled(canEdit);
     }
 
     /**
      * Check if every selected item is under the Trash folder.
      * 
      * @param selection
      * @return
      */
     private boolean selectionInTrash(List<DiskResource> selection) {
         if (selection.isEmpty()) {
             return false;
         }
 
         String trashPath = UserInfo.getInstance().getTrashPath();
         for (DiskResource dr : selection) {
             if (dr.getId().equals(trashPath)) {
                 return false;
             }
 
             if (!dr.getId().startsWith(trashPath)) {
                 return false;
             }
         }
 
         return true;
     }
 }
