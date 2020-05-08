 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.code.nanorm.internal;
 
 import java.lang.reflect.Type;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.nanorm.DataSink;
import com.google.code.nanorm.NanormFactory;
 import com.google.code.nanorm.Session;
 import com.google.code.nanorm.TypeHandlerFactory;
 import com.google.code.nanorm.annotations.SelectKeyType;
 import com.google.code.nanorm.config.SessionConfig;
 import com.google.code.nanorm.exceptions.ConfigurationException;
 import com.google.code.nanorm.exceptions.DataException;
 import com.google.code.nanorm.internal.config.InternalConfiguration;
 import com.google.code.nanorm.internal.config.StatementConfig;
 import com.google.code.nanorm.internal.introspect.Getter;
 import com.google.code.nanorm.internal.introspect.Setter;
 import com.google.code.nanorm.internal.mapping.result.DataSinkSource;
 import com.google.code.nanorm.internal.mapping.result.ResultCollectorUtil;
 import com.google.code.nanorm.internal.mapping.result.RowMapper;
 import com.google.code.nanorm.internal.session.SessionSpi;
 import com.google.code.nanorm.internal.session.SingleConnSessionSpi;
 import com.google.code.nanorm.internal.type.TypeHandler;
 
 /**
  * Factory implementation.
  * 
  * @author Ivan Dubrov
  * @version 1.0 27.05.2008
  */
 public class FactoryImpl implements NanormFactory, QueryDelegate {
 
 	/**
 	 * Thread local that holds per-thread sessions.
 	 */
 	private final ThreadLocal<SessionSpi> sessions = new ThreadLocal<SessionSpi>();
 
 	private final InternalConfiguration config;
 
 	private final SessionConfig sessionSpiConfig;
 
 	/**
 	 * Logger for logging the SQL statements.
 	 */
 	private static final Logger LOGGER_SQL = LoggerFactory
 			.getLogger(FactoryImpl.class.getPackage().getName() + ".SQL");
 
 	/**
 	 * Logger for logging all other events.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(FactoryImpl.class.getName());
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param internalConfig factory configuration
 	 * @param sessionConfig session configuration
 	 */
 	public FactoryImpl(InternalConfiguration internalConfig,
 			SessionConfig sessionConfig) {
 		this.config = internalConfig;
 		this.sessionSpiConfig = sessionConfig;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public InternalConfiguration getInternalConfiguration() {
 		return config;
 	}
 
 	/**
 	 * @see com.google.code.nanorm.NanormFactory#createMapper(java.lang.Class)
 	 */
 	public <T> T createMapper(Class<T> mapperClass) throws ConfigurationException {
 		config.configure(mapperClass);
 
 		// TODO: Check we mapped this class!
 		return config.getIntrospectionFactory().createMapper(mapperClass,
 				config, this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Session openSession() {
 		if (sessionSpiConfig == null) {
 			throw new IllegalArgumentException("Session SPI is not configured!");
 		}
 		if (sessions.get() != null) {
 			throw new IllegalStateException(
 					"Session was already started for this thread!");
 		}
 
 		final SessionSpi spi = sessionSpiConfig.newSessionSpi();
 		sessions.set(spi);
 		return new TransactionImpl(spi);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Session openSession(Connection connection) {
 		if (connection == null) {
 			throw new IllegalArgumentException("Connection must not be null!");
 		}
 		if (sessions.get() != null) {
 			throw new IllegalStateException(
 					"Session was already started for this thread!");
 		}
 
 		final SessionSpi spi = new SingleConnSessionSpi(connection);
 		sessions.set(spi);
 		return new TransactionImpl(spi);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Object query(StatementConfig stConfig, Object[] args) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Executing the query " + stConfig.getId());
 		}
 		// Request-scoped data
 		Request request = new Request(this);
 
 		SessionSpi spi = sessions.get();
 		boolean isAuto = false;
 		if (spi == null) {
 			// Auto-create session for single request
 			// TODO: Check if autosession is enabled
 			isAuto = true;
 			spi = sessionSpiConfig.newSessionSpi();
 		}
 
 		// Close session spi after this block if in auto mode
 		try {
 			Connection conn = spi.getConnection();
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("Using the connection " + conn);
 			}
 
 			// Generate key prior to mapping the parameters, so we
 			// have a chance to update arguments with generated key
 			selectKey(request, stConfig, false, args);
 
 			// Bind fragment to arguments
 			BoundFragment fragment = stConfig.getStatementBuilder()
 					.bindParameters(args);
 
 			// SQL, parameters and their types
 			StringBuilder sql = new StringBuilder();
 			List<Object> parameters = new ArrayList<Object>();
 			List<Type> types = new ArrayList<Type>();
 
 			// Fill SQL string builder, parameter and types
 			fragment.generate(sql, parameters, types);
 
 			// Close connection after this try
 			try {
 				if (LOGGER_SQL.isDebugEnabled()) {
 					LOGGER_SQL.debug(sql.toString());
 					if (LOGGER_SQL.isTraceEnabled()) {
 						LOGGER_SQL
 								.trace("Parameters: " + parameters.toString());
 					}
 				}
 				
 				// Should we get key using JDBC getGeneratedKeys
 				boolean isJDBCKey = stConfig.getSelectKeyType() == SelectKeyType.AFTER &&
 						stConfig.getSelectKey().getStatementBuilder() == null;
 
 				// Prepare the statement
 				PreparedStatement st = isJDBCKey ? 
 						conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS) :
 						conn.prepareStatement(sql.toString());
 				try {
 					// Map parameters to the statement
 					mapParameters(st, types, parameters);
 
 					if (stConfig.isInsert()) {
 						st.executeUpdate();
 
 						
 						if(isJDBCKey) {
 							processResultSet(stConfig, args, request, st.getGeneratedKeys());
 						} else {
 							selectKey(request, stConfig, true, args);
 						}
 					} else if (stConfig.isUpdate()) {
 						request.setResult(st.executeUpdate());
 					} else {
 						processResultSet(stConfig, args, request, st.executeQuery());
 					}
 
 				} finally {
 					st.close();
 				}
 			} catch (SQLException e) {
 				throw new DataException(
 						"SQL exception occured while executing the query!", e);
 			} finally {
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug("Releasing the connection " + conn);
 				}
 				try {
 					spi.releaseConnection(conn);
 				} catch(DataException e) {
 					LOGGER.error("Failed to release the connection.", e);
 				}
 			}
 		} finally {
 			if (isAuto) {
 				spi.end();
 			}
 		}
 		if (request.getResult() == null) {
 			checkNotPrimitive(stConfig.getResultType());
 		}
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Query result is " + request.getResult());
 		}
 		return request.getResult();
 	}
 
 	private void processResultSet(StatementConfig stConfig, Object[] args, Request request,
 			ResultSet rs) throws SQLException {
 		
 		try {
 			// Create callback that will receive the mapped objects
 			DataSink<Object> callback = createResultSink(
 					stConfig, args, request);
 	
 			// Iterate through the result set
 			RowMapper rowMapper = stConfig.getRowMapper();
 			while (rs.next()) {
 				rowMapper.processResultSet(request, rs, callback);
 			}
 			callback.commit();
 			
 			// Commit all callbacks used in the request
 			request.commitCallbacks();
 		} finally {
 			try {
 				rs.close();
 			} catch(SQLException e) {
 				LOGGER.error("Failed to close ResultSet", e);
 			}
 		}
 	}
 	
 	private DataSink<Object> createResultSink(
 			StatementConfig stConfig, Object[] args, Request request) {
 
 		// If we have DataSink in mapper method parameters -- use it,
 		// otherwise create callback which will set result to the request.
 		DataSink<Object> sink;
 		if (stConfig.getCallbackIndex() != StatementConfig.RETURN_VALUE) {
 			// This is OK, since we deduced result type exactly
 			// from this parameter
 			@SuppressWarnings("unchecked")
 			DataSink<Object> temp = (DataSink<Object>) args[stConfig
 					.getCallbackIndex()];
 			sink = temp;
 		} else {
 			// Prepare data sink and process results
 			ResultGetterSetter rgs = new ResultGetterSetter(stConfig
 					.getResultType());
 			DataSinkSource sinkSource = ResultCollectorUtil
 					.createDataSinkSource(rgs, rgs, stConfig);
 
 			sink = sinkSource.forInstance(request);
 		}
 		return sink;
 	}
 
 	private void checkNotPrimitive(Type type) {
 		if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()
 				&& type != void.class) {
 			// TODO: Refer to the method/mapper class?
 			throw new DataException(
 					"Going to return null result, but return type is primitive ("
 							+ type + ")");
 		}
 	}
 
 	private void selectKey(Request request, StatementConfig stConfig,
 			boolean after, Object[] args) {
 		boolean isKeyAfter = stConfig.getSelectKeyType() == SelectKeyType.AFTER;
 		
 		if (stConfig.isInsert() && stConfig.getSelectKey() != null
 				&& after == isKeyAfter) {
 			
 			if(LOGGER.isDebugEnabled()) {
 				LOGGER.debug("Generating the key for statement " + stConfig.getId());
 			}
 			Object result = query(stConfig.getSelectKey(), args);
 			
 			if(LOGGER.isDebugEnabled()) {
 				LOGGER.debug("Generated key is " + result);
 			}
 			if (stConfig.getKeySetter() != null) {
 				stConfig.getKeySetter().setValue(args, result);
 			}
 
 			request.setResult(result);
 		}
 	}
 
 	private void mapParameters(PreparedStatement statement, List<Type> types,
 			List<Object> params) throws SQLException {
 
 		TypeHandlerFactory factory = config.getTypeHandlerFactory();
 
 		for (int i = 0; i < params.size(); ++i) {
 			Object item = params.get(i);
 			Type type = types.get(i);
 			TypeHandler<?> typeHandler = factory.getTypeHandler(type);
 			typeHandler.setParameter(statement, i + 1, item);
 		}
 	}
 
 	private static class ResultGetterSetter implements Getter, Setter {
 
 		private final Type type;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param type result map result type.
 		 */
 		private ResultGetterSetter(Type type) {
 			this.type = type;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public Type getType() {
 			return type;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public Object getValue(Object instance) {
 			Request request = (Request) instance;
 			return request.getResult();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void setValue(Object instance, Object value) {
 			Request request = (Request) instance;
 			request.setResult(value);
 		}
 	}
 
 	// TODO: toString
 
 	/**
 	 * {@link Session} implementation.
 	 */
 	private class TransactionImpl implements Session {
 
 		private final SessionSpi spi;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param spi {@link SessionSpi} implementation.
 		 */
 		TransactionImpl(SessionSpi spi) {
 			this.spi = spi;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void commit() {
 			checkThread();
 			spi.commit();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void end() {
 			checkThread();
 
 			// Remove from active sessions thread local
 			sessions.remove();
 			spi.end();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void rollback() {
 			checkThread();
 			spi.rollback();
 		}
 
 		private void checkThread() {
 			if (sessions.get() != spi) {
 				throw new IllegalStateException(
 						"This transaction is not bound to this thread!");
 			}
 		}
 	}
 }
