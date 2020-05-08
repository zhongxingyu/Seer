 package org.iplantc.core.uicommons.client.services;
 
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.uicommons.client.models.HasPaths;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceExistMap;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadata;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceStatMap;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.diskresources.RootFolders;
 import org.iplantc.core.uicommons.client.models.services.DiskResourceMove;
 
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public interface DiskResourceServiceFacade {
 
     void getHomeFolder(AsyncCallback<String> callback);
 
     /**
      * Call service to retrieve the root folder info for the current user
      *
      * @param callback executed when RPC call completes.
      */
     void getRootFolders(AsyncCallback<RootFolders> callback);
 
     /**
      * get user's default analyses output folder
      *
      * @param folderName
      * @param callback
      */
     void getDefaultOutput(String folderName, AsyncCallback<String> callback);
 
     /**
      * set user's default analyses output folder
      *
      * @param folderName
      * @param callback
      */
     void putDefaultOutput(AsyncCallback<String> callback);
 
     /**
      * Called to retrieve the entire contents of a folder.
      *
      * @param path path to requested folder.
      * @param pageSize limit the no.of records to fetch
      * @param offset to start from
      * @param sortCol column name to sort on
      * @param sortOder asc or desc direction
      * @param callback executed when RPC call completes.
      */
     void getFolderContents(final String path, int pageSize, int offset, String sortCol, String sortOrder, final AsyncCallback<Folder> callback);
 
     /**
      * Called to retrieve the contents of a folder without its file contents.
      * 
     * @param parent requested folder.
      * @param callback executed when RPC call completes.
      */
    void getSubFolders(final Folder parent, final AsyncCallback<List<Folder>> callback);
 
     /**
      * Call service to create a new folder
      *
      * @param parentFolder parent folder where the new folder will be created
      * @param callback executed when RPC call completes.
      */
     void createFolder(Folder parentFolder, final String newFolderName, AsyncCallback<Folder> callback);
 
     /**
      * Check if a list of files or folders exist.
      * 
      * @param diskResourcePaths paths to desired resources.
      * @param callback callback executed when RPC call completes. On success, a map that maps
      *            resource paths to whether or not they exist.
      */
     void diskResourcesExist(HasPaths diskResourcePaths, AsyncCallback<DiskResourceExistMap> callback);
 
     /**
      * Fetch preview data for a file.
      * 
      * @param path path to desired file.
      * @param callback callback executed when RPC call completes.
      */
     void previewFile(String path, AsyncCallback<String> callback);
 
     /**
      * Calls the move folder and move file services for the list of given disk resource ids.
      *
      * @param diskResources list of file and folder ids to move.
      * @param destFolder the destination folder where the disk resources will be moved.
      */
     void moveDiskResources(final Set<DiskResource> diskResources, final Folder destFolder,
             AsyncCallback<DiskResourceMove> callback);
     
     /**
      * Calls the move folder and move file services for moving contents of a given folder.
      * 
      * @param sourceFolderId
      * @param destFolder
      * @param callback
      */
     void moveContents(final String sourceFolderId, final Folder destFolder,  AsyncCallback<DiskResourceMove> callback);
 
     /**
      * Call service rename a file or folder.
      *
      * @param src
      * @param destName
      * @param callback service success/failure callback
      */
     void renameDiskResource(DiskResource src, String destName, AsyncCallback<DiskResource> callback);
 
     /**
      * Call service to upload a file from a given URL.
      *
      * @param url
      * @param dest id of the destination folder.
      * @param callback service success/failure callback
      */
     void importFromUrl(String url, DiskResource dest, AsyncCallback<String> callback);
 
     /**
      * Call service to retrieve upload configuration values for idrop-lite.
      *
      * @param callback executed when RPC call completes.
      */
     void upload(AsyncCallback<String> callback);
 
     /**
      * Call service to retrieve upload configuration values for idrop-lite.
      *
      * @param callback executed when RPC call completes.
      */
     void download(HasPaths paths, AsyncCallback<String> callback);
 
     /**
      * Opens a window to download the file with the given path.
      *
      * @param path Path of the file to download.
      */
     void simpleDownload(String path);
 
     /**
      * Call service to delete disk resources (i.e. {@link File}s and {@link Folder}s)
      *
      * @param diskResources a set of <code>DiskResource</code>s to be deleted
      * @param callback callback executed when service call completes.
      */
     <T extends DiskResource> void deleteDiskResources(Set<T> diskResources, AsyncCallback<HasPaths> callback);
 
     /**
      * Call service to delete disk resources in case user selects all items
      * 
      * @param selectedFolderId
      * @param callback
      */
     void deleteContents(String selectedFolderId, AsyncCallback<HasPaths> callback);
     
     /**
      * Call service to delete disk resources (i.e. {@link File}s and {@link Folder}s)
      * 
      * @param diskResources a set of <code>DiskResource</code>s to be deleted
      * @param callback callback executed when service call completes.
      */
     void deleteDiskResources(HasPaths diskResources, AsyncCallback<HasPaths> callback);
 
     /**
      * @param resource the <code>DiskResource</code> for which metadata will be retrieved.
      * @param callback callback executed when service call completes.
      */
     void getDiskResourceMetaData(DiskResource resource, AsyncCallback<String> callback);
 
     /**
      * Calls service to set disk resource metadata.
      *
      * @param resource the <code>DiskResource</code> whose metadata will be updated
      * @param mdToUpdate a list of <code>DiskResourceMetadata</code> objects which will be updated
      * @param mdToDelete a list of <code>DiskResourceMetadata</code> objects which will be deleted
      * @param callback executed when the service call completes.
      */
     void setDiskResourceMetaData(DiskResource resource, Set<DiskResourceMetadata> mdToUpdate, Set<DiskResourceMetadata> mdToDelete,
             AsyncCallback<String> callback);
 
     /**
      * call service to set folder metadata
      *
      * @param folderId id of folder resource
      * @param body metadata in json format
      * @param callback execute when RPC call complete
      */
     void setFolderMetaData(String folderId, String body, AsyncCallback<String> callback);
 
     /**
      * call service to set file metadata
      *
      * @param fileId id of file resource
      * @param body metadata in json format
      * @param callback execute when RPC call complete
      */
     void setFileMetaData(String fileId, String body, AsyncCallback<String> callback);
 
     /**
      *
      * Share a resource with give user with permission
      *
      *
      * @param body - Post body in JSONObject format
      * @param callback callback object
      */
     void shareDiskResource(JSONObject body, AsyncCallback<String> callback);
 
     /**
      * UnShare a resource with give user with permission
      *
      * @param body - Post body in JSONObject format
      * @param callback callback object
      */
     void unshareDiskResource(JSONObject body, AsyncCallback<String> callback);
 
     /**
      * get user permission info on selected disk resources
      *
      * @param body - Post body in JSONObject format
      * @param callback callback object
      */
     void getPermissions(JSONObject body, AsyncCallback<String> callback);
 
     /**
      * search users irods directory structure
      *
      * @param term search term
      * @param size limit for results to return
      * @param type file or folder
      * @param callback callback object
      */
     void search(String term, int size, String type, AsyncCallback<String> callback);
 
     /**
      * Get info about a selected file or folder
      * 
      * @param body request body
      * @param callback callback object
      * @deprecated User {@link #getStat(HasPaths, AsyncCallback)}
      */
     @Deprecated
     void getStat(String body, AsyncCallback<String> callback);
 
     /**
      * Get info about a selected file or folder
      * 
      * @param diskResourcePaths the paths to query
      * @param callback callback object
      */
     void getStat(final HasPaths diskResourcePaths, final AsyncCallback<DiskResourceStatMap> callback);
 
     /**
      * get data search history
      *
      * @param callback callback object
      *
      */
     void getDataSearchHistory(AsyncCallback<String> callback);
 
     /**
      * save users data search history
      *
      * @param body json object search history
      * @param callback callback object
      */
     void saveDataSearchHistory(String body, AsyncCallback<String> callback);
 
     /**
      * empty user's trash
      *
      * @param user
      * @param callback
      */
     public void emptyTrash(String user, AsyncCallback<String> callback);
 
     /**
     * get users trash path
     *
     * @param userName
     * @param callback
     */
     public void getUserTrashPath(String userName, AsyncCallback<String> callback);
 
     /**
      * Restore deleted disk resources.
      * 
      * @param request
      * @param callback
      */
     public void restoreDiskResource(HasPaths request, AsyncCallback<String> callback);
 
     /**
      * Creates a set of public data links for the given disk resources.
      *
      * @param ticketIdList the id of the disk resource for which the ticket will be created.
      * @param callback
      */
     public void createDataLinks(List <String> ticketIdList,
             AsyncCallback<String> callback);
 
     /**
      * Requests a listing of all the tickets for the given disk resources.
      *
      * @param diskResourceIds the disk resources whose tickets will be listed.
      * @param callback
      */
     public void listDataLinks(List<String> diskResourceIds, AsyncCallback<String> callback);
 
     /**
      * Requests that the given Kif Share tickets will be deleted.
      *
      * @param dataLinkIds the tickets which will be deleted.
      * @param callback
      */
     public void deleteDataLinks(List<String> dataLinkIds, AsyncCallback<String> callback);
 
     /**
      * Get a list of files types recognized
      *
      * @param callback
      */
 	void getFileTypes(AsyncCallback<String> callback);
 
 	/**
 	 *set type to a file
 	 *
 	 * @param filePath
 	 * @param type
 	 * @param callback
 	 */
 	void setFileType(String filePath, String type,
 			AsyncCallback<String> callback);
 
     /**
      * Convenience method which returns a valid {@link DiskResourceAutoBeanFactory} instance.
      * 
      * @return a ready to use <code>DiskResourceAutoBeanFactory</code>
      */
     DiskResourceAutoBeanFactory getDiskResourceFactory();
 
     /**
      * Restore all items in trash to its original location.
      * 
      * @param callback
      */
     void restoreAll(AsyncCallback<String> callback);
 
     /**
      * Method to use when user selects all items in a folder.
      * 
      * @param parentFolderId
      * @param callback
      */
     void downloadContents(String parentFolderId, AsyncCallback<String> callback);
 }
 
