 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.connection;
 
 import Sirius.navigator.connection.proxy.*;
 
 import org.apache.log4j.*;
 
 import de.cismet.tools.CurrentStackTrace;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class SessionManager {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(SessionManager.class);
     private static SessionManager manager = null;
     private static double nonce = Math.random();
     private static final Object blocker = new Object();
 
     //~ Instance fields --------------------------------------------------------
 
     private ConnectionProxy proxy;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SessionManager object.
      *
      * @param  proxy  DOCUMENT ME!
      */
     private SessionManager(final ConnectionProxy proxy) {
         this.proxy = proxy;
         if (logger.isInfoEnabled()) {
             logger.info("singleton shared instance of SessionManager created"); // NOI18N
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  proxy  DOCUMENT ME!
      */
     public static void init(final ConnectionProxy proxy) {
         if (logger.isDebugEnabled()) {
             logger.debug("init SessionManager " + nonce, new CurrentStackTrace()); // NOI18N
         }
         synchronized (blocker) {
             if (manager == null) {
                 manager = new SessionManager(proxy);
             } else {
                 manager.proxy = proxy;
                 logger.warn("SessionManager has already been initialized");        // NOI18N
                 // throw new RuntimeException("SessionManager has alreadyt been initialized");
             }
         }
     }
 
     /**
      * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isInitialized() {
        synchronized (blocker) {
            return manager != null;
        }
    }

    /**
     * DOCUMENT ME!
      */
     public static void destroy() {
         if (logger.isInfoEnabled()) {
             logger.info("destroy SessionManager" + manager, new CurrentStackTrace()); // NOI18N
         }
         synchronized (blocker) {
             logger.warn("destroying singelton SessionManager instance");              // NOI18N
 //            manager = null;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean isConnected() {
         if (manager == null) {
             return false;
         } else {
             return getManager().proxy.isConnected();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean onClientSide() {
         return isConnected();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  RuntimeException  DOCUMENT ME!
      */
     private static SessionManager getManager() {
         if (manager == null) {
             final Throwable t = new CurrentStackTrace();
             logger.warn("SessionManager has not been initialized", t);                // NOI18N
             throw new RuntimeException("SessionManager has not been initialized", t); // NOI18N
         }
 
         return manager;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Connection getConnection() {
         try {
             return getManager().proxy.getSession().getConnection();
         } catch (Exception e) {
             logger.error("Error in getConnection()\nmaybe the manager is null" + getManager(), e); // NOI18N
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static ConnectionSession getSession() {
         return getManager().proxy.getSession();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static ConnectionProxy getProxy() {
         return getManager().proxy;
     }
 }
