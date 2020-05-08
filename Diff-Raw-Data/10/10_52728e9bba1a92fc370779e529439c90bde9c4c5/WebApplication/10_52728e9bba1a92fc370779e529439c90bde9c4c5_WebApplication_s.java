 package com.yiij.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.yiij.base.Application;
 import com.yiij.base.Component;
 import com.yiij.base.ComponentConfig;
 import com.yiij.base.HttpException;
 import com.yiij.base.Module;
 import com.yiij.base.interfaces.IContext;
 import com.yiij.base.interfaces.IWebApplication;
 import com.yiij.utils.StringHelper;
 import com.yiij.web.interfaces.IApplicationViewRenderer;
 import com.yiij.web.interfaces.IViewRenderer;
 import com.yiij.web.interfaces.IWebComponent;
 import com.yiij.web.interfaces.IWebModule;
 import com.yiij.web.actions.Action;
 import com.yiij.web.renderers.ClassRenderer;
 
 public class WebApplication extends Application implements IWebApplication,
 		IWebModule, IWebComponent
 {
 	private ServletConfig _servletConfig;
 	private HttpServletRequest _servletRequest;
 	private HttpServletResponse _servletResponse;
 
 	private String _viewPath;
 	private String _defaultController = "site";
 	private String _layout = "main";
 	private UrlManager _urlManager;
 	private HttpRequest _request;
 	private HttpResponse _response;
 	private IApplicationViewRenderer _viewRenderer;
 	private Controller _controller;
 	private String _controllerPath;
 	private String _layoutPath;
 	private String _homeUrl;
 
 	public WebApplication(IContext context, ServletConfig servletConfig,
 			HttpServletRequest servletRequest,
 			HttpServletResponse servletResponse)
 	{
 		super(context);
 		_servletConfig = servletConfig;
 		_servletRequest = servletRequest;
 		_servletResponse = servletResponse;
 
		setBasePath(_servletConfig.getServletContext().getRealPath("/"));
 		setPathOfAlias("application", getBasePath());
 	}
 
 	/**
 	 * @return the route of the default controller, action or module. Defaults
 	 *         to 'site'.
 	 */
 	@Override
 	public String getDefaultController()
 	{
 		return _defaultController;
 	}
 
 	/**
 	 * @see #getDefaultController()
 	 * @param
 	 */
 	public void setDefaultController(String value)
 	{
 		_defaultController = value;
 	}
 
 	/**
 	 * The application-wide layout. Defaults to 'main' (relative to
 	 * {@link getLayoutPath layoutPath}). If this is blank, then no layout will
 	 * be used.
 	 * 
 	 * @return
 	 */
 	public String getLayout()
 	{
 		return _layout;
 	}
 
 	/**
 	 * @see #getLayout()
 	 * @param
 	 */
 	public void setLayout(String value)
 	{
 		_layout = value;
 	}
 
 	/**
 	 * Processes the current request. It first resolves the request into
 	 * controller and action, and then creates the controller to perform the
 	 * action.
 	 */
 	@Override
 	public void processRequest() throws Exception
 	{
 		String route = getUrlManager().parseUrl(getRequest());
 		runController(route);
 	}
 
 	/**
 	 * Registers the core application components. This method overrides the
 	 * parent implementation by registering additional core components.
 	 * 
 	 * @see #setComponents()
 	 */
 	@Override
 	protected void registerCoreComponents()
 	{
 		super.registerCoreComponents();
 
 		ComponentConfig config = new ComponentConfig();
 		config.put("urlManager", new ComponentConfig("com.yiij.web.UrlManager"));
 		config.put("request", new ComponentConfig("com.yiij.web.HttpRequest"));
 		config.put("response", new ComponentConfig("com.yiij.web.HttpResponse"));
 		config.put("viewRenderer", new ComponentConfig("com.yiij.web.renderers.JTMERenderer"));
 
 		setComponents(config);
 	}
 
 	/**
 	 * Returns the request component.
 	 * 
 	 * @return the request component
 	 */
 	public HttpRequest getRequest()
 	{
 		if (_request == null)
 			try
 			{
 				_request = (HttpRequest) getComponent("request");
 			} catch (Exception e)
 			{
 				_request = null; // should never happen
 			}
 		return _request;
 	}
 
 	/**
 	 * Returns the response component.
 	 * 
 	 * @return the response component
 	 */
 	public HttpResponse getResponse()
 	{
 		if (_response == null)
 			try
 			{
 				_response = (HttpResponse) getComponent("response");
 			} catch (Exception e)
 			{
 				_response = null; // should never happe
 			}
 		return _response;
 	}
 
 	/**
 	 * Returns the URL manager component.
 	 * 
 	 * @return the URL manager component
 	 */
 	public UrlManager getUrlManager()
 	{
 		if (_urlManager == null)
 			try
 			{
 				_urlManager = (UrlManager) getComponent("urlManager");
 			} catch (Exception e)
 			{
 				_urlManager = null; // should never happen
 			}
 		return _urlManager;
 	}
 
 	/**
 	 * Returns the view renderer. If this component is registered and enabled,
 	 * the default view rendering logic defined in {@link BaseController} will
 	 * be replaced by this renderer.
 	 * 
 	 * @return the view renderer.
 	 */
 	public IApplicationViewRenderer getViewRenderer()
 	{
 		if (_viewRenderer == null)
 			try
 			{
 				_viewRenderer = (IApplicationViewRenderer) getComponent("viewRenderer");
 				if (_viewRenderer == null)
 					_viewRenderer = new ClassRenderer(context());
 			} catch (Exception e)
 			{
 				throw new com.yiij.base.Exception(e);
 			}
 		return _viewRenderer;
 	}
 
 	/**
 	 * Creates the controller and performs the specified action.
 	 * 
 	 * @param route
 	 *            the route of the current request. See
 	 *            {@link #createController()} for more details.
 	 * @throws HttpException
 	 *             if the controller could not be created.
 	 */
 	public void runController(String route) throws Exception
 	{
 		Object[] ca = createController(route);
 
 		if (ca != null && ca.length == 2)
 		{
 			Controller controller = (Controller) ca[0];
 			String actionID = (String) ca[1];
 
 			// list($controller,$actionID)=$ca;
 			// $oldController=$this->_controller;
 			// $this->_controller=$controller;
 			controller.init();
 			controller.run(actionID);
 			// $this->_controller=$oldController;
 		} else
 			throw new HttpException(404, "Unable to resolve the request '"
 					+ route + "'.");
 
 	}
 
 	/**
 	 * @see #createController(String, Module)
 	 */
 	public Object[] createController(String route) throws java.lang.Exception
 	{
 		return createController(route, null);
 	}
 
 	/**
 	 * Creates a controller instance based on a route. The route should contain
 	 * the controller ID and the action ID. It may also contain additional GET
 	 * variables. All these must be concatenated together with slashes.
 	 * 
 	 * This method will attempt to create a controller in the following order:
 	 * <ol>
 	 * <li>If the first segment is found in {@link #getControllerMap()}, the
 	 * corresponding controller configuration will be used to create the
 	 * controller;</li>
 	 * <li>If the first segment is found to be a module ID, the corresponding
 	 * module will be used to create the controller;</li>
 	 * <li>Otherwise, it will search under the {@link #getControllerPath()} to
 	 * create the corresponding controller. For example, if the route is
 	 * "admin/user/create", then the controller will be created using the class
 	 * file "protected/controllers/admin/UserController.php".</li>
 	 * </ol>
 	 * 
 	 * @param route
 	 *            the route of the request.
 	 * @param owner
 	 *            the module that the new controller will belong to. Defaults to
 	 *            null, meaning the application instance is the owner.
 	 * @return the controller instance and the action ID. Null if the controller
 	 *         class does not exist or the route is invalid.
 	 */
 	public Object[] createController(String route, Module owner)
 			throws java.lang.Exception
 	{
 		if (owner == null)
 			owner = this;
 		if (!(owner instanceof IWebModule))
 			throw new com.yiij.base.Exception(
 					"Controller can only be added to IWebModule classes");
 
 		if (StringUtils.stripEnd(route, "/").equals(""))
 		{
 			route = ((IWebModule) owner).getDefaultController();
 		}
 		boolean caseSensitive = getUrlManager().getCaseSensitive();
 		String basePath = null;
 		String controllerID = null;
 
 		route += "/";
 		int pos;
 		while ((pos = route.indexOf("/")) != -1)
 		{
 			String id = route.substring(0, pos);
 			if (!caseSensitive)
 				id = id.toLowerCase();
 			route = route.substring(pos + 1);
 			if (basePath == null) // first segment
 			{
 				Module module = owner.getModule(id);
 				if (module != null)
 					return createController(route, module);
 
 				basePath = owner.getPackageName();
 				controllerID = "";
 			} else
 			{
 				controllerID += "/";
 			}
 
 			String className = StringHelper.upperCaseFirst(id) + "Controller";
 			String classFile = basePath + "." + className;
 
 			try
 			{
 				Class<?> classClass = Class.forName(classFile);
 				if (Controller.class.isAssignableFrom(classClass))
 				{
 					Controller controller = (Controller)Component.newInstance(context(),
 							classClass.getCanonicalName(),
 							new Object[] { controllerID + id,
 									owner == this ? null : owner },
 							new Class[] { String.class,
 									IWebModule.class });
 					return new Object[] {
 							controller,
 							parseActionParams(route) };
 				}
 				return null;
 			} catch (ClassNotFoundException e)
 			{
 			}
 			controllerID += id;
 			// basePath = "/" + id;
 		}
 		return null;
 	}
 
 	/**
 	 * Parses a path info into an action ID and GET variables.
 	 * 
 	 * @param pathInfo
 	 *            path info
 	 * @return action ID
 	 */
 	protected String parseActionParams(String pathInfo) throws Exception
 	{
 		int pos;
 		if ((pos = pathInfo.indexOf("/")) != -1)
 		{
 			UrlManager manager = getUrlManager();
 			manager.parsePathInfo(pathInfo.substring(pos + 1));
 			String actionID = pathInfo.substring(0, pos);
 			return manager.getCaseSensitive() ? actionID : actionID.toLowerCase();
 		} else
 			return pathInfo;
 	}
 
 	/**
 	 * @return the currently active controller
 	 */
 	public Controller getController()
 	{
 		return _controller;
 	}
 
 	/**
 	 * @param value
 	 *            the currently active controller
 	 */
 	public void setController(Controller value)
 	{
 		_controller = value;
 	}
 
 	/**
 	 * @return the package name that contains the controller classes. Defaults
 	 *         to 'protected/controllers'.
 	 */
 	public String getControllerPath()
 	{
 		if (_controllerPath != null)
 			return _controllerPath;
 		else
 			return _controllerPath = getPackageName();
 	}
 
 	/**
 	 * @param value
 	 *            the package name that contains the controller classes.
 	 * @throws Exception
 	 *             if the directory is invalid
 	 */
 	public void setControllerPath(String value)
 	{
 		_controllerPath = value;
 	}
 
 	/*
 	public String getViewPackageName()
 	{
 		if (_viewPackageName == null)
 			_viewPackageName = getPackageName() + ".views";
 		return _viewPackageName;
 	}
 
 	public void setViewPackageName(String value)
 	{
 		_viewPackageName = value;
 	}
 	*/
 
 	/**
 	 * @return the root directory of view files. Defaults to 'protected/views'.
 	 */
 	public String getViewPath()
 	{
 		if (_viewPath != null)
 			return _viewPath;
 		else
 		{
 			IViewRenderer renderer = webApp().getViewRenderer();
 			if (renderer.getFileExtension()!=null)
 				return _viewPath=getBasePath()+"/views";
 			else
 				return _viewPath=".views";
 		}
 	}
 
 	/**
 	 * @param path
 	 *            the root directory of view files.
 	 * @throws Exception
 	 *             if the directory does not exist.
 	 */
 	public void setViewPath(String path)
 	{
 		_viewPath = path;
 		/*
 		 * if(($this->_viewPath=realpath($path))===false ||
 		 * !is_dir($this->_viewPath)) throw new CException(Yii::t('yii','The
 		 * view path "{path}" is not a valid directory.',
 		 * array('{path}'=>$path)));
 		 */
 	}
 
 	/**
 	 * @return the root directory of layout files. Defaults to
 	 *         'protected/views/layouts'.
 	 */
 	public String getLayoutPath()
 	{
 		if (_layoutPath != null)
 			return _layoutPath;
 		else
 		{
 			IViewRenderer renderer = webApp().getViewRenderer();
 			if (renderer.getFileExtension()!=null)
 				return _layoutPath = getViewPath() + "/layouts";
 			else
 				return _layoutPath = getViewPath() + ".Layouts";
 		}
 	}
 
 	/**
 	 * @param path
 	 *            the root directory of layout files.
 	 * @throws Exception
 	 *             if the directory does not exist.
 	 */
 	public void setLayoutPath(String path)
 	{
 		_layoutPath = path;
 		/*
 		 * if(($this->_layoutPath=realpath($path))===false ||
 		 * !is_dir($this->_layoutPath)) throw new CException(Yii::t('yii','The
 		 * layout path "{path}" is not a valid directory.',
 		 * array('{path}'=>$path)));
 		 */
 	}
 
 	/**
 	 * The pre-filter for controller actions. This method is invoked before the
 	 * currently requested controller action and all its filters are executed.
 	 * You may override this method with logic that needs to be done before all
 	 * controller actions.
 	 * 
 	 * @param controller
 	 *            the controller
 	 * @param action
 	 *            the action
 	 * @return whether the action should be executed.
 	 */
 	public boolean beforeControllerAction(Controller controller, Action action)
 	{
 		return true;
 	}
 
 	/**
 	 * The post-filter for controller actions. This method is invoked after the
 	 * currently requested controller action and all its filters are executed.
 	 * You may override this method with logic that needs to be done after all
 	 * controller actions.
 	 * 
 	 * @param controller
 	 *            the controller
 	 * @param action
 	 *            the action
 	 */
 	public void afterControllerAction(Controller controller, Action action)
 	{
 	}
 
 	/**
 	 * Initializes the application. This method overrides the parent
 	 * implementation by preloading the 'request' component.
 	 */
 	@Override
 	public void init()
 	{
 		super.init();
 		// preload 'request' so that it has chance to respond to onBeginRequest
 		// event.
 		getRequest();
 	}
 
 	/**
 	 * Returns the directory that stores runtime files.
 	 * 
 	 * @return the directory that stores runtime files. Defaults to
 	 *         'protected/runtime'.
 	 */
 	public String getRuntimePath()
 	{
 		String rp = super.getRuntimePath();
 		if (rp == null)
 		{
 			rp = getServletConfig().getServletContext().getAttribute(
 					"javax.servlet.context.tmpdir")
 					+ "/runtime";
 			setRuntimePath(rp);
 		}
 		return rp;
 	}
 
 	/**
 	 * @see #createUrl(String, Map, String)
 	 */
 	public String createUrl(String route)
 	{
 		return createUrl(route, null, "&");
 	}
 
 	/**
 	 * @see #createUrl(String, Map, String)
 	 */
 	public String createUrl(String route, Map<String, String> params)
 	{
 		return createUrl(route, params, "&");
 	}
 
 	/**
 	 * Creates a relative URL based on the given controller and action
 	 * information.
 	 * 
 	 * @param route
 	 *            the URL route. This should be in the format of
 	 *            'ControllerID/ActionID'.
 	 * @param params
 	 *            additional GET parameters (name=>value). Both the name and
 	 *            value will be URL-encoded.
 	 * @param ampersand
 	 *            the token separating name-value pairs in the URL.
 	 * @return the constructed URL
 	 */
 	public String createUrl(String route, Map<String, String> params,
 			String ampersand)
 	{
 		return getUrlManager().createUrl(route, params, ampersand);
 	}
 
 	/**
 	 * Creates an absolute URL based on the given controller and action
 	 * information.
 	 * 
 	 * @param string
 	 *            $route the URL route. This should be in the format of
 	 *            'ControllerID/ActionID'.
 	 * @param array
 	 *            $params additional GET parameters (name=>value). Both the name
 	 *            and value will be URL-encoded.
 	 * @param string
 	 *            $schema schema to use (e.g. http, https). If empty, the schema
 	 *            used for the current request will be used.
 	 * @param string
 	 *            $ampersand the token separating name-value pairs in the URL.
 	 * @return string the constructed URL
 	 */
 	public String createAbsoluteUrl(String route, Map<String, String> params,
 			String schema, String ampersand)
 	{
 		String url = createUrl(route, params, ampersand);
 		if (url.startsWith("http"))
 			return url;
 		else
 			return getRequest().getHostInfo(schema) + url;
 	}
 
 	/**
 	 * see #getBaseUrl(boolean)
 	 */
 	public String getBaseUrl()
 	{
 		return getBaseUrl(false);
 	}
 
 	/**
 	 * Returns the relative URL for the application. This is a shortcut method
 	 * to {@link HttpRequest#getBaseUrl()}.
 	 * 
 	 * @param boolean $absolute whether to return an absolute URL. Defaults to
 	 *        false, meaning returning a relative one.
 	 * @return string the relative URL for the application
 	 * @see HttpRequest#getBaseUrl()
 	 */
 	public String getBaseUrl(boolean absolute)
 	{
 		return getRequest().getBaseUrl(absolute);
 	}
 
 	/**
 	 * @return the homepage URL
 	 */
 	public String getHomeUrl()
 	{
 		if (_homeUrl == null)
 		{
 			return getRequest().getBaseUrl() + "/";
 		} else
 			return _homeUrl;
 	}
 
 	/**
 	 * @param value
 	 *            the homepage URL
 	 */
 	public void setHomeUrl(String value)
 	{
 		_homeUrl = value;
 	}
 
 	@Override
 	public PrintWriter out() throws IOException
 	{
 		return getResponse().getWriter();
 	}
 	
 	@Override
 	public ServletConfig getServletConfig()
 	{
 		return _servletConfig;
 	}
 
 	@Override
 	public HttpServletRequest getServletRequest()
 	{
 		return _servletRequest;
 	}
 
 	@Override
 	public HttpServletResponse getServletResponse()
 	{
 		return _servletResponse;
 	}
 
 	@Override
 	public WebApplication webApp()
 	{
 		return this;
 	}
 }
