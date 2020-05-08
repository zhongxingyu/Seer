 /**
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package net.sf.springderby;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.derby.jdbc.EmbeddedDataSource;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 /**
  * 
  * @author Andreas Veithen
  * @version $Id$
  */
 public class EmbeddedDataSourceFactory implements InitializingBean, DisposableBean, FactoryBean {
 	private static class OfflineActionContextImpl implements OfflineActionContext {
 		private final File databaseLocation;
 		
 		public OfflineActionContextImpl(File databaseLocation) {
 			this.databaseLocation = databaseLocation;
 		}
 
 		public File getDatabaseLocation() {
 			return databaseLocation;
 		}
 	}
 	
 	private static class OnlineActionContextImpl implements OnlineActionContext {
 		public final JdbcTemplate jdbcTemplate;
 
 		public OnlineActionContextImpl(DataSource dataSource) {
 			jdbcTemplate = new JdbcTemplate(dataSource);
 		}
 
 		public JdbcTemplate getJdbcTemplate() {
 			return jdbcTemplate;
 		}
 	}
 	
 	private final Log log = LogFactory.getLog(EmbeddedDataSourceFactory.class);
 	
 	private boolean create;
 	
 	private String databaseName;
 	private String user;
 	private List<OfflineAction> beforeStartupActions;
 	private List<OnlineAction> afterCreationActions;
 	private List<OfflineAction> afterShutdownActions;
 	private EmbeddedDataSource dataSource;
 	
 	public void setDatabaseName(String databaseName) {
 		this.databaseName = databaseName;
 	}
 	
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public void setCreate(boolean create) {
 		this.create = create;
 	}
 
 	public void setBeforeStartupActions(List<OfflineAction> beforeStartupActions) {
 		this.beforeStartupActions = beforeStartupActions;
 	}
 	
 	public void setAfterCreationActions(List<OnlineAction> afterCreationActions) {
 		this.afterCreationActions = afterCreationActions;
 	}
 
 	public void setAfterShutdownActions(List<OfflineAction> afterShutdownActions) {
 		this.afterShutdownActions = afterShutdownActions;
 	}
 	
 	private void executeOfflineActions(List<OfflineAction> actions) throws Exception {
 		if (actions != null) {
 			OfflineActionContext context = new OfflineActionContextImpl(new File(databaseName));
 			for (OfflineAction action : actions) {
 				action.execute(context);
 			}
 		}
 	}
 	
 	private void executeOnlineActions(List<OnlineAction> actions) throws Exception {
 		if (actions != null) {
 			OnlineActionContext context = new OnlineActionContextImpl(dataSource);
 			for (OnlineAction action : actions) {
 				action.execute(context);
 			}
 		}
 	}
 	
 	public void afterPropertiesSet() throws Exception {
 		executeOfflineActions(beforeStartupActions);
 		dataSource = new EmbeddedDataSource();
 		dataSource.setDatabaseName(databaseName);
 		dataSource.setUser(user);
 		boolean databaseExists;
 		try {
 			// Attempt to open a connection to check the status of the database
 			dataSource.getConnection().close();
 			databaseExists = true;
 		}
 		catch (SQLException ex) {
 			if ("XJ004".equals(ex.getSQLState())) {
 				databaseExists = false;
 			} else {
 				throw ex;
 			}
 		}
 		if (!databaseExists) {
 			if (!create) {
 				throw new Exception("Database " + databaseName + " doesn't exist"); // TODO: decide on exception type to use
 			} else {
 				dataSource.setCreateDatabase("create");
 				// Open a connection to create the database and take it online
 				dataSource.getConnection().close();
 				boolean failure = true;
 				try {
 					executeOnlineActions(afterCreationActions);
 					failure = false;
 				}
 				finally {
 					if (failure) {
 						shutdown();
 					}
 				}
 			}
 		}
 	}
 	
 	private void shutdown() throws Exception {
 		dataSource.setShutdownDatabase("shutdown");
 		// getConnection must be called to actually perform the shutdown. Note that this 
 		// instruction always throws an exception. We therefore catch SQLExceptions and check
 		// for the expected exception type.
 		try {
 			dataSource.getConnection();
 		}
 		catch (SQLException ex) {
 			if ("08006".equals(ex.getSQLState())) {
 				log.info(ex.getMessage());
 			} else {
 				throw ex;
 			}
 		}
 		executeOfflineActions(afterShutdownActions);
 	}
 	
 	public void destroy() throws Exception {
 		shutdown();
 	}
 
 	public Class<?> getObjectType() {
		return EmbeddedDataSourceFactory.class;
 	}
 
 	public boolean isSingleton() {
 		return true;
 	}
 
 	public Object getObject() throws Exception {
 		return dataSource;
 	}
 }
