 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.core.subinterfaces;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.FileUtil;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.EmfStoreController;
 import org.eclipse.emf.emfstore.server.core.AbstractEmfstoreInterface;
 import org.eclipse.emf.emfstore.server.core.AbstractSubEmfstoreInterface;
 import org.eclipse.emf.emfstore.server.core.helper.EmfStoreMethod;
 import org.eclipse.emf.emfstore.server.core.helper.EmfStoreMethod.MethodId;
 import org.eclipse.emf.emfstore.server.core.helper.HistoryCache;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidProjectIdException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.server.exceptions.StorageException;
 import org.eclipse.emf.emfstore.server.model.ModelFactory;
 import org.eclipse.emf.emfstore.server.model.ProjectHistory;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.SessionId;
 import org.eclipse.emf.emfstore.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.Version;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 
 /**
  * This subinterfaces implements all project related functionality for the
  * {@link org.eclipse.emf.emfstore.server.core.EmfStoreImpl} interface.
  * 
  * @author wesendon
  */
 public class ProjectSubInterfaceImpl extends AbstractSubEmfstoreInterface {
 
 	private HistoryCache historyCache;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param parentInterface
 	 *            parent interface
 	 * @throws FatalEmfStoreException
 	 *             in case of failure
 	 */
 	public ProjectSubInterfaceImpl(AbstractEmfstoreInterface parentInterface) throws FatalEmfStoreException {
 		super(parentInterface);
 	}
 
 	@Override
 	protected void initSubInterface() throws FatalEmfStoreException {
 		super.initSubInterface();
 		historyCache = EmfStoreController.getHistoryCache(getServerSpace(), false);
 	}
 
 	/**
 	 * Returns the corresponding project.
 	 * 
 	 * @param projectId
 	 *            project id
 	 * @return a project or throws exception
 	 * @throws EmfStoreException
 	 *             if project couldn't be found
 	 */
 	protected ProjectHistory getProject(ProjectId projectId) throws EmfStoreException {
 		ProjectHistory projectHistory = getProjectOrNull(projectId);
 		if (projectHistory != null) {
 			return projectHistory;
 		}
 		throw new InvalidProjectIdException("Project with the id:" + ((projectId == null) ? "null" : projectId)
 			+ " doesn't exist.");
 	}
 
 	private ProjectHistory getProjectOrNull(ProjectId projectId) {
 		for (ProjectHistory project : getServerSpace().getProjects()) {
 			if (project.getProjectId().equals(projectId)) {
 				return project;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	// TODO BRANCH change
 	@EmfStoreMethod(MethodId.GETPROJECT)
 	public Project getProject(ProjectId projectId, VersionSpec versionSpec) throws EmfStoreException {
 		sanityCheckObjects(projectId, versionSpec);
 
 		synchronized (getMonitor()) {
 			PrimaryVersionSpec resolvedVersion = getSubInterface(VersionSubInterfaceImpl.class).resolveVersionSpec(
 				projectId, versionSpec);
 			Version version = getSubInterface(VersionSubInterfaceImpl.class).getVersion(projectId, resolvedVersion);
 			return getProject(version);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected Project getProject(Version version) throws InvalidVersionSpecException, EmfStoreException {
 		if (version.getProjectState() == null) {
 
 			// TODO BRANCH review. performance optimization by searching state in both directions
 			ArrayList<Version> versions = new ArrayList<Version>();
 			Version currentVersion = version;
 			while (currentVersion.getProjectState() == null) {
 				versions.add(currentVersion);
 				currentVersion = VersionSubInterfaceImpl.findNextVersion(currentVersion);
 			}
 			if (currentVersion.getProjectState() == null) {
 				// TODO: nicer exception.
 				// is this null check necessary anyway? (there were problems
 				// in past, because
 				// the xml files were inconsistent.
 				throw new EmfStoreException("Couldn't find project state.");
 			}
 			Project projectState = ModelUtil.clone(currentVersion.getProjectState());
 			Collections.reverse(versions);
 			for (Version vers : versions) {
 				vers.getChanges().apply(projectState);
 			}
 			return projectState;
 		}
 		return version.getProjectState();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws EmfStoreException
 	 * @throws AccessControlException
 	 */
 	@EmfStoreMethod(MethodId.GETPROJECTLIST)
 	public List<ProjectInfo> getProjectList(SessionId sessionId) throws EmfStoreException {
 		synchronized (getMonitor()) {
 			List<ProjectInfo> result = new ArrayList<ProjectInfo>();
 			for (ProjectHistory projectHistory : getServerSpace().getProjects()) {
 				try {
 					getAuthorizationControl().checkReadAccess(sessionId, projectHistory.getProjectId(), null);
 					result.add(createProjectInfo(projectHistory));
 				} catch (AccessControlException e) {
 					// if this exception occurs, project won't be added to list
 				}
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ProjectInfo createProject(String name, String description, LogMessage logMessage) throws EmfStoreException {
 		synchronized (getMonitor()) {
 			ProjectHistory projectHistory = null;
 			try {
 				logMessage.setDate(new Date());
 				projectHistory = createEmptyProject(name, description, logMessage,
 					org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createProject());
 			} catch (FatalEmfStoreException e) {
 				throw new StorageException(StorageException.NOSAVE);
 			}
 			return createProjectInfo(projectHistory);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@EmfStoreMethod(MethodId.CREATEPROJECT)
 	public ProjectInfo createProject(String name, String description, LogMessage logMessage, Project project)
 		throws EmfStoreException {
 		sanityCheckObjects(name, description, logMessage, project);
 		synchronized (getMonitor()) {
 			ProjectHistory projectHistory = null;
 			try {
 				logMessage.setDate(new Date());
 				projectHistory = createEmptyProject(name, description, logMessage, project);
 			} catch (FatalEmfStoreException e) {
 				throw new StorageException(StorageException.NOSAVE);
 			}
 
 			return createProjectInfo(projectHistory);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@EmfStoreMethod(MethodId.DELETEPROJECT)
 	public void deleteProject(ProjectId projectId, boolean deleteFiles) throws EmfStoreException {
 		sanityCheckObjects(projectId);
 		deleteProject(projectId, deleteFiles, true);
 	}
 
 	/**
 	 * Implemenation of {@link #deleteProject(ProjectId, boolean)} with
 	 * additional possibility of not throwing an invalid id exception.
 	 * 
 	 * @param projectId
 	 *            project id
 	 * @param deleteFiles
 	 *            boolean, whether to delete files in file system
 	 * @param throwInvalidIdException
 	 *            boolean
 	 * @throws EmfStoreException
 	 *             in case of failure
 	 */
 	protected void deleteProject(ProjectId projectId, boolean deleteFiles, boolean throwInvalidIdException)
 		throws EmfStoreException {
 		synchronized (getMonitor()) {
 			try {
 				historyCache.removeProjectFromCache(projectId);
 				ProjectHistory project = getProject(projectId);
 				getServerSpace().getProjects().remove(project);
 				try {
 					save(getServerSpace());
 				} catch (FatalEmfStoreException e) {
 					throw new StorageException(StorageException.NOSAVE);
 				} finally {
 					// delete resources
 					project.eResource().delete(null);
 					for (Version version : project.getVersions()) {
 						ChangePackage changes = version.getChanges();
 						if (changes != null) {
 							changes.eResource().delete(null);
 						}
 						Project projectState = version.getProjectState();
 						if (projectState != null) {
 							projectState.eResource().delete(null);
 						}
 						version.eResource().delete(null);
 					}
 				}
 			} catch (InvalidProjectIdException e) {
 				if (throwInvalidIdException) {
 					throw e;
 				}
 			} catch (IOException e) {
 				throw new StorageException("Project resource files couldn't be deleted.", e);
 			} finally {
 				// delete project files
 				if (deleteFiles) {
 					File projectFolder = new File(getResourceHelper().getProjectFolder(projectId));
 					try {
 						FileUtil.deleteDirectory(projectFolder, true);
 					} catch (IOException e) {
 						ModelUtil.logException(
 							"Project files couldn't be deleted, but it was deleted from containment tree.", e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@EmfStoreMethod(MethodId.IMPORTPROJECTHISTORYTOSERVER)
 	public ProjectId importProjectHistoryToServer(ProjectHistory projectHistory) throws EmfStoreException {
 		sanityCheckObjects(projectHistory);
 		synchronized (getMonitor()) {
 			ProjectHistory projectOrNull = getProjectOrNull(projectHistory.getProjectId());
 			if (projectOrNull != null) {
 				// if project with same id exists, create a new id.
 				projectHistory.setProjectId(ModelFactory.eINSTANCE.createProjectId());
 			}
 			try {
 				getResourceHelper().createResourceForProjectHistory(projectHistory);
 				getServerSpace().getProjects().add(projectHistory);
 				getResourceHelper().save(getServerSpace());
 				for (Version version : projectHistory.getVersions()) {
 					if (version.getChanges() != null) {
 						getResourceHelper().createResourceForChangePackage(version.getChanges(),
 							version.getPrimarySpec(), projectHistory.getProjectId());
 					}
 					if (version.getProjectState() != null) {
 						getResourceHelper().createResourceForProject(version.getProjectState(),
 							version.getPrimarySpec(), projectHistory.getProjectId());
 					}
 					getResourceHelper().createResourceForVersion(version, projectHistory.getProjectId());
 				}
 				getResourceHelper().save(projectHistory);
 				getResourceHelper().saveAll();
 			} catch (FatalEmfStoreException e) {
 				// roll back
 				deleteProject(projectHistory.getProjectId(), true, false);
 				throw new StorageException(StorageException.NOSAVE);
 			}
 			return ModelUtil.clone(projectHistory.getProjectId());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@EmfStoreMethod(MethodId.EXPORTPROJECTHISTORYFROMSERVER)
 	public ProjectHistory exportProjectHistoryFromServer(ProjectId projectId) throws EmfStoreException {
 		sanityCheckObjects(projectId);
 		synchronized (getMonitor()) {
 			return ModelUtil.clone(getProject(projectId));
 		}
 	}
 
 	private ProjectHistory createEmptyProject(String name, String description, LogMessage logMessage,
 		Project initialProjectState) throws FatalEmfStoreException {
 
 		// create initial ProjectHistory
 		ProjectHistory projectHistory = ModelFactory.eINSTANCE.createProjectHistory();
 		projectHistory.setProjectName(name);
 		projectHistory.setProjectDescription(description);
 		projectHistory.setProjectId(ModelFactory.eINSTANCE.createProjectId());
 
 		// create a initial version without previous and change package
 		Version firstVersion = VersioningFactory.eINSTANCE.createVersion();
 		firstVersion.setLogMessage(logMessage);
 		PrimaryVersionSpec primary = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 		primary.setIdentifier(0);
 		firstVersion.setPrimarySpec(primary);
 
 		// create branch information
 		BranchInfo branchInfo = VersioningFactory.eINSTANCE.createBranchInfo();
 		branchInfo.setName(VersionSpec.BRANCH_DEFAULT_NAME);
 		branchInfo.setHead(ModelUtil.clone(primary));
 		branchInfo.setSource(ModelUtil.clone(primary));
 		projectHistory.getBranches().add(branchInfo);
 
 		// create initial project
 		// Project project =
 		// org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
 		// .createProject();
 		firstVersion.setProjectState(initialProjectState);
 		getResourceHelper().createResourceForProject(initialProjectState, firstVersion.getPrimarySpec(),
 			projectHistory.getProjectId());
 		projectHistory.getVersions().add(firstVersion);
 
 		// add to serverspace and saved
 		getResourceHelper().createResourceForVersion(firstVersion, projectHistory.getProjectId());
 		getResourceHelper().createResourceForProjectHistory(projectHistory);
 		getServerSpace().getProjects().add(projectHistory);
 		save(getServerSpace());
 
 		// init cache
 		historyCache.initCache(Arrays.asList(projectHistory));
 
 		return projectHistory;
 	}
 
 	private ProjectInfo createProjectInfo(ProjectHistory project) {
 		ProjectInfo info = ModelFactory.eINSTANCE.createProjectInfo();
 		info.setName(project.getProjectName());
 		info.setDescription(project.getProjectDescription());
 		info.setProjectId(ModelUtil.clone(project.getProjectId()));
 		info.setVersion(ModelUtil.clone(project.getLastVersion().getPrimarySpec()));
 		return info;
 	}
 }
