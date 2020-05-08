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
 package org.eclipse.jst.j2ee.applicationclient.internal.creation;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.internal.operations.J2EEComponentCreationOperation;
 import org.eclipse.jst.j2ee.application.internal.operations.UpdateManifestDataModel;
 import org.eclipse.jst.j2ee.applicationclient.componentcore.util.AppClientArtifactEdit;
 import org.eclipse.jst.j2ee.datamodel.properties.IAppClientComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
 import org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassDataModelProvider;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.FlexibleJavaProjectPreferenceUtil;
 
 public class AppClientComponentCreationOperation extends J2EEComponentCreationOperation implements IAppClientComponentCreationDataModelProperties {
 
     public AppClientComponentCreationOperation(IDataModel model) {
         super(model);
     }
 
     protected void createAndLinkJ2EEComponentsForMultipleComponents() throws CoreException {
         IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
         component.create(0, null);
         //create and link appClientModule Source Folder
 		IVirtualFolder rootFolder = component.getRootFolder();
         IVirtualFolder appClientModuleFolder = rootFolder.getFolder(new Path("/")); //$NON-NLS-1$        
         appClientModuleFolder.createLink(new Path("/" + getModuleName() + "/appClientModule"), 0, null); //$NON-NLS-1$ //$NON-NLS-2$
         
         //create and link META-INF folder
         IVirtualFolder metaInfFolder = appClientModuleFolder.getFolder(J2EEConstants.META_INF);
         metaInfFolder.create(IResource.FORCE, null);    
     }
     
     protected void createAndLinkJ2EEComponentsForSingleComponent() throws CoreException {
         IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
         component.create(0, null);
         //create and link appClientModule Source Folder
 		IVirtualFolder rootFolder = component.getRootFolder();
         IVirtualFolder appClientModuleFolder = rootFolder.getFolder(new Path("/")); //$NON-NLS-1$        
         appClientModuleFolder.createLink(new Path("/appClientModule"), 0, null); //$NON-NLS-1$ //$NON-NLS-2$
         
         //create and link META-INF folder
         IVirtualFolder metaInfFolder = appClientModuleFolder.getFolder(J2EEConstants.META_INF);
         metaInfFolder.create(IResource.FORCE, null);    
     }
 
     protected void createDeploymentDescriptor(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
         AppClientArtifactEdit artifactEdit = null;
         try {
 			ComponentHandle handle = ComponentHandle.create(getProject(),model.getStringProperty(COMPONENT_DEPLOY_NAME));
             artifactEdit = AppClientArtifactEdit.getAppClientArtifactEditForWrite(handle);
             Integer version = (Integer)model.getProperty(COMPONENT_VERSION);
             artifactEdit.createModelRoot(version.intValue());
             artifactEdit.save(monitor);
         } catch(Exception e){
             Logger.getLogger().logError(e);
         } finally {
             if (artifactEdit != null)
                 artifactEdit.dispose();
         }
     }
     
     protected String getVersion() {
         int version = model.getIntProperty(COMPONENT_VERSION);
         return J2EEVersionUtil.getJ2EETextVersion(version);
     }
 
     public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
         try {
             super.execute( IModuleConstants.JST_APPCLIENT_MODULE, monitor );
             
             if (model.getBooleanProperty(CREATE_DEFAULT_MAIN_CLASS)) {
             	IDataModel mainClassDataModel = DataModelFactory.createDataModel(NewJavaClassDataModelProvider.class);
                 mainClassDataModel.setProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME, getProject().getName());
                 mainClassDataModel.setProperty(INewJavaClassDataModelProperties.CLASS_NAME, "Main"); //$NON-NLS-1$
                 mainClassDataModel.setBooleanProperty(INewJavaClassDataModelProperties.MAIN_METHOD, true);
                 String projRelativeSourcePath = IPath.SEPARATOR + getProject().getName() + model.getStringProperty(JAVASOURCE_FOLDER);
                 if(FlexibleJavaProjectPreferenceUtil.getMultipleModulesPerProjectProp())
                     projRelativeSourcePath = IPath.SEPARATOR + getProject().getName() + IPath.SEPARATOR + getModuleName() + IPath.SEPARATOR + model.getStringProperty(JAVASOURCE_FOLDER);             
                 mainClassDataModel.setProperty(INewJavaClassDataModelProperties.SOURCE_FOLDER, projRelativeSourcePath);
                 // mainClassDataModel.setProperty(NewJavaClassDataModel.JAVA_PACKAGE, "appclient");//$NON-NLS-1$
                 mainClassDataModel.getDefaultOperation().execute(monitor,null);
                 
                 createManifestEntryForMainClass(monitor);
             }
         } catch (CoreException e) {
             Logger.getLogger().log(e.getMessage());
         } catch (InvocationTargetException e) {
             Logger.getLogger().log(e.getMessage());
         } catch (InterruptedException e) {
             Logger.getLogger().log(e.getMessage());
         }
         return OK_STATUS;
     }
 
     protected void createManifestEntryForMainClass(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
         String manifestFolder = model.getStringProperty(MANIFEST_FOLDER);
         IContainer container = getProject().getFolder(manifestFolder);
         IFile file = container.getFile(new Path(J2EEConstants.MANIFEST_SHORT_NAME));
         
         if (model.getBooleanProperty(CREATE_DEFAULT_MAIN_CLASS)) {
             UpdateManifestDataModel dm = new UpdateManifestDataModel();
             dm.setProperty(UpdateManifestDataModel.PROJECT_NAME, getProject().getName());
             dm.setBooleanProperty(UpdateManifestDataModel.MERGE, false);
             dm.setProperty(UpdateManifestDataModel.MANIFEST_FILE, file);
             dm.setProperty(UpdateManifestDataModel.MAIN_CLASS, "Main"); //$NON-NLS-1$
             dm.getDefaultOperation().run(monitor);
         }
     }
     public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
         // TODO Auto-generated method stub
         return null;
     }
 
     public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
