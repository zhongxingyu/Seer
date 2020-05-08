 package org.eclipse.dltk.launching.debug;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.launching.debug.DebuggingEngine;
 import org.eclipse.dltk.launching.IInterpreterRunnerFactory;
 
 public class DebuggingEngineManager {
 	private static DebuggingEngineManager instance;
 
 	public static DebuggingEngineManager getInstance() {
 		if (instance == null) {
 			instance = new DebuggingEngineManager();
 		}
 		return instance;
 	}
 
 	private IDebuggingEngineSelector defaultSelector;
 	private Map natureToEnginesMap;
 	private Map natureToSelectorMap;
 
 	private static final String DEBUGGING_ENGINE_EXT_POINT = DLTKLaunchingPlugin.PLUGIN_ID
 			+ ".debuggingEngine";
 
 	private static final String ID = "id";
 	private static final String NATURE_ID = "natureId";
 	private static final String PREFERENCE_PAGE_ID = "preferencePageId";
 	private static final String NAME = "name";
 	private static final String DESCRIPTION = "description";
 	private static final String PRIORITY = "priority";
 	private static final String CLASS = "class";
 
 	private static final String ENGINE_TAG = "engine";
 	private static final String SELECTOR_TAG = "selector";
 
 	private static final String SUPPORTS_SUSPEND_ON_ENTRY = "supportsSupendOnEntry";
 	private static final String SUPPORTS_SUSPEND_ON_EXIT = "supportsSuspendOnExit";
 
 	private void addEngine(String natureId, IConfigurationElement element) {
 		final String id = element.getAttribute(ID);
 		final String preferencePageId = element
 				.getAttribute(PREFERENCE_PAGE_ID);
 		final String name = element.getAttribute(NAME);
 		final String description = element.getAttribute(DESCRIPTION);
 		final int priority = Integer.parseInt(element.getAttribute(PRIORITY));
 
 		boolean supportsSuspendOnEntry = getOptionalBoolean(element,
 				SUPPORTS_SUSPEND_ON_ENTRY);
 		boolean supportsSuspendOnExit = getOptionalBoolean(element,
 				SUPPORTS_SUSPEND_ON_EXIT);
 
 		try {
 			Object object = element.createExecutableExtension(CLASS);
 			if (object instanceof IInterpreterRunnerFactory) {
 				IInterpreterRunnerFactory factory = (IInterpreterRunnerFactory) object;
 
 				IDebuggingEngine engine = new DebuggingEngine(id, natureId,
 						preferencePageId, name, description, priority,
 						supportsSuspendOnEntry, supportsSuspendOnExit, factory);
 
 				// Add to natureToEnginesMap
 				List engines = (List) natureToEnginesMap.get(natureId);
 				if (engines == null) {
 					engines = new ArrayList();
 					natureToEnginesMap.put(natureId, engines);
 				}
 				engines.add(engine);
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean getOptionalBoolean(IConfigurationElement element,
 			String name) {
 		String value = element.getAttribute(name);
 
 		// this is default behavior befor the 'bug fix'
 		if (value == null) {
 			return true;
 		}
 
		return (new Boolean(value)).booleanValue();
 	}
 
 	private void addSelector(String natureId, IConfigurationElement element) {
 		try {
 			Object object = element.createExecutableExtension(CLASS);
 			if (object instanceof IDebuggingEngineSelector) {
 				natureToSelectorMap.put(natureId, object);
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void loadExtenstionPoints() {
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtension[] extensions = registry.getExtensionPoint(
 				DEBUGGING_ENGINE_EXT_POINT).getExtensions();
 
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] elements = extension
 					.getConfigurationElements();
 
 			IConfigurationElement main = elements[0];
 
 			final String natureId = main.getAttribute(NATURE_ID);
 
 			IConfigurationElement[] innerElements = main.getChildren();
 
 			for (int j = 0; j < innerElements.length; j++) {
 				IConfigurationElement innerElement = innerElements[j];
 				String name = innerElement.getName();
 
 				if (name.equals(ENGINE_TAG)) {
 					addEngine(natureId, innerElement);
 				} else if (name.equals(SELECTOR_TAG)) {
 					addSelector(natureId, innerElement);
 				}
 			}
 		}
 	}
 
 	protected DebuggingEngineManager() {
 		defaultSelector = new PriorityBasedDebuggingEngineSelector();
 		natureToEnginesMap = new HashMap();
 		natureToSelectorMap = new HashMap();
 
 		loadExtenstionPoints();
 	}
 
 	private static IDebuggingEngine[] NO_ENGINES = new IDebuggingEngine[0];
 
 	/**
 	 * Returns debugging engine selector for script language with nature
 	 * natureId
 	 * 
 	 * @param natureId
 	 * @return
 	 */
 	public IDebuggingEngineSelector getDebuggingEngineSelector(String natureId) {
 		IDebuggingEngineSelector selector = (IDebuggingEngineSelector) natureToSelectorMap
 				.get(natureId);
 
 		if (selector == null) {
 			selector = defaultSelector;
 		}
 
 		return selector;
 	}
 
 	/**
 	 * @param natureId
 	 * @return
 	 */
 	public boolean hasDebuggingEngineSelector(String natureId) {
 		return natureToSelectorMap.containsKey(natureId);
 	}
 
 	/**
 	 * 
 	 * @param natureId
 	 * @return
 	 */
 	public IDebuggingEngine[] getDebuggingEngines(String natureId) {
 		final List engines = (List) natureToEnginesMap.get(natureId);
 
 		if (engines != null) {
 			return (IDebuggingEngine[]) engines
 					.toArray(new IDebuggingEngine[engines.size()]);
 		}
 
 		return NO_ENGINES;
 	}
 
 	/**
 	 * 
 	 * @param natureId
 	 * @return
 	 */
 	public boolean hasDebuggingEngines(String natureId) {
 		final List engines = (List) natureToEnginesMap.get(natureId);
 		return !engines.isEmpty();
 	}
 
 	public IDebuggingEngine getDebuggingEngine(String id) {
 		final Iterator it = natureToEnginesMap.keySet().iterator();
 		while (it.hasNext()) {
 			final List list = (List) natureToEnginesMap.get(it.next());
 			final Iterator listIt = list.iterator();
 			while (listIt.hasNext()) {
 				IDebuggingEngine engine = (IDebuggingEngine) listIt.next();
 				if (engine.getId().equals(id)) {
 					return engine;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * @deprecated
 	 * @param natureId
 	 * @param id
 	 * @return
 	 */
 	public IDebuggingEngine getDebuggingEngine(String natureId, String id) {
 		return getDebuggingEngine(id);
 	}
 
 	/**
 	 * Returns selected debugging engine for script language with natureId. Uses
 	 * default debugging engine selector (priority based) if custom selector is
 	 * not contributed.
 	 * 
 	 * @param natureId
 	 * @return Selected debugging engine or null (if there are no debugging
 	 *         engines at all or there are no selected engine)
 	 */
 	public IDebuggingEngine getSelectedDebuggingEngine(String natureId) {
 		IDebuggingEngine[] engines = getDebuggingEngines(natureId);
 
 		if (engines.length > 0) {
 			IDebuggingEngineSelector selector = getDebuggingEngineSelector(natureId);
 			return selector.select(engines);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns if script language with nature natureId has selected debugging
 	 * engine. If this method returns false then getSelectedDebuggingEngine
 	 * returns null.
 	 * 
 	 * @param natureId
 	 * @return
 	 */
 	public boolean hasSelectedDebuggingEngine(String natureId) {
 		return getSelectedDebuggingEngine(natureId) != null;
 	}
 }
