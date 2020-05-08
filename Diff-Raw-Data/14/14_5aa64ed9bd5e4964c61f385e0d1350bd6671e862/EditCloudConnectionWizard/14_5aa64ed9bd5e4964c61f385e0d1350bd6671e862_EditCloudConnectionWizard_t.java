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
 
 import java.net.MalformedURLException;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.equinox.security.storage.ISecurePreferences;
 import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.jboss.tools.common.log.StatusFactory;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
import org.jboss.tools.deltacloud.core.client.DeltaCloudClientImpl.DeltaCloudServerType;
 import org.jboss.tools.deltacloud.ui.Activator;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 
 public class EditCloudConnectionWizard extends Wizard implements INewWizard, CloudConnection {
 
 	private static final String MAINPAGE_NAME = "EditCloudConnection.name"; //$NON-NLS-1$
 	private CloudConnectionPage mainPage;
 	private DeltaCloud cloud;
 
 	public EditCloudConnectionWizard(DeltaCloud cloud) {
 		super();
 		this.cloud = cloud;
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	}
 
 	@Override
 	public void addPages() {
 		String password = getPassword();
 		try {
 			mainPage = new CloudConnectionPage(WizardMessages.getString(MAINPAGE_NAME),
 					cloud.getName(), cloud.getURL(), cloud.getUsername(), password,
 					cloud.getType(), this);
 			addPage(mainPage);
 		} catch (MalformedURLException e) {
 			ErrorUtils.handleError(WizardMessages.getString("EditCloudConnectionError.title"),
 					WizardMessages.getString("EditCloudConnectionError.message"), e, getShell());
 		}
 	}
 
 	private String getPassword() {
 		String password = "";
 		String key = DeltaCloud.getPreferencesKey(cloud.getURL(), cloud.getUsername());
 		ISecurePreferences root = SecurePreferencesFactory.getDefault();
 		ISecurePreferences node = root.node(key);
 		try {
 			password = node.get("password", null); //$NON-NLS-1$
 		} catch (Exception e) {
 			Activator.log(e);
 		}
 		return password;
 	}
 
 	@Override
 	public boolean canFinish() {
 		return mainPage.isPageComplete();
 	}
 
 	public boolean performTest() {
 		String name = mainPage.getName();
 		String url = mainPage.getModel().getUrl();
 		String username = mainPage.getModel().getUsername();
 		String password = mainPage.getModel().getPassword();
 		try {
 			DeltaCloud newCloud = new DeltaCloud(name, url, username, password);
 			return newCloud.testConnection();
 		} catch (DeltaCloudException e) {
 			IStatus status = StatusFactory.getInstance(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
 			Activator.log(status);
 			ErrorDialog.openError(
 					getShell(),
 					WizardMessages.getString("CloudConnectionAuthError.title"),
 					WizardMessages.getFormattedString("CloudConnectionAuthError.message", url),
 					status);
 			return false;
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
 			String oldName = cloud.getName();
 			cloud.editCloud(name, url, username, password, type);
 			DeltaCloudManager.getDefault().saveClouds();
 			if (!name.equals(oldName)) {
 				DeltaCloudManager.getDefault().notifyCloudRename();
 			}
 		} catch (Exception e) {
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
