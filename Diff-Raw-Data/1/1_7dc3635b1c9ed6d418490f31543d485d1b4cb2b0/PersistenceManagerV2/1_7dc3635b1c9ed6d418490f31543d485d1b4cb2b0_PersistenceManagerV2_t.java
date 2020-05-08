 package de.skuzzle.polly.sdk;
 
 import java.util.Arrays;
 import java.util.List;
 
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 
 
 
 public interface PersistenceManagerV2 {
     
     public final static class Param {
         private final Object[] params;
         
         
         public Param(Object...params) {
             this.params = params;
         }
         
         
         
         public Param(String[] params) {
             this.params = new Object[params.length];
             for (int i = 0; i < params.length; ++i) {
                 this.params[i] = params[i];
             }
         }
         
         
         
         public Object[] getParams() {
             return this.params;
         }
         
         
         
         @Override
         public String toString() {
             return Arrays.toString(this.params);
         }
     }
     
     
     
     /**
      * This represents an atomic database write operation. Instances of this interface 
      * can be used to perform multiple write operations to the database within a single 
      * transaction.
      * 
      * <p>The great advantage of using this class to perform multiple operations at once
      * is, that you may decide to perform all the changes asynchronously without having
      * to change a single line of code.</p>
      * 
      * @author Simon Taddiken
      */
     public interface Atomic {
         /**
          * This method is called to perform all the write operations to the database. The
          * provided {@link Write} instance will always support the 
          * {@link Write#read() read} operation.
          * 
          * @param write Instance to write to the database.
          * @throws DatabaseException If a database error occurred 
          */
         public void perform(Write write) throws DatabaseException;
     }
     
     
     
     /**
      * The Write interface provides access to insert or remove entries from the database.
      * Optionally, a Write instance may support reading values by providing a 
      * {@link Read} instance via {@link #read()}.
      *
      * <p>All modifications made using this class will first be submitted when calling 
      * the {@link #close()} method. Instances of this class must always be used within
      * a try-resources block, otherwise deadlocks or database corruption may occur.</p>
      * @author Simon Taddiken
      */
     public interface Write extends AutoCloseable {
         
         /**
          * Marks each of the provided elements to be persisted upon closing this Write
          * instance.
          * 
          * @param elements The elements to persist.
          * @return This instance for method chaining.
          */
         public <T> Write all(Iterable<T> elements);
         
         /**
          * Marks a single element to be persisted upon closing this Write instance.
          * 
          * @param obj The object to persist.
          * @return This instance for method chaining.
          */
         public <T> Write single(T obj);
         
         /**
          * Marks a single element to be removed upon closing this Write instance.
          * 
          * @param obj The object to persist.
          * @return This instance for method chaining.
          */
         public <T> Write remove(T obj);
         
         /**
          * Marks each of the provided elements to be removed upon closing this Write
          * instance.
          * 
          * @param elements The elements to remove.
          * @return This instance for method chaining.
          */
         public <T> Write removeAll(Iterable<T> elements);
         
         /**
          * Provides a Read instance to read values from the database within the current
          * lock scope. The returned Read instance does not need to be closed as it will
          * be closed automatically when this instance is closed.
          * 
          * <p>This operation is optional and may throw an 
          * {@link UnsupportedOperationException}. Methods returning a Write instance are 
          * required to document whether the returned instance supports reading or not.</p>
          * 
          * @return A Read instance to read values from the database.
          */
         public Read read();
         
         /**
          * Closes this Write instance and submits all changes to the database. Also, all
          * locks are released, even if an error occurred.
          * 
          * @throws DatabaseException If submitting the changes to the database failed.
          */
         @Override
         public void close() throws DatabaseException;
     }
     
     
     
     public interface Read extends AutoCloseable {
         
         /**
          * Finds an entity using its primary key.
          * 
          * Usage:
          * <pre>
          *  find(Employee.class, employeeId);
          * </pre>
          * @param type The entities type.
          * @param key The primary key of the entity to find.
          * @return The found entity or <code>null</code> of the primary key was not found.
          */
         public <T> T find(Class<T> type, Object key);
         
         /**
          * Retrieves a whole list of entities from the database using a named query.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @return A list of entities matching the query. The list may be empty if no
          *      entity was found.
          */
         public <T> List<T> findList(Class<T> type, String query);
         
         /**
          * Retrieves a whole list of entities from the database using a named query.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @param params The parameter values for the query in order they appear in the
          *      query string.
          * @return A list of entities matching the query. The list may be empty if no
          *      entity was found.
          */
         public <T> List<T> findList(Class<T> type, String query, Param params);
         
         /**
          * Retrieves a whole list of entities from the database using a named query.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @param limit The maximum amount of entities to retrieve.
          * @return A list of entities matching the query. The list may be empty if no
          *      entity was found.
          */
         public <T> List<T> findList(Class<T> type, String query, int limit);
         
         /**
          * Retrieves a whole list of entities from the database using a named query.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @param limit The maximum amount of entities to retrieve.
          * @param params The parameter values for the query in order they appear in the
          *      query string.
          * @return A list of entities matching the query. The list may be empty if no
          *      entity was found.
          */
         public <T> List<T> findList(Class<T> type, String query, int limit, 
             Param params);
         
         /**
          * Retrieves a whole list of entities from the database using a named query.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @param first Number of the first entry to retrieve.
          * @param limit The maximum amount of entities to retrieve.
          * @return A list of entities matching the query. The list may be empty if no
          *      entity was found.
          */
         public <T> List<T> findList(Class<T> type, String query, int first, int limit);
         
         /**
          * Retrieves a whole list of entities from the database using a named query.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @param first Number of the first entry to retrieve.
          * @param limit The maximum amount of entities to retrieve.
          * @param params The parameter values for the query in order they appear in the
          *      query string.
          * @return A list of entities matching the query. The list may be empty if no
          *      entity was found.
          */
         public <T> List<T> findList(Class<T> type, String query, int first, int limit,
             Param params);
         
         /**
          * Finds a single entity using a named query. This method may throw an 
          * Exception if the query returns more than one item.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @return The entity found or <code>null</code> if it was not found.
          */
         public <T> T findSingle(Class<T> type, String query);
         
         /**
          * Finds a single entity using a named query. This method may throw an 
          * Exception if the query returns more than one item.
          * 
          * @param type The entities type.
          * @param query The name of the named query. The query may only use numbered 
          *      parameters. 
          * @param params The parameter values for the query in order they appear in the
          *      query string.
          * @return The entity found or <code>null</code> if it was not found.
          */
         public <T> T findSingle(Class<T> type, String query, Param params);
         
         /**
          * Closes this Read instance and releases all database locks.
          */
         @Override
         public void close();
     }
     
     
     
     /**
      * Callback interface which is used in conjunction with the asynchronous methods to
      * submit database changes.
      * 
      * @author Simon Taddiken
      */
     public interface TransactionCallback {
         /**
          * This method is called when a database transaction could be committed without
          * any errors.
          */
         public void success();
         
         /**
          * This method is called when committing a database transaction failed.
          * 
          * @param e The exception which caused the transaction to fail.
          */
         public void fail(DatabaseException e);
     }
     
     
     
     /**
      * Creates a new {@link Read} object to read values from the database. Upon calling
      * this method, the database will be locked for read access and upon closing the
      * returned Read instance this lock will be released. The returned object should
      * always be used within a try-resources block like this:
      * <pre>
      * try (final Read read = persistence.read()) {
      *     final MyEntity me = read.find(MyEntity.class, 1);
      * }
     * </pre>
      * This ensures that the lock created by this method is released correctly.
      * 
      * @return Read access to the database.
      */
     public Read read();
     
     public Read atomic();
     
     /**
      * Creates a new {@link Write} object to add, modify or delete entities from the 
      * database. Upon calling this method, the database will be locked for write access 
      * and a new transaction will be initiated. The transaction will be submitted to the 
      * database when the returned Write instance is being closed. Also, the lock will be 
      * released when closing the instance. You should always use this method within a 
      * try-resources block like this:
      * <pre>
      * try (final Write write = persistence.write()) {
      *     write.single(myObject);
      *     // ...
      * }
      * </pre> 
      * This ensures that the lock and transaction created by this method are released
      * correctly.
      * 
      * <p>The Write instance returned by this method also supports reading using 
      * {@link Write#read()}.</p>
      * 
      * @return Write access to the database.
      */
     public Write write();
 
     /**
      * This method atomically creates a new write-locked transaction, 
      * {@link Atomic#perform(Write) executes} the passed {@link Atomic} and submits all
      * changes to the database. All this is done within the current thread.
      * 
      * @param a The Atomic instance to execute.
      * @throws DatabaseException If submitting the transaction failed.
      */
     public void writeAtomic(Atomic a) throws DatabaseException;
     
     /**
      * Executes the changes performed by the passed {@link Atomic} instance within a new
      * thread. Information about whether the operation was successful are simply 
      * ignored by this method.
      * 
      * @param a The Atomic instance to execute.
      */
     public void writeAtomicParallel(Atomic a);
     
     /**
      * Executes the changes performed by the passed {@link Atomic} instance within a new
      * thread. Using a {@link TransactionCallback} the caller can be notified about 
      * whether the changes could be submitted to the database or an error occurred.
      * 
      * @param a The atomic instance to execute within a new thread.
      * @param cb Callback which is notified about the success of the parallel transaction.
      */
     public void writeAtomicParallel(Atomic a, TransactionCallback cb);
     
     /**
      * Gets a Write instance to perform add- and delete tasks within a new thread. Unlike
      * the {@link #write()} method, this method does not lock the database nor does it 
      * create a new transaction when being called. Instead, all changes to be performed
      * are collected and are submitted within a new thread when the returned Write 
      * instance is being closed. You should always use this method within a 
      * try-resources block like this:
      * <pre>
      * try (final Write write = persistence.writeParallel()) {
      *     write.single(myObject);
      *     // ...
      * }
      * </pre>
      * <p>The Write instance returned by this method does <b>not</b> support the 
      * {@link Write#read() read} operation.</p>
      * 
      * @return Write access to the database.
      */
     public Write writeParallel();
     
     public Write write(TransactionCallback cb);
     
     public Write writeParallel(TransactionCallback cb);
 
     public void registerEntity(Class<?> clazz);
 
     public void registerEntityConverter(EntityConverter ec);
 
     public void refresh(Object obj);
 }
