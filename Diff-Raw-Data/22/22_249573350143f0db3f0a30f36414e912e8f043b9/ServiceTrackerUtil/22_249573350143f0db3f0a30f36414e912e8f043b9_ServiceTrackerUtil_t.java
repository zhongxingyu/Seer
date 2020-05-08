 /*******************************************************************************
  * Copyright (c) 2008 - 2011 Bug Labs, Inc.
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
 package com.buglabs.util.osgi;
 
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 /**
  * Helper class to manage services in OSGi using the ServiceTracker API.
  * 
  * Clients call the static method openServiceTracker() with a ManagedRunnable, ManagedInlineRunnable, or ServiceTrackerCustomizer.
  * For the first two, run() is called once and only once when all service dependencies are met, and shutdown()
  * is called once and only once when a previously active tracker has lost a required service.
  * 
  * When passing in a ServiceTrackerCustomizer, normal (standard ServiceTracker) behaviour is followed.
  * 
  * @author kgilmer
  * 
  */
 public class ServiceTrackerUtil implements ServiceTrackerCustomizer {
 
 	private final ManagedRunnable runnable;
 	private volatile int sc;
 	private final BundleContext bc;
 	private Thread thread;
	private final Map<String, Object> serviceMap;
 	private int serviceCount;
 	private volatile boolean runCalled;
 	private volatile boolean shutdownCalled;
 
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
		void run(Map<String, Object> services);
 		/**
 		 * Called directly before the thread is interrupted.  Client may optionally add necessary code to shutdown thread.
 		 */
 		void shutdown();
 	}
 
 	/**
 	 * A ManagedRunnable that calls run() in-line with parent thread.  This is useful if the client
 	 * does not need to create a new thread or wants to manage thread creation independently. 
 	 * @author kgilmer
 	 *
 	 */
 	public interface ManagedInlineRunnable extends ManagedRunnable {
 	}
 
 	/**
 	 * @param context BundleContext
 	 * @param runnable ManagedRunnable
 	 * @param serviceCount number of services tracked
 	 */
 	public ServiceTrackerUtil(BundleContext context, ManagedRunnable runnable, int serviceCount) {
 		this.bc = context;
 		this.runnable = runnable;
 		this.serviceCount = serviceCount;
		this.serviceMap = new Hashtable<String, Object>();
 		this.runCalled = false;
 		this.shutdownCalled = false;
 
 		sc = 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
 	 */
 	public Object addingService(ServiceReference arg0) {
 		Object svc = bc.getService(arg0);
 		String key = ((String []) arg0.getProperty(Constants.OBJECTCLASS))[0];
 		
 		if (!serviceMap.containsKey(key)) {
 			sc++;
 			serviceMap.put(key, svc);
 			//Store a dictionary of all the services properties.
			serviceMap.put(key + ".properties", getProperties(arg0));
 		}
 
 		if (thread == null && sc == serviceCount && !runCalled) {
 			synchronized (runnable) {
 				if (runnable instanceof ManagedInlineRunnable) {
 					//Client wants to run in same thread, just call method.
 					runnable.run(serviceMap);
 					runCalled = true;
 					shutdownCalled = false;
 				} else {
 					//Create new thread and pass off to client Runnable implementation.
 					thread = new Thread(new Runnable() {
 
 						public void run() {
 							runnable.run(serviceMap);
 						}
 					});
 					thread.start();
 					runCalled = true;
 					shutdownCalled = false;
 				}
 			}
 		}
 
 		return svc;
 	}
 
 	/**
 	 * @param arg0 ServiceReference
 	 * @return A dictionary containing all of a service reference's properties.
 	 */
 	private Dictionary<String, Object> getProperties(ServiceReference arg0) {
 		Dictionary<String, Object> dict = new Hashtable<String, Object>();
 		
 		if (arg0.getPropertyKeys() != null) {
 			for (String key : Arrays.asList(arg0.getPropertyKeys())) {
 				dict.put(key, arg0.getProperty(key));
 			}
 		}
 		
 		return dict;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
 	 */
 	public void modifiedService(ServiceReference arg0, Object arg1) {
 		String key = ((String []) arg0.getProperty(Constants.OBJECTCLASS))[0];
 		serviceMap.put(key, arg1);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
 	 */
 	public void removedService(ServiceReference arg0, Object arg1) {
 		if (shutdownCalled)
 			return;
 		
 		String key = ((String []) arg0.getProperty(Constants.OBJECTCLASS))[0];
 		
 		if (serviceMap.containsKey(key)) {
 			serviceMap.remove(key);
 			sc--;
 		}
 		
 		if (!(thread == null) && !thread.isInterrupted()) {
 			try {
 				runnable.shutdown();
 			} catch (Exception e) {
				if (arg0 != null && arg0.getBundle() != null && arg0.getBundle().getBundleContext() != null) {
					LogService ls = LogServiceUtil.getLogService(arg0.getBundle().getBundleContext());
					if (ls != null) {
						ls.log(LogService.LOG_ERROR, "An error occured while shutting down ManagedRunnable.", e);
					}
				}				
 			}
 			thread.interrupt();
 			thread = null;
 			shutdownCalled = true;
 			runCalled = false;
 			
 			return;
 		}
 		
 		if (runnable instanceof ManagedInlineRunnable) {
 			((ManagedInlineRunnable) runnable).shutdown();
 			shutdownCalled = true;
 			runCalled = false;
 		}
 	}
 
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTrackerRunnable-based ServiceTracker.
 	 * 
 	 * @param context BundleContext
 	 * @param runnable ManagedRunnable
 	 * @param services Service or array of services
 	 * @return An instance of ClosingServiceTracker that will call shutdown() on IManagedRunnable instances when closing.
 	 * @throws InvalidSyntaxException on Filter syntax error.
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context
 			, ManagedRunnable runnable, String ... services) throws InvalidSyntaxException {
 		
 		ServiceTracker st = new ClosingServiceTracker(context, FilterUtil.generateServiceFilter(context, services)
 				, new ServiceTrackerUtil(context, runnable, services.length), services);
 		st.open();
 		
 		return st;
 	}
 
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTrackerRunnable-based ServiceTracker.
 	 * 
 	 * @param context BundleContext
 	 * @param filter OSGi filter
 	 * @param runnable ManagedRunnable
 	 * @param services Service or array of services
 	 * @return An instance of ClosingServiceTracker that will call shutdown() on IManagedRunnable instances when closing.
 	 * @throws InvalidSyntaxException on Filter syntax error.
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context, Filter filter
 			, ManagedRunnable runnable, String ... services) throws InvalidSyntaxException {
 		
 		ServiceTracker st = new ClosingServiceTracker(context
 				, filter, new ServiceTrackerUtil(context, runnable, services.length), services);
 		st.open();
 
 		return st;
 	}
 	
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTracker.
 	 * 
 	 * @param context BundleContext
 	 * @param filter OSGi filter
 	 * @param customizer ServiceTrackerCustomizer
 	 * @param services Service or array of services
 	 * @return An instance of ClosingServiceTracker that will call shutdown() on IManagedRunnable instances when closing.
 	 * @throws InvalidSyntaxException on Filter syntax error.
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context, Filter filter
 			, ServiceTrackerCustomizer customizer, String ... services) throws InvalidSyntaxException {
 		ServiceTracker st = new ClosingServiceTracker(context, filter, customizer, services);
 		st.open();
 
 		return st;
 	}
 	
 	/**
 	 * Convenience method for creating and opening a
 	 * ServiceTracker.
 	 * 
 	 * @param context BundleContext
 	 * @param customizer ServiceTrackerCustomizer
 	 * @param services Service or array of services
 	 * @return An instance of ClosingServiceTracker that will call shutdown() on IManagedRunnable instances when closing.
 	 * @throws InvalidSyntaxException on Filter syntax error.
 	 */
 	public static ServiceTracker openServiceTracker(BundleContext context, ServiceTrackerCustomizer customizer
 			, String ... services) throws InvalidSyntaxException {
 		ServiceTracker st = new ClosingServiceTracker(context
 				, FilterUtil.generateServiceFilter(context, services), customizer, services);
 		st.open();
 
 		return st;
 	}
 }
