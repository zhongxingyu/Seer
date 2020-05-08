 package org.mule.galaxy.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jcr.AccessDeniedException;
 import javax.jcr.InvalidItemStateException;
 import javax.jcr.ItemExistsException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
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
 import net.sf.saxon.javax.xml.xquery.XQException;
 import net.sf.saxon.javax.xml.xquery.XQItem;
 import net.sf.saxon.javax.xml.xquery.XQPreparedExpression;
 import net.sf.saxon.javax.xml.xquery.XQResultSequence;
 import net.sf.saxon.xqj.SaxonXQDataSource;
 import org.apache.commons.lang.BooleanUtils;
 import org.mule.galaxy.ActivityManager;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.ContentService;
 import org.mule.galaxy.GalaxyException;
 import org.mule.galaxy.Index;
 import org.mule.galaxy.IndexManager;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.XmlContentHandler;
 import org.mule.galaxy.ActivityManager.EventType;
 import org.mule.galaxy.impl.jcr.JcrUtil;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.util.DOMUtils;
 import org.mule.galaxy.util.LogUtils;
 import org.mule.galaxy.util.QNameUtil;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.SessionFactory;
 import org.springmodules.jcr.SessionFactoryUtils;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 
 public class IndexManagerImpl extends AbstractReflectionDao<Index> 
     implements IndexManager, ApplicationContextAware {
     private Logger LOGGER = LogUtils.getL7dLogger(IndexManagerImpl.class);
 
     private ContentService contentService;
 
     private XPathFactory factory = XPathFactory.newInstance();
 
     private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
     
     private ThreadPoolExecutor executor;
 
     private Registry registry;
 
     private ApplicationContext context;
     
     private ActivityManager activityManager;
     
     private boolean destroyed = false;
     
     public IndexManagerImpl() throws Exception {
         super(Index.class, "indexes", false);
     }
 
     @Override
     public void save(Index t) {
         save(t, false);
     }
 
     public void save(Index t, boolean block) {
         super.save(t);
         
         if (block) {
             getIndexer(t).run();
         } else {
             reindex(t);
         }
     }
     @SuppressWarnings("unchecked")
     public Collection<Index> getIndexes() {
         return listAll();
     }
 
     @Override
     protected String getObjectNodeName(Index t) {
         return t.getId();
     }
 
     @Override
     public Index build(Node node, Session session) throws Exception {
         Index i = super.build(node, session);
         i.setId(node.getName());
         return i;
     }
 
     protected Node findNode(String id, Session session) throws RepositoryException {
         try {
             return getObjectsNode(session).getNode(id);
         } catch (PathNotFoundException e) {
             return null;
         }
     }
     
     @Override
     protected String getNodeType() {
         return "galaxy:index";
     }
 
     @SuppressWarnings("unchecked")
     public Set<Index> getIndices(final QName documentType) {
         return (Set<Index>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 QueryManager qm = getQueryManager(session);
                 Query query = qm.createQuery("//element(*, galaxy:index)[@documentTypes=" 
                                                  + JcrUtil.stringToXPathLiteral(documentType.toString()) + "]", 
                                              Query.XPATH);
                 
                 QueryResult result = query.execute();
                 
                 Set<Index> indices = new HashSet<Index>();
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node node = nodes.nextNode();
 //                    JcrUtil.dump(node);
                     try {
                         indices.add(build(node, session));
                     } catch (Exception e) {
                         throw new RuntimeException(e);
                     }
                 }
                 return indices;
             }
         });
     }
 
     public Index getIndex(final String id) throws NotFoundException {
         Index i = get(id);
         if (i == null) {
             throw new NotFoundException(id);
         }
         return i;
     }
 
     
     @Override
     public void initialize() throws Exception {
         super.initialize();
         
         executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, queue);
         executor.prestartAllCoreThreads();
     }
 
     public void destroy() throws Exception {
         LOGGER.log(Level.FINE, "Starting IndexManager.destroy() with " + executor.getQueue().size() + " indexing jobs left");
         if (destroyed) return;
         
         executor.shutdown();
         destroyed = true;
 
         executor.awaitTermination(10, TimeUnit.SECONDS);
         
         // TODO finish reindexing on startup?
         List<Runnable> tasks = executor.shutdownNow();
         
         if (tasks.size() > 0) {
             LOGGER.warning("Could not shut down indexer! Indexing was still going.");
         }
     }
 
     private void reindex(final Index idx) {
         Runnable runnable = getIndexer(idx);
         
         if (!queue.add(runnable)) handleIndexingException(new Exception("Could not add indexer to queue."));
     }
 
     private Runnable getIndexer(final Index idx) {
         Runnable runnable = new Runnable() {
 
             public void run() {
                 Session session = null;
                 boolean participate = false;
                 SessionFactory sf = getSessionFactory();
                 if (TransactionSynchronizationManager.hasResource(sf)) {
                     // Do not modify the Session: just set the participate
                     // flag.
                     participate = true;
                 } else {
                     logger.debug("Opening reeindexing session");
                     session = SessionFactoryUtils.getSession(sf, true);
                     TransactionSynchronizationManager.bindResource(sf, sf.getSessionHolder(session));
                 }
 
                 try {
                     
                     findAndReindex(session, idx);
                 } catch (RepositoryException e) {
                     handleIndexingException(e);
                 } finally {
                     if (!participate) {
                         TransactionSynchronizationManager.unbindResource(sf);
                         logger.debug("Closing reindexing session");
                         SessionFactoryUtils.releaseSession(session, sf);
                     }
                 }
             }
         };
         return runnable;
     }
 
     protected void findAndReindex(Session session, Index idx) throws RepositoryException {
         org.mule.galaxy.query.Query q = new org.mule.galaxy.query.Query(Artifact.class)
             .add(Restriction.in("documentType", idx.getDocumentTypes()));
         
         try {
             Set results = getRegistry().search(q);
             
             logActivity("Reindexing " + idx.getId() + " for " + results.size() + " artifacts.");
             
             for (Object o : results) {
                 Artifact a = (Artifact) o;
                 
                 for (ArtifactVersion v : a.getVersions()) {
                     index(v, idx);
                 }
                 
                 session.save();
             }
         } catch (QueryException e) {
             logActivity("Could not reindex documents for index " + idx.getId(), e);
         } catch (RegistryException e) {
             logActivity("Could not reindex documents for index " + idx.getId(), e);
         }
         
     }
 
 
     private void logActivity(String activity, Exception e) {
         LOGGER.log(Level.SEVERE, activity, e);
         activityManager.logActivity(activity, EventType.ERROR);
     }
 
     private void logActivity(String activity) {
         LOGGER.log(Level.FINE, activity);
         activityManager.logActivity(activity, EventType.INFO);
     }
 
     protected void handleIndexingException(Throwable t) {
         activityManager.logActivity("Could not reindex documents: " + t.getMessage(), EventType.ERROR);
         LOGGER.log(Level.SEVERE, "Could not index documents.", t);
     }
 
     private void handleIndexingException(Index idx, Throwable t) {
         activityManager.logActivity("Could not process index " + idx.getId() + ": " + t.getMessage(), EventType.ERROR);
         LOGGER.log(Level.SEVERE, "Could not process index " + idx.getId(), t);
     }
     
     public void index(final ArtifactVersion version) {
         QName dt = version.getParent().getDocumentType();
         if (dt == null) return;
         
         Collection<Index> indices = getIndices(dt);
         
         for (Index idx : indices) {
             index(version, idx);
         }
     }
 
     private void index(final ArtifactVersion version, Index idx) {
         try {
             switch (idx.getLanguage()) {
             case XQUERY:
                 indexWithXQuery(version, idx);
                 break;
             case XPATH:
                 indexWithXPath(version, idx);
                 break;
             default:
                 throw new UnsupportedOperationException();
             }
         } catch (Throwable t) {
             handleIndexingException(idx, t);
         }
     }
 
 
     private void indexWithXPath(ArtifactVersion jcrVersion, Index idx) throws IOException, XPathExpressionException, PropertyException {
         XmlContentHandler ch = (XmlContentHandler) contentService.getContentHandler(jcrVersion.getParent().getContentType());
         
         Document document = ch.getDocument(jcrVersion.getData());
         
         XPath xpath = factory.newXPath();
         XPathExpression expr = xpath.compile(idx.getExpression());
 
         Object result = expr.evaluate(document, XPathConstants.STRING);
         
         if (result instanceof String) {
             jcrVersion.setProperty(idx.getId(), result);
             jcrVersion.setLocked(idx.getId(), true);
         }
         
     }
 
    private void indexWithXQuery(ArtifactVersion jcrVersion, Index idx) throws XQException, IOException, PropertyException {
         
         XQDataSource ds = new SaxonXQDataSource();
         
             
         XQConnection conn = ds.getConnection();
         
         XQPreparedExpression ex = conn.prepareExpression(idx.getExpression());
         XmlContentHandler ch = (XmlContentHandler) contentService.getContentHandler(jcrVersion.getParent().getContentType());
         
         ex.bindNode(new QName("document"), ch.getDocument(jcrVersion.getData()), null);
         
         XQResultSequence result = ex.executeQuery();
         
         List<Object> results = new ArrayList<Object>();
         
         boolean visible = true;
         
         if (result.next()) {
             XQItem item = result.getItem();
 
             org.w3c.dom.Node values = item.getNode();
             
             // check locking & visibility
             NamedNodeMap atts = values.getAttributes();
             org.w3c.dom.Node visibleNode = atts.getNamedItem("visible");
             if (visibleNode != null) {
                 visible = BooleanUtils.toBoolean(visibleNode.getNodeValue());
             }
             
             // loop through the values
             Element value = DOMUtils.getFirstElement(values);
             while (value != null) {
                 Object content = DOMUtils.getContent(value);
                 if (idx.getQueryType().equals(QName.class)) {
                     results.add(QNameUtil.fromString(content.toString())); 
                 } else {
                     results.add(content);
                 }
                 
                 value = (Element) DOMUtils.getNext(value, "value", org.w3c.dom.Node.ELEMENT_NODE);
             }
         }
 
         jcrVersion.setProperty(idx.getId(), results);
         jcrVersion.setLocked(idx.getId(), true);
         jcrVersion.setVisible(idx.getId(), visible);
     
     }
 
     public void setContentService(ContentService contentService) {
         this.contentService = contentService;
     }
 
     private Registry getRegistry() {
         if (registry == null) {
             registry = (Registry) context.getBean("registry");
         }
         return registry;
     }
     
     public void setApplicationContext(ApplicationContext ctx) throws BeansException {
         this.context = ctx;
     }
 
     public void setActivityManager(ActivityManager activityManager) {
         this.activityManager = activityManager;
     }
 
 
 }
