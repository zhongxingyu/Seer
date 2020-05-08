 /*******************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Feb 17, 2005
  */
 package org.eclipse.jst.j2ee.navigator.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.SWTException;
 
 public class EMFRootObjectProvider implements  IResourceChangeListener, IResourceDeltaVisitor{
 	private final HashMap emfModelCache = new HashMap();
 	private final List listeners = new ArrayList();
 	
 	public interface IRefreshHandlerListener {
 		void onRefresh(Object element);
 	}
 	
 	/**
 	 *  
 	 */
 	public EMFRootObjectProvider() {
 		super();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 	
	public Object[] getModels(IProject project){
 		try {
			EMFModelManager modelManager = null;
 			synchronized (emfModelCache) {
				modelManager = (EMFModelManager) emfModelCache.get(project);
				if (modelManager == null) {
					modelManager= EMFModelManagerFactory.createEMFModelManager(project,this) ;
					emfModelCache.put(project,modelManager);
				}
 			}
 		return modelManager.getModels();
 		} catch (Exception ex) {
 			return null;
 		}
 	}
 	
	public boolean hasLoadedModels(IProject project) {
		synchronized (emfModelCache) {
			return emfModelCache.get(project) != null;
		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		final IResourceDelta delta = event.getDelta();
 
 		if (delta != null) {
 			try {
 				delta.accept(EMFRootObjectProvider.this);
 			} catch (CoreException e) {
 				Logger.getLogger().logError(e);
 			} catch (SWTException swte) {
 				Logger.getLogger().logError(swte);
 			} catch (SWTError swte) {
 				Logger.getLogger().logError(swte);
 			}
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		IResource resource = delta.getResource();
 		if (resource != null) {
 			switch (resource.getType()) {
 				case IResource.ROOT :
 					return true;
 				case IResource.PROJECT :
 					boolean projectOpenStateChanged = ((delta.getFlags() & IResourceDelta.OPEN) != 0);
 					if (delta.getKind() == IResourceDelta.REMOVED || projectOpenStateChanged) {
 						IProject project = (IProject) resource;
 						dispose(project);
 					}
 					return false;
 			}
 		}
 		return false;
 	}
 	
 	private void dispose(IProject project) {
 		if (project == null) return;
 		EMFModelManager modelManager = (EMFModelManager) emfModelCache.remove(project);
 		if (modelManager != null) {
 			modelManager.dispose();
 			modelManager = null;
 		}
 	}
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		EMFModelManager modelManager = null;
 		Object[] keys = emfModelCache.keySet().toArray();
 		for (int i = 0; i < keys.length; i++) {
 			modelManager = (EMFModelManager) emfModelCache.remove(keys[i]);
 			if (modelManager != null) {
 				modelManager.dispose();
 			}
 		}
 	}
 	
 
 	/**
 	 * @param aProject
 	 */
 	public void notifyListeners(IProject aProject) {
 		for (int x = 0; x < listeners.size(); ++x) {
 			IRefreshHandlerListener refreshHandler = (IRefreshHandlerListener) listeners.get(x);
 			refreshHandler.onRefresh(aProject);
 		}
 
 	}
 	
 	public void addRefreshHandlerListener(IRefreshHandlerListener aListener) {
 		synchronized (getListeners()) {
 			if (aListener != null && !getListeners().contains(aListener))
 				getListeners().add(aListener);
 		}
 	}
 
 	public void removeRefreshHandlerListener(IRefreshHandlerListener aListener) {
 		synchronized (listeners) {
 			listeners.remove(aListener);
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (4/11/2001 4:42:58 PM)
 	 * 
 	 * @return java.util.List
 	 */
 	protected java.util.List getListeners() {
 		return listeners;
 	}
 	
 
 }
