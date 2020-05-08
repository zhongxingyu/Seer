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
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.internal.annotations.controller.AnnotationsControllerManager;
 import org.eclipse.jst.common.internal.annotations.controller.AnnotationsControllerManager.Descriptor;
 import org.eclipse.jst.j2ee.datamodel.properties.IEarComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.UpdateProjectClasspath;
 import org.eclipse.jst.j2ee.internal.project.ManifestFileCreationAction;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.ComponentType;
 import org.eclipse.wst.common.componentcore.internal.ComponentcoreFactory;
 import org.eclipse.wst.common.componentcore.internal.Property;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.operation.ComponentCreationOperation;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.FlexibleJavaProjectPreferenceUtil;
 
 public abstract class J2EEComponentCreationOperation extends ComponentCreationOperation implements IJ2EEComponentCreationDataModelProperties, IAnnotationsDataModel {
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
 
     public J2EEComponentCreationOperation(IDataModel model) {
         super(model);
     }
 
     protected void execute(String componentType, IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
         super.execute(componentType, monitor, null);
 
         if (model.getBooleanProperty(CREATE_DEFAULT_FILES)) {
             createDeploymentDescriptor(monitor);
             createManifest(monitor);
         }
 
         addSrcFolderToProject();
         if (model.getBooleanProperty(USE_ANNOTATIONS))
             addAnnotationsBuilder();
 
         linkToEARIfNecessary(monitor);
     }
 
     protected abstract void createDeploymentDescriptor(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException;
 
     public void linkToEARIfNecessary(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
         if (model.getBooleanProperty(ADD_TO_EAR)) {
             createEARComponentIfNecessary(monitor);
             runAddToEAROperation(monitor);
         }
     }
 
     /**
      * @param moduleModel
      * @param monitor
      * @throws CoreException
      * @throws InvocationTargetException
      * @throws InterruptedException
      */
     protected void runAddToEAROperation(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 
         IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
 		IDataModel dm =  (IDataModel)model.getProperty(NESTED_ADD_COMPONENT_TO_EAR_DM);
 		ComponentHandle earhandle = (ComponentHandle) model.getProperty(EAR_COMPONENT_HANDLE);
 		dm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT_HANDLE, earhandle);
 		
         List modList = (List) dm.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_HANDLE_LIST);
         modList.add(component.getComponentHandle());
         dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_HANDLE_LIST, modList);
 		try {
 			dm.getDefaultOperation().execute(monitor, null);
 		} catch (ExecutionException e) {
 			Logger.getLogger().log(e);
 		}		
     }
 
     /**
      * @param moduleModel
      * @param monitor
      * @throws CoreException
      * @throws InvocationTargetException
      * @throws InterruptedException
      */
     protected void createEARComponentIfNecessary(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
         IDataModel earModel = (IDataModel) model.getProperty(NESTED_EAR_COMPONENT_CREATION_DM);
 		ComponentHandle handle = (ComponentHandle)model.getProperty(EAR_COMPONENT_HANDLE);
 
 		earModel.setProperty(IEarComponentCreationDataModelProperties.COMPONENT_NAME, handle.getName());
 		earModel.setProperty(IEarComponentCreationDataModelProperties.PROJECT_NAME, handle.getProject().getName());
 	
         try {
             earModel.getDefaultOperation().execute(monitor, null);
         } catch (ExecutionException e) {
             Logger.getLogger().log(e.getMessage());
         }
        
     }
 
     public String getModuleName() {
         return (String) model.getProperty(COMPONENT_NAME);
     }
 
     public String getModuleDeployName() {
         return (String) model.getProperty(COMPONENT_DEPLOY_NAME);
     }
 
     protected abstract String getVersion();
 
     protected void setupComponentType(String typeID) {
         IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
         ComponentType componentType = ComponentcoreFactory.eINSTANCE.createComponentType();
         componentType.setComponentTypeId(typeID);
         componentType.setVersion(getVersion());
         List newProps = getProperties();
         EList existingProps = componentType.getProperties();
         if (newProps != null && !newProps.isEmpty()) {  
             for (int i = 0; i < newProps.size(); i++) {
                 existingProps.add(newProps.get(i));
             }
         }
         Property javaOutputProp = getOutputProperty();
         if(javaOutputProp != null)
             existingProps.add(javaOutputProp);        
         StructureEdit.setComponentType(component, componentType);
     }
 
     // Should return null if no additional properties needed
     protected List getProperties() {
         return null;
     }
 
     /**
      * @param monitor
      */
     protected void createManifest(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 
         String manifestFolder = model.getStringProperty(MANIFEST_FOLDER);
         IContainer container = getProject().getFolder(manifestFolder);
 
         IFile file = container.getFile(new Path(J2EEConstants.MANIFEST_SHORT_NAME));
 
         try {
             ManifestFileCreationAction.createManifestFile(file, getProject());
         } catch (CoreException e) {
             Logger.getLogger().log(e);
         } catch (IOException e) {
             Logger.getLogger().log(e);
         }
         // UpdateManifestOperation op = new
         // UpdateManifestOperation(((J2EEModuleCreationDataModel)
         // operationDataModel).getUpdateManifestDataModel());
         // op.doRun(monitor);
 
     }
 
     /**
      * This method is intended for internal use only. This method will add the
      * annotations builder for Xdoclet to the targetted project. This needs to
      * be removed from the operation and set up to be more extensible throughout
      * the workbench.
      * 
      * @see EJBModuleCreationOperation#execute(IProgressMonitor)
      * 
      */
     protected final void addAnnotationsBuilder() {
     	//If there is an extended annotations processor, let it add itself instead of xdoclet
     	Descriptor descriptor = AnnotationsControllerManager.INSTANCE.getDescriptor(ProjectUtilities.getProject(model.getStringProperty(PROJECT_NAME)));
     	if (descriptor!=null) 
     		return;
         try {
             // Find the xdoclet builder from the extension registry
             IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATE_EMITTER);
             String builderID = configurationElements[0].getNamespace() + "." + configurationElements[0].getAttribute(BUILDER_ID); //$NON-NLS-1$
             IProject project = ProjectUtilities.getProject(model.getProperty(PROJECT_NAME));
            if(project != null && project.isAccessible()) {
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
           }
         } catch (Exception e) {
             // Ignore
         }
     }
 
     private void addSrcFolderToProject() {
         UpdateProjectClasspath update = new UpdateProjectClasspath(model.getStringProperty(JAVASOURCE_FOLDER), model.getStringProperty(COMPONENT_NAME), ProjectUtilities.getProject(model.getStringProperty(PROJECT_NAME)));
     }
     
     protected Property getOutputProperty() {
         String javaSourceFolder = model.getStringProperty(JAVASOURCE_FOLDER);
         if(javaSourceFolder != null && !javaSourceFolder.equals("")) {
             Property prop = ComponentcoreFactory.eINSTANCE.createProperty();
             IPath newOutputPath = null;
             if(FlexibleJavaProjectPreferenceUtil.getMultipleModulesPerProjectProp())
                 newOutputPath= Path.fromOSString("/bin/" + getComponentName() + Path.SEPARATOR);
             else
                 newOutputPath = Path.fromOSString("/bin/");
             // need a java property constant
             prop.setName(IModuleConstants.PROJ_REL_JAVA_OUTPUT_PATH);
             prop.setValue(newOutputPath.toString());
             return prop;
         }
         return null;
     }
 }
