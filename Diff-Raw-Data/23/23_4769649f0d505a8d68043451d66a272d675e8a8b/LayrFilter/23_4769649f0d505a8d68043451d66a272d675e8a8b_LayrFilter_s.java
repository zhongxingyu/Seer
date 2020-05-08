 package layr.servlet;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.parsers.ParserConfigurationException;
 
import layr.LayrFactory;
 import layr.ApplicationContext;
 import layr.LifeCycle;
 import layr.RequestContext;
 import layr.components.IComponent;
 
 import org.xml.sax.SAXException;
 
 
 @WebFilter("/*")
 public class LayrFilter implements Filter {
 
 	private FilterConfig filterConfig;
 	private ApplicationContext layrApplicationContext;
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		filterConfig.getServletContext().log( "[Layr] Initializing Main Filter defined by " + this.getClass().getCanonicalName());
 		layrApplicationContext = LayrFactory.getOrCreateApplicationContext(filterConfig.getServletContext());
 		this.filterConfig = filterConfig;
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
 	 */
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 
 		RequestContext layrContext = LayrFactory.createRequestContext(request,
 				response, filterConfig.getServletContext());
 
 		try {
 			if ( renderTheWebPageAsXHTML( layrContext ) )
 				return;
 
 			Object instance = lookupLayrResource(layrContext);
 			if (instance == null) {
 				chain.doFilter(request, response);
 				return;
 			}
 
 			// TODO: colocar cache de lifecycle
 			runWebResource(layrContext, instance);
 		} catch (Exception e) {
 			throw new ServletException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * @param instance
 	 * @throws IOException 
 	 * @throws ServletException 
 	 * @throws CloneNotSupportedException 
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public boolean renderTheWebPageAsXHTML( RequestContext layrContext ) 
 			throws IOException, ParserConfigurationException, SAXException, CloneNotSupportedException, ServletException {
 
 		String relativePath = layrContext.getRelativePath().replaceFirst("/$", "");
 		if ( !relativePath.endsWith(".xhtml") )
 			relativePath += ".xhtml";
 
 		IComponent webpage = null;
 		webpage = layrApplicationContext.compile( relativePath, layrContext);
 		if ( webpage == null )
 			return false;
 
 		HttpServletRequest request = layrContext.getRequest();
 		Enumeration<String> parameterNames = request.getParameterNames();
 		String expresion = null;
 
 		while ( parameterNames.hasMoreElements() ){
 			expresion = parameterNames.nextElement();
 			layrContext.put(expresion, request.getParameter(expresion));
 		}
 
 		webpage.render();
 		return true;
 	}
 
 	/**
 	 * @param layrContext
 	 * @return
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	public Object lookupLayrResource(RequestContext layrContext) throws InstantiationException, IllegalAccessException {
 		
 		String requestURI = layrContext.getRelativePath();
 
 		Map<String, Object> webResources = layrApplicationContext.getRegisteredWebResources();
 		Set<String> patterns = webResources.keySet();
 		for (String pattern : patterns) {
 			if (requestURI.matches(pattern+".*")){
 				layrContext.setServletPath(pattern);
 				Object instance = webResources.get(pattern);
 
 				if (Class.class.isInstance(instance))
 					instance = ((Class<?>)instance).newInstance();
 
 				return instance;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param layrContext
 	 * @param target
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void runWebResource(RequestContext layrContext, Object target) throws ServletException, IOException {
     	LifeCycle layrLifeCycle = new LifeCycle();
     	layrLifeCycle.setTargetInstance(target);
     	layrLifeCycle.setLayrRequestContext(layrContext);
     	layrLifeCycle.run();
     }
 
 	@Override
 	public void destroy() {
 		filterConfig = null;
 		layrApplicationContext = null;
 	}
 
 	public FilterConfig getFilterConfig() {
 		return filterConfig;
 	}
 
 	public void setFilterConfig(FilterConfig filterConfig) {
 		this.filterConfig = filterConfig;
 	}
 
 }
