 package org.mule.galaxy.impl.jcr;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jcr.AccessDeniedException;
 import javax.jcr.ItemExistsException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.jackrabbit.util.ISO9075;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.AttachedWorkspace;
 import org.mule.galaxy.ContentHandler;
 import org.mule.galaxy.ContentService;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Entry;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.event.EntryMovedEvent;
 import org.mule.galaxy.event.EventManager;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.impl.jcr.query.QueryBuilder;
 import org.mule.galaxy.impl.jcr.query.SimpleQueryBuilder;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.query.AbstractFunction;
 import org.mule.galaxy.query.FunctionCall;
 import org.mule.galaxy.query.FunctionRegistry;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.query.OpRestriction.Operator;
 import org.mule.galaxy.security.AccessControlManager;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.type.TypeManager;
 import org.mule.galaxy.util.BundleUtils;
 import org.mule.galaxy.util.Message;
 import org.mule.galaxy.util.SecurityUtils;
 import org.mule.galaxy.workspace.WorkspaceManager;
 import org.mule.galaxy.workspace.WorkspaceManagerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.dao.DataAccessException;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 public class JcrRegistryImpl extends JcrTemplate implements Registry, ApplicationContextAware {
     private final Log log = LogFactory.getLog(getClass());
 
     private String id;
     
     private String workspacesId;
     
     private ContentService contentService;
 
     private FunctionRegistry functionRegistry;
     
     private LifecycleManager lifecycleManager;
     
     private PolicyManager policyManager;
     
     private UserManager userManager;
     
     private AccessControlManager accessControlManager;
 
     private EventManager eventManager;
     
     private SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder(new String[0]);
 
     private JcrWorkspaceManager localWorkspaceManager;
     
     private Map<String, WorkspaceManager> idToWorkspaceManager = new HashMap<String, WorkspaceManager>();
     
     private List<Extension> extensions;
 
     private List<QueryBuilder> queryBuilders;
     
     private TypeManager typeManager;
     
     private ApplicationContext context;
     
     public JcrRegistryImpl() {
         super();
         
         simpleQueryBuilder.addAppliesTo(Artifact.class);
         simpleQueryBuilder.addAppliesTo(Entry.class);
     }
 
     public String getUUID() {
         return id;
     }
 
     public Collection<Workspace> getWorkspaces() throws AccessException {
         return localWorkspaceManager.getWorkspaces();
     }
 
     private Workspace buildWorkspace(Node node) throws RepositoryException {
         return new JcrWorkspace(localWorkspaceManager, node);
     }
 
     public Workspace newWorkspace(final String name) throws RegistryException, AccessException, DuplicateItemException {
         // we should throw an error, but lets be defensive for now
         final String escapedName = JcrUtil.escape(name);
 
         return localWorkspaceManager.newWorkspace(escapedName);
     }
     
     public Collection<WorkspaceManager> getWorkspaceManagers() {
         return idToWorkspaceManager.values();
     }
 
     @SuppressWarnings("unchecked")
     public Collection<AttachedWorkspace> getAttachedWorkspaces() {
         return (Collection<AttachedWorkspace>) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 
                 QueryResult result = qm.createQuery("//element(*, galaxy:attachedWorkspace)", Query.XPATH).execute();
                 
                 List<AttachedWorkspace> workspaces = new ArrayList<AttachedWorkspace>();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     workspaces.add(new JcrAttachedWorkspace(node, localWorkspaceManager));
                 }
                 
                 return workspaces;
             }
             
         });
     }
 
     public AttachedWorkspace attachWorkspace(final Workspace parent, 
                                              final String name,
                                              final String factory,
                                              final Map<String, String> configuration) throws RegistryException {
         
         if (parent != null && !(parent instanceof JcrWorkspace)) {
             throw new RegistryException(new Message("LOCAL_ATTACH_ONLY", BundleUtils.getBundle(this.getClass())));
         }
         
         return (AttachedWorkspace) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node parentNode;
                 if (parent != null) {
                     JcrWorkspace w = (JcrWorkspace) parent;
                     parentNode = w.getNode();
                 } else {
                     parentNode = getNodeByUUID(workspacesId);
                 }
                 
                 Node attachedNode = parentNode.addNode(name, JcrWorkspaceManager.ATTACHED_WORKSPACE_NODE_TYPE);
                 attachedNode.addMixin("mix:referenceable");
                 Calendar now = Calendar.getInstance();
                 now.setTime(new Date());
                 attachedNode.setProperty(AbstractJcrItem.CREATED, now);
                 attachedNode.setProperty(AbstractJcrItem.UPDATED, now);
                 attachedNode.setProperty(AbstractJcrItem.NAME, name);
                 attachedNode.setProperty(JcrAttachedWorkspace.WORKSPACE_MANAGER_FACTORY, factory);
 
                 try {
                     
                     JcrAttachedWorkspace attached = new JcrAttachedWorkspace(attachedNode, localWorkspaceManager);
                     attached.setConfiguration(configuration);
                     createWorkspaceManager(attached);
                     
                     session.save();
                     
                     return attached;
                 } catch (RegistryException e) {
                     throw new RuntimeException(e);
                 }
             }
             
         });
     }
 
     public WorkspaceManager getWorkspaceManager(AttachedWorkspace w) throws RegistryException {
         WorkspaceManager wm = idToWorkspaceManager.get(trimWorkspaceManagerId(w.getId()));
         
         if (wm == null) {
             wm = createWorkspaceManager(w);
         }
         
         return wm;
     }
 
     private synchronized WorkspaceManager createWorkspaceManager(AttachedWorkspace w) throws RegistryException {
         WorkspaceManagerFactory wmf = (WorkspaceManagerFactory) context.getBean(w.getWorkspaceManagerFactory());
         if (wmf == null) {
             throw new RegistryException(new Message("INVALID_WORKSPACE_MANAGER", BundleUtils.getBundle(getClass())));
         }
 
         WorkspaceManager wm = wmf.createWorkspaceManager(w.getConfiguration());
         wm.attachTo(w);
         wm.validate();
         idToWorkspaceManager.put(trimWorkspaceManagerId(w.getId()), wm);
         
         return wm;
     }
 
     public void save(Item i) throws AccessException, RegistryException {
         getWorkspaceManagerByItemId(i.getId()).save(i);
     }
 
     public void save(final Workspace w, final String _parentId) 
         throws RegistryException, NotFoundException, AccessException, DuplicateItemException {
         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, w);
 
         executeWithNotFoundDuplicate(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 AbstractJcrItem jw = (AbstractJcrItem) w;
                 Node node = jw.getNode();
                 
                 String parentId = _parentId;
                 if ("root".equals(parentId)) {
                     parentId = workspacesId;
                 } else if (parentId != null) {
                     parentId = trimWorkspaceManagerId(parentId);
                 }
                 
                 if (parentId != null && !parentId.equals(node.getParent().getUUID())) {
                     Node parentNode = null;
                     try {
                         parentNode = getNodeByUUID(parentId);
                     } catch (DataAccessException e) {
                         throw new RuntimeException(new NotFoundException(parentId));
                     }
                     
                     // ensure the user isn't trying to move this to a child node of the workspace
                     Node checked = parentNode;
                     while (checked != null && checked.getPrimaryNodeType().getName().equals("galaxy:workspace")) {
                         if (checked.equals(node)) {
                             throw new RuntimeException(new RegistryException(new Message("MOVE_ONTO_CHILD", BundleUtils.getBundle(getClass()))));
                         }
                         
                         checked = checked.getParent();
                     }
                     
                     JcrWorkspace toWkspc = new JcrWorkspace(localWorkspaceManager, parentNode);
                     try {
                         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, toWkspc);
                     } catch (AccessException e) {
                         throw new RuntimeException(e);
                     }
                     
                     String dest = parentNode.getPath() + "/" + w.getName();
                     try {
                         session.move(node.getPath(), dest);
                     } catch (ItemExistsException e) {
                         throw new RuntimeException(new DuplicateItemException(dest));
                     }
                 }
                 
                 session.save();
                 
                 return null;
             }
         });
     }
 
 
     public Item resolve(Item item, String location) throws RegistryException {
         String[] paths = location.split("/");
         
         while (!(item instanceof Workspace)) {
             item = item.getParent();
         }
         
         Workspace w = (Workspace) item;
 
         try {
             for (int i = 0; i < paths.length - 1 && w != null; i++) {
                 String p = paths[i];
                 
                 // TODO: escaping?
                 if (p.equals("..")) {
                     w = ((Workspace)w.getParent());
                 } else if (!p.equals(".")) {
                    w = w.getWorkspace(p);
                 }
             }
             
             if (w == null) {
         	return null;
             }
             
             return w.getItem(paths[paths.length-1]);
         } catch (NotFoundException e) {
             return null;
         } catch (AccessException e) {
             return null;
         }
     }
     
     public Node getWorkspacesNode() {
         return getNodeByUUID(workspacesId);
     }
 
     private WorkspaceManager getWorkspaceManager(String wmId) {
         WorkspaceManager wm = idToWorkspaceManager.get(wmId);
         if (wm == null) {
             throw new IllegalStateException();
         }
         return wm;
     }
 
     private WorkspaceManager getWorkspaceManagerByItemId(String itemId) {
         int idx = itemId.indexOf(WORKSPACE_MANAGER_SEPARATOR);
 
         if (idx == -1) {
             throw new IllegalStateException("Invalid item id: " + itemId);
         }
 
         return getWorkspaceManager(itemId.substring(0, idx));
     }
 
     public Item getItemById(final String id) throws NotFoundException, RegistryException, AccessException {
         return getWorkspaceManagerByItemId(id).getItemById(id);
     }
     
     protected Item build(Node node, String type) throws RepositoryException, ItemNotFoundException,
         AccessDeniedException, AccessException, RegistryException {
         if (type.equals("galaxy:artifact")) {
             Artifact a = buildArtifact(node);
     
             accessControlManager.assertAccess(Permission.READ_ARTIFACT, a);
             
             return a;
         } else if (type.equals("galaxy:artifactVersion")) {
             Artifact a = buildArtifact(node.getParent());
     
             accessControlManager.assertAccess(Permission.READ_ARTIFACT, a);
             
             return a.getVersion(node.getName());
         } else if (type.equals("galaxy:entry")) {
             Entry a = buildEntry(node);
     
             accessControlManager.assertAccess(Permission.READ_ARTIFACT, a);
             
             return a;
         } else if (type.equals("galaxy:entryVersion")) {
             Entry a = buildEntry(node.getParent());
     
             accessControlManager.assertAccess(Permission.READ_ARTIFACT, a);
             
             return a.getVersion(node.getName());
         } else if (type.equals("galaxy:attachedWorkspace")) {
             AttachedWorkspace w = new JcrAttachedWorkspace(node, localWorkspaceManager); 
     
             accessControlManager.assertAccess(Permission.READ_ARTIFACT, w);
             
             return w;
         } else {
              Workspace wkspc = buildWorkspace(node);
              
              accessControlManager.assertAccess(Permission.READ_WORKSPACE, wkspc);
              
              return wkspc;
         }
     }
     
     public Item getItemByPath(String path) throws RegistryException, NotFoundException, AccessException {
         return localWorkspaceManager.getItemByPath(path);
     }
     
     protected void setupContentHandler(JcrArtifact artifact) {
         ContentHandler ch = null;
         if (artifact.getDocumentType() != null) {
             ch = contentService.getContentHandler(artifact.getDocumentType());
         } else {
             ch = contentService.getContentHandler(artifact.getContentType());
         }
         artifact.setContentHandler(ch);
     }
 
     private String trimWorkspaceManagerId(String id) {
         int idx = id.indexOf(WORKSPACE_MANAGER_SEPARATOR);
         if (idx == -1) {
             throw new IllegalStateException("Illegal workspace manager id.");
         }
 
         return id.substring(idx + 1);
     }
 
     private Object executeWithNotFoundDuplicate(JcrCallback jcrCallback) 
         throws RegistryException, NotFoundException, AccessException, DuplicateItemException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
             } else if (cause instanceof NotFoundException) {
                 throw (NotFoundException) cause;
             } else if (cause instanceof DuplicateItemException) {
                 throw (DuplicateItemException) cause;
             } else if (cause instanceof AccessException) {
                 throw (AccessException) cause;
             } else {
                 throw e;
             }
         }
     }
     private Object executeWithQueryException(JcrCallback jcrCallback) 
         throws RegistryException, QueryException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
             } else if (cause instanceof QueryException) {
                 throw (QueryException) cause;
             } else {
                 throw e;
             }
         }
     }
     
     public void move(final Entry entry, final String newWorkspacePath, final String newName) throws RegistryException, AccessException, NotFoundException {
         boolean wasRenamed = false;
         boolean wasMoved = false;
         final String oldPath = entry.getPath();
 
         try {
             // handle artifact renaming
             accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, entry);
 
             if (!entry.getName().equals(newName)) {
                 // save only if name changed
                 entry.setName(newName);
                 save(entry);
                 wasRenamed = true;
             }
 
             // handle workspace move
             final Workspace workspace = (Workspace) getItemByPath(newWorkspacePath);
             accessControlManager.assertAccess(Permission.MODIFY_WORKSPACE, workspace);
 
             // only if workspace changed
             if (!entry.getParent().getId().equals(workspace.getId())) {
 
                 execute(new JcrCallback() {
                     public Object doInJcr(Session session) throws IOException, RepositoryException {
 
                         Node aNode = ((JcrArtifact) entry).getNode();
                         Node wNode = ((JcrWorkspace) workspace).getNode();
 
                         final String newPath = wNode.getPath() + "/" + aNode.getName();
                         session.move(aNode.getPath(), newPath);
 
                         session.save();
                         ((JcrArtifact) entry).setWorkspace(workspace);
                         return null;
                     }
                 });
 
                 wasMoved = true;
             }
         } finally {
             // fire an event only if there was an actual action taken, and guarantee it will be fired
             if (wasRenamed || wasMoved) {
                 EntryMovedEvent event = new EntryMovedEvent(entry, oldPath);
                 event.setUser(SecurityUtils.getCurrentUser());
                 eventManager.fireEvent(event);
             }
         }
     }
 
     
     public SearchResults suggest(final String p, final int maxResults, final String excludePath, final Class... types)
         throws RegistryException, QueryException {
         return (SearchResults) executeWithQueryException(new JcrCallback() {
             
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 
                 String path = p;
                 
                 StringBuilder qstr = new StringBuilder();
                 qstr.append("/");
                 
                 boolean startsWithExact = path.startsWith("/");
                 if (startsWithExact) path = path.substring(1);
                 
                 boolean endsWithExact = path.startsWith("/");
                 if (endsWithExact) path = path.substring(0, path.length());
                 
                 String[] paths = path.split("/");
                 
                 if (paths.length == 0) {
                     Set<Item> empty = Collections.emptySet();
                     return new SearchResults(0, empty);
                 }
                 
                 for (int i = 0; i < paths.length; i++) {
                     if (paths[i].equals("")) {
                         startsWithExact = true;
                         continue;
                     }
                         
                     if (i == 0 && startsWithExact) {
                         qstr.append("/*[jcr:like(fn:lower-case(@name), '").append(paths[i].toLowerCase()).append("%')]");
                     } else if (i == (paths.length - 1) && endsWithExact) {
                         qstr.append("/*[jcr:like(fn:lower-case(@name), '%").append(paths[i].toLowerCase()).append("')]");
                     } else {
                         qstr.append("/*[jcr:like(fn:lower-case(@name), '%").append(paths[i].toLowerCase()).append("%')]");
                     }
                 }
                 
                 qstr.append("[");
                 List<String> allowedTypes = new ArrayList<String>();
                 for (Class selectType : types) {
                     if (selectType.equals(Entry.class)) {
                         allowedTypes.add("galaxy:entry");
                     } else if (selectType.equals(Artifact.class)) {
                         allowedTypes.add("galaxy:artifact");
                     } else if (selectType.equals(EntryVersion.class)) {
                         allowedTypes.add("galaxy:entryVersion");
                     } else if (selectType.equals(ArtifactVersion.class)) {
                         allowedTypes.add("galaxy:artifactVersion");
                     } else if (selectType.equals(Workspace.class)) {
                         allowedTypes.add("galaxy:workspace");
                     } else {
                         throw new RuntimeException(new QueryException(new Message("INVALID_SELECT_TYPE", BundleUtils.getBundle(getClass()))));
                     }
                 }
                 
                 for (int i = 0; i < allowedTypes.size(); i++) {
                     if (i > 0) {
                         qstr.append(" or ");
                     }
                     String type = allowedTypes.get(i);
                     qstr.append("@jcr:primaryType='").append(type)
                         .append("' or *//@jcr:primaryType='").append(type).append("'");
                 }
                 qstr.append("]");
 
                 QueryResult result = qm.createQuery(qstr.toString(), Query.XPATH).execute();
                 
                 Set<Item> results = new HashSet<Item>();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
 
                     try {
                         addNodes(node, allowedTypes, results);
                     } catch (RegistryException e) {
                         throw new RuntimeException(e);
                     }
                     
                     if (results.size() >= maxResults) {
                         break;
                     }
                 }
                 
                 return new SearchResults(results.size(), results);
             }
 
             private void addNodes(Node node, List<String> allowedTypes,
                                   Set<Item> results) throws RepositoryException, ItemNotFoundException,
                 AccessDeniedException, RegistryException {
                 String type = node.getPrimaryNodeType().getName();
                 if (allowedTypes.contains(type)) {
                     try {
                         Item item = build(node, type);
                         if (excludePath != null && item.getPath().startsWith(excludePath)) {
                             return;
                         }
                         results.add(item);
                     } catch (AccessException e) {
                     }
                     
                     
                 }
                 
                 if (results.size() == maxResults) {
                     return;
                 }
                 
                 for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
                     addNodes(nodes.nextNode(), allowedTypes, results);
                     
                     if (results.size() == maxResults) {
                         return;
                     }
                 }
             }
         });
     }
 
     private QueryManager getQueryManager(Session session) throws RepositoryException {
         return session.getWorkspace().getQueryManager();
     }
 
     public SearchResults search(String queryString, int startOfResults, int maxResults) throws RegistryException, QueryException {
         org.mule.galaxy.query.Query q = org.mule.galaxy.query.Query.fromString(queryString);
 
         q.setStart(startOfResults);
         q.setMaxResults(maxResults);
         
         return search(q);
     }
 
     public SearchResults search(final org.mule.galaxy.query.Query query) 
         throws RegistryException, QueryException {
         return (SearchResults) executeWithQueryException(new JcrCallback() {
             
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                         
                 QueryManager qm = getQueryManager(session);
                 
                 Set<Item> items = new HashSet<Item>();
                 
                 Map<FunctionCall, AbstractFunction> functions = new HashMap<FunctionCall, AbstractFunction>();
                 
                 String qstr = null;
                 try {
                     qstr = createQueryString(query, functions);
                 } catch (QueryException e) {
                     // will be dewrapped later
                     throw new RuntimeException(e);
                 }
 
                 if (qstr == null) {
                     return new SearchResults(0, items);
                 }
                 
                 if (log.isDebugEnabled()) {
                     log.debug("Query: " + qstr.toString());
                 }
                 
                 Query jcrQuery = qm.createQuery(qstr, Query.XPATH);
                 
                 QueryResult result = jcrQuery.execute();
                 NodeIterator nodes = result.getNodes(); 
                
                 if (query.getStart() != -1) {
                     if (nodes.getSize() <= query.getStart()) {
                         return new SearchResults(0, items);
                     } else {
                         nodes.skip(query.getStart());
                     }
                 }
                 
                 int max = query.getMaxResults();
                 int count = 0;
                 int filteredCount = 0;
                 while (nodes.hasNext()) {
                     Node node = nodes.nextNode();
                     
                     boolean filtered = false;
                     try {
                         Item item = build(node, node.getPrimaryNodeType().getName());
                         
                         for (Map.Entry<FunctionCall, AbstractFunction> e : functions.entrySet()) {
                             if (e.getValue().filter(e.getKey().getArguments(), item)) {
                                 filtered = true;
                                 filteredCount++;
                                 break;
                             }
                         }
                         
                         if (!filtered) {
                             count++;
                             items.add(item);
                         }
                         
                         if (count == max) {
                             break;
                         }
                     } catch (AccessException e1) {
                     } catch (RegistryException e) {
                         throw new RuntimeException(e);
                     }
                 }                                                   
 
                 long total = nodes.getSize() - filteredCount;
                 
                 return new SearchResults(total, items);
             }
 
         });
     }
 
     protected JcrArtifact buildArtifact(Node node) throws RepositoryException, ItemNotFoundException,
         AccessDeniedException {
         JcrArtifact artifact = new JcrArtifact(
                                         new JcrWorkspace(localWorkspaceManager, node.getParent()),
                                         node, localWorkspaceManager);
 
         setupContentHandler(artifact);
         return artifact;
     }
 
     protected JcrEntry buildEntry(Node node) throws RepositoryException, ItemNotFoundException,
         AccessDeniedException {
         JcrEntry artifact = new JcrEntry(new JcrWorkspace(localWorkspaceManager, node.getParent()),
                                          node, localWorkspaceManager);
         return artifact;
     }
     
     protected String createQueryString(final org.mule.galaxy.query.Query query, 
                                        Map<FunctionCall, AbstractFunction> functions) throws QueryException {
         StringBuilder base = new StringBuilder();
         
         // Set up the type selection - artifact, entry, artifactVersion, etc
         StringBuilder typeQuery = new StringBuilder("[");
         String all = "@jcr:primaryType=\"galaxy:entry\" or @jcr:primaryType=\"galaxy:entryVersion\"" +
             " or @jcr:primaryType=\"galaxy:artifact\" or @jcr:primaryType=\"galaxy:artifactVersion\"" +
             " or @jcr:primaryType=\"galaxy:workspace\"";
         
         boolean searchEntries = false;
         boolean searchVersions = false;
         
         for (Class<?> selectType : query.getSelectTypes()) {
             if (typeQuery.length() > 1) {
                 typeQuery.append(" or ");
             }
             
             if (selectType == Item.class) {
                 typeQuery.append(all);
                 break;
             } else if (selectType.equals(Entry.class)) {
                 typeQuery.append("@jcr:primaryType=\"galaxy:entry\"");
                 searchEntries = true;
             } else if (selectType.equals(Artifact.class)) {
                 typeQuery.append("@jcr:primaryType=\"galaxy:artifact\"");
                 searchEntries = true;
             } else if (selectType.equals(EntryVersion.class)) {
                 typeQuery.append("@jcr:primaryType=\"galaxy:entryVersion\"");
                 searchVersions = true;
             } else if (selectType.equals(ArtifactVersion.class)) {
                 typeQuery.append("@jcr:primaryType=\"galaxy:artifactVersion\"");
                 searchVersions = true;
             } else if (selectType.equals(Workspace.class)) {
                 typeQuery.append("@jcr:primaryType=\"galaxy:workspace\"");
             } else {
                 throw new QueryException(new Message("INVALID_SELECT_TYPE", BundleUtils.getBundle(getClass())));
             }
         }
 
         if (typeQuery.length() == 1) {
             typeQuery.append(all);
         }
         
         typeQuery.append(']');
         
         // Search by workspace id, workspace path, or any workspace
         if (query.getFromId() != null) {
             base.append("//*[@jcr:uuid='")
                 .append(trimWorkspaceManagerId(query.getFromId()))
                 .append("']");
 
             if (query.isFromRecursive()) {
                 base.append("//*");
             } else {
                 base.append("/*");
 
                 if (searchVersions) {
                     base.append("/*");
                 }
             }
             
             base.append(typeQuery);
         } else if (query.getFromPath() != null && !"".equals(query.getFromPath())) {
             String path = query.getFromPath();
 
             if (path.startsWith("/")) {
                 path = path.substring(1);
             }
             
             if (path.endsWith("/")) {
                 path = path.substring(0, path.length() - 1);
             }
             
             base.append("//")
             .append(ISO9075.encode(path))
             .append("");
             
             if (query.isFromRecursive()) {
                 base.append("//*");
             } else {
                 base.append("/*");
                 
                 if (searchVersions) {
                     base.append("/*");
                 }
             }
 
             base.append(typeQuery);
         } else {
             base.append("//*").append(typeQuery);
         }
         
         StringBuilder artifactQuery = new StringBuilder();
 
         for (Restriction r : query.getRestrictions()) {
             if (r instanceof OpRestriction) {
                 if (!handleOperator((OpRestriction) r, query, artifactQuery, true, searchEntries, searchVersions)) {
                     return null;
                 }
             } else if (r instanceof FunctionCall) {
                 if (!handleFunction((FunctionCall) r, query, functions, artifactQuery, searchEntries, searchVersions)) {
                     return null;
                 }
             }
         }
         
         if (artifactQuery.length() > 0) artifactQuery.append("]");
 
 //        // Search the latest if we're searching for artifacts, otherwise
 //        // search all versions
 //        if (version) {
 //            avQuery.insert(0, "[@enabled='true']");
 //        }
         
         base.append(artifactQuery);
         
         return base.toString();
     }
 
     private boolean handleFunction(FunctionCall r, 
                                    org.mule.galaxy.query.Query query, 
                                    Map<FunctionCall, AbstractFunction> functions, 
                                    StringBuilder qstr,
                                    boolean typeEntries,
                                    boolean typeVersions) throws QueryException {
         AbstractFunction fn = functionRegistry.getFunction(r.getModule(), r.getName());
         
         functions.put(r, fn);
         
         // Narrow down query if possible
         List<OpRestriction> restrictions = fn.getRestrictions(r.getArguments());
         
         if (restrictions != null && restrictions.size() > 0) {
             for (OpRestriction opR : restrictions) {
                 if (!handleOperator(opR, query, qstr, true, typeEntries, typeVersions)) return false;
             }
         }
         return true;
     }
 
     private boolean handleOperator(OpRestriction r, 
                                    org.mule.galaxy.query.Query query, 
                                    StringBuilder queryStr,
                                    boolean prepend, 
                                    boolean typeEntries, 
                                    boolean typeVersions)
         throws QueryException {
         
         boolean not = false;
         Operator operator = r.getOperator();
 
         if (prepend) {
             if (queryStr.length() == 0) {
                 queryStr.append("[");
             } else {
                 queryStr.append(" and ");
             }
         }
         
         // Do special stuff if this is an OR/AND operator
         if (operator.equals(Operator.OR)) {
             return join(r, query, queryStr, "or", typeEntries, typeVersions);
         } else if (operator.equals(Operator.AND)) {
             return join(r, query, queryStr, "and", typeEntries, typeVersions);
         }
         
         String property = (String) r.getLeft();
         QueryBuilder builder = getQueryBuilder(property);
         
         // Do special stuff if this is a NOT operator
         if (operator.equals(Operator.NOT)) {
             not = true;
             r = (OpRestriction) r.getRight();
             operator = r.getOperator();
             property = r.getLeft().toString();
         }
 
         boolean searchChild = false;
         boolean searchParent = false;
         
         if ((builder.appliesTo(Entry.class) || builder.appliesTo(Artifact.class))) {
             if (typeVersions && !typeEntries) {
                 searchParent = true;
                 queryStr.append("(");
             }
         } 
         
         if (builder.appliesTo(EntryVersion.class) || builder.appliesTo(ArtifactVersion.class)) {
             if (typeEntries && !typeVersions) {
                 searchChild = true;
                 queryStr.append("(");
             }
         } 
         
         if (builder.build(queryStr, property, "", r.getRight(), not, operator)) {
             if (searchChild || searchParent) {
                 if (not) {
                     queryStr.append(" and ");
                 } else {
                     queryStr.append(" or ");
                 }
                 String predicate = searchChild ? "*/" : "../";
                 if (builder.build(queryStr, property, predicate, r.getRight(), not, operator)) {
                     queryStr.append(")");
                     return true;
                 }
             } else {
                 return true;
             }
         }
         return false;
     }
 
     private boolean join(OpRestriction r, 
                          org.mule.galaxy.query.Query query, 
                          StringBuilder queryStr, 
                          String opName,
                          boolean typeEntries,
                          boolean typeVersions) throws QueryException {
         Restriction r1 = (Restriction) r.getLeft();
         Restriction r2 = (Restriction) r.getRight();
         queryStr.append("(");
         if (!handleOperator((OpRestriction) r1, query, queryStr, false, typeEntries, typeVersions)) {
             return false;
         }
         queryStr.append(" ")
              .append(opName)
              .append(" ");
         
         if (!handleOperator((OpRestriction) r2, query, queryStr, false, typeEntries, typeVersions)) {
             return false;
         }
         queryStr.append(")");
         
         return true;
     }
 
     private QueryBuilder getQueryBuilder(String property) throws QueryException {
         for (QueryBuilder qb : getQueryBuilders()) {
              if (qb.getProperties().contains(property)) {
                  return qb;
              }
         }
         
         return simpleQueryBuilder;
     }
 
     public Extension getExtension(String id) {
         for (Extension e : getExtensions()) {
             if (e.getId().equals(id)) {
                 return e;
             }
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public synchronized List<Extension> getExtensions() {
         if (extensions == null) {
              Map beansOfType = context.getBeansOfType(Extension.class);
              
              extensions = new ArrayList<Extension>();
              extensions.addAll(beansOfType.values());
         }
         return extensions;
     }
 
     @SuppressWarnings("unchecked")
     protected synchronized List<QueryBuilder> getQueryBuilders() {
         if (queryBuilders == null) {
              Map beansOfType = context.getBeansOfType(QueryBuilder.class);
              
              queryBuilders = new ArrayList<QueryBuilder>();
              queryBuilders.addAll(beansOfType.values());
         }
         return queryBuilders;
     }
 
     public Map<String, String> getQueryProperties() {
         HashMap<String, String> props = new HashMap<String, String>();
         
         for (PropertyDescriptor pd : typeManager.getPropertyDescriptors(true)) {
             Extension ext = pd.getExtension();
             
             if (ext != null) {
                 Map<String, String> p2 = ext.getQueryProperties(pd);
                 
                 if (p2 != null) {
                     props.putAll(p2);
                 }
             } else {
                 props.put(pd.getProperty(), pd.getDescription());
             }
         }
         
         return props;
     }
 
     public void initialize() throws RepositoryException {
         final Session session = getSessionFactory().getSession();
         Node root = session.getRootNode();
         Node workspaces = JcrUtil.getOrCreate(root, "workspaces", "galaxy:noSiblings");
         
         workspacesId = workspaces.getUUID();
         
         session.save();
         session.logout();
     }
     
     public void setExtensions(List<Extension> extensions) {
         this.extensions = extensions;
     }
     public void setAccessControlManager(AccessControlManager accessControlManager) {
         this.accessControlManager = accessControlManager;
     }
 
     public void setFunctionRegistry(FunctionRegistry functionRegistry) {
         this.functionRegistry = functionRegistry;
     }
     
     public void setContentService(ContentService contentService) {
         this.contentService = contentService;
     }
 
     public void setLifecycleManager(LifecycleManager lifecycleManager) {
         this.lifecycleManager = lifecycleManager;
     }
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
     public void setPolicyManager(PolicyManager policyManager) {
         this.policyManager = policyManager;
     }
 
     public LifecycleManager getLifecycleManager() {
         return lifecycleManager;
     }
 
     public PolicyManager getPolicyManager() {
         return policyManager;
     }
 
     public UserManager getUserManager() {
         return userManager;
     }
     
     public void setLocalWorkspaceManager(JcrWorkspaceManager localWorkspaceManager) {
         this.localWorkspaceManager = localWorkspaceManager;
         localWorkspaceManager.setRegistry(this);
         idToWorkspaceManager.put(localWorkspaceManager.getId(), localWorkspaceManager);
     }
 
     public EventManager getEventManager() {
         return eventManager;
     }
 
     public void setEventManager(final EventManager eventManager) {
         this.eventManager = eventManager;
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.context = applicationContext;
     }
 
     public void setTypeManager(TypeManager typeManager) {
         this.typeManager = typeManager;
     }
     
 }
