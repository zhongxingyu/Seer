 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.wizards.pages;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.model.ModelManager;
 import org.eclipse.tcf.te.tcf.ui.controls.PeerNameControl;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.controls.validator.RegexValidator;
 import org.eclipse.tcf.te.ui.controls.validator.Validator;
 import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
 import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
 import org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 import org.eclipse.tcf.te.ui.wizards.pages.AbstractFormsWizardPage;
 import org.eclipse.ui.forms.IManagedForm;
 
 /**
  * Abstract new configuration wizard page implementation.
  */
 public abstract class AbstractConfigWizardPage extends AbstractFormsWizardPage implements IDataExchangeNode {
 	private ConfigNameControl configName = null;
 	private AbstractSection selectorSection = null;
 	private AbstractSection detailsSection = null;
 	/* default */ Button launchDbg = null;
 	/* default */ Button connect = null;
 	private Button advancedButton = null;
 
 	/* default */ boolean autoConnect = false;
 	/* default */ boolean autoLaunchDbg = false;
 
 	// The list of existing configuration names. Used to generate a unique name
 	// and validate the wizard
 	/* default */ final java.util.List<String> usedNames = new ArrayList<String>();
 
 	/**
 	 * Internal configuration name control implementation.
 	 */
 	protected static class ConfigNameControl extends PeerNameControl {
 
 		/**
 		 * Constructor.
 		 * @param parentPage The parent dialog page this control is embedded in.
 		 *                   Might be <code>null</code> if the control is not associated with a page.
 		 */
 		public ConfigNameControl(IDialogPage parentPage) {
 			super(parentPage);
 
 			setEditFieldLabel(Messages.AbstractConfigWizardPage_configName_label);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
 		 */
 		@Override
 		protected Validator doCreateEditFieldValidator() {
 			return new RegexValidator(Validator.ATTR_MANDATORY, ".*"); //$NON-NLS-1$
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#configureEditFieldValidator(org.eclipse.tcf.te.ui.controls.validator.Validator)
 		 */
 		@Override
 		protected void configureEditFieldValidator(Validator validator) {
 			if (validator instanceof RegexValidator) {
 				validator.setMessageText(RegexValidator.INFO_MISSING_VALUE, Messages.AbstractConfigWizardPage_configName_infoMissingValue);
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#isValid()
 		 */
 		@Override
 		public boolean isValid() {
 			boolean valid = true;
 
 			String name = getEditFieldControlTextForValidation();
 			if (!"".equals(name)) { //$NON-NLS-1$
 				// Name is not empty -> check against the list of used names
 				if (getParentPage() instanceof AbstractConfigWizardPage) {
 					valid = !((AbstractConfigWizardPage)getParentPage()).usedNames.contains(name.trim().toUpperCase());
 					if (!valid) {
 						setMessage(Messages.AbstractConfigWizardPage_configName_nameInUse, IMessageProvider.ERROR);
 					}
 				}
 			}
 
 			if (!valid && getControlDecoration() != null) {
 				// Setup and show the control decoration if necessary
 				if (isEnabled()) {
 					// Update the control decorator
 					updateControlDecoration(getMessage(), getMessageType());
 				}
 			}
 
 			return valid ? super.isValid() : false;
 		}
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param pageName The page name. Must not be <code>null</code>.
 	 */
 	public AbstractConfigWizardPage(String pageName) {
 		super(pageName);
 
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param pageName The page name. Must not be <code>null</code>.
 	 * @param title The wizard page title or <code>null</code>.
 	 * @param titleImage The wizard page title image or <code>null</code>.
 	 */
 	public AbstractConfigWizardPage(String pageName, String title, ImageDescriptor titleImage) {
 		super(pageName, title, titleImage);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (configName != null) { configName.dispose(); configName = null; }
 		if (selectorSection != null) { selectorSection.dispose(); selectorSection = null; }
 		if (detailsSection != null) { detailsSection.dispose(); detailsSection = null; }
 
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractFormsWizardPage#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
 	 */
 	@Override
 	protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
 		Assert.isNotNull(parent);
 		Assert.isNotNull(toolkit);
 
 		// Add the controls
 		configName = new ConfigNameControl(this);
 		configName.setupPanel(parent);
 
 		Label label = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
 		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		selectorSection = doCreateSelectorSection(getManagedForm(), parent);
 		if (selectorSection != null) {
 			selectorSection.getSection().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
 		}
 
 		detailsSection = doCreateDetailsSection(getManagedForm(), parent);
 		if (detailsSection != null) {
 			detailsSection.getSection().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
 		}
 
		connect = new Button(parent, SWT.CHECK);
 		connect.setText(Messages.AbstractConfigWizardPage_connect_label);
 		connect.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		connect.setSelection(autoConnect);
 		connect.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				autoConnect = SWTControlUtil.getSelection(connect);
 			}
 		});
 
		launchDbg = new Button(parent, SWT.CHECK);
 		launchDbg.setText(Messages.AbstractConfigWizardPage_launchDbg_label);
 		launchDbg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		launchDbg.setSelection(autoLaunchDbg);
 		launchDbg.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				autoLaunchDbg = SWTControlUtil.getSelection(launchDbg);
 			}
 		});
 
 		if (hasAdvancedButton()) {
			advancedButton = new Button(parent, SWT.PUSH);
 			advancedButton.setText("  " + Messages.AbstractConfigWizardPage_advancedButton_label + "  "); //$NON-NLS-1$ //$NON-NLS-2$
 			advancedButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
 			advancedButton.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					onAdvancedButtonSelected();
 				}
 			});
 		}
 
 		// restore the widget values from the history
 		restoreWidgetValues();
 
 		// Initialize the used configuration name list
 		initializeUsedNameList();
 	}
 
 	/**
 	 * Creates the selector section.
 	 *
 	 * @param form The managed form instance. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 *
 	 * @return The selector section instance or <code>null</code>.
 	 */
 	protected abstract AbstractSection doCreateSelectorSection(IManagedForm form, Composite parent);
 
 	/**
 	 * Returns the selector section instance.
 	 *
 	 * @return The selector section instance or <code>null</code> if not yet created.
 	 */
 	protected final AbstractSection getSelectorSection() {
 		return selectorSection;
 	}
 
 	/**
 	 * Creates the details section.
 	 *
 	 * @param form The managed form instance. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 *
 	 * @return The details section instance or <code>null</code>.
 	 */
 	protected abstract AbstractSection doCreateDetailsSection(IManagedForm form, Composite parent);
 
 	/**
 	 * Returns the details section instance.
 	 *
 	 * @return The details section instance or <code>null</code> if not yet created.
 	 */
 	protected final AbstractSection getDetailsSection() {
 		return detailsSection;
 	}
 
 	/**
 	 * Returns if or if not the page should show an advanced button.
 	 *
 	 * @return <code>True</code> if the page has an advanced button, <code>false</code> if not.
 	 */
 	protected boolean hasAdvancedButton() {
 		return false;
 	}
 
 	/**
 	 * Called once the user pressed the advanced button. Subclasses
 	 * needs to overwrite this method to provide the logic behind the
 	 * advanced button if {@link #hasAdvancedButton()} returned <code>true</code>.
 	 */
 	protected void onAdvancedButtonSelected() {
 	}
 
 	/**
 	 * Returns the peer type.
 	 *
 	 * @return The peer type. Never <code>null</code>.
 	 */
 	protected abstract String getPeerType();
 
 	/**
 	 * Returns the default configuration name template.
 	 *
 	 * @return The default configuration name template. Never <code>null</code>.
 	 */
 	protected abstract String getDefaultConfigNameTemplate();
 
 	/**
 	 * Return the pattern that matches a configuration name with
 	 * the default name. The pattern is used to decide if the user
 	 * modified the configuration name or not.
 	 *
 	 * @param The default configuration name template. Must not be <code>null</code>.
 	 * @return The default configuration name pattern. Never <code>null</code>.
 	 */
 	protected abstract String getDefaultConfigNamePattern(String template);
 
 	/**
 	 * Initialize the used name list.
 	 */
 	protected void initializeUsedNameList() {
 		usedNames.clear();
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				// Get all peer model objects
 				IPeerNode[] peers = ModelManager.getPeerModel().getPeerNodes();
 				// Loop them and find the ones which are of our handled types
 				for (IPeerNode peerNode : peers) {
 						String name = peerNode.getPeer().getName();
 						Assert.isNotNull(name);
 						if (!"".equals(name) && !usedNames.contains(name)) { //$NON-NLS-1$
 							usedNames.add(name.trim().toUpperCase());
 						}
 				}
 			}
 		};
 
 		Assert.isTrue(!Protocol.isDispatchThread());
 		Protocol.invokeAndWait(runnable);
 	}
 
 	/**
 	 * Auto-generate a configuration name.
 	 *
 	 * @param customID The custom ID to bind with the default configuration name template, or <code>null</code>.
 	 */
 	public void autoGenerateConfigurationName(String customID) {
 		// Get the default configuration name template
 		String template = getDefaultConfigNameTemplate();
 		Assert.isNotNull(template);
 		// If the user modified the configuration name, we have to leave it alone.
 		String pattern = getDefaultConfigNamePattern(template);
 		Assert.isNotNull(pattern);
 		String currentName = configName != null ? configName.getEditFieldControlText().trim() : null;
 		if (currentName != null && !"".equals(currentName.trim()) && !currentName.matches(pattern)) { //$NON-NLS-1$
 			return;
 		}
 		// Generate a new proposal
 		final String origProposedName = NLS.bind(template, customID != null ? customID : ""); //$NON-NLS-1$
 		String proposedName = origProposedName;
 
 		// Unify the proposed name to avoid duplicated configuration names
 		int count = 0;
 		while (usedNames.contains(proposedName.trim().toUpperCase())) {
 			count++;
 			proposedName = origProposedName.trim() + " (" + count + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		// Set the configuration name
 		if (configName != null) {
 			configName.setEditFieldControlText(proposedName.trim());
 		}
 	}
 
 	/**
 	 * Returns if or if not to connect after the configuration got created.
 	 *
 	 * @return <code>True</code> to connect, <code>false</code> if not.
 	 */
 	public final boolean isAutoConnect() {
 		return autoConnect;
 	}
 
 	/**
 	 * Returns if or if not to auto-start the debugger after the configuration got created.
 	 *
 	 * @return <code>True</code> to auto-start the debugger, <code>false</code> if not.
 	 */
 	public final boolean isAutoStartDebugger() {
 		return autoLaunchDbg;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractValidatingWizardPage#doValidate()
 	 */
 	@Override
 	protected ValidationResult doValidate() {
 		ValidationResult result = new ValidationResult();
 
 		boolean valid = true;
 
 		if (configName != null) {
 			valid &= configName.isValid();
 			result.setResult(configName);
 		}
 
 		if (selectorSection != null) {
 			valid &= selectorSection.isValid();
 			result.setResult(selectorSection);
 		}
 
 		if (detailsSection != null) {
 			valid &= detailsSection.isValid();
 			result.setResult(detailsSection);
 		}
 
 		result.setValid(valid);
 
 		return result;
 	}
 
 	/**
 	 * Updates the given attributes properties container with the current control content.
 	 *
 	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
 	 */
 	protected void updatePeerAttributes(IPropertiesContainer peerAttributes) {
 		Assert.isNotNull(peerAttributes);
 
 		// If the page has been never shown, we are done here
 		if (getControl() == null) {
 			return;
 		}
 
 		String value = configName != null ? configName.getEditFieldControlText() : null;
 		if (value != null && !"".equals(value)) { //$NON-NLS-1$
 			peerAttributes.setProperty(IPeer.ATTR_NAME, value);
 		}
 
 		if (isAutoStartDebugger()) {
 			peerAttributes.setProperty(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties.PROP_AUTO_START_DEBUGGER, true);
 		}
 
 		if (isAutoConnect()) {
 			peerAttributes.setProperty(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties.PROP_AUTO_CONNECT, true);
 		}
 
 		if (selectorSection != null) {
 			updateAttribute(selectorSection, peerAttributes);
 		}
 		if (detailsSection != null) {
 			updateAttribute(detailsSection, peerAttributes);
 		}
 	}
 
 	/**
 	 * Update the given attributes properties container with the section
 	 * attributes.
 	 *
 	 * @param section The section. Must not be <code>null</code>.
 	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
 	 */
 	protected abstract void updateAttribute(AbstractSection section, IPropertiesContainer peerAttributes);
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode#extractData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
 	public void extractData(IPropertiesContainer data) {
 		Assert.isNotNull(data);
 		// Update with the current control content
 		updatePeerAttributes(data);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode#setupData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
 	public void setupData(IPropertiesContainer data) {
 		if (selectorSection instanceof IDataExchangeNode) {
 			((IDataExchangeNode)selectorSection).setupData(data);
 		}
 		if (detailsSection instanceof IDataExchangeNode) {
 			((IDataExchangeNode)detailsSection).setupData(data);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractWizardPage#saveWidgetValues()
 	 */
 	@Override
 	public void saveWidgetValues() {
 		super.saveWidgetValues();
 
 		IDialogSettings settings = getDialogSettings();
 		if (settings != null) {
 			if (selectorSection != null) {
 				selectorSection.saveWidgetValues(settings);
 			}
 			if (detailsSection != null) {
 				detailsSection.saveWidgetValues(settings);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractWizardPage#restoreWidgetValues()
 	 */
 	@Override
 	public void restoreWidgetValues() {
 		super.restoreWidgetValues();
 
 		IDialogSettings settings = getDialogSettings();
 		if (settings != null) {
 			if (selectorSection != null) {
 				selectorSection.restoreWidgetValues(settings);
 			}
 			if (detailsSection != null) {
 				detailsSection.restoreWidgetValues(settings);
 			}
 		}
 	}
 }
