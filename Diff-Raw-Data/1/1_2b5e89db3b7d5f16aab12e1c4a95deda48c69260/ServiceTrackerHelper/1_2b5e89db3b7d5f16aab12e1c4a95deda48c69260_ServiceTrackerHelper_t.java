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
 package com.buglabs.application;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.ServiceFilterGenerator;
 
 /**
  * Helper class to construct ServiceTrackers.
  * 
  * @author kgilmer
  * 
  */
 public class ServiceTrackerHelper implements ServiceTrackerCustomizer {
 
 	private final ManagedRunnable runnable;
 	private final String[] services;
 	private volatile int sc;
 	private final BundleContext bc;
 	private Thread thread;
 	private final Map serviceMap;
 	private LogService log;
 
 	/**
 	 * A runnable that provides access to OSGi services passed to
 	 * ServiceTrackerHelper.
 	 * 
 	 * Runnable will be started when all service are available, and interrupted
 	 * if any services become unavailable.
 	 * 
 	 * @author kgilmer
 	 * 
 	 */
 	public interface ManagedRunnable {
 		/**
 		 * This is called for execution of application logic when OSGi services are available.
 		 * @param services key contains String of service name, value is service instance.
 		 */
 		public abstract void run(Map services);
 		/**
 		 * Called directly before the thread is interrupted.  Client may optionally add necessary code to shutdown thread.
 		 */
 		public abstract void shutdown();
 	}
 
 	/**
 	 * A Runnable and ServiceTrackerCustomizer that allows for fine-grained
 	 * application behavior based on OSGi service activity. An implementation of
 	 * this can be passed anywhere a ServiceTrackerRunnable is expected and
 	 * behavior will change accordingly.
 	 * 
 	 * Runnable is not started automatically. It is up to the implementor to
 	 * decide when and how to create and start the thread.
 	 * 
 	 * @author kgilmer
 	 * @deprecated Use ServiceTrackerCustomizer instead.
 	 * 
 	 */
 	public interface UnmanagedRunnable extends ManagedRunnable, ServiceTrackerCustomizer {
 	}
 
 	/**
 	 * A ManagedRunnable that calls run() in-line with parent thread.  This is useful if the client
 	 * does not need to create a new thread or wants to manage thread creation independently. 
 	 * @author kgilmer
 	 *
 	 */
 	public interface ManagedInlineRunnable extends ManagedRunnable {
 	}
 
 	public ServiceTrackerHelper(BundleContext bc, ManagedRunnable t, String[] services) {
 		this.bc = bc;
 		this.runnable = t;
 		this.services = services;
 		this.serviceMap = new HashMap();
 		this.log = LogServiceUtil.getLogService(bc);
 
 		sc = 0;
 	}
 
 	public Object addingService(ServiceReference arg0) {
 		sc++;
 		Object svc = bc.getService(arg0);
 		String key = ((String []) arg0.getProperty(Constants.OBJECTCLASS))[0];
 		serviceMap.put(key, svc);
 
 		if (thread == null && sc == services.length && !(runnable instanceof UnmanagedRunnable)) {
 			if (runnable instanceof ManagedInlineRunnable) {
 				//Client wants to run in same thread, just call method.
 				runnable.run(serviceMap);
 			} else {
 				//Create new thread and pass off to client Runnable implementation.
 				thread = new Thread(new Runnable() {
 
 					public void run() {
 						runnable.run(serviceMap);
 					}
 				});
 				thread.start();
 			}
 		}
 
 		if (runnable instanceof UnmanagedRunnable) {
 			//Simply call the unmanaged runnable in-line.
 			return ((UnmanagedRunnable) runnable).addingService(arg0);
 		}
 
 		return svc;
 	}
 
 	public void modifiedService(ServiceReference arg0, Object arg1) {
 		String key = ((String []) arg0.getProperty(Constants.OBJECTCLASS))[0];
 		serviceMap.put(key, arg1);
 		
 		if (runnable instanceof UnmanagedRunnable) {
 			((UnmanagedRunnable) runnable).modifiedService(arg0, arg1);
 		}
 	}
 
 	public void removedService(ServiceReference arg0, Object arg1) {
 		String key = ((String []) arg0.getProperty(Constants.OBJECTCLASS))[0];
 		serviceMap.remove(key);
 		
 		sc--;
 		if (!(thread == null) && !thread.isInterrupted() && !(runnable instanceof UnmanagedRunnable)) {
 			runnable.shutdown();
 			thread.interrupt();
 			return;
 		}
 
 		if (runnable instanceof UnmanagedRunnable) {
 			((UnmanagedRunnable) runnable).removedService(arg0, arg1);
 		}
 		
 		if (runnable instanceof ManagedInlineRunnable) {
 			((ManagedInlineRunnable) runnable).shutdown();
 		}
 	}
 
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTrackerRunnable-based ServiceTracker.
 	 * 
 	 * @param context
 	 * @param services
 	 * @param runnable
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context, String[] services, ManagedRunnable runnable) throws InvalidSyntaxException {
 		ServiceTracker st = new ClosingServiceTracker(context, ServiceFilterGenerator.generateServiceFilter(context, services), new ServiceTrackerHelper(context, runnable, services), services);
 		st.open();
 		
 		return st;
 	}
 
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTrackerRunnable-based ServiceTracker.
 	 * 
 	 * @param context
 	 * @param services
 	 * @param filter
 	 * @param runnable
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context, String[] services, Filter filter, ManagedRunnable runnable) throws InvalidSyntaxException {
 		ServiceTracker st = new ClosingServiceTracker(context, filter, new ServiceTrackerHelper(context, runnable, services), services);
 		st.open();
 
 		return st;
 	}
 	
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTracker.
 	 * 
 	 * @param context
 	 * @param services
 	 * @param filter
 	 * @param customizer
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context, String[] services, Filter filter, ServiceTrackerCustomizer customizer) throws InvalidSyntaxException {
 		ServiceTracker st = new ClosingServiceTracker(context, filter, customizer, services);
 		st.open();
 
 		return st;
 	}
 
 	/**
 	 * @param context
 	 *            BundleContext
 	 * @param services
 	 *            Services to be tracked
 	 * @param runnable
 	 *            Object handling service changes
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker createAndOpen(BundleContext context, List services, RunnableWithServices runnable) throws InvalidSyntaxException {
 		Filter filter = context.createFilter(ServiceFilterGenerator.generateServiceFilter(services));
 		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizerAdapter(context, runnable, services));
 		st.open();
 
 		return st;
 	}
 
 	/**
 	 * @param context
 	 *            BundleContext
 	 * @param services
 	 *            Services to be tracked
 	 * @param runnable
 	 *            Object handling service changes
 	 * @return
 	 * @throws InvalidSyntaxException
 	 * 
 	 */
 	public static ServiceTracker createAndOpen(BundleContext context, String[] services, RunnableWithServices runnable) throws InvalidSyntaxException {
 
 		Filter filter = context.createFilter(ServiceFilterGenerator.generateServiceFilter(Arrays.asList(services)));
 		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizerAdapter(context, runnable, Arrays.asList(services)));
 		st.open();
 
 		return st;
 	}
 
 	/**
 	 * @param context
 	 *            BundleContext
 	 * @param service
 	 *            Service to be tracked
 	 * @param runnable
 	 *            Object handling service changes
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker createAndOpen(BundleContext context, String service, RunnableWithServices runnable) throws InvalidSyntaxException {
 		Filter filter = context.createFilter(ServiceFilterGenerator.generateServiceFilter(Arrays.asList(new String[] { service })));
 		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizerAdapter(context, runnable, Arrays.asList(new String[] { service })));
 		st.open();
 
 		return st;
 	}
 
 	/**
 	 * @param context
 	 *            BundleContext
 	 * @param services
 	 *            Services to be tracked
 	 * @param runnable
 	 *            Object handling service changes
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker createAndOpen(BundleContext context, List services, ServiceChangeListener runnable) throws InvalidSyntaxException {
 		Filter filter = context.createFilter(ServiceFilterGenerator.generateServiceFilter(services));
 		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizerAdapter(context, runnable, services));
 		st.open();
 
 		return st;
 	}
 
 	/**
 	 * @param context
 	 *            BundleContext
 	 * @param services
 	 *            Services to be tracked
 	 * @param runnable
 	 *            Object handling service changes
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker createAndOpen(BundleContext context, String[] services, ServiceChangeListener runnable) throws InvalidSyntaxException {
 
 		Filter filter = context.createFilter(ServiceFilterGenerator.generateServiceFilter(Arrays.asList(services)));
 		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizerAdapter(context, runnable, Arrays.asList(services)));
 		st.open();
 
 		return st;
 	}
 
 	/**
 	 * @param context
 	 *            BundleContext
 	 * @param services
 	 *            Services to be tracked
 	 * @param runnable
 	 *            Object handling service changes
 	 * @return
 	 * @throws InvalidSyntaxException
 	 */
 	public static ServiceTracker createAndOpen(BundleContext context, String service, ServiceChangeListener runnable) throws InvalidSyntaxException {
 		Filter filter = context.createFilter(ServiceFilterGenerator.generateServiceFilter(Arrays.asList(new String[] { service })));
 		ServiceTracker st = new ServiceTracker(context, filter, new ServiceTrackerCustomizerAdapter(context, runnable, Arrays.asList(new String[] { service })));
 		st.open();
 
 		return st;
 	}
 }
