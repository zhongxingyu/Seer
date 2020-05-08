 package org.eclipse.gmf.tooling.runtime;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.gmf.tooling.runtime.impl.ocl.tracker.activeocl.ActiveOclTrackerFactory;
 import org.eclipse.gmf.tooling.runtime.ocl.tracker.OclTrackerFactory;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 public class GMFToolingRuntimePlugin extends AbstractUIPlugin {
 
 	public static final String ID = "org.eclipse.gmf.tooling.runtime"; //$NON-NLS-1$
 
 	private static GMFToolingRuntimePlugin ourInstance;
 
 	private Map<OclTrackerFactory.Type, OclTrackerFactory> myOclTrackerFactories;
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		ourInstance = this;
 		myOclTrackerFactories = loadOclTrackerFactories();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		ourInstance = null;
 		super.stop(context);
 	}
 
 	public static GMFToolingRuntimePlugin getInstance() {
 		return ourInstance;
 	}
 
 	/**
 	 * This is the main intended way to access the {@link OclTrackerFactory}. 
 	 * This method will return the suited implementation based on the available plugins.
 	 * Caller should not make any assumptions against the return implementation type.   
 	 */
 	public OclTrackerFactory getOclTrackerFactory() {
 		OclTrackerFactory result = myOclTrackerFactories.get(OclTrackerFactory.Type.ANY);
 		if (result == null) {
 			result = new ActiveOclTrackerFactory();
 			myOclTrackerFactories.put(OclTrackerFactory.Type.ANY, result);
 		}
 		return result;
 	}
 
 	/**
 	 * This is the helper method allowing caller to access the specific {@link OclTrackerFactory} 
 	 * implementation without adding an explicit dependency to the containing plugin. 
 	 * <p> 
 	 * This method will try to return the preferred implementation based on the available plugins, 
 	 * but will roll back to default implementation if preferred one is not available.  
 	 * Caller still is not recommended to make any assumptions about the returned implementation type.   
 	 */
 	public OclTrackerFactory getOclTrackerFactory(OclTrackerFactory.Type type) {
 		OclTrackerFactory result = myOclTrackerFactories.get(type);
		return result != null ? result : getOclTrackerFactory();
 	}
 
 	private Map<OclTrackerFactory.Type, OclTrackerFactory> loadOclTrackerFactories() {
 		Map<OclTrackerFactory.Type, OclTrackerFactory> result = new HashMap<OclTrackerFactory.Type, OclTrackerFactory>();
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint(ID + ".ocl_tracker_factory");
 		IConfigurationElement points[] = extensionPoint.getConfigurationElements();
 		for (IConfigurationElement point : points) {
 			if ("oclTrackerFactory".equals(point.getName())) {
 				Object impl;
 				try {
 					impl = point.createExecutableExtension("class");
 				} catch (CoreException e) {
 					getLog().log(e.getStatus());
 					continue;
 				}
 				if (impl instanceof OclTrackerFactory) {
 					OclTrackerFactory factory = (OclTrackerFactory) impl;
 					result.put(factory.getImplementationType(), factory);
 					if (Boolean.valueOf(point.getAttribute("default"))) {
 						result.put(OclTrackerFactory.Type.ANY, factory);
 					}
 				}
 			}
 		}
 		if (!result.containsKey(OclTrackerFactory.Type.DEFAULT_GMFT)) {
 			result.put(OclTrackerFactory.Type.DEFAULT_GMFT, new ActiveOclTrackerFactory());
 		}
 		return result;
 	}
 
 }
