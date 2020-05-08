 /*
  * @(#) JNDIInitializeServlet.java @VERSION@
  * 
  * Copyright (c) 2009+ Amit Kumar
  * 
  * The software is released under ASL 2.0, Please
  * read License.txt
  *
  */
 package org.imirsel.plugins;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import javax.sql.DataSource;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.imirsel.annotations.SqlPersistence;
 import org.imirsel.model.Job;
 import org.imirsel.model.JobResult;
import org.imirsel.nema.artifactservice.ArtifactManagerImpl;
import org.imirsel.nema.directoryservice.PluginConstants;
 import org.meandre.configuration.CoreConfiguration;
 import org.meandre.plugins.MeandrePlugin;
 
 
 
 /**Initializes various JNDI resources for MEANDRE
  * 
  * @author Amit Kumar
  * Created on Sep 1 2009
  * @revised Amit Kumar -changed the autoCommit to false in the pooling data source.
  * @modified Amit Kumar -throws runtime exception if the nemaflowproperties is not
  * found
  *
  */
 public class JndiInitializeServlet extends HttpServlet implements MeandrePlugin {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Logger logger;
 	private Context ctx;
 	private boolean inited=Boolean.FALSE;
 	private CoreConfiguration coreConfiguration;
 	private DataSource dataSourceFlowResults=null;
 	private DataSource dataSourceJob=null;
 	
 	final static String DATA_PROPERTY_1 =  "driver";
 	final static String DATA_PROPERTY_2 =  "jdbcURL";
 	final static String DATA_PROPERTY_3 =  "user";
 	final static String DATA_PROPERTY_4 =  "password";
 	final static String DATA_PROPERTY_5 =  "concurrentConnection";
 	
 	
 	// TEST
 	public static void main(String args[]){
 		System.out.println("Before Trying to load the flow result properties file");
 		Properties flowServiceProperties = new Properties();
 		try {
 			flowServiceProperties.load(JndiInitializeServlet.class.getClassLoader().getResourceAsStream("nemaflowservice.properties"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		System.out.println("After Trying to load the flow result properties file");
 	}
 	
 	
 	/**Called when the servlet starts -called only once.
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	public void init(ServletConfig config) throws ServletException{
 		super.init(config);
 		// initialize the ArtifactManagerImpl
 		ArtifactManagerImpl.init(this.coreConfiguration);
 		logger.info("Starting the JNDIInitialize Servlet: -loading various database contexts");
 		Properties nemaFlowServiceProperties = new Properties();
 		try {
 			nemaFlowServiceProperties.load(JndiInitializeServlet.class.getClassLoader().getResourceAsStream("nemaflowservice.properties"));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			logger.severe(e1.getMessage());
 			throw new RuntimeException("Cannot find nemaflowservice.properties");
 		}
 		try{
 			Hashtable env = new Hashtable();
 			env.put(Context.INITIAL_CONTEXT_FACTORY,"org.mortbay.naming.InitialContextFactory");
 			env.put(Context.PROVIDER_URL, "localhost:1099");
 			ctx = new InitialContext(env);
 		}catch (Exception e){
 			logger.severe("Error configuring initial context "+e);
 			throw new ServletException(e);
 		}
 		
 		try {
 			dataSourceFlowResults = getDataSource(nemaFlowServiceProperties);
 			dataSourceJob = getDataSource(nemaFlowServiceProperties);
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.severe("Error getting properites  "+e);
 		}
 		logger.info("binding flowresults datasource");
 		bindObject(PluginConstants.JOBRESULT_JNDI_SERVICE,dataSourceFlowResults);
 		bindObject(PluginConstants.JOB_JNDI_SERVICE,dataSourceJob);
 		createDataTablesIfNotExist();
 		this.inited(true);
 	}
 	
 	
 	/**Creates tables if the tables don't already exist
 	 * 
 	 */
 	private void createDataTablesIfNotExist() {
 		// check for the Job table, if it does not exist create it
 		SqlPersistence mdata=Job.class.getAnnotation(SqlPersistence.class);
 		String sqlCreate =mdata.create();
 		if(sqlCreate.equals("[unassigned]")){
 			System.out.println("Ignoring sql Create for Job.class "+ sqlCreate);
 			return;
 		}
 		
 		 Connection con = null;
          try {
                 con = dataSourceJob.getConnection();
          }catch(SQLException e) {
                 System.out.println("Error getting connection from the Job dataSource " + e.getMessage());
          }
          PreparedStatement createTable = null;
          try {
 			createTable= con.prepareStatement(sqlCreate);
 			createTable.execute();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				con.commit();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				con.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		
 		// create the job result table if it does not exist
 		mdata=JobResult.class.getAnnotation(SqlPersistence.class);
 		sqlCreate =mdata.create();
 		if(sqlCreate.equals("[unassigned]")){
 			System.out.println("Ignoring sql Create for JobResult.class "+ sqlCreate);
 			return;
 		}
 		
 		
        try {
              con = dataSourceJob.getConnection();
          }catch(SQLException e) {
              System.out.println("Error getting connection from the Job dataSource " + e.getMessage());
         }
          createTable = null;
        try {
 			createTable= con.prepareStatement(sqlCreate);
 			createTable.execute();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				con.commit();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				con.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 
 	private DataSource getDataSource(Properties properties) throws Exception {
 		String driver =properties.getProperty(DATA_PROPERTY_1);
 		String jdbc_url = properties.getProperty(DATA_PROPERTY_2);
 		String user = properties.getProperty(DATA_PROPERTY_3);
 		String password =properties.getProperty(DATA_PROPERTY_4);
 		try {
 			Class.forName(driver);
 		} catch (ClassNotFoundException e) {
 			throw new Exception("Missing database driver: "+ driver);
 		}
 
 		
 		DataSource	dataSource = setupDataSource(jdbc_url, user, password);
 		try {
 			Connection connection = dataSource.getConnection();
 			if(connection==null){
 				logger.severe("Error: Connection is null..." + jdbc_url);
 				throw new Exception("Error: Connection is null..." + jdbc_url);
 			}
 			System.out.println(connection.getMetaData().getDatabaseProductName());
 			connection.close();
 		} catch (SQLException e) {
 			System.out.println("Error: Could not connect to the server: " + jdbc_url +" \nuser: " +user + "\npassword: " + password);
 			System.exit(0);
 		}
 		return dataSource;
 	}
 	
 	private  DataSource setupDataSource(String jdbc_url, String user,String password) {
 		//System.out.println("Setting up jdbc datasource: " + jdbc_url + " user: " + user);
 		GenericObjectPool connectionPool = new GenericObjectPool(null);
 		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbc_url, user, password);
 		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
 				connectionFactory, connectionPool, null, null, false, false);
 		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
 		return dataSource;
 	}
 
 
 	public void bindObject(String jndiLoc, Object obj)
 	{	
 		//System.out.println("JNDI Binding: " + jndiLoc);
 		try{
 		ctx.bind(jndiLoc, obj);
 		}catch(NamingException e){
 			logger.severe("Error occured with JNDI namespace. "+e +":"+ e.getMessage());
 		}
 	}
 	
 	
 	public  void service(HttpServletRequest req, HttpServletResponse res)
     throws ServletException,java.io.IOException{
 		res.getOutputStream().println("JNDI Service Servlet");
 	}
 
 	public String getAlias() {
 		return "/jndi";
 	}
 
 	public String getName() {
 		return this.getClass().getName();
 	}
 
 	public String getPluginClassName() {
 		return this.getClass().getName();
 	}
 
 	public Properties getProperties() {
 		return null;
 	}
 
 	public void inited(Boolean success) {
 		this.inited=success;
 	}
 
 	public boolean isFilter() {
 		return false;
 	}
 
 	public boolean isInited() {
 		return this.inited;
 	}
 
 	public boolean isResource() {
 		return false;
 	}
 
 	public boolean isServlet() {
 		return true;
 	}
 
 	public void setLogger(Logger log) {
 		this.logger=log;
 	}
 
 
 	public void setCoreConfiguration(CoreConfiguration cnf) {
 		// TODO Auto-generated method stub
 		this.coreConfiguration = cnf;
 	}
 
 }
