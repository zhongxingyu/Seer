 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.diff.engine;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.diff.EMFCompareDiffMessages;
 import org.eclipse.emf.compare.diff.engine.check.AttributesCheck;
 import org.eclipse.emf.compare.diff.engine.check.ReferencesCheck;
 import org.eclipse.emf.compare.diff.metamodel.ConflictingDiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.MoveModelElement;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.UpdateContainmentFeature;
 import org.eclipse.emf.compare.match.metamodel.Match2Elements;
 import org.eclipse.emf.compare.match.metamodel.Match3Elements;
 import org.eclipse.emf.compare.match.metamodel.MatchElement;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.metamodel.MatchPackage;
 import org.eclipse.emf.compare.match.metamodel.Side;
 import org.eclipse.emf.compare.match.metamodel.UnmatchElement;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EGenericType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * This class is useful when one wants to determine a diff from a matching model.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 // FIXME this engine must be refactored (e.g create checkers for 'checkxxDiff')
 public class GenericDiffEngine implements IDiffEngine {
 	/** Allows retrieval of the ancestor matched object. */
 	protected static final int ANCESTOR_OBJECT = 0;
 
 	/** Allows retrieval of the left matched object. */
 	protected static final int LEFT_OBJECT = 1;
 
 	/** Allows retrieval of the right matched object. */
 	protected static final int RIGHT_OBJECT = 2;
 
 	/**
 	 * If we're currently doing a resourceSet differencing, this will have been initialized with the whole
 	 * MatchResourceSet.
 	 */
 	protected EcoreUtil.CrossReferencer matchCrossReferencer;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.engine.IDiffEngine#doDiff(org.eclipse.emf.compare.match.metamodel.MatchModel)
 	 */
 	public DiffModel doDiff(MatchModel match) {
 		return doDiff(match, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.engine.IDiffEngine#doDiff(org.eclipse.emf.compare.match.metamodel.MatchModel,
 	 *      boolean)
 	 */
 	public DiffModel doDiff(MatchModel match, boolean threeWay) {
 		matchCrossReferencer = new EcoreUtil.CrossReferencer(match) {
 			private static final long serialVersionUID = 1L;
 
 			/** initializer. */
 			{
 				crossReference();
 			}
 		};
 		final DiffModel result = DiffFactory.eINSTANCE.createDiffModel();
 		result.getLeftRoots().addAll(match.getLeftRoots());
 		result.getRightRoots().addAll(match.getRightRoots());
 		result.getAncestorRoots().addAll(match.getAncestorRoots());
 		DiffGroup diffRoot = null;
 
 		if (threeWay) {
 			diffRoot = doDiffThreeWay(match);
 		} else {
 			diffRoot = doDiffTwoWay(match);
 		}
 		result.getOwnedElements().add(diffRoot);
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.engine.IDiffEngine#doDiffResourceSet(org.eclipse.emf.compare.match.metamodel.MatchModel,
 	 *      boolean, org.eclipse.emf.ecore.util.EcoreUtil.CrossReferencer)
 	 */
 	public DiffModel doDiffResourceSet(MatchModel match, boolean threeWay,
 			EcoreUtil.CrossReferencer crossReferencer) {
 		matchCrossReferencer = crossReferencer;
 		final DiffModel result = DiffFactory.eINSTANCE.createDiffModel();
 		result.getLeftRoots().addAll(match.getLeftRoots());
 		result.getRightRoots().addAll(match.getRightRoots());
 		result.getAncestorRoots().addAll(match.getAncestorRoots());
 		DiffGroup diffRoot = null;
 
 		if (threeWay) {
 			diffRoot = doDiffThreeWay(match);
 		} else {
 			diffRoot = doDiffTwoWay(match);
 		}
 		result.getOwnedElements().add(diffRoot);
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.diff.engine.IDiffEngine#reset()
 	 */
 	public void reset() {
 		matchCrossReferencer = null;
 	}
 
 	/**
 	 * Looks for an already created {@link DiffGroup diff group} in order to add the operation, if none
 	 * exists, create one where the operation belongs to.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffModel}.
 	 * @param operation
 	 *            Operation to add to the {@link DiffModel}.
 	 * @param targetParent
 	 *            Parent {@link EObject} for the operation.
 	 */
 	protected void addInContainerPackage(DiffGroup root, DiffElement operation, EObject targetParent) {
 		if (targetParent == null) {
 			root.getSubDiffElements().add(operation);
 			return;
 		}
 		DiffGroup targetGroup = findExistingGroup(root, targetParent);
 		if (targetGroup == null) {
 			// Searches for a DiffGroup with the matched parent
 			targetGroup = findExistingGroup(root, getMatchedEObject(targetParent));
 			if (targetGroup == null) {
 				// we have to create the group
 				targetGroup = buildHierarchyGroup(targetParent, root);
 			}
 		}
 		targetGroup.getSubDiffElements().add(operation);
 	}
 
 	/**
 	 * Returns the implementation of a {@link org.eclipse.emf.compare.diff.engine.check.AbstractCheck}
 	 * responsible for the verification of updates on attribute values.
 	 * 
 	 * @return The implementation of a {@link org.eclipse.emf.compare.diff.engine.check.AbstractCheck}
 	 *         responsible for the verification of updates on attribute values.
 	 * @since 1.0
 	 */
 	protected AttributesCheck getAttributesChecker() {
 		return new AttributesCheck(matchCrossReferencer);
 	}
 
 	/**
 	 * Returns the implementation of a {@link org.eclipse.emf.compare.diff.engine.check.AbstractCheck}
 	 * responsible for the verification of updates on reference values.
 	 * 
 	 * @return The implementation of a {@link org.eclipse.emf.compare.diff.engine.check.AbstractCheck}
 	 *         responsible for the verification of updates on reference values.
 	 * @since 1.0
 	 */
 	protected ReferencesCheck getReferencesChecker() {
 		return new ReferencesCheck(matchCrossReferencer);
 	}
 
 	/**
 	 * This will iterate through all the attributes of the <code>mapping</code>'s two elements to check if any
 	 * of them has been modified.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if one of the attributes has
 	 *            actually been changed.
 	 * @param mapping
 	 *            This contains the mapping information about the elements we need to check.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails.
 	 * @deprecated Override {@link AttributesCheck#checkAttributesUpdates(DiffGroup, Match2Elements)} and
 	 *             return your overriden implementation through {@link #getAttributesChecker()}.
 	 */
 	@Deprecated
 	protected void checkAttributesUpdates(DiffGroup root, Match2Elements mapping) throws FactoryException {
 		getAttributesChecker().checkAttributesUpdates(root, mapping);
 	}
 
 	/**
 	 * This will iterate through all the attributes of the <code>mapping</code>'s three elements to check if
 	 * any of them has been modified.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if one of the attribute has
 	 *            actually been changed.
 	 * @param mapping
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails.
 	 * @deprecated Override {@link AttributesCheck#checkAttributesUpdates(DiffGroup, Match3Elements)} and
 	 *             return your overriden implementation through {@link #getAttributesChecker()}.
 	 */
 	@Deprecated
 	protected void checkAttributesUpdates(DiffGroup root, Match3Elements mapping) throws FactoryException {
 		getAttributesChecker().checkAttributesUpdates(root, mapping);
 	}
 
 	/**
 	 * This will check whether the left and right element are contained in the same containment reference and
 	 * create a difference if need be.
 	 * 
 	 * @param current
 	 *            {@link DiffGroup} under which the new differences will be added.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a containment
 	 *            reference update.
 	 */
 	protected void checkContainmentUpdate(DiffGroup current, Match2Elements matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		if (leftElement.eContainmentFeature() != null && rightElement.eContainmentFeature() != null) {
 			if (!leftElement.eContainmentFeature().getName().equals(
					rightElement.eContainmentFeature().getName())
					&& getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())) {
 				createUpdateContainmentOperation(current, leftElement, rightElement);
 			}
 		}
 	}
 
 	/**
 	 * This will check whether the left and right element are contained in the same containment reference and
 	 * create a difference if need be.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup} under which the new differences will be added.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a containment
 	 *            reference update.
 	 */
 	protected void checkContainmentUpdate(DiffGroup root, Match3Elements matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		final EObject originElement = matchElement.getOriginElement();
 		if (originElement == null || leftElement.eContainer() == null && rightElement.eContainer() == null
 				&& originElement.eContainer() == null)
 			return;
 
 		final boolean leftChangedContainment = originElement.eContainmentFeature() != null
 				&& leftElement.eContainmentFeature() != null
 				&& !leftElement.eContainmentFeature().getName().equals(
 						originElement.eContainmentFeature().getName())
 				&& getMatchedEObject(leftElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 		final boolean rightChangedContainment = originElement.eContainmentFeature() != null
 				&& rightElement.eContainmentFeature() != null
 				&& !rightElement.eContainmentFeature().getName().equals(
 						originElement.eContainmentFeature().getName())
 				&& getMatchedEObject(rightElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 
 		// effective change
 		if (getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())
 				&& !leftElement.eContainmentFeature().getName().equals(
 						rightElement.eContainmentFeature().getName())) {
 			// conflicting change
 			if (leftChangedContainment && rightChangedContainment) {
 				final UpdateContainmentFeature updateContainment = DiffFactory.eINSTANCE
 						.createUpdateContainmentFeature();
 				updateContainment.setLeftElement(leftElement);
 				updateContainment.setRightElement(rightElement);
 				updateContainment.setRightTarget(getMatchedEObject(leftElement.eContainer()));
 				updateContainment.setLeftTarget(getMatchedEObject(rightElement.eContainer()));
 
 				final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 						.createConflictingDiffElement();
 				conflictingDiff.setLeftParent(leftElement);
 				conflictingDiff.setRightParent(rightElement);
 				conflictingDiff.setOriginElement(originElement);
 				conflictingDiff.getSubDiffElements().add(updateContainment);
 				root.getSubDiffElements().add(conflictingDiff);
 			} else if (leftChangedContainment) {
 				createUpdateContainmentOperation(root, leftElement, rightElement);
 			} else if (rightChangedContainment) {
 				createRemoteUpdateContainmentOperation(root, leftElement, rightElement);
 			}
 		}
 	}
 
 	/**
 	 * This will call all the different checks we need to call for when computing the diff. Clients can
 	 * override this to alter the checks or add others.
 	 * 
 	 * @param current
 	 *            current {@link DiffGroup} under which the new differences will be added.
 	 * @param match
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails somehow.
 	 */
 	protected void checkForDiffs(DiffGroup current, Match2Elements match) throws FactoryException {
 		getAttributesChecker().checkAttributesUpdates(current, match);
 		getReferencesChecker().checkReferencesUpdates(current, match);
 		checkMoves(current, match);
 		checkContainmentUpdate(current, match);
 	}
 
 	/**
 	 * This will call all the different checks we need to call for when computing the diff. Clients can
 	 * override this to alter the checks or add others.
 	 * 
 	 * @param current
 	 *            current {@link DiffGroup} under which the new differences will be added.
 	 * @param match
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 * @throws FactoryException
 	 *             Thrown if one of the checks fails somehow.
 	 */
 	protected void checkForDiffs(DiffGroup current, Match3Elements match) throws FactoryException {
 		getAttributesChecker().checkAttributesUpdates(current, match);
 		getReferencesChecker().checkReferencesUpdates(current, match);
 		checkMoves(current, match);
 		checkContainmentUpdate(current, match);
 	}
 
 	/**
 	 * This will check if the elements matched by a given {@link Match2Elements} have been moved.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if the elements have actually
 	 *            been moved.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 */
 	protected void checkMoves(DiffGroup root, Match2Elements matchElement) {
 		final EObject left = matchElement.getLeftElement();
 		final EObject right = matchElement.getRightElement();
 
 		if (left instanceof EGenericType || right instanceof EGenericType)
 			return;
 		if (left.eContainer() != null && right.eContainer() != null
 				&& getMatchedEObject(left.eContainer()) != right.eContainer()) {
 			createMoveOperation(root, left, right);
 		}
 	}
 
 	/**
 	 * This will check if the elements matched by a given {@link Match3Element} have been moved since the
 	 * models common ancestor.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create if the elements have actually
 	 *            been moved.
 	 * @param matchElement
 	 *            This contains the mapping information about the elements we need to check for a move.
 	 */
 	protected void checkMoves(DiffGroup root, Match3Elements matchElement) {
 		final EObject leftElement = matchElement.getLeftElement();
 		final EObject rightElement = matchElement.getRightElement();
 		final EObject originElement = matchElement.getOriginElement();
 		if (leftElement.eContainer() == null && rightElement.eContainer() == null
 				&& originElement.eContainer() == null)
 			return;
 		if (leftElement instanceof EGenericType || rightElement instanceof EGenericType
 				|| originElement instanceof EGenericType)
 			return;
 
 		final boolean leftMoved = originElement != null
 				&& leftElement.eContainer() != null
 				&& !getMatchedEObject(leftElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 		final boolean rightMoved = originElement != null
 				&& rightElement.eContainer() != null
 				&& !getMatchedEObject(rightElement.eContainer(), ANCESTOR_OBJECT).equals(
 						originElement.eContainer());
 
 		// effective change
 		if (!getMatchedEObject(leftElement.eContainer()).equals(rightElement.eContainer())) {
 			// conflicting change
 			if (leftMoved && rightMoved) {
 				final MoveModelElement operation = DiffFactory.eINSTANCE.createMoveModelElement();
 				operation.setRightElement(rightElement);
 				operation.setLeftElement(leftElement);
 				operation.setRightTarget(getMatchedEObject(leftElement.eContainer()));
 				operation.setLeftTarget(getMatchedEObject(rightElement.eContainer()));
 
 				final ConflictingDiffElement conflictingDiff = DiffFactory.eINSTANCE
 						.createConflictingDiffElement();
 				conflictingDiff.setLeftParent(leftElement);
 				conflictingDiff.setRightParent(rightElement);
 				conflictingDiff.setOriginElement(originElement);
 				conflictingDiff.getSubDiffElements().add(operation);
 				root.getSubDiffElements().add(conflictingDiff);
 				// non conflicting change
 			} else if (rightMoved) {
 				createRemoteMoveOperation(root, leftElement, rightElement);
 			} else if (leftMoved) {
 				createMoveOperation(root, leftElement, rightElement);
 			}
 		}
 	}
 
 	/**
 	 * This will be called to check for changes on a given reference values. Note that we know
 	 * <code>reference.isMany()</code> and <code>reference.isOrdered()</code> always return true here for the
 	 * generic diff engine and the tests won't be made.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffElement}s to create.
 	 * @param reference
 	 *            {@link EReference} to check for modifications.
 	 * @param leftElement
 	 *            Element corresponding to the final value for the given reference.
 	 * @param rightElement
 	 *            Element corresponding to the initial value for the given reference.
 	 * @param addedReferences
 	 *            Contains the created differences for added reference values.
 	 * @param removedReferences
 	 *            Contains the created differences for removed reference values.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the references' values.
 	 * @deprecated Replaced by {@link ReferencesCheck}.
 	 */
 	@Deprecated
 	@SuppressWarnings("unused")
 	protected void checkReferenceOrderChange(DiffGroup root, EReference reference, EObject leftElement,
 			EObject rightElement, List<ReferenceChangeLeftTarget> addedReferences,
 			List<ReferenceChangeRightTarget> removedReferences) throws FactoryException {
 		// Deprecated, see ReferencesCheck#checkReferenceOrderChange
 	}
 
 	/**
 	 * Checks if there's been references updates in the model.<br/>
 	 * <p>
 	 * A reference is considered updated if its value(s) has been changed (either removal or addition of an
 	 * element if the reference is multi-valued or update of a single-valued reference) between the left and
 	 * the right model.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param mapping
 	 *            Contains informations about the left and right model elements we have to compare.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the references' values.
 	 * @deprecated Override {@link ReferencesCheck#checkReferencesUpdates(DiffGroup, Match2Elements)} and
 	 *             return your overriden implementation through {@link #getReferencesChecker()}.
 	 */
 	@Deprecated
 	protected void checkReferencesUpdates(DiffGroup root, Match2Elements mapping) throws FactoryException {
 		getReferencesChecker().checkReferencesUpdates(root, mapping);
 	}
 
 	/**
 	 * Checks if there's been references updates in the model.<br/>
 	 * <p>
 	 * A reference is considered updated if its value(s) has been changed (either removal or addition of an
 	 * element if the reference is multi-valued or update of a single-valued reference) between the left and
 	 * the ancestor model, the right and the ancestor or between the left and the right model.
 	 * </p>
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param mapping
 	 *            Contains informations about the left, right and origin model elements we have to compare.
 	 * @throws FactoryException
 	 *             Thrown if we cannot fetch the references' values.
 	 * @deprecated Override {@link ReferencesCheck#checkReferencesUpdates(DiffGroup, Match3Elements)} and
 	 *             return your overriden implementation through {@link #getReferencesChecker()}.
 	 */
 	@Deprecated
 	protected void checkReferencesUpdates(DiffGroup root, Match3Elements mapping) throws FactoryException {
 		getReferencesChecker().checkReferencesUpdates(root, mapping);
 	}
 
 	/**
 	 * The diff computing for three way comparisons is handled here. We'll compute the diff model from the
 	 * given match model.
 	 * 
 	 * @param match
 	 *            {@link MatchModel match model} we'll be using to compute the differences.
 	 * @return {@link DiffGroup root} of the {@link DiffModel} computed from the given {@link MatchModel}.
 	 */
 	protected DiffGroup doDiffThreeWay(MatchModel match) {
 		final DiffGroup diffRoot = DiffFactory.eINSTANCE.createDiffGroup();
 
 		// It is a possibility that no elements were matched
 		if (match.getMatchedElements().size() > 0) {
 			// we have to browse the model and create the corresponding operations
 			final Match3Elements matchRoot = (Match3Elements)match.getMatchedElements().get(0);
 			doDiffDelegate(diffRoot, matchRoot);
 		}
 
 		final List<UnmatchElement> filteredUnmatched = new ArrayList<UnmatchElement>(match
 				.getUnmatchedElements().size());
 		final Iterator<UnmatchElement> unmatched = match.getUnmatchedElements().iterator();
 		while (unmatched.hasNext()) {
 			final UnmatchElement unmatchElement = unmatched.next();
 			// We'll consider both conflicting elements and root of an unmatched hierarchy
 			if (unmatchElement.isConflicting()) {
 				filteredUnmatched.add(unmatchElement);
 				continue;
 			}
 
 			boolean isChild = false;
 			for (final UnmatchElement elem : match.getUnmatchedElements()) {
 				if (elem != unmatchElement
 						&& EcoreUtil.isAncestor(elem.getElement(), unmatchElement.getElement())) {
 					isChild = true;
 					break;
 				}
 			}
 			if (!isChild) {
 				filteredUnmatched.add(unmatchElement);
 			}
 		}
 		if (filteredUnmatched.size() > 0) {
 			processUnmatchedElements(diffRoot, filteredUnmatched);
 		}
 		return diffRoot;
 	}
 
 	/**
 	 * The diff computing for two way comparisons is handled here. We'll compute the diff model from the given
 	 * match model.
 	 * 
 	 * @param match
 	 *            {@link MatchModel match model} we'll be using to compute the differences.
 	 * @return {@link DiffGroup root} of the {@link DiffModel} computed from the given {@link MatchModel}.
 	 */
 	protected DiffGroup doDiffTwoWay(MatchModel match) {
 		final DiffGroup diffRoot = DiffFactory.eINSTANCE.createDiffGroup();
 
 		// It is a possibility that no elements were matched
 		if (match.getMatchedElements().size() > 0) {
 			// we have to browse the model and create the corresponding operations
 			final Match2Elements matchRoot = (Match2Elements)match.getMatchedElements().get(0);
 			// browsing the match model
 			doDiffDelegate(diffRoot, matchRoot);
 		}
 		// iterate over the unmatched elements end determine if they have been
 		// added or removed.
 		processUnmatchedElements(diffRoot, match.getUnmatchedElements());
 		return diffRoot;
 	}
 
 	/**
 	 * Return the left or right matched EObject from the one given. More specifically, this will return the
 	 * left matched element if the given {@link EObject} is the right one, or the right matched element if the
 	 * given {@link EObject} is either the left or the origin one.
 	 * 
 	 * @param from
 	 *            The original {@link EObject}.
 	 * @return The matched {@link EObject}.
 	 */
 	protected final EObject getMatchedEObject(EObject from) {
 		EObject matchedEObject = null;
 		final Collection<EStructuralFeature.Setting> settings = matchCrossReferencer.get(from);
 		if (settings == null)
 			return null;
 		for (final org.eclipse.emf.ecore.EStructuralFeature.Setting setting : settings) {
 			if (setting.getEObject() instanceof Match2Elements) {
 				if (setting.getEStructuralFeature().getFeatureID() == MatchPackage.MATCH2_ELEMENTS__LEFT_ELEMENT) {
 					matchedEObject = ((Match2Elements)setting.getEObject()).getRightElement();
 				} else if (setting.getEStructuralFeature().getFeatureID() == MatchPackage.MATCH2_ELEMENTS__RIGHT_ELEMENT) {
 					matchedEObject = ((Match2Elements)setting.getEObject()).getLeftElement();
 				}
 			}
 		}
 		return matchedEObject;
 	}
 
 	/**
 	 * Return the specified matched {@link EObject} from the one given.
 	 * 
 	 * @param from
 	 *            The original {@link EObject}.
 	 * @param side
 	 *            side of the object we seek. Must be one of
 	 *            <ul>
 	 *            <li>{@link #ANCESTOR_OBJECT}</li>
 	 *            <li>{@link #LEFT_OBJECT}</li>
 	 *            <li>{@link #RIGHT_OBJECT}</li>
 	 *            </ul>
 	 *            .
 	 * @return The matched EObject.
 	 * @throws IllegalArgumentException
 	 *             Thrown if <code>side</code> is invalid.
 	 */
 	protected final EObject getMatchedEObject(EObject from, int side) throws IllegalArgumentException {
 		if (side != LEFT_OBJECT && side != RIGHT_OBJECT && side != ANCESTOR_OBJECT) {
 			throw new IllegalArgumentException(EMFCompareDiffMessages
 					.getString("GenericDiffEngine.IllegalSide")); //$NON-NLS-1$
 		}
 		EObject matchedEObject = null;
 		final Collection<EStructuralFeature.Setting> settings = matchCrossReferencer.get(from);
 		if (settings == null)
 			return null;
 		for (final org.eclipse.emf.ecore.EStructuralFeature.Setting setting : settings) {
 			if (setting.getEObject() instanceof Match2Elements) {
 				if (side == LEFT_OBJECT) {
 					matchedEObject = ((Match2Elements)setting.getEObject()).getLeftElement();
 				} else if (side == RIGHT_OBJECT) {
 					matchedEObject = ((Match2Elements)setting.getEObject()).getRightElement();
 				} else if (setting.getEObject() instanceof Match3Elements) {
 					matchedEObject = ((Match3Elements)setting.getEObject()).getOriginElement();
 				}
 			}
 		}
 		return matchedEObject;
 	}
 
 	/**
 	 * This will process the {@link #unmatchedElements unmatched elements} list and create the appropriate
 	 * {@link DiffElement}s.
 	 * 
 	 * @param diffRoot
 	 *            {@link DiffGroup} under which to create the {@link DiffElement}s.
 	 * @param unmatched
 	 *            The MatchModel's {@link UnmatchElement}s.
 	 */
 	protected void processUnmatchedElements(DiffGroup diffRoot, List<UnmatchElement> unmatched) {
 		final List<UnmatchElement> filteredUnmatched = new ArrayList<UnmatchElement>(unmatched.size());
 		for (final UnmatchElement element : unmatched) {
 			if (!(element.getElement() instanceof EGenericType)) {
 				filteredUnmatched.add(element);
 			}
 		}
 		for (final UnmatchElement unmatchElement : filteredUnmatched) {
 			final EObject element = unmatchElement.getElement();
 			final EObject leftParent = getMatchedEObject(element.eContainer());
 
 			final ConflictingDiffElement container;
 			if (unmatchElement.isConflicting()) {
 				container = DiffFactory.eINSTANCE.createConflictingDiffElement();
 				container.setLeftParent(leftParent);
 				container.setRightParent(element.eContainer());
 				container.setOriginElement(getMatchedEObject(element, ANCESTOR_OBJECT));
 			} else {
 				container = null;
 			}
 
 			if (unmatchElement.getSide() == Side.RIGHT) {
 				// add RemoveModelElement
 				final ModelElementChangeRightTarget operation = DiffFactory.eINSTANCE
 						.createModelElementChangeRightTarget();
 				operation.setRightElement(element);
 				operation.setRemote(unmatchElement.isRemote());
 				operation.setLeftParent(leftParent);
 				if (container != null) {
 					container.getSubDiffElements().add(operation);
 					addInContainerPackage(diffRoot, container, leftParent);
 				} else {
 					addInContainerPackage(diffRoot, operation, leftParent);
 				}
 			} else {
 				// add AddModelElement
 				final ModelElementChangeLeftTarget operation = DiffFactory.eINSTANCE
 						.createModelElementChangeLeftTarget();
 				operation.setLeftElement(element);
 				operation.setRemote(unmatchElement.isRemote());
 				operation.setRightParent(leftParent);
 				if (container != null) {
 					container.getSubDiffElements().add(operation);
 					addInContainerPackage(diffRoot, container, leftParent);
 				} else {
 					addInContainerPackage(diffRoot, operation, leftParent);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determines if we should ignore an attribute for diff detection.
 	 * <p>
 	 * Default is to ignore attributes marked either
 	 * <ul>
 	 * <li>Transient</li>
 	 * <li>Derived</li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * Clients should override this if they wish to ignore other attributes.
 	 * </p>
 	 * 
 	 * @param attribute
 	 *            Attribute to determine whether it should be ignored.
 	 * @return <code>True</code> if attribute has to be ignored, <code>False</code> otherwise.
 	 * @deprecated Replaced by {@link AttributesCheck}.
 	 */
 	@Deprecated
 	protected boolean shouldBeIgnored(EAttribute attribute) {
 		boolean ignore = attribute.isTransient();
 		ignore = ignore || attribute.isDerived();
 		return ignore;
 	}
 
 	/**
 	 * Determines if we should ignore a reference for diff detection.
 	 * <p>
 	 * Default is to ignore references marked either
 	 * <ul>
 	 * <li>Containment</li>
 	 * <li>Container</li>
 	 * <li>Transient</li>
 	 * <li>Derived</li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * Clients should override this if they wish to ignore other references.
 	 * </p>
 	 * 
 	 * @param reference
 	 *            Reference to determine whether it should be ignored.
 	 * @return <code>True</code> if reference has to be ignored, <code>False</code> otherwise.
 	 * @deprecated Replaced by {@link ReferencesCheck}.
 	 */
 	@Deprecated
 	protected boolean shouldBeIgnored(EReference reference) {
 		boolean ignore = reference.isContainment();
 		ignore = ignore || reference.isDerived();
 		ignore = ignore || reference.isTransient();
 		ignore = ignore || reference.isContainer();
 		ignore = ignore || reference.eContainer() == EcorePackage.eINSTANCE.getEGenericType();
 		return ignore;
 	}
 
 	/**
 	 * Builds a {@link DiffGroup} for the <code>targetParent</code> with its full hierarchy.
 	 * 
 	 * @param targetParent
 	 *            Parent of the operation we're building a {@link DiffGroup} for.
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffModel}.
 	 * @return {@link DiffGroup} containing the full hierarchy needed for the <code>targetParent</code>.
 	 */
 	private DiffGroup buildHierarchyGroup(EObject targetParent, DiffGroup root) {
 		// if targetElement has a parent, we call buildgroup on it, else we add
 		// the current group to the root
 		DiffGroup curGroup = DiffFactory.eINSTANCE.createDiffGroup();
 		curGroup.setRightParent(targetParent);
 		final DiffGroup targetGroup = findExistingGroup(root, targetParent);
 		if (targetGroup != null) {
 			curGroup = targetGroup;
 		}
 		if (targetParent.eContainer() == null) {
 			root.getSubDiffElements().add(curGroup);
 			return curGroup;
 		}
 		// if targetElement is the root of a fragment resource, do not walk the hierarchy up,
 		// instead report changes to fragments in their own resource's context
 		if (targetParent.eResource() == targetParent.eContainer().eResource()) {
 			buildHierarchyGroup(targetParent.eContainer(), root).getSubDiffElements().add(curGroup);
 		} else {
 			root.getSubDiffElements().add(curGroup);
 		}
 		return curGroup;
 	}
 
 	/**
 	 * This will create the {@link MoveModelElement} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element that has been moved in the left model.
 	 * @param right
 	 *            Corresponding element that has been moved in the right model.
 	 */
 	private void createMoveOperation(DiffGroup root, EObject left, EObject right) {
 		final MoveModelElement operation = DiffFactory.eINSTANCE.createMoveModelElement();
 		operation.setRightElement(right);
 		operation.setLeftElement(left);
 		operation.setRightTarget(getMatchedEObject(left.eContainer()));
 		operation.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(operation);
 	}
 
 	/**
 	 * This will create the {@link RemoteMoveModelElement} under the given {@link DiffGroup root}.<br/>
 	 * A {@link RemoteMoveModelElement} represents the fact that an element has been remotely moved since the
 	 * ancestor model, but the right model has kept the element in its former place.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element that has been moved in the left model.
 	 * @param right
 	 *            Element of the right model corresponding to the left one.
 	 */
 	private void createRemoteMoveOperation(DiffGroup root, EObject left, EObject right) {
 		final MoveModelElement operation = DiffFactory.eINSTANCE.createMoveModelElement();
 		operation.setRemote(true);
 		operation.setRightElement(right);
 		operation.setLeftElement(left);
 		operation.setRightTarget(getMatchedEObject(left.eContainer()));
 		operation.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(operation);
 	}
 
 	/**
 	 * This will create a {@link UpdateContainmentFeature} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element of the left model corresponding to the right one.
 	 * @param right
 	 *            Element that has been moved since the last (ancestor for three-way comparison, left for
 	 *            two-way comparison) version.
 	 */
 	private void createRemoteUpdateContainmentOperation(DiffGroup root, EObject left, EObject right) {
 		final UpdateContainmentFeature updateContainment = DiffFactory.eINSTANCE
 				.createUpdateContainmentFeature();
 		updateContainment.setRemote(true);
 		updateContainment.setLeftElement(left);
 		updateContainment.setRightElement(right);
 		updateContainment.setRightTarget(getMatchedEObject(left.eContainer()));
 		updateContainment.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(updateContainment);
 	}
 
 	/**
 	 * This will create a {@link UpdateContainmentFeature} under the given {@link DiffGroup root}.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffElement} to create.
 	 * @param left
 	 *            Element of the left model corresponding to the right one.
 	 * @param right
 	 *            Element that has been moved since the last (ancestor for three-way comparison, right for
 	 *            two-way comparison) version.
 	 */
 	private void createUpdateContainmentOperation(DiffGroup root, EObject left, EObject right) {
 		final UpdateContainmentFeature updateContainment = DiffFactory.eINSTANCE
 				.createUpdateContainmentFeature();
 		updateContainment.setLeftElement(left);
 		updateContainment.setRightElement(right);
 		updateContainment.setRightTarget(getMatchedEObject(left.eContainer()));
 		updateContainment.setLeftTarget(getMatchedEObject(right.eContainer()));
 		root.getSubDiffElements().add(updateContainment);
 	}
 
 	/**
 	 * This is the core of the diff computing for two way comparison. This will call for checks on attributes,
 	 * references and model elements to check for updates/changes.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffModel} to create.
 	 * @param match
 	 *            {@link Match2Elements root} of the {@link MatchModel} to analyze.
 	 */
 	private void doDiffDelegate(DiffGroup root, Match2Elements match) {
 		final DiffGroup current = DiffFactory.eINSTANCE.createDiffGroup();
 		current.setRightParent(match.getRightElement());
 		try {
 			checkForDiffs(current, match);
 		} catch (final FactoryException e) {
 			log(e);
 		}
 		// we need to build this list to avoid concurrent modifications
 		final List<DiffElement> shouldAddToList = new ArrayList<DiffElement>();
 		// we really have changes
 		if (current.getSubDiffElements().size() > 0) {
 			final Iterator<DiffElement> it2 = current.getSubDiffElements().iterator();
 			while (it2.hasNext()) {
 				final DiffElement diff = it2.next();
 				if (!(diff instanceof DiffGroup)) {
 					shouldAddToList.add(diff);
 				}
 			}
 			for (final DiffElement diff : shouldAddToList) {
 				addInContainerPackage(root, diff, current.getRightParent());
 			}
 		}
 		// taking care of our children
 		final Iterator<MatchElement> it = match.getSubMatchElements().iterator();
 		while (it.hasNext()) {
 			final Match2Elements element = (Match2Elements)it.next();
 			doDiffDelegate(root, element);
 		}
 	}
 
 	/**
 	 * Log an error. Clients may override this to perform custom logging.
 	 * 
 	 * @since 1.1
 	 * @param e
 	 *            any Exception.
 	 */
 	protected void log(final Exception e) {
 		EMFComparePlugin.log(e, false);
 	}
 
 	/**
 	 * This is the core of the diff computing for three way comparison. This will call for checks on
 	 * attributes, references and model elements to check for updates/changes.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup root} of the {@link DiffModel} to create.
 	 * @param match
 	 *            {@link Match3Elements root} of the {@link MatchModel} to analyze.
 	 */
 	private void doDiffDelegate(DiffGroup root, Match3Elements match) {
 		final DiffGroup current = DiffFactory.eINSTANCE.createDiffGroup();
 		current.setRightParent(match.getRightElement());
 		try {
 			checkForDiffs(current, match);
 		} catch (final FactoryException e) {
 			log(e);
 		}
 		// we need to build this list to avoid concurrent modifications
 		final List<DiffElement> shouldAddToList = new ArrayList<DiffElement>();
 		// we really have changes
 		if (current.getSubDiffElements().size() > 0) {
 			final Iterator<DiffElement> it2 = current.getSubDiffElements().iterator();
 			while (it2.hasNext()) {
 				final DiffElement diff = it2.next();
 				if (!(diff instanceof DiffGroup)) {
 					shouldAddToList.add(diff);
 				}
 			}
 			for (final DiffElement diff : shouldAddToList) {
 				addInContainerPackage(root, diff, current.getRightParent());
 			}
 		}
 		// taking care of our children
 		final Iterator<MatchElement> it = match.getSubMatchElements().iterator();
 		while (it.hasNext()) {
 			final MatchElement element = it.next();
 			if (element instanceof Match3Elements) {
 				doDiffDelegate(root, (Match3Elements)element);
 			} else {
 				doDiffDelegate(root, (Match2Elements)element);
 			}
 		}
 	}
 
 	/**
 	 * Searches for an existing {@link DiffGroup} under <code>root</code> to add the operation which parent is
 	 * <code>targetParent</code>.
 	 * 
 	 * @param root
 	 *            {@link DiffGroup Root} of the {@link DiffModel}.
 	 * @param targetParent
 	 *            Parent of the operation we're seeking a {@link DiffGroup} for.
 	 * @return {@link DiffGroup} for the <code>targetParent</code>.
 	 */
 	private DiffGroup findExistingGroup(DiffGroup root, EObject targetParent) {
 		final TreeIterator<EObject> it = root.eAllContents();
 		while (it.hasNext()) {
 			final EObject obj = it.next();
 			if (obj instanceof DiffGroup) {
 				final EObject groupParent = ((DiffGroup)obj).getRightParent();
 				if (groupParent == targetParent || getMatchedEObject(groupParent) == targetParent)
 					return (DiffGroup)obj;
 			}
 		}
 		return null;
 	}
 }
