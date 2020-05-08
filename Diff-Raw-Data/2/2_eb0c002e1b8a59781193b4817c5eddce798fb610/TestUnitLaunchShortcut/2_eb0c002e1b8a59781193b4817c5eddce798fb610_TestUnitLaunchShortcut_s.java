 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.rubypeople.rdt.testunit.launcher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.debug.ui.launcher.RubyApplicationShortcut;
 import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
 import org.rubypeople.rdt.internal.testunit.ui.TestUnitMessages;
 import org.rubypeople.rdt.internal.testunit.ui.TestunitPlugin;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.launching.RubyRuntime;
 
 public class TestUnitLaunchShortcut extends RubyApplicationShortcut {
 
 	static final String TEST_RUNNER_FILE = "RemoteTestRunner.rb";
 	
 	/**
 	 * @param mode
 	 * @param rubyElement
 	 */
 	protected void doLaunch(IRubyElement rubyElement, String mode) throws CoreException {
 		String container = getContainer(rubyElement);
 		ILaunchConfiguration config = findOrCreateLaunchConfiguration(rubyElement, mode, container, "", "");
 		if (config != null) {
 			DebugUITools.launch(config, mode);
 		}
 		TestSearchEngine.findTests((IFile) rubyElement.getUnderlyingResource());
 	}
 
 	protected ILaunchConfiguration findOrCreateLaunchConfiguration(IRubyElement rubyElement, String mode, String container, String testClass, String testName) throws CoreException {
 		IFile rubyFile = (IFile) rubyElement.getUnderlyingResource();
 		ILaunchConfigurationType configType = getRubyLaunchConfigType();
 		List<ILaunchConfiguration> candidateConfigs = null;
 
 		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
 		candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
 		for (int i = 0; i < configs.length; i++) {
 			ILaunchConfiguration config = configs[i];
 			if ((config.getAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, "").equals(container)) 
 					&& (config.getAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, "").equals("")) 
 					&& (config.getAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, "").equals(testName)) 
 					&& (config.getAttribute(RubyLaunchConfigurationAttribute.PROJECT_NAME, "").equals(rubyFile.getProject().getName()))) {
 				candidateConfigs.add(config);
 			}
 		}
 		switch (candidateConfigs.size()) {
 		case 0:
 			return createConfiguration(rubyFile, container, testName);
 		case 1:
 			return (ILaunchConfiguration) candidateConfigs.get(0);
 		default:
 			ILaunchConfiguration config = chooseConfiguration(candidateConfigs, mode);
 			if (config != null) { return config; }
 			return null;
 		}
 	}
 
 	/**
 	 * Show a selection dialog that allows the user to choose one of the
 	 * specified launch configurations. Return the chosen config, or
 	 * <code>null</code> if the user cancelled the dialog.
 	 */
 	protected ILaunchConfiguration chooseConfiguration(List configList, String mode) {
 		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
 		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
 		dialog.setElements(configList.toArray());
 		dialog.setTitle(TestUnitMessages.getString("LaunchTestAction.message.selectConfiguration")); //$NON-NLS-1$
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			dialog.setMessage(TestUnitMessages.getString("LaunchTestAction.message.selectDebugConfiguration")); //$NON-NLS-1$
 		} else {
 			dialog.setMessage(TestUnitMessages.getString("LaunchTestAction.message.selectRunConfiguration")); //$NON-NLS-1$
 		}
 		dialog.setMultipleSelection(false);
 		int result = dialog.open();
 		labelProvider.dispose();
 		if (result == Window.OK) { return (ILaunchConfiguration) dialog.getFirstResult(); }
 		return null;
 	}
 
 	/**
 	 * Convenience method to get the window that owns this action's Shell.
 	 */
 	protected Shell getShell() {
 		return TestunitPlugin.getActiveWorkbenchShell();
 	}
 
 	/**
 	 * @param rubyElement
 	 * @return
 	 */
 	private String getContainer(IRubyElement rubyElement) {
 		try {
 			IFile rubyFile = (IFile) rubyElement.getUnderlyingResource();
 			return rubyFile.getProjectRelativePath().toString();
 		} catch (RubyModelException e) {
 			RubyPlugin.log(e);
 			return rubyElement.getElementName();
 		}
 	}
 
 	protected ILaunchConfiguration createConfiguration(IFile rubyFile, String container, String testName) {
		if (RubyRuntime.getDefault().getDefaultVMInstall() == null) {
 			showNoInterpreterDialog();
 			return null;
 		}
 
 		ILaunchConfiguration config = null;
 		try {
 			ILaunchConfigurationType configType = getRubyLaunchConfigType();
 			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(rubyFile.getName()));
 			wc.setAttribute(RubyLaunchConfigurationAttribute.PROJECT_NAME, rubyFile.getProject().getName());
 			wc.setAttribute(RubyLaunchConfigurationAttribute.FILE_NAME, TestUnitLaunchConfigurationDelegate.getTestRunnerPath());
 			wc.setAttribute(RubyLaunchConfigurationAttribute.WORKING_DIRECTORY, TestUnitLaunchShortcut.getDefaultWorkingDirectory(rubyFile.getProject()));
 			wc.setAttribute(RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, RubyRuntime.getCompositeIdFromVM(RubyRuntime.getDefaultVMInstall()));
 			wc.setAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, container);
 			wc.setAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, testName);
 			wc.setAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, "");
 			wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.rubypeople.rdt.debug.ui.rubySourceLocator");
 			config = wc.doSave();
 		} catch (CoreException ce) {
 			log(ce);
 		}
 		return config;
 	}
 
 	protected ILaunchConfigurationType getRubyLaunchConfigType() {
 		return getLaunchManager().getLaunchConfigurationType(TestUnitLaunchConfigurationDelegate.ID_TESTUNIT_APPLICATION);
 	}
 
 	protected ILaunchManager getLaunchManager() {
 		return DebugPlugin.getDefault().getLaunchManager();
 	}
 
 	protected void log(String message) {
 		TestunitPlugin.log(new Status(Status.INFO, TestunitPlugin.PLUGIN_ID, Status.INFO, message, null));
 	}
 
 	protected void log(Throwable t) {
 		TestunitPlugin.log(t);
 	}
 
 	protected void showNoInterpreterDialog() {
 		MessageDialog.openInformation(TestunitPlugin.getActiveWorkbenchShell(), TestUnitMessages.getString("Dialog.launchWithoutSelectedInterpreter.title"), TestUnitMessages.getString("Dialog.launchWithoutSelectedInterpreter"));
 	}
 
 	protected static String getDefaultWorkingDirectory(IProject project) {
 		if (project != null && project.exists()) { return project.getLocation().toOSString(); }
 		// might have been deleted
 		return TestunitPlugin.getWorkspace().getRoot().getLocation().toOSString();
 	}
 }
