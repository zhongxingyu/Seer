 /*******************************************************************************
  * Copyright (c) 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 
 package org.zend.php.zendserver.deployment.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.Wizard;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
 import org.zend.php.zendserver.deployment.core.sdk.SdkApplication;
 import org.zend.php.zendserver.deployment.core.sdk.SdkTarget;
 import org.zend.php.zendserver.deployment.core.utils.DeploymentUtils;
 import org.zend.php.zendserver.deployment.ui.Activator;
 
 public class ProjectDeploymentWizard extends Wizard {
 
 	private DeploymentDescriptorPage descriptorPage;
 	private ApplicationParametersPage parametersPage;
 	private IDescriptorContainer model;
 
 	public ProjectDeploymentWizard(IDescriptorContainer model) {
 		if (!model.getFile().exists()) {
 			descriptorPage = new DeploymentDescriptorPage(model);
 		}
 		this.model = model;
 		parametersPage = new ApplicationParametersPage(model);
 		setNeedsProgressMonitor(false);
 		setWindowTitle(Messages.deployWizardTitle);
 		setDefaultPageImageDescriptor(Activator
 				.getImageDescriptor(Activator.IMAGE_DEPLOY_WIZARD));
 	}
 
 	@Override
 	public void addPages() {
 		super.addPages();
 		if (!model.getFile().exists()) {
 			addPage(descriptorPage);
 		}
 		addPage(parametersPage);
 	}
 
 	@Override
 	public boolean performFinish() {
 		final String name = getName();
 		final String root = getRoot();
		final String appName = parametersPage.getUserAppName();
 		final String baseUrl = parametersPage.getBaseURL();
 		final String path = model.getFile().getParent().getLocation()
 				.toString();
 		final boolean defaultServer = parametersPage.isDefaultServer();
 		final SdkTarget target = parametersPage.getTarget();
 		final boolean isIgnoreFailures = parametersPage.isIgnoreFailures();
 		final HashMap<String, String> userParams = parametersPage
 				.getParameters();
 		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(
 				getShell());
 		try {
 			progressDialog.run(true, true, new IRunnableWithProgress() {
 				public void run(IProgressMonitor monitor) {
 					try {
 						createDeploymentDescriptor(name, root, monitor);
 						SdkApplication application = new SdkApplication();
 						application.deploy(path, baseUrl, target.getId(),
								userParams, appName, isIgnoreFailures,
 								!defaultServer, defaultServer);
 					} catch (CoreException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated method stub
 		} catch (InterruptedException e) {
 			// TODO Auto-generated method stub
 		}
 		return true;
 	}
 
 	@Override
 	public boolean performCancel() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	private void createDeploymentDescriptor(final String name,
 			final String folder, IProgressMonitor monitor) throws CoreException {
 		IProject project = model.getFile().getProject();
 		if (!model.getFile().exists()) {
 			monitor.beginTask("Creating deployment descriptor...", 1);
 			DeploymentUtils.createDescriptor(project, name, folder, monitor);
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			monitor.done();
 			project.refreshLocal(IResource.DEPTH_ONE, monitor);
 			if (monitor.isCanceled()) {
 				return;
 			}
 		}
 	}
 
 	private String getRoot() {
 		return descriptorPage != null ? descriptorPage.getDocumentRoot() : null;
 	}
 
 	private String getName() {
 		return descriptorPage != null ? descriptorPage.getApplciationName()
 				: null;
 	}
 
 }
