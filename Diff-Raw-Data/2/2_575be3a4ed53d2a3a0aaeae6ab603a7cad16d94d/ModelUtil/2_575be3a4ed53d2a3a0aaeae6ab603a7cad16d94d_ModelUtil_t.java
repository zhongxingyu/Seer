 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel
  * Edgar Mueller
  * Otto von Wesendonk
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.common.model.util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.ECollections;
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
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPointException;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESPriorityComparator;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESResourceSetProvider;
 import org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver;
 import org.eclipse.emf.emfstore.internal.common.model.AssociationClassElement;
 import org.eclipse.emf.emfstore.internal.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ESModelElementIdImpl;
 import org.eclipse.emf.emfstore.internal.common.model.impl.IdEObjectCollectionImpl;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ProjectImpl;
 
 /**
  * Utility class for ModelElements.
  * 
  * @author koegel
  * @author emueller
  * @author ottovonwesen
  */
 public final class ModelUtil {
 
 	/**
 	 * Constant that may be used in case no checksum computation has taken place.
 	 */
 	public static final long NO_CHECKSUM = -1;
 
 	/**
 	 * URI used to serialize EObject with the model util.
 	 */
 	public static final URI VIRTUAL_URI = URI.createURI("virtualUri");
 
 	private static final String ORG_ECLIPSE_EMF_EMFSTORE_COMMON_MODEL = "org.eclipse.emf.emfstore.common.model";
 
 	public static final String DISCARD_DANGLING_HREF_ID = "org.eclipse.emf.emfstore.common.discardDanglingHREFs";
 
 	private static IResourceLogger resourceLogger = new IResourceLogger() {
 
 		public void logWarning(String msg) {
 			ModelUtil.logWarning(msg);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.emfstore.internal.common.model.util.IResourceLogger#logError(java.lang.String)
 		 */
 		public void logError(String msg) {
 			ModelUtil.logError(msg);
 		}
 	};
 
 	/**
 	 * Contains the canonical names of classes which will be ignored.
 	 */
 	private static Set<String> ignoredDataTypes;
 
 	/**
 	 * Contains all ID resolvers for singleton datatypes.
 	 */
 	private static Set<ESSingletonIdResolver> singletonIdResolvers;
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
 	 * Compares two {@link IdEObjectCollection}s. Order of the model elements at root level
 	 * of a collection does not influence the outcome of this operations
 	 * 
 	 * @param collectionA
 	 *            the first collection
 	 * @param collectionB
 	 *            the second collection
 	 * @return true if the two collections are equal, false otherwise
 	 */
 	public static boolean areEqual(IdEObjectCollection collectionA, IdEObjectCollection collectionB) {
 		long a;
 		long b;
 
 		try {
 			// collections are treated specially because the order
 			// of root elements should not matter
 			a = computeChecksum(collectionA);
 			b = computeChecksum(collectionB);
 		} catch (SerializationException e) {
 			return false;
 		}
 
 		return a == b;
 	}
 
 	/**
 	 * Copies the given EObject and converts it to a string.
 	 * 
 	 * @param object
 	 *            the eObject
 	 * @return the string representation of the EObject
 	 * @throws SerializationException
 	 *             if a serialization problem occurs
 	 */
 	public static String eObjectToString(EObject object) throws SerializationException {
 
 		if (object == null) {
 			return null;
 		}
 
 		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		XMIResource res = (XMIResource) resourceSetImpl.createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 
 		EObject copy;
 		if (object instanceof IdEObjectCollection) {
 			copy = copyIdEObjectCollection((IdEObjectCollection) object, res);
 		} else {
 			copy = copyEObject(ModelUtil.getProject(object), object, res);
 		}
 
 		return copiedEObjectToString(copy, res);
 	}
 
 	/**
 	 * Converts the given {@link EObject} to a string.
 	 * 
 	 * @param copy The copied {@link EObject}.
 	 * @param resource The resource for the {@link EObject}.
 	 * @return The string representing the {@link EObject}.
 	 * @throws SerializationException If a serialization problem occurs.
 	 */
 	private static String copiedEObjectToString(EObject copy, XMIResource resource) throws SerializationException {
 		int step = 200;
 		int initialSize = step;
 		resource.getContents().add(copy);
 
 		StringWriter stringWriter = new StringWriter(initialSize);
 		URIConverter.WriteableOutputStream uws = new URIConverter.WriteableOutputStream(stringWriter, "UTF-8");
 
 		String lineSeparator = System.getProperty("line.separator");
 		try {
 			System.setProperty("line.separator", "\r\n");
 			resource.save(uws, getResourceSaveOptions());
 		} catch (IOException e) {
 			throw new SerializationException(e);
 		} finally {
 			System.setProperty("line.separator", lineSeparator);
 			try {
 				stringWriter.close();
 				uws.close();
 			} catch (IOException e) {
 				throw new SerializationException(e);
 			}
 		}
 
 		return stringWriter.toString();
 	}
 
 	/**
 	 * Computes the checksum for a given string representing an {@link EObject}.
 	 * 
 	 * @param eObjectString
 	 *            the string representing the {@link EObject}.
 	 * @return the computed checksum
 	 * 
 	 * @throws SerializationException
 	 *             in case any errors occur during computation of the checksum
 	 */
 	private static long computeChecksum(String eObjectString) {
 		long h = 1125899906842597L; // prime
 		int len = eObjectString.length();
 
 		for (int i = 0; i < len; i++) {
 			h = 31 * h + eObjectString.charAt(i);
 		}
 
 		return h;
 	}
 
 	/**
 	 * Computes the checksum for a given {@link EObject}.
 	 * 
 	 * @param eObject
 	 *            the EObject for which to compute a checksum
 	 * @return the computed checksum
 	 * 
 	 * @throws SerializationException
 	 *             in case any errors occur during computation of the checksum
 	 */
 	public static long computeChecksum(EObject eObject) throws SerializationException {
 		return (computeChecksum(eObjectToString(eObject)));
 	}
 
 	/**
 	 * Computes the checksum for a given {@link IdEObjectCollection}.
 	 * The checksum for a collection is independent of the order of the
 	 * collection's elements at the root level.
 	 * 
 	 * @param collection
 	 *            the collection for which to compute a checksum
 	 * @return the computed checksum
 	 * 
 	 * @throws SerializationException
 	 *             in case any errors occur during computation of the checksum
 	 */
 	public static long computeChecksum(IdEObjectCollection collection) throws SerializationException {
 
 		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		XMIResource res = (XMIResource) resourceSetImpl.createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 		final IdEObjectCollection copy = copyIdEObjectCollection(collection, res);
 
 		ECollections.sort(copy.getModelElements(), new Comparator<EObject>() {
 			public int compare(EObject o1, EObject o2) {
 				return copy.getModelElementId(o1).getId().compareTo(copy.getModelElementId(o2).getId());
 			}
 		});
 
 		return computeChecksum(copiedEObjectToString(copy, res));
 	}
 
 	/**
 	 * Returns the resource logger.
 	 * 
 	 * @return the resource logger
 	 */
 	public static IResourceLogger getResourceLogger() {
 		return resourceLogger;
 	}
 
 	/**
 	 * Copies the given {@link IdEObjectCollection} and writes the IDs it contains into the given {@link XMIResource}.
 	 * 
 	 * @param collection
 	 *            the collection to be copied
 	 * @param res
 	 *            the resource into which the collection's IDs should be written into
 	 * @return the copied collection
 	 */
 	public static IdEObjectCollection copyIdEObjectCollection(IdEObjectCollection collection, XMIResource res) {
 		IdEObjectCollection copiedCollection = clone(collection);
 
 		for (EObject modelElement : copiedCollection.getAllModelElements()) {
 			if (isIgnoredDatatype(modelElement)) {
 				continue;
 			}
 			ModelElementId modelElementId = copiedCollection.getModelElementId(modelElement);
 			res.setID(modelElement, modelElementId.getId());
 		}
 
 		for (EObject modelElement : ((Project) copiedCollection).getCutElements()) {
 			if (isIgnoredDatatype(modelElement)) {
 				continue;
 			}
 			ModelElementId modelElementId = ((IdEObjectCollectionImpl) copiedCollection)
 				.getModelElementId(modelElement);
 			res.setID(modelElement, modelElementId.getId());
 		}
 
 		return copiedCollection;
 	}
 
 	// TODO: javadoc
 	private static EObject copyEObject(IdEObjectCollection collection, EObject object, XMIResource res) {
 		IdEObjectCollection copiedCollection = copyIdEObjectCollection(collection, res);
 		EObject copiedEObject = copiedCollection.getModelElement(collection.getModelElementId(object));
 		return copiedEObject;
 	}
 
 	/**
 	 * Determines whether the type of an EObject is an ignored one.
 	 * 
 	 * @param eObject
 	 *            the EObject which is to be checked
 	 * @return true, if the EObject will be ignored, false otherwise
 	 */
 	public static synchronized boolean isIgnoredDatatype(EObject eObject) {
 
 		if (ignoredDataTypes == null) {
 			ignoredDataTypes = new LinkedHashSet<String>();
 			for (ESExtensionElement element : new ESExtensionPoint(
 				"org.eclipse.emf.emfstore.common.model.ignoreDatatype",
 				true).getExtensionElements()) {
 				try {
 					ignoredDataTypes.add(element.getAttribute("type"));
 				} catch (ESExtensionPointException e) {
 				}
 			}
 		}
 
 		return ignoredDataTypes.contains(eObject.eClass().getInstanceClassName());
 	}
 
 	/**
 	 * Delivers a map of options for loading resources. Especially {@link XMLResource#OPTION_DEFER_IDREF_RESOLUTION}
 	 * which speeds up loading
 	 * due to our id based resources.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	@SuppressWarnings("rawtypes")
 	public static synchronized Map<Object, Object> getResourceLoadOptions() {
 		if (resourceLoadOptions == null) {
 			resourceLoadOptions = new LinkedHashMap<Object, Object>();
 			resourceLoadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
 			resourceLoadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
 			resourceLoadOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap());
 			resourceLoadOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
 		}
 		return resourceLoadOptions;
 	}
 
 	/**
 	 * Delivers a map of mandatory options for saving resources.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	public static synchronized Map<Object, Object> getResourceSaveOptions() {
 
 		if (resourceSaveOptions == null) {
 
 			resourceSaveOptions = new LinkedHashMap<Object, Object>();
 			resourceSaveOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 			resourceSaveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
 			resourceSaveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
 			resourceSaveOptions.put(XMLResource.OPTION_FLUSH_THRESHOLD, 100000);
 			resourceSaveOptions.put(XMLResource.OPTION_USE_FILE_BUFFER, Boolean.TRUE);
 
 			ESExtensionPoint extensionPoint = new ESExtensionPoint(DISCARD_DANGLING_HREF_ID);
 			Boolean discardDanglingHREFs = extensionPoint.getBoolean("value", Boolean.FALSE);
 			if (discardDanglingHREFs) {
 				resourceSaveOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF,
 					XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);
 			}
 
 			logInfo("Resource save options initialized:");
 			for (Map.Entry<Object, Object> entry : resourceSaveOptions.entrySet()) {
 				logInfo("\t" + entry.getKey() + ": " + entry.getValue());
 			}
 		}
 		return resourceSaveOptions;
 	}
 
 	/**
 	 * Saves a given resource and logs any warning and/or errors.
 	 * 
 	 * @param resource
 	 *            the resource to be saved
 	 * @param logger
 	 *            a logger instance which will be used to log warnings and errors on resources
 	 * @throws IOException
 	 *             in case an exception occurs during save
 	 */
 	public static void saveResource(Resource resource, IResourceLogger logger) throws IOException {
 		try {
 			resource.save(ModelUtil.getResourceSaveOptions());
 		} catch (IOException e) {
 			// rethrow exception
 			throw e;
 		} finally {
 			logWarningsAndErrors(resource, logger);
 		}
 	}
 
 	/**
 	 * Loads a given resource and logs any warning and/or errors.
 	 * 
 	 * @param resource
 	 *            the resource to be loaded
 	 * @param logger
 	 *            a logger instance which will be used to log warnings and errors on resources
 	 * @throws IOException
 	 *             in case an exception occurs during load
 	 */
 	public static void loadResource(Resource resource, IResourceLogger logger) throws IOException {
 		try {
 			resource.load(ModelUtil.getResourceLoadOptions());
 		} catch (IOException e) {
 			// rethrow exception
 			throw e;
 		} finally {
 			logWarningsAndErrors(resource, logger);
 		}
 	}
 
 	private static void logWarningsAndErrors(Resource resource, IResourceLogger logger) {
 
 		if (resource.getWarnings().size() > 0) {
 			for (Diagnostic diagnostic : resource.getErrors()) {
 				logger.logWarning(logDiagnostic(diagnostic).toString());
 			}
 		}
 
 		if (resource.getErrors().size() > 0) {
 			for (Diagnostic diagnostic : resource.getErrors()) {
 				logger.logError(logDiagnostic(diagnostic).toString());
 			}
 		}
 
 	}
 
 	private static StringWriter logDiagnostic(Diagnostic diagnostic) {
 
 		StringWriter error = new StringWriter();
 		error.append(diagnostic.getLocation() + "\n");
 		error.append(diagnostic.getMessage() + "\n");
 
 		if (diagnostic instanceof Exception) {
 			StringWriter stringWriter = new StringWriter();
 			PrintWriter printWriter = new PrintWriter(stringWriter);
 			((Throwable) diagnostic).printStackTrace(printWriter);
 			error.append(stringWriter.toString() + "\n");
 		}
 
 		return error;
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
 
 		Set<EClass> nonAbstractMETypes = new LinkedHashSet<EClass>();
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
 		Set<EClass> meTypes = new LinkedHashSet<EClass>();
 
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
 	 * Log a warning to the platform log. This will NOT create a popup in the UI.
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
 	 * Log a warning to the platform log. This will NOT create a popup in the UI.
 	 * 
 	 * @param message
 	 *            the message being logged
 	 */
 	public static void logWarning(String message) {
 		log(message, null, IStatus.WARNING);
 	}
 
 	/**
 	 * Log a error to the platform log. This will NOT create a popup in the UI.
 	 * 
 	 * @param message
 	 *            the message being logged
 	 */
 	public static void logError(String message) {
 		log(message, null, IStatus.ERROR);
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
 
 		ResourceSet resourceSet = null;
 
 		if (resourceURI != null && resourceURI.scheme().equals("emfstore")) {
 			ESExtensionPoint extensionPoint = null;
			if (resourceURI.authority().equals("workspaces")) {
 				extensionPoint = new ESExtensionPoint("org.eclipse.emf.emfstore.client.resourceSetProvider",
 					false);
 			} else {
 				extensionPoint = new ESExtensionPoint("org.eclipse.emf.emfstore.server.resourceSetProvider",
 					false);
 			}
 
 			extensionPoint.setComparator(new ESPriorityComparator("priority", true));
 			extensionPoint.reload();
 
 			ESResourceSetProvider resourceSetProvider = extensionPoint.getElementWithHighestPriority().getClass(
 				"class",
 				ESResourceSetProvider.class);
 
 			if (resourceSetProvider == null) {
 				resourceSet = new ResourceSetImpl();
 			} else {
 				resourceSet = resourceSetProvider.getResourceSet();
 			}
 
 		} else {
 			resourceSet = new ResourceSetImpl();
 		}
 
 		Resource resource;
 
 		if (checkConstraints) {
 			resource = resourceSet.getResource(resourceURI, false);
 		} else {
 			resource = resourceSet.getResource(resourceURI, true);
 		}
 
 		loadResource(resource, resourceLogger);
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
 			Map<EObject, String> eObjectToIdMap = new LinkedHashMap<EObject, String>();
 			Map<String, EObject> idToEObjectMap = new LinkedHashMap<String, EObject>();
 
 			TreeIterator<EObject> it = project.eAllContents();
 			while (it.hasNext()) {
 				EObject obj = it.next();
 				String id = xmiResource.getID(obj);
 				if (id != null) {
 					eObjectToIdMap.put(obj, id);
 					idToEObjectMap.put(id, obj);
 				}
 			}
 
 			project.initMapping(eObjectToIdMap, idToEObjectMap);
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
 	 * @param options The save options for the resource.
 	 * @throws IOException
 	 *             if saving to the resource fails
 	 */
 	public static void saveEObjectToResource(List<? extends EObject> eObjects, URI resourceURI,
 		Map<Object, Object> options) throws IOException {
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
 		resource.save(options);
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
 		saveEObjectToResource(eObjects, resourceURI, null);
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
 	 * @throws IOException if deleting the resource fails
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
 	 * Get Project that contains a model element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @return the project or null if the element is not contained in a project.
 	 */
 	public static Project getProject(EObject modelElement) {
 		Set<EObject> seenModelElements = new LinkedHashSet<EObject>();
 		seenModelElements.add(modelElement);
 		return getParent(Project.class, modelElement, seenModelElements);
 	}
 
 	/**
 	 * Searches for the project and then looks for the modelelement id.
 	 * 
 	 * @param modelElement me
 	 * @return id
 	 */
 	public static ModelElementId getModelElementId(EObject modelElement) {
 		Project project = getProject(modelElement);
 		if (project == null) {
 			return null;
 		}
 		return project.getModelElementId(modelElement);
 	}
 
 	/**
 	 * Get the EContainer that contains the given model element and whose
 	 * EContainer is null.
 	 * 
 	 * @param <T>
 	 * 
 	 * @param parent
 	 *            the Class of the parent
 	 * @param child
 	 *            the model element whose container should get returned
 	 * @param <T> type of the parent class
 	 * @return the container
 	 */
 	public static <T extends EObject> T getParent(Class<T> parent, EObject child) {
 		Set<EObject> seenModelElements = new LinkedHashSet<EObject>();
 		seenModelElements.add(child);
 		return getParent(parent, child, seenModelElements);
 	}
 
 	@SuppressWarnings("unchecked")
 	private static <T extends EObject> T getParent(Class<T> parent, EObject child, Set<EObject> seenModelElements) {
 		if (child == null) {
 			return null;
 		}
 
 		if (seenModelElements.contains(child.eContainer())) {
 			throw new IllegalStateException("ModelElement is in a containment cycle");
 		}
 
 		if (parent.isInstance(child)) {
 			return (T) child;
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
 
 		Set<EObject> result = new LinkedHashSet<EObject>();
 
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
 				if (opposite instanceof Map.Entry<?, ?> && eStructuralFeature.getName().equals("key")) {
 					logWarning("Incoming cross reference for model element " + modelElement + " is a map key.");
 				}
 
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
 		Set<EObject> allModelElements = new LinkedHashSet<EObject>();
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
 
 		if (referencedElement == null) {
 			return false;
 		}
 		if ((!collection.contains(referencedElement))
 			&& ((ProjectImpl) collection).getDeletedModelElementId(referencedElement) == null) {
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
 	 * @see org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver#getSingleton(org.eclipse.emf.emfstore.internal.common.model.ModelElementId)
 	 */
 	public static EObject getSingleton(ModelElementId singletonId) {
 
 		initSingletonIdResolvers();
 
 		for (ESSingletonIdResolver resolver : singletonIdResolvers) {
 			EObject singleton = resolver.getSingleton(singletonId.toAPI());
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
 	 * @see org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver#getSingletonModelElementId(org.eclipse.emf.ecore.EObject)
 	 */
 	public static ModelElementId getSingletonModelElementId(EObject singleton) {
 
 		initSingletonIdResolvers();
 
 		for (ESSingletonIdResolver resolver : singletonIdResolvers) {
 			ESModelElementIdImpl id = (ESModelElementIdImpl) resolver.getSingletonModelElementId(singleton);
 			if (id != null) {
 				return clone(id.toInternalAPI());
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
 	 * @see org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver#isSingleton(org.eclipse.emf.ecore.EObject)
 	 */
 	public static boolean isSingleton(EObject eObject) {
 
 		initSingletonIdResolvers();
 
 		for (ESSingletonIdResolver resolver : singletonIdResolvers) {
 			if (resolver.isSingleton(eObject)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Initializes all available {@link ESSingletonIdResolver}.
 	 */
 	private static synchronized void initSingletonIdResolvers() {
 		if (singletonIdResolvers == null) {
 			// collect singleton ID resolvers
 			singletonIdResolvers = new LinkedHashSet<ESSingletonIdResolver>();
 
 			for (ESExtensionElement element : new ESExtensionPoint(
 				"org.eclipse.emf.emfstore.common.model.singletonIdResolver").getExtensionElements()) {
 				try {
 					singletonIdResolvers.add(element.getClass("class", ESSingletonIdResolver.class));
 				} catch (ESExtensionPointException e) {
 					ModelUtil.logWarning("Couldn't instantiate Singleton ID resolver:" + e.getMessage());
 				}
 			}
 		}
 	}
 }
