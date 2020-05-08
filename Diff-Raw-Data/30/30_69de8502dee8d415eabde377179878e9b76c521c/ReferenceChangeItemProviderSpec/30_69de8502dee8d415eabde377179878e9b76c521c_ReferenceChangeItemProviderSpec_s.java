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
 package org.eclipse.emf.compare.provider.spec;
 
 import static com.google.common.base.Predicates.not;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 
 import com.google.common.collect.ImmutableCollection;
 import com.google.common.collect.ImmutableList;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.provider.ReferenceChangeItemProvider;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
 
 /**
  * Specialized {@link ReferenceChangeItemProvider} returning nice output for {@link #getText(Object)} and
  * {@link #getImage(Object)}.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ReferenceChangeItemProviderSpec extends ReferenceChangeItemProvider {
 
 	/**
 	 * Constructor calling super {@link #ReferenceChangeItemProvider(AdapterFactory)}.
 	 * 
 	 * @param adapterFactory
 	 *            the adapter factory
 	 */
 	public ReferenceChangeItemProviderSpec(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ReferenceChangeItemProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object object) {
 		final ReferenceChange refChange = (ReferenceChange)object;
 
 		final String valueText = getValueText(refChange);
 		final String referenceText = getReferenceText(refChange);
 
 		String remotely = ""; //$NON-NLS-1$
 		if (refChange.getSource() == DifferenceSource.RIGHT) {
 			remotely = "remotely "; //$NON-NLS-1$
 		}
 
 		String ret = ""; //$NON-NLS-1$
 		switch (refChange.getKind()) {
 			case ADD:
 				ret = valueText + " has been " + remotely + "added to " + referenceText; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			case DELETE:
 				ret = valueText + " has been " + remotely + "deleted from " + referenceText; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			case CHANGE:
 				String changeText = changeText(refChange, refChange.getReference());
 				ret = referenceText + " " + valueText + " has been " + remotely + changeText; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			case MOVE:
 				ret = valueText + " has been " + remotely + "moved in " + referenceText; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				throw new IllegalStateException("Unsupported " + DifferenceKind.class.getSimpleName() //$NON-NLS-1$
 						+ " value: " + refChange.getKind()); //$NON-NLS-1$
 		}
 
 		return ret;
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
 
 	private static String changeText(final EStructuralFeature eStructuralFeature, EObject sourceSide,
 			EObject otherSide) {
 		String changeText;
 		if (sourceSide != null) {
 			Object leftValue = sourceSide.eGet(eStructuralFeature);
			if (leftValue == null) {
 				changeText = "unset"; //$NON-NLS-1$
			} else {
 				Object otherValue = otherSide.eGet(eStructuralFeature);
				if (otherValue != null) {
					changeText = "changed"; //$NON-NLS-1$
				} else {
 					changeText = "set"; //$NON-NLS-1$
 				}
 			}
 		} else {
 			changeText = "unset"; //$NON-NLS-1$
 		}
 		return changeText;
 	}
 
 	protected String getReferenceText(final ReferenceChange refChange) {
 		return refChange.getReference().getName();
 	}
 
 	protected String getValueText(final ReferenceChange refChange) {
 		String value = CompareItemProviderAdapterFactorySpec.getText(getRootAdapterFactory(), refChange
 				.getValue());
 		if (value == null) {
 			value = "<null>"; //$NON-NLS-1$
 		} else {
			value = Strings.elide(value, 32, "..."); //$NON-NLS-1$
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
 
 		Object image = CompareItemProviderAdapterFactorySpec.getImage(getRootAdapterFactory(), refChange
 				.getValue());
 
 		return image;
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
 			if (matchOfValue != null) {
 				Collection<?> children = getChildren(matchOfValue);
 				children.remove(referenceChange);
 				ret.addAll(children);
 			}
 		}
 
 		return ImmutableList.copyOf(filter(ret,
 				not(MatchItemProviderSpec.REFINED_OR_REQUIRED_BY_REFINED_DIFF)));
 
 	}
 
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
 
 }
