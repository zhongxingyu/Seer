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
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.ERROR_PROPERTY;
 import static br.octahedron.cotopaxi.CotopaxiProperty.ERROR_TEMPLATE;
 import static br.octahedron.cotopaxi.CotopaxiProperty.RUNNING_MODE_PROP;
 import static br.octahedron.cotopaxi.CotopaxiProperty.TEMPLATE_RENDER;
 import static br.octahedron.cotopaxi.CotopaxiProperty.property;
 import static br.octahedron.cotopaxi.CotopaxiProperty.runningMode;
 import static br.octahedron.cotopaxi.config.ConfigurationLoader.CONFIGURATION_FILENAME;
 import static br.octahedron.cotopaxi.inject.DependencyManager.registerDependency;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.exception.VelocityException;
 
 import br.octahedron.cotopaxi.Bootloader.Booter;
 import br.octahedron.cotopaxi.CotopaxiProperty.RunningMode;
 import br.octahedron.cotopaxi.config.ConfigurationLoader;
 import br.octahedron.cotopaxi.config.ConfigurationSyntaxException;
 import br.octahedron.cotopaxi.controller.ControllerDescriptor;
 import br.octahedron.cotopaxi.controller.ControllerExecutor;
 import br.octahedron.cotopaxi.controller.ControllerResponse;
 import br.octahedron.cotopaxi.inject.DependencyManager;
 import br.octahedron.cotopaxi.interceptor.ControllerInterceptor;
 import br.octahedron.cotopaxi.interceptor.InterceptorManager;
 import br.octahedron.cotopaxi.interceptor.TemplateInterceptor;
 import br.octahedron.cotopaxi.route.NotFoundExeption;
 import br.octahedron.cotopaxi.route.Router;
 import br.octahedron.cotopaxi.view.render.TemplateRender;
 import br.octahedron.cotopaxi.view.response.TemplateResponse;
 import br.octahedron.util.Log;
 /**
  * The Cotopaxi Framework entry point.
  * 
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class CotopaxiServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8958499809792016589L;
 	private static final Log log = new Log(CotopaxiServlet.class);
 	
 	protected static void setRunningMode(RunningMode mode) {
 		System.setProperty(RUNNING_MODE_PROP, mode.name());
 	}
 	
 	protected InterceptorManager interceptor;
 	private Router router;
 	private ControllerExecutor executor;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.GenericServlet#init()
 	 */
 	@Override
 	public void init() throws ServletException {
 		try {
 			this.router = new Router();
 			this.interceptor = new InterceptorManager();
 			this.executor = new ControllerExecutor(this.interceptor);
 			this.forceReload();
 			log.info("Running Mode: %s", runningMode());
 			log.info("Cotopaxi is ready to serve...");
 		} catch (FileNotFoundException ex) {
 			log.error("Configuration file not found. Make sure the %s file exists", CONFIGURATION_FILENAME);
 			throw new ServletException(ex);
 		} catch (ConfigurationSyntaxException ex) {
 			log.error("Error parsing configuration file: Invalid Syntax. Check you configuration file and try again");
 			throw new ServletException(ex);
 		} catch (Exception ex) {
 			log.error(ex, "Unexpected error loading cotopaxi: %s", ex.getMessage());
 			throw new ServletException(ex);
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
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		this.deliver(req, resp);
 	}
 
 	@Override
 	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		this.deliver(req, resp);
 	}
 
 	// internal methods
 
 	/**
 	 * Loads the configuration from file.
 	 * 
 	 * @param booter
 	 *            The system booter class
 	 * @throws FileNotFoundException
 	 *             If configuration file isn't found.
 	 * @throws ConfigurationSyntaxException
 	 *             If a syntax error is found at configuration file.
 	 */
 	protected void loadConfiguration() throws FileNotFoundException, ConfigurationSyntaxException, ClassNotFoundException, InstantiationException,
 			IllegalAccessException {
 		Booter booter = new Bootloader.Booter();
 		log.info("Loading cotopaxi configuration...");
 		ConfigurationLoader loader = new ConfigurationLoader(this.router, this.interceptor, booter);
 		loader.loadConfiguration();
 		registerDependency(TemplateRender.class.getName(), property(TEMPLATE_RENDER));
 		log.info("Configuration loaded...");
 		booter.boot();
 	}
 
 	/**
 	 * Resets the servlet/framework and dependencies to it initial state - previously to any
 	 * configuration load.
 	 * 
 	 * Calling this method can make application stop works properly.
 	 */
 	public void forceReset() {
		CotopaxiProperty.forceReset();
		DependencyManager.forceReset();
 		this.router.forceReset();
 		this.interceptor.forceReset();
 		this.executor.forceReset();
 	}
 
 	public void forceReload() throws FileNotFoundException, ConfigurationSyntaxException, ClassNotFoundException, InstantiationException,
 			IllegalAccessException {
 		this.forceReset();
 		this.loadConfiguration();
 	}
 
 	/**
 	 * Dispatches a request/response.
 	 */
 	public void deliver(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		try {
 			ControllerResponse controllerResponse = processRequest(request);
 			if (controllerResponse != null) {
 				processResponse(response, controllerResponse);
 			} else {
 				log.error("Cannot determine a ControllerResponse for url %s.\nDid you call some \"render\" method?", request.getRequestURI());
 				throw new ServletException("Cannot determine a ControllerResponse for request. Did you call some \"render\" method?");
 			}
 		} catch (Throwable t) {
 			log.warning(t, "Unexpected error executing controller for %s. Message: %s", request.getRequestURI(), t.getMessage());
 			Map<String, Object> output = new HashMap<String, Object>();
 			output.put(property(ERROR_PROPERTY), t);
 			ControllerResponse resp;
 			if (t instanceof Error) {
 				resp = new TemplateResponse("compilation_error.vm", 500, output, Locale.getDefault());
 			} else if (t instanceof VelocityException) {
 				resp = new TemplateResponse("template_error.vm", 500, output, Locale.getDefault());
 			} else {
 				resp = new TemplateResponse(property(ERROR_TEMPLATE), 500, output, request.getLocale());
 			}
 			processResponse(response, resp);
 		} finally {
 			this.interceptor.finish();
 		}
 
 	}
 
 	/**
 	 * Process the given request and returns a {@link ControllerResponse}. It will route the request
 	 * to a controller, call the executor to process it and return the {@link ControllerResponse}.
 	 * This method is also responsible by execute the necessaries {@link ControllerInterceptor}
 	 * 
 	 * @param request
 	 *            The request to be processed.
 	 * @return The {@link ControllerResponse} for this request.
 	 */
 	protected ControllerResponse processRequest(HttpServletRequest request) throws Throwable {
 		try {
 			ControllerDescriptor controllerDesc = this.router.route(request);
 			return this.executor.execute(controllerDesc, request);
 		} catch (NotFoundExeption ex) {
 			return this.executor.execute(request, ex);
 		}
 	}
 
 	/**
 	 * Process and dispatch the {@link ControllerResponse}. This method is also responsible by
 	 * execute the necessaries {@link TemplateInterceptor}.
 	 * 
 	 * @param response
 	 *            The {@link HttpServletResponse} to be used to dispatch the
 	 *            {@link ControllerResponse}
 	 * @param controllerResponse
 	 *            The {@link ControllerResponse} to be dispatched.
 	 */
 	protected void processResponse(HttpServletResponse response, ControllerResponse controllerResponse) throws IOException, ServletException {
 		if (controllerResponse instanceof TemplateResponse) {
 			this.interceptor.preRender((TemplateResponse) controllerResponse);
 		}
 		controllerResponse.dispatch(response);
 	}
 }
