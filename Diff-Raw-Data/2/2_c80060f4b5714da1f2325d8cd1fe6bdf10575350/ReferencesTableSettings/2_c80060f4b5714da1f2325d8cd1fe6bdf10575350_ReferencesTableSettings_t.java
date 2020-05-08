 /**
  * 
  */
 package org.eclipse.emf.eef.runtime.ui.widgets.referencestable;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class ReferencesTableSettings implements EEFEditorSettings {
 
 	private EObject source;
 	private EReference[] features;
 
 	/**
 	 * @param source
 	 * @param path
 	 */
 	public ReferencesTableSettings(EObject source, EReference... features) {
 		super();
 		this.source = source;
 		this.features = features;
 	}
 
 	/**
 	 * @return the source
 	 */
 	public EObject getSource() {
 		return source;
 	}
 
 	/**
 	 * @param source the source to set
 	 */
 	public void setSource(EObject source) {
 		this.source = source;
 	}
 	
 	/**
 	 * @return the type of the last feature
 	 */
 	public EClassifier getEType() {
 		return features[features.length - 1].getEType();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#isAffectingFeature(org.eclipse.emf.ecore.EStructuralFeature)
 	 */
 	public boolean isAffectingFeature(EStructuralFeature feature) {
 		return Arrays.asList(features).contains(feature);
 	}
 
 	/************************************************************************************************
 	 * 																								*
 	 * getElements()		 																		*
 	 * 																								*
 	 ************************************************************************************************/
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#getValue()
 	 */
 	public Object[] getValue() {
 		if (((EClass)features[0].eContainer()).isInstance(source)) {
 			Object value1 = ((EObject)source).eGet(features[0]);
 			if (value1 != null) {
 				if (features.length == 1) {
 					return features[0].isMany()?((List<EObject>)value1).toArray():new Object[] { value1 };
 				}
 				else {
 					if (features[0].isMany()) {
 						List<Object> result = new ArrayList<Object>();
 						for (EObject elem : ((List<EObject>)value1)) {
 							if (features[1].isMany())
 								result.addAll((List<EObject>)elem.eGet(features[1]));
 							else {
 								EObject value2 = (EObject)elem.eGet(features[1]);
 								result.add(value2 == null?"":value2);
 							}
 						}
 						return result.toArray();
 					} else {
 						if (features[1].isMany()) {
 							return ((List)((EObject)value1).eGet(features[1])).toArray();
 						}
 						else {
 							Object value2 = ((EObject)value1).eGet(features[1]);
 							return new Object[] { value2 == null?"":value2 };
 						}
 					}
 				}
 			}
 		}
 		return new Object[0];
 	}
 
 	/************************************************************************************************
 	 * 																								*
 	 * Add via ModelNavigation 																		*
 	 * 																								*
 	 ************************************************************************************************/
 	
 	/**
 	 * Add a new value following a list of StructualFeatures to a given EObject
 	 * @param newValue the value to add
 	 */
 	public void addToReference(EObject newValue) {
 		Object value1 = source.eGet(features[0]);
 		if (features[0].isMany()) {
 			addFirstMany((List<EObject>)value1, newValue);
 		} else /* ref is Single */  {
 			addFirstSingle((EObject)value1, newValue);
 		}
 	}
 
 
 	/**
 	 * This method add newValue to the managed reference(s) if the first reference in the path is a multiple reference 
 	 * @param ref1Values
 	 * @param newValue
 	 */
 	private void addFirstMany(List<EObject> ref1Values, EObject newValue) {
 		if (features.length > 1) {
 			if (features[1].isMany()) {
 				addFirstManySecondMany(ref1Values, newValue);
 			}
 			else {
 				addFirstManySecondSingle(ref1Values, newValue);
 			}
 		}
 		else {
 			// There is only one multiple reference in the path, we simply add the new value to
 			// the existing values
 			((List<EObject>)ref1Values).add(newValue);
 		}
 	}
 
 	/**
 	 * @param ref1Values
 	 * @param newValue
 	 */
 	private void addFirstManySecondMany(List<EObject> ref1Values, EObject newValue) {
 		throw new IllegalStateException("Ambigous case - Cannot process ModelNavigation with more than one multiple reference");
 	}
 
 	/**
 	 * @param newValue
 	 * @param ref2
 	 */
 	private void addFirstManySecondSingle(List<EObject> ref1Values, EObject newValue) {
 		EObject intermediate  = EcoreUtil.create(features[0].getEReferenceType());
 		((EObject)intermediate).eSet(features[1], newValue);
 		ref1Values.add(intermediate);
 	}
 
 	/**
 	 * This method add newValue to the managed reference(s) if the first reference in the path is a single reference 
 	 * @param ref1Value
 	 * @param newValue
 	 */
 	private void addFirstSingle(EObject ref1Value, EObject newValue) {
 		if (features.length > 1) {
 			if (features[1].isMany()) {
 				addFirstSingleSecondMany(ref1Value, newValue);
 			}
 			else {
 				addFirstSingleSecondSingle(ref1Value, newValue);
 			}
 		}
 		else {
 			// There is only one single reference in the path, we simply add the new value to
 			// the existing values. Must be an error ?
 			source.eSet(features[0], newValue);
 		}
 	}
 
 	/**
 	 * @param ref1Value
 	 * @param newValue
 	 */
 	private void addFirstSingleSecondMany(EObject ref1Value, EObject newValue) {
 		if (ref1Value == null)  {
 			ref1Value = EcoreUtil.create(features[0].getEReferenceType());
 			// WARNING: Cannot be an abstract class
 			source.eSet(features[0], ref1Value);
 		}
 		((List<EObject>)ref1Value.eGet(features[1])).add(newValue);
 	}
 	
 	/**
 	 * @param ref1Value
 	 * @param newValue
 	 */
 	private void addFirstSingleSecondSingle(EObject ref1Value, EObject newValue) {
 		throw new IllegalStateException("Ambigous case - Cannot process ModelNavigation without multiple reference");
 	}
 
 	/************************************************************************************************
 	 * 																								*
 	 * Set via ModelNavigation 																		*
 	 * 																								*
 	 ************************************************************************************************/
 	
 	/**
 	 * Add a new value following a list of StructualFeatures to a given EObject
 	 * @param newValues the value to add
 	 */
 	public void setToReference(List<EObject> newValues) {
 		Object value1 = source.eGet(features[0]);
 		if (features[0].isMany()) {
 			setFirstMany((List<EObject>)value1, newValues);
 		} else /* ref is Single */  {
 			setFirstSingle((EObject)value1, newValues);
 		}
 	}
 
 
 	/**
 	 * This method add newValue to the managed reference(s) if the first reference in the path is a multiple reference 
 	 * @param ref1Values
 	 * @param newValues
 	 */
 	private void setFirstMany(List<EObject> ref1Values, List<EObject> newValues) {
 		if (features.length > 1) {
 			EReference ref2 = features[1];
 			if (ref2.isMany()) {
 				setFirstManySecondMany(ref1Values, newValues);
 			}
 			else {
 				setFirstManySecondSingle(ref1Values, newValues);
 			}
 		}
 		else {
 			// There is only one multiple reference in the path, we simply add the new value to
 			// the existing values
 			source.eSet(features[0], newValues);
 		}
 	}
 
 	/**
 	 * @param ref1Values
 	 * @param newValues
 	 */
 	private void setFirstManySecondMany(List<EObject> ref1Values, List<EObject> newValues) {
 		throw new IllegalStateException("Ambigous case - Cannot process ModelNavigation with more than one multiple reference");
 	}
 
 	/**
 	 * @param ref1Values
 	 * @param newValues
 	 */
 	private void setFirstManySecondSingle(List<EObject> ref1Values, List<EObject> newValues) {
 		List<EObject> todo = new ArrayList<EObject>(newValues);
 		List<EObject> toremove = new ArrayList<EObject>();
 		// First, we check the existing values. Mainly, we create a list of elements
 		// to remove (i.e. not in the new values.
 		for (EObject ref1Value : ref1Values) {
 			EObject ref2Value = (EObject) ref1Value.eGet(features[1]);
 			if (todo.contains(ref2Value))
 				todo.remove(ref2Value);
 			else
 				toremove.add(ref1Value);
 		}
 		// We remove those we have detected to remove
 		for (EObject eObject : toremove) {
 			ref1Values.remove(eObject);
 		}
 		// Finally we have those we don't have found in the existing values
 		for (EObject eObject : todo) {
 			EObject intermediate = EcoreUtil.create(features[0].getEReferenceType());
			intermediate.eSet(features[1], eObject);
 			ref1Values.add(intermediate);
 		}
 	}
 
 	/**
 	 * This method add newValue to the managed reference(s) if the first reference in the path is a single reference 
 	 * @param ref1Value
 	 * @param newValues
 	 * @param ref
 	 */
 	private void setFirstSingle(EObject ref1Value, List<EObject> newValues) {
 		if (features.length > 1) {
 			if (features[1].isMany()) {
 				setFirstSingleSecondMany(ref1Value, newValues);
 			}
 			else {
 				setFirstSingleSecondSingle(features[1], newValues);
 			}
 		}
 		else {
 			// There is only one single reference in the path, we simply add the new value to
 			// the existing values. Must be an error ?
 			source.eSet(features[0], newValues);
 		}
 	}
 
 	/**
 	 * @param ref1Value
 	 * @param newValues
 	 */
 	private void setFirstSingleSecondMany(EObject ref1Value, List<EObject> newValues) {
 		if (ref1Value == null)  {
 			ref1Value = EcoreUtil.create(features[0].getEReferenceType());
 			// WARNING: Cannot be an abstract class
 			source.eSet(features[0], ref1Value);
 		}
 		ref1Value.eSet(features[1], newValues);
 	}
 	
 	/**
 	 * @param value2
 	 * @param newValues
 	 */
 	private void setFirstSingleSecondSingle(Object value2, List<EObject> newValues) {
 		throw new IllegalStateException("Ambigous case - Cannot process ModelNavigation without multiple reference");
 	}
 
 	/************************************************************************************************
 	 * 																								*
 	 * Remove via ModelNavigation 																	*
 	 * 																								*
 	 ************************************************************************************************/
 
 	/**
 	 * Remove a value following a list of StructualFeatures to a given EObject
 	 * @param valueToRemove the value to remove
 	 */
 	public void removeFromReference(EObject valueToRemove) {
 		EReference ref = features[0];
 		Object value1 = source.eGet(ref);
 		if (ref.isMany()) {
 			removeFirstMany((List<EObject>) value1, valueToRemove);
 		} else /* ref is Single */  {
 			removeFirstSingle((EObject)value1, valueToRemove);
 		}
 	}
 
 	private void removeFirstMany(List<EObject> value1, EObject valueToRemove) {
 		if (features.length > 1) {
 			EReference ref2 = features[1];
 			if (ref2.isMany()) {
 				removeFirstManySecondMany(value1, valueToRemove);
 			}
 			else { /* ref2 is Single */
 				removeFirstManySecondSingle(value1, valueToRemove);
 			}
 		}
 		else {
 			value1.remove(valueToRemove);
 		}
 	}
 
 	private void removeFirstManySecondMany(List<EObject> ref1Values, EObject valueToRemove) {
 		throw new IllegalStateException("Ambigous case - Cannot process ModelNavigation with more than one multiple reference");
 	}
 
 	private void removeFirstManySecondSingle(List<EObject> value1, EObject valueToRemove) {
 		EObject elemToRemove = null;
 		for (EObject elem : value1) {
 			EObject elem2 = (EObject) ((EObject)elem).eGet(features[1]);
 			if (elem2 != null && elem2.equals(valueToRemove)) {
 				elemToRemove = elem;
 			}
 		}
 		value1.remove(elemToRemove);
 	}
 
 	private void removeFirstSingle(EObject value1, EObject valueToRemove) {
 		if (features.length > 1) {
 			Object value2 = value1.eGet(features[1]);
 			if (features[1].isMany()) {
 				removeFirstSingleSecondMany((List<EObject>) value2, valueToRemove);
 			}
 			else { /* ref2 is Single */
 				removeFirstSingleSecondSingle();
 			}
 		}
 		else {
 			EcoreUtil.remove((EObject) source.eGet(features[0]));
 		}
 	}
 
 
 	private void removeFirstSingleSecondMany(List<EObject> value2, EObject valueToRemove) {
 		value2.remove(valueToRemove);
 	}
 
 	private void removeFirstSingleSecondSingle() {
 		EcoreUtil.remove((EObject) source.eGet(features[0]));
 	}
 
 	/**
 	 * Defines if the given value is already contained in the given path	
 	 * @param toCheck the element to check
 	 */
 	public boolean contains(EObject toCheck) {
 		EReference ref = features[0];
 		Object value1 = source.eGet(ref);
 		if (ref.isMany()) {
 			if (features.length > 1) {
 				EReference ref2 = features[1];
 				if (ref2.isMany()) {
 					for (EObject elem : (List<EObject>)value1) {
 						List<EObject> value2 = (List<EObject>) ((EObject)elem).eGet(ref2);
 						if (value2.contains(toCheck))
 							return true;
 					}
 					return false;
 				}
 				else { /* ref2 is Single */
 					EObject elemToRemove = null;
 					for (EObject elem : (List<EObject>)value1) {
 						EObject elem2 = (EObject) ((EObject)elem).eGet(ref2);
 						if (elem2 != null && elem2.equals(toCheck)) {
 							return true;
 						}
 					}
 					return false;
 				}
 			}
 			else {
 				return ((List<EObject>)value1).contains(toCheck);
 			}
 		} else /* ref is Single */  {
 			if (features.length > 1) {
 				EReference ref2 = features[1];
 				Object value2 = ((EObject)value1).eGet(ref2);
 				if (ref2.isMany()) {
 					return ((List<EObject>)value2).contains(toCheck);
 				}
 				else { /* ref2 is Single */
 					return value2.equals(toCheck);
 				}
 			} 
 			else {
 				return value1.equals(toCheck);
 			}
 		}
 	}
 
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings#choiceOfValues(org.eclipse.emf.common.notify.AdapterFactory)
 	 */
 	public Object choiceOfValues(AdapterFactory adapterFactory) {
 		// FIXME: choiceOfValues should be called with the adapterFactory in parameter
 		if (features.length == 1)
 			return EEFUtils.choiceOfValues(source, features[0]);
 		else {
 			if (features.length > 1) {
 				EObject tmp = EcoreUtil.create((EClass) features[0].getEType());
 				source.eResource().getContents().add(tmp);
 				Object result = EEFUtils.choiceOfValues(tmp, features[1]);
 				EcoreUtil.delete(tmp);
 				return result;
 			}
 		}
 		return null;
 	}
 }
