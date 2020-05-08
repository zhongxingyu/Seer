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
  * Created on Nov 5, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.JavaComponentCreationDataModel;
 import org.eclipse.jst.j2ee.internal.common.UpdateProjectClasspath;
 import org.eclipse.jst.j2ee.internal.earcreation.EARComponentCreationDataModel;
 import org.eclipse.jst.j2ee.internal.project.ManifestFileCreationAction;
 import org.eclipse.wst.common.modulecore.ComponentType;
 import org.eclipse.wst.common.modulecore.ModuleCore;
 import org.eclipse.wst.common.modulecore.ModuleCoreFactory;
 import org.eclipse.wst.common.modulecore.WorkbenchComponent;
 import org.eclipse.wst.common.modulecore.internal.operation.ComponentCreationOperation;
 import org.eclipse.wst.common.modulecore.resources.IVirtualContainer;
 
 public abstract class J2EEComponentCreationOperation extends ComponentCreationOperation {
 	/**
      * name of the template emitter to be used to generate the deployment
      * descriptor from the tags
 	 */
 	protected static final String TEMPLATE_EMITTER = "org.eclipse.jst.j2ee.ejb.annotations.emitter.template"; //$NON-NLS-1$
 	/**
      * id of the builder used to kick off generation of web metadata based on
      * parsing of annotations
 	 */
 	protected static final String BUILDER_ID = "builderId"; //$NON-NLS-1$
 	
 	public J2EEComponentCreationOperation(JavaComponentCreationDataModel dataModel) {
 		super(dataModel);
 	}
 
 	public J2EEComponentCreationOperation() {
 		super();
 	}
 
 	protected void execute( String componentType, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 		J2EEComponentCreationDataModel dataModel = (J2EEComponentCreationDataModel) operationDataModel;
 		
         createAndLinkJ2EEComponents();
         setupComponentType(componentType);
 		
 		if (dataModel.getBooleanProperty(J2EEComponentCreationDataModel.CREATE_DEFAULT_FILES)) {
 			createDeploymentDescriptor(monitor);
 			createManifest(monitor);
 		}
 		
 		addSrcFolderToProject();
 		if (((J2EEComponentCreationDataModel) operationDataModel).getBooleanProperty(IAnnotationsDataModel.USE_ANNOTATIONS))
 			addAnnotationsBuilder();	
 		
 		linkToEARIfNecessary(dataModel, monitor);
 	}
 
     protected abstract void createAndLinkJ2EEComponents() throws CoreException;
 	
 
 	
 	protected abstract void createDeploymentDescriptor(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException;
 
 		public  void linkToEARIfNecessary(J2EEComponentCreationDataModel moduleModel, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 			if (moduleModel.getBooleanProperty(J2EEComponentCreationDataModel.ADD_TO_EAR)) {
 				createEARComponentIfNecessary(moduleModel, monitor);
 				runAddToEAROperation(moduleModel, monitor);
 			}
 		}
 
 		/**
 		 * @param moduleModel
 		 * @param monitor
 		 * @throws CoreException
 		 * @throws InvocationTargetException
 		 * @throws InterruptedException
 		 */
 		protected void runAddToEAROperation(J2EEComponentCreationDataModel moduleModel, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 			ModuleCore core = null;
 			try {
 				core = ModuleCore.getModuleCoreForRead(getProject());
 				WorkbenchComponent wc = core.findWorkbenchModuleByDeployName((String)moduleModel.getProperty(J2EEComponentCreationDataModel.COMPONENT_DEPLOY_NAME));
				AddComponentToEnterpriseApplicationDataModel dm = moduleModel.addComponentToEARDataModel;
 				dm.setProperty(AddComponentToEnterpriseApplicationDataModel.MODULE_NAME,wc.getName());
				
				dm.setProperty(AddComponentToEnterpriseApplicationDataModel.EAR_MODULE_NAME, moduleModel.getProperty(J2EEComponentCreationDataModel.EAR_MODULE_DEPLOY_NAME));
				
//				String earModuleName = moduleModel.getStringProperty(J2EEComponentCreationDataModel.EAR_MODULE_NAME);
//				if(earModuleName.endsWith("ear"))
//					dm.setProperty(AddComponentToEnterpriseApplicationDataModel.EAR_MODULE_NAME,moduleModel.getProperty(J2EEComponentCreationDataModel.EAR_MODULE_NAME));
//				else
//					dm.setProperty(AddComponentToEnterpriseApplicationDataModel.EAR_MODULE_NAME,moduleModel.getProperty(J2EEComponentCreationDataModel.EAR_MODULE_DEPLOY_NAME));
				
				List modulesList = new ArrayList();
				modulesList.add(wc);
				dm.setProperty(AddComponentToEnterpriseApplicationDataModel.MODULE_LIST,modulesList);
				AddComponentToEnterpriseApplicationOperation addModuleOp = new AddComponentToEnterpriseApplicationOperation(moduleModel.addComponentToEARDataModel);
 				addModuleOp.doRun(monitor);
 			} finally {
 				if(core != null)
 					core.dispose();
 			}
 		}
 
 		/**
 		 * @param moduleModel
 		 * @param monitor
 		 * @throws CoreException
 		 * @throws InvocationTargetException
 		 * @throws InterruptedException
 		 */
 		protected void createEARComponentIfNecessary(J2EEComponentCreationDataModel moduleModel, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 			EARComponentCreationDataModel earModel = moduleModel.getEarComponentCreationDataModel();
 			earModel.setProperty(EARComponentCreationDataModel.COMPONENT_NAME, moduleModel.getStringProperty(J2EEComponentCreationDataModel.EAR_MODULE_NAME));
 			earModel.setProperty(EARComponentCreationDataModel.COMPONENT_DEPLOY_NAME, moduleModel.getStringProperty(J2EEComponentCreationDataModel.EAR_MODULE_DEPLOY_NAME));			
 			earModel.setProperty(EARComponentCreationDataModel.PROJECT_NAME, moduleModel.getStringProperty(J2EEComponentCreationDataModel.PROJECT_NAME));
 			if (!doesEARComponentExist(moduleModel.getStringProperty(J2EEComponentCreationDataModel.EAR_MODULE_DEPLOY_NAME))) {
 				EARComponentCreationOperation earOp = new EARComponentCreationOperation(earModel);
 				earOp.doRun(monitor);
 			}
 		}
 	
 		public  boolean doesEARComponentExist(String earComponentName) {
 			ModuleCore moduleCore = null;
 			try{
 				moduleCore = ModuleCore.getModuleCoreForRead(getProject());
 				return (moduleCore.findWorkbenchModuleByDeployName(earComponentName) != null);
 			} finally {
 				if(moduleCore != null)
 					moduleCore.dispose();
 			}
 		}
 		
 		public IProject getProject() {
 			String projName = operationDataModel.getStringProperty(J2EEComponentCreationDataModel.PROJECT_NAME );
 			return ProjectUtilities.getProject( projName );
 		}
 		
 		public String getModuleName() {
 			return (String)operationDataModel.getProperty(J2EEComponentCreationDataModel.COMPONENT_NAME);
 		}
 		
 		public String getModuleDeployName() {
 			return (String)operationDataModel.getProperty(J2EEComponentCreationDataModel.COMPONENT_DEPLOY_NAME);
 		}
 		
     protected abstract String getVersion();
 
     protected void setupComponentType(String typeID) {
         IVirtualContainer component = ModuleCore.create(getProject(), getModuleDeployName());
         ComponentType componentType = ModuleCoreFactory.eINSTANCE.createComponentType();
         componentType.setModuleTypeId(typeID);
         componentType.setVersion(getVersion());
         List newProps = getProperties();
         if (newProps != null && !newProps.isEmpty()) {
             EList existingProps = componentType.getProperties();
             for (int i = 0; i < newProps.size(); i++) {
                 existingProps.add(newProps.get(i));
 		}
 		}
         ModuleCore.setComponentType(component, componentType);
     }
 
     // Should return null if no additional properties needed
     protected List getProperties() {
         return null;
 		}
 		
 
 	/**
 	 * @param monitor
 	 */
 	protected void createManifest(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 		
 		String manifestFolder = operationDataModel.getStringProperty(J2EEComponentCreationDataModel.MANIFEST_FOLDER);
 		IContainer container = getProject().getFolder( manifestFolder );
 
 		IFile file = container.getFile( new Path(J2EEConstants.MANIFEST_SHORT_NAME));
 		
 		try {
 			ManifestFileCreationAction.createManifestFile(file, getProject());
 		}
 		catch (CoreException e) {
 			Logger.getLogger().log(e);
 		}
 		catch (IOException e) {
 			Logger.getLogger().log(e);
 		}
 //		UpdateManifestOperation op = new UpdateManifestOperation(((J2EEModuleCreationDataModel) operationDataModel).getUpdateManifestDataModel());
 //		op.doRun(monitor);		
 
 	}
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperation#dispose(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected void dispose(IProgressMonitor pm) {
 		try {
 			getOperationDataModel().dispose();
 			super.dispose(pm);
 		} catch (RuntimeException re) {
 			//Ignore
 		}
 	}
 	
 	/**
 	 * This method is intended for internal use only.  This method will add the annotations builder
 	 * for Xdoclet to the targetted project.  This needs to be removed from the operation and set
 	 * up to be more extensible throughout the workbench.
 	 * @see EJBModuleCreationOperation#execute(IProgressMonitor)
 	 * 
 	 * @deprecated
 	 */
 	protected final void addAnnotationsBuilder() {
 		try {
 			// Find the xdoclet builder from the extension registry
 			IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATE_EMITTER);
 			String builderID = configurationElements[0].getNamespace() + "."+ configurationElements[0].getAttribute(BUILDER_ID); //$NON-NLS-1$
 			IProject project = ((J2EEComponentCreationDataModel)operationDataModel).getTargetProject(); 
 			IProjectDescription description = project.getDescription();
 			ICommand[] commands = description.getBuildSpec();
 			boolean found = false;
 			// Check if the builder is already set on the project
 			for (int i = 0; i < commands.length; ++i) {
 				if (commands[i].getBuilderName().equals(builderID)) {
 					found = true;
 					break;
 				}
 			}
 			// If the builder is not on the project, add it
 			if (!found) {
 				ICommand command = description.newCommand();
 				command.setBuilderName(builderID);
 				ICommand[] newCommands = new ICommand[commands.length + 1];
 				System.arraycopy(commands, 0, newCommands, 0, commands.length);
 				newCommands[commands.length] = command;
 				IProjectDescription desc = project.getDescription();
 				desc.setBuildSpec(newCommands);
 				project.setDescription(desc, null);
 			}
 		} catch (Exception e) {
 			//Ignore
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public String getJavaSourceSourceFolder() {
 		return getOperationDataModel().getStringProperty(J2EEComponentCreationDataModel.JAVASOURCE_FOLDER);
 	}
 
 	/**
 	 * @return
 	 */
 	public String getDeploymentDescriptorFolder() {
 		return getOperationDataModel().getStringProperty(J2EEComponentCreationDataModel.DD_FOLDER);
 	}
 	
 	public String getJavaSourceSourcePath() {
 		return getOperationDataModel().getStringProperty(J2EEComponentCreationDataModel.JAVASOURCE_FOLDER);
 	}
    
 	
 	private void addSrcFolderToProject() {
 	 	JavaComponentCreationDataModel dm = (JavaComponentCreationDataModel)operationDataModel;		
 		UpdateProjectClasspath update = new UpdateProjectClasspath(dm.getJavaSourceFolder(), dm.getProject());
 	}
 }
