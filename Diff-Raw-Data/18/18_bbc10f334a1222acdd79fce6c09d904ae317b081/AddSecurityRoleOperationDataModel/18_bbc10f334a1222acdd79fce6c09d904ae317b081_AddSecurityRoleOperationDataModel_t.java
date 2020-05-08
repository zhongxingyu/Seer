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
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.ejb.AssemblyDescriptor;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 
 /**
  * @author DABERG
  * 
  * To change the template for this generated type comment go to Window>Preferences>Java>Code
  * Generation>Code and Comments
  */
 public class AddSecurityRoleOperationDataModel extends J2EEModelModifierOperationDataModel {
	private EObject ddRoot;
 	/**
 	 * Required - The name of the new role.
 	 * 
 	 * @see String
 	 */
 	public static final String ROLE_NAME = "AddSecurityRoleOperationDataModel.ROLE_NAME"; //$NON-NLS-1$
 
 	/**
 	 * Optional - The description of the new role.
 	 * 
 	 * @see String
 	 */
 	public static final String ROLE_DESCRIPTION = "AddSecurityRoleOperationDataModel.ROLE_DESCRIPTION"; //$NON-NLS-1$
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#getDefaultOperation()
 	 */
 	public WTPOperation getDefaultOperation() {
 		return new AddSecurityRoleOperation(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.operation.ModelModifierOperationDataModel#initValidBaseProperties()
 	 */
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		addValidBaseProperty(ROLE_NAME);
 		addValidBaseProperty(ROLE_DESCRIPTION);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doValidateProperty(java.lang.String)
 	 */
 	protected IStatus doValidateProperty(String propertyName) {
 		if (propertyName.equals(ROLE_NAME))
 			return validateRoleName(getStringProperty(propertyName));
 		return super.doValidateProperty(propertyName);
 	}
 
 	/**
 	 * @param roleName
 	 * @return
 	 */
 	private IStatus validateRoleName(String roleName) {
 		if (roleName.length() == 0) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_SECURITY_ROLE_EMPTY);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
		if (ddRoot == null)
			ddRoot = getDeploymentDescriptorRoot();
		boolean exists = roleExists(roleName);
 		if (exists) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_SECURITY_ROLE_EXIST, new String[]{roleName});
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * @param roleName
 	 * @param root
 	 * @return
 	 */
	private boolean roleExists(String roleName) {
 		switch (getDeploymentDescriptorType()) {
 			case XMLResource.APPLICATION_TYPE :
				return roleExists(roleName, (Application) ddRoot);
 			case XMLResource.EJB_TYPE :
				return roleExists(roleName, (EJBJar) ddRoot);
 			case XMLResource.WEB_APP_TYPE :
				return roleExists(roleName, (WebApp) ddRoot);
 		}
 		return false;
 	}
 
 	/**
 	 * @param roleName
 	 * @param jar
 	 * @return
 	 */
 	private boolean roleExists(String roleName, EJBJar jar) {
 		AssemblyDescriptor descriptor = jar.getAssemblyDescriptor();
 		return descriptor != null && descriptor.getSecurityRoleNamed(roleName) != null;
 	}
 
 	/**
 	 * @param roleName
 	 * @param application
 	 * @return
 	 */
 	private boolean roleExists(String roleName, Application application) {
 		return application.getSecurityRoleNamed(roleName) != null;
 	}
 
 	/**
 	 * @param roleName
 	 * @param webApp
 	 * @return
 	 */
 	private boolean roleExists(String roleName, WebApp webApp) {
 		return webApp.getSecurityRoleNamed(roleName) != null;
 	}
 }
