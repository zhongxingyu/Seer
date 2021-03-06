 package com.sun.sgs.impl.service.data.store;
 
 import com.sleepycat.bind.tuple.LongBinding;
 import com.sleepycat.bind.tuple.StringBinding;
 import com.sleepycat.db.CacheFileStats;
 import com.sleepycat.db.Database;
 import com.sleepycat.db.DatabaseConfig;
 import com.sleepycat.db.DatabaseEntry;
 import com.sleepycat.db.DatabaseException;
 import com.sleepycat.db.DatabaseType;
 import com.sleepycat.db.DeadlockException;
 import com.sleepycat.db.Environment;
 import com.sleepycat.db.EnvironmentConfig;
 import com.sleepycat.db.LockDetectMode;
 import com.sleepycat.db.LockMode;
 import com.sleepycat.db.LockNotGrantedException;
import com.sleepycat.db.ErrorHandler;
 import com.sleepycat.db.MessageHandler;
 import com.sleepycat.db.OperationStatus;
 import com.sleepycat.db.RunRecoveryException;
 import com.sleepycat.db.TransactionConfig;
 import com.sun.sgs.app.NameNotBoundException;
 import com.sun.sgs.app.ObjectNotFoundException;
 import com.sun.sgs.app.TransactionConflictException;
 import com.sun.sgs.app.TransactionTimeoutException;
 import com.sun.sgs.impl.service.data.Util;
 import com.sun.sgs.impl.util.LoggerWrapper;
 import com.sun.sgs.service.Transaction;
 import com.sun.sgs.service.TransactionParticipant;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /*
  * XXX: Implement recovery for prepared transactions
  */
 public final class DataStoreImpl implements DataStore, TransactionParticipant {
 
     /** The property that specifies the transaction timeout in milliseconds. */
     private static final String TXN_TIMEOUT_PROPERTY =
 	"com.sun.sgs.txnTimeout";
 
     /** The default transaction timeout in milliseconds. */
     private static final long DEFAULT_TXN_TIMEOUT = 1000;
 
     /** This class name. */
     private static final String CLASSNAME = DataStoreImpl.class.getName();
 
     /**
      * The property that specifies the directory in which to store database
      * files.
      */
     private static final String DIRECTORY_PROPERTY =
 	CLASSNAME + ".directory";
 
     /**
      * The property that specifies the number of object IDs to allocate at one
      * time.
      */
     private static final String ALLOCATION_BLOCK_SIZE_PROPERTY =
 	CLASSNAME + ".allocationBlockSize";
 
     /** The default for the number of object IDs to allocate at one time. */
     private static final int DEFAULT_ALLOCATION_BLOCK_SIZE = 100;
 
     /**
      * The property that specifies the number of transactions between logging
      * database statistics.
      */
     private static final String LOG_STATS_PROPERTY =
 	CLASSNAME + ".logStats";
 
     /** The logger for this class. */
     private static final LoggerWrapper logger =
 	new LoggerWrapper(Logger.getLogger(CLASSNAME));
 
     /** An empty array returned when Berkeley DB returns null for a value. */
     private static final byte[] NO_BYTES = { };
 
     /** A transaction configuration that supports uncommitted reads. */
     private static final TransactionConfig uncommittedReadTxnConfig =
 	new TransactionConfig();
     static {
 	uncommittedReadTxnConfig.setReadUncommitted(true);
     }
 
     /** The directory in which to store database files. */
     private final String directory;
 
     /** The number of object IDs to allocate at one time. */
     private final int allocationBlockSize;
 
     private final int logStats;
     private int logStatsCount;
 
     /** The Berkeley DB environment. */
     private final Environment env;
 
     /** The Berkeley DB database that maps object IDs to object bytes. */
     private final Database ids;
 
     /** The Berkeley DB database that maps name bindings to object IDs. */
     private final Database names;
 
     /** Provides information about the transaction for the current thread. */
     private final ThreadLocal<TxnInfo> threadTxnInfo =
 	new ThreadLocal<TxnInfo>();
 
     /**
      * Object to synchronize on when accessing nextObjectId and
      * lastObjectId.
      */
     private final Object objectIdLock = new Object();
 
     /**
      * The next object ID to use for allocating an object.  Valid if not
      * greater than lastObjectId.
      */
     private long nextObjectId;
 
     /**
      * The last object ID that is free for allocating an object before needing
      * to obtain more numbers from the database.
      */
     private long lastObjectId;
 
     /** Stores transaction information. */
     private static class TxnInfo {
 
 	/** The SGS transaction. */
 	final Transaction txn;
 
 	/** The associated Berkeley DB transaction. */
 	final com.sleepycat.db.Transaction bdbTxn;
 
 	/** Whether preparation of the transaction has started. */
 	boolean prepared;
 
 	/** Whether any changes have been made in this transaction. */
 	boolean modified;
 
 	TxnInfo(Transaction txn, Environment env) throws DatabaseException {
 	    this.txn = txn;
 	    bdbTxn = env.beginTransaction(null, null);
 	}
     }
 
     /** A Berkeley DB message handler that uses logging. */
     private static class LoggingMessageHandler implements MessageHandler {
 	public void message(Environment env, String message) {
 	    logger.log(Level.FINE, "Database message: {0}", message);
 	}
     }
 
     /** A Berkeley DB error handler that uses logging. */
     private static class LoggingErrorHandler implements ErrorHandler {
 	public void error(Environment env, String prefix, String message) {
 	    logger.log(Level.FINE, "Database error message: {0}{1}",
 		       prefix != null ? prefix : "", message);
 	}
     }
 
     /**
      * Creates an instance of this class configured with the specified
      * properties.
      *
      * @param	properties the properties for configuring this instance
      * @throws	DataStoreException if there is a problem with the database
      * @throws	IllegalArgumentException if the <code>DIRECTORY_PROPERTY</code>
      *		property is not specified, or if the value of the
      *		<code>ALLOCATION_BLOCK_SIZE_PROPERTY</code> is not a valid
      *		integer greater than zero
      */
     public DataStoreImpl(Properties properties) {
 	directory = properties.getProperty(DIRECTORY_PROPERTY);
 	if (directory == null) {
 	    throw new IllegalArgumentException("Directory must be specified");
 	}
 	allocationBlockSize = Util.getIntProperty(
 	    properties, ALLOCATION_BLOCK_SIZE_PROPERTY,
 	    DEFAULT_ALLOCATION_BLOCK_SIZE);
 	if (allocationBlockSize < 1) {
 	    throw new IllegalArgumentException(
 		"The allocation block size must be greater than zero");
 	}
 	logStats = Util.getIntProperty(
 	    properties, LOG_STATS_PROPERTY, Integer.MAX_VALUE);
 	com.sleepycat.db.Transaction bdbTxn = null;
 	boolean done = false;
 	try {
 	    env = getEnvironment(properties);
 	    bdbTxn = env.beginTransaction(null, null);
 	    DatabaseConfig createConfig = new DatabaseConfig();
 	    createConfig.setType(DatabaseType.BTREE);
 	    createConfig.setAllowCreate(true);
 	    boolean create = false;
 	    String idsFileName = directory + File.separator + "ids";
 	    Database ids;
 	    try {
 		ids = env.openDatabase(bdbTxn, idsFileName, null, null);
 		DataStoreHeader.verify(ids, bdbTxn);
 	    } catch (FileNotFoundException e) {
 		try {
 		    ids = env.openDatabase(
 			bdbTxn, idsFileName, null, createConfig);
 		} catch (FileNotFoundException e2) {
 		    throw new DataStoreException(
 			"Problem creating database: " + e2.getMessage(),
 			e2);
 		}
 		DataStoreHeader.create(ids, bdbTxn);
 		create = true;
 	    }
 	    this.ids = ids;
 	    try {
 		names = env.openDatabase(
 		    bdbTxn, directory + File.separator + "names", null,
 		    create ? createConfig : null);
 	    } catch (FileNotFoundException e) {
 		throw new DataStoreException("Names database not found");
 	    }
 	    done = true;
 	    bdbTxn.commit();
 	} catch (DatabaseException e) {
 	    throw new DataStoreException(
 		"Problem initializing DataStore: " + e.getMessage(), e);
 	} finally {
 	    if (bdbTxn != null && !done) {
 		try {
 		    bdbTxn.abort();
 		} catch (DatabaseException e) {
 		    logger.logThrow(Level.FINE, "Exception during abort", e);
 		}
 	    }
 	}
     }
 
     /**
      * Obtains a Berkeley DB environment suitable for the specified
      * properties.
      */
     private Environment getEnvironment(Properties properties)
 	throws DatabaseException
     {
         EnvironmentConfig config = new EnvironmentConfig();
 	long timeout = 1000L * Util.getLongProperty(
 	    properties, TXN_TIMEOUT_PROPERTY, DEFAULT_TXN_TIMEOUT);
         config.setAllowCreate(true);
 	config.setErrorHandler(new LoggingErrorHandler());
         config.setInitializeCache(true);
         config.setInitializeLocking(true);
         config.setInitializeLogging(true);
         config.setLockDetectMode(LockDetectMode.MINWRITE);
 	config.setLockTimeout(timeout);
 	config.setMessageHandler(new LoggingMessageHandler());
         config.setRunRecovery(true);
         config.setTransactional(true);
 	config.setTxnTimeout(timeout);
 	config.setTxnWriteNoSync(true);
 	try {
 	    return new Environment(new File(directory), config);
 	} catch (FileNotFoundException e) {
 	    throw new DataStoreException(
 		"DataStore directory does not exist: " + directory);
 	}
     }
 
     /* -- Implement DataStore -- */
 
     /** {@inheritDoc} */
     public long createObject(Transaction txn) {
 	logger.log(Level.FINEST, "createObject txn:{0}", txn);
 	try {
 	    checkTxn(txn);
 	    synchronized (objectIdLock) {
 		if (nextObjectId >= lastObjectId) {
 		    long newNextObjectId;
 		    com.sleepycat.db.Transaction bdbTxn =
 			env.beginTransaction(
 			    null, uncommittedReadTxnConfig);
 		    boolean done = false;
 		    try {
 			newNextObjectId = DataStoreHeader.getNextId(
 			    ids, bdbTxn, allocationBlockSize);
 			done = true;
 			bdbTxn.commit();
 		    } finally {
 			if (!done) {
 			    bdbTxn.abort();
 			}
 		    }
 		    nextObjectId = newNextObjectId;
 		    lastObjectId = newNextObjectId + allocationBlockSize;
 		}
 		return nextObjectId++;
 	    }
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	    throw new AssertionError();
 	}
     }
 
     /** {@inheritDoc} */
     public void markForUpdate(Transaction txn, long id) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST, "markForUpdate txn:{0}, id:{1,number,#}",
 		       txn, id);
 	}
 	/*
 	 * Berkeley DB doesn't seem to provide a way to obtain a write lock
 	 * without reading or writing, so get the object and ask for a write
 	 * lock.  -tjb@sun.com (10/06/2006)
 	 */
 	getObjectInternal(txn, id, true);
     }
 
     /** {@inheritDoc} */
     public byte[] getObject(Transaction txn, long id, boolean forUpdate) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST,
 		       "getObject txn:{0}, id:{1,number,#}, forUpdate:{2}",
 		       txn, id, forUpdate);
 	}
 	return getObjectInternal(txn, id, forUpdate);
     }
 
     /** Implement getObject, without logging. */
     private byte[] getObjectInternal(
 	Transaction txn, long id, boolean forUpdate)
     {
 	checkId(id);
 	try {
 	    TxnInfo txnInfo = checkTxn(txn);
 	    DatabaseEntry key = new DatabaseEntry();
 	    LongBinding.longToEntry(id, key);
 	    DatabaseEntry value = new DatabaseEntry();
 	    OperationStatus status = ids.get(
 		txnInfo.bdbTxn, key, value, forUpdate ? LockMode.RMW : null);
 	    if (status == OperationStatus.NOTFOUND) {
 		throw new ObjectNotFoundException("Object not found");
 	    } else if (status != OperationStatus.SUCCESS) {
 		throw new DataStoreException(
 		    "Getting object failed: " + status);
 	    }
 	    byte[] result = value.getData();
 	    /* Berkeley DB returns null if the data is empty. */
 	    return result != null ? result : NO_BYTES;
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	    throw new AssertionError();
 	}
     }
 
     /** {@inheritDoc} */
     public void setObject(Transaction txn, long id, byte[] data) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST, "setObject txn:{0}, id:{1,number,#}",
 		       txn, id);
 	}
 	checkId(id);
 	if (data == null) {
 	    throw new NullPointerException("The data must not be null");
 	}
 	try {
 	    TxnInfo txnInfo = checkTxn(txn);
 	    DatabaseEntry key = new DatabaseEntry();
 	    LongBinding.longToEntry(id, key);
 	    DatabaseEntry value = new DatabaseEntry(data);
 	    OperationStatus status = ids.put(txnInfo.bdbTxn, key, value);
 	    if (status != OperationStatus.SUCCESS) {
 		throw new DataStoreException(
 		    "Setting object failed: " + status);
 	    }
 	    txnInfo.modified = true;
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	}
     }
 
     /** {@inheritDoc} */
     public void removeObject(Transaction txn, long id) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST, "removeObject txn:{0}, id:{1,number,#}",
 		       txn, id);
 	}
 	checkId(id);
 	try {
 	    TxnInfo txnInfo = checkTxn(txn);
 	    DatabaseEntry key = new DatabaseEntry();
 	    LongBinding.longToEntry(id, key);
 	    OperationStatus status = ids.delete(txnInfo.bdbTxn, key);
 	    if (status == OperationStatus.NOTFOUND) {
 		throw new ObjectNotFoundException("Object not found: " + id);
 	    } else if (status != OperationStatus.SUCCESS) {
 		throw new DataStoreException(
 		    "Removing object failed: " + status);
 	    }
 	    txnInfo.modified = true;
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	}
     }
 
     /** {@inheritDoc} */
     public long getBinding(Transaction txn, String name) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST, "getBinding txn:{0}, name:{1}", txn, name);
 	}
 	if (name == null) {
 	    throw new NullPointerException("Name must not be null");
 	}
 	try {
 	    TxnInfo txnInfo = checkTxn(txn);
 	    DatabaseEntry key = new DatabaseEntry();
 	    StringBinding.stringToEntry(name, key);
 	    DatabaseEntry value = new DatabaseEntry();
 	    OperationStatus status =
 		names.get(txnInfo.bdbTxn, key, value, null);
 	    if (status == OperationStatus.NOTFOUND) {
 		throw new NameNotBoundException("Name not bound: " + name);
 	    } else if (status != OperationStatus.SUCCESS) {
 		throw new DataStoreException(
 		    "Getting binding failed: " + status);
 	    }
 	    return LongBinding.entryToLong(value);
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	    throw new AssertionError();
 	}
     }
 
     /** {@inheritDoc} */
     public void setBinding(Transaction txn, String name, long id) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST, "setBinding txn:{0}, name:{1}", txn, name);
 	}
 	if (name == null) {
 	    throw new NullPointerException("Name must not be null");
 	}
 	checkId(id);
 	try {
 	    TxnInfo txnInfo = checkTxn(txn);
 	    DatabaseEntry key = new DatabaseEntry();
 	    StringBinding.stringToEntry(name, key);
 	    DatabaseEntry value = new DatabaseEntry();
 	    LongBinding.longToEntry(id, value);
 	    OperationStatus status = names.put(txnInfo.bdbTxn, key, value);
 	    if (status != OperationStatus.SUCCESS) {
 		throw new DataStoreException(
 		    "Setting binding failed: " + status);
 	    }
 	    txnInfo.modified = true;
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	}
     }
 
     /** {@inheritDoc} */
     public void removeBinding(Transaction txn, String name) {
 	if (logger.isLoggable(Level.FINEST)) {
 	    logger.log(Level.FINEST, "removeBinding txn:{0}, name:{1}",
 		       txn, name);
 	}
 	if (name == null) {
 	    throw new NullPointerException("Name must not be null");
 	}
 	try {
 	    TxnInfo txnInfo = checkTxn(txn);
 	    DatabaseEntry key = new DatabaseEntry();
 	    StringBinding.stringToEntry(name, key);
 	    OperationStatus status = names.delete(txnInfo.bdbTxn, key);
 	    if (status == OperationStatus.NOTFOUND) {
 		throw new NameNotBoundException("Name not bound: " + name);
 	    } else if (status != OperationStatus.SUCCESS) {
 		throw new DataStoreException(
 		    "Removing binding failed: " + status);
 	    }
 	    txnInfo.modified = true;
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	}
     }
 
     /* -- Implement TransactionParticipant -- */
 
     /** {@inheritDoc} */
    public String getIdentifier() {
	return toString();
    }

    /** {@inheritDoc} */
     public boolean prepare(Transaction txn) {
 	logger.log(Level.FINER, "prepare txn:{0}", txn);
 	if (txn == null) {
 	    throw new NullPointerException("Transaction must not be null");
 	}
 	TxnInfo txnInfo = threadTxnInfo.get();
 	if (txnInfo == null) {
 	    throw new IllegalStateException("Transaction is not active");
 	} else if (!txnInfo.txn.equals(txn)) {
 	    throw new IllegalStateException("Wrong transaction");
 	} else if (txnInfo.prepared) {
 	    throw new IllegalStateException(
 		"Transaction has already been prepared");
 	} else {
 	    txnInfo.prepared = true;
 	}
 	boolean done = false;
 	try {
 	    if (txnInfo.modified) {
 		byte[] id = txn.getId();
 		byte[] gid = new byte[128];
 		System.arraycopy(id, 0, gid, 128 - id.length, id.length);
 		txnInfo.bdbTxn.prepare(gid);
 	    }
 	    done = true;
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	    throw new AssertionError();
 	} finally {
 	    if (!done) {
 		txnInfo.prepared = false;
 	    }
 	}
 	return !txnInfo.modified;
     }
 
     /** {@inheritDoc} */
     public void commit(Transaction txn) {
 	logger.log(Level.FINER, "commit txn:{0}", txn);
 	if (txn == null) {
 	    throw new NullPointerException("Transaction must not be null");
 	}
 	TxnInfo txnInfo = threadTxnInfo.get();
 	if (txnInfo == null) {
 	    throw new IllegalStateException("Transaction is not active");
 	} else if (!txnInfo.txn.equals(txn)) {
 	    throw new IllegalStateException("Wrong transaction");
 	} else if (!txnInfo.prepared) {
 	    throw new IllegalStateException(
 		"Transaction has not been prepared");
 	} else {
 	    threadTxnInfo.set(null);
 	}
 	try {
 	    txnInfo.bdbTxn.commit();
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	} catch (RuntimeException e) {
 	    throw new DataStoreException(e.getMessage(), e);
 	}
     }
 
     /** {@inheritDoc} */
     public void prepareAndCommit(Transaction txn) {
 	logger.log(Level.FINER, "prepareAndCommit txn:{0}", txn);
 	if (txn == null) {
 	    throw new NullPointerException("Transaction must not be null");
 	}
 	TxnInfo txnInfo = threadTxnInfo.get();
 	if (txnInfo == null) {
 	    throw new IllegalStateException("Transaction is not active");
 	} else if (!txnInfo.txn.equals(txn)) {
 	    throw new IllegalStateException("Wrong transaction");
 	} else if (txnInfo.prepared) {
 	    throw new IllegalStateException(
 		"Transaction has already been prepared");
 	} else {
 	    threadTxnInfo.set(null);
 	}
 	try {
 	    txnInfo.bdbTxn.commit();
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	} catch (RuntimeException e) {
 	    throw new DataStoreException(e.getMessage(), e);
 	}
     }
 
     /** {@inheritDoc} */
     public void abort(Transaction txn) {
 	logger.log(Level.FINER, "abort txn:{0}", txn);
 	if (txn == null) {
 	    throw new NullPointerException("Transaction must not be null");
 	}
 	TxnInfo txnInfo = threadTxnInfo.get();
 	if (txnInfo == null) {
 	    throw new IllegalStateException("Transaction is not active");
 	}
 	threadTxnInfo.set(null);
 	if (!txnInfo.txn.equals(txn)) {
 	    throw new IllegalStateException("Wrong transaction");
 	}
 	try {
 	    txnInfo.bdbTxn.abort();
 	} catch (DatabaseException e) {
 	    handleDatabaseException(e);
 	} catch (RuntimeException e) {
 	    throw new DataStoreException(e.getMessage(), e);
 	}
     }
 
     /* -- Other public methods -- */
 
     /**
      * Returns a string representation of this object.
      *
      * @return	a string representation of this object
      */
     public String toString() {
 	return "DataStoreImpl[directory=" + directory + "]";
     }
 
     /* -- Private methods -- */
 
     /** Checks that the object ID argument is not negative. */
     private void checkId(long id) {
 	if (id < 0) {
 	    throw new IllegalArgumentException(
 		"Object ID must not be negative");
 	}
     }
 
     /**
      * Checks that the transaction is in progress for an operation other than
      * prepare or commit.
      */
     private TxnInfo checkTxn(Transaction txn) throws DatabaseException {
 	if (txn == null) {
 	    throw new NullPointerException("Transaction must not be null");
 	}
 	TxnInfo txnInfo = threadTxnInfo.get();
 	if (txnInfo == null) {
 	    txn.join(this);
 	    txnInfo = new TxnInfo(txn, env);
 	    threadTxnInfo.set(txnInfo);
 	    if (++logStatsCount >= logStats) {
 		logStatsCount = 0;
 		logStats(txnInfo);
 	    }
 	} else if (!txnInfo.txn.equals(txn)) {
 	    throw new IllegalStateException("Wrong transaction");
 	} else if (txnInfo.prepared) {
 	    throw new IllegalStateException(
 		"Transaction has been prepared");
 	}
 	return txnInfo;
     }
 
     /**
      * Takes the correct action for a Berkeley DB DatabaseException thrown
      * during an operation.  Converts to the correct SGS exceptions, and throws
      * an Error if recovery is needed.
      */
     private void handleDatabaseException(DatabaseException e) {
 	if (e instanceof LockNotGrantedException) {
 	    throw new TransactionTimeoutException(e.getMessage(), e);
 	} else if (e instanceof DeadlockException) {
 	    throw new TransactionConflictException(e.getMessage(), e);
 	} else if (e instanceof RunRecoveryException) {
 	    /*
 	     * It is tricky to clean up the data structures in this instance in
 	     * order to reopen the Berkeley DB databases, because it's hard to
 	     * know when they are no longer in use.  It's OK to catch this
 	     * Error and create a new DataStoreImpl instance, but this instance
 	     * is dead.  -tjb@sun.com (10/19/2006)
 	     */
 	    throw new Error(
 		"Database requires recovery -- need to restart the server " +
 		"or create a new instance of DataStoreImpl",
 		e);
 	} else {
 	    throw new DataStoreException(e.getMessage(), e);
 	}
     }
 
     /** Log statistics using the specified transaction. */
     private void logStats(TxnInfo txnInfo) throws DatabaseException {
 	if (logger.isLoggable(Level.FINE)) {
 	    logger.log(Level.FINE, "Ids database: {0}",
 		       ids.getStats(txnInfo.bdbTxn, null));
 	    logger.log(Level.FINE, "Names database: {0}",
 		       names.getStats(txnInfo.bdbTxn, null));
 	    CacheFileStats[] stats = env.getCacheFileStats(null);
 	    for (int i = 0; i < stats.length; i++) {
 		logger.log(Level.FINE, "{0}", stats[i]);
 	    }
 	    logger.log(Level.FINE, "{0}", env.getCacheStats(null));
 	    logger.log(Level.FINE, "{0}", env.getLockStats(null));
 	    logger.log(Level.FINE, "{0}", env.getLogStats(null));
 	    logger.log(Level.FINE, "{0}", env.getMutexStats(null));
 	    logger.log(Level.FINE, "{0}", env.getTransactionStats(null));
 	}
     }
 }
