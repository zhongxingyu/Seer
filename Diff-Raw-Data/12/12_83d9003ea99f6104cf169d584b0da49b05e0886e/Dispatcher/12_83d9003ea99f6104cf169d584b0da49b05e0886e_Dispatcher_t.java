 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import zcu.xutil.Logger;
 import zcu.xutil.Objutil;
 import zcu.xutil.cfg.Context;
 import zcu.xutil.cfg.NProvider;
 import zcu.xutil.utils.MethodInvocation;
 import static zcu.xutil.Objutil.*;
 
 /**
  * 
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  * 
  */
 public final class Dispatcher implements Filter {
 	int prefixLen;
 	Resolver[] resolvers;
 	String[] resolverNames;
 	Map<String, String> namesMap;
 	ServletContext servletCtx;
 	Context context;
 
 	@Override
 	public void init(FilterConfig cfg) throws ServletException {
 		prefixLen = (servletCtx = cfg.getServletContext()).getContextPath().length() + 1;
 		context = Webutil.getAppContext(servletCtx);
 		List<NProvider> res = new ArrayList<NProvider>();
 		for (NProvider p : context.getProviders(Resolver.class)) {
 			if (p.getName().charAt(0) == '.')
 				res.add(p);
 		}
 		int len = res.size();
 		resolvers = new Resolver[len];
 		resolverNames = new String[len];
 		while (--len >= 0) {
 			resolverNames[len] = res.get(len).getName();
 			resolvers[len] = (Resolver) res.get(len).instance();
 		}
 		len = (res = context.getProviders(Action.class)).size();
 		namesMap = new HashMap<String, String>(len);
 		while (--len >= 0) {
 			String beanName = res.get(len).getName();
 			int i = beanName.indexOf('?');
 			String actionName = i > 0 ? beanName.substring(0, i) : beanName;
 			validate(namesMap.put(actionName, beanName) == null, "duplicated name: {}", actionName);
 		}
		Logger.LOG.info("webapp {}: resolver={}  actions={}", servletCtx.getContextPath() , resolverNames, namesMap.size());
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
 			ServletException {
 		HttpServletRequest httpreq = (HttpServletRequest) req;
 		String uri = (String) httpreq.getAttribute(Webutil.INCLUDE_REQUEST_URI);
 		if (uri == null)
 			uri = httpreq.getRequestURI();
 		int begin, end = uri.indexOf(';', begin = prefixLen);// ;jsessionid=xxxxx
 		if (end < 0)
 			end = uri.length();
 		if (end > begin && new Executor(httpreq, (HttpServletResponse) resp).forward(uri.substring(begin, end), null))
 			return;
 		chain.doFilter(req, resp);
 	}
 
 	@Override
 	public void destroy() {
 		// nothing
 	}
 
 	final class Executor extends WebContext implements Invocation {
 		private final HttpServletRequest request;
 		private final HttpServletResponse response;
 		private String name;
 		MethodInvocation invoc;
 
 		Executor(HttpServletRequest req, HttpServletResponse resp) {
 			request = req;
 			response = resp;
 		}
 
 		@Override
 		public HttpServletRequest getRequest() {
 			return request;
 		}
 
 		@Override
 		public HttpServletResponse getResponse() {
 			return response;
 		}
 
 		@Override
 		public ServletContext getServletContext() {
 			return servletCtx;
 		}
 
 		@Override
 		public Context getContext() {
 			return context;
 		}
 
 		@Override
 		public String getActionName() {
 			return name;
 		}
 
 		@Override
 		public Map<String, String> inject(Object obj) {
 			return Injector.injector.inject(request, obj);
 		}
 
 		@Override
 		boolean forward(String view, Map<String, Object> model) throws ServletException, IOException {
 			String beaname = namesMap.get(view);
 			if (beaname != null) {
 				name = view;
 				if (model != null) {
 					for (Entry<String, Object> entry : model.entrySet())
 						request.setAttribute(entry.getKey(), entry.getValue());
 				}
 				View v = ((Action) context.getBean(beaname)).execute(this);
 				invoc = null;
 				if (v != null)
 					v.handle(this);
 				return true;
 			}
 			int i = resolverNames.length;
 			while (--i >= 0) {
 				if (view.endsWith(resolverNames[i])) {
 					if (model == null)
 						model = new HashMap<String, Object>();
 					model.put("xutils", this);
 					resolvers[i].resolve(view, model, Webutil.getTemplateWriter(response));
 					return true;
 				}
 			}
 			return false;
 		}
 
 		@Override
 		public Object getAction() {
 			return invoc.getThis();
 		}
 
 		@Override
 		public String getBeanName() {
 			return namesMap.get(name);
 		}
 
 		@Override
 		public View proceed() throws ServletException, IOException {
 			try {
 				return (View) invoc.proceed();
 			} catch (ServletException e) {
 				throw e;
 			} catch (IOException e) {
 				throw e;
 			} catch (Throwable e) {
 				throw Objutil.rethrow(e);
 			}
 		}
 	}
 }
