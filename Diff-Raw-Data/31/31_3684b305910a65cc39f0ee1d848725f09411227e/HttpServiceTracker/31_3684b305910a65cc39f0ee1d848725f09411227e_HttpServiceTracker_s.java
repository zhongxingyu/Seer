 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.event;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
import com.buglabs.util.LogServiceUtil;
 
 /**
  * A utility class that will handle management of servlets when containers come
  * in and out of runtime scope.
  * 
  * @author ken
  * 
  */
 public class HttpServiceTracker implements ServiceTrackerCustomizer {
 	protected Hashtable config;
 
 	protected BundleContext context;
 
 	protected HttpService httpService;
 
 	protected final Map servlets;
 
 	protected final Servlet servlet;
 
 	protected final LogService logService;
 
 	protected final String alias;
 
 	/**
 	 * @param context
 	 * @param config
 	 * @param servlets -
 	 *            a Map of servlets, with the key being the path of the servlet
 	 *            on the http container.
 	 */
 	public HttpServiceTracker(BundleContext context, Hashtable config, Map servlets, LogService logService) {
 		this.config = config;
 		this.context = context;
 		this.servlets = servlets;
 		this.logService = logService;
 		this.servlet = null;
 		this.alias = null;
 	}
 
 	/**
 	 * @param context
 	 * @param config
 	 * @param servlet
 	 * @param logService
 	 */
 	public HttpServiceTracker(BundleContext context, Hashtable config, Servlet servlet, String alias, LogService logService) {
 		this.config = config;
 		this.context = context;
 		this.alias = alias;
 		this.servlets = null;
 		this.logService = logService;
 		this.servlet = servlet;
 	}
 
 	public HttpServiceTracker(BundleContext context2, Hashtable config2, Map servlets2) {
 		this.config = config2;
 		this.context = context2;
 		this.servlets = servlets2;
 		alias = null;
 		servlet = null;
 
 		logService = LogServiceUtil.getLogService(context);
 	}
 
 	public Object addingService(ServiceReference reference) {
 		httpService = (HttpService) context.getService(reference);
 
 		try {
 			if (servlet == null && servlets != null) {
 				for (Iterator i = servlets.keySet().iterator(); i.hasNext();) {
 					String path = (String) i.next();
 					httpService.registerServlet(path, (Servlet) servlets.get(path), config, null);
 					logService.log(LogService.LOG_INFO, "Registered servlet at " + path + ".");
 				}
 			} else {
 				httpService.registerServlet(alias, servlet, config, null);
 				logService.log(LogService.LOG_INFO, "Registered servlet at " + alias + ".");
 			}
 		} catch (Exception e) {
 			logService.log(LogService.LOG_ERROR, "Failed to register servlet.", e);
 		}
 
 		return reference;
 	}
 
 	public void modifiedService(ServiceReference reference, Object service) {
 
 	}
 
 	public void removedService(ServiceReference reference, Object service) {
 		for (Iterator i = servlets.keySet().iterator(); i.hasNext();) {
 			String path = (String) i.next();
 			httpService.unregister(path);
 			logService.log(LogService.LOG_DEBUG, "Unregistered servlet at " + path + ".");
 		}
 	}
 }
