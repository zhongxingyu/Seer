 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 /*
  * Created on Mar 24, 2004
  * 
  * To change the template for this generated file go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.reference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.ApplicationClientNatureRuntime;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.common.MessageDestination;
 import org.eclipse.jst.j2ee.ejb.AssemblyDescriptor;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
import org.eclipse.jst.j2ee.ejb.MessageDriven;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.earcreation.EARNatureRuntime;
 import org.eclipse.jst.j2ee.internal.moduleextension.EarModuleManager;
 import org.eclipse.jst.j2ee.internal.project.IEJBNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.IWebNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EECreationResourceHandler;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 public class MessageDestReferenceDataModel extends ReferenceDataModel {
 	/**
 	 * Required, type String
 	 */
 	public static final String TARGET = "MessageDestReferenceDataModel.TARGET"; //$NON-NLS-1$
 	/**
 	 * Required, type String
 	 */
 	public static final String TYPE = "MessageDestReferenceDataModel.TYPE"; //$NON-NLS-1$
 	/**
 	 * Required, type String
 	 */
 	public static final String LINK = "MessageDestReferenceDataModel.LINK"; //$NON-NLS-1$
 	/**
 	 * Required, type String
 	 */
 	public static final String USAGE = "MessageDestReferenceDataModel.USAGE"; //$NON-NLS-1$
 	/**
 	 * Required, type String
 	 */
 	public static final String HAS_LINK = "MessageDestReferenceDataModel.HAS_LINK"; //$NON-NLS-1$
 	/**
 	 * Required, type String
 	 */
 	public static final String TARGET_IN_DIFFERENT_EAR = "MessageDestReferenceDataModel.TARGET_IN_DIFFERENT_EAR"; //$NON-NLS-1$
 
 
 	private List messageDestinationNames;
 
 	protected void init() {
 		super.init();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.operation.EditModelOperationDataModel#initValidBaseProperties()
 	 */
 	protected void initValidBaseProperties() {
 		addValidBaseProperty(TARGET);
 		addValidBaseProperty(TYPE);
 		addValidBaseProperty(LINK);
 		addValidBaseProperty(USAGE);
 		addValidBaseProperty(HAS_LINK);
 		addValidBaseProperty(TARGET_IN_DIFFERENT_EAR);
 		super.initValidBaseProperties();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#getDefaultProperty(java.lang.String)
 	 */
 	protected Object getDefaultProperty(String propertyName) {
 		return super.getDefaultProperty(propertyName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.reference.ReferenceDataModel#doValidateProperty(java.lang.String)
 	 */
 	protected IStatus doValidateProperty(String propertyName) {
 		IStatus status = super.doValidateProperty(propertyName);
 		if (TYPE.equals(propertyName) || USAGE.equals(propertyName)) {
 			String temp = getStringProperty(propertyName);
 			if (temp == null || temp.trim().equals("")) { //$NON-NLS-1$
 				if (TYPE.equals(propertyName)) {
 					return WTPCommonPlugin.createErrorStatus(J2EECreationResourceHandler.getString("MessageDestReferenceDataModel.7")); //$NON-NLS-1$
 				} else if (USAGE.equals(propertyName)) {
 					return WTPCommonPlugin.createErrorStatus(J2EECreationResourceHandler.getString("MessageDestReferenceDataModel.8")); //$NON-NLS-1$
 				}
 			}
 		} else if (LINK.equals(propertyName) && !getBooleanProperty(HAS_LINK)) {
 			if (!verifyLinkUnique(getStringProperty(LINK)))
 				return WTPCommonPlugin.createErrorStatus(J2EECreationResourceHandler.getString("MessageDestReferenceDataModel.9")); //$NON-NLS-1$
 		} else if (propertyName.equals(TARGET)) {
 			Object target = getProperty(TARGET);
			if (target == null || !(target instanceof EnterpriseBean))
 				return WTPCommonPlugin.createErrorStatus(J2EECreationResourceHandler.getString("EJBReferenceDataModel_UI_12")); //$NON-NLS-1$
 		}
 		return status;
 	}
 
 	/**
 	 * @param string
 	 */
 	private boolean verifyLinkUnique(String link) {
 		if (messageDestinationNames == null) {
 			EObject owner = (EObject) getProperty(OWNER);
 			IProject containingProject = J2EEProjectUtilities.getProject(owner);
 			EARNatureRuntime[] ears = J2EEProjectUtilities.getReferencingEARProjects(containingProject);
 			if (ears == null) {
 				messageDestinationNames = getModuleMessageDestinationNames(owner);
 			} else {
 				messageDestinationNames = new ArrayList();
 				for (int j = 0; j < ears.length; j++) {
 					try {
 						IProject[] refProj = ears[j].getProject().getReferencedProjects();
 						if (refProj != null) {
 							for (int k = 0; k < refProj.length; k++) {
 								messageDestinationNames.addAll(getModuleMessageDestinationNames(refProj[k]));
 							}
 						}
 					} catch (CoreException e) {
 						Logger.getLogger().log(e.getMessage());
 					}
 				}
 			}
 		}
 		if (messageDestinationNames == null || messageDestinationNames.isEmpty())
 			return true;
 		String name;
 		for (int i = 0; i < messageDestinationNames.size(); i++) {
 			name = ((MessageDestination) messageDestinationNames.get(i)).getName();
 			if (name != null && name.equals(link))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param project
 	 * @return
 	 */
 	private List getModuleMessageDestinationNames(IProject project) throws CoreException {
 		if (project.hasNature(ApplicationClientNatureRuntime.NATURE_ID)) {
 			J2EENature runtime = (J2EENature) project.getNature(ApplicationClientNatureRuntime.NATURE_ID);
 			ApplicationClient appClient = (ApplicationClient) runtime.getDeploymentDescriptorRoot();
 			if (appClient != null)
 				return appClient.getMessageDestinations();
 		} else if (project.hasNature(IWebNatureConstants.J2EE_NATURE_ID)) {
 			J2EENature runtime = (J2EENature) project.getNature(IWebNatureConstants.J2EE_NATURE_ID);
 			WebApp webApp = (WebApp) runtime.getDeploymentDescriptorRoot();
 			if (webApp != null)
 				return webApp.getMessageDestinations();
 			return (List) (runtime.getDeploymentDescriptorRoot());
 		} else if (project.hasNature(IEJBNatureConstants.NATURE_ID)) {
 			EJBJar ejbJar = EarModuleManager.getEJBModuleExtension().getEJBJar(project);
 			AssemblyDescriptor assDesc = ejbJar.getAssemblyDescriptor();
 			if (assDesc != null)
 				return assDesc.getMessageDestinations();
 		}
 		return new ArrayList();
 	}
 
 	/**
 	 * @return
 	 */
 	private List getModuleMessageDestinationNames(EObject owner) {
 		List temp = new ArrayList();
 		if (owner == null)
 			return temp;
 		switch (getDeploymentDescriptorType()) {
 			case XMLResource.APP_CLIENT_TYPE :
 				ApplicationClient client = (ApplicationClient) owner;
 				temp = client.getMessageDestinations();
 				break;
 			case XMLResource.EJB_TYPE :
 				EnterpriseBean ejb = (EnterpriseBean) owner;
 				AssemblyDescriptor assDesc = ejb.getEjbJar().getAssemblyDescriptor();
 				if (assDesc != null)
 					temp = assDesc.getMessageDestinations();
 				break;
 			case XMLResource.WEB_APP_TYPE :
 				WebApp app = (WebApp) owner;
 				temp = app.getMessageDestinations();
 				break;
 		}
 		return temp;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.reference.ReferenceDataModel#doSetProperty(java.lang.String,
 	 *      java.lang.Object)
 	 */
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		if (TARGET.equals(propertyName)) {
			MessageDriven target = (MessageDriven) propertyValue;
 			//setup Link
 			if (target == null)
 				return super.doSetProperty(propertyName, propertyValue);
			if (target.getLink() != null) {
 				setProperty(HAS_LINK, Boolean.TRUE);
				setProperty(LINK, target.getLink());
 			} else {

 				setProperty(HAS_LINK, Boolean.FALSE);
 				setProperty(LINK, target.getName());
 			}
 			//setup type
 			//setProperty(TYPE, target.getMess);
 
 			//setup name
 			setProperty(REF_NAME, target.getName());
 			notifyEnablementChange(LINK);
 		}
 		return super.doSetProperty(propertyName, propertyValue);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#basicIsEnabled(java.lang.String)
 	 */
 	protected Boolean basicIsEnabled(String propertyName) {
 		if (propertyName.equals(LINK)) {
 			if (getBooleanProperty(HAS_LINK))
 				return Boolean.FALSE;
 			return Boolean.TRUE;
 		}
 		return super.basicIsEnabled(propertyName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#getDefaultOperation()
 	 */
 	public WTPOperation getDefaultOperation() {
 		return new MessageDestReferenceCreationOperation(this);
 	}
 
 }
