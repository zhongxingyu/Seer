 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage.engine;
 
 import java.io.IOException;
 import java.io.SyncFailedException;
 import java.nio.BufferUnderflowException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.ConcurrentModificationException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.locks.Lock;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Unbind;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.api.PrimitiveConverter;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigService;
 import org.araqne.confdb.Predicates;
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserBugException;
 import org.araqne.log.api.LogParserBuilder;
 import org.araqne.logstorage.CachedRandomSeeker;
 import org.araqne.logstorage.CallbackSet;
 import org.araqne.logstorage.DateUtil;
 import org.araqne.logstorage.LockKey;
 import org.araqne.logstorage.LockStatus;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogCallback;
 import org.araqne.logstorage.LogCursor;
 import org.araqne.logstorage.LogFileService;
 import org.araqne.logstorage.LogFileServiceEventListener;
 import org.araqne.logstorage.LogFileServiceRegistry;
 import org.araqne.logstorage.LogMarshaler;
 import org.araqne.logstorage.LogRetentionPolicy;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogStorageEventListener;
 import org.araqne.logstorage.LogStorageStatus;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.LogTraverseCallback;
 import org.araqne.logstorage.LogWriterStatus;
 import org.araqne.logstorage.ReplicaStorageConfig;
 import org.araqne.logstorage.ReplicationMode;
 import org.araqne.logstorage.SimpleLogTraverseCallback;
 import org.araqne.logstorage.TableEventListener;
 import org.araqne.logstorage.TableLock;
 import org.araqne.logstorage.TableNotFoundException;
 import org.araqne.logstorage.TableScanRequest;
 import org.araqne.logstorage.TableSchema;
 import org.araqne.logstorage.UnsupportedLogFileTypeException;
 import org.araqne.logstorage.WriteFallback;
 import org.araqne.logstorage.WriterPreparationException;
 import org.araqne.logstorage.file.DatapathUtil;
 import org.araqne.logstorage.file.LogFileReader;
 import org.araqne.logstorage.file.LogFileServiceV2;
 import org.araqne.logstorage.file.LogFileWriter;
 import org.araqne.logstorage.file.LogRecordCursor;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logstorage-engine")
 @Provides
 public class LogStorageEngine implements LogStorage, TableEventListener, LogFileServiceEventListener {
 	private static final String DEFAULT_LOGFILETYPE = "v2";
 
 	private final Logger logger = LoggerFactory.getLogger(LogStorageEngine.class.getName());
 
 	private static final int DEFAULT_LOG_CHECK_INTERVAL = 1000;
 	private static final int DEFAULT_MAX_IDLE_TIME = 600000; // 10min
 	private static final int DEFAULT_LOG_FLUSH_INTERVAL = 60000; // 60sec
 
 	private LogStorageStatus status = LogStorageStatus.Closed;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private ConfigService conf;
 
 	@Requires
 	private LogFileServiceRegistry lfsRegistry;
 
 	@Requires
 	private StorageManager storageManager;
 
 	// online writers
 	private ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters;
 	private ConcurrentMap<OnlineWriterKey, AtomicLong> lastIds;
 
 	private CopyOnWriteArraySet<LogCallback> callbacks;
 
 	private CallbackSet callbackSet;
 	private CallbackSet fallbackSet;
 
 	// sweeping and flushing data
 	private WriterSweeper writerSweeper;
 	private Thread writerSweeperThread;
 
 	private LogFileFetcher fetcher;
 
 	private FilePath logDir;
 
 	private ConcurrentHashMap<String, Integer> tableNameCache;
 
 	// private CopyOnWriteArraySet<LogStorageEventListener> listeners;
 
 	@Unbind
 	public void unbind(LogFileServiceRegistry reg) {
 		logger.info("log file service registry unbinded");
 	}
 
 	public LogStorageEngine() {
 		int checkInterval = getIntParameter(Constants.LogCheckInterval, DEFAULT_LOG_CHECK_INTERVAL);
 		int maxIdleTime = getIntParameter(Constants.LogMaxIdleTime, DEFAULT_MAX_IDLE_TIME);
 		int flushInterval = getIntParameter(Constants.LogFlushInterval, DEFAULT_LOG_FLUSH_INTERVAL);
 
 		onlineWriters = new ConcurrentHashMap<OnlineWriterKey, OnlineWriter>();
 		lastIds = new ConcurrentHashMap<OnlineWriterKey, AtomicLong>();
 		writerSweeper = new WriterSweeper(checkInterval, maxIdleTime, flushInterval);
 		callbacks = new CopyOnWriteArraySet<LogCallback>();
 		fallbackSet = new CallbackSet();
 		callbackSet = new CallbackSet();
 		tableNameCache = new ConcurrentHashMap<String, Integer>();
 
 		logDir = storageManager.resolveFilePath(System.getProperty("araqne.data.dir")).newFilePath("araqne-logstorage/log");
 		logDir = storageManager.resolveFilePath(getStringParameter(Constants.LogStorageDirectory, logDir.getAbsolutePath()));
 		logDir.mkdirs();
 		DatapathUtil.setLogDir(logDir);
 
 		// listeners = new CopyOnWriteArraySet<LogStorageEventListener>();
 	}
 
 	@Override
 	public FilePath getDirectory() {
 		return logDir;
 	}
 
 	@Override
 	public void setDirectory(FilePath f) {
 		if (f == null)
 			throw new IllegalArgumentException("storage path should be not null");
 
 		if (!f.isDirectory())
 			throw new IllegalArgumentException("storage path should be directory");
 
 		ConfigUtil.set(conf, Constants.LogStorageDirectory, f.getAbsolutePath());
 		logDir = f;
 		DatapathUtil.setLogDir(logDir);
 	}
 
 	private String getStringParameter(Constants key, String defaultValue) {
 		String value = ConfigUtil.get(conf, key);
 		if (value != null)
 			return value;
 		return defaultValue;
 	}
 
 	private int getIntParameter(Constants key, int defaultValue) {
 		String value = ConfigUtil.get(conf, key);
 		if (value != null)
 			return Integer.valueOf(value);
 		return defaultValue;
 	}
 
 	@Override
 	public LogStorageStatus getStatus() {
 		return status;
 	}
 
 	@Validate
 	@Override
 	public void start() {
 		FilePath sysArgLogDir = storageManager.resolveFilePath(System.getProperty("araqne.data.dir")).newFilePath(
 				"araqne-logstorage/log");
 		logDir = storageManager
 				.resolveFilePath(getStringParameter(Constants.LogStorageDirectory, sysArgLogDir.getAbsolutePath()));
 		logDir.mkdirs();
 		DatapathUtil.setLogDir(logDir);
 
 		if (status != LogStorageStatus.Closed)
 			throw new IllegalStateException("log archive already started");
 
 		status = LogStorageStatus.Starting;
 		fetcher = new LogFileFetcher(tableRegistry, lfsRegistry, storageManager);
 
 		writerSweeperThread = new Thread(writerSweeper, "LogStorage LogWriter Sweeper");
 		writerSweeperThread.start();
 
 		// load table name cache
 		tableNameCache.clear();
 		for (TableSchema schema : tableRegistry.getTableSchemas()) {
 			tableNameCache.put(schema.getName(), schema.getId());
 		}
 
 		tableRegistry.addListener(this);
 		lfsRegistry.addListener(this);
 
 		status = LogStorageStatus.Open;
 	}
 
 	@Invalidate
 	@Override
 	public void stop() {
 		if (status != LogStorageStatus.Open)
 			throw new IllegalStateException("log archive already stopped");
 
 		status = LogStorageStatus.Stopping;
 
 		try {
 			if (tableRegistry != null) {
 				tableRegistry.removeListener(this);
 			}
 		} catch (IllegalStateException e) {
 			if (!e.getMessage().contains("Cannot create the Nullable Object"))
 				throw e;
 		}
 
 		writerSweeper.doStop = true;
 		synchronized (writerSweeper) {
 			writerSweeper.notifyAll();
 		}
 
 		// wait writer sweeper stop
 		try {
 			for (int i = 0; i < 25; i++) {
 				if (writerSweeper.isStopped)
 					break;
 
 				Thread.sleep(200);
 			}
 		} catch (InterruptedException e) {
 		}
 
 		// close all writers
 		for (OnlineWriterKey key : onlineWriters.keySet()) {
 			try {
 				OnlineWriter writer = onlineWriters.get(key);
 				if (writer != null)
 					writer.close();
 			} catch (Throwable t) {
 				logger.warn("exception caught", t);
 			}
 		}
 
 		onlineWriters.clear();
 		lastIds.clear();
 
 		lfsRegistry.removeListener(this);
 
 		status = LogStorageStatus.Closed;
 	}
 
 	@Override
 	public void createTable(TableSchema schema) {
 		tableRegistry.createTable(schema);
 	}
 
 	@Override
 	public void ensureTable(TableSchema schema) {
 		if (!tableRegistry.exists(schema.getName()))
 			createTable(schema);
 	}
 
 	@Override
 	public void alterTable(String tableName, TableSchema schema) {
 		tableRegistry.alterTable(tableName, schema);
 	}
 
 	@Override
 	public void dropTable(String tableName) {
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		int tableId = schema.getId();
 		Collection<Date> dates = getLogDates(tableName);
 
 		// drop retention policy
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", tableName));
 		if (c != null)
 			c.remove();
 
 		Lock tLock = tableRegistry.getExclusiveTableLock(tableName, "engine", "dropTable");
 		try {
 			tLock.lock();
 			// drop table metadata
 			tableRegistry.dropTable(tableName);
 
 			// evict online writers
 			for (Date day : dates) {
 				OnlineWriterKey key = new OnlineWriterKey(tableName, day, tableId);
 				OnlineWriter writer = onlineWriters.get(key);
 				if (writer != null) {
 					writer.close();
 					if (logger.isTraceEnabled())
 						logger.trace("araqne logstorage: removing logger [{}] according to table drop", key);
 					onlineWriters.remove(key);
 				}
 			}
 
 			// purge existing files
 			FilePath tableDir = getTableDirectory(schema);
 			if (!tableDir.exists())
 				return;
 
 			// delete all .idx, .dat, .key files
 			for (FilePath f : tableDir.listFiles()) {
 				String name = f.getName();
 				if (f.isFile() && (name.endsWith(".idx") || name.endsWith(".dat") || name.endsWith(".key"))) {
 					ensureDelete(f);
 				}
 			}
 
 			// delete directory if empty
 			if (tableDir.listFiles().length == 0) {
 				logger.info("araqne logstorage: deleted table {} directory", tableName);
 				tableDir.delete();
 			}
 		} finally {
 			if (tLock != null)
 				tLock.unlock();
 		}
 	}
 
 	@Override
 	public LogRetentionPolicy getRetentionPolicy(String tableName) {
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", tableName));
 		if (c == null)
 			return null;
 		return c.getDocument(LogRetentionPolicy.class);
 	}
 
 	@Override
 	public void setRetentionPolicy(LogRetentionPolicy policy) {
 		if (policy == null)
 			throw new IllegalArgumentException("policy should not be null");
 
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", policy.getTableName()));
 		if (c == null) {
 			db.add(policy);
 		} else {
 			c.setDocument(PrimitiveConverter.serialize(policy));
 			c.update();
 		}
 	}
 
 	@Override
 	public FilePath getTableDirectory(String tableName) {
 		if (!tableRegistry.exists(tableName))
 			throw new IllegalArgumentException("table not exists: " + tableName);
 
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		return getTableDirectory(schema);
 	}
 
 	private FilePath getTableDirectory(TableSchema schema) {
 		FilePath baseDir = logDir;
 		if (schema.getPrimaryStorage().getBasePath() != null)
 			baseDir = storageManager.resolveFilePath(schema.getPrimaryStorage().getBasePath());
 
 		if (baseDir == null)
 			return null;
 
 		return baseDir.newFilePath(Integer.toString(schema.getId()));
 	}
 
 	@Override
 	public Collection<Date> getLogDates(String tableName) {
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 
 		String storageType = schema.getPrimaryStorage().getType();
 		LogFileService lfs = lfsRegistry.getLogFileService(storageType);
 		if (lfs == null)
 			throw new UnsupportedLogFileTypeException(storageType);
 
 		return lfs.getPartitions(tableName);
 	}
 
 	@Override
 	public Collection<Date> getLogDates(String tableName, Date from, Date to) {
 		List<Date> l = new ArrayList<Date>();
 		for (Date d : getLogDates(tableName)) {
 			if (from != null && d.before(from))
 				continue;
 
 			if (to != null && d.after(to))
 				continue;
 
 			l.add(d);
 		}
 
 		Collections.sort(l, Collections.reverseOrder());
 
 		return l;
 	}
 
 	@Override
 	public boolean tryWrite(Log log, long waitFor, TimeUnit tu) throws InterruptedException {
 		// inlined verify() for fast-path performance
 		if (status != LogStorageStatus.Open)
 			throw new IllegalStateException("archive not opened");
 
 		// write data
 		String tableName = log.getTableName();
 
 		int tryCnt = 0;
 		int tryCntLimit = 2;
 		for (; tryCnt < tryCntLimit; tryCnt++) {
 			TableLock tl = tableRegistry.getSharedTableLock(tableName);
 			BackOffLock bol = new BackOffLock(tl);
 			try {
 				do {
 					boolean locked = bol.tryLock();
 					if (locked) {
 						OnlineWriter writer = loadOnlineWriter(tableName, log.getDate());
 						writer.write(log);
 						break;
 					} else {
 						if (callWriteFallback(Arrays.asList(log), tl))
 							return true;
 					}
 				} while (!bol.isDone());
 				if (!bol.hasLocked())
 					return false;
 				else
 					break;
 			} catch (WriterPreparationException ex) {
 				continue;
 			} catch (TimeoutException ex) {
 				throw new IllegalStateException(ex);
 			} catch (IOException e) {
 				if (e.getMessage().contains("closed")) {
 					logger.info("closed online writer: trying one more time");
 					continue;
 				}
 
 				throw new IllegalStateException("cannot write log: " + tableName + ", " + log.getDate());
 			} finally {
 				bol.unlock();
 			}
 		}
 
 		if (tryCnt == tryCntLimit) {
 			throw new IllegalStateException("cannot write [1] logs to table [" + tableName + "]: retry count exceeded");
 		}
 
 		if (log.getId() == 0)
 			throw new IllegalStateException("cannot write log: " + tableName + ", " + log.getDate());
 
 		// invoke log callbacks
 		List<Log> one = Arrays.asList(log);
 		invokeLogCallbacks(log.getTableName(), one);
 
 		return true;
 	}
 
 	private boolean callWriteFallback(List<Log> logs, TableLock tl) {
 		for (WriteFallback fb : fallbackSet.get(WriteFallback.class)) {
 			int handled = fb.onLockFailure(tl, logs);
 			if (handled == logs.size())
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean tryWrite(List<Log> logs, long waitFor, TimeUnit tu) throws InterruptedException {
 		// inlined verify() for fast-path performance
 		if (status != LogStorageStatus.Open)
 			throw new IllegalStateException("archive not opened");
 
 		HashMap<OnlineWriterKey, List<Log>> keyLogs = new HashMap<OnlineWriterKey, List<Log>>();
 
 		for (Log log : logs) {
 			Integer tableId = tableNameCache.get(log.getTableName());
 			if (tableId == null)
 				throw new TableNotFoundException(log.getTableName());
 
 			OnlineWriterKey writerKey = new OnlineWriterKey(log.getTableName(), log.getDay(), tableId);
 			List<Log> l = keyLogs.get(writerKey);
 			if (l == null) {
 				l = new ArrayList<Log>();
 				keyLogs.put(writerKey, l);
 			}
 
 			l.add(log);
 		}
 
 		HashMap<OnlineWriterKey, BackOffLock> locks = new HashMap<OnlineWriterKey, BackOffLock>();
 
 		try {
 			for (OnlineWriterKey k : keyLogs.keySet()) {
 				TableLock lock = tableRegistry.getSharedTableLock(k.getTableName());
 				BackOffLock bol = new BackOffLock(lock, waitFor, tu);
 				do {
 					boolean locked = bol.tryLock();
 					if (locked) {
 						locks.put(k, bol);
 					} else {
 						if (callWriteFallback(keyLogs.get(k), lock))
 							break;
 					}
 				} while (!bol.isDone());
 			}
 			// write data
 			for (Entry<OnlineWriterKey, List<Log>> e : keyLogs.entrySet()) {
 				OnlineWriterKey writerKey = e.getKey();
 				String tableName = writerKey.getTableName();
 				List<Log> l = e.getValue();
 
 				if (!locks.containsKey(writerKey))
 					continue;
 
 				int tryCnt = 0;
 				int tryCntLimit = 2;
 
 				for (; tryCnt < tryCntLimit; tryCnt++) {
 					try {
 						OnlineWriter writer = loadOnlineWriter(writerKey.getTableName(), writerKey.getDay());
 						writer.write(l);
 						break;
 					} catch (WriterPreparationException ex) {
 						logger.debug("WriterPreparationException", ex);
 						// retry
 					} catch (TimeoutException ex) {
 						throw new IllegalStateException("cannot write [" + l.size() + "] logs to table [" + tableName + "]", ex);
 					} catch (InterruptedException ex) {
 						throw new IllegalStateException("cannot write [" + l.size() + "] logs to table [" + tableName + "]", ex);
 					} catch (IOException ex) {
 						if (ex.getMessage().contains("closed")) {
 							logger.info("araqne logstorage: closed online writer, trying one more time");
 							continue;
 						}
 
 						throw new IllegalStateException("cannot write [" + l.size() + "] logs to table [" + tableName + "]", ex);
 					}
 				}
 
 				if (tryCnt == tryCntLimit) {
 					logger.info("tryCnt == tryCntLimit");
 					throw new IllegalStateException("cannot write [" + l.size() + "] logs to table [" + tableName
 							+ "]: retry count exceeded");
 				}
 
 				invokeLogCallbacks(writerKey.getTableName(), l);
 			}
 		} catch (RuntimeException e) {
 			logger.error("unexpected exception", e);
 			throw e;
 		} finally {
 			for (BackOffLock l : locks.values()) {
 				l.unlock();
 			}
 		}
 		return true;
 	}
 
 	private void invokeLogCallbacks(String tableName, List<Log> l) {
 		// invoke log callbacks
 		for (LogCallback callback : callbacks) {
 			try {
 				callback.onLogBatch(tableName, l);
 			} catch (Exception ex) {
 				logger.warn("araqne logstorage: log callback should not throw any exception", ex);
 			}
 		}
 	}
 
 	@Override
 	public Collection<Log> getLogs(String tableName, Date from, Date to, int limit) {
 		return getLogs(tableName, from, to, 0, limit);
 	}
 
 	@Override
 	public Collection<Log> getLogs(String tableName, Date from, Date to, long offset, int limit) {
 		final List<Log> ret = new ArrayList<Log>(limit);
 		try {
 			LogTraverseCallback.Sink listSink = new LogTraverseCallback.Sink(offset, limit) {
 
 				@Override
 				protected void processLogs(List<Log> logs) {
 					ret.addAll(logs);
 				}
 			};
 
 			search(new TableScanRequest(tableName, from, to, null, new SimpleLogTraverseCallback(listSink)));
 		} catch (InterruptedException e) {
 			throw new RuntimeException("interrupted");
 		}
 		return ret;
 	}
 
 	@Override
 	public Date getPurgeBaseline(String tableName) {
 		LogRetentionPolicy p = getRetentionPolicy(tableName);
 		if (p == null || p.getRetentionDays() == 0)
 			return null;
 
 		Collection<Date> logDays = getLogDates(tableName);
 		Date lastLogDay = getMaxDay(logDays.iterator());
 		if (lastLogDay == null)
 			return null;
 
 		Date now = new Date();
 		if (lastLogDay.after(now))
 			lastLogDay = now;
 
 		return getBaseline(lastLogDay, p.getRetentionDays());
 	}
 
 	private Date getBaseline(Date lastDay, int days) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(lastDay);
 		c.add(Calendar.DAY_OF_MONTH, -days);
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		c.set(Calendar.MILLISECOND, 0);
 		return c.getTime();
 	}
 
 	private Date getMaxDay(Iterator<Date> days) {
 		Date max = null;
 		while (days.hasNext()) {
 			Date day = days.next();
 			if (max == null)
 				max = day;
 			else if (max != null && day.after(max))
 				max = day;
 		}
 		return max;
 	}
 
 	@Override
 	public void purge(String tableName, Date day) {
 		purge(tableName, day, false);
 	}
 
 	@Override
 	public void purge(String tableName, Date day, boolean skipArgCheck) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		FilePath dir = getTableDirectory(tableName);
 
 		if (!skipArgCheck) {
 			ReplicaStorageConfig config = ReplicaStorageConfig.parseTableSchema(schema);
 			if (config != null && config.mode() != ReplicationMode.ACTIVE)
 				throw new IllegalArgumentException("specified table has replica storage config and cannot purge non-active table");
 		}
 
 		// evict online buffer and close
 		OnlineWriter writer = onlineWriters.remove(new OnlineWriterKey(tableName, day, schema.getId()));
 		if (writer != null)
 			writer.close();
 
 		String fileName = dateFormat.format(day);
 		FilePath idxFile = dir.newFilePath(fileName + ".idx");
 		FilePath datFile = dir.newFilePath(fileName + ".dat");
 
 		for (LogStorageEventListener listener : callbackSet.get(LogStorageEventListener.class)) {
 			try {
 				listener.onPurge(tableName, day);
 			} catch (Throwable t) {
 				logger.error("araqne logstorage: storage event listener should not throw any exception", t);
 			}
 		}
 
 		logger.debug("araqne logstorage: try to purge log data of table [{}], day [{}]", tableName, fileName);
 		ensureDelete(idxFile);
 		ensureDelete(datFile);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> CopyOnWriteArraySet<T> getCallbacks(ConcurrentMap<Class<?>, CopyOnWriteArraySet<?>> callbackSets,
 			Class<T> class1) {
 		CopyOnWriteArraySet<?> result = callbackSets.get(class1);
 		if (result == null) {
 			result = new CopyOnWriteArraySet<T>();
 			CopyOnWriteArraySet<?> concensus = callbackSets.putIfAbsent(class1, result);
 			if (concensus != null)
 				result = concensus;
 		}
 		return (CopyOnWriteArraySet<T>) result;
 	}
 
 	@Override
 	public void purge(String tableName, Date fromDay, Date toDay) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		String from = "unbound";
 		if (fromDay != null)
 			from = dateFormat.format(fromDay);
 		String to = "unbound";
 		if (toDay != null)
 			to = dateFormat.format(toDay);
 
 		logger.debug("araqne logstorage: try to purge log data of table [{}], range [{}~{}]",
 				new Object[] { tableName, from, to });
 
 		List<Date> purgeDays = new ArrayList<Date>();
 		for (Date day : getLogDates(tableName)) {
 			// check range
 			if (fromDay != null && day.before(fromDay))
 				continue;
 
 			if (toDay != null && day.after(toDay))
 				continue;
 
 			purgeDays.add(day);
 		}
 
 		for (Date day : purgeDays) {
 			purge(tableName, day);
 		}
 	}
 
 	private boolean ensureDelete(FilePath f) {
 		final int MAX_TIMEOUT = 30000;
 
 		long begin = System.currentTimeMillis();
 
 		while (true) {
 			if (!f.exists() || f.delete()) {
 				if (logger.isTraceEnabled())
 					logger.trace("araqne logstorage: deleted log file [{}]", f.getAbsolutePath());
 				return true;
 			}
 
 			if (System.currentTimeMillis() - begin > MAX_TIMEOUT) {
 				logger.error("araqne logstorage: delete timeout, cannot delete log file [{}]", f.getAbsolutePath());
 				return false;
 			}
 		}
 	}
 
 	@Override
 	public CachedRandomSeeker openCachedRandomSeeker() {
 		verify();
 
 		for (OnlineWriter writer : onlineWriters.values()) {
 			try {
 				writer.sync();
 			} catch (IOException e) {
 				logger.error("araqne logstorage: cannot sync online writer", e);
 			}
 		}
 
 		return new CachedRandomSeekerImpl(tableRegistry, fetcher, onlineWriters, logDir);
 	}
 
 	@Override
 	public LogCursor openCursor(String tableName, Date day, boolean ascending) throws IOException {
 		verify();
 
 		Integer tableId = tableNameCache.get(tableName);
 		if (tableId == null)
 			throw new TableNotFoundException(tableName);
 
 		OnlineWriter onlineWriter = onlineWriters.get(new OnlineWriterKey(tableName, day, tableId));
 		ArrayList<Log> buffer = null;
 		if (onlineWriter != null)
 			buffer = (ArrayList<Log>) onlineWriter.getBuffer();
 
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		String basePathString = schema.getPrimaryStorage().getBasePath();
 		FilePath basePath = logDir;
 		if (basePathString != null)
 			basePath = storageManager.resolveFilePath(basePathString);
 
 		FilePath indexPath = DatapathUtil.getIndexFile(tableId, day, basePath);
 		FilePath dataPath = DatapathUtil.getDataFile(tableId, day, basePath);
 		FilePath keyPath = DatapathUtil.getKeyFile(tableId, day, basePath);
 
 		String logFileType = schema.getPrimaryStorage().getType();
 		LogFileServiceV2.Option options = new LogFileServiceV2.Option(schema.getPrimaryStorage(), schema.getMetadata(),
 				tableName, basePath, indexPath, dataPath, keyPath);
 		options.put("day", day);
 
 		syncOnlineWriter(onlineWriter);
 		LogFileReader reader = lfsRegistry.newReader(tableName, logFileType, options);
 
 		return new LogCursorImpl(tableName, day, buffer, reader, ascending);
 	}
 
 	private OnlineWriter loadOnlineWriter(String tableName, Date date) throws TimeoutException, InterruptedException,
 			WriterPreparationException {
 		// check table existence
 		Integer tableId = tableNameCache.get(tableName);
 		if (tableId == null)
 			throw new TableNotFoundException(tableName);
 
 		Date day = DateUtil.getDay(date);
 		OnlineWriterKey key = new OnlineWriterKey(tableName, day, tableId);
 
 		OnlineWriter online = onlineWriters.get(key);
 		if (online != null && !online.isClosed()) {
 			if (online.isReady()) {
 				return online;
 			} else {
 				online.awaitWriterPreparation();
 				return online;
 			}
 		}
 
 		try {
 			OnlineWriter oldWriter = onlineWriters.get(key);
 
 			// @formatter:off
 			/*
 			 * statuses of OnlineWriter
 			 * 
 			 * writer closing writer.closed 1) null false false 2) !null false
 			 * false 3) !null true false 4) !null true true
 			 */
 			// @formatter:on
 			if (oldWriter != null) {
 				if (oldWriter.isCloseCompleted()) {
 					while (onlineWriters.get(key) == oldWriter)
 						Thread.yield();
 					return loadNewOnlineWriter(key, getLogFileType(tableName));
 				} else if (oldWriter.isClosed()) {
 					synchronized (oldWriter) {
 						while (!oldWriter.isCloseCompleted()) {
 							oldWriter.wait(1000);
 						}
 						while (onlineWriters.get(key) == oldWriter)
 							Thread.yield();
 						return loadNewOnlineWriter(key, getLogFileType(tableName));
 					}
 				} else {
 					return loadNewOnlineWriter(key, getLogFileType(tableName));
 				}
 			} else {
 				return loadNewOnlineWriter(key, getLogFileType(tableName));
 			}
 		} catch (UnsupportedLogFileTypeException e) {
 			throw new IllegalStateException("cannot open writer: " + tableName + ", date=" + day, e);
 		} catch (IOException e) {
 			throw new IllegalStateException("cannot open writer: " + tableName + ", date=" + day, e);
 		}
 	}
 
 	private String getLogFileType(String tableName) {
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		return schema.getPrimaryStorage().getType();
 	}
 
 	private OnlineWriter loadNewOnlineWriter(OnlineWriterKey key, String logFileType) throws IOException, InterruptedException,
 			TimeoutException, WriterPreparationException {
 		OnlineWriter newWriter = newOnlineWriter(key.getTableName(), key.getDay(), logFileType);
 		OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
 		if (consensus == null) {
 			try {
 				AtomicLong lastKey = getLastKey(key);
 				newWriter.prepareWriter(storageManager, callbackSet, logDir, lastKey);
 				return newWriter;
 			} catch (IOException e) {
 				logger.error("loadNewOnlineWriter failed: " + key, e);
 				onlineWriters.remove(key, newWriter);
 				throw e;
 			} catch (RuntimeException e) {
 				logger.error("loadNewOnlineWriter failed: " + key, e);
 				onlineWriters.remove(key, newWriter);
 				throw e;
 			}
 		} else {
 			try {
 				consensus.awaitWriterPreparation();
 
 				if (!consensus.isReady())
 					throw new IllegalStateException("log writer preparation failed - " + key);
 				else {
 					return consensus;
 				}
 			} finally {
 				if (consensus != newWriter)
 					newWriter.close();
 			}
 		}
 	}
 
 	private OnlineWriter newOnlineWriter(String tableName, Date day, String logFileType) throws InterruptedException {
 		if (logFileType == null)
 			logFileType = DEFAULT_LOGFILETYPE;
 		LogFileService lfs = lfsRegistry.getLogFileService(logFileType);
 		if (lfs == null) {
 			throw new UnsupportedLogFileTypeException(logFileType);
 		}
 
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 
 		Lock tableLock = tableRegistry.getSharedTableLock(tableName);
 		try {
 			tableLock.lock();
 
 			return new OnlineWriter(lfs, schema, day);
 		} finally {
 			tableLock.unlock();
 		}
 	}
 
 	private AtomicLong getLastKey(OnlineWriterKey key) {
 		if (!lastIds.containsKey(key))
 			lastIds.putIfAbsent(key, new AtomicLong(-1));
 		return lastIds.get(key);
 	}
 
 	@Override
 	public void reload() {
 		int flushInterval = getIntParameter(Constants.LogFlushInterval, DEFAULT_LOG_FLUSH_INTERVAL);
 		int maxIdleTime = getIntParameter(Constants.LogMaxIdleTime, DEFAULT_MAX_IDLE_TIME);
 		writerSweeper.setFlushInterval(flushInterval);
 		writerSweeper.setMaxIdleTime(maxIdleTime);
 	}
 
 	@Override
 	public void flush() {
 		synchronized (writerSweeper) {
 			writerSweeper.setFlushAll(true);
 			writerSweeper.notifyAll();
 		}
 	}
 
 	@Override
 	public void addLogListener(LogCallback callback) {
 		callbacks.add(callback);
 	}
 
 	@Override
 	public void removeLogListener(LogCallback callback) {
 		callbacks.remove(callback);
 	}
 
 	private void verify() {
 		if (status != LogStorageStatus.Open)
 			throw new IllegalStateException("archive not opened");
 	}
 
 	@Override
 	public List<LogWriterStatus> getWriterStatuses() {
 		List<LogWriterStatus> writers = new ArrayList<LogWriterStatus>(onlineWriters.size());
 		for (OnlineWriterKey key : onlineWriters.keySet()) {
 			OnlineWriter writer = onlineWriters.get(key);
 			LogWriterStatus s = new LogWriterStatus();
 			s.setTableName(key.getTableName());
 			s.setDay(key.getDay());
 			s.setLastWrite(writer.getLastAccess());
 			s.setBufferSize(writer.getBuffer().size());
 			writers.add(s);
 		}
 
 		return writers;
 	}
 
 	/**
 	 * @since 2.7.0
 	 */
 	@Override
 	public LogFileWriter getOnlineWriter(String tableName, Date day) {
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		int tableId = schema.getId();
 		OnlineWriter online = onlineWriters.get(new OnlineWriterKey(tableName, day, tableId));
 		if (online == null)
 			return null;
 		return online.getWriter();
 	}
 
 	@Override
 	public void addEventListener(LogStorageEventListener listener) {
 		callbackSet.get(LogStorageEventListener.class).add(listener);
 	}
 
 	@Override
 	public void removeEventListener(LogStorageEventListener listener) {
 		callbackSet.get(LogStorageEventListener.class).remove(listener);
 	}
 
 	@Override
 	public <T> void addEventListener(Class<T> clazz, T listener) {
 		callbackSet.get(clazz).add(listener);
 	}
 
 	@Override
 	public <T> void removeEventListener(Class<T> clazz, T listener) {
 		callbackSet.get(clazz).remove(listener);
 	}
 
 	@Override
 	public <T> void addFallback(Class<T> clazz, T fallback) {
 		fallbackSet.get(clazz).add(fallback);
 	}
 
 	@Override
 	public <T> void removeFallback(Class<T> clazz, T fallback) {
 		fallbackSet.get(clazz).remove(fallback);
 	}
 
 	private class WriterSweeper implements Runnable {
 		private final Logger logger = LoggerFactory.getLogger(WriterSweeper.class.getName());
 		private volatile int checkInterval;
 		private volatile int maxIdleTime;
 		private volatile int flushInterval;
 
 		private volatile boolean doStop = false;
 		private volatile boolean isStopped = true;
 		private volatile boolean flushAll = false;
 
 		public WriterSweeper(int checkInterval, int maxIdleTime, int flushInterval) {
 			this.checkInterval = checkInterval;
 			this.maxIdleTime = maxIdleTime;
 			this.flushInterval = flushInterval;
 		}
 
 		public void setFlushInterval(int flushInterval) {
 			this.flushInterval = flushInterval;
 		}
 
 		public void setMaxIdleTime(int maxIdleTime) {
 			this.maxIdleTime = maxIdleTime;
 		}
 
 		public void setFlushAll(boolean flushAll) {
 			this.flushAll = flushAll;
 		}
 
 		@Override
 		public void run() {
 			try {
 				isStopped = false;
 
 				while (true) {
 					try {
 						if (doStop)
 							break;
 
 						synchronized (this) {
 							this.wait(checkInterval);
 						}
 						sweep();
 					} catch (InterruptedException e) {
 						logger.trace("araqne logstorage: sweeper interrupted");
 					} catch (Exception e) {
 						logger.error("araqne logstorage: sweeper error", e);
 					}
 				}
 			} finally {
 				doStop = false;
 				isStopped = true;
 			}
 
 			logger.info("araqne logstorage: writer sweeper stopped");
 		}
 
 		private void sweep() {
 			List<OnlineWriterKey> evicts = new ArrayList<OnlineWriterKey>();
 			long now = new Date().getTime();
 			try {
 				// periodic log flush
 				boolean flushAll = this.flushAll;
 				this.flushAll = false;
 				for (OnlineWriterKey key : onlineWriters.keySet()) {
 					OnlineWriter writer = onlineWriters.get(key);
 					if (writer == null || writer.isClosed() || !writer.isReady())
 						continue;
 					boolean doFlush = writer.isCloseReserved() || ((now - writer.getLastFlush().getTime()) > flushInterval);
 					doFlush = flushAll ? true : doFlush;
 					if (doFlush) {
 						try {
 							if (logger.isTraceEnabled())
 								logger.trace("araqne logstorage: flushing writer [{}]", key);
 							writer.flush();
 						} catch (IOException e) {
 							logger.error("araqne logstorage: log flush failed", e);
 						}
 					}
 
 					// close file if writer is in idle state
 					int interval = (int) (now - writer.getLastAccess().getTime());
 					if (interval > maxIdleTime || writer.isCloseReserved())
 						evicts.add(key);
 				}
 			} catch (ConcurrentModificationException e) {
 			}
 
 			closeAndKickout(evicts);
 		}
 
 		private void closeAndKickout(List<OnlineWriterKey> evicts) {
 			for (OnlineWriterKey key : evicts) {
 				OnlineWriter evictee = onlineWriters.get(key);
 				if (evictee != null) {
 					evictee.close();
 					if (logger.isTraceEnabled())
 						logger.trace("araqne logstorage: evict logger [{}]", key);
 					onlineWriters.remove(key);
 				}
 			}
 		}
 	}
 
 	private static class LogCursorImpl implements LogCursor {
 		private String tableName;
 		private Date day;
 		private ArrayList<Log> buffer;
 		private LogFileReader reader;
 		private LogRecordCursor cursor;
 		private boolean ascending;
 
 		private Log prefetch;
 		private int bufferNext;
 		private int bufferTotal;
 
 		public LogCursorImpl(String tableName, Date day, ArrayList<Log> buffer, LogFileReader reader, boolean ascending)
 				throws IOException {
 			this.tableName = tableName;
 			this.day = day;
 			this.reader = reader;
 			this.cursor = reader.getCursor(ascending);
 			this.ascending = ascending;
 
 			if (buffer != null) {
 				this.buffer = buffer;
 				this.bufferTotal = buffer.size();
 				this.bufferNext = ascending ? 0 : bufferTotal - 1;
 			}
 		}
 
 		@Override
 		public boolean hasNext() {
 			if (prefetch != null)
 				return true;
 
 			if (ascending) {
 				if (cursor.hasNext()) {
 					prefetch = LogMarshaler.convert(tableName, cursor.next());
 					return true;
 				}
 
 				if (bufferNext < bufferTotal) {
 					prefetch = buffer.get(bufferNext++);
 					return true;
 				}
 
 				return false;
 			} else {
 				if (bufferNext < 0) {
 					prefetch = buffer.get(bufferNext--);
 					return true;
 				}
 
 				if (cursor.hasNext()) {
 					prefetch = LogMarshaler.convert(tableName, cursor.next());
 					return true;
 				}
 
 				return false;
 			}
 		}
 
 		@Override
 		public Log next() {
 			if (!hasNext())
 				throw new NoSuchElementException("end of log cursor");
 
 			Log log = prefetch;
 			prefetch = null;
 			return log;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void close() {
 			reader.close();
 		}
 
 		@Override
 		public String toString() {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			return "log cursor for table " + tableName + ", day " + dateFormat.format(day);
 		}
 	}
 
 	@Override
 	public void onCreate(TableSchema schema) {
 		tableNameCache.put(schema.getName(), schema.getId());
 	}
 
 	@Override
 	public void onAlter(TableSchema oldSchema, TableSchema newSchema) {
 	}
 
 	@Override
 	public void onDrop(TableSchema schema) {
 		tableNameCache.remove(schema.getName());
 	}
 
 	public void purgeOnlineWriters() {
 		List<OnlineWriterKey> keys = new ArrayList<OnlineWriterKey>();
 		for (Map.Entry<OnlineWriterKey, OnlineWriter> e : onlineWriters.entrySet()) {
 			e.getValue().close();
 			keys.add(e.getKey());
 		}
 		for (OnlineWriterKey key : keys) {
 			onlineWriters.remove(key);
 		}
 	}
 
 	@Override
 	public void onUnloadingFileService(String engineName) {
 		List<OnlineWriterKey> toRemove = new ArrayList<OnlineWriterKey>();
 		for (OnlineWriterKey key : onlineWriters.keySet()) {
 			try {
 				OnlineWriter writer = onlineWriters.get(key);
 				if (writer != null && writer.getFileServiceType().equals(engineName))
 					toRemove.add(key);
 			} catch (Throwable t) {
 				logger.warn("exception caught", t);
 			}
 		}
 
 		for (OnlineWriterKey key : toRemove) {
 			try {
 				OnlineWriter writer = onlineWriters.get(key);
 				if (writer != null) {
 					writer.close();
 					onlineWriters.remove(key);
 				}
 			} catch (Throwable t) {
 				logger.warn("exception caught", t);
 			}
 		}
 
 	}
 
 	@Override
 	public boolean search(TableScanRequest req) throws InterruptedException {
 		verify();
 
 		String tableName = req.getTableName();
 		Collection<Date> days = getLogDates(tableName);
 
 		List<Date> filtered = DateUtil.filt(days, req.getFrom(), req.getTo());
 		logger.trace("araqne logstorage: searching {} tablets of table [{}]", filtered.size(), tableName);
 		
 		if (req.isAsc())
 			Collections.sort(filtered);
 
 		for (Date day : filtered) {
 			if (logger.isTraceEnabled())
 				logger.trace("araqne logstorage: searching table {}, date={}", tableName, DateUtil.getDayText(day));
 
 			searchTablet(req, day);
 			if (req.getTraverseCallback().isEof())
 				break;
 		}
 
 		return !req.getTraverseCallback().isEof();
 	}
 
 	private void syncOnlineWriter(OnlineWriter onlineWriter) {
 		if (onlineWriter != null) {
 			try {
 				onlineWriter.sync();
 			} catch (SyncFailedException e) {
 				logger.debug("araqne logstorage: sync failed", e);
 			} catch (IOException e) {
 				logger.error("araqne logstorage: cannot sync online writer", e);
 			}
 		}
 	}
 
 	@Override
 	public boolean searchTablet(TableScanRequest req, Date day) throws InterruptedException {
 		String tableName = req.getTableName();
 		Date from = req.getFrom();
 		Date to = req.getTo();
 		long minId = req.getMinId();
 		long maxId = req.getMaxId();
 		LogParserBuilder builder = req.getParserBuilder();
 		LogTraverseCallback c = req.getTraverseCallback();
 
 		TableSchema schema = tableRegistry.getTableSchema(tableName, true);
 		int tableId = schema.getId();
 		String basePathString = schema.getPrimaryStorage().getBasePath();
 		FilePath basePath = logDir;
 		if (basePathString != null)
 			basePath = storageManager.resolveFilePath(basePathString);
 
 		FilePath indexPath = DatapathUtil.getIndexFile(tableId, day, basePath);
 		FilePath dataPath = DatapathUtil.getDataFile(tableId, day, basePath);
 		FilePath keyPath = DatapathUtil.getKeyFile(tableId, day, basePath);
 		LogFileReader reader = null;
 
 		long onlineMinId = -1;
 		List<Log> logs = null;
 
 		try {
 			// do NOT use getOnlineWriter() here (it loads empty writer on cache
 			// automatically if writer not found)
 			OnlineWriter onlineWriter = onlineWriters.get(new OnlineWriterKey(tableName, day, tableId));
 			if (onlineWriter != null) {
 				List<Log> buffer = onlineWriter.getBuffer();
 
 				syncOnlineWriter(onlineWriter);
 
 				if (buffer != null && !buffer.isEmpty()) {
 					LogParser parser = null;
 					if (builder != null)
 						parser = builder.build();
 
 					if (logger.isTraceEnabled())
 						logger.trace("araqne logstorage: {} logs in writer buffer.", buffer.size());
 
 					logs = new ArrayList<Log>(buffer.size());
 
 					if (req.isAsc()) {
 						for (Log logData : buffer) {
 							if (onlineMinId == -1)
 								onlineMinId = logData.getId();
 
 							if ((from == null || !logData.getDate().before(from)) && (to == null || logData.getDate().before(to))
 									&& (minId < 0 || minId <= logData.getId()) && (maxId < 0 || maxId >= logData.getId())) {
 								List<Log> result = null;
 								try {
 									result = LogFileReader.parse(tableName, parser, logData);
 								} catch (LogParserBugException e) {
 									result = Arrays.asList(new Log[] { new Log(e.tableName, e.date, e.id, e.logMap) });
 									c.setFailure(e);
 								}
 								logs.addAll(result);
 							}
 						}
 					} else {
 						ListIterator<Log> li = buffer.listIterator(buffer.size());
 
 						while (li.hasPrevious()) {
 							Log logData = li.previous();
 							onlineMinId = logData.getId();
 							if ((from == null || !logData.getDate().before(from)) && (to == null || logData.getDate().before(to))
 									&& (minId < 0 || minId <= logData.getId()) && (maxId < 0 || maxId >= logData.getId())) {
 								List<Log> result = null;
 								try {
 									result = LogFileReader.parse(tableName, parser, logData);
 								} catch (LogParserBugException e) {
 									result = Arrays.asList(new Log[] { new Log(e.tableName, e.date, e.id, e.logMap) });
 									c.setFailure(e);
 								}
 								logs.addAll(result);
 							}
 						}
 					}
 
 					if (c.isEof())
 						return false;
 				}
 			}
 			
			if (logs != null && logger.isDebugEnabled())
				logger.debug("#buffer flush bug# logs size: {}", logs.size());
 
 			String logFileType = schema.getPrimaryStorage().getType();
 			LogFileServiceV2.Option options = new LogFileServiceV2.Option(schema.getPrimaryStorage(), schema.getMetadata(),
 					tableName, basePath, indexPath, dataPath, keyPath);
 			options.put("day", day);
 
 			syncOnlineWriter(onlineWriter);
 			reader = lfsRegistry.newReader(tableName, logFileType, options);
 
 			long flushedMaxId = (onlineMinId > 0) ? onlineMinId - 1 : maxId;
 			long readerMaxId = maxId != -1 ? Math.min(flushedMaxId, maxId) : flushedMaxId;
 
 			logger.debug("#buffer flush bug# minId: {}, readerMaxId: {}", minId, readerMaxId);
 			logger.debug("#buffer flush bug# maxId: {}, onlineMinId: {}", maxId, flushedMaxId);
 
 			if (minId < 0 || readerMaxId < 0 || readerMaxId >= minId) {
 				if (req.isAsc()) {
 					TableScanRequest tabletReq = req.clone();
 					tabletReq.setMaxId(readerMaxId);
 					reader.traverse(tabletReq);
 					if (logs != null)
 						c.writeLogs(logs);
 				} else {
 					if (logs != null)
 						c.writeLogs(logs);
 
 					TableScanRequest tabletReq = req.clone();
 					tabletReq.setMaxId(readerMaxId);
 					reader.traverse(tabletReq);
 				}
 			}
 
 		} catch (InterruptedException e) {
 			throw e;
 		} catch (IllegalStateException e) {
 			c.setFailure(e);
 			Throwable cause = e.getCause();
 			if (cause instanceof BufferUnderflowException || cause instanceof IOException)
 				c.setFailure(cause);
 
 			if (e.getMessage().contains("license is locked"))
 				logger.warn("araqne logstorage: search tablet failed. {}", e.getMessage());
 			else if (logger.isTraceEnabled())
 				logger.trace("araqne logstorage: search tablet failed. logfile may be not synced yet", e);
 		} catch (BufferUnderflowException e) {
 			c.setFailure(e);
 			logger.trace("araqne logstorage: search tablet failed. logfile may be not synced yet", e);
 		} catch (IOException e) {
 			c.setFailure(e);
 			logger.warn("araqne logstorage: search tablet failed. logfile may be not synced yet", e);
 		} catch (Exception e) {
 			c.setFailure(e);
 			logger.error("araqne logstorage: search tablet failed", e);
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 
 		return !c.isEof();
 	}
 
 	@Override
 	public StorageManager getStorageManager() {
 		return storageManager;
 	}
 
 	@Override
 	public boolean lock(LockKey key, String purpose, long timeout, TimeUnit unit) throws InterruptedException {
 		Lock lock = tableRegistry.getExclusiveTableLock(key.tableName, key.owner, purpose);
 
 		if (lock.tryLock(timeout, unit)) {
 			flush(key.tableName);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public void unlock(LockKey storageLockKey, String purpose) {
 		Lock lock = tableRegistry.getExclusiveTableLock(storageLockKey.tableName, storageLockKey.owner, purpose);
 
 		lock.unlock();
 	}
 
 	@Override
 	public LockStatus lockStatus(LockKey storageLockKey) {
 		return tableRegistry.getTableLockStatus(storageLockKey.tableName);
 	}
 
 	@Override
 	public void flush(String tableName) {
 		HashMap<OnlineWriterKey, CountDownLatch> monitors = new HashMap<OnlineWriterKey, CountDownLatch>();
 		for (OnlineWriterKey key : onlineWriters.keySet()) {
 			if (key.getTableName().equals(tableName)) {
 				OnlineWriter ow = onlineWriters.get(key);
 				CountDownLatch monitor = ow.reserveClose();
 				monitors.put(key, monitor);
 			}
 		}
 		synchronized (writerSweeper) {
 			writerSweeper.notifyAll();
 		}
 		for (Map.Entry<OnlineWriterKey, CountDownLatch> e : monitors.entrySet()) {
 			waitForClose(e.getKey(), e.getValue());
 		}
 	}
 
 	@SuppressWarnings("serial")
 	private static class SweeperThreadStoppedException extends RuntimeException {
 	}
 
 	private void waitForClose(OnlineWriterKey key, CountDownLatch monitor) {
 		try {
 			if (writerSweeperThread.isAlive()) {
 				boolean closed = monitor.await(1, TimeUnit.MINUTES);
 				if (!closed) {
 					logger.info("wait for closing Table: {}", key.getTableName());
 					if (writerSweeperThread.isAlive())
 						monitor.await();
 					else
 						throw new SweeperThreadStoppedException();
 				}
 			} else {
 				throw new SweeperThreadStoppedException();
 			}
 		} catch (SweeperThreadStoppedException e) {
 			OnlineWriter o = onlineWriters.get(key);
 			if (o != null)
 				o.close();
 		} catch (InterruptedException e) {
 			logger.warn("wait for closing interrupted: {}", key.getTableName());
 			OnlineWriter o = onlineWriters.get(key);
 			if (o != null)
 				o.close();
 		}
 	}
 
 	@Override
 	public boolean tryWrite(Log log) {
 		try {
 			return tryWrite(log, 0, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean tryWrite(List<Log> logs) {
 		try {
 			return tryWrite(logs, 0, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public void write(Log log) throws InterruptedException {
 		tryWrite(log, Long.MAX_VALUE, TimeUnit.SECONDS);
 	}
 
 	@Override
 	public void write(List<Log> logs) throws InterruptedException {
 		tryWrite(logs, Long.MAX_VALUE, TimeUnit.SECONDS);
 	}
 }
