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
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserBuilder;
 import org.araqne.log.api.LogParserFactory;
 import org.araqne.log.api.LogParserFactoryRegistry;
 import org.araqne.log.api.LogParserRegistry;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.impl.Strings;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.LogTraverseCallback;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Table extends LogQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(Table.class);
 	private LogStorage storage;
 	private LogTableRegistry tableRegistry;
 	private LogParserFactoryRegistry parserFactoryRegistry;
 	private LogParserRegistry parserRegistry;
 
 	private TableParams params = new TableParams();
 
 	public Table(TableParams params) {
 		this.params = params;
 	}
 
 	public List<String> getTableNames() {
 		return params.tableNames;
 	}
 
 	public void setTableNames(List<String> tableNames) {
 		params.tableNames = tableNames;
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
 
 	private class TableLogParserBuilder implements LogParserBuilder {
 		LogParserRegistry parserRegistry;
 		
 		String tableParserName = null;
 		String tableParserFactoryName = null;
 		
 		LogParserFactory tableParserFactory = null;
 		Map<String, String> parserProperty = null;
 		
 		public TableLogParserBuilder(LogParserRegistry parserRegistry, LogParserFactoryRegistry parserFactoryRegistry,
 				LogTableRegistry tableRegistry, String tableName) {
 			this.parserRegistry = parserRegistry;
 
 			if (tableName != null) {
 				this.tableParserName = tableRegistry.getTableMetadata(tableName, "parser");
 				this.tableParserFactoryName = tableRegistry.getTableMetadata(tableName, "logparser");
 
 				if (tableParserFactoryName != null) {
 					this.tableParserFactory = parserFactoryRegistry.get(tableParserFactoryName);
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
 	}
 
 	@Override
 	public void start() {
 		try {
 			status = Status.Running;
 			
 			ResultSink sink = new ResultSink(this, params.offset, params.limit);
 
 			for (String tableName : params.tableNames) {
 				LogParserBuilder builder = new TableLogParserBuilder(parserRegistry, parserFactoryRegistry, tableRegistry, tableName);
 
 				storage.search(tableName, params.from, params.to,  
 						builder, new LogTraverseCallbackImpl(this, sink));
 				if (sink.isEof())
 					break;
 			}
 		} catch (InterruptedException e) {
 			logger.trace("araqne logdb: query interrupted");
 		} catch (Exception e) {
 			logger.error("araqne logdb: table exception", e);
 		} catch (Error e) {
 			logger.error("araqne logdb: table error", e);
 		} finally {
 			eof(false);
 		}
 	}
 
 	@Override
 	public void push(LogMap m) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 	
 	private static class ResultSink extends LogTraverseCallback.Sink {
 		private final Table self;
 
 		public ResultSink(Table self, long offset, long limit) {
 			super(offset, limit);
 			this.self = self;
 		}
 
 		@Override
 		protected void processLogs(List<Log> logs) {
 			for (Log log : logs) {
 				self.write(new LogMap(log.getData()));
 			}
 		}
 		
 	}
 	
 	private static class LogTraverseCallbackImpl extends LogTraverseCallback {
 		private final Table self;
 		LogTraverseCallbackImpl(Table self, Sink sink) {
 			super(sink);
 			this.self = self;
 		}
 		
 		@Override
 		public void interrupt() {
 			self.eof(true);
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			return self.status.equals(Status.End);
 		}
 
 		@Override
 		protected List<Log> filter(List<Log> logs) {
 			return logs;
 		}		
 	}
 
 	public static class TableParams {
 		private List<String> tableNames;
 		private long offset;
 		private long limit;
 		private Date from;
 		private Date to;
 		private String parserName;
 
 		public List<String> getTableNames() {
 			return tableNames;
 		}
 
 		public void setTableNames(List<String> tableNames) {
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
 
 		public String getParserName() {
 			return parserName;
 		}
 
 		public void setParserName(String parserName) {
 			this.parserName = parserName;
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
 
 		s += " offset=" + params.getOffset();
 
 		if (params.getLimit() > 0)
 			s += " limit=" + params.getLimit();
 
 		return s + " " + Strings.join(params.getTableNames(), ", ");
 	}
 }
