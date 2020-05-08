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
 package org.araqne.logdb.client;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.ExecutionException;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 import org.araqne.api.PrimitiveConverter;
 import org.araqne.codec.EncodingRule;
 import org.araqne.logdb.client.http.WebSocketTransport;
 import org.araqne.logdb.client.http.impl.StreamingResultDecoder;
 import org.araqne.logdb.client.http.impl.StreamingResultEncoder;
 import org.araqne.logdb.client.http.impl.TrapListener;
 import org.araqne.websocket.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * <p>
  * 쿼리 실행, 로그 수집 설정, 트랜스포머 설정, 테이블, 인덱스, 스트림 쿼리 설정, 예약된 쿼리 설정, 계정 관리 등 로그프레소를
  * 원격으로 제어하는데 필요한 모든 기능을 제공합니다.
  * </p>
  * 
  * <p>
  * 아래는 접속과 쿼리를 수행하는 간단한 예시입니다:
  * </p>
  * 
  * <pre>
  * {@link LogDbClient} client = null;
  * {@link LogCursor} cursor = null;
  * 
  * try {
  * 	client = new LogDbClient();
  * 	client.connect(&quot;localhost&quot;, 8888, &quot;araqne&quot;, &quot;&quot;);
  * 	cursor = client.query(&quot;logdb tables&quot;);
  * 
  * 	while (cursor.hasNext()) {
  * 		System.out.println(cursor.next());
  * 	}
  * } finally {
  * 	if (cursor != null)
  * 		cursor.close();
  * 
  * 	if (client != null)
  * 		client.close();
  * }
  * </pre>
  * 
  * @since 0.5.0
  * @author xeraph@eediom.com
  * 
  */
 public class LogDbClient implements TrapListener, Closeable {
 	private Logger logger = LoggerFactory.getLogger(LogDbClient.class);
 	private LogDbTransport transport;
 	private LogDbSession session;
 	private int fetchSize = 10000;
 	private ConcurrentMap<Integer, LogQuery> queries = new ConcurrentHashMap<Integer, LogQuery>();
 	private ConcurrentMap<Integer, StreamingResultSet> streamCallbacks = new ConcurrentHashMap<Integer, StreamingResultSet>();
 	private Locale locale = Locale.getDefault();
 	private StreamingResultDecoder streamingDecoder;
 
 	private Timer timer;
 	private StreamingResultEncoder streamingEncoder;
 	private int counter = 0;
 
 	private int insertBatchSize = 5000;
 
 	// milliseconds
 	private int indexFlushInterval = 1000;
 
 	// table name to row list mappings
 	private Map<String, List<Row>> flushBuffers = new HashMap<String, List<Row>>();
 	private CopyOnWriteArraySet<FailureListener> failureListeners = new CopyOnWriteArraySet<FailureListener>();
 
 	public LogDbClient() {
 		this(new WebSocketTransport());
 	}
 
 	public LogDbClient(LogDbTransport transport) {
 		this.transport = transport;
 		int poolSize = Math.min(8, Runtime.getRuntime().availableProcessors());
 		this.streamingDecoder = new StreamingResultDecoder("Streaming Result Decoder", poolSize);
 		this.streamingEncoder = new StreamingResultEncoder("Streaming Result Incoder", poolSize);
 	}
 
 	public Locale getLocale() {
 		return locale;
 
 	}
 
 	public void setLocale(Locale locale) {
 		checkNotNull("locale", locale);
 		this.locale = locale;
 	}
 
 	/**
 	 * 현재 접속된 세션 개체를 반환합니다.
 	 * 
 	 * @since 0.9.1
 	 */
 	public LogDbSession getSession() {
 		return session;
 	}
 
 	/**
 	 * 현재 커서가 서버에서 한 번에 조회하는 행의 갯수를 조회합니다. 기본값은 10000입니다.
 	 * 
 	 * @since 0.6.0
 	 */
 	public int getFetchSize() {
 		return fetchSize;
 	}
 
 	/**
 	 * 커서가 서버에서 한 번에 조회하는 행의 갯수를 설정합니다. 이 수치가 클수록 RPC 통신 횟수가 줄어들지만, 반대로 클라이언트와
 	 * 서버의 메모리 소모와 RPC 통신 시의 소요 시간이 증가합니다. 반대로 너무 작으면 RPC 통신 회수가 증가하므로 쿼리 결과를
 	 * 가져오는 속도가 느려질 수 있습니다.
 	 * 
 	 * @param fetchSize
 	 *            RPC 호출마다 가져오는 행의 갯수
 	 * @since 0.6.0
 	 */
 	public void setFetchSize(int fetchSize) {
 		this.fetchSize = fetchSize;
 	}
 
 	public int getInsertFetchSize() {
 		return insertBatchSize;
 	}
 
 	public void setInserFetchSize(int insertFetchSize) {
 		if (insertFetchSize < 0 || insertFetchSize > 200000)
 			throw new IllegalArgumentException("InsertFetchSize should be > 0 and < 200000");
 		this.insertBatchSize = insertFetchSize;
 	}
 
 	// index flush interval (ms)
 	public int getIndexFlushInterval() {
 		return indexFlushInterval;
 	}
 
 	public void setIndexFlushInterval(int millisec) {
 		if (millisec < 0)
 			throw new IllegalArgumentException("Index flush interval should be greater than 0");
 		this.indexFlushInterval = millisec;
 		if (timer != null) {
 			timer.cancel();
 			timer = null;
 			timer = new Timer("Insert Flush Timer");
 			timer.schedule(new FlushTask(), indexFlushInterval, indexFlushInterval);
 		}
 	}
 
 	/**
 	 * 연결 해제 상태 여부를 조회합니다.
 	 * 
 	 * @return 접속한 적이 없거나, 기존 연결이 닫힌 경우 true를 반환합니다.
 	 */
 	public boolean isClosed() {
 		return session == null || session.isClosed();
 	}
 
 	/**
 	 * 현재 세션에서 실행 중인 로그 쿼리 목록을 조회합니다. 포어그라운드와 백그라운드 실행 중인 모든 쿼리 정보가 반환됩니다.
 	 * 
 	 * @return 현재 세션에서 실행 중인 로그 쿼리 목록이 LogQuery 개체의 리스트로 반환됩니다.
 	 */
 	public List<LogQuery> getQueries() throws IOException {
 		Message resp = rpc("org.araqne.logdb.msgbus.LogQueryPlugin.queries");
 
 		@SuppressWarnings("unchecked")
 		List<Map<String, Object>> l = (List<Map<String, Object>>) resp.getParameters().get("queries");
 
 		for (Map<String, Object> q : l) {
 			int queryId = (Integer) q.get("id");
 			LogQuery query = queries.get(queryId);
 			if (query == null) {
 				query = new LogQuery(this, queryId, (String) q.get("query_string"));
 				LogQuery old = queries.putIfAbsent(queryId, query);
 				if (old != null)
 					query = old;
 			}
 
 			parseQueryStatus(q, query);
 		}
 
 		return new ArrayList<LogQuery>(queries.values());
 	}
 
 	/**
 	 * 특정 쿼리 ID에 대응하는 쿼리 실행 정보를 조회합니다. 호출 시마다 RPC 호출이 발생하면서 쿼리 상태 정보를 갱신합니다. 지정된
 	 * 쿼리 ID가 존재하지 않거나 액세스 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param id
 	 *            쿼리 ID
 	 * @return 갱신된 쿼리 실행 정보를 담고 있는 LogQuery 개체가 반환됩니다.
 	 */
 	public LogQuery getQuery(int id) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 
 		try {
 			Message resp = rpc("org.araqne.logdb.msgbus.LogQueryPlugin.queryStatus", params);
 
 			Map<String, Object> q = resp.getParameters();
 			int queryId = (Integer) q.get("id");
 			LogQuery query = queries.get(queryId);
 			if (query == null) {
 				query = new LogQuery(this, queryId, (String) q.get("query_string"));
 				LogQuery old = queries.putIfAbsent(queryId, query);
 				if (old != null)
 					query = old;
 			}
 
 			parseQueryStatus(q, query);
 		} catch (MessageException t) {
 			if (!t.getMessage().startsWith("msgbus-handler-not-found"))
 				throw t;
 		}
 
 		return queries.get(id);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void parseQueryStatus(Map<String, Object> q, LogQuery query) {
 		List<LogQueryCommand> commands = new ArrayList<LogQueryCommand>();
 
 		List<Map<String, Object>> cl = (List<Map<String, Object>>) q.get("commands");
 		for (Map<String, Object> cm : cl) {
 			commands.add(parseCommand(cm));
 		}
 
 		long stamp = 0;
 		if (q.containsKey("stamp"))
 			stamp = Long.parseLong(q.get("stamp").toString());
 
 		query.setCommands(commands);
 		boolean end = (Boolean) q.get("is_end");
 
 		boolean eof = end;
 		if (q.containsKey("is_eof"))
 			eof = (Boolean) q.get("is_eof");
 
 		boolean cancelled = false;
 		if (q.containsKey("is_cancelled"))
 			cancelled = (Boolean) q.get("is_cancelled");
 
 		if (eof) {
 			if (!query.getCommands().get(0).getStatus().equalsIgnoreCase("Waiting"))
 				query.updateStatus("Ended", stamp);
 
 			if (cancelled)
 				query.updateStatus("Cancelled", stamp);
 		} else if (end) {
 			query.updateStatus("Stopped", stamp);
 		} else {
 			query.updateStatus("Running", stamp);
 		}
 
 		if (q.containsKey("background"))
 			query.setBackground((Boolean) q.get("background"));
 
 		query.setElapsed(toLong(q.get("elapsed")));
 
 		Long startTime = toLong(q.get("start_time"));
 		Long finishTime = toLong(q.get("finish_time"));
 
 		if (startTime != null && startTime != 0)
 			query.setStartTime(new Date(startTime));
 
 		if (finishTime != null && finishTime != 0)
 			query.setFinishTime(new Date(finishTime));
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 		if (q.get("last_started") != null)
 			query.setLastStarted(df.parse((String) q.get("last_started"), new ParsePosition(0)));
 
 		List<Object> subQueries = (List<Object>) q.get("sub_queries");
 		if (subQueries != null) {
 			for (Object o : subQueries) {
 				SubQuery subQuery = parseSubQuery((Map<String, Object>) o);
 				query.getSubQueries().add(subQuery);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private SubQuery parseSubQuery(Map<String, Object> m) {
 		SubQuery q = new SubQuery();
 		q.setId((Integer) m.get("id"));
 		List<Object> l = (List<Object>) m.get("commands");
 		if (l != null) {
 			for (Object o : l)
 				q.getCommands().add(parseCommand((Map<String, Object>) o));
 		}
 		return q;
 	}
 
 	@SuppressWarnings("unchecked")
 	private LogQueryCommand parseCommand(Map<String, Object> m) {
 		LogQueryCommand c = new LogQueryCommand();
 		c.setName((String) m.get("name"));
 		c.setStatus((String) m.get("status"));
 		c.setPushCount(toLong(m.get("push_count")));
 		c.setCommand((String) m.get("command"));
 
 		List<Object> l = (List<Object>) m.get("commands");
 		if (l != null) {
 			for (Object o : l)
 				c.getCommands().add(parseCommand((Map<String, Object>) o));
 		}
 
 		return c;
 	}
 
 	private Long toLong(Object v) {
 		if (v == null)
 			return null;
 
 		if (v instanceof Integer)
 			return (long) (Integer) v;
 
 		if (v instanceof Long)
 			return (Long) v;
 
 		return null;
 	}
 
 	/**
 	 * 로그프레소 서버에 접속을 시도합니다. 잘못된 IP 주소, DNS 조회 실패, 방화벽 문제 혹은 암호 실패 등의 원인으로 접속 실패
 	 * 시 IOException 예외가 발생합니다.
 	 * 
 	 * @param host
 	 *            도메인 혹은 IP 주소
 	 * @param loginName
 	 *            DB 계정
 	 * @param password
 	 *            DB 암호
 	 */
 	public void connect(String host, String loginName, String password) throws IOException {
 		connect(host, 80, loginName, password);
 	}
 
 	/**
 	 * 로그프레소 서버에 접속을 시도합니다. 잘못된 IP 주소, DNS 조회 실패, 방화벽 문제 혹은 암호 실패 등의 원인으로 접속 실패
 	 * 시 IOException 예외가 발생합니다.
 	 * 
 	 * @param host
 	 *            도메인 혹은 IP 주소
 	 * @param port
 	 *            포트 번호
 	 * @param loginName
 	 *            DB 계정
 	 * @param password
 	 *            DB 암호
 	 */
 	public void connect(String host, int port, String loginName, String password) throws IOException {
 		this.session = transport.newSession(host, port);
 		this.session.login(loginName, password, true);
 		this.session.addListener(this);
 	}
 
 	/**
 	 * 로그 저장 설정 개체 목록을 조회합니다.
 	 * 
 	 * @return 저장 설정 개체의 리스트
 	 */
 	@SuppressWarnings("unchecked")
 	public List<ArchiveConfig> listArchiveConfigs() throws IOException {
 		List<ArchiveConfig> configs = new ArrayList<ArchiveConfig>();
 		Message resp = rpc("com.logpresso.core.msgbus.ArchivePlugin.getConfigs");
 		List<Map<String, Object>> l = (List<Map<String, Object>>) resp.get("configs");
 		for (Map<String, Object> m : l) {
 			configs.add(parseArchiveConfig(m));
 		}
 
 		return configs;
 	}
 
 	/**
 	 * 지정된 로그 수집기 이름과 대응하는 저장 설정을 조회합니다. 로그 수집기가 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param loggerName
 	 *            이름공간\이름 형식의 로그 수집기 이름 (NULL 허용 안 함)
 	 * @return 지정된 로그 이름과 대응하는 저장 설정 개체
 	 */
 	@SuppressWarnings("unchecked")
 	public ArchiveConfig getArchiveConfig(String loggerName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", loggerName);
 
 		Message resp = rpc("com.logpresso.core.msgbus.ArchivePlugin.getConfig", params);
 		Map<String, Object> m = (Map<String, Object>) resp.getParameters().get("config");
 		return parseArchiveConfig(m);
 	}
 
 	@SuppressWarnings("unchecked")
 	private ArchiveConfig parseArchiveConfig(Map<String, Object> m) {
 		ArchiveConfig c = new ArchiveConfig();
 		c.setLoggerName((String) m.get("logger"));
 		c.setTableName((String) m.get("table"));
 		c.setHost((String) m.get("host"));
 		c.setPrimaryLogger((String) m.get("primary_logger"));
 		c.setBackupLogger((String) m.get("backup_logger"));
 		c.setEnabled((Boolean) m.get("enabled"));
 		c.setMetadata((Map<String, String>) m.get("metadata"));
 		return c;
 	}
 
 	/**
 	 * 새 로그 저장 설정을 생성합니다. 로그 수집기에서 수집되는 모든 로그를 지정된 테이블에 저장하도록 설정합니다. 로그 수집기 이름이
 	 * 중복되는 경우에 예외가 발생합니다. 테이블이 존재하지 않으면 저장 설정의 메타데이터를 이용하여 테이블이 자동 생성됩니다.
 	 * 
 	 * @param config
 	 *            로그 저장 설정 (NULL 허용 안 함)
 	 */
 	public void createArchiveConfig(ArchiveConfig config) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", config.getLoggerName());
 		params.put("table", config.getTableName());
 		params.put("host", config.getHost());
 		params.put("enabled", config.isEnabled());
 		params.put("metadata", config.getMetadata());
 		rpc("com.logpresso.core.msgbus.ArchivePlugin.createConfig", params);
 	}
 
 	/**
 	 * 지정된 로그 수집기 이름으로 된 저장 설정을 삭제합니다. 지정된 로그 수집기 이름의 설정이 존재하지 않으면 예외가 발생합니다.
 	 * 
 	 * @param loggerName
 	 *            이름공간\이름 형식의 로그 수집기 이름 (NULL 허용 안 함)
 	 */
 	public void removeArchiveConfig(String loggerName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", loggerName);
 		rpc("com.logpresso.core.msgbus.ArchivePlugin.removeConfig", params);
 	}
 
 	/**
 	 * DB 계정 목록을 조회합니다.
 	 * 
 	 * @return 계정정보 목록
 	 */
 	@SuppressWarnings("unchecked")
 	public List<AccountInfo> listAccounts() throws IOException {
 		Message resp = rpc("org.araqne.logdb.msgbus.ManagementPlugin.listAccounts");
 		List<AccountInfo> accounts = new ArrayList<AccountInfo>();
 		List<Object> l = (List<Object>) resp.get("accounts");
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			List<Object> pl = (List<Object>) m.get("privileges");
 
 			AccountInfo account = new AccountInfo();
 			String loginName = (String) m.get("login_name");
 			account.setLoginName(loginName);
 
 			for (Object o2 : pl) {
 				Map<String, Object> m2 = (Map<String, Object>) o2;
 				String tableName = (String) m2.get("table_name");
 				Privilege p = new Privilege(loginName, tableName);
 				account.getPrivileges().add(p);
 			}
 			accounts.add(account);
 		}
 
 		return accounts;
 	}
 
 	/**
 	 * 새로운 DB 계정을 생성합니다. 관리자 계정이 아니거나 계정 이름이 중복된 경우 예외가 발생합니다.
 	 * 
 	 * @param account
 	 *            새로 생성할 계정 정보 (NULL 허용 안 함)
 	 */
 	public void createAccount(AccountInfo account) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", account.getLoginName());
 		params.put("password", account.getPassword());
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.createAccount", params);
 	}
 
 	/**
 	 * 기존 DB 계정을 삭제합니다. 관리자 계정이 아니거나 계정이 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param loginName
 	 *            삭제할 계정 이름 (NULL 허용 안 함)
 	 */
 	public void removeAccount(String loginName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", loginName);
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.removeAccount", params);
 	}
 
 	/**
 	 * DB 계정의 암호를 변경합니다. 관리자 계정이거나 자신의 계정 암호인 경우에만 변경 가능하고, 그 외의 경우에는 예외가 발생합니다.
 	 * 존재하지 않는 계정인 경우에도 예외가 발생합니다.
 	 * 
 	 * @param loginName
 	 *            DB 계정 이름 (NULL 허용 안 함)
 	 * @param password
 	 *            새 암호 (NULL 허용 안 함)
 	 */
 	public void changePassword(String loginName, String password) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", loginName);
 		params.put("password", password);
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.changePassword", params);
 	}
 
 	/**
 	 * 계정에 테이블 접근 권한을 부여합니다. 관리자 계정이 아니거나, 권한을 부여할 계정이 존재하지 않거나, 테이블이 존재하지 않는 경우
 	 * 예외가 발생합니다.
 	 * 
 	 * @param privilege
 	 *            권한을 부여할 계정과 테이블 매핑 (NULL 허용 안 함)
 	 */
 	public void grantPrivilege(Privilege privilege) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", privilege.getLoginName());
 		params.put("table_name", privilege.getTableName());
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.grantPrivilege", params);
 	}
 
 	/**
 	 * 계정의 테이블 접근 권한을 박탈합니다. 관리자 계정이 아니거나, 권한을 박탈할 계정이 존재하지 않거나, 테이블이 존재하지 않는 경우
 	 * 예외가 발생합니다.
 	 * 
 	 * @param privilege
 	 *            권한을 박탈할 계정과 테이블 매핑 (NULL 허용 안 함)
 	 */
 	public void revokePrivilege(Privilege privilege) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", privilege.getLoginName());
 		params.put("table_name", privilege.getTableName());
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.revokePrivilege", params);
 	}
 
 	/**
 	 * 인덱스 토크나이저 유형 목록을 조회합니다. 일반적으로 createIndex() 할 때 사용자에게 설정 가능한 인덱스 토크나이저
 	 * 목록을 보여주기 위해서 호출합니다. 관리자 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @return 인덱스 토크나이저 유형 목록이 리스트로 반환됩니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<IndexTokenizerFactoryInfo> listIndexTokenizerFactories() throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("locale", locale.getLanguage());
 
 		Message resp = rpc("com.logpresso.index.msgbus.ManagementPlugin.listIndexTokenizerFactories", params);
 
 		List<IndexTokenizerFactoryInfo> l = new ArrayList<IndexTokenizerFactoryInfo>();
 		for (Object o : (List<Object>) resp.getParameters().get("factories")) {
 			IndexTokenizerFactoryInfo f = parseIndexTokenizerFactory(o);
 			l.add(f);
 		}
 
 		return l;
 	}
 
 	/**
 	 * 지정된 이름을 가진 인덱스 토크나이저 유형을 조회합니다. 관리자 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            인덱스 토크나이저 유형 이름 (NULL 허용 안 함)
 	 * @return 지정된 이름을 가진 인덱스 토크나이저 유형 개체를 반환합니다.
 	 */
 	public IndexTokenizerFactoryInfo getIndexTokenizerFactory(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 		params.put("locale", locale.getLanguage());
 
 		Message resp = rpc("com.logpresso.index.msgbus.ManagementPlugin.getIndexTokenizerFactoryInfo", params);
 		return parseIndexTokenizerFactory(resp.getParameters().get("factory"));
 	}
 
 	@SuppressWarnings("unchecked")
 	private IndexTokenizerFactoryInfo parseIndexTokenizerFactory(Object o) {
 		Map<String, Object> m = (Map<String, Object>) o;
 		IndexTokenizerFactoryInfo f = new IndexTokenizerFactoryInfo();
 		f.setName((String) m.get("name"));
 		f.setConfigSpecs(parseIndexConfigList((List<Object>) m.get("config_specs")));
 		return f;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<IndexConfigSpec> parseIndexConfigList(List<Object> l) {
 		List<IndexConfigSpec> specs = new ArrayList<IndexConfigSpec>();
 
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			IndexConfigSpec spec = new IndexConfigSpec();
 			spec.setKey((String) m.get("key"));
 			spec.setName((String) m.get("name"));
 			spec.setDescription((String) m.get("description"));
 			spec.setRequired((Boolean) m.get("required"));
 			specs.add(spec);
 		}
 
 		return specs;
 	}
 
 	/**
 	 * 인덱스 목록을 반환합니다. 관리자 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용), NULL인 경우 전체 인덱스 목록을, 테이블 이름이 지정된 경우에는 테이블
 	 *            이름으로 필터링된 결과를 반환합니다.
 	 * @return 인덱스 정보 개체의 리스트를 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<IndexInfo> listIndexes(String tableName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 
 		Message resp = rpc("com.logpresso.index.msgbus.ManagementPlugin.listIndexes", params);
 		List<IndexInfo> indexes = new ArrayList<IndexInfo>();
 
 		List<Object> l = (List<Object>) resp.getParameters().get("indexes");
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			IndexInfo indexInfo = getIndexInfo(m);
 			indexes.add(indexInfo);
 		}
 
 		return indexes;
 	}
 
 	/**
 	 * 주어진 테이블 및 인덱스 이름과 일치하는 인덱스 정보를 반환합니다. 관리자 권한이 없거나 해당 인덱스가 존재하지 않는 경우 예외가
 	 * 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 * @param indexName
 	 *            인덱스 이름 (NULL 허용 안 함)
 	 * @return 주어진 테이블 및 인덱스 이름과 일치하는 인덱스 정보 개체를 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public IndexInfo getIndexInfo(String tableName, String indexName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("index", indexName);
 
 		Message resp = rpc("com.logpresso.index.msgbus.ManagementPlugin.getIndexInfo", params);
 		return getIndexInfo((Map<String, Object>) resp.getParameters().get("index"));
 	}
 
 	/**
 	 * 특정 인덱스에 데이터가 입력될 때 풀텍스트 토큰이 어떻게 추출되는지 시험합니다. 정상적으로 인덱스가 설정되었는지, 의도한대로
 	 * 풀텍스트 인덱싱이 진행되는지 확인할 용도로 사용합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 * @param indexName
 	 *            인덱스 이름 (NULL 허용 안 함)
 	 * @param data
 	 *            키/값 쌍으로 구성된 원본 데이터 (NULL 허용 안 함), 일반적으로 텍스트 파일에서 데이터를 읽어오거나
 	 *            시스로그와 같은 경우 원본 데이터가 line 키와 값으로 구성됩니다. SNMP 트랩의 경우 OID와 변수 바인딩
 	 *            목록, 윈도우 이벤트 로그의 경우 이벤트 로그 필드 이름과 값 목록이 데이터 원본 키/값 쌍으로 반영됩니다.
 	 * @return 추출된 풀텍스트 토큰 문자열 집합
 	 * @since 0.8.1
 	 */
 	@SuppressWarnings("unchecked")
 	public Set<String> testIndexTokenizer(String tableName, String indexName, Map<String, Object> data) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("index", indexName);
 		params.put("data", data);
 
 		Message resp = rpc("com.logpresso.index.msgbus.ManagementPlugin.testIndexTokenizer", params);
 		return new HashSet<String>((List<String>) resp.getParameters().get("tokens"));
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private IndexInfo getIndexInfo(Map<String, Object> m) {
 		IndexInfo index = new IndexInfo();
 		index.setTableName((String) m.get("table"));
 		index.setIndexName((String) m.get("index"));
 		index.setTokenizerName((String) m.get("tokenizer_name"));
 		index.setTokenizerConfigs((Map<String, String>) m.get("tokenizer_configs"));
 
 		try {
 			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 			String s = (String) m.get("min_index_day");
 			if (s != null)
 				index.setMinIndexDay(f.parse(s));
 		} catch (ParseException e) {
 		}
 
 		index.setUseBloomFilter((Boolean) m.get("use_bloom_filter"));
 		index.setBloomFilterCapacity0((Integer) m.get("bf_lv0_capacity"));
 		index.setBloomFilterErrorRate0((Double) m.get("bf_lv0_error_rate"));
 		index.setBloomFilterCapacity1((Integer) m.get("bf_lv1_capacity"));
 		index.setBloomFilterErrorRate1((Double) m.get("bf_lv1_error_rate"));
 		index.setBasePath((String) m.get("base_path"));
 		index.setBuildPastIndex((Boolean) m.get("build_past_index"));
 
 		return index;
 	}
 
 	/**
 	 * 새 인덱스를 생성합니다. 관리자 권한이 없거나, 테이블이 존재하지 않거나, 인덱스 이름이 중복되는 경우 예외가 발생합니다.
 	 * 
 	 * @param info
 	 *            새 인덱스를 생성하는데 필요한 설정 (NULL 허용 안 함)
 	 */
 	public void createIndex(IndexInfo info) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", info.getTableName());
 		params.put("index", info.getIndexName());
 		params.put("tokenizer_name", info.getTokenizerName());
 		params.put("tokenizer_configs", info.getTokenizerConfigs());
 		params.put("base_path", info.getBasePath());
 		params.put("use_bloom_filter", info.isUseBloomFilter());
 		params.put("bf_lv0_capacity", info.getBloomFilterCapacity0());
 		params.put("bf_lv0_error_rate", info.getBloomFilterErrorRate0());
 		params.put("bf_lv1_capacity", info.getBloomFilterCapacity1());
 		params.put("bf_lv1_error_rate", info.getBloomFilterErrorRate1());
 		params.put("min_index_day", info.getMinIndexDay());
 		params.put("build_past_index", info.isBuildPastIndex());
 
 		rpc("com.logpresso.index.msgbus.ManagementPlugin.createIndex", params);
 	}
 
 	/**
 	 * 인덱스를 삭제합니다. 진행 중인 백그라운드 배치 인덱스 생성 작업은 취소됩니다. 관리자 권한이 없거나 테이블이나 인덱스가 존재하지
 	 * 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 * @param indexName
 	 *            인덱스 이름 (NULL 허용 안 함)
 	 */
 	public void dropIndex(String tableName, String indexName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("index", indexName);
 
 		rpc("com.logpresso.index.msgbus.ManagementPlugin.dropIndex", params);
 	}
 
 	/**
 	 * 액세스 가능한 전체 테이블 목록을 반환합니다.
 	 * 
 	 * @return 테이블 정보를 담은 TableInfo 개체의 리스트
 	 */
 	@SuppressWarnings("unchecked")
 	public List<TableInfo> listTables() throws IOException {
 		Message resp = rpc("org.araqne.logdb.msgbus.ManagementPlugin.listTables");
 		List<TableInfo> tables = new ArrayList<TableInfo>();
 		Map<String, Object> metadataMap = (Map<String, Object>) resp.getParameters().get("tables");
 		Map<String, Object> fieldsMap = (Map<String, Object>) resp.getParameters().get("fields");
 
 		for (String tableName : metadataMap.keySet()) {
 			Map<String, Object> params = (Map<String, Object>) metadataMap.get(tableName);
 			List<Object> fields = (List<Object>) fieldsMap.get(tableName);
 			TableInfo tableInfo = getTableInfo(tableName, params, fields);
 			tables.add(tableInfo);
 		}
 
 		return tables;
 	}
 
 	/**
 	 * 특정 테이블 정보를 반환합니다. 테이블이 존재하지 않거나, 액세스 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            조회할 테이블 이름 (NULL 허용 안 함)
 	 * @return 테이블 이름과 대응되는 테이블 정보
 	 */
 	@SuppressWarnings("unchecked")
 	public TableInfo getTableInfo(String tableName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		Message resp = rpc("org.araqne.logdb.msgbus.ManagementPlugin.getTableInfo", params);
 
 		return getTableInfo(tableName, (Map<String, Object>) resp.get("table"), (List<Object>) resp.get("fields"));
 	}
 
 	private TableInfo getTableInfo(String tableName, Map<String, Object> params, List<Object> fields) {
 		Map<String, String> metadata = new HashMap<String, String>();
 		for (Entry<String, Object> pair : params.entrySet())
 			metadata.put(pair.getKey(), pair.getValue() == null ? null : pair.getValue().toString());
 
 		List<FieldInfo> fieldDefs = null;
 		if (fields != null) {
 			fieldDefs = new ArrayList<FieldInfo>();
 
 			for (Object o : fields) {
 				@SuppressWarnings("unchecked")
 				Map<String, Object> m = (Map<String, Object>) o;
 				FieldInfo f = new FieldInfo();
 				f.setType((String) m.get("type"));
 				f.setName((String) m.get("name"));
 				f.setLength((Integer) m.get("length"));
 				fieldDefs.add(f);
 			}
 		}
 
 		TableInfo t = new TableInfo(tableName, metadata);
 		t.getSchema().setFieldDefinitions(fieldDefs);
 		return t;
 	}
 
 	/***
 	 * 테이블 스키마를 설정합니다. 필드 정의는 실제 데이터 적재 및 쿼리에 제약을 가하지 않으며, UI 지원용으로만 사용됩니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름
 	 * @param fields
 	 *            필드 정의 목록
 	 * @since 0.9.0 and logdb 2.0.3
 	 */
 	public void setTableFields(String tableName, List<FieldInfo> fields) throws IOException {
 		if (tableName == null)
 			throw new IllegalArgumentException("table name cannot be null");
 
 		List<Object> l = null;
 
 		if (fields != null) {
 			l = new ArrayList<Object>();
 
 			for (FieldInfo f : fields) {
 				Map<String, Object> m = new HashMap<String, Object>();
 				m.put("name", f.getName());
 				m.put("type", f.getType());
 				m.put("length", f.getLength());
 				l.add(m);
 			}
 		}
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("fields", l);
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.setTableFields", params);
 	}
 
 	/**
 	 * 테이블 메타데이터를 설정합니다. 관리자 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 * @param config
 	 *            키/값 쌍으로 설정할 테이블 메타데이터 목록을 지정합니다. (NULL 허용 안 함)
 	 * @throws IOException
 	 */
 	public void setTableMetadata(String tableName, Map<String, String> config) throws IOException {
 		if (tableName == null)
 			throw new IllegalArgumentException("table name cannot be null");
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("metadata", config);
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.setTableMetadata", params);
 	}
 
 	/**
 	 * 테이블 메타데이터를 삭제합니다. 관리자 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 * @param keySet
 	 *            삭제할 메타데이터 키 문자열 목록을 지정합니다. (NULL 허용 안 함)
 	 */
 	public void unsetTableMetadata(String tableName, Set<String> keySet) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("keys", keySet);
 
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.unsetTableMetadata", params);
 	}
 
 	/**
 	 * 새 테이블을 생성합니다. 관리자 권한이 없거나 테이블 이름이 중복되는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 */
 	public void createTable(String tableName) throws IOException {
 		createTable(tableName, null);
 	}
 
 	/**
 	 * 새 테이블을 생성합니다. 관리자 권한이 없거나 테이블 이름이 중복되는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 * @param metadata
 	 *            테이블 초기 메타데이터 설정 (NULL 허용)
 	 */
 	public void createTable(String tableName, Map<String, String> metadata) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("metadata", metadata);
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.createTable", params);
 	}
 
 	/**
 	 * 테이블을 삭제합니다. 모든 원본 데이터와 인덱스 데이터가 삭제되고 되돌릴 수 없습니다. 관리자 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름 (NULL 허용 안 함)
 	 */
 	public void dropTable(String tableName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		rpc("org.araqne.logdb.msgbus.ManagementPlugin.dropTable", params);
 	}
 
 	/**
 	 * 로그 수집기 유형 목록을 조회합니다. 일반적으로 createLogger()를 할 때 사용자에게 사용 가능한 로그 수집기 목록을
 	 * 보여주기 위해서 호출합니다.
 	 * 
 	 * @return 로그 수집기 유형 개체를 리스트로 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<LoggerFactoryInfo> listLoggerFactories() throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("locale", locale.getLanguage());
 
 		Message resp = rpc("org.araqne.log.api.msgbus.LoggerPlugin.getLoggerFactories", params);
 
 		List<LoggerFactoryInfo> factories = new ArrayList<LoggerFactoryInfo>();
 		List<Object> l = (List<Object>) resp.get("factories");
 		for (Object o : l) {
 			parseLoggerFactoryInfo(factories, o);
 		}
 
 		return factories;
 	}
 
 	/**
 	 * 지정된 이름을 가진 로그 수집기 유형 정보를 반환합니다. 로그 수집기를 생성하는데 필요한 설정 명세 목록을 포함하여 반환합니다.
 	 * 해당 이름의 로그 수집기 유형이 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param factoryName
 	 *            로그 수집기 유형 이름 (NULL 허용 안 함)
 	 * @return 지정된 이름을 가진 로그 수집기 유형 개체
 	 */
 	@SuppressWarnings("unchecked")
 	public LoggerFactoryInfo getLoggerFactoryInfo(String factoryName) throws IOException {
 		List<LoggerFactoryInfo> factories = listLoggerFactories();
 		LoggerFactoryInfo found = null;
 
 		for (LoggerFactoryInfo f : factories) {
 			if (f.getNamespace().equals("local") && f.getName().equals(factoryName)) {
 				found = f;
 				break;
 			}
 		}
 
 		if (found == null)
 			throw new IllegalStateException("logger factory not found: " + factoryName);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("factory", factoryName);
 		params.put("locale", locale.getLanguage());
 		Message resp2 = rpc("org.araqne.log.api.msgbus.LoggerPlugin.getFactoryOptions", params);
 		List<ConfigSpec> configSpecs = parseConfigList((List<Object>) resp2.get("options"));
 		found.setConfigSpecs(configSpecs);
 		return found;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void parseLoggerFactoryInfo(List<LoggerFactoryInfo> factories, Object o) {
 		Map<String, Object> m = (Map<String, Object>) o;
 
 		LoggerFactoryInfo f = new LoggerFactoryInfo();
 		f.setFullName((String) m.get("full_name"));
 		f.setDisplayName((String) m.get("display_name"));
 		f.setNamespace((String) m.get("namespace"));
 		f.setName((String) m.get("name"));
 		f.setDescription((String) m.get("description"));
 
 		factories.add(f);
 	}
 
 	/**
 	 * 파서 유형 목록을 조회합니다. 일반적으로 createParser() 매개변수 설정에 필요한 파서 유형 목록을 사용자에게 표시하기
 	 * 위해 호출합니다.
 	 * 
 	 * @return 파서 유형 목록 개체의 리스트를 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<ParserFactoryInfo> listParserFactories() throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("locale", locale.getLanguage());
 
 		Message resp = rpc("org.araqne.log.api.msgbus.LoggerPlugin.getParserFactories", params);
 		List<Object> l = (List<Object>) resp.get("factories");
 
 		List<ParserFactoryInfo> parsers = new ArrayList<ParserFactoryInfo>();
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 
 			ParserFactoryInfo f = new ParserFactoryInfo();
 			f.setName((String) m.get("name"));
 			f.setDisplayName((String) m.get("display_name"));
 			f.setDescription((String) m.get("description"));
 			f.setConfigSpecs(parseConfigList((List<Object>) m.get("options")));
 			parsers.add(f);
 		}
 
 		return parsers;
 	}
 
 	/**
 	 * 설정 명세 목록이 포함된 파서 유형 정보를 조회합니다. 지정된 이름의 파서 유형이 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            파서 유형 이름 (NULL 허용 안 함)
 	 * @return 지정된 이름의 파서 유형 정보 개체를 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public ParserFactoryInfo getParserFactoryInfo(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("factory_name", name);
 
 		Message resp = rpc("com.logpresso.core.msgbus.ParserPlugin.getParserFactoryInfo", params);
 		Map<String, Object> m = (Map<String, Object>) resp.get("factory");
 
 		ParserFactoryInfo f = new ParserFactoryInfo();
 		f.setName((String) m.get("name"));
 		f.setDisplayName((String) m.get("display_name"));
 		f.setDescription((String) m.get("description"));
 		f.setConfigSpecs(parseConfigList((List<Object>) m.get("options")));
 		return f;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<ConfigSpec> parseConfigList(List<Object> l) {
 		List<ConfigSpec> specs = new ArrayList<ConfigSpec>();
 
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			ConfigSpec spec = new ConfigSpec();
 			spec.setName((String) m.get("name"));
 			spec.setDescription((String) m.get("description"));
 			spec.setDisplayName((String) m.get("display_name"));
 			spec.setType((String) m.get("type"));
 			spec.setRequired((Boolean) m.get("required"));
 			spec.setDefaultValue((String) m.get("default_value"));
 			specs.add(spec);
 		}
 
 		return specs;
 	}
 
 	/**
 	 * 파서 목록을 조회합니다.
 	 * 
 	 * @deprecated Use listParsers() instead
 	 * @return 파서 설정 개체의 리스트가 반환됩니다.
 	 */
 	@Deprecated
 	public List<ParserInfo> getParsers() throws IOException {
 		return listParsers();
 	}
 
 	/**
 	 * 파서 목록을 조회합니다.
 	 * 
 	 * @return 파서 설정 개체의 리스트가 반환됩니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<ParserInfo> listParsers() throws IOException {
 		Message resp = rpc("com.logpresso.core.msgbus.ParserPlugin.getParsers");
 		List<Object> l = (List<Object>) resp.get("parsers");
 
 		List<ParserInfo> parsers = new ArrayList<ParserInfo>();
 		for (Object o : l) {
 			parsers.add(parseParserInfo((Map<String, Object>) o));
 		}
 
 		return parsers;
 	}
 
 	/**
 	 * 지정된 이름의 파서 정보를 조회합니다.
 	 * 
 	 * @param name
 	 *            파서 이름 (NULL 허용 안 함)
 	 * @return 지정된 이름의 파서 정보 개체를 반환합니다. 지정된 이름의 파서가 존재하지 않는 경우 NULL이 반환됩니다.
 	 */
 	public ParserInfo getParser(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 		Message resp = rpc("com.logpresso.core.msgbus.ParserPlugin.getParser", params);
 
 		@SuppressWarnings("unchecked")
 		Map<String, Object> m = (Map<String, Object>) resp.get("parser");
 		return parseParserInfo(m);
 	}
 
 	@SuppressWarnings("unchecked")
 	private ParserInfo parseParserInfo(Map<String, Object> m) {
 		ParserInfo p = new ParserInfo();
 		p.setName((String) m.get("name"));
 		p.setFactoryName((String) m.get("factory_name"));
 		p.setConfigs((Map<String, String>) m.get("configs"));
 
 		// since 0.9.0 and logpresso-core 0.8.0
 		if (m.get("fields") != null) {
 			List<FieldInfo> l = new ArrayList<FieldInfo>();
 			for (Object o : (List<Object>) m.get("fields"))
 				l.add(PrimitiveConverter.parse(FieldInfo.class, o));
 
 			p.setFieldDefinitions(l);
 		}
 
 		return p;
 	}
 
 	/**
 	 * 새 파서를 생성합니다. 파서 유형이 존재하지 않는 경우, 필수적인 파서 설정이 누락된 경우, 파서 이름이 중복된 경우 예외가
 	 * 발생합니다.
 	 * 
 	 * @param parser
 	 *            새 파서 설정 (NULL 허용 안 함)
 	 */
 	public void createParser(ParserInfo parser) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", parser.getName());
 		params.put("factory_name", parser.getFactoryName());
 		params.put("configs", parser.getConfigs());
 
 		rpc("com.logpresso.core.msgbus.ParserPlugin.createParser", params);
 	}
 
 	/**
 	 * 파서를 삭제합니다. 지정된 이름의 파서가 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            삭제할 파서 이름 (NULL 허용 안 함)
 	 */
 	public void removeParser(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 		rpc("com.logpresso.core.msgbus.ParserPlugin.removeParser", params);
 	}
 
 	/**
 	 * 특정한 파서의 동작을 시험합니다. 지정된 파서가 존재하지 않거나 파싱이 실패하는 경우 예외가 발생합니다.
 	 * 
 	 * @param parserName
 	 *            파서 이름 (NULL 허용 안 함)
 	 * @param data
 	 *            원본 데이터 키/값 쌍 (NULL 허용 안 함), 일반적으로 텍스트 파일에서 수집되는 데이터 혹은 시스로그의
 	 *            경우 line 키/값 쌍으로 표현됩니다.
 	 * @return 파싱된 키/값 쌍의 리스트가 반환됩니다. 대부분(V1 파서)은 데이터 원본과 파싱된 결과가 1:1로 대응되지만,
 	 *         넷플로우처럼 다수의 레코드가 단일 패킷에 팩킹된 경우 (V2 파서) 다수의 파싱 결과를 얻을 수 있습니다.
 	 * @since 0.8.1
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Map<String, Object>> testParser(String parserName, Map<String, Object> data) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", parserName);
 		params.put("data", data);
 
 		Message resp = rpc("com.logpresso.core.msgbus.ParserPlugin.testParser", params);
 		return (List<Map<String, Object>>) resp.get("rows");
 	}
 
 	/**
 	 * 트랜스포머 유형 목록을 조회합니다. 일반적으로 createTransformer()를 호출하기 전에 사용자에게 가능한 트랜스포머 유형
 	 * 목록을 표시하기 위해 호출합니다.
 	 * 
 	 * @return 트랜스포머 유형 개체의 리스트가 반환됩니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<TransformerFactoryInfo> listTransformerFactories() throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("locale", locale.getLanguage());
 
 		Message resp = rpc("com.logpresso.core.msgbus.TransformerPlugin.listTransformerFactories", params);
 		List<Object> l = (List<Object>) resp.get("factories");
 
 		List<TransformerFactoryInfo> factories = new ArrayList<TransformerFactoryInfo>();
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 
 			TransformerFactoryInfo f = new TransformerFactoryInfo();
 			f.setName((String) m.get("name"));
 			f.setDisplayName((String) m.get("display_name"));
 			f.setDescription((String) m.get("description"));
 			f.setConfigSpecs(parseConfigList((List<Object>) m.get("options")));
 			factories.add(f);
 		}
 
 		return factories;
 	}
 
 	/**
 	 * 지정된 이름의 트랜스포머 유형 정보를 조회합니다. 트랜스포머 설정 명세 목록을 조회하는데 사용합니다. 지정된 이름의 트랜스포머 유형
 	 * 이름이 존재하지 않으면 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            트랜스포머 유형 이름 (NULL 허용 안 함)
 	 * @return 지정된 이름의 트랜스포머 유형 정보 개체
 	 */
 	@SuppressWarnings("unchecked")
 	public TransformerFactoryInfo getTransformerFactoryInfo(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("factory_name", name);
 		params.put("locale", locale.getLanguage());
 
 		Message resp = rpc("com.logpresso.core.msgbus.TransformerPlugin.getTransformerFactoryInfo", params);
 		Map<String, Object> m = (Map<String, Object>) resp.get("factory");
 
 		TransformerFactoryInfo f = new TransformerFactoryInfo();
 		f.setName((String) m.get("name"));
 		f.setDisplayName((String) m.get("display_name"));
 		f.setDescription((String) m.get("description"));
 		f.setConfigSpecs(parseConfigList((List<Object>) m.get("options")));
 		return f;
 	}
 
 	/**
 	 * 트랜스포머 목록을 조회합니다.
 	 * 
 	 * @return 트랜스포머 개체의 리스트를 반환합니다.
 	 * @deprecated Use listTransformers() instead.
 	 */
 	@Deprecated
 	public List<TransformerInfo> getTransformers() throws IOException {
 		return listTransformers();
 	}
 
 	/**
 	 * 트랜스포머 목록을 조회합니다.
 	 * 
 	 * @return 트랜스포머 개체의 리스트를 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<TransformerInfo> listTransformers() throws IOException {
 		Message resp = rpc("com.logpresso.core.msgbus.TransformerPlugin.getTransformers");
 		List<Object> l = (List<Object>) resp.get("transformers");
 
 		List<TransformerInfo> transformers = new ArrayList<TransformerInfo>();
 		for (Object o : l) {
 			transformers.add(parseTransformerInfo((Map<String, Object>) o));
 		}
 
 		return transformers;
 	}
 
 	/**
 	 * 지정된 이름의 트랜스포머 설정을 조회합니다. 지정된 이름의 트랜스포머가 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            트랜스포머 이름 (NULL 허용 안 함)
 	 * @return 트랜스포머 설정 정보 개체
 	 */
 	public TransformerInfo getTransformer(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 		Message resp = rpc("com.logpresso.core.msgbus.TransformerPlugin.getTransformer", params);
 
 		@SuppressWarnings("unchecked")
 		Map<String, Object> m = (Map<String, Object>) resp.get("transformer");
 		return parseTransformerInfo(m);
 	}
 
 	@SuppressWarnings("unchecked")
 	private TransformerInfo parseTransformerInfo(Map<String, Object> m) {
 		TransformerInfo p = new TransformerInfo();
 		p.setName((String) m.get("name"));
 		p.setFactoryName((String) m.get("factory_name"));
 		p.setConfigs((Map<String, String>) m.get("configs"));
 		return p;
 	}
 
 	/**
 	 * 새 트랜스포머 설정을 추가합니다. 트랜스포머 이름이 중복되거나, 트랜스포머 유형이 존재하지 않거나, 필수적인 설정이 누락된 경우
 	 * 예외가 발생합니다.
 	 * 
 	 * @param transformer
 	 *            새 트랜스포머 설정 (NULL 허용 안 함)
 	 */
 	public void createTransformer(TransformerInfo transformer) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", transformer.getName());
 		params.put("factory_name", transformer.getFactoryName());
 		params.put("configs", transformer.getConfigs());
 
 		rpc("com.logpresso.core.msgbus.TransformerPlugin.createTransformer", params);
 	}
 
 	/**
 	 * 트랜스포머를 삭제합니다. 지정된 이름의 트랜스포머가 존재하지 않는 경우 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            삭제할 트랜스포머 이름 (NULL 허용 안 함)
 	 */
 	public void removeTransformer(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 		rpc("com.logpresso.core.msgbus.TransformerPlugin.removeTransformer", params);
 	}
 
 	/**
 	 * 로그 수집기 목록을 반환합니다.
 	 * 
 	 * @return 로그 수집기 정보 개체 리스트를 반환합니다.
 	 */
 	public List<LoggerInfo> listLoggers() throws IOException {
 		return listLoggers(null);
 	}
 
 	/**
 	 * 로그 수집기 목록을 반환합니다.
 	 * 
 	 * @param loggerNames
 	 *            정보를 조회할 로거 이름 목록
 	 * @return 로그 수집기 정보 개체 리스트를 반환합니다.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<LoggerInfo> listLoggers(List<String> loggerNames) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger_names", loggerNames);
 
 		Message resp = rpc("org.araqne.log.api.msgbus.LoggerPlugin.getLoggers", params);
 		List<Object> l = (List<Object>) resp.get("loggers");
 
 		List<LoggerInfo> loggers = new ArrayList<LoggerInfo>();
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			LoggerInfo lo = decodeLoggerInfo(m);
 			loggers.add(lo);
 		}
 
 		return loggers;
 	}
 
 	/**
 	 * 특정한 로그 수집기 정보를 조회합니다. 이 때 로그 수집기의 수집상태 정보는 포함되지 않습니다. Retrieve specific
 	 * logger information with config using RPC call. States will not returned
 	 * because logger states' size can be very large.
 	 * 
 	 * @param loggerName
 	 *            새 로그 수집기 생성에 필요한 설정 (NULL 허용 안 함)
 	 * @since 0.8.6
 	 */
 	public LoggerInfo getLogger(String loggerName) throws IOException {
 		return getLogger(loggerName, false);
 	}
 
 	/**
 	 * 특정한 로그 수집기 정보를 조회합니다.
 	 * 
 	 * @param loggerName
 	 *            조회 대상 로그 수집기 이름
 	 * @param includeStates
 	 *            로거 수집상태 정보 포함 여부
 	 * @since 0.8.6
 	 */
 	@SuppressWarnings("unchecked")
 	public LoggerInfo getLogger(String loggerName, boolean includeStates) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger_name", loggerName);
 		params.put("include_configs", true);
 		params.put("include_states", includeStates);
 
 		Message resp = rpc("org.araqne.log.api.msgbus.LoggerPlugin.getLogger", params);
 		Map<String, Object> m = (Map<String, Object>) resp.get("logger");
 		if (m == null)
 			return null;
 
 		return decodeLoggerInfo(m);
 	}
 
 	@SuppressWarnings("unchecked")
 	private LoggerInfo decodeLoggerInfo(Map<String, Object> m) {
 		LoggerInfo lo = new LoggerInfo();
 		lo.setNamespace((String) m.get("namespace"));
 		lo.setName((String) m.get("name"));
 		lo.setFactoryName((String) m.get("factory_full_name"));
 		lo.setDescription((String) m.get("description"));
 		lo.setPassive((Boolean) m.get("is_passive"));
 		lo.setInterval((Integer) m.get("interval"));
 		lo.setStatus((String) m.get("status"));
 		lo.setLastStartAt(parseDate((String) m.get("last_start")));
 		lo.setLastRunAt(parseDate((String) m.get("last_run")));
 		lo.setLastLogAt(parseDate((String) m.get("last_log")));
 		lo.setLogCount(Long.valueOf(m.get("log_count").toString()));
 
 		Object dropCount = m.get("drop_count");
 		if (dropCount != null)
 			lo.setDropCount(Long.valueOf(dropCount.toString()));
 
 		Object updateCount = m.get("update_count");
 		if (updateCount != null)
 			lo.setUpdateCount(Long.valueOf(updateCount.toString()));
 
 		lo.setConfigs((Map<String, String>) m.get("configs"));
 		lo.setStates((Map<String, Object>) m.get("states"));
 		return lo;
 	}
 
 	private Date parseDate(String s) {
 		if (s == null)
 			return null;
 
 		try {
 			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 			return f.parse(s);
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * 새 로그 수집기를 생성합니다. 로그 수집기 이름이 중복되거나, 로그 수집기 유형의 설정 명세에서 필수로 표시된 설정이 입력되지 않은
 	 * 경우 예외가 발생합니다.
 	 * 
 	 * @param logger
 	 *            새 로그 수집기 생성에 필요한 설정 (NULL 허용 안 함)
 	 * @throws IOException
 	 */
 	public void createLogger(LoggerInfo logger) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("factory", logger.getFactoryName());
 		params.put("namespace", logger.getNamespace());
 		params.put("name", logger.getName());
 		params.put("description", logger.getDescription());
 		params.put("options", logger.getConfigs());
 
 		rpc("org.araqne.log.api.msgbus.LoggerPlugin.createLogger", params);
 	}
 
 	/**
 	 * 로그 수집기를 삭제합니다. 로그 수집기가 존재하지 않거나 아직 실행 중인 경우 예외가 발생합니다.
 	 * 
 	 * @param fullName
 	 *            이름공간\이름 형식의 로그 수집기 이름 (NULL 허용 안 함)
 	 */
 	public void removeLogger(String fullName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", fullName);
 		rpc("org.araqne.log.api.msgbus.LoggerPlugin.removeLogger", params);
 	}
 
 	/**
 	 * 로그 수집기를 시작합니다. 패시브 속성의 로그 수집기인 경우 수집 주기를 무시합니다. 로거가 이미 시작된 경우 예외가 발생합니다.
 	 * 
 	 * @param fullName
 	 *            이름공간\이름 형식의 로그 수집기 이름 (NULL 허용 안 함)
 	 * @param interval
 	 *            밀리세컨드 단위의 로그 수집 주기, 패시브 로그 수집기인 경우 무시됨.
 	 */
 	public void startLogger(String fullName, int interval) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", fullName);
 		params.put("interval", interval);
 		rpc("org.araqne.log.api.msgbus.LoggerPlugin.startLogger", params);
 	}
 
 	/**
 	 * 로그 수집기를 정지합니다. 패시브 속성의 로그 수집기인 경우 최대 대기 시간을 무시합니다.
 	 * 
 	 * @param fullName
 	 *            이름공간\이름 형식의 로그 수집기 이름 (NULL 허용 안 함)
 	 * @param waitTime
 	 *            액티브 로그 수집기의 정지를 기다리는 밀리세컨드 단위의 최대 대기 시간
 	 */
 	public void stopLogger(String fullName, int waitTime) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", fullName);
 		params.put("wait_time", waitTime);
 		rpc("org.araqne.log.api.msgbus.LoggerPlugin.stopLogger", params);
 	}
 
 	/**
 	 * 기존의 JDBC 프로파일 설정 목록을 조회합니다. DB 관리자 권한이 없는 경우 예외가 발생합니다. 프로파일 정보 조회 시 암호
 	 * 문자열은 반환하지 않습니다.
 	 * 
 	 * @return JDBC 프로파일 설정 목록
 	 */
 	@SuppressWarnings("unchecked")
 	public List<JdbcProfileInfo> listJdbcProfiles() throws IOException {
 		List<JdbcProfileInfo> l = new ArrayList<JdbcProfileInfo>();
 
 		Message resp = rpc("org.logpresso.jdbc.JdbcProfilePlugin.getProfiles");
 
 		List<Object> profiles = (List<Object>) resp.get("profiles");
 
 		for (Object o : profiles) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			JdbcProfileInfo info = new JdbcProfileInfo();
 			info.setName((String) m.get("name"));
 			info.setConnectionString((String) m.get("connection_string"));
 			info.setReadOnly((Boolean) m.get("readonly"));
 			info.setUser((String) m.get("user"));
 			l.add(info);
 		}
 
 		return l;
 	}
 
 	/**
 	 * 새 JDBC 프로파일을 생성합니다. 이미 동일한 이름의 JDBC 프로파일이 존재하거나, DB 관리자 권한이 없는 경우 예외가
 	 * 발생합니다.
 	 * 
 	 * @param profile
 	 *            새 JDBC 프로파일 설정 (NULL 허용 안 함)
 	 */
 	public void createJdbcProfile(JdbcProfileInfo profile) throws IOException {
 		checkNotNull("profile", profile);
 		checkNotNull("profile.name", profile.getName());
 		checkNotNull("profile.connectionString", profile.getConnectionString());
 		checkNotNull("profile.user", profile.getUser());
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", profile.getName());
 		params.put("connection_string", profile.getConnectionString());
 		params.put("readonly", profile.isReadOnly());
 		params.put("user", profile.getUser());
 		params.put("password", profile.getPassword());
 
 		rpc("org.logpresso.jdbc.JdbcProfilePlugin.createProfile", params);
 	}
 
 	/**
 	 * 기존의 JDBC 프로파일을 삭제합니다. 지정된 이름의 JDBC 프로파일이 존재하지 않거나, DB 관리자 권한이 없는 경우 예외가
 	 * 발생합니다.
 	 * 
 	 * @param name
 	 *            삭제할 대상 JDBC 프로파일 이름
 	 */
 	public void removeJdbcProfile(String name) throws IOException {
 		checkNotNull("name", name);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 
 		rpc("org.logpresso.jdbc.JdbcProfilePlugin.removeProfile", params);
 	}
 
 	/**
 	 * 쿼리 실행 결과를 커서 개체로 반환합니다. 쿼리 실행이 완전히 끝날 때까지 스레드가 차단(blocking)되며, 커서를 모두
 	 * 순회하거나 닫기 전까지 쿼리가 유지됩니다. 권한이 없거나, 쿼리 문법이 틀린 경우 예외가 발생합니다. 스레드를 차단하지 않고 쿼리
 	 * 실행 상태를 폴링하면서 부분적인 쿼리 결과를 최대한 빨리 가져오고 싶은 경우(pipelining)에는 아래에 설명되는
 	 * createQuery(), startQuery(), stopQuery(), removeQuery(), getResult() 메소드
 	 * 조합을 사용하십시오.
 	 * 
 	 * @param queryString
 	 *            쿼리 문자열 (NULL 허용 안 함)
 	 * @return 쿼리 결과를 조회할 수 있는 커서가 반환됩니다.
 	 */
 	public LogCursor query(String queryString) throws IOException {
 		int id = createQuery(queryString);
 		startQuery(id);
 		LogQuery q = queries.get(id);
 		q.waitUntil(null);
 		if (q.getStatus().equals("Cancelled"))
 			throw new IllegalStateException("query cancelled, id [" + q.getId() + "] query string [" + queryString + "]");
 
 		long total = q.getLoadedCount();
 
 		return new LogCursorImpl(id, 0L, total, true, fetchSize);
 	}
 
 	private class LogCursorImpl implements LogCursor {
 
 		private int id;
 		private long offset;
 		private long limit;
 		private boolean removeOnClose;
 
 		private long p;
 		private Map<String, Object> cached;
 		private Long currentCacheOffset;
 		private Long nextCacheOffset;
 		private int fetchUnit;
 		private Map<String, Object> prefetch;
 
 		public LogCursorImpl(int id, long offset, long limit, boolean removeOnClose, int fetchUnit) {
 			this.id = id;
 			this.offset = offset;
 			this.limit = limit;
 			this.removeOnClose = removeOnClose;
 
 			this.p = offset;
 			this.nextCacheOffset = offset;
 			this.fetchUnit = fetchUnit;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean hasNext() {
 			if (prefetch != null)
 				return true;
 
 			if (p < offset || p >= offset + limit)
 				return false;
 
 			try {
 				if (cached == null || p >= currentCacheOffset + fetchUnit) {
 					cached = getResult(id, nextCacheOffset, fetchUnit);
 					currentCacheOffset = nextCacheOffset;
 					nextCacheOffset += fetchUnit;
 				}
 
 				int relative = (int) (p - currentCacheOffset);
 				List<Object> l = (List<Object>) cached.get("result");
 				if (relative >= l.size())
 					return false;
 
 				prefetch = (Map<String, Object>) l.get(relative);
 				p++;
 				return true;
 			} catch (IOException e) {
 				logger.error("araqne logdb client: cannot fetch log query result", e);
 				return false;
 			}
 		}
 
 		@Override
 		public Map<String, Object> next() {
 			if (!hasNext())
 				throw new NoSuchElementException("end of log cursor");
 
 			Map<String, Object> m = prefetch;
 			prefetch = null;
 			return m;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void close() throws IOException {
 			if (removeOnClose)
 				removeQuery(id);
 		}
 	}
 
 	/**
 	 * 스트림 쿼리 목록을 조회합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public List<StreamQueryStatus> listStreamQueries() throws IOException {
 		Message resp = rpc("com.logpresso.query.msgbus.StreamQueryPlugin.getStreamQueries");
 
 		@SuppressWarnings("unchecked")
 		List<Object> l = (List<Object>) resp.get("stream_queries");
 		List<StreamQueryStatus> statuses = new ArrayList<StreamQueryStatus>();
 
 		for (Object o : l) {
 			StreamQueryStatus status = parseStreamQueryStatus(o);
 			statuses.add(status);
 		}
 
 		return statuses;
 	}
 
 	/**
 	 * @since 0.9.5
 	 */
 	@SuppressWarnings("unchecked")
 	public StreamQueryStatus getStreamQuery(String name) throws IOException {
 		checkNotNull("name", name);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 
 		Message resp = rpc("com.logpresso.query.msgbus.StreamQueryPlugin.getStreamQuery", params);
 		Map<String, Object> o = (Map<String, Object>) resp.get("stream_query");
 		if (o == null)
 			return null;
 
 		return parseStreamQueryStatus(o);
 	}
 
 	@SuppressWarnings("unchecked")
 	private StreamQueryStatus parseStreamQueryStatus(Object o) {
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 		StreamQueryStatus status = new StreamQueryStatus();
 
 		Map<String, Object> m = (Map<String, Object>) o;
 		Map<String, Object> c = (Map<String, Object>) m.get("config");
 
 		StreamQueryInfo query = new StreamQueryInfo();
 		query.setName((String) c.get("name"));
 		query.setDescription((String) c.get("description"));
 		query.setInterval((Integer) c.get("interval"));
 		query.setQueryString((String) c.get("query"));
 		query.setOwner((String) c.get("owner"));
 		query.setSourceType((String) c.get("source_type"));
 		query.setEnabled((Boolean) c.get("is_enabled"));
 		query.setCreated(df.parse((String) c.get("created"), new ParsePosition(0)));
 		query.setModified(df.parse((String) c.get("modified"), new ParsePosition(0)));
 
 		status.setStreamQuery(query);
 		status.setInputCount(Long.parseLong(m.get("input_count").toString()));
 		status.setLastRefresh(df.parse((String) m.get("last_refresh"), new ParsePosition(0)));
 		status.setRunning((Boolean) m.get("is_running"));
 		return status;
 	}
 
 	/**
 	 * 스트림 쿼리를 생성합니다. 스트림 쿼리 이름이 중복되는 경우 예외가 발생합니다. logger, table, stream 이외의
 	 * 데이터 원본 타입이 지정된 경우 예외가 발생합니다. 새로고침 주기가 음수인 경우 예외가 발생합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public void createStreamQuery(StreamQueryInfo query) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", query.getName());
 		params.put("description", query.getDescription());
 		params.put("interval", query.getInterval());
 		params.put("source_type", query.getSourceType());
 		params.put("sources", query.getSources());
 		params.put("query", query.getQueryString());
 
 		rpc("com.logpresso.query.msgbus.StreamQueryPlugin.createStreamQuery", params);
 	}
 
 	/**
 	 * 지정된 이름의 스트림 쿼리를 삭제합니다. 지정된 스트림 쿼리가 존재하지 않거나, 소유자가 아닌 경우 예외가 발생합니다.
 	 * 
 	 * @param name
 	 *            스트림 쿼리 이름
 	 * @since 0.9.5
 	 */
 	public void removeStreamQuery(String name) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("name", name);
 
 		rpc("com.logpresso.query.msgbus.StreamQueryPlugin.removeStreamQuery", params);
 	}
 
 	/**
 	 * 예약된 쿼리 목록을 조회합니다. 자신이 설정한 예약된 쿼리 목록만 조회됩니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public List<ScheduledQueryInfo> listScheduledQueries() throws IOException {
 		Message resp = rpc("com.logpresso.core.msgbus.ScheduledQueryPlugin.getScheduledQueries");
 
 		@SuppressWarnings("unchecked")
 		List<Object> l = (List<Object>) resp.get("scheduled_queries");
 
 		List<ScheduledQueryInfo> queries = new ArrayList<ScheduledQueryInfo>();
 		for (Object o : l) {
 			queries.add(parseScheduledQuery(o));
 		}
 
 		return queries;
 	}
 
 	/**
 	 * 지정된 GUID를 가진 예약된 쿼리 설정을 조회합니다. 예약된 쿼리가 존재하지 않거나 조회 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public ScheduledQueryInfo getScheduledQuery(String guid) throws IOException {
 		checkNotNull("guid", guid);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("guid", guid);
 		Message resp = rpc("com.logpresso.core.msgbus.ScheduledQueryPlugin.getScheduledQuery", params);
 
 		return parseScheduledQuery(resp.get("scheduled_query"));
 	}
 
 	private ScheduledQueryInfo parseScheduledQuery(Object o) {
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 
 		@SuppressWarnings("unchecked")
 		Map<String, Object> m = (Map<String, Object>) o;
 
 		ScheduledQueryInfo query = new ScheduledQueryInfo();
 		query.setGuid((String) m.get("guid"));
 		query.setTitle((String) m.get("title"));
 		query.setCronSchedule((String) m.get("cron_schedule"));
 		query.setOwner((String) m.get("owner"));
 		query.setQueryString((String) m.get("query_string"));
 		query.setSaveResult((Boolean) m.get("use_save_result"));
 		query.setUseAlert((Boolean) m.get("use_alert"));
 		query.setAlertQuery((String) m.get("alert_query"));
 		query.setSuppressInterval((Integer) m.get("suppress_interval"));
 		query.setMailProfile((String) m.get("mail_profile"));
 		query.setMailFrom((String) m.get("mail_from"));
 		query.setMailTo((String) m.get("mail_to"));
 		query.setMailSubject((String) m.get("mail_subject"));
 		query.setEnabled((Boolean) m.get("is_enabled"));
 		query.setCreated(df.parse((String) m.get("created_at"), new ParsePosition(0)));
 
 		return query;
 	}
 
 	/**
 	 * 예약된 쿼리를 생성합니다. 예약된 쿼리의 GUID가 중복되는 경우 예외가 발생합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public void createScheduledQuery(ScheduledQueryInfo query) throws IOException {
 		Map<String, Object> params = buildScheduledQueryParams(query);
 		rpc("com.logpresso.core.msgbus.ScheduledQueryPlugin.createScheduledQuery", params);
 	}
 
 	/**
 	 * 예약된 쿼리를 수정합니다. 지정된 GUID의 예약된 쿼리가 존재하지 않으면 예외가 발생합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public void updateScheduledQuery(ScheduledQueryInfo query) throws IOException {
 		Map<String, Object> params = buildScheduledQueryParams(query);
 		rpc("com.logpresso.core.msgbus.ScheduledQueryPlugin.updateScheduledQuery", params);
 	}
 
 	private Map<String, Object> buildScheduledQueryParams(ScheduledQueryInfo query) {
 		Map<String, Object> params = new HashMap<String, Object>();
		params.put("guid", query.getGuid());
 		params.put("title", query.getTitle());
 		params.put("cron_schedule", query.getCronSchedule());
 		params.put("query", query.getQueryString());
 		params.put("save_result", query.isSaveResult());
 		params.put("use_alert", query.isUseAlert());
 		params.put("alert_query", query.getAlertQuery());
 		params.put("suppress_interval", query.getSuppressInterval());
 		params.put("mail_profile", query.getMailProfile());
 		params.put("mail_from", query.getMailFrom());
 		params.put("mail_to", query.getMailTo());
 		params.put("mail_subject", query.getMailSubject());
 		params.put("is_enabled", query.isEnabled());
 		return params;
 	}
 
 	/**
 	 * 예약된 쿼리를 삭제합니다. 지정된 GUID의 예약된 쿼리가 존재하지 않으면 예외가 발생합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public void removeScheduledQuery(String guid) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("guid", guid);
 		rpc("com.logpresso.core.msgbus.ScheduledQueryPlugin.removeScheduledQuery", params);
 	}
 
 	/**
 	 * 주어진 쿼리 문자열을 사용하여 쿼리를 생성합니다. 권한이 없거나 문법이 틀린 경우 예외가 발생합니다.
 	 * 
 	 * @param queryString
 	 *            쿼리 문자열 (NULL 허용 안 함)
 	 * @return 새로 생성된 쿼리 ID가 반환됩니다.
 	 */
 	public int createQuery(String queryString) throws IOException {
 		return createQuery(queryString, null);
 	}
 
 	/**
 	 * 주어진 쿼리 문자열을 사용하여 스트리밍 쿼리를 생성합니다. 권한이 없거나 문법이 틀린 경우 예외가 발생합니다.
 	 * 
 	 * @param queryString
 	 *            쿼리 문자열 (NULL 허용 안 함)
 	 * @param rs
 	 *            쿼리 결과 스트리밍에 사용할 콜백 인스턴스, NULL인 경우 스트리밍 모드로 전환되지 않습니다.
 	 * @return 새로 생성된 쿼리 ID가 반환됩니다.
 	 * @since 0.9.1
 	 */
 	public int createQuery(String queryString, StreamingResultSet rs) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("query", queryString);
 
 		Message resp = rpc("org.araqne.logdb.msgbus.LogQueryPlugin.createQuery", params);
 		int id = resp.getInt("id");
 		session.registerTrap("logdb-query-" + id);
 		session.registerTrap("logdb-query-timeline-" + id);
 
 		if (rs != null) {
 			streamCallbacks.put(id, rs);
 			session.registerTrap("logdb-query-result-" + id);
 		}
 
 		queries.putIfAbsent(id, new LogQuery(this, id, queryString));
 		return id;
 	}
 
 	/**
 	 * 지정된 쿼리를 시작시킵니다. 주어진 ID에 대응하는 쿼리가 없거나 액세스 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param id
 	 *            쿼리 ID
 	 */
 	public void startQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		params.put("streaming", streamCallbacks.containsKey(id));
 
 		rpc("org.araqne.logdb.msgbus.LogQueryPlugin.startQuery", params);
 	}
 
 	/**
 	 * 지정된 쿼리를 정지(취소)시킵니다. 주어진 ID에 대응하는 쿼리가 없거나 액세스 권한이 없는 경우 예외가 발생합니다. 정지되기 직전
 	 * 시점까지의 쿼리 결과는 removeQuery()를 호출하기 전까지 getResult()를 사용하여 조회할 수 있습니다.
 	 * 
 	 * @param id
 	 *            쿼리 ID
 	 */
 	public void stopQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 
 		rpc("org.araqne.logdb.msgbus.LogQueryPlugin.stopQuery", params);
 	}
 
 	/**
 	 * 지정된 쿼리를 삭제합니다. 서버의 임시 쿼리 결과 파일이 삭제됩니다. 이후에는 getResult()를 사용하여 쿼리 결과를 조회할
 	 * 수 없습니다. 주어진 ID에 대응하는 쿼리가 없거나 액세스 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param id
 	 *            쿼리 ID
 	 */
 	public void removeQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		StreamingResultSet rs = streamCallbacks.remove(id);
 		if (rs != null)
 			session.unregisterTrap("logdb-query-result-" + id);
 
 		session.unregisterTrap("logdb-query-" + id);
 		session.unregisterTrap("logdb-query-timeline-" + id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		rpc("org.araqne.logdb.msgbus.LogQueryPlugin.removeQuery", params);
 
 		queries.remove(id);
 	}
 
 	public void addFailureListener(FailureListener listener) {
 		failureListeners.add(listener);
 	}
 
 	public void removeFailureListener(FailureListener listener) {
 		failureListeners.remove(listener);
 	}
 
 	/**
 	 * 지정된 테이블에 행을 입력합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름
 	 * @param rows
 	 *            행 목록
 	 * @since 0.9.5
 	 */
 	public void insert(String tableName, List<Row> rows) {
 
 		for (Row row : rows) {
 			if (row.get("_time") == null || !(row.get("_time") instanceof Date))
 				row.put("_time", new Date());
 		}
 
 		// buffering
 		synchronized (flushBuffers) {
 			flushBuffers.put(tableName, rows);
 			counter += rows.size();
 		}
 		// timer thread
 		if (timer == null) {
 			timer = new Timer("Insert Flush Timer");
 			timer.schedule(new FlushTask(), indexFlushInterval, indexFlushInterval);
 		}
 		// count over -> flush
 		if (counter >= insertBatchSize) {
 			flush();
 		}
 
 	}
 
 	/**
 	 * 지정된 테이블에 행을 입력합니다.
 	 * 
 	 * @param tableName
 	 *            테이블 이름
 	 * @param row
 	 *            입력할 행
 	 * @since 0.9.5
 	 */
 	public void insert(String tableName, Row row) {
 		if (row.get("_time") == null || !(row.get("_time") instanceof Date))
 			row.put("_time", new Date());
 
 		// buffering
 		List<Row> rows = new ArrayList<Row>();
 		synchronized (flushBuffers) {
 			if (flushBuffers.containsKey(tableName))
 				rows = flushBuffers.get(tableName);
 
 			rows.add(row);
 			flushBuffers.put(tableName, rows);
 			counter++;
 		}
 
 		// timer thread
 		if (timer == null) {
 			timer = new Timer("Insert Flush Timer");
 			timer.schedule(new FlushTask(), indexFlushInterval, indexFlushInterval);
 		}
 		// count over -> flush
 		if (counter >= insertBatchSize) {
 			flush();
 		}
 	}
 
 	private class FlushTask extends TimerTask {
 		@Override
 		public void run() {
 			if (counter > 0)
 				flush();
 		}
 	}
 
 	/**
 	 * 현재 대기 중인 쓰기 버퍼를 비우고 RPC 통신을 통해 로그프레소 테이블에 기록합니다.
 	 * 
 	 * @since 0.9.5
 	 */
 	public void flush() {
 		if (counter == 0)
 			return;
 
 		Map<String, List<Row>> binsMap = null;
 		synchronized (flushBuffers) {
 			binsMap = new HashMap<String, List<Row>>(flushBuffers);
 			flushBuffers.clear();
 			counter = 0;
 		}
 
 		for (Map.Entry<String, List<Row>> entry : binsMap.entrySet()) {
 			String tableName = entry.getKey();
 			List<Row> rows = entry.getValue();
 			try {
 				List<Object> l = new ArrayList<Object>(rows.size());
 				for (Row row : rows)
 					l.add(row.map());
 
 				List<Map<String, Object>> bins = streamingEncoder.encode(l, false);
 				Map<String, Object> params = new HashMap<String, Object>();
 				params.put("table", entry.getKey());
 				params.put("bins", bins);
 				rpc("org.araqne.logdb.msgbus.LogQueryPlugin.insertBatch", params);
 			} catch (Throwable t) {
 				logger.debug("araqne logdb client: cannot insert data", t);
 
 				for (FailureListener c : failureListeners) {
 					try {
 						c.onInsertFailure(tableName, rows, t);
 					} catch (Throwable t2) {
 						logger.debug("araqne logdb client: insert failure callback should not throw any exception", t2);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * 특정 쿼리에 대해서 주어진 쿼리 결과 갯수가 조회 가능할 때까지 현재 스레드를 대기(blocking) 합니다. 주어진 쿼리 결과
 	 * 갯수를 채우지 못하더라도 쿼리가 완료 혹은 취소되면 스레드 대기 상태가 풀립니다. 이 메소드를 이용하면 매번 getQuery()를
 	 * 사용하여 서버에 폴링하지 않더라도 원하는 시점까지 대기할 수 있으며 서버 부하도 감소합니다.
 	 * 
 	 * @param id
 	 *            쿼리 ID
 	 * @param count
 	 *            쿼리 결과 행 갯수, null을 넘기는 경우 쿼리 완료 혹은 취소 시까지 대기합니다.
 	 */
 	public void waitUntil(int id, Long count) {
 		verifyQueryId(id);
 		queries.get(id).waitUntil(count);
 	}
 
 	/**
 	 * 쿼리 결과를 조회합니다. 주어진 offset 갯수만큼 건너뛰고, 최대 limit 갯수만큼 쿼리 결과를 조회합니다. 쿼리가 존재하지
 	 * 않거나 액세스 권한이 없는 경우 예외가 발생합니다.
 	 * 
 	 * @param id
 	 *            쿼리 ID
 	 * @param offset
 	 *            건너뛸 결과 행 갯수
 	 * @param limit
 	 *            가져올 최대 행 갯수. 너무 큰 값을 넘기면 서버나 클라이언트에서 메모리 고갈이 발생할 수 있습니다. 일반적으로
 	 *            10000 내외의 값을 사용하여 페이징 조회합니다.
 	 * @return Map 타입으로 아래와 같은 항목들을 반환합니다. result: Map 타입의 결과 행의 List, count: 전체
 	 *         쿼리 결과 행 갯수, 쿼리가 실행 중인 경우 getResult() 호출 시점까지의 쿼리 결과 행 갯수를 반환하며 쿼리
 	 *         완료 시까지 계속 증가할 수 있습니다. fields: 쿼리 문자열에 fields 쿼리 커맨드를 사용한 경우, 출력
 	 *         필드 순서를 정렬하는데 사용할 수 있도록 필드 이름 목록을 반환합니다.
 	 */
 	public Map<String, Object> getResult(int id, long offset, int limit) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		params.put("offset", offset);
 		params.put("limit", limit);
 		params.put("binary_encode", true);
 
 		Message resp = rpc("org.araqne.logdb.msgbus.LogQueryPlugin.getResult", params);
 		if (resp.getParameters().size() == 0)
 			throw new MessageException("query-not-found", "", resp.getParameters());
 
 		// support backward compatibility
 		if (!resp.getParameters().containsKey("uncompressed_size"))
 			return resp.getParameters();
 
 		// decompress and decode
 		int uncompressedSize = (Integer) resp.getParameters().get("uncompressed_size");
 		String binary = (String) resp.getParameters().get("binary");
 		return decodeBinary(binary, uncompressedSize);
 	}
 
 	private Map<String, Object> decodeBinary(String binary, int uncompressedSize) {
 		byte[] b = Base64.decode(binary);
 		byte[] uncompressed = new byte[uncompressedSize];
 		uncompress(uncompressed, b);
 
 		Map<String, Object> m = EncodingRule.decodeMap(ByteBuffer.wrap(uncompressed));
 
 		Object[] resultArray = (Object[]) m.get("result");
 		m.put("result", Arrays.asList(resultArray));
 
 		return m;
 	}
 
 	private void uncompress(byte[] output, byte[] b) {
 		Inflater inflater = new Inflater();
 		inflater.setInput(b, 0, b.length);
 		try {
 			inflater.inflate(output);
 			inflater.reset();
 		} catch (DataFormatException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			inflater.end();
 		}
 	}
 
 	private void verifyQueryId(int id) {
 		if (!queries.containsKey(id))
 			throw new MessageException("query-not-found", "query [" + id + "] does not exist", null);
 	}
 
 	/**
 	 * 접속을 끊고 할당된 자원을 정리합니다.
 	 */
 	public void close() throws IOException {
 
 		if (counter > 0)
 			flush();
 
 		if (session != null)
 			session.close();
 
 		if (streamingDecoder != null) {
 			streamingDecoder.close();
 			streamingDecoder = null;
 		}
 
 		if (timer != null)
 			timer.cancel();
 
 		if (streamingEncoder != null) {
 			streamingEncoder.close();
 			streamingEncoder = null;
 		}
 	}
 
 	@Override
 	public void onTrap(Message msg) {
 		String method = msg.getMethod();
 		long stamp = 0;
 		if (msg.containsKey("stamp"))
 			stamp = Long.parseLong(msg.get("stamp").toString());
 
 		if (method.startsWith("logdb-query-timeline-")) {
 			int id = msg.getInt("id");
 			LogQuery q = queries.get(id);
 
 			q.updateCount(msg.getLong("count"), stamp);
 			if (msg.getString("type").equals("eof"))
 				q.updateStatus("Ended", stamp);
 		} else if (method.startsWith("logdb-query-result-")) {
 			handleStreamingResult(msg);
 		} else if (method.startsWith("logdb-query-")) {
 			int id = msg.getInt("id");
 			LogQuery q = queries.get(id);
 			if (msg.getString("type").equals("eof")) {
 				q.updateCount(msg.getLong("total_count"), stamp);
 				q.updateStatus("Ended", stamp);
 			} else if (msg.getString("type").equals("page_loaded")) {
 				q.updateCount(msg.getLong("count"), stamp);
 				q.updateStatus("Running", stamp);
 			} else if (msg.getString("type").equals("status_change")) {
 				q.updateCount(msg.getLong("count"), stamp);
 				q.updateStatus(msg.getString("status"), stamp);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void handleStreamingResult(Message msg) {
 		List<Map<String, Object>> chunks = (List<Map<String, Object>>) msg.get("bins");
 		boolean last = msg.getBoolean("last");
 		boolean lastCalled = false;
 		int queryId = Integer.valueOf(msg.getMethod().substring("logdb-query-result-".length()));
 		StreamingResultSet rs = null;
 		LogQuery query = null;
 
 		try {
 			query = queries.get(queryId);
 			rs = streamCallbacks.get(queryId);
 
 			List<Object> l = streamingDecoder.decode(chunks);
 
 			ArrayList<Row> rows = new ArrayList<Row>(l.size());
 			for (Object o : l)
 				rows.add(new Row((Map<String, Object>) o));
 
 			if (query != null && rs != null) {
 				rs.onRows(query, rows, last);
 				if (last)
 					lastCalled = true;
 			}
 		} catch (ExecutionException e) {
 			logger.error("araqne logdb client: cannot decode streaming result", e);
 			if (query != null && rs != null && last && !lastCalled)
 				rs.onRows(query, new ArrayList<Row>(), true);
 		} catch (Throwable t) {
 			if (query != null && rs != null && last && !lastCalled)
 				rs.onRows(query, new ArrayList<Row>(), true);
 		}
 	}
 
 	@Override
 	public void onClose(Throwable t) {
 		for (LogQuery q : queries.values())
 			q.updateStatus("Cancelled", Long.MAX_VALUE);
 	}
 
 	private void checkNotNull(String name, Object o) {
 		if (o == null)
 			throw new IllegalArgumentException(name + " parameter should be not null");
 	}
 
 	private Message rpc(String method) throws IOException {
 		if (session == null)
 			throw new IOException("not connected yet, use connect()");
 		return session.rpc(method);
 	}
 
 	private Message rpc(String method, Map<String, Object> params) throws IOException {
 		if (session == null)
 			throw new IOException("not connected yet, use connect()");
 		return session.rpc(method, params);
 	}
 }
