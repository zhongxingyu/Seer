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
 package org.eclipse.emf.compare.match.statistic;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.util.EFactory;
 import org.eclipse.emf.compare.util.FactoryException;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 /**
  * This class determines the unused features in a metamodel using models.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class MetamodelFilter {
 	protected Map<EStructuralFeature, FeatureInformation> featuresToInformation = new HashMap<EStructuralFeature, FeatureInformation>();
 
 	protected List<FeatureInformation> unusedFeatures;
 
 	private Map<EClass, List<EStructuralFeature>> eClassToFeaturesList;
 
 	/**
 	 * Returns a list of the pertinent features for this {@link EObject}.
 	 * 
 	 * @param eObj
 	 *            {@link EObject} from which we seek the features.
 	 * @return A list of the pertinent features for this {@link EObject}.
 	 */
	public List<EStructuralFeature> getFilteredFeatures(EObject eObj) {
 		// cache the filtered features for a type
 		if (eClassToFeaturesList == null)
 			eClassToFeaturesList = new HashMap<EClass, List<EStructuralFeature>>();
 		if (eClassToFeaturesList.containsKey(eObj.eClass()))
 			return eClassToFeaturesList.get(eObj.eClass());
 		// end of memorize cache
 
 		final List<EStructuralFeature> result = new ArrayList<EStructuralFeature>();
 		final Collection unused = getUnusedFeatures();
 		final Iterator it = eObj.eClass().getEAllStructuralFeatures().iterator();
 		while (it.hasNext()) {
 			final EStructuralFeature feat = (EStructuralFeature)it.next();
 			if (!unused.contains(feat))
 				result.add(feat);
 		}
 		eClassToFeaturesList.put(eObj.eClass(), result);
 		return result;
 	}
 
 	private Collection getUnusedFeatures() {
 		if (unusedFeatures == null)
 			buildUnusedFeatures();
 		return unusedFeatures;
 	}
 
 	/**
 	 * Analyses a model and changes the stats using this model.
 	 * 
 	 * @param root
 	 *            Model to analyze.
 	 */
 	public void analyseModel(EObject root) {
 		processEObject(root);
 		final Iterator it = root.eAllContents();
 		while (it.hasNext()) {
 			final EObject eObj = (EObject)it.next();
 			processEObject(eObj);
 		}
 		unusedFeatures = null;
 		eClassToFeaturesList = null;
 	}
 
 	private void buildUnusedFeatures() {
 		unusedFeatures = new ArrayList<FeatureInformation>();
 		final Iterator<EStructuralFeature> it = featuresToInformation.keySet().iterator();
 		while (it.hasNext()) {
 			final EStructuralFeature feat = it.next();
 			if (featuresToInformation.get(feat).hasUniqueValue())
 				unusedFeatures.add(featuresToInformation.get(feat));
 		}
 	}
 
 	private void processEObject(EObject eObj) {
 		final Iterator featIt = eObj.eClass().getEAllStructuralFeatures().iterator();
 		while (featIt.hasNext()) {
 			final EStructuralFeature feat = (EStructuralFeature)featIt.next();
 			if (!featuresToInformation.containsKey(feat))
 				featuresToInformation.put(feat, new FeatureInformation(feat));
 			try {
 				if (EFactory.eGet(eObj, feat.getName()) != null) {
 					featuresToInformation.get(feat).processValue(eObj.eGet(feat).toString());
 				} else {
 					featuresToInformation.get(feat).processValue("null"); //$NON-NLS-1$
 				}
 			} catch (FactoryException e) {
 				EMFComparePlugin.getDefault().log(e.getMessage(), false);
 			}
 		}
 	}
 }
 
 /**
  * Describes a feature.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 class FeatureInformation {
 	private EStructuralFeature feature;
 
 	private int timesUsed;
 
 	private boolean hasUniqueValue = true;
 
 	private String uniqueValue;
 
 	/**
 	 * Creates a {@link FeatureInformation} from a feature.
 	 * 
 	 * @param feat
 	 *            The {@link EStructuralFeature feature} we want described.
 	 */
 	public FeatureInformation(EStructuralFeature feat) {
 		feature = feat;
 	}
 
 	/**
 	 * Adds this value in the calculus model.
 	 * 
 	 * @param value
 	 *            The value to add.
 	 */
 	public void processValue(String value) {
 		timesUsed += 1;
 		if (uniqueValue != null && !uniqueValue.equals(value)) {
 			hasUniqueValue = false;
 		} else if (uniqueValue == null) {
 			uniqueValue = value;
 		}
 	}
 
 	/**
 	 * Returns the feature described by this {@link FeatureInformation}.
 	 * 
 	 * @return The feature described by this {@link FeatureInformation}.
 	 */
 	public EStructuralFeature getFeature() {
 		return feature;
 	}
 
 	/**
 	 * Indicates that this features always has the same value.
 	 * 
 	 * @return <code>True</code> if this feature always has the same value, <code>False</code> otherwise.
 	 */
 	public boolean hasUniqueValue() {
 		return hasUniqueValue;
 	}
 
 	/**
 	 * Returns the number of time this feature has been used.
 	 * 
 	 * @return The number of time this feature has been used.
 	 */
 	public int getTimesUsed() {
 		return timesUsed;
 	}
 
 	/**
 	 * Returns the feature unique value.
 	 * 
 	 * @return The feature unique value.
 	 */
 	public String getUniqueValue() {
 		return uniqueValue;
 	}
 }
