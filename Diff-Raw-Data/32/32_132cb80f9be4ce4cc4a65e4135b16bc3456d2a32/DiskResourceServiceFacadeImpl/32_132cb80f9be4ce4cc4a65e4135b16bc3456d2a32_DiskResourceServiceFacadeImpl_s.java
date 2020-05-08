 package org.iplantc.core.uicommons.client.services;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.DEClientConstants;
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.HasPaths;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceExistMap;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceMetadata;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceStatMap;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.diskresources.RootFolders;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uicommons.client.util.WindowUtil;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
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
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
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
         DiskResourceServiceFacade {
 
     public DiskResourceServiceFacadeImpl() {
         super(new ModelKeyProvider<Folder>() {
 
             @Override
             public String getKey(Folder item) {
                 return item.getId();
             }
         });
     }
 
     private static final DiskResourceAutoBeanFactory FACTORY = GWT.create(DiskResourceAutoBeanFactory.class);
 
     private static <T> String encode(final T entity) {
         return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(entity)).getPayload();
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
                     RootFolders result = AutoBeanCodex.decode(FACTORY, RootFolders.class, json).as();
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
     public void getFolderContents(final String path, final AsyncCallback<Set<DiskResource>> callback) {
         Folder folder = findModelWithKey(path);
 
         if (hasFoldersLoaded(folder) && hasFilesLoaded(folder)) {
             callback.onSuccess(getFolderContents(folder));
         } else {
             String address = getDirectoryListingEndpoint(path, true);
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
             callService(wrapper, new AsyncCallbackConverter<String, Set<DiskResource>>(callback) {
 
                 @Override
                 protected Set<DiskResource> convertFrom(String result) {
                     Folder folder = findModelWithKey(path);
                     if (folder != null) {
                         boolean foldersLoaded = hasFoldersLoaded(folder);
 
                         // Turn json result into a Splittable and wrap the loaded folder
                         Splittable split = StringQuoter.split(result);
                         AutoBeanCodex.decodeInto(split,
                                 AutoBeanUtils.<Folder, Folder> getAutoBean(folder));
 
                         if (!foldersLoaded) {
                             saveSubFolders(folder);
                         }
                     }
 
                     return getFolderContents(folder);
                 }
             });
         }
     }
 
     private Set<DiskResource> getFolderContents(final Folder folder) {
         Set<DiskResource> children = Sets.newHashSet();
 
         if (folder != null) {
             if (folder.getFolders() != null) {
                 children.addAll(folder.getFolders());
             }
             if (folder.getFiles() != null) {
                 children.addAll(folder.getFiles());
             }
         }
 
         return children;
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
                     Folder folder = findModelWithKey(path);
                     if (folder != null) {
                         // Turn json result into a Splittable and wrap the loaded folder
                         Splittable split = StringQuoter.split(result);
                         AutoBeanCodex.decodeInto(split,
                                 AutoBeanUtils.<Folder, Folder> getAutoBean(folder));
 
                         saveSubFolders(folder);
                     }
 
                     return getSubFolders(folder);
                 }
             });
         }
     }
 
    private void saveSubFolders(final Folder parent) {
        if (parent != null && parent.getFolders() != null) {
            add(parent, parent.getFolders());
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
 
     private boolean hasFilesLoaded(final Folder folder) {
         return folder != null && folder.getFiles() != null;
     }
 
     private String getDirectoryListingEndpoint(final String path, boolean includeFiles) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl()
                 + "directory?includefiles=" + (includeFiles ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
         if (!Strings.isNullOrEmpty(path)) {
             address += "&path=" + URL.encodePathSegment(path); //$NON-NLS-1$
         }
 
         return address;
     }
 
     @Override
     public void createFolder(Folder parentFolder, final String newFolderName,
             AsyncCallback<Folder> callback) {
         final String parentId = parentFolder.getId();
 
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "directory/create"; //$NON-NLS-1$
         JSONObject obj = new JSONObject();
         obj.put("path", new JSONString(parentId + "/" + newFolderName)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 obj.toString());
         callService(wrapper, new AsyncCallbackConverter<String, Folder>(callback) {
 
             @Override
             protected Folder convertFrom(String result) {
                 Folder folder = AutoBeanCodex.decode(FACTORY, Folder.class, result).as();
 
                 // Set the new folder name since the create folder service call result does not contain
                 // the name of the new folder
                 folder.setName(newFolderName);
 
                 // Use the service call result to set the ID of the new folder. Otherwise, calls to
                 // getId() on this new folder instance will return null.
                 folder.setId(folder.getPath());
 
                 addFolder(parentId, folder);
 
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
                 return AutoBeanCodex.decode(FACTORY, DiskResourceExistMap.class, json).as();
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
     public void moveDiskResources(
             final Set<org.iplantc.core.uicommons.client.models.diskresources.DiskResource> diskResources,
             final Folder idDestFolder, AsyncCallback<String> callback) {
 
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + "move"; //$NON-NLS-1$
 
         List<String> idSrcFiles = DiskResourceUtil.asStringIdList(diskResources);
         JSONObject body = new JSONObject();
         body.put("dest", new JSONString(idDestFolder.getId())); //$NON-NLS-1$
         body.put("sources", JsonUtil.buildArrayFromStrings(idSrcFiles)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, address,
                 body.toString());
 
         callService(wrapper, callback);
     }
 
     @Override
     public void renameDiskResource(DiskResource src, String destName, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "rename"; //$NON-NLS-1$
         JSONObject body = new JSONObject();
         body.put("source", new JSONString(src.getId())); //$NON-NLS-1$
         body.put("dest", new JSONString(DiskResourceUtil.parseParent(src.getId()) + "/" + destName)); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.POST, fullAddress,
                 body.toString());
         callService(wrapper, callback);
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
     public void importFromUrl(final String url, final HasId dest, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getFileIoBaseUrl() + "urlupload"; //$NON-NLS-1$
         JSONObject body = new JSONObject();
         body.put("dest", new JSONString(dest.getId())); //$NON-NLS-1$
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
         final List<String> paths = Lists.newArrayList();
         for (DiskResource res : diskResources) {
             paths.add(res.getId());
         }
         final HasPaths dto = FACTORY.pathsList().as();
         dto.setPaths(paths);
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
                 return AutoBeanCodex.decode(FACTORY, HasPaths.class, json).as();
             }});
     }
 
     @Override
     public void getDiskResourceMetaData(DiskResource resource, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "metadata" + "?path=" //$NON-NLS-1$ //$NON-NLS-2$
                 + URL.encodePathSegment(resource.getId());
         ServiceCallWrapper wrapper = new ServiceCallWrapper(ServiceCallWrapper.Type.GET, fullAddress);
         callService(wrapper, callback);
 
     }
 
     @Override
     public void setDiskResourceMetaData(DiskResource resource, Set<DiskResourceMetadata> mdToUpdate,
             Set<DiskResourceMetadata> mdToDelete, AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "metadata-batch" //$NON-NLS-1$
                 + "?path=" + URL.encodePathSegment(resource.getId()); //$NON-NLS-1$
 
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
                 return AutoBeanCodex.decode(FACTORY, DiskResourceStatMap.class, json).as();
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
      * @param ticketIdToResourceIdMap the id of the disk resource for which the ticket will be created.
      * @param isPublicTicket
      * @param callback
      */
     @Override
     public void createDataLinks(Map<String, String> ticketIdToResourceIdMap,
             AsyncCallback<String> callback) {
         String fullAddress = DEProperties.getInstance().getDataMgmtBaseUrl() + "tickets"; //$NON-NLS-1$
         String args = "public=1";
 
         JSONObject body = new JSONObject();
         JSONArray tickets = new JSONArray();
         int index = 0;
         for (String ticketId : ticketIdToResourceIdMap.keySet()) {
             String resourceId = ticketIdToResourceIdMap.get(ticketId);
 
             JSONObject subBody = new JSONObject();
             subBody.put("path", new JSONString(resourceId));
             subBody.put("ticket-id", new JSONString(ticketId));
             tickets.set(index, subBody);
             index++;
         }
         body.put("tickets", tickets);
 
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
 
 }
