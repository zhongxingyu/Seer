 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.app.controllers;
 
 import static org.oobium.app.http.Action.showEdit;
 import static org.oobium.app.http.Action.showNew;
 import static org.oobium.app.http.MimeType.CSS;
 import static org.oobium.app.http.MimeType.JS;
 import static org.oobium.app.http.MimeType.JSON;
 import static org.oobium.app.sessions.Session.SESSION_ID_KEY;
 import static org.oobium.app.sessions.Session.SESSION_UUID_KEY;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.varName;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.json.JsonUtils.format;
 import static org.oobium.utils.json.JsonUtils.toJson;
 import static org.oobium.utils.json.JsonUtils.toMap;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.handler.codec.http.Cookie;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.oobium.app.AppService;
 import org.oobium.app.http.Action;
 import org.oobium.app.http.MimeType;
 import org.oobium.app.request.Request;
 import org.oobium.app.response.Response;
 import org.oobium.app.response.StaticResponse;
 import org.oobium.app.routing.AppRouter;
 import org.oobium.app.routing.IPathRouting;
 import org.oobium.app.routing.IUrlRouting;
 import org.oobium.app.routing.Realm;
 import org.oobium.app.routing.Router;
 import org.oobium.app.routing.handlers.AuthorizationHandler;
 import org.oobium.app.server.netty4.Attribute;
 import org.oobium.app.server.netty4.FileUpload;
 import org.oobium.app.server.netty4.HttpData;
 import org.oobium.app.sessions.Session;
 import org.oobium.app.views.DynamicAsset;
 import org.oobium.app.views.ScriptFile;
 import org.oobium.app.views.StyleSheet;
 import org.oobium.app.views.View;
 import org.oobium.app.views.ViewRenderer;
 import org.oobium.cache.CacheObject;
 import org.oobium.cache.CacheService;
 import org.oobium.logging.Logger;
 import org.oobium.persist.Model;
 import org.oobium.utils.Base64;
 
 public class Controller implements IFlash, IParams, IPathRouting, IUrlRouting, ISessions, IHttp {
 
 	private static final String AUTHENTICATED_AT = "authenticatedAt";
 	private static final String AUTHENTICATED_BY = "authenticatedBy";
 	private static final String AUTHENTICATED_BY_TYPE = "authenticatedByType";
 	private static final long AUTHENTICATION_INTERVAL = 1000*60*30; // 30 minutes
 	
 	public static final String FLASH_KEY = "oobium_flash";
 	public static final String FLASH_ERROR = "error";
 	public static final String FLASH_NOTICE = "notice";
 	public static final String FLASH_WARNING = "warning";
 	
 	static String createActionCacheKey(Class<? extends Controller> controller, Action action) {
 		return controller.getSimpleName() + "/" + action.name();
 	}
 	
 	static String createActionCacheKey(Class<? extends Controller> controller, Action action, MimeType type) {
 		return controller.getSimpleName() + "/" + action.name() + "." + type.extension();
 	}
 	
 	static String createCacheKey(AppService handler, String key) {
 		return handler.getName() + "/" + key;
 	}
 
 	/**
 	 * @return a model aware map of the parameters, or an empty map if params is null; never null
 	 */
 	@SuppressWarnings("unchecked")
 	static Map<String, Object> mapParams(Map<String, Object> params) {
 		if(params == null) {
 			return new HashMap<String, Object>(0);
 		}
 		
 		Map<String, Object> map = new HashMap<String, Object>();
 		
 		for(String param : params.keySet()) {
 			map.put(param, params.get(param));
 			String[] parts = splitParam(param);
 			if(parts.length > 1) {
 				Map<String, Object> m = map;
 				for(int i = 0; i < parts.length; i++) {
 					if(i == parts.length-1) {
 						m.put(parts[i], params.get(param));
 					} else {
 						if(!m.containsKey(parts[i])) {
 							m.put(parts[i], new HashMap<String, Object>());
 						}
 						m = (Map<String, Object>) m.get(parts[i]);
 					}
 				}
 			}
 		}
 		
 		return map;
 	}
 
 	static String[] splitParam(String param) {
 		return param.split("\\]\\[|\\[|\\]");
 	}
 
 
 	protected Logger logger;
 	private AppService handler;
 	private AppRouter appRouter;
 	private Router router;
 	private Action action;
 	protected Request request;
 	protected Response response;
 	private Map<String, Object> routeParams;
 	private Map<String, Object> params;
 	private Map<String, Object> flash;
 	private Map<String, Object> flashOut;
 	private Session session;
 	private boolean sessionResolved;
 	private boolean isRendered;
 	
 	public Controller() {
 		// no args constructor
 	}
 	
 	@Override
 	public boolean accepts(MimeType type) {
 		List<MimeType> requestTypes = request.getAcceptedTypes();
 		if(!blank(requestTypes)) {
 			for(MimeType accept : requestTypes) {
 				if(accept.resolves(type)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private void addFlashError(Model model) {
 		if(model.hasErrors()) {
 			if(flash == null || !flash.containsKey(FLASH_ERROR)) {
 				setFlashError(model);
 			} else {
 				Set<String> errors = new LinkedHashSet<String>();
 				Object error = flash.get(FLASH_ERROR);
 				if(error instanceof Collection<?>) {
 					for(Object o : (Collection<?>) error) {
 						errors.add(String.valueOf(o));
 					}
 				} else {
 					errors.add(String.valueOf(error));
 				}
 				for(String e : model.getErrorsList()) {
 					errors.add(e);
 				}
 				setFlashError(errors);
 			}
 		}
 	}
 	
 	/**
 	 * Code to be executed after the actual request handler.  This code
 	 * will be called whether or not there has been a render or redirect.
 	 * <p>All filters through out the class hierarchy will be called, starting with
 	 * the top most class; there is no need for an explicit call to super.afterFilter.</p>
 	 */
 	public void afterFilter(Action action) {
 		// subclasses to implement
 	}
 	
 	public boolean authenticate() {
 		resolveSession(true);
 		if(session != null) {
 			session.putData(AUTHENTICATED_AT, System.currentTimeMillis());
 			session.removeData(AUTHENTICATED_BY);
 			session.removeData(AUTHENTICATED_BY_TYPE);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean authenticate(Model model) {
 		if(model != null) {
 			resolveSession(true);
 			if(session != null) {
 				session.putData(AUTHENTICATED_AT, System.currentTimeMillis());
 				session.putData(AUTHENTICATED_BY, model.getId());
 				session.putData(AUTHENTICATED_BY_TYPE, model.getClass().getName());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * This method calls the {@link #authorize(String, String)} method, passing in the
 	 * username and password from the request. If the {@link #authorize(String, String)}
 	 * method returns false then a 401 - Not Authorized response will be rendered.  If called
 	 * from a beforeFilter (as intended) then execution will be stopped and the 401 response
 	 * will be returned.  If {@link #authorize(String, String)} returns true then this
 	 * method simply returns.  If the request does not contain at least a username the
 	 * {@link #authorize(String, String)} will not be called and this method will behave as
 	 * if it had returned false by rendering the 401 response.
 	 */
 	public void authorize() {
 		boolean authorized = false;
 		String header = request.getHeader(HttpHeaders.Names.AUTHORIZATION);
 		if(header != null && header.startsWith("Basic ")) {
 			String[] sa = new String(Base64.decode(header.substring(6).getBytes())).split(":");
 			if(sa.length == 1) {
 				authorized = authorize(sa[0], null);
 			} else if(sa.length == 2) {
 				authorized = authorize(sa[0], sa[1]);
 			}
 		}
 		if(!authorized) {
 			try {
 				rendering();
 				response = new AuthorizationHandler(router, "secure area").routeRequest(request);
 			} catch(Exception e) {
 				logger.warn(e);
 			}
 		}
 	}
 	
 	/**
 	 * Authorize a request that is being authenticated using basic authentication.
 	 * This method is called from the {@link #authorize()} method.  This method is intended to be
 	 * implemented by subclasses to add actual authorization rules.  To use this method, call
 	 * the authorize method from the {@link #beforeFilter(Action)} method.
 	 * @param username the username sent in the request; never null
 	 * @param password the password sent in the request; may be null
 	 * @return true if the username and password are authorized to continue; false otherwise
 	 */
 	public boolean authorize(String username, String password) {
 		return false;
 	}
 	
 	/**
 	 * Code to be executed before the actual request handler.  A filter that either
 	 * renders or redirects will end the execution chain and all subsequent filters
 	 * and the actual handler will not be run.
 	 * <p>All filters through out the class hierarchy will be called, starting with
 	 * the top most class; there is no need for an explicit call to super.beforeFilter.</p>
 	 */
 	public void beforeFilter(Action action) {
 		// subclasses to implement
 	}
 	
 	private void callFilters(boolean before) {
 		List<Class<?>> classes = new ArrayList<Class<?>>();
 		Class<?> clazz = getClass();
 		while(clazz != Controller.class) {
 			classes.add(clazz);
 			clazz = clazz.getSuperclass();
 		}
 		for(int i = classes.size()-1; i >= 0; i--) {
 			try {
 				Method method = classes.get(i).getDeclaredMethod(before ? "beforeFilter" : "afterFilter", Action.class);
 				method.setAccessible(true);
 				try {
 					method.invoke(this, action);
 					if(before && isRendered) {
 						return;
 					}
 				} catch (IllegalArgumentException e) {
 					logger.error(e);
 				} catch (IllegalAccessException e) {
 					logger.error(e);
 				} catch (InvocationTargetException e) {
 					logger.error(e);
 				}
 			} catch(NoSuchMethodException e) {
 				// discard
 			}
 		}
 	}
 	
 	public void clear() {
 		logger = null;
 		handler = null;
 		appRouter = null;
 		action = null;
 		request = null;
 		response = null;
 		if(routeParams != null) routeParams.clear();
 		routeParams = null;
 		if(params != null) params.clear();
 		params = null;
 		if(flash != null) flash.clear();
 		flash = null;
 		if(flashOut != null) flashOut.clear();
 		flashOut = null;
 		if(session != null) session.clearData();
 		session = null;
 		sessionResolved = false;
 		isRendered = false;
 	}
 	
 	/**
 	 * POST url/[model]s
 	 * Implemented by subclasses, if necessary, to create a model.
 	 * @throws SQLException
 	 */
 	public void create() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 	
 	/** 
 	 * DELETE url/[model]s/1
 	 * Implemented by subclasses, if necessary, to destroy / delete a model.
 	 * @throws SQLException
 	 */
 	public void destroy() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 	
 	public void execute(Action action) throws SQLException {
 		if(logger.isLoggingDebug()) {
 			logger.debug("start controller#execute - " + getControllerName() + "#" + ((action != null) ? action : "handleRequest"));
 		}
 		if(!isRendered) {
 			this.action = action;
 			callFilters(true);
 			if(!isRendered) {
 				CacheObject cache = getCacheForAction(action);
 				if(cache != null) {
 					render(wants(), cache);
 				} else {
 					if(action == null) {
 						handleRequest();
 					} else {
 						switch(action) {
 						case create:	create();	break;
 						case update:	update();	break;
 						case destroy:	destroy();	break;
 						case show:		show();		break;
 						case showAll:	showAll();	break;
 						case showEdit:	showEdit();	break;
 						case showNew:	showNew();	break;
 						}
 					}
 					if(isRendered && response != null && ActionCache.isCaching(this, action)) {
 						setCacheForAction(action, response.getContent().array());
 					}
 				}
 			}
 			callFilters(false);
 			this.action = null;
 		}
 
 		if(response != null) {
 			if(flashOut != null) {
 				response.setCookie(FLASH_KEY, toJson(flashOut));
 			} else if(flash != null) {
 				response.expireCookie(FLASH_KEY);
 			}
 			
 			if(session != null) {
 				if(session.isDestroyed()) {
 					response.expireCookie(SESSION_ID_KEY);
 					response.expireCookie(SESSION_UUID_KEY);
 				} else {
 					Timestamp exp = new Timestamp(System.currentTimeMillis() + 30*60*1000);
 					session.setExpiration(exp);
 					if(session.save()) {
 						response.setCookie(SESSION_ID_KEY, Integer.toString(session.getId()), 30);
 						response.setCookie(SESSION_UUID_KEY, session.getUuid(), 30);
 					}
 				}
 			}
 		}
 
 		logger.debug("end controller execute");
 	}
 	
 	public void expireCache(String key) {
 		CacheService cache = handler.getCacheService();
 		if(cache != null) {
 			cache.expire(createCacheKey(handler, key));
 		} else {
 			logger.warn("cache service is not available");
 		}
 	}
 	
 	protected void expireCacheForAction() {
 		expireCacheForAction(action);
 	}
 	
 	protected void expireCacheForAction(Action action) {
 		if(ActionCache.isCaching(this, action)) {
 			expireCache(createActionCacheKey(getClass(), action, wants()));
 		}
 	}
 	
 	/**
 	 * Create a {@link Filter} object for the parameter with the given name.<br>
 	 * If the parameter with the given name is not a map (or there is no parameter with 
 	 * the given name) then this method returns an empty {@link Filter} whose methods
 	 * can still be called, but will not perform any function.
 	 * <p>Use the returned {@link Filter} object to remove (filter out) any unwanted
 	 * parameters that may be sent from an untrusted source, such as a web form.
 	 * @param param the name of the parameter to create the {@link Filter} object on
 	 * @return a {@link Filter}; never null
 	 */
 	public Filter filter(String param) {
 		if(params == null) {
 			initParams();
 		}
 		return new Filter(params.get(param));
 	}
 
 	@Override
 	public String flash(String name) {
 		return getFlash(name);
 	}
 	
 	public <T> T flash(String name, java.lang.Class<T> type) {
 		if(flash != null) {
 			return coerce(flash.get(name), type);
 		}
 		return null;
 	}
 
 	public <T> T flash(String name, T defaultValue) {
 		if(flash != null) {
 			return coerce(flash.get(name), defaultValue);
 		}
 		return defaultValue;
 	}
 	
 	@Override
 	public Action getAction() {
 		return action;
 	}
 	
 	@Override
 	public String getActionName() {
 		return (action != null) ? action.name() : "handleRequest";
 	}
 	
 	public AppService getApplication() {
 		return handler;
 	}
 	
 	@Override
 	public <T extends Model> T getAuthenticated(Class<T> clazz) {
 		if(clazz != null) {
 			resolveSession(true);
 			if(clazz.getName() == session.getData(AUTHENTICATED_BY_TYPE)) {
 				return coerce(session.getData(AUTHENTICATED_BY), clazz);
 			}
 		}
 		return null;
 	}
 	
 	protected CacheObject getCacheForAction() {
 		return getCacheForAction(action);
 	}
 
 	protected CacheObject getCacheForAction(Action action) {
 		if(ActionCache.isCaching(this, action)) {
 			String key = createActionCacheKey(getClass(), action, wants());
 			CacheService cache = handler.getCacheService();
 			if(cache != null) {
 				return cache.get(createCacheKey(handler, key));
 			} else {
 				logger.warn("cache service is not available");
 				return null;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public String getControllerName() {
 		return getClass().getSimpleName();
 	}
 
 	@Override
 	public String getFlash(String name) {
 		if(flash != null) {
 			Object o = flash.get(name);
 			if(o != null) {
 				return coerce(o, String.class);
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public <T> T getFlash(String name, Class<T> type) {
 		return flash(name, type);
 	}
 	
 	@Override
 	public <T> T getFlash(String name, T defaultValue) {
 		return flash(name, defaultValue);
 	}
 	
 	@Override
 	public String getFlashError() {
 		return getFlash(FLASH_ERROR);
 	}
 	
 	@Override
 	public String getFlashNotice() {
 		return getFlash(FLASH_NOTICE);
 	}
 	
 	@Override
 	public String getFlashWarning() {
 		return getFlash(FLASH_WARNING);
 	}
 
 	public int getId() {
 		return param("id", int.class);
 	}
 	
 	public Logger getLogger() {
 		return logger;
 	}
 	
 	@Override
 	public Object getParam(String name) {
 		if(params == null) {
 			initParams();
 		}
 		return resolve(name, params);
 	}
 	
 	@Override
 	public <T> T getParam(String name, Class<T> clazz) {
 		return coerce(getParam(name), clazz);
 	}
 	
 	@Override
 	public <T> T getParam(String name, T defaultValue) {
 		return coerce(getParam(name), defaultValue);
 	}
 	
 	@Override
 	public Set<String> getParams() {
 		if(params == null) {
 			initParams();
 		}
 		return params.keySet();
 	}
 	
 	public Request getRequest() {
 		return request;
 	}
 	
 	public Response getResponse() {
 		return response;
 	}
 	
 	public AppRouter getRouter() {
 		return appRouter;
 	}
 	
 	public Session getSession() {
 		return getSession(true);
 	}
 	
 	public Session getSession(boolean create) {
 		resolveSession(create);
 		return session;
 	}
 	
 	/**
 	 * Implemented by subclasses, if necessary, to handle non-RESTful routes.
 	 * @throws SQLException
 	 * @see {@link Router#}
 	 */
 	public void handleRequest() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	@Override
 	public boolean hasFlash(String name) {
 		if(flash != null) {
 			return flash.containsKey(name);
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean hasFlashError() {
 		return hasFlash(FLASH_ERROR);
 	}
 	
 	@Override
 	public boolean hasFlashNotice() {
 		return hasFlash(FLASH_NOTICE);
 	}
 
 	@Override
 	public boolean hasFlashWarning() {
 		return hasFlash(FLASH_WARNING);
 	}
 	
 	@Override
 	public boolean hasParam(String name) {
 		if(params == null) {
 			initParams();
 		}
 		return params.containsKey(name);
 	}
 
 	@Override
 	public boolean hasParams() {
 		if(params == null) {
 			initParams();
 		}
 		return !params.isEmpty();
 	}
 
 	@Override
 	public boolean hasSession() {
 		resolveSession(false);
 		return session != null;
 	}
 
 	public void initialize(Router router, Request request, Map<String, Object> routeParams) {
 		this.router = router;
 		this.request = request;
 		this.handler = (AppService) request.getHandler();
 		this.logger = handler.getLogger();
 		this.appRouter = handler.getRouter();
 		this.routeParams = routeParams;
 		
 		// flash is always checked, so set it up here
 		String s = request.getCookieValue(FLASH_KEY);
 		if(s != null) {
 			flash = toMap(s);
 		}
 
 		sessionResolved = false;
 	}
 	
 	private void initParams() {
 		Map<String, Object> requestParams = request.getParameters();
 		if(requestParams != null) {
 			Map<String, Object> params = new HashMap<String, Object>(requestParams);
 			if(routeParams != null) {
 				// routeParams overwrite requestParams
 				params.putAll(routeParams);
 			}
 			this.params = mapParams(params);
 		} else if(routeParams != null) {
 			this.params = mapParams(routeParams);
		} else {
			this.params = new HashMap<String, Object>(0);
 		}
 	}
 	
 	@Override
 	public boolean isAction(Action action) {
 		return getAction() == action;
 	}
 	
 	@Override
 	public boolean isAuthenticated() {
 		resolveSession(true);
 		long start = coerce(session.getData(AUTHENTICATED_AT), long.class);
 		return (System.currentTimeMillis() - start) < AUTHENTICATION_INTERVAL;
 	}
 
 	@Override
 	public boolean isAuthenticated(Model model) {
 		if(model != null) {
 			resolveSession(true);
 			if(model.getId() == coerce(session.getData(AUTHENTICATED_BY), int.class) &&
 					model.getClass().getName().equals(session.getData(AUTHENTICATED_BY_TYPE))) {
 				return isAuthenticated();
 			}
 		}
 		return false;
 	}
 	
 	protected boolean isAuthorized(Realm realm) {
 		return appRouter.isAuthorized(request, realm);
 	}
 	
 	protected boolean isAuthorized(String username, String password) {
 		return appRouter.isAuthorized(request, username, password);
 	}
 	
 	@Override
 	public boolean isPath(String path) {
 		return request.getPath().equals(path);
 	}
 	
 	public boolean isRendered() {
 		return isRendered;
 	}
 	
 	@Override
 	public boolean isXhr() {
 		String header = request.getHeader("X-Requested-With");
 		return "XMLHttpRequest".equals(header);
 	}
 	
 	@Override
 	public String param(String name) {
 		return coerce(getParam(name), String.class);
 	}
 	
 	@Override
 	public <T> T param(String name, Class<T> clazz) {
 		return getParam(name, clazz);
 	}
 	
 	@Override
 	public <T> T param(String name, T defaultValue) {
 		return getParam(name, defaultValue);
 	}
 	
 	@Override
 	public Set<String> params() {
 		return getParams();
 	}
 	
 	public Map<String, Object> params(String...names) {
 		Map<String, Object> params = new HashMap<String, Object>();
 		for(String name : names) {
 			if(hasParam(name)) {
 				params.put(name, getParam(name));
 			}
 		}
 		return params;
 	}
 
 	@Override
 	public String pathTo(Class<? extends Model> modelClass) {
 		return appRouter.pathTo(router, modelClass);
 	}
 	
 	@Override
 	public String pathTo(Class<? extends Model> modelClass, Action action) {
 		return appRouter.pathTo(router, modelClass, action);
 	}
 	
 	@Override
 	public String pathTo(Model model) {
 		return appRouter.pathTo(router, model);
 	}
 	
 	@Override
 	public String pathTo(Model model, Action action) {
 		return appRouter.pathTo(router, model, action);
 	}
 	
 	@Override
 	public String pathTo(Model parent, String field) {
 		return appRouter.pathTo(router, parent, field);
 	}
 
 	@Override
 	public String pathTo(Model parent, String field, Action action) {
 		return appRouter.pathTo(router, parent, field, action);
 	}
 	
 	@Override
 	public String pathTo(String routeName) {
 		return appRouter.pathTo(router, routeName);
 	}
 	
 	@Override
 	public String pathTo(String routeName, Model model) {
 		return appRouter.pathTo(router, routeName, model);
 	}
 	
 	@Override
 	public String pathTo(String routeName, Object... params) {
 		return appRouter.pathTo(router, routeName, params);
 	}
 
 	public void redirectTo(Class<? extends Model> clazz, Action action) {
 		redirectTo(pathTo(clazz, action));
 	}
 	
 	public void redirectTo(Model model, Action action) {
 		redirectTo(model, action, null);
 	}
 	
 	public void redirectTo(Model model, Action action, String notice) {
 		if(action == showEdit) {
 			addFlashError(model);
 			setFlash(varName(model.getClass()), model);
 		} else if(action == showNew) {
 			addFlashError(model);
 			setFlash(varName(model.getClass()), model);
 		}
 		if(!blank(notice)) {
 			setFlashNotice(notice);
 		}
 		redirectTo(pathTo(model, action));
 	}
 	
 	public void redirectTo(Model parent, String field, Action action) {
 		if(action == showEdit || action == showNew) {
 			Object fieldValue = parent.get(field);
 			if(fieldValue instanceof Model) {
 				addFlashError((Model) fieldValue);
 				if(action == showNew) {
 					setFlash(varName(fieldValue.getClass()), (Model) fieldValue);
 				}
 			}
 		}
 		redirectTo(pathTo(parent, field, action));
 	}
 
 	public void redirectTo(String path) {
 		rendering();
 		response = new Response(HttpResponseStatus.FOUND);
 		response.addHeader(HttpHeaders.Names.LOCATION, path);
 	}
 
 	public void redirectToHome() {
 		redirectTo("/");
 	}
 	
 	public void render(Collection<? extends Model> models) {
 		render(JSON, models);
 	}
 	
 	public void render(File file) {
 		rendering();
 		response = new StaticResponse(file);
 	}
 
 	public void render(HttpResponseStatus status) {
 		render(status, wants(), status.getReasonPhrase());
 	}
 	
 	public void render(HttpResponseStatus status, MimeType contentType, String body) {
 		rendering();
 		response = new Response(status);
 		response.setContentType(contentType);
 		response.setContent(body);
 	}
 
 	public void render(HttpResponseStatus status, String body) {
 		render(status, wants(), body);
 	}
 	
 	public void render(MimeType type, byte[] data) {
 		rendering();
 		response = new Response();
 		response.setContentType(type);
 		response.setContent(ChannelBuffers.wrappedBuffer(data));
 	}
 	
 	private void render(MimeType type, CacheObject cache) {
 		rendering();
 		response = new StaticResponse(type, cache.payload(), cache.contentLength(), cache.lastModified());
 	}
 	
 	public void render(MimeType type, Collection<? extends Model> models) {
 		render(type, toJson(models));
 	}
 	
 	private void render(MimeType type, DynamicAsset asset) {
 		rendering();
 		response = new Response();
 		response.setContentType(type);
 		response.setContent(asset.getContent());
 	}
 	
 	public void render(MimeType type, String body) {
 		rendering();
 		response = new Response();
 		response.setContentType(type);
 		if(blank(body) && (type == MimeType.JSON || type == MimeType.JS)) {
 			response.setContent("null");
 		} else {
 			response.setContent(body);
 		}
 	}
 	
 	public void render(Model model) {
 		render(JSON, (model == null) ? "null" : model.toJson());
 	}
 	
 	public void render(Model model, String include, Object...values) {
 		render(JSON, (model == null) ? "null" : model.toJson(include, values));
 	}
 	
 	/**
 	 * Convenience method for render(String.valueOf(object))
 	 * @param object
 	 */
 	public void render(Object object) {
 		if(object instanceof ScriptFile) {
 			render((ScriptFile) object);
 		}
 		else if(object instanceof StyleSheet) {
 			render((StyleSheet) object);
 		}
 		else if(object instanceof Model) {
 			render((Model) object);
 		}
 		else if(object instanceof View) {
 			render((View) object);
 		}
 		else if(object instanceof HttpResponseStatus) {
 			render((HttpResponseStatus) object);
 		}
 		else {
 			render(String.valueOf(object));
 		}
 	}
 	
 	public void render(ScriptFile sf) {
 		render(JS, sf);
 	}
 	
 	public void render(String body) {
 		rendering();
 		response = new Response();
 		response.setContentType(wants());
 		response.setContent(body);
 	}
 	
 	public void render(String body, Collection<?> values) {
 		if(values == null || values.isEmpty()) {
 			render(body);
 		} else {
 			render(body, values.toArray());
 		}
 	}
 	
 	public void render(String body, Map<String, Object> values) {
 		if(values == null || values.isEmpty()) {
 			render(body);
 		} else {
 			StringBuilder sb = new StringBuilder(body);
 			Pattern pattern = Pattern.compile("#\\{(\\w+)}");
 			Matcher matcher = pattern.matcher(sb);
 			for(int i = 0, start = 0; matcher.find(start); i++) {
 				String key = matcher.group(1);
 				Object val = values.containsKey(key) ? values.get(key) : ("#{" + key + ": *** UNKNOWN ***}");
 				sb.replace(matcher.start(), matcher.end(), String.valueOf(val));
 				start = matcher.end();
 				matcher = pattern.matcher(sb);
 			}
 			render(sb);
 		}
 	}
 	
 	public void render(String body, Object...values) {
 		if(values.length == 0) {
 			render(body);
 		} else {
 			StringBuilder sb = new StringBuilder(body);
 			Pattern pattern = Pattern.compile("#\\{\\?}");
 			Matcher matcher = pattern.matcher(sb);
 			for(int i = 0, start = 0; matcher.find(start); i++) {
 				sb.replace(matcher.start(), matcher.end(), String.valueOf(values[i]));
 				start = matcher.end();
 				matcher = pattern.matcher(sb);
 			}
 			render(sb);
 		}
 	}
 	
 	public void render(StyleSheet ss) {
 		render(CSS, ss);
 	}
 
 	public void render(View view) {
 		render(view, isXhr());
 	}
 	
 	public void render(View view, boolean partial) {
 		if(view == null) {
 			throw new IllegalArgumentException("view cannot be null");
 		}
 		if(logger.isLoggingDebug()) {
 			logger.debug("start render of view " + view.getClass().getCanonicalName());
 		}
 		rendering();
 		
 		try {
 			response = new Response();
 			response.setContentType(MimeType.HTML);
 			
 			ViewRenderer renderer = new ViewRenderer(this, view);
 			renderer.setPartial(partial);
 			
 			response.setContent(renderer.render());
 		} finally {
 			view.setRenderer(null);
 		}
 		logger.debug("end render of view");
 	}
 	
 	public void renderAccepted() {
 		render(HttpResponseStatus.ACCEPTED, wantsJS() ? "[]" : HttpResponseStatus.ACCEPTED.getReasonPhrase());
 	}
 	
 	public void renderCreated(int id) {
 		renderCreated((long) id);
 	}
 	
 	public void renderCreated(long id) {
 		renderCreated(id, null);
 	}
 
 	public void renderCreated(long id, String path) {
 		rendering();
 		response = new Response(HttpResponseStatus.CREATED);
 		response.addHeader("id", String.valueOf(id));
 		if(!blank(path)) {
 			response.addHeader(HttpHeaders.Names.LOCATION, path);
 		}
 		if(wantsJS()) {
 			response.setContentType(wants());
 			response.setContent("null");
 		}
 	}
 	
 	public void renderCreated(Model model) {
 		renderCreated((long) model.getId(), pathTo(model));
 	}
 
 	public void renderDestroyed(Model model) {
 		rendering();
 		response = new Response();
 		response.addHeader("id", String.valueOf(model.getId()));
 		if(wantsJS()) {
 			response.setContentType(wants());
 			response.setContent("null");
 		}
 	}
 
 	public void renderErrors(List<String> errors) {
 		if(errors != null) {
 			renderErrors(errors.toArray(new String[errors.size()]));
 		} else {
 			renderErrors(new String[0]);
 		}
 	}
 
 	public void renderErrors(Model...models) {
 		rendering();
 		response = new Response(HttpResponseStatus.CONFLICT);
 		response.setContentType(MimeType.JSON);
 		if(models.length == 0) {
 			response.setContent("[]");
 		} else if(models.length == 1) {
 			if(models[0] != null) {
 				Map<String, Object> errors = new HashMap<String, Object>();
 				errors.put("errors", models[0].getErrorsList());
 				response.setContent(toJson(errors));
 			}
 		} else {
 			List<String> list = new ArrayList<String>();
 			for(Model model : models) {
 				if(model != null) {
 					list.addAll(model.getErrorsList());
 				}
 			}
 			Map<String, Object> errors = new HashMap<String, Object>();
 			errors.put("errors", list);
 			response.setContent(toJson(errors));
 		}
 	}
 	
 	public void renderErrors(String...errors) {
 		rendering();
 		response = new Response(HttpResponseStatus.CONFLICT);
 		response.setContentType(MimeType.JSON);
 		if(errors.length == 0) {
 			response.setContent("[]");
 		} else {
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put("errors", errors);
 			response.setContent(toJson(errors));
 		}
 	}
 	
 	private void rendering() {
 		if(isRendered) {
 			throw new UnsupportedOperationException("cannot render more than once");
 		}
 		isRendered = true;
 	}
 
 	public void renderJson(Collection<? extends Model> models, String include, Object...values) {
 		if(blank(models)) {
 			render(MimeType.JSON, "[]");
 		} else {
 			String json = Model.toJson(models, include, values);
 			render(MimeType.JSON, json);
 		}
 	}
 	
 	public void renderJson(Object object) {
 		render(MimeType.JSON, format(toJson(object)));
 	}
 
 	/**
 	 * Render the given object as JSON data in a manner appropriate for the standard
 	 * JQuery JSONP implementation.
 	 * <p>Note: the request <b>must</b> contain a 'callback' parameter.
 	 * See http://api.jquery.com/jQuery.ajax/ for more details.</p>
 	 * @param object the object to be rendered as JSON data
 	 */
 	public void renderJsonP(Object object) {
 		String callbackName = param("callback", String.class); // standard JQuery jsonp implementation
 		if(callbackName == null) {
 			logger.warn("error in renderJsonP: no 'callback' parameter in request");
 			render(JS, "alert('error in renderJsonP: no callback parameter in request')");
 		} else {
 			String json = format(toJson(object));
 			String callback = callbackName + "(" + json + ");";
 			render(MimeType.JS, callback);
 		}
 	}
 	
 	public void renderOK() {
 		render(HttpResponseStatus.OK, wantsJS() ? "[]" : HttpResponseStatus.OK.getReasonPhrase());
 	}
 
 	public void renderPage(String text) {
 		renderPage("", text);
 	}
 	
 	public void renderPage(String title, String text) {
 		render(MimeType.PLAIN,
 				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
 				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
 				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
 				"<head>\n" +
 			   	"<title>" + title + "</title>\n" +
 			   	"</head>\n" +
 			   	"<body>\n" +
 			   	text + "\n" +
 			   	"</body>\n" +
 				"</html>"
 		);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Object resolve(String name, Map<String, Object> map) {
 		
 		// TODO make this even lazier by implementing a TypeCoercer...
 		
 		Object o = map.get(name);
 		if(o instanceof Map) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			for(Object k : m.keySet()) {
 				resolve((String) k, m);
 			}
 		} else if(o instanceof HttpData) {
 			HttpData data = (HttpData) o;
 			try {
 				switch(data.getHttpDataType()) {
 				case Attribute:
 					o = ((Attribute) data).getValue();
 					break;
 				case FileUpload:
 					FileUpload up = (FileUpload) data;
 					o = up.get();
 					if(!params.containsKey("transfer-encoding")) {
  						params.put("transfer-encoding", up.getContentTransferEncoding());
 					}
 					if(!params.containsKey("content-type")) {
 						params.put("content-type", up.getContentType());
 					}
 					if(!params.containsKey("filename")) {
 						params.put("filename", up.getFilename());
 					}
 					break;
 				}
 			} catch(IOException e) {
 				logger.warn(e.getLocalizedMessage());
 			}
 			map.put(name, o);
 			params.put(data.getName(), o);
 			data.delete();
 		}
 		return o;
 	}
 
 	private void resolveSession(boolean create) {
 		if((create && session == null) || (!create && !sessionResolved)) {
 			sessionResolved = true;
 			Cookie cookie = request.getCookie(SESSION_ID_KEY);
 			if(cookie != null) {
 				try {
 					int id = Integer.parseInt(cookie.getValue());
 					cookie = request.getCookie(SESSION_UUID_KEY);
 					if(cookie != null) {
 						String uuid = cookie.getValue();
 						session = handler.getSession(id, uuid, create);
 					}
 				} catch(NumberFormatException e) {
 					logger.warn("invalid cookie id format: " + cookie.getValue());
 				}
 			} else if(create) {
 				session = handler.getSession(-1, null, true);
 			}
 		}
 	}
 
 	protected void setCacheForAction(Action action, byte[] content) {
 		if(ActionCache.isCaching(this, action)) {
 			String key = createActionCacheKey(getClass(), action, wants());
 			CacheService cache = handler.getCacheService();
 			if(cache != null) {
 				cache.set(createCacheKey(handler, key), content);
 			} else {
 				logger.warn("cache service is not available");
 			}
 		}
 	}
 
 	protected void setCacheForAction(byte[] content) {
 		setCacheForAction(action, content);
 	}
 
 	public void setFlash(String name, Object value) {
 		if(flash == null) {
 			flash = new HashMap<String, Object>();
 		}
 		if(flashOut == null) {
 			flashOut = new HashMap<String, Object>();
 		}
 		if(value == null) {
 			flash.remove(name);
 			flashOut.remove(name);
 		} else {
 			flash.put(name, value);
 			flashOut.put(name, value);
 		}
 	}
 
 	public void setFlashError(Model model) {
 		if(model.hasErrors()) {
 			List<String> errors = new ArrayList<String>();
 			for(String error : model.getErrorsList()) {
 				errors.add(error);
 			}
 			setFlash(FLASH_ERROR, errors);
 		}
 	}
 
 	public void setFlashError(Model... models) {
 		List<String> errors = new ArrayList<String>();
 		for(Model model : models) {
 			for(String error : model.getErrorsList()) {
 				errors.add(error);
 			}
 		}
 		if(!errors.isEmpty()) {
 			setFlash(FLASH_ERROR, errors);
 		}
 	}
 
 	public void setFlashError(Object value) {
 		setFlash(FLASH_ERROR, value);
 	}
 	
 	public void setFlashNotice(Object value) {
 		setFlash(FLASH_NOTICE, value);
 	}
 	
 	public void setFlashWarning(Object value) {
 		setFlash(FLASH_ERROR, value);
 	}
 	
 	public void setParam(String name, Object value) {
 		if(params == null) {
 			initParams();
 		}
 		if(value == null) {
 			params.remove(name);
 		} else {
 			params.put(name, value);
 		}
 	}
 	
 	/**
 	 * GET url/[model]s/1
 	 * Implemented by subclasses, if necessary, to show (retrieve) a model.
 	 * @throws SQLException
 	 */
 	public void show() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * GET url/[model]s
 	 * Implemented by subclasses, if necessary, to show (retrieve) all models.
 	 * @throws SQLException
 	 */
 	public void showAll() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * GET url/[model]s/1/edit
 	 * Implemented by subclasses, if necessary, to show the edit page for a model.
 	 * @throws SQLException
 	 */
 	public void showEdit() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * GET url/[model]s/new
 	 * Implemented by subclasses, if necessary, to show the new page for a model.
 	 * @throws SQLException
 	 */
 	public void showNew() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * PUT url/[model]s/1
 	 * Implemented by subclasses, if necessary, to update a model.
 	 * @throws SQLException
 	 */
 	public void update() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	@Override
 	public String urlTo(Class<? extends Model> modelClass) {
 		return appRouter.urlTo(router, modelClass);
 	}
 
 	@Override
 	public String urlTo(Class<? extends Model> modelClass, Action action) {
 		return appRouter.urlTo(router, modelClass, action);
 	}
 
 	@Override
 	public String urlTo(Model model) {
 		return appRouter.urlTo(router, model);
 	}
 
 	@Override
 	public String urlTo(Model model, Action action) {
 		return appRouter.urlTo(router, model, action);
 	}
 
 	@Override
 	public String urlTo(Model parent, String field) {
 		return appRouter.urlTo(router, parent, field);
 	}
 
 	@Override
 	public String urlTo(Model parent, String field, Action action) {
 		return appRouter.urlTo(router, parent, field, action);
 	}
 
 	@Override
 	public String urlTo(String routeName) {
 		return appRouter.urlTo(router, routeName);
 	}
 
 	@Override
 	public String urlTo(String routeName, Model model) {
 		return appRouter.urlTo(router, routeName, model);
 	}
 
 	@Override
 	public String urlTo(String routeName, Object... params) {
 		return appRouter.urlTo(router, routeName, params);
 	}
 	
 	@Override
 	public MimeType wants() {
 		List<MimeType> requestTypes = request.getAcceptedTypes();
 		if(requestTypes != null && !requestTypes.isEmpty()) {
 			return requestTypes.get(0);
 		}
 		return MimeType.UNKNOWN;
 	}
 
 	@Override
 	public MimeType.Name wants(MimeType...options) {
 		List<MimeType> acceptedTypes = request.getAcceptedTypes();
 		if(!blank(acceptedTypes)) {
 			if(blank(options)) {
 				return acceptedTypes.get(0).name;
 			} else {
 				for(MimeType accept : acceptedTypes) {
 					if(accept == MimeType.ALL) {
 						return options[0].name;
 					} else {
 						for(MimeType produce : options) {
 							if(accept.resolves(produce)) {
 								return accept.name;
 							}
 						}
 					}
 				}
 			}
 		}
 		if(!blank(options)) {
 			return options[options.length-1].name;
 		}
 		return MimeType.UNKNOWN.name;
 	}
 
 	@Override
 	public boolean wants(MimeType type) {
 		return wants().resolves(type);
 	}
 
 	protected boolean wantsJS() {
 		MimeType wants = wants();
 		return wants.resolves(MimeType.JS) || wants.resolves(MimeType.JSON);
 	}
 	
 }
