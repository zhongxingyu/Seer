 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.internal.deltacloud.ui.wizards;
 
 import java.text.MessageFormat;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
import org.jboss.tools.deltacloud.core.client.DeltaCloudClientImpl.DeltaCloudServerType;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 
 public class NewCloudConnectionWizard extends Wizard implements INewWizard, CloudConnection {
 
 	private static final String MAINPAGE_NAME = "NewCloudConnection.name"; //$NON-NLS-1$
 	private CloudConnectionPage mainPage;
 
 	public NewCloudConnectionWizard() {
 		super();
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	}
 
 	@Override
 	public void addPages() {
 		mainPage = new CloudConnectionPage(WizardMessages.getString(MAINPAGE_NAME), this);
 		addPage(mainPage);
 	}
 
 	@Override
 	public boolean canFinish() {
 		return mainPage.isPageComplete();
 	}
 
 	public boolean performTest() {
 		String name = mainPage.getModel().getName();
 		String url = mainPage.getModel().getUrl();
 		String username = mainPage.getModel().getUsername();
 		String password = mainPage.getModel().getPassword();
 		try {
 			DeltaCloud newCloud = new DeltaCloud(name, url, username, password);
 			return newCloud.testConnection();
 		} catch (DeltaCloudException e) {
 			ErrorUtils
 					.handleError(WizardMessages.getString("CloudConnectionAuthError.title"),
 							WizardMessages.getFormattedString("CloudConnectionAuthError.message", url), e, getShell());
 			return true;
 		}
 	}
 
 	@Override
 	public boolean performFinish() {
 		String name = mainPage.getModel().getName();
 		String url = mainPage.getModel().getUrl();
 		String username = mainPage.getModel().getUsername();
 		String password = mainPage.getModel().getPassword();
		String type = getServerType();
 		try {
 			DeltaCloud newCloud = new DeltaCloud(name, url, username, password, type);
 			DeltaCloudManager.getDefault().addCloud(newCloud);
 			DeltaCloudManager.getDefault().saveClouds();
 		} catch (Exception e) {
 			// TODO internationalize strings
 			ErrorUtils
 					.handleError("Error", MessageFormat.format("Could not create cloud {0}", name), e, getShell());
 		}
 		return true;
 	}
 
	private String getServerType() {
		DeltaCloudServerType type = mainPage.getModel().getType();
		if (type == null) {
			return null;
		}
		
		return type.toString();
	}

 	@Override
 	public boolean needsProgressMonitor() {
 		return true;
 	}
 }
