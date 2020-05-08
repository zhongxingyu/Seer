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
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugModelPresentation;
 import org.eclipse.debug.ui.ILaunchShortcut;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.rubypeople.rdt.core.RubyElement;
 import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
 import org.rubypeople.rdt.internal.launching.RubyRuntime;
 import org.rubypeople.rdt.internal.ui.rubyeditor.outline.RubyOutlineLabelProvider;
 import org.rubypeople.rdt.testunit.TestunitPlugin;
 import org.rubypeople.rdt.testunit.views.TestUnitMessages;
 
 public class TestUnitLaunchShortcut implements ILaunchShortcut {
 
 	private static final String TEST_RUNNER_FILE = "RemoteTestRunner.rb";
 
 	public void launch(ISelection selection, String mode) {
 		Object firstSelection = null;
 		if (selection instanceof IStructuredSelection) {
 			firstSelection = ((IStructuredSelection) selection).getFirstElement();
 
 		}
 		if (firstSelection == null) {
 			log("Could not find selection.");
 			return;
 		}
 
 		// TODO Allow running of specific methods or classes, not just files
 		RubyElement rubyElement = null;
 		if (firstSelection instanceof IAdaptable) {
 			rubyElement = (RubyElement) ((IAdaptable) firstSelection).getAdapter(RubyElement.class);
 		}
 		if (rubyElement == null) {
 			log("Selection is not a ruby element.");
 			return;
 		}
 		doLaunch(mode, rubyElement);
 	}
 
 	public void launch(IEditorPart editor, String mode) {
 		IEditorInput input = editor.getEditorInput();
 		if (input == null) {
 			log("Could not retrieve input from editor: " + editor.getTitle());
 			return;
 		}
 		RubyElement rubyElement = (RubyElement) input.getAdapter(RubyElement.class);
 		if (rubyElement == null) {
 			log("Editor input is not a ruby file or external ruby file.");
 			return;
 		}
 		doLaunch(mode, rubyElement);
 	}
 
 	/**
 	 * @param mode
 	 * @param rubyElement
 	 */
 	private void doLaunch(String mode, RubyElement rubyElement) {
 		try {
 			String container = getContainer(rubyElement);
 			org.rubypeople.rdt.internal.core.parser.ast.RubyElement[] classes = TestSearchEngine.findTests((IFile) rubyElement.getUnderlyingResource());
 			String testClass = null;
 			if (classes.length == 0) {
 				MessageDialog.openInformation(getShell(), TestUnitMessages.getString("LaunchTestAction.dialog.title"), TestUnitMessages.getString("LaunchTestAction.message.notests")); //$NON-NLS-1$ //$NON-NLS-2$
 			} else if (classes.length > 1) {
 				testClass = chooseType(classes, mode);
 			} else {
 				testClass = classes[0].getName();
 			}
 			if (testClass != null) {
 				ILaunchConfiguration config = findOrCreateLaunchConfiguration(rubyElement, mode, container, testClass, "");
 				if (config != null) {
 					config.launch(mode, null);
 				}
 			}
 		} catch (CoreException e) {
 			log(e);
 		}
 	}
 
 	/**
 	 * Prompts the user to select a type
 	 * 
 	 * @return the selected type or <code>null</code> if none.
 	 */
 	protected String chooseType(org.rubypeople.rdt.internal.core.parser.ast.RubyElement[] types, String mode) {
 		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new RubyOutlineLabelProvider());
 		dialog.setElements(types);
 		dialog.setTitle(TestUnitMessages.getString("LaunchTestAction.dialog.title2")); //$NON-NLS-1$
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			dialog.setMessage(TestUnitMessages.getString("LaunchTestAction.message.selectTestToRun")); //$NON-NLS-1$
 		} else {
 			dialog.setMessage(TestUnitMessages.getString("LaunchTestAction.message.selectTestToDebug")); //$NON-NLS-1$
 		}
 		dialog.setMultipleSelection(false);
 		if (dialog.open() == Window.OK) { return ((org.rubypeople.rdt.internal.core.parser.ast.RubyElement) dialog.getFirstResult()).getName(); }
 		return null;
 	}
 
 	protected ILaunchConfiguration findOrCreateLaunchConfiguration(RubyElement rubyElement, String mode, String container, String testClass, String testName) throws CoreException {
 		IFile rubyFile = (IFile) rubyElement.getUnderlyingResource();
 		ILaunchConfigurationType configType = getRubyLaunchConfigType();
 		List candidateConfigs = null;
 
 		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
 		candidateConfigs = new ArrayList(configs.length);
 		for (int i = 0; i < configs.length; i++) {
 			ILaunchConfiguration config = configs[i];
 			if ((config.getAttribute(TestUnitLaunchConfiguration.LAUNCH_CONTAINER_ATTR, "").equals(container)) && (config.getAttribute(TestUnitLaunchConfiguration.TESTTYPE_ATTR, "").equals(testClass)) && (config.getAttribute(TestUnitLaunchConfiguration.TESTNAME_ATTR, "").equals(testName)) && (config.getAttribute(RubyLaunchConfigurationAttribute.PROJECT_NAME, "").equals(rubyFile.getProject().getName()))) {
 				candidateConfigs.add(config);
 			}
 		}
 		switch (candidateConfigs.size()) {
 		case 0:
 			return createConfiguration(rubyFile, container, testClass, testName);
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
 	private String getContainer(RubyElement rubyElement) {
 		IFile rubyFile = (IFile) rubyElement.getUnderlyingResource();
 		String filename = rubyFile.getProjectRelativePath().toString();
 		//filename = filename.substring(0, filename.lastIndexOf('.'));
 		return filename;
 	}
 
 	protected ILaunchConfiguration createConfiguration(IFile rubyFile, String container, String testClass, String testName) {
 		if (RubyRuntime.getDefault().getSelectedInterpreter() == null) {
 			showNoInterpreterDialog();
 			return null;
 		}
 
 		ILaunchConfiguration config = null;
 		try {
 			ILaunchConfigurationType configType = getRubyLaunchConfigType();
 			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(rubyFile.getName()));
 			wc.setAttribute(RubyLaunchConfigurationAttribute.PROJECT_NAME, rubyFile.getProject().getName());
 
 			String location = TestunitPlugin.getDefault().getBundle().getLocation();
 			int prefixLength = location.indexOf('@');
 			if (prefixLength == -1) { throw new RuntimeException("Location of launching bundle does not contain @: " + location); }
 			String pluginDir = location.substring(prefixLength + 1) + "ruby";
 			if (!new File(pluginDir).exists()) { throw new RuntimeException("Expected directory of RemoteTestRunner.rb does not exist: " + pluginDir); }
 			int port = SocketUtil.findFreePort();
 			wc.setAttribute(RubyLaunchConfigurationAttribute.FILE_NAME, pluginDir + File.separator + TEST_RUNNER_FILE);
 			wc.setAttribute(RubyLaunchConfigurationAttribute.WORKING_DIRECTORY, TestUnitLaunchShortcut.getDefaultWorkingDirectory(rubyFile.getProject()));
 			wc.setAttribute(RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, RubyRuntime.getDefault().getSelectedInterpreter().getName());
 			wc.setAttribute(TestUnitLaunchConfiguration.LAUNCH_CONTAINER_ATTR, container);
 			wc.setAttribute(TestUnitLaunchConfiguration.TESTNAME_ATTR, testName);
 			wc.setAttribute(TestUnitLaunchConfiguration.TESTTYPE_ATTR, testClass);
 			wc.setAttribute(TestUnitLaunchConfiguration.PORT_ATTR, port);
 			wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.rubypeople.rdt.debug.ui.rubySourceLocator");
 			config = wc.doSave();
 		} catch (CoreException ce) {
 			log(ce);
 		}
 		return config;
 	}
 
 	protected ILaunchConfigurationType getRubyLaunchConfigType() {
 		return getLaunchManager().getLaunchConfigurationType(TestUnitLaunchConfiguration.ID_TESTUNIT_APPLICATION);
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
