 /** 
  * @(#)DispatcherServlet.java
  */
 package framework.action;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import framework.cache.Cache;
 import framework.util.StringUtil;
 
 /** 
  * Ʈѷ  ϴ   Ŭ̾Ʈ û ޾ ش ׼ Ѵ.
  * web.xml Ͽ  Ͽ ϸ  ý Ѱ ü  ´.  
  * û  ׼Ű routes.properties ControllerŬ ã ü Ͽ Ͻ μ Ѵ. 
  */
 public class DispatcherServlet extends HttpServlet {
 	private static final long serialVersionUID = -6478697606075642071L;
 	private static Log _logger = LogFactory.getLog(framework.action.DispatcherServlet.class);
 	private static final String[] _DEFAULT_SERVLET_NAMES = new String[] { "default", "WorkerServlet", "FileServlet", "resin-file", "SimpleFileServlet", "_ah_default" };
	private String _defaultServletName = null;
 
 	/**
 	 *  ü ʱȭ Ѵ.
 	 * web.xml ʱȭ Ķͷ ϵǾ ִ routes-mapping  ã ҽ  ϴ  Ѵ.
 	 * @param config ServletConfig ü
 	 */
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		ResourceBundle bundle = null;
 		try {
 			bundle = ResourceBundle.getBundle(config.getInitParameter("routes-mapping"));
 			_defaultServletName = StringUtil.nullToBlankString(config.getInitParameter("default-servlet-name"));
 			if ("".equals(_defaultServletName)) {
 				for (String servletName : _DEFAULT_SERVLET_NAMES) {
 					if (getServletContext().getNamedDispatcher(servletName) != null) {
 						_defaultServletName = servletName;
 						break;
 					}
 				}
 			}
 			if (getServletContext().getNamedDispatcher(_defaultServletName) == null) {
 				throw new IllegalStateException("defaultServletName Error!");
 			}
 		} catch (MissingResourceException e) {
 			throw new ServletException(e);
 		}
 		getServletContext().setAttribute("routes-mapping", bundle);
 		// Cache
 		Cache.init();
 	}
 
 	/**
 	 * Ŭ̾Ʈ Get  û  processRequest ó ̰Ѵ.
 	 * @param request HTTP Ŭ̾Ʈ ûü
 	 * @param response HTTP Ŭ̾Ʈ 䰴ü
 	 * @exception java.io.IOException DispatcherServlet IO õ  ߻  
 	 * @exception javax.servlet.ServletException  õ  ߻ 
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		_processRequest(request, response);
 	}
 
 	/**
 	 * Ŭ̾Ʈ Post  û  processRequest ó ̰Ѵ.
 	 * @param request HTTP Ŭ̾Ʈ ûü
 	 * @param response HTTP Ŭ̾Ʈ 䰴ü
 	 * @exception java.io.IOException Servlet IO õ  ߻  
 	 * @exception javax.servlet.ServletException  õ  ߻ 
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		_processRequest(request, response);
 	}
 
 	/**
 	 * Ŭ̾Ʈ Put  û  processRequest ó ̰Ѵ.
 	 * @param request HTTP Ŭ̾Ʈ ûü
 	 * @param response HTTP Ŭ̾Ʈ 䰴ü
 	 * @exception java.io.IOException DispatcherServlet IO õ  ߻  
 	 * @exception javax.servlet.ServletException  õ  ߻ 
 	 */
 	@Override
 	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		_processRequest(request, response);
 	}
 
 	/**
 	 * Ŭ̾Ʈ Delete  û  processRequest ó ̰Ѵ.
 	 * @param request HTTP Ŭ̾Ʈ ûü
 	 * @param response HTTP Ŭ̾Ʈ 䰴ü
 	 * @exception java.io.IOException DispatcherServlet IO õ  ߻  
 	 * @exception javax.servlet.ServletException  õ  ߻ 
 	 */
 	@Override
 	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		_processRequest(request, response);
 	}
 
 	//////////////////////////////////////////////////////////////////////////////////////////Private ޼ҵ
 
 	private void _processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			Controller controller = null;
 			Method actionMethod = null;
 			try {
 				String routePath = _getRoutePath(request);
 				String[] controllerAndAction = _getControllerAndAction(routePath);
 				String controllerName = controllerAndAction[0];
 				String actionName = controllerAndAction[1];
 				Class<?> controllerClass = Class.forName(controllerName);
 				controller = (Controller) controllerClass.newInstance();
 				actionMethod = controllerClass.getMethod(actionName);
 				if (!Modifier.isPublic(actionMethod.getModifiers())) { // Public ޼ҵ常 ȣⰡ
 					throw new Exception("This is not a public method.");
 				}
 			} catch (Exception e) {
 				this.getServletContext().getNamedDispatcher(_defaultServletName).forward(request, response);
 				return;
 			}
 			long currTime = 0;
 			if (_getLogger().isDebugEnabled()) {
 				currTime = System.currentTimeMillis();
 				_getLogger().debug("ڡڡ " + request.getRemoteAddr() + "   \"" + request.getMethod() + " " + request.getRequestURI() + "\" û ۵Ǿϴ");
 				_getLogger().debug("ContentLength : " + request.getContentLength() + "bytes");
 			}
 			controller.execute(this, request, response, actionMethod);
 			if (_getLogger().isDebugEnabled()) {
 				_getLogger().debug("١١ " + request.getRemoteAddr() + "   \"" + request.getMethod() + " " + request.getRequestURI() + "\" û Ǿϴ | duration : " + (System.currentTimeMillis() - currTime) + "ms\n");
 			}
 		} catch (Exception e) {
 			_getLogger().error(e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String _getRoutePath(HttpServletRequest request) {
 		String path = request.getServletPath() + StringUtil.nullToBlankString(request.getPathInfo());
 		String normalizePath = path.replaceAll("/+", "/");
 		return normalizePath;
 	}
 
 	private String[] _getControllerAndAction(String routePath) {
 		try {
 			ResourceBundle bundle = (ResourceBundle) getServletContext().getAttribute("routes-mapping");
 			String value = ((String) bundle.getObject(routePath)).trim();
 			int period = value.lastIndexOf(".");
 			return new String[] { value.substring(0, period), value.substring(period + 1) };
 		} catch (MissingResourceException e) {
 			return null;
 		}
 	}
 
 	private Log _getLogger() {
 		return DispatcherServlet._logger;
 	}
 }
