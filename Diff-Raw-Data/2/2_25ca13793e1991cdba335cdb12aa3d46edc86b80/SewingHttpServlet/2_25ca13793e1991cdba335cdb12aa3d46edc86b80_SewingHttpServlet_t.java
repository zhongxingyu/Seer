 /*
  * Sewing: a Simple framework for Embedded-OSGi Web Development
  * Copyright (C) 2009 Bug Labs
  * Email: bballantine@buglabs.net
  * Site: http://www.buglabs.net
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA  02111-1307, USA.
  */
 
 package com.buglabs.osgi.sewing.pub;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.osgi.sewing.LogManager;
 import com.buglabs.osgi.sewing.TemplateIncludesCache;
 import com.buglabs.osgi.sewing.pub.util.ControllerMap;
 import com.buglabs.osgi.sewing.pub.util.RequestHelper;
 import com.buglabs.osgi.sewing.pub.util.RequestParameters;
 
 import freemarker.template.InputSource;
 import freemarker.template.SimpleHash;
 import freemarker.template.SimpleScalar;
 import freemarker.template.Template;
 import freemarker.template.TemplateModelRoot;
 
 /**
  * This is the main Sewing framework servlet class. clients are expected to
  * extend this abstract class and implement the <code>getControllerMap()</code>
  * method. This class Servlet is then registered with the OSGi runtime using
  * ISewingService.
  * 
  * Then, for each page a client would like to publish, the client must create
  * SewingController classes (typically as inner classes of the client's
  * SewingHttpServlet implementation). The url mapping (url -> controller) is
  * done in the getControllerMap() method.
  * 
  * @author brian
  * 
  *         UPDATE: 2010-07-14 akweon: added beforeGet() and beforePost() in
  *         processRequest
  * 
  */
 public abstract class SewingHttpServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	public static final int GET = 0;
 	public static final int POST = 1;
 	private static final String ERROR_404 = "The resource you requested was not found.";
 	private static final String ERROR_500 = "There was an application error.";
 	private static final String ERROR_REDIRECT = "There was a redirect error.";
 	private static final String TEMPLATE_EXT = ".fml";
 	private static final String TEMPLATES_ALIAS = "templates";
 	private static final String IMAGES_ALIAS = "images";
 	private static final String STYLESHEET_ALIAS = "stylesheets";
 	private static final String JAVASCRIPT_ALIAS = "javascripts";
 	private static final String INCLUDES_ALIAS = "includes";
 
 	private static final String ASSET_ROOT_KEY = "_assetRoot";
 	private static final String IMAGE_ROOT_KEY = "_imageRoot";
 	private static final String STYLESHEET_ROOT_KEY = "_stylesheetRoot";
 	private static final String JAVASCRIPT_ROOT_KEY = "_javascriptRoot";
 
 	private BundleContext bundle_context = null;
 	private String servlet_alias = null;
 	private ControllerMap controller_map = null;
 
 	/**
 	 * This is called during setup. Your implementation needs to map the inner
 	 * class controllers to a string name which is what shows up on the URL.
 	 * 
 	 * @return
 	 */
 	public abstract ControllerMap getControllerMap();
 
 	/**
 	 * This is called by the Service framework when the servlet is registered
 	 * this sets up the servlet resources and stuff
 	 * 
 	 * @param bundleContext
 	 * @param httpService
 	 * @param servletAlias
 	 */
 	public final void setup(BundleContext bundleContext, HttpService httpService, String servletAlias) {
 		bundle_context = bundleContext;
 		servlet_alias = servletAlias;
 		controller_map = getControllerMap();
 
 		try {
 			// register servlet
 			httpService.registerServlet(servletAlias, this, null, null);
 
 			// Register images
 			httpService.registerResources(servlet_alias + "." + IMAGES_ALIAS, "", new ImagesHttpContext(bundle_context));
 
 			// Register stylesheets
 			httpService.registerResources(servlet_alias + "." + STYLESHEET_ALIAS, "", new StylesheetsHttpContext(bundle_context));
 
 			// Register javascripts
 			httpService.registerResources(servlet_alias + "." + JAVASCRIPT_ALIAS, "", new JavascriptsHttpContext(bundle_context));
 
 		} catch (ServletException e) {
 			LogManager.log(LogService.LOG_ERROR, "Servlet Failed.", e);
 		} catch (NamespaceException e) {
 			LogManager.log(LogService.LOG_ERROR, "Unable to register resources.", e);
 		}
 	}
 
 	/**
 	 * called by the service framework to unregiser everything
 	 * 
 	 * @param httpService
 	 */
 	public final void teardown(HttpService httpService) {
 		try {
 			httpService.unregister(servlet_alias);
 		} catch (IllegalArgumentException e) {
 		}
 		if (!servlet_alias.endsWith("/")) {
 			try {
 				httpService.unregister(servlet_alias + "/");
 			} catch (IllegalArgumentException e) {
 			}
 		}
 		try {
 			httpService.unregister(servlet_alias + "." + IMAGES_ALIAS);
 		} catch (IllegalArgumentException e) {
 		}
 		try {
 			httpService.unregister(servlet_alias + "." + STYLESHEET_ALIAS);
 		} catch (IllegalArgumentException e) {
 		}
 		try {
 			httpService.unregister(servlet_alias + "." + JAVASCRIPT_ALIAS);
 		} catch (IllegalArgumentException e) {
 		}
 	}
 
 	/**
 	 * Handles the GET request for SewingHttpServlet Do not override if you hope
 	 * to use the framework as it was designed
 	 */
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
 		try {
 			processRequest(GET, req, resp);
 		} catch (Exception e) {
 			LogManager.log(LogService.LOG_ERROR, "Unable to process GET request.", e);
 			renderError(500, resp);
 			return;
 		}
 	}
 
 	/**
 	 * Handles the POST request for SewingHttpServlet Do not override if you
 	 * hope to use the framework as it was designed
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
 		try {
 			processRequest(POST, req, resp);
 		} catch (Exception e) {
 			LogManager.log(LogService.LOG_ERROR, "Unable to process POST request.", e);
 			renderError(500, resp);
 			return;
 		}
 	}
 
 	/**
 	 * This one does all the work, called by both doGet and doPost
 	 * 
 	 * @param type
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	private void processRequest(int type, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		String controllerPath = req.getPathInfo();
 		// set up controllerPath to be index if no controller specified in url
 		if (controllerPath == null)
 			controllerPath = "/index";
 		String controllerName = controllerPath.substring(1, controllerPath.length());
 		RequestParameters params = getRequestParams(req);
 
 		processRequest(type, controllerName, params, req, resp);
 	}
 
 	/**
 	 * Broke down processRequest into a function that takes all of it's moving
 	 * parts explicitly.
 	 * 
 	 * @param type
 	 * @param controllerName
 	 * @param params
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	private void processRequest(int type, String controllerName, RequestParameters params, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		SewingController controller = getController(controllerName);
 
 		if (controller == null) {
 			renderError(404, resp);
 			return;
 		}
 
 		TemplateModelRoot root = null;
 		// synchronize this section on controller
 		// this is because one controller instance can be shared across requests
 		// and, though the servlet container on BUG is currently
 		// single-threaded,
 		// in the future, there could be synchronization problems, especially
 		// with re-direct
 		synchronized (controller) {
 			try {
 				if (type == GET) {
 					controller.beforeGet(params, req, resp);
 					if (!controller.getSkipAction()) {
 						root = controller.get(params, req, resp);
 					}
 				} else if (type == POST) {
 					controller.beforePost(params, req, resp);
 					if (!controller.getSkipAction()) {
 						root = controller.post(params, req, resp);
 					}
 				}
 				// check if the controller asked for a redirect and do it if
 				// redirect, root is ignored
 				if (controller.doRedirect() && controller.getRedirectInfo() != null) {
 					handleRedirect(controller, resp);
 					return;
 				}
 			} finally {
 				controller.clearRedirect();
 			}
 		}
 		if (root == null)
 			root = new SimpleHash();
 		setDefaultValues(root);
 
 		Template t = getTemplate(controller, controllerName);
 		t.setCache(new TemplateIncludesCache(bundle_context, INCLUDES_ALIAS));
 
 		resp.setContentType("text/html");
 		Writer out = resp.getWriter();
 		t.process(root, out);
 	}
 
 	/**
 	 * helper for doing a processRequest with a RedirectInfo object
 	 * 
 	 * @param redirectInfo
 	 * @throws IOException
 	 */
 	private void processRequest(RedirectInfo redirectInfo) throws IOException {
 		processRequest(redirectInfo.getRequestType(), redirectInfo.getControllerName(), redirectInfo.getParams(), redirectInfo.getRequestObject(), redirectInfo.getResponseObject());
 	}
 
 	private void handleRedirect(SewingController requestController, HttpServletResponse resp) throws IOException {
 		// make sure we're not trying to redirect to self
 		if (requestController.equals(getController(requestController.getRedirectInfo().getControllerName()))) {
 			resp.sendError(500, ERROR_REDIRECT + " A controller cannot redirect to itself");
 			return;
 		}
 		if (requestController.getRedirectInfo().getUrl() != null) {
 			resp.setContentType("text/html");
 			Writer out = resp.getWriter();
 			out.write(requestController.getRedirectInfo().getRedirectHtml());
 		} else {
 			processRequest(requestController.getRedirectInfo());
 		}
 		requestController.clearRedirect();
 	}
 
 	private RequestParameters getRequestParams(HttpServletRequest req) {
 		if (RequestHelper.isMultipart(req))
 			return RequestHelper.parseMultipart(req);
 		else
 			return RequestHelper.parseParams(req);
 	}
 
 	/**
 	 * Sets up some defaults in the templates that implementers can use to
 	 * access images, javascripts, etc.
 	 * 
 	 * @param root
 	 */
 	private void setDefaultValues(TemplateModelRoot root) {
 		root.put(ASSET_ROOT_KEY, new SimpleScalar(servlet_alias + "."));
 		root.put(IMAGE_ROOT_KEY, new SimpleScalar(servlet_alias + "." + IMAGES_ALIAS));
 		root.put(STYLESHEET_ROOT_KEY, new SimpleScalar(servlet_alias + "." + STYLESHEET_ALIAS));
 		root.put(JAVASCRIPT_ROOT_KEY, new SimpleScalar(servlet_alias + "." + JAVASCRIPT_ALIAS));
 	}
 
 	private SewingController getController(String controllerName) {
 		if (controller_map == null)
 			controller_map = getControllerMap();
 		if (controller_map == null)
 			return null;
 		return controller_map.get(controllerName);
 	}
 
 	private Template getTemplate(SewingController controller, String controllerName) throws IOException {
 		String templateName = controller.getTemplateName();
 		if (templateName == null)
 			templateName = controllerName + TEMPLATE_EXT;
 
 		URL templateUrl = bundle_context.getBundle().getResource("/" + TEMPLATES_ALIAS + "/" + templateName);
 
 		InputSource inputSource = new InputSource(new InputStreamReader(templateUrl.openStream()));
 
 		return new Template(inputSource);
 	}
 
 	/**
 	 * call this to render an http error page in the browser
 	 * 
 	 * @param errorNum
 	 * @param req
 	 * @param resp
 	 */
 	private void renderError(int errorNum, HttpServletResponse resp) {
 		String message = ERROR_500;
 		if (errorNum == 404)
 			message = ERROR_404;
 
 		try {
 			resp.sendError(errorNum, message);
 		} catch (IOException e) {
 			LogManager.log(LogService.LOG_ERROR, "Unable to process error response.", e);
 		}
 	}
 
 	/**
 	 * images, javascripts, and stylesheets are assets this HttpContext is
 	 * implemented for each of the asset types to properly server up the
 	 * resource type from the right spot
 	 * 
 	 * @author brian
 	 * 
 	 */
 	private abstract class AssetsHttpContext implements HttpContext {
 		protected BundleContext context;
 
 		public AssetsHttpContext(BundleContext context) {
 			this.context = context;
 		}
 
 		public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) {
 			return true;
 		}
 
 		public String getMimeType(String name) {
 			return null;
 		}
 
 		public URL getResource(String path) {
 			// get first slash after first character
 			// (which is probably also a slash we don't want)
 			int slash = path.indexOf('/', 1);
 
 			String file = path;
 			if (slash > -1) {
 				file = path.substring(slash);
 			}
 
			return context.getBundle().getResource(getResourcePath() + "/" + file);
 		}
 
 		protected abstract String getResourcePath();
 	}
 
 	private class ImagesHttpContext extends AssetsHttpContext {
 
 		public ImagesHttpContext(BundleContext context) {
 			super(context);
 		}
 
 		protected String getResourcePath() {
 			return "/" + IMAGES_ALIAS;
 		}
 	}
 
 	private class StylesheetsHttpContext extends AssetsHttpContext {
 
 		public StylesheetsHttpContext(BundleContext context) {
 			super(context);
 		}
 
 		protected String getResourcePath() {
 			return "/" + STYLESHEET_ALIAS;
 		}
 	}
 
 	private class JavascriptsHttpContext extends AssetsHttpContext {
 
 		public JavascriptsHttpContext(BundleContext context) {
 			super(context);
 		}
 
 		protected String getResourcePath() {
 			return "/" + JAVASCRIPT_ALIAS;
 		}
 	}
 
 }
