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
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.java.JavaRefFactory;
 import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEEditorUtility;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.web.operations.NewServletClassDataModelProvider;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.servlet.ui.IWebUIContextIds;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;
 
 /**
  * New servlet wizard
  */
 public class AddServletWizard extends NewWebWizard {
 	private static final String PAGE_ONE = "pageOne"; //$NON-NLS-1$
 	private static final String PAGE_TWO = "pageTwo"; //$NON-NLS-1$
 	private static final String PAGE_THREE = "pageThree"; //$NON-NLS-1$
 	/**
 	 * @param model
 	 */
 	public AddServletWizard(IDataModel model) {
 		super(model);
 		setWindowTitle(IWebWizardConstants.ADD_SERVLET_WIZARD_WINDOW_TITLE);
 		setDefaultPageImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor("newservlet_wiz")); //$NON-NLS-1$
 	}
 	
 	public AddServletWizard() {
 	    this(null);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.Wizard#addPages()
 	 */
 	public void doAddPages() {
 		
 		NewServletClassWizardPage page1 = new NewServletClassWizardPage(
 				getDataModel(), 
 				PAGE_ONE,
 				IWebWizardConstants.NEW_JAVA_CLASS_DESTINATION_WIZARD_PAGE_DESC,
 				IWebWizardConstants.ADD_SERVLET_WIZARD_PAGE_TITLE, IModuleConstants.JST_WEB_MODULE);
 		page1.setInfopopID(IWebUIContextIds.WEBEDITOR_SERVLET_PAGE_ADD_SERVLET_WIZARD_2);
 		addPage(page1);
 		AddServletWizardPage page2 = new AddServletWizardPage(getDataModel(), PAGE_TWO);
 		page2.setInfopopID(IWebUIContextIds.WEBEDITOR_SERVLET_PAGE_ADD_SERVLET_WIZARD_1);
 		addPage(page2);
 		NewServletClassOptionsWizardPage page3 = new NewServletClassOptionsWizardPage(
 				getDataModel(), 
 				PAGE_THREE,
 				IWebWizardConstants.NEW_JAVA_CLASS_OPTIONS_WIZARD_PAGE_DESC,
 				IWebWizardConstants.ADD_SERVLET_WIZARD_PAGE_TITLE);
 		page3.setInfopopID(IWebUIContextIds.WEBEDITOR_SERVLET_PAGE_ADD_SERVLET_WIZARD_3);
 		addPage(page3);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.util.ui.wizard.WTPWizard#runForked()
 	 */
 	protected boolean runForked() {
 		return false;
 	}
 	
 	public boolean canFinish() {
 		return getDataModel().isValid();
 	}
 	
 	protected void postPerformFinish() throws InvocationTargetException {
 		//open new servlet class in java editor
 		WebArtifactEdit artifactEdit = null;
 		try {
 			JavaClass javaClass = null;
 			String className = getDataModel().getStringProperty(INewJavaClassDataModelProperties.QUALIFIED_CLASS_NAME);
 			IProject p = (IProject) getDataModel().getProperty(INewJavaClassDataModelProperties.PROJECT);
 			IVirtualComponent component = ComponentCore.createComponent(p);
 			artifactEdit = WebArtifactEdit.getWebArtifactEditForRead(component);
 			ResourceSet resourceSet = artifactEdit.getDeploymentDescriptorResource().getResourceSet();
 			javaClass = (JavaClass) JavaRefFactory.eINSTANCE.reflectType(className,resourceSet);
 			J2EEEditorUtility.openInEditor(javaClass, p );
 		} catch (Exception cantOpen) {
 			cantOpen.printStackTrace();
 		} finally {
 			if (artifactEdit!=null)
 				artifactEdit.dispose();
 		}	
 	}
 
 	protected IDataModelProvider getDefaultProvider() {
 		return new NewServletClassDataModelProvider();
 	}
 }
