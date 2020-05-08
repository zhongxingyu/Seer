 /*******************************************************************************
  * Copyright (c) 2007 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.ws.creation.ui.project.facet;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EventObject;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.common.project.facet.ui.AbstractFacetWizardPage;
 import org.eclipse.wst.common.project.facet.ui.IFacetWizardPage;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntime;
 import org.jboss.tools.ws.core.classpath.JBossWSRuntimeManager;
 import org.jboss.tools.ws.core.facet.delegate.IJBossWSFacetDataModelProperties;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.ui.CreationUIPlugin;
 import org.jboss.tools.ws.ui.preferences.JBossRuntimeListFieldEditor;
 
 /**
  * @author Dennyxu
  * 
  */
 public class JBossWSFacetInstallPage extends AbstractFacetWizardPage implements
 		IFacetWizardPage, IDataModelListener {
 
 	private Button btnServerSupplied;
 	private Button btnUserSupplied;
 	private Combo cmbRuntimes;
 	private Button btnDeploy;
 	private Button btnNew;
 
 	private IDataModel model;
 
 	public JBossWSFacetInstallPage() {
 		super(JBossWSCreationCoreMessages.JBossWSFacetInstallPage_Title);
 		setTitle(JBossWSCreationCoreMessages.JBossWSFacetInstallPage_Title);
 		setDescription(JBossWSCreationCoreMessages.JBossWSFacetInstallPage_Description);
 	}
 
 	public void setConfig(Object config) {
 		this.model = (IDataModel) config;
 
 	}
 	
 	private void setInitialValues(){
 		boolean isServerSupplied = model.getBooleanProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_IS_SERVER_SUPPLIED);
 		String runtimeName = model.getStringProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_ID);
 		boolean isDeploy = model.getBooleanProperty(IJBossWSFacetDataModelProperties.JBOSS_WS_DEPLOY);
 		if(isServerSupplied){
 			btnServerSupplied.setSelection(true);
 		}else if(runtimeName != null && !runtimeName.equals("")){
 			btnUserSupplied.setSelection(true);
 			if(isDeploy){
 				btnDeploy.setSelection(true);
 			}
 		}
 		initializeRuntimesCombo(cmbRuntimes, runtimeName);
 			
 	}
 
 	public void createControl(Composite parent) {
 		initializeDialogUnits(parent);
 
 		Composite composite = new Composite(parent, SWT.NONE);
 
 		GridLayout gridLayout = new GridLayout(4, false);
 		composite.setLayout(gridLayout);
 
 		btnServerSupplied = new Button(composite, SWT.RADIO);
 		btnServerSupplied.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				setServerSuppliedSelection(e);
 			}
 		});
 		GridData gd = new GridData();
 
 		gd.horizontalSpan = 1;
 		btnServerSupplied.setLayoutData(gd);
 
 		Label lblServerSupplied = new Label(composite, SWT.NONE);
 		lblServerSupplied.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				btnServerSupplied.setSelection(true);
 				setServerSuppliedSelection(e);
 			}
 		});
 		lblServerSupplied.setText(JBossWSCreationCoreMessages.JBossWSFacetInstallPage_ServerSuppliedJBossWS);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 3;
 		lblServerSupplied.setLayoutData(gd);
 
 		btnUserSupplied = new Button(composite, SWT.RADIO);
 		
 		btnUserSupplied.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				setUserSuppliedSelection(e);
 			}
 		});
 
 		cmbRuntimes = new Combo(composite, SWT.READ_ONLY);
 		cmbRuntimes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		cmbRuntimes.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				String runtimeName = cmbRuntimes.getText();
 				JBossWSRuntime jr = (JBossWSRuntime) cmbRuntimes
 						.getData(runtimeName);
 				saveJBosswsRuntimeToModel(jr);
 			}
 		});
 
 		btnDeploy = new Button(composite, SWT.CHECK);
 		btnDeploy.setText(JBossWSCreationCoreMessages.JBossWSFacetInstallPage_Deploy);
 		btnDeploy.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				model.setBooleanProperty(
 						IJBossWSFacetDataModelProperties.JBOSS_WS_DEPLOY,
 						btnDeploy.getSelection());
 			}
 		});
 
 		btnNew = new Button(composite, SWT.NONE);
 		btnNew.setText(JBossWSCreationCoreMessages.JBossWSFacetInstallPage_New);
 		btnNew.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				newJBossWSRuntime();
 				changePageStatus();
 			}
 		});
 		
 		setInitialValues();
 		setControl(composite);
 		changePageStatus();
 
 	}
 	
 	protected void saveJBosswsRuntimeToModel(JBossWSRuntime jbws) {
 		String duplicateMsg = "";
 		try {
 			duplicateMsg = getDuplicateJars(jbws.getName());
 		} catch (JavaModelException e1) {
 			CreationUIPlugin.getDefault().getLog().log(
 					StatusUtils.errorStatus(e1));
 		}
 		if ("".equals(duplicateMsg)) {
 			model.setStringProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_HOME,
 					jbws.getHomeDir());
 			model.setStringProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_ID, jbws
 							.getName());
 		}else{
 			model.setStringProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_ID, null);
 			model.setStringProperty(
 					IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_HOME,	null);	
 		}
 	}
 
 	protected void setServerSuppliedSelection(EventObject e) {
 		btnServerSupplied.setSelection(true);
 		btnUserSupplied.setSelection(false);
 		model
 				.setBooleanProperty(
 						IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_IS_SERVER_SUPPLIED,
 						true);
 		//remove user supplied properties
 		model.setStringProperty(
 				IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_ID, null);
 		model.setStringProperty(
 				IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_HOME,	null);		
 		enableUserSupplied(false);		
 		changePageStatus();
 
 	}
 
 	protected void setUserSuppliedSelection(EventObject e) {
 		btnServerSupplied.setSelection(false);
 		btnUserSupplied.setSelection(true);
 		model
 				.setBooleanProperty(
 						IJBossWSFacetDataModelProperties.JBOSS_WS_RUNTIME_IS_SERVER_SUPPLIED,
 						false);
 		String runtimeId = cmbRuntimes.getText();		
 		JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance().findRuntimeByName(runtimeId);
 		
 		
 		if (jbws != null) {
 			saveJBosswsRuntimeToModel(jbws);
 		}
 		enableUserSupplied(true);
 		changePageStatus();
 
 	}
 
 	protected void enableUserSupplied(boolean enabled) {
 		cmbRuntimes.setEnabled(enabled);
 		btnDeploy.setEnabled(enabled);
 		btnNew.setEnabled(enabled);
 
 	}
 
 	protected void initializeRuntimesCombo(Combo cmRuntime, String runtimeName) {
 		JBossWSRuntime selectedJbws = null;
 		JBossWSRuntime defaultJbws = null;
 		int selectIndex = 0;
 		int defaultIndex = 0;
 		cmRuntime.removeAll();
 		JBossWSRuntime[] runtimes = JBossWSRuntimeManager.getInstance()
 				.getRuntimes();
 		for (int i = 0; i < runtimes.length; i++) {
 			JBossWSRuntime jr = runtimes[i];
 			cmRuntime.add(jr.getName());
 			cmRuntime.setData(jr.getName(), jr);
 			
 			if(jr.getName().equals(runtimeName)){
 				selectedJbws = jr;
 				selectIndex = i;
 			}
 			// get default jbossws runtime
 			if (jr.isDefault()) {
 				defaultJbws = jr;
 				defaultIndex = i;
 			}
 		}
 		
 		if(selectedJbws != null){
 		cmRuntime.select(selectIndex);
 		saveJBosswsRuntimeToModel(selectedJbws);
		}else if(defaultJbws != null){
 			cmRuntime.select(defaultIndex);
 			saveJBosswsRuntimeToModel(defaultJbws);
 		}
 	}
 
 	/*
 	 * create a new jbossws runtime and set user supplied runtime to the new one
 	 */
 	protected void newJBossWSRuntime() {
 		List<JBossWSRuntime> exists = new ArrayList<JBossWSRuntime>(Arrays.asList(JBossWSRuntimeManager.getInstance().getRuntimes()));
 		List<JBossWSRuntime> added = new ArrayList<JBossWSRuntime>();
 		
 		JBossRuntimeListFieldEditor.JBossWSRuntimeNewWizard newRtwizard = new JBossRuntimeListFieldEditor.JBossWSRuntimeNewWizard(
 				exists, added) {
 			public boolean performFinish() {
 				JBossWSRuntime rt = getRuntime();
 				rt.setDefault(true);
 				JBossWSRuntimeManager.getInstance().addRuntime(rt);
 				JBossWSRuntimeManager.getInstance().save();
 
 				return true;
 			}
 		};
 		WizardDialog dialog = new WizardDialog(Display.getCurrent()
 				.getActiveShell(), newRtwizard);
 		if (dialog.open() == WizardDialog.OK) {
 			initializeRuntimesCombo(cmbRuntimes, null);
 			//cmbRuntimes.select(0);
 		}
 	}
 
 	protected void changePageStatus() {
 		
 		if (btnUserSupplied.getSelection()
 				&& cmbRuntimes.getSelectionIndex() == -1) {
 			setErrorMessage(JBossWSCreationCoreMessages.Error_WS_No_Runtime_Specifed);
 		} else if (!btnUserSupplied.getSelection()
 				&& !btnServerSupplied.getSelection()) {
 			setErrorMessage(JBossWSCreationCoreMessages.Error_WS_Chose_runtime);
 		}else if(btnUserSupplied.getSelection()){
 			String duplicateMsg = "";
 			try {
 				duplicateMsg = getDuplicateJars(cmbRuntimes.getText());
 			} catch (JavaModelException e1) {
 				CreationUIPlugin.getDefault().getLog().log(StatusUtils.errorStatus(e1));
 			}
 			if(!duplicateMsg.equals("")){
 				setErrorMessage("Duplicated jar on classpath:" + duplicateMsg);
 			}else{
 				setErrorMessage(null);
 			}
 		}else{
 			setErrorMessage(null);
 		}
 			
 		setPageComplete(isPageComplete());
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		if (btnServerSupplied.getSelection()
 				|| (btnUserSupplied.getSelection() && cmbRuntimes
 						.getSelectionIndex() != -1))  {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public void propertyChanged(DataModelEvent event) {
 
 	}
 	
 	protected String getDuplicateJars(String jbwsName) throws JavaModelException{
 		List<String> allExistingJars = new ArrayList<String>();
 		List<String> runtimeJars = new ArrayList<String>();
 
 		JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance().findRuntimeByName(jbwsName);
 		if(jbws.isUserConfigClasspath()){
 			runtimeJars.addAll(jbws.getLibraries());
 		}else{
 			runtimeJars.addAll(JBossWSRuntimeManager.getInstance().getAllRuntimeJars(jbws));
 		}
 		
 		String prjName = model.getStringProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);
 		IJavaProject javaProject = JavaCore.create(project);
 		IClasspathEntry[] entries = javaProject.getRawClasspath();
 		for(IClasspathEntry entry: entries){
 			if(entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
 				IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), javaProject);
 				for(IClasspathEntry containedEntry: container.getClasspathEntries()){
 					allExistingJars.add(containedEntry.getPath().toOSString());
 				}
 			}else if(entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY){
 				allExistingJars.add(entry.getPath().toOSString());
 			}
 		}
 		
 		for(String jarName: runtimeJars){
 			if(allExistingJars.contains(jarName)){
 				return jarName;
 			}
 		}
 		
 		 return "";
 		
 	}
 
 }
