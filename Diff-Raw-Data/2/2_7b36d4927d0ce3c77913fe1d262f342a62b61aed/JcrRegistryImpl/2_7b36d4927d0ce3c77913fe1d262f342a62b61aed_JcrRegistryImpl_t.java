 package org.mule.galaxy.impl.jcr;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.activation.MimeType;
 import javax.activation.MimeTypeParseException;
 import javax.jcr.ItemExistsException;
 import javax.jcr.NamespaceException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.jcr.version.VersionException;
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import net.sf.saxon.javax.xml.xquery.XQConnection;
 import net.sf.saxon.javax.xml.xquery.XQDataSource;
 import net.sf.saxon.javax.xml.xquery.XQItem;
 import net.sf.saxon.javax.xml.xquery.XQPreparedExpression;
 import net.sf.saxon.javax.xml.xquery.XQResultSequence;
 import net.sf.saxon.xqj.SaxonXQDataSource;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
 import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
 import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
 import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactResult;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Comment;
 import org.mule.galaxy.ContentHandler;
 import org.mule.galaxy.ContentService;
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.Index;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Settings;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.XmlContentHandler;
 import org.mule.galaxy.Index.Language;
 import org.mule.galaxy.impl.IndexImpl;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.Approval;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.util.DOMUtils;
 import org.mule.galaxy.util.LogUtils;
 import org.mule.galaxy.util.Message;
 import org.mule.galaxy.util.QNameUtil;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 
 public class JcrRegistryImpl extends JcrTemplate implements Registry, JcrRegistry {
 
     public static final String ARTIFACT_NODE_NAME = "__artifact";
     public static final String LATEST = "latest";
     private static final String NAMESPACE = "http://galaxy.mule.org";
 
     private Logger LOGGER = LogUtils.getL7dLogger(JcrRegistryImpl.class);
 
     private XPathFactory factory = XPathFactory.newInstance();
     
     private Settings settings;
     
     private ContentService contentService;
 
     private LifecycleManager lifecycleManager;
     
     private PolicyManager policyManager;
     
     private UserManager userManager;
 
     private Dao<Comment> commentDao;
     
     private String workspacesId;
 
     private String indexesId;
 
     private String artifactTypesId;
     
     
     public JcrRegistryImpl() {
         super();
     }
 
     public Workspace getWorkspace(String id) throws RegistryException {
         // TODO: implement a query
         // TODO: possibility for injenction in the id here?
         try {
             Node node = getWorkspacesNode().getNode(id);
 
             return new JcrWorkspace(node);
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
     }
 
     
     public Workspace createWorkspace(String name) throws RegistryException {
         try {
             Node node = getWorkspacesNode().addNode(name, "galaxy:workspace");
             node.addMixin("mix:referenceable");
 
             JcrWorkspace workspace = new JcrWorkspace(node);
             workspace.setName(name);
             return workspace;
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
     }
     
 
     public Workspace createWorkspace(Workspace parent, String name) throws RegistryException {
         try {
             Collection<Workspace> workspaces = parent.getWorkspaces();
             
             Node parentNode = ((JcrWorkspace) parent).getNode();
             Node node = parentNode.addNode(name);
             node.addMixin("mix:referenceable");
 
             JcrWorkspace workspace = new JcrWorkspace(node);
             workspace.setName(name);
             workspaces.add(workspace);
             return workspace;
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
     
     }
 
     public void removeWorkspace(Workspace w) throws RegistryException {
         try {
             ((JcrWorkspace) w).getNode().remove();
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
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
     
     public Collection<Workspace> getWorkspaces() throws RegistryException {
         try {
             Collection<Workspace> workspaceCol = new ArrayList<Workspace>();
             for (NodeIterator itr = getWorkspacesNode().getNodes(); itr.hasNext();) {
                 Node n = itr.nextNode();
 
                 if (!n.getName().equals("jcr:system")) {
                     workspaceCol.add(new JcrWorkspace(n));
                 }
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
                 artifact.setContentHandler(contentService.getContentHandler(artifact.getContentType()));
                 artifacts.add(artifact);
             }
         } catch (RepositoryException e) {
             throw new RegistryException(e);
         }
 
         return artifacts;
     }
 
     public Artifact getArtifact(final String id) throws NotFoundException {
         final JcrRegistryImpl registry = this;
         return (Artifact) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Node node = session.getNodeByUUID(id);
                 Node wNode = node.getParent();
                 JcrArtifact artifact = new JcrArtifact(new JcrWorkspace(wNode), 
                                                        node, registry);
                 
                 artifact.setContentHandler(contentService.getContentHandler(artifact.getContentType()));
 
                 return artifact;
             }
         });
     }
 
     
     public Artifact getArtifact(final Workspace w, final  String name) throws NotFoundException {
         final JcrRegistryImpl registry = this;
         Artifact a = (Artifact) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 StringBuilder sb = new StringBuilder();
                 sb.append("//")
                   .append(ARTIFACT_NODE_NAME)
                   .append("[@name='")
                   .append(name)
                   .append("']");
                 
                 Query query = qm.createQuery(sb.toString(), Query.XPATH);
                 
                 QueryResult result = query.execute();
                 NodeIterator nodes = result.getNodes();
                 
                 if (nodes.hasNext()) {
                     Node node = nodes.nextNode();
                     JcrArtifact artifact = new JcrArtifact(w, node, registry);
                     
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
         throws RegistryException, ArtifactPolicyException, MimeTypeParseException {
         ContentHandler ch = contentService.getContentHandler(data.getClass());
         
         if (ch == null) {
             throw new RegistryException(new Message("UNKNOWN_TYPE", LOGGER, data.getClass()));
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
         throws RegistryException, ArtifactPolicyException {
         
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
                 Node artifactNode = workspaceNode.addNode(ARTIFACT_NODE_NAME);
                 artifactNode.addMixin("mix:referenceable");
                 Node versionNode = artifactNode.addNode("version");
                 if (is != null) {
                     versionNode.setProperty(JcrVersion.DATA, is);
                 }
                 
                 ContentHandler ch = contentService.getContentHandler(contentType);
                 
                 JcrArtifact artifact = new JcrArtifact(workspace, artifactNode, registry);
                 artifact.setContentType(contentType);
                 artifact.setName(name);
                 artifact.setContentHandler(ch);
                 
                 // set up the initial version
                 
                 Calendar now = Calendar.getInstance();
                 now.setTime(new Date());
                 versionNode.setProperty(JcrVersion.CREATED, now);
                 versionNode.setProperty(JcrVersion.LATEST, true);
                 
                 JcrVersion jcrVersion = new JcrVersion(artifact, versionNode);
                 
                 // Store the data
                 Object loadedData = null;
                 if (data != null) {
                     jcrVersion.setData(data);
                     InputStream dataStream = ch.read(data);
                     versionNode.setProperty(JcrVersion.DATA, dataStream);
                     loadedData = data;
                 } else {
                     InputStream dataStream = versionNode.getProperty(JcrVersion.DATA).getStream();
                     jcrVersion.setData(ch.read(dataStream, workspace));
                     loadedData = jcrVersion.getData();
                 }
                 
                 if (ch instanceof XmlContentHandler) {
                     XmlContentHandler xch = (XmlContentHandler) ch;
                     artifact.setDocumentType(xch.getDocumentType(loadedData));
                 }
     
                 jcrVersion.setVersionLabel(versionLabel);
                 jcrVersion.setAuthor(user);
                 jcrVersion.setLatest(true);
                 
                 try {
                     index(jcrVersion);
                 
                     Set<Artifact> dependencies = ch.detectDependencies(loadedData, workspace);
                     jcrVersion.addDependencies(dependencies, false);
                     
                     Lifecycle lifecycle = lifecycleManager.getLifecycle(workspace);
                     artifact.setPhase(lifecycle.getInitialPhase());
                     
                     
                     Set<ArtifactVersion> versions = new HashSet<ArtifactVersion>();
                     versions.add(jcrVersion);
                     artifact.setVersions(versions);
                     
                     LOGGER.info("Created artifact " + artifact.getId());
     
                     return approve(session, artifact, null, jcrVersion);
                 } catch (RegistryException e) {
                     // gets unwrapped by executeAndDewrap
                     throw new RuntimeException(e);
                 }
             }
 
         });
     }
 
     private ArtifactResult executeAndDewrap(JcrCallback jcrCallback) 
         throws RegistryException, ArtifactPolicyException {
         try {
             return (ArtifactResult) execute(jcrCallback);
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
 
     private ArtifactResult approve(Session session, Artifact artifact, 
                                    JcrVersion previous, JcrVersion next)
         throws RegistryException, RepositoryException {
         boolean approved = true;
         
         Collection<Approval> approvals = approve(previous, next);
         for (Approval a : approvals) {
             if (!a.isApproved()) {
                 approved = false;
             }
         }
         
         if (!approved) {
             throw new RuntimeException(new ArtifactPolicyException(approvals));
         }
         
         session.save();
 
         return new ArtifactResult(artifact, next, approvals);
     }
     
     public ArtifactResult createArtifact(Workspace workspace, 
                                          String contentType, 
                                          String name,
                                          String versionLabel, 
                                          InputStream inputStream, 
                                          User user) 
         throws RegistryException, ArtifactPolicyException, IOException, MimeTypeParseException {
         contentType = trimContentType(contentType);
         MimeType ct = new MimeType(contentType);
 
         return createArtifact(workspace, inputStream, null, name, versionLabel, ct, user);
     }
 
     private Object getData(Workspace workspace, MimeType contentType, InputStream inputStream) 
         throws RegistryException, IOException {
         ContentHandler ch = contentService.getContentHandler(contentType);
 
         if (ch == null) {
             throw new RegistryException(new Message("UNSUPPORTED_CONTENT_TYPE", LOGGER, contentType));
         }
 
         return ch.read(inputStream, workspace);
     }
 
 
     public ArtifactResult newVersion(Artifact artifact, 
                                      Object data, 
                                      String versionLabel, 
                                      User user)
         throws RegistryException, ArtifactPolicyException, IOException {
         return newVersion(artifact, null, data, versionLabel, user);
     }
 
     public ArtifactResult newVersion(final Artifact artifact, 
                                      final InputStream inputStream, 
                                      final String versionLabel, 
                                      final User user) 
         throws RegistryException, ArtifactPolicyException, IOException {
         return newVersion(artifact, inputStream, null, versionLabel, user);
     }
     
     protected ArtifactResult newVersion(final Artifact artifact, 
                                         final InputStream inputStream, 
                                         final Object data,
                                         final String versionLabel, 
                                         final User user) 
         throws RegistryException, ArtifactPolicyException, IOException {
        
         if (user == null) {
             throw new NullPointerException("User cannot be null!");
         }
         
         return (ArtifactResult) executeAndDewrap(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 JcrArtifact jcrArtifact = (JcrArtifact) artifact;
                 Node artifactNode = jcrArtifact.getNode();
                 artifactNode.refresh(false);
                 JcrVersion previousLatest = ((JcrVersion)jcrArtifact.getLatestVersion());
                 Node previousNode = previousLatest.getNode();
                 previousNode.setProperty(JcrVersion.LATEST, (String) null);
                 previousLatest.setLatest(false);
 
                 ContentHandler ch = contentService.getContentHandler(jcrArtifact.getContentType());
                 
                 // create a new version node
                 Node versionNode = artifactNode.addNode("version");
                 
                 Calendar now = Calendar.getInstance();
                 now.setTime(new Date());
                 versionNode.setProperty(JcrVersion.CREATED, now);
 
                 versionNode.setProperty(JcrVersion.LATEST, true);
                 
                 
                 
                 JcrVersion next = new JcrVersion(jcrArtifact, versionNode);
                 
                 try {
                     // Store the data
                     if (inputStream != null) {
                         versionNode.setProperty(JcrVersion.DATA, inputStream);
                         
                         InputStream s = versionNode.getProperty(JcrVersion.DATA).getStream();
                         Object data = getData(artifact.getWorkspace(), artifact.getContentType(), s);
                         next.setData(data);
                     } else {
                         next.setData(data);
                         versionNode.setProperty(JcrVersion.DATA, ch.read(data));
                     }
                     
                     next.setVersionLabel(versionLabel);
                     next.setAuthor(user);
                     next.setLatest(true);
                     
                     jcrArtifact.getVersions().add(next);
                     ch.addMetadata(next);
                     
                     Node newProps = JcrUtil.getOrCreate(versionNode, "properties");
                     Node prevProps = previousNode.getNode("properties");
                     
                     for (NodeIterator nodes = prevProps.getNodes(); nodes.hasNext();) {
                         copy(nodes.nextNode(), newProps);
                     }
                 
                     return approve(session, artifact, previousLatest, next);
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
 
     /**
      * Approve the next artifact version. NOTE: previous may be null here!
      * @param previous
      * @param next
      * @return
      * @throws RegistryException
      */
     public Collection<Approval> approve(ArtifactVersion previous, 
                                         ArtifactVersion next) throws RegistryException {
         Collection<ArtifactPolicy> policies = policyManager.getActivePolicies(next.getParent());
         ArrayList<Approval> approvals = new ArrayList<Approval>();
         for (ArtifactPolicy p : policies) {
             approvals.add(p.isApproved(next.getParent(), previous, next));
         }
         return approvals;
     }
 
     public void delete(Artifact artifact) {
         throw new UnsupportedOperationException();
     }
     
     @SuppressWarnings("unchecked")
     public Set<Index> getIndices() {
         return (Set<Index>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 Set<Index> indices = new HashSet<Index>();
                 for (NodeIterator nodes = getIndexNode().getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     indices.add(createIndexFromNode(node.getParent()));
                 }
                 return indices;
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     public Set<Index> getIndices(final QName documentType) throws RegistryException {
         return (Set<Index>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 Query query = qm.createQuery("//indexes/*/documentType[@value='" + documentType.toString() + "']", 
                                              Query.XPATH);
                 
                 QueryResult result = query.execute();
                 
                 Set<Index> indices = new HashSet<Index>();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     indices.add(createIndexFromNode(node.getParent()));
                 }
                 return indices;
             }
         });
     }
 
     private Index createIndexFromNode(Node node) throws RepositoryException {
         IndexImpl idx = new IndexImpl();
         
         idx.setId(JcrUtil.getStringOrNull(node, IndexImpl.ID));
         idx.setExpression(JcrUtil.getStringOrNull(node, IndexImpl.EXPRESSION));
         idx.setLanguage(Language.valueOf(JcrUtil.getStringOrNull(node, IndexImpl.LANGUAGE)));
         idx.setName(JcrUtil.getStringOrNull(node, IndexImpl.NAME));
         
         String qt = JcrUtil.getStringOrNull(node, IndexImpl.QUERY_TYPE);
         try {
             idx.setQueryType(getClass().getClassLoader().loadClass(qt));
         } catch (ClassNotFoundException e) {
             // not gonna happen
             throw new RuntimeException(e);
         }
         
         HashSet<QName> docTypes = new HashSet<QName>();
         for (NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
             Node child = nodes.nextNode();
             
             if (child.getName().equals(IndexImpl.DOCUMENT_TYPE)) {
                 String value = JcrUtil.getStringOrNull(child, IndexImpl.DOCUMENT_TYPE_VALUE);
                 
                 docTypes.add(QNameUtil.fromString(value));
             }
         }
         idx.setDocumentTypes(docTypes);
         
         return idx;
     }
 
     private QueryManager getQueryManager(Session session) throws RepositoryException {
         return session.getWorkspace().getQueryManager();
     }
 
     public Index registerIndex(final String indexId, 
                                final String displayName, 
                                final Index.Language language,
                                final Class<?> searchType,
                                final String expression, 
                                final QName... documentTypes) throws RegistryException {
         // TODO: check if index name already exists.
         
         return (Index) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 
                 Node idxNode = JcrUtil.getOrCreate(getIndexNode(), indexId);
                 
                 idxNode.setProperty(IndexImpl.ID, indexId);
                 idxNode.setProperty(IndexImpl.EXPRESSION, expression);
                 idxNode.setProperty(IndexImpl.NAME, displayName);
                 idxNode.setProperty(IndexImpl.QUERY_TYPE, searchType.getName());
                 idxNode.setProperty(IndexImpl.LANGUAGE, language.toString());
                 
                 String name = IndexImpl.DOCUMENT_TYPE;
                 JcrUtil.removeChildren(idxNode, name);
                 
                 Set<QName> typeSet = new HashSet<QName>();
                 for (QName q : documentTypes) {
                     typeSet.add(q);
                     Node typeNode = idxNode.addNode(name);
                     typeNode.setProperty(IndexImpl.DOCUMENT_TYPE_VALUE, q.toString());
                 }
                 
                 session.save();
                 
                 IndexImpl idx = new IndexImpl();
                 idx.setId(indexId);
                 idx.setName(displayName);
                 idx.setLanguage(language);
                 idx.setQueryType(searchType);
                 idx.setExpression(expression);
                 idx.setDocumentTypes(typeSet);
                 
                 return idx;
             }
         });
     }
 
     public Set search(String queryString) throws RegistryException, QueryException {
         List<String> tokens = new ArrayList<String>();
         int start = 0;
         for (int i = 0; i < queryString.length(); i++) {
             char c = queryString.charAt(i);
             switch (c) {
             case ' ':
                 if (start != i) {
                     tokens.add(queryString.substring(start, i));
                 }
                 start = i + 1;
                 break;
             case '=':
                  tokens.add("=");
                  start = i+1;
                  break;
             case '<':
                 if (queryString.charAt(i+1) == '=') {
                     i++;
                     tokens.add("<=");
                 } else {
                     tokens.add("<");
                 }
                 start = i+1;  
                 break;
             }
         }
         
         if (start != queryString.length()) {
             tokens.add(queryString.substring(start));
         }
 
         Iterator<String> itr = tokens.iterator(); 
         if (!itr.hasNext()) {
             throw new QueryException(new Message("EMPTY_QUERY_STRING", LOGGER));
         }
         
         if (!itr.next().toLowerCase().equals("select")){
             throw new QueryException(new Message("EXPECTED_SELECT", LOGGER));
         }
         
         if (!itr.hasNext()) {
             throw new QueryException(new Message("EXPECTED_SELECT_TYPE", LOGGER));
         }
         
         Class<?> selectTypeCls = null;
         String selectType = itr.next();
         if (selectType.equals("artifact")) {
             selectTypeCls = Artifact.class;
         } else if (selectType.equals("artifactVersion")) {
             selectTypeCls = ArtifactVersion.class;
         } else {
             throw new QueryException(new Message("UNKNOWN_SELECT_TYPE", LOGGER, selectType));
         }
         
         if (!itr.hasNext()){
             return search(new org.mule.galaxy.query.Query(selectTypeCls));
         } else if  (!itr.next().toLowerCase().equals("where")) {
             throw new QueryException(new Message("EXPECTED_WHERE", LOGGER));
         }
         
         org.mule.galaxy.query.Query q = null;
         while (itr.hasNext()) {
             String left = itr.next();
             if (!itr.hasNext()) {
                 throw new QueryException(new Message("EXPECTED_COMPARATOR", LOGGER));
             }
             
             String compare = itr.next();
             
             if (!itr.hasNext()) {
                 throw new QueryException(new Message("EXPECTED_RIGHT", LOGGER));
             }
             
             String right = itr.next();
             
             if (right.startsWith("'") && right.endsWith("'")) {
                 right = right.substring(1, right.length()-1);
             }
             
             Restriction r = null;
             if (compare.equals("=")) {
                 r = Restriction.eq(left, right);
             } else {
                 new QueryException(new Message("UNKNOWN_COMPARATOR", LOGGER));
             }
             
             if (q == null) {
                 q = new org.mule.galaxy.query.Query(selectTypeCls, r);
             } else {
                 q.add(r);
             }
         }
         
         return search(q);
     }
 
 
     public Set search(final org.mule.galaxy.query.Query query) 
         throws RegistryException, QueryException {
         final JcrRegistryImpl registry = this;
         return (Set) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                         
                 QueryManager qm = getQueryManager(session);
                 StringBuilder qstr = new StringBuilder();
                 
                 Set<Object> artifacts = new HashSet<Object>();
                 
                 boolean av = false;
                 Class<?> selectType = query.getSelectType();
                 if (selectType.equals(ArtifactVersion.class)) {
                     av = true;
                 } else if (!selectType.equals(Artifact.class)) {
                     throw new RuntimeException(new QueryException(new Message("INVALID_SELECT_TYPE", LOGGER, selectType.getName())));
                 }
                 
                 for (Restriction r : query.getRestrictions()) {
                     
                     // TODO: NOT, LIKE, OR, etc
                     
                     String property = (String) r.getLeft();
                     qstr.append("//")
                         .append(ARTIFACT_NODE_NAME);
                     // Search the latest if we're searching for artifacts, otherwise
                     // search all versions
                     if (!av) {
                         qstr.append("/version[@latest='true']");
                     } else {
                         qstr.append("/version");
                     }
                     
                     qstr.append("/properties/")
                         .append(property)
                         .append("/")
                         .append(JcrUtil.VALUE)
                         .append("[@")
                         .append(JcrUtil.VALUE)
                         .append("= \"")
                         .append(r.getRight())
                         .append("\"]");
                 }
                 
                 // No search criteria
                 if (qstr.length() == 0) {
                     qstr.append("//")
                         .append(ARTIFACT_NODE_NAME);
                 }
                 
                 LOGGER.info("Query: " + qstr.toString());
                 
                 Query jcrQuery = qm.createQuery(qstr.toString(), Query.XPATH);
                 
                 QueryResult result = jcrQuery.execute();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
                     
                     // UGH: jackrabbit does not support parent::* xpath expressions
                     // so we need to traverse the hierarchy to find the right node
                     if (av) {
                         while (!node.getName().equals("version")) {
                             node = node.getParent();
                         }
                         Node artifactNode = node.getParent();
                         JcrArtifact artifact = new JcrArtifact(new JcrWorkspace(artifactNode.getParent()), 
                                                                artifactNode, 
                                                                registry);
                         artifacts.add(new JcrVersion(artifact, node));
                     } else {
                         while (!node.getName().equals(ARTIFACT_NODE_NAME)) {
                             node = node.getParent();
                         }
                         artifacts.add(new JcrArtifact(new JcrWorkspace(node.getParent()), 
                                                       node, 
                                                       registry));
                     }
                     
                     
                 }                                                   
                 
                 return artifacts;
             }
         });
     }
 
     private String trimContentType(String contentType) {
         int comma = contentType.indexOf(';');
         if (comma != -1) {
             contentType = contentType.substring(0, comma);
         }
         return contentType;
     }
 
     private void index(JcrVersion jcrVersion) throws RegistryException {
         QName dt = jcrVersion.getParent().getDocumentType();
         if (dt == null) return;
         
         Set<Index> indices = getIndices(dt);
         
         for (Index idx : indices) {
             try {
                 switch (idx.getLanguage()) {
                 case XQUERY:
                     indexWithXQuery(jcrVersion, idx);
                     break;
                 case XPATH:
                     indexWithXPath(jcrVersion, idx);
                     break;
                 default:
                     throw new UnsupportedOperationException();
                 }
             } catch (Throwable t) {
                 LOGGER.log(Level.SEVERE, "Could not process index " + idx.getId(), t);
             }
         }
     }
 
     private void indexWithXPath(JcrVersion jcrVersion, Index idx) throws RegistryException {
         XmlContentHandler ch = (XmlContentHandler) contentService.getContentHandler(jcrVersion.getParent().getContentType());
         try {
             Document document = ch.getDocument(jcrVersion.getData());
             
             XPath xpath = factory.newXPath();
             XPathExpression expr = xpath.compile(idx.getExpression());
 
             Object result = expr.evaluate(document, XPathConstants.STRING);
             
             if (result instanceof String) {
                 jcrVersion.setProperty(idx.getId(), result);
             }
         } catch (IOException e) {
             throw new RegistryException(e);
         } catch (XPathExpressionException e) {
             throw new RegistryException(e);
         }
         
     }
 
     private void indexWithXQuery(JcrVersion jcrVersion, Index idx) throws RegistryException {
         XQDataSource ds = new SaxonXQDataSource();
         
         try {
             XQConnection conn = ds.getConnection();
             
             XQPreparedExpression ex = conn.prepareExpression(idx.getExpression());
             XmlContentHandler ch = (XmlContentHandler) contentService.getContentHandler(jcrVersion.getParent().getContentType());
             
             ex.bindNode(new QName("document"), ch.getDocument(jcrVersion.getData()), null);
             
             XQResultSequence result = ex.executeQuery();
             
             Node versionNode = jcrVersion.node;
             
             Node property = JcrUtil.getOrCreate(versionNode, idx.getId());
             JcrUtil.removeChildren(property);
             
             List<Object> results = new ArrayList<Object>();
             
             boolean locked = true;
             boolean visible = true;
             
             if (result.next()) {
                 XQItem item = result.getItem();
 
                 org.w3c.dom.Node values = item.getNode();
                 
                 // check locking & visibility
                 NamedNodeMap atts = values.getAttributes();
                 org.w3c.dom.Node lockedNode = atts.getNamedItem("locked");
                 if (lockedNode != null) {
                     locked = BooleanUtils.toBoolean(lockedNode.getNodeValue());
                 }
                 org.w3c.dom.Node visibleNode = atts.getNamedItem("visible");
                 if (visibleNode != null) {
                     visible = BooleanUtils.toBoolean(visibleNode.getNodeValue());
                 }
                 
                 // loop through the values
                 Element value = DOMUtils.getFirstElement(values);
                 while (value != null) {
                     Object content = DOMUtils.getContent(value);
                     
                     LOGGER.info("Adding value " + content + " to index " + idx.getId());
     
                     if (idx.getQueryType().equals(QName.class)) {
                         results.add(QNameUtil.fromString(content.toString())); 
                     } else {
                         results.add(content);
                     }
                     
                     value = (Element) DOMUtils.getNext(value, "value", org.w3c.dom.Node.ELEMENT_NODE);
                 }
             }
             
             jcrVersion.setProperty(idx.getId(), results);
             jcrVersion.setLocked(idx.getId(), locked);
             jcrVersion.setVisible(idx.getId(), visible);
         } catch (Exception e) {
             // TODO: better error handling for frontends
             // We should log this and make the logs retrievable
             // We should also prepare the expressions when the expression is created
             // or on startup
             throw new RegistryException(e);
         }
         
     }
 
     public void initialize() throws Exception {
         Session session = getSessionFactory().getSession();
         Node root = session.getRootNode();
         
         // UGH, Jackrabbit specific code
         javax.jcr.Workspace workspace = session.getWorkspace();
         try {
             workspace.getNamespaceRegistry().getPrefix(NAMESPACE);
         } catch (NamespaceException e) {
             workspace.getNamespaceRegistry().registerNamespace("galaxy", NAMESPACE);
         }
 
         NodeTypeDef[] nodeTypes = NodeTypeReader.read(getClass()
             .getResourceAsStream("/org/mule/galaxy/impl/jcr/nodeTypes.xml"));
 
         // Get the NodeTypeManager from the Workspace.
         // Note that it must be cast from the generic JCR NodeTypeManager to the
         // Jackrabbit-specific implementation.
         NodeTypeManagerImpl ntmgr = (NodeTypeManagerImpl)workspace.getNodeTypeManager();
 
         // Acquire the NodeTypeRegistry
         NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
 
         // Loop through the prepared NodeTypeDefs
         for (NodeTypeDef ntd : nodeTypes) {
             // ...and register it
             if (!ntreg.isRegistered(ntd.getName())) {
                 ntreg.registerNodeType(ntd);
             }
         }
         
         Node workspaces = JcrUtil.getOrCreate(root, "workspaces");
         workspacesId = workspaces.getUUID();
         indexesId = JcrUtil.getOrCreate(root, "indexes").getUUID();
         artifactTypesId = JcrUtil.getOrCreate(root, "artifactTypes").getUUID();
 
         NodeIterator nodes = workspaces.getNodes();
         // ignore the system node
         if (nodes.getSize() == 0) {
             Node node = workspaces.addNode(settings.getDefaultWorkspaceName(),
                                            "galaxy:workspace");
             node.addMixin("mix:referenceable");
 
             JcrWorkspace w = new JcrWorkspace(node);
             w.setName(settings.getDefaultWorkspaceName());
         } 
         
         session.save();
         
         for (ContentHandler ch : contentService.getContentHandlers()) {
             ch.setRegistry(this);
         }
 //        session.logout();
     }
 
     public void addComment(Comment c) {
         commentDao.save(c);
     }
 
     @SuppressWarnings("unchecked")
     public List<Comment> getComments(final Artifact a) {
         return (List) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 return commentDao.find("artifact", a.getId());
             }
         });
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
                         boolean user = JcrUtil.getBooleanOrNull(dep, JcrVersion.USER_SPECIFIED);
                        if (user && ids.contains(dep.getName())) {
                             dep.remove();
                         }
                     }
                     session.save();
                     return null;
                 }
             });
         }
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
 
     public void setPolicyManager(PolicyManager policyManager) {
         this.policyManager = policyManager;
     }
 
     public void setCommentDao(Dao<Comment> commentDao) {
         this.commentDao = commentDao;
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
 
 }
