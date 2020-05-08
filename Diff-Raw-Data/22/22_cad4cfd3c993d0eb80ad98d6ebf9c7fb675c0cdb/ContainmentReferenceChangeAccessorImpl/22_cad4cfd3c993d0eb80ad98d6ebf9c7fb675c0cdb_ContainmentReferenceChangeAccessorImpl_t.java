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
 package org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.impl;
 
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.transform;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.impl.TypeConstants;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.item.impl.MergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.internal.util.MergeViewerUtil;
 import org.eclipse.emf.compare.utils.EMFComparePredicates;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ContainmentReferenceChangeAccessorImpl extends AbstractStructuralFeatureAccessor {
 
 	private final Match fRootMatch;
 
 	/**
 	 * 
 	 */
 	public ContainmentReferenceChangeAccessorImpl(AdapterFactory adapterFactory,
 			ReferenceChange referenceChange, MergeViewerSide side) {
 		super(adapterFactory, referenceChange, side);
		final EObject value = referenceChange.getValue();
		final EObject rootContainer = EcoreUtil.getRootContainer(value);
		Match match = getComparison().getMatch(rootContainer);
		if (match == null) {
			match = getTopMostContainerWithMatch(value);
		}
		this.fRootMatch = match;

	}

	private Match getTopMostContainerWithMatch(final EObject value) {
		EObject eContainer = value.eContainer();
		Match eContainerMatch = getComparison().getMatch(eContainer);
		Match match = eContainerMatch;
		while (eContainer != null && eContainerMatch != null) {
			eContainer = eContainer.eContainer();
			match = eContainerMatch;
			eContainerMatch = getComparison().getMatch(eContainer);
		}
		return match;
 	}
 
 	/**
 	 * @return
 	 */
 	@Override
 	protected ImmutableList<Diff> computeDifferences() {
 		List<Diff> allDifferences = getComparison().getDifferences();
 		return ImmutableList.<Diff> copyOf(filter(filter(allDifferences, ReferenceChange.class),
 				EMFComparePredicates.CONTAINMENT_REFERENCE_CHANGE));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.mergeviewer.accessor.ICompareAccessor#getInitialItem()
 	 */
 	@Override
 	public IMergeViewerItem getInitialItem() {
 		IMergeViewerItem ret = null;
 		Iterable<? extends IMergeViewerItem> items = getAllItems();
 		for (IMergeViewerItem item : items) {
 			Diff diff = item.getDiff();
 			if (diff == getInitialDiff()) {
 				ret = item;
 				break;
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.mergeviewer.accessor.ICompareAccessor#getItems()
 	 */
 	public ImmutableList<? extends IMergeViewerItem> getItems() {
 		final ImmutableList<? extends IMergeViewerItem> ret;
 
 		if (MergeViewerUtil.getEObject(fRootMatch, getSide()) == null) {
 			Diff diff = null;
 			if (getSide() != MergeViewerSide.ANCESTOR) {
 				diff = getDiffWithValue(MergeViewerUtil.getEObject(fRootMatch, getSide()));
 				if (diff == null) {
 					diff = getDiffWithValue(MergeViewerUtil.getEObject(fRootMatch, getSide().opposite()));
 				}
 			} else {
 				diff = getDiffWithValue(MergeViewerUtil.getEObject(fRootMatch, MergeViewerSide.LEFT));
 				if (diff == null) {
 					diff = getDiffWithValue(MergeViewerUtil.getEObject(fRootMatch, MergeViewerSide.RIGHT));
 				}
 			}
 			ret = ImmutableList.of(new MergeViewerItem.Container(getComparison(), diff, fRootMatch.getLeft(),
 					fRootMatch.getRight(), fRootMatch.getOrigin(), getSide(), getAdapterFactory()));
 		} else {
 			ret = ImmutableList.of(new MergeViewerItem.Container(getComparison(), null, fRootMatch,
 					getSide(), getAdapterFactory()));
 		}
 
 		return ret;
 	}
 
 	protected Iterable<? extends IMergeViewerItem> getAllItems() {
 		return concat(transform(getItems(), SELF_AND_ALL_ITEMS));
 
 	}
 
 	private static final Function<IMergeViewerItem, Iterable<? extends IMergeViewerItem>> SELF_AND_ALL_ITEMS = new Function<IMergeViewerItem, Iterable<? extends IMergeViewerItem>>() {
 		public Iterable<? extends IMergeViewerItem> apply(IMergeViewerItem item) {
 			if (item instanceof IMergeViewerItem.Container) {
 				List<IMergeViewerItem> children = Arrays.asList(((IMergeViewerItem.Container)item)
 						.getChildren());
 				return concat(ImmutableList.of(item), concat(transform(children, SELF_AND_ALL_ITEMS)));
 			} else {
 				return ImmutableList.of(item);
 			}
 		}
 	};
 
 	/**
 	 * 
 	 */
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
 
 	/**
 	 * Returns the values of the current feature on the given side.
 	 * 
 	 * @param side
 	 * @return
 	 */
 	protected List<?> getFeatureValues(EStructuralFeature feature, MergeViewerSide side) {
 		final EObject eObject = getEObject(side);
 		return ReferenceUtil.getAsList(eObject, feature);
 	}
 
 	/**
 	 * @param diff
 	 * @param side
 	 * @return
 	 */
 	protected Object getValueFromDiff(final Diff diff, MergeViewerSide side) {
 		Object diffValue = getDiffValue(diff);
 		EStructuralFeature affectedFeature = getAffectedFeature(diff);
 		Object ret = matchingValue(diffValue, affectedFeature, side);
 		return ret;
 	}
 
 	private Object matchingValue(Object object, EStructuralFeature feature, MergeViewerSide side) {
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
 				ret = matchingValue(object, getFeatureValues(feature, side));
 			}
 		} else {
 			ret = matchingValue(object, getFeatureValues(feature, side));
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
 
 	/**
 	 * Returns either {@link ReferenceChange#getValue()} or {@link AttributeChange#getValue()} depending on
 	 * the runtime type of the give {@code diff} or null otherwise.
 	 * 
 	 * @param diff
 	 * @return
 	 */
 	protected Object getDiffValue(Diff diff) {
 		final Object ret;
 		if (diff instanceof ReferenceChange) {
 			ret = ((ReferenceChange)diff).getValue();
 		} else if (diff instanceof AttributeChange) {
 			ret = ((AttributeChange)diff).getValue();
 		} else {
 			ret = null;
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the structural feature affected by the given diff, if any.
 	 * 
 	 * @param diff
 	 *            The diff from which we need to retrieve a feature.
 	 * @return The feature affected by this {@code diff}, if any. <code>null</code> if none.
 	 */
 	@Override
 	protected EStructuralFeature getAffectedFeature(Diff diff) {
 		final EStructuralFeature feature;
 		if (diff instanceof ReferenceChange) {
 			feature = ((ReferenceChange)diff).getReference();
 		} else if (diff instanceof AttributeChange) {
 			feature = ((AttributeChange)diff).getAttribute();
 		} else {
 			feature = null;
 		}
 		return feature;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.contentmergeviewer.accessor.legacy.ITypedElement#getType()
 	 */
 	@Override
 	public String getType() {
 		return TypeConstants.TYPE__ETREE_DIFF;
 	}
 }
