 package com.projectmaxwell.datasource;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import javax.ws.rs.WebApplicationException;
 
 import org.apache.tomcat.jdbc.pool.DataSource;
 import org.apache.tomcat.jdbc.pool.PoolProperties; 
 
 import com.projectmaxwell.exception.DependentServiceException;
 import com.projectmaxwell.exception.PropertiesException;
 
 public class DatasourceConnection {   
 	
 	
 	private static DataSource datasource = null;     
 	
 	DatasourceConnection() {    }     
 	
 	public static Connection getConnection() throws WebApplicationException {        
 		try {            
 			if (datasource == null) {  
 				DatasourceConnection.initDatasource();        
 			}
			Connection con = datasource.getConnection();
			if(con == null){
					throw new DependentServiceException(String.valueOf(Math.random()), "Could not establish connection to datasource.");
 			}
			return con;        
 		} catch (SQLException e) {            
 			throw new WebApplicationException(e);        
 		}    
 	}
 	
 	public static void initDatasource() {
 		/*
 		 * Get database connection values from config.properties
 		 */
 		Properties prop = new Properties();
 		try {
 			prop.load(DatasourceConnection.class.getClassLoader().getResourceAsStream("config.properties"));
 		} catch (IOException e) {
 			throw new PropertiesException(String.valueOf(Math.random()),"Could not load properties for service.");
 		}
 		
 		PoolProperties p = new PoolProperties();     
 		p.setDriverClassName("com.mysql.jdbc.Driver");
 		p.setUrl(prop.getProperty("database.url"));		 
 		p.setUsername(prop.getProperty("database.username"));
 		p.setPassword(prop.getProperty("database.password"));
 		p.setJmxEnabled(true);       
 		p.setTestWhileIdle(false);        
 		p.setTestOnBorrow(true);        
 		p.setValidationQuery("SELECT 1");    
 		p.setTestOnReturn(false);     
 		p.setValidationInterval(30000);      
 		p.setTimeBetweenEvictionRunsMillis(30000);    
 		p.setMaxActive(75);      
 		p.setMaxIdle(75);      
 		p.setInitialSize(10);     
 		p.setMaxWait(10000);      
 		p.setRemoveAbandonedTimeout(60);     
 		p.setMinEvictableIdleTimeMillis(30000);     
 		p.setMinIdle(10);     
 		p.setLogAbandoned(true);           
 		p.setRemoveAbandoned(true);    
 		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"             
 				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");        
 		datasource = new DataSource();      
 		datasource.setPoolProperties(p);
 	} 
 	
 	public static void closeDatasource() {
 		//datasource.
 		datasource.close();   
 	}
 }
