 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.match.engine.internal;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.compare.FactoryException;
 import org.eclipse.emf.compare.match.engine.AbstractSimilarityChecker;
 import org.eclipse.emf.compare.match.statistic.MetamodelFilter;
 import org.eclipse.emf.compare.util.EMFCompareMap;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * A Similarity checker using EMF ID's.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class EcoreIDSimilarityChecker extends AbstractSimilarityChecker {
 	/**
 	 * Class keeping references on a couple of EObjects.
 	 * 
 	 * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
 	 */
 	class EObjectCouple {
 		/**
 		 * left element.
 		 */
 		private EObject left;
 
 		/**
 		 * right element.
 		 */
 		private EObject right;
 
 		public EObject getLeft() {
 			return left;
 		}
 
 		public void setLeft(EObject passedLeft) {
 			this.left = passedLeft;
 		}
 
 		public EObject getRight() {
 			return right;
 		}
 
 		public void setRight(EObject passedRight) {
 			this.right = passedRight;
 		}
 
 		/**
 		 * return the other element.
 		 * 
 		 * @param obj1
 		 *            one of the 2 elements wrapped in this instance.
 		 * @return the other element.
 		 */
 		public EObject getOther(EObject obj1) {
 			EObject found = null;
 			if (getLeft() == obj1) {
 				found = getRight();
 			} else if (getRight() == obj1) {
 				found = getLeft();
 			}
 			return found;
 		}
 
 	}
 
 	/**
 	 * This map keep track of the mapping between left EObject and the Right one.
 	 */
 	private final Map<EObject, EObject> leftToRight = new EMFCompareMap<EObject, EObject>();
 
 	/**
 	 * This collection keep track of elements non providing ID's.
 	 */
 	private final Set<EObject> nonIdentified = new LinkedHashSet<EObject>();
 
 	/**
 	 * Delegating checker to ask for similarity when elements are not providing ID's.
 	 */
 	private AbstractSimilarityChecker delegate;
 
 	/**
 	 * Create a new checker.
 	 * 
 	 * @param filter
 	 *            a metamodel filter the checker can use to know whether a feature alwaas has the same value
 	 *            or not in the models.
 	 * @param fallback
 	 *            checker to call if the elements have no ID at all.
 	 */
 	public EcoreIDSimilarityChecker(MetamodelFilter filter, AbstractSimilarityChecker fallback) {
 		super(filter);
 		this.delegate = fallback;
 	}
 
 	/**
 	 * Iterates through both of the given EObjects to find all of their children that can be matched by their
 	 * functional ID, then populates {@link #matchedByID} with those mappings.
 	 * <p>
 	 * </p>
 	 * 
 	 * @param obj1
 	 *            First of the two EObjects to visit.
 	 * @param obj2
 	 *            Second of the EObjects to visit.
 	 * @throws FactoryException
 	 *             Thrown if we couldn't compute a key to store the items in cache.
 	 */
 	@Override
 	public void init(EObject obj1, EObject obj2) throws FactoryException {
 		leftToRight.clear();
 		nonIdentified.clear();
 		final Iterator<EObject> leftIterator = obj1.eAllContents();
 		final Iterator<EObject> rightIterator = obj2.eAllContents();
 		browseComputingId(leftIterator, rightIterator);
 		delegate.init(obj1, obj2);
 	}
 
 	/**
 	 * Browse both given models and compute their ID's.
 	 * 
 	 * @param leftIterator
 	 *            left model.
 	 * @param rightIterator
 	 *            right model.
 	 */
 	private void browseComputingId(final Iterator<EObject> leftIterator, final Iterator<EObject> rightIterator) {
 		final Map<String, EObjectCouple> matchedByID = new EMFCompareMap<String, EObjectCouple>();
 		while (leftIterator.hasNext()) {
 			final EObject item1 = leftIterator.next();
 			final String item1ID = computeID(item1);
 			if (item1ID == null) {
 				nonIdentified.add(item1);
 			} else {
 				final EObjectCouple duo = getOrCreate(matchedByID, item1ID);
 				duo.setLeft(item1);
 			}
 		}
 		while (rightIterator.hasNext()) {
 			final EObject item2 = rightIterator.next();
 			final String item2ID = computeID(item2);
 			if (item2ID == null) {
 				nonIdentified.add(item2);
 			} else {
 				final EObjectCouple duo = getOrCreate(matchedByID, item2ID);
 				duo.setRight(item2);
 			}
 		}
 
 		for (EObjectCouple pair : matchedByID.values()) {
 			if (pair.getLeft() != null && pair.getRight() != null)
 				leftToRight.put(pair.getLeft(), pair.getRight());
 		}
 
 	}
 
 	/**
 	 * Get or create if not already created a new couple with the given ID.
 	 * 
 	 * @param matchedByID
 	 *            map to keep track of the elements and their IDs.
 	 * @param item1id
 	 *            id.
 	 * @return the couple corresonding to the ID.
 	 */
 	private EObjectCouple getOrCreate(Map<String, EObjectCouple> matchedByID, String item1id) {
 		EObjectCouple found = matchedByID.get(item1id);
 		if (found == null) {
 			found = new EObjectCouple();
 			matchedByID.put(item1id, found);
 		}
 		return found;
 	}
 
 	/**
 	 * Iterates through both of the given {@link Resource resources} to find all the elements that can be
 	 * matched by their ID, then populates {@link #matchedByID} with those mappings.
 	 * 
 	 * @param left
 	 *            First of the two {@link Resource resources} to visit.
 	 * @param right
 	 *            Second of the {@link Resource resources} to visit.
 	 * @throws FactoryException
 	 *             Thrown if we couldn't compute a key to store the items in cache.
 	 */
 	@Override
 	public void init(Resource left, Resource right) throws FactoryException {
 		leftToRight.clear();
 		nonIdentified.clear();
 		final Iterator<EObject> leftIterator = left.getAllContents();
 		final Iterator<EObject> rightIterator = right.getAllContents();
 		browseComputingId(leftIterator, rightIterator);
 		delegate.init(left, right);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isSimilar(EObject obj1, EObject obj2) throws FactoryException {
 		if (nonIdentified.contains(obj1) && nonIdentified.contains(obj2))
 			return delegate.isSimilar(obj1, obj2);
 		return leftToRight.get(obj1) == obj2;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public double absoluteMetric(EObject obj1, EObject obj2) throws FactoryException {
		if (nonIdentified.contains(obj1) && nonIdentified.contains(obj2))
			return delegate.absoluteMetric(obj1, obj2);
 		if (leftToRight.get(obj1) == obj2)
 			return 1;
 		return 0;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected String computeID(EObject obj) {
 		return EcoreUtil.getID(obj);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public EObject fastLookup(EObject obj1) {
 		return leftToRight.get(obj1);
 	}
 }
