 /*******************************************************************************
 * Copyright 2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
 * are made available under the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.conflictDetection;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.server.Activator;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.ContainmentType;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CreateDeleteOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.FeatureOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.ReferenceOperation;
 
 /**
  * Detects conflicts with a given {@link ConflictDetectionStrategy}.
  * 
  * @author koegel
  */
 public class ConflictDetector {
 
 	private static ConflictDetectionStrategy defaultStrategy;
 
 	private ConflictDetectionStrategy conflictDetectionStrategy;
 
 	/**
 	 * Constructor. Uses default conflict detection strategy
 	 */
 	public ConflictDetector() {
 		this(getStrategy());
 	}
 
 	private static ConflictDetectionStrategy getStrategy() {
 		if (defaultStrategy == null) {
 			ConflictDetectionStrategy strategy = new ExtensionPoint(
 				"org.eclipse.emf.emfstore.client.merge.conflictDetectorStrategy").getClass("class",
 				ConflictDetectionStrategy.class);
 			if (strategy != null) {
 				defaultStrategy = strategy;
 			} else {
 				defaultStrategy = new IndexSensitiveConflictDetectionStrategy();
 			}
 		}
 		return defaultStrategy;
 	}
 
 	/**
 	 * Constructor with a given strategy.
 	 * 
 	 * @param conflictDetectionStrategy the detection strategy to use
 	 */
 	public ConflictDetector(ConflictDetectionStrategy conflictDetectionStrategy) {
 		this.conflictDetectionStrategy = conflictDetectionStrategy;
 	}
 
 	/**
 	 * Determines if two changepackages are conflicting.
 	 * 
 	 * @param operation operation
 	 * @param otherOperation otheroperation
 	 * @return true, if conflicting
 	 */
 	public boolean doConflict(AbstractOperation operation, AbstractOperation otherOperation) {
 		return conflictDetectionStrategy.doConflict(operation, otherOperation);
 	}
 
 	/**
 	 * Determine if the set of conflict bucket candidates contains conflicting buckets that can actually result in
 	 * conflicts.
 	 * 
 	 * @param conflictBucketCandidates the set of conflict candidate buckets
 	 * @return true if potentially conflicting buckets are found
 	 */
 	public boolean containsConflictingBuckets(Set<ConflictBucketCandidate> conflictBucketCandidates) {
 		for (ConflictBucketCandidate conflictBucketCandidate : conflictBucketCandidates) {
 			if (conflictBucketCandidate.isConflicting()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Calculate a set of conflict candidate buckets from a list of my and their change packages.
 	 * 
 	 * @param myChangePackages their operations in a list of change packages
 	 * @param theirChangePackages their operations in a list of change packages
 	 * @return a set of buckets with potentially conflicting operations
 	 */
 	public Set<ConflictBucketCandidate> calculateConflictCandidateBuckets(List<ChangePackage> myChangePackages,
 		List<ChangePackage> theirChangePackages) {
 
 		List<AbstractOperation> myOperations = flattenChangepackages(myChangePackages);
 		List<AbstractOperation> theirOperations = flattenChangepackages(theirChangePackages);
 
 		Set<ConflictBucketCandidate> conflictBucketsCandidateSet = new LinkedHashSet<ConflictBucketCandidate>();
 		ModelElementIdFeatureConflictMap conflictMap = new ModelElementIdFeatureConflictMap();
 
 		int counter = 0;
 		for (AbstractOperation myOperation : myOperations) {
 			scanOperationIntoConflictBucketCandidates(conflictBucketsCandidateSet, conflictMap, myOperation, true,
 				counter);
 			counter++;
 		}
 
 		for (AbstractOperation theirOperation : theirOperations) {
 			scanOperationIntoConflictBucketCandidates(conflictBucketsCandidateSet, conflictMap, theirOperation, false,
 				counter);
 			counter++;
 		}
 		return conflictBucketsCandidateSet;
 	}
 
 	private List<AbstractOperation> flattenChangepackages(List<ChangePackage> cps) {
 		List<AbstractOperation> operations = new ArrayList<AbstractOperation>();
 		for (ChangePackage cp : cps) {
 			operations.addAll(cp.getOperations());
 		}
 		return operations;
 	}
 
 	private void scanOperationIntoConflictBucketCandidates(Set<ConflictBucketCandidate> conflictBucketsSet,
 		ModelElementIdFeatureConflictMap conflictMap, AbstractOperation operation, boolean isMyOperation,
 		Integer priority) {
 
 		// extract a modelelementId to feature map from the operation (including any sub operations)
 		ModelElementIdToFeatureSetMapping modelElementIdToFeatureSetMapping = new ModelElementIdToFeatureSetMapping();
 		extractModelElementIdFeatureMappings(operation, modelElementIdToFeatureSetMapping);
 
 		ConflictBucketCandidate currentOperationsConflictBucket = null;
 		for (String modelElementId : modelElementIdToFeatureSetMapping.keySet()) {
 
 			Set<String> featureSet = modelElementIdToFeatureSetMapping.get(modelElementId);
 
 			for (String featureName : featureSet) {
 
 				Set<ConflictBucketCandidate> conflictBuckets = conflictMap.get(modelElementId, featureName);
 				ConflictBucketCandidate conflictBucket;
 				if (conflictBuckets.size() == 0) {
 					conflictBucket = null;
 				} else if (conflictBuckets.size() == 1) {
 					conflictBucket = conflictBuckets.iterator().next();
 				} else {
 					conflictBucket = mergeAllBucketCandidates(conflictBuckets, conflictBucketsSet, conflictMap);
 				}
 
 				if (conflictBucket == null && currentOperationsConflictBucket == null) {
 					// no existing ConflictBucket for id and feature and no bucket for current op => create new
 					// ConflictBucket
 					currentOperationsConflictBucket = new ConflictBucketCandidate();
 					conflictBucketsSet.add(currentOperationsConflictBucket);
 					currentOperationsConflictBucket.addOperation(operation, modelElementId, featureName, isMyOperation,
 						priority);
 					conflictMap.put(modelElementId, featureName, currentOperationsConflictBucket, false);
 				} else if (conflictBucket == currentOperationsConflictBucket) {
 					// conflictMap.put(modelElementId, featureName, currentOperationsConflictBucket, false);
 					currentOperationsConflictBucket.addModelElementId(modelElementId, featureName);
 				} else if (conflictBucket == null && currentOperationsConflictBucket != null) {
 					// no existing ConflictBucket for id but existing ConflictBucket for operation => keep operations
 					// ConflictBucket
 					conflictMap.put(modelElementId, featureName, currentOperationsConflictBucket, false);
 					currentOperationsConflictBucket.addModelElementId(modelElementId, featureName);
 				} else if (conflictBucket != null && currentOperationsConflictBucket == null) {
 					// existing ConflictBucket for id but none for operation => keep id ConflictBucket
 					currentOperationsConflictBucket = conflictBucket;
 					currentOperationsConflictBucket.addOperation(operation, modelElementId, featureName, isMyOperation,
 						priority);
 				} else {
 					// existing ConflictBucket for both id and operation => merge ConflictBuckets
 					currentOperationsConflictBucket = mergeConflictBuckets(conflictBucketsSet, conflictMap,
 						currentOperationsConflictBucket, conflictBucket);
 					currentOperationsConflictBucket.addModelElementId(modelElementId, featureName);
 				}
 			}
 		}
 	}
 
 	private ConflictBucketCandidate mergeAllBucketCandidates(Set<ConflictBucketCandidate> conflictBuckets,
 		Set<ConflictBucketCandidate> conflictBucketsSet, ModelElementIdFeatureConflictMap conflictMap) {
 		Iterator<ConflictBucketCandidate> iterator = conflictBuckets.iterator();
 		ConflictBucketCandidate currentBucket = iterator.next();
 		while (iterator.hasNext()) {
 			currentBucket = mergeConflictBuckets(conflictBucketsSet, conflictMap, currentBucket, iterator.next());
 		}
 		return currentBucket;
 	}
 
 	private ConflictBucketCandidate mergeConflictBuckets(Set<ConflictBucketCandidate> conflictBucketsSet,
 		ModelElementIdFeatureConflictMap conflictMap, ConflictBucketCandidate currentOperationsConflictBucket,
 		ConflictBucketCandidate conflictBucket) {
 		ConflictBucketCandidate biggerBucket = currentOperationsConflictBucket;
 		ConflictBucketCandidate smallerBucket = conflictBucket;
 
 		if (conflictBucket.size() > currentOperationsConflictBucket.size()) {
 			biggerBucket = conflictBucket;
 			smallerBucket = currentOperationsConflictBucket;
 		}
 		biggerBucket.addConflictBucketCandidate(smallerBucket);
 
 		ModelElementIdToFeatureSetMapping idToFeatureSetMap = smallerBucket
 			.getInvolvedModelElementIdToFeatureSetMapping();
 		for (String id : idToFeatureSetMap.keySet()) {
 			for (String featureName : idToFeatureSetMap.get(id)) {
 				conflictMap.put(id, featureName, biggerBucket, true);
 			}
 		}
 		conflictBucketsSet.remove(smallerBucket);
 
 		return biggerBucket;
 
 	}
 
 	private void extractModelElementIdFeatureMappings(AbstractOperation operation,
 		ModelElementIdToFeatureSetMapping modelElementIdToFeatureSetMap) {
 
 		if (operation instanceof CompositeOperation) {
 			CompositeOperation compositeOperation = (CompositeOperation) operation;
 			for (AbstractOperation subOperation : compositeOperation.getSubOperations()) {
 				extractModelElementIdFeatureMappings(subOperation, modelElementIdToFeatureSetMap);
 			}
 			return;
 		} else if (operation instanceof CreateDeleteOperation) {
 			// handle containment tree
 			CreateDeleteOperation createDeleteOperation = (CreateDeleteOperation) operation;
 			for (ModelElementId createDeletedElement : createDeleteOperation.getEObjectToIdMap().values()) {
 				modelElementIdToFeatureSetMap.add(createDeletedElement.getId());
 			}
 			// handle suboperations
 			for (AbstractOperation subOperation : createDeleteOperation.getSubOperations()) {
 				extractModelElementIdFeatureMappings(subOperation, modelElementIdToFeatureSetMap);
 			}
 			return;
 		} else if (operation instanceof FeatureOperation) {
 			handleFeatureOperation(operation, modelElementIdToFeatureSetMap);
 			return;
 		}
 		throw new IllegalStateException("Unkown operation type: " + operation.getClass().getCanonicalName());
 	}
 
 	private void handleFeatureOperation(AbstractOperation operation,
 		ModelElementIdToFeatureSetMapping modelElementIdToFeatureSetMap) {
 		FeatureOperation featureOperation = (FeatureOperation) operation;
 		String modelElementId = featureOperation.getModelElementId().getId();
 		String featureName = featureOperation.getFeatureName();
 		if (featureOperation instanceof ReferenceOperation) {
 			ReferenceOperation referenceOperation = (ReferenceOperation) featureOperation;
 			// if (isRemovingReferencesOnly(referenceOperation)) {
 			// return;
 			// }
 			for (ModelElementId otherModelElement : referenceOperation.getOtherInvolvedModelElements()) {
 				if (referenceOperation.getContainmentType().equals(ContainmentType.CONTAINMENT)
 					&& !referenceOperation.isBidirectional()) {
 					modelElementIdToFeatureSetMap.addForContainerFeature(otherModelElement.getId());
 				} else {
 					modelElementIdToFeatureSetMap.addRequired(otherModelElement.getId());
 				}
 			}
 		}
 		if (isExcludedFeature(featureName)) {
 			return;
 		}
 		modelElementIdToFeatureSetMap.add(modelElementId, featureName);
 		return;
 	}
 
 	// private boolean isRemovingReferencesOnly(ReferenceOperation referenceOperation) {
 	// if (referenceOperation instanceof MultiReferenceOperation) {
 	// return !((MultiReferenceOperation) referenceOperation).isAdd();
 	// }
 	// if (referenceOperation instanceof SingleReferenceOperation) {
 	// return ((SingleReferenceOperation) referenceOperation).getNewValue() == null;
 	// }
 	//
 	// return false;
 	// }
 
 	private static final Set<String> excludedFeatureNameSet = initExcludedFeatureNameSet();
 
 	private boolean isExcludedFeature(String featureName) {
 		return excludedFeatureNameSet.contains(featureName);
 	}
 
 	private static Set<String> initExcludedFeatureNameSet() {
 
 		Set<String> excludedFeatures = new LinkedHashSet<String>();
 		ExtensionPoint extensionPoint = new ExtensionPoint(Activator.PLUGIN_ID + ".conflictdetection.excludedfeatures");
 
 		IExcludedFeaturesProvider excludingFeaturesProvider = extensionPoint.getClass("class",
 			IExcludedFeaturesProvider.class);
 
 		if (excludingFeaturesProvider != null) {
 			excludedFeatures.addAll(excludingFeaturesProvider.getExcludedFeatures());
 		}
 
 		return excludedFeatures;
 	}
 
 	/**
 	 * Calculate a set of conflict buckets from an existing set of conflict candidate buckets. the resulting set may be
 	 * empty.
 	 * 
 	 * @param conflictBucketsCandidateSet a set of conflict candidate buckets
 	 * @param notInvolvedInConflict all my operations that are not involved in a conflict are collected in this
 	 *            transient parameter set
 	 * @return a set of conflict buckets
 	 */
 	public Set<ConflictBucket> calculateConflictBucketsFromConflictCandidateBuckets(
 		Set<ConflictBucketCandidate> conflictBucketsCandidateSet, Set<AbstractOperation> notInvolvedInConflict) {
 		Set<ConflictBucket> conflictBucketsSet = new LinkedHashSet<ConflictBucket>();
 		for (ConflictBucketCandidate conflictBucketCandidate : conflictBucketsCandidateSet) {
 			conflictBucketsSet.addAll(conflictBucketCandidate.calculateConflictBuckets(this, notInvolvedInConflict));
 		}
 		return conflictBucketsSet;
 	}
 
 	/**
 	 * Retrieve all operations in other ops that are conflicting with operations in ops. If any operation is in both
 	 * lists, it is not considered to be conflicting.
 	 * 
 	 * @param ops A list of operations.
 	 * @param otherOps A list of the other operations.
 	 * @return A set of conflicting operations which is a subset of otherOps.
 	 */
 	public Set<AbstractOperation> getConflicting(List<AbstractOperation> ops, List<AbstractOperation> otherOps) {
 		// the operations that are conflicting
 		Set<AbstractOperation> conflicting = new HashSet<AbstractOperation>();
 
 		// check each operation in ops against otherOps
 		for (AbstractOperation position : ops) {
 			for (AbstractOperation other : otherOps) {
 				if (conflicting.contains(other)) {
 					// a conflict has already been registered
 					continue;
 				}
 				// if there is a conflict, add the other op to the
 				// list of conflicting ops along with all ops that
 				// require other ops
 				if (conflictDetectionStrategy.doConflict(position, other)) {
 					conflicting.addAll(getRequiring(otherOps, other));
 					conflicting.add(other);
 				}
 			}
 
 		}
 
 		// return the set of conflicting operations in other ops
 		return conflicting;
 	}
 
 	/**
 	 * Retrieve all operations in other ops that are conflicting on "index integrity only" with operations in ops. If
 	 * any operation is in both lists, it is not considered to be conflicting.
 	 * 
 	 * @param ops A list of operations.
 	 * @param otherOps A list of the other operations.
 	 * @return A set of conflicting operations which is a subset of otherOps.
 	 */
 	public Set<AbstractOperation> getConflictingIndexIntegrity(List<AbstractOperation> ops,
 		List<AbstractOperation> otherOps) {
 		// the operations that are conflicting
 		Set<AbstractOperation> conflicting = new HashSet<AbstractOperation>();
 
 		// works with only one strategy, as of now, hardcoding it
 		IndexSensitiveConflictDetectionStrategy indexSensitiveStrategy = new IndexSensitiveConflictDetectionStrategy();
 
 		// check each operation in ops against otherOps
 		for (AbstractOperation position : ops) {
 			for (AbstractOperation other : otherOps) {
 				if (conflicting.contains(other)) {
 					// a conflict has already been registered
 					continue;
 				}
 				// if there is a conflict, add the other op to the
 				// list of conflicting ops along with all ops that
 				// require other ops
 
 				if (indexSensitiveStrategy.doConflictIndexIntegrity(position, other)) {
 					conflicting.addAll(getRequiring(otherOps, other));
 					conflicting.add(other);
 				}
 			}
 
 		}
 
 		// return the set of conflicting operations in other ops
 		return conflicting;
 	}
 
 	/**
 	 * Retrieve all operations in ops that are required by op. The operation <code>op</code> must be part of
 	 * <code>ops</code>.
 	 * 
 	 * @param ops A time ordered (ascending) list of operations containing op.
 	 * @param op The operation for which the requirements should be determined.
 	 * @return A list of operations that are required for op which is a subset of ops. <code>op</code> will not be part
 	 *         of the returned list.
 	 * @throws IllegalArgumentException If op is not in ops.
 	 */
 	public List<AbstractOperation> getRequired(List<AbstractOperation> ops, AbstractOperation op)
 		throws IllegalArgumentException {
 		// sanity check
 		if (!ops.contains(op)) {
 			throw new IllegalArgumentException("the ops list dos not contain op");
 		}
 
 		// Check all operations if they are (transitively) required by op.
 		// We make use of the fact here that an operation can only require
 		// operations that are before it in time.
 		List<AbstractOperation> required = new ArrayList<AbstractOperation>();
 		required.add(op);
 
 		for (int i = ops.indexOf(op) - 1; i >= 0; i--) {
 			AbstractOperation current = ops.get(i);
 			// else check if it is required by any of the already required ops
 
 			for (AbstractOperation req : required) {
 				if (conflictDetectionStrategy.isRequired(current, req)) {
 					required.add(0, current);
 					break;
 				}
 			}
 		}
 		required.remove(op);
 
 		// return the sub list of operations required by op
 		return required;
 	}
 
 	/**
 	 * Retrieve all operations in ops that require op.
 	 * 
 	 * @param ops A time ordered (ascending) list of operations containing op.
 	 * @param op The operation for which the dependants should be determined.
 	 * @return A list of operations that require op which is a subset of ops.
 	 */
 	public List<AbstractOperation> getRequiring(List<AbstractOperation> ops, AbstractOperation op) {
 		// sanity check
 		if (!ops.contains(op)) {
 			throw new IllegalArgumentException("the ops list dos not contain op");
 		}
 
 		// Check all operations if they (transitively) require op.
 		// We make use of the fact here that an operation can only
 		// possibly require an operation that is before it in time.
 		int opIdx = ops.indexOf(op);
 		List<AbstractOperation> requiring = new ArrayList<AbstractOperation>();
 		requiring.add(op);
 
 		for (AbstractOperation current : ops) {
 			// if the current op is before op, it can not require it
 			if (ops.indexOf(current) <= opIdx) {
 				continue;
 			}
 			// else check if it requires any of the already requiring ops
 			for (AbstractOperation req : requiring) {
 				if (conflictDetectionStrategy.isRequired(req, current)) {
 					requiring.add(current);
 					break;
 				}
 			}
 		}
 		requiring.remove(op);
 		// return the sub list of operations requiring op
 		return requiring;
 	}
 
 }
