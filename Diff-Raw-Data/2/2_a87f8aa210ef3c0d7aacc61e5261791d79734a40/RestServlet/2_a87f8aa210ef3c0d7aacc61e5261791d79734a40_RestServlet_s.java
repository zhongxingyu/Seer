 package common.web.rest;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.inject.Injector;
 
 /**
  * Servlet for handling REST Api requests.
  * @author jared.pearson
  */
 @Singleton
 public class RestServlet extends HttpServlet {
 	private static final long serialVersionUID = 7150818916790895212L;
 	private List<RequestHandlerMapping> requestMappings = null;
 	private Set<ResourceHandler> handlers;
 	
 	@Inject
 	Injector injector;
 	
 	@Inject
 	public RestServlet(Set<ResourceHandler> handlers) {
 		this.handlers = handlers;
 	}
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		
 		//parse the RequestHandler annotations from the handler classes
 		requestMappings = new ArrayList<RequestHandlerMapping>();
 		for(ResourceHandler handler : handlers) {
 			Class<?> requestHandlerClass = handler.getClass();
 			
 			for(java.lang.reflect.Method method : requestHandlerClass.getMethods()) {
 				if(!method.isAnnotationPresent(RequestHandler.class)) {
 					continue;
 				}
 				RequestHandler requestHandlerAnnotation = method.getAnnotation(RequestHandler.class);
 				
 				String pathRegex = requestHandlerAnnotation.value();
 				Pattern pattern = Pattern.compile(pathRegex);
 				
 				requestMappings.add(new RequestHandlerMapping(pattern, handler, method, requestHandlerAnnotation.method()));
 			}
 		}
 	}
 
 	@Override
 	public void service(ServletRequest request, ServletResponse response)
 			throws ServletException, IOException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		HttpServletResponse httpResponse = (HttpServletResponse) response;
 		
 		try {
 			RequestHandlerMapping mapping = getMapping(httpRequest);
 			mapping.execute(httpRequest, httpResponse, injector);
 		} catch(Exception exc) {
 			exc.printStackTrace(System.err);
 			httpResponse.sendError(500);
 		}
 	}
 	
 	/**
 	 * Gets the mapping from the request. If the no mapping is found, then
 	 * an {@link IllegalStateException} is thrown.
 	 */
 	private RequestHandlerMapping getMapping(HttpServletRequest request) {
 		for(RequestHandlerMapping mapping : requestMappings) {
 			if(mapping.handles(request)) {
 				return mapping;
 			}
 		}
 		
		throw new IllegalStateException("No mapping specified for request");
 	}
 	
 	private static class RequestHandlerMapping {
 		private final Pattern pattern;
 		private final Object handler;
 		private final Method method;
 		private final java.lang.reflect.Method handlerMethod;
 		
 		public RequestHandlerMapping(Pattern pattern, Object handler, java.lang.reflect.Method handlerMethod, Method method) {
 			this.pattern = pattern;
 			this.handler = handler;
 			this.handlerMethod = handlerMethod;
 			this.method = method;
 		}
 		
 		/**
 		 * Determines if the request is mapped to this RequestHandler
 		 */
 		public boolean handles(HttpServletRequest request) {
 			if(!request.getMethod().equals(method.toString())) {
 				return false;
 			}
 			
 			return getMatcher(request).matches();
 		}
 		
 		/**
 		 * Executes the handler against the specified request
 		 */
 		public void execute(final HttpServletRequest request, final HttpServletResponse response, final Injector injector)
 			throws ServletException, IOException {
 			
 			//get the arguments for the method
 			Object[] values = getMethodArguments(request, injector);
 			
 			//invoke the handler method with arguments requested
 			Object result = null;
 			try {
 				result = handlerMethod.invoke(handler, values);
 			} catch (IllegalArgumentException e) {
 				throw new ServletException(e);
 			} catch (IllegalAccessException e) {
 				throw new ServletException(e);
 			} catch (InvocationTargetException e) {
 				throw new ServletException(e);
 			}
 			
 			//if we get a return value, then output the value as JSON
 			if(result != null) {
 				response.setContentType("application/json");
 				Writer out = response.getWriter();
 				try {
 					ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
 					objectMapper.writeValue(out, result);
 				} finally {
 					out.close();
 				}
 			}
 		}
 		
 		private Matcher getMatcher(HttpServletRequest request) {
 			String pathInfo = request.getPathInfo();
 			if(pathInfo == null) {
 				pathInfo = "";
 			}
 			return pattern.matcher(pathInfo);
 		}
 		
 		private Object[] getMethodArguments(final HttpServletRequest request, final Injector injector) {
 
 			//initialize the value retrievers
 			PathParameterValueRetriever pathParameterValueRetriever = PathParameterValueRetriever.createFromRequest(request, this);
 			
 			//we need to determine the value to be specified in the parameter.
 			Annotation[][] parameterAnnotations = handlerMethod.getParameterAnnotations();
 			Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
 			Object[] values = new Object[parameterTypes.length];
 			for(int index = 0; index < parameterTypes.length; index++) {
 				Class<?> parameterType = parameterTypes[index];
 				Annotation[] annotations = parameterAnnotations[index];
 				
 				//let's check to see if the path parameter annotation has been specified
 				if(pathParameterValueRetriever.handles(index, parameterType, annotations)) {
 					values[index] = pathParameterValueRetriever.getValue(index, parameterType, annotations);
 					continue;
 				}
 				
 				//from the parameter types specified on the method, look up the 
 				//objects corresponding to the types from the IOC container
 				values[index] = injector.getInstance(parameterType);
 			}
 			return values;
 		}
 	}
 	
 	private static interface ValueRetriever {
 		public boolean handles(int index, Class<?> parameterType, Annotation[] parameterAnnotations);
 		public Object getValue(int index, Class<?> parameterType, Annotation[] parameterAnnotations);
 	}
 	
 	/**
 	 * Retrieves the values from the PathInfo using the {@link PathParameter} annotations.
 	 * @author jared.pearson
 	 */
 	private static class PathParameterValueRetriever implements ValueRetriever {
 		private final Matcher pathInfoMatcher;
 		
 		public PathParameterValueRetriever(final Matcher pathInfoMatcher) {
 			this.pathInfoMatcher = pathInfoMatcher;
 		}
 		
 		@Override
 		public boolean handles(int index, Class<?> parameterType, Annotation[] parameterAnnotations) {
 			return getPathParameterAnnotation(parameterAnnotations) != null;
 		}
 		
 		@Override
 		public Object getValue(int index, Class<?> parameterType, Annotation[] parameterAnnotations) {
 			//let's check to see if the path parameter annotation has been specified
 			PathParameter pathParameter = getPathParameterAnnotation(parameterAnnotations);
 			if(pathParameter == null) {
 				return null;
 			}
 			
 			if(!parameterType.isAssignableFrom(String.class)) {
 				throw new IllegalArgumentException("PathParameter must be of type String");
 			}
 			
 			int groupIndex = pathParameter.value();
 			
 			if(groupIndex > pathInfoMatcher.groupCount()) {
 				throw new IllegalArgumentException("Group index specified in PathParameter is out of bounds: " + groupIndex);
 			}
 			return pathInfoMatcher.group(groupIndex);
 		}
 		
 		public static PathParameterValueRetriever createFromRequest(final HttpServletRequest request, final RequestHandlerMapping mapping) {
 			Matcher pathInfoMatcher = mapping.getMatcher(request);
 			if(!pathInfoMatcher.matches()) {
 				throw new IllegalStateException("Expected the path to always match this handler's pattern");
 			}
 			return new PathParameterValueRetriever(pathInfoMatcher);
 		}
 		
 		private PathParameter getPathParameterAnnotation(Annotation[] annotations) {
 			PathParameter pathParameter = null;
 			for(Annotation annotation : annotations) {
 				if(annotation instanceof PathParameter) {
 					pathParameter = (PathParameter)annotation;
 					break;
 				}
 			}
 			return pathParameter;
 		}
 	}
 }
