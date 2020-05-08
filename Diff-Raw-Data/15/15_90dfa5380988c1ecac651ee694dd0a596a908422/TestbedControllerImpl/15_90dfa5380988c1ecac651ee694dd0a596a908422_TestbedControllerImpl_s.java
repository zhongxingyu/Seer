 package eu.wisebed.wisedb.controller;
 
 import eu.wisebed.wisedb.model.Link;
 import eu.wisebed.wisedb.model.Node;
 import eu.wisebed.wisedb.model.Setup;
 import eu.wisebed.wisedb.model.Testbed;
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.ProjectionList;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * CRUD operations for Testbed entities.
  */
 public class TestbedControllerImpl extends AbstractController<Testbed> implements TestbedController {
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static TestbedController ourInstance = null;
 
     /**
      * UrnPrefix literal.
      */
     private static final String URN_PREFIX = "urnPrefix";
 
     /**
      * Name literal.
      */
     private static final String NAME = "name";
     /**
      * Setup literal.
      */
     private static final String SETUP = "setup";
 
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(TestbedControllerImpl.class);
 
 
     /**
      * Public constructor .
      */
     public TestbedControllerImpl() {
         // Does nothing
         super();
     }
 
     /**
      * TestbedController is loaded on the first execution of
      * TestbedController.getInstance() or the first access to
      * TestbedController.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static TestbedController getInstance() {
         synchronized (TestbedControllerImpl.class) {
             if (ourInstance == null) {
                 ourInstance = (TestbedController) new TestbedControllerImpl();
             }
         }
         return ourInstance;
     }
 
     /**
      * Delete the input Testbed from the database.
      *
      * @param id the Testbed id that we want to delete
      */
     public void delete(final int id) {
         LOGGER.info("delete(" + id + ")");
 //        super.delete(new Testbed(), id);
         final Session session = getSessionFactory().getCurrentSession();
         final Object entity2 = session.load(Testbed.class, id);
         session.delete(entity2);
     }
 
     /**
      * Listing all the Testbeds from the database.
      *
      * @return a list of all the entries that exist inside the table Testbed.
      */
     public List<Testbed> list() {
         LOGGER.info("list()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(Testbed.class);
         List testbeds = criteria.list();
         LOGGER.info("returning " + testbeds.size() + " testbeds");
         return (List<Testbed>) testbeds;
     }
 
     /**
      * get the Testbed from the database that corresponds to the input id.
      *
      * @param entityID the id of the Entity object.
      * @return an Entity object.
      */
     public Testbed getByID(final int entityID) {
         LOGGER.info("getByID(" + entityID + ")");
 //        return super.getByID(new Testbed(), entityID);
         final Session session = getSessionFactory().getCurrentSession();
         return (Testbed) session.get(Testbed.class, entityID);
     }
 
     /**
      * Return the Testbed from the database that has the given urn prefix.
      *
      * @param urnPrefix the Urn prefix of the Testbed object.
      * @return a Testbed object.
      */
     public Testbed getByUrnPrefix(final String urnPrefix) {
         LOGGER.info("getByUrnPrefix(" + urnPrefix + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(Testbed.class);
         criteria.add(Restrictions.like(URN_PREFIX, urnPrefix, MatchMode.START));
         criteria.addOrder(Order.asc(URN_PREFIX));
         criteria.setMaxResults(1);
         return (Testbed) criteria.uniqueResult();
     }
 
 
     /**
      * Return the Testbed from the database that has the given urn prefix.
      *
      * @param testbedName the name of a Testbed object.
      * @return a Testbed object.
      */
     public Testbed getByName(final String testbedName) {
         LOGGER.info("getByName(" + testbedName + ")");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(Testbed.class);
         criteria.add(Restrictions.eq(NAME, testbedName));
         Object obj = criteria.uniqueResult();
         return (Testbed) obj;
     }
 
     /**
      * Returns the number of nodes in database for each setup
      *
      * @return map containing the setups and node count
      */
     public Map<String, Long> countNodes() {
         LOGGER.info("countNodes()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(Node.class);
         final ProjectionList projList = Projections.projectionList();
         projList.add(Projections.groupProperty(SETUP));
         projList.add(Projections.rowCount());
         criteria.setProjection(projList);
 
         final List results = criteria.list();
         final Iterator iter = results.iterator();
         if (!iter.hasNext()) {
             LOGGER.debug("No objects to display.");
             return null;
         }
         final Map<String, Long> resultsMap = new HashMap<String, Long>();
         while (iter.hasNext()) {
 
             final Object[] obj = (Object[]) iter.next();
             final Setup setup = (Setup) obj[0];
            final long count = Long.parseLong(String.valueOf((Integer) obj[1]));
             resultsMap.put(setup.getTestbed().getName(), count);
 
         }
 
         return resultsMap;
     }
 
     /**
      * Returns the number of links in database for each setup
      *
      * @return map containing the setups and link count
      */
     public Map<String, Long> countLinks() {
         LOGGER.info("countLinks()");
         final Session session = getSessionFactory().getCurrentSession();
         final Criteria criteria = session.createCriteria(Link.class);
         ProjectionList projList = Projections.projectionList();
         projList.add(Projections.groupProperty(SETUP));
         projList.add(Projections.rowCount());
         criteria.setProjection(projList);
 
         final List results = criteria.list();
         final Iterator iter = results.iterator();
         if (!iter.hasNext()) {
             LOGGER.debug("No objects to display.");
             return null;
         }
         final Map<String, Long> resultsMap = new HashMap<String, Long>();
         while (iter.hasNext()) {
 
             final Object[] obj = (Object[]) iter.next();
             final Setup setup = (Setup) obj[0];
            final long count = Long.parseLong(String.valueOf((Integer) obj[1]));
             resultsMap.put(setup.getTestbed().getName(), count);
 
         }
 
         return resultsMap;
     }
 
 }
