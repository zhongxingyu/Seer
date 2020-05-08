 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.model.util;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EStructuralFeature.Setting;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
 import org.eclipse.emf.emfstore.common.CommonUtil;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;
 import org.eclipse.emf.emfstore.common.model.AssociationClassElement;
 import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.SingletonIdResolver;
 import org.eclipse.emf.emfstore.common.model.impl.ProjectImpl;
 import org.xml.sax.InputSource;
 
 /**
  * Utility class for ModelElements.
  * 
  * @author koegel
  */
 public final class ModelUtil {
 
 	private static final String ORG_ECLIPSE_EMF_EMFSTORE_COMMON_MODEL = "org.eclipse.emf.emfstore.common.model";
 
 	/**
 	 * Text marker for the begin of the plaintext in rich text attributes.
 	 */
 	public static final String BEGINTEXT_TOKEN = "%BEGINNTEXT%";
 
 	/**
 	 * URI used to serialize EObject with the model util.
 	 */
 	public static final URI VIRTUAL_URI = URI.createURI("virtualUri");
 
 	/**
 	 * Contains the canonical names of classes which will be ignored.
 	 */
 	private static Set<String> ignoredDataTypes;
 
 	/**
 	 * Contains all ID resolvers for singleton datatypes.
 	 */
 	private static Set<SingletonIdResolver> singletonIdResolvers;
 
 	private static HashMap<Object, Object> resourceLoadOptions;
 
 	private static HashMap<Object, Object> resourceSaveOptions;
 
 	/**
 	 * Private constructor.
 	 */
 	private ModelUtil() {
 		// nothing to do
 	}
 
 	/**
 	 * Creates a ModelElementId object from a string.
 	 * 
 	 * @param id
 	 *            as string
 	 * @return id as object
 	 */
 	public static ModelElementId createModelElementId(String id) {
 		ModelElementId modelElementId = ModelFactory.eINSTANCE.createModelElementId();
 		modelElementId.setId(id);
 		return modelElementId;
 	}
 
 	/**
 	 * Compares two EObject by checking whether the string representations of
 	 * the EObjects are equal.
 	 * 
 	 * @param eobjectA
 	 *            the first EObject
 	 * @param eobjectB
 	 *            the second EObject
 	 * @return true if the two objects are equal
 	 */
 	public static boolean areEqual(EObject eobjectA, EObject eobjectB) {
 		String stringA;
 		String stringB;
 		try {
 			stringA = eObjectToString(eobjectA);
 			stringB = eObjectToString(eobjectB);
 		} catch (SerializationException e) {
 			return false;
 		}
 		return stringA.equals(stringB);
 
 	}
 
 	/**
 	 * Compares two lists of EObject by checking whether the string representations of
 	 * the EObjects are equal.
 	 * 
 	 * @param listA
 	 *            the first list of EObject
 	 * @param listB
 	 *            the second list of EObject
 	 * @return true if the two lists are equal
 	 */
 	public static boolean areEqual(EList<? extends EObject> listA, EList<? extends EObject> listB) {
 		if (listA == listB) {
 			return true;
 		}
 
 		if (listA.size() != listB.size()) {
 			return false;
 		}
 
 		for (int i = 0; i < listA.size(); ++i) {
 			EObject o1 = listA.get(i);
 			EObject o2 = listB.get(i);
 			if (!areEqual(o1, o2)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Converts an EObject to a String.
 	 * 
 	 * @param object
 	 *            the eObject
 	 * @return String representation of the EObject
 	 * @throws SerializationException
 	 *             if a serialization problem occurs
 	 */
 	public static String eObjectToString(EObject object) throws SerializationException {
 
 		boolean containmentCheckEnabled = false;
 		boolean hrefCheckEnabled = false;
 		boolean proxyCheckEnabled = false;
 
 		ExtensionElement element = new ExtensionPoint("org.eclipse.emf.emfstore.common.model.serializationoptions")
 			.getFirst();
 		if (element != null) {
 			hrefCheckEnabled = element.getBoolean("HrefCheck");
 			proxyCheckEnabled = element.getBoolean("ProxyCheck");
 			containmentCheckEnabled = element.getBoolean("SelfContainmentCheck");
 		}
 
 		return eObjectToString(object, !containmentCheckEnabled, !hrefCheckEnabled, !proxyCheckEnabled);
 	}
 
 	/**
 	 * Converts an {@link EObject} to a {@link String}.
 	 * 
 	 * @param object
 	 *            the {@link EObject}
 	 * @param overrideContainmentCheck
 	 *            if true, no containment check is performed
 	 * @param overrideHrefCheck
 	 *            checks whether there is a <code>href</code> in the serialized
 	 *            text
 	 * @param overrideProxyCheck
 	 *            if true, proxy check is ignored
 	 * @return String representation of the {@link EObject}
 	 * @throws SerializationException
 	 *             if a serialization problem occurs
 	 */
 	public static String eObjectToString(EObject object, boolean overrideContainmentCheck, boolean overrideHrefCheck,
 		boolean overrideProxyCheck) throws SerializationException {
 
 		if (object == null) {
 			return null;
 		}
 
 		XMIResource res = (XMIResource) (new ResourceSetImpl()).createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 		EObject copy;
 		if (object instanceof IdEObjectCollection) {
 			copy = copyIdEObjectCollection((IdEObjectCollection) object, res);
 		} else {
 			copy = ModelUtil.clone(object);
 			res.getContents().add(copy);
 		}
 
 		if (!overrideContainmentCheck && !(copy instanceof EClass)) {
 			if (!CommonUtil.isSelfContained(copy) || !CommonUtil.isContainedInResource(copy, res)) {
 				throw new SerializationException(copy);
 			}
 		}
 
 		int step = 200;
 		int initialSize = step;
 		if (object instanceof Project) {
 			Project project = (Project) object;
 			initialSize = project.getAllModelElements().size() * step;
 		}
 		if (!overrideProxyCheck) {
 			proxyCheck(res);
 		}
 
 		StringWriter stringWriter = new StringWriter(initialSize);
 		URIConverter.WriteableOutputStream uws = new URIConverter.WriteableOutputStream(stringWriter, "UTF-8");
 		try {
 			res.save(uws, getResourceSaveOptions());
 		} catch (IOException e) {
 			throw new SerializationException(e);
 		}
 		String result = stringWriter.toString();
 
 		if (!overrideHrefCheck) {
 			hrefCheck(result);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Converts an {@link EObject} to a {@link String}.
 	 * 
 	 * @param writer
 	 *            a writer that will be used as the destination where to write the serialized EObject
 	 * @param object
 	 *            the {@link EObject} that needs to be serialized
 	 * @param overrideContainmentCheck
 	 *            if true, no containment check is performed
 	 * @param overrideHrefCheck
 	 *            checks whether there is a <code>href</code> in the serialized
 	 *            text
 	 * @param overrideProxyCheck
 	 *            if true, proxy check is ignored
 	 * @throws SerializationException
 	 *             if a serialization problem occurs
 	 */
 	public static void eobjectToString(OutputStreamWriter writer, EObject object, boolean overrideContainmentCheck,
 		boolean overrideHrefCheck, boolean overrideProxyCheck) throws SerializationException {
 
 		if (object == null) {
 			return;
 		}
 
 		XMIResource res = (XMIResource) object.eResource();
 
 		if (!overrideContainmentCheck && !(object instanceof EClass)) {
 			if (!CommonUtil.isSelfContained(object) || !CommonUtil.isContainedInResource(object, res)) {
 				throw new SerializationException(object);
 			}
 		}
 
 		if (!overrideProxyCheck) {
 			proxyCheck(res);
 		}
 
 		URIConverter.WriteableOutputStream uws = new URIConverter.WriteableOutputStream(writer, "UTF-8");
 		try {
 			res.save(uws, getResourceSaveOptions());
 		} catch (IOException e) {
 			throw new SerializationException(e);
 		}
 	}
 
 	private static EObject copyIdEObjectCollection(IdEObjectCollection collection, XMIResource res) {
 		IdEObjectCollection copiedCollection = clone(collection);
 
 		for (EObject modelElement : copiedCollection.getAllModelElements()) {
 			if (isIgnoredDatatype(modelElement)) {
 				continue;
 			}
 			ModelElementId modelElementId = copiedCollection.getModelElementId(modelElement);
 			res.setID(modelElement, modelElementId.getId());
 		}
 
 		res.getContents().add(copiedCollection);
 		return copiedCollection;
 	}
 
 	/**
 	 * @param result
 	 */
 	private static void proxyCheck(Resource resource) throws SerializationException {
 		EcoreUtil.resolveAll(resource);
 		TreeIterator<EObject> contents = resource.getAllContents();
 		while (contents.hasNext()) {
 			EObject eObject = contents.next();
 			if (eObject.eIsProxy()) {
 				throw new SerializationException("Serialization failed due to unresolved proxy detection.");
 			}
 		}
 	}
 
 	private static void hrefCheck(String result) throws SerializationException {
 		char[] needle = "href".toCharArray();
 		int pointer = 0;
 		boolean insideQuotes = false;
 		for (char character : result.toCharArray()) {
 			if (character == '"') {
 				insideQuotes = !insideQuotes;
 			}
 			if (!insideQuotes && character == needle[pointer]) {
 				if (++pointer == needle.length) {
 					throw new SerializationException("Serialization failed due to href detection.");
 				}
 			} else {
 				pointer = 0;
 			}
 		}
 	}
 
 	/**
 	 * Determines whether the type of an EObject is an ignored one.
 	 * 
 	 * @param eObject
 	 *            the EObject which is to be checked
 	 * @return true, if the EObject will be ignored, false otherwise
 	 */
 	public synchronized static boolean isIgnoredDatatype(EObject eObject) {
 
 		if (ignoredDataTypes == null) {
 			ignoredDataTypes = new HashSet<String>();
 			for (ExtensionElement element : new ExtensionPoint("org.eclipse.emf.emfstore.common.model.ignoredatatype",
 				true).getExtensionElements()) {
 				try {
 					ignoredDataTypes.add(element.getAttribute("type"));
 				} catch (ExtensionPointException e) {
 				}
 			}
 		}
 
 		return ignoredDataTypes.contains(eObject.eClass().getInstanceClassName());
 	}
 
 	/**
 	 * Converts a {@link String} to an {@link EObject}. <b>Note</b>: {@link String} must be the result of
 	 * {@link ModelUtil#eObjectToString(EObject)}
 	 * 
 	 * @param object
 	 *            the {@link String} representation of the {@link EObject}
 	 * @return the deserialized {@link EObject}
 	 * @throws SerializationException
 	 *             if deserialization fails
 	 */
 	public static EObject stringToEObject(String object) throws SerializationException {
 
 		if (object == null) {
 			return null;
 		}
 
 		XMIResource res = (XMIResource) (new ResourceSetImpl()).createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 		try {
 			res.load(new InputSource(new StringReader(object)), getResourceLoadOptions());
 		} catch (UnsupportedEncodingException e) {
 			throw new SerializationException(e);
 		} catch (IOException e) {
 			throw new SerializationException(e);
 		}
 
 		return handleParsedEObject(res);
 	}
 
 	/**
 	 * Reads from a given {@link Reader} instance and converts the read stream
 	 * into an {@link EObject}.
 	 * 
 	 * @param reader
 	 *            the {@link Reader} capable of reading in a serialized EObject
 	 * @return the deserialized {@link EObject}
 	 * @throws SerializationException
 	 *             if deserialization fails
 	 */
 	public static EObject stringToEObject(Reader reader) throws SerializationException {
 
 		XMIResource res = (XMIResource) (new ResourceSetImpl()).createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 		URIConverter.ReadableInputStream ris = new URIConverter.ReadableInputStream(reader, "UTF-8");
 
 		try {
 			res.load(ris, getResourceLoadOptions());
 		} catch (UnsupportedEncodingException e) {
 			throw new SerializationException(e);
 		} catch (IOException e) {
 			throw new SerializationException(e);
 		}
 
 		return handleParsedEObject(res);
 	}
 
 	private static EObject handleParsedEObject(XMIResource res) throws SerializationException {
 		EObject result = res.getContents().get(0);
 
 		if (result instanceof IdEObjectCollection) {
 			IdEObjectCollection collection = (IdEObjectCollection) result;
 			Map<EObject, String> eObjectToIdMap = new HashMap<EObject, String>();
 			Map<String, EObject> idToEObjectMap = new HashMap<String, EObject>();
 
 			for (EObject modelElement : collection.getAllModelElements()) {
 				String modelElementId;
 				if (ModelUtil.isIgnoredDatatype(modelElement)) {
 					// create random ID for generic types, won't get serialized
 					// anyway
 					modelElementId = ModelFactory.eINSTANCE.createModelElementId().getId();
 				} else {
 					modelElementId = res.getID(modelElement);
 				}
 
 				if (modelElementId == null) {
 					throw new SerializationException("Failed to retrieve ID for EObject contained in project: "
 						+ modelElement);
 				}
 
 				eObjectToIdMap.put(modelElement, modelElementId);
 				idToEObjectMap.put(modelElementId, modelElement);
 			}
 
 			collection.initCaches(eObjectToIdMap, idToEObjectMap);
 		}
 
 		EcoreUtil.resolveAll(result);
 		res.getContents().remove(result);
 
 		return result;
 	}
 
 	/**
 	 * Delivers a map of options for loading resources. Especially {@link XMLResource#OPTION_DEFER_IDREF_RESOLUTION}
 	 * which speeds up loading
 	 * due to our id based resources.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	@SuppressWarnings("rawtypes")
 	public synchronized static Map<Object, Object> getResourceLoadOptions() {
 		if (resourceLoadOptions == null) {
 			resourceLoadOptions = new HashMap<Object, Object>();
 			resourceLoadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
 			resourceLoadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
 			resourceLoadOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap());
 			resourceLoadOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 		}
 		return resourceLoadOptions;
 	}
 
 	/**
 	 * Delivers a map of mandatory options for saving resources.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	public synchronized static Map<Object, Object> getResourceSaveOptions() {
 		if (resourceSaveOptions == null) {
 			resourceSaveOptions = new HashMap<Object, Object>();
 			resourceSaveOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 			resourceSaveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
 		}
 		return resourceSaveOptions;
 	}
 
 	private static boolean canHaveInstances(EClass eClass) {
 		return !(eClass.isAbstract() || eClass.isInterface());
 	}
 
 	/**
 	 * Recursively goes through model and create a list of all non-Abstract
 	 * classes.
 	 * 
 	 * @param ePackage
 	 *            the package to start with.
 	 * @return list of all non-Abstract model element classes in starting
 	 *         package and its sub-packages
 	 */
 	public static Set<EClass> getNonAbstractMETypes(EPackage ePackage) {
 
 		Set<EClass> nonAbstractMETypes = new HashSet<EClass>();
 		Set<EClass> allMETypes = getAllMETypes(ePackage);
 
 		Iterator<EClass> iterator = allMETypes.iterator();
 		while (iterator.hasNext()) {
 			EClass eClass = iterator.next();
 			if (canHaveInstances(eClass)) {
 				nonAbstractMETypes.add(eClass);
 			}
 		}
 
 		return nonAbstractMETypes;
 
 	}
 
 	/**
 	 * Recursively goes through package and returns a list of all EClasses
 	 * inheriting ModelElement (abstract classes and interfaces are also
 	 * include).
 	 * 
 	 * @param ePackage
 	 *            starting package
 	 * @return a list of all EClasses inheriting ModelElement (inclusive
 	 *         abstract classes and interfaces) in starting package and all its
 	 *         sub-packages.
 	 */
 	public static Set<EClass> getAllMETypes(EPackage ePackage) {
 		Set<EClass> meTypes = new HashSet<EClass>();
 
 		for (EObject eObject : ePackage.eContents()) {
 			if (eObject instanceof EClass) {
 				EClass eClass = (EClass) eObject;
 				meTypes.add(eClass);
 			} else if (eObject instanceof EPackage) {
 				EPackage eSubPackage = (EPackage) eObject;
 				meTypes.addAll(getAllMETypes(eSubPackage));
 			}
 		}
 
 		return meTypes;
 	}
 
 	/**
 	 * This will add a new entry to error log view of eclipse.
 	 * 
 	 * @param message
 	 *            message
 	 * @param exception
 	 *            exception
 	 * @param statusInt
 	 *            severity. Use one of constants in
 	 *            org.eclipse.core.runtime.Status class.
 	 * @throws LoggedException
 	 */
 	public static void log(String message, Throwable exception, int statusInt) {
 		Status status = new Status(statusInt, Platform.getBundle(ORG_ECLIPSE_EMF_EMFSTORE_COMMON_MODEL)
 			.getSymbolicName(), statusInt, message, exception);
 		Platform.getLog(Platform.getBundle(ORG_ECLIPSE_EMF_EMFSTORE_COMMON_MODEL)).log(status);
 	}
 
 	/**
 	 * Log an exception to the platform log. This will create a popup in the ui.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param exception
 	 *            the exception
 	 */
 	public static void logException(String message, Throwable exception) {
 		log(message, exception, IStatus.ERROR);
 	}
 
 	/**
 	 * Log an exception to the platform log. This will create a popup in the ui.
 	 * 
 	 * @param exception
 	 *            the exception
 	 */
 	public static void logException(Throwable exception) {
 		logException(exception.getMessage(), exception);
 	}
 
 	/**
 	 * Log a warning to the platform log. This will NOT create a popup in the
 	 * ui.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param exception
 	 *            the exception
 	 */
 	public static void logWarning(String message, Throwable exception) {
 		log(message, exception, IStatus.WARNING);
 	}
 
 	/**
 	 * Log a warning to the platform log. This will NOT create a popup in the
 	 * ui.
 	 * 
 	 * @param message
 	 *            the message
 	 */
 	public static void logWarning(String message) {
 		log(message, null, IStatus.WARNING);
 	}
 
 	/**
 	 * Log an exception to the platform log. This will create a popup in the ui.
 	 * 
 	 * @param message
 	 *            the message
 	 */
 	public static void logInfo(String message) {
 		log(message, null, IStatus.INFO);
 	}
 
 	/**
 	 * Clone any EObject.
 	 * 
 	 * @param <T>
 	 *            the Eobject sub type
 	 * @param eObject
 	 *            the Eobject instance
 	 * @return a clone of the Eobject instance
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> T clone(T eObject) {
 		if (eObject instanceof ProjectImpl) {
 			return (T) ((ProjectImpl) eObject).copy();
 		}
 		EObject clone = EcoreUtil.copy(eObject);
 		return (T) clone;
 	}
 
 	/**
 	 * Clone a list of EObjects.
 	 * 
 	 * @param <T>
 	 *            the EObject sub type the list consists of
 	 * @param list
 	 *            the list instance
 	 * @return a clone of the list and its contents instance
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> List<T> clone(List<T> list) {
 		ArrayList<T> result = new ArrayList<T>();
 		for (EObject eObject : list) {
 			T clone = (T) ModelUtil.clone(eObject);
 			result.add(clone);
 		}
 		return result;
 	}
 
 	/**
 	 * Create a flat clone of the list, the list if cloned but ot its content.
 	 * 
 	 * @param <T>
 	 *            the list type parameter
 	 * @param originalList
 	 *            the original list
 	 * @return a flat copy
 	 */
 	public static <T extends EObject> List<T> flatCloneList(List<T> originalList) {
 		List<T> clonedList = new ArrayList<T>(originalList.size());
 		for (T element : originalList) {
 			clonedList.add(element);
 		}
 		return clonedList;
 	}
 
 	/**
 	 * Load an EObject from a resource, the resource is supposed to contain only
 	 * one root object of the given EClass type. Type T must match EClass type.
 	 * 
 	 * @param <T>
 	 *            Type of the EObject
 	 * @param eClass
 	 *            the EClass of the EObject
 	 * @param resourceURI
 	 *            the resources URI
 	 * @param checkConstraints
 	 *            whether to perform additional sanity checks. These checks
 	 *            basically try to enforce that a resource contains exactly one
 	 *            object.
 	 * @return the object loaded from the resource
 	 * @throws IOException
 	 *             if loading the object from the resource fails.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> T loadEObjectFromResource(EClass eClass, URI resourceURI, boolean checkConstraints)
 		throws IOException {
 		ResourceSet resourceSet = new ResourceSetImpl();
 		Resource resource;
 
 		if (checkConstraints) {
 			resource = resourceSet.getResource(resourceURI, false);
 		} else {
 			resource = resourceSet.getResource(resourceURI, true);
 		}
 
 		resource.load(getResourceLoadOptions());
 		EList<EObject> contents = resource.getContents();
 
 		if (checkConstraints) {
 			if (contents.size() > 1) {
 				throw new IOException("Resource containes multiple objects!");
 			}
 		}
 
 		if (contents.size() < 1) {
 			throw new IOException("Resource contains no objects");
 		}
 
 		EObject eObject = contents.get(0);
 
 		if (eObject instanceof Project && resource instanceof XMIResource) {
 			XMIResource xmiResource = (XMIResource) resource;
 			Project project = (Project) eObject;
 			Map<EObject, String> eObjectToIdMap = new HashMap<EObject, String>();
 			Map<String, EObject> idToEObjectMap = new HashMap<String, EObject>();
 
 			TreeIterator<EObject> it = project.eAllContents();
 			while (it.hasNext()) {
 				EObject obj = it.next();
 				String id = xmiResource.getID(obj);
 				if (id != null) {
 					eObjectToIdMap.put(obj, id);
 					idToEObjectMap.put(id, obj);
 				}
 			}
 
 			project.initCaches(eObjectToIdMap, idToEObjectMap);
 		}
 
 		if (!(eClass.isInstance(eObject))) {
 			throw new IOException("Resource contains no objects of given class");
 		}
 
 		return (T) eObject;
 	}
 
 	/**
 	 * Save a list of EObjects to the resource with the given URI.
 	 * 
 	 * @param eObjects
 	 *            the EObjects to be saved
 	 * @param resourceURI
 	 *            the URI of the resource, which should be used to save the
 	 *            EObjects
 	 * @throws IOException
 	 *             if saving to the resource fails
 	 */
 	public static void saveEObjectToResource(List<? extends EObject> eObjects, URI resourceURI) throws IOException {
 		ResourceSet resourceSet = new ResourceSetImpl();
 		Resource resource = resourceSet.createResource(resourceURI);
 		EList<EObject> contents = resource.getContents();
 
 		for (EObject eObject : eObjects) {
 			contents.add(eObject);
 			if (eObject instanceof Project && resource instanceof XMIResource) {
 				setXmiIdsOnResource((Project) eObject, (XMIResource) resource);
 			}
 		}
 
 		contents.addAll(eObjects);
 		resource.save(null);
 	}
 
 	/**
 	 * Set all IDs contained in the project as XMI IDs for the model elements in
 	 * the project.
 	 * 
 	 * @param project
 	 *            a project
 	 * @param xmiResource
 	 *            the resource that will contain the XMI IDs
 	 */
 	public static void setXmiIdsOnResource(Project project, XMIResource xmiResource) {
 		for (EObject modelElement : project.getAllModelElements()) {
 			ModelElementId modelElementId = project.getModelElementId(modelElement);
 			xmiResource.setID(modelElement, modelElementId.getId());
 		}
 	}
 
 	/**
 	 * Save an EObject to a resource.
 	 * 
 	 * @param eObject
 	 *            the object
 	 * @param resourceURI
 	 *            the resources URI
 	 * @throws IOException
 	 *             if saving to the resource fails.
 	 */
 	public static void saveEObjectToResource(EObject eObject, URI resourceURI) throws IOException {
 		ArrayList<EObject> list = new ArrayList<EObject>();
 		list.add(eObject);
 		saveEObjectToResource(list, resourceURI);
 	}
 
 	/**
 	 * Deletes all resources from resourceSet, which string representation of URI starts with prefix.
 	 * 
 	 * @param resourceSet resource set
 	 * @param prefix string prefix of the resource path
 	 * @throws IOException
 	 */
 	public static void deleteResourcesWithPrefix(ResourceSet resourceSet, String prefix) throws IOException {
 		List<Resource> toDelete = new ArrayList<Resource>();
 		for (Resource resource : resourceSet.getResources()) {
 			if (resource.getURI().toFileString().startsWith(prefix)) {
 				toDelete.add(resource);
 			}
 		}
 		for (Resource resource : toDelete) {
 			resource.delete(null);
 		}
 	}
 
 	/**
	 * Deletes all resources from resourceSet, which string representation of URI starts with prefix.
	 * 
	 * @param resourceSet resource set
	 * @param prefix string prefix of the resource path
	 * @throws IOException
	 */
	public static void deleteResourcesWithPrefix(ResourceSet resourceSet, String prefix) throws IOException {
		List<Resource> toDelete = new ArrayList<Resource>();
		for (Resource resource : resourceSet.getResources()) {
			if (resource.getURI().toFileString().startsWith(prefix)) {
				toDelete.add(resource);
			}
		}
		for (Resource resource : toDelete) {
			resource.delete(null);
		}
	}

	/**
 	 * Retrieve the current model version number.
 	 * 
 	 * @return an integer identifing the current model version
 	 * @throws MalformedModelVersionException
 	 *             if there is no well formed or defined model version
 	 */
 	public static int getModelVersionNumber() throws MalformedModelVersionException {
 		ExtensionPoint extensionPoint = new ExtensionPoint("org.eclipse.emf.emfstore.common.model.modelversion", true);
 		if (extensionPoint.size() != 1) {
 			String message = "There is " + extensionPoint.size()
 				+ " Model Version(s) registered for the given model. Migrator will assume model version 0.";
 			logInfo(message);
 			return 0;
 		}
 		try {
 			return extensionPoint.getFirst().getInteger("versionIdentifier");
 		} catch (ExtensionPointException e) {
 			throw new MalformedModelVersionException("Version identifier was malformed, it must be an integer.", e);
 		}
 	}
 
 	/**
 	 * Get Project that contains a model element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @return the project or null if the element is not contained in a project.
 	 */
 	public static Project getProject(EObject modelElement) {
 		Set<EObject> seenModelElements = new HashSet<EObject>();
 		seenModelElements.add(modelElement);
 		return (Project) getParent(Project.class, modelElement, seenModelElements);
 	}
 
 	/**
 	 * Get the EContainer that contains the given model element and whose
 	 * EContainer is null.
 	 * 
 	 * @param parent
 	 *            the Class of the parent
 	 * @param child
 	 *            the model element whose container should get returned
 	 * @return the container
 	 */
 	public static EObject getParent(Class<? extends EObject> parent, EObject child) {
 		Set<EObject> seenModelElements = new HashSet<EObject>();
 		seenModelElements.add(child);
 		return getParent(parent, child, seenModelElements);
 	}
 
 	private static EObject getParent(Class<? extends EObject> parent, EObject child, Set<EObject> seenModelElements) {
 		if (child == null) {
 			return null;
 		}
 
 		if (seenModelElements.contains(child.eContainer())) {
 			throw new IllegalStateException("ModelElement is in a containment cycle");
 		}
 
 		if (parent.isInstance(child)) {
 			return child;
 		} else {
 			seenModelElements.add(child);
 			return getParent(parent, child.eContainer(), seenModelElements);
 		}
 	}
 
 	/**
 	 * Whether a {@link EClass} is a association class. Association classes are
 	 * not displayed as dedicated elements. A link from one element to another
 	 * which goes over an association class is displayed by a dedicated widget.
 	 * This widgets allows to trace transparently without seeing the association
 	 * class.
 	 * 
 	 * @param eClazz
 	 *            the {@link EClass}
 	 * @return if it is an association
 	 */
 	public static boolean isAssociationClassElement(EClass eClazz) {
 		if (eClazz == null || eClazz.isAbstract() || eClazz.getInstanceClass() == null) {
 			return false;
 		}
 		return AssociationClassElement.class.isAssignableFrom(eClazz.getInstanceClass());
 	}
 
 	/**
 	 * Get all contained elements of a given element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @return a set of contained model elements
 	 */
 	public static Set<EObject> getAllContainedModelElements(EObject modelElement, boolean includeTransientContainments) {
 		return getAllContainedModelElements(modelElement, includeTransientContainments, false);
 	}
 
 	/**
 	 * Get all contained elements of a given element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @param ignoreSingletonDatatypes
 	 *            whether to ignore singleton datatypes like, for example,
 	 *            EString
 	 * @return a set of contained model elements
 	 */
 	public static Set<EObject> getAllContainedModelElements(EObject modelElement, boolean includeTransientContainments,
 		boolean ignoreSingletonDatatypes) {
 		return getAllContainedModelElements(Collections.singletonList(modelElement), includeTransientContainments,
 			ignoreSingletonDatatypes);
 	}
 
 	/**
 	 * Get all contained elements of a given resource.
 	 * 
 	 * @param resource
 	 *            the resource
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @param ignoreSingletonDatatypes
 	 *            whether to ignore singleton datatypes like, for example,
 	 *            EString
 	 * @return a set of contained model elements
 	 *         Get all
 	 */
 	public static Set<EObject> getAllContainedModelElements(Resource resource, boolean includeTransientContainments,
 		boolean ignoreSingletonDatatypes) {
 		return getAllContainedModelElements(resource.getContents(), includeTransientContainments,
 			ignoreSingletonDatatypes);
 	}
 
 	/**
 	 * Get all contained elements of a given collection of model elements.
 	 * 
 	 * @param modelElements
 	 *            a collection of elements
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @param ignoreSingletonDatatypes
 	 *            whether to ignore singleton datatypes like, for example,
 	 *            EString
 	 * @return a set of contained model elements
 	 */
 	public static Set<EObject> getAllContainedModelElements(Collection<EObject> modelElements,
 		boolean includeTransientContainments, boolean ignoreSingletonDatatypes) {
 
 		Set<EObject> result = new HashSet<EObject>();
 
 		for (EObject modelElement : modelElements) {
 			for (EObject containee : modelElement.eContents()) {
 
 				if (!ignoreSingletonDatatypes && isSingleton(containee)) {
 					continue;
 				}
 
 				if (!containee.eContainingFeature().isTransient() || includeTransientContainments) {
 					Set<EObject> elements = getAllContainedModelElements(containee, includeTransientContainments,
 						ignoreSingletonDatatypes);
 					result.add(containee);
 					result.addAll(elements);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get the container of an EObject.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @return the container
 	 */
 	public static EObject getContainerModelElement(EObject modelElement) {
 		EObject container = modelElement.eContainer();
 		if (container == null) {
 			return null;
 		}
 		if (EcoreFactory.eINSTANCE.getEcorePackage().getEObject().isInstance(container)) {
 			return container;
 		}
 		return null;
 	}
 
 	/**
 	 * Get all contained elements of a given element as a list.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @return a list of contained model elements
 	 */
 	public static List<EObject> getAllContainedModelElementsAsList(EObject modelElement,
 		boolean includeTransientContainments) {
 
 		TreeIterator<EObject> it = modelElement.eAllContents();
 
 		List<EObject> result = new ArrayList<EObject>();
 		while (it.hasNext()) {
 			EObject containee = it.next();
 			if (containee.eContainingFeature() != null && !containee.eContainingFeature().isTransient()
 				|| includeTransientContainments) {
 				result.add(containee);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Delete the given incoming cross references to the given model element from any
 	 * other model element in the given project.
 	 * 
 	 * @param inverseReferences a collection of inverse references
 	 * @param modelElement
 	 *            the model element
 	 */
 	public static void deleteIncomingCrossReferencesFromParent(Collection<Setting> inverseReferences,
 		EObject modelElement) {
 		for (Setting setting : inverseReferences) {
 			EStructuralFeature eStructuralFeature = setting.getEStructuralFeature();
 			EReference reference = (EReference) eStructuralFeature;
 
 			if (reference.isContainer() || reference.isContainment() || !reference.isChangeable()) {
 				continue;
 			}
 
 			EObject opposite = setting.getEObject();
 
 			if (eStructuralFeature.isMany()) {
 				((EList<?>) opposite.eGet(eStructuralFeature)).remove(modelElement);
 			} else {
 				opposite.eUnset(eStructuralFeature);
 			}
 		}
 	}
 
 	/**
 	 * Delete all outgoing cross references of the given model element to any element in the given collection.
 	 * 
 	 * @param collection the collection
 	 * @param modelElement
 	 *            the model element
 	 */
 	public static void deleteOutgoingCrossReferences(IdEObjectCollection collection, EObject modelElement) {
 		Set<EObject> allModelElements = new HashSet<EObject>();
 		allModelElements.add(modelElement);
 		allModelElements.addAll(ModelUtil.getAllContainedModelElements(modelElement, false));
 
 		List<SettingWithReferencedElement> crossReferences = collectOutgoingCrossReferences(collection,
 			allModelElements);
 		for (SettingWithReferencedElement settingWithReferencedElement : crossReferences) {
 			Setting setting = settingWithReferencedElement.getSetting();
 			if (!settingWithReferencedElement.getSetting().getEStructuralFeature().isMany()) {
 				setting.getEObject().eUnset(setting.getEStructuralFeature());
 			} else {
 				List<?> references = (List<?>) setting.getEObject().eGet(setting.getEStructuralFeature());
 				references.remove(settingWithReferencedElement.getReferencedElement());
 			}
 		}
 	}
 
 	/**
 	 * Retrieve all outgoing connections from the model elements to other elements in the collection.
 	 * 
 	 * @param collection the collection
 	 * @param modelElements the model elements
 	 * @return a List of references
 	 */
 	public static List<SettingWithReferencedElement> collectOutgoingCrossReferences(IdEObjectCollection collection,
 		Set<EObject> modelElements) {
 		// result object
 		List<SettingWithReferencedElement> settings = new ArrayList<SettingWithReferencedElement>();
 
 		for (EObject currentElement : modelElements) {
 
 			for (EReference reference : currentElement.eClass().getEAllReferences()) {
 				EClassifier eType = reference.getEType();
 				// sanity checks
 				if (reference.isContainer() || reference.isContainment() || !reference.isChangeable()
 					|| (!(eType instanceof EClass))) {
 					continue;
 				}
 
 				Setting setting = ((InternalEObject) currentElement).eSetting(reference);
 
 				// multi references
 				if (reference.isMany()) {
 					@SuppressWarnings("unchecked")
 					List<EObject> referencedElements = (List<EObject>) currentElement.eGet(reference);
 					for (EObject referencedElement : referencedElements) {
 						if (shouldBeCollected(collection, modelElements, referencedElement)) {
 							settings.add(new SettingWithReferencedElement(setting, referencedElement));
 						}
 					}
 				} else {
 					// single references
 
 					EObject referencedElement = (EObject) currentElement.eGet(reference);
 					if (shouldBeCollected(collection, modelElements, referencedElement)) {
 						settings.add(new SettingWithReferencedElement(setting, referencedElement));
 					}
 
 				}
 			}
 		}
 
 		return settings;
 	}
 
 	/**
 	 * Checks if the referenced elements is an element in the given collection which is not a singleton, not an ignored
 	 * data type and not already contained in the given set of elements.
 	 * 
 	 * @param collection the collection
 	 * @param allModelElements the set of model elements
 	 * @param referencedElement the referenced element
 	 * @return true, if the specified conditions are met.
 	 */
 	public static boolean shouldBeCollected(IdEObjectCollection collection, Set<EObject> allModelElements,
 		EObject referencedElement) {
 
 		if (referencedElement == null || !collection.containsInstance(referencedElement)) {
 			return false;
 		}
 
 		return !ModelUtil.isSingleton(referencedElement) && !ModelUtil.isIgnoredDatatype(referencedElement)
 			&& !allModelElements.contains(referencedElement);
 	}
 
 	/**
 	 * Get the singleton instance for a given model element id.
 	 * 
 	 * @param singletonId
 	 *            the id
 	 * @return the singleton instance
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.SingletonIdResolver#getSingleton(org.eclipse.emf.emfstore.common.model.ModelElementId)
 	 */
 	public static EObject getSingleton(ModelElementId singletonId) {
 
 		initSingletonIdResolvers();
 
 		for (SingletonIdResolver resolver : singletonIdResolvers) {
 			EObject singleton = resolver.getSingleton(singletonId);
 			if (singleton != null) {
 				return singleton;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the singleton id for a singleton instance.
 	 * 
 	 * @param singleton
 	 *            the singleton
 	 * @return the id
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.SingletonIdResolver#getSingletonModelElementId(org.eclipse.emf.ecore.EObject)
 	 */
 	public static ModelElementId getSingletonModelElementId(EObject singleton) {
 
 		initSingletonIdResolvers();
 
 		for (SingletonIdResolver resolver : singletonIdResolvers) {
 			ModelElementId id = resolver.getSingletonModelElementId(singleton);
 			if (id != null) {
 				return clone(id);
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Return whether the given eObject instance is a singelton.
 	 * 
 	 * @param eObject
 	 *            the instance
 	 * @return true if it is a singleton
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.SingletonIdResolver#isSingleton(org.eclipse.emf.ecore.EObject)
 	 */
 	public static boolean isSingleton(EObject eObject) {
 
 		initSingletonIdResolvers();
 
 		for (SingletonIdResolver resolver : singletonIdResolvers) {
 			if (resolver.isSingleton(eObject)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Initializes all available {@link SingletonIdResolver}.
 	 */
 	private synchronized static void initSingletonIdResolvers() {
 		if (singletonIdResolvers == null) {
 			// collect singleton ID resolvers
 			singletonIdResolvers = new HashSet<SingletonIdResolver>();
 
 			for (ExtensionElement element : new ExtensionPoint(
 				"org.eclipse.emf.emfstore.common.model.singletonidresolver").getExtensionElements()) {
 				try {
 					singletonIdResolvers.add(element.getClass("class", SingletonIdResolver.class));
 				} catch (ExtensionPointException e) {
 					ModelUtil.logWarning("Couldn't instantiate Singleton ID resolver:" + e.getMessage());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Copy an element including its ids from a project.
 	 * 
 	 * @param originalObject the source
 	 * @param copiedObject the target
 	 * @return a map from copied objects to ids.
 	 */
 	public static Map<EObject, ModelElementId> copyModelElement(EObject originalObject, EObject copiedObject) {
 
 		Map<EObject, ModelElementId> idMap = new HashMap<EObject, ModelElementId>();
 
 		Project project = getProject(originalObject);
 		if (project == null) {
 			throw new IllegalArgumentException("EObject is not contained in a project.");
 		}
 
 		List<EObject> allContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(originalObject, false);
 		allContainedModelElements.add(originalObject);
 		// EObject copiedElement = ModelUtil.clone(originalObject);
 		List<EObject> copiedAllContainedModelElements = ModelUtil.getAllContainedModelElementsAsList(copiedObject,
 			false);
 		copiedAllContainedModelElements.add(copiedObject);
 
 		for (int i = 0; i < allContainedModelElements.size(); i++) {
 			EObject child = allContainedModelElements.get(i);
 			EObject copiedChild = copiedAllContainedModelElements.get(i);
 			ModelElementId childId = ModelUtil.clone(project.getModelElementId(child));
 
 			if (ModelUtil.isIgnoredDatatype(child)) {
 				continue;
 			}
 
 			idMap.put(copiedChild, childId);
 		}
 
 		return idMap;
 	}
 }
