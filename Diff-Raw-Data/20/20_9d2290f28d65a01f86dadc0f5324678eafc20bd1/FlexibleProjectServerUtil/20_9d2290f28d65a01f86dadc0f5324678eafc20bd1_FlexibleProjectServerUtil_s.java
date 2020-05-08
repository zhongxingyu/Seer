 package org.eclipse.jst.j2ee.internal.deployables;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.wst.common.componentcore.internal.ComponentType;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.Module;
 import org.eclipse.wst.server.core.internal.ModuleFactory;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 
 public class FlexibleProjectServerUtil {
 	public static final String JST_EJB_ID = "jst.ejb";
 	public static final String JST_WEB_ID = "jst.web";
 	public static final String JST_EAR_ID = "jst.ear";
 	public static final String J2EE_EJB_ID = "j2ee.ejb";
 	public static final String J2EE_WEB_ID = "j2ee.web";
 	public static final String J2EE_EAR_ID = "j2ee.ear";
 
 
 	public static IJ2EEModule getModuleDelegate(WorkbenchComponent component) {
 		IModule[] modules  = getModules(component);
 		for (int i = 0; i < modules.length; i++) {
 			Module module = (Module) modules[i];
 			if (module.getName().equals(component.getName()))
				return getModuleDelgate(module);
 		}
 
 		return null;
 	}
 	
	public static IJ2EEModule getModuleDelegate(IVirtualComponent component) {
 		IModule[] modules  = getModules(component);
 		for (int i = 0; i < modules.length; i++) {
 			Module module = (Module) modules[i];
			if (module.getName().equals(component.getName()))
				return getModuleDelgate(module);
 		}
 
 		return null;
 	}
 
	public static IJ2EEModule getModuleDelgate(Module module) {
 		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
 		for (int i = 0; i < factories.length; i++) {
 			if (factories[i].getDelegate() == null)
 				continue;
 			ModuleDelegate moduleDel = factories[i].getDelegate().getModuleDelegate(module);
 			if (moduleDel != null)
 				return (IJ2EEModule) moduleDel;
 		}
 		return null;
 	}
 
 	public static IModule getModule(WorkbenchComponent component) {
 		IModule[] modules = getModules(component);
 		for (int i = 0; i < modules.length; i++) {
 			Module module = (Module) modules[i];
 			if (module.getName().equals(component.getName()))
 				return module;
 		}
 
 		return null;
 	}
 
 	protected static IModule[] getModules(WorkbenchComponent component) {
 		ComponentType type = component.getComponentType();
 		IProject project = StructureEdit.getContainingProject(component);
 		if (type == null || project == null || type.getComponentTypeId() == null)
 			return null;
 		IModule[] modules = ServerUtil.getModules(project);
 		return modules;
 	}
 	
 	protected static IModule[] getModules(IVirtualComponent component) {
 		IProject project = component.getProject();
 		if (project == null || component.getComponentTypeId() == null)
 			return null;
 		IModule[] modules = ServerUtil.getModules(project);
 		return modules;
 	}
 
 
 	public static String convertJSTVersions(String id) {
 
 		if (id.equals(JST_EJB_ID))
 			return J2EE_EJB_ID;
 
 		if (id.equals(JST_WEB_ID))
 			return J2EE_WEB_ID;
 
 		if (id.equals(JST_EAR_ID))
 			return J2EE_EAR_ID;
 
 
 		return "";
 	}
 
 }
