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
  * Created on Dec 4, 2003
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.common.operations;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.internal.operation.ModelModifierOperationDataModel;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerCore;
 
 /**
  * @author DABERG
  * 
  * To change the template for this generated type comment go to Window>Preferences>Java>Code
  * Generation>Code and Comments
  */
 public abstract class J2EEModelModifierOperationDataModel extends ModelModifierOperationDataModel {
 	protected ArtifactEdit artifactEdit;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doSetProperty(java.lang.String,
 	 *      java.lang.Object)
 	 */
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		boolean notify = super.doSetProperty(propertyName, propertyValue);
 		if (propertyName.equals(MODULE_NAME))
			updateArtifactEdit(propertyName);
 		return notify;
 	}
 	
 	private void updateArtifactEdit(String moduleName) {
 		IProject project = ProjectUtilities.getProject(getStringProperty(PROJECT_NAME));
 		artifactEdit = ArtifactEdit.getArtifactEditForWrite(ComponentHandle.create(project,moduleName));
 		
 	}
 
 
 	/**
 	 * This will be the type of the deployment descriptor docuemnt.
 	 * 
 	 * @see XMLResource#APP_CLIENT_TYPE
 	 * @see XMLResource#APPLICATION_TYPE
 	 * @see XMLResource#EJB_TYPE
 	 * @see XMLResource#WEB_APP_TYPE
 	 * @see XMLResource#RAR_TYPE
 	 */
 	public int getDeploymentDescriptorType() {
 		if (artifactEdit != null)
 			return ((XMLResource)((EnterpriseArtifactEdit)artifactEdit).getDeploymentDescriptorResource()).getType();
 		return -1;
 	}
 
 	public EObject getDeploymentDescriptorRoot() {
 		if (artifactEdit != null)
 			return ((EnterpriseArtifactEdit)artifactEdit).getDeploymentDescriptorRoot();
 		return null;
 	}
 
 	public String getServerTargetID() {
 		if (artifactEdit != null) {
 			IRuntime target = ServerCore.getProjectProperties(ProjectUtilities.getProject(artifactEdit.getContentModelRoot())).getRuntimeTarget();
 			if (null != target) {
 				return target.getId();
 			}
 		}
 		return null;
 	}
 
 	public String getServerTargetTypeID() {
 		if (artifactEdit != null) {
 			IRuntime target = ServerCore.getProjectProperties(ProjectUtilities.getProject(artifactEdit.getContentModelRoot())).getRuntimeTarget();
 			if (null != target) {
 				return target.getRuntimeType().getId();
 			}
 		}
 		return null;
 	}
 
 }
