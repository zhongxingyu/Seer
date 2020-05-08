 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.wizards;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.core.PackageFragment;
 import org.eclipse.jdt.ui.IJavaElementSearchConstants;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.SelectionDialog;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 import org.jboss.tools.ws.creation.core.utils.RestEasyLibUtils;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 
 @SuppressWarnings("restriction")
 public class JBossWSAnnotatedClassWizardPage extends WizardPage {
 
 	private JBossWSAnnotatedClassWizard wizard;
 	private Combo projects;
 	private boolean bHasChanged = false;
 	private Button optJAXWS;
 	private Button optJAXRS;
 	private Text packageName;
 	private Text className;
 	private Text appClassName;
 	private Text name;
 	private Button updateWebXML;
 	private Button addJarsIfFound;
 	private Button btnPackageBrowse;
 	private Button btnServiceClassBrowse;
 	private Button btnAppClassBrowse;
 
 	protected JBossWSAnnotatedClassWizardPage(String pageName) {
 		super(pageName);
 		this
 				.setTitle(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_PageTitle);
 		this
 				.setDescription(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_PageDescription);
 	}
 
 	private void createWsTechComposite(Composite parent) {
 		Group wsTechGroup = new Group(parent, SWT.NONE);
 		wsTechGroup
 				.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_WS_Tech_Group);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		wsTechGroup.setLayout(new GridLayout(2, false));
 		wsTechGroup.setLayoutData(gd);
 
 		Composite wsTechComposite = new Composite (wsTechGroup, SWT.NONE);
 		GridLayout gl = new GridLayout(2, true);
 		gl.marginTop = -5;
 		gl.marginBottom = -5;
 		wsTechComposite.setLayout(gl);
 		GridData gridData = new GridData();
 		gridData.horizontalIndent = -5;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
 		wsTechComposite.setLayoutData(gridData);
 		
 		optJAXWS = new Button(wsTechComposite, SWT.RADIO);
 		optJAXWS.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_JAXWS_Button);
 		optJAXWS.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false));
 		
 		optJAXRS = new Button(wsTechComposite, SWT.RADIO);
 		optJAXRS.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_JAXRS_Button);
 		optJAXRS.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false));
 		
 		//default
 		optJAXWS.setSelection(true);
 		
 		optJAXWS.addSelectionListener(new SelectionListener(){
 			public void widgetSelected(SelectionEvent e) {
 				wizard.setJAXWS(true);
 				appClassName.setEnabled(!wizard.isJAXWS());
 				btnAppClassBrowse.setEnabled(!wizard.isJAXWS());
 				updateDefaultValues();
 				setPageComplete(isPageComplete());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		optJAXRS.addSelectionListener(new SelectionListener(){
 			public void widgetSelected(SelectionEvent e) {
 				wizard.setJAXWS(false);
 				appClassName.setEnabled(!wizard.isJAXWS());
 				btnAppClassBrowse.setEnabled(!wizard.isJAXWS());
 				updateDefaultValues();
 				setPageComplete(isPageComplete());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 	}
 	
 	private String testDefaultServiceName( String currentName) {
 		ServiceModel model = wizard.getServiceModel();
 		JBossWSGenerateWizardValidator.setServiceModel(model);
 		IStatus status = JBossWSGenerateWizardValidator.isWSNameValid();
 		try {
 			if (status.getSeverity() == IStatus.ERROR
 					&& !JavaEEProjectUtilities.isDynamicWebProject(wizard
 							.getProject())) {
 				return currentName;
 			}
 		} catch (NullPointerException npe) {
 			return currentName;
 		}
 		String testName = currentName;
 		int i = 1;
 		while (status != null) {
 			testName = currentName + i;
 			wizard.setServiceName(testName);
 			model = wizard.getServiceModel();
 			JBossWSGenerateWizardValidator.setServiceModel(model);
 			status = JBossWSGenerateWizardValidator.isWSNameValid();
 			i++;
 		}
 		return testName;
 	}
 
 	private String testDefaultAppClassName(String currentName) {
 		ServiceModel model = wizard.getServiceModel();
 		JBossRSGenerateWizardValidator.setServiceModel(model);
 		if (wizard.getProject() == null) {
 			return currentName;
 		} else {
 			boolean isDynamicWebProject = false;
 			try {
 				if (wizard.getProject().getNature(
 						"org.eclipse.wst.common.project.facet.core.nature") != null) { //$NON-NLS-1$
 					isDynamicWebProject = true;
 				}
 			} catch (CoreException e) {
 				// ignore
 			}
 			if (!isDynamicWebProject) {
 				return currentName;
 			}
 		}
 		String testName = currentName;
 		IStatus status = JBossRSGenerateWizardValidator.isAppClassNameValid(
 				model.getCustomPackage() + '.' + currentName);
 		int i = 1;
 		while (status != null && status.getSeverity() == IStatus.ERROR) {
 			testName = currentName + i;
 			wizard.setClassName(testName);
 			model = wizard.getServiceModel();
 			JBossWSGenerateWizardValidator.setServiceModel(model);
 			status = JBossWSGenerateWizardValidator.isWSClassValid(testName,
 					wizard.getProject());
 			i++;
 		}
 		return testName;
 	}
 
 	private String testDefaultClassName(String currentName) {
 		ServiceModel model = wizard.getServiceModel();
 		JBossWSGenerateWizardValidator.setServiceModel(model);
 		if (wizard.getProject() == null) {
 			return currentName;
 		} else {
 			boolean isDynamicWebProject = false;
 			try {
 				if (wizard.getProject().getNature(
 						"org.eclipse.wst.common.project.facet.core.nature") != null) { //$NON-NLS-1$
 					isDynamicWebProject = true;
 				}
 			} catch (CoreException e) {
 				// ignore
 			}
 			if (!isDynamicWebProject) {
 				return currentName;
 			}
 		}
 		String testName = currentName;
 		IStatus status = JBossWSGenerateWizardValidator.isWSClassValid(
 				testName, wizard.getProject());
 		int i = 1;
 		while (status != null && status.getSeverity() == IStatus.ERROR) {
 			testName = currentName + i;
 			wizard.setClassName(testName);
 			model = wizard.getServiceModel();
 			JBossWSGenerateWizardValidator.setServiceModel(model);
 			status = JBossWSGenerateWizardValidator.isWSClassValid(testName,
 					wizard.getProject());
 			i++;
 		}
 		return testName;
 	}
 
 	private void updateDefaultValues() {
 		
 		String testName = null;
 		if (wizard.isJAXWS()) {
 			if (className != null && className.getText().trim().length() == 0) {
 				testName = testDefaultClassName(JBossWSAnnotatedClassWizard.WSCLASSDEFAULT);
 				className.setText(testName);
 				wizard.setClassName(testName);
 			}
 			
 			if (name != null && name.getText().trim().length() == 0) {
 				testName = testDefaultServiceName(JBossWSAnnotatedClassWizard.WSNAMEDEFAULT);
 				name.setText(testName);
 				wizard.setServiceName(testName);
 			}
 			appClassName.setText(""); //$NON-NLS-1$
 			wizard.setAppClassName(""); //$NON-NLS-1$
 		} else {
 			if (className != null && className.getText().trim().length() == 0) {
 				testName = testDefaultClassName(JBossWSAnnotatedClassWizard.RSCLASSDEFAULT);
 				className.setText(testName);
 				wizard.setClassName(testName);
 			}
 			if (name != null && name.getText().trim().length() == 0) {
 				testName = testDefaultServiceName(JBossWSAnnotatedClassWizard.RSNAMEDEFAULT);
 				name.setText(testName);
 				wizard.setServiceName(testName);
 			}
 			if (appClassName != null && appClassName.getText().trim().length() == 0) {
 				testName = testDefaultAppClassName(JBossWSAnnotatedClassWizard.RSAPPCLASSDEFAULT);
 				appClassName.setText(testName);
 				wizard.setAppClassName(testName);
 			}
 		}
 		if (packageName != null && packageName.getText().trim().length() == 0) {
 			packageName.setText(JBossWSAnnotatedClassWizard.PACKAGEDEFAULT);
 			wizard.setPackageName(packageName.getText());
 		}
 	}
 	
 	private void createProjectGroup ( Composite parent ) {
 		Group group = new Group(parent, SWT.NONE);
 		group
 				.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Project_Group);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		group.setLayout(new GridLayout(2, false));
 		group.setLayoutData(gd);
 
 		projects = new Combo(group, SWT.BORDER | SWT.DROP_DOWN);
 		projects
 				.setToolTipText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Projects_Combo_Tooltip);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		projects.setLayoutData(gd);
 		refreshProjectList(wizard.getServiceModel().getWebProjectName());
 
 		projects.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				wizard.setProject(projects.getText());
 				setWebXMLSelectionValueBasedOnProjectFacet();
 				bHasChanged = true;
 				setPageComplete(isPageComplete());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		
 	}
 
 	private void createApplicationGroup(Composite parent) {
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalSpan = 2;
 		gd.horizontalIndent = -5;
 		gd.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
 		Group group = new Group(parent, SWT.NONE);
 		group.setLayout(new GridLayout(2, false));
 		group.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Web_Service_Group);
 		group.setLayoutData(gd);
 
 		new Label(group, SWT.NONE)
 			.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Service_Name_field);
 		name = new Text(group, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		name.setLayoutData(gd);
 //		name.setText(wizard.getServiceName());
 		name.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setServiceName(name.getText());
 				bHasChanged = true;
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 		
 		updateWebXML = new Button(group, SWT.CHECK);
 		updateWebXML.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Update_Web_xml_checkbox);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		updateWebXML.setLayoutData(gd);
 		updateWebXML.setSelection(wizard.getUpdateWebXML());
 		updateWebXML.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				wizard.setUpdateWebXML(updateWebXML.getSelection());
 				name.setEnabled(wizard.getUpdateWebXML());
 				setPageComplete(isPageComplete());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		
 		addJarsIfFound = new Button(group, SWT.CHECK);
 		addJarsIfFound.setText(JBossWSUIMessages.JBossRSGenerateWizardPage_AddJarsIfFoundCheckbox);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		addJarsIfFound.setLayoutData(gd);
 		addJarsIfFound.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				wizard.setAddJarsFromRootRuntime(updateWebXML.getSelection());
 				setPageComplete(isPageComplete());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		setWebXMLSelectionValueBasedOnProjectFacet();
 	}
 	
 	private void createImplementationGroup(Composite parent) {
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalSpan = 2;
 		gd.horizontalIndent = -5;
 		gd.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
 		Group group = new Group(parent, SWT.NONE);
 		group.setLayout(new GridLayout(3, false));
 		group.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Service_implementation_group);
 		group.setLayoutData(gd);
 
 		new Label(group, SWT.NONE)
 			.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_package_name_field);
 		packageName = new Text(group, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		packageName.setLayoutData(gd);
 		packageName.setText(wizard.getPackageName());
 		packageName.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setPackageName(packageName.getText());
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 		
 		btnPackageBrowse = new Button(group, SWT.PUSH);
 		btnPackageBrowse.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_package_browse_btn);
 		btnPackageBrowse.addSelectionListener(new SelectionListener(){
 			public void widgetSelected(SelectionEvent e) {
 				if (wizard.getProject() == null) {
 					return;
 				}
 
 				IJavaProject project = JavaCore.create( wizard.getProject());
 				if (project == null) {
 					return;
 				}
 				
 				try {
 					SelectionDialog dialog =
 						JavaUI.createPackageDialog(
 								getShell(), 
 								project, 
 								IJavaElementSearchConstants.CONSIDER_REQUIRED_PROJECTS);
 					if (dialog.open() == Window.OK) {
 						if (dialog.getResult() != null && dialog.getResult().length == 1) {
 							String fqClassName = ((PackageFragment) dialog.getResult()[0]).getElementName();
 							packageName.setText(fqClassName);
 							setPageComplete(isPageComplete());
 						}
 					}
 				} catch (JavaModelException e1) {
 					e1.printStackTrace();
 				}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 
 		new Label(group, SWT.NONE)
 				.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Service_class_field);
 		className = new Text(group, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		className.setLayoutData(gd);
 //		className.setText(wizard.getClassName());
 		className.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setClassName(className.getText());
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 		
 		btnServiceClassBrowse = new Button(group, SWT.PUSH);
 		btnServiceClassBrowse.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Service_class_Browse_btn);
 		btnServiceClassBrowse.addSelectionListener(new SelectionListener(){
 			public void widgetSelected(SelectionEvent e) {
 				if (wizard.getProject() == null) {
 					return;
 				}
 
 				try {
 					SelectionDialog dialog =
 						JavaUI.createTypeDialog(
 								getShell(), 
 								null, 
 								wizard.getProject(), 
 								IJavaElementSearchConstants.CONSIDER_CLASSES, 
 								false);
 					if (dialog.open() == Window.OK) {
 						if (dialog.getResult() != null && dialog.getResult().length == 1) {
 							String fqClassName = ((IType) dialog.getResult()[0]).getElementName();
 							className.setText(fqClassName);
 							setPageComplete(isPageComplete());
 						}
 					}
 				} catch (JavaModelException e1) {
 					e1.printStackTrace();
 				}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		
 		new Label(group, SWT.NONE)
 			.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Application_Class_field);
 		appClassName = new Text(group, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		appClassName.setLayoutData(gd);
 //		appClassName.setText(wizard.getAppClassName());
 		appClassName.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setAppClassName(appClassName.getText());
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 
 		btnAppClassBrowse = new Button(group, SWT.PUSH);
 		btnAppClassBrowse.setText(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_Application_Class_Browse_btn);
 		btnAppClassBrowse.addSelectionListener(new SelectionListener(){
 			public void widgetSelected(SelectionEvent e) {
 				if (wizard.getProject() == null) {
 					return;
 				}
 
 				try {
 					SelectionDialog dialog =
 						JavaUI.createTypeDialog(
 								getShell(), 
 								null, 
 								wizard.getProject(), 
 								IJavaElementSearchConstants.CONSIDER_CLASSES, 
 								false);
 					if (dialog.open() == Window.OK) {
 						if (dialog.getResult() != null && dialog.getResult().length == 1) {
 							String fqClassName = ((IType) dialog.getResult()[0]).getElementName();
 							appClassName.setText(fqClassName);
 							setPageComplete(isPageComplete());
 						}
 					}
 				} catch (JavaModelException e1) {
 					e1.printStackTrace();
 				}
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 	}
 
 	public void createControl(Composite parent) {
 		Composite composite = createDialogArea(parent);
 		this.wizard = (JBossWSAnnotatedClassWizard) this.getWizard();
 
 		createWsTechComposite(composite);
 		createProjectGroup(composite);
 		createApplicationGroup(composite);
 		createImplementationGroup(composite);
 		appClassName.setEnabled(!wizard.isJAXWS());
 		btnAppClassBrowse.setEnabled(!wizard.isJAXWS());
 		updateDefaultValues();
 
 		setControl(composite);
 	}
 
 	private void refreshProjectList(String projectName) {
 		String[] projectNames = getProjects();
 		boolean foundInitialProject = false;
 		projects.removeAll();
 		for (int i = 0; i < projectNames.length; i++) {
 			projects.add(projectNames[i]);
 			if (projectNames[i].equals(projectName)) {
 				foundInitialProject = true;
 			}
 		}
 		if (foundInitialProject)
 			projects.setText(projectName);
 	}
 
 	public IWizardPage getNextPage() {
 		return super.getNextPage();
 	}
 
 	private Composite createDialogArea(Composite parent) {
 		// create a composite with standard margins and spacing
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 7;
 		layout.marginWidth = 7;
 		layout.verticalSpacing = 4;
 		layout.horizontalSpacing = 4;
 		layout.numColumns = 2;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		return composite;
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		return validate();
 	}
 
 	private void setWebXMLSelectionValueBasedOnProjectFacet () {
 		try {
 			if (((JBossWSAnnotatedClassWizard)this.getWizard()).getProject() == null) {
 				return;
 			}
 			IFacetedProject facetProject =
 					ProjectFacetsManager.create(((JBossWSAnnotatedClassWizard)this.getWizard()).getProject());
             if (facetProject == null) {
                 // then we're not a dynamic web project, do nothing
                 return;
             }
 
             IProjectFacetVersion version = 
 					facetProject.getProjectFacetVersion(IJ2EEFacetConstants.DYNAMIC_WEB_FACET);
 			if (version == null) {
 				// then we're not a dynamic web project, do nothing
 				return;
 			}
 			Double versionDouble = Double.valueOf(version.getVersionString());
 			if (versionDouble.doubleValue() == 3 || versionDouble.doubleValue() > 3) {
 				// dynamic web project 3.0, web.xml not needed
 				updateWebXML.setSelection(false);
 			} else if (versionDouble.doubleValue() < 3){
 				// dynamic web project < 3.0 
 				updateWebXML.setSelection(true);
 			}
 		} catch (CoreException e1) {
 			// ignore
 		} catch (NumberFormatException nfe) {
 			// ignore
 		}
 	}
 
 	private boolean validate() {
 		ServiceModel model = wizard.getServiceModel();
 		if (wizard.isJAXWS()) {
 			setMessage(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Description);
 			setErrorMessage(null);
 
 			JBossWSGenerateWizardValidator.setServiceModel(model);
 			
 			addJarsIfFound.setEnabled(false);
 
 			if (!projects.isDisposed() && projects.getText().length() > 0) {
 				model.setWebProjectName(projects.getText());
 			}
 
 			if (((JBossWSAnnotatedClassWizard) this.getWizard()).getProject() == null) {
 				setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoProjectSelected);
 				return false;
 			}
 
 			try {
 				IFacetedProject facetProject =
 						ProjectFacetsManager.create(((JBossWSAnnotatedClassWizard)this.getWizard()).getProject());
 				if (facetProject == null || facetProject.getProjectFacetVersion(IJ2EEFacetConstants.DYNAMIC_WEB_FACET) == null) {
 					// then we're not a dynamic web project
 					setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
 					return false;
 				}
 			} catch (CoreException e1) {
 				setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
 				return false;
 			}
 			
 			// project not a dynamic web project
 			IFile web = ((JBossWSAnnotatedClassWizard) this.getWizard()).getWebFile();
 			if (web == null || !web.exists()) {
 				if (updateWebXML.getSelection()) {
 					setMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoWebXML, 
 							DialogPage.WARNING);
 					return true;
 				}
 			}
 
 			try {
 				if (""	.equals(JBossWSCreationUtils.getJavaProjectSrcLocation(((JBossWSAnnotatedClassWizard) this.getWizard()).getProject()))) { //$NON-NLS-1$
 					setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoSrcInProject);
 					return false;
 				}
 			} catch (JavaModelException e) {
 				e.printStackTrace();
 			}
 
 			IStatus status = JBossWSGenerateWizardValidator.isWSNameValid();
 			if (status != null) {
 				setErrorMessage(status.getMessage());
 				return false;
 			} 
 			
 			IStatus classNameStatus = JBossWSGenerateWizardValidator.isWSClassValid(model
 					.getCustomClassName(), wizard.getProject());
 			if (classNameStatus != null) {
 				if (classNameStatus.getSeverity() == IStatus.ERROR) {
 					setMessage(classNameStatus.getMessage(), DialogPage.WARNING);
 					setErrorMessage(null);
 //					setErrorMessage(classNameStatus.getMessage());
 					return true;
 				} else if (classNameStatus.getSeverity() == IStatus.WARNING) {
 					setMessage(classNameStatus.getMessage(), DialogPage.WARNING);
 					setErrorMessage(null);
 					return true;
 				}
 			} 
 			
 			setMessage(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Description);
 			setErrorMessage(null);
 			return true;
 		} else {
 			setMessage(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Description);
 			setErrorMessage(null);
 
 			JBossRSGenerateWizardValidator.setServiceModel(model);
 			if (!projects.isDisposed() && projects.getText().length() > 0) {
 				model.setWebProjectName(projects.getText());
 			}
 
 			setErrorMessage(null);
 			// no project selected
 			if (((JBossWSAnnotatedClassWizard) this.getWizard()).getProject() == null) {
 				setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoProjectSelected);
 				return false;
 			}
 
 			try {
 				IFacetedProject facetProject =
 						ProjectFacetsManager.create(((JBossWSAnnotatedClassWizard)this.getWizard()).getProject());
				if (facetProject.getProjectFacetVersion(IJ2EEFacetConstants.DYNAMIC_WEB_FACET) == null) {
 					// then we're not a dynamic web project
 					setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
 					return false;
 				}
 			} catch (CoreException e1) {
 				setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
 				return false;
 			}
 			
 			// project not a dynamic web project
 			IFile web = ((JBossWSAnnotatedClassWizard) this.getWizard()).getWebFile();
 			if (web == null || !web.exists()) {
 				if (updateWebXML.getSelection()) {
 					setMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoWebXML, 
 							DialogPage.WARNING);
 					return true;
 				}
 			}
 
 			IStatus reNoREDirectoryInRuntimeRoot = RestEasyLibUtils.doesRuntimeHaveRootLevelRestEasyDir(
 					((JBossWSAnnotatedClassWizard) this.getWizard()).getProject());
 			addJarsIfFound.setEnabled(reNoREDirectoryInRuntimeRoot.getSeverity() == IStatus.OK);
 			
 			IStatus reInstalledStatus =
 				RestEasyLibUtils.doesRuntimeSupportRestEasy(((JBossWSAnnotatedClassWizard) this.getWizard()).getProject());
 			if (reInstalledStatus.getSeverity() != IStatus.OK && !addJarsIfFound.getSelection()){
 				setMessage(JBossWSUIMessages.JBossRSGenerateWizardPage_Error_RestEasyJarsNotFoundInRuntime, DialogPage.WARNING);
 				return true;
 			}
 
 			// no source folder in web project
 			try {
 				if (""	.equals(JBossWSCreationUtils.getJavaProjectSrcLocation(((JBossWSAnnotatedClassWizard) this.getWizard()).getProject()))) { //$NON-NLS-1$
 					setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoSrcInProject);
 					return false;
 				}
 			} catch (JavaModelException e) {
 				e.printStackTrace();
 			}
 
 			// already has a REST sample installed - can't use wizard again
 			if (wizard.getUpdateWebXML()) {
 				IStatus alreadyHasREST = JBossRSGenerateWizardValidator.RESTAppExists();
 				if (alreadyHasREST != null) {
 					if (alreadyHasREST.getSeverity() == IStatus.ERROR) {
 						setErrorMessage(alreadyHasREST.getMessage());
 						return false;
 					} else if (alreadyHasREST.getSeverity() == IStatus.WARNING) {
 						setMessage(alreadyHasREST.getMessage(), DialogPage.WARNING);
 						setErrorMessage(null);
 						return true;
 					}
 				}
 			} 
 			
 			// Check the service class name
 			IStatus classNameStatus = JBossRSGenerateWizardValidator.isWSClassValid(model
 					.getCustomClassName(), wizard.getProject());
 			if (classNameStatus != null) {
 				if (classNameStatus.getSeverity() == IStatus.ERROR) {
 					setMessage(classNameStatus.getMessage(), DialogPage.WARNING);
 					setErrorMessage(null);
 					return true;
 //					setErrorMessage(classNameStatus.getMessage());
 //					return false;
 				} else if (classNameStatus.getSeverity() == IStatus.WARNING) {
 					setMessage(classNameStatus.getMessage(), DialogPage.WARNING);
 					setErrorMessage(null);
 					return true;
 				}
 			} 
 			
 			// check the application class name
 			IStatus appClassNameStatus = JBossRSGenerateWizardValidator.isAppClassNameValid(
 					model.getCustomPackage() + '.' + model.getApplicationClassName());
 			if (appClassNameStatus != null) {
 				if (appClassNameStatus.getSeverity() == IStatus.ERROR) {
 					setMessage(appClassNameStatus.getMessage(), DialogPage.ERROR);
 					return false;
 				} else if (appClassNameStatus.getSeverity() == IStatus.WARNING) {
 					setMessage(appClassNameStatus.getMessage(), DialogPage.WARNING);
 					return true;
 				}
 			} 
 		}
 		setMessage(JBossWSUIMessages.JBossWSAnnotatedClassWizardPage_PageDescription);
 		setErrorMessage(null);
 		return true;
 	}
 
 	private String[] getProjects() {
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
 				.getProjects();
 		ArrayList<String> dynamicProjects = new ArrayList<String>();
 		for (int i = 0; i < projects.length; i++) {
 			boolean isDynamicWebProject = JavaEEProjectUtilities
 					.isDynamicWebProject(projects[i]);
 			if (isDynamicWebProject) {
 				dynamicProjects.add(projects[i].getName());
 			}
 		}
 		return dynamicProjects.toArray(new String[dynamicProjects.size()]);
 	}
 
 	protected boolean hasChanged() {
 		return bHasChanged;
 	}
 }
