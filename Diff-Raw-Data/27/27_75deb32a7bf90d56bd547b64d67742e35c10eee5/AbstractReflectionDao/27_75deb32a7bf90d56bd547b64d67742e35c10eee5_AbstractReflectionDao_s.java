 package org.mule.galaxy.impl.jcr;
 
 import static org.mule.galaxy.impl.jcr.JcrUtil.getOrCreate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.mule.galaxy.Identifiable;
 
 public abstract class AbstractReflectionDao<T extends Identifiable> extends AbstractDao<T> {
 
     private ClassPersister persister;
     private String rootNode;
     private String objectsNodeId;
     private String idNode;
     private boolean useNodeId;
     private String objectNodeName;
 
     protected AbstractReflectionDao(Class t, String rootNode, boolean useNodeId) throws Exception {
         this(t, rootNode, useNodeId, "id");
     }
     
     protected AbstractReflectionDao(Class t, String rootNode, String idNode) throws Exception {
         this(t, rootNode, false, idNode);
     }
     
     private AbstractReflectionDao(Class t, String rootNode, boolean useNodeId, String idNode) throws Exception {
         this.persister = new ClassPersister(t);
         this.rootNode = rootNode;
         this.idNode = idNode;
         this.useNodeId = useNodeId;
         
         objectNodeName = t.getSimpleName();
         objectNodeName = objectNodeName.substring(0, 1).toLowerCase() + objectNodeName.substring(1);
     }
     
     public void initialize() throws Exception {
         Session session = getSessionFactory().getSession();
         Node root = session.getRootNode();
         
         Node objects = getOrCreate(root, rootNode);
         objectsNodeId = objects.getUUID();
 
         doCreateInitialNodes(session, objects); 
         
         session.save();
 //        ?? session.logout(); 
     }
 
     protected void doCreateInitialNodes(Session session, Node objects) throws RepositoryException {
     }
     
     protected void persist(T o, Node node) throws Exception {
         persister.persist(o, node);
     }
     
     @Override
     protected T doGet(String id, Session session) throws RepositoryException {
         Node node = findNode(id, session);
         
         if (node == null) {
             return null;
         }
         
         try {
             return build(node);
         } catch (Exception e) {
             if (e instanceof RepositoryException) {
                 throw ((RepositoryException) e);
             }
             throw new RuntimeException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     protected T build(Node node) throws Exception {
         return (T) persister.build(node);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected List<T> doListAll(Session session) throws RepositoryException {
         ArrayList<T> objs = new ArrayList<T>();
         for (NodeIterator nodes = getObjectsNode().getNodes(); nodes.hasNext();) {
             Node node = nodes.nextNode();
             
             try {
                 objs.add((T) persister.build(node));
             } catch (Exception e) {
                 // TODO: not sure what to do here
                 if (e instanceof RepositoryException) {
                     throw ((RepositoryException) e);
                 }
                 throw new RuntimeException(e);
             }
         }
         return objs;
     }
 
     protected Node getObjectsNode() {
         return getNodeByUUID(objectsNodeId);
     }
     
     protected void doDelete(String id, Session session) throws RepositoryException {
         Node node = findNode(id, session);
         node.remove();
         session.save();
     }
     
     @Override
     protected void doSave(T t, Session session) throws RepositoryException {
         String id = t.getId();
         Node node = null;
         
         if (id == null) {
             node = getObjectsNode().addNode(getObjectNodeName(t));
             node.addMixin("mix:referenceable");
             id = getId(t, node, session);
             t.setId(id);
         } else {
             findNode(id, session);
         }
         
         if (node == null) throw new NullPointerException("Could not find node");
         
         try {
             persister.persist(t, node);
         } catch (Exception e) {
             if (e instanceof RepositoryException) {
                 throw ((RepositoryException) e);
             }
             throw new RuntimeException(e);
         }
     }
 
     protected String getId(T t, Node node, Session session) throws RepositoryException {
         if (useNodeId) {
             return node.getUUID();
         }
         
         return null;
     }
 
     protected String getObjectNodeName(T t) {
         return objectNodeName;
     }
 
     protected Node findNode(String id, Session session) throws RepositoryException {
         if (useNodeId) {
             return getNodeByUUID(id);
         } 
         
         QueryManager qm = getQueryManager(session);
        Query q = qm.createQuery("/" + rootNode + "/*[@" + idNode + "='" + id + "']", Query.XPATH);
         
         QueryResult qr = q.execute();
         
         NodeIterator nodes = qr.getNodes();
         if (!nodes.hasNext()) {
             return null;
         }
         
         return nodes.nextNode();
     }
 
 }
