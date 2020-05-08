 package org.mule.galaxy.impl.jcr;
 
 
 import java.io.IOException;
 import java.io.InputStream;
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
 
 import javax.activation.MimeType;
 import javax.activation.MimeTypeParseException;
 import javax.jcr.AccessDeniedException;
 import javax.jcr.ItemExistsException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.ValueFormatException;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.jcr.version.VersionException;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.jackrabbit.util.ISO9075;
 import org.mule.galaxy.ActivityManager;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.ArtifactType;
 import org.mule.galaxy.ArtifactTypeDao;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.ContentHandler;
 import org.mule.galaxy.ContentService;
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.Dependency;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyDescriptor;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Settings;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.XmlContentHandler;
 import org.mule.galaxy.ActivityManager.EventType;
 import org.mule.galaxy.impl.jcr.query.QueryBuilder;
 import org.mule.galaxy.impl.jcr.query.SimpleQueryBuilder;
 import org.mule.galaxy.index.IndexManager;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.policy.ArtifactPolicy;
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
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.util.BundleUtils;
 import org.mule.galaxy.util.DateUtil;
 import org.mule.galaxy.util.Message;
 import org.mule.galaxy.util.SecurityUtils;
 import org.springframework.dao.DataAccessException;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 public class JcrRegistryImpl extends JcrTemplate implements Registry, JcrRegistry {
 
     public static final String ARTIFACT_NODE_TYPE = "galaxy:artifact";
     public static final String ARTIFACT_VERSION_NODE_TYPE = "galaxy:artifactVersion";
     public static final String LATEST = "latest";
     private static final String REPOSITORY_LAYOUT_VERSION = "version";
 
     private final Log log = LogFactory.getLog(getClass());
 
     private Settings settings;
     
     private ContentService contentService;
 
     private FunctionRegistry functionRegistry;
     
     private LifecycleManager lifecycleManager;
     
     private PolicyManager policyManager;
     
     private UserManager userManager;
 
     private IndexManager indexManager;
     
     private Dao<PropertyDescriptor> propertyDescriptorDao;
     
     private String workspacesId;
 
     private String indexesId;
 
     private String artifactTypesId;
     
     private ActivityManager activityManager;
     
     private AccessControlManager accessControlManager;
     
     private String id;
     
     private ArtifactTypeDao artifactTypeDao;
     
     private Map<String, QueryBuilder> queryBuilders;
     
     private SimpleQueryBuilder simpleQueryBuilder = new SimpleQueryBuilder(new String[0], false);
     
     public JcrRegistryImpl() {
         super();
     }
 
     public String getUUID() {
         return id;
     }
 
     public Workspace getWorkspace(String id) throws RegistryException, AccessException {
         try {
             if (id == null) {
                 throw new NullPointerException("Workspace ID cannot be null.");
             }
             
             Node node = getNodeByUUID(id);
 
             Workspace w = buildWorkspace(node);
             
             accessControlManager.assertAccess(Permission.READ_WORKSPACE, w);
             
             return w;
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
     }
 
     private Workspace buildWorkspace(Node node) throws RepositoryException {
         return new JcrWorkspace(this, lifecycleManager, node);
     }
 
     public Workspace getWorkspaceByPath(String path) throws RegistryException, NotFoundException, AccessException {
         try {
             if (path.startsWith("/")) {
                 path = path.substring(1);
             }
             if (path.endsWith("/")) {
                 path = path.substring(0, path.length()-1);
             }
             
             Node wNode = getWorkspacesNode();
             
             try {
                 // have to have the catch because jackrabbit is lame...
                 if (!wNode.hasNode(path)) throw new NotFoundException(path);
             } catch (RepositoryException e) {
                 throw new NotFoundException(path);
             }
             
             Node node = wNode.getNode(path);
 
             if (node.getPrimaryNodeType().getName().equals("galaxy:workspace")) {
                 Workspace w = buildWorkspace(node);
                 accessControlManager.assertAccess(Permission.READ_WORKSPACE, w);
                 return w;
             }
             
             throw new NotFoundException(path);
         } catch (PathNotFoundException e) {
             throw new NotFoundException(e);
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
     }
     
     public Workspace createWorkspace(final String name) throws RegistryException, AccessException, DuplicateItemException {
         // we should throw an error, but lets be defensive for now
         final String escapedName = JcrUtil.escape(name);
         final JcrRegistryImpl registry = this;
         
         accessControlManager.assertAccess(Permission.MODIFY_WORKSPACE);
 
         return (Workspace) executeAndDewrapWithDuplicate(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node;
                 try {
                     node = getWorkspacesNode().addNode(escapedName, "galaxy:workspace");
                 } catch (javax.jcr.ItemExistsException e) {
                     throw new RuntimeException(new DuplicateItemException(name));
                 }
                 node.addMixin("mix:referenceable");
     
                 JcrWorkspace workspace = new JcrWorkspace(registry, lifecycleManager, node);
                 workspace.setName(escapedName);
                 workspace.setDefaultLifecycle(lifecycleManager.getDefaultLifecycle());
                 
                 Calendar now = DateUtil.getCalendarForNow();
                 node.setProperty(JcrWorkspace.CREATED, now);
                 node.setProperty(JcrWorkspace.UPDATED, now);
                 
                 session.save();
                 
                 activityManager.logActivity(SecurityUtils.getCurrentUser(),
                                             "Workspace " + workspace.getPath() + " was created", 
                                             EventType.INFO);
                 return workspace;
             }
         });
     }
 
     public void save(Workspace w) throws AccessException {
         accessControlManager.assertAccess(Permission.MODIFY_WORKSPACE, w);
         
         execute(new JcrCallback() {
 
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 session.save();
                 return null;
             }
             
         });
     }
 
     public void save(final Workspace w, final String parentId) 
         throws RegistryException, NotFoundException, AccessException {
         
         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, w);
         
         final JcrRegistryImpl registry = this;
         executeWithNotFound(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 
                 JcrWorkspace jw = (JcrWorkspace) w;
                 Node node = jw.getNode();
                 
                 if (parentId != null && !parentId.equals(node.getParent().getUUID())) {
                     Node parentNode = null;
                     if (parentId != null) {
                         try {
                             parentNode = getNodeByUUID(parentId);
                         } catch (DataAccessException e) {
                             throw new RuntimeException(new NotFoundException(parentId));
                         }
                     } else {
                         parentNode = node.getParent();
                     }
                     
                     // ensure the user isn't trying to move this to a child node of the workspace
                     Node checked = parentNode;
                     while (checked != null && checked.getPrimaryNodeType().getName().equals("galaxy:workspace")) {
                         if (checked.equals(node)) {
                             throw new RuntimeException(new RegistryException(new Message("MOVE_ONTO_CHILD", BundleUtils.getBundle(getClass()))));
                         }
                         
                         checked = checked.getParent();
                     }
                     
                     JcrWorkspace toWkspc = new JcrWorkspace(registry, lifecycleManager, parentNode);
                     try {
                         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, toWkspc);
                     } catch (AccessException e) {
                         throw new RuntimeException(e);
                     }
                     
                     String dest = parentNode.getPath() + "/" + w.getName();
                     session.move(node.getPath(), dest);
                 }
                 
                 session.save();
                 
                 return null;
             }
         });
     }
 
     public void deleteWorkspace(final String id) throws RegistryException, NotFoundException, AccessException {
         accessControlManager.assertAccess(Permission.DELETE_WORKSPACE);
 
         final JcrRegistryImpl registry = this;
         executeWithNotFound(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 
                 try {
                     Node node = getNodeByUUID(id);
 
                     JcrWorkspace wkspc = new JcrWorkspace(registry, lifecycleManager, node);
                     String path = wkspc.getPath();
                     
                     node.remove();
 
                     activityManager.logActivity(SecurityUtils.getCurrentUser(),
                                                 "Workspace " + path + " was deleted", 
                                                 EventType.INFO);
                     session.save();
                     
                 } catch (ItemNotFoundException e) {
                     throw new RuntimeException(new NotFoundException(id));
                 }
                 return null;
             }
         });
     }
 
     public Workspace createWorkspace(final Workspace parent, 
                                      final String name) throws DuplicateItemException, RegistryException, AccessException {
         accessControlManager.assertAccess(Permission.MODIFY_WORKSPACE, parent);
 
         // we should throw an error, but lets be defensive for now
         final String escapedName = JcrUtil.escape(name);
 
         final JcrRegistryImpl registry = this;
         
         return (Workspace) executeAndDewrapWithDuplicate(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Collection<Workspace> workspaces = parent.getWorkspaces();
                 
                 Node parentNode = ((JcrWorkspace) parent).getNode();
                 Node node = null;
                 try {
                     node = parentNode.addNode(escapedName, "galaxy:workspace");
                 } catch (ItemExistsException e) {
                     throw new RuntimeException(new DuplicateItemException(name));
                 }
                 
                 node.addMixin("mix:referenceable");
     
                 Calendar now = DateUtil.getCalendarForNow();
                 node.setProperty(JcrWorkspace.CREATED, now);
                 node.setProperty(JcrWorkspace.UPDATED, now);
                 
                 JcrWorkspace workspace = new JcrWorkspace(registry, lifecycleManager, node);
                 workspace.setName(escapedName);
                 workspace.setDefaultLifecycle(lifecycleManager.getDefaultLifecycle());
                 workspaces.add(workspace);
 
                 session.save();
                 
                 activityManager.logActivity(SecurityUtils.getCurrentUser(),
                                             "Workspace " + workspace.getPath() + " was created", 
                                             EventType.INFO);
                 
                 return workspace;
             }
         });
     }
 
     public Artifact resolve(Workspace w, String location) {
         String[] paths = location.split("/");
         
         for (int i = 0; i < paths.length - 1; i++) {
             String p = paths[i];
             
             // TODO: escaping?
             if (p.equals("..")) {
                 w = w.getParent();
             } else if (!p.equals(".")) {
                 w = w.getWorkspace(p);
             }
         }
 
         try {
             return getArtifact(w, paths[paths.length-1]);
         } catch (NotFoundException e) {
             return null;
         }
     }
     
     public Node getWorkspacesNode() {
         return getNodeByUUID(workspacesId);
     }
 
     public Node getIndexNode() {
         return getNodeByUUID(indexesId);
     }
     
     public Node getArtifactTypesNode() {
         return getNodeByUUID(artifactTypesId);
     }
     
     public Collection<Workspace> getWorkspaces() throws RegistryException, AccessException {
         try {
             List<Workspace> workspaceCol = new ArrayList<Workspace>();
             for (NodeIterator itr = getWorkspacesNode().getNodes(); itr.hasNext();) {
                 Node n = itr.nextNode();
 
                 if (!n.getName().equals("jcr:system")) {
 
                     JcrWorkspace wkspc = new JcrWorkspace(this, lifecycleManager, n);
                     accessControlManager.assertAccess(Permission.READ_WORKSPACE, wkspc);
                     
                     workspaceCol.add(wkspc);
                 }
                 
                 Collections.sort(workspaceCol, new WorkspaceComparator());
             }
             return workspaceCol;
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
     }
 
     public Collection<Artifact> getArtifacts(Workspace w) throws RegistryException {
         JcrWorkspace jw = (JcrWorkspace)w;
 
         Node node = jw.getNode();
 
         ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
         try {
             for (NodeIterator itr = node.getNodes(); itr.hasNext();) {
                 JcrArtifact artifact = new JcrArtifact(jw, itr.nextNode(), this);
                 
                 try {
                     accessControlManager.assertAccess(Permission.READ_ARTIFACT, artifact);
                     
                     artifact.setContentHandler(contentService.getContentHandler(artifact.getContentType()));
                     artifacts.add(artifact);
                 } catch (AccessException e) {
                     // don't list artifacts which the user doesn't have perms for
                 }
             }
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
 
         return artifacts;
     }
 
     public Artifact getArtifact(final String id) throws NotFoundException, RegistryException, AccessException {
         if (id == null) {
             throw new NotFoundException("No id specified.");
         }
         return (Artifact) executeWithNotFound(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(id);
                 Artifact a = buildArtifact(node);
                 
                 try {
                     accessControlManager.assertAccess(Permission.READ_ARTIFACT, a);
                 } catch (AccessException e) {
                     throw new RuntimeException(e);
                 }
 
                 return a;
             }
         });
     }
 
     private Artifact buildArtifact(Node node)
         throws ItemNotFoundException, AccessDeniedException, RepositoryException {
         Node wNode = node.getParent();
         JcrArtifact artifact = new JcrArtifact(new JcrWorkspace(this, lifecycleManager, wNode), 
                                                node, this);
 
         setupContentHandler(artifact);
 
         return artifact;
     }
 
     public Item getRegistryItem(final String id) throws NotFoundException, RegistryException, AccessException {
         return (Item) executeWithNotFound(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(id);
                 
                 try {
                     if (node.getPrimaryNodeType().getName().equals("galaxy:artifact")) {
                         Artifact a = buildArtifact(node);
     
                         accessControlManager.assertAccess(Permission.READ_ARTIFACT, a);
                         
                         return a;
                     } else {
                          Workspace wkspc = buildWorkspace(node);
                          
                          accessControlManager.assertAccess(Permission.READ_WORKSPACE, wkspc);
                          
                          return wkspc;
                     }
                 } catch (AccessException e){
                     throw new RuntimeException(e);
                 }
             }
         });
     }
     public ArtifactVersion getArtifactVersion(final String id) throws NotFoundException, RegistryException, AccessException {
         final JcrRegistryImpl registry = this;
         return (ArtifactVersion) executeWithNotFound(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(id);
                 Node aNode = node.getParent();
                 Node wNode = aNode.getParent();
                 
                 JcrArtifact artifact = new JcrArtifact(new JcrWorkspace(registry, lifecycleManager, wNode), 
                                                        aNode, registry);
 
                 try {
                     accessControlManager.assertAccess(Permission.READ_ARTIFACT, artifact);
                 } catch (AccessException e) {
                     throw new RuntimeException(e);
                 }
                 
                 setupContentHandler(artifact);
 
                 ArtifactVersion av = artifact.getVersion(node.getName());
                 if (av == null) {
                     throw new RuntimeException(new NotFoundException(id));
                 }
                 return av;
             }
         });
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
     
     public Artifact getArtifact(final Workspace w, final String name) throws NotFoundException {
         final JcrRegistryImpl registry = this;
         Artifact a = (Artifact) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 StringBuilder sb = new StringBuilder();
                 sb.append("//element(*, galaxy:artifact)[@name='")
                   .append(name)
                   .append("']");
                 
                 Query query = qm.createQuery(sb.toString(), Query.XPATH);
                 
                 QueryResult result = query.execute();
                 NodeIterator nodes = result.getNodes();
                 
                 if (nodes.hasNext()) {
                     Node node = nodes.nextNode();
                     JcrArtifact artifact = new JcrArtifact(w, node, registry);
                     
                     try {
                         accessControlManager.assertAccess(Permission.READ_ARTIFACT, artifact);
                     } catch (AccessException e) {
                         throw new RuntimeException(e);
                     }
                     
                     artifact.setContentHandler(contentService.getContentHandler(artifact.getContentType()));
 
                     return artifact;
                 }
                 return null;
             }
         });
         
         if (a == null) {
             throw new NotFoundException(name);
         }
         
         return a;
     }
 
     public ArtifactResult createArtifact(Workspace workspace, Object data, String versionLabel, User user) 
         throws RegistryException, ArtifactPolicyException, MimeTypeParseException, DuplicateItemException, AccessException {
         accessControlManager.assertAccess(Permission.READ_ARTIFACT);
 
         ContentHandler ch = contentService.getContentHandler(data.getClass());
         
         if (ch == null) {
             throw new RegistryException(new Message("UNKNOWN_TYPE", BundleUtils.getBundle(getClass()), data.getClass()));
         }
         
         MimeType ct = ch.getContentType(data);
         String name = ch.getName(data);
         
         return createArtifact(workspace, null, data, name, versionLabel, ct, user);
     }
 
     public ArtifactResult createArtifact(final Workspace workspace, 
                                          final InputStream is, 
                                          final Object data,
                                          final String name, 
                                          final String versionLabel,
                                          final MimeType contentType,
                                          final User user)
         throws RegistryException, ArtifactPolicyException, DuplicateItemException {
         
         if (user == null) {
             throw new NullPointerException("User cannot be null.");
         }
         if (name == null) {
             throw new NullPointerException("Artifact name cannot be null.");
         }
         
         final JcrRegistryImpl registry = this;
         return (ArtifactResult) executeAndDewrap(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node workspaceNode = ((JcrWorkspace)workspace).getNode();
                 Node artifactNode;
                 try {
                     artifactNode = workspaceNode.addNode(ISO9075.encode(name), ARTIFACT_NODE_TYPE);
                 } catch (javax.jcr.ItemExistsException e) {
                     throw new RuntimeException(new DuplicateItemException(name));
                 }
                 
                 artifactNode.addMixin("mix:referenceable");
                 Node versionNode = artifactNode.addNode(versionLabel, ARTIFACT_VERSION_NODE_TYPE);
                 versionNode.addMixin("mix:referenceable");
                 JcrArtifact artifact = new JcrArtifact(workspace, artifactNode, registry);
                 artifact.setName(name);
                 
                 ContentHandler ch = initializeContentHandler(artifact, name, contentType);
                 
                 // set up the initial version
                 
                 Calendar now = Calendar.getInstance();
                 now.setTime(new Date());
                 versionNode.setProperty(JcrVersion.CREATED, now);
                 versionNode.setProperty(JcrVersion.LATEST, true);
                 
                 Node resNode = createVersionContentNode(versionNode, is, contentType, now);
                 
                 JcrVersion jcrVersion = new JcrVersion(artifact, versionNode, resNode);
 
                 // Store the data
                 Object loadedData = null;
                 if (data != null) {
                     jcrVersion.setData(data);
                     InputStream dataStream = ch.read(data);
                     resNode.setProperty("jcr:data", dataStream);
                     loadedData = data;
                 } else {
                     InputStream dataStream = jcrVersion.getStream();
                     jcrVersion.setData(ch.read(dataStream, workspace));
                     loadedData = jcrVersion.getData();
                 }
                 
                 if (ch instanceof XmlContentHandler) {
                     XmlContentHandler xch = (XmlContentHandler) ch;
                     artifact.setDocumentType(xch.getDocumentType(loadedData));
                     ch = contentService.getContentHandler(artifact.getDocumentType());
                 }
     
                 jcrVersion.setAuthor(user);
                 jcrVersion.setLatest(true);
                 jcrVersion.setDefault(true);
                 jcrVersion.setEnabled(true);
                 
                 try {
                     Set<Artifact> dependencies = ch.detectDependencies(loadedData, workspace);
                     jcrVersion.addDependencies(dependencies, false);
                     
                     Lifecycle lifecycle = workspace.getDefaultLifecycle();
                     jcrVersion.setPhase(lifecycle.getInitialPhase());                    
                     
                     List<ArtifactVersion> versions = new ArrayList<ArtifactVersion>();
                     versions.add(jcrVersion);
                     artifact.setVersions(versions);
 
                     if (log.isDebugEnabled())
                     {
                         log.debug("Created artifact " + artifact.getId());
                     }
                     return approve(session, artifact, null, jcrVersion, user);
                 } catch (RegistryException e) {
                     // gets unwrapped by executeAndDewrap
                     throw new RuntimeException(e);
                 }
             }
 
         });
     }
 
     protected Node createVersionContentNode(Node versionNode, final InputStream is,
                                             final MimeType contentType, Calendar now)
         throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException,
         VersionException, ConstraintViolationException, RepositoryException, ValueFormatException {
         // these are required since we inherit from nt:file
         Node resNode = versionNode.addNode("jcr:content", "nt:resource");
         resNode.setProperty("jcr:mimeType", contentType.toString());
 //        resNode.setProperty("jcr:encoding", "");
         resNode.setProperty("jcr:lastModified", now);
 
         if (is != null) {
             resNode.setProperty(JcrVersion.JCR_DATA, is);
         }
         return resNode;
     }
 
     protected ContentHandler initializeContentHandler(JcrArtifact artifact, 
                                                       final String name,
                                                       MimeType contentType) {
         ContentHandler ch = null;
         if ("application/octet-stream".equals(contentType.toString())) {
             String ext = getExtension(name);
             ArtifactType type = artifactTypeDao.getArtifactType(ext);
             
             try {
                 if (type == null && "xml".equals(ext)) {
                     contentType = new MimeType("application/xml");
                 } else if (type != null) {
                     contentType = new MimeType(type.getContentType());
                     
                     if (type.getDocumentTypes().size() > 0) {
                         for (QName q : type.getDocumentTypes()) {
                             ch = contentService.getContentHandler(q);
                             if (ch != null) {
                                 break;
                             }
                         }
                     }
                 }
             } catch (MimeTypeParseException e) {
                 throw new RuntimeException(e);
             }
         } 
         
         if (ch == null) {
             ch = contentService.getContentHandler(contentType);
         }
         
         artifact.setContentType(contentType);
         artifact.setContentHandler(ch);
         
         return ch;
     }
 
     private String getExtension(String name) {
         int idx = name.lastIndexOf('.');
         if (idx > 0) {
             return name.substring(idx+1);
         }
         
         return "";
     }
 
     private Object executeAndDewrap(JcrCallback jcrCallback) 
         throws RegistryException, ArtifactPolicyException, DuplicateItemException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
             } else if (cause instanceof DuplicateItemException) {
                 throw (DuplicateItemException) cause;
             } else if (cause instanceof ArtifactPolicyException) {
                 throw (ArtifactPolicyException) cause;
             } else {
                 throw e;
             }
         }
     }
     private Object executeAndDewrapWithDuplicate(JcrCallback jcrCallback) 
         throws RegistryException, DuplicateItemException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
             } else if (cause instanceof DuplicateItemException) {
                 throw (DuplicateItemException) cause;
             } else {
                 throw e;
             }
         }
     }
 
     private Object executeWithNotFound(JcrCallback jcrCallback) 
         throws RegistryException, NotFoundException, AccessException {
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
             } else {
                 throw e;
             }
         }
     }
 
     private Object executeWithPolicy(JcrCallback jcrCallback) 
         throws RegistryException, ArtifactPolicyException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
             } else if (cause instanceof ArtifactPolicyException) {
                 throw (ArtifactPolicyException) cause;
             } else {
                 throw e;
             }
         }
     }
     private Object executeWithRegistryException(JcrCallback jcrCallback) 
         throws RegistryException {
         try {
             return execute(jcrCallback);
         } catch (RuntimeException e) {
             Throwable cause = e.getCause();
             if (cause instanceof RegistryException) {
                 throw (RegistryException) cause;
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
 
     private ArtifactResult approve(Session session, 
                                    Artifact artifact, 
                                    JcrVersion previous, 
                                    JcrVersion next,
                                    User user)
         throws RegistryException, RepositoryException {
         List<ApprovalMessage> approvals = approve(previous, next);
 
         // save this so the indexer will work
         session.save();
         
         // index in a separate thread
         indexManager.index(next);
         
         // save the "we're indexing" flag
         session.save();
         
         if (previous == null) {
             activityManager.logActivity(user, "Artifact " + artifact.getName() + " was created in workspace "
                                               + artifact.getParent().getPath() + ".", EventType.INFO);
         } else {
             activityManager.logActivity(user, "Version " + next.getVersionLabel() 
                                         + " of artifact " + artifact.getPath() + " was created.", EventType.INFO);
         }
         
         
         return new ArtifactResult(artifact, next, approvals);
     }
 
     private List<ApprovalMessage> approve(ArtifactVersion previous, ArtifactVersion next) {
         boolean approved = true;
         
         List<ApprovalMessage> approvals = policyManager.approve(previous, next);
         for (ApprovalMessage a : approvals) {
             if (!a.isWarning()) {
                 approved = false;
                 break;
             }
         }
         
         if (!approved) {
             throw new RuntimeException(new ArtifactPolicyException(approvals));
         }
         return approvals;
     }
     
     public ArtifactResult createArtifact(Workspace workspace, 
                                          String contentType, 
                                          String name,
                                          String versionLabel, 
                                          InputStream inputStream, 
                                          User user) 
         throws RegistryException, ArtifactPolicyException, IOException, MimeTypeParseException, DuplicateItemException, AccessException {
         accessControlManager.assertAccess(Permission.READ_ARTIFACT);
         contentType = trimContentType(contentType);
         MimeType ct = new MimeType(contentType);
 
         return createArtifact(workspace, inputStream, null, name, versionLabel, ct, user);
     }
 
     private Object getData(Workspace workspace, MimeType contentType, InputStream inputStream) 
         throws RegistryException, IOException {
         ContentHandler ch = contentService.getContentHandler(contentType);
 
         if (ch == null) {
             throw new RegistryException(new Message("UNSUPPORTED_CONTENT_TYPE", BundleUtils.getBundle(getClass()), contentType));
         }
 
         return ch.read(inputStream, workspace);
     }
 
 
     public ArtifactResult newVersion(Artifact artifact, 
                                      Object data, 
                                      String versionLabel, 
                                      User user)
         throws RegistryException, ArtifactPolicyException, IOException, DuplicateItemException, AccessException {
         return newVersion(artifact, null, data, versionLabel, user);
     }
 
     public ArtifactResult newVersion(final Artifact artifact, 
                                      final InputStream inputStream, 
                                      final String versionLabel, 
                                      final User user) 
         throws RegistryException, ArtifactPolicyException, IOException, DuplicateItemException, AccessException {
         return newVersion(artifact, inputStream, null, versionLabel, user);
     }
     
     protected ArtifactResult newVersion(final Artifact artifact, 
                                         final InputStream inputStream, 
                                         final Object data,
                                         final String versionLabel, 
                                         final User user) 
         throws RegistryException, ArtifactPolicyException, IOException, DuplicateItemException, AccessException {
         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, artifact);
         
         if (user == null) {
             throw new NullPointerException("User cannot be null!");
         }
         
         return (ArtifactResult) executeAndDewrap(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 JcrArtifact jcrArtifact = (JcrArtifact) artifact;
                 Node artifactNode = jcrArtifact.getNode();
                 artifactNode.refresh(false);
                 JcrVersion previousLatest = ((JcrVersion)jcrArtifact.getDefaultVersion());
                 Node previousNode = previousLatest.getNode();
                 
                 previousLatest.setDefault(false);
                 previousLatest.setLatest(false);
 
                 ContentHandler ch = contentService.getContentHandler(jcrArtifact.getContentType());
                 
                 // create a new version node
                 Node versionNode = artifactNode.addNode(versionLabel, ARTIFACT_VERSION_NODE_TYPE);
                 versionNode.addMixin("mix:referenceable");
                 
                 Calendar now = Calendar.getInstance();
                 now.setTime(new Date());
                 versionNode.setProperty(JcrVersion.CREATED, now);
                 
                 Node resNode = createVersionContentNode(versionNode, 
                                                         inputStream, 
                                                         jcrArtifact.getContentType(), 
                                                         now);
                 
                 JcrVersion next = new JcrVersion(jcrArtifact, versionNode, resNode);
                 next.setDefault(true);
                 next.setLatest(true);
                 
                 try {
                     // Store the data
                     if (inputStream != null) {
                         InputStream s = next.getStream();
                         Object data = getData(artifact.getParent(), artifact.getContentType(), s);
                         next.setData(data);
                     } else {
                         next.setData(data);
                         resNode.setProperty(JcrVersion.JCR_DATA, ch.read(data));
                     }
                     
                     next.setAuthor(user);
                     next.setLatest(true);
                     next.setEnabled(true);
                     
                    ((List<ArtifactVersion>)jcrArtifact.getVersions()).add(0, next);
                     
                     Lifecycle lifecycle = jcrArtifact.getParent().getDefaultLifecycle();
                     next.setPhase(lifecycle.getInitialPhase());        
                     
                     ch.addMetadata(next);
                     
                     try {
                         Property pNames = previousNode.getProperty(AbstractJcrItem.PROPERTIES);
                     
                         for (Value name : pNames.getValues()) {
                             Property prop = previousNode.getProperty(name.getString());
                             
                             if (prop.getDefinition().isMultiple()) {
                                 versionNode.setProperty(prop.getName(), prop.getValues());
                             } else {
                                 versionNode.setProperty(prop.getName(), prop.getValue());
                             }
                         }
                         
                         versionNode.setProperty(pNames.getName(), pNames.getValues());
                     } catch (PathNotFoundException e) {
                     }
                     return approve(session, artifact, previousLatest, next, user);
                 } catch (RegistryException e) {
                     // this will get dewrapped
                     throw new RuntimeException(e);
                 }
             }
         });
     }
 
     protected void copy(Node original, Node parent) throws RepositoryException {
         Node node = parent.addNode(original.getName());
         node.addMixin("mix:referenceable");
         
         for (PropertyIterator props = original.getProperties(); props.hasNext();) {
             Property p = props.nextProperty();
             if (!p.getName().startsWith("jcr:")) {
                 node.setProperty(p.getName(), p.getValue());
             }
         }
         
         for (NodeIterator nodes = original.getNodes(); nodes.hasNext();) {
             Node child = nodes.nextNode();
             if (!child.getName().startsWith("jcr:")) {
                 copy(child, node);
             }
         }
     }
     
     public void setDefaultVersion(final ArtifactVersion version, 
                                   final User user) throws RegistryException,
         ArtifactPolicyException {
         execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 ArtifactVersion oldDefault = version.getParent().getDefaultVersion();
                 
                 ((JcrVersion) oldDefault).setDefault(false);
                 ((JcrVersion) version).setDefault(true);
                 
                 session.save();
                 return null;
             }
         });
     }
     
     public void setEnabled(final ArtifactVersion version, 
                            final boolean enabled,
                            final User user) throws RegistryException,
         ArtifactPolicyException {
         executeWithPolicy(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 if (enabled) approve(version.getPrevious(), version);
                 
                 ((JcrVersion) version).setEnabled(enabled);
                 
                 session.save();
                 return null;
             }
         });
         
     }
 
     public void save(Artifact artifact) throws RegistryException, AccessException {
         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, artifact);
         
         execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 // TODO: Fix artifact saving, we should have to call artifact.save().
                 session.save();
                 return null;
             }
         });
     }
 
     public void move(final Artifact artifact, final String workspaceId) throws RegistryException, AccessException {
         final Workspace workspace = getWorkspace(workspaceId);
         
         accessControlManager.assertAccess(Permission.MODIFY_WORKSPACE, workspace);
         accessControlManager.assertAccess(Permission.MODIFY_ARTIFACT, artifact);
         
         if (artifact.getParent().getId().equals(workspaceId)) {
             return;
         }
 
         execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 
                 String p1 = artifact.getPath();
                 Node aNode = ((JcrArtifact) artifact).getNode();
                 Node wNode = ((JcrWorkspace) workspace).getNode();
                 
                 session.move(aNode.getPath(), wNode.getPath() + "/" + aNode.getName());
 
                 activityManager.logActivity(SecurityUtils.getCurrentUser(),
                                             "Workspace " + p1 + " was moved to " + artifact.getPath(), 
                                             EventType.INFO);
                 session.save();
                 ((JcrArtifact) artifact).setWorkspace(workspace);
                 return null;
             }
         });
     }
 
     public void delete(final Artifact artifact) throws RegistryException, AccessException {
         accessControlManager.assertAccess(Permission.DELETE_ARTIFACT, artifact);
 
         executeWithRegistryException(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Set<Dependency> deps;
                 try {
                     deps = getDependedOnBy(artifact);
                 } catch (QueryException e) {
                     throw new RuntimeException(e);
                 } catch (RegistryException e) {
                     throw new RuntimeException(e);
                 }
                 
                 for (Dependency d : deps) {
                     ((JcrDependency) d).getDependencyNode().remove();
                 }
                 String path = artifact.getPath();
                 ((JcrArtifact) artifact).getNode().remove();
 
                 activityManager.logActivity(SecurityUtils.getCurrentUser(),
                                             "Artifact " + path + " was deleted", 
                                             EventType.INFO);
                 
                 session.save();
                 return null;
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
         final JcrRegistryImpl registry = this;
         return (SearchResults) executeWithQueryException(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                         
                 QueryManager qm = getQueryManager(session);
                 
                 Set<Object> artifacts = new HashSet<Object>();
                 
                 boolean av = false;
                 Class<?> selectType = query.getSelectType();
                 if (selectType.equals(ArtifactVersion.class)) {
                     av = true;
                 } else if (!selectType.equals(Artifact.class)) {
                     throw new RuntimeException(new QueryException(new Message("INVALID_SELECT_TYPE", BundleUtils.getBundle(getClass()), selectType.getName())));
                 }
                 
                 Map<FunctionCall, AbstractFunction> functions = new HashMap<FunctionCall, AbstractFunction>();
                 
                 String qstr = null;
                 try {
                     qstr = createQueryString(query, av, functions);
                 } catch (QueryException e) {
                     // will be dewrapped later
                     throw new RuntimeException(e);
                 }
 
                 if (log.isDebugEnabled())
                 {
                     log.debug("Query: " + qstr.toString());
                 }
                 
                 Query jcrQuery = qm.createQuery(qstr, Query.XPATH);
                 
                 QueryResult result = jcrQuery.execute();
                 NodeIterator nodes = result.getNodes(); 
                
                 if (query.getStart() != -1) {
                     if (nodes.getSize() <= query.getStart()) {
                         return new SearchResults(0, artifacts);
                     } else {
                         nodes.skip(query.getStart());
                     }
                 }
                 
                 int max = query.getMaxResults();
                 int count = 0;
                 while (nodes.hasNext()) {
                     Node node = nodes.nextNode();
                     
                     // UGH: jackrabbit does not support parent::* xpath expressions
                     // so we need to traverse the hierarchy to find the right node
                     if (av) {
                         Node artifactNode = node;
                         while (!artifactNode.getPrimaryNodeType().getName().equals(ARTIFACT_NODE_TYPE)) {
                             artifactNode = node.getParent();
                         }
                         JcrArtifact artifact = new JcrArtifact(new JcrWorkspace(registry, lifecycleManager, artifactNode.getParent()), 
                                                                artifactNode, 
                                                                registry);
                         try {
                             accessControlManager.assertAccess(Permission.READ_ARTIFACT, artifact);
 
                             setupContentHandler(artifact);
                             artifacts.add(new JcrVersion(artifact, node));
                         } catch (AccessException e) {
                             // don't include artifacts the user can't read in the search
                         }
                     } else {
                         while (!node.getPrimaryNodeType().getName().equals(ARTIFACT_NODE_TYPE)) {
                             node = node.getParent();
                         }
                         JcrArtifact artifact = new JcrArtifact(new JcrWorkspace(registry, lifecycleManager, node.getParent()), node,
                                                                registry);
                         
                         try {
                             accessControlManager.assertAccess(Permission.READ_ARTIFACT, artifact);
 
                             setupContentHandler(artifact);
                             artifacts.add(artifact);
                         } catch (AccessException e) {
                             // don't include artifacts the user can't read in the search
                         }
                     }
                     
                     count++;
                     
                     if (count == max) {
                         break;
                     }
                 }                                                   
                 
                 for (Map.Entry<FunctionCall, AbstractFunction> e : functions.entrySet()) {
                     if (av) {
                         Set<ArtifactVersion> artifacts2 = new HashSet<ArtifactVersion>();
                         for (Object o : artifacts) {
                             artifacts2.add((ArtifactVersion) o);
                         }
                         e.getValue().modifyArtifactVersions(e.getKey().getArguments(), artifacts2);
                         return new SearchResults(artifacts2.size(), artifacts2);
                     } else {
                         Set<Artifact> artifacts2 = new HashSet<Artifact>();
                         for (Object o : artifacts) {
                             artifacts2.add((Artifact) o);
                         }
                         e.getValue().modifyArtifacts(e.getKey().getArguments(), artifacts2);
                         return new SearchResults(artifacts2.size(), artifacts2);
                     }
                 }
                 return new SearchResults(nodes.getSize(), artifacts);
             }
 
         });
     }
 
     protected String createQueryString(final org.mule.galaxy.query.Query query, 
                                        boolean av, 
                                        Map<FunctionCall, AbstractFunction> functions) throws QueryException {
         StringBuilder base = new StringBuilder();
         
         // Search by workspace id, workspace path, or any workspace
         if (query.getWorkspaceId() != null) {
             base.append("//*[@jcr:uuid='")
                 .append(query.getWorkspaceId())
                 .append("'][@jcr:primaryType=\"galaxy:workspace\"]/element(*, galaxy:artifact)");
         } else if (query.getWorkspacePath() != null) {
             String path = query.getWorkspacePath();
 
             if (path.startsWith("/")) {
                 path = path.substring(1);
             }
             
             if (path.endsWith("/")) {
                 path = path.substring(0, path.length() - 1);
             }
             
             base.append("//")
             .append(ISO9075.encode(path))
             .append("[@jcr:primaryType=\"galaxy:workspace\"]/element(*, galaxy:artifact)");
         } else {
             base.append("//element(*, galaxy:artifact)");
         }
         
         StringBuilder avQuery = new StringBuilder();
         StringBuilder artifactQuery = new StringBuilder();
 
         for (Restriction r : query.getRestrictions()) {
             if (r instanceof OpRestriction) {
                 handleOperator((OpRestriction) r, artifactQuery, avQuery);
             } else if (r instanceof FunctionCall) {
                 handleFunction((FunctionCall) r, functions, artifactQuery, avQuery);
             }
         }
         
         if (avQuery.length() > 0) avQuery.append("]");
         if (artifactQuery.length() > 0) artifactQuery.append("]");
 
         // Search the latest if we're searching for artifacts, otherwise
         // search all versions
         if (!av) {
             avQuery.insert(0, "/*[@latest='true']");
         } else {
             avQuery.insert(0, "/*");
         }
         
         base.append(artifactQuery);
         base.append(avQuery);
         
         return base.toString();
     }
 
     private void handleFunction(FunctionCall r, Map<FunctionCall, AbstractFunction> functions, StringBuilder qstr, StringBuilder propStr) throws QueryException {
         AbstractFunction fn = functionRegistry.getFunction(r.getModule(), r.getName());
         
         functions.put(r, fn);
         
         // Narrow down query if possible
         List<OpRestriction> restrictions = fn.getRestrictions(r.getArguments());
         
         if (restrictions != null && restrictions.size() > 0) {
             for (OpRestriction opR : restrictions) {
                 handleOperator(opR, qstr, propStr);
             }
         }
     }
 
     private void handleOperator(OpRestriction or, StringBuilder artifactQuery, StringBuilder avQuery)
         throws QueryException {
         
         // TODO: NOT, LIKE, OR, etc
         String property = (String) or.getLeft();
         boolean not = false;
         Operator operator = or.getOperator();
         
         if (operator.equals(Operator.NOT)) {
             not = true;
             or = (OpRestriction) or.getRight();
             operator = or.getOperator();
             property = or.getLeft().toString();
         }
 
         QueryBuilder builder = getQueryBuilder(property);
         StringBuilder query;
         // are we searching a property on the artifact itself or the artifact version?
         if (builder.isArtifactProperty()) {
             query = artifactQuery;
         } else {
             query = avQuery;
         }
         
         if (query.length() == 0) {
             query.append("[");
         } else {
             query.append(" and ");
         }
         
         builder.build(query, property, or.getRight(), not, operator);
     }
 
     private QueryBuilder getQueryBuilder(String property) throws QueryException {
         QueryBuilder qb = queryBuilders.get(property);
         
         if (qb == null) {
             return simpleQueryBuilder;
         }
         
         return qb;
     }
 
     @SuppressWarnings("unchecked")
     public Set<Dependency> getDependedOnBy(final Artifact artifact) 
         throws RegistryException {
         final JcrRegistryImpl registry = this;
         
         return (Set<Dependency>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                         
                 QueryManager qm = getQueryManager(session);
                 
                 StringBuilder qstr = new StringBuilder();
                 qstr.append("//element(*, galaxy:artifact)/*/")
                     .append(JcrVersion.DEPENDENCIES)
                     .append("/")
                     .append(ISO9075.encode(artifact.getId()))
                     .append("");
                 
                 Set<Dependency> artifacts = new HashSet<Dependency>();
                 
                 Query query = qm.createQuery(qstr.toString(), Query.XPATH);
                 
                 QueryResult result = query.execute();
                 
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     final Node depNode = nodes.nextNode();
                     
                     final Node node = depNode.getParent().getParent().getParent();
                     
                     JcrWorkspace workspace = new JcrWorkspace(registry, lifecycleManager, node.getParent());
                     final JcrArtifact artDep = new JcrArtifact(workspace, node, registry);
                     
                     Dependency dependency = new JcrDependency(artDep, depNode);
                     artifacts.add(dependency);
                 }
                 return artifacts;
             }
 
         });
     }
 
     @SuppressWarnings("unchecked")
     public PropertyDescriptor getPropertyDescriptorByName(final String propertyName) {
         
         return (PropertyDescriptor) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 List<PropertyDescriptor> pds = propertyDescriptorDao.find("property", propertyName);
                 
                 if (pds.size() == 0) {
                     return null;
                 }
                 return pds.get(0);
             }
         });
     }
 
     public PropertyDescriptor getPropertyDescriptor(final String id) throws NotFoundException {
         return propertyDescriptorDao.get(id);
     }
     
     public Collection<PropertyDescriptor> getPropertyDescriptors() throws RegistryException {
         return propertyDescriptorDao.listAll();
     }
 
     public void savePropertyDescriptor(PropertyDescriptor pd) throws RegistryException, AccessException, DuplicateItemException, NotFoundException {
         accessControlManager.assertAccess(Permission.MANAGE_PROPERTIES);
         propertyDescriptorDao.save(pd);
     }
     
     public void deletePropertyDescriptor(String id) throws RegistryException {
         propertyDescriptorDao.delete(id);
     }
     protected String escapeNodeName(String right) {
         return ISO9075.encode(right);
     }
 
     private String trimContentType(String contentType) {
         int comma = contentType.indexOf(';');
         if (comma != -1) {
             contentType = contentType.substring(0, comma);
         }
         return contentType;
     }
 
     public void initialize() throws Exception {
         
         Session session = getSessionFactory().getSession();
         Node root = session.getRootNode();
         
         Node workspaces = JcrUtil.getOrCreate(root, "workspaces", "galaxy:noSiblings");
         
         workspacesId = workspaces.getUUID();
         indexesId = JcrUtil.getOrCreate(root, "indexes").getUUID();
         artifactTypesId = JcrUtil.getOrCreate(root, "artifactTypes").getUUID();
 
         NodeIterator nodes = workspaces.getNodes();
         // ignore the system node
         if (nodes.getSize() == 0) {
             Node node = workspaces.addNode(settings.getDefaultWorkspaceName(),
                                            "galaxy:workspace");
             node.addMixin("mix:referenceable");
 
             JcrWorkspace w = new JcrWorkspace(this, lifecycleManager, node);
             w.setName(settings.getDefaultWorkspaceName());
 
             workspaces.setProperty(REPOSITORY_LAYOUT_VERSION, "2");
         } 
         id = workspaces.getUUID();
         
         session.save();
         
         for (ContentHandler ch : contentService.getContentHandlers()) {
             ch.setRegistry(this);
         }
         
         for (ArtifactPolicy a : policyManager.getPolicies()) {
             a.setRegistry(this);
         }
         
         session.logout();
     }
 
     public void addDependencies(ArtifactVersion artifactVersion, final Artifact... dependencies)
         throws RegistryException {
         final JcrVersion jcrVersion = (JcrVersion) artifactVersion;
         
         if (dependencies != null) {
             execute(new JcrCallback() {
                 public Object doInJcr(Session session) throws IOException, RepositoryException {
                     jcrVersion.addDependencies(dependencies, true);
                     session.save();
                     return null;
                 }
             });
         }
     }
     
     public void removeDependencies(ArtifactVersion artifactVersion, final Artifact... dependencies)
         throws RegistryException {
         final JcrVersion jcrVersion = (JcrVersion) artifactVersion;
         
         if (dependencies != null) {
             execute(new JcrCallback() {
                 public Object doInJcr(Session session) throws IOException, RepositoryException {
                     Set<String> ids = new HashSet<String>();
                     for (Artifact a : dependencies) {
                         ids.add(a.getId());
                     }
 
                     Node depsNode = JcrUtil.getOrCreate(jcrVersion.node, JcrVersion.DEPENDENCIES);
                     for (NodeIterator nodes = depsNode.getNodes(); nodes.hasNext();) {
                         Node dep = nodes.nextNode();
                         Boolean user = JcrUtil.getBooleanOrNull(dep, JcrVersion.USER_SPECIFIED);
                         if (user != null && user && ids.contains(dep.getName())) {
                             dep.remove();
                         }
                     }
                     session.save();
                     return null;
                 }
             });
         }
     }
     
     public void setQueryBuilders(List<QueryBuilder> qbs) {
         queryBuilders = new HashMap<String, QueryBuilder>();
         for (QueryBuilder q : qbs) {
             for (String prop : q.getProperties()) {
                 queryBuilders.put(prop, q);
             }
         }
     }
     public ActivityManager getActivityManager() {
         return activityManager;
     }
 
     public void setAccessControlManager(AccessControlManager accessControlManager) {
         this.accessControlManager = accessControlManager;
     }
 
     public void setActivityManager(ActivityManager activityManager) {
         this.activityManager = activityManager;
     }
 
     public void setFunctionRegistry(FunctionRegistry functionRegistry) {
         this.functionRegistry = functionRegistry;
     }
 
     public void setSettings(Settings settings) {
         this.settings = settings;
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
 
     public void setIndexManager(IndexManager indexManager) {
         this.indexManager = indexManager;
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
 
     public void setPropertyDescriptorDao(Dao<PropertyDescriptor> propertyDescriptorDao) {
         this.propertyDescriptorDao = propertyDescriptorDao;
     }
 
     public void setArtifactTypeDao(ArtifactTypeDao artifactTypeDao) {
         this.artifactTypeDao = artifactTypeDao;
     }
 }
