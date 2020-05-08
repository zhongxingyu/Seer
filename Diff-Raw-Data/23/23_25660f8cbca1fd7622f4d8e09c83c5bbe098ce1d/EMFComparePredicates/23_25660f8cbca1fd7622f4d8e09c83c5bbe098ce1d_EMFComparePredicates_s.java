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
 
 import static com.google.common.base.Predicates.and;
 
 import com.google.common.base.Predicate;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.ConflictKind;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 
 /**
  * This class will provide a number of Predicates that can be used to retrieve particular {@link Diff}s from
  * an iterable.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class EMFComparePredicates {
 	/**
 	 * This class does not need to be instantiated.
 	 */
 	private EMFComparePredicates() {
 		// Hides default constructor
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the modification of a single-valued
 	 * reference going by the given {@code referenceName} on an EObject which name matches
 	 * {@code qualifiedName}. This can be used both on three-way and two-way Diffs : if three-way, we'll
 	 * consider that the {@code fromQualifiedName} can be either one of the right or origin values, and the
 	 * {@code toQualifiedName} to be either left or right. on two-way diffs however, {@code fromQualifiedName}
 	 * can only be the right value, and {@code toQualifiedName} will be the left one.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present a ReferenceChange.
 	 * @param referenceName
 	 *            Name of the single-valued reference on which we expect a change.
 	 * @param fromQualifiedName
 	 *            The original value of this reference.
 	 * @param toQualifiedName
 	 *            The value to which this reference has been changed.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> changedReference(final String qualifiedName,
 			final String referenceName, final String fromQualifiedName, final String toQualifiedName) {
 		final Predicate<? super Diff> valuesMatch = new ReferenceValuesMatch(referenceName,
 				fromQualifiedName, toQualifiedName);
 		return and(ofKind(DifferenceKind.CHANGE), onEObject(qualifiedName), valuesMatch);
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the addition of a value in a
 	 * multi-valued attribute going by {@code attributeName} on an EObject which name matches
 	 * {@code qualifiedName}.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present an AttributeChange.
 	 * @param attributeName
 	 *            Name of the multi-valued attribute on which we expect a change.
 	 * @param addedValue
 	 *            The value we expect to have been added to this attribute.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> addedToAttribute(final String qualifiedName,
 			final String attributeName, final Object addedValue) {
 		// This is only meant for multi-valued attributes
 		return and(ofKind(DifferenceKind.ADD), onEObject(qualifiedName), attributeValueMatch(attributeName,
 				addedValue, true));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the addition of a value in a
 	 * multi-valued reference going by {@code referenceName} on an EObject which name matches
 	 * {@code qualifiedName}.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present a ReferenceChange.
 	 * @param referenceName
 	 *            Name of the multi-valued reference on which we expect a change.
 	 * @param addedQualifiedName
 	 *            Qualified name of the EObject which we expect to have been added to this reference.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> addedToReference(final String qualifiedName,
 			final String referenceName, final String addedQualifiedName) {
 		// This is only meant for multi-valued references
 		return and(ofKind(DifferenceKind.ADD), onEObject(qualifiedName), referenceValueMatch(referenceName,
 				addedQualifiedName, true));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the moving of a value within a
 	 * multi-valued attribute going by {@code attributeName} on an EObject which name matches
 	 * {@code qualifiedName}.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present an AttributeChange.
 	 * @param attributeName
 	 *            Name of the multi-valued attribute on which we expect a change.
 	 * @param removedValue
 	 *            Value which we expect to have been moved within this attribute.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> movedInAttribute(final String qualifiedName,
 			final String attributeName, final Object removedValue) {
 		// This is only meant for multi-valued attributes
 		return and(ofKind(DifferenceKind.MOVE), onEObject(qualifiedName), attributeValueMatch(attributeName,
 				removedValue, true));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the moving of a value within a
 	 * multi-valued reference going by {@code referenceName} on an EObject which name matches
 	 * {@code qualifiedName}.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present a ReferenceChange.
 	 * @param referenceName
 	 *            Name of the multi-valued reference on which we expect a change.
 	 * @param removedQualifiedName
 	 *            Qualified name of the EObject which we expect to have been moved within this reference.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> movedInReference(final String qualifiedName,
 			final String referenceName, final String removedQualifiedName) {
 		// This is only meant for multi-valued references
 		return and(ofKind(DifferenceKind.MOVE), onEObject(qualifiedName), referenceValueMatch(referenceName,
 				removedQualifiedName, true));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the deletion of a value from a
 	 * multi-valued attribute going by {@code attributeName} on an EObject which name matches
 	 * {@code qualifiedName}.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present an AttributeChange.
 	 * @param attributeName
 	 *            Name of the multi-valued attribute on which we expect a change.
 	 * @param removedValue
 	 *            Value which we expect to have been removed from this attribute.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> removedFromAttribute(final String qualifiedName,
 			final String attributeName, final Object removedValue) {
 		// This is only meant for multi-valued attributes
 		return and(ofKind(DifferenceKind.DELETE), onEObject(qualifiedName), attributeValueMatch(
 				attributeName, removedValue, true));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the deletion of a value from a
 	 * multi-valued reference going by {@code referenceName} on an EObject which name matches
 	 * {@code qualifiedName}.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present a ReferenceChange.
 	 * @param referenceName
 	 *            Name of the multi-valued reference on which we expect a change.
 	 * @param removedQualifiedName
 	 *            Qualified name of the EObject which we expect to have been removed from this reference.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> removedFromReference(final String qualifiedName,
 			final String referenceName, final String removedQualifiedName) {
 		// This is only meant for multi-valued references
 		return and(ofKind(DifferenceKind.DELETE), onEObject(qualifiedName), referenceValueMatch(
 				referenceName, removedQualifiedName, true));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the modification of a single-valued
 	 * attribute going by the given {@code attributeName} on an EObject which name matches
 	 * {@code qualifiedName}. This can be used both on three-way and two-way Diffs : if three-way, we'll
 	 * consider that the {@code fromValue} can be either one of the right or origin values, and the
 	 * {@code toValue} to be either left or right. on two-way diffs however, {@code fromValue} can only be the
 	 * right value, and {@code toValue} will be the left one.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a String
 	 * for us to compare it with the given qualified name.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            Qualified name of the EObject which we expect to present an AttributeChange.
 	 * @param attributeName
 	 *            Name of the single-valued attribute on which we expect a change.
 	 * @param fromValue
 	 *            The original value of this attribute.
 	 * @param toValue
 	 *            The value to which this attribute has been changed.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> changedAttribute(final String qualifiedName,
 			final String attributeName, final Object fromValue, final Object toValue) {
 		final Predicate<? super Diff> valuesMatch = new AttributeValuesMatch(attributeName, fromValue,
 				toValue);
 		return and(ofKind(DifferenceKind.CHANGE), onEObject(qualifiedName), valuesMatch);
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the addition of an EObject matching
 	 * the given qualified name. Namely, it will check that that Diff is a ReferenceChange, that one of its
 	 * Match sides correspond to the given qualified name's ancestors, and that its value correspond to the
 	 * given qualified name's last segment.
 	 * <p>
 	 * For example, {@code added("extlibrary.BookCategory.Encyclopedia")} will check that an EObject named
 	 * "Encyclopedia" has been added under the container "extlibrary.BookCategory". Note that
 	 * {@code added("emf.compare.Match")} will <b>not</b> match a difference on the EObject
 	 * "org.eclipse.emf.compare.Match". The qualified name must be absolute.
 	 * </p>
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a
 	 * String.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            The qualified name of the EObject we expect to have been added.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> added(final String qualifiedName) {
 		final int parentEndIndex = qualifiedName.lastIndexOf('.');
 		if (parentEndIndex >= 0) {
 			final String ancestors = qualifiedName.substring(0, parentEndIndex);
 			final String objectName = qualifiedName.substring(parentEndIndex + 1);
 			return and(ofKind(DifferenceKind.ADD), onEObject(ancestors), valueNameMatches(objectName));
 		}
 		return and(valueNameMatches(qualifiedName), ofKind(DifferenceKind.ADD));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the move of an EObject matching the
 	 * given qualified name. Namely, it will check that that Diff is a ReferenceChange, that one of its Match
 	 * sides correspond to the given qualified name's ancestors, and that its value correspond to the given
 	 * qualified name's last segment.
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a
 	 * String.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            The qualified name of the EObject we expect to have been moved.
 	 * @param referenceName
 	 *            Name of the reference in which we expect a child to have been added.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> moved(final String qualifiedName, final String referenceName) {
 		final int parentEndIndex = qualifiedName.lastIndexOf('.');
 		if (parentEndIndex >= 0) {
 			final String ancestors = qualifiedName.substring(0, parentEndIndex);
 			final String objectName = qualifiedName.substring(parentEndIndex + 1);
 			return and(ofKind(DifferenceKind.MOVE), onEObject(ancestors), onFeature(referenceName),
 					valueNameMatches(objectName));
 		}
 		return and(ofKind(DifferenceKind.MOVE), valueNameMatches(qualifiedName), onFeature(referenceName));
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff represents the removal of an EObject matching
 	 * the given qualified name. Namely, it will check that that Diff is a ReferenceChange, that one of its
 	 * Match sides correspond to the given qualified name's ancestors, and that its value correspond to the
 	 * given qualified name's last segment.
 	 * <p>
 	 * For example, {@code removed("extlibrary.BookCategory.Encyclopedia")} will check that an EObject named
 	 * "Encyclopedia" has been removed from the container "extlibrary.BookCategory". Note that
 	 * {@code removed("emf.compare.Match")} will <b>not</b> match a difference on the EObject
 	 * "org.eclipse.emf.compare.Match". The qualified name must be absolute.
 	 * </p>
 	 * <p>
 	 * Note that in order for this to work, we expect the EObjects to have a "name" feature returning a
 	 * String.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            The qualified name of the EObject we expect to have been removed.
 	 * @return The created predicate.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Predicate<? super Diff> removed(final String qualifiedName) {
 		final int parentEndIndex = qualifiedName.lastIndexOf('.');
 		if (parentEndIndex >= 0) {
 			final String ancestors = qualifiedName.substring(0, parentEndIndex);
 			final String objectName = qualifiedName.substring(parentEndIndex + 1);
 			return and(ofKind(DifferenceKind.DELETE), onEObject(ancestors), valueNameMatches(objectName));
 		}
 		return and(valueNameMatches(qualifiedName), ofKind(DifferenceKind.DELETE));
 	}
 
 	/**
 	 * This can be used to check that a given Diff correspond to either an {@link AttributeChange} or a
 	 * {@link ReferenceChange}, and that the corresponding reference or attribute matches the given
 	 * {@code featureName}.
 	 * 
 	 * @param featureName
 	 *            Name of the feature on which we expect a change.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> onFeature(final String featureName) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				final EStructuralFeature affectedFeature;
 				if (input instanceof AttributeChange) {
 					affectedFeature = ((AttributeChange)input).getAttribute();
 				} else if (input instanceof ReferenceChange) {
 					affectedFeature = ((ReferenceChange)input).getReference();
 				} else {
 					return false;
 				}
 				return featureName.equals(affectedFeature.getName());
 			}
 		};
 	}
 
 	/**
 	 * This can be used to check that a given Diff originates from the given {@code source} side.
 	 * 
 	 * @param source
 	 *            The side from which we expect this diff to originate.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> fromSide(final DifferenceSource source) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				return input != null && input.getSource() == source;
 			}
 		};
 	}
 
 	/**
 	 * This can be used in order to check that a Diff has been detected on the given EObject.
 	 * 
 	 * @param eObject
 	 *            The EObject which we expect the diff to concern.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> onEObject(final EObject eObject) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				if (input == null) {
 					return false;
 				}
 
 				final Match match = input.getMatch();
 				return match.getLeft() == eObject || match.getRight() == eObject
 						|| match.getOrigin() == eObject;
 			}
 		};
 	}
 
 	/**
 	 * This can be used in order to check whether a Diff has been detected on an EObject matching the given
 	 * qualified name.
 	 * <p>
 	 * For this to work, we expect the EObjects to have a feature named "name" returning a String.
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            The qualified name of the EObject we expect that diff to concern.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> onEObject(final String qualifiedName) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				if (input == null) {
 					return false;
 				}
 
 				final Match match = input.getMatch();
 				return match(match.getLeft(), qualifiedName, null)
 						|| match(match.getRight(), qualifiedName, null)
 						|| match(match.getOrigin(), qualifiedName, null);
 			}
 		};
 	}
 
 	/**
 	 * This can be used in order to check whether a Diff has been detected on an EObject matching the given
 	 * qualified name or the qualified name under the given feature.
 	 * <p>
 	 * For this to work, we expect the EObjects to have a feature named "name" returning a String or to have
 	 * the given feature (String or EObject with a feature named "name").
 	 * </p>
 	 * 
 	 * @param qualifiedName
 	 *            The qualified name of the EObject we expect that diff to concern.
 	 * @param featureDelegate
 	 *            The optional feature to define the name of the objects. May be null.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> onEObject(final String qualifiedName,
 			final EStructuralFeature featureDelegate) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				if (input == null) {
 					return false;
 				}
 
 				final Match match = input.getMatch();
 				return match(match.getLeft(), qualifiedName, featureDelegate)
 						|| match(match.getRight(), qualifiedName, featureDelegate)
 						|| match(match.getOrigin(), qualifiedName, featureDelegate);
 			}
 		};
 	}
 
 	/**
 	 * This predicate can be used to check whether a particular diff is of the given {@code kind}. This is
 	 * mainly used to differentiate additions from deletions.
 	 * 
 	 * @param kind
 	 *            The kind we expect this diff to have.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> ofKind(final DifferenceKind kind) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				return input != null && input.getKind() == kind;
 			}
 		};
 	}
 
 	/**
 	 * This predicate can be used in order to check that a particular Diff describes either a
 	 * {@link ReferenceChange} or {@link AttributeChange} for the given {@code expectedValue}.
 	 * <p>
 	 * For example, this could be used to check that the given value has indeed been added to a reference or
 	 * attribute, though such checks are more easily performed through {@link #addedIn(EObject, EObject)} or
 	 * {@link #removedFrom(EObject, EObject)}.
 	 * </p>
 	 * 
 	 * @param expectedValue
 	 *            The value which we expect to have changed and detected through a Diff.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> valueIs(final Object expectedValue) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				final Object value;
 				if (input instanceof ReferenceChange) {
 					value = ((ReferenceChange)input).getValue();
 				} else if (input instanceof AttributeChange) {
 					value = ((AttributeChange)input).getValue();
 				} else {
 					return false;
 				}
 				return value == expectedValue;
 			}
 		};
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff describes an AttributeChange with the given
 	 * {@code attributeName} and which changed value corresponds to the given {@code expectedValue}.
 	 * 
 	 * @param attributeName
 	 *            The name of the attribute for which we seek an AttributeChange.
 	 * @param expectedValue
 	 *            The value we expect to correspond to this AttributeChange.
 	 * @param multiValued
 	 *            Tells us to check for either multi- or single-valued reference changes.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> attributeValueMatch(final String attributeName,
 			final Object expectedValue, final boolean multiValued) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				if (input instanceof AttributeChange
 						&& ((AttributeChange)input).getAttribute().getName().equals(attributeName)
 						&& ((AttributeChange)input).getAttribute().isMany() == multiValued) {
 					final Object value = ((AttributeChange)input).getValue();
 					return new EqualityHelper().matchingAttributeValues(value, expectedValue);
 				}
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff describes a ReferenceChange with the given
 	 * {@code referenceName} and which changed value corresponds to the given {@code qualifiedName}.
 	 * <p>
 	 * For this to work, we expect the EObject to have a feature named "name" returning a String for us to try
 	 * and match it.
 	 * </p>
 	 * 
 	 * @param referenceName
 	 *            The reference for which we seek a ReferenceChange.
 	 * @param qualifiedName
 	 *            The qualified name of the EObject on which we detected a change.
 	 * @param multiValued
 	 *            Tells us to check for either multi- or single-valued reference changes.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> referenceValueMatch(final String referenceName,
 			final String qualifiedName, final boolean multiValued) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				if (input instanceof ReferenceChange
 						&& ((ReferenceChange)input).getReference().getName().equals(referenceName)
 						&& ((ReferenceChange)input).getReference().isMany() == multiValued) {
 					final EObject value = ((ReferenceChange)input).getValue();
 					return qualifiedName != null && match(value, qualifiedName, null);
 				}
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * This predicate can be used to check whether a given Diff describes a ReferenceChange with the given
 	 * {@code referenceName} and which changed value corresponds to the given {@code qualifiedName} or the
 	 * qualified name under the given {@code featureDelegate}.
 	 * <p>
 	 * For this to work, we expect the EObject to have a feature named "name" returning a String or to have
 	 * the given feature (String or EObject with a feature named "name") for us to try and match it.
 	 * </p>
 	 * 
 	 * @param referenceName
 	 *            The reference for which we seek a ReferenceChange.
 	 * @param qualifiedName
 	 *            The qualified name of the EObject on which we detected a change.
 	 * @param multiValued
 	 *            Tells us to check for either multi- or single-valued reference changes.
 	 * @param featureDelegate
 	 *            The optional feature to define the name of the objects. May be null.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> referenceValueMatch(final String referenceName,
 			final String qualifiedName, final boolean multiValued, final EStructuralFeature featureDelegate) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				if (input instanceof ReferenceChange
 						&& ((ReferenceChange)input).getReference().getName().equals(referenceName)
 						&& ((ReferenceChange)input).getReference().isMany() == multiValued) {
 					final EObject value = ((ReferenceChange)input).getValue();
 					return qualifiedName != null && match(value, qualifiedName, featureDelegate);
 				}
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * This can be used to check whether a given Diff describes either a {@link ReferenceChange} on an EObject
 	 * which name is {@code expectedName}.
 	 * <p>
 	 * For this to work, we expect the EObject to have a feature named "name" returning a String for us to try
 	 * and match it.
 	 * </p>
 	 * 
 	 * @param expectedName
 	 *            The name of the EObject which we expect as a changed reference value.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> valueNameMatches(final String expectedName) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				final EObject value;
 				if (input instanceof ReferenceChange) {
 					value = ((ReferenceChange)input).getValue();
 				} else {
 					return false;
 				}
 				return internalMatch(value, expectedName, null);
 			}
 		};
 	}
 
 	/**
 	 * This can be used to check whether a given Diff describes either a {@link ReferenceChange} on an EObject
 	 * which name is {@code expectedName} or which the given feature provides the {@code expectedName}.
 	 * <p>
 	 * For this to work, we expect the EObject to have a feature named "name" returning a String or to have
 	 * the given feature (String or EObject with a feature named "name") for us to try and match it.
 	 * </p>
 	 * 
 	 * @param expectedName
 	 *            The name of the EObject which we expect as a changed reference value.
 	 * @param featureDelegate
 	 *            The optional feature to define the name of the objects. May be null.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> valueNameMatches(final String expectedName,
 			final EStructuralFeature featureDelegate) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				final EObject value;
 				if (input instanceof ReferenceChange) {
 					value = ((ReferenceChange)input).getValue();
 				} else {
 					return false;
 				}
 				return internalMatch(value, expectedName, featureDelegate);
 			}
 		};
 	}
 
 	/**
 	 * This can be used to check whether a given Diff is in a pseudo conflict state regarding other diffs.
 	 * <p>
 	 * pseudo conflict means both parties did the same change compared to the ancestor state.
 	 * </p>
 	 * 
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> hasPseudoConflict() {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				return input != null && input.getConflict() != null
 						&& input.getConflict().getKind() == ConflictKind.PSEUDO;
 			}
 		};
 	}
 
 	/**
 	 * This can be used to check whether a given Diff is in a conflicting state regarding other diffs. It is
 	 * only considering states which are real conflicts opposed to the pseudo conflicts.
 	 * 
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> hasRealConflict() {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				return input != null && input.getConflict() != null
 						&& input.getConflict().getKind() == ConflictKind.REAL;
 			}
 		};
 	}
 
 	/**
 	 * This can be used to check whether a given Diff is in (one of) the given state(s).
 	 * 
 	 * @param states
 	 *            State(s) in which we need a Diff to be.
 	 * @return The created predicate.
 	 */
 	public static Predicate<? super Diff> hasState(final DifferenceState... states) {
 		return new Predicate<Diff>() {
 			public boolean apply(Diff input) {
 				return input != null && Arrays.asList(states).contains(input.getState());
 			}
 		};
 	}
 
 	/**
 	 * This will be used to check that a given {@link EObject} corresponds to the given {@code qualifiedName}.
 	 * <p>
 	 * For example, {@code match("extlibrary.BookCategory.Encyclopedia")} will return {@code true} for an
 	 * EObject named "Encyclopedia" under the container "extlibrary.BookCategory". Note, however that
 	 * {@code match("emf.compare.Match")} will <b>not</b> match the EObject "org.eclipse.emf.compare.Match".
 	 * The qualified name must be absolute.
 	 * </p>
 	 * <p>
 	 * For this to work, we expect the EObject to have a feature named "name" returning a String or to have
 	 * the given feature (String or EObject with a feature named "name") for us to try and match it. See also
 	 * {@link #getNameFeature(EObject)}.
 	 * </p>
 	 * 
 	 * @param eObject
 	 *            The EObject which qualified name we are to check.
 	 * @param qualifiedName
 	 *            The expected, <b>absolute</b> qualified name of the given {@code eObject}.
	 * @param The
	 *            optional feature to define the name of the objects. May be null.
 	 * @return {@code true} if the given {@code eObject} matches the given {@code qualifiedName},
 	 *         {@code false} if not, or if we could not determine the "name" feature of that EObject.
 	 * @see #getNameFeature(EObject)
 	 */
 	private static boolean match(EObject eObject, String qualifiedName, EStructuralFeature featureDelegate) {
 		if (eObject == null || qualifiedName == null || qualifiedName.length() == 0) {
 			return false;
 		}
 		final String[] names = qualifiedName.split("\\."); //$NON-NLS-1$
 
 		int current = names.length - 1;
 		boolean matches = internalMatch(eObject, names[current--], featureDelegate);
 		if (matches) {
 			EObject container = eObject.eContainer();
 			while (matches && container != null && current >= 0) {
 				matches = internalMatch(container, names[current--], featureDelegate);
 				container = container.eContainer();
 			}
 			// This qualified name does not match if there was still a container "above"
 			// "emf.compare.Match" does not match the EObject "org.eclipse.emf.compare.Match"
 			matches = matches && container == null;
 		}
 
 		return matches;
 	}
 
 	/**
 	 * This will be used to check whether a given Object matches the given {@code qualifiedName}, considering
 	 * {@code null} as legal values. Namely, this will return {@code true} in the following cases :
 	 * <ul>
 	 * <li>both {@code eObject} and {@code qualifiedName} are {@code null}</li>
 	 * <li>eObject is an instance of {@link EObject} and its qualified name matches the given
 	 * {@code qualifiedName} according to the semantics of {@link #match(EObject, String)}</li>
 	 * </ul>
 	 * 
 	 * @param eObject
 	 *            The Object which qualified name we are to check. May be {@code null}.
 	 * @param qualifiedName
 	 *            The expected, <b>absolute</b> qualified name of the given {@code eObject}. May be
 	 *            {@code null}.
 	 * @return {@code true} if the given {@code eObject} matches the given {@code qualifiedName},
 	 *         {@code false} if not, or if we could not determine the "name" feature of that EObject.
 	 * @see #match(EObject, String)
 	 */
 	private static boolean matchAllowingNull(Object eObject, String qualifiedName) {
 		if (eObject == null) {
 			return qualifiedName == null;
 		}
 		return qualifiedName != null && eObject instanceof EObject
 				&& match((EObject)eObject, qualifiedName, null);
 	}
 
 	/**
 	 * Checks that the given {@code eObject}'s name is equal to {@code name}.
 	 * <p>
 	 * For this to work, we expect the EObject to have a feature named "name" returning a String or to have
 	 * the given feature (String or EObject with a feature named "name") for us to try and match it. See also
 	 * {@link #getNameFeature(EObject)}.
 	 * </p>
 	 * 
 	 * @param eObject
 	 *            the EObject which name we are to check.
 	 * @param name
 	 *            The expected name of {@code eObject}.
	 * @param The
	 *            optional feature to define the name of the objects. May be null.
 	 * @return {@code true} if the given {@code eObject}'s name is equal to the given {@code name},
 	 *         {@code false} if not, or if we could not determine the "name" feature of that EObject.
 	 * @see #getNameFeature(EObject)
 	 */
 	private static boolean internalMatch(EObject eObject, String name, EStructuralFeature featureDelegate) {
 		final EStructuralFeature nameFeature = getNameFeature(eObject);
 		if (nameFeature != null) {
 			final Object featureValue = eObject.eGet(nameFeature);
 			if (featureValue instanceof String) {
				return featureValue.equals(name);
 			}
 		} else if (featureDelegate != null && !featureDelegate.isMany()) {
 			final Object featureValue = eObject.eGet(featureDelegate, false);
 			if (featureValue instanceof String) {
				return featureValue.equals(name);
 			} else if (featureValue instanceof EObject) {
				return internalMatch((EObject)featureValue, name, null);
 			}
 		}
		return false;
 	}
 
 	/**
 	 * Tries and determine the "name" feature of the given EObject. By default, we only consider
 	 * {@link ENamedElement#name} or a feature of the given {@code eObject}'s EClass which would be named
 	 * "name".
 	 * 
 	 * @param eObject
 	 *            The EObject for which we are trying to determine a name.
 	 * @return The name feature of the given EObject if we could find one, {@code null} otherwise.
 	 */
 	private static EStructuralFeature getNameFeature(EObject eObject) {
 		if (eObject instanceof ENamedElement) {
 			return EcorePackage.eINSTANCE.getENamedElement_Name();
 		}
 		EStructuralFeature nameFeature = null;
 		final Iterator<EStructuralFeature> features = eObject.eClass().getEAllStructuralFeatures().iterator();
 		while (nameFeature == null && features.hasNext()) {
 			final EStructuralFeature feature = features.next();
 			if ("name".equals(feature.getName())) { //$NON-NLS-1$
 				nameFeature = feature;
 			}
 		}
 		return nameFeature;
 	}
 
 	/**
 	 * This particular predicate will be used to check that a given Diff corresponds to a ReferenceChange on a
 	 * given reference, with known "original" and "changed" values.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static final class ReferenceValuesMatch implements Predicate<Diff> {
 		/** Name of the reference we expect to have been changed. */
 		private final String referenceName;
 
 		/** Qualified name of the expected original value of this reference. */
 		private final String fromQualifiedName;
 
 		/** Qualified name of the value to which this reference is expected to have changed. */
 		private final String toQualifiedName;
 
 		/**
 		 * Instantiates this predicate given the values it is meant to match.
 		 * 
 		 * @param referenceName
 		 *            Name of the single-valued reference on which we expect a change.
 		 * @param fromQualifiedName
 		 *            The original value of this reference.
 		 * @param toQualifiedName
 		 *            The value to which this reference has been changed.
 		 */
 		public ReferenceValuesMatch(String referenceName, String fromQualifiedName, String toQualifiedName) {
 			this.referenceName = referenceName;
 			this.fromQualifiedName = fromQualifiedName;
 			this.toQualifiedName = toQualifiedName;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see com.google.common.base.Predicate#apply(java.lang.Object)
 		 */
 		public boolean apply(Diff input) {
 			// Note that this is not meant for many-valued references
 			if (input instanceof ReferenceChange
 					&& ((ReferenceChange)input).getReference().getName().equals(referenceName)
 					&& !((ReferenceChange)input).getReference().isMany()) {
 				final EReference reference = ((ReferenceChange)input).getReference();
 				final Match match = input.getMatch();
 				final Object leftValue;
 				if (match.getLeft() != null) {
 					leftValue = match.getLeft().eGet(reference);
 				} else {
 					leftValue = null;
 				}
 				final Object rightValue;
 				if (match.getRight() != null) {
 					rightValue = match.getRight().eGet(reference);
 				} else {
 					rightValue = null;
 				}
 				final Object originValue;
 				if (match.getOrigin() != null) {
 					originValue = match.getOrigin().eGet(reference);
 				} else {
 					originValue = null;
 				}
 
 				// "from" is either right or origin
 				boolean applies = false;
 				if (matchAllowingNull(originValue, fromQualifiedName)) {
 					// "from" is origin, "to" can be either left or right
 					applies = matchAllowingNull(leftValue, toQualifiedName)
 							|| matchAllowingNull(rightValue, toQualifiedName);
 				} else if (matchAllowingNull(rightValue, fromQualifiedName)) {
 					// "from" is right, "to" can only be left
 					applies = matchAllowingNull(leftValue, toQualifiedName);
 				}
 				return applies;
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * This particular predicate will be used to check that a given Diff corresponds to an AttributeChange on
 	 * a given attribute, with known "original" and "changed" values.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private static final class AttributeValuesMatch implements Predicate<Diff> {
 		/** Name of the attribute we expect to have been changed. */
 		private final String attributeName;
 
 		/** The expected original value of this attribute. */
 		private final Object fromValue;
 
 		/** The value to which this attribute is expected to have changed. */
 		private final Object toValue;
 
 		/**
 		 * Instantiates this predicate given the values it is meant to match.
 		 * 
 		 * @param attributeName
 		 *            Name of the single-valued attribute on which we expect a change.
 		 * @param fromValue
 		 *            The original value of this attribute.
 		 * @param toValue
 		 *            The value to which this attribute has been changed.
 		 */
 		public AttributeValuesMatch(String attributeName, Object fromValue, Object toValue) {
 			this.attributeName = attributeName;
 			this.fromValue = fromValue;
 			this.toValue = toValue;
 		}
 
 		/**
 		 * Checks whether the two given Objects match : they are either both {@code null}, the same instance,
 		 * or their "equals" returns {@code true}. If neither is {@code true}, we assume that these two
 		 * Objects don't match.
 		 * <p>
 		 * Do note that "unset" values are in fact set to the empty String instead of {@code null}. We will
 		 * thus consider {@code null} equal to the empty String here.
 		 * </p>
 		 * 
 		 * @param attributeValue
 		 *            The reference value, first of the two Objects to compare.
 		 * @param expectedValue
 		 *            The expected value, second of the two Objects to compare.
 		 * @return {@code true} if these two Objects are equal, {@code false} otherwise.
 		 */
 		private static boolean equalAttributeValues(Object attributeValue, Object expectedValue) {
 			// Using == to handle the "null" case
 			boolean equal = expectedValue == attributeValue || expectedValue != null
 					&& expectedValue.equals(attributeValue);
 			// Consider that null is equal to the empty string (unset attributes)
 			if (!equal) {
 				equal = "".equals(attributeValue) && expectedValue == null || "".equals(expectedValue) //$NON-NLS-1$ //$NON-NLS-2$
 						&& attributeValue == null;
 			}
 			return equal;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see com.google.common.base.Predicate#apply(java.lang.Object)
 		 */
 		public boolean apply(Diff input) {
 			// Note that this is not meant for multi-valued attributes
 			if (input instanceof AttributeChange
 					&& ((AttributeChange)input).getAttribute().getName().equals(attributeName)
 					&& !((AttributeChange)input).getAttribute().isMany()) {
 				final EAttribute attribute = ((AttributeChange)input).getAttribute();
 				final Match match = input.getMatch();
 
 				final Object leftValue;
 				if (match.getLeft() != null) {
 					leftValue = match.getLeft().eGet(attribute);
 				} else {
 					leftValue = attribute.getDefaultValue();
 				}
 
 				final Object rightValue;
 				if (match.getRight() != null) {
 					rightValue = match.getRight().eGet(attribute);
 				} else {
 					rightValue = attribute.getDefaultValue();
 				}
 
 				final Object originValue;
 				if (match.getOrigin() != null) {
 					originValue = match.getOrigin().eGet(attribute);
 				} else {
 					originValue = attribute.getDefaultValue();
 				}
 
 				final Object actualFrom;
 				if (fromValue == null) {
 					actualFrom = attribute.getDefaultValue();
 				} else {
 					actualFrom = fromValue;
 				}
 				final Object actualTo;
 				if (toValue == null) {
 					actualTo = attribute.getDefaultValue();
 				} else {
 					actualTo = toValue;
 				}
 
 				// "from" is either right or origin
 				boolean applies = false;
 				if (equalAttributeValues(actualFrom, originValue)) {
 					// "from" is origin, "to" can be either left or right
 					applies = equalAttributeValues(actualTo, leftValue)
 							|| equalAttributeValues(actualTo, rightValue);
 				} else if (equalAttributeValues(actualFrom, rightValue)) {
 					// "from" is right, "to" can only be left
 					applies = equalAttributeValues(actualTo, leftValue);
 				}
 				return applies;
 			}
 			return false;
 		}
 	}
 
 }
