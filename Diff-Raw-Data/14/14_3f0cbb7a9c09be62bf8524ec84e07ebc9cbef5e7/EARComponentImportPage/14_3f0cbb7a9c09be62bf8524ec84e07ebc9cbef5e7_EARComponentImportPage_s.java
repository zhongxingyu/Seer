 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Dec 8, 2003
  * 
  * To change the template for this generated file go to Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import org.eclipse.jst.j2ee.application.internal.operations.IAnnotationsDataModel;
 import org.eclipse.jst.j2ee.datamodel.properties.IEARComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IEarComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.actions.IJ2EEUIContextIds;
 import org.eclipse.jst.j2ee.internal.earcreation.EarComponentCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.earcreation.IEARNatureConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPluginIcons;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 /**
  * @author cbridgha
  * 
  * To change the template for this generated type comment go to Window>Preferences>Java>Code
  * Generation>Code and Comments
  */
 public class EARComponentImportPage extends J2EEImportPage {
 	protected Combo serverTargetCombo;
 
 	/**
 	 * @param model
 	 * @param pageName
 	 */
 	public EARComponentImportPage(IDataModel dataModel, String pageName) {
 		super(dataModel, pageName);
 		setTitle(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_MAIN_PG_TITLE));
 		setDescription(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_MAIN_PG_DESC));
 		setImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(J2EEUIPluginIcons.EAR_IMPORT_WIZARD_BANNER));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#createTopLevelComposite(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL);
 		setInfopopID(IJ2EEUIContextIds.IMPORT_EAR_WIZARD_P1);
 		GridLayout layout = new GridLayout(3, false);
 		composite.setLayout(layout);
 		createFileNameComposite(composite);
 		createProjectNameComposite(composite);
 		//createServerTargetComposite(composite);
 		createAnnotationsStandaloneGroup(composite);
 		restoreWidgetValues();
 		return composite;
 	}
 
 	protected IDataModel getNewProjectCreationDataModel() {
 		IDataModel earModel = DataModelFactory.createDataModel(new EarComponentCreationDataModelProvider());
 		earModel.setIntProperty(IJ2EEComponentCreationDataModelProperties.COMPONENT_VERSION, model.getIntProperty(IJ2EEComponentCreationDataModelProperties.COMPONENT_VERSION));
 		earModel.setProperty(IJ2EEComponentCreationDataModelProperties.PROJECT_NAME, model.getProperty(IJ2EEComponentCreationDataModelProperties.PROJECT_NAME));
 		earModel.setProperty(IJ2EEComponentCreationDataModelProperties.COMPONENT_NAME, model.getProperty(IJ2EEComponentCreationDataModelProperties.COMPONENT_NAME));
 		return earModel;
 	}
 
 	protected String getProjectImportLabel() {
 		return J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_PROJECT_LABEL);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEImportPage#getFileImportLabel()
 	 */
 	protected String getFileImportLabel() {
 		return J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_FILE_LABEL);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEImportPage#getFilterExpression()
 	 */
 	protected String[] getFilterExpression() {
 		return new String[]{"*.ear"}; //$NON-NLS-1$
 	}
 
 	protected void createServerTargetComposite(Composite parent) {
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.TARGET_SERVER_LBL));
 		serverTargetCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
 		serverTargetCombo.setLayoutData((new GridData(GridData.FILL_HORIZONTAL)));
 		// if (!org.eclipse.jst.j2ee.internal.internal.plugin.J2EEPlugin.hasDevelopmentRole()) {
 		// serverTargetCombo.setEnabled(false);
 		// }
 
 		Button newServerTargetButton = new Button(parent, SWT.NONE);
 		newServerTargetButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		newServerTargetButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.NEW_THREE_DOTS_E));
		// newServerTargetButton.addSelectionListener(new SelectionAdapter() {
		// public void widgetSelected(SelectionEvent e) {
		// FlexibleProjectCreationWizardPage.launchNewRuntimeWizard(getShell(),
		// getJ2EEProjectCreationDataModel().getServerTargetDataModel());
		// }
		// });
 		Control[] deps = new Control[]{label, newServerTargetButton};
 		synchHelper.synchCombo(serverTargetCombo, IEarComponentCreationDataModelProperties.RUNTIME_TARGET_ID, deps);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEModuleImportPage#createAnnotationsStandaloneGroup(org.eclipse.swt.widgets.Composite)
 	 */
 	protected void createAnnotationsStandaloneGroup(Composite composite) {
 		// new AnnotationsStandaloneGroup(composite, model, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#getValidationPropertyNames()
 	 */
 	protected String[] getValidationPropertyNames() {
 		return new String[]{IEARComponentImportDataModelProperties.FILE_NAME, IEARComponentImportDataModelProperties.PROJECT_NAME, IAnnotationsDataModel.USE_ANNOTATIONS};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEImportPage#getFileNamesStoreID()
 	 */
 	protected String getFileNamesStoreID() {
 		return IEARNatureConstants.NATURE_ID;
 	}
 
 
 
 }
