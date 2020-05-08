 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.uml2.ide.ui.internal.accessor;
 
 import static com.google.common.base.Predicates.and;
 import static com.google.common.base.Predicates.instanceOf;
 import static org.eclipse.emf.compare.utils.EMFComparePredicates.hasState;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 import java.util.List;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.uml2.StereotypeApplicationChange;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.Stereotype;
 import org.eclipse.uml2.uml.util.UMLUtil;
 
 /**
  * In the case of stereotype application changes, we will display to the user the whole list of stereotypes
  * applied to the element instead of just the changed one. This will allow us to display which of the
  * stereotypes of this list have changed, much like we display the list of all referenced objects on a
  * reference change.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class UMLStereotypeApplicationChangeFeatureAccessor extends UMLIDEManyStructuralFeatureAccessor {
 	/**
 	 * Creates a specialized accessor for the stereotype application differences.
 	 * 
 	 * @param diff
 	 *            The diff for which we need an accessor.
 	 * @param side
 	 *            The side on which this accessor will be used.
 	 */
 	public UMLStereotypeApplicationChangeFeatureAccessor(StereotypeApplicationChange diff,
 			MergeViewerSide side) {
 		super(diff, side);
 	}
 
 	/**
 	 * Returns the list of applied stereotypes for the underlying difference's target on the given side.
 	 * 
 	 * @param side
 	 *            The side for which we need the list of applied steretoypes.
 	 * @return The list of stereotypes to display for this side.
 	 */
 	@Override
 	protected List<Stereotype> getFeatureValues(MergeViewerSide side) {
 		final EObject eObject = getEObject(side);
 		if (eObject instanceof Element) {
 			return ((Element)eObject).getAppliedStereotypes();
 		}
 		return ImmutableList.of();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.uml2.ide.ui.internal.accessor.UMLIDEManyStructuralFeatureAccessor#findInsertionIndex(org.eclipse.emf.compare.Diff,
 	 *      boolean)
 	 */
 	@Override
 	protected int findInsertionIndex(Diff diff, boolean rightToLeft) {
 		// We can't use the "default" implementation of DiffUtil : we have no actual reference for the list of
 		// applied stereotypes.
 		final StereotypeApplicationChange stereotypeChange = (StereotypeApplicationChange)diff;
 		final Match match = diff.getMatch();
 		final Comparison comparison = match.getComparison();
 
 		final List<Stereotype> sourceList;
 		if (match.getOrigin() != null && diff.getKind() == DifferenceKind.DELETE) {
 			sourceList = getFeatureValues(MergeViewerSide.ANCESTOR);
 		} else if (rightToLeft) {
 			sourceList = getFeatureValues(MergeViewerSide.RIGHT);
 		} else {
 			sourceList = getFeatureValues(MergeViewerSide.LEFT);
 		}
 
 		final List<Stereotype> targetList;
 		if (rightToLeft) {
 			targetList = getFeatureValues(MergeViewerSide.LEFT);
 		} else {
 			targetList = getFeatureValues(MergeViewerSide.RIGHT);
 		}
 
 		final Iterable<Stereotype> ignoredElements;
 		if (comparison.isThreeWay() && diff.getKind() == DifferenceKind.DELETE) {
 			ignoredElements = computeIgnoredElements(targetList, stereotypeChange);
 		} else {
 			ignoredElements = Lists.newArrayList();
 		}
 
 		final Stereotype stereotype = UMLUtil.getStereotype(stereotypeChange.getDiscriminant());
 
 		return DiffUtil.findInsertionIndex(comparison, ignoredElements, sourceList, targetList, stereotype);
 	}
 
 	/**
 	 * When computing the insertion index of a stereotype in the list of applied stereotypes, we need to
 	 * ignore all stereotypes that feature unresolved Diffs.
 	 * 
 	 * @param candidates
 	 *            The sequence in which we need to compute an insertion index.
 	 * @param diff
 	 *            The diff we are computing an insertion index for.
 	 * @param <E>
 	 *            Type of the list's content.
 	 * @return The list of elements that should be ignored when computing the insertion index for a new
 	 *         element in {@code candidates}.
 	 */
 	private static <E> Iterable<E> computeIgnoredElements(Iterable<E> candidates,
 			final StereotypeApplicationChange diff) {
 		return Iterables.filter(candidates, new Predicate<Object>() {
 			public boolean apply(final Object element) {
 				if (element instanceof EObject) {
 					final Comparison comparison = diff.getMatch().getComparison();
 					final Match match = comparison.getMatch((EObject)element);
 
 					return Iterables.any(match.getDifferences(), and(
 							instanceOf(StereotypeApplicationChange.class),
 							hasState(DifferenceState.UNRESOLVED)));
 				}
 				return false;
 			}
 		});
 	}
 }
