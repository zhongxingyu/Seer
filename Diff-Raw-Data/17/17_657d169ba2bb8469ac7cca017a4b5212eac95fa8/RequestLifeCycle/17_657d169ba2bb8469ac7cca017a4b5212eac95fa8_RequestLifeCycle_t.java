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
 import org.layr.engine.TemplateParser;
 import org.layr.engine.components.IComponent;
 import org.layr.engine.components.TemplateParsingException;
 import org.layr.engine.expressions.ComplexExpressionEvaluator;
 import org.layr.engine.expressions.ExpressionEvaluator;
 import org.layr.jee.commons.EnterpriseJavaBeans;
 import org.xml.sax.SAXException;
 
 public class RequestLifeCycle {
 
     static final String AVAILABLES_ROUTES = "AVAILABLES_ROUTES";
 
     JEEBusinessRoutingRequestContext requestContext;
     List<Method> routes;
     Object targetInstance;
     EnterpriseJavaBeans ejbManager;
 	DefaultDataParser defaultDataParser;
	String currentContentType;
 
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
 	 * Render route not found
 	 */
 	public void renderRouteNotFound() {
 		requestContext.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
 		requestContext.log("[WARN] No route found to " + requestContext.getRequestedRoute());
 	}
 
     /**
      * Configure the WebResource. Realize some dependency injection.
      */
     public void performDependencyInjection() {
     	if (IResource.class.isInstance(targetInstance)) {
     		IResource instance = (IResource) targetInstance;
     		instance.initialize(getRequestContext());
     	}
 
     	EnterpriseJavaBeans ejbManager = getEjbManager();
 		if ( ejbManager != null )
 			ejbManager.injectEJB(targetInstance);
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
         	Object returnedObject = executeFoundRouteMethod(routeMethod);
             Route route = routeMethod.getAnnotation(Route.class);
 
     		String redirectTo = measureRedirectURL(route);
     		if ( !StringUtil.isEmpty(redirectTo) ) {
     			redirectToResource( redirectTo );
     		    return;
     		}
 
             render(returnedObject, route);
         } catch (Exception e) {
             throw new ServletException(e.getMessage(), e);
         }
     }
 
 	/**
 	 * @param returnedObject
 	 * @param route
 	 * @throws ServletException
 	 * @throws IOException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws CloneNotSupportedException
 	 * @throws TemplateParsingException
 	 */
 	public void render(Object returnedObject, Route route)
 			throws ServletException, IOException, IllegalAccessException,
 			InvocationTargetException, ParserConfigurationException,
 			SAXException, CloneNotSupportedException, TemplateParsingException {
 		defineOutputContentType(route);
 		String template = measureRouteTemplate(route);
 		renderTemplateOrTheRouteReturnedObject(template, returnedObject);
 	}
 
 	/**
 	 * @param route
 	 */
	public void defineOutputContentType(Route route) {
 		String contentType = measureRequestContentType(route);
 		if ( !StringUtil.isEmpty(contentType) )
 			requestContext.setContentType(contentType);
		currentContentType = contentType;
	}
	
	/**
	 * @return
	 */
	public boolean wasAlreadySetAContentType(){
		return !StringUtil.isEmpty(currentContentType);
 	}
 
 	/**
 	 * @param route
 	 * @return
 	 */
 	public String measureRequestContentType(Route route) {
 		String contentType = (String)ComplexExpressionEvaluator
 				.getValue(route.contentType(), requestContext);
 		return contentType;
 	}
 	
 	/**
 	 * @param route
 	 * @return
 	 */
 	public String measureRedirectURL(Route route) {
 		String redirectURL = (String)ComplexExpressionEvaluator
 				.getValue(route.redirectTo(), requestContext);
 		return redirectURL;
 	}
 
 	/**
 	 * @param routeMethod
 	 * @return
 	 * @throws ServletException
 	 * @throws IOException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	public Object executeFoundRouteMethod(Method routeMethod) throws ServletException,
 			IOException, IllegalAccessException, InvocationTargetException {
         bindParametersSentFromRequestAgainstWebResourceFields();
 		Object[] parameters = retrieveRouteMethodParametersFromRequest(routeMethod);
 		Object returnedObject = routeMethod.invoke(targetInstance, parameters);
 		bindWebResourceFieldsAgainstRequestContextForFurtherTemplateRenderingUse();
 		return returnedObject;
 	}
 
 	/**
 	 * @param route
 	 * @return
 	 */
 	public String measureRouteTemplate(Route route) {
 		String template = getGeneralTemplate();
 		if ( !StringUtil.isEmpty(route.template()) )
 		    template = route.template();
 
 		Object measuredTemplate = ExpressionEvaluator
 				.eval(targetInstance, template).getValue();
 
 		return measuredTemplate != null
 					? measuredTemplate.toString()
 					: template;
 	}
 
 	/**
 	 * Render the found web page (usually a template) or an route returned object
 	 * 
 	 * @param webpage
 	 * @param returnedValue
 	 * @throws IOException
 	 * @throws ServletException 
 	 * @throws CloneNotSupportedException 
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 * @throws TemplateParsingException 
 	 */
 	public void renderTemplateOrTheRouteReturnedObject(String templateName, Object returnedValue)
 			throws IOException, ParserConfigurationException, SAXException, CloneNotSupportedException, ServletException, TemplateParsingException {
 		HttpServletResponse response = requestContext.getResponse();
 
 		if (returnedValue != null && InputStream.class.isInstance(returnedValue))
 			renderABinaryObject(returnedValue, response);
 
 		else if (!StringUtil.isEmpty(templateName))
             renderRouteTemplate(templateName);
 		
 		else
 			renderReturnedValueAsJSON(returnedValue);
 	}
 
 	/**
 	 * @param returnedObject
 	 * @return
 	 */
 	public boolean isReturnedObjectNativeJavaObject(Object returnedObject) {
 		return returnedObject != null && returnedObject.getClass().getPackage().getName().equals("java.lang");
 	}
 
 	/**
 	 * @param returnedObject
 	 * @param response
 	 * @return
 	 * @throws IOException
 	 */
 	public PrintWriter renderAsString(Object returnedObject, HttpServletResponse response) throws IOException {
 		return response.getWriter().append(returnedObject.toString());
 	}
 
 	/**
 	 * @param templateName
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws CloneNotSupportedException
 	 * @throws ServletException
 	 * @throws TemplateParsingException 
 	 */
 	public void renderRouteTemplate(String templateName) throws IOException,
 			ParserConfigurationException, SAXException,
 			CloneNotSupportedException, ServletException, TemplateParsingException {
 		IComponent webpage = compileTemplate(templateName);
 		if ( webpage == null )
 			throw new IOException("Can't find template '" + templateName + "'.");
 		webpage.render();
 	}
 
 	/**
 	 * @param templateName
 	 * @return
 	 * @throws TemplateParsingException
 	 */
 	public IComponent compileTemplate(String templateName) throws TemplateParsingException{
 		TemplateParser parser = new TemplateParser(getRequestContext());
 		IComponent compiledComponent = parser.compile(templateName);
 		return compiledComponent;
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
     public void renderReturnedValueAsJSON(Object returnedValue) throws ServletException, IOException {
         try {
        	if (!wasAlreadySetAContentType())
        		requestContext.setContentType("application/json");
             PrintWriter writer = getRequestContext().getResponse().getWriter();
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
                     Object parameter = retrieveParameterFromAnnotation(requestParamsFromRoute, annotation);
 
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
                 } else {
                 	parameters[counter] = null;
                 }
             }
             counter++;
         }
 		return parameters;
 	}
 
 	/**
 	 * @param requestParamsFromRoute
 	 * @param annotation
 	 * @return
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public Object retrieveParameterFromAnnotation(Map<String, String> requestParamsFromRoute,
 			Annotation annotation) throws ServletException, IOException {
 		String annotatedParam = ((Parameter)annotation).value();
 		Object parameter = requestParamsFromRoute.get(annotatedParam);
 
 		if ( parameter == null )
 			parameter = requestContext.get(annotatedParam);
 
 		if ( parameter == null )
 			parameter = requestContext.getParameter(annotatedParam);
 		return parameter;
 	}
 
     /**
      * @throws IllegalAccessException 
      * @throws InstantiationException 
      */
 	public void bindParametersSentFromRequestAgainstWebResourceFields () {
 		HttpServletRequest request = getRequestContext().getRequest();
 		Enumeration<String> parameterNames = request.getParameterNames();
 		String expresion = null;
 		
 		while ( parameterNames.hasMoreElements() ){
 			expresion = parameterNames.nextElement();
 			try {
 				bindParameterAgainstWebResourceField(request, expresion);
 			} catch ( Throwable e ) {
 				getServletContext().log("[Layr] ERROR: " + e.getMessage());
 				e.printStackTrace();
 				continue;
 			}
 		}
 	}
 
 	/**
 	 * @param request
 	 * @param expresion
 	 */
 	public void bindParameterAgainstWebResourceField(HttpServletRequest request, String expresion) {
 		Object parsedValue = request.getParameter(expresion);
 		ExpressionEvaluator evaluator = ExpressionEvaluator.eval(targetInstance, "#{"+expresion+"}");
 		if (evaluator.setValue(parsedValue))
 			requestContext.put(expresion, parsedValue);
 	}
 
     /**
      */
     public void bindWebResourceFieldsAgainstRequestContextForFurtherTemplateRenderingUse() {
         Class<? extends Object> clazz = targetInstance.getClass();
 
         while(!clazz.equals(Object.class)){
 			for (Field field : clazz.getDeclaredFields()) {
 	            try {
 	            	bindFieldAgainstRequestContextForFurtherTemplateRenderingUse(field);
 	            } catch (Throwable e) {
 	                continue;
 	            }
 	        }
 
 			clazz = clazz.getSuperclass();
         }
     }
 
 	/**
 	 * @param field
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	public void bindFieldAgainstRequestContextForFurtherTemplateRenderingUse(Field field) throws IllegalAccessException,
 			InvocationTargetException {
 		Object returnedValue = Reflection.getAttribute(targetInstance, field.getName());
 		requestContext.put(field.getName(), returnedValue);
 	}
 
 	/**
 	 * @param request
 	 * @return
 	 */
 	public String getRequestURI(HttpServletRequest request) {
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
     public void redirectToResource(String urlPattern) throws IOException, ServletException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
     	String url = measureTheUrlShouldBeRedirectedTo(urlPattern);
         redirect( url );
     }
 
 	/**
 	 * @param urlPattern
 	 * @return
 	 */
 	public String measureTheUrlShouldBeRedirectedTo(String urlPattern) {
 		String url = (String) ComplexExpressionEvaluator.getValue(urlPattern, getRequestContext());
 
     	if (!isOutsideRedirectRequest(url)){
 			if ( !url.startsWith("/") )
 				url = getRequestContext().getWebResourceRootPath() + url;
 			url = (getRequestContext().getApplicationRootPath() + url)
 					.replaceAll("//+", "/");
     	}
 		return url;
 	}
 
     /**
      * @param url
      * @return
      */
     public boolean isOutsideRedirectRequest( String url ) {
     	return url.startsWith("http://")
     		|| url.startsWith("https://");
     }
 
 	/**
 	 * @return
 	 */
 	public Object getTargetInstance() {
 		return targetInstance;
 	}
 
 	/**
 	 * @param targetInstance
 	 */
 	public void setTargetInstance(Object targetInstance) {
 		this.targetInstance = targetInstance;
 	}
 
 	/**
 	 * @return
 	 */
 	public List<Method> getRoutes() {
 		return routes;
 	}
 
 	/**
 	 * @param routes
 	 */
 	public void setRoutes(List<Method> routes) {
 		this.routes = routes;
 	}
 
 	/**
 	 * @return
 	 */
 	public EnterpriseJavaBeans getEjbManager() {
 		if ( ejbManager == null )
 			ejbManager = ((JEEBusinessRoutingConfiguration)getRequestContext().getConfiguration()).getEjbManager();
 		return ejbManager;
 	}
 
 	/**
 	 * @return
 	 */
 	public ServletContext getServletContext() {
 		return requestContext
 				.getConfiguration()
 				.getServletContext();
 	}
 
 	/**
 	 * @param ejbManager
 	 */
 	public void setEjbManager(EnterpriseJavaBeans ejbManager) {
 		this.ejbManager = ejbManager;
 	}
 }
