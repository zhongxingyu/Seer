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
 package org.eclipse.jst.servlet.ui.internal.wizard;
 
 import org.eclipse.jst.j2ee.application.internal.operations.J2EEComponentCreationDataModel;
 import org.eclipse.jst.j2ee.application.internal.operations.J2EEModuleImportDataModel;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPluginIcons;
 import org.eclipse.jst.j2ee.internal.web.archive.operations.WebModuleImportDataModel;
 import org.eclipse.jst.j2ee.internal.web.archive.operations.WebModuleImportOperation;
 import org.eclipse.jst.j2ee.internal.wizard.J2EEModuleImportWizard;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel;
import org.eclipse.wst.common.modulecore.internal.operation.ComponentCreationDataModel;
 
 /**
  * <p>
  * Wizard used to import J2EE Web Application module 
  * structures into the Eclipse Workbench from an existing
  * Web Application *.war file.  
  * </p>
  */
 public final class WebModuleImportWizard extends J2EEModuleImportWizard { 
 	
 	/**
 	 * <p>
 	 * The default constructor. Creates a wizard with no selection, 
 	 * no model instance, and no operation instance. The model and 
 	 * operation will be created as needed.
 	 * </p>
 	 */
 	public WebModuleImportWizard() {
 		super();
 	}
 
 	/**
 	 * <p>
 	 * The model is used to prepopulate the wizard controls
 	 * and interface with the operation.
 	 * </p>
 	 * @param model The model parameter is used to pre-populate wizard controls and interface with the operation
 	 */
 	public WebModuleImportWizard(WebModuleImportDataModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * Overridden to return an {@link WARImportDataModel} and defaults
 	 * the value of {@link J2EEModuleImportDataModel#ADD_TO_EAR} to <b>true</b>
 	 * </p>
 	 *  
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizard#createDefaultModel()
 	 */
 	protected WTPOperationDataModel createDefaultModel() {
 		WebModuleImportDataModel aModel = new WebModuleImportDataModel();
 		aModel.setBooleanProperty(J2EEModuleImportDataModel.ADD_TO_EAR, true);
 		return aModel;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizard#createOperation()
 	 */
 	protected WTPOperation getImportOperation() {
 		return new WebModuleImportOperation(getSpecificDataModel());
 	}
 
 	/**
 	 * <p>
 	 * Adds an {@link WARImportPage} as the main wizard page ({@link #MAIN_PG}).
 	 * </p>
 	 */
 	public void doAddPages() {
 		addPage(new WARImportPage(getSpecificDataModel(), MAIN_PG));
 	}
 
 	/**
 	 * {@inheritDoc}   
 	 * 
 	 * <p>
 	 * Sets up the dialog window title and default wizard page image. 
 	 * </p>
 	 */
 	public final void doInit() {
 		setWindowTitle(J2EEUIMessages.getResourceString(J2EEUIMessages.IMPORT_WIZ_TITLE));
 		setDefaultPageImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(J2EEUIPluginIcons.WEB_IMPORT_WIZARD_BANNER));
 		updateEARToModelFromSelection(getSpecificDataModel());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEModuleImportWizard#getModuleValidatorStrings()
 	 */
 	protected String[] getModuleValidatorStrings() {
 		return new String[]{"org.eclipse.jst.j2ee.internal.web.validation.UIWarValidator"}; //$NON-NLS-1$
 	}
  
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.internal.wizard.J2EEModuleImportWizard#getFinalPerspectiveID()
 	 */
 	protected String getFinalPerspectiveID() {
 		J2EEComponentCreationDataModel projCreationModel = getSpecificDataModel().getJ2EEModuleCreationDataModel();
		return projCreationModel.getStringProperty(ComponentCreationDataModel.FINAL_PERSPECTIVE);
 	}
  
 	private WebModuleImportDataModel getSpecificDataModel() {
 		return (WebModuleImportDataModel) getModel();
 	}
 
 }
