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
 package com.buglabs.bug.base;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Map;
 
import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
import com.buglabs.util.osgi.BUGBundleConstants;
import com.buglabs.util.osgi.ServiceTrackerUtil;
import com.buglabs.util.osgi.ServiceTrackerUtil.ManagedRunnable;
 import com.buglabs.bug.base.bug20.pub.IBUG20BaseControl;
 import com.buglabs.bug.base.bug20.pub.ITimeProvider;
 import com.buglabs.bug.buttons.ButtonEvent;
 import com.buglabs.bug.buttons.IButtonEventListener;
 import com.buglabs.bug.buttons.IButtonEventProvider;
 import com.buglabs.bug.input.pub.InputEventProvider;
 import com.buglabs.support.SupportInfoTextFormatter;
 import com.buglabs.support.SupportInfoXMLFormatter;
 import com.buglabs.util.osgi.LogServiceUtil;
 
 /**
  * This bundle offers base unit features to the runtime, such as date/time and
  * power information.
  * 
  * @author kgilmer
  * 
  */
 public class Activator implements BundleActivator, ITimeProvider, IButtonEventListener, ManagedRunnable {
 
 	private static final String BUG_BASE_VERSION_KEY = "bug.base.version";
 
 	private static final String INFO_SERVLET_PATH = "/support";
 
 	private static final String INFO_SERVLET_HTML_PATH = "/support.html";
 
 	private static final String DEVNODE_BUGNAV = "/dev/input/user_button";
 
 	private static final String DEVNODE_BUGPOWER = "/dev/input/power_button";
 
 	/**
 	 * Location where static web content will be registered with web server.
 	 */
 	private static final String ROOT_ALIAS = "/";
 
 	/**
 	 * Sleep delay for blinking LED.
 	 */
 	protected static final long LED_SLEEP_DELAY = 300;
 
 	private static Activator ref;
 
 	private ServiceRegistration timeReg;
 
 	private static LogService logService;
 
 	private BUGBaseControl bbc;
 
 	private ServiceRegistration baseControlReg;
 
 	private ServiceTracker httpTracker;
 
 	private InputEventProvider userbep;
 
 	private ServiceRegistration userBepReg;
 
 	private BundleContext context;
 
 	private InputEventProvider powerbep;
 
 	private ServiceRegistration powerBepReg;
 
 	private HttpService httpService;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.buglabs.bug.base.pub.ITimeProvider#getTime()
 	 */
 	public Date getTime() {
 		return Calendar.getInstance().getTime();
 	}
 
 	/**
 	 * Register all the OSGi services that this bundle provides.
 	 * 
 	 * @param context
 	 *            BundleContext
 	 */
 	private void registerServices(BundleContext context) {
 		timeReg = context.registerService(ITimeProvider.class.getName(), this, null);
 		userBepReg = context.registerService(IButtonEventProvider.class.getName(), userbep, getUserButtonProperties());
 		powerBepReg = context.registerService(IButtonEventProvider.class.getName(), powerbep, getPowerButtonProperties());
 
 		if (bbc != null) {
 			baseControlReg = context.registerService(IBUG20BaseControl.class.getName(), bbc, getBaseControlServiceProperties());
 		}
 	}
 
 	/**
 	 * @return Dictionary of properties for power button.
 	 */
 	private Dictionary<String, String> getPowerButtonProperties() {
 		Dictionary<String, String> d = new Hashtable<String, String>();
 		d.put(BUGBundleConstants.MODULE_PROVIDER_KEY, this.getClass().getName());
 		d.put("Button", "Power");
 		return d;
 	}
 
 	/**
 	 * @return Dictionary of properties for user button.
 	 */
 	private Dictionary<String, String> getUserButtonProperties() {
 		Dictionary<String, String> d = new Hashtable<String, String>();
 		d.put(BUGBundleConstants.MODULE_PROVIDER_KEY, this.getClass().getName());
 		d.put("Button", "User");
 		return d;
 	}
 
 	/**
 	 * @return A dictionary with properties for base control
 	 */
 	private Dictionary<String, String> getBaseControlServiceProperties() {
 		Dictionary<String, String> d = new Hashtable<String, String>();
 
 		d.put(BUG_BASE_VERSION_KEY, getBaseVersion());
 
 		return d;
 	}
 
 	/**
 	 * @return version of BUG base this code is running on.
 	 */
 	private String getBaseVersion() {
 		return "2.0";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(final BundleContext context) throws Exception {
 		this.context = context;
 		ref = this;
 		logService = LogServiceUtil.getLogService(context);
 
 		// Set base version property.
 		System.setProperty(BUG_BASE_VERSION_KEY, getBaseVersion());
 		try {
 			bbc = new BUGBaseControl();
 			// see http://redmine.buglabs.net/issues/show/1424
 			bbc.setLEDTrigger(IBUG20BaseControl.LED_POWER, IBUG20BaseControl.COLOR_BLUE, "none");
 			bbc.setLEDBrightness(IBUG20BaseControl.LED_POWER, 255);
 		} catch (FileNotFoundException e) {
 			logService.log(LogService.LOG_ERROR, "Unable to initialize LEDs.  " + e.getMessage());
 		}
 
 		userbep = new InputEventProvider(DEVNODE_BUGNAV, logService);
 		userbep.start();
 
 		powerbep = new InputEventProvider(DEVNODE_BUGPOWER, logService);
 		powerbep.start();
 
 		// listen for the power button to be hit, then toggle the LED sequence
 		// for user feedback. see
 		// http://redmine.buglabs.net/issues/show/1429#note-4
 		powerbep.addListener(this);
 
 		registerServices(context);
 
 		httpTracker = ServiceTrackerUtil.openServiceTracker(context, this, new String[] { HttpService.class.getName() });
 
 		signalStartup();
 	}
 
 	/**
 	 * Signal to user that OSGi runtime is up and running.
 	 */
 	private void signalStartup() {
 		(new Thread(new Runnable() {
 
 			public void run() {
 				try {
 					bbc.setLEDColor(IBUG20BaseControl.LED_POWER, IBUG20BaseControl.COLOR_RED, true);
 					Thread.sleep(LED_SLEEP_DELAY);
 					bbc.setLEDColor(IBUG20BaseControl.LED_POWER, IBUG20BaseControl.COLOR_GREEN, true);
 					Thread.sleep(LED_SLEEP_DELAY);
 					bbc.setLEDColor(IBUG20BaseControl.LED_POWER, IBUG20BaseControl.COLOR_BLUE, true);
 				} catch (Exception e) {
 					// Ignore error
 				}
 			}
 
 		})).start();
 	}
 
 	private void signalShutdown() {
 		Thread t = new Thread(new Runnable() {
 
 			public void run() {
 				try {
 					logService.log(LogService.LOG_INFO, "Power Button engaged: base bundle signalling via LEDs shutdown sequence initiated");
 					bbc.setLEDTrigger(IBUG20BaseControl.LED_POWER, IBUG20BaseControl.COLOR_BLUE, "heartbeat");
 
 				} catch (Exception e) {
 				}
 			}
 
 		});
 		t.start();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		httpTracker.close();
 		unregisterServices(context);
 	}
 
 	/**
 	 * @param context BundleContext
 	 */
 	private void unregisterServices(BundleContext context) {
 		timeReg.unregister();
 		if (baseControlReg != null) {
 			baseControlReg.unregister();
 		}
 		userBepReg.unregister();
 		powerBepReg.unregister();
 	}
 
 	/**
 	 * @return Activator instance
 	 */
 	public static Activator getDefault() {
 		return ref;
 	}
 
 	/**
 	 * @return BundleContext
 	 */
 	public BundleContext getBundleContext() {
 		return context;
 	}
 
 	@Override
 	public void buttonEvent(ButtonEvent event) {
 
 		if (event.getButton() == ButtonEvent.BUTTON_BUG20_POWER && event.getAction() == ButtonEvent.KEY_UP) {
 			logService.log(LogService.LOG_DEBUG, "base bundle received Power Button event: " + event.getButton());
 			signalShutdown();
 		}
 
 	}
 
 	@Override
 	public void run(Map<Object, Object> services) {
 		this.httpService = (HttpService) services.get(HttpService.class.getName());
 
 		try {
 			logService.log(LogService.LOG_INFO, "Registering base servlets.");
 
 			// register xml version
 			httpService.registerServlet(
 					INFO_SERVLET_PATH, new SupportServlet(
 							new BUGSupportInfo(context), new SupportInfoXMLFormatter()), null, null);
 			// register html version
 			httpService.registerServlet(
 					INFO_SERVLET_HTML_PATH, new SupportServlet(
 							new BUGSupportInfo(context), new SupportInfoTextFormatter()), null, null);
 			// register static root web content
 			httpService.registerResources(ROOT_ALIAS, "static", new StaticResourceContext("static" + ROOT_ALIAS));
 		} catch (Exception e) {
 			logService.log(LogService.LOG_ERROR, "An error occurred registering servlet or resource: " + e.getMessage());
 		} 
 	}
 
 	@Override
 	public void shutdown() {
 		if (httpService != null) {
 			httpService.unregister(INFO_SERVLET_PATH);
 			httpService.unregister(INFO_SERVLET_HTML_PATH);
 			httpService.unregister(ROOT_ALIAS);
 			httpService = null;
 		}
 	}
 
 	/**
 	 * HttpContext for static resource included in bundle.
 	 * TODO: Determine if this class is necessary or servlet can be registered w/ null HttpContext.
 	 */
 	private class StaticResourceContext implements HttpContext {
 
 		private final String root;
 
 		public StaticResourceContext(String root) {
 			this.root = root;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
 		 */
 		public String getMimeType(String name) {
 			return null;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
 		 */
 		public URL getResource(String name) {
 			if (name.equals(root))
 				name = name + "index.html";
 			
 			return context.getBundle().getResource(name);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 		 */
 		public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
 			return true;
 		}
 	}
 
 	/**
 	 * @return Activator's instance of log service.
 	 */
 	public static LogService getLog() {
 		return logService;
 	}
 }
