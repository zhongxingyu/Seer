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
 package org.eclipse.emf.compare.utils;
 
 import static org.eclipse.emf.compare.utils.ReferenceUtil.getAsList;
 
import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * This utility class holds methods that will be used by the diff and merge processes.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  */
 public final class MatchUtil {
 	/**
 	 * Utility classes don't need a default constructor.
 	 */
 	private MatchUtil() {
 		// Hides default constructor
 	}
 
 	/**
 	 * Get the object which is the origin value from the given matching <code>object</code>.
 	 * 
 	 * @param comparison
 	 *            The comparison.
 	 * @param object
 	 *            The given object.
 	 * @return The origin value.
 	 */
 	public static EObject getOriginObject(Comparison comparison, EObject object) {
 		EObject result = null;
 		Match match = comparison.getMatch(object);
 		if (match != null) {
 			if (comparison.isThreeWay()) {
 				result = match.getOrigin();
 			} else {
 				if (object == match.getLeft()) {
 					result = match.getRight();
 				} else {
 					result = match.getLeft();
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * From a given mono-valued reference change, get the origin value.
 	 * 
 	 * @param comparison
 	 *            The comparison.
 	 * @param difference
 	 *            The given reference change.
 	 * @return The origin value.
 	 */
 	public static EObject getOriginValue(Comparison comparison, ReferenceChange difference) {
 		if (!difference.getReference().isContainment() && !difference.getReference().isMany()
 				&& difference.getKind().equals(DifferenceKind.CHANGE)) {
 			EObject originContainer = getOriginContainer(comparison, difference);
 			if (originContainer != null) {
 				Object originValue = originContainer.eGet(difference.getReference());
 				if (originValue instanceof EObject) {
 					return (EObject)originValue;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get the business model object containing the given <code>difference</code> in the origin side.
 	 * 
 	 * @param comparison
 	 *            The comparison.
 	 * @param difference
 	 *            The difference.
 	 * @return The object.
 	 */
 	public static EObject getOriginContainer(Comparison comparison, ReferenceChange difference) {
 		final EObject diffContainer;
 		if (comparison.isThreeWay()) {
 			diffContainer = difference.getMatch().getOrigin();
 		} else {
 			if (getContainer(comparison, difference) == difference.getMatch().getLeft()) {
 				diffContainer = difference.getMatch().getRight();
 			} else {
 				diffContainer = difference.getMatch().getLeft();
 			}
 		}
 		return diffContainer;
 	}
 
 	/**
 	 * Get the business model object containing the given <code>difference</code>.
 	 * 
 	 * @param comparison
 	 *            The comparison.
 	 * @param difference
 	 *            The difference.
 	 * @return The object.
 	 */
 	public static EObject getContainer(Comparison comparison, ReferenceChange difference) {
 		EObject result = null;
 		final EObject value = difference.getValue();
 		Match match = difference.getMatch();
 		if (getAsList(match.getLeft(), difference.getReference()).contains(value)) {
 			result = match.getLeft();
 		} else if (getAsList(match.getRight(), difference.getReference()).contains(value)) {
 			result = match.getRight();
 		} else if (getAsList(match.getOrigin(), difference.getReference()).contains(value)) {
 			result = match.getOrigin();
 		}
 		return result;
 	}
 
 	/**
 	 * Get the business model object containing the given <code>difference</code>.
 	 * 
 	 * @param difference
 	 *            The detected difference for which we need the actually modified object.
 	 * @return The object which presents the given difference.
 	 */
	public static EObject getContainer(AttributeChange difference) {
 		EObject result = difference.getMatch().getLeft();
 		if (result == null) {
 			result = difference.getMatch().getRight();
 		}
 		if (result == null) {
 			result = difference.getMatch().getOrigin();
 		}
 		return result;
 	}
 
 	/**
 	 * Get the business model object containing the given <code>difference</code>.
 	 * 
 	 * @param comparison
 	 *            The current comparison.
 	 * @param difference
 	 *            The detected difference for which we need the actually modified object.
 	 * @return The object which presents the given difference.
 	 */
 	public static EObject getContainer(Comparison comparison, Diff difference) {
 		EObject result = null;
		if (difference instanceof AttributeChange) {
			result = getContainer((AttributeChange)difference);
		} else if (difference instanceof ReferenceChange) {
 			result = getContainer(comparison, (ReferenceChange)difference);
 		}
 		return result;
 	}
 }
