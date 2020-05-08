 package at.bestsolution.persistence.java.internal;
 
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 
 import at.bestsolution.persistence.ObjectMapper;
 import at.bestsolution.persistence.PersistanceException;
 import at.bestsolution.persistence.Session;
 import at.bestsolution.persistence.SessionFactory;
 import at.bestsolution.persistence.java.DatabaseSupport;
 import at.bestsolution.persistence.java.JDBCConnectionProvider;
 import at.bestsolution.persistence.java.JavaSession;
 import at.bestsolution.persistence.java.ObjectMapperFactoriesProvider;
 import at.bestsolution.persistence.java.ObjectMapperFactory;
 import at.bestsolution.persistence.java.ObjectMapperFactory.NamedQuery;
 import at.bestsolution.persistence.java.ProxyFactory;
 import at.bestsolution.persistence.java.SessionCache;
 import at.bestsolution.persistence.java.SessionCacheFactory;
 
 public class JavaSessionFactory implements SessionFactory {
 	JDBCConnectionProvider connectionProvider;
 	ProxyFactory proxyFactory;
 	SessionCacheFactory cacheFactory;
	Map<Class<? extends ObjectMapper<?>>, ObjectMapperFactory<?,?>> factories = new HashMap<Class<? extends ObjectMapper<?>>, ObjectMapperFactory<?,?>>();
 	Map<String,DatabaseSupport> databaseSupports = new HashMap<String,DatabaseSupport>();
 	private static final Logger LOGGER = Logger.getLogger(JavaSessionFactory.class);
 	EventAdmin eventAdmin;
 
 	public void registerConfiguration(JDBCConnectionProvider connectionProvider) {
 		this.connectionProvider = connectionProvider;
 	}
 
 	public void unregisterConfiguration(JDBCConnectionProvider connectionProvider) {
 		this.connectionProvider = null;
 	}
 
 	public void registerMapperFactoriesProvider(ObjectMapperFactoriesProvider provider) {
		factories.putAll(provider.getMapperFactories());
 	}
 
 	public void unregisterMapperFactoriesProvider(ObjectMapperFactoriesProvider provider) {
 		factories.keySet().removeAll(provider.getMapperFactories().keySet());
 	}
 
 	public void registerProxyFactory(ProxyFactory proxyFactory) {
 		this.proxyFactory = proxyFactory;
 	}
 
 	public void unregisterProxyFactory(ProxyFactory proxyFactory) {
 		this.proxyFactory = null;
 	}
 
 	public void registerSessionCacheFactory(SessionCacheFactory cacheFactory) {
 		this.cacheFactory = cacheFactory;
 	}
 
 	public void unregisterSessionCacheFactory(SessionCacheFactory cacheFactory) {
 		this.cacheFactory = null;
 	}
 
 	public void registerDatabaseSupport(DatabaseSupport databaseSupport) {
 		databaseSupports.put(databaseSupport.getDatabaseType(), databaseSupport);
 	}
 
 	public void unregisterDatabaseSupport(DatabaseSupport databaseSupport) {
 		databaseSupports.remove(databaseSupport.getDatabaseType());
 	}
 
 	public void registerEventAdmin(EventAdmin eventAdmin) {
 		this.eventAdmin = eventAdmin;
 	}
 
 	public void unregisterEventAdmin(EventAdmin eventAdmin) {
 		this.eventAdmin = null;
 	}
 
 	@Override
 	public Session createSession() {
 		return new JavaSessionImpl(cacheFactory.createCache());
 	}
 
 	class JavaSessionImpl implements JavaSession {
 		private String id = UUID.randomUUID().toString();
		private Map<String, ObjectMapper<?>> mapperInstances = new HashMap<String, ObjectMapper<?>>();
 		private Stack<Connection> transactionQueue;
 		private SessionCache sessionCache;
 
 		public JavaSessionImpl(SessionCache sessionCache) {
 			this.sessionCache = sessionCache;
 		}
 
 		@Override
 		public String getId() {
 			return id;
 		}
 
 		@Override
 		public DatabaseSupport getDatabaseSupport() {
 			return databaseSupports.get(getDatabaseType());
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public <M extends ObjectMapper<?>> M createMapper(Class<M> mapper) {
			M m = (M) mapperInstances.get(mapper.getName());
 			if( m == null ) {
				m = (M) factories.get(mapper).createMapper(this);
				mapperInstances.put(mapper.getName(), m);
 			}
 			return m;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public <O> List<O> queryForList(String fqnMapper, String queryName,
 				Object... parameters) {
			NamedQuery<O> q = (NamedQuery<O>) factories.get(fqnMapper).createNamedQuery(this, queryName);
			return q.queryForList(parameters);
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public <O> O queryForOne(String fqnMapper, String queryName,
 				Object... parameters) {
 			NamedQuery<O> q = (NamedQuery<O>) factories.get(fqnMapper).createNamedQuery(this, queryName);
 			return q.queryForOne(parameters);
 		}
 
 		@Override
 		public boolean isTransaction() {
 			return transactionQueue != null;
 		}
 
 		@Override
 		public void runInTransaction(Transaction transaction) {
 			String transactionId = UUID.randomUUID().toString();
 
 			LOGGER.debug("Started transaction '"+transactionId+"'");
 
 			Connection connection = connectionProvider.checkoutConnection();
 			try {
 				connection.setAutoCommit(false);
 			} catch (SQLException e2) {
 				LOGGER.error("Failed to turn off auto commit", e2);
 				throw new PersistanceException(e2);
 			}
 
 			if( eventAdmin != null ) {
 				Map<String, Object> properties = new HashMap<String, Object>();
 				properties.put(DATA_SESSION_ID_TOPIC_TRANSACTION_START, transactionId);
 				eventAdmin.sendEvent(new Event(TOPIC_TRANSACTION_START, properties));
 			}
 
 			if( transactionQueue == null ) {
 				transactionQueue = new Stack<Connection>();
 			}
 
 			transactionQueue.add(connection);
 			try {
 				if( transaction.execute() ) {
 					try {
 						connection.commit();
 						if( eventAdmin != null ) {
 							Map<String, Object> properties = new HashMap<String, Object>();
 							properties.put(DATA_SESSION_ID_TOPIC_TRANSACTION_END, transactionId);
 							properties.put(DATA_STATUS_TOPIC_TRANSACTION_END, VALUE_COMMIT);
 							eventAdmin.sendEvent(new Event(TOPIC_TRANSACTION_END, properties));
 						}
 					} catch( SQLException e ) {
 						LOGGER.error("Failed to commit transaction",e);
 						throw new PersistanceException(e);
 					}
 				} else {
 					try {
 						connection.rollback();
 						if( eventAdmin != null ) {
 							Map<String, Object> properties = new HashMap<String, Object>();
 							properties.put(DATA_SESSION_ID_TOPIC_TRANSACTION_END, transactionId);
 							properties.put(DATA_STATUS_TOPIC_TRANSACTION_END, VALUE_ROLLBACK);
 							eventAdmin.sendEvent(new Event(TOPIC_TRANSACTION_END, properties));
 						}
 					} catch( SQLException e ) {
 						LOGGER.error("Failed to rollback transaction",e);
 						throw new PersistanceException(e);
 					}
 				}
 			} catch (Throwable e) {
 				LOGGER.error("Error while executing transactional code", e);
 				try {
 					connection.rollback();
 					if( eventAdmin != null ) {
 						Map<String, Object> properties = new HashMap<String, Object>();
 						properties.put(DATA_SESSION_ID_TOPIC_TRANSACTION_END, transactionId);
 						properties.put(DATA_STATUS_TOPIC_TRANSACTION_END, VALUE_ROLLBACK);
 						eventAdmin.sendEvent(new Event(TOPIC_TRANSACTION_END, properties));
 					}
 				} catch (SQLException e1) {
 					LOGGER.error("Failed to rollback transaction. Swallowing and rethrowing original connection.",e1);
 				}
 				throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
 			} finally {
 				try {
 					connection.setAutoCommit(true);
 				} catch (SQLException e) {
 					LOGGER.error("Failed to set back auto commit", e);
 					throw new PersistanceException(e);
 				}
 				connectionProvider.returnConnection(transactionQueue.pop());
 			}
 			LOGGER.debug("Finished transaction '"+transactionId+"'");
 		}
 
 		@Override
 		public void close() {
 			mapperInstances.clear();
 			sessionCache.release();
 		}
 
 		@Override
 		public Connection checkoutConnection() {
 			if( transactionQueue != null ) {
 				return transactionQueue.peek();
 			}
 			return connectionProvider.checkoutConnection();
 		}
 
 		@Override
 		public void returnConnection(Connection connection) {
 			if( transactionQueue != null ) {
 				return;
 			}
 			connectionProvider.returnConnection(connection);
 		}
 
 		@Override
 		public String getDatabaseType() {
 			return connectionProvider.getDatabaseType();
 		}
 
 		@Override
 		public SessionCache getCache() {
 			return sessionCache;
 		}
 
 		@Override
 		public ProxyFactory getProxyFactory() {
 			return proxyFactory;
 		}
 
 		@Override
 		public Object convertType(Class<?> targetType, Object value) {
 			if( targetType == Boolean.class ) {
 				if( value instanceof Number ) {
 					return ((Number)value).intValue() != 0;
 				}
 			}
 			return value;
 		}
 
 		@Override
 		public Blob handleBlob(String tableName, String blobColumnName,
 				String idColumnName, ResultSet set) throws SQLException {
 			return new LazyBlob(this, tableName, blobColumnName, idColumnName, set.getObject(idColumnName));
 		}
 	}
 }
