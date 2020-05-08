 package org.eclipse.emf.compare.match.statistic;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 /**
  * This class determine the unused features in a metamodel using models
  * 
 * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public class MetamodelFilter {
 	
 	
 	private Map eClassToFeaturesList = null;
 	/**
 	 * Return a list of the pertinent features for this eObject
 	 * 
 	 * @param eObj
 	 * @return a list of the pertinent features for this eObject
 	 */
 	public List getFilteredFeatures(EObject eObj) {
 		// cache the filtered features for a type
 		if (eClassToFeaturesList == null)
 			eClassToFeaturesList = new HashMap();
 		if (eClassToFeaturesList.containsKey(eObj.eClass()))
 			return (List)eClassToFeaturesList.get(eObj.eClass());
 		// end of memoize cache
 		
 		List result = new ArrayList();
 		Collection unused = getUnusedFeatures();
 		Iterator it = eObj.eClass().getEAllStructuralFeatures().iterator();
 		while (it.hasNext())
 		{
 			EStructuralFeature feat = (EStructuralFeature)it.next();
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
 
 	protected Map featuresToInformation = new HashMap();
 
 	protected Collection unusedFeatures = null;
 
 	/**
 	 * Analyse a model and change the stats using this model
 	 * 
 	 * @param root
 	 */
 	public void analyseModel(EObject root) {
 		processEObject(root);
 		Iterator it = root.eAllContents();
 		while (it.hasNext()) {
 			EObject eObj = (EObject) it.next();
 			processEObject(eObj);
 		}
 		unusedFeatures = null;
 		eClassToFeaturesList = null;
 	}
 
 
 	private void buildUnusedFeatures() {
 		unusedFeatures = new ArrayList();
 		Iterator it = featuresToInformation.keySet().iterator();
 		while (it.hasNext()) {
 			EStructuralFeature feat = (EStructuralFeature) it.next();
 			if (((FeatureInformation) featuresToInformation.get(feat)).hasUniqueValue)
 				unusedFeatures.add(featuresToInformation.get(feat));
 		}
 	}
 
 	private void processEObject(EObject eObj) {
 		Iterator featIt = eObj.eClass().getEAllStructuralFeatures().iterator();
 		while (featIt.hasNext()) {
 			EStructuralFeature feat = (EStructuralFeature) featIt.next();
 			if (!featuresToInformation.containsKey(feat))
 				featuresToInformation.put(feat, new FeatureInformation(feat));
 			if (eObj.eGet(feat) != null) {
 				((FeatureInformation) featuresToInformation.get(feat))
 						.processValue(eObj.eGet(feat).toString());
 			} else {
 				((FeatureInformation) featuresToInformation.get(feat))
 						.processValue("null"); //$NON-NLS-1$
 			}
 		}
 	}
 }
 
 class FeatureInformation {
 
 	EStructuralFeature feature;
 
 	int timesUsed = 0;
 
 	boolean hasUniqueValue = true;
 
 	String uniqueValue = null;
 
 	/**
 	 * Create a featureInformation from a feature
 	 * 
 	 * @param feat
 	 */
 	public FeatureInformation(EStructuralFeature feat) {
 		this.feature = feat;
 	}
 
 	/**
 	 * Add this value in the calculus model
 	 * 
 	 * @param value
 	 */
 	public void processValue(String value) {
 		timesUsed += 1;
 		if (uniqueValue != null) {
 			if (!uniqueValue.equals(value))
 				hasUniqueValue = false;
 		} else {
 			uniqueValue = value;
 		}
 	}
 
 	/**
 	 * 
 	 * @return the feature
 	 */
 	public EStructuralFeature getFeature() {
 		return feature;
 	}
 
 	/**
 	 * 
 	 * @return true if this feature always has the same value
 	 */
 	public boolean hasUniqueValue() {
 		return hasUniqueValue;
 	}
 
 	/**
 	 * 
 	 * @return the number of time this feature has been used
 	 */
 	public int getTimesUsed() {
 		return timesUsed;
 	}
 
 	/**
 	 * 
 	 * @return the feature unique value
 	 */
 	public String getUniqueValue() {
 		return uniqueValue;
 	}
 
 }
