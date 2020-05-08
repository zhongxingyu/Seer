 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import br.octahedron.cotopaxi.config.CotopaxiConfig;
 import br.octahedron.cotopaxi.config.CotopaxiConfigView;
 import br.octahedron.cotopaxi.config.CotopaxiConfigViewImpl;
 import br.octahedron.cotopaxi.config.CotopaxiConfigurator;
 import br.octahedron.cotopaxi.controller.AuthorizationException;
 import br.octahedron.cotopaxi.controller.ControllerManager;
 import br.octahedron.cotopaxi.metadata.MetadataHandler;
 import br.octahedron.cotopaxi.metadata.MetatadaMapper;
 import br.octahedron.cotopaxi.metadata.PageNotFoundExeption;
 import br.octahedron.cotopaxi.model.SuccessActionResponse;
 import br.octahedron.cotopaxi.view.ViewResponse;
 import br.octahedron.cotopaxi.view.ViewResponseBuilder;
 import br.octahedron.cotopaxi.view.i18n.LocaleManager;
 import br.octahedron.util.ThreadProperties;
 import br.octahedron.util.reflect.ReflectionUtil;
 
 /**
  * The Servlet is the entry point for the requests/responses. It's responsible by route the request,
  * and pass the request and the ModelMetadata to the controller manager and deliver the
  * ModelResponse to the ViewManager.
  * 
  * @see ControllerManager
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  * 
  */
 public class CotopaxiServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8958499809792016589L;
 
 	/**
 	 * Property used to define the CotopaxiConfigurator to be used by application. This property
 	 * must be defined at "web.xml" file.
 	 */
 	private static final String MODEL_CONFIGURATOR_PROPERTY = "CONFIGURATOR";
 	/**
 	 * Property used to define the facades (separated by commas) to used. This property must be
 	 * defined at "web.xml" file.
 	 */
 	private static final String MODEL_FACADES_PROPERTY = "MODELFACADES";
 
 	// private static final Class<? extends Formatter> DEFAULT_HTML_FORMATTER =
 	// VelocityFormatter.class;
 	// private static final Class<? extends Formatter> DEFAULT_JSON_FORMATTER = JSONFormatter.class;
 
 	private static final Logger logger = Logger.getLogger(CotopaxiServlet.class.getName());
 	private transient ControllerManager controller;
 	private transient ViewResponseBuilder view;
 	private transient MetatadaMapper mapper;
 	private CotopaxiConfigViewImpl config;
 
 	private LocaleManager localeManager;
 	
 
 	@Override
 	public void init(ServletConfig servletConfig) throws ServletException {
 		try {
 			logger.info("Configuring Cotopaxi [1/4]...");
 			// Cotopaxi configuration
 			this.configure(servletConfig.getInitParameter(MODEL_CONFIGURATOR_PROPERTY));
 			// creating mapper
 			logger.info("Creating Mapper and loading Model Metadata [2/4]...");
 			String[] modelFacadesClassesNames = servletConfig.getInitParameter(MODEL_FACADES_PROPERTY).split(",");
 			this.mapper = new MetatadaMapper(modelFacadesClassesNames);
 			// create the ControllerManager
 			logger.info("Creating Controller Manager [3/4]");
 			this.controller = new ControllerManager(this.config);
 			// create the ViewerManager
 			logger.info("Creating View Manager [4/4]");
 			this.view = new ViewResponseBuilder(this.config);
 			this.localeManager = LocaleManager.getInstance();
 			// Done!
 			logger.info("Cotopaxi is ready to serve!");
 		} catch (ClassNotFoundException e) {
 			throw new ServletException("The class " + e.getMessage()
 					+ " was not found. You should use the full class name (including the package). Eg.: java.lang.String");
 		} catch (InstantiationException e) {
 			throw new ServletException(
 					"Unable to instantiate the Configurator. The configurator should have a Constructor method without parameters.");
 		} catch (NoSuchMethodException e) {
 			throw new ServletException(
 					"Some of the given facades doesn't have a valid Constructor. All facades should have a Constructor method without parameters.");
 		} catch (Exception e) {
 			throw new ServletException("Error creating ModelFacade: " + e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Used by tests. Create and calls the {@link CotopaxiConfigurator}
 	 */
 	protected void configure(String configuratorClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 		this.config = (CotopaxiConfigViewImpl) CotopaxiConfigView.Handler.getConfigView();
 		if (configuratorClassName != null) {
 			CotopaxiConfig configurable = this.config.getControllerConfig();
 			CotopaxiConfigurator configurator = (CotopaxiConfigurator) ReflectionUtil.createInstance(configuratorClassName);
 			configurator.configure(configurable);
 		}
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		this.deliver(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		this.deliver(req, resp);
 	}
 
 	/**
 	 * Dispatches a request/response.
 	 * 
 	 * <pre>
 	 * 	Check if the requested URL is a redirect
 	 * 		If is, redirect;
 	 * 	Finds the {@link MetadataHandler}
 	 * 		If not found, call the {@link ViewResponseBuilder} to show 404 page
 	 * 	Call the {@link ControllerManager} to execute the controller
 	 * 	Deliver the {@link SuccessActionResponse} to the {@link ViewResponseBuilder}
 	 * </pre>
 	 */
 	private void deliver(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		ViewResponse viewResponse;
 		RequestWrapper request = new RequestWrapper(req);
 		String requestedURL = request.getURL();
 
 		if (this.config.isRedirect(requestedURL)) {
 			// request is a redirect, send response!
 			logger.fine("Redirecting request for " + requestedURL);
 			viewResponse = this.view.getViewResponse(requestedURL);
 		} else {
 			this.prepareThreadProperties(req);
 			Locale lc = this.localeManager.getLocale(request); 
 			try {
 				MetadataHandler metadata = this.mapper.getMapping(request);
 				viewResponse =  this.view.getViewResponse(lc, request.getFormat(), this.controller.process(request, metadata), metadata);
 			} catch (AuthorizationException e) {
 				logger.fine("Request for " + requestedURL + " failed due authorization restrictions! Redirecting to " + e.getRedirectURL());
 				viewResponse = this.view.getViewResponse(lc, e);
 			} catch (PageNotFoundExeption pnfex) {
 				logger.fine("Page Not Found! Requested url was " + requestedURL);
 				viewResponse = this.view.getViewResponse(lc, pnfex);
 			} catch (Exception ex) {
 				logger.log(Level.WARNING, ex.getMessage(), ex);
 				viewResponse = this.view.getViewResponse(lc, ex);
 			} finally {
 				this.finalizeThreadProperties(req);
 			}
 		}
 		
 		// dispatch the view response
 		try {
 			ResponseWrapper response = new ResponseWrapper(resp);
 			viewResponse.dispatch(response);
		} catch (IOException ioex) {
			logger.log(Level.SEVERE, "Can't dispatch response due an IOException: " + ioex.getMessage(), ioex);
 		}
 	}
 
 	/**
 	 * Take the sessions attributes and put them at the {@link ThreadProperties}
 	 */
 	@SuppressWarnings("unchecked")
 	private void prepareThreadProperties(HttpServletRequest request) {
 		// Remove previous properties
 		ThreadProperties.clear();
 		// Get atts from session and put on ThreadProperties
 		HttpSession session = request.getSession(false);
 		if (session != null) {
 			Enumeration<String> atts = session.getAttributeNames();
 			while (atts.hasMoreElements()) {
 				String att = atts.nextElement();
 				ThreadProperties.setProperty(att, session.getAttribute(att));
 			}
 		}
 	}
 
 	/**
 	 * Take the {@link ThreadProperties} attributes and put them at the Session (or remove from
 	 * session)
 	 */
 	private void finalizeThreadProperties(HttpServletRequest request) {
 		// Get atts from ThreadProperties and put on Session
 		HttpSession session = request.getSession(false);
 		if (!ThreadProperties.isEmpty()) {
 			for (String key : ThreadProperties.keys()) {
 				Object value = ThreadProperties.getProperty(key);
 				if (value == null && session != null) {
 					session.removeAttribute(key);
 				} else {
 					if (session == null) {
 						session = request.getSession(true);
 					}
 					session.setAttribute(key, value);
 				}
 			}
 		}
 	}
 }
