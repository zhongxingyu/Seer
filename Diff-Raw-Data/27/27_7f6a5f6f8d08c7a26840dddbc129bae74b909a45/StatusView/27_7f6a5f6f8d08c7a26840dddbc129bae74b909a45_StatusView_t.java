 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.gui.impl.servlets;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.osgi.framework.InvalidSyntaxException;
 import org.paxle.core.IMWComponent;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.IServiceManager;
 
 public class StatusView extends ALayoutServlet {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
 		try {  			
 			if (request.getParameter("pauseCrawl") != null) {
 				IMWComponent<?> crawler = this.getCrawler();
 				if (crawler != null) crawler.pause();
 				response.sendRedirect(request.getServletPath() + "#dcrawler");
 			} else if (request.getParameter("resumeCrawl") != null) {
 				IMWComponent<?> crawler = this.getCrawler();
 				if (crawler != null) crawler.resume();
 				response.sendRedirect(request.getServletPath() + "#dcrawler");
 			} else if (request.getParameter("processNextCrawl") != null) {
 				IMWComponent<?> crawler = this.getCrawler();
 				if (crawler != null) crawler.processNext();
 				response.sendRedirect(request.getServletPath() + "#dcrawler");
 			} else if (request.getParameter("shutdown") != null) {
 				// redirecting to shutdown-servlet
 				// TODO: servlet-path prefix handling required here
				response.sendRedirect("/sysdown?action=shutdown");
 			} else if (request.getParameter("restart") != null) {
 				// redirecting to shutdown-servlet
 				// TODO: servlet-path prefix handling required here
				response.sendRedirect("/sysdown?action=restart");
 			} else {		
 				super.doRequest(request, response);
 			}
 		} catch (Throwable e) {
 			this.logger.error(e);
 		}		
 	}
 	
 	@Override
 	protected void fillContext(Context context, HttpServletRequest request) {
 		try {        	
 			// adding servlet to context
 			context.put("statusView",this);
 			context.put("servletContext", this.getServletConfig().getServletContext());
 		} catch (Throwable e) {
 			this.logger.error(e);
 		}
 	}
 	
 	/**
 	 * Choosing the template to use 
 	 */
 	@Override
 	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
 		return this.getTemplate("/resources/templates/StatusView.vm");
 	}	
 	
 	private IMWComponent<?> getCrawler() throws InvalidSyntaxException {
 		IServiceManager sm = this.getServiceManager();
 		Object[] crawlers = sm.getServices("org.paxle.core.IMWComponent","(component.ID=org.paxle.crawler)");		
 		if (crawlers == null && crawlers.length == 0) return null;
 		return (IMWComponent<?>) crawlers[0];
 	}
 	
 	/**
 	 * @return a reference to the {@link net.sf.ehcache.CacheManager EhCache-Cachemanager}
 	 */
 	public Object getCacheManager() {
 		try {
 			return Thread.currentThread().getContextClassLoader()
 				.loadClass("net.sf.ehcache.CacheManager")
 				.getMethod("getInstance", (Class[]) null)
 				.invoke(null, (Object[]) null);
 		} catch (Throwable e) {
 			this.logger.error(e);
 			return null;
 		}			
 	}
 }
