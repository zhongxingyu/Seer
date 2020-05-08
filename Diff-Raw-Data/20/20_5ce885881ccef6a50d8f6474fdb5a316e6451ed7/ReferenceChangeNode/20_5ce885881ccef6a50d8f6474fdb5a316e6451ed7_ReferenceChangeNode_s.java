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
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.ManyReferenceChangeNode;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 
 /**
  * Specific AbstractEDiffNode for {@link ReferenceChange} objects.
  * 
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class ReferenceChangeNode extends DiffNode {
 
 	/**
 	 * Creates a node with the given factory.
 	 * 
 	 * @param adapterFactory
 	 *            the factory given to the super constructor.
 	 */
 	public ReferenceChangeNode(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider.DiffNode#getTarget()
 	 */
 	@Override
 	public ReferenceChange getTarget() {
 		return (ReferenceChange)super.getTarget();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffNode#getAncestor()
 	 */
 	@Override
 	public ITypedElement getAncestor() {
 		ITypedElement ret = null;
 		final EReference reference = getTarget().getReference();
 		final Match match = getTarget().getMatch();
 		final EObject origin = match.getOrigin();
		final EObject value = match.getComparison().getMatch(getTarget().getValue()).getOrigin();
 		if (origin != null) {
 			if (reference.isMany()) {
 				ret = new ManyReferenceChangeNode(origin, reference, value);
 			} else {
 				// todo
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffNode#getLeft()
 	 */
 	@Override
 	public ITypedElement getLeft() {
 		ITypedElement ret = null;
 		final EReference reference = getTarget().getReference();
 		final Match match = getTarget().getMatch();
 		final EObject left = match.getLeft();
		final EObject value = match.getComparison().getMatch(getTarget().getValue()).getLeft();
 		if (left != null) {
 			if (reference.isMany()) {
 				ret = new ManyReferenceChangeNode(left, reference, value);
 			} else {
 				// todo
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractEDiffNode#getRight()
 	 */
 	@Override
 	public ITypedElement getRight() {
 		ITypedElement ret = null;
 		EReference reference = getTarget().getReference();
 		final Match match = getTarget().getMatch();
 		EObject right = match.getRight();
		EObject value = match.getComparison().getMatch(getTarget().getValue()).getRight();
 		if (right != null) {
 			if (reference.isMany()) {
 				ret = new ManyReferenceChangeNode(right, reference, value);
 			} else {
 				// todo
 			}
 		}
 		return ret;
 	}
 }
