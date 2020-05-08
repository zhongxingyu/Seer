 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.archive.operations;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.SaveFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.datamodel.properties.IEARComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class EARComponentSaveStrategyImpl extends J2EEComponentSaveStrategyImpl implements IJ2EEImportExportConstants {
 
 	protected Map createdComponentsMap;
 
 	public EARComponentSaveStrategyImpl(IVirtualComponent component) {
 		super(component);
 	}
 
 	public void setDataModel(IDataModel dataModel) {
 		super.setDataModel(dataModel);
 
 		setArchive((Archive) dataModel.getProperty(IEARComponentImportDataModelProperties.FILE));
 		overwriteHandler = (IOverwriteHandler) dataModel.getProperty(IEARComponentImportDataModelProperties.OVERWRITE_HANDLER);
 		if (null != overwriteHandler) {
 			overwriteHandler.setEarSaveStrategy(this);
 		}
 		buildProjectsMap();
 	}
 
 
 
 	/**
 	 * Creates a Map mapping archive uris to projects for all archives in the ear that imported as
 	 * projects.
 	 */
 	private void buildProjectsMap() {
 		createdComponentsMap = new HashMap();
 		List createdProjectsList = (List) dataModel.getProperty(IEARComponentImportDataModelProperties.ALL_PROJECT_MODELS_LIST);
 		IDataModel importDM = null;
 		Archive anArchive = null;
 		for (int i = 0; i < createdProjectsList.size(); i++) {
 			importDM = (IDataModel) createdProjectsList.get(i);
 			anArchive = (Archive) importDM.getProperty(IJ2EEComponentImportDataModelProperties.FILE);
 			createdComponentsMap.put(anArchive.getURI(), importDM.getProperty(IJ2EEComponentImportDataModelProperties.COMPONENT));
 		}
 	}
 
 	protected void addFileToClasspath(IProject p, IFile file, List cp) {
 		if (!file.exists())
 			return;
 
 		// Assume the file also contains the source
 		IPath path = file.getFullPath();
 		IClasspathEntry entry = JavaCore.newLibraryEntry(path, path, null, true);
 		if (!cp.contains(entry))
 			cp.add(entry);
 	}
 
 	protected void addProjectToClasspath(IProject dependent, IProject prereq, List cp) {
 		IClasspathEntry entry = JavaCore.newProjectEntry(prereq.getFullPath(), true);
 		if (!cp.contains(entry))
 			cp.add(entry);
 	}
 
 	protected EARFile getEARFile() {
 		return (EARFile) getArchive();
 	}
 
 
 	protected java.io.OutputStream getOutputStreamForResource(org.eclipse.emf.ecore.resource.Resource aResource) throws java.io.IOException {
 		return null;
 	}
 
 	public void save() throws SaveFailureException {
 
 		saveFiles();
 		saveManifest();
 		saveMofResources();
 		progressMonitor.subTask(EARArchiveOpsResourceHandler.getString("Updating_project_classpath_UI_")); //$NON-NLS-1$ = "Updating project classpaths..."
 		updateComponentClasspaths();
 
 	}
 
 	public void save(Archive anArchive) throws SaveFailureException {
 		progressMonitor.subTask(anArchive.getURI());
 		saveArchiveAsJARInEAR(anArchive);
 	}
 
 	protected void saveArchiveAsJARInEAR(Archive anArchive) throws SaveFailureException {
 		try {
 			anArchive.save(createNestedSaveStrategy(anArchive));
 			progressMonitor.worked(1);
 		} catch (IOException e) {
 			throw new SaveFailureException(anArchive.getURI(), e);
 		}
 	}
 
 	protected void mkdirs(IPath newPath, IWorkspaceRoot root) throws CoreException {
 		if (newPath.segmentCount() <= 2)
 			return;
 		IPath parentPath = newPath.removeLastSegments(1);
 		IFolder folder = root.getFolder(parentPath);
 		if (!folder.exists()) {
 			mkdirs(parentPath, root);
 			folder.create(true, true, null);
 		}
 	}
 
 	protected SubProgressMonitor subMonitor() {
 		return new SubProgressMonitor(progressMonitor, 10);
 	}
 
 	public void setMonitor(org.eclipse.core.runtime.IProgressMonitor newMonitor) {
 		progressMonitor = newMonitor;
 	}
 
 	protected boolean shouldSave(File aFile) {
 		if (aFile.isArchive()) {
 			// TODO
 			// if (dataModel.handlesArchive((Archive) aFile)) {
 			// return false;
 			// }
 			return getFilter().shouldSave(aFile.getURI(), getArchive());
 		}
 		return super.shouldSave(aFile);
 	}
 
 	protected boolean shouldSave(String uri) {
 		if (overwriteHandler != null) {
 			if (overwriteHandler.isOverwriteNone())
 				return false;
 			return ((super.shouldSave(uri)) && (!overwriteHandler.isOverwriteResources()) && (!overwriteHandler.isOverwriteAll()) && (overwriteHandler.shouldOverwrite(uri)));
 		}
 		return true;
 	}
 
 
 	/*
 	 * Parse the manifest of the module file; for each cp entry 1) cananonicalize to a uri that
 	 * looks like the entry in the ear 2) If the ear contains a file with that uri (the entry is
 	 * valid) a) If the file is another was blown out to a project, add a cp entry for a referenced
 	 * project b) otherwise, add a cp entry that points to the file in the ear project, and cp
 	 * entries for all prereqs
 	 */
 	protected void updateProjectClasspath(Archive anArchive, IVirtualComponent component) {
 
 		String message = EARArchiveOpsResourceHandler.getString("Updating_project_classpath_UI_") + component.getName(); //$NON-NLS-1$ = "Updating project classpaths..."
 		progressMonitor.subTask(message);
 		List projectCpEntries = new ArrayList();
 		Set visited = new HashSet();
 		traverseClasspaths(component.getProject(), anArchive, projectCpEntries, visited);
 
 		try {
 			if (!projectCpEntries.isEmpty())
 				JemProjectUtilities.appendJavaClassPath(component.getProject(), projectCpEntries);
 			JemProjectUtilities.forceClasspathReload(component.getProject());
 		} catch (JavaModelException ex) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 		}
 
 	}
 
 	/*
 	 * If you have a dependency to a JAR in the EAR project, and the JAR depends on another JAR in
 	 * the EAR; you want to compile cleanly after import, so you need both those JARs on your build
 	 * path
 	 */
 	protected void traverseClasspaths(IProject p, Archive anArchive, List projectCpEntries, Set visitedArchives) {
 		visitedArchives.add(anArchive);
 		String[] manifestCpEntries = anArchive.getManifest().getClassPathTokenized();
 		EARFile earFile = (EARFile) dataModel.getProperty(IJ2EEComponentImportDataModelProperties.FILE);
 		for (int i = 0; i < manifestCpEntries.length; i++) {
 			String uri = ArchiveUtil.deriveEARRelativeURI(manifestCpEntries[i], anArchive);
 			// ensure the entry is valid or skip to the next
 			if (uri == null)
 				continue;
 			File aFile = null;
 			try {
 				aFile = earFile.getFile(uri);
 			} catch (FileNotFoundException notThere) {
 			}
 			if (aFile == null || !aFile.isArchive() || visitedArchives.contains(aFile))
 				continue;
 			Archive depArchive = (Archive) aFile;
 
			IVirtualComponent depComponent = (IVirtualComponent) createdComponentsMap.get(uri);
			IProject prereq = depComponent.getProject();
 
 			if (prereq != null) {
 				addProjectToClasspath(p, prereq, projectCpEntries);
 			} else {
 				addFileToClasspath(p, vComponent.getRootFolder().getFile(uri).getUnderlyingFile(), projectCpEntries);
 				traverseClasspaths(p, depArchive, projectCpEntries, visitedArchives);
 			}
 		}
 	}
 
 	protected void updateComponentClasspaths() {
 		List jarFiles = getEARFile().getArchiveFiles();
 		for (int i = 0; i < jarFiles.size(); i++) {
 			Archive anArchive = (Archive) jarFiles.get(i);
 			IVirtualComponent component = (IVirtualComponent) createdComponentsMap.get(anArchive.getURI());
 			if (component != null)
 				updateProjectClasspath(anArchive, component);
 		}
 	}
 
 }
