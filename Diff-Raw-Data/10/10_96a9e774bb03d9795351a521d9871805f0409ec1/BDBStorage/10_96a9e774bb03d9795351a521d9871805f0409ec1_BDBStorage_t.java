 package pt.com.gcs.messaging;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.cryto.MD5;
 import org.caudexorigo.io.UnsynchByteArrayOutputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sleepycat.bind.ByteArrayBinding;
 import com.sleepycat.bind.tuple.LongBinding;
 import com.sleepycat.bind.tuple.StringBinding;
 import com.sleepycat.je.Cursor;
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.DatabaseException;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.OperationStatus;
 import com.sleepycat.je.SecondaryConfig;
 import com.sleepycat.je.SecondaryDatabase;
 
 class BDBStorage
 {
 	private static Logger log = LoggerFactory.getLogger(BDBStorage.class);
 
 	private static final int MAX_DELIVERY_COUNT = 25;
 
 	private Environment env;
 
 	private Database messageDb;
 
 	private SecondaryDatabase midDb;
 
 	private String dbName;
 
 	private QueueProcessor queueProcessor;
 
 	private final AtomicBoolean isMarkedForDeletion = new AtomicBoolean(false);
 
 	public BDBStorage(QueueProcessor qp)
 	{
 		try
 		{
 			queueProcessor = qp;
 			dbName = MD5.getHashString(queueProcessor.getDestinationName());
 
 			env = BDBEnviroment.get();
 
 			DatabaseConfig dbConfig = new DatabaseConfig();
 			dbConfig.setTransactional(false);
 			dbConfig.setAllowCreate(true);
 			dbConfig.setSortedDuplicates(false);
 			dbConfig.setBtreeComparator(BDBMessageComparator.class);
 			messageDb = env.openDatabase(null, dbName, dbConfig);
 
 			SecondaryConfig secConfig = new SecondaryConfig();
 			secConfig.setTransactional(false);
 			secConfig.setAllowCreate(true);
 			secConfig.setSortedDuplicates(false);
 			secConfig.setKeyCreator(new BDBMessageKeyCreator());
 
 			midDb = env.openSecondaryDatabase(null, "sec:" + dbName, messageDb, secConfig);
 
 			log.info("Storage for queue '{}' is ready.", queueProcessor.getDestinationName());
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 			Shutdown.now();
 		}
 	}
 
 	protected boolean deleteMessage(String msgId)
 	{
 		if (isMarkedForDeletion.get())
 			return false;
 
 		DatabaseEntry key = new DatabaseEntry();
 		StringBinding.stringToEntry(msgId, key);
 		try
 		{
 			OperationStatus op = midDb.delete(null, key);
 
 			if (op.equals(OperationStatus.SUCCESS))
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (DatabaseException e)
 		{
 			dealWithError(e, true);
 			return false;
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
 
 	protected void insert(Message msg, long sequence, int deliveryCount, boolean localConsumersOnly)
 	{
 		if (isMarkedForDeletion.get())
 			return;
 
 		try
 		{
 			BDBMessage bdbm = new BDBMessage(msg, sequence, deliveryCount, localConsumersOnly);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = buildDatabaseEntry(bdbm);
 
 			LongBinding.longToEntry(sequence, key);
 
 			messageDb.put(null, key, data);
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, true);
 		}
 
 	}
 
 	private DatabaseEntry buildDatabaseEntry(BDBMessage bdbm) throws IOException
 	{
 		DatabaseEntry data = new DatabaseEntry();
 
 		UnsynchByteArrayOutputStream bout = new UnsynchByteArrayOutputStream();
 		ObjectOutputStream oout = new ObjectOutputStream(bout);
 		bdbm.writeExternal(oout);
 		oout.flush();
 
 		ByteArrayBinding bab = new ByteArrayBinding();
 		byte[] bdata = bout.toByteArray();
 
 		bab.objectToEntry(bdata, data);
 
 		return data;
 	}
 
 	protected Message poll()
 	{
 		if (isMarkedForDeletion.get())
 			return null;
 
 		Cursor msg_cursor = null;
 
 		try
 		{
 			msg_cursor = messageDb.openCursor(null, null);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			while (msg_cursor.getNext(key, data, null) == OperationStatus.SUCCESS)
 			{
				byte[] bdata = data.getData();
				BDBMessage bdbm = BDBMessage.fromByteArray(bdata);
				final Message msg = bdbm.getMessage();
				
				String msgId = msg.getMessageId();
 				if (!queueProcessor.getReservedMessages().contains(msgId))
 				{
 					queueProcessor.getReservedMessages().add(msgId);
 					return msg;
 				}
 			}
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 		finally
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
 		return null;
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
 		return seqValue;
 	}
 
 	protected void recoverMessages()
 	{
 		if (isMarkedForDeletion.get())
 			return;
 
 		long c0 = System.currentTimeMillis();
 
 		Cursor msg_cursor = null;
 		int batchCount = queueProcessor.batchCount.incrementAndGet();
 		boolean redelivery = ((batchCount % 10) == 0);
 
 		if (redelivery)
 		{
 			queueProcessor.batchCount.set(0);
 		}
 
 		try
 		{
 			msg_cursor = messageDb.openCursor(null, null);
 
 			long c1 = System.currentTimeMillis();
 			long d0 = (c1 - c0);
 
 			DatabaseEntry key = new DatabaseEntry();
 			DatabaseEntry data = new DatabaseEntry();
 
 			int i0 = 0;
 			int j0 = 0;
 
 			while (msg_cursor.getNext(key, data, null) == OperationStatus.SUCCESS)
 			{
 				if (isMarkedForDeletion.get())
 					break;
 
 				byte[] bdata = data.getData();
 				BDBMessage bdbm = BDBMessage.fromByteArray(bdata);
 				final Message msg = bdbm.getMessage();
 				final int deliveryCount = bdbm.getDeliveryCount();
 				final boolean localConsumersOnly = bdbm.isLocalConsumersOnly();
 
 				if ((deliveryCount < 1) || redelivery)
 				{
 					if (!queueProcessor.getReservedMessages().contains(msg.getMessageId()))
 					{
 						bdbm.setDeliveryCount(deliveryCount + 1);
 						msg_cursor.put(key, buildDatabaseEntry(bdbm));
 
 						if (processMessage(msg, deliveryCount, localConsumersOnly))
 						{
 							i0++;
 						}
 						else
 						{
 							j0++;
 							bdbm.setDeliveryCount(deliveryCount);
 							msg_cursor.put(key, buildDatabaseEntry(bdbm));
 						}
 					}
 				}
 			}
 
 			long c2 = System.currentTimeMillis();
 			long d1 = (c2 - c1);
 
 			if (log.isDebugEnabled())
 			{
 				log.debug(String.format("Queue '%s' processing summary; Retrieval time: %s ms; Delivery time: %s ms; Delivered: %s; Failed delivered: %s, Redelivery: %s;", queueProcessor.getDestinationName(), d0, d1, i0, j0, redelivery));
 			}
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 		finally
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
 
 	private boolean processMessage(final Message msg, int deliveryCount, final boolean localConsumersOnly)
 	{
 		long mark = System.currentTimeMillis();
 
 		if ((mark <= msg.getExpiration()) && (deliveryCount <= MAX_DELIVERY_COUNT))
 		{
 			if (!queueProcessor.forward(msg, localConsumersOnly))
 			{
 				if (log.isDebugEnabled())
 				{
 					log.debug("Could not deliver message. Dump: {}", msg.toString());
 				}
 				return false;
 			}
 			return true;
 		}
 		else
 		{
 			log.warn("Expired or overdelivered message: '{}' id: '{}'", msg.getDestination(), msg.getMessageId());
 			deleteMessage(msg.getMessageId());
 			return false;
 		}
 	}
 
 	protected void deleteQueue()
 	{
 		isMarkedForDeletion.set(true);
 		Sleep.time(2500);
 		truncateDatabase();
 	}
 
 	private void truncateDatabase()
 	{
 		long cnt = -1;
 
 		while (cnt < 0)
 		{
 			try
 			{
 				closeDatabase(messageDb);
 				closeDatabase(midDb);
 				BDBEnviroment.sync();
 				// cnt = env.truncateDatabase(null, dbName, true);
 				// log.info("Discard {} records for queue '{}'", cnt,
 				// queueProcessor.getDestinationName());
 				cnt = 10L;
 			}
 			catch (Throwable t)
 			{
 				System.err.println("truncateDatabase.ERROR: " + t.getMessage());
 				Sleep.time(2500);
 			}
 		}
 
 		removeDatabase();
 	}
 
 	private void closeDatabase(Database db)
 	{
 		try
 		{
 			db.close();
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 	}
 
 	private void removeDatabase()
 	{
 		try
 		{
 			env.removeDatabase(null, dbName);
 			BDBEnviroment.sync();
 			log.info("Storage for queue '{}' was removed", queueProcessor.getDestinationName());
 		}
 		catch (Throwable t)
 		{
 			dealWithError(t, false);
 		}
 	}
 
 }
