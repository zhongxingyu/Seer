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
 package org.eclipse.mylyn.docs.intent.compare.utils;
 
 import com.google.common.base.Predicate;
 
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.EMFCompare;
 import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
 import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher;
 import org.eclipse.emf.compare.utils.EqualityHelper;
 import org.eclipse.emf.compare.utils.UseIdentifiers;
 import org.eclipse.mylyn.docs.intent.compare.scope.IntentComparisonScope;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 
 import static com.google.common.base.Predicates.instanceOf;
 import static com.google.common.base.Predicates.not;
 import static com.google.common.base.Predicates.or;
 
 /**
  * Utilities for EMF Compare use.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public final class EMFCompareUtils {
 
 	/**
 	 * Prevents instantiation.
 	 */
 	private EMFCompareUtils() {
 	}
 
 	/**
	 * Returns the differences between the left element and the right element.
 	 * 
 	 * @param left
 	 *            the left element
 	 * @param right
 	 *            the right element
 	 * @return the differences between the left element and the right element
 	 */
 	public static Comparison compare(Notifier left, Notifier right) {
		EMFCompare compare = EMFCompare.newComparator(new IntentComparisonScope(left, right));
 		compare.matchByID(UseIdentifiers.NEVER);
 		return compare.compare();
 	}
 
 	/**
	 * Returns the differences between the left element and the right element.
 	 * 
 	 * @param left
 	 *            the left element
 	 * @param right
 	 *            the right element
 	 * @return the differences between the left element and the right element
 	 */
 	public static Comparison compareDocuments(Notifier left, Notifier right) {
 		IntentComparisonScope scope = new IntentComparisonScope(left, right);
 		EqualityHelper helper = new IntentEqualityHelper();
 
 		Predicate<Object> filter = not(or(instanceOf(CompilationStatus.class),
 				instanceOf(SynchronizerCompilationStatus.class)));
 		scope.setEObjectContentFilter(filter);
 
 		IEObjectMatcher matcher = ProximityEObjectMatcher.builder(
 				org.eclipse.mylyn.docs.intent.compare.match.EditionDistance.builder(helper).build()).build();
 
 		EMFCompare compare = EMFCompare.newComparator(scope);
 		compare.setEObjectMatcher(matcher);
 		compare.matchByID(UseIdentifiers.NEVER);
 		compare.setEqualityHelper(helper);
 		return compare.compare();
 	}
 }
