 package org.layr.jee.routing.business;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.layr.commons.Cache;
 import org.layr.commons.Reflection;
 import org.layr.commons.StringUtil;
 import org.layr.commons.gson.DefaultDataParser;
 import org.layr.engine.components.IComponent;
 import org.layr.engine.expressions.ComplexExpressionEvaluator;
 import org.layr.engine.expressions.ExpressionEvaluator;
 import org.layr.jee.commons.EnterpriseJavaBeans;
 import org.xml.sax.SAXException;
 
 public class RequestLifeCycle {
 
     private static final String AVAILABLES_ROUTES = "AVAILABLES_ROUTES";
 
     private JEEBusinessRoutingRequestContext requestContext;
     private List<Method> routes;
     private Object targetInstance;
     private EnterpriseJavaBeans ejbManager;
 	private DefaultDataParser defaultDataParser;
 
     public RequestLifeCycle() {
         defaultDataParser = new DefaultDataParser();
     }
 
     public void run() throws ServletException, IOException {
         measureTargetInstanceAvailableRoutes();
 
         Method routeMethod = getRouteMethod();
         if (routeMethod == null) {
         	renderRouteNotFound();
         	return;
         }
 
         performDependencyInjection();
         run( routeMethod );
     }
 
 	/**
 	 * Measure which route methods are available from target instance
 	 */
 	public void measureTargetInstanceAvailableRoutes() {
 		Map<Class<?>, List<Method>> routeMethodCache = getRouteMethodCache();
         if ( routeMethodCache.containsKey(targetInstance.getClass()) )
         	routes = routeMethodCache.get(targetInstance.getClass());
     	else
     		routes = Reflection.extractAnnotatedMethodsFor(Route.class, targetInstance);
 	}
 
 	/**
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public Map<Class<?>, List<Method>> getRouteMethodCache() {
 		Cache cache = requestContext.getCache();
 
 		if ( cache == null )
 			return new HashMap<Class<?>, List<Method>>();
 
 		Map<Class<?>, List<Method>> routeMethodCache = (Map<Class<?>, List<Method>>)cache.get(AVAILABLES_ROUTES);
 		if ( routeMethodCache == null ){
         	routeMethodCache = new ConcurrentHashMap<Class<?>, List<Method>>();
         	cache.put(AVAILABLES_ROUTES, routeMethodCache);
 		}
 		return routeMethodCache;
 	}
 
     /**
      * Configure the WebResource. Realize some dependency injection.
      */
     public void performDependencyInjection() {
     	if (IResource.class.isInstance(targetInstance)) {
     		IResource instance = (IResource) targetInstance;
     		instance.initialize(getRequestContext());
     	}
 
     	getEjbManager().injectEJB(targetInstance);
 	}
 
     /**
      * Makes the route method discovery. If found, run the route method. It will always try to render something,
      * unless any route method has defined a template neither the Web.
      * @param routeMethod 
      * @param request
      * @param response
      * @throws ServletException
      */
     public void run(Method routeMethod) throws ServletException {
         try {
             Route route = routeMethod.getAnnotation(Route.class);
             bindParameters();
 
             if ( !StringUtil.isEmpty(route.contentType()) )
             	requestContext.setContentType(route.contentType());
 
             if ( route.json() ) {
                 renderAsJSON(routeMethod);
                 return;
             }
 
             String redirectTo = route.redirectTo();
 			if ( !StringUtil.isEmpty(redirectTo) ) {
 				redirectToResource( routeMethod, redirectTo );
                 return;
             }
 
 			runMethodAndRenderResponse(routeMethod, route);
         } catch (Exception e) {
             throw new ServletException(e.getMessage(), e);
         }
     }
 
 	/**
 	 * @param routeMethod
 	 * @param route
 	 * @throws ServletException
 	 * @throws IOException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws CloneNotSupportedException
 	 */
 	public void runMethodAndRenderResponse(Method routeMethod, Route route)
 			throws ServletException, IOException, IllegalAccessException,
 			InvocationTargetException, ParserConfigurationException,
 			SAXException, CloneNotSupportedException {
 		Object[] parameters = retrieveRouteMethodParametersFromRequest(routeMethod);
 		Object returnedObject = routeMethod.invoke(targetInstance, parameters);
         bindEntities();
         String template = measureRouteTemplate(route);
 		renderWebPageOrRouteReturnedObject(template, returnedObject);
 	}
 
 	/**
 	 * Render route not found
 	 */
 	public void renderRouteNotFound() {
 		requestContext.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
 		requestContext.log("[WARN] No route found to " + requestContext.getRequestedRoute());
 	}
 
 	/**
 	 * @param route
 	 * @return
 	 */
 	public String measureRouteTemplate(Route route) {
 		String template = getGeneralTemplate();
 		if ( !StringUtil.isEmpty(route.template()) )
 		    template = route.template();
		return ExpressionEvaluator
 				.eval(targetInstance, template)
				.getValue()
				.toString();
 	}
 
 	/**
 	 * Render the found web page (usually a template) or an route returned object
 	 * 
 	 * @param webpage
 	 * @param returnedObject
 	 * @throws IOException
 	 * @throws ServletException 
 	 * @throws CloneNotSupportedException 
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public void renderWebPageOrRouteReturnedObject(String templateName, Object returnedObject)
 			throws IOException, ParserConfigurationException, SAXException, CloneNotSupportedException, ServletException {
 		
 		HttpServletResponse response = requestContext.getResponse();
 		if (returnedObject != null && returnedObject.getClass().getPackage().getName().equals("java.lang")) {
 			response.getWriter().append(returnedObject.toString());
 		}
 		else if (returnedObject != null && InputStream.class.isInstance(returnedObject)) {
 			renderABinaryObject(returnedObject, response);
 		}
 		else if (!StringUtil.isEmpty(templateName)) {
             renderRouteTemplate(templateName);
 		}
 	}
 
 	/**
 	 * @param templateName
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws CloneNotSupportedException
 	 * @throws ServletException
 	 */
 	public void renderRouteTemplate(String templateName) throws IOException,
 			ParserConfigurationException, SAXException,
 			CloneNotSupportedException, ServletException {
 		IComponent webpage = requestContext.compile(templateName);
 		if ( webpage == null )
 			throw new IOException("Can't find template '" + templateName + "'.");
 		webpage.render();
 	}
 
 	/**
 	 * @param returnedObject
 	 * @param response
 	 * @throws IOException
 	 */
 	public void renderABinaryObject(Object returnedObject,
 			HttpServletResponse response) throws IOException {
 		InputStream in = (InputStream) returnedObject;
 		OutputStream out = response.getOutputStream();
 
 		int nextChar;
 		while ((nextChar = in.read()) != -1)
 			out.write(nextChar);
 	}
 
     /**
      * Render a JSON Route
      * @param routeMethod
      * @throws ServletException
      * @throws IOException
      */
     public void renderAsJSON(Method routeMethod) throws ServletException, IOException {
         Object[] parameters = retrieveRouteMethodParametersFromRequest(routeMethod);
         requestContext.setContentType("application/json");
 
         try {
             PrintWriter writer = getRequestContext().getResponse().getWriter();
             Object returnedValue = routeMethod.invoke(targetInstance, parameters);
             bindEntities();
 			String json = defaultDataParser.encode(returnedValue);
             writer.write(json);
         } catch (Exception e) {
             throw new ServletException(e.getMessage(), e);
         }
     }
 
 	/**
 	 * Retrieve parameters to the route method from the request. All sent parameters
 	 * should be JSON complaint. 
 	 * @param routeMethod
 	 * @return
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public Object[] retrieveRouteMethodParametersFromRequest(Method routeMethod) throws ServletException,
 			IOException {
 		Class<?>[] parameterTypes = routeMethod.getParameterTypes();
         Annotation[][] parameterAnnotations = routeMethod.getParameterAnnotations();
         Object[] parameters = new Object[parameterTypes.length];
         Map<String, String> requestParamsFromRoute = requestContext.getRequestParamsFromRoutePattern();
         
         short counter = 0;
         for ( Annotation[] annotations : parameterAnnotations ) {
             for ( Annotation annotation : annotations ) {
                 if (Parameter.class.isAssignableFrom(annotation.getClass())) {
                     String annotatedParam = ((Parameter)annotation).value();
 					Object parameter = requestParamsFromRoute.get(annotatedParam);
 
 					if ( parameter == null )
 						parameter = requestContext.get(annotatedParam);
 
 					if ( parameter == null )
 						parameter = requestContext.getParameter(annotatedParam);
 
                     if (parameterTypes[counter].isAssignableFrom(String.class)) {
                     	parameters[counter] = (String)parameter;
                     	continue;
                     }
 
                     if ( parameter == null ) {
                     	parameters[counter] = null;
                     	continue;
                     }
 
                     if ( String.class.isInstance(parameter) ) {
                     	Object fromJson = defaultDataParser.decode(
                 			(String)parameter, parameterTypes[counter], null);
 	                    parameters[counter] = fromJson;
                     } else if ( parameterTypes[counter].equals(parameter.getClass()) )
                     	parameters[counter] = parameter;
                     else
                     	requestContext.log("Can't set parameter '"+annotatedParam+"': incompatible assignment types.");
                 } else {
                 	parameters[counter] = null;
                 }
             }
             counter++;
         }
 		return parameters;
 	}
 
     /**
      * Execute the Parameters Bind step from Layr Life Cycle. By default,
      * auto-bind any sent parameter from HTTP client against field attributes
      * at targetInstance defined object. Developers are encouraged to manually
      * override this method to bind just what is useful.
      * @throws IllegalAccessException 
      * @throws InstantiationException 
      */
 	public void bindParameters () {
 		HttpServletRequest request = getRequestContext().getRequest();
 		Enumeration<String> parameterNames = request.getParameterNames();
 		String expresion = null;
 		
 		while ( parameterNames.hasMoreElements() ){
 			expresion = parameterNames.nextElement();
 
 			try {
 				Object parsedValue = request.getParameter(expresion);
 				ExpressionEvaluator evaluator = ExpressionEvaluator.eval(targetInstance, "#{"+expresion+"}");
 				if (evaluator.setValue(parsedValue))
 					requestContext.put(expresion, parsedValue);
 			} catch ( Throwable e ) {
 				getServletContext().log("[Layr] ERROR: " + e.getMessage());
 				e.printStackTrace();
 				continue;
 			}
 		}
 	}
 
     /**
      * Execute the Entity Bind step from Layr Life Cycle. By default,
      * auto-bind any field from the current resource against the LayrContext.
      * Developers are encouraged to manually override this method to bind just
      * what is useful.
      */
     public void bindEntities() {
         Class<? extends Object> clazz = targetInstance.getClass();
 
         while(!clazz.equals(Object.class)){
 			for (Field field : clazz.getDeclaredFields()) {
 	            try {
 	            	Object returnedValue = Reflection.getAttribute(targetInstance, field.getName());
 	                requestContext.put(field.getName(), returnedValue);
 	            } catch (Throwable e) {
 	                continue;
 	            }
 	        }
 
 			clazz = clazz.getSuperclass();
         }
     }
 
     /**
      * Returns witch method represents the current request.
      * 
      * @return
      */
     public Method getRouteMethod() {
     	HttpServletRequest request = getRequestContext().getRequest();
         String route = getRequestURI(request);
 
         if (route != null)
 	        for (Method method : routes) {
 	        	String pattern = parseMethodUrlPattern(method);
 				if ( ( pattern != null && route.matches( pattern ) )
 	            	|| method.getName().equals(
 	            			route.replaceFirst("/$", ""))) {
 					Route annotation = method.getAnnotation(Route.class);
 					getRequestContext().setRequestRoutePattern(
 							annotation.pattern()
 			        			.replaceFirst("^/", "")
 								.replaceFirst(getRequestContext().getWebResourceRootPath(), ""));
 					getRequestContext().setRequestRoute(route);
 	                return method;
 	            }
 	        }
 
         return null;
     }
 
 	/**
 	 * @param request
 	 * @return
 	 */
 	private String getRequestURI(HttpServletRequest request) {
 		return request.getRequestURI()
 			.replaceFirst(getRequestContext().getApplicationRootPath(),"")
 			.replaceFirst(getRequestContext().getWebResourceRootPath(), "")
 			.replaceFirst("^/", "");
 	}
     
     /**
      * Parse Method URL Pattern
      * @param method
      * @return
      */
     public String parseMethodUrlPattern(Method method) {
     	Route annotation = method.getAnnotation(Route.class);
     	if ( !annotation.pattern().isEmpty() ) {
 			String urlpattern = ComplexExpressionEvaluator.newInstance().parseMethodUrlPatternToRegExp(
 					annotation.pattern()
 						.replaceFirst(getRequestContext().getWebResourceRootPath(), "")
 						.replaceFirst("^/","")
 						.replaceFirst("//*", "/"));
 			return urlpattern;
     	}
     	return null;
     }
 
     /**
      * @return the template defined at WebResource annotation.
      */
     public String getGeneralTemplate() {
         WebResource resource = targetInstance.getClass().getAnnotation(WebResource.class);
         if (resource != null)
             return resource.template();
         return null;
     }
 
     /**
      * @return
      */
     public JEEBusinessRoutingRequestContext getRequestContext() {
         return requestContext;
     }
 
     /**
      * @param layrRequestContext
      */
     public void setRequestContext(JEEBusinessRoutingRequestContext layrRequestContext) {
         this.requestContext = layrRequestContext;
     }
 
     /**
      * Changes the layrContext navigation. Whenever you chance the layr
      * layrContext navigation the user will be redirected to the new URI.
      *
      * @param uri
      * @throws IOException
      */
     public void redirect(String uri) throws IOException {
         requestContext.getResponse().sendRedirect(uri);
     }
 
     /**
      * @param routeMethod 
      * @param url
      * @throws IOException
      * @throws ServletException 
      * @throws InvocationTargetException 
      * @throws IllegalAccessException 
      * @throws IllegalArgumentException 
      */
     public void redirectToResource(Method routeMethod, String urlPattern) throws IOException, ServletException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
     	Object[] retrievedParameters = retrieveRouteMethodParametersFromRequest(routeMethod);
     	routeMethod.invoke(targetInstance, retrievedParameters);
     	String url = (String) ComplexExpressionEvaluator.getValue(urlPattern, requestContext);
 		if ( !url.startsWith("/") )
 			url = requestContext.getWebResourceRootPath() + url;
         redirect(requestContext.getApplicationRootPath() + url);
     }
 
 	public Object getTargetInstance() {
 		return targetInstance;
 	}
 
 	public void setTargetInstance(Object targetInstance) {
 		this.targetInstance = targetInstance;
 	}
 
 	public List<Method> getRoutes() {
 		return routes;
 	}
 
 	public void setRoutes(List<Method> routes) {
 		this.routes = routes;
 	}
 
 	public EnterpriseJavaBeans getEjbManager() {
 		if ( ejbManager == null )
 			ejbManager = ((JEEBusinessRoutingConfiguration)getRequestContext().getConfiguration()).getEjbManager();
 		return ejbManager;
 	}
 
 	public ServletContext getServletContext() {
 		return requestContext
 				.getConfiguration()
 				.getServletContext();
 	}
 
 	public void setEjbManager(EnterpriseJavaBeans ejbManager) {
 		this.ejbManager = ejbManager;
 	}
 }
