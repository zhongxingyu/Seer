 /*
 * @(#)Connection.java   10/06/02
  *
  * Copyright (c) 2010 Roger Suen(SUNRUJUN)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  */
 
 package org.javaplus.netbeans.persistence.connection;
 
 import org.javaplus.netbeans.api.persistence.PersistenceProvider;
 import org.javaplus.netbeans.api.persistence.PersistenceProviderManager;
 import org.javaplus.netbeans.api.persistence.PersistenceUnit;
 import org.javaplus.netbeans.api.persistence.PersistenceUtil;
 
 import org.netbeans.api.db.explorer.JDBCDriver;
 import org.netbeans.api.db.explorer.JDBCDriverManager;
 
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.URLMapper;
 import org.openide.util.Exceptions;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.metamodel.Metamodel;
 
 /**
  *
  * @author Roger Suen
  */
 public class Connection extends ConnectionEventSupport {
 
     /**
      * The singleton logger instance.
      */
     private static final Logger logger =
         Logger.getLogger(Connection.class.getName(),
                          Connection.class.getPackage().getName() + ".Bundle");
 
     /**
      * The connection manager which created this connection.
      */
     private final ConnectionManager manager;
 
     /**
      * The persistence unit which this connection connects to.
      */
     private final PersistenceUnit unit;
 
     /**
      * Open state indicator
      */
     private boolean isOpen = true;
 
     /**
      *
      */
     private ExecutorService executor;
 
     /**
      *
      */
     private EntityManagerFactory entityManagerFactory;
 
     /**
      * The metamodel of the persistence unit.
      */
     private Metamodel metamodel;
 
     //
     // Session management
     //
     private final List<Session> sessions = new CopyOnWriteArrayList<Session>();
     private int nextSessionId = 1;
 
     /**
      * Constructs a new instance of <tt>Connection</tt> with the specified
      * connection manager and the persistence unit.
      * @param manager the manager creates this connection.
      * @param unit    the unit which this connection connects to.
      * @throws NullPointerException if either <tt>manager</tt> or <tt>unit</tt>
      *                              is <tt>null</tt>.
      * @throws ConnectionException  if failed to connect to the specified unit.
      */
     Connection(ConnectionManager manager, PersistenceUnit unit)
             throws ConnectionException {
         if (manager == null) {
             throw new NullPointerException("null manager");
         } else if (unit == null) {
             throw new NullPointerException("null unit");
         }
 
         this.manager = manager;
         this.unit = unit;
         doOpen();
     }
 
     /**
      *
      * @return
      * @throws IllegalStateException if this connection has been closed.
      */
     public ConnectionManager getConnectionManager() {
         return manager;
     }
 
     /**
      *
      * @return
      * @throws IllegalStateException if this connection has been closed.
      */
     public PersistenceUnit getPersistenceUnit() {
         return unit;
     }
 
     /**
      *
      * @return
      * @throws IllegalStateException if this connection has been closed.
      */
     public Metamodel getMetamodel() {
         verifyOpen();
         return metamodel;
     }
 
     /**
      * Indicates whether this connection is open. Returns <tt>true</tt> until
      * the connection has been closed.
      * @return <tt>true</tt> if this connection is doOpen; <tt>false</tt>
      *         otherwise.
      */
     public boolean isOpen() {
         return isOpen;
     }
 
     /**
      * Closes this connection, releasing any resources that might be held
      * by this connection. After invoking this method, all methods on the
      * instance will throw an {@link IllegalStateException}, except for
      * {@link #isOpen}, which will return <code>false</code>.
      * @throws IllegalStateException if this connection has been closed.
      */
     public synchronized void close() throws ConnectionException {
         verifyOpen();
         isOpen = false;
         try {
             executor.submit(new CloseTask()).get();
         } catch (InterruptedException ex) {
             Exceptions.printStackTrace(ex);
             throw new ConnectionException(ex);
         } catch (ExecutionException ex) {
             Exceptions.printStackTrace(ex);
             throw new ConnectionException(ex);
         }
 
         executor.shutdownNow();
         fireConnectionClosedEvent(this);
     }
 
     /**
      *
      * @return
      * @throws ConnectionException
      */
     public Session openSession() throws ConnectionException {
         verifyOpen();
         Session session = new Session(this, nextSessionId++);
         sessions.add(session);
         return session;
     }
 
     /**
      * Used only by {@link Session#close() } to remove the closed session
      * from our internal sessions list.
      * @param session the session to remove
      */
     void removeSession(Session session) {
         sessions.remove(session);
     }
 
     EntityManagerFactory getEntityManagerFactory() {
         return entityManagerFactory;
     }
 
     private void verifyOpen() {
         if (!isOpen) {
             throw new IllegalStateException(
                 "Attempting to execute an operation on a closed "
                 + "Connection instance.");
         }
     }
 
     private void closeAllSessions() {
         for (Session session : sessions) {
             try {
 
                 // NOTE:
                 // Although the Session.close() method will call
                 // Connection.removeSession() method to remove it from
                 // our sessions list, since we use CopyOnWriteArrayList,
                 // so it's still safe to iterate on the list here
                 session.close();
             } catch (ConnectionException ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
     }
 
     private void doOpen() throws ConnectionException {
         executor =
             Executors.newSingleThreadExecutor(new ConnectionThreadFactory());
         try {
             executor.submit(new OpenTask()).get();
         } catch (InterruptedException ex) {
             Exceptions.printStackTrace(ex);
             throw new ConnectionException(ex);
         } catch (ExecutionException ex) {
             Exceptions.printStackTrace(ex);
             throw new ConnectionException(ex);
         }
     }
 
     private class ConnectionThreadFactory implements ThreadFactory {
        @Override
         public Thread newThread(Runnable target) {
             Thread t = new Thread(target);
             t.setName("ConnectionExecutorThread (" + unit.getName() + ")");
            // NOTE:
            // DON'T use thread context classloader. The context classloader
            // is the system class loader of Netbeans. It will try to load
            // classes from any loaded modules. See:
            // http://wiki.netbeans.org/DevFaqModuleCCE
            ClassLoader cl = new URLClassLoader(getConnectionUrls(),
                                 Connection.class.getClassLoader());
             t.setContextClassLoader(cl);
             return t;
         }
     }
 
 
     private URL[] getConnectionUrls() {
         ArrayList<URL> urlList = new ArrayList<URL>();
 
         // append URLs of the persistence unit
         List<URL> unitUrls = PersistenceUtil.getUrls(unit);
         urlList.addAll(unitUrls);
 
         // append URLs of all peristence providers
         PersistenceProvider[] providers =
             PersistenceProviderManager.getDefault().getProviders();
         for (int i = 0; i < providers.length; i++) {
             List<URL> providerUrls = PersistenceUtil.getUrls(providers[i]);
             urlList.addAll(providerUrls);
         }
 
         // append URLs of the database driver
         JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
         for (int i = 0; i < drivers.length; i++) {
             JDBCDriver driver = drivers[i];
             URL[] driverUrls = driver.getURLs();
             urlList.addAll(Arrays.asList(driverUrls));
         }
 
         for (int i = 0; i < urlList.size(); i++) {
             URL url = urlList.get(i);
             if ("nbinst".equals(url.getProtocol())) {
 
                 // try to get a file: URL for the nbinst: URL
                 FileObject fo = URLMapper.findFileObject(url);
                 if (fo != null) {
                     URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                     if (localURL != null) {
                         urlList.set(i, localURL);
                     }
                 }
             }
         }
 
         return urlList.toArray(new URL[urlList.size()]);
     }
 
     private class OpenTask implements Callable<Object> {
         public Object call() throws Exception {
             logger.log(Level.INFO, "Connection.CONNECTING", unit);
 
             // create entity manager factory
             EntityManagerFactory emf = null;
             try {
                 emf = Persistence.createEntityManagerFactory(unit.getName());
                 if (emf == null) {
                     logger.log(Level.WARNING, "Connection.NO_PROVIDER", unit);
                     throw new ConnectionException("Connection.NO_PROVIDER");
                 } else if (logger.isLoggable(Level.FINER)) {
                     logger.log(Level.FINER, "Connection.PROVIDER_FOUND", unit);
                 }
             } catch (Exception e) {
                 logger.log(Level.WARNING, "Connection.NO_PROVIDER_EXCEPTION",
                            e);
                 throw new ConnectionException(e);
             }
 
             // retrieve metamodel
             //
             // NOTE:
             // use an entity manager to retrieve the metamodel.
             // eclipselink will throw NPE if use entity manager factory
             // to retrieve the metamodel directly
             EntityManager em = null;
             Metamodel mm = null;
             try {
                 em = emf.createEntityManager();
                 mm = em.getMetamodel();
             } catch (Exception e) {
                 logger.log(Level.WARNING,
                            "Connection.OpenTask.METAMODEL_EXCEPTION", e);
                 throw new ConnectionException(e);
             } finally {
                 if (em != null) {
                     try {
                         em.close();
                     } catch (Exception e) {
                         logger.log(
                             Level.WARNING,
                             "Connection.OpenTask.CLOSE_ENTITY_MANAGER_FAILED",
                             e);
                     }
                 }
             }
 
             // save the entity manager factory and metamodel
             // associated with the connection
             entityManagerFactory = emf;
             metamodel = mm;
 
             // done
             logger.log(Level.FINE, "Connection.CONNECTED", unit);
             return null;
         }
     }
 
 
     private class CloseTask implements Callable<Object> {
         public Object call() throws Exception {
             logger.log(Level.INFO, "Connection.CLOSING", unit);
             try {
                 entityManagerFactory.close();
             } catch (Exception e) {
                 logger.log(Level.WARNING, "Connection.CloseTask.FAIL", unit);
                 throw new ConnectionException(e);
             }
 
             entityManagerFactory = null;
             metamodel = null;
 
             // done
             logger.log(Level.FINE, "Connection.CLOSED", unit);
             return null;
         }
     }
 }
