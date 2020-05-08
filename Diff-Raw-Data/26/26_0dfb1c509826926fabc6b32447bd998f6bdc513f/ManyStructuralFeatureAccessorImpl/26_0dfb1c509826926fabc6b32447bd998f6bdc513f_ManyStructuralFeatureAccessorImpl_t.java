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
 package org.eclipse.emf.compare.rcp.ui.mergeviewer.accessor;
 
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.size;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Lists.newArrayListWithCapacity;
 
 import com.google.common.collect.ImmutableList;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.IMergeViewerItem;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.InsertionPoint;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MatchedObject;
 import org.eclipse.emf.compare.rcp.ui.mergeviewer.MergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ManyStructuralFeatureAccessorImpl extends BasicStructuralFeatureAccessorImpl {
 
 	/**
 	 * @param diff
 	 * @param side
 	 */
 	public ManyStructuralFeatureAccessorImpl(Diff diff, MergeViewerSide side) {
 		super(diff, side);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.BasicStructuralFeatureAccessorImpl#getItems()
 	 */
 	public ImmutableList<? extends IMergeViewerItem> getItems() {
 		List<? extends IMergeViewerItem> ret;
 		List<?> list = getFeatureValues(getSide());
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
 						&& (left == null || !getFeatureValues(getSide()).contains(left));
 				final boolean rightEmptyBox = getSide() == MergeViewerSide.RIGHT
 						&& (right == null || !getFeatureValues(getSide()).contains(right));
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
 		return DiffUtil.findInsertionIndex(getComparison(), diff, rightToLeft);
 	}
 
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
 	 * @param diff
 	 * @param side
 	 * @return
 	 */
	protected Object getValueFromDiff(final Diff diff, MergeViewerSide side) {
 		Object diffValue = getDiffValue(diff);
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
 				ret = matchingValue(object, getFeatureValues(side));
 			}
 		} else {
 			ret = matchingValue(object, getFeatureValues(side));
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
 	 * Returns the values of the current feature on the given side.
 	 * 
 	 * @param side
 	 * @return
 	 */
 	protected List<?> getFeatureValues(MergeViewerSide side) {
 		final EObject eObject = getEObject(side);
 		return getAsList(eObject, getStructuralFeature());
 	}
 
 	/**
 	 * This utility simply allows us to retrieve the value of a given feature as a List.
 	 * 
 	 * @param object
 	 *            The object for which feature we need a value.
 	 * @param feature
 	 *            The actual feature of which we need the value.
 	 * @return The value of the given <code>feature</code> for the given <code>object</code> as a list. An
 	 *         empty list if this object has no value for that feature or if the object is <code>null</code>.
 	 */
 	private static List<?> getAsList(EObject object, EStructuralFeature feature) {
 		final List<?> asList;
 		if (object != null) {
 			Object value = ReferenceUtil.safeEGet(object, feature);
 			if (value instanceof InternalEList<?>) {
 				// EMF ignores the "resolve" flag for containment lists...
 				asList = newArrayList(((InternalEList<?>)value).basicList());
 			} else if (value instanceof List) {
 				asList = newArrayList((List<?>)value);
 			} else if (value instanceof Iterable) {
 				asList = newArrayList((Iterable<?>)value);
 			} else if (value != null) {
 				asList = newArrayList(value);
 			} else {
 				asList = newArrayList();
 			}
 		} else {
 			asList = newArrayList();
 		}
 		return asList;
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
 }
