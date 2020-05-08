 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.launchConfigurations;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.debug.ui.messages.DLTKLaunchConfigurationsMessages;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.preferences.FieldValidators;
 import org.eclipse.dltk.ui.preferences.IFieldValidator;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.views.navigator.ResourceComparator;
 
 public abstract class MainLaunchConfigurationTab extends
 		ScriptLaunchConfigurationTab {
 
 	private Text fScriptText;
 	
 	protected void doInitializeForm(ILaunchConfiguration config) {
 		updateMainModuleFromConfig(config);
 	}
 
 	private Button fSearchButton;
 
 	/**
 	 * Creates the widgets for specifying a main type.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	protected void createMainModuleEditor(Composite parent, String text) {
 		Font font = parent.getFont();
 		Group mainGroup = new Group(parent, SWT.NONE);
 		mainGroup.setText(text);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		mainGroup.setLayoutData(gd);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		mainGroup.setLayout(layout);
 		mainGroup.setFont(font);
 		fScriptText = new Text(mainGroup, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fScriptText.setLayoutData(gd);
 		fScriptText.setFont(font);
 		fScriptText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				updateLaunchConfigurationDialog();
 			}
 		});
 		fSearchButton = createPushButton(mainGroup,
 				DLTKLaunchConfigurationsMessages.mainTab_searchButton, null);
 		fSearchButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				handleSearchButtonSelected();
 			}
 		});
 	}
 
 	/**
 	 * The select button pressed handler
 	 */
 	protected void handleSearchButtonSelected() {
 		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 				getShell(), new WorkbenchLabelProvider(),
 				new WorkbenchContentProvider());
 		dialog
 				.setTitle(DLTKLaunchConfigurationsMessages.mainTab_searchButton_title);
 		dialog
 				.setMessage(DLTKLaunchConfigurationsMessages.mainTab_searchButton_message);
 		IScriptProject proj = getProject();
 		if (proj == null)
 			return;
 		dialog.setInput(proj.getProject());
 		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
 		if (dialog.open() == IDialogConstants.OK_ID) {
 			IResource resource = (IResource) dialog.getFirstResult();
 			String arg = resource.getProjectRelativePath().toPortableString();
 			// check extension
 			fScriptText.setText(arg);
 		}
 	}
 
 	/**
 	 * Loads the main type from the launch configuration's preference store
 	 * 
 	 * @param config
 	 *            the config to load the main type from
 	 */
 	protected void updateMainModuleFromConfig(ILaunchConfiguration config) {
 		String mainModuleName = EMPTY_STRING;
 		try {
 			mainModuleName = config.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 					EMPTY_STRING);
 		}
 		catch (CoreException ce) {
 			DLTKLaunchingPlugin.log(ce);			
 		}
 		
 		if (EMPTY_STRING.equals(mainModuleName))
 		{
 			String[] guesses = getProjectAndScriptNames();
 			if (guesses != null)
 			{
 				super.setProjectName(guesses[0]);
 				mainModuleName = guesses[1];
 			}
 		}
 		
 		fScriptText.setText(mainModuleName);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#doCreateControl(org.eclipse.swt.widgets.Composite)
 	 */
 	protected void doCreateControl(Composite composite) {
 		createMainModuleEditor(composite,
 				DLTKLaunchConfigurationsMessages.mainTab_mainModule);
 	}
 
 	/*
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return DLTKLaunchConfigurationsMessages.mainTab_title;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#doPerformApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	protected void doPerformApply(ILaunchConfigurationWorkingCopy config) {
 		config.setAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME,
 				getScriptName());
 	}
 
	private String getScriptName() {
 		return fScriptText.getText().trim();
 	}
 
 	/**
 	 * Validates the selected launch script.
 	 * 
 	 * @return true if the selected script is valid, false otherwise
 	 */
 	protected boolean validateScript() {
 		IFieldValidator validator = new FieldValidators.FilePathValidator();
 		
 		String projectName = getProjectName();
 		IScriptProject proj = getScriptModel().getScriptProject(projectName);
 		
 		String script = proj.getProject().getLocation().toPortableString() + '/' + getScriptName();
 		IStatus result = validator.validate(script);
 
 		if (!result.isOK())
 		{
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_scriptNotFound);
 			return false;
 		}
 		
 		return true;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.debug.ui.launchConfigurations.ScriptLaunchConfigurationTab#doCanSave()
 	 */
 	protected boolean doCanSave() {
 		return validateScript();
 	}
 
 	
 	/*
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
 	 */
 	public Image getImage() {
 		return DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_CLASS);
 	}
 }
