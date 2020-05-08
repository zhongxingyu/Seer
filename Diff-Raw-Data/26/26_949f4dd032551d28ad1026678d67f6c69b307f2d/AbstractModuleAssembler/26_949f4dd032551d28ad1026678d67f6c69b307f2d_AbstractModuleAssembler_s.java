 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.core.internal.publishers;
 			
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.PublishUtil;
 import org.eclipse.jst.server.generic.core.internal.CorePlugin;
 import org.eclipse.jst.server.generic.core.internal.GenericServer;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.model.IModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.util.ProjectModule;
 
 /**
  * Base class for module assemblers
  * 
  * @author Gorkem Ercan
  */
 public abstract class AbstractModuleAssembler {
 
 	protected ServerRuntime fServerdefinition;
 	protected IModule fModule; 
 	protected GenericServer fServer;
 	protected IPath fAssembleRoot;
 	
 	protected AbstractModuleAssembler(IModule module, GenericServer server, IPath assembleRoot)
 	{
 		fModule=module;
 		fServerdefinition=server.getServerDefinition();
 		fServer=server;
 		fAssembleRoot = assembleRoot;
 	}
 	
 	/**
 	 * Assemble the module.
 	 * 
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	public abstract IPath assemble(IProgressMonitor monitor) throws CoreException;
 
 	
 	/**
 	 * Factory for creating concrete module assemblers for 
 	 * corresponding module types.
 	 *
 	 */
 	public static class Factory {		
 		
 		public static IPath getDefaultAssembleRoot(IModule module, GenericServer server) {
 			ProjectModule pm =(ProjectModule)module.loadAdapter(ProjectModule.class, new NullProgressMonitor());
 			return ServerPlugin.getInstance().getTempDirectory(server.getServer().getId()).append(pm.getId());
 		}
 		
 		/**
 		 * Returns a concrete module assembler
 		 * 
 		 * @param module
 		 * @param server
 		 * @return assembler
 		 */
 		public static AbstractModuleAssembler getModuleAssembler(IModule module, GenericServer server)
 		{
 			return getModuleAssembler(module, server, getDefaultAssembleRoot(module, server));
 		}
 		
 		/**
 		 * Returns a concrete module assembler that assembles under the specified root path
 		 * 
 		 * @param module
 		 * @param server
 		 * @param assembleRoot
 		 * @return assembler
 		 */
 		public static AbstractModuleAssembler getModuleAssembler(IModule module, GenericServer server, IPath assembleRoot)
 		{
 			if(isModuleType(module, "jst.web")) //$NON-NLS-1$
 				return new WarModuleAssembler(module,server,assembleRoot);
 			if(isModuleType(module, "jst.ear")) //$NON-NLS-1$
 				return new EarModuleAssembler(module,server,assembleRoot);
 			return new DefaultModuleAssembler(module,server,assembleRoot);
 		}
 		
 		private static boolean isModuleType(IModule module, String moduleTypeId){	
 			if(module.getModuleType()!=null && moduleTypeId.equals(module.getModuleType().getId()))
 				return true;
 			return false;
 		}
 	}
 	
 	protected void packModule(IModule module, String deploymentUnitName, IPath destination)throws CoreException {
 		
 	
 		String dest = destination.append(deploymentUnitName).toString();
 		ModulePackager packager = null;
 		try {
 			packager = new ModulePackager(dest, false);
 			ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
 			IModuleResource[] resources = pm.members();
 			for (int i = 0; i < resources.length; i++) {
 				doPackModule(resources[i], packager);
 			}
 		} catch (IOException e) {
 			IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
 					"unable to assemble module", e); //$NON-NLS-1$
 			throw new CoreException(status);
 		}
 		finally{
 			try{
 				packager.finished();
 			}
 			catch(IOException e){
 				//unhandled
 			}
 		}
 	}
 
 	private void doPackModule(IModuleResource resource, ModulePackager packager) throws CoreException, IOException{
 			if (resource instanceof IModuleFolder) {
 				IModuleFolder mFolder = (IModuleFolder)resource;
 				IModuleResource[] resources = mFolder.members();
 
 				packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());
 
 				for (int i = 0; resources!= null && i < resources.length; i++) {
 					doPackModule(resources[i], packager);
 				}
 			} else {
 				String destination = resource.getModuleRelativePath().append(resource.getName()).toPortableString();
 				IFile file = (IFile) resource.getAdapter(IFile.class);
 				if (file != null)
 					packager.write(file, destination);
 				else {
 					File file2 = (File) resource.getAdapter(File.class);
 					packager.write(file2, destination);
 				}
 			}
 	}
 
 	protected IPath copyModule(IModule module, IProgressMonitor monitor) throws CoreException {
 		ProjectModule pm =(ProjectModule)module.loadAdapter(ProjectModule.class, monitor);
 		IStatus[] status = PublishUtil.publishSmart(pm.members(), fAssembleRoot, monitor);
 		if (status != null && status.length > 0)
 			throw new CoreException(status[0]);
 		return fAssembleRoot;
 	}
 }
