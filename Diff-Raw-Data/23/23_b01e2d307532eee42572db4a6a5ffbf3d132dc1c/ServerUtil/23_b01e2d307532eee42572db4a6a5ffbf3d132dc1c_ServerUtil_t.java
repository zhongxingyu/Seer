 /**********************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core;
 
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.ILaunchConfiguration;
 
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.internal.*;
 /**
  * Server utility methods. These static methods can be used to perform
  * common operations on server artifacts.
  * 
  * @since 1.0
  */
 public class ServerUtil {
 	/**
 	 * Static utility class - cannot create an instance.
 	 */
 	private ServerUtil() {
 		// do nothing
 	}
 
 	/**
 	 * Returns the module contained within the given project. If more than one module
	 * is contained with the project, this method will return an arbitrary module
	 * unless the module factory defines an ordering. If there might be multiple
	 * modules in a project, users should typically use getModules(IProject) instead.
 	 * 
 	 * @param project a project
 	 * @return a module that is contained with the project, or null if no
 	 *    modules are contained in the given project
 	 * @see #getModules(IProject)
 	 */
 	public static IModule getModule(IProject project) {
 		if (project == null)
 			throw new IllegalArgumentException();
 		
 		IModule[] modules = getModules();
 		if (modules != null) {
 			int size = modules.length;
 			for (int i = 0; i < size; i++) {
 				if (modules[i] != null && project.equals(modules[i].getProject()))
 					return modules[i];
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the modules contained within the given project.
 	 * 
 	 * @param project a project
 	 * @return a possibly-empty array of modules
 	 * @see #getModule(IProject)
 	 */
 	public static IModule[] getModules(IProject project) {
 		if (project == null)
 			throw new IllegalArgumentException();
 		
 		IModule[] modules = getModules();
 		List list = new ArrayList();
 		if (modules != null) {
 			int size = modules.length;
 			for (int i = 0; i < size; i++) {
 				if (modules[i] != null && project.equals(modules[i].getProject()))
 					list.add(modules[i]);
 			}
 		}
 		IModule[] modules2 = new IModule[list.size()];
 		list.toArray(modules2);
 		return modules2;
 	}
 
 	/**
 	 * Returns a module from the given moduleId. The moduleId must not be null.
 	 * 
 	 * @param moduleId a module id
 	 * @return the module, or <code>null</code> if the module could not be found
 	 */
 	public static IModule getModule(String moduleId) {
 		if (moduleId == null)
 			throw new IllegalArgumentException();
 		
 		int index = moduleId.indexOf(":");
 		if (index <= 0)
 			return null;
 		
 		String factoryId = moduleId.substring(0, index);
 		ModuleFactory moduleFactory = ServerPlugin.findModuleFactory(factoryId);
 		if (moduleFactory == null)
 			return null;
 
 		String moduleSubId = moduleId.substring(index+1);
 		IModule module = moduleFactory.getModule(moduleSubId);
 		if (module != null)
 			return module;
 		return null;
 	}
 
 	/**
 	 * Return all the available modules from all factories whose
 	 * type matches the given module types.
 	 * 
 	 * @param moduleTypes an array of module types
 	 * @return a possibly empty array of modules
 	 */
 	public static IModule[] getModules(IModuleType[] moduleTypes) {
 		List list = new ArrayList();
 
 		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(factories[i].getModuleTypes(), moduleTypes)) {
 					IModule[] modules = factories[i].getModules();
 					if (modules != null) {
 						int size2 = modules.length;
 						for (int j = 0; j < size2; j++)
 							list.add(modules[j]);
 					}
 				}
 			}
 		}
 		IModule[] modules = new IModule[list.size()];
 		list.toArray(modules);
 		return modules;
 	}
 
 	/**
 	 * Return all the available modules from all factories whose
 	 * type matches the given module type id.
 	 * 
 	 * @param type a module type
 	 * @return a possibly empty array of modules
 	 */
 	public static IModule[] getModules(String type) {
 		List list = new ArrayList();
 
 		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(factories[i].getModuleTypes(), type, null)) {
 					IModule[] modules = factories[i].getModules();
 					if (modules != null) {
 						int size2 = modules.length;
 						for (int j = 0; j < size2; j++)
 							if(type.equals(modules[j].getModuleType().getId()))
 								list.add(modules[j]);
 					}
 				}
 			}
 		}
 		IModule[] modules = new IModule[list.size()];
 		list.toArray(modules);
 		return modules;
 	}
 	
 	/**
 	 * Returns <code>true</code> if any of the given moduleTypes have the given
 	 * module type id and version id.
 	 * 
 	 * @param moduleTypes an array of module types
 	 * @param typeId a module type
 	 * @param versionId a module version
 	 * @return <code>true</code> if the module type is supported, and
 	 *    <code>false</code> otherwise
 	 */
 	public static boolean isSupportedModule(IModuleType[] moduleTypes, String typeId, String versionId) {
 		if (moduleTypes != null) {
 			int size = moduleTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(moduleTypes[i], typeId, versionId))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	private static boolean isSupportedModule(IModuleType[] moduleTypes, IModuleType[] mt) {
 		if (mt != null) {
 			int size = mt.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(moduleTypes, mt[i]))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns <code>true</code> if any of the given moduleTypes match the given
 	 * module type.
 	 * 
 	 * @param moduleTypes an array of modules types
 	 * @param mt a module type
 	 * @return <code>true</code> if the module type is supported, and
 	 *    <code>false</code> otherwise
 	 */
 	public static boolean isSupportedModule(IModuleType[] moduleTypes, IModuleType mt) {
 		if (moduleTypes != null) {
 			int size = moduleTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(moduleTypes[i], mt))
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	private static boolean isSupportedModule(IModuleType moduleType, String type, String version) {
 		String type2 = moduleType.getId();
 		if (matches(type, type2)) {
 			String version2 = moduleType.getVersion();
 			if (matches(version, version2))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the two given module types are compatible. The moduleTypes may not
 	 * be null.
 	 * 
 	 * @param moduleType a module type
 	 * @param mt a module type
 	 * @return <code>true</code> if the module type is supported, and
 	 *    <code>false</code> otherwise
 	 */
 	public static boolean isSupportedModule(IModuleType moduleType, IModuleType mt) {
 		if (moduleType == null || mt == null)
 			throw new IllegalArgumentException();
 		
 		String type2 = moduleType.getId();
 		if (matches(mt.getId(), type2)) {
 			String version2 = moduleType.getVersion();
 			if (matches(mt.getVersion(), version2))
 				return true;
 		}
 		return false;
 	}
 
 	private static boolean matches(String a, String b) {
 		if (a == null || b == null || "*".equals(a) || "*".equals(b) || a.startsWith(b) || b.startsWith(a)
 			|| (a.endsWith(".*") && b.startsWith(a.substring(0, a.length() - 1)))
 			|| (b.endsWith(".*") && a.startsWith(b.substring(0, b.length() - 1))))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Return all the available modules from all factories.
 	 * 
 	 * @return a possibly empty array of modules
 	 */
 	private static IModule[] getModules() {
 		List list = new ArrayList();
 		
 		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				IModule[] modules = factories[i].getModules();
 				if (modules != null) {
 					int size2 = modules.length;
 					for (int j = 0; j < size2; j++) {
 						if (!list.contains(modules[j])) {
 							if (isSupportedModule(factories[i].getModuleTypes(), modules[j].getModuleType()))
 								list.add(modules[j]);
 							else
 								Trace.trace(Trace.WARNING, "Invalid module returned from factory, ignored: " + modules[j]);
 						}
 					}
 				}
 			}
 		}
 		IModule[] modules = new IModule[list.size()];
 		list.toArray(modules);
 		return modules;
 	}
 
 	/**
 	 * Adds or removes modules from a server. Will search for the first parent module
 	 * of each module and add it to the server instead. This method will handle multiple
 	 * modules having the same parent (the parent will only be added once), but may not
 	 * handle the case where the same module or parent is being both added and removed.
 	 * 
 	 * @param server a server
 	 * @param add an array of modules to add, or <code>null</code> to not add any
 	 * @param remove an array of modules to remove, or <code>null</code> to not remove any
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @throws CoreException if anything goes wrong
 	 */
 	public static void modifyModules(IServerWorkingCopy server, IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
 		if (server == null)
 			throw new IllegalArgumentException();
 		
 		if (add == null)
 			add = new IModule[0];
 		if (remove == null)
 			remove = new IModule[0];
 		
 		int size = add.length;
 		List addParentModules = new ArrayList();
 		for (int i = 0; i < size; i++) {
 			boolean found = false;
 			try {
 				IModule[] parents = server.getRootModules(add[i], monitor);
 				if (parents != null && parents.length > 0) {				
 					Object parent = parents[0];
 					found = true;
 					if (!addParentModules.contains(parent))
 						addParentModules.add(parent);
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Could not find parent module", e);
 			}
 			
 			if (!found)
 				addParentModules.add(add[i]);
 		}
 		
 		size = remove.length;
 		List removeParentModules = new ArrayList();
 		for (int i = 0; i < size; i++) {
 			boolean found = false;
 			try {
 				IModule[] parents = server.getRootModules(remove[i], monitor);
 				if (parents != null && parents.length > 0) {				
 					Object parent = parents[0];
 					found = true;
 					if (!removeParentModules.contains(parent))
 						removeParentModules.add(parent);
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Could not find parent module 2", e);
 			}
 			
 			if (!found)
 				removeParentModules.add(remove[i]);
 		}
 		
 		IModule[] add2 = new IModule[addParentModules.size()];
 		addParentModules.toArray(add2);
 		IModule[] remove2 = new IModule[removeParentModules.size()];
 		removeParentModules.toArray(remove2);
 		
 		server.modifyModules(add2, remove2, monitor);
 	}
 
 	/**
 	 * Sets a default name on the given runtime.
 	 * 
 	 * @param runtime a runtime
 	 */
 	public static void setRuntimeDefaultName(IRuntimeWorkingCopy runtime) {
 		String typeName = runtime.getRuntimeType().getName();
 		
 		String name = NLS.bind(Messages.defaultRuntimeName, typeName);
 		int i = 2;
 		while (isNameInUse(name)) {
 			name = NLS.bind(Messages.defaultRuntimeName2, new String[] {typeName, i + ""});
 			i++;
 		}
 		runtime.setName(name);
 	}
 
 	/**
 	 * Sets a default name on the given server.
 	 * 
 	 * @param server a server
 	 */
 	public static void setServerDefaultName(IServerWorkingCopy server) {
 		if (server == null)
 			throw new IllegalArgumentException();
 		
 		String typeName = server.getServerType().getName();
 		String host = server.getHost();
 		
 		String name = NLS.bind(Messages.defaultServerName, new String[] {typeName, host});
 		int i = 2;
 		while (isNameInUse(name)) {
 			name = NLS.bind(Messages.defaultServerName2, new String[] {typeName, host, i + ""});
 			i++;
 		}
 		server.setName(name);
 	}
 
 	private static boolean isValidFilename(String name) {
 		IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
 		if (status != null && !status.isOK())
 			return false;
 		
 		status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FOLDER);
 		if (status != null && !status.isOK())
 			return false;
 		
 		return true;
 	}
 
 	private static String getValidFileName(String name) {
 		if (isValidFilename(name))
 			return name;
 	
 		// remove invalid characters
 		String[] s = new String[] {".", "\\", "/", "?", ":", "*", "\"", "|", "<", ">"};
 		int ind = 0;
 		while (ind < s.length) {
 			int index = name.indexOf(s[ind]);
 			while (index >= 0) {
 				name = name.substring(0, index) + name.substring(index+1);
 				index = name.indexOf(s[ind]);
 			}
 			ind++;
 		}
 		return name;
 	}
 
 	/**
 	 * Returns an unused file in the given project.
 	 * 
 	 * @param project a project
 	 * @param type a server type
 	 * @return an unused file within the given project
 	 */
 	/*public static IFile getUnusedServerFile(IProject project, IServerType type) {
 		if (project == null || type == null)
 			throw new IllegalArgumentException();
 		
 		String typeName = getValidFileName(type.getName());
 		String name = NLS.bind(Messages.defaultServerName3, typeName)+ "."  + Server.FILE_EXTENSION;
 		int i = 2;
 		while (isFileNameInUse(project, name)) {
 			name = NLS.bind(Messages.defaultServerName4, new String[] {typeName, i + ""}) + "."  + Server.FILE_EXTENSION;
 			i++;
 		}
 		return project.getFile(name);
 	}*/
 
 	/**
 	 * Returns an unused file in the given project.
 	 * 
 	 * @param project a project
 	 * @param server a server
 	 * @return an unused file within the given project
 	 */
 	public static IFile getUnusedServerFile(IProject project, IServer server) {
 		if (project == null || server == null)
 			throw new IllegalArgumentException();
 		
 		String typeName = getValidFileName(server.getName());
 		String name = NLS.bind(Messages.defaultServerName3, typeName)+ "."  + Server.FILE_EXTENSION;
 		int i = 2;
 		while (isFileNameInUse(project, name)) {
 			name = NLS.bind(Messages.defaultServerName4, new String[] {typeName, i + ""}) + "."  + Server.FILE_EXTENSION;
 			i++;
 		}
 		return project.getFile(name);
 	}
 
 	/**
 	 * Returns true if a server or runtime exists with the given name.
 	 *
 	 * @param name a name
 	 * @return <code>true</code> if the name is in use, and <code>false</code>
 	 *    otherwise
 	 */
 	private static boolean isNameInUse(String name) {
 		if (name == null)
 			return true;
 	
 		List list = new ArrayList();
 		
 		addAll(list, ServerCore.getRuntimes());
 		addAll(list, ServerCore.getServers());
 
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext()) {
 			Object obj = iterator.next();
 			if (obj instanceof IServerAttributes && name.equalsIgnoreCase(((IServerAttributes)obj).getName()))
 				return true;
 			if (obj instanceof IRuntime && name.equalsIgnoreCase(((IRuntime)obj).getName()))
 				return true;
 		}
 
 		return false;
 	}
 	
 	private static void addAll(List list, Object[] obj) {
 		if (obj == null)
 			return;
 		
 		int size = obj.length;
 		for (int i = 0; i < size; i++) {
 			list.add(obj[i]);
 		}
 	}
 	
 	/**
 	 * Returns true if an element exists with the given name.
 	 *
 	 * @param project a project
 	 * @param name a file or folder name
 	 * @return boolean <code>true</code> if the file or folder name is being
 	 *    used, and <code>false</code> otherwise
 	 */
 	private static boolean isFileNameInUse(IProject project, String name) {
 		if (name == null || project == null)
 			return false;
 		
 		if (project.getFile(name).exists())
 			return true;
 		if (project.getFolder(name).exists())
 			return true;
 	
 		return false;
 	}
 
 	/**
 	 * Return a list of all runtime targets that match the given type and version.
 	 * If type or version is null, it matches all of that type or version.
 	 * 
 	 * @param type a module type
 	 * @param version a module version
 	 * @return a possibly-empty array of runtime instances {@link IRuntime}
 	 */
 	public static IRuntime[] getRuntimes(String type, String version) {
 		List list = new ArrayList();
 		IRuntime[] runtimes = ServerCore.getRuntimes();
 		if (runtimes != null) {
 			int size = runtimes.length;
 			for (int i = 0; i < size; i++) {
 				IRuntimeType runtimeType = runtimes[i].getRuntimeType();
 				if (runtimeType != null && isSupportedModule(runtimeType.getModuleTypes(), type, version)) {
 					list.add(runtimes[i]);
 				}
 			}
 		}
 		
 		IRuntime[] runtimes2 = new IRuntime[list.size()];
 		list.toArray(runtimes2);
 		return runtimes2;
 	}
 
 	/**
 	 * Return a list of all runtime types that match the given type and version.
 	 * If type or version is null, it matches all of that type or version.
 	 * 
 	 * @param type a module type
 	 * @param version a module version
 	 * @return a possibly-empty array of runtime type instances {@link IRuntimeType}
 	 */
 	public static IRuntimeType[] getRuntimeTypes(String type, String version) {
 		List list = new ArrayList();
 		IRuntimeType[] runtimeTypes = ServerCore.getRuntimeTypes();
 		if (runtimeTypes != null) {
 			int size = runtimeTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(runtimeTypes[i].getModuleTypes(), type, version)) {
 					list.add(runtimeTypes[i]);
 				}
 			}
 		}
 		
 		IRuntimeType[] rt = new IRuntimeType[list.size()];
 		list.toArray(rt);
 		return rt;
 	}
 	
 	/**
 	 * Return a list of all runtime types that match the given type, version,
 	 * and partial runtime type id. If type, version, or runtimeTypeId is null,
 	 * it matches all of that type or version.
 	 * 
 	 * @param type a module type
 	 * @param version a module version
 	 * @param runtimeTypeId the id of a runtime type
 	 * @return a possibly-empty array of runtime type instances {@link IRuntimeType}
 	 */
 	public static IRuntimeType[] getRuntimeTypes(String type, String version, String runtimeTypeId) {
 		List list = new ArrayList();
 		IRuntimeType[] runtimeTypes = ServerCore.getRuntimeTypes();
 		if (runtimeTypes != null) {
 			int size = runtimeTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(runtimeTypes[i].getModuleTypes(), type, version)) {
 					if (runtimeTypeId == null || runtimeTypes[i].getId().startsWith(runtimeTypeId))
 						list.add(runtimeTypes[i]);
 				}
 			}
 		}
 		
 		IRuntimeType[] rt = new IRuntimeType[list.size()];
 		list.toArray(rt);
 		return rt;
 	}
 
 	/**
 	 * Returns a list of all servers that this deployable is not currently
 	 * configured on, but could be added to. If includeErrors is true, this
 	 * method return servers where the parent deployable may throw errors. For
 	 * instance, this deployable may be the wrong spec level.
 	 *
 	 * @param module a module
 	 * @param includeErrors <code>true</code> to include servers that returned
 	 *    errors when trying to add the module, and <code>false</code> otherwise
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return a possibly empty array of servers
 	 */
 	public static IServer[] getAvailableServersForModule(IModule module, boolean includeErrors, IProgressMonitor monitor) {
 		if (module == null)
 			return new IServer[0];
 
 		// do it the slow way - go through all servers and
 		// see if this deployable is not configured in it
 		// but could be added
 		List list = new ArrayList();
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (!containsModule(servers[i], module, monitor)) {
 					try {
 						IModule[] parents = servers[i].getRootModules(module, monitor);
 						if (parents != null && parents.length > 0) {
 							boolean found = false;
 							if (parents != null) {
 								int size2 = parents.length;
 								for (int j = 0; !found && j < size2; j++) {
 									IModule parent = parents[j];
 									IStatus status = servers[i].canModifyModules(new IModule[] { parent }, new IModule[0], monitor);
 									if (status == null || status.isOK()){
 										list.add(servers[i]);
 										found = true;
 									}
 								}
 							}
 						}
 					} catch (Exception se) {
 						if (includeErrors)
 							list.add(servers[i]);
 					}
 				}
 			}
 		}
 		
 		// make sure that the preferred server is the first one
 		//IServer server = ServerCore.getServerPreferences().getDeployableServerPreference(deployable);
 		//if (server != null && list.contains(server) && list.indexOf(server) != 0) {
 		//	list.remove(server);
 		//	list.add(0, server);
 		//}
 
 		IServer[] allServers = new IServer[list.size()];
 		list.toArray(allServers);
 		return allServers;
 	}
 
 	/**
 	 * Returns a list of all servers that this module is configured on.
 	 * 
 	 * @param module a module
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return a possibly-empty array of server instances {@link IServer}
 	 */
 	public static IServer[] getServersByModule(IModule module, IProgressMonitor monitor) {
 		if (module == null)
 			return new IServer[0];
 
 		// do it the slow way - go through all servers and
 		// see if this module is configured in it
 		List list = new ArrayList();
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (containsModule(servers[i], module, monitor))
 					list.add(servers[i]);
 			}
 		}
 		
 		IServer[] allServers = new IServer[list.size()];
 		list.toArray(allServers);
 		return allServers;
 	}
 
 	/**
 	 * Returns true if the given server currently contains the given module.
 	 *
 	 * @param server a server
 	 * @param module a module
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return boolean <code>true</code> if the module is contained on the server,
 	 *    or <code>false</code> otherwise
 	 */
 	public static boolean containsModule(IServer server, final IModule module, IProgressMonitor monitor) {
 		if (server == null)
 			return false;
 		Trace.trace(Trace.FINEST, "containsModule() " + server + " " + module);
 		
 		final boolean[] b = new boolean[1];
 		
 		((Server)server).visit(new IModuleVisitor() {
 			public boolean visit(IModule[] modules) {
 				int size = modules.length;
 				if (modules[size - 1].equals(module)) {
 					b[0] = true;
 					return false;
 				}
 				return true;
 			}
 		}, null);
 		return b[0];
 	}
 
 	/**
 	 * Returns the server associated with the given launch configuration.
 	 * 
 	 * @param configuration a launch configuration
 	 * @return the server associated with the launch configuration, or
 	 *    <code>null</code> if no server could be found
 	 * @throws CoreException if there is a problem getting the attribute from
 	 *    the launch configuration
 	 */
 	public static IServer getServer(ILaunchConfiguration configuration) throws CoreException {
 		String serverId = configuration.getAttribute(Server.ATTR_SERVER_ID, (String) null);
 
 		if (serverId != null)
 			return ServerCore.findServer(serverId);
 		return null;
 	}
 
 	/**
 	 * Validates whether this server can be editted.
 	 * 
 	 * @param context the context (Shell)
 	 * @param server the server
 	 * @return a status object with code <code>IStatus.OK</code> if the server
 	 *   can be edited, otherwise a status object indicating what when wrong
 	 *   with the checkout
 	 */
 	public static IStatus validateEdit(Object context, IServer server) {
 		return ((Server)server).validateEdit(context);
 	}
 
 	/**
 	 * Returns the port that is being used to monitor the given port on the server.
 	 * This method can be used whenever creating a 'client' for the server, and allows
 	 * the client to seamlessly use a monitored port instead of going directly to the
 	 * server.
 	 * 
 	 * <b>Provisional API:</b> This class/interface is part of an interim API that is still under development and expected to 
 	 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 	 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 	 * (repeatedly) as the API evolves.
 	 * </p>
 	 * 
 	 * @param server a server
 	 * @param port a port on the server
 	 * @param contentType the content type, e.g. "web"
 	 * @return the monitored port, or the original port number if the port is not
 	 *    currently being monitored
 	 */
 	public static int getMonitoredPort(IServer server, int port, String contentType) {
 		return ServerMonitorManager.getInstance().getMonitoredPort(server, port, contentType);
 	}
 }
