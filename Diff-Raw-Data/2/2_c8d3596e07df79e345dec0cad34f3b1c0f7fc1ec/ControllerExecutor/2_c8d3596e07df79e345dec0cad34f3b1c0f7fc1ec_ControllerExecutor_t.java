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
 
 import static br.octahedron.cotopaxi.controller.ControllerContext.clearContext;
 import static br.octahedron.cotopaxi.controller.ControllerContext.setContext;
 import static br.octahedron.cotopaxi.controller.ControllerContext.getContext;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import br.octahedron.cotopaxi.inject.InstanceHandler;
 import br.octahedron.cotopaxi.interceptor.InterceptorManager;
 import br.octahedron.cotopaxi.route.NotFoundExeption;
 import br.octahedron.util.Log;
 import br.octahedron.util.ReflectionUtil;
 
 /**
  * This class is responsible by the controllers' execution.
  * 
  * TODO improve it
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class ControllerExecutor {
 
 	private static final Log log = new Log(ControllerExecutor.class);
 
 	private InstanceHandler handler = new InstanceHandler();
 	private Map<Integer, Method> methodsCache = new HashMap<Integer, Method>();
 	private InterceptorManager interceptor;
 	
 	public ControllerExecutor(InterceptorManager interceptor) {
 		this.interceptor = interceptor;
 	}
 
 	public ControllerResponse execute(ControllerDescriptor controllerDesc, HttpServletRequest request) throws IOException, NotFoundExeption,
 			ControllerException {
 		try {
 			// load controller
 			Controller controller = this.loadController(controllerDesc);
 			Method method = this.getMethod(controllerDesc, controller);
 			setContext(request, controllerDesc.getControllerName());
 			ControllerContext context = getContext();
 			interceptor.execute(method);
 			// execute controller
 			if (!context.isAnswered()) {
 				log.debug("Executing controller %s - %s", controller.getClass().getName(), method.getName());
 				ReflectionUtil.invoke(controller, method);
 			} else {
 				log.debug("Controller %s - %s already answered, controller NOT executed!", controller.getClass().getName(), method.getName());
 			}
 			return context.getControllerResponse();
 		} catch (InvocationTargetException ex) {
 			log.warning("Error executing controller %s", controllerDesc);
 			throw new ControllerException(ex.getCause());
 		} catch (Exception ex) {
 			log.error("Unable to load controller %s", controllerDesc);
 			log.terror("Unable to load controller", ex);
 			throw new NotFoundExeption(request.getRequestURI(), request.getMethod());
 		} finally {
 			clearContext();
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
 			String name = controllerDesc.getControllerName();
 			String method = controllerDesc.getHttpMethod();
 			method += (name.length() > 2) ? name.substring(0, 1).toUpperCase() + name.substring(1) : name.toUpperCase();
 			Method result = ReflectionUtil.getMethod(controller.getClass(), method);
 			this.methodsCache.put(hash, result);
 			return result;
 		}
 
 	}
 
 	/**
 	 * Loads the controller instance
 	 */
 	private Controller loadController(ControllerDescriptor controllerDesc) throws InstantiationException, ClassNotFoundException {
 		return (Controller) handler.getInstance(ReflectionUtil.getClass(controllerDesc.getControllerClass()));
 	}
 }
