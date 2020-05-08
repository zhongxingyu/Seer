 /**
  * Copyright (c) 2013 itemis AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 
 package org.eclipse.rmf.internal.serialization;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.xerces.impl.Constants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.xmi.ClassNotFoundException;
 import org.eclipse.emf.ecore.xmi.FeatureNotFoundException;
 import org.eclipse.emf.ecore.xmi.XMLHelper;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler;
 import org.eclipse.emf.ecore.xml.type.AnyType;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingExtendedMetaData;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingExtendedMetaDataImpl;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingResource;
 
 // TODO: add javadoc for each state chart
 // TODO: add pool that reuses loadpatterns (reduces GC overhead)
 // TODO: assert that extended metadata is available. 
 // TODO: assert that xmlMap is never used
 // TODO: check for correct handling of types, object stacks.
 // TODO: consider using symboltyble and replace .equals by ==
 // TODO: create assertions in case the Handler is not used corretly. e.g. extennded metadata has incorrect format
 // TODO: log info if options are used that are not supported
 public class XMLPersistenceMappingHandler extends SAXXMLHandler {
 	String xsiType;
 
 	XMLPersistenceMappingExtendedMetaData xmlPersistenceMappingExtendedMetaData = null;
 	MyStack<LoadPattern> loadPatternStack = null;
 
 	IProgressMonitor progressMonitor = null;
 	int progressMonitorChunksRead = 0;
 	int progressMonitorChunkSize = 2048;
 	int progressMonitorLastStartInChunk = 0;
 	int progressMonitorNumberOfChunksPerUpdate = 500;
	private Set<String> progressReportedNamespaces = new HashSet<String>();
 
 	interface LoadPattern {
 		public static int STATE_READY = 0;
 		public static int STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT = 1;
 		public static int STATE_HAS_SEEN_START_FEATURE_ELEMENT = 2;
 		public static int STATE_HAS_SEEN_START_CLASSIFIER_WRAPPER_ELEMENT = 3;
 		public static int STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT = 4;
 		public static int STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT = 5;
 		public static int STATE_HAS_SEEN_END_FEATURE_ELEMENT = 6;
 		public static int STATE_HAS_SEEN_END_CLASSIFIER_WRAPPER_ELEMENT = 7;
 		public static int STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT = 8;
 		public static int STATE_DELEGATE_CHILD_NEEDED = -1;
 		public static int STATE_DELEGATE_PARENT_NEEDED = -2;
 		public static int STATE_DELEGATE_SIBLING_NEEDED = -3;
 
 		public static int STATE_UNEXPECTED_ELEMENT = -100;
 
 		void startElement(String namespace, String xmlName);
 
 		void endElement(String namespace, String xmlName);
 
 		boolean needsDelegateChild();
 
 		boolean needsDelegateSibling();
 
 		boolean needsDelegateParent();
 
 	}
 
 	abstract class AbstractLoadPatternImpl implements LoadPattern {
 		final EObject anchorEObject;
 		final EStructuralFeature feature;
 		int currentState;
 
 		public AbstractLoadPatternImpl(EObject anchorEObject, EStructuralFeature feature) {
 			this.anchorEObject = anchorEObject;
 			this.feature = feature;
 			currentState = STATE_READY;
 		}
 
 		public boolean needsDelegateChild() {
 			return STATE_DELEGATE_CHILD_NEEDED == currentState;
 		}
 
 		public boolean needsDelegateParent() {
 			return STATE_DELEGATE_PARENT_NEEDED == currentState;
 		}
 
 		public boolean needsDelegateSibling() {
 			return STATE_DELEGATE_SIBLING_NEEDED == currentState;
 		}
 
 	}
 
 	class LoadPatternEReferenceContained0001Impl extends AbstractLoadPatternImpl {
 		String classifierName = null;
 
 		public LoadPatternEReferenceContained0001Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				classifierName = xmlName;
 				createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				currentState = STATE_DELEGATE_CHILD_NEEDED;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				if (xmlName.equals(classifierName)) {
 					currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 					createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				// this happens if there are no nested structures
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 				// this happens if there are nested structures
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceContained0100Impl extends AbstractLoadPatternImpl {
 		String featureName;
 
 		public LoadPatternEReferenceContained0100Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				featureName = xmlName;
 				// This puts the newly created eObject onto the objects stack
 				createObject(anchorEObject, feature);
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_DELEGATE_CHILD_NEEDED;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				if (featureName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 					createObject(anchorEObject, feature);
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceContained0101Impl extends AbstractLoadPatternImpl {
 		String featureName = null;
 
 		public LoadPatternEReferenceContained0101Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				featureName = xmlName;
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 
 				createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				currentState = STATE_DELEGATE_CHILD_NEEDED;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				// for robustness: we allow multiple classiefier elements in a feature element. a warning should be
 				// created
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				if (featureName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				// only the feature element was available => consider it as a null value;
 
 				if (feature.isMany()) {
 					@SuppressWarnings("unchecked")
 					EList<EObject> values = (EList<EObject>) anchorEObject.eGet(feature);
 					values.clear();
 				} else {
 					setFeatureValue(anchorEObject, feature, null);
 				}
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceContained1001Impl extends AbstractLoadPatternImpl {
 		String featureWrapperName = null;
 
 		public LoadPatternEReferenceContained1001Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				featureWrapperName = xmlName;
 				currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				currentState = STATE_DELEGATE_CHILD_NEEDED;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				if (featureWrapperName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				// only the feature element was available => consider it as a null value;
 				if (feature.isMany()) {
 					@SuppressWarnings("unchecked")
 					EList<EObject> values = (EList<EObject>) anchorEObject.eGet(feature);
 					values.clear();
 				} else {
 					setFeatureValue(anchorEObject, feature, null);
 				}
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 				handleEndCreateObjectElement();
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	/**
 	 * This class implements.... <img src="doc-files/LoadPatternEAttributeContained0100.png">
 	 */
 	class LoadPatternEAttributeContained0100Impl extends AbstractLoadPatternImpl {
 		String featureName = null;
 		int depthsOfUnknownElements = 0;
 		String value = null;
 
 		public LoadPatternEAttributeContained0100Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				featureName = xmlName;
 				types.push(feature);
 				if (!isNull()) {
 					text = new StringBuffer();
 				}
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				if (featureName.equals(xmlName)) {
 					text = new StringBuffer(); // record all strings
 					currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 					// wait to read contained text
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				if (isNull()) {
 					setFeatureValue(anchorEObject, feature, null);
 				} else {
 					setFeatureValue(anchorEObject, feature, text == null ? null : text.toString());
 				}
 				text = null;
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEAttributeContained1000Impl extends AbstractLoadPatternImpl {
 		String featureWrapperName = null;
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternEAttributeContained1000Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				featureWrapperName = xmlName;
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				if (featureWrapperName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 					text = new StringBuffer(); // record all strings
 					// wait to read contained text
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				if (null == text) {
 					setFeatureValue(anchorEObject, feature, null);
 				} else {
 					String svalues = text.toString();
 					StringTokenizer st = new StringTokenizer(svalues);
 					String svalue;
 					while (st.hasMoreTokens()) {
 						svalue = st.nextToken();
 						setFeatureValue(anchorEObject, feature, svalue);
 					}
 				}
 				text = null;
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEAttributeContained1100Impl extends AbstractLoadPatternImpl {
 		String featureWrapperName = null;
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternEAttributeContained1100Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				featureWrapperName = xmlName;
 				currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				if (featureWrapperName == xmlName) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_READY;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				setFeatureValue(anchorEObject, feature, text == null ? null : text.toString());
 				text = null;
 				currentState = STATE_READY;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceReferenced0100Impl extends AbstractLoadPatternImpl {
 		String featureName = null;
 		InternalEObject proxy;
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternEReferenceReferenced0100Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				createObject(anchorEObject, feature);
 				proxy = (InternalEObject) objects.peekEObject();
 				text = new StringBuffer(); // record all strings
 				featureName = xmlName;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				if (featureName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 					createObject(anchorEObject, feature);
 					proxy = (InternalEObject) objects.peekEObject();
 					text = new StringBuffer(); // record all strings
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				if (null != proxy) {
 					handleProxy(proxy, resourceURI.toString() + "#" + text.toString()); //$NON-NLS-1$
 					objects.pop();
 				} else {
 					// NOP: error already handled during startElement()
 				}
 				text = null;
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceReferenced0101Impl extends AbstractLoadPatternImpl {
 		String featureName = null;
 		InternalEObject proxy = null;
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternEReferenceReferenced0101Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				featureName = xmlName;
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				proxy = (InternalEObject) createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				// for robustness: we allow multiple classifier elements in a feature element. a warning should be
 				// created
 				proxy = (InternalEObject) createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				if (featureName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				if (null != proxy) {
 					handleProxy(proxy, resourceURI.toString() + "#" + text.toString()); //$NON-NLS-1$
 					objects.pop();
 				} else {
 					// NOP: error already handled during startElement
 				}
 				text = null;
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceReferenced1001Impl extends AbstractLoadPatternImpl {
 		InternalEObject proxy = null;
 		String featureWrapperName = null;
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternEReferenceReferenced1001Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				featureWrapperName = xmlName;
 				currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				proxy = (InternalEObject) createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				// for robustness: we allow multiple classiefier elements in a feature element. a warning should be
 				// created
 				currentState = STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT;
 				proxy = (InternalEObject) createObjectFromNamespaceAndType(anchorEObject, feature, namespace, xmlName);
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				if (featureWrapperName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				// only the feature element was available => consider it as a null value;
 				if (feature.isMany()) {
 					@SuppressWarnings("unchecked")
 					EList<EObject> values = (EList<EObject>) anchorEObject.eGet(feature);
 					values.clear();
 				} else {
 					setFeatureValue(anchorEObject, feature, null);
 				}
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_CLASSIFIER_ELEMENT:
 				if (null != proxy) {
 					objects.pop();
 					handleProxy(proxy, resourceURI.toString() + "#" + text.toString()); //$NON-NLS-1$
 				}
 
 				text = null;
 				currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_CLASSIFIER_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternEReferenceReferenced1100Impl extends AbstractLoadPatternImpl {
 		String featureWrapperName = null;
 		EObject proxy;
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternEReferenceReferenced1100Impl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				featureWrapperName = xmlName;
 				currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				createObject(anchorEObject, feature);
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				if (featureWrapperName.equals(xmlName)) {
 					currentState = STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT;
 				} else {
 					currentState = STATE_DELEGATE_SIBLING_NEEDED;
 				}
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_START_FEATURE_ELEMENT;
 				createObject(anchorEObject, feature);
 				text = new StringBuffer(); // record all strings
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_READY;
 				break;
 			case STATE_HAS_SEEN_START_FEATURE_ELEMENT:
 				if (null != proxy) {
 					handleProxy((InternalEObject) objects.peekEObject(), resourceURI.toString() + "#" + text.toString()); //$NON-NLS-1$
 					objects.pop();
 				}
 				text = null;
 				currentState = STATE_READY;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_HAS_SEEN_END_FEATURE_ELEMENT:
 				currentState = STATE_HAS_SEEN_END_FEATURE_WRAPPER_ELEMENT;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_HAS_SEEN_END_FEATURE_ELEMENT;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	class LoadPatternUnknownImpl extends AbstractLoadPatternImpl {
 		int depthsOfUnknownElements = 0;
 
 		public LoadPatternUnknownImpl(EObject anchorEObject, EStructuralFeature feature) {
 			super(anchorEObject, feature);
 		}
 
 		public void startElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_UNEXPECTED_ELEMENT;
 				depthsOfUnknownElements = 1;
 				types.push(ERROR_TYPE);
 				error(new FeatureNotFoundException(xmlName, null, getLocation(), getLineNumber(), getColumnNumber()));
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements++;
 				break;
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		public void endElement(String namespace, String xmlName) {
 			switch (currentState) {
 			case STATE_READY:
 				currentState = STATE_DELEGATE_PARENT_NEEDED;
 				break;
 			case STATE_UNEXPECTED_ELEMENT:
 				depthsOfUnknownElements--;
 				if (0 > depthsOfUnknownElements) {
 					currentState = STATE_DELEGATE_PARENT_NEEDED;
 				}
 				break;
 			case STATE_DELEGATE_CHILD_NEEDED:
 			case STATE_DELEGATE_SIBLING_NEEDED:
 			case STATE_DELEGATE_PARENT_NEEDED:
 				assert false : "handshake error: the dispatcher should have switched to another load pattern instance (state=" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			default:
 				assert false : "state machine error: unsupported state (state =" + currentState + ", startElement)"; //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 	}
 
 	public XMLPersistenceMappingHandler(XMLResource xmlResource, XMLHelper helper, Map<?, ?> options) {
 		super(xmlResource, helper, options);
 
 		// set extended meta data
 		Object extendedMetaDataOption = options.get(XMLResource.OPTION_EXTENDED_META_DATA);
 		if (null != extendedMetaDataOption && extendedMetaDataOption instanceof XMLPersistenceMappingExtendedMetaData) {
 			xmlPersistenceMappingExtendedMetaData = (XMLPersistenceMappingExtendedMetaData) options.get(XMLResource.OPTION_EXTENDED_META_DATA);
 			extendedMetaData = xmlPersistenceMappingExtendedMetaData;
 		} else {
 			// default
 			xmlPersistenceMappingExtendedMetaData = xmlResource == null || xmlResource.getResourceSet() == null ? XMLPersistenceMappingExtendedMetaData.INSTANCE
 					: new XMLPersistenceMappingExtendedMetaDataImpl(xmlResource.getResourceSet().getPackageRegistry());
 			extendedMetaData = xmlPersistenceMappingExtendedMetaData;
 		}
 		helper.setExtendedMetaData(xmlPersistenceMappingExtendedMetaData);
 
 		// initialize progress monitor
 		Object progressMonitor = options.get(XMLPersistenceMappingResource.OPTION_PROGRESS_MONITOR);
 		if (progressMonitor instanceof IProgressMonitor) {
 			this.progressMonitor = (IProgressMonitor) progressMonitor;
 		} else {
 			// ignore
 		}
 
 		// get chunk size of xml parser, that is required for calculation of progress monitor ticks
 		Object parserPropertiesObject = options.get(XMLResource.OPTION_PARSER_PROPERTIES);
 		if (null != parserPropertiesObject && parserPropertiesObject instanceof Map<?, ?>) {
 			@SuppressWarnings("unchecked")
 			Map<String, Object> parserProperties = (Map<String, Object>) parserPropertiesObject;
 			Object bufferSizeObject = parserProperties.get(Constants.XERCES_PROPERTY_PREFIX + Constants.BUFFER_SIZE_PROPERTY);
 			if (bufferSizeObject instanceof Integer) {
 				progressMonitorChunkSize = (Integer) bufferSizeObject;
 			} else {
 				// use default;
 			}
 		} else {
 			// use default;
 		}
 
 		loadPatternStack = new MyStack<LoadPattern>();
 		xsiType = null;
 
 		// redefine href attribute since e.g.:
 		// xhtml.a.type/@href conflicts with this attribute and results in proxy resolution which can in turn
 		// result in long delays during load.
 		hrefAttribute = XMLPersistenceMappingResource.HREF;
 
 		// postconditions
 		assert null != xmlPersistenceMappingExtendedMetaData;
 		assert null != extendedMetaData;
 
 	}
 
 	/**
 	 * Create an object based on the given feature and attributes.
 	 */
 	protected EObject createObjectFromNamespaceAndType(EObject peekObject, EStructuralFeature feature, String namespace, String typeXMLName) {
 		assert null != xmlPersistenceMappingExtendedMetaData;
 		assert null != peekObject;
 		assert null != feature;
 		assert null != namespace;
 		assert null != typeXMLName;
 
		// Inform the progress monitor. As looking up the namespace can be slow (network),
		// it will at least tell the user what's taking so long
		if (progressMonitor != null && namespace != null && !progressReportedNamespaces.contains(namespace)) {
			progressMonitor.subTask("Processing Namespace: " + namespace); //$NON-NLS-1$
			progressReportedNamespaces.add(namespace);
		}

 		// make sure, that information about feature is available for handleMissingPackage
 		contextFeature = feature;
 		EPackage ePackage = getPackageForURI(namespace);
 		contextFeature = null;
 
 		if (null != ePackage) {
 			EClassifier eClassifier;
 			if (xmlPersistenceMappingExtendedMetaData.demandedPackages().contains(ePackage)) {
 				// demand package requires demand type
 				eClassifier = xmlPersistenceMappingExtendedMetaData.demandType(namespace, typeXMLName);
 			} else {
 				eClassifier = xmlPersistenceMappingExtendedMetaData.getTypeByXMLName(namespace, typeXMLName, feature);
 			}
 
 			EFactory eFactory = ePackage.getEFactoryInstance();
 
 			if (null != eClassifier) {
 				EObject obj = createObject(eFactory, eClassifier, false);
 				obj = validateCreateObjectFromFactory(eFactory, typeXMLName, obj, feature);
 				if (obj != null) {
 					setFeatureValue(peekObject, feature, obj);
 					processObject(obj);
 				}
 				return obj;
 			} else {
 				error(new ClassNotFoundException(typeXMLName, eFactory, getLocation(), getLineNumber(), getColumnNumber()));
 				return null;
 			}
 
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	protected EObject validateCreateObjectFromFactory(EFactory factory, String typeName, EObject newObject, EStructuralFeature feature) {
 		if (newObject != null) {
 			if (extendedMetaData != null) {
 				Collection<EPackage> demandedPackages = extendedMetaData.demandedPackages();
 				if (!demandedPackages.isEmpty() && demandedPackages.contains(newObject.eClass().getEPackage())) {
 					if (xmlPersistenceMappingExtendedMetaData.isXMLPersistenceMappingEnabled(feature)) {
 						List<String> wildcards = xmlPersistenceMappingExtendedMetaData.getWildcards(feature);
 						if (xmlPersistenceMappingExtendedMetaData.matches(wildcards, newObject.eClass().getEPackage().getNsURI())) {
 							return newObject;
 						} else {
 							return null;
 						}
 					} else {
 						// the new object resulted from a demanded package
 						if (recordUnknownFeature) {
 							// recordUnknownFeature
 							EObject peekObject = objects.peekEObject();
 							if (!(peekObject instanceof AnyType)) {
 								AnyType anyType = getExtension(objects.peekEObject());
 								EStructuralFeature entryFeature = extendedMetaData.demandFeature(extendedMetaData.getNamespace(feature),
 										extendedMetaData.getName(feature), true);
 								anyType.getAny().add(entryFeature, newObject);
 								contextFeature = entryFeature;
 							}
 							return newObject;
 						} else {
 							// reportUnknowFeature
 							String namespace = extendedMetaData.getNamespace(feature);
 							String name = extendedMetaData.getName(feature);
 							EStructuralFeature wildcardFeature = extendedMetaData.getElementWildcardAffiliation(objects.peekEObject().eClass(),
 									namespace, name);
 							if (wildcardFeature != null) {
 								int processingKind = laxWildcardProcessing ? ExtendedMetaData.LAX_PROCESSING : extendedMetaData
 										.getProcessingKind(wildcardFeature);
 								switch (processingKind) {
 								case ExtendedMetaData.LAX_PROCESSING:
 								case ExtendedMetaData.SKIP_PROCESSING: {
 									return newObject;
 								}
 								}
 							}
 						}
 
 						newObject = null;
 					}
 				}
 			}
 		} else if (feature != null && factory != null && extendedMetaData != null) {
 			// processing unknown feature with xsi:type (xmi:type)
 			if (recordUnknownFeature || processAnyXML) {
 
 				EObject result = null;
 				String namespace = extendedMetaData.getNamespace(factory.getEPackage());
 				if (namespace == null) {
 					usedNullNamespacePackage = true;
 				}
 
 				EClassifier type = extendedMetaData.demandType(namespace, typeName);
 				result = createObject(type.getEPackage().getEFactoryInstance(), type, false);
 
 				EObject peekObject = objects.peekEObject();
 				if (!(peekObject instanceof AnyType)) {
 					// add as extension to an existing eobject
 					AnyType anyType = getExtension(peekObject);
 					EStructuralFeature entryFeature = extendedMetaData.demandFeature(extendedMetaData.getNamespace(feature),
 							extendedMetaData.getName(feature), true);
 					anyType.getAny().add(entryFeature, result);
 					contextFeature = entryFeature;
 				}
 				return result;
 			} else {
 				String namespace = extendedMetaData.getNamespace(feature);
 				String name = extendedMetaData.getName(feature);
 				EStructuralFeature wildcardFeature = extendedMetaData.getElementWildcardAffiliation(objects.peekEObject().eClass(), namespace, name);
 				if (wildcardFeature != null) {
 					int processingKind = laxWildcardProcessing ? ExtendedMetaData.LAX_PROCESSING : extendedMetaData
 							.getProcessingKind(wildcardFeature);
 					switch (processingKind) {
 					case ExtendedMetaData.LAX_PROCESSING:
 					case ExtendedMetaData.SKIP_PROCESSING: {
 						// EATM Demand create metadata; needs to depend on processing mode...
 						String factoryNamespace = extendedMetaData.getNamespace(factory.getEPackage());
 						if (factoryNamespace == null) {
 							usedNullNamespacePackage = true;
 						}
 						EClassifier type = extendedMetaData.demandType(factoryNamespace, typeName);
 						return createObject(type.getEPackage().getEFactoryInstance(), type, false);
 					}
 					}
 				}
 			}
 		}
 
 		validateCreateObjectFromFactory(factory, typeName, newObject);
 
 		return newObject;
 	}
 
 	@Override
 	public void characters(char[] ch, int start, int length) {
 		super.characters(ch, start, length);
 		if (null != progressMonitor) {
 			if (start < progressMonitorLastStartInChunk) {
 				progressMonitorChunksRead += 1;
 			}
 			if (progressMonitorChunksRead > progressMonitorNumberOfChunksPerUpdate) {
 				progressMonitor.worked(progressMonitorChunksRead);
 				progressMonitorChunksRead = 0;
 			}
 			progressMonitorLastStartInChunk = start;
 		}
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName) {
 		if (null != xmlPersistenceMappingExtendedMetaData) {
 			elements.pop();
 			types.pop();
 
 			helper.popContext(prefixesToFactories);
 
 			// end from super.endElement
 
 			LoadPattern activeDeserializationRule = loadPatternStack.peek();
 			if (null != activeDeserializationRule) {
 				activeDeserializationRule.endElement(uri, localName);
 				if (activeDeserializationRule.needsDelegateParent()) {
 					loadPatternStack.pop();
 					activeDeserializationRule = loadPatternStack.peek();
 					if (null != activeDeserializationRule) {
 						// this happens if we return to the root object
 						activeDeserializationRule.endElement(uri, localName);
 					}
 				}
 			}
 		} else {
 			super.endElement(uri, localName, qName);
 		}
 	}
 
 	/**
 	 * The XML element represents a feature. There are two cases to handle: 1. The feature has a type that is a
 	 * datatype. 2. The feature has a type that is a class.
 	 */
 	@Override
 	protected void handleFeature(String prefix, String name) {
 		assert useNewMethods == true;
 		EObject peekObject = objects.peekEObject();
 
 		// This happens when processing an element with simple content that has elements content even though it
 		// shouldn't.
 		//
 		if (peekObject == null) {
 			types.push(ERROR_TYPE);
 			error(new FeatureNotFoundException(name, null, getLocation(), getLineNumber(), getColumnNumber()));
 			return;
 		}
 
 		if (null != xmlPersistenceMappingExtendedMetaData) {
 			String namespace = helper.getNamespaceURI(prefix);
 			LoadPattern activeDeserializationRule = loadPatternStack.peek();
 			if (null == activeDeserializationRule) {
 				activeDeserializationRule = getLoadPattern(peekObject, prefix, name);
 				assert null != activeDeserializationRule : "getLoadPattern() should never return null"; //$NON-NLS-1$
 				loadPatternStack.push(activeDeserializationRule);
 			}
 
 			if (null != activeDeserializationRule) {
 				activeDeserializationRule.startElement(namespace, name);
 				if (activeDeserializationRule.needsDelegateChild()) {
 					activeDeserializationRule = getLoadPattern(peekObject, prefix, name);
 					assert null != activeDeserializationRule : "getLoadPattern() should never return null"; //$NON-NLS-1$
 					loadPatternStack.push(activeDeserializationRule);
 					activeDeserializationRule.startElement(namespace, name);
 				} else if (activeDeserializationRule.needsDelegateSibling()) {
 					activeDeserializationRule = getLoadPattern(peekObject, prefix, name);
 					assert null != activeDeserializationRule : "getLoadPattern() should never return null"; //$NON-NLS-1$
 					loadPatternStack.pop();
 					loadPatternStack.push(activeDeserializationRule);
 					activeDeserializationRule.startElement(namespace, name);
 				} else {
 					// normal operation, no need for switching to another load pattern
 				}
 
 			}
 
 		} else {
 			EStructuralFeature feature = getFeature(peekObject, prefix, name, true);
 			if (feature != null) {
 				int kind = helper.getFeatureKind(feature);
 				if (kind == XMLHelper.DATATYPE_SINGLE || kind == XMLHelper.DATATYPE_IS_MANY) {
 					objects.push(null);
 					mixedTargets.push(null);
 					types.push(feature);
 					if (!isNull()) {
 						text = new StringBuffer();
 					}
 				} else if (extendedMetaData != null) {
 					EReference eReference = (EReference) feature;
 					boolean isContainment = eReference.isContainment();
 					if (!isContainment && !eReference.isResolveProxies()
 							&& extendedMetaData.getFeatureKind(feature) != ExtendedMetaData.UNSPECIFIED_FEATURE) {
 						isIDREF = true;
 						objects.push(null);
 						mixedTargets.push(null);
 						types.push(feature);
 						text = new StringBuffer();
 					} else {
 						createObject(peekObject, feature);
 						// child object is put on top of objects stack
 						// if an error occured, then types.peek is ERROR
 						//
 						EObject childObject = objects.peekEObject();
 						if (childObject != null) {
 							if (isContainment) {
 								EStructuralFeature simpleFeature = extendedMetaData.getSimpleFeature(childObject.eClass());
 								if (simpleFeature != null) {
 									isSimpleFeature = true;
 									isIDREF = simpleFeature instanceof EReference;
 									objects.push(null);
 									mixedTargets.push(null);
 									types.push(simpleFeature);
 									text = new StringBuffer();
 								}
 							} else if (!childObject.eIsProxy()) {
 								text = new StringBuffer();
 							}
 						}
 					}
 				} else {
 					createObject(peekObject, feature);
 				}
 			} else {
 				// Try to get a general-content feature.
 				// Use a pattern that's not possible any other way.
 				//
 				if (xmlMap != null && (feature = getFeature(peekObject, null, "", true)) != null) { //$NON-NLS-1$
 
 					EFactory eFactory = getFactoryForPrefix(prefix);
 
 					// This is for the case for a local unqualified element that has been bound.
 					//
 					if (eFactory == null) {
 						eFactory = feature.getEContainingClass().getEPackage().getEFactoryInstance();
 					}
 
 					EObject newObject = null;
 					newObject = createObject(eFactory, helper.getType(eFactory, name), false);
 					newObject = validateCreateObjectFromFactory(eFactory, name, newObject, feature);
 					if (newObject != null) {
 						setFeatureValue(peekObject, feature, newObject);
 					}
 					processObject(newObject);
 				} else {
 					// This handles the case of a substitution group.
 					//
 					if (xmlMap != null) {
 						EFactory eFactory = getFactoryForPrefix(prefix);
 						EObject newObject = createObjectFromFactory(eFactory, name);
 						validateCreateObjectFromFactory(eFactory, name, newObject);
 						if (newObject != null) {
 							for (EReference eReference : peekObject.eClass().getEAllReferences()) {
 								if (eReference.getEType().isInstance(newObject)) {
 									setFeatureValue(peekObject, eReference, newObject);
 									processObject(newObject);
 									return;
 								}
 							}
 						}
 					}
 
 					handleUnknownFeature(prefix, name, true, peekObject, null);
 				}
 			}
 		}
 	}
 
 	protected LoadPattern getLoadPattern(EObject eObject, String prefix, String name) {
 		assert null != xmlPersistenceMappingExtendedMetaData;
 
 		LoadPattern deserializationRule = null;
 
 		if (eObject instanceof AnyType) {
 			// handle any type using standard emf serialization
 			EStructuralFeature feature = super.getFeature(eObject, prefix, name, true);
 			deserializationRule = new LoadPatternEReferenceContained0100Impl(eObject, feature);
 		} else {
 			EStructuralFeature feature = getFeature(eObject, prefix, name, true);
 			if (null != feature) {
 				int featureSerializationStructure = xmlPersistenceMappingExtendedMetaData.getFeatureSerializationStructure(feature);
 				if (feature instanceof EReference) {
 					EReference reference = (EReference) feature;
 					if (reference.isContainment()) {
 						switch (featureSerializationStructure) {
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0001__CLASSIFIER_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceContained0001Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceContained0100Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceContained0101Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceContained1001Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 							deserializationRule = new LoadPatternEReferenceContained0100Impl(eObject, feature);
 							break;
 						default:
 							deserializationRule = new LoadPatternEReferenceContained1001Impl(eObject, feature);
 							break;
 						}
 					} else {
 						switch (featureSerializationStructure) {
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceReferenced0100Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceReferenced0101Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT:
 							deserializationRule = new LoadPatternEReferenceReferenced1001Impl(eObject, feature);
 							break;
 						case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 							deserializationRule = new LoadPatternEReferenceReferenced0100Impl(eObject, feature);
 							break;
 						default:
 							deserializationRule = new LoadPatternEReferenceReferenced1001Impl(eObject, feature);
 							break;
 						}
 					}
 				} else {
 					// feature is an EAttribute
 					switch (featureSerializationStructure) {
 					case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__0100__FEATURE_ELEMENT:
 						deserializationRule = new LoadPatternEAttributeContained0100Impl(eObject, feature);
 						break;
 					case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1000__FEATURE_WRAPPER_ELEMENT:
 						deserializationRule = new LoadPatternEAttributeContained1000Impl(eObject, feature);
 						break;
 					case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT:
 						deserializationRule = new LoadPatternEAttributeContained1100Impl(eObject, feature);
 						break;
 					case XMLPersistenceMappingExtendedMetaData.SERIALIZATION_STRUCTURE__UNDEFINED:
 						deserializationRule = new LoadPatternEAttributeContained0100Impl(eObject, feature);
 						break;
 
 					default:
 						deserializationRule = new LoadPatternEAttributeContained0100Impl(eObject, feature);
 						break;
 					}
 
 				}
 			} else {
 				deserializationRule = new LoadPatternUnknownImpl(eObject, feature);
 			}
 		}
 
 		assert null != deserializationRule : "getLoadPattern() should always return a LoadPattern (!=null)"; //$NON-NLS-1$
 		return deserializationRule;
 
 	}
 
 	@Override
 	protected EStructuralFeature getFeature(EObject object, String prefix, String name, boolean isElement) {
 		assert null != object;
 		assert null != name;
 		EStructuralFeature result = null;
 		if (isElement && null != xmlPersistenceMappingExtendedMetaData) {
 			String namespace = helper.getNamespaceURI(prefix);
 			result = xmlPersistenceMappingExtendedMetaData.getFeatureByXMLElementName(object.eClass(), namespace, name);
 		} else {
 			result = super.getFeature(object, prefix, name, isElement);
 		}
 		return result;
 	}
 
 	@Override
 	public void reset() {
 		super.reset();
 		loadPatternStack = null;
 		hrefAttribute = XMLPersistenceMappingResource.HREF;
 
 	};
 
 	@Override
 	public void prepare(XMLResource resource, XMLHelper helper, Map<?, ?> options) {
 		super.prepare(resource, helper, options);
 		loadPatternStack = new MyStack<LoadPattern>();
 		// enforce use of new methods
 		useNewMethods = true;
 		xsiType = null;
 	}
 
 	@Override
 	protected String getXSIType() {
 		if (xsiType != null) {
 			return xsiType;
 		} else {
 			return super.getXSIType();
 		}
 	}
 
 	protected void handleEndCreateObjectElement() {
 		if (text == null) {
 			objects.pop();
 			mixedTargets.pop();
 		} else {
 			EObject object = objects.popEObject();
 			if (mixedTargets.peek() != null
 					&& (object.eContainer() != null || suppressDocumentRoot || recordUnknownFeature
 							&& (eObjectToExtensionMap.containsValue(object) || ((InternalEObject) object).eDirectResource() != null))) {
 				handleMixedText();
 				mixedTargets.pop();
 			}
 		}
 	}
 
 	@Override
 	public void startDocument() {
 		super.startDocument();
 		// initialize the progress monitor
 		if (null != progressMonitor) {
 			progressMonitorChunksRead = 0;
 			progressMonitorLastStartInChunk = 0;
 			progressMonitor.beginTask("Reading resource '" + resourceURI + "'", getProgressMonitorTotalWork()); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 	}
 
 	@Override
 	public void endDocument() {
 		super.endDocument();
 		// finalize the progress monitor
 		if (null != progressMonitor) {
 			progressMonitor.done();
 		}
 	}
 
 	protected int getProgressMonitorTotalWork() {
 		int totalWork = IProgressMonitor.UNKNOWN;
 		URI uri = xmlResource.getURI();
 
 		InputStream is = null;
 		try {
 			is = URIConverter.INSTANCE.createInputStream(uri);
 			totalWork = is.available() / progressMonitorChunkSize;
 		} catch (IOException ex) {
 			// NOP
 		} finally {
 			if (null != is) {
 				try {
 					is.close();
 				} catch (IOException ex) {
 					// NOP
 				}
 			}
 		}
 		return totalWork;
 	}
 
 	@Override
 	protected EPackage handleMissingPackage(String uriString) {
 		if (null != contextFeature && null != xmlPersistenceMappingExtendedMetaData
 				&& xmlPersistenceMappingExtendedMetaData.isXMLPersistenceMappingEnabled(contextFeature)) {
 			return xmlPersistenceMappingExtendedMetaData.demandPackage(uriString);
 
 		} else {
 			return super.handleMissingPackage(uriString);
 		}
 	}
 
 }
