 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.ui.internal.launcher;
 
 import java.lang.reflect.Method;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.ClientDelegate;
 import org.jboss.tools.ws.jaxrs.core.metamodel.IJaxrsEndpoint;
 import org.jboss.tools.ws.jaxrs.ui.JBossJaxrsUIPlugin;
 import org.jboss.tools.ws.jaxrs.ui.internal.utils.Logger;
 
 /**
  * Custom client for the "Run as>Run as Server" launcher, that redirects the call to the WSTester instead of the Web
  * Browser.
  * 
  * @author Xavier Coulon
  * 
  */
 public class WSTesterClientDelegate extends ClientDelegate {
 
 	/**
 	 * Returns true if the given launchable is a JaxrsEndpointModuleArtifact, false otherwise
 	 */
 	@Override
 	public boolean supports(IServer server, Object launchable, String launchMode) {
 		if (launchable instanceof JaxrsEndpointModuleArtifact) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Opens the WS Tester with the appropriate params.
 	 */
 	public IStatus launch(IServer server, Object launchable, String launchMode, ILaunch launch) {
 		if (launchable instanceof JaxrsEndpointModuleArtifact) {
 			JaxrsEndpointModuleArtifact artifact = (JaxrsEndpointModuleArtifact) launchable;
 			final IJaxrsEndpoint endpoint = artifact.getEndpoint();
 			final IModule module = artifact.getModule();
 			String endpointUri = computeEndpointURI(module, endpoint);
 			// Now we call the WS Tester through Reflection so there's no direct plug-in dependency
 			IWorkbench wb = PlatformUI.getWorkbench();
 			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
 			IWorkbenchPage page = win.getActivePage();
 			String id = "org.jboss.tools.ws.ui.tester.views.TestWSView"; //$NON-NLS-1$
 			try {
 				IViewPart part = page.showView(id);
 				if (part != null
 						&& part.getClass().getName().equalsIgnoreCase("org.jboss.tools.ws.ui.views.JAXRSWSTestView2")) { //$NON-NLS-1$
 					Class<?> c = part.getClass();
 					Class<?> parmtypes[] = new Class[2];
 					parmtypes[0] = String.class;
 					parmtypes[1] = String.class;
 					Method setURL = c.getDeclaredMethod("setJAXRS", parmtypes); //$NON-NLS-1$
 					if (setURL != null) {
 						Object arglist[] = new Object[2];
 						arglist[0] = endpointUri;
 						arglist[1] = endpoint.getHttpMethod().getHttpVerb();
 						setURL.invoke(part, arglist);
 					}
 				}
 			} catch (Exception e) {
 				Logger.error("Failed to open WS Tester", e);
 				return new Status(IStatus.ERROR, JBossJaxrsUIPlugin.PLUGIN_ID, "Failed to launch WS Tester", e);
 			}
 		}
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Creates the endpoint URI from the endpoint, by looking at the servers on which the module is deployed.
 	 * 
 	 * @param element
 	 * @return
 	 */
 	private String computeEndpointURI(final IModule module, final IJaxrsEndpoint endpoint) {
		String uriPathTemplate = module.getName() + "/" + endpoint.getUriPathTemplate();
 		// check to see if this project has been deployed...
 		IServer[] servers = ServerUtil.getServersByModule(module, null);
 		if (servers == null || servers.length == 0) {
			return "http://[domain]:[port]/" + uriPathTemplate; //$NON-NLS-1$
 		}
 		// if it's been deployed, we can grab the domain and web port
 		String domain = servers[0].getHost();
 		String webport = servers[0].getAttribute("org.jboss.ide.eclipse.as.core.server.webPort", "8080");//$NON-NLS-1$ //$NON-NLS-2$
		return "http://" + domain + ':' + webport + "/" + uriPathTemplate; //$NON-NLS-1$
 
 	}
 }
