 package org.eclipse.dltk.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.IModelElement;
 
 
 public class DLTKUILanguageManager {
 
 	private final static String LANGUAGE_EXTPOINT = DLTKUIPlugin.PLUGIN_ID
 			+ ".language";
 
 	private final static String NATURE_ATTR = "nature";
 
 	private static Map toolkits;
 
 	private static void initialize() {
 		if (toolkits != null) {
 			return;
 		}
 
 		toolkits = new HashMap(5);
 		IConfigurationElement[] cfg = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(LANGUAGE_EXTPOINT);
 
 		for (int i = 0; i < cfg.length; i++) {
 			String nature = cfg[i].getAttribute(NATURE_ATTR);
 			if (toolkits.get(nature) != null)
 				System.err.println("TODO log redeclaration");
 			toolkits.put(nature, cfg[i]);
 		}
 	}
 
 	private static String findScriptNature(IProject project)
 			throws CoreException {
 		initialize();
 
 		String[] natureIds = project.getDescription().getNatureIds();
 		for (int i = 0; i < natureIds.length; i++) {
 			String natureId = natureIds[i];
 
 			if (toolkits.containsKey(natureId)) {
 				return natureId;
 			}
 		}
 
 		return null;
 	}
 
 	private static IDLTKUILanguageToolkit getLanguageToolkit(String natureId)
 			throws CoreException {
 		initialize();
 
 		Object ext = toolkits.get(natureId);
 
 		if (ext != null) {
 			if (ext instanceof IDLTKUILanguageToolkit)
 				return (IDLTKUILanguageToolkit) ext;
 
 			IConfigurationElement cfg = (IConfigurationElement) ext;
 			IDLTKUILanguageToolkit toolkit = (IDLTKUILanguageToolkit) cfg
 					.createExecutableExtension("class");
 			toolkits.put(natureId, toolkit);
 			return toolkit;
 		}
 		return null;
 	}
 
 
 	public static IDLTKUILanguageToolkit getLangaugeToolkit(IModelElement element)
 			throws CoreException {
 		IProject project = element.getScriptProject().getProject();
 		String natureId = findScriptNature(project);
 		if (natureId != null) {
 			IDLTKUILanguageToolkit toolkit = getLanguageToolkit(natureId);
 			if (toolkit != null) {
 				return toolkit;
 			}
 		}
 		return null;
 //
 //		IStatus status = new Status(IStatus.ERROR, DLTKCore.PLUGIN_ID, 0,
 //				"Project has no associated script nature", null);
 //		throw new CoreException(status);
 	}
 }
