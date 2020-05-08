 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jee.model.internal;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipse.jem.util.emf.workbench.FlexibleProjectResourceSet;
 import org.eclipse.jem.util.emf.workbench.ProjectResourceSet;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.j2ee.model.IModelProviderListener;
 import org.eclipse.jst.j2ee.model.ModelProviderEvent;
 import org.eclipse.jst.javaee.core.internal.util.JavaeeResourceImpl;
 import org.eclipse.jst.jee.JEEPlugin;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.impl.PlatformURLModuleConnection;
 import org.eclipse.wst.common.componentcore.internal.impl.WTPResourceFactoryRegistry;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateInputProvider;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidator;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidatorImpl;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidatorPresenter;
 
 public class JEE5ModelProvider implements IModelProvider, ResourceStateInputProvider, ResourceStateValidator, IModelProviderListener{
 
 	protected XMLResourceImpl writableResource;
 	protected IProject proj;
 	protected IPath defaultResourcePath;
 	protected ResourceStateValidator stateValidator;
 	protected ResourceAdapter resourceAdapter = new ResourceAdapter();
 	protected final ListenerList listeners = new ListenerList();
 	//private static boolean resourceChangeListenerEnabled = false;
 
 	private List modelResources = new ArrayList();
 	protected class ResourceAdapter extends AdapterImpl {
 		@Override
 		public void notifyChanged(Notification notification) {
 			if (notification.getEventType() == Notification.SET
 					&& notification.getFeatureID(null) == Resource.RESOURCE__IS_LOADED) {
 				resourceIsLoadedChanged((Resource) notification.getNotifier(), notification.getOldBooleanValue(), notification.getNewBooleanValue());
 			} else if (notification.getFeatureID(null) == Resource.RESOURCE__IS_MODIFIED)
 				resourceChanged((Resource) notification.getNotifier());
 		}
 	}
 	
 	public JEE5ModelProvider() {
 		super();
 	}
 
 	protected ProjectResourceSet getResourceSet(IProject proj2) {
 		return (ProjectResourceSet)WorkbenchResourceHelperBase.getResourceSet(proj);
 	}
 
 	public XMLResourceImpl getWritableResource() {
 		return writableResource;
 	}
 
 	public void setWritableResource(XMLResourceImpl writableResource) {
 		this.writableResource = writableResource;
 	}
 	
 	protected void resourceChanged(Resource aResource) {
 		if (hasListeners()) {
 			int eventCode = ModelProviderEvent.KNOWN_RESOURCES_CHANGED;
 			ModelProviderEvent evt = new ModelProviderEvent(eventCode, this, proj);
 			evt.addResource(aResource);
 			notifyListeners(evt);
 		}
 	}
 	
 	protected void resourceIsLoadedChanged(Resource aResource, boolean oldValue, boolean newValue) {
 		if (hasListeners()) {
 			int eventCode = newValue ? ModelProviderEvent.LOADED_RESOURCE : ModelProviderEvent.UNLOADED_RESOURCE;
 			ModelProviderEvent evt = new ModelProviderEvent(eventCode, this, proj);
 			evt.addResource(aResource);
 			notifyListeners(evt);
 		}
 	}
 	private void addManagedResource(XMLResourceImpl res) {
 		modelResources.add(res);
 		if (!res.eAdapters().contains(resourceAdapter))
 			res.eAdapters().add(resourceAdapter);
 	}
 	/**
 	 * Returns true if there are any listeners
 	 */
 	public boolean hasListeners() {
 		return !listeners.isEmpty();
 	}
 	
 	private URI getModuleURI(URI uri) {
 		URI moduleuri = ModuleURIUtil.fullyQualifyURI(proj,getContentTypeDescriber());
 		IPath requestPath = new Path(moduleuri.path()).append(new Path(uri.path()));
 		URI resourceURI = URI.createURI(PlatformURLModuleConnection.MODULE_PROTOCOL + requestPath.toString());
 		return resourceURI;
 	}
 
 	protected XMLResourceImpl getModelResource(final IPath modelPath) {
 		if (writableResource != null) {
 			addManagedResource(writableResource);
 			return writableResource;
 		}
 		IPath innerModelPath = modelPath;
 		if ((innerModelPath == null) || innerModelPath.equals(IModelProvider.FORCESAVE))
 			innerModelPath = getDefaultResourcePath();
 		ProjectResourceSet resSet = getResourceSet(proj);
 		IVirtualFolder container = ComponentCore.createComponent(proj).getRootFolder();
 		String modelPathURI = innerModelPath.toString();
 		URI uri = URI.createURI(modelPathURI);
 		
 		IPath projURIPath = new Path("");//$NON-NLS-1$
 		projURIPath = projURIPath.append(container.getProjectRelativePath());
 		projURIPath = projURIPath.addTrailingSeparator();
 		projURIPath = projURIPath.append(innerModelPath);
 		URI projURI = URI.createURI(projURIPath.toString());
 		XMLResourceImpl res = null;
 		try {
 			if (proj.getFile(projURI.toString()).exists())
 			{
 				res = (XMLResourceImpl) resSet.getResource(getModuleURI(uri),true);
 				addManagedResource(res);
 			} else {//First find in resource set, then create if not found new Empty Resource.
 				XMLResourceImpl newRes =  createModelResource(innerModelPath, resSet, projURI);
 				addManagedResource(newRes);
 				return newRes;
 			}
 		} catch (WrappedException ex) {
 			if (ex.getCause() instanceof FileNotFoundException)
 				return null;
 			throw ex;
 		}
 		return res;
 	}
 	
 
 	protected XMLResourceImpl createModelResource(IPath modelPath, ProjectResourceSet resourceSet, URI uri) {
 		// First try to find existing cached resource.
 		XMLResourceImpl res = (XMLResourceImpl)resourceSet.getResource(getModuleURI(uri), false);
 		if (res == null || !res.isLoaded()) {
 			// Create temp resource if no file exists
 			res=  (XMLResourceImpl)((FlexibleProjectResourceSet)resourceSet).createResource(getModuleURI(uri),WTPResourceFactoryRegistry.INSTANCE.getFactory(uri, getContentType(getContentTypeDescriber())));
 			populateRoot(res, resourceSet.getProject().getName());
 		}
 		return res;
 	}
 
 	public void populateRoot(XMLResourceImpl res, String string) {
 	}
 
 	private IContentDescription getContentType(String contentTypeDescriber) {
 		if (contentTypeDescriber != null)
 			return Platform.getContentTypeManager().getContentType(contentTypeDescriber).getDefaultDescription();
 		return null;
 	}
 
 	public IPath getDefaultResourcePath() {
 		return defaultResourcePath;
 	}
 
 	public void setDefaultResourcePath(IPath defaultResourcePath) {
 		this.defaultResourcePath = defaultResourcePath;
 	}
 
 	public Object getModelObject() {
 		return getModelObject(getDefaultResourcePath());
 	}
 
 	public Object getModelObject(IPath modelPath) {
 		return null;
 	}
 	
 	/**
 	 * Used to optionally define an associated content type for XML file creation
 	 * @return
 	 */
 	protected String getContentTypeDescriber() {
 		
 		return null;
 	}
 	
 
 
 	public IStatus validateEdit(final IPath modelPath, final Object context) {
 		IPath innaerModelPath = modelPath;
 		Object innerContext = context;
 		if (innaerModelPath == null)
 			innaerModelPath = getDefaultResourcePath();
 		IWorkspace work = ResourcesPlugin.getWorkspace();
 		IFile file = WorkbenchResourceHelper.getFile(getModelResource(innaerModelPath));
 		if (file != null && file.exists()) {
 			IFile[] files = { file };
 			if (innerContext == null)
 				innerContext = IWorkspace.VALIDATE_PROMPT;
 			return work.validateEdit(files, innerContext);
 		}
 		return Status.OK_STATUS;
 	}
 
 	public void modify(Runnable runnable, IPath modelPath) {
 		//About to modify and save this model
 		try {
 			JavaeeResourceImpl res = (JavaeeResourceImpl)getModelResource(modelPath);
 			if (res != null)
 				setWritableResource(res);
 			runnable.run();
 			try {
 				if (res != null) {
 					if (modelPath != null && modelPath.equals(IModelProvider.FORCESAVE))
 						res.save(Collections.EMPTY_MAP,true);
 					else
 						res.save(Collections.EMPTY_MAP);
 				}
 			} catch (IOException e) {
 				JEEPlugin.logError(e);
 			}
 		} catch (Exception ex) {
 			JEEPlugin.logError(ex);
 		} finally {
 			setWritableResource(null);
 		}
 		
 	}
 
 //	private class ResourceChangeListener implements IResourceChangeListener {
 //		public void resourceChanged(IResourceChangeEvent event) {
 //			IResourceDelta delta= event.getDelta();
 //			// make sure that there is a delta (since some events don't have one)
 //			if (delta != null)
 //			{
 //				IResourceDelta[] affectedChildren= delta.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.REMOVED , IResource.FILE);
 //				IResourceDelta projectDelta = null;
 //				IResource changedResource = null; 
 //				IProject changedProject = null;
 //				IPath resourcePath = null;
 //
 //				for (int i= 0; i < affectedChildren.length; i++) {
 //					projectDelta = affectedChildren[i];
 //					changedResource = projectDelta.getResource(); 
 //					changedProject = changedResource.getProject();
 //					HashSet<IPath> currentResources = modelResources.get(changedProject);
 //					// only deal with the projects that have resources that have been loaded 
 //					if (currentResources != null)
 //					{
 //						// if this is a project deletion, remove the project from the HashMap.
 //						if (changedResource == changedProject && projectDelta.getKind() == IResourceDelta.REMOVED)
 //						{
 //							modelResources.remove(changedProject);
 //							// if modelResources is empty, we should self-destruct
 //							if (modelResources.isEmpty())
 //							{
 //								resourceChangeListenerEnabled = false;
 //								ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 //							}
 //						}
 //						else
 //						{
 //							Iterator<IPath> iter = currentResources.iterator();
 //							ArrayList<IPath> toUnload = new ArrayList<IPath>();
 //							// check each resource that was loaded from the project to see if it is part of the change
 //							while (iter.hasNext())
 //							{
 //								resourcePath = iter.next();
 //								if (projectDelta.findMember(resourcePath) != null)
 //								{
 //									// limit the list of resources that need to be unloaded to those that have changed
 //									toUnload.add(resourcePath);
 //								}
 //							}
 //							if (toUnload.size() > 0)
 //							{
 //								Resource current = null;
 //								ProjectResourceSet resourceSet = getResourceSet(changedProject);
 //								URIConverter uriConverter = resourceSet.getURIConverter();
 //								HashSet<URI> resourceURIs = new HashSet<URI>();
 //								iter = toUnload.iterator();
 //								while (iter.hasNext())
 //								{
 //									// convert all of the resources to URIs - this is a faster match during the compare
 //									resourceURIs.add(uriConverter.normalize(URI.createURI(iter.next().toString())));
 //								}
 //								Iterator<Resource> iter2 = resourceSet.getResources().iterator();
 //								while (iter2.hasNext())
 //								{
 //									current = iter2.next();
 //									if (resourceURIs.contains(current.getURI()))
 //									{
 //										current.unload();
 //									}
 //								}
 //							}
 //						}
 //					}
 //				}
 //			}
 //		}
 //	}
 
 	public void addListener(IModelProviderListener listener) {
 		
 		listeners.add(listener);
 	}
 
 	public void removeListener(IModelProviderListener listener)
 	{
 		listeners.remove(listener);
 	}
 	/**
 	 * Save only resources that need to be saved (i.e., no other references).
 	 */
 	public void modelsChanged(IModelProviderEvent anEvent) {
 		int code = anEvent.getEventCode();
 		switch (code) {
 			case IModelProviderEvent.REMOVED_RESOURCE : {
 				if (hasResourceReference(anEvent.getChangedResources()))
 					removeResources(anEvent.getChangedResources());
 				else
 					return;
 				break;
 			}
 		}
 		if (hasListeners()) {
 			anEvent.setModel(this);
 			notifyListeners(anEvent);
 		}
 	}
 	protected void removeResources(List aList) {
 		Resource res;
 		for (int i = 0; i < aList.size(); i++) {
 			res = (Resource) aList.get(i);
 			removeResource(res) ;
 		}
 	}
 	/**
 	 * Remove reference to the aResource.
 	 */
 	protected boolean removeResource(Resource aResource) {
 		if (aResource != null) {
 			aResource.eAdapters().remove(resourceAdapter);
 			return getResources().remove(aResource);
 		}
 		return false;
 	}
 	
 	/**
 	 * Return true if any Resource in the list of
 	 * 
 	 * @resources is referenced by me.
 	 */
 	protected boolean hasResourceReference(List tResources) {
 		for (int i = 0; i < tResources.size(); i++) {
 			if (hasResourceReference((Resource) tResources.get(i)))
 				return true;
 		}
 		return false;
 	}
 	/**
 	 * Return true if aResource is referenced by me.
 	 */
 	protected boolean hasResourceReference(Resource aResource) {
 		if (aResource != null)
 			return getResources().contains(aResource);
 		return false;
 	}
 	
 	/**
 	 * Notify listeners of
 	 * 
 	 * @anEvent.
 	 */
 	protected void notifyListeners(IModelProviderEvent anEvent) {
 		
 		NotifyRunner notifier = new NotifyRunner(anEvent); 
 		
 		Object[] notifyList = listeners.getListeners(); 
 		for (int i = 0; i < notifyList.length; i++) {
 			notifier.setListener( (IModelProviderListener) notifyList[i] );
 			SafeRunner.run(notifier);
 		}
 	}
 	public class NotifyRunner implements ISafeRunnable { 
 		
 		private final IModelProviderEvent event;
 		private IModelProviderListener listener;
 		
 		public NotifyRunner(IModelProviderEvent event) {
 			Assert.isNotNull(event);
 			this.event = event;
 		}
 		
 		
 		public void setListener(IModelProviderListener listener) {
 			this.listener = listener;
 		}
 
 		public void handleException(Throwable exception) { 
 			JEEPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JEEPlugin.PLUGIN_ID, 0, exception.getMessage(), exception));
 			
 		}
 
 		public void run() throws Exception {
 			if(listener != null)
 				listener.modelsChanged(event); 
 		}
 		
 	}
 	public ResourceStateValidator getStateValidator() {
 		if (stateValidator == null)
 			stateValidator = createStateValidator();
 		return stateValidator;
 	}
 
 	/**
 	 * Method createStateValidator.
 	 * 
 	 * @return ResourceStateValidator
 	 */
 	private ResourceStateValidator createStateValidator() {
 		return new ResourceStateValidatorImpl(this);
 	}
 
 	protected EnterpriseArtifactEdit createArtifactEdit() {
 		return null;
 	}
 
 	public void checkActivation(ResourceStateValidatorPresenter presenter) throws CoreException {
 		getStateValidator().checkActivation(presenter);
 		
 	}
 
 	public boolean checkReadOnly() {
 		return getStateValidator().checkReadOnly();
 	}
 
 	public boolean checkSave(ResourceStateValidatorPresenter presenter) throws CoreException {
 		return getStateValidator().checkSave(presenter);
 	}
 
 	public void lostActivation(ResourceStateValidatorPresenter presenter) throws CoreException {
 		getStateValidator().lostActivation(presenter);
 		
 	}
 
 	public IStatus validateState(ResourceStateValidatorPresenter presenter) throws CoreException {
 		if (presenter == null)
 			return Status.OK_STATUS;
 		return getStateValidator().validateState(presenter);
 	}
 
 	public void cacheNonResourceValidateState(List roNonResourceFiles) {
 		// do nothing
 	}
 
 	public List getNonResourceFiles() {
 		return null;
 	}
 
 	public List getNonResourceInconsistentFiles() {
 		return null;
 	}
 
 	public List getResources() {
 		return modelResources;
 	}
 
 
 	public boolean isDirty() {
 		
 			List list = getResources();
 			for (int i = 0; i < list.size(); i++) {
 				if (((Resource) list.get(i)).isModified())
 					return true;
 			}
 			return false;
 	}
 }
