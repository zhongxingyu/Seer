 package ch.epfl.bbcf.gdv.config;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Request;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.Response;
 import org.apache.wicket.authentication.AuthenticatedWebApplication;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebSession;
 
 import ch.epfl.bbcf.gdv.access.database.Connect;
 import ch.epfl.bbcf.gdv.config.utility.FileResource;
 import ch.epfl.bbcf.gdv.html.HomePage;
 import ch.epfl.bbcf.gdv.html.LoginPage;
 import ch.epfl.bbcf.gdv.utility.thread.ManagerService;
 
 /**
  * Application object for your web application. If you want to run this application without deploying, run the Start class.
  * 
  * @see ch.epfl.Start#main(String[])
  */
 public class Application extends AuthenticatedWebApplication
 {    
 
 
 	//private static Map<String, Logger> logs = null;
 	private static Logger theLogger;// = Logger.getLogger(Application.class);
 	private static Map<Integer,Logger> loggers;
 	
 
 	/**
 	 * Constructor
 	 */
 	public Application()
 	{
 	}
 	@Override
 	public RequestCycle newRequestCycle(Request request, Response response) {
 		return super.newRequestCycle(request, response);
 	}
 	protected void init() {
 		super.init();
 		ServletContext ctx = this.getServletContext();
 		String catalina_home = System.getenv("CATALINA_HOME");
 		String ctxPath = ctx.getContextPath();

 		String metaInf = catalina_home+"/webapps"+ctxPath+"/META-INF";
 		File confFile = new File(metaInf+"/gdv.yaml");
 		if(!confFile.exists()){
 			fatal("configuration file not exist - APPLICATION WILL NOT WORK");
 		}
 		
 		theLogger = Logs.init(metaInf);
 		if(!Configuration.init(catalina_home,metaInf,confFile)){
 			fatal("configuration not initialized properly - APPLICATION WILL NOT WORK");
 		}
 		Configuration.addRessourcesLocations(getResourceSettings());
 		loggers = new HashMap<Integer,Logger>();
 		
 		
 		Configuration.addURLMounting(this);
 		Configuration.authorirization(this);
 		Configuration.setErrorPages(this);
 		getSharedResources().add("fileResource", new FileResource());
 		
 		info("--- starting application --- \t" +
 				new Date()+" --- " );
 	}
 
 	public static void destruct(){
 		Connect.removeAllConnection();
 		info("destroying thread manager...");
 		ManagerService.destruct();
 	}
 	/**
 	 * @see org.apache.wicket.Application#getHomePage()
 	 */
 	public Class<HomePage> getHomePage()
 	{
 		return HomePage.class;
 	}
 
 	public WebSession newSession(Request request, Response response)
 	{	
 		return new UserSession(request);
 	}
 
 	@Override
 	protected Class<? extends WebPage> getSignInPageClass() {
 		return LoginPage.class;
 	}
 
 	@Override
 	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
 		return UserSession.class;
 	}
 	
 	public static void debug(Object message){
 		theLogger.debug(new Date()+" : "+message);
 	}
 	public static void info(Object message){
 		theLogger.info(new Date()+" : "+message);
 	}
 	public static void error(Object message){
 		theLogger.info(new Date()+" : "+message);
 	}
 	public static void warn(Object message){
 		theLogger.warn(new Date()+" : "+message);
 	}
 	public static void fatal(Object message){
 		theLogger.warn(new Date()+" : "+message);
 	}
 	
 	
 	public static void debug(Object message,int userId){
 		if(null==loggers.get(userId)){
 			loggers.put(userId,Logs.initUserLogger(userId));
 		}
 		loggers.get(userId).debug(new Date()+" : "+message);
 	}
 	
 	public static void info(Object message,int userId){
 		if(null==loggers.get(userId)){
 			loggers.put(userId,Logs.initUserLogger(userId));
 		}
 		loggers.get(userId).info(new Date()+" : "+message);
 	}
 	
 	public static void warn(Object message,int userId){
 		if(null==loggers.get(userId)){
 			loggers.put(userId,Logs.initUserLogger(userId));
 		}
 		loggers.get(userId).warn(new Date()+" : "+message);
 	}
 	
 	public static void fatal(Object message,int userId){
 		if(null==loggers.get(userId)){
 			loggers.put(userId,Logs.initUserLogger(userId));
 		}
 		loggers.get(userId).fatal(message);
 	}
 	
 	
 	public static void error(Object message,int userId){
 		if(null==loggers.get(userId)){
 			loggers.put(userId,Logs.initUserLogger(userId));
 		}
 		loggers.get(userId).error(new Date()+" : "+message);
 	}
 	
 	public static void removeLogger(int userId) {
 		if(userId!=0){
 			loggers.remove(userId);
 		}
 		
 	}
 	
 	
 	
 	
 	
 }
