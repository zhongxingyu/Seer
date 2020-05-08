 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.web.pagereloader.internal.listener;
 
 import java.util.concurrent.ArrayBlockingQueue;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.util.PublishAdapter;
 import org.jboss.tools.web.pagereloader.internal.remote.websocketx.WebSocketServer;
 import org.jboss.tools.web.pagereloader.internal.util.Logger;
 import org.jboss.tools.web.pagereloader.internal.util.WtpUtils;
 
 /**
  * @author xcoulon
  * 
  */
 public class WebResourceChangeListener implements IResourceChangeListener {
 
 	private final IServer server;
 	private final IModule module;
 	private final IFolder webappFolder;
 
 	private final ArrayBlockingQueue<String> pendingChanges = new ArrayBlockingQueue<String>(1000);
 
 	public static void enableLiveReload(final IServer server) {
 		try {
 			for (IModule module : server.getModules()) {
 				final IProject project = module.getProject();
 				final IFolder webappFolder = WtpUtils.getWebappFolder(project);
 				WebResourceChangeListener listener = new WebResourceChangeListener(server, module, webappFolder);
 				ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
 			}
 
 
 		} catch (Exception e) {
 			Logger.error("Failed to register observer for " + server, e);
 		}
 	}
 
 	public WebResourceChangeListener(IServer server, IModule module, IFolder webappFolder) {
 		this.server = server;
 		this.module = module;
 		this.webappFolder = webappFolder;
 		server.addPublishListener(new PageReloadPublishAdapter());
 	}
 
 	/**
 	 * Receives a notification event each time a resource changed. If the
 	 * resource is a subresource of the observed location, then the event is
 	 * propagated.
 	 */
 	@Override
 	public void resourceChanged(IResourceChangeEvent event) {
 		final IResource resource = findChangedResource(event.getDelta());
 		if (webappFolder.getFullPath().isPrefixOf(resource.getFullPath())) {
 			try {
 				final IPath changedPath = resource.getFullPath().makeRelativeTo(webappFolder.getFullPath());
				String path = "http://" + server.getHost() + ":" + server.getAttribute("org.jboss.ide.eclipse.as.core.server.webPort", "8080") + "/" + module.getName() + "/" + changedPath.toString();
 				System.out.println("Putting '" + path + "' on wait queue until server publish is done.");
 				pendingChanges.offer(path);
 			} catch (Exception e) {
 				Logger.error("Failed to send Page.Reload command over websocket", e);
 			}
 
 		}
 	}
 
 	private IResource findChangedResource(IResourceDelta delta) {
 		if (delta.getAffectedChildren().length > 0) {
 			return findChangedResource(delta.getAffectedChildren()[0]);
 		}
 		return delta.getResource();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((webappFolder.getFullPath() == null) ? 0 : webappFolder.getFullPath().toPortableString().hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		WebResourceChangeListener other = (WebResourceChangeListener) obj;
 		if (webappFolder.getFullPath() == null) {
 			if (other.webappFolder.getFullPath() != null) {
 				return false;
 			}
 		} else if (!webappFolder.getFullPath().toPortableString()
 				.equals(other.webappFolder.getFullPath().toPortableString())) {
 			return false;
 		}
 		return true;
 	}
 
 	class PageReloadPublishAdapter extends PublishAdapter {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.wst.server.core.util.PublishAdapter#publishFinished(org
 		 * .eclipse.wst.server.core.IServer, org.eclipse.core.runtime.IStatus)
 		 */
 		@Override
 		public void publishFinished(IServer server, IStatus status) {
 			if (!status.isOK()) {
 				return;
 			}
 			try {
 				while (!pendingChanges.isEmpty()) {
 					String changedPath = pendingChanges.take();
 					WebSocketServer.getInstance().notifyResourceChange(changedPath);
 				}
 			} catch (Exception e) {
 				Logger.error("Failed to send notifications for pending changes", e);
 			}
 		}
 
 	}
 
 }
