 package eu.wisebed.wisedb.controller;
 
 import eu.uberdust.caching.Cachable;
 import eu.uberdust.caching.EvictCache;
 import eu.wisebed.wisedb.model.*;
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.*;
 
 /**
  * CRUD operations for NodeCapabilities entities.
  */
 @SuppressWarnings("unchecked")
 public class NodeCapabilityControllerImpl extends AbstractController<NodeCapability> implements NodeCapabilityController {
 
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static NodeCapabilityController ourInstance = null;
 
     /**
      * Source literal.
      */
     private static final String NODE = "node";
 
     /**
      * Capabilities literal.
      */
     private static final String CAPABILITY = "capability";
 
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(NodeCapabilityControllerImpl.class);
 
     /**
      * Public constructor .
      */
     public NodeCapabilityControllerImpl() {
         // Does nothing
         super();
     }
 
     /**
      * NodeCapabilityController is loaded on the first execution of
      * NodeCapabilityController.getInstance() or the first access to
      * NodeCapabilityController.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static NodeCapabilityController getInstance() {
         synchronized (NodeCapabilityControllerImpl.class) {
             if (ourInstance == null) {
                 ourInstance = new NodeCapabilityControllerImpl();
             }
         }
         return ourInstance;
     }
 
     /**
      * Prepares and inserts a capability to the persistence with the provided capability name.
      *
      * @param capabilityName , a capability name.
      * @return returns the inserted capability instance.
      */
     @EvictCache(cacheName = "eu.wisebed.wisedb.controller.NodeCapabilityControllerImpl.list,eu.wisebed.wisedb.controller.NodeCapabilityControllerImpl.count")
     public NodeCapability prepareInsertNodeCapability(final String capabilityName, final Node node) {
         LOGGER.info("prepareInsertNodeCapability(" + capabilityName + "," + node + ")");
 
         Capability capability = CapabilityControllerImpl.getInstance().getByID(capabilityName);
         if (capability == null) {
             capability = CapabilityControllerImpl.getInstance().prepareInsertCapability(capabilityName);
         }
 
         NodeCapability nodeCapability = new NodeCapability();
 
         nodeCapability.setCapability(capability);
         nodeCapability.setNode(node);
 
         final LastNodeReading lastNodeReading = new LastNodeReading();
        nodeCapability.setLastNodeReading(lastNodeReading);
 
        NodeCapabilityControllerImpl.getInstance().add(nodeCapability);
 
         lastNodeReading.setNodeCapability(nodeCapability);
         lastNodeReading.setTimestamp(new Date());
         lastNodeReading.setReading(null);
         lastNodeReading.setStringReading(null);
         LastNodeReadingControllerImpl.getInstance().add(lastNodeReading);
 
         return nodeCapability;
     }
 
     @Override
     @EvictCache(cacheName = "eu.wisebed.wisedb.controller.NodeCapabilityControllerImpl.list,eu.wisebed.wisedb.controller.NodeCapabilityControllerImpl.count")
     public void delete(int id) {
         LOGGER.info("delete(" + id + ")");
         super.delete(new NodeCapability(), id);
     }
 
     @SuppressWarnings("unchecked")
     @Cachable
     public long count() {
         LOGGER.info("count()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.setProjection(Projections.rowCount());
         return (Long) criteria.list().get(0);
     }
 
     public List<Capability> list() {
         LOGGER.info("list()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         final List<Capability> capabilities = new ArrayList<Capability>();
         for (Object obj : criteria.list()) {
             if (obj instanceof NodeCapability) {
                 final NodeCapability cap = (NodeCapability) obj;
                 final Capability capability = new Capability();
                 capability.setName(cap.getCapability().getName());
                 if (capability != null) {
                     capabilities.add(capability);
                 }
             }
         }
         return capabilities;
     }
 
     public NodeCapability getByID(int entityId) {
         LOGGER.info("getByID(" + entityId + ")");
         return super.getByID(new NodeCapability(), entityId);
 //        final Session session = getSessionFactory().getCurrentSession();
 //        final Criteria criteria = session.createCriteria(NodeCapability.class);
 //        criteria.add(Restrictions.eq(ID, id));
 //        Object obj = criteria.list().get(0);
 //
 //        if (obj instanceof NodeCapability) {
 //
 //            return (NodeCapability) obj;
 //        }
 //
 //        return null;
     }
 
     public NodeCapability getByID(final Node node, final String capabilityName) {
         final Capability capability = CapabilityControllerImpl.getInstance().getByID(capabilityName);
         LOGGER.debug("getByID(" + node.getId() + "," + capabilityName + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.add(Restrictions.eq(NODE, node));
         criteria.add(Restrictions.eq(CAPABILITY, capability));
         return (NodeCapability) criteria.uniqueResult();
     }
 
     public NodeCapability getByID(final Node node, final Capability capability) {
         LOGGER.debug("getByID(" + node.getId() + "," + capability + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.add(Restrictions.eq(NODE, node));
         criteria.add(Restrictions.eq(CAPABILITY, capability));
         return (NodeCapability) criteria.uniqueResult();
     }
 
     @Cachable
     public List<NodeCapability> getByIDs(final List<Node> nodes, final Capability capability) {
         LOGGER.debug("getByIDs(" + "," + capability + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.add(Restrictions.in(NODE, nodes));
         criteria.add(Restrictions.eq(CAPABILITY, capability));
         return criteria.list();
     }
 
     public boolean isAssociated(final Node node, final Capability capability) {
         LOGGER.debug("isAssociated(" + node.getId() + "," + capability + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.add(Restrictions.eq(NODE, node));
         criteria.add(Restrictions.eq(CAPABILITY, capability));
         criteria.setProjection(Projections.rowCount());
         try {
             if ((Long) criteria.uniqueResult() > 0) {
                 return true;
             }
         } catch (java.lang.ClassCastException ex) {
             if ((Integer) criteria.uniqueResult() > 0) {
                 return true;
             }
         }
         return false;
 
     }
 
     @Cachable
     public List<NodeCapability> list(final Node node) {
         LOGGER.debug("list(" + node.getId() + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.add(Restrictions.eq(NODE, node));
         NodeCapability filter = null;
         final List<NodeCapability> capabilities = new ArrayList<NodeCapability>();
         for (Object obj : criteria.list()) {
             if (obj instanceof NodeCapability) {
                 capabilities.add((NodeCapability) obj);
                 if (((NodeCapability) obj).getCapability().getName().equals("filter")) {
                     filter = (NodeCapability) obj;
                 }
             }
         }
 
 
         Map<String, NodeCapability> virtualCapabilities = new HashMap<String, NodeCapability>();
         if (node.getName().contains("virtual")) {
             Set<String> mySet = new HashSet<String>();
             if ((filter != null) && (!filter.equals(""))) {
                 for (String part : filter.getLastNodeReading().getStringReading().split(",")) {
                     mySet.add(part);
                 }
             }
 
             List<Link> links = LinkControllerImpl.getInstance().getBySource(node);
 
             for (Link link : links) {
                 final LinkCapability lcap = LinkCapabilityControllerImpl.getInstance().getByID(link, "virtual");
                 if (lcap != null && lcap.getLastLinkReading().getReading() == 1.0) {
                     List<NodeCapability> caps = list(link.getTarget());
                     for (NodeCapability cap : caps) {
                         if (!mySet.isEmpty() && !mySet.contains(cap.getCapability().getName())) continue;
 //                        boolean isContained = false;
 //                        for (String myPart : myParts) {
 //                            if (cap.getCapability().getName().contains(myPart)) {
 //                                isContained = true;
 //                                break;
 //                            }
 //                        }
 //                        if (!isContained) {
 //                            continue;
 //                        }
 
                         if (!virtualCapabilities.containsKey(cap.getCapability().getName())) {
                             NodeCapability vncap = new NodeCapability();
                             vncap.setCapability(cap.getCapability());
                             vncap.setLastNodeReading(cap.getLastNodeReading());
                             vncap.setNode(node);
                             virtualCapabilities.put(cap.getCapability().getName(), vncap);
                         } else {
                             final NodeCapability vncap = virtualCapabilities.get(cap.getCapability().getName());
                             if (vncap.getLastNodeReading().getTimestamp().after(cap.getLastNodeReading().getTimestamp())) {
                                 vncap.setLastNodeReading(cap.getLastNodeReading());
                             }
                         }
                     }
                 }
             }
 
         }
         for (String s : virtualCapabilities.keySet()) {
             capabilities.add(virtualCapabilities.get(s));
         }
         return capabilities;
     }
 
     public List<NodeCapability> list(final Setup setup) {
         LOGGER.debug("list(" + setup + ")");
         final List<Node> nodes = NodeControllerImpl.getInstance().list(setup);
         if (nodes.size() > 0) {
             final Session session = getSessionFactory().getCurrentSession();
             final Criteria criteria = session.createCriteria(NodeCapability.class);
             criteria.add(Restrictions.in(NODE, nodes));
             criteria.addOrder(Order.asc(NODE));
             return (List<NodeCapability>) criteria.list();
         }
         return null;
     }
 
     @Cachable
     public List<NodeCapability> list(final Setup setup, final Capability capability) {
         LOGGER.debug("list(" + setup + "," + capability + ")");
         final List<Node> nodes = NodeControllerImpl.getInstance().list(setup);
         final List<NodeCapability> capabilities = new ArrayList<NodeCapability>();
 
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(NodeCapability.class);
         criteria.add(Restrictions.in(NODE, nodes));
         criteria.add(Restrictions.eq(CAPABILITY, capability));
         for (Object obj : criteria.list()) {
             if (obj instanceof NodeCapability) {
                 capabilities.add((NodeCapability) obj);
             }
         }
 
         return capabilities;
     }
 
 
 }
