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
 package org.eclipse.emf.ecp.common.model;
 
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 
/**
 * Represents the met context of the EMF Client Platform describing involved EClasses and their properties.
 * @author Jonas Helming
 *
 */
 public interface ECPMetaModelElementContext {
 
 	/**
 	 * Returns all types of model elements in this context. Could exclude {@link AssociationClassElement}s.
 	 * 
 	 * @param association whether to include {@link AssociationClassElement}
 	 * @return a set of {@link EClass}es
 	 */
 	Set<EClass> getAllModelElementEClasses(boolean association);
 
 	/**
	 * Retrieve all EClasses from the EcorePackage registry that are subclasses of the given {@link EClass}.
 	 * Does not include abstract classes or interfaces. Could exclude {@link AssociationClassElement}s.
 	 * 
 	 * @param eClass the superClass of the subClasses to retrieve
 	 * @param association whether to include {@link AssociationClassElement}
 	 * @return a set of EClasses
 	 */
 	Set<EClass> getAllSubEClasses(EClass eClass, boolean association);
 
 	/**
 	 * Whether a {@link EClass} is a domain element. Non Domain Elements are not root nodes of the model and
 	 * do not exist on their own. They have the following characteristic behavior: they don't appear in the openME
 	 * dialog,
 	 * they are deleted if the last link to them is deleted, and they are not shown in the new element wizard and
 	 * the METyselection dialog (Table View).
 	 * 
	 * @param clazz the EClass
 	 * @return whether the given class is is a non domain element
 	 */
 	boolean isNonDomainElement(EClass clazz);
 
 	/**
 	 * Whether a {@link EObject} is a association class. Association classes are not displayed as dedicated elements. A
 	 * link from one element to another which goes over an association class is displayed by a dedicated widget. This
 	 * widgets allows to trace transparently without seeing the association class.
 	 * 
 	 * @param eObject the {@link EObject}
 	 * @return true, if it is an association
 	 */
 	boolean isAssociationClassElement(EObject eObject);
 
 	/**
 	 * Whether a {@link EClass} is a association class. Association classes are not displayed as dedicated elements. A
 	 * link from one element to another which goes over an association class is displayed by a dedicated widget. This
 	 * widgets allows to trace transparently without seeing the association class.
 	 * 
 	 * @param eClazz the {@link EClass}
 	 * @return true, if it is an association
 	 */
 	boolean isAssociationClassElement(EClass eClazz);
 
 	/**
 	 * Returns an {@link ECPAssociationClassElement} wrapper for a {@link EObject}.
 	 * 
 	 * @param eObject the {@link EObject}
 	 * @return the wrapper, {@code null} if {@link EObject} not exists
 	 */
 	ECPAssociationClassElement getAssociationClassElement(EObject eObject);
 
 	/**
 	 * @param newMEInstance {@link EObject} the new modelElement instance.
 	 * @return EReference the Container
 	 * @param parent The EObject to get containment references from
 	 */
 	EReference getPossibleContainingReference(final EObject newMEInstance, EObject parent);
 
 	/**
 	 * If the meta model context is guessing the packages. Happens if no model package is registered for EMF Client
 	 * Platform.
 	 * 
 	 * @return if the context is guessed.
 	 */
	boolean isGuessed();
 }
