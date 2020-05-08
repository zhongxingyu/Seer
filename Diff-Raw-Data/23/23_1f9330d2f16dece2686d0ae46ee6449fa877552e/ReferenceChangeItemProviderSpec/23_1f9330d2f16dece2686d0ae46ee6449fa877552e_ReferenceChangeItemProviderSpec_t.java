 /*******************************************************************************
  * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.provider.spec;
 
 import static com.google.common.base.Predicates.instanceOf;
 import static com.google.common.base.Predicates.not;
 import static com.google.common.collect.Iterables.any;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableCollection;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ConflictKind;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.provider.AdapterFactoryUtil;
 import org.eclipse.emf.compare.provider.IItemDescriptionProvider;
 import org.eclipse.emf.compare.provider.IItemStyledLabelProvider;
 import org.eclipse.emf.compare.provider.ReferenceChangeItemProvider;
 import org.eclipse.emf.compare.provider.utils.ComposedStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString;
 import org.eclipse.emf.compare.provider.utils.IStyledString.Style;
 import org.eclipse.emf.compare.utils.EMFComparePredicates;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.edit.provider.IItemFontProvider;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 /**
  * Specialized {@link ReferenceChangeItemProvider} returning nice output for {@link #getText(Object)} and
  * {@link #getImage(Object)}.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ReferenceChangeItemProviderSpec extends ReferenceChangeItemProvider implements IItemStyledLabelProvider, IItemDescriptionProvider {
 
 	/** The elide length. */
 	private static final int ELIDE_LENGTH = 50;
 
 	/** The image provider used with this item provider. */
 	private final OverlayImageProvider overlayProvider;
 
 	/**
 	 * Constructor calling super {@link #ReferenceChangeItemProvider(AdapterFactory)}.
 	 * 
 	 * @param adapterFactory
 	 *            the adapter factory
 	 */
 	public ReferenceChangeItemProviderSpec(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 		overlayProvider = new OverlayImageProvider(getResourceLocator());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ReferenceChangeItemProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object object) {
 		return getStyledText(object).getString();
 	}
 
 	static String changeText(final Diff diff, EStructuralFeature feature) {
 		DifferenceSource source = diff.getSource();
 		Match matchOfInterrest = diff.getMatch();
 		final EObject sourceSide;
 		final EObject otherSide;
 		if (source == DifferenceSource.LEFT) {
 			sourceSide = matchOfInterrest.getLeft();
 			otherSide = matchOfInterrest.getRight();
 		} else { // source == DifferenceSource.RIGHT
 			sourceSide = matchOfInterrest.getRight();
 			otherSide = matchOfInterrest.getLeft();
 		}
 		String changeText = changeText(feature, sourceSide, otherSide);
 		return changeText;
 	}
 
 	/**
 	 * Returns the type of change linked to the given {@link EStructuralFeature} ("unset", "set" or
 	 * "changed"), according the the given sides.
 	 * 
 	 * @param eStructuralFeature
 	 *            the given {@link EStructuralFeature}.
 	 * @param sourceSide
 	 *            the source side as an {@link EObject}.
 	 * @param otherSide
 	 *            the other side as an {@link EObject}.
 	 * @return a String ("unset", "set" or "changed") containing the type of change linked to the given
 	 *         {@link EStructuralFeature}, according the the given sides.
 	 */
 	private static String changeText(final EStructuralFeature eStructuralFeature, EObject sourceSide,
 			EObject otherSide) {
 		String changeText;
 		if (sourceSide != null) {
 			Object leftValue = ReferenceUtil.safeEGet(sourceSide, eStructuralFeature);
 			if (leftValue == null || isStringAndNullOrEmpty(leftValue)) {
 				changeText = "unset";
 			} else if (otherSide != null) {
 				Object otherValue = ReferenceUtil.safeEGet(otherSide, eStructuralFeature);
 				if (otherValue == null || isStringAndNullOrEmpty(otherValue)) {
 					changeText = "set";
 				} else {
 					changeText = "changed";
 				}
 			} else {
 				changeText = "set";
 			}
 		} else {
 			changeText = "unset";
 		}
 		return changeText;
 	}
 
 	/**
 	 * Checks if the given Object is a null or empty String.
 	 * 
 	 * @param s
 	 *            the given Object.
 	 * @return true if the Object is a null or empty String, false otherwise.
 	 */
 	private static boolean isStringAndNullOrEmpty(Object s) {
 		if (s instanceof String) {
 			return Strings.isNullOrEmpty((String)s);
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Returns the name of the reference linked to the given {@link ReferenceChange}.
 	 * 
 	 * @param refChange
 	 *            the given {@link ReferenceChange}.
 	 * @return the name of the reference linked to the given {@link ReferenceChange}.
 	 */
 	protected String getReferenceText(final ReferenceChange refChange) {
 		return refChange.getReference().getName();
 	}
 
 	/**
 	 * Converts to text the given {@link ReferenceChange}.
 	 * 
 	 * @param refChange
 	 *            the given {@link ReferenceChange}.
 	 * @return a nice text from the the given {@link ReferenceChange}.
 	 */
 	protected String getValueText(final ReferenceChange refChange) {
 		EObject refChangeValue = refChange.getValue();
 		String value = AdapterFactoryUtil.getText(getRootAdapterFactory(), refChangeValue);
 		if (Strings.isNullOrEmpty(value)) {
 			if (refChangeValue.eIsProxy()) {
 				value = "proxy : " + ((InternalEObject)refChangeValue).eProxyURI().toString();
 			} else {
 				value = "<null>"; //$NON-NLS-1$
 			}
 		} else {
 			value = org.eclipse.emf.compare.provider.spec.Strings.elide(value, ELIDE_LENGTH, "..."); //$NON-NLS-1$
 		}
 		return value;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ReferenceChangeItemProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Object getImage(Object object) {
 		ReferenceChange refChange = (ReferenceChange)object;
 
 		Object refChangeValueImage = AdapterFactoryUtil.getImage(getRootAdapterFactory(), refChange
 				.getValue());
 
 		Object diffImage = overlayProvider.getComposedImage(refChange, refChangeValueImage);
 		Object ret = overlayImage(object, diffImage);
 
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Collection<?> getChildren(Object object) {
 		Collection<?> superChildren = super.getChildren(object);
 		List<? super Object> ret = newArrayList(superChildren);
 
 		ReferenceChange referenceChange = (ReferenceChange)object;
 		EReference reference = referenceChange.getReference();
 
 		if (reference.isContainment()) {
 			Match matchOfValue = referenceChange.getMatch().getComparison().getMatch(
 					referenceChange.getValue());
			if (matchOfValue == null && DifferenceState.MERGED == referenceChange.getState()) {
				Match parentMatch = referenceChange.getMatch();
				matchOfValue = getMatchWithNullValues(parentMatch);
			}
 			if (matchOfValue != null) {
 				Collection<?> children = getChildren(matchOfValue);
 				children.remove(referenceChange);
 				Iterable<?> filter = filter(children, fromSideOrInRealConflict(((Diff)object).getSource()));
 				Iterables.addAll(ret, filter);
 			}
 		}
 
 		return ImmutableList.copyOf(filter(ret, not(instanceOf(ResourceAttachmentChange.class))));
 
 	}
 
 	/**
	 * After merging a diff which will lead to have an insertion point on both sides, the match associated
	 * with this diff will be unreacheable because its left and right sides will be null. This method will
	 * find this match.
	 * 
	 * @param match
	 *            the given match.
	 * @return the match associated with the given merged diff.
	 */
	private Match getMatchWithNullValues(Match match) {
		for (Match subMatch : match.getSubmatches()) {
			if (subMatch.getLeft() == null && subMatch.getRight() == null) {
				return subMatch;
			}
		}
		return null;
	}

	/**
 	 * This can be used to check that a given Object originates from the given {@code source} side, and is not
 	 * in real conflict.
 	 * 
 	 * @param source
 	 *            The side from which we expect this diff to originate.
 	 * @return The created predicate.
 	 */
 	private Predicate<? super Object> fromSideOrInRealConflict(final DifferenceSource source) {
 		return new Predicate<Object>() {
 			public boolean apply(Object object) {
 				boolean ret = false;
 				if (object instanceof Diff) {
 					Diff diff = (Diff)object;
 					if (source == diff.getSource()) {
 						ret = true;
 					} else if (EMFComparePredicates.hasConflict(ConflictKind.REAL).apply(diff)) {
 						ret = true;
 					} else if (EMFComparePredicates.hasConflict(ConflictKind.PSEUDO).apply(diff)) {
 						ret = !(diff instanceof ReferenceChange)
 								|| (diff instanceof ReferenceChange && ((ReferenceChange)diff).getReference()
 										.isContainment());
 					} else {
 						ret = true;
 					}
 				} else if (object instanceof Match) {
 					ret = true;
 				}
 				return ret;
 			}
 		};
 	}
 
 	/**
 	 * Returns the children of the given {@link Match}.
 	 * 
 	 * @param matchOfValue
 	 *            the given {@link Match}.
 	 * @return the children of the given {@link Match}.
 	 */
 	private Collection<?> getChildren(Match matchOfValue) {
 		final Collection<?> children;
 		ITreeItemContentProvider matchItemContentProvider = (ITreeItemContentProvider)adapterFactory.adapt(
 				matchOfValue, ITreeItemContentProvider.class);
 		if (matchItemContentProvider != null) {
 			Collection<?> itemProviderChildren = matchItemContentProvider.getChildren(matchOfValue);
 			if (itemProviderChildren instanceof ImmutableCollection<?>) {
 				children = newArrayList(itemProviderChildren);
 			} else {
 				children = itemProviderChildren;
 			}
 
 			Iterator<?> childrenIterator = children.iterator();
 			while (childrenIterator.hasNext()) {
 				Object child = childrenIterator.next();
 				if (child instanceof Match) {
 					if (!matchItemContentProvider.hasChildren(child)) {
 						childrenIterator.remove();
 					}
 				}
 
 			}
 		} else {
 			children = ImmutableList.of();
 		}
 		return children;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getForeground(java.lang.Object)
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		ReferenceChange referenceChange = (ReferenceChange)object;
 		switch (referenceChange.getState()) {
 			case MERGED:
 			case DISCARDED:
 				return URI.createURI("color://rgb/156/156/156"); //$NON-NLS-1$
 			default:
 				return super.getForeground(object);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemStyledLabelProvider#getStyledText(java.lang.Object)
 	 */
 	public IStyledString.IComposedStyledString getStyledText(Object object) {
 		final ReferenceChange refChange = (ReferenceChange)object;
 
 		final String valueText = getValueText(refChange);
 
 		final String referenceText = getReferenceText(refChange);
 
 		ComposedStyledString ret = new ComposedStyledString();
 
 		if (refChange.getReference().isContainment()) {
 			EObject value = refChange.getValue();
 			Match match = refChange.getMatch().getComparison().getMatch(value);
 			if (match != null) {
 				Iterable<Diff> subDifferences = match.getAllDifferences();
 				if (refChange.getState() != DifferenceState.UNRESOLVED
 						&& any(subDifferences, EMFComparePredicates.hasState(DifferenceState.UNRESOLVED))) {
 					ret.append("> ", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 				}
 			}
 		}
 		final EObject refChangeValue = refChange.getValue();
 		if (refChangeValue.eIsProxy()) {
 			Style italic = Style.builder().setFont(IItemFontProvider.ITALIC_FONT).build();
 			ret.append(valueText, italic);
 		} else {
 			ret.append(valueText);
 		}
 		ret.append(" [" + referenceText, Style.DECORATIONS_STYLER); //$NON-NLS-1$
 
 		switch (refChange.getKind()) {
 			case ADD:
 				ret.append(" add", Style.DECORATIONS_STYLER);
 				break;
 			case DELETE:
 				ret.append(" delete", Style.DECORATIONS_STYLER);
 				break;
 			case CHANGE:
 				ret.append(" " + changeText(refChange, refChange.getReference()), Style.DECORATIONS_STYLER);
 				break;
 			case MOVE:
 				ret.append(" move", Style.DECORATIONS_STYLER);
 				break;
 			default:
 				throw new IllegalStateException("Unsupported " + DifferenceKind.class.getSimpleName()
 						+ " value: " + refChange.getKind());
 		}
 		ret.append("]", Style.DECORATIONS_STYLER); //$NON-NLS-1$
 
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.IItemDescriptionProvider#getDescription(java.lang.Object)
 	 */
 	public String getDescription(Object object) {
 		final ReferenceChange refChange = (ReferenceChange)object;
 
 		final String valueText = getValueText(refChange);
 		final String referenceText = getReferenceText(refChange);
 
 		String remotely = ""; //$NON-NLS-1$
 		if (refChange.getSource() == DifferenceSource.RIGHT) {
 			remotely = "remotely "; //$NON-NLS-1$
 		}
 
 		String ret = ""; //$NON-NLS-1$
 		final String hasBeen = " has been "; //$NON-NLS-1$
 
 		switch (refChange.getKind()) {
 			case ADD:
 				ret = valueText + hasBeen + remotely + "added to " + referenceText; //$NON-NLS-1$ 
 				break;
 			case DELETE:
 				ret = valueText + hasBeen + remotely + "deleted from " + referenceText; //$NON-NLS-1$ 
 				break;
 			case CHANGE:
 				String changeText = changeText(refChange, refChange.getReference());
 				ret = referenceText + " " + valueText + hasBeen + remotely + changeText; //$NON-NLS-1$ 
 				break;
 			case MOVE:
 				ret = valueText + hasBeen + remotely + "moved in " + referenceText; //$NON-NLS-1$ 
 				break;
 			default:
 				throw new IllegalStateException("Unsupported " + DifferenceKind.class.getSimpleName() //$NON-NLS-1$
 						+ " value: " + refChange.getKind()); //$NON-NLS-1$
 		}
 
 		return ret;
 	}
 }
