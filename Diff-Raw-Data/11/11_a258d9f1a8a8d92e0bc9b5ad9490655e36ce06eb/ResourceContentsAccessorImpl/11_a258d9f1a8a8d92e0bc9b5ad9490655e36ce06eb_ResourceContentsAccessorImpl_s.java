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
 package org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.impl;
 
 import static com.google.common.base.Predicates.and;
 import static com.google.common.base.Predicates.instanceOf;
 import static com.google.common.base.Predicates.not;
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.size;
 import static com.google.common.collect.Iterables.transform;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Lists.newArrayListWithCapacity;
 import static org.eclipse.emf.compare.utils.EMFComparePredicates.hasConflict;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.ConflictKind;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.MatchResource;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.IResourceContentsAccessor;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.impl.TypeConstants;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.InsertionPoint;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MatchedObject;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.provider.EcoreEditPlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  */
 public class ResourceContentsAccessorImpl implements IResourceContentsAccessor {
 
 	/** The difference performed. */
 	private final Diff fDiff;
 
 	/** The side on which the difference is located. */
 	private final MergeViewerSide fSide;
 
 	/** The match associated to the performed difference. */
 	private final Match fOwnerMatch;
 
 	/** The list of sibling differences of the performed difference. */
 	private final ImmutableList<Diff> fDifferences;
 
 	/**
 	 * @param diff
 	 *            The difference performed.
 	 * @param side
 	 *            The side on which the difference is located.
 	 */
 	public ResourceContentsAccessorImpl(Diff diff, MergeViewerSide side) {
 		fDiff = diff;
 		fSide = side;
 		fOwnerMatch = diff.getMatch();
 		fDifferences = computeDifferences();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.IResourceContentsAccessor#getComparison()
 	 */
 	public Comparison getComparison() {
 		return fOwnerMatch.getComparison();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.IResourceContentsAccessor#getInitialItem()
 	 */
 	public IMergeViewerItem getInitialItem() {
 		IMergeViewerItem ret = null;
 		ImmutableList<? extends IMergeViewerItem> items = getItems();
 		for (IMergeViewerItem item : items) {
 			Diff diff = item.getDiff();
 			if (diff == fDiff) {
 				ret = item;
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.IResourceContentsAccessor#getResource(org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide)
 	 */
 	public Resource getResource(MergeViewerSide side) {
 		Resource resource = null;
 		Collection<MatchResource> matchResources = fOwnerMatch.getComparison().getMatchedResources();
 		final String diffResourceURI = ((ResourceAttachmentChange)fDiff).getResourceURI();
 		for (MatchResource matchResource : matchResources) {
 			switch (side) {
 				case ANCESTOR:
 					resource = matchResource.getOrigin();
 					break;
 				case LEFT:
 					resource = matchResource.getLeft();
 					break;
 				case RIGHT:
 					resource = matchResource.getRight();
 					break;
 				default:
 					throw new IllegalStateException();
 			}
 			if (resource != null) {
 				URI resourceURI = resource.getURI();
 				if (diffResourceURI.equals(resourceURI.toString())) {
 					return resource;
 				}
 			}
 		}
 		return resource;
 	}
 
 	/**
 	 * Returns the contents of the current resource on the given side.
 	 * 
 	 * @param side
 	 *            The given side.
 	 * @return The contents of the current resource on the given side.
 	 */
 	public List<EObject> getResourceContents(MergeViewerSide side) {
 		Resource resource = getResource(side);
 		if (resource != null) {
 			return resource.getContents();
 		}
 		return Collections.emptyList();
 
 	}
 
 	/**
 	 * Returns the side of the content merge viewer on which the difference is performed.
 	 * 
 	 * @return The side of the content merge viewer on which the difference is performed.
 	 */
 	protected final MergeViewerSide getSide() {
 		return fSide;
 	}
 
 	/**
 	 * Returns the list of sibling differences of the current difference.
 	 * 
 	 * @return The list of sibling differences of the current difference.
 	 */
 	protected final ImmutableList<Diff> getDifferences() {
 		return fDifferences;
 	}
 
 	/**
 	 * Computes the list of sibling differences of the current difference.
 	 * 
 	 * @return The list of sibling differences of the current difference.
 	 */
 	protected ImmutableList<Diff> computeDifferences() {
 		Iterable<EObject> concat = Iterables.concat(getResourceContents(MergeViewerSide.LEFT),
 				getResourceContents(MergeViewerSide.RIGHT), getResourceContents(MergeViewerSide.ANCESTOR));
 
 		final Comparison comparison = fOwnerMatch.getComparison();
 		Iterable<Match> matches = transform(concat, new Function<EObject, Match>() {
 			public Match apply(EObject eObject) {
 				return comparison.getMatch(eObject);
 			}
 		});
 		Iterable<Diff> siblingDifferences = concat(transform(matches, new Function<Match, List<Diff>>() {
 			public List<Diff> apply(Match match) {
 				return match.getDifferences();
 			}
 		}));
 		Predicate<? super Diff> diffFilter = and(instanceOf(ResourceAttachmentChange.class),
 				not(hasConflict(ConflictKind.PSEUDO)));
 		return ImmutableSet.<Diff> copyOf(filter(siblingDifferences, diffFilter)).asList();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.IResourceContentsAccessor#getItems()
 	 */
 	public ImmutableList<? extends IMergeViewerItem> getItems() {
 		List<? extends IMergeViewerItem> ret;
 		List<?> list = getResourceContents(getSide());
 		ret = createMergeViewerItemFrom(list);
 
 		if (getSide() != MergeViewerSide.ANCESTOR) {
 			ret = createInsertionPoints(ret);
 		}
 
 		return ImmutableList.copyOf(ret);
 	}
 
 	private List<? extends IMergeViewerItem> createMergeViewerItemFrom(List<?> values) {
 		List<IMergeViewerItem> ret = newArrayListWithCapacity(values.size());
 		for (Object value : values) {
 			IMergeViewerItem valueToAdd = createMergeViewerItemFrom(value);
 			ret.add(valueToAdd);
 		}
 		return ret;
 	}
 
 	private IMergeViewerItem createMergeViewerItemFrom(Object object) {
 		Diff diff = getDiffWithValue(object);
 		Object left = matchingValue(object, MergeViewerSide.LEFT);
 		Object right = matchingValue(object, MergeViewerSide.RIGHT);
 		Object ancestor = matchingValue(object, MergeViewerSide.ANCESTOR);
 		return new MatchedObject(diff, left, right, ancestor);
 	}
 
 	private List<? extends IMergeViewerItem> createInsertionPoints(
 			final List<? extends IMergeViewerItem> values) {
 		List<IMergeViewerItem> ret = newArrayList(values);
 		for (Diff diff : getDifferences().reverse()) {
 			boolean rightToLeft = (getSide() == MergeViewerSide.LEFT);
 			Object left = getValueFromDiff(diff, MergeViewerSide.LEFT);
 			Object right = getValueFromDiff(diff, MergeViewerSide.RIGHT);
 
 			if (left == null && right == null) {
 				// Do not display anything
 			} else {
 				final boolean leftEmptyBox = getSide() == MergeViewerSide.LEFT
 						&& (left == null || !getResourceContents(getSide()).contains(left));
 				final boolean rightEmptyBox = getSide() == MergeViewerSide.RIGHT
 						&& (right == null || !getResourceContents(getSide()).contains(right));
 				if (leftEmptyBox || rightEmptyBox) {
 					Object ancestor = getValueFromDiff(diff, MergeViewerSide.ANCESTOR);
 
 					InsertionPoint insertionPoint = new InsertionPoint(diff, left, right, ancestor);
 
 					final int insertionIndex = Math.min(findInsertionIndex(diff, rightToLeft), ret.size());
 					List<IMergeViewerItem> subList = ret.subList(0, insertionIndex);
 					final int nbInsertionPointBefore = size(filter(subList, InsertionPoint.class));
 
 					int index = Math.min(insertionIndex + nbInsertionPointBefore, ret.size());
 					ret.add(index, insertionPoint);
 				}
 			}
 		}
 		return ret;
 	}
 
 	protected int findInsertionIndex(Diff diff, boolean rightToLeft) {
 		final Match valueMatch = diff.getMatch();
 		final Comparison comparison = valueMatch.getComparison();
 
 		final EObject expectedValue;
 		if (valueMatch.getLeft() != null) {
 			expectedValue = valueMatch.getLeft();
 		} else {
 			expectedValue = valueMatch.getRight();
 		}
 
 		final Resource initialResource;
 		final Resource expectedResource;
 		if (rightToLeft) {
 			initialResource = getResource(MergeViewerSide.RIGHT);
 			expectedResource = getResource(MergeViewerSide.LEFT);
 		} else {
 			initialResource = getResource(MergeViewerSide.LEFT);
 			expectedResource = getResource(MergeViewerSide.RIGHT);
 		}
		final List<EObject> sourceList = initialResource.getContents();
		final List<EObject> targetList = expectedResource.getContents();
 
		return DiffUtil.findInsertionIndex(comparison, sourceList, targetList, expectedValue);
 	}
 
 	private Diff getDiffWithValue(Object value) {
 		Diff ret = null;
 		for (Diff diff : getDifferences()) {
 			Object valueOfDiff = getValueFromDiff(diff, getSide());
 			if (valueOfDiff == value) {
 				ret = diff;
 				break;
 			}
 		}
 		return ret;
 	}
 
 	protected Object getValueFromDiff(final Diff diff, MergeViewerSide side) {
 		Object diffValue = getDiffValue(diff, side);
 		Object ret = matchingValue(diffValue, side);
 		return ret;
 	}
 
 	private Object matchingValue(Object object, MergeViewerSide side) {
 		final Object ret;
 		if (object instanceof EObject) {
 			final Match matchOfValue = getComparison().getMatch((EObject)object);
 			if (matchOfValue != null) {
 				switch (side) {
 					case ANCESTOR:
 						ret = matchOfValue.getOrigin();
 						break;
 					case LEFT:
 						ret = matchOfValue.getLeft();
 						break;
 					case RIGHT:
 						ret = matchOfValue.getRight();
 						break;
 					default:
 						throw new IllegalStateException();
 				}
 			} else {
 				ret = matchingValue(object, getResourceContents(side));
 			}
 		} else {
 			ret = matchingValue(object, getResourceContents(side));
 		}
 		return ret;
 	}
 
 	private Object matchingValue(Object value, List<?> in) {
 		Object ret = null;
 		IEqualityHelper equalityHelper = getComparison().getEqualityHelper();
 		Iterator<?> valuesIterator = in.iterator();
 		while (valuesIterator.hasNext() && ret == null) {
 			Object object = valuesIterator.next();
 			if (equalityHelper.matchingValues(object, value)) {
 				ret = object;
 			}
 		}
 		return ret;
 	}
 
 	protected Object getDiffValue(Diff diff, MergeViewerSide side) {
 		final Object ret;
 		if (diff instanceof ResourceAttachmentChange) {
 			Match match = ((ResourceAttachmentChange)diff).getMatch();
 			switch (side) {
 				case ANCESTOR:
 					switch (diff.getSource()) {
 						case LEFT:
 							ret = match.getRight();
 							break;
 						case RIGHT:
 							ret = match.getLeft();
 							break;
 						default:
 							throw new IllegalStateException();
 					}
 					break;
 				case LEFT:
 					ret = match.getLeft();
 					break;
 				case RIGHT:
 					ret = match.getRight();
 					break;
 				default:
 					throw new IllegalStateException();
 			}
 		} else {
 			ret = null;
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement#getName()
 	 */
 	public String getName() {
 		return ResourceContentsAccessorImpl.class.getName();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement#getImage()
 	 */
 	public Image getImage() {
 		return ExtendedImageRegistry.getInstance().getImage(
 				EcoreEditPlugin.getPlugin().getImage("full/obj16/EObject")); //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement#getType()
 	 */
 	public String getType() {
 		return TypeConstants.TYPE__ERESOURCE_DIFF;
 	}
 
 }
