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
 package org.eclipse.emf.compare.uml2.internal.postprocessor;
 
 import static com.google.common.base.Predicates.instanceOf;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.ResourceAttachmentChange;
 import org.eclipse.emf.compare.internal.postprocessor.factories.AbstractChangeFactory;
 import org.eclipse.emf.compare.uml2.internal.StereotypeApplicationChange;
 import org.eclipse.emf.compare.uml2.internal.UMLDiff;
 import org.eclipse.emf.compare.utils.MatchUtil;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EStructuralFeature.Setting;
 import org.eclipse.emf.ecore.util.Switch;
 import org.eclipse.uml2.common.util.UML2Util;
 import org.eclipse.uml2.uml.util.UMLSwitch;
 
 /**
  * Factory of UML difference extensions.
  * 
  * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
  */
 public abstract class AbstractUMLChangeFactory extends AbstractChangeFactory {
 
 	/**
 	 * UML Switch to get the discriminants (if they exist) related to the given business object.
 	 * 
 	 * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
 	 */
 	protected class DiscriminantsGetter extends UMLSwitch<Set<EObject>> {
 
 		@Override
 		public Set<EObject> defaultCase(EObject object) {
 			return defaultCaseForDiscriminantsGetter(this, object);
 		}
 	}
 
 	/**
 	 * Setting to define a candidate to the seeking of refining differences.
 	 * 
 	 * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
 	 */
 	private class RefiningCandidate implements Setting {
 
 		/** The business object containing the structural feature. */
 		private EObject holdingObject;
 
 		/** The structural feature. */
 		private EStructuralFeature eStructuralFeature;
 
 		/**
 		 * Constructor to use if the origin of the target value is unknown (neither holding object nor
 		 * structural feature). This candidate will be considered as a containment reference to the target
 		 * value.
 		 */
 		public RefiningCandidate() {
 		}
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param holdingObject
 		 *            The business object containing the structural feature to the target value.
 		 * @param feature
 		 *            the structural feature to the target value.
 		 */
 		public RefiningCandidate(EObject holdingObject, EStructuralFeature feature) {
 			this.holdingObject = holdingObject;
 			this.eStructuralFeature = feature;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.EStructuralFeature.Setting#getEObject()
 		 */
 		public EObject getEObject() {
 			return holdingObject;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.EStructuralFeature.Setting#getEStructuralFeature()
 		 */
 		public EStructuralFeature getEStructuralFeature() {
 			return eStructuralFeature;
 		}
 
 		/**
 		 * {@inheritDoc}<br>
 		 * No use. Return null.
 		 * 
 		 * @see org.eclipse.emf.ecore.EStructuralFeature.Setting#get(boolean)
 		 */
 		public Object get(boolean resolve) {
 			return null;
 		}
 
 		/**
 		 * {@inheritDoc}<br>
 		 * No use. Do nothing.
 		 * 
 		 * @see org.eclipse.emf.ecore.EStructuralFeature.Setting#set(java.lang.Object)
 		 */
 		public void set(Object newValue) {
 		}
 
 		/**
 		 * {@inheritDoc}<br>
 		 * No use. Return false.
 		 * 
 		 * @see org.eclipse.emf.ecore.EStructuralFeature.Setting#isSet()
 		 */
 		public boolean isSet() {
 			return false;
 		}
 
 		/**
 		 * {@inheritDoc}<br>
 		 * No use. Do nothing.
 		 * 
 		 * @see org.eclipse.emf.ecore.EStructuralFeature.Setting#unset()
 		 */
 		public void unset() {
 		}
 
 	}
 
 	/**
 	 * It defines the predicate to keep only the differences which match with the given settings (refining
 	 * candidates).
 	 * 
 	 * @author <a href="mailto:cedric.notot@obeo.fr">Cedric Notot</a>
 	 */
 	private class DifferencesOnRefiningCandidates implements Predicate<Diff> {
 
 		/** The comparison. */
 		private final Comparison fComparison;
 
 		/** The specified settings. */
 		private final HashMultimap<Object, RefiningCandidate> fRefiningCandidates;
 
 		/**
 		 * Constructor.
 		 * 
 		 * @param comparison
 		 *            The comparison.
 		 * @param refiningCandidates
 		 *            The specified settings.
 		 */
 		public DifferencesOnRefiningCandidates(Comparison comparison,
 				HashMultimap<Object, RefiningCandidate> refiningCandidates) {
 			fComparison = comparison;
 			fRefiningCandidates = refiningCandidates;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see com.google.common.base.Predicate#apply(java.lang.Object)
 		 */
 		public boolean apply(final Diff input) {
 			boolean result = false;
 			Object value = MatchUtil.getValue(input);
 			Set<RefiningCandidate> settings = fRefiningCandidates.get(value);
 			if (settings.size() > 0) {
 				// Keep the current difference if one specified candidate setting match with it at least.
 				result = Iterables.any(settings, new Predicate<EStructuralFeature.Setting>() {
 					public boolean apply(EStructuralFeature.Setting setting) {
 						boolean res = true;
 						if (setting.getEObject() != null) {
 							// Keep if match of the current difference is the same as the match of the
 							// specified
 							// holding object in the setting...
 							res = input.getMatch() == fComparison.getMatch(setting.getEObject());
 						}
 						if (setting.getEStructuralFeature() != null) {
 							// ... and the structural feature is the same as the specified one in the
 							// setting.
 							res = res
 									&& MatchUtil.getStructuralFeature(input) == setting
 											.getEStructuralFeature();
 						} else {
 							// If no structural feature specified, check that the reference of the
 							// difference is containment.
 							EStructuralFeature diffFeature = MatchUtil.getStructuralFeature(input);
 							res = res && diffFeature instanceof EReference
 									&& ((EReference)diffFeature).isContainment();
 						}
 						return res;
 					}
 				});
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}<br>
 	 * It checks that the given difference concerns the creation of an UML macroscopic change. <br>
 	 * It verifies this difference is not a part of a macroscopic ADD or DELETE not to have a macroscopic
 	 * ADD/DELETE plus a macroscopic CHANGE.<br>
 	 * At last, the first matching difference allows to create a complete macroscopic change (with all the
 	 * refining differences. So, the next matching ones will not be held to avoid to create duplicated
 	 * macroscopic changes.
 	 * 
 	 * @see org.eclipse.emf.compare.internal.postprocessor.factories.AbstractChangeFactory#handles(org.eclipse.emf.compare.Diff)
 	 */
 	@Override
 	public boolean handles(Diff input) {
 		return super.handles(input) && !isChangeOnAddOrDelete(input) && input.getRefines().isEmpty();
 	}
 
 	/**
 	 * {@inheritDoc}<br>
 	 * It creates the macroscopic change and builds it. It sets its discriminant (main business object to
 	 * focus). <br>
 	 * For a macroscopic ADD/DELETE, it sets its eReference (the reference of the main refining difference (on
 	 * the discriminant)).
 	 * 
 	 * @see org.eclipse.emf.compare.internal.postprocessor.factories.IChangeFactory#create(org.eclipse.emf.compare.Diff)
 	 */
 	@Override
 	public Diff create(Diff input) {
 		Diff ret = super.create(input);
 		if (ret instanceof UMLDiff) {
 			((UMLDiff)ret).setDiscriminant(getDiscriminant(input));
 			setEReference(input, (UMLDiff)ret);
 		}
 		return ret;
 	}
 
 	/**
 	 * {@inheritDoc}<br>
 	 * During the building process, it sets the differences refining the macroscopic one.
 	 * 
 	 * @see org.eclipse.emf.compare.internal.postprocessor.factories.AbstractChangeFactory#setRefiningChanges(org.eclipse.emf.compare.Diff,
 	 *      org.eclipse.emf.compare.DifferenceKind, org.eclipse.emf.compare.Diff)
 	 */
 	@Override
 	public void setRefiningChanges(Diff extension, DifferenceKind extensionKind, Diff refiningDiff) {
 		HashMultimap<Object, RefiningCandidate> refiningCandidates = HashMultimap.create();
 
 		Comparison comparison = refiningDiff.getMatch().getComparison();
 		// From each discriminant business object, ...
 		Set<EObject> discriminants = getDiscriminants(refiningDiff);
 		for (EObject discriminant : discriminants) {
 			// ... define all the business objects which may be impacted by refining differences specifying
 			// the
 			// settings (holding object and structural feature) which link them.
 			defineRefiningCandidates(discriminant, refiningCandidates);
 			// For each of these business objects, find the impacted differences, keeping only the ones
 			// matching the defined settings.
 			for (Object elt : refiningCandidates.keys()) {
 				beRefinedByCrossReferences(comparison, elt, (UMLDiff)extension,
 						new DifferencesOnRefiningCandidates(comparison, refiningCandidates));
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.internal.postprocessor.factories.IChangeFactory#getParentMatch(org.eclipse.emf.compare.Diff)
 	 */
 	@Override
 	public Match getParentMatch(Diff input) {
 		return getParentMatch(input.getMatch().getComparison(), input);
 	}
 
 	/**
 	 * Get the discriminant business objects concerned by the given difference.<br>
 	 * 
 	 * @param input
 	 *            The given difference.
 	 * @return The set of discriminant business objects.
 	 */
 	protected Set<EObject> getDiscriminants(Diff input) {
 		EObject value;
 		// Get the business object to focus as starting point to find all the discriminant objects.
 		if (input instanceof ReferenceChange && ((ReferenceChange)input).getReference().isContainment()) {
 			value = ((ReferenceChange)input).getValue();
 		} else {
 			value = MatchUtil.getContainer(input.getMatch().getComparison(), input);
 		}
 		return getDiscriminants(value);
 	}
 
 	/**
 	 * Get the cross referenced object (setting) matching with given predicate, from the given business
 	 * object.
 	 * 
 	 * @param object
 	 *            The given object from which we seek a cross referenced object.
 	 * @param predicate
 	 *            The predicate.
 	 * @return the setting containing the cross referenced object.
 	 */
 	protected Setting getInverseReferences(EObject object, Predicate<EStructuralFeature.Setting> predicate) {
 		final Iterator<EStructuralFeature.Setting> crossReferences = UML2Util.getInverseReferences(object)
 				.iterator();
 		return Iterators.find(crossReferences, predicate, null);
 	}
 
 	/**
 	 * {@inheritDoc}<br>
 	 * The given reference change is related to a macroscopic ADD if... <br>
 	 * - Its reference is a containment one<br>
 	 * - Its value is the specified discriminant<br>
 	 * - Its kind is ADD
 	 * 
 	 * @see org.eclipse.emf.compare.internal.postprocessor.factories.AbstractChangeFactory#isRelatedToAnExtensionAdd(org.eclipse.emf.compare.ReferenceChange)
 	 */
 	@Override
 	protected boolean isRelatedToAnExtensionAdd(ReferenceChange input) {
 		return input.getReference().isContainment() && input.getValue() == getDiscriminant(input)
 				&& input.getKind() == DifferenceKind.ADD;
 	}
 
 	/**
 	 * {@inheritDoc}<br>
 	 * The given reference change is related to a macroscopic DELETE if... <br>
 	 * - Its reference is a containment one<br>
 	 * - Its value is the specified discriminant<br>
 	 * - Its kind is DELETE
 	 * 
 	 * @see org.eclipse.emf.compare.internal.postprocessor.factories.AbstractChangeFactory#isRelatedToAnExtensionDelete(org.eclipse.emf.compare.ReferenceChange)
 	 */
 	@Override
 	protected boolean isRelatedToAnExtensionDelete(ReferenceChange input) {
 		return input.getReference().isContainment() && input.getValue() == getDiscriminant(input)
 				&& input.getKind() == DifferenceKind.DELETE;
 	}
 
	// No UML macroscopic change anymore for any changes on discriminants.
	// /**
	// * {@inheritDoc}<br>
	// * The given reference change is related to a macroscopic CHANGE if discriminants are found from the
	// * business impacted object.
	// *
	// * @see
	// org.eclipse.emf.compare.internal.postprocessor.factories.AbstractChangeFactory#isRelatedToAnExtensionChange(org.eclipse.emf.compare.ReferenceChange)
	// */
	// @Override
	// protected boolean isRelatedToAnExtensionChange(ReferenceChange input) {
	// return !getDiscriminants(MatchUtil.getContainer(input.getMatch().getComparison(), input)).isEmpty();
	// }
 
 	// TODO: Add isRelatedToAnExtensionChange(AttributeChange input)
 
 	/**
 	 * Get the switch which allows to return the set of discriminants from any business object.<br>
 	 * 
 	 * @return The switch to get the discriminants.
 	 */
 	protected abstract Switch<Set<EObject>> getDiscriminantsGetter();
 
 	/**
 	 * Get the main discriminant business object concerned by the given difference. Usually, it will be one of
 	 * the ones returned by {@link AbstractUMLChangeFactory#getDiscriminants(Diff)}
 	 * 
 	 * @param input
 	 *            The difference.
 	 * @return The discriminant.
 	 */
 	protected abstract EObject getDiscriminant(Diff input);
 
 	/**
 	 * Default method in discriminant getters ({@link DiscriminantsGetter}) to find discriminants.
 	 * 
 	 * @param discriminantsGetter
 	 *            The specific discriminant getter.
 	 * @param object
 	 *            The current object as starting point to the seeking of discriminants.
 	 * @return The set of discriminants.
 	 */
 	protected static Set<EObject> defaultCaseForDiscriminantsGetter(Switch<Set<EObject>> discriminantsGetter,
 			EObject object) {
 		Set<EObject> result = new HashSet<EObject>();
 		EObject parent = object.eContainer();
 		if (parent != null) {
 			result.addAll(discriminantsGetter.doSwitch(parent));
 		}
 		return result;
 	}
 
 	/**
 	 * Get the discriminant business objects from the given one.
 	 * 
 	 * @param value
 	 *            The given business object.
 	 * @return The set of discriminants.
 	 */
 	private Set<EObject> getDiscriminants(EObject value) {
 		Switch<Set<EObject>> discriminantGetter = getDiscriminantsGetter();
 		return discriminantGetter.doSwitch(value);
 	}
 
 	/**
 	 * It defines the business objects to scan and their settings to find the matching refining differences,
 	 * from the given discriminant business object.
 	 * 
 	 * @param discriminant
 	 *            The given discriminant.
 	 * @param refiningCandidates
 	 *            Map business object to the list of settings to keep during the research of refining
 	 *            differences. This map must not be null.
 	 */
 	private void defineRefiningCandidates(EObject discriminant,
 			HashMultimap<Object, RefiningCandidate> refiningCandidates) {
 		// The discriminant itself is a candidate, only on an incoming containment reference.
 		refiningCandidates.put(discriminant, new RefiningCandidate());
 		// Delegation to a recursive method to find the other candidates.
 		defineRefiningCandidatesFrom(discriminant, refiningCandidates);
 	}
 
 	/**
 	 * It defines the business objects to scan and their settings to find the matching refining differences,
 	 * from the given discriminant business object. It ignores the given object and searches candidates in all
 	 * the referenced objects from it and all the attributes. A recursion is made between containment
 	 * references, to scan all the children. Opposite references are taken into account for the candidates.
 	 * 
 	 * @param discriminant
 	 *            The given discriminant.
 	 * @param refiningCandidates
 	 *            Map business object to the list of settings to keep during the research of refining
 	 *            differences. This map must not be null.
 	 */
 	private void defineRefiningCandidatesFrom(EObject discriminant,
 			HashMultimap<Object, RefiningCandidate> refiningCandidates) {
 		Iterator<EStructuralFeature> outgoingFeatures = discriminant.eClass().getEAllStructuralFeatures()
 				.iterator();
 		while (outgoingFeatures.hasNext()) {
 			EStructuralFeature outgoingFeature = outgoingFeatures.next();
 			// For each referenced objects and attributes...
 			Iterator<Object> values = ReferenceUtil.getAsList(discriminant, outgoingFeature).iterator();
 			while (values.hasNext()) {
 				Object value = values.next();
 				// ... register it with its setting.
 				refiningCandidates.put(value, new RefiningCandidate(discriminant, outgoingFeature));
 				if (outgoingFeature instanceof EReference && value instanceof EObject) {
 					if (((EReference)outgoingFeature).isContainment()) {
 						// Recursion on children
 						defineRefiningCandidatesFrom((EObject)value, refiningCandidates);
 					} else if (((EReference)outgoingFeature).getEOpposite() != null) {
 						// Take opposite references
 						refiningCandidates.put(discriminant, new RefiningCandidate((EObject)value,
 								((EReference)outgoingFeature).getEOpposite()));
 					}
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Get the match in which the macroscopic change should be added.<br>
 	 * Take the same match of the given difference. If it is related to a macroscopic CHANGE, take the match
 	 * of the discriminant.
 	 * 
 	 * @param comparison
 	 *            The current comparison.
 	 * @param input
 	 *            The difference to locate.
 	 * @return The containing match.
 	 */
 	private Match getParentMatch(Comparison comparison, Diff input) {
 		if (getRelatedExtensionKind(input) == DifferenceKind.CHANGE) {
 			return comparison.getMatch(getDiscriminant(input));
 		} else {
 			return input.getMatch();
 		}
 	}
 
 	/**
 	 * Fill the refining link of the given refined extension (macroscopic change) with the found differences
 	 * on the given object (lookup), according to the given predicate.
 	 * 
 	 * @param comparison
 	 *            The comparison.
 	 * @param lookup
 	 *            The object on which differences have to be found.
 	 * @param refinedExtension
 	 *            The macroscopic change to set (refinedBy link)
 	 * @param p
 	 *            The predicate.
 	 */
 	private void beRefinedByCrossReferences(Comparison comparison, Object lookup, UMLDiff refinedExtension,
 			Predicate<Diff> p) {
 		if (lookup instanceof EObject) {
 			List<Diff> crossReferences = findCrossReferences(comparison, (EObject)lookup, p);
 			refinedExtension.getRefinedBy().addAll(crossReferences);
 		}
 	}
 
 	/**
 	 * Set the eReference link in the given UML difference, from the given unit difference.
 	 * 
 	 * @param input
 	 *            The unit difference.
 	 * @param umlDiff
 	 *            The UML difference (macroscopic change).
 	 */
 	private void setEReference(Diff input, UMLDiff umlDiff) {
 		if (getRelatedExtensionKind(input) == DifferenceKind.ADD
 				|| getRelatedExtensionKind(input) == DifferenceKind.DELETE) {
 			if (input instanceof ReferenceChange) {
 				umlDiff.setEReference(((ReferenceChange)input).getReference());
 			} else if (input instanceof ResourceAttachmentChange
 					&& umlDiff instanceof StereotypeApplicationChange) {
 				// the resource attachment concerns the stereotype application itself.
 				// The reference is located "below" that.
 				final List<Diff> candidates = input.getMatch().getDifferences();
 				// Little chance that there is more is that the input ... and what we seek.
 				for (Diff candidate : candidates) {
 					if (candidate instanceof ReferenceChange) {
 						umlDiff.setEReference(((ReferenceChange)candidate).getReference());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * It checks if the given difference concerns is related to a macroscopic CHANGE within a macroscopic
 	 * ADD/DELETE.
 	 * 
 	 * @param input
 	 *            The difference.
 	 * @return True if it is related to a CHANGE in an ADD/DELETE.
 	 */
 	private boolean isChangeOnAddOrDelete(Diff input) {
 		if (getRelatedExtensionKind(input) == DifferenceKind.CHANGE) {
 			final Comparison comparison = input.getMatch().getComparison();
 			final EObject discriminant = getDiscriminant(input);
 			if (discriminant != null) {
 				return isChangeOnAddOrDelete(input, comparison, discriminant);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * It checks if the given difference concerns is related to a macroscopic CHANGE within a macroscopic
 	 * ADD/DELETE.
 	 * 
 	 * @param input
 	 *            The given difference.
 	 * @param comparison
 	 *            the related comparison.
 	 * @param discriminant
 	 *            The discriminant found for the given difference.
 	 * @return True if it is related to a CHANGE in an ADD/DELETE.
 	 */
 	private boolean isChangeOnAddOrDelete(Diff input, final Comparison comparison, final EObject discriminant) {
 		boolean result = false;
 		if (Iterables.any(comparison.getMatch(discriminant).getDifferences(),
 				instanceOf(ResourceAttachmentChange.class))) {
 			result = true;
 		}
 		if (!result) {
 			final List<Diff> candidates = comparison.getDifferences(discriminant);
 			for (Diff diff : candidates) {
 				if (diff == input) {
 					// ignore this one
 				} else {
 					DifferenceKind relatedExtensionKind = getRelatedExtensionKind(diff);
 					if ((relatedExtensionKind == DifferenceKind.ADD || relatedExtensionKind == DifferenceKind.DELETE)
 							&& getDiscriminant(diff) == discriminant) {
 						result = true;
 						break;
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 }
