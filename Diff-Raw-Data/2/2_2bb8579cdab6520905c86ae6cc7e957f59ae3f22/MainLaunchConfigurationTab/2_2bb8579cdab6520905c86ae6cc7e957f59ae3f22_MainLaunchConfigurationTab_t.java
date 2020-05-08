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
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.debug.ui.messages.DLTKLaunchConfigurationsMessages;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.ui.DLTKUIStatus;
 import org.eclipse.dltk.launching.IDLTKLaunchConfigurationConstants;
 import org.eclipse.dltk.ui.DLTKUILanguageManager;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.model.WorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.views.navigator.ResourceComparator;
 
 
 public abstract class MainLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
 	/**
 	 * A listener which handles widget change events for the controls in this
 	 * tab.
 	 */
 	private class WidgetListener implements ModifyListener, SelectionListener {
 		public void modifyText(ModifyEvent e) {
 			setErrorMessage(null);
 			if (e.getSource() == fProjText) {
 				IDLTKProject proj = getProject();
 				if (proj != null) {
 					if (!validateProject(proj))
 						setErrorMessage(DLTKLaunchConfigurationsMessages.error_notAValidProject);
 				} else
 					setErrorMessage(DLTKLaunchConfigurationsMessages.error_selectProject);
 			}
 			updateLaunchConfigurationDialog();
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {/* do nothing */
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			Object source = e.getSource();
 			if (source == fProjButton) {
 				handleProjectButtonSelected();
 			} else {
 				updateLaunchConfigurationDialog();
 			}
 		}
 	}
 	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
 	// Project UI widgets
 	protected Text fProjText;
 	private Button fProjButton;
 	private WidgetListener fListener = new WidgetListener();
 
 	/**
 	 * chooses a project for the type of launch config that it is
 	 * 
 	 * @return
 	 */
 	private IDLTKProject chooseProject() {
 		ILabelProvider labelProvider = DLTKUILanguageManager.createLabelProvider(getNatureID());
 		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
 		dialog.setTitle(DLTKLaunchConfigurationsMessages.mainTab_chooseProject_title);
 		dialog.setMessage(DLTKLaunchConfigurationsMessages.mainTab_chooseProject_message);
 		try {
 			dialog.setElements(DLTKCore.create(getWorkspaceRoot()).getScriptProjects());
 		}// end try
 		catch (ModelException jme) {
 			DLTKLaunchingPlugin.log(jme);
 		}
 		IDLTKProject project = getProject();
 		if (project != null) {
 			dialog.setInitialSelections(new Object[] {
 				project
 			});
 		}// end if
 		if (dialog.open() == Window.OK) {
 			return (IDLTKProject) dialog.getFirstResult();
 		}// end if
 		return null;
 	}
 
 	/**
 	 * Creates the widgets for specifying a main type.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	protected void createProjectEditor(Composite parent) {
 		Font font = parent.getFont();
 		Group group = new Group(parent, SWT.NONE);
 		group.setText(DLTKLaunchConfigurationsMessages.mainTab_projectGroup);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		group.setLayoutData(gd);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		group.setLayout(layout);
 		group.setFont(font);
 		fProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fProjText.setLayoutData(gd);
 		fProjText.setFont(font);
 		fProjText.addModifyListener(fListener);
 		fProjButton = createPushButton(group, DLTKLaunchConfigurationsMessages.mainTab_projectButton, null);
 		fProjButton.addSelectionListener(fListener);
 	}
 
 	/**
 	 * Convenience method to get access to thescriptmodel.
 	 */
 	private IScriptModel getScriptModel() {
 		return DLTKCore.create(getWorkspaceRoot());
 	}
 
 	/**
 	 * Return the IDLTKProject corresponding to the project name in the project
 	 * name text field, or null if the text does not match a project name.
 	 */
 	protected IDLTKProject getProject() {
 		String projectName = fProjText.getText().trim();
 		if (projectName.length() < 1) {
 			return null;
 		}// end if
 		return getScriptModel().getScriptProject(projectName);
 	}
 
 	/**
 	 * Convenience method to get the workspace root.
 	 */
 	protected IWorkspaceRoot getWorkspaceRoot() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 
 	protected abstract boolean validateProject(IDLTKProject project);
 
 	protected abstract String getLanguageName();
 	protected abstract String getNatureID();
 
 	/**
 	 * Show a dialog that lets the user select a project. This in turn provides
 	 * context for the main type, allowing the user to key a main type name, or
 	 * constraining the search for main types to the specified project.
 	 */
 	protected void handleProjectButtonSelected() {
 		IDLTKProject project = chooseProject();
 		if (project == null)
 			return;
 		if (!validateProject(project)) {
 			String msg = Messages.format(DLTKLaunchConfigurationsMessages.mainTab_errorDlg_notALangProject, new String[] {
 				getLanguageName()
 			});
 			String reason = Messages.format(DLTKLaunchConfigurationsMessages.mainTab_errorDlg_reasonNotALangProject, new String[] {
 				getLanguageName()
 			});
 			ErrorDialog.openError(getShell(), DLTKLaunchConfigurationsMessages.mainTab_errorDlg_invalidProject, msg,
 					DLTKUIStatus.createError(IStatus.ERROR, reason, null));
 			return;
 		}
 		String projectName = project.getElementName();
 		fProjText.setText(projectName);
 	}// end handle selected
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration config) {
 		updateProjectFromConfig(config);
 		updateMainModuleFromConfig(config);
 		if (fProjText.getText().trim().length() == 0 && fMainText.getText().trim().length() == 0) {
 			// try to fill-in
 			IWorkbenchPage page = DLTKUIPlugin.getActivePage();
 			if (page != null) {
 				IEditorPart editor = page.getActiveEditor();
 				if (editor != null) {
 					IEditorInput editorInput = editor.getEditorInput();
 					if (editorInput != null) {
 						IModelElement me = DLTKUIPlugin.getEditorInputModelElement(editorInput);
 						IDLTKProject project = me.getScriptProject();
 						if (project != null && validateProject(project)) {
 							String projectName = project.getProject().getName();
 							String scriptName =  me.getResource().getProjectRelativePath().toPortableString();//me.getResource().getLocation().toPortableString(); /*me.getResource().getFullPath().toPortableString();*/
 							if (scriptName.trim().length() > 0) {
 								fProjText.setText(projectName);
 								fMainText.setText(scriptName);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * updates the project text field form the configuration
 	 * 
 	 * @param config
 	 *            the configuration we are editing
 	 */
	protected void updateProjectFromConfig(ILaunchConfiguration config) {
 		String projectName = EMPTY_STRING;
 		try {
 			projectName = config.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
 		}// end try
 		catch (CoreException ce) {
 			DLTKLaunchingPlugin.log(ce);
 		}
 		fProjText.setText(projectName);
 	}
 	protected Text fMainText;
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
 		fMainText = new Text(mainGroup, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fMainText.setLayoutData(gd);
 		fMainText.setFont(font);
 		fMainText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				updateLaunchConfigurationDialog();
 			}
 		});
 		fSearchButton = createPushButton(mainGroup, DLTKLaunchConfigurationsMessages.mainTab_searchButton, null);
 		fSearchButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {}
 
 			public void widgetSelected(SelectionEvent e) {
 				handleSearchButtonSelected();
 			}
 		});
 	}
 
 	/**
 	 * The select button pressed handler
 	 */
 	protected void handleSearchButtonSelected() {
 		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
 				new WorkbenchContentProvider());
 		dialog.setTitle(DLTKLaunchConfigurationsMessages.mainTab_searchButton_title);
 		dialog.setMessage(DLTKLaunchConfigurationsMessages.mainTab_searchButton_message);
 		IDLTKProject proj = getProject();
 		if (proj == null)
 			return;
 		dialog.setInput(proj.getProject());
 		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
 		if (dialog.open() == IDialogConstants.OK_ID) {
 			IResource resource = (IResource) dialog.getFirstResult();
 			String arg = resource.getProjectRelativePath().toPortableString();
 			// check extension
 			fMainText.setText(arg);
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
 			mainModuleName = config.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME, EMPTY_STRING);
 		}// end try
 		catch (CoreException ce) {
 			DLTKLaunchingPlugin.log(ce);
 		}
 		fMainText.setText(mainModuleName);
 	}
 
 	public void createControl(Composite parent) {
 		Font font = parent.getFont();
 		Composite comp = new Composite(parent, SWT.NONE);
 		setControl(comp);
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
 		// IScriptDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
 		GridLayout topLayout = new GridLayout();
 		topLayout.verticalSpacing = 0;
 		comp.setLayout(topLayout);
 		comp.setFont(font);
 		createProjectEditor(comp);
 		createVerticalSpacer(comp, 1);
 		createMainModuleEditor(comp, DLTKLaunchConfigurationsMessages.mainTab_mainModule);
 	}
 
 	public String getName() {
 		return DLTKLaunchConfigurationsMessages.mainTab_title;
 	}
 
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		String mainModule = fMainText.getText().trim();
 		String project = fProjText.getText().trim();
 		configuration.setAttribute(IDLTKLaunchConfigurationConstants.ATTR_MAIN_SCRIPT_NAME, mainModule);
 		configuration.setAttribute(IDLTKLaunchConfigurationConstants.ATTR_PROJECT_NAME, project);
 	}
 
 	protected boolean validateProject() {
 		String projectName = fProjText.getText().trim();
 		if (projectName.length() == 0) {
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_selectProject);
 			return false;
 		}
 		IDLTKProject proj = getScriptModel().getScriptProject(projectName);
 		if (proj == null || !validateProject(proj)) {
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_notAValidProject);
 			return false;
 		}
 		return true;
 	}
 
 	protected boolean validateModule() {
 		String mainModule = fMainText.getText().trim();
 		if (mainModule.length() == 0) {
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_selectScript);
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
 	 */
 	public boolean canSave() {
 		return validateProject() && validateModule();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	public boolean isValid(ILaunchConfiguration launchConfig) {
 		setMessage(null);
 		setErrorMessage(null);
 		return validateProject() && validateModule();
 	}
 
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 	// TODO Auto-generated method stub
 	}
 }
