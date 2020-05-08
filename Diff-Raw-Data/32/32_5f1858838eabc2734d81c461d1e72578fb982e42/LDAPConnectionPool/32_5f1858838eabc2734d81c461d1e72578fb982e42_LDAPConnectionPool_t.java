 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LDAPConnectionPool.java,v 1.3 2007-01-12 00:49:06 goodearth Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.common;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.Collections;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPException;
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.shared.debug.Debug;
 
 /**
  * Class to maintain a pool of individual connections to the
  * same server. Specify the initial size and the max size
  * when constructing a pool. Call getConnection() to obtain
  * a connection from the pool and close() to return it. If
  * the pool is fully extended and there are no free connections,
  * getConnection() blocks until a connection has been returned
  * to the pool.<BR>
  * Call destroy() to release all connections.
  *<BR><BR>Example:<BR>
  *<PRE>
  * ConnectionPool pool = null;
  * try {
  *     pool = new ConnectionPool("test", 10, 30,
  *                                "foo.acme.com",389,
  *                                "uid=me, o=acme.com",
  *                                "password" );
  * } catch ( LDAPException e ) {
  *    System.err.println( "Unable to create connection pool" );
  *    System.exit( 1 );
  * }
  * while ( clientsKnocking ) {
  *     String filter = getSearchFilter();
  *     LDAPConnection ld = pool.getConnection();
  *     try {
  *         LDAPSearchResults res = ld.search( BASE, ld.SCOPE_SUB,
  *                                            filter, attrs,
  *                                            false );
  *         pool.close( ld );
  *         while( res.hasMoreElements() ) {
  *             ...
  *</PRE>
  */
 
 /**
  * Connection pool, typically used by a server to avoid creating
  * a new connection for each client
  **/
 public class LDAPConnectionPool {
  
     /**
      * Constructor for specifying all parameters
      *
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @param authdn DN to authenticate as
      * @param authpw password for authentication
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port,
         String authdn,
         String authpw
     ) throws LDAPException {
         this(name, min, max, host, port, authdn, authpw, null);
     }
 
     /**
      * Constructor for specifying all parameters, anonymous
      * identity
      *
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port
     ) throws LDAPException {
         this(name, min, max, host, port, "", ""); 
     }
 
     /**
      * Constructor for using default parameters, anonymous identity
      *
      * @param name name of connection pool
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(String name, String host, int port)
         throws LDAPException
     {
         // poolsize=10,max=20,host,port,
         // noauth,nopswd
         this(name, 10, 20, host, port, "", "");
     }
 
     /** 
      * Constructor for using an existing connection to clone
      * from.
      * <P>
      * The connection to clone must be already established and
      * the user authenticated.
      * 
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param ldc connection to clone 
      * @exception LDAPException on failure to create connections 
      */ 
     public LDAPConnectionPool(
         String name,
         int min,
         int max,
         LDAPConnection ldc
     ) throws LDAPException {
         this(name, min, max, ldc.getHost(), ldc.getPort(),
               ldc.getAuthenticationDN(), ldc.getAuthenticationPassword(),
               (LDAPConnection)ldc.clone());
     }
 
     /* 
      * Constructor for using an existing connection to clone
      * from
      * 
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @param authdn DN to authenticate as
      * @param authpw password for authentication
      * @param ldc connection to clone 
      * @exception LDAPException on failure to create connections 
      */ 
     private LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port,
         String authdn,
         String authpw,
         LDAPConnection ldc
     ) throws LDAPException {
         this(name, min, max, host, port,
              authdn, authpw, ldc, getIdleTime(name));
     }
 
     private static final int getIdleTime(String poolName) {
         String idleStr =
             SystemProperties.get(Constants.LDAP_CONN_IDLE_TIME_IN_SECS);
         int idleTimeInSecs = 0;
         if (idleStr != null && idleStr.length() > 0) {
             try {
                 idleTimeInSecs = Integer.parseInt(idleStr);
             } catch(NumberFormatException nex) {
                 debug.error("Connection pool: " + poolName +
                             ": Cannot parse idle time: " + idleStr +
                             " Connection reaping is disabled.");
             }
         }
         return idleTimeInSecs;
     }
 
 
     /**
      * Most generic constructor which initializes all variables.
      */
     private LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port,
         String authdn,
         String authpw,
         LDAPConnection ldc,
         int idleTimeInSecs
     ) throws LDAPException {
         this.name = name;
         this.minSize = min;
         this.maxSize  = max;
         this.host = host;
         this.port = port;
         this.authdn = authdn;
         this.authpw = authpw;
         this.ldc = ldc;
         this.idleTime = idleTimeInSecs * 1000;
         this.stayAlive = true;
         this.defunct = false;
 
         createPool();
         if (debug.messageEnabled()) {
             debug.message("Connection pool: " + name +
                           ": successfully created: Min:" + minSize +
                           " Max:" + maxSize + " Idle time:" + idleTimeInSecs);
         }
         createIdleCleanupThread();
     }
 
     /**
      * Destroy the whole pool - called during a shutdown
      */
     public void destroy() {
         stayAlive = false;
        // if idle timeout property is not set, cleanupThread will be
        // null. null checked here to avoid NPE.
        if (cleanupThread != null) {
            cleanupThread.interrupt();
            while (cleanupThread.isAlive()) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException iex) {}
            }
         }
         destroyPool(pool);
     }
 
     /**
      * Gets a connection from the pool
      *
      * If no connections are available, the pool will be
      * extended if the number of connections is less than
      * the maximum; if the pool cannot be extended, the method
      * blocks until a free connection becomes available.
      *
      * @return an active connection.
      */
     public LDAPConnection getConnection() {
         return getConnection(0);
     }
 
     /**
      * Gets a connection from the pool within a time limit.
      *
      * If no connections are available, the pool will be
      * extended if the number of connections is less than
      * the maximum; if the pool cannot be extended, the method
      * blocks until a free connection becomes available or the
      * time limit is exceeded. 
      *
      * @param timeout timeout in milliseconds
      * @return an active connection or <CODE>null</CODE> if timed out. 
      */
     public synchronized LDAPConnection getConnection(int timeout) {
         LDAPConnection con = null;
         long waitTime = 0;
         while ((con = getConnFromPool()) == null ) {
             long t0 = System.currentTimeMillis();
 
             if (timeout < 0) {
                 break;
             }
 
             synchronized (pool) {
                 try {
                     if (defunct) return con;
                     pool.wait(timeout);
                 } catch (InterruptedException e) {
                     return null;
                 }
             }
 
             waitTime += System.currentTimeMillis() - t0;
             timeout -= (timeout > 0) ? waitTime : 0;
         }
         return con;
     }
 
     /**
      * Gets a connection from the pool
      *
      * If no connections are available, the pool will be
      * extended if the number of connections is less than
      * the maximum; if the pool cannot be extended, the method
      * returns null.
      *
      * @return an active connection or null.
      */
     protected LDAPConnection getConnFromPool() {
         LDAPConnection con = null;
         LDAPConnectionObject ldapconnobj = null;
 
         // Get an available connection
         for (int i = 0; i < ((pool.size()< maxSize)?pool.size():maxSize); ++i) {
             // Get the ConnectionObject from the pool
             LDAPConnectionObject co = (LDAPConnectionObject)pool.get(i);
             synchronized (co) {
                 if (co.isAvailable()) {  // Conn available?
                     ldapconnobj = co;
                     co.setInUse(true);
                     break;
                 }
             }
         }
 
         if ((ldapconnobj == null) && (pool.size() < maxSize)) {
 	    /*
              * If there there were no conns in pool, can we grow
              * the pool?
              */
             synchronized (pool) {
 
                 if ((maxSize < 0) || ((maxSize > 0) &&
                                       (pool.size() < maxSize))) {
         
                     // Yes we can grow it
                     ldapconnobj = addConnection();
         
                     // If a new connection was created, use it
                     if (ldapconnobj != null) {
                         ldapconnobj.setInUse(true);
                         pool.add(ldapconnobj);
                     }
                 } else {
                     debug.message("Connection pool:" + name +
                                   ":All pool connections in use");
                 }
             }
         }
 
         if (ldapconnobj != null) {
             con = ldapconnobj.getLDAPConn();
         }
         return con;
     }
 
     /**
      * This is our soft close - all we do is mark
      * the connection as available for others to use.
      * We also reset the auth credentials in case
      * they were changed by the caller.
      *
      * @param ld a connection to return to the pool
      */
     public void close (LDAPConnection ld) {
         if (find(deprecatedPool, ld) != -1) {
             removeFromPool(deprecatedPool, ld);
         } else {
             removeFromPool(pool, ld);
         }
     }
 
     private void removeFromPool(ArrayList thePool, LDAPConnection ld) {
         int index = find(thePool, ld);
         if (index != -1) {
             LDAPConnectionObject co = 
                 (LDAPConnectionObject)thePool.get(index);
 
             co.setInUse (false);  // Mark as available
             synchronized (thePool) {
                 thePool.notify();
             }
         }
     }
   
     private void disconnect(LDAPConnectionObject ldapconnObject) {
         if (ldapconnObject != null) {
             if (ldapconnObject.isAvailable()) {
                 ldapconnObject.setAsDestroyed();
                 LDAPConnection ld = ldapconnObject.getLDAPConn();
                 if ( (ld != null) && (ld.isConnected()) ) {
                     try {
                         ld.disconnect();
                     } catch (LDAPException e) {
                         debug.error("Connection pool:" + name +
                                     ":Error during disconnect.", e);
                     }
                  }
                  ldapconnObject.setLDAPConn(null); // Clear conn
             }
         }
     }
  
     private void createPool() throws LDAPException {
         // Called by the constructors
         if (minSize <= 0) {
             throw new LDAPException("Connection pool:" + name +
                                     ":ConnectionPoolSize invalid");
         }
         if (maxSize < minSize) {
             debug.error("Connection pool:" + name +
                         ":ConnectionPoolMax is invalid, set to " +
                         minSize);
             maxSize = minSize;
         }
 
         if (debug.messageEnabled()) {
             StringBuffer buf = new StringBuffer();
             buf.append("");
             buf.append("New Connection pool name =" + name);
             buf.append(" LDAP host =").append(host);
             buf.append(" Port =").append(port);
             buf.append(" Min =").append(minSize);
             buf.append(" Max =").append(maxSize);
         }
 
         // To avoid resizing we set the size to twice the max pool size.
         pool = new ArrayList(maxSize * 2); 
         deprecatedPool = new ArrayList(maxSize * 2);
         setUpPool (minSize); // Initialize it
     }
 
     private LDAPConnectionObject addConnection() {
         LDAPConnectionObject ldapconnobj = null;
 
         if (defunct) {
             debug.error("Connection pool:" + name +
                           ":Defunct connection pool object.  " +
                           "Cannot add connections.");
             return ldapconnobj;
         }
         try {
             ldapconnobj = createConnection();
         } catch (Exception ex) {
             debug.error("Connection pool:" + name +
                         ":Error while adding a connection.", ex);
         }
         if (ldapconnobj != null) {
             debug.message("Connection pool:" + name +
                           ":adding a connection to pool...");
         }
         return ldapconnobj;
     }
   
     private void setUpPool (int size) throws LDAPException {
         synchronized (pool) {
             // Loop on creating connections
             while (pool.size() < size) {
                 pool.add(createConnection());
             }
         }
     }
 
     private LDAPConnectionObject createConnection() throws LDAPException {
         LDAPConnectionObject co = new LDAPConnectionObject();
         // Make LDAP connection, using template if available
         LDAPConnection newConn =
             (ldc != null) ? (LDAPConnection)ldc.clone() :
             new LDAPConnection();
         co.setLDAPConn(newConn);
         try {
             if (newConn.isConnected()) {
                 /*
                  * If using a template, then reconnect
                  * to create a separate physical connection
                  */
                 newConn.reconnect();
             } else {
                 /*
                  * Not using a template, so connect with
                  * simple authentication using ldap v3
                  */
                 try { 
                     newConn.connect (3, host, port, authdn, authpw); 
                 } catch (LDAPException connEx) {
                     // fallback to ldap v2 if v3 is not supported
                     if (connEx.getLDAPResultCode() ==
                         LDAPException.PROTOCOL_ERROR)
                     {
                         newConn.connect (2, host, port, authdn, authpw); 
                     }
                     else {
                         throw connEx;
                     }
                 }
             }
         } catch (LDAPException le) {
             debug.error("Connection pool:" + "Error while Creating pool.", le);
             throw le;
         }
         co.setInUse (false); // Mark not in use
         return co;
     }
 
     private int find(ArrayList list, LDAPConnection con ) {
         // Find the matching Connection in the pool
         if (con != null) {
             for (int i = 0; i < list.size(); i++) {
                 LDAPConnectionObject co = 
                     (LDAPConnectionObject)list.get(i);
                 if (((Object)co.getLDAPConn()).equals(con)) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /**
      * Wrapper for LDAPConnection object in pool
      */
     class LDAPConnectionObject implements java.lang.Comparable {
         LDAPConnectionObject() {
             inUse = false;
             destroyed = false;
         }
 
         /**
          * Returns the associated LDAPConnection.
          *
          * @return the LDAPConnection.
          * 
          */
         LDAPConnection getLDAPConn() {
             return !destroyed ? this.ld : null;
         }
 
         /**
          * Sets the associated LDAPConnection
          *
          * @param ld the LDAPConnection
          * 
          */
         void setLDAPConn (LDAPConnection ld) {
             this.ld = ld;
         }
 
         /**
          * Marks a connection in use or available
          *
          * @param inUse <code>true</code> to mark in use, <code>false</code>
          * if available
          * 
          */
         void setInUse (boolean inUse) {
             this.inUse = inUse;
             if (inUse) {
                 expirationTime = Long.MAX_VALUE;
             } else {
                 expirationTime = System.currentTimeMillis() + idleTime;
             }
         }
 
         /**
          * Used by comparator to sort before cleanup.
          */
         public synchronized int compareTo (Object l) {
             return (int)
                 (((LDAPConnectionObject)l).expirationTime-this.expirationTime);
         }
 
         /**
          * Method called by purge thread to check
          * if this connection can be reaped.
          *
          * @param currTime given current time, this method will return
          * if the connection has been idle too long.
          */
         boolean canPurge (long currTime) {
             return destroyed || (!inUse && (currTime >= expirationTime));
         }
 
         /**
          * Returns whether the connection is available
          * for use by another user.
          *
          * @return <code>true</code> if available.
          */
         boolean isAvailable() {
             return !destroyed && !inUse;
         }
   
         /**
          * Set the methoed as destroyed so that it will
          * not return a LDAP Connection
          */
         public void setAsDestroyed() {
             this.destroyed = true; 
         }
 
         /**
          * Debug method
          *
          * @returns user-friendly rendering of the object.
          */
         public String toString() {
             return "LDAPConnection=" + ld + ",inUse=" + inUse +
                    " IsDestroyed=" + destroyed;
         }
 
         private LDAPConnection ld;   // LDAP Connection
         private boolean inUse;       // In use? (true = yes)
         private long expirationTime; // time in future when
                                      // when connection considered stale
         private boolean destroyed;   // destroyed is set when the connection
                                      // has been cleaned up.
     }
 
     private void createIdleCleanupThread() {
         if (idleTime > 0) {
             cleaner = new CleanupTask(pool);
             cleanupThread = new Thread(cleaner, name + "-cleanupThread");
             cleanupThread.start();
             if (debug.messageEnabled()) {
                 debug.message("Connection pool: " + name +
                               ": Cleanup thread created successfully.");
             }
         }
         return;
     }
 
     static {
         debug = Debug.getInstance("LDAPConnectionPool");
     }
 
     /**
      * Reinitialize the connection pool with a new connection
      * template.  This method will reap all existing connections
      * and create new connections with the master connection passed
      * in this parameter.
      *
      * @param ld master LDAP connection with new parameters.
      */
     public synchronized void reinitialize(LDAPConnection ld)
         throws LDAPException
     {
         synchronized (pool) {
             synchronized (deprecatedPool) {
                 deprecatedPool.addAll(pool);
                 stayAlive = false;
                // if idle timeout property is not set, cleanupThread will be
                // null. null checked here to avoid NPE.
                if (cleanupThread != null) {
                    cleanupThread.interrupt();
                    while (cleanupThread.isAlive()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException iex) {}
                    }
                 }
                 
                 pool.clear();
                 pool = new ArrayList();
                 this.host = ld.getHost();
                 this.port = ld.getPort();
                 this.authdn = ld.getAuthenticationDN();
                 this.authpw = ld.getAuthenticationPassword();
                 this.ldc = (LDAPConnection)ld.clone();
                 if (debug.messageEnabled()) {
                     debug.message("Connection pool: " + name +
                                   ": reinitializing connection pool: Host:" +
                                   host + " Port:" + port + "Auth DN:" + authdn);
                 }
                 createPool();
                 createIdleCleanupThread();
                 if (debug.messageEnabled()) {
                     debug.message("Connection pool: " + name +
                                   ": reinitialized successfully.");
                 }
             }
         }
     }
 
     private void destroyPool (ArrayList connPool) {
         synchronized (connPool) {
             this.defunct = true;
             while (pool.size() > 0) {
                 for (int i = 0; i < connPool.size(); ++i) {
                     LDAPConnectionObject lObj =
                         (LDAPConnectionObject)pool.get(i);
                     synchronized (lObj) {
                         if (lObj.isAvailable()) {
                             pool.remove(lObj);
                             disconnect(lObj);
                         }
                     }
                 }
 
                 // sleep for a second before retrying
                 if (pool.size() > 0) {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException iex) {
                         debug.error ("Connection pool:" + name +
                                     ":Interrupted in destroy method while " +
                                     "waiting for connections to be released.");
                     }
                 }
             }
         }
     }
 
     /**
      * Set minimum and maximum connnections that is maintained by
      * the connection pool object.
      *
      * @param min minimum number
      * @param max maximum number
      */
     public synchronized void resetPoolLimits(int min, int max) {
         if ((maxSize > 0) && (maxSize != max) && (min < max)) {
             if (debug.messageEnabled()) {
                 debug.message ("Connection pool:" + name +
                                ": is being resized: Old Min/Old Max:" +
                                minSize + '/' + maxSize + ": New Min/Max:" +
                                min + '/' + max);
             }
 
             int oldSize = this.maxSize;
             this.minSize = min;
             this.maxSize = max;
 
             synchronized (pool) {
                 if (oldSize > max) {
                     // if idle time is not set
                     if (cleaner == null) {
                         int diff = oldSize - max;
                         while (diff > 0) {
                             for (int i = 0; i < pool.size() &&
                                         (pool.size() > maxSize); ++i)
                             {
                                 LDAPConnectionObject ldO =
                                         (LDAPConnectionObject)pool.get(i);
                                 synchronized (ldO) {
                                     if (ldO.isAvailable()) {
                                         pool.remove(i);
                                         disconnect(ldO);
                                         --diff;
                                     }
                                 }
                             }
                             // wait for one second and retry till diff
                             // connections are removed.
                             try {
                                 Thread.sleep(1000);
                             } catch (InterruptedException iex) {}
                         }
                     }
                 } else {
                     if (debug.messageEnabled()) {
                         debug.message("Connection pool:" + name +
                                ":Ensuring pool buffer capacity to:" +
                                max * 2);
                     }
                     pool.ensureCapacity(max * 2);
                 }
             }
         }
     }
 
     public class CleanupTask implements Runnable {
         CleanupTask(ArrayList cleanupPool) {
             this.cleanupPool = cleanupPool;
         }
 
         private void checkDeprecatedConnections() {
             synchronized (deprecatedPool) {
                 if (debug.messageEnabled()) {
                     debug.message("Connection pool:" + name +
                            ": found " + deprecatedPool.size() +
                            " connection(s) to clean.");
                 }
 
                 for (int i = 0; i < deprecatedPool.size(); ++i) {
                     LDAPConnectionObject lObj =
                             (LDAPConnectionObject)deprecatedPool.get(i);
                     synchronized(lObj) {
                         if (lObj.isAvailable()) {
                             deprecatedPool.remove(i);
                             disconnect(lObj);
                         }
                     }
                 }
             }
         }
 
         public void run() {
             int sleepTime = (int)(idleTime/2);
             while (stayAlive) {
                 try {
                     Thread.sleep(sleepTime);
                 } catch(InterruptedException iex) {
                     continue;
                 }
 
                 if (debug.messageEnabled()) {
                     debug.message("Connection pool: " + name +
                                   ": starting cleanup.");
                 }
 
                 int startPoolSize = cleanupPool.size();
 
                 // check on connections deprecated earlier
                 if (deprecatedPool.size() > 0)
                     checkDeprecatedConnections();
 
                 synchronized (cleanupPool) {
                     Collections.sort(cleanupPool);
 
                     // Case: max size is reset to a lower limit
                     // Action: Move excess connections to deprecatedPool
                     // if they are currently used.  If not, remove
                     // them and disconnect.
                     if (maxSize < cleanupPool.size()) {
                         int diff = cleanupPool.size() - maxSize;
                         while (diff-- > 0) {
                             LDAPConnectionObject lObj =
                                 (LDAPConnectionObject)cleanupPool.get(0);
                             synchronized (lObj) {
                                 cleanupPool.remove(0);
                                 if (!lObj.isAvailable()) {
                                     synchronized (deprecatedPool) {
                                         deprecatedPool.add(lObj);
                                     }
                                 } else {
                                     disconnect(lObj);
                                 }
                             }
                         }
                     }
 
                     /*
                      * for now this code removes all code that's
                      * timed out.  The pool could shrink to 0, but
                      * on a request for new connection, it would
                      * clone and grow as per demand.
                      */
                     long now = System.currentTimeMillis();
                     for (int i = 0; i < cleanupPool.size(); ++i) {
                         LDAPConnectionObject ldO =
                             (LDAPConnectionObject)cleanupPool.get(i);
                         synchronized (ldO) {
                             if (ldO.canPurge(now)) {
                                 cleanupPool.remove(i);
                                 LDAPConnection ld = ldO.getLDAPConn();
                                 try {
                                     ld.disconnect();
                                 } catch (LDAPException e) {
                                     debug.error("Connection pool:" + name +
                                                 ":Error during disconnect.", e);
                                 }
                             }
                         }
                     }
                 }
                 if (debug.messageEnabled()) {
                     debug.message("Connection pool: " + name +
                                 ": finished cleanup: Start pool size:" +
                                 startPoolSize + ": end pool size:" +
                                 cleanupPool.size());
                 }
             }
         }
 
         private ArrayList cleanupPool = null;
     }
 
     private static Debug debug;   // Debug object
     private String name;          // name of connection pool;
     private int minSize;          // Min pool size
     private int maxSize;          // Max pool size
     private String host;          // LDAP host
     private int port;             // Port to connect at
     private String authdn;        // Identity of connections
     private String authpw;        // Password for authdn
     private LDAPConnection ldc = null;          // Connection to clone
     private java.util.ArrayList pool;           // the actual pool
     private java.util.ArrayList deprecatedPool; // the pool to be purged.
     private long idleTime;        // idle time in milli seconds
     private boolean stayAlive;    // boolean flag to exit cleanup loop
     private boolean defunct;      // becomes true after calling destroy
     private Thread cleanupThread; // cleanup thread
     private CleanupTask cleaner;  // cleaner object
 }
 
