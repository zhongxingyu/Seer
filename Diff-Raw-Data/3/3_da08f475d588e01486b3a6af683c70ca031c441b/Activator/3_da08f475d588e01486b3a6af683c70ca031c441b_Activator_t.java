 /*******************************************************************************
  * Copyright (c) 2011 Bug Labs, Inc.
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
 package com.buglabs.bug.appui;
 
 import java.awt.Frame;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 import com.buglabs.application.IDesktopApp;
 import com.buglabs.bug.module.lcd.pub.IModuleDisplay;
 import com.buglabs.util.LogServiceUtil;
import com.buglabs.util.osgi.BundleUtils;
 import com.buglabs.util.osgi.FilterUtil;
 
 /**
  * Activator for AppUI.  Listens to bundle and IDesktopApp service events.
  * @author kgilmer
  *
  */
 public class Activator implements BundleActivator, ServiceTrackerCustomizer, BundleListener, ServiceListener {
 
 	private ServiceTracker st;
 	private static BundleContext context;
 	private AppWindow appWindow;
 	private static LogService log;
 	private Map appBundles;
 	private Map launchListeners;
 	private Frame appFrame;
 	
 	protected static final Toolkit toolkit = Toolkit.getDefaultToolkit();
 
 	protected static Image BUNDLE_STOPPED;
 	protected static Image APP_ACTIVE;
 	protected static Image APP_INVERTED;
 	protected static Image BUNDLE_STARTED;
 	
 	public void start(BundleContext context) throws Exception {
 		Activator.context = context;
 
 		APP_ACTIVE = toolkit.createImage(context.getBundle().getResource("app_started.png"));
 		BUNDLE_STOPPED = toolkit.createImage(context.getBundle().getResource("bundle_stopped.png"));
 		BUNDLE_STARTED = toolkit.createImage(context.getBundle().getResource("bundle_started.png"));
 		APP_INVERTED = toolkit.createImage(context.getBundle().getResource("app_invert.png"));
 
 		appBundles = loadModel(context.getBundles());
 		launchListeners = loadLaunchListeners();
 
 		st = new ServiceTracker(context, IModuleDisplay.class.getName(), this);
 		st.open();
 		context.addBundleListener(this);
 		context.addServiceListener(this, FilterUtil.generateServiceFilter(IDesktopApp.class.getName()));
 	}
 	
 	/**
 	 * @return A log service
 	 */
 	public static LogService getLogService() {
 		if (log == null) {
 			log = LogServiceUtil.getLogService(context);
 		}
 		
 		return log;
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		st.close();
 		
 		if (appFrame != null) {
 			appFrame.dispose();
 		}
 		context.removeBundleListener(this);
 		context.removeServiceListener(this);
 	}
 
 	/**
 	 * @return A map containing bundle names and launch listeners.
 	 * @throws InvalidSyntaxException
 	 */
 	private Map loadLaunchListeners() throws InvalidSyntaxException {
 		ServiceReference[] srs = context.getServiceReferences(IDesktopApp.class.getName(), null);
 		Map model = new Hashtable();
 
 		if (srs != null) {
 			for (int i = 0; i < srs.length; ++i) {
 				Bundle b = srs[i].getBundle();
 				String name = BundleUtils.getBestName(b);
 
 				if (!model.containsKey(name)) {
 					model.put(name, context.getService(srs[i]));
 				}
 			}
 		}
 
 		return model;
 	}
 
 	public Object addingService(ServiceReference reference) {
 		IModuleDisplay display = (IModuleDisplay) context.getService(reference);
 		
 		appFrame = display.getFrame();
 		addWindowListeners(appFrame);
 		
 		appWindow = new AppWindow(appFrame);
 		appWindow.setLaunchClients(launchListeners);
 		appWindow.setVisible(true);
 		appWindow.setBundles(appBundles);
 		appWindow.refresh();
 
 		return display;
 	}
 	
 	/**
 	 * @return a map of existing app bundles.
 	 */
 	private Map loadModel(Bundle[] bundles) {
 		Map model = new Hashtable();
 
 		for (int i = 0; i < bundles.length; ++i) {
 			Bundle b = bundles[i];
 
 			if (isApp(b)) {
 				String name = BundleUtils.getBestName(b);
 
 				model.put(name, b);
 			}
 		}
 
 		return model;
 	}
 
 	/**
 	 * @param b
 	 * @return true if a given bundle is a BUG app
 	 */
 	private boolean isApp(Bundle b) {
 		Object o = b.getHeaders().get("Bug-Bundle-Type");
 
 		if (o != null && ((String) o).equals("Application")) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public void modifiedService(ServiceReference reference, Object service) {
 	}
 
 	public void removedService(ServiceReference reference, Object service) {
 		if (appWindow != null) {
 			appWindow.dispose();
 		}
 	}
 
 	public void bundleChanged(BundleEvent event) {
 		if (!isApp(event.getBundle())) {
 			return;
 		}
 		
 		String name = BundleUtils.getBestName(event.getBundle());
 		boolean refresh = false;
 
 		switch (event.getType()) {
 		case BundleEvent.INSTALLED:
 		case BundleEvent.UPDATED:
 		case BundleEvent.STARTED:
 		case BundleEvent.STOPPED:
 			appBundles.put(name, event.getBundle());
 			refresh = true;
 
 			break;
 		case BundleEvent.UNINSTALLED:
 			if (appBundles.containsKey(name)) {
 				appBundles.remove(name);
 				refresh = true;
 			}
 			break;
 		}
 
 		if (refresh && appWindow != null) {
 			appWindow.refresh();
 		}
 	}
 
 	public void serviceChanged(ServiceEvent event) {
 		String name = BundleUtils.getBestName(event.getServiceReference().getBundle());
 
 		switch (event.getType()) {
 		case ServiceEvent.REGISTERED:
 			if (!launchListeners.containsKey(name)) {
 				launchListeners.put(name, context.getService(event.getServiceReference()));
 			}
 			break;
 		case ServiceEvent.UNREGISTERING:
 			if (launchListeners.containsKey(name)) {
 				launchListeners.remove(name);
 			}
 		}
 	}
 	
 	private void addWindowListeners(final Frame f2) {
 		f2.addWindowListener(new WindowListener() {
 
 			public void windowActivated(WindowEvent arg0) {
 			}
 
 			public void windowClosed(WindowEvent arg0) {
 			}
 
 			public void windowClosing(WindowEvent arg0) {
 				appFrame.dispose();
 				appFrame = null;
 			}
 
 			public void windowDeactivated(WindowEvent arg0) {
 			}
 
 			public void windowDeiconified(WindowEvent arg0) {
 			}
 
 			public void windowIconified(WindowEvent arg0) {
 			}
 
 			public void windowOpened(WindowEvent arg0) {
 			}
 		});
 	}
 }
