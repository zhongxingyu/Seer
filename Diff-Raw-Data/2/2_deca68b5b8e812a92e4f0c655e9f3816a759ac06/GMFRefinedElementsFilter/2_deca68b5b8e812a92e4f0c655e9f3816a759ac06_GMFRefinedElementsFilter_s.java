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
 package org.eclipse.emf.compare.diagram.ide.ui.internal.structuremergeviewer.filters;
 
 import static com.google.common.base.Predicates.instanceOf;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.diagram.internal.extensions.DiagramDiff;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.impl.AbstractDifferenceFilter;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.edit.tree.TreeNode;
 
 /**
  * A filter used by default that filtered out refined GMF differences.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class GMFRefinedElementsFilter extends AbstractDifferenceFilter {
 
 	/**
 	 * The predicate use by this filter when it is selected.
 	 */
 	private static final Predicate<? super EObject> PREDICATE_WHEN_SELECTED = new Predicate<EObject>() {
 		public boolean apply(EObject input) {
 			if (input instanceof TreeNode) {
 				EObject data = ((TreeNode)input).getData();
 				if (data instanceof Diff) {
 					Diff diff = (Diff)data;
 					return Iterables.any(diff.getRefines(), instanceOf(DiagramDiff.class));
 				}
 			}
 			return false;
 		}
 	};
 
 	/**
 	 * The predicate use by this filter when it is unselected.
 	 */
 	private static final Predicate<? super EObject> PREDICATE_WHEN_UNSELECTED = new Predicate<EObject>() {
 		public boolean apply(EObject input) {
 			if (input instanceof TreeNode) {
 				EObject data = ((TreeNode)input).getData();
 				if (data != null) {
					EPackage p = input.eClass().getEPackage();
 					if (p != null) {
 						return p.getNsURI().startsWith("http://www.eclipse.org/emf/compare/diagram"); //$NON-NLS-1$
 					}
 				}
 			}
 			return false;
 		}
 	};
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilter#isEnabled(org.eclipse.emf.compare.scope.IComparisonScope,
 	 *      org.eclipse.emf.compare.Comparison)
 	 */
 	@Override
 	public boolean isEnabled(IComparisonScope scope, Comparison comparison) {
 		if (scope != null) {
 			for (String nsURI : scope.getNsURIs()) {
 				if (nsURI.matches("http://www\\.eclipse\\.org/gmf/runtime/.*/notation")) { //$NON-NLS-1$
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilter#getPredicateWhenSelected()
 	 */
 	@Override
 	public Predicate<? super EObject> getPredicateWhenSelected() {
 		return PREDICATE_WHEN_SELECTED;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.impl.AbstractDifferenceFilter#getPredicateWhenUnselected()
 	 */
 	@Override
 	public Predicate<? super EObject> getPredicateWhenUnselected() {
 		return PREDICATE_WHEN_UNSELECTED;
 	}
 
 }
