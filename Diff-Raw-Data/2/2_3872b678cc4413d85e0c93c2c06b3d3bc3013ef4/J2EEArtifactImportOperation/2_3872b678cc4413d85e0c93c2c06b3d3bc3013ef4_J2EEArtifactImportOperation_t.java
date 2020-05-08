 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.ModuleFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.SaveStrategy;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsOp;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.enablement.nonui.WFTWrappedException;
 
 public abstract class J2EEArtifactImportOperation extends AbstractDataModelOperation {
 
 	protected ModuleFile moduleFile;
 	protected IVirtualComponent virtualComponent;
 	protected IAdaptable info;
 
 	public J2EEArtifactImportOperation(IDataModel model) {
 		super(model);
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		try {
 			this.info = info;
 			doExecute(monitor);
 			return OK_STATUS;
 		} finally {
 			model.dispose();
 		}
 	}
 
 	protected void doExecute(IProgressMonitor monitor) throws ExecutionException {
 		moduleFile = (ModuleFile) model.getProperty(IJ2EEComponentImportDataModelProperties.FILE);
 		monitor.beginTask(null, moduleFile.getFiles().size());
 
 		virtualComponent = createVirtualComponent(model.getNestedModel(IJ2EEComponentImportDataModelProperties.NESTED_MODEL_J2EE_COMPONENT_CREATION), monitor);
 
 		try {
 			importModuleFile(monitor);
 		} catch (InvocationTargetException e) {
 			throw new ExecutionException(e.getMessage(), e);
 		} catch (InterruptedException e) {
 			throw new ExecutionException(e.getMessage(), e);
 		}
 	}
 
 	protected IVirtualComponent createVirtualComponent(IDataModel model, IProgressMonitor monitor) throws ExecutionException {
 		model.getDefaultOperation().execute(monitor, info);
 		return (IVirtualComponent) model.getProperty(IComponentCreationDataModelProperties.COMPONENT);
 	}
 
 	/**
 	 * Creates the appropriate save strategy. Subclases should overwrite this method to create the
 	 * appropriate save startegy for the kind of J2EE module project to import the archive
 	 */
 	protected abstract SaveStrategy createSaveStrategy(IVirtualComponent virtualComponent);
 
 	protected void modifyStrategy(SaveStrategy saveStrat) {
 	}
 
 	/**
 	 * perform the archive import operation
 	 * 
 	 * @throws java.lang.reflect.InvocationTargetException
 	 * @throws java.lang.InterruptedException
 	 */
 	protected void importModuleFile(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		try {
 			monitor.worked(1);
 			J2EEComponentSaveStrategyImpl aStrategy = (J2EEComponentSaveStrategyImpl) createSaveStrategy(virtualComponent);
 			aStrategy.setProgressMonitor(monitor);
 			aStrategy.setOverwriteHandler((IOverwriteHandler) model.getProperty(IJ2EEComponentImportDataModelProperties.OVERWRITE_HANDLER));
 			aStrategy.setDataModel(model);
 			modifyStrategy(aStrategy);
 			moduleFile.save(aStrategy);
 		} catch (OverwriteHandlerException oe) {
 			throw new InterruptedException();
 		} catch (Exception ex) {
 			throw new WFTWrappedException(ex, EJBArchiveOpsResourceHandler.getString("ERROR_IMPORTING_MODULE_FILE")); //$NON-NLS-1$
 		}
 	}
 
 	protected static void addToClasspath(IDataModel importModel, List extraEntries) throws JavaModelException {
 		if (extraEntries.size() > 0) {
 			IJavaProject javaProject = JavaCore.create(((IVirtualComponent)importModel.getProperty(IJ2EEComponentImportDataModelProperties.COMPONENT)).getProject());
 			IVirtualComponent comp = (IVirtualComponent)importModel.getProperty(IJ2EEComponentImportDataModelProperties.COMPONENT);
 			IClasspathEntry[] javaClasspath = javaProject.getRawClasspath();
 			List nonDuplicateList = new ArrayList();
 			for (int i = 0; i < extraEntries.size(); i++) {
 				IClasspathEntry extraEntry = (IClasspathEntry) extraEntries.get(i);
 				boolean include = true;
 				for (int j = 0; include && j < javaClasspath.length; j++) {
 					if (extraEntry.equals(javaClasspath[j])) {
 						include = false;
 					}
 				}
 				if (include) {
 					nonDuplicateList.add(extraEntry);
 				}
 			}
 			if (nonDuplicateList.size() > 0) {
 				IClasspathEntry[] newJavaClasspath = new IClasspathEntry[javaClasspath.length + nonDuplicateList.size()];
 				System.arraycopy(javaClasspath, 0, newJavaClasspath, 0, javaClasspath.length);
 				for (int j = 0; j < nonDuplicateList.size(); j++) {
 					newJavaClasspath[javaClasspath.length + j] = (IClasspathEntry) nonDuplicateList.get(j);
 				}
 				javaProject.setRawClasspath(newJavaClasspath, new NullProgressMonitor());
 			}
 		}
 	}
 
 	//Assumes that the project exists with the same name as the
 	//entry in the manifest.
 	
 	protected void fixModuleReference(IDataModel importModel, String[] manifestEntries){
 		IVirtualComponent comp = (IVirtualComponent)importModel.getProperty(IJ2EEComponentImportDataModelProperties.COMPONENT);
 		
 		if ( comp.getComponentTypeId().equals(IModuleConstants.JST_EJB_MODULE)  && manifestEntries.length > 0){
 			for (int j = 0; j < manifestEntries.length; j++) {
 				String name = manifestEntries[j];
 				name = name.substring(0, name.length() - 4);
 				IProject project = ProjectUtilities.getProject(name);
				if( project != null && project.isAccessible() && project.exists()){
 					IVirtualComponent refcomp = ComponentCore.createComponent(project, name);
 					if( refcomp.exists()){
 						ArrayList list = new ArrayList();
 						list.add(refcomp.getComponentHandle());						
 						CreateReferenceComponentsOp op = ComponentUtilities.createReferenceComponentOperation(comp.getComponentHandle(), list);
 						try {
 							op.execute(null, null);
 						} catch (ExecutionException e) {
 							Logger.getLogger().logError(e);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		return null;
 	}
 
 	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		return null;
 	}
 
 
 }
