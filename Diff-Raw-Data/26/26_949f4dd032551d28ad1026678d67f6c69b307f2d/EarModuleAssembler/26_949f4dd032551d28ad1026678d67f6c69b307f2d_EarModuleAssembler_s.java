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
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.generic.core.internal.CorePlugin;
 import org.eclipse.jst.server.generic.core.internal.GenericServer;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.model.IModuleResourceDelta;
 
 /**
  * Utility for EAR module assembly.
  */
 public class EarModuleAssembler extends AbstractModuleAssembler {
 
 	protected EarModuleAssembler(IModule module, GenericServer server, IPath assembleRoot)
 	{
 		super(module, server, assembleRoot);
 	}
 
 	public IPath assemble(IProgressMonitor monitor) throws CoreException{
 		IPath parent =copyModule(fModule,monitor);
 		IEnterpriseApplication earModule = (IEnterpriseApplication)fModule.loadAdapter(IEnterpriseApplication.class, monitor);
 		IModule[] childModules = earModule.getModules();
 		for (int i = 0; i < childModules.length; i++) {
 			IModule module = childModules[i];
 			String uri = earModule.getURI(module);
			if(uri==null){
 				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,	"unable to assemble module null uri",null ); //$NON-NLS-1$
 				throw new CoreException(status);
 			}
 			if( shouldRepack( module ) ){	
 			    packModule(module,uri, parent);
             }
 		}
 		return parent;
 	}
 	/**
      * Checks if there has been a change in the published resources.
      * @param module
      * @return module changed
 	 */
 	private boolean shouldRepack( IModule module ) {
         final Server server = (Server) fServer.getServer();
         final IModule[] modules ={module}; 
         IModuleResourceDelta[] deltas = server.getPublishedResourceDelta( modules );
         return deltas.length > 0;
     }
 
     protected void packModule(IModule module, String deploymentUnitName, IPath destination) throws CoreException {
 		if(module.getModuleType().getId().equals("jst.web")) //$NON-NLS-1$
 		{
 			AbstractModuleAssembler assembler= AbstractModuleAssembler.Factory.getModuleAssembler(module, fServer);
 			IPath webAppPath = assembler.assemble(new NullProgressMonitor());
 			String realDestination = destination.append(deploymentUnitName).toString();
 			ModulePackager packager=null;
 			try {
 				packager =new ModulePackager(realDestination,false);
 				packager.pack(webAppPath.toFile(),webAppPath.toOSString());
 			
 			} catch (IOException e) {
 				IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 0,
 						"unable to assemble module", e); //$NON-NLS-1$
 				throw new CoreException(status);
 			}
 			finally{
 				if(packager!=null)
 				{
 					
 					try {
 						packager.finished();
 					} catch (IOException e) {
 						// Unhandled
 					}
 				}
 				
 			}
 			
 			
 		}
 		else
 		{
 			super.packModule(module, deploymentUnitName, destination);
 		}
 	}
 }
