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
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientImpl.DeltaCloudServerType;
 import org.jboss.tools.deltacloud.ui.Activator;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 public class NewCloudConnectionWizard extends Wizard implements INewWizard, CloudConnection  {
 
 	private static final String MAINPAGE_NAME = "NewCloudConnection.name"; //$NON-NLS-1$
 	public static final String LAST_USED_URL = "org.jboss.tools.internal.deltacloud.ui.wizards.LAST_CREATED_URL";
	protected CloudConnectionPage mainPage;
 	protected DeltaCloud initialCloud;
 	private String pageTitle;
 
 	public NewCloudConnectionWizard() {
 		this(WizardMessages.getString(MAINPAGE_NAME));
 	}
 	
 	public NewCloudConnectionWizard(String pageTitle) {
 		super();
 		this.pageTitle = pageTitle;
 	}
 	
 	public NewCloudConnectionWizard(String pageTitle, DeltaCloud initial) {
 		this(pageTitle);
 		this.initialCloud = initial;
 	}
 	
 	protected CloudConnectionPage createCloudConnectionPage() {
 		Exception e = null;
 		try {
 			if( initialCloud == null )
 				return new CloudConnectionPage(pageTitle, this);
 			// else
 			return new CloudConnectionPage(pageTitle, initialCloud, this);
 		} catch (MalformedURLException e2) {
 			e = e2;
 		} catch (DeltaCloudException e2) {
 			e = e2;
 		}
 		if( e != null ) {
 			ErrorUtils.handleError(WizardMessages.getString("EditCloudConnectionError.title"),
 					WizardMessages.getString("EditCloudConnectionError.message"), e, getShell());
 		}
 		return null;
 	}
 
 	
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	}
 
 
 	@Override
 	public boolean canFinish() {
 		return mainPage.isPageComplete();
 	}
 	
 	@Override
 	public void addPages() {
 		mainPage = createCloudConnectionPage();
 		if( mainPage != null ) 
 			addPage(mainPage);
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
 			ErrorUtils
 					.handleError(WizardMessages.getString("CloudConnectionAuthError.title"),
 							WizardMessages.getFormattedString("CloudConnectionAuthError.message", url), e, getShell());
 			return true;
 		}
 	}
 	
 	protected String getServerType() {
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
 
 	@Override
 	public boolean performFinish() {
 		String name = mainPage.getModel().getName();
 		String url = mainPage.getModel().getUrl();
 		String username = mainPage.getModel().getUsername();
 		String password = mainPage.getModel().getPassword();
 		String type = getServerType();
 		
 		// save URL in some plugin preference key!
 		Preferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
 		String previousURL = prefs.get(LAST_USED_URL, "");
 		if( previousURL == null || previousURL.equals("") || !previousURL.equals(url)) {
 			prefs.put(LAST_USED_URL, url);
 			try {
 				prefs.flush();
 			} catch( BackingStoreException bse ) {
 				// intentionally ignore, non-critical
 			}
 		}
 		
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
 }
