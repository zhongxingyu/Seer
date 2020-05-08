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
  * Created on Dec 3, 2003
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.common.operations;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.Description;
 import org.eclipse.jst.j2ee.common.SecurityRole;
 import org.eclipse.jst.j2ee.ejb.AssemblyDescriptor;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.ejb.EjbPackage;
 import org.eclipse.jst.j2ee.internal.webapplication.WebapplicationPackage;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.common.componentcore.internal.operation.ModelModifierOperation;
 import org.eclipse.wst.common.internal.emfworkbench.integration.ModifierHelper;
 
 
 /**
  * @author DABERG
  * 
  * To change the template for this generated type comment go to Window>Preferences>Java>Code
  * Generation>Code and Comments
  */
 public class AddSecurityRoleOperation extends ModelModifierOperation {
 	/**
 	 * @param dataModel
 	 */
 	public AddSecurityRoleOperation(AddSecurityRoleOperationDataModel dataModel) {
 		super(dataModel);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.operation.ModelModifierOperation#addHelpers()
 	 */
 	protected void addHelpers() {
 		AddSecurityRoleOperationDataModel model = (AddSecurityRoleOperationDataModel) operationDataModel;
 		ModifierHelper helper = createRoleHelper(model);
 		setOwner(helper, model);
 		modifier.addHelper(helper);
 	}
 
 	/**
 	 * @param model
 	 * @return
 	 */
 	private ModifierHelper createRoleHelper(AddSecurityRoleOperationDataModel model) {
 		SecurityRole role = CommonFactory.eINSTANCE.createSecurityRole();
 		role.setRoleName(model.getStringProperty(AddSecurityRoleOperationDataModel.ROLE_NAME));
 		String desc = (String) model.getProperty(AddSecurityRoleOperationDataModel.ROLE_DESCRIPTION);
//		J2EENature nature = J2EENature.getRegisteredRuntime(model.getTargetProject());
 		if (desc != null) {
 			role.setDescription(desc);
//			if (nature.getJ2EEVersion() >= J2EEVersionConstants.J2EE_1_4_ID) {
			EObject root = model.getDeploymentDescriptorRoot();
			int j2eeVertion = 0;
			switch (model.getDeploymentDescriptorType()) {
				case XMLResource.APPLICATION_TYPE :
					j2eeVertion = ((Application)root).getJ2EEVersionID();
					break;
				case XMLResource.EJB_TYPE :
					j2eeVertion = ((EJBJar)root).getJ2EEVersionID();
					break;
				case XMLResource.WEB_APP_TYPE :
					j2eeVertion = ((WebApp)root).getJ2EEVersionID();
					break;
			}
			if (j2eeVertion >= J2EEVersionConstants.J2EE_1_4_ID) {
 				Description description = CommonFactory.eINSTANCE.createDescription();
 				description.setValue(desc);
 				role.getDescriptions().add(description);
 			}
 		}
 		ModifierHelper helper = new ModifierHelper();
 		helper.setValue(role);
 		return helper;
 	}
 
 	private void setOwner(ModifierHelper helper, AddSecurityRoleOperationDataModel model) {
 		EObject root = model.getDeploymentDescriptorRoot();
 		if (root != null) {
 			switch (model.getDeploymentDescriptorType()) {
 				case XMLResource.APPLICATION_TYPE :
 					setOwner(helper, (Application) root);
 					break;
 				case XMLResource.EJB_TYPE :
 					setOwner(helper, (EJBJar) root);
 					break;
 				case XMLResource.WEB_APP_TYPE :
 					setOwner(helper, (WebApp) root);
 					break;
 			}
 		}
 	}
 
 	/**
 	 * @param application
 	 */
 	private void setOwner(ModifierHelper helper, Application application) {
 		helper.setOwner(application);
 		helper.setFeature(ApplicationPackage.eINSTANCE.getApplication_SecurityRoles());
 	}
 
 	/**
 	 * @param ejbJar
 	 */
 	private void setOwner(ModifierHelper helper, EJBJar ejbJar) {
 		helper.setFeature(EjbPackage.eINSTANCE.getAssemblyDescriptor_SecurityRoles());
 		AssemblyDescriptor descriptor = ejbJar.getAssemblyDescriptor();
 		if (descriptor != null) {
 			helper.setOwner(descriptor);
 		} else {
 			ModifierHelper ownerHelper = J2EEModifierHelperCreator.createAssemblyDescriptorHelper(ejbJar);
 			helper.setOwnerHelper(ownerHelper);
 		}
 	}
 
 	/**
 	 * @param webApp
 	 */
 	private void setOwner(ModifierHelper helper, WebApp webApp) {
 		helper.setOwner(webApp);
 		helper.setFeature(WebapplicationPackage.eINSTANCE.getWebApp_SecurityRoles());
 	}
 }
