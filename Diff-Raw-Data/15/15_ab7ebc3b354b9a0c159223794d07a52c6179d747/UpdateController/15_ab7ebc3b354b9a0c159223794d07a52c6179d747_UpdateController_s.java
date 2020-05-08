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
 import java.util.ArrayList;
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
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
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
		ChangePackage localChanges = getProjectSpace().getLocalChangePackage();
 
		final List<AbstractOperation> duplicateOperations = calcDuplicateOperations(incomingChanges, localChanges);
 
 		if (duplicateOperations.size() > 0) {
 			// TODO: refactor
			removeFromChangePackage(Collections.singletonList(localChanges), duplicateOperations);
 			final int baseVersionDelta = removeFromChangePackage(incomingChanges, duplicateOperations);
 			final PrimaryVersionSpec newBaseVersion = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 			newBaseVersion.setIdentifier(getProjectSpace().getBaseVersion().getIdentifier() + baseVersionDelta);
 			getProjectSpace().setBaseVersion(newBaseVersion);
 			save(getProjectSpace(), "Could not save project space");
			save(localChanges, "Could not save local changes");
 		}
 
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
 		if (getProgressMonitor().isCanceled() || hasNoOperations(incomingChanges)
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
 
 	private boolean hasNoOperations(List<ChangePackage> changePackages) {
 		for (final ChangePackage changePackage : changePackages) {
 			if (!hasNoOperations(changePackage)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private boolean hasNoOperations(ChangePackage changePackage) {
 		return changePackage.getOperations().size() == 0;
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
 
 	private int removeFromChangePackage(final List<ChangePackage> changes,
 		final List<AbstractOperation> duplicateOperations) {
 		List<AbstractOperation> duplicateOps = new ArrayList<AbstractOperation>(duplicateOperations);
 		int idx = 0;
 		for (final ChangePackage changePackage : changes) {
 			idx++;
 			final List<AbstractOperation> remainingOps = removeFromChangePackage(changePackage, duplicateOps);
 			if (remainingOps.size() == 0) {
 				return idx;
 			}
 			duplicateOps = remainingOps;
 		}
 
 		return idx;
 	}
 
 	private List<AbstractOperation> removeFromChangePackage(ChangePackage changePackage,
 		List<AbstractOperation> duplicateOperations) {
 		final List<AbstractOperation> remainingOperations = new ArrayList<AbstractOperation>(duplicateOperations);
 		final Iterator<AbstractOperation> iterator = changePackage.getOperations().iterator();
 		int duplicateIdx = 0;
 		final int duplicateOpsSize = duplicateOperations.size();
 		while (iterator.hasNext()) {
 			final AbstractOperation incomingOp = iterator.next();
 			if (duplicateIdx == duplicateOpsSize) {
 				// the list should always be empty here
 				return remainingOperations;
 			}
 			final AbstractOperation duplicateOp = duplicateOperations.get(duplicateIdx++);
 			if (duplicateOp.getIdentifier().equals(incomingOp.getIdentifier())) {
 				remainingOperations.remove(duplicateOp);
 				iterator.remove();
 			} else {
 				// the list should always be empty here
 				return remainingOperations;
 			}
 		}
 
 		// duplicates were bigger than the number of ops in change package
 		return remainingOperations;
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
 
 	private List<AbstractOperation> calcDuplicateOperations(List<ChangePackage> incomingChanges,
 		ChangePackage localChanges) {
 		final List<AbstractOperation> redundant = new ArrayList<AbstractOperation>();
 		for (final ChangePackage changePackage : incomingChanges) {
 			final List<AbstractOperation> duplicateOperations = calcDuplicateOperations(changePackage, localChanges);
 			if (duplicateOperations.size() == 0) {
 				return redundant;
 			}
 			redundant.addAll(duplicateOperations);
 		}
 		return redundant;
 	}
 
 	private List<AbstractOperation> calcDuplicateOperations(ChangePackage incomingChanges, ChangePackage localChanges) {
 
 		final List<AbstractOperation> redundant = new ArrayList<AbstractOperation>();
 		final List<AbstractOperation> localOperations = localChanges.getOperations();
 		final List<AbstractOperation> incomingOps = incomingChanges.getOperations();
 		final int incomingOpsSize = incomingOps.size();
 		int incomingIdx = 0;
 
 		for (final AbstractOperation localOp : localOperations) {
 			if (incomingIdx == incomingOpsSize) {
 				return redundant;
 			}
 			final AbstractOperation incomingOp = incomingOps.get(incomingIdx++);
 			if (incomingOp.getIdentifier().equals(localOp.getIdentifier())) {
 				redundant.add(localOp);
 			} else {
 				return redundant;
 			}
 		}
 
 		return redundant;
 	}
 
 }
