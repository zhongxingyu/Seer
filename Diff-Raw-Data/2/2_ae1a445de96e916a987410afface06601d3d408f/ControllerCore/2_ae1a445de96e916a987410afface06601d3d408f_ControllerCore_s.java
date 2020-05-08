 /*
  * ControllerCore.java
  *
  * Copyright 2013 Patrick Mairif.
  * The program is distributed under the terms of the Apache License (ALv2).
  * 
  * tabstop=4, charset=UTF-8
  */
 package de.highbyte_le.weberknecht;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.naming.NamingException;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.highbyte_le.weberknecht.conf.ActionDeclaration;
 import de.highbyte_le.weberknecht.conf.ConfigurationException;
 import de.highbyte_le.weberknecht.conf.ProcessorList;
 import de.highbyte_le.weberknecht.conf.WeberknechtConf;
 import de.highbyte_le.weberknecht.db.DBConnectionException;
 import de.highbyte_le.weberknecht.db.DbConnectionHolder;
 import de.highbyte_le.weberknecht.db.DbConnectionProvider;
 import de.highbyte_le.weberknecht.db.DefaultWebDbConnectionProvider2;
 import de.highbyte_le.weberknecht.request.Configurable;
 import de.highbyte_le.weberknecht.request.DatabaseCapable;
 import de.highbyte_le.weberknecht.request.ModelHelper;
 import de.highbyte_le.weberknecht.request.actions.ActionNotFoundException;
 import de.highbyte_le.weberknecht.request.actions.ExecutableAction;
 import de.highbyte_le.weberknecht.request.error.DefaultErrorHandler;
 import de.highbyte_le.weberknecht.request.error.ErrorHandler;
 import de.highbyte_le.weberknecht.request.processing.ActionExecution;
 import de.highbyte_le.weberknecht.request.processing.ProcessingChain;
 import de.highbyte_le.weberknecht.request.processing.Processor;
 import de.highbyte_le.weberknecht.request.processing.RedirectException;
 import de.highbyte_le.weberknecht.request.routing.AreaCapableRouter;
 import de.highbyte_le.weberknecht.request.routing.AreaPathResolver;
 import de.highbyte_le.weberknecht.request.routing.MetaRouter;
 import de.highbyte_le.weberknecht.request.routing.Router;
 import de.highbyte_le.weberknecht.request.routing.RoutingTarget;
 import de.highbyte_le.weberknecht.request.view.ActionViewProcessor;
 import de.highbyte_le.weberknecht.request.view.ActionViewProcessorFactory;
 import de.highbyte_le.weberknecht.request.view.AutoViewProcessor;
 
 /**
 * webapp controller to be used in servlet ({@link Controller}) or filter ( {@ControllerFilter} )
  * 
  * @author pmairif
  */
 @SuppressWarnings({ "nls" })
 public class ControllerCore {
 	
 	private DbConnectionProvider dbConnectionProvider = null;
 	
 	private AreaPathResolver pathResolver;
 	
 	private ActionViewProcessorFactory actionProcessorFactory = null;
 	
 	private WeberknechtConf conf;
 	
 	private ServletContext servletContext;
 	
 	private ServletConfig servletConfig = null;
 	
 	private FilterConfig filterConfig = null;
 	
 	/**
 	 * Logger for this class
 	 */
 	private final static Log log = LogFactory.getLog(ControllerCore.class);
 
 	public ControllerCore(ServletContext servletContext) throws ClassNotFoundException, ConfigurationException {
 		this(servletContext, WeberknechtConf.readConfig(servletContext), initDbConnectionProvider());
 	}
 	
 	public ControllerCore(ServletContext servletContext, WeberknechtConf conf, DbConnectionProvider dbConnectionProvider) throws ClassNotFoundException, ConfigurationException {
 		this.servletContext = servletContext;
 		this.conf = conf;
 		
 		//actions
 		this.pathResolver = new AreaPathResolver(conf);
 		
 		//action processors
 		actionProcessorFactory = new ActionViewProcessorFactory();
 		//register action processors from config
 		for (Entry<String, String> e: conf.getActionProcessorSuffixMap().entrySet()) {
 			actionProcessorFactory.registerProcessor(e.getKey(), e.getValue());
 		}
 		
 		this.dbConnectionProvider = dbConnectionProvider;
 	}
 
 	private static DbConnectionProvider initDbConnectionProvider() {
 		DbConnectionProvider dbConnectionProvider = null;
 		try {
 			dbConnectionProvider = new DefaultWebDbConnectionProvider2("jdbc/mydb");
 		}
 		catch (NamingException e) {
 			if (log.isInfoEnabled())
 				log.info("jdbc/mydb not configured ("+e.getMessage()+")");	//$NON-NLS-1$
 		}
 		return dbConnectionProvider;
 	}
 	
 	/**
 	 * @return the dbConnectionProvider
 	 */
 	public DbConnectionProvider getDbConnectionProvider() {
 		return dbConnectionProvider;
 	}
 	
 	/**
 	 * @param servletConfig the servletConfig to set
 	 */
 	public void setServletConfig(ServletConfig servletConfig) {
 		this.servletConfig = servletConfig;
 	}
 	
 	/**
 	 * @param filterConfig the filterConfig to set
 	 */
 	public void setFilterConfig(FilterConfig filterConfig) {
 		this.filterConfig = filterConfig;
 	}
 	
 	public Router createRouter(DbConnectionHolder conHolder) throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException, DBConnectionException, ConfigurationException {
 		
 		List<String> routerClasses = conf.getRouterClasses();
 		List<Router> routers = new Vector<Router>(routerClasses.size());
 		for (String routerClass: routerClasses) {
 			Object o = Class.forName(routerClass).newInstance();
 			if (!(o instanceof Router))
 				throw new ConfigurationException(routerClass + " is not an instance of Router");
 				
 			Router router = (Router) o;
 			initializeObject(router, conHolder);
 			routers.add(router);
 		}
 		
 		Router ret = null;
 		int size = routers.size();
 		if (size == 0)
 			ret = new AreaCapableRouter();
 		else if (size == 1)
 			ret = routers.get(0);
 		else
 			ret = new MetaRouter(routers);
 
 		ret.setConfig(conf, pathResolver);
 		
 		return ret;
 	}
 	
 	/**
 	 * create instances of processor list
 	 * 
 	 * @param processorList
 	 * @return list of instantiated processors
 	 */
 	private List<Processor> instantiateProcessorList(ProcessorList processorList)
 			throws InstantiationException, IllegalAccessException {
 		
 		List<Class<? extends Processor>> processorClasses = processorList.getProcessorClasses();
 		List<Processor> processors = new Vector<Processor>(processorClasses.size());
 		
 		for (Class<? extends Processor> pp: processorClasses) {
 			processors.add(pp.newInstance());
 		}
 		
 		return processors;
 	}
 	
 	public void executeAction(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
 			DbConnectionHolder conHolder, RoutingTarget routingTarget) throws ActionNotFoundException {
 
 		if (null == routingTarget)
 			throw new ActionNotFoundException();
 
 		try {
 
 			ModelHelper modelHelper = new ModelHelper(httpRequest, servletContext);
 			modelHelper.setSelf(httpRequest);
 			
 			ExecutableAction action = pathResolver.resolveAction(routingTarget);
 			if (log.isDebugEnabled())
 				log.debug("executeAction() - processing action "+action.getClass().getSimpleName());
 
 			List<Processor> processors = setupProcessors(routingTarget);
 			
 			//initialization
 			for (Processor p: processors)
 				initializeObject(p, conHolder);
 			
 			initializeObject(action, conHolder);
 
 			//processing
 			try {
 				ProcessingChain chain = new ProcessingChain(processors, httpRequest, httpResponse, routingTarget, action);
 				chain.doContinue();
 				
 				//process view
 				//TODO implement as processor
 				ActionViewProcessor processor = actionProcessorFactory.createActionProcessor(routingTarget.getViewProcessorName(), servletContext); 
 				processor.processView(httpRequest, httpResponse, action);
 			}
 			catch (RedirectException e) {
 				doRedirect(httpRequest, httpResponse, e.getLocalRedirectDestination());
 			}
 		}
 		catch (ActionNotFoundException e) {
 			throw e;
 		}
 		catch (Exception e) {
 			handleException(httpRequest, httpResponse, routingTarget, e);
 		}
 	}
 	
 	protected List<Processor> setupProcessors(RoutingTarget routingTarget) throws InstantiationException, IllegalAccessException {
 		List<Processor> processors = new Vector<Processor>();
 
 		ActionDeclaration actionDeclaration = pathResolver.getActionDeclaration(routingTarget);
 		
 		//pre processors
 		if (actionDeclaration != null) {
 			ProcessorList processorList = conf.getPreProcessorListMap().get(actionDeclaration.getPreProcessorSet());
 			if (processorList != null)
 				processors.addAll(instantiateProcessorList(processorList));
 		}
 		
 		processors.add(new ActionExecution());
 		
 		//post processors
 		if (actionDeclaration != null) {
 			ProcessorList processorList = conf.getPostProcessorListMap().get(actionDeclaration.getPostProcessorSet());
 			if (processorList != null)
 				processors.addAll(instantiateProcessorList(processorList));
 		}
 
 		return processors;
 	}
 	
 	/**
 	 * do initialization stuff here.
 	 * 
 	 * @param action	the action instance, processor or whatever to be initialized
 	 * @param conHolder		holds database connection
 	 */
 	protected void initializeObject(Object action, DbConnectionHolder conHolder) throws DBConnectionException, ConfigurationException {
 		log.debug("initializeAction()");
 		
 		if (action instanceof DatabaseCapable) {
 			log.debug("setting action database");
 			((DatabaseCapable)action).setDatabase(conHolder.getConnection());
 		}
 
 		if (action instanceof Configurable)
 			((Configurable)action).setContext(servletConfig, filterConfig, servletContext);
 	}
 
 	private void doRedirect(HttpServletRequest request, HttpServletResponse response, String redirectDestination)
 			throws MalformedURLException {
 		URL reqURL = new URL(request.getRequestURL().toString());
 		URL dest = new URL(reqURL, redirectDestination);
 		response.setHeader("Location", dest.toExternalForm());
 		response.setStatus(303);	//303 - "see other" (http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html)
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void handleException(HttpServletRequest request, HttpServletResponse response,
 			RoutingTarget routingTarget, Exception exception) {
 		DbConnectionHolder dbConHolder = new DbConnectionHolder(dbConnectionProvider); 
 		try {
 			//get error handler
 			Class<? extends ErrorHandler> errHandlerClass = DefaultErrorHandler.class;
 			ActionDeclaration actionDeclaration = pathResolver.getActionDeclaration(routingTarget);
 			if (actionDeclaration != null && actionDeclaration.hasErrorHandlerClass())
 				errHandlerClass = (Class<? extends ErrorHandler>) Class.forName(actionDeclaration.getErrorHandlerClass());
 			
 			ErrorHandler handler = errHandlerClass.newInstance();
 			
 			//initialize error handler
 			initializeObject(handler, dbConHolder);
 
 			//handle exception
 			handler.handleException(exception, request, routingTarget);
 			
 			//process view, respecting requested content type
 			AutoViewProcessor processor = new AutoViewProcessor();
 			processor.setServletContext(servletContext);
 			processor.setActionViewProcessorFactory(actionProcessorFactory);
 			boolean view = processor.processView(request, response, handler);
 
 			//status
 			int status = handler.getStatus();
 			if (status > 0)	{//Don't set status, eg. on redirects
 				if (view)
 					response.setStatus(status);
 				else
 					response.sendError(status);
 			}
 
 		}
 		catch (Exception e1) {
 			try {
 				log.error("handleException() - exception while error handler instantiation: "+e1.getMessage(), e1);	//$NON-NLS-1$
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	//call error page 500
 			}
 			catch (IOException e) {
 				log.error("handleException() - IOException: "+e.getMessage(), e);	//$NON-NLS-1$
 				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	//just return 500
 			}
 		}
 		finally {
 			try {
 				dbConHolder.close();
 			}
 			catch (SQLException e) {
 				log.error("SQLException while closing db connection: "+e.getMessage());	//$NON-NLS-1$
 			}
 		}
 	}
 }
