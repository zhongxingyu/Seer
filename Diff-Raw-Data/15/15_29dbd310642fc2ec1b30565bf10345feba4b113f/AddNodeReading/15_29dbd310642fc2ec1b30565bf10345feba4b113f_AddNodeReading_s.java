 package eu.wisebed.wisedb.test;
 
 import eu.wisebed.wisedb.HibernateUtil;
 import eu.wisebed.wisedb.controller.NodeReadingControllerImpl;
 import org.apache.log4j.Logger;
 import org.hibernate.Transaction;
 
 import java.util.Date;
 
 /**
  * Adds a node reading
  */
 public class AddNodeReading {
 
     /**
      * a log4j logger to print messages.
      */
     private static final Logger LOGGER = org.apache.log4j.Logger.getLogger(AddNodeReading.class);
 
     public static void main(String args[]) {
 
         // Initialize hibernate
         HibernateUtil.connectEntityManagers();
         Transaction tx = HibernateUtil.getInstance().getSession().beginTransaction();
         try {
 
             // a valid urnPrefix for the testbed
 
 
             // a node id for the testbed
            final String nodeId = "urn:qopbot:door";
 
             // get that nodes capability name
            final String capabilityName = "urn:node:capability:status";
 
             // reading value
             final double readingValue = 0;
             // string reading value
             final String stringReading = null;
 
             // Occured time
             final Date timestamp = new Date();
 
             LOGGER.info("Node : " + nodeId);
             LOGGER.info("Capability : " + capabilityName);
             LOGGER.info("Reading : " + readingValue);
             LOGGER.info("StringReading : " + stringReading);
             LOGGER.info("Timestamp : " + timestamp.toString());
 
             // insert reading
             NodeReadingControllerImpl.getInstance().insertReading(nodeId, capabilityName, readingValue, stringReading, new Date(0));
 
             tx.commit();
         } catch (Exception e) {
             tx.rollback();
             LOGGER.fatal(e);
             e.printStackTrace();
             System.exit(-1);
         } finally {
             // always close session
             HibernateUtil.getInstance().closeSession();
         }
     }
 }
