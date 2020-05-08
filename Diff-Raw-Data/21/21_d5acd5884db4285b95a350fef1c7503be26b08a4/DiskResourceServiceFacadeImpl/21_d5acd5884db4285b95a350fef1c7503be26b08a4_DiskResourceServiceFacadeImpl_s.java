 package org.iplantc.core.uicommons.client.services;
 
 import java.util.List;
 import java.util.Set;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.DEClientConstants;
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.events.diskresources.DiskResourceRefreshEvent;
 import org.iplantc.core.uicommons.client.events.diskresources.DiskResourceRefreshEvent.DiskResourceRefreshEventHandler;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.HasPaths;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceExistMap;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadata;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceStatMap;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.diskresources.RootFolders;
 import org.iplantc.core.uicommons.client.models.services.DiskResourceMove;
 import org.iplantc.core.uicommons.client.models.services.DiskResourceRename;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uicommons.client.util.WindowUtil;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.AutoBeanUtils;
 import com.sencha.gxt.core.client.util.Format;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.data.shared.TreeStore;
 
 /**
  * Provides access to remote services for folder operations.
  *
  * @author amuir
  *
  */
 public class DiskResourceServiceFacadeImpl extends TreeStore<Folder> implements
         DiskResourceServiceFacade, DiskResourceRefreshEventHandler {
 
     public DiskResourceServiceFacadeImpl() {
         super(new ModelKeyProvider<Folder>() {
 
             @Override
             public String getKey(Folder item) {
                 return item.getId();
             }
         });
 
         EventBus.getInstance().addHandler(DiskResourceRefreshEvent.TYPE, this);
     }
 
     private static final DiskResourceAutoBeanFactory FACTORY = GWT.create(DiskResourceAutoBeanFactory.class);
 
     private static <T> String encode(final T entity) {
         return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(entity)).getPayload();
     }
 
     private static <T> T decode(Class<T> clazz, String playload) {
         return AutoBeanCodex.decode(FACTORY, clazz, playload).as();
     }
 
     @Override
     public void getHomeFolder(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "home"; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         callService(wrapper, callback);
     }
 
     @Override
     public final void getRootFolders(final AsyncCallback<RootFolders> callback) {
         if (getRootCount() > 0) {
             RootFolders result = FACTORY.rootFolders().as();
             result.setRoots(getRootItems());
             callback.onSuccess(result);
         } else {
             String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "root"; //$NON-NLS-1$
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
 
             callService(wrapper, new AsyncCallbackConverter<String, RootFolders>(callback) {
                 @Override
                 protected RootFolders convertFrom(final String json) {
                     RootFolders result = decode(RootFolders.class, json);
                     setRootFolders(result.getRoots());
 
                     return result;
                 }
             });
         }
     }
 
     private void setRootFolders(List<Folder> rootNodes) {
         clear();
         add(rootNodes);
     }
 
     @Override
     public void getDefaultOutput(final String folderName, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "default-output-dir?name="
                 + folderName;
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void putDefaultOutput(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "default-output-dir?name="
                 + DEProperties.getInstance().getDefaultOutputFolderName();
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 "{\"path\":\"" + DEProperties.getInstance().getDefaultOutputFolderName() + "\"}");
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void getFolderContents(final String path,int pageSize,int offset,String sortCol, String sortOrder, final AsyncCallback<Folder> callback) {
         String address = getDirectoryListingEndpoint(path,pageSize,offset, sortCol, sortOrder);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         callService(wrapper, new AsyncCallbackConverter<String, Folder>(callback) {
 
             @Override
             protected Folder convertFrom(String result) {
                 // Decode JSON result into a folder
                 return decode(Folder.class, result);
             }
         });
     }
 
     @Override
     public void getSubFolders(final String path, final AsyncCallback<List<Folder>> callback) {
         Folder folder = findModelWithKey(path);
 
         if (hasFoldersLoaded(folder)) {
             callback.onSuccess(getSubFolders(folder));
         } else {
             String address = getDirectoryListingEndpoint(path, false);
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
             callService(wrapper, new AsyncCallbackConverter<String, List<Folder>>(callback) {
 
                 @Override
                 protected List<Folder> convertFrom(String result) {
                     // Decode JSON result into a folder
                     Folder folder = decode(Folder.class, result);
 
                     // Store or update the folder's subfolders.
                     saveSubFolders(folder);
 
                     return getSubFolders(folder);
                 }
             });
         }
     }
 
     private void saveSubFolders(final Folder folder) {
         if (folder == null) {
             return;
         }
 
         List<Folder> subfolders = folder.getFolders();
         Folder parent = findModel(folder);
         if (parent != null && subfolders != null) {
             parent.setFolders(subfolders);
 
             for (Folder child : subfolders) {
                 Folder current = findModel(child);
                 if (current == null) {
                     add(parent, child);
                 } else {
                     child.setFolders(current.getFolders());
                     update(child);
                 }
             }
         }
     }
 
     private List<Folder> getSubFolders(final Folder folder) {
         if (folder != null && folder.getFolders() != null) {
             return folder.getFolders();
         }
 
         return Lists.newArrayList();
     }
 
     private boolean hasFoldersLoaded(final Folder folder) {
         return folder != null && folder.getFolders() != null;
     }
 
     private String getDirectoryListingEndpoint(final String path, boolean includeFiles) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl()
                 + "directory?includefiles=" + (includeFiles ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
         if (!Strings.isNullOrEmpty(path)) {
             address += "&path=" + URL.encodePathSegment(path); //$NON-NLS-1$
         }
 
         return address;
     }
     
     private String getDirectoryListingEndpoint(final String path,int pageSize,int offset,String sortCol, String sortOrder ) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl()
                 + "paged-directory?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
         if (!Strings.isNullOrEmpty(path)) {
             address += "path=" + URL.encodePathSegment(path) + "&sort-col="+ sortCol + "&limit=" + pageSize + "" + "&offset=" + offset + "&sort-order=" + sortOrder; //$NON-NLS-1$
         }
 
         return address;
     }
 
     @Override
     public void createFolder(final Folder parentFolder, final String newFolderName,
             AsyncCallback<Folder> callback) {
         final String parentId = parentFolder.getPath();
 
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "directory/create"; //$NON-NLS-1$
         JSONObject obj = new JSONObject();
         obj.put("path", new JSONString(parentId + "/" + newFolderName)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 obj.toString());
         callService(wrapper, new AsyncCallbackConverter<String, Folder>(callback) {
 
             @Override
             protected Folder convertFrom(String result) {
                 Folder folder = decode(Folder.class, result);
 
                 // Set the new folder name since the create folder service call result does not contain
                 // the name of the new folder
                 folder.setName(newFolderName);
 
                 // Use the service call result to set the ID of the new folder. Otherwise, calls to
                 // getId() on this new folder instance will return null.
                 folder.setId(folder.getPath());
 
                 addFolder(parentFolder.getId(), folder);
 
                 return folder;
             }
         });
     }
 
     private void addFolder(String parentId, Folder child) {
         Folder parent = findModelWithKey(parentId);
         if (parent != null) {
             if (parent.getFolders() != null) {
                 parent.getFolders().add(child);
             }
 
             add(parent, child);
         }
     }
 
     @Override
     public final void diskResourcesExist(final HasPaths diskResourcePaths, final AsyncCallback<DiskResourceExistMap> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "exists"; //$NON-NLS-1$
         final String body = encode(diskResourcePaths);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body);
         callService(wrapper, new AsyncCallbackConverter<String, DiskResourceExistMap>(callback) {
             @Override
             protected DiskResourceExistMap convertFrom(final String json) {
                 // TODO Verify this facade's store against these results?
                 return decode(DiskResourceExistMap.class, json);
             }
         });
     }
 
     @Override
     public void previewFile(final String path, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "file/preview"; //$NON-NLS-1$
         JSONObject body = new JSONObject();
         body.put("source", new JSONString(path)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 body.toString());
         callService(wrapper, callback);
     }
 
     @Override
     public void moveDiskResources(final Set<DiskResource> diskResources, final Folder destFolder,
             AsyncCallback<DiskResourceMove> callback) {
 
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "move"; //$NON-NLS-1$
 
         DiskResourceMove request = FACTORY.diskResourceMove().as();
         request.setDest(destFolder.getPath());
         request.setSources(DiskResourceUtil.asStringIdList(diskResources));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 encode(request));
 
         callService(wrapper, new AsyncCallbackConverter<String, DiskResourceMove>(callback) {
 
             @Override
             protected DiskResourceMove convertFrom(String result) {
                 DiskResourceMove resourcesMoved = decode(DiskResourceMove.class, result);
                 // KLUDGE manually set destFolder until services are updated to return full dest info.
                 resourcesMoved.setDestination(destFolder);
                 moveFolders(resourcesMoved);
 
                 return resourcesMoved;
             }
         });
     }
 
     private void moveFolders(DiskResourceMove resourcesMoved) {
         if (resourcesMoved == null || resourcesMoved.getSources() == null) {
             return;
         }
 
         Folder dest = findModel(resourcesMoved.getDestination());
         for (String path : resourcesMoved.getSources()) {
             Folder folder = findModelWithKey(path);
             if (folder != null) {
                 Folder parent = getParent(folder);
 
                 // Remove the folder and its children from the cache.
                 remove(folder);
 
                 // Remove the folder from its original parent.
                 if (parent != null && parent.getFolders() != null) {
                     parent.getFolders().remove(folder);
                 }
 
                 // Move the folder and its children to dest in the cache, updating their paths first.
                 if (hasFoldersLoaded(dest)) {
                     // Clone moved folder, so other views can still manage the folder in their stores.
                     folder = decode(Folder.class, encode(folder));
 
                     dest.getFolders().add(folder);
                     moveFolderTree(folder, dest);
                 }
             }
         }
     }
 
     private void moveFolderTree(Folder folder, Folder dest) {
         if (folder == null || dest == null) {
             return;
         }
 
         // Update the folder's path to its new location, then cache it in the TreeStore.
         String newPath = DiskResourceUtil.appendNameToPath(dest.getPath(), folder.getName());
         folder.setId(newPath);
         folder.setPath(newPath);
         add(dest, folder);
 
         // Move the folder's children to the new location in the cache, updating their paths first.
         List<Folder> children = folder.getFolders();
         if (children != null) {
             for (Folder child : children) {
                 moveFolderTree(child, folder);
             }
         }
     }
 
     @Override
     public void renameDiskResource(final DiskResource src, String destName,
             AsyncCallback<DiskResource> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "rename"; //$NON-NLS-1$
 
         DiskResourceRename request = FACTORY.diskResourceRename().as();
         String srcId = src.getPath();
         request.setSource(srcId);
         request.setDest(DiskResourceUtil.appendNameToPath(DiskResourceUtil.parseParent(srcId), destName));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 encode(request));
         callService(wrapper, new AsyncCallbackConverter<String, DiskResource>(callback) {
 
             @Override
             protected DiskResource convertFrom(String result) {
                 DiskResourceRename response = decode(DiskResourceRename.class, result);
 
                 DiskResource newDr = null;
                 if (src instanceof Folder) {
                     newDr = decode(Folder.class, encode(src));
                 } else {
                     newDr = decode(File.class, encode(src));
                 }
 
                 String newId = response.getDest();
                 newDr.setId(newId);
                 newDr.setPath(newId);
                 newDr.setName(DiskResourceUtil.parseNameFromPath(newId));
 
                 if (newDr instanceof Folder) {
                     renameFolder((Folder)src, (Folder)newDr);
                 }
 
                 return newDr;
             }
         });
     }
 
     private void renameFolder(Folder src, Folder renamed) {
         if (src == null || renamed == null) {
             return;
         }
 
         Folder folder = findModel(src);
         if (folder != null) {
             Folder parent = getParent(folder);
 
             // Remove the folder and its children from the cache.
             remove(folder);
 
             if (hasFoldersLoaded(parent)) {
                 // Replace the folder and its children with the renamed folder, by adding the children to
                 // the renamed folder and resetting their paths.
                 parent.getFolders().remove(folder);
                 renamed.setFolders(folder.getFolders());
                 parent.getFolders().add(renamed);
 
                 moveFolderTree(renamed, parent);
             }
         }
     }
 
     @Override
     public void onRefresh(DiskResourceRefreshEvent event) {
         Folder folder = findModelWithKey(event.getCurrentFolderId());
         if (folder == null) {
             return;
         }
 
        // KLUDGE TreeStore#removeChildren doesn't actually remove the children from their parent's
        // TreeModel wrapper, so remove the parent as well, then re-add it without children.
        Folder parent = null;
        int index = getRootItems().indexOf(folder);
        if (index < 0) {
            parent = getParent(folder);

            if (parent != null) {
                index = indexOf(folder);
            }
        }

        remove(folder);
         folder.setFolders(null);

        if (parent == null) {
            insert(index, folder);
        } else {
            insert(parent, index, folder);
        }
     }
 
     /**
      * search data
      *
      * @param term search termt
      * @param callback
      */
     @Override
     public void search(String term, int size, String type, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getMuleServiceBaseUrl() + "search?search-term="
                 + URL.encodePathSegment(term) + "&size=" + size;
 
         if (type != null && !type.isEmpty()) {
             fullAddress = fullAddress + "&type=" + type;
         }
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, fullAddress);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void importFromUrl(final String url, final DiskResource dest, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getFileIoBaseUrl() + "urlupload"; //$NON-NLS-1$
         JSONObject body = new JSONObject();
         body.put("dest", new JSONString(dest.getPath())); //$NON-NLS-1$
         body.put("address", new JSONString(url)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         callService(wrapper, callback);
     }
 
     @Override
     public void upload(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "upload"; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         callService(wrapper, callback);
     }
 
     @Override
     public void download(HasPaths paths, AsyncCallback<String> callback) {
         final String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "download"; //$NON-NLS-1$
         final String body = encode(paths);
         final ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body);
         callService(wrapper, callback);
     }
     
     @Override
     public void downloadContents(String parentFolderId, AsyncCallback<String> callback) {
         final String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "download-contents"; //$NON-NLS-1$
         JSONObject body = new JSONObject();
         body.put("path", new JSONString(parentFolderId));
         final ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body.toString());
         callService(wrapper, callback);
     }
 
     private final DEClientConstants constants = GWT.create(DEClientConstants.class);
 
     @Override
     public void simpleDownload(String path) {
         // We must proxy the download requests through a servlet, since the actual download service may
         // be on a port behind a firewall that the servlet can access, but the client can not.
         String address = Format.substitute("{0}{1}?user={2}&path={3}", GWT.getModuleBaseURL(), //$NON-NLS-1$
                 constants.fileDownloadServlet(), UserInfo.getInstance().getUsername(), path);
 
         WindowUtil.open(URL.encode(address), "width=100,height=100"); //$NON-NLS-1$
     }
 
     @Override
     public <T extends DiskResource> void deleteDiskResources(final Set<T> diskResources, final AsyncCallback<HasPaths> callback) {
         final HasPaths dto = FACTORY.pathsList().as();
         dto.setPaths(DiskResourceUtil.asStringIdList(diskResources));
         deleteDiskResources(dto, callback);
     }
 
     @Override
     public final void deleteDiskResources(final HasPaths diskResources, final AsyncCallback<HasPaths> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "delete"; //$NON-NLS-1$
         final String body = encode(diskResources);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress, body);
         callService(wrapper, new AsyncCallbackConverter<String, HasPaths>(callback) {
             @Override
             protected HasPaths convertFrom(final String json) {
                 HasPaths deletedIds = decode(HasPaths.class, json);
 
                 // Remove any folders found in the response from the TreeStore.
                 if (deletedIds != null && deletedIds.getPaths() != null) {
                     for (String path : deletedIds.getPaths()) {
                         Folder deleted = findModelWithKey(path);
                         if (deleted != null) {
                             remove(deleted);
                         }
                     }
                 }
 
                 return deletedIds;
             }});
     }
 
     @Override
     public void getDiskResourceMetaData(DiskResource resource, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "metadata" + "?path=" //$NON-NLS-1$ //$NON-NLS-2$
                 + URL.encodePathSegment(resource.getPath());
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, fullAddress);
         callService(wrapper, callback);
 
     }
 
     @Override
     public void setDiskResourceMetaData(DiskResource resource, Set<DiskResourceMetadata> mdToUpdate,
             Set<DiskResourceMetadata> mdToDelete, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "metadata-batch" //$NON-NLS-1$
                 + "?path=" + URL.encodePathSegment(resource.getPath()); //$NON-NLS-1$
 
         // Create json body consisting of md to updata and md to delete.
         JSONObject obj = new JSONObject();
         obj.put("add", buildMetadataToAddJsonArray(mdToUpdate));
         obj.put("delete", buildMetadataToDeleteJsonArray(mdToDelete));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 obj.toString());
         callService(wrapper, callback);
     }
 
     private JSONValue buildMetadataToAddJsonArray(Set<DiskResourceMetadata> metadata) {
         final JSONArray arr = new JSONArray();
         int i = 0;
         for (DiskResourceMetadata md : metadata) {
             JSONValue jsonValue = JSONParser.parseStrict(encode(md));
             arr.set(i++, jsonValue);
         }
         return arr;
     }
 
     private JSONValue buildMetadataToDeleteJsonArray(Set<DiskResourceMetadata> metadataToDelete) {
         JSONArray arr = new JSONArray();
         int i = 0;
         for (DiskResourceMetadata md : metadataToDelete) {
             arr.set(i++, new JSONString(md.getAttribute()));
         }
         return arr;
     }
 
     @Override
     public void setFolderMetaData(String folderId, String body, AsyncCallback<String> callback) {
         // String fullAddress = serviceNamePrefix
         //                + ".folder-metadata-batch" + "?path=" + URL.encodePathSegment(folderId); //$NON-NLS-1$
         // ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
         // body);
         // callService(callback, wrapper);
     }
 
     @Override
     public void setFileMetaData(String fileId, String body, AsyncCallback<String> callback) {
         // String fullAddress = serviceNamePrefix
         //                + ".file-metadata-batch" + "?path=" + URL.encodePathSegment(fileId); //$NON-NLS-1$
         // ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
         // body);
         // callService(callback, wrapper);
     }
 
     @Override
     public void shareDiskResource(JSONObject body, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "share"; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 body.toString());
 
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void unshareDiskResource(JSONObject body, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "unshare"; //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 body.toString());
 
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void getPermissions(JSONObject body, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "user-permissions"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         callService(wrapper, callback);
     }
 
     @Override
     public void getStat(String body, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "stat"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         callService(wrapper, callback);
     }
 
     @Override
     public final void getStat(final HasPaths diskResourcePaths, final AsyncCallback<DiskResourceStatMap> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "stat"; //$NON-NLS-1$
         final String body = encode(diskResourcePaths);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body);
         callService(wrapper, new AsyncCallbackConverter<String, DiskResourceStatMap>(callback) {
             @Override
             protected DiskResourceStatMap convertFrom(final String json) {
                 return decode(DiskResourceStatMap.class, json);
             }
         });
     }
 
     /**
      * Performs the actual service call.
      * @param wrapper the wrapper used to get to the actual service via the service proxy.
      * @param callback executed when RPC call completes.
      */
     private void callService(ServiceCallWrapper wrapper, AsyncCallback<String> callback) {
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void getDataSearchHistory(AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "search-history";
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     @Override
     public void saveDataSearchHistory(String body, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getMuleServiceBaseUrl() + "search-history";
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address, body);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void restoreDiskResource(HasPaths request, AsyncCallback<String> callback) {
         final String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "restore"; //$NON-NLS-1$
         final String body = encode(request);
         final ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress, body);
         callService(wrapper, callback);
     }
 
     /**
      * empty user's trash
      *
      * @param user
      * @param callback
      */
     @Override
     public void emptyTrash(String user, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "trash"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.DELETE, address);
         callService(wrapper, callback);
     }
 
     /**
      * get users trash path
      *
      * @param userName
      * @param callback
      */
     @Override
     public void getUserTrashPath(String userName, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "user-trash-dir" //$NON-NLS-1$
                 + "?path=" + URL.encodePathSegment(userName); //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, fullAddress);
         callService(wrapper, callback);
     }
 
     /**
      * Creates a set of public data links for the given disk resources.
      *
      * @param ticketIdList the id of the disk resource for which the ticket will be created.
      * @param isPublicTicket
      * @param callback
      */
     @Override
     public void createDataLinks(List<String> ticketIdList,
             AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "tickets"; //$NON-NLS-1$
         String args = "public=1";
 
         JSONObject body = new JSONObject();
         JSONArray tickets = new JSONArray();
         int index = 0;
         for (String id : ticketIdList) {
             tickets.set(index, new JSONString(id));
             index++;
         }
         body.put("paths", tickets);
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         wrapper.setArguments(args);
         callService(wrapper, callback);
 
     }
 
     /**
      * Requests a listing of all the tickets for the given disk resources.
      *
      * @param diskResourceIds the disk resources whose tickets will be listed.
      * @param callback
      */
     @Override
     public void listDataLinks(List<String> diskResourceIds, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "list-tickets"; //$NON-NLS-1$
 
         JSONObject body = new JSONObject();
         body.put("paths", JsonUtil.buildArrayFromStrings(diskResourceIds));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         callService(wrapper, callback);
 
     }
 
     /**
      * Requests that the given Kif Share tickets will be deleted.
      *
      * @param dataLinkIds the tickets which will be deleted.
      * @param callback
      */
     @Override
     public void deleteDataLinks(List<String> dataLinkIds, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "delete-tickets"; //$NON-NLS-1$
 
         JSONObject body = new JSONObject();
         body.put("tickets", JsonUtil.buildArrayFromStrings(dataLinkIds));
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         callService(wrapper, callback);
     }
 
 	@Override
 	public void getFileTypes(AsyncCallback<String> callback) {
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "filetypes/type-list";
 
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.GET, address);
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
 	@Override
 	public void setFileType(String filePath, String type,
 			AsyncCallback<String> callback) {
 		JSONObject obj = new JSONObject();
 		obj.put("path", new JSONString(filePath));
 		obj.put("type",new JSONString(type));
 
 		String address = DEProperties.getInstance().getMuleServiceBaseUrl()
 				+ "filetypes/type";
 		ServiceCallWrapper wrapper = new ServiceCallWrapper(
 				ServiceCallWrapper.Type.POST, address, obj.toString());
 		DEServiceFacade.getInstance().getServiceData(wrapper, callback);
 	}
 
     @Override
     public DiskResourceAutoBeanFactory getDiskResourceFactory() {
         return FACTORY;
     }
 
     @Override
     public void moveContents(String sourceFolderId, final Folder destFolder, AsyncCallback<DiskResourceMove> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "move-contents"; //$NON-NLS-1$
 
         DiskResourceMove request = FACTORY.diskResourceMove().as();
         request.setDest(destFolder.getPath());
         request.setSelectedFolderId(sourceFolderId);
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 encode(request));
 
         callService(wrapper, new AsyncCallbackConverter<String, DiskResourceMove>(callback) {
 
             @Override
             protected DiskResourceMove convertFrom(String result) {
                 DiskResourceMove resourcesMoved = decode(DiskResourceMove.class, result);
                 // KLUDGE manually set destFolder until services are updated to return full dest info.
                 resourcesMoved.setDestination(destFolder);
                 moveFolders(resourcesMoved);
 
                 return resourcesMoved;
             }
         });
         
     }
     
     @Override
     public void deleteContents(String selectedFolderId, AsyncCallback<HasPaths> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "delete-contents"; //$NON-NLS-1$
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress, "{\"path\":\"" + selectedFolderId + "\"}");
         callService(wrapper, new AsyncCallbackConverter<String, HasPaths>(callback) {
             @Override
             protected HasPaths convertFrom(final String json) {
                 HasPaths deletedIds = decode(HasPaths.class, json);
 
                 // Remove any folders found in the response from the TreeStore.
                 if (deletedIds != null && deletedIds.getPaths() != null) {
                     for (String path : deletedIds.getPaths()) {
                         Folder deleted = findModelWithKey(path);
                         if (deleted != null) {
                             remove(deleted);
                         }
                     }
                 }
 
                 return deletedIds;
             }});
     }
     
     @Override
     public void restoreAll(AsyncCallback<String> callback) {
         final String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "restore-all"; //$NON-NLS-1$
         final ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress, "{}");
         callService(wrapper, callback);
     }
 
 }
