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
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchivePackage;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ArchiveRuntimeException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveOptions;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategy;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.common.impl.J2EEResourceFactoryRegistry;
 import org.eclipse.jst.j2ee.internal.earcreation.EAREditModel;
 import org.eclipse.jst.j2ee.internal.earcreation.EARNatureRuntime;
 import org.eclipse.jst.j2ee.internal.earcreation.modulemap.UtilityJARMapping;
 import org.eclipse.jst.j2ee.internal.project.J2EEModuleNature;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.servertarget.IServerTargetConstants;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 
 import com.ibm.wtp.common.logger.proxy.Logger;
 import com.ibm.wtp.emf.workbench.WorkbenchURIConverter;
 
 public class EARProjectLoadStrategyImpl extends LoadStrategyImpl implements IJ2EEImportExportConstants {
 	protected ArrayList filesList;
 	protected IProject project;
 	protected EARFile earFile;
 	protected boolean exportSource = false;
 	protected WorkbenchURIConverter earURIConverter;
 	protected List jarMergers;
 	protected boolean mergeDependentJars = true;
 	protected boolean includeProjectMetaFiles = false;
 
 	/**
 	 * EARProjectSaveStrategyImpl constructor comment.
 	 */
 	public EARProjectLoadStrategyImpl(IProject aProject) {
 		super();
 		project = aProject;
 		filesList = new ArrayList();
 	}
 
 	/**
 	 * For each module project in this ear project, add a module file the list of files in the EAR
 	 */
 	public void addModulesToFiles() {
 		Map moduleProjects = getEARNature().getModuleProjects();
 		//Next get all the absolute paths of the modules
 
 		//Make a set of all the files collected thus far; if the uri of a module collides with an
 		// existing file,
 		//Then remove that file and give the ModuleFile precedence -
 		//ensure there is a workbench validation that checks for this collision
 		//TODO
 		Map existingFiles = getFilesListMapped();
 
 		Iterator it = moduleProjects.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry entry = (Map.Entry) it.next();
 			String uri = (String) entry.getKey();
 			J2EENature nature = (J2EENature) entry.getValue();
 			if (nature == null)
 				continue;
 			try {
 				Archive moduleFile = ((J2EEModuleNature) nature).asArchive(exportSource);
				if( moduleFile == null )
					continue;
 				moduleFile.setURI(uri);
 				setIncludeProjectMetaFiles(moduleFile);
 				if (existingFiles.containsKey(uri))
 					filesList.remove(existingFiles.get(uri));
 				filesList.add(moduleFile);
 				if ((moduleFile.isEJBJarFile() || moduleFile.isApplicationClientFile()) && isMergeDependentJars())
 					mergeDependentJars(moduleFile, nature.getProject());
 			} catch (OpenFailureException oe) {
 				//this was causing problems in import when a project
 				//does not have a dd
 				//String message = ResourceHandler.getString("UNABLE_TO_LOAD_MODULE_ERROR_",new
 				// Object[] {uri,getProject().getName(),oe.getConcatenatedMessages()});
 				// //$NON-NLS-1$
 				//throw new ArchiveRuntimeException(message, oe);
 			} catch (RuntimeException runtime) {
 				String message = EARArchiveOpsResourceHandler.getString("UNABLE_TO_LOAD_MODULE_ERROR_", new Object[]{uri, getProject().getName(), runtime.toString()}); //$NON-NLS-1$
 				throw new ArchiveRuntimeException(message, runtime);
 			}
 		}
 	}
 
 	protected void setIncludeProjectMetaFiles(Archive anArchive) {
 		LoadStrategy strategy = anArchive.getLoadStrategy();
 		if (strategy instanceof J2EELoadStrategyImpl)
 			((J2EELoadStrategyImpl) strategy).setIncludeProjectMetaFiles(includeProjectMetaFiles);
 	}
 
 	/**
 	 * For each loose utility JAR project in this ear project, add an Archive to the list of files
 	 * in the EAR
 	 */
 	public void addLooseUtilityJARsToFiles() {
 		EAREditModel editModel = null;
 		try {
 			editModel = getEARNature().getEarEditModelForRead(this);
 			List utilMaps = editModel.getUtilityJARMappings();
 
 			Map existingFiles = getFilesListMapped();
 			for (int i = 0; i < utilMaps.size(); i++) {
 				UtilityJARMapping map = (UtilityJARMapping) utilMaps.get(i);
 				String uri = map.getUri();
 				String projectName = map.getProjectName();
 				try {
 					Archive utilJAR = J2EEProjectUtilities.asArchive(uri, projectName, exportSource);
 					if (utilJAR == null)
 						continue;
 					setIncludeProjectMetaFiles(utilJAR);
 					if (existingFiles.containsKey(uri))
 						filesList.remove(existingFiles.get(uri));
 					filesList.add(utilJAR);
 				} catch (OpenFailureException oe) {
 					String message = EARArchiveOpsResourceHandler.getString("UNABLE_TO_LOAD_MODULE_ERROR_", new Object[]{uri, getProject().getName(), oe.getConcatenatedMessages()}); //$NON-NLS-1$
 					com.ibm.wtp.common.logger.proxy.Logger.getLogger().logTrace(message);
 				}
 			}
 		} finally {
 			if (editModel != null)
 				editModel.releaseAccess(this);
 		}
 	}
 
 	public void close() {
 		super.close();
 		if (jarMergers == null)
 			return;
 
 		for (int i = 0; i < jarMergers.size(); i++) {
 			((DependentJarExportMerger) jarMergers.get(i)).release();
 		}
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public boolean contains(String uri) {
 
 		try {
 			EARNatureRuntime.getRuntime(project);
 			return project.getFile(uri).exists(); //$NON-NLS-1$
 		} catch (Exception e) {
 			throw new ArchiveRuntimeException(e.getMessage(), e);
 		}
 	}
 
 	/* should never get called outside of runtime */
 	protected boolean primContains(String uri) {
 		return false;
 	}
 
 	public EARFile getEARFile() {
 		return (EARFile) getContainer();
 	}
 
 	protected EARNatureRuntime getEARNature() {
 		return EARNatureRuntime.getRuntime(project);
 	}
 
 	/**
 	 * save method comment.
 	 */
 	public WorkbenchURIConverter getEARURIConverter() {
 		EARNatureRuntime enr = EARNatureRuntime.getRuntime(project);
 		if (enr == null)
 			return null;
 		earURIConverter = (WorkbenchURIConverter) (enr.getResourceSet().getURIConverter());
 		return earURIConverter;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public IFile getFileForURI(String uri) throws FileNotFoundException {
 
 		try {
 			if (earURIConverter == null)
 				getEARURIConverter();
 			IResource rfile = earURIConverter.getInputContainer().findMember(uri);
 			return (IFile) rfile;
 		} catch (Exception e) {
 
 			String errorString = EARArchiveOpsResourceHandler.getString("ARCHIVE_OPERATION_FileNotFound") + uri; //$NON-NLS-1$
 			throw new FileNotFoundException(errorString);
 		}
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public java.util.List getFiles() {
 
 		filesList.clear();
 		try {
 			filesList = getFiles(java.util.Arrays.asList(project.members()));
 			addModulesToFiles();
 			addLooseUtilityJARsToFiles();
 		} catch (Exception exc) {
 			throw new ArchiveRuntimeException(EARArchiveOpsResourceHandler.getString("ARCHIVE_OPERATION_FilesFromProject"), exc); //$NON-NLS-1$
 		}
 		return filesList;
 	}
 
 	protected long getLastModified(IResource aResource) {
 		return aResource.getLocation().toFile().lastModified();
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	private java.util.ArrayList getFiles(List projectResources) throws Exception {
 		if (projectResources.isEmpty()) {
 			return filesList;
 		}
 		Iterator iterator = projectResources.iterator();
 		while (iterator.hasNext()) {
 			IResource res = (IResource) (iterator.next());
 			if ((res.getType() == IResource.FILE)) {
 				if (isServerTargetFile(res))
 					continue;
 				if (isProjectMetaFile(res) && !includeProjectMetaFiles)
 					continue;
 				if (isProjectSupportFile(res))
 					continue;
 				File cFile = createFile(res.getProjectRelativePath().toString());
 				cFile.setLastModified(getLastModified(res));
 				if (cFile.isArchive())
 					((Archive) cFile).getOptions().setIsReadOnly(true);
 
 				filesList.add(cFile);
 			} else {
 				getFiles(java.util.Arrays.asList(((IContainer) res).members()));
 			}
 		}
 		return filesList;
 	}
 
 	protected boolean isProjectSupportFile(IResource res) {
 		return (res.getProjectRelativePath().toString().equals(PROJECT_RUNTIME_URI) || res.getProjectRelativePath().toString().equals(J2EE_SETTING_URI));
 	}
 
 	protected boolean isProjectMetaFile(IResource res) {
 		return (res.getProjectRelativePath().toString().equals(EAREditModel.MODULE_MAP_URI) || res.getProjectRelativePath().toString().equals(PROJECT_FILE_URI));
 	}
 
 	protected boolean isServerTargetFile(IResource res) {
 		return res.getProjectRelativePath().toString().endsWith(IServerTargetConstants.SERVER_FILE_NAME);
 	}
 
 	protected Map getFilesListMapped() {
 		Map existingFiles = new HashMap();
 		for (int i = 0; i < filesList.size(); i++) {
 			existingFiles.put(((File) filesList.get(i)).getURI(), filesList.get(i));
 		}
 		return existingFiles;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.LoadStrategy
 	 */
 	public java.io.InputStream getInputStream(String uri) throws java.io.IOException, java.io.FileNotFoundException {
 
 		IFile file = getFileForURI(uri);
 		if (!(file == null) && (file.exists())) {
 			try {
 				return file.getContents(true);
 			} catch (CoreException e) {
 				String errorString = EARArchiveOpsResourceHandler.getString("ARCHIVE_OPERATION_FileContents");//$NON-NLS-1$
 				throw new IOException(errorString);
 			}
 
 		}
 		String eString = EARArchiveOpsResourceHandler.getString("ARCHIVE_OPERATION_FileNotFound");//$NON-NLS-1$
 		throw new FileNotFoundException(eString);
 	}
 
 	protected List getJarMergers() {
 		if (jarMergers == null)
 			jarMergers = new ArrayList();
 
 		return jarMergers;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 5:02:59 PM)
 	 * 
 	 * @return org.eclipse.core.resources.IProject
 	 */
 	public org.eclipse.core.resources.IProject getProject() {
 		return project;
 	}
 
 	public boolean isClassLoaderNeeded() {
 		return false;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (6/29/2001 12:51:40 PM)
 	 * 
 	 * @return boolean
 	 */
 	public boolean isExportSource() {
 		return exportSource;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 5:50:40 PM)
 	 * 
 	 * @return boolean
 	 */
 	public boolean isMergeDependentJars() {
 		return mergeDependentJars;
 	}
 
 	protected void mergeDependentJars(Archive moduleFile, IProject aProject) {
 		DependentJarExportMerger merger = new DependentJarExportMerger(moduleFile, aProject, isExportSource());
 		getJarMergers().add(merger);
 		merger.merge();
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (6/29/2001 12:51:40 PM)
 	 * 
 	 * @param newExportSource
 	 *            boolean
 	 */
 	public void setExportSource(boolean newExportSource) {
 		exportSource = newExportSource;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (8/28/2001 5:50:40 PM)
 	 * 
 	 * @param newMergeDependentJars
 	 *            boolean
 	 */
 	public void setMergeDependentJars(boolean newMergeDependentJars) {
 		mergeDependentJars = newMergeDependentJars;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (11/08/00 5:20:53 PM)
 	 * 
 	 * @param newProject
 	 *            com.ibm.itp.core.api.resources.IProject
 	 */
 	public void setProject(IProject newProject) {
 		project = newProject;
 	}
 
 	/**
 	 * Returns the includeProjectMetaFiles.
 	 * 
 	 * @return boolean
 	 */
 	public boolean shouldIncludeProjectMetaFiles() {
 		return includeProjectMetaFiles;
 	}
 
 
 	/**
 	 * Sets the includeProjectMetaFiles.
 	 * 
 	 * @param includeProjectMetaFiles
 	 *            The includeProjectMetaFiles to set
 	 */
 	public void setIncludeProjectMetaFiles(boolean includeProjectMetaFiles) {
 		this.includeProjectMetaFiles = includeProjectMetaFiles;
 	}
 
 	/**
 	 * @see com.ibm.etools.archive.impl.LoadStrategyImpl#openNestedArchive(String)
 	 */
 	protected Archive openNestedArchive(String uri) {
 		IFile aFile = project.getFile(uri);
 		if (aFile == null || !aFile.exists())
 			return null;
 		java.io.File ioFile = aFile.getLocation().toFile();
 		Archive result = null;
 		try {
 			LoadStrategy lStrat = getArchiveFactory().createLoadStrategy(ioFile.getAbsolutePath());
 			ArchiveOptions opts = getEARFile().getOptions().cloneWith(lStrat);
 			result = ((CommonarchivePackage) EPackage.Registry.INSTANCE.getEPackage(CommonarchivePackage.eNS_URI)).getCommonarchiveFactory().openArchive(opts, uri);
 		} catch (OpenFailureException ex) {
 			Logger.getLogger().logError(ex);
 		} catch (IOException ex) {
 			Logger.getLogger().logError(ex);
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.commonarchivecore.strategy.LoadStrategyImpl#initializeResourceSet()
 	 */
 	protected void initializeResourceSet() {
 		resourceSet = WorkbenchResourceHelper.getResourceSet(project);
 	}
 
 	/*
 	 * Overrode from super to bypass SAX
 	 */
 	protected Registry createResourceFactoryRegistry() {
 		return new J2EEResourceFactoryRegistry();
 	}
 
 }
