 package starter;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSourceFactory;
 import org.apache.jasper.servlet.JspServlet;
 import org.directwebremoting.servlet.DwrServlet;
 import org.eclipse.jetty.jndi.NamingUtil;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.util.resource.FileResource;
 import org.eclipse.jetty.util.resource.Resource;
 import org.eclipse.jetty.util.resource.ResourceCollection;
 import org.hsqldb.persist.HsqlProperties;
 import org.openxava.web.servlets.ModuleServlet;
 
 public class ContextApp {
 	private static final int HTTP_PORT = 8080;
 	private static final String CTX_PATH = "/TestApp";		//In OpenXava, the context path is also the application name
 
 	public static void main(String[] args) throws Exception {
 		//Prepare system properties for logging
 		System.setProperty("org.eclipse.jetty.LEVEL", "ALL");
 		System.setProperty("org.eclipse.jetty.util.log.SOURCE", "false");
 		
         Server server = new Server(HTTP_PORT);
         
         //Init the hsql database and the connection pool
         startHsqlServer();
         prepareDataSource(server);
 
         ContextHandlerCollection contexts = new ContextHandlerCollection();
         server.setHandler(contexts);
 
         ServletContextHandler ctx = new ServletContextHandler(contexts, CTX_PATH, ServletContextHandler.SESSIONS);
         //FIXME: org.eclipse.jetty.servlet.ServletHolder.initJspServlet() need it - InitParameter "com.sun.appserv.jsp.classpath"
         ctx.setClassLoader(ContextApp.class.getClassLoader());
         //Allow find resource in multi-folder
         Resource res = new ResourceCollection(
         		buildFolderResource("war-base"), buildFolderResource("war-patch"),
         		buildFolderResource("../OpenXava/web"),
         		buildFolderResource("../OpenXava/xava")/*For taglib only*/
         );
         ctx.setBaseResource(res);
         
         //Default servlet
         ctx.addServlet(DefaultServlet.class, "/");
         //JSP servlet
         ctx.addServlet(JspServlet.class, "*.jsp");
         
         //OpenXava Servlets
         ctx.addServlet(ModuleServlet.class, "/modules/*");
         ctx.addServlet(DwrServlet.class, "/dwr/*");
 
         server.start();
         System.out.println(server.dump());
         server.join();
 	}
 
 	private static final Resource buildFolderResource(String folderName) throws IOException, URISyntaxException {
         //Detect the war's position
         URL binUrl = ContextApp.class.getResource("/");
         String bin = binUrl.getFile();
         String war = (new File(bin)).getParent() + "/" + folderName;
         File f = new File(war);
         f = new File(f.getCanonicalPath());
         Resource r = new FileResource(f.toURI().toURL());
         return r;
 	}
 	
 	private static void startHsqlServer(){
 	    HsqlProperties p = new HsqlProperties();
	    p.setProperty("server.database.0","file:../TestAppHsqlDB/data");
	    p.setProperty("server.dbname.0","TestAppDB");
 
 	    org.hsqldb.Server server = new org.hsqldb.Server();
 	    server.setProperties(p);
 	    server.setLogWriter(null); // can use custom writer
 	    server.setErrWriter(null); // can use custom writer
 	    server.start();
 	}
 	
 	/**
 	 * ref: http://www.junlu.com/list/96/481920.html - setting up JNDI in embedded Jetty
 	 * @param server
 	 * @throws Exception
 	 */
 	private static void prepareDataSource(Server server) throws Exception {
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
 			p.put("driverClassName", "org.hsqldb.jdbcDriver");
 			p.put("url", "jdbc:hsqldb:hsql://localhost/TestAppDB");
 			p.put("username", "SA");
 			p.put("password", "");
			p.put("validationQuery", "SELECT 1");
 			DataSource ds = BasicDataSourceFactory.createDataSource(p);
 			
 			NamingUtil.bind(envContext, "jdbc/TestAppDS", ds);
 		}
 	}
 }
