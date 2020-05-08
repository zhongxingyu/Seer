 /*******************************************************************************
  * Copyright (c) 2003, 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.servertarget;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EECreationResourceHandler;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPOperationDataModel;
 import org.eclipse.wst.common.frameworks.internal.operations.WTPPropertyDescriptor;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.internal.ResourceManager;
 
 public class J2EEProjectServerTargetDataModel extends WTPOperationDataModel {
 
     public J2EEProjectServerTargetDataModel() {
         super();
     }
 
     private static final String DEFAULT_TARGET_ID = "org.eclipse.jst.server.core.runtimeType"; //$NON-NLS-1$
 
     /**
      * required, type String
      */
     public static final String PROJECT_NAME = "J2EEProjectServerTargetDataModel.PROJECT_NAME"; //$NON-NLS-1$
 
     /**
      * required, not defaulted. If null, will not run.
      */
     public static final String RUNTIME_TARGET_ID = "J2EEProjectServerTargetDataModel.RUNTIME_TARGET_ID"; //$NON-NLS-1$
 
     /**
      * optional, default true, type Boolean. Set this to true if the operation
      * is supposed to update all dependent modules and projects in an ear if the
      * passed project name is an ear project
      */
     public static final String UPDATE_MODULES = "J2EEProjectServerTargetDataModel.UPDATE_MODULES"; //$NON-NLS-1$
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#getDefaultOperation()
      */
     public WTPOperation getDefaultOperation() {
         return new J2EEProjectServerTargetOperation(this);
     }
 
     protected void initValidBaseProperties() {
         super.initValidBaseProperties();
         addValidBaseProperty(PROJECT_NAME);
         addValidBaseProperty(RUNTIME_TARGET_ID);
         addValidBaseProperty(UPDATE_MODULES);
     }
 
     public IProject getProject() {
         String name = (String) getProperty(PROJECT_NAME);
         if (name != null && name.length() > 0)
             return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
         return null;
     }
 
     public IRuntime getRuntimeTarget() {
         String serverTargetId = (String) getProperty(RUNTIME_TARGET_ID);
         return ResourceManager.getInstance().getRuntime(serverTargetId);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#getDefaultProperty(java.lang.String)
      */
     protected Object getDefaultProperty(String propertyName) {
         if (propertyName.equals(RUNTIME_TARGET_ID))
             return getDefaultServerTargetID();
         else if (propertyName.equals(UPDATE_MODULES)) {
             return Boolean.TRUE;
         }
         return super.getDefaultProperty(propertyName);
     }
 
     private Integer getDefaultVersionID() {
         return new Integer(J2EEVersionConstants.J2EE_1_4_ID);
     }
 
     /**
      * @return
      */
     private Object getDefaultServerTargetID() {
         List targets = getValidServerTargets();
         if (!targets.isEmpty()) {
             IRuntime target = null;
             for (int i = targets.size() - 1; i < targets.size() && i >= 0; i--) {
                 target = (IRuntime) targets.get(i);
				String id = target.getRuntimeType().getId();
                if (DEFAULT_TARGET_ID.equals(id))
                     return target.getId();
             }
             if (target != null)
                 return target.getId();
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doGetValidPropertyValues(java.lang.String)
      */
     protected WTPPropertyDescriptor[] doGetValidPropertyDescriptors(String propertyName) {
         if (propertyName.equals(RUNTIME_TARGET_ID))
             return getValidServerTargetDescriptors();
         return super.doGetValidPropertyDescriptors(propertyName);
     }
 
     private WTPPropertyDescriptor[] getValidServerTargetDescriptors() {
         List targets = getValidServerTargets();
         WTPPropertyDescriptor[] descriptors = null;
         if (!targets.isEmpty()) {
             int serverTargetListSize = targets.size();
             descriptors = new WTPPropertyDescriptor[serverTargetListSize];
             for (int i = 0; i < targets.size(); i++) {
                 IRuntime runtime = (IRuntime) targets.get(i);
                 descriptors[i] = new WTPPropertyDescriptor(runtime.getId(), runtime.getName());
             }
         } else {
             descriptors = new WTPPropertyDescriptor[0];
         }
         return descriptors;
     }
 
     private IRuntime getServerTargetByID(String id) {
         List targets = getValidServerTargets();
         IRuntime target;
         for (int i = 0; i < targets.size(); i++) {
             target = (IRuntime) targets.get(i);
             if (id.equals(target.getId()))
                 return target;
         }
         return null;
     }
 
     /**
      * @return
      */
     private List getValidServerTargets() {
         List validServerTargets = null;
         //TODO: api is needed from the server target helper to get all server targets
 		//validServerTargets = ServerTargetHelper.getServerTargets(IServerTargetConstants.EAR_TYPE, IServerTargetConstants.J2EE_14);
         validServerTargets = ServerTargetHelper.getServerTargets("", "");  //$NON-NLS-1$  //$NON-NLS-2$
         if (validServerTargets != null && validServerTargets.isEmpty())
             validServerTargets = null;
         if (validServerTargets == null)
             return Collections.EMPTY_LIST;
         return validServerTargets;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doValidateProperty(java.lang.String)
      */
     protected IStatus doValidateProperty(String propertyName) {
         if (propertyName.equals(RUNTIME_TARGET_ID))
             return validateServerTarget();
         return super.doValidateProperty(propertyName);
     }
 
     /**
      * @return
      */
     private IStatus validateServerTarget() {
         List targets = getValidServerTargets();
         if (targets.isEmpty()) {
             return J2EEPlugin.newErrorStatus(J2EECreationResourceHandler.getString("ServerTargetDataModel_UI_7"), null); //$NON-NLS-1$
         }
         IRuntime target = getRuntimeTarget();
         if (target == null) {
             return J2EEPlugin.newErrorStatus(J2EECreationResourceHandler.getString("ServerTargetDataModel_UI_8"), null); //$NON-NLS-1$
         } else if (!targets.contains(target)) {
             return J2EEPlugin.newErrorStatus(J2EECreationResourceHandler.getString("ServerTargetDataModel_UI_9"), null); //$NON-NLS-1$
         }
         return OK_STATUS;
     }
 
 }
