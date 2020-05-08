 package org.javasimon.tomcat;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.Statement;
 import java.util.Map;
 import org.apache.tomcat.jdbc.pool.ConnectionPool;
 import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
 import org.apache.tomcat.jdbc.pool.PoolProperties.InterceptorProperty;
 import org.apache.tomcat.jdbc.pool.PooledConnection;
 import org.javasimon.jdbc4.SimonCallableStatement;
 import org.javasimon.jdbc4.SimonPreparedStatement;
 import org.javasimon.jdbc4.SimonStatement;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Tomcat JDBC connection pool interceptor.
  * Monitors JDBC statement duration. Requires Tomcat 7.
  * @author gquintana
  */
 public class SimonJdbcInterceptor extends JdbcInterceptor {
 	/**
 	 * Logger
 	 */
 	private static final Logger LOGGER=LoggerFactory.getLogger(SimonJdbcInterceptor.class);
 	/**
 	 * Simon name prefix
 	 */
 	private String prefix="org.javasimon.jdbc";
 	private Constructor<SimonStatement> statementConstructor;
 	/**
 	 * Prepared statement constructor 
 	 */
 	private Constructor<SimonPreparedStatement> preparedStatementConstructor;
 	/**
 	 * Callable statement constructor 
 	 */
 	private Constructor<SimonCallableStatement> callableStatementConstructor;
 	/**
 	 * Constructor
 	 */
 	public SimonJdbcInterceptor() {
 		try {
 			statementConstructor = getConstructor(SimonStatement.class,
 				new Class[]{
 					Connection.class,
 					Statement.class,
 					String.class
 				});
 			
 			preparedStatementConstructor = getConstructor(SimonPreparedStatement.class,
 				new Class[]{
 					Connection.class,
 					PreparedStatement.class,
 					String.class,
 					String.class
 				});
 			callableStatementConstructor = getConstructor(SimonCallableStatement.class,
 				new Class[]{
 					Connection.class,
 					CallableStatement.class,
 					String.class,
 					String.class
 				});
 			LOGGER.info("Simon JDBC interceptor initialized");
 		} catch (NoSuchMethodException noSuchMethodException) {
 			LOGGER.info("Simon JDBC interceptor failed", noSuchMethodException);
 		}
 	}
 	public String getPrefix() {
 		return prefix;
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 
 	@Override
 	public void setProperties(Map<String, InterceptorProperty> properties) {
 		super.setProperties(properties);
 		InterceptorProperty prefixProperty=properties.get("prefix");
 		if (prefixProperty!=null) {
 			prefix=prefixProperty.getValue();
 		}
 	}
 
 
 	@Override
 	public void reset(ConnectionPool parent, PooledConnection con) {
 		// Do nothing
 	}
 	/**
 	 * Interceptor main method
 	 */
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		if (proxy instanceof Connection) {
 			Connection connection=(Connection) proxy;
 			Object result=super.invoke(connection, method, args);
 			result=wrapStatement(connection, method, args, result);
 			return result;
 		} else {
 			return super.invoke(proxy, method, args);
 		}
 	}
 
 	/**
 	 * Wrap return statement
 	 */
 	private Object wrapStatement(Connection connection, Method method, Object[] args, Object result) {
 		String methodName = method.getName();
 		Object newResult = null;
 		String sql=null;
 		if (compare("createStatement", methodName)&&result instanceof Statement) {
 			//createStatement
 			newResult = wrapStatement(connection, (Statement) result);
 		} else if (compare("prepareStatement", methodName)&&result instanceof PreparedStatement) {
 			//prepareStatement
 			sql=(String) args[0];
 			newResult = wrapPreparedStatement(connection, (PreparedStatement) result, sql);
 		} else if (compare("prepareCall", methodName)&&result instanceof CallableStatement) {
 			//prepareCall
 			sql=(String) args[0];
 			newResult = wrapCallableStatement(connection, (CallableStatement) result, sql);
 		}
 		if (newResult == null) {
 			newResult = result;
 		} else {
 			LOGGER.info("Wrapper Statement for "+sql);
 		}
 		return newResult;
 	}
 	/**
 	 * Find constructor for given type and argument types
 	 */
 	private <T> Constructor<T> getConstructor(Class<T> type, Class<?>[] argumentTypes) throws NoSuchMethodException {
 		Constructor<T> constructor=type.getDeclaredConstructor(argumentTypes);
 		if (!constructor.isAccessible()) {
 			constructor.setAccessible(true);
 		}
 		return constructor;
 	}
 	/**
 	 * Create a new instance of T using specified constructor arguments
 	 * @return New instance or null in case of failure
 	 */
 	private <T> T newInstance(Constructor constructor, Object[] parameterValues) {
 		try {
			@SuppressWarnings("unchecked")
			T instance= (T) constructor.newInstance(parameterValues);
			return instance;
 		} catch (InstantiationException instantiationException) {
 			LOGGER.error("Simon statement instantiation failed", instantiationException);
 			return null;
 		} catch (IllegalAccessException illegalAccessException) {
 			LOGGER.error("Simon statement instantiation failed", illegalAccessException);
 			return null;
 		} catch (InvocationTargetException invocationTargetException) {
 			LOGGER.error("Simon statement instantiation failed", invocationTargetException);
 			return null;
 		}
 	}
 	/**
 	 * Create a simple statement
 	 */
 	private SimonStatement wrapStatement(Connection connection, Statement statement) {
 		return newInstance(statementConstructor,
 			new Object[]{
 				connection,
 				statement,
 				prefix
 			});
 	}
 	/**
 	 * Create a prepared statement
 	 */
 	private SimonPreparedStatement wrapPreparedStatement(Connection connection, PreparedStatement statement, String sql) {
 		return newInstance(preparedStatementConstructor,
 			new Object[]{
 				connection,
 				statement,
 				sql,
 				prefix
 			});
 	}
 	/**
 	 * Create a callable statement
 	 */
 	private SimonCallableStatement wrapCallableStatement(Connection connection, CallableStatement statement, String sql) {
 		return newInstance(callableStatementConstructor, 
 		  new Object[]{
 			  connection,
 			  statement,
 			  sql,
 			  prefix
 		  });
 	}
 
 }
