 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.jayway.eclipse.adt.extensions.ui.wizards;
 
 
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.CodeGeneration;
 import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 /**
  * Wizard page for new android activity.
  * @author Michael Kober
  */
 public class NewActivityWizardPage extends NewTypeWizardPage {
 
 	private static final String PAGE_NAME = "NewTypeWizardPage";
 	private static final String SETTINGS_CREATEMAIN = "create_main"; 
 	private static final String SETTINGS_CREATECONSTR = "create_constructor"; 
 	private static final String SETTINGS_CREATEUNIMPLEMENTED = "create_unimplemented";
 
 	private boolean isOnStart = false;
 	private boolean isOnRestart = false;
 	private boolean isOnResume = false;
 	private boolean isOnPause = false;
 	private boolean isOnStop = false;
 	private boolean isOnDestroy = false;
 	private IJavaProject javaProject = null;
 	private Set<String> selectedCategories = null;
 	private Set<String> selectedActions = null;
 	
 	/**
 	 * Creates a new {@code ProjectSettingsWizardPage}.
 	 * 
 	 * @param midletProject
 	 *            the project data container
 	 */
 	public NewActivityWizardPage() {
 		super(true, "ProjectSettingWizardPage");
 		setTitle("Android Activity");
 		setDescription("Create a new Android Activity.");
 		
 	}
 
 	/**
 	 * The wizard owning this page is responsible for calling this method with
 	 * the current selection. The selection is used to initialize the fields of
 	 * the wizard page.
 	 * 
 	 * @param selection
 	 *            used to initialize the fields
 	 */
 	public void init(IStructuredSelection selection) {
 		IJavaElement jelem = getInitialJavaElement(selection);
 		javaProject = jelem.getJavaProject();
 		initContainerPage(jelem);
 		initTypePage(jelem);
 		doStatusUpdate();
 		IntentReflectionHelper helper = new IntentReflectionHelper(javaProject);
 		helper.getCategories();
 	}
 
 	// ------ validation --------
 	private void doStatusUpdate() {
 		// status of all used components
 		IStatus[] status = new IStatus[] {
 				fContainerStatus,
 				isEnclosingTypeSelected() ? fEnclosingTypeStatus
 						: fPackageStatus, fTypeNameStatus, fModifierStatus,
 				fSuperClassStatus, fSuperInterfacesStatus };
 
 		// the mode severe status will be displayed and the OK button
 		// enabled/disabled.
 		updateStatus(status);
 	}
 
 	/*
 	 * @see NewContainerWizardPage#handleFieldChanged
 	 */
 	protected void handleFieldChanged(String fieldName) {
 		super.handleFieldChanged(fieldName);
 		doStatusUpdate();
 	}
 
 	// ------ UI --------
 
 	/*
 	 * @see WizardPage#createControl
 	 */
 	public void createControl(Composite parent) {
 		initializeDialogUnits(parent);
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setFont(parent.getFont());
 		int nColumns = 4;
 		GridLayout layout = new GridLayout();
 		layout.numColumns = nColumns;
 		composite.setLayout(layout);
 
 		// pick & choose the wanted UI components
 		createContainerControls(composite, nColumns);
 		createPackageControls(composite, nColumns);
 		createSeparator(composite, nColumns);
 		createTypeNameControls(composite, nColumns);
 		createSuperClassControls(composite, nColumns);
 		setSuperClass("android.app.Activity", true);
 		createSuperInterfacesControls(composite, nColumns);
 		createMethodStubSelectionControls(composite, nColumns);
 
 		// createCommentControls(composite, nColumns);
 		setAddComments(true, false);
 		enableCommentControl(true);
 
 		setControl(composite);
 		Dialog.applyDialogFont(composite);
 		
 		IntentReflectionHelper helper = new IntentReflectionHelper(javaProject);
		createIntentActionsControl(composite, nColumns, helper.getCategories());
		createIntentCategoriesControl(composite, nColumns, helper.getActions());
 	}
 	
 
 	/*
 	 * @see WizardPage#becomesVisible
 	 */
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			setFocus();
 		} else {
 			IDialogSettings dialogSettings = getDialogSettings();
 			if (dialogSettings != null) {
 				IDialogSettings section = dialogSettings.getSection(PAGE_NAME);
 				if (section == null) {
 					section = dialogSettings.addNewSection(PAGE_NAME);
 				}
 				section.put(SETTINGS_CREATEMAIN, false);
 				section.put(SETTINGS_CREATECONSTR, false);
 				section.put(SETTINGS_CREATEUNIMPLEMENTED, true);
 			}
 		}
 	}
 
 	private void createMethodStubSelectionControls(Composite composite,
 			int nColumns) {
 
 		Label label = new Label(composite, SWT.NONE);
 		label.setText("Which method stubs would you like to create?");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false,
 				nColumns, 1));
 
 		Composite methodsComposite = new Composite(composite, SWT.NONE);
 		methodsComposite.setFont(composite.getFont());
 		GridLayout layout = new GridLayout(nColumns, true);
 		methodsComposite.setLayout(layout);
 		methodsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, nColumns, 1));
 
 		Button onCreateCB = new Button(methodsComposite, SWT.CHECK);
 		onCreateCB.setText("onCreate()");
 		onCreateCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onCreateCB.setSelection(true);
 		onCreateCB.setEnabled(false);
 
 		final Button onStartCB = new Button(methodsComposite, SWT.CHECK);
 		onStartCB.setText("onStart()");
 		onStartCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onStartCB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOnStart = onStartCB.getSelection();
 				
 			}
 		});
 
 		final Button onRestartCB = new Button(methodsComposite, SWT.CHECK);
 		onRestartCB.setText("onRestart()");
 		onRestartCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onRestartCB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOnRestart = onRestartCB.getSelection();
 			}
 		});
 
 		final Button onResumeCB = new Button(methodsComposite, SWT.CHECK);
 		onResumeCB.setText("onResume()");
 		onResumeCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onResumeCB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOnResume = onResumeCB.getSelection();
 			}
 		});
 
 		final Button onPauseCB = new Button(methodsComposite, SWT.CHECK);
 		onPauseCB.setText("onPause()");
 		onPauseCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onPauseCB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOnPause = onPauseCB.getSelection();
 			}
 		});
 
 		final Button onStopCB = new Button(methodsComposite, SWT.CHECK);
 		onStopCB.setText("onStop()");
 		onStopCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onStopCB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOnStop = onStopCB.getSelection();
 			}
 		});
 
 		final Button onDestroyCB = new Button(methodsComposite, SWT.CHECK);
 		onDestroyCB.setText("onDestroy()");
 		onDestroyCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
 		onDestroyCB.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				isOnDestroy = onDestroyCB.getSelection();
 			}
 		});
 	}
 
 	/*
 	 * @see NewTypeWizardPage#createTypeMembers
 	 */
 	protected void createTypeMembers(IType type, ImportsManager imports,
 			IProgressMonitor monitor) throws CoreException {
 		boolean doOnCreate = true;
 		boolean doConstr = false;
 		boolean doInherited = true;
 		createInheritedMethods(type, doConstr, doInherited, imports,
 				new SubProgressMonitor(monitor, 1));
 
 		if (doOnCreate) {
 			generateOnCreate(type, imports);
 		}
 		
 		if (isOnStart) {
 			generateStub("onStart", type, imports);
 		}
 		
 		if (isOnRestart) {
 			generateStub("onRestart", type, imports);
 		}
 		
 		if (isOnResume) {
 			generateStub("onResume", type, imports);
 		}
 		if (isOnPause) {
 			generateStub("onPause", type, imports);
 		}
 		
 		if (isOnStop) {
 			generateStub("onStop", type, imports);
 		}
 		
 		if (isOnDestroy) {
 			generateStub("onDestroy", type, imports);
 		}
 		
 		if (monitor != null) {
 			monitor.done();
 		}
 	}
 
 	private void generateOnCreate(IType type, ImportsManager imports) throws CoreException, JavaModelException {
 		StringBuilder buf = new StringBuilder();
 		final String lineDelim = "\n"; // OK, since content is formatted afterwards //$NON-NLS-1$
 		buf.append("/* (non-Javadoc)").append(lineDelim);
 		buf.append("* @see android.app.Activity#onCreate(android.os.Bundle)")
 				.append(lineDelim);
 		buf.append("*/").append(lineDelim);
 		buf.append("@Override");
 		buf.append(lineDelim);
 		buf.append("public void onCreate("); //$NON-NLS-1$
 		buf.append(imports.addImport("android.os.Bundle")); //$NON-NLS-1$
 		buf.append(" savedInstanceState) {"); //$NON-NLS-1$
 		buf.append(lineDelim);
 		buf.append("super.onCreate(savedInstanceState);");
 		buf.append(lineDelim);
 		final String content = CodeGeneration.getMethodBodyContent(type
 				.getCompilationUnit(), type.getTypeQualifiedName('.'),
 				"onCreate", false, "", lineDelim); //$NON-NLS-1$ //$NON-NLS-2$
 		if (content != null && content.length() != 0)
 			buf.append(content);
 		buf.append(lineDelim);
 		buf.append("}"); //$NON-NLS-1$
 		type.createMethod(buf.toString(), null, false, null);
 	}
 
 	private void generateStub(String method, IType type, ImportsManager imports) throws CoreException, JavaModelException {
 		StringBuilder buf = new StringBuilder();
 		final String lineDelim = "\n"; // OK, since content is formatted afterwards //$NON-NLS-1$
 		buf.append("/* (non-Javadoc)").append(lineDelim);
 		buf.append("* @see android.app.Activity#" + method + "()").append(lineDelim);
 		buf.append("*/").append(lineDelim);
 		buf.append("@Override");
 		buf.append(lineDelim);
 		buf.append("public void " + method + "() {"); //$NON-NLS-1$
 		buf.append(lineDelim);
 		buf.append("super." + method + "();");
 		buf.append(lineDelim);
 		final String content = CodeGeneration.getMethodBodyContent(type
 				.getCompilationUnit(), type.getTypeQualifiedName('.'),
 				method, false, "", lineDelim); //$NON-NLS-1$ //$NON-NLS-2$
 		if (content != null && content.length() != 0)
 			buf.append(content);
 		buf.append(lineDelim);
 		buf.append("}"); //$NON-NLS-1$
 		type.createMethod(buf.toString(), null, false, null);
 	}
 	
 	
 	private void createIntentCategoriesControl(final Composite composite, int nColumns, Set<String> categories) {
 		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, nColumns, 1);
 		ElementListSelector selector = new ElementListSelector(composite, gridData, "Intent categories", "Select Intent categories", categories.toArray());
 		selectedCategories = selector.getSelectedElements();
 	}
 	
 	private void createIntentActionsControl(final Composite composite, int nColumns, Set<String> actions) {
 		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, nColumns, 1);
 		ElementListSelector selector = new ElementListSelector(composite, gridData, "Intent actions", "Select Intent actions", actions.toArray());
 		selectedActions = selector.getSelectedElements();
 	}
 	
 	/**
 	 * Get intent categories
 	 * @return selected Intent categories
 	 */
 	public Set<String> getSelectedCategories() {
 		return selectedCategories;
 	}
 
 	/**
 	 * Get intent actions
 	 * @return selected Intent actions
 	 */
 	public Set<String> getSelectedActions() {
 		return selectedActions;
 	}
 }
