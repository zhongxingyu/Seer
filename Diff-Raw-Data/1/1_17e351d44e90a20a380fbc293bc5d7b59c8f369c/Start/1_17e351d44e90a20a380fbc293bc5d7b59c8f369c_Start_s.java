 package org.akkreditierung.ui;
 
 import com.avaje.ebean.EbeanServerFactory;
 import com.avaje.ebean.Transaction;
 import com.avaje.ebean.config.DataSourceConfig;
 import com.avaje.ebean.config.ServerConfig;
 import org.akkreditierung.model.DB;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import scala.Tuple3;
 import scala.util.Properties;
 
 public class Start {
 	public static void main(String[] args) throws Exception {
 		initDataSource();
 
         String webappDirLocation = "src/main/webapp/";
         int port = Integer.valueOf(Properties.envOrElse("PORT", "8081"));
 
         Server server = new Server(port);
         WebAppContext root = new WebAppContext();
         root.setContextPath("/");
         root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
         root.setResourceBase(webappDirLocation);
         root.setParentLoaderPriority(true);
 
         server.setHandler(root);
         server.start();
         server.join();
 	}
 
 	private static void initDataSource() {
 
 		ServerConfig config = new ServerConfig();
 		config.setName("localhost");
 		config.setDdlGenerate(false);
 		config.setDdlRun(false);
 
         DB.WithSSL();
 
 		DataSourceConfig dataSourceConfig = new DataSourceConfig();
         Tuple3<String,String,String> dbConf = DB.parseConfiguredDbUrl(); //jdbcurl, username, password
         DB.getConfiguredMysqlConnection();
         dataSourceConfig.setUsername(dbConf._2());
         dataSourceConfig.setPassword(dbConf._3());
 		dataSourceConfig.setUrl(dbConf._1());
 		dataSourceConfig.setDriver("com.mysql.jdbc.Driver");
 		dataSourceConfig.setMinConnections(1);
 		dataSourceConfig.setMaxConnections(25);
 		dataSourceConfig.setHeartbeatSql("Select 1");
 		dataSourceConfig.setIsolationLevel(Transaction.READ_COMMITTED);
 
 		config.setDataSourceConfig(dataSourceConfig);
 		config.setDefaultServer(true);
 		config.setDdlGenerate(true);
 		config.setName("akkreditierungsrat");
         config.setDebugSql(true);
 		EbeanServerFactory.create(config);
 	}
 }
