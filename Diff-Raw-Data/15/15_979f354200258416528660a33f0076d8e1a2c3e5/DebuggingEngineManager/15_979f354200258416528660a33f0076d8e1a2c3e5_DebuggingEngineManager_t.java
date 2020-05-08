 package org.eclipse.dltk.launching.debug;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.launching.debug.DebuggingEngine;
 import org.eclipse.dltk.internal.launching.debug.PriorityBasedDebuggingEngineSelector;
 import org.eclipse.dltk.launching.IInterpreterConfigModifierFactory;
 
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
 
 	private static final String DEBUGGING_ENGINE_EXT_POINT = DLTKLaunchingPlugin.ID_PLUGIN
 			+ ".debuggingEngine";
 
 	private static final String ID = "id";
 	private static final String MODEL_ID = "modelId";
 	private static final String NATURE_ID = "natureId";
 	private static final String NAME = "name";
 	private static final String DESCRIPTION = "description";
 	private static final String PRIORITY = "priority";
 	private static final String CLASS = "class";
 
 	private void addEngine(String natureId, IConfigurationElement element) {
 		final String id = element.getAttribute(ID);
 		final String modelId = element.getAttribute(MODEL_ID);
 		final String name = element.getAttribute(NAME);
 		final String description = element.getAttribute(DESCRIPTION);
 		final int priority = Integer.parseInt(element.getAttribute(PRIORITY));
 
 		try {
 			Object object = element.createExecutableExtension(CLASS);
 			if (object instanceof IInterpreterConfigModifierFactory ) {
 				IInterpreterConfigModifierFactory factory = (IInterpreterConfigModifierFactory) object;
 
 				IDebuggingEngine engine = new DebuggingEngine(id, modelId, natureId,
 						name, description, priority, factory);
 
 				List engines = (List) natureToEnginesMap.get(natureId);
 				if (engines == null) {
 					natureToEnginesMap.put(natureId, new ArrayList());
 				}
 
 				((List) natureToEnginesMap.get(natureId)).add(engine);
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
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
 
 				if (name.equals("engine")) {
 					addEngine(natureId, innerElement);
 				} else if (name.equals("selector")) {
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
 
 	private IDebuggingEngine[] NO_ENGINES = new IDebuggingEngine[0];
 
 	public IDebuggingEngine[] getDebuggingEngines(String natureId) {
 		List engines = (List) natureToEnginesMap.get(natureId);
 
 		if (engines != null) {
 			return (IDebuggingEngine[]) engines
 					.toArray(new IDebuggingEngine[engines.size()]);
 		}
 
 		return NO_ENGINES;
 	}
 
 	public IDebuggingEngine getSelectedDebuggineEngine(String natureId) {
 		IDebuggingEngineSelector selector = (IDebuggingEngineSelector) natureToSelectorMap
 				.get(natureId);
 		if (selector == null) {
 			selector = defaultSelector;
 		}
 
 		return selector.select(getDebuggingEngines(natureId));
 	}
 }
