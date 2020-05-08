 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.core.subinterfaces;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.core.AbstractEmfstoreInterface;
 import org.eclipse.emf.emfstore.internal.server.core.AbstractSubEmfstoreInterface;
 import org.eclipse.emf.emfstore.internal.server.core.helper.EmfStoreMethod;
 import org.eclipse.emf.emfstore.internal.server.core.helper.EmfStoreMethod.MethodId;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.InvalidProjectIdException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.StorageException;
 import org.eclipse.emf.emfstore.internal.server.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectHistory;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectId;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.SessionId;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Version;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 
 /**
  * This subinterface implements all project related functionality.
  * 
  * @author wesendon
  */
 public class ProjectSubInterfaceImpl extends AbstractSubEmfstoreInterface {
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param parentInterface
 	 *            parent interface
 	 * @throws FatalESException
 	 *             in case of failure
 	 */
 	public ProjectSubInterfaceImpl(AbstractEmfstoreInterface parentInterface) throws FatalESException {
 		super(parentInterface);
 	}
 
 	@Override
 	protected void initSubInterface() throws FatalESException {
 		super.initSubInterface();
 	}
 
 	/**
 	 * Returns the corresponding project.
 	 * 
 	 * @param projectId
 	 *            project id
 	 * @return a project or throws exception
 	 * @throws ESException
 	 *             if project couldn't be found
 	 */
 	protected ProjectHistory getProject(ProjectId projectId) throws ESException {
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
 	@EmfStoreMethod(MethodId.GETPROJECT)
 	public Project getProject(ProjectId projectId, VersionSpec versionSpec)
 		throws InvalidVersionSpecException, ESException {
 
 		sanityCheckObjects(projectId, versionSpec);
 
 		synchronized (getMonitor()) {
 
 			PrimaryVersionSpec resolvedVersion = getSubInterface(VersionSubInterfaceImpl.class)
 				.resolveVersionSpec(
 					projectId, versionSpec);
 			Version version = getSubInterface(VersionSubInterfaceImpl.class).getVersion(projectId, resolvedVersion);
 			return getProject(version);
 
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected Project getProject(Version version) throws InvalidVersionSpecException, ESException {
 		if (version.getProjectState() == null) {
 
 			// TODO BRANCH Review potential performance optimization by searching state in both directions
 			ArrayList<Version> versions = new ArrayList<Version>();
 			Version currentVersion = version;
 			while (currentVersion.getProjectState() == null) {
 				versions.add(currentVersion);
 				currentVersion = VersionSubInterfaceImpl.findNextVersion(currentVersion);
 			}
 			if (currentVersion.getProjectState() == null) {
 				// TODO: nicer exception. Is this null check necessary anyway? (there were problems
 				// in past, because the xml files were inconsistent.
 				throw new ESException("Couldn't find project state.");
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
 	 * @throws ESException
 	 * @throws AccessControlException
 	 */
 	@EmfStoreMethod(MethodId.GETPROJECTLIST)
 	public List<ProjectInfo> getProjectList(SessionId sessionId) throws ESException {
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
 	@EmfStoreMethod(MethodId.CREATEEMPTYPROJECT)
 	public ProjectInfo createProject(String name, String description, LogMessage logMessage) throws ESException {
 		sanityCheckObjects(name, description, logMessage);
 		synchronized (getMonitor()) {
 			ProjectHistory projectHistory = null;
 			try {
 				logMessage.setDate(new Date());
 				projectHistory = createEmptyProject(
 					name,
 					description,
 					logMessage,
 					org.eclipse.emf.emfstore.internal.common.model.ModelFactory.eINSTANCE
 						.createProject());
 			} catch (FatalESException e) {
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
 		throws ESException {
 		sanityCheckObjects(name, description, logMessage, project);
 		synchronized (getMonitor()) {
 			ProjectHistory projectHistory = null;
 			try {
 				logMessage.setDate(new Date());
 				projectHistory = createEmptyProject(name, description, logMessage, project);
 			} catch (FatalESException e) {
 				throw new StorageException(StorageException.NOSAVE);
 			}
 
 			return createProjectInfo(projectHistory);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@EmfStoreMethod(MethodId.DELETEPROJECT)
 	public void deleteProject(ProjectId projectId, boolean deleteFiles) throws ESException {
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
 	 * @throws ESException
 	 *             in case of failure
 	 */
 	protected void deleteProject(ProjectId projectId, boolean deleteFiles, boolean throwInvalidIdException)
 		throws ESException {
 		synchronized (getMonitor()) {
 			try {
 				ProjectHistory project = getProject(projectId);
 				getServerSpace().getProjects().remove(project);
 				try {
 					save(getServerSpace());
 				} catch (FatalESException e) {
 					throw new StorageException(StorageException.NOSAVE);
 				} finally {
 					// delete resources
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
					if (project.eResource() != null) {
						project.eResource().delete(null);
					}
 				}
 			} catch (InvalidProjectIdException e) {
 				if (throwInvalidIdException) {
 					throw e;
 				}
 			} catch (IOException e) {
 				throw new StorageException("Project resource files couldn't be deleted.", e);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@EmfStoreMethod(MethodId.IMPORTPROJECTHISTORYTOSERVER)
 	public ProjectId importProjectHistoryToServer(ProjectHistory projectHistory) throws ESException {
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
 						getResourceHelper().createResourceForChangePackage(
 							version.getChanges(),
 							version.getPrimarySpec(),
 							projectHistory.getProjectId());
 					}
 					if (version.getProjectState() != null) {
 						getResourceHelper().createResourceForProject(
 							version.getProjectState(),
 							version.getPrimarySpec(),
 							projectHistory.getProjectId());
 					}
 					getResourceHelper().createResourceForVersion(version, projectHistory.getProjectId());
 				}
 				getResourceHelper().save(projectHistory);
 				getResourceHelper().saveAll();
 			} catch (FatalESException e) {
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
 	public ProjectHistory exportProjectHistoryFromServer(ProjectId projectId) throws ESException {
 		sanityCheckObjects(projectId);
 		synchronized (getMonitor()) {
 			return ModelUtil.clone(getProject(projectId));
 		}
 	}
 
 	private ProjectHistory createEmptyProject(String name, String description, LogMessage logMessage,
 		Project initialProjectState) throws FatalESException {
 
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
 		// org.eclipse.emf.emfstore.internal.common.model.ModelFactory.eINSTANCE
 		// .createProject();
 		// firstVersion.setProjectState(initialProjectState);
 		getResourceHelper().createResourceForProject(initialProjectState, firstVersion.getPrimarySpec(),
 			projectHistory.getProjectId());
 		projectHistory.getVersions().add(firstVersion);
 
 		// add to serverspace and saved
 		getResourceHelper().createResourceForVersion(firstVersion, projectHistory.getProjectId());
 		getResourceHelper().createResourceForProjectHistory(projectHistory);
 		getServerSpace().getProjects().add(projectHistory);
 		save(getServerSpace());
 
 		firstVersion.setProjectStateResource(initialProjectState.eResource());
 
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
