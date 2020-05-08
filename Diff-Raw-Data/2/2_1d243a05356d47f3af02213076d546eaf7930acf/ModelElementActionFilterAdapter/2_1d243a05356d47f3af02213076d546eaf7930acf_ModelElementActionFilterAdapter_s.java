 package org.eclipse.dltk.internal.ui;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.internal.core.BasicDLTKExtensionManager;
 import org.eclipse.dltk.ui.actions.IActionFilterTester;
 import org.eclipse.ui.IActionFilter;
 
 public class ModelElementActionFilterAdapter implements IActionFilter {
 	private final static String ACTION_FILTER_TESTER = "org.eclipse.dltk.ui.actionFilterTester";
 	private static class IdBasedExtensionManager extends BasicDLTKExtensionManager {
 		private static final String CLASS_ATTR = "class";
 		public IdBasedExtensionManager(String extension) {
 			super(extension);
			this.identifier = "id";
 		}
 		public IActionFilterTester getObject(String id) throws CoreException {
 			ElementInfo ext = this.getElementInfo(id);
 
 			return (IActionFilterTester)getInitObject(ext);
 		}
 
 		public Object getInitObject(ElementInfo ext) throws CoreException {
 			if (ext != null) {
 				if (ext.object != null) {
 					return ext.object;
 				}
 
 				IConfigurationElement cfg = (IConfigurationElement) ext.getConfig();
 				Object object = createObject(cfg);
 				ext.object = object;
 				return object;
 			}
 			return null;
 		}
 
 		protected Object createObject(IConfigurationElement cfg) throws CoreException {
 			return cfg.createExecutableExtension(CLASS_ATTR);
 		}
 	}
 	private static IdBasedExtensionManager actionFilterTesters = new IdBasedExtensionManager(ACTION_FILTER_TESTER) {
 		
 	};
 	public boolean testAttribute(Object target, String name, String value) {
 		try {
 			IActionFilterTester tester = actionFilterTesters.getObject(name);
 			if( tester == null ) {
 				return false;
 			}
 			return tester.test(target, name, value);
 		} catch (CoreException e) {
 			if( DLTKCore.DEBUG ) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 }
