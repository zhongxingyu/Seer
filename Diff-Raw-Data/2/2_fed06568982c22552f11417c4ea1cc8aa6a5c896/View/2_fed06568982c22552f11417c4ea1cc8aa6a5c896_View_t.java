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
 package org.oobium.app.views;
 
 import static org.oobium.utils.StringUtils.camelCase;
 import static org.oobium.utils.StringUtils.h;
 import static org.oobium.utils.StringUtils.pluralize;
 import static org.oobium.utils.StringUtils.titleize;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.oobium.app.controllers.HttpController;
 import org.oobium.app.controllers.IFlash;
 import org.oobium.app.controllers.IHttp;
 import org.oobium.app.controllers.IParams;
 import org.oobium.app.http.Action;
 import org.oobium.app.http.MimeType;
 import org.oobium.app.request.Request;
 import org.oobium.app.response.Response;
 import org.oobium.app.routing.IPathRouting;
 import org.oobium.app.routing.IUrlRouting;
 import org.oobium.app.routing.Path;
 import org.oobium.app.routing.Router;
 import org.oobium.app.sessions.ISession;
 import org.oobium.app.sessions.ISessions;
 import org.oobium.logging.Logger;
 import org.oobium.persist.Model;
 
 public class View implements IFlash, IParams, IPathRouting, IUrlRouting, ISessions, IHttp {
 
 	/**
 	 * Render a view of the given class for the given request. Since this method does not take a 
 	 * Router object then it may not resolve pathTo requests from the perspective of the Application
 	 * rather than the given view class's Module. Thus, this method is primarily suited for views that
 	 * will not make use of pathTo, or are top-level views whose use of pathTo is only relative to the
 	 * application. Default error views (404 and 500) use this method.
 	 * @param viewClass the class of view to be rendered
 	 * @param request the request to use while rendering the view
 	 * @return the rendered Response object
 	 * @throws Exception this will run user generated content - be prepared for anything.
 	 */
 	public static Response render(Class<? extends View> viewClass, Request request) throws Exception {
 		View view = viewClass.newInstance();
 		return render(null, view, request, new HashMap<String, Object>(0));
 	}
 	
 	/**
 	 * Render the given view for the given request. Since this method does not take a
 	 * Router object then it may not resolve pathTo requests from the perspective of the Application
 	 * rather than the given view class's Module. Thus, this method is primarily suited for views that
 	 * will not make use of pathTo, or are top-level views whose use of pathTo is only relative to the
 	 * application.
 	 * @param view the view to be rendered
 	 * @param request the request to use while rendering the view
 	 * @return the rendered Response object
 	 * @throws Exception this will run user generated content - be prepared for anything.
 	 */
 	public static Response render(View view, Request request) throws Exception {
 		return render(null, view, request, new HashMap<String, Object>(0));
 	}
 
 	/**
 	 * Render the given view for the given request. Any pathTo requests will be resolved from the perspective of 
 	 * the given Router.
 	 * @param router the router from which resolution of pathTo requests will begin
 	 * @param view the view to be rendered
 	 * @param request the request to use while rendering the view
 	 * @return the rendered Response object
 	 * @throws Exception this will run user generated content - be prepared for anything.
 	 */
 	public static Response render(Router router, View view, Request request) throws Exception {
 		return render(router, view, request, new HashMap<String, Object>(0));
 	}
 	
 	/**
 	 * Render the given view for the given request. Any pathTo requests will be resolved from the perspective of 
 	 * the given Router.
 	 * @param router the router from which resolution of pathTo requests will begin
 	 * @param view the view to be rendered
 	 * @param request the request to use while rendering the view
 	 * @param params a map of parameters that are to be available to the view
 	 * @return the rendered Response object
 	 * @throws Exception this will run user generated content - be prepared for anything.
 	 */
 	public static Response render(Router router, View view, Request request, Map<String, Object> params) throws Exception {
 		HttpController controller = new HttpController();
 		controller.initialize(router, request, params);
 		controller.render(view);
 		return controller.getResponse();
 	}
 	
 
 	private ViewRenderer renderer;
 
 	protected Logger logger;
 	protected HttpController controller;
 	protected Request request;
 
 	private View child;
 	
 	private String layoutName;
 	private Class<? extends View> layout;
 	
 	@Override
 	public boolean accepts(MimeType type) {
 		return controller.accepts(type);
 	}
 
 	public void addExternalScript(String src) {
 		renderer.addExternalScript(src);
 	}
 	
 	public void addExternalScript(ScriptFile asset) {
 		renderer.addExternalScript(asset);
 	}
 	
 	public void addExternalStyle(Class<? extends StyleSheet> asset) {
 		renderer.addExternalStyle(asset);
 	}
 	
 	public void addExternalStyle(String href) {
 		renderer.addExternalStyle(href);
 	}
 	
 	public void addExternalStyle(StyleSheet asset) {
 		renderer.addExternalStyle(asset);
 	}
 	
 	protected void errorsBlock(StringBuilder sb, Model model, String title, String message) {
 		if(model.hasErrors()) {
 			List<String> errors = model.getErrorsList();
 			sb.append("<div class=\"errorExplanation\">");
 			if(title == null) {
 				String s1 = pluralize(errors.size(), "error");
 				String s2 = titleize(model.getClass().getSimpleName()).toLowerCase();
 				sb.append("<h2>").append(s1).append(" prohibited this ").append(s2).append(" from being saved").append("</h2>");
 			} else if(title.length() > 0) {
 				sb.append("<h2>").append(h(title)).append("</h2>");
 			}
 			if(message == null) {
 				sb.append("<p>There were problems with the following fields:</p>");
 			} else if(message.length() > 0) {
 				sb.append("<p>").append(h(message)).append("</p>");
 			}
 			sb.append("<ul>");
 			for(String error : errors) {
 				sb.append("<li>").append(h(error)).append("</li>");
 			}
 			sb.append("</ul>");
 			sb.append("</div>");
 		}
 	}
 	
 	@Override
 	public Object flash(String name) {
 		return controller.flash(name);
 	}
 	
 	@Override
 	public <T> T flash(String name, Class<T> type) {
 		return controller.flash(name, type);
 	}
 
 	@Override
 	public <T> T flash(String name, T defaultValue) {
 		return controller.flash(name, defaultValue);
 	}
 
 	@Override
 	public Action getAction() {
 		return controller.getAction();
 	}
 	
 	@Override
 	public String getActionName() {
 		return controller.getActionName();
 	}
 
 	public View getChild() {
 		return child;
 	}
 	
 	protected String getContent(String name) {
 		return renderer.getContent(name);
 	}
 	
 	@Override
 	public String getControllerName() {
 		return controller.getControllerName();
 	}
 	
 	@Override
 	public Object getFlash(String name) {
 		return controller.getFlash(name);
 	}
 	
 	@Override
 	public <T> T getFlash(String name, Class<T> type) {
 		return controller.getFlash(name, type);
 	}
 	
 	@Override
 	public <T> T getFlash(String name, T defaultValue) {
 		return controller.getFlash(name, defaultValue);
 	}
 	
 	@Override
 	public Object getFlashError() {
 		return controller.getFlashError();
 	}
 	
 	@Override
 	public Object getFlashNotice() {
 		return controller.getFlashNotice();
 	}
 	
 	@Override
 	public Object getFlashWarning() {
 		return controller.getFlashWarning();
 	}
 	
 	public View getLayout() {
 		Class<?> layout = this.layout;
 		
 		if(layout == null) {
 			Package pkg = getClass().getPackage();
 			if(pkg == null) {
 				return null; // probably a dynamic class in a unit test
 			}
 			String pname = pkg.getName();
 			int i1 = pname.indexOf(".views.") + 7;
 			if(i1 == -1) {
 				return null;
 			}
 			String lfolder = pname.substring(0, i1) + "_layouts.";
 			if(layoutName != null) {
 				try {
 					String fname = lfolder + layoutName;
 					layout = Class.forName(fname, true, getClass().getClassLoader());
 				} catch(ClassNotFoundException e) {
 					// oh well...
 				}
 			} else {
 				// look for a view specific layout
 				try {
 					int i2 = pname.lastIndexOf('.', i1);
 					String vname = camelCase(pname.substring(i2+1));
 					String fname = lfolder + vname + "Layout";
 					layout = Class.forName(fname, true, getClass().getClassLoader());
 				} catch(ClassNotFoundException e1) {
 					// look for the default view layout (cache...)
 					try {
 						String fname = lfolder + "_Layout";
 						layout = Class.forName(fname, true, getClass().getClassLoader());
 					} catch(ClassNotFoundException e2) {
 						// oh well...
 					}
 				}
 			}
 		}
 		
 		if(layout != null) {
 			if(View.class.isAssignableFrom(layout)) {
 				try {
 					Constructor<?> c = layout.getConstructor();
 					return (View) c.newInstance();
 				} catch(Exception e) {
 					// oh well...
 				}
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public Object getParam(String name) {
 		return controller.getParam(name);
 	}
 	
 	@Override
 	public <T> T getParam(String name, Class<T> clazz) {
 		return controller.getParam(name, clazz);
 	}
 	
 	@Override
 	public <T> T getParam(String name, T defaultValue) {
 		return controller.getParam(name, defaultValue);
 	}
 	
 	@Override
 	public Set<String> getParams() {
 		return controller.getParams();
 	}
 	
 	@Override
 	public ISession getSession() {
 		return controller.getSession();
 	}
 	
 	@Override
 	public ISession getSession(boolean create) {
 		return controller.getSession(create);
 	}
 
 	@Override
 	public ISession getSession(String include) {
 		return controller.getSession(include);
 	}
 	
 	@Override
 	public ISession getSession(String include, boolean create) {
 		return controller.getSession(include, create);
 	}
 
 	public boolean hasChild() {
 		return child != null;
 	}
 	
 	protected boolean hasContent(String name) {
 		return renderer.hasContent(name);
 	}
 	
 	@Override
 	public boolean hasFlash(String name) {
 		return controller.hasFlash(name);
 	}
 	
 	@Override
 	public boolean hasFlashError() {
 		return controller.hasFlashError();
 	}
 	
 	@Override
 	public boolean hasFlashNotice() {
 		return controller.hasFlashNotice();
 	}
 
 	@Override
 	public boolean hasFlashWarning() {
 		return controller.hasFlashWarning();
 	}
 	
 	public boolean hasMany(String field) {
 		return field != null && field.equals(getParam("hasMany"));
 	}
 	
 	@Override
 	public boolean hasParam(String name) {
 		return controller.hasParam(name);
 	}
 	
 	@Override
 	public boolean hasParams() {
 		return controller.hasParams();
 	}
 	
 	@Override
 	public boolean hasSession() {
 		return controller.hasSession();
 	}
 	
 	protected void includeScriptEnvironment() {
 		renderer.includeScriptEnvironment = true;
 	}
 	
 	protected String includeScriptModel(Model model, int position) {
 		return includeScriptModel(model, position, false);
 	}
 	
 	protected String includeScriptModel(Model model, int position, boolean includeHasMany) {
 		return renderer.includeScriptModel(model, position, includeHasMany);
 	}
 	
 	protected void includeScriptModels() {
 		includeScriptModels(false);
 	}
 	
 	protected void includeScriptModels(boolean includeHasMany) {
 		renderer.includeScriptModels(Model.class, includeHasMany);
 	}
 	
 	@Override
 	public boolean isAction(Action action) {
 		return controller.isAction(action);
 	}
 	
 	@Override
 	public boolean isPath(Path path) {
 		return (path != null) && request.getPath().equals(path.path());
 	}
 	
 	@Override
 	public boolean isPath(String path) {
 		return request.getPath().equals(path);
 	}
 	
 	@Override
 	public boolean isXhr() {
 		return controller.isXhr();
 	}
 	
 	protected void messagesBlock(StringBuilder sb) {
 		messagesBlock(sb, true, true, true);
 	}
 
 	protected void messagesBlock(StringBuilder sb, boolean errors, boolean warnings, boolean notices) {
 		if(errors && hasFlashError()) {
 			sb.append("<div class=\"errors\">");
 			sb.append("<ul>");
 			Object error = getFlashError();
 			if(error instanceof Iterable<?>) {
 				for(Object o : (Iterable<?>) error) {
 					sb.append("<li>").append(h(o)).append("</li>");
 				}
 			} else {
 				sb.append("<li>").append(h(error)).append("</li>");
 			}
 			sb.append("</ul>");
 			sb.append("</div>");
 		}
 
 		if(warnings && hasFlashWarning()) {
 			sb.append("<div class=\"warnings\">");
 			sb.append("<ul>");
 			Object error = getFlashWarning();
 			if(error instanceof Iterable<?>) {
 				for(Object o : (Iterable<?>) error) {
 					sb.append("<li>").append(h(o)).append("</li>");
 				}
 			} else {
 				sb.append("<li>").append(h(error)).append("</li>");
 			}
 			sb.append("</ul>");
 			sb.append("</div>");
 		}
 		
 		if(notices && hasFlashNotice()) {
 			sb.append("<div class=\"notices\">");
 			sb.append("<ul>");
 			Object error = getFlashNotice();
 			if(error instanceof Iterable<?>) {
 				for(Object o : (Iterable<?>) error) {
 					sb.append("<li>").append(h(o)).append("</li>");
 				}
 			} else {
 				sb.append("<li>").append(h(error)).append("</li>");
 			}
 			sb.append("</ul>");
 			sb.append("</div>");
 		}
 	}
 
 	@Override
 	public String param(String name) {
 		return controller.param(name);
 	}
 
 	@Override
 	public <T> T param(String name, Class<T> type) {
 		return controller.param(name, type);
 	}
 
 	@Override
 	public <T> T param(String name, T defaultValue) {
 		return controller.param(name, defaultValue);
 	}
 
 	@Override
 	public <T> T param(Class<T> type) {
 		return controller.param(type);
 	}
 
 	@Override
 	public <T> T param(T defaultValue) {
 		return controller.param(defaultValue);
 	}
 	
 	@Override
 	public Set<String> params() {
 		return controller.params();
 	}
 	
 	@Override
 	public String path() {
 		return request.getPath();
 	}
 	
 	@Override
 	public Path pathTo(Class<? extends Model> modelClass) {
 		return controller.pathTo(modelClass);
 	}
 	
 	@Override
 	public Path pathTo(Class<? extends Model> modelClass, Action action) {
 		return controller.pathTo(modelClass, action);
 	}
 
 	@Override
 	public Path pathTo(Model model) {
 		return controller.pathTo(model);
 	}
 	
 	@Override
 	public Path pathTo(Model model, Action action) {
 		return controller.pathTo(model, action);
 	}
 	
 	@Override
 	public Path pathTo(Model parent, String field) {
 		return controller.pathTo(parent, field);
 	}
 	
 	@Override
 	public Path pathTo(Model parent, String field, Action action) {
 		return controller.pathTo(parent, field, action);
 	}
 
 	@Override
 	public Path pathTo(String routeName) {
 		return controller.pathTo(routeName);
 	}
 	
 	@Override
 	public Path pathTo(String routeName, Model model) {
 		return controller.pathTo(routeName, model);
 	}
 	
 	@Override
 	public Path pathTo(String routeName, Object... params) {
 		return controller.pathTo(routeName, params);
 	}
 	
 	protected String putContent(String name, String content) {
 		return renderer.putContent(name, content);
 	}
 
 	void render() {
 		try {
 			render(renderer.head, renderer.body);
 		} catch(Exception e) {
 			if(e instanceof RuntimeException) {
 				throw (RuntimeException) e;
 			} else {
 				throw new RuntimeException("Exception thrown during render", e);
 			}
 		}
 	}
 
 	protected void render(StringBuilder __head__, StringBuilder __body__) throws Exception {
 		// subclasses to implement
 	}
 	
 	public View setChild(View child) {
 		this.child = child;
 		return this;
 	}
 	
 	public View setLayout(Class<? extends View> layout) {
 		this.layout = layout;
 		return this;
 	}
 
 	public View setLayout(String layoutName) {
 		this.layoutName = layoutName;
 		return this;
 	}
 
 	public void setRenderer(ViewRenderer renderer) {
 		this.renderer = renderer;
 		if(renderer == null) {
 			controller = null;
 			logger = null;
 			request = null;
 		} else {
 			controller = renderer.controller;
 			logger = controller.getLogger();
 			request = controller.getRequest();
 		}
 	}
 	
 	protected void setTitle(Object title) {
 		renderer.setTitle(title);
 	}
 	
 	@Override
 	public Path urlTo(Class<? extends Model> modelClass) {
 		return controller.urlTo(modelClass);
 	}
 	
 	@Override
 	public Path urlTo(Class<? extends Model> modelClass, Action action) {
 		return controller.urlTo(modelClass, action);
 	}
 	
 	@Override
 	public Path urlTo(Model model) {
 		return controller.urlTo(model);
 	}
 
 	@Override
 	public Path urlTo(Model model, Action action) {
 		return controller.urlTo(model, action);
 	}
 
 	@Override
 	public Path urlTo(Model parent, String field) {
 		return controller.urlTo(parent, field);
 	}
 
 	@Override
 	public Path urlTo(Model parent, String field, Action action) {
 		return controller.urlTo(parent, field, action);
 	}
 
 	@Override
 	public Path urlTo(String routeName) {
 		return controller.urlTo(routeName);
 	}
 
 	@Override
 	public Path urlTo(String routeName, Model model) {
 		return controller.urlTo(routeName, model);
 	}
 
 	@Override
 	public Path urlTo(String routeName, Object... params) {
 		return controller.urlTo(routeName, params);
 	}
 	
 	@Override
 	public MimeType wants() {
 		return controller.wants();
 	}
 	
 	@Override
 	public MimeType.Name wants(MimeType... options) {
 		return controller.wants(options);
 	}
 
 	@Override
 	public boolean wants(MimeType type) {
 		return controller.wants(type);
 	}
 
 	protected void yield() {
 		yield(child);
 	}
 	
 	protected void yieldTo(String name) {
 		if(name != null && name.length() > 0) {
 			renderer.addPosition(name);
 		}
 	}
 	
 	protected void yield(StringBuilder __body__) {
 		yield(child, __body__);
 	}
 	
 	protected void yield(View view) {
 		if(view != null) {
 			view.setRenderer(renderer);
 			view.render();
 		}
 	}
 	
 	protected void yield(View view, StringBuilder __body__) {
 		if(view != null) {
 			try {
 				view.setRenderer(renderer);
				view.render(renderer.head, __body__);
 			} catch(Exception e) {
 				if(e instanceof RuntimeException) {
 					throw (RuntimeException) e;
 				} else {
 					throw new RuntimeException("Exception thrown during render", e);
 				}
 			}
 		}
 	}
 	
 }
