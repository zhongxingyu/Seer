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
 package org.eclipse.emf.emfstore.client.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.ConflictResolver;
 import org.eclipse.emf.emfstore.client.model.controller.callbacks.CommitCallback;
 import org.eclipse.emf.emfstore.client.model.controller.callbacks.UpdateCallback;
 import org.eclipse.emf.emfstore.client.model.exceptions.MEUrlResolutionException;
 import org.eclipse.emf.emfstore.client.model.filetransfer.FileDownloadStatus;
 import org.eclipse.emf.emfstore.client.model.filetransfer.FileInformation;
 import org.eclipse.emf.emfstore.client.model.impl.OperationManager;
 import org.eclipse.emf.emfstore.client.properties.PropertyManager;
 import org.eclipse.emf.emfstore.common.model.EMFStoreProperty;
 import org.eclipse.emf.emfstore.common.model.IdentifiableElement;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.FileTransferException;
 import org.eclipse.emf.emfstore.server.model.FileIdentifier;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.OrgUnitProperty;
 import org.eclipse.emf.emfstore.server.model.url.ModelElementUrlFragment;
 import org.eclipse.emf.emfstore.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.BranchVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 
 /**
  * <!-- begin-user-doc --> A representation of the model object ' <em><b>Project Container</b></em>'. <!-- end-user-doc
  * -->
  * 
  * <p>
  * The following features are supported:
  * <ul>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProject <em> Project</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectId
  * <em>Project Id</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectName
  * <em>Project Name</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectDescription
  * <em>Project Description</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getEvents <em> Events</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getUsersession
  * <em>Usersession</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getLastUpdated
  * <em>Last Updated</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getBaseVersion
  * <em>Base Version</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getResourceCount
  * <em>Resource Count</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#isDirty <em> Dirty</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getOldLogMessages
  * <em>Old Log Messages</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getLocalOperations
  * <em>Local Operations</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getNotifications
  * <em>Notifications</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getEventComposite
  * <em>Event Composite</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getNotificationComposite
  * <em>Notification Composite </em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getWaitingUploads
  * <em>Waiting Uploads</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProperties
  * <em>Properties</em>}</li>
  * <li>
  * {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getChangedSharedProperties
  * <em>Changed Shared Properties</em>}</li>
  * </ul>
  * </p>
  * 
  * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace()
  * @model
  * @generated
  */
 public interface ProjectSpace extends IdentifiableElement {
 
 	/**
 	 * Adds a file to this project space. The file will be uploaded to the
 	 * EMFStore upon a commit. As long as the file is not yet committed, it can
 	 * be removed by first retrieving the {@link FileInformation} via {@link #getFileInfo(FileIdentifier)} and then
 	 * remove it via {@link FileInformation#cancelPendingUpload()}.
 	 * 
 	 * @param file
 	 *            to be added to the project space
 	 * @return The file identifier the file was assigned to. This identifier can
 	 *         be used to retrieve the file later on
 	 * @throws FileTransferException
 	 *             if any error occurs
 	 * 
 	 * @generated NOT
 	 */
 	FileIdentifier addFile(File file) throws FileTransferException;
 
 	/**
 	 * Adds a list of operations to this project space.
 	 * 
 	 * @param operations
 	 *            the list of operations to be added
 	 * 
 	 * @generated NOT
 	 */
 	void addOperations(List<? extends AbstractOperation> operations);
 
 	/**
 	 * Adds a tag to the specified version of this project.
 	 * 
 	 * @param versionSpec
 	 *            the versionSpec
 	 * @param tag
 	 *            the tag
 	 * @throws EmfStoreException
 	 *             if exception occurs on the server
 	 * 
 	 * @generated NOT
 	 */
 	void addTag(PrimaryVersionSpec versionSpec, TagVersionSpec tag) throws EmfStoreException;
 
 	/**
 	 * Begin a composite operation on the projectSpace.
 	 * 
 	 * @return a handle to abort or complete the operation
 	 * 
 	 * @generated NOT
 	 */
 	CompositeOperationHandle beginCompositeOperation();
 
 	/**
 	 * Commits all pending changes of the project space.
 	 * 
 	 * @throws EmfStoreException
 	 *             in case the commit went wrong
 	 * 
 	 * @return the current version spec
 	 **/
 	PrimaryVersionSpec commit() throws EmfStoreException;
 
 	/**
 	 * Commits all pending changes of the project space.
 	 * 
 	 * @param logMessage
 	 *            a log message describing the changes to be committed
 	 * @param callback
 	 *            an optional callback method to be performed while the commit
 	 *            is in progress, may be <code>null</code>
 	 * @param monitor
 	 *            an optional progress monitor to be used while the commit is in
 	 *            progress, may be <code>null</code>
 	 * 
 	 * @return the current version spec
 	 * 
 	 * @throws EmfStoreException
 	 *             in case the commit went wrong
 	 * 
 	 * @generated NOT
 	 */
 	PrimaryVersionSpec commit(LogMessage logMessage, CommitCallback callback, IProgressMonitor monitor)
 		throws EmfStoreException;
 
 	/**
 	 * This method allows to commit changes to a new branch. It works very
 	 * similar to {@link #commit()} with the addition of a Branch specifier.
 	 * Once the branch is created use {@link #commit()} for further commits.
 	 * 
 	 * 
 	 * @param branch
 	 *            branch specifier
 	 * @param logMessage
 	 *            optional logmessage
 	 * @param callback
 	 *            optional callback, passing an implementation is recommended
 	 * @param monitor
 	 *            optional progress monitor
 	 * @return the created version
 	 * @throws EmfStoreException
 	 *             in case of an exception
 	 */
 	PrimaryVersionSpec commitToBranch(BranchVersionSpec branch, LogMessage logMessage, CommitCallback callback,
 		IProgressMonitor monitor) throws EmfStoreException;
 
 	/**
 	 * Allows to merge a version from another branch into the current
 	 * projectspace.
 	 * 
 	 * @param branchSpec
 	 *            the version which is supposed to be merged
 	 * @param conflictResolver
 	 *            a {@link ConflictResolver} for conflict resolving
 	 * @throws EmfStoreException
 	 *             in case of an exception
 	 */
 	void mergeBranch(PrimaryVersionSpec branchSpec, ConflictResolver conflictResolver) throws EmfStoreException;
 
 	/**
 	 * Returns a list of branches of the current project. Every call triggers a
 	 * server call.
 	 * 
 	 * @return list of {@link BranchInfo}
 	 * @throws EmfStoreException
 	 *             in case of an exception
 	 */
 	List<BranchInfo> getBranches() throws EmfStoreException;
 
 	/**
 	 * Export all local changes to a file.
 	 * 
 	 * @param file
 	 *            the file being exported to
 	 * @throws IOException
 	 *             if writing to the given file fails
 	 * 
 	 * @generated NOT
 	 */
 	void exportLocalChanges(File file) throws IOException;
 
 	/**
 	 * Export all local changes to a file.
 	 * 
 	 * @param file
 	 *            the file being exported to
 	 * @param progressMonitor
 	 *            the progress monitor that should be used while exporting
 	 * @throws IOException
 	 *             if writing to the given file fails
 	 * 
 	 * @generated NOT
 	 */
 	void exportLocalChanges(File file, IProgressMonitor progressMonitor) throws IOException;
 
 	/**
 	 * Export a project to the given file.
 	 * 
 	 * @param file
 	 *            the file being exported to
 	 * @throws IOException
 	 *             if writing to the given file fails
 	 * 
 	 * @generated NOT
 	 */
 	void exportProject(File file) throws IOException;
 
 	/**
 	 * Export a project to the given file.
 	 * 
 	 * @param file
 	 *            the file being exported to
 	 * @param progressMonitor
 	 *            the progress monitor that should be used during the export
 	 * @throws IOException
 	 *             if writing to the given file fails
 	 * 
 	 * @generated NOT
 	 */
 	void exportProject(File file, IProgressMonitor progressMonitor) throws IOException;
 
 	/**
 	 * Returns the value of the '<em><b>Base Version</b></em>' containment
 	 * reference. <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Base Version</em>' containment reference isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Base Version</em>' containment reference.
 	 * @see #setBaseVersion(PrimaryVersionSpec)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_BaseVersion()
 	 * @model containment="true" resolveProxies="true" required="true"
 	 * @generated
 	 */
 	PrimaryVersionSpec getBaseVersion();
 
 	/**
 	 * Returns the value of the '<em><b>Changed Shared Properties</b></em>'
 	 * reference list. The list contents are of type {@link org.eclipse.emf.emfstore.common.model.EMFStoreProperty}.
 	 * <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Changed Shared Properties</em>' map isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Changed Shared Properties</em>' reference
 	 *         list.
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_ChangedSharedProperties()
 	 * @model
 	 * @generated
 	 */
 	EList<EMFStoreProperty> getChangedSharedProperties();
 
 	/**
 	 * Returns the value of the '<em><b>Workspace</b></em>' container reference.
 	 * It is bidirectional and its opposite is '
 	 * {@link org.eclipse.emf.emfstore.client.model.Workspace#getProjectSpaces
 	 * <em>Project Spaces</em>}'. <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Workspace</em>' container reference isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Workspace</em>' container reference.
 	 * @see #setWorkspace(Workspace)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_Workspace()
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getProjectSpaces
 	 * @model opposite="projectSpaces" transient="false"
 	 * @generated
 	 */
 	Workspace getWorkspace();
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getWorkspace
 	 * <em>Workspace</em>} ' container reference. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Workspace</em>' container reference.
 	 * @see #getWorkspace()
 	 * @generated
 	 */
 	void setWorkspace(Workspace value);
 
 	/**
 	 * Returns the value of the '<em><b>Local Change Package</b></em>'
 	 * containment reference. <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Local Change Package</em>' containment reference isn't clear, there really should be
 	 * more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Local Change Package</em>' containment
 	 *         reference.
 	 * @see #setLocalChangePackage(ChangePackage)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_LocalChangePackage()
 	 * @model containment="true" resolveProxies="true"
 	 * @generated
 	 */
 	ChangePackage getLocalChangePackage();
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getLocalChangePackage
 	 * <em>Local Change Package</em>}' containment reference. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Local Change Package</em>'
 	 *            containment reference.
 	 * @see #getLocalChangePackage()
 	 * @generated
 	 */
 	void setLocalChangePackage(ChangePackage value);
 
 	/**
 	 * Returns the value of the '<em><b>Merged Version</b></em>' containment
 	 * reference. <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Merged Version</em>' containment reference isn't clear, there really should be more of
 	 * a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Merged Version</em>' containment reference.
 	 * @see #setMergedVersion(PrimaryVersionSpec)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_MergedVersion()
 	 * @model containment="true" resolveProxies="true"
 	 * @generated
 	 */
 	PrimaryVersionSpec getMergedVersion();
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getMergedVersion
 	 * <em>Merged Version</em>}' containment reference. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Merged Version</em>' containment
 	 *            reference.
 	 * @see #getMergedVersion()
 	 * @generated
 	 */
 	void setMergedVersion(PrimaryVersionSpec value);
 
 	/**
 	 * @return a list of the change packages between two PrimarySpecVersions.
 	 * @param sourceVersion
 	 *            the source version spec
 	 * @param targetVersion
 	 *            the target version spec
 	 * @throws EmfStoreException
 	 *             if any error in the EmfStore occurs
 	 * @generated NOT
 	 */
 	List<ChangePackage> getChanges(VersionSpec sourceVersion, VersionSpec targetVersion) throws EmfStoreException;
 
 	/**
 	 * Gets a file with a specific identifier. If the file is not cached
 	 * locally, it is tried to download the file if a connection to the sever
 	 * exists. If the file cannot be found locally and not on the server (or the
 	 * server isn't reachable), a FileTransferException is thrown. Such an
 	 * exception is also thrown if other errors occur while trying to download
 	 * the file. The method returns not the file itself, because it does not
 	 * block in case of downloading the file. Instead, it returns a status
 	 * object which can be queried for the status of the download. Once the
 	 * download is finished ( status.isFinished() ), the file can be retrieved
 	 * from this status object by calling status.getTransferredFile().
 	 * 
 	 * @param fileIdentifier
 	 *            file identifier string.
 	 * @return a status object that can be used to retrieve various information
 	 *         about the file.
 	 * @throws FileTransferException
 	 *             if any error occurs retrieving the files
 	 * 
 	 * @generated NOT
 	 */
 	FileDownloadStatus getFile(FileIdentifier fileIdentifier) throws FileTransferException;
 
 	/**
 	 * Gets the file information for a specific file identifier. This file
 	 * information can be used to access further details of a file (if it
 	 * exists, is cached, is a pending upload). It can also be used to alter the
 	 * file in limited ways (like removing a pending upload). The
 	 * FileInformation class is basically a facade to keep the interface in the
 	 * project space small (only getFileInfo) while still providing a rich
 	 * interface for files.
 	 * 
 	 * @param fileIdentifier
 	 *            the file identifier for which to get the information
 	 * @return the information for that identifier.
 	 * 
 	 * @generated NOT
 	 */
 	FileInformation getFileInfo(FileIdentifier fileIdentifier);
 
 	/**
 	 * Gets a list of history infos.
 	 * 
 	 * @param query
 	 *            the query to be performed in order to fetch the history
 	 *            information
 	 * 
 	 * @see Workspace
 	 * @return a list of history infos
 	 * @throws EmfStoreException
 	 *             if server the throws an exception
 	 * @generated NOT
 	 */
 	List<HistoryInfo> getHistoryInfo(HistoryQuery query) throws EmfStoreException;
 
 	/**
 	 * Returns the value of the '<em><b>Last Updated</b></em>' attribute. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Last Updated</em>' attribute isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Last Updated</em>' attribute.
 	 * @see #setLastUpdated(Date)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_LastUpdated()
 	 * @model
 	 * @generated
 	 */
 	Date getLastUpdated();
 
 	/**
 	 * Gathers all local operations and canonizes them.
 	 * 
 	 * @param canonized
 	 *            true if the operations should be canonized
 	 * @return the list of operations
 	 * 
 	 * @generated NOT
 	 */
 	ChangePackage getLocalChangePackage(boolean canonized);
 
 	/**
 	 * Returns the value of the '<em><b>Local Operations</b></em>' containment
 	 * reference. <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Local Operations</em>' containment reference isn't clear, there really should be more
 	 * of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Local Operations</em>' containment
 	 *         reference.
 	 * @see #setLocalOperations(OperationComposite)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_LocalOperations()
 	 * @model containment="true" resolveProxies="true"
 	 * @generated
 	 */
 	OperationComposite getLocalOperations();
 
 	/**
 	 * @return modified model elements cache. This is class clients (e.g. dirty
 	 *         decorator) can ask to see if a model element has been modified.
 	 * 
 	 * @generated NOT
 	 */
 	ModifiedModelElementsCache getModifiedModelElementsCache();
 
 	/**
 	 * Returns the value of the '<em><b>Old Log Messages</b></em>' attribute
 	 * list. The list contents are of type {@link java.lang.String}. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Old Log Messages</em>' attribute list isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Old Log Messages</em>' attribute list.
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_OldLogMessages()
 	 * @model
 	 * @generated
 	 */
 	EList<String> getOldLogMessages();
 
 	/**
 	 * Get the {@link OperationManager} for this {@link ProjectSpace}.
 	 * 
 	 * @return the operation manager
 	 * @generated NOT
 	 */
 	OperationManager getOperationManager();
 
 	/**
 	 * Return the list of operations that have already been performed on the
 	 * project space.
 	 * 
 	 * @return a list of operations
 	 * @generated NOT
 	 */
 	List<AbstractOperation> getOperations();
 
 	/**
 	 * Returns the value of the '<em><b>Project</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Project</em>' reference isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Project</em>' containment reference.
 	 * @see #setProject(Project)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_Project()
 	 * @model containment="true" resolveProxies="true"
 	 * @generated
 	 */
 	Project getProject();
 
 	/**
 	 * Returns the value of the '<em><b>Project Description</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Project Description</em>' attribute isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Project Description</em>' attribute.
 	 * @see #setProjectDescription(String)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_ProjectDescription()
 	 * @model required="true"
 	 * @generated
 	 */
 	String getProjectDescription();
 
 	/**
 	 * Returns the value of the '<em><b>Project Id</b></em>' containment
 	 * reference. <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Project Id</em>' containment reference isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Project Id</em>' containment reference.
 	 * @see #setProjectId(ProjectId)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_ProjectId()
 	 * @model containment="true" resolveProxies="true" required="true"
 	 * @generated
 	 */
 	ProjectId getProjectId();
 
 	/**
 	 * Get a project info for the project space.
 	 * 
 	 * @return a project info
 	 * 
 	 * @generated NOT
 	 */
 	ProjectInfo getProjectInfo();
 
 	/**
 	 * Returns the value of the '<em><b>Project Name</b></em>' attribute. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Project Name</em>' attribute isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Project Name</em>' attribute.
 	 * @see #setProjectName(String)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_ProjectName()
 	 * @model required="true"
 	 * @generated
 	 */
 	String getProjectName();
 
 	/**
 	 * Returns the value of the '<em><b>Properties</b></em>' containment
 	 * reference list. The list contents are of type {@link org.eclipse.emf.emfstore.common.model.EMFStoreProperty}.
 	 * <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Properties</em>' map isn't clear, there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Properties</em>' containment reference
 	 *         list.
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_Properties()
 	 * @model containment="true" resolveProxies="true"
 	 * @generated
 	 */
 	EList<EMFStoreProperty> getProperties();
 
 	/**
 	 * Get the {@link PropertyManager} for this {@link ProjectSpace}.
 	 * 
 	 * @return the property manager
 	 * @generated NOT
 	 */
 	PropertyManager getPropertyManager();
 
 	/**
 	 * Returns the value of the '<em><b>Usersession</b></em>' reference. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Usersession</em>' reference isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Usersession</em>' reference.
 	 * @see #setUsersession(Usersession)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_Usersession()
 	 * @model
 	 * @generated
 	 */
 	Usersession getUsersession();
 
 	/**
 	 * Returns the value of the '<em><b>Waiting Uploads</b></em>' containment
 	 * reference list. The list contents are of type {@link org.eclipse.emf.emfstore.server.model.FileIdentifier}. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Waiting Uploads</em>' containment reference list isn't clear, there really should be
 	 * more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Waiting Uploads</em>' containment reference
 	 *         list.
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_WaitingUploads()
 	 * @model containment="true" resolveProxies="true"
 	 * @generated
 	 */
 	EList<FileIdentifier> getWaitingUploads();
 
 	/**
 	 * Import changes from a file.
 	 * 
 	 * @param fileName
 	 *            the file name to import from
 	 * @throws IOException
 	 *             if file access fails
 	 * @generated NOT
 	 */
 	void importLocalChanges(String fileName) throws IOException;
 
 	/**
 	 * Initialize the project space and its resources.
 	 * 
 	 * @generated NOT
 	 */
 	void init();
 
 	/**
 	 * Initialize the resources of the project space.
 	 * 
 	 * @param resourceSet
 	 *            the resource set the project space should use
 	 * @generated NOT
 	 */
 	void initResources(ResourceSet resourceSet);
 
 	/**
 	 * Deletes the project space.
 	 * 
 	 * @generated NOT
 	 * 
 	 * @throws IOException
 	 *             in case the project space could not be deleted
 	 */
 	void delete() throws IOException;
 
 	/**
 	 * Returns the resource set of the ProjectSpace.
 	 * 
 	 * @return resource set of the ProjectSpace
 	 */
 	ResourceSet getResourceSet();
 
 	/**
 	 * Sets the resource set of the project space.
 	 * 
 	 * @param resourceSet
 	 *            the resource set to be used by this project space
 	 */
 	void setResourceSet(ResourceSet resourceSet);
 
 	/**
 	 * Returns the value of the '<em><b>Dirty</b></em>' attribute. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Dirty</em>' attribute isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Dirty</em>' attribute.
 	 * @see #setDirty(boolean)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_Dirty()
 	 * @model
 	 * @generated
 	 */
 	boolean isDirty();
 
 	/**
 	 * Shows whether projectSpace is transient.
 	 * 
 	 * @return true, if transient.
 	 * 
 	 * @generated NOT
 	 */
 	boolean isTransient();
 
 	/**
 	 * Determines whether the project is up to date, that is, whether the base
 	 * revision and the head revision are equal.
 	 * 
 	 * @return true, if the project is up to date, false otherwise
 	 * @throws EmfStoreException
 	 *             if the head revision can not be resolved
 	 * 
 	 * @generated NOT
 	 */
 	boolean isUpdated() throws EmfStoreException;
 
 	/**
 	 * Will make the projectSpace transient, it will not make its content or
 	 * changes persistent. Can only be called before the resources or the
 	 * project space have been initialized.
 	 * 
 	 * @generated NOT
 	 */
 	void makeTransient();
 
 	/**
 	 * Merge the changes from current base version to given target version with
 	 * the local operations.
 	 * 
 	 * @param target
 	 *            target version
 	 * @param conflictResolver
 	 *            a conflict resolver that will actually perform the conflict
 	 *            resolution
 	 * 
 	 * 
 	 * @throws EmfStoreException
 	 *             if the connection to the server fails
 	 * @return true if merge was succesful
 	 * 
 	 * @generated NOT
 	 */
 	boolean merge(PrimaryVersionSpec target, ConflictResolver conflictResolver) throws EmfStoreException;
 
 	/**
 	 * Removes a tag to the specified version of this project.
 	 * 
 	 * @param versionSpec
 	 *            the versionSpec
 	 * @param tag
 	 *            the tag
 	 * @throws EmfStoreException
 	 *             if exception occurs on the server
 	 * 
 	 * @generated NOT
 	 */
 	void removeTag(PrimaryVersionSpec versionSpec, TagVersionSpec tag) throws EmfStoreException;
 
 	/**
 	 * Resolve the url to a model element.
 	 * 
 	 * @param modelElementUrlFragment
 	 *            the url
 	 * @return the model element
 	 * @throws MEUrlResolutionException
 	 *             if model element does not exist in project.
 	 * @generated NOT
 	 */
 	EObject resolve(ModelElementUrlFragment modelElementUrlFragment) throws MEUrlResolutionException;
 
 	/**
 	 * <!-- begin-user-doc --> Resolve a version spec to a primary version spec.
 	 * 
 	 * @param versionSpec
 	 *            the spec to resolve
 	 * @return the primary version spec <!-- end-user-doc -->
 	 * @throws EmfStoreException
 	 *             if resolving fails
 	 * @model
 	 * @generated NOT
 	 */
 	PrimaryVersionSpec resolveVersionSpec(VersionSpec versionSpec) throws EmfStoreException;
 
 	/**
 	 * Revert all local changes in the project space. Returns the state of the
 	 * project to that of the project space base version.
 	 * 
 	 * @generated NOT
 	 */
 	void revert();
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getBaseVersion
 	 * <em>Base Version</em>}' containment reference. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Base Version</em>' containment
 	 *            reference.
 	 * @see #getBaseVersion()
 	 * @generated
 	 */
 	void setBaseVersion(PrimaryVersionSpec value);
 
 	/**
 	 * Returns the value of the '<em><b>Resource Count</b></em>' attribute. <!--
 	 * begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Resource Count</em>' attribute isn't clear, there really should be more of a
 	 * description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * 
 	 * @return the value of the '<em>Resource Count</em>' attribute.
 	 * @see #setResourceCount(int)
 	 * @see org.eclipse.emf.emfstore.client.model.ModelPackage#getProjectSpace_ResourceCount()
 	 * @model
 	 * @generated
 	 */
 	int getResourceCount();
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getResourceCount
 	 * <em>Resource Count</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Resource Count</em>' attribute.
 	 * @see #getResourceCount()
 	 * @generated
 	 */
 	void setResourceCount(int value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#isDirty
 	 * <em>Dirty</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Dirty</em>' attribute.
 	 * @see #isDirty()
 	 * @generated
 	 */
 	void setDirty(boolean value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getLastUpdated
 	 * <em>Last Updated</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Last Updated</em>' attribute.
 	 * @see #getLastUpdated()
 	 * @generated
 	 */
 	void setLastUpdated(Date value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getLocalOperations
 	 * <em>Local Operations</em>}' containment reference. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Local Operations</em>' containment
 	 *            reference.
 	 * @see #getLocalOperations()
 	 * @generated
 	 */
 	void setLocalOperations(OperationComposite value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProject
 	 * <em>Project</em>}' containment reference. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Project</em>' containment reference.
 	 * @see #getProject()
 	 * @generated
 	 */
 	void setProject(Project value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectDescription
 	 * <em>Project Description</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Project Description</em>' attribute.
 	 * @see #getProjectDescription()
 	 * @generated
 	 */
 	void setProjectDescription(String value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectId
 	 * <em>Project Id</em>}' containment reference. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Project Id</em>' containment
 	 *            reference.
 	 * @see #getProjectId()
 	 * @generated
 	 */
 	void setProjectId(ProjectId value);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectName
 	 * <em>Project Name</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Project Name</em>' attribute.
 	 * @see #getProjectName()
 	 * @generated
 	 */
 	void setProjectName(String value);
 
 	/**
 	 * Sets a new OrgUnitProperty for the current user.
 	 * 
 	 * @param property
 	 *            the new property
 	 * @generated NOT
 	 */
 	void setProperty(OrgUnitProperty property);
 
 	/**
 	 * Sets the value of the ' {@link org.eclipse.emf.emfstore.client.model.ProjectSpace#getUsersession
 	 * <em>Usersession</em>}' reference. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @param value
 	 *            the new value of the '<em>Usersession</em>' reference.
 	 * @see #getUsersession()
 	 * @generated
 	 */
 	void setUsersession(Usersession value);
 
 	/**
 	 * Shares this project space.
 	 * 
 	 * @throws EmfStoreException
 	 *             if an error occurs during the sharing of the project
 	 */
 	public void shareProject() throws EmfStoreException;
 
 	/**
 	 * Shares this project space.
 	 * 
 	 * @param session
 	 *            the {@link Usersession} that should be used for sharing the
 	 *            project
 	 * @param monitor
 	 *            an instance of an {@link IProgressMonitor}
 	 * 
 	 * @throws EmfStoreException
 	 *             if an error occurs during the sharing of the project
 	 */
 	public void shareProject(Usersession session, IProgressMonitor monitor) throws EmfStoreException;
 
 	/**
 	 * Transmit the OrgUnitproperties to the server.
 	 * 
 	 * @generated NOT
 	 */
 	void transmitProperties();
 
 	/**
 	 * Undo the last operation of the projectSpace.
 	 * 
 	 * @generated NOT
 	 */
 	void undoLastOperation();
 
 	/**
 	 * Undo the last operation <em>n</em> operations of the projectSpace.
 	 * 
 	 * @param nrOperations
 	 *            the number of operations to be undone
 	 * 
 	 * @generated NOT
 	 */
 	void undoLastOperations(int nrOperations);
 
 	/**
 	 * <!-- begin-user-doc --> Update the project to the head version.
 	 * 
 	 * @return the new base version
 	 * @throws EmfStoreException
 	 *             if update fails <!-- end-user-doc -->
 	 * @model
 	 * @generated NOT
 	 */
 	PrimaryVersionSpec update() throws EmfStoreException;
 
 	/**
 	 * <!-- begin-user-doc --> Update the project to the given version.
 	 * 
 	 * @param version
 	 *            the version to update to
 	 * @return the new base version
 	 * @throws EmfStoreException
 	 *             if update fails <!-- end-user-doc -->
 	 * @model
 	 * @generated NOT
 	 */
 	PrimaryVersionSpec update(VersionSpec version) throws EmfStoreException;
 
 	/**
 	 * Update the workspace to the given revision.
 	 * 
 	 * @param version
 	 *            the {@link VersionSpec} to update to
 	 * @param callback
 	 *            the {@link UpdateCallback} that will be called when the update
 	 *            has been performed
 	 * @param progress
 	 *            an {@link IProgressMonitor} instance
 	 * @return the current version spec
 	 * 
 	 * @throws EmfStoreException
 	 *             in case the update went wrong
 	 * @see UpdateCallback#updateCompleted(ProjectSpace, PrimaryVersionSpec, PrimaryVersionSpec)
 	 * @generated NOT
 	 */
 	PrimaryVersionSpec update(VersionSpec version, UpdateCallback callback, IProgressMonitor progress)
 		throws EmfStoreException;
 
 	/**
 	 * Determine if the projectspace has unsave changes to any element in the project.
 	 * 
 	 * @return true if there is unsaved changes.
 	 */
 	boolean hasUnsavedChanges();
 
 	/**
 	 * Saves the project space.
 	 */
 	void save();
 
 	 * Whether this project space has been shared.
 	 * 
 	 * @return true, if the project space has been shared, false otherwise
 	 */
 	boolean isShared();
 } // ProjectContainer
