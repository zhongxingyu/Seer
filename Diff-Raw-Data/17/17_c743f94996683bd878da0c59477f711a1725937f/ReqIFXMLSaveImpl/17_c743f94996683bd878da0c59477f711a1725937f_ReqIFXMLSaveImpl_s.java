 /**
  * Copyright (c) 2012 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.internal.serialization;
 
 import java.util.Iterator;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
 import org.eclipse.rmf.reqif10.ReqIFToolExtension;
 import org.eclipse.rmf.reqif10.XhtmlContent;
 
 /**
  * TODO: write down assumptions as asserts
  * 
  * @author broerkens
  */
 public class ReqIFXMLSaveImpl extends XMLSaveImpl implements IReqIFSerializationConstants {
 	private static final String EMPTY_URI = ""; //$NON-NLS-1$
 
 	private SerializationStrategy serializationStrategy = SerializationStrategy.REQIF;
 
 	public ReqIFXMLSaveImpl(XMLHelper xmlHelper) {
 		super(xmlHelper);
 	}
 
 	@Override
 	protected boolean saveFeatures(EObject o, boolean attributesOnly) {
 		if (SerializationStrategy.REQIF != serializationStrategy && SerializationStrategy.TOOL_EXTENSION != serializationStrategy) {
 			return super.saveFeatures(o, attributesOnly);
 		} else {
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
 			LOOP: for (int i = 0; i < features.length; i++) {
 				int kind = featureKinds[i];
 				EStructuralFeature f = features[i];
 				if (kind != TRANSIENT && shouldSaveFeature(o, f)) {
 					switch (kind) {
 					case DATATYPE_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getDataTypeElementSingleSimple(o, f);
 							continue LOOP;
 						}
 						break;
 					}
 					case DATATYPE_SINGLE: {
 						saveDataTypeSingle(o, f);
 						continue LOOP;
 					}
 					case DATATYPE_SINGLE_NILLABLE: {
 						if (!isNil(o, f)) {
 							saveDataTypeSingle(o, f);
 							continue LOOP;
 						}
 						break;
 					}
 					case OBJECT_ATTRIBUTE_SINGLE: {
 						saveEObjectSingle(o, f);
 						continue LOOP;
 					}
 					case OBJECT_ATTRIBUTE_MANY: {
 						saveEObjectMany(o, f);
 						continue LOOP;
 					}
 					case OBJECT_ATTRIBUTE_IDREF_SINGLE: {
 						saveIDRefSingle(o, f);
 						continue LOOP;
 					}
 					case OBJECT_ATTRIBUTE_IDREF_MANY: {
 						saveIDRefMany(o, f);
 						continue LOOP;
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
 							continue LOOP;
 						} else {
 							switch (sameDocSingle(o, f)) {
 							case SAME_DOC: {
 								saveIDRefSingle(o, f);
 								continue LOOP;
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP;
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_HREF_MANY_UNSETTABLE: {
 						if (isEmpty(o, f)) {
 							saveManyEmpty(o, f);
 							continue LOOP;
 						}
 						// It's intentional to keep going.
 					}
 					case OBJECT_HREF_MANY: {
 						if (useEncodedAttributeStyle) {
 							saveEObjectMany(o, f);
 							continue LOOP;
 						} else {
 							switch (sameDocMany(o, f)) {
 							case SAME_DOC: {
 								saveIDRefMany(o, f);
 								continue LOOP;
 							}
 							case CROSS_DOC: {
 								break;
 							}
 							default: {
 								continue LOOP;
 							}
 							}
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceSingleSimple(o, f);
 							continue LOOP;
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementReferenceManySimple(o, f);
 							continue LOOP;
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_SINGLE_UNSETTABLE:
 					case OBJECT_ELEMENT_IDREF_SINGLE: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefSingleSimple(o, f);
 							continue LOOP;
 						}
 						break;
 					}
 					case OBJECT_ELEMENT_IDREF_MANY: {
 						if (contentKind == ExtendedMetaData.SIMPLE_CONTENT) {
 							content = getElementIDRefManySimple(o, f);
 							continue LOOP;
 						}
 						break;
 					}
 					case DATATYPE_ATTRIBUTE_MANY: {
 						break;
 					}
 					case OBJECT_CONTAIN_MANY_UNSETTABLE: {
 						// needs to be saved as an element. to be handled later
 						break;
 					}
 					case DATATYPE_MANY: {
 						if (isEmpty(o, f)) {
 							saveManyEmpty(o, f);
 							continue LOOP;
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
 						continue LOOP;
 					}
 					default: {
 						continue LOOP;
 					}
 					}
 
 					if (attributesOnly) {
 						continue LOOP;
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
 					if (isEmpty(o, f)) {
 						saveContainedManyEmpty(f);
 					} else {
 						saveContainedMany(o, f);
 					}
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
 				}
 			}
 			endSaveFeatures(o, 0, null);
 			return true;
 		}
 	}
 
 	/**
 	 * write the role wrapper
 	 */
 	@Override
 	protected void saveContainedSingle(EObject o, EStructuralFeature f) {
 		if (SerializationStrategy.REQIF != serializationStrategy && SerializationStrategy.TOOL_EXTENSION != serializationStrategy) {
 			super.saveContainedSingle(o, f);
 		} else {
 			if (!toDOM) {
 				String name = helper.getQName(f);
 				doc.startElement(name);
 			} else {
 				// TODO
 				throw new UnsupportedOperationException();
 			}
 
 			super.saveContainedSingle(o, f);
 
 			if (!toDOM) {
 				doc.endElement();
 			} else {
 				// TODO
 				throw new UnsupportedOperationException();
 			}
 		}
 	}
 
 	/**
 	 * write the role wrapper
 	 */
 	@Override
 	protected void saveContainedMany(EObject o, EStructuralFeature f) {
 		if (SerializationStrategy.REQIF == serializationStrategy || SerializationStrategy.TOOL_EXTENSION == serializationStrategy) {
 			if (!toDOM) {
 				String name = helper.getQName(f);
 				doc.startElement(name);
 			} else {
 				// TODO
 				throw new UnsupportedOperationException();
 			}
 
 			super.saveContainedMany(o, f);
 
 			if (!toDOM) {
 				doc.endElement();
 			} else {
 				// TODO
 				throw new UnsupportedOperationException();
 			}
 
 		} else {
 			super.saveContainedMany(o, f);
 		}
 
 	}
 
 	protected void saveReqIFToolExtensionElement(ReqIFToolExtension toolExtension, EStructuralFeature f) {
 		EClass eClass = toolExtension.eClass();
 		String classXMLName = helper.getQName(eClass);
 		doc.startElement(classXMLName);
 
 		// now save the content of this tool extension
 
 		FeatureMap featureMap = toolExtension.getAny();
 		helper.pushContext();
 		for (Iterator<Object> valueIterator = featureMap.valueListIterator(); valueIterator.hasNext();) {
 			Object obj = valueIterator.next();
 			if (obj instanceof EObject) {
 				EObject extensionRootObj = (EObject) obj;
 				EClass extensionEClass = extensionRootObj.eClass();
 				String name = helper.getQName(extensionEClass);
 				doc.startElement(name);
 				saveElementID(extensionRootObj);
				doc.endElement();
 			}
 
 		}
 
 		doc.endElement();

		helper.popContext();
 	}
 
 	/**
 	 * avoid writing xsi:type. write XML element name instead
 	 */
 	@Override
 	protected void saveElement(EObject o, EStructuralFeature f) {
 		if (SerializationStrategy.REQIF == serializationStrategy || SerializationStrategy.TOOL_EXTENSION == serializationStrategy) {
 			if (o instanceof XhtmlContent) {
 				serializationStrategy = SerializationStrategy.XHTML;
 				XhtmlContent xhtmlContent = (XhtmlContent) o;
 				for (EReference reference : xhtmlContent.eClass().getEAllReferences()) {
 					if (xhtmlContent.eIsSet(reference)) {
 						super.saveContainedSingle(xhtmlContent, reference);
 					}
 				}
 				serializationStrategy = SerializationStrategy.REQIF;
 			} else if (o instanceof ReqIFToolExtension) {
 				serializationStrategy = SerializationStrategy.TOOL_EXTENSION;
 				ReqIFToolExtension reqIFToolExtension = (ReqIFToolExtension) o;
 
 				saveReqIFToolExtensionElement(reqIFToolExtension, f);
 				serializationStrategy = SerializationStrategy.REQIF;
 			} else {
 
 				EClass eClass = o.eClass();
 
 				if (!toDOM) {
 					String featureName = helper.getQName(eClass);
 					doc.startElement(featureName);
 				} else {
 					helper.populateNameInfo(nameInfo, eClass);
 					if (currentNode == null) {
 						// this is a root element
 						currentNode = document.createElementNS(nameInfo.getNamespaceURI(), nameInfo.getQualifiedName());
 						document.appendChild(currentNode);
 						handler.recordValues(currentNode, o.eContainer(), f, o);
 					} else {
 						currentNode = currentNode.appendChild(document.createElementNS(nameInfo.getNamespaceURI(), nameInfo.getQualifiedName()));
 						handler.recordValues(currentNode, o.eContainer(), f, o);
 					}
 				}
 
 				saveElementID(o);
 
 			}
 		} else {
 			super.saveElement(o, f);
 		}
 	}
 
 	/**
 	 * Save single element that is explicitly set to nil. In this case we expect an empty wrapper
 	 */
 	@Override
 	protected void saveNil(EStructuralFeature f) {
 		if (!toDOM) {
 			doc.startElement(helper.getQName(f));
 			doc.endElement();
 		} else {
 			// TODO
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	@Override
 	protected void saveElementReferenceSingle(EObject o, EStructuralFeature f) {
 		if (SerializationStrategy.REQIF != serializationStrategy && SerializationStrategy.TOOL_EXTENSION != serializationStrategy) {
 			super.saveElementReferenceMany(o, f);
 		} else {
 			EObject value = (EObject) helper.getValue(o, f);
 			if (value != null) {
 				if (!toDOM) {
 					doc.startElement(helper.getQName(f));
 					saveElementReference(value, f);
 					doc.endElement();
 				} else {
 					// TODO
 					throw new UnsupportedOperationException();
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void saveElementReferenceMany(EObject o, EStructuralFeature f) {
 		if (SerializationStrategy.REQIF != serializationStrategy && SerializationStrategy.TOOL_EXTENSION != serializationStrategy) {
 			super.saveElementReferenceMany(o, f);
 		} else {
 			@SuppressWarnings("unchecked")
 			InternalEList<? extends EObject> values = (InternalEList<? extends EObject>) helper.getValue(o, f);
 			int size = values.size();
 
 			if (!toDOM) {
 				doc.startElement(helper.getQName(f));
 			} else {
 				// TODO
 				throw new UnsupportedOperationException();
 			}
 
 			for (int i = 0; i < size; i++) {
 				saveElementReference(values.basicGet(i), f);
 			}
 
 			if (!toDOM) {
 				doc.endElement();
 			} else {
 				// TODO
 				throw new UnsupportedOperationException();
 			}
 		}
 
 	}
 
 	@Override
 	protected void saveElementReference(EObject remote, EStructuralFeature f) {
 		if (SerializationStrategy.REQIF != serializationStrategy && SerializationStrategy.TOOL_EXTENSION != serializationStrategy) {
 			super.saveElementReference(remote, f);
 		} else {
 			String href = helper.getHREF(remote);
 			if (href != null) {
 				href = convertURI(href);
 				EClass eClass = remote.eClass();
 
 				if (!toDOM) {
 					doc.startElement(helper.getQName(eClass) + REF);
 				} else {
 					// TODO
 					throw new UnsupportedOperationException();
 				}
 
 				if (!toDOM) {
 					doc.endContentElement(href);
 				} else {
 					// TODO
 					throw new UnsupportedOperationException();
 				}
 			}
 		}
 	}
 
 	protected void saveContainedManyEmpty(EStructuralFeature f) {
 		if (!toDOM) {
 			doc.startElement(helper.getQName(f));
 			doc.endElement();
 		} else {
 			// TODO
 			throw new UnsupportedOperationException();
 		}
 	}
 
 }
