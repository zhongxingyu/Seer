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
 package org.eclipse.emf.compare.rcp.ui.internal.util;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.MatchResource;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.rcp.ui.internal.mergeviewer.IMergeViewer.MergeViewerSide;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public final class MergeViewerUtil {
 
 	private MergeViewerUtil() {
 	}
 
 	public static Diff getDiffWithValue(Iterable<Diff> differences, Object value, MergeViewerSide side) {
 		Diff ret = null;
 		for (Diff diff : differences) {
 			Object valueOfDiff = getValueFromDiff(diff, side);
 			if (valueOfDiff == value) {
 				ret = diff;
 				break;
 			}
 		}
 		return ret;
 	}
 
 	public static List<?> getValues(Diff diff, MergeViewerSide side) {
 		Match ownerMatch = diff.getMatch();
 		EObject eObject = getEObject(ownerMatch, side);
 		EStructuralFeature affectedFeature = getAffectedFeature(diff);
 		return ReferenceUtil.getAsList(eObject, affectedFeature);
 	}
 
 	public static List<?> getFeatureValues(Match match, EStructuralFeature feature, MergeViewerSide side) {
 		final EObject eObject = getEObject(match, side);
 		return ReferenceUtil.getAsList(eObject, feature);
 	}
 
 	public static EObject getEObject(Match match, MergeViewerSide side) {
 		final EObject eObject;
 		switch (side) {
 			case ANCESTOR:
 				eObject = match.getOrigin();
 				break;
 			case LEFT:
 				eObject = match.getLeft();
 				break;
 			case RIGHT:
 				eObject = match.getRight();
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 		return eObject;
 	}
 
 	public static EStructuralFeature getAffectedFeature(Diff diff) {
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
 	 * Returns either {@link ReferenceChange#getValue()} or {@link AttributeChange#getValue()} depending on
 	 * the runtime type of the give {@code diff} or null otherwise.
 	 * 
 	 * @param diff
 	 * @return
 	 */
 	public static Object getDiffValue(Diff diff) {
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
 
 	public static Object getValueFromDiff(final Diff diff, MergeViewerSide side) {
 		Object diffValue = getDiffValue(diff);
 		EStructuralFeature feature = getAffectedFeature(diff);
 		Match ownerMatch = diff.getMatch();
 		Object ret = matchingValue(diffValue, ownerMatch, feature, side, ownerMatch.getComparison());
 		return ret;
 	}
 
 	private static Object matchingValue(Object object, Match ownerMatch, EStructuralFeature feature,
 			MergeViewerSide side, Comparison comparison) {
 		final Object ret;
 		if (object instanceof EObject) {
 			final Match matchOfValue = comparison.getMatch((EObject)object);
 			if (matchOfValue != null) {
 				ret = getEObject(matchOfValue, side);
 			} else {
 				ret = matchingValue(object, getFeatureValues(ownerMatch, feature, side), comparison);
 			}
 		} else {
 			ret = matchingValue(object, getFeatureValues(ownerMatch, feature, side), comparison);
 		}
 		return ret;
 	}
 
 	private static Object matchingValue(Object value, List<?> in, Comparison comparison) {
 		Object ret = null;
 		IEqualityHelper equalityHelper = comparison.getEqualityHelper();
 		Iterator<?> valuesIterator = in.iterator();
 		while (valuesIterator.hasNext() && ret == null) {
 			Object object = valuesIterator.next();
 			if (equalityHelper.matchingValues(object, value)) {
 				ret = object;
 			}
 		}
 		return ret;
 	}
 
 	public static Object getParent(Object object, AdapterFactory adapterFactory) {
 		ITreeItemContentProvider treeItemContentProvider = (ITreeItemContentProvider)adapterFactory.adapt(
 				object, ITreeItemContentProvider.class);
 
 		return treeItemContentProvider != null ? treeItemContentProvider.getParent(object) : null;
 	}
 
 	public static Object[] getChildren(Object object, AdapterFactory adapterFactory) {
 		ITreeItemContentProvider treeItemContentProvider = (ITreeItemContentProvider)adapterFactory.adapt(
 				object, ITreeItemContentProvider.class);
 
 		return (treeItemContentProvider != null ? treeItemContentProvider.getChildren(object)
 				: Collections.EMPTY_LIST).toArray();
 	}
 
 	/**
 	 * Returns the current resource on the given side of the given comparison.
 	 * 
 	 * @param comparison
 	 *            The given comparison.
 	 * @param side
 	 *            The given side.
 	 * @return The current resource on the given side of the given comparison.
 	 */
 	public static Resource getResource(Comparison comparison, MergeViewerSide side, Diff diff) {
 		Resource resource = null;
 		Collection<MatchResource> matchResources = comparison.getMatchedResources();
 		String diffResourceURI = null;
 		if (diff != null) {
 			diffResourceURI = ((ResourceAttachmentChange)diff).getResourceURI();
 		}
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
 			if (resource != null && diffResourceURI != null) {
 				URI resourceURI = resource.getURI();
 				if (diffResourceURI.equals(resourceURI.toString())) {
 					return resource;
 				} else if (side == MergeViewerSide.RIGHT
						&& diffResourceURI.equals(matchResource.getLeftURI())) {
 					return resource;
 				} else if (side == MergeViewerSide.LEFT
						&& diffResourceURI.equals(matchResource.getRightURI())) {
 					return resource;
 				} else if (side == MergeViewerSide.ANCESTOR
						&& diffResourceURI.equals(matchResource.getOriginURI())) {
 					return resource;
 				}
 			}
 		}
 		return resource;
 	}
 
 	/**
 	 * Returns the contents of the current resource on the given side of the given comparison.
 	 * 
 	 * @param comparison
 	 *            The given comparison.
 	 * @param side
 	 *            The given side.
 	 * @return The contents of the current resource on the given side of the given comparison.
 	 */
 	public static List<EObject> getResourceContents(Comparison comparison, MergeViewerSide side, Diff diff) {
 		Resource resource = getResource(comparison, side, diff);
 		if (resource != null) {
 			return resource.getContents();
 		}
 		return Collections.emptyList();
 
 	}
 
 	/**
 	 * @param diff
 	 * @param side
 	 * @return
 	 */
 	public static Object getResourceAttachmentChangeValue(ResourceAttachmentChange diff, MergeViewerSide side) {
 		final Object ret;
 		Match match = diff.getMatch();
 		switch (side) {
 			case ANCESTOR:
 				ret = match.getOrigin();
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
 		return ret;
 	}
 
 	public static Object getValueFromResourceAttachmentChange(final ResourceAttachmentChange diff,
 			Comparison comparison, MergeViewerSide side) {
 		Object diffValue = getResourceAttachmentChangeValue(diff, side);
 		Object ret = matchingValue(diffValue, comparison, side);
 		return ret;
 	}
 
 	public static Object matchingValue(Object object, Comparison comparison, MergeViewerSide side) {
 		final Object ret;
 		if (object instanceof EObject) {
 			final Match matchOfValue = comparison.getMatch((EObject)object);
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
 				ret = matchingValue(object, comparison, getResourceContents(comparison, side,
 						(ResourceAttachmentChange)object));
 			}
 		} else {
 			ret = matchingValue(object, comparison, getResourceContents(comparison, side, null));
 		}
 		return ret;
 	}
 
 	public static Object matchingValue(Object value, Comparison comparison, List<?> in) {
 		Object ret = null;
 		IEqualityHelper equalityHelper = comparison.getEqualityHelper();
 		Iterator<?> valuesIterator = in.iterator();
 		while (valuesIterator.hasNext() && ret == null) {
 			Object object = valuesIterator.next();
 			if (equalityHelper.matchingValues(object, value)) {
 				ret = object;
 			}
 		}
 		return ret;
 	}
 }
