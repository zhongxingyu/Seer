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
 
 import static org.oobium.persist.db.internal.DbCache.expireCache;
 import static org.oobium.utils.literal.Properties;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.persist.Model;
 import org.oobium.persist.PersistClient;
 import org.oobium.persist.PersistService;
 import org.oobium.persist.db.internal.DbPersistor;
 import org.oobium.persist.db.internal.SingleConnectionManager;
 import org.oobium.utils.json.JsonUtils;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 public abstract class DbPersistService implements BundleActivator, PersistService {
 
 	private static final ThreadLocal<String> threadClient = new ThreadLocal<String>();
 	private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<Connection>();
 	private static final ThreadLocal<Boolean> threadAutoCommit = new ThreadLocal<Boolean>();
 
 	private static final int CREATE = 0;
 	private static final int DESTROY = 1;
 	private static final int RETRIEVE = 2;
 	private static final int UPDATE = 3;
 
 	private final Logger logger;
 	private BundleContext context;
 	private DbPersistor persistor;
 	private Map<String, ConnectionPool> connectionPools;
 	private SingleConnectionManager sConnManager;
 
 	private ServiceTracker appTracker;
 
 	private ReadWriteLock lock = new ReentrantReadWriteLock();
 
 
 	public DbPersistService() {
 		logger = LogProvider.getLogger(DbPersistService.class);
 		persistor = new DbPersistor();
 		connectionPools = new HashMap<String, ConnectionPool>();
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
 	public DbPersistService(String client, boolean inMemory) {
 		logger = LogProvider.getLogger(DbPersistService.class);
 		openSession(client);
 		persistor = new DbPersistor();
 		sConnManager = new SingleConnectionManager(threadClient.get(), inMemory);
 	}
 	
 	private void addDatabase(String client, Map<String, Object> properties) {
 		lock.readLock().lock();
 		try {
 			if(connectionPools.containsKey(client)) {
 				return;
 			}
 		} finally {
 			lock.readLock().unlock();
 		}
 		
 		lock.writeLock().lock();
 		try {
 			ConnectionPool cp = createConnectionPool(client, properties);
 			connectionPools.put(client, cp);
 			if(logger.isLoggingInfo()) {
 				logger.info("added ConnectionPool for " + client + " (" + cp.getDatabaseIdentifier() + ")");
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	protected abstract ConnectionPool createConnectionPool(String client, Map<String, Object> properties);
 	
 	@Override
 	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.count(connection, clazz, where, values);
 	}
 	
 	@Override
 	public void create(Model...models) throws SQLException {
 		handleCrud(CREATE, models);
 	}
 
 	@Override
 	public void destroy(Model...models) throws SQLException {
 		handleCrud(DESTROY, models);
 	}
 	
 	@Override
 	public List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeQuery(connection, sql, values);
 	}
 
 	@Override
 	public List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeQueryLists(connection, sql, values);
 	}
 
 	@Override
 	public Object executeQueryValue(String sql, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeQueryValue(connection, sql, values);
 	}
 
 	@Override
 	public int executeUpdate(String sql, Object... values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.executeUpdate(connection, sql, values);
 	}
 	
 	@Override
 	public <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.find(connection, clazz, id);
 	}
 
 	@Override
 	public <T extends Model> T find(Class<T> clazz, String where, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.find(connection, clazz, where, values);
 	}
 
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
 		return findAll(clazz, null);
 	}
 	
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object...values) throws SQLException {
 		Connection connection = getConnection();
 		return persistor.findAll(connection, clazz, where, values);
 	}
 
 	private boolean autoCommit() {
 		Boolean ac = threadAutoCommit.get();
 		return (ac != null) && ac.booleanValue();
 	}
 
 	@Override
 	public void commit() throws SQLException {
 		Connection connection = getConnection();
 		connection.commit();
 		connection.setAutoCommit(true);
 		setAutoCommit(true);
 	}
 	
 	@Override
 	public void rollback() throws SQLException {
 		Connection connection = getConnection();
 		connection.rollback();
 		connection.setAutoCommit(true);
 		setAutoCommit(true);
 	}
 	
 	@Override
 	public void setAutoCommit(boolean autoCommit) throws SQLException {
 		if(autoCommit()) {
 			if(!autoCommit) {
 				threadAutoCommit.set(null);
 				getConnection().setAutoCommit(autoCommit);
 			}
 		} else {
 			if(autoCommit) {
 				threadAutoCommit.set(true);
 				getConnection().setAutoCommit(autoCommit);
 			}
 		}
 	}
 	
 	@Override
 	public void openSession(String name) {
 		threadClient.set(name);
 		threadAutoCommit.set(true);
 		expireCache();
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
 		expireCache();
 	}
 	
 	public Connection getConnection() throws SQLException {
 		lock.readLock().lock();
 		try {
 			Connection connection = threadConnection.get();
 			if(connection == null || connection.isClosed()) {
 				String clientName = threadClient.get();
 				if(clientName == null) {
 					throw new SQLException(clientName + " is not a registered PersistClient");
 				}
 				if(connectionPools != null) {
 					ConnectionPool cp = connectionPools.get(clientName);
 					if(cp == null) {
 						throw new SQLException("database for " + clientName + " has not been setup");
 					}
 					connection = cp.getConnection();
 				} else if(sConnManager != null) {
 					connection = sConnManager.getConnection();
 				} else {
 					throw new SQLException("no connection pool or manager has been setup");
 				}
 				threadConnection.set(connection);
 			}
 			return connection;
 		} finally {
 			lock.readLock().unlock();
 		}
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
 				if(autoCommit()) {
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
 					if(autoCommit()) {
 						connection.setAutoCommit(true);
 					}
 				} catch(Exception e) {
 					logger.warn("failed to reset connection autocommit", e);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean isSessionOpen() {
 		return threadClient.get() != null;
 	}
 	
 	private void removeDatabase(String client) {
 		lock.writeLock().lock();
 		try {
 			ConnectionPool cp = connectionPools.remove(client);
 			if(cp != null) {
 				cp.dispose();
 				logger.log(Logger.INFO, "removed ConnectionPool for " + client);
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	@Override
 	public void retrieve(Model...models) throws SQLException {
 		handleCrud(RETRIEVE, models);
 	}
 	
 	@Override
 	public void retrieve(Model model, String hasMany) throws SQLException {
 		// TODO hack: re-implement directly in DbPersistor
 		Connection connection = getConnection();
 		Model tmp = persistor.find(connection, model.getClass(), "where id=? include:?", model.getId(), hasMany);
 		if(tmp != null) {
			model.put(hasMany, tmp.get(hasMany));
 		}
 	}
 
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		logger.setTag(context.getBundle().getSymbolicName());
 		logger.info("PersistService starting");
 		
 		appTracker = new ServiceTracker(context, PersistClient.class.getName(), new ServiceTrackerCustomizer() {
 			@Override
 			public Object addingService(ServiceReference reference) {
 				List<String> services = JsonUtils.toStringList((String) reference.getProperty(PersistService.SERVICE));
 				if(services != null) {
 					if(services.contains(getPersistServiceName())) {
 						String clientName = (String) reference.getProperty(PersistService.CLIENT);
 						if(clientName != null) {
 							Map<String, Object> properties = new HashMap<String, Object>();
 							for(String key : reference.getPropertyKeys()) {
 								if(!key.equals(PersistService.CLIENT) && !key.equals(PersistService.CLIENT)) {
 									properties.put(key, reference.getProperty(key));
 								}
 							}
 							addDatabase(clientName, properties);
 							return clientName;
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
 
 		context.registerService(PersistService.class.getName(), this, Properties(PersistService.SERVICE, getPersistServiceName()));
 
 		logger.info("PersistService started (" + getPersistServiceName() + ")");
 	}
 
 	public String getMigrationServiceName() {
 		Object o = context.getBundle().getHeaders().get("Oobium-MigrationService");
 		if(o instanceof String) {
 			return (String) o;
 		}
 		return null;
 	}
 	
 	public abstract String getPersistServiceName();
 
 	public void stop(BundleContext context) throws Exception {
 		appTracker.close();
 		appTracker = null;
 		this.context = null;
 		logger.info("PersistService stopped");
 		logger.setTag(null);
 	}
 
 	@Override
 	public void update(Model...models) throws SQLException {
 		handleCrud(UPDATE, models);
 	}
 	
 }
