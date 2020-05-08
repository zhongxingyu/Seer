 package pt.com.gcs.messaging;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.concurrent.Sleep;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.CriticalErrors;
 import pt.com.gcs.messaging.QueueProcessor.ForwardResult;
 import pt.com.gcs.messaging.QueueProcessor.ForwardResult.Result;
 import pt.com.gcs.messaging.serialization.MessageMarshaller;
 
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
 
 	private static final int MAX_REDELIVERY_PER_MESSAGE = 3;
 
 	private Environment env;
 
 	private Database messageDb;
 
 	private String primaryDbName;
 
 	private QueueProcessor queueProcessor;
 
 	private final AtomicBoolean isMarkedForDeletion = new AtomicBoolean(false);
 
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
 
 		ByteArrayBinding bab = new ByteArrayBinding();
 
 		byte[] marshallBDBMessage;
 		try
 		{
 			marshallBDBMessage = MessageMarshaller.marshallBDBMessage(bdbm);
 			if (marshallBDBMessage != null)
 			{
 				bab.objectToEntry(marshallBDBMessage, data);
 			}
 			else
 			{
 
 				throw new Exception("MessageMarshaller.marshallBDBMessage returned null");
 			}
 		}
 		catch (Throwable e)
 		{
 			log.error("Serialization failed", e);
 			return null;
 		}
 
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
 		CriticalErrors.exitIfCritical(rt);
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
 
 	private volatile boolean recoveryRunning = false;
 
 	protected void recoverMessages()
 	{
 
 		if (isMarkedForDeletion.get())
 			return;
 
 		long now = System.currentTimeMillis();
 
 		int i0 = 0; // delivered
 		int j0 = 0; // failed deliver
 		int k0 = 0; // redelivered messages
 		int e0 = 0; // expired messages
 		int a0 = 0; // delivered messages that don't require ACK
 
 		recoveryRunning = true;
 
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
 					bdbm = MessageMarshaller.unmarshallBDBMessage(bdata);
 					if (bdbm == null)
 					{
 						log.info("MessageMarshaller.unmarshallBDBMessage returned null");
 						continue;
 					}
 					msg = bdbm.getMessage();
 				}
 				catch (Throwable e)
 				{
 					cursorDelete(msg_cursor);
 					continue;
 				}
 
 				long k = LongBinding.entryToLong(key);
 				msg.setMessageId(InternalMessage.getBaseMessageId() + k);
 
 				boolean preferLocalConsumer = bdbm.getPreferLocalConsumer();
 				long reserveTimeout = bdbm.getReserveTimeout();
 				final boolean isReserved = reserveTimeout > now;
 
 				if (!isReserved)
 				{
 					if (now > msg.getExpiration())
 					{
 						cursorDelete(msg_cursor);
 						e0++;
 						dumpMessage(msg);
 					}
 					else
 					{
 						try
 						{
 							bdbm.setPreferLocalConsumer(false);
 
 							int tries = 0;
 							// long reserveTime = -1;
 
 							ForwardResult result = null;
 
 							do
 							{
 								result = queueProcessor.forward(msg, preferLocalConsumer);
 								preferLocalConsumer = false;
 								++tries;
 							}
 							while ((result.result == Result.FAILED) && (tries != MAX_REDELIVERY_PER_MESSAGE));
 
 							if (result.result == Result.FAILED)
 							{
 								log.info("Could not deliver message. Queue: '{}',  Id: '{}'.", msg.getDestination(), msg.getMessageId());
 								dumpMessage(msg);
 
 								++j0;
 
 								break;
 							}
 							else
 							{
 								if (bdbm.getReserveTimeout() != 0)
 								{
 									++k0; // It's a re-delivery
 								}
 
 								if (result.result == Result.SUCCESS)
 								{
 									bdbm.setReserveTimeout(now + result.time);
 									msg_cursor.put(key, buildDatabaseEntry(bdbm));
 									++i0;
 								}
 								else
 								// result is Result.NOT_ACKNOWLEDGE
 								{
 									cursorDelete(msg_cursor);
 									++a0;
 								}
 							}
 						}
 						catch (Throwable t)
 						{
 							log.error("Error recovering messages", t);
 							t.printStackTrace();
 							break;
 						}
 					}
 				}
 			}
 
 			if (log.isDebugEnabled())
 			{
 				log.debug(String.format("Queue '%s' processing summary; Delivered: %s; Failed delivered: %s; Expired: %s; Delivered messages that don't require ack: %s", queueProcessor.getDestinationName(), i0, j0, e0, a0));
 			}
 
 			if (e0 > 0)
 			{
 				log.warn("Number of expired messages for queue '{}': {}", queueProcessor.getDestinationName(), e0);
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
 			recoveryRunning = false;
 		}
 	}
 
 	public void deleteExpiredMessages()
 	{
 
 		if (isMarkedForDeletion.get())
 			return;
 
		log.info("Runing BDBStorage.deleteExpiredMessages()");
 
 		long now = System.currentTimeMillis();
 
 		int e0 = 0; // expired messages
 
 		Cursor msg_cursor = null;
 
 		try
 		{
 			msg_cursor = messageDb.openCursor(null, null);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			while (!recoveryRunning && (msg_cursor.getNext(key, data, null) == OperationStatus.SUCCESS))
 			{
 				if (isMarkedForDeletion.get())
 					break;
 
 				byte[] bdata = data.getData();
 
 				BDBMessage bdbm = null;
 				InternalMessage msg = null;
 
 				try
 				{
 					bdbm = MessageMarshaller.unmarshallBDBMessage(bdata);
 					if (bdbm == null)
 					{
 						log.info("MessageMarshaller.unmarshallBDBMessage returned null");
 						continue;
 					}
 					msg = bdbm.getMessage();
 				}
 				catch (Throwable e)
 				{
 					cursorDelete(msg_cursor);
 					continue;
 				}
 
 				long k = LongBinding.entryToLong(key);
 				msg.setMessageId(InternalMessage.getBaseMessageId() + k);
 
 				long reserveTimeout = bdbm.getReserveTimeout();
 				final boolean isReserved = reserveTimeout > now;
 
 				if (!isReserved)
 				{
 					if (now > msg.getExpiration())
 					{
 						cursorDelete(msg_cursor);
 						e0++;
 						dumpMessage(msg);
 					}
 				}
 			}
 			if (e0 > 0)
 			{
 				log.warn("Number of expired messages for queue '{}': {}", queueProcessor.getDestinationName(), e0);
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
