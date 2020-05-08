 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation
  *     xored software, Inc. - remove DbgpService dependency on DLTKDebugPlugin preferences (Alex Panchenko) 
  *******************************************************************************/
 package org.eclipse.dltk.debug.core;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.internal.debug.core.model.DbgpService;
 import org.eclipse.dltk.internal.debug.core.model.HotCodeReplaceManager;
 import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
 import org.osgi.framework.BundleContext;
 
 public class DLTKDebugPlugin extends Plugin {
 
 	private static final String LOCALHOST = "127.0.0.1"; //$NON-NLS-1$
 
 	public static final String PLUGIN_ID = "org.eclipse.dltk.debug"; //$NON-NLS-1$
 
 	public static final int INTERNAL_ERROR = 120;
 
 	private static DLTKDebugPlugin fgPlugin;
 
 	public static DLTKDebugPlugin getDefault() {
 		return fgPlugin;
 	}
 
 	public DLTKDebugPlugin() {
 		super();
 		fgPlugin = this;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		HotCodeReplaceManager.getDefault().startup();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		HotCodeReplaceManager.getDefault().shutdown();
 
 		super.stop(context);
 
 		if (dbgpService != null) {
 			dbgpService.shutdown();
 		}
 
 		ILaunchManager launchManager = DebugPlugin.getDefault()
 				.getLaunchManager();
 		IDebugTarget[] targets = launchManager.getDebugTargets();
 		for (int i = 0; i < targets.length; i++) {
 			IDebugTarget target = targets[i];
 			if (target instanceof ScriptDebugTarget) {
 				((ScriptDebugTarget) target).shutdown();
 			}
 		}
 	}
 
 	private DbgpService dbgpService;
 
 	public synchronized IDbgpService getDbgpService() {
 		if (dbgpService == null) {
 			dbgpService = new DbgpService(getPreferencePort());
 			getPluginPreferences().addPropertyChangeListener(
 					new DbgpServicePreferenceUpdater());
 		}
 		return dbgpService;
 	}
 
 	private class DbgpServicePreferenceUpdater implements
 			IPropertyChangeListener {
 
 		public void propertyChange(PropertyChangeEvent event) {
 			final String property = event.getProperty();
 			if (DLTKDebugPreferenceConstants.PREF_DBGP_PORT.equals(property)) {
 				if (dbgpService != null) {
 					dbgpService.restart(getPreferencePort());
 				}
 			}
 		}
 
 	}
 
 	// Logging
 	public static void log(Throwable t) {
 		Throwable top = t;
 		if (t instanceof DebugException) {
 			Throwable throwable = ((DebugException) t).getStatus()
 					.getException();
 			if (throwable != null) {
 				top = throwable;
 			}
 		}
 
 		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR,
 				Messages.DLTKDebugPlugin_internalErrorLoggedFromDltkDebugPlugin
 						+ top.getMessage(), top));
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public static void logWarning(String message, Throwable t) {
 		log(new Status(IStatus.WARNING, PLUGIN_ID, INTERNAL_ERROR, message, t));
 	}
 
	public static void logError(String message) {
		logError(message, null);
	}

 	public static void logError(String message, Throwable t) {
 		Throwable top = t;
 		if (t instanceof DebugException) {
 			Throwable throwable = ((DebugException) t).getStatus()
 					.getException();
 			if (throwable != null) {
 				top = throwable;
 			}
 		}
 
 		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, message, top));
 	}
 
 	private int getPreferencePort() {
 		return getPluginPreferences().getInt(
 				DLTKDebugPreferenceConstants.PREF_DBGP_PORT);
 	}
 
 	public String getBindAddress() {
 		String address = getPluginPreferences().getString(
 				DLTKDebugPreferenceConstants.PREF_DBGP_BIND_ADDRESS);
 		if (DLTKDebugPreferenceConstants.DBGP_AUTODETECT_BIND_ADDRESS
 				.equals(address)
 				|| address.trim().length() == 0) {
 			String[] ipAddresses = DLTKDebugPlugin.getLocalAddresses();
 			if (ipAddresses.length > 0) {
 				address = ipAddresses[0];
 			} else {
 				address = LOCALHOST;
 			}
 		}
 		return address;
 	}
 
 	public static int getConnectionTimeout() {
 		return getDefault().getPluginPreferences().getInt(
 				DLTKDebugPreferenceConstants.PREF_DBGP_CONNECTION_TIMEOUT);
 	}
 
 	public static String[] getLocalAddresses() {
 		Set addresses = new HashSet();
 		try {
 			Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
 			while (netInterfaces.hasMoreElements()) {
 				NetworkInterface ni = (NetworkInterface) netInterfaces
 						.nextElement();
 				// ignore virtual interfaces for VMware, etc
 				if (ni.getName().startsWith("vmnet")) { //$NON-NLS-1$
 					continue;
 				}
 				if (ni.getDisplayName() != null
 						&& ni.getDisplayName().indexOf("VMware") != -1) { //$NON-NLS-1$
 					continue;
 				}
 				Enumeration inetAddresses = ni.getInetAddresses();
 				while (inetAddresses.hasMoreElements()) {
 					InetAddress ip = (InetAddress) inetAddresses.nextElement();
 					// ignore loopback address (127.0.0.1)
 					// use only IPv4 addresses (ignore IPv6)
 					if (!ip.isLoopbackAddress() && ip.getAddress().length == 4) {
 						addresses.add(ip.getHostAddress());
 					}
 				}
 			}
 
 			if (addresses.isEmpty()) {
 				addresses.add(InetAddress.getLocalHost().getHostAddress());
 			}
 		} catch (SocketException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} catch (UnknownHostException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		return (String[]) addresses.toArray(new String[addresses.size()]);
 	}
 
 }
