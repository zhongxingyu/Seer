 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *    Yatta Solutions - error handling (bug 374105)
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.ConnectException;
 import java.net.NoRouteToHostException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IBundleGroup;
 import org.eclipse.core.runtime.IBundleGroupProvider;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.epp.internal.mpc.core.MarketplaceClientCore;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.engine.IProfile;
 import org.eclipse.equinox.p2.engine.IProfileRegistry;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.ui.ProvisioningUI;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.PlatformUI;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 /**
  * @author Steffen Pingel
  * @author David Green
  * @author Carsten Reckord
  */
 public class MarketplaceClientUi {
 
 	private static final String DOT_FEATURE_DOT_GROUP = ".feature.group"; //$NON-NLS-1$
 
 	public static final String BUNDLE_ID = "org.eclipse.epp.mpc.ui"; //$NON-NLS-1$
 
 	public static ILog getLog() {
 		return Platform.getLog(Platform.getBundle(BUNDLE_ID));
 	}
 
 	public static void error(String message, Throwable exception) {
 		if (message == null) {
 			message = NLS.bind(Messages.MarketplaceClientUi_unexpectedException_reason, exception.getMessage());
 		}
 		getLog().log(new Status(IStatus.ERROR, BUNDLE_ID, IStatus.ERROR, message, exception));
 	}
 
 	public static void error(Throwable exception) {
 		error(null, exception);
 	}
 
 	public static IStatus computeStatus(Exception e, String message) {
 		Throwable cause = e;
 		if (e instanceof InvocationTargetException) {
 			cause = e.getCause();
 		}
 		IStatus statusCause = computeWellknownProblemStatus(e);
 		if (statusCause == null) {
 			if (cause instanceof CoreException) {
 				statusCause = ((CoreException) cause).getStatus();
 			} else {
 				statusCause = new Status(IStatus.ERROR, BUNDLE_ID, cause.getMessage(), cause);
 			}
 		}
 		if (statusCause.getMessage() != null) {
 			message = NLS.bind(Messages.MarketplaceClientUi_message_message2, message, statusCause.getMessage());
 		}
 		IStatus status = new MultiStatus(BUNDLE_ID, 0, new IStatus[] { statusCause }, message, cause);
 		return status;
 	}
 
 	public static IStatus computeWellknownProblemStatus(Throwable exception) {
 		IStatus status = null;
 		while (exception != null) {
 			// name resolution didn't work - possibly offline...
 			if (exception instanceof UnknownHostException) {
 				status = new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, NLS.bind(
 						Messages.MarketplaceClientUi_unknownHost, exception.getMessage()), exception);
 				break;
 			}
 			// could be a previously resolved name, but now unreachable because we're offline...
 			if (exception instanceof NoRouteToHostException) {
 				status = new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, NLS.bind(
 						Messages.MarketplaceClientUi_unknownHost, exception.getMessage()), exception);
 				break;
 			}
 			// some oddly configured networks throw timeouts instead of DNS or routing errors
 			if (exception instanceof ConnectException) {
 				status = new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, NLS.bind(
						Messages.MarketplaceClientUi_unknownHost, exception.getMessage()), exception);
 				break;
 			}
 			if (exception instanceof CoreException) {
 				IStatus exceptionStatus = ((CoreException) exception).getStatus();
 				if (MarketplaceClientCore.BUNDLE_ID.equals(exceptionStatus.getPlugin())
 						&& exceptionStatus.getCode() == 503) {
 					//received service unavailable error from P2 transport
 					status = new Status(IStatus.ERROR, MarketplaceClientUi.BUNDLE_ID, exceptionStatus.getMessage(),
 							exception);
 					break;
 				}
 			}
 			Throwable cause = exception.getCause();
 			if (cause != exception) {
 				exception = cause;
 			} else {
 				break;
 			}
 		}
 		return status;
 	}
 
 
 	public static BundleContext getBundleContext() {
 		return MarketplaceClientUiPlugin.getInstance().getBundle().getBundleContext();
 	}
 
 	public static Map<String, IInstallableUnit> computeInstalledIUsById(IProgressMonitor monitor) {
 		Map<String, IInstallableUnit> iUs = new HashMap<String, IInstallableUnit>();
 		BundleContext bundleContext = MarketplaceClientUi.getBundleContext();
 		ServiceReference serviceReference = bundleContext.getServiceReference(IProvisioningAgent.SERVICE_NAME);
 		if (serviceReference != null) {
 			IProvisioningAgent agent = (IProvisioningAgent) bundleContext.getService(serviceReference);
 			try {
 				IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
 				if (profileRegistry != null) {
 					IProfile profile = profileRegistry.getProfile(ProvisioningUI.getDefaultUI().getProfileId());
 					if (profile != null) {
 						IQueryResult<IInstallableUnit> result = profile.available(QueryUtil.createIUGroupQuery(),
 								monitor);
 						for (Iterator<IInstallableUnit> it = result.iterator(); it.hasNext();) {
 							IInstallableUnit unit = it.next();
 							iUs.put(unit.getId(), unit);
 						}
 					}
 				}
 			} finally {
 				bundleContext.ungetService(serviceReference);
 			}
 		}
 		return iUs;
 	}
 
 	public static Set<String> computeInstalledFeatures(IProgressMonitor monitor) {
 		Set<String> features = new HashSet<String>();
 
 		BundleContext bundleContext = MarketplaceClientUi.getBundleContext();
 		ServiceReference serviceReference = bundleContext.getServiceReference(IProvisioningAgent.SERVICE_NAME);
 		if (serviceReference != null) {
 			IProvisioningAgent agent = (IProvisioningAgent) bundleContext.getService(serviceReference);
 			try {
 				IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
 				if (profileRegistry != null) {
 					IProfile profile = profileRegistry.getProfile(ProvisioningUI.getDefaultUI().getProfileId());
 					if (profile != null) {
 						IQueryResult<IInstallableUnit> result = profile.available(QueryUtil.createIUGroupQuery(),
 								monitor);
 						for (Iterator<IInstallableUnit> it = result.iterator(); it.hasNext();) {
 							IInstallableUnit unit = it.next();
 							features.add(unit.getId());
 						}
 					}
 				}
 			} finally {
 				bundleContext.ungetService(serviceReference);
 			}
 		}
 		if (features.isEmpty()) {
 			// probably a self-hosted environment
 			IBundleGroupProvider[] bundleGroupProviders = Platform.getBundleGroupProviders();
 			for (IBundleGroupProvider provider : bundleGroupProviders) {
 				if (monitor.isCanceled()) {
 					break;
 				}
 				IBundleGroup[] bundleGroups = provider.getBundleGroups();
 				for (IBundleGroup group : bundleGroups) {
 					String identifier = group.getIdentifier();
 					if (!identifier.endsWith(DOT_FEATURE_DOT_GROUP)) {
 						identifier += DOT_FEATURE_DOT_GROUP;
 					}
 					features.add(identifier);
 				}
 			}
 		}
 		return features;
 	}
 
 	public static void setDefaultHelp(Control control) {
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, "org.eclipse.epp.mpc.help.ui.userGuide"); //$NON-NLS-1$
 	}
 
 }
