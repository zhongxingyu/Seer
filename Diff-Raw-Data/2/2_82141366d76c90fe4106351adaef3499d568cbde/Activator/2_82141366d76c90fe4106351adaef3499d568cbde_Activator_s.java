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
 
 package com.buglabs.osgi.sewing;
 
 import java.util.Map;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.osgi.sewing.pub.ISewingService;
 import com.buglabs.util.osgi.FilterUtil;
 import com.buglabs.util.osgi.LogServiceUtil;
 import com.buglabs.util.osgi.ServiceTrackerUtil;
 import com.buglabs.util.osgi.ServiceTrackerUtil.ManagedInlineRunnable;
 
 /**
  * BundleActivator for Sewing.
  * 
  */
 public class Activator implements BundleActivator, ManagedInlineRunnable {
 
 	private HttpService httpService;
 	private LogService log;
 	private BundleContext context;
	private ServiceRegistration<?> sewingRegistration;
 	private ServiceTracker stc;
 
 	//private SewingServiceTracker stc;
 	//private ServiceTracker st;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		LogManager.setContext(context);
 		log = LogServiceUtil.getLogService(context);
 		// Create the service tracker and run it.
 		stc = ServiceTrackerUtil.openServiceTracker(context, HttpService.class.getName(), this);		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		stc.close();
 	}
 
 	@Override
 	public void run(Map<Object, Object> services) {
 		httpService = (HttpService) services.get(HttpService.class.getName());
 		sewingRegistration = context.registerService(ISewingService.class.getName(), new SewingServiceImpl(httpService), null);
 	}
 
 	@Override
 	public void shutdown() {
 		if (sewingRegistration != null) {
 			sewingRegistration.unregister();
 			sewingRegistration = null;
 		}
 	}
 }
