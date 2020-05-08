 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk, Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.model;
 
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <p>
  * A container for {@link EObject}s where each EObject has a unique {@link ESModelElementId} assigned to it.
  * </p>
  * <p>
  * The container does not specify how model elements are added or removed from the underlying collection of EObjects.
  * </p>
  * 
  * @author emueller
  * @author wesendon
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
  * 
  */
 public interface ESObjectContainer<T> {
 
 	/**
 	 * Returns the model element with the given {@link ESModelElementId}.
 	 * 
 	 * @param modelElementId
 	 *            the ID of the model element, that should be retrieved
 	 * @return the model element that has the given ID assigned to it within the container
 	 */
 	EObject getModelElement(T modelElementId);
 
 	/**
 	 * Retrieve the {@link ESModelElementId} of the given model element.
 	 * 
 	 * @param modelElement
 	 *            the model element for which to retrieve the ID for
 	 * @return the {@link ESModelElementId} of the given model element
 	 */
 	T getModelElementId(EObject modelElement);
 
 	/**
 	 * Returns all directly contained model element of the container, i.e. a
 	 * hierarchical representation of the model elements.
 	 * 
 	 * @return a list containing the directly contained model elements within the container
 	 */
 	EList<EObject> getModelElements();
 
 	/**
 	 * <p>
 	 * Returns a flat representation of all model elements in the collection.
 	 * </p>
 	 * <p>
 	 * The returned set is not modifiable and will throw an {@link UnsupportedOperationException} in case trying to do
 	 * so.
 	 * </p>
 	 * 
 	 * @return a set of all model elements contained in the collection
 	 */
 	Set<EObject> getAllModelElements();
 
 	/**
 	 * Checks whether the {@link EObject} with the given {@link ESModelElementId} is contained in the collection.
 	 * 
 	 * @param modelElementId
 	 *            the model element ID of the EObject, which needs to be checked,
 	 *            whether it is contained in the collection
 	 * @return {@code true}, if the EObject with the given model element ID is
 	 *         contained in the collection, {@code false}
 	 */
 	boolean contains(T modelElementId);
 
 	/**
 	 * Checks whether a given {@link EObject} is contained in the collection.
 	 * 
 	 * @param modelElement
 	 *            the model element to be checked, whether it is contained in the
 	 *            collection
 	 * @return {@code true}, if the model element is contained in the collection, {@code false} otherwise
 	 */
 	boolean contains(EObject modelElement);
 
 	/**
 	 * Retrieve a list of all model elements of a certain type in the collection.
 	 * 
 	 * @param <T>
 	 *            the type of the model element(s) to be retrieved
 	 * @param modelElementClass
 	 *            the {@link Class} of the model element(s) to be retrieved
 	 * @param includeSubclasses
 	 *            whether to also include all subclasses of the given {@link Class} in the list
 	 * @return a list of model elements of the given type
 	 */
 	<U extends EObject> Set<U> getAllModelElementsByClass(Class<U> modelElementClass, Boolean includeSubclasses);
 
 	/**
 	 * Retrieve a list of all model elements of a certain type in the collection.
 	 * 
 	 * @param <T>
 	 *            the type of the model element(s) to be retrieved
 	 * @param modelElementClass
 	 *            the {@link Class} of the model element(s) to be retrieved
 	 * @return a list of model elements of the given type
 	 */
 	<U extends EObject> Set<U> getAllModelElementsByClass(Class<U> modelElementClass);
 }
