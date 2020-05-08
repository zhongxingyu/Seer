 /**
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.internal.serialization;
 
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
 import org.eclipse.rmf.serialization.RMFExtendedMetaData;
 import org.eclipse.rmf.serialization.RMFExtendedMetaDataImpl;
 
 /**
  * TODO: write down assumptions as asserts
  * 
  * @author broerkens
  */
 public class RMFXMLSaveImpl extends XMLSaveImpl {
 	RMFExtendedMetaData rmfExtendedMetaData = null;
 
 	public enum SerializationType {
 		attributesOnly, elementsOnly, attributesAndElements
 	}
 
 	public RMFXMLSaveImpl(XMLHelper helper) {
 		super(helper);
 	}
 
 	@Override
 	protected boolean saveFeatures(EObject o, boolean attributesOnly) {
 		if (attributesOnly) {
 			return saveFeatures(o, SerializationType.attributesOnly, false);
 		} else {
 			return saveFeatures(o, SerializationType.attributesAndElements, false);
 		}
 	}
 
 	protected boolean saveFeatures(EObject o, SerializationType serializationType, boolean suppressClosingElement) {
 		EClass eClass = o.eClass();
 		int contentKind = extendedMetaData == null ? ExtendedMetaData.UNSPECIFIED_CONTENT : extendedMetaData.getContentKind(eClass);
 		if (!toDOM) {
 			switch (contentKind) {
 			case ExtendedMetaData.MIXED_CONTENT:
 			case ExtendedMetaData.SIMPLE_CONTENT: {
 				doc.setMixed(true);
 				break;
 			}
 			}
 		}
 
 		if (o == root) {
 			writeTopAttributes(root);
 		}
 
 		EStructuralFeature[] features = featureTable.getFeatures(eClass);
 		int[] featureKinds = featureTable.getKinds(eClass, features);
 		int[] elementFeatures = null;
 		int elementCount = 0;
 
 		String content = null;
 
 		// Process XML attributes
 		if (SerializationType.elementsOnly == serializationType) {
 			LOOP: for (int i = 0; i < features.length; i++) {
 				int kind = featureKinds[i];
 				EStructuralFeature f = features[i];
 
 				if (kind != TRANSIENT && shouldSaveFeature(o, f)) {
 					switch (kind) {
 					case DATATYPE_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getDataTypeElementSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_SINGLE: {
 						continue LOOP; // next feature, no element required
 					}
 					case DATATYPE_SINGLE_NILLABLE: {
 						if (!isNil(o, f)) {
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ATTRIBUTE_SINGLE: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_MANY: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_SINGLE: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_MANY: {
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_HREF_SINGLE_UNSETTABLE: {
 						if (isNil(o, f)) {
 							break;
 						}
 						// it's intentional to keep going
 					}
 					case OBJECT_HREF_SINGLE: {
 						if (useEncodedAttributeStyle) {
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocSingle(o, f)) {
 							case SAME_DOC: {
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_HREF_MANY_UNSETTABLE: {
 						if (isEmpty(o, f)) {
 							continue LOOP; // next feature, no element required
 						}
 						// It's intentional to keep going.
 					}
 					case OBJECT_HREF_MANY: {
 						if (useEncodedAttributeStyle) {
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocMany(o, f)) {
 							case SAME_DOC: {
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_IDREF_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_ATTRIBUTE_MANY: {
 						break;
 					}
 					case OBJECT_CONTAIN_MANY_UNSETTABLE:
 					case DATATYPE_MANY: {
 						if (isEmpty(o, f)) {
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_CONTAIN_SINGLE_UNSETTABLE:
 					case OBJECT_CONTAIN_SINGLE:
 					case OBJECT_CONTAIN_MANY:
 					case ELEMENT_FEATURE_MAP: {
 						break;
 					}
 					case ATTRIBUTE_FEATURE_MAP: {
 						continue LOOP; // next feature, no element required
 					}
 					default: {
 						continue LOOP; // next feature, no element required
 					}
 
 					} // end switch
 
 					// We only get here if we should do this.
 					//
 					if (elementFeatures == null) {
 						elementFeatures = new int[features.length];
 					}
 					elementFeatures[elementCount++] = i;
 				} // end if
 			} // end for
 		} else {
 
 			LOOP: for (int i = 0; i < features.length; i++) {
 				int kind = featureKinds[i];
 				EStructuralFeature f = features[i];
 
 				if (kind != TRANSIENT && shouldSaveFeature(o, f)) {
 
 					switch (kind) {
 					case DATATYPE_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getDataTypeElementSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_SINGLE: {
 						saveDataTypeSingle(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case DATATYPE_SINGLE_NILLABLE: {
 						if (!isNil(o, f)) {
 							saveDataTypeSingle(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ATTRIBUTE_SINGLE: {
 						saveEObjectSingle(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_MANY: {
 						saveEObjectMany(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_SINGLE: {
 						saveIDRefSingle(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_ATTRIBUTE_IDREF_MANY: {
 						saveIDRefMany(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					case OBJECT_HREF_SINGLE_UNSETTABLE: {
 						if (isNil(o, f)) {
 							break;
 						}
 						// it's intentional to keep going
 					}
 					case OBJECT_HREF_SINGLE: {
 						if (useEncodedAttributeStyle) {
 							saveEObjectSingle(o, f);
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocSingle(o, f)) {
 							case SAME_DOC: {
 								saveIDRefSingle(o, f);
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_HREF_MANY_UNSETTABLE: {
 						if (isEmpty(o, f)) {
 							saveManyEmpty(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						// It's intentional to keep going.
 					}
 					case OBJECT_HREF_MANY: {
 						if (useEncodedAttributeStyle) {
 							saveEObjectMany(o, f);
 							continue LOOP; // next feature, no element required
 						} else {
 							switch (sameDocMany(o, f)) {
 							case SAME_DOC: {
 								saveIDRefMany(o, f);
 								continue LOOP; // next feature, no element required
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP; // next feature, no element required
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_IDREF_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefSingleSimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefManySimple(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case DATATYPE_ATTRIBUTE_MANY: {
 						break;
 					}
 					case OBJECT_CONTAIN_MANY_UNSETTABLE:
 					case DATATYPE_MANY: {
 						if (isEmpty(o, f)) {
 							saveManyEmpty(o, f);
 							continue LOOP; // next feature, no element required
 						}
 						break;
 					}
 					case OBJECT_CONTAIN_SINGLE_UNSETTABLE:
 					case OBJECT_CONTAIN_SINGLE:
 					case OBJECT_CONTAIN_MANY:
 					case ELEMENT_FEATURE_MAP: {
 						break;
 					}
 					case ATTRIBUTE_FEATURE_MAP: {
 						saveAttributeFeatureMap(o, f);
 						continue LOOP; // next feature, no element required
 					}
 					default: {
 						continue LOOP; // next feature, no element required
 					}
 					}
 
 					if (SerializationType.attributesOnly == serializationType) {
 						continue LOOP; // next feature, no element required
 					}
 
 					// We only get here if we should do this.
 					//
 					if (elementFeatures == null) {
 						elementFeatures = new int[features.length];
 					}
 					elementFeatures[elementCount++] = i;
 				}
 			}
 			processAttributeExtensions(o);
 
 			if (elementFeatures == null) {
 				if (content == null) {
 					content = getContent(o, features);
 				}
 
 				if (content == null) {
 					if (o == root && writeTopElements(root)) {
 						endSaveFeatures(o, 0, null);
 						return true;
 					} else {
 						endSaveFeatures(o, EMPTY_ELEMENT, null);
 						return false;
 					}
 				} else {
 					endSaveFeatures(o, CONTENT_ELEMENT, content);
 					return true;
 				}
 			}
 		}
 
 		if (o == root) {
 			writeTopElements(root);
 		}
 
 		// Process XML elements
 		for (int i = 0; i < elementCount; i++) {
 			int kind = featureKinds[elementFeatures[i]];
 			EStructuralFeature f = features[elementFeatures[i]];
 			switch (kind) {
 			case DATATYPE_SINGLE_NILLABLE: {
 				saveNil(o, f);
 				break;
 			}
 			case ELEMENT_FEATURE_MAP: {
 				saveElementFeatureMap(o, f);
 				break;
 			}
 			case DATATYPE_MANY: {
 				saveDataTypeMany(o, f);
 				break;
 			}
 			case DATATYPE_ATTRIBUTE_MANY: {
 				saveDataTypeAttributeMany(o, f);
 				break;
 			}
 			case DATATYPE_ELEMENT_SINGLE: {
 				saveDataTypeElementSingle(o, f);
 				break;
 			}
 			case OBJECT_CONTAIN_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_CONTAIN_SINGLE: {
 				saveContainedSingle(o, f);
 				break;
 			}
 			case OBJECT_CONTAIN_MANY_UNSETTABLE:
 			case OBJECT_CONTAIN_MANY: {
 				saveContainedMany(o, f);
 				break;
 			}
 			case OBJECT_HREF_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_HREF_SINGLE: {
 				saveHRefSingle(o, f);
 				break;
 			}
 			case OBJECT_HREF_MANY_UNSETTABLE:
 			case OBJECT_HREF_MANY: {
 				saveHRefMany(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_ELEMENT_SINGLE: {
 				saveElementReferenceSingle(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_MANY: {
 				saveElementReferenceMany(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE: {
 				if (isNil(o, f)) {
 					saveNil(o, f);
 					break;
 				}
 				// it's intentional to keep going
 			}
 			case OBJECT_ELEMENT_IDREF_SINGLE: {
 				saveElementIDRefSingle(o, f);
 				break;
 			}
 			case OBJECT_ELEMENT_IDREF_MANY: {
 				saveElementIDRefMany(o, f);
 				break;
 			}
 			} // end switch
 		} // end for
 		if (!suppressClosingElement) {
 			endSaveFeatures(o, 0, null);
 		}
 		return true;
 	}
 
 	@Override
 	protected void saveContainedMany(EObject o, EStructuralFeature f) {
 		assert f.isMany();
 
 		if (null != rmfExtendedMetaData && null != extendedMetaData) {
 			// RMF serialization enabled
 			@SuppressWarnings("unchecked")
 			List<? extends InternalEObject> values = ((InternalEList<? extends InternalEObject>) helper.getValue(o, f)).basicList();
 			if (null != values && !values.isEmpty()) {
 				int featureSerializationStructure = rmfExtendedMetaData.getFeatureSerializationStructure(f);
 
 				switch (featureSerializationStructure) {
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0000__NONE:
 					saveContainment0000Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 					saveContainment0001Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0010__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContainment0010Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0011__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment0011Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 					saveContainment0100Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment0101Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0110__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContainment0110Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__0111__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment0111Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment1001Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1010__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContainment1010Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1011__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment1011Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 					saveContainment1100Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1101__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment1101Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1110__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT:
 					saveContainment1110Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__1111__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT__CLASSIFIER_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 					saveContainment1111Many(values, f);
 					break;
 				case RMFExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 					// if undefined, use the standard EMF mechanism
 					saveContainment0100Many(values, f);
 					break;
 				default:
 					saveContainment1001Many(values, f);
 					break;
 				}
 
 			}
 
 		} else {
 			super.saveContainedMany(o, f);
 		}
 	}
 
 	protected void saveContainment0000Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		for (EObject value : values) {
 			saveFeatures(value, SerializationType.elementsOnly, true);
 		}
 	}
 
 	protected void saveContainment0001Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		for (EObject value : values) {
 			doc.startElement(getClassifierQName(value.eClass()));
 			saveFeatures(value);
 		}
 	}
 
 	protected void saveContainment0010Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					saveFeatures(value, SerializationType.elementsOnly, true);
 				}
 			}
 			doc.endElement();
 		}
 	}
 
 	protected void saveContainment0011Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					doc.startElement(getClassifierQName(value.eClass()));
 					saveFeatures(value, SerializationType.attributesAndElements, false);
 				}
 			}
 			doc.endElement();
 		}
 	}
 
 	protected void saveContainment0100Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		// this is the default EMF behaviour
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		for (EObject value : values) {
 			saveElement(value, f);
 		}
 	}
 
 	protected void saveContainment0101Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		for (EObject value : values) {
 			doc.startElement(getFeatureQName(f));
 			doc.startElement(getClassifierQName(value.eClass()));
 			saveFeatures(value, SerializationType.attributesAndElements, false);
 			doc.endElement();
 		}
 	}
 
 	protected void saveContainment0110Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureQName(f));
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					saveFeatures(value, SerializationType.elementsOnly, true);
 				}
 			}
 			doc.endElement();
 		}
 		doc.endElement();
 
 	}
 
 	protected void saveContainment0111Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureQName(f));
 		for (EClass eClass : getOrderedClasses(values)) {
 			doc.startElement(getClassifierWrapperQName(eClass));
 			for (EObject value : values) {
 				if (eClass == value.eClass()) {
 					doc.startElement(getClassifierQName(value.eClass()));
 					saveFeatures(value);
 				}
 			}
 			doc.endElement();
 		}
 		doc.endElement();
 	}
 
 	protected void saveContainment1000Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0000Many(values, f);
 		doc.endElement();
 		// last end is written by caller
 	}
 
 	protected void saveContainment1001Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0001Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContainment1010Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0010Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContainment1011Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0011Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContainment1100Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0100Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContainment1101Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0101Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContainment1110Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0110Many(values, f);
 		doc.endElement();
 	}
 
 	protected void saveContainment1111Many(List<? extends InternalEObject> values, EStructuralFeature f) {
 		assert null != values;
 		assert !values.isEmpty();
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 
 		doc.startElement(getFeatureWrapperQName(f));
 		saveContainment0111Many(values, f);
 		doc.endElement();
 	}
 
 	protected String getFeatureQName(EStructuralFeature eStructuralFeature) {
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 		String featureXMLName = rmfExtendedMetaData.getXMLName(eStructuralFeature);
 		EPackage ePackage = eStructuralFeature.getEContainingClass().getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, featureXMLName);
 	}
 
 	protected String getFeatureWrapperQName(EStructuralFeature eStructuralFeature) {
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 		String featureWrapperXMLName = rmfExtendedMetaData.getXMLWrapperName(eStructuralFeature);
 		EPackage ePackage = eStructuralFeature.getEContainingClass().getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, featureWrapperXMLName);
 	}
 
 	protected String getClassifierQName(EClassifier eClassifier) {
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 		String classifierXMLName = rmfExtendedMetaData.getXMLName(eClassifier);
 		EPackage ePackage = eClassifier.getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, classifierXMLName);
 	}
 
 	protected String getClassifierWrapperQName(EClassifier eClassifier) {
 		assert null != rmfExtendedMetaData;
 		assert null != extendedMetaData;
 		String classifierWrapperXMLName = rmfExtendedMetaData.getXMLWrapperName(eClassifier);
 		EPackage ePackage = eClassifier.getEPackage();
 		String prefix = helper.getPrefix(ePackage);
 		return getQName(prefix, classifierWrapperXMLName);
 	}
 
 	protected String getQName(String prefix, String name) {
 		if (null == prefix || 0 == prefix.length()) {
 			return name;
 		} else {
 			return prefix + ":" + name; //$NON-NLS-1$
 		}
 	}
 
 	@Override
 	protected void init(XMLResource resource, Map<?, ?> options) {
 		super.init(resource, options);
 		Object extendedMetaDataOption = options.get(XMLResource.OPTION_EXTENDED_META_DATA);
 		if (extendedMetaDataOption instanceof Boolean) {
 			if (extendedMetaDataOption.equals(Boolean.TRUE)) {
 				rmfExtendedMetaData = resource == null || resource.getResourceSet() == null ? RMFExtendedMetaData.INSTANCE
 						: new RMFExtendedMetaDataImpl(resource.getResourceSet().getPackageRegistry());
 				extendedMetaData = rmfExtendedMetaData;
 			}
 		} else if (extendedMetaDataOption instanceof RMFExtendedMetaData) {
 			rmfExtendedMetaData = (RMFExtendedMetaData) options.get(XMLResource.OPTION_EXTENDED_META_DATA);
 			extendedMetaData = rmfExtendedMetaData;
 		}
 	}
 
	protected Set<EClass> getOrderedClasses(List<? extends InternalEObject> values) {
		Set<EClass> classes = new LinkedHashSet<EClass>();
 		for (EObject value : values) {
			classes.add(value.eClass());
 		}
 
		return classes;
 
 	}
 
 }
