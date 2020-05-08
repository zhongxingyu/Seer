 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.componentcore.JavaEEBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.Module;
 import org.eclipse.wst.server.core.internal.ModuleFactory;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
 import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;
 
 /**
  * J2EE module factory.
  */
 public class J2EEDeployableFactory extends ProjectModuleFactoryDelegate {
 	protected Map <IModule, ModuleDelegate> moduleDelegates = new HashMap<IModule, ModuleDelegate>(5);
 
 	public static final String J2EE_ID = "org.eclipse.jst.j2ee.server"; //$NON-NLS-1$
 	public static final String BINARY_PREFIX = "/binary:"; //$NON-NLS-1$
 	
 	public static J2EEDeployableFactory J2EE_INSTANCE;
 	public static J2EEDeployableFactory j2eeInstance() {
 		if( J2EE_INSTANCE == null ) {
 			ensureFactoryLoaded(J2EE_ID);
 		}
 		return J2EE_INSTANCE;
 	}
 	
 	public static void ensureFactoryLoaded(String factoryId) {
         ModuleFactory[] factories = ServerPlugin.getModuleFactories();
         for( int i = 0; i < factories.length; i++ ) {
                 if( factories[i].getId().equals(factoryId)) {
                         factories[i].getDelegate(new NullProgressMonitor());
                 }
         }
 	}
 	
 	public J2EEDeployableFactory() {
 		super();
 		J2EE_INSTANCE = this;
 	}
 	
 	@Override
 	protected IModule[] createModules(IProject project) {
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if(component != null)
 			return createModuleDelegates(component);
 		return null;
 	}
 
 
 	@Override
 	public ModuleDelegate getModuleDelegate(IModule module) {
 		if (module == null)
 			return null;
 
 		ModuleDelegate md = moduleDelegates.get(module);
 		if( md == null && ((Module)module).getInternalId().startsWith(BINARY_PREFIX))
 			return createDelegate(module);
 		
 		if (md == null) {
 			createModules(module.getProject());
 			md = moduleDelegates.get(module);
 		}
 		return md;
 	}
 
 	protected boolean canHandleProject(IProject p) {
 		return J2EEProjectUtilities.isLegacyJ2EEProject(p);
 	}
 	
 	protected IModule[] createModuleDelegates(IVirtualComponent component) {
 		if(component == null){
 			return null;
 		}
 		
 		List<IModule> projectModules = new ArrayList<IModule>();
 		try {
 			if (canHandleProject(component.getProject())) {
 				String type = JavaEEProjectUtilities.getJ2EEProjectType(component.getProject());
 				String version = J2EEProjectUtilities.getJ2EEProjectVersion(component.getProject());
				IModule module = createModule(component.getName(), component.getDeployedName(), type, version, component.getProject());
 				J2EEFlexProjDeployable moduleDelegate = new J2EEFlexProjDeployable(component.getProject(), component);
 				moduleDelegates.put(module, moduleDelegate);
 				projectModules.add(module);
 
 				// Check to add any binary modules
 				if (J2EEProjectUtilities.ENTERPRISE_APPLICATION.equals(type))
 					projectModules.addAll(LEGACY_createBinaryModules(component));
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			J2EEPlugin.logError(e);
 		}
 		return projectModules.toArray(new IModule[projectModules.size()]);
 	}
 
 	/**
 	 * These are here so that previous modules are able to be found 
 	 * 
 	 * @param component
 	 * @return
 	 */
 	protected List<IModule> LEGACY_createBinaryModules(IVirtualComponent component) {
 		List<IModule> projectModules = new ArrayList<IModule>();
 		IVirtualReference[] references = component.getReferences();
 		for (int i = 0; i < references.length; i++) {
 			IVirtualComponent moduleComponent = references[i].getReferencedComponent();
 			if( moduleComponent.isBinary()) {
 				JavaEEQuickPeek quickPeek = JavaEEBinaryComponentHelper.getJavaEEQuickPeek(moduleComponent);
 				IModule nestedModule = createModule(quickPeek,moduleComponent.getName(),
 						moduleComponent.getDeployedName(), moduleComponent.getProject());
 				if (nestedModule != null) {
 					ModuleDelegate moduleDelegate = getNestedDelegate(moduleComponent);
 					moduleDelegates.put(nestedModule, moduleDelegate);
 					projectModules.add(nestedModule);
 				}
 			}
 		}
 
 		return projectModules;
 	}
 	
 	protected ModuleDelegate getNestedDelegate(IVirtualComponent component) {
 		return new J2EEFlexProjDeployable(component.getProject(), component);
 	}
 
 	protected IModule createModule(JavaEEQuickPeek quickPeek, String id, String name, IProject project) {
 		if (quickPeek.getType() == JavaEEQuickPeek.UNKNOWN) {
 			return null;
 		}
 
 		String moduleType = null;
 		String moduleVersion = null;
 
 		switch (quickPeek.getType()) {
 		case JavaEEQuickPeek.APPLICATION_CLIENT_TYPE:
 			moduleType = J2EEProjectUtilities.APPLICATION_CLIENT;
 			break;
 		case JavaEEQuickPeek.WEB_TYPE:
 			moduleType = JavaEEProjectUtilities.DYNAMIC_WEB;
 			break;
 		case JavaEEQuickPeek.EJB_TYPE:
 			moduleType = JavaEEProjectUtilities.EJB;
 			break;
 		case JavaEEQuickPeek.CONNECTOR_TYPE:
 			moduleType = JavaEEProjectUtilities.JCA;
 			break;
 		case JavaEEQuickPeek.APPLICATION_TYPE:
 			moduleType = JavaEEProjectUtilities.ENTERPRISE_APPLICATION;
 			break;
 		default:
 			moduleType = JavaEEProjectUtilities.UTILITY;
 			moduleVersion = J2EEVersionConstants.VERSION_1_0_TEXT;
 		}
 
 		int version = quickPeek.getVersion();
 		moduleVersion = J2EEVersionUtil.convertVersionIntToString(version);
 
 		IModule nestedModule = createModule(id, name, moduleType, moduleVersion, project);
 		return nestedModule;
 	}
 	
 	/**
 	 * Returns the list of resources that the module should listen to for state
 	 * changes. The paths should be project relative paths. Subclasses can
 	 * override this method to provide the paths.
 	 * 
 	 * @return a possibly empty array of paths
 	 */
 	@Override
 	protected IPath[] getListenerPaths() {
 		return new IPath[] { new Path(".project"), // nature //$NON-NLS-1$
 				new Path(StructureEdit.MODULE_META_FILE_NAME), // component
 				new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets //$NON-NLS-1$
 		};
 	}
 
 	@Override
 	protected void clearCache(IProject project) {
 		super.clearCache(project);
 		List<IModule> modulesToRemove = null;
 		for (Iterator<IModule> iterator = moduleDelegates.keySet().iterator(); iterator.hasNext();) {
 			IModule module = iterator.next();
 			if (module.getProject().equals(project)) {
 				if (modulesToRemove == null) {
 					modulesToRemove = new ArrayList<IModule>();
 				}
 				modulesToRemove.add(module);
 			}
 		}
 		if (modulesToRemove != null) {
 			for (IModule module : modulesToRemove) {
 				moduleDelegates.remove(module);
 			}
 		}
 	}
 	
 	/**
 	 * From this point on, when queried, projects will generate their binary 
 	 * child modules on the fly and they will be small and dumb
 	 * 
 	 * @param parent
 	 * @param child
 	 * @return
 	 */
 	public IModule createChildModule(FlatComponentDeployable parent, IChildModuleReference child) {
 		File file = child.getFile();
 		if( file != null ) {
 			IPath p = new Path(file.getAbsolutePath());
 			JavaEEQuickPeek qp = JavaEEBinaryComponentHelper.getJavaEEQuickPeek(p);
 			IModule module = createModule(qp, BINARY_PREFIX + file.getAbsolutePath(), file.getName(), parent.getProject());
 			ModuleDelegate moduleDelegate = getNestedDelegate(child.getComponent());
 			moduleDelegates.put(module, moduleDelegate);
 			return module;
 		}
 		return null;
 	}
 	
 	/**
 	 * Create a module delegate on the fly for this binary file
 	 * @param module
 	 * @return
 	 */
 	public ModuleDelegate createDelegate(IModule module) {
 		String internalId = ((Module)module).getInternalId();
 		String path = internalId.substring(BINARY_PREFIX.length());
 		File f = new File(path);
 		return new BinaryFileModuleDelegate(f);
 	}
 }
