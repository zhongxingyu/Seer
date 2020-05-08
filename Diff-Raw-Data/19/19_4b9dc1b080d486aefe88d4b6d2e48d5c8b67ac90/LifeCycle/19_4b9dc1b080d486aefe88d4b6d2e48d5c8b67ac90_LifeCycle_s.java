 package layr;
 
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
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import layr.annotation.Action;
 import layr.annotation.Parameter;
 import layr.annotation.WebResource;
 import layr.binding.ComplexExpressionEvaluator;
 import layr.binding.ExpressionEvaluator;
 import layr.components.IComponent;
 import layr.util.Reflection;
 import layr.util.StringUtil;
 
 import org.xml.sax.SAXException;
 
 
 import com.google.gson.Gson;
 
 public class LifeCycle {
 
 	private static Map<Class<?>, List<Method>> actionMethodCache;
 
     private RequestContext layrContext;
     private List<Method> actions;
     private Object targetInstance;
 
     public LifeCycle() {}
 
     public void run() throws ServletException, IOException {
         layrContext.setCharacterEncoding("UTF-8");
         layrContext.setContentType("text/html");
         measureTargetInstanceAvailableActions();
 
         Method actionMethod = getActionMethod();
         if (actionMethod == null) {
         	renderActionNotFound();
         	return;
         }
 
         performDependencyInjection();
         run( actionMethod );
     }
 
 	/**
 	 * Measure which action methods are available from target instance
 	 */
 	public void measureTargetInstanceAvailableActions() {
 		Map<Class<?>, List<Method>> actionMethodCache = getActionMethodCache();
         if ( actionMethodCache.containsKey(targetInstance.getClass()) )
         	actions = actionMethodCache.get(targetInstance.getClass());
     	else
     		actions = Reflection.extractAnnotatedMethodsFor(Action.class, targetInstance);
 	}
 
 	/**
 	 * @return
 	 */
 	public Map<Class<?>, List<Method>> getActionMethodCache() {
 		if ( actionMethodCache == null )
         	actionMethodCache = new HashMap<Class<?>, List<Method>>();
 		return actionMethodCache;
 	}
 
     /**
      * Configure the WebResource. Realize some dependency injection.
      */
     public void performDependencyInjection() {
     	if (IResource.class.isInstance(targetInstance)) {
     		IResource instance = (IResource) targetInstance;
     		instance.initialize(getLayrRequestContext());
     	}
 
     	getApplicationContext().getEjbManager().injectEJB(targetInstance);
 	}
 
     /**
      * Makes the action method discovery. If found, run the action method. It will always try to render something,
      * unless any action method has defined a template neither the Web.
      * @param actionMethod 
      * @param request
      * @param response
      * @throws ServletException
      */
     public void run(Method actionMethod) throws ServletException {
         try {
             Action action = actionMethod.getAnnotation(Action.class);
             String template = measureActionTemplate(action);
             bindParameters();
 
             if ( !StringUtil.isEmpty(action.contentType()) )
             	layrContext.setContentType(action.contentType());
 
             if ( action.json() ) {
                 renderAsJSON(actionMethod);
                 return;
             }
 
             String redirectTo = action.redirectTo();
 			if ( !StringUtil.isEmpty(redirectTo) ) {
 				redirectToResource( (String) ComplexExpressionEvaluator.getValue(redirectTo, layrContext) );
                 return;
             }
 
 			runMethodAndRenderResponse(actionMethod, template);
         } catch (Exception e) {
             throw new ServletException(e.getMessage(), e);
         }
     }
 
 	/**
 	 * @param actionMethod
 	 * @param template
 	 * @throws ServletException
 	 * @throws IOException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws CloneNotSupportedException
 	 */
 	public void runMethodAndRenderResponse(Method actionMethod, String template)
 			throws ServletException, IOException, IllegalAccessException,
 			InvocationTargetException, ParserConfigurationException,
 			SAXException, CloneNotSupportedException {
 		Object[] parameters = retrieveActionMethodParametersFromRequest(actionMethod);
 		Object returnedObject = actionMethod.invoke(targetInstance, parameters);
         bindEntities();
 		renderWebPageOrActionReturnedObject(template, returnedObject);
 	}
 
 	/**
 	 * Render action not found
 	 */
 	public void renderActionNotFound() {
 		layrContext.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
 		layrContext.log("[WARN] No action found to " + layrContext.getRequestAction());
 	}
 
 	/**
 	 * @param action
 	 * @return
 	 */
 	public String measureActionTemplate(Action action) {
 		String template = getGeneralTemplate();
 		if ( !StringUtil.isEmpty(action.template()) )
 		    template = action.template();
 		return template;
 	}
 
 	/**
 	 * Render the found web page (usually a template) or an action returned object
 	 * 
 	 * @param webpage
 	 * @param returnedObject
 	 * @throws IOException
 	 * @throws ServletException 
 	 * @throws CloneNotSupportedException 
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public void renderWebPageOrActionReturnedObject(String templateName, Object returnedObject)
 			throws IOException, ParserConfigurationException, SAXException, CloneNotSupportedException, ServletException {
 		
 		HttpServletResponse response = layrContext.getResponse();
 		if (returnedObject != null && returnedObject.getClass().getPackage().getName().equals("java.lang")) {
 			response.getWriter().append(returnedObject.toString());
 		}
 		else if (returnedObject != null && InputStream.class.isInstance(returnedObject)) {
 			renderABinaryObject(returnedObject, response);
 		}
 		else if (!StringUtil.isEmpty(templateName)) {
             renderActionTemplate(templateName);
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
 	public void renderActionTemplate(String templateName) throws IOException,
 			ParserConfigurationException, SAXException,
 			CloneNotSupportedException, ServletException {
 		IComponent webpage = getApplicationContext().compile(templateName, layrContext);
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
 	 * @return
 	 */
 	public ApplicationContext getApplicationContext() {
 		return getLayrRequestContext().getApplicationContext();
 	}
 
     /**
      * Render a JSON Action
      * @param actionMethod
      * @throws ServletException
      * @throws IOException
      */
     public void renderAsJSON(Method actionMethod) throws ServletException, IOException {
         Object[] parameters = retrieveActionMethodParametersFromRequest(actionMethod);
         layrContext.setContentType("application/json");
 
         try {
             PrintWriter writer = getLayrRequestContext().getResponse().getWriter();
             Object returnedValue = actionMethod.invoke(targetInstance, parameters);
             bindEntities();
            String json = new Gson().toJson(returnedValue);
             writer.write(json);
         } catch (Exception e) {
             throw new ServletException(e.getMessage(), e);
         }
     }
 
 	/**
 	 * Retrieve parameters to the action method from the request. All sent parameters
 	 * should be JSON complaint. 
 	 * @param actionMethod
 	 * @return
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public Object[] retrieveActionMethodParametersFromRequest(Method actionMethod) throws ServletException,
 			IOException {
 		Class<?>[] parameterTypes = actionMethod.getParameterTypes();
         Annotation[][] parameterAnnotations = actionMethod.getParameterAnnotations();
         Object[] parameters = new Object[parameterTypes.length];
         Map<String, String> requestParamsFromAction = layrContext.getRequestParamsFromActionPattern();
         
         short counter = 0;
         for ( Annotation[] annotations : parameterAnnotations ) {
             for ( Annotation annotation : annotations ) {
                 if (Parameter.class.isAssignableFrom(annotation.getClass())) {
                     String annotatedParam = ((Parameter)annotation).value();
 					Object parameter = requestParamsFromAction.get(annotatedParam);
 
 					if ( parameter == null )
 						parameter = layrContext.get(annotatedParam);
 
 					if ( parameter == null )
 						parameter = layrContext.getParameter(annotatedParam);
 
                     if (parameterTypes[counter].isAssignableFrom(String.class)) {
                     	parameters[counter] = (String)parameter;
                     	continue;
                     }
 
                     if ( parameter == null ) {
                     	parameters[counter] = null;
                     	continue;
                     }
 
                     if ( String.class.isInstance(parameter) ) {
 	                    Object fromJson = new Gson().fromJson((String)parameter, parameterTypes[counter]);
 	                    parameters[counter] = fromJson;
                     } else if ( parameterTypes[counter].equals(parameter.getClass()) )
                     	parameters[counter] = parameter;
                     else
                     	layrContext.log("Can't set parameter '"+annotatedParam+"': incompatible assignment types.");
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
 		HttpServletRequest request = getLayrRequestContext().getRequest();
 		Enumeration<String> parameterNames = request.getParameterNames();
 		String expresion = null;
 		
 		while ( parameterNames.hasMoreElements() ){
 			expresion = parameterNames.nextElement();
 
 			try {
 				Object parsedValue = request.getParameter(expresion);
 				ExpressionEvaluator evaluator = ExpressionEvaluator.eval(targetInstance, "#{"+expresion+"}");
 				if (evaluator.setValue(parsedValue))
 					layrContext.put(expresion, parsedValue);
 			} catch ( Throwable e ) {
 				getApplicationContext().getServletContext().log("[Layr] ERROR: " + e.getMessage());
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
 	                layrContext.put(field.getName(), returnedValue);
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
     public Method getActionMethod() {
     	HttpServletRequest request = getLayrRequestContext().getRequest();
         String action = getRequestURI(request);
 
         if (action != null)
 	        for (Method method : actions) {
 	        	String pattern = parseMethodUrlPattern(method);
 				if ( ( pattern != null && action.matches( pattern ) )
 	            	|| method.getName().equals(
 	            			action.replaceFirst("/$", ""))) {
 					Action annotation = method.getAnnotation(Action.class);
 					getLayrRequestContext().setRequestActionPattern(
 							annotation.pattern()
 			        			.replaceFirst("^/", "")
 								.replaceFirst(getLayrRequestContext().getServletPath(), ""));
 					getLayrRequestContext().setRequestAction(action);
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
 			.replaceFirst(getLayrRequestContext().getContextPath(),"")
 			.replaceFirst(getLayrRequestContext().getServletPath(), "")
 			.replaceFirst("^/", "");
 	}
     
     /**
      * Parse Method URL Pattern
      * @param method
      * @return
      */
     public String parseMethodUrlPattern(Method method) {
     	Action annotation = method.getAnnotation(Action.class);
     	if ( !annotation.pattern().isEmpty() ) {
 			String urlpattern = ComplexExpressionEvaluator.parseMethodUrlPatternToRegExp(
 					annotation.pattern()
 						.replaceFirst(getLayrRequestContext().getServletPath(), "")
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
     public RequestContext getLayrRequestContext() {
         return layrContext;
     }
 
     /**
      * @param layrRequestContext
      */
     public void setLayrRequestContext(RequestContext layrRequestContext) {
         this.layrContext = layrRequestContext;
     }
 
     /**
      * Changes the layrContext navigation. Whenever you chance the layr
      * layrContext navigation the user will be redirected to the new URI.
      *
      * @param uri
      * @throws IOException
      */
     public void redirect(String uri) throws IOException {
         layrContext.getResponse().sendRedirect(uri);
     }
 
     /**
      * @param url
      * @throws IOException
      */
     public void redirectToResource(String url) throws IOException {
 		if ( !url.startsWith("/") )
 			url = layrContext.getServletPath() + url;
         redirect(layrContext.getContextPath() + url);
     }
 
 	public Object getTargetInstance() {
 		return targetInstance;
 	}
 
 	public void setTargetInstance(Object targetInstance) {
 		this.targetInstance = targetInstance;
 	}
 
 	public List<Method> getActions() {
 		return actions;
 	}
 
 	public void setActions(List<Method> actions) {
 		this.actions = actions;
 	}
 	
 	public static void clearCache(){
 		if ( actionMethodCache == null )
 			return;
 
 		actionMethodCache.clear();
 		actionMethodCache = null;
 	}
 }
