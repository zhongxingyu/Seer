 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.match.statistic.similarity;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.compare.match.statistic.MetamodelFilter;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.FactoryException;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 /**
  * Utilities for structure comparison.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public final class StructureSimilarity {
 	private StructureSimilarity() {
 		// prevents instantiation
 	}
 
 	/**
 	 * This method returns a double comprised between 0 and 1 representing the type similarities of the 2
 	 * EObjects. - 1 means the 2 objects types are very similar - 0 means the 2 objects types are very
 	 * differents.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s to compare.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s to compare.
 	 * @return Returns a <code>double</code> representing the type similarity of the 2 objects (0 &lt; value
 	 *         &lt; 1).
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the similarity.
 	 */
 	public static double typeSimilarityMetric(EObject obj1, EObject obj2) throws FactoryException {
 		return NameSimilarity.nameSimilarityMetric(typeValue(obj1), typeValue(obj2));
 	}
 
 	/**
 	 * This method returns a double comprised between 0 and 1 representing the relations similarities of the 2
 	 * EObjects. - 1 means the 2 objects surroundings are very similar - 0 means the 2 objects surroundings
 	 * are very different.
 	 * 
 	 * @param obj1
 	 *            First of the two {@link EObject}s to compare.
 	 * @param obj2
 	 *            Second of the two {@link EObject}s to compare.
 	 * @param filter
 	 *            Allows filtering of the pertinent features.
 	 * @return A <code>double</code> representing the relation similarity (0 &lt; value &lt; 1).
 	 * @throws FactoryException
 	 *             Thrown if we cannot compute the similarity.
 	 */
 	public static double relationsSimilarityMetric(EObject obj1, EObject obj2, MetamodelFilter filter)
 			throws FactoryException {
 		return NameSimilarity
 				.nameSimilarityMetric(relationsValue(obj1, filter), relationsValue(obj2, filter));
 	}
 
 	/**
 	 * Returns a {@link List} containing {@link StringBuffer}s representing the Types' names of the given
 	 * {@link EObject}.
 	 * 
 	 * @param current
 	 *            {@link EObject} we need to get the types of.
 	 * @return A {@link List} containing {@link StringBuffer}s representing the Types' names of the eobject.
 	 * @throws FactoryException
 	 *             Thrown if we cannot get the {@link EObject}'s types' names.
 	 */
 	public static List<StringBuffer> typeValueList(EObject current) throws FactoryException {
 		final EObject eclass = current.eClass();
 		final List<StringBuffer> result = new ArrayList<StringBuffer>();
 		List eclassAttributes = new LinkedList();
 		if (eclass instanceof EClass)
 			eclassAttributes = ((EClass)eclass).getEAllAttributes();
 
 		if (eclassAttributes.size() > 0) {
 			result.add(new StringBuffer("type:").append(current.eClass().getName())); //$NON-NLS-1$
 			Iterator it = eclassAttributes.iterator();
 			while (it.hasNext()) {
 				final Object next = it.next();
 				if (next instanceof EObject) {
 					final EObject obj = (EObject)next;
 					final String attributeName = EFactory.eGetAsString(obj, "name"); //$NON-NLS-1$
 					// get the feature name and the feature value
 					result.add(new StringBuffer("attr:").append(obj.eClass().getName()).append(":").append(//$NON-NLS-1$ //$NON-NLS-2$
 							attributeName));
 				}
 			}
 			// get children's name
 			if (eclass instanceof EClass)
 				eclassAttributes = ((EClass)eclass).getEAllReferences();
 			it = eclassAttributes.iterator();
 			while (it.hasNext()) {
 				final Object next = it.next();
 				if (next instanceof EObject) {
 					final EObject obj = (EObject)next;
 					final String attributeName = EFactory.eGetAsString(obj, "name"); //$NON-NLS-1$
 					// get the feature name and the feature value
 					result.add(new StringBuffer("ref:").append(obj.eClass().getName()).append(":").append(//$NON-NLS-1$ //$NON-NLS-2$
 							attributeName));
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * This method returns a {@link String} with content corresponding to the given {@link EObject}'s type.
 	 * 
 	 * @param current
 	 *            {@link EObject} we need the type of.
 	 * @return A {@link String} with content corresponding to the {@link EObject}'s type.
 	 * @throws FactoryException
 	 *             Thrown if we cannot retrieve <code>current</code>'s name.
 	 */
 	public static String typeValue(EObject current) throws FactoryException {
 		final List<StringBuffer> values = typeValueList(current);
 		final StringBuffer result = new StringBuffer();
 		final Iterator<StringBuffer> it = values.iterator();
 		while (it.hasNext())
 			result.append(it.next());
 		return result.toString();
 	}
 
 	/**
 	 * This method returns a {@link String} representing the {@link EObject} attributes' values.
 	 * 
 	 * @param current
 	 *            {@link EObject} we need the attributes' values of.
 	 * @param filter
 	 *            Allows filtering of the pertinent features.
 	 * @return A {@link String} representing the {@link EObject} attributes' values.
 	 * @throws FactoryException
 	 *             Thrown if we cannot retrieve the {@link EObject} features or their values.
 	 */
 	@SuppressWarnings("unchecked")
 	public static String relationsValue(EObject current, MetamodelFilter filter) throws FactoryException {
 		final EObject eclass = current.eClass();
 		final StringBuffer result = new StringBuffer();
		List<EReference> eObjectFeatures = new LinkedList<EReference>();
 		if (eclass instanceof EClass) {
 			if (filter != null)
 				eObjectFeatures = filter.getFilteredFeatures(current);
 			else
 				eObjectFeatures = ((EClass)eclass).getEAllReferences();
 		}
 		for (EStructuralFeature feature : eObjectFeatures) {
 			if (feature instanceof EReference && !((EReference)feature).isDerived()) {
 				Object value = current.eGet(feature);
 				if (value instanceof List) {
 					for (final Iterator valueIterator = ((List)value).iterator(); valueIterator.hasNext(); ) {
 						Object next = valueIterator.next();
 						if (next instanceof EObject) {
 							String objName = NameSimilarity.findName((EObject)next);
 							result.append(objName);
 						}
 					}
 				} else if (value instanceof EObject) {
 					String objName = NameSimilarity.findName((EObject)value);
 					result.append(objName);
 				}
 			}
 		}
 		if (current.eContainer() != null)
 			result.append(NameSimilarity.findName(current.eContainer())).append("\n"); //$NON-NLS-1$
 		
 		return result.toString();
 	}
 }
