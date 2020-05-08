 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *     JBoss (Pascal Rapicault) - Bug 406907 - Add p2 remediation page to MPC install flow
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.accessibility.Accessible;
 import org.eclipse.swt.accessibility.AccessibleAdapter;
 import org.eclipse.swt.accessibility.AccessibleEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 
 public class MarketplaceWizardDialog extends WizardDialog {
 	private Button backButton;
 
 	private Button nextButton;
 
 	public MarketplaceWizardDialog(Shell parentShell, MarketplaceWizard newWizard) {
 		super(parentShell, newWizard);
 	}
 
 	@Override
 	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
 		Button button = super.createButton(parent, id, label, defaultButton);
 		switch (id) {
 		case IDialogConstants.NEXT_ID:
 			nextButton = button;
 			break;
 		case IDialogConstants.BACK_ID:
 			backButton = button;
 			break;
 		}
 		return button;
 	}
 
 	@Override
 	protected void backPressed() {
 		IWizardPage fromPage = getCurrentPage();
 		super.backPressed();
 		if (fromPage instanceof FeatureSelectionWizardPage
 				&& ((FeatureSelectionWizardPage) fromPage).isInRemediationMode()) {
 			((FeatureSelectionWizardPage) fromPage).flipToDefaultComposite();
 		}
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		super.createButtonsForButtonBar(parent);
 		AccessibleAdapter adapter = new AccessibleAdapter() {
 			@Override
 			public void getName(AccessibleEvent e) {
 				final Button button = (Button) ((Accessible) e.getSource()).getControl();
 				final String text = button.getText();
 				e.result = text.replace('<', ' ').replace('>', ' ');
 			}
 		};
 		backButton.getAccessible().addAccessibleListener(adapter);
 		nextButton.getAccessible().addAccessibleListener(adapter);
 	}
 
 	@Override
 	protected MarketplaceWizard getWizard() {
 		return (MarketplaceWizard) super.getWizard();
 	}
 
 	@Override
 	public void updateButtons() {
 		super.updateButtons();
 		IWizardPage currentPage = getCurrentPage();
 		if (currentPage != null) {
			boolean buttonsChanged = false;
 			String nextButtonLabel = getNextButtonLabel(currentPage);
			if (!nextButtonLabel.equals(nextButton.getText())) {
				nextButton.setText(nextButtonLabel);
				setButtonLayoutData(nextButton);
				buttonsChanged = true;
			}
 			String backButtonLabel = getBackButtonLabel(currentPage);
			if (!backButtonLabel.equals(backButton.getText())) {
				backButton.setText(backButtonLabel);
				setButtonLayoutData(backButton);
				buttonsChanged = true;
			}
			if (buttonsChanged) {
				Composite buttonBar = backButton.getParent();
				buttonBar.layout(true);
			}
 		}
 	}
 
 	public String getNextButtonLabel(IWizardPage page) {
 		if (page == getWizard().getCatalogPage()) {
 			return Messages.MarketplaceWizardDialog_Install_Now;
 		} else if (page == getWizard().getFeatureSelectionWizardPage()) {
 			return Messages.MarketplaceWizardDialog_Confirm;
 		}
 		return IDialogConstants.NEXT_LABEL;
 	}
 
 	public String getBackButtonLabel(IWizardPage page) {
 		if (page == getWizard().getFeatureSelectionWizardPage()) {
 			return Messages.MarketplaceWizardDialog_Install_More;
 		}
 		return IDialogConstants.BACK_LABEL;
 	}
 }
