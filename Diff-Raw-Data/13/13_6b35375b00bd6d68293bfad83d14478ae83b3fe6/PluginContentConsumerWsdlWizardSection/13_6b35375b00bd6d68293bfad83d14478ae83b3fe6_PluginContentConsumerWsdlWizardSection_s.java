 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.ui.wizards;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.pde.core.plugin.IPluginLibrary;
 import org.eclipse.swordfish.tooling.ui.Messages;
 import org.eclipse.swordfish.tooling.ui.wizards.actions.GenerationJob;
 import org.eclipse.swordfish.tooling.ui.wizards.generators.CodeGenerator;
 import org.eclipse.swordfish.tooling.ui.wizards.generators.CxfConsumerClientInvokerGenerator;
 import org.eclipse.swordfish.tooling.ui.wizards.generators.CxfConsumerSpringEndpointGenerator;
 
 /**
  * class PluginContentConsumerWsdlWizardSection
  * 
  * @author amarkevich
  */
 public class PluginContentConsumerWsdlWizardSection extends BasePluginContentWsdlWizardSection {
 
 	public static final String CXF_ENDPOINT_FILENAME = "META-INF/spring/jaxws-consumer.xml";
 
 	protected void updateModel(IProgressMonitor monitor) throws CoreException {
 		IPluginLibrary lib = model.getPluginFactory().createLibrary();
 		lib.setName(".");
 		lib.addContentFilter("*");
 		lib.setExported(true);
 		model.getPluginBase().add(lib);
 	}
 
 	public boolean isConsumer() {
 		return true;
 	}
 
 	
 	protected void generateAdditionalArtifacts(IProgressMonitor monitor,
 			PluginContentWsdlGenerationOperation generationOperation, String serviceURL) throws CoreException {
 		monitor.setTaskName("CxfEndpointConsumerGenerationJob");
 		int index = generationOperation.getImplementorName().lastIndexOf('.');
 		String implementorPath = generationOperation.getImplementorName().substring(0, index).replace(".", "/");
 
		removeUnwantedFiles(implementorPath);
 
 		IPath pathCXF = project.getFullPath().append(CXF_ENDPOINT_FILENAME);
 
 		CxfConsumerSpringEndpointGenerator consumerSpringGen = new CxfConsumerSpringEndpointGenerator(
 				generationOperation.getServiceName(), generationOperation.getNameSpace(), generationOperation
 						.getImplementorName(), serviceURL, page.doGenerateStaticEndpoint(), page
 						.doGenerateExampleCode());
 		runAsJob(consumerSpringGen, pathCXF, monitor);
 		
 		if (page.doGenerateExampleCode()) {
 			String implementorClass = generationOperation.getImplementorName().substring(index + 1);
 			
			IPath pathClientInvoker = project.getFullPath().append("/src/").append(implementorPath).append("/")
 					.append("sample").append("/").append(implementorClass + "ClientInvoker.java");
 			
 			CxfConsumerClientInvokerGenerator clientInvokerGen = new CxfConsumerClientInvokerGenerator(generationOperation
 			                                                                   						.getImplementorName());
 			runAsJob(clientInvokerGen, pathClientInvoker, monitor);
 		}
 		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 	}
 
 	private void runAsJob(CodeGenerator generator, IPath targetPath, IProgressMonitor monitor) throws CoreException {
 		GenerationJob cxfEndpointJob = new GenerationJob(generator, targetPath);
 
 		IStatus result = cxfEndpointJob.runInWorkspace(monitor);
 		if (!result.isOK()) {
 			throw new CoreException(result);
 		}
 	}
 
	private void removeUnwantedFiles(String implementorPath) {
		IPath pathPackageInfo = project.getLocation().append("/src/").append(implementorPath).append("/").append("package-info.java");
 		pathPackageInfo.toFile().delete();
 		
		IPath pathClient = project.getLocation().append("/src/").append(implementorPath);
 		File[] files = pathClient.toFile().listFiles();
 
 		if (files != null) {
 			for (File javaFile : files) {
 				if (javaFile.getName().endsWith("_Client.java")) {
 					javaFile.delete();
 				}
 			}
 		}
 	}
 	
 	
 	protected String getGenerationErrorMessage() {
 		return Messages.ERROR_JAXWS_CONSUMER_GENERATION;
 	}
 
 }
