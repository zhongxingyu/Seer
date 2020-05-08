 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging;
 
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isAttribute;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isComposite;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isCompositeMultiRef;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isCompositeSingleRef;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isCompositeWithMain;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isDelete;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isMultiAtt;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isMultiAttMove;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isMultiAttSet;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isMultiRef;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isMultiRefSet;
 import static org.eclipse.emf.emfstore.internal.server.model.versioning.operations.util.OperationUtil.isSingleRef;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.VisualConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.AttributeConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.CompositeConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.DeletionConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiAttributeConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiAttributeMoveConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiAttributeMoveSetConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiAttributeSetConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiAttributeSetSetConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiReferenceConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiReferenceSetConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiReferenceSetSetConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiReferenceSetSingleConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.MultiReferenceSingleConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.ReferenceConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts.SingleReferenceConflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.util.DecisionUtil;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.common.ExtensionRegistry;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementIdToEObjectMapping;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ChangeConflictSet;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictBucket;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictDetector;
import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.impl.ChangePackageImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.MultiAttributeOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.MultiReferenceOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.SingleReferenceOperation;
 
 /**
  * DecisionManager is the controller for the merge dialog and therefore it's
  * main component. It calculates the conflicts from incoming changes and can
  * execute resolved conflicts.
  * 
  * @author wesendon
  */
 public class DecisionManager {
 
 	private final ConflictHandler conflictHandler;
 
 	private ArrayList<VisualConflict> conflicts;
 
 	private final ConflictDetector conflictDetector;
 	private final ChangeConflictSet changeConflictSet;
 	private final ModelElementIdToEObjectMapping mapping;
 	private final boolean isBranchMerge;
 	private final Project project;
 
 	private Set<ConflictBucket> unvisualizedConflicts;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param project
 	 *            the related project
 	 * @param changeConflict
 	 *            the {@link ChangeConflictSet} containing the changes leading to a potential conflict
 	 * @param isBranchMerge
 	 *            allows to specify whether two branches are merged, opposed to
 	 *            changes from the same branch. Has an effect on the wording of
 	 *            conflicts
 	 */
 	public DecisionManager(Project project, ChangeConflictSet changeConflict, boolean isBranchMerge) {
 		this.project = project;
 		mapping = changeConflict.getIdToEObjectMapping();
 		this.isBranchMerge = isBranchMerge;
 		changeConflictSet = changeConflict;
 		conflictHandler = initConflictHandlers();
 		conflictDetector = new ConflictDetector();
 		init();
 	}
 
 	private ConflictHandler initConflictHandlers() {
 		return ExtensionRegistry.INSTANCE.get(
 			ConflictHandler.EXTENSION_POINT_ID,
 			ConflictHandler.class,
 			new ConflictHandler() {
 				public VisualConflict handle(VisualConflict conflict,
 					ModelElementIdToEObjectMapping idToEObjectMapping) {
 					return conflict;
 				}
 			},
 			true);
 	}
 
 	private void init() {
 
 		conflicts = new ArrayList<VisualConflict>();
 
 		unvisualizedConflicts = new LinkedHashSet<ConflictBucket>();
 
 		Set<ConflictBucket> conflictBuckets;
 
 		conflictBuckets = changeConflictSet.getConflictBuckets();
 
 		createConflicts(conflictBuckets);
 	}
 
 	/**
 	 * BEGIN FACTORY TODO EXTRACT FACTORY CLASS.
 	 */
 
 	// BEGIN COMPLEX CODE
 	private void createConflicts(Set<ConflictBucket> conflictBucket) {
 		// Create Conflicts from ConflictBucket
 		for (final ConflictBucket conf : conflictBucket) {
 
 			final AbstractOperation my = conf.getMyOperation();
 			final AbstractOperation their = conf.getTheirOperation();
 			VisualConflict conflict = null;
 
 			if (isAttribute(my) && isAttribute(their)) {
 				conflict = createAttributeAttributeDecision(conf);
 
 			} else if (isSingleRef(my) && isSingleRef(their)) {
 				conflict = createSingleSingleConflict(conf);
 
 			} else if (isMultiRef(my) && isMultiRef(their)) {
 				conflict = createMultiMultiConflict(conf);
 
 			} else if (isMultiRef(my) && isSingleRef(their) || isMultiRef(their) && isSingleRef(my)) {
 				conflict = createMultiSingle(conf);
 
 				// left: CS, right: CS
 				// left: CS, right: S
 				// left: S, right: CS
 			} else if (isCompositeSingleRef(my) && isSingleRef(their)) {
 				conflict = new ReferenceConflict(
 					(SingleReferenceOperation) CompositeOperation.class.cast(my).getMainOperation(),
 					SingleReferenceOperation.class.cast(their),
 					conf, this);
 			} else if (isCompositeSingleRef(my) && isCompositeSingleRef(their)) {
 				conflict = new ReferenceConflict(
 					(SingleReferenceOperation) CompositeOperation.class.cast(my).getMainOperation(),
 					(SingleReferenceOperation) CompositeOperation.class.cast(their).getMainOperation(),
 					conf, this);
 			} else if (isSingleRef(my) && isCompositeSingleRef(their)) {
 				conflict = new ReferenceConflict(
 					SingleReferenceOperation.class.cast(my),
 					(SingleReferenceOperation) CompositeOperation.class.cast(their).getMainOperation(),
 					conf, this);
 
 				// left: CM, right: CM
 				// left: CM, right: M
 				// left: M, right: CM
 			} else if (isCompositeMultiRef(my) && isCompositeMultiRef(their)) {
 				conflict = new ReferenceConflict(
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(my).getMainOperation()),
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(their).getMainOperation()),
 					conf, this);
 			} else if (isCompositeMultiRef(my) && isMultiRef(their)) {
 				conflict = new ReferenceConflict(
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(my).getMainOperation()),
 					MultiReferenceOperation.class.cast(their),
 					conf, this);
 			} else if (isMultiRef(my) && isCompositeMultiRef(their)) {
 				conflict = new ReferenceConflict(
 					MultiReferenceOperation.class.cast(my),
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(their).getMainOperation()),
 					conf, this);
 
 				// left: CM, right: CS
 				// left: CM, right: S
 				// left: M, right: CS
 			} else if (isCompositeMultiRef(my) && isCompositeSingleRef(their)) {
 				conflict = new ReferenceConflict(
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(my).getMainOperation()),
 					SingleReferenceOperation.class.cast(
 						CompositeOperation.class.cast(their).getMainOperation()),
 					conf, this);
 
 			} else if (isCompositeMultiRef(my) && isSingleRef(their)) {
 				conflict = new ReferenceConflict(
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(my).getMainOperation()),
 					SingleReferenceOperation.class.cast(their),
 					conf, this);
 
 			} else if (isMultiRef(my) && isCompositeSingleRef(their)) {
 				conflict = new ReferenceConflict(
 					MultiReferenceOperation.class.cast(my),
 					SingleReferenceOperation.class.cast(
 						CompositeOperation.class.cast(their).getMainOperation()),
 					conf, this);
 
 				// left: CS, right: CM
 				// left: S, right: CM
 				// left: CS, right: M
 			} else if (isCompositeSingleRef(my) && isCompositeMultiRef(their)) {
 				conflict = new ReferenceConflict(
 					SingleReferenceOperation.class.cast(
 						CompositeOperation.class.cast(my).getMainOperation()),
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(their).getMainOperation()),
 					conf, this);
 			} else if (isSingleRef(my) && isCompositeMultiRef(their)) {
 				conflict = new ReferenceConflict(
 					SingleReferenceOperation.class.cast(my),
 					MultiReferenceOperation.class.cast(
 						CompositeOperation.class.cast(their).getMainOperation()),
 					conf, this);
 			} else if (isCompositeSingleRef(my) && isMultiRef(their)) {
 				conflict = new ReferenceConflict(
 					SingleReferenceOperation.class.cast(
 						CompositeOperation.class.cast(my).getMainOperation()),
 					MultiReferenceOperation.class.cast(their),
 					conf, this);
 
 			} else if (isMultiRef(my) && isMultiRefSet(their) || isMultiRef(their) && isMultiRefSet(my)) {
 				conflict = createMultiRefMultiSet(conf);
 
 			} else if (isMultiRefSet(my) && isMultiRefSet(their)) {
 				conflict = createMultiRefSetSet(conf);
 
 			} else if (isMultiRefSet(my) && isSingleRef(their) || isMultiRefSet(their) && isSingleRef(my)) {
 				conflict = createMultiSetSingle(conf);
 
 			} else if (isMultiAtt(my) && isMultiAtt(their)) {
 				conflict = createMultiAtt(conf);
 
 			} else if (isMultiAtt(my) && isMultiAttSet(their) || isMultiAtt(their) && isMultiAttSet(my)) {
 				conflict = createMultiAttSet(conf);
 
 			} else if (isMultiAtt(my) && isMultiAttMove(their) || isMultiAtt(their) && isMultiAttMove(my)) {
 				conflict = createMultiAttMove(conf);
 
 			} else if (isMultiAttSet(my) && isMultiAttMove(their) || isMultiAttSet(their) && isMultiAttMove(my)) {
 				conflict = createMultiAttMoveSet(conf);
 
 			} else if (isMultiAttSet(my) && isMultiAttSet(their)) {
 				conflict = createMultiAttSetSet(conf);
 
 			} else if (isComposite(my) || isComposite(their)) {
 				conflict = createCompositeConflict(conf);
 
 			} else if (isDelete(my) || isDelete(their)) {
 				conflict = createDeleteOtherConflict(conf);
 
 			} else if (isCompositeWithMain(my) || isCompositeWithMain(their)) {
 				conflict = createCompositeConflict(conf);
 			}
 
 			if (conflict != null) {
 				conflict = notifyConflictHandlers(conflict);
 				addConflict(conflict);
 			} else {
 				unvisualizedConflicts.add(conf);
 				WorkspaceUtil
 					.log(
 						Messages.DecisionManager_No_ConflictRule_Applicable,
 						IStatus.WARNING);
 			}
 		}
 	}
 
 	private VisualConflict notifyConflictHandlers(VisualConflict conflict) {
 		return conflictHandler.handle(conflict, mapping);
 	}
 
 	private void addConflict(VisualConflict conflict) {
 		if (conflict == null) {
 			return;
 		}
 		conflicts.add(conflict);
 	}
 
 	// END COMPLEX CODE
 	private VisualConflict createMultiRefMultiSet(ConflictBucket conf) {
 		if (isMultiRef(conf.getMyOperation())) {
 			return new MultiReferenceSetConflict(conf, this, true);
 		}
 		return new MultiReferenceSetConflict(conf, this, false);
 	}
 
 	private VisualConflict createMultiSetSingle(ConflictBucket conf) {
 		if (isMultiRefSet(conf.getMyOperation())) {
 			return new MultiReferenceSetSingleConflict(conf, this, true);
 		}
 		return new MultiReferenceSetSingleConflict(conf, this, false);
 
 	}
 
 	private VisualConflict createMultiSingle(ConflictBucket conf) {
 		if (isMultiRef(conf.getMyOperation())) {
 			return new MultiReferenceSingleConflict(conf, this, true);
 		}
 		return new MultiReferenceSingleConflict(conf, this, false);
 
 	}
 
 	private VisualConflict createMultiRefSetSet(ConflictBucket conf) {
 		return new MultiReferenceSetSetConflict(conf, this);
 	}
 
 	private VisualConflict createMultiAttSetSet(ConflictBucket conf) {
 		return new MultiAttributeSetSetConflict(conf, this);
 	}
 
 	private VisualConflict createMultiAtt(ConflictBucket conf) {
 		if (((MultiAttributeOperation) conf.getMyOperation()).isAdd()) {
 			return new MultiAttributeConflict(conf, this, true);
 		}
 		return new MultiAttributeConflict(conf, this, false);
 	}
 
 	private VisualConflict createMultiAttSet(ConflictBucket conf) {
 		if (isMultiAtt(conf.getMyOperation())) {
 			return new MultiAttributeSetConflict(conf, this, true);
 		}
 		return new MultiAttributeSetConflict(conf, this, false);
 
 	}
 
 	private VisualConflict createMultiAttMove(ConflictBucket conf) {
 		if (isMultiAtt(conf.getMyOperation())) {
 			return new MultiAttributeMoveConflict(conf, this, true);
 		}
 		return new MultiAttributeMoveConflict(conf, this, false);
 
 	}
 
 	private VisualConflict createMultiAttMoveSet(ConflictBucket conf) {
 		if (isMultiAttSet(conf.getMyOperation())) {
 			return new MultiAttributeMoveSetConflict(conf, this, true);
 		}
 		return new MultiAttributeMoveSetConflict(conf, this, false);
 
 	}
 
 	private VisualConflict createAttributeAttributeDecision(ConflictBucket conf) {
 		return new AttributeConflict(conf, this);
 	}
 
 	private VisualConflict createSingleSingleConflict(ConflictBucket conf) {
 		return new SingleReferenceConflict(conf, this);
 	}
 
 	private VisualConflict createMultiMultiConflict(ConflictBucket conf) {
 		if (((MultiReferenceOperation) conf.getMyOperation()).isAdd()) {
 			return new MultiReferenceConflict(conf, this, true);
 		}
 		return new MultiReferenceConflict(conf, this, false);
 	}
 
 	private VisualConflict createDeleteOtherConflict(ConflictBucket conf) {
 		if (isDelete(conf.getMyOperation())) {
 			return new DeletionConflict(conf, true, this);
 		}
 		return new DeletionConflict(conf, false, this);
 	}
 
 	private VisualConflict createCompositeConflict(ConflictBucket conf) {
 		if (isComposite(conf.getMyOperation())) {
 			return new CompositeConflict(conf, this, true);
 		}
 		return new CompositeConflict(conf, this, false);
 	}
 
 	/**
 	 * FACTORY END
 	 */
 
 	/**
 	 * Returns the conflicts.
 	 * 
 	 * @return list of conflicts.
 	 */
 	public ArrayList<VisualConflict> getConflicts() {
 		return conflicts;
 	}
 
 	/**
 	 * Checks whether all conflicts are resolved.
 	 * 
 	 * @return true if all are resolved
 	 */
 	public boolean isResolved() {
 		boolean isResolved = true;
 		for (final VisualConflict conflict : conflicts) {
 			isResolved = isResolved && conflict.isResolved();
 		}
 		return isResolved;
 	}
 
 	/**
 	 * If all conflicts are resolved this method will generate the resulting
 	 * operations from the conflicts.
 	 */
 	public void calcResult() {
 		if (!isResolved()) {
 			return;
 		}
 
 		for (final VisualConflict conflict : conflicts) {
 			conflict.resolve();
 		}
 		// resolve unvisualized conflicts automatically
 		for (final ConflictBucket conflictBucket : unvisualizedConflicts) {
 			conflictBucket.resolveConflict(new LinkedHashSet<AbstractOperation>(),
 				new LinkedHashSet<AbstractOperation>());
 		}
 	}
 
 	/**
 	 * Returns the conflictdetector.
 	 * 
 	 * @return conflictdetector
 	 */
 	public ConflictDetector getConflictDetector() {
 		return conflictDetector;
 	}
 
 	/**
 	 * Flat whether branches are merged opposed to versions on the same branch.
 	 * 
 	 * @return true, if branches
 	 */
 	public boolean isBranchMerge() {
 		return isBranchMerge;
 	}
 
 	/**
 	 * Get the Name of an model element by modelelement id.
 	 * 
 	 * @param modelElementId
 	 *            id of element
 	 * @return name as string
 	 */
 	public String getModelElementName(ModelElementId modelElementId) {
 		return getModelElementName(getModelElement(modelElementId));
 	}
 
 	/**
 	 * Get the Name of an model element.
 	 * 
 	 * @param modelElement
 	 *            element
 	 * @return name as string
 	 */
 	public String getModelElementName(EObject modelElement) {
 		return DecisionUtil.getModelElementName(modelElement);
 	}
 
 	/**
 	 * Returns the model element. Therefore the project as well as creation and
 	 * deletion operations are searched.
 	 * 
 	 * @param modelElementId
 	 *            the id of an element
 	 * @return the model element with the given ID or <code>null</code> if no such model element has been found
 	 */
 	public EObject getModelElement(ModelElementId modelElementId) {
 		return mapping.get(modelElementId);
 	}
 
 	/**
 	 * Returns the name of the author for a operation in list of their
 	 * operations.
 	 * 
 	 * @param theirOperation
 	 *            operation
	 * @return name as string or empty string
 	 */
 	public String getAuthorForOperation(AbstractOperation theirOperation) {
		// TODO: review
		EObject container = theirOperation;
		while (!(container instanceof ChangePackage)) {
			container = container.eContainer();
		}

		final ChangePackage cp = (ChangePackage) container;

		// ME: I don't think this will be ever the case
		if (cp.getLogMessage() == null || cp.getLogMessage().getAuthor() == null) {
			return StringUtils.EMPTY;
		}

		return cp.getLogMessage().getAuthor();
		// theirOperation.
 		// for (ChangePackage cp : theirChangePackages) {
 		// for (AbstractOperation op : cp.getOperations()) {
 		// List<AbstractOperation> tmpList = new ArrayList<AbstractOperation>();
 		// if (op instanceof CompositeOperation) {
 		// tmpList.add(op);
 		// tmpList.addAll(((CompositeOperation) op).getSubOperations());
 		// } else {
 		// tmpList.add(op);
 		// }
 		// for (AbstractOperation ao : tmpList) {
 		// if (ao.equals(theirOperation)) {
 		// LogMessage log = cp.getLogMessage();
 		// if (log == null) {
 		// return "";
 		// }
 		// return (log.getAuthor() == null) ? "" : log.getAuthor();
 		//
 		// }
 		// }
 		// }
 		// }
 		// MKCD
		// return StringUtils.EMPTY;
 	}
 
 	private Integer myLeafOperationCount;
 
 	/**
 	 * Count my leaf operations.
 	 * 
 	 * @return the number of leaf operations
 	 */
 	public int countMyLeafOperations() {
 		if (myLeafOperationCount == null) {
 			countConflicts();
 		}
 		return myLeafOperationCount;
 	}
 
 	private void countConflicts() {
 		int myCount = 0;
 		int myLeafCount = 0;
 		int theirCount = 0;
 		int theirLeafCount = 0;
 		for (final VisualConflict conflict : conflicts) {
 			myCount += conflict.getLeftOperations().size();
 			myLeafCount += ChangePackageImpl.countLeafOperations(conflict.getMyOperations());
 			theirCount += conflict.getRightOperations().size();
 			theirLeafCount += ChangePackageImpl.countLeafOperations(conflict.getTheirOperations());
 		}
 		myOperationCount = myCount;
 		myLeafOperationCount = myLeafCount;
 		theirOperationCount = theirCount;
 		theirLeafOperationCount = theirLeafCount;
 
 	}
 
 	private Integer theirLeafOperationCount;
 
 	/**
 	 * Count their leaf operations.
 	 * 
 	 * @return the number of leaf operations
 	 */
 	public int countTheirLeafOperations() {
 		if (theirLeafOperationCount == null) {
 			countConflicts();
 		}
 		return theirLeafOperationCount;
 	}
 
 	private Integer myOperationCount;
 
 	/**
 	 * Count my leaf operations.
 	 * 
 	 * @return the number of leaf operations
 	 */
 	public int countMyOperations() {
 		if (myOperationCount == null) {
 			countConflicts();
 		}
 		return myOperationCount;
 	}
 
 	private Integer theirOperationCount;
 
 	/**
 	 * Count their leaf operations.
 	 * 
 	 * @return the number of leaf operations
 	 */
 	public int countTheirOperations() {
 		if (theirOperationCount == null) {
 			countConflicts();
 		}
 		return theirOperationCount;
 	}
 
 	/**
 	 * Returns the mapping that is used to resolve model elements correctly.
 	 * 
 	 * @return the ID to {@link EObject} mapping
 	 */
 	public ModelElementIdToEObjectMapping getIdToEObjectMapping() {
 		return mapping;
 	}
 
 	/**
 	 * Returns the project on which the decision manager is working on.
 	 * 
 	 * @return the project associated with this decision manager
 	 */
 	public Project getProject() {
 		return project;
 	}
 
 }
