 /*
  * Author: C.Williams
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
  * can get copy of the GPL along with further information about RubyPeople and
  * third party software bundled with RDT in the file
  * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
  * http://www.rubypeople.org/RDT.license.
  * 
  * RDT is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package org.rubypeople.rdt.testunit.launcher;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.debug.ui.ILaunchConfigurationTab;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
 import org.rubypeople.rdt.internal.testunit.ui.TestUnitMessages;
 import org.rubypeople.rdt.internal.testunit.ui.TestunitPlugin;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.util.RubyFileSelector;
 import org.rubypeople.rdt.internal.ui.util.RubyProjectSelector;
 import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
 
 /**
  * @author Chris
  * 
  */
 public class TestUnitMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {
 
 	protected RubyProjectSelector projectSelector;
 	protected RubyFileSelector fileSelector;
 	private RubyClassSelector classSelector;
 	protected ElementListSelectionDialog dialog;
 	protected String lastProject = "";
 	protected String lastFile = "";
 	private Label classLabel;
 	private Button allClassesCheckBox;
 	private Button allMethodsCheckBox;
 	private Label testLabel;
 	private Text testMethodEditBox;
 
 	public TestUnitMainTab() {
 		super();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite composite = createPageRoot(parent);
 
 		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_projectLabel);
 		projectSelector = new RubyProjectSelector(composite);
 		projectSelector.setBrowseDialogMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_projectSelectorMessage);
 		projectSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		projectSelector.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent evt) {
 				updateLaunchConfigurationDialog();
 				String newProject = projectSelector.getSelectionText();
 				if (!newProject.equals(lastProject)) {
 					fileSelector.setSelectionText("");
 					classSelector.setSelectionText("");
 				}
 				lastProject = newProject;
 			}
 		});
 
 		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_fileLabel);
 		fileSelector = new RubyFileSelector(composite, projectSelector);
 		fileSelector.setBrowseDialogMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_fileSelectorMessage);
 		fileSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		fileSelector.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent evt) {
 				updateLaunchConfigurationDialog();
 				String newFile = fileSelector.getSelectionText();
 				if (!newFile.equals(lastFile)) {
 					classSelector.setSelectionText("");
 				}
 				lastFile = newFile;
 			}
 		});
 
 		allClassesCheckBox = new Button(composite, SWT.CHECK);
 		allClassesCheckBox.setText(TestUnitMessages.LaunchConfigurationTab_RubyEntryPoint_allTestCases);
 		allClassesCheckBox.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				updateLaunchConfigurationDialog();
 				setControlState();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {}
 		});
 
 		classLabel = new Label(composite, SWT.NONE);
 		classLabel.setText(TestUnitMessages.LaunchConfigurationTab_RubyEntryPoint_classLabel);
 
 		classSelector = new RubyClassSelector(composite, fileSelector, projectSelector);
 		classSelector.setBrowseDialogMessage(TestUnitMessages.LaunchConfigurationTab_RubyEntryPoint_classSelectorMessage);
 		classSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		classSelector.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent evt) {
 				updateLaunchConfigurationDialog();
 			}
 		});
 
 		allMethodsCheckBox = new Button(composite, SWT.CHECK);
 		allMethodsCheckBox.setText(TestUnitMessages.LaunchConfigurationTab_RubyEntryPoint_allTestMethods);
 		allMethodsCheckBox.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				updateLaunchConfigurationDialog();
 				setControlState();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {}
 		});
 
 		testLabel = new Label(composite, SWT.NONE);
 		testLabel.setText(TestUnitMessages.LaunchConfigurationTab_RubyEntryPoint_methodLabel);
 
 		testMethodEditBox = new Text(composite, SWT.BORDER | SWT.BORDER);
 		testMethodEditBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		testMethodEditBox.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				updateLaunchConfigurationDialog();
 
 			}
 		});
 		setControlState();
 
 	}
 
 	private void setControlState() {
 		boolean allClassesChecked = allClassesCheckBox.getSelection();
 		boolean allMethodsChecked = allMethodsCheckBox.getSelection();
 		classLabel.setEnabled(!allClassesChecked);
 		classSelector.setEnabled(!allClassesChecked);
 		allMethodsCheckBox.setEnabled(!allClassesChecked);
 		testLabel.setEnabled(!(allClassesChecked || allMethodsChecked));
 		testMethodEditBox.setEnabled(!(allClassesChecked || allMethodsChecked));
 
 	}
 
 	protected Composite createPageRoot(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout compositeLayout = new GridLayout();
 		compositeLayout.marginWidth = 10;
 		compositeLayout.numColumns = 1;
 		composite.setLayout(compositeLayout);
 
 		setControl(composite);
 		return composite;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 		IResource selectedResource = RubyPlugin.getDefault().getSelectedResource();
 		String projectName = "";
 		String fileName = "";
 		String type = "";
 
 		if (RubyPlugin.getDefault().isRubyFile(selectedResource)) {
 			projectName = selectedResource.getProject().getName();
 			fileName = selectedResource.getProjectRelativePath().toString();
 			IRubyElement[] types = TestSearchEngine.findTests((IFile) selectedResource);
 			if (types.length > 0) {
 				type = types[0].getElementName();
 			}
 		}
 
 		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
 		configuration.setAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, fileName);
 		configuration.setAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, type);
 		configuration.setAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, "");
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, TestUnitLaunchConfigurationDelegate.getTestRunnerPath());
 
 		// set hidden attribute
 		configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.rubypeople.rdt.debug.ui.rubySourceLocator");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		try {
 			String projectName= ""; //$NON-NLS-1$
 			try {
 				projectName = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
 			} catch (CoreException ce) {
 			}
 			projectSelector.setSelectionText(projectName);
 			fileSelector.setSelectionText(configuration.getAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, ""));
 
 			String testClass = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, "");
 			classSelector.setSelectionText(testClass);
 			if (testClass.length() == 0) {
 				allClassesCheckBox.setSelection(true);
 			} else {
 				String testMethod = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, "");
 				testMethodEditBox.setText(testMethod);
 				if (testMethod.length() == 0) allMethodsCheckBox.setSelection(true);
 			}
 			setControlState();
 		} catch (CoreException e) {
 			TestunitPlugin.log(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
 	 */
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		String testMethod = testMethodEditBox.getText();
 		if (allMethodsCheckBox.getSelection()) testMethod = "";
 		String testCaseClass = classSelector.getValidatedSelectionText();
 		if (allClassesCheckBox.getSelection()) {
 			testCaseClass = "";
 			testMethod = "";
 		}
 		configuration.setAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, testMethod);
 		configuration.setAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, testCaseClass);
 		configuration.setAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, fileSelector.getValidatedSelectionText());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
 	 */
 	public String getName() {
 		return TestUnitMessages.JUnitMainTab_tab_label;
 	}
 
 }
