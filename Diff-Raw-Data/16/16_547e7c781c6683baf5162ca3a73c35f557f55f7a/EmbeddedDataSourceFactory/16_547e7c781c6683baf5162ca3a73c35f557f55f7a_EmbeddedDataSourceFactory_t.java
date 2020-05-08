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
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.derby.jdbc.EmbeddedDataSource;
 import org.apache.derby.tools.ij;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.core.io.Resource;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.ConnectionCallback;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 // TODO: check that when schema creation fails, the database is shut down (i.e. destroy is called)
 public class EmbeddedDataSourceFactory implements InitializingBean, DisposableBean, FactoryBean {
 	private final Log log = LogFactory.getLog(EmbeddedDataSourceFactory.class);
 	
 	private boolean create;
 	
 	private String databaseName;
 	private String user;
 	private List/*<OfflineAction>*/ beforeStartupActions;
 	private String scriptEncoding = "UTF-8";
 	private List schemaCreationScripts;
 	private List/*<OfflineAction>*/ afterShutdownActions;
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
 
 	public void setBeforeStartupActions(List beforeStartupActions) {
 		this.beforeStartupActions = beforeStartupActions;
 	}
 
 	public void setScriptEncoding(String scriptEncoding) {
 		this.scriptEncoding = scriptEncoding;
 	}
 
 	public void setSchemaCreationScripts(List schemaCreationScripts) {
 		this.schemaCreationScripts = schemaCreationScripts;
 	}
 
 	public void setAfterShutdownActions(List afterShutdownActions) {
 		this.afterShutdownActions = afterShutdownActions;
 	}
 	
 	private void executeOfflineActions(List/*<OfflineAction>*/ actions) throws Exception {
 		if (actions != null) {
 			for (Iterator it = actions.iterator(); it.hasNext();) {
 				((OfflineAction)it.next()).execute(new File(databaseName));
 			}
 		}
 	}
 	
 	public void afterPropertiesSet() throws Exception {
 		executeOfflineActions(beforeStartupActions);
 		dataSource = new EmbeddedDataSource();
 		dataSource.setDatabaseName(databaseName);
 		if (create) {
 			dataSource.setCreateDatabase("create");
 		}
 		dataSource.setUser(user);
 		if (schemaCreationScripts != null) {
 			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 			if (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYS.SYSSCHEMAS WHERE SCHEMANAME=?", new Object[] { user.toUpperCase() }) == 0) {
 				jdbcTemplate.execute(new ConnectionCallback() {
 					public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
 						for (Iterator it = schemaCreationScripts.iterator(); it.hasNext(); ) {
 							Resource resource = (Resource)it.next();
 							try {
 								InputStream in = resource.getInputStream();
 								try {
 									LoggerOutputStream out = new LoggerOutputStream(log, "UTF-8");
 									try {
 										// runScript returns the number of SQLExceptions thrown during the execution
 										if (ij.runScript(connection, in, scriptEncoding, out, "UTF-8") > 0) {
 											throw new SchemaCreationException("Script " + resource.getFilename() + " executed with errors");
 										}
 									}
 									finally {
 										out.close();
 									}
 								}
 								finally {
 									in.close();
 								}
 							}
 							catch (IOException ex) {
 								throw new SchemaCreationException("Failed to read script " + resource.getFilename(), ex);
 							}
 						}
 						return null;
 					}
 				});
 			}
 		}
 	}
 
 	public void destroy() throws Exception {
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
 
 	public Class getObjectType() {
 		return EmbeddedDataSourceFactory.class;
 	}
 
 	public boolean isSingleton() {
 		return true;
 	}
 
 	public Object getObject() throws Exception {
 		return dataSource;
 	}
 }
