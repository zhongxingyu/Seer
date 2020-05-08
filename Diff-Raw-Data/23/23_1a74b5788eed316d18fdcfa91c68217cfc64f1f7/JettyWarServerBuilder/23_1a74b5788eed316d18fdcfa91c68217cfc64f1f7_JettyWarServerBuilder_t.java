 package com.nytimes.eps.war.container;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.eclipse.jetty.annotations.AnnotationConfiguration;
 import org.eclipse.jetty.nosql.mongodb.MongoSessionIdManager;
 import org.eclipse.jetty.nosql.mongodb.MongoSessionManager;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.session.SessionHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.webapp.Configuration;
 import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.eclipse.jetty.webapp.WebXmlConfiguration;
 import org.springframework.security.web.session.HttpSessionEventPublisher;
 import org.springframework.web.context.ContextLoaderListener;
 
 import com.mongodb.DB;
 import com.mongodb.MongoException;
 import com.mongodb.MongoURI;
 import com.nytimes.eps.war.container.filter.RestFilter;
 import com.nytimes.eps.war.container.filter.RestServerFilterBuilder;
 import com.nytimes.eps.war.container.servlet.RestServerServletBuilder;
 import com.nytimes.eps.war.container.servlet.RestServlet;
 
 import static com.nytimes.eps.common.Utils.safe;
 
 /**
  * Builder for a jetty server. Allows a user to add servlets, filters,
  * eventListeners using java code, web.xml or a combiniation of both.
  * 
  * @author mikep
  * 
  */
 public class JettyWarServerBuilder {
 
 	private final static String SESSION_COLLECTION = "session";
 
 	private int port = 8080;
 	private String webXml = "./web.xml";
 	private String resourceBase = "./";
 	private String contextPath = "/";
 	private final Map<String, String> contextParams = new HashMap<String, String>();
 	private List<EventListener> listeners;
 	private List<RestFilter> filters;
 	private List<RestServlet> servlets;
 	private MongoURI mongoUri;
 
 	public JettyWarServerBuilder port(int port) {
 		this.port = port;
 		return this;
 	}
 
 	/**
 	 * Location of the web.xml file
 	 * 
 	 * @param webXml
 	 *            - default is "./web.xml"
 	 * @return JettyWarServerBuilder
 	 */
 	public JettyWarServerBuilder descriptor(final String webXml) {
 		this.webXml = webXml;
 		return this;
 	}
 
 	/**
 	 * Allows the resource base to be configured.
 	 * 
 	 * @param resourceBase
 	 *            - default is "./"
 	 * @return JettyWarServerBuilder
 	 */
 	public JettyWarServerBuilder resourceBase(final String resourceBase) {
 		this.resourceBase = resourceBase;
 		return this;
 	}
 
 	/**
 	 * Allows the context path to be configured.
 	 * 
 	 * @param contextPath
 	 *            - default is "/"
 	 * @return JettyWarServerBuilder
 	 */
 	public JettyWarServerBuilder contextPath(final String contextPath) {
 		this.contextPath = contextPath;
 		return this;
 	}
 
 	/**
 	 * Allows context-params to be configured for the container
 	 * 
 	 * @param name
 	 * @param value
 	 * @return JettyWarServerBuilder
 	 */
 	public JettyWarServerBuilder contextParam(final String name,
 			final String value) {
 		this.contextParams.put(name, value);
 		return this;
 	}
 
 	/**
 	 * Allows EventListeners to be configured in the container.
 	 * 
 	 * @param listeners
 	 * @return JettyWarServerBuilder
 	 */
 	public JettyWarServerBuilder eventListeners(
 			final EventListener... listeners) {
 		this.listeners = new ArrayList<EventListener>(Arrays.asList(listeners));
 		return this;
 	}
 
 	public JettyWarServerBuilder filters(final RestFilter... filters) {
 		this.filters = new ArrayList<RestFilter>(Arrays.asList(filters));
 		return this;
 	}
 
 	public JettyWarServerBuilder servlets(final RestServlet... servlets) {
 		this.servlets = new ArrayList<RestServlet>(Arrays.asList(servlets));
 		return this;
 	}
 
 	public JettyWarServerBuilder mongoSessionSupport(MongoURI mongoUri) {
 		this.mongoUri = mongoUri;
 		return this;
 	}
 
 	private void addSessionSupport(Server server, WebAppContext context) {
 		if (mongoUri != null) {
 			try {
 				DB connectedDb = mongoUri.connectDB();
 				if (mongoUri.getUsername() != null) {
 					connectedDb.authenticate(mongoUri.getUsername(),
 							mongoUri.getPassword());
 				}
 				MongoSessionIdManager idMgr = new MongoSessionIdManager(server,
 						connectedDb.getCollection(SESSION_COLLECTION));
 				Random rand = new Random((new Date()).getTime());
 				int workerNum = 1000 + rand.nextInt(8999);
 				idMgr.setWorkerName(String.valueOf(workerNum));
 				server.setSessionIdManager(idMgr);
 				MongoSessionManager mongoMgr = new MongoSessionManager();
 				mongoMgr.setSessionIdManager(server.getSessionIdManager());
 				SessionHandler sessionHandler = new SessionHandler();
 				sessionHandler.setSessionManager(mongoMgr);
 				context.setSessionHandler(sessionHandler);
 			} catch (MongoException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public Server build() {
 		final Server server = new Server(this.port);
 		final WebAppContext root = new WebAppContext();
 		root.setContextPath(this.contextPath);
		root.setParentLoaderPriority(true);
 		root.setResourceBase(this.resourceBase);
 		final Configuration[] configuration = new Configuration[] {
 				new AnnotationConfiguration(), new WebXmlConfiguration(),
 				new JettyWebXmlConfiguration() };
 		root.setConfigurations(configuration);
 		for (String name : safe(this.contextParams.keySet())) {
 			root.setInitParameter(name, this.contextParams.get(name));
 		}
 		for (EventListener listener : safe(this.listeners)) {
 			root.addEventListener(listener);
 		}
 		for (RestFilter filter : this.filters) {
 			root.addFilter(filter.getFilterHolder(), filter.getPathSpec(),
 					filter.getDispatches());
 		}
 		for (RestServlet servlet : this.servlets) {
 			root.addServlet(servlet.getServletHolder(), servlet.getPathSpec());
 		}
 
 		addSessionSupport(server, root);
 		// root.setInitParameter("webAppRootKey", "tutorial.root");
 		// root.setInitParameter("contextConfigLocation",
 		// "classpath:applicationContext-business.xml classpath:applicationContext-security.xml");
 		// root.addEventListener(new ContextLoaderListener());
 		// root.addEventListener(new HttpSessionEventPublisher());
 		// FilterHolder filterHolder = new
 		// FilterHolder(org.springframework.web.filter.DelegatingFilterProxy.class);
 		// filterHolder.setName("springSecurityFilterChain");
 		// EnumSet<DispatcherType> all = EnumSet.of(DispatcherType.ASYNC,
 		// DispatcherType.ERROR, DispatcherType.FORWARD,
 		// DispatcherType.INCLUDE, DispatcherType.REQUEST);
 		// root.addFilter(filterHolder, "/*", all);
 		// ServletHolder servletHolder = new
 		// ServletHolder(org.springframework.web.servlet.DispatcherServlet.class);
 		// servletHolder.setName("bank");
 		// servletHolder.setInitParameter("contextClass",
 		// "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
 		// servletHolder.setInitParameter("contextConfigLocation",
 		// "com.nytimes.eps.war.container.config.AppConfig");
 		// root.addServlet(servletHolder, "*.html");
 		root.setDescriptor(this.webXml);
 		root.setParentLoaderPriority(true);
 		server.setHandler(root);
 		return server;
 	}
 
 	public static void main(String... args) throws Exception {
 		Server server = new JettyWarServerBuilder()
 				.contextParam("webAppRootKey", "tutorial.root")
 				.contextParam(
 						"contextConfigLocation",
 						"classpath:applicationContext-business.xml classpath:applicationContext-security.xml")
 				.eventListeners(new ContextLoaderListener(),
 						new HttpSessionEventPublisher())
 				.filters(
 						new RestServerFilterBuilder()
 								.filter(org.springframework.web.filter.DelegatingFilterProxy.class)
 								.name("springSecurityFilterChain")
 								.pathSpec("/*").build())
 				.servlets(
 						new RestServerServletBuilder()
 								.servlet(
 										org.springframework.web.servlet.DispatcherServlet.class)
 								.initParams("contextClass",
 										"org.springframework.web.context.support.AnnotationConfigWebApplicationContext")
 								.initParams("contextConfigLocation",
 										"com.nytimes.eps.war.container.config.AppConfig")
 								.name("bank").pathSpec("*.html").build())
 				.build();
 
 		server.start();
 		server.join();
 	}
 }
