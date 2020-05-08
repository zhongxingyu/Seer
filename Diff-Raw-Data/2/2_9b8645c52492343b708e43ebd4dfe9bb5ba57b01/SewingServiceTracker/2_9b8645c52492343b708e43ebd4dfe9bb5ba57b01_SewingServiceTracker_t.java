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
 package com.buglabs.osgi.sewing.servicetracker;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.http.HttpService;
 
 import com.buglabs.application.AbstractServiceTracker;
 import com.buglabs.osgi.sewing.SewingServiceImpl;
 import com.buglabs.osgi.sewing.pub.ISewingService;
 
 /**
  * Service tracker for the BugApp Bundle;
  * 
  */
 public class SewingServiceTracker extends AbstractServiceTracker {
 
 	private HttpService http_service;
 	private BundleContext bundle_context;
 	private ServiceRegistration sewing_registration;
 
 	public SewingServiceTracker(BundleContext context) {
 		super(context);
 		bundle_context = context;
 	}
 
 	/**
 	 * Determines if the application can start.
 	 */
 	public boolean canStart() {
 		return super.canStart();
 	}
 
 	/**
 	 * If canStart returns true this method is called to start the application.
 	 * Place your fun logic here.
 	 */
 	public void doStart() {
 		http_service = (HttpService) this.getService(HttpService.class);
 		sewing_registration = bundle_context.registerService(ISewingService.class.getName(), new SewingServiceImpl(http_service), null);
 	}
 
 	/**
 	 * Called when a service that this application depends is unregistered.
 	 */
 	public void doStop() {
 		sewing_registration.unregister();
 	}
 
 	/**
 	 * Allows the user to set the service dependencies by adding them to
 	 * services list returned by getServices(). i.e.nl
 	 * getServices().add(MyService.class.getName());
 	 */
 	public void initServices() {
		getServices().add(org.osgi.service.http.HttpService.class.getName());
 	}
 }
