 /***************************************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  *   IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.util;
 
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ISaveContext;
 import org.eclipse.core.resources.ISaveParticipant;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jst.jsf.facesconfig.FacesConfigPlugin;
 import org.eclipse.jst.jsf.facesconfig.internal.Logger;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 
 /**
  * This class is responsible for the following:
  * <ol>
  * <li>Ensure that the Struts nature is added to any project to which a Struts
  * config. file is added.</li>
  * <li>Ensure that the Struts nature is added to any project to which a diagram
  * file is added.</li>
  * <li>Restart project(s) if/as apporopriate when a Struts config. file is
  * modified.
  * </ol>
  * It arguably should be multiple classes, but the things it does are closely
  * related and splitting it up would result in duplicate work as well as
  * multiple delta traversals.
  * 
  * This class is not intended for external use. 
  * Should NOT be referenced or extended externally.
  */
 final class FacesResourceChangeListener implements IResourceChangeListener,
 		IResourceDeltaVisitor, ISaveParticipant, IResourceVisitor {
 
 	/** The singleton instance. */
 	private static FacesResourceChangeListener listener;
 	private static boolean restartInProgress = false;
 	private LinkedList facesConfigChangeListeners = new LinkedList();
 	
 	private static IPreferenceStore preferenceStore = null;
 
 	private static final QualifiedName EDITOR_KEY = new QualifiedName(
 			"org.eclipse.ui.internal.registry.ResourceEditorRegistry", "EditorProperty");//$NON-NLS-2$//$NON-NLS-1$
 
 	/** Start up the singleton instance. */
 	public static void startup() {
 
 		// Bail if we're already started.
 		if (listener != null)
 			return;
 
 		// Create the singleton instance.
 		listener = new FacesResourceChangeListener();
 
 		// Register as resource change listener.
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				listener,
 				IResourceChangeEvent.PRE_BUILD + IResourceChangeEvent.POST_BUILD);
 	}
 
 	/** Shutdown the singleton instance. */
 	public static void shutdown() {
 
 		// Bail if we're not started.
 		if (listener == null)
 			return;
 
 		// Deregister as save participant.
		ResourcesPlugin.getWorkspace().removeSaveParticipant(FacesConfigPlugin.getPlugin());
 
 		// Deregister as resource change listener.
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
 
 		// Dereference the singleton instance.
 		listener = null;
 	}
 
 	/**
 	 * Only this class can create instances.
 	 */
 	private FacesResourceChangeListener() {
         // no local instantiation
 	}
 
 	/**
 	 * Process a resource change event. This should be invoked only from the
 	 * workbench.
 	 * 
 	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 
 		IResourceDelta delta = event.getDelta();
 		if (delta != null) {
 			FacesConfigChangeEvent facesConfigChangeEvent = new FacesConfigChangeEvent();
 			fireFacesConfigChangeEvent(facesConfigChangeEvent);
 
 			try {
 				delta.accept(this);
 			} catch (CoreException ignored) {
 				Logger.log(this, ignored);
 			}
 		}
 
 		// Restart projects, if necessary.
 		if ((delta != null) && (event.getType() == IResourceChangeEvent.POST_BUILD)) {
 			FacesConfigRestartServerResourceDeltaVisitor visitor = new FacesConfigRestartServerResourceDeltaVisitor();
 			try {
 				delta.accept(visitor);
 			} catch (CoreException ignored) {
 				Logger.log(this, ignored);
 			}
 			//restartComponents(visitor.getComponents());
 		}
 	}
 
 	/**
 	 * Visit a resource delta. This should be invoked only from the
 	 * IResourceDelta.accept() method invoked above.
 	 * 
 	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
 	 */
 	public boolean visit(IResourceDelta delta) throws CoreException {
 
 		// Check for and handle it if it's a Struts config. file.
 		checkForFacesConfigFile(delta);
 
 		// Done.
 		return true;
 	}
 
 	private void checkForFacesConfigFile(IResourceDelta delta) {
 		boolean isAdded = delta.getKind() == IResourceDelta.ADDED;
 		if (isAdded
 				|| ((delta.getKind() == IResourceDelta.CHANGED) && ((delta.getFlags() & (IResourceDelta.CONTENT
 						| IResourceDelta.TYPE | IResourceDelta.SYNC | IResourceDelta.REPLACED)) != 0))) {
 			checkForFacesConfigFile(delta.getResource(), !isAdded);
 		}
 	}
 
 	private void checkForFacesConfigFile(IResource resource, boolean ignoreNonFacesProjects) {
 		if (resource.getType() == IResource.FILE) {
 
 			// See if the file is a Struts config. file.
 			// If the file was just added, we check the file regardless of
 			// whether or not it is in a Struts project.
 			// Otherwise, a file in a non-Struts project is considered to not be
 			// a Struts config. file.
 			IFile file = (IFile) resource;
 			if (FacesConfigUtil.isFacesConfigFile(file, ignoreNonFacesProjects)) {
 
 				// Ensure that the project has the Struts nature.
 				// TODO:
 				// StrutsNatureRuntime.ensureProjectHasStrutsNature(file.getProject());
 				IVirtualComponent component = ComponentUtilities.findComponent(file);
 				if (component != null) {
 					restartServerIfNecessary(component);
 					// Try to register the SCFE as the default editor.
 					setRegistration(file);
 				}
 			} else {
 				// Try to unregister the SCFE as the default editor.
 				unsetRegistration(file);
 			}
 		}
 	}
 
 	/**
 	 * Look to see if the persisted resource level property keyed by EDITOR_KEY
 	 * has ben set yet. If not then set it to the SCFE.
 	 * 
 	 * @param file
 	 *            The FCF
 	 */
 	private void setRegistration(IFile file) {
 		String editorID = null;
 		try {
 			editorID = file.getPersistentProperty(EDITOR_KEY);
 		} catch (CoreException e) {
             // suppress core exception
 		}
 		if (editorID == null) {
 			try {
 				file.setPersistentProperty(EDITOR_KEY, FacesConfigPlugin.FACES_CONFIG_EDITOR_ID);
 			} catch (CoreException e) {
 				Logger.log(file, "Failed to set the vcurrent editor to SCFE", e); //$NON-NLS-1$
 			}
 		}
 	}
 
 	private void unsetRegistration(IFile file) {
 		// If the default editor for this file is not the Struts config. editor,
 		// then we're done.
 		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
 		IEditorDescriptor userEditor = registry.getDefaultEditor(file.getFullPath().toString());
 		if ((userEditor == null)
 				|| !FacesConfigPlugin.FACES_CONFIG_EDITOR_ID.equals(userEditor.getId())) {
 			traceFiner(file, "Not unsetting: Default already not Faces config. editor"); //$NON-NLS-1$
 			return;
 		}
 
 		// Make the Struts config. editor the default.
 		traceFiner(file, "Unsetting."); //$NON-NLS-1$
 		IEditorDescriptor[] editors = registry.getEditors(file.getFullPath().toString());
 		if (editors.length > 1) {
 			registry.setDefaultEditor(file.getFullPath().toString(), editors[1].getId());
 		}
 	}
 
 	//private boolean isRestarting = false;
 	//private Collection restartableComponents = new HashSet();
 
 /*	private void restartComponents(Collection components) {
 		restartableComponents.addAll(components);
 		if (!isRestarting) {
 			isRestarting = true;
 			try {
 				while (!restartableComponents.isEmpty()) {
 					IVirtualComponent component = (IVirtualComponent) restartableComponents.iterator().next();
 					try {
 						ServerRestartUtil.restartComponent(component, true);
 					} finally {
 						restartableComponents.remove(component);
 					}
 				}
 			} finally {
 				isRestarting = false;
 			}
 
 		}
 	}
 
 */	private void traceFiner(IFile file, String message) {
 		String fileName = file.getProjectRelativePath().toString();
 		Logger.trace("FacesconfigPlugin", this, fileName + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/** @see ISaveParticipant#doneSaving(ISaveContext) */
 	public void doneSaving(ISaveContext context) {
         // nothing to do
 	}
 
 	/** @see ISaveParticipant#prepareToSave(ISaveContext) */
 	public void prepareToSave(ISaveContext context) throws CoreException {
         // nothing to do
 	}
 
 	/** @see ISaveParticipant#rollback(ISaveContext) */
 	public void rollback(ISaveContext context) {
 	    // nothing to do
 	}
 
 	/** @see ISaveParticipant#saving(ISaveContext) */
 	public void saving(ISaveContext context) throws CoreException {
 		context.needDelta();
 	}
 
 	/**
 	 * Visit a resource. This should be invoked only from the
 	 * IResource.accept(IResourceVisitor) invocation, above.
 	 * 
 	 * @see IResourceVisitor#visit(IResource)
 	 */
 	public boolean visit(IResource resource) {
 
 		// Check for and handle a Struts config. file.
 		checkForFacesConfigFile(resource, true);
 
 		// Continue.
 		return true;
 	}
 
 	private void restartServerIfNecessary(IVirtualComponent component) {
 		if(!restartInProgress) {
 			// check against preference about whether to automatically restart
 			boolean restart = false;
 			if (FacesResourceChangeListener.preferenceStore != null) {
 				restart = FacesResourceChangeListener.preferenceStore.getBoolean(IFacesconfigPreferences.PREFSKEY_SERVER_RESTART);
 			}
 			if(restart) {
 				restartInProgress = true;
 				// we'll ask that just the containing EAR is restarted, but it may cycle the whole server if running on Portal
 
 				//ServerRestartUtil.restartComponent(component, true);
 				restartInProgress = false;
 			}
 		}
 	}
 
 	/**
 	 * @return Returns the listener.
 	 */
 	public static FacesResourceChangeListener getFacesResourceChangeListener() {
 		if (listener == null) {
 			listener = new FacesResourceChangeListener();
 			// Register as resource change listener.
 			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_BUILD);
 		}
 		return listener;
 	}
 	
 	/**
 	 * Adds a change listener to the list of listeners that will be notified
 	 * when a change is fired.
 	 * 
 	 * @param facesConfigChangeListener
 	 */
 	public void addFacesConfigChangeListener(IFacesConfigChangeListener facesConfigChangeListener) {
 		facesConfigChangeListeners.add(facesConfigChangeListener);
 	}
 	/**
 	 * Removes the listener from the list.
 	 * 
 	 * @param facesConfigChangeListener
 	 */
 	public void removeFacesConfigChangeListener(IFacesConfigChangeListener facesConfigChangeListener) {
 		facesConfigChangeListeners.remove(facesConfigChangeListener);
 	}
 	
 	private void fireFacesConfigChangeEvent(IFacesConfigChangeEvent event) {
 		LinkedList localCopy;
 		synchronized( this ) {
 			localCopy = (LinkedList)facesConfigChangeListeners.clone();
 		}
 		for ( ListIterator iter = localCopy.listIterator(); iter.hasNext(); ) {
 			IFacesConfigChangeListener facesConfigChangeListener = (IFacesConfigChangeListener)iter.next();
 			facesConfigChangeListener.resourceChanged(event);
 		}
 	}
 
 	/** 
 	 * Set the internally used preference store to preferenceStore
 	 * 
 	 * @param preferenceStore
 	 */
 	public static void setPreferenceStore(IPreferenceStore preferenceStore) {
 		FacesResourceChangeListener.preferenceStore = preferenceStore;
 	}
 }
