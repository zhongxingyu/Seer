 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * ovonwesen
  * emueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.controller;
 
 import java.io.IOException;
import java.text.MessageFormat;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback;
 import org.eclipse.emf.emfstore.client.exceptions.ESProjectNotSharedException;
 import org.eclipse.emf.emfstore.client.observer.ESUpdateObserver;
 import org.eclipse.emf.emfstore.internal.client.common.UnknownEMFStoreWorkloadCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.ChangeConflictException;
 import org.eclipse.emf.emfstore.internal.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ChangeConflictSet;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictDetector;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ModelElementIdToEObjectMappingImpl;
 import org.eclipse.emf.emfstore.internal.server.impl.api.ESConflictSetImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESChangePackage;
 
 /**
  * Controller class for updating a project space.
  * 
  * @author ovonwesen
  * @author emueller
  */
 public class UpdateController extends ServerCall<PrimaryVersionSpec> {
 
 	private final VersionSpec version;
 	private final ESUpdateCallback callback;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param projectSpace
 	 *            the project space to be updated
 	 * @param version
 	 *            the target version
 	 * @param callback
 	 *            an optional update callback instance
 	 * @param progress
 	 *            a progress monitor that is used to indicate the progress of the update
 	 */
 	public UpdateController(ProjectSpaceBase projectSpace, VersionSpec version, ESUpdateCallback callback,
 		IProgressMonitor progress) {
 		super(projectSpace);
 
 		if (!projectSpace.isShared()) {
 			throw new ESProjectNotSharedException();
 		}
 
 		// SANITY CHECKS
 		if (version == null) {
 			version = Versions.createHEAD(projectSpace.getBaseVersion());
 		}
 		if (callback == null) {
 			callback = ESUpdateCallback.NOCALLBACK;
 		}
 
 		this.version = version;
 		this.callback = callback;
 		setProgressMonitor(progress);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall#run()
 	 */
 	@Override
 	protected PrimaryVersionSpec run() throws ESException {
 		return doUpdate(version);
 	}
 
 	private PrimaryVersionSpec doUpdate(VersionSpec version) throws ChangeConflictException, ESException {
 		getProgressMonitor().beginTask("Updating Project...", 100);
 		getProgressMonitor().worked(1);
 		getProgressMonitor().subTask("Resolving new version");
 		final PrimaryVersionSpec resolvedVersion = getProjectSpace().resolveVersionSpec(version, getProgressMonitor());
 		if (equalsBaseVersion(resolvedVersion)) {
 			return resolvedVersion;
 		}
 		getProgressMonitor().worked(5);
 
 		if (getProgressMonitor().isCanceled()) {
 			return getProjectSpace().getBaseVersion();
 		}
 
 		getProgressMonitor().subTask("Fetching changes from server");
 
 		final List<ChangePackage> incomingChanges = getIncomingChanges(resolvedVersion);
 
 		checkAndRemoveDuplicateOperations(incomingChanges, getProjectSpace().getLocalChangePackage());
 
 		ChangePackage localChanges = getProjectSpace().getLocalChangePackage(false);
 
 		// build a mapping including deleted and create model elements in local and incoming change packages
 		final ModelElementIdToEObjectMappingImpl idToEObjectMapping = new ModelElementIdToEObjectMappingImpl(
 			getProjectSpace().getProject(), incomingChanges);
 		idToEObjectMapping.put(localChanges);
 
 		getProgressMonitor().worked(65);
 
 		if (getProgressMonitor().isCanceled()) {
 			return getProjectSpace().getBaseVersion();
 		}
 
 		getProgressMonitor().subTask("Checking for conflicts");
 
 		final List<ESChangePackage> copy = APIUtil.mapToAPI(ESChangePackage.class, incomingChanges);
 
 		// TODO ASYNC review this cancel
 		if (getProgressMonitor().isCanceled()
 			|| !callback.inspectChanges(getProjectSpace().toAPI(), copy, idToEObjectMapping.toAPI())) {
 			return getProjectSpace().getBaseVersion();
 		}
 
 		ESWorkspaceProviderImpl
 			.getObserverBus()
 			.notify(ESUpdateObserver.class)
 			.inspectChanges(getProjectSpace().toAPI(), copy, getProgressMonitor());
 
 		if (getProjectSpace().getOperations().size() > 0) {
 			final ChangeConflictSet changeConflictSet = calcConflicts(localChanges, incomingChanges, idToEObjectMapping);
 			if (changeConflictSet.getConflictBuckets().size() > 0) {
 				getProgressMonitor().subTask("Conflicts detected, calculating conflicts");
 				if (callback.conflictOccurred(new ESConflictSetImpl(changeConflictSet), getProgressMonitor())) {
 					localChanges = getProjectSpace().mergeResolvedConflicts(changeConflictSet,
 						Collections.singletonList(localChanges),
 						incomingChanges);
 					// continue with update by applying changes
 				} else {
 					throw new ChangeConflictException(changeConflictSet);
 				}
 			}
 		}
 
 		getProgressMonitor().worked(15);
 
 		getProgressMonitor().subTask("Applying changes");
 
 		getProjectSpace().applyChanges(resolvedVersion, incomingChanges, localChanges, getProgressMonitor(), true);
 
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESUpdateObserver.class)
 			.updateCompleted(getProjectSpace().toAPI(), getProgressMonitor());
 
 		return getProjectSpace().getBaseVersion();
 	}
 
 	private void save(EObject eObject, String failureMsg) {
 		try {
 			if (eObject.eResource() != null) {
 				eObject.eResource().save(ModelUtil.getResourceSaveOptions());
 			}
 		} catch (final IOException ex) {
 			ModelUtil.logException(failureMsg, ex);
 		}
 	}
 
 	private boolean equalsBaseVersion(final PrimaryVersionSpec resolvedVersion) {
 		return resolvedVersion.compareTo(getProjectSpace().getBaseVersion()) == 0;
 	}
 
 	private List<ChangePackage> getIncomingChanges(final PrimaryVersionSpec resolvedVersion) throws ESException {
 		return new UnknownEMFStoreWorkloadCommand<List<ChangePackage>>(
 			getProgressMonitor()) {
 			@Override
 			public List<ChangePackage> run(IProgressMonitor monitor) throws ESException {
 				return getConnectionManager().getChanges(getSessionId(), getProjectSpace().getProjectId(),
 					getProjectSpace().getBaseVersion(), resolvedVersion);
 			}
 		}.execute();
 	}
 
 	private ChangeConflictSet calcConflicts(ChangePackage localChanges,
 		List<ChangePackage> changes, ModelElementIdToEObjectMappingImpl idToEObjectMapping) {
 
 		final ConflictDetector conflictDetector = new ConflictDetector();
 		return conflictDetector.calculateConflicts(
 			Collections.singletonList(localChanges), changes, idToEObjectMapping);
 	}
 
 	private void checkAndRemoveDuplicateOperations(List<ChangePackage> incomingChanges,
 		ChangePackage localChanges) {
 
 		final int baseVersionDelta = removeFromChangePackages(incomingChanges, localChanges);
 
 		if (baseVersionDelta == 0) {
 			return;
 		}
 
 		// some change have been matched, fix base version and save
 		final PrimaryVersionSpec baseVersion = getProjectSpace().getBaseVersion();
 		baseVersion.setIdentifier(baseVersion.getIdentifier() + baseVersionDelta);
		ModelUtil.logError(MessageFormat
			.format(
				"{0} change packages removed during update!.\n"
					+ "Pulling up base version from {1} to {2} and persisting changes.",
				baseVersionDelta, baseVersion.getIdentifier(), baseVersion.getIdentifier() + baseVersionDelta));
 		save(getProjectSpace(), "Could not save project space");
 		save(localChanges, "Could not save local changes");
 	}
 
 	public int removeFromChangePackages(List<ChangePackage> incomingChanges, ChangePackage localChanges) {
 		final Iterator<ChangePackage> incomingChangesIterator = incomingChanges.iterator();
 		int baseVersionDelta = 0;
 
 		while (incomingChangesIterator.hasNext()) {
 			final ChangePackage incomingChangePackage = incomingChangesIterator.next();
 			final boolean hasBeenConsumed = removeDuplicateOperations(incomingChangePackage, localChanges);
 			if (hasBeenConsumed) {
 				baseVersionDelta += 1;
 				incomingChangesIterator.remove();
 			} else {
 				break;
 			}
 		}
 
 		return baseVersionDelta;
 	}
 
 	public boolean removeDuplicateOperations(ChangePackage incomingChanges, ChangePackage localChanges) {
 
 		final Iterator<AbstractOperation> localOperationsIterator = localChanges.getOperations().iterator();
 		final List<AbstractOperation> incomingOps = incomingChanges.getOperations();
 		final int incomingOpsSize = incomingOps.size();
 		int incomingIdx = 0;
 		boolean operationMatchingStarted = false;
 
 		if (localChanges.getOperations().size() == 0) {
 			return false;
 		}
 
 		while (localOperationsIterator.hasNext()) {
 			if (incomingIdx == incomingOpsSize) {
 				// incoming change package is fully consumed, continue with next change package
 				return true;
 			}
 
 			final AbstractOperation localOp = localOperationsIterator.next();
 			final AbstractOperation incomingOp = incomingOps.get(incomingIdx++);
 			if (incomingOp.getIdentifier().equals(localOp.getIdentifier())) {
 				localOperationsIterator.remove();
 				operationMatchingStarted = true;
 			} else {
 				if (operationMatchingStarted) {
					ModelUtil.logError("Incoming operations only partly match with local.");
 					throw new IllegalStateException("Incoming operations only partly match with local.");
 				}
 				// first operation of incoming change package does not match
 				return false;
 			}
 		}
 
 		// all incoming and local changes have been consumed
 		if (incomingIdx == incomingOpsSize) {
 			return true;
 		}
		ModelUtil.logError("Incoming operations are not fully consumed.");
 		throw new IllegalStateException("Incoming operations are not fully consumed.");
 	}
 
 }
