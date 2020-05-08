 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.SaveStrategy;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.datamodel.properties.IEARComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsDataModelProvider;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class EARComponentImportOperation extends J2EEArtifactImportOperation {
 
	private EARFile earFile;
 
	public EARComponentImportOperation(IDataModel dataModel) {
		super(dataModel);
		earFile = (EARFile) model.getProperty(IEARComponentImportDataModelProperties.FILE);
 	}
 
 	/**
 	 * Subclasses must override to performs the workbench modification steps that are to be
 	 * contained within a single logical workbench change.
 	 * 
 	 * @param monitor
 	 *            the progress monitor to use to display progress
 	 */
 	protected void doExecute(IProgressMonitor monitor) throws ExecutionException {
 		super.doExecute(monitor);
 		List modelsToImport = (List) model.getProperty(IEARComponentImportDataModelProperties.HANDLED_PROJECT_MODELS_LIST);
 		try {
 			IDataModel importModel = null;
 			List allModels = (List) model.getProperty(IEARComponentImportDataModelProperties.ALL_PROJECT_MODELS_LIST);
 			List componentToAdd = new ArrayList();
 			for (int i = 0; i < allModels.size(); i++) {
 				importModel = (IDataModel) allModels.get(i);
 				if (modelsToImport.contains(importModel)) {
 					importModel.setProperty(IJ2EEComponentImportDataModelProperties.CLOSE_ARCHIVE_ON_DISPOSE, Boolean.FALSE);
 					try {
 						importModel.getDefaultOperation().execute(monitor, info);
 					} catch (ExecutionException e) {
 						Logger.getLogger().logError(e);
 					}
 					componentToAdd.add(((IVirtualComponent) importModel.getProperty(IJ2EEComponentImportDataModelProperties.COMPONENT)));
 				}
 			}
 			if (componentToAdd.size() > 0) {
 				IDataModel addComponentsDM = DataModelFactory.createDataModel(new CreateReferenceComponentsDataModelProvider());
 				addComponentsDM.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, virtualComponent);
 				addComponentsDM.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, componentToAdd);
 				addComponentsDM.getDefaultOperation().execute(monitor, info);
 			}
 			try {
 				fixupClasspaths(modelsToImport, virtualComponent);
 			} catch (JavaModelException e) {
 				Logger.getLogger().logError(e);
 			}
 		} finally {
 			resetDisposeImportModels();
 			// FileSet.printState();
 		}
 	}
 
 	/**
 	 * @param modelsToImport
 	 */
 	private void resetDisposeImportModels() {
 		resetDisposeImportModels((List) model.getProperty(IEARComponentImportDataModelProperties.ALL_PROJECT_MODELS_LIST));
 	}
 
 	private void resetDisposeImportModels(List models) {
 		IDataModel model;
 		for (int i = 0; i < models.size(); i++) {
 			model = (IDataModel) models.get(i);
 			model.setProperty(IJ2EEComponentImportDataModelProperties.CLOSE_ARCHIVE_ON_DISPOSE, Boolean.TRUE);
 		}
 	}
 
 
 	private void fixupClasspaths(List selectedModels, IVirtualComponent earComponent) throws JavaModelException {
 		IDataModel importModel;
 		for (int i = 0; i < selectedModels.size(); i++) {
 			importModel = (IDataModel) selectedModels.get(i);
 			Archive archive = (Archive) importModel.getProperty(IJ2EEComponentImportDataModelProperties.FILE);
 			String[] manifestClasspath = archive.getManifest().getClassPathTokenized();
 			if (manifestClasspath.length > 0) {
 				List extraEntries = fixupClasspath(earComponent, manifestClasspath, new ArrayList(), archive, (IVirtualComponent) importModel.getProperty(IJ2EEComponentImportDataModelProperties.COMPONENT));
 				addToClasspath(importModel, extraEntries);
 				fixModuleReference(importModel, manifestClasspath);
 			}
 		}
 	}
 
 	private List fixupClasspath(IVirtualComponent earComponent, String[] manifestClasspath, List computedFiles, Archive anArchive, IVirtualComponent nestedComponent) throws JavaModelException {
 		List extraEntries = new ArrayList();
 		for (int j = 0; j < manifestClasspath.length; j++) {
 			String manifestURI = ArchiveUtil.deriveEARRelativeURI(manifestClasspath[j], anArchive);
 			if (null == manifestURI) {
 				continue;
 			}
 			IVirtualFile vFile = earComponent.getRootFolder().getFile(manifestURI);
 			if (!computedFiles.contains(vFile)) {
 				computedFiles.add(vFile);
 				if (vFile.exists()) {
 					IFile file = vFile.getUnderlyingFile();
 					extraEntries.add(JavaCore.newLibraryEntry(file.getFullPath(), file.getFullPath(), null, true));
 					Archive archive = null;
 					try {
						archive = (Archive) earFile.getFile(manifestURI);
 						String[] nestedManifestClasspath = archive.getManifest().getClassPathTokenized();
 						extraEntries.addAll(fixupClasspath(earComponent, nestedManifestClasspath, computedFiles, archive, nestedComponent));
 					} catch (FileNotFoundException e) {
 						Logger.getLogger().logError(e);
 					} finally {
 						if (null != archive) {
 							archive.close();
 						}
 					}
 				} else {
 					String compSearchName = manifestURI.substring(0, manifestURI.length() - 4);
 					IVirtualReference vRef = earComponent.getReference(compSearchName);
 					if (null != vRef && nestedComponent.getProject() != vRef.getReferencedComponent().getProject()) {
 						IProject project = vRef.getReferencedComponent().getProject();
 						extraEntries.add(JavaCore.newProjectEntry(project.getFullPath(), true));
 					}
 				}
 			}
 		}
 		return extraEntries;
 	}
 
 	private void releaseDeploymentDescriptor() {
 		try {
			if (earFile != null && earFile.isDeploymentDescriptorSet()) {
				XMLResource res = (XMLResource) earFile.getDeploymentDescriptor().eResource();
 				if (res != null)
 					res.releaseFromRead();
 			}
 		} catch (Exception suppress) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(suppress);
 		}
 	}
 
 	protected SaveStrategy createSaveStrategy(IProject project) { // NOOP
 		return null;
 	}
 
 	protected SaveStrategy createSaveStrategy(IVirtualComponent virtualComponent) {
 		return new EARComponentSaveStrategyImpl(virtualComponent);
 	}
 }
