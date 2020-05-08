 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.j2ee.internal.webservice;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jst.j2ee.internal.webservice.plugin.WebServicePlugin;
 
 public class NewProjectsListener implements IResourceChangeListener, IResourceDeltaVisitor  {
 
 	
 	private WebServiceViewerSynchronization synchronization;
 	private boolean listening = false;
 	private boolean synchronizing = false;
 
 	public NewProjectsListener(WebServiceViewerSynchronization sync) {
 		synchronization = sync;
 		if(synchronization.webServiceProjectsExist(new NullProgressMonitor())) {
 			synchronizing = true;
			synchronization.start();
 		}
 		startListening();
 	} 
 	
 	public void dispose() {
 		if(listening)
 			stopListening();
 		if(synchronizing)
 			synchronization.stop();
 	}
 	
 	public void resourceChanged(IResourceChangeEvent event) {
 		try {
 			event.getDelta().accept(this);
 		} catch (CoreException e) {
 			WebServicePlugin.logError(0, e.getMessage(), e);
 		} 
 	} 
 
 	public boolean visit(IResourceDelta delta) throws CoreException { 
 		
 		IResource resource = delta.getResource();
 		switch (resource.getType()) {
 			case IResource.ROOT :
 				return true;
 			case IResource.PROJECT: 
 				if(delta.getKind() == IResourceDelta.ADDED) {
 					if(WebServiceViewerSynchronization.isInteresting((IProject)resource)) {
 						stopListening();
 						synchronizing = true;
 						synchronization.start();
 					}
 				}
 
 			default :
 				break;
 		}
 		
 		return false;
 	}
 
 	private void startListening() {
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 		listening = true;
 	}
 
 	private void stopListening() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		listening = false;
 	}
 }
