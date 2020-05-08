 package org.fhw.asta.kasse.server.inject.modules.provider;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.fhw.asta.kasse.server.component.config.ConfigProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.google.gwt.thirdparty.guava.common.base.Strings;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 public class MysqlDBCPDataSourceProvider implements Provider<DataSource> {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(MysqlDBCPDataSourceProvider.class);
 
 	private static final String DATABASE_URL = "db.url";
 	
 	private static final String DATABASE_USER = "db.user";
 	
 	private static final String DATABASE_PASSWORD = "db.password";
 	
 	private static final String DATABASE_MAX_ACTIVE_CONNECTIONS = "db.maxConnections";
 	
 	@Inject
 	private ConfigProvider configProvider;
 	
 	@Override
 	public DataSource get() {
 
 		final String dbUrl = configProvider.get(DATABASE_URL);
 		final String dbUser = configProvider.get(DATABASE_USER);
 		final String dbPassword = configProvider.get(DATABASE_PASSWORD);
 		final String dbMaxActiveConnections = configProvider.get(DATABASE_MAX_ACTIVE_CONNECTIONS, "3");
 		final int maxActiveConnectionsN = Integer.parseInt(dbMaxActiveConnections);
 		
 		if (Strings.isNullOrEmpty(dbUrl) || Strings.isNullOrEmpty(dbUser) || Strings.isNullOrEmpty(dbPassword)) {
 			LOGGER.error("Check env.properties for database parameters (db.url, db.user, db.password)");
 			throw new RuntimeException();
 		}
 		
 		BasicDataSource basicDataSource = new BasicDataSource();
 		basicDataSource.setDefaultAutoCommit(true);
 		
 		basicDataSource.setMaxActive(maxActiveConnectionsN);
 		// TODO: auch konfigurierbar machen
 		basicDataSource.setMaxIdle(3);
 		basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
 		basicDataSource.setUrl(dbUrl);
 		basicDataSource.setUsername(dbUser);
 		basicDataSource.setPassword(dbPassword);
 				
 		return basicDataSource;
 	}
 
 }
