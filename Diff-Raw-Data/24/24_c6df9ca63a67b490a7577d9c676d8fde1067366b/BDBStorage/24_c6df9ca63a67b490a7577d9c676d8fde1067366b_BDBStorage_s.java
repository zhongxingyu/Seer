 package pt.com.gcs.messaging;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sleepycat.bind.ByteArrayBinding;
 import com.sleepycat.bind.tuple.LongBinding;
 import com.sleepycat.je.Cursor;
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.DatabaseException;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.LockConflictException;
 import com.sleepycat.je.OperationStatus;
 
 /**
  * BDBStorage encapsulates database access logic.
  * 
  */
 
 public class BDBStorage
 {
 	private static Logger log = LoggerFactory.getLogger(BDBStorage.class);
 
 	private Environment env;
 
 	private Database messageDb;
 
 	private String primaryDbName;
 
 	private QueueProcessor queueProcessor;
 
 	private final AtomicBoolean isMarkedForDeletion = new AtomicBoolean(false);
 
 	private static final int MAX_REDELIVERY_PER_MESSAGE = 3;
 
	private long nextCycleTime = System.currentTimeMillis();
 
 	public BDBStorage(QueueProcessor qp)
 	{
 		try
 		{
 			if (isMarkedForDeletion.get())
 				return;
 			queueProcessor = qp;
 			primaryDbName = queueProcessor.getDestinationName();
 
 			env = BDBEnviroment.get();
 
 			DatabaseConfig dbConfig = new DatabaseConfig();
 			dbConfig.setTransactional(false);
 			dbConfig.setAllowCreate(true);
 			dbConfig.setSortedDuplicates(false);
 			dbConfig.setBtreeComparator(BDBMessageComparator.class);
 			messageDb = env.openDatabase(null, primaryDbName, dbConfig);
 
 			log.info("Storage for queue '{}' is ready.", queueProcessor.getDestinationName());
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 			Shutdown.now();
 		}
 	}
 
 	private DatabaseEntry buildDatabaseEntry(BDBMessage bdbm) throws IOException
 	{
 		DatabaseEntry data = new DatabaseEntry();
 
 		UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
 		ObjectOutputStream oout = new ObjectOutputStream(bout);
 		bdbm.writeExternal(oout);
 		oout.flush();
 
 		ByteArrayBinding bab = new ByteArrayBinding();
 		byte[] bdata = bout.toByteArray();
 
 		bab.objectToEntry(bdata, data);
 
 		return data;
 	}
 
 	private void closeDatabase(Database db)
 	{
 		try
 		{
 			BDBEnviroment.sync();
 			String dbName = db.getDatabaseName();
 			log.info("Try to close db '{}'", dbName);
 			db.close();
 			log.info("Closed db '{}'", dbName);
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 	}
 
 	private void closeDbCursor(Cursor msg_cursor)
 	{
 		if (msg_cursor != null)
 		{
 			try
 			{
 				msg_cursor.close();
 			}
 			catch (Throwable t)
 			{
 				dealWithError(t, false);
 			}
 		}
 	}
 
 	private void cursorDelete(Cursor msg_cursor) throws DatabaseException
 	{
 		msg_cursor.delete();
 		queueProcessor.decrementQueuedMessagesCount();
 	}
 
 	private void dealWithError(Throwable t, boolean rethrow)
 	{
 		Throwable rt = ErrorAnalyser.findRootCause(t);
 		log.error(rt.getMessage(), rt);
 		ErrorAnalyser.exitIfOOM(rt);
 		if (rethrow)
 		{
 			throw new RuntimeException(rt);
 		}
 	}
 
 	private void dumpMessage(final InternalMessage msg)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Could not deliver message. Dump: {}", msg.toString());
 		}
 	}
 
 	private void removeDatabase(String dbName)
 	{
 		int retryCount = 0;
 
 		while (retryCount < 5)
 		{
 			try
 			{
 				BDBEnviroment.sync();
 				log.info("Try to remove db '{}'", dbName);
 				env.truncateDatabase(null, dbName, false);
 				env.removeDatabase(null, dbName);
 				BDBEnviroment.sync();
 				log.info("Storage for queue '{}' was removed", queueProcessor.getDestinationName());
 
 				break;
 			}
 			catch (Throwable t)
 			{
 				retryCount++;
 				log.error(t.getMessage());
 				Sleep.time(2500);
 			}
 		}
 	}
 
 	protected long count()
 	{
 		if (isMarkedForDeletion.get())
 			return 0;
 
 		try
 		{
 			return messageDb.count();
 		}
 		catch (DatabaseException e)
 		{
 			dealWithError(e, false);
 			return 0;
 		}
 	}
 
 	protected boolean deleteMessage(String msgId)
 	{
 		if (isMarkedForDeletion.get())
 			return false;
 
 		DatabaseEntry key = new DatabaseEntry();
 		long k = Long.parseLong(msgId.substring(33));
 		LongBinding.longToEntry(k, key);
 
 		int count = 5;
 		LockConflictException lastDeadlockException = null;
 		do
 		{
 			try
 			{
 				OperationStatus op = messageDb.delete(null, key);
 
 				if (op.equals(OperationStatus.SUCCESS))
 				{
 					return true;
 				}
 				else
 				{
 					return false;
 				}
 			}
 			catch (LockConflictException de)
 			{
 				--count;
 				lastDeadlockException = de;
 				log.error("DeadlockException. Number of retries left: " + count, de);
 			}
 			catch (DatabaseException e)
 			{
 				dealWithError(e, true);
 				return false;
 			}
 		}
 		while (count != 0);
 
 		// Stop trying to deal with DeadlockException
 		dealWithError(lastDeadlockException, true);
 
 		return false;
 	}
 
 	protected void deleteQueue()
 	{
 		isMarkedForDeletion.set(true);
 		Sleep.time(2500);
 
 		closeDatabase(messageDb);
 
 		removeDatabase(primaryDbName);
 	}
 
 	protected long getLastSequenceValue()
 	{
 		if (isMarkedForDeletion.get())
 			return 0L;
 
 		Cursor msg_cursor = null;
 		long seqValue = 0L;
 
 		try
 		{
 			msg_cursor = messageDb.openCursor(null, null);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			msg_cursor.getLast(key, data, null);
 
 			seqValue = LongBinding.entryToLong(key);
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 		finally
 		{
 			closeDbCursor(msg_cursor);
 		}
 		return seqValue;
 	}
 
 	AtomicInteger entryCount = new AtomicInteger();
 
 	public void insert(InternalMessage msg, long sequence, boolean preferLocalConsumer)
 	{
 		if (isMarkedForDeletion.get())
 			return;
 
 		try
 		{
 			BDBMessage bdbm = new BDBMessage(msg, sequence, preferLocalConsumer);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = buildDatabaseEntry(bdbm);
 
 			LongBinding.longToEntry(sequence, key);
 
 			if (messageDb.put(null, key, data) != OperationStatus.SUCCESS)
 			{
 				log.error("Failed to insert message in queue '{}'", this.queueProcessor.getDestinationName());
 			}
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, true);
 		}
 
 	}
 
 	// Added for compatibility reasons --- BEGIN
 	public void insert(InternalMessage msg, long sequence, boolean preferLocalConsumer, long reserveTimeout)
 	{
 		if (isMarkedForDeletion.get())
 			return;
 		try
 		{
 			BDBMessage bdbm = new BDBMessage(msg, sequence, preferLocalConsumer, reserveTimeout);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = buildDatabaseEntry(bdbm);
 
 			LongBinding.longToEntry(sequence, key);
 
 			if (messageDb.put(null, key, data) != OperationStatus.SUCCESS)
 			{
 				log.error("Failed to insert message in queue '{}'", this.queueProcessor.getDestinationName());
 			}
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, true);
 		}
 	}
 
 	// Added for compatibility reasons --- END
 
 	protected void recoverMessages()
 	{
 
 		if (isMarkedForDeletion.get())
 			return;
 
 		long now = System.currentTimeMillis();
 
 		if (nextCycleTime > now)
 		{
 			return;
 		}
 
 		int i0 = 0; // delivered
 		int j0 = 0; // failed deliver
 		int k0 = 0; // redelivered messages
 
 		Cursor msg_cursor = null;
 
 		try
 		{
 			msg_cursor = messageDb.openCursor(null, null);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			while ((msg_cursor.getNext(key, data, null) == OperationStatus.SUCCESS) && queueProcessor.hasRecipient())
 			{
 				if (isMarkedForDeletion.get())
 					break;
 
 				byte[] bdata = data.getData();
 
 				BDBMessage bdbm = null;
 				InternalMessage msg = null;
 
 				try
 				{
 					bdbm = BDBMessage.fromByteArray(bdata);
 					msg = bdbm.getMessage();
 				}
 				catch (Throwable e)
 				{
 					cursorDelete(msg_cursor);
 					continue;
 				}
 
 				long k = LongBinding.entryToLong(key);
 				msg.setMessageId(InternalMessage.getBaseMessageId() + k);
 
				final boolean preferLocalConsumer = bdbm.getPreferLocalConsumer();
 				long reserveTimeout = bdbm.getReserveTimeout();
 				final boolean isReserved = reserveTimeout > now;
 
 				if (!isReserved)
 				{
 					if (now > msg.getExpiration())
 					{
 						cursorDelete(msg_cursor);
 						log.warn("Expired message: '{}' id: '{}'", msg.getDestination(), msg.getMessageId());
 						dumpMessage(msg);
 					}
 					else
 					{
 						try
 						{
 							bdbm.setPreferLocalConsumer(false);
 
 							int tries = 0;
 							long reserveTime = -1;
 
 							do
 							{
 								reserveTime = queueProcessor.forward(msg, preferLocalConsumer);
 							}
							while (!(reserveTime > 0) && ((++tries) != MAX_REDELIVERY_PER_MESSAGE));
 
 							if (reserveTime > 0)
 							{
 								if (bdbm.getReserveTimeout() != 0)
 								{
 									++k0; // It's a re-delivery
 								}
 
 								bdbm.setReserveTimeout(reserveTime + now);
 								msg_cursor.put(key, buildDatabaseEntry(bdbm));

 								++i0;
 							}
 							else
 							{
								
 								log.info("Could not deliver message. Queue: '{}',  Id: '{}'", msg.getDestination(), msg.getMessageId());

 								dumpMessage(msg);
 
 								++j0;
								
 								nextCycleTime = System.currentTimeMillis() + 1000;
 								break;
 							}

 						}
 						catch (Throwable t)
 						{
 							log.error(t.getMessage());
 							break;
 						}
 					}
 				}
 			}
 
 			if (log.isDebugEnabled())
 			{
 				log.debug(String.format("Queue '%s' processing summary; Delivered: %s; Failed delivered: %s", queueProcessor.getDestinationName(), i0, j0));
 			}
 
 			if (k0 > 0)
 			{
 				log.info("Number of redelivered messages for queue '{}': {}", queueProcessor.getDestinationName(), k0);
 			}
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 		finally
 		{
 			closeDbCursor(msg_cursor);
 		}
 	}
 }
