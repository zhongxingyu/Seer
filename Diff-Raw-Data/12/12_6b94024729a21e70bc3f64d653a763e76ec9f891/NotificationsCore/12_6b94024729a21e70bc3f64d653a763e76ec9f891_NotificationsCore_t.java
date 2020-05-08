 /**
  * Copyright (c) 2011 Ericsson and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Ericsson Research Canada - Initial API and implementation
  * 
  */
 package org.eclipse.mylyn.reviews.notifications.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.mylyn.commons.core.StatusHandler;
 import org.eclipse.mylyn.reviews.notifications.internal.core.MeetingData;
 import org.eclipse.mylyn.reviews.notifications.spi.NotificationsConnector;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author Alvaro Sanchez-Leon
  * 
  */
 public class NotificationsCore {
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	public static final String	PLUGIN_ID	= "org.eclipse.mylyn.reviews.notifications";	//$NON-NLS-1$
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	/**
 	 * @return
 	 */
 	public static List<String> getConnectorIds() {
 		List<String> connectorIds = new ArrayList<String>();
 
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint connectorsExtensionPoint = registry.getExtensionPoint(PLUGIN_ID + ".connectors"); //$NON-NLS-1$
 		IExtension[] extensions = connectorsExtensionPoint.getExtensions();
 		for (IExtension extension : extensions) {
 			IConfigurationElement[] elements = extension.getConfigurationElements();
 			for (IConfigurationElement element : elements) {
 				connectorIds.add(element.getAttribute("id"));
 			}
 		}
 
 		return connectorIds;
 	}
 
 	/**
 	 * Get the first connector with status Enabled
 	 * 
 	 * @return
 	 */
 	public static NotificationsConnector getFirstEnabled() {
 		MultiStatus result = new MultiStatus(PLUGIN_ID, 0, "Notifications connectors failed to load.", null); //$NON-NLS-1$
 		Map<String, IConfigurationElement> configElements = resolveConfiguredElements();
 
 		// If no ids are provided, obtain the first connector found with state enabled
 		if (configElements.size() < 1) {
 			result.add(new Status(IStatus.WARNING, PLUGIN_ID, NLS.bind("No connectors found extending ''{0}''", //$NON-NLS-1$
 					NotificationsConnector.class.getCanonicalName())));
 			StatusHandler.log(result);
 			return null;
 		}
 
 		IConfigurationElement dConfigElement = null;
 		for (Iterator<IConfigurationElement> iterator = configElements.values().iterator(); iterator.hasNext();) {
 			dConfigElement = iterator.next();
 			NotificationsConnector connector = loadElement(dConfigElement, result);
 			if (connector != null && connector.isEnabled()) {
 				return connector;
 			}
 		}
 
 		if (!result.isOK()) {
 			StatusHandler.log(result);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the first connector enabled, checking the status in the order provided in the ids array
 	 * 
 	 * @param ids
 	 * @return
 	 */
 	public static NotificationsConnector getFirstEnabled(String[] ids) {
 		Assert.isNotNull(ids);
 
 		MultiStatus result = new MultiStatus(PLUGIN_ID, 0, "Notifications connectors failed to load.", null); //$NON-NLS-1$
 
 		Map<String, IConfigurationElement> configElements = resolveConfiguredElements();
 
		NotificationsConnector connector = null;
 		// scan for the id in the order provided by the array
 		IConfigurationElement dConfigElement = null;
 		for (int i = 0; i < ids.length; i++) {
 			dConfigElement = configElements.get(ids[i]);
 			if (dConfigElement != null) {
 				// make sure it's enabled
				connector = loadElement(dConfigElement, result);
 				if (connector != null && connector.isEnabled()) {
 					return connector;
 				}
 			}
 		}
 
		//Nothing found from the given list
		//resolve the first enabled
		connector = getFirstEnabled();
		if (connector != null) {
			return connector;
		}
		
 		if (!result.isOK()) {
 			StatusHandler.log(result);
 		}
 
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	private static Map<String, IConfigurationElement> resolveConfiguredElements() {
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint connectorsExtensionPoint = registry.getExtensionPoint(PLUGIN_ID + ".connectors"); //$NON-NLS-1$
 		IExtension[] extensions = connectorsExtensionPoint.getExtensions();
 
 		Map<String, IConfigurationElement> configElements = new HashMap<String, IConfigurationElement>();
 
 		// Build a map of available connector ids
 		for (IExtension extension : extensions) {
 			IConfigurationElement[] elements = extension.getConfigurationElements();
 			for (IConfigurationElement element : elements) {
 				configElements.put(element.getAttribute("id"), element);
 			}
 		}
 		return configElements;
 	}
 
 	private static NotificationsConnector loadElement(IConfigurationElement aElement, MultiStatus aStatus) {
 		try {
 			Object object = aElement.createExecutableExtension("core"); //$NON-NLS-1$
 			if (object instanceof NotificationsConnector) {
 				// success
 				return (NotificationsConnector) object;
 			} else {
 				aStatus.add(new Status(
 						IStatus.ERROR,
 						PLUGIN_ID,
 						NLS.bind(
 								"Notifications Connector core ''{0}'' does not extend expected class for extension contributed by {1}", //$NON-NLS-1$
 								object.getClass().getCanonicalName(), aElement.getContributor().getName())));
 			}
 		} catch (Throwable e) {
 			aStatus.add(new Status(
 					IStatus.ERROR,
 					PLUGIN_ID,
 					NLS.bind(
 							"Notifications Connector core failed to load for extension contributed by {0}", aElement.getContributor().getName()), e)); //$NON-NLS-1$
 		}
 
 		return null;
 	}
 
 	/**
 	 * Search for a connector with given id, return it if found
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static NotificationsConnector loadConnector(String id) {
 		Assert.isNotNull(id);
 		MultiStatus result = new MultiStatus(PLUGIN_ID, 0, "Notifications connectors failed to load.", null); //$NON-NLS-1$
 
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint connectorsExtensionPoint = registry.getExtensionPoint(PLUGIN_ID + ".connectors"); //$NON-NLS-1$
 		IExtension[] extensions = connectorsExtensionPoint.getExtensions();
 		for (IExtension extension : extensions) {
 			IConfigurationElement[] elements = extension.getConfigurationElements();
 			for (IConfigurationElement element : elements) {
 				if (id.equals(element.getAttribute("id"))) { //$NON-NLS-1$
 					try {
 						Object object = element.createExecutableExtension("core"); //$NON-NLS-1$
 						if (object instanceof NotificationsConnector) {
 							return (NotificationsConnector) object;
 						} else {
 							result.add(new Status(
 									IStatus.ERROR,
 									PLUGIN_ID,
 									NLS.bind(
 											"Notifications Connector core ''{0}'' does not extend expected class for extension contributed by {1}", //$NON-NLS-1$
 											object.getClass().getCanonicalName(), element.getContributor().getName())));
 						}
 					} catch (Throwable e) {
 						result.add(new Status(
 								IStatus.ERROR,
 								PLUGIN_ID,
 								NLS.bind(
 										"Notifications Connector core failed to load for extension contributed by {0}", element.getContributor().getName()), e)); //$NON-NLS-1$
 					}
 				}
 			}
 		}
 
 		if (!result.isOK()) {
 			StatusHandler.log(result);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Access factory method
 	 * 
 	 * @param aCustomId
 	 * @param aSubject
 	 * @param aBody
 	 * @param aLocation
 	 * @param aStartTimeMilli
 	 * @param aEndTimeMilli
 	 * @return
 	 * @throws CoreException
 	 */
 	public static IMeetingData createMeetingData(String aCustomId, String aSubject, String aBody, String aLocation,
 			long aStartTimeMilli, long aEndTimeMilli) throws CoreException {
 		return new MeetingData(aCustomId, aSubject, aBody, aLocation, aStartTimeMilli,
 			 aEndTimeMilli);
 	}
 
 }
