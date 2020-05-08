 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.jsfappconfig;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.facesconfig.emf.ApplicationType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.FromViewIdType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationRuleType;
 
 /**
  * JSFAppConfigManager provides an entry point to an entire JSF application
  * configuration, which is defined in one or more application configuration
  * resource files.
  * 
  * @author Ian Trimble - Oracle
  */
 public class JSFAppConfigManager implements IResourceChangeListener {
 
 	/**
 	 * Key that is used for the IProject instance's session property that
 	 * holds a JSFAppConfigManager instance.
 	 */
 	public static final QualifiedName KEY_SESSIONPROPERTY =
 		new QualifiedName(JSFCorePlugin.PLUGIN_ID, "JSFAppConfigManager"); //$NON-NLS-1$
 
 	/**
 	 * IProject instance to which this JSFAppConfigManager instance is keyed.
 	 */
 	protected IProject project = null;
 
 	/**
 	 * Collection of {@link IJSFAppConfigLocater} instances.
 	 */
 	protected List configLocaters = null;
 
 	/**
 	 * Collection of {@link IJSFAppConfigProvidersChangeListener} instances.
 	 */
 	protected List configProvidersChangeListeners = null;
 
 	/**
 	 * Map of application configuration model EMF classes to
 	 * {@link IFacesConfigChangeListener} instances.
 	 */
 	protected Map facesConfigChangeListeners = null;
 
 	/**
 	 * Single {@link FacesConfigChangeAdapter} instance.
 	 */
 	protected FacesConfigChangeAdapter facesConfigChangeAdapter = null;
 
 	/**
 	 * Gets a JSFAppConfigManager instance that is keyed to the passed IProject
 	 * parameter. May return null if the project is not valid or if a
 	 * CoreException is thrown while attempting to get or set the instance as
 	 * a session property.
 	 * 
 	 * @param project IProject instance to which the returned
 	 * JSFAppConfigManager instance is keyed.
 	 * @return JSFAppConfigManager instance, or null.
 	 */
	public static synchronized JSFAppConfigManager getInstance(IProject project) {
 		JSFAppConfigManager manager = null;
 		if (JSFAppConfigUtils.isValidJSFProject(project)) {
 			manager = getFromSessionProperty(project);
 			if (manager == null) {
 				manager = new JSFAppConfigManager(project);
 			}
 		}
 		return manager;
 	}
 
 	/**
 	 * Attempts to get a JSFAppConfigManager instance from a session property
 	 * of the passed IProject instance. Will return null if the session
 	 * property has not yet been set.
 	 * 
 	 * @param project IProject instance from which to retrieve the
 	 * JSFAppConfigManager instance.
 	 * @return JSFAppConfigManager instance, or null.
 	 */
 	protected static JSFAppConfigManager getFromSessionProperty(IProject project) {
 		JSFAppConfigManager manager = null;
 		try {
 			Object obj = project.getSessionProperty(KEY_SESSIONPROPERTY);
 			if (obj != null && obj instanceof JSFAppConfigManager) {
 				manager = (JSFAppConfigManager)obj;
 			}
 		} catch(CoreException ce) {
 			//log error
 			JSFCorePlugin.log(IStatus.ERROR, ce.getLocalizedMessage(), ce);
 		}
 		return manager;
 	}
 
 	/**
 	 * Sets this JSFAppConfigManager instance as a session property of its
 	 * IProject instance.
 	 */
 	protected void setAsSessionProperty() {
 		if (project != null && project.isAccessible()) {
 			try {
 				project.setSessionProperty(KEY_SESSIONPROPERTY, this);
 			} catch(CoreException ce) {
 				//log error
 				JSFCorePlugin.log(IStatus.ERROR, ce.getLocalizedMessage(), ce);
 			}
 		}
 	}
 
 	/**
 	 * Unsets this JSFAppConfigManager instance as a session property of its
 	 * IProject instance.
 	 */
 	protected void unsetAsSessionProperty() {
 		if (project != null && project.isAccessible()) {
 			try {
 				project.setSessionProperty(KEY_SESSIONPROPERTY, null);
 			} catch(CoreException ce) {
 				//log error
 				JSFCorePlugin.log(IStatus.ERROR, ce.getLocalizedMessage(), ce);
 			}
 		}
 	}
 
 	/**
 	 * Constructor is private to prevent direct instantiation; call
 	 * getInstance(IProject).
 	 * 
 	 * @param project IProject instance to which the new JSFAppConfigManager
 	 * instance is keyed.
 	 */
 	private JSFAppConfigManager(IProject project) {
 		this.project = project;
 		initialize();
 	}
 
 	/**
 	 * Gets this instance's IProject instance.
 	 * 
 	 * @return This instance's IProject instance.
 	 */
 	public IProject getProject() {
 		return project;
 	}
 
 	/**
 	 * Initializes instance by:
 	 * <ul>
 	 *  <li>creating facesConfigChangeListeners collection, </li>
 	 * 	<li>creating configProvidersChangeListeners collection, </li>
 	 * 	<li>creating and populating configLocaters collection, </li>
 	 * 	<li>invoking the startLocating() method on all configLocaters, </li>
 	 * 	<li>setting instance as a session property of the IProject instance, </li>
 	 *  <li>adding a resource change listener to the workspace.</li>
 	 * </ul>
 	 */
 	protected void initialize() {
 		//create collections
 		facesConfigChangeListeners = new HashMap();
 		configProvidersChangeListeners = new ArrayList();
 		configLocaters = new ArrayList();
 		//populate initial set of locaters
 		populateConfigLocaters();
 		//instruct locaters to start locating
 		startConfigLocaters();
 		//set as session property of project
 		setAsSessionProperty();
 		//add resource change listener
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	/**
 	 * Populates configLocaters Set with "built-in" set of
 	 * {@link IJSFAppConfigLocater} implementations.
 	 */
 	protected void populateConfigLocaters() {
 		//implicit runtime-provided configuration
 		IJSFAppConfigLocater implicitRuntimeConfigLocater = new ImplicitRuntimeJSFAppConfigLocater();
 		implicitRuntimeConfigLocater.setJSFAppConfigManager(this);
 		configLocaters.add(implicitRuntimeConfigLocater);
 		//default ("/WEB-INF/faces-config.xml") locater
 		IJSFAppConfigLocater defaultConfigLocater = new DefaultJSFAppConfigLocater();
 		defaultConfigLocater.setJSFAppConfigManager(this);
 		configLocaters.add(defaultConfigLocater);
 		//web.xml context-parameter specified locater
 		IJSFAppConfigLocater contextParamConfigLocater = new ContextParamSpecifiedJSFAppConfigLocater();
 		contextParamConfigLocater.setJSFAppConfigManager(this);
 		configLocaters.add(contextParamConfigLocater);
 		//runtime classpath locater
 		IJSFAppConfigLocater classpathConfigLocater = new RuntimeClasspathJSFAppConfigLocater();
 		classpathConfigLocater.setJSFAppConfigManager(this);
 		configLocaters.add(classpathConfigLocater);
 	}
 
 	/**
 	 * Instructs set of {@link IJSFAppConfigLocater} instances to start
 	 * locating JSF application configuration resources.
 	 */
 	protected void startConfigLocaters() {
 		Iterator itConfigLocaters = configLocaters.iterator();
 		while (itConfigLocaters.hasNext()) {
 			IJSFAppConfigLocater configLocater = (IJSFAppConfigLocater)itConfigLocaters.next();
 			configLocater.startLocating();
 		}
 	}
 
 	/**
 	 * Instructs set of {@link IJSFAppConfigLocater} instances to stop
 	 * locating JSF application configuration resources.
 	 */
 	protected void stopConfigLocaters() {
 		Iterator itConfigLocaters = configLocaters.iterator();
 		while (itConfigLocaters.hasNext()) {
 			IJSFAppConfigLocater configLocater = (IJSFAppConfigLocater)itConfigLocaters.next();
 			configLocater.stopLocating();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		IResourceDelta delta = event.getDelta();
 		if (delta.getKind() == IResourceDelta.CHANGED) {
 			IResourceDelta[] removedDeltas = delta.getAffectedChildren(IResourceDelta.REMOVED);
 			if (removedDeltas.length == 1) {
 				IResourceDelta removedDelta = removedDeltas[0];
 				IResource removedResource = removedDelta.getResource();
 				if (removedResource != null && removedResource == project) {
 					IResourceDelta[] addedDeltas = delta.getAffectedChildren(IResourceDelta.ADDED);
 					if (addedDeltas.length == 1) {
 						IResourceDelta addedDelta = addedDeltas[0];
 						IResource addedResource = addedDelta.getResource();
 						if (addedResource != null && addedResource instanceof IProject) {
 							changeProject((IProject)addedResource);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called to respond to a change in the IProject instance to which this
 	 * instance belongs. Changes the cached IProject instance, stops all config
 	 * locaters, starts all config locaters.
 	 * 
 	 * @param newProject New IProject instance to which this manager belongs.
 	 */
 	protected void changeProject(IProject newProject) {
 		this.project = newProject;
 		stopConfigLocaters();
 		startConfigLocaters();
 	}
 
 	/**
 	 * Adds an instance of {@link IJSFAppConfigProvidersChangeListener}.
 	 * 
 	 * @param listener An instance of {@link IJSFAppConfigProvidersChangeListener}.
 	 * @return true if added, else false.
 	 */
 	public boolean addJSFAppConfigProvidersChangeListener(IJSFAppConfigProvidersChangeListener listener) {
 		return configProvidersChangeListeners.add(listener);
 	}
 
 	/**
 	 * Removes an instance of {@link IJSFAppConfigProvidersChangeListener}.
 	 * 
 	 * @param listener an instance of {@link IJSFAppConfigProvidersChangeListener}.
 	 * @return true if removed, else false.
 	 */
 	public boolean removeJSFAppConfigProvidersChangeListener(IJSFAppConfigProvidersChangeListener listener) {
 		return configProvidersChangeListeners.remove(listener);
 	}
 
 	/**
 	 * Notifies all {@link IJSFAppConfigProvidersChangeListener} instances of
 	 * a change in the Set of {@link IJSFAppConfigProvider} instances.
 	 * 
 	 * @param configProvider {@link IJSFAppConfigProvider} instance that has
 	 * changed.
 	 * @param eventType Event type.
 	 */
 	public void notifyJSFAppConfigProvidersChangeListeners(IJSFAppConfigProvider configProvider, int eventType) {
 		JSFAppConfigProvidersChangeEvent event = new JSFAppConfigProvidersChangeEvent(configProvider, eventType);
 		Iterator itListeners = configProvidersChangeListeners.iterator();
 		while (itListeners.hasNext()) {
 			IJSFAppConfigProvidersChangeListener listener =
 				(IJSFAppConfigProvidersChangeListener)itListeners.next();
 			listener.changedJSFAppConfigProviders(event);
 		}
 	}
 
 	/**
 	 * Adds an instance of {@link IFacesConfigChangeListener}. <br>
 	 * <br>
 	 * <b>NOTE:</b> Calling this method will cause all application
 	 * configuration models to be loaded, to ensure that a
 	 * {@link FacesConfigChangeAdapter} has been added to each model.
 	 * 
 	 * @param emfClass EMF class in which the listener is interested.
 	 * @param listener {@link IFacesConfigChangeListener} instance.
 	 * @return Previous {@link IFacesConfigChangeListener}, or null.
 	 */
 	public Object addFacesConfigChangeListener(Class emfClass, IFacesConfigChangeListener listener) {
 		/* 
 		 * Get all models, which will ensure that each one has had a
 		 * FacesConfigChangeAdapter added to it.
 		 */
 		getFacesConfigModels();
 		return facesConfigChangeListeners.put(emfClass, listener);
 	}
 
 	/**
 	 * Removes an instance of {@link IFacesConfigChangeListener}.
 	 * 
 	 * @param emfClass EMF class in which the listener was interested.
 	 * @return Removed {@link IFacesConfigChangeListener}, or null.
 	 */
 	public Object removeFacesConfigChangeListener(Class emfClass) {
 		return facesConfigChangeListeners.remove(emfClass);
 	}
 
 	/**
 	 * Notifies {@link IFacesConfigChangeListener} instances of model changes
 	 * in which they registered interest.
 	 * 
 	 * @param notification EMF {@link Notification} instance that describes the
 	 * model change.
 	 */
 	public void notifyFacesConfigChangeListeners(Notification notification) {
 		Object emfFeature = notification.getFeature();
 		if (emfFeature != null && emfFeature instanceof EStructuralFeature) {
 			Class emfClass = ((EStructuralFeature)emfFeature).getEType().getInstanceClass();
 			IFacesConfigChangeListener listener = (IFacesConfigChangeListener)facesConfigChangeListeners.get(emfClass);
 			if (listener != null) {
 				listener.notifyChanged(notification);
 			}
 		}
 	}
 
 	/**
 	 * Gets all {@link IJSFAppConfigProvider} instances from all
 	 * {@link IJSFAppConfigLocater} instances.
 	 * 
 	 * @return Set of all {@link IJSFAppConfigProvider} instances.
 	 */
 	public Set getJSFAppConfigProviders() {
 		Set allConfigProviders = new LinkedHashSet();
 		Iterator itConfigLocaters = configLocaters.iterator();
 		while (itConfigLocaters.hasNext()) {
 			IJSFAppConfigLocater configLocater = (IJSFAppConfigLocater)itConfigLocaters.next();
 			allConfigProviders.addAll(configLocater.getJSFAppConfigProviders());
 		}
 		return allConfigProviders;
 	}
 
 	/**
 	 * Gets all {@link FacesConfigType} instances from all
 	 * {@link IJSFAppConfigProvider} instances.
 	 * 
 	 * @return List of all {@link FacesConfigType} instances.
 	 */
 	public List getFacesConfigModels() {
 		List facesConfigModels = new ArrayList();
 		Iterator itConfigProviders = getJSFAppConfigProviders().iterator();
 		while (itConfigProviders.hasNext()) {
 			IJSFAppConfigProvider configProvider = (IJSFAppConfigProvider)itConfigProviders.next();
 			FacesConfigType facesConfig = configProvider.getFacesConfigModel();
 			if (facesConfig != null) {
 				facesConfigModels.add(facesConfig);
 			}
 		}
 		return facesConfigModels;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#finalize()
 	 */
 	protected void finalize() {
 		//remove resource change listener
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		workspace.removeResourceChangeListener(this);
 		//remove session property from project
 		unsetAsSessionProperty();
 		//instruct locaters to stop locating
 		stopConfigLocaters();
 		//clear collections
 		configLocaters.clear();
 		configProvidersChangeListeners.clear();
 		facesConfigChangeListeners.clear();
 	}
 
 	/**
 	 * Gets list of all ManagedBeanType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all ManagedBeanType instances from all known
 	 * faces-config models (list may be empty).
 	 */
 	public List getManagedBeans() {
 		List allManagedBeans = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList managedBeans = facesConfig.getManagedBean();
 			allManagedBeans.addAll(managedBeans);
 		}
 		return allManagedBeans;
 	}
 
 	/**
 	 * Gets list of all ValidatorType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all ValidatorType instances from all known faces-config
 	 * models (list may be empty).
 	 */
 	public List getValidators() {
 		List allValidators = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList validators = facesConfig.getValidator();
 			allValidators.addAll(validators);
 		}
 		return allValidators;
 	}
 
 	/**
 	 * Gets list of all ConverterType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all ConverterType instances from all known faces-config
 	 * models (list may be empty).
 	 */
 	public List getConverters() {
 		List allConverters = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList converters = facesConfig.getConverter();
 			allConverters.addAll(converters);
 		}
 		return allConverters;
 	}
 
 	/**
 	 * Gets list of all NavigationRuleType instances from all known
 	 * faces-config models; list may be empty.
 	 * 
 	 * @return List of all NavigationRuleType instances from all known
 	 * faces-config models (list may be empty).
 	 */
 	public List getNavigationRules() {
 		List allNavigationRules = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList navigationRules = facesConfig.getNavigationRule();
 			allNavigationRules.addAll(navigationRules);
 		}
 		return allNavigationRules;
 	}
 
 	/**
 	 * Gets list of all NavigationRuleType instances from all known
 	 * faces-config models where the navigation-rule's from-view-id value
 	 * matches the web content folder-relative value of the passed IFile
 	 * instance; list may be empty. Matching is performed in the same manner
 	 * as for a JSF implementation's default NavigationHandler.
 	 * 
 	 * @param pageFile IFile instance to match against the from-view-id value
 	 * of all NavigationRuleType instances. File is assumed to be relative to
 	 * the web content folder, but may be expressed in a more complete form;
 	 * its path will be calculated relative to the web content folder.
 	 * @return List of all NavigationRuleType instances from all known
 	 * faces-config models where the navigation-rule's from-view-id value
 	 * matches the web content folder-relative value of the passed IFile
 	 * instance (list may be empty).
 	 */
 	public List getNavigationRulesForPage(IFile pageFile) {
 		List navigationRulesForPage = new ArrayList();
 		IPath pageFilePath = JSFAppConfigUtils.getWebContentFolderRelativePath(pageFile);
 		if (pageFilePath != null) {
 			String pageFileString = pageFilePath.toString();
 			if (!pageFileString.startsWith("/")) {
 				pageFileString = "/" + pageFileString;
 			}
 			List navigationRules = getNavigationRules();
 			Iterator itNavigationRules = navigationRules.iterator();
 			while (itNavigationRules.hasNext()) {
 				NavigationRuleType navigationRule = (NavigationRuleType)itNavigationRules.next();
 				FromViewIdType fromViewIdType = navigationRule.getFromViewId();
 				if (fromViewIdType != null) {
 					String fromViewId = fromViewIdType.getTextContent();
 					if (fromViewId != null && fromViewId.length() > 0) {
 						if (!fromViewId.equals("*")) { //$NON-NLS-1$
 							if (fromViewId.equals(pageFileString)) {
 								//exact match
 								navigationRulesForPage.add(navigationRule);
 							} else if (fromViewId.endsWith("*")) { //$NON-NLS-1$
 								String prefixFromViewId = fromViewId.substring(0, fromViewId.length() - 1);
 								if (pageFileString.startsWith(prefixFromViewId)) {
 									//prefix match
 									navigationRulesForPage.add(navigationRule);
 								}
 							}
 						} else {
 							//from-view-id == "*" - matches all pages
 							navigationRulesForPage.add(navigationRule);
 						}
 					}
 				} else {
 					//no from-view-id element - matches all pages
 					navigationRulesForPage.add(navigationRule);
 				}
 			}
 		}
 		return navigationRulesForPage;
 	}
 
 	/**
 	 * Gets list of all ApplicationType instances from all known
 	 * faces-config models; list may be empty.
 	 * 
 	 * @return List of all ApplicationType instances from all known
 	 * faces-config models (list may be empty).
 	 */
 	public List getApplications() {
 		List allApplications = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList applications = facesConfig.getApplication();
 			allApplications.addAll(applications);
 		}
 		return allApplications;
 	}
 
 	/**
 	 * Gets list of all FactoryType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all FactoryType instances from all known faces-config
 	 * models (list may be empty).
 	 */
 	public List getFactories() {
 		List allFactories = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList factories = facesConfig.getFactory();
 			allFactories.addAll(factories);
 		}
 		return allFactories;
 	}
 
 	/**
 	 * Gets list of all ComponentType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all ComponentType instances from all known faces-config
 	 * models (list may be empty).
 	 */
 	public List getComponents() {
 		List allComponents = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList components = facesConfig.getComponent();
 			allComponents.addAll(components);
 		}
 		return allComponents;
 	}
 
 	/**
 	 * Gets list of all ReferencedBeanType instances from all known
 	 * faces-config models; list may be empty.
 	 * 
 	 * @return List of all ReferencedBeanType instances from all known
 	 * faces-config models (list may be empty).
 	 */
 	public List getReferencedBeans() {
 		List allReferencedBeans = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList referencedBeans = facesConfig.getReferencedBean();
 			allReferencedBeans.addAll(referencedBeans);
 		}
 		return allReferencedBeans;
 	}
 
 	/**
 	 * Gets list of all RenderKitType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all RenderKitType instances from all known faces-config
 	 * models (list may be empty).
 	 */
 	public List getRenderKits() {
 		List allRenderKits = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList renderKits = facesConfig.getRenderKit();
 			allRenderKits.addAll(renderKits);
 		}
 		return allRenderKits;
 	}
 
 	/**
 	 * Gets list of all LifecycleType instances from all known faces-config
 	 * models; list may be empty.
 	 * 
 	 * @return List of all LifecycleType instances from all known faces-config
 	 * models (list may be empty).
 	 */
 	public List getLifecycles() {
 		List allLifecycles = new ArrayList();
 		List facesConfigs = getFacesConfigModels();
 		Iterator itFacesConfigs = facesConfigs.iterator();
 		while (itFacesConfigs.hasNext()) {
 			FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
 			EList lifecycles = facesConfig.getLifecycle();
 			allLifecycles.addAll(lifecycles);
 		}
 		return allLifecycles;
 	}
 
     /**
      * @return the list of all resource bundles declared in all the FacesConfig
      * configurations found.
      */
     public List getResourceBundles()
     {
         List allResourceBundles = new ArrayList();
         List facesConfigs = getFacesConfigModels();
         Iterator itFacesConfigs = facesConfigs.iterator();
         while (itFacesConfigs.hasNext()) {
             FacesConfigType facesConfig = (FacesConfigType)itFacesConfigs.next();
             for (final Iterator applicationIt = facesConfig.getApplication().iterator(); applicationIt.hasNext();)
             {
                 ApplicationType appType = (ApplicationType) applicationIt.next();
                 allResourceBundles.addAll(appType.getResourceBundle());
             }
         }
         return allResourceBundles;
     }
     
 	/**
 	 * Adds this instance's {@link FacesConfigChangeAdapter} instance to the
 	 * passed application configuration model's adapters collection.
 	 * 
 	 * @param facesConfig Application configuration model's root object.
 	 */
 	public void addFacesConfigChangeAdapter(FacesConfigType facesConfig) {
 		if (facesConfig != null) {
 			if (facesConfigChangeAdapter == null) {
 				facesConfigChangeAdapter = new FacesConfigChangeAdapter();
 			}
 			facesConfig.eAdapters().add(facesConfigChangeAdapter);
 		}
 	}
 
 	/**
 	 * Removes this instance's {@link FacesConfigChangeAdapter} instance from
 	 * the passed application configuration model's adapters collection.
 	 * 
 	 * @param facesConfig Application configuration model's root object.
 	 */
 	public void removeFacesConfigChangeAdapter(FacesConfigType facesConfig) {
 		if (facesConfig != null && facesConfigChangeAdapter != null) {
 			facesConfig.eAdapters().remove(facesConfigChangeAdapter);
 		}
 	}
 
 	/**
 	 * FacesConfigChangeAdapter is an EMF adapter which provides a mechanism
 	 * for notification of changes to features in any application configuration
 	 * model for which {@link IFacesConfigChangeListener} instances have
 	 * registered an interest.
 	 * 
 	 * @author Ian Trimble - Oracle
 	 */
 	class FacesConfigChangeAdapter extends EContentAdapter {
 		/*
 		 * (non-Javadoc)
 		 * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
 		 */
 		public void notifyChanged(Notification notification) {
 			super.notifyChanged(notification);
 			notifyFacesConfigChangeListeners(notification);
 		}
 	}
 
 }
