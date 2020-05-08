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
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.core.PublishUtil;
 import org.eclipse.jst.server.generic.core.internal.CorePlugin;
 import org.eclipse.jst.server.generic.core.internal.GenericServer;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.model.IModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.util.ProjectModule;
 
 public abstract class AbstractModuleAssembler {
 
 	protected ServerRuntime fServerdefinition;
 	protected IModule fModule; 
 	protected GenericServer fServer;
 	
 	/**
 	 * Assemble the module.
 	 * 
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	protected abstract void assemble(IProgressMonitor monitor) throws CoreException;
 
 	
 	public static class Factory {		
 		public static AbstractModuleAssembler getModuleAssembler(IModule module, GenericServer server)
 		{
 			
 			if(isModuleType(module, "jst.web"))
 				return new WarModuleAssembler(module,server);
 			if(isModuleType(module, "jst.ear"))
 				return new EarModuleAssembler(module,server);
 			return new DefaultModuleAssembler(module,server);
 		}
 		
 		private static boolean isModuleType(IModule module, String moduleTypeId){	
 			if(module.getModuleType()!=null && moduleTypeId.equals(module.getModuleType().getId()))
 				return true;
 			return false;
 		}
 	}
 	
 	protected void packModule(IModule module, IPath destination)throws CoreException {
 		String name = getDUName(module);
 	
 		String dest = destination.append(name).toString();
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
 					"unable to assemble module", e);
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
 
 	private String getDUName(IModule module){
 		IModuleType moduleType = module.getModuleType();
 		if(moduleType==null)
 			return module.getName()+".jar";		
 		if("jst.web".equals(moduleType.getId())){
 			IWebModule webmodule = (IWebModule)module.loadAdapter(IWebModule.class, null);
			String contextRoot = webmodule.getContextRoot();
			if(contextRoot.charAt(0) == '/')
				return contextRoot.substring(1)+".war";
			return contextRoot+".war";
 		}
 		if("jst.ear".equals(moduleType.getId()))
 			return module.getName()+".ear";
 		if("jst.connector".equals(moduleType.getId()))
 			return module.getName()+".rar";
 		
 		return module.getName()+".jar";
 	}
 
 	private void doPackModule(IModuleResource resource, ModulePackager packager) throws CoreException, IOException{
 			if (resource instanceof IModuleFolder) {
 				IModuleFolder mFolder = (IModuleFolder)resource;
 				IModuleResource[] resources = mFolder.members();
 				if(resources==null || resources.length==0){
 					packager.writeFolder(resource.getModuleRelativePath().append(resource.getName()).toPortableString());
 				}
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
 
 	protected IPath copyModule(IModule module,IProgressMonitor monitor)throws CoreException{
 		ProjectModule pm =(ProjectModule)module.loadAdapter(ProjectModule.class, monitor);
 		IPath to = getProjectWorkingLocation().append(pm.getId());
 		PublishUtil.smartCopy(pm.members(), to, monitor);
 		return to;
 	}
 	
 	private IPath getProjectWorkingLocation(){
 		return ServerPlugin.getInstance().getTempDirectory(fServer.getServer().getId());
 	}
 	
 }
