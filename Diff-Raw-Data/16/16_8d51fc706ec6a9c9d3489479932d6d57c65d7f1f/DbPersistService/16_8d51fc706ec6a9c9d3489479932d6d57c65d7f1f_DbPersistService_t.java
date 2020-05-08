 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.persist.db;
 
 import static org.oobium.persist.db.internal.Utils.getDbType;
 import static org.oobium.persist.db.internal.Utils.isMapQuery;
 import static org.oobium.utils.StringUtils.parseUrl;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.literal.Dictionary;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
 import org.oobium.persist.PersistClient;
 import org.oobium.persist.PersistService;
 import org.oobium.persist.ServiceInfo;
 import org.oobium.persist.db.internal.Conversion;
 import org.oobium.persist.db.internal.DbPersistor;
 import org.oobium.persist.db.internal.LoggingConnection;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 public abstract class DbPersistService implements BundleActivator, PersistService {
 
 	private static final Pattern includePattern = Pattern.compile("(include\\s*:).*");
 
 	private static final ThreadLocal<String> threadClient = new ThreadLocal<String>();
 	private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<Connection>();
 	private static final ThreadLocal<Boolean> threadAutoCommit = new ThreadLocal<Boolean>();
 
 	private static final int CREATE = 0;
 	private static final int DESTROY = 1;
 	private static final int RETRIEVE = 2;
 	private static final int UPDATE = 3;
 	
 	private static Map<String, Object> merge(String url, Map<String, Object> properties) {
 		Map<String, Object> map = parseUrl(url);
 		if(properties != null) {
 			map.putAll(properties);
 		}
 		return map;
 	}
 	
 	
 	protected final Logger logger;
 	private final DbPersistor persistor;
 	private final Map<String, Database> databases;
 	private final ServiceInfo info;
 	private BundleContext context;
 
 	private ServiceTracker appTracker;
 
 	private final ReadWriteLock lock;
 
 	private static String logPath;
 	private static FileWriter logWriter;
 
 	public DbPersistService() {
 		logger = LogProvider.getLogger(DbPersistService.class);
 		persistor = new DbPersistor();
 		databases = new HashMap<String, Database>();
 		lock = new ReentrantReadWriteLock();
 		info = new DbServiceInfo(this);
 	}
 
 	/**
 	 * <p>Instantiates a new DbPersistService, opens a session and adds the given database.
 	 * The service is ready to use as-is, but closeSession() must be called when it is
 	 * no longer used to free up database resources.</p>
 	 * <p>This form of DbPersistService is not intended to be used in a multi-threaded 
 	 * environment because it uses a single connection</p>
 	 * <p>Specifying an in-memory database is a good performance increase for tests</p>
 	 * @param client
 	 * @param timeout
 	 */
 	public DbPersistService(String client, Map<String, Object> properties) {
 		this();
 		addDatabase(client, properties);
 		openSession(client);
 	}
 	
 	public DbPersistService(String client, String url) {
 		this(client, parseUrl(url));
 	}
 	
 	public DbPersistService(String client, String url, Map<String, Object> properties) {
 		this(client, merge(url, properties));
 	}
 	
 	private void addDatabase(String client, Map<String, Object> properties) {
 		lock.readLock().lock();
 		try {
 			if(databases.containsKey(client)) {
 				return;
 			}
 		} finally {
 			lock.readLock().unlock();
 		}
 		
 		lock.writeLock().lock();
 		try {
 			Database db = createDatabase(client, properties);
 			databases.put(client, db);
 			if(logger.isLoggingInfo()) {
 				logger.info("added Database for " + client + " (" + db.getDatabaseIdentifier() + ")");
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	private Connection checkLoggingConnection(Connection connection) throws SQLException {
 		String path = System.getProperty("org.oobium.persist.db.logging");
 		if(path == null) {
 			closeLogWriter();
 			return connection;
 		}
 		createLogWriter(path);
 		return new LoggingConnection(connection, logWriter);
 	}
 	
 	private void closeLogWriter() {
 		if(logWriter != null) {
 			try {
 				logWriter.flush();
 				logWriter.close();
 			} catch(IOException e) {
 				logger.warn(e + ": " + e.getMessage());
 			}
 			logWriter = null;
 			logPath = null;
 		}
 	}
 	
 	@Override
 	public void closeSession() {
 		Connection connection = threadConnection.get();
 		if(connection != null) {
 			boolean closed;
 			try {
 				closed = connection.isClosed();
 			} catch(SQLException e) {
 				closed = true;
 			}
 			if(!closed) {
 				try {
 					if(!connection.getAutoCommit()) {
 						connection.rollback();
 					}
 				} catch(Exception e) {
 					// discard
 				}
 				try {
 					connection.close();
 				} catch(Exception e) {
 					logger.warn("could not close database connection", e);
 				}
 			}
 		}
 		threadConnection.set(null);
 		threadClient.set(null);
 		closeLogWriter();
 	}
 	
 	public void commit() throws SQLException {
 		Connection connection = getConnection(false);
 		if(connection != null) {
 			connection.commit();
 		}
 	}
 	
 	@Override
 	public long count(Class<? extends Model> clazz) throws Exception {
 		return count(clazz, (String) null);
 	}
 
 	@Override
 	public long count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws Exception {
 		if(query != null && !query.isEmpty()) {
 			Connection connection = getConnection();
 			int dbType = getDbType(connection);
 			Conversion conversion = Conversion.run(dbType, clazz, query, values);
 			return persistor.count(connection, clazz, conversion.getSql(), conversion.getValues());
 		}
 		return count(clazz);
 	}
 	
 	@Override
 	public long count(Class<? extends Model> clazz, String query, Object... values) throws Exception {
 		Connection connection = getConnection();
 		if(isMapQuery(query)) {
 			int dbType = getDbType(connection);
 			Conversion conversion = Conversion.run(dbType, clazz, query, values);
 			query = conversion.getSql();
 			values = conversion.getValues();
 		}
 		return persistor.count(connection, clazz, query, values);
 	}
 
 	@Override
 	public void create(Model...models) throws Exception {
 		handleCrud(CREATE, models);
 	}
 	
 	public void createDatabase(String client) throws SQLException {
 		Database db = getDatabase(client);
 		db.createDatabase();
 	}
 
 	protected abstract Database createDatabase(String client, Map<String, Object> properties);
 
 	private void createLogWriter(String path) throws SQLException {
 		if(logWriter != null && !path.equals(logPath)) {
 			closeLogWriter();
 		}
 		if(logWriter == null) {
 			try {
 				logWriter = new FileWriter(path);
 			} catch(IOException e) {
 				throw new SQLException("could not create log writer: " + e.getMessage());
 			}
 		}
 		logPath = path;
 	}
 
 	@Override
 	public void destroy(Model...models) throws Exception {
 		handleCrud(DESTROY, models);
 	}
 	
 	private void doRemoveDatabase(String client) {
 		Database database = databases.get(client);
 		if(database != null) {
 			try {
 				database.preRemove();
 				database.dispose();
 				databases.remove(client);
 				logger.info("removed Database for {}", database.client);
 			} catch(Exception e) {
 				logger.warn("error removing Database for {}", e, database.client);
 			}
 		}
 	}
 
 	public void dropDatabase(String client) throws SQLException {
 		Database db = getDatabase(client);
 		db.dispose();
 		db.dropDatabase();
 	}
 
 	public List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeQuery(connection, sql, values);
 	}
 	
 	public List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeQueryLists(connection, sql, values);
 	}
 
 	public Object executeQueryValue(String sql, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeQueryValue(connection, sql, values);
 	}
 	
 	public int executeUpdate(String sql, Object... values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeUpdate(connection, sql, values);
 	}
 	
 	@Override
 	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
 		return findByMapQuery(clazz, query, values, true);
 	}
 	
 	@Override
 	public <T extends Model> T find(Class<T> clazz, String query, Object...values) throws Exception {
 		Connection connection = getConnection();
 		if(isMapQuery(query)) {
 			int dbType = getDbType(connection);
 			Conversion conversion = Conversion.run(dbType, clazz, query, values);
 			query = conversion.getSql();
 			values = conversion.getValues();
 		}
 		return persistor.find(connection, clazz, query, values);
 	}
 
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz) throws Exception {
 		return findAll(clazz, (String) null);
 	}
 
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
 		return findByMapQuery(clazz, query, values, true);
 	}
 	
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz, String query, Object...values) throws Exception {
 		Connection connection = getConnection();
 		if(isMapQuery(query)) {
 			int dbType = getDbType(connection);
 			Conversion conversion = Conversion.run(dbType, clazz, query, values);
 			query = conversion.getSql();
 			values = conversion.getValues();
 		}
 		return persistor.findAll(connection, clazz, query, values);
 	}
 	
 	@Override
 	public <T extends Model> T findById(Class<T> clazz, Object id) throws Exception {
 		Connection connection = getConnection();
 		// TODO always an int for now?
 		return persistor.find(connection, clazz, coerce(id, int.class));
 	}
 
 	@Override
 	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws Exception {
 		if(include == null) {
 			return findById(clazz, id);
 		}
 		if(include.startsWith("include:")) {
 			include = include.substring(8);
 		}
 		return find(clazz, "where id=? include:?", id, include);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <E, T extends Model> E findByMapQuery(Class<T> clazz, Map<String, Object> query, Object[] values, boolean all) throws Exception {
 		if(query != null && !query.isEmpty()) {
 			Object from = query.get("$from");
 			if(from instanceof Map<?,?>) {
 				Map<?,?> map = (Map<?,?>) from;
 				Class<? extends Model> parentClass = ((Class<?>) map.get("$type")).asSubclass(Model.class);
 				Object id = map.get("$id");
 				String field = (String) map.get("$field");
 				String include = coerce(query.get("$include"), String.class);
 				include = (include == null) ? field : ("{" + field + ":" + include + "}");
				Model parent = Model.getPersistService(parentClass).find(parentClass, "id:?,$include:?", id, include);
				if(parent == null) {
					ModelAdapter adapter = ModelAdapter.getAdapter(parentClass);
					if(adapter.hasMany(field)) {
						// don't EVER return null when a collection is expected!
						return (E) new ArrayList<Model>(0);
					}
					return null;
				} else {
					return (E) parent.get(field);
				}
 			}
 			else {
 				Connection connection = getConnection();
 				int dbType = getDbType(connection);
 				Conversion conversion = Conversion.run(dbType, clazz, query, values);
 				if(all) {
 					return (E) persistor.findAll(connection, clazz, conversion.getSql(), conversion.getValues());
 				} else {
 					return (E) persistor.find(connection, clazz, conversion.getSql(), conversion.getValues());
 				}
 			}
 		}
 		else {
 			return (E) findAll(clazz, (String) null);
 		}
 	}
 	
 	public boolean getAutoCommit() {
 		Boolean ac = threadAutoCommit.get();
 		return (ac != null) && ac.booleanValue();
 	}
 
 	Bundle getBundle() {
 		return context.getBundle();
 	}
 	
 	public Connection getConnection() throws SQLException {
 		return getConnection(true);
 	}
 	
 	private Connection getConnection(boolean create) throws SQLException {
 		lock.readLock().lock();
 		try {
 			Connection connection = threadConnection.get();
 			if(connection != null && connection.isClosed()) {
 				connection = null;
 			}
 			if(connection == null && create) {
 				Database db = getDatabase();
 				connection = db.getConnection();
 				connection.setAutoCommit(getAutoCommit());
 				connection = checkLoggingConnection(connection);
 				threadConnection.set(connection);
 			}
 			return connection;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public BundleContext getContext() {
 		return context;
 	}
 	
 	public Database getDatabase() {
 		String client = threadClient.get();
 		if(client == null) {
 			throw new IllegalStateException("session is not open (cannot determine client)");
 		}
 		return getDatabase(client);
 	}
 	
 	private Database getDatabase(String client) {
 		if(databases != null) {
 			Database db = databases.get(client);
 			if(db == null) {
 				throw new IllegalStateException("database for " + client + " has not been setup");
 			}
 			return db;
 		} else {
 			throw new IllegalStateException("no connection pool has been setup");
 		}
 	}
 	
 	@Override
 	public ServiceInfo getInfo() {
 		return info;
 	}
 	
 	public String getMigrationServiceName() {
 		Object o = context.getBundle().getHeaders().get("Oobium-MigrationService");
 		if(o instanceof String) {
 			return (String) o;
 		}
 		return null;
 	}
 	
 	private void handleCrud(int task, Model[] models) throws SQLException {
 		if(models.length == 0) {
 			return;
 		}
 		
 		Connection connection = getConnection();
 		if(task == RETRIEVE) {
 			persistor.retrieve(connection, models);
 		} else {
 			try {
 				connection.setAutoCommit(false);
 				switch(task) {
 				case CREATE:
 					persistor.create(connection, models);
 					break;
 				case DESTROY:
 					persistor.destroy(connection, models);
 					break;
 				case UPDATE:
 					persistor.update(connection, models);
 					break;
 				}
 				if(getAutoCommit()) {
 					connection.commit();
 				}
 			} catch(Exception e) {
 				connection.rollback();
 				threadAutoCommit.set(null);
 				logger.warn("transaction was rolledback", e);
 				if(e instanceof SQLException) {
 					throw (SQLException) e;
 				} else {
 					throw new SQLException("transaction was rolledback", e);
 				}
 			} finally {
 				try {
 					if(getAutoCommit()) {
 						connection.setAutoCommit(true);
 					}
 				} catch(Exception e) {
 					logger.warn("failed to reset connection autocommit", e);
 				}
 			}
 		}
 	}
 
 	public boolean hasContext() {
 		return context != null;
 	}
 	
 	@Override
 	public boolean isSessionOpen() {
 		return threadClient.get() != null;
 	}
 	
 	@Override
 	public void openSession(String name) {
 		threadClient.set(name);
 		threadAutoCommit.set(true);
 	}
 	
 	private void removeDatabase(String client) {
 		lock.writeLock().lock();
 		try {
 			doRemoveDatabase(client);
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	private void removeDatabases() {
 		lock.writeLock().lock();
 		try {
 			for(String client : databases.keySet()) {
 				doRemoveDatabase(client);
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	@Override
 	public void retrieve(Model...models) throws Exception {
 		handleCrud(RETRIEVE, models);
 	}
 	
 	/**
 	 * Retrieve all or parts of the given model, as specified with the given options parameter.<br/>
 	 * <br/>
 	 * If the given options parameter:
 	 * <p><b>is blank</b>: this method delegates to {@link #retrieve(Model...)}</p>
 	 * <p><b>starts with "include:"</b>: this method will retrieve and set all fields of the given model. This operates just like
 	 * {@link #retrieve(Model...)}, except that it will also use the eager loading specified in the "include:" syntax</p>
 	 * <p><b>else</b>: the given options are used as a comma separated list of fields that are to be retrieved and loaded into the
 	 * given model (fields can also use the "include" syntax, and therefore, eager loading). The difference between this and the second option
 	 * is that the model fields that are <i>not</i> given in the options parameter will <i>not</i> be overwritten.</p>
 	 * @param model the model upon which to perform the retrieval; cannot be null
 	 * @param options a String or null
 	 */
 	@Override
 	public void retrieve(Model model, String options) throws Exception {
 		if(options == null || options.length() == 0) {
 			retrieve(model);
 		} else {
 			Connection connection = getConnection();
 			Matcher m = includePattern.matcher(options);
 			if(m.matches()) {
 				persistor.retrieve(connection, model, options.substring(m.end(1)).trim());
 			}
 			else {
 				persistor.retrieveFields(connection, model, options);
 			}
 		}
 	}
 
 	public void rollback() throws SQLException {
 		Connection connection = getConnection(false);
 		if(connection != null) {
 			connection.rollback();
 		}
 	}
 
 	public void setAutoCommit(boolean autoCommit) throws SQLException {
 		boolean changed = false;
 		if(getAutoCommit()) {
 			if(!autoCommit) {
 				threadAutoCommit.set(null);
 				changed = true;
 			}
 		} else {
 			if(autoCommit) {
 				threadAutoCommit.set(true);
 				changed = true;
 			}
 		}
 		if(changed) {
 			Connection connection = getConnection(false);
 			if(connection != null) {
 				connection.setAutoCommit(autoCommit);
 			}
 		}
 	}
 	
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		
 		final String name = context.getBundle().getSymbolicName();
 
 		logger.setTag(name);
 		logger.info("PersistService starting");
 		
 		appTracker = new ServiceTracker(context, PersistClient.class.getName(), new ServiceTrackerCustomizer() {
 			@Override
 			public Object addingService(ServiceReference reference) {
 				String service = (String) reference.getProperty(PersistService.SERVICE);
 				if(name.equals(service)) {
 					String clientName = (String) reference.getProperty(PersistService.CLIENT);
 					if(clientName != null) {
 						Map<String, Object> properties = new HashMap<String, Object>();
 						for(String key : reference.getPropertyKeys()) {
 							properties.put(key, reference.getProperty(key));
 						}
 						try {
 							addDatabase(clientName, properties);
 							return clientName;
 						} catch(Exception e) {
 							logger.warn("failed to add data: {} - {}", clientName, e.getLocalizedMessage());
 						}
 					}
 				}
 				return null;
 			}
 			@Override
 			public void modifiedService(ServiceReference reference, Object service) {
 				// nothing to do... ?
 			}
 			@Override
 			public void removedService(ServiceReference reference, Object service) {
 				if(service != null) {
 					removeDatabase((String) service);
 					DbPersistService.this.context.ungetService(reference);
 				}
 			}
 		});
 		appTracker.open();
 
 		context.registerService(PersistService.class.getName(), this, Dictionary(PersistService.SERVICE, name));
 
 		logger.info("PersistService started (" + name + ")");
 	}
 	
 	public void stop(BundleContext context) throws Exception {
 		appTracker.close();
 		appTracker = null;
 		removeDatabases();
 		this.context = null;
 		logger.info("PersistService stopped");
 		logger.setTag(null);
 	}
 
 	@Override
 	public void update(Model...models) throws Exception {
 		try {
 			handleCrud(UPDATE, models);
 		} catch(SQLException e) {
 			throw new Exception(e);
 		}
 	}
 	
 }
