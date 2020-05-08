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
 package org.araqne.logdb.query.engine;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.confdb.ConfigService;
 import org.araqne.cron.TickService;
 import org.araqne.log.api.LogParserFactoryRegistry;
 import org.araqne.log.api.LogParserRegistry;
 import org.araqne.log.api.LoggerRegistry;
 import org.araqne.logdb.AbstractQueryCommandParser;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.DefaultQuery;
 import org.araqne.logdb.FunctionRegistry;
 import org.araqne.logdb.LookupHandlerRegistry;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.ProcedureRegistry;
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandParser;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryEventListener;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.QueryPlanner;
 import org.araqne.logdb.QueryResultFactory;
 import org.araqne.logdb.QueryScriptRegistry;
 import org.araqne.logdb.QueryService;
 import org.araqne.logdb.QueryStatus;
 import org.araqne.logdb.QueryStatusCallback;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.RunMode;
 import org.araqne.logdb.SavedResultManager;
 import org.araqne.logdb.Session;
 import org.araqne.logdb.SessionEventListener;
 import org.araqne.logdb.impl.QueryHelper;
 import org.araqne.logdb.query.parser.BoxPlotParser;
 import org.araqne.logdb.query.parser.ConfdbParser;
 import org.araqne.logdb.query.parser.DropParser;
 import org.araqne.logdb.query.parser.EvalParser;
 import org.araqne.logdb.query.parser.EvalcParser;
 import org.araqne.logdb.query.parser.ExecParser;
 import org.araqne.logdb.query.parser.ExplodeParser;
 import org.araqne.logdb.query.parser.FieldsParser;
 import org.araqne.logdb.query.parser.ImportParser;
 import org.araqne.logdb.query.parser.InsertParser;
 import org.araqne.logdb.query.parser.JoinParser;
 import org.araqne.logdb.query.parser.JsonFileParser;
 import org.araqne.logdb.query.parser.JsonParser;
 import org.araqne.logdb.query.parser.LimitParser;
 import org.araqne.logdb.query.parser.LoadParser;
 import org.araqne.logdb.query.parser.CheckTableParser;
 import org.araqne.logdb.query.parser.SystemCommandParser;
 import org.araqne.logdb.query.parser.LoggerParser;
 import org.araqne.logdb.query.parser.LookupParser;
 import org.araqne.logdb.query.parser.MvParser;
 import org.araqne.logdb.query.parser.OutputCsvParser;
 import org.araqne.logdb.query.parser.OutputJsonParser;
 import org.araqne.logdb.query.parser.OutputTxtParser;
 import org.araqne.logdb.query.parser.ParseCsvParser;
 import org.araqne.logdb.query.parser.ParseJsonParser;
 import org.araqne.logdb.query.parser.ParseKvParser;
 import org.araqne.logdb.query.parser.ParseParser;
 import org.araqne.logdb.query.parser.ProcParser;
 import org.araqne.logdb.query.parser.PurgeParser;
 import org.araqne.logdb.query.parser.RateLimitParser;
 import org.araqne.logdb.query.parser.RenameParser;
 import org.araqne.logdb.query.parser.RexParser;
 import org.araqne.logdb.query.parser.ScriptParser;
 import org.araqne.logdb.query.parser.SearchParser;
 import org.araqne.logdb.query.parser.SetParser;
 import org.araqne.logdb.query.parser.SignatureParser;
 import org.araqne.logdb.query.parser.SortParser;
 import org.araqne.logdb.query.parser.StatsParser;
 import org.araqne.logdb.query.parser.TableParser;
 import org.araqne.logdb.query.parser.TextFileParser;
 import org.araqne.logdb.query.parser.TimechartParser;
 import org.araqne.logdb.query.parser.TransactionParser;
 import org.araqne.logdb.query.parser.UnionParser;
 import org.araqne.logdb.query.parser.ZipFileParser;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogFileServiceRegistry;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.StorageConfig;
 import org.araqne.logstorage.TableNotFoundException;
 import org.araqne.logstorage.TableSchema;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-query")
 @Provides(specifications = { QueryService.class })
 public class QueryServiceImpl implements QueryService, SessionEventListener {
 	private static final String QUERY_LOG_TABLE = "araqne_query_logs";
 
 	private final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
 
 	@Requires
 	private ConfigService conf;
 
 	@Requires
 	private TickService tickService;
 
 	@Requires
 	private AccountService accountService;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private LookupHandlerRegistry lookupRegistry;
 
 	@Requires
 	private QueryScriptRegistry scriptRegistry;
 
 	@Requires
 	private LogParserFactoryRegistry parserFactoryRegistry;
 
 	@Requires
 	private LogParserRegistry parserRegistry;
 
 	@Requires
 	private QueryParserService queryParserService;
 
 	@Requires
 	private LogStorage storage;
 
 	@Requires
 	private LogFileServiceRegistry fileServiceRegistry;
 
 	@Requires
 	private MetadataService metadataService;
 
 	@Requires
 	private SavedResultManager savedResultManager;
 
 	@Requires
 	private LoggerRegistry loggerRegistry;
 
 	@Requires
 	private QueryResultFactory resultFactory;
 
 	@Requires
 	private FunctionRegistry functionRegistry;
 
 	@Requires
 	private ProcedureRegistry procedureRegistry;
 
 	private BundleContext bc;
 	private ConcurrentMap<Integer, Query> queries;
 
 	private CopyOnWriteArraySet<QueryEventListener> callbacks;
 
 	private List<QueryCommandParser> queryParsers;
 
 	private List<QueryPlanner> planners;
 
 	private boolean allowQueryPurge = false;
 	private boolean useBom = false;
 
 	public QueryServiceImpl(BundleContext bc) {
 		this.bc = bc;
 		this.queries = new ConcurrentHashMap<Integer, Query>();
 		this.callbacks = new CopyOnWriteArraySet<QueryEventListener>();
 		this.planners = new CopyOnWriteArrayList<QueryPlanner>();
 
 		// ensure directory
 		File dir = new File(System.getProperty("araqne.data.dir"), "araqne-logdb/query");
 		dir.mkdirs();
 
 		allowQueryPurge = Boolean.parseBoolean(System.getProperty("araqne.logdb.allowpurge"));
 		if (System.getProperty("araqne.logdb.purge") != null) {
 			String s = System.getProperty("araqne.logdb.purge");
 			allowQueryPurge = s.equalsIgnoreCase("enabled") || s.equalsIgnoreCase("true");
 		}
 
 		useBom = Boolean.parseBoolean(System.getProperty("araqne.logdb.utf8bom"));
 
 		prepareQueryParsers();
 	}
 
 	private void prepareQueryParsers() {
 		@SuppressWarnings("unchecked")
 		List<Class<? extends AbstractQueryCommandParser>> parserClazzes = Arrays.asList(DropParser.class, EvalParser.class,
 				EvalcParser.class, SearchParser.class, StatsParser.class, FieldsParser.class, SortParser.class,
 				TimechartParser.class, RenameParser.class, RexParser.class, JsonParser.class, SignatureParser.class,
 				LimitParser.class, SetParser.class, BoxPlotParser.class, ParseKvParser.class, TransactionParser.class,
 				ExplodeParser.class, ParseJsonParser.class, ExecParser.class);
 
 		List<QueryCommandParser> parsers = new ArrayList<QueryCommandParser>();
 		for (Class<? extends AbstractQueryCommandParser> clazz : parserClazzes) {
 			try {
 				parsers.add(clazz.newInstance());
 			} catch (Exception e) {
 				logger.error("araqne logdb: failed to add syntax: " + clazz.getSimpleName(), e);
 			}
 		}
 
 		// add table and lookup (need some constructor injection)
 		parsers.add(new TableParser(accountService, storage, tableRegistry, parserFactoryRegistry, parserRegistry));
 		parsers.add(new LookupParser(lookupRegistry));
 		parsers.add(new ScriptParser(bc, scriptRegistry));
 		parsers.add(new TextFileParser(parserFactoryRegistry));
 		parsers.add(new ZipFileParser(parserFactoryRegistry));
 		parsers.add(new JsonFileParser(parserFactoryRegistry));
 		parsers.add(new OutputCsvParser(useBom));
 		parsers.add(new OutputJsonParser());
 		parsers.add(new OutputTxtParser());
 		parsers.add(new SystemCommandParser("logdb", metadataService)); // deprecated
 		parsers.add(new SystemCommandParser("system", metadataService));
 		parsers.add(new CheckTableParser("logcheck", tableRegistry, storage, fileServiceRegistry)); // deprecated
 		parsers.add(new CheckTableParser("checktable", tableRegistry, storage, fileServiceRegistry));
 		parsers.add(new JoinParser(queryParserService, resultFactory));
 		parsers.add(new UnionParser(queryParserService));
 		parsers.add(new ImportParser(tableRegistry, storage));
 		parsers.add(new ParseParser(parserRegistry));
 		parsers.add(new LoadParser(savedResultManager));
 		parsers.add(new LoggerParser(loggerRegistry));
 		parsers.add(new MvParser());
 		parsers.add(new ConfdbParser(conf));
 		parsers.add(new InsertParser(storage));
 		parsers.add(new ParseCsvParser());
 		parsers.add(new ProcParser(accountService, queryParserService, procedureRegistry));
 		parsers.add(new RateLimitParser(tickService));
 		if (allowQueryPurge)
 			parsers.add(new PurgeParser(storage, tableRegistry));
 
 		this.queryParsers = parsers;
 	}
 
 	@Validate
 	public void start() {
 		// NOTE: do not call ensureTable directly, it can cause iPOJO hang.
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				storage.ensureTable(new TableSchema(QUERY_LOG_TABLE, new StorageConfig("v2")));
 			}
 		}, "Araqne Query Log Table Creator").start();
 
 		for (QueryCommandParser p : queryParsers)
 			queryParserService.addCommandParser(p);
 
 		accountService.addListener(this);
 
 		// delete all temporary query files
 		File queryResultDir = new File(System.getProperty("araqne.data.dir"), "araqne-logdb/query/");
 		File[] resultFiles = queryResultDir.listFiles();
 		if (resultFiles == null)
 			return;
 
 		for (File f : resultFiles) {
 			String name = f.getName();
 			if (name.startsWith("result") && (name.endsWith(".idx") || name.endsWith(".dat")))
 				f.delete();
 		}
 
 		// delete all temporary sort files
 		String dataDir = System.getProperty("araqne.sort.dir", System.getProperty("araqne.data.dir"));
 		File sortDir = new File(dataDir, "araqne-logdb/sort");
 
 		File[] runFiles = sortDir.listFiles();
 		if (runFiles == null)
 			return;
 
 		for (File f : runFiles) {
 			String name = f.getName();
 			if (name.startsWith("run") && (name.endsWith(".idx") || name.endsWith(".dat")))
 				f.delete();
 		}
 	}
 
 	@Invalidate
 	public void stop() {
 		for (int id : queries.keySet()) {
 			Query query = queries.get(id);
 			if (query != null) {
 				logger.info("araqne logdb: cancel query [{}:{}] due to service down", id, query.getQueryString());
 				removeQuery(id);
 			}
 		}
 
 		if (accountService != null) {
 			accountService.removeListener(this);
 		}
 
 		if (queryParserService != null) {
 			for (QueryCommandParser p : queryParsers)
 				queryParserService.removeCommandParser(p);
 		}
 	}
 
 	@Override
 	public Query createQuery(Session session, String queryString) {
 		if (logger.isDebugEnabled())
 			logger.debug("araqne logdb: try to create query [{}] from session [{}:{}]",
 					new Object[] { queryString, session.getGuid(), session.getLoginName() });
 
 		QueryContext context = new QueryContext(session);
 		List<QueryCommand> commands = queryParserService.parseCommands(context, queryString);
 		for (QueryPlanner planner : planners)
 			commands = planner.plan(context, commands);
 
 		Query query = new DefaultQuery(context, queryString, commands, resultFactory);
 		for (QueryCommand cmd : commands)
 			cmd.setQuery(query);
 
 		queries.put(query.getId(), query);
 		query.getCallbacks().getStatusCallbacks().add(new EofReceiver());
 		invokeCallbacks(query, QueryStatus.CREATED);
 
 		return query;
 	}
 
 	@Override
 	public void startQuery(int id) {
 		startQuery(null, id);
 	}
 
 	@Override
 	public void startQuery(Session session, int id) {
 		Query query = getQuery(id);
 		if (query == null)
 			throw new IllegalArgumentException("invalid log query id: " + id);
 
 		if (session != null && !query.isAccessible(session))
 			throw new IllegalArgumentException("invalid log query id: " + id);
 
 		QueryHelper.setJoinAndUnionDependencies(query.getCommands());
 
 		new Thread(query, "Query " + id).start();
 		invokeCallbacks(query, QueryStatus.STARTED);
 	}
 
 	@Override
 	public void removeQuery(int id) {
 		removeQuery(null, id);
 	}
 
 	@Override
 	public void removeQuery(Session session, int id) {
 		if (logger.isDebugEnabled()) {
 			if (session == null) {
 				logger.debug("araqne logdb: try to remove query [{}]", id);
 			} else {
 				logger.debug("araqne logdb: try to remove query [{}] from session [{}:{}]", new Object[] { id, session.getGuid(),
 						session.getLoginName() });
 			}
 		}
 
 		Query query = queries.remove(id);
 		if (query == null) {
 			logger.debug("araqne logdb: query [{}] not found, remove failed", id);
 			return;
 		}
 
 		if (session != null && !query.isAccessible(session)) {
 			Session querySession = query.getContext().getSession();
 			logger.warn("araqne logdb: security violation, [{}] access to query of login [{}] session [{}]", new Object[] {
 					session.getLoginName(), querySession.getLoginName(), querySession.getGuid() });
 			return;
 		}
 
 		try {
 			query.getCallbacks().getStatusCallbacks().clear();
 			query.getResult().getResultCallbacks().clear();
 
 			if (query.isStarted() && !query.isFinished())
 				query.stop(QueryStopReason.UserRequest);
 		} catch (Throwable t) {
 			logger.error("araqne logdb: cannot cancel query " + query, t);
 		}
 
 		try {
 			query.purge();
 		} catch (Throwable t) {
 			logger.error("araqne logdb: cannot close file buffer list for query " + query.getId(), t);
 		}
 
 		invokeCallbacks(query, QueryStatus.REMOVED);
 	}
 
 	@Override
 	public Collection<Query> getQueries() {
 		return queries.values();
 	}
 
 	@Override
 	public Collection<Query> getQueries(Session session) {
 		List<Query> l = new ArrayList<Query>();
 		for (Query q : queries.values())
 			if (q.isAccessible(session))
 				l.add(q);
 
 		return l;
 	}
 
 	@Override
 	public Query getQuery(int id) {
 		return queries.get(id);
 	}
 
 	@Override
 	public Query getQuery(Session session, int id) {
 		Query q = queries.get(id);
 		if (q == null)
 			return null;
 
 		if (!q.isAccessible(session))
 			return null;
 
 		return q;
 	}
 
 	@Override
 	public void addListener(QueryEventListener listener) {
 		callbacks.add(listener);
 	}
 
 	@Override
 	public void removeListener(QueryEventListener listener) {
 		callbacks.remove(listener);
 	}
 
 	@Override
 	public List<QueryPlanner> getPlanners() {
 		return new ArrayList<QueryPlanner>(planners);
 	}
 
 	@Override
 	public QueryPlanner getPlanner(String name) {
 		for (QueryPlanner planner : planners)
 			if (planner.getName().equals(name))
 				return planner;
 		return null;
 	}
 
 	@Override
 	public void addPlanner(QueryPlanner planner) {
 		planners.add(planner);
 	}
 
 	@Override
 	public void removePlanner(QueryPlanner planner) {
 		planners.remove(planner);
 	}
 
 	private void invokeCallbacks(Query query, QueryStatus status) {
 		logger.debug("araqne logdb: invoking callback to notify query [{}], status [{}]", query.getId(), status);
 		for (QueryEventListener callback : callbacks) {
 			try {
 				callback.onQueryStatusChange(query, status);
 			} catch (Exception e) {
 				logger.warn("araqne logdb: query event listener should not throw any exception", e);
 			}
 		}
 	}
 
 	/**
 	 * @since 0.17.0
 	 */
 	@Override
 	public void onLogin(Session session) {
 	}
 
 	/**
 	 * @since 0.17.0
 	 */
 	@Override
 	public void onLogout(Session session) {
 		for (Query q : queries.values()) {
			if (q.getContext() == null || q.getContext().getSession() == null)
				continue;

 			if (q.getRunMode() == RunMode.FOREGROUND && q.getContext().getSession().equals(session)) {
 				logger.trace("araqne logdb: removing foreground query [{}:{}] by session [{}] logout", new Object[] { q.getId(),
 						q.getQueryString(), session.getLoginName() });
 				removeQuery(q.getId());
 			}
 		}
 	}
 
 	private class EofReceiver implements QueryStatusCallback {
 		@Override
 		public void onChange(Query query) {
 			if (!query.isFinished())
 				return;
 
 			// prevent duplicated logging
 			query.getCallbacks().getStatusCallbacks().remove(this);
 
 			Date now = new Date();
 
 			HashMap<String, Object> m = new HashMap<String, Object>();
 			m.put("query_id", query.getId());
 			m.put("query_string", query.getQueryString());
 			try {
 				m.put("rows", query.getResultCount());
 			} catch (IOException e) {
 				m.put("rows", 0);
 			}
 			m.put("start_at", new Date(query.getStartTime()));
 			m.put("eof_at", now);
 			m.put("login_name", query.getContext().getSession().getLoginName());
 			m.put("cancelled", query.isCancelled());
 
 			if (query.isStarted())
 				m.put("duration", (query.getFinishTime() - query.getStartTime()) / 1000.0);
 			else
 				m.put("duration", 0);
 
 			try {
 				storage.write(new Log(QUERY_LOG_TABLE, now, m));
 			} catch (InterruptedException e) {
 				logger.warn("writing query log is interrupted: {}", m);
 			} catch (TableNotFoundException e) {
 				storage.ensureTable(new TableSchema(QUERY_LOG_TABLE, new StorageConfig("v2")));
 				try {
 					storage.write(new Log(QUERY_LOG_TABLE, now, m));
 				} catch (InterruptedException e1) {
 				}
 			}
 
 			invokeCallbacks(query, QueryStatus.EOF);
 		}
 	}
 }
