 package org.eclipse.bpel.apache.ode.runtime;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation, University of Stuttgart (IAAS) and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation, University of Stuttgart (IAAS) - initial API and implementation
  *******************************************************************************/
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.bpel.runtimes.publishers.GenericBPELPublisher;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.eclipse.wst.server.core.IRuntime;
 
 /**
  * BPEL publisher for Apache ODE.
  * 
  * @author Tammo van Lessen (IAAS)
  * @author Simon Moser (IBM)
  */
 public class OdeBPELPublisher extends GenericBPELPublisher {
 		
 	public OdeBPELPublisher()
 	{
 		super();
 	}
 
 	@Override
 	public IStatus[] publish(IModuleArtifact[] artifacts, IProgressMonitor monitor) {
 		// resources will always be null for some weird reason :(
 		// therefore we generate a BPELModuleArtifact
 		// the module id value enables us to get BPEL file path relative to its project
 		IModule[] modules = super.getModule();
 		IStatus[] result = new Status[modules.length];
 			
 		try {
 			for (int i = 0; i < modules.length; i++) {
 
 				IModule module = modules[i];
 				
 				List<IFile> files = getArtifacts(module.getProject());
 				
 				if (doesDeploymentDescriptorExist(files)){
 					
 					String currentDeployDir = createDeploymentDestination(module);
 					
 					for (IFile file : files) {
 
 						if (isFileOfInterest(file)) {
 							copyFileToDeploymentDestination(file, currentDeployDir);
 						}
 					}
 
 					//remove <project>.deployed to kick the redeployment
 					new File(new File(currentDeployDir).getParent(), new File(currentDeployDir).getName() + ".deployed").delete();
 
 					result[i] = new Status(IStatus.OK, OdePlugin.PLUGIN_ID, 1,
 							"Deployment successful", null);
 				}
 				else {
 					result[i] = new Status(IStatus.ERROR, OdePlugin.PLUGIN_ID, 1,
 							"Deployment Descriptor does not exist. Create a ODE Deployment Descriptor and try again", null);
 				}
 			}
 		} catch (Exception e) {
 			result[0] = new Status(IStatus.ERROR, OdePlugin.PLUGIN_ID, 0,
 					"Error during deployment: " + e.getMessage(), e);
 
 		}		
 
 		return result;
 	}
 
 	@Override
 	public IStatus[] unpublish(IProgressMonitor monitor) {
 		IModule[] modules = super.getModule();
 		IStatus[] result = new Status[modules.length];
 
 		try {
 			for (int i = 0; i < modules.length; i++) {
 
 				IModule module = modules[i];
 				String currentDeployDir = createDeploymentDestination(module);
 
 				//remove <project>.deployed to kick the redeployment
 				deleteDir(new File(currentDeployDir));
 
 				result[i] = new Status(IStatus.OK, OdePlugin.PLUGIN_ID, 1,
 						"Undeployment successful", null);
 			}
 		} catch (Exception e) {
 			result[0] = new Status(IStatus.ERROR, OdePlugin.PLUGIN_ID, 0,
 					"Error during undeployment: " + e.getMessage(), e);
 		}		
 
 		return result;
 	}
 	
 	protected boolean doesDeploymentDescriptorExist(List<IFile> files){
 				
 		for (IFile file : files) {
 		
 			if ( file.getName().equalsIgnoreCase("deploy.xml")) { //NON-NLS-1				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	protected List<IFile> getArtifacts(IProject project) {
 		final List<IFile> files = new ArrayList<IFile>();
 		IResourceVisitor visitor = new IResourceVisitor() {
 			public boolean visit(org.eclipse.core.resources.IResource resource) throws org.eclipse.core.runtime.CoreException {
 				if (resource.getType() == IResource.FILE) {
 					files.add((IFile)resource);
 				}
 				return true;
 			}
 		};
 		try {
 			project.accept(visitor);
 		} 
 		catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return files;
 	}
 	
 	protected boolean isFileOfInterest(IFile file) {
 		
 		String fileExtension = file.getFileExtension();
 		
 		if (fileExtension.equalsIgnoreCase("bpel"))
 			return true; 
 		if (fileExtension.equalsIgnoreCase("wsdl"))
 			return true; 
 		if (fileExtension.equalsIgnoreCase("xsd"))
 			return true; 
 		if (fileExtension.equalsIgnoreCase("soap"))
 			return true;
 		if (fileExtension.equalsIgnoreCase("xml"))
 			return true; 
 		else
 			return false;
 	}
 
 	/**
 	 * This method will create a folder inside the WEB-INF\processes subfolder of the ODE installation
 	 */
 	protected String createDeploymentDestination(IModule module)
 	{
 		String moduleName = module.getName().substring(0, module.getName().indexOf('/'));
 		String deployAppName = moduleName;
 		
 		//get TOMCAT_HOME
 		IRuntime serverDef = getServerRuntime().getRuntime();
 		String tomcatHome = serverDef.getLocation().toOSString();
 		
 		//append ODE's Process target Dir tomcatHome  
 		String deployTarget = tomcatHome.concat(System.getProperty("file.separator") + "webapps" + System.getProperty("file.separator") + "ode" + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "processes" + System.getProperty("file.separator") + deployAppName );
 		
 		File f = new File(deployTarget);
 		if (!f.exists()) {
 			f.mkdir();
 		}
 		
 		return deployTarget;	
 	}
 
 	private boolean deleteDir(File dir) {
 		if (dir.exists()) {
 			File[] files = dir.listFiles();
 			for (File f : files) {
 				if (f.isDirectory()) {
 					deleteDir(f);
 				} else {
 					f.delete();
 				}
 			}
 		}
 		return dir.delete();
 	}
 
 	private void copyFileToDeploymentDestination(IFile ifile, String toDeployDir) {
 
 		String from = ifile.getFullPath().toOSString();
 		String fileName = ifile.getName();
 		String wspath = ifile.getWorkspace().getRoot().getLocation().toOSString();
 
		//bugzilla 284658
		File srcFile = ifile.getLocation().toFile();
 		File targetFile = new File(toDeployDir + System.getProperty("file.separator") + fileName);
 		
 		if (srcFile != null && srcFile.exists()) {
 
 			try {
 
 				BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(srcFile));
 				BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(targetFile));
 
 				byte[] bytes = new byte[1024];
 
 				int count;
 
 				while ((count = inStream.read(bytes)) != -1) {
 					outStream.write(bytes, 0, count);
 				}
 
 				outStream.close();
 				inStream.close();
 			} 
 			catch (Exception e) {
 
 				e.printStackTrace();
 			}
 		}
 	}	
 }
