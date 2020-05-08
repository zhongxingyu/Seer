 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl;
 
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.getFirst;
 import static com.google.common.collect.Lists.newArrayList;
 
 import com.google.common.collect.Lists;
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.ConflictKind;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.util.MergeViewerUtil;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 
 /**
  * A specific {@link MergeViewerItem} for {@link ResourceAttachmentChange}.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  */
 public class ResourceAttachmentChangeMergeViewerItem extends MergeViewerItem.Container {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem.Container#Container(Comparison
 	 *      comparison, Diff diff, Object left, Object right, Object ancestor, MergeViewerSide side,
 	 *      AdapterFactory adapterFactory)
 	 */
 	public ResourceAttachmentChangeMergeViewerItem(Comparison comparison, Diff diff, Resource left,
 			Resource right, Resource ancestor, MergeViewerSide side, AdapterFactory adapterFactory) {
 		super(comparison, diff, left, right, ancestor, side, adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem.Container#Container(Comparison,
 	 *      Diff, Match, MergeViewerSide, AdapterFactory)
 	 */
 	public ResourceAttachmentChangeMergeViewerItem(Comparison comparison, Diff diff, Match match,
 			MergeViewerSide side, AdapterFactory adapterFactory) {
 		super(comparison, diff, match, side, adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem.Container#getChildren()
 	 */
 	@Override
 	public IMergeViewerItem[] getChildren() {
 		Object sideValue = getSideValue(getSide());
 		Object bestSideValue = getBestSideValue();
 
 		List<IMergeViewerItem> ret = newArrayList();
 
 		if (bestSideValue instanceof Resource) {
 			List<IMergeViewerItem> mergeViewerItems = newArrayList();
 			if (sideValue instanceof Resource) {
 				mergeViewerItems = createMergeViewerItemFrom(((Resource)sideValue).getContents());
 			}
 
 			if (getSide() != MergeViewerSide.ANCESTOR) {
 				EList<Diff> differences = getComparison().getDifferences();
 				List<ResourceAttachmentChange> racs = Lists.newArrayList(filter(differences,
 						ResourceAttachmentChange.class));
 				List<ResourceAttachmentChange> resourceAttachmentChanges = Lists.newArrayList(racs);
 				Object left = getLeft();
 				Object right = getRight();
 				Object ancestor = getAncestor();
 				// Removes resource attachment changes that are not linked with the current resources.
 				for (ResourceAttachmentChange resourceAttachmentChange : racs) {
 					if (left == null
 							|| (left instanceof Resource && !resourceAttachmentChange.getResourceURI()
 									.equals(((Resource)left).getURI().toString()))) {
 						if (right == null
 								|| (right instanceof Resource && !resourceAttachmentChange.getResourceURI()
 										.equals(((Resource)right).getURI().toString()))) {
 							if (ancestor == null
 									|| (ancestor instanceof Resource && !resourceAttachmentChange
 											.getResourceURI()
 											.equals(((Resource)ancestor).getURI().toString()))) {
 								resourceAttachmentChanges.remove(resourceAttachmentChange);
 							}
 						}
 					}
 				}
 				ret.addAll(createInsertionPoints(mergeViewerItems, resourceAttachmentChanges));
 			} else {
 				ret.addAll(mergeViewerItems);
 			}
 
 		}
 
 		return ret.toArray(getNoItemsArr());
 
 	}
 
 	/**
 	 * Creates an IMergeViewerItem from an EObject.
 	 * 
 	 * @param eObject
 	 *            the given eObject.
 	 * @return an IMergeViewerItem.
 	 */
 	@Override
 	protected IMergeViewerItem createMergeViewerItemFrom(EObject eObject) {
 
 		Match match = getComparison().getMatch(eObject);
 
 		if (match != null) {
 			ResourceAttachmentChange rac = getFirst(filter(match.getDifferences(),
 					ResourceAttachmentChange.class), null);
 			return new MergeViewerItem.Container(getComparison(), rac, match, getSide(), getAdapterFactory());
 		}
 		return null;
 
 	}
 
 	/**
 	 * Creates insertion points for the given list of IMergeViewerItem corresponding to the given list of
 	 * ResourceAttachmentChange.
 	 * 
 	 * @param values
 	 *            the given list of IMergeViewerItem.
 	 * @param racs
 	 *            the givel list of ResourceAttachmentChange
 	 * @return the given list of IMergeViewerItem with additional insertion points.
 	 */
 	private List<? extends IMergeViewerItem> createInsertionPoints(
 			final List<? extends IMergeViewerItem> values, final List<ResourceAttachmentChange> racs) {
 		List<IMergeViewerItem> ret = newArrayList(values);
 		for (ResourceAttachmentChange diff : Lists.reverse(racs)) {
 			boolean rightToLeft = (getSide() == MergeViewerSide.LEFT);
 			Comparison comparison = getComparison();
 			Object left = MergeViewerUtil.getValueFromResourceAttachmentChange(diff, comparison,
 					MergeViewerSide.LEFT);
 			Object right = MergeViewerUtil.getValueFromResourceAttachmentChange(diff, comparison,
 					MergeViewerSide.RIGHT);
 
 			boolean b1 = diff.getSource() == DifferenceSource.LEFT && diff.getKind() == DifferenceKind.DELETE
 					&& getSide() == MergeViewerSide.LEFT;
 			boolean b2 = diff.getSource() == DifferenceSource.LEFT && diff.getKind() == DifferenceKind.ADD
 					&& getSide() == MergeViewerSide.RIGHT;
 			boolean b3 = diff.getSource() == DifferenceSource.RIGHT && diff.getKind() == DifferenceKind.ADD
 					&& getSide() == MergeViewerSide.LEFT;
 			boolean b4 = diff.getSource() == DifferenceSource.RIGHT
 					&& diff.getKind() == DifferenceKind.DELETE && getSide() == MergeViewerSide.RIGHT;
 
 			// do not duplicate insertion point for pseudo add conflict
 			// so we must only create one for pseudo delete conflict
 			boolean b5 = diff.getConflict() == null
 					|| (diff.getConflict().getKind() != ConflictKind.PSEUDO || diff.getKind() == DifferenceKind.DELETE);
 
 			if ((b1 || b2 || b3 || b4) && b5) {
 				Object ancestor = MergeViewerUtil.getValueFromResourceAttachmentChange(diff, comparison,
 						MergeViewerSide.ANCESTOR);
 				if (left != null
 						&& MergeViewerUtil.getResource(comparison, MergeViewerSide.LEFT, diff) == null) {
 					left = null;
 				}
 				if (right != null
 						&& MergeViewerUtil.getResource(comparison, MergeViewerSide.RIGHT, diff) == null) {
 					right = null;
 				}
 				if (ancestor != null
 						&& MergeViewerUtil.getResource(comparison, MergeViewerSide.ANCESTOR, diff) == null) {
 					ancestor = null;
 				}
 				IMergeViewerItem insertionPoint = new MergeViewerItem.Container(comparison, diff, left,
 						right, ancestor, getSide(), getAdapterFactory());
 
 				final int insertionIndex;
 				if (left == null && right == null) {
 					insertionIndex = MergeViewerUtil.getResource(comparison, MergeViewerSide.ANCESTOR, diff)
 							.getContents().indexOf(ancestor);
 				} else {
 					insertionIndex = Math.min(findInsertionIndex(diff, rightToLeft), ret.size());
 				}
 
 				// offset the insertion by the number of previous insertion points in the list
 				// Can not b improved by keeping the number of created insertion points because the given
 				// "values" parameter may already contains some insertion points.
 				int realIndex = 0;
				for (int index = 0; index < insertionIndex; realIndex++) {
 					if (!ret.get(realIndex).isInsertionPoint()) {
 						index++;
 					}
 				}
 				ret.add(realIndex, insertionPoint);
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Find an insertion index for the given diff.
 	 * 
 	 * @param diff
 	 *            the given diff.
 	 * @param rightToLeft
 	 *            the way of merge.
 	 * @return an insertion index for the given diff.
 	 */
 	private int findInsertionIndex(Diff diff, boolean rightToLeft) {
 		final Match valueMatch = diff.getMatch();
 		final Comparison comparison = getComparison();
 
 		final EObject expectedValue;
 		if (valueMatch.getLeft() != null) {
 			expectedValue = valueMatch.getLeft();
 		} else {
 			expectedValue = valueMatch.getRight();
 		}
 
 		final Resource initialResource;
 		final Resource expectedResource;
 		if (rightToLeft) {
 			initialResource = MergeViewerUtil.getResource(comparison, MergeViewerSide.RIGHT, diff);
 			expectedResource = MergeViewerUtil.getResource(comparison, MergeViewerSide.LEFT, diff);
 		} else {
 			initialResource = MergeViewerUtil.getResource(comparison, MergeViewerSide.LEFT, diff);
 			expectedResource = MergeViewerUtil.getResource(comparison, MergeViewerSide.RIGHT, diff);
 		}
 		if (expectedResource != null) {
 			final List<EObject> sourceList = initialResource.getContents();
 			final List<EObject> targetList = expectedResource.getContents();
 
 			return DiffUtil.findInsertionIndex(comparison, sourceList, targetList, expectedValue);
 		} else {
 			return 0;
 		}
 	}
 
 }
