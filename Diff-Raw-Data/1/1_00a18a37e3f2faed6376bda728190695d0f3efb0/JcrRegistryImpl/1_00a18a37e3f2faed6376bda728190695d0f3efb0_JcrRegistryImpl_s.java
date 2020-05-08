 package org.mule.galaxy.impl.jcr;
 

 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
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
 import org.mule.galaxy.AttachedItem;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NewItemResult;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.event.EventManager;
 import org.mule.galaxy.event.ItemMovedEvent;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.impl.jcr.query.QueryBuilder;
 import org.mule.galaxy.impl.jcr.query.SimpleQueryBuilder;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.PolicyException;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.query.AbstractFunction;
 import org.mule.galaxy.query.FunctionCall;
 import org.mule.galaxy.query.FunctionRegistry;
 import org.mule.galaxy.query.JcrRestriction;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.OpRestriction.Operator;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.security.AccessControlManager;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.type.Type;
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
     private static final String DEFAULT_ORDER = "name";
 
     private final Log log = LogFactory.getLog(getClass());
 
     private String id;
     
     private String workspacesId;
     
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
     }
 
     public String getUUID() {
         return id;
     }
 
     public Collection<Item> getItems() throws AccessException, RegistryException {
         return localWorkspaceManager.getWorkspaces();
     }
 
     public NewItemResult newItem(final String name, Type type) 
         throws RegistryException, AccessException, DuplicateItemException, PolicyException, PropertyException {
         return newItem(name, type, null);
     }
 
     public NewItemResult newItem(String name, Type type, Map<String,Object> initialProperties)
         throws DuplicateItemException, RegistryException, PolicyException, AccessException, PropertyException {
         return localWorkspaceManager.newItem(null, name, type, initialProperties);
     }
     
     public Collection<WorkspaceManager> getWorkspaceManagers() {
         return idToWorkspaceManager.values();
     }
 
     @SuppressWarnings("unchecked")
     public Collection<AttachedItem> getAttachedItems() {
         return (Collection<AttachedItem>) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 
                 QueryResult result = qm.createQuery("//element(*, galaxy:attachedItem)", Query.XPATH).execute();
                 
                 List<AttachedItem> workspaces = new ArrayList<AttachedItem>();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     workspaces.add(new JcrAttachedItem(node, localWorkspaceManager));
                 }
                 
                 return workspaces;
             }
             
         });
     }
 
     public AttachedItem attachItem(final Item parent, 
                                              final String name,
                                              final String factory,
                                              final Map<String, String> configuration) throws RegistryException {
         
         if (parent != null && !(parent instanceof JcrItem)) {
             throw new RegistryException(new Message("LOCAL_ATTACH_ONLY", BundleUtils.getBundle(this.getClass())));
         }
         
         return (AttachedItem) execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node parentNode;
                 if (parent != null) {
                     JcrItem w = (JcrItem) parent;
                     parentNode = w.getNode();
                 } else {
                     parentNode = getNodeByUUID(workspacesId);
                 }
                 
                 Node attachedNode = parentNode.addNode(name, JcrWorkspaceManagerImpl.ATTACHED_ITEM_NODE_TYPE);
                 attachedNode.addMixin("mix:referenceable");
                 Calendar now = Calendar.getInstance();
                 now.setTime(new Date());
                 attachedNode.setProperty(JcrItem.CREATED, now);
                 attachedNode.setProperty(JcrItem.UPDATED, now);
                 attachedNode.setProperty(JcrItem.NAME, name);
                 attachedNode.setProperty(JcrAttachedItem.WORKSPACE_MANAGER_FACTORY, factory);
 
                 try {
                     
                     JcrAttachedItem attached = new JcrAttachedItem(attachedNode, localWorkspaceManager);
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
 
     public WorkspaceManager getWorkspaceManager(AttachedItem w) throws RegistryException {
         WorkspaceManager wm = idToWorkspaceManager.get(trimWorkspaceManagerId(w.getId()));
         
         if (wm == null) {
             wm = createWorkspaceManager(w);
         }
         
         return wm;
     }
 
     private synchronized WorkspaceManager createWorkspaceManager(AttachedItem w) throws RegistryException {
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
 
     public void save(Item i) throws AccessException, RegistryException, PolicyException, PropertyException {
         getWorkspaceManagerByItemId(i.getId()).save(i);
     }
 
     public void save(final Item w, final String _parentId) 
         throws RegistryException, NotFoundException, AccessException, DuplicateItemException {
         accessControlManager.assertAccess(Permission.MODIFY_ITEM, w);
 
         executeWithNotFoundDuplicate(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 JcrItem jw = (JcrItem) w;
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
                     
                     JcrItem toWkspc = new JcrItem(parentNode, localWorkspaceManager);
                     try {
                         accessControlManager.assertAccess(Permission.MODIFY_ITEM, toWkspc);
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
         if (location.length() == 0) return null;
 //        String query = null;
 //        int idx = location.indexOf('?');
 //        if (idx != -1) {
 //            query = location.substring(idx+1);
 //            location = location.substring(0, idx);
 //        }
         String[] paths = location.split("/");
         
         while (!(item instanceof Item)) {
             item = item.getParent();
         }
         
         Item w = (Item) item;
 
         try {
             for (int i = 0; i < paths.length - 1 && w != null; i++) {
                 String p = paths[i];
                 
                 // TODO: escaping?
                 if (p.equals("..")) {
                     w = ((Item)w.getParent());
                 } else if (!p.equals(".")) {
                     Item child = w.getItem(p);
                     
                     if (child instanceof Item) {
                         w = (Item) child;
                     } else {
                         return null;
                     }
                 }
             }
             
             if (w == null) {
             return null;
             }
             
             Item result = w.getItem(paths[paths.length-1]);
             
             /**
              * Special support for versioned items inside the registry. If
              * this is a versioned item, then retrieve the default or latest
              * version. 
              */ 
             if (result.getType().inheritsFrom(TypeManager.VERSIONED)) {
                 // TODO add support for ?version=foo
 //                
 //                Object def = result.getProperty(TypeManager.DEFAULT_VERSION);
 //                if (def == null || !(def instanceof Item)) {
 //                    def = result.getLatestItem();
 //                }
 //                result = (Item) def;   
             }
             
             return result;
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
         WorkspaceManager wm;
         try {
             wm = getWorkspaceManagerByItemId(id);
         } catch (IllegalStateException e) {
             throw new NotFoundException(id);
         }
         
         return wm.getItemById(id);
     }
         
     public Item getItemByPath(String path) throws RegistryException, NotFoundException, AccessException {
         return localWorkspaceManager.getItemByPath(path);
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
 
     private Object executeMove(JcrCallback jcrCallback) 
         throws RegistryException, AccessException, NotFoundException, PolicyException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
             } else if (cause instanceof NotFoundException) {
                 throw (NotFoundException) cause;
             } else if (cause instanceof AccessException) {
                 throw (AccessException) cause;
             } else if (cause instanceof PolicyException) {
                 throw (PolicyException) cause;
             } else {
                 throw e;
             }
         }
     }
     
     public void move(final Item item, final String newWorkspacePath, final String newName) throws RegistryException, AccessException, NotFoundException, PolicyException, PropertyException {
         boolean wasRenamed = false;
         boolean wasMoved = false;
         final String oldPath = item.getPath();
 
         try {
             // handle artifact renaming
             accessControlManager.assertAccess(Permission.MODIFY_ITEM, item);
 
             if (!item.getName().equals(newName)) {
                 // save only if name changed
                 item.setName(newName);
                 save(item);
                 wasRenamed = true;
             }
 
             // handle workspace move
             final Item parent;
             if (newWorkspacePath != null && !newWorkspacePath.equals("") && !newWorkspacePath.equals("/")) {
                 parent = (Item) getItemByPath(newWorkspacePath);
                 accessControlManager.assertAccess(Permission.MODIFY_ITEM, parent);
             } else {
                 parent = null;
                 accessControlManager.assertAccess(Permission.MODIFY_ITEM);
             }
 
             // only if workspace changed
             if ((item.getParent() == null && parent != null) || 
                 (item.getParent() != null && parent == null) || 
                 (item.getParent() != null && parent != null && !item.getParent().getId().equals(parent.getId()))) {
 
                 executeMove(new JcrCallback() {
                     public Object doInJcr(Session session) throws IOException, RepositoryException {
 
                         Node childNode = ((JcrItem) item).getNode();
                         Node parentNode;
                         if (parent == null) {
                             parentNode = getWorkspacesNode();
                         } else {
                             parentNode = ((JcrItem) parent).getNode();
                         }
 
                         JcrWorkspaceManagerImpl.approveChild(parent, item);
                         
                         final String newPath = parentNode.getPath() + "/" + childNode.getName();
                         session.move(childNode.getPath(), newPath);
 
                         session.save();
                         ((JcrItem) item).setParent((JcrItem)parent);
                         return null;
                     }
                 });
 
                 wasMoved = true;
             }
         } finally {
             // fire an event only if there was an actual action taken, and guarantee it will be fired
             if (wasRenamed || wasMoved) {
                 ItemMovedEvent event = new ItemMovedEvent(item, oldPath);
                 event.setUser(SecurityUtils.getCurrentUser());
                 eventManager.fireEvent(event);
             }
         }
     }
 
     
     public SearchResults suggest(final String p, 
                                  final boolean recursive, 
                                  final int maxResults, 
                                  final String excludePath, 
                                  final String... typeNames)
         throws RegistryException, QueryException {
         return (SearchResults) executeWithQueryException(new JcrCallback() {
             
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 
                 String path = p;
                 
                 StringBuilder qstr = new StringBuilder();
                 qstr.append("/");
                 
                 boolean startsWithExact = path.startsWith("/");
                 if (startsWithExact) path = path.substring(1);
                 
                 boolean endsWithExact = path.endsWith("/");
                 if (endsWithExact) path = path.substring(0, path.length());
                 
                 String[] paths = path.split("/");
                 
                 if (paths.length == 1 && "".equals(paths[0])) {
                     qstr.append("/*");
                 } else {
                     for (int i = 0; i < paths.length; i++) {
                         if (paths[i].equals("")) {
                             startsWithExact = true;
                             continue;
                         }
                          
                         String value = paths[i].toLowerCase();
                         value = JcrUtil.escape(value);
                         if (i == 0 && startsWithExact) {
                             qstr.append("/*[jcr:like(fn:lower-case(@name), '").append(value).append("%')]");
                         } else if (i == (paths.length - 1) && endsWithExact) {
                             qstr.append("/*[jcr:like(fn:lower-case(@name), '%").append(value).append("')]/*");
                         } else {
                             qstr.append("/*[jcr:like(fn:lower-case(@name), '%").append(value).append("%')]");
                         }
                     }
                 }
                 
                 if (typeNames != null) {
                     List<Type> types = new ArrayList<Type>();
                     for (String typeName : typeNames) {
                         try {
                             types.add(typeManager.getTypeByName(typeName));
                         } catch (NotFoundException e) {
                             // Ignore 
                         }
                     }
                     
                     StringBuilder typeQ = new StringBuilder();
                     for (Type type : types) {
                         if (typeQ.length() == 0) {
                             typeQ.append("[");
                         } else {
                             typeQ.append(" or ");
                         }
                         
                         typeQ.append("type='").append(type.getId()).append("'");
                     }
                     
                     if (typeQ.length() > 0) {
                         qstr.append(typeQ).append("]");
                     }
                 }
                 
                 qstr.append("[@jcr:primaryType='galaxy:item' and not(@internal = 'true')]");
 
                 QueryResult result = qm.createQuery(qstr.toString(), Query.XPATH).execute();
                 
                 Set<Item> results = new HashSet<Item>();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
 
                     try {
                         addNodes(node, results, recursive);
                     } catch (RegistryException e) {
                         throw new RuntimeException(e);
                     }
                     
                     if (results.size() >= maxResults) {
                         break;
                     }
                 }
                 
                 return new SearchResults(results.size(), results);
             }
 
             private void addNodes(Node node, 
                                   Set<Item> results,
                                   boolean recursive) throws RepositoryException, ItemNotFoundException,
                 AccessDeniedException, RegistryException {
 
                 try {
                     Item item = new JcrItem(node, localWorkspaceManager);
                     accessControlManager.assertAccess(Permission.READ_ITEM, item);
                     if (excludePath != null && item.getPath().startsWith(excludePath)) {
                         return;
                     }
                     results.add(item);
                 } catch (AccessException e) {
                 }
                 
                 if (results.size() == maxResults) {
                     return;
                 }
                 
                 if (recursive) {
                     for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
                         addNodes(nodes.nextNode(), results, recursive);
                         
                         if (results.size() == maxResults) {
                             return;
                         }
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
         q.orderBy("name");
         
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
                     if (nodes.getSize() != -1 && nodes.getSize() <= query.getStart()) {
                         return new SearchResults(0, items);
                     } else if (query.getStart() > 0) {
                         try {
                             nodes.skip(query.getStart());
                         } catch (NoSuchElementException e) {
                             return new SearchResults(0, items);
                         }
                     }
                 }
                 
                 int max = query.getMaxResults();
                 int count = 0;
                 int filteredCount = 0;
                 while (nodes.hasNext()) {
                     Node node = nodes.nextNode();
                     
                     boolean filtered = false;
                     try {
                         Item item = new JcrItem(node, localWorkspaceManager);
                         accessControlManager.assertAccess(Permission.READ_ITEM, item);
                         
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
                     }
                 }                                                   
 
                 long total = nodes.getSize();
                 if (total == -1) {
                     total = count;
                 } else {
                     total -= filteredCount;
                 }
                 
                 return new SearchResults(total, items);
             }
 
         });
     }
 
     protected String createQueryString(final org.mule.galaxy.query.Query query, 
                                        Map<FunctionCall, AbstractFunction> functions) throws QueryException {
         StringBuilder base = new StringBuilder();
         
         // Search by workspace id, workspace path, or any workspace
         if (query.getFromId() != null) {
             base.append("//*[@jcr:uuid='")
                 .append(trimWorkspaceManagerId(query.getFromId()))
                 .append("']");
 
             if (query.isFromRecursive()) {
                 base.append("//*");
             } else {
                 base.append("/*");
             }
             
         } else if (query.getFromPath() != null && !"".equals(query.getFromPath())) {
             String path = query.getFromPath();
 
             if (path.startsWith("/")) {
                 path = path.substring(1);
             }
             
             if (path.endsWith("/")) {
                 path = path.substring(0, path.length() - 1);
             }
             
             base.append("//")
                 .append(ISO9075.encodePath(path))
                 .append("");
             
             if (query.isFromRecursive()) {
                 base.append("//*");
             } else {
                 base.append("/*");
             }
         } else {
             base.append("//*");
         }
         
         StringBuilder itemQuery = new StringBuilder();
 
         for (Restriction r : query.getRestrictions()) {
             if (r instanceof OpRestriction) {
                 if (!handleOperator((OpRestriction) r, query, itemQuery, true)) {
                     return null;
                 }
             } else if (r instanceof FunctionCall) {
                 if (!handleFunction((FunctionCall) r, query, functions, itemQuery)) {
                     return null;
                 }
             } else if (r instanceof JcrRestriction) {
                 if (itemQuery.length() == 0) {
                     itemQuery.append("[");
                 } else {
                     itemQuery.append(" and ");
                 }
                 itemQuery.append(((JcrRestriction) r).getPredicateRestriction());
             }
         }
         
         if (itemQuery.length() > 0) itemQuery.append("]");
 
         
         base.append(itemQuery);
         base.append("[@jcr:primaryType='galaxy:item']");
 
         String orderBy = query.getOrderBy();
         if(orderBy == null) {
             orderBy = DEFAULT_ORDER;
         }
         base.append(" order by @" + orderBy);
 
         return base.toString();
     }
 
     private boolean handleFunction(FunctionCall r, 
                                    org.mule.galaxy.query.Query query, 
                                    Map<FunctionCall, 
                                    AbstractFunction> functions, 
                                    StringBuilder qstr) throws QueryException {
         AbstractFunction fn = functionRegistry.getFunction(r.getModule(), r.getName());
         
         functions.put(r, fn);
         
         // Narrow down query if possible
         List<OpRestriction> restrictions = fn.getRestrictions(r.getArguments());
         
         if (restrictions != null && restrictions.size() > 0) {
             for (OpRestriction opR : restrictions) {
                 if (!handleOperator(opR, query, qstr, true)) return false;
             }
         }
         return true;
     }
 
     private boolean handleOperator(OpRestriction r, 
                                    org.mule.galaxy.query.Query query, 
                                    StringBuilder queryStr,
                                    boolean prepend)
         throws QueryException {
         if (prepend) {
             if (queryStr.length() == 0) {
                 queryStr.append("[");
             } else {
                 queryStr.append(" and ");
             }
         }
         
         Operator operator = r.getOperator();
 
         // Do special stuff if this is an OR/AND operator
         if (operator.equals(Operator.OR)) {
             return join(r, query, queryStr, "or");
         } else if (operator.equals(Operator.AND)) {
             return join(r, query, queryStr, "and");
         }
         
         String property = (String) r.getLeft();
         
         // Do special stuff if this is a NOT operator
         boolean not = false;
         
         if (operator.equals(Operator.NOT)) {
             not = true;
             r = (OpRestriction) r.getRight();
             operator = r.getOperator();
             property = r.getLeft().toString();
         }
         
         String prefix = "";
         String[] split = property.split(":");
         property = split[split.length-1];
         QueryBuilder builder = getQueryBuilder(property);
         for (int i = 0; i < split.length - 1; i++) {
             if ("child".equals(split[0])) {
                 prefix += "*/";
             } else if ("parent".equals(split[0])) {
                 prefix += "../";
             } else if ("child*".equals(split[0])) {
                 prefix += "*//";
             }
         }
 
         return builder.build(queryStr, property, prefix, r.getRight(), not, operator);
     }
 
     private boolean join(OpRestriction r, 
                          org.mule.galaxy.query.Query query, 
                          StringBuilder queryStr, 
                          String opName) throws QueryException {
         Restriction r1 = (Restriction) r.getLeft();
         Restriction r2 = (Restriction) r.getRight();
         StringBuilder subClause = new StringBuilder();
         subClause.append("(");
         StringBuilder subClause1 = new StringBuilder();
         if (!handleOperator((OpRestriction) r1, query, subClause1, false)) {
             if (!"or".equals(opName)) {
                 return false;
             }
         }
         
         subClause.append(subClause1)
              .append(" ")
              .append(opName)
              .append(" ");
 
         StringBuilder subClause2 = new StringBuilder();
         if (!handleOperator((OpRestriction) r2, query, subClause2, false)) {
             if ("or".equals(opName)) {
                 // only left side matched
                 queryStr.append(subClause1);
                 return true;
             } else {
                 return false;
             }
         } 
         
         // only right side matched
         if (subClause1.length() == 0) {
             queryStr.append(subClause2);
             return true;
         } else {
             subClause.append(subClause2)
                      .append(")");
             queryStr.append(subClause);
             return true;
         }
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
         
         for (PropertyDescriptor pd : typeManager.getGlobalPropertyDescriptors(true)) {
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
         id = workspacesId;
         
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
