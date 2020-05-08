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
 package org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.impl;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableSet;
 
 import org.eclipse.emf.compare.Conflict;
 import org.eclipse.emf.compare.ConflictKind;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.provider.spec.MatchItemProviderSpec;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * A filter used by default that filtered out cascading differences (differences located under differences,
  * also known as sub differences).
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  * @since 3.0
  */
 public class CascadingDifferencesFilter extends AbstractDifferenceFilter {
 
 	/**
 	 * The predicate use by this filter when it is selected.
 	 */
 	private static final Predicate<? super EObject> predicateWhenSelected = new Predicate<EObject>() {
 		public boolean apply(EObject input) {
 			boolean ret = false;
 			if (input instanceof Diff && !(input instanceof ResourceAttachmentChange)) {
 				final Diff diff = (Diff)input;
 				final Conflict conflict = diff.getConflict();
 				if (conflict == null || ConflictKind.PSEUDO == conflict.getKind()) {
					final EObject grandParent = diff.getMatch().eContainer();
 					if (grandParent instanceof Match) {
 						ImmutableSet<EObject> containementDifferenceValues = MatchItemProviderSpec
 								.containmentReferencesValues((Match)grandParent);
 						if (MatchItemProviderSpec.matchOfContainmentDiff(containementDifferenceValues).apply(
								diff.getMatch())) {
 							ret = true;
 						}
 					}
 				}
 			}
 			return ret;
 		}
 	};
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilter#getPredicateWhenSelected()
 	 */
 	@Override
 	public Predicate<? super EObject> getPredicateWhenSelected() {
 		return predicateWhenSelected;
 	}
 }
