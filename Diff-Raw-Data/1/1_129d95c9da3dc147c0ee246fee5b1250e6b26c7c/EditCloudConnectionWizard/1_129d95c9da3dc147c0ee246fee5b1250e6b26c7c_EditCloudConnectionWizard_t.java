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
 
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
 
 public class EditCloudConnectionWizard extends NewCloudConnectionWizard {
 
 	private static final String MAINPAGE_NAME = "EditCloudConnection.name"; //$NON-NLS-1$
 
 	public EditCloudConnectionWizard(DeltaCloud cloud) {
 		super(WizardMessages.getString(MAINPAGE_NAME), cloud);
 	}
 
 	@Override
 	public boolean performFinish() {
 		String name = mainPage.getModel().getName();
 		String url = mainPage.getModel().getUrl();
 		String username = mainPage.getModel().getUsername();
 		String password = mainPage.getModel().getPassword();
 		String type = getServerType();
 		try {
 			String oldName = initialCloud.getName();
 			initialCloud.editCloud(name, url, username, password, type);
 			DeltaCloudManager.getDefault().saveClouds();
 			if (!name.equals(oldName)) {
 				DeltaCloudManager.getDefault().notifyCloudRename();
 			}
 		} catch (Exception e) {
 		}
 		return true;
 	}
 }
