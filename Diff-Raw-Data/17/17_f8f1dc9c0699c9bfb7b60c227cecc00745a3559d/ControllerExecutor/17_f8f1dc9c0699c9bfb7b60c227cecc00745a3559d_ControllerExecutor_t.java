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
 package br.octahedron.cotopaxi.controller;
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.ERROR_PROPERTY;
 import static br.octahedron.cotopaxi.CotopaxiProperty.ERROR_TEMPLATE;
 import static br.octahedron.cotopaxi.CotopaxiProperty.NOT_FOUND_TEMPLATE;
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import static br.octahedron.cotopaxi.controller.ControllerContext.clearContext;
 import static br.octahedron.cotopaxi.controller.ControllerContext.getContext;
 import static br.octahedron.cotopaxi.controller.ControllerContext.setContext;
 import static br.octahedron.cotopaxi.inject.Injector.getInstance;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import br.octahedron.cotopaxi.interceptor.InterceptorManager;
 import br.octahedron.cotopaxi.route.NotFoundExeption;
 import br.octahedron.cotopaxi.view.response.TemplateResponse;
 import br.octahedron.util.Log;
 import br.octahedron.util.ReflectionUtil;
 
 /**
  * This class is responsible by the controllers' execution and by gets the
  * {@link ControllerResponse} for each request. It also is responsible by handles all the
  * ControllerContext lifecycle.
  * 
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class ControllerExecutor {
 
 	private static final Log log = new Log(ControllerExecutor.class);
 
 	private Map<Integer, Method> methodsCache = new HashMap<Integer, Method>();
 	private InterceptorManager interceptor;
 
 	public ControllerExecutor(InterceptorManager interceptor) {
 		this.interceptor = interceptor;
 	}
 
 	/**
 	 * Handles a controller {@link NotFoundExeption}. It's responsible to generate a response once a
 	 * controller has not be found.
 	 * 
 	 * @return A {@link ControllerResponse} for the given {@link HttpServletRequest} /
 	 *         {@link NotFoundExeption}
 	 */
 	public ControllerResponse execute(HttpServletRequest request, NotFoundExeption nfex) {
 		log.warning("Cannot found a controller for url %s - %s.", request.getRequestURI(), request.getMethod());
 		setContext(request, new ControllerDescriptor(nfex.getUrl(), request.getMethod(), "error", "NotFound"));
 		Map<String, Object> output = new HashMap<String, Object>();
 		output.put(getProperty(ERROR_PROPERTY), nfex);
		return new TemplateResponse(getProperty(NOT_FOUND_TEMPLATE), 404, output, request.getLocale());
 	}
 
 	/**
 	 * Executes the given controller
 	 * 
 	 * @param controllerDesc
 	 *            The controller descriptor
 	 * @param request
 	 *            The current request
 	 * @return The {@link ControllerResponse} for the given controller
 	 */
 	public ControllerResponse execute(ControllerDescriptor controllerDesc, HttpServletRequest request) {
 		try {
 			// first of all, clear previous context
 			clearContext();
 			// load controller and fix context
 			Controller controller = this.loadController(controllerDesc);
 			setContext(request, controllerDesc);
 			Method method = this.getMethod(controllerDesc, controller);
 			ControllerContext context = getContext();
 			this.interceptor.execute(method.getDeclaringClass());
 			this.interceptor.execute(method);
 			// execute controller
 			if (!context.isAnswered()) {
 				log.debug("Executing controller %s - %s", controller.getClass().getName(), method.getName());
 				ReflectionUtil.invoke(controller, method);
 			} else {
 				log.debug("Controller %s - %s already answered, controller NOT executed!", controller.getClass().getName(), method.getName());
 			}
 			return context.getControllerResponse();
 		} catch (InvocationTargetException ex) {
 			log.warning(ex, "Unexpected error executing controller for %s. Message: %s", request.getRequestURI(), ex.getMessage());
 			Map<String, Object> output = new HashMap<String, Object>();
 			output.put(getProperty(ERROR_PROPERTY), ex);
			return new TemplateResponse(getProperty(ERROR_TEMPLATE), 500, output, request.getLocale());
 		} catch (Exception ex) {
 			/*
 			 * Here means an access error to controller. It can be cause if controller method
 			 * doesn't exists or has wrong visibility/parameters.
 			 * 
 			 * Anyway, we will (re)load the context as an InternalServerError
 			 */
 			setContext(request, controllerDesc);
 			log.error(ex, "Unable to load controller %s", controllerDesc);
 			return this.execute(request, new NotFoundExeption(request.getRequestURI(), request.getMethod()));
 		}
 	}
 
 	/**
 	 * Gets this controller method.
 	 */
 	private Method getMethod(ControllerDescriptor controllerDesc, Controller controller) throws SecurityException, NoSuchMethodException {
 		int hash = controllerDesc.hashCode();
 		if (this.methodsCache.containsKey(hash)) {
 			log.debug("Method founded at cache table. Hash: %d", hash);
 			return this.methodsCache.get(hash);
 		} else {
 			String method = controllerDesc.getFullControllerName();
 			Method result = ReflectionUtil.getMethod(controller.getClass(), method);
 			this.methodsCache.put(hash, result);
 			return result;
 		}
 
 	}
 
 	/**
 	 * Loads the controller instance
 	 */
 	private Controller loadController(ControllerDescriptor controllerDesc) throws InstantiationException, ClassNotFoundException {
 		return (Controller) getInstance(ReflectionUtil.getClass(controllerDesc.getControllerClass()));
 	}
 }
