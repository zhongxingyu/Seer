 /*
  * Copyright 2011 Future Systems
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
 package org.araqne.logdb.query.command;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserBugException;
 import org.araqne.log.api.LogParserBuilder;
 import org.araqne.log.api.LogParserFactory;
 import org.araqne.log.api.LogParserFactoryRegistry;
 import org.araqne.log.api.LogParserInput;
 import org.araqne.log.api.LogParserOutput;
 import org.araqne.log.api.LogParserRegistry;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.DriverQueryCommand;
 import org.araqne.logdb.Permission;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.QueryTask;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.Strings;
 import org.araqne.logdb.TimeSpan;
 import org.araqne.logdb.query.parser.TableSpec;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogCallback;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.LogTraverseCallback;
 import org.araqne.logstorage.WrongTimeTypeException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Table extends DriverQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(Table.class);
 	private AccountService accountService;
 	private LogStorage storage;
 	private LogTableRegistry tableRegistry;
 	private LogParserFactoryRegistry parserFactoryRegistry;
 	private LogParserRegistry parserRegistry;
 
 	private TableParams params = new TableParams();
 	private volatile boolean stopped = false;
 
 	private RealtimeReceiver receiver;
 
 	public Table(TableParams params) {
 		this.params = params;
 	}
 
 	@Override
 	public String getName() {
 		return "table";
 	}
 
 	@Override
 	public void run() {
 		if (params.window != null)
 			receiveTableInputs();
 		else
 			scanTables();
 	}
 
 	private void receiveTableInputs() {
 		try {
 			this.receiver = new RealtimeReceiver();
 			storage.addLogListener(receiver);
 
 			long expire = System.currentTimeMillis() + params.window.unit.getMillis() * params.window.amount;
 
 			while (true) {
 				if (System.currentTimeMillis() >= expire)
 					break;
 
 				if (stopped)
 					break;
 
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 				}
 			}
 		} finally {
 			storage.removeLogListener(receiver);
 		}
 	}
 
 	private void scanTables() {
 		try {
 			ResultSink sink = new ResultSink(Table.this, params.offset, params.limit, params.ordered);
 			boolean isSuppressedBugAlert = false;
 
 			for (StorageObjectName tableName : expandTableNames(params.tableNames)) {
 				LogParserBuilder builder = new TableLogParserBuilder(parserRegistry, parserFactoryRegistry, tableRegistry,
 						tableName.getTable());
 				if (isSuppressedBugAlert)
 					builder.suppressBugAlert();
 
 				storage.search(tableName.getTable(), params.from, params.to, builder, new LogTraverseCallbackImpl(sink));
 
 				isSuppressedBugAlert = isSuppressedBugAlert || builder.isBugAlertSuppressed();
 				if (sink.isEof())
 					break;
 			}
 		} catch (InterruptedException e) {
 			logger.trace("araqne logdb: query interrupted");
 		} catch (Exception e) {
 			logger.error("araqne logdb: table exception", e);
 		} catch (Error e) {
 			logger.error("araqne logdb: table error", e);
 		}
 	}
 
 	@Deprecated
 	public List<String> getTableNames() {
 		List<String> result = new ArrayList<String>();
 		for (TableSpec s : params.tableNames) {
 			result.add(s.toString());
 		}
 		return result;
 	}
 
 	public List<TableSpec> getTableSpecs() {
 		return params.tableNames;
 	}
 
 	public void setTableNames(List<TableSpec> tableNames) {
 		params.tableNames = tableNames;
 	}
 
 	public AccountService getAccountService() {
 		return accountService;
 	}
 
 	public void setAccountService(AccountService accountService) {
 		this.accountService = accountService;
 	}
 
 	public LogStorage getStorage() {
 		return storage;
 	}
 
 	public void setStorage(LogStorage storage) {
 		this.storage = storage;
 	}
 
 	public LogTableRegistry getTableRegistry() {
 		return tableRegistry;
 	}
 
 	public void setTableRegistry(LogTableRegistry tableRegistry) {
 		this.tableRegistry = tableRegistry;
 	}
 
 	public LogParserFactoryRegistry getParserFactoryRegistry() {
 		return parserFactoryRegistry;
 	}
 
 	public void setParserFactoryRegistry(LogParserFactoryRegistry parserFactoryRegistry) {
 		this.parserFactoryRegistry = parserFactoryRegistry;
 	}
 
 	public LogParserRegistry getParserRegistry() {
 		return parserRegistry;
 	}
 
 	public void setParserRegistry(LogParserRegistry parserRegistry) {
 		this.parserRegistry = parserRegistry;
 	}
 
 	public long getOffset() {
 		return params.offset;
 	}
 
 	public void setOffset(long offset) {
 		params.offset = offset;
 	}
 
 	public long getLimit() {
 		return params.limit;
 	}
 
 	public void setLimit(long limit) {
 		params.limit = limit;
 	}
 
 	public Date getFrom() {
 		return params.from;
 	}
 
 	public Date getTo() {
 		return params.to;
 	}
 
 	@Override
 	public void onClose(QueryStopReason reason) {
 		if (logger.isDebugEnabled())
 			logger.debug("araqne logdb: stopping table scan, query [{}] reason [{}]", getQuery().getId(), reason);
 
 		stopped = true;
 	}
 
 	private class TableLogParserBuilder implements LogParserBuilder {
 		LogParserRegistry parserRegistry;
 
 		String tableParserName = null;
 		String tableParserFactoryName = null;
 
 		LogParserFactory tableParserFactory = null;
 		Map<String, String> parserProperty = null;
 
 		boolean bugAlertSuppressFlag = false;
 
 		public TableLogParserBuilder(LogParserRegistry parserRegistry, LogParserFactoryRegistry parserFactoryRegistry,
 				LogTableRegistry tableRegistry, String tableName) {
 			this.parserRegistry = parserRegistry;
 
 			if (tableName != null) {
 				this.tableParserName = tableRegistry.getTableMetadata(tableName, "parser");
 				this.tableParserFactoryName = tableRegistry.getTableMetadata(tableName, "logparser");
 
 				if (tableParserFactoryName != null) {
 					this.tableParserFactory = parserFactoryRegistry.get(tableParserFactoryName);
 					if (tableParserFactory != null) {
 						parserProperty = new HashMap<String, String>();
 						for (LoggerConfigOption configOption : tableParserFactory.getConfigOptions()) {
 							String optionName = configOption.getName();
 							String optionValue = tableRegistry.getTableMetadata(tableName, optionName);
 							if (configOption.isRequired() && optionValue == null)
 								throw new IllegalArgumentException("require table metadata " + optionName);
 							parserProperty.put(optionName, optionValue);
 						}
 					}
 				}
 			}
 		}
 
 		@Override
 		public LogParser build() {
 			LogParser parser = null;
 
 			if (tableParserName != null && parserRegistry.getProfile(tableParserName) != null) {
 				try {
 					parser = parserRegistry.newParser(tableParserName);
 				} catch (IllegalStateException e) {
 					if (logger.isDebugEnabled())
 						logger.debug("logpresso index: parser profile not found [{}]", tableParserName);
 				}
 			}
 
 			if (parser == null && tableParserFactory != null) {
 				parser = tableParserFactory.createParser(parserProperty);
 			}
 			return parser;
 		}
 
 		@Override
 		public boolean isBugAlertSuppressed() {
 			return bugAlertSuppressFlag;
 		}
 
 		@Override
 		public void suppressBugAlert() {
 			bugAlertSuppressFlag = true;
 		}
 	}
 
 	private List<StorageObjectName> expandTableNames(List<TableSpec> tableNames) {
 		final List<StorageObjectName> localTableNames = new ArrayList<StorageObjectName>();
 		for (TableSpec s : tableNames) {
 			for (StorageObjectName son : s.match(tableRegistry)) {
				if (son.getNamespace() != null)
 					continue;
 				if (son.isOptional() && !tableRegistry.exists(son.getTable()))
 					continue;
 				if (isAccessible(son))
 					localTableNames.add(son);
 			}
 		}
 		return localTableNames;
 	}
 
 	private boolean isAccessible(StorageObjectName name) {
 		return accountService.checkPermission(query.getContext().getSession(), name.getTable(), Permission.READ);
 	}
 
 	@Override
 	public void onPush(Row m) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 
 	private static class ResultSink extends LogTraverseCallback.Sink {
 		private final Table self;
 
 		public ResultSink(Table self, long offset, long limit, boolean ordered) {
 			super(offset, limit, ordered);
 			this.self = self;
 		}
 
 		@Override
 		protected void processLogs(List<Log> logs) {
 			RowBatch batch = new RowBatch();
 			batch.size = logs.size();
 			batch.rows = new Row[batch.size];
 
 			int i = 0;
 			for (Log log : logs)
 				batch.rows[i++] = new Row(log.getData());
 
 			self.pushPipe(batch);
 		}
 
 	}
 
 	private class LogTraverseCallbackImpl extends LogTraverseCallback {
 		LogTraverseCallbackImpl(Sink sink) {
 			super(sink);
 		}
 
 		@Override
 		public void interrupt() {
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			if (task.getStatus() == QueryTask.TaskStatus.CANCELED) {
 				logger.debug("araqne logdb: table scan task canceled, [{}]", Table.this.toString());
 				return true;
 			}
 
 			return stopped;
 		}
 
 		@Override
 		protected List<Log> filter(List<Log> logs) {
 			return logs;
 		}
 	}
 
 	public static class TableParams {
 		private List<TableSpec> tableNames;
 		private long offset;
 		private long limit;
 		private boolean ordered = true;
 		private Date from;
 		private Date to;
 		private TimeSpan window;
 		private String parserName;
 
 		public List<TableSpec> getTableSpecs() {
 			return tableNames;
 		}
 
 		public void setTableSpecs(List<TableSpec> tableNames) {
 			this.tableNames = tableNames;
 		}
 
 		public long getOffset() {
 			return offset;
 		}
 
 		public void setOffset(long offset) {
 			this.offset = offset;
 		}
 
 		public long getLimit() {
 			return limit;
 		}
 
 		public void setLimit(long limit) {
 			this.limit = limit;
 		}
 
 		public Date getFrom() {
 			return from;
 		}
 
 		public void setFrom(Date from) {
 			this.from = from;
 		}
 
 		public Date getTo() {
 			return to;
 		}
 
 		public void setTo(Date to) {
 			this.to = to;
 		}
 
 		public TimeSpan getWindow() {
 			return window;
 		}
 
 		public void setWindow(TimeSpan window) {
 			this.window = window;
 		}
 
 		public String getParserName() {
 			return parserName;
 		}
 
 		public void setParserName(String parserName) {
 			this.parserName = parserName;
 		}
 
 		public boolean isOrdered() {
 			return ordered;
 		}
 
 		public void setOrdered(boolean ordered) {
 			this.ordered = ordered;
 		}
 
 	}
 
 	@Override
 	public String toString() {
 		String s = "table";
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
 		if (params.getFrom() != null)
 			s += " from=" + df.format(params.getFrom());
 
 		if (params.getTo() != null)
 			s += " to=" + df.format(params.getTo());
 
 		if (params.getOffset() > 0)
 			s += " offset=" + params.getOffset();
 
 		if (params.getLimit() > 0)
 			s += " limit=" + params.getLimit();
 
 		if (params.getWindow() != null)
 			s += " window=" + params.getWindow();
 
 		return s + " " + Strings.join(getTableNames(), ", ");
 	}
 
 	private class RealtimeReceiver implements LogCallback {
 
 		private HashSet<String> tableFilters = new HashSet<String>();
 		private Map<String, LogParser> parsers = new HashMap<String, LogParser>();
 
 		public RealtimeReceiver() {
 
 			for (StorageObjectName tableName : expandTableNames(params.tableNames)) {
 				LogParserBuilder builder = new TableLogParserBuilder(parserRegistry, parserFactoryRegistry, tableRegistry,
 						tableName.getTable());
 				parsers.put(tableName.table, builder.build());
 				tableFilters.add(tableName.table);
 			}
 		}
 
 		@Override
 		public void onLogBatch(String tableName, List<Log> logBatch) {
 			if (!tableFilters.contains(tableName))
 				return;
 
 			List<Row> rows = new ArrayList<Row>();
 			LogParser parser = parsers.get(tableName);
 			if (parser != null) {
 				int ver = parser.getVersion();
 
 				for (Log log : logBatch) {
 					try {
 						if (ver == 1) {
 							Log parsed = parseV1(parser, log);
 							if (parsed != null) {
 								Row row = new Row(parsed.getData());
 								row.put("_table", tableName);
 								row.put("_id", log.getId());
 								row.put("_time", log.getDate());
 								rows.add(row);
 							}
 
 						} else if (ver == 2) {
 							for (Log parsed : parseV2(parser, log)) {
 								Row row = new Row(parsed.getData());
 								row.put("_table", tableName);
 								row.put("_id", log.getId());
 								row.put("_time", log.getDate());
 								rows.add(row);
 							}
 						}
 					} catch (Throwable t) {
 						continue;
 					}
 
 				}
 			} else {
 				for (Log log : logBatch) {
 					Row row = new Row(new HashMap<String, Object>(log.getData()));
 					rows.add(row);
 				}
 			}
 
 			RowBatch rowBatch = new RowBatch();
 			rowBatch.size = rows.size();
 			rowBatch.rows = rows.toArray(new Row[0]);
 
 			try {
 				pushPipe(rowBatch);
 			} catch (Throwable t) {
 				if (t.getMessage().contains("already closed"))
 					return;
 				logger.error("araqne logstorage: realtime table query failed", t);
 			}
 		}
 	}
 
 	private static Log parseV1(LogParser parser, Log log) throws LogParserBugException {
 		Map<String, Object> m = null;
 		Object time = log.getDate();
 		try {
 			// can be unmodifiableMap when it comes from memory buffer.
 			Map<String, Object> m2 = new HashMap<String, Object>(log.getData());
 			m2.put("_time", log.getDate());
 			Map<String, Object> parsed = parser.parse(m2);
 			if (parsed == null)
 				throw new ParseException("log parse failed", -1);
 
 			parsed.put("_table", log.getTableName());
 			parsed.put("_id", log.getId());
 
 			time = parsed.get("_time");
 			if (time == null) {
 				parsed.put("_time", log.getDate());
 				time = log.getDate();
 			} else if (!(time instanceof Date)) {
 				throw new WrongTimeTypeException(time);
 			}
 
 			m = parsed;
 			return new Log(log.getTableName(), (Date) time, log.getId(), m);
 		} catch (WrongTimeTypeException e) {
 			throw e;
 		} catch (Throwable t) {
 			// can be unmodifiableMap when it comes from memory
 			// buffer.
 			m = new HashMap<String, Object>(log.getData());
 			m.put("_table", log.getTableName());
 			m.put("_id", log.getId());
 			m.put("_time", log.getDate());
 
 			throw new LogParserBugException(t, log.getTableName(), log.getId(), (Date) time, m);
 		}
 	}
 
 	private static List<Log> parseV2(LogParser parser, Log log) throws LogParserBugException {
 		LogParserInput input = new LogParserInput();
 		input.setDate(log.getDate());
 		input.setSource(log.getTableName());
 		input.setData(log.getData());
 
 		List<Log> ret = new ArrayList<Log>();
 		try {
 			LogParserOutput output = parser.parse(input);
 			if (output != null) {
 				for (Map<String, Object> row : output.getRows()) {
 					row.put("_table", log.getTableName());
 					row.put("_id", log.getId());
 
 					Object time = row.get("_time");
 					if (time == null)
 						row.put("_time", log.getDate());
 					else if (!(time instanceof Date)) {
 						throw new WrongTimeTypeException(time);
 					}
 
 					ret.add(new Log(log.getTableName(), log.getDate(), log.getDay(), log.getId(), row));
 				}
 
 			}
 			return ret;
 		} catch (Throwable t) {
 			// NOTE: log can be unmodifiableMap when it comes from memory
 			// buffer.
 			HashMap<String, Object> row = new HashMap<String, Object>(log.getData());
 			row.put("_table", log.getTableName());
 			row.put("_id", log.getId());
 			row.put("_time", log.getDate());
 
 			throw new LogParserBugException(t, log.getTableName(), log.getId(), log.getDate(), row);
 		}
 	}
 }
