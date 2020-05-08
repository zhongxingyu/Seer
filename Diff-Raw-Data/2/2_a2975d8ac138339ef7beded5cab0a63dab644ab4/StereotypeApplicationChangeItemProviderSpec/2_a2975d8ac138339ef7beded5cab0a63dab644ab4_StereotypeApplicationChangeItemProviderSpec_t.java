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
 package org.eclipse.emf.compare.uml2.provider.spec;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.uml2.StereotypeApplicationChange;
 import org.eclipse.emf.compare.uml2.UMLDiff;
 import org.eclipse.emf.compare.uml2.provider.StereotypeApplicationChangeItemProvider;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Stereotype;
 import org.eclipse.uml2.uml.util.UMLUtil;
 
 /**
  * Specifialized StereotypeApplicationChangeItemProvider.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class StereotypeApplicationChangeItemProviderSpec extends StereotypeApplicationChangeItemProvider {
 
 	/**
 	 * This constructs an instance from a factory and a notifier.
 	 * 
 	 * @param adapterFactory
 	 *            the factory to delegate to.
 	 */
 	public StereotypeApplicationChangeItemProviderSpec(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Collection<?> getChildren(Object object) {
 		final Collection<?> ret;
 		UMLDiff umlDiff = (UMLDiff)object;
 		if (umlDiff.getKind() == DifferenceKind.CHANGE) {
 			ret = umlDiff.getRefinedBy();
 		} else {
 			ret = super.getChildren(object);
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.uml2.internal.utils.ForwardingItemProvider#hasChildren(java.lang.Object)
 	 */
 	@Override
 	public boolean hasChildren(Object object) {
 		return !getChildren(object).isEmpty();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.uml2.internal.utils.ForwardingItemProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object object) {
 		final UMLDiff umlDiff = (UMLDiff)object;
 
 		String remotely = "";
 		if (umlDiff.getSource() == DifferenceSource.RIGHT) {
 			remotely = "remotely ";
 		}
 
 		Stereotype stereotype = ((StereotypeApplicationChange)umlDiff).getStereotype();
 		if (stereotype == null) {
 			stereotype = UMLUtil.getStereotype(umlDiff.getDiscriminant());
 		}
 
 		final String stereotypeText;
 		if (stereotype != null) {
 			stereotypeText = ForwardingUMLDiffItemProvider.getText(getRootAdapterFactory(), stereotype) + ' ';
 		} else if (umlDiff.getDiscriminant() instanceof NamedElement) {
 			stereotypeText = "Stereotype " + ((NamedElement)umlDiff.getDiscriminant()).getName() + ' ';
 		} else {
 			// Can't really do more
			stereotypeText = "Stereotype ";
 		}
 
 		final Match targetMatch = umlDiff.getMatch();
 		final EObject target = findNonNullSide(targetMatch);
 		String targetLabel = null;
 
 		final String action;
 		switch (umlDiff.getKind()) {
 			case ADD:
 				action = "applied";
 				targetLabel = " to ";
 				break;
 			case DELETE:
 				action = "unapplied";
 				targetLabel = " from ";
 				break;
 			case CHANGE:
 				action = "changed";
 				targetLabel = " on ";
 				break;
 			case MOVE:
 				action = "moved";
 				targetLabel = " to ";
 				break;
 			default:
 				throw new IllegalStateException("Unsupported " + DifferenceKind.class.getSimpleName()
 						+ " value: " + umlDiff.getKind());
 		}
 
 		if (target != null) {
 			targetLabel += ForwardingUMLDiffItemProvider.getText(getRootAdapterFactory(), target);
 		}
 
 		return stereotypeText + "has been " + remotely + action + targetLabel;
 	}
 
 	/**
 	 * Tries and find a non-<code>null</code> side for the given match. Still returns <code>null</code> if all
 	 * three sides are <code>null</code> though.
 	 * 
 	 * @param match
 	 *            The match for which we need a non-<code>null</code> side.
 	 * @return The first side (in order : left, right or origin) that was not <code>null</code>.
 	 */
 	private EObject findNonNullSide(Match match) {
 		final EObject side;
 		if (match.getLeft() != null) {
 			side = match.getLeft();
 		} else if (match.getRight() != null) {
 			side = match.getRight();
 		} else {
 			side = match.getOrigin();
 		}
 		return side;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.provider.ReferenceChangeItemProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Object getImage(Object object) {
 		final UMLDiff umlDiff = (UMLDiff)object;
 
 		Stereotype stereotype = ((StereotypeApplicationChange)umlDiff).getStereotype();
 		if (stereotype == null) {
 			stereotype = UMLUtil.getStereotype(umlDiff.getDiscriminant());
 		}
 
 		Object image = ForwardingUMLDiffItemProvider.getImage(getRootAdapterFactory(), stereotype);
 		return image;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProviderAdapter#getForeground(java.lang.Object)
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		StereotypeApplicationChange referenceChange = (StereotypeApplicationChange)object;
 		switch (referenceChange.getState()) {
 			case MERGED:
 			case DISCARDED:
 				return URI.createURI("color://rgb/156/156/156"); //$NON-NLS-1$
 			default:
 				return super.getForeground(object);
 		}
 	}
 }
