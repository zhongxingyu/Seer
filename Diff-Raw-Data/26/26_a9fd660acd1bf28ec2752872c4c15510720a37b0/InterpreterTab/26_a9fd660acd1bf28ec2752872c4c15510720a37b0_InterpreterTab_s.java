 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.launchConfigurations;
 
 import java.text.MessageFormat;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.debug.ui.messages.ScriptLaunchMessages;
 import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterComboBlock;
 import org.eclipse.dltk.internal.debug.ui.interpreters.InterpreterDescriptor;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.launching.ScriptRuntime.DefaultInterpreterEntry;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * A launch configuration tab that displays and edits the Interpreter install
  * launch configuration attributes.
  * <p>
  * This class may be instantiated. This class is not intended to be subclassed.
  * </p>
  */
 
 public abstract class InterpreterTab extends CommonScriptLaunchTab {
 
 	// Interpreter Block
 	protected AbstractInterpreterComboBlock fInterpreterBlock;
 
 	// Dynamic Interpreter UI widgets
 	protected ILaunchConfigurationTab fDynamicTab;
 	protected Composite fDynamicTabHolder;
 	protected boolean fUseDynamicArea = true;
 
 	protected ILaunchConfigurationWorkingCopy fWorkingCopy;
 	protected ILaunchConfiguration fLaunchConfiguration;
 
 	// State
 	protected boolean fIsInitializing = false;
 
 	private IMainLaunchConfigurationTabListenerManager listenerManager = null;
 
 	public InterpreterTab(
 			IMainLaunchConfigurationTabListenerManager listenerManager) {
 		this.listenerManager = listenerManager;
 	}
 
 	/**
 	 * @deprecated Using of default constructor not allow listening for project
 	 * 	changes. Project changes are method to obtain environment information.
 	 */
 	public InterpreterTab() {
 	}
 
 	public IMainLaunchConfigurationTabListenerManager getMainTab() {
 		return listenerManager;
 	}
 
 	// Selection changed listener (checked InterpreterEnvironment)
 	private IPropertyChangeListener fCheckListener = new IPropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent event) {
 			handleSelectedInterpreterChanged();
 		}
 	};
 
 	// Constants
 	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	public void dispose() {
 		super.dispose();
 		if (fInterpreterBlock != null) {
 			fInterpreterBlock.removePropertyChangeListener(fCheckListener);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		Font font = parent.getFont();
 
 		Composite topComp = new Composite(parent, SWT.NONE);
 		setControl(topComp);
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
 		// IScriptDebugHelpContextIds.
 		// LAUNCH_CONFIGURATION_DIALOG_InterpreterEnvironment_TAB);
 		GridLayout topLayout = new GridLayout();
 		topLayout.numColumns = 1;
 		topLayout.marginHeight = 0;
 		topLayout.marginWidth = 0;
 		topComp.setLayout(topLayout);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		topComp.setLayoutData(gd);
 		topComp.setFont(font);
 
 		fInterpreterBlock = getInterpreterBlock();
 		fInterpreterBlock
 				.setDefaultInterpreterDescriptor(getDefaultInterpreterDescriptor());
 		fInterpreterBlock
 				.setSpecificInterpreterDescriptor(getSpecificInterpreterDescriptor());
 		fInterpreterBlock.createControl(topComp);
 		Control control = fInterpreterBlock.getControl();
 		fInterpreterBlock.addPropertyChangeListener(fCheckListener);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		control.setLayoutData(gd);
 
 		Composite dynTabComp = new Composite(topComp, SWT.NONE);
 		dynTabComp.setFont(font);
 
 		setDynamicTabHolder(dynTabComp);
 		GridLayout tabHolderLayout = new GridLayout();
 		tabHolderLayout.marginHeight = 0;
 		tabHolderLayout.marginWidth = 0;
 		tabHolderLayout.numColumns = 1;
 		getDynamicTabHolder().setLayout(tabHolderLayout);
 		gd = new GridData(GridData.FILL_BOTH);
 		getDynamicTabHolder().setLayoutData(gd);
 	}
 
 	protected abstract AbstractInterpreterComboBlock getInterpreterBlock();
 
 	protected void setDynamicTabHolder(Composite tabHolder) {
 		this.fDynamicTabHolder = tabHolder;
 	}
 
 	protected Composite getDynamicTabHolder() {
 		return fDynamicTabHolder;
 	}
 
 	protected void setDynamicTab(ILaunchConfigurationTab tab) {
 		fDynamicTab = tab;
 	}
 
 	protected ILaunchConfigurationTab getDynamicTab() {
 		return fDynamicTab;
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
 		setLaunchConfigurationWorkingCopy(config);
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			dynamicTab.setDefaults(config);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		fIsInitializing = true;
 		getControl().setRedraw(false);
 		setLaunchConfiguration(configuration);
 		updateInterpreterFromConfig(configuration);
 		fInterpreterBlock
 				.setDefaultInterpreterDescriptor(getDefaultInterpreterDescriptor());
 		IScriptProject scriptProject = getScriptProject();
 		if (scriptProject != null) {
 			fInterpreterBlock.setEnvironment(EnvironmentManager
 					.getEnvironment(scriptProject));
 			fInterpreterBlock.refreshInterpreters();
 		} else {
 			fInterpreterBlock.setEnvironment(EnvironmentManager
 					.getLocalEnvironment());
 			fInterpreterBlock.refreshInterpreters();
 		}
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			dynamicTab.initializeFrom(configuration);
 		}
 		getControl().setRedraw(true);
 		fIsInitializing = false;
 	}
 
 	protected abstract String getNature();
 
 	/**
 	 * @see
 	 * 	ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		configuration.setAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_SCRIPT_NATURE,
 				getNature());
 		if (fInterpreterBlock.isDefaultInterpreter()) {
 			configuration.setAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_CONTAINER_PATH,
 					(String) null);
 		} else {
 			IPath containerPath = fInterpreterBlock.getInterpreterPath();
 			String portablePath = null;
 			if (containerPath != null) {
 				portablePath = containerPath.toPortableString();
 			}
 			configuration.setAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_CONTAINER_PATH,
 					portablePath);
 		}
 
 		// Handle any attributes in the Interpreter-specific area
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab == null) {
 			configuration
 					.setAttribute(
 							ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,
 							(Map) null);
 		} else {
 			dynamicTab.performApply(configuration);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
 	 */
 	public boolean isValid(ILaunchConfiguration config) {
 
 		setErrorMessage(null);
 		setMessage(null);
 
 		IStatus status = fInterpreterBlock.getStatus();
 		if (!status.isOK()) {
 			setErrorMessage(status.getMessage());
 			return false;
 		}
 
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			return dynamicTab.isValid(config);
 		}
 		return true;
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return ScriptLaunchMessages.InterpreterTab__Interp_1;
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#getImage()
 	 */
 	public Image getImage() {
 		return DLTKPluginImages.DESC_OBJS_NATIVE_LIB_PATH_ATTRIB.createImage();
 	}
 
 	protected void updateInterpreterFromConfig(ILaunchConfiguration config) {
 		try {
 			String path = config.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_CONTAINER_PATH,
 					(String) null);
 			if (path != null) {
 				fInterpreterBlock.setPath(Path.fromPortableString(path));
 				return;
 			}
 		} catch (CoreException e) {
 			DLTKDebugUIPlugin.log(e);
 		}
 		fInterpreterBlock.setUseDefaultInterpreter();
 	}
 
 	/**
 	 * Notification that the user changed the selection in the
 	 * InterpreterEnvironment combination box.
 	 */
 	protected void handleSelectedInterpreterChanged() {
 		loadDynamicInterpreterArea();
 
 		// always set the newly created area with defaults
 		ILaunchConfigurationWorkingCopy wc = getLaunchConfigurationWorkingCopy();
 		if (getDynamicTab() == null) {
 			// remove any Interpreter specific arguments from the config
 			if (wc == null) {
 				if (getLaunchConfiguration().isWorkingCopy()) {
 					wc = (ILaunchConfigurationWorkingCopy) getLaunchConfiguration();
 				}
 			}
 			if (!fIsInitializing) {
 				if (wc != null) {
 					wc
 							.setAttribute(
 									ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,
 									(Map) null);
 				}
 			}
 		} else {
 			if (wc == null) {
 				try {
 					if (getLaunchConfiguration().isWorkingCopy()) {
 						// get a fresh copy to work on
 						wc = ((ILaunchConfigurationWorkingCopy) getLaunchConfiguration())
 								.getOriginal().getWorkingCopy();
 					} else {
 						wc = getLaunchConfiguration().getWorkingCopy();
 					}
 				} catch (CoreException e) {
 					DLTKDebugUIPlugin
 							.errorDialog(
 									ScriptLaunchMessages.InterpreterTab_Unable_to_initialize_defaults_for_selected_InterpreterEnvironment_1,
 									e);
 					return;
 				}
 			}
 			if (!fIsInitializing) {
 				getDynamicTab().setDefaults(wc);
 				getDynamicTab().initializeFrom(wc);
 			}
 		}
 
 		updateLaunchConfigurationDialog();
 	}
 
 	protected void selectInterpreter(String typeID, String InterpreterName) {
 		if (typeID == null) {
 			fInterpreterBlock.setUseDefaultInterpreter();
 		} else {
 			fInterpreterBlock.setPath(ScriptRuntime
 					.newInterpreterContainerPath(typeID, InterpreterName));
 		}
 	}
 
 	/**
 	 * Return the class that implements <code>ILaunchConfigurationTab</code>
 	 * that is registered against the install type of the currently selected
 	 * Interpreter.
 	 */
 	protected ILaunchConfigurationTab getTabForCurrentInterpreter() {
 		IPath path = fInterpreterBlock.getInterpreterPath();
 		if (path != null) {
 			IInterpreterInstall Interpreter = fInterpreterBlock
 					.getInterpreter();
 			if (Interpreter != null) {
 				String InterpreterInstallTypeID = Interpreter
 						.getInterpreterInstallType().getId();
 				return DLTKDebugUIPlugin
 						.getDefault()
 						.getInterpreterInstallTypePage(InterpreterInstallTypeID);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Show the contributed piece of UI that was registered for the install type
 	 * of the currently selected Interpreter.
 	 */
 	protected void loadDynamicInterpreterArea() {
 
 		// Dispose of any current child widgets in the tab holder area
 		Control[] children = getDynamicTabHolder().getChildren();
 		for (int i = 0; i < children.length; i++) {
 			children[i].dispose();
 		}
 
 		if (isUseDynamicInterpreterArea()) {
 			// Retrieve the dynamic UI for the current InterpreterEnvironment
 			setDynamicTab(getTabForCurrentInterpreter());
 			if (getDynamicTab() == null) {
 				return;
 			}
 
 			// Ask the dynamic UI to create its Control
 			getDynamicTab().setLaunchConfigurationDialog(
 					getLaunchConfigurationDialog());
 			getDynamicTab().createControl(getDynamicTabHolder());
 			getDynamicTabHolder().layout();
 		}
 
 	}
 
 	protected ILaunchConfigurationWorkingCopy getLaunchConfigurationWorkingCopy() {
 		return fWorkingCopy;
 	}
 
 	/**
 	 * Overridden here so that any error message in the dynamic UI gets
 	 * returned.
 	 * 
 	 * @see ILaunchConfigurationTab#getErrorMessage()
 	 */
 	public String getErrorMessage() {
 		ILaunchConfigurationTab tab = getDynamicTab();
 		if ((super.getErrorMessage() != null) || (tab == null)) {
 			return super.getErrorMessage();
 		}
 		return tab.getErrorMessage();
 	}
 
 	protected void setLaunchConfigurationWorkingCopy(
 			ILaunchConfigurationWorkingCopy workingCopy) {
 		fWorkingCopy = workingCopy;
 	}
 
 	protected ILaunchConfiguration getLaunchConfiguration() {
 		return fLaunchConfiguration;
 	}
 
 	protected void setLaunchConfiguration(
 			ILaunchConfiguration launchConfiguration) {
 		fLaunchConfiguration = launchConfiguration;
 	}
 
 	/**
 	 * Sets whether this tab will display the Interpreter specific arguments
 	 * area if a InterpreterEnvironment supports Interpreter specific arguments.
 	 * 
 	 * @param visible
 	 * 		whether this tab will display the Interpreter specific arguments
 	 * 		area if a InterpreterEnvironment supports Interpreter specific
 	 * 		arguments
 	 */
 	public void setInterpreterSpecificArgumentsVisible(boolean visible) {
 		fUseDynamicArea = visible;
 	}
 
 	protected boolean isUseDynamicInterpreterArea() {
 		return fUseDynamicArea;
 	}
 
 	protected InterpreterDescriptor getDefaultInterpreterDescriptor() {
 		return new InterpreterDescriptor() {
 
 			public String getDescription() {
 				IScriptProject project = getScriptProject();
 				String name = ScriptLaunchMessages.InterpreterTab_7;
 				if (project == null || project.getProject().isAccessible()) {
 					IEnvironment environment = EnvironmentManager
 							.getEnvironment(project);
 					if (environment != null) {
						IInterpreterInstall interpreter = null;
						interpreter = ScriptRuntime
								.getDefaultInterpreterInstall(new DefaultInterpreterEntry(
										getNature(), environment.getId()));
						if (interpreter != null) {
							name = interpreter.getName();
						}
						return MessageFormat.format(
								ScriptLaunchMessages.InterpreterTab_8,
								new String[] { name });
 					}
 				}
 				try {
 					IInterpreterInstall interpreter = ScriptRuntime
 							.getInterpreterInstall(project);
 					if (interpreter != null) {
 						name = interpreter.getName();
 					}
 				} catch (CoreException e) {
 				}
 				return MessageFormat.format(
 						ScriptLaunchMessages.InterpreterTab_9,
 						new String[] { name });
 			}
 		};
 	}
 
 	protected InterpreterDescriptor getSpecificInterpreterDescriptor() {
 		return null;
 	}
 
 	/**
 	 * Returns the Script project associated with the current config being
 	 * edited, or <code>null</code> if none.
 	 * 
 	 * @returnscriptproject or <code>null</code>
 	 */
 	protected IScriptProject getScriptProject() {
 		if (getLaunchConfiguration() != null) {
 			try {
 				String name = getLaunchConfiguration().getAttribute(
 						ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 						(String) null);
 				if (name != null && name.length() > 0) {
 					IProject project = ResourcesPlugin.getWorkspace().getRoot()
 							.getProject(name);
 					if (project.exists()) {
 						return DLTKCore.create(project);
 					}
 				}
 			} catch (CoreException e) {
 				DLTKDebugUIPlugin.log(e);
 			}
 		}
 		return null;
 	}
 
 	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
 		// update the default InterpreterEnvironment description, in case it has
 		// changed
 		// based on the selected project
 		fInterpreterBlock.refresh();
 	}
 
 	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
 		// do nothing when deactivated
 	}
 }
