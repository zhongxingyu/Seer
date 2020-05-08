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
 package org.eclipse.emf.compare.match.eobject.internal;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.match.eobject.EObjectIndex;
 import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher;
 import org.eclipse.emf.compare.match.eobject.ScopeQuery;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
 
 /**
  * This class is responsible for holding several version of EObjects from sides left, right or origins and
  * provides the ability to return the closest instance (from a difference side) of a given EObject. The
  * implementation expects that the queried EObjects have all been indexed before a query is done. The
  * implementation also expects that when you're done with an EObject and if you don't want to get it back in
  * the result of subsequent queries, the EObject is removed. <br>
  * The scalability of this class will highly depend on the given distance function, especially with calls
  * having a max distance of 0. <br>
  * It is also based on the assumption that it is <b> very likely that the EObject has another version with e
  * distance of value 0</b> in the other versions sets.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class ProximityIndex implements EObjectIndex {
	/**
	 * Null object pattern, this object is returned when the index had to backtrack and as such could not even
	 * try to find the match.
	 */
	private static final EObject BACKTRACK = EcoreFactory.eINSTANCE.createEClass();
 
 	/**
 	 * The distance function used to compare the Objects.
 	 */
 	private ProximityEObjectMatcher.DistanceFunction meter;
 
 	/**
 	 * the left objects still present in the index.
 	 */
 	private List<EObject> lefts;
 
 	/**
 	 * the right objects still present in the index.
 	 */
 	private List<EObject> rights;
 
 	/**
 	 * the origin objects still present in the index.
 	 */
 	private List<EObject> origins;
 
 	/**
 	 * An object able to tell us whether an object is in the scope or not.
 	 */
 	private ScopeQuery scope;
 
 	/**
 	 * An object to gather statistics while matching.
 	 */
 	private ProximityMatchStats stats = new ProximityMatchStats();
 
 	/**
 	 * Create a new {@link ProximityIndex} using the given distance function.
 	 * 
 	 * @param meter
 	 *            the distance function to use to compare the EObjects.
 	 * @param matcher
 	 *            the object used to know if an instance is in the scope or not.
 	 */
 	public ProximityIndex(ProximityEObjectMatcher.DistanceFunction meter, ScopeQuery matcher) {
 		this.meter = meter;
 		this.lefts = Lists.newArrayList();
 		this.rights = Lists.newArrayList();
 		this.origins = Lists.newArrayList();
 		this.scope = matcher;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 
 	public Map<Side, EObject> findClosests(Comparison inProgress, EObject eObj, Side passedObjectSide) {
 		if (!readyForThisTest(inProgress, eObj)) {
 			return null;
 		}
 		Map<Side, EObject> result = new HashMap<EObjectIndex.Side, EObject>(3);
 		result.put(passedObjectSide, eObj);
 		if (passedObjectSide == Side.LEFT) {
 			EObject closestRight = findTheClosest(inProgress, eObj, Side.LEFT, Side.RIGHT, true);
 			EObject closestOrigin = findTheClosest(inProgress, eObj, Side.LEFT, Side.ORIGIN, true);
 			result.put(Side.RIGHT, closestRight);
 			result.put(Side.ORIGIN, closestOrigin);
 		} else if (passedObjectSide == Side.RIGHT) {
 			EObject closestLeft = findTheClosest(inProgress, eObj, Side.RIGHT, Side.LEFT, true);
 			EObject closestOrigin = findTheClosest(inProgress, eObj, Side.RIGHT, Side.ORIGIN, true);
 			result.put(Side.LEFT, closestLeft);
 			result.put(Side.ORIGIN, closestOrigin);
 
 		} else if (passedObjectSide == Side.ORIGIN) {
 			EObject closestLeft = findTheClosest(inProgress, eObj, Side.ORIGIN, Side.LEFT, true);
 			EObject closestRight = findTheClosest(inProgress, eObj, Side.ORIGIN, Side.RIGHT, true);
 			result.put(Side.LEFT, closestLeft);
 			result.put(Side.RIGHT, closestRight);
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * return the closest EObject of the passed one found in the sideToFind storage.
 	 * 
 	 * @param inProgress
 	 *            the comparison under match.
 	 * @param eObj
 	 *            the base EObject.
 	 * @param originalSide
 	 *            the side of the base EObject.
 	 * @param sideToFind
 	 *            the side to search in.
 	 * @param shouldDoubleCheck
 	 *            true if we should make sure that the found EObject has the inverse relationship with the
 	 *            base one.
 	 * @return the closest EObject of the passed one found in the sideToFind storage.
 	 */
 	private EObject findTheClosest(Comparison inProgress, final EObject eObj, final Side originalSide,
 			final Side sideToFind, boolean shouldDoubleCheck) {
 		List<EObject> storageToSearchFor = lefts;
 		switch (sideToFind) {
 			case RIGHT:
 				storageToSearchFor = rights;
 				break;
 			case LEFT:
 				storageToSearchFor = lefts;
 				break;
 			case ORIGIN:
 				storageToSearchFor = origins;
 				break;
 
 			default:
 				break;
 		}
 		/*
 		 * We are starting by looking for EObject having a distance of 0. It means we'll iterate two times in
 		 * the worst case but it is very likely that the EObject has another version with a distance of 0. It
 		 * is also based on the assumption that calling distance() with a 0 max distance triggers shortcuts
 		 * and is faster than calling the same distance() method with a max_distance > 0.
 		 */
 		Candidate best = findIdenticMatch(inProgress, eObj, storageToSearchFor);
 		if (best.some()) {
 			return best.eObject;
 		}
 
 		SortedMap<Double, EObject> candidates = Maps.newTreeMap();
 		/*
 		 * We could not find an EObject which is identical, let's search again and find the closest EObject.
 		 */
 		Iterator<EObject> it = storageToSearchFor.iterator();
 		while (best.distance != 0 && it.hasNext()) {
 			EObject potentialClosest = it.next();
 			double dist = meter.distance(inProgress, eObj, potentialClosest);
 			stats.similarityCompare();
 			if (dist < best.distance) {
 				if (shouldDoubleCheck) {
 					// We need to double check the currentlyDigging has the same object as the closest !
 					candidates.put(Double.valueOf(dist), potentialClosest);
 				} else {
 					best.distance = dist;
 					best.eObject = potentialClosest;
 				}
 			}
 		}
 		if (shouldDoubleCheck) {
 			for (Entry<Double, EObject> entry : candidates.entrySet()) {
 				EObject doubleCheck = findTheClosest(inProgress, entry.getValue(), sideToFind, originalSide,
 						false);
 				stats.doubleCheck();
 				if (doubleCheck == eObj) {
 					stats.similaritySuccess();
 					best.eObject = entry.getValue();
					best.distance = Double.valueOf(entry.getKey());
 					break;
 				} else {
 					stats.failedDoubleCheck();
 				}
 			}
 		}
 
 		if (!best.some()) {
 			stats.noMatch();
 		}
 		return best.eObject;
 	}
 
 	/**
 	 * Look for a perfect match (identic content) in the given list of candidates.
 	 * 
 	 * @param inProgress
 	 *            the comparison being matched.
 	 * @param eObj
 	 *            the object
 	 * @param candidates
 	 *            the list of possible matches.
 	 * @return a candidate instance wrapping the found match Object (if found)
 	 */
 	private Candidate findIdenticMatch(Comparison inProgress, final EObject eObj, List<EObject> candidates) {
 		Iterator<EObject> it = candidates.iterator();
 		Candidate best = new Candidate();
 
 		while (it.hasNext() && !best.some()) {
 			EObject fastCheck = it.next();
 			if (!readyForThisTest(inProgress, fastCheck)) {
 				stats.backtrack();
				best.eObject = BACKTRACK;
 			} else {
 				stats.identicCompare();
 				if (meter.areIdentic(inProgress, eObj, fastCheck)) {
 					stats.identicSuccess();
 					best.eObject = fastCheck;
 					best.distance = 0;
 				}
 			}
 		}
 		return best;
 	}
 
 	/**
 	 * Check whether we have all the required information to search for this object matches.
 	 * 
 	 * @param inProgress
 	 *            the Comparison which is being matched.
 	 * @param fastCheck
 	 *            the Object we are trying to match.
 	 * @return true if we have all the required information, false otherwise.
 	 */
 	private boolean readyForThisTest(Comparison inProgress, EObject fastCheck) {
		if (fastCheck.eContainer() != null) {
			return inProgress.getMatch(fastCheck.eContainer()) != null
					|| scope.isInScope(fastCheck.eContainer());
 		}
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void remove(EObject obj, Side side) {
 		switch (side) {
 			case RIGHT:
 				rights.remove(obj);
 				break;
 			case LEFT:
 				lefts.remove(obj);
 				break;
 
 			case ORIGIN:
 				origins.remove(obj);
 				break;
 
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void index(EObject eObject, Side side) {
 		switch (side) {
 			case RIGHT:
 				rights.add(eObject);
 				break;
 			case LEFT:
 				lefts.add(eObject);
 				break;
 			case ORIGIN:
 				origins.add(eObject);
 				break;
 
 			default:
 				break;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Collection<EObject> getValuesStillThere(final Side side) {
 		Collection<EObject> result = Collections.emptyList();
 		switch (side) {
 			case RIGHT:
 				result = ImmutableList.copyOf(rights);
 				break;
 			case LEFT:
 				result = ImmutableList.copyOf(lefts);
 				break;
 			case ORIGIN:
 				result = ImmutableList.copyOf(origins);
 				break;
 			default:
 				break;
 		}
 		return result;
 
 	}
 
 	/**
 	 * A Wrapper class to keep an {@link EObject} aside with a distance.
 	 * 
 	 * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
 	 */
	class Candidate {
 		/**
 		 * an EObject.
 		 */
		EObject eObject;
 
 		/**
 		 * A distance.
 		 */
		double distance = Double.MAX_VALUE;
 
 		/**
 		 * return true of the candidate has an {@link EObject}.
 		 * 
 		 * @return true of the candidate has an {@link EObject}.
 		 */
 		public boolean some() {
 			return eObject != null;
 		}
 	}
 
 }
