 package org.eclipse.jst.jsf.core.jsfappconfig.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.jst.jsf.common.internal.managedobject.AbstractManagedObject;
 import org.eclipse.jst.jsf.core.jsfappconfig.IFacesConfigChangeListener;
 import org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigLocater;
 import org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigProvider;
 import org.eclipse.jst.jsf.core.jsfappconfig.IJSFAppConfigProvidersChangeListener;
 import org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigProvidersChangeEvent;
 import org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigUtils;
 import org.eclipse.jst.jsf.facesconfig.emf.ApplicationType;
 import org.eclipse.jst.jsf.facesconfig.emf.BehaviorType;
 import org.eclipse.jst.jsf.facesconfig.emf.ComponentType;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigExtensionType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.FactoryType;
 import org.eclipse.jst.jsf.facesconfig.emf.FromViewIdType;
 import org.eclipse.jst.jsf.facesconfig.emf.LifecycleType;
 import org.eclipse.jst.jsf.facesconfig.emf.ManagedBeanType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationRuleType;
 import org.eclipse.jst.jsf.facesconfig.emf.PropertyResolverType;
 import org.eclipse.jst.jsf.facesconfig.emf.ReferencedBeanType;
 import org.eclipse.jst.jsf.facesconfig.emf.RenderKitType;
 import org.eclipse.jst.jsf.facesconfig.emf.ResourceBundleType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValidatorType;
 import org.eclipse.jst.jsf.facesconfig.emf.VariableResolverType;
 
 /**
  * Abstract JSFAppConfigManager that implementers of {@link IJSFAppConfigManager} MUST
  * extend.
  * <p>
  * JSFAppConfigManager provides an entry point to an entire JSF application
  * configuration, which is defined in one or more application configuration
  * resource files.
  */
 public abstract class AbstractJSFAppConfigManager 
 	extends 	AbstractManagedObject 
 	implements 	IJSFAppConfigManager { 
 
 	/**
 	 * The IProject
 	 * @deprecated - use getProject()
 	 */
 	protected IProject _project;
 
 	/**
 	 * Collection of {@link IJSFAppConfigLocater} instances.
 	 */
 	protected List<IJSFAppConfigLocater> configLocaters;
 	
 	/**
 	 * Collection of {@link IJSFAppConfigProvidersChangeListener} instances.
 	 */
 	protected List<IJSFAppConfigProvidersChangeListener> configProvidersChangeListeners;
 
 	/**
 	 * Map of application configuration model EMF classes to
 	 * {@link IFacesConfigChangeListener} instances.
 	 */
 	protected Map<Class, IFacesConfigChangeListener> facesConfigChangeListeners;
 
 	/**
 	 * Single {@link FacesConfigChangeAdapter} instance.
 	 */
 	protected FacesConfigChangeAdapter facesConfigChangeAdapter;
 
 	/**
 	 * Constructor
 	 * @param project
 	 */
 	public AbstractJSFAppConfigManager(final IProject project) {
 		_project = project;
 		initialize();
 	}
 	
 	public IProject getProject() {
 		return _project;
 	}
 
 	/**
 	 * Initializes instance by:
 	 * <ul>
 	 *  <li>creating facesConfigChangeListeners collection, </li>
 	 * 	<li>creating configProvidersChangeListeners collection, </li>
 	 * 	<li>creating and populating configLocaters collection, </li>
 	 * 	<li>invoking the startLocating() method on all configLocaters, </li>
 	 * </ul>
 	 */
 	protected void initialize() {
 		//create collections
 		facesConfigChangeListeners = new HashMap<Class, IFacesConfigChangeListener>();
 		configProvidersChangeListeners = new ArrayList<IJSFAppConfigProvidersChangeListener>();
 		configLocaters = new ArrayList<IJSFAppConfigLocater>();
 		//populate initial set of locaters
 		populateConfigLocaters();
 		//instruct locaters to start locating
 		startConfigLocaters();
 	}
 	
 	/**
 	 * Populates configLocaters using @link IJSFAppConfigLocater} implementations
 	 * from CompositeLocatorProviderStrategy
 	 */
 	protected void populateConfigLocaters() {		
 		final CompositeJSFAppConfigLocatorProviderStrategy clps = new CompositeJSFAppConfigLocatorProviderStrategy(this.getProject());
 		for (final IJSFAppConfigLocater locator : clps.getLocators()) {
 			locator.setJSFAppConfigManager(this);
 			configLocaters.add(locator);
 		}		
 	}
 
 	/**
 	 * Instructs set of {@link IJSFAppConfigLocater} instances to start
 	 * locating JSF application configuration resources.
 	 */
 	protected void startConfigLocaters() {
 		for (final IJSFAppConfigLocater configLocater : configLocaters) {
 			configLocater.startLocating();
 		}
 	}
 	
 	/**
 	 * Instructs set of {@link IJSFAppConfigLocater} instances to stop
 	 * locating JSF application configuration resources.
 	 */
 	protected void stopConfigLocaters() {		
 		for (final IJSFAppConfigLocater configLocater : configLocaters) {
 			configLocater.stopLocating();
 			configLocater.dispose();
 		}
 	}
 
 	/**
 	 * Called to respond to a change in the IProject instance to which this
 	 * instance belongs. Changes the cached IProject instance, stops all config
 	 * locaters, starts all config locaters.
 	 * 
 	 * @param newProject New IProject instance to which this manager belongs.
 	 * @deprecated - SHOULD NOT USE
 	 */
 	protected void changeProject(final IProject newProject) {
 		_project = newProject;
 		stopConfigLocaters();
 		startConfigLocaters();
 	}
 
 	public boolean addJSFAppConfigProvidersChangeListener(final IJSFAppConfigProvidersChangeListener listener) {
 		return configProvidersChangeListeners.add(listener);
 	}
 
 	public boolean removeJSFAppConfigProvidersChangeListener(final IJSFAppConfigProvidersChangeListener listener) {
 		return configProvidersChangeListeners.remove(listener);
 	}
 
 	public void notifyJSFAppConfigProvidersChangeListeners(final IJSFAppConfigProvider configProvider, final int eventType) {
 		final JSFAppConfigProvidersChangeEvent event = new JSFAppConfigProvidersChangeEvent(configProvider, eventType);
 		for (final IJSFAppConfigProvidersChangeListener listener : configProvidersChangeListeners) {
 			listener.changedJSFAppConfigProviders(event);
 		}
 	}
 
 	public Object addFacesConfigChangeListener(Class emfClass, IFacesConfigChangeListener listener) {
 		/* 
 		 * Get all models, which will ensure that each one has had a
 		 * FacesConfigChangeAdapter added to it (if the model is updateable).
 		 */
 		getFacesConfigModels();
 		return facesConfigChangeListeners.put(emfClass, listener);
 	}
 
 	public Object removeFacesConfigChangeListener(Class emfClass) {
 		return facesConfigChangeListeners.remove(emfClass);
 	}
 
 	public void notifyFacesConfigChangeListeners(final Notification notification) {
 		final Object emfFeature = notification.getFeature();
 		if (emfFeature instanceof EStructuralFeature) {
 			final Class emfClass = ((EStructuralFeature)emfFeature).getEType().getInstanceClass();
 			final IFacesConfigChangeListener listener = facesConfigChangeListeners.get(emfClass);
 			if (listener != null) {
 				listener.notifyChanged(notification);
 			}
 		}
 	}
 
 	/**
 	 * @return Set of {@link IJSFAppConfigProvider}s
 	 */
 	protected Set<IJSFAppConfigProvider> getJSFAppConfigProviders() {
 		final Set<IJSFAppConfigProvider> allConfigProviders = new LinkedHashSet<IJSFAppConfigProvider>();
 		final Iterator itConfigLocaters = configLocaters.iterator();
 		while (itConfigLocaters.hasNext()) {
 			final IJSFAppConfigLocater configLocater = (IJSFAppConfigLocater)itConfigLocaters.next();
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
 	protected List<FacesConfigType> getFacesConfigModels() {
 		final List<FacesConfigType> facesConfigModels = new ArrayList<FacesConfigType>();		
		for(final IJSFAppConfigProvider configProvider : getJSFAppConfigProviders()) {		
		    SafeRunner.run(new ISafeRunnable() {

                public void handleException(Throwable exception) {
                    //SafeRunner will log the exception
                }

                public void run() throws Exception {
                    final FacesConfigType facesConfig = configProvider.getFacesConfigModel();
                    if (facesConfig != null) {
                        facesConfigModels.add(facesConfig);
                    }
                }
		        
		    });
 		}
 		return facesConfigModels;
 	}
 
 //-----------------------  IManagedObject lifecycle methods ----------------------------//
     /**
      * Disposes of resources by:
      * <ul>
      *  <li>removing a resource change listener to the workspace</li>
      *  <li>removing instance as a session property of the IProject instance</li>
      *  <li>invoking the stopLocating() method on all configLocaters</li>
      *  <li>clearing the configLocaters collection</li>
      *  <li>clearing the configProvidersChangeListeners collection</li>
      *  <li>clearing the facesConfigChangeListeners collection</li>
      * </ul>
      */
     public void dispose() {
         //instruct locaters to stop locating
         stopConfigLocaters();
         //clear collections
         configLocaters.clear();
         configProvidersChangeListeners.clear();
         facesConfigChangeListeners.clear();
     }
     
 	public void destroy() {
 		// 		
 	}
 
 	public void checkpoint() {
 		//		
 	}
 //-------------------------------------------------------------------------------------------------//	
 
 	public List<ManagedBeanType> getManagedBeans() {
 		final List<ManagedBeanType> allManagedBeans = new ArrayList<ManagedBeanType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {		
 			final EList managedBeans = facesConfig.getManagedBean();
 			allManagedBeans.addAll(managedBeans);
 		}
 		return allManagedBeans;
     }
 
     public final List<String> getPropertyResolvers()
     {
         final List<String> allPropertyResolvers = new ArrayList<String>();
         final List<ApplicationType> applications = getApplications();
         for (final ApplicationType  application : applications) 
         {
             for (final Iterator it = application.getPropertyResolver().iterator(); it.hasNext();)
             {
                 final PropertyResolverType propRes = (PropertyResolverType) it.next();
                 String propClass = propRes.getTextContent();
                 if (propClass != null)
                 {
                     propClass = propClass.trim();
                     if (!"".equals(propClass)) //$NON-NLS-1$
                     {
                         allPropertyResolvers.add(propClass);
                     }
                 }
             }
         }
         return allPropertyResolvers;
 	}
 
 	public List<ValidatorType> getValidators() {
 		final List<ValidatorType> allValidators = new ArrayList<ValidatorType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {	
 			final EList validators = facesConfig.getValidator();
 			allValidators.addAll(validators);
 		}
 		return allValidators;
 	}
 
 	public final List<String> getVariableResolvers()
     {
         final List<String> allVariableResolvers = new ArrayList<String>();
         final List<ApplicationType> applications = getApplications();      
         for ( final ApplicationType  application : applications)
         {           
             for (final Iterator it = application.getVariableResolver().iterator(); it.hasNext();)
             {
                 final VariableResolverType varRes = (VariableResolverType) it.next();
                 String varClass = varRes.getTextContent();
                 if (varClass != null)
                 {
                     varClass = varClass.trim();
                     if (!"".equals(varClass)) //$NON-NLS-1$
                     {
                         allVariableResolvers.add(varClass);
                     }
                 }
             }
         }
         return allVariableResolvers;
     }
 
 	public List<ConverterType> getConverters() {
 		final List<ConverterType> allConverters = new ArrayList<ConverterType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList converters = facesConfig.getConverter();
 			allConverters.addAll(converters);
 		}
 		return allConverters;
 	}
 
 	public List<NavigationRuleType> getNavigationRules() {
 		final List<NavigationRuleType> allNavigationRules = new ArrayList<NavigationRuleType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList navigationRules = facesConfig.getNavigationRule();
 			allNavigationRules.addAll(navigationRules);
 		}
 		return allNavigationRules;
 	}
 
 	public List<NavigationRuleType> getNavigationRulesForPage(final IFile pageFile) {
 		final List<NavigationRuleType> navigationRulesForPage = new ArrayList<NavigationRuleType>();
 		final IPath pageFilePath = JSFAppConfigUtils.getWebContentFolderRelativePath(pageFile);
 		if (pageFilePath != null) {
 			String pageFileString = pageFilePath.toString();
 			if (!pageFileString.startsWith("/")) { //$NON-NLS-1$
 				pageFileString = "/" + pageFileString; //$NON-NLS-1$
 			}
 			final List<NavigationRuleType> navigationRules = getNavigationRules();
 			for (final NavigationRuleType navigationRule : navigationRules) {				
 				FromViewIdType fromViewIdType = navigationRule.getFromViewId();
 				if (fromViewIdType != null) {
 					final String fromViewId = fromViewIdType.getTextContent();
 					if (fromViewId != null && fromViewId.length() > 0) {
 						if (!fromViewId.equals("*")) { //$NON-NLS-1$
 							if (fromViewId.equals(pageFileString)) {
 								//exact match
 								navigationRulesForPage.add(navigationRule);
 							} else if (fromViewId.endsWith("*")) { //$NON-NLS-1$
 								final String prefixFromViewId = fromViewId.substring(0, fromViewId.length() - 1);
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
 
 	public List<ApplicationType> getApplications() {
 		final List<ApplicationType> allApplications = new ArrayList<ApplicationType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList applications = facesConfig.getApplication();
 			allApplications.addAll(applications);
 		}
 		return allApplications;
 	}
 
 	public List<FactoryType> getFactories() {
 		final List<FactoryType> allFactories = new ArrayList<FactoryType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList factories = facesConfig.getFactory();
 			allFactories.addAll(factories);
 		}
 		return allFactories;
 	}
 
 	public List<ComponentType> getComponents() {
 		final List<ComponentType> allComponents = new ArrayList<ComponentType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList components = facesConfig.getComponent();
 			allComponents.addAll(components);
 		}
 		return allComponents;
 	}
 
 	public List<ReferencedBeanType> getReferencedBeans() {
 		final List<ReferencedBeanType> allReferencedBeans = new ArrayList<ReferencedBeanType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {	
 			final EList referencedBeans = facesConfig.getReferencedBean();
 			allReferencedBeans.addAll(referencedBeans);
 		}
 		return allReferencedBeans;
 	}
 
 	public List<RenderKitType> getRenderKits() {
 		final List<RenderKitType>  allRenderKits = new ArrayList<RenderKitType> ();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList renderKits = facesConfig.getRenderKit();
 			allRenderKits.addAll(renderKits);
 		}
 		return allRenderKits;
 	}
 
 	public List<LifecycleType> getLifecycles() {
 		final List<LifecycleType> allLifecycles = new ArrayList<LifecycleType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList lifecycles = facesConfig.getLifecycle();
 			allLifecycles.addAll(lifecycles);
 		}
 		return allLifecycles;
 	}
 
     public List<ResourceBundleType> getResourceBundles()
     {
         final List<ResourceBundleType> allResourceBundles = new ArrayList<ResourceBundleType>();
         for (final FacesConfigType facesConfig : getFacesConfigModels()) {
             for (final Iterator applicationIt = facesConfig.getApplication().iterator(); applicationIt.hasNext();)
             {
                 final ApplicationType appType = (ApplicationType) applicationIt.next();
                 allResourceBundles.addAll(appType.getResourceBundle());
             }
         }
         return allResourceBundles;
     }
     
     public List<BehaviorType> getBehaviors()
     {
 		final List<BehaviorType> allBehaviors = new ArrayList<BehaviorType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {
 			final EList behaviors = facesConfig.getBehavior();
 			allBehaviors.addAll(behaviors);
 		}
 		return allBehaviors;
     }
     
     public List<FacesConfigExtensionType> getFacesConfigExtensions()
     {
 		final List<FacesConfigExtensionType> allFCExts = new ArrayList<FacesConfigExtensionType>();
 		for (final FacesConfigType facesConfig : getFacesConfigModels()) {	
 			final EList fcExts = facesConfig.getFacesConfigExtension();
 			allFCExts.addAll(fcExts);
 		}
 		return allFCExts;
     }
 
 	public void addFacesConfigChangeAdapter(final FacesConfigType facesConfig) {
 		if (facesConfig != null) {
 			if (facesConfigChangeAdapter == null) {
 				facesConfigChangeAdapter = new FacesConfigChangeAdapter();
 			}
 			facesConfig.eAdapters().add(facesConfigChangeAdapter);
 		}
 	}
 
 	public void removeFacesConfigChangeAdapter(final FacesConfigType facesConfig) {
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
 		public void notifyChanged(final Notification notification) {
 			super.notifyChanged(notification);
 			notifyFacesConfigChangeListeners(notification);
 		}
 	}
 
 
 
 }
