 /**
  * JBoss, a Division of Red Hat
  * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
 * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.ide.eclipse.as.core.modules;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.wst.server.core.IModule;
 import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
 import org.jboss.ide.eclipse.archives.core.model.ArchivesModelCore;
 import org.jboss.ide.eclipse.archives.core.model.IArchive;
 import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.IModuleContributor;
 import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.PackagedModuleDelegate;
 
 /**
  * 
  * @author Rob Stryker rob.stryker@jboss.com
  *
  */
 public class ArchivesModelModuleContributor implements IModuleContributor {
 
 	private static ArchivesModelModuleContributor instance;
 	public static ArchivesModelModuleContributor getInstance() {
 		if( instance == null ) {
 			instance = new ArchivesModelModuleContributor(PackageModuleFactory.getFactory());
 		}
 		return instance;
 	}
 	
 	private PackageModuleFactory factory;
 	protected ArrayList<IModule> modules = null;
 	protected HashMap<IPath, ArrayList<IModule>> projectToModules = new HashMap<IPath, ArrayList<IModule>>(5); //IPath to IModule
 	protected HashMap<IModule, Object> moduleDelegates = new HashMap<IModule, Object>(5);
 	protected HashMap<IArchive, IModule> packageToModule = new HashMap<IArchive, IModule>(5);	
 	
 	private ArchivesModelModuleContributor(PackageModuleFactory factory) {
 		this.factory = factory;
 	}
 	
 	public IModule[] getModules() {
 		if( modules == null ) {
 			modules = new ArrayList<IModule>();
 			IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 			int size = projects2.length;
 			for (int i = 0; i < size; i++) {
 				if (projects2[i].isAccessible()) {
 					createModules(projects2[i]);
 				}
 			}
 		}
 		return modules.toArray(new IModule[modules.size()]);
 	}
 	
 	protected void createModules(IProject project) {
 		IArchive[] packs = ArchivesModelCore.getProjectPackages(project.getLocation(), null, false);
 		if( packs != null && packs.length > 0 ) {
 			IModule module;
 			IArchive[] packages = ArchivesModelCore.getProjectPackages(project.getLocation(), new NullProgressMonitor(), true);
			boolean requiresSave = ensureArchivesHaveIDs(project, packages);
 			ArrayList<IModule> mods = new ArrayList<IModule>();
 			for( int i = 0; i < packages.length; i++ ) {
 				module = factory.createModule2(packages[i], project);
 				modules.add(module);
 				Object moduleDelegate = new PackagedModuleDelegate(packages[i]);
 				packageToModule.put(packages[i], module);
 				moduleDelegates.put(module, moduleDelegate);
 				mods.add(module);
 			}
 			projectToModules.put(project.getLocation(), mods);
			if( requiresSave )
				ArchivesModel.instance().saveModel(project.getLocation(), 
						new NullProgressMonitor());
 		}
 	}
 	
 	public boolean containsModule(IModule module) {
 		return moduleDelegates.containsKey(module);
 	}
 
 	public PackagedModuleDelegate getModuleDelegate(IModule module) {
 		return (PackagedModuleDelegate)moduleDelegates.get(module);
 	}
 	
 	public void refreshProject(IPath projectLoc) {
 		// prime, make sure all are found
 		if( modules == null ) 
 			getModules();
 		
 		// remove old mods
 		ArrayList<IModule> mods = projectToModules.get(projectLoc);
 		IModule mod;
		PackagedModuleDelegate delegate;
 		if (mods != null) {
 			for( Iterator<IModule> i = mods.iterator(); i.hasNext();) {
 				mod = (IModule)i.next();
				if( modules.contains(mod)) {
					delegate = ((PackagedModuleDelegate)moduleDelegates.get(mod));
 					moduleDelegates.remove(mod);
 					modules.remove(mod);
					if( delegate != null ) 
						packageToModule.remove(delegate.getPackage());
 				}
 			}
 		}
 		createModules(findProject(projectLoc));
 	}
 
 	protected IProject findProject(IPath projectLoc) {
 		IProject proj = null;
 		IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		int size = projects2.length;
 		for (int i = 0; i < size; i++) {
 			if( projects2[i].getLocation().equals(projectLoc))
 				proj = projects2[i];
 		}
 		return proj;
 	}
 	
 	/**
 	 * Set a property so that each module that's here in the factory
 	 * has a unique ID other than it's name (which is not unique)
 	 * @param archives
 	 * @return  returns whether a save has occurred
 	 */
 	protected boolean ensureArchivesHaveIDs(IProject project, IArchive[] archives) {
 		boolean requiresSave = false;
 		for( int i = 0; i < archives.length; i++ ) {
 			if( PackageModuleFactory.getID(archives[i]) == null ) {
 				requiresSave = true;
 				archives[i].setProperty(PackageModuleFactory.MODULE_ID_PROPERTY_KEY, 
 										PackageModuleFactory.getID(archives[i], true));
 			}
 		}
 		return requiresSave;
 	}
 }
