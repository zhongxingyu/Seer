 /*
  * Licensed Material - Property of IBM
  * (C) Copyright IBM Corp. 2001, 2005 - All Rights Reserved.
  * US Government Users Restricted Rights - Use, duplication or disclosure
  * restricted by GSA ADP Schedule Contract with IBM Corp.
  */
 package org.eclipse.jst.j2ee.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.internal.emf.resource.CompatibilityXMIResource;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public class ModelProviderManager {
 	
 public static class ModelProviderKey { 
  		
 		protected IProjectFacetVersion version;
 		protected int priority;
 		
 		public ModelProviderKey() {
 			super();
 		}
 		public boolean equals(Object aOther){
 			if( this == aOther )
 				return true;
 			
 			ModelProviderKey otherKey = (ModelProviderKey)aOther;
 			if( version.equals( otherKey.version) )
 				return true;
 			return false;
 		}
 		
 		public int hashCode() {
 			return version.hashCode();
 		}		
 	}
 private static class ResourceSetListener extends AdapterImpl {
 	/*
 	 * @see Adapter#notifyChanged(new ENotificationImpl((InternalEObject)Notifier,
 	 *      int,(EStructuralFeature) EObject, Object, Object, int))
 	 */
 	public void notifyChanged(Notification notification) {
 		switch (notification.getEventType()) {
 			case Notification.ADD :
 				addedResource((Resource) notification.getNewValue());
 				break;
 			case Notification.REMOVE :
 				removedResource((Resource) notification.getOldValue());
 				break;
 			case Notification.REMOVE_MANY :
 				removedResources((List) notification.getOldValue());
 				break;
 		}
 	}
 }
 private static WeakHashMap modelsProviders = new WeakHashMap();
 private static final int DEFAULT_PRIORITY = 100;
 private static HashMap providers;
 protected static HashMap resourceSetListeners;
 
 	/**
 	 * 
 	 * @param 
 	 * @param 
 	 * @return IModelProvider for the given project of the given version, NULL if no IModelProvider exists for project, version pair
 	 */
 	public static IModelProvider getModelProvider(IProject project, IProjectFacetVersion vers) {
 		
 		IModelProviderFactory factory = getProvider(vers);
 		if(factory != null){
 			startListeningToResourceSet(project);
 			return factory.create(project);
 		}
 		
 		String errorMessage = J2EECommonMessages.getResourceString(
 				J2EECommonMessages.ERR_NO_MODEL_PROVIDER_FOR_PROJECT, new Object[] {project, vers});
 		Throwable error = new NullPointerException(errorMessage);
 		
 		J2EEPlugin.INSTANCE.getLogger().logError(error);
 				
 		return null;
 	}
 
 
 	/**
 	 * 
 	 * @param 
 	 * @param 
 	 * @return IModelProvider for the given component of the given version, NULL if no IModelProvider exists for virtual component/version pair
 	 */
 	public static IModelProvider getModelProvider(IVirtualComponent aModule, IProjectFacetVersion vers) {
 
 		IModelProviderFactory factory = getProvider(vers);
 		if(factory != null){
 			IModelProvider mp = factory.create(aModule);
 			addProvider(mp);
 			return mp;
 		}
 		
 		String errorMessage = J2EECommonMessages.getResourceString(
 				J2EECommonMessages.ERR_NO_MODEL_PROVIDER_FOR_PROJECT, new Object[] {aModule, vers});
 		Throwable error = new NullPointerException(errorMessage);
 		
 		J2EEPlugin.INSTANCE.getLogger().logError(error);
 		
 		return null;
 	}
 	
 		
 		private static void addProvider(IModelProvider mp) {
 			modelsProviders.put(mp, null);
 			
 		}
 	
 		/**
 		 * Notify all editModels of the change.
 		 */
 		private static void addedResource(Resource addedResource) {
 			if ((addedResource != null) && (addedResource instanceof CompatibilityXMIResource))
 				((CompatibilityXMIResource) addedResource).setFormat(CompatibilityXMIResource.FORMAT_MOF5);
 			IProject proj = WorkbenchResourceHelper.getProject(addedResource);
 				IModelProviderEvent event = new ModelProviderEvent(IModelProviderEvent.ADDED_RESOURCE, null,proj);
 				event.addResource(addedResource);
 				notifyModelProviders(event);
 			
 		}
 		/**
 		 * Notify all editModels of the change.
 		 */
 		protected static void notifyModelProviders(IModelProviderEvent anEvent) {
 		if ((anEvent == null) || (modelsProviders.size() == 0))
 				return;
 			List aList = new ArrayList();
 			synchronized (modelsProviders) {
 				aList.addAll(modelsProviders.keySet());
 			}
 		
 		for (int i = 0; i < aList.size(); i++) {
				IModelProvider mod;
				mod = (IModelProvider) aList.get(i);
 				try {
					if (mod instanceof IModelProviderListener)
					{
						((IModelProviderListener)mod).modelsChanged(anEvent);
					}
 				} catch (Exception e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 		}
 	
 		/**
 		 * Notify all editModels of the change.
 		 */
 		private static void removedResource(Resource removedResource) {
 			IProject proj = WorkbenchResourceHelper.getProject(removedResource);
 				IModelProviderEvent event = new ModelProviderEvent(IModelProviderEvent.REMOVED_RESOURCE, null,proj);
 				event.addResource(removedResource);
 				notifyModelProviders(event);
 			
 		}
 	
 		/**
 		 * Notify all editModels of the change.
 		 */
 		private static void removedResources(List removedResources) {
 			Resource firstRes = (Resource)removedResources.get(0);
 			IProject proj = WorkbenchResourceHelper.getProject(firstRes);
 				IModelProviderEvent event = new ModelProviderEvent(IModelProviderEvent.REMOVED_RESOURCE, null,proj);
 				event.addResources(removedResources);
 				notifyModelProviders(event);
 			
 		}
 		
 
 	/**
 	 * Used to register an IModelProviderFactory against a facet version
 	 * @param providerFactory - {@link IModelProviderFactory}
 	 * @param v - {@link IProjectFacetVersion}
 	 * @param priority - {@link String}- Used to allow multiple instances, the highest priority is chosen
 	 */
 	public static void registerProvider(IModelProviderFactory providerFactory,
 			IProjectFacetVersion v, String priority) {
 
 		int newPriority = (priority != null) ? Integer.parseInt(priority) : DEFAULT_PRIORITY;
 		int currentPriority = getProviderPriority(v);
 		if (newPriority <= currentPriority) {
 			ModelProviderKey key = createProviderKey(v, newPriority);
 			getProviders().put(key, providerFactory);
 		}
 	}
 
 	private static IModelProviderFactory getProvider(IProjectFacetVersion v) {
 		Set<ModelProviderKey> keys = getProviders().keySet();
 		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
 			ModelProviderKey key = (ModelProviderKey) iterator.next();
 			if (key.version.equals(v))
 				return (IModelProviderFactory)providers.get(key);
 		}
 		return null;
 	}
 	private static int getProviderPriority(IProjectFacetVersion v) {
 		Set<ModelProviderKey> keys = getProviders().keySet();
 		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
 			ModelProviderKey key = (ModelProviderKey) iterator.next();
 			if (key.version.equals(v))
 				return key.priority;
 		}
 		return DEFAULT_PRIORITY;
 	}
 	protected static Adapter getResourceSetListener(IProject project) {
 		if (resourceSetListeners == null)
 			resourceSetListeners = new HashMap();
 		Adapter listener = (Adapter)resourceSetListeners.get(project);
 		if (listener == null) {
 			listener = new ResourceSetListener();
 			resourceSetListeners.put(project, listener);
 		}
 		return listener;
 	}
 
 	private static J2EEModelProviderRegistry registry;
 
 
 	private static void initProviders() {
 		providers = new HashMap();
 		registry = J2EEModelProviderRegistry.getInstance();
 		
 	}
 	private static void startListeningToResourceSet(IProject project) {
 		ResourceSet set = WorkbenchResourceHelper.getResourceSet(project);
 		Adapter listener = getResourceSetListener(project);
 		if (set != null && !set.eAdapters().contains(listener))
 			set.eAdapters().add(listener);
 	}
 	private static ModelProviderKey createProviderKey(IProjectFacetVersion fv, int priority) {
 		ModelProviderKey key =  new ModelProviderKey();
 		key.priority = priority;
 		key.version = fv;
 		return key;
 	}
 
 	public static IModelProvider getModelProvider(IProject proj) {
 		IProjectFacetVersion facetVersion = getDefaultFacet(proj);
 		return getModelProvider(proj, facetVersion);
 		
 	}
 
 	public static IModelProvider getModelProvider(IVirtualComponent aModule) {
 		IProjectFacetVersion facetVersion = getDefaultFacet(aModule);
 		return getModelProvider(aModule, facetVersion);		
 	}
 
 	private static IProjectFacetVersion getDefaultFacet(IProject proj) {
 		String type = J2EEProjectUtilities.getJ2EEProjectType(proj);
 		IProjectFacet facet = ProjectFacetsManager.getProjectFacet(type);
 		IFacetedProject fp = null;
 		try {
 			fp = ProjectFacetsManager.create(proj);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (fp != null && facet != null) {
 			return fp.getInstalledVersion(facet);
 		}
 		return null;
 			
 	}
 
 	private static IProjectFacetVersion getDefaultFacet(IVirtualComponent aModule) {
 		String type = J2EEProjectUtilities.getJ2EEComponentType(aModule);
 		IProjectFacet facet = ProjectFacetsManager.getProjectFacet(type);
 		IFacetedProject fp = null;
 		try {
 			if (aModule.isBinary())
 			{
 				
 			}
 			else
 			{
 				fp = ProjectFacetsManager.create(aModule.getProject());
 			}
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (fp != null && facet != null) {
 			return fp.getInstalledVersion(facet);
 		}
 		return null;			
 	}
 
 	private static HashMap<ModelProviderKey, IModelProviderFactory> getProviders() {
 		if (registry == null)
 			initProviders();
 		return providers;
 	}
 
 }
