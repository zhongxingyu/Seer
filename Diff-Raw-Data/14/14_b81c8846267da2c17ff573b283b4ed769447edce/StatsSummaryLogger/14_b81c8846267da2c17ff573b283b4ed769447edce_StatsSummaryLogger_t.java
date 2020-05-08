 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.logdb.logapi;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.araqne.log.api.*;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryContext;
 import org.araqne.logdb.Session;
 import org.araqne.logdb.query.aggregator.AggregationField;
 import org.araqne.logdb.query.aggregator.AggregationFunction;
 import org.araqne.logdb.query.parser.AggregationParser;
 import org.araqne.logdb.query.parser.StatsParser;
 import org.araqne.logdb.query.parser.StatsParser.SyntaxParseResult;
 
 public class StatsSummaryLogger extends AbstractLogger implements LoggerRegistryEventListener, LogPipe {
 	public class EmptySession implements Session {
 		@Override
 		public String getGuid() {
 			return "";
 		}
 
 		@Override
 		public String getLoginName() {
 			return "user";
 		}
 
 		@Override
 		public Date getCreated() {
 			return new Date();
 		}
 
 		@Override
 		public boolean isAdmin() {
 			return false;
 		}
 
 	}
 
 	private static final String OPT_SOURCE_LOGGER = "source_logger";
 	private static final String OPT_QUERY = "stats_query";
 	private static final String OPT_MIN_INTERVAL = "aggr_interval";
 	private static final String OPT_FLUSH_INTERVAL = "flush_interval";
 	private static final String OPT_MEMORY_ITEMSIZE = "max_itemsize";
 	private static final String OPT_PARSER = "parser";
 
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(StatsSummaryLogger.class.getName());
 	private LoggerRegistry loggerRegistry;
 
 	/**
 	 * full name of data source logger
 	 */
 	private String loggerName;
 
 	private String pattern;
 	private String queryString;
 	private int aggrInterval;
 	private long flushInterval;
 	private int maxItemSize;
 	private StatsSummaryKeyExtractor keyExtractor;
 	private AggregationFunction[] funcs;
 	private List<String> clauses;
 	private Date lastFlush = new Date();
 	private LogParserRegistry parserRegistry;
 	private String parserName;
 	private LogParser parser;
 	private ArrayBlockingQueue<Log> logList;
 
 	public StatsSummaryLogger(LoggerSpecification spec, LoggerFactory factory, LoggerRegistry loggerRegistry,
 			LogParserRegistry parserRegistry) {
 		super(spec, factory);
 		this.loggerRegistry = loggerRegistry;
 		this.parserRegistry = parserRegistry;
 		Map<String, String> config = spec.getConfig();
 		loggerName = config.get(OPT_SOURCE_LOGGER);
 		queryString = config.get(OPT_QUERY);
 		aggrInterval = Integer.parseInt(config.get(OPT_MIN_INTERVAL));
 		flushInterval = Integer.parseInt(config.get(OPT_FLUSH_INTERVAL));
 		maxItemSize = Integer.parseInt(config.get(OPT_MEMORY_ITEMSIZE));
 		parserName = config.get(OPT_PARSER);
 
 		init();
 	}
 
 	static Map<String, Class<? extends AggregationFunction>> funcTable = new HashMap<String, Class<? extends AggregationFunction>>();
 	{
 		// funcTable.put("sum", org.araqne.logdb.summary.Sum.class);
 	}
 
 	private void init() {
 		buffer = new HashMap<StatsSummaryKey, AggregationFunction[]>(maxItemSize);
 
 		// sanitize queryString
 		queryString = queryString.trim();
 		if (!queryString.startsWith("stats "))
 			queryString = "stats " + queryString;
 
 		// get AST
 		SyntaxParseResult pr = new StatsParser().parseSyntax(null, queryString);
 
 		// generate aggr fields
 		LogQueryContext context = new LogQueryContext(new EmptySession());
 		clauses = pr.clauses;
 		fields = new ArrayList<AggregationField>();
 		for (String aggTerm : pr.aggTerms) {
 			AggregationField field = AggregationParser.parse(context, aggTerm/* , funcTable */);
 			fields.add(field);
 		}
 
 		keyExtractor = new StatsSummaryKeyExtractor(aggrInterval, pr.clauses);
 
 		funcs = new AggregationFunction[fields.size()];
 		for (int i = 0; i < fields.size(); ++i) {
 			this.funcs[i] = fields.get(i).getFunction();
 		}
 
 		for (AggregationFunction f : funcs) {
 			f.clean();
 		}
 
 		logList = new ArrayBlockingQueue<Log>(250000);
 	}
 
 	@Override
 	protected void onStart() {
 		loggerRegistry.addListener(this);
 		Logger logger = loggerRegistry.getLogger(loggerName);
 
 		if (logger != null) {
 			slog.debug("araqne log api: connect pipe to source logger [{}]", loggerName);
 			logger.addLogPipe(this);
 		} else
 			slog.debug("araqne log api: source logger [{}] not found", loggerName);
		
		try {
			if (parserName != null)
				parser = parserRegistry.newParser(parserName);
		} catch (Exception e) {
			slog.warn("stats-summary-logger: could not find parser [" + parserName + "]", e);
		}
 	}
 
 	@Override
 	protected void onStop() {
 		try {
 			// XXX: not working now.
 			processLogs();
 			flush();
 
 			if (loggerRegistry != null) {
 				Logger logger = loggerRegistry.getLogger(loggerName);
 				if (logger != null) {
 					slog.debug("araqne log api: disconnect pipe from source logger [{}]", loggerName);
 					logger.removeLogPipe(this);
 				}
 
 				loggerRegistry.removeListener(this);
 			}
 		} catch (RuntimeException e) {
 			if (!e.getMessage().endsWith("unavailable"))
 				throw e;
 		}
 	}
 
 	@Override
 	public boolean isPassive() {
 		return false;
 	}
 
 	@Override
 	protected void runOnce() {
 		// if needed, flush in memory items
 		slog.trace("summary-logger - runOnce called");
 
 		processLogs();
 
 		if (needFlush())
 			flush();
 	}
 
 	private void processLogs() {
 		Log log = null;
 		while ((log = logList.peek()) != null) {
 			processLog(log);
 			logList.poll();
 		}
 	}
 
 	private boolean needFlush() {
 		// XXX
 		if (forceFlush) {
 			forceFlush = false;
 			return true;
 		}
 		long interval = 1000 * flushInterval;
 		return (lastFlush.getTime() / interval + 1) * interval < new Date().getTime();
 	}
 
 	public void flush() {
 		slog.trace("flush called");
 
 		Map<StatsSummaryKey, AggregationFunction[]> captured = buffer;
 		buffer = new HashMap<StatsSummaryKey, AggregationFunction[]>(maxItemSize);
 
 		for (StatsSummaryKey key : captured.keySet()) {
 			HashMap<String, Object> m = new HashMap<String, Object>();
 
 			// put summary values
 			AggregationFunction[] fs = captured.get(key);
 			for (int i = 0; i < fs.length; ++i) {
 				m.put(fields.get(i).getName(), fs[i].eval());
 			}
 
 			// put key
 			List<String> cs = keyExtractor.getClauses();
 			for (int i = 0; i < key.size(); ++i) {
 				m.put(cs.get(i), key.get(i));
 			}
 
 			write(new SimpleLog(key.getDate(), this.getName(), m));
 		}
 
 		lastFlush = new Date();
 	}
 
 	@Override
 	public void loggerAdded(Logger logger) {
 		if (logger.getFullName().equals(loggerName)) {
 			slog.debug("araqne log api: source logger [{}] loaded", loggerName);
 			logger.addLogPipe(this);
 		}
 	}
 
 	@Override
 	public void loggerRemoved(Logger logger) {
 		if (logger.getFullName().equals(loggerName)) {
 			slog.debug("araqne log api: source logger [{}] unloaded", loggerName);
 			logger.removeLogPipe(this);
 		}
 	}
 
 	List<Log> logs = new ArrayList<Log>();
 	private List<AggregationField> fields;
 	private int inputCount;
 
 	private Map<StatsSummaryKey, AggregationFunction[]> buffer;
 	private boolean forceFlush = false;
 	private boolean warnSuppressed;
 
 	@Override
 	public void onLog(Logger logger, Log log) {
 		try {
 			inputCount++;
 
 			if (!isRunning() && !warnSuppressed) {
 				slog.warn("logs are being dropped because logger is not running: {}", this.getName());
 				warnSuppressed = true;
 			}
 
 			if (isRunning() && warnSuppressed) {
 				warnSuppressed = false;
 			}
 
 			boolean offer = logList.offer(log, 30, TimeUnit.SECONDS);
 			if (!offer) {
 				if (!isRunning()) {
 					slog.warn("log is dropped because logger is stopped: {}", this.getName());
 				} else {
 					// second chance
 					logList.offer(log, 30, TimeUnit.SECONDS);
 					slog.error("log is dropped because logger is congested: {}", this.getName());
 				}
 			}
 
 		} catch (Throwable t) {
 			throw new IllegalStateException("logger-name: " + logger.getName() + ", log: " + log.toString(), t);
 		}
 
 	}
 
 	private void processLog(Log log) {
 		// parse log
 		Map<String, Object> parsed = log.getParams();
 		if (parser != null) {
 			// XXX: distinguish v1 v2
 			log.getParams().put("_time", log.getDate());
 			parsed = parser.parse(log.getParams());
 		}
 
 		StatsSummaryKey key = keyExtractor.extract(log);
 
 		if (!buffer.containsKey(key)) {
 			if (buffer.size() == maxItemSize)
 				flush();
 
 			AggregationFunction[] fs = new AggregationFunction[funcs.length];
 			for (int i = 0; i < fs.length; ++i) {
 				fs[i] = funcs[i].clone();
 			}
 			buffer.put(key, fs);
 		}
 		// XXX: replace LogMap to more proper type
 		AggregationFunction[] fs = buffer.get(key);
 		for (AggregationFunction f : fs) {
 			f.apply(new LogMap(parsed));
 		}
 	}
 
 	public void setForceFlush() {
 		forceFlush = true;
 	}
 }
