 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.txm;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.WeakHashMap;
 
 import javax.sql.CommonDataSource;
 import javax.sql.DataSource;
 
 import zcu.xutil.Objutil;
 import zcu.xutil.XutilRuntimeException;
 import zcu.xutil.sql.AbstractDataSource;
 import zcu.xutil.sql.CloseSuppressing;
 
 /**
  *
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public final class SmartDataSource extends AbstractDataSource implements MResourceFactory {
 	private static final WeakHashMap<DataSource, SmartDataSource> maps = new WeakHashMap<DataSource, SmartDataSource>();
 
 	public static SmartDataSource wrap(DataSource ds) {
 		if (ds instanceof SmartDataSource)
 			return (SmartDataSource) ds;
 		synchronized (maps) {
 			SmartDataSource ret = maps.get(ds);
 			if (ret == null)
 				maps.put(ds, ret = new SmartDataSource(ds));
 			return ret;
 		}
 	}
 
 	private final DataSource datasource;
 	private int commitOrder;
 
 	private SmartDataSource(DataSource dataSource) {
 		this.datasource = dataSource;
 	}
 
 	@Override
 	protected CommonDataSource getBase() {
 		return datasource;
 	}
 
 	@Override
 	public Connection getConnection() throws SQLException {
 		try {
 			Connection ret = (Connection) TxManager.getTxManager().getConnection(this);
 			return ret == null ? datasource.getConnection() : ret;
 		} catch (SQLException e) {
 			throw e;
 		} catch (Exception e) {
 			throw Objutil.rethrow(e);
 		}
 	}
 
 	public void setLastCommit(boolean lastCommit) {
 		this.commitOrder = lastCommit ? 1 : 0;
 	}
 
 	@Override
 	public MResource newResource(TxInfo txinfo) throws SQLException {
 		return new ConRes(this, datasource, txinfo);
 	}
 
 	@Override
 	public int getCommitOrder() {
 		return commitOrder;
 	}
 
 	private static final class ConRes implements MResource {
 		private static final boolean jta = TxManager.getTxManager().getUserTransaction() != null;
 
 		final SmartDataSource resourceFactory;
 		final TxInfo txinfo;
 		final Connection target;
 
 		ConRes(SmartDataSource rf, DataSource ds, TxInfo info) throws SQLException {
 			resourceFactory = rf;
 			txinfo = info;
 			target = ds.getConnection();
 			try {
 				int i = info.getIsolation();
 				if (i != Connection.TRANSACTION_NONE && i != target.getTransactionIsolation())
 					target.setTransactionIsolation(i);
 				if (!jta)
 					target.setAutoCommit(false);
 			} catch (SQLException e) {
 				target.close();
 				throw e;
 			}
 		}
 
 		@Override
 		public final Object getHandle() {
 			if (jta)
 				return new CloseSuppressing(target);
 			return new CloseSuppressing(target) {
 
 				@Override
 				protected void option(Statement statement) throws SQLException {
 					int timeout = txinfo.getTimeout();
 					if (timeout > 0)
 						statement.setQueryTimeout(timeout);
 				}
 
 				@Override
 				public void commit() throws SQLException {
 					closeCheck();
 				}
 
 				public void rollback() throws SQLException {
					closeCheck();
					txinfo.setRollbackOnly();
 				}
 
 				@Override
 				public void setAutoCommit(boolean autoCommit) throws SQLException {
 					if (autoCommit)
 						throw new UnsupportedOperationException("setAutoCommit(true)");
 					closeCheck();
 				}
 			};
 		}
 
 		public final void commit() throws Exception {
 			if (!jta)
 				target.commit();
 		}
 
 		public final void rollback() throws Exception {
 			if (!jta)
 				target.rollback();
 		}
 
 		public final void beforeSuspend() {
 		}
 
 		public final void beforeCompletion() {
 			if (!jta)
 				resourceFactory.testConnection(target);
 		}
 
 		public final void afterCompletion() {
 			try {
 				target.close();
 			} catch (SQLException e) {
 				throw new XutilRuntimeException(e);
 			}
 		}
 
 		public final MResourceFactory getFactory() {
 			return resourceFactory;
 		}
 	}
 }
