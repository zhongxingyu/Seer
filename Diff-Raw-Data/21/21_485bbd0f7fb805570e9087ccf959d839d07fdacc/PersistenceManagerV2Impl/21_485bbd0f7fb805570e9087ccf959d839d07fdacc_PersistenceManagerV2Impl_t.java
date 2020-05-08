 package de.skuzzle.polly.core.internal.persistence;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.NoResultException;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 
import sun.reflect.Reflection;
 import de.skuzzle.polly.core.util.MillisecondStopwatch;
 import de.skuzzle.polly.core.util.Stopwatch;
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.EntityConverter;
 import de.skuzzle.polly.sdk.PersistenceManagerV2;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.tools.concurrent.ThreadFactoryBuilder;
 
 public class PersistenceManagerV2Impl extends AbstractDisposable 
         implements PersistenceManagerV2 {
 
     private final static Logger logger = Logger.getLogger(PersistenceManagerV2Impl.class
         .getName());
 
     
     
     private abstract class WriteImpl implements Write {
         
         @Override
         public <T> Write all(Iterable<T> list) {
             for (final T element : list) {
                 em.persist(element);
             }
             return this;
         }
 
 
 
         @Override
         public <T> Write single(T obj) {
             em.persist(obj);
             return this;
         }
 
 
 
         @Override
         public <T> Write remove(T obj) {
             em.remove(obj);
             return this;
         }
 
 
 
         @Override
         public <T> Write removeAll(Iterable<T> elements) {
             for (final T element : elements) {
                 em.remove(element);
             }
             return this;
         }
 
 
 
         @Override
         public Read read() {
             // return new unlocked read instance
             return new ReadImpl() {
                 @Override
                 public void close() {
                     // do nothing
                 }
             };
         }
     }
 
     
     
     private abstract class ReadImpl implements Read {
 
         @Override
         public <T> T find(Class<T> type, Object key) {
             logger.trace("Looking up primary key " + key + " in " + type.getName());
             final Stopwatch watch = new MillisecondStopwatch();
             watch.start();
             try {
                 return em.find(type, key);
            } catch (Exception e) {
                logger.error("", e);
                throw e;
             } finally {
                 long time = watch.stop();
                 logger.trace("Query time: " + time + "ms");
             }
         }
 
 
 
         @Override
         public <T> T findSingle(Class<T> type, String query) {
             return this.findSingle(type, query, new Param());
         }
         
         
         
         @Override
         @SuppressWarnings("unchecked")
         public <T> T findSingle(Class<T> type, String query, Param params) {
             logger.trace("Executing named query '" + query + "'. Parameters: " + params);
             final Stopwatch watch = new MillisecondStopwatch();
             watch.start();
             try {
                 Query q = em.createNamedQuery(query);
                 int i = 1;
                 for (Object param : params.getParams()) {
                     q.setParameter(i++, param);
                 }
     
                 return (T) q.getSingleResult();
             } catch (NoResultException e) {
                 return null;
            } catch (Exception e) {
                logger.error("", e);
                throw e;
             } finally {
                 long time = watch.stop();
                 logger.trace("Query time: " + time + "ms");
             }
         }
 
 
         
         @Override
         public <T> List<T> findList(Class<T> type, String query) {
             return this.findList(type, query, new Param());
         }
         
         
 
         @Override
         @SuppressWarnings("unchecked")
         public <T> List<T> findList(Class<T> type, String query, Param params) {
             logger.trace("Executing named query '" + query + "'. Parameters: " + params);
             final Stopwatch watch = new MillisecondStopwatch();
             watch.start();
             try {
                 Query q = em.createNamedQuery(query);
                 int i = 1;
                 for (Object param : params.getParams()) {
                     q.setParameter(i++, param);
                 }
     
                 return q.getResultList();
            } catch (Exception e) {
                logger.error("", e);
                throw e;
             } finally {
                 long time = watch.stop();
                 logger.trace("Query time: " + time + "ms");
             }
         }
 
 
         
         @Override
         public <T> List<T> findList(Class<T> type, String query, int limit) {
             return this.findList(type, query, limit, new Param());
         }
 
         
         
         @Override
         @SuppressWarnings("unchecked")
         public <T> List<T> findList(Class<T> type, String query, int limit,
                 Param params) {
             logger.trace("Executing named query '" + query + "'. Parameters: "
                 + params + ", limit: " + limit);
             final Stopwatch watch = new MillisecondStopwatch();
             watch.start();
             
             try {
                 Query q = em.createNamedQuery(query);
                 q.setMaxResults(limit);
                 int i = 1;
                 for (Object param : params.getParams()) {
                     q.setParameter(i++, param);
                 }
     
                 return q.getResultList();
            } catch (Exception e) {
                logger.error("", e);
                throw e;
             } finally {
                 long time = watch.stop();
                 logger.trace("Query time: " + time + "ms");
             }
         }
 
 
         
         @Override
         public <T> List<T> findList(Class<T> type, String query, int first, int limit) {
             return this.findList(type, query, first, limit, new Param());
         }
 
         
         
         @Override
         @SuppressWarnings("unchecked")
         public <T> List<T> findList(Class<T> type, String query, int first, int limit,
                 Param params) {
             logger.trace("Executing named query '" + query + "'. Parameters: "
                 + params + ", first: " + first + ", limit:" + limit);
             final Stopwatch watch = new MillisecondStopwatch();
             watch.start();
             try {
                 final Query q = em.createNamedQuery(query);
                 q.setFirstResult(first);
                 q.setMaxResults(limit);
                 int i = 1;
                 for (Object param : params.getParams()) {
                     q.setParameter(i++, param);
                 }
                 return q.getResultList();
            } catch (Exception e) {
                logger.error("", e);
                throw e;
             } finally {
                 long time = watch.stop();
                 logger.trace("Query time: " + time + "ms");
             }
         }
     }
     
     
     
     private class SynchedRead implements Read {
 
         @Override
         public <T> T find(Class<T> cls, Object key) {
             try (final Read r = read()) {
                 return r.find(cls, key);
             }
         }
 
         @Override
         public <T> List<T> findList(Class<T> type, String query) {
             try (final Read r = read()) {
                 return r.findList(type, query);
             }
         }
 
         @Override
         public <T> List<T> findList(Class<T> type, String query, Param params) {
             try (final Read r = read()) {
                 return r.findList(type, query, params);
             }
         }
 
         @Override
         public <T> List<T> findList(Class<T> type, String query, int limit) {
             try (final Read r = read()) {
                 return r.findList(type, query, limit);
             }
         }
 
         @Override
         public <T> List<T> findList(Class<T> type, String query, int limit, 
                 Param params) {
             try (final Read r = read()) {
                 return r.findList(type, query, limit, params);
             }
         }
 
         @Override
         public <T> List<T> findList(Class<T> type, String query, int first, int limit) {
             try (final Read r = read()) {
                 return r.findList(type, query, first, limit);
             }
         }
 
         @Override
         public <T> List<T> findList(Class<T> type, String query, int first, int limit,
                 Param params) {
             try (final Read r = read()) {
                 return r.findList(type, query, first, limit, params);
             }
         }
 
         @Override
         public <T> T findSingle(Class<T> type, String query) {
             try (final Read r = read()) {
                 return r.findSingle(type, query);
             }
         }
 
         @Override
         public <T> T findSingle(Class<T> type, String query, Param params) {
             try (final Read r = read()) {
                 return r.findSingle(type, query, params);
             }
         }
 
         @Override
         public void close() {
             // Do nothing
         }
     }
     
     
 
     private abstract class ParallelWriteImpl implements Write {
 
         protected final Collection<Atomic> actions;
 
 
 
         public ParallelWriteImpl() {
             this.actions = new ArrayList<>();
         }
 
 
 
         @Override
         public <T> Write all(final Iterable<T> list) {
             this.actions.add(new Atomic() {
                 @Override
                 public void perform(Write write) {
                     write.all(list);
                 }
             });
             return this;
         }
 
 
 
         @Override
         public <T> Write single(final T obj) {
             this.actions.add(new Atomic() {
                 @Override
                 public void perform(Write write) {
                     write.single(obj);
                 }
             });
             return this;
         }
 
 
 
         @Override
         public <T> Write remove(final T obj) {
             this.actions.add(new Atomic() {
                 @Override
                 public void perform(Write write) {
                     write.remove(obj);
                 }
             });
             return this;
         }
 
 
 
         @Override
         public <T> Write removeAll(final Iterable<T> elements) {
             this.actions.add(new Atomic() {
                 @Override
                 public void perform(Write write) {
                     write.all(elements);
                 }
             });
             return this;
         }
 
 
 
         @Override
         public Read read() {
             throw new UnsupportedOperationException();
         }
     }
     
     
 
     private EntityManagerFactory emf;
     private EntityManager em;
     private EntityTransaction activeTransaction;
     private final ReadWriteLock locker;
     private final ExecutorService executor;
     private final EntityList entities;
     private final EntityConverterManagerImpl entityConverter;
 
     
 
     public PersistenceManagerV2Impl() {
         this.locker = new ReentrantReadWriteLock();
         this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder(
             "PERSISTENCE"));
         this.entities = new EntityList();
         this.entityConverter = new EntityConverterManagerImpl(this);
     }
 
 
     
     EntityList getEntities() {
         return this.entities;
     }
 
 
 
     @Override
     public void registerEntity(Class<?> clazz) {
         logger.debug("Registering new entity: " + clazz.getName());
         this.entities.add(clazz);
     }
     
     
     
     @Override
     public void registerEntityConverter(EntityConverter ec) {
         this.entityConverter.addConverter(ec);
     }
     
     
     
     void runAllEntityConverters() throws DatabaseException {
         this.entityConverter.convertAll();
     }
     
     
 
     public void connect(String persistenceUnit) {
         logger.info("Connecting to persistence unit '" + persistenceUnit + "'...");
 
         this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
         this.em = this.emf.createEntityManager();
 
         logger.info("Database connection established.");
     }
 
 
 
     /**
      * Starts a new transaction which can be used to add, modify or delete entities 
      * within the database. In order to do this, a WriteLock is acquired so that no other
      * thread can access the database while this transaction is active.
      */
     private void startTransaction() {
         logger.trace("Acquiring write lock...");
         this.locker.writeLock().lock();
         
         logger.trace("Got write lock.");
         logger.debug("Starting transaction...");
         this.activeTransaction = this.em.getTransaction();
         this.activeTransaction.begin();
         logger.debug("Transaction started.");
     }
 
 
 
     private void commitTransaction() throws DatabaseException {
         logger.debug("Committing transaction...");
         final EntityTransaction tx = this.activeTransaction;
         
         try {
             if (tx == null) {
                 throw new DatabaseException("No transaction active.");
             }
             
             tx.commit();
             logger.debug("Transaction finished successful");
         } catch (Exception e) {
             logger.error("Committing transaction failed.", e);
             if (tx != null && tx.isActive()) {
                 try {
                     logger.debug("Trying to rollback transaction.");
                     tx.rollback();
                     logger.debug("Rollback successful.");
                 } catch (Exception e1) {
                     logger.fatal("Rollback failed!", e1);
                 }
             }
             throw new DatabaseException("Transaction failed", e);
         } finally {
             logger.trace("Writelock released");
             locker.writeLock().unlock();
         }
     }
 
     
     
     @Override
     public void refresh(Object obj) {
         this.em.refresh(obj);
     }
 
 
     
     @Override
     public Read read() {
         logger.trace("Acquiring read lock...");
         final Stopwatch watch = new MillisecondStopwatch();
         watch.start();
         
         this.locker.readLock().lock();
         logger.trace("Got read lock.");
         
         return new ReadImpl() {
             @Override
             public void close() {
                 logger.trace("Readlock released");
                 locker.readLock().unlock();
                 long time = watch.stop();
                 logger.trace("Read transaction time: " + time + "ms");
             }
         };
     }
 
 
     
     @Override
     public Read atomic() {
         return new SynchedRead();
     }
     
     
 
     @Override
     public Write write() {
         final Stopwatch watch = new MillisecondStopwatch();
         watch.start();
         
         this.startTransaction();
         
         return new WriteImpl() {
             @Override
             public void close() throws DatabaseException {
                 try {
                     commitTransaction();
                 } finally {
                     long time = watch.stop();
                     logger.trace("Write transaction time: " + time + "ms");
                 }
             }
         };
     }
     
     
     
     @Override
     public void writeAtomic(Atomic a) throws DatabaseException {
         try (final Write w = this.write()) {
             a.perform(w);
         }
     }
 
     
     
     @Override
     public void writeAtomicParallel(Atomic a) {
         this.writeAtomicParallel(a, new TransactionCallback() {
             @Override
             public void success() {}
             
             
             
             @Override
             public void fail(DatabaseException e) {
                 logger.error("", e);
             }
         });
     }
     
     
     
     @Override
     public void writeAtomicParallel(final Atomic a, final TransactionCallback cb) {
         this.executor.submit(new Runnable() {
             @Override
             public void run() {
                 try (final Write w = write()) {
                     a.perform(w);
                 } catch (DatabaseException e) {
                     cb.fail(e);
                 }
                 cb.success();
             }
         });
     }
     
     
     
     @Override
     public Write writeParallel() {
         return this.writeParallel(new TransactionCallback() {
             @Override
             public void success() {}
             
             
             
             @Override
             public void fail(DatabaseException e) {
                 logger.error("", e);
             }
         });
     }
     
 
 
     @Override
     public Write write(final TransactionCallback cb) {
         return new ParallelWriteImpl() {
             @Override
             public void close() {
                 try (final Write w = write()) {
                     for (final Atomic wr : actions) {
                         wr.perform(w);
                     }
                 } catch (DatabaseException e) {
                     cb.fail(e);
                 }
                 cb.success();
             }
         };
     }
 
 
 
     @Override
     public Write writeParallel(final TransactionCallback cb) {
         return new ParallelWriteImpl() {
             @Override
             public void close() {
                 executor.submit(new Runnable() {
                     @Override
                     public void run() {
                         try (final Write w = write()) {
                             for (final Atomic wr : actions) {
                                 wr.perform(w);
                             }
                         } catch (DatabaseException e) {
                             cb.fail(e);
                         }
                         cb.success();
                     }
                 });
             }
         };
     }
     
     
     
     @Override
     protected void actualDispose() throws DisposingException {
         logger.debug("Shutting down database...");
         logger.trace("Shutting down entity manager...");
         try {
             logger.trace("Waiting for all operations to end...");
             this.locker.writeLock().lock();
             if (this.em.isOpen()) {
                 try {
                     logger.trace("Sending SHUTDOWN command.");
                     this.startTransaction();
                     final Query q = this.em.createNativeQuery("SHUTDOWN");
                     q.executeUpdate();
                     this.commitTransaction();
                 } catch (Exception e) {
                     logger.error("SHUTDOWN command failed.");
                 }
                 this.em.close();
                 this.em = null;
             }
 
             logger.trace("Shutting down entity manager factory...");
             if (this.emf.isOpen()) {
                 this.emf.close();
                 this.emf = null;
             }
 
             logger.debug("Database connection closed.");
         } catch (Exception e) {
             logger.fatal("Error while shutting down database.");
         } finally {
             this.locker.writeLock().unlock();
         }
     }
 }
