 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.creation.core.commands;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCore;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 
 /**
  * @author Grid Qian
  */
 public class ValidateWSImplCommand extends AbstractDataModelOperation {
 
 	private ServiceModel model;
 
 	public ValidateWSImplCommand(ServiceModel model) {
 		this.model = model;
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
 			throws ExecutionException {
		
 		String implClass = model.getServiceClasses().get(0);
 		String project = model.getWebProjectName();
 		ICompilationUnit unit = null;
 		try {
 			if (JBossWSCreationUtils.getJavaProjectByName(project).findType(
 					implClass) != null) {
 				unit = JBossWSCreationUtils.getJavaProjectByName(project)
						.findType(implClass).getCompilationUnit();				
 			} else {
 				return StatusUtils.errorStatus(NLS.bind(
 						JBossWSCreationCoreMessages.Error_No_Class,
 						new String[] { implClass, project }));
 			}
 			if (!unit.getSource().contains(
					JBossWSCreationCoreMessages.Webservice_Annotation)) {
 				return StatusUtils
 						.errorStatus(JBossWSCreationCoreMessages.Error_No_Annotation);
 			}
 		} catch (JavaModelException e) {
 			JBossWSCreationCore.getDefault().logError(e);
 			return StatusUtils.errorStatus(NLS.bind(
 					JBossWSCreationCoreMessages.Error_No_Class, new String[] {
 							implClass, project }));
		} 
 		return Status.OK_STATUS;
 	}
 
 }
