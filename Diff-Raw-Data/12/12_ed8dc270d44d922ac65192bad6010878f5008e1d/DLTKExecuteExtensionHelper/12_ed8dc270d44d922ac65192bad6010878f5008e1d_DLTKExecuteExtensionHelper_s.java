 package org.eclipse.dltk.ui;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 
 public class DLTKExecuteExtensionHelper {
 	public static IDLTKLanguageToolkit getLanguageToolkit(IConfigurationElement config,
 			String propertyName, Object data) {
 		String nature = null;
 		if (data instanceof String) {
 			nature = (String) data;
 
 		} else if (data instanceof Map) {
 			nature = (String) ((Map) data).get("nature"); //$NON-NLS-1$
 		}
 		if (nature != null) {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager.getLanguageToolkit(nature);
 			if( toolkit == null ) {
 				throw new RuntimeException(
						Messages.DLTKExecuteExtensionHelper_natureAttributeMustBeSpecifiedAndCirrect);
 			}
 			return toolkit;
 		} else {
 			throw new RuntimeException(
					Messages.DLTKExecuteExtensionHelper_natureAttributeMustBeSpecifiedAndCirrect);
 		}
 	}
 }
