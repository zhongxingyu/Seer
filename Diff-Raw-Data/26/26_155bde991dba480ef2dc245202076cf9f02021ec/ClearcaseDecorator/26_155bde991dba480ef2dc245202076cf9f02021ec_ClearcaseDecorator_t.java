 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas taken from Eclipse code
  *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
  *     Tobias Sodergren - added quick refresh
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui;
 
 import java.util.*;
 import net.sourceforge.eclipseccase.*;
 import net.sourceforge.eclipseccase.ui.preferences.ClearcaseUIPreferences;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.team.ui.ISharedImages;
 import org.eclipse.team.ui.TeamImages;
 
 /**
  * The ClearCase label decorator.
  */
 public class ClearcaseDecorator extends LabelProvider implements ILightweightLabelDecorator, IResourceStateListener, IResourceChangeListener {
 
 	/** trace if */
 	private static final String DECORATOR = "ClearcaseDecorator"; //$NON-NLS-1$
 
 	/*
 	 * Define a cached image descriptor which only creates the image data once
 	 */
 	public static class CachedImageDescriptor extends ImageDescriptor {
 
 		ImageData data;
 
 		ImageDescriptor descriptor;
 
 		public CachedImageDescriptor(ImageDescriptor descriptor) {
 			if (null == descriptor)
 				throw new IllegalArgumentException("Image descriptor must not be null"); //$NON-NLS-1$
 			this.descriptor = descriptor;
 		}
 
 		public ImageData getImageData() {
 			if (data == null)
 				data = descriptor.getImageData();
 			return data;
 		}
 	}
 
 	/** used to exit the resource visitor for calculating the dirty state */
 	static final CoreException CORE_DIRTY_EXCEPTION = new CoreException(new Status(IStatus.OK, "dirty", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
 
 	/** used to exit the resource visitor for calculating the dirty state */
 	static final CoreException CORE_UNKNOWN_EXCEPTION = new CoreException(new Status(IStatus.OK, "unknown", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
 
 	/** the decorator id */
 	public static final String ID = "net.sourceforge.eclipseccase.ui.decorator"; //$NON-NLS-1$
 
 	private static ImageDescriptor IMG_DESC_CHECKED_IN;
 
 	private static ImageDescriptor IMG_DESC_CHECKED_OUT;
 
 	// Images cached for better performance
 	private static ImageDescriptor IMG_DESC_DIRTY;
 
 	private static ImageDescriptor IMG_DESC_EDITED;
 
 	private static ImageDescriptor IMG_DESC_HIJACKED;
 
 	private static ImageDescriptor IMG_DESC_LINK;
 
 	private static ImageDescriptor IMG_DESC_LINK_WARNING;
 
 	private static ImageDescriptor IMG_DESC_NEW_RESOURCE;
 
 	private static ImageDescriptor IMG_DESC_UNKNOWN_STATE;
 
 	/** internal state constant */
 	private static final int STATE_CLEAN = 0;
 
 	/** internal state constant */
 	private static final int STATE_DIRTY = 1;
 
 	/** internal state constant */
 	private static final int STATE_UNKNOWN = 2;
 
 	static {
 		IMG_DESC_DIRTY = new CachedImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_DIRTY_OVR));
 		IMG_DESC_CHECKED_IN = new CachedImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
 		IMG_DESC_CHECKED_OUT = new CachedImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR));
 		IMG_DESC_NEW_RESOURCE = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_QUESTIONABLE_OVR));
 		IMG_DESC_EDITED = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_EDITED_OVR));
 		IMG_DESC_UNKNOWN_STATE = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_UNKNOWN_OVR));
 		IMG_DESC_LINK = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_LINK_OVR));
 		IMG_DESC_LINK_WARNING = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_LINK_WARNING_OVR));
 		IMG_DESC_HIJACKED = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_HIJACKED_OVR));
 	}
 
 	/**
 	 * Detects the dirty state of the specified resource
 	 * 
 	 * @param resource
 	 * @return the dirty state of the specified resource
 	 */
 	private static int calculateDirtyState(IResource resource) {
 		/*
 		 * Since dirty == checkout/hijacked for files, redundant to show files
 		 * as dirty; we also need to filter out obsolete resources (removed due
 		 * to pending background jobs)
 		 */
 		if (resource.getType() == IResource.FILE || !resource.isAccessible() || resource.getLocation() == null)
 			return STATE_CLEAN;
 
 		// don't need to calculate if deep decoration is disabled
 		if (!ClearcaseUIPreferences.decorateFoldersDirty())
 			return STATE_CLEAN;
 
 		// cache some settings (visitor performance)
 		final boolean decorateNew = ClearcaseUIPreferences.decorateFoldersContainingViewPrivateElementsDirty() && (ClearcaseUIPreferences.decorateViewPrivateElements() || (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix() && ClearcaseUI.getTextPrefixNew().length() > 0));
 		final boolean decorateUnknown = ClearcaseUIPreferences.decorateUnknownElements() || (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix() && ClearcaseUI.getTextPrefixUnknown().length() > 0);
 		final boolean decorateHijacked = ClearcaseUIPreferences.decorateHijackedElements() || (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix() && ClearcaseUI.getTextPrefixHijacked().length() > 0);
 		try {
 			// visit all children to determine the state
 			resource.accept(new IResourceVisitor() {
 
 				/*
 				 * (non-Javadoc)
 				 * 
 				 * @see
 				 * org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse
 				 * .core.resources.IResource)
 				 */
 				public boolean visit(IResource childResource) throws CoreException {
 					// the provider of the child resource
 					ClearcaseProvider p = ClearcaseProvider.getClearcaseProvider(childResource);
 
 					// sanity check
 					if (p == null || !childResource.isAccessible())
 						return false;
 
 					// ignore some resources
 					if (p.isIgnored(childResource))
 						return false;
 
 					// test if unknown
 					if (decorateUnknown && p.isUnknownState(childResource)) {
 						if (ClearcaseUI.DEBUG_DECORATION)
 							ClearcaseUI.trace(DECORATOR, "  is dirty: child with unknown state"); //$NON-NLS-1$
 						throw CORE_UNKNOWN_EXCEPTION;
 					}
 
 					// test if new
 					if (decorateNew && !p.hasRemote(childResource)) {
 						if (ClearcaseUI.DEBUG_DECORATION)
 							ClearcaseUI.trace(DECORATOR, "  is dirty: child without remote"); //$NON-NLS-1$
 						throw CORE_DIRTY_EXCEPTION;
 					}
 
 					// test if hijacked
 					if (decorateHijacked && p.isHijacked(childResource)) {
 						if (ClearcaseUI.DEBUG_DECORATION)
 							ClearcaseUI.trace(DECORATOR, "  is dirty: hijacked child"); //$NON-NLS-1$
 						throw CORE_DIRTY_EXCEPTION;
 					}
 
 					// test if checked out
 					if (p.isCheckedOut(childResource)) {
 						if (ClearcaseUI.DEBUG_DECORATION)
 							ClearcaseUI.trace(DECORATOR, "  is dirty: child is checked out"); //$NON-NLS-1$
 						throw CORE_DIRTY_EXCEPTION;
 					}
 
 					// go into children
 					return true;
 				}
 			}, IResource.DEPTH_INFINITE, true);
 		} catch (CoreException e) {
 			// if our exception was caught, we know there's a dirty child
 			if (e == CORE_DIRTY_EXCEPTION)
 				return STATE_DIRTY;
 			else if (e == CORE_UNKNOWN_EXCEPTION)
 				return STATE_UNKNOWN;
 			else
 				// should not occure
 				handleException(e);
 		}
 		return STATE_CLEAN;
 	}
 
 	/**
 	 * Adds decoration for checked in state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateCheckedIn(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateCheckedIn"); //$NON-NLS-1$
 		decoration.addOverlay(IMG_DESC_CHECKED_IN);
 	}
 
 	/**
 	 * Adds decoration for checked out state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateCheckedOut(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateCheckedOut"); //$NON-NLS-1$
 		decoration.addOverlay(IMG_DESC_CHECKED_OUT);
 		if (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix())
 			decoration.addPrefix(ClearcaseUI.getTextPrefixDirty());
 	}
 
 	/**
 	 * Adds decoration for dirty state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateDirty(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateDirty"); //$NON-NLS-1$
 		decoration.addOverlay(IMG_DESC_DIRTY);
 		if (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix())
 			decoration.addPrefix(ClearcaseUI.getTextPrefixDirty());
 	}
 
 	/**
 	 * Adds decoration for edited state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateEdited(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateEdited"); //$NON-NLS-1$
 		if (ClearcaseUIPreferences.decorateEditedElements())
 			decoration.addOverlay(IMG_DESC_EDITED);
 		if (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix())
 			decoration.addPrefix(ClearcaseUI.getTextPrefixEdited());
 	}
 
 	/**
 	 * Adds decoration for hijaced state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateHijacked(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateHijacked"); //$NON-NLS-1$
 		if (ClearcaseUIPreferences.decorateHijackedElements())
 			decoration.addOverlay(IMG_DESC_HIJACKED);
 		if (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix())
 			decoration.addPrefix(ClearcaseUI.getTextPrefixHijacked());
 	}
 
 	/**
 	 * Adds decoration for links.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateLink(IDecoration decoration, String linkTarget, boolean isValidLinkTarget) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateLink"); //$NON-NLS-1$
 		if (isValidLinkTarget)
 			decoration.addOverlay(IMG_DESC_LINK);
 		else
 			decoration.addOverlay(IMG_DESC_LINK_WARNING);
 
 		decoration.addSuffix(" --> " + linkTarget); //$NON-NLS-1$
 	}
 
 	/**
 	 * Adds decoration for new state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateNew(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateNew"); //$NON-NLS-1$
 		if (ClearcaseUIPreferences.decorateViewPrivateElements())
 			decoration.addOverlay(IMG_DESC_NEW_RESOURCE);
 		if (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix())
 			decoration.addPrefix(ClearcaseUI.getTextPrefixNew());
 	}
 
 	/**
 	 * Adds decoration for unknown state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateUnknown(IDecoration decoration) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateUnknown"); //$NON-NLS-1$
 		if (ClearcaseUIPreferences.decorateUnknownElements())
 			decoration.addOverlay(IMG_DESC_UNKNOWN_STATE);
 		if (ClearcaseUIPreferences.decorateElementStatesWithTextPrefix())
 			decoration.addPrefix(ClearcaseUI.getTextPrefixUnknown());
 	}
 
 	/**
 	 * Adds decoration for the version.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateVersion(IDecoration decoration, String version) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateVersion"); //$NON-NLS-1$
 		if (ClearcaseUIPreferences.decorateElementsWithVersionInfo() && null != version)
 			decoration.addSuffix("  " + version); //$NON-NLS-1$
 	}
 
 	/**
 	 * Adds decoration for the view name.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateViewName(IDecoration decoration, String viewName) {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "  decorateViewName"); //$NON-NLS-1$
 		if (ClearcaseUIPreferences.decorateProjectsWithViewInfo() && null != viewName)
 			decoration.addSuffix(" [" + viewName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * Returns the resource for the given input object, or null if there is no
 	 * resource associated with it.
 	 * 
 	 * @param object
 	 *            the object to find the resource for
 	 * @return the resource for the given object, or null
 	 */
 	private static IResource getResource(Object object) {
 		if (object instanceof IResource)
 			return (IResource) object;
 		if (object instanceof IAdaptable)
 			return (IResource) ((IAdaptable) object).getAdapter(IResource.class);
 		return null;
 	}
 
 	/**
 	 * Handles the specified exception
 	 * 
 	 * @param e
 	 */
 	private static void handleException(CoreException e) {
 		ClearcasePlugin.log(IStatus.ERROR, Messages.getString("ClearcaseDecorator.error.exception") //$NON-NLS-1$
 				+ e.getMessage(), e);
 	}
 
 	/**
 	 * Creates a new instance.
 	 */
 	public ClearcaseDecorator() {
 		super();
 		StateCacheFactory.getInstance().addStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "activated"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
 	 *      org.eclipse.jface.viewers.IDecoration)
 	 */
 	public void decorate(Object element, IDecoration decoration) {
 
 		// don't do anything until the state cache is ready
 		if (!StateCacheFactory.getInstance().isInitialized())
 			return;
 
 		IResource resource = getResource(element);
 
 		// sanity check
 		if (resource == null || resource.getType() == IResource.ROOT || resource.getLocation() == null || !resource.isAccessible())
 			return;
 
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "decorating " + resource.getFullPath().toString()); //$NON-NLS-1$
 
 		// get our provider
 		ClearcaseProvider p = ClearcaseProvider.getClearcaseProvider(resource);
 		if (p == null) {
 			if (ClearcaseUI.DEBUG_DECORATION)
 				ClearcaseUI.trace(DECORATOR, "  no ClearcaseProvider"); //$NON-NLS-1$
 			return;
 		}
 
 		// test if ignored
 		if (p.isIgnored(resource)) {
 			if (ClearcaseUI.DEBUG_DECORATION)
 				ClearcaseUI.trace(DECORATOR, "  ignored"); //$NON-NLS-1$
 			return;
 		}
 
 		// test if uninitialized before all other checks
 		if (p.isUnknownState(resource)) {
 
 			// unknown state
 			decorateUnknown(decoration);
 
 			// no further decoration
 			return;
 		}
 
 		// Projects may be the view directory containing the VOBS, if so,
 		// they are not decoratable
 		if (p.isViewRoot(resource) || p.isVobRoot(resource)) {
 			if (ClearcaseUI.DEBUG_DECORATION)
 				ClearcaseUI.trace(DECORATOR, "  view or vob root"); //$NON-NLS-1$
 			return;
 		}
 
 		// decorate view tag for projects
 		if (resource.getType() == IResource.PROJECT)
 			decorateViewName(decoration, p.getViewName(resource));
 
 		/*
 		 * test the different states
 		 */
 		if (resource.getType() != IResource.PROJECT && !p.hasRemote(resource)) {
 			// decorate new elements not added to ClearCase
 			decorateNew(decoration);
 
 			// no further decoration
 			return;
 		} else if (p.isCheckedOut(resource)) {
 			// check out
 			decorateCheckedOut(decoration);
 
 			// no further decoration
 			return;
 		} else if (p.isHijacked(resource)) {
 			// hijacked
 			decorateHijacked(decoration);
 
 			// no further decoration
 			return;
 		} else if (p.isSymbolicLink(resource)) {
 			// symbolic link
 			decorateLink(decoration, p.getSymbolicLinkTarget(resource), p.isSymbolicLinkTargetValid(resource));
 
 			// no further decoration
 			return;
 		} else {
 			// calculate the state
 			int dirty = calculateDirtyState(resource);
 
 			switch (dirty) {
 			case STATE_CLEAN:
 				if (p.hasRemote(resource)) {
 					if (p.isEdited(resource))
 						// the resource is edited by someone else
 						decorateEdited(decoration);
 					else
 						// at this point, we assume everything is ok
 						decorateCheckedIn(decoration);
 					// add version info only at this point
 					decorateVersion(decoration, p.getVersion(resource));
 				}
 				return;
 
 			case STATE_DIRTY:
 				// dirty
 				decorateDirty(decoration);
 				return;
 
 			case STATE_UNKNOWN:
 				// unknown
 				decorateUnknown(decoration);
 				return;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
 	 */
 	public void dispose() {
 		if (ClearcaseUI.DEBUG_DECORATION)
 			ClearcaseUI.trace(DECORATOR, "disposed"); //$NON-NLS-1$
 		StateCacheFactory.getInstance().removeStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.LabelProvider#fireLabelProviderChanged(org.
 	 * eclipse.jface.viewers.LabelProviderChangedEvent)
 	 */
 	protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
 		// delegate to UI thread
 		Display display = ClearcaseUI.getDisplay();
 		if (null != display && !display.isDisposed())
 			display.asyncExec(new Runnable() {
 
 				public void run() {
 					superFireLabelProviderChanged(event);
 				}
 			});
 	}
 
 	/**
 	 * Updates all decorators on any resource.
 	 */
 	public void refresh() {
 		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
 	}
 
 	/**
 	 * Update the decorators for every resource in project.
	 * Used when Associating/Deassociate project.
 	 * 
 	 * @param project
 	 */
 	public void refresh(IProject project) {
 		final List resources = new ArrayList();
 		try {
 			project.accept(new IResourceVisitor() {
 				public boolean visit(IResource resource) {
 					resources.add(resource);
 					return true;
 				}
 			});
 			fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources.toArray()));
 		} catch (CoreException e) {
 			handleException(e);
 		}
 	}
 
 	public void refreshUsingStatusCollector(IProject[] projects, IProgressMonitor monitor) {
 		final ClearcaseElementStatusCollector statusCollector = StateCacheFactory.getInstance().getStatusCollector(projects);
 		statusCollector.collectRefreshStatus(monitor);
 		final List resources = new ArrayList();
 		for (int i = 0; i < projects.length; i++) {
 			IProject project = projects[i];
 			try {
 				project.accept(new IResourceVisitor() {
 					public boolean visit(IResource resource) {
 						StateCacheFactory.getInstance().getUsingStatusCollector(resource, statusCollector);
 						resources.add(resource);
 						return true;
 					}
 				});
 				fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources.toArray()));
 			} catch (CoreException e) {
 				handleException(e);
 			}
 		}
 	}
 
 	/**
 	 * Updates the decorators for the specified resources.
 	 * 
 	 * @param resources
 	 */
 	public void refresh(IResource[] resources) {
 		if (null == resources || resources.length == 0)
 			return;
		//Make sure you take into account changes to workspace
		//not performed outside Eclipse.
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			synchronizeResource(resource,IResource.DEPTH_ZERO);
		}
		
 		//FIXME: I removed this code since it seems to make refresh veeeery slow.....
 		// I will test it and see if it fixes problem with refresh
 		// sometimes is better to fire a global refresh
 //		if (resources.length > 200) {
 //			refresh();
 //			return;
 //		}
 
 		// if deep decoration is disabled, update only the specified resources
 		if (!ClearcaseUIPreferences.decorateFoldersDirty())
 			fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources));
 		else {
 			// deep decoration is enabled: update parents also
 			final HashSet changedResources = new HashSet(resources.length + 20);
 			for (int i = 0; i < resources.length; i++) {
 				IResource resource = resources[i];
 				changedResources.add(resource);
 
 				// collect parents
 				IResource parent = resource.getParent();
 				while (null != parent && changedResources.add(parent))
 					parent = parent.getParent();
 
 				// // collect children
 				// if (resource.isAccessible())
 				// {
 				//
 				// try
 				// {
 				// // refresh children
 				// resource.accept(new IResourceVisitor()
 				// {
 				// public boolean visit(IResource child)
 				// throws CoreException
 				// {
 				// return changedresources.add(child);
 				// }
 				// });
 				// }
 				// catch (CoreException ex)
 				// {
 				// ClearcasePlugin.log(IStatus.ERROR,
 				// "Could not access resource: "
 				// + resource.getFullPath().toString(), ex);
 				// }
 				//
 				// }
 			}
 
 			// fire the change
 			fireLabelProviderChanged(new LabelProviderChangedEvent(this, changedResources.toArray()));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.IResourceStateListener#stateChanged(net.
 	 * sourceforge.eclipseccase.StateCache)
 	 */
 	public void resourceStateChanged(IResource[] resources) {
 		refresh(resources);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
 	 * .eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 
 		// get the delta
 		IResourceDelta rootDelta = event.getDelta();
 
 		// process delta
 		if (null != rootDelta) {
 
 			final Set resources = new HashSet();
 
 			try {
 				// recursive
 				rootDelta.accept(new IResourceDeltaVisitor() {
 
 					public boolean visit(IResourceDelta delta) throws CoreException {
 						switch (delta.getKind()) {
 
 						case IResourceDelta.ADDED:
 						case IResourceDelta.REMOVED:
 							// if resource was added or removed
 							if (ClearcaseUIPreferences.decorateFoldersDirty())
 								// refresh parent if deep decoration is enabled
 								resources.add(delta.getResource().getParent());
 							return false;
 
 						}
 						return true;
 					}
 				});
 
 			} catch (CoreException e) {
 				// ignore
 			}
 
 			if (!resources.isEmpty())
 				refresh((IResource[]) resources.toArray(new IResource[resources.size()]));
 		}
 	}
 
 	/**
 	 * Delegates the event to the super class for firing.
 	 * 
 	 * @param event
 	 */
 	final void superFireLabelProviderChanged(LabelProviderChangedEvent event) {
 		super.fireLabelProviderChanged(event);
 	}
	//TODO:Testing to see if this will help take into
	//account changes in workspace not within eclipse.
	/**
	 * 
	 * @param resource
	 * @param depth
	 */
	private void synchronizeResource(IResource resource, int depth) {
		if (!resource.isSynchronized(depth)) {
			try {
				resource.refreshLocal(depth, null);
			} catch (CoreException e) {
				throw new RuntimeException("Problem refreshing resource " + resource.getLocation(), e);
			}
		}
	}
 
 }
