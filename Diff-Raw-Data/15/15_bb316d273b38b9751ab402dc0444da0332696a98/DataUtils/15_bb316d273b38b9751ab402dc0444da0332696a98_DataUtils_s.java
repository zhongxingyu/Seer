 package org.iplantc.de.client.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.services.FolderServiceFacade;
 
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class DataUtils {
     public enum Action {
         RenameFolder(I18N.DISPLAY.rename()), RenameFile(I18N.DISPLAY.rename()), Delete(I18N.DISPLAY
                 .delete()), View(I18N.DISPLAY.view()), ViewTree(I18N.DISPLAY.viewTreeViewer()), Download(
                 I18N.DISPLAY.download()), Metadata(I18N.DISPLAY.metadata());
 
         private final String displayText;
 
         private Action(String displayText) {
             this.displayText = displayText;
         }
 
         @Override
         public String toString() {
             return displayText;
         }
     }
 
     public static boolean hasFolders(final List<DiskResource> resources) {
         boolean ret = false; // assume failure
 
         if (resources != null) {
             for (DiskResource resource : resources) {
                 if (resource instanceof Folder) {
                     ret = true;
                     break;
                 }
             }
         }
 
         return ret;
     }
 
     public static List<Action> getSupportedActions(final List<DiskResource> resources) {
         List<Action> ret = new ArrayList<Action>();
         int size = 0;
         boolean hasFolders = false;
         if (resources != null) {
             size = resources.size();
             hasFolders = hasFolders(resources);
 
             if (size > 0) {
                 if (size == 1) {
                     if (hasFolders) {
                         ret.add(Action.RenameFolder);
                     } else {
                         ret.add(Action.RenameFile);
                         ret.add(Action.View);
                         ret.add(Action.ViewTree);
                     }
                     ret.add(Action.Metadata);
                 } else {
                     if (!hasFolders) {
                         ret.add(Action.View);
                     }
                 }
 
                 ret.add(Action.Download);
                 ret.add(Action.Delete);
             }
         }
 
         return ret;
     }
 
     /**
      * Parse the parent folder from a path.
      * 
      * @param path the path to parse.
      * @return the parent folder.
      */
     public static String parseParent(String path) {
         String ret = ""; //$NON-NLS-1$
         if (path == null || path.trim().isEmpty()) {
             return ret;
         }
         String[] items = path.split("/"); //$NON-NLS-1$
         boolean firstPass = true;
 
         for (int i = 0; i < items.length - 1; i++) {
             if (firstPass) {
                 firstPass = false;
             } else {
                 ret += "/"; //$NON-NLS-1$
             }
 
             ret += items[i];
         }
 
         return ret;
     }
 
     /**
      * Parse the display name from a path.
      * 
      * @param path the path to parse.
      * @return the display name.
      */
     public static String parseNameFromPath(String path) {
         String ret = ""; //$NON-NLS-1$
 
         if (path != null && !path.trim().isEmpty()) {
             String[] items = path.split("/"); //$NON-NLS-1$
             ret = items[items.length - 1];
         }
 
         return ret;
     }
 
     public static String getSizeForDisplay(long size) {
         String value = I18N.DISPLAY.nBytes("0"); //$NON-NLS-1$
 
         try {
             // TODO internationalize number format (dot vs comma)
             if (size >= 1073741824) {
                 value = I18N.DISPLAY.nGigabytes(NumberFormat
                         .getFormat("0.0#").format(size / 1073741824.0)); //$NON-NLS-1$
             } else if (size >= 1048576) {
                 value = I18N.DISPLAY.nMegabytes(NumberFormat.getFormat("0.0#").format(size / 1048576.0)); //$NON-NLS-1$
             } else if (size >= 1000) {
                 // instead of showing bytes in the 1000-1023 range, just show a fraction of a KB.
                 value = I18N.DISPLAY.nKilobytes(NumberFormat.getFormat("0.0#").format(size / 1024.0)); //$NON-NLS-1$
             } else {
                 value = I18N.DISPLAY.nBytes(NumberFormat.getFormat("0").format(size)); //$NON-NLS-1$
             }
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
 
         return value;
     }
 
     public static boolean isViewable(List<DiskResource> resources) {
         // fail even if one of items fails
         if (resources != null && resources.size() > 0) {
             for (int i = 0; i < resources.size(); i++) {
                 DiskResource dr = resources.get(i);
                 if (!dr.getPermissions().isReadable()) {
 
                     return false;
                 }
             }
             return true;
         }
         return false;
     }
 
     public static boolean isDownloadable(List<DiskResource> resources) {
         // for now same logic like isViewable
         return isViewable(resources);
     }
 
     public static boolean isRenamable(DiskResource resource) {
         if (resource != null) {
             return resource.getPermissions().isWritable();
         }
 
         return false;
 
     }
 
     public static boolean isDeletable(List<DiskResource> resources) {
         // fail even if one of items fails
         if (resources != null && resources.size() > 0) {
             for (int i = 0; i < resources.size(); i++) {
                 DiskResource dr = resources.get(i);
                 if (!dr.getPermissions().isWritable()) {
                     return false;
                 }
             }
             return true;
         }
         return false;
     }
 
     public static boolean isMovable(List<DiskResource> resources) {
         // for now same logic as deletable
         return isDeletable(resources);
     }
 
     public static boolean canUploadToThisFolder(Folder destination) {
         if (destination != null) {
             return destination.getPermissions().isWritable();
         }
 
         return false;
 
     }
 
     public static boolean canCreateFolderInThisFolder(Folder destination) {
         // for now same logic as canUploadToThisFolder
         return canUploadToThisFolder(destination);
     }
 
     public static void checkForDuplicateFilename(final String diskResourceId,
             final AsyncCallback<String> callback) {
         checkListForDuplicateFilenames(Arrays.asList(diskResourceId), callback);
 
     }
 
     public static boolean isMetadtaUpdatable(DiskResource resource) {
         if (resource != null) {
             return resource.getPermissions().isWritable();
         }
 
         return false;
 
     }
 
     public static void checkListForDuplicateFilenames(final List<String> diskResourceIds,
             final AsyncCallback<String> callback) {
         final FolderServiceFacade facade = new FolderServiceFacade();
         facade.diskResourcesExist(diskResourceIds, new AsyncCallback<String>() {
             @Override
             public void onSuccess(String response) {
                 JSONObject jsonResponse = JsonUtil.getObject(response);
 
                 String status = JsonUtil.getString(jsonResponse, "status"); //$NON-NLS-1$
                 JSONObject paths = JsonUtil.getObject(jsonResponse, "paths"); //$NON-NLS-1$
 
                 if (!status.equalsIgnoreCase("success") || paths == null) { //$NON-NLS-1$
                     onFailure(new Exception(JsonUtil.getString(jsonResponse, "reason"))); //$NON-NLS-1$
                     return;
                 }
 
                 for (final String resourceId : diskResourceIds) {
                     // TODO add an extra check to make sure the resourceId key is found in paths?
                     boolean fileExists = JsonUtil.getBoolean(paths, resourceId, false);
 
                     if (fileExists) {
                        String errMsg = I18N.ERROR.duplicateFile(parseNameFromPath(resourceId));
                        onFailure(new Exception(errMsg));
                        return;
                     }
                 }
 
                 callback.onSuccess(response);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 // could not check for duplicate files, so abort the move/upload operation.
                 callback.onFailure(caught);
             }
         });
     }
 
     /**
      * A helper method to join strings in a list into one string containing each item separated by glue.
      * Works similar to JavaScript's Array.join(glue) function.
      * 
      * @param stringList
      * @param glue
      * @return A string containing each item in stringList separated by glue.
      */
     public static String join(List<String> stringList, String glue) {
         if (stringList == null) {
             return null;
         }
 
         StringBuilder builder = new StringBuilder();
 
         boolean first = true;
         for (String s : stringList) {
             if (first) {
                 first = false;
             } else {
                 builder.append(glue);
             }
 
             builder.append(s);
         }
 
         return builder.toString();
     }
 
 }
