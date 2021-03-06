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
 package org.eclipse.emf.compare.match.eobject;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.compare.CompareFactory;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.DifferenceSource;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.diff.DefaultDiffEngine;
 import org.eclipse.emf.compare.diff.FeatureFilter;
 import org.eclipse.emf.compare.diff.IDiffProcessor;
 import org.eclipse.emf.compare.match.DefaultComparisonFactory;
 import org.eclipse.emf.compare.match.IComparisonFactory;
 import org.eclipse.emf.compare.match.IEqualityHelperFactory;
 import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.compare.utils.EqualityHelper;
 import org.eclipse.emf.compare.utils.IEqualityHelper;
 import org.eclipse.emf.compare.utils.ReferenceUtil;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * This distance function implementation will actually compare the given EObject.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class EditionDistance implements DistanceFunction {
 
 	/**
 	 * Weight coefficient of a change on a reference.
 	 */
 	private int referenceChangeCoef = 10;
 
 	/**
 	 * Weight coefficient of a change on an attribute.
 	 */
 	private int attributeChangeCoef = 10 + 10;
 
 	/**
 	 * Weight coefficient of a change of location (uri).
 	 */
 	private int locationChangeCoef = 4;
 
 	/**
 	 * Weight coefficient of a change of order within a reference.
 	 */
 	private int orderChangeCoef = 5;
 
 	/**
 	 * The list of specific weight to apply on specific Features.
 	 */
 	private Map<EStructuralFeature, Integer> weights;
 
 	/**
 	 * The list of features to ignore during the distance computation.
 	 */
 	private Set<EStructuralFeature> toBeIgnored;
 
 	/**
 	 * The instance used to compare location of EObjects.
 	 */
 	private URIDistance uriDistance = new URIDistance();
 
 	/**
 	 * The equality helper used to retrieve the URIs through its cache and to instanciate a specific diff
 	 * engine.
 	 */
 	private EqualityHelper helper;
 
 	/**
 	 * Instanciate a new Edition Distance.
 	 */
 	public EditionDistance() {
 		weights = Maps.newHashMap();
 		this.helper = new EqualityHelper() {
 			@Override
 			protected boolean matchingEObjects(EObject object1, EObject object2) {
 				final Match match = getTarget().getMatch(object1);
 
 				final boolean equal;
 				// Match could be null if the value is out of the scope
 				if (match != null) {
 					equal = match.getLeft() == object2 || match.getRight() == object2
 							|| match.getOrigin() == object2;
 				} else {
 					/*
 					 * use a temporary variable as buffer for the "equal" boolean. We know that the following
 					 * try/catch block can, and will, only initialize it once ... but the compiler does not.
 					 */
 					equal = uriDistance.proximity(object1, object2) == 0;
 				}
 
 				return equal;
 			}
 
 		};
 		this.toBeIgnored = Sets.newLinkedHashSet();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int distance(EObject a, EObject b) {
 		int maxDist = Math.max(getMaxDistance(a), getMaxDistance(b));
 		int measuredDist = new CountingDiffEngine(maxDist).measureDifferences(a, b);
 		if (measuredDist >= maxDist) {
 			return Integer.MAX_VALUE;
 		}
 		return measuredDist;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean areIdentic(EObject a, EObject b) {
 		return new CountingDiffEngine(0).measureDifferences(a, b) == 0;
 	}
 
 	/**
 	 * Create a new builder to instantiate and configure an EditionDistance.
 	 * 
 	 * @return a configuration builder.
 	 */
 	public static Builder builder() {
 		return new Builder();
 	}
 
 	/**
 	 * Builder class to configure an EditionDistance instance.
 	 */
 	public static class Builder {
 		/**
 		 * The EditionDistance built by the builder.
 		 */
 		private EditionDistance toBeBuilt;
 
 		/**
 		 * Create the builder.
 		 */
 		protected Builder() {
 			this.toBeBuilt = new EditionDistance();
 		}
 
 		/**
 		 * Specify a weight for a given feature.
 		 * 
 		 * @param feat
 		 *            the feature to customize.
 		 * @param weight
 		 *            the weight, it will be multiplied by the type of change coefficient.
 		 * @return the current builder instance.
 		 */
 		public Builder weight(EStructuralFeature feat, Integer weight) {
 			this.toBeBuilt.weights.put(feat, weight);
 			return this;
 		}
 
 		/**
 		 * Specify a feature to ignore during the measure.
 		 * 
 		 * @param featToIgnore
 		 *            the feature to ignore.
 		 * @return the current builder instance.
 		 */
 		public Builder ignore(EStructuralFeature featToIgnore) {
 			this.toBeBuilt.toBeIgnored.add(featToIgnore);
 			return this;
 		}
 
 		/**
 		 * Specify the weight of any change of uri between two instances.
 		 * 
 		 * @param weight
 		 *            the new weight.
 		 * @return the current builder instance.
 		 */
 		public Builder uri(int weight) {
 			this.toBeBuilt.locationChangeCoef = weight;
 			return this;
 		}
 
 		/**
 		 * Specify the weight of any change of reference order between two instances.
 		 * 
 		 * @param weight
 		 *            the new weight.
 		 * @return the current builder instance.
 		 */
 
 		public Builder order(int weight) {
 			this.toBeBuilt.orderChangeCoef = weight;
 			return this;
 		}
 
 		/**
 		 * Specify the weight of any change of attribute value between two instances.
 		 * 
 		 * @param weight
 		 *            the new weight.
 		 * @return the current builder instance.
 		 */
 
 		public Builder attribute(int weight) {
 			this.toBeBuilt.attributeChangeCoef = weight;
 			return this;
 		}
 
 		/**
 		 * Specify the weight of any change of reference between two instances.
 		 * 
 		 * @param weight
 		 *            the new weight.
 		 * @return the current builder instance.
 		 */
 
 		public Builder reference(int weight) {
 			this.toBeBuilt.referenceChangeCoef = weight;
 			return this;
 		}
 
 		/**
 		 * return the configured instance.
 		 * 
 		 * @return the configured instance.
 		 */
 		public EditionDistance build() {
 			return toBeBuilt;
 		}
 	}
 
 	/**
 	 * This class is an implementation of a {@link IDiffProcessor} which counts the number of differences to
 	 * given an overall distance between two objects.
 	 */
 	class CountingDiffProcessor implements IDiffProcessor {
 		/**
 		 * The current distance.
 		 */
 		private int distance;
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void referenceChange(Match match, EReference reference, EObject value, DifferenceKind kind,
 				DifferenceSource source) {
			switch (kind) {
				case MOVE:
					distance += getWeight(reference) * orderChangeCoef;
					break;
				case ADD:
				case DELETE:
				case CHANGE:
					distance += getWeight(reference) * referenceChangeCoef;
					break;
				default:
					break;
			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void attributeChange(Match match, EAttribute attribute, Object value, DifferenceKind kind,
 				DifferenceSource source) {
 			Object aValue = ReferenceUtil.safeEGet(match.getLeft(), attribute);
 			Object bValue = ReferenceUtil.safeEGet(match.getRight(), attribute);
 			switch (kind) {
 				case MOVE:
 					distance += getWeight(attribute) * orderChangeCoef;
 					break;
 				case ADD:
 				case DELETE:
 				case CHANGE:
 					if (aValue instanceof String && bValue instanceof String) {
 						distance += getWeight(attribute)
 								* (1 - DiffUtil.diceCoefficient((String)aValue, (String)bValue))
 								* attributeChangeCoef;
 					} else {
 						distance += getWeight(attribute) * attributeChangeCoef;
 					}
 					break;
 				default:
 					break;
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.compare.diff.IDiffProcessor#resourceAttachmentChange(org.eclipse.emf.compare.Match,
 		 *      java.lang.String, org.eclipse.emf.compare.DifferenceKind,
 		 *      org.eclipse.emf.compare.DifferenceSource)
 		 */
 		public void resourceAttachmentChange(Match match, String uri, DifferenceKind kind,
 				DifferenceSource source) {
 			// Not important for the distance computation
 		}
 
 		/**
 		 * return the computed distance.
 		 * 
 		 * @return the computed distance.
 		 */
 		public int getComputedDistance() {
 			return distance;
 		}
 
 	}
 
 	/**
 	 * Return the weight for the given feature.
 	 * 
 	 * @param attribute
 	 *            any {@link EStructuralFeature}.
 	 * @return the weight for the given feature.
 	 */
 	private int getWeight(EStructuralFeature attribute) {
 		Integer found = weights.get(attribute);
 		if (found == null) {
 			if ("name".equals(attribute.getName())) { //$NON-NLS-1$
 				found = Integer.valueOf(3);
 			} else {
 				found = Integer.valueOf(1);
 			}
 		}
 		return found.intValue();
 	}
 
 	/**
 	 * An implementation of a diff engine which count and measure the detected changes.
 	 */
 	class CountingDiffEngine extends DefaultDiffEngine {
 		/**
 		 * The maximum distance until which we just have to stop.
 		 */
 		private int maxDistance;
 
 		/**
 		 * The comparison factory to create fake comparison.
 		 */
 		private final IComparisonFactory fakeComparisonFactory;
 
 		/**
 		 * Create the diff engine.
 		 * 
 		 * @param maxDistance
 		 *            the maximum distance we might reach.
 		 */
 		public CountingDiffEngine(int maxDistance) {
 			super(new CountingDiffProcessor());
 			this.maxDistance = maxDistance;
 			// will always return the same instance.
 			IEqualityHelperFactory fakeEqualityHelperFactory = new IEqualityHelperFactory() {
 				public IEqualityHelper createEqualityHelper() {
 					return EditionDistance.this.helper;
 				}
 			};
 			fakeComparisonFactory = new DefaultComparisonFactory(fakeEqualityHelperFactory);
 		}
 
 		@Override
 		protected void computeDifferences(Match match, EAttribute attribute, boolean checkOrdering) {
 			if (getCounter().getComputedDistance() <= maxDistance) {
 				super.computeDifferences(match, attribute, checkOrdering);
 			}
 		}
 
 		@Override
 		protected void computeDifferences(Match match, EReference reference, boolean checkOrdering) {
 			if (getCounter().getComputedDistance() <= maxDistance) {
 				super.computeDifferences(match, reference, checkOrdering);
 			}
 		}
 
 		/**
 		 * Measure the difference between two objects and return a distance value.
 		 * 
 		 * @param a
 		 *            first object.
 		 * @param b
 		 *            second object.
 		 * @return the distance between them computed using the number of changes required to change a to b.
 		 */
 		public int measureDifferences(EObject a, EObject b) {
 			Match fakeMatch = createMockMatch(a, b);
 			int changes = 0;
 			int dist = uriDistance.proximity(a, b);
 			changes += dist * locationChangeCoef;
 			if (changes <= maxDistance) {
 				checkForDifferences(fakeMatch, new BasicMonitor());
 				changes += getCounter().getComputedDistance();
 			}
 			// System.err.println(changes + ":max=>" + maxDistance + ":" + a + ":" + b);
 			return changes;
 
 		}
 
 		/**
 		 * Create a mock {@link Match} between the two given EObjects so that we can use the exposed
 		 * {@link #checkForDifferences(Match, org.eclipse.emf.common.util.Monitor)} method to check for
 		 * differences.
 		 * 
 		 * @param a
 		 *            First of the two EObjects for which we want to force a comparison.
 		 * @param b
 		 *            Second of the two EObjects for which we want to force a comparison.
 		 * @return The created Match.
 		 */
 		private Match createMockMatch(EObject a, EObject b) {
 			Comparison fakeComparison = fakeComparisonFactory.createComparison();
 			Match fakeMatch = CompareFactory.eINSTANCE.createMatch();
 			((InternalEList<Match>)fakeComparison.getMatches()).addUnique(fakeMatch);
 			fakeMatch.setLeft(a);
 			fakeMatch.setRight(b);
 			return fakeMatch;
 		}
 
 		protected CountingDiffProcessor getCounter() {
 			return (CountingDiffProcessor)getDiffProcessor();
 		}
 
 		@Override
 		protected FeatureFilter createFeatureFilter() {
 			return new FeatureFilter() {
 
 				@Override
 				public Iterator<EReference> getReferencesToCheck(Match match) {
 					return Iterators.filter(super.getReferencesToCheck(match), new Predicate<EReference>() {
 
 						public boolean apply(EReference input) {
 							return !toBeIgnored.contains(input) && !input.isContainment();
 						}
 					});
 				}
 
 				@Override
 				public Iterator<EAttribute> getAttributesToCheck(Match match) {
 					return Iterators.filter(super.getAttributesToCheck(match), new Predicate<EAttribute>() {
 
 						public boolean apply(EAttribute input) {
 							return !toBeIgnored.contains(input);
 						}
 					});
 				}
 
 			};
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getMaxDistance(EObject eObj) {
 
 		Predicate<EStructuralFeature> featureFilter = new Predicate<EStructuralFeature>() {
 
 			public boolean apply(EStructuralFeature feat) {
 				return !feat.isDerived() && !feat.isTransient() && !toBeIgnored.contains(feat);
 			}
 		};
 		// When can you safely says these are not the same EObjects *at all* ?
 		// lets consider every feature which is set, and add this in the max distance.
 		// and then tweak the max value adding half a location change
 		// thats very empirical... and might be wrong in the end, but it gives pretty good results with
 		// Ecore so I'll try to gather as much as test data I can and add the corresponding test to be able to
 		// assess the quality of further changes.
 		int max = 0;
 		for (EReference feat : Iterables.filter(eObj.eClass().getEAllReferences(), featureFilter)) {
 			if (!feat.isContainer() && !feat.isContainment() && eObj.eIsSet(feat)) {
 				max += getWeight(feat) * referenceChangeCoef;
 			}
 		}
 		for (EAttribute feat : Iterables.filter(eObj.eClass().getEAllAttributes(), featureFilter)) {
 			if (eObj.eIsSet(feat)) {
 				max += getWeight(feat) * attributeChangeCoef;
 			}
 		}
 		max = max + locationChangeCoef * 5;
		return Double.valueOf(max / 3 * 2.5).intValue();
 	}
 
 }
