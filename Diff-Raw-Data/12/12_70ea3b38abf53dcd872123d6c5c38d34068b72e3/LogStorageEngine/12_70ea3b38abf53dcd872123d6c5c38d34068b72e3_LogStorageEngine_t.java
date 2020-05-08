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
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
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
 import java.util.NoSuchElementException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.api.PrimitiveConverter;
 import org.araqne.codec.EncodingRule;
 import org.araqne.codec.FastEncodingRule;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigService;
 import org.araqne.confdb.Predicates;
 import org.araqne.logstorage.*;
 import org.araqne.logstorage.file.LogFileFixReport;
 import org.araqne.logstorage.file.LogFileReader;
 import org.araqne.logstorage.file.LogFileRepairer;
 import org.araqne.logstorage.file.LogFileServiceV2;
 import org.araqne.logstorage.file.LogRecord;
 import org.araqne.logstorage.file.LogRecordCursor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logstorage-engine")
 @Provides
 public class LogStorageEngine implements LogStorage, LogTableEventListener {
 	private static final String DEFAULT_LOGFILETYPE = "v2";
 
 	private final Logger logger = LoggerFactory.getLogger(LogStorageEngine.class.getName());
 
 	private static final int DEFAULT_LOG_CHECK_INTERVAL = 1000;
 	private static final int DEFAULT_MAX_IDLE_TIME = 600000; // 10min
 	private static final int DEFAULT_LOG_FLUSH_INTERVAL = 60000; // 60sec
 	private static final int DEFAULT_BLOCK_SIZE = 640 * 1024; // 640KB
 
 	private LogStorageStatus status = LogStorageStatus.Closed;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private ConfigService conf;
 
 	@Requires
 	private LogFileServiceRegistry lfsRegistry;
 
 	// online writers
 	private ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters;
 
 	private CopyOnWriteArraySet<LogCallback> callbacks;
 
 	// sweeping and flushing data
 	private WriterSweeper writerSweeper;
 	private Thread writerSweeperThread;
 
 	private LogFileFetcher fetcher;
 
 	private File logDir;
 
 	private ConcurrentHashMap<String, Integer> tableNameCache;
 	private CopyOnWriteArraySet<LogStorageEventListener> listeners;
 
 	public LogStorageEngine() {
 		int checkInterval = getIntParameter(Constants.LogCheckInterval, DEFAULT_LOG_CHECK_INTERVAL);
 		int maxIdleTime = getIntParameter(Constants.LogMaxIdleTime, DEFAULT_MAX_IDLE_TIME);
 		int flushInterval = getIntParameter(Constants.LogFlushInterval, DEFAULT_LOG_FLUSH_INTERVAL);
 
 		onlineWriters = new ConcurrentHashMap<OnlineWriterKey, OnlineWriter>();
 		writerSweeper = new WriterSweeper(checkInterval, maxIdleTime, flushInterval);
 		callbacks = new CopyOnWriteArraySet<LogCallback>();
 		tableNameCache = new ConcurrentHashMap<String, Integer>();
 
 		logDir = new File(System.getProperty("araqne.data.dir"), "araqne-logstorage/log");
 		logDir = new File(getStringParameter(Constants.LogStorageDirectory, logDir.getAbsolutePath()));
 		logDir.mkdirs();
 		DatapathUtil.setLogDir(logDir);
 
 		listeners = new CopyOnWriteArraySet<LogStorageEventListener>();
 	}
 
 	@Override
 	public File getDirectory() {
 		return logDir;
 	}
 
 	@Override
 	public void setDirectory(File f) {
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
 		if (status != LogStorageStatus.Closed)
 			throw new IllegalStateException("log archive already started");
 
 		status = LogStorageStatus.Starting;
 		fetcher = new LogFileFetcher(tableRegistry, lfsRegistry);
 
 		// checkAllLogFiles();
 		checkLatestLogFiles();
 
 		writerSweeperThread = new Thread(writerSweeper, "LogStorage LogWriter Sweeper");
 		writerSweeperThread.start();
 
 		// load table name cache
 		tableNameCache.clear();
 		for (String tableName : tableRegistry.getTableNames()) {
 			tableNameCache.put(tableName, tableRegistry.getTableId(tableName));
 		}
 
 		tableRegistry.addListener(this);
 
 		status = LogStorageStatus.Open;
 	}
 
 	@Invalidate
 	@Override
 	public void stop() {
 		if (status != LogStorageStatus.Open)
 			throw new IllegalStateException("log archive already stopped");
 
 		status = LogStorageStatus.Stopping;
 
 		if (tableRegistry != null)
 			tableRegistry.removeListener(this);
 
 		writerSweeper.doStop = true;
 		writerSweeperThread.interrupt();
 
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
 			OnlineWriter writer = onlineWriters.get(key);
 			if (writer != null)
 				writer.close();
 		}
 
 		onlineWriters.clear();
 
 		status = LogStorageStatus.Closed;
 	}
 
 	@SuppressWarnings("unused")
 	private void checkAllLogFiles() {
 		logger.info("araqne logstorage: verifying all log tables");
 		for (String tableName : tableRegistry.getTableNames()) {
 			File dir = getTableDirectory(tableName);
 			if (dir == null) {
 				logger.error("araqne logstorage: table [{}] directory not found", tableName);
 				continue;
 			}
 
 			logger.trace("araqne logstorage: checking for [{}] table", tableName);
 
 			File[] files = dir.listFiles();
 			if (files == null)
 				continue;
 
 			for (File f : files) {
 				if (f.getName().endsWith(".idx")) {
 					String datFileName = f.getName().replace(".idx", ".dat");
 					File indexPath = f;
 					File dataPath = new File(dir, datFileName);
 
 					try {
 						LogFileFixReport report = new LogFileRepairer().fix(indexPath, dataPath);
 						if (report != null)
 							logger.info("araqne logstorage: fixed log table [{}], detail report: \n{}", tableName, report);
 					} catch (IOException e) {
 						logger.error("araqne logstorage: cannot fix index [" + indexPath.getAbsoluteFile() + "], data ["
 								+ dataPath.getAbsolutePath() + "]", e);
 					}
 				}
 			}
 		}
 		logger.info("araqne logstorage: all table verifications are completed");
 	}
 
 	private void checkLatestLogFiles() {
 		logger.info("araqne logstorage: verifying all log tables");
 		for (String tableName : tableRegistry.getTableNames()) {
 			File dir = getTableDirectory(tableName);
 			if (dir == null) {
 				logger.error("araqne logstorage: table [{}] directory not found", tableName);
 				continue;
 			}
 
 			logger.trace("araqne logstorage: checking for [{}] table", tableName);
 
 			File[] files = dir.listFiles();
 			if (files == null)
 				continue;
 
 			long lastModified = 0;
 			File lastModifiedFile = null;
 
 			// max lastmodified
 			for (File f : files) {
 				if (f.getName().endsWith(".idx") && lastModified < f.lastModified()) {
 					lastModified = f.lastModified();
 					lastModifiedFile = f;
 				}
 			}
 
 			if (lastModifiedFile == null) {
 				logger.trace("araqne logstorage: empty table [{}], skip verification", tableName);
 				continue;
 			}
 
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logstorage: table [{}], last modified [{}]", tableName, new Date(lastModified));
 
 			String datFileName = lastModifiedFile.getName().replace(".idx", ".dat");
 			File indexPath = lastModifiedFile;
 			File dataPath = new File(dir, datFileName);
 
 			try {
 				LogFileFixReport report = new LogFileRepairer().fix(indexPath, dataPath);
 				if (report != null)
 					logger.info("araqne logstorage: fixed log table [{}], detail report: \n{}", tableName, report);
 			} catch (IOException e) {
 				logger.error(
 						"araqne logstorage: cannot fix index [" + indexPath.getAbsoluteFile() + "], data ["
 								+ dataPath.getAbsolutePath() + "]", e);
 			}
 		}
 		logger.info("araqne logstorage: all table verifications are completed");
 	}
 
 	@Override
 	public void createTable(String tableName, String type) {
 		createTable(tableName, type, null);
 	}
 
 	@Override
 	public void createTable(String tableName, String type, Map<String, String> tableMetadata) {
 		tableRegistry.createTable(tableName, type, tableMetadata);
 	}
 
 	@Override
 	public void dropTable(String tableName) {
 		int tableId = tableRegistry.getTableId(tableName);
 		Collection<Date> dates = getLogDates(tableName);
 
 		// drop retention policy
 		ConfigDatabase db = conf.ensureDatabase("araqne-logstorage");
 		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", tableName));
 		if (c != null)
 			c.remove();
 
 		// drop table metadata
 		String basePath = tableRegistry.getTableMetadata(tableName, "base_path");
 		tableRegistry.dropTable(tableName);
 
 		// evict online writers
 		for (Date day : dates) {
 			OnlineWriterKey key = new OnlineWriterKey(tableName, day, tableId);
 			OnlineWriter writer = onlineWriters.get(key);
 			if (writer != null) {
 				writer.close();
 				logger.trace("araqne logstorage: removing logger [{}] according to table drop", key);
 				onlineWriters.remove(key);
 			}
 		}
 
 		// purge existing files
 		File tableDir = getTableDirectory(tableId, basePath);
 		if (!tableDir.exists())
 			return;
 
 		// delete all .idx and .dat files
 		for (File f : tableDir.listFiles()) {
 			if (f.isFile() && (f.getName().endsWith(".idx") || f.getName().endsWith(".dat"))) {
 				ensureDelete(f);
 			}
 		}
 
 		// delete directory if empty
 		if (tableDir.listFiles().length == 0) {
 			logger.info("araqne logstorage: deleted table {} directory", tableName);
 			tableDir.delete();
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
 	public File getTableDirectory(String tableName) {
 		if (!tableRegistry.exists(tableName))
 			throw new IllegalArgumentException("table not exists: " + tableName);
 
 		int tableId = tableRegistry.getTableId(tableName);
 		String basePath = tableRegistry.getTableMetadata(tableName, "base_path");
 		return getTableDirectory(tableId, basePath);
 	}
 
 	private File getTableDirectory(int tableId, String basePath) {
 		File baseDir = logDir;
 		if (basePath != null)
 			baseDir = new File(basePath);
 
 		return new File(baseDir, Integer.toString(tableId));
 	}
 
 	@Override
 	public Collection<Date> getLogDates(String tableName) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		int tableId = tableRegistry.getTableId(tableName);
 		String basePath = tableRegistry.getTableMetadata(tableName, "base_path");
 
 		File tableDir = getTableDirectory(tableId, basePath);
 		File[] files = tableDir.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".idx");
 			}
 		});
 
 		List<Date> dates = new ArrayList<Date>();
 		if (files != null) {
 			for (File file : files) {
 				try {
 					dates.add(dateFormat.parse(file.getName().split("\\.")[0]));
 				} catch (ParseException e) {
 					logger.error("araqne logstorage: invalid log filename, table {}, {}", tableName, file.getName());
 				}
 			}
 		}
 
 		Collections.sort(dates, Collections.reverseOrder());
 
 		return dates;
 	}
 
 	@Override
 	public void write(Log log) {
 		// inlined verify() for fast-path performance
 		if (status != LogStorageStatus.Open)
 			throw new IllegalStateException("archive not opened");
 
 		// write data
 		String tableName = log.getTableName();
 
 		for (int i = 0; i < 2; i++) {
 			try {
 				OnlineWriter writer = getOnlineWriter(tableName, log.getDate());
 				writer.write(log);
 				break;
 			} catch (IOException e) {
 				if (e.getMessage().contains("closed")) {
 					logger.info("closed online writer: trying one more time");
 					continue;
 				}
 
 				throw new IllegalStateException("cannot write log: " + tableName + ", " + log.getDate());
 			}
 		}
 
 		if (log.getId() == 0)
 			throw new IllegalStateException("cannot write log: " + tableName + ", " + log.getDate());
 
 		// invoke log callbacks
 		for (LogCallback callback : callbacks) {
 			try {
 				callback.onLog(log);
 			} catch (Exception e) {
 				logger.warn("araqne logstorage: log callback should not throw any exception", e);
 			}
 		}
 	}
 
 	@Override
 	public void write(Collection<Log> logs) {
 		for (Log log : logs)
 			write(log);
 	}
 
 	private LogRecord convert(Log log) {
 		ByteBuffer bb = new FastEncodingRule().encode(log.getData());
 		LogRecord logdata = new LogRecord(log.getDate(), log.getId(), bb);
 		log.setBinaryLength(bb.remaining());
 		return logdata;
 	}
 
 	@Override
 	public Collection<Log> getLogs(String tableName, Date from, Date to, int limit) {
 		return getLogs(tableName, from, to, 0, limit);
 	}
 
 	@Override
 	public Collection<Log> getLogs(String tableName, Date from, Date to, long offset, int limit) {
 		final List<Log> logs = new ArrayList<Log>(limit);
 		try {
 			search(tableName, from, to, offset, limit, new LogSearchCallback() {
 				@Override
 				public void onLog(Log log) {
 					logs.add(log);
 				}
 
 				@Override
 				public boolean isInterrupted() {
 					return false;
 				}
 
 				@Override
 				public void interrupt() {
 				}
 			});
 		} catch (InterruptedException e) {
 			throw new RuntimeException("interrupted");
 		}
 		return logs;
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
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		int tableId = tableRegistry.getTableId(tableName);
 		File dir = getTableDirectory(tableName);
 
 		// evict online buffer and close
 		OnlineWriter writer = onlineWriters.remove(new OnlineWriterKey(tableName, day, tableId));
 		if (writer != null)
 			writer.close();
 
 		String fileName = dateFormat.format(day);
 		File idxFile = new File(dir, fileName + ".idx");
 		File datFile = new File(dir, fileName + ".dat");
 
 		logger.debug("araqne logstorage: try to purge log data of table [{}], day [{}]", tableName, fileName);
 		ensureDelete(idxFile);
 		ensureDelete(datFile);
 
 		for (LogStorageEventListener listener : listeners) {
 			try {
 				listener.onPurge(tableName, day);
 			} catch (Throwable t) {
 				logger.error("araqne logstorage: storage event listener should not throw any exception", t);
 			}
 		}
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
 
 	private boolean ensureDelete(File f) {
 		final int MAX_TIMEOUT = 30000;
 
 		long begin = System.currentTimeMillis();
 
 		while (true) {
 			if (!f.exists() || f.delete()) {
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
 
 		return new CachedRandomSeekerImpl(tableRegistry, fetcher, onlineWriters);
 	}
 
 	@Override
 	public Log getLog(LogKey logKey) {
 		String tableName = tableRegistry.getTableName(logKey.getTableId());
 		return getLog(tableName, logKey.getDay(), logKey.getLogId());
 	}
 
 	@Override
 	public Log getLog(String tableName, Date day, int id) {
 		verify();
 
 		// check memory buffer (flush waiting)
 		Integer tableId = tableNameCache.get(tableName);
 		if (tableId == null)
 			throw new LogTableNotFoundException(tableName);
 
 		OnlineWriter writer = onlineWriters.get(new OnlineWriterKey(tableName, day, tableId));
 		if (writer != null) {
 			for (Log r : writer.getBuffer())
 				if (r.getId() == id)
 					return new Log(tableName, r.getDate(), id, EncodingRule.decodeMap(convert(r).getData().duplicate()));
 		}
 
 		// load from disk
 		LogFileReader reader = null;
 		try {
 			reader = fetcher.fetch(tableName, day);
 			LogRecord logdata = reader.find(id);
 			if (logdata == null) {
 				if (logger.isTraceEnabled()) {
 					String dayText = DateUtil.getDayText(day);
 					logger.trace("araqne logstorage: log [table={}, date={}, id={}] not found", new Object[] { tableName,
 							dayText, id });
 				}
 				return null;
 			}
 			return LogMarshaler.convert(tableName, logdata);
 		} catch (IOException e) {
 			throw new IllegalStateException("cannot read log: " + tableName + ", " + day + ", " + id);
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 	}
 
 	@Override
 	public LogCursor openCursor(String tableName, Date day, boolean ascending) throws IOException {
 		verify();
 
 		Integer tableId = tableNameCache.get(tableName);
 		if (tableId == null)
 			throw new LogTableNotFoundException(tableName);
 
 		OnlineWriter onlineWriter = onlineWriters.get(new OnlineWriterKey(tableName, day, tableId));
 		ArrayList<Log> buffer = null;
 		if (onlineWriter != null)
 			buffer = (ArrayList<Log>) onlineWriter.getBuffer();
 
 		String basePath = tableRegistry.getTableMetadata(tableName, "base_path");
 		File indexPath = DatapathUtil.getIndexFile(tableId, day, basePath);
 		File dataPath = DatapathUtil.getDataFile(tableId, day, basePath);
 
 		String logFileType = tableRegistry.getTableMetadata(tableName, LogTableRegistry.LogFileTypeKey);
 		if (logFileType == null)
 			logFileType = "v2";
 
 		LogFileReader reader = lfsRegistry.newReader(tableName, logFileType, new LogFileServiceV2.Option(tableName, indexPath,
 				dataPath));
 
 		return new LogCursorImpl(tableName, day, buffer, reader, ascending);
 	}
 
 	@Override
 	public long search(Date from, Date to, long limit, LogSearchCallback callback) throws InterruptedException {
 		return search(from, to, 0, limit, callback);
 	}
 
 	@Override
 	public long search(Date from, Date to, long offset, long limit, LogSearchCallback callback) throws InterruptedException {
 		verify();
 
 		int found = 0;
 		for (String tableName : tableRegistry.getTableNames()) {
 			long needed = limit - found;
 			if (needed <= 0)
 				break;
 
 			found += search(tableName, from, to, offset, needed, callback);
 
 			if (offset > 0) {
 				if (found > offset) {
 					found -= offset;
 					offset = 0;
 				} else {
 					offset -= found;
 					found = 0;
 				}
 			}
 		}
 
 		return found;
 	}
 
 	@Override
 	public long search(String tableName, Date from, Date to, long limit, LogSearchCallback callback) throws InterruptedException {
 		return search(tableName, from, to, 0, limit, callback);
 	}
 
 	@Override
 	public long search(String tableName, Date from, Date to, long offset, long limit, LogSearchCallback callback)
 			throws InterruptedException {
 		verify();
 
 		Collection<Date> days = getLogDates(tableName);
 
 		long found = 0;
 		List<Date> filtered = DateUtil.filt(days, from, to);
 		logger.trace("araqne logstorage: searching {} tablets of table [{}]", filtered.size(), tableName);
 
 		for (Date day : filtered) {
 			if (logger.isTraceEnabled())
 				logger.trace("araqne logstorage: searching table {}, date={}", tableName, DateUtil.getDayText(day));
 
 			long needed = limit - found;
 			if (limit != 0 && needed <= 0)
 				break;
 
 			found += searchTablet(tableName, day, from, to, -1, -1, offset, needed, new TraverseCallback(from, to, callback), false);
 
 			if (offset > 0) {
 				if (found > offset) {
 					found -= offset;
 					offset = 0;
 				} else {
 					offset -= found;
 					found = 0;
 				}
 			}
 		}
 
 		return found;
 	}
 	
 	@Override
 	public long searchTablet(String tableName, Date day, Date from, Date to, long minId, LogMatchCallback c, boolean doParallel) throws InterruptedException {
 		return searchTablet(tableName, day, from, to, minId, -1, 0, 0, c, doParallel);
 	}
 	
 	@Override
 	public long searchTablet(String tableName, Date day, long minId, long maxId, LogMatchCallback c, boolean doParallel) throws InterruptedException {
 		return searchTablet(tableName, day, null, null, minId, maxId, 0, 0, c, doParallel);
 	}
 	
 	private long searchTablet(String tableName, Date day, Date from, Date to, long minId, long maxId, long offset, long limit,
 			LogMatchCallback c, boolean doParallel) throws InterruptedException {
 		int tableId = tableRegistry.getTableId(tableName);
 		String basePath = tableRegistry.getTableMetadata(tableName, "base_path");
 
 		File indexPath = DatapathUtil.getIndexFile(tableId, day, basePath);
 		File dataPath = DatapathUtil.getDataFile(tableId, day, basePath);
 		LogFileReader reader = null;
		
		long onlineMinId = -1;
 
 		try {
 			// do NOT use getOnlineWriter() here (it loads empty writer on cache
 			// automatically if writer not found)
 			OnlineWriter onlineWriter = onlineWriters.get(new OnlineWriterKey(tableName, day, tableId));
 			if (onlineWriter != null) {
 				List<Log> buffer = onlineWriter.getBuffer();
 
 				if (buffer != null && !buffer.isEmpty()) {
 					logger.trace("araqne logstorage: {} logs in writer buffer.", buffer.size());
 					ListIterator<Log> li = buffer.listIterator(buffer.size());
 					while (li.hasPrevious()) {
 						Log logData = li.previous();
 						if ((from == null || !logData.getDate().before(from)) && (to == null || logData.getDate().before(to))
 								&& (minId < 0 || minId <= logData.getId()) && (maxId < 0 || maxId >= logData.getId())) {
 							if (offset > 0) {
 								offset--;
 								continue;
 							}
 
 							if (c.match(convert(logData)) && c.onLog(logData)) {
								if (onlineMinId < 0 || logData.getId() < onlineMinId)
									onlineMinId = logData.getId();
								
 								if (--limit == 0)
 									return c.getMatchedCount();
 							}
 						}
 					}
 				}
 			}
 
 			String logFileType = tableRegistry.getTableMetadata(tableName, LogTableRegistry.LogFileTypeKey);
 			if (logFileType == null)
 				logFileType = "v2";
 
 			reader = lfsRegistry.newReader(tableName, logFileType, new LogFileServiceV2.Option(tableName, indexPath, dataPath));
			long flushedMaxId = (onlineMinId > 0)? onlineMinId - 1: maxId;
			if (minId < 0 || flushedMaxId < 0 || flushedMaxId >= minId)
				reader.traverse(from, to, minId, flushedMaxId, offset, limit, c, doParallel);
 		} catch (InterruptedException e) {
 			throw e;
 		} catch (Exception e) {
 			logger.error("araqne logstorage: search tablet failed", e);
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 
 		return c.getMatchedCount();
 	}
 
 	private class TraverseCallback implements LogMatchCallback {
 		private Logger logger = LoggerFactory.getLogger(TraverseCallback.class);
 		private Date from;
 		private Date to;
 		private LogSearchCallback callback;
 		private long matched = 0;
 
 		public TraverseCallback(Date from, Date to, LogSearchCallback callback) {
 			this.from = from;
 			this.to = to;
 			this.callback = callback;
 		}
 
 		@Override
 		public long getMatchedCount() {
 			return matched;
 		}
 		
 		@Override
 		public boolean onLog(Log log) throws InterruptedException {
 			if (callback.isInterrupted())
 				throw new InterruptedException("interrupted log traverse");
 
 			try {
 				matched++;
 
 				logger.debug("araqne logdb: traverse log [{}]", log);
 				callback.onLog(log);
 
 				return true;
 			} catch (Exception e) {
 				if (callback.isInterrupted())
 					throw new InterruptedException("interrupted log traverse");
 				else
 					throw new RuntimeException(e);
 			}
 		}
 
 		@Override
 		public boolean match(LogRecord record) {
 			Date d = record.getDate();
 			if (from != null && d.before(from))
 				return false;
 			if (to != null && d.after(to))
 				return false;
 
 			return true;
 		}
 	}
 
 	private OnlineWriter getOnlineWriter(String tableName, Date date) {
 		// check table existence
 		Integer tableId = tableNameCache.get(tableName);
 		if (tableId == null)
 			throw new LogTableNotFoundException(tableName);
 
 		Date day = DateUtil.getDay(date);
 		OnlineWriterKey key = new OnlineWriterKey(tableName, day, tableId);
 
 		OnlineWriter online = onlineWriters.get(key);
 		if (online != null && online.isOpen())
 			return online;
 
 		try {
 			int blockSize = getIntParameter(Constants.LogBlockSize, DEFAULT_BLOCK_SIZE);
 			OnlineWriter oldWriter = onlineWriters.get(key);
 			String logFileType = tableRegistry.getTableMetadata(tableName, LogTableRegistry.LogFileTypeKey);
 
 			if (oldWriter != null) {
 				synchronized (oldWriter) {
 					if (!oldWriter.isOpen() && !oldWriter.isClosed()) { // closing
 						while (!oldWriter.isClosed()) {
 							try {
 								oldWriter.wait(1000);
 							} catch (InterruptedException e) {
 							}
 						}
 						while (onlineWriters.get(key) == oldWriter) {
 							Thread.yield();
 						}
 						OnlineWriter newWriter = newOnlineWriter(tableName, tableId, day, blockSize, logFileType);
 						OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
 						if (consensus == null)
 							online = newWriter;
 						else {
 							online = consensus;
 							if (consensus != newWriter)
 								newWriter.close();
 						}
 					} else if (oldWriter.isClosed()) {
 						while (onlineWriters.get(key) == oldWriter) {
 							Thread.yield();
 						}
 						OnlineWriter newWriter = newOnlineWriter(tableName, tableId, day, blockSize, logFileType);
 						OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
 						if (consensus == null)
 							online = newWriter;
 						else {
 							online = consensus;
 							if (consensus != newWriter)
 								newWriter.close();
 						}
 					} else {
 						online = oldWriter;
 					}
 				}
 			} else {
 				OnlineWriter newWriter = newOnlineWriter(tableName, tableId, day, blockSize, logFileType);
 				OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
 				if (consensus == null)
 					online = newWriter;
 				else {
 					online = consensus;
 					if (consensus != newWriter)
 						newWriter.close();
 				}
 			}
 		} catch (UnsupportedLogFileTypeException e) {
 			throw new IllegalStateException("cannot open writer: " + tableName + ", date=" + day, e);
 		} catch (IOException e) {
 			throw new IllegalStateException("cannot open writer: " + tableName + ", date=" + day, e);
 		}
 
 		return online;
 	}
 
 	private OnlineWriter newOnlineWriter(String tableName, int tableId, Date day, int blockSize, String logFileType)
 			throws IOException {
 		if (logFileType == null)
 			logFileType = DEFAULT_LOGFILETYPE;
 		LogFileService lfs = lfsRegistry.getLogFileService(logFileType);
 		if (lfs == null) {
 			throw new UnsupportedLogFileTypeException(logFileType);
 		}
 
 		Map<String, String> tableMetadata = new HashMap<String, String>();
 		for (String key : tableRegistry.getTableMetadataKeys(tableName))
 			tableMetadata.put(key, tableRegistry.getTableMetadata(tableName, key));
 		return new OnlineWriter(lfs, tableName, tableId, day, tableMetadata);
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
 		writerSweeper.setForceFlush(true);
 		writerSweeperThread.interrupt();
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
 
 	@Override
 	public void addEventListener(LogStorageEventListener listener) {
 		listeners.add(listener);
 	}
 
 	@Override
 	public void removeEventListener(LogStorageEventListener listener) {
 		listeners.remove(listener);
 	}
 
 	private class WriterSweeper implements Runnable {
 		private final Logger logger = LoggerFactory.getLogger(WriterSweeper.class.getName());
 		private volatile int checkInterval;
 		private volatile int maxIdleTime;
 		private volatile int flushInterval;
 
 		private volatile boolean doStop = false;
 		private volatile boolean isStopped = true;
 		private volatile boolean forceFlush = false;
 
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
 
 		public void setForceFlush(boolean forceFlush) {
 			this.forceFlush = forceFlush;
 		}
 
 		@Override
 		public void run() {
 			try {
 				isStopped = false;
 
 				while (true) {
 					try {
 						if (doStop)
 							break;
 
 						Thread.sleep(checkInterval);
 						sweep();
 					} catch (InterruptedException e) {
 						if (forceFlush) {
 							sweep();
 							forceFlush = false;
 						}
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
 				for (OnlineWriterKey key : onlineWriters.keySet()) {
 					OnlineWriter writer = onlineWriters.get(key);
 					boolean doFlush = forceFlush || ((now - writer.getLastFlush().getTime()) > flushInterval);
 					if (doFlush) {
 						try {
 							logger.trace("araqne logstorage: flushing writer [{}]", key);
 							writer.flush();
 						} catch (IOException e) {
 							logger.error("araqne logstorage: log flush failed", e);
 						}
 					}
 
 					// close file if writer is in idle state
 					int interval = (int) (now - writer.getLastAccess().getTime());
 					if (interval > maxIdleTime)
 						evicts.add(key);
 				}
 			} catch (ConcurrentModificationException e) {
 			}
 
 			for (OnlineWriterKey key : evicts) {
 				OnlineWriter evictee = onlineWriters.get(key);
 				if (evictee != null) {
 					evictee.close();
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
 	public void ensureTable(String tableName, String type) {
 		if (!tableRegistry.exists(tableName))
 			createTable(tableName, type);
 	}
 
 	@Override
 	public void onCreate(String tableName, Map<String, String> tableMetadata) {
 		tableNameCache.put(tableName, tableRegistry.getTableId(tableName));
 	}
 
 	@Override
 	public void onDrop(String tableName) {
 		tableNameCache.remove(tableName);
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
 }
