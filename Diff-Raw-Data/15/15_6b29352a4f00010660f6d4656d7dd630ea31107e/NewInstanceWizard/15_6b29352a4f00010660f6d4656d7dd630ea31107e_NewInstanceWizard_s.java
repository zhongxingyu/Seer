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
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.wizard.Wizard;
 import org.jboss.tools.common.jobs.ChainedJob;
 import org.jboss.tools.deltacloud.core.DeltaCloud;
 import org.jboss.tools.deltacloud.core.DeltaCloudException;
 import org.jboss.tools.deltacloud.core.DeltaCloudImage;
 import org.jboss.tools.deltacloud.core.DeltaCloudInstance;
 import org.jboss.tools.deltacloud.core.DeltaCloudManager;
 import org.jboss.tools.deltacloud.core.job.InstanceStateJob;
 import org.jboss.tools.deltacloud.ui.Activator;
 import org.jboss.tools.deltacloud.ui.DeltacloudUIExtensionManager;
 import org.jboss.tools.deltacloud.ui.ErrorUtils;
 import org.jboss.tools.deltacloud.ui.IDeltaCloudPreferenceConstants;
 import org.jboss.tools.deltacloud.ui.INewInstanceWizardPage;
 import org.osgi.service.prefs.Preferences;
 
 public class NewInstanceWizard extends Wizard {
 
 	private final static String CREATE_INSTANCE_FAILURE_TITLE = "CreateInstanceError.title"; //$NON-NLS-1$
 	private final static String CREATE_INSTANCE_FAILURE_MSG = "CreateInstanceError.msg"; //$NON-NLS-1$
 	private final static String CONFIRM_CREATE_TITLE = "ConfirmCreate.title"; //$NON-NLS-1$
 	private final static String CONFIRM_CREATE_MSG = "ConfirmCreate.msg"; //$NON-NLS-1$
 	private final static String DONT_SHOW_THIS_AGAIN_MSG = "DontShowThisAgain.msg"; //$NON-NLS-1$
 	private final static String STARTING_INSTANCE_TITLE = "StartingInstance.title"; //$NON-NLS-1$
 
 	protected NewInstancePage mainPage;
 	protected INewInstanceWizardPage[] additionalPages;
 	protected DeltaCloud cloud;
 	protected DeltaCloudInstance instance;
 	/**
 	 * Initial image, may be null
 	 */
 	private DeltaCloudImage image;
 
 	public NewInstanceWizard(DeltaCloud cloud) {
 		this.cloud = cloud;
 	}
 
 	public NewInstanceWizard(DeltaCloud cloud, DeltaCloudImage image) {
 		this(cloud);
 		this.image = image;
 	}
 
 	@Override
 	public void addPages() {
 		mainPage = new NewInstancePage(cloud, image);
 		addPage(mainPage);
 		additionalPages = DeltacloudUIExtensionManager.getDefault().loadNewInstanceWizardPages();
 		for (int i = 0; i < additionalPages.length; i++) {
 			addPage(additionalPages[i]);
 		}
 	}
 
 	@Override
 	public boolean canFinish() {
 		return mainPage.isPageComplete();
 	}
 
 	@Override
 	public boolean performFinish() {
 		NewInstanceModel model = mainPage.getModel();
 
 		DeltaCloudImage image = model.getImage();
 		Assert.isTrue(image != null);
 		String imageId = image.getId();
 
 		String profileId = model.getProfile();
 		String realmId = model.getRealm();
 		String memory = mainPage.getMemoryProperty();
 		String storage = mainPage.getStorageProperty();
 
 		String keyname = model.getKeyname();
 		String name = utf8Encode(model.getName());
 
 		// Save persistent settings for this particular cloud
 		cloud.setLastImageId(imageId);
 		cloud.setLastKeyname(keyname);
 
 		Preferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
 
 		boolean result = false;
 		Exception e = null;
 		try {
 			DeltaCloudManager.getDefault().saveClouds();
 			boolean dontShowDialog = prefs.getBoolean(IDeltaCloudPreferenceConstants.DONT_CONFIRM_CREATE_INSTANCE,
 					false);
 			if (!dontShowDialog) {
 				MessageDialogWithToggle dialog =
 						MessageDialogWithToggle.openOkCancelConfirm(getShell(),
 								WizardMessages.getString(CONFIRM_CREATE_TITLE),
 								WizardMessages.getString(CONFIRM_CREATE_MSG),
 								WizardMessages.getString(DONT_SHOW_THIS_AGAIN_MSG),
 								false, null, null);
 				int retCode = dialog.getReturnCode();
 				boolean toggleState = dialog.getToggleState();
 				if (retCode == Dialog.CANCEL)
 					return true;
 				// If warning turned off by user, set the preference for future
 				// usage
 				if (toggleState) {
 					prefs.putBoolean(IDeltaCloudPreferenceConstants.DONT_CONFIRM_CREATE_INSTANCE, true);
 				}
 			}
 			instance = cloud.createInstance(name, imageId, realmId, profileId, keyname, memory, storage);
 			if (instance != null) {
 				result = true;
 			}
 			if (instance != null && instance.getState().equals(DeltaCloudInstance.State.PENDING)) {
 				// TODO use chained job? Maybe. But chainedJob needs to be moved
				ChainedJob first = 
					new InstanceStateJob(
						WizardMessages.getFormattedString(STARTING_INSTANCE_TITLE, instance.getName()), 
						instance,
						DeltaCloudInstance.State.RUNNING);
 				first.setUser(true);
 				ChainedJob last = first;
 				ChainedJob temp;
 				for (int i = 0; i < additionalPages.length; i++) {
 					temp = additionalPages[i].getPerformFinishJob(instance);
 					if (temp != null) {
 						last.setNextJob(temp);
 						last = temp;
 					}
 				}
 				first.schedule();
 			}
 		} catch (DeltaCloudException ex) {
 			e = ex;
 		}
 		if (!result) {
 			ErrorUtils.handleError(
 					WizardMessages.getString(CREATE_INSTANCE_FAILURE_TITLE),
					WizardMessages.getFormattedString(CREATE_INSTANCE_FAILURE_MSG, new String[] { name, imageId,
							realmId, profileId }),
 					e, getShell());
 		}
 		return result;
 	}
 
 	private String utf8Encode(String string) {
 		try {
 			return URLEncoder.encode(string, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO: implement proper handling
 			return "";
 		} //$NON-NLS-1$
 	}
 
 }
