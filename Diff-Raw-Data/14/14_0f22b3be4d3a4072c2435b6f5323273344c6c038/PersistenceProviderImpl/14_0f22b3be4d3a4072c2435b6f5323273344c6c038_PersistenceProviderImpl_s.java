 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.jpa.backend.core;
 
 import com.mchange.v1.util.ClosableResource;
 
 import org.apache.log4j.Logger;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import de.cismet.cids.jpa.entity.common.CommonEntity;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 public final class PersistenceProviderImpl implements PersistenceProvider {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(PersistenceProviderImpl.class);
 
     //~ Instance fields --------------------------------------------------------
 
     final transient EntityManagerFactory emf;
     final transient ThreadLocal<EntityManager> em;
    
     private final transient Properties runtimeProperties;
     private transient boolean closed;
 
     // the lock shall be used to synchronize the closed state
     private final transient ReentrantReadWriteLock rwLock;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of PersistenceProviderImpl.
      *
      * @param   runtimeProperties  the domainservers runtime properties
      * @param   caching            DOCUMENT ME!
      *
      * @throws  IllegalArgumentException  if the runtime properties are null or some essential properties are missing
      */
     public PersistenceProviderImpl(final Properties runtimeProperties, final boolean caching) {
         rwLock = new ReentrantReadWriteLock();
 
         closed = false;
         if (runtimeProperties == null) {
             throw new IllegalArgumentException("properties may not be null");                                          // NOI18N
         }
         this.runtimeProperties = runtimeProperties;
         this.em = new ThreadLocal<EntityManager>();
         emf = Persistence.createEntityManagerFactory("cidsDataObjectsPU", getPropertyMap(runtimeProperties, caching)); // NOI18N
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Closes this <code>PersistenceProviderImpl</code> object so that the
      * {@link javax.persistence.EntityManagerFactory} is released and no operation can be performed anymore.
      *
      * @see  com.mchange.v1.util.ClosableResource
      */
     @Override
     public void close() {
         rwLock.readLock().lock();
         if (closed) {
             rwLock.readLock().unlock();
             return;
         } else {
             rwLock.readLock().unlock();
             rwLock.writeLock().lock();
             try {
                 closed = true;
                 emf.close();
                 if (LOG.isInfoEnabled()) {
                     LOG.info("PersistenceProvider closed: " + this); // NOI18N
                 }
             } finally {
                 rwLock.writeLock().unlock();
             }
         }
     }
 
     /**
      * This method shall be used to close resources that depend on the opened state of this provider.
      *
      * @param   resource  the resource to be closed
      *
      * @throws  Exception              any exception that occurs during the close operation of the given resource
      * @throws  IllegalStateException  if the provider is already closed
      */
     // there is no choice but to obay the interface specification
     @Override
     @SuppressWarnings("PMD.SignatureDeclareThrowsException")
     public void monitorClose(final ClosableResource resource) throws Exception {
         rwLock.readLock().lock();
 
         try {
             if (closed) {
                 throw new IllegalStateException("provider already closed, cannot monitor close resource: " + resource); // NOI18N
             }
 
             // we won't close ourself
             if (resource == this) {
                 return;
             } else {
                 resource.close();
             }
         } finally {
             rwLock.readLock().unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public boolean isClosed() {
         try {
             rwLock.readLock().lock();
 
             return closed;
         } finally {
             rwLock.readLock().unlock();
         }
     }
 
     /**
      * Delivers the runtime properties associated with this <code>PersistenceProviderImpl</code>.
      *
      * @return  the runtime properties
      */
     @Override
     public Properties getRuntimeProperties() {
         return runtimeProperties;
     }
 
     /**
      * Gets the {@link javax.persistence.EntityManager} that is associated with the current thread.
      *
      * @return  the <code>EntityManager</code> of the current thread.
      *
      * @throws  IllegalStateException  if the <code>PersistenceProviderImpl</code> is already close (usually by a
      *                                 previous call to {@link #close()}).
      */
     @Override
     public EntityManager getEntityManager() {
         try {
             rwLock.readLock().lock();
 
             if (closed) {
                 throw new IllegalStateException("Provider already closed"); // NOI18N
             }
 
             return em.get();
         } finally {
             rwLock.readLock().unlock();
         }
     }
 
     /**
      * Creates a property map containing connection related information.
      *
      * @param   p        the properties from which the info is extracted
      * @param   caching  DOCUMENT ME!
      *
      * @return  a map containing (hibernate, c3p0,...) key value pairs
      *
      * @throws  IllegalArgumentException  if an essential property is missing
      */
     private Map getPropertyMap(final Properties p, final boolean caching) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("building property map: start"); // NOI18N
         }
         try {
             final Map map = new HashMap();
 
             String currentProp = p.getProperty("dialect");                                      // NOI18N
             if ((currentProp == null) || currentProp.trim().isEmpty()) {
                 throw new IllegalArgumentException("dialect is not set in runtime properties"); // NOI18N
             }
             map.put("hibernate.dialect", currentProp);                                          // NOI18N
 
             currentProp = p.getProperty("connection.driver_class");                                  // NOI18N
             if ((currentProp == null) || currentProp.trim().isEmpty()) {
                 throw new IllegalArgumentException("driver_class is not set in runtime properties"); // NOI18N
             }
             map.put("hibernate.connection.driver_class", currentProp);                               // NOI18N
 
             currentProp = p.getProperty("connection.username");                                  // NOI18N
             if ((currentProp == null) || currentProp.trim().isEmpty()) {
                 throw new IllegalArgumentException("username is not set in runtime properties"); // NOI18N
             }
             map.put("hibernate.connection.username", currentProp);                               // NOI18N
 
             currentProp = p.getProperty("connection.password");                                  // NOI18N
             if ((currentProp == null) || currentProp.trim().isEmpty()) {
                 throw new IllegalArgumentException("password is not set in runtime properties"); // NOI18N
             }
             map.put("hibernate.connection.password", currentProp);                               // NOI18N
 
             currentProp = p.getProperty("connection.url");                                  // NOI18N
             if ((currentProp == null) || currentProp.trim().isEmpty()) {
                 throw new IllegalArgumentException("url is not set in runtime properties"); // NOI18N
             }
             map.put("hibernate.connection.url", currentProp);
 
             // TODO: move to runtime.properties
             map.put("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider"); // NOI18N
             map.put("c3p0.initialPoolSize", "5");                                                              // NOI18N
             map.put("c3p0.min_size", "5");                                                                     // NOI18N
             map.put("c3p0.max_size", "10");                                                                    // NOI18N
             map.put("c3p0.checkoutTimeout", "2000");                                                           // NOI18N
             map.put("c3p0.maxStatements", "500");                                                              // NOI18N
             map.put("c3p0.maxIdleTimeExcessConnections", "5");                                                 // NOI18N
             map.put("c3p0.idleConnectionTestPeriod", "300");                                                   // NOI18N
             map.put("c3p0.acquireIncrement", "2");                                                             // NOI18N
             map.put("c3p0.numHelperThreads", "5");                                                             // NOI18N
 
             if (caching) {
                 // cache properties
                 map.put(
                     "hibernate.cache.region.factory_class",
                    "net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory");                                       // NOI18N
                map.put("hibernate.cache.use_second_level_cache", "true");                                           // NOI18N
                map.put("hibernate.cache.use_query_cache", "true");                                                  // NOI18N
                 map.put("net.sf.ehcache.configurationResourceName", "de/cismet/cids/jpa/backend/core/ehcache.xml"); // NOI18N
             } else {
                map.put("hibernate.cache.use_second_level_cache", "false");                                          // NOI18N
                map.put("hibernate.cache.use_query_cache", "false");                                                 // NOI18N
             }
 
             map.put("hibernate.show_sql", "false");      // NOI18N
             if (LOG.isDebugEnabled()) {
                 LOG.debug("building property map: end"); // NOI18N
             }
 
             return map;
         } catch (final Exception e) {
             LOG.error("could not set property hibernate.connection.url, will use environment settings", e); // NOI18N
             throw new IllegalArgumentException(
                 "connection properties could not be set, probably erroneous runtime properties",            // NOI18N
                 e);
         }
     }
 
     /**
      * Stores a subtype of {@link CommonEntity} using the {@link EntityManager} of the current thread. If the given
      * entity is a new one (the primary key is <code>null</code>) then the returned entity will contain an appropriate
      * key that was somehow assigned by the implementation/underlying data storage.
      *
      * @param   <T>     a subtype of <code>CommonEntity</code>
      * @param   entity  the subtype of <code>CommonEntity</code> to store
      *
      * @return  the now stored subtype of <code>CommonEntity</code>
      */
     @Override
     public <T extends CommonEntity> T store(final T entity) {
         final EntityManager e = getEntityManager();
         T ret = null;
         if (e.contains(entity)) {
             e.persist(entity);
         } else {
             ret = e.merge(entity);
         }
 
         return (ret == null) ? entity : ret;
     }
 
     /**
      * Deletes a {@link CommonEntity} using the {@link EntityManager} of the current thread.
      *
      * @param  ce  the <code>CommonEntity</code> to delete
      */
     @Override
     public void delete(final CommonEntity ce) {
         final EntityManager e = getEntityManager();
         final Object toDelete;
         if (e.contains(ce)) {
             toDelete = ce;
         } else {
             toDelete = e.merge(ce);
         }
         e.remove(toDelete);
     }
 
     @Override
     public void delete(final List<CommonEntity> entities) {
         for (final CommonEntity ce : entities) {
             delete(ce);
         }
     }
 
     /**
      * Retrieves the desired subtype of {@link CommonEntity} with the given <code>id</code> using the
      * {@link EntityManager} of the current thread. If there is no entity of the desired type and with the given <code>
      * id</code> a {@link NoResultException} is thrown.
      *
      * @param   <T>     a subtype of <code>CommonEntity</code>
      * @param   entity  the subtype of <code>CommonEntity</code> that shall be retrieved
      * @param   id      the <code>id</code> of the entity
      *
      * @return  the desired entity of type <code>T</code> with the given <code>id</code>
      *
      * @throws  NoResultException  if no entity of the desired type and with the given <code>id</code> can be found
      */
     @Override
     public <T extends CommonEntity> T getEntity(final Class<T> entity, final int id) throws NoResultException {
         final EntityManager entityManager = getEntityManager();
         final T e = entityManager.find(entity, id);
         // throw exception to be consistent with em.getSingleResult
         if (e == null) {
             throw new NoResultException("'" + entity.getSimpleName() + "' with id '" + id + "' does not exist"); // NOI18N
         }
 
         return e;
     }
 
     /**
      * Retrieves all entities of the desired subtype of {@link CommonEntity} using the {@link EntityManager} of the
      * current thread. If there exist no entities of the desired type an empty {@link List} is retured.
      *
      * @param   <T>     a subtype of <code>CommonEntity</code>
      * @param   entity  the subtype of <code>CommonEntity</code> that shall be retrieved
      *
      * @return  a <code>List</code> containing all entities of type <code>T</code>, never null.
      */
     @Override
     public <T extends CommonEntity> List<T> getAllEntities(final Class<T> entity) {
         final EntityManager entityManager = getEntityManager();
         final Query q = entityManager.createQuery("FROM " + entity.getSimpleName()); // NOI18N
 
         return q.getResultList();
     }
 
     /**
      * Retrieves the desired subtype of {@link CommonEntity} with the given <code>name</code> using the
      * {@link EntityManager} of the current thread. If there is no entity of the desired type and with the given <code>
      * name</code> a {@link NoResultException} is thrown.
      *
      * <p>This implementation assumes that there is a mapped field <code>name</code> withing the entity class.</p>
      *
      * @param   <T>     a subtype of <code>CommonEntity</code>
      * @param   entity  the subtype of <code>CommonEntity</code> that shall be retrieved
      * @param   name    the <code>name</code> of the entity
      *
      * @return  the desired entity of type <code>T</code> with the given <code>name</code>
      *
      * @throws  NoResultException  if no entity of the desired type and with the given <code>name</code> can be found
      */
     // TODO: introduce NamedEntity
     @Override
     public <T extends CommonEntity> T getEntity(final Class<T> entity, final String name) throws NoResultException {
         final EntityManager entityManager = getEntityManager();
         final Query q = entityManager.createQuery("FROM " + entity.getSimpleName() + " WHERE name = :name"); // NOI18N
         q.setParameter("name", name);                                                                        // NOI18N
 
         return (T)q.getSingleResult();
     }
 
     /**
      * Checks whether an entity of type <code>T</code> with the given <code>name</code> exists or not.
      *
      * <p>This implementation assumes that there is a mapped field <code>name</code> withing the entity class.</p>
      *
      * @param   <T>     a subtype of <code>CommonEntity</code>
      * @param   entity  the subtype of <code>CommonEntity</code> that shall be retrieved
      * @param   name    the <code>name</code> of the entity
      *
      * @return  true if an entity of type <code>T</code> with the given <code>name</code> exists, false otherwise
      */
     @Override
     public <T extends CommonEntity> boolean contains(final Class<T> entity, final String name) {
         try {
             getEntity(entity, name);
 
             return true;
         } catch (final NoResultException ex) {
             return false;
         }
     }
 }
