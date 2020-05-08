 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.launchConfigurations;
 
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.debug.ui.messages.ScriptLaunchMessages;
 import org.eclipse.dltk.internal.debug.ui.interpreters.AbstractInterpreterComboBlock;
 import org.eclipse.dltk.internal.debug.ui.interpreters.IInterpreterComboBlockContext;
 import org.eclipse.dltk.internal.debug.ui.interpreters.InterpreterDescriptor;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ui.DLTKPluginImages;
 import org.eclipse.dltk.ui.viewsupport.ImageDescriptorRegistry;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.osgi.util.NLS;
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
 
 public class InterpreterTab extends CommonScriptLaunchTab {
 
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
 
 	private final IMainLaunchConfigurationTab fMainTab;
 
 	/**
 	 * @since 2.0
 	 */
 	public InterpreterTab(IMainLaunchConfigurationTab mainTab) {
 		this.fMainTab = mainTab;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected IMainLaunchConfigurationTab getMainTab() {
 		return fMainTab;
 	}
 
 	// Selection changed listener (checked InterpreterEnvironment)
 	private IPropertyChangeListener fCheckListener = new IPropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent event) {
 			handleSelectedInterpreterChanged();
 		}
 	};
 
 	// Constants
 	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		registry.dispose();
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
 
 		fInterpreterBlock = createInterpreterBlock(createInterpreterBlockHost());
 		if (mainListener == null) {
 			mainListener = new MainListener();
 			getMainTab().addListener(mainListener);
 		}
 		fInterpreterBlock
 				.setDefaultInterpreterDescriptor(getDefaultInterpreterDescriptor());
 		fInterpreterBlock
 				.setSpecificInterpreterDescriptor(getSpecificInterpreterDescriptor());
 		fInterpreterBlock.createControl(topComp);
 		Control control = fInterpreterBlock.getControl();
 		fInterpreterBlock.addPropertyChangeListener(fCheckListener);
 		gd = new GridData(GridData.FILL_BOTH);
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
 
 	private IMainLaunchConfigurationTabListener mainListener = null;
 
 	private class MainListener implements IMainLaunchConfigurationTabListener {
 		public void projectChanged(IProject project) {
 			refreshInterpreters();
 		}
 
 		public void interactiveChanged(boolean state) {
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected void refreshInterpreters() {
 		fInterpreterBlock.refreshInterpreters();
 	}
 
 	/**
 	 * @return
 	 */
 	private IInterpreterComboBlockContext createInterpreterBlockHost() {
 		return new IInterpreterComboBlockContext() {
 
 			public int getMode() {
 				return M_LAUNCH_CONFIGURATION;
 			}
 
 			public IEnvironment getEnvironment() {
 				final IScriptProject project = getScriptProject();
 				if (project != null) {
 					return EnvironmentManager.getEnvironment(project);
 				} else {
 					return EnvironmentManager.getLocalEnvironment();
 				}
 			}
 
 			public String getNatureId() {
 				return fMainTab.getNatureID();
 			}
 		};
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected AbstractInterpreterComboBlock createInterpreterBlock(
 			IInterpreterComboBlockContext context) {
 		return new AbstractInterpreterComboBlock(context);
 	}
 
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
 	@Override
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		fIsInitializing = true;
 		getControl().setRedraw(false);
 		setLaunchConfiguration(configuration);
 		updateInterpreterFromConfig(configuration);
 		fInterpreterBlock
 				.setDefaultInterpreterDescriptor(getDefaultInterpreterDescriptor());
 		fInterpreterBlock.refreshInterpreters();
 		ILaunchConfigurationTab dynamicTab = getDynamicTab();
 		if (dynamicTab != null) {
 			dynamicTab.initializeFrom(configuration);
 		}
 		getControl().setRedraw(true);
 		fIsInitializing = false;
 	}
 
 	protected final String getNature() {
 		return fMainTab.getNatureID();
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
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
							(Map<String, String>) null);
 		} else {
 			dynamicTab.performApply(configuration);
 		}
 	}
 
 	/**
 	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
 	 */
 	@Override
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
 	@Override
 	public Image getImage() {
 		return registry.get(DLTKPluginImages.DESC_OBJS_NATIVE_LIB_PATH_ATTRIB);
 	}
 
 	private final ImageDescriptorRegistry registry = new ImageDescriptorRegistry(
 			false);
 
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
					wc.setAttribute(
							ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_INSTALL_TYPE_SPECIFIC_ATTRS_MAP,
							(Map<String, String>) null);
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
 	@Override
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
 	 *            whether this tab will display the Interpreter specific
 	 *            arguments area if a InterpreterEnvironment supports
 	 *            Interpreter specific arguments
 	 */
 	public void setInterpreterSpecificArgumentsVisible(boolean visible) {
 		fUseDynamicArea = visible;
 	}
 
 	protected boolean isUseDynamicInterpreterArea() {
 		return fUseDynamicArea;
 	}
 
 	protected InterpreterDescriptor getDefaultInterpreterDescriptor() {
 		return new InterpreterDescriptor() {
 
 			@Override
 			public String getDescription() {
 				final IScriptProject project = getScriptProject();
 				String name = ScriptLaunchMessages.InterpreterTab_7;
 				if (!isValid(project)) {
 					final IInterpreterInstall interpreter = getWorkspaceInterpreter(project);
 					if (interpreter != null) {
 						name = interpreter.getName();
 					}
 					return NLS
 							.bind(ScriptLaunchMessages.InterpreterTab_8, name);
 				}
 				IInterpreterInstall interpreter = getProjectInterpreter(project);
 				if (interpreter != null) {
 					name = interpreter.getName();
 				}
 				return NLS.bind(ScriptLaunchMessages.InterpreterTab_9, name);
 			}
 
 			private boolean isValid(final IScriptProject project) {
 				return project != null && project.getProject().isAccessible();
 			}
 
 			private IInterpreterInstall getProjectInterpreter(
 					IScriptProject project) {
 				try {
 					return ScriptRuntime.getInterpreterInstall(project);
 				} catch (CoreException e) {
 					return null;
 				}
 			}
 
 			private IInterpreterInstall getWorkspaceInterpreter(
 					IScriptProject project) {
 				final IEnvironment environment = EnvironmentManager
 						.getEnvironment(project);
 				return ScriptRuntime.getDefaultInterpreterInstall(getNature(),
 						environment);
 			}
 
 			@Override
 			public IInterpreterInstall getInterpreter() {
 				final IScriptProject project = getScriptProject();
 				if (!isValid(project)) {
 					return getWorkspaceInterpreter(project);
 				} else {
 					return getProjectInterpreter(project);
 				}
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
 	 * @return scriptproject or <code>null</code>
 	 */
 	protected IScriptProject getScriptProject() {
 		return fMainTab.getProject();
 	}
 
 	@Override
 	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
 		// update the default InterpreterEnvironment description, in case it has
 		// changed
 		// based on the selected project
 		fInterpreterBlock.refresh();
 	}
 
 	@Override
 	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
 		// do nothing when deactivated
 	}
 }
