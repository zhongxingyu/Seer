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
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.dialogs.DialogPage;
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
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 import org.jboss.tools.ws.creation.core.utils.RestEasyLibUtils;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 
 public class JBossRSGenerateWizardPage extends WizardPage {
 
 	private JBossRSGenerateWizard wizard;
 	private Text name;
 	private Combo projects;
 	private boolean bHasChanged = false;
 	private Text packageName;
 	private Text className;
 	private Text appClassName;
 	private Button updateWebXML;
 	private Button addJarsIfFound;
 
 	protected JBossRSGenerateWizardPage(String pageName) {
 		super(pageName);
 		this
 				.setTitle(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Title);
 		this
 				.setDescription(JBossWSUIMessages.JBossRSGenerateWizardPage_Page_title);
 	}
 
 	public void createControl(Composite parent) {
 		Composite composite = createDialogArea(parent);
 		this.wizard = (JBossRSGenerateWizard) this.getWizard();
 
 		Group group = new Group(composite, SWT.NONE);
 		group
 				.setText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Project_Group);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		group.setLayout(new GridLayout(2, false));
 		group.setLayoutData(gd);
 
 		projects = new Combo(group, SWT.BORDER | SWT.DROP_DOWN);
 		projects
 				.setToolTipText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Project_Group_Tooltip);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		projects.setLayoutData(gd);
 		refreshProjectList(wizard.getServiceModel().getWebProjectName());
 
 		projects.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				wizard.setProject(projects.getText());
 				name.setText(updateDefaultName());
 				className.setText(updateDefaultClassName());
 				setWebXMLSelectionValueBasedOnProjectFacet();
 				bHasChanged = true;
 				setPageComplete(isPageComplete());
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		Group group2 = new Group(composite, SWT.NONE);
 		group2
 				.setText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Web_Service_Group);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		group2.setLayout(new GridLayout(2, false));
 		group2.setLayoutData(gd);
 		
 		new Label(group2, SWT.NONE)
 				.setText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_ServiceName_Label);
 		name = new Text(group2, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		name.setLayoutData(gd);
 		name.setText(updateDefaultName());
 		name.setToolTipText(JBossWSUIMessages.JBossRSGenerateWizardPage_ServiceName_Tooltip);
 		name.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				wizard.setServiceName(name.getText());
 				bHasChanged = true;
 				setPageComplete(isPageComplete());
 			}
 
 		});
 
 		updateWebXML = new Button(group2, SWT.CHECK);
 		updateWebXML.setText(JBossWSUIMessages.JBossRSGenerateWizardPage_UpdateWebXMLCheckbox);
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
 
 		addJarsIfFound = new Button(group2, SWT.CHECK);
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
 
 		Group group3 = new Group(composite, SWT.NONE);
 		group3.setText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Class_Group);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		group3.setLayout(new GridLayout(2, false));
 		group3.setLayoutData(gd);
 		
 		new Label(group3, SWT.NONE)
 				.setText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Package_Label);
 		packageName = new Text(group3, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		packageName.setLayoutData(gd);
 		packageName.setText(wizard.getPackageName());
 		packageName.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setPackageName(packageName.getText());
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 		
 		new Label(group3, SWT.NONE)
 				.setText(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_ClassName_Label);
 		className = new Text(group3, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		className.setLayoutData(gd);
 		className.setText(updateDefaultClassName());
 		className.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setClassName(className.getText());
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 
 		new Label(group3, SWT.NONE)
 			.setText(JBossWSUIMessages.JBossRSGenerateWizardPage_Label_Application_Class_Name);
 		appClassName = new Text(group3, SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		appClassName.setLayoutData(gd);
 		appClassName.setText(updateDefaultAppClassName());
 		appClassName.addModifyListener(new ModifyListener() {
 		
 			public void modifyText(ModifyEvent e) {
 				wizard.setAppClassName(appClassName.getText());
 				setPageComplete(isPageComplete());
 			}
 		
 		});
 
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
 
 	private String updateDefaultName() {
 		ServiceModel model = wizard.getServiceModel();
 		JBossWSGenerateWizardValidator.setServiceModel(model);
 		String currentName = wizard.getServiceName();
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
 	
 	private String updateDefaultAppClassName() {
 		ServiceModel model = wizard.getServiceModel();
 		JBossRSGenerateWizardValidator.setServiceModel(model);
 		String currentName = wizard.getAppClassName();
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
 				model.getCustomPackage() + '.' + model.getApplicationClassName());
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
 
 	private String updateDefaultClassName() {
 		ServiceModel model = wizard.getServiceModel();
 		JBossWSGenerateWizardValidator.setServiceModel(model);
 		String currentName = wizard.getClassName();
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
 
 	private void setWebXMLSelectionValueBasedOnProjectFacet () {
 		try {
 			if (((JBossRSGenerateWizard)this.getWizard()).getProject() == null) {
 				return;
 			}
 			IFacetedProject facetProject =
 					ProjectFacetsManager.create(((JBossRSGenerateWizard)this.getWizard()).getProject());
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
 		JBossRSGenerateWizardValidator.setServiceModel(model);
 		if (!projects.isDisposed() && projects.getText().length() > 0) {
 			model.setWebProjectName(projects.getText());
 		}
 
 		setErrorMessage(null);
 		// no project selected
 		if (((JBossRSGenerateWizard) this.getWizard()).getProject() == null) {
 			setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoProjectSelected);
 			return false;
 		}
 
 		try {
 			IFacetedProject facetProject =
 					ProjectFacetsManager.create(((JBossRSGenerateWizard)this.getWizard()).getProject());
            if (facetProject == null) {
                // then we're not a dynamic web project
                setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
                return false;
            }
 			IProjectFacetVersion version = 
 					facetProject.getProjectFacetVersion(IJ2EEFacetConstants.DYNAMIC_WEB_FACET);
 			if (version == null) {
 				// then we're not a dynamic web project
 				setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
 				return false;
 			}
 		} catch (CoreException e1) {
 			setErrorMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NotDynamicWebProject2);
 			return false;
 		}
 		
 		// project not a dynamic web project
 		IFile web = ((JBossRSGenerateWizard) this.getWizard()).getWebFile();
 		if (web == null || !web.exists()) {
 			if (updateWebXML.getSelection()) {
 				setMessage(JBossWSUIMessages.Error_JBossWS_GenerateWizard_NoWebXML, 
 						DialogPage.WARNING);
 				return true;
 			}
 		}
 		
 		IStatus reNoREDirectoryInRuntimeRoot = RestEasyLibUtils.doesRuntimeHaveRootLevelRestEasyDir(((JBossRSGenerateWizard) this.getWizard()).getProject());
 		addJarsIfFound.setEnabled(reNoREDirectoryInRuntimeRoot.getSeverity() == IStatus.OK);
 
 		IStatus reInstalledStatus =
 			RestEasyLibUtils.doesRuntimeSupportRestEasy(((JBossRSGenerateWizard) this.getWizard()).getProject());
 		if (reInstalledStatus.getSeverity() != IStatus.OK && !addJarsIfFound.getSelection()){
 			setMessage(JBossWSUIMessages.JBossRSGenerateWizardPage_Error_RestEasyJarsNotFoundInRuntime, 
 					DialogPage.WARNING);
 			return true;
 		}
 		
 		// no source folder in web project
 		try {
 			if ("".equals(JBossWSCreationUtils.getJavaProjectSrcLocation(((JBossRSGenerateWizard) this.getWizard()).getProject()))) { //$NON-NLS-1$
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
 					setMessage(alreadyHasREST.getMessage(), DialogPage.ERROR);
 					return false;
 				} else if (alreadyHasREST.getSeverity() == IStatus.WARNING) {
 					setMessage(alreadyHasREST.getMessage(), DialogPage.WARNING);
 					return true;
 				}
 			}
 		} 
 		
 		// Check the service class name
 		IStatus classNameStatus = JBossRSGenerateWizardValidator.isWSClassValid(model
 				.getCustomClassName(), wizard.getProject());
 		if (classNameStatus != null) {
 			if (classNameStatus.getSeverity() == IStatus.ERROR) {
 				setMessage(classNameStatus.getMessage(), DialogPage.ERROR);
 				return false;
 			} else if (classNameStatus.getSeverity() == IStatus.WARNING) {
 				setMessage(classNameStatus.getMessage(), DialogPage.WARNING);
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
 
 		setMessage(JBossWSUIMessages.JBossWS_GenerateWizard_GenerateWizardPage_Description);
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
