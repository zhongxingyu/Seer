 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jdt.internal.debug.ui.launcher;
 
  
 import java.io.File;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.variables.IStringVariableManager;
 import org.eclipse.core.variables.VariablesPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.internal.ui.stringsubstitution.StringVariableSelectionDialog;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 import org.eclipse.ui.help.WorkbenchHelp;
 
 /**
  * A control for setting the working directory associated with a launch
  * configuration.
  */
 public class WorkingDirectoryBlock extends JavaLaunchConfigurationTab {
 			
 	// Working directory UI widgets
 	protected Label fWorkingDirLabel;
 	
 	// Local directory
 	protected Button fLocalDirButton;
 	protected Text fWorkingDirText;
 	protected Button fWorkingDirBrowseButton;
 	protected Button fWorkingDirVariablesButton;
 	
 	
 	// Workspace directory
 	protected Button fWorkspaceDirButton;
 	protected Text fWorkspaceDirText;
 	protected Button fWorkspaceDirBrowseButton;
 	protected Button fWorkspaceDirVariablesButton;
 		
 	// use default button
 	protected Button fUseDefaultWorkingDirButton;
 	
 	/**
 	 * The last launch config this tab was initialized from
 	 */
 	protected ILaunchConfiguration fLaunchConfiguration;
 	
 	/**
 	 * A listener to update for text changes and widget selection
 	 */
 	private class WidgetListener extends SelectionAdapter implements ModifyListener {
 		public void modifyText(ModifyEvent e) {
 			updateLaunchConfigurationDialog();
 		}
 		public void widgetSelected(SelectionEvent e) {
 			Object source= e.getSource();
 			if (source == fLocalDirButton) {
 				handleLocationButtonSelected();
 			} else if (source == fWorkingDirBrowseButton) {
 				handleWorkingDirBrowseButtonSelected();
 			} else if (source == fWorkspaceDirButton) {
 				handleLocationButtonSelected();
 			} else if (source == fWorkspaceDirBrowseButton) {
 				handleWorkspaceDirBrowseButtonSelected();
 			} else if (source == fUseDefaultWorkingDirButton) {
 				handleUseDefaultWorkingDirButtonSelected();
 			} else if (source == fWorkingDirVariablesButton) {
 				handleWorkingDirVariablesButtonSelected();
 			} else if (source == fWorkspaceDirVariablesButton) {
 				handleWorkspaceDirVariablesButtonSelected();
 			}
 		}
 	}
 	
 	
 	
 	private WidgetListener fListener= new WidgetListener();
 	
 	/**
 	 * @see ILaunchConfigurationTab#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		Font font = parent.getFont();
 				
 		Composite workingDirComp = new Composite(parent, SWT.NONE);
 		WorkbenchHelp.setHelp(workingDirComp, IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);		
 		GridLayout workingDirLayout = new GridLayout();
 		workingDirLayout.numColumns = 4;
 		workingDirLayout.marginHeight = 0;
 		workingDirLayout.marginWidth = 0;
 		workingDirComp.setLayout(workingDirLayout);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		workingDirComp.setLayoutData(gd);
 		workingDirComp.setFont(font);
 		setControl(workingDirComp);
 		
 		fWorkingDirLabel = new Label(workingDirComp, SWT.NONE);
 		fWorkingDirLabel.setText(LauncherMessages.getString("WorkingDirectoryBlock.12")); //$NON-NLS-1$
 		gd = new GridData();
 		gd.horizontalSpan = 4;
 		fWorkingDirLabel.setLayoutData(gd);
 		fWorkingDirLabel.setFont(font);
 		
 		fLocalDirButton = createRadioButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.&Local_directory__1")); //$NON-NLS-1$
 		fLocalDirButton.addSelectionListener(fListener);
 		
 		fWorkingDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fWorkingDirText.setLayoutData(gd);
 		fWorkingDirText.setFont(font);
 		fWorkingDirText.addModifyListener(fListener);
 		
 		fWorkingDirBrowseButton = createPushButton(workingDirComp, LauncherMessages.getString("JavaArgumentsTab.&Browse_3"), null); //$NON-NLS-1$
 		fWorkingDirBrowseButton.addSelectionListener(fListener);
 		
 		fWorkingDirVariablesButton = createPushButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.17"), null); //$NON-NLS-1$
 		fWorkingDirVariablesButton.addSelectionListener(fListener);		
 		
 		fWorkspaceDirButton = createRadioButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.Works&pace__2")); //$NON-NLS-1$
 		fWorkspaceDirButton.addSelectionListener(fListener);		
 		
 		fWorkspaceDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fWorkspaceDirText.setLayoutData(gd);
 		fWorkspaceDirText.setFont(font);
 		fWorkspaceDirText.addModifyListener(fListener);
 		
 		fWorkspaceDirBrowseButton = createPushButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.B&rowse..._3"), null); //$NON-NLS-1$
 		fWorkspaceDirBrowseButton.addSelectionListener(fListener);		
 
		fWorkspaceDirVariablesButton = createPushButton(workingDirComp, LauncherMessages.getString("WorkingDirectoryBlock.17"), null); //$NON-NLS-1$
 		fWorkspaceDirVariablesButton.addSelectionListener(fListener);
 		
 		fUseDefaultWorkingDirButton = new Button(workingDirComp,SWT.CHECK);
 		fUseDefaultWorkingDirButton.setText(LauncherMessages.getString("JavaArgumentsTab.Use_de&fault_working_directory_4")); //$NON-NLS-1$
 		gd = new GridData();
 		gd.horizontalSpan = 4;
 		fUseDefaultWorkingDirButton.setLayoutData(gd);
 		fUseDefaultWorkingDirButton.setFont(font);
 		fUseDefaultWorkingDirButton.addSelectionListener(fListener);
 				
 	}
 					
 	/**
 	 * @see ILaunchConfigurationTab#dispose()
 	 */
 	public void dispose() {
 	}
 		
 	/**
 	 * Show a dialog that lets the user select a working directory
 	 */
 	protected void handleWorkingDirBrowseButtonSelected() {
 		DirectoryDialog dialog = new DirectoryDialog(getShell());
 		dialog.setMessage(LauncherMessages.getString("WorkingDirectoryBlock.7")); //$NON-NLS-1$
 		String currentWorkingDir = fWorkingDirText.getText();
 		if (!currentWorkingDir.trim().equals("")) { //$NON-NLS-1$
 			File path = new File(currentWorkingDir);
 			if (path.exists()) {
 				dialog.setFilterPath(currentWorkingDir);
 			}			
 		}
 		
 		String selectedDirectory = dialog.open();
 		if (selectedDirectory != null) {
 			fWorkingDirText.setText(selectedDirectory);
 		}		
 	}
 
 	/**
 	 * Show a dialog that lets the user select a working directory from 
 	 * the workspace
 	 */
 	protected void handleWorkspaceDirBrowseButtonSelected() {
 		ContainerSelectionDialog dialog = 
 			new ContainerSelectionDialog(getShell(),
 					ResourcesPlugin.getWorkspace().getRoot(), false,
 					LauncherMessages.getString("WorkingDirectoryBlock.4")); //$NON-NLS-1$
 		
 		IContainer currentContainer= null;
 		try {
 			currentContainer = getContainer();
 		} catch (CoreException e) {
 			// Invalid container specified
 		}
 		if (currentContainer != null) {
 			IPath path = currentContainer.getFullPath();
 			dialog.setInitialSelections(new Object[] {path});
 		}
 		
 		dialog.showClosedProjects(false);
 		dialog.open();
 		Object[] results = dialog.getResult();		
 		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
 			IPath path = (IPath)results[0];
 			String containerName = path.makeRelative().toString();
 			fWorkspaceDirText.setText(containerName);
 		}			
 	}
 	
 	/**
 	 * Returns the selected workspace container,or <code>null</code>
 	 */
 	protected IContainer getContainer() throws CoreException {
 		IResource res = getResource();
 		if (res instanceof IContainer) {
 			return (IContainer)res;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the selected workspace resource, or <code>null</code>
 	 */
 	protected IResource getResource() throws CoreException {
 		String text= fWorkspaceDirText.getText();
 		text= VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(text);
 		IPath path = new Path(text);
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		return root.findMember(path);
 	}	
 	
 	/**
 	 * The "local directory" or "workspace directory" button has been selected.
 	 */
 	protected void handleLocationButtonSelected() {
 		if (!isDefaultWorkingDirectory()) {
 			boolean local = isLocalWorkingDirectory();
 			fWorkingDirText.setEnabled(local);
 			fWorkingDirBrowseButton.setEnabled(local);
 			fWorkingDirVariablesButton.setEnabled(local);
 			fWorkspaceDirText.setEnabled(!local);
 			fWorkspaceDirBrowseButton.setEnabled(!local);
 			fWorkspaceDirVariablesButton.setEnabled(!local);
 		}
 		updateLaunchConfigurationDialog();
 	}
 		
 	/**
 	 * The default working dir check box has been toggled.
 	 */
 	protected void handleUseDefaultWorkingDirButtonSelected() {
 		if (isDefaultWorkingDirectory()) {
 			setDefaultWorkingDir();
 			fLocalDirButton.setEnabled(false);
 			fWorkingDirText.setEnabled(false);
 			fWorkingDirBrowseButton.setEnabled(false);
 			fWorkingDirVariablesButton.setEnabled(false);
 			fWorkspaceDirButton.setEnabled(false);
 			fWorkspaceDirText.setEnabled(false);
 			fWorkspaceDirBrowseButton.setEnabled(false);
 			fWorkspaceDirVariablesButton.setEnabled(false);
 		} else {
 			fLocalDirButton.setEnabled(true);
 			fWorkspaceDirButton.setEnabled(true);
 			fWorkspaceDirVariablesButton.setEnabled(true);
 			handleLocationButtonSelected();
 		}
 	}
 
 
 	protected void handleWorkingDirVariablesButtonSelected() {
 		String variableText = getVariable();
 		if (variableText != null) {
 			fWorkingDirText.append(variableText);
 		}
 	}
 	
 	protected void handleWorkspaceDirVariablesButtonSelected() {
 		String variableText = getVariable();
 		if (variableText != null) {
 			fWorkspaceDirText.append(variableText);
 		}
 	}
 	
 	private String getVariable() {
 		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
 		dialog.open();
 		return dialog.getVariableExpression();
 	}
 	/**
 	 * Sets the default working directory
 	 */
 	protected void setDefaultWorkingDir() {
 		try {
 			ILaunchConfiguration config = getLaunchConfiguration();
 			if (config != null) {
 				IJavaProject javaProject = JavaRuntime.getJavaProject(config);
 				if (javaProject != null) {
 					fWorkspaceDirText.setText(javaProject.getPath().makeRelative().toOSString());
 					fLocalDirButton.setSelection(false);
 					fWorkspaceDirButton.setSelection(true);
 					return;
 				}
 			}
 		} catch (CoreException ce) {
 		}
 		
 		fWorkingDirText.setText(System.getProperty("user.dir")); //$NON-NLS-1$
 		fLocalDirButton.setSelection(true);
 		fWorkspaceDirButton.setSelection(false);		
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
 	 */
 	public boolean isValid(ILaunchConfiguration config) {
 		
 		setErrorMessage(null);
 		setMessage(null);
 		
 		if (isLocalWorkingDirectory()) {
 			String workingDirPath = fWorkingDirText.getText().trim();
 			IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
 			try {
 				workingDirPath= manager.performStringSubstitution(workingDirPath);
 			} catch (CoreException e) {
 				setErrorMessage(e.getMessage());
 				return false;
 			}
 			if (workingDirPath.length() > 0) {
 				File dir = new File(workingDirPath);
 				if (!dir.exists()) {
 					setErrorMessage(LauncherMessages.getString("WorkingDirectoryBlock.10")); //$NON-NLS-1$
 					return false;
 				}
 				if (!dir.isDirectory()) {
 					setErrorMessage(LauncherMessages.getString("WorkingDirectoryBlock.11")); //$NON-NLS-1$
 					return false;
 				}
 			}
 		} else {
 			IContainer container= null;
 			try {
 				container = getContainer();
 			} catch (CoreException e) {
 				setErrorMessage(e.getMessage());
 				return false;
 			}
 			if (container == null) {
 				setErrorMessage(LauncherMessages.getString("WorkingDirectoryBlock.5")); //$NON-NLS-1$
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Defaults are empty.
 	 * 
 	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
 		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		setLaunchConfiguration(configuration);
 		try {			
 			String wd = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null); //$NON-NLS-1$
 			fWorkspaceDirText.setText(""); //$NON-NLS-1$
 			fWorkingDirText.setText(""); //$NON-NLS-1$
 			if (wd == null) {
 				fUseDefaultWorkingDirButton.setSelection(true);
 			} else {
 				IPath path = new Path(wd);
 				if (path.isAbsolute()) {
 					fWorkingDirText.setText(wd);
 					fLocalDirButton.setSelection(true);
 					fWorkspaceDirButton.setSelection(false);
 				} else {
 					fWorkspaceDirText.setText(wd);
 					fWorkspaceDirButton.setSelection(true);
 					fLocalDirButton.setSelection(false);
 				}
 				fUseDefaultWorkingDirButton.setSelection(false);
 			}
 			handleUseDefaultWorkingDirButtonSelected();
 		} catch (CoreException e) {
 			setErrorMessage(LauncherMessages.getString("JavaArgumentsTab.Exception_occurred_reading_configuration___15") + e.getStatus().getMessage()); //$NON-NLS-1$
 			JDIDebugUIPlugin.log(e);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		String wd = null;
 		if (!isDefaultWorkingDirectory()) {
 			if (isLocalWorkingDirectory()) {
 				wd = getAttributeValueFrom(fWorkingDirText);
 			} else {
 				IPath path = new Path(fWorkspaceDirText.getText());
 				path = path.makeRelative();
 				wd = path.toString();
 			}
 		} 
 		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, wd);
 	}
 
 	/**
 	 * Retuns the string in the text widget, or <code>null</code> if empty.
 	 * 
 	 * @return text or <code>null</code>
 	 */
 	protected String getAttributeValueFrom(Text text) {
 		String content = text.getText().trim();
 		if (content.length() > 0) {
 			return content;
 		}
 		return null;
 	}
 	
 	/**
 	 * @see ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return LauncherMessages.getString("WorkingDirectoryBlock.Working_Directory_8"); //$NON-NLS-1$
 	}	
 	
 	/**
 	 * Returns whether the default working directory is to be used
 	 */
 	protected boolean isDefaultWorkingDirectory() {
 		return fUseDefaultWorkingDirButton.getSelection();
 	}
 	
 	/**
 	 * Returns whether the working directory is local
 	 */
 	protected boolean isLocalWorkingDirectory() {
 		return fLocalDirButton.getSelection();
 	}
 
 	/**
 	 * Sets the java project currently specified by the
 	 * given launch config, if any.
 	 */
 	protected void setLaunchConfiguration(ILaunchConfiguration config) {
 		fLaunchConfiguration = config;
 	}	
 	
 	/**
 	 * Returns the current java project context
 	 */
 	protected ILaunchConfiguration getLaunchConfiguration() {
 		return fLaunchConfiguration;
 	}
 	
 }
 
