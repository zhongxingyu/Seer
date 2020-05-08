 package starter;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSourceFactory;
 import org.apache.jasper.servlet.JspServlet;
 import org.eclipse.jetty.jndi.NamingUtil;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.FilterMapping;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.resource.FileResource;
 import org.eclipse.jetty.util.resource.Resource;
 import org.eclipse.jetty.util.resource.ResourceCollection;
 import org.openxava.ex.dwr.ForceUtf8DwrServlet;
 import org.openxava.ex.json.JsonViewerServlet;
 import org.openxava.ex.tools.DynamicLoaderFilter;
 import org.openxava.ex.tools.SchemaUpdateServlet;
 import org.openxava.ex.tools.TokenCookieSSOFilter;
 import org.openxava.web.servlets.ImagesServlet;
 import org.openxava.web.servlets.ModuleServlet;
 
 /**
  * The jetty server to start development workspace.<br/>
  * Run this application with following environment variables:
  * <pre>
  *  - HTTP_PORT: http port, default is 8080
  *  - CTX_PATH:  context path, default TestApp
  *  - JDBC_URL:  jdbc url
  *  - DB_USER:   username of database
  *  - DB_PASS:   password of database
  * </pre>
  * @author root
  *
  */
 public class ContextApp {
 	private static final String DEFAULT_HTTP_PORT = "8080";
 	private static final String DEFAULT_CTX_PATH = "TestApp";		//In OpenXava, the context path is also the application name
 
 	@SuppressWarnings("serial")
 	public static void main(String[] args) throws Exception {
 		EnvSettings es = readEnv();
 		
 		//System properties for logging
 		System.setProperty("org.eclipse.jetty.LEVEL", "ALL");
 		System.setProperty("org.eclipse.jetty.util.log.SOURCE", "false");
 		
         //Try to stop the previous server instance
         URL stop = new URL("http://127.0.0.1:" + es.httpPort + "/STOP");
         try{ stop.openStream(); }catch(Exception ex){ /*Ignore it*/}
 		
 		final Server server = new Server(es.httpPort);
         
         prepareDataSource(server, es);
         //Properties for persistence.xml and hibernate.cfg.xml
         System.setProperty("PROP_DATASOURCE_JNDI_NAME", "java:comp/env/" + es.getJndiName());
         System.setProperty("PROP_HIBERNATE_DIALECT", es.getHibernateDialect());
         System.setProperty("PROP_HIBERNATE_DEFAULT_SCHEMA", es.getDefaultSchema());
 
         ContextHandlerCollection contexts = new ContextHandlerCollection();
         server.setHandler(contexts);
 
         //The ROOT web app
         ServletContextHandler root = new ServletContextHandler(contexts, "/", ServletContextHandler.SESSIONS);
         root.setBaseResource(buildFolderResource("jetty-starter/war-root"));
         root.addServlet(DefaultServlet.class, "/");    //Default servlet
         //FIXME: org.eclipse.jetty.servlet.ServletHolder.initJspServlet() need it - InitParameter "com.sun.appserv.jsp.classpath"
         root.setClassLoader(ContextApp.class.getClassLoader());
         //JSP Servlet
         System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
         root.addServlet(JspServlet.class, "*.jsp");
         //The STOP Servlet
         root.addServlet(new ServletHolder(new HttpServlet() {
             @Override
             public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
                 System.err.println(">>> Stop server request from /STOP ...");
                 System.exit(0);
             }
         }), "/STOP");
         //The "main" web app - for SSO testing
         ServletContextHandler main = new ServletContextHandler(contexts, "/main", ServletContextHandler.SESSIONS);
         main.setBaseResource(buildFolderResource("jetty-starter/war-app-main"));
         main.addServlet(DefaultServlet.class, "/");    //Default servlet
         //FIXME: org.eclipse.jetty.servlet.ServletHolder.initJspServlet() need it - InitParameter "com.sun.appserv.jsp.classpath"
         main.setClassLoader(ContextApp.class.getClassLoader());
         //JSP Servlet
         System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
         main.addServlet(JspServlet.class, "*.jsp");
         //Welcome files
         main.setWelcomeFiles(new String[]{"shell_index.jsp"});
 
         ServletContextHandler ctx = new ServletContextHandler(contexts, "/" + es.ctxPath, ServletContextHandler.SESSIONS);
         //FIXME: org.eclipse.jetty.servlet.ServletHolder.initJspServlet() need it - InitParameter "com.sun.appserv.jsp.classpath"
         ctx.setClassLoader(ContextApp.class.getClassLoader());
         //Allow find resource in multi-folder
         Resource res = new ResourceCollection(
         		buildFolderResource("jetty-starter/war-patch"),
         		//buildFolderResource("jetty-starter/war-base"),
         		buildFolderResource("OpenXavaEx/web"),
         		buildFolderResource("OpenXava/web"),
         		buildFolderResource(es.ctxPath + "/web"),
         		null
         );
         ctx.setBaseResource(res);
         
         //Default servlet
         ctx.addServlet(DefaultServlet.class, "/");
         //JSP servlet
         System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
         ctx.addServlet(JspServlet.class, "*.jsp");
         
         //OpenXava Servlets
         ctx.addServlet(ModuleServlet.class, "/modules/*");
         ctx.addServlet(ForceUtf8DwrServlet.class, "/dwr/*");
         ctx.addServlet(ImagesServlet.class, "/xava/ximage");
         ctx.addServlet(ImagesServlet.class, "/ximage");
         
         //Schema Update Servlet
         ServletHolder susSh = new ServletHolder(SchemaUpdateServlet.class);
         susSh.setInitParameter(SchemaUpdateServlet.PERSISTENCE_UNIT_LIST, "default");	//default;junit
 		ctx.addServlet(susSh, "/schema-update/*");
 		
 		//JsonViewer Servlet
 		ctx.addServlet(JsonViewerServlet.class, "/json");
 		
 		//SSO Integration Filter
 		FilterHolder ssoFh = new FilterHolder(TokenCookieSSOFilter.class);
 		ssoFh.setInitParameter(TokenCookieSSOFilter.TOKEN_CHECK_URL_INIT_PARAME,
 				"http://localhost:"+es.httpPort+"/main/bridge.jsp?token=");
         ctx.addFilter(ssoFh, "/modules/*", FilterMapping.REQUEST);
 		FilterHolder ssoFh2 = new FilterHolder(TokenCookieSSOFilter.class);
 		ssoFh2.setInitParameter(TokenCookieSSOFilter.TOKEN_CHECK_URL_INIT_PARAME, null);
         ctx.addFilter(ssoFh2, "*.jsp", FilterMapping.REQUEST);
         ctx.addFilter(ssoFh2, "/dwr/*", FilterMapping.REQUEST);
         
         //Dynamic class load filter, only for development
         FilterHolder clFh = new FilterHolder(DynamicLoaderFilter.class);
         clFh.setInitParameter(DynamicLoaderFilter.INIT_PARAM_NAME_CLASSPATH, getAppClassPathList(es.ctxPath));
         ctx.addFilter(clFh, "*.jsp", FilterMapping.REQUEST);
         ctx.addFilter(clFh, "/modules/*", FilterMapping.REQUEST);
         ctx.addFilter(clFh, "/dwr/*", FilterMapping.REQUEST);
         ctx.addFilter(clFh, "/schema-update/*", FilterMapping.REQUEST);
         
         ctx.setWelcomeFiles(new String[]{"index.jsp", es.ctxPath+".jsp"});
 
         server.start();
         //System.out.println(server.dump());
         System.out.println("********************************************************************************");
         System.out.println("Embeded Jetty("+Server.getVersion()+") Server started at port ["+es.httpPort+"].");
         System.out.println("********************************************************************************");
         server.join();
 	}
 
 	/**
 	 * Get the parent folder path of current project, based the directory structure
 	 * @return
 	 */
 	private static final String getProjectParent() {
 		URL binUrl = ContextApp.class.getResource("/");
         String bin = binUrl.getFile();
         String parent = (new File(bin)).getParent();
         parent = (new File(parent)).getParent();
 		return parent;
 	}
 	private static final String getAppClassPathList(String... appNames){
 		StringBuffer buf = new StringBuffer();
 		String parent = getProjectParent();
 		for(int i=0; i<appNames.length; i++){
 			String app = appNames[i];
 			if (i>0) buf.append(";");
 			//FIXME: Now you can only complie App's class into it's /web/WEB-INF/classes folder
 			buf.append(parent + "/" + app + "/web/WEB-INF/classes");
 		}
 		return buf.toString();
 	}
 	/**
 	 * Get the resource of specified web content folder
 	 * @param warFolderName
 	 * @return
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	private static final Resource buildFolderResource(String warFolderName) throws IOException, URISyntaxException {
         String parent = getProjectParent();
 		String war = parent + "/" + warFolderName;
         File f = new File(war);
         f = new File(f.getCanonicalPath());
         Resource r = new FileResource(f.toURI().toURL());
         return r;
 	}
 	
 	/**
 	 * ref: http://www.junlu.com/list/96/481920.html - setting up JNDI in embedded Jetty
 	 * @param server
 	 * @throws Exception
 	 */
 	private static void prepareDataSource(Server server, EnvSettings es) throws Exception {
 		Context envContext = null;
 		
 		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
 		Thread.currentThread().setContextClassLoader(server.getClass().getClassLoader());
 		try {
 			Context context = new InitialContext();
 			Context compContext = (Context) context.lookup ("java:comp");
 			envContext = compContext.createSubcontext("env");
 		} finally {
 			Thread.currentThread().setContextClassLoader(oldLoader);
 		}
 
 		if (null != envContext){
 			Properties p = new Properties();
 			p.put("driverClassName", es.getJdbcDriver());
 			p.put("url", es.jdbcUrl);
 			p.put("username", es.dbUser);
 			p.put("password", es.dbPass);
 			p.put("validationQuery", es.getValidationQuery());
 			DataSource ds = BasicDataSourceFactory.createDataSource(p);
 			
 			NamingUtil.bind(envContext, es.getJndiName(), ds);
 		}
 		
 		System.out.println(">>> DataSource ["+es.getJndiName()+"] created:");
 		System.out.println(">>> \t url: " + es.jdbcUrl);
 	}
 	
 	private static EnvSettings readEnv(){
 		EnvSettings es = new EnvSettings();
 		es.httpPort = Integer.valueOf(_readEnv("HTTP_PORT", DEFAULT_HTTP_PORT));
 		es.ctxPath = _readEnv("CTX_PATH", DEFAULT_CTX_PATH);
 		es.jdbcUrl = _readEnv("JDBC_URL", "Unknown_JDBC_URL");
 		es.dbUser = _readEnv("DB_USER", "Unknown_DB_USER");
 		es.dbPass = _readEnv("DB_PASS", "");
 		return es;
 	}
 	private static String _readEnv(String var, String defVal){
 		String v = System.getenv(var);
 		if (null==v){
 			v=defVal;
 		}
 		if (null!=v){
 			System.setProperty(var, v);		//Remember the real variable value into System Properties
 		}
 		return v;
 	}
 	/**
 	 * Store the settings defined by environment variables
 	 * @author root
 	 *
 	 */
 	private static class EnvSettings{
 		private int httpPort;
 		private String ctxPath;
 		private String jdbcUrl;
 		private String dbUser;
 		private String dbPass;
 		
 		private String _jdbcUrl(){
 			return (null==this.jdbcUrl)?"":this.jdbcUrl;
 		}
 		/** jdbc:oracle:thin:@localhost:1521:XE */
 		private boolean isOracle(){
 			return _jdbcUrl().startsWith("jdbc:oracle:thin:");
 		}
 		/** jdbc:sqlserver://localhost:1433;databaseName=orderMgr */
 		private boolean isMSSQL(){
 			return _jdbcUrl().startsWith("jdbc:sqlserver://");
 		}
 		/** jdbc:jtds:sqlserver://localhost:1433/orderMgr */
 		private boolean isMSSQL_JTDS(){
 			return _jdbcUrl().startsWith("jdbc:jtds:sqlserver://");
 		}
 		/** jdbc:mysql://localhost:3306/orderMgr?useUnicode=true&amp;characterEncoding=UTF-8 */
 		private boolean isMySQL(){
 			return _jdbcUrl().startsWith("jdbc:mysql://");
 		}
 		/** jdbc:hsqldb:hsql://localhost/TestHSQLDB */
 		private boolean isHSQL(){
 			return _jdbcUrl().startsWith("jdbc:hsqldb:");
 		}
 		
 		private String getJndiName(){
 			return "jdbc/"+this.ctxPath+"DS";
 		}
 		private String getDefaultSchema(){
 			if (isHSQL()){
 				return "PUBLIC";
 			}else{
 				return this.dbUser;
 			}
 		}
 		private String getJdbcDriver(){
 			if (isOracle()){
 				return "oracle.jdbc.driver.OracleDriver";
 			}else if (isMSSQL()){
 				return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
 			}else if (isMSSQL_JTDS()){
 				return "net.sourceforge.jtds.jdbc.Driver";
 			}else if (isMySQL()){
 				return "com.mysql.jdbc.Driver";
 			}else if (isHSQL()){
 				return "org.hsqldb.jdbcDriver";
 			}else{
 				throw new RuntimeException("Unknown database type ["+this.jdbcUrl+"]");
 			}
 		}
 		private String getValidationQuery(){
 			if (isOracle()){
 				return "SELECT 1 From dual";
 			}else if (isMSSQL()){
 				return "Select 1";
 			}else if (isMSSQL_JTDS()){
 				return "Select 1";
 			}else if (isMySQL()){
 				return "Select 1";
 			}else if (isHSQL()){
 				return "Select COUNT(*) As X From INFORMATION_SCHEMA.SYSTEM_USERS Where 1=0";
 			}else{
 				throw new RuntimeException("Unknown database type ["+this.jdbcUrl+"]");
 			}
 		}
 		private String getHibernateDialect(){
 			if (isOracle()){
 				return "org.hibernate.dialect.Oracle10gDialect";
 			}else if (isMSSQL()){
 				return "org.hibernate.dialect.SQLServer2005Dialect";
 			}else if (isMSSQL_JTDS()){
 				return "org.hibernate.dialect.SQLServer2005Dialect";
 			}else if (isMySQL()){
 				return "org.hibernate.dialect.MySQL5Dialect";
 			}else if (isHSQL()){
 				return "org.hibernate.dialect.HSQLDialect";
 			}else{
 				throw new RuntimeException("Unknown database type ["+this.jdbcUrl+"]");
 			}
 		}
 	}
 }
