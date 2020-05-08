 /*******************************************************************************
  * Copyright 2009 Regents of the University of Minnesota. All rights
  * reserved.
  * Copyright 2009 Mayo Foundation for Medical Education and Research.
  * All rights reserved.
  *
  * This program is made available under the terms of the Eclipse
  * Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR
  * IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS
  * OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
  * PARTICULAR PURPOSE.  See the License for the specific language
  * governing permissions and limitations under the License.
  *
  * Contributors:
  * Minnesota Supercomputing Institute - initial API and implementation
  ******************************************************************************/
 
 package edu.umn.msi.tropix.persistence.dao.hibernate;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.ManagedBean;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.springframework.util.StringUtils;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 
 import edu.umn.msi.tropix.common.reflect.ReflectionHelper;
 import edu.umn.msi.tropix.common.reflect.ReflectionHelpers;
 import edu.umn.msi.tropix.models.DirectPermission;
 import edu.umn.msi.tropix.models.Folder;
 import edu.umn.msi.tropix.models.Group;
 import edu.umn.msi.tropix.models.InternalRequest;
 import edu.umn.msi.tropix.models.Permission;
 import edu.umn.msi.tropix.models.ProteomicsRun;
 import edu.umn.msi.tropix.models.Provider;
 import edu.umn.msi.tropix.models.Request;
 import edu.umn.msi.tropix.models.TropixFile;
 import edu.umn.msi.tropix.models.TropixObject;
 import edu.umn.msi.tropix.models.User;
 import edu.umn.msi.tropix.models.VirtualFolder;
 import edu.umn.msi.tropix.models.VirtualPermission;
 import edu.umn.msi.tropix.persistence.dao.TropixObjectDao;
 
 @ManagedBean
 @Named("tropixObjectDao")
 class TropixObjectDaoImpl extends TropixPersistenceTemplate implements TropixObjectDao {
   private static final Pattern ID_FROM_PATH_PATTERN = Pattern.compile(".*\\[id:(.+)\\]");
   private static final ReflectionHelper REFLECTION_HELPER = ReflectionHelpers.getInstance();
   private static final Log LOG = LogFactory.getLog(TropixObjectDaoImpl.class);
   private static final int DEFAULT_MAX_SEARCH_RESULTS = 1000;
   private int maxSearchResults = DEFAULT_MAX_SEARCH_RESULTS;
 
   /**
    * Override setSessionFactory so the @Inject annotation can be added to it.
    */
   @Inject
   public void setSessionFactory(@Named("sessionFactory") final SessionFactory sessionFactory) {
     super.setSessionFactory(sessionFactory);
   }
 
   public void setMaxSearchResults(final int maxSearchResults) {
     this.maxSearchResults = maxSearchResults;
   }
 
   public boolean isInstance(final String objectId, final Class<? extends TropixObject> clazz) {
     final TropixObject object = loadTropixObject(objectId);
     return clazz.isInstance(object);
   }
 
   public Collection<TropixObject> getFolderContents(final String folderId) {
     Collection<TropixObject> contents;
     final TropixObject parent = (TropixObject) get(TropixObject.class, folderId);
     if(parent instanceof Folder) {
       contents = ((Folder) parent).getContents();
     } else if(parent instanceof VirtualFolder) {
       contents = ((VirtualFolder) parent).getContents();
     } else if(parent instanceof Request) {
       contents = ((Request) parent).getContents();
     } else {
       throw new IllegalArgumentException("Id " + folderId + " doesn't correspond to a folder or virtual folder");
     }
     return contents;
   }
 
   public void addToFolder(final String folderId, final String objectId) {
     final Folder parent = (Folder) load(Folder.class, folderId);
     final TropixObject object = (TropixObject) load(TropixObject.class, objectId);
     object.setParentFolder(parent);
     parent.getContents().add(object);
     saveOrUpdate(object);
   }
 
   public void addToVirtualFolder(final String virtualFolderId, final String objectId) {
     final VirtualFolder parent = (VirtualFolder) load(VirtualFolder.class, virtualFolderId);
     final TropixObject object = (TropixObject) load(TropixObject.class, objectId);
     object.getParentVirtualFolders().add(parent);
     parent.getContents().add(object);
     saveOrUpdate(parent);
   }
 
   public void addPermissionParent(final String childId, final String parentId) {
     final TropixObject child = (TropixObject) load(TropixObject.class, childId);
     final TropixObject parent = (TropixObject) load(TropixObject.class, parentId);
     child.getPermissionParents().add(parent);
     parent.getPermissionChildren().add(child);
     saveOrUpdate(child);
     saveOrUpdate(parent);
   }
 
   public void removePermissionParent(final String childId, final String parentId) {
     final TropixObject child = (TropixObject) load(TropixObject.class, childId);
     final TropixObject parent = (TropixObject) load(TropixObject.class, parentId);
     child.getPermissionParents().remove(parent);
     parent.getPermissionChildren().remove(child);
     saveOrUpdate(child);
     saveOrUpdate(parent);
   }
 
   public TropixObject loadTropixObject(final String objectId) {
     final TropixObject object = (TropixObject) get(TropixObject.class, objectId);
     return object;
   }
 
   @SuppressWarnings("unchecked")
   public <T extends TropixObject> T loadTropixObject(final String objectId, final Class<? extends T> type) {
     return (T) get(type, objectId);
   }
 
   public List<TropixObject> loadTropixObjects(final String[] objectIds) {
     final List<TropixObject> objects = new ArrayList<TropixObject>(objectIds.length);
     for(final String id : objectIds) {
       objects.add(loadTropixObject(id));
     }
     return objects;
   }
 
   public <T extends TropixObject> List<T> loadTropixObjects(final String[] objectIds, final Class<? extends T> type) {
     final List<T> objects = new ArrayList<T>(objectIds.length);
     for(final String id : objectIds) {
       objects.add(loadTropixObject(id, type));
     }
     return objects;
   }
 
   public void saveOrUpdateTropixObject(final TropixObject object) {
     saveOrUpdate(object);
   }
 
   public String getOwnerId(final String objectId) {
     final Query query = getSession().getNamedQuery("getOwnerId");
     query.setParameter("objectId", objectId);
     return (String) query.uniqueResult();
   }
 
   public User getOwner(final String objectId) {
     final Query query = getSession().getNamedQuery("getOwner");
     query.setParameter("objectId", objectId);
     return (User) query.uniqueResult();
   }
 
   public void setOwner(final String objectId, final User user) {
     final Query query = super.getSession().createQuery(
         "select p from DirectPermission p inner join p.objects o where p.role = 'owner' and o.id = :objectId");
     query.setParameter("objectId", objectId);
     DirectPermission permission = (DirectPermission) query.uniqueResult();
     if(permission == null) {
       addRole(objectId, "owner", user);
     } else if(!Iterables.getOnlyElement(permission.getUsers()).getId().equals(user.getId())) {
       delete(permission); // This will need to change when there is only one owner permission...
       addRole(objectId, "owner", user);
     }
   }
 
   public void addRole(final String objectId, final String role, final User user) {
     final DirectPermission permission = new DirectPermission();
     final HashSet<User> users = new HashSet<User>();
     users.add(user);
     permission.setUsers(users);
     permission.setGroups(new HashSet<Group>());
     permission.setRole(role);
     final TropixObject object = loadTropixObject(objectId);
     final HashSet<TropixObject> objects = new HashSet<TropixObject>();
     objects.add(object);
     permission.setObjects(objects);
     saveOrUpdate(permission);
   }
 
   public void addGroupRole(final String objectId, final String role, final Group group) {
     final DirectPermission permission = new DirectPermission();
     final HashSet<User> users = new HashSet<User>();
     permission.setUsers(users);
     final HashSet<Group> groups = new HashSet<Group>();
     groups.add(group);
     permission.setGroups(groups);
     permission.setRole(role);
     final TropixObject object = loadTropixObject(objectId);
     final HashSet<TropixObject> objects = new HashSet<TropixObject>();
     objects.add(object);
     permission.setObjects(objects);
     saveOrUpdate(permission);
   }
 
   public void delete(final String objectId) {
     final TropixObject object = (TropixObject) load(TropixObject.class, objectId);
     final Query query = getSession().getNamedQuery("getRoles");
     query.setParameter("objectId", objectId);
     @SuppressWarnings("unchecked")
     final List<Permission> permissions = query.list();
     for(final Permission permission : permissions) {
       permission.getObjects().remove(object);
       super.saveOrUpdate(permission);
       super.saveOrUpdate(object);
     }
     for(final TropixObject parent : object.getPermissionParents()) {
       this.removePermissionParent(objectId, parent.getId());
     }
     for(final TropixObject child : object.getPermissionChildren()) {
       this.removePermissionParent(child.getId(), objectId);
     }
     super.delete(object);
   }
 
   public void move(final String objectId, final String folderId) {
     final TropixObject object = loadTropixObject(objectId, TropixObject.class);
     final Folder oldParent = object.getParentFolder();
     oldParent.getContents().remove(object);
     this.saveOrUpdateTropixObject(oldParent);
 
     this.addToFolder(folderId, objectId);
   }
 
   public List<TropixObject> quickSearchObjects(final String userId, final String queryStr) {
     final Query query = getSession().getNamedQuery("quickSearch");
     query.setParameter("userId", userId);
     query.setParameter("query", queryStr);
     query.setMaxResults(maxSearchResults);
     @SuppressWarnings("unchecked")
     final List<TropixObject> results = query.list();
     return results;
   }
 
   public List<TropixObject> searchObjects(final String userId, final Class<? extends TropixObject> objectType, final String name,
       final String description, final String ownerId) {
     final StringBuilder queryBuilder = new StringBuilder(getSession().getNamedQuery("baseSearch").getQueryString());
     if(StringUtils.hasText(description)) {
       queryBuilder.append(" and o.description like :description ");
     }
     if(StringUtils.hasText(name)) {
       queryBuilder.append(" and o.name like :name ");
     }
     if(StringUtils.hasText(ownerId)) {
       queryBuilder
           .append(" and (select count(*) from DirectPermission ownerRole join ownerRole.users owner join ownerRole.objects ownerObject where ownerObject.id=o.id and owner.cagridId = :ownerId and ownerRole.role='owner' ) > 0");
     }
     final String objectTypeStr = objectType == null ? "TropixObject" : objectType.getSimpleName();
     final String typeStr = "TropixObject";
     final int typePos = queryBuilder.indexOf(typeStr);
     queryBuilder.replace(typePos, typePos + typeStr.length(), objectTypeStr);
     final Query query = getSession().createQuery(queryBuilder.toString());
     query.setParameter("userId", userId);
     if(StringUtils.hasText(name)) {
       query.setParameter("name", name);
     }
     if(StringUtils.hasText(description)) {
       query.setParameter("description", description);
     }
     if(StringUtils.hasText(ownerId)) {
       query.setParameter("ownerId", ownerId);
     }
     query.setMaxResults(maxSearchResults);
     @SuppressWarnings("unchecked")
     final List<TropixObject> results = query.list();
 
     return results;
   }
 
   public List<TropixObject> getTopLevelObjects(final String userId, final String ownerId) {
     final Query query = super.getSession().getNamedQuery(ownerId == null ? "findTopLevelItems" : "findTopLevelItemsWithOwner");
     query.setParameter("userId", userId);
     if(ownerId != null) {
       query.setParameter("ownerId", ownerId);
     }
     query.setMaxResults(maxSearchResults);
     @SuppressWarnings("unchecked")
     final List<TropixObject> results = query.list();
     return results;
   }
 
   public TropixObject getAssociation(final String objectId, final String associationName) {
     final TropixObject object = loadTropixObject(objectId, TropixObject.class);
     return (TropixObject) REFLECTION_HELPER.getBeanProperty(object, associationName);
   }
 
   @SuppressWarnings("unchecked")
   public Collection<TropixObject> getAssociations(final String objectId, final String associationName) {
     final TropixObject object = loadTropixObject(objectId, TropixObject.class);
     return (Collection<TropixObject>) REFLECTION_HELPER.getBeanProperty(object, associationName);
   }
 
   private VirtualPermission loadVirtualPermission(final String objectId, final String role) {
     final Query query = super.getSession().getNamedQuery("loadVirtualPermission");
     query.setParameter("objectId", objectId);
     query.setParameter("role", role);
     return (VirtualPermission) query.uniqueResult();
   }
 
   public Collection<User> getVirtualPermissionUsers(final String objectId, final String role) {
     return loadVirtualPermission(objectId, role).getUsers();
   }
 
   public Collection<Group> getVirtualPermissionGroups(final String objectId, final String role) {
     return loadVirtualPermission(objectId, role).getGroups();
   }
 
   private Collection<VirtualPermission> loadVirtualPermission(final String objectId) {
     final Query query = super.getSession().getNamedQuery("loadVirtualPermissions");
     query.setParameter("objectId", objectId);
     @SuppressWarnings("unchecked")
     final List<VirtualPermission> perms = query.list();
     return perms;
   }
 
   public DirectPermission getUserDirectRole(final String userId, final String objectId) {
     final Query query = super.getSession().getNamedQuery("getUserDirectRole");
     query.setParameter("cagridId", userId);
     query.setParameter("objectId", objectId);
     @SuppressWarnings("unchecked")
     final List<DirectPermission> roles = query.list();
     if(roles.size() > 1) {
       LOG.warn("More than one direct permission exists for object " + objectId + " and user " + userId);
     }
     return roles.isEmpty() ? null : roles.get(0);
   }
 
   public DirectPermission getGroupDirectRole(final String groupId, final String objectId) {
     final Query query = super.getSession().getNamedQuery("getGroupDirectRole");
     query.setParameter("groupId", groupId);
     query.setParameter("objectId", objectId);
     @SuppressWarnings("unchecked")
     final List<DirectPermission> roles = query.list();
     if(roles.size() > 1) {
       LOG.warn("More than one direct permission exists for object " + objectId + " and group " + groupId);
     }
     return roles.isEmpty() ? null : roles.get(0);
   }
 
   public void addVirtualPermissionUser(final String objectId, final String role, final String userId) {
     final VirtualPermission vp = loadVirtualPermission(objectId, role);
     final User user = (User) find("from User u where u.cagridId = ?", new String[] {userId}).get(0);
     vp.getUsers().add(user);
     saveOrUpdate(vp);
   }
 
   public void addVirtualPermissionGroup(final String objectId, final String role, final String groupId) {
     final VirtualPermission vp = loadVirtualPermission(objectId, role);
     vp.getGroups().add((Group) load(Group.class, groupId));
     saveOrUpdate(vp);
   }
 
   public void removeVirtualPermissionGroup(final String objectId, final String role, final String groupId) {
     final VirtualPermission vp = loadVirtualPermission(objectId, role);
     vp.getGroups().remove(load(Group.class, groupId));
     saveOrUpdate(vp);
   }
 
   public void removeVirtualPermissionUser(final String objectId, final String role, final String userId) {
     final VirtualPermission vp = loadVirtualPermission(objectId, role);
     final User user = (User) find("from User u where u.cagridId = ?", new String[] {userId}).get(0);
     vp.getUsers().remove(user);
     saveOrUpdate(vp);
   }
 
   public void createVirtualPermission(final String objectId, final String role) {
     final VirtualPermission vp = new VirtualPermission();
     vp.setRole(role);
     vp.setGroups(new HashSet<Group>());
     vp.setUsers(new HashSet<User>());
     vp.setObjects(new HashSet<TropixObject>());
     vp.getObjects().add(loadTropixObject(objectId));
     vp.setRootVirtualFolder((VirtualFolder) load(VirtualFolder.class, objectId));
     saveOrUpdate(vp);
   }
 
   public Collection<DirectPermission> getRoles(final String objectId) {
     final Query query = getSession().getNamedQuery("getDirectRoles");
     query.setParameter("objectId", objectId);
     @SuppressWarnings("unchecked")
     final Collection<DirectPermission> roles = query.list();
     return roles;
   }
 
   public Collection<Folder> getGroupFolders(final String userId) {
     final Query query = super.getSession().getNamedQuery("getGroupFolders");
     query.setParameter("userId", userId);
     @SuppressWarnings("unchecked")
     final List<Folder> results = query.list();
     return results;
   }
 
   public Collection<Folder> getAllGroupFolders() {
     final Query query = super.getSession().getNamedQuery("getAllGroupFolders");
     @SuppressWarnings("unchecked")
     final List<Folder> results = query.list();
     return results;
   }
 
   public Collection<VirtualFolder> getSharedFolders(final String userId) {
     final Query query = super.getSession().getNamedQuery("getSharedFolders");
     query.setParameter("userId", userId);
     @SuppressWarnings("unchecked")
     final List<VirtualFolder> results = query.list();
     return results;
   }
 
   public void copyVirtualPermissions(final String fromObjectId, final String toObjectId) {
     final Collection<VirtualPermission> perms = loadVirtualPermission(fromObjectId);
     final TropixObject object = (TropixObject) load(TropixObject.class, toObjectId);
     for(final VirtualPermission perm : perms) {
       perm.getObjects().add(object);
       saveOrUpdate(perm);
     }
   }
 
   public void dropVirtualPermission(final String virtualFolderId, final String objectId) {
     final Collection<VirtualPermission> perms = loadVirtualPermission(virtualFolderId);
     final TropixObject object = (TropixObject) load(TropixObject.class, objectId);
     for(final VirtualPermission perm : perms) {
       perm.getObjects().remove(object);
       saveOrUpdate(perm);
     }
   }
 
   public void removeFromVirtualFolder(final String virtualFolderId, final String objectId) {
     final TropixObject object = (TropixObject) load(TropixObject.class, objectId);
     final VirtualFolder folder = (VirtualFolder) load(VirtualFolder.class, virtualFolderId);
     object.getParentVirtualFolders().remove(folder); // Inverse, don't need to save
     folder.getContents().remove(object);
     saveOrUpdate(folder);
   }
 
   public String getRootVirtualFolderId(final String virtualFolderId) {
     final VirtualPermission vp = loadVirtualPermission(virtualFolderId).iterator().next();
     return vp.getRootVirtualFolder().getId();
   }
 
   public boolean isAnOwner(final String userId, final String objectId) {
     final Query query = super.getSession().getNamedQuery("isUserOwner");
     query.setParameter("objectId", objectId);
     query.setParameter("userId", userId);
     if((Long) query.uniqueResult() > 0L) {
       return true;
     } else {
       final Query groupQuery = super.getSession().getNamedQuery("isGroupOwner");
       groupQuery.setParameter("objectId", objectId);
       groupQuery.setParameter("userId", userId);
       return (Long) groupQuery.uniqueResult() > 0L;
     }
   }
 
   public boolean fileExists(final String fileId) {
     final Query query = super.getSession().createQuery("select count(*) from TropixFile f where f.fileId = :fileId");
     query.setParameter("fileId", fileId);
     return 0L < (Long) query.uniqueResult();
   }
 
   public String getFilesObjectId(final String fileId) {
     final Query query = super.getSession().createQuery("select f.id from TropixFile f where f.fileId = :fileId");
     query.setParameter("fileId", fileId);
     return (String) query.uniqueResult();
   }
 
   public Set<String> getFilesObjectIds(final Set<String> fileIds) {
     final Set<String> objectIds = Sets.newHashSet();
     for(final Iterable<String> fileIdsPartition : Iterables.partition(fileIds, 100)) {
       final Query query = super.getSession().createQuery("select f.id from TropixFile f where f.fileId in (:fileIds)");
       query.setParameterList("fileIds", Lists.newArrayList(fileIdsPartition));
       @SuppressWarnings("unchecked")
       List<String> partitionObjectIds = query.list();
       objectIds.addAll(partitionObjectIds);
     }
     return objectIds;
   }
 
   public long virtualHierarchyCount(final String objectId, final String rootId) {
     final Query query = super.getSession().getNamedQuery("virtualHierarchyCount");
     query.setParameter("objectId", objectId);
     query.setParameter("rootId", rootId);
     return (Long) query.uniqueResult();
   }
 
   public long ownedObjectsVirtualHierarchyCount(final String userId, final String rootVirtualFolderId) {
     final Query query = super.getSession().getNamedQuery("objectsInVirtualHierarchy");
     query.setParameter("userId", userId);
     query.setParameter("rootId", rootVirtualFolderId);
     return (Long) query.uniqueResult();
   }
 
   public TropixFile loadTropixFileWithFileId(final String fileId) {
     final Query query = super.getSession().createQuery("from TropixFile f where f.fileId = :fileId");
     query.setParameter("fileId", fileId);
     return (TropixFile) query.uniqueResult();
   }
 
   public boolean ownsSharedFolderWithName(final String userGridId, final String name) {
     final Query query = getSession().getNamedQuery("ownsSharedFolderWithName");
     query.setParameter("name", name);
     query.setParameter("userId", userGridId);
     final long result = (Long) query.uniqueResult();
     return result != 0L;
   }
 
   @SuppressWarnings("unchecked")
   public Collection<TropixObject> loadRecent(final String userId, final int inputNum, final boolean includeFolders, final boolean requireParent) {
     final int num = inputNum > maxSearchResults ? maxSearchResults : inputNum;
     final Query query = super.getSession().getNamedQuery("loadRecent");
     query.setMaxResults(num);
     query.setParameter("userId", userId);
     query.setParameter("allowFolder", includeFolders);
     query.setParameter("requireParent", requireParent);
     return query.list();
   }
 
   public Request loadRequest(final String requestorId, final String externalId) {
     final Query query = super.getSession().createQuery("from Request r where r.externalId = :externalId and r.requestorId = :requestorId");
     query.setParameter("externalId", externalId);
     query.setParameter("requestorId", requestorId);
     return (Request) query.uniqueResult();
   }
 
   public Collection<Request> getActiveRequests(final String gridId) {
     final Query query = super.getSession().getNamedQuery("getActiveRequests");
     query.setParameter("userGridId", gridId);
     @SuppressWarnings("unchecked")
     final List<Request> results = query.list();
     return results;
   }
 
   public Collection<InternalRequest> getInternalRequests(final String gridId) {
     final Query query = super.getSession().getNamedQuery("getInternalRequests");
     query.setParameter("userGridId", gridId);
     @SuppressWarnings("unchecked")
     final List<InternalRequest> results = query.list();
     return results;
   }
 
   public Collection<ProteomicsRun> getRunsFromScaffoldAnalysis(final String scaffoldId) {
     final Query query = super.getSession().getNamedQuery("getRunsFromScaffoldAnalysis");
     query.setParameter("scaffoldId", scaffoldId);
     @SuppressWarnings("unchecked")
     final List<ProteomicsRun> runs = query.list();
     return runs;
   }
 
   /*
    * public Request getObjectsRequest(String objectId) { final Query query =
    * super.getSession().createQuery("select r from Request r, TropixObject o where o.id = :objectId and o member of r.contents");
    * query.setParameter("objectId", objectId); return (Request)
    * query.uniqueResult(); }
    */
 
   public boolean isAHomeFolder(final String id) {
     final Query query = super.getSession().createQuery("select f from Folder f, User u where f.id = :id and u.homeFolder = f");
     query.setParameter("id", id);
     return !query.list().isEmpty();
   }
 
   /*
    * private TropixObject getPathFromId(final String userId, final List<String> pathParts) {
    * TropixObject object = null;
    * if(!pathParts.isEmpty()) {
    * final String lastPart = pathParts.get(pathParts.size());
    * 
    * }
    * return object;
    * }
    */
 
   private void addConstraintForPathPart(final String pathPart, final int index, final StringBuilder whereBuilder, final LinkedList<String> parameters) {
     final Matcher matcher = ID_FROM_PATH_PATTERN.matcher(pathPart);
     final boolean containsId = matcher.matches();
     if(containsId) {
       whereBuilder.append(String.format(" and o%d.id = :o%dconst", index, index));
       parameters.addFirst(matcher.group(1));
     } else {
       parameters.addFirst(pathPart);
       whereBuilder.append(String.format(" and o%d.name = :o%dconst", index, index));
     }
   }
 
   public TropixObject getHomeDirectoryPath(final String userId, final List<String> pathParts) {
     if(LOG.isDebugEnabled()) {
       LOG.debug(String.format("getPath called with userId %s and path parts %s", userId, Iterables.toString(pathParts)));
     }
     final StringBuilder joins = new StringBuilder(), wheres = new StringBuilder();
     final ListIterator<String> pathPartsIter = pathParts.listIterator(pathParts.size());
     final LinkedList<String> parameters = Lists.newLinkedList();
     while(pathPartsIter.hasPrevious()) {
       int index = pathPartsIter.previousIndex() + 1;
       final String pathPart = pathPartsIter.previous();
 
       int nextObjectBackIndex = pathPartsIter.previousIndex() + 1;
       joins.append(String.format(" inner join o%d.permissionParents as o%d ", index, nextObjectBackIndex));
       wheres.append(String.format(" and o%d.deletedTime is null", index));
       wheres.append(String.format(" and o%d.committed is true", index));
       addConstraintForPathPart(pathPart, index, wheres, parameters);
     }
 
     final String queryString = String.format("select %s from  User u, TropixObject o%d %s where u.cagridId = :userId %s and u.homeFolder.id = o0.id",
         String.format("o%d", pathParts.size()),
         pathParts.size(),
         joins.toString(), wheres.toString());
     return executePathQuery(userId, queryString, 1, parameters);
   }
 
   private TropixObject executePathQuery(final String userId, final String queryString, final int firstIndex, final List<String> parameters) {
     final Query query = super.getSession().createQuery(queryString);
     query.setParameter("userId", userId);
     int index = firstIndex;
     for(String parameter : parameters) {
       String parameterName = String.format("o%dconst", index++);
       query.setParameter(parameterName, parameter);
     }
     final TropixObject result = (TropixObject) query.uniqueResult();
     return result;
   }
 
   public TropixObject getGroupDirectoryPath(final String userId, final List<String> pathParts) {
     final StringBuilder joins = new StringBuilder(), wheres = new StringBuilder();
     final ListIterator<String> pathPartsIter = pathParts.listIterator(pathParts.size());
     final LinkedList<String> parameters = Lists.newLinkedList();
     while(pathPartsIter.hasPrevious()) {
       int index = pathPartsIter.previousIndex();
       final String pathPart = pathPartsIter.previous();
       wheres.append(String.format(" and o%d.deletedTime is null", index));
       wheres.append(String.format(" and o%d.committed is true", index));
       addConstraintForPathPart(pathPart, index, wheres, parameters);
       if(pathPartsIter.hasPrevious()) {
         int nextObjectBackIndex = pathPartsIter.previousIndex();
         joins.append(String.format(" inner join o%d.permissionParents as o%d ", index, nextObjectBackIndex));
       }
     }
 
     final String queryString = String
         .format(
            "select %s from TropixObject o%d %s inner join o0.permissions p left join p.users u left join p.groups g left join g.users gu where (u.cagridId = :userId or gu.cagridId = :userId) and o0.parentFolder is null %s",
            String.format("o%d", pathParts.size() - 1),
            pathParts.size() - 1,
             joins.toString(),
             wheres.toString());
    System.out.println(queryString);
     return executePathQuery(userId, queryString, 0, parameters);
   }
 
   // TODO:
   public TropixObject getSharedDirectoryPath(final String userId, final List<String> asList) {
     return null;
   }
 
   public Multimap<String, String> getRoles(final String userIdentity, final Iterable<String> objectIds) {
     final Multimap<String, String> roleMap = HashMultimap.create();
     final Iterable<List<String>> objectIdPartitions = Iterables.partition(objectIds, 500);
     for(List<String> objectIdPartition : objectIdPartitions) {
       final Query query = getSession().getNamedQuery("getRolesForObjects");
       query.setParameter("userId", userIdentity);
       query.setParameterList("objectIds", objectIdPartition);
       @SuppressWarnings("unchecked")
       final List<Object[]> results = query.list();
       for(Object[] result : results) {
         roleMap.put((String) result[0], (String) result[1]);
       }
     }
     return roleMap;
   }
 
   public Collection<Provider> getProviderRoles(String objectId) {
     final Query query = getSession().getNamedQuery("getProviderRoles");
     query.setParameter("objectId", objectId);
     @SuppressWarnings("unchecked")
     final Collection<Provider> roles = query.list();
     return roles;
   }
 
   public boolean filesExistAndCanReadAll(String[] fileIds, String callerIdentity) {
     boolean allReadable = true;
     for(final List<String> fileIdsPartition : partition(Arrays.asList(fileIds))) {
       final Query query = getSession().getNamedQuery("filesExistAndCanReadAll");
       query.setParameter("userId", callerIdentity);
       query.setParameterList("fileIds", fileIdsPartition);
       final Long partitionReadableCount = (Long) query.uniqueResult();
       allReadable = partitionReadableCount >= fileIdsPartition.size();
       if(!allReadable) {
         break;
       }
     }
     return allReadable;
   }
 
 }
